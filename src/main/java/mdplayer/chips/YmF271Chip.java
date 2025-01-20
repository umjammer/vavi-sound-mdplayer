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
import mdsound.chips.YmF271;
import mdsound.instrument.YmF271Inst;


/**
 * YmF271Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF271Chip implements Chip {

    private final Setting.ChipType2[] ctYMF271 = new Setting.ChipType2[] {
            setting.getYMF271Type()[0], setting.getYMF271Type()[1]
    };

    private final RSoundChip[] scYMF271 = {null, null};

    public int[][][] fmRegisterYMF271 = {
            {null, null},
            {null, null}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {

            fmRegisterYMF271[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF271[chipId][0][i] = 0;
                fmRegisterYMF271[chipId][1][i] = 0;
                fmRegisterYMF271[chipId][2][i] = 0;
                fmRegisterYMF271[chipId][3][i] = 0;
                fmRegisterYMF271[chipId][4][i] = 0;
                fmRegisterYMF271[chipId][5][i] = 0;
                fmRegisterYMF271[chipId][6][i] = 0;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setYMF271Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPX", 2);
        else
            context.chipLED.put("SecOPX", 2);

        if (model == EnmModel.VirtualModel)
            fmRegisterYMF271[chipId][dPort][dAddr] = dData;

        if (model == EnmModel.VirtualModel) {
            if (!ctYMF271[chipId].getUseReal()[0]) {
                context.mds.write(YmF271Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF271[chipId] == null)
                return;
            scYMF271[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void writeYmF271PCMData(int chipId,
                                   int romSize,
                                   int dataStart,
                                   int dataLength,
                                   byte[] romData,
                                   int srcStartAdr,
                                   EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPX", 2);
        else
            context.chipLED.put("SecOPX", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteYmf271PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public YmF271 getYMF271Register(int chipId) {
        return context.mds.ReadYmf271Register(chipId);
    }
}
