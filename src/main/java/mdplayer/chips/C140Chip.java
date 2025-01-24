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
import mdsound.chips.C140;
import mdsound.instrument.C140Inst;


/**
 * C140Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class C140Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = {
            setting.getC140Type()[0], setting.getC140Type()[1]
    };

    private final RSoundChip[] realChips = {null, null};

    public byte[][] pcmRegister = {null, null};

    public boolean[][] pcmKeyOn = {null, null};

    private static final boolean[][] mask = {
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
            pcmRegister[chipId] = new byte[0x200];
            pcmKeyOn[chipId] = new boolean[24];
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMask(int chipId, int ch, boolean mask) {
        C140Chip.mask[chipId][ch] = mask;
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC140", 2);
        else
            context.chipLED.put("SecC140", 2);

        if ((model == EnmModel.VirtualModel && (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (realChips != null && realChips[chipId] != null))) {
            pcmRegister[chipId][adr] = (byte) data;
            int ch = adr >> 4;
            switch (adr & 0xf) {
                case 0x05:
                    if ((data & 0x80) != 0) {
                        pcmKeyOn[chipId][ch] = true;
                        data = mask[chipId][ch] ? data & 0x7f : data;
                    }
                    break;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])
                context.mds.write(C140Inst.class, chipId, 0, adr, data);
        } else {
            if (realChips != null && realChips[chipId] != null)
                realChips[chipId].setRegister(adr, data);
        }
    }

    public void writePcm(int chipId,
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
            context.mds.inst(C140Inst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
        else {
            if (realChips != null && realChips[chipId] != null) {
                // Start address setting
                realChips[chipId].setRegister(0x10000, dataStart);
                realChips[chipId].setRegister(0x10001, dataStart >> 8);
                realChips[chipId].setRegister(0x10002, dataStart >> 16);
                // Data Transfer
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    realChips[chipId].setRegister(0x10004, romData[srcStartAdr + cnt]);
                }
//                realChips[chipId].setRegister(0x10006, romSize);

                context.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void writeType(int chipId, C140.Type type, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                switch (type) {
                    case SYSTEM2:
                        realChips[chipId].setRegister(0x10008, 0);
                        break;
                    case SYSTEM21:
                        realChips[chipId].setRegister(0x10008, 1);
                        break;
                    case ASIC219:
                        realChips[chipId].setRegister(0x10008, 2);
                        break;
                }
            }
        }
    }

    public byte[] read(int chipId) {
        return pcmRegister[chipId];
    }

    public boolean[] getKeyOn(int chipId) {
        return pcmKeyOn[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
