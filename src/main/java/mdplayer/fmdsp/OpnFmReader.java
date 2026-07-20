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
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;
import vavi.sound.visualizer.fmdsp.TrackInfo;


/**
 * The OPN family's shared shape: FM channels with the ch3 extended slots on the three extra
 * channels 6-8, and optionally the SSG section. Subclasses supply the chip's register banks and
 * key-on cache.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public abstract class OpnFmReader implements FmDspChipReader {

    /** carrier mask per algorithm, {@link mdplayer.chips.BaseChip#algM} */
    private static final byte[] algM = {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};

    /** ch3 extended slots 1-3: F-number registers and the reg 0x28 key bit of each */
    private static final int[] exFnumLo = {0xa9, 0xaa, 0xa8};
    private static final int[] exFnumHi = {0xad, 0xae, 0xac};
    private static final int[] exKeyBit = {0x10, 0x20, 0x40};

    protected ChipRegister chipRegister;

    private final int[] prevKeyOns = new int[6];
    private final boolean[] prevExOns = new boolean[3];
    private final boolean[] prevSsgSoundings = new boolean[3];
    private final int[] prevSsgPeriods = new int[3];
    private boolean fmActive;
    private boolean ssgActive;

    /** the chip's register banks; a single-port chip returns one bank */
    protected abstract int[][] ports();

    /** the chip's 6-entry key-on cache */
    protected abstract int[] keyOns();

    /** the chip exists and its caches are initialized */
    protected abstract boolean chipReady();

    /** FM channels the chip actually has, 3 or 6 */
    protected int fmCount() {
        return 6;
    }

    protected boolean hasSsg() {
        return false;
    }

    /** the mono members of the family have no register 0xb4 */
    protected boolean hasPan() {
        return true;
    }


    /** F-number coefficient [Hz]: {@code freq = fnum * 2^(block-1) * fnumK / 2^20} */
    protected double fnumK() {
        return 7987200.0 / 144;
    }

    /** FM channel mute in the chip's own numbering, {@code ch} 0-8 with the ch3 slots at 6-8 */
    protected boolean fmMasked(int ch) {
        return false;
    }

    protected boolean ssgMasked(int s) {
        return false;
    }

    @Override
    public void bind(ChipRegister chipRegister) {
        this.chipRegister = chipRegister;
    }

    @Override
    public void reset() {
        Arrays.fill(prevKeyOns, 0);
        Arrays.fill(prevExOns, false);
        Arrays.fill(prevSsgSoundings, false);
        Arrays.fill(prevSsgPeriods, 0);
        fmActive = false;
        ssgActive = false;
    }

    @Override
    public Set<Group> groups() {
        return hasSsg() ? EnumSet.of(Group.FM, Group.SSG) : EnumSet.of(Group.FM);
    }

    @Override
    public boolean ready() {
        return chipRegister != null && chipReady();
    }

    @Override
    public boolean active(Group group) {
        return switch (group) {
            case FM -> {
                if (!fmActive) fmActive = Arrays.stream(keyOns()).anyMatch(k -> k != 0);
                yield fmActive;
            }
            case SSG -> {
                if (!ssgActive) {
                    for (int s = 0; s < 3 && !ssgActive; s++) ssgActive = ssgSounding(s);
                }
                yield ssgActive;
            }
            default -> false;
        };
    }

    @Override
    public int channels(Group group) {
        return group == Group.FM ? 9 : 3;
    }

    @Override
    public int meters(Group group) {
        return switch (group) {
            case FM -> fmCount();
            case SSG -> 3;
            default -> channels(group);
        };
    }

    /** the ch3 extension rows are operator slots of FM3, they have no meter of their own */
    @Override
    public int meterOf(Group group, int ch) {
        if (group != Group.FM) return ch;
        return ch < fmCount() ? ch : -1;
    }

    @Override
    public void read(Group group, int ch, FmDspChannel out) {
        if (group == Group.SSG) {
            readSsg(ch, out);
        } else {
            readFm(ch, out);
        }
    }

    @Override
    public boolean masked(Group group, int ch) {
        return group == Group.FM ? fmMasked(ch) : ssgMasked(ch);
    }

    @Override
    public int timerB() {
        return timerBRegister();
    }

    // ----- FM -----

    private void readFm(int ch, FmDspChannel out) {
        boolean ch3ex = ch3Extended();

        out.name = "FM";
        if (ch < 6) {
            out.num = ch + 1;
            if (ch >= fmCount()) return;
            int kc = keyOns()[ch];
            boolean on = ch == 2 && ch3ex ? (kc & 0x80) != 0 : (kc & 1) != 0;
            out.keyOn = kc != prevKeyOns[ch] && on;
            prevKeyOns[ch] = kc;
            out.sounding = on;
            out.note = fmNote(fmFnum(ch), fmBlock(ch));
            int tl = fmTotalLevel(ch);
            out.volume = 127 - tl;
            out.amplitude = Math.pow(10, -tl * 0.75 / 20);
            out.pan = hasPan() ? fmPan(ch) : Pan.CENTER;
        } else {
            int x = ch - 6;
            out.num = 3; // the ch3 slots number as FM3, like the C fmdsp
            if (!ch3ex) {
                prevExOns[x] = false;
                return;
            }
            boolean on = (keyOns()[2] & exKeyBit[x]) != 0;
            out.info = TrackInfo.FM3EX;
            out.sounding = on;
            out.keyOn = on && !prevExOns[x];
            prevExOns[x] = on;
            out.note = fmNote(exFnum(x), exBlock(x));
            out.volume = 127 - fmTotalLevel(2);
        }
    }

    /** the value of register 0x26, which paces the display's clock */
    protected int timerBRegister() {
        return ports()[0][0x26] & 0xff;
    }

    // ----- what a channel is doing; register backed chips answer from their bank -----

    /** the ch3 special mode: {@code 0x40} in register 0x27, CSM keying the chip as normal */
    protected boolean ch3Extended() {
        return (ports()[0][0x27] & 0xc0) == 0x40;
    }

    protected int fmFnum(int ch) {
        int[] regs = ports()[ch / 3];
        int r = ch % 3;
        return ((regs[0xa4 + r] & 0x07) << 8) | (regs[0xa0 + r] & 0xff);
    }

    protected int fmBlock(int ch) {
        return (ports()[ch / 3][0xa4 + ch % 3] >> 3) & 0x07;
    }

    protected int fmTotalLevel(int ch) {
        return carrierTl(ports()[ch / 3], 0xb0 + ch % 3, 0x40 + ch % 3);
    }

    protected Pan fmPan(int ch) {
        return opnPan(ports()[ch / 3][0xb4 + ch % 3]);
    }

    protected int exFnum(int x) {
        int[] regs = ports()[0];
        return ((regs[exFnumHi[x]] & 0x07) << 8) | (regs[exFnumLo[x]] & 0xff);
    }

    protected int exBlock(int x) {
        return (ports()[0][exFnumHi[x]] >> 3) & 0x07;
    }

    /** semitones above C0 of an OPN F-number and block, -1 when no note was set */
    protected final int fmNote(int fnum, int block) {
        if (fnum == 0) return -1;
        return Notes.noteOf(fnum * Math.pow(2, block - 1) * fnumK() / (1 << 20));
    }

    /**
     * The softest carrier's total level of a channel, the closest thing the register file has to
     * the part volume.
     */
    private static int carrierTl(int[] regs, int algReg, int tlReg) {
        int alg = regs[algReg] & 0x07;
        int tl = 127;
        for (int slot = 0; slot < 4; slot++) {
            if ((algM[alg] & (1 << slot)) == 0) continue;
            // logical slot order is M1,M2,C1,C2 while the register file is M1,C1,M2,C2
            int t = slot == 1 ? 2 : slot == 2 ? 1 : slot;
            tl = Math.min(tl, regs[tlReg + t * 4] & 0x7f);
        }
        return tl;
    }

    /** the OPN register 0xb4 pan bits */
    protected static Pan opnPan(int reg) {
        boolean left = (reg & 0x80) != 0;
        boolean right = (reg & 0x40) != 0;
        if (left && right) return Pan.CENTER;
        if (left) return Pan.LEFT;
        if (right) return Pan.RIGHT;
        return Pan.NONE;
    }

    // ----- SSG -----

    /** the SSG section's registers; the OPN family keeps them in its PSG */
    protected int[] ssgRegs() {
        return ports()[0];
    }

    private boolean ssgSounding(int s) {
        int[] regs = ssgRegs();
        if (regs == null) return false;
        int level = regs[0x08 + s] & 0x1f;
        boolean tone = (regs[0x07] & (1 << s)) == 0;
        boolean noise = (regs[0x07] & (8 << s)) == 0;
        return (tone || noise) && level > 0;
    }

    private void readSsg(int s, FmDspChannel out) {
        int[] regs = ssgRegs();
        if (regs == null) return;
        int period = (regs[s * 2] & 0xff) | ((regs[s * 2 + 1] & 0x0f) << 8);
        int level = regs[0x08 + s] & 0x1f;
        // a part on the hardware envelope has no readable level, meter it at full scale
        if ((level & 0x10) != 0) level = 15;
        boolean tone = (regs[0x07] & (1 << s)) == 0;
        boolean noise = (regs[0x07] & (8 << s)) == 0;
        boolean sounding = (tone || noise) && level > 0;

        out.name = "SSG";
        out.num = s + 1;
        out.info = TrackInfo.SSG;
        out.sounding = sounding;
        out.keyOn = sounding && (!prevSsgSoundings[s] || period != prevSsgPeriods[s]);
        prevSsgSoundings[s] = sounding;
        prevSsgPeriods[s] = period;
        // the SSG section runs at half the FM clock input, one tone step is clock/2 / 16
        out.note = period > 0 ? Notes.noteOf(fnumK() * 144 / 4 / (16.0 * period)) : -1;
        out.volume = level;
        out.ssgTone = tone;
        out.ssgNoise = noise;
        // one step of the SSG's 16 level table is 3 dB
        out.amplitude = Math.pow(10, (Math.min(level, 15) - 15) * 3.0 / 20);
        out.pan = Pan.CENTER;
    }
}
