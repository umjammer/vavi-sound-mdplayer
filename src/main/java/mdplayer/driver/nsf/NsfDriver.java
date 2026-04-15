/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import musicDriverInterface.MetaData;


/**
 * NsfDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public interface NsfDriver {

    void setSong(int songNo);

    boolean useFds();

    boolean useFme7();

    boolean useMmc5();

    boolean useN106();

    boolean useVrc6();

    boolean useVrc7();

    MetaData getMetaData(byte[] buf, Object... args);

    int getSongs();
}
