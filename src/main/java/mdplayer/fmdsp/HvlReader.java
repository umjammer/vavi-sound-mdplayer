/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import mdplayer.driver.hvl.HVL;
import mdplayer.driver.hvl.HvlDriver;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * Hively Tracker, which plays its own audio and registers no chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 * @see AmigaTrackerReader for what the voices mean
 */
public class HvlReader extends AmigaTrackerReader {

    /** the replayer's note volume tops out here */
    private static final int volumeMax = 64;

    @Override
    public String chipName() {
        return "HVL";
    }

    @Override
    public boolean ready() {
        return driver != null && driver.get() instanceof HvlDriver;
    }

    private HVL.Tune tune() {
        return driver.get() instanceof HvlDriver hvl ? hvl.getTune() : null;
    }

    @Override
    protected int voices() {
        HVL.Tune tune = tune();
        return tune == null ? 0 : Math.min(tune.ht_Channels, tune.ht_Voices.length);
    }

    @Override
    protected int note(int voice) {
        HVL.Tune tune = tune();
        return tune == null ? 0 : noteOfPeriod(HVL.PERIOD_TAB, tune.ht_Voices[voice].vc_AudioPeriod);
    }

    @Override
    protected int volume(int voice) {
        HVL.Tune tune = tune();
        if (tune == null) return 0;
        HVL.Voice v = tune.ht_Voices[voice];
        return v.vc_TrackOn == 0 ? 0 : v.vc_NoteMaxVolume;
    }

    @Override
    protected int volumeMax() {
        return volumeMax;
    }

    /** HVL is the one of these with real stereo: the pan runs 0 to 255 across the field */
    @Override
    protected Pan pan(int voice) {
        HVL.Tune tune = tune();
        if (tune == null) return Pan.CENTER;
        int pan = tune.ht_Voices[voice].vc_Pan;
        if (pan < 48) return Pan.LEFT;
        if (pan < 112) return Pan.MID_LEFT;
        if (pan <= 144) return Pan.CENTER;
        if (pan <= 208) return Pan.MID_RIGHT;
        return Pan.RIGHT;
    }
}
