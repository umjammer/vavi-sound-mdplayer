/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.hvl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.BasePlugin.HasSongNo;

import static java.lang.System.getLogger;


/**
 * HVL (HivelyTracker, Amiga) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-03 nsano initial version <br>
 */
public class HvlPlugin extends BasePlugin<HvlDriver> implements HasSongNo {

    private static final Logger logger = getLogger(HvlPlugin.class.getName());

    @Override
    public void setSongNo(int songNo) {
logger.log(Level.INFO, "songNo: " + songNo);
        this.songNo = songNo;
    }

    @Override
    public void prepare() {
        driverVirtual = new HvlDriver(this);

        driverReal = null;

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        // hvl uses its own renderer, no emulated chips are used

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
