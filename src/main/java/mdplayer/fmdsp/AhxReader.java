/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import mdplayer.lib.ahx.AHX;
import mdplayer.driver.ahx.AhxDriver;


/**
 * AHX, the Amiga tracker Hively grew out of: four voices, no panning of its own.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 * @see AmigaTrackerReader for what the voices mean
 */
public class AhxReader extends AmigaTrackerReader {

    private static final int VOICES = 4;

    /** the replayer's note volume tops out here */
    private static final int volumeMax = 64;

    @Override
    public String chipName() {
        return "AHX";
    }

    @Override
    public int priority() {
        return 44;
    }

    @Override
    public boolean ready() {
        return driver != null && driver.get() instanceof AhxDriver;
    }

    private AHX.AHXPlayer player() {
        return driver.get() instanceof AhxDriver ahx ? ahx.getPlayer() : null;
    }

    @Override
    protected int voices() {
        return player() == null ? 0 : VOICES;
    }

    /**
     * The voice's Amiga period turned back into a note. It briefly holds a note index while the
     * replayer works it out, but what survives the routine - and what is heard - is the period,
     * slides and vibrato folded in.
     */
    @Override
    protected int note(int voice) {
        AHX.AHXPlayer player = player();
        return player == null ? 0
                : noteOfPeriod(AHX.AHXPlayer.PERIOD_TABLE, player.voices[voice].audioPeriod);
    }

    @Override
    protected int volume(int voice) {
        AHX.AHXPlayer player = player();
        if (player == null) return 0;
        AHX.AHXPlayer.AHXVoice v = player.voices[voice];
        return v.trackOn == 0 ? 0 : v.noteMaxVolume;
    }

    @Override
    protected int volumeMax() {
        return volumeMax;
    }
}
