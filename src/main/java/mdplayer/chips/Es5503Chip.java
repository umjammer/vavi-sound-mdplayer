/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.Es5503Inst;


/**
 * Es5503Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-02 nsano initial version <br>
 */
public class Es5503Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Es5503Inst.class};
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
        }
    }

    public void writePcm(int chipId, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(Es5503Inst.class).writePcm(chipId, Arrays.copyOfRange(buf, srcOffset, srcOffset + length), offset, length);
        } else {
        }

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        Es5503Inst inst = context.mds.inst(Es5503Inst.class);
        return inst == null ? Collections.emptyMap() : inst.getView(chipId, "info");
    }
}
