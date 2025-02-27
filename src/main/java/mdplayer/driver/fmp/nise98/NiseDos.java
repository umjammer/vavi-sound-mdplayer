package mdplayer.driver.fmp.nise98;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dotnet4j.util.compat.StringUtilities;
import vavi.util.ByteUtil;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


public class NiseDos {

    private static final Logger logger = getLogger(NiseDos.class.getName());

    private final Register286 regs;
    private final Memory98 mem;
    private final FileTemp fileTemp;
    private short paragraphsSize;
    private short paragraphsSizeSeg;
    private short freeBlockSeg;
    private static final int sysVarsStartAddress = 0x10100;
    private static final int fcbStartAddress = 0x10200;
    private static final int mcbStartAddress = 0x10300;
    private static final int PSPStartAddress = 0x20000;
    private static final int InDOSFLAGAdr = 0x11000;

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
    private Path filePath;

    private int allocateMemStartAddress = 0x9_0000;
    private int allocateMemSize;
    private Map<Byte, Runnable> dicHookINT = new HashMap<>();
    private String playingArcFile;
    private List<String> searchPath;

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

        mem.pokeW(InDOSFLAGAdr, (short) 1); // 0: Can be used! 1: Resident programs cannot use system calls!!

        // mem.PokeW(0xfd802, 0x2a27); // EPSON machine!!
        // mem.PokeB(0xfd804, 6); // EPSON PC-286VE
    }

    public void loadAndExecuteFile(String filename, String option /* = "" */, int startSegment /* = PSPStartAddress >> 4 */) {
        logger.log(Level.INFO, "niseDOS>%s %s", filename, option);

        byte[] bin = null;
        try {
            bin = Files.readAllBytes(Path.of(filename));
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
        String fext = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
        loadRunner(bin, fext.equals(".COM"), option, startSegment);

        // Set FCB
        // 0x00 next FCB
        // 0x04 number of files
        // 0x06 The location where the actual information is recorded
        // +0x11 file size (dword)
        mem.PokeB(fcbStartAddress + 0x06 + 0x11 + 0, (byte) bin.length);
        mem.PokeB(fcbStartAddress + 0x06 + 0x11 + 1, (byte) (bin.length >> 8));
        mem.PokeB(fcbStartAddress + 0x06 + 0x11 + 2, (byte) (bin.length >> 16));
        mem.PokeB(fcbStartAddress + 0x06 + 0x11 + 3, (byte) (bin.length >> 24));

        // +0x20 11 byte filename, no path, no period, space padding
        int cnt = 0;
        String fn = Path.of(filename).getFileName().toString().toUpperCase();
        String[] fns = fn.split("\\.");
        fns[0] = fns[0].replaceFirst("\\s*$", "").substring(0, 8);
        fns[1] = fns[1].replaceFirst("\\s*$", "").substring(0, 3);
        fn = fns[0] + fns[1];
        for (char c : fn.toCharArray()) {
            mem.PokeB(fcbStartAddress + 0x06 + 0x20 + cnt, (byte) c);
            cnt++;
            if (cnt == 11) break;
        }

        int PSPAdr = startSegment << 4;

        // MCB located just before the PSP
        mem.PokeB(PSPAdr - 0x10 + 0, (byte) 'Z');
        mem.pokeW(PSPAdr - 0x10 + 1, (short) (PSPStartAddress >> 4));
        mem.pokeW(PSPAdr - 0x10 + 3, (short) 0xffff);

        // Environment variable segment 0x0100 -> real address 0x0_1000 (arbitrary)
        int envPtr = 0x0_1000;
        mem.pokeW(PSPAdr + 0x2c, (short) (envPtr >> 4));
        byte[] env = new byte[] {(byte) 'P', (byte) 'V', (byte) 'I', (byte) '=', (byte) '.', 0, 0, 1, 0};
        int p = 0;
        for (byte c : env) {
            mem.PokeB(envPtr + p++, c);
        }
    }

    public void makeDummyMCB() {
        mem.PokeB(mcbStartAddress + 0x00, (byte) 'M'); // member of a MCB chain, (not last)
        mem.pokeW(mcbStartAddress + 0x01, (short) (PSPStartAddress >> 4)); // free PSP segment address of MCB owner (Process Id)
        mem.pokeW(mcbStartAddress + 0x03, (short) 0);// The size of this mcb
        mem.pokeW(mcbStartAddress + 0x10, (short) (0x20cd - 1)); // apparently other than 0x20cd is needed

        mem.pokeW(0x0_0000 + 0x00ba, (short) (PSPStartAddress >> 4));

        // Environment variable segment 0x0100 -> real address 0x0_1000 (arbitrary)
        int envPtr = mcbStartAddress + 0x10;
        byte[] env = new byte[] {(byte) 'P', (byte) 'V', (byte) 'I', (byte) '=', (byte) '.', 0, 0, 1, 0};
        int p = 0;
        for (byte c : env) {
            mem.PokeB(envPtr + p++, c);
        }
    }

    public void loadImage(byte[] bin, int StartAdr) {
        logger.log(Level.DEBUG, "<NiseDos>LoadImage");
        for (int i = 0; i < bin.length; i++) {
            mem.PokeB(StartAdr + i, bin[i]);
        }
    }

    public void int_(byte imm8) {
        switch (imm8) {
            case 0x18:
                logger.log(Level.DEBUG, "<NiseDos>INT18h AH:$%02x".formatted(regs.getAH()));
                int18();
                break;
            case 0x21:
                logger.log(Level.DEBUG, "<NiseDos>INT21h AH:$%02x".formatted(regs.getAH()));
                INT21();
                break;
            case 0x2f:
                logger.log(Level.DEBUG, "<NiseDos>INT2fh AX:$%04x".formatted(regs.getAX()));
                int2F();
                break;
            default:
                if (checkHookINT(imm8)) {
                    hookINT(imm8);
                    return;
                }

                logger.log(Level.DEBUG, "<NiseDos>INT%02xh AH:$%02x".formatted(imm8, regs.getAH()));
                int ptr = imm8 * 4;
                short ip = (short) mem.peekW(ptr);
                short cs = (short) mem.peekW(ptr + 2);
                if ((ip | cs) == 0) break;

                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.FLAG);
                // regs.SP -= 2;
                // mem.PokeW(regs.SS_SP, regs.DS);
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.getCS());
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.IP);
                regs.IP = ip;
                regs.setCS(cs);
                // regs.DS = cs;

                break;
        }
    }

    private void loadRunner(byte[] prog, boolean IsCom, String option, int StartSegment /* = 0 */) {
        logger.log(Level.DEBUG, "<NiseDos>LoadRunner");
        int ofs;
        int ptr = (StartSegment << 4);
        loadImage(prog, ptr + 0x100);

        if (IsCom) {
            // Setup PSP http: // programmer.main.jp/assembler2/7_5.html

            // 0x80 Number of characters in the argument
            if (StringUtilities.isNullOrEmpty(option)) {
                mem.PokeB(ptr + 0x80, (byte) 0);
            } else {
                byte[] optAry = (option + "\n").getBytes(charset);
                mem.PokeB(ptr + 0x80, (byte) optAry.length);

                // 0x81～0xff Argument Entities
                int i = 0;
                for (byte b : optAry) mem.PokeB(ptr + 0x81 + i++, b);
            }

            regs.setDS((short) StartSegment);
            regs.setCS(regs.getDS());
            regs.IP = 0x100;
            programTerminate = false;
        } else {
            // When exe

            ptr += 0x100;
            short signature = mem.peekW(ptr + 0x00);
            int headerSize = mem.peekW(ptr + 0x08) * 0x10;
            regs.setSS((short) (mem.peekW(ptr + 0x0e) + StartSegment + 0x10));
            regs.setSP(mem.peekW(ptr + 0x10));
            regs.IP = mem.peekW(ptr + 0x14);
            regs.setDS((short) (mem.peekW(ptr + 0x16) + StartSegment + 0x10));
            regs.setCS(regs.getDS());
            regs.setDS((short) (regs.getDS() - 0x10));
            int relocOfs = mem.peekW(ptr + 0x18);
            //int relocSize = headerSize - relocOfs;
            int relocSize = mem.peekW(ptr + 0x06) * 4; // headerSize - relocOfs - 8;
            for (int i = 0; i < relocSize; i += 4) {
                short rOfs = (short) mem.peekW(ptr + relocOfs + i + 0);
                short rSeg = (short) mem.peekW(ptr + relocOfs + i + 2);
                int rPtr = (rSeg << 4) + rOfs;
                short val = (short) mem.peekW(ptr + rPtr + headerSize);
                mem.pokeW(ptr + rPtr + headerSize, (short) (short) (StartSegment + 0x10 + val));
            }
            for (int i = 0; i < prog.length - headerSize; i++) {
                byte b = mem.PeekB(ptr + i + headerSize);
                mem.PokeB(ptr + i, b); // prog[headerSize + i]);
            }
            ptr -= 0x100;

            // 0x80 Number of characters in the argument
            if (StringUtilities.isNullOrEmpty(option)) {
                mem.PokeB(ptr + 0x80, (byte) 0);
            } else {
                byte[] optAry = (option + "\n").getBytes(charset);
                mem.PokeB(ptr + 0x80, (byte) optAry.length);

                // 0x81~0xff Argument Entities
                int i = 0;
                for (byte b : optAry) mem.PokeB(ptr + 0x81 + i++, b);
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
                logger.log(Level.DEBUG, "<NiseDos>  (98)KEY BOARD press check");
                byte KeyGroup = regs.getAL();
                regs.setAH((short) 0x00); // Nothing is being pressed
                break;
            default:
                throw new UnsupportedOperationException(String.format("AH:$%02x".formatted(regs.getAH())));
        }
    }

    private void INT21() {
        List<Byte> msg;
        String text;
        byte b = 0;
        int cnt;
        FileStatus fnd;
        FileStatus fs;
        String filename;

        switch (regs.getAH()) {
            case 0x02:
                msg = new ArrayList<Byte>();
                b = regs.getDL();
                msg.add(b);
                text = new String(ByteUtil.toByteArray(msg), charset);
                System.out.print(text); // Normal console output
                break;
            case 0x09:
                msg = new ArrayList<Byte>();
                cnt = 0;
                do {
                    b = mem.PeekB(regs.getDS_DX() + cnt);
                    if ((char) b == '$') break;
                    msg.add(b);
                    cnt++;
                } while (true);
                text = new String(ByteUtil.toByteArray(msg));
                System.out.print(text); // Normal console output
                break;
            case 0x19:
                logger.log(Level.DEBUG, "<NiseDos>  Get Current Default Drive");
                regs.setAL((short) 2); // 2 => C: drive
                break;
            case 0x25:
                logger.log(Level.DEBUG, "<NiseDos>  SET INTERRUPT VECTOR");
                mem.pokeW(regs.getAL() * 4 + 0, regs.getDX());
                mem.pokeW(regs.getAL() * 4 + 2, regs.getDS());
                break;
            case 0x30:
                logger.log(Level.DEBUG, "<NiseDos>  DOS VERSION");
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
                logger.log(Level.DEBUG, "<NiseDos>  GET ADDRESS OF INDOS FLAG"); // https: // fd.lod.bz/rbil/interrup/dos_kernel/2134.html#2778
                regs.setBX((short) (InDOSFLAGAdr & 0xf));
                regs.setES((short) (InDOSFLAGAdr >> 4));
                break;
            case 0x35:
                logger.log(Level.DEBUG, "<NiseDos>  GET INTERRUPT VECTOR");
                regs.setBX(mem.peekW(regs.getAL() * 4 + 0));
                regs.setES(mem.peekW(regs.getAL() * 4 + 2));
                break;
            case 0x3c:
                logger.log(Level.DEBUG, "<NiseDos>  Create File Using Handle");
                short attribute = regs.getCX();
                msg = new ArrayList<>();
                cnt = 0;
                do {
                    b = mem.PeekB(regs.getDS_DX() + cnt);
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
                fs.lstBuf = new ArrayList<Byte>();
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
                    b = mem.PeekB(regs.getDS_DX() + cnt);
                    if ((char) b == '\0') break;
                    msg.add(b);
                    cnt++;
                } while (true);

                filename = new String(ByteUtil.toByteArray(msg), charset);
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
                logger.log(Level.DEBUG, "<NiseDos>  FILE CLOSE handle=%02x", regs.getBX());
                fnd = searchFileStatus(regs.getBX());
                regs.setCF(true);
                try {
                    if (fnd != null) {
                        files.remove(fnd);
                        if (fnd.mode == 1) {
                            String wFn = fnd.path.resolve(fnd.name).toString();
                            fileTemp.WriteTemp(wFn, ByteUtil.toByteArray(fnd.lstBuf));
                            // File.WriteAllBytes(wFn, fnd.lstBuf.ToArray());
                        }
                        regs.setCF(false);
                    }
                } catch (Exception e) {
                    regs.setCF(true);
                }
                break;
            case 0x3f:
                logger.log(Level.DEBUG, "<NiseDos>  FILE READ handle=%02x", regs.getBX());

                fnd = searchFileStatus(regs.getBX());
                if (fnd == null) {
                    regs.setCF(true);
                    break;
                }
                if (StringUtilities.isNullOrEmpty(fnd.name.toString())) {
                    regs.setCF(true);
                    break;
                }

                byte[] buf = readAllByte(fnd);
                fnd.size = buf.length;
                int size = Math.min((short) regs.getCX(), buf.length - fnd.ptr);
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
                        b = mem.PeekB(regs.getDS_DX() + c);
                        msg.add(b);
                        c++;
                    }
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
                        b = mem.PeekB(regs.getDS_DX() + c);
                        fnd.lstBuf.add(b);
                        c++;
                    }
                    logger.log(Level.DEBUG, "<NiseDos>  WRITE buff length:%d", c);
                    regs.setCF(false);
                    break;
                }

                // output:
                // ERROR
                regs.setCF(true);
                break;
            case 0x42:
                logger.log(Level.DEBUG, "<NiseDos>  SEEK FILE POINTER handle=%02x".formatted(regs.getBX()));
                fnd = searchFileStatus(regs.getBX());
                if (fnd == null) {
                    regs.setCF(true);
                    break;
                }

                int d = (regs.getCX() << 4) + regs.getDX();
                if (regs.getAL() == 0) fnd.ptr = d;
                else if (regs.getAL() == 1) fnd.ptr += d;
                else if (regs.getAL() == 2) fnd.ptr = fnd.size - 1 + d;
                regs.setCF(false);
                regs.setDX((short) ((fnd.ptr >> 4) & 0xf000));
                regs.setAX((short) (fnd.ptr & 0xffff));
                break;
            case 0x43:
                logger.log(Level.DEBUG, "<NiseDos>  Get/Set File Attributes");
                break;
            case 0x47:
                logger.log(Level.DEBUG, "<NiseDos>  Get Current Directory DL=%02x DS:SI[%04x:%04x]", regs.getDL(), regs.getDS(), regs.getSI());
                regs.setCF(false);
                mem.PokeB(regs.getDS_SI(), (byte) '.');
                mem.PokeB(regs.getDS_SI() + 1, (byte) 0x00);
                break;
            case 0x48:
                regs.setCF(false);
                regs.setAX((short) (allocateMemStartAddress / 16));
                regs.setBX(regs.getBX());
                allocateMemSize = regs.getBX() * 16;
                allocateMemStartAddress += allocateMemSize;
                logger.log(Level.DEBUG, "<NiseDos>  Allocate Memory AX(allocatedStartSeg)=%04x BX(paragraphs size)=%04x", regs.getBX(), regs.getAX());
                break;
            case 0x49:
                logger.log(Level.DEBUG, "<NiseDos>  FREE MEMORY"); // https: // fd.lod.bz/rbil/interrup/dos_kernel/2149.html#sect-2975
                regs.setCF(false);
                freeBlockSeg = regs.getES();
                break;
            case 0x4A:
                logger.log(Level.DEBUG, "<NiseDos>  RESIZE MEMORY BLOCK"); // https: // fd.lod.bz/rbil/interrup/dos_kernel/214a.html
                regs.setCF(false);
                paragraphsSize = regs.getBX();
                paragraphsSizeSeg = regs.getES();
                break;
            case 0x4c:
                logger.log(Level.DEBUG, "<NiseDos>  TERMINATE WITH RETURN CODE"); // https: // fd.lod.bz/rbil/interrup/dos_kernel/214c.html#sect-3014
                returnCode = regs.getAL();
                programTerminate = true;
                break;
            case 0x52:
                logger.log(Level.DEBUG, "<NiseDos>  SYSVARS");
                regs.setES((short) 0x1000);
                regs.setBX((short) 0x0100);
                break;
            case 0x57:
                logger.log(Level.DEBUG, "<NiseDos>  Get/Set File Date and Time Using Handle");
                if (regs.getAL() == 0x00) {
                    regs.setCX((short) 0b00000_000000_00000); // hh5bit_mm6bit_ss5bit
                    regs.setDX((short) 0b0000000_0000_00000); // yy7bit_MM4bit_dd5bit
                } else {
                }
                break;
            default:
                throw new UnsupportedOperationException("AH:$%02x".formatted(regs.getAH()));
        }
    }

    private void int2F() {
        if (regs.getAX() == 0x1600) {
            // major version
            regs.setAL((short) 0);
            // minor version
            regs.setAH((short) 0);
            return;
        }

        if (regs.getAX() == 0x4300) {
            regs.setAL((byte) 0x80); // XMS ドライバ有り
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
        filePath = v;
    }

    private byte[] readAllByte(FileStatus fs) {
        try {
            Path fn = fs.path.resolve(fs.name);
            if (fileTemp.ExistTemp(fn.toString()))
                return fileTemp.ReadTemp(fn.toString());
            if (Files.exists(fn))
                return Files.readAllBytes(fn);
        } catch (Exception e) {
        }

        return readAllByteFromArcFile(fs.name.toString());
    }

    private boolean checkFileExist(String filename, /* out */ String[] fndFilename) {
        if (Files.exists(Path.of(filename)) || fileTemp.ExistTemp(filename)) {
            fndFilename[0] = filename;
            return true;
        }
        Path fn = filePath.resolve(filename);
        if (Files.exists(fn) || fileTemp.ExistTemp(fn.toString())) {
            fndFilename[0] = fn.toString();
            return true;
        }

        if (!searchPath.isEmpty()) {
            String f = fn.getFileName().toString();
            for (String fp : searchPath) {
                Path sfn = Path.of(fp, f);
                logger.log(Level.INFO, "Search File: %s".formatted(sfn));
                if (Files.exists(sfn)) {
                    fndFilename[0] = sfn.toString();
                    return true;
                }
            }
        }

        if (playingArcFileExist(filename)) {
            fndFilename[0] = fn.toString();
            return true;
        }

        fndFilename[0] = "";
        return false;
    }

    private boolean playingArcFileExist(String fn) {
        if (playingArcFile.isEmpty()) return false;
        if (!Files.exists(Path.of(playingArcFile))) return false;

        try {
            Archive archive = Archives.getArchive(playingArcFile);
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
        if (!Files.exists(Path.of(playingArcFile))) return null;

        try {
            Archive archive = Archives.getArchive(playingArcFile);
            for (Entry ent : archive.entries()) {
                if (!ent.getName().equals(Path.of(fs).getFileName().toString())) continue;
                return archive.getInputStream(ent).readAllBytes();
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return null;
    }

    public byte[] loadData(String fn) {
        try {
            Path p = filePath.resolve(fn);
            if (fileTemp.ExistTemp(p.toString()))
                return fileTemp.ReadTemp(fn);

            if (Files.exists(p))
                return Files.readAllBytes(p);

            if (!searchPath.isEmpty()) {
                String f = p.getFileName().toString();
                for (String fp : searchPath) {
                    Path sfn = Path.of(fp, f);
                    logger.log(Level.INFO, "Search File: %s", sfn);
                    if (Files.exists(sfn)) {
                        byte[] b = Files.readAllBytes(sfn);
                        logger.log(Level.INFO, "read data size: %s", b.length);
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
        this.playingArcFile = playingArcFileName;
    }

    public void setSearchPath(List<String> searchPaths) {
        this.searchPath = searchPaths;
    }
}
