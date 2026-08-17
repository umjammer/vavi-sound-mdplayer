/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp7;

import mdplayer.Common;
import mdplayer.driver.BasePlugin;


/**
 * FMP7 (".owi") Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 */
public class Fmp7Plugin extends BasePlugin<Fmp7Driver> {

    @Override
    public void prepare() {
        driverVirtual = new Fmp7Driver(this);

        driverReal = null;

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        // FMP7 renders the audio itself, so no mdsound chip is registered

        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
    }

    /**
     * The emulated machine is a JVM-wide singleton and it holds a thread and a scratch directory,
     * so it has to go when the song does - the next song cannot start one until it has.
     */
    @Override
    public void stop() {
        if (driverVirtual != null) {
            driverVirtual.stopPlayer();
        }
        super.stop();
    }
}
