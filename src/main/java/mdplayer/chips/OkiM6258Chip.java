/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.OkiM6258Inst;


/**
 * Oki6258Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class OkiM6258Chip implements Chip {

    private final boolean[] maskOKIM6258 = {false, false};

    public boolean[] okim6258Keyon = {false, false};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMaskOKIM6258(int chipId, boolean mask) {
        maskOKIM6258[chipId] = mask;

        writeOKIM6258(chipId, 0, 1, EnmModel.VirtualModel);
        writeOKIM6258(chipId, 0, 1, EnmModel.RealModel);
    }

    public void writeOKIM6258(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOKI5", 2);
        else
            context.chipLED.put("SecOKI5", 2);

        if (port == 0x00) {
            if ((data & 0x2) != 0)
                okim6258Keyon[chipId] = true;

            if (maskOKIM6258[chipId]) {
                if ((data & 0x2) != 0)
                    return;
            }
        }
        if (port == 0x1) {
            if (maskOKIM6258[chipId])
                return;
        }

        if (model == EnmModel.VirtualModel) {
            context.mds.write(OkiM6258Inst.class, chipId, 0, port, data);
        }
    }
}
