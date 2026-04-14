/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.Tables;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.YmFmYm2151Inst;


/**
 * Ym2151Chip.
 * <p>
 * system property
 * <li>{@code mdplayer.variant.ym2151} ... active chip index</li>
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2151Chip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getYM2151Type();

    private final RSoundChip[] realChips = {null, null};

    public final int[][] register = {null, null};
    public final int[][] keyOn = {null, null};
    public final int[][] volume = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private final int[] fadeout = {0, 0};
    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };
    public final int[] amd = {-1, -1};
    public final int[] pmd = {-1, -1};

    private final boolean[] use4MYM2151scci = {false, false};

    public boolean[] getUse4MYM2151scci() {
        return use4MYM2151scci;
    }

    public final int[] corrections = {0, 0};

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ym2151Inst.class, MameYm2151Inst.class, X68kYm2151Inst.class, YmFmYm2151Inst.class};
    }

    @Override
    public int activeIndex(int chipId) {
        return chipTypes[chipId].getEnabledId();
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                register[chipId][i] = 0;
            }
            keyOn[chipId] = new int[] {0, 0, 0, 0, 0, 0, 0, 0};

            fadeout[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int c = 0; c < 8; c++) {
                write(chipId, 0, 0x60 + c, 127, EnmModel.RealModel, 0, -1);
                write(chipId, 0, 0x68 + c, 127, EnmModel.RealModel, 0, -1);
                write(chipId, 0, 0x70 + c, 127, EnmModel.RealModel, 0, -1);
                write(chipId, 0, 0x78 + c, 127, EnmModel.RealModel, 0, -1);
            }
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 8; i++) {
                if (volume[chipId][i] > 0) {
                    volume[chipId][i] -= 50;
                    if (volume[chipId][i] < 0)
                        volume[chipId][i] = 0;
                }
            }
        }
    }

    public void write(int chipId,
                      int port,
                      int addr,
                      int data,
                      EnmModel model,
                      int hosei,
                      long frameCounter) {
        int correction = hosei == 0 ? this.corrections[0] : this.corrections[1];

        if (chipId == 0)
            context.chipLED.put("PriOPM", 2);
        else
            context.chipLED.put("SecOPM", 2);

        if ((model == EnmModel.VirtualModel && (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (realChips != null && realChips[chipId] != null))) {
            register[chipId][addr] = data;
            context.chipRegister.plugin(MidiPlugin.class).export.outMIDIData(Ym2151Chip.class, chipId, port, addr, data, correction, frameCounter);
        }

        if ((model == EnmModel.RealModel && chipTypes[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !chipTypes[chipId].getUseReal()[0])) {
            if (addr == 0x08) { // Key-On/Off
                int ch = data & 0x7;
                if (ch >= 0 && ch < 8) {
                    if ((data & 0x78) != 0) {
                        int con = data & 0x78;
                        keyOn[chipId][ch] = con | 1;
                        volume[chipId][ch] = 256 * 6;
                    } else {
                        keyOn[chipId][ch] &= 0xfe;
                    }
                }
            }
        }

        // AMD/PMD
        if (addr == 0x19) {
            if ((data & 0x80) != 0) {
                pmd[chipId] = data & 0x7f;
            } else {
                amd[chipId] = data & 0x7f;
            }
        }

        if ((addr & 0xf8) == 0x20) {
            int al = data & 0x07; // AL
            int ch = addr & 0x7;

            for (int i = 0; i < 4; i++) {
                int slot = (i == 0) ? 0 : ((i == 1) ? 2 : ((i == 2) ? 1 : 3));
                if ((algM[al] & (1 << slot)) > 0) {
                    if (mask[chipId][ch]) {
                        if (model == EnmModel.VirtualModel) {
                            if (!chipTypes[chipId].getUseReal()[0]) {
                                context.mds.write(inst(chipId), chipId, 0, 0x60 + i * 8 + ch, 127);
                            }
                        } else {
                            if (realChips != null && realChips[chipId] != null)
                                realChips[chipId].setRegister(0x60 + i * 8 + ch, 127);
                        }
                    }
                }
            }
        }

        if ((addr & 0xf0) == 0x60 || (addr & 0xf0) == 0x70) { // TL
            int ch = addr & 0x7;
            data &= 0x7f;

            data = Math.min(data + fadeout[chipId], 127);
            data = mask[chipId][ch] ? 127 : data;
        }

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(inst(chipId), chipId, 0, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;

            if (addr >= 0x28 && addr <= 0x2f) {
                if (correction == 0) {
                    realChips[chipId].setRegister(addr, data);
                } else {
                    int oct = (data & 0x70) >> 4;
                    int note = data & 0xf;
                    note = (note < 3) ? note : ((note < 7) ? (note - 1) : ((note < 11) ? (note - 2) : (note - 3)));
                    note += correction - 1;
                    if (note < 0) {
                        oct += (note / 12) - 1;
                        note = (note % 12) + 12;
                    } else {
                        oct += (note / 12);
                        note %= 12;
                    }

                    note = (note < 3) ? note : ((note < 6) ? (note + 1) : ((note < 9) ? (note + 2) : (note + 3)));
                    if (realChips[chipId] != null)
                        realChips[chipId].setRegister(addr, (oct << 4) | note);
                }
            } else {
                realChips[chipId].setRegister(addr, data);
            }
        }
    }

    private void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(inst(chipId), chipId, 0, addr, data);
            }
        } else {
            if (realChips[chipId] != null)
                realChips[chipId].setRegister(addr, data);
        }
    }

    public void softReset(int chipId, EnmModel model) {

        // FM all channel keys off
        for (int i = 0; i < 8; i++) {
            // note off
            write(chipId, 0, 0x08, 0x00 + i, model);
        }

        write(chipId, 0, 0x0f, 0x00, model); // FM NOISE ENABLE/NOISE FREQ
        write(chipId, 0, 0x18, 0x00, model); // FM HW LFO FREQ
        write(chipId, 0, 0x19, 0x80, model); // FM PMD/VALUE
        write(chipId, 0, 0x19, 0x00, model); // FM AMD/VALUE
        write(chipId, 0, 0x1b, 0x00, model); // FM HW LFO WAVEFORM

        // FM HW LFO RESET
        write(chipId, 0, 0x01, 0x02, model);
        write(chipId, 0, 0x01, 0x00, model);

        write(chipId, 0, 0x10, 0x00, model); // FM Timer-a(H)
        write(chipId, 0, 0x11, 0x00, model); // FM Timer-a(L)
        write(chipId, 0, 0x12, 0x00, model); // FM Timer-B
        write(chipId, 0, 0x14, 0x00, model); // FM Timer Control

        for (int i = 0; i < 8; i++) {
            // FB/ALG/PAN
            write(chipId, 0, 0x20 + i, 0x00, model);
            // KC
            write(chipId, 0, 0x28 + i, 0x00, model);
            // KF
            write(chipId, 0, 0x30 + i, 0x00, model);
            // PMS/AMS
            write(chipId, 0, 0x38 + i, 0x00, model);
        }
        for (int i = 0; i < 0x20; i++) {
            // DT1/ML
            write(chipId, 0, 0x40 + i, 0x00, model);
            // TL=127
            write(chipId, 0, 0x60 + i, 0x7f, model);
            // KS/AR
            write(chipId, 0, 0x80 + i, 0x1F, model);
            // AMD/D1R
            write(chipId, 0, 0xa0 + i, 0x00, model);
            // DT2/D2R
            write(chipId, 0, 0xc0 + i, 0x00, model);
            // D1L/RR
            write(chipId, 0, 0xe0 + i, 0x0F, model);
        }
    }

    private void setMask(int chipId, int ch, boolean mask, boolean noSend /* = false */) {
        this.mask[chipId][ch] = mask;

        if (noSend) return;

        write(chipId, 0, 0x60 + ch, register[chipId][0x60 + ch], EnmModel.VirtualModel, 0, -1);
        write(chipId, 0, 0x68 + ch, register[chipId][0x68 + ch], EnmModel.VirtualModel, 0, -1);
        write(chipId, 0, 0x70 + ch, register[chipId][0x70 + ch], EnmModel.VirtualModel, 0, -1);
        write(chipId, 0, 0x78 + ch, register[chipId][0x78 + ch], EnmModel.VirtualModel, 0, -1);

        write(chipId, 0, 0x60 + ch, register[chipId][0x60 + ch], EnmModel.RealModel, 0, -1);
        write(chipId, 0, 0x68 + ch, register[chipId][0x68 + ch], EnmModel.RealModel, 0, -1);
        write(chipId, 0, 0x70 + ch, register[chipId][0x70 + ch], EnmModel.RealModel, 0, -1);
        write(chipId, 0, 0x78 + ch, register[chipId][0x78 + ch], EnmModel.RealModel, 0, -1);
    }

    public void setSyncWait(int chipId, int wait) {
        if (realChips[chipId] != null && chipTypes[chipId].getRealChipInfo()[0].getUseWait()) {
            realChips[chipId].setRegister(-1, (int) (wait * (chipTypes[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void sendData(int chipId, EnmModel model) {
        if (model == EnmModel.VirtualModel)
            return;

        if (realChips[chipId] != null && chipTypes[chipId].getRealChipInfo()[0].getUseWait()) {
            context.chipRegister.plugin(RealChipPlugin.class).realChip.SendData();
            while (!realChips[chipId].isBufferEmpty()) {
            }
        }
    }

    public int getClock(int chipId) {
        if (realChips[chipId] == null)
            return -1;

        return realChips[chipId].dClock;
    }

    public void setFadeout(int chipId, int v) {
        fadeout[chipId] = v;
        for (int c = 0; c < 8; c++) {
            write(chipId, 0, 0x60 + c, register[chipId][0x60 + c], EnmModel.RealModel, 0, -1);
            write(chipId, 0, 0x68 + c, register[chipId][0x68 + c], EnmModel.RealModel, 0, -1);
            write(chipId, 0, 0x70 + c, register[chipId][0x70 + c], EnmModel.RealModel, 0, -1);
            write(chipId, 0, 0x78 + c, register[chipId][0x78 + c], EnmModel.RealModel, 0, -1);
        }
    }

    public void writeClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                realChips[chipId].dClock = realChips[chipId].setMasterClock(clock);
            }
        }
    }

    public int[] getVolume(int chipId) {
        return volume[chipId];
    }

    public int[] read(int chipId) {
        return register[chipId];
    }

    public int[] getKeyOn(int chipId) {
        return keyOn[chipId];
    }

    public int getPmd(int chipId) {
        return pmd[chipId];
    }

    public int getAmd(int chipId) {
        return amd[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true, false);
    }

    public void resetMask(int chipId, int ch, boolean stopped) {
        setMask(chipId, ch, false, stopped);
    }

    @Override
    public void softReset(EnmModel model) {
        softReset(0, model);
        softReset(1, model);
    }

    @Override
    public void clearFadeout() {
        setFadeout(0, 0);
        setFadeout(1, 0);
    }

    public void setCorrection(EnmModel model, float clockValue) {
        for (int chipId = 0; chipId < 2; chipId++) {
            corrections[chipId] = getCorrection(clockValue, 3579545);
            if (model == EnmModel.RealModel) {
                corrections[chipId] = 0;
                int clock = context.chipRegister.chip(Ym2151Chip.class).getClock(chipId);
                if (clock != -1) {
                    corrections[chipId] = getCorrection(clockValue, clock);
                }
            }
        }
    }

    private static int getCorrection(float clockValue, float baseClock) {
        int ret = 0;

        float delta = clockValue / baseClock;
        float d;
        float oldD = Float.MAX_VALUE;
        for (int i = 0; i < Tables.pcmMulTbl.length; i++) {
            d = Math.abs(delta - Tables.pcmMulTbl[i]);
            ret = i;
            if (d > oldD) break;
            oldD = d;
        }
        ret -= 12;

        return ret;
    }
}
