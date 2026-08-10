/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf;

import mdplayer.Common;
import mdplayer.driver.BasePlugin;


/**
 * PSF1 (Sony PlayStation) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class PsfPlugin extends BasePlugin<PsfDriver> {

    @Override
    public void prepare() {
        driverVirtual = new PsfDriver(this);

        driverReal = null;

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        // the emulated SPU renders the audio itself, so no mdsound chip is registered

        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
    }
}
