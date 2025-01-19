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
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.YmFmYm2608Inst;


/**
 * Ym2608Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2608Chip implements Chip {

    private final Setting.ChipType2[] ctYM2608 = new Setting.ChipType2[] {
            setting.getYM2608Type()[0], setting.getYM2608Type()[1]
    };

    private final RSoundChip[] scYM2608 = {null, null};

    public int[][][] fmRegisterYM2608 = {
            {null, null},
            {null, null}
    };

    public int[][] fmKeyOnYM2608 = {null, null};

    public int[][] fmVolYM2608 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public int[][] fmCh3SlotVolYM2608 = {
            new int[4], new int[4]
    };

    public int[][][] fmVolYM2608Rhythm = {
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]},
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}
    };

    public int[][] fmVolYM2608Adpcm = {new int[2], new int[2]};

    public int[] fmVolYM2608AdpcmPan = {0, 0};

    private final int[] nowYM2608FadeoutVol = {0, 0};

    private final boolean[][] maskFMChYM2608 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM2608[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2608[chipId][0][i] = 0; // -1;
                fmRegisterYM2608[chipId][1][i] = 0; // -1;
            }
            fmRegisterYM2608[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2608[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2608[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2608[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            nowYM2608FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    setYM2608Register(chipId, p, 0x40 + c, 127, EnmModel.RealModel);
                    setYM2608Register(chipId, p, 0x44 + c, 127, EnmModel.RealModel);
                    setYM2608Register(chipId, p, 0x48 + c, 127, EnmModel.RealModel);
                    setYM2608Register(chipId, p, 0x4c + c, 127, EnmModel.RealModel);
                }
            }

            // ssg
            setYM2608Register(chipId, 0, 0x08, 0, EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x09, 0, EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x0a, 0, EnmModel.RealModel);

            // rhythm
            setYM2608Register(chipId, 0, 0x11, 0, EnmModel.RealModel);

            // adpcm
            setYM2608Register(chipId, 1, 0x0b, 0, EnmModel.RealModel);
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 9; i++) {
                if (fmVolYM2608[chipId][i] > 0) {
                    fmVolYM2608[chipId][i] -= 50;
                    if (fmVolYM2608[chipId][i] < 0)
                        fmVolYM2608[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2608[chipId][i] > 0) {
                    fmCh3SlotVolYM2608[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2608[chipId][i] < 0)
                        fmCh3SlotVolYM2608[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 6; i++) {
                if (fmVolYM2608Rhythm[chipId][i][0] > 0) {
                    fmVolYM2608Rhythm[chipId][i][0] -= 50;
                    if (fmVolYM2608Rhythm[chipId][i][0] < 0)
                        fmVolYM2608Rhythm[chipId][i][0] = 0;
                }
                if (fmVolYM2608Rhythm[chipId][i][1] > 0) {
                    fmVolYM2608Rhythm[chipId][i][1] -= 50;
                    if (fmVolYM2608Rhythm[chipId][i][1] < 0)
                        fmVolYM2608Rhythm[chipId][i][1] = 0;
                }
            }

            if (fmVolYM2608Adpcm[chipId][0] > 0) {
                fmVolYM2608Adpcm[chipId][0] -= 50;
                if (fmVolYM2608Adpcm[chipId][0] < 0)
                    fmVolYM2608Adpcm[chipId][0] = 0;
            }
            if (fmVolYM2608Adpcm[chipId][1] > 0) {
                fmVolYM2608Adpcm[chipId][1] -= 50;
                if (fmVolYM2608Adpcm[chipId][1] < 0)
                    fmVolYM2608Adpcm[chipId][1] = 0;
            }
        }
    }

    public void setYM2608Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
//if (chipId == 0 && dPort == 1 && dAddr == 0x01) {
// logger.log(Level.TRACE, "FM P1 Out:Adr[%02x] val[%02x]".formatted((int) dAddr, (int) dData));
//}
        if (dAddr < 0 || dData < 0)
            return;

        if (chipId == 0)
            context.chipLED.put("PriOPNA", 2);
        else
            context.chipLED.put("SecOPNA", 2);

        if ((model == EnmModel.VirtualModel && (ctYM2608[chipId] == null || !ctYM2608[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scYM2608 != null && scYM2608[chipId] != null))) {
            if (dPort == 0 && (dAddr == 0x2d || dAddr == 0x2e || dAddr == 0x2f)) {
                fmRegisterYM2608[chipId][0][0x2d] = dAddr - 0x2d;
            } else {
                fmRegisterYM2608[chipId][dPort][dAddr] = dData;
            }
        }

        if ((model == EnmModel.RealModel && ctYM2608[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !ctYM2608[chipId].getUseReal()[0])) {
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6) /* && (dData & 0xf0) > 0) */ {
                    if (ch != 2 || (fmRegisterYM2608[chipId][0][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            fmKeyOnYM2608[chipId][ch] = (dData & 0xf0) | 1;
                            fmVolYM2608[chipId][ch] = 256 * 6;
                        } else {
                            fmKeyOnYM2608[chipId][ch] = (dData & 0xf0) | 0;
                        }
                    } else {
                        fmKeyOnYM2608[chipId][2] = dData & 0xf0;
                        if ((dData & 0x10) > 0)
                            fmCh3SlotVolYM2608[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0)
                            fmCh3SlotVolYM2608[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0)
                            fmCh3SlotVolYM2608[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0)
                            fmCh3SlotVolYM2608[chipId][3] = 256 * 6;
                    }
                }
            }

            if (dPort == 1 && dAddr == 0x01) {
                fmVolYM2608AdpcmPan[chipId] = (dData & 0xc0) >> 6;
                if (fmVolYM2608AdpcmPan[chipId] > 0) {
                    fmVolYM2608Adpcm[chipId][0] = (int) ((256 * 6.0 * fmRegisterYM2608[chipId][1][0x0b] / 64.0) *
                            ((fmVolYM2608AdpcmPan[chipId] & 0x02) > 0 ? 1 : 0));
                    fmVolYM2608Adpcm[chipId][1] = (int) ((256 * 6.0 * fmRegisterYM2608[chipId][1][0x0b] / 64.0) *
                            ((fmVolYM2608AdpcmPan[chipId] & 0x01) > 0 ? 1 : 0));
                }
            }

            if (dPort == 0 && dAddr == 0x10) {
                int tl = fmRegisterYM2608[chipId][0][0x11] & 0x3f;
                for (int i = 0; i < 6; i++) {
                    if ((dData & (0x1 << i)) != 0) {
                        int il = (fmRegisterYM2608[chipId][0][0x18 + i] & 0x1f) * (((dData & 0x80) == 0) ? 1 : 0);
                        int pan = (fmRegisterYM2608[chipId][0][0x18 + i] & 0xc0) >> 6;
                        fmVolYM2608Rhythm[chipId][i][0] = (int) (256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 2) > 0 ? 1 : 0);
                        fmVolYM2608Rhythm[chipId][i][1] = (int) (256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 1) > 0 ? 1 : 0);
                    }
                }
            }
        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int al = fmRegisterYM2608[chipId][dPort][0xb0 + ch] & 0x07;// AL
            int slot = (dAddr & 0xc) >> 2;
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    dData = Math.min(dData + nowYM2608FadeoutVol[chipId], 127);
                    dData = maskFMChYM2608[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0)// AL
        {
            int ch = (dAddr & 0x3);
            int al = dData & 0x07;// AL

            if (ch != 3 && maskFMChYM2608[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) > 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2608Register(chipId,
                                dPort,
                                0x40 + ch + tslot,
                                fmRegisterYM2608[chipId][dPort][0x40 + ch + tslot],
                                model);
                    }
                }
            }
        }

        // ssg mixer
        if (dPort == 0 && dAddr == 0x07) {
            int maskData = 0;
            if (maskFMChYM2608[chipId][6])
                maskData |= 0x9 << 0;
            if (maskFMChYM2608[chipId][7])
                maskData |= 0x9 << 1;
            if (maskFMChYM2608[chipId][8])
                maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // ssg level
        if (dPort == 0 && (dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowYM2608FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2608[chipId][dAddr - 0x08 + 6] ? 0 : dData;
        }

        // rhythm level
        if (dPort == 0 && dAddr == 0x11) {
            int d = nowYM2608FadeoutVol[chipId] >> 1;
            dData = Math.max(dData - d, 0);
        }

        // adpcm level
        if (dPort == 1 && dAddr == 0x0b) {
            int d = nowYM2608FadeoutVol[chipId] * 2;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2608[chipId][12] ? 0 : dData;
        }

        // adpcm start
        if (dPort == 1 && dAddr == 0x00) {
            if ((dData & 0x80) != 0 && maskFMChYM2608[chipId][12]) {
                dData &= 0x7f;
            }
        }

        // Rhythm
        if (dPort == 0 && dAddr == 0x10) {
            if (maskFMChYM2608[chipId][13]) {
                dData = 0;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2608[chipId].getUseReal()[0] && ctYM2608[chipId].getUseEmu()[0]) {
//if (dAddr == 0x29) logger.log(Level.TRACE, "%2x:%2x:%2x ".formatted(dPort, dAddr, dData));
                if (setting.getYM2608Type()[chipId].getUseEmu()[0]) {
                    context.mds.write(Ym2608Inst.class, chipId, dPort, dAddr, dData);
                } else if (setting.getYM2608Type()[chipId].getUseEmu()[1]) {
                    context.mds.write(YmFmYm2608Inst.class, chipId, dPort, dAddr, dData);
                }
            }
        } else {
            if (scYM2608[chipId] == null)
                return;

            scYM2608[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public int getYM2608Register(int chipId, int dPort, int dAddr, EnmModel model) {
        if (ctYM2608 == null)
            return 0;

        if (model == EnmModel.VirtualModel) {
            return 0;
        } else {
            if (scYM2608[chipId] == null)
                return 0;

            return scYM2608[chipId].getRegister(dPort * 0x100 + dAddr);
        }
    }

    private void writeYm2608(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM2608[chipId].getUseReal()[0] && ctYM2608[chipId].getUseEmu()[0]) {
                if (setting.getYM2608Type()[chipId].getUseEmu()[0]) {
                    context.mds.write(Ym2608Inst.class, chipId, dPort, dAddr, dData);
                } else if (setting.getYM2608Type()[chipId].getUseEmu()[1]) {
                    context.mds.write(YmFmYm2608Inst.class, chipId, dPort, dAddr, dData);
                }
            }
        } else {
            if (scYM2608[chipId] == null)
                return;

            scYM2608[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void softResetYM2608(int chipId, EnmModel model) {
        int i;

        // FM All Channel Key Off
        writeYm2608(chipId, 0, 0x28, 0x00, model);
        writeYm2608(chipId, 0, 0x28, 0x01, model);
        writeYm2608(chipId, 0, 0x28, 0x02, model);
        writeYm2608(chipId, 0, 0x28, 0x04, model);
        writeYm2608(chipId, 0, 0x28, 0x05, model);
        writeYm2608(chipId, 0, 0x28, 0x06, model);

        // FM TL=127
        for (i = 0x40; i < 0x4F + 1; i++) {
            writeYm2608(chipId, 0, i, 0x7f, model);
            writeYm2608(chipId, 1, i, 0x7f, model);
        }
        // FM ML/DT
        for (i = 0x30; i < 0x3F + 1; i++) {
            writeYm2608(chipId, 0, i, 0x0, model);
            writeYm2608(chipId, 1, i, 0x0, model);
        }
        // FM AR,DR,SR,KS,AMON
        for (i = 0x50; i < 0x7F + 1; i++) {
            writeYm2608(chipId, 0, i, 0x0, model);
            writeYm2608(chipId, 1, i, 0x0, model);
        }
        // FM SL,RR
        for (i = 0x80; i < 0x8F + 1; i++) {
            writeYm2608(chipId, 0, i, 0xff, model);
            writeYm2608(chipId, 1, i, 0xff, model);
        }
        // FM F-Num, FB/CONNECT
        for (i = 0x90; i < 0xBF + 1; i++) {
            writeYm2608(chipId, 0, i, 0x0, model);
            writeYm2608(chipId, 1, i, 0x0, model);
        }
        // FM PAN/AMS/PMS
        for (i = 0xB4; i < 0xB6 + 1; i++) {
            writeYm2608(chipId, 0, i, 0xc0, model);
            writeYm2608(chipId, 1, i, 0xc0, model);
        }
        writeYm2608(chipId, 0, 0x22, 0x00, model); // HW LFO
        writeYm2608(chipId, 0, 0x24, 0x00, model); // Timer-a(1)
        writeYm2608(chipId, 0, 0x25, 0x00, model); // Timer-a(2)
        writeYm2608(chipId, 0, 0x26, 0x00, model); // Timer-B
        writeYm2608(chipId, 0, 0x27, 0x30, model); // Timer Control
        writeYm2608(chipId, 0, 0x29, 0x80, model); // FM4-6 Enable

        // SSG 音程(2byte*3ch)
        for (i = 0x00; i < 0x05 + 1; i++) {
            writeYm2608(chipId, 0, i, 0x00, model);
        }
        writeYm2608(chipId, 0, 0x06, 0x00, model); // SSG Noise Frequency
        writeYm2608(chipId, 0, 0x07, 0x38, model); // SSG Mixer
        // SSG volume(3ch)
        for (i = 0x08; i < 0x0A + 1; i++) {
            writeYm2608(chipId, 0, i, 0x00, model);
        }
        // SSG Envelope
        for (i = 0x0B; i < 0x0D + 1; i++) {
            writeYm2608(chipId, 0, i, 0x00, model);
        }

        // RHYTHM
        writeYm2608(chipId, 0, 0x10, 0xBF, model); // Forced sound stop
        writeYm2608(chipId, 0, 0x11, 0x00, model); // Total Level
        writeYm2608(chipId, 0, 0x18, 0x00, model); // BD volume
        writeYm2608(chipId, 0, 0x19, 0x00, model); // SD volume
        writeYm2608(chipId, 0, 0x1A, 0x00, model); // CYM volume
        writeYm2608(chipId, 0, 0x1B, 0x00, model); // HH volume
        writeYm2608(chipId, 0, 0x1C, 0x00, model); // TOM volume
        writeYm2608(chipId, 0, 0x1D, 0x00, model); // RIM volume

        // ADPCM
        writeYm2608(chipId, 1, 0x00, 0x21, model); // ADPCM reset
        writeYm2608(chipId, 1, 0x01, 0x06, model); // ADPCM mute
        writeYm2608(chipId, 1, 0x10, 0x9C, model); // FLAG reset
    }

    public void setMaskYM2608(int chipId, int ch, boolean mask, boolean noSend/*=false*/) {
        maskFMChYM2608[chipId][ch] = mask;
        if (ch >= 9 && ch < 12) {
            maskFMChYM2608[chipId][2] = mask;
            maskFMChYM2608[chipId][9] = mask;
            maskFMChYM2608[chipId][10] = mask;
            maskFMChYM2608[chipId][11] = mask;
        }

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        if (noSend) return;

        if (ch < 6) {
            setYM2608Register(chipId, p, 0x40 + c, fmRegisterYM2608[chipId][p][0x40 + c], EnmModel.VirtualModel);
            setYM2608Register(chipId, p, 0x44 + c, fmRegisterYM2608[chipId][p][0x44 + c], EnmModel.VirtualModel);
            setYM2608Register(chipId, p, 0x48 + c, fmRegisterYM2608[chipId][p][0x48 + c], EnmModel.VirtualModel);
            setYM2608Register(chipId, p, 0x4c + c, fmRegisterYM2608[chipId][p][0x4c + c], EnmModel.VirtualModel);

            setYM2608Register(chipId, p, 0x40 + c, fmRegisterYM2608[chipId][p][0x40 + c], EnmModel.RealModel);
            setYM2608Register(chipId, p, 0x44 + c, fmRegisterYM2608[chipId][p][0x44 + c], EnmModel.RealModel);
            setYM2608Register(chipId, p, 0x48 + c, fmRegisterYM2608[chipId][p][0x48 + c], EnmModel.RealModel);
            setYM2608Register(chipId, p, 0x4c + c, fmRegisterYM2608[chipId][p][0x4c + c], EnmModel.RealModel);
        } else if (ch < 9) {
            setYM2608Register(chipId, 0, 0x08 + ch - 6, fmRegisterYM2608[chipId][0][0x08 + ch - 6], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x08 + ch - 6, fmRegisterYM2608[chipId][0][0x08 + ch - 6], EnmModel.RealModel);
        } else if (ch < 12) {
            setYM2608Register(chipId, 0, 0x40 + 2, fmRegisterYM2608[chipId][0][0x40 + 2], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x44 + 2, fmRegisterYM2608[chipId][0][0x44 + 2], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x48 + 2, fmRegisterYM2608[chipId][0][0x48 + 2], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x4c + 2, fmRegisterYM2608[chipId][0][0x4c + 2], EnmModel.VirtualModel);

            setYM2608Register(chipId, 0, 0x40 + 2, fmRegisterYM2608[chipId][0][0x40 + 2], EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x44 + 2, fmRegisterYM2608[chipId][0][0x44 + 2], EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x48 + 2, fmRegisterYM2608[chipId][0][0x48 + 2], EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x4c + 2, fmRegisterYM2608[chipId][0][0x4c + 2], EnmModel.RealModel);
        }
    }

    public void setYM2608SyncWait(int chipId, int wait) {
        if (scYM2608[chipId] != null && ctYM2608[chipId].getRealChipInfo()[0].getUseWait()) {
            scYM2608[chipId].setRegister(-1, (int) (wait * (ctYM2608[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void sendDataYM2608(int chipId, EnmModel model) {
        if (model == EnmModel.VirtualModel)
            return;

        if (scYM2608[chipId] != null && ctYM2608[chipId].getRealChipInfo()[0].getUseWait()) {
            context.realChip.SendData();
            while (!scYM2608[chipId].isBufferEmpty()) {
            }
        }
    }

    public void setFadeoutVolYM2608(int chipId, int v) {

        nowYM2608FadeoutVol[chipId] = v;

        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                setYM2608Register(chipId, p, 0x40 + c, fmRegisterYM2608[chipId][p][0x40 + c], EnmModel.RealModel);
                setYM2608Register(chipId, p, 0x44 + c, fmRegisterYM2608[chipId][p][0x44 + c], EnmModel.RealModel);
                setYM2608Register(chipId, p, 0x48 + c, fmRegisterYM2608[chipId][p][0x48 + c], EnmModel.RealModel);
                setYM2608Register(chipId, p, 0x4c + c, fmRegisterYM2608[chipId][p][0x4c + c], EnmModel.RealModel);
            }
        }

        // ssg
        setYM2608Register(chipId, 0, 0x08, fmRegisterYM2608[chipId][0][0x08], EnmModel.RealModel);
        setYM2608Register(chipId, 0, 0x09, fmRegisterYM2608[chipId][0][0x09], EnmModel.RealModel);
        setYM2608Register(chipId, 0, 0x0a, fmRegisterYM2608[chipId][0][0x0a], EnmModel.RealModel);

        // rhythm
        setYM2608Register(chipId, 0, 0x11, fmRegisterYM2608[chipId][0][0x11], EnmModel.RealModel);

        // adpcm
        setYM2608Register(chipId, 1, 0x0b, fmRegisterYM2608[chipId][1][0x0b], EnmModel.RealModel);
    }

    public void writeYm2608Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2608 != null && scYM2608[chipId] != null) {
                scYM2608[chipId].dClock = scYM2608[chipId].setMasterClock(clock);
            }
        }
    }

    public void setYM2608SSGVolume(int chipId, int vol, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2608 != null && scYM2608[chipId] != null) {
                scYM2608[chipId].setSSGVolume(vol);
            }
        }
    }

    public int[] getYM2608Volume(int chipId) {
        return fmVolYM2608[chipId];
    }

    public int[][] getYM2608RhythmVolume(int chipId) {
        return fmVolYM2608Rhythm[chipId];
    }

    public int[] getYM2608Ch3SlotVolume(int chipId) {
        // if (ctYM2612.UseScci) {
        return fmCh3SlotVolYM2608[chipId];
        // }
        // return mds.readFMCh3SlotVolume();
    }

    public int[] getYM2608AdpcmVolume(int chipId) {
        return fmVolYM2608Adpcm[chipId];
    }
}
