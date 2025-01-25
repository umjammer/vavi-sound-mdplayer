/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.K054539Inst;


/**
 * K054539Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K054539Chip implements Chip {

    private Audio context;

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

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK054539", 2);
        else
            context.chipLED.put("SecK054539", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.write(K054539Inst.class, chipId, 0, adr, data);
    }

    public void writePcm(int chipId,
                         int romSize,
                         int dataStart,
                         int dataLength,
                         byte[] romData,
                         int srcStartAdr,
                         EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK054539", 2);
        else
            context.chipLED.put("SecK054539", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(K054539Inst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }
}
