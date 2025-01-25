package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.x68sound.SoundIocs;
import mdsound.x68sound.X68Sound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MDXPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDXPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MDXPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MXDRV();
        ((MXDRV) audio.driverVirtual).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MXDRV();
            ((MXDRV) audio.driverReal).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        }
        boolean r = mdxPlay(setting);
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mdxPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;
            if (setting.getOutputDevice().getSampleRate() != 44100) {
                return false;
            }
            //Stop();

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

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 3;

            audio.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip = null;

            if (setting.getYM2151Type()[0].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(Ym2151Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[1]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(MameYm2151Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[2]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = Instrument.getInstrument(X68kYm2151Inst.class);
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = new Object[] {1, 0, 0};
            }
            if (chip != null) {
                lstChips.add(chip);
            }
            useChip.add(Common.EnmChip.YM2151);

            X68kYm2151Inst mdxPCM_V = Instrument.getInstrument(X68kYm2151Inst.class);
            mdxPCM_V.chips[0] = new X68Sound();
            mdxPCM_V.soundIocs[0] = new SoundIocs(mdxPCM_V.chips[0]);
            X68kYm2151Inst mdxPCM_R = Instrument.getInstrument(X68kYm2151Inst.class);
            mdxPCM_R.chips[0] = new X68Sound();
            mdxPCM_R.soundIocs[0] = new SoundIocs(mdxPCM_R.chips[0]);
            useChip.add(Common.EnmChip.OKIM6258);

            audio.chipLED.put("PriOPM", 1);
            audio.chipLED.put("PriOKI5", 1);

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips);

            audio.setVolume(MAIN_TAG, Ym2151Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Inst.class));

            if (useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
//            audio.chipRegister.writeYM2151Clock(1, 4000000, enmModel.RealModel);

            audio.driverVirtual.setYm2151Hosei(4000000);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);
//            audio.chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
//            audio.chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
//            audio.chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
//            audio.chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            boolean retV = ((mdplayer.driver.mxdrv.MXDRV) audio.driverVirtual).init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
                    , mdxPCM_V);
            boolean retR = true;
            if (audio.driverReal != null) {
                retR = ((mdplayer.driver.mxdrv.MXDRV) audio.driverReal).init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
                        , mdxPCM_R);
            }

            if (!retV || !retR) {
                audio.errMsg = !audio.driverVirtual.errMsg.isEmpty() ? audio.driverVirtual.errMsg : (audio.driverReal != null ? audio.driverReal.errMsg : "");
                return false;
            }

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
