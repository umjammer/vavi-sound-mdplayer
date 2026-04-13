/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.MameYm3812Inst;
import mdsound.instrument.Ym3812Inst;


/**
 * Ym3812Chip.
 * <p>
 * system property
 * <li>{@code mdplayer.variant.ym3812} ... active chip index</li>
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym3812Chip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getYM3812Type();

    private final RSoundChip[] realChips = {null, null};

    public final int[][] register = {null, null};

    private final int[] fadeout = {0, 0};

    private final ChipKeyInfo[] keyInfo = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ym3812Inst.class, MameYm3812Inst.class};
    }

    @Override
    public int activeIndex(int chipId) {
        return chipTypes[chipId].getEnabledId();
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                register[chipId][i] = 0;
                register[chipId][i] = 0;
            }

            fadeout[chipId] = 0;
        }
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL2", 2);
        else
            context.chipLED.put("SecOPL2", 2);

        register[chipId][addr] = data;

        if (addr >= 0x40 && addr <= 0x55) { // TL
            int ksl = data & 0xc0;
            int tl = data & 0x3f;
            int ch = addr - 0x40;
            boolean cr = false;
            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);

            // Career determination during 2op
            if (ch % 8 > 2)
                cr = true;
            else {
                int cnt = register[chipId][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                if (cnt == 1)
                    cr = true;
            }

            if (ch >= 0x10 && (register[chipId][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                data = Math.min(tl + fadeout[chipId], 0x3f);
                data = ksl + (mask[chipId][twoOpChannel] ? 0x3f : data);
            }
        }

        // if (model == EnmModel.VirtualModel)
        {
            if (addr >= 0xb0 && addr <= 0xb8) {
                int ch = addr - 0xb0;
                int k = (data >> 5) & 1;
                if (k == 0) {
                    keyInfo[chipId].off[ch] = true;
                } else {
                    if (keyInfo[chipId].off[ch])
                        keyInfo[chipId].on[ch] = true;
                    keyInfo[chipId].off[ch] = false;
                }
                if (mask[chipId][ch])
                    data &= 0x1f;
            }

            if (addr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((data & (0x10 >> c)) == 0) {
                        keyInfo[chipId].off[c + 9] = true;
                    } else {
                        if (keyInfo[chipId].off[c + 9])
                            keyInfo[chipId].on[c + 9] = true;
                        keyInfo[chipId].off[c + 9] = false;
                    }
                }

                if (mask[chipId][9])
                    data &= 0xef;
                if (mask[chipId][10])
                    data &= 0xf7;
                if (mask[chipId][11])
                    data &= 0xfb;
                if (mask[chipId][12])
                    data &= 0xfd;
                if (mask[chipId][13])
                    data &= 0xfe;
            }
        }

        _write(chipId, addr, data, model);
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

    private void _write(int chipId, int addr, int data, EnmModel model) {
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

    public void softReset(int chipId, EnmModel model) {
        // FM All Channel Key Off
        for (int i = 0; i < 9; i++) {
            _write(chipId, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (int i = 0; i < 22; i++) {
            _write(chipId, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (int i = 0; i < 22; i++) {
            _write(chipId, 0x80 + i, 0xff, model);
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    public void setFadeout(int chipId, int v) {
        fadeout[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            write(chipId, 0x40 + c, register[chipId][0x40 + c], EnmModel.RealModel);
        }
    }

    public void writeClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                realChips[chipId].dClock = realChips[chipId].setMasterClock(clock);
            }
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
