package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Audio;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.YmF262Inst;
import mdsound.instrument.YmF278bInst;

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
        boolean r = mdrPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mdrPlay() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //stop();

            //int r = ((NRTDRV) driverVirtual).checkUseChip(vgmBuf);

            audio.chipRegister.chip(Ym2151Chip.class).setFadeoutVolYM2151(0, 0);
            audio.chipRegister.chip(Ym2151Chip.class).setFadeoutVolYM2151(1, 0);

            audio.chipRegister.resetChips();
            useChip.clear();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            audio.chipRegister.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            byte sg = vgmBuf[7];

            boolean isOPL3 = (sg & 2) != 0;

            if (isOPL3) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = Instrument.getInstrument(YmF262Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF262Inst.class);
                chip.clock = 14318180;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                audio.chipRegister.chipLED.put("PriOPL3", 1);

                lstChips.add(chip);
                useChip.add(Common.EnmChip.YMF262);
            } else {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = Instrument.getInstrument(YmF278bInst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278bInst.class);
                chip.clock = 33868800;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                audio.chipRegister.chipLED.put("PriOPL4", 1);

                lstChips.add(chip);
                useChip.add(Common.EnmChip.YMF278B);
            }

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips.toArray(MDSound.Chip[]::new));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (isOPL3) audio.setVolume(MAIN_TAG, YmF262Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF262Inst.class));
            else audio.setVolume(MAIN_TAG, YmF278bInst.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF278bInst.class));
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            ((MoonDriver) audio.driverVirtual).isOPL3 = isOPL3;
            if (audio.driverReal != null) ((MoonDriver) audio.driverReal).isOPL3 = isOPL3;

            audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            if (audio.driverReal != null) {
                audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            }

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
