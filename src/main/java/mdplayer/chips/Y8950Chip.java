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
import mdplayer.Setting;
import mdsound.instrument.Y8950Inst;

import static java.lang.System.getLogger;


/**
 * Y8950Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Y8950Chip implements Chip {

    private static final Logger logger = getLogger(Y8950Chip.class.getName());

    private final Setting.ChipType2[] chipTypes = new Setting.ChipType2[] {
            setting.getY8950Type()[0], setting.getY8950Type()[1]
    };

    public int[][] register = {null, null};

    private final ChipKeyInfo[] keyInfo = {new ChipKeyInfo(15), new ChipKeyInfo(15)};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                register[chipId][i] = 0;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public ChipKeyInfo getKeyInfo(int chipId) {
        ChipKeyInfo[] keyInfoRet = {new ChipKeyInfo(15), new ChipKeyInfo(15)}; // TODO out for memory usage?
        for (int ch = 0; ch < keyInfo[chipId].off.length; ch++) {
            keyInfoRet[chipId].off[ch] = keyInfo[chipId].off[ch];
            keyInfoRet[chipId].on[ch] = keyInfo[chipId].on[ch];
            keyInfo[chipId].on[ch] = false;
        }
        return keyInfoRet[chipId];
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriY8950", 2);
        else
            context.chipLED.put("SecY8950", 2);

        if (model == EnmModel.VirtualModel) {
            register[chipId][addr] = data;
            if (addr >= 0xb0 && addr <= 0xb8) {
                int ch = addr - 0xb0;
                int k = (data >> 5) & 1;
                if (k == 0) {
                    keyInfo[chipId].on[ch] = false;
                    keyInfo[chipId].off[ch] = true;
                } else {
                    keyInfo[chipId].on[ch] = true;
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

            // ADPCM
            if (addr == 0x07) {
                int k = (data & 0x80);
                if (k == 0) {
                    keyInfo[chipId].on[14] = false;
                    keyInfo[chipId].off[14] = true;
                } else {
                    keyInfo[chipId].on[14] = true;
                    keyInfo[chipId].off[14] = false;
                }
                if (mask[chipId][14])
                    data &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            // if (!chipTypes[chipId].UseScci)
            {
                context.mds.write(Y8950Inst.class, chipId, 0, addr, data);
            }
        } else {
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    public void writePcm(int chipId,
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
            context.mds.inst(Y8950Inst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public int[] read(int chipId) {
        return register[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        try {
            setMask(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
