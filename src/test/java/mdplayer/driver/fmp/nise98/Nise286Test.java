/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp.nise98;

import mdplayer.Common;
import mdplayer.emu.nise98.Nise98;
import mdplayer.emu.nise98.Nise98.OngenBoardType;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Nise286Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-21 nsano initial version <br>
 * @see "https://github.com/jeffpar/pcjs/blob/master/software/pcx86/test/cpu/bin/test386.com"
 */
class Nise286Test {

    @Test
    @Disabled("nise286 still has a unimplemented instruction for test386.com")
    void test1() throws Exception {
        Nise98 nise98 = new Nise98();
        nise98.init(null, this::nop, null, OngenBoardType.SpeakBoard, Common.VGMProcSampleRate);

        int r = nise98.loadRun("tmp/test386.com", "", 0x2000);
        assertEquals(0, r);
    }

    /** the 8253 counter #0 at $71 - FMP seeds its random number generator with it */
    @Test
    void test2() throws Exception {
        Nise98 nise98 = new Nise98();
        nise98.init(null, this::nop, null, OngenBoardType.SpeakBoard, Common.VGMProcSampleRate);

        // counter #0, LSB then MSB, mode 3: what FMP writes
        nise98.outpB((short) 0x77, (byte) 0x36);
        nise98.outpB((short) 0x71, (byte) 0x34);
        nise98.outpB((short) 0x71, (byte) 0x12);

        // the counter has not been clocked yet, so it still holds the count it was given
        assertEquals(0x34, nise98.inpB((short) 0x71) & 0xff);
        assertEquals(0x12, nise98.inpB((short) 0x71) & 0xff);
    }

    private void nop(int p, int a, int d) {
    }
}
