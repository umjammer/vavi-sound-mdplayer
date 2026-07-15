/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.K053260Inst;


/**
 * K053260Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K053260Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {K053260Inst.class};
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }

    public Map<String, Object> getInfo(int chipId) {
        K053260Inst inst = context.mds.inst(K053260Inst.class);
        return inst == null ? null : inst.getInfo(chipId);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(K053260Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }
}
