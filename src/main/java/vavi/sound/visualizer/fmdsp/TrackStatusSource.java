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

    /**
     * Display name of the track row's chip section, up to 5 characters (e.g. {@code "OPM"},
     * {@code "PCM"}). Null keeps the built-in PC-98 label of the row.
     */
    default String trackTypeName(TrackId track) {
        return null;
    }

    /**
     * Display number of the track row, 1-based. Negative keeps the built-in numbering.
     */
    default int trackNumber(TrackId track) {
        return -1;
    }

    /**
     * The rows {@link LeftMode#AUTO} shows, at most 10, typically the channels the current song
     * actually uses. Null or empty falls back to the default layout.
     */
    default TrackId[] displayTracks() {
        return null;
    }
}
