/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.Instrument;


/**
 * Ym2608Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2608Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getYM2608Type();

    private final Class<? extends Instrument>[] inst = new Class[2];

    private final RSoundChip[] realChips = {null, null};

    public final int[][][] register = {
            {null, null},
            {null, null}
    };

    public final int[][] keyOn = {null, null};

    public final int[][] volume = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public final int[][] ch3SlotVolume = {
            new int[4], new int[4]
    };

    public final int[][][] rhythmVolume = {
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]},
            {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}
    };

    public final int[][] adpcmVolume = {new int[2], new int[2]};

    public final int[] adpcmPan = {0, 0};

    private final int[] fadeout = {0, 0};

    private final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private Audio context;

    @Override
    public void init(Audio context) {
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

            //
            inst[chipId] = EnmChip.YM2608.getInstClass(chipTypes[chipId].getEnabledId());
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    write(chipId, p, 0x40 + c, 127, EnmModel.RealModel);
                    write(chipId, p, 0x44 + c, 127, EnmModel.RealModel);
                    write(chipId, p, 0x48 + c, 127, EnmModel.RealModel);
                    write(chipId, p, 0x4c + c, 127, EnmModel.RealModel);
                }
            }

            // ssg
            write(chipId, 0, 0x08, 0, EnmModel.RealModel);
            write(chipId, 0, 0x09, 0, EnmModel.RealModel);
            write(chipId, 0, 0x0a, 0, EnmModel.RealModel);

            // rhythm
            write(chipId, 0, 0x11, 0, EnmModel.RealModel);

            // adpcm
            write(chipId, 1, 0x0b, 0, EnmModel.RealModel);
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
            for (int i = 0; i < 6; i++) {
                if (rhythmVolume[chipId][i][0] > 0) {
                    rhythmVolume[chipId][i][0] -= 50;
                    if (rhythmVolume[chipId][i][0] < 0)
                        rhythmVolume[chipId][i][0] = 0;
                }
                if (rhythmVolume[chipId][i][1] > 0) {
                    rhythmVolume[chipId][i][1] -= 50;
                    if (rhythmVolume[chipId][i][1] < 0)
                        rhythmVolume[chipId][i][1] = 0;
                }
            }

            if (adpcmVolume[chipId][0] > 0) {
                adpcmVolume[chipId][0] -= 50;
                if (adpcmVolume[chipId][0] < 0)
                    adpcmVolume[chipId][0] = 0;
            }
            if (adpcmVolume[chipId][1] > 0) {
                adpcmVolume[chipId][1] -= 50;
                if (adpcmVolume[chipId][1] < 0)
                    adpcmVolume[chipId][1] = 0;
            }
        }
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
//if (chipId == 0 && port == 1 && addr == 0x01) {
// logger.log(Level.TRACE, "FM P1 Out:Adr[%02x] val[%02x]".formatted((int) addr, (int) data));
//}
        if (addr < 0 || data < 0)
            return;

        if (chipId == 0)
            context.chipLED.put("PriOPNA", 2);
        else
            context.chipLED.put("SecOPNA", 2);

        if ((model == EnmModel.VirtualModel && (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (realChips != null && realChips[chipId] != null))) {
            if (port == 0 && (addr == 0x2d || addr == 0x2e || addr == 0x2f)) {
                register[chipId][0][0x2d] = addr - 0x2d;
            } else {
                register[chipId][port][addr] = data;
            }
        }

        if ((model == EnmModel.RealModel && chipTypes[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !chipTypes[chipId].getUseReal()[0])) {
            if (port == 0 && addr == 0x28) {
                int ch = (data & 0x3) + ((data & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6) /* && (data & 0xf0) > 0) */ {
                    if (ch != 2 || (register[chipId][0][0x27] & 0xc0) != 0x40) {
                        if ((data & 0xf0) != 0) {
                            keyOn[chipId][ch] = (data & 0xf0) | 1;
                            volume[chipId][ch] = 256 * 6;
                        } else {
                            keyOn[chipId][ch] = (data & 0xf0) | 0;
                        }
                    } else {
                        keyOn[chipId][2] = data & 0xf0;
                        if ((data & 0x10) > 0)
                            ch3SlotVolume[chipId][0] = 256 * 6;
                        if ((data & 0x20) > 0)
                            ch3SlotVolume[chipId][1] = 256 * 6;
                        if ((data & 0x40) > 0)
                            ch3SlotVolume[chipId][2] = 256 * 6;
                        if ((data & 0x80) > 0)
                            ch3SlotVolume[chipId][3] = 256 * 6;
                    }
                }
            }

            if (port == 1 && addr == 0x01) {
                adpcmPan[chipId] = (data & 0xc0) >> 6;
                if (adpcmPan[chipId] > 0) {
                    adpcmVolume[chipId][0] = (int) ((256 * 6.0 * register[chipId][1][0x0b] / 64.0) *
                            ((adpcmPan[chipId] & 0x02) > 0 ? 1 : 0));
                    adpcmVolume[chipId][1] = (int) ((256 * 6.0 * register[chipId][1][0x0b] / 64.0) *
                            ((adpcmPan[chipId] & 0x01) > 0 ? 1 : 0));
                }
            }

            if (port == 0 && addr == 0x10) {
                int tl = register[chipId][0][0x11] & 0x3f;
                for (int i = 0; i < 6; i++) {
                    if ((data & (0x1 << i)) != 0) {
                        int il = (register[chipId][0][0x18 + i] & 0x1f) * (((data & 0x80) == 0) ? 1 : 0);
                        int pan = (register[chipId][0][0x18 + i] & 0xc0) >> 6;
                        rhythmVolume[chipId][i][0] = (int) (256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 2) > 0 ? 1 : 0);
                        rhythmVolume[chipId][i][1] = (int) (256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 1) > 0 ? 1 : 0);
                    }
                }
            }
        }

        if ((addr & 0xf0) == 0x40) { // TL
            int ch = (addr & 0x3);
            int al = register[chipId][port][0xb0 + ch] & 0x07;// AL
            int slot = (addr & 0xc) >> 2;
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
            int al = data & 0x07;// AL

            if (ch != 3 && mask[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) > 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        write(chipId,
                                port,
                                0x40 + ch + tslot,
                                register[chipId][port][0x40 + ch + tslot],
                                model);
                    }
                }
            }
        }

        // ssg mixer
        if (port == 0 && addr == 0x07) {
            int maskData = 0;
            if (mask[chipId][6])
                maskData |= 0x9 << 0;
            if (mask[chipId][7])
                maskData |= 0x9 << 1;
            if (mask[chipId][8])
                maskData |= 0x9 << 2;
            data |= maskData;
        }

        // ssg level
        if (port == 0 && (addr == 0x08 || addr == 0x09 || addr == 0x0a)) {
            int d = fadeout[chipId] >> 3;
            data = Math.max(data - d, 0);
            data = mask[chipId][addr - 0x08 + 6] ? 0 : data;
        }

        // rhythm level
        if (port == 0 && addr == 0x11) {
            int d = fadeout[chipId] >> 1;
            data = Math.max(data - d, 0);
        }

        // adpcm level
        if (port == 1 && addr == 0x0b) {
            int d = fadeout[chipId] * 2;
            data = Math.max(data - d, 0);
            data = mask[chipId][12] ? 0 : data;
        }

        // adpcm start
        if (port == 1 && addr == 0x00) {
            if ((data & 0x80) != 0 && mask[chipId][12]) {
                data &= 0x7f;
            }
        }

        // Rhythm
        if (port == 0 && addr == 0x10) {
            if (mask[chipId][13]) {
                data = 0;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0] && chipTypes[chipId].getUseEmu()[0]) {
//if (addr == 0x29) logger.log(Level.TRACE, "%2x:%2x:%2x ".formatted(port, addr, data));
                context.mds.write(inst[chipId], chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;

            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    public int read(int chipId, int port, int addr, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            return 0;
        } else {
            if (realChips[chipId] == null)
                return 0;

            return realChips[chipId].getRegister(port * 0x100 + addr);
        }
    }

    private void _write(int chipId, int port, int addr, int data, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0] && chipTypes[chipId].getUseEmu()[0]) {
                context.mds.write(inst[chipId], chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;

            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    public void softReset(int chipId, EnmModel model) {
        // FM All Channel Key Off
        _write(chipId, 0, 0x28, 0x00, model);
        _write(chipId, 0, 0x28, 0x01, model);
        _write(chipId, 0, 0x28, 0x02, model);
        _write(chipId, 0, 0x28, 0x04, model);
        _write(chipId, 0, 0x28, 0x05, model);
        _write(chipId, 0, 0x28, 0x06, model);

        // FM TL=127
        for (int i = 0x40; i < 0x4F + 1; i++) {
            _write(chipId, 0, i, 0x7f, model);
            _write(chipId, 1, i, 0x7f, model);
        }
        // FM ML/DT
        for (int i = 0x30; i < 0x3F + 1; i++) {
            _write(chipId, 0, i, 0x0, model);
            _write(chipId, 1, i, 0x0, model);
        }
        // FM AR,DR,SR,KS,AMON
        for (int i = 0x50; i < 0x7F + 1; i++) {
            _write(chipId, 0, i, 0x0, model);
            _write(chipId, 1, i, 0x0, model);
        }
        // FM SL,RR
        for (int i = 0x80; i < 0x8F + 1; i++) {
            _write(chipId, 0, i, 0xff, model);
            _write(chipId, 1, i, 0xff, model);
        }
        // FM F-Num, FB/CONNECT
        for (int i = 0x90; i < 0xBF + 1; i++) {
            _write(chipId, 0, i, 0x0, model);
            _write(chipId, 1, i, 0x0, model);
        }
        // FM PAN/AMS/PMS
        for (int i = 0xB4; i < 0xB6 + 1; i++) {
            _write(chipId, 0, i, 0xc0, model);
            _write(chipId, 1, i, 0xc0, model);
        }
        _write(chipId, 0, 0x22, 0x00, model); // HW LFO
        _write(chipId, 0, 0x24, 0x00, model); // Timer-a(1)
        _write(chipId, 0, 0x25, 0x00, model); // Timer-a(2)
        _write(chipId, 0, 0x26, 0x00, model); // Timer-B
        _write(chipId, 0, 0x27, 0x30, model); // Timer Control
        _write(chipId, 0, 0x29, 0x80, model); // FM4-6 Enable

        // SSG 音程(2byte*3ch)
        for (int i = 0x00; i < 0x05 + 1; i++) {
            _write(chipId, 0, i, 0x00, model);
        }
        _write(chipId, 0, 0x06, 0x00, model); // SSG Noise Frequency
        _write(chipId, 0, 0x07, 0x38, model); // SSG Mixer
        // SSG volume(3ch)
        for (int i = 0x08; i < 0x0A + 1; i++) {
            _write(chipId, 0, i, 0x00, model);
        }
        // SSG Envelope
        for (int i = 0x0B; i < 0x0D + 1; i++) {
            _write(chipId, 0, i, 0x00, model);
        }

        // RHYTHM
        _write(chipId, 0, 0x10, 0xBF, model); // Forced sound stop
        _write(chipId, 0, 0x11, 0x00, model); // Total Level
        _write(chipId, 0, 0x18, 0x00, model); // BD volume
        _write(chipId, 0, 0x19, 0x00, model); // SD volume
        _write(chipId, 0, 0x1A, 0x00, model); // CYM volume
        _write(chipId, 0, 0x1B, 0x00, model); // HH volume
        _write(chipId, 0, 0x1C, 0x00, model); // TOM volume
        _write(chipId, 0, 0x1D, 0x00, model); // RIM volume

        // ADPCM
        _write(chipId, 1, 0x00, 0x21, model); // ADPCM reset
        _write(chipId, 1, 0x01, 0x06, model); // ADPCM mute
        _write(chipId, 1, 0x10, 0x9C, model); // FLAG reset
    }

    public void setMask(int chipId, int ch, boolean mask, boolean noSend/*=false*/) {
        this.mask[chipId][ch] = mask;
        if (ch >= 9 && ch < 12) {
            this.mask[chipId][2] = mask;
            this.mask[chipId][9] = mask;
            this.mask[chipId][10] = mask;
            this.mask[chipId][11] = mask;
        }

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        if (noSend) return;

        if (ch < 6) {
            write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.VirtualModel);
            write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.VirtualModel);
            write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.VirtualModel);
            write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.VirtualModel);

            write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.RealModel);
            write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.RealModel);
            write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.RealModel);
            write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.RealModel);
        } else if (ch < 9) {
            write(chipId, 0, 0x08 + ch - 6, register[chipId][0][0x08 + ch - 6], EnmModel.VirtualModel);
            write(chipId, 0, 0x08 + ch - 6, register[chipId][0][0x08 + ch - 6], EnmModel.RealModel);
        } else if (ch < 12) {
            write(chipId, 0, 0x40 + 2, register[chipId][0][0x40 + 2], EnmModel.VirtualModel);
            write(chipId, 0, 0x44 + 2, register[chipId][0][0x44 + 2], EnmModel.VirtualModel);
            write(chipId, 0, 0x48 + 2, register[chipId][0][0x48 + 2], EnmModel.VirtualModel);
            write(chipId, 0, 0x4c + 2, register[chipId][0][0x4c + 2], EnmModel.VirtualModel);

            write(chipId, 0, 0x40 + 2, register[chipId][0][0x40 + 2], EnmModel.RealModel);
            write(chipId, 0, 0x44 + 2, register[chipId][0][0x44 + 2], EnmModel.RealModel);
            write(chipId, 0, 0x48 + 2, register[chipId][0][0x48 + 2], EnmModel.RealModel);
            write(chipId, 0, 0x4c + 2, register[chipId][0][0x4c + 2], EnmModel.RealModel);
        }
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

    public void setFadeout(int chipId, int v) {

        fadeout[chipId] = v;

        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                write(chipId, p, 0x40 + c, register[chipId][p][0x40 + c], EnmModel.RealModel);
                write(chipId, p, 0x44 + c, register[chipId][p][0x44 + c], EnmModel.RealModel);
                write(chipId, p, 0x48 + c, register[chipId][p][0x48 + c], EnmModel.RealModel);
                write(chipId, p, 0x4c + c, register[chipId][p][0x4c + c], EnmModel.RealModel);
            }
        }

        // ssg
        write(chipId, 0, 0x08, register[chipId][0][0x08], EnmModel.RealModel);
        write(chipId, 0, 0x09, register[chipId][0][0x09], EnmModel.RealModel);
        write(chipId, 0, 0x0a, register[chipId][0][0x0a], EnmModel.RealModel);

        // rhythm
        write(chipId, 0, 0x11, register[chipId][0][0x11], EnmModel.RealModel);

        // adpcm
        write(chipId, 1, 0x0b, register[chipId][1][0x0b], EnmModel.RealModel);
    }

    public void writeClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                realChips[chipId].dClock = realChips[chipId].setMasterClock(clock);
            }
        }
    }

    public void setVolume(int chipId, int vol, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                realChips[chipId].setSSGVolume(vol);
            }
        }
    }

    public int[] getVolume(int chipId) {
        return volume[chipId];
    }

    public int[][] getRhythmVolume(int chipId) {
        return rhythmVolume[chipId];
    }

    public int[] getCh3SlotVolume(int chipId) {
//        if (ctYM2612.UseScci) {
            return ch3SlotVolume[chipId];
//        }
//        return context.mds.inst(inst[chipId]).readFMCh3SlotVolume();
    }

    public int[] getAdpcmVolume(int chipId) {
        return adpcmVolume[chipId];
    }

    public int[][] read(int chipId) {
        return register[chipId];
    }

    public int[] getKeyOn(int chipId) {
        return keyOn[chipId];
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
}
