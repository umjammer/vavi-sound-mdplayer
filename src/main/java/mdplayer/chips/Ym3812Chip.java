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
import mdsound.instrument.Ym3812Inst;


/**
 * Ym3812Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym3812Chip implements Chip {

    private final Setting.ChipType2[] ctYM3812 = new Setting.ChipType2[] {
            setting.getYM3812Type()[0], setting.getYM3812Type()[1]
    };

    private final RSoundChip[] scYM3812 = {null, null};

    public int[][] fmRegisterYM3812 = {null, null};

    private final int[] nowYM3812FadeoutVol = {0, 0};

    private final ChipKeyInfo[] kiYM3812 = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private final ChipKeyInfo[] kiYM3812ret = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private final boolean[][] maskFMChYM3812 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM3812[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM3812[chipId][i] = 0;
                fmRegisterYM3812[chipId][i] = 0;
            }

            nowYM3812FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setYM3812Register(int chipId, int dAddr, int dData, EnmModel model) {
        // if (ctYM3812 == null) return;

        if (chipId == 0)
            context.chipLED.put("PriOPL2", 2);
        else
            context.chipLED.put("SecOPL2", 2);

        fmRegisterYM3812[chipId][dAddr] = dData;

        if (dAddr >= 0x40 && dAddr <= 0x55) { // TL
            int ksl = dData & 0xc0;
            int tl = dData & 0x3f;
            int ch = dAddr - 0x40;
            boolean cr = false;
            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);

            // Career determination during 2op
            if (ch % 8 > 2)
                cr = true;
            else {
                int cnt = fmRegisterYM3812[chipId][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                if (cnt == 1)
                    cr = true;
            }

            if (ch >= 0x10 && (fmRegisterYM3812[chipId][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                dData = Math.min(tl + nowYM3812FadeoutVol[chipId], 0x3f);
                dData = ksl + (maskFMChYM3812[chipId][twoOpChannel] ? 0x3f : dData);
            }
        }

        // if (model == EnmModel.VirtualModel)
        {
            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    kiYM3812[chipId].off[ch] = true;
                } else {
                    if (kiYM3812[chipId].off[ch])
                        kiYM3812[chipId].on[ch] = true;
                    kiYM3812[chipId].off[ch] = false;
                }
                if (maskFMChYM3812[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((dData & (0x10 >> c)) == 0) {
                        kiYM3812[chipId].off[c + 9] = true;
                    } else {
                        if (kiYM3812[chipId].off[c + 9])
                            kiYM3812[chipId].on[c + 9] = true;
                        kiYM3812[chipId].off[c + 9] = false;
                    }
                }

                if (maskFMChYM3812[chipId][9])
                    dData &= 0xef;
                if (maskFMChYM3812[chipId][10])
                    dData &= 0xf7;
                if (maskFMChYM3812[chipId][11])
                    dData &= 0xfb;
                if (maskFMChYM3812[chipId][12])
                    dData &= 0xfd;
                if (maskFMChYM3812[chipId][13])
                    dData &= 0xfe;
            }
        }

        writeYm3812(chipId, dAddr, dData, model);
    }

    public ChipKeyInfo getYM3812KeyInfo(int chipId) {
        for (int ch = 0; ch < kiYM3812[chipId].off.length; ch++) {
            kiYM3812ret[chipId].off[ch] = kiYM3812[chipId].off[ch];
            kiYM3812ret[chipId].on[ch] = kiYM3812[chipId].on[ch];
            kiYM3812[chipId].on[ch] = false;
        }
        return kiYM3812ret[chipId];
    }

    private void writeYm3812(int chipId, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM3812[chipId].getUseReal()[0]) {
                context.mds.write(Ym3812Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM3812[chipId] == null)
                return;

            scYM3812[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM3812(int chipId, EnmModel model) {
        // FM All Channel Key Off
        for (int i = 0; i < 9; i++) {
            writeYm3812(chipId, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (int i = 0; i < 22; i++) {
            writeYm3812(chipId, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (int i = 0; i < 22; i++) {
            writeYm3812(chipId, 0x80 + i, 0xff, model);
        }
    }

    public void setMaskYM3812(int chipId, int ch, boolean mask) {
        maskFMChYM3812[chipId][ch] = mask;
    }

    public void setFadeoutVolYM3812(int chipId, int v) {
        nowYM3812FadeoutVol[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setYM3812Register(chipId, 0x40 + c, fmRegisterYM3812[chipId][0x40 + c], EnmModel.RealModel);
        }
    }

    public void writeYm3812Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM3812 != null && scYM3812[chipId] != null) {
                scYM3812[chipId].dClock = scYM3812[chipId].setMasterClock(clock);
            }
        }
    }
}
