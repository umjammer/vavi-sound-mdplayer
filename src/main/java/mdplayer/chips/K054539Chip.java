/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.K054539Inst;


/**
 * K054539Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K054539Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {K054539Inst.class};
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK054539", 2);
        else
            context.chipLED.put("SecK054539", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK054539", 2);
        else
            context.chipLED.put("SecK054539", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(K054539Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "K054539_PCMData", srcOffset, buf, length);
    }
}
