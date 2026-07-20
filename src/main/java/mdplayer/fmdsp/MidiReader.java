/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import mdplayer.ChipRegister;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.mid.MidiDriver;
import mdplayer.driver.rcp.RcpDriver;
import mdplayer.driver.rcp.RcsDriver;
import mdplayer.driver.zms.ZmsDriver;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * The MIDI drivers - RCP, RCS, MID and ZMS - which emulate no chip at all.
 * <p>
 * There is nothing to read back: the notes go out to a synthesizer that reports nothing, so what
 * is shown is what went past {@link MidiPlugin} on its way there. A MIDI note is already a note,
 * so these keys are exact; the volume is the note's velocity scaled by the channel's controller 7,
 * and the pan is controller 10.
 * <p>
 * Sixteen channels do not fit nine rows, so they are taken in the order they first sound.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
public class MidiReader implements FmDspChipReader {

    private static final int CHANNELS = 16;

    private ChipRegister chipRegister;
    private Supplier<BaseDriver> driver;

    private final boolean[] prevSoundings = new boolean[CHANNELS];
    private final int[] prevNotes = new int[CHANNELS];
    private boolean active;

    /** MIDI channel shown on each row, -1 = none yet */
    private final int[] slotChannels = new int[CHANNELS];

    private int mappedChannels;

    @Override
    public String chipName() {
        return "MIDI";
    }

    @Override
    public void bind(ChipRegister chipRegister) {
        this.chipRegister = chipRegister;
    }

    @Override
    public void bind(Supplier<BaseDriver> driver) {
        this.driver = driver;
    }

    @Override
    public void reset() {
        Arrays.fill(prevSoundings, false);
        Arrays.fill(prevNotes, 0);
        Arrays.fill(slotChannels, -1);
        mappedChannels = 0;
        active = false;
        if (midi() != null) midi().clearChannels();
    }

    private MidiPlugin midi() {
        return chipRegister == null ? null : chipRegister.plugin(MidiPlugin.class);
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.PCM);
    }

    @Override
    public int priority() {
        return 43;
    }

    @Override
    public boolean ready() {
        if (driver == null || midi() == null) return false;
        BaseDriver d = driver.get();
        return d instanceof RcpDriver || d instanceof RcsDriver
                || d instanceof MidiDriver || d instanceof ZmsDriver;
    }

    @Override
    public void poll() {
        for (int ch = 0; ch < CHANNELS && mappedChannels < slotChannels.length; ch++) {
            if (sounding(ch) && slotOf(ch) < 0) {
                slotChannels[mappedChannels++] = ch;
            }
        }
    }

    private int slotOf(int ch) {
        for (int s = 0; s < mappedChannels; s++) {
            if (slotChannels[s] == ch) return s;
        }
        return -1;
    }

    private boolean sounding(int ch) {
        MidiPlugin midi = midi();
        return midi != null && midi.velocity(ch) > 0;
    }

    @Override
    public boolean active(Group group) {
        if (!active) {
            for (int ch = 0; ch < CHANNELS && !active; ch++) active = sounding(ch);
        }
        return active;
    }

    @Override
    public int channels(Group group) {
        return CHANNELS;
    }

    @Override
    public void read(Group group, int slot, FmDspChannel out) {
        out.name = "MIDI";
        int ch = slotChannels[slot];
        MidiPlugin midi = midi();
        if (ch < 0 || midi == null) return;

        boolean sounding = sounding(ch);
        int note = midi.note(ch);

        out.num = ch + 1; // the MIDI channel, wherever the slot map put it
        out.pcmCh = ch + 1;
        out.sounding = sounding;
        out.keyOn = sounding && (!prevSoundings[ch] || note != prevNotes[ch]);
        prevSoundings[ch] = sounding;
        prevNotes[ch] = note;

        // a MIDI note is a note already, and the display counts from the same C
        out.note = sounding ? note : -1;
        out.toneNum = midi.program(ch);
        int velocity = midi.velocity(ch);
        int volume = midi.volume(ch);
        out.volume = velocity;
        out.amplitude = sounding ? velocity * volume / (127.0 * 127) : 0;
        out.pan = panOf(midi.pan(ch));
    }

    /** controller 10: 0 hard left, 64 centre, 127 hard right */
    private static Pan panOf(int pan) {
        if (pan < 16) return Pan.LEFT;
        if (pan < 56) return Pan.MID_LEFT;
        if (pan <= 72) return Pan.CENTER;
        if (pan <= 112) return Pan.MID_RIGHT;
        return Pan.RIGHT;
    }

    @Override
    public boolean masked(Group group, int slot) {
        return false;
    }
}
