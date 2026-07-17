/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.MDChipParams.VolumeInfo;
import mdsound.Instrument;
import mdsound.instrument.Ga20Inst;


/**
 * Ga20Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ga20Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ga20Inst.class};
    }

    public void write(int chipId, int adr, int dat, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, adr, dat);
        } else {
        }
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        Ga20Inst inst = context.mds.inst(Ga20Inst.class);
        return inst == null ? null : inst.getView(chipId, "info", null);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(Ga20Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    @Deprecated
    public static class Params {

        public final mdplayer.MDChipParams.Channel[] channels = new mdplayer.MDChipParams.Channel[] {
                new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()
        };
    }

    @Deprecated
    public final Params[] ga20 = new Params[] {new Params(), new Params()};
    @Deprecated
    public final Params[] ga20_old = new Params[] {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo GA20 = new VolumeInfo();
    @Deprecated
    public final VolumeInfo GA20_old = new VolumeInfo();
}
