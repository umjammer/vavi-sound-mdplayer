
package mdplayer.driver.pmd;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileShare;
import dotnet4j.io.FileStream;
import dotnet4j.io.IOException;
import dotnet4j.io.MemoryStream;
import dotnet4j.io.Path;
import dotnet4j.io.Stream;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.P86Chip;
import mdplayer.chips.PpsChip;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.format.MMLFileFormat;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;
import musicDriverInterface.MetaData;
import musicDriverInterface.MmlDatum;

import static java.lang.System.getLogger;


/**
 * PMD
 * <p>
 * environment variable
 * <li>{@code mdplayer.pmd.dir} ... </li>
 * <li>{@code mdplayer.pmd.opt} ... </li>
 *
 * @author kumatan
 */
public class PmdDriver extends BaseDriver {

    private static final Logger logger = getLogger(PmdDriver.class.getName());

    private ICompiler pmdCompiler = null;

    private IDriver pmdDriver = null;

    private static String[] envPmd = null;

    private static String[] envPmdOpt = null;

    private boolean isNRM;

    private boolean isSPB;

    private boolean isVA;

    private boolean usePPS;

    private boolean usePPZ;

    private String playingFileName;
    public String getPlayingFileName() { return playingFileName; }
    public void setPlayingFileName(String value) { playingFileName = value; }

    public static final int baseClock = 7987200;

    private PMDFileType mtype;

    public PmdDriver() {
    }

    public MetaData getMetaData(byte[] buf, int vgmGd3, PMDFileType mtype) {
        MetaData metaData;

        if (mtype == PMDFileType.MML) {
            EnvironmentE env = new EnvironmentE();
            env.addEnv("mdplayer.pmd.dir");
            env.addEnv("mdplayer.pmd.opt");
            envPmd = env.getEnvVal("mdplayer.pmd.dir");
            envPmdOpt = env.getEnvVal("mdplayer.pmd.opt");

            pmdCompiler = ICompiler.factory("pmd.compiler.Compiler");
            pmdCompiler.setCompileSwitch((Function<String, Stream>) this::appendFileReaderCallback);
            metaData = pmdCompiler.getMetaData(buf);
        } else {
            pmdDriver = IDriver.factory("pmd.driver.Driver");
            // pmdDriver.SetDriverSwitch((Func<String, Stream>)appendFileReaderCallback);
            metaData = pmdDriver.getMetaData(buf);
        }

        return metaData;
    }

    /**
     * @param args 0: FileFormat
     */
    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {

        FileFormat fileFormat = (FileFormat) args[0];
        mtype = fileFormat instanceof MMLFileFormat ? PMDFileType.MML : PMDFileType.M;
        metaData = getMetaData(vgmBuf, 0, mtype);

        this.dataBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;

//#if DEBUG
        // The actual chip thread skips processing (for debugging)
        if (model == EnmModel.RealModel)
            return;
//#endif

        if (mtype == PMDFileType.MML)
            initMML();
        else
            initM();
    }

    @Override
    public void processOneFrame() {

//#if DEBUG
        // The actual chip thread skips processing (for debugging)
        if (model == EnmModel.RealModel) {
            stopped = true;
            return;
        }
//#endif

        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                pmdDriver.render();

                counter++;
                frameCounter++;
            }

            int lp = pmdDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            curLoop = lp;

            if (pmdDriver.getStatus() < 1) {
                if (pmdDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0)); // Wait for latency*2 until the actual voice is fully pronounced
                }
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public enum PMDFileType {
        unknown,
        MML,
        M
    }

    private static PMDFileType checkFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return PMDFileType.unknown;
        }

        if (buf[0] == 0x4d && buf[1] == 0x55 && buf[2] == 0x43 && buf[3] == 0x38) {
            return PMDFileType.M;
        }
        if (buf[0] == 0x4d && buf[1] == 0x55 && buf[2] == 0x42 && buf[3] == 0x38) {
            return PMDFileType.M;
        }

        return PMDFileType.MML;
    }

    private void initMML() {
        pmdCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            pmdCompiler.setCompileSwitch("PmdOption=%s \"%s\"".formatted(
                    setting.getPmd().compilerArguments, playingFileName));
            try (MemoryStream sourceMML = new MemoryStream(dataBuf)) {
                ret = pmdCompiler.compile(sourceMML, this::appendFileReaderCallback);// wrkMUCFullPath, disp);
            }

            info = pmdCompiler.getCompilerInfo();

        } catch (Exception e) {
            throw new IllegalStateException("error in compiling", e);
        }

        if (!info.errorList.isEmpty()) {
//            if (model == EnmModel.VirtualModel) {
//                JOptionPane.showMessageDialog(null, "Compile error");
//            }
            throw new IllegalArgumentException("Compile error: " + info.errorList);
        }

        if (pmdDriver == null) pmdDriver = IDriver.factory("pmd.driver.Driver");

//        boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;

        isNRM = setting.getPmd().soundBoard == 0;
        isSPB = setting.getPmd().soundBoard == 1;
        isVA = false;
        usePPS = setting.getPmd().usePPSDRV;
        usePPZ = setting.getPmd().usePPZ8;

        EnvironmentE env = new EnvironmentE();
        env.addEnv("pmd");
        env.addEnv("pmdopt");
        envPmd = env.getEnvVal("pmd");
        envPmdOpt = env.getEnvVal("pmdopt");

        Object[] additionalPDDDotNETOption = {
                isLoadADPCM, // bool
                loadADPCMOnly, // bool
                setting.getPmd().isAuto, // boolean isAUTO;
                isVA, // bool
                isNRM, // bool
                usePPS, // bool
                usePPZ, // bool
                isSPB, // bool
                envPmd, // String[] Environment variable PMD
                envPmdOpt, // String[] Environment variable PMDOpt
                playingFileName, // String srcFile;
                "", // String PPCFileHeader is ignored (no setting required)
                (Function<String, Stream>) this::appendFileReaderCallback
        };

        String[] additionalPMOption = getPMDOption();

        List<ChipAction> lca = new ArrayList<>();
        PMDChipAction ca = new PMDChipAction(this::writeOPNA1, this::sendOPNAWait);
        lca.add(ca);

        pmdDriver.init(
                lca,
                // fileName,
                // oPNAWrite,
                // oPNAWaitSend,
                ret,
                null, // This callback is unused
                additionalPDDDotNETOption, // PMDDotNET option
                additionalPMOption, // PMD option
                (Function<ChipDatum, Integer>) this::writePPZ8,
                (Function<ChipDatum, Integer>) this::writePPSDRV,
                (Function<ChipDatum, Integer>) this::writeP86);


        pmdDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YM2608", baseClock));
        pmdDriver.startMusic(0);
    }

    private void writeOPNA1(ChipDatum cd) {
        if (cd == null)
            return;
        if (cd.address == -1)
            return;
        if (cd.data == -1)
            return;
        if (cd.port == -1 || cd.port == 10000) // vavi
            return;

        plugin.chipRegister.chip(Ym2608Chip.class).write(0, cd.port, cd.address, cd.data, model);
    }

    private void sendOPNAWait(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
//            JOptionPane.showMessageDialog("elapsed:%d size:%d".formatted(elapsed, size));
//            int n = Math.max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
//            Thread.sleep(n);
            return;
        }

        // Add additional weight based on size and elapsed time.
        int m = Math.max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
        try { Thread.sleep(m); } catch (InterruptedException _) {}
    }

    public static class PMDChipAction implements ChipAction {
        private final Consumer<ChipDatum> oPNAWrite;

        private final BiConsumer<Long, Integer> oPNAWaitSend;

        public PMDChipAction(Consumer<ChipDatum> oPNAWrite, BiConsumer<Long, Integer> oPNAWaitSend) {
            this.oPNAWrite = oPNAWrite;
            this.oPNAWaitSend = oPNAWaitSend;
        }

        @Override
        public String getChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void waitSend(long t1, int t2) {
            oPNAWaitSend.accept(t1, t2);
        }

        @Override
        public void writePCMData(byte[] data, int startAddress, int endAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeRegister(ChipDatum cd) {
            oPNAWrite.accept(cd);
        }
    }

    private void initM() {
        if (pmdDriver == null)
            pmdDriver = IDriver.factory("pmd.driver.Driver");

        // boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;
        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : dataBuf)
            buf.add(new MmlDatum(b & 0xff));

        isNRM = setting.getPmd().soundBoard == 0;
        isSPB = setting.getPmd().soundBoard == 1;
        isVA = false;
        usePPS = setting.getPmd().usePPSDRV;
        usePPZ = setting.getPmd().usePPZ8;

        EnvironmentE env = new EnvironmentE();
        env.addEnv("mdplayer.pmd.dir");
        env.addEnv("mdplayer.pmd.opt");
        envPmd = env.getEnvVal("mdplayer.pmd.dir");
        envPmdOpt = env.getEnvVal("mdplayer.pmd.opt");

        Object[] additionalPDDDotNETOption = new Object[] {
            isLoadADPCM, // bool
            loadADPCMOnly, // bool
            setting.getPmd().isAuto, // boolean isAUTO;
            isVA, // bool
            isNRM, // bool
            usePPS, // bool
            usePPZ, // bool
            isSPB, // bool
            envPmd, // String[] Environment variable PMD
            envPmdOpt, // String[] Environment variable PMDOpt
                playingFileName, // String srcFile;
            "", // String PPCFileHeader is ignored (no setting required)
            (Function<String, Stream>) this::appendFileReaderCallback
        };

        String[] additionalPMDOption = getPMDOption();

        List<ChipAction> lca = new ArrayList<>();
        PMDChipAction ca = new PMDChipAction(this::writeOPNA1, this::sendOPNAWait);
        lca.add(ca);

        pmdDriver.init(lca,
                buf.toArray(MmlDatum[]::new),
                null, // This callback is unused
                additionalPDDDotNETOption, // PMDDotNET option
                additionalPMDOption, // PMD option
                (Function<ChipDatum, Integer>) this::writePPZ8,
                (Function<ChipDatum, Integer>) this::writePPSDRV,
                (Function<ChipDatum, Integer>) this::writeP86);

        pmdDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YM2608", baseClock));
        pmdDriver.startMusic(0);
    }

    private void chipWaitSend(long elapsed, int size) {
        if (model == EnmModel.VirtualModel) {
            //JOptionPane.showMessageDialog(null, "elapsed:%d size:%d".formatted(elapsed, size));
            //int n = Math.max((int)(size / 20 - elapsed), 0);//20 Threshold (magic number)
            //Thread.sleep(n);
            return;
        }

        // Add additional weight based on size and elapsed time.
        int m = Math.max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
        try { Thread.sleep(m); } catch (InterruptedException e) {}
    }

    private void chipWriteRegister(ChipDatum dat) {
        if (dat == null)
            return;
        if (dat.address == -1)
            return;
        if (dat.data == -1)
            return;
        if (dat.port == -1)
            return;

        plugin.chipRegister.chip(Ym2608Chip.class).write(0, dat.port, dat.address, dat.data, model);
        //logger.log(Level.TRACE, "%d %d".formatted(dat.address, dat.data));
    }

    private int writePPSDRV(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x05) {
            plugin.chipRegister.chip(PpsChip.class).writePcm(0, (byte[]) arg.additionalData, model);
        } else {
            plugin.chipRegister.chip(PpsChip.class).write(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private int writeP86(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x00) {
            plugin.chipRegister.chip(P86Chip.class).writePcm(0, arg.address, arg.data, (byte[]) arg.additionalData, model);
        } else {
            plugin.chipRegister.chip(P86Chip.class).write(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private int writePPZ8(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x03) {
            plugin.chipRegister.chip(Ppz8Chip.class).writePcm(0, arg.address, arg.data, (byte[][]) arg.additionalData, model);
        } else {
            plugin.chipRegister.chip(Ppz8Chip.class).write(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private Stream appendFileReaderCallback(String arg) {
logger.log(Level.DEBUG, "find pmd additional file: " + arg);
        String fileName;
        fileName = arg;
        String dir = Path.getDirectoryName(arg);
        if (dir == null || dir.isEmpty())
            fileName = Path.combine(Path.getDirectoryName(playingFileName.replace(java.io.File.separator, "\\")), fileName);

        if (envPmd != null) {
            int i = 0;
            while (!File.exists(fileName.replace("\\", java.io.File.separator)) && i < envPmd.length) {
                fileName = Path.combine(envPmd[i++], Path.getFileName(arg));
            }
        }

        FileStream stream;
        try {
logger.log(Level.DEBUG, "found pmd additional file: " + fileName.replace("\\", java.io.File.separator));
            stream = new FileStream(fileName.replace("\\", java.io.File.separator), FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            stream = null;
        }

        return stream;
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        throw new UnsupportedOperationException();
    }

    public static class EnvironmentE {
        private final List<String> envs;

        public EnvironmentE() {
            envs = new ArrayList<>();
        }

        public void addEnv(String envName) {
            String env = System.getProperty(envName);
            if (env != null && !env.isEmpty()) {
                envs.add("%s=%s".formatted(envName, env));
            }
        }

        public String[] getEnvs() {
            return envs.toArray(String[]::new);
        }

        public String[] getEnvVal(String envName) {
            if (envs == null)
                return null;

            for (String item : envs) {
                String[] kv = item.split("=");
                if (kv == null)
                    continue;
                if (kv.length != 2)
                    continue;
                if (!kv[0].equalsIgnoreCase(envName))
                    continue;

                String[] vals = kv[1].split(";");
                return vals;
            }

            return null;
        }
    }

    private static String[] getPMDOption() {
        List<String> op = new ArrayList<>();

        // envPMDOpt
        if (envPmdOpt != null && envPmdOpt.length > 0)
            op.addAll(Arrays.asList(envPmdOpt));

        // Arguments (optional in the IDE)
        String[] drvArgs = setting.getPmd().driverArguments.split(" ");
        if (drvArgs != null && drvArgs.length > 0)
            op.addAll(Arrays.asList(drvArgs));

        return op.toArray(String[]::new);
    }
}
