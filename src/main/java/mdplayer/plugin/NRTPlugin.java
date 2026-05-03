package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.nrtdrv.NrtDriver;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/** NRTDRV (X1) Plugin. */
public class NRTPlugin extends BasePlugin<NrtDriver> {

    private static final Logger logger = getLogger(NRTPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new NrtDriver(this);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new NrtDriver(this);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        int r = driverVirtual.checkUseChip(dataBuf);
logger.log(Level.DEBUG, "used chip: %02x".formatted(r));

        chipRegister.chip(Ym2151Chip.class).setFadeout(0, 0);
        chipRegister.chip(Ym2151Chip.class).setFadeout(1, 0);

        int hiyorimiDeviceFlag = 0;

        for (int i = 0; i < 2; i++) {
            if ((i == 0 && (r & 0x3) != 0) || (i == 1 && (r & 0x2) != 0)) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = chipRegister.chip(Ym2151Chip.class).instrument(i);
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
                chip.clock = 4000000;
                chip.samplingRate = chip.clock / 64;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) chipLED.put("PriOPM", 1);
                else chipLED.put("SecOPM", 1);

                put(Ym2151Chip.class, chip);
            }
        }

        if ((r & 0x4) != 0) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = 2000000 / 2;
            chipRegister.chip(Ay8910Chip.class).clock = chip.clock;
            chip.option = null;

            hiyorimiDeviceFlag |= 0x1;
            chipLED.put("PriAY10", 1);

            put(Ay8910Chip.class, chip);
        }

        chipRegister.plugin(RealChipPlugin.class).initChip(hiyorimiDeviceFlag);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        if (contains(Ym2151Chip.class, 0) || contains(Ym2151Chip.class, 1)) {
            setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));
        }
        if (contains(Ay8910Chip.class, 0))
            setVolume(MAIN_TAG, Ay8910Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class));

        if (contains(Ym2151Chip.class, 0))
            chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 1))
            chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, 4000000, EnmModel.RealModel);

        chipRegister.chip(Ym2151Chip.class).setCorrection(EnmModel.VirtualModel,4000000);
        if (driverReal != null) chipRegister.chip(Ym2151Chip.class).setCorrection(EnmModel.RealModel, 4000000);
//            chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//            chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//            chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//            chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        driverVirtual.init(EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        driverVirtual.call(0); //

        if (driverReal != null) {
            driverReal.init(EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            driverReal.call(0); //
        }

        driverVirtual.call(1); // MPLAY

        if (driverReal != null) {
            driverReal.call(1); // MPLAY
        }
    }
}
