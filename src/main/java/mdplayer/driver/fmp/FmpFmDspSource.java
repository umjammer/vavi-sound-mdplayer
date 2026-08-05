/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mdplayer.Common;
import mdplayer.chips.SegaPcmChip;
import mdplayer.driver.BaseDriver;
import mdplayer.fmdsp.Notes;
import mdplayer.lib.fmp.FmpWork;
import musicDriverInterface.MetaData.Tag;
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
 * Feeds {@link FmDspVisualizer} from FMP.
 * <p>
 * Subscribe {@link #update(GenericEvent)} to the audio event stream: the "fmp" event carries
 * {@link FmpWork} (~120 Hz) and the "master" event carries the rendered PCM, which is where the
 * spectrum comes from.
 * <p>
 * FMDSP was written for FMP as much as for PMD, so the track rows, the keyboard and the clock are
 * exact. Two things are not:
 * <ul>
 * <li>the level meters. Neither FMP nor the chip emulation publishes a per channel sample peak, so
 * they are synthesized from the part volume and a decay envelope retriggered on every key on.</li>
 * <li>the PPZ8 rows. FMP has no PPZ8 parts of its own - in a PDZF song the PPZ8 channels are played
 * by the FM3 extended parts and the ADPCM part - so those rows are read from the chip itself, which
 * knows the rate a channel plays at but not the note the part meant, see {@link #ppz8}.</li>
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-14 nsano initial version <br>
 */
public class FmpFmDspSource implements FmDspDataSource, LevelDataSource, TrackStatusSource, WorkStateSource {

    /** how fast a held note's meter sags, per "fmp" event */
    private static final double levelSustainDecay = 0.995;

    /** how far a held note's meter sags, as a fraction of the part volume */
    private static final double levelSustain = 0.6;

    /** how fast a released note's meter falls, per "fmp" event */
    private static final double levelRelease = 0.95;

    /** the rhythm part has no volume of its own, meter it at a fixed height */
    private static final double rhythmLevel = 0.75;

    /** level meter channel of the rhythm part */
    private static final int rhythmChannel = 9;

    /** FMP part of each {@link TrackId}, -1 for the PPZ8 rows, which FMP has no part for */
    private final int[] parts = new int[TrackId.COUNT];

    /** latest snapshot, written by the driver thread, read by the EDT */
    private final TrackStatus[] tracks = new TrackStatus[TrackId.COUNT];

    /** total tick count of the note being played, FMP only tells us the remaining ones */
    private final int[] ticks = new int[TrackId.COUNT];

    /** note of the previous snapshot, a change means a new note */
    private final int[] notes = new int[TrackId.COUNT];

    private final double[] envelopes = new double[LevelDataSource.COUNT];

    private final Pan[] pans = new Pan[LevelDataSource.COUNT];

    /**
     * the live work area. The tempo and the loop count are read straight from it when the renderer
     * asks for them, so they have the resolution of the display and not of the "fmp" event.
     */
    private volatile FmpWork work;

    private volatile boolean paused;

    private long loopTimerBCount;
    private long loopStartTicks;
    private int lastLoopCount;

    private String filename;
    private final String[] comments = new String[3];

    private final FftAnalyzer fft = new FftAnalyzer(Common.VGMProcSampleRate);

    public FmpFmDspSource() {
        Arrays.setAll(tracks, i -> new TrackStatus());
        mapParts();
        reset();
    }

    /**
     * Clears everything that belongs to one song. A song brings its own driver, whose sample
     * counter starts at 0 again, while the displayed clock only ever moves forwards - so without
     * this the clock would stay frozen at the previous song's end until the new one rendered past
     * it. Call it before every song of a play list.
     */
    public void reset() {
        Arrays.stream(tracks).forEach(FmpFmDspSource::clear);
        Arrays.fill(pans, Pan.CENTER);
        Arrays.fill(notes, -1);
        Arrays.fill(ticks, 0);
        Arrays.fill(envelopes, 0);
        Arrays.fill(ppz8KeyOns, false);
        Arrays.fill(comments, null);
        commented = false;
        work = null;
        paused = false;
        frames = 0;
        stopped = false;
        loopTimerBCount = 0;
        loopStartTicks = 0;
        lastLoopCount = 0;
        clock = 0;
        lastClockNanos = 0;
        shownFrames = 0;
    }

    /** the title shown on the file bar */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** true while the player is paused, {@link FmpWork} doesn't know about it */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @param event "fmp" ... args 0: {@link FmpWork}, "master" ... args 0: pcm buffer, 1: offset
     */
    public void update(GenericEvent event) {
        switch (event.getName()) {
        case "fmp" -> {
            if (event.getSource() instanceof BaseDriver driver) {
                if (!commented) {
                    commented = true;
                    List<String> list = new ArrayList<>();

                    // an FMC memo is a screen image, so it is displayed as it was laid out -
                    // the tags are the same text with the indent stripped, so they would lose it
                    if (driver.comments() != null) {
                        list.addAll(List.of(driver.comments()));
                    }

                    if (list.isEmpty()) {
                        addLines(list, driver.metaData.getFirst(Tag.Title));

                        String comp = driver.metaData.getFirst(Tag.Composer);
                        if (comp.isEmpty()) comp = driver.metaData.getFirst(Tag.ComposerJ);
                        addLines(list, comp);

                        String arr = driver.metaData.getFirst(Tag.Arranger);
                        if (arr.isEmpty()) arr = driver.metaData.getFirst(Tag.Note);
                        if (arr.isEmpty()) arr = driver.metaData.getFirst(Tag.Maker);
                        addLines(list, arr);
                    }

                    for (int i = 0; i < 3; i++) {
                        comments[i] = i < list.size() ? list.get(i) : null;
                    }
                }
                frames = driver.counter;
                stopped = driver.stopped;
            }
            snapshot((FmpWork) event.getArguments()[0]);
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

    /** samples rendered so far, which is what the elapsed time is counted in */
    private volatile long frames;

    private volatile boolean stopped;

    // ----- snapshot -----

    private void snapshot(FmpWork fw) {
        if (!fw.valid()) return;

        for (TrackId t : TrackId.values()) {
            int i = t.ordinal();
            TrackStatus status = tracks[i];
            int p = parts[i];
            if (p < 0) {
                clear(status);
                continue;
            }

            status.playing = fw.playing(p);
            status.info = infoOf(t);
            int note = fw.note(p);
            status.key = keyOf(note);
            status.actualKey = actualKeyOf(t, fw, p, status.key);
            status.toneNum = fw.toneNum(p);
            status.volume = fw.volume(p);
            status.detune = fw.detune(p);
            status.status = statusOf(fw, p);
            status.ticksLeft = fw.ticksLeft(p);
            if (status.info == TrackInfo.SSG) {
                // the mixer bits FMP lets through for this SSG channel: tone in 0-2, noise in 3-5
                int ssg = p - FmpWork.SSG_1;
                status.ssgTone = (fw.ssgMixer & (1 << ssg)) == 0;
                status.ssgNoise = (fw.ssgMixer & (8 << ssg)) == 0;
                status.ssgNoiseFreq = fw.ssgNoiseFreq & 0x1f;
            } else {
                status.ssgTone = false;
                status.ssgNoise = false;
                status.ssgNoiseFreq = 0;
            }
            boolean isFm3ExTrack = (t == TrackId.FM_3 || t == TrackId.FM_3_EX_1 || t == TrackId.FM_3_EX_2 || t == TrackId.FM_3_EX_3);
            if (isFm3ExTrack) {
                int mask = fw.slotMask(p);
                if ((mask & 0xf0) != 0) {
                    status.info = TrackInfo.FM3EX;
                    for (int c = 0; c < 4; c++) {
                        status.fmSlotMask[c] = (mask & (1 << (4 + c))) != 0;
                    }
                } else {
                    status.info = TrackInfo.NORMAL;
                    Arrays.fill(status.fmSlotMask, false);
                }
            } else {
                Arrays.fill(status.fmSlotMask, false);
            }

            boolean keyOn = note >= 0 && notes[i] != note;
            notes[i] = note;
            if (keyOn) ticks[i] = status.ticksLeft;
            status.ticks = ticks[i];
            status.gate = fw.gate(p, status.ticks);

            level(t, fw, p, keyOn, fw.keyOn(p));
        }

        ppz8(fw);

        // the rhythm part is a level meter only, it has no track row
        if (fw.rhythmKeyOn != 0) {
            fw.rhythmKeyOn = 0;
            envelopes[rhythmChannel] = rhythmLevel;
        } else {
            envelopes[rhythmChannel] *= levelRelease;
        }

        loop(fw);

        work = fw;
    }

    /**
     * The PPZ8 rows, which come from the chip and not from FMP: a PDZF song plays PPZ8 from its FM3
     * extended and ADPCM parts, so the parts say nothing about the eight PPZ8 channels, while
     * {@link mdplayer.chips.Ppz8Chip} knows all of them. The chip has no note, only a playback rate,
     * so the key is the note whose pitch that rate comes closest to.
     */
    private void ppz8(FmpWork fw) {
        Map<String, Object> info = fw.ppz8;
        for (int ch = 0; ch < 8; ch++) {
            int i = TrackId.PPZ8_1.ordinal() + ch;
            TrackStatus status = tracks[i];
            int c = LevelDataSource.COUNT - 8 + ch;
            if (info == null || !info.containsKey("channels." + ch + ".playing")) {
                clear(status);
                envelopes[c] *= levelRelease;
                continue;
            }

            boolean playing = (boolean) info.get("channels." + ch + ".playing");
            boolean keyOn = (boolean) info.get("channels." + ch + ".keyOn");
            int volume = (int) info.get("channels." + ch + ".volume");
            int pan = (int) info.get("channels." + ch + ".pan");
            int note = playing
                    ? SegaPcmChip.searchSegaPCMNote((int) info.get("channels." + ch + ".frequency") / (double) 0x8000)
                    : -1;

            clear(status);
            status.playing = playing;
            status.info = TrackInfo.PPZ8;
            status.key = keyOf(note);
            status.actualKey = status.key;
            status.volume = volume;
            status.toneNum = (int) info.get("channels." + ch + ".flg16");
            status.ppz8Ch = ch + 1;

            // one step of PPZ8's 16 level table is 1.5 dB
            double amplitude = playing ? Math.pow(10, (Math.min(volume, 15) - 15) * 1.5 / 20) : 0;
            if (keyOn && (!ppz8KeyOns[ch] || note != notes[i])) {
                envelopes[c] = amplitude;
            } else if (playing) {
                envelopes[c] = Math.max(envelopes[c] * levelSustainDecay, amplitude * levelSustain);
            } else {
                envelopes[c] *= levelRelease;
            }
            ppz8KeyOns[ch] = keyOn;
            notes[i] = note;
            pans[c] = panOfPpz8(pan);
        }
    }

    private final boolean[] ppz8KeyOns = new boolean[8];

    /** PPZ8 pans over 0..9, 5 being the centre. 0 is a silent channel, the chip skips it */
    private static Pan panOfPpz8(int pan) {
        return switch (pan) {
            case 0 -> Pan.NONE;
            case 1, 2 -> Pan.LEFT;
            case 3, 4 -> Pan.MID_LEFT;
            case 5 -> Pan.CENTER;
            case 6, 7 -> Pan.MID_RIGHT;
            default -> Pan.RIGHT;
        };
    }

    /**
     * Drives the level meter of {@code t}'s channel: it jumps to the part volume on key on, sags
     * towards the sustain level while the key is held, and releases once FMP keys off or the part
     * rests. Note that this is an envelope, not a measurement - see the class comment.
     */
    private void level(TrackId t, FmpWork fw, int part, boolean keyOn, boolean sounding) {
        int c = levelChannelOf(t);
        if (c < 0) return;

        double volume = fw.playing(part) ? volumeOf(t, fw.volume(part)) : 0;
        if (keyOn) {
            envelopes[c] = volume;
        } else if (sounding) {
            envelopes[c] = Math.max(envelopes[c] * levelSustainDecay, volume * levelSustain);
        } else {
            envelopes[c] *= levelRelease;
        }
        pans[c] = panOf(t, fw, part);
    }

    /**
     * Amplitude 0..1 of a part at its current volume. The meter is read back as dBFS, so the
     * volumes have to be un-logged first: an FM part carries a total level, one step of 0.75 dB
     * below full scale, and an SSG part carries one 3 dB step of the chip's 16 level table. FMP
     * drives the ADPCM part's level register straight from its volume, so that one is linear.
     */
    private static double volumeOf(TrackId t, int v) {
        double db = switch (t) {
            case SSG_1, SSG_2, SSG_3 -> (Math.min(v, 15) - 15) * 3.0;
            case ADPCM -> 20 * Math.log10(Math.max(v, 1) / 255.0);
            default -> -Math.min(v, 127) * 0.75; // FM
        };
        return Math.pow(10, db / 20);
    }

    /** only the FM and the ADPCM parts carry a pan, the OPNA register 0xb4 bits */
    private static Pan panOf(TrackId t, FmpWork fw, int part) {
        switch (t) {
        case SSG_1, SSG_2, SSG_3:
            return Pan.CENTER;
        default:
            int pan = t == TrackId.ADPCM ? fw.adpcmPan : fw.pan(part);
            boolean left = (pan & 0x80) != 0;
            boolean right = (pan & 0x40) != 0;
            if (left && right) return Pan.CENTER;
            if (left) return Pan.LEFT;
            if (right) return Pan.RIGHT;
            return Pan.NONE;
        }
    }

    /** MML key: high nibble octave, low nibble note, 0xff while the part rests */
    private static int keyOf(int note) {
        return note < 0 ? 0xff : (note / 12) << 4 | note % 12;
    }

    /**
     * The key the chip is really playing, read back from the pitch register FMP last wrote, which
     * has the LFO and the portamento in it. fmdsp draws it on the keyboard in its own color, so a
     * note bent away from its MML key lights a second key - which is the whole point of the
     * keyboard having two of them, and what makes a portamento visible as it slides.
     * <p>
     * The ADPCM part has none: its register is a playback rate, and FMP drives it from the sample's
     * own pitch rather than from the note, so the note it lands nearest says nothing. The C fmdsp
     * blanks it too.
     */
    private static int actualKeyOf(TrackId t, FmpWork fw, int part, int key) {
        if (t == TrackId.ADPCM) return 0xff;
        if (key == 0xff) return 0xff;
        int freq = fw.outFnum(part);
        // FMP blanks the register cache when an LFO is switched off and fills it again on the next
        // frame; the note has not moved, so show it where it is rather than at the bottom key
        if (freq == 0) return key;
        return switch (t) {
            case SSG_1, SSG_2, SSG_3 -> Notes.ssgKeyOf(freq);
            default -> Notes.fmKeyOf(freq);
        };
    }

    /**
     * The eight character mnemonic fmdsp shows behind "M:": FMP's five software LFOs by their MML
     * letters, a slot fmdsp leaves blank, the OPNA's own hardware LFO, and the portamento. Same
     * letters and same order as the C fmdsp.
     * <p>
     * The first six come out of one byte, so they are tabulated - the snapshot runs at ~120 Hz over
     * sixteen tracks and the string would otherwise be built from scratch every time.
     */
    private static String statusOf(FmpWork fw, int part) {
        boolean hardware = (fw.type(part) & 0x01) != 0 && fw.hlfoApms(part) != 0;
        boolean portamento = (fw.statusBits(part) & 0x20) != 0;
        return LFO_STATUS[fw.lfoFlags(part)] + (hardware ? "H" : "-") + (portamento ? "P" : "-");
    }

    private static final String[] LFO_STATUS = new String[256];

    static {
        for (int i = 0; i < LFO_STATUS.length; i++) {
            LFO_STATUS[i] = new String(new char[] {
                    (i & 0x80) != 0 ? 'P' : '-',  // pitch LFO
                    (i & 0x40) != 0 ? 'Q' : '-',  // the second pitch LFO
                    (i & 0x20) != 0 ? 'R' : '-',  // the third pitch LFO
                    (i & 0x10) != 0 ? 'A' : '-',  // volume LFO
                    '-',
                    (i & 0x04) != 0 ? 'e' : '-',  // envelope, which replaces the volume LFO
            });
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
            default -> -1; // PPZ8, whose meters come from the chip and not from a part
        };
    }

    private static TrackInfo infoOf(TrackId t) {
        return switch (t) {
            case SSG_1, SSG_2, SSG_3 -> TrackInfo.SSG;
            default -> TrackInfo.NORMAL;
        };
    }

    /** FMP keeps its parts in chip order: FM 1-6, ADPCM, the FM3 extended parts, then SSG 1-3 */
    private void mapParts() {
        Arrays.fill(parts, -1);
        for (int i = 0; i < 3; i++) {
            parts[TrackId.FM_1.ordinal() + i] = FmpWork.FM_1 + i;
            parts[TrackId.FM_4.ordinal() + i] = FmpWork.FM_1 + 3 + i;
            parts[TrackId.FM_3_EX_1.ordinal() + i] = FmpWork.FM3_EX_1 + i;
            parts[TrackId.SSG_1.ordinal() + i] = FmpWork.SSG_1 + i;
        }
        parts[TrackId.ADPCM.ordinal()] = FmpWork.ADPCM;
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

    /** FMP counts its own loops, the loop bar only needs to know how long one of them took */
    private void loop(FmpWork fw) {
        int loop = fw.loopCount();
        if (loop != lastLoopCount) {
            if (fw.ticks > loopStartTicks) {
                loopTimerBCount = fw.ticks - loopStartTicks;
            }
            loopStartTicks = fw.ticks;
            lastLoopCount = loop;
        }
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

    /** FMP masks a part by stopping it, which the row already shows */
    @Override
    public boolean masked(TrackId track) {
        return false;
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
     * the driver's sample counter advances in ~10 ms bursts of several hundred samples - reading it
     * straight makes the clock stutter. Playback is real time, so the displayed clock runs off the
     * wall clock instead and is slewed towards the driver's counter, which keeps it smooth without
     * letting it drift. It is frozen while paused and never runs backwards.
     */
    @Override
    public long generatedFrames() {
        if (work == null) return 0;

        long counter = frames;
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

    @Override public long timerBCount() { FmpWork fw = work; return fw != null ? fw.ticks : 0; }

    @Override public int timerB() { FmpWork fw = work; return fw != null ? fw.timerB : 0; }

    @Override public int loopCount() { FmpWork fw = work; return fw != null ? fw.loopCount() : 0; }

    @Override public long loopTimerBCount() { return loopTimerBCount; }

    @Override public long timerBCountLoop() { FmpWork fw = work; return fw != null ? fw.ticks - loopStartTicks : 0; }

    @Override public boolean playing() { return work != null && !stopped; }

    @Override public boolean paused() { return paused; }

    @Override public String driverName() { return "FMP"; }

    @Override public String chips() { FmpWork fw = work; return (fw != null && fw.ppz8 != null && !fw.ppz8.isEmpty()) ? "YM2608 PPZ8" : "YM2608"; }

    @Override public String filename() { return filename; }

    @Override public String comment(int line) { return comments[line]; }

    @Override
    public String pcmType(int index) {
        return switch (index) {
            case 0 -> "PVI";
            case 1 -> "PPZ";
            default -> null;
        };
    }

    @Override
    public String pcmFilename(int index) {
        FmpWork fw = work;
        if (fw == null) return null;
        return switch (index) {
            case 0 -> fw.pviName;
            case 1 -> fw.ppzName;
            default -> null;
        };
    }

    @Override
    public boolean pcmError(int index) {
        FmpWork fw = work;
        if (fw == null) return false;
        return switch (index) {
            case 0 -> fw.pviError;
            case 1 -> fw.ppzError;
            default -> false;
        };
    }

    private static void addLines(List<String> list, String text) {
        if (text != null && !text.isEmpty()) {
            String[] split = text.split("\\r?\\n|\\r");
            for (String s : split) {
                if (!s.isEmpty()) {
                    list.add(s);
                }
            }
        }
    }
}
