/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.sid;

import java.util.Map;

import musicDriverInterface.MetaData;


/**
 * SidDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public interface SidDriver {

    Integer[][] getRegisterFromSid();

    Map<String, Object> getInfo();

    MetaData getMetaData(byte[] buf, Object... args);

    int getSongs();
}
