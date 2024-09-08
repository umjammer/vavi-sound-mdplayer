package mdplayer.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipLEDs;
import mdplayer.Common;
import mdplayer.driver.moonDriver.MoonDriverDotNET;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.YmF278bInst;
import vavi.util.Debug;


/**
 * MDLPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MDLPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MoonDriverDotNET();
        audio.driverVirtual.setting = setting;
        ((MoonDriverDotNET) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
            audio.driverReal = new MoonDriverDotNET();
            audio.driverReal.setting = setting;
            ((MoonDriverDotNET) audio.driverReal).setPlayingFileName(playingFileName);
        }

        boolean r = mdlPlay_moonDriverDotNET(MoonDriverDotNET.MoonDriverFileType.MDL);
        if (!r) {
Debug.println(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    boolean mdlPlay_moonDriverDotNET(MoonDriverDotNET.MoonDriverFileType fileType) {

        try {

            if (vgmBuf == null || setting == null) return false;

            if (fileType == MoonDriverDotNET.MoonDriverFileType.MDL) {
                vgmBuf = ((MoonDriverDotNET) audio.driverVirtual).Compile(vgmBuf);
            }
            Common.EnmChip[] useChipFromMdr = new Common.EnmChip[] {Common.EnmChip.YMF278B};

            //stop();
            audio.chipRegister.resetChips();
            resetFadeOutParam();
            audio.useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            audio.hiyorimiNecessary = setting.getHiyorimiMode();

            audio.chipLED = new ChipLEDs();
            audio.masterVolume = setting.getBalance().getMasterVolume();

            YmF278bInst ymf278b = new YmF278bInst();
            //Func<String, Stream> fn = Common.GetOPNARyhthmStream;

            if (useChipFromMdr[0] != Common.EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                audio.chipLED.put("PriOPL4", 1);
                chip.instrument = ymf278b;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getVolume("MAIN", YmF278bInst.class);
                chip.clock = 33868800;
                chip.option = null; // new Object[] { fn };
                lstChips.add(chip);
                audio.useChip.add(Common.EnmChip.YMF278B);
                //clockYM2608 = MucomDotNET.opnaBaseClock;
            }

            if (audio.mds == null)
                audio.mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                audio.mds.init(setting.getOutputDevice().getSampleRate(), audio.SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            audio.chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            //SetYM2608Volume(true, setting.getbalance().getYM2608Volume);
            //SetYM2608FMVolume(true, setting.getbalance().getYM2608FMVolume);
            //SetYM2608PSGVolume(true, setting.getbalance().getYM2608PSGVolume);
            //SetYM2608RhythmVolume(true, setting.getbalance().getYM2608RhythmVolume);
            //SetYM2608AdpcmVolume(true, setting.getbalance().getYM2608AdpcmVolume);

            //chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
            //chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
            //chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.RealModel);

            //chipRegister.writeYM2608Clock(0, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
            //chipRegister.writeYM2608Clock(1, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, EnmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, EnmModel.RealModel);


            if (!audio.driverVirtual.init(vgmBuf, audio.chipRegister, Common.EnmModel.VirtualModel, new Common.EnmChip[] {Common.EnmChip.YMF278B}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (audio.driverReal != null) {
                if (!audio.driverReal.init(vgmBuf, audio.chipRegister, Common.EnmModel.RealModel, new Common.EnmChip[] {Common.EnmChip.YMF278B}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            audio.paused = false;

            //if (driverReal != null && setting.getYMF278BType()[0].getUseReal()[0]) {
            //    realChip.WaitOPL4PCMData(setting.getYMF278BType()[0].getrealChipInfo()[0].getSoundLocation() == -1);
            //}

            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
