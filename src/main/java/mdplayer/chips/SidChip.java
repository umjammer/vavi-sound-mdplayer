/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.driver.sid.SidDriver;
import mdsound.Instrument;


/**
 * SidChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class SidChip extends BaseChip {

    public SidDriver sid;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[0];
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
