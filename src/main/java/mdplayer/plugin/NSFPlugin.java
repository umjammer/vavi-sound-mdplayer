package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.nsf.Nsf;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.MDSound.Chip;
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
        boolean r = nsfPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean nsfPlay() {
        try {
            if (vgmBuf == null || setting == null) return false;

            //stop();

            audio.chipRegister.reset();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            audio.chipRegister.clearFadeoutVolume();

            audio.chipRegister.reset();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED.clear();
            audio.chipLED.put("PriNES", 1);
            audio.chipLED.put("PriDMC", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            ((Nsf) audio.driverVirtual).song = songNo;
            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                ((Nsf) audio.driverReal).song = songNo;
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            if (((Nsf) audio.driverVirtual).useFds) audio.chipLED.put("PriFDS", 1);
            if (((Nsf) audio.driverVirtual).useFme7) audio.chipLED.put("PriFME7", 1);
            if (((Nsf) audio.driverVirtual).useMmc5) audio.chipLED.put("PriMMC5", 1);
            if (((Nsf) audio.driverVirtual).useN106) audio.chipLED.put("PriN106", 1);
            if (((Nsf) audio.driverVirtual).useVrc6) audio.chipLED.put("PriVRC6", 1);
            if (((Nsf) audio.driverVirtual).useVrc7) audio.chipLED.put("PriVRC7", 1);

            MDSound.Chip chip;

            NesInst apu = Instrument.getInstrument(NesInst.class);
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = apu;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.class);
            chip.clock = 0;
            chip.setVolumes.put("APU", chip.mainWrappedSetVolume(apu::setVolume));
            chip.option = null;
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cAPU = chip;
            useChip.add(Common.EnmChip.NES);

            NesInst.DMC dmc = new NesInst.DMC();
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = dmc;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.setVolumes.put("DMC", chip.mainWrappedSetVolume(dmc::setVolume));
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.DMC.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cDMC = chip;
            useChip.add(Common.EnmChip.DMC);

            NesInst.FDS fds = new NesInst.FDS();
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = fds;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.setVolumes.put("FDS", chip.mainWrappedSetVolume(fds::setVolume));
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.FDS.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cFDS = chip;
            useChip.add(Common.EnmChip.FDS);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new NesInst.MMC5();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.MMC5.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cMMC5 = chip;
            useChip.add(Common.EnmChip.MMC5);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new NesInst.N160();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.N160.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cN160 = chip;
            useChip.add(Common.EnmChip.N163);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new NesInst.VRC6();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.VRC6.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cVRC6 = chip;
            useChip.add(Common.EnmChip.VRC6);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new NesInst.VRC7();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.VRC7.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cVRC7 = chip;
            useChip.add(Common.EnmChip.VRC7);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new NesInst.FME7();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, NesInst.FME7.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cFME7 = chip;
            useChip.add(Common.EnmChip.FME7);

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips);

            lstChips.toArray(new Chip[0]);

            audio.chipRegister.plugin(MidiPlugin.class).initChipRegisterNSF();

            //Play

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
