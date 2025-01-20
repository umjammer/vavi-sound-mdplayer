/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.MameYm2612Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;

import static java.lang.System.getLogger;


/**
 * Ym2612Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2612Chip implements Chip {

    private static final Logger logger = getLogger(Ym2612Chip.class.getName());

    private final Setting.ChipType2[] ctYM2612 = new Setting.ChipType2[] {
            setting.getYM2612Type()[0], setting.getYM2612Type()[1]
    };
    private final RSoundChip[] scYM2612 = {null, null};

    public int[][][] fmRegisterYM2612 = {
            {null, null},
            {null, null}
    };
    public int[][] fmKeyOnYM2612 = {null, null};
    public int[][] fmVolYM2612 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };
    public int[][] fmCh3SlotVolYM2612 = {new int[4], new int[4]};
    private final int[] nowYM2612FadeoutVol = {0, 0};
    private final boolean[][] maskFMChYM2612 = {
            {false, false, false, false, false, false},
            {false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM2612[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2612[chipId][0][i] = 0; // -1;
                fmRegisterYM2612[chipId][1][i] = 0; // -1;
            }
            fmRegisterYM2612[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2612[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2612[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2612[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            nowYM2612FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    setYM2612Register(chipId, p, 0x40 + c, 127, EnmModel.RealModel, -1);
                    setYM2612Register(chipId, p, 0x44 + c, 127, EnmModel.RealModel, -1);
                    setYM2612Register(chipId, p, 0x48 + c, 127, EnmModel.RealModel, -1);
                    setYM2612Register(chipId, p, 0x4c + c, 127, EnmModel.RealModel, -1);
                }
            }
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 9; i++) {
                if (fmVolYM2612[chipId][i] > 0) {
                    fmVolYM2612[chipId][i] -= 50;
                    if (fmVolYM2612[chipId][i] < 0)
                        fmVolYM2612[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2612[chipId][i] > 0) {
                    fmCh3SlotVolYM2612[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2612[chipId][i] < 0)
                        fmCh3SlotVolYM2612[chipId][i] = 0;
                }
            }
        }
    }

    public void setYM2612Register(int chipId, int dPort, int dAddr, int dData, EnmModel model, int vgmFrameCounter) {
        if (dAddr < 0 || dData < 0) return;

        if (chipId == 0) context.chipLED.put("PriOPN2", 2);
        else context.chipLED.put("SecOPN2", 2);

        if (model == EnmModel.VirtualModel) {
            fmRegisterYM2612[chipId][dPort][dAddr] = dData;
            context.plugin(MidiPlugin.class).midiExport.outMIDIData(EnmChip.YM2612, chipId, dPort, dAddr, dData, 0, vgmFrameCounter);
        }

        if ((model == EnmModel.RealModel && ctYM2612[chipId].getUseReal()[0]) || (model == EnmModel.VirtualModel && !ctYM2612[chipId].getUseReal()[0])) {
            //fmRegister[dPort][dAddr] = dData;
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6) /* && (dData & 0xf0) > 0) */ {

                    if (ch != 2 || (fmRegisterYM2612[chipId][0][0x27] & 0xc0) != 0x40) {
                        if (ch != 5 || (fmRegisterYM2612[chipId][0][0x2b] & 0x80) == 0) {
                            if ((dData & 0xf0) != 0) {
                                fmKeyOnYM2612[chipId][ch] = (dData & 0xf0) | 1;
                                fmVolYM2612[chipId][ch] = 256 * 6;
                            } else {
                                fmKeyOnYM2612[chipId][ch] = (dData & 0xf0) | 0;
                            }
                        }
                    } else {
                        fmKeyOnYM2612[chipId][2] = (dData & 0xf0);
                        if ((dData & 0x10) > 0) fmCh3SlotVolYM2612[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0) fmCh3SlotVolYM2612[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0) fmCh3SlotVolYM2612[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0) fmCh3SlotVolYM2612[chipId][3] = 256 * 6;
                    }
                }
            }

            // PCM
            if ((fmRegisterYM2612[chipId][0][0x2b] & 0x80) > 0) {
                if (fmRegisterYM2612[chipId][0][0x2a] > 0) {
                    fmVolYM2612[chipId][5] = Math.abs(fmRegisterYM2612[chipId][0][0x2a] - 0x7f) * 20;
                }
            }
        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = fmRegisterYM2612[chipId][dPort][0xb0 + ch] & 0x07;
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    dData = Math.min(dData + nowYM2612FadeoutVol[chipId], 127);
                    dData = maskFMChYM2612[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) { // AL
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && maskFMChYM2612[chipId][dPort * 3 + ch]) {
                // Reconfigure the carrier's TL
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2612Register(
                                chipId
                                , dPort
                                , 0x40 + ch + tslot
                                , fmRegisterYM2612[chipId][dPort][0x40 + ch + tslot]
                                , model
                                , vgmFrameCounter);
                    }
                }
            }
        }

        if (dAddr == 0x2a) {
            // Masking PCM data
            if (maskFMChYM2612[chipId][5]) dData = 0x00;
//logger.log(Level.TRACE, "%02x".formatted(dData));
        }

        if (model == EnmModel.VirtualModel) {

            // Virtual Sound Source Processing

            if (ctYM2612[chipId].getUseReal()[0]) {
                // When using Scci,
                // only PCM (6Ch) is played by emulator
                if (ctYM2612[chipId].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                    if (dPort == 0 && dAddr == 0x2b) {
                        //if (ctYM2612[chipId].getUseEmu()[0])
                        context.mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, dPort, dAddr, dData);
                    } else if (dPort == 0 && dAddr == 0x2a) {
                        //if (ctYM2612[chipId].getUseEmu()[0])
                        context.mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, dPort, dAddr, dData);
                    } else if (dPort == 1 && dAddr == 0xb6) {
                        //if (ctYM2612[chipId].getUseEmu()[0])
                        context.mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, dPort, dAddr, dData);
                    }
                }
            } else {
//#if DEBUG
//                if (dAddr == 0x2a || dAddr == 0x2b) return; // DAC
//                if (dPort == 1) return; // port1
//                if (dAddr == 0x28 && (dData & 7) == 0) return; // Ch1Keyon/off
//                if (dAddr == 0x28 && (dData & 7) == 1) return; // Ch2Keyon/off
//                if (dAddr == 0x28 && (dData & 7) == 2) return; // Ch3Keyon/off
//                if (dAddr == 0x28 && (dData & 7) == 4) return; // Ch4Keyon/off
//                if (dAddr == 0x28 && (dData & 7) == 5) return; // Ch5Keyon/off
//                if (dAddr == 0x28 && (dData & 7) == 6) return; // Ch6Keyon/off
//                if ((dAddr & 0xf0) == 0x30) return; // DTMUL cancel
//                if ((dAddr & 0xf0) == 0x40) return; // TL cancel
//                if ((dAddr & 0xf0) == 0x50) return; // TL cancel
//                if ((dAddr & 0xf0) == 0x60) return; // TL cancel
//                if ((dAddr & 0xf0) == 0x70) return; // TL cancel
//                if ((dAddr & 0xf0) == 0x80) return; // TL cancel
//                if ((dAddr & 0xf0) == 0x90) return; // TL cancel
//                if (dAddr >= 0x00 && dAddr < 0x22) return; // cancel various
//                if (dAddr >= 0xb4) return; // TL cancel
//                return;
//#endif

                if (ctYM2612[chipId].getUseEmu()[1] && dAddr == 0x21)
                    return; // Cancel sending data to the TEST register

                // Send data to MDSound only when using the emulator
//logger.log(Level.TRACE, "setYM2612: chipId: %d, port: %02X, addr: %02X, data: %02X".formatted(chipId, dPort, dAddr, dData));
                if (ctYM2612[chipId].getUseEmu()[0])
                    context.mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                if (ctYM2612[chipId].getUseEmu()[1])
                    context.mds.write(Ym3438Inst.class, chipId, dPort, dAddr, dData);
                if (ctYM2612[chipId].getUseEmu()[2])
                    context.mds.write(MameYm2612Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {

            // Real sound source (Scci)

            if (scYM2612[chipId] == null) return;

            // When playing only PCM (6Ch) with an emulator
            if (ctYM2612[chipId].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                // Check the address and do not send data to the PCM
                if (dPort == 0 && dAddr == 0x2b) {
                    scYM2612[chipId].setRegister(dPort * 0x100 + dAddr, dData);
                } else if (dPort == 0 && dAddr == 0x2a) {
                } else {
                    scYM2612[chipId].setRegister(dPort * 0x100 + dAddr, dData);
                }
            } else {
                // Send data to Scci
                scYM2612[chipId].setRegister(dPort * 0x100 + dAddr, dData);
            }
        }
    }

    public void setMaskYM2612(int chipId, int ch, boolean mask) {
        maskFMChYM2612[chipId][ch] = mask;

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        setYM2612Register(chipId, p, 0x40 + c, fmRegisterYM2612[chipId][p][0x40 + c], EnmModel.VirtualModel, -1);
        setYM2612Register(chipId, p, 0x44 + c, fmRegisterYM2612[chipId][p][0x44 + c], EnmModel.VirtualModel, -1);
        setYM2612Register(chipId, p, 0x48 + c, fmRegisterYM2612[chipId][p][0x48 + c], EnmModel.VirtualModel, -1);
        setYM2612Register(chipId, p, 0x4c + c, fmRegisterYM2612[chipId][p][0x4c + c], EnmModel.VirtualModel, -1);

        setYM2612Register(chipId, p, 0x40 + c, fmRegisterYM2612[chipId][p][0x40 + c], EnmModel.RealModel, -1);
        setYM2612Register(chipId, p, 0x44 + c, fmRegisterYM2612[chipId][p][0x44 + c], EnmModel.RealModel, -1);
        setYM2612Register(chipId, p, 0x48 + c, fmRegisterYM2612[chipId][p][0x48 + c], EnmModel.RealModel, -1);
        setYM2612Register(chipId, p, 0x4c + c, fmRegisterYM2612[chipId][p][0x4c + c], EnmModel.RealModel, -1);

        if (mask)
            context.mds.setYm2612Mask(chipId, ch);
        else
            context.mds.resetYm2612Mask(chipId, ch);
    }

    public void setYM2612SyncWait(int chipId, int wait) {
        if (scYM2612[chipId] != null && ctYM2612[chipId].getRealChipInfo()[0].getUseWait()) {
            scYM2612[chipId].setRegister(-1, (int) (wait * (ctYM2612[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void setFadeoutVolYM2612(int chipId, int v) {
        nowYM2612FadeoutVol[chipId] = v;
        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                setYM2612Register(chipId, p, 0x40 + c, fmRegisterYM2612[chipId][p][0x40 + c], EnmModel.RealModel, -1);
                setYM2612Register(chipId, p, 0x44 + c, fmRegisterYM2612[chipId][p][0x44 + c], EnmModel.RealModel, -1);
                setYM2612Register(chipId, p, 0x48 + c, fmRegisterYM2612[chipId][p][0x48 + c], EnmModel.RealModel, -1);
                setYM2612Register(chipId, p, 0x4c + c, fmRegisterYM2612[chipId][p][0x4c + c], EnmModel.RealModel, -1);
            }
        }
    }

    public int[] getYM2612Volume(int chipId) {
        return fmVolYM2612[chipId];
    }

    public int[] getYM2612Ch3SlotVolume(int chipId) {
        return fmCh3SlotVolYM2612[chipId];
    }

    public int[][] getFMRegister(int chipId) {
        return fmRegisterYM2612[chipId];
    }

    public int[] getFMKeyOn(int chipId) {
        return fmKeyOnYM2612[chipId];
    }

    public int[] getFMVolume(int chipId) {
        return getYM2612Volume(chipId);
    }

    public int[] getFMCh3SlotVolume(int chipId) {
        return getYM2612Ch3SlotVolume(chipId);
    }

    public void setYM2612Mask(int chipId, int ch) {
        setMaskYM2612(chipId, ch, true);
    }

    public void resetYM2612Mask(int chipId, int ch) {
        try {
            setMaskYM2612(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void clearFadeoutVolume() {
        setFadeoutVolYM2612(0, 0);
        setFadeoutVolYM2612(1, 0);
    }
}
