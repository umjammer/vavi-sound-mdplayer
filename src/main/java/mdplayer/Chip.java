/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;


/**
 * Chip. {@link Instrument} and it's variant abstraction.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public interface Chip {

    abstract class Unused implements Chip {}

    @Deprecated
    class ChipKeyInfo {

        public final boolean[] on;

        public final boolean[] off;

        public ChipKeyInfo(int n) {
            on = new boolean[n];
            off = new boolean[n];
            for (int i = 0; i < n; i++)
                off[i] = true;
        }
    }

    /** implementation variants database */
    Class<? extends Instrument>[] implementations();

    /** which is active for {@link #implementations} */
    default int activeIndex(int chipId) { return 0; }

    /** */
    void init(BasePlugin<? extends BaseDriver> context);

    /** */
    void reset();

    /** */
    void updateVol();

    /** */
    default void clearFadeout() {}

    /** */
    default void softReset(EnmModel model) {}

    /** get active instruction for chipId */
    default Class<? extends Instrument> inst(int chipId) {
        return implementations()[activeIndex(chipId)];
    }

    /** instantiate an instrument */
    default Instrument instrument(int chipId) {
        return Instrument.getInstrument(inst(chipId));
    }

    default void setFadeout(int chipId, int v) {
    }
}
