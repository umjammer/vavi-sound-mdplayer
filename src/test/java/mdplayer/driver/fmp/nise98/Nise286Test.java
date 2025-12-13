/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp.nise98;

import mdplayer.driver.fmp.nise98.Nise98.OngenBoardType;
import musicDriverInterface.ChipDatum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Nise286Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-21 nsano initial version <br>
 * @see "https://github.com/jeffpar/pcjs/blob/master/software/pcx86/test/cpu/bin/test386.com"
 */
class Nise286Test {

    @Test
    void test1() throws Exception {
        Nise98 nise98 = new Nise98();
        nise98.init(null, this::nop, null, OngenBoardType.SpeakBoard);

        int r = nise98.loadRun("tmp/test386.com", "", 0x2000);
        assertEquals(0, r);
    }

    private void nop(ChipDatum dat) {
    }
}
