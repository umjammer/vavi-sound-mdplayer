package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.zgm.Zgm;
import mdplayer.format.FileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;


/**
 * ZGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class ZGMPlugin extends BasePlugin {

    private static final Logger logger = getLogger(ZGMPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Zgm();
        audio.driverVirtual.setting = setting;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new Zgm();
            audio.driverReal.setting = setting;
        }

        boolean r = zgmPlay(setting);
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean zgmPlay(Setting setting) {
        if (vgmBuf == null || setting == null) return false;

        try {
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

            // MIDIに対応するまで封印
            // startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (!audio.driverVirtual.init(vgmBuf
                    , audio.chipRegister
                    , Common.EnmModel.VirtualModel
                    , new Common.EnmChip[] {Common.EnmChip.YM2203}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            // MIDIに対応するまで封印
            //if (driverReal != null && !driverReal.init(vgmBuf
            //    , chipRegister
            //    , EnmModel.RealModel
            //    , new EnmChip[] { EnmChip.YM2203 }
            //    , (int)(setting.getoutputDevice().getSampleRate() * setting.LatencySCCI / 1000)
            //    , (int)(setting.getoutputDevice().getSampleRate() * setting.getoutputDevice().getWaitTime() / 1000)))
            //    return false;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            //
            //chips initialization
            //


            audio.hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && audio.hiyorimiNecessary;

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

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
