/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;


/**
 * What the {@link R3000} needs from the machine around it.
 * <p>
 * ported from the program_read_*_32le / program_write_*_32le callbacks in aosdk psx_hw.c
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public interface PsxBus {

    /** @return 0 .. 0xff */
    int read8(int address);

    /** @return 0 .. 0xffff */
    int read16(int address);

    int read32(int address);

    void write8(int address, int data);

    void write16(int address, int data);

    void write32(int address, int data);

    /** the exception vector, which the machine services in software instead of in the BIOS rom */
    void biosHle(int pc);

    /** an IOP kernel call, which aosdk spells as an "addiu $zero" the real hardware would ignore */
    void iopCall(int pc, int callNumber);
}
