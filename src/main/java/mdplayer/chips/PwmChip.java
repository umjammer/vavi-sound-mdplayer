/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.PwmInst;


/**
 * PwmChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class PwmChip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {PwmInst.class};
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriPWM", 2);
        else
            context.chipLED.put("SecPWM", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }
}
