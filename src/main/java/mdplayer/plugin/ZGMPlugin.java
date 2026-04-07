package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Common;
import mdplayer.driver.zgm.Zgm;

import static java.lang.System.getLogger;


/**
 * ZGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class ZGMPlugin extends BasePlugin<Zgm> {

    private static final Logger logger = getLogger(ZGMPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new Zgm();

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            driverReal = new Zgm();
        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        // Sealed until MIDI is supported
//        startTrdVgmReal();

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);

        // Sealed until MIDI is supported
//            if (driverReal != null)
//                 driverReal.init(dataBuf, this, EnmModel.RealModel,
//                    new EnmChip[] {EnmChip.YM2203},
//                    (int) (setting.getoutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000),
//                    (int) (setting.getoutputDevice().getSampleRate() * setting.getoutputDevice().getWaitTime() / 1000));

        int hiyorimiDeviceFlag = 0;

        //
        // chips initialization
        //

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());
    }
}
