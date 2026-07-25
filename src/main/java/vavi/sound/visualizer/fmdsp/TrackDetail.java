/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.util.Arrays;


/**
 * What a track row is doing inside its chip, as {@link RightMode#TRACK_INFO} shows it.
 * <p>
 * The original reads the OPNA emulator itself and lays each track type out its own way: an FM
 * track is four operator bars with an envelope state beside each, an SSG one a single bar and its
 * tone period, an ADPCM or PPZ8 one a titled row of registers. This carries all of those without
 * naming a chip, so that any {@link TrackDetailSource} can fill it: up to {@link #LINES} bar lines
 * with three text fields each, or - with no bar lines at all - a {@link #header} and the
 * {@link #text} under it, which is the register dump shape.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-25 nsano initial version <br>
 * @see FmDspVisualizer#trackDetail
 */
public final class TrackDetail {

    /** The most bar lines a row has, which is an FM row's four operators. */
    public static final int LINES = 4;

    /** Columns of one bar. */
    public static final int COLUMNS = 64;

    /** Bar lines in use, {@code 0} for a row that only has the {@link #header} dump. */
    public int lines;

    /** How far each bar is filled, {@code 0} to {@link #COLUMNS}; negative draws no bar. */
    public final int[] bar = new int[LINES];

    /** First column of each bar's bright marker, negative for none. */
    public final int[] mark = new int[LINES];

    /** Columns the marker covers; the original's FM marker is 1 wide and its SSG one 32. */
    public final int[] markWidth = new int[LINES];

    /** Mnemonic beyond the end of each bar, 3 characters - the original's "ATT", "DEC", "SUS". */
    public final String[] state = new String[LINES];

    /** The value behind {@link #state}, 3 characters. */
    public final String[] value = new String[LINES];

    /** A 4 character value further right; the original has the F-number on the first line only. */
    public final String[] extra = new String[LINES];

    /**
     * Whether {@link #bar} is the level the chip's registers ask for rather than the level its
     * envelope generator is actually at.
     * <p>
     * The original reads the envelope out of the emulator, which a source built on register caches
     * cannot do. Saying so lets such a source scale the bars by the envelope it already models for
     * the row's level meter, so that they attack and release with the note instead of standing at
     * the register value for as long as it is held.
     */
    public boolean modelled;

    /** Column titles of a row that shows registers instead of bars, e.g. "VOL DELTA  START". */
    public String header;

    /** Their values, drawn under {@link #header}. */
    public String text;

    /** Empties this, the way the sources reuse one instance. */
    public void clear() {
        lines = 0;
        Arrays.fill(bar, -1);
        Arrays.fill(mark, -1);
        Arrays.fill(markWidth, 1);
        Arrays.fill(state, null);
        Arrays.fill(value, null);
        Arrays.fill(extra, null);
        modelled = false;
        header = null;
        text = null;
    }
}
