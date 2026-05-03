/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Collections;
import java.util.Map;

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

    public Map<String, Object> getInfo(int chipId) {
        return sid != null ? Map.of("register",  sid.getRegisterFromSid()[chipId]) : Collections.emptyMap();
    }

    public void setDriver(SidDriver driver) {
        this.sid = driver;
    }
}
