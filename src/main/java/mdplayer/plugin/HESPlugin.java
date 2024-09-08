package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.hes.Hes;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.HuC6280Inst;
import vavi.util.Debug;


/**
 * HESPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class HESPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Hes();
        audio.driverVirtual.setting = setting;

        audio.driverReal = null;
        //if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
        //    driverReal = new Hes();
        //    driverReal.setting = setting;
        //}
        boolean r = hesPlay();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean hesPlay() {
        try {
            if (vgmBuf == null || setting == null) return false;

            //Stop();

            audio.chipRegister.resetChips();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            audio.vgmRealFadeoutVol = 0;
            audio.vgmRealFadeoutVolWait = 4;

            audio.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            audio.useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.chipLED.put("PriHuC", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            //((Hes)driverVirtual).song = (byte)SongNo;
            //((Hes)driverReal).song = (byte)SongNo;
            //if (!driverVirtual.init(vgmBuf, chipRegister, enmModel.VirtualModel, new enmUseChip[] { enmUseChip.Unuse }, 0)) return false;
            //if (!driverReal.init(vgmBuf, chipRegister, enmModel.RealModel, new enmUseChip[] { enmUseChip.Unuse }, 0)) return false;

            MDSound.Chip chip;
            HuC6280Inst huc = new HuC6280Inst();

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = huc;
            chip.additionalUpdate = ((Hes) audio.driverVirtual)::additionalUpdate;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume("MAIN", HuC6280Inst.class);
            chip.clock = 3579545;
            chip.option = null;
            lstChips.add(chip);
            ((Hes) audio.driverVirtual).c6280 = chip;
            audio.useChip.add(Common.EnmChip.HuC6280);

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(MDSound.Chip[]::new));

            ((Hes) audio.driverVirtual).song = songNo;
            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                ((Hes) audio.driverReal).song = songNo;
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }
            // Play

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
