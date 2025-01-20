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
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Sn76496Inst;

import static java.lang.System.getLogger;


/**
 * Sn76489Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Sn76489Chip implements Chip {

    private static final Logger logger = getLogger(Sn76489Chip.class.getName());

    private final Setting.ChipType2[] ctSN76489 = new Setting.ChipType2[] {
            setting.getSN76489Type()[0], setting.getSN76489Type()[1]
    };

    private final RSoundChip[] scSN76489 = {null, null};

    public int[][] sn76489Register = {null, null};

    public int[] sn76489RegisterGGPan = {0xff, 0xff};

    public int[][][] sn76489Vol = {
            {new int[2], new int[2], new int[2], new int[2]},
            {new int[2], new int[2], new int[2], new int[2]}
    };

    public int[] nowSN76489FadeoutVol = {0, 0};

    public boolean[][] maskChSN76489 = {
            {false, false, false, false},
            {false, false, false, false}
    };

    private final int[] LatchedRegister = {
            0, 0
    };

    private final int[] NoiseFreq = {
            0, 0
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            sn76489Register[chipId] = new int[] {0, 15, 0, 15, 0, 15, 0, 15};

            nowSN76489FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int c = 0; c < 4; c++) {
                setSN76489Register(chipId, 0x90 + (c << 5) + 0xf, EnmModel.RealModel);
            }
        }
    }

    @Override
    public void updateVol() {
    }

    public void setSN76489Register(int chipId, int dData, EnmModel model) {

        if (chipId == 0)
            context.chipLED.put("PriDCSG", 2);
        else
            context.chipLED.put("SecDCSG", 2);

        writeSN76489(chipId, dData);

        if ((dData & 0x10) != 0) {
            if (LatchedRegister[chipId] != 0 && LatchedRegister[chipId] != 2 && LatchedRegister[chipId] != 4 &&
                    LatchedRegister[chipId] != 6) {
                sn76489Vol[chipId][(dData & 0x60) >> 5][0] = (15 - (dData & 0xf)) *
                        ((sn76489RegisterGGPan[chipId] >> (((dData & 0x60) >> 5) + 4)) &
                                0x1);
                sn76489Vol[chipId][(dData & 0x60) >> 5][1] = (15 - (dData & 0xf)) *
                        ((sn76489RegisterGGPan[chipId] >> ((dData & 0x60) >> 5)) & 0x1);

                int v = dData & 0xf;
                v = v + nowSN76489FadeoutVol[chipId];
                v = maskChSN76489[chipId][(dData & 0x60) >> 5] ? 15 : v;
                v = Math.min(v, 15);
                dData = (dData & 0xf0) | v;
            }
        }

        if (model == EnmModel.RealModel) {
            if (ctSN76489[chipId].getUseReal()[0]) {
                if (scSN76489[chipId] == null)
                    return;
                scSN76489[chipId].setRegister(0, dData);
            }
        } else {
            if (!ctSN76489[chipId].getUseReal()[0]) {
                if (ctSN76489[chipId].getUseEmu()[0])
                    context.mds.write(Sn76489Inst.class, chipId, 0, 0, dData);
                else if (ctSN76489[chipId].getUseEmu()[1])
                    context.mds.write(Sn76496Inst.class, chipId, 0, 0, dData);
            }
        }
    }

    public void setSN76489RegisterGGpanning(int chipId, int dData, EnmModel model) {
        if (ctSN76489 == null)
            return;

        if (chipId == 0)
            context.chipLED.put("PriDCSG", 2);
        else
            context.chipLED.put("SecDCSG", 2);

        if (model == EnmModel.RealModel) {
            if (ctSN76489[chipId].getUseReal()[0]) {
                if (scSN76489[chipId] == null) {
                }
            }
        } else {
            if (!ctSN76489[chipId].getUseReal()[0]) {
                if (ctSN76489[chipId].getUseEmu()[0])
                    context.mds.writeSn76489GGPanning(chipId, dData);
                else if (ctSN76489[chipId].getUseEmu()[1])
                    context.mds.writeSn76496GGPanning(chipId, dData);
                sn76489RegisterGGPan[chipId] = dData;
            }
        }
    }

    public void setMaskSN76489(int chipId, int ch, boolean mask) {
        maskChSN76489[chipId][ch] = mask;
    }

    private void writeSN76489(int chipId, int data) {
        if ((data & 0x80) != 0) {
            // Latch/data byte %1 cc t dddd
            LatchedRegister[chipId] = (data >> 4) & 0x07;
            sn76489Register[chipId][LatchedRegister[chipId]] = (sn76489Register[chipId][LatchedRegister[chipId]] &
                    0x3f0) // zero low 4 bits
                    | (data & 0xf); // and replace with data
        } else {
            // data byte %0 - dddddd
            if ((LatchedRegister[chipId] % 2) == 0 && (LatchedRegister[chipId] < 5))
                // Tone register
                sn76489Register[chipId][LatchedRegister[chipId]] = (sn76489Register[chipId][LatchedRegister[chipId]] &
                        0x00f) // zero high 6 bits
                        | ((data & 0x3f) << 4); // and replace with data
            else
                // Other register
                sn76489Register[chipId][LatchedRegister[chipId]] = data & 0x0f; // Replace with data
        }
        switch (LatchedRegister[chipId]) {
            case 0:
            case 2:
            case 4: // Tone channels
                //if (sn76489Register[chipId][LatchedRegister[chipId]] == 0)
                // sn76489Register[chipId][LatchedRegister[chipId]] = 1; // Zero frequency changed to 1 to avoid div/0
                break;
            case 6: // Noise
                NoiseFreq[chipId] = 0x10 << (sn76489Register[chipId][6] & 0x3); // set noise signal generator frequency
                break;
        }
    }

    public void setFadeoutVolSN76489(int chipId, int v) {
        nowSN76489FadeoutVol[chipId] = (v & 0x78) >> 3;
        for (int c = 0; c < 4; c++) {

            setSN76489Register(chipId, 0x90 + (c << 5) + sn76489Register[chipId][1 + (c << 1)], EnmModel.RealModel);
        }
    }

    public int[][] getPSGVolume(int chipId) {
        return sn76489Vol[chipId];
    }

    public int[] getPSGRegister(int chipId) {
        return sn76489Register[chipId];
    }

    public int getPSGRegisterGGPanning(int chipId) {
        return sn76489RegisterGGPan[chipId];
    }

    public void setSN76489Mask(int chipId, int ch) {
        setMaskSN76489(chipId, ch, true);
        sn76489ForcedSendVolume(chipId, ch);
    }

    public void resetSN76489Mask(int chipId, int ch) {
        try {
            setMaskSN76489(chipId, ch, false);
            sn76489ForcedSendVolume(chipId, ch);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    protected void sn76489ForcedSendVolume(int chipId, int ch) {
        Setting.ChipType2 ct = setting.getSN76489Type()[chipId];
        setSN76489Register(chipId
                , (0x90
                        | ((ch & 3) << 5)
                        | (15 - (Math.max(sn76489Vol[chipId][ch][0], sn76489Vol[chipId][ch][1]) & 0xf)))
                , ct.getUseEmu()[0] ? Common.EnmModel.VirtualModel : Common.EnmModel.RealModel);
    }

//    public int[][] getPSGVolume(int chipId) {
//        return getPSGVolume(chipId);
//    }

    public boolean sn76489NGPFlag = false;

    public boolean getSn76489NGPFlag() {
        return sn76489NGPFlag;
    }

    @Override
    public void clearFadeoutVolume() {
        setFadeoutVolSN76489( 0, 0);
        setFadeoutVolSN76489( 1, 0);
    }
}
