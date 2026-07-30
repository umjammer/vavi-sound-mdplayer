/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Layout selector for the right half of the display.
 * <p>
 * The C original has a third, {@code FMDSP_RIGHT_MODE_PPZ8}, which puts the eight PPZ8 rows there
 * as a second track block so that they can be watched beside another {@link LeftMode}. It is left
 * out rather than left dead: the row renderer has the left half's coordinates built into it, and a
 * mode that the cycle steps through without the display changing reads as a broken key.
 */
public enum RightMode {
    /** The logo, the counters, the spectrum analyzer, the level meters and the comments. */
    DEFAULT,
    /**
     * What each row the left half shows is doing inside its chip, from {@link TrackDetailSource},
     * in place of all of that.
     */
    TRACK_INFO,
}
