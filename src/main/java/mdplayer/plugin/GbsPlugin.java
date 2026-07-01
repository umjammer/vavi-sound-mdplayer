/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.DmgChip;
import mdplayer.driver.gbs.Gbs;
import mdplayer.plugin.BasePlugin.HasSongNo;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * GBS (GameBoy) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-10 nsano initial version <br>
 */
public class GbsPlugin extends BasePlugin<Gbs> implements HasSongNo {

    private static final Logger logger = getLogger(GbsPlugin.class.getName());

    @Override
    public void setSongNo(int songNo) {
logger.log(Level.INFO, "songNo: " + songNo);
        this.songNo = songNo;
    }

    @Override
    public void prepare() {
        driverVirtual = new Gbs(this);

        driverReal = null;
//        if (setting.getoutputDevice().deviceType != Common.DEV_Null) {
//            driverReal = new Gbs(this);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(DmgChip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, DmgChip.class);
        chip.clock = 4194304;
        chip.option = null;
        put(DmgChip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        chipRegister.chip(DmgChip.class).write(0, 0x16, 0x8f, EnmModel.VirtualModel);
        chipRegister.chip(DmgChip.class).write(0, 0x14, 0x77, EnmModel.VirtualModel);
        chipRegister.chip(DmgChip.class).write(0, 0x15, 0xf7, EnmModel.VirtualModel);
        chipRegister.chip(DmgChip.class).write(0, 0x16, 0x8f, EnmModel.RealModel);
        chipRegister.chip(DmgChip.class).write(0, 0x14, 0x77, EnmModel.RealModel);
        chipRegister.chip(DmgChip.class).write(0, 0x15, 0xf7, EnmModel.RealModel);
//        chipRegister.chip(DmgChip.class).write(0, 0x16, 0x8f, EnmModel.PianoRollModel, 0);
//        chipRegister.chip(DmgChip.class).write(0, 0x14, 0x77, EnmModel.PianoRollModel, 0);
//        chipRegister.chip(DmgChip.class).write(0, 0x15, 0xf7, EnmModel.PianoRollModel, 0);

        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                songNo);
        if (driverReal != null) {
            driverReal.init(Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000,
                    songNo);
        }
    }
}
