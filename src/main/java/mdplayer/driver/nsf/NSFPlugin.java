package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Consumer;

import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.NpNesChip;
import mdplayer.chips.NpNesChip.DmcChip;
import mdplayer.chips.NpNesChip.FdsChip;
import mdplayer.chips.NpNesChip.Fme7Chip;
import mdplayer.chips.NpNesChip.Mmc5Chip;
import mdplayer.chips.NpNesChip.N163Chip;
import mdplayer.chips.NpNesChip.Vrc6Chip;
import mdplayer.chips.NpNesChip.Vrc7Chip;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.BasePlugin.HasSongNo;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.NpNesInst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * NSFPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class NSFPlugin extends BasePlugin<NsfMdDriver> implements HasSongNo {

    private static final Logger logger = getLogger(NSFPlugin.class.getName());

    @Override
    public void setSongNo(int songNo) {
logger.log(Level.INFO, "songNo: " + songNo);
        this.songNo = songNo;
    }

    @Override
    public void prepare() {
        driverVirtual = new mdplayer.driver.nsf.NsfMdDriver(this); // use np driver

        driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new mdplayer.driver.nsf.NsfMdDriver(this);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {

        driverVirtual.setSong(songNo);
        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.setSong(songNo);
            driverReal.init(Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        NpNesInst apu = Instrument.getInstrument(NpNesInst.class);
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = apu;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, NpNesChip.class);
        chip.clock = 0;
        chip.option = null;
        put(NpNesChip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.DmcInst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, DmcChip.class);
        put(DmcChip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.FdsInst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, FdsChip.class);
        put(FdsChip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.Mmc5Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Mmc5Chip.class);
        put(Mmc5Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.N160Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, N163Chip.class);
        put(N163Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.Vrc6Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Vrc6Chip.class);
        put(Vrc6Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.Vrc7Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        Consumer<int[]> fn = ds -> { if (ds[7] != -1) apu.np_nes_vrc7_volume = ds[7]; };
        chip.option = new Object[] {fn};
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Vrc7Chip.class);
        put(Vrc7Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(NpNesInst.Fme7Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 0;
        chip.option = null;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Fme7Chip.class);
        put(Fme7Chip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        chipRegister.chip(NpNesChip.class).init();
        chipRegister.plugin(MidiPlugin.class).initChipRegisterNSF();
    }
}
