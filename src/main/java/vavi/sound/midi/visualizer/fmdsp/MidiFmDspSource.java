/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.visualizer.fmdsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.sound.visualizer.fmdsp.FmDspDataSource;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.RightMode;
import vavi.sound.visualizer.fmdsp.TrackDetail;
import vavi.sound.visualizer.fmdsp.TrackDetailSource;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackInfo;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.sound.visualizer.fmdsp.TrackStatusSource;
import vavi.sound.visualizer.fmdsp.WorkStateSource;


/**
 * Feeds {@link FmDspVisualizer} from plain Java MIDI: a {@link Receiver} that shows what goes
 * through it.
 * <p>
 * Nothing here knows about mdplayer, its chips or its drivers. Insert it between a
 * {@link Sequencer} and wherever the notes are going and it displays the stream on its way past:
 * <pre>{@code
 *  Synthesizer synthesizer = MidiSystem.getSynthesizer();
 *  synthesizer.open();
 *  Sequencer sequencer = MidiSystem.getSequencer(false);
 *  sequencer.open();
 *  sequencer.setSequence(MidiSystem.getSequence(file));
 *
 *  MidiFmDspSource source = new MidiFmDspSource(synthesizer.getReceiver());
 *  sequencer.getTransmitter().setReceiver(source);
 *  visualizer.setDataSource(source);
 * }</pre>
 * A MIDI note is already a note, so the keyboards are exact where a source reading a chip's
 * frequency registers has to decode them. What the wire does not carry is the sound itself, which is
 * where the two approximations are:
 * <ul>
 * <li>the level meters, which are an envelope retriggered by each note-on at its velocity, sagging
 * while the key is held and released when it comes up, scaled by the channel's volume and
 * expression - the same model the chip sources use, for the same reason: nothing reports back.</li>
 * <li>the spectrum, which is drawn from the notes that are sounding rather than from PCM - see
 * {@link NoteSpectrum}.</li>
 * </ul>
 * The note length and gate meters are timed rather than read, as they are for a chip: a sequence's
 * note lengths belong to the sequence, and a receiver only sees the key going down and coming up
 * again. Notes struck within {@value #CHORD_TICKS} counts of each other are taken for one chord and
 * measured as one note, so a chord does not read as a run of instant ones.
 * <p>
 * The sixteen channels take a row and a meter column each, in channel order; the left half shows
 * the ten of them {@link #displayTracks} reports, which are the channels that have sounded. GM
 * keeps the drums on channel 10, whose column is the one the original strip labels {@code RHY}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-28 nsano initial version <br>
 */
public class MidiFmDspSource implements Receiver, FmDspDataSource, FftDataSource, LevelDataSource,
        TrackStatusSource, TrackDetailSource, WorkStateSource {

    /** the channels of one MIDI port, a row and a meter column each */
    public static final int CHANNELS = 16;

    /** GM puts the drums on channel 10 */
    private static final int DRUM_CHANNEL = 9;

    /**
     * The row each channel takes, in the order the left half lists rows. The names are the PC-98
     * sections the display was built for and mean nothing here - every row is labelled
     * {@code MIDI} - they are simply the rows in screen order, sixteen of the twenty-one.
     */
    private static final TrackId[] ROWS = {
            TrackId.FM_1, TrackId.FM_2, TrackId.FM_3, TrackId.FM_4, TrackId.FM_5, TrackId.FM_6,
            TrackId.SSG_1, TrackId.SSG_2, TrackId.SSG_3,
            TrackId.ADPCM,
            TrackId.PPZ8_1, TrackId.PPZ8_2, TrackId.PPZ8_3, TrackId.PPZ8_4, TrackId.PPZ8_5,
            TrackId.PPZ8_6,
    };

    /** channel of each row, {@code -1} for a row no channel takes */
    private static final int[] CHANNEL_OF = new int[TrackId.COUNT];

    static {
        Arrays.fill(CHANNEL_OF, -1);
        for (int ch = 0; ch < ROWS.length; ch++) CHANNEL_OF[ROWS[ch].ordinal()] = ch;
    }

    /** rows the left half fits */
    private static final int DISPLAY_ROWS = 10;

    /** how fast a held note's meter sags [s], and how far, as a fraction of the struck level */
    private static final double SUSTAIN_TAU = 1.67, SUSTAIN_LEVEL = 0.6;

    /** how fast a released note's meter falls [s] */
    private static final double RELEASE_TAU = 0.16;

    /** how fast a held note's spectrum decays [s] - see {@link #spectral} */
    private static final double SPECTRUM_TAU = 0.7;

    /**
     * The clock the note length and gate meters are measured against [Hz], the rate
     * {@code ChipFmDspSource} uses: a quarter note at 120 bpm is 120 counts, a sixteenth 30, and
     * the bar's 255 count full scale a bit over a second.
     */
    private static final double NOTE_TICK_HZ = 240;

    /** the bar is 64 columns of 4 counts each */
    private static final int BAR_FULL_SCALE = 255;

    /** the longest note the bar measures, past which it stays full */
    private static final int MAX_NOTE_LENGTH = (int) (NOTE_TICK_HZ * 60);

    /**
     * How close together two note-ons on one channel have to be to count as one chord, in note
     * clock counts - about 8 ms, which is nothing musically and more than a sequencer needs to send
     * the notes of a chord. Without this each note of a chord ends the one before it and the row
     * measures a string of instant notes.
     */
    private static final int CHORD_TICKS = 2;

    /** the pitch bend range a synthesizer defaults to, in cents */
    private static final int BEND_RANGE = 200;

    /** where the stream goes on to, null for a source that only watches */
    private final Receiver out;

    /** keys held down on each channel, and those a damper pedal is holding past their note-off */
    private final boolean[][] keys = new boolean[CHANNELS][128];
    private final boolean[][] damped = new boolean[CHANNELS][128];

    /** the note each row shows, {@code -1} while the channel is silent */
    private final int[] notes = new int[CHANNELS];

    /** velocity it was struck at */
    private final int[] velocities = new int[CHANNELS];

    private final int[] programs = new int[CHANNELS];

    /** CC7, CC11, CC10 and CC1 */
    private final int[] volumes = new int[CHANNELS];
    private final int[] expressions = new int[CHANNELS];
    private final int[] pans = new int[CHANNELS];
    private final int[] modulations = new int[CHANNELS];

    /** {@code 0..16383}, 8192 being no bend */
    private final int[] bends = new int[CHANNELS];

    /** CC65 and CC64 */
    private final boolean[] portamentos = new boolean[CHANNELS];
    private final boolean[] dampers = new boolean[CHANNELS];

    /** a row is shown playing once its channel has sounded, and gets a panel once it says anything */
    private final boolean[] used = new boolean[CHANNELS];
    private final boolean[] touched = new boolean[CHANNELS];

    /** the level the envelope is falling from, when it started to [s], and whether a key is down */
    private final double[] envLevels = new double[CHANNELS];
    private final double[] envTimes = new double[CHANNELS];
    private final boolean[] holdings = new boolean[CHANNELS];

    /** note clock count of each channel's last note-on and note-off, {@code -1} for none yet */
    private final long[] keyOnTicks = new long[CHANNELS];
    private final long[] keyOffTicks = new long[CHANNELS];

    /** measured length and gate of each channel's previous note, in note clock counts */
    private final int[] noteLengths = new int[CHANNELS];
    private final int[] gates = new int[CHANNELS];

    private final NoteSpectrum spectrum = new NoteSpectrum();

    /** the rows the left half shows, null until something sounds; written on the MIDI thread */
    private volatile TrackId[] dispTracks;

    // clock

    /** {@link System#nanoTime} the first message arrived at, 0 before it */
    private volatile long baseNanos;
    private volatile long pausedNanos;
    private volatile long pauseNanos;
    private volatile boolean paused;
    private volatile boolean playing;
    private volatile boolean closed;

    private volatile String filename;
    private final String[] comments = new String[3];

    /** a source that only watches the stream */
    public MidiFmDspSource() {
        this(null);
    }

    /**
     * @param out where the messages go on to after being displayed, typically a
     *            {@link Synthesizer}'s receiver; null to only watch a stream someone else delivers
     */
    public MidiFmDspSource(Receiver out) {
        this.out = out;
        reset();
    }

    /**
     * Forgets the previous song. The channel state is what a synthesizer would have been left in,
     * and the clock only ever runs forwards - call this before each song.
     */
    public void reset() {
        for (int ch = 0; ch < CHANNELS; ch++) {
            Arrays.fill(keys[ch], false);
            Arrays.fill(damped[ch], false);
        }
        Arrays.fill(notes, -1);
        Arrays.fill(velocities, 0);
        Arrays.fill(programs, 0);
        Arrays.fill(volumes, 100); // what GM resets a part to
        Arrays.fill(expressions, 127);
        Arrays.fill(pans, 64);
        Arrays.fill(modulations, 0);
        Arrays.fill(bends, 8192);
        Arrays.fill(portamentos, false);
        Arrays.fill(dampers, false);
        Arrays.fill(used, false);
        Arrays.fill(touched, false);
        Arrays.fill(envLevels, 0);
        Arrays.fill(envTimes, 0);
        Arrays.fill(holdings, false);
        Arrays.fill(keyOnTicks, -1);
        Arrays.fill(keyOffTicks, -1);
        Arrays.fill(noteLengths, 0);
        Arrays.fill(gates, 0);
        Arrays.fill(comments, null);
        dispTracks = null;
        baseNanos = 0;
        pausedNanos = 0;
        pauseNanos = 0;
        paused = false;
        playing = false;
        shownFrames = 0;
    }

    /** the title shown on the file bar */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** one of the three comment lines, {@code line} being 0..2; a sequence's own text fills these */
    public void setComment(int line, String comment) {
        comments[line] = comment;
    }

    /** freezes the clock, which the stream itself says nothing about */
    public void setPaused(boolean paused) {
        if (paused == this.paused) return;
        long now = System.nanoTime();
        if (paused) {
            pauseNanos = now;
        } else if (baseNanos != 0) {
            // a pause taken before the first message is not time the song spent waiting
            pausedNanos += now - pauseNanos;
        }
        this.paused = paused;
    }

    /** the song has ended: the rows stop and the clock stops with them */
    public void stop() {
        playing = false;
        allNotesOff();
    }

    // ----- Receiver -----

    /**
     * Displays the message and passes it on. Delivering it comes first, so watching the stream
     * never delays it.
     */
    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (out != null && !closed) out.send(message, timeStamp);
        if (closed) return;

        if (baseNanos == 0) {
            baseNanos = System.nanoTime();
            playing = true;
        }
        if (message instanceof ShortMessage sm) {
            shortMessage(sm);
        } else if (message instanceof MetaMessage mm) {
            metaMessage(mm);
        }
    }

    /**
     * Stops watching. The receiver this was built on belongs to whoever opened it and is left
     * open, the way a chained receiver leaves the one behind it alone.
     */
    @Override
    public void close() {
        closed = true;
    }

    private void shortMessage(ShortMessage sm) {
        int ch = sm.getChannel();
        if (ch >= CHANNELS) return;
        touched[ch] = true;
        int data1 = sm.getData1(), data2 = sm.getData2();
        switch (sm.getCommand()) {
            case ShortMessage.NOTE_ON -> {
                // a note-on at velocity 0 is a note-off, which is how running status sends them
                if (data2 > 0) noteOn(ch, data1, data2); else noteOff(ch, data1);
            }
            case ShortMessage.NOTE_OFF -> noteOff(ch, data1);
            case ShortMessage.PROGRAM_CHANGE -> programs[ch] = data1;
            case ShortMessage.PITCH_BEND -> bends[ch] = (data2 << 7) | data1;
            case ShortMessage.CONTROL_CHANGE -> controlChange(ch, data1, data2);
            default -> { /* aftertouch and the system messages say nothing the display shows */ }
        }
    }

    private void controlChange(int ch, int controller, int value) {
        switch (controller) {
            case 1 -> modulations[ch] = value;
            case 7 -> volumes[ch] = value;
            case 10 -> pans[ch] = value;
            case 11 -> expressions[ch] = value;
            case 64 -> {
                dampers[ch] = value >= 64;
                // the pedal coming up releases everything it was holding
                if (!dampers[ch]) {
                    for (int note = 0; note < 128; note++) {
                        if (damped[ch][note]) release(ch, note);
                    }
                }
            }
            case 65 -> portamentos[ch] = value >= 64;
            case 121 -> { // reset all controllers
                modulations[ch] = 0;
                expressions[ch] = 127;
                bends[ch] = 8192;
                dampers[ch] = false;
                portamentos[ch] = false;
            }
            case 120, 123 -> releaseAll(ch); // all sound off, all notes off
            default -> { /* the rest is the synthesizer's business */ }
        }
    }

    private void noteOn(int ch, int note, int velocity) {
        long tick = ticks();
        // the notes of a chord arrive together and are one note as far as the meters are concerned
        if (keyOnTicks[ch] < 0 || tick - keyOnTicks[ch] >= CHORD_TICKS) {
            if (keyOnTicks[ch] >= 0) {
                long measured = tick - keyOnTicks[ch];
                noteLengths[ch] = (int) Math.min(measured, MAX_NOTE_LENGTH);
                // a note the key never came up on was played legato, so its gate is its length
                long held = keyOffTicks[ch] < 0 ? measured : keyOffTicks[ch] - keyOnTicks[ch];
                gates[ch] = (int) Math.clamp(held, 0, MAX_NOTE_LENGTH);
            }
            keyOnTicks[ch] = tick;
            keyOffTicks[ch] = -1;
        }
        keys[ch][note] = true;
        damped[ch][note] = false;
        notes[ch] = note;
        velocities[ch] = velocity;
        if (!used[ch]) {
            used[ch] = true;
            updateDisplayTracks();
        }
        // re-attack: the meter jumps to the velocity and starts sagging from there
        envLevels[ch] = velocity / 127.0;
        envTimes[ch] = time();
        holdings[ch] = true;
    }

    private void noteOff(int ch, int note) {
        if (dampers[ch] && keys[ch][note]) {
            damped[ch][note] = true; // still sounding, the pedal is holding it
            return;
        }
        release(ch, note);
    }

    private void release(int ch, int note) {
        keys[ch][note] = false;
        damped[ch][note] = false;
        if (note == notes[ch]) {
            // the row follows what is left of the chord, its top note
            notes[ch] = -1;
            for (int n = 127; n >= 0; n--) {
                if (keys[ch][n]) { notes[ch] = n; break; }
            }
        }
        if (notes[ch] >= 0) return;

        // the channel has gone quiet: freeze the meter where it stands and let it fall
        if (holdings[ch]) {
            envLevels[ch] = envelope(ch);
            envTimes[ch] = time();
            holdings[ch] = false;
        }
        if (keyOffTicks[ch] < 0) keyOffTicks[ch] = ticks();
    }

    /** lets go of everything the channel is holding, the pedal included */
    private void releaseAll(int ch) {
        dampers[ch] = false;
        for (int note = 0; note < 128; note++) {
            if (keys[ch][note]) release(ch, note);
        }
    }

    private void allNotesOff() {
        for (int ch = 0; ch < CHANNELS; ch++) releaseAll(ch);
    }

    /**
     * A sequence's own text, where a {@link Sequencer} passes it on: its name and copyright fill
     * the comment lines the way a driver's metadata does, and its end of track ends the song. Not
     * every sequencer sends meta messages to its transmitters, so nothing here is relied on.
     */
    private void metaMessage(MetaMessage mm) {
        byte[] data = mm.getData();
        switch (mm.getType()) {
            case 0x03 -> { // sequence / track name
                if (comments[0] == null) comments[0] = new String(data);
            }
            case 0x02 -> { // copyright
                if (comments[1] == null) comments[1] = new String(data);
            }
            case 0x01 -> { // text
                if (comments[2] == null) comments[2] = new String(data);
            }
            case 0x2f -> playing = false; // end of track
            default -> { /* tempo and the rest: the clock runs off the wall clock */ }
        }
    }

    // ----- clock -----

    /** seconds of playback so far; frozen while paused, and never negative */
    private double time() {
        long base = baseNanos;
        if (base == 0) return 0;
        long now = paused ? pauseNanos : System.nanoTime();
        return Math.max(0, (now - base - pausedNanos) / 1e9);
    }

    /** the note length clock, see {@link #NOTE_TICK_HZ} */
    private long ticks() {
        return (long) (time() * NOTE_TICK_HZ);
    }

    // ----- FmDspDataSource -----

    @Override public FftDataSource fft() { return this; }

    @Override public LevelDataSource level() { return this; }

    @Override public TrackStatusSource trackStatus() { return this; }

    @Override public WorkStateSource work() { return this; }

    @Override public TrackDetailSource trackDetail() { return this; }

    // ----- FftDataSource -----

    /** every note that is sounding, at the level of the channel holding it */
    @Override
    public void readFft(int[] out) {
        spectrum.clear();
        for (int ch = 0; ch < CHANNELS; ch++) {
            double amplitude = spectral(ch);
            if (amplitude <= 0) continue;
            for (int note = 0; note < 128; note++) {
                if (!keys[ch][note]) continue;
                if (ch == DRUM_CHANNEL) spectrum.addDrum(note, amplitude);
                else spectrum.add(note, amplitude);
            }
        }
        spectrum.readFft(out);
    }

    // ----- LevelDataSource -----

    /**
     * Where the channel's meter stands: the level it was struck at, sagging while the key is held
     * and falling once it comes up, scaled by the volume and expression it is being played through
     * so that a fade drawn with CC7 or CC11 is a fade on the meter too.
     */
    private double amplitude(int ch) {
        return envelope(ch) * volumes[ch] / 127.0 * expressions[ch] / 127.0;
    }

    /**
     * What the channel puts into the spectrum, which is not what it puts into its meter.
     * <p>
     * A meter holds a sounding channel at a floor under the level it was struck at, because the
     * part is still playing and a meter that fell to nothing would say it had stopped. A spectrum
     * is the sound itself: almost everything a synthesizer plays gets quieter the longer it is
     * held, and a bar that stands where the note struck it - as this did until the movement was
     * called robotic - never comes back down for the next note to rise above. So the same struck
     * level decays here with {@link #SPECTRUM_TAU} and no floor under it, and the bars bounce with
     * the part instead of hanging off the ceiling.
     */
    private double spectral(int ch) {
        double dt = Math.max(0, time() - envTimes[ch]);
        double tau = holdings[ch] ? SPECTRUM_TAU : RELEASE_TAU;
        return envLevels[ch] * Math.exp(-dt / tau) * volumes[ch] / 127.0 * expressions[ch] / 127.0;
    }

    /** the bare envelope, before the channel's volume reaches it */
    private double envelope(int ch) {
        double dt = Math.max(0, time() - envTimes[ch]);
        double level = envLevels[ch];
        if (holdings[ch]) {
            return Math.max(level * Math.exp(-dt / SUSTAIN_TAU), level * SUSTAIN_LEVEL);
        }
        return level * Math.exp(-dt / RELEASE_TAU);
    }

    @Override
    public int level(int channel) {
        return channel < CHANNELS ? (int) (amplitude(channel) * 32767) : 0;
    }

    @Override
    public Pan pan(int channel) {
        return channel < CHANNELS ? panOf(pans[channel]) : Pan.CENTER;
    }

    /** controller 10: 0 hard left, 64 centre, 127 hard right */
    private static Pan panOf(int pan) {
        if (pan < 16) return Pan.LEFT;
        if (pan < 56) return Pan.MID_LEFT;
        if (pan <= 72) return Pan.CENTER;
        if (pan <= 112) return Pan.MID_RIGHT;
        return Pan.RIGHT;
    }

    /**
     * The channel over each meter column, {@code CH1} to {@code CH16} - and {@code DRM} over
     * channel 10, whose column happens to be the one the original labels {@code RHY}. The strip is
     * 19 columns wide, so the three past channel 16 stay blank.
     */
    @Override
    public String label(int channel) {
        if (channel >= CHANNELS) return null;
        if (channel == DRUM_CHANNEL) return "DRM";
        // a column is three characters wide before the labels start touching, so the two digit
        // channels lose the "H" rather than the digit
        return channel < 9 ? "CH" + (channel + 1) : "C" + (channel + 1);
    }

    @Override
    public TrackId track(int channel) {
        return channel < CHANNELS ? ROWS[channel] : null;
    }

    // ----- TrackStatusSource -----

    @Override
    public void readStatus(TrackId track, TrackStatus out) {
        int ch = CHANNEL_OF[track.ordinal()];
        clear(out);
        // a channel the song has not addressed at all shows nothing rather than its defaults: the
        // meter strip prints the tone number of every column, and a silent one saying "001" reads
        // like a part that is there
        if (ch < 0 || !touched[ch]) return;

        out.playing = used[ch];
        out.info = TrackInfo.NORMAL;
        out.key = keyOf(notes[ch]);
        out.actualKey = out.key;
        out.toneNum = programs[ch] + 1; // GM numbers its programs from 1
        out.volume = volumes[ch];
        out.detune = (bends[ch] - 8192) * BEND_RANGE / 8192;
        out.status = statusOf(ch);
        noteLength(ch, out);
    }

    /**
     * MML key: high nibble octave, low nibble note, {@code 0xff} while the channel is silent.
     * <p>
     * The octave is the note's less one, which is the PC-98 MML convention the keyboard was drawn
     * for - middle C, MIDI 60, is {@code o4c} - and it puts MIDI 12..107 on the eight octaves the
     * keyboard has, which is everything but the extremes of the range.
     */
    static int keyOf(int note) {
        if (note < 0) return 0xff;
        int octave = note / 12 - 1;
        return octave < 0 || octave > 7 ? 0xff : octave << 4 | note % 12;
    }

    /**
     * The eight character mnemonic fmdsp shows behind "M:", as much of it as MIDI says: the
     * modulation wheel is an LFO at the synthesizer - "P" for the pitch it moves and "H" for whose
     * LFO it is - and CC65 is a portamento. The software LFO slots belong to a driver's work area
     * and there is no driver here.
     */
    private String statusOf(int ch) {
        boolean lfo = modulations[ch] > 0 && notes[ch] >= 0;
        return new String(new char[] {
                lfo ? 'P' : '-', '-', '-', '-', '-', '-',
                lfo ? 'H' : '-',
                portamentos[ch] ? 'P' : '-',
        });
    }

    /**
     * The note length bar: {@link TrackStatus#ticks} is how long the channel's previous note ran,
     * which the renderer marks as the note's start, and {@link TrackStatus#ticksLeft} how much of
     * it the note now playing has left. A sequence carries a length for every note, but a receiver
     * does not see it, so - as for a chip - the length is measured between note-ons and the note
     * playing counts down against the one before it.
     */
    private void noteLength(int ch, TrackStatus out) {
        if (notes[ch] < 0) {
            out.ticks = 0;
            out.ticksLeft = 0;
            out.gate = 0;
            return;
        }
        int scale = barScale();
        out.gate = Math.min(BAR_FULL_SCALE, gates[ch] / scale);
        if (noteLengths[ch] == 0) {
            // the channel's first note: it is being held and how long it runs is not known yet
            out.ticks = BAR_FULL_SCALE;
            out.ticksLeft = BAR_FULL_SCALE;
            return;
        }
        long elapsed = keyOnTicks[ch] < 0 ? 0 : ticks() - keyOnTicks[ch];
        out.ticks = noteLengths[ch] / scale;
        out.ticksLeft = (int) Math.max(0, noteLengths[ch] - elapsed) / scale;
    }

    /**
     * What the note clock is divided by on its way to the bar: the smallest power of two that
     * brings the longest note now sounding inside the bar's 255 counts. One divider for every row,
     * so the rows stay comparable, and it follows the music because a channel that has gone quiet
     * no longer counts towards it.
     */
    private int barScale() {
        int longest = 0;
        for (int ch = 0; ch < CHANNELS; ch++) {
            if (notes[ch] >= 0) longest = Math.max(longest, noteLengths[ch]);
        }
        int scale = 1;
        while (longest / scale > BAR_FULL_SCALE) scale <<= 1;
        return scale;
    }

    private static void clear(TrackStatus out) {
        out.playing = false;
        out.info = TrackInfo.NORMAL;
        out.ticks = 0;
        out.ticksLeft = 0;
        out.key = 0xff;
        out.actualKey = 0xff;
        out.toneNum = 0;
        out.volume = 0;
        out.gate = 0;
        out.detune = 0;
        out.status = "--------";
        out.ppz8Ch = 0;
        out.ssgTone = false;
        out.ssgNoise = false;
        out.ssgNoiseFreq = 0;

        Arrays.fill(out.fmSlotMask, false);
    }

    /** every row is a MIDI channel, the drums saying so */
    @Override
    public String trackTypeName(TrackId track) {
        int ch = CHANNEL_OF[track.ordinal()];
        return ch < 0 ? null : ch == DRUM_CHANNEL ? "DRUM" : "MIDI";
    }

    /** the MIDI channel, 1 based */
    @Override
    public int trackNumber(TrackId track) {
        int ch = CHANNEL_OF[track.ordinal()];
        return ch < 0 ? -1 : ch + 1;
    }

    /** the channels that have sounded, in channel order; sixteen do not fit ten rows */
    @Override
    public TrackId[] displayTracks() {
        return dispTracks;
    }

    /** rebuilt when a channel sounds for the first time, not once a frame */
    private void updateDisplayTracks() {
        List<TrackId> list = new ArrayList<>(DISPLAY_ROWS);
        for (int ch = 0; ch < CHANNELS && list.size() < DISPLAY_ROWS; ch++) {
            if (used[ch]) list.add(ROWS[ch]);
        }
        dispTracks = list.isEmpty() ? null : list.toArray(new TrackId[0]);
    }

    // ----- TrackDetailSource -----

    /**
     * What {@link RightMode#TRACK_INFO} shows for a MIDI row: the channel's controllers, in the
     * titled dump shape the original uses for a track type that has registers rather than
     * operators. There are no envelopes to draw bars from - only the synthesizer knows those - so
     * the row is a dump and nothing else.
     */
    @Override
    public boolean readDetail(TrackId track, TrackDetail out) {
        int ch = CHANNEL_OF[track.ordinal()];
        if (ch < 0 || !touched[ch]) return false;

        out.lines = 0;
        out.header = "PRG VOL EXP VEL PAN  BEND MOD DMP POR KEYS";
        out.text = "%03d %03d %03d %03d %3s %+5d %03d %3s %3s %4d".formatted(
                programs[ch] + 1, volumes[ch], expressions[ch], velocities[ch], panText(pans[ch]),
                (bends[ch] - 8192) * BEND_RANGE / 8192, modulations[ch],
                dampers[ch] ? " ON" : "OFF", portamentos[ch] ? " ON" : "OFF", held(ch));
        return true;
    }

    /** how far the pan leans and which way, the way the original writes a PPZ8 one */
    private static String panText(int pan) {
        int offset = pan - 64;
        if (offset == 0) return "CTR";
        return "%c%02d".formatted(offset < 0 ? 'L' : 'R', Math.min(Math.abs(offset), 64));
    }

    /** keys down on the channel, a chord being more than one */
    private int held(int ch) {
        int count = 0;
        for (int note = 0; note < 128; note++) {
            if (keys[ch][note]) count++;
        }
        return count;
    }

    // ----- WorkStateSource -----

    /**
     * The clock, off the wall clock: MIDI is played in real time and nothing in the stream counts
     * samples. It never runs backwards, so a receiver handed a stream twice does not rewind.
     */
    @Override
    public long generatedFrames() {
        shownFrames = Math.max(shownFrames, (long) (time() * sampleRate()));
        return shownFrames;
    }

    private long shownFrames;

    /**
     * The TimerB tick the clock circle and the loop bar are counted in, rebuilt from the elapsed
     * time the way {@code OPNATimer} would: TimerB overflows every {@code (256 - timerB) * 16}
     * steps of the 55467 Hz step clock the elapsed time is counted in.
     */
    @Override
    public long timerBCount() {
        return generatedFrames() / ((256 - timerB()) * 16L);
    }

    /** nothing programs a timer here; the period the PC-98 drivers mostly used stands in for one */
    @Override
    public int timerB() {
        return 200;
    }

    @Override public int loopCount() { return 0; }

    @Override public long loopTimerBCount() { return 0; }

    @Override public long timerBCountLoop() { return timerBCount(); }

    @Override public boolean playing() { return playing; }

    @Override public boolean paused() { return paused; }

    @Override public String driverName() { return "MIDI"; }

    @Override public String filename() { return filename; }

    @Override public String comment(int line) { return comments[line]; }
}
