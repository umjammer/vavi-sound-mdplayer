/*
 * https://github.com/kuma4649/MDPlayer
 */

package mdplayer.lib.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import mdplayer.emu.common.EncodingUtils;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.emu.nise98.Memory98;
import mdplayer.emu.nise98.Nise98;
import mdplayer.emu.nise98.Nise98.OngenBoardType;
import mdplayer.emu.nise98.NiseDos;
import mdplayer.emu.nise98.Register286;
import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;
import static vavi.util.compat.Util.isNullOrEmpty;


/**
 * @author kumatan
 */
public class FMP {

    private static final Logger logger = getLogger(FMP.class.getName());

    public TriConsumer<Integer, Integer, byte[][]> setPPZ8PCMData;
    public TriConsumer<Integer, Integer, Integer> setPPZ8Data;
    public java.util.function.BiConsumer<Integer, String> setPPZ8PCMFilename;
    public TriConsumer<Integer, Integer, Integer> opnaWrite;
    public Consumer<Boolean> blockWrite;

    public Charset charset;
    public String dir;
    public int sampleRate;

    public static final int baseClock = 7987200;
    private int step = 0;
    public final Nise98 nise98 = new Nise98();
    private Register286 regs;
    private List<String> searchPaths = null;
    public FileTemp ft = null;
    public int pcmDataSendCount;
    public String playingFileName;
    public String playingArcFileName;

    public void setSearchPath(String searchPath) {
        try {
            searchPath = searchPath != null ? searchPath : "";
            // Get the environment variable "PVI"
            String pvi = "";
            try {
                pvi = System.getProperty("mdplayer.fmp.pvi");
                if (!isNullOrEmpty(pvi)) searchPath += (searchPath.isEmpty() ? "" : ";") + pvi;
            } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage());
            }
            searchPaths = Arrays.stream(searchPath.split(";"))
                    .filter(path -> !isNullOrEmpty(path)).toList();
            for (String path : searchPaths)
                logger.log(Level.INFO, "Search Path: %s".formatted(path));
        } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage());
            this.searchPaths = List.of(searchPath);
        }
    }

    /** address of FMP's internal work area in the emulated memory, 0 until the first frame */
    public int workPtr; // TODO test only

    /** FMP's work area, for the visualizer. valid from the first frame on */
    private final FmpWork work = new FmpWork();

    /** FMP's work area, seen from the outside */
    public FmpWork getWork() {
        return work;
    }

    public int processOneFrame(Runnable stopper) {
        regs.setSS((short) 0xe000);
        regs.setSP((short) 0x0000);
        nise98.callRunFunctionCall((byte) 0x14);

        // Performance Check
        regs.setAX((short) 0x0004);
        regs.setSS((short) 0xe000);
        regs.setSP((short) 0x0000);
        nise98.callRunFunctionCall((byte) 0xd2);
        if (regs.getAX() == 0)
            stopper.run();

        // Get the internal work address and check the number of times the song has looped
        regs.setAX((short) 0x1104);
        regs.setSS((short) 0xe000);
        regs.setSP((short) 0x0000);
        nise98.callRunFunctionCall((byte) 0xd2);
        int ptr = (0x2000 << 4) + (regs.getAX() & 0xffff);
        workPtr = ptr;
        work.setWork(nise98.getMem(), ptr);
        int fmpSloop_c = nise98.getMem().peekB(ptr + 0x17) & 0xff;
        int pcmUse = nise98.getMem().peekW(ptr + 0x20) & 0xffff;
        if ((pcmUse & 0xff00) != 0) {
        }
        return fmpSloop_c;
    }

    public void run(byte[] vgmBuf) {
        //var fileNameFMP = "FMP.COM";
        //var fileNamePPZ8 = "PPZ8.COM";
        Path crntDir = Path.of(dir);
        Path fileNameFMP = crntDir.resolve("FMP.COM");
        logger.log(Level.DEBUG, fileNameFMP);
        nise98.init(null, opnaWrite, ft, OngenBoardType.SpeakBoard, sampleRate); // .PC9801_86B); // .SpeakBoard); // .PC9801_26K);
        nise98.getDos().setArcFile(playingArcFileName);
        nise98.getDos().setSearchPath(searchPaths);
        nise98.getDos().charset = charset;
        nise98.getPPZ8().charset = charset;

        // FMP Residency
        //nise98.LoadRun(fileNameFMP, "s -s", 0x2000); // , true, true, true, 3_000_000, 108213); // 108213->wait Loop exit
        int r = nise98.loadRun(fileNameFMP.toString(), "s -s -#42", 0x2000); //, true, true, true, 3_000_000, 0);// 108213->wait Loop exit
        if (r != 0) {
            throw new IllegalStateException("exec " + fileNameFMP + " returns " + r);
        }
        regs = nise98.getRegisters();

        // nisePPZ8 resident
        step = 0;
        Memory98 mem = nise98.getMem();
        int[] tmp1 = {step};
        Register286[] tmp2 = new Register286[1];
        nise98.getPPZ8().fmpRegisterPPZ8(/* out */ tmp1, /* out */ tmp2);
        step = tmp1[0];
        regs = tmp2[0];
        nise98.getPPZ8().setCallBack(setPPZ8PCMData, setPPZ8Data, setPPZ8PCMFilename);

        // Song data loading and playback start notification
        //
        fmpLoadAndPlayFileAL2(nise98.getDos(), regs);
    }

    private void fmpLoadAndPlayFileAL2(NiseDos dos, Register286 regs) {
        byte[] m = Path.of(playingFileName).getFileName().toString().getBytes(charset);
        dos.setPath(Path.of(playingFileName).getParent());
        dos.loadImage(m, (0x5000 << 4) + 0x0000);

        step = 0;
        regs.setAL((byte) 0x02);
        regs.setDS((short) 0x5000);
        regs.setDX((short) 0x0000);
        regs.setSS((short) 0xe000);
        regs.setSP((short) 0x0000);
        nise98.callRunFunctionCall((byte) 0xd2, false, true, true, 10_000_000_000L, 0_000);

        if (pcmDataSendCount != 0) {
            blockWrite.accept(true);
            // Add additional weight based on size and elapsed time.
            try { Thread.sleep(Math.max(pcmDataSendCount / 20, 0)); } catch (InterruptedException _) {}
            blockWrite.accept(false);
            pcmDataSendCount = 0;
        }

        logger.log(Level.TRACE, "return CF=%s code=%02x".formatted(regs.isCF(), regs.getAL() & 0xff));
    }

    public void compile() {
        Path crntDir = Path.of(dir);
        Path fileNameFMP = crntDir.resolve("FMP.COM");
        Path fileNameFMC = crntDir.resolve("FMC.EXE");
        int rc = 0;

        nise98.init(null, opnaWrite, ft, OngenBoardType.SpeakBoard, sampleRate); // .PC9801_86B); // .SpeakBoard); // .PC9801_26K);
        nise98.getDos().setPath(Path.of(playingFileName).getParent());
        nise98.getDos().setSearchPath(searchPaths);
        nise98.getDos().charset = charset;

        // FMP resident
        rc = nise98.loadRun(fileNameFMP.toString(), "s -s", 0x2000);
        if (rc != 0)
            throw new IllegalArgumentException("fmp return %d".formatted(rc));
        regs = nise98.getRegisters();

        // Running FMC
        nise98.getDos().setProgramTerminate(false);
        rc = nise98.loadRun(fileNameFMC.toString(), Path.of(playingFileName).toString(), 0x3000); //, true, true, true, 3_000_000, 0
        if (rc != 0)
            throw new IllegalArgumentException("fmc return %d".formatted(rc));
    }

    public static String decodePc98ShiftJis(byte[] buf, int start, int end) {
        return EncodingUtils.decodePc98ShiftJis(buf, start,end);
    }

    public static String normalizeKanji(String s) {
        return EncodingUtils.normalizeKanji(s);
    }
}
