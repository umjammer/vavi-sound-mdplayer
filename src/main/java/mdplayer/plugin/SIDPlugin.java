package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.driver.sid.Sid;
import mdplayer.driver.sid.Sid2;
import mdplayer.driver.sid.SidDriver;
import mdplayer.format.FileFormat;

import static java.lang.System.getLogger;


/**
 * SID (Commodore) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SIDPlugin extends BasePlugin {

    private static final Logger logger = getLogger(SIDPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Sid2();

        audio.driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Sid.Sid();
//        }
        prepare();
        boolean r = sidPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean sidPlay() {
        startTrdVgmReal();

        audio.chipLED.put("priSID", 1);

        ((SidDriver) audio.driverVirtual).setSong(songNo + 1);
        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            ((SidDriver) audio.driverReal).setSong(songNo + 1);
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        return true;
    }
}
