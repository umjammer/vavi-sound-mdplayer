/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.Upd7759Inst;


/**
 * Upd7759Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-02 nsano initial version <br>
 */
public class Upd7759Chip implements Chip {

    private BasePlugin context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Upd7759Inst.class};
    }

    @Override
    public void init(BasePlugin context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriuPD7759", 2);
        else
            context.chipLED.put("SecuPD7759", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriuPD7759", 2);
        else
            context.chipLED.put("SecuPD7759", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(Upd7759Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);
        } else {
        }
    }
}
