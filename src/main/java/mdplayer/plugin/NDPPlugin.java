package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.mgsdrv.MgsDrv;
import mdplayer.driver.ndp.Ndp;
import mdplayer.driver.ndp.NdpDriver;
import mdsound.MDSound;
import mdsound.instrument.MameAy8910Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * NDP (MSX) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-10 nsano initial version <br>
 */
public class NDPPlugin extends BasePlugin<NdpDriver> {

    private static final Logger logger = getLogger(NDPPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new NdpDriver();
//        driverVirtual.setPlayingFileName(playingFileName);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new NdpDriver();
//            driverReal.setPlayingFileName(playingFileName);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        boolean useAY = true;
        boolean useSCC = false;
        boolean useOPLL = false;

        if (useAY) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriAY10", 1);
            chip.instrument = chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = MgsDrv.baseClockAY8910 / 2;
            chip.option = null;
            if (chip.instrument instanceof MameAy8910Inst) {
                chip.option = new Object[] {
                        (byte) (setting.getAY8910Type()[0].getYM2149mode() ? 0x10 : 0x00), // chip_type 0x10:YM2149 0x00:AY
                        (byte) 0x00  // chip_flag
                };
            }
            put(Ay8910Chip.class, chip);
            chipRegister.chip(Ay8910Chip.class).clock = Ndp.baseClockAY8910;
        }

        if (useOPLL) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriOPLL", 1);
            chip.instrument = chipRegister.chip(Ym2413Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
            chip.clock = MgsDrv.baseClockYM2413;
            chip.option = null;
            put(Ym2413Chip.class, chip);
            chipRegister.chip(Ym2413Chip.class).clock = Ndp.baseClockYM2413;
        }

        if (useSCC) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriK051649", 1);
            chip.instrument = chipRegister.chip(K051649Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
            chip.clock = MgsDrv.baseClockK051649;
            chip.option = null;
            put(K051649Chip.class, chip);
            chipRegister.chip(K051649Chip.class).clock = Ndp.baseClockK051649;
        }

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        if (useOPLL) {
            chipRegister.chip(Ym2413Chip.class).write(0, 14, 32, EnmModel.VirtualModel);
        }

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }
}
