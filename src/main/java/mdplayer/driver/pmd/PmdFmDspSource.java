/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.pmd;

import java.util.Arrays;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.fmdsp.Notes;
import musicDriverInterface.MetaData.Tag;
import pmd.driver.PW;
import pmd.driver.PW.partWork;
import vavi.sound.visualizer.fmdsp.FftAnalyzer;
import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.sound.visualizer.fmdsp.FmDspDataSource;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackInfo;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.sound.visualizer.fmdsp.TrackStatusSource;
import vavi.sound.visualizer.fmdsp.WorkStateSource;
import vavi.util.event.GenericEvent;


/**
 * Feeds {@link FmDspVisualizer} from PMD.
 * <p>
 * Subscribe {@link #update(GenericEvent)} to the audio event stream: the "pmd" event carries the
 * driver work area ({@link PW}, ~120 Hz) and the "master" event carries the rendered PCM, which is
 * where the spectrum comes from.
 * <p>
 * PMD's own note state maps onto fmdsp almost one to one (fmdsp was written for it), so the track
 * rows, the keyboard and the clock are exact. The level meters are not: neither PMD nor the chip
 * emulation publishes a per channel sample peak, so they are synthesized from the part volume and
 * a decay envelope retriggered on every key on.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-13 nsano initial version <br>
 */
public class PmdFmDspSource implements FmDspDataSource, LevelDataSource, TrackStatusSource, WorkStateSource {

    /** how fast a held note's meter sags, per "pmd" event */
    private static final double levelSustainDecay = 0.995;

    /** how far a held note's meter sags, as a fraction of the part volume */
    private static final double levelSustain = 0.6;

    /** how fast a released note's meter falls, per "pmd" event */
    private static final double levelRelease = 0.95;

    /** the drum part has no volume of its own, meter it at a fixed height */
    private static final double drumLevel = 0.75;

    /** level meter channel of the drum part */
    private static final int drumChannel = 9;

    /** PMD part of each {@link TrackId}, -1 when the part doesn't exist in this configuration */
    private final int[] parts = new int[TrackId.COUNT];

    /** latest snapshot, written by the driver thread, read by the EDT */
    private final TrackStatus[] tracks = new TrackStatus[TrackId.COUNT];

    private final boolean[] masks = new boolean[TrackId.COUNT];

    /** total tick count of the note being played, PMD only tells us the remaining ones */
    private final int[] ticks = new int[TrackId.COUNT];

    /** {@link partWork#keyon_flag} of the previous snapshot, a change means a new note */
    private final int[] keyOns = new int[TrackId.COUNT];

    private final double[] envelopes = new double[LevelDataSource.COUNT];

    private final Pan[] pans = new Pan[LevelDataSource.COUNT];

    // work state

    /**
     * the live work area. The elapsed time, the tempo and the loop count are read straight from it
     * when the renderer asks for them, so the clock has the resolution of the display and not of
     * the "pmd" event.
     */
    private volatile PW work;
    private volatile BaseDriver baseDriver;

    private volatile long timerBCount;
    private volatile long loopTimerBCount;
    private volatile long timerBCountLoop;
    private volatile boolean paused;

    private long lastTimeCounter;
    private long loopStartTimerBCount;
    private int lastLoopCount;
    /** TimerB fraction carried over between two snapshots, in 55467 Hz steps */
    private double timerBStep;

    private String filename;
    private final String[] comments = new String[3];

    private final FftAnalyzer fft = new FftAnalyzer(Common.VGMProcSampleRate);

    public PmdFmDspSource() {
        Arrays.setAll(tracks, i -> new TrackStatus());
        Arrays.fill(pans, Pan.CENTER);
        reset();
    }

    /**
     * Clears everything that belongs to one song. A song brings its own driver, whose sample
     * counter starts at 0 again, while the displayed clock only ever moves forwards - so without
     * this the clock would stay frozen at the previous song's end until the new one rendered past
     * it. Call it before every song of a play list.
     */
    public void reset() {
        Arrays.stream(tracks).forEach(PmdFmDspSource::clear);
        Arrays.fill(pans, Pan.CENTER);
        Arrays.fill(ticks, 0);
        Arrays.fill(keyOns, 0);
        Arrays.fill(envelopes, 0);
        Arrays.fill(comments, null);
        commented = false;
        work = null;
        paused = false;
        timerBCount = 0;
        timerBStep = 0;
        lastTimeCounter = 0;
        loopTimerBCount = 0;
        loopStartTimerBCount = 0;
        timerBCountLoop = 0;
        lastLoopCount = 0;
        drumKeyOn = 0;
        clock = 0;
        lastClockNanos = 0;
        shownFrames = 0;
    }

    /** the title shown on the file bar */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** true while the player is paused, {@link PW} doesn't know about it */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @param event "pmd" ... args 0: {@link PW}, "master" ... args 0: pcm buffer, 1: offset
     */
    public void update(GenericEvent event) {
        switch (event.getName()) {
        case "pmd" -> {
            if (event.getSource() instanceof BaseDriver d) {
                baseDriver = d;
                if (!commented) {
                    commented = true;
                    comments[0] = d.metaData.getFirst(Tag.Title);
                    comments[1] = d.metaData.getFirst(Tag.Composer);
                    comments[2] = d.metaData.getFirst(Tag.Arranger);
                }
            }
            snapshot((PW) event.getArguments()[0]);
        }
        case "master" -> {
            short[] buffer = (short[]) event.getArguments()[0];
            int offset = (int) event.getArguments()[1];
            for (int i = offset; i + 1 < buffer.length; i += 2) {
                fft.push((buffer[i] + buffer[i + 1]) / 2f);
            }
        }
        default -> {
        }
        }
    }

    private boolean commented;

    // ----- snapshot -----

    private void snapshot(PW pw) {
        if (pw.partWk == null) return;

        mapParts(pw);

        for (TrackId t : TrackId.values()) {
            int i = t.ordinal();
            TrackStatus status = tracks[i];
            int p = parts[i];
            if (p < 0 || p >= pw.partWk.length) {
                clear(status);
                masks[i] = false;
                continue;
            }
            partWork part = pw.partWk[p];
            masks[i] = part.partmask != 0;

            status.playing = part.address != 0;
            boolean isFm3ExTrack = (t == TrackId.FM_3 || t == TrackId.FM_3_EX_1 || t == TrackId.FM_3_EX_2 || t == TrackId.FM_3_EX_3);
            if (isFm3ExTrack && (part.slotmask & 0xff) != 0xf0) {
                status.info = TrackInfo.FM3EX;
            } else {
                status.info = infoOf(t);
            }
            status.key = part.onkai & 0xff;
            status.actualKey = actualKeyOf(t, part, status.key);
            status.toneNum = part.voicenum & 0xff;
            status.volume = part.volume & 0xff;
            status.gate = part.qdat & 0xff;
            // fmdsp prints the detune as three digits and a sign, PMD counts it in f-number steps
            // and does not bound it - clamp the way the C fmdsp does so a deep detune cannot run
            // over the field
            status.detune = Math.max(-128, Math.min(127, part.detune));
            status.status = LFO_STATUS[part.lfoswi & 0xff];
            status.ticksLeft = part.leng & 0xff;
            status.ppz8Ch = status.info == TrackInfo.PPZ8 ? i - TrackId.PPZ8_1.ordinal() + 1 : 0;
            if (status.info == TrackInfo.SSG) {
                // the mixer bits PMD lets through for this SSG channel: tone in 0-2, noise in 3-5
                int ssg = p - pw.part7;
                status.ssgTone = (part.psgpat & (1 << ssg)) != 0;
                status.ssgNoise = (part.psgpat & (8 << ssg)) != 0;
                status.ssgNoiseFreq = pw.psnoi & 0x1f;
                if (t == TrackId.SSG_3 && pw.effon != 0) {
                    status.info = TrackInfo.SSGEFF;
                    status.toneNum = pw.psgefcnum & 0xff;
                    if (pw.eswthz != 0) status.ssgTone = true;
                    if (pw.eswnhz != 0) status.ssgNoise = true;
                }
            } else {
                status.ssgTone = false;
                status.ssgNoise = false;
                status.ssgNoiseFreq = 0;
            }
            for (int c = 0; c < 4; c++) {
                status.fmSlotMask[c] = (part.slotmask & (0x10 << c)) == 0;
            }

            boolean keyOn = keyOns[i] != (part.keyon_flag & 0xff);
            keyOns[i] = part.keyon_flag & 0xff;
            if (keyOn) ticks[i] = status.ticksLeft;
            status.ticks = ticks[i];

            boolean sounding = status.playing && status.key != 0xff && part.keyoff_flag == 0;
            level(t, part, keyOn && status.key != 0xff, sounding);
        }

        // the drum part is a level meter only, it has no track row
        if (pw.part11 < pw.partWk.length) {
            partWork drum = pw.partWk[pw.part11];
            if (drumKeyOn != (drum.keyon_flag & 0xff) && drum.address != 0) {
                envelopes[drumChannel] = drumLevel;
            }
            drumKeyOn = drum.keyon_flag & 0xff;
            envelopes[drumChannel] *= levelRelease;
        }

        state(pw);
    }

    private int drumKeyOn;

    /**
     * Drives the level meter of {@code t}'s channel: it jumps to the part volume on key on, sags
     * towards the sustain level while the key is held, and releases once the part keys off or
     * rests. Note that this is an envelope, not a measurement - see the class comment.
     */
    private void level(TrackId t, partWork part, boolean keyOn, boolean sounding) {
        int c = levelChannelOf(t);
        if (c < 0) return;

        double volume = volumeOf(t, part);
        if (keyOn) {
            envelopes[c] = volume;
        } else if (sounding) {
            envelopes[c] = Math.max(envelopes[c] * levelSustainDecay, volume * levelSustain);
        } else {
            envelopes[c] *= levelRelease;
        }
        pans[c] = panOf(t, part);
    }

    /**
     * Amplitude 0..1 of a part at its current volume. The meter is read back as dBFS, so the
     * volumes have to be un-logged first: an FM volume is a total level, one step of 0.75 dB below
     * full scale at 127, and an SSG volume is one 3 dB step of the chip's 16 level table. The PCM
     * parts - ADPCM and PPZ8, both of which default to 128 - are linear over 0..255.
     */
    private static double volumeOf(TrackId t, partWork part) {
        int v = part.volume & 0xff;
        double db = switch (t) {
            case SSG_1, SSG_2, SSG_3 -> (Math.min(v, 15) - 15) * 3.0;
            case ADPCM,
                 PPZ8_1, PPZ8_2, PPZ8_3, PPZ8_4, PPZ8_5, PPZ8_6, PPZ8_7, PPZ8_8
                    -> 20 * Math.log10(Math.max(v, 1) / 255.0);
            default -> (Math.min(v, 127) - 127) * 0.75; // FM
        };
        return Math.pow(10, db / 20);
    }

    /** only the FM and the ADPCM parts carry a pan, the OPNA register 0xb4 bits */
    private static Pan panOf(TrackId t, partWork part) {
        switch (t) {
        case SSG_1, SSG_2, SSG_3:
            return Pan.CENTER;
        default:
            boolean left = (part.fmpan & 0x80) != 0;
            boolean right = (part.fmpan & 0x40) != 0;
            if (left && right) return Pan.CENTER;
            if (left) return Pan.LEFT;
            if (right) return Pan.RIGHT;
            return Pan.NONE;
        }
    }

    private static int levelChannelOf(TrackId t) {
        return switch (t) {
            case FM_1 -> 0;
            case FM_2 -> 1;
            case FM_3, FM_3_EX_1, FM_3_EX_2, FM_3_EX_3 -> 2;
            case FM_4 -> 3;
            case FM_5 -> 4;
            case FM_6 -> 5;
            case SSG_1 -> 6;
            case SSG_2 -> 7;
            case SSG_3 -> 8;
            case ADPCM -> 10;
            default -> 11 + t.ordinal() - TrackId.PPZ8_1.ordinal();
        };
    }

    private static TrackInfo infoOf(TrackId t) {
        return switch (t) {
            case SSG_1, SSG_2, SSG_3 -> TrackInfo.SSG;
            case PPZ8_1, PPZ8_2, PPZ8_3, PPZ8_4, PPZ8_5, PPZ8_6, PPZ8_7, PPZ8_8 -> TrackInfo.PPZ8;
            default -> TrackInfo.NORMAL;
        };
    }

    /**
     * The eight character mnemonic fmdsp shows behind "M:", for every value of PMD's
     * {@link partWork#lfoswi}: the two software LFOs, each as pitch / volume / sync, then a slot
     * fmdsp leaves blank, then portamento. Same letters and same order as the C fmdsp.
     * <p>
     * A table because the snapshot runs at ~120 Hz over sixteen tracks and the string would
     * otherwise be built from scratch every time; there are only 256 of them.
     */
    private static final String[] LFO_STATUS = new String[256];

    static {
        for (int i = 0; i < LFO_STATUS.length; i++) {
            LFO_STATUS[i] = new String(new char[] {
                    (i & 0x01) != 0 ? 'P' : '-',  // LFO 1 pitch
                    (i & 0x02) != 0 ? 'A' : '-',  // LFO 1 volume
                    (i & 0x04) != 0 ? 'S' : '-',  // LFO 1 sync
                    (i & 0x10) != 0 ? 'P' : '-',  // LFO 2 pitch
                    (i & 0x20) != 0 ? 'A' : '-',  // LFO 2 volume
                    (i & 0x40) != 0 ? 'S' : '-',  // LFO 2 sync
                    '-',
                    (i & 0x08) != 0 ? 'P' : '-',  // portamento
            });
        }
    }

    /**
     * The key the chip is really playing, which is the MML key moved by everything that bends it
     * after the note was set: portamento, detune and the two pitch LFOs. fmdsp draws it on the
     * keyboard in its own color, so a note bent away from its MML key lights a second key - which
     * is the whole point of the keyboard having two of them, and what makes a portamento visible
     * as it slides.
     * <p>
     * The C fmdsp reads PMD's {@code output_freq}, the value the driver last wrote to the chip, and
     * turns it back into a note. PMD's work area has no such field, so the write is recomputed here
     * from the note and the four things PMD adds to it - see {@link #outputFreqOf}.
     */
    private static int actualKeyOf(TrackId t, partWork part, int key) {
        if ((key & 0xf) == 0xf) return 0xff; // resting, or no note yet
        int freq = outputFreqOf(t, part);
        if (freq < 0) return key;
        return switch (t) {
            case SSG_1, SSG_2, SSG_3 -> Notes.ssgKeyOf(freq);
            case ADPCM -> adpcmKeyOf(freq);
            case PPZ8_1, PPZ8_2, PPZ8_3, PPZ8_4, PPZ8_5, PPZ8_6, PPZ8_7, PPZ8_8 -> Notes.ppz8KeyOf(freq);
            default -> Notes.fmKeyOf(freq);
        };
    }

    /**
     * The value PMD is about to write to the part's pitch register, or -1 when it has none to
     * write. Mirrors PMD's own {@code otodasi} (FM), {@code otodasip} (SSG), {@code otodasim}
     * (ADPCM) and {@code otodasiz} (PPZ8), stopping short of the write itself.
     * <p>
     * Each of them starts from {@link partWork#fnum}, the bare note, and adds the portamento, the
     * detune and whichever of the two software LFOs is set to bend the pitch - though no two agree
     * on how, the FM one carrying its block along and the PPZ8 one scaling rather than shifting.
     * FM3 in effect mode detunes each slot separately and is not followed here; the keyboard shows
     * one key per row either way.
     */
    private static int outputFreqOf(TrackId t, partWork part) {
        int lfo = 0;
        if ((part.lfoswi & 0x01) != 0) lfo += part.lfodat;
        if ((part.lfoswi & 0x10) != 0) lfo += part._lfodat;

        if (t.ordinal() >= TrackId.PPZ8_1.ordinal()) {
            long base = ((part.fnum2 & 0xffffL) << 16) | (part.fnum & 0xffffL);
            if (base == 0) return -1;
            // PPZ8 counts a playback rate, so PMD scales it by the bend instead of shifting it
            long freq = base + ((long) part.porta_num << 4)
                    + (long) (lfo + part.detune) * ((base >> 8) & 0xffff);
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, freq));
        }

        int base = part.fnum & 0xffff;
        if (base == 0) return -1;

        switch (t) {
        case SSG_1, SSG_2, SSG_3 -> {
            int period = base + part.porta_num;
            if ((part.extendmode & 1) == 0) {
                // a period, so PMD subtracts here what it adds everywhere else
                period -= part.detune + lfo;
            } else {
                // extended detune scales the period instead of shifting it, so that the same
                // detune is the same interval whatever octave the part plays in
                period = extendDetune(period, part.detune);
                period = extendDetune(period, lfo);
            }
            period &= 0xffff;
            if (period >= 0x1000) period = (period & 0x8000) != 0 ? 0 : 0xfff;
            return period;
        }
        case ADPCM -> {
            // the ADPCM row is the ADPCM row only under PMDB2 - PMD86 and the PPZ8 emulation play
            // it from a rate pair this does not know how to read, and neither bends it
            if (part.fnum2 != 0) return -1;
            // it is hard to hear an LFO on a sample, so PMD applies it four times as deep
            int rate = base + part.porta_num + lfo * 4 + part.detune;
            return Math.max(0, Math.min(0xffff, rate));
        }
        default -> {
            // the f-number leaves its block only to be renormalized back into register range
            return blkFnum(base & 0x3800, (base & 0x7ff) + part.porta_num + part.detune + lfo);
        }
        }
    }

    /** PMD's extended detune: {@code value} shifted by {@code detune}/4096th of itself */
    private static int extendDetune(int value, int detune) {
        if (detune == 0) return value;
        int product = ((short) value * (short) detune) << 4;
        int shift = (short) (product >> 16) + (product >= 0 ? 1 : -1);
        return (short) (value - shift);
    }

    /**
     * PMD's {@code fm_block_calc}: an f-number bent out of the register's range is carried into the
     * next block, which on OPN means halving or doubling - and PMD does that by the 0x26a that
     * separates the two ends of the range, so this is what really decides the pitch once a bend
     * crosses an octave and not the f-number alone.
     */
    private static int blkFnum(int blk, int fnum) {
        fnum &= 0xffff;
        for (;;) {
            if ((fnum & 0x8000) != 0 || fnum < 0x26a) {
                if (blk < 0x800) { // as low as the chip goes
                    blk = 0;
                    if ((fnum & 0x8000) != 0 || fnum < 8) fnum = 8;
                    break;
                }
                blk -= 0x800;
                fnum = (fnum + 0x26a) & 0xffff;
                continue;
            }
            if (fnum < 0x4d4) break;
            blk += 0x800;
            if (blk != 0x4000) {
                fnum = (fnum - 0x26a) & 0xffff;
                continue;
            }
            blk = 0x3800; // as high as the chip goes
            if (fnum > 0x7ff) fnum = 0x7ff;
            break;
        }
        return blk | fnum;
    }

    /**
     * MML key of an OPNA ADPCM delta-N, as PMD writes it: the table is the delta-N half a semitone
     * above each note of one octave, at the rate PMD plays its samples back at.
     */
    private static int adpcmKeyOf(int freq) {
        if (freq == 0) return 0x00;
        int octave = 5;
        freq &= 0xffff;
        while ((freq & 0x8000) == 0) {
            freq <<= 1;
            octave--;
        }
        int key = 0;
        while (key < 12 && freq >= ADPCM_FREQ[key]) key++;
        key += 5;
        if (key >= 12) {
            key -= 12;
            octave++;
        }
        if (octave < 0) return 0x00;
        if (octave > 8) return 0x8b;
        return octave << 4 | key;
    }

    /** delta-N half a semitone above e - d+, the octave a normalized ADPCM delta-N spans */
    private static final int[] ADPCM_FREQ = {
            0x8738, 0x8f42, 0x97c7, 0xa0cd, 0xaa5d, 0xb47e,
            0xbf3a, 0xca99, 0xd6a5, 0xe369, 0xf0ee, 0xff42,
    };

    /**
     * FM 4-6 only exist with a sound board 2 and PPZ8 only with the PPZ8 driver;
     * PMD leaves the part index of the others at 0, which would alias FM 1.
     */
    private void mapParts(PW pw) {
        parts[TrackId.FM_1.ordinal()] = pw.part1;
        parts[TrackId.FM_2.ordinal()] = pw.part2;
        parts[TrackId.FM_3.ordinal()] = pw.part3;
        parts[TrackId.FM_3_EX_1.ordinal()] = pw.part3b;
        parts[TrackId.FM_3_EX_2.ordinal()] = pw.part3c;
        parts[TrackId.FM_3_EX_3.ordinal()] = pw.part3d;
        parts[TrackId.FM_4.ordinal()] = pw.board2 != 0 ? pw.part4 : -1;
        parts[TrackId.FM_5.ordinal()] = pw.board2 != 0 ? pw.part5 : -1;
        parts[TrackId.FM_6.ordinal()] = pw.board2 != 0 ? pw.part6 : -1;
        parts[TrackId.SSG_1.ordinal()] = pw.part7;
        parts[TrackId.SSG_2.ordinal()] = pw.part8;
        parts[TrackId.SSG_3.ordinal()] = pw.part9;
        parts[TrackId.ADPCM.ordinal()] = pw.part10;
        int[] ppz8 = {pw.part10a, pw.part10b, pw.part10c, pw.part10d, pw.part10e, pw.part10f, pw.part10g, pw.part10h};
        for (int i = 0; i < ppz8.length; i++) {
            parts[TrackId.PPZ8_1.ordinal() + i] = pw.ppz != 0 ? ppz8[i] : -1;
        }
    }

    private static void clear(TrackStatus status) {
        status.playing = false;
        status.info = TrackInfo.NORMAL;
        status.ticks = 0;
        status.ticksLeft = 0;
        status.key = 0xff;
        status.actualKey = 0xff;
        status.toneNum = 0;
        status.volume = 0;
        status.gate = 0;
        status.detune = 0;
        status.status = "";
        status.ppz8Ch = 0;
        status.ssgTone = false;
        status.ssgNoise = false;
        status.ssgNoiseFreq = 0;

        Arrays.fill(status.fmSlotMask, false);
    }

    /**
     * PMD counts rendered samples, not TimerB ticks, so the tick counter fmdsp wants for its clock
     * and its loop bar is rebuilt the way {@code OPNATimer} does it: one TimerB step is
     * {@code baseClock / 72 / 2} = 55467 Hz, and TimerB overflows every {@code (256 - tempo) * 16}
     * steps.
     */
    private void state(PW pw) {
        int overflow = (256 - (pw.tempo_d & 0xff)) << 4;

        long counter = baseDriver != null ? baseDriver.counter : 0;
        timerBStep += (counter - lastTimeCounter)
                * (PmdDriver.baseClock / 72.0 / 2.0 / Common.VGMProcSampleRate);
        lastTimeCounter = counter;
        while (timerBStep >= overflow) {
            timerBStep -= overflow;
            timerBCount++;
        }

        int loop = Math.max(pw.getnowLoopCounter(), 0);
        if (loop != lastLoopCount) {
            if (timerBCount > loopStartTimerBCount) {
                loopTimerBCount = timerBCount - loopStartTimerBCount;
            }
            loopStartTimerBCount = timerBCount;
            lastLoopCount = loop;
        }
        timerBCountLoop = timerBCount - loopStartTimerBCount;

        work = pw;
    }

    // ----- FmDspDataSource -----

    @Override public FftDataSource fft() { return fft; }

    @Override public LevelDataSource level() { return this; }

    @Override public TrackStatusSource trackStatus() { return this; }

    @Override public WorkStateSource work() { return this; }

    // ----- TrackStatusSource -----

    @Override
    public void readStatus(TrackId track, TrackStatus out) {
        TrackStatus status = tracks[track.ordinal()];
        out.playing = status.playing;
        out.info = status.info;
        out.ticks = status.ticks;
        out.ticksLeft = status.ticksLeft;
        out.key = status.key;
        out.actualKey = status.actualKey;
        out.toneNum = status.toneNum;
        out.volume = status.volume;
        out.gate = status.gate;
        out.detune = status.detune;
        out.status = status.status;
        out.ppz8Ch = status.ppz8Ch;
        out.ssgTone = status.ssgTone;
        out.ssgNoise = status.ssgNoise;
        out.ssgNoiseFreq = status.ssgNoiseFreq;

        System.arraycopy(status.fmSlotMask, 0, out.fmSlotMask, 0, out.fmSlotMask.length);
    }

    @Override
    public boolean masked(TrackId track) {
        return masks[track.ordinal()];
    }

    // ----- LevelDataSource -----

    @Override
    public int level(int channel) {
        return (int) (envelopes[channel] * 32767);
    }

    @Override
    public Pan pan(int channel) {
        return pans[channel];
    }

    // ----- WorkStateSource -----

    @Override public int sampleRate() { return Common.VGMProcSampleRate; }

    /** time constant [s] with which the displayed clock is pulled onto the driver's own counter */
    private static final double clockSlew = 0.2;

    /**
     * The render loop blocks on the audio line and then renders a whole device period at once, so
     * PMD's sample counter advances in ~10 ms bursts of several hundred samples - reading it
     * straight makes the clock stutter. Playback is real time, so the displayed clock runs off the
     * wall clock instead and is slewed towards the driver's counter, which keeps it smooth without
     * letting it drift. It is frozen while paused and never runs backwards.
     */
    @Override
    public long generatedFrames() {
        PW pw = work;
        if (pw == null) return 0;

        long counter = pw.timeCounter;
        long now = System.nanoTime();
        if (lastClockNanos == 0) {
            clock = counter;
            lastClockNanos = now;
        }
        double elapsed = (now - lastClockNanos) / 1e9;
        lastClockNanos = now;

        if (!paused && playing()) {
            clock += elapsed * sampleRate();
        }
        // time based, so the result does not depend on how often the renderer asks
        clock += (counter - clock) * (1 - Math.exp(-elapsed / clockSlew));
        // a stall (a long pause, a seek) is not worth slewing through
        if (Math.abs(counter - clock) > sampleRate()) {
            clock = counter;
        }

        shownFrames = Math.max(shownFrames, (long) clock);
        return shownFrames;
    }

    private double clock;
    private long lastClockNanos;
    private long shownFrames;

    @Override public long timerBCount() { return timerBCount; }

    @Override public int timerB() { PW pw = work; return pw != null ? pw.tempo_d & 0xff : 0; }

    @Override public int loopCount() { PW pw = work; return pw != null ? Math.max(pw.getnowLoopCounter(), 0) : 0; }

    @Override public long loopTimerBCount() { return loopTimerBCount; }

    @Override public long timerBCountLoop() { return timerBCountLoop; }

    @Override public boolean playing() { PW pw = work; return pw != null && pw.getStatus() > 0; }

    @Override public boolean paused() { return paused; }

    @Override public String driverName() { return "PMD"; }

    @Override public String chips() { PW pw = work; return (pw != null && pw.ppz != 0) ? "YM2608 PPZ8" : "YM2608"; }

    @Override public String filename() { return filename; }

    @Override public String comment(int line) { return comments[line]; }

    @Override
    public String pcmType(int index) {
        return switch (index) {
            case 0 -> "PPC";
            case 1 -> "PPZ1";
            case 2 -> "PPZ2";
            case 3 -> "PPS";
            default -> null;
        };
    }

    @Override
    public String pcmFilename(int index) {
        PW pw = work;
        if (pw == null) return null;
        return switch (index) {
            case 0 -> pw.ppcFile;
            case 1 -> pw.ppz1File;
            case 2 -> pw.ppz2File;
            case 3 -> pw.ppsFile;
            default -> null;
        };
    }

    @Override
    public boolean pcmError(int index) {
        PW pw = work;
        if (pw == null) return false;
        return switch (index) {
            case 0 -> pw.ppcError;
            case 1 -> pw.ppz1Error;
            case 2 -> pw.ppz2Error;
            case 3 -> pw.ppsError;
            default -> false;
        };
    }
}
