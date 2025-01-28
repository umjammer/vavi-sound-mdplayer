/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.C352Inst;


/**
 * C352Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class C352Chip implements Chip {

    public int[][] register = {null, null};

    public int[][] keyOn = {null, null};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private Audio context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {C352Inst.class};
    }

    @Override
    public void init(Audio context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[0x203];
            keyOn[chipId] = new int[32];
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

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC352", 2);
        else
            context.chipLED.put("SecC352", 2);

        if (adr < register[chipId].length)
            register[chipId][adr] = data;
        int c = adr / 8;
        if (adr < 0x100 && (adr % 8) == 3 && mask[chipId][adr / 8]) {
            data &= 0xbfff;
        }
        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }

    public int[] read(int chipId) {
        return context.mds.inst(C352Inst.class).readFlags(chipId);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriC352", 2);
        else
            context.chipLED.put("SecC352", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(C352Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);
    }

    public int[] getChip(int chipId) {
        return register[chipId];
    }

    public int[] getKeyOn(int chipId) {
        return read(chipId);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
