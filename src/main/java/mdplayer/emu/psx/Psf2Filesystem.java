/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;


/**
 * What the IOP kernel calls of {@link PsxHw} need from the PSF2 virtual filesystem.
 * <p>
 * ported from the psf2_load_file / psf2_load_elf / psf2_*_loadaddr entry points of
 * aosdk eng_psf/eng_psf2.c
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public interface Psf2Filesystem {

    /** aosdk spells "not found" as 0xffffffff, so this returns -1 */
    int NOT_FOUND = 0xffffffff;

    /**
     * @return the length read, or {@link #NOT_FOUND}
     */
    int loadFile(String name, byte[] buffer, int bufferLength);

    /**
     * Relocates an IRX module into IOP ram.
     *
     * @return the entry point, or {@link #NOT_FOUND}
     */
    int loadElf(byte[] start, int length);

    /** where the next module or allocation goes */
    int getLoadAddr();

    void setLoadAddr(int address);
}
