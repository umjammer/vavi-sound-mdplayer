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
import mdsound.instrument.Ppz8Inst;


/**
 * FMP.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ppz8Chip extends BaseChip {

    @Deprecated
    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ppz8Inst.class};
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        return context.mds.inst(Ppz8Inst.class).getView(chipId, "info", null);
    }

    public void writePcm(int chipId, int bank, int mode, byte[][] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        fireEventHappened("led.on", chipId);

        context.mds.inst(Ppz8Inst.class).writePcm(chipId, bank, mode, pcmData);
    }

    public void write(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        fireEventHappened("led.on", chipId);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        context.mds.inst(inst(chipId)).write(chipId, dPort, dAddr, dData);
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    @Deprecated
    public static class Params {

        public final mdplayer.MDChipParams.Channel[] channels = {
                new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(),
                new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()
        };
    }

    @Deprecated
    public final Params[] ppz8 = {new Params(), new Params()};
    @Deprecated
    public final Params[] ppz8_old = {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo PPZ8 = new VolumeInfo();
    @Deprecated
    public final VolumeInfo PPZ8_old = new VolumeInfo();
}
