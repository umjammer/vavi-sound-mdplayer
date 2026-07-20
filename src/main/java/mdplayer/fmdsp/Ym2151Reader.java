/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import mdplayer.ChipRegister;
import mdplayer.chips.Ym2151Chip;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * OPM: eight FM channels, the note straight from the key code register.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public class Ym2151Reader implements FmDspChipReader {

    /** carrier mask per algorithm, {@link mdplayer.chips.BaseChip#algM} */
    private static final byte[] algM = {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};

    private ChipRegister chipRegister;

    private final int[] prevKeyOns = new int[8];
    private boolean active;

    private Ym2151Chip chip() {
        return chipRegister.chip(Ym2151Chip.class);
    }

    @Override
    public String chipName() {
        return "OPM";
    }

    @Override
    public void bind(ChipRegister chipRegister) {
        this.chipRegister = chipRegister;
    }

    @Override
    public void reset() {
        Arrays.fill(prevKeyOns, 0);
        active = false;
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.FM);
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public boolean ready() {
        if (chipRegister == null) return false;
        Ym2151Chip chip = chip();
        return chip != null;
    }

    @Override
    public boolean active(Group group) {
        if (!active) {
            for (int ch = 0; ch < 8 && !active; ch++) active = boolOf("channels." + ch + ".keyOn");
        }
        return active;
    }

    @Override
    public int channels(Group group) {
        return 8;
    }

    @Override
    public void read(Group group, int ch, FmDspChannel out) {
        if (info == null) return;
        boolean on = boolOf("channels." + ch + ".keyOn");
        int kc = on ? 1 : 0;

        out.name = "FM";
        out.num = ch + 1;
        out.sounding = on;
        out.keyOn = kc != prevKeyOns[ch] && on;
        prevKeyOns[ch] = kc;

        int keyCode = intOf("channels." + ch + ".keyCode");
        int code = keyCode & 0x0f;
        // the OPM key code skips every fourth value: 0 is C#, 14 the next octave's C
        out.note = ((keyCode >> 4) & 0x07) * 12 + code - (code >> 2) + 1;
        int tl = intOf("channels." + ch + ".totalLevel");
        out.volume = 127 - tl;
        out.amplitude = Math.pow(10, -tl * 0.75 / 20);
        out.pan = panOf(intOf("channels." + ch + ".pan") << 6);
    }


    /** the OPM register 0x20 pan bits, left in bit 6 and right in bit 7 */
    private static Pan panOf(int reg) {
        boolean left = (reg & 0x40) != 0;
        boolean right = (reg & 0x80) != 0;
        if (left && right) return Pan.CENTER;
        if (left) return Pan.LEFT;
        if (right) return Pan.RIGHT;
        return Pan.NONE;
    }

    @Override
    public boolean masked(Group group, int ch) {
        return chip().getMask(0, ch);
    }

    @Override
    public int timerB() {
        return intOf("timerB");
    }

    /** the chip's channel state, read back once a frame */
    private java.util.Map<String, Object> info;

    @Override
    public void poll() {
        try {
            info = chip().getInfo(0);
        } catch (RuntimeException ignore) {
            info = null; // the chip exists but the song never loaded it
        }
    }

    private int intOf(String key) {
        Object value = info == null ? null : info.get(key);
        return value instanceof Integer i ? i : 0;
    }

    private boolean boolOf(String key) {
        Object value = info == null ? null : info.get(key);
        return value instanceof Boolean b && b;
    }
}
