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
import mdsound.instrument.OkiM6295Inst;


/**
 * Oki6295Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class OkiM6295Chip extends BaseChip {

    @Deprecated
    private final boolean[][] mask = {
            {false, false, false, false},
            {false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {OkiM6295Inst.class};
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;

        Instrument instrument = context.mds.inst(inst(chipId), 0);
        if (instrument == null) return; // the song being played does not use this chip

        if (mask)
            instrument.setMask(chipId, 1 << ch);
        else
            instrument.resetMask(chipId, 1 << ch);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        return context.mds.inst(OkiM6295Inst.class, 0).getView(chipId, "info", null);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(OkiM6295Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
//logger.log(Level.TRACE, "chipId=%d Port=%x data=%x".formatted(chipId, port, data));
        }
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    @Deprecated
    public static class Params {

        public final mdplayer.MDChipParams.Channel[] channels = new mdplayer.MDChipParams.Channel[] {new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()};

        public int masterClock = 0;
        public int pin7State = 0;
        public final int[] nmkBank = new int[4];
    }

    @Deprecated
    public final Params[] okim6295 = new Params[] {new Params(), new Params()};
    @Deprecated
    public final Params[] okim6295_old = new Params[] {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo OKIM6295 = new VolumeInfo();
    @Deprecated
    public final VolumeInfo OKIM6295_old = new VolumeInfo();
}
