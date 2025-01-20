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
import mdsound.instrument.YmF278bInst;

import static java.lang.System.getLogger;


/**
 * YmF278BChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF278BChip implements Chip {

    private static final Logger logger = getLogger(YmF278BChip.class.getName());

    private final Setting.ChipType2[] ctYMF278B = new Setting.ChipType2[] {
            setting.getYMF278BType()[0], setting.getYMF278BType()[1]
    };
    private final RSoundChip[] scYMF278B = {null, null};

    public int[][][] fmRegisterYMF278B = {
            {null, null},
            {null, null}
    };

    private final int[] fmRegisterYMF278BFM = {0, 0};

    private final int[][] fmRegisterYMF278BPCM = {new int[24], new int[24]};

    private final int[] fmRegisterYMF278BRhythmB = {0, 0};

    private final int[] fmRegisterYMF278BRhythm = {
            0, 0
    };

    private static final boolean[][] maskFMChYMF278B = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    static final byte[] YMF278BCh = {
            0, 3, 1, 4, 2, 5, 6, 7, 8, 9, 12, 10, 13, 11, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {

            fmRegisterYMF278B[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF278B[chipId][0][i] = 0;
                fmRegisterYMF278B[chipId][1][i] = 0;
                fmRegisterYMF278B[chipId][2][i] = 0;
            }
            fmRegisterYMF278BRhythm[0] = 0;
            fmRegisterYMF278BRhythm[1] = 0;
            fmRegisterYMF278BRhythmB[0] = 0;
            fmRegisterYMF278BRhythmB[1] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public int getYMF278BRyhthmKeyON(int chipId) {
        return fmRegisterYMF278BRhythm[chipId];
    }

    public void resetYMF278BRyhthmKeyON(int chipId) {
        fmRegisterYMF278BRhythm[chipId] = 0;
    }

    public int[] getYMF278BPCMKeyON(int chipId) {
        return fmRegisterYMF278BPCM[chipId];
    }

    public void resetYMF278BPCMKeyON(int chipId) {
        for (int i = 0; i < 24; i++)
            fmRegisterYMF278BPCM[chipId][i] = 0;
    }

    public int getYMF278BFMKeyON(int chipId) {
        return fmRegisterYMF278BFM[chipId];
    }

    public void resetYMF278BFMKeyON(int chipId) {
        fmRegisterYMF278BFM[chipId] = 0;
    }

    public void setYMF278BRegister(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL4", 2);
        else
            context.chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel) {
            fmRegisterYMF278B[chipId][dPort][dAddr] = dData;

//             if (dPort == 2) {
//                 logger.log(Level.TRACE, "p=2:adr%02x dat%02x".formatted(dAddr, dData));
//             }

            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0 + dPort * 9;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    fmRegisterYMF278BFM[chipId] &= ~(1 << ch);
                } else {
                    fmRegisterYMF278BFM[chipId] |= (1 << ch);
                }
                fmRegisterYMF278BFM[chipId] &= 0x3_ffff;
                if (maskFMChYMF278B[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd && dPort == 0) {
                if ((fmRegisterYMF278BRhythmB[chipId] & 0x10) == 0 && (dData & 0x10) != 0)
                    fmRegisterYMF278BRhythm[chipId] |= 0x10;
                if ((fmRegisterYMF278BRhythmB[chipId] & 0x08) == 0 && (dData & 0x08) != 0)
                    fmRegisterYMF278BRhythm[chipId] |= 0x08;
                if ((fmRegisterYMF278BRhythmB[chipId] & 0x04) == 0 && (dData & 0x04) != 0)
                    fmRegisterYMF278BRhythm[chipId] |= 0x04;
                if ((fmRegisterYMF278BRhythmB[chipId] & 0x02) == 0 && (dData & 0x02) != 0)
                    fmRegisterYMF278BRhythm[chipId] |= 0x02;
                if ((fmRegisterYMF278BRhythmB[chipId] & 0x01) == 0 && (dData & 0x01) != 0)
                    fmRegisterYMF278BRhythm[chipId] |= 0x01;
                fmRegisterYMF278BRhythmB[chipId] = dData;

                if (maskFMChYMF278B[chipId][18])
                    dData &= 0xef;
                if (maskFMChYMF278B[chipId][19])
                    dData &= 0xf7;
                if (maskFMChYMF278B[chipId][20])
                    dData &= 0xfb;
                if (maskFMChYMF278B[chipId][21])
                    dData &= 0xfd;
                if (maskFMChYMF278B[chipId][22])
                    dData &= 0xfe;
            }

            if (dPort == 2 && (dAddr >= 0x68 && dAddr <= 0x7f)) {
                int k = dData >> 7;
                if (k == 0) {
                    fmRegisterYMF278BPCM[chipId][dAddr - 0x68] = 2;
                } else {
                    fmRegisterYMF278BPCM[chipId][dAddr - 0x68] = 1;
                }
                if (maskFMChYMF278B[chipId][dAddr - 0x68 + 23])
                    dData &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYMF278B[chipId].getUseReal()[0]) {
                context.mds.write(YmF278bInst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF278B[chipId] == null)
                return;
            scYMF278B[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void setMaskYMF278B(int chipId, int ch, boolean mask) {
        maskFMChYMF278B[chipId][YMF278BCh[ch]] = mask;
    }

    public void writeYmF278BPCMData(int chipId,
                                    int romSize,
                                    int dataStart,
                                    int dataLength,
                                    byte[] romData,
                                    int srcStartAdr,
                                    EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL4", 2);
        else
            context.chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteYmF278bPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeYmF278BPCMRAMData(int chipId,
                                       int romSize,
                                       int dataStart,
                                       int dataLength,
                                       byte[] romData,
                                       int srcStartAdr,
                                       EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL4", 2);
        else
            context.chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteYmF278bPCMramData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[][] getYMF278BRegister(int chipId) {
        return fmRegisterYMF278B[chipId];
    }

//    public int getYMF278BFMKeyON(int chipId) {
//        return getYMF278BFMKeyON(chipId);
//    }
//
//    public void resetYMF278BFMKeyON(int chipId) {
//        resetYMF278BFMKeyON(chipId);
//    }
//
//    public int getYMF278BRyhthmKeyON(int chipId) {
//        return getYMF278BRyhthmKeyON(chipId);
//    }
//
//    public void resetYMF278BRyhthmKeyON(int chipId) {
//        resetYMF278BRyhthmKeyON(chipId);
//    }
//
//    public int[] getYMF278BPCMKeyON(int chipId) {
//        return getYMF278BPCMKeyON(chipId);
//    }
//
//    public void resetYMF278BPCMKeyON(int chipId) {
//        resetYMF278BPCMKeyON(chipId);
//    }

    public void setYMF278BMask(int chipId, int ch) {
        setMaskYMF278B(chipId, ch, true);
    }

    public void resetYMF278BMask(int chipId, int ch) {
        try {
            setMaskYMF278B(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
