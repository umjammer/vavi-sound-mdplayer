package mdplayer.driver.zms.nise68;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.MemoryStream;
import dotnet4j.io.Path;
import dotnet4j.io.SeekOrigin;
import vavi.util.ByteUtil;
import vavi.util.StringUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


public class NiseHuman {

    private static final Logger logger = getLogger(NiseHuman.class.getName());

    public static int mpcmPtr = 0xfe_9000; // MPCM permanent location (tentative)

    private final Memory68 mem;
    private final Register68 reg;
    public boolean programTerminate;
    public int returnCode;
    private int beforePSP;
    private int envAddress = 0;
    private int ctrlCAbortAddress = 0;
    private int errorAbortAddress = 0;
    private int cmdLineAddress = 0;
    private int pspSize = 16 + 240;
    public int defUSP = 0xfe_0000;
    public int defSSP = 0xff_0000;
    private Runnable[] tblFunc = new Runnable[256];
    private Runnable[] tblFEFunc = new Runnable[256];

    public MemMng memMng;
    private final ProcInfo currentProc = new ProcInfo();
    private int execPtr;
    private int fileHandle = 0;
    private final FileIni[] fi = new FileIni[256];
    private String currentWorkPath = "C:\\"; // The actual path that makes niseHuman think it is C:\\
    //public Dictionary<String, byte[]> fb = new Dictionary<String, byte[]>();
    private final List<String> envZPDs;
    private final FileMng fileMng;

    public static class ProcInfo {

        public int startAddress;
    }

    public NiseHuman(Memory68 mem, Register68 reg, List<String> envZPDs, FileMng fm) {
        this.mem = mem;
        this.reg = reg;
        this.envZPDs = envZPDs;
        this.fileMng = fm;

        programTerminate = false;
        returnCode = 0;
        beforePSP = 0;
        memMng = new MemMng(0x0004_0000);
        //defUSP = (int)memMng.Malloc(0x1_0000);
        //defSSP = (int)memMng.Malloc(0x1_0000);

        tblFunc = new Runnable[] {
                // 0x00
                this::exit, null, this::putChar, null, null, null, null, this::inKey, null, this::print, null, null, null, null, null, this::drvctrl,
                // 0x10
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, this::fputs, null,
                // 0x20
                this::super_, null, null, this::conCtrl, null, this::intVcs, null, null, null, null, null, null, null, null, null, null,
                // 0x30
                this::verNum, this::keepPr, null, null, null, null, null, this::nameck, null, null, null, null, this::create, this::open, this::close, this::read,

                // 0x40
                this::write, this::delete, this::seek, null, null, null, null, null, this::malloc, this::mFree, this::setBlock, this::exec, this::exit2, null, this::files, null,
                // 0x50
                null, this::getPsp, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x60
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x70
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,

                // 0x80
                null, this::getPsp, null, null, null, null, null, this::fileDate, null, null, this::makeTmp, null, null, null, null, null,
                // 0x90
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xa0
                null, null, null, null, null, null, null, null, null, null, null, null, null, this::s_malloc, this::s_mfree, null,
                // 0xb0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,

                // 0xc0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xd0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xe0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xf0
                null, null, null, null, null, null, null, this::bus_err, null, null, null, null, null, null, null, null,
        };

        tblFEFunc = new Runnable[] {
                // 0x00
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x10
                null, this::ltos, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x20
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x30
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,

                // 0x40
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x50
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x60
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x70
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,

                // 0x80
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x90
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xa0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xb0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,

                // 0xc0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xd0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xe0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xf0
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        };
    }

    public void loadAndExecuteFile(String filename, String option, int startAddress) {
        logger.log(Level.INFO, "niseHuman>%s %s".formatted(filename, option));
        logger.log(Level.INFO, "CurrentWorkPath>%s".formatted(currentWorkPath));

        currentWorkPath = fileMng.VCurrentPath;
        if (currentWorkPath == null || currentWorkPath.isEmpty()) currentWorkPath = "C:\\";

        byte[] bin = fileMng.vReadAllBytes(filename);
        String fext = Path.getExtension(filename).toUpperCase();
        loadRunner(bin, fext.equals(".R"), option, startAddress);
    }

    private void loadRunner(byte[] prog, boolean isR, String option, int startAddress /* = 0 */) {

        //startAddress = (int) memMng.Malloc((int) prog.length + 0x10);

        int envSize = 0x2000;
        envAddress = startAddress - envSize;
        makeEnv(envAddress, envSize);

        int stackPtr = startAddress;
        int stackSize = 0x0100;
        cmdLineAddress = stackPtr + stackSize;
        int cmdLineSize = 0x100;
        execPtr = stackPtr + stackSize + cmdLineSize;

        makePSP(startAddress, prog.length, this.memMng.address, 0x1000_0000);

        loadImage(prog, execPtr);

        if (isR) {
            // .r

            writeOption(cmdLineAddress, option);

            // init reg
            reg.pc = execPtr;
            reg.getA().set(0, startAddress);
            reg.getA().set(1, execPtr + prog.length); // end address
            reg.getA().set(2, cmdLineAddress);
            reg.getA().set(3, envAddress);
            reg.getA().set(4, execPtr);
            reg.setSR((short) 0x0000);
            reg.setUSP(defUSP);
            reg.setSSP(defSSP);
            currentProc.startAddress = startAddress;

        } else {
            // .x
            short id = mem.peekW(execPtr + 0);
            int baseAdr = mem.peekL(execPtr + 4);
            int startAdr = mem.peekL(execPtr + 0x08);
            int textSize = mem.peekL(execPtr + 0x0c);
            int dataSize = mem.peekL(execPtr + 0x10);
            int heapSize = mem.peekL(execPtr + 0x14);
            int relocTblSize = mem.peekL(execPtr + 0x18);
            int symbolTblSize = mem.peekL(execPtr + 0x1c);
            for (int i = 0; i < prog.length - 0x40; i++) {
                mem.pokeB(execPtr + i, mem.peekB(execPtr + i + 0x40));
            }
            relocate(textSize + dataSize, relocTblSize, execPtr);

            writeOption(cmdLineAddress, option);

            // init reg
            reg.pc = execPtr + startAdr;
            reg.getA().set(0, startAddress);
            reg.getA().set(1, execPtr + prog.length); // end address
            reg.getA().set(2, cmdLineAddress);
            reg.getA().set(3, envAddress);
            reg.getA().set(4, execPtr);
            reg.setSR((short) 0x0000);
            reg.setUSP(defUSP);
            reg.setSSP(defSSP);
            currentProc.startAddress = startAddress;
        }

        memMng.set(startAddress + 0x10, prog.length);

        // memory set up
        // XC20 Programmer's Manual Memory Map P.697 Reference

        // Interrupt Vector $00 ($00_0000) - $ff ($00_03ff)
        //    $2c  TRAP12 Processing with the COPY key
        mem.pokeL(0x02c * 4, 0x00fe_002c); // Dummy value
        //    $58  SCCA RS232C transmission
        mem.pokeL(0x058 * 4, 0x00fe_0000); // Dummy value

        // IOCS Vector      $100($00_0400) - $1ff($00_07ff)
        //    $1ff Abort processing(?)
        mem.pokeL(0x1ff * 4, 0x0012_1212); // Dummy value

        // debug
        //int zmusicPtr = 0xfe1000; // zmusic's permanent location (tentative)
        //mem.PokeL(0x8c, zmusicPtr); // Does it say where the traps are located?
        //mem.PokeL(zmusicPtr - 0x8, 0x5a6d7553); // 'ZmuS'
        //mem.PokeW(zmusicPtr - 0x4, 0x6943); // 'iC'
        //mem.PokeB(zmusicPtr - 0x2, 0x31); // Version 0x30 or higher is OK

        // Device name(?)
        mem.pokeL(0x67f2, 0xffff_ffff); // EOF
        mem.pokeW(0x67f6, (short) 0x8024); // ?
        mem.pokeL(0x6800, 0x4e55_4c20); // 'NUL '
        mem.pokeL(0x6804, 0x2020_2020); // '    '
    }

    /**
     * Relocate .x format
     * from run68
     */
    private boolean relocate(int reloc_adr, int reloc_size, int read_top) {
        int prog_adr;
        int data;
        short disp;

        prog_adr = read_top;
        for (; reloc_size > 0; reloc_size -= 2, reloc_adr += 2) {
            disp = mem.peekW(read_top + reloc_adr);
            if (disp == 1)
                return false;
            prog_adr += disp & 0xffff;
            data = mem.peekL(prog_adr) + read_top;
            //logger.log(Level.TRACE, "progAdr:[%08x] relocAdr:[%08x]".formatted(prog_adr, data));
            mem.pokeL(prog_adr, data);
        }

        return true;
    }

    private void writeOption(int stackPtr, String option) {
        if (option == null || option.isEmpty()) {
            mem.pokeB(stackPtr, (byte) 0x00);
            return;
        } else {
            mem.pokeB(stackPtr++, (byte) ' ');
        }
        byte[] opAry = option.getBytes(charset);
        for (byte op : opAry) {
            mem.pokeB(stackPtr++, op);
        }
        mem.pokeB(stackPtr, (byte) 0x00);
    }

    /**
     * Create an environment variable at the specified address
     */
    private void makeEnv(int envAddress, int envSize) {
        mem.pokeL(envAddress + 0x00, envSize); // Size Storage
        mem.pokeB(envAddress + 0x04, (byte) 0); // termination
    }

    private void makePSP(int startAddress, int length, int processID, int progSize) {
        mem.pokeL(startAddress + 0x00, beforePSP);
        //mem.PokeL(startAddress + 0x04, startAddress + length);
        //mem.PokeB(startAddress + 0x04, 0x00); // 0x00:normal memBlock 0xff:regidentProc memBlock
        mem.pokeL(startAddress + 0x04, processID);
        mem.pokeL(startAddress + 0x08, length);
        mem.pokeL(startAddress + 0x08, 0xb0_0000); // startAddress + PSPSize + progSize); // lax (for lzz.r)
        mem.pokeL(startAddress + 0x0c, 0); // nextProcPSP);

        if (beforePSP != 0) {
            mem.pokeL(beforePSP + 0x0c, startAddress);
        }
        beforePSP = startAddress;

        mem.pokeL(startAddress + 0x10, envAddress); // env adr
        mem.pokeL(startAddress + 0x14, startAddress + length + 0x100 - 1); // end address
        mem.pokeL(startAddress + 0x18, ctrlCAbortAddress); // ctrl+C abort address
        mem.pokeL(startAddress + 0x1c, errorAbortAddress); // error  abort address
        mem.pokeL(startAddress + 0x20, cmdLineAddress);
        mem.pokeL(startAddress + 0x24, 0); // file handle manage
        mem.pokeL(startAddress + 0x28, 0); // file handle manage
        mem.pokeL(startAddress + 0x2c, 0); // file handle manage
        mem.pokeL(startAddress + 0x30, startAddress + pspSize + progSize); // BSS address
        mem.pokeL(startAddress + 0x34, startAddress + pspSize + progSize); // Heap start address
        mem.pokeL(startAddress + 0x38, execPtr); // Stack address(heap end adr +1)
        mem.pokeL(startAddress + 0x3c, 0); // P Proc.USP
        mem.pokeL(startAddress + 0x40, 0); // P Proc.SSP
        mem.pokeW(startAddress + 0x44, (short) 0); // P Proc.SR
        mem.pokeW(startAddress + 0x46, (short) 0); // Abort  SR
        mem.pokeL(startAddress + 0x48, 0); // Abort  SSP
        mem.pokeL(startAddress + 0x4c, 0); // TRAP#10 vector address
        mem.pokeL(startAddress + 0x50, 0); // TRAP#11 vector address
        mem.pokeL(startAddress + 0x54, 0); // TRAP#12 vector address
        mem.pokeL(startAddress + 0x58, 0); // TRAP#13 vector address
        mem.pokeL(startAddress + 0x5c, 0); // TRAP#14 vector address
        mem.pokeL(startAddress + 0x60, 0xffff_ffff); // process flag(0:have P -1:OS)
        // unuse 0x64
        mem.pokeL(startAddress + 0x68, 0); // c Proc. PSPAddress(0: none)
        // unuse 0x6c - 0x7f
        mem.pokeB(startAddress + 0x80, (byte) 'C'); // Drive where Proc exists
        mem.pokeB(startAddress + 0x81, (byte) ':'); // Drive where Proc exists
        // Proc Path  0x82 - (max: 0xff)

    }

    public void loadImage(byte[] bin, int startAdr) {
        logger.log(Level.TRACE, "<NiseHuman>LoadImage");
        for (int i = 0; i < bin.length; i++) {
            mem.pokeB(startAdr + i, bin[i]);
        }
    }

    public void feFunc(short n) {
        try {
            tblFEFunc[n & 0xff].run();
        } catch (Exception e) {
            logger.log(Level.TRACE, "<NiseHuman>FEFunc call $%04x", n & 0xffff);
            throw e;
        }
    }

    public void dosCall(short n) {
        try {
            tblFunc[n & 0xff].run();
        } catch (Exception e) {
            logger.log(Level.TRACE, "<NiseHuman>dos call $%04x", n & 0xffff);
            throw e;
        }
    }

    private void exit() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF00 exit");

        programTerminate = true;
        returnCode = 0;
    }

    private final List<Byte> consoleTextBuf = new ArrayList<>();

    private void putChar() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF02 putchar");
        short code = mem.peekW(reg.getA().get(7) + 0);

        consoleTextBuf.add((byte) code);
        if (code == 0x0d && !consoleTextBuf.isEmpty()) {
            logger.log(Level.INFO, new String(ByteUtil.toByteArray(consoleTextBuf), charset));
            consoleTextBuf.clear();
        }
    }

    private void inKey() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF07 inkey");
        reg.getD()[0] = 'Y';
    }

    private void print() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF09 print");
        int mesPtr = mem.peekL(reg.getA().get(7) + 0);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(mesPtr + cnt);
            if ((char) b == '\0') break;
            if ((b & 0xff) < 0xf0) msg.add(b); // skip half tall character prefix
            cnt++;
        } while (true);
        String text = new String(ByteUtil.toByteArray(msg), charset);
        if (!consoleTextBuf.isEmpty()) {
            logger.log(Level.INFO, new String(ByteUtil.toByteArray(consoleTextBuf), charset));
            consoleTextBuf.clear();
        }
        text = text.replace("{", "{{");
        text = text.replace("}", "}}");
        System.out.print(text); // Normal console output
    }

    private void drvctrl() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF0F drvctrl");
        short mode = mem.peekW(reg.getA().get(7) + 0);
        byte md = (byte) ((mode >> 8) & 0x7);
        byte drive = (byte) mode;

        if (md != 0) {
            reg.getD()[0] = (int) 0xffff_ffff_ffff_fff1L;
            return;
            // throw new UnsupportedOperationException();
        }

        // Unconditionally ready
        // b1: Media insertion
        reg.getD()[0] = 0b0000_0010;
    }

    private void fputs() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF1E fputs");
        int mesPtr = mem.peekL(reg.getA().get(7) + 0);
        short fileNo = mem.peekW(reg.getA().get(7) + 4);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(mesPtr + cnt);
            if ((char) b == '\0') break;
            if ((b & 0xff) < 0xf0) msg.add(b); // skip half tall character prefix
            cnt++;
        } while (true);
//logger.log(Level.INFO, "\n" + StringUtil.getDump(ByteUtil.toByteArray(msg)));
        if (!consoleTextBuf.isEmpty()) {
            logger.log(Level.INFO, new String(ByteUtil.toByteArray(consoleTextBuf), charset));
            consoleTextBuf.clear();
        }
        String text = new String(ByteUtil.toByteArray(msg), charset);
        System.out.print(text); // Normal console output
    }

    private void super_() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF20 super");
        reg.getD()[0] = reg.getSSP();
        reg.setSSP(reg.getUSP());
        reg.setSR((short) (reg.getSR() | 0x2000)); // super
    }

    private void conCtrl() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF23 conctrl");
        short md = mem.peekW(reg.getA().get(7) + 0);
        switch (md) {
            case 0:
                byte code = (byte) mem.peekW(reg.getA().get(7) + 2);
                if (code < 0x20) {
                    if (code != 0x07)
                        logger.log(Level.INFO, "ascii code %02x", code);
                    else
                        logger.log(Level.INFO, String.valueOf((char) code));
                } else {
                    logger.log(Level.INFO, String.valueOf((char) code));
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void intVcs() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF25 intvcs");
        short intNo = mem.peekW(reg.getA().get(7) + 0);
        int jobAdr = mem.peekL(reg.getA().get(7) + 2);

        if (intNo < 0x100) {
            reg.getD()[0] = mem.peekL((int) (intNo * 4));
            mem.pokeL((intNo & 0xffff) * 4, jobAdr);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void verNum() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF30 vernum");

        reg.getD()[0] = 0x3638_0300; // 0x3638 '68' 0x0300 version3.00
    }

    private void keepPr() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF31 keeppr");
        int prgLen = mem.peekL(reg.getA().get(7) + 0);
        short code = mem.peekW(reg.getA().get(7) + 4);

        programTerminate = true;
        returnCode = code & 0xffff;
    }

    private void nameck() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF37 nameck");
        int file = mem.peekL(reg.getA().get(7) + 0);
        int buffer = mem.peekL(reg.getA().get(7) + 4);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(file + cnt);
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg), charset);
        logger.log(Level.TRACE, "Filename:[%s]", fn);
        fn = fileMng.vGetFullFilename(fn);
        try {
            String path = Path.getDirectoryName(fn);
            String filename = Path.getFileNameWithoutExtension(fn);
            String extension = Path.getExtension(fn);

            // Drive Letter (dummy)
            mem.pokeB(buffer + 0, (byte) path.charAt(0));
            mem.pokeB(buffer + 1, (byte) path.charAt(1));

            // Pathname MAX:64byte + endMark:$00
            cnt = -2;
            for (char c : path.toCharArray()) {
                if (cnt < 0) {
                    cnt++;
                    continue;
                }
                mem.pokeB(buffer + 2 + cnt, (byte) c);
                cnt++;
                if (cnt == 64) break;
            }
            mem.pokeB(buffer + 2 + cnt, (byte) 0x00);

            // File Name MAX:18byte + endMark:$00
            cnt = 0;
            for (char c : filename.toCharArray()) {
                mem.pokeB(buffer + 67 + cnt, (byte) c);
                cnt++;
                if (cnt == 18) break;
            }
            mem.pokeB(buffer + 67 + cnt, (byte) 0x00);

            // Extension MAX:4byte + endMark:$00
            cnt = 0;
            for (char c : extension.toCharArray()) {
                mem.pokeB(buffer + 86 + cnt, (byte) c);
                cnt++;
                if (cnt == 4) break;
            }
            mem.pokeB(buffer + 86 + cnt, (byte) 0x00);

            reg.getD()[0] = 0; // No wildcard specified
        } catch (Exception e) {
            reg.getD()[0] = -1; // error
        }
    }

    private void create() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF3C create");
        int namePtr = mem.peekL(reg.getA().get(7) + 0);
        short atr = mem.peekW(reg.getA().get(7) + 4);
        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(namePtr + cnt);
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg), charset);
        logger.log(Level.DEBUG, "Filename:[%s] ATR:%d", fn, atr & 0xffff);

        //String physicalFn = getPhysicalFn(fn);

        reg.getD()[0] = -1;

        if (atr != 0x20) {
            // Read-only support failure
            return;
        }

        // Find free file information
        fileHandle = -1;
        for (int i = 0; i < fi.length; i++) {
            if (fi[i] == null) fi[i] = new FileIni();
            if (fi[i].isopen) continue;
            //File.create(physicalFn).close();

            fileHandle = i;
            fi[i].isTemp = false;
            fi[i].isopen = true;
            fi[i].filename = fn;
            fi[i].ptr = 0;
            //fi[i].dat = File.ReadAllBytes(fn);
            byte[] dat;
            //if (fb.ContainsKey(physicalFn)) {
            //    dat = fb[physicalFn];
            //} else {
            //    if (File.Exists(physicalFn)) {
            //        dat = File.ReadAllBytes(physicalFn);
            //    } else {
            //        dat= new byte[0];
            //    }
            //    fb.add(physicalFn, dat);
            //}
            dat = fileMng.vReadAllBytes(fn);
            if (dat == null)
                fi[i].memoryStream = new MemoryStream(new byte[0]);
            else fi[i].memoryStream = new MemoryStream(dat);
            break;
        }
        if (fileHandle < 0) return;

        reg.getD()[0] = fileHandle;
    }

    private void open() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF3D open");
        int namePtr = mem.peekL(reg.getA().get(7) + 0);
        short mode = mem.peekW(reg.getA().get(7) + 4);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(namePtr + cnt);
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg), charset);
        logger.log(Level.DEBUG, "Filename:[%s] Mode:%d", fn, mode & 0xffff);

        //String physicalFn = getPhysicalFn(fn);

        reg.getD()[0] = -1;

        if (mode != 0 && mode != 1) {
            // Read-only support failure
            return;
        }

        // Find free file information
        fileHandle = -1;
        for (int i = 0; i < fi.length; i++) {
            if (fi[i] == null) fi[i] = new FileIni();
            if (fi[i].isopen) continue;
            //if (!File.exists(physicalFn)) continue;
            if (!fileMng.existsFile(fn)) {
logger.log(Level.INFO, "file not found: %s".formatted(fn));
                continue;
            }

            fileHandle = i;
            fi[i].isTemp = false;
            fi[i].isopen = true;
            fi[i].filename = fn;
            fi[i].ptr = 0;
            byte[] dat;
            //if (fb.ContainsKey(physicalFn)) {
            //    dat = fb[physicalFn];
            //} else {
            //    dat = File.ReadAllBytes(physicalFn);
            //    fb.add(physicalFn, dat);
            //}
            dat = fileMng.vReadAllBytes(fn);
            fi[i].memoryStream = new MemoryStream(dat);
            break;
        }
        if (fileHandle < 0) return;

        reg.getD()[0] = fileHandle;
    }

    private String getPhysicalFn(String fn) {
        String physicalFn;
        if (fn.toUpperCase().indexOf("C:\\") == 0) {
            physicalFn = Path.combine(currentWorkPath, fn.substring(3));
        } else {
            physicalFn = Path.combine(currentWorkPath, fn);
        }

        if (!File.exists(physicalFn)) {
            if (Path.getExtension(physicalFn).equalsIgnoreCase(".ZPD") && envZPDs != null && !envZPDs.isEmpty()) {
                String f = Path.getFileName(physicalFn);
                for (String s : envZPDs) {
                    if (!File.exists(Path.combine(s, f))) {
logger.log(Level.INFO, "file not found: %s".formatted(fn));
                        continue;
                    }
                    physicalFn = Path.combine(s, f);
                }
            }
        }

        logger.log(Level.TRACE, "PhysicalFilename:[%s] ", physicalFn);
        return physicalFn;
    }

    private void close() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF3E close");
        short fileNo = mem.peekW(reg.getA().get(7) + 0);

        reg.getD()[0] = -1;
        try {
            if (fileNo >= fi.length) return;
            if (fi[fileNo] == null) return;
            if (!fi[fileNo].isopen) return;

            fi[fileNo].isopen = false;

            reg.getD()[0] = 0;
        } catch (Exception e) {
        }
    }

    private void read() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF3F read");
        int fileNo = mem.peekW(reg.getA().get(7) + 0) & 0xffff;
        int dataPtr = mem.peekL(reg.getA().get(7) + 2);
        int size = mem.peekL(reg.getA().get(7) + 6);

        reg.getD()[0] = -1;
        if (fi[fileNo] == null) return;
        if (!fi[fileNo].isopen) return;

        int i = 0;
        //if (fi[fileno].dat != null && fi[fileno].dat.length > 0) {
        //    for (; i < size; i++) {
        //        if (fi[fileno].ptr == fi[fileno].dat.length) break;
        //        mem.PokeB(dataPtr + i, fi[fileno].dat[fi[fileno].ptr]);
        //        fi[fileno].ptr++;
        //    }
        //}
        if (fi[fileNo].memoryStream != null && fi[fileNo].memoryStream.getLength() > 0) {
            for (; i < size; i++) {
                if (fi[fileNo].ptr == fi[fileNo].memoryStream.getLength()) break;
                int b = fi[fileNo].memoryStream.readByte();
                if (b < 0) break;
                mem.pokeB(dataPtr + i, (byte) b);
                fi[fileNo].ptr++;
            }
        }

        reg.getD()[0] = i;
    }

    private void write() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF40 write");
        int fileNo = mem.peekW(reg.getA().get(7) + 0) & 0xffff;
        int dataPtr = mem.peekL(reg.getA().get(7) + 2);
        int size = mem.peekL(reg.getA().get(7) + 6);

        reg.getD()[0] = -1;
        if (fi[fileNo] == null) return;
        if (!fi[fileNo].isopen) return;

        int i = 0;
        List<Byte> data = new ArrayList<>();

        for (; i < size; i++) data.add(mem.peekB(dataPtr + i));
        //String physicalFn = getPhysicalFn(fi[fileno].filename);

        //if (fb.containsKey(physicalFn)) {
        //    byte[] f = fb[physicalFn]; // File.readAllBytes(physicalFn);
        //    int nSize = f.length + data.count - fi[fileno].ptr;
        //    byte[] nf = new byte[nSize];
        //    System.arraycopy(ByteUtil.toByteArray(data), 0, nf, fi[fileno].ptr, data.count);
        //    fb[physicalFn] = nf; // File.writeAllBytes(physicalFn, nf);

        //    reg.getD()[0] = i;
        //}

        String fn = fi[fileNo].filename;
        if (fileMng.existsFile(fn)) {
            byte[] f = fileMng.vReadAllBytes(fn);
            int nSize = (f != null ? f.length : 0) + data.size() - fi[fileNo].ptr;
            byte[] nf = new byte[nSize];
            System.arraycopy(ByteUtil.toByteArray(data), 0, nf, fi[fileNo].ptr, data.size());
            fileMng.setVFile(fn, nf); // File.writeAllBytes(physicalFn, nf);

            reg.getD()[0] = (int) i;
        }
    }

    private void delete() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF41 delete");
        int namePtr = mem.peekL(reg.getA().get(7) + 0);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(namePtr + cnt);
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg));
        logger.log(Level.TRACE, "Filename:[%s]", fn);
        String physicalFn = getPhysicalFn(fn);

        reg.getD()[0] = 0x0000_0000; // Unconditional success
    }

    private void seek() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF42 seek");
        int fileNo = mem.peekW(reg.getA().get(7) + 0) & 0xffff;
        int offset = (int) mem.peekL(reg.getA().get(7) + 2);
        short mode = mem.peekW(reg.getA().get(7) + 6);

        reg.getD()[0] = -1;
        if (fi[fileNo] == null) return;
        if (!fi[fileNo].isopen) return;

        switch (mode) {
            case 0: // begin
                fi[fileNo].memoryStream.seek(offset, SeekOrigin.Begin);
                fi[fileNo].ptr = 0 + offset;
                break;
            case 1: // seek
                fi[fileNo].memoryStream.seek(offset, SeekOrigin.Current);
                fi[fileNo].ptr += offset;
                break;
            case 2: // end
                fi[fileNo].memoryStream.seek(offset, SeekOrigin.End);
                //fi[fileNo].ptr = fi[fileNo].dat.length + offset;
                fi[fileNo].ptr = (int) (fi[fileNo].memoryStream.getLength() + offset);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        reg.getD()[0] = fi[fileNo].ptr;
    }

    private void malloc() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF48 malloc");
        int byteSize = mem.peekL(reg.getA().get(7) + 0);

        int ptr = memMng.malloc(byteSize + 16);

        if (ptr < 0) {
            reg.getD()[0] = 0x8100_0000 + byteSize + 16; // Unable to secure
            reg.getD()[0] = 0x8200_0000; // Not at all secure
            return;
        }

        reg.getD()[0] = ptr + 16;
    }

    private void mFree() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF49 mfree");
        int memPtr = mem.peekL(reg.getA().get(7) + 0);

        int ret = memMng.mfree(memPtr - 16);

        reg.getD()[0] = ret;
    }

    private void setBlock() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF4A setblock");
        int newLen = mem.peekL(reg.getA().get(7) + 4);
        int newPtr = mem.peekL(reg.getA().get(7) + 0);
        boolean ret = memMng.Change(newPtr, newLen);

        if (!ret) {
            reg.getD()[0] = 0x8100_0000 + newLen; // Unable to secure
            reg.getD()[0] = 0x8200_0000; // Not at all secure
            return;
        }

        reg.getD()[0] = 0x0000_0000;// + newlen;
    }

    private void exec() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF4B exec");
        short md = mem.peekW(reg.getA().get(7) + 0);
        int fil = mem.peekL(reg.getA().get(7) + 2);
        int p1 = mem.peekL(reg.getA().get(7) + 6);
        int p2 = mem.peekL(reg.getA().get(7) + 10);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB((int) (fil + cnt));
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg), charset);
        msg = new ArrayList<>();
        cnt = 0;
        do {
            byte b = mem.peekB((int) (p1 + cnt));
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String op = new String(ByteUtil.toByteArray(msg), charset);

        switch (md) {
            case 0:
                logger.log(Level.TRACE, "<NiseHuman>in:  md:0 fil:%s op:%s p2:%08x ", fn, op, p2);
                if (!fn.equalsIgnoreCase("ZMC")) {
                    throw new UnsupportedOperationException(); // We do not accept anything other than ZMC!
                }

                // TBD
                reg.getD()[0] = 0x0000_0000; // Process Exit Codes
                reg.getD()[1] = 0x0000_0000; // Error Codes

                break;
            case 2:
                logger.log(Level.TRACE, "<NiseHuman>in:  md:2 fil:%s p1:%08x p2:%08x ", fn, p1, p2);
                cnt = 0;
                int cnt2 = 0;
                boolean o = false;
                while (true) {
                    byte b = mem.peekB(fil + cnt);
                    if (o) {
                        mem.pokeB(p1 + cnt2, b);
                        cnt2++;
                    }
                    if ((char) b == '\0') break;
                    if ((char) b == ' ') {
                        if (!o) mem.pokeB(fil + cnt, (byte) 0);
                        o = true;
                    }
                    cnt++;
                }

                msg = new ArrayList<>();
                cnt = 0;
                do {
                    byte b = mem.peekB(fil + cnt);
                    if ((char) b == '\0') break;
                    msg.add(b);
                    cnt++;
                } while (true);
                fn = new String(ByteUtil.toByteArray(msg), charset);
                msg = new ArrayList<>();
                cnt = 0;
                do {
                    byte b = mem.peekB(p1 + cnt);
                    if ((char) b == '\0') break;
                    msg.add(b);
                    cnt++;
                } while (true);
                op = new String(ByteUtil.toByteArray(msg), charset);
                logger.log(Level.TRACE, "<NiseHuman>out: md:2 fil:%s op:%s p2:%08x ", fn, op, p2);

                reg.getD()[0] = 0x0000_0000;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void exit2() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF4C exit2");
        short rc = mem.peekW(reg.getA().get(7) + 0);

        programTerminate = true;
        returnCode = rc & 0xffff;
    }

    private void files() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF4E files");
        int filBuf = mem.peekL(reg.getA().get(7) + 0);
        int namePtr = mem.peekL(reg.getA().get(7) + 4);
        short atr = mem.peekW(reg.getA().get(7) + 8);

        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB(namePtr + cnt);
            if ((char) b == '\0') break;
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg), charset);
        logger.log(Level.TRACE, "Filename:[%s] Atr:%04x".formatted(fn, atr & 0xffff));
        //String physicalFn = getPhysicalFn(fn);

        reg.getD()[0] = -1;
        //if (File.exists(physicalFn))
        if (fileMng.existsFile(fn)) {
            mem.pokeB(filBuf + 0, (byte) 0); // ATR(sys)
            mem.pokeB(filBuf + 1, (byte) 0); // DriveNo(sys)
            mem.pokeW(filBuf + 2, (short) 0); // DirCls(sys)
            mem.pokeW(filBuf + 4, (byte) 0); // DirFAT(sys)
            mem.pokeW(filBuf + 6, (byte) 0); // DirSec(sys)
            mem.pokeW(filBuf + 8, (byte) 0); // DirPos(sys)
            for (int i = 0; i < 8; i++)
                mem.pokeB(filBuf + 10 + i, (byte) 0x20); // FileName
            for (int i = 0; i < 3; i++)
                mem.pokeB(filBuf + 18 + i, (byte) 0x20); // EXT
            mem.pokeB(filBuf + 21, (byte) atr); // ATR
            mem.pokeW(filBuf + 22, (short) 0x0000); // TIME
            mem.pokeW(filBuf + 24, (short) 0x0000); // DATE
            //FileInfo fi = new FileInfo(fn);
            mem.pokeL(filBuf + 26, fn.length()); // FileLength
            for (int i = 0; i < 23; i++)
                mem.pokeB(filBuf + 30 + i, (byte) 0x00); // PACKEDNAME

            reg.getD()[0] = 0x0000_0000; // If negative, it is an error.
        }
    }

    private void getPsp() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF51 getpsp");
        // reg.getD()[0] = (int)(currentProc.startAddress - 0xf0);
        reg.getD()[0] = beforePSP + 0x10; // Address of the environment variable
    }

    private void fileDate() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF87 filedate");
        short fileNo = mem.peekW(reg.getA().get(7) + 0);
        int datetime = mem.peekL(reg.getA().get(7) + 2);

        if (datetime == 0) {
            // Reading the date and time
            reg.setDl(0, fi[fileNo].datetime);
            return;
        } else {
            // Date and time settings
            fi[fileNo].datetime = datetime;
        }

        reg.getD()[0] = 0;
    }

    private void makeTmp() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FF8A _MAKETMP");
        int namePtr = mem.peekL(reg.getA().get(7) + 0);
        short atr = mem.peekW(reg.getA().get(7) + 4);
        List<Byte> msg = new ArrayList<>();
        int cnt = 0;
        do {
            byte b = mem.peekB((int) (namePtr + cnt));
            if ((char) b == '\0') break;
            if ((char) b == '?') {
                b = (byte) '0';
                mem.pokeB(namePtr + cnt, b);
            }
            msg.add(b);
            cnt++;
        } while (true);
        String fn = new String(ByteUtil.toByteArray(msg), charset);
        logger.log(Level.TRACE, "SRC Filename:[%s] Atr:%04x".formatted(fn, atr & 0xffff));

        // Replace ???? with numbers and check that the file name does not exist in the specified path.
        // TBD

        // Find free file information
        fileHandle = -1;
        for (int i = 0; i < fi.length; i++) {
            if (fi[i] != null) continue;

            fi[i] = new FileIni();

            fileHandle = i;
            fi[i].isTemp = true;
            fi[i].isopen = true;
            fi[i].filename = fn;
            fi[i].ptr = 0;
            fi[i].memoryStream = new MemoryStream();
            break;
        }
        if (fileHandle < 0) {
            reg.getD()[0] = 0xffff_ffff;
            return;
        }

        reg.getD()[0] = fileHandle;
    }

    private void s_malloc() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FFAD s_malloc");
        short md = mem.peekW(reg.getA().get(7) + 0);
        int len = mem.peekL(reg.getA().get(7) + 2);

        int ptr = memMng.malloc(len + 16);

        if (ptr < 0) {
            reg.getD()[0] = 0x8100_0000 + len + 16; // Unable to secure
            reg.getD()[0] = 0x8200_0000; //Not at all secure
            return;
        }

        reg.getD()[0] = ptr + 16;
    }

    private void s_mfree() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FFAE s_mfree");
        int memPtr = mem.peekL(reg.getA().get(7) + 0);

        int ret = memMng.mfree(memPtr - 16);

        reg.getD()[0] = ret;
    }

    private void bus_err() {
        logger.log(Level.TRACE, "<NiseHuman>dos call $FFF7 bus_err");

        int sadr = mem.peekL(reg.getA().get(7) + 0);
        int dadr = mem.peekL(reg.getA().get(7) + 4);
        short md = mem.peekW(reg.getA().get(7) + 8);

        switch (md) {
            case 1: // byte
                // MIDI IF isr(src): $eafa05
                // MIDI IF icr(dst): $eafa07
                // MIDI IF isr(src): $eafa15
                // MIDI IF icr(dst): $eafa17
                break;
            case 2: // word
                break;
            case 4: // long
                break;
            default:
                throw new UnsupportedOperationException();
        }

        reg.getD()[0] = 0x0000_0000; // 0: Read/write possible 1,2,-1: Error
    }

    private void ltos() {
        logger.log(Level.TRACE, "<NiseHuman>FE func call $FE11 _LTOS");

        int val = reg.getDl(0);
        int dadr = reg.getA().get(0);
        byte[] dat = "%d".formatted(val).getBytes(charset);
        for (byte d : dat) {
            mem.pokeB(dadr++, d);
        }
        mem.pokeB(dadr, (byte) 0);
        reg.getA().set(0, dadr);
    }
}
