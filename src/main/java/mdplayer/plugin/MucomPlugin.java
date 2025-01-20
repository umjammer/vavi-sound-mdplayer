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
import mdplayer.Setting;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.mucom.MucomJava;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2610Inst;
import mdsound.instrument.YmFmYm2608Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MucomPlugin.
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
        boolean r = mucPlay_mucomDotNET(setting, MucomJava.MUCOMFileType.MUB); // MucomDotNET.MUCOMFileType.MUC
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    private boolean mucPlay_mucomDotNET(Setting setting, MucomJava.MUCOMFileType fileType) {

        try {
            if (vgmBuf == null || setting == null) return false;

            if (fileType == MucomJava.MUCOMFileType.MUC) {
                vgmBuf = ((MucomJava) audio.driverVirtual).compile(vgmBuf);
            }
            Common.EnmChip[] useChipFromMub = ((MucomJava) audio.driverVirtual).useChipsFromMub(vgmBuf);

            //stop();
            audio.chipRegister.resetChips();
            resetFadeOutParam();
            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> chips = new ArrayList<>();
            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipRegister.chipLED.clear();
            audio.masterVolume = setting.getBalance().getMasterVolume();

            Ym2610Inst ym2610 = Instrument.getInstrument(Ym2610Inst.class);
            Ym2151Inst ym2151 = Instrument.getInstrument(Ym2151Inst.class);
            Function<String, Stream> fn = Common::getOPNARyhthmStream;

            if (useChipFromMub[0] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipRegister.chipLED.put("PriOPNA", 1);

                if (setting.getYM2608Type()[0].getUseEmu()[0]) {
                    Ym2608Inst ym2608 = Instrument.getInstrument(Ym2608Inst.class);
                    chip.instrument = ym2608;
                    chip.setVolumes.put("FM", ym2608::setFMVolume);
                    chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                    chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                    chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                } else if (setting.getYM2608Type()[0].getUseEmu()[1]) {
                    YmFmYm2608Inst ym2608 = Instrument.getInstrument(YmFmYm2608Inst.class);
                    chip.instrument = ym2608;
                }

                chip.samplingRate = 55467;
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass() /* Ym2608Inst.class */);
                chip.clock = MucomJava.opnaBaseClock;
                chip.option = new Object[] {fn};
                chips.add(chip);
                useChip.add(Common.EnmChip.YM2608);
//                audio.clockYM2608 = MucomJava.opnaBaseClock;
            }

            if (useChipFromMub[1] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 1;
                audio.chipRegister.chipLED.put("SecOPNA", 1);


                if (setting.getYM2608Type()[1].getUseEmu()[0]) {
                    Ym2608Inst ym2608 = Instrument.getInstrument(Ym2608Inst.class);
                    chip.instrument = ym2608;
                    chip.setVolumes.put("FM", ym2608::setFMVolume);
                    chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                    chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                    chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                } else if (setting.getYM2608Type()[1].getUseEmu()[1]) {
                    YmFmYm2608Inst ym2608 = Instrument.getInstrument(YmFmYm2608Inst.class);
                    chip.instrument = ym2608;
                }

                chip.samplingRate = 55467;
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Inst.class);
                chip.clock = MucomJava.opnaBaseClock;
                chip.option = new Object[] {fn};
                chips.add(chip);
                useChip.add(Common.EnmChip.S_YM2608);
            }

            if (useChipFromMub[2] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipRegister.chipLED.put("PriOPNB", 1);
                chip.instrument = ym2610;
                chip.samplingRate = 55467;
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Inst.class);
                chip.clock = MucomJava.opnbBaseClock;
                chip.setVolumes.put("FM", ym2610::setFMVolume);
                chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                chip.option = null;
                chips.add(chip);
                useChip.add(Common.EnmChip.YM2610);
//                audio.clockYM2610 = MucomJava.opnbBaseClock;
            }

            if (useChipFromMub[3] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 1;
                audio.chipRegister.chipLED.put("SecOPNB", 1);
                chip.instrument = ym2610;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Inst.class);
                chip.clock = MucomJava.opnbBaseClock;
                chip.setVolumes.put("FM", ym2610::setFMVolume);
                chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                chip.option = null;
                chips.add(chip);
                useChip.add(Common.EnmChip.S_YM2610);
            }

            if (useChipFromMub[4] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipRegister.chipLED.put("PriOPM", 1);
                chip.instrument = ym2151;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class);
                chip.clock = MucomJava.opmBaseClock;
                chip.option = null;
                chips.add(chip);
                useChip.add(Common.EnmChip.YM2151);
            }

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, chips.toArray(MDSound.Chip[]::new));

            audio.chipRegister.initChipRegister(chips.toArray(new MDSound.Chip[0]));

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
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x08, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x08, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x09, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x09, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x0a, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608Register(0, 0, 0x0a, 0x00, Common.EnmModel.RealModel);

            audio.chipRegister.chip(Ym2608Chip.class).writeYm2608Clock((byte) 0, MucomJava.opnaBaseClock, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).writeYm2608Clock((byte) 1, MucomJava.opnaBaseClock, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2608Chip.class).setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);


            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            audio.paused = false;

            if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//                SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
            }

            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
