/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.C352Inst;

import static java.lang.System.getLogger;


/**
 * C352Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class C352Chip implements Chip {

    private static final Logger logger = getLogger(C352Chip.class.getName());

    public int[][] pcmRegisterC352 = {null, null};

    public int[][] pcmKeyOnC352 = {null, null};

    private static final boolean[][] maskChC352 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            pcmRegisterC352[chipId] = new int[0x203];
            pcmKeyOnC352[chipId] = new int[32];
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMaskC352(int chipId, int ch, boolean mask) {
        maskChC352[chipId][ch] = mask;
    }

    public void writeC352(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC352", 2);
        else
            context.chipLED.put("SecC352", 2);

        if (adr < pcmRegisterC352[chipId].length)
            pcmRegisterC352[chipId][adr] = data;
        int c = adr / 8;
        if (adr < 0x100 && (adr % 8) == 3 && maskChC352[chipId][adr / 8]) {
            data &= 0xbfff;
        }
        if (model == EnmModel.VirtualModel)
            context.mds.write(C352Inst.class, chipId, 0, adr, data);
    }

    public int[] readC352(int chipId) {
        return context.mds.ReadC352Flag(chipId);
    }

    public void writeC352PCMData(int chipId,
                                 int romSize,
                                 int dataStart,
                                 int dataLength,
                                 byte[] romData,
                                 int srcStartAdr,
                                 EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC352", 2);
        else
            context.chipLED.put("SecC352", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteC352PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[] getC352Register(int chipId) {
        return pcmRegisterC352[chipId];
    }

    public int[] getC352KeyOn(int chipId) {
        return readC352(chipId);
    }

    public void setC352Mask(int chipId, int ch) {
        setMaskC352(chipId, ch, true);
    }

    public void resetC352Mask(int chipId, int ch) {
        try {
            setMaskC352(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
