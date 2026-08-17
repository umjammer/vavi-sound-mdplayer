/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.fmp7;

/**
 * What FMP7 says it is playing: one reading of its public work.
 * <p>
 * FMP7 has no dll to link against and is not going to tell anyone what its player does inside.
 * What it does publish is this - a shared memory it keeps up to date as it plays, laid out by
 * {@code fmp32_sharework.h} and described at <a href="http://fmpdoc.fmp.jp/fmp7-api/">the api
 * page</a> - which is a good deal better than a register trace: these are the notes as the
 * driver understands them, with the part they belong to and the sound source they went to.
 * <p>
 * The sizes are asked for rather than assumed ({@code GetWorkSize}, api 0x0101), because they
 * have grown: the published struct is 40 bytes a part and 7.10g writes 48. Nothing here reads
 * past the documented fields, so a later build only adds room this does not use - but a build
 * whose {@link #PART_SIZE} differs would put every part but the first at the wrong offset, which
 * is why {@link Fmp7Player} asks and passes the answer in.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 * @see <a href="https://github.com/aosoft/FMP7ApiCLR">FMP7ApiCLR</a>, the same work in C#
 */
public class Fmp7Work {

    /** the name FMP7 publishes it under */
    public static final String KEY_MAP = "FMP7_PUBLIC_WORK";

    /** parts the work always has room for, whatever the song uses */
    public static final int MAX_PART = 128;

    /** the global work of 7.10g; {@code GetWorkSize} is what really says */
    public static final int GLOBAL_SIZE = 576;

    /** one part's work in 7.10g - the published struct is 40 */
    public static final int PART_SIZE = 48;

    // global work, "status"
    public static final int PLAYING = 0x0000_0001;
    public static final int PAUSED = 0x0000_0002;
    public static final int FADING = 0x0000_0010;
    public static final int LOOPED = 0x0001_0000;
    public static final int MASKED = 0x0002_0000;

    // part work, "mode": which of the three kinds of part this is
    public static final int MODE_NONE = 0;
    public static final int MODE_FM = 1;
    public static final int MODE_SSG = 2;
    public static final int MODE_PCM = 3;

    // part work, "device": which sound source it actually goes to
    public static final int DEVICE_OPNA = 0x01;
    public static final int DEVICE_OPM = 0x02;
    public static final int DEVICE_SSG = 0x10;
    public static final int DEVICE_PCM = 0x20;
    public static final int DEVICE_ADPCM = 0x21;
    public static final int DEVICE_RHYTHM = 0x30;

    // part work, "state"
    public static final int PART_PLAY = 0x0001;
    public static final int PART_KEYON = 0x0004;
    public static final int PART_TIE = 0x0008;
    public static final int PART_SOFT_ENV = 0x0010;
    public static final int PART_HARD_ENV = 0x0020;
    public static final int PART_TONE = 0x0040;
    public static final int PART_NOISE = 0x0080;
    public static final int PART_LFO0 = 0x0100;
    public static final int PART_PITCH_BEND = 0x1000;
    public static final int PART_TONE_BEND = 0x2000;

    /** what a part's note reads while it is resting */
    public static final int REST = 255;

    /** a semitone, in the units the part's frequency is counted in */
    public static final int SEMITONE = 64;

    private final int globalSize;
    private final int partSize;

    /** the bytes as they were read, which every accessor here reads out of */
    private final byte[] b;

    /** where in the audio this reading belongs, in frames of the stream it was taken against */
    public long atFrame;

    public Fmp7Work(int globalSize, int partSize) {
        this.globalSize = globalSize;
        this.partSize = partSize;
        this.b = new byte[globalSize + partSize * MAX_PART];
    }

    /** the buffer to read the shared memory into; {@link #size} bytes of it */
    public byte[] buffer() {
        return b;
    }

    public int size() {
        return b.length;
    }

    public void copyFrom(Fmp7Work other) {
        System.arraycopy(other.b, 0, b, 0, Math.min(b.length, other.b.length));
        atFrame = other.atFrame;
    }

    // global

    public int status() {
        return le32(0);
    }

    public boolean playing() {
        return (status() & PLAYING) != 0;
    }

    /** how long it has been playing [ms]; the work counts it in hundredths of a second */
    public double playMillis() {
        return le32(4) * 10.0;
    }

    /** how hard the song is working the chip, averaged and right now - FMP7 calls it calories */
    public int averageCalorie() {
        return le32(8);
    }

    public int instantCalorie() {
        return le32(12);
    }

    /** the whole song's length, in clocks */
    public int count() {
        return le32(16);
    }

    /** where the song has got to, in clocks */
    public int countNow() {
        return le32(20);
    }

    public int tempo() {
        return le32(24);
    }

    /** how many times it has been round */
    public int loop() {
        return le32(28);
    }

    /** clocks to a whole note, which is what {@link #count} is counted in */
    public int clock() {
        return le32(420);
    }

    /** goes up by one every time playing starts, which is how a replay is told from a stall */
    public int startCounter() {
        return le32(424);
    }

    public int levelL() {
        return (short) le16(416);
    }

    public int levelR() {
        return (short) le16(418);
    }

    /** {@link #MODE_FM}, {@link #MODE_SSG}, {@link #MODE_PCM}, or none for a part not in use */
    public int mode(int part) {
        return b[32 + part] & 0xff;
    }

    /** the part's number within its own kind, which is what the driver calls it (1-32) */
    public int partNo(int part) {
        return b[160 + part] & 0xff;
    }

    public boolean masked(int part) {
        return b[288 + part] != 0;
    }

    /** which sound source the part goes to - {@link #DEVICE_OPNA} and friends */
    public int device(int part) {
        return b[448 + part] & 0xff;
    }

    // part

    private int part(int p) {
        return globalSize + partSize * p;
    }

    public int state(int p) {
        return le32(part(p));
    }

    /** the pitch it is sounding at, {@link #SEMITONE} per semitone and one semitone high */
    public int freq(int p) {
        return (short) le16(part(p) + 8);
    }

    public int detune(int p) {
        return (short) le16(part(p) + 10);
    }

    /** the SSG hardware envelope's period, 0 on anything but SSG */
    public int hardEnvFreq(int p) {
        return le16(part(p) + 12);
    }

    /** the SSG software envelope in use, 0 on FM */
    public int envNo(int p) {
        return le16(part(p) + 14);
    }

    /** how long the note has been sounding, in clocks */
    public int noteCount(int p) {
        return le16(part(p) + 16);
    }

    /** goes up by one on every key-on, which is the only way to see one that lands on the same note */
    public int keyOn(int p) {
        return b[part(p) + 20] & 0xff;
    }

    /** o1c=0 to o9b=107, or {@link #REST} */
    public int note(int p) {
        return b[part(p) + 21] & 0xff;
    }

    public int tone(int p) {
        return b[part(p) + 22] & 0xff;
    }

    /** 0-127 */
    public int volume(int p) {
        return b[part(p) + 23] & 0xff;
    }

    /** 1 hard right, 128 centre, 255 hard left - the other way round from a MIDI pan */
    public int pan(int p) {
        return b[part(p) + 24] & 0xff;
    }

    /** the SSG noise period, 0 on anything but SSG */
    public int noise(int p) {
        return b[part(p) + 25] & 0xff;
    }

    public int keyTranspose(int p) {
        return b[part(p) + 26];
    }

    /** one of the four LFOs: the kind in the top bits, the waveform in the bottom four */
    public int lfo(int p, int n) {
        return le16(part(p) + 32 + n * 2);
    }

    /** is any of the part's four LFOs bending its pitch, i.e. a vibrato */
    public boolean vibrato(int p) {
        for (int n = 0; n < 4; n++) {
            if ((state(p) & (PART_LFO0 << n)) != 0 && (lfo(p, n) & 0x0010) != 0) {
                return true;
            }
        }
        return false;
    }

    /** the same for a tremolo, which is an LFO on the level rather than the pitch */
    public boolean tremolo(int p) {
        for (int n = 0; n < 4; n++) {
            if ((state(p) & (PART_LFO0 << n)) != 0 && (lfo(p, n) & 0x0020) != 0) {
                return true;
            }
        }
        return false;
    }

    private int le16(int o) {
        return (b[o] & 0xff) | (b[o + 1] & 0xff) << 8;
    }

    private int le32(int o) {
        return (b[o] & 0xff) | (b[o + 1] & 0xff) << 8 | (b[o + 2] & 0xff) << 16 | (b[o + 3] & 0xff) << 24;
    }
}
