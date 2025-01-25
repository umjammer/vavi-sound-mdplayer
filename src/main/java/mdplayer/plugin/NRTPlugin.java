package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.nrtdrv.NRTDRV;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Ay8910Inst;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68SoundYm2151Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * NRTPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class NRTPlugin extends BasePlugin {

    private static final Logger logger = getLogger(NRTPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new NRTDRV();
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new NRTDRV();
        }
        boolean r = nrdPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean nrdPlay() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            int r = ((NRTDRV) audio.driverVirtual).checkUseChip(vgmBuf);

            audio.chipRegister.chip(Ym2151Chip.class).setFadeout(0, 0);
            audio.chipRegister.chip(Ym2151Chip.class).setFadeout(1, 0);

            audio.chipRegister.reset();

            useChip.clear();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;
            audio.chipRegister.clearFadeoutVolume();
            audio.chipRegister.reset();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            audio.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            Ym2151Inst ym2151 = null;
            MameYm2151Inst ym2151_mame = null;
            X68SoundYm2151Inst ym2151_x68sound = null;
            for (int i = 0; i < 2; i++) {
                if ((i == 0 && (r & 0x3) != 0) || (i == 1 && (r & 0x2) != 0)) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;

                    if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[0]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[0])) {
                        if (ym2151 == null) ym2151 = Instrument.getInstrument(Ym2151Inst.class);
                        chip.instrument = ym2151;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                        if (ym2151_mame == null) ym2151_mame = Instrument.getInstrument(MameYm2151Inst.class);
                        chip.instrument = ym2151_mame;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                        if (ym2151_x68sound == null) ym2151_x68sound = Instrument.getInstrument(X68SoundYm2151Inst.class);
                        chip.instrument = ym2151_x68sound;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class);
                    chip.clock = 4000000;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) audio.chipLED.put("PriOPM", 1);
                    else audio.chipLED.put("SecOPM", 1);

                    if (chip.instrument != null) {
                        lstChips.add(chip);
                        useChip.add(i == 0 ? Common.EnmChip.YM2151 : Common.EnmChip.S_YM2151);
                    }
                }
            }

            if ((r & 0x4) != 0) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(Ay8910Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Inst.class);
                chip.clock = 2000000 / 2;
//                audio.clockAY8910 = chip.clock;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x1;
                audio.chipLED.put("PriAY10", 1);

                lstChips.add(chip);
                useChip.add(Common.EnmChip.AY8910);
            }

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips);

            if (useChip.contains(Common.EnmChip.YM2151) || useChip.contains(Common.EnmChip.S_YM2151)) {
                audio.setVolume(MAIN_TAG, Ym2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class));
                audio.setVolume(MAIN_TAG, MameYm2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, MameYm2151Inst.class));
                audio.setVolume(MAIN_TAG, X68SoundYm2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, X68SoundYm2151Inst.class));
            }
            if (useChip.contains(Common.EnmChip.AY8910))
                audio.setVolume(MAIN_TAG, Ay8910Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ay8910Inst.class));

            if (useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
            if (useChip.contains(Common.EnmChip.S_YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 1, 4000000, Common.EnmModel.RealModel);

            if (audio.driverVirtual != null) audio.driverVirtual.setYm2151Hosei(4000000);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);
            //chipRegister.getChip(Ym2203Chip.class).setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.getChip(Ym2203Chip.class).setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.getChip(Ym2608Chip.class).setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.getChip(Ym2608Chip.class).setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            if (audio.driverVirtual != null) {
                audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2151, Common.EnmChip.AY8910}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
                ((NRTDRV) audio.driverVirtual).call(0);//
            }

            if (audio.driverReal != null) {
                audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2151, Common.EnmChip.AY8910}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
                ((NRTDRV) audio.driverReal).call(0);//
            }

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            ((NRTDRV) audio.driverVirtual).call(1); // MPLAY

            if (audio.driverReal != null) {
                ((NRTDRV) audio.driverReal).call(1); // MPLAY
            }

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
