package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.MDSound.Chip;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.x68sound.SoundIocs;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MDXDRV (X68000) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDXPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MDXPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MXDRV();
        ((MXDRV) audio.driverVirtual).extendFile = (extendFiles != null && !extendFiles.isEmpty()) ? extendFiles.get(0) : null;
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new MXDRV();
//            ((MXDRV) audio.driverReal).extendFiles = (extendFiles != null && !extendFiles.isEmpty()) ? extendFiles.get(0) : null;
//        }
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
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        chip.clock = 4000000;
        if (chip.instrument instanceof X68kYm2151Inst) {
            chip.option = new Object[] {1, 0, 0};
        }
        chip.samplingRate = chip.clock / 64;
        put(Ym2151Chip.class, chip);

        X68kYm2151Inst mdxPCM_V = Instrument.getInstrument(X68kYm2151Inst.class); // virtual
        mdxPCM_V.soundIocs[0] = new SoundIocs(mdxPCM_V.chips[0]);
        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = mdxPCM_V;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        chip.clock = 4000000;
        chip.samplingRate = setting.getOutputDevice().getSampleRate(); // TODO vavi
        put(Ym2151Chip.class, chip);

        X68kYm2151Inst mdxPCM_R = Instrument.getInstrument(X68kYm2151Inst.class); // real
        mdxPCM_R.soundIocs[0] = new SoundIocs(mdxPCM_R.chips[0]);
        X68kYm2151Inst mdxPCM_P = Instrument.getInstrument(X68kYm2151Inst.class); // piano roll
        mdxPCM_P.soundIocs[0] = new SoundIocs(mdxPCM_P.chips[0]);

        Pcm8PPInst pcm8pp = Instrument.getInstrument(Pcm8PPInst.class);
        ((MXDRV) audio.driverVirtual).pcm8type = 0;
        if (setting.getMxDrv().pcm8Type == 0) {
            // mxdrv is special and requires PCM8
        } else {
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = pcm8pp;
            chip.volume = 0;
            chip.clock = 4_000_000;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.option = new Object[] {setting.getMxDrv().pcm8ppsOption};
            put(Pcm8Chip.class, chip);
            ((MXDRV) audio.driverVirtual).pcm8type = 1;
        }

        audio.chipLED.put("PriOPM", 1);
        audio.chipLED.put("PriOKI5", 1);

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));

        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
//            audio.chipRegister.chip(Ym2151Chip.class).writeClock(1, 4000000, enmModel.RealModel);
        audio.chipRegister.chip(Ym2151Chip.class).getUse4MYM2151scci()[0] = false;
        if (setting.getYM2151Type()[0].getUseRealChipFreqDiff() != null
                && setting.getYM2151Type()[0].getUseRealChipFreqDiff().length > 0
                && setting.getYM2151Type()[0].getUseRealChipFreqDiff()[0]) {
            audio.chipRegister.chip(Ym2151Chip.class).getUse4MYM2151scci()[0] = true;
        }

        audio.driverVirtual.setYm2151Hosei(4000000);
        if (audio.driverReal != null) audio.driverReal.setYm2151Hosei(4000000);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        audio.chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        boolean retV = ((mdplayer.driver.mxdrv.MXDRV) audio.driverVirtual).init(
                vgmBuf,
                this,
                Common.EnmModel.VirtualModel,
                new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                mdxPCM_V, pcm8pp);
        boolean retR = true;
        if (audio.driverReal != null) {
            retR = ((mdplayer.driver.mxdrv.MXDRV) audio.driverReal).init(
                    vgmBuf,
                    this,
                    Common.EnmModel.RealModel,
                    new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    mdxPCM_R, pcm8pp);
        }

        if (!retV || !retR) {
            audio.errMsg = !audio.driverVirtual.errMsg.isEmpty() ? audio.driverVirtual.errMsg : (audio.driverReal != null ? audio.driverReal.errMsg : "");
logger.log(Level.WARNING, "cannot start: " + audio.errMsg);
            return false;
        }

        return true;
    }
}
