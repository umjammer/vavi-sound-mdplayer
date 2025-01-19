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
import mdsound.instrument.Ym2610Inst;


/**
 * Ym2610Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2610Chip implements Chip {

    private final Setting.ChipType2[] ctYM2610 = new Setting.ChipType2[] {
            setting.getYM2610Type()[0], setting.getYM2610Type()[1]
    };

    private final RSoundChip[] scYM2610 = {null, null};
    private final RSoundChip[] scYM2610EA = {null, null};
    private final RSoundChip[] scYM2610EB = {null, null};

    public int[][][] fmRegisterYM2610 = {
            {null, null},
            {null, null}
    };

    public int[][] fmKeyOnYM2610 = {null, null};

    public int[][] fmVolYM2610 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public int[][] fmCh3SlotVolYM2610 = {new int[4], new int[4]};

    public int[][][] fmVolYM2610Rhythm = {
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]},
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}
    };

    public int[][] fmVolYM2610Adpcm = {new int[2], new int[2]};

    public int[] fmVolYM2610AdpcmPan = {0, 0};

    private final int[] nowYM2610FadeoutVol = {0, 0};

    private final boolean[][] maskFMChYM2610 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM2610[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2610[chipId][0][i] = 0; // -1;
                fmRegisterYM2610[chipId][1][i] = 0; // -1;
            }
            fmRegisterYM2610[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2610[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2610[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2610[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            nowYM2610FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    setYM2610Register(chipId, p, 0x40 + c, 127, EnmModel.RealModel);
                    setYM2610Register(chipId, p, 0x44 + c, 127, EnmModel.RealModel);
                    setYM2610Register(chipId, p, 0x48 + c, 127, EnmModel.RealModel);
                    setYM2610Register(chipId, p, 0x4c + c, 127, EnmModel.RealModel);
                }
            }

            // ssg
            setYM2610Register(chipId, 0, 0x08, 0, EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x09, 0, EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x0a, 0, EnmModel.RealModel);

            // rhythm
            setYM2610Register(chipId, 0, 0x11, 0, EnmModel.RealModel);

            // adpcm
            setYM2610Register(chipId, 1, 0x0b, 0, EnmModel.RealModel);
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 9; i++) {
                if (fmVolYM2610[chipId][i] > 0) {
                    fmVolYM2610[chipId][i] -= 50;
                    if (fmVolYM2610[chipId][i] < 0)
                        fmVolYM2610[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2610[chipId][i] > 0) {
                    fmCh3SlotVolYM2610[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2610[chipId][i] < 0)
                        fmCh3SlotVolYM2610[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 6; i++) {
                if (fmVolYM2610Rhythm[chipId][i][0] > 0) {
                    fmVolYM2610Rhythm[chipId][i][0] -= 50;
                    if (fmVolYM2610Rhythm[chipId][i][0] < 0)
                        fmVolYM2610Rhythm[chipId][i][0] = 0;
                }
                if (fmVolYM2610Rhythm[chipId][i][1] > 0) {
                    fmVolYM2610Rhythm[chipId][i][1] -= 50;
                    if (fmVolYM2610Rhythm[chipId][i][1] < 0)
                        fmVolYM2610Rhythm[chipId][i][1] = 0;
                }
            }

            if (fmVolYM2610Adpcm[chipId][0] > 0) {
                fmVolYM2610Adpcm[chipId][0] -= 50;
                if (fmVolYM2610Adpcm[chipId][0] < 0)
                    fmVolYM2610Adpcm[chipId][0] = 0;
            }
            if (fmVolYM2610Adpcm[chipId][1] > 0) {
                fmVolYM2610Adpcm[chipId][1] -= 50;
                if (fmVolYM2610Adpcm[chipId][1] < 0)
                    fmVolYM2610Adpcm[chipId][1] = 0;
            }
        }
    }

    public void setYM2610Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (dAddr < 0 || dData < 0) return;

        if (chipId == 0) context.chipLED.put("PriOPNB", 2);
        else context.chipLED.put("SecOPNB", 2);

        if ((model == EnmModel.VirtualModel && (ctYM2610[chipId] == null || !ctYM2610[chipId].getUseReal()[0]))
                || (model == EnmModel.RealModel && (scYM2610 != null && scYM2610[chipId] != null))
        ) {
            if (dPort == 0 && (dAddr == 0x2d || dAddr == 0x2e || dAddr == 0x2f)) {
                fmRegisterYM2610[chipId][0][0x2d] = dAddr - 0x2d;
            } else {
                fmRegisterYM2610[chipId][dPort][dAddr] = dData;
            }
        }

//logger.log(Level.TRACE, "OPNB p:%02X a:%02X D:%02X".formatted(dPort, dAddr, dData));

        if ((model == EnmModel.RealModel && ctYM2610[chipId].getUseReal()[0]) || (model == EnmModel.VirtualModel && !ctYM2610[chipId].getUseReal()[0])) {
            //fmRegisterYM2610[dPort][dAddr] = dData;
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6 /* && (dData & 0xf0) > 0 */) {
                    if (ch != 2 || (fmRegisterYM2610[chipId][0][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            fmKeyOnYM2610[chipId][ch] = (dData & 0xf0) | 1;
                            fmVolYM2610[chipId][ch] = 256 * 6;
                        } else {
                            fmKeyOnYM2610[chipId][ch] &= 0xfe;
                        }
                    } else {
                        fmKeyOnYM2610[chipId][2] = dData & 0xf0;
                        if ((dData & 0x10) > 0) fmCh3SlotVolYM2610[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0) fmCh3SlotVolYM2610[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0) fmCh3SlotVolYM2610[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0) fmCh3SlotVolYM2610[chipId][3] = 256 * 6;
                    }
                }
            }

            // ADPCM B KEYON
            if (dPort == 0 && dAddr == 0x10) {
                if ((dData & 0x80) != 0) {
                    int p = (fmRegisterYM2610[chipId][0][0x11] & 0xc0) >> 6;
                    p = p == 0 ? 3 : p;
                    if (fmVolYM2610AdpcmPan[chipId] != p)
                        fmVolYM2610AdpcmPan[chipId] = p;

                    fmVolYM2610Adpcm[chipId][0] = ((fmVolYM2610AdpcmPan[chipId] & 0x02) != 0 ? 1 : 0);
                    fmVolYM2610Adpcm[chipId][1] = ((fmVolYM2610AdpcmPan[chipId] & 0x01) != 0 ? 1 : 0);
                } else {
                    fmVolYM2610Adpcm[chipId][0] = 0;
                    fmVolYM2610Adpcm[chipId][1] = 0;
                }
            }

            // ADPCM a KEYON
            if (dPort == 1 && dAddr == 0x00) {
                if ((dData & 0x80) == 0) {
                    int tl = fmRegisterYM2610[chipId][1][0x01] & 0x3f;
                    for (int i = 0; i < 6; i++) {
                        if ((dData & (0x1 << i)) != 0) {
                            //int il = fmRegisterYM2610[chipId][1][0x08 + i] & 0x1f;
                            int pan = ((fmRegisterYM2610[chipId][1][0x08 + i] & 0xc0) >> 6) * (((dData & 0x80) == 0) ? 1 : 0);
                            //fmVolYM2610Rhythm[chipId][i][0] = (int)(256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 2) > 0 ? 1 : 0);
                            //fmVolYM2610Rhythm[chipId][i][1] = (int)(256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 1) > 0 ? 1 : 0);
                            fmVolYM2610Rhythm[chipId][i][0] = ((pan & 2) > 0 ? 1 : 0);
                            fmVolYM2610Rhythm[chipId][i][1] = ((pan & 1) > 0 ? 1 : 0);
                        } else {
                            fmVolYM2610Rhythm[chipId][i][0] = 0;
                            fmVolYM2610Rhythm[chipId][i][1] = 0;
                        }
                    }
                }
            }
        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = fmRegisterYM2610[chipId][dPort][0xb0 + ch] & 0x07; // AL
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) > 0) {
                    dData = Math.min(dData + nowYM2610FadeoutVol[chipId], 127);
                    dData = maskFMChYM2610[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) { // AL
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && maskFMChYM2610[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2610Register(
                                chipId
                                , dPort
                                , 0x40 + ch + tslot
                                , fmRegisterYM2610[chipId][dPort][0x40 + ch + tslot]
                                , model);
                    }
                }
            }
        }

        // ssg mixer
        if (dPort == 0 && dAddr == 0x07) {
            int maskData = 0;
            if (maskFMChYM2610[chipId][6]) maskData |= 0x9 << 0;
            if (maskFMChYM2610[chipId][7]) maskData |= 0x9 << 1;
            if (maskFMChYM2610[chipId][8]) maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // ssg level
        if (dPort == 0 && (dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowYM2610FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2610[chipId][dAddr - 0x08 + 6] ? 0 : dData;
        }

        // rhythm level
        if (dPort == 1 && dAddr == 0x01) {
            int d = nowYM2610FadeoutVol[chipId] >> 1;
            dData = Math.max(dData - d, 0);
            //dData = maskFMChYM2610[chipId][12] ? 0 : dData;
        }

        // Rhythm
        if (dPort == 1 && dAddr == 0x00) {
            if (maskFMChYM2610[chipId][12]) {
                dData = 0xbf;
            }
        }

        // adpcm level
        if (dPort == 0 && dAddr == 0x1b) {
            int d = nowYM2610FadeoutVol[chipId] * 2;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2610[chipId][13] ? 0 : dData;
        }

        // adpcm start
        if (dPort == 0 && dAddr == 0x10) {
            if ((dData & 0x80) != 0 && maskFMChYM2610[chipId][13]) {
                dData &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if ((ctYM2610[chipId].getUseReal().length > 0 && !ctYM2610[chipId].getUseReal()[0])
                    && (ctYM2610[chipId].getUseReal().length < 2 || (ctYM2610[chipId].getUseReal().length > 1 && !ctYM2610[chipId].getUseReal()[1]))
            ) {
                context.mds.write(Ym2610Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYM2610[chipId] != null) scYM2610[chipId].setRegister(dPort * 0x100 + dAddr, dData);
            if (scYM2610EA[chipId] != null) {
                int dReg = (dPort << 8) | dAddr;
                boolean bSend = true;
                // Mask the register and send
                if (dReg >= 0x100 && dReg <= 0x12d) {
                    // ADPCM-a
                    bSend = false;
                } else if (dReg >= 0x010 && dReg <= 0x01c) {
                    // ADPCM-B
                    bSend = false;
                }
                if (bSend) {
                    scYM2610EA[chipId].setRegister((dPort << 8) | dAddr, dData);
                }
            }
            if (scYM2610EB[chipId] != null) {
                scYM2610EB[chipId].setRegister((dPort << 8) | dAddr | 0x10000, dData);
            }
        }
    }

    public void writeYm2610_SetAdpcmA(int chipId, byte[] ym2610AdpcmA, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            context.mds.writeYm2610SetAdpcmA(chipId, ym2610AdpcmA);
        } else {
            if (scYM2610[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x01);
                // Data Transfer
                for (byte b : ym2610AdpcmA) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, b & 0xff);
                }

                context.realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x01);
                // Data Transfer
                for (byte b : ym2610AdpcmA) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, b & 0xff);
                }

                context.realChip.SendData();
            }
        }
    }

    public void writeYm2610_SetAdpcmA(int chipId, EnmModel model, int startAddr, int length, byte[] buf, int srcStartAddr) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2610[chipId] != null) {
                int dPort = 2;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x01);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, buf[srcStartAddr + cnt] & 0xff);
                }

                context.realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                int dPort = 2;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x01);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, buf[srcStartAddr + cnt] & 0xff);
                }

                context.realChip.SendData();
            }
        }
    }

    public void WriteYM2610_SetAdpcmB(int chipId, byte[] ym2610AdpcmB, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            context.mds.writeYm2610SetAdpcmB(chipId, ym2610AdpcmB);
        } else {
            if (scYM2610[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x00);
                // Data Transfer
                for (byte b : ym2610AdpcmB) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, b & 0xff);
                }

                context.realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x00);
                // Data Transfer
                for (byte b : ym2610AdpcmB) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, b & 0xff);
                }

                context.realChip.SendData();
            }
        }
    }

    public void WriteYM2610_SetAdpcmB(int chipId, EnmModel model, int startAddr, int length, byte[] buf, int srcStartAddr) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2610[chipId] != null) {
                int dPort = 2;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x00);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, buf[srcStartAddr + cnt] & 0xff);
                }

                context.realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                int dPort = 2;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x00);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, buf[srcStartAddr + cnt] & 0xff);
                }

                context.realChip.SendData();
            }
        }
    }

    public void setMaskYM2610(int chipId, int ch, boolean mask) {
        maskFMChYM2610[chipId][ch] = mask;
        if (ch >= 9 && ch < 12) {
            maskFMChYM2610[chipId][2] = mask;
            maskFMChYM2610[chipId][9] = mask;
            maskFMChYM2610[chipId][10] = mask;
            maskFMChYM2610[chipId][11] = mask;
        }

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        if (ch < 6) {
            setYM2610Register(chipId, p, 0x40 + c, fmRegisterYM2610[chipId][p][0x40 + c], EnmModel.VirtualModel);
            setYM2610Register(chipId, p, 0x44 + c, fmRegisterYM2610[chipId][p][0x44 + c], EnmModel.VirtualModel);
            setYM2610Register(chipId, p, 0x48 + c, fmRegisterYM2610[chipId][p][0x48 + c], EnmModel.VirtualModel);
            setYM2610Register(chipId, p, 0x4c + c, fmRegisterYM2610[chipId][p][0x4c + c], EnmModel.VirtualModel);

            setYM2610Register(chipId, p, 0x40 + c, fmRegisterYM2610[chipId][p][0x40 + c], EnmModel.RealModel);
            setYM2610Register(chipId, p, 0x44 + c, fmRegisterYM2610[chipId][p][0x44 + c], EnmModel.RealModel);
            setYM2610Register(chipId, p, 0x48 + c, fmRegisterYM2610[chipId][p][0x48 + c], EnmModel.RealModel);
            setYM2610Register(chipId, p, 0x4c + c, fmRegisterYM2610[chipId][p][0x4c + c], EnmModel.RealModel);
        } else if (ch < 9) {
            setYM2610Register(chipId,
                    0,
                    0x08 + ch - 6,
                    fmRegisterYM2610[chipId][0][0x08 + ch - 6],
                    EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x08 + ch - 6, fmRegisterYM2610[chipId][0][0x08 + ch - 6], EnmModel.RealModel);
        } else if (ch < 12) {
            setYM2610Register(chipId, 0, 0x40 + 2, fmRegisterYM2610[chipId][0][0x40 + 2], EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x44 + 2, fmRegisterYM2610[chipId][0][0x44 + 2], EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x48 + 2, fmRegisterYM2610[chipId][0][0x48 + 2], EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x4c + 2, fmRegisterYM2610[chipId][0][0x4c + 2], EnmModel.VirtualModel);

            setYM2610Register(chipId, 0, 0x40 + 2, fmRegisterYM2610[chipId][0][0x40 + 2], EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x44 + 2, fmRegisterYM2610[chipId][0][0x44 + 2], EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x48 + 2, fmRegisterYM2610[chipId][0][0x48 + 2], EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x4c + 2, fmRegisterYM2610[chipId][0][0x4c + 2], EnmModel.RealModel);
        } else if (ch == 12) {
            if (maskFMChYM2610[chipId][12]) {
                setYM2610Register(chipId, 1, 0x00, 1, EnmModel.VirtualModel);
                setYM2610Register(chipId, 1, 0x00, 1, EnmModel.RealModel);
            }
        }
    }

    public void setFadeoutVolYM2610(int chipId, int v) {
        nowYM2610FadeoutVol[chipId] = v;
        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                setYM2610Register(chipId, p, 0x40 + c, fmRegisterYM2610[chipId][p][0x40 + c], EnmModel.RealModel);
                setYM2610Register(chipId, p, 0x44 + c, fmRegisterYM2610[chipId][p][0x44 + c], EnmModel.RealModel);
                setYM2610Register(chipId, p, 0x48 + c, fmRegisterYM2610[chipId][p][0x48 + c], EnmModel.RealModel);
                setYM2610Register(chipId, p, 0x4c + c, fmRegisterYM2610[chipId][p][0x4c + c], EnmModel.RealModel);
            }
        }

        // ssg
        setYM2610Register(chipId, 0, 0x08, fmRegisterYM2610[chipId][0][0x08], EnmModel.RealModel);
        setYM2610Register(chipId, 0, 0x09, fmRegisterYM2610[chipId][0][0x09], EnmModel.RealModel);
        setYM2610Register(chipId, 0, 0x0a, fmRegisterYM2610[chipId][0][0x0a], EnmModel.RealModel);

        // rhythm
        setYM2610Register(chipId, 0, 0x11, fmRegisterYM2610[chipId][0][0x11], EnmModel.RealModel);

        // adpcm
        setYM2610Register(chipId, 1, 0x0b, fmRegisterYM2610[chipId][1][0x0b], EnmModel.RealModel);
    }

    public int[] getYM2610Volume(int chipId) {
        return fmVolYM2610[chipId];
    }

    public int[][] getYM2610RhythmVolume(int chipId) {
        return fmVolYM2610Rhythm[chipId];
    }

    public int[] getYM2610AdpcmVolume(int chipId) {
        return fmVolYM2610Adpcm[chipId];
    }

    public int[] getYM2610Ch3SlotVolume(int chipId) {
        // if (ctYM2612.UseScci) {
        return fmCh3SlotVolYM2610[chipId];
        // }
        // return mds.readFMCh3SlotVolume();
    }
}
