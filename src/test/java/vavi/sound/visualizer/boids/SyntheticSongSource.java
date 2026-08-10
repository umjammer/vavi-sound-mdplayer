/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.boids;

import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.sound.visualizer.fmdsp.FmDspDataSource;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.sound.visualizer.fmdsp.TrackStatusSource;
import vavi.sound.visualizer.fmdsp.WorkStateSource;


/**
 * A song that is not one: six parts playing a chord sequence at fixed note lengths, with a plucked
 * envelope on each note, so that {@link BoidsVisualizer} can be looked at and measured without a
 * chip, a driver or a file.
 * <p>
 * Time is taken from a clock the caller advances ({@link #advance}) rather than from the wall, so a
 * test can step it deterministically while the demo just follows real time.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
class SyntheticSongSource implements FmDspDataSource, TrackStatusSource, LevelDataSource, WorkStateSource {

    /** the rows that play */
    private static final TrackId[] TRACKS = {
            TrackId.FM_1, TrackId.FM_2, TrackId.FM_3, TrackId.FM_4, TrackId.SSG_1, TrackId.ADPCM,
    };

    /** semitones of the four chords the parts walk through, one column per part */
    private static final int[][] CHORDS = {
            // C     E     G     C'    bass  drum
            {  48,   52,   55,   60,   36,   0},
            {  53,   57,   60,   65,   41,   0},
            {  50,   53,   57,   62,   38,   0},
            {  55,   59,   62,   67,   43,   0},
    };

    /** seconds each part waits between its notes; deliberately not a common multiple */
    private static final double[] PERIODS = {1.0, 0.75, 0.5, 0.25, 0.5, 0.125};

    /** how long a note takes to die away, in seconds */
    private static final double DECAY = 0.35;

    private double time;

    /** true while nothing plays, so that a test can look at a resting screen */
    private boolean silent;

    void advance(double seconds) {
        time += seconds;
    }

    double time() {
        return time;
    }

    void setSilent(boolean silent) {
        this.silent = silent;
    }

    /** the chord the piece is on, changing every two seconds */
    private int chord() {
        return (int) (time / 2) % CHORDS.length;
    }

    /** the note a part is playing, in semitones, or -1 while it rests */
    private int note(int part) {
        if (silent) return -1;
        int note = CHORDS[chord()][part];
        if (note <= 0) return -1;
        // the arpeggio: the top part walks its chord tone up an octave and back
        if (part == 3) note += 12 * ((int) (time / PERIODS[part]) % 2);
        return note;
    }

    /** 0..1, one plucked envelope per note */
    private double envelope(int part) {
        if (note(part) < 0) return 0;
        double since = time % PERIODS[part];
        return Math.exp(-since / DECAY);
    }

    private int partOf(TrackId track) {
        for (int i = 0; i < TRACKS.length; i++) {
            if (TRACKS[i] == track) return i;
        }
        return -1;
    }

    @Override public FftDataSource fft() { return null; }
    @Override public LevelDataSource level() { return this; }
    @Override public TrackStatusSource trackStatus() { return this; }
    @Override public WorkStateSource work() { return this; }

    // ----- tracks -----

    @Override
    public TrackId[] displayTracks() {
        return TRACKS;
    }

    @Override
    public void readStatus(TrackId track, TrackStatus out) {
        int part = partOf(track);
        int note = part < 0 ? -1 : note(part);
        out.playing = note >= 0;
        out.key = note < 0 ? 0xff : (note / 12) << 4 | note % 12;
        out.actualKey = out.key;
        out.volume = (int) (envelope(Math.max(part, 0)) * 127);
        out.ticks = 48;
        out.ticksLeft = 24;
        out.toneNum = Math.max(part, 0);
        out.gate = 0;
        out.detune = 0;
        out.status = "";
        out.ppz8Ch = 0;
        out.ssgTone = false;
        out.ssgNoise = false;
        out.ssgNoiseFreq = 0;
    }

    @Override
    public String trackTypeName(TrackId track) {
        return switch (track) {
            case SSG_1, SSG_2, SSG_3 -> "SSG";
            case ADPCM -> "PCM";
            default -> "FM";
        };
    }

    @Override
    public int trackNumber(TrackId track) {
        return partOf(track) + 1;
    }

    // ----- levels -----

    @Override
    public int level(int channel) {
        TrackId track = track(channel);
        int part = track == null ? -1 : partOf(track);
        return part < 0 ? 0 : (int) (envelope(part) * 32767);
    }

    @Override
    public Pan pan(int channel) {
        return Pan.CENTER;
    }

    @Override
    public TrackId track(int channel) {
        return channel < TRACKS.length ? TRACKS[channel] : null;
    }

    // ----- work -----

    @Override public long generatedFrames() { return (long) (time * sampleRate()); }
    @Override public long timerBCount() { return (long) (time * 100); }
    @Override public int timerB() { return 200; }
    @Override public int loopCount() { return 0; }
    @Override public long loopTimerBCount() { return 0; }
    @Override public long timerBCountLoop() { return 0; }
    @Override public boolean playing() { return !silent; }
    @Override public boolean paused() { return false; }
    @Override public String driverName() { return "SYNTH"; }
    @Override public String filename() { return "synthetic"; }
}
