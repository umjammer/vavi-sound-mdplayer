/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Function;

import dotnet4j.io.Path;
import dotnet4j.io.Stream;
import dotnet4j.util.compat.StringUtilities;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.fmp.FMP;
import mdplayer.driver.fmp.nise98.FileTemp;
import mdplayer.driver.pmd.PMDJava;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.instrument.Ym2608Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * FMP (PC-9801) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class FMPPlugin extends BasePlugin {

    private static final Logger logger = getLogger(FMPPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        FileTemp ft = new FileTemp();
        String ext = playingFileName.substring(playingFileName.lastIndexOf('.' + 1));
        if (!StringUtilities.isNullOrEmpty(ext)) {
            ext = ext.toLowerCase();
            if (ext.length() > 3 && ext.charAt(1) == 'm') {
                //compile
                if (!(new FMP(ft).Compile(playingFileName))) return false;
                playingFileName = Path.changeExtension(
                        playingFileName,
                        ext.equals(".mpi") ? ".opi" : (ext.equals(".mvi") ? ".ovi" : ".ozi"));
                vgmBuf = ft.ReadTemp(playingFileName);
                //vgmBuf = File.ReadAllBytes(PlayingFileName);
            }
        }

        audio.driverVirtual = new FMP(ft);
//        ((FMP)audio.driverVirtual).playingFileName = playingFileName;
//        ((FMP)audio.driverVirtual).playingArcFileName = playingArcFileName;
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
//            audio.driverReal = new FMP(ft);
//            ((FMP)audio.driverReal).PlayingFileName = playingFileName;
//            ((FMP)audio.driverReal).PlayingArcFileName = playingArcFileName;
//        }

        prepare();
        boolean r = play(ft);
        if (!r) {
            logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    private boolean play(FileTemp ft) {

        startTrdVgmReal();

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ym2608Chip.class).instrument(0);
        chip.samplingRate = 55467; // setting.outputDevice.SampleRate;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        if (chip.instrument instanceof Ym2608Inst ym2608) {
            chip.setVolumes.put("FM", ym2608::setVolume);
            chip.setVolumes.put("SSG", ym2608::setVolume);
            chip.setVolumes.put("RHYTHM", ym2608::setVolume);
            chip.setVolumes.put("ADPCM", ym2608::setVolume);
        }
        chip.clock = FMP.baseclock;
        Function<String, Stream> fn = Common::getOPNARyhthmStream;
        chip.option = new Object[] {fn};
        audio.chipLED.put("PriOPNA", 1);
        put(Ym2608Chip.class, chip);
        audio.chipRegister.chip(Ym2608Chip.class).clock = FMP.baseclock;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ppz8Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ppz8Chip.class);
        chip.clock = FMP.baseclock;
        chip.option = null;
        audio.chipLED.put("PriPPZ8", 1);
        put(Ppz8Chip.class, chip);

        if (hiyorimiNecessary) hiyorimiNecessary = true;
        else hiyorimiNecessary = false;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.mds.setVolume(MAIN_TAG, audio.chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        audio.mds.setVolume("FM", audio.chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("FM", Ym2608Chip.class));
        audio.mds.setVolume("SSG", audio.chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        audio.mds.setVolume("RHYTHM", audio.chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        audio.mds.setVolume("ADPCM", audio.chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // reset PSG TONE
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.RealModel);

        audio.chipRegister.chip(Ym2608Chip.class).writeClock(0, PMDJava.baseclock, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).writeClock(1, PMDJava.baseclock, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        ((FMP) audio.driverVirtual).SetSearchPath(setting.getFileSearchPathList());
        if (audio.driverReal != null) {
            ((FMP) audio.driverReal).SetSearchPath(setting.getFileSearchPathList());
        }

        if (!audio.driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                new Class[] { Ym2608Chip.class },
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    new Class[] {Ym2608Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
            audio.chipRegister.plugin(RealChipPlugin.class).realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
        }

        oneTimeReset = false;

        return true;
    }
}
