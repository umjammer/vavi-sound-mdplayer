/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/** Layout selector for the left half of the display. */
public enum LeftMode {
    /**
     * The rows the data source reports in use ({@link TrackStatusSource#displayTracks()});
     * falls back to {@link #OPNA} while the source names none.
     */
    AUTO,
    /** FM1, 2, 3, 4, 5, 6, SSG1, 2, 3, PCM. */
    OPNA,
    /** FM1, 2, 3, FMEX1, 2, 3, SSG1, 2, 3, PCM. */
    OPN,
    /** FM1, 2, 3, FMEX1..3, FM4, 5, 6, SSG1..3, PCM. */
    THIRTEEN,
    /** PPZ8 1..8, PCM. */
    PPZ8,
}
