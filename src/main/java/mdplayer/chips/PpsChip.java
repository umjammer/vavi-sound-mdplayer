/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;


/**
 * PpsChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class PpsChip implements Chip {

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void loadPPSDRV(int chipId, byte[] additionalData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPSDRV", 2);
        else
            context.chipLED.put("SecPPSDRV", 2);

        context.mds.writePPSDRVPCMData(chipId, additionalData);
    }

    public void writePPSDRV(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPSDRV", 2);
        else
            context.chipLED.put("SecPPSDRV", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        context.mds.writePPSDRV(chipId, dPort, dAddr, dData, null);
    }
}
