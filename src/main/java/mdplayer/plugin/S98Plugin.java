package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import dotnet4j.io.Stream;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.s98.S98;
import mdplayer.format.FileFormat;
import mdsound.instrument.Ay8910Inst;
import mdsound.MDSound;
import mdsound.instrument.MameYm2612Inst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68SoundYm2151Inst;
import mdsound.instrument.Ym2203Inst;
import mdsound.instrument.Ym2413Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.Ym3526Inst;
import mdsound.instrument.Ym3812Inst;
import mdsound.instrument.YmF262Inst;
import vavi.util.Debug;


/**
 * S98Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class S98Plugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new S98();
        audio.driverVirtual.setting = setting;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new S98();
            audio.driverReal.setting = setting;
        }

        boolean r = s98Play();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean s98Play() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //stop();

            audio.chipRegister.resetChips();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            audio.vgmRealFadeoutVol = 0;
            audio.vgmRealFadeoutVolWait = 4;

            audio.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2203}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2203}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            List<S98.S98DevInfo> s98DInfo = ((S98) audio.driverVirtual).s98Info.DeviceInfos;

            Ay8910Inst ym2149 = null;
            Ym2203Inst ym2203 = null;
            Ym2612Inst ym2612 = null;
            Ym3438Inst ym3438 = null;
            MameYm2612Inst ym2612mame = null;
            Ym2608Inst ym2608 = null;
            Ym2151Inst ym2151 = null;
            MameYm2151Inst ym2151mame = null;
            X68SoundYm2151Inst ym2151_x68sound = null;
            Ym2413Inst ym2413 = null;
            Ym3526Inst ym3526 = null;
            Ym3812Inst ym3812 = null;
            YmF262Inst ymf262 = null;
            Ay8910Inst ay8910 = null;

            int YM2151ClockValue = 4000000;
            int YM2203ClockValue = 4000000;
            int YM2608ClockValue = 8000000;
            int YMF262ClockValue = 14318180;
            audio.useChip.clear();

            for (S98.S98DevInfo dInfo : s98DInfo) {
                switch (dInfo.deviceType) {
                case 1:
                    chip = new MDSound.Chip();
                    if (ym2149 == null) {
                        ym2149 = new Ay8910Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriAY10", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecAY10", 1);
                    }
                    chip.instrument = ym2149;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ay8910Inst.class);
                    chip.clock = dInfo.clock / 4;
                    audio.clockAY8910 = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.AY8910 : Common.EnmChip.S_AY8910);
                    break;
                case 2:
                    chip = new MDSound.Chip();
                    if (ym2203 == null) {
                        ym2203 = new Ym2203Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPN", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPN", 1);
                    }
                    chip.instrument = ym2203;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2203Inst.class);
                    chip.clock = dInfo.clock;
                    YM2203ClockValue = chip.clock;
                    chip.option = null;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM2203 : Common.EnmChip.S_YM2203);

                    break;
                case 3:
                    chip = new MDSound.Chip();
                    chip.option = null;
                    if (ym2612 == null) {
                        ym2612 = new Ym2612Inst();
                        ym3438 = new Ym3438Inst();
                        ym2612mame = new MameYm2612Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPN2", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPN2", 1);
                    }

                    if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[0]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[0])) {
                        chip.instrument = ym2612;
                        chip.option = new Object[] {
                                (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00)
                                        | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                        };
                    } else if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[1]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[1])) {
                        chip.instrument = ym3438;
                        switch (setting.getNukedOPN2().emuType) {
                        case 0:
                            ym3438.setChipType(Ym3438Const.Type.discrete);
                            break;
                        case 1:
                            ym3438.setChipType(Ym3438Const.Type.asic);
                            break;
                        case 2:
                            ym3438.setChipType(Ym3438Const.Type.ym2612);
                            break;
                        case 3:
                            ym3438.setChipType(Ym3438Const.Type.ym2612_u);
                            break;
                        case 4:
                            ym3438.setChipType(Ym3438Const.Type.asic_lp);
                            break;
                        }
                    } else if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[2]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[2])) {
                        chip.instrument = ym2612mame;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2612Inst.class);
                    chip.clock = dInfo.clock;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM2612 : Common.EnmChip.S_YM2612);

                    break;
                case 4:
                    chip = new MDSound.Chip();
                    if (ym2608 == null) {
                        ym2608 = new Ym2608Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPNA", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPNA", 1);
                    }
                    chip.instrument = ym2608;
                    chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
                    chip.clock = dInfo.clock;
                    YM2608ClockValue = chip.clock;
                    chip.setVolumes.put("FM", ym2608::setFMVolume);
                    chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                    chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                    chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                    Function<String, Stream> fn = Common::getOPNARyhthmStream;
                    chip.option = new Object[] {fn};
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM2608 : Common.EnmChip.S_YM2608);

                    break;
                case 5:
                    chip = new MDSound.Chip();
                    if (ym2151 == null && ym2151mame == null) {
                        chip.id = 0;
                        audio.chipLED.put("PriOPM", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPM", 1);
                    }

                    if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[0]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[0])) {
                        if (ym2151 == null) ym2151 = new Ym2151Inst();
                        chip.instrument = ym2151;
                    } else if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                        if (ym2151mame == null) ym2151mame = new MameYm2151Inst();
                        chip.instrument = ym2151mame;
                    } else if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                        if (ym2151_x68sound == null) ym2151_x68sound = new X68SoundYm2151Inst();
                        chip.instrument = ym2151_x68sound;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
                    chip.clock = dInfo.clock;
                    YM2151ClockValue = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    if (chip.instrument != null)
                        lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM2151 : Common.EnmChip.S_YM2151);

                    break;
                case 6:
                    chip = new MDSound.Chip();
                    if (ym2413 == null) {
                        ym2413 = new Ym2413Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPLL", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPLL", 1);
                    }
                    chip.instrument = ym2413;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2413Inst.class);
                    chip.clock = dInfo.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM2413 : Common.EnmChip.S_YM2413);

                    break;
                case 7:
                    chip = new MDSound.Chip();
                    if (ym3526 == null) {
                        ym3526 = new Ym3526Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPL", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPL", 1);
                    }
                    chip.instrument = ym3526;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym3526Inst.class);
                    chip.clock = dInfo.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM3526 : Common.EnmChip.S_YM3526);

                    break;
                case 8:
                    chip = new MDSound.Chip();
                    if (ym3812 == null) {
                        ym3812 = new Ym3812Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPL2", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPL2", 1);
                    }
                    chip.instrument = ym3812;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym3812Inst.class);
                    chip.clock = dInfo.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YM3812 : Common.EnmChip.S_YM3812);

                    break;
                case 9:
                    chip = new MDSound.Chip();
                    if (ymf262 == null) {
                        ymf262 = new YmF262Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriOPL3", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecOPL3", 1);
                    }
                    chip.instrument = ymf262;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", YmF262Inst.class);
                    chip.clock = dInfo.clock;
                    YMF262ClockValue = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.YMF262 : Common.EnmChip.S_YMF262);

                    break;
                case 15:
                    chip = new MDSound.Chip();
                    if (ay8910 == null) {
                        ay8910 = new Ay8910Inst();
                        chip.id = 0;
                        audio.chipLED.put("PriAY10", 1);
                    } else {
                        chip.id = 1;
                        audio.chipLED.put("SecAY10", 1);
                    }
                    chip.instrument = ay8910;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ay8910Inst.class);
                    chip.clock = dInfo.clock;
                    audio.clockAY8910 = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    audio.useChip.add(chip.id == 0 ? Common.EnmChip.AY8910 : Common.EnmChip.S_AY8910);

                    break;
                }
            }

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (audio.useChip.contains(Common.EnmChip.YM2203) || audio.useChip.contains(Common.EnmChip.S_YM2203)) {
                audio.setVolume("MAIN", Ym2203Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2203Inst.class));
                audio.setVolume("FM", Ym2203Inst.class, true, setting.getBalance().getVolume("FM", Ym2203Inst.class));
                audio.setVolume("PSG", Ym2203Inst.class, true, setting.getBalance().getVolume("PSG", Ym2203Inst.class));
            }

            if (audio.useChip.contains(Common.EnmChip.YM2612) || audio.useChip.contains(Common.EnmChip.S_YM2612))
                audio.setVolume("MAIN", Ym2612Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2612Inst.class));

            if (audio.useChip.contains(Common.EnmChip.YM2608) || audio.useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.setVolume("MAIN", Ym2608Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2608Inst.class));
                audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
                audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
                audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
                audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));
            }

            if (audio.useChip.contains(Common.EnmChip.YM2608)) {
                audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
                audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
                audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.YM2151) || audio.useChip.contains(Common.EnmChip.S_YM2151))
                audio.setVolume("MAIN", Ym2151Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2151Inst.class));
            if (audio.useChip.contains(Common.EnmChip.YM2413) || audio.useChip.contains(Common.EnmChip.S_YM2413))
                audio.setVolume("MAIN", Ym2413Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2413Inst.class));
            if (audio.useChip.contains(Common.EnmChip.YM3526) || audio.useChip.contains(Common.EnmChip.S_YM3526))
                audio.setVolume("MAIN", Ym3526Inst.class, true, setting.getBalance().getVolume("MAIN", Ym3526Inst.class));
            if (audio.useChip.contains(Common.EnmChip.AY8910) || audio.useChip.contains(Common.EnmChip.S_AY8910))
                audio.setVolume("MAIN", Ay8910Inst.class, true, setting.getBalance().getVolume("MAIN", Ay8910Inst.class));

            if (audio.useChip.contains(Common.EnmChip.AY8910))
                audio.chipRegister.writeAY8910Clock((byte) 0, audio.clockAY8910, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_AY8910))
                audio.chipRegister.writeAY8910Clock((byte) 1, audio.clockAY8910, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 0, YM2151ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 1, YM2151ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM2203))
                audio.chipRegister.writeYm2203Clock((byte) 0, YM2203ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2203))
                audio.chipRegister.writeYm2203Clock((byte) 1, YM2203ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM2608))
                audio.chipRegister.writeYm2608Clock((byte) 0, YM2608ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2608))
                audio.chipRegister.writeYm2608Clock((byte) 1, YM2608ClockValue, Common.EnmModel.RealModel);

            if (audio.useChip.contains(Common.EnmChip.YMF262)) {
                audio.chipRegister.setYMF262Register(0, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
                audio.chipRegister.writeYmF262Clock((byte) 0, YMF262ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.S_YMF262)) {
                audio.chipRegister.setYMF262Register(1, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
                audio.chipRegister.writeYmF262Clock((byte) 1, YMF262ClockValue, Common.EnmModel.RealModel);
            }

            audio.driverVirtual.setYm2151Hosei(YM2151ClockValue);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(YM2151ClockValue);

            if (audio.driverReal == null || ((S98) audio.driverReal).SSGVolumeFromTAG == -1) {
                if (audio.useChip.contains(Common.EnmChip.YM2203))
                    audio.chipRegister.setYM2203SSGVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.S_YM2203))
                    audio.chipRegister.setYM2203SSGVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.YM2608))
                    audio.chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.S_YM2608))
                    audio.chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            } else {
                if (audio.useChip.contains(Common.EnmChip.YM2203))
                    audio.chipRegister.setYM2203SSGVolume((byte) 0, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.S_YM2203))
                    audio.chipRegister.setYM2203SSGVolume((byte) 1, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.YM2608))
                    audio.chipRegister.setYM2608SSGVolume((byte) 0, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.S_YM2608))
                    audio.chipRegister.setYM2608SSGVolume((byte) 1, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
            }
            // play

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
