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
import mdsound.instrument.DmgInst;


/**
 * DmgChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class DmgChip extends BaseChip {

    @Deprecated
    public final boolean[][] mask = {
            {false, false, false, false},
            {false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {DmgInst.class};
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
            context.mds.write(inst(chipId), chipId, 0, addr, data);
//            }
        } else {
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(addr, data);
        }
    }

    public void setMask(int chipId, int ch) {
        mask[chipId][ch] = true;

        Instrument instrument = context.mds.inst(inst(chipId));
        if (instrument == null) return; // the song being played does not use this chip
        instrument.setMask(chipId, ch);
    }

    public void resetMask(int chipId, int ch) {
        mask[chipId][ch] = false;

        Instrument instrument = context.mds.inst(inst(chipId));
        if (instrument == null) return; // the song being played does not use this chip
        instrument.resetMask(chipId, ch);
    }

    public int read(int chipId, int addr) {
        if (chipId == 1) return 0;

        return context.mds.inst(DmgInst.class).read(chipId, addr);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        if (chipId == 1) return null;

        return context.mds.inst(DmgInst.class).getView(chipId, "info", null);
    }

    @Deprecated
    public static class Params {

        public final byte[] wf = new byte[32];
        public final mdplayer.MDChipParams.Channel[] channels = new mdplayer.MDChipParams.Channel[] {new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()};
    }

    @Deprecated
    public final Params[] dmg = new Params[] {new Params(), new Params()};
    @Deprecated
    public final Params[] dmg_old= new Params[] {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo DMG = new VolumeInfo();
    @Deprecated
    public final VolumeInfo DMG_old = new VolumeInfo();
}
