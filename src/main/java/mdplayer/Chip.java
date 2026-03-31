/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import mdplayer.Common.EnmModel;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;


/**
 * Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public interface Chip {

    abstract class Unused implements Chip {}

    /** */
    class ChipKeyInfo {

        public boolean[] on;

        public boolean[] off;

        public ChipKeyInfo(int n) {
            on = new boolean[n];
            off = new boolean[n];
            for (int i = 0; i < n; i++)
                off[i] = true;
        }
    }

    Setting setting = Setting.getInstance();

    // for ym chips
    byte[] algM = {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};

    /** implementation variants database */
    Class<? extends Instrument>[] implementations();

    /** which is active for {@link #implementations} */
    default int activeIndex(int chipId) { return 0; }

    /** */
    void init(BasePlugin context);

    /** */
    void reset();

    /** */
    void updateVol();

    /** */
    default void clearFadeout() {}

    /** */
    default void softReset(EnmModel model) {}

    /** */
    default Class<? extends Instrument> inst(int chipId) {
        return implementations()[activeIndex(chipId)];
    }

    /** */
    default Instrument instrument(int chipId) {
        return Instrument.getInstrument(inst(chipId));
    }
}
