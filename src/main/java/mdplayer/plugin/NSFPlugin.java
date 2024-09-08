package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.nsf.Nsf;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.IntFNesInst;
import vavi.util.Debug;


/**
 * NSFPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class NSFPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        IntFNesInst.np_nes_apu_volume = 0;
        IntFNesInst.np_nes_dmc_volume = 0;
        IntFNesInst.np_nes_fds_volume = 0;
        IntFNesInst.np_nes_fme7_volume = 0;
        IntFNesInst.np_nes_mmc5_volume = 0;
        IntFNesInst.np_nes_n106_volume = 0;
        IntFNesInst.np_nes_vrc6_volume = 0;
        IntFNesInst.np_nes_vrc7_volume = 0;

        audio.driverVirtual = new Nsf();
        audio.driverVirtual.setting = setting;
        audio.driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Nsf();
//            driverReal.setting = setting;
//        }
        boolean r = nsfPlay();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean nsfPlay() {
        try {
            if (vgmBuf == null || setting == null) return false;

            //stop();

            audio.chipRegister.resetChips();

            audio.vgmFadeout = false;
            audio.vgmFadeoutCounter = 1.0;
            audio.vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            audio.vgmRealFadeoutVol = 0;
            audio.vgmRealFadeoutVolWait = 4;

            audio.clearFadeoutVolume();

            audio.chipRegister.resetChips();

            audio.useChip.clear();


            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.chipLED.put("PriNES", 1);
            audio.chipLED.put("PriDMC", 1);

            audio.masterVolume = setting.getBalance().getMasterVolume();

            ((Nsf) audio.driverVirtual).song = songNo;
            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                ((Nsf) audio.driverReal).song = songNo;
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.Unuse}
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

            IntFNesInst apu = new IntFNesInst();
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = apu;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.class);
            chip.clock = 0;
            chip.setVolumes.put("APU", chip.mainWrappedSetVolume(apu::setVolume));
            chip.option = null;
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cAPU = chip;
            audio.useChip.add(Common.EnmChip.NES);

            IntFNesInst.DMC dmc = new IntFNesInst.DMC();
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = dmc;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.setVolumes.put("DMC", chip.mainWrappedSetVolume(dmc::setVolume));
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.DMC.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cDMC = chip;
            audio.useChip.add(Common.EnmChip.DMC);

            IntFNesInst.FDS fds = new IntFNesInst.FDS();
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = fds;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.setVolumes.put("FDS", chip.mainWrappedSetVolume(fds::setVolume));
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.FDS.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cFDS = chip;
            audio.useChip.add(Common.EnmChip.FDS);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new IntFNesInst.MMC5();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.MMC5.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cMMC5 = chip;
            audio.useChip.add(Common.EnmChip.MMC5);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new IntFNesInst.N160();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.N160.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cN160 = chip;
            audio.useChip.add(Common.EnmChip.N163);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new IntFNesInst.VRC6();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.VRC6.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cVRC6 = chip;
            audio.useChip.add(Common.EnmChip.VRC6);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new IntFNesInst.VRC7();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.VRC7.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cVRC7 = chip;
            audio.useChip.add(Common.EnmChip.VRC7);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = new IntFNesInst.FME7();
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVolume("MAIN", IntFNesInst.FME7.class);
            lstChips.add(chip);
            ((Nsf) audio.driverVirtual).cFME7 = chip;
            audio.useChip.add(Common.EnmChip.FME7);

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegisterNSF(lstChips.toArray(new MDSound.Chip[0]));

            //Play

            audio.paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
