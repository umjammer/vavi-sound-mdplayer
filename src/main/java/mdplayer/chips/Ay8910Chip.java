/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RC86ctlSoundChip;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.Ay8910Inst;
import mdsound.instrument.MameAy8910Inst;
import mdsound.instrument.NpYm2149Inst;


/**
 * Ay8910Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ay8910Chip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getAY8910Type();

    private final RSoundChip[] realChips = {null, null};

    // TODO eliminate cache like params, retrieve directly
    @Deprecated
    public final int[][] psgRegister = {null, null};

    @Deprecated
    public final int[][] psgKeyOn = {null, null};

    @Deprecated
    private final int[] fadeoutVolume = {0, 0};

    @Deprecated
    public final int[][] psgVolume = {new int[3], new int[3]};

    private final boolean[][] mask = {
            {false, false, false},
            {false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Ay8910Inst.class, MameAy8910Inst.class, NpYm2149Inst.class};
    }

    @Override
    public int activeIndex(int chipId) {
        return chipTypes[chipId].getEnabledId();
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);

        for (int chipId = 0; chipId < 2; chipId++) {
            psgRegister[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                psgRegister[chipId][i] = 0;
            }
            psgKeyOn[chipId] = new int[] {0, 0, 0};

            fadeoutVolume[chipId] = 0;
        }
    }

    public void write(int chipId, int addr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            psgRegister[chipId][addr] = data;

        // psg mixer
        if (addr == 0x07) {
            int maskData = 0;
            if (mask[chipId][0])
                maskData |= 0x9 << 0;
            if (mask[chipId][1])
                maskData |= 0x9 << 1;
            if (mask[chipId][2])
                maskData |= 0x9 << 2;
            data |= maskData;
        }

        // psg level
        if ((addr == 0x08 || addr == 0x09 || addr == 0x0a)) {
            int d = fadeoutVolume[chipId] >> 3;
            data = Math.max(data - d, 0);
            data = mask[chipId][addr - 0x08] ? 0 : data;
        }

        if (model == EnmModel.VirtualModel) {
            if (chipTypes[chipId].getUseReal()[0])
                return;
            context.mds.write(inst(chipId), chipId, 0, addr, data);
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(addr + 0x000, data);
        }
    }

    public void softReset(int chipId, EnmModel model) {

        // All Channel Key Off
        write(chipId, 0x07, 0x00, model);

        // Volume Off
        for (int ch = 0; ch < 3; ch++) {
            write(chipId, 0x8 + ch, 0x00, model);
        }

        // Noise Initialization
        write(chipId, 0x06, 0x00, model);
        // Envelope Initialization
        write(chipId, 0x0b, 0x00, model);
        write(chipId, 0x0c, 0x00, model);
        write(chipId, 0x0d, 0x00, model);
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;

        write(chipId, 0x8 + ch, psgRegister[chipId][8 + ch], EnmModel.VirtualModel);
        write(chipId, 0x8 + ch, psgRegister[chipId][8 + ch], EnmModel.RealModel);
    }

    public void setFadeout(int chipId, int v) {
        fadeoutVolume[chipId] = v;
        for (int c = 0; c < 3; c++) {
            write(chipId, 0x8 + c, psgRegister[chipId][0x8 + c], EnmModel.RealModel);
        }
    }

    public void writeClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                if (realChips[chipId] instanceof RC86ctlSoundChip) {
//                    Nc86ctl.ChipType ct = ((RC86ctlSoundChip) realChips[chipId]).ChipType;
//                    // If YM2149 is selected, double the frequency
//                    if (ct == Nc86ctl.ChipType.CHIP_YM2149) {
//                        clock *= 2;
//                    }
                }
                realChips[chipId].dClock = realChips[chipId].setMasterClock(clock);
            }
        }
    }

    public int[] read(int chipId) {
        return psgRegister[chipId];
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
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

    /** the panel/main-window view of whether a channel is muted; this array is the source of truth */
    public boolean getMask(int chipId, int ch) {
        return ch < mask[chipId].length && mask[chipId][ch];
    }
}
