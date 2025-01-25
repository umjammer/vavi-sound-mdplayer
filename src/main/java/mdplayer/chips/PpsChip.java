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
import mdsound.instrument.PpsInst;


/**
 * PpsChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class PpsChip implements Chip {

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

    public void writePcm(int chipId, byte[] additionalData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPSDRV", 2);
        else
            context.chipLED.put("SecPPSDRV", 2);

        context.mds.inst(PpsInst.class).writePcm(chipId, additionalData);
    }

    public void write(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPSDRV", 2);
        else
            context.chipLED.put("SecPPSDRV", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        context.mds.inst(PpsInst.class).write(chipId, dPort, dAddr, dData);
    }
}
