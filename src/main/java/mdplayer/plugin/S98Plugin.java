package mdplayer.plugin;

import java.lang.System.Logger;
import java.util.List;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
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
import mdplayer.driver.s98.S98Driver;
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
public class S98Plugin extends BasePlugin<S98Driver> {

    private static final Logger logger = getLogger(S98Plugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new S98Driver();

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new S98Driver();
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        List<S98.S98DevInfo> s98DInfo = driverVirtual.getDeviceInfos();

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
                    chipLED.put("SecAY10", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriAY10", 1);
                }
                chip.instrument = chipRegister.chip(Ay8910Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
                chip.clock = dInfo.clock / 4;
                chip.option = null;
                chipRegister.chip(Ay8910Chip.class).clock = chip.clock;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ay8910Chip.class, chip);
                break;
            case 2:
                chip = new MDSound.Chip();
                if (contains(Ym2203Chip.class, 0)) {
                    chip.id = 1;
                    chipLED.put("SecOPN", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPN", 1);
                }
                chip.instrument = chipRegister.chip(Ym2203Chip.class).instrument(chip.id);
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
                    chipLED.put("SecOPN2", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPN2", 1);
                }
                chip.instrument = chipRegister.chip(Ym2612Chip.class).instrument(chip.id);
                if (chip.instrument instanceof Ym2612Inst) {
                    chip.option = new Object[] {
                            (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00) |
                                    (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
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
                    chipLED.put("SecOPNA", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPNA", 1);
                }
                chip.instrument = chipRegister.chip(Ym2608Chip.class).instrument(chip.id);
                chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
                chip.clock = dInfo.clock;
                YM2608ClockValue = chip.clock;
                if (chip.instrument instanceof Ym2608Inst ym2608) {
                    chip.setVolumes.put("FM", ym2608::setVolume);
                    chip.setVolumes.put("SSG", ym2608::setVolume);
                    chip.setVolumes.put("RHYTHM", ym2608::setVolume);
                    chip.setVolumes.put("ADPCM", ym2608::setVolume);
                }
                Function<String, Stream> fn = Ym2608Chip::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                put(Ym2608Chip.class, chip);

                break;
            case 5:
                chip = new MDSound.Chip();
                if (contains(Ym2151Chip.class, 0)) {
                    chip.id = 1;
                    chipLED.put("SecOPM", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPM", 1);
                }
                chip.instrument = chipRegister.chip(Ym2151Chip.class).instrument(chip.id);
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
                chip.clock = dInfo.clock;
                chip.samplingRate = chip.clock / 64;
                YM2151ClockValue = chip.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ym2151Chip.class, chip);

                break;
            case 6:
                chip = new MDSound.Chip();
                if (contains(Ym2413Chip.class, 0)) {
                    chip.id = 1;
                    chipLED.put("SecOPLL", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPLL", 1);
                }
                chip.instrument = chipRegister.chip(Ym2413Chip.class).instrument(chip.id);
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
                    chipLED.put("SecOPL", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPL", 1);
                }
                chip.instrument = chipRegister.chip(Ym3526Chip.class).instrument(chip.id);
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
                    chipLED.put("SecOPL2", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPL2", 1);
                }
                chip.instrument = chipRegister.chip(Ym3812Chip.class).instrument(chip.id);
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
                    chipLED.put("SecOPL3", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriOPL3", 1);
                }
                chip.instrument = chipRegister.chip(YmF262Chip.class).instrument(chip.id);
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
                    chipLED.put("SecAY10", 1);
                } else {
                    chip.id = 0;
                    chipLED.put("PriAY10", 1);
                }
                chip.instrument = chipRegister.chip(Ay8910Chip.class).instrument(chip.id);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
                chip.clock = dInfo.clock;
                chipRegister.chip(Ay8910Chip.class).clock = chip.clock;
                chip.option = null;
//                hiyorimiDeviceFlag |= 0x2;
                put(Ay8910Chip.class, chip);

                break;
            }
        }

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        if (contains(Ym2203Chip.class, 0) || contains(Ym2203Chip.class, 1)) {
            setVolume(MAIN_TAG, Ym2203Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2203Chip.class));
            setVolume("FM", Ym2203Chip.class, true, setting.getBalance().getVolume("FM", Ym2203Chip.class));
            setVolume("PSG", Ym2203Chip.class, true, setting.getBalance().getVolume("PSG", Ym2203Chip.class));
        }

        if (contains(Ym2612Chip.class, 0) || contains(Ym2612Chip.class, 1))
            setVolume(MAIN_TAG, Ym2612Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class));

        if (contains(Ym2608Chip.class, 0) || contains(Ym2608Chip.class, 1)) {
            setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
            setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
            setVolume("SSG", Ym2608Chip.class, true, setting.getBalance().getVolume("SSG", Ym2608Chip.class));
            setVolume("RHYTHM", Ym2608Chip.class, true, setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
            setVolume("ADPCM", Ym2608Chip.class, true, setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));
        }

        if (contains(Ym2608Chip.class, 0)) {
            chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
            chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.RealModel);
        }
        if (contains(Ym2608Chip.class, 0)) {
            chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
            chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.RealModel);
        }
        if (contains(Ym2151Chip.class, 0) || contains(Ym2151Chip.class, 1))
            setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));
        if (contains(Ym2413Chip.class, 0) || contains(Ym2413Chip.class, 1))
            setVolume(MAIN_TAG, Ym2413Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class));
        if (contains(Ym3526Chip.class, 0) || contains(Ym3526Chip.class, 1))
            setVolume(MAIN_TAG, Ym3526Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym3526Chip.class));
        if (contains(Ay8910Chip.class, 0) || contains(Ay8910Chip.class, 1))
            setVolume(MAIN_TAG, Ay8910Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class));

        if (contains(Ay8910Chip.class, 0))
            chipRegister.chip(Ay8910Chip.class).writeClock((byte) 0, mds.getChipInfo(Ay8910Inst.class).clock, EnmModel.RealModel);
        if (contains(Ay8910Chip.class, 1))
            chipRegister.chip(Ay8910Chip.class).writeClock((byte) 1, mds.getChipInfo(Ay8910Inst.class).clock, EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 0))
            chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, YM2151ClockValue, EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 1))
            chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, YM2151ClockValue, EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 0))
            chipRegister.chip(Ym2203Chip.class).writeClock((byte) 0, YM2203ClockValue, EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 1))
            chipRegister.chip(Ym2203Chip.class).writeClock((byte) 1, YM2203ClockValue, EnmModel.RealModel);
        if (contains(Ym2608Chip.class, 0))
            chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, YM2608ClockValue, EnmModel.RealModel);
        if (contains(Ym2608Chip.class, 1))
            chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, YM2608ClockValue, EnmModel.RealModel);

        if (contains(YmF262Chip.class, 0)) {
            chipRegister.chip(YmF262Chip.class).write(0, 1, 5, 1, EnmModel.RealModel); // opl3mode
            chipRegister.chip(YmF262Chip.class).writeClock((byte) 0, YMF262ClockValue, EnmModel.RealModel);
        }
        if (contains(YmF262Chip.class, 1)) {
            chipRegister.chip(YmF262Chip.class).write(1, 1, 5, 1, EnmModel.RealModel); // opl3mode
            chipRegister.chip(YmF262Chip.class).writeClock((byte) 1, YMF262ClockValue, EnmModel.RealModel);
        }

        chipRegister.chip(Ym2151Chip.class).setYm2151Hosei(EnmModel.VirtualModel, YM2151ClockValue);
        if (driverReal != null) chipRegister.chip(Ym2151Chip.class).setYm2151Hosei(EnmModel.RealModel, YM2151ClockValue);

        if (driverReal == null || driverReal.getSSGVolumeFromTAG() == -1) {
            if (contains(Ym2203Chip.class, 0))
                chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
            if (contains(Ym2203Chip.class, 1))
                chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 0))
                chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 1))
                chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
        } else {
            if (contains(Ym2203Chip.class, 0))
                chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 0, driverReal.getSSGVolumeFromTAG(), EnmModel.RealModel);
            if (contains(Ym2203Chip.class, 1))
                chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 1, driverReal.getSSGVolumeFromTAG(), EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 0))
                chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, driverReal.getSSGVolumeFromTAG(), EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 1))
                chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, driverReal.getSSGVolumeFromTAG(), EnmModel.RealModel);
        }
    }
}
