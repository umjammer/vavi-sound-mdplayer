package mdplayer.driver.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import dotnet4j.util.compat.StringUtilities;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.fmp.nise98.FileTemp;
import mdplayer.driver.fmp.nise98.Memory98;
import mdplayer.driver.fmp.nise98.Nise98;
import mdplayer.driver.fmp.nise98.Nise98.OngenBoardType;
import mdplayer.driver.fmp.nise98.NiseDos;
import mdplayer.driver.fmp.nise98.Register286;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.ChipDatum;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


public class FMP extends BaseDriver {

    private static final Logger logger = getLogger(FMP.class.getName());

    public static final int baseClock = 7987200;
    private int step = 0;
    private final Nise98 nise98 = new Nise98();
    private Register286 regs;
    private String searchPath = "";
    private List<String> searchPaths = null;
    private FileTemp ft = null;
    private int pcmDataSendCount;

    private String playingFileName;

    public String getPlayingFileName() {
        return playingFileName;
    }

    private String playingArcFileName;

    public String getPlayingArcFileName() {
        return playingArcFileName;
    }

    public FMP(FileTemp ft) {
        this.ft = ft;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Vgm.Gd3 ret = new Vgm.Gd3();

        try {
            if (buf != null && buf.length > 2) {
                int[] ptr = new int[] {(ByteUtil.readLeShort(buf, 0) & 0xffff) + 4}; // 4 'FMC'+version(1byte)
                String comment = Common.getNRDString(buf, /* ref */ ptr);
                ret.trackName = comment;
                ret.trackNameJ = ret.trackName;

            }
        } catch (Exception e) {
            ret.trackName = "";
            ret.trackNameJ = "";
        }

        return ret;
    }

    public void setSearchPath(String searchPath) {
        try {
            this.searchPath = searchPath;
            // Get the environment variable "PVI"
            String pvi = "";
            try {
                pvi = System.getenv("PVI");
                if (!StringUtilities.isNullOrEmpty(pvi)) this.searchPath += ";" + pvi;
            } catch (Exception e) {
                this.searchPath = searchPath;
            }
            searchPaths = Arrays.stream(this.searchPath.split(";"))
                    .filter(path -> !StringUtilities.isNullOrEmpty(path)).toList();
            for (String path : searchPaths)
                logger.log(Level.INFO, "Search Path:%s".formatted(path));
        } catch (Exception e) {
            this.searchPath = searchPath;
        }
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        Vgm.Gd3 gd3 = getGD3Info(vgmBuf, 0);
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;

        try {
            run(vgmBuf);
        } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                if (vgmFrameCounter > -1) {
                    counter++;

                    nise98.runTimer();
                    if (!nise98.intTimer()) continue;
                    regs.setSS((short) 0xe000);
                    regs.setSP((short) 0x0000);
                    nise98.callRunFunctionCall((byte) 0x14);

                    // Performance Check
                    regs.setAX((short) 0x0004);
                    regs.setSS((short) 0xe000);
                    regs.setSP((short) 0x0000);
                    nise98.callRunFunctionCall((byte) 0xd2);
                    if (regs.getAX() == 0)
                        stopped = true;

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
                    vgmCurLoop = fmpSloop_c;
                }
                vgmFrameCounter++;
            }

            //vgmCurLoop = mm.ReadUInt16(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void run(byte[] vgmBuf) {
        //var fileNameFMP = "FMP.COM";
        //var fileNamePPZ8 = "PPZ8.COM";
        Path crntDir = Path.of(System.getProperty("mdplayer.fmp.dir", System.getProperty("user.dir")));
        Path fileNameFMP = crntDir.resolve("FMP.COM");
        logger.log(Level.DEBUG, fileNameFMP);
        nise98.init(null, this::opnaWrite, ft, OngenBoardType.SpeakBoard); // .PC9801_86B); // .SpeakBoard); // .PC9801_26K);
        nise98.getDos().setArcFile(playingArcFileName);
        nise98.getDos().setSearchPath(searchPaths);

        // FMP Residency
        //nise98.LoadRun(fileNameFMP, "s -s", 0x2000); // , true, true, true, 3_000_000, 108213); // 108213->wait Loop exit
        nise98.loadRun(fileNameFMP.toString(), "s -s -#42", 0x2000); //, true, true, true, 3_000_000, 0);// 108213->wait Loop exit
        regs = nise98.getRegisters();

        // nisePPZ8 resident
        step = 0;
        Memory98 mem = nise98.getMem();
        int[] tmp1 = new int[] {step};
        Register286[] tmp2 = new Register286[1];
        nise98.getPPZ8().fmpRegisterPPZ8(/* out */ tmp1, /* out */ tmp2);
        step = tmp1[0];
        regs = tmp2[0];
        nise98.getPPZ8().setCallBack(this::setPPZ8PCMData, this::setPPZ8Data);

        // Song data loading and playback start notification
        //
        fmpLoadAndPlayFileAL2(nise98.getDos(), regs);
    }

    private void setPPZ8PCMData(int bank, int mode, byte[][] pcmData) {
        plugin.audio.chipRegister.chip(Ppz8Chip.class).writePcm(0, bank, mode, pcmData, model);
    }

    private void setPPZ8Data(int port, int adr, int data) {
        plugin.audio.chipRegister.chip(Ppz8Chip.class).write(0, port, adr, data, model);
    }

    private void opnaWrite(ChipDatum dat) {
        byte cn = (byte) (dat.port >> 8);
        int port = dat.port == 0x8a ? 0 : 1;
        plugin.audio.chipRegister.chip(Ym2608Chip.class).write(0, port, dat.address, dat.data, model);

        if (port == 1 && dat.address == 0x8 && model == EnmModel.RealModel) {
            this.isDataBlock = true;
            pcmDataSendCount++;
            plugin.audio.chipRegister.chip(Ym2608Chip.class).setSyncWait(0, 1);
        }
    }

    private void fmpLoadAndPlayFileAL2(NiseDos dos, Register286 regs) {
        byte[] m = Path.of(playingFileName).getFileName().toString().getBytes(charset);
        dos.setPath(Path.of(playingFileName).getParent());
        dos.loadImage(m, (0x5000 << 4) + 0x0000);

        step = 0;
        regs.setAL((short) 0x02);
        regs.setDS((short) 0x5000);
        regs.setDX((short) 0x0000);
        regs.setSS((short) 0xe000);
        regs.setSP((short) 0x0000);
        nise98.callRunFunctionCall((byte) 0xd2, true, true, true, 10_000_000_000L, 0_000);

        if (pcmDataSendCount != 0) {
            this.isDataBlock = true;
            // Add additional weight based on size and elapsed time.
            try {
                Thread.sleep(Math.max(pcmDataSendCount / 20, 0));
            } catch (InterruptedException ignore) {
            }
            this.isDataBlock = false;
            pcmDataSendCount = 0;
        }

        logger.log(Level.DEBUG, "return CF=%s code=%02x", regs.isCF(), regs.getAL() & 0xff);
    }

    public boolean compile(String playingFileName) {
        var fileNameFMP = "FMP.COM";
        var fileNameFMC = "FMC.EXE";
        int rc = 0;

        nise98.init(null, this::opnaWrite, ft, OngenBoardType.SpeakBoard); // .PC9801_86B); // .SpeakBoard); // .PC9801_26K);

        // FMP resident
        nise98.loadRun(fileNameFMP, "s -s", 0x2000);
        regs = nise98.getRegisters();

        // Running FMC
        nise98.getDos().setProgramTerminate(false);
        if ((rc = nise98.loadRun(fileNameFMC, playingFileName, 0x3000
                //, true, true, true, 3_000_000, 0
        )) != 0) return false;

        return true;
    }
}
