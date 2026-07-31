/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.fmp;

import java.util.Map;

import mdplayer.emu.nise98.Memory98;


/**
 * FMP's work area, seen from the outside.
 * <p>
 * FMP is the original PC-98 driver running inside the {@link mdplayer.emu.nise98} emulator, so
 * there is no driver object to ask: the playing state lives in the emulated memory, in the resident
 * segment {@code 0x2000}. FMP publishes the address of its work area through function call
 * {@code 0xd2 / AX=0x1104} ({@code FMP#workPtr}); the part works follow it as a plain array of
 * {@value #PART_SIZE} byte entries.
 * <p>
 * The layout is not documented anywhere - the offsets below were recovered by running the driver
 * and matching what moves in memory against what it writes to the OPNA (a byte that follows the
 * f-number of one FM channel and no other is that channel's note, and so on). They agree with the
 * offsets 98fmplayer notes on its own {@code struct fmp_part}, which is laid out to match FMP's
 * work area: this file's fields sit nine bytes below the ones it lists, {@link #NOTE} being its
 * {@code prev_note} at 0x09.
 * <p>
 * A few things FMP never puts in memory - the timer B period, the rhythm keys, the SSG mixer - are
 * echoed here from the chip writes instead, see {code FmpDriver#opnaWrite}.
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

    /**
     * which of the part's software LFOs are switched on: 0x80 p, 0x40 q, 0x20 r, 0x10 a, 0x08 w,
     * 0x04 e. 0x02 and 0x01 are FMP's own, an LFO retrigger and the channel mask.
     */
    private static final int LFO_FLAGS = -9;
    private static final int VOLUME = -7;
    /** the note's remaining tick count at which the part keys off, see {@link #gate} */
    private static final int GATE_CMP = -6;
    private static final int TICKS_LEFT = -5;
    private static final int NOTE = 0;
    /** 0x08 the part has stopped, 0x20 a pitchbend (portamento) is running */
    private static final int STATUS = 1;
    /** signed */
    private static final int DETUNE = 3;
    /** the f-number of the note being played, before the detune and, on SSG, before the octave */
    private static final int FNUM = 49;
    /** the value last written to the part's pitch register, i.e. what the chip really plays */
    private static final int OUT_FNUM = 51;
    private static final int TONE = 54;
    /** nonzero while the key is down, which is until the gate time runs out */
    private static final int KEY_ON = 55;
    /** the OPNA register 0xb4 bits */
    private static final int PAN = 57;
    /** the part's data pointer, 0 while the part holds no data */
    private static final int DATA = 61;
    /** the part's bit in the mask word: FM 1-6, SSG 1-3, rhythm, ADPCM, FM3 extended 1-3 */
    private static final int BIT = 67;
    /** 0x01 FM, 0x02 FM3 extended, 0x04 SSG, 0x08 rhythm, 0x10 ADPCM */
    private static final int TYPE = 65;
    /** FM only: the OPNA register 0xb4 AMS/PMS the hardware LFO drives, 0 while it is off */
    private static final int HLFO_APMS = 96;
    /** FM only: slot mask 0b1234???? (0x6a relative to work start, 47 relative to note) */
    private static final int SLOT_MASK = 47;

    // fields of the work area itself

    /** how often the song has looped */
    private static final int LOOP_COUNT = 0x17;

    /** FMP's note value for "no note" */
    private static final int REST = 0x61;

    /** the emulated memory, and the work address in it. 0 until FMP has run its first frame */
    private volatile Memory98 mem;

    private volatile int work;

    /** timer B period, echoed from the chip writes */
    public volatile int timerB;

    /** the rhythm keys struck in the last frame, echoed from the chip writes */
    public volatile int rhythmKeyOn;

    /** the OPNA register 0x07 bits, echoed from the chip writes */
    public volatile int ssgMixer = 0xff;

    /** the OPNA register 0x06 bits, echoed from the chip writes */
    public volatile int ssgNoiseFreq;

    /** the ADPCM pan, echoed from the chip writes */
    public volatile int adpcmPan;

    /** timer B interrupts since playback started, which is what FMP counts time in */
    public volatile long ticks;

    /**
     * PPZ8's channels, from {@link mdplayer.chips.Ppz8Chip#getInfo}. FMP has no PPZ8 part of its own - a PDZF song
     * plays PPZ8 from its FM3 extended and ADPCM parts - so the only place the eight PPZ8 channels
     * exist as such is the chip.
     */
    public volatile Map<String, Object> ppz8;

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

    /**
     * How many of a note's {@code ticks} the key is held for, which is what fmdsp shows behind
     * "GT:". FMP does not keep it: it counts the note down and keys off once the count reaches
     * {@link #GATE_CMP}, so the gate is the difference between the two - hence the note's total
     * length as an argument, which only the caller has (see {@code FmpFmDspSource#ticks}).
     */
    public int gate(int part, int ticks) {
        return Math.max(0, ticks - peek(part, GATE_CMP));
    }

    public int ticksLeft(int part) {
        return peek(part, TICKS_LEFT);
    }

    /** which software LFOs are on, and whether a portamento is running - see {@link #LFO_FLAGS} */
    public int lfoFlags(int part) {
        return peek(part, LFO_FLAGS);
    }

    /** see {@link #STATUS} */
    public int statusBits(int part) {
        return peek(part, STATUS);
    }

    /** see {@link #TYPE} */
    public int type(int part) {
        return peek(part, TYPE);
    }

    /** nonzero while the part's hardware LFO is on, FM parts only */
    public int hlfoApms(int part) {
        return peek(part, HLFO_APMS);
    }

    public int slotMask(int part) {
        return peek(part, SLOT_MASK);
    }

    public int toneNum(int part) {
        return peek(part, TONE);
    }

    public int detune(int part) {
        return (short) peekWord(part, DETUNE);
    }

    /** the f-number of the note being played, see {@link #FNUM} */
    public int fnum(int part) {
        return peekWord(part, FNUM);
    }

    /** the f-number the chip is playing, LFO, portamento and detune included */
    public int outFnum(int part) {
        return peekWord(part, OUT_FNUM);
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
