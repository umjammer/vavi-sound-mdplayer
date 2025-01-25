package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.mndrv.MnDrv;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.OkiM6258Inst;
import mdsound.instrument.X68kMPcmInst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68SoundYm2151Inst;

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
        boolean r = mndPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mndPlay() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            audio.chipRegister.reset();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.reset();

            useChip.clear();

            startTrdVgmReal();

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 3;

            audio.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip = null;

            if (setting.getYM2151Type()[0].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(Ym2151Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[1]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(MameYm2151Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, MameYm2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[2]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(X68SoundYm2151Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, X68SoundYm2151Inst.class);
                chip.clock = 4000000;
                chip.option = new Object[] {1, 0, 0};
            }
            if (chip != null) {
                lstChips.add(chip);
            }
            useChip.add(Common.EnmChip.YM2151);

            Ym2608Inst opna = Instrument.getInstrument(Ym2608Inst.class);
            if (setting.getYM2608Type()[0].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = opna;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Inst.class);
                chip.clock = 8000000; // 7987200;
                chip.setVolumes.put("FM", opna::setFMVolume);
                chip.setVolumes.put("PSG", opna::setPSGVolume);
                chip.setVolumes.put("Rhythm", opna::setRhythmVolume);
                chip.setVolumes.put("Adpcm", opna::setAdpcmVolume);
                Function<String, Stream> fn = Common::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                lstChips.add(chip);
//                audio.clockYM2608 = 8000000;
            }
            useChip.add(Common.EnmChip.YM2608);

            if (setting.getYM2608Type()[1].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 1;
                chip.instrument = opna;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Inst.class);
                chip.clock = 8000000; // 7987200;
                chip.option = new Object[] {Common.getApplicationFolder()};
                lstChips.add(chip);
//                audio.clockYM2608 = 8000000;
            }
            useChip.add(Common.EnmChip.S_YM2608);

            X68kMPcmInst mpcm = Instrument.getInstrument(X68kMPcmInst.class);
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = mpcm;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, OkiM6258Inst.class);
            chip.clock = 15600;
            chip.option = new Object[] {Common.getApplicationFolder()};
            lstChips.add(chip);
            useChip.add(Common.EnmChip.OKIM6258);

            audio.chipLED.put("PriOPM", 1);
            audio.chipLED.put("PriOPNA", 1);
            audio.chipLED.put("SecOPNA", 1);
            audio.chipLED.put("PriOKI5", 1);

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips);

            if (useChip.contains(Common.EnmChip.YM2151) || useChip.contains(Common.EnmChip.S_YM2151)) {
                audio.setVolume(MAIN_TAG, Ym2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class));
                audio.setVolume(MAIN_TAG, MameYm2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, MameYm2151Inst.class));
                audio.setVolume(MAIN_TAG, X68SoundYm2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, X68SoundYm2151Inst.class));
            }

            if (useChip.contains(Common.EnmChip.YM2608) || useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.setVolume(MAIN_TAG, Ym2608Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Inst.class));
                audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
                audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
                audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
                audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));
            }

            Thread.sleep(500);

            if (useChip.contains(Common.EnmChip.YM2608)) {
                audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
                audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, 8000000, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).setVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            }

            if (useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
                audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x07, 0x38, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, 8000000, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2608Chip.class).setVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            }

            if (useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
            if (useChip.contains(Common.EnmChip.S_YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, 4000000, Common.EnmModel.RealModel);

            audio.driverVirtual.setYm2151Hosei(4000000);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);

            if (useChip.contains(Common.EnmChip.YM2203))
                audio.chipRegister.chip(Ym2203Chip.class).setVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
            if (useChip.contains(Common.EnmChip.S_YM2203))
                audio.chipRegister.chip(Ym2203Chip.class).setVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);

            boolean retV = audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2151, Common.EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
            );
            boolean retR = true;
            if (audio.driverReal != null) {
                retR = audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2151, Common.EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
                );
            }

            if (!retV || !retR) {
                audio.errMsg = !audio.driverVirtual.errMsg.isEmpty() ? audio.driverVirtual.errMsg : (audio.driverReal != null ? audio.driverReal.errMsg : "");
                return false;
            }

            ((MnDrv) audio.driverVirtual).mpcm = mpcm;

            audio.paused = false;
            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
