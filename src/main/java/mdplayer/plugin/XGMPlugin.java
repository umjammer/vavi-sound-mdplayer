package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.driver.Xgm;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.MameYm2612Inst;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;
import mdsound.chips.Ym3438Const;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * XGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class XGMPlugin extends BasePlugin {

    private static final Logger logger = getLogger(XGMPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Xgm();
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new Xgm();
        }

        boolean r = xgmPlay();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean xgmPlay() {

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

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED.clear();

            audio.masterVolume = setting.getBalance().getMasterVolume();

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.option = null;
            Ym2612Inst ym2612;
            Ym3438Inst ym3438;
            MameYm2612Inst ym2612mame;

            if (setting.getYM2612Type()[0].getUseEmu()[0]) {
                ym2612 = Instrument.getInstrument(Ym2612Inst.class);
                chip.instrument = ym2612;
                chip.option = new Object[] {
                        (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00)
                                | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                };
            } else if (setting.getYM2612Type()[0].getUseEmu()[1]) {
                ym3438 = Instrument.getInstrument(Ym3438Inst.class);
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
                ym2612mame = Instrument.getInstrument(MameYm2612Inst.class);
                chip.instrument = ym2612mame;
            }

            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2612Inst.class);
            chip.clock = 7670454;
//            audio.clockYM2612 = 7670454;
            audio.chipLED.put("PriOPN2", 1);
            lstChips.add(chip);
            useChip.add(Common.EnmChip.YM2612);

            Sn76489Inst sn76489 = Instrument.getInstrument(Sn76489Inst.class);
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = sn76489;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Inst.class);
            chip.clock = 3579545;
            chip.option = null;
            audio.chipLED.put("PriDCSG", 1);
            lstChips.add(chip);
            useChip.add(Common.EnmChip.SN76489);

            audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, lstChips);

            audio.setVolume(MAIN_TAG, Ym2612Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2612Inst.class));
            audio.setVolume(MAIN_TAG, Sn76489Inst.class, true, setting.getBalance().getVolume(MAIN_TAG, Sn76489Inst.class));
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2612, Common.EnmChip.SN76489}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2612, Common.EnmChip.SN76489}
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }
}
