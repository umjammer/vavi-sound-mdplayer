/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Arrays;

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
        if (chipId == 0)
            context.chipLED.put("PriEs53", 2);
        else
            context.chipLED.put("SecEs53", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
        }
    }

    public void writePcm(int chipId, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriEs53", 2);
        else
            context.chipLED.put("SecEs53", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(Es5503Inst.class).writePcm(chipId, Arrays.copyOfRange(buf, srcOffset, srcOffset + length), offset, length);
        } else {
        }

        dumpData(model, "ES5503_PCMData", srcOffset, buf, length);
    }
}
