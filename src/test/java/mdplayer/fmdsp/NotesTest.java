/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * The register value to MML key tables the fmdsp keyboard reads a bent note back through.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-25 nsano initial version <br>
 */
class NotesTest {

    /** OPNA at 7.987 MHz: the f-number of a note in block 4, {@code round(f * 2^20 / (fs/144) / 8)} */
    private static int fm(double hz) {
        return 4 << 11 | (int) Math.round(hz * (1 << 20) / (7987200 / 144.0) / 8);
    }

    @Test
    @DisplayName("an OPN block/f-number reads back as the note it plays")
    void fm() {
        assertEquals(0x40, Notes.fmKeyOf(fm(261.63)), "o4 c");
        assertEquals(0x49, Notes.fmKeyOf(fm(440.00)), "o4 a");
        assertEquals(0x4b, Notes.fmKeyOf(fm(493.88)), "o4 b");
        // one block up is one octave up, and the same f-number
        assertEquals(0x59, Notes.fmKeyOf(fm(440.00) + (1 << 11)), "o5 a");
        assertEquals(0x39, Notes.fmKeyOf(fm(440.00) - (1 << 11)), "o3 a");
        // a quarter tone either way still lands on the note
        assertEquals(0x49, Notes.fmKeyOf(fm(440.00 * Math.pow(2, 0.25 / 12))));
        assertEquals(0x49, Notes.fmKeyOf(fm(440.00 * Math.pow(2, -0.25 / 12))));
        // and three quarters of one lands on its neighbour
        assertEquals(0x4a, Notes.fmKeyOf(fm(440.00 * Math.pow(2, 0.75 / 12))), "o4 a+");
        assertEquals(0x48, Notes.fmKeyOf(fm(440.00 * Math.pow(2, -0.75 / 12))), "o4 g+");
        assertEquals(0x00, Notes.fmKeyOf(0), "silent");
    }

    /** SSG on a 7.987 MHz OPNA: the tone period of a note, {@code clock / 8 / 16 / f} */
    private static int ssg(double hz) {
        return (int) Math.round(7987200 / 8 / 16.0 / hz);
    }

    @Test
    @DisplayName("an SSG tone period reads back as the note it plays")
    void ssg() {
        assertEquals(0x40, Notes.ssgKeyOf(ssg(261.63)), "o4 c");
        assertEquals(0x49, Notes.ssgKeyOf(ssg(440.00)), "o4 a");
        assertEquals(0x30, Notes.ssgKeyOf(ssg(130.81)), "o3 c");
        assertEquals(0x60, Notes.ssgKeyOf(ssg(1046.50)), "o6 c");
        // a longer period is a lower note, so the table runs the other way round
        assertEquals(0x4a, Notes.ssgKeyOf(ssg(440.00 * Math.pow(2, 0.75 / 12))), "o4 a+");
        assertEquals(0x48, Notes.ssgKeyOf(ssg(440.00 * Math.pow(2, -0.75 / 12))), "o4 g+");
        assertEquals(0x00, Notes.ssgKeyOf(0), "silent");
    }

    /** PPZ8 counts its playback rate so that o4 c is 0x8000 */
    private static long ppz8(double ratio) {
        return Math.round(0x8000 * ratio);
    }

    @Test
    @DisplayName("a PPZ8 playback rate reads back as the note it plays")
    void ppz8() {
        assertEquals(0x40, Notes.ppz8KeyOf(ppz8(1)), "o4 c");
        assertEquals(0x50, Notes.ppz8KeyOf(ppz8(2)), "o5 c");
        assertEquals(0x30, Notes.ppz8KeyOf(ppz8(0.5)), "o3 c");
        assertEquals(0x49, Notes.ppz8KeyOf(ppz8(Math.pow(2, 9 / 12.0))), "o4 a");
        assertEquals(0x4b, Notes.ppz8KeyOf(ppz8(Math.pow(2, 11 / 12.0))), "o4 b");
        // just short of the next c, which is where the C fmdsp overflows the note nibble
        assertEquals(0x50, Notes.ppz8KeyOf(ppz8(Math.pow(2, 11.75 / 12.0))), "o5 c");
        assertEquals(0x00, Notes.ppz8KeyOf(0), "silent");
    }
}
