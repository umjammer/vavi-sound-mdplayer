/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Aggregator passed to {@link FmDspVisualizer}. Each sub-source can be
 * supplied independently; null sources cause that visual element to render
 * empty / inert.
 */
public interface FmDspDataSource {

    FftDataSource fft();

    LevelDataSource level();

    TrackStatusSource trackStatus();

    WorkStateSource work();
}
