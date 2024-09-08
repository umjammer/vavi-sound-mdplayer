package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.mgsdrv.MGSDRV;
import mdplayer.format.FileFormat;
import mdsound.instrument.Ay8910Inst;
import mdsound.instrument.K051649Inst;
import mdsound.MDSound;
import mdsound.instrument.Ym2413Inst;
import vavi.util.Debug;


/**
 * MGSPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MGSPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MGSDRV();
        audio.driverVirtual.setting = setting;
        ((MGSDRV) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MGSDRV();
            audio.driverReal.setting = setting;
            ((MGSDRV) audio.driverReal).setPlayingFileName(playingFileName);
        }
        boolean r = mgsPlay_mgsdrv();
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    private boolean mgsPlay_mgsdrv() {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            int i = 0;
            while (vgmBuf.length > 1 && i < vgmBuf.length - 1 && (vgmBuf[i] != 0x1a || vgmBuf[i + 1] != 0x00)) {
                i++;
            }
            i += 7;
            int[] trkOffsets = new int[18];
            for (int t = 0; t < trkOffsets.length; t++) {
                trkOffsets[t] = vgmBuf[i + t * 2] + vgmBuf[i + t * 2 + 1] * 0x100;
            }
            boolean useAY = (trkOffsets[0] + trkOffsets[1] + trkOffsets[2] != 0);
            boolean useSCC = (trkOffsets[3] + trkOffsets[4] + trkOffsets[5] + trkOffsets[6] + trkOffsets[7] != 0);
            boolean useOPLL = (trkOffsets[8] + trkOffsets[9] + trkOffsets[10]
                    + trkOffsets[11] + trkOffsets[12] + trkOffsets[13]
                    + trkOffsets[14] + trkOffsets[15] + trkOffsets[16]
                    + trkOffsets[17]
                    != 0);

            audio.chipRegister.resetChips();
            resetFadeOutParam();
            audio.useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.masterVolume = setting.getBalance().getMasterVolume();

            if (useAY) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriAY10", 1);
                chip.instrument = new Ay8910Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ay8910Inst.class);
                chip.clock = MGSDRV.baseclockAY8910 / 2;
                chip.option = null;
                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.AY8910);
                audio.clockAY8910 = MGSDRV.baseclockAY8910;
            }

            if (useOPLL) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriOPLL", 1);
                chip.instrument = new Ym2413Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", Ym2413Inst.class);
                chip.clock = MGSDRV.baseclockYM2413;
                chip.option = null;
                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.YM2413);
                audio.clockYM2413 = MGSDRV.baseclockYM2413;
            }

            if (useSCC) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriK051649", 1);
                chip.instrument = new K051649Inst();
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", K051649Inst.class);
                chip.clock = MGSDRV.baseclockK051649;
                chip.option = null;
                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.K051649);
                audio.clockK051649 = MGSDRV.baseclockK051649;
            }

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.AY8910, Common.EnmChip.YM2413, Common.EnmChip.K051649}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.AY8910}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            audio.paused = false;
            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
