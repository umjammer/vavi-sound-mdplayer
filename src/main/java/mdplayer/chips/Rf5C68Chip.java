/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.chips.Rf5c68;
import mdsound.instrument.Rf5C68Inst;


/**
 * Rf5C68Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Rf5C68Chip implements Chip {

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

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

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
        if (mask)
            context.mds.inst(Rf5C68Inst.class).setMask(chipId, ch);
        else
            context.mds.inst(Rf5C68Inst.class).resetMask(chipId, ch);
    }

    public void writePcm(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(Rf5C68Inst.class).writePcm(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(Rf5C68Inst.class, chipId, 0, adr, data);
        }
    }

    public void writeMemory(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(Rf5C68Inst.class).writeMemory(chipId, offset, data);
    }

    public Rf5c68 read(int chipId) {
        return context.mds.inst(Rf5C68Inst.class).getChip(chipId);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
