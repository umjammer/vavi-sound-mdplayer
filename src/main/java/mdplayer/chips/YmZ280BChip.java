/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.YmZ280BInst;


/**
 * YmZ280BChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmZ280BChip implements Chip {

    private final Setting.ChipType2[] chipTypes = {
            setting.getYMZ280BType()[0], setting.getYMZ280BType()[1]
    };

    private final RSoundChip[] realChips = {null, null};

    public int[][] register = {null, null};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                register[chipId][i] = 0;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriYMZ", 2);
        else
            context.chipLED.put("SecYMZ", 2);

        if (model == EnmModel.VirtualModel)
            register[chipId][addr] = data;

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(YmZ280BInst.class, chipId, 0, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(addr, data);
        }
    }

    public void writePcm(int chipId,
                         int romSize,
                         int dataStart,
                         int dataLength,
                         byte[] romData,
                         int srcStartAdr,
                         EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriYMZ", 2);
        else
            context.chipLED.put("SecYMZ", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(YmZ280BInst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[] read(int chipId) {
        return register[chipId];
    }
}
