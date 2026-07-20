/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.YmF278BInst;


/**
 * YmF278BChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF278BChip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getYMF278BType();

    private final RSoundChip[] realChips = {null, null};

    @Deprecated
    public final int[][][] register = {
            {null, null},
            {null, null}
    };

    @Deprecated
    private final int[] registerFm = {0, 0};

    @Deprecated
    private final int[][] registerPcm = {new int[24], new int[24]};

    @Deprecated
    private final int[] registerRhythmB = {0, 0};

    @Deprecated
    private final int[] registerRhythm = {
            0, 0
    };

    private final boolean[][] mask = {
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

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {YmF278BInst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);

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

    public void resetRhythmKeyOn(int chipId) {
        registerRhythm[chipId] = 0;
    }

    public void resetPcmKeyOn(int chipId) {
        for (int i = 0; i < 24; i++)
            registerPcm[chipId][i] = 0;
    }

    public void resetFmKeyOn(int chipId) {
        registerFm[chipId] = 0;
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

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
                context.mds.write(inst(chipId), chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    @Override
    protected void setMask(int chipId, int ch, boolean mask, Object... args) {
        this.mask[chipId][channel[ch]] = mask;
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(YmF278BInst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    public void writeRam(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(YmF278BInst.class).writeRam(chipId, romSize, offset, length, buf, srcOffset);

        dumpData(model, "PCMRAMData", srcOffset, buf, length);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        Instrument inst = context.mds.inst(inst(chipId));
        if (inst == null) return Collections.emptyMap();
        Map<String, Object> info = new HashMap<>();
        info.put("register", register[chipId]);
        info.put("rhythmKeyON", registerRhythm[chipId]);
        info.put("pcmKeyOn", registerPcm[chipId]);
        info.put("gmKeyOn", registerFm[chipId]);
        return info;
    }

    @Override
    public boolean getMask(int chipId, int ch) {
        return ch < mask[chipId].length && mask[chipId][ch];
    }
}
