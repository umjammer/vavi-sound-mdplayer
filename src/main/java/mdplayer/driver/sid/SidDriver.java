/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.sid;

import java.util.Map;

import mdplayer.driver.Vgm;


/**
 * SidDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public interface SidDriver {

    Integer[][] getRegisterFromSid();

    Map<String, Object> getInfo();

    void setSong(int songNo);

    Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3);

    int getSongs();
}
