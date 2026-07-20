/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Collections;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.OkiM6258Inst;


/**
 * Oki6258Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class OkiM6258Chip extends BaseChip {

    private final boolean[] mask = {false, false};

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {OkiM6258Inst.class};
    }

    @Override
    protected void setMask(int chipId, int ch, boolean mask, Object... args) {
        this.mask[chipId] = mask;

        write(chipId, 0, 1, EnmModel.VirtualModel);
        write(chipId, 0, 1, EnmModel.RealModel);
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (port == 0x00) {
            if (mask[chipId]) {
                if ((data & 0x2) != 0)
                    return;
            }
        }
        if (port == 0x1) {
            if (mask[chipId])
                return;
        }

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
        }
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        OkiM6258Inst inst = context.mds.inst(OkiM6258Inst.class);
        return inst == null ? Collections.emptyMap() : inst.getView(chipId, "info");
    }

    @Override
    public boolean getMask(int chipId, int ch) {
        return mask[chipId];
    }
}
