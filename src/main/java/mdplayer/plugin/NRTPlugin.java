package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.nrtdrv.NRTDRV;
import mdplayer.format.FileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * NRTDRV (X1) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class NRTPlugin extends BasePlugin {

    private static final Logger logger = getLogger(NRTPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new NRTDRV();
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new NRTDRV();
//        }
        prepare();
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
        int r = ((NRTDRV) audio.driverVirtual).checkUseChip(vgmBuf);
logger.log(Level.DEBUG, "used chip: %02x".formatted(r));

        audio.chipRegister.chip(Ym2151Chip.class).setFadeout(0, 0);
        audio.chipRegister.chip(Ym2151Chip.class).setFadeout(1, 0);

        startTrdVgmReal();

        int hiyorimiDeviceFlag = 0;

        for (int i = 0; i < 2; i++) {
            if ((i == 0 && (r & 0x3) != 0) || (i == 1 && (r & 0x2) != 0)) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(i);
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
                chip.clock = 4000000;
                chip.samplingRate = chip.clock / 64;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPM", 1);
                else audio.chipLED.put("SecOPM", 1);

                put(Ym2151Chip.class, chip);
            }
        }

        if ((r & 0x4) != 0) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = 2000000 / 2;
            audio.chipRegister.chip(Ay8910Chip.class).clock = chip.clock;
            chip.option = null;

            hiyorimiDeviceFlag |= 0x1;
            audio.chipLED.put("PriAY10", 1);

            put(Ay8910Chip.class, chip);
        }

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (contains(Ym2151Chip.class, 0) || contains(Ym2151Chip.class, 1)) {
            audio.setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));
        }
        if (contains(Ay8910Chip.class, 0))
            audio.setVolume(MAIN_TAG, Ay8910Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class));

        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 1))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, 4000000, Common.EnmModel.RealModel);

        if (audio.driverVirtual != null) audio.driverVirtual.setYm2151Hosei(4000000);
        if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);
//            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        if (audio.driverVirtual != null) {
            audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                    new Class[] {Ym2151Chip.class, Ay8910Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            ((NRTDRV) audio.driverVirtual).call(0); //
        }

        if (audio.driverReal != null) {
            audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ym2151Chip.class, Ay8910Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            ((NRTDRV) audio.driverReal).call(0); //
        }

        ((NRTDRV) audio.driverVirtual).call(1); // MPLAY

        if (audio.driverReal != null) {
            ((NRTDRV) audio.driverReal).call(1); // MPLAY
        }

        return true;
    }
}
