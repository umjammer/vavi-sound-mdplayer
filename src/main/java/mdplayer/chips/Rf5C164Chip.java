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
import mdsound.instrument.ScdPcmInst;


/**
 * Rf5C164Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Rf5C164Chip extends BaseChip {

    @Deprecated
    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {ScdPcmInst.class};
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;

        Instrument instrument = context.mds.inst(inst(chipId));
        if (instrument == null) return; // the song being played does not use this chip

        if (mask)
            instrument.setMask(chipId, ch);
        else
            instrument.resetMask(chipId, ch);
    }

    public void writePcm(int chipId, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(ScdPcmInst.class).writePcm(chipId, buf, offset, length, srcOffset);

        dumpData(model, "PCMData(8BitMonoSigned)", srcOffset, buf, length);
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, adr, data);
        }
    }

    public void writeMemory(int chipId, int offset, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(ScdPcmInst.class).writeMemory(chipId, offset, data);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        return context.mds.inst(ScdPcmInst.class).getView(chipId, "info", null);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    @Deprecated
    public static class Params {

        public final mdplayer.MDChipParams.Channel[] channels = new mdplayer.MDChipParams.Channel[] {new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()};
    }

    @Deprecated
    public final mdplayer.chips.Rf5C164Chip.Params[] rf5c164 = new mdplayer.chips.Rf5C164Chip.Params[] {new mdplayer.chips.Rf5C164Chip.Params(), new mdplayer.chips.Rf5C164Chip.Params()};
    @Deprecated
    public final mdplayer.chips.Rf5C164Chip.Params[] rf5c164_old = new mdplayer.chips.Rf5C164Chip.Params[] {new mdplayer.chips.Rf5C164Chip.Params(), new mdplayer.chips.Rf5C164Chip.Params()};

    @Deprecated
    public final VolumeInfo RF5C164 = new VolumeInfo();
    @Deprecated
    public final VolumeInfo RF5C164_old = new VolumeInfo();
}
