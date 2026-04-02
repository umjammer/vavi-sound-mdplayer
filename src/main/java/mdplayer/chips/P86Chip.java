/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.P86Inst;


/**
 * PMD.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class P86Chip implements Chip {

    private BasePlugin context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {P86Inst.class};
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

    public void writePcm(int chipId, int bank, int mode, byte[] buf, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriP86", 2);
        else
            context.chipLED.put("SecP86", 2);

        context.mds.inst(P86Inst.class).writePcm(chipId, buf, 0, buf.length, bank, mode);
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriP86", 2);
        else
            context.chipLED.put("SecP86", 2);

        if (port == -1 && addr == -1 && data == -1)
            return;
        context.mds.inst(inst(chipId)).write(chipId, port, addr, data);
    }
}
