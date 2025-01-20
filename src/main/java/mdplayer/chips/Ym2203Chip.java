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
import mdplayer.RealChip.RC86ctlSoundChip;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.Ym2203Inst;
import mdsound.instrument.YmFmYm2203Inst;

import static java.lang.System.getLogger;


/**
 * Ym2203Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2203Chip implements Chip {

    private static final Logger logger = getLogger(Ym2203Chip.class.getName());

    private final Setting.ChipType2[] ctYM2203 = new Setting.ChipType2[] {
            setting.getYM2203Type()[0], setting.getYM2203Type()[1]
    };
    private final RSoundChip[] scYM2203 = {null, null};

    public int[][] fmRegisterYM2203 = {null, null};
    public int[][] fmKeyOnYM2203 = {null, null};
    public int[][] fmCh3SlotVolYM2203 = {new int[4], new int[4]};
    private final int[] nowYM2203FadeoutVol = {0, 0};
    public int[][] fmVolYM2203 = {new int[9], new int[9]};
    private final boolean[][] maskFMChYM2203 = {
            {false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM2203[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2203[chipId][i] = 0; // -1;
            }
            fmKeyOnYM2203[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            nowYM2203FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {

            for (int i = 0; i < 6; i++) {
                if (fmVolYM2203[chipId][i] > 0) {
                    fmVolYM2203[chipId][i] -= 50;
                    if (fmVolYM2203[chipId][i] < 0)
                        fmVolYM2203[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2203[chipId][i] > 0) {
                    fmCh3SlotVolYM2203[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2203[chipId][i] < 0)
                        fmCh3SlotVolYM2203[chipId][i] = 0;
                }
            }
        }
    }

    public void setYM2203Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctYM2203 == null)
            return;
        if (dAddr < 0 || dData < 0)
            return;

        if (chipId == 0)
            context.chipLED.put("PriOPN", 2);
        else
            context.chipLED.put("SecOPN", 2);

        if (model == EnmModel.VirtualModel) {
            if (dAddr != 0x2d && dAddr != 0x2e && dAddr != 0x2f) {
                fmRegisterYM2203[chipId][dAddr] = dData;
            } else {
                fmRegisterYM2203[chipId][0x2d] = dAddr - 0x2d;
            }
        }

        if ((model == EnmModel.RealModel && ctYM2203[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !ctYM2203[chipId].getUseReal()[0])) {
            if (dAddr == 0x28) {
                int ch = dData & 0x3;
                if (ch >= 0 && ch < 3) {
                    if (ch != 2 || (fmRegisterYM2203[chipId][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            fmKeyOnYM2203[chipId][ch] = (dData & 0xf0) | 1;
                            fmVolYM2203[chipId][ch] = 256 * 6;
                        } else {
                            fmKeyOnYM2203[chipId][ch] &= 0xfe;
                        }
                    } else {
                        fmKeyOnYM2203[chipId][2] = (dData & 0xf0);
                        if ((dData & 0x10) > 0)
                            fmCh3SlotVolYM2203[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0)
                            fmCh3SlotVolYM2203[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0)
                            fmCh3SlotVolYM2203[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0)
                            fmCh3SlotVolYM2203[chipId][3] = 256 * 6;
                    }
                }
            }
        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = fmRegisterYM2203[chipId][0xb0 + ch] & 0x7;
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    dData = Math.min(dData + nowYM2203FadeoutVol[chipId], 127);
                    dData = maskFMChYM2203[chipId][ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) { // AL
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && maskFMChYM2203[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2203Register(chipId, 0x40 + ch + tslot, fmRegisterYM2203[chipId][0x40 + ch + tslot], model);
                    }
                }
            }
        }

        // ssg mixer
        if (dAddr == 0x07) {
            int maskData = 0;
            if (maskFMChYM2203[chipId][3])
                maskData |= 0x9 << 0;
            if (maskFMChYM2203[chipId][4])
                maskData |= 0x9 << 1;
            if (maskFMChYM2203[chipId][5])
                maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // ssg level
        if ((dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowYM2203FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2203[chipId][dAddr - 0x08 + 3] ? 0 : dData;
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2203[chipId].getUseReal()[0]) {
                if (setting.getYM2203Type()[0].getUseEmu()[0]) {
                    context.mds.write(Ym2203Inst.class, chipId, 0, dAddr, dData);
                } else if (setting.getYM2203Type()[0].getUseEmu()[1]) {
                    context.mds.write(YmFmYm2203Inst.class, chipId, 0, dAddr, dData);
                }
            }
        } else {
            if (scYM2203[chipId] == null)
                return;

            scYM2203[chipId].setRegister(dAddr, dData);
        }
    }

    private void writeYm2203(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM2203[chipId].getUseReal()[0]) {
                if (setting.getYM2203Type()[chipId].getUseEmu()[0]) {
                    context.mds.write(Ym2203Inst.class, chipId, 0, dAddr, dData);
                } else if (setting.getYM2203Type()[chipId].getUseEmu()[1]) {
                    context.mds.write(YmFmYm2203Inst.class, chipId, 0, dAddr, dData);
                }
            }
        } else {
            if (scYM2203[chipId] == null)
                return;

            scYM2203[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM2203(int chipId, EnmModel model) {
        // FM all channel key off
        writeYm2203(chipId, 0, 0x28, 0x00, model);
        writeYm2203(chipId, 0, 0x28, 0x01, model);
        writeYm2203(chipId, 0, 0x28, 0x02, model);

        // FM TL=127
        for (int i = 0x40; i < 0x4F + 1; i++) {
            writeYm2203(chipId, 0, i, 0x7f, model);
        }
        // FM ML/DT
        for (int i = 0x30; i < 0x3F + 1; i++) {
            writeYm2203(chipId, 0, i, 0x0, model);
        }
        // FM AR,DR,SR,KS,AMON
        for (int i = 0x50; i < 0x7F + 1; i++) {
            writeYm2203(chipId, 0, i, 0x0, model);
        }
        // FM SL,RR
        for (int i = 0x80; i < 0x8F + 1; i++) {
            writeYm2203(chipId, 0, i, 0xff, model);
        }
        // FM F-Num, FB/CONNECT
        for (int i = 0x90; i < 0xBF + 1; i++) {
            writeYm2203(chipId, 0, i, 0x0, model);
        }
        // FM PAN/AMS/PMS
        for (int i = 0xB4; i < 0xB6 + 1; i++) {
            writeYm2203(chipId, 0, i, 0xc0, model);
        }
        writeYm2203(chipId, 0, 0x22, 0x00, model); // HW LFO
        writeYm2203(chipId, 0, 0x24, 0x00, model); // Timer-a(1)
        writeYm2203(chipId, 0, 0x25, 0x00, model); // Timer-a(2)
        writeYm2203(chipId, 0, 0x26, 0x00, model); // Timer-B
        writeYm2203(chipId, 0, 0x27, 0x30, model); // Timer Control

        // SSG Pitch(2byte*3ch)
        for (int i = 0x00; i < 0x05 + 1; i++) {
            writeYm2203(chipId, 0, i, 0x00, model);
        }
        writeYm2203(chipId, 0, 0x06, 0x00, model); // SSG Noise Frequency
        writeYm2203(chipId, 0, 0x07, 0x38, model); // SSG Mixer
        // SSG volume(3ch)
        for (int i = 0x08; i < 0x0A + 1; i++) {
            writeYm2203(chipId, 0, i, 0x00, model);
        }
        // SSG Envelope
        for (int i = 0x0B; i < 0x0D + 1; i++) {
            writeYm2203(chipId, 0, i, 0x00, model);
        }
    }

    public void setMaskYM2203(int chipId, int ch, boolean mask, boolean noSend /*= false*/) {
        maskFMChYM2203[chipId][ch] = mask;

        if (noSend) return;

        int c = ch;
        if (ch < 3) {
            setYM2203Register(chipId, 0x40 + c, fmRegisterYM2203[chipId][0x40 + c], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x44 + c, fmRegisterYM2203[chipId][0x44 + c], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x48 + c, fmRegisterYM2203[chipId][0x48 + c], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x4c + c, fmRegisterYM2203[chipId][0x4c + c], EnmModel.VirtualModel);

            setYM2203Register(chipId, 0x40 + c, fmRegisterYM2203[chipId][0x40 + c], EnmModel.RealModel);
            setYM2203Register(chipId, 0x44 + c, fmRegisterYM2203[chipId][0x44 + c], EnmModel.RealModel);
            setYM2203Register(chipId, 0x48 + c, fmRegisterYM2203[chipId][0x48 + c], EnmModel.RealModel);
            setYM2203Register(chipId, 0x4c + c, fmRegisterYM2203[chipId][0x4c + c], EnmModel.RealModel);
        } else if (ch < 6) {
            setYM2203Register(chipId, 0x08 + c - 3, fmRegisterYM2203[chipId][0x08 + c - 3], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x08 + c - 3, fmRegisterYM2203[chipId][0x08 + c - 3], EnmModel.RealModel);
        }
    }

    int[] algVolTbl = {
            8, 8, 8, 8, 0xa, 0xe, 0xe, 0xf
    };

    public void setFadeoutVolYM2203(int chipId, int v) {
        nowYM2203FadeoutVol[chipId] = v;
        for (int c = 0; c < 3; c++) {
            int alg = fmRegisterYM2203[chipId][0xb0 + c] & 0x7;
            if ((algVolTbl[alg] & 1) != 0)
                setYM2203Register(chipId, 0x40 + c, fmRegisterYM2203[chipId][0x40 + c], EnmModel.RealModel);
            if ((algVolTbl[alg] & 4) != 0)
                setYM2203Register(chipId, 0x44 + c, fmRegisterYM2203[chipId][0x44 + c], EnmModel.RealModel);
            if ((algVolTbl[alg] & 2) != 0)
                setYM2203Register(chipId, 0x48 + c, fmRegisterYM2203[chipId][0x48 + c], EnmModel.RealModel);
            if ((algVolTbl[alg] & 8) != 0)
                setYM2203Register(chipId, 0x4c + c, fmRegisterYM2203[chipId][0x4c + c], EnmModel.RealModel);
        }
    }

    public void writeYm2203Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2203 != null && scYM2203[chipId] != null) {
                if (scYM2203[chipId] instanceof RC86ctlSoundChip) {
//                    Nc86ctl.ChipType ct = ((RC86ctlSoundChip) scYM2203[chipId]).ChipType;
//                    // If OPNA/OPN3L is selected, double the frequency
//                    if (ct == Nc86ctl.ChipType.CHIP_OPN3L || ct == Nc86ctl.ChipType.CHIP_OPNA) {
//                        clock *= 2;
//                    }
                }
                scYM2203[chipId].dClock = scYM2203[chipId].setMasterClock(clock);
            }
        }
    }

    public void setYM2203SSGVolume(int chipId, int vol, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2203 != null && scYM2203[chipId] != null) {
                scYM2203[chipId].setSSGVolume((byte) vol);
            }
        }
    }

    public int[] getYM2203Volume(int chipId) {
        return fmVolYM2203[chipId];
    }

    public int[] getYM2203Ch3SlotVolume(int chipId) {
        // if (ctYM2612.UseScci) {
        return fmCh3SlotVolYM2203[chipId];
        // }
        // return mds.readFMCh3SlotVolume();
    }

    public int[] getYm2203Register(int chipId) {
        return fmRegisterYM2203[chipId];
    }

    public int[] getYM2203KeyOn(int chipId) {
        return fmKeyOnYM2203[chipId];
    }

    public void setYM2203Mask(int chipId, int ch) {
        setMaskYM2203(chipId, ch, true, false);
    }

    public void resetYM2203Mask(int chipId, int ch, boolean stopped) {
        try {
            setMaskYM2203(chipId, ch, false, stopped);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

//    public int[] getYM2203Volume(int chipId) {
//        return getYM2203Volume(chipId);
//    }

//    public int[] getYM2203Ch3SlotVolume(int chipId) {
//        return getYM2203Ch3SlotVolume(chipId);
//    }

    @Override
    public void softReset(EnmModel model) {
        softResetYM2203(0, model);
        softResetYM2203(1, model);
    }

    @Override
    public void clearFadeoutVolume() {
        setFadeoutVolYM2203(0, 0);
        setFadeoutVolYM2203(1, 0);
    }
}
