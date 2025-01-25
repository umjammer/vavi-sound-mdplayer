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
import mdsound.instrument.YmF262Inst;

import static mdplayer.chips.YmF278BChip.channel;


/**
 * YmF262Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF262Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getYMF262Type();

    private final RSoundChip[] realChips = {null, null};

    public final int[][][] register = {
            {null, null},
            {null, null}
    };

    private final int[] registerFm = {0, 0};

    private final int[] registerRhythmB = {0, 0};

    private final int[] registerRhythm = {0, 0};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false}
    };

    private final int[] fadeout = {0, 0};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                register[chipId][0][i] = 0;
                register[chipId][1][i] = 0;
            }

            fadeout[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public int getRhythmKeyON(int chipId) {
        int r = registerRhythm[chipId];
        registerRhythm[chipId] = 0;
        return r;
    }

    public int getFmKeyON(int chipId) {
        return registerFm[chipId];
    }

    public void setRegister(int chipId, int port, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPL3", 2);
        else
            context.chipLED.put("SecOPL3", 2);

        register[chipId][port][addr] = data;

        if (addr >= 0x40 && addr <= 0x55) { // TL
            int ksl = (data & 0xc0);
            int tl = (data & 0x3f);
            int ch = addr - 0x40;
            int conSel = register[chipId][1][4] & 0x3f;
            boolean cr = false;

            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);
            int fourOpChannel = twoOpChannel > 5 ? -1 : ((twoOpChannel % 3) + port * 3);
            boolean fourOpMode = fourOpChannel != -1 && ((conSel & (1 << fourOpChannel)) != 0);
            int slotNumber = ((ch % 8) / 3) + (twoOpChannel > 2 ? 2 : 0);
            twoOpChannel += port * 9;

            if (!fourOpMode) {
                // Career determination during 2op
                if (ch % 8 > 2)
                    cr = true;
                else {
                    int cnt = register[chipId][port][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                    if (cnt == 1)
                        cr = true;
                }
            } else {
                if (slotNumber == 3)
                    cr = true;
                else {
                    int cnt0 = register[chipId][port][0xc0 + (fourOpChannel % 3)] & 1;
                    int cnt1 = register[chipId][port][0xc3 + (fourOpChannel % 3)] & 1;
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

            if (ch >= 0x10 && port == 0 && (register[chipId][port][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                data = Math.min(tl + fadeout[chipId], 0x3f);
                data = ksl + (mask[chipId][twoOpChannel] ? 0x3f : data);
            }
        }

        if (addr >= 0xb0 && addr <= 0xb8) {
            int ch = addr - 0xb0 + port * 9;
            int k = (data >> 5) & 1;
            if (k == 0) {
                registerFm[chipId] &= ~(1 << ch);
            } else {
                registerFm[chipId] |= (1 << ch);
            }
            registerFm[chipId] &= 0x3ffff;
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

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(YmF262Inst.class, chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    private void _write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(YmF262Inst.class, chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;

            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    public void softReset(int chipId, EnmModel model) {
        int i;

        // FM All Channel Key Off
        for (i = 0; i < 9; i++) {
            _write(chipId, 0, 0xb0 + i, 0x00, model);
            _write(chipId, 1, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (i = 0; i < 22; i++) {
            _write(chipId, 0, 0x40 + i, 0x3f, model);
            _write(chipId, 1, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (i = 0; i < 22; i++) {
            _write(chipId, 0, 0x80 + i, 0xff, model);
            _write(chipId, 1, 0x80 + i, 0xff, model);
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][channel[ch]] = mask;
    }

    public void setFadeout(int chipId, int v) {
        fadeout[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setRegister(chipId, 0, 0x40 + c, register[chipId][0][0x40 + c], EnmModel.RealModel);
            setRegister(chipId, 1, 0x40 + c, register[chipId][1][0x40 + c], EnmModel.RealModel);
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

    public int[][] read(int chipId) {
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
