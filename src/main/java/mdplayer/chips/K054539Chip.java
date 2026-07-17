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
import mdsound.instrument.K054539Inst;


/**
 * K054539Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K054539Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {K054539Inst.class};
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.write(inst(chipId), chipId, 0, adr, data);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        K054539Inst inst = context.mds.inst(K054539Inst.class);
        return inst == null ? null : inst.getView(chipId, "info", null);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(K054539Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    @Deprecated
    public static class Params {

        public final mdplayer.MDChipParams.Channel[] channels = new mdplayer.MDChipParams.Channel[] {
                new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(),
                new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()
        };
    }

    @Deprecated
    public final Params[] k054539 = new Params[] {new Params(), new Params()};
    @Deprecated
    public final Params[] k054539_old = new Params[] {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo K054539 = new VolumeInfo();
    @Deprecated
    public final VolumeInfo K054539_old = new VolumeInfo();
}
