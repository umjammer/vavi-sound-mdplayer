/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import mdplayer.ChipRegister;
import mdplayer.chips.BaseChip;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * The shape nearly every sampled chip here has: some number of channels on the PCM rows, each with
 * a playing flag, a volume, a pan and a rate, read out of the chip's own {@code getInfo}.
 * <p>
 * A subclass says where the chip is and how to read one channel out of the map. What it gets in
 * return is the row claiming - chips with more channels than there are rows hand them out in the
 * order they first sound - and the key on edges, found by watching the playing flag rather than
 * trusting a key on bit that stays set long after the sound has gone.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
public abstract class PcmSlotReader implements FmDspChipReader {

    protected ChipRegister chipRegister;

    /** the chip's own state, read back once a frame */
    protected Map<String, Object> info;

    private boolean[] prevSoundings;
    private int[] prevRates;
    private boolean active;

    /** chip channel shown on each row slot, -1 = none yet */
    private int[] slotChannels;

    private int mappedChannels;

    /** how many channels the chip has */
    protected abstract int channelCount();

    protected abstract BaseChip chip();

    /** whether the channel is sounding, as the chip has it now */
    protected abstract boolean sounding(int ch);

    /** fills in everything but the row bookkeeping - the pitch, volume, pan and tone number */
    protected abstract void readChannel(int ch, FmDspChannel out);

    /**
     * A number standing for the channel's pitch, whatever the chip measures it in. It is only
     * compared with itself, to spot a key on where a channel is re-struck without stopping.
     */
    protected abstract int rateOf(int ch);

    protected abstract boolean muted(int ch);

    @Override
    public void bind(ChipRegister chipRegister) {
        this.chipRegister = chipRegister;
        reset();
    }

    /** the source resets every reader as it is built, which is before any of them are bound */
    private void allocate() {
        if (slotChannels != null) return;
        slotChannels = new int[channelCount()];
        prevSoundings = new boolean[channelCount()];
        prevRates = new int[channelCount()];
    }

    @Override
    public void reset() {
        allocate();
        Arrays.fill(prevSoundings, false);
        Arrays.fill(prevRates, 0);
        Arrays.fill(slotChannels, -1);
        mappedChannels = 0;
        active = false;
        info = null;
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.PCM);
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
            // the chip exists but the song never loaded it
            info = null;
        }
        for (int ch = 0; ch < channelCount() && mappedChannels < slotChannels.length; ch++) {
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

    @Override
    public boolean active(Group group) {
        if (!active) {
            for (int ch = 0; ch < channelCount() && !active; ch++) active = sounding(ch);
        }
        return active;
    }

    @Override
    public int channels(Group group) {
        return channelCount();
    }

    @Override
    public void read(Group group, int slot, FmDspChannel out) {
        out.name = "PCM";
        int ch = slotChannels[slot];
        if (ch < 0 || info == null) return;

        boolean sounding = sounding(ch);
        int rate = rateOf(ch);
        out.num = ch + 1; // the chip channel, wherever the slot map put it
        out.pcmCh = ch + 1;
        out.sounding = sounding;
        out.keyOn = sounding && (!prevSoundings[ch] || rate != prevRates[ch]);
        prevSoundings[ch] = sounding;
        prevRates[ch] = rate;

        readChannel(ch, out);
        if (!sounding) {
            out.note = -1;
            out.amplitude = 0;
        }
    }

    @Override
    public boolean masked(Group group, int slot) {
        int ch = slotChannels[slot];
        return ch >= 0 && muted(ch);
    }

    // helpers for reading the chip's map

    protected int intOf(int ch, String field, int fallback) {
        Object value = info.get("channels." + ch + "." + field);
        return value instanceof Integer i ? i : fallback;
    }

    protected boolean boolOf(int ch, String field) {
        Object value = info.get("channels." + ch + "." + field);
        return value instanceof Boolean b && b;
    }

    /** a left and right level of any depth, as one of the pans the display has */
    protected static Pan panOf(int l, int r) {
        if (l == 0 && r == 0) return Pan.NONE;
        if (r == 0) return Pan.LEFT;
        if (l == 0) return Pan.RIGHT;
        double balance = r / (double) (l + r);
        if (balance < 0.25) return Pan.LEFT;
        if (balance < 0.45) return Pan.MID_LEFT;
        if (balance <= 0.55) return Pan.CENTER;
        if (balance <= 0.75) return Pan.MID_RIGHT;
        return Pan.RIGHT;
    }
}
