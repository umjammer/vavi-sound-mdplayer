/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.Instrument.PannableInstrument;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Sn76496Inst;


/**
 * Sn76489Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Sn76489Chip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getSN76489Type();

    private final RSoundChip[] realChips = {null, null};

    public final int[][] register = {null, null};

    public final int[] pan = {0xff, 0xff};

    public final int[][][] volumes = {
            {new int[2], new int[2], new int[2], new int[2]},
            {new int[2], new int[2], new int[2], new int[2]}
    };

    public final int[] fadeout = {0, 0};

    public final boolean[][] mask = {
            {false, false, false, false},
            {false, false, false, false}
    };

    private final int[] latchedRegister = {
            0, 0
    };

    private final int[] noiseFreq = {
            0, 0
    };

    public int clock;

    @SuppressWarnings("unchecked")
    private Class<? extends PannableInstrument> _inst(int chipId) {
        return (Class<? extends PannableInstrument>) inst(chipId);
    }

    @Override
    public int activeIndex(int chipId) {
        return chipTypes[chipId].getEnabledId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Sn76489Inst.class, Sn76496Inst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[] {0, 15, 0, 15, 0, 15, 0, 15};

            fadeout[chipId] = 0;
        }
    }

    @Override
    public void reset() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int c = 0; c < 4; c++) {
                write(chipId, 0x90 + (c << 5) + 0xf, EnmModel.RealModel);
            }
        }
    }

    public void write(int chipId, int data, EnmModel model) {

        if (chipId == 0)
            context.chipLED.put("PriDCSG", 2);
        else
            context.chipLED.put("SecDCSG", 2);

        write(chipId, data);

        if ((data & 0x10) != 0) {
            if (latchedRegister[chipId] != 0 && latchedRegister[chipId] != 2 && latchedRegister[chipId] != 4 &&
                    latchedRegister[chipId] != 6) {
                volumes[chipId][(data & 0x60) >> 5][0] = (15 - (data & 0xf)) *
                        ((pan[chipId] >> (((data & 0x60) >> 5) + 4)) &
                                0x1);
                volumes[chipId][(data & 0x60) >> 5][1] = (15 - (data & 0xf)) *
                        ((pan[chipId] >> ((data & 0x60) >> 5)) & 0x1);

                int v = data & 0xf;
                v = v + fadeout[chipId];
                v = mask[chipId][(data & 0x60) >> 5] ? 15 : v;
                v = Math.min(v, 15);
                data = (data & 0xf0) | v;
            }
        }

        if (model == EnmModel.RealModel) {
            if (chipTypes[chipId].getUseReal()[0]) {
                if (realChips[chipId] == null)
                    return;
                realChips[chipId].setRegister(0, data);
            }
        } else {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(_inst(chipId), chipId, 0, 0, data);
            }
        }
    }

    public void setPan(int chipId, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriDCSG", 2);
        else
            context.chipLED.put("SecDCSG", 2);

        if (model == EnmModel.RealModel) {
            if (chipTypes[chipId].getUseReal()[0]) {
                if (realChips[chipId] == null) {
                }
            }
        } else {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.inst(_inst(chipId)).setPan(chipId, dData);
                pan[chipId] = dData;
            }
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
    }

    private void write(int chipId, int data) {
        if ((data & 0x80) != 0) {
            // Latch/data byte %1 cc t dddd
            latchedRegister[chipId] = (data >> 4) & 0x07;
            register[chipId][latchedRegister[chipId]] = (register[chipId][latchedRegister[chipId]] &
                    0x3f0) | // zero low 4 bits
                    (data & 0xf); // and replace with data
        } else {
            // data byte %0 - dddddd
            if ((latchedRegister[chipId] % 2) == 0 && (latchedRegister[chipId] < 5))
                // Tone register
                register[chipId][latchedRegister[chipId]] = (register[chipId][latchedRegister[chipId]] &
                        0x00f) |  // zero high 6 bits
                        ((data & 0x3f) << 4); // and replace with data
            else
                // Other register
                register[chipId][latchedRegister[chipId]] = data & 0x0f; // Replace with data
        }
        switch (latchedRegister[chipId]) {
            case 0:
            case 2:
            case 4: // Tone channels
//                if (register[chipId][latchedRegister[chipId]] == 0)
//                    register[chipId][latchedRegister[chipId]] = 1; // Zero frequency changed to 1 to avoid div/0
                break;
            case 6: // Noise
                noiseFreq[chipId] = 0x10 << (register[chipId][6] & 0x3); // set noise signal generator frequency
                break;
        }
    }

    public void setFadeout(int chipId, int v) {
        fadeout[chipId] = (v & 0x78) >> 3;
        for (int c = 0; c < 4; c++) {

            write(chipId, 0x90 + (c << 5) + register[chipId][1 + (c << 1)], EnmModel.RealModel);
        }
    }

    public Map<String, Object> getInfo(int chipId) {
        return Map.of(
                "volumes", volumes[chipId],
                "register", register[chipId],
                "pan", pan[chipId],
                "flag", ngpFlag
        );
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
        sendVolumeForced(chipId, ch);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
        sendVolumeForced(chipId, ch);
    }

    protected void sendVolumeForced(int chipId, int ch) {
        Setting.ChipType2 ct = setting.getSN76489Type()[chipId];
        write(chipId, (0x90 |
                        ((ch & 3) << 5) |
                        (15 - (Math.max(volumes[chipId][ch][0], volumes[chipId][ch][1]) & 0xf))),
                ct.getUseEmu()[0] ? Common.EnmModel.VirtualModel : Common.EnmModel.RealModel);
    }

    public boolean ngpFlag = false;

    @Override
    public void clearFadeout() {
        setFadeout(0, 0);
        setFadeout(1, 0);
    }
}
