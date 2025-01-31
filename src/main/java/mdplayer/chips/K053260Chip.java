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
import mdsound.instrument.K053260Inst;


/**
 * K053260Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K053260Chip implements Chip {

    private Audio context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {K053260Inst.class};
    }

    @Override
    public void init(Audio context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK053260", 2);
        else
            context.chipLED.put("SecK053260", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK053260", 2);
        else
            context.chipLED.put("SecK053260", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(K053260Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);
    }
}
