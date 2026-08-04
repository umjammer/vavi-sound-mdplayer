/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.function.IntPredicate;


/**
 * Hands a reader's rows to the channels that sound, in the order they first do, and keeps them
 * there for the rest of the song.
 * <p>
 * The second chip of a doubled pair gets whatever rows the first one leaves - three of the nine FM
 * rows, where a six channel chip is in front of it - so a row per channel from the top would show
 * whichever channels the song happens to have numbered lowest, and leave the parts that play
 * invisible. This is the same rule {@link SegaPcmReader} uses for sixteen voices over nine rows.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-04 nsano initial version <br>
 */
final class RowSlots {

    /** the channel shown on each row, -1 until one claims it */
    private final int[] channels;

    private int mapped;

    RowSlots(int slots) {
        channels = new int[slots];
        Arrays.fill(channels, -1);
    }

    void reset() {
        Arrays.fill(channels, -1);
        mapped = 0;
    }

    /**
     * Gives a row to every channel that has begun to sound and has none yet. Cheap and idempotent:
     * a reader calls it as it resolves each row rather than keeping a poll of its own.
     */
    void claim(int channelCount, IntPredicate sounding) {
        for (int ch = 0; ch < channelCount && mapped < channels.length; ch++) {
            if (!sounding.test(ch) || indexOf(ch) >= 0) continue;
            channels[mapped++] = ch;
        }
    }

    /** the channel a row shows, or -1 while nothing has claimed it */
    int channelOf(int slot) {
        return slot >= 0 && slot < channels.length ? channels[slot] : -1;
    }

    private int indexOf(int ch) {
        for (int slot = 0; slot < mapped; slot++) {
            if (channels[slot] == ch) return slot;
        }
        return -1;
    }
}
