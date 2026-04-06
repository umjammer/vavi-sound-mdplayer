package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.format.MDLFileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MoonDriver (MSX, External Driver) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDLPlugin extends BasePlugin<MoonDriver> {

    private static final Logger logger = getLogger(MDLPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new MoonDriver();
        driverVirtual.setPlayingFileName(playingFileName);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new MoonDriverJava();
//            driverReal.setExtendFile((extendFile != null && !extendFile.isEmpty()) ? extendFile.get(0) : null);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        if (fileFormat instanceof MDLFileFormat) {
            vgmBuf = driverVirtual.compile(vgmBuf);
        }

        chipRegister.chip(Ym2151Chip.class).setFadeout(0, 0);
        chipRegister.chip(Ym2151Chip.class).setFadeout(1, 0);

        hiyorimiNecessary = setting.getHiyorimiMode();
        int hiyorimiDeviceFlag = 0;

        byte sndgen = vgmBuf[7];
        boolean EX_OPL3 = ((sndgen & 2) != 0);
        boolean OPL4_NOUSE = ((sndgen & 1) == 0);
        Class<? extends Chip>[] useChipFromMdr = new Class[1];

        if (OPL4_NOUSE && !EX_OPL3) {
            throw new IllegalArgumentException("The combination of OPL4_NOUSE and EX_OPL3 is invalid.");
        }

        if (EX_OPL3 && OPL4_NOUSE) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(YmF262Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class);
            chip.clock = 14318180;
            chip.option = new Object[] {Common.getApplicationFolder()};

            hiyorimiDeviceFlag |= 0x2;

            chipLED.put("PriOPL3", 1);

            put(YmF262Chip.class, chip);
            useChipFromMdr[0] = YmF262Chip.class;
        } else {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(YmF278BChip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
            chip.clock = 33868800;
            chip.option = new Object[] {Common.getApplicationFolder()};

            hiyorimiDeviceFlag |= 0x2;

            chipLED.put("PriOPL4", 1);

            put(YmF278BChip.class, chip);
            useChipFromMdr[0] = YmF278BChip.class;
        }

        if (hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary) hiyorimiNecessary = true;
        else hiyorimiNecessary = false;

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        if (EX_OPL3 && OPL4_NOUSE) setVolume(MAIN_TAG, YmF262Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class));
        else setVolume(MAIN_TAG, YmF278BChip.class, true, setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class));
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), RnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }
}
