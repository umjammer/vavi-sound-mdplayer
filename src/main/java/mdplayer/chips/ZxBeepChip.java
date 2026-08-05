/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Arrays;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.ZxBeepInst;


/**
 * ZxBeepChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-19 nsano initial version <br>
 */
public class ZxBeepChip extends BaseChip {

    /**
     * How many times the speaker has been flipped, which is everything there is to know about it.
     * <p>
     * A ZX Spectrum beeper is one bit: the CPU flips the cone and the tune is in how fast it does
     * it. There is no register to read, no note and no level - a display that waits for one shows
     * an empty screen for the whole song - so the flips are counted here, and how often they come
     * is the pitch.
     *
     * @see mdplayer.fmdsp.ZxBeepReader
     */
    public final long[] flips = new long[2];

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {ZxBeepInst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);
        Arrays.fill(flips, 0); // shared between songs, like the chip itself
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model == EnmModel.RealModel) return;
        flips[chipId]++;
        context.mds.write(inst(chipId), chipId, port, addr, data);
    }
}
