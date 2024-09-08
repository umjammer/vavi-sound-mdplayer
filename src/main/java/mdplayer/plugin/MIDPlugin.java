package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.mid.MID;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import vavi.util.Debug;


/**
 * MIDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MIDPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MID();
        audio.driverVirtual.setting = setting;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MID();
            audio.driverReal.setting = setting;
        }
        boolean r = midPlay();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean midPlay() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

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

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.chipLED.put("PriMID", 1);
            audio.chipLED.put("SecMID", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            audio.chipRegister.initChipRegister(null);
            audio.releaseAllMIDIout();
            audio.makeMIDIout(setting, midiMode);
            audio.chipRegister.setMIDIout(setting.getMidiOut().getMidiOutInfos().get(midiMode), audio.midiOuts, audio.midiOutsType);

            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            audio.stopped = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
