package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.NesChip;
import mdplayer.chips.NesChip.DmcChip;
import mdplayer.chips.NesChip.FdsChip;
import mdplayer.chips.NesChip.Fme7Chip;
import mdplayer.chips.NesChip.Mmc5Chip;
import mdplayer.chips.NesChip.N160Chip;
import mdplayer.chips.NesChip.Vrc6Chip;
import mdplayer.chips.NesChip.Vrc7Chip;
import mdplayer.driver.nsf.Nsf;
import mdplayer.format.FileFormat;
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
    public boolean play(String playingFileName, FileFormat format) {
        NesInst.np_nes_apu_volume = 0;
        NesInst.np_nes_dmc_volume = 0;
        NesInst.np_nes_fds_volume = 0;
        NesInst.np_nes_fme7_volume = 0;
        NesInst.np_nes_mmc5_volume = 0;
        NesInst.np_nes_n106_volume = 0;
        NesInst.np_nes_vrc6_volume = 0;
        NesInst.np_nes_vrc7_volume = 0;

        audio.driverVirtual = new Nsf();
        audio.driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Nsf();
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
        startTrdVgmReal();

        audio.chipLED.put("PriNES", 1);
        audio.chipLED.put("PriDMC", 1);

        ((Nsf) audio.driverVirtual).song = songNo;
        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            ((Nsf) audio.driverReal).song = songNo;
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        if (((Nsf) audio.driverVirtual).useFds) audio.chipLED.put("PriFDS", 1);
        if (((Nsf) audio.driverVirtual).useFme7) audio.chipLED.put("PriFME7", 1);
        if (((Nsf) audio.driverVirtual).useMmc5) audio.chipLED.put("PriMMC5", 1);
        if (((Nsf) audio.driverVirtual).useN106) audio.chipLED.put("PriN106", 1);
        if (((Nsf) audio.driverVirtual).useVrc6) audio.chipLED.put("PriVRC6", 1);
        if (((Nsf) audio.driverVirtual).useVrc7) audio.chipLED.put("PriVRC7", 1);

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
        ((Nsf) audio.driverVirtual).cAPU = chip;

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
        ((Nsf) audio.driverVirtual).cDMC = chip;

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
        ((Nsf) audio.driverVirtual).cFDS = chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.MMC5();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Mmc5Chip.class);
        put(Mmc5Chip.class, chip);
        ((Nsf) audio.driverVirtual).cMMC5 = chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.N160();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, N160Chip.class);
        put(N160Chip.class, chip);
        ((Nsf) audio.driverVirtual).cN160 = chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.VRC6();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Vrc6Chip.class);
        put(Vrc6Chip.class, chip);
        ((Nsf) audio.driverVirtual).cVRC6 = chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.VRC7();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Vrc7Chip.class);
        put(Vrc7Chip.class, chip);
        ((Nsf) audio.driverVirtual).cVRC7 = chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new NesInst.FME7();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Fme7Chip.class);
        put(Fme7Chip.class, chip);
        ((Nsf) audio.driverVirtual).cFME7 = chip;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.chipRegister.plugin(MidiPlugin.class).initChipRegisterNSF();

        return true;
    }
}
