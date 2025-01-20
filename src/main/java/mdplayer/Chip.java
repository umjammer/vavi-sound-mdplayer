/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;


import mdplayer.Common.EnmModel;


/**
 * Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public interface Chip {

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

    void init(ChipRegister context);

    void reset();

    void updateVol();

    default void clearFadeoutVolume() {}

    default void softReset(EnmModel model) {}
}
