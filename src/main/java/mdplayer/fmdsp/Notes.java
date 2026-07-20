/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;


/**
 * Note math shared by the {@link FmDspChipReader}s.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public final class Notes {

    private Notes() {
    }

    /** C0 [Hz], the origin of the semitone scale the MML key is expressed in */
    private static final double c0 = 440.0 / Math.pow(2, 57 / 12.0);

    /** semitones above C0 of a frequency, -1 when silent */
    public static int noteOf(double freq) {
        if (freq <= 0) return -1;
        return (int) Math.round(12 * (Math.log(freq / c0) / Math.log(2)));
    }

    /**
     * Semitones above C0 of a PCM playback rate ratio, ratio 1.0 being o4 c - the convention the
     * chip panels use ({@code SegaPcmChip#searchSegaPCMNote}). -1 when silent.
     */
    public static int noteOfRatio(double ratio) {
        if (ratio <= 0) return -1;
        return 48 + (int) Math.round(12 * (Math.log(ratio) / Math.log(2)));
    }
}
