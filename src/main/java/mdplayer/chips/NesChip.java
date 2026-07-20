/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.NesInst;
import mdsound.instrument.NesInst.DmcInst;
import mdsound.instrument.NesInst.FdsInst;

import static java.lang.System.getLogger;


/**
 * NsfChip. (for vgm)
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
            return new Class[] {DmcInst.class};
        }

        // TODO getInfo
        public int[] getInfo(int chipId, EnmModel model) {
            fireEventHappened("led.on", chipId);

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

        public void setMask(int chipId, int ch) {
            super.setMask(chipId, ch + 2);
        }

        public void resetDmcMask(int chipId, int ch) {
            resetMask(chipId, ch + 2);
        }

        public boolean getDmcMask(int chipId, int ch) {
            return (dmcMask & (1 << ch)) != 0;
        }
    }

    // vgm
    public static class FdsChip extends NesChip {
        private int fdsMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {FdsInst.class};
        }

        // TODO getInfo
        public Map<String, Object> readFds(int chipId, EnmModel model) {
            fireEventHappened("led.on", chipId);

            if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                return context.mds.inst(FdsInst.class).getView(chipId, "info", null);
//            }
            } else {
                return null;
//            if (scFDS[chipId] == null) return;
//
//            scFDS[chipId].setRegister(dAddr, dData);
            }
        }

        public void setFdsMask(int chipId) {
            FdsInst instrument = context.mds.inst(FdsInst.class);
            if (instrument == null) return; // the song being played does not use this chip
            instrument.setFDSMask(chipId);
        }

        public void resetFdsMask(int chipId) {
            FdsInst instrument = context.mds.inst(FdsInst.class);
            if (instrument == null) return; // the song being played does not use this chip
            instrument.resetFDSMask(chipId);
        }
    }

    private int apuMask = 0;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {NesInst.class};
    }

    // vgm
    public void write(int chipId, int addr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                context.mds.write(inst(chipId), chipId, 0, addr, data);
//            }
        } else {
//            if (scNES[chipId] == null) return;
//
//            scNES[chipId].setRegister(addr, data);
        }
    }

    // vgm
    // TODO getInfo
    public int[] readApu(int chipId, EnmModel model) {
        fireEventHappened("led.on", chipId);

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
                    break;
                case 2:
                case 3:
                case 4:
                    context.chipRegister.chip(NesChip.DmcChip.class).dmcMask |= 1 << (ch - 2);
                    break;
            }
        }
        Instrument instrument = context.mds.inst(NesInst.class);
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
                    break;
                case 2:
                case 3:
                case 4:
                    context.chipRegister.chip(NesChip.DmcChip.class).dmcMask &= ~(1 << (ch - 2));
                    break;
            }
        }
        Instrument instrument = context.mds.inst(NesInst.class);
        if (instrument == null) return; // the song being played does not use this chip
        instrument.resetMask(chipId, ch);
    }

    public boolean getMask(int chipId, int ch) {
        return (apuMask & (1 << ch)) != 0;
    }

    /**
     * The emulator's live APU registers, {@code 0x4000} relative, or null when the song has no
     * NES. Unlike {@link #readApu} this fires no led event: a view polling the state at frame
     * rate would otherwise hold the led on for the whole song.
     */
    public int[] apuRegisters(int chipId) {
        if (context == null) return null;
        NesInst instrument = context.mds.inst(NesInst.class);
        return instrument == null ? null : instrument.readApu(chipId);
    }

    /**
     * The emulator's live triangle, noise and delta PCM registers, {@code 0x4008} relative - the
     * APU proper only carries the two pulses. {@code 0x4015}, which both halves see, lands at
     * {@code 0x0d} here. Null when the song has no NES; fires no led event.
     */
    public int[] dmcRegisters(int chipId) {
        if (context == null) return null;
        NesInst instrument = context.mds.inst(NesInst.class);
        return instrument == null ? null : instrument.readDmc(chipId);
    }

    // vgm
    public void writePcm(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(NesInst.class).writePcm(chipId, vgmBuf, vgmAdr, dataSize, stAdr);

        dumpData(model, "PCMData", vgmAdr, vgmBuf, dataSize);
    }
}
