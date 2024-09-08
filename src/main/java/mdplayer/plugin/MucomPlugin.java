package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import dotnet4j.io.Stream;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.mucom.MucomDotNET;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2610Inst;
import vavi.util.Debug;


/**
 * MucomPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MucomPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MucomDotNET();
        audio.driverVirtual.setting = setting;
        ((MucomDotNET) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
            audio.driverReal = new MucomDotNET();
            audio.driverReal.setting = setting;
            ((MucomDotNET) audio.driverReal).setPlayingFileName(playingFileName);
        }
        boolean r = mucPlay_mucomDotNET(setting, MucomDotNET.MUCOMFileType.MUB); // MucomDotNET.MUCOMFileType.MUC
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    private boolean mucPlay_mucomDotNET(Setting setting, MucomDotNET.MUCOMFileType fileType) {

        try {

            if (vgmBuf == null || setting == null) return false;

            if (fileType == MucomDotNET.MUCOMFileType.MUC) {
                vgmBuf = ((MucomDotNET) audio.driverVirtual).compile(vgmBuf);
            }
            Common.EnmChip[] useChipFromMub = ((MucomDotNET) audio.driverVirtual).useChipsFromMub(vgmBuf);

            //stop();
            audio.chipRegister.resetChips();
            resetFadeOutParam();
            audio.useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> chips = new ArrayList<>();
            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.masterVolume = setting.getBalance().getMasterVolume();

            Ym2608Inst ym2608 = new Ym2608Inst();
            Ym2610Inst ym2610 = new Ym2610Inst();
            Ym2151Inst ym2151 = new Ym2151Inst();
            Function<String, Stream> fn = Common::getOPNARyhthmStream;

            if (useChipFromMub[0] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriOPNA", 1);
                chip.instrument = ym2608;
                chip.samplingRate = 55467;
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
                chip.clock = MucomDotNET.opnaBaseClock;
                chip.setVolumes.put("FM", ym2608::setFMVolume);
                chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                chip.option = new Object[] {fn};
                chips.add(chip);
                audio.useChip.add(Common.EnmChip.YM2608);
                audio.clockYM2608 = MucomDotNET.opnaBaseClock;
            }

            if (useChipFromMub[1] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 1;
                audio.chipLED.put("SecOPNA", 1);
                chip.instrument = ym2608;
                chip.samplingRate = 55467;
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
                chip.clock = MucomDotNET.opnaBaseClock;
                chip.setVolumes.put("FM", ym2608::setFMVolume);
                chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                chip.option = new Object[] {fn};
                chips.add(chip);
                audio.useChip.add(Common.EnmChip.S_YM2608);
            }

            if (useChipFromMub[2] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriOPNB", 1);
                chip.instrument = ym2610;
                chip.samplingRate = 55467;
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2610Inst.class);
                chip.clock = MucomDotNET.opnbBaseClock;
                chip.setVolumes.put("FM", ym2610::setFMVolume);
                chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                chip.option = null;
                chips.add(chip);
                audio.useChip.add(Common.EnmChip.YM2610);
                audio.clockYM2610 = MucomDotNET.opnbBaseClock;
            }

            if (useChipFromMub[3] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 1;
                audio.chipLED.put("SecOPNB", 1);
                chip.instrument = ym2610;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2610Inst.class);
                chip.clock = MucomDotNET.opnbBaseClock;
                chip.setVolumes.put("FM", ym2610::setFMVolume);
                chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                chip.option = null;
                chips.add(chip);
                audio.useChip.add(Common.EnmChip.S_YM2610);
            }

            if (useChipFromMub[4] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriOPM", 1);
                chip.instrument = ym2151;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
                chip.clock = MucomDotNET.opmBaseClock;
                chip.option = null;
                chips.add(chip);
                audio.useChip.add(Common.EnmChip.YM2151);
            }

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, chips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, chips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(chips.toArray(new MDSound.Chip[0]));

            audio.setVolume("MAIN", Ym2608Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2608Inst.class));
            audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
            audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
            audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
            audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));

            audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
            audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x08, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x08, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x09, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x09, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, Common.EnmModel.RealModel);

            audio.chipRegister.writeYm2608Clock((byte) 0, MucomDotNET.opnaBaseClock, Common.EnmModel.RealModel);
            audio.chipRegister.writeYm2608Clock((byte) 1, MucomDotNET.opnaBaseClock, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);


            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
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

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
