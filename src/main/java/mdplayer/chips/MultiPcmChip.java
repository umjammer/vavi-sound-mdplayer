/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.chips.MultiPCM;
import mdsound.instrument.MultiPcmInst;


/**
 * MultiPcmChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MultiPcmChip implements Chip {

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

    public void setMultiPCMRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(MultiPcmInst.class, chipId, 0, dAddr, dData);
        } else {
        }
    }

    public MultiPCM getMultiPCMRegister(int chipId) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        return context.mds.ReadMultiPCMRegister(chipId);
    }

    public void setMultiPCMSetBank(int chipId, int dCh, int dAddr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.WriteMultiPCMSetBank(chipId, dCh, dAddr);
        } else {
        }
    }

    public void writeMultiPCMPCMData(int chipId,
                                     int romSize,
                                     int dataStart,
                                     int dataLength,
                                     byte[] romData,
                                     int srcStartAdr,
                                     EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteMultiPCMPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

//    public MultiPCM getMultiPCMRegister(int chipId) {
//        return getMultiPCMRegister(chipId);
//    }
}
