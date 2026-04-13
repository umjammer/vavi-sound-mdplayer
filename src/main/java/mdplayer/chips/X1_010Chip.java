/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.X1_010Inst;


/**
 * X1_010Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class X1_010Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {X1_010Inst.class};
    }

    public void write(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriX1010", 2);
        else
            context.chipLED.put("SecX1010", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, 0, mm * 0x100 + ll, rr);
        } else {
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriX1010", 2);
        else
            context.chipLED.put("SecX1010", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(X1_010Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "X1-010_PCMData", srcOffset, buf, length);
    }
}
