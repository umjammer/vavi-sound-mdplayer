package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.YmF262Inst;
import mdsound.instrument.YmF278BInst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MDRPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDRPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MDRPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MoonDriver();
        ((MoonDriver) audio.driverVirtual).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new MoonDriver();
//            ((MoonDriver) audio.driverReal).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
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

        audio.vgmFadeout = false;
        audio.vgmFadeoutCounter = 1.0;
        audio.vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        vgmRealFadeoutVol = 0;
        vgmRealFadeoutVolWait = 4;

        audio.chipRegister.clearFadeoutVolume();

        audio.chipRegister.reset();

        startTrdVgmReal();

        hiyorimiNecessary = setting.getHiyorimiMode();
        int hiyorimiDeviceFlag = 0;

        audio.chipLED.clear();

        audio.masterVolume = setting.getBalance().getMasterVolume();

        byte sg = vgmBuf[7];

        boolean isOPL3 = (sg & 2) != 0;

        if (isOPL3) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = Instrument.getInstrument(YmF262Inst.class);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class);
            chip.clock = 14318180;
            chip.option = new Object[] {Common.getApplicationFolder()};

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriOPL3", 1);

            put(YmF262Chip.class, chip);
        } else {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = Instrument.getInstrument(YmF278BInst.class);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
            chip.clock = 33868800;
            chip.option = new Object[] {Common.getApplicationFolder()};

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriOPL4", 1);

            put(YmF278BChip.class, chip);
        }

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (isOPL3) audio.setVolume(MAIN_TAG, YmF262Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class));
        else audio.setVolume(MAIN_TAG, YmF278BChip.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class));
//            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), RnmModel.RealModel);
//            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        ((MoonDriver) audio.driverVirtual).isOPL3 = isOPL3;
        if (audio.driverReal != null) ((MoonDriver) audio.driverReal).isOPL3 = isOPL3;

        audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (audio.driverReal != null) {
            audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        audio.paused = false;
        oneTimeReset = false;

        sleep(500);

        return true;
    }
}
