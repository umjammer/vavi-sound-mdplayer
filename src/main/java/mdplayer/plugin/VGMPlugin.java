package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Consumer;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.*;
import mdplayer.chips.NesChip.DmcChip;
import mdplayer.chips.NesChip.FdsChip;
import mdplayer.driver.Vgm;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.chips.C352;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.C352Inst;
import mdsound.instrument.OkiM6258Inst;
import mdsound.instrument.OkiM6295Inst;
import mdsound.instrument.Sn76496Inst;
import mdsound.instrument.Ym2203Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2610Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;
import mdsound.instrument.YmFmYm2203Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * VGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class VGMPlugin extends BasePlugin {

    private static final Logger logger = getLogger(VGMPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Vgm();
        ((Vgm) audio.driverVirtual).dacControl.chipRegister = audio.chipRegister;
        ((Vgm) audio.driverVirtual).dacControl.model = Common.EnmModel.VirtualModel;

        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new Vgm();
//            ((Vgm) audio.driverReal).dacControl.chipRegister = audio.chipRegister;
//            ((Vgm) audio.driverReal).dacControl.model = Common.EnmModel.RealModel;
//        }
        prepare();
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

        if (audio.driverReal != null && !audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                new Class[] {Ym2203Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;

        int hiyorimiDeviceFlag = 0;

        if (((Vgm) audio.driverVirtual).sn76489ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).sn76489DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.option = null;
                chip.instrument = audio.chipRegister.chip(Sn76489Chip.class).instrument(i);
                if (chip.instrument instanceof Sn76496Inst) {
                    chip.option = ((Vgm) audio.driverVirtual).sn76489Option;
                }
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).sn76489ClockValue
                        | (((Vgm) audio.driverVirtual).sn76489NGPFlag ? 0x8000_0000 : 0);

//                    audio.clockSN76489 = chip.clock & 0x7fff_ffff;

                if (i == 0) audio.chipLED.put("PriDCSG", 1);
                else audio.chipLED.put("SecDCSG", 1);

                hiyorimiDeviceFlag |= (setting.getSN76489Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                audio.chipRegister.chip(Sn76489Chip.class).ngpFlag = ((Vgm) audio.driverVirtual).sn76489NGPFlag;

                put(Sn76489Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym2612ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2612DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.option = null;
                chip.instrument = audio.chipRegister.chip(Ym2612Chip.class).instrument(i);
                if (chip.instrument instanceof Ym2612Inst ||
                        setting.getYM2612Type()[i].getRealChipInfo()[0].getOnlyPCMEmulation() ||
                        setting.getYM2612Type()[i].getUseReal()[0]
                ) {
                    chip.option = new Object[] {
                            (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00) | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
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
                chip.clock = ((Vgm) audio.driverVirtual).ym2612ClockValue;

//                audio.clockYM2612 = ((Vgm) audio.driverVirtual).ym2612ClockValue;

                hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]
                        && setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) ? 0x2 : 0x0;

                if (i == 0) audio.chipLED.put("PriOPN2", 1);
                else audio.chipLED.put("SecOPN2", 1);

                put(Ym2612Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).rf5C68ClockValue != 0) {

            for (int i = 0; i < (((Vgm) audio.driverVirtual).rf5C68DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Rf5C68Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Rf5C68Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).rf5C68ClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriRF5C68", 1);
                else audio.chipLED.put("SecRF5C68", 1);

                put(Rf5C68Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).rf5C164ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).rf5C164DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Rf5C164Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Rf5C164Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).rf5C164ClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriRF5C", 1);
                else audio.chipLED.put("SecRF5C", 1);

                put(Rf5C164Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).pwmClockValue != 0) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(PwmChip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, PwmChip.class);
            chip.clock = ((Vgm) audio.driverVirtual).pwmClockValue;
            chip.option = null;

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriPWM", 1);

            put(PwmChip.class, chip);
        }

        if (((Vgm) audio.driverVirtual).c140ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).c140DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(C140Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, C140Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).c140ClockValue;
                chip.option = new Object[] {((Vgm) audio.driverVirtual).C140Type};

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriC140", 1);
                else audio.chipLED.put("SecC140", 1);

                put(C140Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).multiPCMClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).multiPCMDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(MultiPcmChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, MultiPcmChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).multiPCMClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriMPCM", 1);
                else audio.chipLED.put("SecMPCM", 1);

                put(MultiPcmChip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).uPD7759ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).uPD7759DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Upd7759Chip.class).instrument(i);
                chip.samplingRate = (int) setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Upd7759Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).uPD7759ClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriuPD7759", 1);
                else audio.chipLED.put("SecuPD7759", 1);

                put(Upd7759Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).okiM6258ClockValue != 0) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(OkiM6258Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, OkiM6258Chip.class);
            chip.clock = ((Vgm) audio.driverVirtual).okiM6258ClockValue;
            chip.option = new Object[] {((Vgm) audio.driverVirtual).okiM6258Type};
//            chip.option = new Object[1] { 6 };
            if (chip.instrument instanceof OkiM6258Inst okim6258)
                okim6258.setCallback(0, this::changeChipSampleRate, chip);

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriOKI5", 1);

            put(OkiM6258Chip.class, chip);
        }

        if (((Vgm) audio.driverVirtual).okiM6295ClockValue != 0) {
            for (byte i = 0; i < (((Vgm) audio.driverVirtual).okiM6295DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(OkiM6295Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, OkiM6295Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).okiM6295ClockValue;
                chip.option = null;
                if (chip.instrument instanceof OkiM6295Inst okim6295)
                    okim6295.setCallback(i, this::changeChipSampleRate, chip);

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOKI9", 1);
                else audio.chipLED.put("SecOKI9", 1);

                put(OkiM6295Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).segaPCMClockValue != 0) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(SegaPcmChip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, SegaPcmChip.class);
            chip.clock = ((Vgm) audio.driverVirtual).segaPCMClockValue;
            chip.option = new Object[] {((Vgm) audio.driverVirtual).segaPCMInterface};

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriSPCM", 1);

            put(SegaPcmChip.class, chip);
        }

        if (((Vgm) audio.driverVirtual).ym2608ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2608DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(i);
                if (chip.instrument instanceof Ym2608Inst ym2608) {
                    chip.setVolumes.put("FM", ym2608::setFMVolume);
                    chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                    chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                    chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                }

                chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ym2608ClockValue;
                Function<String, Stream> fn = Common::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                hiyorimiDeviceFlag |= 0x2;

//                audio.clockYM2608 = ((Vgm) audio.driverVirtual).ym2608ClockValue;

                if (i == 0) audio.chipLED.put("PriOPNA", 1);
                else audio.chipLED.put("SecOPNA", 1);

                put(Ym2608Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym2151ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2151DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ym2151ClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPM", 1);
                else audio.chipLED.put("SecOPM", 1);

                put(Ym2151Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym2203ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2203DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym2203Chip.class).instrument(i);
                if (chip.instrument instanceof Ym2203Inst ym2203) {
                    chip.setVolumes.put("FM", ym2203::setFMVolume);
                    chip.setVolumes.put("PSG", ym2203::setPSGVolume);
                } else if (chip.instrument instanceof YmFmYm2203Inst ym2203) {
                    chip.setVolumes.put("FM", ym2203::setFMVolume);
                    chip.setVolumes.put("PSG", ym2203::setPSGVolume);
                }
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2203Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ym2203ClockValue;
                chip.option = null;

//                audio.clockYM2203 = ((Vgm) audio.driverVirtual).ym2203ClockValue;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPN", 1);
                else audio.chipLED.put("SecOPN", 1);

                put(Ym2203Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym2610ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2610DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym2610Chip.class).instrument(i);
                if (chip.instrument instanceof Ym2610Inst ym2610) {
                    chip.setVolumes.put("FM", ym2610::setFMVolume);
                    chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                    chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                    chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                }

                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ym2610ClockValue & 0x7fff_ffff;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPNB", 1);
                else audio.chipLED.put("SecOPNB", 1);

                put(Ym2610Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym3812ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym3812DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym3812Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym3812Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ym3812ClockValue & 0x7fff_ffff;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPL2", 1);
                else audio.chipLED.put("SecOPL2", 1);

                put(Ym3812Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ymF262ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF262DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(YmF262Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ymF262ClockValue & 0x7fff_ffff;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPL3", 1);
                else audio.chipLED.put("SecOPL3", 1);

                put(YmF262Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ymF271ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF271DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(YmF271Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF271Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ymF271ClockValue & 0x7fff_ffff;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPX", 1);
                else audio.chipLED.put("SecOPX", 1);

                put(YmF271Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ymF278BClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF278BDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(YmF278BChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ymF278BClockValue & 0x7fff_ffff;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPL4", 1);
                else audio.chipLED.put("SecOPL4", 1);

                put(YmF278BChip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ymZ280BClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ymZ280BDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(YmZ280BChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmZ280BChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ymZ280BClockValue & 0x7fff_ffff;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriYMZ", 1);
                else audio.chipLED.put("SecYMZ", 1);

                put(YmZ280BChip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ay8910ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ay8910DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).ay8910ClockValue & 0x7fff_ffff) / 2;
//                    audio.clockAY8910 = chip.clock;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriAY10", 1);
                else audio.chipLED.put("SecAY10", 1);

                put(Ay8910Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym2413ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2413DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym2413Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).ym2413ClockValue & 0x7fff_ffff);
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriOPLL", 1);
                else audio.chipLED.put("SecOPLL", 1);

                put(Ym2413Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).huC6280ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).huC6280DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(HuC6280Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, HuC6280Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).huC6280ClockValue & 0x7fff_ffff);
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriHuC", 1);
                else audio.chipLED.put("SecHuC", 1);

                put(HuC6280Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).qSoundClockValue != 0) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = audio.chipRegister.chip(QSoundChip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, QSoundChip.class);
            chip.clock = (((Vgm) audio.driverVirtual).qSoundClockValue); // & 0x7fff_ffff);
            chip.option = null;

            hiyorimiDeviceFlag |= 0x2;

            audio.chipLED.put("PriQsnd", 1);

            put(QSoundChip.class, chip);
        }

        if (((Vgm) audio.driverVirtual).saa1099ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).saA1099DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Saa1099Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Saa1099Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).saa1099ClockValue & 0x3fff_ffff);
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriSAA", 1);
                else audio.chipLED.put("SecSAA", 1);

                put(Saa1099Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).wSwanClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).wSwanDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(WSwanChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, WSwanChip.class);
                chip.clock = (((Vgm) audio.driverVirtual).wSwanClockValue & 0x3fff_ffff);
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriWSW", 1);
                else audio.chipLED.put("SecWSW", 1);

                put(WSwanChip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).es5503ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).es5503DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Es5503Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Es5503Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).es5503ClockValue & 0x3fff_ffff);
                Consumer<Integer> fn = sr -> chip.samplingRate = sr;
                chip.option = new Object[] {((Vgm) audio.driverVirtual).es5503Ch, fn};
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriES53", 1);
                else audio.chipLED.put("SecES53", 1);

                put(Es5503Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).pokeyClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).pokeyDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(PokeyChip.class).instrument(i);
                chip.samplingRate = (((Vgm) audio.driverVirtual).pokeyClockValue & 0x3fff_ffff); // (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, PokeyChip.class);
                chip.clock = (((Vgm) audio.driverVirtual).pokeyClockValue & 0x3fff_ffff);
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriPOK", 1);
                else audio.chipLED.put("SecPOK", 1);

                put(PokeyChip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).x1_010ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).x1_010DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(X1_010Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, X1_010Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).x1_010ClockValue & 0x3fff_ffff);
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriX1010", 1);
                else audio.chipLED.put("SecX1010", 1);

                put(X1_010Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).c352ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).c352DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(C352Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, C352Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).c352ClockValue & 0x7fff_ffff);
                if (chip.instrument instanceof C352Inst c352)
                    chip.setVolumes.put("Rear", c352::setRearMute);
                chip.option = new Object[] {(((Vgm) audio.driverVirtual).c352ClockDivider)};
                int divider = (((Vgm) audio.driverVirtual).c352ClockDivider) != 0 ? (((Vgm) audio.driverVirtual).c352ClockDivider) : 288;
//                    audio.clockC352 = chip.clock / divider;
                C352.setOptions((((Vgm) audio.driverVirtual).c352ClockValue >> 31));
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriC352", 1);
                else audio.chipLED.put("SecC352", 1);

                put(C352Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ga20ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ga20DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ga20Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ga20Chip.class);
                chip.clock = (((Vgm) audio.driverVirtual).ga20ClockValue & 0x7fff_ffff);
                chip.option = null;
                hiyorimiDeviceFlag |= 0x2;

                if (i == 0) audio.chipLED.put("PriGA20", 1);
                else audio.chipLED.put("SecGA20", 1);

                put(Ga20Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).k053260ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).k053260DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(K053260Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, K053260Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).k053260ClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriK053260", 1);
                else audio.chipLED.put("SecK053260", 1);

                hiyorimiDeviceFlag |= 0x2;

                put(K053260Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).k054539ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).k054539DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(K054539Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, K054539Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).k054539ClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriK054539", 1);
                else audio.chipLED.put("SecK054539", 1);

                hiyorimiDeviceFlag |= 0x2;

                put(K054539Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).k051649ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).k051649DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(K051649Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).k051649ClockValue;
//                    audio.clockK051649 = chip.clock;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriK051649", 1);
                else audio.chipLED.put("SecK051649", 1);

                hiyorimiDeviceFlag |= 0x2;

                put(K051649Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).ym3526ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).ym3526DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Ym3526Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym3526Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).ym3526ClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriOPL", 1);
                else audio.chipLED.put("SecOPL", 1);

                hiyorimiDeviceFlag |= 0x2;

                put(Ym3526Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).y8950ClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).y8950DualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(Y8950Chip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Y8950Chip.class);
                chip.clock = ((Vgm) audio.driverVirtual).y8950ClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriY8950", 1);
                else audio.chipLED.put("SecY8950", 1);

                hiyorimiDeviceFlag |= 0x2;

                put(Y8950Chip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).dmgClockValue != 0) {
            for (int i = 0; i < (((Vgm) audio.driverVirtual).dmgDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(DmgChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, DmgChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).dmgClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriDMG", 1);
                else audio.chipLED.put("SecDMG", 1);

                hiyorimiDeviceFlag |= 0x2;

                put(DmgChip.class, chip);
            }
        }

        if (((Vgm) audio.driverVirtual).nesClockValue != 0) {

            for (int i = 0; i < (((Vgm) audio.driverVirtual).nesDualChipFlag ? 2 : 1); i++) {
                MDSound.Chip chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(NesChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriNES", 1);
                else audio.chipLED.put("SecNES", 1);

                put(NesChip.class, chip);

                chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(NesChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, DmcChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriDMC", 1);
                else audio.chipLED.put("SecDMC", 1);

                put(DmcChip.class, chip);

                chip = new MDSound.Chip();
                chip.id = i;
                chip.instrument = audio.chipRegister.chip(NesChip.class).instrument(i);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, FdsChip.class);
                chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                chip.option = null;
                if (i == 0) audio.chipLED.put("PriFDS", 1);
                else audio.chipLED.put("SecFDS", 1);

                put(FdsChip.class, chip);

                hiyorimiDeviceFlag |= 0x2;
            }
        }

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (contains(Ym2203Chip.class, 0) || contains(Ym2203Chip.class, 1)) {
            audio.chipRegister.chip(Ym2203Chip.class).write(0, 0x7, 0x3f, Common.EnmModel.RealModel); // Output off
            audio.chipRegister.chip(Ym2203Chip.class).write(1, 0x7, 0x3f, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2203Chip.class).write(0, 0x8, 0x0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2203Chip.class).write(1, 0x8, 0x0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2203Chip.class).write(0, 0x9, 0x0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2203Chip.class).write(1, 0x9, 0x0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2203Chip.class).write(0, 0xa, 0x0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2203Chip.class).write(1, 0xa, 0x0, Common.EnmModel.RealModel);
            audio.setVolume("FM", Ym2203Chip.class, true, setting.getBalance().getVolume("FM", Ym2203Chip.class));
            audio.setVolume("PSG", Ym2203Chip.class, true, setting.getBalance().getVolume("PSG", Ym2203Chip.class));
        }

        if (contains(Ym2608Chip.class, 0) || contains(Ym2608Chip.class, 1)) {
            audio.setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
            audio.setVolume("PSG", Ym2608Chip.class, true, setting.getBalance().getVolume("PSG", Ym2608Chip.class));
            audio.setVolume("Rhythm", Ym2608Chip.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Chip.class));
            audio.setVolume("Adpcm", Ym2608Chip.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Chip.class));

        }

        if (contains(Ym2610Chip.class, 0) || contains(Ym2610Chip.class, 1)) {
            audio.setVolume("FM", Ym2610Chip.class, true, setting.getBalance().getVolume("FM", Ym2610Chip.class));
            audio.setVolume("PSG", Ym2610Chip.class, true, setting.getBalance().getVolume("PSG", Ym2610Chip.class));
            audio.setVolume("AdpcmA", Ym2610Chip.class, true, setting.getBalance().getVolume("AdpcmA", Ym2610Chip.class));
            audio.setVolume("AdpcmB", Ym2610Chip.class, true, setting.getBalance().getVolume("AdpcmB", Ym2610Chip.class));
        }

        if (contains(Ay8910Chip.class, 0))
            audio.chipRegister.chip(Ay8910Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ay8910ClockValue, Common.EnmModel.RealModel);
        if (contains(Ay8910Chip.class, 1))
            audio.chipRegister.chip(Ay8910Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ay8910ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym2151ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2151Chip.class, 1))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym2151ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 0))
            audio.chipRegister.chip(Ym2203Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym2203ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2203Chip.class, 1))
            audio.chipRegister.chip(Ym2203Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym2203ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2608Chip.class, 0))
            audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym2608ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym2608Chip.class, 1))
            audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym2608ClockValue, Common.EnmModel.RealModel);
        if (contains(Ym3526Chip.class, 0)) {
            audio.chipRegister.chip(Ym3526Chip.class).write(0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
            audio.chipRegister.chip(Ym3526Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym3526ClockValue, Common.EnmModel.RealModel);
        }
        if (contains(Ym3526Chip.class, 1)) {
            audio.chipRegister.chip(Ym3526Chip.class).write(1, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
            audio.chipRegister.chip(Ym3526Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym3526ClockValue, Common.EnmModel.RealModel);
        }
        if (contains(Ym3812Chip.class, 0)) {
            audio.chipRegister.chip(Ym3812Chip.class).write(0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
            audio.chipRegister.chip(Ym3812Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym3812ClockValue, Common.EnmModel.RealModel);
        }
        if (contains(Ym3812Chip.class, 1)) {
            audio.chipRegister.chip(Ym3812Chip.class).write(1, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
            audio.chipRegister.chip(Ym3812Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym3812ClockValue, Common.EnmModel.RealModel);
        }
        if (contains(YmF262Chip.class, 0)) {
            audio.chipRegister.chip(YmF262Chip.class).setRegister(0, 0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
            audio.chipRegister.chip(YmF262Chip.class).setRegister(0, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
            audio.chipRegister.chip(YmF262Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ymF262ClockValue, Common.EnmModel.RealModel);
        }
        if (contains(YmF262Chip.class, 1)) {
            audio.chipRegister.chip(YmF262Chip.class).setRegister(1, 0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
            audio.chipRegister.chip(YmF262Chip.class).setRegister(1, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
            audio.chipRegister.chip(YmF262Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ymF262ClockValue, Common.EnmModel.RealModel);
        }
        if (audio.chipRegister.chip(Sn76489Chip.class).ngpFlag) {
            audio.chipRegister.chip(Sn76489Chip.class).write(0, 0xe5, Common.EnmModel.RealModel); // white noise mode
            audio.chipRegister.chip(Sn76489Chip.class).write(1, 0xe5, Common.EnmModel.RealModel); // white noise mode
            audio.chipRegister.chip(Sn76489Chip.class).write(0, 0xe5, Common.EnmModel.VirtualModel); // white noise mode
            audio.chipRegister.chip(Sn76489Chip.class).write(1, 0xe5, Common.EnmModel.VirtualModel); // white noise mode
        }
        if (contains(Ym2610Chip.class, 0)) {
            // control2 Presets register pan to center
            audio.chipRegister.chip(Ym2610Chip.class).write(0, 0, 0x11, 0xc0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2610Chip.class).write(0, 0, 0x11, 0xc0, Common.EnmModel.VirtualModel);
        }
        if (contains(Ym2610Chip.class, 1)) {
            // control2 Presets register pan to center
            audio.chipRegister.chip(Ym2610Chip.class).write(1, 0, 0x11, 0xc0, Common.EnmModel.RealModel);
            audio.chipRegister.chip(Ym2610Chip.class).write(1, 0, 0x11, 0xc0, Common.EnmModel.VirtualModel);
        }
        if (contains(C140Chip.class, 0))
            audio.chipRegister.chip(C140Chip.class).writeType((byte) 0, ((Vgm) audio.driverVirtual).C140Type, Common.EnmModel.RealModel);
        if (contains(SegaPcmChip.class, 0))
            audio.chipRegister.chip(SegaPcmChip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).segaPCMClockValue, Common.EnmModel.RealModel);

        int SSGVolumeFromTAG = -1;
        if (audio.driverReal != null) {
            if (audio.driverReal.gd3.systemNameJ.indexOf("9801") > 0) SSGVolumeFromTAG = 31;
            if (audio.driverReal.gd3.systemNameJ.indexOf("8801") > 0) SSGVolumeFromTAG = 63;
            if (audio.driverReal.gd3.systemNameJ.indexOf("pc-88") > 0) SSGVolumeFromTAG = 63;
            if (audio.driverReal.gd3.systemNameJ.indexOf("PC88") > 0) SSGVolumeFromTAG = 63;
            if (audio.driverReal.gd3.systemNameJ.indexOf("pc-98") > 0) SSGVolumeFromTAG = 31;
            if (audio.driverReal.gd3.systemNameJ.indexOf("PC98") > 0) SSGVolumeFromTAG = 31;
            if (audio.driverReal.gd3.systemName.indexOf("9801") > 0) SSGVolumeFromTAG = 31;
            if (audio.driverReal.gd3.systemName.indexOf("8801") > 0) SSGVolumeFromTAG = 63;
            if (audio.driverReal.gd3.systemName.indexOf("pc-88") > 0) SSGVolumeFromTAG = 63;
            if (audio.driverReal.gd3.systemName.indexOf("PC88") > 0) SSGVolumeFromTAG = 63;
            if (audio.driverReal.gd3.systemName.indexOf("pc-98") > 0) SSGVolumeFromTAG = 31;
            if (audio.driverReal.gd3.systemName.indexOf("PC98") > 0) SSGVolumeFromTAG = 31;
        }

        if (SSGVolumeFromTAG == -1) {
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
                audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 0, SSGVolumeFromTAG, Common.EnmModel.RealModel);
            if (contains(Ym2203Chip.class, 1))
                audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume((byte) 1, SSGVolumeFromTAG, Common.EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 0))
                audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, SSGVolumeFromTAG, Common.EnmModel.RealModel);
            if (contains(Ym2608Chip.class, 1))
                audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, SSGVolumeFromTAG, Common.EnmModel.RealModel);
        }

        audio.driverVirtual.setYm2151Hosei(((Vgm) audio.driverVirtual).ym2151ClockValue);
        if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(((Vgm) audio.driverReal).ym2151ClockValue);

        //frmMain.ForceChannelMask(EnmChip.Ym2612, 0, 0, true);

        return true;
    }
}
