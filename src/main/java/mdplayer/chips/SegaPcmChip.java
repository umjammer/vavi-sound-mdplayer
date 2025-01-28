/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.Instrument;
import mdsound.chips.SegaPcm;
import mdsound.instrument.SegaPcmInst;


/**
 * SegaPcmChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class SegaPcmChip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getSEGAPCMType();

    private final RSoundChip[] realChips = {null, null};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,}
    };

    public final byte[][] register = {
            null, null
    };

    public final boolean[][] keyOn = {
            null, null
    };

    private Audio context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {SegaPcmInst.class};
    }

    @Override
    public void init(Audio context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new byte[0x200];
            keyOn[chipId] = new boolean[16];
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    public void write(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriSPCM", 2);
        else
            context.chipLED.put("SecSPCM", 2);

        if ((model == EnmModel.VirtualModel && (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (realChips != null && realChips[chipId] != null))) {
            register[chipId][offset & 0x1ff] = (byte) data;

            if ((offset & 0x87) == 0x86) {
                int ch = (offset >> 3) & 0xf;
                if ((data & 0x01) == 0)
                    keyOn[chipId][ch] = true;
                data = mask[chipId][ch] ? data | 0x01 : data;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0])
                context.mds.write(inst(chipId), chipId, 0, offset, data);
//logger.log(Level.TRACE, "chipId=%d offset=%x data=%x ".formatted(chipId, offset, data));
        } else {
            if (realChips != null && realChips[chipId] != null)
                realChips[chipId].setRegister(offset, data);
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
            context.chipLED.put("PriSPCM", 2);
        else
            context.chipLED.put("SecSPCM", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(SegaPcmInst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
        } else {
            if (realChips != null && realChips[chipId] != null) {
                // Start address setting
                realChips[chipId].setRegister(0x10000, dataStart);
                realChips[chipId].setRegister(0x10001, dataStart >> 8);
                realChips[chipId].setRegister(0x10002, dataStart >> 16);
                // Data Transfer
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    realChips[chipId].setRegister(0x10004, romData[srcStartAdr + cnt] & 0xff);
                }
                realChips[chipId].setRegister(0x10006, romSize);

                context.chipRegister.plugin(RealChipPlugin.class).realChip.SendData();
            }
        }
    }

    public void writeClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                realChips[chipId].setRegister(0x10005, clock);
            }
        }
    }

    public byte[] read(int chipId) {
        return register[chipId];
    }

    public boolean[] getKeyOn(int chipId) {
        return keyOn[chipId];
    }

    public SegaPcm getChip(int chipId) {
        return context.mds.inst(SegaPcmInst.class).getChip(chipId);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
