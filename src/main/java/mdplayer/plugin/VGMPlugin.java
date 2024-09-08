package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import dotnet4j.io.Stream;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.VRC7;
import mdplayer.driver.Vgm;
import mdplayer.format.FileFormat;
import mdsound.instrument.*;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.Ym3438Const;
import vavi.util.Debug;


/**
 * VGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class VGMPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Vgm();
        audio.driverVirtual.setting = setting;
        ((Vgm) audio.driverVirtual).dacControl.chipRegister = audio.chipRegister;
        ((Vgm) audio.driverVirtual).dacControl.model = Common.EnmModel.VirtualModel;

        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new Vgm();
//            audio.driverReal.setting = setting;
//            ((Vgm) audio.driverReal).dacControl.chipRegister = audio.chipRegister;
//            ((Vgm) audio.driverReal).dacControl.model = Common.EnmModel.RealModel;
//        }
        boolean r = vgmPlay();
        if (!r) {
 Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean vgmPlay() {
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

            audio.useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (!audio.driverVirtual.init(vgmBuf
                    , audio.chipRegister
                    , Common.EnmModel.VirtualModel
                    , new Common.EnmChip[] {Common.EnmChip.YM2203} // usechip.toArray(new MDSound.Chip[0])
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            if (audio.driverReal != null && !audio.driverReal.init(vgmBuf
                    , audio.chipRegister
                    , Common.EnmModel.RealModel
                    , new Common.EnmChip[] {Common.EnmChip.YM2203} // usechip.toArray(new MDSound.Chip[0])
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (((Vgm) audio.driverVirtual).sn76489ClockValue != 0) {
                Sn76489Inst sn76489 = null;
                Sn76496Inst sn76496 = null;

                for (int i = 0; i < (((Vgm) audio.driverVirtual).sn76489DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.option = null;

                    if ((i == 0 && setting.getSN76489Type()[0].getUseEmu()[0])
                            || (i == 1 && setting.getSN76489Type()[1].getUseEmu()[0])) {
                        if (sn76489 == null) sn76489 = new Sn76489Inst();
                        chip.instrument = sn76489;
                    } else if ((i == 0 && setting.getSN76489Type()[0].getUseEmu()[1])
                            || (i == 1 && setting.getSN76489Type()[1].getUseEmu()[1])) {
                        if (sn76496 == null) sn76496 = new Sn76496Inst();
                        chip.instrument = sn76496;
                        chip.option = ((Vgm) audio.driverVirtual).sn76489Option;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Sn76489Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).sn76489ClockValue
                            | (((Vgm) audio.driverVirtual).sn76489NGPFlag ? 0x8000_0000 : 0);
                    audio.clockSN76489 = chip.clock & 0x7fff_ffff;
                    if (i == 0) audio.chipLED.put("PriDCSG", 1);
                    else audio.chipLED.put("SecDCSG", 1);

                    hiyorimiDeviceFlag |= (setting.getSN76489Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                    audio.sn76489NGPFlag = ((Vgm) audio.driverVirtual).sn76489NGPFlag;

                    if (chip.instrument != null) lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.SN76489 : Common.EnmChip.S_SN76489);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2612ClockValue != 0) {
                Ym2612Inst ym2612 = null;
                Ym3438Inst ym3438 = null;
                MameYm2612Inst ym2612mame = null;

                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2612DualChipFlag ? 2 : 1); i++) {
                    //mdsound.ym2612 ym2612 = new mdsound.ym2612();
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.option = null;

                    if ((i == 0 && ((setting.getYM2612Type()[0].getUseEmu()[0] ||
                            setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) ||
                            setting.getYM2612Type()[0].getUseReal()[0])
                    ) ||
                            (i == 1 && (setting.getYM2612Type()[1].getUseEmu()[0] ||
                                    setting.getYM2612Type()[1].getRealChipInfo()[0].getOnlyPCMEmulation()) ||
                                    setting.getYM2612Type()[1].getUseReal()[0])
                    ) {
                        if (ym2612 == null) ym2612 = new Ym2612Inst();
                        chip.instrument = ym2612;
                        chip.option = new Object[] {
                                (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00) | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                        };
                    } else if ((i == 0 && setting.getYM2612Type()[0].getUseEmu()[1]) || (i == 1 && setting.getYM2612Type()[1].getUseEmu()[1])) {
                        if (ym3438 == null) ym3438 = new Ym3438Inst();
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
                    } else if ((i == 0 && setting.getYM2612Type()[0].getUseEmu()[2]) || (i == 1 && setting.getYM2612Type()[0].getUseEmu()[2])) {
                        if (ym2612mame == null) ym2612mame = new MameYm2612Inst();
                        chip.instrument = ym2612mame;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2612Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ym2612ClockValue;
                    audio.clockYM2612 = ((Vgm) audio.driverVirtual).ym2612ClockValue;

                    hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                    hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]
                            && setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) ? 0x2 : 0x0;

                    if (i == 0) audio.chipLED.put("PriOPN2", 1);
                    else audio.chipLED.put("SecOPN2", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM2612 : Common.EnmChip.S_YM2612);
                }
            }

            if (((Vgm) audio.driverVirtual).rf5C68ClockValue != 0) {
                Rf5c68Inst rf5c68 = new Rf5c68Inst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).rf5C68DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = rf5c68;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Rf5c68Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).rf5C68ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriRF5C68", 1);
                    else audio.chipLED.put("SecRF5C68", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.RF5C68 : Common.EnmChip.S_RF5C68);
                }
            }

            if (((Vgm) audio.driverVirtual).rf5C164ClockValue != 0) {
                ScdPcmInst rf5c164 = new ScdPcmInst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).rf5C164DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = rf5c164;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", ScdPcmInst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).rf5C164ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriRF5C", 1);
                    else audio.chipLED.put("SecRF5C", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.RF5C164 : Common.EnmChip.S_RF5C164);
                }
            }

            if (((Vgm) audio.driverVirtual).pwmClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                PwmInst pwm = new PwmInst();
                chip.instrument = pwm;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", PwmInst.class);
                chip.clock = ((Vgm) audio.driverVirtual).pwmClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                audio.chipLED.put("PriPWM", 1);

                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.PWM);
            }

            if (((Vgm) audio.driverVirtual).c140ClockValue != 0) {
                C140Inst c140 = new C140Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).c140DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = c140;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", C140Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).c140ClockValue;
                    chip.option = new Object[] {((Vgm) audio.driverVirtual).C140Type};

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriC140", 1);
                    else audio.chipLED.put("SecC140", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.C140 : Common.EnmChip.S_C140);
                }
            }

            if (((Vgm) audio.driverVirtual).multiPCMClockValue != 0) {
                MultiPcmInst multipcm = new MultiPcmInst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).multiPCMDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = multipcm;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", MultiPcmInst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).multiPCMClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriMPCM", 1);
                    else audio.chipLED.put("SecMPCM", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.MultiPCM : Common.EnmChip.S_MultiPCM);
                }
            }

            if (((Vgm) audio.driverVirtual).okiM6258ClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                OkiM6258Inst okim6258 = new OkiM6258Inst();
                chip.instrument = okim6258;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", OkiM6258Inst.class);
                chip.clock = ((Vgm) audio.driverVirtual).okiM6258ClockValue;
                chip.option = new Object[] {((Vgm) audio.driverVirtual).okiM6258Type};
                //chips.option = new Object[1] { 6 };
                okim6258.okim6258_set_srchg_cb((byte) 0, this::changeChipSampleRate, chip);

                hiyorimiDeviceFlag |= 0x2;

                audio.chipLED.put("PriOKI5", 1);

                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.OKIM6258);
            }

            if (((Vgm) audio.driverVirtual).okiM6295ClockValue != 0) {
                OkiM6295Inst okim6295 = new OkiM6295Inst();
                for (byte i = 0; i < (((Vgm) audio.driverVirtual).okiM6295DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = okim6295;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", OkiM6295Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).okiM6295ClockValue;
                    chip.option = null;
                    okim6295.okim6295_set_srchg_cb(i, this::changeChipSampleRate, chip);

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOKI9", 1);
                    else audio.chipLED.put("SecOKI9", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.OKIM6295 : Common.EnmChip.S_OKIM6295);
                }
            }

            if (((Vgm) audio.driverVirtual).segaPCMClockValue != 0) {
                chip = new MDSound.Chip();
                chip.id = 0;
                SegaPcmInst segapcm = new SegaPcmInst();
                chip.instrument = segapcm;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", SegaPcmInst.class);
                chip.clock = ((Vgm) audio.driverVirtual).segaPCMClockValue;
                chip.option = new Object[] {((Vgm) audio.driverVirtual).segaPCMInterface};

                hiyorimiDeviceFlag |= 0x2;

                audio.chipLED.put("PriSPCM", 1);

                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.SEGAPCM);
            }

            if (((Vgm) audio.driverVirtual).yn2608ClockValue != 0) {
                Ym2608Inst ym2608 = new Ym2608Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2608DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ym2608;
                    chip.samplingRate = 55467; // (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).yn2608ClockValue;
                    chip.setVolumes.put("FM", ym2608::setFMVolume);
                    chip.setVolumes.put("PSG", ym2608::setPSGVolume);
                    chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
                    chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
                    Function<String, Stream> fn = Common::getOPNARyhthmStream;
                    chip.option = new Object[] {fn};
                    hiyorimiDeviceFlag |= 0x2;
                    audio.clockYM2608 = ((Vgm) audio.driverVirtual).yn2608ClockValue;

                    if (i == 0) audio.chipLED.put("PriOPNA", 1);
                    else audio.chipLED.put("SecOPNA", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM2608 : Common.EnmChip.S_YM2608);
                }
            }

            if (((Vgm) audio.driverVirtual).yn2151ClockValue != 0) {
                Ym2151Inst ym2151 = null;
                MameYm2151Inst ym2151_mame = null;
                X68SoundYm2151Inst ym2151_x68sound = null;
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2151DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;

                    if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[0]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[0])) {
                        if (ym2151 == null) ym2151 = new Ym2151Inst();
                        chip.instrument = ym2151;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                        if (ym2151_mame == null) ym2151_mame = new MameYm2151Inst();
                        chip.instrument = ym2151_mame;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                        if (ym2151_x68sound == null) ym2151_x68sound = new X68SoundYm2151Inst();
                        chip.instrument = ym2151_x68sound;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).yn2151ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPM", 1);
                    else audio.chipLED.put("SecOPM", 1);

                    if (chip.instrument != null)
                        lstChips.add(chip);

                    audio.useChip.add(i == 0 ? Common.EnmChip.YM2151 : Common.EnmChip.S_YM2151);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2203ClockValue != 0) {
                Ym2203Inst ym2203 = new Ym2203Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2203DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ym2203;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2203Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ym2203ClockValue;
                    chip.setVolumes.put("FM", ym2203::setFMVolume);
                    chip.setVolumes.put("PSG", ym2203::setPSGVolume);
                    chip.option = null;

                    audio.clockYM2203 = ((Vgm) audio.driverVirtual).ym2203ClockValue;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPN", 1);
                    else audio.chipLED.put("SecOPN", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM2203 : Common.EnmChip.S_YM2203);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2610ClockValue != 0) {
                Ym2610Inst ym2610 = new Ym2610Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2610DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ym2610;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2610Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ym2610ClockValue & 0x7fffffff;
                    chip.setVolumes.put("FM", ym2610::setFMVolume);
                    chip.setVolumes.put("PSG", ym2610::setPSGVolume);
                    chip.setVolumes.put("AdpcmA", ym2610::setAdpcmAVolume);
                    chip.setVolumes.put("AdpcmB", ym2610::setAdpcmBVolume);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPNB", 1);
                    else audio.chipLED.put("SecOPNB", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM2610 : Common.EnmChip.S_YM2610);
                }
            }

            if (((Vgm) audio.driverVirtual).ym3812ClockValue != 0) {
                Ym3812Inst ym3812 = new Ym3812Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym3812DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ym3812;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym3812Inst.class)
                    ;
                    chip.clock = ((Vgm) audio.driverVirtual).ym3812ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPL2", 1);
                    else audio.chipLED.put("SecOPL2", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM3812 : Common.EnmChip.S_YM3812);
                }
            }

            if (((Vgm) audio.driverVirtual).ymF262ClockValue != 0) {
                YmF262Inst ymf262 = new YmF262Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF262DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ymf262;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", YmF262Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ymF262ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPL3", 1);
                    else audio.chipLED.put("SecOPL3", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YMF262 : Common.EnmChip.S_YMF262);
                }
            }

            if (((Vgm) audio.driverVirtual).ymF271ClockValue != 0) {
                YmF271Inst ymf271 = new YmF271Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF271DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ymf271;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", YmF271Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ymF271ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPX", 1);
                    else audio.chipLED.put("SecOPX", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YMF271 : Common.EnmChip.S_YMF271);
                }
            }

            if (((Vgm) audio.driverVirtual).ymF278BClockValue != 0) {
                YmF278bInst ymf278b = new YmF278bInst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymF278BDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ymf278b;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", YmF278bInst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ymF278BClockValue & 0x7fffffff;
                    chip.option = new Object[] {Common.getApplicationFolder()};

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPL4", 1);
                    else audio.chipLED.put("SecOPL4", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YMF278B : Common.EnmChip.S_YMF278B);
                }
            }

            if (((Vgm) audio.driverVirtual).ymZ280BClockValue != 0) {
                YmZ280bInst ymz280b = new YmZ280bInst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ymZ280BDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = ymz280b;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", YmZ280bInst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ymZ280BClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriYMZ", 1);
                    else audio.chipLED.put("SecYMZ", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YMZ280B : Common.EnmChip.S_YMZ280B);
                }
            }

            if (((Vgm) audio.driverVirtual).ay8910ClockValue != 0) {
                Ay8910Inst ay8910 = null;
                MameAy8910Inst ay8910mame = null;

                for (int i = 0; i < (((Vgm) audio.driverVirtual).ay8910DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;

                    if ((i == 0 && setting.getAY8910Type()[0].getUseEmu()[0])
                            || (i == 1 && setting.getAY8910Type()[1].getUseEmu()[0])) {
                        if (ay8910 == null) ay8910 = new Ay8910Inst();
                        chip.instrument = ay8910;
                    } else if ((i == 0 && setting.getAY8910Type()[0].getUseEmu()[1])
                            || (i == 1 && setting.getAY8910Type()[1].getUseEmu()[1])) {
                        if (ay8910mame == null) ay8910mame = new MameAy8910Inst();
                        chip.instrument = ay8910mame;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ay8910Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).ay8910ClockValue & 0x7fffffff) / 2;
                    audio.clockAY8910 = chip.clock;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriAY10", 1);
                    else audio.chipLED.put("SecAY10", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.AY8910 : Common.EnmChip.S_AY8910);
                }
            }

            if (((Vgm) audio.driverVirtual).ym2413ClockValue != 0) {
                Instrument opll;
                if (!((Vgm) audio.driverVirtual).ym2413VRC7Flag) {
                    opll = new Ym2413Inst();
                } else {
                    opll = new VRC7();
                }

                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym2413DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = opll;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym2413Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).ym2413ClockValue & 0x7fffffff);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPLL", 1);
                    else audio.chipLED.put("SecOPLL", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM2413 : Common.EnmChip.S_YM2413);
                }
            }

            if (((Vgm) audio.driverVirtual).huC6280ClockValue != 0) {
                HuC6280Inst huc6280 = new HuC6280Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).huC6280DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = huc6280;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", HuC6280Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).huC6280ClockValue & 0x7fffffff);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriHuC", 1);
                    else audio.chipLED.put("SecHuC", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.HuC6280 : Common.EnmChip.S_HuC6280);
                }
            }

            if (((Vgm) audio.driverVirtual).qSoundClockValue != 0) {
                CtrQSoundInst qsound = new CtrQSoundInst();
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = qsound;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", QSoundInst.class);
                chip.clock = (((Vgm) audio.driverVirtual).qSoundClockValue);// & 0x7fffffff);
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                //if (i == 0) audio.chipLED.put("PriHuC", 1);
                //else audio.chipLED.put("SecHuC", 1);
                audio.chipLED.put("PriQsnd", 1);

                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.QSound);
            }

            if (((Vgm) audio.driverVirtual).saa1099ClockValue != 0) {
                Saa1099Inst saa1099 = new Saa1099Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).saA1099DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = saa1099;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Saa1099Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).saa1099ClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriSAA", 1);
                    else audio.chipLED.put("SecSAA", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.SAA1099 : Common.EnmChip.S_SAA1099);
                }
            }

            if (((Vgm) audio.driverVirtual).wSwanClockValue != 0) {
                WsAudioInst WSwan = new WsAudioInst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).wSwanDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.instrument = WSwan;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", WsAudioInst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).wSwanClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriWSW", 1);
                    else audio.chipLED.put("SecWSW", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.WSwan : Common.EnmChip.S_WSwan);
                }
            }

            if (((Vgm) audio.driverVirtual).pokeyClockValue != 0) {
                PokeyInst pokey = new PokeyInst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).pokeyDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = pokey;
                    chip.samplingRate = (((Vgm) audio.driverVirtual).pokeyClockValue & 0x3fff_ffff); // (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", PokeyInst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).pokeyClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriPOK", 1);
                    else audio.chipLED.put("SecPOK", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.POKEY : Common.EnmChip.S_POKEY);
                }
            }

            if (((Vgm) audio.driverVirtual).x1_010ClockValue != 0) {
                X1_010Inst X1_010 = new X1_010Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).x1_010DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = X1_010;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", X1_010Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).x1_010ClockValue & 0x3fff_ffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriX1010", 1);
                    else audio.chipLED.put("SecX1010", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.X1_010 : Common.EnmChip.S_X1_010);
                }
            }

            if (((Vgm) audio.driverVirtual).c352ClockValue != 0) {
                C352Inst c352 = new C352Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).c352DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = c352;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", C352Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).c352ClockValue & 0x7fff_ffff);
                    chip.setVolumes.put("Rear", c352::setRearMute);
                    chip.option = new Object[] {(((Vgm) audio.driverVirtual).c352ClockDivider)};
                    int divider = (((Vgm) audio.driverVirtual).c352ClockDivider) != 0 ? (((Vgm) audio.driverVirtual).c352ClockDivider) : 288;
                    audio.clockC352 = chip.clock / divider;
                    c352.c352_set_options((byte) (((Vgm) audio.driverVirtual).c352ClockValue >> 31));
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriC352", 1);
                    else audio.chipLED.put("SecC352", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.C352 : Common.EnmChip.S_C352);
                }
            }

            if (((Vgm) audio.driverVirtual).ga20ClockValue != 0) {
                Ga20Inst ga20 = new Ga20Inst();
                for (int i = 0; i < (((Vgm) audio.driverVirtual).ga20DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = ga20;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ga20Inst.class);
                    chip.clock = (((Vgm) audio.driverVirtual).ga20ClockValue & 0x7fff_ffff);
                    chip.option = null;
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriGA20", 1);
                    else audio.chipLED.put("SecGA20", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.GA20 : Common.EnmChip.S_GA20);
                }
            }

            if (((Vgm) audio.driverVirtual).k053260ClockValue != 0) {
                K053260Inst k053260 = new K053260Inst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).k053260DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = k053260;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", K053260Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).k053260ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriK053260", 1);
                    else audio.chipLED.put("SecK053260", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.K053260 : Common.EnmChip.S_K053260);
                }
            }

            if (((Vgm) audio.driverVirtual).k054539ClockValue != 0) {
                K054539Inst k054539 = new K054539Inst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).k054539DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = k054539;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", K054539Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).k054539ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriK054539", 1);
                    else audio.chipLED.put("SecK054539", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.K054539 : Common.EnmChip.S_K054539);
                }
            }

            if (((Vgm) audio.driverVirtual).k051649ClockValue != 0) {
                K051649Inst k051649 = new K051649Inst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).k051649DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = k051649;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", K051649Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).k051649ClockValue;
                    audio.clockK051649 = chip.clock;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriK051649", 1);
                    else audio.chipLED.put("SecK051649", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.K051649 : Common.EnmChip.S_K051649);
                }
            }

            if (((Vgm) audio.driverVirtual).ym3526ClockValue != 0) {
                Ym3526Inst ym3526 = new Ym3526Inst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).ym3526DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = ym3526;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Ym3526Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).ym3526ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriOPL", 1);
                    else audio.chipLED.put("SecOPL", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.YM3526 : Common.EnmChip.S_YM3526);
                }
            }

            if (((Vgm) audio.driverVirtual).y8950ClockValue != 0) {
                Y8950Inst y8950 = new Y8950Inst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).y8950DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = y8950;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", Y8950Inst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).y8950ClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriY8950", 1);
                    else audio.chipLED.put("SecY8950", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.Y8950 : Common.EnmChip.S_Y8950);
                }
            }

            if (((Vgm) audio.driverVirtual).dmgClockValue != 0) {
                DmgInst dmg = new DmgInst();

                for (int i = 0; i < (((Vgm) audio.driverVirtual).dmgDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = dmg;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", DmgInst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).dmgClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriDMG", 1);
                    else audio.chipLED.put("SecDMG", 1);

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.DMG : Common.EnmChip.S_DMG);
                }
            }

            if (((Vgm) audio.driverVirtual).nesClockValue != 0) {

                for (int i = 0; i < (((Vgm) audio.driverVirtual).nesDualChipFlag ? 2 : 1); i++) {
                    IntFNesInst nes = new IntFNesInst();
                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = nes;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.class);
                    chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriNES", 1);
                    else audio.chipLED.put("SecNES", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.NES : Common.EnmChip.S_NES);

                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = nes;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.DMC.class);
                    chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriDMC", 1);
                    else audio.chipLED.put("SecDMC", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.DMC : Common.EnmChip.S_DMC);


                    chip = new MDSound.Chip();
                    chip.id = i;
                    chip.instrument = nes;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.FDS.class);
                    chip.clock = ((Vgm) audio.driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) audio.chipLED.put("PriFDS", 1);
                    else audio.chipLED.put("SecFDS", 1);

                    lstChips.add(chip);
                    audio.useChip.add(i == 0 ? Common.EnmChip.FDS : Common.EnmChip.S_FDS);


                    hiyorimiDeviceFlag |= 0x2;
                }
            }

            audio.hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && audio.hiyorimiNecessary;

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));


            if (audio.useChip.contains(Common.EnmChip.YM2203) || audio.useChip.contains(Common.EnmChip.S_YM2203)) {
                audio.chipRegister.setYM2203Register(0, 0x7, 0x3f, Common.EnmModel.RealModel); // 出力オフ
                audio.chipRegister.setYM2203Register(1, 0x7, 0x3f, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2203Register(0, 0x8, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2203Register(1, 0x8, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2203Register(0, 0x9, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2203Register(1, 0x9, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2203Register(0, 0xa, 0x0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2203Register(1, 0xa, 0x0, Common.EnmModel.RealModel);
                audio.setVolume("FM", Ym2203Inst.class, true, setting.getBalance().getVolume("FM", Ym2203Inst.class));
                audio.setVolume("PSG", Ym2203Inst.class, true, setting.getBalance().getVolume("PSG", Ym2203Inst.class));
            }

            if (audio.useChip.contains(Common.EnmChip.YM2608) || audio.useChip.contains(Common.EnmChip.S_YM2608)) {
                audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
                audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
                audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
                audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));
            }

            if (audio.useChip.contains(Common.EnmChip.YM2610) || audio.useChip.contains(Common.EnmChip.S_YM2610)) {

                audio.setVolume("FM", Ym2610Inst.class, true, setting.getBalance().getVolume("FM", Ym2610Inst.class));
                audio.setVolume("PSG", Ym2610Inst.class, true, setting.getBalance().getVolume("PSG", Ym2610Inst.class));
                audio.setVolume("AdpcmA", Ym2610Inst.class, true, setting.getBalance().getVolume("AdpcmA", Ym2610Inst.class));
                audio.setVolume("AdpcmB", Ym2610Inst.class, true, setting.getBalance().getVolume("AdpcmB", Ym2610Inst.class));
            }

            if (audio.useChip.contains(Common.EnmChip.AY8910))
                audio.chipRegister.writeAY8910Clock((byte) 0, ((Vgm) audio.driverVirtual).ay8910ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_AY8910))
                audio.chipRegister.writeAY8910Clock((byte) 1, ((Vgm) audio.driverVirtual).ay8910ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 0, ((Vgm) audio.driverVirtual).yn2151ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 1, ((Vgm) audio.driverVirtual).yn2151ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM2203))
                audio.chipRegister.writeYm2203Clock((byte) 0, ((Vgm) audio.driverVirtual).ym2203ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2203))
                audio.chipRegister.writeYm2203Clock((byte) 1, ((Vgm) audio.driverVirtual).ym2203ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM2608))
                audio.chipRegister.writeYm2608Clock((byte) 0, ((Vgm) audio.driverVirtual).yn2608ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.S_YM2608))
                audio.chipRegister.writeYm2608Clock((byte) 1, ((Vgm) audio.driverVirtual).yn2608ClockValue, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.YM3526)) {
                audio.chipRegister.setYM3526Register(0, 0xbd, 0, Common.EnmModel.RealModel); // リズムモードオフ
                audio.chipRegister.writeYm3526Clock((byte) 0, ((Vgm) audio.driverVirtual).ym3526ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.S_YM3526)) {
                audio.chipRegister.setYM3526Register(1, 0xbd, 0, Common.EnmModel.RealModel); // リズムモードオフ
                audio.chipRegister.writeYm3526Clock((byte) 1, ((Vgm) audio.driverVirtual).ym3526ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.YM3812)) {
                audio.chipRegister.setYM3812Register(0, 0xbd, 0, Common.EnmModel.RealModel); // リズムモードオフ
                audio.chipRegister.writeYm3812Clock((byte) 0, ((Vgm) audio.driverVirtual).ym3812ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.S_YM3812)) {
                audio.chipRegister.setYM3812Register(1, 0xbd, 0, Common.EnmModel.RealModel); // リズムモードオフ
                audio.chipRegister.writeYm3812Clock((byte) 1, ((Vgm) audio.driverVirtual).ym3812ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.YMF262)) {
                audio.chipRegister.setYMF262Register(0, 0, 0xbd, 0, Common.EnmModel.RealModel); // リズムモードオフ
                audio.chipRegister.setYMF262Register(0, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
                audio.chipRegister.writeYmF262Clock((byte) 0, ((Vgm) audio.driverVirtual).ymF262ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.useChip.contains(Common.EnmChip.S_YMF262)) {
                audio.chipRegister.setYMF262Register(1, 0, 0xbd, 0, Common.EnmModel.RealModel); // リズムモードオフ
                audio.chipRegister.setYMF262Register(1, 1, 5, 1, Common.EnmModel.RealModel); // opl3mode
                audio.chipRegister.writeYmF262Clock((byte) 1, ((Vgm) audio.driverVirtual).ymF262ClockValue, Common.EnmModel.RealModel);
            }
            if (audio.sn76489NGPFlag) {
                audio.chipRegister.setSN76489Register(0, 0xe5, Common.EnmModel.RealModel); // white noise mode
                audio.chipRegister.setSN76489Register(1, 0xe5, Common.EnmModel.RealModel); // white noise mode
                audio.chipRegister.setSN76489Register(0, 0xe5, Common.EnmModel.VirtualModel); // white noise mode
                audio.chipRegister.setSN76489Register(1, 0xe5, Common.EnmModel.VirtualModel); // white noise mode
            }
            if (audio.useChip.contains(Common.EnmChip.YM2610)) {
                // control2 レジスタのパンをセンターに予め設定
                audio.chipRegister.setYM2610Register(0, 0, 0x11, 0xc0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2610Register(0, 0, 0x11, 0xc0, Common.EnmModel.VirtualModel);
            }
            if (audio.useChip.contains(Common.EnmChip.S_YM2610)) {
                // control2 レジスタのパンをセンターに予め設定
                audio.chipRegister.setYM2610Register(1, 0, 0x11, 0xc0, Common.EnmModel.RealModel);
                audio.chipRegister.setYM2610Register(1, 0, 0x11, 0xc0, Common.EnmModel.VirtualModel);
            }
            if (audio.useChip.contains(Common.EnmChip.C140))
                audio.chipRegister.writeC140Type((byte) 0, ((Vgm) audio.driverVirtual).C140Type, Common.EnmModel.RealModel);
            if (audio.useChip.contains(Common.EnmChip.SEGAPCM))
                audio.chipRegister.writeSEGAPCMClock((byte) 0, ((Vgm) audio.driverVirtual).segaPCMClockValue, Common.EnmModel.RealModel);

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
                    audio.chipRegister.setYM2203SSGVolume((byte) 0, SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.S_YM2203))
                    audio.chipRegister.setYM2203SSGVolume((byte) 1, SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.YM2608))
                    audio.chipRegister.setYM2608SSGVolume((byte) 0, SSGVolumeFromTAG, Common.EnmModel.RealModel);
                if (audio.useChip.contains(Common.EnmChip.S_YM2608))
                    audio.chipRegister.setYM2608SSGVolume((byte) 1, SSGVolumeFromTAG, Common.EnmModel.RealModel);
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
            ex.printStackTrace();
            return false;
        }
    }
}
