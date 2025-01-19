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
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68SoundYm2151Inst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.YmFmYm2151Inst;


/**
 * Ym2151Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ym2151Chip implements Chip {

    private final Setting.ChipType2[] ctYM2151 = new Setting.ChipType2[] {
            setting.getYM2151Type()[0], setting.getYM2151Type()[1]
    };

    private final RSoundChip[] scYM2151 = {null, null};

    public int[][] fmRegisterYM2151 = {null, null};
    public int[][] fmKeyOnYM2151 = {null, null};
    public int[][] fmVolYM2151 = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private final int[] nowYM2151FadeoutVol = {0, 0};
    private final boolean[][] maskFMChYM2151 = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
    };
    public int[] fmAMDYM2151 = {-1, -1};
    public int[] fmPMDYM2151 = {-1, -1};

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        for (int chipId = 0; chipId < 2; chipId++) {
            fmRegisterYM2151[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2151[chipId][i] = 0;
            }
            fmKeyOnYM2151[chipId] = new int[] {0, 0, 0, 0, 0, 0, 0, 0};

            nowYM2151FadeoutVol[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int c = 0; c < 8; c++) {
                setYM2151Register(chipId, 0, 0x60 + c, 127, EnmModel.RealModel, 0, -1);
                setYM2151Register(chipId, 0, 0x68 + c, 127, EnmModel.RealModel, 0, -1);
                setYM2151Register(chipId, 0, 0x70 + c, 127, EnmModel.RealModel, 0, -1);
                setYM2151Register(chipId, 0, 0x78 + c, 127, EnmModel.RealModel, 0, -1);
            }
        }
    }

    @Override
    public void updateVol() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 8; i++) {
                if (fmVolYM2151[chipId][i] > 0) {
                    fmVolYM2151[chipId][i] -= 50;
                    if (fmVolYM2151[chipId][i] < 0)
                        fmVolYM2151[chipId][i] = 0;
                }
            }
        }
    }

    public void setYM2151Register(int chipId,
                                  int dPort,
                                  int dAddr,
                                  int dData,
                                  EnmModel model,
                                  int hosei,
                                  long vgmFrameCounter) {
        if (ctYM2151 == null)
            return;

        if (chipId == 0)
            context.chipLED.put("PriOPM", 2);
        else
            context.chipLED.put("SecOPM", 2);

        if ((model == EnmModel.VirtualModel && (ctYM2151[chipId] == null || !ctYM2151[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scYM2151 != null && scYM2151[chipId] != null))) {
            fmRegisterYM2151[chipId][dAddr] = dData;
            context.plugin(MidiPlugin.class).midiExport.outMIDIData(EnmChip.YM2151, chipId, dPort, dAddr, dData, hosei, vgmFrameCounter);
        }

        if ((model == EnmModel.RealModel && ctYM2151[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !ctYM2151[chipId].getUseReal()[0])) {
            if (dAddr == 0x08) { // Key-On/Off
                int ch = dData & 0x7;
                if (ch >= 0 && ch < 8) {
                    if ((dData & 0x78) != 0) {
                        int con = dData & 0x78;
                        fmKeyOnYM2151[chipId][ch] = con | 1;
                        fmVolYM2151[chipId][ch] = 256 * 6;
                    } else {
                        fmKeyOnYM2151[chipId][ch] &= 0xfe;
                    }
                }
            }
        }

        // AMD/PMD
        if (dAddr == 0x19) {
            if ((dData & 0x80) != 0) {
                fmPMDYM2151[chipId] = dData & 0x7f;
            } else {
                fmAMDYM2151[chipId] = dData & 0x7f;
            }
        }

        if ((dAddr & 0xf8) == 0x20) {
            int al = dData & 0x07; // AL
            int ch = dAddr & 0x7;

            for (int i = 0; i < 4; i++) {
                int slot = (i == 0) ? 0 : ((i == 1) ? 2 : ((i == 2) ? 1 : 3));
                if ((algM[al] & (1 << slot)) > 0) {
                    if (maskFMChYM2151[chipId][ch]) {
                        if (model == EnmModel.VirtualModel) {
                            if (!ctYM2151[chipId].getUseReal()[0]) {
                                if (ctYM2151[chipId].getUseEmu()[0])
                                    context.mds.write(Ym2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                                if (ctYM2151[chipId].getUseEmu()[1])
                                    context.mds.write(MameYm2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                                if (ctYM2151[chipId].getUseEmu()[2])
                                    context.mds.write(X68SoundYm2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                                if (ctYM2151[chipId].getUseEmu()[2])
                                    context.mds.write(YmFmYm2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                            }
                        } else {
                            if (scYM2151 != null && scYM2151[chipId] != null)
                                scYM2151[chipId].setRegister(0x60 + i * 8 + ch, 127);
                        }
                    }
                }
            }
        }

        if ((dAddr & 0xf0) == 0x60 || (dAddr & 0xf0) == 0x70) { // TL
            int ch = dAddr & 0x7;
            dData &= 0x7f;

            dData = Math.min(dData + nowYM2151FadeoutVol[chipId], 127);
            dData = maskFMChYM2151[chipId][ch] ? 127 : dData;
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2151[chipId].getUseReal()[0]) {
                if (ctYM2151[chipId].getUseEmu()[0])
                    context.mds.write(Ym2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[1])
                    context.mds.write(MameYm2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[2])
                    context.mds.write(X68SoundYm2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[3])
                    context.mds.write(YmFmYm2151Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2151[chipId] == null)
                return;

            if (dAddr >= 0x28 && dAddr <= 0x2f) {
                if (hosei == 0) {
                    scYM2151[chipId].setRegister(dAddr, dData);
                } else {
                    int oct = (dData & 0x70) >> 4;
                    int note = dData & 0xf;
                    note = (note < 3) ? note : ((note < 7) ? (note - 1) : ((note < 11) ? (note - 2) : (note - 3)));
                    note += hosei - 1;
                    if (note < 0) {
                        oct += (note / 12) - 1;
                        note = (note % 12) + 12;
                    } else {
                        oct += (note / 12);
                        note %= 12;
                    }

                    note = (note < 3) ? note : ((note < 6) ? (note + 1) : ((note < 9) ? (note + 2) : (note + 3)));
                    if (scYM2151[chipId] != null)
                        scYM2151[chipId].setRegister(dAddr, (oct << 4) | note);
                }
            } else {
                scYM2151[chipId].setRegister(dAddr, dData);
            }
        }
    }

    private void writeYm2151(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM2151[chipId].getUseReal()[0]) {
                if (ctYM2151[chipId].getUseEmu()[0])
                    context.mds.write(Ym2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[1])
                    context.mds.write(MameYm2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[2])
                    context.mds.write(X68SoundYm2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[3])
                    context.mds.write(YmFmYm2151Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2151[chipId] != null)
                scYM2151[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM2151(int chipId, EnmModel model) {

        // FM all channel keys off
        for (int i = 0; i < 8; i++) {
            // note off
            writeYm2151(chipId, 0, 0x08, 0x00 + i, model);
        }

        writeYm2151(chipId, 0, 0x0f, 0x00, model); // FM NOISE ENABLE/NOISE FREQ
        writeYm2151(chipId, 0, 0x18, 0x00, model); // FM HW LFO FREQ
        writeYm2151(chipId, 0, 0x19, 0x80, model); // FM PMD/VALUE
        writeYm2151(chipId, 0, 0x19, 0x00, model); // FM AMD/VALUE
        writeYm2151(chipId, 0, 0x1b, 0x00, model); // FM HW LFO WAVEFORM

        // FM HW LFO RESET
        writeYm2151(chipId, 0, 0x01, 0x02, model);
        writeYm2151(chipId, 0, 0x01, 0x00, model);

        writeYm2151(chipId, 0, 0x10, 0x00, model); // FM Timer-a(H)
        writeYm2151(chipId, 0, 0x11, 0x00, model); // FM Timer-a(L)
        writeYm2151(chipId, 0, 0x12, 0x00, model); // FM Timer-B
        writeYm2151(chipId, 0, 0x14, 0x00, model); // FM Timer Control

        for (int i = 0; i < 8; i++) {
            // FB/ALG/PAN
            writeYm2151(chipId, 0, 0x20 + i, 0x00, model);
            // KC
            writeYm2151(chipId, 0, 0x28 + i, 0x00, model);
            // KF
            writeYm2151(chipId, 0, 0x30 + i, 0x00, model);
            // PMS/AMS
            writeYm2151(chipId, 0, 0x38 + i, 0x00, model);
        }
        for (int i = 0; i < 0x20; i++) {
            // DT1/ML
            writeYm2151(chipId, 0, 0x40 + i, 0x00, model);
            // TL=127
            writeYm2151(chipId, 0, 0x60 + i, 0x7f, model);
            // KS/AR
            writeYm2151(chipId, 0, 0x80 + i, 0x1F, model);
            // AMD/D1R
            writeYm2151(chipId, 0, 0xa0 + i, 0x00, model);
            // DT2/D2R
            writeYm2151(chipId, 0, 0xc0 + i, 0x00, model);
            // D1L/RR
            writeYm2151(chipId, 0, 0xe0 + i, 0x0F, model);
        }
    }

    public void setMaskYM2151(int chipId, int ch, boolean mask, boolean noSend /* = false */) {
        maskFMChYM2151[chipId][ch] = mask;

        if (noSend) return;

        setYM2151Register(chipId, 0, 0x60 + ch, fmRegisterYM2151[chipId][0x60 + ch], EnmModel.VirtualModel, 0, -1);
        setYM2151Register(chipId, 0, 0x68 + ch, fmRegisterYM2151[chipId][0x68 + ch], EnmModel.VirtualModel, 0, -1);
        setYM2151Register(chipId, 0, 0x70 + ch, fmRegisterYM2151[chipId][0x70 + ch], EnmModel.VirtualModel, 0, -1);
        setYM2151Register(chipId, 0, 0x78 + ch, fmRegisterYM2151[chipId][0x78 + ch], EnmModel.VirtualModel, 0, -1);

        setYM2151Register(chipId, 0, 0x60 + ch, fmRegisterYM2151[chipId][0x60 + ch], EnmModel.RealModel, 0, -1);
        setYM2151Register(chipId, 0, 0x68 + ch, fmRegisterYM2151[chipId][0x68 + ch], EnmModel.RealModel, 0, -1);
        setYM2151Register(chipId, 0, 0x70 + ch, fmRegisterYM2151[chipId][0x70 + ch], EnmModel.RealModel, 0, -1);
        setYM2151Register(chipId, 0, 0x78 + ch, fmRegisterYM2151[chipId][0x78 + ch], EnmModel.RealModel, 0, -1);
    }

    public void setYM2151SyncWait(int chipId, int wait) {
        if (scYM2151[chipId] != null && ctYM2151[chipId].getRealChipInfo()[0].getUseWait()) {
            scYM2151[chipId].setRegister(-1, (int) (wait * (ctYM2151[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void sendDataYM2151(int chipId, EnmModel model) {
        if (model == EnmModel.VirtualModel)
            return;

        if (scYM2151[chipId] != null && ctYM2151[chipId].getRealChipInfo()[0].getUseWait()) {
            context.realChip.SendData();
            while (!scYM2151[chipId].isBufferEmpty()) {
            }
        }
    }

    public int getYM2151Clock(int chipId) {
        if (scYM2151[chipId] == null)
            return -1;

        return scYM2151[chipId].dClock;
    }

    public void setFadeoutVolYM2151(int chipId, int v) {
        nowYM2151FadeoutVol[chipId] = v;
        for (int c = 0; c < 8; c++) {
            setYM2151Register(chipId, 0, 0x60 + c, fmRegisterYM2151[chipId][0x60 + c], EnmModel.RealModel, 0, -1);
            setYM2151Register(chipId, 0, 0x68 + c, fmRegisterYM2151[chipId][0x68 + c], EnmModel.RealModel, 0, -1);
            setYM2151Register(chipId, 0, 0x70 + c, fmRegisterYM2151[chipId][0x70 + c], EnmModel.RealModel, 0, -1);
            setYM2151Register(chipId, 0, 0x78 + c, fmRegisterYM2151[chipId][0x78 + c], EnmModel.RealModel, 0, -1);
        }
    }

    public void writeYm2151Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2151 != null && scYM2151[chipId] != null) {
                scYM2151[chipId].dClock = scYM2151[chipId].setMasterClock(clock);
            }
        }
    }

    public int[] getYM2151Volume(int chipId) {
        return fmVolYM2151[chipId];
    }
}
