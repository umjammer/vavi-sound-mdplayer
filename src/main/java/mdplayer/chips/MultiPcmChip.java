/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Collections;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.MultiPcmInst;


/**
 * MultiPcmChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MultiPcmChip extends BaseChip {

    private final int[] mask = {0, 0};

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {MultiPcmInst.class};
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, addr, data);
        } else {
        }
    }

    /**
     * The channel state as the chip has it now. This fires no led event: it is a read, and a view
     * polling it at frame rate would otherwise hold the led on for the whole song.
     */
    @Override
    public Map<String, Object> getInfo(int chipId) {
        MultiPcmInst inst = context.mds.inst(MultiPcmInst.class);
        return inst == null ? Collections.emptyMap() : inst.getView(chipId, "info");
    }

    @Override
    protected void setMask(int chipId, int ch, boolean mask, Object... args) {
        if (mask) {
            this.mask[chipId] |= 1 << ch;
        } else {
            this.mask[chipId] &= ~(1 << ch);
        }

        Instrument instrument = context.mds.inst(inst(chipId), 0);
        if (instrument == null) return; // the song being played does not use this chip

        if (mask)
            instrument.setMask(chipId, 1 << ch);
        else
            instrument.resetMask(chipId, 1 << ch);
    }

    @Override
    public boolean getMask(int chipId, int ch) {
        return ch < MultiPcmInst.CHANNELS && (mask[chipId] & (1 << ch)) != 0;
    }

    public void setBank(int chipId, int ch, int addr, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(MultiPcmInst.class).writeBank(chipId, ch, addr);
        } else {
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(MultiPcmInst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }
}
