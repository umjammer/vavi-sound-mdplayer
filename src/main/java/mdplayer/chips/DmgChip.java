/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.chips.GbSound;
import mdsound.instrument.DmgInst;


/**
 * DmgChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class DmgChip implements Chip {

    public boolean[][] maskChDMG = {
            {false, false, false, false},
            {false, false, false, false}
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

    public void setDMGRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriDMG", 2);
        else
            context.chipLED.put("SecDMG", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            context.mds.write(DmgInst.class, chipId, 0, dAddr, dData);
            // }
        } else {
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public void setDMGMask(int chipId, int ch) {
        maskChDMG[chipId][ch] = true;
        context.mds.setGbMask(chipId, ch);
    }

    public void resetDMGMask(int chipId, int ch) {
        maskChDMG[chipId][ch] = false;
        context.mds.resetGbMask(chipId, ch);
    }

    public GbSound getDMGRegister(int chipId) {
        if (chipId == 1) return null;

        return context.mds.ReadGb(chipId);
    }

//    public void setDMGMask(int chipId, int ch) {
//        setDMGMask(chipId, ch);
//    }
//
//    public void resetDMGMask(int chipId, int ch) {
//        resetDMGMask(chipId, ch);
//    }
}
