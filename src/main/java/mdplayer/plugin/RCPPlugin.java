package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.rcp.RCP;
import mdplayer.format.FileFormat;

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
        startTrdVgmReal();

        audio.chipLED.put("PriMID", 1);
        audio.chipLED.put("SecMID", 1);

        audio.chipRegister.plugin(MidiPlugin.class).releaseAll();
        audio.chipRegister.plugin(MidiPlugin.class).make(setting, midiMode);
        audio.chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(midiMode));

        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        return true;
    }
}
