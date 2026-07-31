/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Per-track snapshot consumed by the visualizer. Mirrors the fields the C
 * {@code fmdsp} reads from {@code struct fmdriver_track_status}.
 */
public final class TrackStatus {

    public boolean playing;
    public TrackInfo info = TrackInfo.NORMAL;
    /** total tick length for the current step (0 - 255). */
    public int ticks;
    /** ticks remaining for the current step. */
    public int ticksLeft;
    /**
     * MML key. High nibble = octave, low nibble = note (0..11 = C..B,
     * 0xF = release, 0xFF = silent).
     */
    public int key = 0xff;
    /** key after pitchbend / LFO applied. */
    public int actualKey = 0xff;
    public int toneNum;
    public int volume;
    public int gate;
    /** signed detune. */
    public int detune;
    /** short status mnemonic. up to 8 chars + NUL in C. */
    public String status = "";
    /** per-FM-slot mute mask (FM3 EX). */
    public final boolean[] fmSlotMask = new boolean[4];
    /** PPZ8 channel + 1, 0 = none. */
    public int ppz8Ch;
    public boolean ssgTone;
    public boolean ssgNoise;
    public int ssgNoiseFreq;
}
