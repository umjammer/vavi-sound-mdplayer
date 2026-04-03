/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.chips.ScdPcm;
import mdsound.instrument.ScdPcmInst;


/**
 * Rf5C164Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Rf5C164Chip implements Chip {

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    private BasePlugin<? extends BaseDriver> context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {ScdPcmInst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
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
            context.mds.inst(inst(chipId)).setMask(chipId, ch);
        else
            context.mds.inst(inst(chipId)).resetMask(chipId, ch);
    }

    public void writePcm(int chipId, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C", 2);
        else
            context.chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(ScdPcmInst.class).writePcm(chipId, buf, offset, length, srcOffset);
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C", 2);
        else
            context.chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, adr, data);
        }
    }

    public void writeMemory(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C", 2);
        else
            context.chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(ScdPcmInst.class).writeMemory(chipId, offset, data);
    }

    public ScdPcm read(int chipId) {
        return context.mds.inst(ScdPcmInst.class).getChip(chipId);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
