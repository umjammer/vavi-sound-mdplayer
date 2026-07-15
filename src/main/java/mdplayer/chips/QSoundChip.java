/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdsound.Instrument;
import mdsound.Instrument.PcmEnabledInstrument;
import mdsound.instrument.CtrQSoundInst;
import mdsound.instrument.QSoundInst;


/**
 * QSoundChip.
 * <p>
 * system property
 * <li>{@code mdplayer.variant.qsound} ... active chip index</li>
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class QSoundChip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getQSoundType();

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,}
    };

    @SuppressWarnings("unchecked")
    private Class<? extends PcmEnabledInstrument> _inst(int chipId) {
        return (Class<? extends PcmEnabledInstrument>) inst(chipId);
    }

    @Override
    public int activeIndex(int chipId) {
        return chipTypes[chipId].getEnabledId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {CtrQSoundInst.class, QSoundInst.class};
    }

    public void write(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(_inst(chipId), chipId, 0, 0, mm);
            context.mds.write(_inst(chipId), chipId, 0, 1, ll);
            context.mds.write(_inst(chipId), chipId, 0, 2, rr);

            register[chipId][rr] = mm * 0x100 + ll;
        } else {
        }
    }

    private final int[][] register = {
            new int[256], new int[256]
    };

    public Map<String, Object> getInfo(int chipId) {
        return Map.of("register", register[chipId]);
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;

        PcmEnabledInstrument instrument = context.mds.inst(_inst(chipId));
        if (instrument == null) return; // the song being played does not use this chip

        if (mask)
            instrument.setMask(chipId, ch);
        else
            instrument.resetMask(chipId, ch);
    }

    public void writePcm(int chipId,
                         int romSize,
                         int dataStart,
                         int dataLength,
                         byte[] romData,
                         int srcStartAdr,
                         EnmModel model) {
        if (chipId == 0)
            fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(_inst(chipId)).writePcm(chipId, romData, dataStart, dataLength, srcStartAdr, romSize);
        }

        dumpData(model, "PCMData", srcStartAdr, romData, dataLength);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
