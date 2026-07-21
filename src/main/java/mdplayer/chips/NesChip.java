/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdsound.Instrument;
import mdsound.instrument.NesInst;
import mdsound.instrument.NesInst.DmcInst;
import mdsound.instrument.NesInst.FdsInst;


/**
 * NsfChip. (for vgm)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class NesChip extends BaseChip {

    // vgm
    public static class DmcChip extends NesChip {
        private int dmcMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {DmcInst.class};
        }

        @Override
        public Map<String, Object> getInfo(int chipId) {
            fireEventHappened("led.on", chipId);

//            if (!ctNES[chipId].UseScci) {
            return Map.of("register", context.mds.inst(NesInst.class).readDmc(chipId));
//            }
        }

        @Override
        protected void setMask(int chipId, int ch, boolean mask, Object... args) {
            if (mask) {
                super.setMask(chipId, ch + 2, true);
            } else {
                super.setMask(chipId, ch + 2, false);
            }
        }

        @Override
        public boolean getMask(int chipId, int ch) {
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

        @Override
        public Map<String, Object> getInfo(int chipId) {
            fireEventHappened("led.on", chipId);

//            if (!ctNES[chipId].UseScci) {
            return context.mds.inst(FdsInst.class).getView(chipId, "info");
//            }
        }

        @Override
        protected void setMask(int chipId, int ch, boolean mask, Object... args) {
            if (mask) {
                FdsInst instrument = context.mds.inst(FdsInst.class);
                if (instrument == null) return; // the song being played does not use this chip
                instrument.setFDSMask(chipId);
            } else  {
                FdsInst instrument = context.mds.inst(FdsInst.class);
                if (instrument == null) return; // the song being played does not use this chip
                instrument.resetFDSMask(chipId);
            }
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
    @Override
    public Map<String, Object> getInfo(int chipId) {
        fireEventHappened("led.on", chipId);

//        if (!ctNES[chipId].UseScci) {
        NesInst inst = context.mds.inst(NesInst.class);
        if (inst == null) return Collections.emptyMap();
        Map<String, Object> info = new HashMap<>();
        info.put("register", inst.readApu(chipId));
        info.put("dmcRegister", inst.readDmc(chipId));
        return info;
//        }
    }

    // vgm
    @Override
    protected void setMask(int chipId, int ch, boolean mask, Object... args) {
        if (mask) {
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
        } else {
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
    }

    @Override
    public boolean getMask(int chipId, int ch) {
        return (apuMask & (1 << ch)) != 0;
    }

    // vgm
    public void writePcm(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(NesInst.class).writePcm(chipId, vgmBuf, vgmAdr, dataSize, stAdr);

        dumpData(model, "PCMData", vgmAdr, vgmBuf, dataSize);
    }
}
