/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.NesInst;
import mdsound.instrument.NesInst.FME7;
import mdsound.np.NpNesFds;
import mdsound.np.chip.DeviceInfo;
import mdsound.np.chip.NesApu;
import mdsound.np.chip.NesDmc;
import mdsound.np.chip.NesFds;
import mdsound.np.chip.NesFme7;
import mdsound.np.chip.NesMmc5;
import mdsound.np.chip.NesN106;
import mdsound.np.chip.NesVrc6;
import mdsound.np.chip.NesVrc7;
import mdsound.np.cpu.Km6502;
import mdsound.np.memory.NesBank;
import mdsound.np.memory.NesMem;

import static java.lang.System.getLogger;


/**
 * NsfChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class NesChip implements Chip {

    private static final Logger logger = getLogger(NesChip.class.getName());

    public abstract static class DmcChip implements Chip {}
    public abstract static class Mmc5Chip implements Chip {}
    public abstract static class Vrc6Chip implements Chip {}
    public abstract static class Vrc7Chip implements Chip {}
    public abstract static class N160Chip implements Chip {}
    public abstract static class N163Chip implements Chip {}
    public abstract static class FdsChip implements Chip {}
    public abstract static class Fme7Chip implements Chip {}

    private int apuMask = 0;
    private int dmcMask = 0;
    private int fdsMask = 0;
    private int mmc5Mask = 0;
    private int vrc6Mask = 0;
    private int vrc7Mask = 0;
    private int n163Mask = 0;

    public NesBank bank = null;
    public NesMem mem = null;
    public Km6502 cpu = null;
    public NesApu apu = null;
    public NesDmc dmc = null;
    public NesFds fds = null;
    public NesN106 n106 = null;
    public NesVrc6 vrc6 = null;
    public NesMmc5 mmc5 = null;
    public NesFme7 fme7 = null;
    public NesVrc7 vrc7 = null;

    private final ChipKeyInfo[] vrc7KeyOn = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private Audio context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {NesInst.class};
    }

    @Override
    public void init(Audio context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                context.mds.write(NesInst.class, chipId, 0, addr, data);
//            }
        } else {
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(addr, data);
        }
    }

    public int[] readApu(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                return context.mds.inst(NesInst.class).readApu(chipId);
//            }
        } else {
            return null;
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public int[] readDmc(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                return context.mds.inst(NesInst.class).readDmc(chipId);
//            }
        } else {
            return null;
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public NpNesFds readFds(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriFDS", 2);
        else
            context.chipLED.put("SecFDS", 2);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                return context.mds.inst(NesInst.class).readFds(chipId);
//            }
        } else {
            return null;
//            if (scFDS[chipId] == null) return;
//
//            scFDS[chipId].setRegister(dAddr, dData);
        }
    }

    public NesMmc5 readMmc5(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMMC5", 2);
        else
            context.chipLED.put("SecMMC5", 2);

        if (model == EnmModel.VirtualModel) {
            return null; // mds.readMMC5(chipId);
        } else {
            return null;
        }
    }

    public ChipKeyInfo getVRC7KeyInfo(int chipId) {
        if (vrc7 == null)
            return null;
        if (chipId != 0)
            return null;

        NesVrc7.ChipKeyInfo ki = vrc7.getKeyInfo(chipId);

        ChipKeyInfo[] vrc7KeyOnRet = {new ChipKeyInfo(14), new ChipKeyInfo(14)};
        for (int ch = 0; ch < 6; ch++) {
            vrc7KeyOnRet[chipId].on[ch] = ki.on[ch];
            vrc7KeyOnRet[chipId].off[ch] = ki.off[ch];
        }
        return vrc7KeyOnRet[chipId];
    }

    public void setMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
                case 0:
                case 1:
                    apuMask |= 1 << ch;
                    if (apu != null)
                        apu.setMask(apuMask);
                    break;
                case 2:
                case 3:
                case 4:
                    dmcMask |= 1 << (ch - 2);
                    if (dmc != null)
                        dmc.setMask(dmcMask);
                    break;
            }
        }
        context.mds.inst(NesInst.class).setMask(chipId, ch);
    }

    public void resetMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
                case 0:
                case 1:
                    apuMask &= ~(1 << ch);
                    if (apu != null)
                        apu.setMask(apuMask);
                    break;
                case 2:
                case 3:
                case 4:
                    dmcMask &= ~(1 << (ch - 2));
                    if (dmc != null)
                        dmc.setMask(dmcMask);
                    break;
            }
        }
        context.mds.inst(NesInst.class).resetMask(chipId, ch);
    }

    public void setFdsMask(int chipId) {
        fdsMask |= 1;
        if (fds != null)
            fds.setMask(fdsMask);
        context.mds.inst(NesInst.class).setFDSMask(chipId);
    }

    public void resetFdsMask(int chipId) {
        fdsMask &= ~1;
        if (fds != null)
            fds.setMask(fdsMask);
        context.mds.inst(NesInst.class).resetFDSMask(chipId);
    }

    public void setMmc5Mask(int chipId, int ch) {
        mmc5Mask |= 1 << ch;
        if (mmc5 != null)
            mmc5.setMask(mmc5Mask);
    }

    public void resetMmc5Mask(int chipId, int ch) {
        mmc5Mask &= ~(1 << ch);
        if (mmc5 != null)
            mmc5.setMask(mmc5Mask);
    }

    public void setVrc7Mask(int chipId, int ch) {
        vrc7Mask |= 1 << ch;
        if (vrc7 != null)
            vrc7.setMask(vrc7Mask);
    }

    public void resetVrc7Mask(int chipId, int ch) {
        vrc7Mask &= ~(1 << ch);
        if (vrc7 != null)
            vrc7.setMask(vrc7Mask);
    }

    public void setVrc6Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        vrc6Mask |= 1 << ch;
        if (vrc6 != null)
            vrc6.setMask(vrc6Mask);
    }

    public void resetVrc6Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        vrc6Mask &= ~(1 << ch);
        if (vrc6 != null)
            vrc6.setMask(vrc6Mask);
    }

    public void setN163Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        n163Mask |= 1 << ch;
        if (n106 != null)
            n106.setMask(n163Mask);
    }

    public void resetN163Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        n163Mask &= ~(1 << ch);
        if (n106 != null)
            n106.setMask(n163Mask);
    }

    public void writePcm(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(NesInst.class).writeRam(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public DeviceInfo.TrackInfo[] readVrc6(int chipId) {
        if (vrc6 == null)
            return null;
        if (chipId != 0)
            return null;

        return vrc6.getTracksInfo();
    }

    public int[] readVrc7(int chipId) {
        if (vrc7 == null) return null;
        if (chipId != 0) return null;

        return vrc7.getRegs();
    }

    public DeviceInfo.TrackInfo[] readN106(int chipId) {
        if (n106 == null)
            return null;
        if (chipId != 0)
            return null;

        return n106.getTracksInfo();
    }

    public int[] readApu(int chipId) {
        int[] reg;

        // for nsf
        if (apu == null) reg = null;
        else if (apu.apu == null) reg = null;
        else if (chipId == 1) reg = null;
        else reg = apu.apu.reg;

        // for vgm
        if (reg == null) reg = readApu(chipId, Common.EnmModel.VirtualModel);

        return reg;
    }

    public int[] readDmc(int chipId) {
        int[] reg;
        try {
            // for nsf
            if (apu == null) reg = null;
            else if (apu.apu == null) reg = null;
            else if (chipId == 1) reg = null;
            else reg = dmc.dmc.reg;

            // for vgm
            if (reg == null) reg = readDmc(chipId, Common.EnmModel.VirtualModel);

            return reg;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    public NpNesFds readFds(int chipId) {
        NpNesFds reg;

        // for nsf
        if (apu == null) reg = null;
        else if (apu.apu == null) reg = null;
        else if (chipId == 1) reg = null;
        else reg = fds.fds;

        // for vgm
        if (reg == null) reg = readFds(chipId, Common.EnmModel.VirtualModel);

        return reg;
    }

    private final byte[] s5bRegs = new byte[0x20];

    public byte[] readS5B(int chipId) {
        // for nsf
        if (fme7 == null) return null;
        else if (chipId == 1) return null;

        int[] dat = new int[] { 0 };
        for (int adr = 0x00; adr < 0x20; adr++) {
            fme7.read(adr, dat);
            s5bRegs[adr] = (byte) dat[0];
        }

        return s5bRegs;
    }

    private final byte[] mmc5Regs = new byte[10];

    public byte[] readMmc5(int chipId) {
        // for nsf
        if (mmc5 == null) return null;
        else if (chipId == 1) return null;

        int[] dat = new int[] { 0 };
        for (int adr = 0x5000; adr < 0x5008; adr++) {
            mmc5.read(adr, dat);
            mmc5Regs[adr & 0x7] = (byte) dat[0];
        }

        mmc5.read(0x5010, dat);
        mmc5Regs[8] = (byte) (mmc5.pcmMode ? 1 : 0);
        mmc5Regs[9] = mmc5.pcm;

        return mmc5Regs;
    }

    public void setDmcMask(int chipId, int ch) {
        setMask(chipId, ch + 2);
    }

    public void resetDmcMask(int chipId, int ch) {
        resetMask(chipId, ch + 2);
    }
}
