/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.instruments.Vrc7Inst;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.Emu2413Inst;
import mdsound.instrument.Ym2413Inst;


/**
 * Ym2413Chip.
 * <p>
 * system property
 * <li>{@code mdplayer.variant.ym2413} ... active chip index</li>
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2413Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getYM2413Type();

    private final RSoundChip[] realChips = {null, null};

    public final int[][] register = {null, null};
    //    private final int[] registerRhythmB = {0, 0};
//    private final int[] registerRhythm = {0, 0};
    private final ChipKeyInfo[] keyInfo = {new ChipKeyInfo(14), new ChipKeyInfo(14)};
    private final int[] fadeout = {0, 0};
    private final boolean[] rm = {false, false};
    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    public int clock;

    private BasePlugin<? extends BaseDriver> context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ym2413Inst.class, Vrc7Inst.class, Emu2413Inst.class};
    }

    @Override
    public int activeIndex(int chipId) {
        return Integer.parseInt(System.getProperty("mdplayer.variant.ym2413", "0")); //!((Vgm) context.driverVirtual).ym2413VRC7Flag ? 0 : 1; // TODO setting
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[0x39];
            for (int i = 0; i < 0x39; i++) {
                register[chipId][i] = 0;
            }
//            registerRhythm[0] = 0;
//            registerRhythm[1] = 0;
//            registerRhythmB[0] = 0;
//            registerRhythmB[1] = 0;

            fadeout[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPLL", 2);
        else
            context.chipLED.put("SecOPLL", 2);

        if (model == EnmModel.VirtualModel)
            register[chipId][addr] = data;

        if (addr == 0x0e) {
            rm[chipId] = (data & 0x20) != 0;
        }

        if (addr >= 0x20 && addr <= 0x28) {
            int ch = addr - 0x20;
            int k = data & 0x10;
            if (k == 0) {
                keyInfo[chipId].off[ch] = true;
            } else {
                if (keyInfo[chipId].off[ch])
                    keyInfo[chipId].on[ch] = true;
                keyInfo[chipId].off[ch] = false;
            }

            // Apply mask
            if (mask[chipId][ch])
                data &= 0xef;
        }

        if (addr >= 0x30 && addr < 0x39) { // TL
            int inst = data & 0xf0;
            int tl = data & 0x0f;
            int ch = addr - 0x30;

            if (addr < 0x36 || !rm[chipId]) {
                data = Math.min(tl + fadeout[chipId], 0x0f);
                data = inst | data;
            } else {
                data = Math.min(tl + fadeout[chipId], 0x0f);
                if (addr > 0x36)
                    data = (data << 4) | data;
            }
        }

        if (addr == 0x0e) {
            for (int c = 0; c < 5; c++) {
                if ((data & (0x10 >> c)) == 0) {
                    keyInfo[chipId].off[c + 9] = true;
                } else {
                    if (keyInfo[chipId].off[c + 9])
                        keyInfo[chipId].on[c + 9] = true;
                    keyInfo[chipId].off[c + 9] = false;
                }
            }

            data = (data & 0x20) | (mask[chipId][9] ? 0 : (data & 0x10)) |
                    (mask[chipId][10] ? 0 : (data & 0x08)) | (mask[chipId][11] ? 0 : (data & 0x04)) |
                    (mask[chipId][12] ? 0 : (data & 0x02)) | (mask[chipId][13] ? 0 : (data & 0x01));
        }

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(inst(chipId), chipId, 0, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(addr, data);
        }
    }

    public ChipKeyInfo getKeyInfo(int chipId) {
        ChipKeyInfo[] keyInfoRet = {new ChipKeyInfo(14), new ChipKeyInfo(14)}; // TODO out for memory usage?
        for (int ch = 0; ch < keyInfo[chipId].off.length; ch++) {
            keyInfoRet[chipId].off[ch] = keyInfo[chipId].off[ch];
            keyInfoRet[chipId].on[ch] = keyInfo[chipId].on[ch];
            keyInfo[chipId].on[ch] = false;
        }
        return keyInfoRet[chipId];
    }

    public void softReset(int chipId, EnmModel model) {
        // FM All Channel Key Off
        for (int ch = 0; ch < 9; ch++) {
            write(chipId, 0x20 + ch, 0x00, model);
        }
        write(chipId, 0x0e, 0x00, model);

        // FM TL=15
        for (int ch = 0; ch < 9; ch++) {
            write(chipId, 0x30 + ch, 0x0f, model);
        }
        write(chipId, 0x36, 0x0f, model);
        write(chipId, 0x37, 0xff, model);
        write(chipId, 0x38, 0xff, model);
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;

        if (ch < 9) {
            write(chipId, 0x20 + ch, register[chipId][0x20 + ch], EnmModel.VirtualModel);
            write(chipId, 0x20 + ch, register[chipId][0x20 + ch], EnmModel.RealModel);
        } else if (ch < 14) {
            write(chipId, 0x0e, register[chipId][0x0e], EnmModel.VirtualModel);
            write(chipId, 0x0e, register[chipId][0x0e], EnmModel.RealModel);
        }
    }

    public void setFadeout(int chipId, int v) {
        fadeout[chipId] = v / (128 / 16);
        for (int c = 0; c < 9; c++) {
            write(chipId, 0x30 + c, register[chipId][0x30 + c], EnmModel.RealModel);
        }
    }

    public int[] read(int chipId) {
        return register[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    @Override
    public void softReset(EnmModel model) {
        softReset(0, model);
        softReset(1, model);
    }

    @Override
    public void clearFadeout() {
        setFadeout(0, 0);
        setFadeout(1, 0);
    }
}
