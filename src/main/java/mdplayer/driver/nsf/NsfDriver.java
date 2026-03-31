/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import mdplayer.driver.Vgm.Gd3;
import mdsound.MDSound.Chip;


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

    void setApu(Chip chip);

    void setDmc(Chip chip);

    void setFds(Chip chip);

    void setMmc5(Chip chip);

    void setN160(Chip chip);

    void setVrc6(Chip chip);

    void setVrc7(Chip chip);

    void setFme7(Chip chip);

    Gd3 getGD3Info(byte[] buf, int[] vgmGd3);

    int getSongs();
}
