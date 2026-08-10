/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import vavi.sound.visualizer.fmdsp.TrackDetail;


/**
 * The operator panel an FM channel shows under
 * {@link vavi.sound.visualizer.fmdsp.RightMode#TRACK_INFO}, which is the same for every member of
 * the OPN and OPM families: one bar line per operator, scaled the way the original scales the
 * OPNA's.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-25 nsano initial version <br>
 */
final class FmDetail {

    private FmDetail() {
    }

    /** carrier mask per algorithm, {@code mdplayer.chips.BaseChip#algM} */
    private static final byte[] algM = {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};

    /**
     * The slot the algorithm mask numbers an operator by. Both families keep their operator
     * registers in the order M1, M2, C1, C2 while {@link #algM} runs M1, C1, M2, C2.
     *
     * @param op operator, in register order
     */
    private static int slotOf(int op) {
        return op == 1 ? 2 : op == 2 ? 1 : op;
    }

    /** whether operator {@code op} of algorithm {@code alg} is a carrier, i.e. is heard directly */
    static boolean carrier(int alg, int op) {
        return (algM[alg & 0x07] & (1 << slotOf(op))) != 0;
    }

    /**
     * Fills one operator's line: a bar as long as the level its total level register leaves it, a
     * marker at the end of it, and behind them what the operator is and the register itself.
     * <p>
     * The bar is scaled the way the original scales the OPNA's - a total level of 0 fills it and
     * one of 127 empties it, over the chip's own 96 dB, a column being 1.5 dB - so an OPM channel
     * and an OPN one can be read against each other. This is the line for a chip whose envelope
     * generator cannot be read: the original fills the bar to the envelope and marks the total
     * level at the far end of it, where here the two coincide until {@code ChipFmDspSource} drops
     * the bar by the envelope it models - see {@link TrackDetail#modelled}.
     *
     * @param tl the operator's total level register, 0 (loudest) to 127
     */
    static void operator(TrackDetail out, int line, int tl, boolean carrier) {
        int columns = level(tl << 5);
        out.bar[line] = columns;
        out.mark[line] = columns > 0 ? columns - 1 : -1;
        out.state[line] = carrier ? "CAR" : "MOD";
        out.value[line] = "%03d".formatted(tl & 0x7f);
    }

    /**
     * The same line for a chip whose envelope generator can be read, which is the original's own
     * panel: the bar reaches the level the operator is sounding at, the marker stays at the level
     * its total level allows it, and beside them are the part of the envelope it is in and how far
     * down that envelope has taken it.
     *
     * @param tl the operator's total level register, 0 (loudest) to 127
     * @param envelope the attenuation its envelope adds, 0 (wide open) to 1023 (silent)
     * @param phase which part of the envelope it is in, {@code Fmgen}'s own name for it. The
     *              original knows no {@code Off}: its envelope generator sits in release for ever
     *              instead of stopping, so that one state is the port's own
     */
    static void operator(TrackDetail out, int line, int tl, int envelope, String phase) {
        boolean silent = phase == null || phase.equals("Off");
        out.mark[line] = Math.max(level(tl << 5) - 1, -1);
        out.bar[line] = silent ? 0 : level((tl << 5) + (envelope << 2));
        out.state[line] = switch (phase == null ? "" : phase) {
            case "Attack" -> "ATT";
            case "Decay" -> "DEC";
            case "Sustain" -> "SUS";
            case "Release" -> "REL";
            case "Off" -> "OFF";
            default -> "";
        };
        out.value[line] = "%03X".formatted(silent ? 0x3ff : envelope & 0x3ff);
    }

    /**
     * Columns an attenuation leaves lit, on the original's own 4096 step scale over the chip's
     * 96 dB: 32 steps to a total level, 64 to one of the bar's 1.5 dB columns.
     */
    private static int level(int attenuation) {
        return Math.clamp((4096 - attenuation) / 64, 0, TrackDetail.COLUMNS);
    }
}
