/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.chips.MultiPCM;
import mdsound.instrument.MultiPcmInst;


/**
 * MultiPcmChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MultiPcmChip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {MultiPcmInst.class};
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, addr, data);
        } else {
        }
    }

    public MultiPCM getChip(int chipId) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        return context.mds.inst(MultiPcmInst.class).getChip(chipId);
    }

    public void setBank(int chipId, int ch, int addr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(MultiPcmInst.class).writeBank(chipId, ch, addr);
        } else {
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(MultiPcmInst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "MultiPCM_PCMData", srcOffset, buf, length);
    }
}
