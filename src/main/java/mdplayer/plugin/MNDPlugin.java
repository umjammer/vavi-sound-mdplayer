package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import dotnet4j.io.Stream;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.mndrv.MnDrv;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.OkiM6258Inst;
import mdsound.instrument.X68kMPcmInst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68SoundYm2151Inst;
import vavi.util.Debug;


/**
 * MNDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MNDPlugin extends BasePlugin {
    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MnDrv();
        audio.driverVirtual.setting = setting;

        ((MnDrv) audio.driverVirtual).extendFile = extendFile;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MnDrv();
            audio.driverReal.setting = setting;
            ((MnDrv) audio.driverReal).extendFile = extendFile;
        }
        boolean r = mndPlay();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mndPlay() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            audio.chipRegister.resetChips();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            audio.vgmRealFadeoutVol = 0;
            audio.vgmRealFadeoutVolWait = 4;

            audio.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            audio.useChip.clear();

            startTrdVgmReal();

            audio.hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 3;

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip = null;

            if (setting.getYM2151Type()[0].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = new Ym2151Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[1]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = new MameYm2151Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", MameYm2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[2]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = new X68SoundYm2151Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", X68SoundYm2151Inst.class);
                chip.clock = 4000000;
                chip.option = new Object[] {1, 0, 0};
            }
            if (chip != null) {
                lstChips.add(chip);
            }
            audio.useChip.add(Common.EnmChip.YM2151);

            Ym2608Inst opna = new Ym2608Inst();
            if (setting.getYM2608Type()[0].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = opna;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
                chip.clock = 8000000; // 7987200;
                chip.setVolumes.put("FM", opna::setFMVolume);
                chip.setVolumes.put("PSG", opna::setPSGVolume);
                chip.setVolumes.put("Rhythm", opna::setRhythmVolume);
                chip.setVolumes.put("Adpcm", opna::setAdpcmVolume);
                Function<String, Stream> fn = Common::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                lstChips.add(chip);
                audio.clockYM2608 = 8000000;
            }
            audio.useChip.add(Common.EnmChip.YM2608);

            if (setting.getYM2608Type()[1].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 1;
                chip.instrument = opna;
                chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
                chip.clock = 8000000; // 7987200;
                chip.option = new Object[] {Common.getApplicationFolder()};
                lstChips.add(chip);
                audio.clockYM2608 = 8000000;
            }
            audio.useChip.add(Common.EnmChip.S_YM2608);

            X68kMPcmInst mpcm = new X68kMPcmInst();
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = mpcm;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume("MAIN", OkiM6258Inst.class);
            chip.clock = 15600;
            chip.option = new Object[] {Common.getApplicationFolder()};
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.OKIM6258);

            audio.chipLED.put("PriOPM", 1);
            audio.chipLED.put("PriOPNA", 1);
            audio.chipLED.put("SecOPNA", 1);
            audio.chipLED.put("PriOKI5", 1);

            audio.hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && audio.hiyorimiNecessary;

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (audio.useChip.contains(Common.EnmChip.YM2151) || audio.useChip.contains(Common.EnmChip.S_YM2151)) {
                audio.setVolume("MAIN", Ym2151Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2151Inst.class));
                audio.setVolume("MAIN", MameYm2151Inst.class, true, setting.getBalance().getVolume("MAIN", MameYm2151Inst.class));
                audio.setVolume("MAIN", X68SoundYm2151Inst.class, true, setting.getBalance().getVolume("MAIN", X68SoundYm2151Inst.class));
            }

            if (audio.useChip.contains(Common.EnmChip.YM2608) || audio.useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.setVolume("MAIN", Ym2608Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2608Inst.class));
                audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
                audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
                audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
                audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));
            }

            Thread.sleep(500);

            if (audio.useChip.contains(Common.EnmChip.YM2608)) {
                audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
                audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
                audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
                audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);
                audio.chipRegister.writeYm2608Clock((byte) 0, 8000000, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            }

            if (audio.useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.chipRegister.setYM2608Register(1, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
                audio.chipRegister.setYM2608Register(1, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
                audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2608Register(1, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
                audio.chipRegister.setYM2608Register(1, 0, 0x07, 0x38, Common.EnmModel.RealModel);
                audio.chipRegister.writeYm2608Clock((byte) 1, 8000000, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            }

            if (audio.useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 0, 4000000, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 1, 4000000, Common.EnmModel.RealModel);

            audio.driverVirtual.setYm2151Hosei(4000000);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);

            if (audio.useChip.contains(Common.EnmChip.YM2203))
                audio.chipRegister.setYM2203SSGVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2203))
                audio.chipRegister.setYM2203SSGVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);

            boolean retV = audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2151, Common.EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
            );
            boolean retR = true;
            if (audio.driverReal != null) {
                retR = audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2151, Common.EnmChip.YM2608}
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
            ex.printStackTrace();
            return false;
        }
    }
}
