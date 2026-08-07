/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.vst;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import mdplayer.vst.VstMng.VstInfo2;


/**
 * A VST instrument seen as a MIDI out.
 * <p>
 * Everything that plays MIDI here - the RCP, ZMS, MID and RCS drivers - sends to a
 * {@link Receiver}, so an instrument that looks like one needs nothing else to be playable: the
 * notes go into the plug-in's queue and come back out as sound the next time the mixer asks
 * {@link VstMng#update} for a block.
 * <p>
 * Nothing is timestamped. The drivers produce their MIDI from the audio thread while it renders
 * the very block the plug-in is about to be asked for, so an event handed over with no offset
 * already lands within that block.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-08 nsano initial version <br>
 */
public class VstReceiver implements Receiver {

    private final VstInfo2 vi;

    /** where in the block being rendered the messages arriving now belong */
    private int deltaFrames;

    public VstReceiver(VstInfo2 vi) {
        this.vi = vi;
    }

    /** the instrument behind this out */
    public VstInfo2 getInfo() {
        return vi;
    }

    /**
     * Says where the messages that follow sit inside the block about to be rendered.
     * <p>
     * The drivers already count this while they play - see {@code VstPlugin.vstDelta} - and it is
     * the difference between every note in a block landing on its first sample and each landing
     * where the song put it.
     */
    public void setDeltaFrames(int deltaFrames) {
        this.deltaFrames = deltaFrames;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (vi.plugin == null) return;
        vi.plugin.getEventQueue().add(message.getMessage(), deltaFrames);
    }

    @Override
    public void close() {
        // the instrument outlives the out: it is handed back by VstMng#releaseInstruments and
        // given to the next song rather than unloaded here
    }
}
