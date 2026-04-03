/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.sid.SidDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;


/**
 * SidChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class SidChip implements Chip {

    public SidDriver sid;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[0];
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public Integer[] read(int chipId) {
        if (sid == null)
            return null;
        return sid.getRegisterFromSid()[chipId];
    }

    public void setDriver(SidDriver driver) {
        this.sid = driver;
    }
}
