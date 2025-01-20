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
import mdsound.instrument.YmZ280bInst;


/**
 * YmZ280BChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmZ280BChip implements Chip {

    private final Setting.ChipType2[] ctYMZ280B = new Setting.ChipType2[] {
            setting.getYMZ280BType()[0], setting.getYMZ280BType()[1]
    };

    private final RSoundChip[] scYMZ280B = {null, null};

    public int[][] YMZ280BRegister = {null, null};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            YMZ280BRegister[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                YMZ280BRegister[chipId][i] = 0;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setYMZ280BRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriYMZ", 2);
        else
            context.chipLED.put("SecYMZ", 2);

        if (model == EnmModel.VirtualModel)
            YMZ280BRegister[chipId][dAddr] = dData;

        if (model == EnmModel.VirtualModel) {
            if (!ctYMZ280B[chipId].getUseReal()[0]) {
                context.mds.write(YmZ280bInst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYMZ280B[chipId] == null)
                return;
            scYMZ280B[chipId].setRegister(dAddr, dData);
        }
    }

    public void writeYmZ280BPCMData(int chipId,
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
            context.mds.WriteYmZ280bPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[] getYMZ280BRegister(int chipId) {
        return YMZ280BRegister[chipId];
    }
}
