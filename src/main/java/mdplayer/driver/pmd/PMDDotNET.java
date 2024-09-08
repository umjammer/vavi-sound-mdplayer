
package mdplayer.driver.pmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.JOptionPane;

import dotnet4j.util.compat.Tuple;
import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileShare;
import dotnet4j.io.FileStream;
import dotnet4j.io.IOException;
import dotnet4j.io.MemoryStream;
import dotnet4j.io.Path;
import dotnet4j.io.Stream;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.GD3Tag;
import musicDriverInterface.InstanceMarker;
import musicDriverInterface.MmlDatum;
import musicDriverInterface.Tag;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;


public class PMDDotNET extends BaseDriver {

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

    public static final int baseclock = 7987200;

    private PMDFileType mtype;

    public PMDDotNET() {
        // "chips\\driver\\PMDDotNETCompiler.dll"
        // "chips\\driver\\PMDDotNETdll"
    }

    InstanceMarker im;

    public Gd3 getGD3Info(byte[] buf, int vgmGd3, PMDFileType mtype) {
        GD3Tag gt;

        if (mtype == PMDFileType.MML) {
            EnvironmentE env = new EnvironmentE();
            env.addEnv("pmd");
            env.addEnv("pmdopt");
            envPmd = env.getEnvVal("pmd");
            envPmdOpt = env.getEnvVal("pmdopt");

            pmdCompiler = im.getCompiler("PMDDotNET.Compiler.Compiler");
            pmdCompiler.setCompileSwitch((Function<String, Stream>) this::appendFileReaderCallback);
            gt = pmdCompiler.getGD3TagInfo(buf);
        } else {
            pmdDriver = im.getDriver("PMDDotNET.Driver.Driver");
            // pmdDriver.SetDriverSwitch((Func<String,
            // Stream>)appendFileReaderCallback);
            gt = pmdDriver.getGD3TagInfo(buf);
        }

        Vgm.Gd3 g = new Gd3();
        g.trackName = gt.items.containsKey(Tag.Title) ? gt.items.get(Tag.Title)[0] : "";
        g.trackNameJ = gt.items.containsKey(Tag.TitleJ) ? gt.items.get(Tag.TitleJ)[0] : "";
        g.composer = gt.items.containsKey(Tag.Composer) ? gt.items.get(Tag.Composer)[0] : "";
        g.composerJ = gt.items.containsKey(Tag.ComposerJ) ? gt.items.get(Tag.ComposerJ)[0] : "";
        g.vgmBy = gt.items.containsKey(Tag.Artist) ? gt.items.get(Tag.Artist)[0] : "";
        g.converted = gt.items.containsKey(Tag.ReleaseDate) ? gt.items.get(Tag.ReleaseDate)[0] : "";

        return g;
    }

    @Override
    public boolean init(byte[] vgmBuf,
                        int fileType,
                        ChipRegister chipRegister,
                        EnmModel model,
                        EnmChip[] useChip,
                        int latency,
                        int waitTime) {
        mtype = fileType == 0 ? PMDFileType.MML : PMDFileType.M;
        gd3 = getGD3Info(vgmBuf, 0, mtype);

        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

//#if DEBUG
        // 実チップスレッドは処理をスキップ(デバッグ向け)
        if (model == EnmModel.RealModel)
            return true;
//#endif

        if (mtype == PMDFileType.MML)
            return initMML();
        else
            return initM();
    }

    @Override
    public void processOneFrame() {

//#if DEBUG
        // 実チップスレッドは処理をスキップ(デバッグ向け)
        if (model == EnmModel.RealModel) {
            stopped = true;
            return;
        }
//#endif

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                pmdDriver.render();

                counter++;
                vgmFrameCounter++;
            }

            int lp = pmdDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            vgmCurLoop = lp;

            if (pmdDriver.getStatus() < 1) {
                if (pmdDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0));// 実際の音声が発音しきるまでlatency*2の分だけ待つ
                }
                stopped = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum PMDFileType {
        unknown,
        MML,
        M
    }

    private PMDFileType CheckFileType(byte[] buf) {
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

    private boolean initMML()
        {
            pmdCompiler.init();

            MmlDatum[] ret;
            CompilerInfo info;
            try {
                pmdCompiler.setCompileSwitch(String.format(
                    "PmdOption=%s \"%s\""
                    , setting.getPmdDotNET().compilerArguments
                    , playingFileName));
                try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                    ret = pmdCompiler.compile(sourceMML, this::appendFileReaderCallback);// wrkMUCFullPath, disp);
                }

                info = pmdCompiler.getCompilerInfo();

            } catch (Exception e) {
                e.printStackTrace();
                ret = null;
                info = null;
            }

            if (ret == null || info == null) return false;
            if (info.errorList.size() > 0) {
                if (model == EnmModel.VirtualModel) {
                    JOptionPane.showMessageDialog(null, "Compile error");
                }
                return false;
            }

            if (pmdDriver == null) pmdDriver = im.getDriver("PMDDotNET.Driver.Driver");

            //boolean notSoundBoard2 = false;
            boolean isLoadADPCM = true;
            boolean loadADPCMOnly = false;

            isNRM = setting.getPmdDotNET().soundBoard == 0;
            isSPB = setting.getPmdDotNET().soundBoard == 1;
            isVA = false;
            usePPS = setting.getPmdDotNET().usePPSDRV;
            usePPZ = setting.getPmdDotNET().usePPZ8;

            EnvironmentE env = new EnvironmentE();
            env.addEnv("pmd");
            env.addEnv("pmdopt");
            envPmd = env.getEnvVal("pmd");
            envPmdOpt = env.getEnvVal("pmdopt");

            Object[] additionalPDDDotNETOption = new Object[] {
                isLoadADPCM, // bool
                loadADPCMOnly, // bool
                setting.getPmdDotNET().isAuto, // boolean isAUTO;
                isVA, // bool
                isNRM, // bool
                usePPS, // bool
                usePPZ, // bool
                isSPB, // bool
                envPmd, // String[] 環境変数PMD
                envPmdOpt, // String[] 環境変数PMDOpt
                    playingFileName, // String srcFile;
                "", // String PPCFileHeader無視されます(設定不要)
                (Function<String,Stream>)this::appendFileReaderCallback
            };

            String[] addtionalPMDOption = getPMDOption();

            List<ChipAction> lca = new ArrayList<>();
            PMDChipAction ca = new PMDChipAction(this::writeOPNA1, this::sendOPNAWait);
            lca.add(ca);

            pmdDriver.init(
                lca
                //fileName
                //, oPNAWrite
                //, oPNAWaitSend
                , ret
                , null//ここのコールバックは未使用
                , additionalPDDDotNETOption //PMDDotNET option
                    , addtionalPMDOption // PMD option
                    , (Function<ChipDatum, Integer>)this::writePPZ8
                    , (Function<ChipDatum, Integer>)this::writePPSDRV
                    , (Function<ChipDatum, Integer>)this::writeP86);


            pmdDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YM2608", baseclock));
            pmdDriver.startMusic(0);
            return true;
        }

    private void writeOPNA1(ChipDatum cd) {
        if (cd == null)
            return;
        if (cd.address == -1)
            return;
        if (cd.data == -1)
            return;
        if (cd.port == -1)
            return;

        chipRegister.setYM2608Register(0, cd.port, cd.address, cd.data, model);
    }

    private void sendOPNAWait(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
            // JOptionPane.showMessageDialog(String.format("elapsed:%d size:%d", elapsed,
            // size));
            // int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic
            // number)
            // Thread.sleep(n);
            return;
        }

        // サイズと経過時間から、追加でウエイトする。
        int m = Math.max((int) (size / 20 - elapsed), 0);// 20 閾値(magic number)
        try { Thread.sleep(m); } catch (InterruptedException e) {}
    }

    public static class PMDChipAction implements ChipAction {
        private Consumer<ChipDatum> oPNAWrite;

        private BiConsumer<Long, Integer> oPNAWaitSend;

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

    private boolean initM() {
        if (pmdDriver == null)
            pmdDriver = im.getDriver("PMDDotNET.Driver.Driver");

        // boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;
        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : vgmBuf)
            buf.add(new MmlDatum(b & 0xff));

        isNRM = setting.getPmdDotNET().soundBoard == 0;
        isSPB = setting.getPmdDotNET().soundBoard == 1;
        isVA = false;
        usePPS = setting.getPmdDotNET().usePPSDRV;
        usePPZ = setting.getPmdDotNET().usePPZ8;

        EnvironmentE env = new EnvironmentE();
        env.addEnv("pmd");
        env.addEnv("pmdopt");
        envPmd = env.getEnvVal("pmd");
        envPmdOpt = env.getEnvVal("pmdopt");

        Object[] additionalPDDDotNETOption = new Object[] {
            isLoadADPCM, // bool
            loadADPCMOnly, // bool
            setting.getPmdDotNET().isAuto, // boolean isAUTO;
            isVA, // bool
            isNRM, // bool
            usePPS, // bool
            usePPZ, // bool
            isSPB, // bool
            envPmd, // String[] 環境変数PMD
            envPmdOpt, // String[] 環境変数PMDOpt
                playingFileName, // String srcFile;
            "", // String PPCFileHeader無視されます(設定不要)
            (Function<String, Stream>) this::appendFileReaderCallback
        };

        String[] additionalPMDOption = getPMDOption();

        List<ChipAction> lca = new ArrayList<>();
        PMDChipAction ca = new PMDChipAction(this::writeOPNA1, this::sendOPNAWait);
        lca.add(ca);

        pmdDriver.init(lca,
                buf.toArray(MmlDatum[]::new),
                null, // ここのコールバックは未使用
                additionalPDDDotNETOption, // PMDDotNET option
                additionalPMDOption, // PMD option
                (Function<ChipDatum, Integer>) this::writePPZ8,
                (Function<ChipDatum, Integer>) this::writePPSDRV,
                (Function<ChipDatum, Integer>) this::writeP86);

        pmdDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YM2608", baseclock));
        pmdDriver.startMusic(0);

        return true;
    }

    private void chipWaitSend(long elapsed, int size) {
        if (model == EnmModel.VirtualModel) {
            //JOptionPane.showMessageDialog(null, String.format("elapsed:%d size:%d", elapsed, size));
            //int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic number)
            //Thread.sleep(n);
            return;
        }

        // サイズと経過時間から、追加でウエイトする。
        int m = Math.max((int) (size / 20 - elapsed), 0);// 20 閾値 (magic number)
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

        chipRegister.setYM2608Register(0, dat.port, dat.address, dat.data, model);
        //Debug.printf("%d %d", dat.address, dat.data);
    }

    private int writePPSDRV(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x05) {
            chipRegister.loadPPSDRV(0, (byte[]) arg.additionalData, model);
        } else {
            chipRegister.writePPSDRV(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private int writeP86(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x00) {
            chipRegister.loadPcmP86(0, arg.address, arg.data, (byte[]) arg.additionalData, model);
        } else {
            chipRegister.writeP86(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private int writePPZ8(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x03) {
            chipRegister.loadPcmPPZ8(0, arg.address, arg.data, (byte[][]) arg.additionalData, model);
        } else {
            chipRegister.writePPZ8(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private Stream appendFileReaderCallback(String arg) {
        String fileName;
        fileName = arg;
        String dir = Path.getDirectoryName(arg);
        if (dir == null || dir.isEmpty())
            fileName = Path.combine(Path.getDirectoryName(playingFileName), fileName);

        if (envPmd != null) {
            int i = 0;
            while (!File.exists(fileName) && i < envPmd.length) {
                fileName = Path.combine(envPmd[i++], Path.getFileName(arg));
            }
        }

        FileStream stream;
        try {
            stream = new FileStream(fileName, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            e.printStackTrace();
            stream = null;
        }

        return stream;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean init(byte[] vgmBuf,
                        ChipRegister chipRegister,
                        EnmModel model,
                        EnmChip[] useChip,
                        int latency,
                        int waitTime) {
        throw new UnsupportedOperationException();
    }

    public static class EnvironmentE {
        private List<String> envs;

        public EnvironmentE() {
            envs = new ArrayList<>();
        }

        public void addEnv(String envName) {
            String env = System.getenv(envName);
            if (env != null && !env.isEmpty()) {
                envs.add(String.format("%s=%s", envName, env));
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

    private String[] getPMDOption() {
        List<String> op = new ArrayList<>();

        // envPMDOpt
        if (envPmdOpt != null && envPmdOpt.length > 0)
            op.addAll(Arrays.asList(envPmdOpt));

        // 引数(IDEではオプション設定)
        String[] drvArgs = setting.getPmdDotNET().driverArguments.split(" ");
        if (drvArgs != null && drvArgs.length > 0)
            op.addAll(Arrays.asList(drvArgs));

        return op.toArray(String[]::new);
    }
}
