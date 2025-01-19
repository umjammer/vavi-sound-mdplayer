/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.Saa1099Inst;


/**
 * Saa1099Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Saa1099Chip implements Chip {

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

    public void writeSAA1099(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriSAA", 2);
        else
            context.chipLED.put("SecSAA", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(Saa1099Inst.class, chipId, 0, port, data);
        }
    }
}
