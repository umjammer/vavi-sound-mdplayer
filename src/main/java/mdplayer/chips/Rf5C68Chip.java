/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.chips.Rf5C68;
import mdsound.instrument.Rf5C68Inst;


/**
 * Rf5C68Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Rf5C68Chip extends BaseChip {

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Rf5C68Inst.class};
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
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(Rf5C68Inst.class).writePcm(chipId, buf, offset, length, srcOffset);

        dumpData(model, "RF5C68_PCMData(8BitMonoSigned)", srcOffset, buf, length);
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, adr, data);
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

    public Rf5C68 read(int chipId) {
        return context.mds.inst(Rf5C68Inst.class).getChip(chipId);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
