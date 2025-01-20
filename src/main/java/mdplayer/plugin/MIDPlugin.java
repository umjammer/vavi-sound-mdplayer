package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.mid.MID;
import mdplayer.format.FileFormat;

import static java.lang.System.getLogger;


/**
 * MIDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MIDPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MIDPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MID();
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MID();
        }
        boolean r = midPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
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
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipRegister.chipLED.clear();
            audio.chipRegister.chipLED.put("PriMID", 1);
            audio.chipRegister.chipLED.put("SecMID", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            audio.chipRegister.initChipRegister(null);
            audio.chipRegister.plugin(MidiPlugin.class).releaseAllMIDIout();
            audio.chipRegister.plugin(MidiPlugin.class).makeMIDIout(setting, midiMode);
            audio.chipRegister.plugin(MidiPlugin.class).setMIDIout(setting.getMidiOut().getMidiOutInfos().get(midiMode));

            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            // Play

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            audio.stopped = false;

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
