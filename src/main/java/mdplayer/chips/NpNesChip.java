/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.driver.nsf.Nsf;
import mdplayer.driver.nsf.NsfMdDriver;
import mdsound.Instrument;
import mdsound.instrument.NesInst;
import mdsound.instrument.NpNesInst;
import mdsound.np.chip.DeviceInfo;
import mdsound.np.chip.NesMmc5;
import mdsound.np.chip.NesVrc7;

import static java.lang.System.getLogger;


/**
 * NpNesChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-15 nsano initial version <br>
 */
public class NpNesChip extends BaseChip {

    private static final Logger logger = getLogger(NpNesChip.class.getName());

    // TODO
    public Nsf nsf;

    public void init() {
        if (context.driverVirtual instanceof NsfMdDriver nsfMdDriver)
            nsf = nsfMdDriver.nsf;
    }

    // vgm
    public static class DmcChip extends NpNesChip {
        private int dmcMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.DmcInst.class};
        }

        public int[] readDmc(int chipId) {
            int[] reg;
            try {
                // for nsf
                if (nsf.apu == null) reg = null;
                else if (nsf.apu.apu == null) reg = null;
                else if (chipId == 1) reg = null;
                else reg = nsf.dmc.dmc.reg;

                return reg;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                return null;
            }
        }

        public void setDmcMask(int chipId, int ch) {
            setMask(chipId, ch + 2);
        }

        public void resetDmcMask(int chipId, int ch) {
            resetMask(chipId, ch + 2);
        }
    }

    public static class Mmc5Chip extends NpNesChip {
        private int mmc5Mask = 0;
        private final byte[] mmc5Regs = new byte[10];

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.Mmc5Inst.class};
        }

        public byte[] readMmc5(int chipId) {
            // for nsf
            if (nsf == null || nsf.mmc5 == null) return null;
            else if (chipId == 1) return null;

            int[] dat = new int[] { 0 };
            for (int adr = 0x5000; adr < 0x5008; adr++) {
                nsf.mmc5.read(adr, dat);
                mmc5Regs[adr & 0x7] = (byte) dat[0];
            }

            nsf.mmc5.read(0x5010, dat);
            mmc5Regs[8] = (byte) (nsf.mmc5.pcmMode ? 1 : 0);
            mmc5Regs[9] = (byte) nsf.mmc5.pcm;

            return mmc5Regs;
        }

        public NesMmc5 readMmc5(int chipId, EnmModel model) {
            fireEventHappened("led.on", chipId);

            if (model == EnmModel.VirtualModel) {
                return null; // mds.readMMC5(chipId);
            } else {
                return null;
            }
        }

        public void setMmc5Mask(int chipId, int ch) {
            mmc5Mask |= 1 << ch;
            // there is no nsf behind this unless an nsf is what is playing
            if (nsf != null && nsf.mmc5 != null)
                nsf.mmc5.setMask(mmc5Mask);
        }

        public void resetMmc5Mask(int chipId, int ch) {
            mmc5Mask &= ~(1 << ch);
            if (nsf != null && nsf.mmc5 != null)
                nsf.mmc5.setMask(mmc5Mask);
        }
    }

    public static class Vrc6Chip extends NpNesChip {
        private int vrc6Mask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.Vrc6Inst.class};
        }

        public void setVrc6Mask(int chipId, int ch) {
            if (chipId != 0)
                return;
            vrc6Mask |= 1 << ch;
            if (nsf != null && nsf.vrc6 != null)
                nsf.vrc6.setMask(vrc6Mask);
        }

        public void resetVrc6Mask(int chipId, int ch) {
            if (chipId != 0)
                return;
            vrc6Mask &= ~(1 << ch);
            if (nsf != null && nsf.vrc6 != null)
                nsf.vrc6.setMask(vrc6Mask);
        }

        public DeviceInfo.TrackInfo[] readVrc6(int chipId) {
            if (nsf.vrc6 == null)
                return null;
            if (chipId != 0)
                return null;

            return nsf.vrc6.getTracksInfo();
        }
    }

    public static class Vrc7Chip extends NpNesChip {
        private int vrc7Mask = 0;
        private final ChipKeyInfo[] vrc7KeyOn = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.Vrc7Inst.class};
        }

        public ChipKeyInfo getVRC7KeyInfo(int chipId) {
            if (nsf.vrc7 == null)
                return null;
            if (chipId != 0)
                return null;

            NesVrc7.ChipKeyInfo ki = nsf.vrc7.getKeyInfo(chipId);

            ChipKeyInfo[] vrc7KeyOnRet = {new ChipKeyInfo(14), new ChipKeyInfo(14)};
            for (int ch = 0; ch < 6; ch++) {
                vrc7KeyOnRet[chipId].on[ch] = ki.on[ch];
                vrc7KeyOnRet[chipId].off[ch] = ki.off[ch];
            }
            return vrc7KeyOnRet[chipId];
        }

        public int[] readVrc7(int chipId) {
            if (nsf == null || nsf.vrc7 == null) return null;
            if (chipId != 0) return null;

            return nsf.vrc7.getRegs();
        }

        public void setVrc7Mask(int chipId, int ch) {
            vrc7Mask |= 1 << ch;
            if (nsf != null && nsf.vrc7 != null)
                nsf.vrc7.setMask(vrc7Mask);
        }

        public void resetVrc7Mask(int chipId, int ch) {
            vrc7Mask &= ~(1 << ch);
            if (nsf != null && nsf.vrc7 != null)
                nsf.vrc7.setMask(vrc7Mask);
        }
    }

    public static class N163Chip extends NpNesChip {
        private int n163Mask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.N160Inst.class};
        }

        public DeviceInfo.TrackInfo[] readN163(int chipId) {
            if (nsf.n106 == null)
                return null;
            if (chipId != 0)
                return null;

            return nsf.n106.getTracksInfo();
        }

        public void setN163Mask(int chipId, int ch) {
            if (chipId != 0)
                return;
            n163Mask |= 1 << ch;
            if (nsf != null && nsf.n106 != null)
                nsf.n106.setMask(n163Mask);
        }

        public void resetN163Mask(int chipId, int ch) {
            if (chipId != 0)
                return;
            n163Mask &= ~(1 << ch);
            if (nsf != null && nsf.n106 != null)
                nsf.n106.setMask(n163Mask);
        }
    }

    // vgm
    public static class FdsChip extends NpNesChip {
        private int fdsMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.FdsInst.class};
        }

        public Map<String, Object> readFds(int chipId) {

            // for nsf
            if (nsf == null || nsf.apu == null) return null;
            else if (nsf.apu.apu == null) return null;
            else if (chipId == 1) return null;
            else {
                return NesInst.toInfo(nsf.fds.fds);
            }
        }

        public void setFdsMask(int chipId) {
            fdsMask |= 1;
            if (nsf != null && nsf.fds != null)
                nsf.fds.setMask(fdsMask);
        }

        public void resetFdsMask(int chipId) {
            fdsMask &= ~1;
            if (nsf != null && nsf.fds != null)
                nsf.fds.setMask(fdsMask);
        }
    }

    public static class Fme7Chip extends NpNesChip {

        private final byte[] s5bRegs = new byte[0x20];

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {NpNesInst.Fme7Inst.class};
        }

        public byte[] readS5B(int chipId) {
            // for nsf
            if (nsf == null || nsf.fme7 == null) return null;
            else if (chipId == 1) return null;

            int[] dat = new int[] { 0 };
            for (int adr = 0x00; adr < 0x20; adr++) {
                nsf.fme7.read(adr, dat);
                s5bRegs[adr] = (byte) dat[0];
            }

            return s5bRegs;
        }
    }

    private int apuMask = 0;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {NpNesInst.class};
    }

    // vgm
    public void setMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
                case 0:
                case 1:
                    apuMask |= 1 << ch;
                    if (nsf != null && nsf.apu != null)
                        nsf.apu.setMask(apuMask);
                    break;
                case 2:
                case 3:
                case 4:
                    context.chipRegister.chip(NpNesChip.DmcChip.class).dmcMask |= 1 << (ch - 2);
                    if (nsf != null && nsf.dmc != null)
                        nsf.dmc.setMask(context.chipRegister.chip(NpNesChip.DmcChip.class).dmcMask);
                    break;
            }
        }
        Instrument instrument = context.mds.inst(NpNesInst.class);
        if (instrument == null) return; // the song being played does not use this chip
        instrument.setMask(chipId, ch);
    }

    // vgm
    public void resetMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
                case 0:
                case 1:
                    apuMask &= ~(1 << ch);
                    if (nsf != null && nsf.apu != null)
                        nsf.apu.setMask(apuMask);
                    break;
                case 2:
                case 3:
                case 4:
                    context.chipRegister.chip(NpNesChip.DmcChip.class).dmcMask &= ~(1 << (ch - 2));
                    if (nsf != null && nsf.dmc != null)
                        nsf.dmc.setMask(context.chipRegister.chip(NpNesChip.DmcChip.class).dmcMask);
                    break;
            }
        }
        Instrument instrument = context.mds.inst(NpNesInst.class);
        if (instrument == null) return; // the song being played does not use this chip
        instrument.resetMask(chipId, ch);
    }

    // vgm
    public Map<String, Object> getInfo(int chipId) {
        int[] reg;

        // for nsf
        if (nsf.apu == null) reg = null;
        else if (nsf.apu.apu == null) reg = null;
        else if (chipId == 1) reg = null;
        else reg = nsf.apu.apu.reg;

        return reg != null ? Map.of("register", reg) : Collections.emptyMap();
    }

    // nsf
    public int getVolume(int chip) {
//logger.log(Level.INFO, "VOL: %d, %d, %d, %d, %d, %d, %d, %d".formatted(
// context.mds.getChipInfo(NpNesInst.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.DMC.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.FDS.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.N160.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.VRC6.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.MMC5.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.FME7.class).getTVolume(),
// context.mds.getChipInfo(NpNesInst.VRC7.class).getTVolume()));
        return switch (chip) {
            case 0 -> context.mds.getChipInfo(NpNesInst.class).getTVolume();
            case 1 -> context.mds.getChipInfo(NpNesInst.DmcInst.class).getTVolume();
            case 2 -> context.mds.getChipInfo(NpNesInst.FdsInst.class).getTVolume();
            case 3 -> context.mds.getChipInfo(NpNesInst.N160Inst.class).getTVolume();
            case 4 -> context.mds.getChipInfo(NpNesInst.Vrc6Inst.class).getTVolume();
            case 5 -> context.mds.getChipInfo(NpNesInst.Mmc5Inst.class).getTVolume();
            case 6 -> context.mds.getChipInfo(NpNesInst.Fme7Inst.class).getTVolume();
            case 7 -> context.mds.getChipInfo(NpNesInst.Vrc7Inst.class).getTVolume();
            default -> throw new IllegalArgumentException("Unexpected value: " + chip);
        };
    }
}
