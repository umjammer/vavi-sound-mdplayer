/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp;

import java.util.Map;

import mdplayer.chips.Ppz8Chip;
import mdplayer.emu.nise98.Memory98;


/**
 * FMP's work area, seen from the outside.
 * <p>
 * FMP is the original PC-98 driver running inside the {@link mdplayer.emu.nise98} emulator, so
 * there is no driver object to ask: the playing state lives in the emulated memory, in the resident
 * segment {@code 0x2000}. FMP publishes the address of its work area through function call
 * {@code 0xd2 / AX=0x1104} ({@link FMP#workPtr}); the part works follow it as a plain array of
 * {@value #PART_SIZE} byte entries.
 * <p>
 * The layout is not documented anywhere - the offsets below were recovered by running the driver
 * and matching what moves in memory against what it writes to the OPNA (a byte that follows the
 * f-number of one FM channel and no other is that channel's note, and so on).
 * <p>
 * A few things FMP never puts in memory - the timer B period, the rhythm keys, the SSG mixer - are
 * echoed here from the chip writes instead, see {@link FmpDriver#opnaWrite}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-14 nsano initial version <br>
 */
public class FmpWork {

    /** size of one part work */
    public static final int PART_SIZE = 0x6f;

    /** the note byte of part 0, relative to the work address */
    private static final int PART0 = 0x3b;

    // part indices. FMP keeps one work per part, in chip order, not in MML letter order

    public static final int FM_1 = 0;
    public static final int ADPCM = 6;
    /** the FM3 extended parts (MML X, Y, Z), which also drive PPZ8 in a PDZF song */
    public static final int FM3_EX_1 = 7;
    public static final int SSG_1 = 19;

    // fields of a part work, relative to its note byte

    private static final int GATE = -8;
    private static final int VOLUME = -7;
    private static final int TICKS_LEFT = -5;
    private static final int NOTE = 0;
    /** signed */
    private static final int DETUNE = 3;
    /** the f-number actually sent to the chip, i.e. after LFO and portamento */
    private static final int FNUM = 49;
    private static final int TONE = 54;
    /** nonzero while the key is down, which is until the gate time runs out */
    private static final int KEY_ON = 55;
    /** the OPNA register 0xb4 bits */
    private static final int PAN = 57;
    /** the part's data pointer, 0 while the part holds no data */
    private static final int DATA = 61;
    /** the part's bit in the mask word: FM 1-6, SSG 1-3, rhythm, ADPCM, FM3 extended 1-3 */
    private static final int BIT = 67;

    // fields of the work area itself

    /** how often the song has looped */
    private static final int LOOP_COUNT = 0x17;

    /** FMP's note value for "no note" */
    private static final int REST = 0x61;

    /** the emulated memory, and the work address in it. 0 until FMP has run its first frame */
    private volatile Memory98 mem;

    private volatile int work;

    /** timer B period, echoed from the chip writes */
    volatile int timerB;

    /** the rhythm keys struck in the last frame, echoed from the chip writes */
    volatile int rhythmKeyOn;

    /** the OPNA register 0x07 bits, echoed from the chip writes */
    volatile int ssgMixer = 0xff;

    /** the ADPCM pan, echoed from the chip writes */
    volatile int adpcmPan;

    /** timer B interrupts since playback started, which is what FMP counts time in */
    volatile long ticks;

    /**
     * PPZ8's channels, from {@link Ppz8Chip#getInfo}. FMP has no PPZ8 part of its own - a PDZF song
     * plays PPZ8 from its FM3 extended and ADPCM parts - so the only place the eight PPZ8 channels
     * exist as such is the chip.
     */
    volatile Map<String, Object> ppz8;

    void setWork(Memory98 mem, int work) {
        this.mem = mem;
        this.work = work;
    }

    /** false until FMP has published its work address */
    public boolean valid() {
        return mem != null && work != 0;
    }

    private int peek(int part, int field) {
        return mem.peekB(work + PART0 + PART_SIZE * part + field) & 0xff;
    }

    private int peekWord(int part, int field) {
        return mem.peekW(work + PART0 + PART_SIZE * part + field) & 0xffff;
    }

    /** true while the part has song data left to play, i.e. while it has a row to show */
    public boolean playing(int part) {
        return valid() && ticksLeft(part) != 0 && peekWord(part, BIT) != 0;
    }

    /** true while the part's key is down. FMP releases it at the end of the gate time */
    public boolean keyOn(int part) {
        return peek(part, KEY_ON) != 0;
    }

    /** octave * 12 + note, or -1 while the part rests */
    public int note(int part) {
        int note = peek(part, NOTE);
        return note == REST || note > 0x60 ? -1 : note;
    }

    /**
     * FM parts carry a total level, so a larger value is a quieter part; SSG parts carry the chip's
     * own 0..15 volume, so a larger value is a louder part.
     */
    public int volume(int part) {
        return peek(part, VOLUME);
    }

    public int gate(int part) {
        return peek(part, GATE);
    }

    public int ticksLeft(int part) {
        return peek(part, TICKS_LEFT);
    }

    public int toneNum(int part) {
        return peek(part, TONE);
    }

    public int detune(int part) {
        return (short) peekWord(part, DETUNE);
    }

    /** the f-number the chip is playing, LFO and portamento included */
    public int fnum(int part) {
        return peekWord(part, FNUM);
    }

    /** the OPNA register 0xb4 bits of an FM part */
    public int pan(int part) {
        return peek(part, PAN);
    }

    /** where the part is reading its song data, 0 while it holds none */
    public int dataPtr(int part) {
        return peekWord(part, DATA);
    }

    /** how often the song has looped */
    public int loopCount() {
        return valid() ? mem.peekB(work + LOOP_COUNT) & 0xff : 0;
    }
}
