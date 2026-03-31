package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.mxdrv.MxDriver;
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
 * <p>
 * extendFile[0]: pdx data bytes
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDXPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MDXPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new MxDriver();
        ((MxDriver) driverVirtual).setExtendFile((extendFiles != null && !extendFiles.isEmpty()) ? extendFiles.getFirst() : null);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new MxDriver();
//            ((MxDriver) driverReal).extendFile = (extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null;
//        }

        prepareInternal();
    }

    @Override
    protected void initChips() {
        if (setting.getOutputDevice().getSampleRate() != 44100) {
            throw new IllegalStateException("supported sample rate is only 44100: " + setting.getOutputDevice().getSampleRate());
        }

        startTrdVgmReal();

        int hiyorimiDeviceFlag = 3;

        Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Ym2151Chip.class).instrument(0);
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
        ((MxDriver) driverVirtual).setPcm8type(0);
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
            ((MxDriver) driverVirtual).setPcm8type(1);
        }

        chipLED.put("PriOPM", 1);
        chipLED.put("PriOKI5", 1);

        hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        setVolume(MAIN_TAG, Ym2151Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class));

        if (contains(Ym2151Chip.class, 0))
            chipRegister.chip(Ym2151Chip.class).writeClock((byte) 0, 4000000, Common.EnmModel.RealModel);
//            chipRegister.chip(Ym2151Chip.class).writeClock(1, 4000000, enmModel.RealModel);
        chipRegister.chip(Ym2151Chip.class).getUse4MYM2151scci()[0] = false;
        if (setting.getYM2151Type()[0].getUseRealChipFreqDiff() != null
                && setting.getYM2151Type()[0].getUseRealChipFreqDiff().length > 0
                && setting.getYM2151Type()[0].getUseRealChipFreqDiff()[0]) {
            chipRegister.chip(Ym2151Chip.class).getUse4MYM2151scci()[0] = true;
        }

        chipRegister.chip(Ym2151Chip.class).setYm2151Hosei(EnmModel.VirtualModel, 4000000);
        if (driverReal != null) chipRegister.chip(Ym2151Chip.class).setYm2151Hosei(EnmModel.RealModel, 4000000);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        driverVirtual.init(
                vgmBuf,
                this,
                Common.EnmModel.VirtualModel,
                new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                mdxPCM_V, pcm8pp);
        if (driverReal != null) {
            driverReal.init(
                    vgmBuf,
                    this,
                    Common.EnmModel.RealModel,
                    new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    mdxPCM_R, pcm8pp);
        }
    }
}
