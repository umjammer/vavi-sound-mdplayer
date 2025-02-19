/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import mdplayer.Audio;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.ZxBeepChip;
import mdplayer.driver.ay.AY;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.MameAy8910Inst;

import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * AyPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-19 nsano initial version <br>
 */
public class AyPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new AY();
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new AY();
//            audio.driverReal.setting = setting;
//        }
        return play();
    }

    public boolean play() {
        startTrdVgmReal();

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(0);
        chip.option = null;
        if (chip.instrument instanceof MameAy8910Inst) {
            chip.option = new Object[] {
                    (setting.getAY8910Type()[0].getYM2149mode() ? 0x10 : 0x00), // chip_type 0x10: YM2149, 0x00: AY
                    0x00 // chip_flag
            };
        }
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
        chip.clock = 1789773 / 2;
        audio.chipRegister.chip(Ay8910Chip.class).clock = 1789773;
        audio.chipLED.put("PriAY10", 1);
        put(Ay8910Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(ZxBeepChip.class).instrument(0);
        chip.option = null;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
        chip.clock = 1789773 / 2;
        put(ZxBeepChip.class, chip);

        if (hiyorimiNecessary) hiyorimiNecessary = true;
        else hiyorimiNecessary = false;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        ((AY) audio.driverVirtual).song = songNo;
        if (!audio.driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                new Class[] {Ay8910Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            ((AY) audio.driverReal).song = songNo;
            if (!audio.driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    new Class[] {Ay8910Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        return true;
    }
}
