/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.chips.OkiM6258;
import mdsound.instrument.OkiM6258Inst;


/**
 * Oki6258Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class OkiM6258Chip extends BaseChip {

    private final boolean[] mask = {false, false};

    public final boolean[] keyOn = {false, false};

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {OkiM6258Inst.class};
    }

    public void setMask(int chipId, boolean mask) {
        this.mask[chipId] = mask;

        write(chipId, 0, 1, EnmModel.VirtualModel);
        write(chipId, 0, 1, EnmModel.RealModel);
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOKI5", 2);
        else
            context.chipLED.put("SecOKI5", 2);

        if (port == 0x00) {
            if ((data & 0x2) != 0)
                keyOn[chipId] = true;

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

    // TODO abstract
    public OkiM6258 read(int chipId) {
        return context.mds.inst(OkiM6258Inst.class).getChip(chipId);
    }

    public boolean getKeyOn(int chipId) {
        return keyOn[chipId];
    }

    public void resetKeyOn(int chipId) {
        keyOn[chipId] = false;
    }

    public void setMask(int chipId) {
        setMask(chipId, true);
    }

    public void resetMask(int chipId) {
        setMask(chipId, false);
    }
}
