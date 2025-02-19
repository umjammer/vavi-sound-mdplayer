/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.P86Inst;
import mdsound.instrument.ZxBeepInst;


/**
 * ZxBeepChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-19 nsano initial version <br>
 */
public class ZxBeepChip implements Chip {

    private Audio context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {ZxBeepInst.class};
    }

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

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model == EnmModel.RealModel) return;
        context.mds.write(inst(chipId), chipId, port, addr, data);
    }
}
