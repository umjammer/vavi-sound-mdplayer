/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import mdplayer.ChipRegister;
import mdplayer.chips.BaseChip;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * The OPL family's shared shape: nine two operator channels.
 * <p>
 * The chips are read through their own channel view rather than a copy of the register writes,
 * because the family is emulated several ways and they do not agree on what they keep - some hold
 * the register file, others decode it into operators and throw it away. Every one of them can say
 * what its channels are doing, so that is what this asks for.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public abstract class OplReader implements FmDspChipReader {

    /** the OPL tick [Hz], clock / 72 - every family member's standard clock lands here */
    private static final double tick = 3579545.0 / 72;

    protected ChipRegister chipRegister;

    private final boolean[] prevOns = new boolean[9];
    private final int[] prevFnums = new int[9];
    private boolean active;

    /** the chip this reads */
    protected abstract BaseChip chip();

    /** the chip's channel state, read back once a frame */
    protected Map<String, Object> info;

    protected abstract boolean chipMask(int ch);

    protected Pan pan(int ch) {
        return Pan.CENTER;
    }

    @Override
    public String chipName() {
        return "OPL";
    }

    @Override
    public void bind(ChipRegister chipRegister) {
        this.chipRegister = chipRegister;
    }

    @Override
    public void reset() {
        Arrays.fill(prevOns, false);
        Arrays.fill(prevFnums, 0);
        active = false;
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.FM);
    }

    @Override
    public boolean ready() {
        return chipRegister != null && chip() != null;
    }

    @Override
    public void poll() {
        try {
            info = chip().getInfo(0);
        } catch (RuntimeException ignore) {
            // the chip exists but the song never loaded it
            info = null;
        }
    }

    protected int intOf(int ch, String field) {
        Object value = info.get("channels." + ch + "." + field);
        return value instanceof Integer i ? i : 0;
    }

    protected boolean boolOf(int ch, String field) {
        Object value = info == null ? null : info.get("channels." + ch + "." + field);
        return value instanceof Boolean b && b;
    }

    @Override
    public boolean active(Group group) {
        if (!active) {
            for (int ch = 0; ch < 9 && !active; ch++) active = boolOf(ch, "keyOn");
        }
        return active;
    }

    @Override
    public int channels(Group group) {
        return 9;
    }

    @Override
    public void read(Group group, int ch, FmDspChannel out) {
        out.name = "FM";
        out.num = ch + 1;
        if (info == null) return;

        boolean on = boolOf(ch, "keyOn");
        int fnum = intOf(ch, "fnum");
        int block = intOf(ch, "block");
        out.sounding = on;
        out.keyOn = on && (!prevOns[ch] || fnum != prevFnums[ch]);
        prevOns[ch] = on;
        prevFnums[ch] = fnum;

        out.note = fnum > 0 ? Notes.noteOf(fnum * Math.pow(2, block - 1) * tick / (1 << 19)) : -1;
        // the carrier's total level, 0.75 dB per step over 0..63
        int tl = intOf(ch, "totalLevel");
        out.volume = 63 - tl;
        out.amplitude = Math.pow(10, -tl * 0.75 / 20);
        out.pan = pan(ch);
    }

    @Override
    public boolean masked(Group group, int ch) {
        return chipMask(ch);
    }
}
