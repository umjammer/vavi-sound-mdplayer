package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.OkiM6258Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.mndrv.MnDrv;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.X68kMPcmInst;
import mdsound.instrument.Ym2608Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MNDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MNDPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MNDPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MnDrv();

        ((MnDrv) audio.driverVirtual).extendFile = extendFile;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MnDrv();
            ((MnDrv) audio.driverReal).extendFile = extendFile;
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

        int hiyorimiDeviceFlag = 3;

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        chip.clock = 4000000;
        chip.option = null;
        put(Ym2151Chip.class, chip);

        if (setting.getYM2608Type()[0].getUseEmu()[0]) {
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(0);
            chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
            chip.clock = 8000000; // 7987200;
            if (chip.instrument instanceof Ym2608Inst opna) {
                chip.setVolumes.put("FM", opna::setFMVolume);
                chip.setVolumes.put("PSG", opna::setPSGVolume);
                chip.setVolumes.put("Rhythm", opna::setRhythmVolume);
                chip.setVolumes.put("Adpcm", opna::setAdpcmVolume);
            }
            Function<String, Stream> fn = Common::getOPNARyhthmStream;
            chip.option = new Object[] {fn};
            put(Ym2608Chip.class, chip);
//            audio.clockYM2608 = 8000000;
        }

        if (setting.getYM2608Type()[1].getUseEmu()[0]) {
            chip = new MDSound.Chip();
            chip.id = 1;
            chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(1);
            chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
            chip.clock = 8000000; // 7987200;
            chip.option = new Object[] {Common.getApplicationFolder()};
            put(Ym2608Chip.class, chip);
//            audio.clockYM2608 = 8000000;
        }

        X68kMPcmInst mpcm = Instrument.getInstrument(X68kMPcmInst.class);
        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = mpcm;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, OkiM6258Chip.class);
        chip.clock = 15600;
        chip.option = new Object[] {Common.getApplicationFolder()};
        put(OkiM6258Chip.class, chip);

        audio.chipLED.put("PriOPM", 1);
        audio.chipLED.put("PriOPNA", 1);
        audio.chipLED.put("SecOPNA", 1);
        audio.chipLED.put("PriOKI5", 1);

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (contains(Ym2151Chip.class, 0) || contains(Ym2151Chip.class, 1)) {
            audio.setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));
        }

        if (contains(Ym2608Chip.class, 0) || contains(Ym2608Chip.class, 1)) {
            audio.setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
            audio.setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
            audio.setVolume("PSG", Ym2608Chip.class, true, setting.getBalance().getVolume("PSG", Ym2608Chip.class));
            audio.setVolume("Rhythm", Ym2608Chip.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Chip.class));
            audio.setVolume("Adpcm", Ym2608Chip.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Chip.class));
        }

        sleep(500);

        if (contains(Ym2608Chip.class, 0)) {
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Reset with Psg TONE
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, 8000000, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        }

        if (contains(Ym2608Chip.class, 0)) {
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Reset with Psg TONE
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x07, 0x38, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, 8000000, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        }

        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 1))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, 4000000, Common.EnmModel.RealModel);

        audio.driverVirtual.setYm2151Hosei(4000000);
        if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);

        if (contains(Ym2203Chip.class, 0))
            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 1))
            audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);

        boolean retV = audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ym2151Chip.class, Ym2608Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
        );
        boolean retR = true;
        if (audio.driverReal != null) {
            retR = audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ym2151Chip.class, Ym2608Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        if (!retV || !retR) {
            audio.errMsg = !audio.driverVirtual.errMsg.isEmpty() ? audio.driverVirtual.errMsg : (audio.driverReal != null ? audio.driverReal.errMsg : "");
            return false;
        }

        ((MnDrv) audio.driverVirtual).mpcm = mpcm;

        return true;
    }
}
