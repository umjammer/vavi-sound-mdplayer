
package mdplayer.driver.pmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JOptionPane;

import dotnet4j.Tuple;
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
import mdplayer.Log;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;


public class PMDDotNET extends BaseDriver {

    private iCompiler PMDCompiler = null;

    private iDriver PMDDriver = null;

    private static String[] envPmd = null;

    private static String[] envPmdOpt = null;

    private boolean isNRM;

    private boolean isSPB;

    private boolean isVA;

    private boolean usePPS;

    private boolean usePPZ;

    private String PlayingFileName;
    public String getPlayingFileName() { return PlayingFileName; }
    public void setPlayingFileName(String value) { PlayingFileName = value; }

    public static final int baseclock = 7987200;

    private enmPMDFileType mtype;

    public PMDDotNET() {
        // "plugin\\driver\\PMDDotNETCompiler.dll"
        // "plugin\\driver\\PMDDotNETdll"
    }

    public Gd3 getGD3Info(byte[] buf, int vgmGd3, enmPMDFileType mtype) {
        GD3Tag gt;

        if (mtype == enmPMDFileType.MML) {
            EnvironmentE env = new EnvironmentE();
            env.AddEnv("pmd");
            env.AddEnv("pmdopt");
            envPmd = env.GetEnvVal("pmd");
            envPmdOpt = env.GetEnvVal("pmdopt");

            PMDCompiler = im.GetCompiler("PMDDotNET.Compiler.Compiler");
            PMDCompiler.SetCompileSwitch((Function<String, Stream>) this::appendFileReaderCallback);
            gt = PMDCompiler.GetGD3TagInfo(buf);
        } else {
            PMDDriver = im.GetDriver("PMDDotNET.Driver.Driver");
            // PMDDriver.SetDriverSwitch((Func<String,
            // Stream>)appendFileReaderCallback);
            gt = PMDDriver.GetGD3TagInfo(buf);
        }

        Vgm.Gd3 g = new Gd3();
        g.trackName = gt.dicItem.containsKey(enmTag.Title) ? gt.dicItem[enmTag.Title][0] : "";
        g.trackNameJ = gt.dicItem.containsKey(enmTag.TitleJ) ? gt.dicItem[enmTag.TitleJ][0] : "";
        g.composer = gt.dicItem.containsKey(enmTag.Composer) ? gt.dicItem[enmTag.Composer][0] : "";
        g.composerJ = gt.dicItem.containsKey(enmTag.ComposerJ) ? gt.dicItem[enmTag.ComposerJ][0] : "";
        g.vgmBy = gt.dicItem.containsKey(enmTag.Artist) ? gt.dicItem[enmTag.Artist][0] : "";
        g.converted = gt.dicItem.containsKey(enmTag.ReleaseDate) ? gt.dicItem[enmTag.ReleaseDate][0] : "";

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
        mtype = fileType == 0 ? enmPMDFileType.MML : enmPMDFileType.M;
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

// #if DEBUG
        // 実チップスレッドは処理をスキップ(デバッグ向け)
        if (model == EnmModel.RealModel)
            return true;
// #endif

        if (mtype == enmPMDFileType.MML)
            return initMML();
        else
            return initM();
    }

    @Override
    public void oneFrameProc() {

// #if DEBUG
        // 実チップスレッドは処理をスキップ(デバッグ向け)
        if (model == EnmModel.RealModel) {
            stopped = true;
            return;
        }
// #endif

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().SampleRate * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                PMDDriver.Rendering();

                counter++;
                vgmFrameCounter++;
            }

            int lp = PMDDriver.GetNowLoopCounter();
            lp = Math.max(lp, 0);
            vgmCurLoop = lp;

            if (PMDDriver.GetStatus() < 1) {
                if (PMDDriver.GetStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0));// 実際の音声が発音しきるまでlatency*2の分だけ待つ
                }
                stopped = true;
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    public enum enmPMDFileType {
        unknown,
        MML,
        M
    }

    private enmPMDFileType CheckFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return enmPMDFileType.unknown;
        }

        if (buf[0] == 0x4d && buf[1] == 0x55 && buf[2] == 0x43 && buf[3] == 0x38) {
            return enmPMDFileType.M;
        }
        if (buf[0] == 0x4d && buf[1] == 0x55 && buf[2] == 0x42 && buf[3] == 0x38) {
            return enmPMDFileType.M;
        }

        return enmPMDFileType.MML;
    }

    private boolean initMML()
        {
            PMDCompiler.Init();

            MmlDatum[] ret;
            CompilerInfo info = null;
            try
            {
                PMDCompiler.SetCompileSwitch(String.format(
                    "PmdOption=%d \"%d\""
                    , setting.pmdDotNET.compilerArguments
                    , PlayingFileName));
                try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                    ret = PMDCompiler.Compile(sourceMML, this::appendFileReaderCallback);// wrkMUCFullPath, disp);
                }

                info = PMDCompiler.GetCompilerInfo();

            } catch (Exception e) {
                ret = null;
                info = null;
            }

            if (ret == null || info == null) return false;
            if (info.errorList.size() > 0) {
                if (model == EnmModel.VirtualModel) {
                    JOptionPane.showConfirmDialog(null, "Compile error");
                }
                return false;
            }

            if (PMDDriver == null) PMDDriver = im.GetDriver("PMDDotNET.Driver.Driver");

            //boolean notSoundBoard2 = false;
            boolean isLoadADPCM = true;
            boolean loadADPCMOnly = false;

            isNRM = setting.getPmdDotNET().soundBoard == 0;
            isSPB = setting.getPmdDotNET().soundBoard == 1;
            isVA = false;
            usePPS = setting.getPmdDotNET().usePPSDRV;
            usePPZ = setting.getPmdDotNET().usePPZ8;

            EnvironmentE env = new EnvironmentE();
            env.AddEnv("pmd");
            env.AddEnv("pmdopt");
            envPmd = env.GetEnvVal("pmd");
            envPmdOpt = env.GetEnvVal("pmdopt");

            Object[] addtionalPMDDotNETOption = new Object[] {
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
                PlayingFileName, // String srcFile;
                "", // String PPCFileHeader無視されます(設定不要)
                (Function<String,Stream>)this::appendFileReaderCallback
            };

            String[] addtionalPMDOption = GetPMDOption();

            //PMDDriver.Init(
            //    PlayingFileName
            //    , chipWriteRegister
            //    , chipWaitSend
            //    , ret
            //    , new Object[] {
            //          addtionalPMDDotNETOption //PMDDotNET option
            //        , addtionalPMDOption // PMD option
            //        , (Func<ChipDatum, int>)PPZ8Write
            //        , (Func<ChipDatum, int>)PPSDRVWrite
            //        , (Func<ChipDatum, int>)P86Write
            //    });

            List<ChipRunnable> lca = new ArrayList<ChipRunnable>();
            PMDChipRunnable ca = new PMDChipRunnable(OPNA1Write, OPNAWaitSend);
            lca.add(ca);

            PMDDriver.Init(
                lca
                //fileName
                //, oPNAWrite
                //, oPNAWaitSend
                , ret
                , null//ここのコールバックは未使用
                , new Object[] {
                      addtionalPMDDotNETOption //PMDDotNET option
                    , addtionalPMDOption // PMD option
                    , (Function<ChipDatum, Integer>)PPZ8Write
                    , (Function<ChipDatum, Integer>)PPSDRVWrite
                    , (Function<ChipDatum, Integer>)P86Write
                });


            PMDDriver.StartRendering(Common.VGMProcSampleRate
                , new Tuple[] {new Tuple<>("YM2608", baseclock) });
            PMDDriver.MusicSTART(0);
            return true;
        }

    private void OPNA1Write(ChipDatum cd) {
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

    private void OPNAWaitSend(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
            // JOptionPane.showConfirmDialog(String.format("elapsed:%d size:%d", elapsed,
            // size));
            // int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic
            // number)
            // Thread.sleep(n);
            return;
        }

        // サイズと経過時間から、追加でウエイトする。
        int m = Math.max((int) (size / 20 - elapsed), 0);// 20 閾値(magic number)
        Thread.sleep(m);
    }

    public static class PMDChipRunnable extends ChipRunnable {
        private Consumer<ChipDatum> oPNAWrite;

        private BiConsumer<Long, Integer> oPNAWaitSend;

        public PMDChipRunnable(Consumer<ChipDatum> oPNAWrite, BiConsumer<Long, Integer> oPNAWaitSend) {
            this.oPNAWrite = oPNAWrite;
            this.oPNAWaitSend = oPNAWaitSend;
        }

        @Override
        public String GetChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void WaitSend(long t1, int t2) {
            oPNAWaitSend(t1, t2);
        }

        @Override
        public void WritePCMData(byte[] data, int startAddress, int endAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void WriteRegister(ChipDatum cd) {
            oPNAWrite(cd);
        }
    }

    private boolean initM() {
        if (PMDDriver == null)
            PMDDriver = im.GetDriver("PMDDotNET.Driver.Driver");

        // boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;
        List<MmlDatum> buf = new ArrayList<MmlDatum>();
        for (byte b : vgmBuf)
            buf.add(new MmlDatum(b));

        isNRM = setting.pmdDotNET.soundBoard == 0;
        isSPB = setting.pmdDotNET.soundBoard == 1;
        isVA = false;
        usePPS = setting.pmdDotNET.usePPSDRV;
        usePPZ = setting.pmdDotNET.usePPZ8;

        EnvironmentE env = new EnvironmentE();
        env.AddEnv("pmd");
        env.AddEnv("pmdopt");
        envPmd = env.GetEnvVal("pmd");
        envPmdOpt = env.GetEnvVal("pmdopt");

        Object[] addtionalPMDDotNETOption = new Object[] {
            isLoadADPCM, // bool
            loadADPCMOnly, // bool
            setting.pmdDotNET.isAuto, // boolean isAUTO;
            isVA, // bool
            isNRM, // bool
            usePPS, // bool
            usePPZ, // bool
            isSPB, // bool
            envPmd, // String[] 環境変数PMD
            envPmdOpt, // String[] 環境変数PMDOpt
            PlayingFileName, // String srcFile;
            "", // String PPCFileHeader無視されます(設定不要)
            (Function<String, Stream>) this::appendFileReaderCallback
        };

        String[] addtionalPMDOption = GetPMDOption();

        // PMDDriver.Init(
        // PlayingFileName
        // , chipWriteRegister
        // , chipWaitSend
        // , buf.toArray()
        // , new Object[] {
        // addtionalPMDDotNETOption //PMDDotNET option
        // , addtionalPMDOption // PMD option
        // , (Func<ChipDatum, int>)PPZ8Write
        // , (Func<ChipDatum, int>)PPSDRVWrite
        // , (Func<ChipDatum, int>)P86Write
        // });
        List<ChipRunnable> lca = new ArrayList<ChipRunnable>();
        PMDChipRunnable ca = new PMDChipRunnable(OPNA1Write, OPNAWaitSend);
        lca.add(ca);

        PMDDriver.Init(lca
        // fileName
        // , oPNAWrite
        // , oPNAWaitSend
                       , buf.toArray(), null// ここのコールバックは未使用
                       ,
                       new Object[] {
                           addtionalPMDDotNETOption // PMDDotNET option
                           , addtionalPMDOption // PMD option
                           , (Function<ChipDatum, Integer>) PPZ8Write, (Function<ChipDatum, Integer>) PPSDRVWrite,
                           (Function<ChipDatum, Integer>) P86Write
                       });

        PMDDriver.StartRendering(Common.VGMProcSampleRate,
                                 new Tuple[] {
                                         new Tuple<>("YM2608", baseclock)
                                 });
        PMDDriver.MusicSTART(0);

        return true;
    }

    private void chipWaitSend(long elapsed, int size) {
        if (model == EnmModel.VirtualModel) {
            // JOptionPane.showConfirmDialog(String.format("elapsed:%d size:%d", elapsed,
            // size));
            // int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic
            // number)
            // Thread.sleep(n);
            return;
        }

        // サイズと経過時間から、追加でウエイトする。
        int m = Math.max((int) (size / 20 - elapsed), 0);// 20 閾値(magic number)
        Thread.sleep(m);
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
        // System.err.println("%d %d", dat.address, dat.data);
    }

    private int PPSDRVWrite(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x05) {
            chipRegister.PPSDRVLoad(0, (byte[]) arg.addtionalData, model);
        } else {
            chipRegister.PPSDRVWrite(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private int P86Write(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x00) {
            chipRegister.P86LoadPcm(0, (byte) arg.address, (byte) arg.data, (byte[]) arg.addtionalData, model);
        } else {
            chipRegister.P86Write(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private int PPZ8Write(ChipDatum arg) {
        if (arg == null)
            return 0;

        if (arg.port == 0x03) {
            chipRegister.PPZ8LoadPcm(0, (byte) arg.address, (byte) arg.data, (byte[][]) arg.addtionalData, model);
        } else {
            chipRegister.PPZ8Write(0, arg.port, arg.address, arg.data, model);
        }

        return 0;
    }

    private Stream appendFileReaderCallback(String arg) {
        String fn;
        fn = arg;
        String dir = Path.getDirectoryName(arg);
        if (dir == null || dir.isEmpty())
            fn = Path.combine(Path.getDirectoryName(PlayingFileName), fn);

        if (envPmd != null) {
            int i = 0;
            while (!File.exists(fn) && i < envPmd.length) {
                fn = Path.combine(envPmd[i++], Path.getFileName(arg));
            }
        }

        FileStream strm;
        try {
            strm = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            strm = null;
        }

        return strm;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int vgmGd3) {
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
        private List<String> envs = null;

        public EnvironmentE() {
            envs = new ArrayList<>();
        }

        public void AddEnv(String envname) {
            String env = System.getenv(envname);
            if (env != null && !env.isEmpty()) {
                envs.add(String.format("%s=%s", envname, env));
            }
        }

        public String[] GetEnv() {
            return envs.toArray(new String[0]);
        }

        public String[] GetEnvVal(String envname) {
            if (envs == null)
                return null;

            for (String item : envs) {
                String[] kv = item.split("=");
                if (kv == null)
                    continue;
                if (kv.length != 2)
                    continue;
                if (!kv[0].equalsIgnoreCase(envname))
                    continue;

                String[] vals = kv[1].split(";");
                return vals;
            }

            return null;
        }

    }

    private String[] GetPMDOption() {
        List<String> op = new ArrayList<>();

        // envPMDOpt
        if (envPmdOpt != null && envPmdOpt.length > 0)
            op.addAll(Arrays.asList(envPmdOpt));

        // 引数(IDEではオプション設定)
        String[] drvArgs = setting.pmdDotNET.driverArguments.split(" ");
        if (drvArgs != null && drvArgs.length > 0)
            op.addAll(Arrays.asList(drvArgs));

        return op.toArray(new String[0]);
    }

}
