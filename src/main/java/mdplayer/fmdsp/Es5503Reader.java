/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import mdplayer.chips.BaseChip;
import mdplayer.chips.Es5503Chip;


/**
 * Ensoniq ES5503, the Apple IIGS's DOC: thirty two oscillators on the PCM rows.
 * <p>
 * An oscillator steps a wave table by its frequency register, so that register is the playback
 * ratio and the key is the note it comes nearest, {@code 0x10000} being the chip's own rate. The
 * control register halts an oscillator with its bit 0 and picks an output channel in its high
 * nibble - the display has no room for eight outputs, so that becomes a left or right lean.
 * <p>
 * Thirty two oscillators do not fit nine rows, so they are taken in the order they first sound.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
public class Es5503Reader extends PcmSlotReader {

    private static final int OSCILLATORS = 32;

    /** the step that plays a wave table at the chip's own rate */
    private static final double unitFreq = 0x10000;

    /** the level register is eight bits */
    private static final double volumeMax = 255;

    @Override
    public String chipName() {
        return "ES55";
    }

    @Override
    public int priority() {
        return 49;
    }

    @Override
    protected int channelCount() {
        return OSCILLATORS;
    }

    @Override
    protected BaseChip chip() {
        return chipRegister.chip(Es5503Chip.class);
    }

    @Override
    protected boolean sounding(int osc) {
        return info != null && boolOf(osc, "enable") && intOf(osc, "volume", 0) > 0;
    }

    @Override
    protected int rateOf(int osc) {
        return intOf(osc, "frequency", 0);
    }

    @Override
    protected void readChannel(int osc, FmDspChannel out) {
        int freq = rateOf(osc);
        int volume = intOf(osc, "volume", 0);

        out.volume = volume;
        out.amplitude = volume / volumeMax;
        out.toneNum = intOf(osc, "output", 0);
        // even outputs go one way and odd ones the other, which is how a IIGS gets its stereo
        out.pan = (intOf(osc, "output", 0) & 1) == 0
                ? vavi.sound.visualizer.fmdsp.LevelDataSource.Pan.LEFT
                : vavi.sound.visualizer.fmdsp.LevelDataSource.Pan.RIGHT;
        out.note = freq > 0 ? Notes.noteOfRatio(freq / unitFreq) : -1;
    }

    @Override
    protected boolean muted(int osc) {
        return boolOf(osc, "mute");
    }
}
