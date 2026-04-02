/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.Instrument.PcmEnabledInstrument;
import mdsound.chips.MPcm;
import mdsound.chips.MPcmPP;
import mdsound.chips.MPcmPP.SETPCM;
import mdsound.instrument.MPcmPPInst;
import mdsound.instrument.X68kMPcmInst;


/**
 * MPcm (MSX).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-01 nsano initial version <br>
 */
public class MPcmChip implements Chip {

    private static final Logger logger = System.getLogger(MPcmChip.class.getName());

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

    public void keyOn(int chipId, int ch) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.keyOn(chipId, ch);
            case MPcmPPInst mpcmpp -> mpcmpp.keyOn(chipId, ch);
            default -> {assert false;}
        }
    }

    public void keyOff(int chipId, int ch) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.keyOff(chipId, ch);
            case MPcmPPInst mpcmpp -> mpcmpp.keyOff(chipId, ch);
            default -> {assert false;}
        }
    }

    public void writePcm(int chipId, int ch, Object pcm, Object mem, Object reg, int n) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> {
                if (pcm instanceof mdplayer.driver.zms.Zms.MPCMSt[] mpcmSt && mem instanceof mdplayer.driver.zms.nise68.Memory68 mem68 && reg instanceof mdplayer.driver.zms.nise68.Register68 reg68) {
                    MPcm.PCM ptr = new MPcm.PCM();
                    ptr.adrsBuf = mem68.mem;
                    mpcmSt[ch].type = ptr.type = mem68.peekB(0x00 + reg68.getAl(1));
                    mpcmSt[ch].orig = ptr.orig = mem68.peekB(0x01 + reg68.getAl(1));
                    mpcmSt[ch].adrs_ptr = ptr.adrsPtr = mem68.peekL(0x04 + reg68.getAl(1));
                    mpcmSt[ch].size = ptr.size = mem68.peekL(0x08 + reg68.getAl(1));
                    mpcmSt[ch].start = ptr.start = mem68.peekL(0x0c + reg68.getAl(1));
                    mpcmSt[ch].end = ptr.end = mem68.peekL(0x10 + reg68.getAl(1));
                    mpcmSt[ch].count = ptr.count = mem68.peekL(0x14 + reg68.getAl(1));
                    //mpcmSt[ch].frq = mpcmSt[ch].type == 0xff ? 4 : (mpcmSt[ch].type == 1 ? 8 : (mpcmSt[ch].type == 2 ? 0x10 : 0));
                    mpcmSt[ch].rate = mpcm.chips[0].rate;
                    mpcmSt[ch].base_ = mpcm.chips[0].base;

                    //nise68.dumpMemory((int) ptr.adrs_ptr, (int) (ptr.adrs_ptr + ptr.size));
                    mpcm.writePcm(0, ch, ptr);
                } else if (pcm instanceof mdplayer.driver.zms.Zms.MPCMSt[] mpcmSt && mem instanceof mdplayer.driver.mxdrv.XMemory mm && reg instanceof mdplayer.driver.mndrv.Reg reg68) {
                    final MPcm.PCM tbl = new MPcm.PCM();
                    tbl.adrsBuf = mm.mm;
                    mpcmSt[ch].type = tbl.type = mm.readByte(0x00 + reg68.a1);
                    mpcmSt[ch].orig = tbl.orig = mm.readByte(0x01 + reg68.a1);
                    mpcmSt[ch].adrs_ptr = tbl.adrsPtr = mm.readInt(0x04 + reg68.a1);
                    mpcmSt[ch].size = tbl.size = mm.readInt(0x08 + reg68.a1);
                    mpcmSt[ch].start = tbl.start = mm.readInt(0x0c + reg68.a1);
                    mpcmSt[ch].end = tbl.end = mm.readInt(0x10 + reg68.a1);
                    mpcmSt[ch].count = tbl.count = mm.readInt(0x14 + reg68.a1);
                    mpcmSt[ch].frq = (mpcmSt[ch].type & 0xff) == 0xff ? 4 : (mpcmSt[ch].type == 1 ? 8 : (mpcmSt[ch].type == 2 ? 0x10 : 0));
                    mpcmSt[n & 0xf].rate = mpcm.chips[0].rate;
                    mpcmSt[n & 0xf].base_ = mpcm.chips[0].base;
                    mpcm.writePcm(0, ch, tbl);
                } else {
logger.log(Level.WARNING, "unhandled type: {0}, {1}", pcm.getClass().getName(), mem.getClass().getName());
                }
            }
            case MPcmPPInst mpcmpp -> {
                if (pcm instanceof mdplayer.driver.zms.Zms.MPCMSt[] mpcmSt && mem instanceof mdplayer.driver.zms.nise68.Memory68 mem68 && reg instanceof mdplayer.driver.zms.nise68.Register68 reg68) {
                    SETPCM ptr = new SETPCM();
                    ptr.adrs_buf = mem68.mem;
                    mpcmSt[ch].type = ptr.type = mem68.peekB(0x00 + reg68.getAl(1));
                    mpcmSt[ch].orig = ptr.orig = mem68.peekB(0x01 + reg68.getAl(1));
                    mpcmSt[ch].adrs_ptr = ptr.adrs_ptr = mem68.peekL(0x04 + reg68.getAl(1));
                    mpcmSt[ch].size = ptr.size = mem68.peekL(0x08 + reg68.getAl(1));
                    mpcmSt[ch].start = ptr.start = mem68.peekL(0x0c + reg68.getAl(1));
                    mpcmSt[ch].end = ptr.end = mem68.peekL(0x10 + reg68.getAl(1));
                    mpcmSt[ch].count = ptr.count = mem68.peekL(0x14 + reg68.getAl(1));
                    //mpcmSt[ch].frq = mpcmSt[ch].type == 0xff ? 4 : (mpcmSt[ch].type == 1 ? 8 : (mpcmSt[ch].type == 2 ? 0x10 : 0));
                    mpcmSt[ch].rate = mpcmpp.chips[0].rate;
                    mpcmSt[ch].base_ = mpcmpp.chips[0].base;

                    //nise68.dumpMemory((int) ptr.adrs_ptr, (int) (ptr.adrs_ptr + ptr.size));
                    mpcmpp.setPcm(0, ch, ptr);
                } else if (pcm instanceof mdplayer.driver.zms.Zms.MPCMSt[] mpcmSt && mem instanceof mdplayer.driver.mxdrv.XMemory mm && reg instanceof mdplayer.driver.mndrv.Reg reg68) {
                    final MPcmPP.SETPCM ptr = new MPcmPP.SETPCM();
                    ptr.adrs_buf = mm.mm;
                    mpcmSt[ch].type = ptr.type = mm.readByte(0x00 + reg68.a1);
                    mpcmSt[ch].orig = ptr.orig = mm.readByte(0x01 + reg68.a1);
                    mpcmSt[ch].adrs_ptr = ptr.adrs_ptr = mm.readInt(0x04 + reg68.a1);
                    mpcmSt[ch].size = ptr.size = mm.readInt(0x08 + reg68.a1);
                    mpcmSt[ch].start = ptr.start = mm.readInt(0x0c + reg68.a1);
                    mpcmSt[ch].end = ptr.end = mm.readInt(0x10 + reg68.a1);
                    mpcmSt[ch].count = ptr.count = mm.readInt(0x14 + reg68.a1);
                    mpcmSt[ch].rate = mpcmpp.chips[0].rate;
                    mpcmSt[ch].base_ = mpcmpp.chips[0].base;
                    mpcmSt[ch].frq = (mpcmSt[ch].type & 0xff) == 0xff ? 4 : (mpcmSt[ch].type == 1 ? 8 : (mpcmSt[ch].type == 2 ? 0x10 : 0));
                    //nise68.DumpMemory((uint)ptr.adrs_ptr, (uint)(ptr.adrs_ptr + ptr.size));
                    mpcmpp.setFreq(0, ch, mpcmSt[ch].frq);
                    mpcmpp.setPcm(0, ch, ptr);
                } else {
logger.log(Level.WARNING, "unhandled type: {0}, {1}", pcm.getClass().getName(), mem.getClass().getName());
                }
            }
            default -> {assert false;}
        }
    }

    public void setFreq(int chipId, int ch, int value) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.setFreq(chipId, ch, value);
            case MPcmPPInst mpcmpp -> mpcmpp.setFreq(chipId, ch, value);
            default -> {assert false;}
        }
    }

    public void setPitch(int chipId, int ch, int value) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.setPitch(chipId, ch, value);
            case MPcmPPInst mpcmpp -> setPitch(chipId, ch, value);
            default -> {assert false;}
        }
    }

    public void setVol(int chipId, int ch, int value) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.setVol(chipId, ch, value);
            case MPcmPPInst mpcmpp -> mpcmpp.setVol(chipId, ch, value);
            default -> {assert false;}
        }
    }

    public void setPan(int chipId, int ch, int value) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.setPan(chipId, ch, value);
            case MPcmPPInst mpcmpp -> mpcmpp.setPan(chipId, ch, value);
            default -> {assert false;}
        }
    }

    public void reset(int chipId) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.reset(chipId);
            case MPcmPPInst mpcmpp -> mpcmpp.reset(chipId);
            default -> {assert false;}
        }
    }

    public void setVolTable(int chipId, int type) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.setVolTable(chipId, type);
            case MPcmPPInst mpcmpp -> mpcmpp.setVolTable(chipId, type);
            default -> {assert false;}
        }
    }

    public void setVolTable(int chipId, int type, int[] vtbl) {
        switch (context.mds.inst(inst(chipId))) {
            case X68kMPcmInst mpcm -> mpcm.setVolTable(chipId, type, vtbl);
            case MPcmPPInst mpcmpp -> mpcmpp.setVolTable(chipId, type, vtbl);
            default -> {assert false;}
        }
    }
}
