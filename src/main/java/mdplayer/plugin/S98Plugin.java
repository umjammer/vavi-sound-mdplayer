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
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.Ay8910Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.MameYm2612Inst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.Ym2203Inst;
import mdsound.instrument.Ym2413Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;
import mdsound.instrument.Ym3526Inst;
import mdsound.instrument.Ym3812Inst;
import mdsound.instrument.YmF262Inst;

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

        boolean r = _olay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _olay() {
        audio.vgmFadeout = false;
        audio.vgmFadeoutCounter = 1.0;
        audio.vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        vgmRealFadeoutVol = 0;
        vgmRealFadeoutVolWait = 4;

        audio.chipRegister.clearFadeoutVolume();

        audio.chipRegister.reset();

        startTrdVgmReal();

        hiyorimiNecessary = setting.getHiyorimiMode();

        audio.chipLED.clear();

        audio.masterVolume = setting.getBalance().getMasterVolume();

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

        Ay8910Inst ym2149 = null;
        Ym2203Inst ym2203 = null;
        Ym2612Inst ym2612 = null;
        Ym3438Inst ym3438 = null;
        MameYm2612Inst ym2612mame = null;
        Ym2608Inst ym2608 = null;
        Ym2151Inst ym2151 = null;
        MameYm2151Inst ym2151mame = null;
        X68kYm2151Inst ym2151_x68sound = null;
        Ym2413Inst ym2413 = null;
        Ym3526Inst ym3526 = null;
        Ym3812Inst ym3812 = null;
        YmF262Inst ymf262 = null;
        Ay8910Inst ay8910 = null;

        int YM2151ClockValue = 4000000;
        int YM2203ClockValue = 4000000;
        int YM2608ClockValue = 8000000;
        int YMF262ClockValue = 14318180;

        for (S98.S98DevInfo dInfo : s98DInfo) {
            switch (dInfo.deviceType) {
            case 1:
                MDSound.Chip chip = new MDSound.Chip();
                if (ym2149 == null) {
                    ym2149 = Instrument.getInstrument(Ay8910Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriAY10", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecAY10", 1);
                }
                chip.instrument = ym2149;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
                chip.clock = dInfo.clock / 4;
//                    audio.clockAY8910 = chip.clock;
                chip.option = null;
//                    hiyorimiDeviceFlag |= 0x2;
                put(Ay8910Chip.class, chip);
                break;
            case 2:
                chip = new MDSound.Chip();
                if (ym2203 == null) {
                    ym2203 = Instrument.getInstrument(Ym2203Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriOPN", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecOPN", 1);
                }
                chip.instrument = ym2203;
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
                if (ym2612 == null) {
                    ym2612 = Instrument.getInstrument(Ym2612Inst.class);
                    ym3438 = Instrument.getInstrument(Ym3438Inst.class);
                    ym2612mame = Instrument.getInstrument(MameYm2612Inst.class);
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
                    case 0 -> ym3438.setChipType(Ym3438Const.Type.discrete);
                    case 1 -> ym3438.setChipType(Ym3438Const.Type.asic);
                    case 2 -> ym3438.setChipType(Ym3438Const.Type.ym2612);
                    case 3 -> ym3438.setChipType(Ym3438Const.Type.ym2612_u);
                    case 4 -> ym3438.setChipType(Ym3438Const.Type.asic_lp);
                    }
                } else if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[2]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[2])) {
                    chip.instrument = ym2612mame;
                }

                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
                chip.clock = dInfo.clock;
                put(Ym2612Chip.class, chip);

                break;
            case 4:
                chip = new MDSound.Chip();
                if (ym2608 == null) {
                    ym2608 = Instrument.getInstrument(Ym2608Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriOPNA", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecOPNA", 1);
                }
                chip.instrument = ym2608;
                chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
                chip.clock = dInfo.clock;
                YM2608ClockValue = chip.clock;
                chip.setVolumes.put("FM", ym2608::setFMVolume);
                chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                Function<String, Stream> fn = Common::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                put(Ym2608Chip.class, chip);

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
                    if (ym2151 == null) ym2151 = Instrument.getInstrument(Ym2151Inst.class);
                    chip.instrument = ym2151;
                } else if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                    if (ym2151mame == null) ym2151mame = Instrument.getInstrument(MameYm2151Inst.class);
                    chip.instrument = ym2151mame;
                } else if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                    if (ym2151_x68sound == null) ym2151_x68sound = Instrument.getInstrument(X68kYm2151Inst.class);
                    chip.instrument = ym2151_x68sound;
                }

                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
                chip.clock = dInfo.clock;
                YM2151ClockValue = chip.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                if (chip.instrument != null)
                    put(Ym2151Chip.class, chip);

                break;
            case 6:
                chip = new MDSound.Chip();
                if (ym2413 == null) {
                    ym2413 = Instrument.getInstrument(Ym2413Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriOPLL", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecOPLL", 1);
                }
                chip.instrument = ym2413;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
                chip.clock = dInfo.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym2413Chip.class, chip);

                break;
            case 7:
                chip = new MDSound.Chip();
                if (ym3526 == null) {
                    ym3526 = Instrument.getInstrument(Ym3526Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriOPL", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecOPL", 1);
                }
                chip.instrument = ym3526;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym3526Chip.class);
                chip.clock = dInfo.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym3526Chip.class, chip);

                break;
            case 8:
                chip = new MDSound.Chip();
                if (ym3812 == null) {
                    ym3812 = Instrument.getInstrument(Ym3812Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriOPL2", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecOPL2", 1);
                }
                chip.instrument = ym3812;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym3812Chip.class);
                chip.clock = dInfo.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym3812Chip.class, chip);

                break;
            case 9:
                chip = new MDSound.Chip();
                if (ymf262 == null) {
                    ymf262 = Instrument.getInstrument(YmF262Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriOPL3", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecOPL3", 1);
                }
                chip.instrument = ymf262;
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
                if (ay8910 == null) {
                    ay8910 = Instrument.getInstrument(Ay8910Inst.class);
                    chip.id = 0;
                    audio.chipLED.put("PriAY10", 1);
                } else {
                    chip.id = 1;
                    audio.chipLED.put("SecAY10", 1);
                }
                chip.instrument = ay8910;
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
        // play

        audio.paused = false;
        oneTimeReset = false;

        sleep(500);

        return true;
    }
}
