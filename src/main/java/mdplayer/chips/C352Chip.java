/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.C352Inst;


/**
 * C352Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class C352Chip extends BaseChip {

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {C352Inst.class};
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (adr < 0x100 && (adr % 8) == 3 && mask[chipId][adr / 8]) {
            data &= 0xbfff;
        }
        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(C352Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        C352Inst inst = context.mds.inst(C352Inst.class);
        if (inst == null) return null; // the song being played does not use this chip
        // read back from the chip rather than shadowing the writes: the emulator moves the flags
        // on by itself, so a copy taken on write would never show a sample running out
        return Map.of(
                "register", inst.getView(chipId, "register", null).get("register"),
                "flags", inst.getView(chipId, "flags", null).get("flags")
        );
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    /** the panel/main-window view of whether a channel is muted; this array is the source of truth */
    public boolean getMask(int chipId, int ch) {
        return ch < mask[chipId].length && mask[chipId][ch];
    }
}
