/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.Ga20Inst;


/**
 * Ga20Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ga20Chip implements Chip {

    private BasePlugin<? extends BaseDriver> context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ga20Inst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int adr, int dat, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriGA20", 2);
        else
            context.chipLED.put("SecGA20", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, adr, dat);
        } else {
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriGA20", 2);
        else
            context.chipLED.put("SecGA20", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(Ga20Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);
    }
}
