/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Auxiliary track-info badge. Mirrors the C enum {@code fmdriver_track_info}.
 */
public enum TrackInfo {
    NORMAL,
    SSG,
    FM3EX,
    PPZ8,
    PDZF,
    SSGEFF,
    /**
     * Not in the C enum: a row playing one continuous sample rather than notes, as an arcade
     * board's streamed part does. Its key and note-length meters are blank because there is no
     * note to show, and this says so rather than leaving the row looking silent.
     */
    STREAM,
}
