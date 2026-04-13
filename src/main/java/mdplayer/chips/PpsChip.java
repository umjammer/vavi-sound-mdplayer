/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.PpsInst;


/**
 * PpsChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class PpsChip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {PpsInst.class};
    }

    public void writePcm(int chipId, byte[] buf, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPSDRV", 2);
        else
            context.chipLED.put("SecPPSDRV", 2);

        context.mds.inst(PpsInst.class).writePcm(chipId, buf, 0, buf.length);
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPSDRV", 2);
        else
            context.chipLED.put("SecPPSDRV", 2);

        if (port == -1 && addr == -1 && data == -1)
            return;
        context.mds.inst(inst(chipId)).write(chipId, port, addr, data);
    }
}
