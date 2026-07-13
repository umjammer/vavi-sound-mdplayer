/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.pmd;

import java.util.Arrays;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import musicDriverInterface.MetaData.Tag;
import pmd.driver.PW;
import pmd.driver.PW.partWork;
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
public class PmdFmDspSource implements FmDspDataSource, FftDataSource, LevelDataSource, TrackStatusSource, WorkStateSource {

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

    public PmdFmDspSource() {
        Arrays.setAll(tracks, i -> new TrackStatus());
        Arrays.fill(pans, Pan.CENTER);
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
            if (!commented && event.getSource() instanceof BaseDriver driver) {
                commented = true;
                comments[0] = driver.metaData.getFirst(Tag.Title);
                comments[1] = driver.metaData.getFirst(Tag.Composer);
                comments[2] = driver.metaData.getFirst(Tag.Arranger);
            }
            snapshot((PW) event.getArguments()[0]);
        }
        case "master" -> {
            short[] buffer = (short[]) event.getArguments()[0];
            int offset = (int) event.getArguments()[1];
            for (int i = offset; i + 1 < buffer.length; i += 2) {
                push((buffer[i] + buffer[i + 1]) / 2f);
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
            status.info = infoOf(t);
            status.key = part.onkai & 0xff;
            status.actualKey = status.key;
            status.toneNum = part.voicenum & 0xff;
            status.volume = part.volume & 0xff;
            status.gate = part.qdat & 0xff;
            status.detune = part.detune;
            status.ticksLeft = part.leng & 0xff;
            status.ppz8Ch = status.info == TrackInfo.PPZ8 ? i - TrackId.PPZ8_1.ordinal() + 1 : 0;
            if (status.info == TrackInfo.SSG) {
                // the mixer bits PMD lets through for this SSG channel: tone in 0-2, noise in 3-5
                int ssg = p - pw.part7;
                status.ssgTone = (part.psgpat & (1 << ssg)) != 0;
                status.ssgNoise = (part.psgpat & (8 << ssg)) != 0;
            } else {
                status.ssgTone = false;
                status.ssgNoise = false;
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
            case FM_3_EX_1, FM_3_EX_2, FM_3_EX_3 -> TrackInfo.FM3EX;
            case SSG_1, SSG_2, SSG_3 -> TrackInfo.SSG;
            case PPZ8_1, PPZ8_2, PPZ8_3, PPZ8_4, PPZ8_5, PPZ8_6, PPZ8_7, PPZ8_8 -> TrackInfo.PPZ8;
            default -> TrackInfo.NORMAL;
        };
    }

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
        status.ppz8Ch = 0;
        status.ssgTone = false;
        status.ssgNoise = false;
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

        long counter = pw.timeCounter;
        timerBStep += (counter - lastTimeCounter)
                * (PmdDriver.baseClock / 72.0 / 2.0 / Common.VGMProcSampleRate);
        lastTimeCounter = counter;
        while (timerBStep >= overflow) {
            timerBStep -= overflow;
            timerBCount++;
        }

        int loop = Math.max(pw.getnowLoopCounter(), 0);
        if (loop != lastLoopCount) {
            if (lastLoopCount > 0) loopTimerBCount = timerBCount - loopStartTimerBCount;
            loopStartTimerBCount = timerBCount;
            lastLoopCount = loop;
        }
        timerBCountLoop = timerBCount - loopStartTimerBCount;

        work = pw;
    }

    // ----- FmDspDataSource -----

    @Override public FftDataSource fft() { return this; }

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

    @Override public String filename() { return filename; }

    @Override public String comment(int line) { return comments[line]; }

    // ----- FftDataSource -----

    /** power of two, ~93 ms at 44100 Hz. Long enough to resolve the low end of the printed axis. */
    private static final int fftLength = 4096;

    /**
     * The renderer prints its frequency axis 48 px per octave and draws the 70 bars 4 px apart, so
     * a bar spans an octave / 12 and 250 Hz sits on bar 10.5. Anything else and the bars don't
     * stand where the labels say they do.
     */
    private static final double fftBarsPerOctave = 12;

    /** frequency of bar 0, so that bar 10.5 is 250 Hz */
    private static final double fftLow = 250 / Math.pow(2, 10.5 / fftBarsPerOctave);

    /** the axis is labelled 0 dB at the top and -48 dB at the bottom, one bar step is 1.5 dB */
    private static final double fftFloor = -48;

    private final float[] pcm = new float[fftLength];

    private int pcmPosition;

    private final double[] window = new double[fftLength];

    private final double[] re = new double[fftLength];
    private final double[] im = new double[fftLength];

    /** magnitude of each fft bin, normalized so a full scale sine reads 1.0 */
    private final double[] magnitude = new double[fftLength / 2];

    {
        for (int i = 0; i < fftLength; i++) {
            window[i] = 0.5 - 0.5 * Math.cos(2 * Math.PI * i / (fftLength - 1));
        }
    }

    private void push(float sample) {
        pcm[pcmPosition] = sample;
        pcmPosition = (pcmPosition + 1) % fftLength;
    }

    @Override
    public void readFft(int[] out) {
        int position = pcmPosition;
        for (int i = 0; i < fftLength; i++) {
            re[i] = pcm[(position + i) % fftLength] / 32768.0 * window[i];
            im[i] = 0;
        }
        fft(re, im);

        // the hann window halves the amplitude and a real signal splits it over two bins
        for (int b = 0; b < magnitude.length; b++) {
            magnitude[b] = Math.sqrt(re[b] * re[b] + im[b] * im[b]) * 4 / fftLength;
        }

        double binWidth = (double) sampleRate() / fftLength;
        for (int i = 0; i < FftDataSource.LENGTH; i++) {
            double from = fftLow * Math.pow(2, (i - 0.5) / fftBarsPerOctave) / binWidth;
            double to = fftLow * Math.pow(2, (i + 0.5) / fftBarsPerOctave) / binWidth;
            double peak = 0;
            int first = (int) Math.floor(from);
            int last = (int) Math.ceil(to);
            for (int b = Math.max(1, first); b <= Math.min(last, magnitude.length - 1); b++) {
                peak = Math.max(peak, magnitude[b]);
            }
            double db = 20 * Math.log10(Math.max(peak, 1e-9));
            out[i] = (int) Math.clamp(
                    Math.round((db - fftFloor) / -fftFloor * (FftDataSource.MAX + 1)), 0, FftDataSource.MAX);
        }
    }

    /** in place radix-2 FFT */
    private static void fft(double[] re, double[] im) {
        int n = re.length;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) {
                j ^= bit;
            }
            j ^= bit;
            if (i < j) {
                double t = re[i]; re[i] = re[j]; re[j] = t;
                t = im[i]; im[i] = im[j]; im[j] = t;
            }
        }
        for (int len = 2; len <= n; len <<= 1) {
            double angle = -2 * Math.PI / len;
            double wr = Math.cos(angle);
            double wi = Math.sin(angle);
            for (int i = 0; i < n; i += len) {
                double cr = 1, ci = 0;
                for (int j = 0; j < len / 2; j++) {
                    double ur = re[i + j], ui = im[i + j];
                    double vr = re[i + j + len / 2] * cr - im[i + j + len / 2] * ci;
                    double vi = re[i + j + len / 2] * ci + im[i + j + len / 2] * cr;
                    re[i + j] = ur + vr;
                    im[i + j] = ui + vi;
                    re[i + j + len / 2] = ur - vr;
                    im[i + j + len / 2] = ui - vi;
                    double nr = cr * wr - ci * wi;
                    ci = cr * wi + ci * wr;
                    cr = nr;
                }
            }
        }
    }
}
