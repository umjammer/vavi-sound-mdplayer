/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Provides the FFT spectrum frame for the visualizer.
 * <p>
 * The C code computes a 70-bin spectrum in the range 0..31 (4 steps per 6 dB).
 * Implementations may either run an FFT over a sliding window of PCM, or
 * return any non-negative bar values.
 */
public interface FftDataSource {

    /** Number of spectrum bins. Mirrors {@code FFTDISPLEN}. */
    int LENGTH = 70;

    /** Maximum bin value. Each bin spans 0..31. */
    int MAX = 31;

    /**
     * Fill {@code out} with the latest spectrum frame.
     *
     * @param out destination of length at least {@link #LENGTH}; values are
     *            clamped by the renderer to {@link #MAX}.
     */
    void readFft(int[] out);
}
