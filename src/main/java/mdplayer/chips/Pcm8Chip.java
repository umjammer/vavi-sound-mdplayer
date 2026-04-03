/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.util.function.BiConsumer;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.x68sound.X68Sound;


/**
 * PCM8 (X68000).
 * <p>
 * system property
 * <li>{@code mdplayer.variant.pcm8} ... active chip index</li>
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-09 nsano initial version <br>
 */
public class Pcm8Chip implements Chip {

    private static final Logger logger = System.getLogger(Pcm8Chip.class.getName());

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    private BasePlugin<? extends BaseDriver> context;

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
    public void init(BasePlugin<? extends BaseDriver> context) {
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

        Pcm8PPInst pcm8 = context.mds.inst(Pcm8PPInst.class, chipId);
        if (pcm8 != null) pcm8.writePcm(chipId, pcmData, 0, pcmData.length);
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
        Instrument inst = context.mds.inst(inst(chipId), chipId);
        if (inst != null) { // for mxdrv
            switch (inst) {
                case X68kYm2151Inst opmPCM -> opmPCM.keyOn(chipId, ch, addr, mode, len);
                case Pcm8PPInst pcm8pp -> pcm8pp.keyOn(chipId, ch, addr, mode, len);
                default -> { assert false; }
            }
        }
    }

    public void keyOff(int chipId, int ch) {
        Instrument inst = context.mds.inst(inst(chipId), chipId);
        if (inst != null) { // for mxdrv
            switch (inst) {
                case X68kYm2151Inst opmPCM -> opmPCM.keyOff(chipId, ch);
                case Pcm8PPInst pcm8pp -> pcm8pp.keyOff(chipId, ch);
                default -> { assert false; }
            }
        }
    }

    public void abort(int chipId) {
        Instrument inst = context.mds.inst(inst(chipId), chipId);
        if (inst != null) { // for mxdrv
            switch (inst) {
                case X68kYm2151Inst opmPCM -> opmPCM.abort(chipId);
                case Pcm8PPInst _ -> {}
                default -> { assert false; }
            }
        }
    }

    // mxd

    public int getPcm(int chipId, short[] buffer, int offset, int length, BiConsumer<Runnable, Boolean> clock) {
        return context.mds.inst(X68kYm2151Inst.class).getPcm(chipId, buffer, offset, length, clock);
    }

    public int getPcm(int chipId, short[] buffer, int offset, int length) {
        return context.mds.inst(X68kYm2151Inst.class).getPcm(chipId, buffer, offset, length);
    }

    public int start(int chipId, int sampleRate, int opmFlag, int adpcmFlag, int betw, int pcmBuf, int late, double rev) {
        return context.mds.inst(X68kYm2151Inst.class).start(chipId, sampleRate, opmFlag, adpcmFlag, betw, pcmBuf, late, rev);
    }

    public int startPcm(int chipId, int sampleRate, int opmFlag, int adpcmFlag, int pcmBuf) {
        return context.mds.inst(X68kYm2151Inst.class).startPcm(chipId, sampleRate, opmFlag, adpcmFlag, pcmBuf);
    }

    public void initIocs(int chipId) {
        context.mds.inst(X68kYm2151Inst.class).initIocs(chipId);
    }

    public void opmInt(int chipId, Runnable func) {
        context.mds.inst(X68kYm2151Inst.class).opmInt(chipId, func);
    }

    public int opmWait(int chipId, int wait) {
        return context.mds.inst(X68kYm2151Inst.class).opmWait(chipId, wait);
    }

    public int totalVolume(int chipId, int vol) {
        return context.mds.inst(X68kYm2151Inst.class).totalVolume(chipId, vol);
    }

    public void free(int chipId) {
        context.mds.inst(X68kYm2151Inst.class).free(chipId);
    }

    public void opmSetIocs(int chipId, int addr, int data) {
        context.mds.inst(X68kYm2151Inst.class).opmSetIocs(chipId, addr, data);
    }

    public void keyOnAdpcm(int chipId, int addr, int mode, int len) {
        switch (context.mds.inst(inst(chipId), chipId)) {
            case X68kYm2151Inst opmPCM -> opmPCM.keyOnAdpcm(chipId, addr, mode, len);
            case Pcm8PPInst pcm8pp -> pcm8pp.keyOn(0, 0, addr, mode + 0x0c00, len);
            default -> {assert false;}
        }
    }

    public void adpcmMod(int chipId, int mode) {
        switch (context.mds.inst(inst(chipId), chipId)) {
            case X68kYm2151Inst opmPCM -> opmPCM.adpcmMod(chipId, mode);
            case Pcm8PPInst pcm8pp -> pcm8pp.keyOff(0, 0);
            default -> {assert false;}
        }
    }

    public static boolean isFromDF(int v) {
        //noinspection ConstantValue
        return switch (v) {
            case X68Sound.SNDERR_DLL,
                 X68Sound.SNDERR_FUNC -> true;
            default -> true; // original is so
        };
    }

    public static boolean isFromPTM(int v) {
        return switch (v) {
            case X68Sound.SNDERR_PCMOUT,
                 X68Sound.SNDERR_TIMER,
                 X68Sound.SNDERR_MEMORY -> true;
            default -> false;
        };
    }
}
