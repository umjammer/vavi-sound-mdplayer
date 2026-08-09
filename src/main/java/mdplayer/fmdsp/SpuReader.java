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
import mdplayer.driver.BaseDriver;
import mdplayer.driver.psf.PsfDriver;
import mdplayer.driver.psf2.Psf2Driver;
import mdplayer.emu.psx.SpuVoices;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;
import vavi.sound.visualizer.fmdsp.TrackDetail;


/**
 * The PlayStation's SPU and the PS2's SPU2, as the psf and psf2 drivers play them.
 * <p>
 * Both drivers render their own audio and register no chip, so the voices come from the emulated
 * SPU on the driver. One reader serves both because the voice is the same either way - the PS2
 * simply has two of the PS1's cores, 48 voices against 24 - and only one of the two can be
 * playing at a time.
 * <p>
 * Key on edges come from the chip's own key on counters rather than from a change in the
 * registers, so that a vibrato is not mistaken for a new note.
 * <p>
 * Every SPU voice is a sampled one: the chip knows a playback rate and not a note, so the key
 * shown is the note that rate comes closest to, {@code 0x1000} being the sample's own rate, i.e.
 * o4 c. Its level is the ADSR envelope the chip is actually at rather than the volume the
 * registers ask for, which is why it attacks and releases with the note.
 * <p>
 * There are more voices than the PCM rows can show, so they are taken in the order they first
 * sound, the way {@link C352Reader} does with its thirty two.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class SpuReader implements FmDspChipReader {

    /** the most voices either chip has, which is the PS2's two cores */
    private static final int MAX_VOICES = 48;

    /** the envelope tops out here, and its top three bits are what {@code MixADSR} scales by */
    private static final double envelopeMax = 0x7fffffff;

    /** a voice's own volume registers run to this */
    private static final double volumeMax = 0x3fff;

    /** the pitch that plays a sample at the rate it was recorded at */
    private static final double pitchUnity = 0x1000;

    private Supplier<BaseDriver> driver;

    private SpuVoices spu;

    /** the key on count each voice was last seen at, so a note struck again shows as one */
    private final int[] prevKeyOns = new int[MAX_VOICES];

    private boolean active;

    /** voice shown on each row slot, -1 = none yet; the voices claim rows as they first sound */
    private final int[] slotVoices = new int[MAX_VOICES];

    private int mappedVoices;

    @Override
    public String chipName() {
        return spu != null && spu.voiceCount() > 24 ? "SPU2" : "SPU";
    }

    @Override
    public void bind(ChipRegister chipRegister) {
    }

    @Override
    public void bind(Supplier<BaseDriver> driver) {
        this.driver = driver;
    }

    @Override
    public void reset() {
        Arrays.fill(prevKeyOns, 0);
        Arrays.fill(slotVoices, -1);
        mappedVoices = 0;
        active = false;
        spu = null;
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.PCM);
    }

    @Override
    public int priority() {
        return 28;
    }

    @Override
    public boolean ready() {
        return spuOf() != null;
    }

    private SpuVoices spuOf() {
        if (driver == null) {
            return null;
        }
        BaseDriver d = driver.get();
        if (d instanceof PsfDriver psf) {
            return psf.getSpu();
        }
        if (d instanceof Psf2Driver psf2) {
            return psf2.getSpu();
        }
        return null;
    }

    @Override
    public void poll() {
        spu = spuOf();
        if (spu == null) {
            return;
        }
        for (int v = 0; v < voices() && mappedVoices < slotVoices.length; v++) {
            if (spu.on(v) && slotOf(v) < 0) {
                slotVoices[mappedVoices++] = v;
            }
        }
    }

    private int voices() {
        return spu == null ? 0 : Math.min(spu.voiceCount(), MAX_VOICES);
    }

    private int slotOf(int voice) {
        for (int s = 0; s < mappedVoices; s++) {
            if (slotVoices[s] == voice) return s;
        }
        return -1;
    }

    @Override
    public boolean active(Group group) {
        if (!active && spu != null) {
            for (int v = 0; v < voices() && !active; v++) active = spu.on(v);
        }
        return active;
    }

    @Override
    public int channels(Group group) {
        // the chip is not there yet before the first snapshot, and the strip wants a width now
        return spu == null ? 24 : voices();
    }

    @Override
    public void read(Group group, int slot, FmDspChannel out) {
        out.name = "PCM";
        if (spu == null || slot >= slotVoices.length) {
            return;
        }
        int v = slotVoices[slot];
        if (v < 0 || v >= voices()) {
            return;
        }

        boolean on = spu.on(v);
        int start = spu.sampleStart(v);
        int pitch = spu.pitch(v);
        int keyOns = spu.keyOnCount(v);

        out.num = v + 1; // the chip voice, wherever the slot map put it
        out.pcmCh = v + 1;
        out.sampled = true;
        // the sample address is the nearest thing the chip has to an instrument; the low bits of
        // its own eight byte addressing unit tell two samples apart without running off the column
        out.toneNum = (start >>> 3) & 0xff;

        out.sounding = on;
        // the chip's own key ons rather than a change in the registers, so that a vibrato does
        // not read as a new note and a note struck again at the same pitch still does
        out.keyOn = on && keyOns != prevKeyOns[v];
        prevKeyOns[v] = keyOns;

        int left = spu.leftVolume(v);
        int right = spu.rightVolume(v);
        int level = spu.envelopeLevel(v);

        // the level is the envelope the chip is at, so the meter must not be enveloped again
        out.measured = true;
        out.volume = (int) (level / envelopeMax * 127);
        out.amplitude = on
                ? level / envelopeMax * Math.max(Math.abs(left), Math.abs(right)) / volumeMax : 0;
        out.pan = panOf(left, right);

        // the noise generator has no pitch worth showing
        if (on && pitch > 0 && !spu.noise(v)) {
            out.pitchOfRatio(pitch / pitchUnity);
        } else {
            out.note = -1;
        }
    }

    /** a voice's two volumes are signed, and a phase inverted side is still that side */
    private static Pan panOf(int l, int r) {
        int left = Math.abs(l);
        int right = Math.abs(r);
        if (left == 0 && right == 0) return Pan.NONE;
        if (right == 0) return Pan.LEFT;
        if (left == 0) return Pan.RIGHT;
        double balance = right / (double) (left + right);
        if (balance < 0.25) return Pan.LEFT;
        if (balance < 0.45) return Pan.MID_LEFT;
        if (balance <= 0.55) return Pan.CENTER;
        if (balance <= 0.75) return Pan.MID_RIGHT;
        return Pan.RIGHT;
    }

    @Override
    public boolean readDetail(Group group, int slot, TrackDetail out) {
        if (spu == null || slot >= slotVoices.length) {
            return false;
        }
        int v = slotVoices[slot];
        if (v < 0 || v >= voices()) {
            return false;
        }

        out.header = "ENV  ADSR   VOL-L VOL-R PITCH    START     LOOP";
        out.text = " %-3s %04X   %04X  %04X  %04X  %06X   %06X".formatted(
                phaseOf(v),
                (spu.envelopeLevel(v) >>> 16) & 0xffff,
                spu.leftVolume(v) & 0xffff,
                spu.rightVolume(v) & 0xffff,
                spu.pitch(v) & 0xffff,
                spu.sampleStart(v) & 0xffffff,
                spu.sampleLoop(v) & 0xffffff);
        return true;
    }

    /** the envelope phase, the way the original writes an FM operator's */
    private String phaseOf(int v) {
        if (!spu.on(v)) return "OFF";
        if (spu.noise(v)) return "NOI";
        return switch (spu.envelopePhase(v)) {
            case SpuVoices.ATTACK -> "ATT";
            case SpuVoices.DECAY -> "DEC";
            case SpuVoices.SUSTAIN -> "SUS";
            default -> "REL";
        };
    }
}
