package mdplayer.emu.nise98;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdplayer.emu.common.Utils;
import vavi.util.ByteUtil;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;

import static java.lang.System.getLogger;
import static vavi.util.compat.Util.isNullOrEmpty;


public class NiseDos {

    private static final Logger logger = getLogger(NiseDos.class.getName());

    /** the TRACE lines below are per emulated software interrupt - see the note in {@link Nise286} */
    private static final boolean tracing = logger.isLoggable(Level.TRACE);

    public Charset charset;

    private final Register286 regs;
    private final Memory98 mem;
    private final FileTemp fileTemp;
    private short paragraphsSize;
    private short paragraphsSizeSeg;
    private short freeBlockSeg;
    private static final int sysVarsStartAddress = 0x10100;
    private static final int fcbStartAddress = 0x1_0200;
    private static final int mcbStartAddress = 0x1_0300;
    private static final int pspStartAddress = 0x2_0000;
    private static final int inDOSFLAGAdr = 0x1_1000;

    public static class FileStatus {

        public Path name;
        public int ptr = 0;
        public int size = 0;
        public Path path;
        public int handle = 0;
        public int mode = 0;
        public List<Byte> lstBuf = new ArrayList<>();
    }

    private final List<FileStatus> files = new ArrayList<>();
    private int fileHandler = 10;
    private Path filePath = Path.of(".");

    private int allocateMemStartAddress = 0x9_0000;
    private int allocateMemSize;
    private final Map<Byte, Runnable> dicHookINT = new HashMap<>();
    private String playingArcFile = "";
    private List<String> searchPath = new ArrayList<>();

    private byte returnCode = 0x00;

    public byte getReturnCode() {
        return returnCode;
    }

    private boolean programTerminate = false;

    public boolean getProgramTerminate() {
        return programTerminate;
    }

    public void setProgramTerminate(boolean value) {
        programTerminate = value;
    }

    public NiseDos(Register286 regs, Memory98 mem, FileTemp fileTemp) {
        this.regs = regs;
        this.mem = mem;
        this.fileTemp = fileTemp;

        makeSysVars();

        mem.pokeW(inDOSFLAGAdr, (short) 1); // 0: Can be used! 1: Resident programs cannot use system calls!!

        // mem.pokeW(0xfd802, 0x2a27); // EPSON machine!!
        // mem.pokeB(0xfd804, 6); // EPSON PC-286VE
    }

    public void loadAndExecuteFile(String filename, String option /* = "" */, int startSegment /* = pspStartAddress >> 4 */) {
        logger.log(Level.INFO, "niseDOS>%s %s".formatted(filename, Path.of(option).getFileName().toString()));

        byte[] bin;
        try {
            bin = Files.readAllBytes(Path.of(filename));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        String fext = filename.substring(filename.lastIndexOf('.')).toUpperCase();
        loadRunner(bin, fext.equals(".COM"), option, startSegment);

        // Set FCB
        // 0x00 next FCB
        // 0x04 number of files
        // 0x06 The location where the actual information is recorded
        // +0x11 file size (dword)
        mem.pokeB(fcbStartAddress + 0x06 + 0x11 + 0, (byte) (bin.length & 0xff));
        mem.pokeB(fcbStartAddress + 0x06 + 0x11 + 1, (byte) ((bin.length & 0xff00) >> 8));
        mem.pokeB(fcbStartAddress + 0x06 + 0x11 + 2, (byte) ((bin.length & 0xff_0000) >> 16));
        mem.pokeB(fcbStartAddress + 0x06 + 0x11 + 3, (byte) ((bin.length & 0xff00_0000) >> 24));

        // +0x20 11 byte filename, no path, no period, space padding
        int cnt = 0;
        String fn = Path.of(filename).getFileName().toString().toUpperCase();
        String[] fns = fn.split("\\.");
        fns[0] = (fns[0] + " ".repeat(8)).substring(0, 8);
        fns[1] = (fns[1] + " ".repeat(3)).substring(0, 3);
        fn = fns[0] + fns[1];
        for (char c : fn.toCharArray()) {
            mem.pokeB(fcbStartAddress + 0x06 + 0x20 + cnt, (byte) c);
            cnt++;
            if (cnt == 11) break;
        }

        int pspAdr = startSegment << 4;

        // MCB located just before the PSP
        mem.pokeB(pspAdr - 0x10 + 0, (byte) 'Z');
        mem.pokeW(pspAdr - 0x10 + 1, (short) (pspStartAddress >> 4));
        mem.pokeW(pspAdr - 0x10 + 3, (short) 0xffff);

        // Environment variable segment 0x0100 -> real address 0x0_1000 (arbitrary)
        int envPtr = 0x0_1000;
        mem.pokeW(pspAdr + 0x2c, (short) (envPtr >> 4));
        byte[] env = {(byte) 'P', (byte) 'V', (byte) 'I', (byte) '=', (byte) '.', 0, 0, 1, 0};
        int p = 0;
        for (byte c : env) {
            mem.pokeB(envPtr + p++, c);
        }
    }

    public void makeDummyMCB() {
        mem.pokeB(mcbStartAddress + 0x00, (byte) 'M'); // member of a MCB chain, (not last)
        mem.pokeW(mcbStartAddress + 0x01, (short) (pspStartAddress >> 4)); // free PSP segment address of MCB owner (Process Id)
        mem.pokeW(mcbStartAddress + 0x03, (short) 0);// The size of this mcb
        mem.pokeW(mcbStartAddress + 0x10, (short) (0x20cd - 1)); // apparently other than 0x20cd is needed

        mem.pokeW(0x0_0000 + 0x00ba, (short) (pspStartAddress >> 4));

        // Environment variable segment 0x0100 -> real address 0x0_1000 (arbitrary)
        int envPtr = mcbStartAddress + 0x10;
        byte[] env = {(byte) 'P', (byte) 'V', (byte) 'I', (byte) '=', (byte) '.', 0, 0, 1, 0};
        int p = 0;
        for (byte c : env) {
            mem.pokeB(envPtr + p++, c);
        }
    }

    public void loadImage(byte[] bin, int startAdr) {
        logger.log(Level.TRACE, "<NiseDos>LoadImage");
        for (int i = 0; i < bin.length; i++) {
            mem.pokeB(startAdr + i, bin[i]);
        }
    }

    public void int_(byte imm8) {
        switch (imm8) {
            case 0x18:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>INT18h AH:$%02x".formatted(regs.getAH() & 0xff));
                int18();
                break;
            case 0x21:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>INT21h AH:$%02x".formatted(regs.getAH() & 0xff));
                int21();
                break;
            case 0x2f:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>INT2fh AX:$%04x".formatted(regs.getAX() & 0xffff));
                int2F();
                break;
            default:
                if (checkHookINT(imm8)) {
                    hookINT(imm8);
                    return;
                }

                if (tracing) logger.log(Level.TRACE, "<NiseDos>INT%02xh AH:$%02x".formatted(imm8 & 0xff, regs.getAH() & 0xff));
                int ptr = (imm8 & 0xff) * 4;
                short ip = mem.peekW(ptr);
                short cs = mem.peekW(ptr + 2);
                if ((ip | cs) == 0) break;

                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.flag);
                // regs.SP -= 2;
                // mem.PokeW(regs.SS_SP, regs.DS);
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.getCS());
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.ip);
                regs.ip = ip;
                regs.setCS(cs);
                // regs.DS = cs;

                break;
        }
    }

    private void loadRunner(byte[] prog, boolean isCom, String option, int startSegment /* = 0 */) {
        logger.log(Level.TRACE, "<NiseDos>LoadRunner");
        int ofs;
        int ptr = (startSegment << 4);
        loadImage(prog, ptr + 0x100);

        if (isCom) {
            // Setup PSP https://programmer.main.jp/assembler2/7_5.html

            // 0x80 Number of characters in the argument
            if (isNullOrEmpty(option)) {
                mem.pokeB(ptr + 0x80, (byte) 0);
            } else {
                byte[] optAry = (option + "\r").getBytes(charset);
                mem.pokeB(ptr + 0x80, (byte) optAry.length);

                // 0x81～0xff Argument Entities
                int i = 0;
                for (byte b : optAry) mem.pokeB(ptr + 0x81 + i++, b);
            }

            regs.setDS((short) startSegment);
            regs.setCS(regs.getDS());
            regs.ip = 0x100;
            programTerminate = false;
        } else {
            // When exe

            ptr += 0x100;
            short signature = mem.peekW(ptr + 0x00);
            int headerSize = mem.peekW(ptr + 0x08) * 0x10;
            regs.setSS((short) (mem.peekW(ptr + 0x0e) + startSegment + 0x10));
            regs.setSP(mem.peekW(ptr + 0x10));
            regs.ip = mem.peekW(ptr + 0x14);
            regs.setDS((short) (mem.peekW(ptr + 0x16) + startSegment + 0x10));
            regs.setCS(regs.getDS());
            regs.setDS((short) ((regs.getDS() & 0xffff) - 0x10));
            int relocOfs = mem.peekW(ptr + 0x18);
            //int relocSize = headerSize - relocOfs;
            int relocSize = mem.peekW(ptr + 0x06) * 4; // headerSize - relocOfs - 8;
            for (int i = 0; i < relocSize; i += 4) {
                short rOfs = mem.peekW(ptr + relocOfs + i + 0);
                short rSeg = mem.peekW(ptr + relocOfs + i + 2);
                int rPtr = ((rSeg & 0xffff) << 4) + (rOfs & 0xffff);
                short val = mem.peekW(ptr + rPtr + headerSize);
                mem.pokeW(ptr + rPtr + headerSize, (short) (startSegment + 0x10 + (val & 0xffff)));
            }
            for (int i = 0; i < prog.length - headerSize; i++) {
                byte b = mem.peekB(ptr + i + headerSize);
                mem.pokeB(ptr + i, b); // prog[headerSize + i]);
            }
            ptr -= 0x100;

            // 0x80 Number of characters in the argument
            if (isNullOrEmpty(option)) {
                mem.pokeB(ptr + 0x80, (byte) 0);
            } else {
                byte[] optAry = (option + "\r").getBytes(charset);
                mem.pokeB(ptr + 0x80, (byte) optAry.length);

                // 0x81~0xff Argument Entities
                int i = 0;
                for (byte b : optAry) mem.pokeB(ptr + 0x81 + i++, b);
            }
        }
    }

    private void makeSysVars() {
        // 0x10100

        // FCB Start Adr 0x10200
        mem.pokeW(sysVarsStartAddress - 2, (short) (mcbStartAddress >> 4));
        mem.pokeW(sysVarsStartAddress + 4, (short) (fcbStartAddress & 0xf));
        mem.pokeW(sysVarsStartAddress + 6, (short) (fcbStartAddress >> 4));
    }

    private void int18() {
        List<Byte> msg;
        String text;
        byte b = 0;
        int cnt;
        FileStatus fnd;
        switch (regs.getAH()) {
            case 0x04:
                logger.log(Level.TRACE, "<NiseDos>  (98)KEY BOARD press check");
                byte keyGroup = regs.getAL();
                regs.setAH((byte) 0x00); // Nothing is being pressed
                break;
            default:
                throw new UnsupportedOperationException("AH:$%02x".formatted(regs.getAH() & 0xff));
        }
    }

    private void int21() {
        List<Byte> msg;
        String text;
        byte b = 0;
        int cnt;
        FileStatus fnd;
        FileStatus fs;
        String filename;

        switch (regs.getAH()) {
            case 0x02:
                msg = new ArrayList<>();
                b = regs.getDL();
                msg.add(b);
                text = new String(ByteUtil.toByteArray(msg), charset);
                System.out.print(text); // Normal console output
                break;
            case 0x09:
                msg = new ArrayList<>();
                cnt = 0;
                do {
                    b = mem.peekB(regs.getDS_DX() + cnt);
                    if ((char) b == '$') break;
                    msg.add(b);
                    cnt++;
                } while (true);
                text = new String(ByteUtil.toByteArray(msg), charset);
                System.out.print(text); // Normal console output
                break;
            case 0x19:
                logger.log(Level.TRACE, "<NiseDos>  Get Current Default Drive");
                regs.setAL((byte) 2); // 2 => C: drive
                break;
            case 0x25:
                logger.log(Level.TRACE, "<NiseDos>  SET INTERRUPT VECTOR");
                mem.pokeW((regs.getAL() & 0xff) * 4 + 0, regs.getDX());
                mem.pokeW((regs.getAL() & 0xff) * 4 + 2, regs.getDS());
                break;
            case 0x30:
                logger.log(Level.TRACE, "<NiseDos>  DOS VERSION");
                // major version
                regs.setAX((short) 4); // dos 4.x
                break;
            case 0x31:
                logger.log(Level.DEBUG, "<NiseDos>  TERMINATE BUT STAY RESIDENT");
                returnCode = regs.getAL();
                paragraphsSize = regs.getDX();
                programTerminate = true;
                break;
            case 0x34:
                logger.log(Level.TRACE, "<NiseDos>  GET ADDRESS OF INDOS flag"); // https://fd.lod.bz/rbil/interrup/dos_kernel/2134.html#2778
                regs.setBX((short) (inDOSFLAGAdr & 0xf));
                regs.setES((short) (inDOSFLAGAdr >> 4));
                break;
            case 0x35:
                logger.log(Level.TRACE, "<NiseDos>  GET INTERRUPT VECTOR");
                regs.setBX(mem.peekW((regs.getAL() & 0xff) * 4 + 0));
                regs.setES(mem.peekW((regs.getAL() & 0xff) * 4 + 2));
                break;
            case 0x3c:
                logger.log(Level.DEBUG, "<NiseDos>  Create File Using Handle");
                short attribute = regs.getCX();
                msg = new ArrayList<>();
                cnt = 0;
                do {
                    b = mem.peekB(regs.getDS_DX() + cnt);
                    if ((char) b == '\0') break;
                    msg.add(b);
                    cnt++;
                } while (true);

                filename = new String(ByteUtil.toByteArray(msg), charset);
                logger.log(Level.DEBUG, filename);

                fs = new FileStatus();
                files.add(fs);
                fs.name = Path.of(filename).getFileName();
                fs.ptr = 0;
                fs.path = Path.of(filename).getParent();
                fs.handle = fileHandler++;
                fs.mode = 1;
                fs.lstBuf = new ArrayList<>();
                setPath(fs.path);
                makeDummyMCB();

                regs.setCF(false);
                regs.setAX((short) fs.handle); // file handle

                break;
            case 0x3d:
                logger.log(Level.DEBUG, "<NiseDos>  FILE OPEN");
                msg = new ArrayList<>();
                cnt = 0;
                do {
                    b = mem.peekB(regs.getDS_DX() + cnt);
                    if ((char) b == '\0') break;
                    msg.add(b);
                    cnt++;
                } while (true);

                filename = new String(ByteUtil.toByteArray(msg), charset).replace("\\", File.separator);
                logger.log(Level.DEBUG, filename);

                String[] fndFilename = new String[1];
                if (checkFileExist(filename, /* out */ fndFilename)) {
                    regs.setCF(false);
                    fs = new FileStatus();
                    files.add(fs);
                    fs.name = Path.of(fndFilename[0]).getFileName();
                    fs.ptr = 0;
                    fs.path = Path.of(fndFilename[0]).getParent();
                    fs.handle = fileHandler++;
                    setPath(fs.path);
                    regs.setAX((short) fs.handle); // file handle
                    makeDummyMCB();
                } else {
                    regs.setCF(true);
                    regs.setAX((short) 1);
                }
                break;
            case 0x3e:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>  FILE CLOSE handle=%02x".formatted(regs.getBX() & 0xff));
                fnd = searchFileStatus(regs.getBX());
                regs.setCF(true);
                try {
                    if (fnd != null) {
                        files.remove(fnd);
                        if (fnd.mode == 1) {
                            String wFn = fnd.path.resolve(fnd.name).toString();
                            fileTemp.writeTemp(wFn, ByteUtil.toByteArray(fnd.lstBuf));
                            // File.WriteAllBytes(wFn, fnd.lstBuf.ToArray());
                        }
                        regs.setCF(false);
                    }
                } catch (Exception e) {
                    regs.setCF(true);
                }
                break;
            case 0x3f:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>  FILE READ handle=%02x".formatted(regs.getBX() & 0xff));

                fnd = searchFileStatus(regs.getBX());
                if (fnd == null) {
                    regs.setCF(true);
                    break;
                }
                if (isNullOrEmpty(fnd.name.toString())) {
                    regs.setCF(true);
                    break;
                }

                byte[] buf = readAllByte(fnd);
                fnd.size = buf.length;
                int size = Math.min(regs.getCX() & 0xffff, buf.length - fnd.ptr);
                byte[] rbuf = new byte[size];
                System.arraycopy(buf, fnd.ptr, rbuf, 0, size);
                loadImage(rbuf, regs.getDS_DX());
                fnd.ptr += size;

                // return
                regs.setAX((short) size);
                regs.setCF(false);

                break;
            case 0x40:
                logger.log(Level.DEBUG, "<NiseDos>  'WRITE'-WRITE TO FILE OR DEVICE");
                // input:
                // BX = file handle
                // CX = number of bytes to write
                // DS: DX->data to write

                fnd = searchFileStatus(regs.getBX());
                if (fnd == null) {
                    msg = new ArrayList<>();
                    int c = 0;
                    while (c < regs.getCX()) {
                        b = mem.peekB(regs.getDS_DX() + c);
                        msg.add(b);
                        c++;
                    }
logger.log(Level.TRACE, "error message from program");
                    text = new String(ByteUtil.toByteArray(msg), charset);
                    System.out.print(text); // Normal console output

                    // output:
                    // ERROR
                    regs.setCF(true);
                    break;
                }

                if (fnd.mode == 1) {
                    int c = 0;
                    while (c < regs.getCX()) {
                        b = mem.peekB(regs.getDS_DX() + c);
                        fnd.lstBuf.add(b);
                        c++;
                    }
                    logger.log(Level.DEBUG, "<NiseDos>  WRITE buff length:%d".formatted(c));
                    regs.setCF(false);
                    break;
                }

                // output:
                // ERROR
                regs.setCF(true);
                break;
            case 0x42:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>  SEEK FILE POINTER handle=%02x".formatted(regs.getBX() & 0xff));
                fnd = searchFileStatus(regs.getBX());
                if (fnd == null) {
                    regs.setCF(true);
                    break;
                }

                int d = ((regs.getCX() & 0xffff) << 4) + (regs.getDX() & 0xffff);
                if (regs.getAL() == 0) fnd.ptr = d;
                else if (regs.getAL() == 1) fnd.ptr += d;
                else if (regs.getAL() == 2) fnd.ptr = fnd.size - 1 + d;
                regs.setCF(false);
                regs.setDX((short) ((fnd.ptr >> 4) & 0xf000));
                regs.setAX((short) (fnd.ptr & 0xffff));
                break;
            case 0x43:
                logger.log(Level.TRACE, "<NiseDos>  Get/Set File Attributes");
                break;
            case 0x47:
                if (tracing) logger.log(Level.TRACE, "<NiseDos>  Get Current Directory DL=%02x DS:SI[%04x:%04x]".formatted(regs.getDL() & 0xff, regs.getDS() & 0xffff, regs.getSI() & 0xffff));
                regs.setCF(false);
                mem.pokeB(regs.getDS_SI(), (byte) '.');
                mem.pokeB(regs.getDS_SI() + 1, (byte) 0x00);
                break;
            case 0x48:
                regs.setCF(false);
                regs.setAX((short) (allocateMemStartAddress / 16));
                regs.setBX(regs.getBX());
                allocateMemSize = regs.getBX() * 16;
                allocateMemStartAddress += allocateMemSize;
                if (tracing) logger.log(Level.TRACE, "<NiseDos>  Allocate Memory AX(allocatedStartSeg)=%04x BX(paragraphs size)=%04x".formatted(regs.getBX() & 0xffff, regs.getAX() & 0xffff));
                break;
            case 0x49:
                logger.log(Level.TRACE, "<NiseDos>  FREE MEMORY"); // https://fd.lod.bz/rbil/interrup/dos_kernel/2149.html#sect-2975
                regs.setCF(false);
                freeBlockSeg = regs.getES();
                break;
            case 0x4A:
                logger.log(Level.TRACE, "<NiseDos>  RESIZE MEMORY BLOCK"); // https://fd.lod.bz/rbil/interrup/dos_kernel/214a.html
                regs.setCF(false);
                paragraphsSize = regs.getBX();
                paragraphsSizeSeg = regs.getES();
                break;
            case 0x4c:
                logger.log(Level.TRACE, "<NiseDos>  TERMINATE WITH RETURN CODE"); // https://fd.lod.bz/rbil/interrup/dos_kernel/214c.html#sect-3014
                returnCode = regs.getAL();
                programTerminate = true;
                break;
            case 0x52:
                logger.log(Level.TRACE, "<NiseDos>  SYSVARS");
                regs.setES((short) 0x1000);
                regs.setBX((short) 0x0100);
                break;
            case 0x57:
                logger.log(Level.TRACE, "<NiseDos>  Get/Set File Date and Time Using Handle");
                if (regs.getAL() == 0x00) {
                    regs.setCX((short) 0b00000_000000_00000); // hh5bit_mm6bit_ss5bit
                    regs.setDX((short) 0b0000000_0000_00000); // yy7bit_MM4bit_dd5bit
                } else {
                }
                break;
            default:
                throw new UnsupportedOperationException("AH:$%02x".formatted(regs.getAH() & 0xff));
        }
    }

    private void int2F() {
        if (regs.getAX() == 0x1600) {
            // major version
            regs.setAL((byte) 0);
            // minor version
            regs.setAH((byte) 0);
            return;
        }

        if (regs.getAX() == 0x4300) {
            regs.setAL((byte) 0x80); // XMS driver available
            return;
        }
        if (regs.getAX() == 0x4310) {
            regs.setES((short) 0xe000);
            regs.setBX((short) 0x0000);
        }
    }

    public void setHookINT(byte intNum, Runnable action) {
        if (dicHookINT.containsKey(intNum))
            dicHookINT.remove(intNum);

        dicHookINT.put(intNum, action);
    }

    private boolean checkHookINT(byte imm8) {
        if (dicHookINT.containsKey(imm8)) return true;
        return false;
    }

    private void hookINT(byte imm8) {
        dicHookINT.get(imm8).run();
    }

    public void setPath(Path v) {
logger.log(Level.DEBUG, "dos path: " + v);
        filePath = v;
    }

    private byte[] readAllByte(FileStatus fs) {
        try {
            Path fn = fs.path.resolve(fs.name);
            if (fileTemp.existTemp(fn.toString()))
                return fileTemp.readTemp(fn.toString());
            if (Files.exists(fn))
                return Files.readAllBytes(fn);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return readAllByteFromArcFile(fs.name.toString());
    }

    private boolean checkFileExist(String filename, /* out */ String[] fndFilename) {
        if (Files.exists(Path.of(filename)) || fileTemp.existTemp(filename)) {
            fndFilename[0] = filename;
logger.log(Level.INFO, "file found: '" + filename + "' as '" + fndFilename[0] + "', in temp: " + fileTemp.existTemp(filename));
            return true;
        }
        Path fn = filePath.resolve(Path.of(filename).getFileName());
        Path realFn = Utils.fileExistsIgnoreCase(fn);
        if (realFn != null || fileTemp.existTemp(fn.toString())) {
            fndFilename[0] = realFn != null ? realFn.toString() : fn.toString();
logger.log(Level.INFO, "file found: '" + filename + "' as '" + fndFilename[0] + "', in temp: " + fileTemp.existTemp(filename));
            return true;
        }

        if (!searchPath.isEmpty()) {
            String f = fn.getFileName().toString();
            for (String fp : searchPath) {
                Path sfn = Path.of(fp, f);
                logger.log(Level.INFO, "Search File: %s".formatted(sfn));
                Path realSfn = Utils.fileExistsIgnoreCase(sfn);
                if (realSfn != null) {
                    fndFilename[0] = realSfn.toString();
logger.log(Level.INFO, "file found in searchPath: " + filename + " as " + fndFilename[0]);
                    return true;
                }
            }
        }

        if (playingArcFileExist(filename)) {
            fndFilename[0] = fn.toString();
logger.log(Level.INFO, "file found in arc: " + filename + " as " + fndFilename[0]);
            return true;
        }

        fndFilename[0] = "";
logger.log(Level.INFO, "file not found: " + filename);
        return false;
    }

    private boolean playingArcFileExist(String fn) {
        if (playingArcFile.isEmpty()) return false;
        Path realPath = Utils.fileExistsIgnoreCase(Path.of(playingArcFile));
        if (realPath == null) return false;

        try {
            Archive archive = Archives.getArchive(realPath);
            for (Entry ent : archive.entries()) {
                if (ent.getName().equals(fn))
                    return true;
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return false;
    }

    private byte[] readAllByteFromArcFile(String fs) {
        if (playingArcFile.isEmpty()) return null;
        Path realPath = Utils.fileExistsIgnoreCase(Path.of(playingArcFile));
        if (realPath == null) return null;

        try {
            Archive archive = Archives.getArchive(playingArcFile);
            for (Entry ent : archive.entries()) {
                String entryFileName = Path.of(ent.getName()).getFileName().toString();
                String targetFileName = Path.of(fs).getFileName().toString();
                if (!entryFileName.equalsIgnoreCase(targetFileName)) continue;
                return archive.getInputStream(ent).readAllBytes();
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return null;
    }

    public byte[] loadData(String fn) {
        try {
            Path p = filePath.resolve(fn.replace("\\", File.separator));
            if (fileTemp.existTemp(p.toString()))
                return fileTemp.readTemp(fn);

            Path realPath = Utils.fileExistsIgnoreCase(p);
            if (realPath != null)
                return Files.readAllBytes(realPath);

            if (!searchPath.isEmpty()) {
                String f = p.getFileName().toString();
                for (String fp : searchPath) {
                    Path sfn = Path.of(fp, f);
                    logger.log(Level.INFO, "Search File: %s".formatted(sfn));
                    Path realSfn = Utils.fileExistsIgnoreCase(sfn);
                    if (realSfn != null) {
                        byte[] b = Files.readAllBytes(realSfn);
                        logger.log(Level.INFO, "read data size: %s".formatted(b.length));
                        return b;
                    }
                }
            }

            return readAllByteFromArcFile(fn);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FileStatus searchFileStatus(int handle) {
        for (FileStatus fs : files) {
            if (fs.handle == regs.getBX()) {
                return fs;
            }
        }
        return null;
    }

    public void setArcFile(String playingArcFileName) {
        if (playingArcFileName != null) {
            this.playingArcFile = playingArcFileName;
        }
    }

    public void setSearchPath(List<String> searchPaths) {
        if (searchPaths != null) {
            this.searchPath = searchPaths;
        }
    }
}
