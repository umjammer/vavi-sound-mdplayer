package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.driver.sid.SidMdDriver;
import mdplayer.plugin.BasePlugin.HasSongNo;

import static java.lang.System.getLogger;


/**
 * SID (Commodore) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SIDPlugin extends BasePlugin<SidMdDriver> implements HasSongNo {

    private static final Logger logger = getLogger(SIDPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new SidMdDriver(this);

        driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new SidMdDriver(this);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        chipLED.put("priSID", 1);

        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                songNo + 1);
        if (driverReal != null) {
            driverReal.init(Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    songNo + 1);
        }
    }

    @Override
    public void setSongNo(int songNo) {
logger.log(Level.INFO, "songNo: " + songNo);
        this.songNo = songNo + 1;
    }
}
