package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.moonDriver.MoonDriverJava;
import mdplayer.driver.moonDriver.MoonDriverJava.MoonDriverFileType;
import mdplayer.driver.mucom.MucomJava;
import mdplayer.format.FileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MDLPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDLPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MDLPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MoonDriverJava();
        ((MoonDriverJava) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0] && !setting.getYM2608Type()[0].getUseEmu()[1]) {
            audio.driverReal = new MoonDriverJava();
            ((MoonDriverJava) audio.driverReal).setPlayingFileName(playingFileName);
        }

        boolean r = _play(MoonDriverFileType.MDL);
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _play(MoonDriverFileType fileType) {
        if (fileType == MoonDriverJava.MoonDriverFileType.MDL) {
            vgmBuf = ((MoonDriverJava) audio.driverVirtual).compile(vgmBuf);
        }

        Class<? extends Chip>[] useChipFromMdr = new Class[] {YmF278BChip.class};

        startTrdVgmReal();

//        Function<String, Stream> fn = Common.GetOPNARyhthmStream;

        if (useChipFromMdr[0] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPL4", 1);
            chip.instrument = audio.chipRegister.chip(YmF278BChip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
            chip.clock = 33868800;
            chip.option = null; // new Object[] { fn };
            put(YmF278BChip.class, chip);
            audio.chipRegister.chip(Ym2608Chip.class).clock = MucomJava.opnaBaseClock;
        }

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

//        SetYM2608Volume(true, setting.getBalance().getVolume(MAIN_TAG, YM2608Chip.class);
//        SetYM2608FMVolume(true, setting.getBalance().getVolume(FM, YM2608Chip.class);
//        SetYM2608PSGVolume(true, setting.getBalance().getVolume(PSG, YM2608Chip.class);
//        SetYM2608RhythmVolume(true, setting.getBalance().getVolume(Rhythm, YM2608Chip.class);
//        SetYM2608AdpcmVolume(true, setting.getBalance().getVolume(Adpcm, YM2608Chip.class);
//
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Reset with Psg TONE
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, EnmModel.RealModel);
//
//        audio.chipRegister.chip(Ym2608Chip.class).writeClock(0, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).writeClock(1, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).setSSGVolume(0, setting.getBalance().getGimicOPNAVolume, EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2608Chip.class).setSSGVolume(1, setting.getBalance().getGimicOPNAVolume, EnmModel.RealModel);

        if (!audio.driverVirtual.init(
                vgmBuf,
                this,
                Common.EnmModel.VirtualModel,
                new Class[] {YmF278BChip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(
                    vgmBuf,
                    this,
                    Common.EnmModel.RealModel,
                    new Class[] {YmF278BChip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

//        if (driverReal != null && setting.getYMF278BType()[0].getUseReal()[0]) {
//            realChip.WaitOPL4PCMData(setting.getYMF278BType()[0].getrealChipInfo()[0].getSoundLocation() == -1);
//        }

        return true;
    }
}
