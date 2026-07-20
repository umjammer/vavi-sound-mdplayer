/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;
import vavi.sound.visualizer.fmdsp.TrackInfo;


/**
 * One chip channel's state, as a {@link FmDspChipReader} reports it. The source turns this into
 * the visualizer's row and level meter.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public class FmDspChannel {

    /** the key is currently held */
    public boolean sounding;

    /** a key-on happened since the previous snapshot */
    public boolean keyOn;

    /** semitones above C0, -1 while silent */
    public int note = -1;

    /** display volume, whatever unit the chip counts in */
    public int volume;

    /** linear output level 0..1 the level meter attacks to */
    public double amplitude;

    /**
     * Whether {@link #amplitude} is the sound the chip actually made, rather than what its
     * registers say it should be making. A measured one is already an envelope, so the source
     * shows it as it is instead of putting its own attack and sustain over the top of it.
     */
    public boolean measured;

    /**
     * Whether this is a sampled voice, i.e. one whose {@link #note} is worked back from a playback
     * rate rather than played as a note. It matters for a part that is streamed rather than
     * sequenced - a whole track as one endless sample, which arcade boards do - where the rate is
     * fixed and the note it lands nearest means nothing at all. See {@code ChipFmDspSource#stream}.
     */
    public boolean sampled;

    public Pan pan = Pan.CENTER;

    public TrackInfo info = TrackInfo.NORMAL;

    public boolean ssgTone;

    public boolean ssgNoise;

    /** shown in the PPZ8 channel column, 0 = none */
    public int pcmCh;

    /** instrument number if the chip has one */
    public int toneNum;

    /** display name of the chip section, up to 5 characters (e.g. "OPM", "PCM"); null = generic */
    public String name;

    /** display number of the row, 1-based; 0 lets the source number by slot */
    public int num;

    public void clear() {
        sounding = false;
        keyOn = false;
        note = -1;
        volume = 0;
        amplitude = 0;
        measured = false;
        sampled = false;
        pan = Pan.CENTER;
        info = TrackInfo.NORMAL;
        ssgTone = false;
        ssgNoise = false;
        pcmCh = 0;
        toneNum = 0;
        name = null;
        num = 0;
    }
}
