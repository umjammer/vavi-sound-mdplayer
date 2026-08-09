/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.visualizer.fmdsp;

import java.util.Arrays;

import vavi.sound.visualizer.fmdsp.FftDataSource;


/**
 * The spectrum bars drawn from the notes that are sounding rather than from rendered PCM.
 * <p>
 * {@link vavi.sound.visualizer.fmdsp.FftAnalyzer} needs the mixed-down samples, which a source that
 * only sees MIDI going out to a synthesizer never has. What it does have is every note that is
 * being held and how loud it was struck, and that is most of what the bars show: a note is put at
 * the bar its frequency falls on, with {@link #HARMONICS} harmonics above it at {@code 1/n} - the
 * fall-off of a saw, which is roughly what an instrument's first few partials look like.
 * <p>
 * Bars are placed twelve to an octave with 250 Hz at bar 10.5, exactly as {@code FftAnalyzer} places
 * them, so they stand where the printed axis says they do. That puts bar 0 at ~136 Hz: the
 * fundamental of anything lower is off the low end of the axis and only its harmonics show, which
 * is what a real spectrum of the same note does too.
 * <p>
 * Everything past that is there to make it move like an analyzer rather than like a piano roll,
 * because notes arrive as steps and a spectrum drawn straight from them jerks from one shape to the
 * next:
 * <ul>
 * <li>each partial is drawn with a {@linkplain #SKIRT skirt} instead of on one bar, the way an FFT
 * window spreads a tone over its neighbours - a continuous curve rather than a comb of spikes;</li>
 * <li>the bars are {@linkplain #ATTACK attacked} and {@linkplain #RELEASE released} towards what the
 * notes ask for instead of jumping to it, so a note-on rises and a note-off falls;</li>
 * <li>a note held still is still shimmered by {@link #wave}, and a quiet {@linkplain #AIR floor}
 * moves along the bottom under whatever is playing. Both are invented - nothing in a MIDI stream
 * says what the sound is doing between two events - and both are scaled by what is actually
 * sounding, so silence stays silent and the fakery never outweighs the music.</li>
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-28 nsano initial version <br>
 */
public class NoteSpectrum implements FftDataSource {

    /** bars per octave, which the renderer's frequency axis is printed for */
    private static final double BARS_PER_OCTAVE = 12;

    /** frequency of bar 0, so that bar 10.5 is 250 Hz */
    private static final double LOW = 250 / Math.pow(2, 10.5 / BARS_PER_OCTAVE);

    /** the axis is 0 dB at the top and -48 dB at the bottom */
    private static final double FLOOR_DB = -48;

    /** partials a note is drawn with, its fundamental included */
    private static final int HARMONICS = 5;

    /**
     * Bars either side of a partial its skirt reaches, and how wide the skirt is: a bar away from
     * the peak is 11 dB down and two bars away is off the axis, so the partials keep their own
     * peaks and valleys instead of smearing the display into one wall.
     */
    private static final int SKIRT = 3;
    private static final double SKIRT_WIDTH = 0.9;

    /** how fast a bar rises to what the notes ask for, and how slowly it falls back [s] */
    private static final double ATTACK = 0.03, RELEASE = 0.12;

    /**
     * How much of a note's level reaches the bars, about 10 dB.
     * <p>
     * A note taken at face value is nearly full scale, and the bars stand at the ceiling with
     * nowhere left to move - where a spectrum measured off rendered PCM has one voice's partial
     * well down the axis, because a mix shares the scale between everything in it and an FFT
     * shares each voice between its own bins. This is the headroom that would have bought, so a
     * loud note peaks around three quarters of the way up and a chorus can still rise above it.
     */
    private static final double GAIN = 0.3;

    /** how far the shimmer moves a bar, as a fraction of it - 0.15 is about a decibel */
    private static final double SHIMMER = 0.15;

    /**
     * The moving floor under the music, as a fraction of the loudest bar: 23 dB below it, which
     * lands around -40 dBFS and draws as four or five of a bar's 32 columns - grass, not a wall.
     */
    private static final double AIR = 0.07;

    /** how far the floor tilts down towards the top of the axis, in bars - a couple of dB across it */
    private static final double AIR_ROLLOFF = 300;

    /** what the notes of this frame ask for */
    private final double[] targets = new double[LENGTH];

    /** what is drawn, which follows {@link #targets} rather than jumping to it */
    private final double[] levels = new double[LENGTH];

    /** {@link System#nanoTime} of the previous frame, 0 before the first one */
    private long lastNanos;

    /** seconds the animation has been running, which the shimmer and the floor move with */
    private double clock;

    /** forgets the previous frame; the notes of the next one are added over it */
    public void clear() {
        Arrays.fill(targets, 0);
    }

    /**
     * Adds one sounding note to the frame.
     *
     * @param note MIDI note number
     * @param amplitude how loud it is, {@code 0..1}
     */
    public void add(int note, double amplitude) {
        if (amplitude <= 0) return;
        double f = 440 * Math.pow(2, (note - 69) / 12.0);
        for (int h = 1; h <= HARMONICS; h++) {
            put(BARS_PER_OCTAVE * (Math.log(f * h / LOW) / Math.log(2)), amplitude / h);
        }
    }

    /**
     * Adds a band of noise rather than a note, which is what a drum is: its pitch is a sample's,
     * not a note's, and it covers a stretch of the axis instead of standing on one bar.
     * <p>
     * The band is worth less per bar than a note is - the same hit spread over forty of them - and
     * it is tilted {@link #BAND_TILT} down towards its top end, so that it reads as a drum losing
     * its high end rather than as a block sitting on the display.
     *
     * @param amplitude how loud it is, {@code 0..1}
     * @param from first bar of the band, and {@code to} its last; it fades over {@link #BAND_EDGE}
     *             bars either side so that it has no walls
     */
    private void addBand(double amplitude, double from, double to) {
        if (amplitude <= 0) return;
        for (int b = (int) (from - BAND_EDGE); b <= to + BAND_EDGE; b++) {
            double weight = b < from ? ramp((b - from + BAND_EDGE) / BAND_EDGE)
                    : b > to ? ramp((to + BAND_EDGE - b) / BAND_EDGE) : 1;
            double tilt = Math.pow(BAND_TILT, Math.clamp((b - from) / (to - from), 0, 1));
            energy(b, amplitude * BAND_LEVEL * weight * tilt);
        }
    }

    /**
     * Adds one drum as the band of the axis it covers, since its note number picks a sample rather
     * than a pitch: a kick sits under the low end, a snare spans the middle, a cymbal washes the
     * top. The bands are the GM kit's, roughly - what they are for is that the drums move the
     * spectrum the way drums move one, instead of standing on the bar their note number happens to
     * land on.
     *
     * @param note MIDI note number on a drum channel, which is a kit piece and not a pitch
     * @param amplitude how loud it is, {@code 0..1}
     */
    public void addDrum(int note, double amplitude) {
        switch (note) {
            case 35, 36 -> addBand(amplitude, 0, 5);                     // kick
            case 41, 43, 45, 47, 48, 50 -> addBand(amplitude, 2, 24);    // toms
            case 38, 40, 37, 39 -> addBand(amplitude, 10, 48);           // snare, rim, clap
            case 42, 44, 46 -> addBand(amplitude, 42, LAST_BAR);         // hi-hats
            case 49, 51, 52, 53, 55, 57, 59 -> addBand(amplitude, 36, LAST_BAR); // cymbals
            default -> addBand(amplitude, 24, 58);                       // the rest of the kit
        }
    }

    /** the top of the axis */
    private static final int LAST_BAR = LENGTH - 1;

    /** bars a band fades over at each end */
    private static final double BAND_EDGE = 5;

    /** how much of a hit's level one bar of its band carries */
    private static final double BAND_LEVEL = 0.45;

    /** what is left of a band at its top end, about 8 dB */
    private static final double BAND_TILT = 0.4;

    /** a raised cosine from 0 to 1, for edges that have no corner in them */
    private static double ramp(double x) {
        return 0.5 - 0.5 * Math.cos(Math.PI * Math.clamp(x, 0, 1));
    }

    /** one partial, spread over the bars around the one it falls on */
    private void put(double bar, double amplitude) {
        int center = (int) Math.floor(bar);
        for (int b = center - SKIRT; b <= center + SKIRT + 1; b++) {
            double d = (b - bar) / SKIRT_WIDTH;
            energy(b, amplitude * Math.exp(-d * d));
        }
    }

    /**
     * Adds one contribution to a bar, in power rather than by taking the louder of the two: an FFT
     * bin holds everything that falls in it, so two voices on one bar are 3 dB up on one. That is
     * where the display's dynamics come from - a chorus reaches the top of the axis and a single
     * line sits well below it, instead of every bar being pinned by whatever is loudest.
     */
    private void energy(int bar, double amplitude) {
        if (bar >= 0 && bar < LENGTH) {
            targets[bar] = Math.sqrt(targets[bar] * targets[bar] + amplitude * amplitude);
        }
    }

    /**
     * Advances the display towards this frame's notes and reads it off.
     * <p>
     * Called once a drawn frame, so the ballistics are timed off the wall clock rather than counted
     * in frames: the rate the renderer manages is not this class's business, and a dropped frame
     * should not slow the bars down.
     */
    @Override
    public void readFft(int[] out) {
        long now = System.nanoTime();
        // the first frame, and any frame after the display has been standing still, is one step
        double dt = lastNanos == 0 ? 1 / 60.0 : Math.clamp((now - lastNanos) / 1e9, 0, 0.25);
        lastNanos = now;
        clock += dt;

        double loudest = 0;
        for (double target : targets) loudest = Math.max(loudest, target);

        double attack = 1 - Math.exp(-dt / ATTACK);
        double release = 1 - Math.exp(-dt / RELEASE);
        for (int i = 0; i < LENGTH; i++) {
            double target = targets[i] * (1 + SHIMMER * wave(i, clock));
            // the floor, which is quietest where a real one is: up at the top of the axis
            // the floor wobbles gently: enough to be alive, not enough to fall off the axis and
            // leave holes in the grass
            target = Math.max(target, loudest * AIR * Math.exp(-i / AIR_ROLLOFF)
                    * (0.8 + 0.2 * wave(i + 37, clock * 0.6)));
            levels[i] += (target - levels[i]) * (target > levels[i] ? attack : release);

            double db = 20 * Math.log10(Math.max(levels[i] * GAIN, 1e-9));
            out[i] = Math.clamp(Math.round((db - FLOOR_DB) / -FLOOR_DB * (MAX + 1)), 0, MAX);
        }
    }

    /**
     * A smooth {@code -1..1} wobble that differs from bar to bar and never repeats to the eye: two
     * sines whose periods do not divide each other, one slow and one slower. Cheaper than noise and
     * continuous, which is what matters - a random number per frame would flicker, which is exactly
     * what this is here to avoid.
     */
    private static double wave(int bar, double t) {
        return 0.5 * Math.sin(t * 2.7 + bar * 0.7) + 0.5 * Math.sin(t * 1.13 + bar * 1.9);
    }
}
