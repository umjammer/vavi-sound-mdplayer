/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.musica.MuSICA;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.MameAy8910Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MuSICAPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class MuSICAPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MuSICAPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MuSICA();
//        ((MuSICA)audio.driverVirtual).playingFileName = PlayingFileName;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MuSICA();
//            ((MuSICA) audio.driverReal).playingFileName = PlayingFileName;
        }
        prepare();
        boolean r = _play();
        if (!r) {
            logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    private boolean _play() {
        int[] trkOffsets = new int[17];
        for (int t = 0; t < trkOffsets.length; t++) {
            trkOffsets[t] = (vgmBuf[8 + t * 2] & 0xff) + (vgmBuf[9 + t * 2] & 0xff) * 0x100;
        }
        boolean useAY = ((trkOffsets[9] + trkOffsets[10] + trkOffsets[11]) != 0);
        boolean useSCC = ((trkOffsets[12] + trkOffsets[13] + trkOffsets[14] + trkOffsets[15] + trkOffsets[16]) != 0);
        boolean useOPLL = ((trkOffsets[0] + trkOffsets[1] + trkOffsets[2]
                + trkOffsets[3] + trkOffsets[4] + trkOffsets[5]
                + trkOffsets[6] + trkOffsets[7] + trkOffsets[8]
        ) != 0);

        startTrdVgmReal();

        if (useAY) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.option = null;
            if (chip.instrument instanceof MameAy8910Inst) {
                chip.option = new Object[] {
                        (setting.getAY8910Type()[0].getYM2149mode() ? 0x10 : 0x00), // chip_type 0x10: YM2149, 0x00: AY
                        0x00  // chip_flag
                };
            }
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = MuSICA.baseClockAY8910 / 2;
            audio.chipRegister.chip(Ay8910Chip.class).clock = MuSICA.baseClockAY8910;

            audio.chipLED.put("PriAY10", 1);

            put(Ay8910Chip.class, chip);
        }

        if (useOPLL) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPLL", 1);
            chip.instrument = audio.chipRegister.chip(Ym2413Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
            chip.clock = MuSICA.baseClockYM2413;
            chip.option = null;
            put(Ym2413Chip.class, chip);
            audio.chipRegister.chip(Ym2413Chip.class).clock = MuSICA.baseClockYM2413;
        }

        if (useSCC) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriK051649", 1);
            chip.instrument = audio.chipRegister.chip(K051649Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
            chip.clock = MuSICA.baseClockK051649;
            chip.option = null;
            put(K051649Chip.class, chip);
            audio.chipRegister.chip(K051649Chip.class).clock = MuSICA.baseClockK051649;
        }

        if (hiyorimiNecessary) hiyorimiNecessary = true;
        else hiyorimiNecessary = false;

        audio.mds.init((int) setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (useOPLL) {
            audio.chipRegister.chip(Ym2413Chip.class).write(0, 14, 32, EnmModel.VirtualModel);
        }

        if (!audio.driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                new Class[] {Ay8910Chip.class, Ym2413Chip.class, K051649Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    new Class[] {Ay8910Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        return true;
    }
}
