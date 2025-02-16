/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.driver.sid.SidDriver;
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
    public void init(Audio context) {
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
