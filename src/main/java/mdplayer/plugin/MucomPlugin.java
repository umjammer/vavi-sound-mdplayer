package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.driver.mucom.MucomJava;
import mdplayer.driver.mucom.MucomJava.MUCOMFileType;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2610Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * Mucom88 (PC-8801) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MucomPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MucomPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MucomJava();
        ((MucomJava) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0] && !setting.getYM2608Type()[0].getUseEmu()[1]) {
            audio.driverReal = new MucomJava();
            ((MucomJava) audio.driverReal).setPlayingFileName(playingFileName);
        }
        boolean r = _play(setting, MUCOMFileType.MUB); // MucomDotNET.MUCOMFileType.MUC
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _play(Setting setting, MUCOMFileType fileType) {
        if (fileType == MUCOMFileType.MUC) {
            vgmBuf = ((MucomJava) audio.driverVirtual).compile(vgmBuf);
        }
        Class<? extends Chip>[] useChipFromMub = ((MucomJava) audio.driverVirtual).useChipsFromMub(vgmBuf);

        startTrdVgmReal();

        Function<String, Stream> fn = Common::getOPNARyhthmStream;

        if (useChipFromMub[0] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(0);
            if (chip.instrument instanceof Ym2608Inst ym2608) {
                chip.setVolumes.put("FM", ym2608::setVolume);
                chip.setVolumes.put("SSG", ym2608::setVolume);
                chip.setVolumes.put("RHYTHM", ym2608::setVolume);
                chip.setVolumes.put("ADPCM", ym2608::setVolume);
            }
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
            chip.clock = MucomJava.opnaBaseClock;
            chip.option = new Object[] {fn};
            put(Ym2608Chip.class, chip);

            audio.chipLED.put("PriOPNA", 1);
            audio.chipRegister.chip(Ym2608Chip.class).clock = MucomJava.opnaBaseClock;
        }

        if (useChipFromMub[1] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 1;
            chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(0);
            if (chip.instrument instanceof Ym2608Inst ym2608) {
                chip.setVolumes.put("FM", ym2608::setVolume);
                chip.setVolumes.put("SSG", ym2608::setVolume);
                chip.setVolumes.put("RHYTHM", ym2608::setVolume);
                chip.setVolumes.put("ADPCM", ym2608::setVolume);
            }
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
            chip.clock = MucomJava.opnaBaseClock;
            chip.option = new Object[] {fn};
            put(Ym2608Chip.class, chip);

            audio.chipLED.put("SecOPNA", 1);
        }

        if (useChipFromMub[2] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPNB", 1);
            chip.instrument = audio.chipRegister.chip(Ym2610Chip.class).instrument(0);
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Chip.class);
            chip.clock = MucomJava.opnbBaseClock;
            if (chip.instrument instanceof Ym2610Inst ym2610) {
                chip.setVolumes.put("FM", ym2610::setVolume);
                chip.setVolumes.put("SSG", ym2610::setVolume);
                chip.setVolumes.put("ADPCMA", ym2610::setVolume);
                chip.setVolumes.put("ADPCMB", ym2610::setVolume);
            }
            chip.option = null;
            put(Ym2610Chip.class, chip);
            audio.chipRegister.chip(Ym2610Chip.class).clock = MucomJava.opnbBaseClock;
        }

        if (useChipFromMub[3] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 1;
            audio.chipLED.put("SecOPNB", 1);
            chip.instrument = audio.chipRegister.chip(Ym2610Chip.class).instrument(1);
            chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Chip.class);
            chip.clock = MucomJava.opnbBaseClock;
            if (chip.instrument instanceof Ym2610Inst ym2610) {
                chip.setVolumes.put("FM", ym2610::setVolume);
                chip.setVolumes.put("PSG", ym2610::setVolume);
                chip.setVolumes.put("ADPCMA", ym2610::setVolume);
                chip.setVolumes.put("ADPCMB", ym2610::setVolume);
            }
            chip.option = null;
            put(Ym2610Chip.class, chip);
        }

        if (useChipFromMub[4] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPM", 1);
            chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(0);
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
            chip.clock = MucomJava.opmBaseClock;
            chip.samplingRate = chip.clock / 64;
            chip.option = null;
            put(Ym2151Chip.class, chip);
        }

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        audio.setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
        audio.setVolume("SSG", Ym2608Chip.class, true, setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        audio.setVolume("RHYTHM", Ym2608Chip.class, true, setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        audio.setVolume("ADPCM", Ym2608Chip.class, true, setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Reset with Psg TONE
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, Common.EnmModel.RealModel);

        audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, MucomJava.opnaBaseClock, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, MucomJava.opnaBaseClock, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);


        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ym2608Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ym2608Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//            SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
        }

        return true;
    }
}
