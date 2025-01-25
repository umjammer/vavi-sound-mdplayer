/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdsound.Instrument.PcmEnabledInstrument;


/**
 * QSoundChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class QSoundChip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getQSoundType();

    private final Class<? extends PcmEnabledInstrument>[] inst = new Class[2];

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,}
    };

    private ChipRegister context;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < inst.length; chipId++) {
            //
            inst[chipId] = (Class<? extends PcmEnabledInstrument>) EnmChip.QSound.getInstClass(chipTypes[chipId].getEnabledId());
        }
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
            context.mds.write(inst[chipId], chipId, 0, 0, mm);
            context.mds.write(inst[chipId], chipId, 0, 1, ll);
            context.mds.write(inst[chipId], chipId, 0, 2, rr);

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
        if (mask)
            context.mds.inst(inst[chipId]).setMask(chipId, ch);
        else
            context.mds.inst(inst[chipId]).resetMask(chipId, ch);
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
            context.mds.inst(inst[chipId]).writePcm(chipId, romData, dataStart, dataLength, srcStartAdr, romSize);
        }
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
