/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.smaf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * What a SMAF song is made of, and what of it is sounding at a given moment.
 * <p>
 * Nothing about the playing can be read back out of {@link MmfToolPlayer}: the Yamaha dll inside
 * it sequences the file itself and answers nothing but a position, so there is no chip state to
 * poll and no note to catch on its way past. What there is instead is the score - mmftool parsed
 * it for its own piano roll and writes it out before it starts playing - and a clock, which is
 * the audio itself. Together they say what is sounding now.
 * <p>
 * The events are MIDI shaped, because that is what SMAF is underneath: a channel, a key, a
 * velocity and - unlike MIDI - the length the note is held for, so a note carries its own note
 * off. Both of the sequence formats end up here, mmftool having converted the older one on the
 * way (see its {@code smaf.c}).
 * <p>
 * {@link #seek} walks the play head forward and is the only thing that moves it, so the state
 * below is always the state at the moment last asked for. Asking for a moment that has gone by
 * starts it over rather than refusing - which is what a song played twice does.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-11 nsano initial version <br>
 * @see SmafTelemetry for how it gets here and what puts it in step with the sound
 */
public class SmafScore {

    /** channels a SMAF sequence has, the same sixteen MIDI has */
    public static final int CHANNELS = 16;

    /** how many notes of one channel can be held at once before the oldest is forgotten */
    private static final int POLYPHONY = 16;

    /**
     * The shortest a note is shown for. A file may hold a note for no time at all - a percussion
     * hit is a strike, not a length - and a note that ends in the same moment it starts would
     * never be seen by a display that looks sixty times a second.
     */
    private static final int MINIMUM_GATE_MILLIS = 40;

    /** a note: when it is struck, how hard, and how long it is held [ms] */
    public record Note(int timeMs, int ch, int key, int velocity, int gateMs) {

        int endMillis() {
            return timeMs + Math.max(gateMs, MINIMUM_GATE_MILLIS);
        }
    }

    /**
     * Anything that is not a note. {@code cc} is the controller number, or {@link #PROGRAM} for a
     * program change and {@link #BEND} for a pitch bend - they are all one channel and one number,
     * and keeping them in one list is what keeps them in order with each other.
     */
    public record Control(int timeMs, int ch, int cc, int value) {
    }

    public static final int PROGRAM = -1;
    public static final int BEND = -2;

    /** what one channel is doing, as the score has it */
    public static class Channel {

        /** the notes held right now, most recently struck last */
        private final Note[] held = new Note[POLYPHONY];
        private int heldCount;

        /** a note was struck since the play head was last asked about it */
        public boolean keyOn;

        public int volume = 100;
        public int expression = 127;

        /** controller 1, which is the wheel the file bends and wobbles a note with */
        public int modulation;

        public int pan = 64;
        public int program;
        public int bend = 0x2000;
        public int bankMsb;

        /** the note the display shows, which is the last one struck of those still held */
        public Note top() {
            return heldCount == 0 ? null : held[heldCount - 1];
        }

        public boolean sounding() {
            return heldCount > 0;
        }

        private void strike(Note note) {
            if (heldCount == held.length) {
                System.arraycopy(held, 1, held, 0, held.length - 1);
                heldCount--;
            }
            held[heldCount++] = note;
            keyOn = true;
        }

        private void expire(double millis) {
            int kept = 0;
            for (int i = 0; i < heldCount; i++) {
                if (held[i].endMillis() > millis) {
                    held[kept++] = held[i];
                }
            }
            Arrays.fill(held, kept, heldCount, null);
            heldCount = kept;
        }

        private void reset() {
            Arrays.fill(held, null);
            heldCount = 0;
            keyOn = false;
            volume = 100;
            expression = 127;
            modulation = 0;
            pan = 64;
            program = 0;
            bend = 0x2000;
            bankMsb = 0;
        }
    }

    private final List<Note> notes = new ArrayList<>();
    private final List<Control> controls = new ArrayList<>();
    private final Channel[] channels = new Channel[CHANNELS];

    /** how far down each list the play head has read */
    private int noteCursor, controlCursor;

    /** where the play head is [ms], -1 before it has been put anywhere */
    private double headMillis = -1;

    /** what the file says it is for, as mmftool numbers the formats */
    private int format;

    /**
     * Where in the file playing begins [ms], which is not always its beginning.
     * <p>
     * A SMAF file may carry a start point - the {@code MspI} chunk names one - and the player
     * begins there rather than at the top, counting its own position from it. So everything below
     * is held on the player's clock rather than the file's: the events are moved back by this as
     * they arrive, and whatever is in front of the start point ends up before zero, where the
     * setup it consists of is applied by the first {@link #seek} and its notes are never struck.
     */
    private int startMillis;

    public SmafScore() {
        for (int ch = 0; ch < CHANNELS; ch++) {
            channels[ch] = new Channel();
        }
    }

    void setFormat(int format) {
        this.format = format;
    }

    /** where the file says playing begins; set before the events arrive, they are moved by it */
    void setStartMillis(int startMillis) {
        this.startMillis = startMillis;
    }

    /** where in the file the player started, which everything here is counted from [ms] */
    public int getStartMillis() {
        return startMillis;
    }

    /** which of the MA chips the song is for, in mmftool's numbering; 0 when it did not say */
    public int getFormat() {
        return format;
    }

    void add(Note note) {
        if (note.ch() >= 0 && note.ch() < CHANNELS) {
            notes.add(new Note(note.timeMs() - startMillis, note.ch(), note.key(),
                    note.velocity(), note.gateMs()));
        }
    }

    void add(Control control) {
        if (control.ch() >= 0 && control.ch() < CHANNELS) {
            controls.add(new Control(control.timeMs() - startMillis, control.ch(),
                    control.cc(), control.value()));
        }
    }

    public int noteCount() {
        return notes.size();
    }

    public int controlCount() {
        return controls.size();
    }

    /**
     * When the first note that is played is struck [ms], or -1 for a song with none. A note in
     * front of the start point is not one of those - it is never reached.
     */
    public int firstNoteMillis() {
        for (Note note : notes) {
            if (note.endMillis() > 0) {
                return Math.max(0, note.timeMs());
            }
        }
        return -1;
    }

    /** how long the song plays for, as the last thing in it ends [ms] */
    public int lengthMillis() {
        int end = 0;
        for (Note note : notes) {
            end = Math.max(end, note.endMillis());
        }
        return end;
    }

    public Channel channel(int ch) {
        return channels[ch];
    }

    /**
     * Moves the play head to {@code millis} and brings every channel up to it.
     * <p>
     * Only forward: the events are read once each, in order, which is what makes a key on an
     * edge rather than something worked out by comparing two pictures. Going back - a song
     * starting again - is a rewind, which reads them from the top.
     */
    public void seek(double millis) {
        if (millis < headMillis) {
            rewind();
        }
        headMillis = millis;

        while (controlCursor < controls.size() && controls.get(controlCursor).timeMs() <= millis) {
            apply(controls.get(controlCursor++));
        }
        while (noteCursor < notes.size() && notes.get(noteCursor).timeMs() <= millis) {
            Note note = notes.get(noteCursor++);
            // a note whose whole length is already behind the head was missed rather than
            // struck - the head jumped over it - and showing it now would be showing it late
            if (note.endMillis() > millis) {
                channels[note.ch()].strike(note);
            }
        }
        for (Channel channel : channels) {
            channel.expire(millis);
        }
    }

    /** clears the key on flags; call once a snapshot after reading them */
    public void clearEdges() {
        for (Channel channel : channels) {
            channel.keyOn = false;
        }
    }

    private void apply(Control control) {
        Channel channel = channels[control.ch()];
        switch (control.cc()) {
            case PROGRAM -> channel.program = control.value();
            case BEND -> channel.bend = control.value();
            case 0 -> channel.bankMsb = control.value();
            case 1 -> channel.modulation = control.value();
            case 7 -> channel.volume = control.value();
            case 10 -> channel.pan = control.value();
            case 11 -> channel.expression = control.value();
            default -> {
            }
        }
    }

    /** empties this of the song it holds, for one about to be read into it */
    public void clear() {
        notes.clear();
        controls.clear();
        startMillis = 0;
        rewind();
    }

    /** puts the play head back before the first event, for a song about to be played again */
    public void rewind() {
        noteCursor = 0;
        controlCursor = 0;
        headMillis = -1;
        for (Channel channel : channels) {
            channel.reset();
        }
    }
}
