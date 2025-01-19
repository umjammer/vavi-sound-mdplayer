/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.instrument.CtrQSoundInst;
import mdsound.instrument.QSoundInst;


/**
 * QSoundChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class QSoundChip implements Chip {

    private final boolean[][] maskChQSound = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setQSoundRegister(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriQsnd", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(CtrQSoundInst.class, chipId, 0, 0, mm);
            context.mds.write(CtrQSoundInst.class, chipId, 0, 1, ll);
            context.mds.write(CtrQSoundInst.class, chipId, 0, 2, rr);

            qSoundRegister[chipId][rr] = mm * 0x100 + ll;
        } else {
        }
    }

    private final int[][] qSoundRegister = {
            new int[256], new int[256]
    };

    public int[] getQSoundRegister(int chipId) {
        return qSoundRegister[chipId];
    }

    public void setMaskQSound(int chipId, int ch, boolean mask) {
        maskChQSound[chipId][ch] = mask;
        if (context.dicChipsInfo.containsKey(QSoundInst.class)) {
            if (mask)
                context.mds.setQSoundMask(chipId, ch);
            else
                context.mds.resetQSoundMask(chipId, ch);
        }
        if (context.dicChipsInfo.containsKey(CtrQSoundInst.class)) {
            if (mask)
                context.mds.setQSoundCtrMask(chipId, ch);
            else
                context.mds.resetQSoundCtrMask(chipId, ch);
        }
    }

    public void writeQSoundPCMData(int chipId,
                                   int romSize,
                                   int dataStart,
                                   int dataLength,
                                   byte[] romData,
                                   int srcStartAdr,
                                   EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriQsnd", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteQSoundCtrPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }
}
