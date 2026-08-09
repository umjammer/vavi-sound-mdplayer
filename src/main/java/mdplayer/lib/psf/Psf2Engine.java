/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.psf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import mdplayer.emu.psx.PeopsSpu2;
import mdplayer.emu.psx.Psf2Filesystem;
import mdplayer.emu.psx.PsxHw;
import mdplayer.emu.psx.R3000;

import static java.lang.System.getLogger;


/**
 * Plays a PSF2, i.e. a set of IRX modules and their data dropped into an emulated PS2 IOP.
 * <p>
 * A PSF2 carries no program of its own: its reserved area holds a small read only filesystem,
 * and the IOP is started on the "psf2.irx" it finds there, which then loads whatever else it
 * wants through the kernel.
 * <p>
 * ported from aosdk eng_psf/eng_psf2.c
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class Psf2Engine implements Psf2Filesystem {

    private static final Logger logger = getLogger(Psf2Engine.class.getName());

    /** the most filesystems, i.e. libraries and subdirectories, a song may bring */
    private static final int MAX_FS = 32;

    public final PsxHw hw = new PsxHw();
    public final PeopsSpu2 spu = new PeopsSpu2();

    /** the filesystem of the file itself is 0, of its libraries 1 to 9 */
    private final byte[][] filesys = new byte[MAX_FS][];
    private int numFs;

    /** where the next module or allocation goes */
    private int loadAddr;

    private int initialPC, initialSP;

    private final int[] initialRam = new int[(2 * 1024 * 1024) / 4];

    /** the tags of the file itself */
    public PsfFile main;

    /**
     * @param files what {@link PsfFile#load} returned: the file at 0 and its libraries at 1 to 9
     */
    public void start(PsfFile[] files) throws IOException {
        this.main = files[0];

        // the value Highly Experimental happens to leave things at, which Shadow Hearts
        // has hard coded assumptions about
        loadAddr = 0x23f00;

        java.util.Arrays.fill(hw.ram, 0);

        numFs = 0;
        java.util.Arrays.fill(filesys, null);
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                continue;
            }
            if (i == 0 && files[i].program.length > 0) {
                logger.log(Level.WARNING, "a PSF2 should have no program section, %d bytes"
                        .formatted(files[i].program.length));
            }
            numFs = Math.max(numFs, i + 1);
            filesys[i] = files[i].reserved;
        }

        hw.setSpu2(spu);
        hw.setFilesystem(this);
        spu.setPsxRam(hw.ram);

        // psf2.irx is what starts everything else
        byte[] buf = new byte[512 * 1024];
        int irxLen = loadFile("psf2.irx", buf, buf.length);
        if (irxLen == NOT_FOUND) {
            throw new IOException("psf2.irx is missing");
        }

        initialPC = loadElf(buf, irxLen);
        initialSP = 0x801ffff0;

        if (initialPC == NOT_FOUND) {
            throw new IOException("psf2.irx is not a usable ELF");
        }

        hw.cpu.init();
        hw.cpu.reset();

        hw.cpu.setPc(initialPC);
        hw.cpu.r[29] = initialSP;
        hw.cpu.r[30] = initialSP;
        hw.cpu.r[31] = 0x80000000; // RA, i.e. the IOP null state

        // argc and argv, pointing at "aofile:/"
        hw.cpu.r[4] = 2;
        hw.cpu.r[5] = 0x80000004;
        hw.ram[1] = 0x80000008;
        writeString(8, "aofile:/");
        hw.ram[0] = R3000.FUNCT_HLECALL;

        System.arraycopy(hw.ram, 0, initialRam, 0, hw.ram.length);

        hw.init();
        spu.init();
        spu.open();
    }

    /** starts the song over without decoding the file again */
    public void restart() {
        spu.close();

        System.arraycopy(initialRam, 0, hw.ram, 0, hw.ram.length);

        hw.cpu.init();
        hw.cpu.reset();
        hw.init();
        spu.init();
        spu.open();

        hw.cpu.setPc(initialPC);
        hw.cpu.r[29] = initialSP;
        hw.cpu.r[30] = initialSP;
        hw.cpu.r[31] = 0x80000000;
        hw.cpu.r[4] = 2;
        hw.cpu.r[5] = 0x80000004;

        hw.init();
    }

    /** one sample, the SPU first and then the cpu, which is the order aosdk uses here */
    public void sample() {
        spu.sample();
        hw.ps2Slice();
    }

    /** one video frame, which is where the IOP scheduler gets a chance to switch threads */
    public void frame() {
        hw.ps2Frame();
    }

    public int getLeft() {
        return spu.left;
    }

    public int getRight() {
        return spu.right;
    }

    private void writeString(int byteAddress, String s) {
        byte[] b = s.getBytes(StandardCharsets.ISO_8859_1);
        for (int i = 0; i < b.length; i++) {
            hw.setRamByte(byteAddress + i, b[i]);
        }
        hw.setRamByte(byteAddress + b.length, 0);
    }

    // ---- the virtual filesystem ----

    @Override
    public int getLoadAddr() {
        return loadAddr;
    }

    @Override
    public void setLoadAddr(int address) {
        this.loadAddr = address;
    }

    @Override
    public int loadFile(String name, byte[] buffer, int bufferLength) {
        for (int i = 0; i < numFs; i++) {
            if (filesys[i] == null) {
                continue;
            }
            int len = loadFileFrom(filesys[i], 0, name, buffer, bufferLength);
            if (len != NOT_FOUND) {
logger.log(Level.DEBUG, "loadFile: %s -> %d bytes".formatted(name, len));
                return len;
            }
        }
logger.log(Level.DEBUG, "loadFile: %s not found".formatted(name));
        return NOT_FOUND;
    }

    /**
     * One directory of the read only filesystem: a file count, then 48 byte entries of a name,
     * an offset, an uncompressed size and a block size. A zero size means the entry is a
     * subdirectory, so the rest of the path is looked up inside it.
     *
     * @param start where this directory begins inside {@code top}
     */
    private int loadFileFrom(byte[] top, int start, String file, byte[] buf, int bufLen) {
        // only the first path element belongs to this directory
        int slash = 0;
        while (slash < file.length() && file.charAt(slash) != '/' && file.charAt(slash) != '\\') {
            slash++;
        }
        String matchName = file.substring(0, slash);
        String remainder = slash + 1 <= file.length() ? file.substring(Math.min(slash + 1, file.length())) : "";

        int numFiles = readLeInt(top, start);
        int cptr = start + 4;

        for (int i = 0; i < numFiles; i++) {
            if (cptr + 48 > top.length) {
                break;
            }
            int offs = readLeInt(top, cptr + 36);
            int uncomp = readLeInt(top, cptr + 40);
            int bsize = readLeInt(top, cptr + 44);

            String name = readString(top, cptr, 36);

            if (name.equalsIgnoreCase(matchName)) {
                if (uncomp == 0 && bsize == 0) {
logger.log(Level.DEBUG, "descending into [%s] for [%s] at %x".formatted(matchName, remainder, offs));
                    return loadFileFrom(top, offs, remainder, buf, bufLen);
                }

                int blocks = (uncomp + bsize - 1) / bsize;

                int cofs = offs + (blocks * 4);
                int uofs = 0;
                for (int j = 0; j < blocks; j++) {
                    int usize = readLeInt(top, offs + (j * 4));

                    int written;
                    try {
                        written = inflate(top, cofs, usize, buf, uofs, bufLen - uofs);
                    } catch (IOException e) {
                        logger.log(Level.ERROR, "decompress fail: " + e.getMessage());
                        return NOT_FOUND;
                    }

                    cofs += usize;
                    uofs += written;
                }

                return uncomp;
            } else {
                cptr += 48;
            }
        }

        return NOT_FOUND;
    }

    private static int inflate(byte[] in, int inOffset, int inLength, byte[] out, int outOffset, int outLength)
            throws IOException {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(in, inOffset, inLength);
            int written = 0;
            while (!inflater.finished() && written < outLength) {
                int n = inflater.inflate(out, outOffset + written, outLength - written);
                if (n == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) {
                        throw new IOException("truncated");
                    }
                    continue;
                }
                written += n;
            }
            return written;
        } catch (DataFormatException e) {
            throw new IOException(e);
        } finally {
            inflater.end();
        }
    }

    // ---- the IRX loader ----

    private static final int ELF32_R_TYPE_MASK = 0xff;

    @Override
    public int loadElf(byte[] start, int length) {
        if ((loadAddr & 3) != 0) {
            loadAddr &= ~3;
            loadAddr += 4;
        }

logger.log(Level.DEBUG, "loadElf: starting at %08x".formatted(loadAddr | 0x80000000));

        if (start.length < 52 || (start[0] & 0xff) != 0x7f || start[1] != 'E' || start[2] != 'L' || start[3] != 'F') {
            logger.log(Level.ERROR, "not an ELF file");
            return NOT_FOUND;
        }

        int entry = readLeInt(start, 24);
        int shoff = readLeInt(start, 32);

        int shentsize = readLeShort(start, 46);
        int shnum = readLeShort(start, 48);

        int shent = shoff;
        int totallen = 0;

        // the hi16 half of a relocation pair waits here for its lo16
        int hi16offs = 0;
        int hi16target = 0;

        for (int i = 0; i < shnum; i++) {
            int type = readLeInt(start, shent + 4);
            int addr = readLeInt(start, shent + 12);
            int offset = readLeInt(start, shent + 16);
            int size = readLeInt(start, shent + 20);

            switch (type) {
            case 0: // the section table header
                break;

            case 1: // PROGBITS, copy it in
                copyToRam(loadAddr + addr, start, offset, size);
                totallen += size;
                break;

            case 2: // SYMTAB
            case 3: // STRTAB
                break;

            case 8: // NOBITS, i.e. the bss, which is zeroed
                zeroRam(loadAddr + addr, size);
                totallen += size;
                break;

            case 9: // REL, the short relocation records
                for (int rec = 0; rec < (size / 8); rec++) {
                    int offs = readLeInt(start, offset + (rec * 8));
                    int info = readLeInt(start, offset + 4 + (rec * 8));
                    int target = ramWordAt(loadAddr + offs);

                    switch (info & ELF32_R_TYPE_MASK) {
                    case 2: // R_MIPS_32
                        target += loadAddr;
                        break;

                    case 4: { // R_MIPS_26
                        int temp = (target & 0x03ffffff);
                        target &= 0xfc000000;
                        temp += (loadAddr >>> 2);
                        target |= temp;
                        break;
                    }

                    case 5: // R_MIPS_HI16
                        hi16offs = offs;
                        hi16target = target;
                        break;

                    case 6: { // R_MIPS_LO16
                        int vallo = ((target & 0xffff) ^ 0x8000) - 0x8000;

                        int val = ((hi16target & 0xffff) << 16) + vallo;
                        val += loadAddr;

                        // account for the sign extension the low half will do
                        val = ((val >>> 16) + ((val & 0x8000) != 0 ? 1 : 0)) & 0xffff;

                        hi16target = (hi16target & ~0xffff) | val;

                        val = loadAddr + vallo;
                        target = (target & ~0xffff) | (val & 0xffff);

                        setRamWordAt(loadAddr + hi16offs, hi16target);
                        break;
                    }

                    default:
                        logger.log(Level.ERROR, "unknown MIPS ELF relocation %d".formatted(info & 0xff));
                        return NOT_FOUND;
                    }

                    setRamWordAt(loadAddr + offs, target);
                }
                break;

            case 0x70000080: // .iopmod, which only names the module
                break;

            default:
logger.log(Level.TRACE, "unhandled ELF section type %d".formatted(type));
                break;
            }

            shent += shentsize;
        }

        entry += loadAddr;
        entry |= 0x80000000;
        loadAddr += totallen;

logger.log(Level.DEBUG, "loadElf: entry PC %08x".formatted(entry));
        return entry;
    }

    /** the C indexes ram as words, so an odd address lands on the word below it */
    private int ramWordAt(int byteAddress) {
        return hw.ram[((byteAddress / 4)) & (hw.ram.length - 1)];
    }

    private void setRamWordAt(int byteAddress, int value) {
        hw.ram[((byteAddress / 4)) & (hw.ram.length - 1)] = value;
    }

    private void copyToRam(int byteAddress, byte[] src, int srcOffset, int size) {
        int base = (byteAddress / 4) * 4;
        for (int i = 0; i < size; i++) {
            if (srcOffset + i >= src.length) {
                break;
            }
            hw.setRamByte(base + i, src[srcOffset + i]);
        }
    }

    private void zeroRam(int byteAddress, int size) {
        int base = (byteAddress / 4) * 4;
        for (int i = 0; i < size; i++) {
            hw.setRamByte(base + i, 0);
        }
    }

    private static String readString(byte[] b, int offset, int max) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < max && offset + i < b.length; i++) {
            if (b[offset + i] == 0) {
                break;
            }
            baos.write(b[offset + i]);
        }
        return baos.toString(StandardCharsets.ISO_8859_1);
    }

    private static int readLeInt(byte[] b, int offset) {
        if (offset < 0 || offset + 4 > b.length) {
            return 0;
        }
        return (b[offset] & 0xff) | ((b[offset + 1] & 0xff) << 8)
                | ((b[offset + 2] & 0xff) << 16) | ((b[offset + 3] & 0xff) << 24);
    }

    private static int readLeShort(byte[] b, int offset) {
        if (offset < 0 || offset + 2 > b.length) {
            return 0;
        }
        return (b[offset] & 0xff) | ((b[offset + 1] & 0xff) << 8);
    }
}
