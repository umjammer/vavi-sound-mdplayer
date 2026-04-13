/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.ZxBeepInst;


/**
 * ZxBeepChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-19 nsano initial version <br>
 */
public class ZxBeepChip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {ZxBeepInst.class};
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model == EnmModel.RealModel) return;
        context.mds.write(inst(chipId), chipId, port, addr, data);
    }
}
