package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.rcp.RCP;
import mdplayer.format.FileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;


/**
 * RCPPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class RCPPlugin extends BasePlugin {

    private static final Logger logger = getLogger(RCPPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new RCP();
        ((RCP) audio.driverVirtual).ExtendFile = extendFile;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new RCP();
            ((RCP) audio.driverReal).ExtendFile = extendFile;
        }
        boolean r = rcpPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean rcpPlay() {
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
            audio.chipRegister.chipLED.put("PriMID", 1);
            audio.chipRegister.chipLED.put("SecMID", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            audio.chipRegister.initChipRegister(null);
            audio.chipRegister.plugin(MidiPlugin.class).releaseAll();
            audio.chipRegister.plugin(MidiPlugin.class).make(setting, midiMode);
            audio.chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(midiMode));

            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

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
