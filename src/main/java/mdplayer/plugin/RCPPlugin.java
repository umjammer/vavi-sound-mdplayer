package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.rcp.RcpDriver;

import static java.lang.System.getLogger;


/**
 * RCPPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class RCPPlugin extends BasePlugin<RcpDriver> {

    private static final Logger logger = getLogger(RCPPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new RcpDriver();
        driverVirtual.setExtendFile(extendFiles);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new RcpDriver();
//            driverReal.setExtendFile(extendFile);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        startTrdVgmReal();

        chipLED.put("PriMID", 1);
        chipLED.put("SecMID", 1);

        chipRegister.plugin(MidiPlugin.class).releaseAll();
        chipRegister.plugin(MidiPlugin.class).make(setting, midiMode);
//        chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(midiMode));

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }
}
