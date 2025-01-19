/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdsound.instrument.Y8950Inst;


/**
 * Y8950Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Y8950Chip implements Chip {

    private final Setting.ChipType2[] ctY8950 = new Setting.ChipType2[] {
            setting.getY8950Type()[0], setting.getY8950Type()[1]
    };

    public int[][] fmRegisterY8950 = {null, null};

    private final ChipKeyInfo[] kiY8950 = {new ChipKeyInfo(15), new ChipKeyInfo(15)};

    private final ChipKeyInfo[] kiY8950ret = {new ChipKeyInfo(15), new ChipKeyInfo(15)};

    private final boolean[][] maskFMChY8950 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterY8950[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterY8950[chipId][i] = 0;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public ChipKeyInfo getY8950KeyInfo(int chipId) {
        for (int ch = 0; ch < kiY8950[chipId].off.length; ch++) {
            kiY8950ret[chipId].off[ch] = kiY8950[chipId].off[ch];
            kiY8950ret[chipId].on[ch] = kiY8950[chipId].on[ch];
            kiY8950[chipId].on[ch] = false;
        }
        return kiY8950ret[chipId];
    }

    public void setY8950Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriY8950", 2);
        else
            context.chipLED.put("SecY8950", 2);

        if (model == EnmModel.VirtualModel) {
            fmRegisterY8950[chipId][dAddr] = dData;
            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    kiY8950[chipId].on[ch] = false;
                    kiY8950[chipId].off[ch] = true;
                } else {
                    kiY8950[chipId].on[ch] = true;
                }
                if (maskFMChY8950[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((dData & (0x10 >> c)) == 0) {
                        kiY8950[chipId].off[c + 9] = true;
                    } else {
                        if (kiY8950[chipId].off[c + 9])
                            kiY8950[chipId].on[c + 9] = true;
                        kiY8950[chipId].off[c + 9] = false;
                    }
                }

                if (maskFMChY8950[chipId][9])
                    dData &= 0xef;
                if (maskFMChY8950[chipId][10])
                    dData &= 0xf7;
                if (maskFMChY8950[chipId][11])
                    dData &= 0xfb;
                if (maskFMChY8950[chipId][12])
                    dData &= 0xfd;
                if (maskFMChY8950[chipId][13])
                    dData &= 0xfe;
            }

            // ADPCM
            if (dAddr == 0x07) {
                int k = (dData & 0x80);
                if (k == 0) {
                    kiY8950[chipId].on[14] = false;
                    kiY8950[chipId].off[14] = true;
                } else {
                    kiY8950[chipId].on[14] = true;
                    kiY8950[chipId].off[14] = false;
                }
                if (maskFMChY8950[chipId][14])
                    dData &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            // if (!ctY8950[chipId].UseScci)
            {
                context.mds.write(Y8950Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
        }
    }

    public void setMaskY8950(int chipId, int ch, boolean mask) {
        maskFMChY8950[chipId][ch] = mask;
    }

    public void writeY8950PCMData(int chipId,
                                  int romSize,
                                  int dataStart,
                                  int dataLength,
                                  byte[] romData,
                                  int srcStartAdr,
                                  EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriY8950", 2);
        else
            context.chipLED.put("SecY8950", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteY8950PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }
}
