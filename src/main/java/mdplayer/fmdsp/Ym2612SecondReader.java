/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;


/**
 * The second OPN2 of a VGM that declares two of them - a Sega System 18 board is two YM3438s, and
 * the second one carries most of the music.
 * <p>
 * A reader shows one chip, so the pair needs two of them; this one differs from
 * {@link Ym2612Reader} only in which of {@code Ym2612Chip}'s two register sets it reads.
 * <p>
 * What it does not get is a row per channel. The FM group has nine rows, the chip in front of it
 * holds six, and the three that are left are handed out to the channels that sound first - the same
 * way {@link SegaPcmReader} maps sixteen voices onto nine rows. The rows are numbered on from the
 * first chip's, FM7 to FM12, so the strip reads as one instrument of twelve channels rather than
 * two of six.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
public class Ym2612SecondReader extends Ym2612Reader {

    /** rows this reader can ever be given: what a six channel chip in front of it leaves over */
    private static final int ROWS = 3;

    /** chip channel shown on each row, -1 = none yet */
    private final int[] slotChannels = {-1, -1, -1};

    private int mapped;

    @Override
    protected int chipId() {
        return 1;
    }

    /** behind the first OPN2, so it claims the rows the first one leaves */
    @Override
    public int priority() {
        return 21;
    }

    @Override
    public int channels(Group group) {
        return group == Group.FM ? ROWS : 3;
    }

    @Override
    public int meters(Group group) {
        return group == Group.FM ? ROWS : 3;
    }

    /** every row here is a channel of its own - none of them are ch3's operator slots */
    @Override
    public int meterOf(Group group, int slot) {
        return slot;
    }

    @Override
    public void reset() {
        super.reset();
        Arrays.fill(slotChannels, -1);
        mapped = 0;
    }

    @Override
    public void poll() {
        super.poll();
        for (int ch = 0; ch < 6 && mapped < slotChannels.length; ch++) {
            if ((keyOns()[ch] & 1) != 0 && slotOf(ch) < 0) {
                slotChannels[mapped++] = ch;
            }
        }
    }

    private int slotOf(int ch) {
        for (int slot = 0; slot < mapped; slot++) {
            if (slotChannels[slot] == ch) return slot;
        }
        return -1;
    }

    @Override
    public void read(Group group, int slot, FmDspChannel out) {
        if (group != Group.FM) {
            super.read(group, slot, out);
            return;
        }
        out.name = "FM";
        int ch = slotChannels[slot];
        if (ch < 0) return;
        super.read(group, ch, out);
        out.num = 6 + ch + 1;
    }

    @Override
    public boolean masked(Group group, int slot) {
        int ch = channelOf(group, slot);
        return ch >= 0 && super.masked(group, ch);
    }

    @Override
    public boolean readDetail(Group group, int slot, vavi.sound.visualizer.fmdsp.TrackDetail out) {
        int ch = channelOf(group, slot);
        return ch >= 0 && super.readDetail(group, ch, out);
    }

    private int channelOf(Group group, int slot) {
        return group == Group.FM ? slotChannels[slot] : slot;
    }
}
