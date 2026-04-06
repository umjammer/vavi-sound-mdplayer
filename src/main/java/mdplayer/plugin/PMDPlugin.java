package mdplayer.plugin;

import java.lang.System.Logger;
import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Common;
import mdplayer.chips.P86Chip;
import mdplayer.chips.PpsChip;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.pmd.PmdDriver;
import mdsound.MDSound;
import mdsound.instrument.Ym2608Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * PMD (PC-9801) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class PMDPlugin extends BasePlugin<PmdDriver> {

    private static final Logger logger = getLogger(PMDPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new PmdDriver();
        driverVirtual.setPlayingFileName(playingFileName);

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0] && !setting.getYM2608Type()[0].getUseEmu()[1]) {
            driverReal = new PmdDriver();
            driverReal.setPlayingFileName(playingFileName);
        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Ym2608Chip.class).instrument(0);
        chip.samplingRate = 55467;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        chip.clock = PmdDriver.baseClock;
        if (chip.instrument instanceof Ym2608Inst ym2608) {
            chip.setVolumes.put("FM", ym2608::setVolume);
            chip.setVolumes.put("SSG", ym2608::setVolume);
            chip.setVolumes.put("RHYTHM", ym2608::setVolume);
            chip.setVolumes.put("ADPCM", ym2608::setVolume);
        }
        Function<String, Stream> fn = Common::getOPNARyhthmStream;
        chip.option = new Object[] {fn};
        put(Ym2608Chip.class, chip);
        chipRegister.chip(Ym2608Chip.class).clock = PmdDriver.baseClock;
        chipLED.put("PriOPNA", 1);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Ppz8Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ppz8Chip.class);
        chip.clock = PmdDriver.baseClock;
        chip.option = null;
        chipLED.put("PriPPZ8", 1);
        put(Ppz8Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(PpsChip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = 0;
        chip.clock = PmdDriver.baseClock;
        chip.option = null;
        chipLED.put("PriPPSDRV", 1);
        put(PpsChip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(P86Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = 0;
        chip.clock = PmdDriver.baseClock;
        chip.option = null;
        chipLED.put("PriP86", 1);
        put(P86Chip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
        setVolume("SSG", Ym2608Chip.class, true, setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        setVolume("RHYTHM", Ym2608Chip.class, true, setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        setVolume("ADPCM", Ym2608Chip.class, true, setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Reset with Psg TONE
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);

        chipRegister.chip(Ym2608Chip.class).writeClock(0, PmdDriver.baseClock, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).writeClock(1, PmdDriver.baseClock, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                fileFormat);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    fileFormat);
        }

//        if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//             SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
//        }
    }
}
