package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Ym3526Chip;
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.driver.s98.S98;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.Ay8910Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * S98Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class S98Plugin extends BasePlugin {

    private static final Logger logger = getLogger(S98Plugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new S98();
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new S98();
//        }

        boolean r = _play();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _play() {
        startTrdVgmReal();

        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ym2203Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ym2203Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        List<S98.S98DevInfo> s98DInfo = ((S98) audio.driverVirtual).s98Info.DeviceInfos;

        int YM2151ClockValue = 4000000;
        int YM2203ClockValue = 4000000;
        int YM2608ClockValue = 8000000;
        int YMF262ClockValue = 14318180;

        for (S98.S98DevInfo dInfo : s98DInfo) {
            switch (dInfo.deviceType) {
            case 1:
                MDSound.Chip chip = new MDSound.Chip();
                if (contains(Ay8910Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecAY10", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriAY10", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
                chip.clock = dInfo.clock / 4;
                chip.option = null;
//                audio.clockAY8910 = chip.clock;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ay8910Chip.class, chip);
                break;
            case 2:
                chip = new MDSound.Chip();
                if (contains(Ym2203Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPN", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPN", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym2203Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2203Chip.class);
                chip.clock = dInfo.clock;
                YM2203ClockValue = chip.clock;
                chip.option = null;
                put(Ym2203Chip.class, chip);

                break;
            case 3:
                chip = new MDSound.Chip();
                chip.option = null;
                if (contains(Ym2612Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPN2", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPN2", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym2612Chip.class).instrument(chip.id);
                if (chip.instrument instanceof Ym2612Inst) {
                    chip.option = new Object[] {
                            (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00)
                                    | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                    };
                } else if (chip.instrument instanceof Ym3438Inst ym3438) {
                    switch (setting.getNukedOPN2().emuType) {
                    case 0 -> ym3438.setChipType(Ym3438Const.Type.discrete);
                    case 1 -> ym3438.setChipType(Ym3438Const.Type.asic);
                    case 2 -> ym3438.setChipType(Ym3438Const.Type.ym2612);
                    case 3 -> ym3438.setChipType(Ym3438Const.Type.ym2612_u);
                    case 4 -> ym3438.setChipType(Ym3438Const.Type.asic_lp);
                    }
                }
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
                chip.clock = dInfo.clock;
                put(Ym2612Chip.class, chip);

                break;
            case 4:
                chip = new MDSound.Chip();
                if (contains(Ym2608Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPNA", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPNA", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(chip.id);
                chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
                chip.clock = dInfo.clock;
                YM2608ClockValue = chip.clock;
                if (chip.instrument instanceof Ym2608Inst ym2608) {
                    chip.setVolumes.put("FM", ym2608::setFMVolume);
                    chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                    chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                    chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                }
                Function<String, Stream> fn = Common::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                put(Ym2608Chip.class, chip);

                break;
            case 5:
                chip = new MDSound.Chip();
                if (contains(Ym2151Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPM", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPM", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
                chip.clock = dInfo.clock;
                YM2151ClockValue = chip.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym2151Chip.class, chip);

                break;
            case 6:
                chip = new MDSound.Chip();
                if (contains(Ym2413Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPLL", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPLL", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym2413Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
                chip.clock = dInfo.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym2413Chip.class, chip);

                break;
            case 7:
                chip = new MDSound.Chip();
                if (contains(Ym3526Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPL", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPL", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym3526Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym3526Chip.class);
                chip.clock = dInfo.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym3526Chip.class, chip);

                break;
            case 8:
                chip = new MDSound.Chip();
                if (contains(Ym3812Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPL2", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPL2", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ym3812Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym3812Chip.class);
                chip.clock = dInfo.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym3812Chip.class, chip);

                break;
            case 9:
                chip = new MDSound.Chip();
                if (contains(YmF262Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecOPL3", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriOPL3", 1);
                }
                chip.instrument = audio.chipRegister.chip(YmF262Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class);
                chip.clock = dInfo.clock;
                YMF262ClockValue = chip.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(YmF262Chip.class, chip);

                break;
            case 15:
                chip = new MDSound.Chip();
                if (contains(Ay8910Chip.class, 0)) {
                    chip.id = 1;
                    audio.chipLED.put("SecAY10", 1);
                } else {
                    chip.id = 0;
                    audio.chipLED.put("PriAY10", 1);
                }
                chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
                chip.clock = dInfo.clock;
//                audio.clockAY8910 = chip.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ay8910Chip.class, chip);

                break;
            }
        }

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (contains(Ym2203Chip.class, 0) || contains(Ym2203Chip.class, 1)) {
            audio.setVolume(MAIN_TAG, Ym2203Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2203Chip.class));
            audio.setVolume("FM", Ym2203Chip.class, true, setting.getBalance().getVolume("FM", Ym2203Chip.class));
            audio.setVolume("PSG", Ym2203Chip.class, true, setting.getBalance().getVolume("PSG", Ym2203Chip.class));
        }

        if (contains(Ym2612Chip.class, 0) || contains(Ym2612Chip.class, 1))
            audio.setVolume(MAIN_TAG, Ym2612Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class));

        if (contains(Ym2608Chip.class, 0) || contains(Ym2608Chip.class, 1)) {
            audio.setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
            audio.setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
            audio.setVolume("PSG", Ym2608Chip.class, true, setting.getBalance().getVolume("PSG", Ym2608Chip.class));
            audio.setVolume("Rhythm", Ym2608Chip.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Chip.class));
            audio.setVolume("Adpcm", Ym2608Chip.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Chip.class));
        }

        if (contains(Ym2608Chip.class, 0)) {
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        }
        if (contains(Ym2608Chip.class, 0)) {
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        }
        if (contains(Ym2151Chip.class, 0) || contains(Ym2151Chip.class, 1))
            audio.setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));
        if (contains(Ym2413Chip.class, 0) || contains(Ym2413Chip.class, 1))
            audio.setVolume(MAIN_TAG, Ym2413Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class));
        if (contains(Ym3526Chip.class, 0) || contains(Ym3526Chip.class, 1))
            audio.setVolume(MAIN_TAG, Ym3526Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym3526Chip.class));
        if (contains(Ay8910Chip.class, 0) || contains(Ay8910Chip.class, 1))
            audio.setVolume(MAIN_TAG, Ay8910Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class));

        if (contains(Ay8910Chip.class, 0))
            audio.chipRegister.chip(Ay8910Chip.class).writeClock((byte) 0, audio.mds.getChipInfo(Ay8910Inst.class).clock, Common.EnmModel.RealModel);
        if (contains(Ay8910Chip.class, 1))
            audio.chipRegister.chip(Ay8910Chip.class).writeClock((byte) 1, audio.mds.getChipInfo(Ay8910Inst.class).clock, Common.EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, YM2151ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 1))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, YM2151ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 0))
            audio.chipRegister.chip(Ym2203Chip.class).writeClock((byte) 0, YM2203ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 1))
            audio.chipRegister.chip(Ym2203Chip.class).writeClock((byte) 1, YM2203ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2608Chip.class, 0))
            audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, YM2608ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2608Chip.class, 1))
            audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, YM2608ClockValue, Common.EnmModel.RealModel);

        if (contains(YmF262Chip.class, 0)) {
            audio.chipRegister.chip(YmF262Chip.class).setRegister(0, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
            audio.chipRegister.chip(YmF262Chip.class).writeClock((byte) 0, YMF262ClockValue, Common.EnmModel.RealModel);
        }
        if (contains(YmF262Chip.class, 1)) {
            audio.chipRegister.chip(YmF262Chip.class).setRegister(1, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
            audio.chipRegister.chip(YmF262Chip.class).writeClock((byte) 1, YMF262ClockValue, Common.EnmModel.RealModel);
        }

        audio.driverVirtual.setYm2151Hosei(YM2151ClockValue);
        if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(YM2151ClockValue);

        if (audio.driverReal == null || ((S98) audio.driverReal).SSGVolumeFromTAG == -1) {
            if (contains(Ym2203Chip.class, 0))
                audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
            if (contains(Ym2203Chip.class, 1))
                audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 0))
                audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 1))
                audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        } else {
            if (contains(Ym2203Chip.class, 0))
                audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 0, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
            if (contains(Ym2203Chip.class, 1))
                audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 1, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 0))
                audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 1))
                audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, ((S98) audio.driverReal).SSGVolumeFromTAG, Common.EnmModel.RealModel);
        }

        return true;
    }
}
