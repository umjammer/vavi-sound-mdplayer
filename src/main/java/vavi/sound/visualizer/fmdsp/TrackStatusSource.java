/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Provides per-track status snapshots for the row strips and on-screen
 * keyboard.
 */
public interface TrackStatusSource {

    /**
     * Fill {@code out} with the current snapshot for {@code track}. The
     * passed-in instance is reused across calls to avoid allocation; the
     * implementation must overwrite every field.
     */
    void readStatus(TrackId track, TrackStatus out);

    /** True if the track's audio output is muted (and should grey out). */
    default boolean masked(TrackId track) {
        return false;
    }
}
