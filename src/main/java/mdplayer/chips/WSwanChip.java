/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.WSwanInst;


/**
 * WSwanChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class WSwanChip implements Chip {

    private Audio context;

    @Override
    public void init(Audio context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriWSW", 2);
        else
            context.chipLED.put("SecWSW", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(WSwanInst.class, chipId, 0, port, data);
        }
    }

    public void writeMemory(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriWSW", 2);
        else
            context.chipLED.put("SecWSW", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(WSwanInst.class).writeMemory(chipId, port, data);
        }
    }
}
