package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.Ym2151Inst;
import mdsound.instrument.MameYm2151Inst;
import mdsound.instrument.X68SoundYm2151Inst;
import mdsound.x68sound.SoundIocs;
import mdsound.x68sound.X68Sound;
import vavi.util.Debug;


/**
 * MDXPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDXPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MXDRV();
        audio.driverVirtual.setting = setting;
        ((MXDRV) audio.driverVirtual).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MXDRV();
            audio.driverReal.setting = setting;
            ((MXDRV) audio.driverReal).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
        }
        boolean r = mdxPlay(setting);
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
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

            audio.chipRegister.resetChips();
            audio.useChip.clear();
            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            audio.vgmRealFadeoutVol = 0;
            audio.vgmRealFadeoutVolWait = 4;

            audio.clearFadeoutVolume();

            audio.chipRegister.resetChips();

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
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[2]) {
                chip = new MDSound.Chip();
                chip.id = (byte) 0;
                chip.instrument = new X68SoundYm2151Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
                chip.clock = 4000000;
                chip.option = new Object[] {1, 0, 0};
            }
            if (chip != null) {
                lstChips.add(chip);
            }
            audio.useChip.add(Common.EnmChip.YM2151);

            X68SoundYm2151Inst mdxPCM_V = new X68SoundYm2151Inst();
            mdxPCM_V.x68sound[0] = new X68Sound();
            mdxPCM_V.sound_Iocs[0] = new SoundIocs(mdxPCM_V.x68sound[0]);
            X68SoundYm2151Inst mdxPCM_R = new X68SoundYm2151Inst();
            mdxPCM_R.x68sound[0] = new X68Sound();
            mdxPCM_R.sound_Iocs[0] = new SoundIocs(mdxPCM_R.x68sound[0]);
            audio.useChip.add(Common.EnmChip.OKIM6258);

            audio.chipLED.put("PriOPM", 1);
            audio.chipLED.put("PriOKI5", 1);

            audio.hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && audio.hiyorimiNecessary;

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            audio.setVolume("MAIN", Ym2151Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2151Inst.class));

            if (audio.useChip.contains(Common.EnmChip.YM2151))
                audio.chipRegister.writeYm2151Clock((byte) 0, 4000000, Common.EnmModel.RealModel);
            //chipRegister.writeYM2151Clock(1, 4000000, enmModel.RealModel);

            audio.driverVirtual.setYm2151Hosei(4000000);
            if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

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
            ex.printStackTrace();
            return false;
        }
    }
}
