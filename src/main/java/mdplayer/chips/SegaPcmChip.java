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
import mdsound.instrument.SegaPcmInst;


/**
 * SegaPcmChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class SegaPcmChip implements Chip {

    private final Setting.ChipType2[] ctSEGAPCM = new Setting.ChipType2[] {
            setting.getSEGAPCMType()[0], setting.getSEGAPCMType()[1]
    };
    private final RSoundChip[] scSEGAPCM = {null, null};

    private final boolean[][] maskChSegaPCM = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,}
    };

    public byte[][] pcmRegisterSEGAPCM = {
            null, null
    };

    public boolean[][] pcmKeyOnSEGAPCM = {
            null, null
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            pcmRegisterSEGAPCM[chipId] = new byte[0x200];
            pcmKeyOnSEGAPCM[chipId] = new boolean[16];
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setMaskSegaPCM(int chipId, int ch, boolean mask) {
        maskChSegaPCM[chipId][ch] = mask;
    }

    public void writeSEGAPCM(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriSPCM", 2);
        else
            context.chipLED.put("SecSPCM", 2);

        if ((model == EnmModel.VirtualModel && (ctSEGAPCM[chipId] == null || !ctSEGAPCM[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scSEGAPCM != null && scSEGAPCM[chipId] != null))) {
            pcmRegisterSEGAPCM[chipId][offset & 0x1ff] = (byte) data;

            if ((offset & 0x87) == 0x86) {
                int ch = (offset >> 3) & 0xf;
                if ((data & 0x01) == 0)
                    pcmKeyOnSEGAPCM[chipId][ch] = true;
                data = maskChSegaPCM[chipId][ch] ? data | 0x01 : data;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctSEGAPCM[chipId].getUseReal()[0])
                context.mds.write(SegaPcmInst.class, chipId, 0, offset, data);
//logger.log(Level.TRACE, "chipId=%d offset=%x data=%x ".formatted(chipId, offset, data));
        } else {
            if (scSEGAPCM != null && scSEGAPCM[chipId] != null)
                scSEGAPCM[chipId].setRegister(offset, data);
        }
    }

    public void writeSEGAPCMPCMData(int chipId,
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
            context.mds.writeSegaPcmPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
        } else {
            if (scSEGAPCM != null && scSEGAPCM[chipId] != null) {
                // Start address setting
                scSEGAPCM[chipId].setRegister(0x10000, dataStart);
                scSEGAPCM[chipId].setRegister(0x10001, dataStart >> 8);
                scSEGAPCM[chipId].setRegister(0x10002, dataStart >> 16);
                // Data Transfer
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    scSEGAPCM[chipId].setRegister(0x10004, romData[srcStartAdr + cnt] & 0xff);
                }
                scSEGAPCM[chipId].setRegister(0x10006, romSize);

                context.realChip.SendData();
            }
        }
    }

    public void writeSEGAPCMClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scSEGAPCM != null && scSEGAPCM[chipId] != null) {
                scSEGAPCM[chipId].setRegister(0x10005, clock);
            }
        }
    }
}
