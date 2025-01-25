/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.Instrument.AdpcmEnabledInstrument;


/**
 * Ym2610Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2610Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getYM2610Type();

    private final Class<? extends AdpcmEnabledInstrument>[] inst = new Class[2];

    private final RSoundChip[] realChips = {null, null};
    private final RSoundChip[] realChipsEA = {null, null};
    private final RSoundChip[] realChipsEB = {null, null};

    public final int[][][] register = {
            {null, null},
            {null, null}
    };

    public final int[][] keyOn = {null, null};

    public final int[][] volume = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public final int[][] ch3SlotVolume = {new int[4], new int[4]};

    public final int[][][] rhythmVolume = {
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]},
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}
    };

    public final  int[][] adpcmVolume = {new int[2], new int[2]};

    public final int[] adpcmPan = {0, 0};

    private final int[] nowFadeoutVol = {0, 0};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                register[chipId][0][i] = 0; // -1;
                register[chipId][1][i] = 0; // -1;
            }
            register[chipId][0][0xb4] = 0xc0;
            register[chipId][0][0xb5] = 0xc0;
            register[chipId][0][0xb6] = 0xc0;
            register[chipId][1][0xb4] = 0xc0;
            register[chipId][1][0xb5] = 0xc0;
            register[chipId][1][0xb6] = 0xc0;
            keyOn[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            nowFadeoutVol[chipId] = 0;

            //
            inst[chipId] = (Class<? extends AdpcmEnabledInstrument>) EnmChip.YM2610.getInstClass(chipTypes[chipId].getEnabledId());
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    write(chipId, p, 0x40 + c, 127, EnmModel.RealModel);
                    write(chipId, p, 0x44 + c, 127, EnmModel.RealModel);
                    write(chipId, p, 0x48 + c, 127, EnmModel.RealModel);
                    write(chipId, p, 0x4c + c, 127, EnmModel.RealModel);
                }
            }

            // ssg
            write(chipId, 0, 0x08, 0, EnmModel.RealModel);
            write(chipId, 0, 0x09, 0, EnmModel.RealModel);
            write(chipId, 0, 0x0a, 0, EnmModel.RealModel);

            // rhythm
            write(chipId, 0, 0x11, 0, EnmModel.RealModel);

            // adpcm
            write(chipId, 1, 0x0b, 0, EnmModel.RealModel);
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 9; i++) {
                if (volume[chipId][i] > 0) {
                    volume[chipId][i] -= 50;
                    if (volume[chipId][i] < 0)
                        volume[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (ch3SlotVolume[chipId][i] > 0) {
                    ch3SlotVolume[chipId][i] -= 50;
                    if (ch3SlotVolume[chipId][i] < 0)
                        ch3SlotVolume[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 6; i++) {
                if (rhythmVolume[chipId][i][0] > 0) {
                    rhythmVolume[chipId][i][0] -= 50;
                    if (rhythmVolume[chipId][i][0] < 0)
                        rhythmVolume[chipId][i][0] = 0;
                }
                if (rhythmVolume[chipId][i][1] > 0) {
                    rhythmVolume[chipId][i][1] -= 50;
                    if (rhythmVolume[chipId][i][1] < 0)
                        rhythmVolume[chipId][i][1] = 0;
                }
            }

            if (adpcmVolume[chipId][0] > 0) {
                adpcmVolume[chipId][0] -= 50;
                if (adpcmVolume[chipId][0] < 0)
                    adpcmVolume[chipId][0] = 0;
            }
            if (adpcmVolume[chipId][1] > 0) {
                adpcmVolume[chipId][1] -= 50;
                if (adpcmVolume[chipId][1] < 0)
                    adpcmVolume[chipId][1] = 0;
            }
        }
    }

    public void write(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (dAddr < 0 || dData < 0) return;

        if (chipId == 0) context.chipLED.put("PriOPNB", 2);
        else context.chipLED.put("SecOPNB", 2);

        if ((model == EnmModel.VirtualModel && (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0]))
                || (model == EnmModel.RealModel && (realChips != null && realChips[chipId] != null))
        ) {
            if (dPort == 0 && (dAddr == 0x2d || dAddr == 0x2e || dAddr == 0x2f)) {
                register[chipId][0][0x2d] = dAddr - 0x2d;
            } else {
                register[chipId][dPort][dAddr] = dData;
            }
        }

//logger.log(Level.TRACE, "OPNB p:%02X a:%02X D:%02X".formatted(dPort, dAddr, dData));

        if ((model == EnmModel.RealModel && chipTypes[chipId].getUseReal()[0]) || (model == EnmModel.VirtualModel && !chipTypes[chipId].getUseReal()[0])) {
            //register[dPort][dAddr] = dData;
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6 /* && (dData & 0xf0) > 0 */) {
                    if (ch != 2 || (register[chipId][0][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            keyOn[chipId][ch] = (dData & 0xf0) | 1;
                            volume[chipId][ch] = 256 * 6;
                        } else {
                            keyOn[chipId][ch] &= 0xfe;
                        }
                    } else {
                        keyOn[chipId][2] = dData & 0xf0;
                        if ((dData & 0x10) > 0) ch3SlotVolume[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0) ch3SlotVolume[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0) ch3SlotVolume[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0) ch3SlotVolume[chipId][3] = 256 * 6;
                    }
                }
            }

            // ADPCM B KEYON
            if (dPort == 0 && dAddr == 0x10) {
                if ((dData & 0x80) != 0) {
                    int p = (register[chipId][0][0x11] & 0xc0) >> 6;
                    p = p == 0 ? 3 : p;
                    if (adpcmPan[chipId] != p)
                        adpcmPan[chipId] = p;

                    adpcmVolume[chipId][0] = ((adpcmPan[chipId] & 0x02) != 0 ? 1 : 0);
                    adpcmVolume[chipId][1] = ((adpcmPan[chipId] & 0x01) != 0 ? 1 : 0);
                } else {
                    adpcmVolume[chipId][0] = 0;
                    adpcmVolume[chipId][1] = 0;
                }
            }

            // ADPCM a KEYON
            if (dPort == 1 && dAddr == 0x00) {
                if ((dData & 0x80) == 0) {
                    int tl = register[chipId][1][0x01] & 0x3f;
                    for (int i = 0; i < 6; i++) {
                        if ((dData & (0x1 << i)) != 0) {
                            //int il = register[chipId][1][0x08 + i] & 0x1f;
                            int pan = ((register[chipId][1][0x08 + i] & 0xc0) >> 6) * (((dData & 0x80) == 0) ? 1 : 0);
                            //rhythmVolume[chipId][i][0] = (int)(256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 2) > 0 ? 1 : 0);
                            //rhythmVolume[chipId][i][1] = (int)(256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 1) > 0 ? 1 : 0);
                            rhythmVolume[chipId][i][0] = ((pan & 2) > 0 ? 1 : 0);
                            rhythmVolume[chipId][i][1] = ((pan & 1) > 0 ? 1 : 0);
                        } else {
                            rhythmVolume[chipId][i][0] = 0;
                            rhythmVolume[chipId][i][1] = 0;
                        }
                    }
                }
            }
        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = register[chipId][dPort][0xb0 + ch] & 0x07; // AL
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) > 0) {
                    dData = Math.min(dData + nowFadeoutVol[chipId], 127);
                    dData = mask[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) { // AL
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && mask[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        write(
                                chipId
                                , dPort
                                , 0x40 + ch + tslot
                                , register[chipId][dPort][0x40 + ch + tslot]
                                , model);
                    }
                }
            }
        }

        // ssg mixer
        if (dPort == 0 && dAddr == 0x07) {
            int maskData = 0;
            if (mask[chipId][6]) maskData |= 0x9 << 0;
            if (mask[chipId][7]) maskData |= 0x9 << 1;
            if (mask[chipId][8]) maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // ssg level
        if (dPort == 0 && (dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowFadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = mask[chipId][dAddr - 0x08 + 6] ? 0 : dData;
        }

        // rhythm level
        if (dPort == 1 && dAddr == 0x01) {
            int d = nowFadeoutVol[chipId] >> 1;
            dData = Math.max(dData - d, 0);
            //dData = mask[chipId][12] ? 0 : dData;
        }

        // Rhythm
        if (dPort == 1 && dAddr == 0x00) {
            if (mask[chipId][12]) {
                dData = 0xbf;
            }
        }

        // adpcm level
        if (dPort == 0 && dAddr == 0x1b) {
            int d = nowFadeoutVol[chipId] * 2;
            dData = Math.max(dData - d, 0);
            dData = mask[chipId][13] ? 0 : dData;
        }

        // adpcm start
        if (dPort == 0 && dAddr == 0x10) {
            if ((dData & 0x80) != 0 && mask[chipId][13]) {
                dData &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if ((chipTypes[chipId].getUseReal().length > 0 && !chipTypes[chipId].getUseReal()[0])
                    && (chipTypes[chipId].getUseReal().length < 2 || (chipTypes[chipId].getUseReal().length > 1 && !chipTypes[chipId].getUseReal()[1]))
            ) {
                context.mds.write(inst[chipId], chipId, dPort, dAddr, dData);
            }
        } else {
            if (realChips[chipId] != null) realChips[chipId].setRegister(dPort * 0x100 + dAddr, dData);
            if (realChipsEA[chipId] != null) {
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
                    realChipsEA[chipId].setRegister((dPort << 8) | dAddr, dData);
                }
            }
            if (realChipsEB[chipId] != null) {
                realChipsEB[chipId].setRegister((dPort << 8) | dAddr | 0x10000, dData);
            }
        }
    }

    public void writeAdpcmA(int chipId, byte[] adpcmA, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            context.mds.inst(inst[chipId]).writeAdpcmA(chipId, adpcmA);
        } else {
            if (realChips[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                realChips[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                realChips[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                realChips[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChips[chipId].setRegister((dPort << 8) | 0x03, 0x01);
                // Data Transfer
                for (byte b : adpcmA) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChips[chipId].setRegister((dPort << 8) | 0x04, b & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
            if (realChipsEB[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10003, 0x01);
                // Data Transfer
                for (byte b : adpcmA) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChipsEB[chipId].setRegister((dPort << 8) | 0x10004, b & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void writeAdpcmA(int chipId, EnmModel model, int startAddr, int length, byte[] buf, int srcStartAddr) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips[chipId] != null) {
                int dPort = 2;
                realChips[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                realChips[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                realChips[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChips[chipId].setRegister((dPort << 8) | 0x03, 0x01);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChips[chipId].setRegister((dPort << 8) | 0x04, buf[srcStartAddr + cnt] & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
            if (realChipsEB[chipId] != null) {
                int dPort = 2;
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10003, 0x01);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChipsEB[chipId].setRegister((dPort << 8) | 0x10004, buf[srcStartAddr + cnt] & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void writeAdpcmB(int chipId, byte[] adpcmB, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            context.mds.inst(inst[chipId]).writeAdpcmB(chipId, adpcmB);
        } else {
            if (realChips[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                realChips[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                realChips[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                realChips[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChips[chipId].setRegister((dPort << 8) | 0x03, 0x00);
                // Data Transfer
                for (byte b : adpcmB) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChips[chipId].setRegister((dPort << 8) | 0x04, b & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
            if (realChipsEB[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10003, 0x00);
                // Data Transfer
                for (byte b : adpcmB) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChipsEB[chipId].setRegister((dPort << 8) | 0x10004, b & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void writeAdpcmB(int chipId, EnmModel model, int startAddr, int length, byte[] buf, int srcStartAddr) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips[chipId] != null) {
                int dPort = 2;
                realChips[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                realChips[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                realChips[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChips[chipId].setRegister((dPort << 8) | 0x03, 0x00);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChips[chipId].setRegister((dPort << 8) | 0x04, buf[srcStartAddr + cnt] & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
            if (realChipsEB[chipId] != null) {
                int dPort = 2;
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                //pushReg(CMD_YM2610|0x02,0x03,0x01);
                realChipsEB[chipId].setRegister((dPort << 8) | 0x10003, 0x00);
                // Data Transfer
                for (int cnt = 0; cnt < length; cnt++) {
                    //pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    realChipsEB[chipId].setRegister((dPort << 8) | 0x10004, buf[srcStartAddr + cnt] & 0xff);
                }

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
        if (ch >= 9 && ch < 12) {
            this.mask[chipId][2] = mask;
            this.mask[chipId][9] = mask;
            this.mask[chipId][10] = mask;
            this.mask[chipId][11] = mask;
        }

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        if (ch < 6) {
            write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.VirtualModel);
            write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.VirtualModel);
            write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.VirtualModel);
            write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.VirtualModel);

            write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.RealModel);
            write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.RealModel);
            write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.RealModel);
            write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.RealModel);
        } else if (ch < 9) {
            write(chipId,
                    0,
                    0x08 + ch - 6,
                    register[chipId][0][0x08 + ch - 6],
                    EnmModel.VirtualModel);
            write(chipId, 0, 0x08 + ch - 6, register[chipId][0][0x08 + ch - 6], EnmModel.RealModel);
        } else if (ch < 12) {
            write(chipId, 0, 0x40 + 2, register[chipId][0][0x40 + 2], EnmModel.VirtualModel);
            write(chipId, 0, 0x44 + 2, register[chipId][0][0x44 + 2], EnmModel.VirtualModel);
            write(chipId, 0, 0x48 + 2, register[chipId][0][0x48 + 2], EnmModel.VirtualModel);
            write(chipId, 0, 0x4c + 2, register[chipId][0][0x4c + 2], EnmModel.VirtualModel);

            write(chipId, 0, 0x40 + 2, register[chipId][0][0x40 + 2], EnmModel.RealModel);
            write(chipId, 0, 0x44 + 2, register[chipId][0][0x44 + 2], EnmModel.RealModel);
            write(chipId, 0, 0x48 + 2, register[chipId][0][0x48 + 2], EnmModel.RealModel);
            write(chipId, 0, 0x4c + 2, register[chipId][0][0x4c + 2], EnmModel.RealModel);
        } else if (ch == 12) {
            if (this.mask[chipId][12]) {
                write(chipId, 1, 0x00, 1, EnmModel.VirtualModel);
                write(chipId, 1, 0x00, 1, EnmModel.RealModel);
            }
        }
    }

    public void setFadeout(int chipId, int v) {
        nowFadeoutVol[chipId] = v;
        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.RealModel);
                write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.RealModel);
                write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.RealModel);
                write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.RealModel);
            }
        }

        // ssg
        write(chipId, 0, 0x08, register[chipId][0][0x08], EnmModel.RealModel);
        write(chipId, 0, 0x09, register[chipId][0][0x09], EnmModel.RealModel);
        write(chipId, 0, 0x0a, register[chipId][0][0x0a], EnmModel.RealModel);

        // rhythm
        write(chipId, 0, 0x11, register[chipId][0][0x11], EnmModel.RealModel);

        // adpcm
        write(chipId, 1, 0x0b, register[chipId][1][0x0b], EnmModel.RealModel);
    }

    public int[] getVolume(int chipId) {
        return volume[chipId];
    }

    public int[][] getRhythmVolume(int chipId) {
        return rhythmVolume[chipId];
    }

    public int[] getAdpcmVolume(int chipId) {
        return adpcmVolume[chipId];
    }

    public int[] getCh3SlotVolume(int chipId) {
//        if (ctYM2612.UseScci) {
            return ch3SlotVolume[chipId];
//        }
//        return context.mds.inst(inst[chipId]).readFMCh3SlotVolume();
    }

    public int[][] read(int chipId) {
        return register[chipId];
    }

    public int[] getKeyOn(int chipId) {
        return keyOn[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
