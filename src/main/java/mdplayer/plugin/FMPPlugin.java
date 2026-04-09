/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.lang.System.Logger;
import java.util.function.Function;

import dotnet4j.io.Path;
import dotnet4j.io.Stream;
import dotnet4j.util.compat.StringUtilities;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.fmp.FMP;
import mdplayer.driver.fmp.FmpDriver;
import mdplayer.driver.pmd.PmdDriver;
import mdplayer.emu.nise98.FileTemp;
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
public class FMPPlugin extends BasePlugin<FmpDriver> {

    private static final Logger logger = getLogger(FMPPlugin.class.getName());

    @Override
    public void prepare() {
        FileTemp ft = new FileTemp();
        String ext = playingFileName.substring(playingFileName.lastIndexOf('.') + 1);
        if (!StringUtilities.isNullOrEmpty(ext)) {
            ext = ext.toLowerCase();
            if (ext.length() > 3 && ext.charAt(1) == 'm') {
                //compile
                FmpDriver fmp = new FmpDriver();
                fmp.setFileTemp(ft);
                fmp.setPlayingFileName(playingFileName);
                fmp.compile();
                playingFileName = Path.changeExtension(
                        playingFileName,
                        ext.equals(".mpi") ? ".opi" : (ext.equals(".mvi") ? ".ovi" : ".ozi"));
                vgmBuf = ft.readTemp(playingFileName);
                //dataBuf = File.readAllBytes(PlayingFileName);
            }
        }

        driverVirtual = new FmpDriver();
        driverVirtual.setFileTemp(ft);
        driverVirtual.setPlayingFileName(playingFileName);
        driverVirtual.setPlayingArcFileName(playingArcFileName);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
//            driverReal = new FmpDriver();
//            driverReal.setFileTemp(ft);
//            driverReal.setPlayingFileName(playingFileName);
//            driverReal.setPlayingArcFileName(playingArcFileName);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Ym2608Chip.class).instrument(0);
        chip.samplingRate = 55467; // setting.outputDevice.SampleRate;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        if (chip.instrument instanceof Ym2608Inst ym2608) {
            chip.setVolumes.put("FM", ym2608::setVolume);
            chip.setVolumes.put("SSG", ym2608::setVolume);
            chip.setVolumes.put("RHYTHM", ym2608::setVolume);
            chip.setVolumes.put("ADPCM", ym2608::setVolume);
        }
        chip.clock = FMP.baseClock;
        Function<String, Stream> fn = Common::getOPNARyhthmStream;
        chip.option = new Object[] {fn};
        chipLED.put("PriOPNA", 1);
        put(Ym2608Chip.class, chip);
        chipRegister.chip(Ym2608Chip.class).clock = FMP.baseClock;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Ppz8Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ppz8Chip.class);
        chip.clock = FMP.baseClock;
        chip.option = null;
        chipLED.put("PriPPZ8", 1);
        put(Ppz8Chip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        mds.setVolume(MAIN_TAG, chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        mds.setVolume("FM", chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("FM", Ym2608Chip.class));
        mds.setVolume("SSG", chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        mds.setVolume("RHYTHM", chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        mds.setVolume("ADPCM", chipRegister.chip(Ym2608Chip.class).inst(0), setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // reset PSG TONE
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.RealModel);

        chipRegister.chip(Ym2608Chip.class).writeClock(0, PmdDriver.baseClock, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).writeClock(1, PmdDriver.baseClock, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        driverVirtual.setSearchPath(setting.getFileSearchPathList());
        if (driverReal != null) {
            driverReal.setSearchPath(setting.getFileSearchPathList());
        }

        driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

        if (driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
            chipRegister.plugin(RealChipPlugin.class).realChip.waitOpnAdpcmData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
        }

        oneTimeReset = false;
    }
}
