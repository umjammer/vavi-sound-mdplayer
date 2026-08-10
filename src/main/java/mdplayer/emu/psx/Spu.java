/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;


/**
 * The PS1 sound processing unit, as {@link PsxHw} talks to it.
 * <p>
 * ported from the SPU* entry points of aosdk eng_psf/peops
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
interface Spu {

    /** @param reg the full bus address, 0x1f801c00 .. 0x1f801dff */
    int readRegister(int reg);

    /** @param reg the full bus address, 0x1f801c00 .. 0x1f801dff */
    void writeRegister(int reg, int val);

    /** sound ram to main ram */
    void readDMAMem(int psxAddress, int size);

    /** main ram to sound ram */
    void writeDMAMem(int psxAddress, int size);
}
