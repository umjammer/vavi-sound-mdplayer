/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Top-level work state. Mirrors the subset of {@code struct fmdriver_work}
 * the C visualizer reads.
 */
public interface WorkStateSource {

    /** Sample rate the elapsed-time counter is expressed in. C uses 55467. */
    default int sampleRate() {
        return 55467;
    }

    /** Frames generated since playback started. */
    long generatedFrames();

    /** Driver internal tick counter (TimerB ticks). */
    long timerBCount();

    /** TimerB period (0..255). */
    int timerB();

    /** Loop count. */
    int loopCount();

    /** TimerB ticks measured for one loop, 0 if unknown. */
    long loopTimerBCount();

    /** TimerB ticks since the most recent loop start. */
    long timerBCountLoop();

    /** True if a song is loaded and currently advancing. */
    boolean playing();

    /** True if playback is paused. */
    boolean paused();

    /** Name of the sound driver shown next to {@code DRIVER}, e.g. {@code "PMD"}, may be null. */
    default String driverName() {
        return null;
    }

    /** Current title / filename, may be null. */
    default String filename() {
        return null;
    }

    /** Comment line for {@code line in 0..2}, may be null. */
    default String comment(int line) {
        return null;
    }
}
