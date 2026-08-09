/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;


/**
 * What a voice of an SPU is doing, for a view onto the chip rather than for the sound.
 * <p>
 * The PS1's SPU and the PS2's SPU2 are the same voice twice over - an ADPCM sample stepped at a
 * pitch, through an ADSR envelope, into a left and a right volume - so both answer this and the
 * visualizer needs to know which it is looking at only for the name.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public interface SpuVoices {

    /** the envelope phases, which are also the states {@code MixADSR} steps through */
    int ATTACK = 0;
    int DECAY = 1;
    int SUSTAIN = 2;
    int RELEASE = 3;

    /** 24 on a PS1, 48 on a PS2 */
    int voiceCount();

    /** the voice is playing, i.e. it was keyed on and its envelope has not run out */
    boolean on(int voice);

    /** the voice was keyed off and is in its release */
    boolean released(int voice);

    /** the ADSR level, 0 to 0x7fffffff */
    int envelopeLevel(int voice);

    /** {@link #ATTACK}, {@link #DECAY}, {@link #SUSTAIN} or {@link #RELEASE} */
    int envelopePhase(int voice);

    /** 0 to 0x3fff */
    int leftVolume(int voice);

    /** 0 to 0x3fff */
    int rightVolume(int voice);

    /** the pitch as the register holds it: 0x1000 plays the sample at the chip's own rate */
    int pitch(int voice);

    /** byte offset of the sample into sound ram, which is the nearest thing to a tone number */
    int sampleStart(int voice);

    /** byte offset the sample loops back to */
    int sampleLoop(int voice);

    /** the voice is playing the noise generator instead of its sample */
    boolean noise(int voice);

    /** the voice is being fed to the reverb unit */
    boolean reverb(int voice);

    /**
     * How many times the voice has been keyed on since the song started.
     * <p>
     * A view sees a key on where this changes. It is a count rather than a flag that is cleared
     * on read so that two views onto the same chip - the fmdsp strip and a keyboard panel - each
     * see every edge, and so that a note struck again at the same pitch is not missed the way a
     * pitch comparison misses it.
     */
    int keyOnCount(int voice);
}
