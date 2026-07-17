/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.MDChipParams.VolumeInfo;
import mdplayer.Setting;
import mdsound.Instrument;
import mdsound.instrument.HuC6280Inst;


/**
 * HuC6280Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class HuC6280Chip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getHuC6280Type();

    @Deprecated
    private final boolean[][] mask = {
            {false, false, false, false, false, false},
            {false, false, false, false, false, false}
    };

    @Deprecated
    private final int[] currentCh = {
            0, 0
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {HuC6280Inst.class};
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                if (addr == 0) {
                    currentCh[chipId] = data & 7;
                }
                if (addr == 4) {
                    data = mask[chipId][currentCh[chipId]] ? 0 : data;
                }
//logger.log(Level.TRACE, "chipId:%d adr:%d Dat:%d".formatted(chipId, addr, data));
                context.mds.write(inst(chipId), chipId, 0, addr, data);
            }
        } else {
//            if (scHuC6280[chipId] == null) return;
        }
    }

    public int read(int chipId, int adr, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            return context.mds.inst(inst(chipId)).read(chipId, adr);
        }

        return 0;
    }

    private void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        return context.mds.inst(HuC6280Inst.class).getView(chipId, "info", null);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    @Deprecated
    public static class Params {

        public int mvolL = -1;
        public int mvolR = -1;
        public int LfoCtrl = -1;
        public int LfoFrq = -1;

        public final mdplayer.MDChipParams.Channel[] channels = {new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()};
    }

    @Deprecated
    public final Params[] huc6280 = {new Params(), new Params()};
    @Deprecated
    public final Params[] huc6280_old = {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo HuC6280 = new VolumeInfo();
    @Deprecated
    public final VolumeInfo HuC6280_old = new VolumeInfo();
}
