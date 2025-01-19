/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.ScdPcmInst;


/**
 * Rf5C164Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Rf5C164Chip implements Chip {

    private final boolean[][] maskChRF5C164 = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

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

    public void setMaskRF5C164(int chipId, int ch, boolean mask) {
        maskChRF5C164[chipId][ch] = mask;
        if (mask)
            context.mds.setRf5c164Mask(chipId, ch);
        else
            context.mds.resetRf5c164Mask(chipId, ch);
    }

    public void writeRF5C164PCMData(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C", 2);
        else
            context.chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.writeScdPcmPCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public void writeRF5C164(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C", 2);
        else
            context.chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(ScdPcmInst.class, chipId, 0, adr, data);
        }
    }

    public void writeRF5C164MemW(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C", 2);
        else
            context.chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.writeScdPcmMemW(chipId, offset, data);
    }
}
