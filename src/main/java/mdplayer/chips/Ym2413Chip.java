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
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.Ym2413Inst;

import static java.lang.System.getLogger;


/**
 * Ym2413Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2413Chip implements Chip {

    private static final Logger logger = getLogger(Ym2413Chip.class.getName());

    private final Setting.ChipType2[] ctYM2413 = new Setting.ChipType2[] {
            setting.getYM2413Type()[0], setting.getYM2413Type()[1]
    };

    private final RSoundChip[] scYM2413 = {null, null};

    public int[][] fmRegisterYM2413 = {null, null};
//    private int[] fmRegisterYM2413RyhthmB = {0, 0};
//    private int[] fmRegisterYM2413Ryhthm = {0, 0};
    private final ChipKeyInfo[] kiYM2413 = {new ChipKeyInfo(14), new ChipKeyInfo(14)};
    private final ChipKeyInfo[] kiYM2413ret = {new ChipKeyInfo(14), new ChipKeyInfo(14)};
    private final int[] nowYM2413FadeoutVol = {0, 0};
    private final boolean[] rmYM2413 = {false, false};
    private final boolean[][] maskFMChYM2413 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM2413[chipId] = new int[0x39];
            for (int i = 0; i < 0x39; i++) {
                fmRegisterYM2413[chipId][i] = 0;
            }
//                fmRegisterYM2413Ryhthm[0] = 0;
//                fmRegisterYM2413Ryhthm[1] = 0;
//                fmRegisterYM2413RyhthmB[0] = 0;
//                fmRegisterYM2413RyhthmB[1] = 0;

            nowYM2413FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setYM2413Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPLL", 2);
        else
            context.chipLED.put("SecOPLL", 2);

        if (model == EnmModel.VirtualModel)
            fmRegisterYM2413[chipId][dAddr] = dData;

        if (dAddr == 0x0e) {
            rmYM2413[chipId] = (dData & 0x20) != 0;
        }

        if (dAddr >= 0x20 && dAddr <= 0x28) {
            int ch = dAddr - 0x20;
            int k = dData & 0x10;
            if (k == 0) {
                kiYM2413[chipId].off[ch] = true;
            } else {
                if (kiYM2413[chipId].off[ch])
                    kiYM2413[chipId].on[ch] = true;
                kiYM2413[chipId].off[ch] = false;
            }

            // mask適用
            if (maskFMChYM2413[chipId][ch])
                dData &= 0xef;
        }

        if (dAddr >= 0x30 && dAddr < 0x39) { // TL
            int inst = dData & 0xf0;
            int tl = dData & 0x0f;
            int ch = dAddr - 0x30;

            if (dAddr < 0x36 || !rmYM2413[chipId]) {
                dData = Math.min(tl + nowYM2413FadeoutVol[chipId], 0x0f);
                dData = inst | dData;
            } else {
                dData = Math.min(tl + nowYM2413FadeoutVol[chipId], 0x0f);
                if (dAddr > 0x36)
                    dData = (dData << 4) | dData;
            }
        }

        if (dAddr == 0x0e) {
            for (int c = 0; c < 5; c++) {
                if ((dData & (0x10 >> c)) == 0) {
                    kiYM2413[chipId].off[c + 9] = true;
                } else {
                    if (kiYM2413[chipId].off[c + 9])
                        kiYM2413[chipId].on[c + 9] = true;
                    kiYM2413[chipId].off[c + 9] = false;
                }
            }

            dData = (dData & 0x20) | (maskFMChYM2413[chipId][9] ? 0 : (dData & 0x10)) |
                    (maskFMChYM2413[chipId][10] ? 0 : (dData & 0x08)) | (maskFMChYM2413[chipId][11] ? 0 : (dData & 0x04)) |
                    (maskFMChYM2413[chipId][12] ? 0 : (dData & 0x02)) | (maskFMChYM2413[chipId][13] ? 0 : (dData & 0x01));
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2413[chipId].getUseReal()[0]) {
                context.mds.write(Ym2413Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2413[chipId] == null)
                return;
            scYM2413[chipId].setRegister(dAddr, dData);
        }
    }

    public ChipKeyInfo getYM2413KeyInfo(int chipId) {
        for (int ch = 0; ch < kiYM2413[chipId].off.length; ch++) {
            kiYM2413ret[chipId].off[ch] = kiYM2413[chipId].off[ch];
            kiYM2413ret[chipId].on[ch] = kiYM2413[chipId].on[ch];
            kiYM2413[chipId].on[ch] = false;
        }
        return kiYM2413ret[chipId];
    }

//    public int getYM2413RyhthmKeyON(int chipId) {
//        int r = fmRegisterYM2413Ryhthm[chipId];
//        fmRegisterYM2413Ryhthm[chipId] = 0;
//        return r;
//    }

    public void softResetYM2413(int chipId, EnmModel model) {
        // FM All Channel Key Off
        for (int ch = 0; ch < 9; ch++) {
            setYM2413Register(chipId, 0x20 + ch, 0x00, model);
        }
        setYM2413Register(chipId, 0x0e, 0x00, model);

        // FM TL=15
        for (int ch = 0; ch < 9; ch++) {
            setYM2413Register(chipId, 0x30 + ch, 0x0f, model);
        }
        setYM2413Register(chipId, 0x36, 0x0f, model);
        setYM2413Register(chipId, 0x37, 0xff, model);
        setYM2413Register(chipId, 0x38, 0xff, model);
    }

    public void setMaskYM2413(int chipId, int ch, boolean mask) {
        maskFMChYM2413[chipId][ch] = mask;

        if (ch < 9) {
            setYM2413Register(chipId, 0x20 + ch, fmRegisterYM2413[chipId][0x20 + ch], EnmModel.VirtualModel);
            setYM2413Register(chipId, 0x20 + ch, fmRegisterYM2413[chipId][0x20 + ch], EnmModel.RealModel);
        } else if (ch < 14) {
            setYM2413Register(chipId, 0x0e, fmRegisterYM2413[chipId][0x0e], EnmModel.VirtualModel);
            setYM2413Register(chipId, 0x0e, fmRegisterYM2413[chipId][0x0e], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolYM2413(int chipId, int v) {
        nowYM2413FadeoutVol[chipId] = v / (128 / 16);
        for (int c = 0; c < 9; c++) {
            setYM2413Register(chipId, 0x30 + c, fmRegisterYM2413[chipId][0x30 + c], EnmModel.RealModel);
        }
    }

    public int[] getYM2413Register(int chipId) {
        return fmRegisterYM2413[chipId];
    }

//    public Chip.ChipKeyInfo getYM2413KeyInfo(int chipId) {
//        return getYM2413KeyInfo(chipId);
//    }

    public void setYM2413Mask(int chipId, int ch) {
        setMaskYM2413(chipId, ch, true);
    }

    public void resetYM2413Mask(int chipId, int ch) {
        try {
            setMaskYM2413(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void softReset(EnmModel model) {
        softResetYM2413(0, model);
        softResetYM2413(1, model);
    }

    @Override
    public void clearFadeoutVolume() {
        setFadeoutVolYM2413(0, 0);
        setFadeoutVolYM2413(1, 0);
    }
}
