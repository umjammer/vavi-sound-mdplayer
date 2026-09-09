/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.mgsc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import konamiman.z80.impls.PlainMemory;

import static java.lang.System.getLogger;


/**
 * Turns an MGSDRV ".mus" into the ".mgs" the driver plays, by running the real MSX
 * {@code MGSC.COM} on a Z80 with just enough MSX-DOS under it.
 * <p>
 * Nothing here understands MML. The compiler is the MSX binary itself, so what it accepts and
 * what it emits is exactly what it was on the machine - which is the only way to be sure, since
 * MGSC's MML has no other specification than MGSC.
 * <p>
 * Ported from <a href="https://github.com/digital-sound-antiques/mgsc">mgsc</a>, which is where
 * the page-zero image and the shape of the run loop come from. That one traps the guest with
 * {@code HALT}s written over the MSX-DOS entry points; here the entry points are caught before
 * the instruction is fetched instead, the way {@link mdplayer.lib.mgsdrv.MgsDrv} catches the
 * BIOS - so the image keeps its {@code HALT}s only as the thing that happens when the compiler
 * calls somewhere we did not expect.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-30 nsano initial version <br>
 */
public class MgscCompiler {

    private static final Logger logger = getLogger(MgscCompiler.class.getName());

    /** the MSX binary that does the compiling, looked for beside MGSDRV.COM */
    public static final String COMPILER = "MGSC.COM";

    /** where the compiler is loaded, the way MSX-DOS loads a ".COM" */
    private static final int TPA = 0x100;

    /** the name the source has on the emulated disk - the command tail in page zero names it */
    private static final String SOURCE = "TEMP.MUS";

    /** the name MGSC gives the object it writes, being the source's with the extension changed */
    private static final String OBJECT = "TEMP.MGS";

    /**
     * How long the compiler is given, in instructions. A song compiles in a few million; this is
     * only here so that a compiler which has lost its way stops rather than hangs the player.
     */
    private static final long INSTRUCTION_LIMIT =
            Long.getLong("mdplayer.mgsc.limit", 500_000_000L);

    /** MSX-DOS page zero, as much of it as MGSC.COM reads */
    private static byte[] pageZero() {
        byte[] page = new byte[TPA];
        // every entry point is a HALT, so a call to one we do not serve stops the machine there
        // instead of running into whatever the compiler left in memory
        Arrays.fill(page, (byte) 0x76);
        page[0x0001] = (byte) 0xc9; // WBOOT, whose HALT at 0000H is how a program says it is done
        page[0x0005] = (byte) 0xc3; // BDOS: JP F000H - and MGSC takes its stack from 0006H, so
        page[0x0006] = (byte) 0x00; // this doubles as the top of the transient program area
        page[0x0007] = (byte) 0xf0;
        page[0x000d] = (byte) 0xc9; // RDSLT
        page[0x0015] = (byte) 0xc9; // WRSLT
        page[0x001d] = (byte) 0xc9; // CALSLT
        page[0x0025] = (byte) 0xc9; // ENASLT
        page[0x0031] = (byte) 0xc9; // CALLF
        page[0x0039] = (byte) 0xc9; // the interrupt handler's RET
        page[0x003a] = 0;
        page[0x003b] = 0;

        // the default FCB, naming the source
        page[0x005c] = 0;
        put(page, 0x005d, "TEMP    ");
        put(page, 0x0065, "MUS");
        Arrays.fill(page, 0x0068, 0x006d, (byte) 0);
        put(page, 0x006d, "-T ");
        put(page, 0x0070, "        ");
        Arrays.fill(page, 0x0078, 0x0080, (byte) 0);

        // the command tail, as MGSC.COM parses it for itself:
        //   -E makes it call MSX-DOS through the CP/M functions, which are the ones served here
        //   -S makes it write the object even when the MML says #no_mgs
        String tail = " " + SOURCE + " -E -S  ";
        tail = tail + " ".repeat(0x1f - tail.length());
        page[0x0080] = (byte) 0x1f;
        put(page, 0x0081, tail);

        return page;
    }

    private static void put(byte[] page, int offset, String text) {
        for (int i = 0; i < text.length(); i++) {
            page[offset + i] = (byte) text.charAt(i);
        }
    }

    /**
     * The "#" directives MGSC.COM knows, taken from the binary's own table.
     * <p>
     * This is what tells an MGSDRV ".mus" from a MUAP98 ".mus", which is the other MML that goes
     * by that extension: MUAP's has no directive lines at all, and MGSDRV's opens with a few.
     */
    private static final Set<String> DIRECTIVES = Set.of(
            "opll_mode", "title", "lfo_mode", "allocate", "alloc", "psg_tune", "tempo",
            "play_track", "play_trk", "play", "play_start", "no_mgs", "track_status",
            "macro_offset", "machine_id", "disenable_mgsrc", "opll_tune", "track_volume");

    /**
     * Is this MGSDRV MML?
     *
     * @param head the first bytes of the file - the directives are at the top of a song, so the
     *             first few KB is all this needs to see
     */
    public static boolean isMgsMml(byte[] head) {
        int i = 0;
        while (i < head.length) {
            while (i < head.length && (head[i] == ' ' || head[i] == '\t')) i++;
            if (i < head.length && head[i] == '#') {
                int start = ++i;
                while (i < head.length && (head[i] >= 'a' && head[i] <= 'z' || head[i] >= 'A' && head[i] <= 'Z' || head[i] == '_')) i++;
                String directive = new String(head, start, i - start, StandardCharsets.US_ASCII).toLowerCase();
                if (DIRECTIVES.contains(directive)) {
                    return true;
                }
            }
            while (i < head.length && head[i] != '\n') i++;
            i++;
        }
        return false;
    }

    /** where MGSC.COM is, which is where MGSDRV.COM is */
    public static File compilerFile() {
        return new File(System.getProperty("mdplayer.mgs.dir", System.getProperty("user.dir")), COMPILER);
    }

    /** is there an MGSC.COM to compile with? */
    public static boolean isAvailable() {
        return compilerFile().exists();
    }

    private final BdosEmulator bdos = new BdosEmulator();

    /** what stopped the machine, when something other than the compiler finishing did */
    private String failure;

    private long instructions;

    /** what the compiler printed, which is where it says what it did not like */
    public List<String> getLog() {
        List<String> lines = new ArrayList<>();
        for (String line : bdos.console.toString().split("\r?\n")) {
            if (!line.isBlank()) lines.add(line.strip());
        }
        return lines;
    }

    /**
     * Compiles one song.
     *
     * @param mml the ".mus" itself, in whatever encoding it was written in - it is bytes to the
     *            compiler and bytes to us
     * @return the ".mgs"
     * @throws IOException when there is no MGSC.COM to compile with, or when the compiler
     *                     rejected the song - in which case the message carries what it said
     */
    public byte[] compile(byte[] mml) throws IOException {
        File compiler = compilerFile();
        if (!compiler.exists()) {
            throw new IOException("no " + COMPILER + " in " + compiler.getParent()
                    + "; set -Dmdplayer.mgs.dir=<dir>");
        }
        byte[] program = Files.readAllBytes(compiler.toPath());

        bdos.reset();
        bdos.files.put(SOURCE, crlf(mml));
        bdos.files.put(OBJECT, new byte[0]);
        failure = null;
        instructions = 0;

        Z80Processor z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setMemory(new PlainMemory(0x10000));
        z80.reset();
        z80.getMemory().setContents(0, pageZero(), 0, null);
        z80.getMemory().setContents(TPA, program, 0, null);
        z80.getRegisters().setPC((short) TPA);
        z80.getRegisters().setSP((short) 0xf380); // MGSC replaces this from (0006H) straight away
        z80.beforeInstructionFetch().addListener(this::onBeforeInstructionFetch);
        z80.continue_();

        for (String line : getLog()) {
logger.log(Level.DEBUG, "MGSC: " + line);
        }

        if (failure != null) {
            throw new IOException(COMPILER + ": " + failure + whatItSaid());
        }

        byte[] mgs = bdos.files.get(OBJECT);
        if (mgs == null || mgs.length == 0) {
            throw new IOException(COMPILER + ": the song did not compile" + whatItSaid());
        }
        return mgs;
    }

    /** what the compiler printed, for an exception message that would otherwise say nothing */
    private String whatItSaid() {
        List<String> lines = getLog();
        // the banner is the first two lines and says nothing about this song
        List<String> said = lines.size() > 2 ? lines.subList(2, lines.size()) : List.of();
        return said.isEmpty() ? "" : " - " + String.join("; ", said);
    }

    /**
     * MGSC reads the source a 128 byte record at a time and counts on CR+LF, which is what the
     * MSX wrote; a song that has been through a unix tool since has lost the CRs.
     */
    private static byte[] crlf(byte[] mml) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(mml.length + mml.length / 16);
        for (int i = 0; i < mml.length; i++) {
            if (mml[i] == '\n' && (i == 0 || mml[i - 1] != '\r')) out.write('\r');
            out.write(mml[i]);
        }
        if (mml.length > 0 && mml[mml.length - 1] != '\n') {
            out.write('\r');
            out.write('\n');
        }
        return out.toByteArray();
    }

    private void onBeforeInstructionFetch(BeforeInstructionFetchEvent args) {
        Z80Processor z80 = (Z80Processor) args.getSource();

        if (++instructions > INSTRUCTION_LIMIT) {
            failure = "the compiler ran for " + INSTRUCTION_LIMIT + " instructions without finishing";
            args.getExecutionStopper().stop(false);
            return;
        }

        switch (z80.getRegisters().getPC() & 0xffff) {
        case 0x0000 -> // WBOOT: the compiler is done, whether or not it wrote anything
                args.getExecutionStopper().stop(false);
        case 0x0005 -> { // BDOS
            if (!bdos.process(z80)) {
                failure = "unsupported BDOS function %02xH".formatted(z80.getRegisters().getC() & 0xff);
                args.getExecutionStopper().stop(false);
                return;
            }
            // MSX-DOS answers in HL as well as in AB, and MGSC reads whichever it feels like
            z80.getRegisters().setL(z80.getRegisters().getA());
            z80.getRegisters().setH(z80.getRegisters().getB());
            z80.executeRet();
        }
        case 0x0038 -> // H.TIMI, with no interrupts to service
                z80.executeRet();
        }
    }
}
