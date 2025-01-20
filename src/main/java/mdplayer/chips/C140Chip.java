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
import mdsound.chips.C140;
import mdsound.instrument.C140Inst;

import static java.lang.System.getLogger;


/**
 * C140Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class C140Chip implements Chip {

    private static final Logger logger = getLogger(C140Chip.class.getName());

    private final Setting.ChipType2[] ctC140 = new Setting.ChipType2[] {
            setting.getC140Type()[0], setting.getC140Type()[1]
    };

    private final RSoundChip[] scC140 = {null, null};

    public byte[][] pcmRegisterC140 = {null, null};

    public boolean[][] pcmKeyOnC140 = {null, null};

    private static final boolean[][] maskChC140 = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            pcmRegisterC140[chipId] = new byte[0x200];
            pcmKeyOnC140[chipId] = new boolean[24];
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMaskC140(int chipId, int ch, boolean mask) {
        maskChC140[chipId][ch] = mask;
    }

    public void writeC140(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC140", 2);
        else
            context.chipLED.put("SecC140", 2);

        if ((model == EnmModel.VirtualModel && (ctC140[chipId] == null || !ctC140[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scC140 != null && scC140[chipId] != null))) {
            pcmRegisterC140[chipId][adr] = (byte) data;
            int ch = adr >> 4;
            switch (adr & 0xf) {
                case 0x05:
                    if ((data & 0x80) != 0) {
                        pcmKeyOnC140[chipId][ch] = true;
                        data = maskChC140[chipId][ch] ? data & 0x7f : data;
                    }
                    break;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (ctC140[chipId] == null || !ctC140[chipId].getUseReal()[0])
                context.mds.write(C140Inst.class, chipId, 0, adr, data);
        } else {
            if (scC140 != null && scC140[chipId] != null)
                scC140[chipId].setRegister(adr, data);
        }
    }

    public void writeC140PCMData(int chipId,
                                 int romSize,
                                 int dataStart,
                                 int dataLength,
                                 byte[] romData,
                                 int srcStartAdr,
                                 EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC140", 2);
        else
            context.chipLED.put("SecC140", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.writeC140PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
        else {
            if (scC140 != null && scC140[chipId] != null) {
                // Start address setting
                scC140[chipId].setRegister(0x10000, dataStart);
                scC140[chipId].setRegister(0x10001, dataStart >> 8);
                scC140[chipId].setRegister(0x10002, dataStart >> 16);
                // Data Transfer
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    scC140[chipId].setRegister(0x10004, romData[srcStartAdr + cnt]);
                }
                // scC140[chipId].setRegister(0x10006, (int)ROMSize);

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void writeC140Type(int chipId, C140.Type type, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scC140 != null && scC140[chipId] != null) {
                switch (type) {
                    case SYSTEM2:
                        scC140[chipId].setRegister(0x10008, 0);
                        break;
                    case SYSTEM21:
                        scC140[chipId].setRegister(0x10008, 1);
                        break;
                    case ASIC219:
                        scC140[chipId].setRegister(0x10008, 2);
                        break;
                }
            }
        }
    }

    public byte[] getC140Register(int chipId) {
        return pcmRegisterC140[chipId];
    }

    public boolean[] getC140KeyOn(int chipId) {
        return pcmKeyOnC140[chipId];
    }

    public void setC140Mask(int chipId, int ch) {
        setMaskC140(chipId, ch, true);
    }

    public void resetC140Mask(int chipId, int ch) {
        try {
            setMaskC140(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
