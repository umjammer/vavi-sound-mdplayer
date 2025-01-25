/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.driver.sid.Sid;


/**
 * SidChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class SidChip implements Chip {

    public Sid SID;

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
        if (SID == null)
            return null;
        return SID.GetRegisterFromSid()[chipId];
    }
}
