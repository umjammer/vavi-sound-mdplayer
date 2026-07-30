/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Provides the chip-level detail of a track row, which {@link RightMode#TRACK_INFO} draws in place
 * of the spectrum and level meters.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-25 nsano initial version <br>
 */
public interface TrackDetailSource {

    /**
     * Fills {@code out} with what {@code track} is doing inside its chip. The instance is reused
     * across calls and {@linkplain TrackDetail#clear cleared} before each one.
     *
     * @return whether the row has any detail to show; false leaves it blank, as the original does
     *         for a track type it has no panel for
     */
    boolean readDetail(TrackId track, TrackDetail out);
}
