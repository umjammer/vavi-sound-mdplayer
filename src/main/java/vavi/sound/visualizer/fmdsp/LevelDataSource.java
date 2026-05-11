/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Provides per-channel sample-peak values for the level meter strip.
 * <p>
 * Channel layout matches the C visualizer:
 * <pre>
 *   0..5  : FM 1..6
 *   6..8  : SSG 1..3
 *   9     : Drum (max of 6 drum voices)
 *   10    : ADPCM
 *   11..18: PPZ8 1..8
 * </pre>
 */
public interface LevelDataSource {

    /** Number of metered channels. Mirrors {@code FMDSP_LEVEL_COUNT}. */
    int COUNT = 19;

    /** Pan position. */
    enum Pan {
        /** Hard left. */
        LEFT,
        /** Mid-left. */
        MID_LEFT,
        /** Center. */
        CENTER,
        /** Mid-right. */
        MID_RIGHT,
        /** Hard right. */
        RIGHT,
        /** No pan / muted. */
        NONE,
    }

    /**
     * Sample-peak amplitude for the given channel, in {@code 0..32767}.
     * The renderer converts this to dBFS via {@code 20*log10(x/32768)} and
     * paints a 0..32 bar (1 step per 1.5 dB, 32 steps = 0 dBFS).
     */
    int level(int channel);

    /** Pan position to display. */
    Pan pan(int channel);
}
