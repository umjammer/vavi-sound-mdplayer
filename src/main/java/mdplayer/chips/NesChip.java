/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;
import mdsound.instrument.IntFNesInst;
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


/**
 * NsfChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class NesChip implements Chip {

    private int nsfAPUmask = 0;
    private int nsfDMCmask = 0;
    private int nsfFDSmask = 0;
    private int nsfMMC5mask = 0;
    private int nsfVRC6mask = 0;
    private int nsfVRC7mask = 0;
    private int nsfN163mask = 0;

    public NesBank nes_bank = null;
    public NesMem nes_mem = null;
    public Km6502 nes_cpu = null;
    public NesApu nes_apu = null;
    public NesDmc nes_dmc = null;
    public NesFds nes_fds = null;
    public NesN106 nes_n106 = null;
    public NesVrc6 nes_vrc6 = null;
    public NesMmc5 nes_mmc5 = null;
    public NesFme7 nes_fme7 = null;
    public NesVrc7 nes_vrc7 = null;

    private final ChipKeyInfo[] kiVRC7 = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private final ChipKeyInfo[] kiVRC7ret = {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setNESRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            context.mds.write(IntFNesInst.class, chipId, 0, dAddr, dData);
            // }
        } else {
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public byte[] getNESRegisterAPU(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            return context.mds.ReadNESapu(chipId);
            // }
        } else {
            return null;
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public byte[] getNESRegisterDMC(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            return context.mds.ReadNESdmc(chipId);
            // }
        } else {
            return null;
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public mdsound.np.NpNesFds getFDSRegister(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriFDS", 2);
        else
            context.chipLED.put("SecFDS", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            return context.mds.readFDS(chipId);
            // }
        } else {
            return null;
            // if (scFDS[chipId] == null) return;

            // scFDS[chipId].setRegister(dAddr, dData);
        }
    }

    public NesMmc5 getMMC5Register(int chipId, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriMMC5", 2);
        else
            context.chipLED.put("SecMMC5", 2);

        if (model == EnmModel.VirtualModel) {
            return null;// mds.readMMC5((byte)chipId);
        } else {
            return null;
        }
    }

    public ChipKeyInfo getVRC7KeyInfo(int chipId) {
        if (nes_vrc7 == null)
            return null;
        if (chipId != 0)
            return null;

        NesVrc7.ChipKeyInfo ki = nes_vrc7.getKeyInfo(chipId);

        for (int ch = 0; ch < 6; ch++) {
            kiVRC7ret[chipId].on[ch] = ki.on[ch];
            kiVRC7ret[chipId].off[ch] = ki.off[ch];
        }
        return kiVRC7ret[chipId];
    }

    public void setNESMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
                case 0:
                case 1:
                    nsfAPUmask |= 1 << ch;
                    if (nes_apu != null)
                        nes_apu.setMask(nsfAPUmask);
                    break;
                case 2:
                case 3:
                case 4:
                    nsfDMCmask |= 1 << (ch - 2);
                    if (nes_dmc != null)
                        nes_dmc.setMask(nsfDMCmask);
                    break;
            }
        }
        context.mds.setNESMask(chipId, ch);
    }

    public void resetNESMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
                case 0:
                case 1:
                    nsfAPUmask &= ~(1 << ch);
                    if (nes_apu != null)
                        nes_apu.setMask(nsfAPUmask);
                    break;
                case 2:
                case 3:
                case 4:
                    nsfDMCmask &= ~(1 << (ch - 2));
                    if (nes_dmc != null)
                        nes_dmc.setMask(nsfDMCmask);
                    break;
            }
        }
        context.mds.resetNESMask(chipId, ch);
    }

    public void setFDSMask(int chipId) {
        nsfFDSmask |= 1;
        if (nes_fds != null)
            nes_fds.setMask(nsfFDSmask);
        context.mds.setFDSMask(chipId);
    }

    public void resetFDSMask(int chipId) {
        nsfFDSmask &= ~1;
        if (nes_fds != null)
            nes_fds.setMask(nsfFDSmask);
        context.mds.resetFDSMask(chipId);
    }

    public void setMMC5Mask(int chipId, int ch) {
        nsfMMC5mask |= 1 << ch;
        if (nes_mmc5 != null)
            nes_mmc5.setMask(nsfMMC5mask);
    }

    public void resetMMC5Mask(int chipId, int ch) {
        nsfMMC5mask &= ~(1 << ch);
        if (nes_mmc5 != null)
            nes_mmc5.setMask(nsfMMC5mask);
    }

    public void setVRC7Mask(int chipId, int ch) {
        nsfVRC7mask |= 1 << ch;
        if (nes_vrc7 != null)
            nes_vrc7.setMask(nsfVRC7mask);
    }

    public void resetVRC7Mask(int chipId, int ch) {
        nsfVRC7mask &= ~(1 << ch);
        if (nes_vrc7 != null)
            nes_vrc7.setMask(nsfVRC7mask);
    }

    public void setVRC6Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfVRC6mask |= 1 << ch;
        if (nes_vrc6 != null)
            nes_vrc6.setMask(nsfVRC6mask);
    }

    public void resetVRC6Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfVRC6mask &= ~(1 << ch);
        if (nes_vrc6 != null)
            nes_vrc6.setMask(nsfVRC6mask);
    }

    public void setN163Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfN163mask |= 1 << ch;
        if (nes_n106 != null)
            nes_n106.setMask(nsfN163mask);
    }

    public void resetN163Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfN163mask &= ~(1 << ch);
        if (nes_n106 != null)
            nes_n106.setMask(nsfN163mask);
    }

    public void writeNESPCMData(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriNES", 2);
        else
            context.chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteNESRam(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public DeviceInfo.TrackInfo[] getVRC6Register(int chipId) {
        if (nes_vrc6 == null)
            return null;
        if (chipId != 0)
            return null;

        return nes_vrc6.getTracksInfo();
    }

    public byte[] getVRC7Register(int chipId) {
        if (nes_vrc7 == null) return null;
        if (chipId != 0) return null;

        return nes_vrc7.getRegs();
    }

    public DeviceInfo.TrackInfo[] getN106Register(int chipId) {
        if (nes_n106 == null)
            return null;
        if (chipId != 0)
            return null;

        return nes_n106.getTracksInfo();
    }
}
