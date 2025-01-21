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

    public int[][] register = {null, null};

    public int[][] keyOn = {null, null};

    private final boolean[][] mask = {
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
            register[chipId] = new int[0x203];
            keyOn[chipId] = new int[32];
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC352", 2);
        else
            context.chipLED.put("SecC352", 2);

        if (adr < register[chipId].length)
            register[chipId][adr] = data;
        int c = adr / 8;
        if (adr < 0x100 && (adr % 8) == 3 && mask[chipId][adr / 8]) {
            data &= 0xbfff;
        }
        if (model == EnmModel.VirtualModel)
            context.mds.write(C352Inst.class, chipId, 0, adr, data);
    }

    public int[] read(int chipId) {
        return context.mds.inst(C352Inst.class).readFlags(chipId);
    }

    public void writePcm(int chipId,
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
            context.mds.inst(C352Inst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[] getChip(int chipId) {
        return register[chipId];
    }

    public int[] getKeyOn(int chipId) {
        return read(chipId);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        try {
            setMask(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
