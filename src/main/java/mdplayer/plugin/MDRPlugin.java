package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.moonDriver.MoonDriverJava;
import mdplayer.format.FileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MoonDriver (MSX, Local) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDRPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MDRPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MoonDriverJava();
        ((MoonDriverJava) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new MoonDriverJava();
//            ((MoonDriverJava) audio.driverReal).extendFiles = (extendFiles != null && !extendFiles.isEmpty()) ? extendFiles.get(0) : null;
//        }
        boolean r = _play();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _play() {
        //int r = ((NRTDRV) driverVirtual).checkUseChip(vgmBuf);

        audio.chipRegister.chip(Ym2151Chip.class).setFadeout(0, 0);
        audio.chipRegister.chip(Ym2151Chip.class).setFadeout(1, 0);

        startTrdVgmReal();

        hiyorimiNecessary = setting.getHiyorimiMode();
        int hiyorimiDeviceFlag = 0;

        byte sndgen = vgmBuf[7];
        boolean EX_OPL3 = ((sndgen & 2) != 0);
        boolean OPL4_NOUSE = ((sndgen & 1) == 0);
        Class<? extends Chip>[] useChipFromMdr = new Class[1];

        if (OPL4_NOUSE && !EX_OPL3) {
            logger.log(Level.WARNING, "The combination of OPL4_NOUSE and EX_OPL3 is invalid.");
            return false;
        }

        if (EX_OPL3 && OPL4_NOUSE) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(YmF262Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class);
            chip.clock = 14318180;
            chip.option = new Object[] {Common.getApplicationFolder()};

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriOPL3", 1);

            put(YmF262Chip.class, chip);
            useChipFromMdr[0] = YmF262Chip.class;
        } else {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(YmF278BChip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
            chip.clock = 33868800;
            chip.option = new Object[] {Common.getApplicationFolder()};

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriOPL4", 1);

            put(YmF278BChip.class, chip);
            useChipFromMdr[0] = YmF278BChip.class;
        }

        if (hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary) hiyorimiNecessary = true;
        else hiyorimiNecessary = false;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (EX_OPL3 && OPL4_NOUSE) audio.setVolume(MAIN_TAG, YmF262Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class));
        else audio.setVolume(MAIN_TAG, YmF278BChip.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class));
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), RnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, useChipFromMdr,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (audio.driverReal != null) {
            audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, useChipFromMdr,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        return true;
    }
}
