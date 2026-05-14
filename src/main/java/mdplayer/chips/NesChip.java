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
            return new Class[] {DmcInst.class};
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

    // vgm
    public static class FdsChip extends NesChip {
        private int fdsMask = 0;

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Instrument>[] implementations() {
            return new Class[] {FdsInst.class};
        }

        public Map<String, Object> readFds(int chipId, EnmModel model) {
            if (chipId == 0)
                context.chipLED.put("PriFDS", 2);
            else
                context.chipLED.put("SecFDS", 2);

            if (model == EnmModel.VirtualModel) {
//            if (!ctNES[chipId].UseScci) {
                return context.mds.inst(FdsInst.class).getInfo(chipId);
//            }
            } else {
                return null;
//            if (scFDS[chipId] == null) return;
//
//            scFDS[chipId].setRegister(dAddr, dData);
            }
        }

        public void setFdsMask(int chipId) {
            context.mds.inst(FdsInst.class).setFDSMask(chipId);
        }

        public void resetFdsMask(int chipId) {
            context.mds.inst(FdsInst.class).resetFDSMask(chipId);
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
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

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
                    break;
                case 2:
                case 3:
                case 4:
                    context.chipRegister.chip(NesChip.DmcChip.class).dmcMask |= 1 << (ch - 2);
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
                    break;
                case 2:
                case 3:
                case 4:
                    context.chipRegister.chip(NesChip.DmcChip.class).dmcMask &= ~(1 << (ch - 2));
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

        dumpData(model, "PCMData", vgmAdr, vgmBuf, dataSize);
    }
}
