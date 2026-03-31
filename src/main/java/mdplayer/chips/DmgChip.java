/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.chips.GbSound;
import mdsound.instrument.DmgInst;


/**
 * DmgChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class DmgChip implements Chip {

    public boolean[][] mask = {
            {false, false, false, false},
            {false, false, false, false}
    };

    private BasePlugin context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {DmgInst.class};
    }

    @Override
    public void init(BasePlugin context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriDMG", 2);
        else
            context.chipLED.put("SecDMG", 2);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                context.mds.write(inst(chipId), chipId, 0, addr, data);
//            }
        } else {
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(addr, data);
        }
    }

    public void setMask(int chipId, int ch) {
        mask[chipId][ch] = true;
        context.mds.inst(inst(chipId)).setMask(chipId, ch);
    }

    public void resetMask(int chipId, int ch) {
        mask[chipId][ch] = false;
        context.mds.inst(inst(chipId)).resetMask(chipId, ch);
    }

    public GbSound read(int chipId) {
        if (chipId == 1) return null;

        return context.mds.inst(DmgInst.class).getChip(chipId);
    }
}
