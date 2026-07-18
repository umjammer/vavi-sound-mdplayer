/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.form.kb;

/**
 * The draw state of one channel row that every chip panel shows: pan, key-on note, volume
 * meters, mute and the instrument matrix.
 * <p>
 * A panel keeps two of these per channel — what it drew last frame and what this frame's chip
 * state decodes to — and redraws only what differs. This is view state owned by the panel;
 * the chip itself is the source of truth (see {@code BaseChip#getInfo}).
 * <p>
 * Fields only one chip family knows (PCM addresses, OPM key fraction, …) do not belong here:
 * subclass this in the panel that draws them (see {@link PcmChannelParams} for the pattern),
 * so adding a chip never touches this class.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-18 nsano initial version <br>
 */
public class ChannelParams {

    public int pan = -1;
    public int panL = -1;
    public int panR = -1;
    public int pantp = -1;
    public int note = -1;
    public int volume = -1;
    public int volumeL = -1;
    public int volumeR = -1;
    public int freq = -1;
    public Boolean mask = false;
    public int slot = 0;
    public int tp = -1;

    public int[] inst = new int[48];
    public final int[] typ = new int[48];
    public final boolean[] bit = new boolean[48];

    public ChannelParams() {
        for (int i = 0; i < inst.length; i++) {
            inst[i] = -1;
            typ[i] = 0;
            bit[i] = false;
        }
    }
}
