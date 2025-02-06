package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.MDSound.Chip;
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
        if (setting.getOutputDevice().getSampleRate() != 44100) {
logger.log(Level.WARNING, "sample rate: " + setting.getOutputDevice().getSampleRate());
            return false;
        }

        startTrdVgmReal();

        int hiyorimiDeviceFlag = 3;

        Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        chip.clock = 4000000;
        if (chip.instrument instanceof X68kYm2151Inst) {
            chip.option = new Object[] {1, 0, 0};
        }
        put(Ym2151Chip.class, chip);

        X68kYm2151Inst mdxPCM_V = Instrument.getInstrument(X68kYm2151Inst.class);
        mdxPCM_V.chips[0] = new X68Sound();
        mdxPCM_V.soundIocs[0] = new SoundIocs(mdxPCM_V.chips[0]);
        X68kYm2151Inst mdxPCM_R = Instrument.getInstrument(X68kYm2151Inst.class);
        mdxPCM_R.chips[0] = new X68Sound();
        mdxPCM_R.soundIocs[0] = new SoundIocs(mdxPCM_R.chips[0]);

        audio.chipLED.put("PriOPM", 1);
        audio.chipLED.put("PriOKI5", 1);

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));

        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2151Chip.class).writeClock(1, 4000000, enmModel.RealModel);

        audio.driverVirtual.setYm2151Hosei(4000000);
        if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        boolean retV = ((mdplayer.driver.mxdrv.MXDRV) audio.driverVirtual).init(
                vgmBuf,
                audio.chipRegister,
                Common.EnmModel.VirtualModel,
                new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                mdxPCM_V);
        boolean retR = true;
        if (audio.driverReal != null) {
            retR = ((mdplayer.driver.mxdrv.MXDRV) audio.driverReal).init(
                    vgmBuf,
                    audio.chipRegister,
                    Common.EnmModel.RealModel,
                    new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    mdxPCM_R);
        }

        if (!retV || !retR) {
            audio.errMsg = !audio.driverVirtual.errMsg.isEmpty() ? audio.driverVirtual.errMsg : (audio.driverReal != null ? audio.driverReal.errMsg : "");
            return false;
        }

        return true;
    }
}
