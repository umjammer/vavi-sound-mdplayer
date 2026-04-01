/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.Instrument.PcmEnabledInstrument;
import mdsound.instrument.MPcmPPInst;
import mdsound.instrument.X68kMPcmInst;


/**
 * MPcm (MSX).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-01 nsano initial version <br>
 */
public class MPcmChip implements Chip {

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    private BasePlugin context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {X68kMPcmInst.class, MPcmPPInst.class};
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

    public void writePcm(int chipId, int bank, int mode, byte[] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

        context.mds.inst((Class<PcmEnabledInstrument>) inst(chipId)).writePcm(chipId, pcmData, 0, pcmData.length);
    }

    public void write(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriMPCM", 2);
        else
            context.chipLED.put("SecMPCM", 2);

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
}
