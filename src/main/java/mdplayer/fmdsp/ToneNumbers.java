/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.HashMap;
import java.util.Map;


/**
 * Numbers the voices an FM chip is played with, for the "TN:" column of the fmdsp row.
 * <p>
 * A driver keeps a table of voices and tells the display which one a part is on; a chip has no
 * such number - a voice reaches it as the operator registers themselves, and once written there is
 * nothing left to say which entry of which table they came from. What can still be told apart is
 * the voices from each other, so that is what this does: it fingerprints the registers that make a
 * voice and hands out a number per distinct one, in the order the song first plays them. The
 * numbers are not the driver's and will not match its MML, but they behave the way the column is
 * read - rows on the same voice show the same number, and a voice change shows as a change.
 * <p>
 * One per reader, cleared with the song, since the numbering only means anything within one.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-25 nsano initial version <br>
 */
final class ToneNumbers {

    /** as many as the three digit column can show */
    private static final int limit = 1000;

    private final Map<Long, Integer> numbers = new HashMap<>();

    public void reset() {
        numbers.clear();
    }

    /**
     * The number of the voice {@code fingerprint} stands for, counting from 0 as a driver's own
     * table would. Past {@link #limit} voices - which no song has, and only a stream of noise
     * would reach - the numbering wraps rather than growing a map for the rest of the song.
     */
    public int numberOf(long fingerprint) {
        Integer number = numbers.get(fingerprint);
        if (number != null) return number;
        int next = numbers.size() % limit;
        numbers.put(fingerprint, next);
        return next;
    }

    // FNV-1a, which is enough to keep a few dozen voices apart and folds a register at a time

    public static long seed() {
        return 0xcbf2_9ce4_8422_2325L;
    }

    public static long fold(long fingerprint, int value) {
        return (fingerprint ^ (value & 0xff)) * 0x0000_0100_0000_01b3L;
    }
}
