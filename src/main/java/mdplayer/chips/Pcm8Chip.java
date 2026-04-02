/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.Instrument.PcmEnabledInstrument;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;


/**
 * Pcm8 (MSX).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-09 nsano initial version <br>
 */
public class Pcm8Chip implements Chip {

    private static final Logger logger = System.getLogger(Pcm8Chip.class.getName());

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    private BasePlugin context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {X68kYm2151Inst.class, Pcm8PPInst.class};
    }

    @Override
    public int activeIndex(int chipId) {
        return setting.getZMusic().pcm8Type;
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
            context.chipLED.put("PriPCM8", 2);
        else
            context.chipLED.put("SecPCM8", 2);

        context.mds.inst((Class<PcmEnabledInstrument>) inst(chipId)).writePcm(chipId, pcmData, 0, pcmData.length);
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPCM8", 2);
        else
            context.chipLED.put("SecPCM8", 2);

        if (port == -1 && addr == -1 && data == -1)
            return;
        context.mds.inst(inst(chipId)).write(chipId, port, addr, data);
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

    public void keyOn(int chipId, int ch, int addr, int mode, int len) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kYm2151Inst opmPCM -> opmPCM.chips[chipId].pcm8Out(ch, null, addr, mode, len);
            case Pcm8PPInst pcm8pp -> pcm8pp.keyOn(0, ch, addr, mode, len);
            default -> {assert false;}
        }
    }

    public void keyOff(int chipId, int ch) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kYm2151Inst opmPCM -> opmPCM.chips[chipId].pcm8Out(ch, null, 0, 0, 0);
            case Pcm8PPInst pcm8pp -> pcm8pp.keyOff(0, ch);
            default -> {assert false;}
        }
    }

    public void abort(int chipId) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kYm2151Inst opmPCM -> opmPCM.chips[0].pcm8Abort();
            case Pcm8PPInst pcm8pp -> {}
            default -> {assert false;}
        }
    }
}
