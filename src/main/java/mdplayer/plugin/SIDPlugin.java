package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.driver.sid.Sid;
import mdplayer.format.FileFormat;

import static java.lang.System.getLogger;


/**
 * SIDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SIDPlugin extends BasePlugin {

    private static final Logger logger = getLogger(SIDPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Sid();

        audio.driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Sid.Sid();
//        }
        boolean r = sidPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean sidPlay() {

        try {

            if (vgmBuf == null || setting == null) return false;

            stop();

            audio.chipRegister.reset();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.reset();

            useChip.clear();

            startTrdVgmReal();

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED.clear();
            audio.chipLED.put("priSID", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            ((Sid) audio.driverVirtual).song = songNo + 1;
            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                ((Sid) audio.driverReal).song = songNo + 1;
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
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
