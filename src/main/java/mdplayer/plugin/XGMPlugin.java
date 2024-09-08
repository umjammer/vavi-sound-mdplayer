package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.Xgm;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.MameYm2612Inst;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;
import mdsound.chips.Ym3438Const;
import vavi.util.Debug;


/**
 * XGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class XGMPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Xgm();
        audio.driverVirtual.setting = setting;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new Xgm();
            audio.driverReal.setting = setting;
        }

        boolean r = xgmPlay();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean xgmPlay() {

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

            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.option = null;
            Ym2612Inst ym2612;
            Ym3438Inst ym3438;
            MameYm2612Inst ym2612mame;

            if (setting.getYM2612Type()[0].getUseEmu()[0]) {
                ym2612 = new Ym2612Inst();
                chip.instrument = ym2612;
                chip.option = new Object[] {
                        (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00)
                                | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                };
            } else if (setting.getYM2612Type()[0].getUseEmu()[1]) {
                ym3438 = new Ym3438Inst();
                chip.instrument = ym3438;
                switch (setting.getNukedOPN2().emuType) {
                case 0:
                    ym3438.setChipType(Ym3438Const.Type.discrete);
                    break;
                case 1:
                    ym3438.setChipType(Ym3438Const.Type.asic);
                    break;
                case 2:
                    ym3438.setChipType(Ym3438Const.Type.ym2612);
                    break;
                case 3:
                    ym3438.setChipType(Ym3438Const.Type.ym2612_u);
                    break;
                case 4:
                    ym3438.setChipType(Ym3438Const.Type.asic_lp);
                    break;
                }
            } else if (setting.getYM2612Type()[0].getUseEmu()[2]) {
                ym2612mame = new MameYm2612Inst();
                chip.instrument = ym2612mame;
            }

            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume("MAIN", Ym2612Inst.class);
            chip.clock = 7670454;
            audio.clockYM2612 = 7670454;
            audio.chipLED.put("PriOPN2", 1);
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.YM2612);

            Sn76489Inst sn76489 = new Sn76489Inst();
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = sn76489;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume("MAIN", Sn76489Inst.class);
            chip.clock = 3579545;
            chip.option = null;
            audio.chipLED.put("PriDCSG", 1);
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.SN76489);

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            audio.setVolume("MAIN", Ym2612Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2612Inst.class));
            audio.setVolume("MAIN", Sn76489Inst.class, true, setting.getBalance().getVolume("MAIN", Sn76489Inst.class));
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2612, Common.EnmChip.SN76489}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2612, Common.EnmChip.SN76489}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }
            // Play

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
