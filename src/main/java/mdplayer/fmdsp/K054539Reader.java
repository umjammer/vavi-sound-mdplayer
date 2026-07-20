/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import mdplayer.chips.BaseChip;
import mdplayer.chips.K054539Chip;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * Konami K054539: eight sampled channels on the PCM rows.
 * <p>
 * The chip hands over its whole register file, twenty bytes a channel:
 * <ul>
 * <li>{@code +0} to {@code +2} the pitch, a 16.16 increment, so {@code 0x10000} is the chip's own
 * rate</li>
 * <li>{@code +3} the volume, which counts the wrong way about - 0 is loudest and 0x40 is -36 dB</li>
 * <li>{@code +5} the pan: 1 to 0xf across the right, 0x10 middle, 0x11 to 0x1f across the left</li>
 * </ul>
 * and {@code 0x22c} says which channels are active, a bit each.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
public class K054539Reader extends PcmSlotReader {

    private static final int CHANNELS = 8;

    /** twenty bytes a channel */
    private static final int STRIDE = 0x20;

    /** the register holding a bit for each active channel */
    private static final int ACTIVE = 0x22c;

    /** the volume register reaches -36 dB here, which is as good as silent on the display */
    private static final double volumeFloor = 0x40;

    private int[] regs;

    @Override
    public String chipName() {
        return "K054";
    }

    @Override
    public int priority() {
        return 55;
    }

    @Override
    protected int channelCount() {
        return CHANNELS;
    }

    @Override
    protected BaseChip chip() {
        return chipRegister.chip(K054539Chip.class);
    }

    @Override
    public void poll() {
        super.poll();
        regs = info != null && info.get("regs") instanceof int[] r ? r : null;
    }

    @Override
    protected boolean sounding(int ch) {
        return regs != null && ACTIVE < regs.length && (regs[ACTIVE] & (1 << ch)) != 0;
    }

    @Override
    protected int rateOf(int ch) {
        if (regs == null) return 0;
        int base = STRIDE * ch;
        return regs[base] | (regs[base + 1] << 8) | (regs[base + 2] << 16);
    }

    @Override
    protected void readChannel(int ch, FmDspChannel out) {
        if (regs == null) return;
        int base = STRIDE * ch;
        int delta = rateOf(ch);
        int volume = regs[base + 3] & 0xff;

        // the register attenuates, so turn it back into something that grows with loudness
        out.volume = (int) Math.max(0, volumeFloor - volume);
        out.amplitude = Math.max(0, 1 - volume / volumeFloor);
        out.pan = panOf(regs[base + 5] & 0x1f);
        out.note = delta > 0 ? Notes.noteOfRatio(delta / 65536.0) : -1;
    }

    /**
     * 1 to 0xf runs across the right, 0x10 is the middle and 0x11 to 0x1f across the left, so the
     * register is a distance from centre in one direction or the other.
     */
    private static Pan panOf(int pan) {
        if (pan == 0 || pan == 0x10) return Pan.CENTER;
        return pan < 0x10 ? PcmSlotReader.panOf(pan, 0x10) : PcmSlotReader.panOf(0x10, 0x20 - pan);
    }

    @Override
    protected boolean muted(int ch) {
        return false; // the chip has no channel mask of its own
    }
}
