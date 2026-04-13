/*
 * https://github.com/kuma4649/MDPlayer
 */

package mdplayer.driver.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import dotnet4j.util.compat.StringUtilities;
import dotnet4j.util.compat.TriConsumer;
import mdplayer.Common;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.emu.nise98.Memory98;
import mdplayer.emu.nise98.Nise98;
import mdplayer.emu.nise98.Nise98.OngenBoardType;
import mdplayer.emu.nise98.NiseDos;
import mdplayer.emu.nise98.Register286;

import static java.lang.System.getLogger;


/**
 * @author kumatan
 */
public class FMP {

    private static final Logger logger = getLogger(FMP.class.getName());

    TriConsumer<Integer, Integer, byte[][]> setPPZ8PCMData;
    TriConsumer<Integer, Integer, Integer> setPPZ8Data;
    TriConsumer<Integer, Integer, Integer> opnaWrite;
    Consumer<Boolean> blockWrite;

    Charset charset;
    String dir;

    public static final int baseClock = 7987200;
    private int step = 0;
    final Nise98 nise98 = new Nise98();
    private Register286 regs;
    private List<String> searchPaths = null;
    FileTemp ft = null;
    int pcmDataSendCount;
    String playingFileName;
    String playingArcFileName;

    public void setSearchPath(String searchPath) {
        try {
            searchPath = searchPath != null ? searchPath : "";
            // Get the environment variable "PVI"
            String pvi = "";
            try {
                pvi = System.getProperty("mdplayer.fmp.pvi");
                if (!StringUtilities.isNullOrEmpty(pvi)) searchPath += (searchPath.isEmpty() ? "" : ";") + pvi;
            } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage());
            }
            searchPaths = Arrays.stream(searchPath.split(";"))
                    .filter(path -> !StringUtilities.isNullOrEmpty(path)).toList();
            for (String path : searchPaths)
                logger.log(Level.INFO, "Search Path: %s".formatted(path));
        } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage());
            this.searchPaths = List.of(searchPath);
        }
    }

    int processOneFrame(Runnable stopper) {
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
        int fmpSloop_c = nise98.getMem().peekB(ptr + 0x17) & 0xff;
        int pcmUse = nise98.getMem().peekW(ptr + 0x20) & 0xffff;
        if ((pcmUse & 0xff00) != 0) {
        }
        return fmpSloop_c;
    }

    void run(byte[] vgmBuf) {
        //var fileNameFMP = "FMP.COM";
        //var fileNamePPZ8 = "PPZ8.COM";
        Path crntDir = Path.of(dir);
        Path fileNameFMP = crntDir.resolve("FMP.COM");
        logger.log(Level.DEBUG, fileNameFMP);
        nise98.init(null, opnaWrite, ft, OngenBoardType.SpeakBoard, Common.VGMProcSampleRate); // .PC9801_86B); // .SpeakBoard); // .PC9801_26K);
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
        int[] tmp1 = new int[] {step};
        Register286[] tmp2 = new Register286[1];
        nise98.getPPZ8().fmpRegisterPPZ8(/* out */ tmp1, /* out */ tmp2);
        step = tmp1[0];
        regs = tmp2[0];
        nise98.getPPZ8().setCallBack(setPPZ8PCMData, setPPZ8Data);

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
        nise98.callRunFunctionCall((byte) 0xd2, true, true, true, 10_000_000_000L, 0_000);

        if (pcmDataSendCount != 0) {
            blockWrite.accept(true);
            // Add additional weight based on size and elapsed time.
            try {
                Thread.sleep(Math.max(pcmDataSendCount / 20, 0));
            } catch (InterruptedException ignore) {
            }
            blockWrite.accept(false);
            pcmDataSendCount = 0;
        }

        logger.log(Level.TRACE, "return CF=%s code=%02x".formatted(regs.isCF(), regs.getAL() & 0xff));
    }

    public void compile() {
        var fileNameFMP = "FMP.COM";
        var fileNameFMC = "FMC.EXE";
        int rc = 0;

        nise98.init(null, opnaWrite, ft, OngenBoardType.SpeakBoard, Common.VGMProcSampleRate); // .PC9801_86B); // .SpeakBoard); // .PC9801_26K);

        // FMP resident
        nise98.loadRun(fileNameFMP, "s -s", 0x2000);
        regs = nise98.getRegisters();

        // Running FMC
        nise98.getDos().setProgramTerminate(false);
        if ((rc = nise98.loadRun(fileNameFMC, playingFileName, 0x3000
                //, true, true, true, 3_000_000, 0
        )) != 0) {
            throw new IllegalArgumentException("return %d".formatted(rc));
        }
    }
}
