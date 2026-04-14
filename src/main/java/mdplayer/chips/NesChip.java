/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.NesInst;
import mdsound.np.Device.Bus;
import mdsound.np.NpNesApu;
import mdsound.np.NpNesDmc;
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
public class NesChip extends BaseChip {

    private static final Logger logger = getLogger(NesChip.class.getName());

    // vgm
    public static class DmcChip extends NesChip {
        private int dmcMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.DMC.class};
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

        public void setDmcMask(int chipId, int ch) {
            setMask(chipId, ch + 2);
        }

        public void resetDmcMask(int chipId, int ch) {
            resetMask(chipId, ch + 2);
        }
    }

    public static class Mmc5Chip extends NesChip {
        private int mmc5Mask = 0;
        private final byte[] mmc5Regs = new byte[10];

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.MMC5.class};
        }

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
            mmc5Regs[9] = (byte) mmc5.pcm;

            return mmc5Regs;
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
    }

    public static class Vrc6Chip extends NesChip {
        private int vrc6Mask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.VRC6.class};
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

        public DeviceInfo.TrackInfo[] readVrc6(int chipId) {
            if (vrc6 == null)
                return null;
            if (chipId != 0)
                return null;

            return vrc6.getTracksInfo();
        }
    }

    public static class Vrc7Chip extends NesChip {
        private int vrc7Mask = 0;
        private final ChipKeyInfo[] vrc7KeyOn = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.VRC7.class};
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

        public int[] readVrc7(int chipId) {
            if (vrc7 == null) return null;
            if (chipId != 0) return null;

            return vrc7.getRegs();
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
    }

    public static class N163Chip extends NesChip {
        private int n163Mask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.N160.class};
        }

        public DeviceInfo.TrackInfo[] readN163(int chipId) {
            if (n106 == null)
                return null;
            if (chipId != 0)
                return null;

            return n106.getTracksInfo();
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
    }

    // vgm
    public static class FdsChip extends NesChip {
        private int fdsMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.FDS.class};
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

        public NpNesFds readFds(int chipId, EnmModel model) {
            if (chipId == 0)
                context.chipLED.put("PriFDS", 2);
            else
                context.chipLED.put("SecFDS", 2);

            if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                return context.mds.inst(NesInst.FDS.class).readFds(chipId);
//            }
            } else {
                return null;
//            if (scFDS[chipId] == null) return;
//
//            scFDS[chipId].setRegister(dAddr, dData);
            }
        }

        public void setFdsMask(int chipId) {
            fdsMask |= 1;
            if (fds != null)
                fds.setMask(fdsMask);
            context.mds.inst(NesInst.FDS.class).setFDSMask(chipId);
        }

        public void resetFdsMask(int chipId) {
            fdsMask &= ~1;
            if (fds != null)
                fds.setMask(fdsMask);
            context.mds.inst(NesInst.FDS.class).resetFDSMask(chipId);
        }
    }

    public static class Fme7Chip extends NesChip {

        private final byte[] s5bRegs = new byte[0x20];

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NesInst.FME7.class};
        }

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
    }

    private int apuMask = 0;

    // TODO consider more
    public NesBank bank = null;
    public NesMem mem = null;
    public Km6502 cpu = null;

    // TODO consider more
    public NesApu apu = null;
    public NesDmc dmc = null;
    public NesFds fds = null;
    public NesN106 n106 = null;
    public NesVrc6 vrc6 = null;
    public NesMmc5 mmc5 = null;
    public NesFme7 fme7 = null;
    public NesVrc7 vrc7 = null;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {NesInst.class};
    }

    // vgm
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

    // vgm
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

    // vgm
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
                    context.chipRegister.chip(NesChip.DmcChip.class).dmcMask |= 1 << (ch - 2);
                    if (dmc != null)
                        dmc.setMask(context.chipRegister.chip(NesChip.DmcChip.class).dmcMask);
                    break;
            }
        }
        context.mds.inst(NesInst.class).setMask(chipId, ch);
    }

    // vgm
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
                    context.chipRegister.chip(NesChip.DmcChip.class).dmcMask &= ~(1 << (ch - 2));
                    if (dmc != null)
                        dmc.setMask(context.chipRegister.chip(NesChip.DmcChip.class).dmcMask);
                    break;
            }
        }
        context.mds.inst(NesInst.class).resetMask(chipId, ch);
    }

    // vgm
    public void writePcm(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(NesInst.class).writePcm(chipId, vgmBuf, vgmAdr, dataSize, stAdr);

        dumpData(model, "NES_PCMData", vgmAdr, vgmBuf, dataSize);
    }

    // vgm
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

    // nsf
    public int getVolume(int chip) {
        return switch (chip) {
            case 0 -> context.mds.getChipInfo(NesInst.class).getTVolume();
            case 1 -> context.mds.getChipInfo(NesInst.DMC.class).getTVolume();
            case 2 -> context.mds.getChipInfo(NesInst.FDS.class).getTVolume();
            case 3 -> context.mds.getChipInfo(NesInst.N160.class).getTVolume();
            case 4 -> context.mds.getChipInfo(NesInst.VRC6.class).getTVolume();
            case 5 -> context.mds.getChipInfo(NesInst.MMC5.class).getTVolume();
            case 6 -> context.mds.getChipInfo(NesInst.FME7.class).getTVolume();
            case 7 -> context.mds.getChipInfo(NesInst.VRC7.class).getTVolume();
            default -> throw new IllegalArgumentException("Unexpected value: " + chip);
        };
    }

    // nsf TODO consider more
    public void start(int clock, int sampleRate) {
        this.bank = new NesBank();
        this.mem = new NesMem();
        this.cpu = new Km6502(true);
        this.apu = new NesApu();
        this.dmc = new NesDmc();
        this.fds = new NesFds();
        this.n106 = new NesN106();
        this.vrc6 = new NesVrc6();
        this.mmc5 = new NesMmc5();
        this.fme7 = new NesFme7();
        this.vrc7 = new NesVrc7();

        this.apu.apu.init(clock, sampleRate);
        this.apu.reset();
        this.dmc.dmc.init(clock, sampleRate);
        this.dmc.reset();
        this.fds.fds.init(clock, sampleRate);
        this.fds.reset();
        this.n106.setClock(clock);
        this.n106.setRate(sampleRate);
        this.n106.reset();
        this.vrc6.setClock(clock);
        this.vrc6.setRate(sampleRate);
        this.vrc6.reset();
        this.mmc5.setClock(clock);
        this.mmc5.setRate(sampleRate);
        this.mmc5.reset();
        this.mmc5.setCPU(this.cpu);
        this.fme7.setClock(clock);
        this.fme7.setRate(sampleRate);
        this.fme7.reset();
        this.vrc7.setClock(clock);
        this.vrc7.setRate(sampleRate);
        this.vrc7.reset();

        this.dmc.dmc.nes_apu = this.apu.apu;
        this.dmc.dmc.setAPU(this.apu.apu);
    }

    // nsf TODO consider more
    public void setOptions(Bus apuBus, boolean useFds, boolean useN106, boolean useVrc6, boolean useMmc5, boolean useFme7, boolean useVrc7, byte[] bankSwitch) {
        apuBus.attach(this.apu);
        apuBus.attach(this.dmc);

        this.apu.setOption(NpNesApu.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getNESUnmuteOnReset() ? 1 : 0);
        this.apu.setOption(NpNesApu.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getNESNonLinearMixer() ? 1 : 0);
        this.apu.setOption(NpNesApu.OPT.PHASE_REFRESH.ordinal(), setting.getNsf().getNESPhaseRefresh() ? 1 : 0);
        this.apu.setOption(NpNesApu.OPT.DUTY_SWAP.ordinal(), setting.getNsf().getNESDutySwap() ? 1 : 0);

        this.dmc.setOption(NpNesDmc.OPT.ENABLE_4011.ordinal(), setting.getNsf().getDMCEnable4011() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.ENABLE_PNOISE.ordinal(), setting.getNsf().getDMCEnablePnoise() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getDMCUnmuteOnReset() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.DPCM_ANTI_CLICK.ordinal(), setting.getNsf().getDMCDPCMAntiClick() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getDMCNonLinearMixer() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.RANDOMIZE_NOISE.ordinal(), setting.getNsf().getDMCRandomizeNoise() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.TRI_MUTE.ordinal(), setting.getNsf().getDMCTRImute() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.RANDOMIZE_TRI.ordinal(), setting.getNsf().getDMCRandomizeTRI() ? 1 : 0);
        this.dmc.setOption(NpNesDmc.OPT.DPCM_REVERSE.ordinal(), setting.getNsf().getDMCDPCMReverse() ? 1 : 0);

        if (useFds) {
            boolean write_enable = !setting.getNsf().getFDSWriteDisable8000();
            this.fds.setOption(0, setting.getNsf().getFDSLpf());
            this.fds.setOption(1, setting.getNsf().getFDS4085Reset() ? 1 : 0);
            this.mem.setFDSMode(write_enable);
            this.bank.setFDSMode(write_enable);
            this.bank.setBankDefault(6, bankSwitch[6] & 0xff);
            this.bank.setBankDefault(7, bankSwitch[7] & 0xff);
            apuBus.attach(this.fds);
        } else {
            this.mem.setFDSMode(false);
            this.bank.setFDSMode(false);
        }
        if (useN106) {
            this.n106.setOption(0, setting.getNsf().getN160Serial() ? 1 : 0);
            apuBus.attach(this.n106);
        }
        if (useVrc6) {
            apuBus.attach(this.vrc6);
        }
        if (useMmc5) {
            this.mmc5.setOption(0, setting.getNsf().getMMC5NonLinearMixer() ? 1 : 0);
            this.mmc5.setOption(1, setting.getNsf().getMMC5PhaseRefresh() ? 1 : 0);
            apuBus.attach(this.mmc5);
        }
        if (useFme7) {
            apuBus.attach(this.fme7);
        }
        if (useVrc7) {
            apuBus.attach(this.vrc7);
        }
    }
}
