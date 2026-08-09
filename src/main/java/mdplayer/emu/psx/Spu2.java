/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;


/**
 * The PS2 sound processing unit, a pair of PS1 style cores, as {@link PsxHw} talks to it.
 * <p>
 * ported from the SPU2* entry points of aosdk eng_psf/peops2
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
interface Spu2 {

    /** @param reg the full bus address, 0xbf900000 .. 0xbf9007ff */
    int read(int reg);

    /** @param reg the full bus address, 0xbf900000 .. 0xbf9007ff */
    void write(int reg, int val);

    void readDMA4Mem(int psxAddress, int size);

    void writeDMA4Mem(int psxAddress, int size);

    void readDMA7Mem(int psxAddress, int size);

    void writeDMA7Mem(int psxAddress, int size);

    void interruptDMA4();

    void interruptDMA7();
}
