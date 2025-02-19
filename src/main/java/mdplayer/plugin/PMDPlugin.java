package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.P86Chip;
import mdplayer.chips.PpsChip;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.pmd.PMDJava;
import mdplayer.format.FileFormat;
import mdplayer.format.MMLFileFormat;
import mdsound.MDSound;
import mdsound.instrument.Ym2608Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * PMDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class PMDPlugin extends BasePlugin {

    private static final Logger logger = getLogger(PMDPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new PMDJava();
        ((PMDJava) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0] && !setting.getYM2608Type()[0].getUseEmu()[1]) {
            audio.driverReal = new PMDJava();
            ((PMDJava) audio.driverReal).setPlayingFileName(playingFileName);
        }
        boolean r = _play(format instanceof MMLFileFormat ? 0 : 1);
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _play(int fileType) {
        startTrdVgmReal();

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(0);
        chip.samplingRate = 55467;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        chip.clock = PMDJava.baseclock;
        if (chip.instrument instanceof Ym2608Inst ym2608) {
            chip.setVolumes.put("FM", ym2608::setFMVolume);
            chip.setVolumes.put("PSG", ym2608::setPSGVolume);
            chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
            chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
        }
        Function<String, Stream> fn = Common::getOPNARyhthmStream;
        chip.option = new Object[] {fn};
        put(Ym2608Chip.class, chip);
        audio.chipRegister.chip(Ym2608Chip.class).clock = PMDJava.baseclock;
        audio.chipLED.put("PriOPNA", 1);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ppz8Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ppz8Chip.class);
        chip.clock = PMDJava.baseclock;
        chip.option = null;
        audio.chipLED.put("PriPPZ8", 1);
        put(Ppz8Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(PpsChip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = 0;
        chip.clock = PMDJava.baseclock;
        chip.option = null;
        audio.chipLED.put("PriPPSDRV", 1);
        put(PpsChip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(P86Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = 0;
        chip.clock = PMDJava.baseclock;
        chip.option = null;
        audio.chipLED.put("PriP86", 1);
        put(P86Chip.class, chip);

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        audio.setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
        audio.setVolume("PSG", Ym2608Chip.class, true, setting.getBalance().getVolume("PSG", Ym2608Chip.class));
        audio.setVolume("Rhythm", Ym2608Chip.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Chip.class));
        audio.setVolume("Adpcm", Ym2608Chip.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Chip.class));

        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Reset with Psg TONE
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);

        audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, PMDJava.baseclock, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, PMDJava.baseclock, Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);

        if (!audio.driverVirtual.init(vgmBuf, fileType, this, Common.EnmModel.VirtualModel, new Class[] {Ym2608Chip.class}
                , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, fileType, this, Common.EnmModel.RealModel, new Class[] {Ym2608Chip.class}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//             SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
        }

        return true;
    }
}
