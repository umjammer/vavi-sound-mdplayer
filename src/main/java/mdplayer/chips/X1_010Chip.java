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
import mdsound.instrument.X1_010Inst;


/**
 * X1_010Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class X1_010Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {X1_010Inst.class};
    }

    public void write(int chipId, int mm, int ll, int rr, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, 0, mm * 0x100 + ll, rr);
        } else {
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(X1_010Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        X1_010Inst inst = context.mds.inst(X1_010Inst.class);
        return inst == null ? Collections.emptyMap() : inst.getView(chipId, "info");
    }

    @Override
    public String getName() {
        return "X1-010"; // visualizer doesn't have underline glyph
    }
}
