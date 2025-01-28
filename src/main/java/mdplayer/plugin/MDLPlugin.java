package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.moonDriver.MoonDriverJava;
import mdplayer.driver.moonDriver.MoonDriverJava.MoonDriverFileType;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.YmF278BInst;

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

        resetFadeOutParam();

        startTrdVgmReal();

        hiyorimiNecessary = setting.getHiyorimiMode();

        audio.chipLED.clear();
        audio.masterVolume = setting.getBalance().getMasterVolume();

        YmF278BInst ymf278b = Instrument.getInstrument(YmF278BInst.class);
//        Function<String, Stream> fn = Common.GetOPNARyhthmStream;

        if (useChipFromMdr[0] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPL4", 1);
            chip.instrument = ymf278b;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
            chip.clock = 33868800;
            chip.option = null; // new Object[] { fn };
            put(YmF278BChip.class, chip);
//            clockYM2608 = MucomDotNET.opnaBaseClock;
        }

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

//        SetYM2608Volume(true, setting.getbalance().getYM2608Volume);
//        SetYM2608FMVolume(true, setting.getbalance().getYM2608FMVolume);
//        SetYM2608PSGVolume(true, setting.getbalance().getYM2608PSGVolume);
//        SetYM2608RhythmVolume(true, setting.getbalance().getYM2608RhythmVolume);
//        SetYM2608AdpcmVolume(true, setting.getbalance().getYM2608AdpcmVolume);
//
//        audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.RealModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
//        audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
//        audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
//        audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.RealModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.RealModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.RealModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.VirtualModel);
//        audio.chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.RealModel);
//
//        audio.chipRegister.writeYM2608Clock(0, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
//        audio.chipRegister.writeYM2608Clock(1, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
//        audio.chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, EnmModel.RealModel);
//        audio.chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, EnmModel.RealModel);

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

        // Play

        audio.paused = false;

//        if (driverReal != null && setting.getYMF278BType()[0].getUseReal()[0]) {
//            realChip.WaitOPL4PCMData(setting.getYMF278BType()[0].getrealChipInfo()[0].getSoundLocation() == -1);
//        }

        oneTimeReset = false;

        return true;
    }
}
