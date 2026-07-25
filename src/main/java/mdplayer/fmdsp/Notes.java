/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;


/**
 * Note math shared by the {@link FmDspChipReader}s and by the driver specific fmdsp sources.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public final class Notes {

    private Notes() {
    }

    /** C0 [Hz], the origin of the semitone scale the MML key is expressed in */
    private static final double c0 = 440.0 / Math.pow(2, 57 / 12.0);

    /** semitones above C0 of a frequency, -1 when silent */
    public static int noteOf(double freq) {
        if (freq <= 0) return -1;
        return (int) Math.round(12 * (Math.log(freq / c0) / Math.log(2)));
    }

    /**
     * Semitones above C0 of a PCM playback rate ratio, ratio 1.0 being o4 c - the convention the
     * chip panels use ({@code SegaPcmChip#searchSegaPCMNote}). -1 when silent.
     */
    public static int noteOfRatio(double ratio) {
        if (ratio <= 0) return -1;
        return 48 + (int) Math.round(12 * (Math.log(ratio) / Math.log(2)));
    }

    /**
     * Cents {@code freq} sits above or below the note {@link #noteOf} rounds it to, -50 to 50.
     * This is what a chip register file has in place of a driver's detune: whatever the driver bent
     * the note by - a detune, an LFO, a portamento - it wrote into the pitch register, and this is
     * the part of it that did not amount to a whole semitone.
     */
    public static int centsOf(double freq) {
        if (freq <= 0) return 0;
        return cents(12 * (Math.log(freq / c0) / Math.log(2)));
    }

    /** cents a playback rate ratio sits off the note {@link #noteOfRatio} rounds it to */
    public static int centsOfRatio(double ratio) {
        if (ratio <= 0) return 0;
        return cents(12 * (Math.log(ratio) / Math.log(2)));
    }

    private static int cents(double semitones) {
        return (int) Math.round((semitones - Math.round(semitones)) * 100);
    }

    /** highest key fmdsp's keyboard can show, o8 b */
    private static final int maxKey = 0x8b;

    /**
     * MML key (high nibble octave, low nibble note) of an OPN block/f-number, the value a driver
     * writes to registers 0xa0-0xa6. Port of the C fmdsp's {@code fmdriver_fm_freq2key}.
     * <p>
     * The block is an exponent, so the f-number is first brought into the top octave of its range
     * and the block corrected for it; the table then holds the f-number half a semitone above each
     * note of that one octave, which is where the nearest note changes. It starts at a because a
     * normalized f-number crosses the octave boundary there and not at c.
     */
    public static int fmKeyOf(int freq) {
        int block = freq >> 11;
        int fNum = freq & 0x7ff;
        if (fNum == 0) return 0x00;
        while ((fNum & 0x400) == 0) {
            fNum <<= 1;
            block--;
        }
        int note = 0;
        while (note < 12 && fNum >= FM_FREQ[note]) note++;
        note += 9;
        block += note / 12;
        note %= 12;
        if (block < 0) return 0x00;
        if (block > 8) return maxKey;
        return block << 4 | note;
    }

    /** f-number half a semitone above a - g+, the octave a normalized OPN f-number spans */
    private static final int[] FM_FREQ = {
            0x042e, 0x046e, 0x04b1, 0x04f9, 0x0544, 0x0595,
            0x05ea, 0x0644, 0x06a3, 0x0708, 0x0773, 0x07e4,
    };

    /**
     * MML key of an SSG tone period, the value a driver writes to registers 0x00-0x05. Port of the
     * C fmdsp's {@code fmdriver_ssg_freq2key}. The SSG counts a period rather than a frequency, so
     * the table runs downwards and the octave falls as the period grows.
     */
    public static int ssgKeyOf(int freq) {
        if (freq == 0) return 0x00;
        int octave = -5;
        freq &= 0xffff;
        while ((freq & 0x8000) == 0) {
            freq <<= 1;
            octave++;
        }
        int note = 0;
        while (note < 12 && freq <= SSG_FREQ[note]) note++;
        note += 11;
        octave += note / 12;
        note %= 12;
        if (octave < 0) return 0x00;
        if (octave > 8) return maxKey;
        return octave << 4 | note;
    }

    /** period half a semitone below c - b, of a period normalized into one octave */
    private static final int[] SSG_FREQ = {
            0xf57f, 0xe7b8, 0xdab7, 0xce70, 0xc2da, 0xb7ea,
            0xad98, 0xa3da, 0x9aa7, 0x91f9, 0x89c8, 0x820c,
    };

    /**
     * MML key of a PPZ8 playback rate, a 16.16 fixed point ratio against the sample's own rate.
     * Port of the C fmdsp's {@code fmdriver_ppz8_freq2key}.
     */
    public static int ppz8KeyOf(long freq) {
        if (freq == 0) return 0x00;
        int octave = 16 + 4;
        freq &= 0xffff_ffffL;
        while ((freq & 0x8000_0000L) == 0) {
            freq <<= 1;
            octave--;
        }
        if (octave < 0) return 0x00;
        if (octave > 8) return maxKey;
        int high = (int) (freq >> 16);
        int note = 0;
        while (note < 12 && high >= PPZ8_FREQ[note]) note++;
        // a rate past the last boundary is the next octave's c; the C fmdsp leaves it at note 12,
        // which is not a note at all
        if (note == 12) {
            note = 0;
            octave++;
        }
        return octave > 8 ? maxKey : octave << 4 | note;
    }

    /** rate half a semitone above c - b, of a rate normalized into one octave */
    private static final int[] PPZ8_FREQ = {
            0x83c0, 0x8b96, 0x93e3, 0x9cae, 0xa5ff, 0xafde,
            0xba53, 0xc567, 0xd124, 0xdd94, 0xeac1, 0xf8b6,
    };
}
