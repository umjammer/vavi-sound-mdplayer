/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * The PS1 kernel calls, driven directly rather than through a song.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class PsxHwTest {

    /** where the kernel's B0 calls are entered, which is what the HLE traps */
    private static final int B0 = 0xb0;

    /** somewhere in ram for the call to return to */
    private static final int RA = 0x80010000;

    private static final int EvMdNOINTR = 0x2000;

    private PsxHw hw;

    @BeforeEach
    void setup() {
        hw = new PsxHw();
        hw.init();
    }

    /** makes a B0 call the way a game does: the subcall in t1, the arguments in a0 to a3 */
    private int callB0(int subcall, int a0, int a1, int a2, int a3) {
        hw.cpu.r[9] = subcall;
        hw.cpu.r[4] = a0;
        hw.cpu.r[5] = a1;
        hw.cpu.r[6] = a2;
        hw.cpu.r[7] = a3;
        hw.cpu.r[31] = RA;
        hw.cpu.setPc(B0);
        hw.biosHle(B0);
        return hw.cpu.r[2];
    }

    /**
     * The one that made fourteen of the forty six tracks of Dragon Quest Monsters 1+2 play
     * silence: delivering an event nothing has opened has nothing to do, but it still has to
     * return. Leaving the pc on the call re-runs it on the next step, for ever.
     */
    @Test
    void deliveringAnEventNobodyOpenedStillReturns() {
        callB0(0x07, 0xf0000009, 0, 0, 0); // DeliverEvent

        assertEquals(RA, hw.cpu.pc, "DeliverEvent must return to its caller");
        assertNotEquals(B0, hw.cpu.pc, "leaving the pc on the call spins the song for ever");
    }

    /** and the same call on an event that is open delivers it and returns */
    @Test
    void deliveringAnOpenEventMarksItAndReturns() {
        int event = callB0(0x08, 0xf0000009, 0, EvMdNOINTR, 0); // OpenEvent
        assertEquals(RA, hw.cpu.pc);

        callB0(0x0c, event, 0, 0, 0); // EnableEvent, i.e. make it active
        assertEquals(RA, hw.cpu.pc);

        callB0(0x07, 0xf0000009, 0, 0, 0); // DeliverEvent
        assertEquals(RA, hw.cpu.pc);

        // the event is now waiting to be picked up, which is what the song polls for
        assertEquals(1, callB0(0x0b, event, 0, 0, 0), "TestEvent should report the delivery");
        assertEquals(RA, hw.cpu.pc);
    }

    /** every other B0 call returns to its caller too, including one the HLE does not know */
    @Test
    void anUnknownCallReturns() {
        callB0(0xfe, 0, 0, 0, 0);

        assertEquals(RA, hw.cpu.pc);
    }

    /**
     * ReturnFromException is the one call that deliberately does not: it puts the pc back where
     * the exception came from itself.
     */
    @Test
    void returnFromExceptionSetsItsOwnPc() {
        hw.cpu.cp0r[R3000.CP0_EPC] = 0x80020000;

        callB0(0x17, 0, 0, 0, 0);

        assertEquals(0x80020000, hw.cpu.pc);
    }
}
