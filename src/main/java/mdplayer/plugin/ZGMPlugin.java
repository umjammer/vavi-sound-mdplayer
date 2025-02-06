package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Ym2203Chip;
import mdplayer.driver.zgm.Zgm;
import mdplayer.format.FileFormat;

import static java.lang.System.getLogger;


/**
 * ZGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class ZGMPlugin extends BasePlugin {

    private static final Logger logger = getLogger(ZGMPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Zgm();
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new Zgm();
        }

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
        // Sealed until MIDI is supported
//        startTrdVgmReal();

        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ym2203Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;

        // Sealed until MIDI is supported
//            if (driverReal != null && !driverReal.init(vgmBuf, this, EnmModel.RealModel,
//                    new EnmChip[] {EnmChip.YM2203},
//                    (int) (setting.getoutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000),
//                    (int) (setting.getoutputDevice().getSampleRate() * setting.getoutputDevice().getWaitTime() / 1000)))
//                return false;

        int hiyorimiDeviceFlag = 0;

        //
        // chips initialization
        //

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        return true;
    }
}
