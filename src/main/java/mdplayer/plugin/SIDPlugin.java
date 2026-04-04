package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.driver.sid.SidMdDriver2;

import static java.lang.System.getLogger;


/**
 * SID (Commodore) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SIDPlugin extends BasePlugin<SidMdDriver2> {

    private static final Logger logger = getLogger(SIDPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new SidMdDriver2();

        driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Sid.Sid();
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        startTrdVgmReal();

        chipLED.put("priSID", 1);

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                songNo + 1);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    songNo + 1);
        }
    }
}
