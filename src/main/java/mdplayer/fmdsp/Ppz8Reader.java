/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import mdplayer.ChipRegister;
import mdplayer.chips.Ppz8Chip;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;
import vavi.sound.visualizer.fmdsp.TrackInfo;


/**
 * PPZ8: eight channels from the chip's info view. The chip has no note, only a playback rate, so
 * the key is the note whose pitch that rate comes closest to.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public class Ppz8Reader implements FmDspChipReader {

    private ChipRegister chipRegister;

    private Map<String, Object> info;
    private final boolean[] prevKeyOns = new boolean[8];
    private final int[] prevNotes = new int[8];
    private boolean active;

    private Ppz8Chip chip() {
        return chipRegister.chip(Ppz8Chip.class);
    }

    @Override
    public String chipName() {
        return "PPZ8";
    }

    @Override
    public void bind(ChipRegister chipRegister) {
        this.chipRegister = chipRegister;
    }

    @Override
    public void reset() {
        info = Collections.emptyMap();
        Arrays.fill(prevKeyOns, false);
        Arrays.fill(prevNotes, -1);
        active = false;
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.PCM);
    }

    @Override
    public int priority() {
        return 55;
    }

    @Override
    public boolean ready() {
        return chipRegister != null && chip() != null;
    }

    @Override
    public void poll() {
        try {
            info = chip().getInfo(0);
        } catch (RuntimeException ignore) {
            // the chip exists but was never initialized for this song
            info = Collections.emptyMap();
        }
        if (!info.containsKey("channels.0.playing")) info = Collections.emptyMap();
    }

    @Override
    public boolean active(Group group) {
        if (!active && !info.isEmpty()) {
            for (int ch = 0; ch < 8 && !active; ch++) {
                active = (boolean) info.get("channels." + ch + ".playing")
                        || (boolean) info.get("channels." + ch + ".keyOn");
            }
        }
        return active;
    }

    @Override
    public int channels(Group group) {
        return 8;
    }

    @Override
    public void read(Group group, int ch, FmDspChannel out) {
        if (info.isEmpty()) return;

        boolean playing = (boolean) info.get("channels." + ch + ".playing");
        boolean keyOn = (boolean) info.get("channels." + ch + ".keyOn");
        int volume = (int) info.get("channels." + ch + ".volume");
        int pan = (int) info.get("channels." + ch + ".pan");
        int note = playing
                ? Notes.noteOfRatio((int) info.get("channels." + ch + ".frequency") / (double) 0x8000)
                : -1;

        out.sounding = playing;
        out.keyOn = keyOn && (!prevKeyOns[ch] || note != prevNotes[ch]);
        prevKeyOns[ch] = keyOn;
        prevNotes[ch] = note;

        out.name = "PPZ8";
        out.num = ch + 1;
        out.info = TrackInfo.PPZ8;
        out.pcmCh = ch + 1;
        out.note = note;
        out.volume = volume;
        out.toneNum = (int) info.get("channels." + ch + ".flg16");
        // one step of PPZ8's 16 level table is 1.5 dB
        out.amplitude = playing ? Math.pow(10, (Math.min(volume, 15) - 15) * 1.5 / 20) : 0;
        out.pan = panOf(pan);
    }

    /** PPZ8 pans over 0..9, 5 being the centre. 0 is a silent channel, the chip skips it */
    private static Pan panOf(int pan) {
        return switch (pan) {
            case 0 -> Pan.NONE;
            case 1, 2 -> Pan.LEFT;
            case 3, 4 -> Pan.MID_LEFT;
            case 5 -> Pan.CENTER;
            case 6, 7 -> Pan.MID_RIGHT;
            default -> Pan.RIGHT;
        };
    }

    @Override
    public boolean masked(Group group, int ch) {
        return chip().getMask(0, ch);
    }
}
