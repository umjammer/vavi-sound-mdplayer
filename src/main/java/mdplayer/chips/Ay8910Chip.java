/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RC86ctlSoundChip;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.Ay8910Inst;
import mdsound.instrument.MameAy8910Inst;


/**
 * Ay8910Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ay8910Chip implements Chip {

    private final Setting.ChipType2[] ctAY8910 = new Setting.ChipType2[] {
            setting.getAY8910Type()[0], setting.getAY8910Type()[1]
    };

    private final RSoundChip[] scAY8910 = {null, null};

    public int[][] psgRegisterAY8910 = {null, null};

    public int[][] psgKeyOnAY8910 = {null, null};

    private final int[] nowAY8910FadeoutVol = {0, 0};

    public int[][] psgVolAY8910 = {new int[3], new int[3]};

    private final boolean[][] maskPSGChAY8910 = {
            {false, false, false},
            {false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            psgRegisterAY8910[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                psgRegisterAY8910[chipId][i] = 0;
            }
            psgKeyOnAY8910[chipId] = new int[] {0, 0, 0};

            nowAY8910FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setAY8910Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriAY10", 2);
        else
            context.chipLED.put("SecAY10", 2);

        if (model == EnmModel.VirtualModel)
            psgRegisterAY8910[chipId][dAddr] = dData;

        // psg mixer
        if (dAddr == 0x07) {
            int maskData = 0;
            if (maskPSGChAY8910[chipId][0])
                maskData |= 0x9 << 0;
            if (maskPSGChAY8910[chipId][1])
                maskData |= 0x9 << 1;
            if (maskPSGChAY8910[chipId][2])
                maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // psg level
        if ((dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowAY8910FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskPSGChAY8910[chipId][dAddr - 0x08] ? 0 : dData;
        }

        if (model == EnmModel.VirtualModel) {
            if (ctAY8910[chipId].getUseReal()[0])
                return;
            if (ctAY8910[chipId].getUseEmu()[0])
                context.mds.write(Ay8910Inst.class, chipId, 0, dAddr, dData);
            else if (ctAY8910[chipId].getUseEmu()[1])
                context.mds.write(MameAy8910Inst.class, chipId, 0, dAddr, dData);
        } else {
            if (scAY8910[chipId] == null)
                return;
            scAY8910[chipId].setRegister(dAddr + 0x000, dData);
        }
    }

    public void softResetAY8910(int chipId, EnmModel model) {

        // All Channel Key Off
        setAY8910Register(chipId, 0x07, 0x00, model);

        // Volume Off
        for (int ch = 0; ch < 3; ch++) {
            setAY8910Register(chipId, 0x8 + ch, 0x00, model);
        }

        // Noise Initialization
        setAY8910Register(chipId, 0x06, 0x00, model);
        // Envelope Initialization
        setAY8910Register(chipId, 0x0b, 0x00, model);
        setAY8910Register(chipId, 0x0c, 0x00, model);
        setAY8910Register(chipId, 0x0d, 0x00, model);
    }

    public void setMaskAY8910(int chipId, int ch, boolean mask) {
        maskPSGChAY8910[chipId][ch] = mask;

        setAY8910Register(chipId, 0x8 + ch, psgRegisterAY8910[chipId][8 + ch], EnmModel.VirtualModel);
        setAY8910Register(chipId, 0x8 + ch, psgRegisterAY8910[chipId][8 + ch], EnmModel.RealModel);
    }

    public void setFadeoutVolAY8910(int chipId, int v) {
        nowAY8910FadeoutVol[chipId] = v;
        for (int c = 0; c < 3; c++) {
            setAY8910Register(chipId, 0x8 + c, psgRegisterAY8910[chipId][0x8 + c], EnmModel.RealModel);
        }
    }

    public void writeAY8910Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scAY8910 != null && scAY8910[chipId] != null) {
                if (scAY8910[chipId] instanceof RC86ctlSoundChip) {
//                    Nc86ctl.ChipType ct = ((RC86ctlSoundChip) scAY8910[chipId]).ChipType;
//                    // If YM2149 is selected, double the frequency
//                    if (ct == Nc86ctl.ChipType.CHIP_YM2149) {
//                        clock *= 2;
//                    }
                }
                scAY8910[chipId].dClock = scAY8910[chipId].setMasterClock(clock);
            }
        }
    }

    public int[] getAY8910Register(int chipId) {
        return psgRegisterAY8910[chipId];
    }

    public void setAY8910Mask(int chipId, int ch) {
        setMaskAY8910(chipId, ch, true);
    }

    public void resetAY8910Mask(int chipId, int ch) {
        setMaskAY8910(chipId, ch, false);
    }

    @Override
    public void softReset(EnmModel model) {
        softResetAY8910(0, model);
        softResetAY8910(1, model);
    }

    @Override
    public void clearFadeoutVolume() {
        setFadeoutVolAY8910(0, 0);
        setFadeoutVolAY8910(1, 0);
    }
}
