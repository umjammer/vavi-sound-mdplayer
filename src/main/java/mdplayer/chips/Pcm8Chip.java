/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.util.Objects;

import mdplayer.Common.EnmModel;
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
public class Pcm8Chip extends BaseChip {

    private static final Logger logger = System.getLogger(Pcm8Chip.class.getName());

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {X68kYm2151Inst.class, Pcm8PPInst.class};
    }

    @Override
    public int activeIndex(int chipId) {
        return setting.getZMusic().pcm8Type;
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

    public void writePcm(int chipId, int bank, int mode, byte[] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPCM8", 2);
        else
            context.chipLED.put("SecPCM8", 2);

        switch (setting.getZMusic().pcm8Type) {
            case 0 -> context.mds.inst(X68kYm2151Inst.class).writePcm(chipId, pcmData, 0, pcmData.length);
            case 1 -> { try { Objects.requireNonNull(context.mds.inst(Pcm8PPInst.class)).writePcm(chipId, pcmData, 0, pcmData.length); } catch (NullPointerException _) {}}
            default -> { assert false; }
        }
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

    //

    public void keyOn(int chipId, int ch, int addr, int mode, int len) {
        switch (setting.getZMusic().pcm8Type) {
            case 0 -> context.mds.inst(X68kYm2151Inst.class).pcm8Out(chipId, ch, addr, mode, len);
            case 1 -> { try { Objects.requireNonNull(context.mds.inst(Pcm8PPInst.class)).keyOn(chipId, ch, addr, mode + 0x0800, len); } catch (NullPointerException _) {}}
            default -> { assert false; }
        }
    }

    public void keyOff(int chipId, int ch) {
        switch (setting.getZMusic().pcm8Type) {
            case 0 -> context.mds.inst(X68kYm2151Inst.class).pcm8Out(chipId, ch, 0, 0, 0);
            case 1 -> { try { Objects.requireNonNull(context.mds.inst(Pcm8PPInst.class)).keyOff(chipId, ch); } catch (NullPointerException _) {}}
            default -> { assert false; }
        }
    }

    public void abort(int chipId) {
        switch (setting.getZMusic().pcm8Type) {
            case 0 -> context.mds.inst(X68kYm2151Inst.class).abort(chipId);
            case 1 -> {}
            default -> { assert false; }
        }
    }

    // mxd

    public int getPcm(int chipId, short[] buffer, int offset, int length) {
        return context.mds.inst(X68kYm2151Inst.class).getPcm(chipId, buffer, offset, length);
    }

    public int start(int chipId, int sampleRate, int opmFlag, int adpcmFlag, int betw, int pcmBuf, int late, double rev) {
        return context.mds.inst(X68kYm2151Inst.class).start(chipId, sampleRate, opmFlag, adpcmFlag, betw, pcmBuf, late, rev);
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

    public void stop(int chipId) {
        context.mds.inst(X68kYm2151Inst.class).stop(chipId);
    }

    public void keyOnAdpcm(int chipId, int addr, int mode, int len) {
        switch (setting.getZMusic().pcm8Type) {
            case 0 -> context.mds.inst(X68kYm2151Inst.class).keyOnAdpcm(chipId, addr, mode, len);
            case 1 -> context.mds.inst(Pcm8PPInst.class).keyOn(chipId, 0, addr, mode + 0x0c00, len);
            default -> {assert false;}
        }
    }

    public void adpcmMod(int chipId, int mode) {
        switch (setting.getZMusic().pcm8Type) {
            case 0 -> context.mds.inst(X68kYm2151Inst.class).adpcmMod(chipId, mode);
            case 1 -> context.mds.inst(Pcm8PPInst.class).keyOff(chipId, 0);
            default -> {assert false;}
        }
    }
}
