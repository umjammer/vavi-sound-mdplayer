package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.pmd.PMDJava;
import mdplayer.format.FileFormat;
import mdplayer.format.MMLFileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.PpsDrvInst;
import mdsound.instrument.Ppz8Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.P86Inst;

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
        boolean r = mmlPlay_PMDDotNET(format instanceof MMLFileFormat ? 0 : 1);
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mmlPlay_PMDDotNET(int fileType) {

        try {
            if (vgmBuf == null || setting == null) return false;

            //Stop();

            audio.chipRegister.resetChips();
            resetFadeOutParam();
            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipRegister.chipLED.clear();
            audio.masterVolume = setting.getBalance().getMasterVolume();

            Ym2608Inst ym2608 = Instrument.getInstrument(Ym2608Inst.class);
            chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipRegister.chipLED.put("PriOPNA", 1);
            chip.instrument = ym2608;
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Inst.class);
            chip.clock = PMDJava.baseclock;
            chip.setVolumes.put("FM", ym2608::setFMVolume);
            chip.setVolumes.put("PSG", ym2608::setPSGVolume);
            chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
            chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
            Function<String, Stream> fn = Common::getOPNARyhthmStream;
            chip.option = new Object[] {fn};
            lstChips.add(chip);
            useChip.add(Common.EnmChip.YM2608);
//            audio.clockYM2608 = PMDJava.baseclock;

            Ppz8Inst ppz8 = Instrument.getInstrument(Ppz8Inst.class);
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = ppz8;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ppz8Inst.class);
            chip.clock = PMDJava.baseclock;
            chip.option = null;
            audio.chipRegister.chipLED.put("PriPPZ8", 1);
            lstChips.add(chip);
            useChip.add(Common.EnmChip.PPZ8);


            PpsDrvInst ppsdrv = Instrument.getInstrument(PpsDrvInst.class);
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = ppsdrv;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = 0;
            chip.clock = PMDJava.baseclock;
            chip.option = null;
            audio.chipRegister.chipLED.put("PriPPSDRV", 1);
            lstChips.add(chip);
            useChip.add(Common.EnmChip.PPSDRV);


            P86Inst P86 = Instrument.getInstrument(P86Inst.class);
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = P86;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = 0;
            chip.clock = PMDJava.baseclock;
            chip.option = null;
            audio.chipRegister.chipLED.put("PriP86", 1);
            lstChips.add(chip);
            useChip.add(Common.EnmChip.P86);

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips.toArray(MDSound.Chip[]::new));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            audio.setVolume(MAIN_TAG, Ym2608Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Inst.class));
            audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
            audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
            audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
            audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));

            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);

            audio.chipRegister.chip(Ym2608Chip.class).writeYm2608Clock((byte) 0, PMDJava.baseclock, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).writeYm2608Clock((byte) 1, PMDJava.baseclock, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);


            if (!audio.driverVirtual.init(vgmBuf, fileType, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, fileType, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            // Play

            audio.paused = false;

            if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//                SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
            }

            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
