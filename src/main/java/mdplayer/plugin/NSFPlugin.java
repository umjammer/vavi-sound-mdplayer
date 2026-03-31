package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.NesChip;
import mdplayer.chips.NesChip.DmcChip;
import mdplayer.chips.NesChip.FdsChip;
import mdplayer.chips.NesChip.Fme7Chip;
import mdplayer.chips.NesChip.Mmc5Chip;
import mdplayer.chips.NesChip.N163Chip;
import mdplayer.chips.NesChip.Vrc6Chip;
import mdplayer.chips.NesChip.Vrc7Chip;
import mdplayer.driver.nsf.NsfDriver;
import mdplayer.driver.nsf.NsfMdDriver2;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.NesInst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * NSFPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class NSFPlugin extends BasePlugin {

    private static final Logger logger = getLogger(NSFPlugin.class.getName());

    @Override
    public void prepare() {
        NesInst nesInst = Instrument.getInstrument(NesInst.class);
        nesInst.np_nes_apu_volume = 0;
        nesInst.np_nes_dmc_volume = 0;
        nesInst.np_nes_fds_volume = 0;
        nesInst.np_nes_fme7_volume = 0;
        nesInst.np_nes_mmc5_volume = 0;
        nesInst.np_nes_n106_volume = 0;
        nesInst.np_nes_vrc6_volume = 0;
        nesInst.np_nes_vrc7_volume = 0;

        driverVirtual = new NsfMdDriver2();

        driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Nsf();
//        }

        prepareInternal();
        initChips();
    }

    @Override
    protected void initChips() {
        startTrdVgmReal();

        chipLED.put("PriNES", 1);
        chipLED.put("PriDMC", 1);

        ((NsfDriver) driverVirtual).setSong(songNo);
        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            ((NsfDriver) driverReal).setSong(songNo);;
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        if (((NsfDriver) driverVirtual).useFds())  chipLED.put("PriFDS", 1);
        if (((NsfDriver) driverVirtual).useFme7()) chipLED.put("PriFME7", 1);
        if (((NsfDriver) driverVirtual).useMmc5()) chipLED.put("PriMMC5", 1);
        if (((NsfDriver) driverVirtual).useN106()) chipLED.put("PriN106", 1);
        if (((NsfDriver) driverVirtual).useVrc6()) chipLED.put("PriVRC6", 1);
        if (((NsfDriver) driverVirtual).useVrc7()) chipLED.put("PriVRC7", 1);

        NesInst apu = Instrument.getInstrument(NesInst.class);
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = apu;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesChip.class);
        chip.clock = 0;
        chip.setVolumes.put("APU", chip.mainWrappedSetVolume(apu::setVolume));
        chip.option = null;
        put(NesChip.class, chip);
        ((NsfDriver) driverVirtual).setApu(chip);

        NesInst.DMC dmc = new NesInst.DMC();
        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = dmc;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.setVolumes.put("DMC", chip.mainWrappedSetVolume(dmc::setVolume));
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, DmcChip.class);
        put(DmcChip.class, chip);
        ((NsfDriver) driverVirtual).setDmc(chip);

        NesInst.FDS fds = new NesInst.FDS();
        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = fds;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.setVolumes.put("FDS", chip.mainWrappedSetVolume(fds::setVolume));
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, FdsChip.class);
        put(FdsChip.class, chip);
        ((NsfDriver) driverVirtual).setFds(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.MMC5();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Mmc5Chip.class);
        put(Mmc5Chip.class, chip);
        ((NsfDriver) driverVirtual).setMmc5(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.N160();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, N163Chip.class);
        put(N163Chip.class, chip);
        ((NsfDriver) driverVirtual).setN160(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.VRC6();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Vrc6Chip.class);
        put(Vrc6Chip.class, chip);
        ((NsfDriver) driverVirtual).setVrc6(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.VRC7();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Vrc7Chip.class);
        put(Vrc7Chip.class, chip);
        ((NsfDriver) driverVirtual).setVrc7(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.FME7();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Fme7Chip.class);
        put(Fme7Chip.class, chip);
        ((NsfDriver) driverVirtual).setFme7(chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        chipRegister.plugin(MidiPlugin.class).initChipRegisterNSF();
    }
}
