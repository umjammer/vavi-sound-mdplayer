/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * The delay slot corners of the r3000, which is where this cpu keeps its surprises.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class R3000Test {

    private static final int BASE = 0x80010000;

    /** {@code beq $zero, $zero, +1}, i.e. branch to just past the delay slot */
    private static final int BEQ_FORWARD = 0x10000001;

    /** {@code beq $zero, $zero, +2}, i.e. branch over the instruction past the delay slot */
    private static final int BEQ_OVER = 0x10000002;

    /**
     * A branch in a branch's delay slot. The pending branch has {@code delayr} set to the
     * "a branch is waiting" marker, which is not a register - the C writes one past the end of
     * its register file over it, which in java threw
     * {@code ArrayIndexOutOfBoundsException: Index 32 out of bounds for length 32} and killed
     * the song. Most of the psf2 rips out there hit it.
     */
    @Test
    void aBranchInABranchDelaySlotDoesNotThrow() {
        PsxHw hw = new PsxHw();
        hw.init();

        hw.setRamWord(BASE, BEQ_FORWARD);
        hw.setRamWord(BASE + 4, BEQ_FORWARD);
        hw.setRamWord(BASE + 8, 0); // nop, the second branch's delay slot
        hw.setRamWord(BASE + 12, 0);

        hw.cpu.setPc(BASE);
        hw.cpu.execute(3);

        // the first branch is dropped and the second one taken, from its own delay slot
        assertEquals(BASE + 12, hw.cpu.pc, "the second branch should be the one that happens");
    }

    /** and an ordinary branch still lands where it should */
    @Test
    void anOrdinaryBranchTakesItsDelaySlotWithIt() {
        PsxHw hw = new PsxHw();
        hw.init();

        hw.setRamWord(BASE, BEQ_OVER);
        hw.setRamWord(BASE + 4, 0x24080007); // addiu $t0, $zero, 7 - the delay slot, which runs
        hw.setRamWord(BASE + 8, 0x24080009); // addiu $t0, $zero, 9 - branched over
        hw.setRamWord(BASE + 12, 0);
        hw.setRamWord(BASE + 16, 0);

        hw.cpu.setPc(BASE);
        hw.cpu.execute(3);

        assertEquals(BASE + 16, hw.cpu.pc);
        assertEquals(7, hw.cpu.r[8], "the delay slot runs, what it branches over does not");
    }
}
