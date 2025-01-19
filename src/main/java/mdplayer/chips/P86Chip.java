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
 * P86Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class P86Chip implements Chip {

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

    public void loadPcmP86(int chipId, int bank, int mode, byte[] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriP86", 2);
        else
            context.chipLED.put("SecP86", 2);

        context.mds.writeP86PCMData(chipId, bank, mode, pcmData);
    }

    public void writeP86(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriP86", 2);
        else
            context.chipLED.put("SecP86", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        context.mds.writeP86(chipId, dPort, dAddr, dData, null);
    }
}
