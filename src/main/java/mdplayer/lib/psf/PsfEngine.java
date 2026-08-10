/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.psf;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import mdplayer.emu.psx.PeopsSpu;
import mdplayer.emu.psx.PsxHw;

import static java.lang.System.getLogger;


/**
 * Plays a PSF1, i.e. a PS-X EXE dropped into an emulated PlayStation with only its sound
 * hardware around it.
 * <p>
 * ported from aosdk eng_psf/eng_psf.c
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class PsfEngine {

    private static final Logger logger = getLogger(PsfEngine.class.getName());

    public final PsxHw hw = new PsxHw();
    public final PeopsSpu spu = new PeopsSpu();

    private int initialPC, initialGP, initialSP;

    /** the ram as it was when the program started, so a song can be restarted without reloading */
    private final int[] initialRam = new int[(2 * 1024 * 1024) / 4];

    /** the tags of the file itself, not of its libraries */
    private PsfFile main;

    /**
     * @param files what {@link PsfFile#load} returned: the file at 0 and its libraries at 1 to 9
     */
    public void start(PsfFile[] files) throws IOException {
        this.main = files[0];

        Arrays.fill(hw.ram, 0);

        for (int i = 0; i < files.length; i++) {
            if (files[i] != null && !isPsxExe(files[i].program)) {
                throw new IOException("library %d is not a PS-X EXE".formatted(i));
            }
        }

        // the order the original player happened to use, which songs have come to depend on:
        // the registers of the file, then of _lib, then _lib's image, then the file's, then the rest
        setRegisters(files[0]);
        setRegisters(files[1]);
        hw.psfRefresh = -1;
        setRefresh(files[0]);
        setRefresh(files[1]);
        patch(files[1]);
        patch(files[0]);
        for (int i = 2; i <= PsfFile.MAX_LIBS; i++) {
            patch(files[i]);
        }

        hw.setSpu(spu);
        spu.setPsxRam(hw.ram);

        hw.cpu.init();
        hw.cpu.reset();

logger.log(Level.DEBUG, "initial PC %08x, GP %08x, SP %08x, refresh %d"
        .formatted(initialPC, initialGP, initialSP, hw.psfRefresh));

        hw.cpu.setPc(initialPC);

        if (initialSP == 0) {
            initialSP = 0x801fff00; // a stack has to go somewhere
        }

        hw.cpu.r[29] = initialSP;
        hw.cpu.r[30] = initialSP;
        hw.cpu.r[28] = initialGP;

        hw.init();
        spu.init();
        spu.open();

        // Chocobo Dungeon 2 puts a jump in the delay slot of a bne and leans on Highly
        // Experimental's cpu bug to survive it; real hardware says the code is simply wrong
        String game = main.tag("game");
        if ("Chocobo Dungeon 2".equals(game)) {
            if (hw.ram[0xbc090 / 4] == 0x0802f040) {
                hw.ram[0xbc090 / 4] = 0;
                hw.ram[0xbc094 / 4] = 0x0802f040;
                hw.ram[0xbc098 / 4] = 0;
            }
        }

        System.arraycopy(hw.ram, 0, initialRam, 0, hw.ram.length);

        hw.cpu.execute(5000);
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
        hw.cpu.r[28] = initialGP;

        hw.cpu.execute(5000);
    }

    /** one sample, i.e. 768 clocks of the cpu and one of the SPU */
    public void sample() {
        hw.slice();
        spu.sample();
    }

    /** one video frame, which is where the vblank interrupt comes from */
    public void frame() {
        hw.frame();
    }

    public int getLeft() {
        return spu.left;
    }

    public int getRight() {
        return spu.right;
    }

    private static boolean isPsxExe(byte[] program) {
        return program != null && program.length >= 8
                && new String(program, 0, 8, StandardCharsets.ISO_8859_1).equals("PS-X EXE");
    }

    private void setRegisters(PsfFile file) {
        if (file == null) {
            return;
        }
        byte[] lib = file.program;
        initialPC = readLeInt(lib, 0x10);
        initialGP = readLeInt(lib, 0x14);
        initialSP = readLeInt(lib, 0x30);
    }

    /** the first "_refresh" tag found decides between 50 and 60 Hz */
    private void setRefresh(PsfFile file) {
        if (file == null || hw.psfRefresh != -1) {
            return;
        }
        String refresh = file.tag("_refresh");
        if (refresh == null || refresh.isEmpty()) {
            return;
        }
        if (refresh.charAt(0) == '5') {
            hw.psfRefresh = 50;
        }
        if (refresh.charAt(0) == '6') {
            hw.psfRefresh = 60;
        }
    }

    /** copies the text section of one PS-X EXE into ram */
    private void patch(PsfFile file) {
        if (file == null || file.program == null || file.program.length == 0) {
            return;
        }
        byte[] lib = file.program;

        int offset = readLeInt(lib, 0x18);
        offset &= 0x3fffffff; // drop any cache segment indicator
        int plength = readLeInt(lib, 0x1c);

        // Philosoma names a length that runs past the end of its own file
        if (plength > lib.length - 2048) {
            plength = lib.length - 2048;
        }
        if (plength <= 0) {
            return;
        }

logger.log(Level.DEBUG, "patch: offset %08x length %d".formatted(offset, plength));

        for (int i = 0; i < plength; i++) {
            // the C copies word wise, so an odd offset lands on the word below it
            int a = ((offset & ~3) + i) & 0x1fffff;
            int shift = (a & 3) * 8;
            hw.ram[a >> 2] = (hw.ram[a >> 2] & ~(0xff << shift)) | ((lib[2048 + i] & 0xff) << shift);
        }
    }

    private static int readLeInt(byte[] b, int offset) {
        return (b[offset] & 0xff) | ((b[offset + 1] & 0xff) << 8)
                | ((b[offset + 2] & 0xff) << 16) | ((b[offset + 3] & 0xff) << 24);
    }
}
