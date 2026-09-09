/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.mgsc;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;

import konamiman.z80.Z80Processor;

import static java.lang.System.getLogger;


/**
 * As much of MSX-DOS as MGSC.COM asks for: the handful of BDOS functions it calls, over a
 * couple of files that only ever live in memory.
 * <p>
 * Ported from {@code bdos.cpp} of <a href="https://github.com/digital-sound-antiques/mgsc">mgsc</a>.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-30 nsano initial version <br>
 */
class BdosEmulator {

    private static final Logger logger = getLogger(BdosEmulator.class.getName());

    /** a record is what the guest reads and writes a file in */
    private static final int RECORD_SIZE = 128;

    /**
     * The disk MGSC.COM sees: {@code "TEMP.MUS"} going in, {@code "TEMP.MGS"} coming out.
     * A file is just its bytes - every read and write names the record it wants, so there is
     * no position to keep.
     */
    final Map<String, byte[]> files = new HashMap<>();

    /** what the compiler wrote to the console, kept so the caller can report it */
    final StringBuilder console = new StringBuilder();

    /** where the guest wants the next record read into, or taken from */
    private int dta;

    private final Fcb fcb = new Fcb();

    void reset() {
        dta = 0x80;
        files.clear();
        console.setLength(0);
    }

    /**
     * Serves one {@code CALL 5}.
     *
     * @return false when the function is one we have never taught it, which is the caller's cue
     *         to give up rather than let the compiler carry on believing it worked
     */
    boolean process(Z80Processor z80) {
        int de = z80.getRegisters().getDE() & 0xffff;
        int function = z80.getRegisters().getC() & 0xff;

        switch (function) {
        case 0x00: // system reset
            return false;

        case 0x02: // console out
            console.append((char) (z80.getRegisters().getE() & 0xff));
            return true;

        case 0x0f: { // open file
            readFcb(z80);
            byte[] file = files.get(fcb.getFilename());
            if (file == null) {
                z80.getRegisters().setA((byte) 0xff);
                return true;
            }
            fcb.drive = 0x01;
            fcb.fileSize = file.length;
            fcb.recordSize = RECORD_SIZE;
            fcb.deviceId = 0x40 + fcb.drive;
            fcb.firstCluster = 0x01;
            fcb.lastCluster = 0x20;
            fcb.relativePosition = 0x00;
            fcb.date = 0x1234;
            fcb.time = 0x1234;
            writeFcb(z80);
            z80.getRegisters().setA((byte) 0);
            return true;
        }

        case 0x10: // close file
            z80.getRegisters().setA((byte) 0);
            return true;

        case 0x16: // create file
            readFcb(z80);
            files.computeIfAbsent(fcb.getFilename(), k -> new byte[0]);
            fcb.deviceId = 0x40 + fcb.drive;
            writeFcb(z80);
            z80.getRegisters().setA((byte) 0);
            return true;

        case 0x1a: // set DTA
            dta = de;
            return true;

        case 0x21: { // random read
            readFcb(z80);
            byte[] file = files.get(fcb.getFilename());
            if (file == null || fcb.fileSize < RECORD_SIZE * fcb.randomRecord) {
                z80.getRegisters().setA((byte) 0x01);
                return true;
            }
            byte[] record = new byte[RECORD_SIZE];
            int offset = RECORD_SIZE * fcb.randomRecord;
            if (offset < file.length) {
                System.arraycopy(file, offset, record, 0, Math.min(RECORD_SIZE, file.length - offset));
            }
            writeFcb(z80);
            z80.getMemory().setContents(dta, record, 0, null);
            z80.getRegisters().setA((byte) 0);
            return true;
        }

        case 0x22: { // random write
            readFcb(z80);
            byte[] file = files.get(fcb.getFilename());
            if (file == null) {
                z80.getRegisters().setA((byte) 0x01);
                return true;
            }
            int offset = RECORD_SIZE * fcb.randomRecord;
            if (offset + RECORD_SIZE > file.length) {
                byte[] grown = new byte[offset + RECORD_SIZE];
                System.arraycopy(file, 0, grown, 0, file.length);
                file = grown;
                files.put(fcb.getFilename(), file);
            }
            System.arraycopy(z80.getMemory().getContents(dta, RECORD_SIZE), 0, file, offset, RECORD_SIZE);
            fcb.fileSize = file.length;
            writeFcb(z80);
            z80.getRegisters().setA((byte) 0);
            return true;
        }

        case 0x6f: // MSX-DOS version
            z80.getRegisters().setA((byte) 0);
            z80.getRegisters().setB((byte) 0);
            return true;

        default:
logger.log(Level.DEBUG, "BDOS: unsupported function %02x".formatted(function));
            return false;
        }
    }

    private void readFcb(Z80Processor z80) {
        fcb.set(z80.getMemory().getContents(z80.getRegisters().getDE() & 0xffff, Fcb.SIZE));
    }

    private void writeFcb(Z80Processor z80) {
        z80.getMemory().setContents(z80.getRegisters().getDE() & 0xffff, fcb.get(), 0, null);
    }
}
