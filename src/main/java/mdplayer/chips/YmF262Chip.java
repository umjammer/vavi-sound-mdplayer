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
import mdsound.instrument.YmF262Inst;

import static java.lang.System.getLogger;
import static mdplayer.chips.YmF278BChip.YMF278BCh;


/**
 * YmF262Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF262Chip implements Chip {

    private static final Logger logger = getLogger(YmF262Chip.class.getName());

    private final Setting.ChipType2[] ctYMF262 = new Setting.ChipType2[] {
            setting.getYMF262Type()[0], setting.getYMF262Type()[1]
    };

    private final RSoundChip[] scYMF262 = {null, null};

    public int[][][] fmRegisterYMF262 = {
            {null, null},
            {null, null}
    };

    private final int[] fmRegisterYMF262FM = {0, 0};

    private final int[] fmRegisterYMF262RyhthmB = {0, 0};

    private final int[] fmRegisterYMF262Ryhthm = {0, 0};

    private final boolean[][] maskFMChYMF262 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false}
    };

    private final int[] nowYMF262FadeoutVol = {0, 0};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYMF262[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF262[chipId][0][i] = 0;
                fmRegisterYMF262[chipId][1][i] = 0;
            }

            nowYMF262FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public int getYMF262RyhthmKeyON(int chipId) {
        int r = fmRegisterYMF262Ryhthm[chipId];
        fmRegisterYMF262Ryhthm[chipId] = 0;
        return r;
    }

    public int getYMF262FMKeyON(int chipId) {
        return fmRegisterYMF262FM[chipId];
    }

    public void setYMF262Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL3", 2);
        else
            context.chipLED.put("SecOPL3", 2);

        fmRegisterYMF262[chipId][dPort][dAddr] = dData;

        if (dAddr >= 0x40 && dAddr <= 0x55) { // TL
            int ksl = (dData & 0xc0);
            int tl = (dData & 0x3f);
            int ch = dAddr - 0x40;
            int conSel = fmRegisterYMF262[chipId][1][4] & 0x3f;
            boolean cr = false;

            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);
            int fourOpChannel = twoOpChannel > 5 ? -1 : ((twoOpChannel % 3) + dPort * 3);
            boolean fourOpMode = fourOpChannel != -1 && ((conSel & (1 << fourOpChannel)) != 0);
            int slotNumber = ((ch % 8) / 3) + (twoOpChannel > 2 ? 2 : 0);
            twoOpChannel += dPort * 9;

            if (!fourOpMode) {
                // Career determination during 2op
                if (ch % 8 > 2)
                    cr = true;
                else {
                    int cnt = fmRegisterYMF262[chipId][dPort][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                    if (cnt == 1)
                        cr = true;
                }
            } else {
                if (slotNumber == 3)
                    cr = true;
                else {
                    int cnt0 = fmRegisterYMF262[chipId][dPort][0xc0 + (fourOpChannel % 3)] & 1;
                    int cnt1 = fmRegisterYMF262[chipId][dPort][0xc3 + (fourOpChannel % 3)] & 1;
                    if (cnt0 == 0) {
                        if (cnt1 == 1 && slotNumber == 1)
                            cr = true;
                    } else {
                        if (cnt1 == 0) {
                            if (slotNumber == 0)
                                cr = true;
                        } else {
                            if (slotNumber != 1)
                                cr = true;
                        }
                    }
                }
            }

            if (ch >= 0x10 && dPort == 0 && (fmRegisterYMF262[chipId][dPort][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                dData = Math.min(tl + nowYMF262FadeoutVol[chipId], 0x3f);
                dData = ksl + (maskFMChYMF262[chipId][twoOpChannel] ? 0x3f : dData);
            }
        }

        if (dAddr >= 0xb0 && dAddr <= 0xb8) {
            int ch = dAddr - 0xb0 + dPort * 9;
            int k = (dData >> 5) & 1;
            if (k == 0) {
                fmRegisterYMF262FM[chipId] &= ~(1 << ch);
            } else {
                fmRegisterYMF262FM[chipId] |= (1 << ch);
            }
            fmRegisterYMF262FM[chipId] &= 0x3ffff;
            if (maskFMChYMF262[chipId][ch])
                dData &= 0x1f;
        }

        if (dAddr == 0xbd && dPort == 0) {
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x10) == 0 && (dData & 0x10) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x10;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x08) == 0 && (dData & 0x08) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x08;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x04) == 0 && (dData & 0x04) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x04;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x02) == 0 && (dData & 0x02) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x02;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x01) == 0 && (dData & 0x01) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x01;
            fmRegisterYMF262RyhthmB[chipId] = dData;

            if (maskFMChYMF262[chipId][18])
                dData &= 0xef;
            if (maskFMChYMF262[chipId][19])
                dData &= 0xf7;
            if (maskFMChYMF262[chipId][20])
                dData &= 0xfb;
            if (maskFMChYMF262[chipId][21])
                dData &= 0xfd;
            if (maskFMChYMF262[chipId][22])
                dData &= 0xfe;

        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYMF262[chipId].getUseReal()[0]) {
                context.mds.write(YmF262Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF262[chipId] == null)
                return;
            scYMF262[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    private void writeYmF262(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYMF262[chipId].getUseReal()[0]) {
                context.mds.write(YmF262Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF262[chipId] == null)
                return;

            scYMF262[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void softResetYMF262(int chipId, EnmModel model) {
        int i;

        // FM All Channel Key Off
        for (i = 0; i < 9; i++) {
            writeYmF262(chipId, 0, 0xb0 + i, 0x00, model);
            writeYmF262(chipId, 1, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (i = 0; i < 22; i++) {
            writeYmF262(chipId, 0, 0x40 + i, 0x3f, model);
            writeYmF262(chipId, 1, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (i = 0; i < 22; i++) {
            writeYmF262(chipId, 0, 0x80 + i, 0xff, model);
            writeYmF262(chipId, 1, 0x80 + i, 0xff, model);
        }
    }

    public void setMaskYMF262(int chipId, int ch, boolean mask) {
        maskFMChYMF262[chipId][YMF278BCh[ch]] = mask;
    }

    public void setFadeoutVolYMF262(int chipId, int v) {
        nowYMF262FadeoutVol[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setYMF262Register(chipId, 0, 0x40 + c, fmRegisterYMF262[chipId][0][0x40 + c], EnmModel.RealModel);
            setYMF262Register(chipId, 1, 0x40 + c, fmRegisterYMF262[chipId][1][0x40 + c], EnmModel.RealModel);
        }
    }

    public void writeYmF262Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYMF262 != null && scYMF262[chipId] != null) {
                scYMF262[chipId].dClock = scYMF262[chipId].setMasterClock(clock);
            }
        }
    }

    public int[][] getYMF262Register(int chipId) {
        return fmRegisterYMF262[chipId];
    }

//    public int getYMF262FMKeyON(int chipId) {
//        return getYMF262FMKeyON(chipId);
//    }
//
//    public int getYMF262RyhthmKeyON(int chipId) {
//        return getYMF262RyhthmKeyON(chipId);
//    }

    public void setYMF262Mask(int chipId, int ch) {
        setMaskYMF262(chipId, ch, true);
    }

    public void resetYMF262Mask(int chipId, int ch) {
        try {
            setMaskYMF262(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void softReset(EnmModel model) {
        softResetYMF262(0, model);
        softResetYMF262(1, model);
    }

    @Override
    public void clearFadeoutVolume() {
        setFadeoutVolYMF262(0, 0);
        setFadeoutVolYMF262(1, 0);
    }
}
