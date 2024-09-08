package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import dotnet4j.io.Stream;
import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.pmd.PMDDotNET;
import mdplayer.format.FileFormat;
import mdplayer.format.MMLFileFormat;
import mdsound.MDSound;
import mdsound.instrument.PpsDrvInst;
import mdsound.instrument.Ppz8Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.P86Inst;
import vavi.util.Debug;


/**
 * PMDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class PMDPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new PMDDotNET();
        audio.driverVirtual.setting = setting;
        ((PMDDotNET) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
            audio.driverReal = new PMDDotNET();
            audio.driverReal.setting = setting;
            ((PMDDotNET) audio.driverReal).setPlayingFileName(playingFileName);
        }
        boolean r = mmlPlay_PMDDotNET(format instanceof MMLFileFormat ? 0 : 1);
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mmlPlay_PMDDotNET(int fileType) {

        try {
            if (vgmBuf == null || setting == null) return false;

            //Stop();

            audio.chipRegister.resetChips();
            resetFadeOutParam();
            audio.useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.masterVolume = setting.getBalance().getMasterVolume();

            Ym2608Inst ym2608 = new Ym2608Inst();
            chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPNA", 1);
            chip.instrument = ym2608;
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
            chip.clock = PMDDotNET.baseclock;
            chip.setVolumes.put("FM", ym2608::setFMVolume);
            chip.setVolumes.put("PSG", ym2608::setPSGVolume);
            chip.setVolumes.put("Rhythm", ym2608::setRhythmVolume);
            chip.setVolumes.put("Adpcm", ym2608::setAdpcmVolume);
            Function<String, Stream> fn = Common::getOPNARyhthmStream;
            chip.option = new Object[] {fn};
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.YM2608);
            audio.clockYM2608 = PMDDotNET.baseclock;

            Ppz8Inst ppz8 = new Ppz8Inst();
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = ppz8;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume("MAIN", Ppz8Inst.class);
            chip.clock = PMDDotNET.baseclock;
            chip.option = null;
            audio.chipLED.put("PriPPZ8", 1);
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.PPZ8);


            PpsDrvInst ppsdrv = new PpsDrvInst();
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = ppsdrv;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = 0;
            chip.clock = PMDDotNET.baseclock;
            chip.option = null;
            audio.chipLED.put("PriPPSDRV", 1);
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.PPSDRV);


            P86Inst P86 = new P86Inst();
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.instrument = P86;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = 0;
            chip.clock = PMDDotNET.baseclock;
            chip.option = null;
            audio.chipLED.put("PriP86", 1);
            lstChips.add(chip);
            audio.useChip.add(Common.EnmChip.P86);

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            audio.setVolume("MAIN", Ym2608Inst.class, true, setting.getBalance().getVolume("MAIN", Ym2608Inst.class));
            audio.setVolume("FM", Ym2608Inst.class, true, setting.getBalance().getVolume("FM", Ym2608Inst.class));
            audio.setVolume("PSG", Ym2608Inst.class, true, setting.getBalance().getVolume("PSG", Ym2608Inst.class));
            audio.setVolume("Rhythm", Ym2608Inst.class, true, setting.getBalance().getVolume("Rhythm", Ym2608Inst.class));
            audio.setVolume("Adpcm", Ym2608Inst.class, true, setting.getBalance().getVolume("Adpcm", Ym2608Inst.class));

            audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
            audio.chipRegister.setYM2608Register(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Psg TONE でリセット
            audio.chipRegister.setYM2608Register(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);

            audio.chipRegister.writeYm2608Clock((byte) 0, PMDDotNET.baseclock, Common.EnmModel.RealModel);
            audio.chipRegister.writeYm2608Clock((byte) 1, PMDDotNET.baseclock, Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
            audio.chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);


            if (!audio.driverVirtual.init(vgmBuf, fileType, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, fileType, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            // Play

            audio.paused = false;

            if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//                SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
            }

            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
