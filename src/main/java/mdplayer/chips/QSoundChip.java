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

    private final boolean[][] mask = {
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

    public void write(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriQsnd", 2);

        if (model == EnmModel.VirtualModel) {
            if (context.usedInstruments.containsKey(QSoundInst.class)) {
                context.mds.write(QSoundInst.class, chipId, 0, 0, mm);
                context.mds.write(QSoundInst.class, chipId, 0, 1, ll);
                context.mds.write(QSoundInst.class, chipId, 0, 2, rr);
            }
            if (context.usedInstruments.containsKey(CtrQSoundInst.class)) {
                context.mds.write(CtrQSoundInst.class, chipId, 0, 0, mm);
                context.mds.write(CtrQSoundInst.class, chipId, 0, 1, ll);
                context.mds.write(CtrQSoundInst.class, chipId, 0, 2, rr);
            }

            register[chipId][rr] = mm * 0x100 + ll;
        } else {
        }
    }

    private final int[][] register = {
            new int[256], new int[256]
    };

    public int[] read(int chipId) {
        return register[chipId];
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
        if (context.usedInstruments.containsKey(QSoundInst.class)) {
            if (mask)
                context.mds.inst(QSoundInst.class).setMask(chipId, ch);
            else
                context.mds.inst(QSoundInst.class).resetMask(chipId, ch);
        }
        if (context.usedInstruments.containsKey(CtrQSoundInst.class)) {
            if (mask)
                context.mds.inst(CtrQSoundInst.class).setMask(chipId, ch);
            else
                context.mds.inst(CtrQSoundInst.class).resetMask(chipId, ch);
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
            context.chipLED.put("PriQsnd", 2);

        if (model == EnmModel.VirtualModel) {
            if (context.usedInstruments.containsKey(QSoundInst.class)) {
                context.mds.inst(QSoundInst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
            }
            if (context.usedInstruments.containsKey(CtrQSoundInst.class)) {
                context.mds.inst(CtrQSoundInst.class).writePcm(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
            }
        }
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
