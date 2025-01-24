/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.instrument.MameYm2612Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;


/**
 * Ym2612Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2612Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = {
            setting.getYM2612Type()[0], setting.getYM2612Type()[1]
    };
    private final RSoundChip[] realChips = {null, null};

    public int[][][] register = {
            {null, null},
            {null, null}
    };
    public int[][] keyOn = {null, null};
    public int[][] volume = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };
    public int[][] ch3SlotVolume = {new int[4], new int[4]};
    private final int[] fadeout = {0, 0};
    private final boolean[][] mask = {
            {false, false, false, false, false, false},
            {false, false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                register[chipId][0][i] = 0; // -1;
                register[chipId][1][i] = 0; // -1;
            }
            register[chipId][0][0xb4] = 0xc0;
            register[chipId][0][0xb5] = 0xc0;
            register[chipId][0][0xb6] = 0xc0;
            register[chipId][1][0xb4] = 0xc0;
            register[chipId][1][0xb5] = 0xc0;
            register[chipId][1][0xb6] = 0xc0;
            keyOn[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fadeout[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    write(chipId, p, 0x40 + c, 127, EnmModel.RealModel, -1);
                    write(chipId, p, 0x44 + c, 127, EnmModel.RealModel, -1);
                    write(chipId, p, 0x48 + c, 127, EnmModel.RealModel, -1);
                    write(chipId, p, 0x4c + c, 127, EnmModel.RealModel, -1);
                }
            }
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 9; i++) {
                if (volume[chipId][i] > 0) {
                    volume[chipId][i] -= 50;
                    if (volume[chipId][i] < 0)
                        volume[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (ch3SlotVolume[chipId][i] > 0) {
                    ch3SlotVolume[chipId][i] -= 50;
                    if (ch3SlotVolume[chipId][i] < 0)
                        ch3SlotVolume[chipId][i] = 0;
                }
            }
        }
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model, int frameCounter) {
        if (addr < 0 || data < 0) return;

        if (chipId == 0) context.chipLED.put("PriOPN2", 2);
        else context.chipLED.put("SecOPN2", 2);

        if (model == EnmModel.VirtualModel) {
            register[chipId][port][addr] = data;
            context.plugin(MidiPlugin.class).export.outMIDIData(EnmChip.YM2612, chipId, port, addr, data, 0, frameCounter);
        }

        if ((model == EnmModel.RealModel && chipTypes[chipId].getUseReal()[0]) || (model == EnmModel.VirtualModel && !chipTypes[chipId].getUseReal()[0])) {
            //register[port][addr] = data;
            if (port == 0 && addr == 0x28) {
                int ch = (data & 0x3) + ((data & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6) /* && (data & 0xf0) > 0) */ {

                    if (ch != 2 || (register[chipId][0][0x27] & 0xc0) != 0x40) {
                        if (ch != 5 || (register[chipId][0][0x2b] & 0x80) == 0) {
                            if ((data & 0xf0) != 0) {
                                keyOn[chipId][ch] = (data & 0xf0) | 1;
                                volume[chipId][ch] = 256 * 6;
                            } else {
                                keyOn[chipId][ch] = (data & 0xf0) | 0;
                            }
                        }
                    } else {
                        keyOn[chipId][2] = (data & 0xf0);
                        if ((data & 0x10) > 0) ch3SlotVolume[chipId][0] = 256 * 6;
                        if ((data & 0x20) > 0) ch3SlotVolume[chipId][1] = 256 * 6;
                        if ((data & 0x40) > 0) ch3SlotVolume[chipId][2] = 256 * 6;
                        if ((data & 0x80) > 0) ch3SlotVolume[chipId][3] = 256 * 6;
                    }
                }
            }

            // PCM
            if ((register[chipId][0][0x2b] & 0x80) > 0) {
                if (register[chipId][0][0x2a] > 0) {
                    volume[chipId][5] = Math.abs(register[chipId][0][0x2a] - 0x7f) * 20;
                }
            }
        }

        if ((addr & 0xf0) == 0x40) { // TL
            int ch = (addr & 0x3);
            int slot = (addr & 0xc) >> 2;
            int al = register[chipId][port][0xb0 + ch] & 0x07;
            data &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    data = Math.min(data + fadeout[chipId], 127);
                    data = mask[chipId][port * 3 + ch] ? 127 : data;
                }
            }
        }

        if ((addr & 0xf0) == 0xb0) { // AL
            int ch = (addr & 0x3);
            int al = data & 0x07; // AL

            if (ch != 3 && mask[chipId][port * 3 + ch]) {
                // Reconfigure the carrier's TL
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        write(
                                chipId
                                , port
                                , 0x40 + ch + tslot
                                , register[chipId][port][0x40 + ch + tslot]
                                , model
                                , frameCounter);
                    }
                }
            }
        }

        if (addr == 0x2a) {
            // Masking PCM data
            if (mask[chipId][5]) data = 0x00;
//logger.log(Level.TRACE, "%02x".formatted(data));
        }

        if (model == EnmModel.VirtualModel) {

            // Virtual Sound Source Processing

            if (chipTypes[chipId].getUseReal()[0]) {
                // When using Scci,
                // only PCM (6Ch) is played by emulator
                if (chipTypes[chipId].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                    if (port == 0 && addr == 0x2b) {
                        //if (chipTypes[chipId].getUseEmu()[0])
                        context.mds.write(Ym2612Inst.class, chipId, port, addr, data);
                        //if (chipTypes[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, port, addr, data);
                        //if (chipTypes[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, port, addr, data);
                    } else if (port == 0 && addr == 0x2a) {
                        //if (chipTypes[chipId].getUseEmu()[0])
                        context.mds.write(Ym2612Inst.class, chipId, port, addr, data);
                        //if (chipTypes[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, port, addr, data);
                        //if (chipTypes[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, port, addr, data);
                    } else if (port == 1 && addr == 0xb6) {
                        //if (chipTypes[chipId].getUseEmu()[0])
                        context.mds.write(Ym2612Inst.class, chipId, port, addr, data);
                        //if (chipTypes[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, port, addr, data);
                        //if (chipTypes[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, port, addr, data);
                    }
                }
            } else {
//#if DEBUG
//                if (addr == 0x2a || addr == 0x2b) return; // DAC
//                if (port == 1) return; // port1
//                if (addr == 0x28 && (data & 7) == 0) return; // Ch1Keyon/off
//                if (addr == 0x28 && (data & 7) == 1) return; // Ch2Keyon/off
//                if (addr == 0x28 && (data & 7) == 2) return; // Ch3Keyon/off
//                if (addr == 0x28 && (data & 7) == 4) return; // Ch4Keyon/off
//                if (addr == 0x28 && (data & 7) == 5) return; // Ch5Keyon/off
//                if (addr == 0x28 && (data & 7) == 6) return; // Ch6Keyon/off
//                if ((addr & 0xf0) == 0x30) return; // DTMUL cancel
//                if ((addr & 0xf0) == 0x40) return; // TL cancel
//                if ((addr & 0xf0) == 0x50) return; // TL cancel
//                if ((addr & 0xf0) == 0x60) return; // TL cancel
//                if ((addr & 0xf0) == 0x70) return; // TL cancel
//                if ((addr & 0xf0) == 0x80) return; // TL cancel
//                if ((addr & 0xf0) == 0x90) return; // TL cancel
//                if (addr >= 0x00 && addr < 0x22) return; // cancel various
//                if (addr >= 0xb4) return; // TL cancel
//                return;
//#endif

                if (chipTypes[chipId].getUseEmu()[1] && addr == 0x21)
                    return; // Cancel sending data to the TEST register

                // Send data to MDSound only when using the emulator
//logger.log(Level.TRACE, "setYM2612: chipId: %d, port: %02X, addr: %02X, data: %02X".formatted(chipId, port, addr, data));
                if (chipTypes[chipId].getUseEmu()[0])
                    context.mds.write(Ym2612Inst.class, chipId, port, addr, data);
                if (chipTypes[chipId].getUseEmu()[1])
                    context.mds.write(Ym3438Inst.class, chipId, port, addr, data);
                if (chipTypes[chipId].getUseEmu()[2])
                    context.mds.write(MameYm2612Inst.class, chipId, port, addr, data);
            }
        } else {

            // Real sound source (Scci)

            if (realChips[chipId] == null) return;

            // When playing only PCM (6Ch) with an emulator
            if (chipTypes[chipId].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                // Check the address and do not send data to the PCM
                if (port == 0 && addr == 0x2b) {
                    realChips[chipId].setRegister(port * 0x100 + addr, data);
                } else if (port == 0 && addr == 0x2a) {
                } else {
                    realChips[chipId].setRegister(port * 0x100 + addr, data);
                }
            } else {
                // Send data to Scci
                realChips[chipId].setRegister(port * 0x100 + addr, data);
            }
        }
    }

    private void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.VirtualModel, -1);
        write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.VirtualModel, -1);
        write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.VirtualModel, -1);
        write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.VirtualModel, -1);

        write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.RealModel, -1);
        write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.RealModel, -1);
        write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.RealModel, -1);
        write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.RealModel, -1);

        if (mask)
            context.mds.inst(Ym2612Inst.class).setMask(chipId, ch);
        else
            context.mds.inst(Ym2612Inst.class).resetMask(chipId, ch);
    }

    public void setSyncWait(int chipId, int wait) {
        if (realChips[chipId] != null && chipTypes[chipId].getRealChipInfo()[0].getUseWait()) {
            realChips[chipId].setRegister(-1, (int) (wait * (chipTypes[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void setFadeout(int chipId, int v) {
        fadeout[chipId] = v;
        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.RealModel, -1);
                write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.RealModel, -1);
                write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.RealModel, -1);
                write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.RealModel, -1);
            }
        }
    }

    public int[] getVolume(int chipId) {
        return volume[chipId];
    }

    public int[] getCh3SlotVolume(int chipId) {
        return ch3SlotVolume[chipId];
    }

    public int[][] read(int chipId) {
        return register[chipId];
    }

    public int[] getKeyOn(int chipId) {
        return keyOn[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
            setMask(chipId, ch, false);
    }

    @Override
    public void clearFadeout() {
        setFadeout(0, 0);
        setFadeout(1, 0);
    }
}
