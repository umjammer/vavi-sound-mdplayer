package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.YmF262Inst;
import mdsound.instrument.YmF278bInst;
import vavi.util.Debug;


/**
 * MDRPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDRPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MoonDriver();
        audio.driverVirtual.setting = setting;
        ((MoonDriver) audio.driverVirtual).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MoonDriver();
            audio.driverReal.setting = setting;
            ((MoonDriver) audio.driverReal).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        }
        boolean r = mdrPlay();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
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

            audio.chipRegister.setFadeoutVolYM2151(0, 0);
            audio.chipRegister.setFadeoutVolYM2151(1, 0);

            audio.chipRegister.resetChips();
            audio.useChip.clear();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            audio.vgmRealFadeoutVol = 0;
            audio.vgmRealFadeoutVolWait = 4;

            audio.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            byte sg = vgmBuf[7];

            boolean isOPL3 = (sg & 2) != 0;

            if (isOPL3) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = new YmF262Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", YmF262Inst.class);
                chip.clock = 14318180;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                audio.chipLED.put("PriOPL3", 1);

                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.YMF262);
            } else {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = new YmF278bInst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", YmF278bInst.class);
                chip.clock = 33868800;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                audio.chipLED.put("PriOPL4", 1);

                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.YMF278B);
            }

            audio.hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && audio.hiyorimiNecessary;

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (isOPL3) audio.setVolume("MAIN", YmF262Inst.class, true, setting.getBalance().getVolume("MAIN", YmF262Inst.class));
            else audio.setVolume("MAIN", YmF278bInst.class, true, setting.getBalance().getVolume("MAIN", YmF278bInst.class));
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            ((MoonDriver) audio.driverVirtual).isOPL3 = isOPL3;
            ((MoonDriver) audio.driverReal).isOPL3 = isOPL3;

            audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            if (audio.driverReal != null) {
                audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            }

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
