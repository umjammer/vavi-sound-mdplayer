/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.X1_010Inst;


/**
 * X1_010Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class X1_010Chip implements Chip {

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

    public void setX1_010Register(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriX1010", 2);
        else
            context.chipLED.put("SecX1010", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(X1_010Inst.class, chipId, 0, 0, mm * 0x100 + ll, rr);
        } else {
        }
    }

    public void writeX1_010PCMData(int chipId,
                                   int romSize,
                                   int dataStart,
                                   int dataLength,
                                   byte[] romData,
                                   int srcStartAdr,
                                   EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriX1010", 2);
        else
            context.chipLED.put("SecX1010", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.writeX1_010PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }
}
