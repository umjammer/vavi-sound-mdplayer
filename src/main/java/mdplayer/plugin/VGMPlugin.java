package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.C140Chip;
import mdplayer.chips.SegaPcmChip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym3526Chip;
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.driver.Vgm;
import mdplayer.format.FileFormat;
import mdplayer.instruments.VRC7;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.C352;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.*;

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
        boolean r = vgmPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean vgmPlay() {
        try {
            if (vgmBuf == null || setting == null) return false;

            //stop();

//            audio.chipRegister.resetChips();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipRegister.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (!audio.driverVirtual.init(vgmBuf
                    , this
                    , Common.EnmModel.VirtualModel
                    , new EnmChip[] {EnmChip.YM2203} // usechip.toArray(new MDSound.Chip[0])
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            if (audio.driverReal != null && !audio.driverReal.init(vgmBuf
                    , this
                    , Common.EnmModel.RealModel
                    , new EnmChip[] {EnmChip.YM2203} // usechip.toArray(new MDSound.Chip[0])
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            audio.chipRegister.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (((Vgm) audio.driverVirtual).sn76489ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).sn76489DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.option = null;
                    chip.instrument = Instrument.getInstrument(EnmChip.SN76489.getInstClass(setting.getSN76489Type()[i].getEnabledId()));
                    if (chip.instrument instanceof Sn76496Inst) {
                        chip.option = ((Vgm) audio.driverVirtual).sn76489Option;
                    }
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).sn76489ClockValue
                            | (((Vgm) audio.driverVirtual).sn76489NGPFlag ? 0x8000_0000 : 0);
//                    audio.clockSN76489 = chip.clock & 0x7fff_ffff;
                    if (i == 0) audio.chipRegister.chipLED.put("PriDCSG", 1);
                    else audio.chipRegister.chipLED.put("SecDCSG", 1);

                    hiyorimiDeviceFlag |= (setting.getSN76489Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                    audio.chipRegister.chip(Sn76489Chip.class).ngpFlag = ((Vgm) audio.driverVirtual).sn76489NGPFlag;

                    if (chip.instrument != null) lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.SN76489 : EnmChip.S_SN76489);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2612ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2612DualChipFlag ? 2 : 1); i++) {
                    //mdsound.ym2612 ym2612 = new mdsound.ym2612();
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.option = null;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM2612.getInstClass(setting.getYM2612Type()[i].getEnabledId()));
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
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ym2612ClockValue;
//                    audio.clockYM2612 = ((Vgm) audio.driverVirtual).ym2612ClockValue;

                    hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                    hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]
                            && setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) ? 0x2 : 0x0;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPN2", 1);
                    else audio.chipRegister.chipLED.put("SecOPN2", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2612 : EnmChip.S_YM2612);
                }
            }

            if (((Vgm) audio.driverVirtual).rf5C68ClockValue != 0) {

                for (int i = 0; i < (((Vgm) audio.driverVirtual).rf5C68DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.RF5C68.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).rf5C68ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriRF5C68", 1);
                    else audio.chipRegister.chipLED.put("SecRF5C68", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.RF5C68 : EnmChip.S_RF5C68);
                }
            }

            if (((Vgm) audio.driverVirtual).rf5C164ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).rf5C164DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.RF5C164.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).rf5C164ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriRF5C", 1);
                    else audio.chipRegister.chipLED.put("SecRF5C", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.RF5C164 : EnmChip.S_RF5C164);
                }
            }

            if (((Vgm) audio.driverVirtual).pwmClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = Instrument.getInstrument(EnmChip.PWM.getInstClass(0));
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                chip.clock = ((Vgm) audio.driverVirtual).pwmClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                audio.chipRegister.chipLED.put("PriPWM", 1);

                lstChips.add(chip);
                useChip.add(EnmChip.PWM);
            }

            if (((Vgm) audio.driverVirtual).c140ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).c140DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.C140.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).c140ClockValue;
                    chip.option = new Object[] {((Vgm) audio.driverVirtual).C140Type};

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriC140", 1);
                    else audio.chipRegister.chipLED.put("SecC140", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.C140 : EnmChip.S_C140);
                }
            }

            if (((Vgm) audio.driverVirtual).multiPCMClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).multiPCMDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.MultiPCM.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).multiPCMClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriMPCM", 1);
                    else audio.chipRegister.chipLED.put("SecMPCM", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.MultiPCM : EnmChip.S_MultiPCM);
                }
            }

            if (((Vgm) audio.driverVirtual).okiM6258ClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = Instrument.getInstrument(EnmChip.OKIM6258.getInstClass(0));
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                chip.clock = ((Vgm) audio.driverVirtual).okiM6258ClockValue;
                chip.option = new Object[] {((Vgm) audio.driverVirtual).okiM6258Type};
                //chips.option = new Object[1] { 6 };
                if (chip.instrument instanceof OkiM6258Inst okim6258)
                    okim6258.setCallback(0, this::changeChipSampleRate, chip);

                hiyorimiDeviceFlag |= 0x2;

                audio.chipRegister.chipLED.put("PriOKI5", 1);

                lstChips.add(chip);
                useChip.add(EnmChip.OKIM6258);
            }

            if (((Vgm) audio.driverVirtual).okiM6295ClockValue != 0) {
                for (byte i = 0; i < (((Vgm) audio.driverVirtual).okiM6295DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.OKIM6295.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, OkiM6295Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).okiM6295ClockValue;
                    chip.option = null;
                    if (chip.instrument instanceof OkiM6295Inst okim6295)
                        okim6295.setCallback(i, this::changeChipSampleRate, chip);

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOKI9", 1);
                    else audio.chipRegister.chipLED.put("SecOKI9", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.OKIM6295 : EnmChip.S_OKIM6295);
                }
            }

            if (((Vgm) audio.driverVirtual).segaPCMClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = Instrument.getInstrument(EnmChip.SEGAPCM.getInstClass(0));
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, SegaPcmInst.class);
                chip.clock = ((Vgm) audio.driverVirtual).segaPCMClockValue;
                chip.option = new Object[] {((Vgm) audio.driverVirtual).segaPCMInterface};

                hiyorimiDeviceFlag |= 0x2;

                audio.chipRegister.chipLED.put("PriSPCM", 1);

                lstChips.add(chip);
                useChip.add(EnmChip.SEGAPCM);
            }

            if (((Vgm) audio.driverVirtual).yn2608ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2608DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM2608.getInstClass(setting.getYM2608Type()[i].getEnabledId()));
                    if (chip.instrument instanceof Ym2608Inst ym2608) {
                        chip.setVolumes.put("FM", ym2608::setFMVolume);
                        chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                        chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                        chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                    }

                    chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).yn2608ClockValue;
                    Function<String, Stream> fn = Common::getOPNARyhthmStream;
                    chip.option = new Object[] {fn};
                    hiyorimiDeviceFlag |= 0x2;
//                    audio.clockYM2608 = ((Vgm) audio.driverVirtual).yn2608ClockValue;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPNA", 1);
                    else audio.chipRegister.chipLED.put("SecOPNA", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2608 : EnmChip.S_YM2608);
                }
            }

            if (((Vgm) audio.driverVirtual).yn2151ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2151DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM2151.getInstClass(setting.getYM2151Type()[i].getEnabledId()));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).yn2151ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPM", 1);
                    else audio.chipRegister.chipLED.put("SecOPM", 1);

                    if (chip.instrument != null)
                        lstChips.add(chip);

                    useChip.add(i == 0 ? EnmChip.YM2151 : EnmChip.S_YM2151);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2203ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2203DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM2203.getInstClass(setting.getYM2203Type()[i].getEnabledId()));
                    if (chip.instrument instanceof Ym2203Inst ym2203) {
                        chip.setVolumes.put("FM", ym2203::setFMVolume);
                        chip.setVolumes.put("PSG", ym2203::setPSGVolume);
                    } else if (chip.instrument instanceof YmFmYm2203Inst ym2203) {
                        chip.setVolumes.put("FM", ym2203::setFMVolume);
                        chip.setVolumes.put("PSG", ym2203::setPSGVolume);
                    }
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ym2203ClockValue;
                    chip.option = null;

//                    audio.clockYM2203 = ((Vgm) audio.driverVirtual).ym2203ClockValue;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPN", 1);
                    else audio.chipRegister.chipLED.put("SecOPN", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2203 : EnmChip.S_YM2203);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2610ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2610DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM2610.getInstClass(setting.getYM2610Type()[i].getEnabledId()));
                    if (chip.instrument instanceof Ym2610Inst ym2610) {
                        chip.setVolumes.put("FM", ym2610::setFMVolume);
                        chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                        chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                        chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ym2610ClockValue & 0x7fff_ffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPNB", 1);
                    else audio.chipRegister.chipLED.put("SecOPNB", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2610 : EnmChip.S_YM2610);
                }
            }

            if (((Vgm) audio.driverVirtual).ym3812ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym3812DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM3812.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ym3812ClockValue & 0x7fff_ffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPL2", 1);
                    else audio.chipRegister.chipLED.put("SecOPL2", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM3812 : EnmChip.S_YM3812);
                }
            }

            if (((Vgm) audio.driverVirtual).ymF262ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF262DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YMF262.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ymF262ClockValue & 0x7fff_ffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPL3", 1);
                    else audio.chipRegister.chipLED.put("SecOPL3", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMF262 : EnmChip.S_YMF262);
                }
            }

            if (((Vgm) audio.driverVirtual).ymF271ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF271DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YMF271.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ymF271ClockValue & 0x7fff_ffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPX", 1);
                    else audio.chipRegister.chipLED.put("SecOPX", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMF271 : EnmChip.S_YMF271);
                }
            }

            if (((Vgm) audio.driverVirtual).ymF278BClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF278BDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YMF278B.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ymF278BClockValue & 0x7fff_ffff;
                    chip.option = new Object[] {Common.getApplicationFolder()};

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPL4", 1);
                    else audio.chipRegister.chipLED.put("SecOPL4", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMF278B : EnmChip.S_YMF278B);
                }
            }

            if (((Vgm) audio.driverVirtual).ymZ280BClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymZ280BDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YMZ280B.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ymZ280BClockValue & 0x7fff_ffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriYMZ", 1);
                    else audio.chipRegister.chipLED.put("SecYMZ", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMZ280B : EnmChip.S_YMZ280B);
                }
            }

            if (((Vgm) audio.driverVirtual).ay8910ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ay8910DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.AY8910.getInstClass(setting.getAY8910Type()[i].getEnabledId()));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).ay8910ClockValue & 0x7fff_ffff) / 2;
//                    audio.clockAY8910 = chip.clock;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriAY10", 1);
                    else audio.chipRegister.chipLED.put("SecAY10", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.AY8910 : EnmChip.S_AY8910);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2413ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2413DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM2413.getInstClass(!((Vgm) audio.driverVirtual).ym2413VRC7Flag ? 0 : 1));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).ym2413ClockValue & 0x7fff_ffff);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriOPLL", 1);
                    else audio.chipRegister.chipLED.put("SecOPLL", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2413 : EnmChip.S_YM2413);
                }
            }

            if (((Vgm) audio.driverVirtual).huC6280ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).huC6280DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.HuC6280.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).huC6280ClockValue & 0x7fff_ffff);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriHuC", 1);
                    else audio.chipRegister.chipLED.put("SecHuC", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.HuC6280 : EnmChip.S_HuC6280);
                }
            }

            if (((Vgm) audio.driverVirtual).qSoundClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chip.instrument = Instrument.getInstrument(EnmChip.QSound.getInstClass(setting.getQSoundType()[0].getEnabledId()));
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                chip.clock = (((Vgm) audio.driverVirtual).qSoundClockValue); // & 0x7fff_ffff);
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                audio.chipRegister.chipLED.put("PriQsnd", 1);

                lstChips.add(chip);
                useChip.add(EnmChip.QSound);
            }

            if (((Vgm) audio.driverVirtual).saa1099ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).saA1099DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.SAA1099.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).saa1099ClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriSAA", 1);
                    else audio.chipRegister.chipLED.put("SecSAA", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.SAA1099 : EnmChip.S_SAA1099);
                }
            }

            if (((Vgm) audio.driverVirtual).wSwanClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).wSwanDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.WSwan.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).wSwanClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriWSW", 1);
                    else audio.chipRegister.chipLED.put("SecWSW", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.WSwan : EnmChip.S_WSwan);
                }
            }

            if (((Vgm) audio.driverVirtual).pokeyClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).pokeyDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.POKEY.getInstClass(0));
                    chip.samplingRate = (((Vgm) audio.driverVirtual).pokeyClockValue & 0x3fff_ffff); // (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).pokeyClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriPOK", 1);
                    else audio.chipRegister.chipLED.put("SecPOK", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.POKEY : EnmChip.S_POKEY);
                }
            }

            if (((Vgm) audio.driverVirtual).x1_010ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).x1_010DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.X1_010.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).x1_010ClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriX1010", 1);
                    else audio.chipRegister.chipLED.put("SecX1010", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.X1_010 : EnmChip.S_X1_010);
                }
            }

            if (((Vgm) audio.driverVirtual).c352ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).c352DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.C352.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).c352ClockValue & 0x7fff_ffff);
                    if (chip.instrument instanceof C352Inst c352)
                        chip.setVolumes.put("Rear", c352::setRearMute);
                    chip.option = new Object[] {(((Vgm) audio.driverVirtual).c352ClockDivider)};
                    int divider = (((Vgm) audio.driverVirtual).c352ClockDivider) != 0 ? (((Vgm) audio.driverVirtual).c352ClockDivider) : 288;
//                    audio.clockC352 = chip.clock / divider;
                    C352.setOptions((((Vgm) audio.driverVirtual).c352ClockValue >> 31));
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriC352", 1);
                    else audio.chipRegister.chipLED.put("SecC352", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.C352 : EnmChip.S_C352);
                }
            }

            if (((Vgm) audio.driverVirtual).ga20ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ga20DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.GA20.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = (((Vgm) audio.driverVirtual).ga20ClockValue & 0x7fff_ffff);
                    chip.option = null;
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipRegister.chipLED.put("PriGA20", 1);
                    else audio.chipRegister.chipLED.put("SecGA20", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.GA20 : EnmChip.S_GA20);
                }
            }

            if (((Vgm) audio.driverVirtual).k053260ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).k053260DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.K053260.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).k053260ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriK053260", 1);
                    else audio.chipRegister.chipLED.put("SecK053260", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.K053260 : EnmChip.S_K053260);
                }
            }

            if (((Vgm) audio.driverVirtual).k054539ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).k054539DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.K054539.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).k054539ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriK054539", 1);
                    else audio.chipRegister.chipLED.put("SecK054539", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.K054539 : EnmChip.S_K054539);
                }
            }

            if (((Vgm) audio.driverVirtual).k051649ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).k051649DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.K051649.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).k051649ClockValue;
//                    audio.clockK051649 = chip.clock;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriK051649", 1);
                    else audio.chipRegister.chipLED.put("SecK051649", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.K051649 : EnmChip.S_K051649);
                }
            }

            if (((Vgm) audio.driverVirtual).ym3526ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym3526DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.YM3526.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).ym3526ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriOPL", 1);
                    else audio.chipRegister.chipLED.put("SecOPL", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM3526 : EnmChip.S_YM3526);
                }
            }

            if (((Vgm) audio.driverVirtual).y8950ClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).y8950DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.Y8950.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).y8950ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriY8950", 1);
                    else audio.chipRegister.chipLED.put("SecY8950", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.Y8950 : EnmChip.S_Y8950);
                }
            }

            if (((Vgm) audio.driverVirtual).dmgClockValue != 0) {
                for (int i = 0; i < (((Vgm) audio.driverVirtual).dmgDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.DMG.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).dmgClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriDMG", 1);
                    else audio.chipRegister.chipLED.put("SecDMG", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.DMG : EnmChip.S_DMG);
                }
            }

            if (((Vgm) audio.driverVirtual).nesClockValue != 0) {

                for (int i = 0; i < (((Vgm) audio.driverVirtual).nesDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.NES.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriNES", 1);
                    else audio.chipRegister.chipLED.put("SecNES", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.NES : EnmChip.S_NES);

                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.NES.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriDMC", 1);
                    else audio.chipRegister.chipLED.put("SecDMC", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.DMC : EnmChip.S_DMC);


                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = Instrument.getInstrument(EnmChip.NES.getInstClass(0));
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, chip.instrument.getClass());
                    chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipRegister.chipLED.put("PriFDS", 1);
                    else audio.chipRegister.chipLED.put("SecFDS", 1);

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.FDS : EnmChip.S_FDS);


                    hiyorimiDeviceFlag |= 0x2;
                }
            }

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips.toArray(MDSound.Chip[]::new));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));


            if (useChip.contains(EnmChip.YM2203) || useChip.contains(EnmChip.S_YM2203)) {
                audio.chipRegister.chip(Ym2203Chip.class).write(0, 0x7, 0x3f, Common.EnmModel.RealModel); // Output off
                audio.chipRegister.chip(Ym2203Chip.class).write(1, 0x7, 0x3f, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2203Chip.class).write(0, 0x8, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2203Chip.class).write(1, 0x8, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2203Chip.class).write(0, 0x9, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2203Chip.class).write(1, 0x9, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2203Chip.class).write(0, 0xa, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2203Chip.class).write(1, 0xa, 0x0, Common.EnmModel.RealModel);
                audio.setVolume("FM", Ym2203Inst.class, true, setting.getBalance().getVolume("FM", Ym2203Inst.class));
                audio.setVolume("PSG", Ym2203Inst.class, true, setting.getBalance().getVolume("PSG", Ym2203Inst.class));
            }

            if (useChip.contains(EnmChip.YM2608) || useChip.contains(EnmChip.S_YM2608)) {
                audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
                audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
                audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
                audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));
            }

            if (useChip.contains(EnmChip.YM2610) || useChip.contains(EnmChip.S_YM2610)) {

                audio.setVolume("FM", Ym2610Inst.class, true, setting.getBalance().getVolume("FM", Ym2610Inst.class));
                audio.setVolume("PSG", Ym2610Inst.class, true, setting.getBalance().getVolume("PSG", Ym2610Inst.class));
                audio.setVolume("AdpcmA", Ym2610Inst.class, true, setting.getBalance().getVolume("AdpcmA", Ym2610Inst.class));
                audio.setVolume("AdpcmB", Ym2610Inst.class, true, setting.getBalance().getVolume("AdpcmB", Ym2610Inst.class));
            }

            if (useChip.contains(EnmChip.AY8910))
                audio.chipRegister.chip(Ay8910Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ay8910ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_AY8910))
                audio.chipRegister.chip(Ay8910Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ay8910ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).yn2151ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).yn2151ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2203))
                audio.chipRegister.chip(Ym2203Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym2203ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2203))
                audio.chipRegister.chip(Ym2203Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym2203ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2608))
                audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).yn2608ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2608))
                audio.chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).yn2608ClockValue, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM3526)) {
                audio.chipRegister.chip(Ym3526Chip.class).write(0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
                audio.chipRegister.chip(Ym3526Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym3526ClockValue, Common.EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YM3526)) {
                audio.chipRegister.chip(Ym3526Chip.class).write(1, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
                audio.chipRegister.chip(Ym3526Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym3526ClockValue, Common.EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.YM3812)) {
                audio.chipRegister.chip(Ym3812Chip.class).write(0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
                audio.chipRegister.chip(Ym3812Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ym3812ClockValue, Common.EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YM3812)) {
                audio.chipRegister.chip(Ym3812Chip.class).write(1, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
                audio.chipRegister.chip(Ym3812Chip.class).writeClock((byte) 1, ((Vgm) audio.driverVirtual).ym3812ClockValue, Common.EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.YMF262)) {
                audio.chipRegister.chip(YmF262Chip.class).setRegister(0, 0, 0xbd, 0, Common.EnmModel.RealModel); // Rhythm mode off
                audio.chipRegister.chip(YmF262Chip.class).setRegister(0, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
                audio.chipRegister.chip(YmF262Chip.class).writeClock((byte) 0, ((Vgm) audio.driverVirtual).ymF262ClockValue, Common.EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YMF262)) {
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
            if (useChip.contains(EnmChip.YM2610)) {
                // control2 Presets register pan to center
                audio.chipRegister.chip(Ym2610Chip.class).write(0, 0, 0x11, 0xc0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2610Chip.class).write(0, 0, 0x11, 0xc0, Common.EnmModel.VirtualModel);
            }
            if (useChip.contains(EnmChip.S_YM2610)) {
                // control2 Presets register pan to center
                audio.chipRegister.chip(Ym2610Chip.class).write(1, 0, 0x11, 0xc0, Common.EnmModel.RealModel);
                audio.chipRegister.chip(Ym2610Chip.class).write(1, 0, 0x11, 0xc0, Common.EnmModel.VirtualModel);
            }
            if (useChip.contains(EnmChip.C140))
                audio.chipRegister.chip(C140Chip.class).writeType((byte) 0, ((Vgm) audio.driverVirtual).C140Type, Common.EnmModel.RealModel);
            if (useChip.contains(EnmChip.SEGAPCM))
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
                if (useChip.contains(EnmChip.YM2203))
                    audio.chipRegister.chip(Ym2203Chip.class).setVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2203))
                    audio.chipRegister.chip(Ym2203Chip.class).setVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), Common.EnmModel.RealModel);
                if (useChip.contains(EnmChip.YM2608))
                    audio.chipRegister.chip(Ym2608Chip.class).setVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2608))
                    audio.chipRegister.chip(Ym2608Chip.class).setVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            } else {
                if (useChip.contains(EnmChip.YM2203))
                    audio.chipRegister.chip(Ym2203Chip.class).setVolume((byte) 0, SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2203))
                    audio.chipRegister.chip(Ym2203Chip.class).setVolume((byte) 1, SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (useChip.contains(EnmChip.YM2608))
                    audio.chipRegister.chip(Ym2608Chip.class).setVolume((byte) 0, SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2608))
                    audio.chipRegister.chip(Ym2608Chip.class).setVolume((byte) 1, SSGVolumeFromTAG, Common.EnmModel.RealModel);
            }

            audio.driverVirtual.setYm2151Hosei(((Vgm) audio.driverVirtual).yn2151ClockValue);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(((Vgm) audio.driverReal).yn2151ClockValue);

            //frmMain.ForceChannelMask(EnmChip.Ym2612Inst, 0, 0, true);

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            //Stopped = false;

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
