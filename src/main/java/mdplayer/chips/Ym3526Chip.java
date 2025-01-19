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
import mdsound.instrument.Ym3526Inst;


/**
 * Ym3526Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym3526Chip implements Chip {

    private final Setting.ChipType2[] ctYM3526 = new Setting.ChipType2[] {
            setting.getYM3526Type()[0], setting.getYM3526Type()[1]
    };

    private final RSoundChip[] scYM3526 = {null, null};

    public int[][] fmRegisterYM3526 = {null, null};

    private final int[] nowYM3526FadeoutVol = {0, 0};

    private final ChipKeyInfo[] kiYM3526 = {
            new ChipKeyInfo(14), new ChipKeyInfo(14)
    };

    private final ChipKeyInfo[] kiYM3526ret = {
            new ChipKeyInfo(14), new ChipKeyInfo(14)
    };

    private final boolean[][] maskFMChYM3526 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM3526[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM3526[chipId][i] = 0;
                fmRegisterYM3526[chipId][i] = 0;
            }

            nowYM3526FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setYM3526Register(int chipId, int dAddr, int dData, EnmModel model) {
        // if (ctYM3526 == null) return;

        if (chipId == 0)
            context.chipLED.put("PriOPL", 2);
        else
            context.chipLED.put("SecOPL", 2);

        fmRegisterYM3526[chipId][dAddr] = dData;

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
                int cnt = fmRegisterYM3526[chipId][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                if (cnt == 1)
                    cr = true;
            }

            if (ch >= 0x10 && (fmRegisterYM3526[chipId][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                dData = Math.min(tl + nowYM3526FadeoutVol[chipId], 0x3f);
                dData = ksl + (maskFMChYM3526[chipId][twoOpChannel] ? 0x3f : dData);
            }
        }

        // if (model == EnmModel.VirtualModel)
        {
            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    kiYM3526[chipId].on[ch] = false;
                    kiYM3526[chipId].off[ch] = true;
                } else {
                    kiYM3526[chipId].on[ch] = true;
                }
                if (maskFMChYM3526[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((dData & (0x10 >> c)) == 0) {
                        kiYM3526[chipId].off[c + 9] = true;
                    } else {
                        if (kiYM3526[chipId].off[c + 9])
                            kiYM3526[chipId].on[c + 9] = true;
                        kiYM3526[chipId].off[c + 9] = false;
                    }
                }

                if (maskFMChYM3526[chipId][9])
                    dData &= 0xef;
                if (maskFMChYM3526[chipId][10])
                    dData &= 0xf7;
                if (maskFMChYM3526[chipId][11])
                    dData &= 0xfb;
                if (maskFMChYM3526[chipId][12])
                    dData &= 0xfd;
                if (maskFMChYM3526[chipId][13])
                    dData &= 0xfe;
            }
        }

        writeYm3526(chipId, dAddr, dData, model);
    }

    public ChipKeyInfo getYM3526KeyInfo(int chipId) {
        for (int ch = 0; ch < kiYM3526[chipId].off.length; ch++) {
            kiYM3526ret[chipId].off[ch] = kiYM3526[chipId].off[ch];
            kiYM3526ret[chipId].on[ch] = kiYM3526[chipId].on[ch];
            kiYM3526[chipId].on[ch] = false;
        }
        return kiYM3526ret[chipId];
    }

    private void writeYm3526(int chipId, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM3526[chipId].getUseReal()[0]) {
                context.mds.write(Ym3526Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM3526[chipId] == null)
                return;

            scYM3526[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM3526(int chipId, EnmModel model) {
        // FM All Channel Key Off
        for (int i = 0; i < 9; i++) {
            writeYm3526(chipId, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (int i = 0; i < 22; i++) {
            writeYm3526(chipId, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (int i = 0; i < 22; i++) {
            writeYm3526(chipId, 0x80 + i, 0xff, model);
        }
    }

    public void setMaskYM3526(int chipId, int ch, boolean mask) {
        maskFMChYM3526[chipId][ch] = mask;
    }

    public void setFadeoutVolYM3526(int chipId, int v) {
        nowYM3526FadeoutVol[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setYM3526Register(chipId, 0x40 + c, fmRegisterYM3526[chipId][0x40 + c], EnmModel.RealModel);
        }
    }

    public void writeYm3526Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM3526 != null && scYM3526[chipId] != null) {
//                if (scYM3526[chipId] instanceof RC86ctlSoundChip
//                        && ((RC86ctlSoundChip) scYM3526[chipId]).ChipType == Nc86ctl.ChipType.CHIP_OPL3) clock *= 4;
//                scYM3526[chipId].dClock = scYM3526[chipId].SetMasterClock((int) clock);
            }
        }
    }
}
