/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.musica.MuSICA;
import mdplayer.driver.musica.MusicaDriver;
import mdplayer.driver.musica.MusicaK4Driver;
import mdsound.MDSound;
import mdsound.instrument.MameAy8910Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MuSICA (MSX) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class MuSICAPlugin extends BasePlugin<MusicaDriver> {

    private static final Logger logger = getLogger(MuSICAPlugin.class.getName());

    @Override
    public void prepare() {
        if (playingFileName.toLowerCase().endsWith(".msd")) {

            String vcd = Path.changeExtension(playingFileName, ".vcd");
            byte[] vcdBuf = null;
            if (File.exists(vcd)) {
                vcdBuf = File.readAllBytes(vcd);
            }

            MusicaK4Driver driverVirtual = new MusicaK4Driver();
            driverVirtual.init(null, -1, -1);
            driverVirtual.compile(dataBuf, vcdBuf);

            dataBuf = driverVirtual.getBgmBin();
        }

        driverVirtual = new MusicaDriver(this);

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new MusicaDriver(this);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        int[] trkOffsets = new int[17];
        for (int t = 0; t < trkOffsets.length; t++) {
            trkOffsets[t] = (dataBuf[8 + t * 2] & 0xff) + (dataBuf[9 + t * 2] & 0xff) * 0x100;
        }
        boolean useAY = ((trkOffsets[9] + trkOffsets[10] + trkOffsets[11]) != 0);
        boolean useSCC = ((trkOffsets[12] + trkOffsets[13] + trkOffsets[14] + trkOffsets[15] + trkOffsets[16]) != 0);
        boolean useOPLL = ((trkOffsets[0] + trkOffsets[1] + trkOffsets[2] +
                trkOffsets[3] + trkOffsets[4] + trkOffsets[5] +
                trkOffsets[6] + trkOffsets[7] + trkOffsets[8]
        ) != 0);
logger.log(Level.INFO, "MuSICA: AY: %b, SCC: %b, OPLL: %b".formatted(useAY, useSCC, useOPLL));

        if (useAY) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.option = null;
            if (chip.instrument instanceof MameAy8910Inst) {
                chip.option = new Object[] {
                        (setting.getAY8910Type()[0].getYM2149mode() ? 0x10 : 0x00), // chip_type 0x10: YM2149, 0x00: AY
                        0x00  // chip_flag
                };
            }
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = MuSICA.baseClockAY8910 / 2;
            chipRegister.chip(Ay8910Chip.class).clock = MuSICA.baseClockAY8910;

            chipLED.put("PriAY10", 1);

            put(Ay8910Chip.class, chip);
        }

        if (useOPLL) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriOPLL", 1);
            chip.instrument = chipRegister.chip(Ym2413Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
            chip.clock = MuSICA.baseClockYM2413;
            chip.option = null;
            put(Ym2413Chip.class, chip);
            chipRegister.chip(Ym2413Chip.class).clock = MuSICA.baseClockYM2413;
        }

        if (useSCC) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriK051649", 1);
            chip.instrument = chipRegister.chip(K051649Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
            chip.clock = MuSICA.baseClockK051649;
            chip.option = null;
            put(K051649Chip.class, chip);
            chipRegister.chip(K051649Chip.class).clock = MuSICA.baseClockK051649;
        }

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        if (useOPLL) {
            chipRegister.chip(Ym2413Chip.class).write(0, 14, 32, EnmModel.VirtualModel);
        }

        driverVirtual.init(EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }
}
