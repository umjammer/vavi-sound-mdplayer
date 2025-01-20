package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Audio;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.hes.Hes;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.HuC6280Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * HESPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class HESPlugin extends BasePlugin {

    private static final Logger logger = getLogger(HESPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Hes();

        audio.driverReal = null;
        //if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
        //    driverReal = new Hes();
        //}
        boolean r = hesPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
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
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipRegister.chipLED.clear();
            audio.chipRegister.chipLED.put("PriHuC", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            //((Hes)driverVirtual).song = (byte)SongNo;
            //((Hes)driverReal).song = (byte)SongNo;
            //if (!driverVirtual.init(vgmBuf, chipRegister, enmModel.VirtualModel, new enmUseChip[] { enmUseChip.Unuse }, 0)) return false;
            //if (!driverReal.init(vgmBuf, chipRegister, enmModel.RealModel, new enmUseChip[] { enmUseChip.Unuse }, 0)) return false;

            MDSound.Chip chip;
            HuC6280Inst huc = Instrument.getInstrument(HuC6280Inst.class);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = huc;
            chip.additionalUpdate = ((Hes) audio.driverVirtual)::additionalUpdate;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, HuC6280Inst.class);
            chip.clock = 3579545;
            chip.option = null;
            lstChips.add(chip);
            ((Hes) audio.driverVirtual).c6280 = chip;
            useChip.add(Common.EnmChip.HuC6280);

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips.toArray(MDSound.Chip[]::new));

            audio.chipRegister.initChipRegister(lstChips.toArray(MDSound.Chip[]::new));

            ((Hes) audio.driverVirtual).song = songNo;
            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                ((Hes) audio.driverReal).song = songNo;
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
