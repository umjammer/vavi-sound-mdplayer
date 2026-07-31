/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;

import mdplayer.Common.EnmModel;
import mdplayer.MIDIExport;
import mdplayer.MIDIParam;
import mdplayer.MidiOutInfo;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;

import static java.lang.System.getLogger;
import static mdplayer.driver.BasePlugin.BUFFER_SIZE;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MidiPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MidiPlugin implements Plugin {

    private static final Logger logger = getLogger(MidiPlugin.class.getName());

    public int midiMode = 0;

    public final MIDIParam[] params = {null, null};

    public MIDIExport export;

    private MidiOutInfo[] outInfos = null;

    public final MDSound mds;

    private final List<Receiver> outs = new ArrayList<>();
    private final List<Integer> outsType = new ArrayList<>();

    protected short[] bufVirtualFunction_MIDIKeyboard = null;

    private BasePlugin<? extends BaseDriver> context;

    /** the software synthesizer opened as a fallback when no MIDI out is configured */
    private Synthesizer fallbackSynth;

    public MidiPlugin() {
        mds = new MDSound();
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        this.context = context;

        export = new MIDIExport();
        export.registerYM2612 = context.chipRegister.chip(Ym2612Chip.class).register;
        export.registerYM2151 = context.chipRegister.chip(Ym2151Chip.class).register;

        for (int chipId = 0; chipId < 2; chipId++) {
            params[chipId] = new MIDIParam();
        }
    }

    /** */
    public void prepare() {
        mdsInit();
        resetAll();
    }

    @Override
    public void close() {
        // whatever is still keyed on would otherwise be left sounding at an out we no longer hold
        allSoundOff();
        export.close();
        if (fallbackSynth != null && fallbackSynth.isOpen()) {
            fallbackSynth.close();
        }
        fallbackSynth = null;
    }

    // ???
    public void initChipRegisterNSF() {
        for (int chipId = 0; chipId < 2; chipId++) {
            params[chipId] = new MIDIParam();
        }
    }

    public MidiOutInfo[] get() {
        return outInfos;
    }

    public void set(MidiOutInfo[] midiOutInfos) {
        this.outInfos = null;
        if (midiOutInfos != null && midiOutInfos.length > 0) {
            this.outInfos = new MidiOutInfo[midiOutInfos.length];
            for (int i = 0; i < midiOutInfos.length; i++) {
                this.outInfos[i] = new MidiOutInfo();
                this.outInfos[i].beforeSendType = midiOutInfos[i].beforeSendType;
                this.outInfos[i].fileName = midiOutInfos[i].fileName;
                this.outInfos[i].id = midiOutInfos[i].id;
                this.outInfos[i].isVST = midiOutInfos[i].isVST;
                this.outInfos[i].manufacturer = midiOutInfos[i].manufacturer;
                this.outInfos[i].name = midiOutInfos[i].name;
                this.outInfos[i].type = midiOutInfos[i].type;
                this.outInfos[i].vendor = midiOutInfos[i].vendor;
            }
        }
//        VstMng.vstMidiOuts = vstMidiOuts;
//        this.vstMidiOutsType = vstMidiOutsType;

        if (params == null && params.length < 1) return;
//        if (outsType == null && vstMng.vstMidiOutsType == null) return;
//        if (outs == null && vstMng.vstMidiOuts == null) return;

        if (!outsType.isEmpty()) params[0].MIDIModule = Math.min(outsType.get(0), 2);
        if (outsType.size() > 1) params[1].MIDIModule = Math.min(outsType.get(1), 2);

//        if (vstMng.vstMidiOutsType.size() > 0) {
//            if (outsType.size() < 1 || (outsType.size() > 0 && outs.get(0) == null))
//                params[0].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(0), 2);
//        }
//        if (vstMng.vstMidiOutsType.size() > 1) {
//            if (outsType.size() < 2 || (outsType.size() > 1 && outs.get(1) == null))
//                params[1].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(1), 2);
//        }
    }

    public void setFileName(String fn) {
        export.playingFileName = fn;
    }

    public int getCount() {
        if (outs == null)
            return 0;
        return outs.size();
    }

    public void send(EnmModel model, int num, byte cmd, byte prm1, byte prm2, int deltaFrames /* = 0 */) {
        send(model, num, new byte[] {cmd, prm1, prm2}, deltaFrames);
    }

    public void send(EnmModel model, int num, byte cmd, byte prm1, int deltaFrames /* = 0 */) {
        send(model, num, new byte[] {cmd, prm1}, deltaFrames);
    }

    public void send(EnmModel model, int num, byte[] data, int deltaFrames /* = 0 */) {
        // In the original, VirtualModel is routed to VST software synths. VST is not ported,
        // so both models are sent to the (possibly software) MIDI out here.
        if (outs.isEmpty()) return;
        if (num >= outs.size()) return;
        Receiver out = outs.get(num);
        if (out == null) return;

        // Some drivers (e.g. ZMS emulating an X68000 MIDI board) emit a raw MIDI byte stream one
        // byte at a time using running status, others (e.g. RCP) emit complete messages; a per
        // receiver stream parser assembles both into whole MidiMessages before dispatching them.
        parsers.computeIfAbsent(out, MidiStreamParser::new).feed(data);
        if (num < params.length) params[num].sendBuffer(data);

//        vstMng.sendMIDIout(model, num, data, deltaFrames);
    }

    /**
     * The gain the balance asks the MIDI side to play at, as a linear factor.
     * <p>
     * Read fresh every time, so the mixer's master slider moves a MIDI song while it plays.
     *
     * @see Setting.Balance#getMidiVolume()
     */
    private double midiGain() {
        Setting.Balance balance = setting.getBalance();
        return Math.pow(10.0, (balance.getMasterVolume() + balance.getMidiVolume()) / 40.0);
    }

    /**
     * The channel volume actually sent for a song's {@code value}.
     * <p>
     * Scaling the controller is the only volume control that works for both the software
     * synthesizer and a real MIDI port; it is applied linearly to the controller value, which is
     * an approximation - what a given synthesizer makes of controller 7 is up to the synthesizer.
     * Never scaled to zero: a song that asked to be heard stays audible.
     */
    private int scaleVolume(int value) {
        if (value <= 0) return 0; // the song is silencing the channel, leave it silenced
        return (int) Math.clamp(Math.round(value * midiGain()), 1, 127);
    }

    /**
     * Re-sends every channel's volume at the current {@link #midiGain}.
     * <p>
     * Sent when the outs are made, so a song that never touches controller 7 is still played at
     * the calibrated level, and again whenever the volume changes under a playing song.
     */
    public void applyVolume() {
        if (outs.isEmpty()) return;
logger.log(Level.DEBUG, "midi volume: gain=%.3f (master=%d, midi=%d)".formatted(
        midiGain(), setting.getBalance().getMasterVolume(), setting.getBalance().getMidiVolume()));
        for (Receiver out : outs) {
            if (out == null) continue;
            for (int ch = 0; ch < MIDI_CHANNELS; ch++) {
                try {
                    send(out, ShortMessage.CONTROL_CHANGE, ch, 7, scaleVolume(volumes[ch]));
                } catch (InvalidMidiDataException | IllegalStateException e) {
                    logger.log(Level.DEBUG, "apply volume: " + e.getMessage());
                }
            }
        }
    }

    /** the sixteen MIDI channels, as the last note on left them */
    private static final int MIDI_CHANNELS = 16;

    private final int[] notes = new int[MIDI_CHANNELS];
    private final int[] velocities = new int[MIDI_CHANNELS];
    private final int[] programs = new int[MIDI_CHANNELS];
    private final int[] volumes = new int[MIDI_CHANNELS];
    private final int[] pans = new int[MIDI_CHANNELS];

    {
        java.util.Arrays.fill(volumes, 100); // the General MIDI default
        java.util.Arrays.fill(pans, 64);
    }

    /**
     * Remembers what the channel is playing.
     * <p>
     * A MIDI driver emulates no chip and the synth it plays into reports nothing back, so this is
     * the only view of the music there is. It records the last note on of each channel, its
     * program, and the two controllers a display cares about.
     * <p>
     * Fed from {@link MidiStreamParser#emit}, not from the bytes handed to {@link #send}: a driver
     * emulating a hardware MIDI port (ZMS, MID) writes one byte at a time and leans on running
     * status, so the raw buffer often holds no status byte at all and frequently just one byte.
     * Only the parser knows which message those bytes belong to.
     */
    private void observe(int status, int len, int d1, int d2) {
        int ch = status & 0x0f;
        switch (status & 0xf0) {
            case 0x90 -> { // note on, or note off when the velocity is zero
                if (len < 2) return;
                if (d2 == 0) {
                    if (d1 == notes[ch]) velocities[ch] = 0;
                } else {
                    notes[ch] = d1;
                    velocities[ch] = d2;
                }
            }
            case 0x80 -> { // note off
                if (len < 1) return;
                if (d1 == notes[ch]) velocities[ch] = 0;
            }
            case 0xb0 -> { // controllers: 7 is the volume, 10 the pan
                if (len < 2) return;
                if (d1 == 7) volumes[ch] = d2;
                if (d1 == 10) pans[ch] = d2;
            }
            case 0xc0 -> {
                if (len < 1) return;
                programs[ch] = d1;
            }
            default -> {
            }
        }
    }

    /** what each channel is playing, for the visualizer */
    public int note(int ch) {
        return notes[ch];
    }

    public int velocity(int ch) {
        return velocities[ch];
    }

    public int program(int ch) {
        return programs[ch];
    }

    public int volume(int ch) {
        return volumes[ch];
    }

    public int pan(int ch) {
        return pans[ch];
    }

    /**
     * Silences every channel of every MIDI out.
     * <p>
     * Stopping a MIDI driver only stops it sending: whatever it had keyed on is still held down at
     * the synthesizer, which keeps sounding a chord for as long as the note is sustained - a chip
     * driver has no equivalent because stopping the emulation stops the sound with it. Sent on
     * stop, so the song ends when the player says it ends.
     */
    public void allSoundOff() {
        if (outs == null) return;
        for (Receiver out : outs) {
            if (out == null) continue;
            for (int ch = 0; ch < MIDI_CHANNELS; ch++) {
                try {
                    send(out, ShortMessage.CONTROL_CHANGE, ch, 64, 0); // release the damper first
                    send(out, ShortMessage.CONTROL_CHANGE, ch, 120, 0); // all sound off
                    send(out, ShortMessage.CONTROL_CHANGE, ch, 123, 0); // all notes off
                    // Those two controllers are what a synthesizer is supposed to answer, and the
                    // OPL3 one this falls back to does not: it kept sounding the last chord of
                    // every song. A note off for every key is what it does answer.
                    for (int note = 0; note < 128; note++) {
                        send(out, ShortMessage.NOTE_OFF, ch, note, 0);
                    }
                } catch (InvalidMidiDataException | IllegalStateException e) {
                    logger.log(Level.DEBUG, "all sound off: " + e.getMessage());
                }
            }
        }
        java.util.Arrays.fill(velocities, 0);
    }

    private static void send(Receiver out, int command, int channel, int data1, int data2)
            throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(command, channel, data1, data2);
        out.send(sm, -1);
    }

    /**
     * How many MIDI messages have gone out since the plugin was made. A MIDI driver renders no
     * audio of its own, so this is how a test tells "playing" from "not playing at all".
     */
    private long sentMessages;

    public long sentMessages() {
        return sentMessages;
    }

    /**
     * How many voices the fallback synthesizer is still sounding, {@code -1} when there is no
     * fallback synthesizer. The only way to tell whether a song was really silenced: the notes go
     * out to a synthesizer that otherwise reports nothing back.
     */
    public int activeVoices() {
        if (fallbackSynth == null || !fallbackSynth.isOpen()) return -1;
        var voices = fallbackSynth.getVoiceStatus();
        if (voices == null) return -1; // the OPL3 synthesizer does not report its voices
        int active = 0;
        for (var voice : voices) {
            if (voice.active) active++;
        }
        return active;
    }

    /** forgets the last song's notes; the channels are shared between songs */
    public void clearChannels() {
        java.util.Arrays.fill(notes, 0);
        java.util.Arrays.fill(velocities, 0);
        java.util.Arrays.fill(programs, 0);
        java.util.Arrays.fill(volumes, 100);
        java.util.Arrays.fill(pans, 64);
    }

    /** stream parser per receiver, keyed by receiver identity */
    private final Map<Receiver, MidiStreamParser> parsers = new HashMap<>();

    /**
     * Assembles a raw MIDI byte stream (possibly delivered one byte at a time using running
     * status) into complete {@link MidiMessage}s and forwards them to a {@link Receiver}, the
     * way a hardware MIDI out port consumes bytes.
     */
    class MidiStreamParser {
        private final Receiver receiver;
        private int status = 0; // current running status byte, 0 = none
        private final byte[] data = new byte[2];
        private int dataIndex = 0;
        private int dataNeeded = 0;
        private boolean inSysex = false;
        private final ByteArrayOutputStream sysex = new ByteArrayOutputStream();

        MidiStreamParser(Receiver receiver) {
            this.receiver = receiver;
        }

        void feed(byte[] bytes) {
            for (byte b : bytes) feed(b & 0xff);
        }

        private void feed(int b) {
            if (b >= 0xf8) { // system real-time: single byte, may interleave anywhere
                emit(b, 0, 0, 0);
                return;
            }
            if (b == 0xf0) { // sysex start
                inSysex = true;
                sysex.reset();
                sysex.write(0xf0);
                status = 0;
                return;
            }
            if (b == 0xf7) { // end of exclusive
                if (inSysex) {
                    sysex.write(0xf7);
                    emitSysex();
                    inSysex = false;
                }
                return;
            }
            if (b >= 0x80) { // other status byte (channel voice or system common)
                inSysex = false; // a status byte aborts an unfinished sysex
                status = b;
                dataIndex = 0;
                dataNeeded = dataLength(b);
                if (dataNeeded == 0) { // e.g. tune request 0xf6
                    emit(status, 0, 0, 0);
                    status = 0;
                }
                return;
            }
            // data byte (< 0x80)
            if (inSysex) {
                sysex.write(b);
                return;
            }
            if (status == 0) return; // data byte without a status: ignore
            data[dataIndex++] = (byte) b;
            if (dataIndex >= dataNeeded) {
                emit(status, dataNeeded, data[0] & 0xff, data[1] & 0xff);
                dataIndex = 0; // running status: keep status for the next message
                if (status >= 0xf0) status = 0; // system common is not retained as running status
            }
        }

        private static int dataLength(int status) {
            return switch (status & 0xf0) {
                case 0x80, 0x90, 0xa0, 0xb0, 0xe0 -> 2;
                case 0xc0, 0xd0 -> 1;
                default -> switch (status) { // 0xf1..0xf6
                    case 0xf1, 0xf3 -> 1;
                    case 0xf2 -> 2;
                    default -> 0; // 0xf6 tune request and others
                };
            };
        }

        private void emit(int status, int len, int d1, int d2) {
            observe(status, len, d1, d2); // observed unscaled: the display shows what the song asked for
            sentMessages++;
            try {
                ShortMessage sm = new ShortMessage();
                switch (len) {
                    case 1 -> sm.setMessage(status, d1, 0);
                    // the song setting a channel volume is where the balance gets applied
                    case 2 -> sm.setMessage(status,
                            d1, (status & 0xf0) == 0xb0 && d1 == 7 ? scaleVolume(d2) : d2);
                    default -> sm.setMessage(status);
                }
                receiver.send(sm, -1);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.WARNING, "invalid midi data: status=" + Integer.toHexString(status));
            }
        }

        private void emitSysex() {
            try {
                byte[] d = sysex.toByteArray();
                SysexMessage sx = new SysexMessage();
                sx.setMessage(d, d.length);
                receiver.send(sx, -1);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.WARNING, "invalid sysex: " + e.getMessage());
            }
        }
    }

    public void resetAll() {
        if (outs != null) {
            for (Receiver midiOut : outs) {
                if (midiOut == null)
                    continue;
                midiOut.close(); // TODO
            }
        }

//        vstMng.resetAllMIDIout(EnmModel.VirtualModel);
    }

    public void softReset(int chipId, EnmModel model) {
        resetAll();
    }

    public void mdsInit() {
        List<MDSound.Chip> infos = new ArrayList<>();
        //
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Ym2612Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = Plugin.setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
        chip.clock = 7670454;
        chip.option = null;
        context.getDriver().fireEventHappened(context.chipRegister.chip(Ym2612Chip.class), "led.set");
        infos.add(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Sn76489Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class);
        chip.clock = 3579545;
        chip.option = null;
        context.getDriver().fireEventHappened(context.chipRegister.chip(Sn76489Chip.class), "led.set");
        infos.add(chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, infos);

        // Creates a midi instance.
        midiMode = 1;
        make();
    }

    public void make() {
        List<MidiOutInfo[]> midiOutInfos = setting.getMidiOut().getMidiOutInfos();
        if (midiOutInfos == null || midiOutInfos.isEmpty()
                || midiOutInfos.get(midiMode) == null || midiOutInfos.get(midiMode).length < 1) {
            // No MIDI out is configured (e.g. no GUI): fall back to the software synthesizer
            // (Gervill) so drivers that emit MIDI can still be heard. Note: MidiSystem.getReceiver()
            // returns the platform default MIDI out (often a silent hardware port), so open the
            // Synthesizer explicitly to render audio.
            try {
                if (fallbackSynth == null) {
                    fallbackSynth = MidiSystem.getSynthesizer();
                }
                if (!fallbackSynth.isOpen()) {
                    fallbackSynth.open();
                }
                Receiver r = fallbackSynth.getReceiver();
                outs.add(r);
                outsType.add(0);
                logger.log(Level.INFO, "MIDI out fallback to the software synthesizer: " + fallbackSynth.getDeviceInfo().getName());
            } catch (MidiUnavailableException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            applyVolume();
            return;
        }

        for (int i = 0; i < setting.getMidiOut().getMidiOutInfos().get(midiMode).length; i++) {
            int t = 0;
            Receiver mo = null;
            MidiDevice found = null;

            MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
            for (var info : midiDeviceInfos) {
                MidiDevice device;
                try {
                    device = MidiSystem.getMidiDevice(info);
                } catch (MidiUnavailableException e) {
                    throw new RuntimeException(e);
                }
                if (device.getMaxReceivers() == 0) {
                    continue;
                }
                if (!setting.getMidiOut().getMidiOutInfos().get(midiMode)[i].name.equals(info.getName()))
                    continue;

                found = device;
                t = setting.getMidiOut().getMidiOutInfos().get(midiMode)[i].type;
                break;
            }

            if (found != null) {
                try {
                    // the receiver of the device that was just matched by name - not
                    // MidiSystem.getReceiver(), which hands out the platform default out
                    // (often a port with nothing behind it, so the song plays into silence)
                    if (!found.isOpen()) found.open();
                    mo = found.getReceiver();
                } catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    mo = null;
                }
            }

//            if (n == -1) {
//                vstMng.SetupVstMidiOut(setting.getMidiOut().getMidiOutInfos().get(m)[i]);
//            }

            if (mo != null) {
                outs.add(mo);
                outsType.add(t);
            }
        }

        applyVolume();
    }

//    public static final VstMng vstMng = new VstMng();

    public void releaseAll() {
        // before the outs go: closing a receiver does not silence the synthesizer behind it, and
        // the fallback one outlives every song, so a note still held here would ring forever with
        // nothing left to send it a note off
        allSoundOff();
        if (!outs.isEmpty()) {
            for (int i = 0; i < outs.size(); i++) {
                if (outs.get(i) != null) {
                    outs.get(i).close();
                    outs.set(i, null);
                }
            }
            outs.clear();
            outsType.clear();
        }
        parsers.clear();

//        vstMng.ReleaseAllMIDIout();
    }

    public void midiClose() {
        // release the midi out
        if (!outs.isEmpty()) {
            for (int i = 0; i < outs.size(); i++) {
                if (outs.get(i) != null) {
                    outs.get(i).close();
                    outs.set(i, null);
                }
            }
            outs.clear();
            outsType.clear();
        }
        parsers.clear();

//        vstMng.ReleaseAllMIDIout();
//        vstMng.Close();
    }

    public int[][] readYM2612() {
        Instrument inst = mds.inst(Ym2612Inst.class, 0);
        if (inst == null) return null;
        return (int[][]) inst.getView(0, "registers").get("Ym2612");
    }

    public MIDIParam get(int chipId) {
        return params[chipId];
    }

    public void softReset(EnmModel model) {
        softReset(0, model);
        softReset(1, model);
    }

    public void keyboard(short[] buffer, int offset, int sampleCount) {
        if (bufVirtualFunction_MIDIKeyboard == null || bufVirtualFunction_MIDIKeyboard.length < sampleCount) {
            bufVirtualFunction_MIDIKeyboard = new short[sampleCount];
        }
        mds.update(bufVirtualFunction_MIDIKeyboard, 0, sampleCount, null);
        for (int i = 0; i < sampleCount; i++) {
            buffer[i + offset] += bufVirtualFunction_MIDIKeyboard[i];
        }
    }
}
