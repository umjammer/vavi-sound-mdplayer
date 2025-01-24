/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.YmF278BInst;


/**
 * YmF278BChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF278BChip implements Chip {

    private final Setting.ChipType2[] chipTypes = new Setting.ChipType2[] {
            setting.getYMF278BType()[0], setting.getYMF278BType()[1]
    };
    private final RSoundChip[] realChips = {null, null};

    public int[][][] register = {
            {null, null},
            {null, null}
    };

    private final int[] registerFm = {0, 0};

    private final int[][] registerPcm = {new int[24], new int[24]};

    private final int[] registerRhythmB = {0, 0};

    private final int[] registerRhythm = {
            0, 0
    };

    private static final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    static final byte[] channel = {
            0, 3, 1, 4, 2, 5, 6, 7, 8, 9, 12, 10, 13, 11, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                register[chipId][0][i] = 0;
                register[chipId][1][i] = 0;
                register[chipId][2][i] = 0;
            }
            registerRhythm[0] = 0;
            registerRhythm[1] = 0;
            registerRhythmB[0] = 0;
            registerRhythmB[1] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public int getRhythmKeyOn(int chipId) {
        return registerRhythm[chipId];
    }

    public void resetRhythmKeyOn(int chipId) {
        registerRhythm[chipId] = 0;
    }

    public int[] getPcmKeyOn(int chipId) {
        return registerPcm[chipId];
    }

    public void resetPcmKeyOn(int chipId) {
        for (int i = 0; i < 24; i++)
            registerPcm[chipId][i] = 0;
    }

    public int getFmKeyOn(int chipId) {
        return registerFm[chipId];
    }

    public void resetFmKeyOn(int chipId) {
        registerFm[chipId] = 0;
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL4", 2);
        else
            context.chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel) {
            register[chipId][port][addr] = data;

//             if (port == 2) {
//                 logger.log(Level.TRACE, "p=2:adr%02x dat%02x".formatted(addr, data));
//             }

            if (addr >= 0xb0 && addr <= 0xb8) {
                int ch = addr - 0xb0 + port * 9;
                int k = (data >> 5) & 1;
                if (k == 0) {
                    registerFm[chipId] &= ~(1 << ch);
                } else {
                    registerFm[chipId] |= (1 << ch);
                }
                registerFm[chipId] &= 0x3_ffff;
                if (mask[chipId][ch])
                    data &= 0x1f;
            }

            if (addr == 0xbd && port == 0) {
                if ((registerRhythmB[chipId] & 0x10) == 0 && (data & 0x10) != 0)
                    registerRhythm[chipId] |= 0x10;
                if ((registerRhythmB[chipId] & 0x08) == 0 && (data & 0x08) != 0)
                    registerRhythm[chipId] |= 0x08;
                if ((registerRhythmB[chipId] & 0x04) == 0 && (data & 0x04) != 0)
                    registerRhythm[chipId] |= 0x04;
                if ((registerRhythmB[chipId] & 0x02) == 0 && (data & 0x02) != 0)
                    registerRhythm[chipId] |= 0x02;
                if ((registerRhythmB[chipId] & 0x01) == 0 && (data & 0x01) != 0)
                    registerRhythm[chipId] |= 0x01;
                registerRhythmB[chipId] = data;

                if (mask[chipId][18])
                    data &= 0xef;
                if (mask[chipId][19])
                    data &= 0xf7;
                if (mask[chipId][20])
                    data &= 0xfb;
                if (mask[chipId][21])
                    data &= 0xfd;
                if (mask[chipId][22])
                    data &= 0xfe;
            }

            if (port == 2 && (addr >= 0x68 && addr <= 0x7f)) {
                int k = data >> 7;
                if (k == 0) {
                    registerPcm[chipId][addr - 0x68] = 2;
                } else {
                    registerPcm[chipId][addr - 0x68] = 1;
                }
                if (mask[chipId][addr - 0x68 + 23])
                    data &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(YmF278BInst.class, chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        YmF278BChip.mask[chipId][channel[ch]] = mask;
    }

    public void writePcm(int chipId,
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
            context.mds.inst(YmF278BInst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeRam(int chipId,
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
            context.mds.inst(YmF278BInst.class).writeRam(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[][] read(int chipId) {
        return register[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
