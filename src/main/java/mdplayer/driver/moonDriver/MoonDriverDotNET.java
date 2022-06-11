package mdplayer.driver.moonDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.Log;


public class MoonDriverDotNET extends BaseDriver {
    private InstanceMarker im = null;
    private iCompiler moonDriverCompiler = null;
    private iDriver moonDriverDriver = null;
    private enmMoonDriverFileType mtype;

    private String PlayingFileName;

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    public MoonDriverDotNET(InstanceMarker moonDriverDotNET_Im) {
        im = moonDriverDotNET_Im;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        mtype = CheckFileType(buf);
        GD3Tag gt;

        if (mtype == enmMoonDriverFileType.MDL) {
            moonDriverCompiler = im.GetCompiler("MoonDriverDotNET.Compiler.Compiler");
            gt = moonDriverCompiler.GetGD3TagInfo(buf);
        } else {
            moonDriverDriver = im.GetDriver("MoonDriverDotNET.Driver.Driver");
            gt = moonDriverDriver.GetGD3TagInfo(buf);
        }

        Vgm.Gd3 g = new Vgm.Gd3();
        g.trackName = gt.dicItem.containsKey(enmTag.Title) ? gt.dicItem[enmTag.Title][0] : "";
        g.trackNameJ = gt.dicItem.containsKey(enmTag.TitleJ) ? gt.dicItem[enmTag.TitleJ][0] : "";
        g.composer = gt.dicItem.containsKey(enmTag.Composer) ? gt.dicItem[enmTag.Composer][0] : "";
        g.composerJ = gt.dicItem.containsKey(enmTag.ComposerJ) ? gt.dicItem[enmTag.ComposerJ][0] : "";
        g.vgmBy = gt.dicItem.containsKey(enmTag.Artist) ? gt.dicItem[enmTag.Artist][0] : "";
        g.converted = gt.dicItem.containsKey(enmTag.ReleaseDate) ? gt.dicItem[enmTag.ReleaseDate][0] : "";

        return g;
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        gd3 = getGD3Info(vgmBuf, 0);

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
        //実チップスレッドは処理をスキップ(デバッグ向け)
        if (model == EnmModel.RealModel) return true;
// #endif

        if (mtype == enmMoonDriverFileType.MDL) return initMDL();
        else return initMDR();
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    @Override
    public void oneFrameProc() {
// #if DEBUG
        //実チップスレッドは処理をスキップ(デバッグ向け)
        if (model == EnmModel.RealModel) {
            stopped = true;
            return;
        }
// #endif

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                moonDriverDriver.Rendering();

                counter++;
                vgmFrameCounter++;
            }

            int lp = moonDriverDriver.GetNowLoopCounter();
            lp = lp < 0 ? 0 : lp;
            vgmCurLoop = (int) lp;

            if (moonDriverDriver.GetStatus() < 1) {
                if (moonDriverDriver.GetStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0));//実際の音声が発音しきるまでlatency*2の分だけ待つ
                }
                stopped = true;
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    public byte[] Compile(byte[] vgmBuf) {
        if (moonDriverCompiler == null) moonDriverCompiler = im.GetCompiler("MoonDriverDotNET.Compiler.Compiler");
        moonDriverCompiler.Init();
        moonDriverCompiler.SetCompileSwitch("SRC");
        moonDriverCompiler.SetCompileSwitch("MoonDriverOption=-i");
        moonDriverCompiler.SetCompileSwitch(String.format("MoonDriverOption={0}", PlayingFileName));

        MmlDatum[] ret;
        CompilerInfo info = null;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = moonDriverCompiler.Compile(sourceMML, appendFileReaderCallback);
            }

            info = moonDriverCompiler.GetCompilerInfo();

        } catch (Exception e) {
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return null;
        if (info.errorList.size() > 0) {
            if (model == EnmModel.VirtualModel) {
                JJOptionPane.showConfirmDialog("Compile error");
            }
            return null;
        }

        List<Byte> dest = new ArrayList<Byte>();
        for (MmlDatum md : ret) {
            dest.add(md != null ? (byte) md.dat : (byte) 0);
        }

        return mdsound.Common.toByteArray(dest);
    }


    public enum enmMoonDriverFileType {
        unknown,
        MDR,
        MDL
    }

    private enmMoonDriverFileType CheckFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return enmMoonDriverFileType.unknown;
        }

        if (buf[0] == 'M'
                && buf[1] == 'D'
                && buf[2] == 'R'
                && buf[3] == 'V') {
            return enmMoonDriverFileType.MDR;
        }

        return enmMoonDriverFileType.MDL;
    }

    private Boolean initMDL() {
        moonDriverCompiler.Init();

        MmlDatum[] ret;
        CompilerInfo info = null;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = moonDriverCompiler.Compile(sourceMML, appendFileReaderCallback);// wrkMUCFullPath, disp);
            }

            info = moonDriverCompiler.GetCompilerInfo();

        } catch (Exception e) {
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return false;
        if (info.errorList.size() > 0) {
            if (model == EnmModel.VirtualModel) {
                JJOptionPane.showConfirmDialog("Compile error");
            }
            return false;
        }

        if (moonDriverDriver == null) moonDriverDriver = im.GetDriver("MoonDriverDotNET.Driver.Driver");

        //Boolean notSoundBoard2 = false;
        //Boolean isLoadADPCM = true;
        //Boolean loadADPCMOnly = false;

        ////mucomDriver.Init(PlayingFileName,chipWriteRegister,chipWaitSend, ret, new Object[] {
        //         notSoundBoard2
        //       , isLoadADPCM
        //       , loadADPCMOnly
        //   });
        //List<ChipRunnable> lca = new ArrayList<ChipRunnable>();
        //mucomChipRunnable ca;
        //ca = new mucomChipRunnable(OPNA1Write, null, OPNAWaitSend); lca.add(ca);
        //ca = new mucomChipRunnable(OPNA2Write, null, null); lca.add(ca);
        //ca = new mucomChipRunnable(OPNB1Write, WriteOPNB1PCMData, null); lca.add(ca);
        //ca = new mucomChipRunnable(OPNB2Write, WriteOPNB2PCMData, null); lca.add(ca);
        //ca = new mucomChipRunnable(OPM1Write, null, null); lca.add(ca);
        //moonDriverDriver.Init(
        //    lca,
        //    ret
        //    , null
        //    , new Object[] {
        //          notSoundBoard2
        //        , isLoadADPCM
        //        , loadADPCMOnly
        //        , PlayingFileName
        //    });

        //moonDriverDriver.StartRendering(Common.VGMProcSampleRate
        //    , new Tuple<String, int>[] { new Tuple<String, int>("", OPNAbaseclock) });
        //moonDriverDriver.MusicSTART(0);

        return true;
    }

    private Boolean initMDR() {
        if (moonDriverDriver == null) moonDriverDriver = im.GetDriver("MoonDriverDotNET.Driver.Driver");

        List<MmlDatum> buf = new ArrayList<MmlDatum>();
        for (byte b : vgmBuf) buf.add(new MmlDatum(b));

        List<ChipRunnable> lca = new ArrayList<ChipRunnable>();
        ChipRunnable ca = new MoonDriverChipRunnable(opl4Write, opl4WaitSend);
        lca.add(ca);
        Object additionalOption = new Object[] {
                PlayingFileName,
                (double) 44100,
                (int) 0
        };
        moonDriverDriver.Init(
                lca,
                buf.toArray()
                , appendFileReaderCallback
                , additionalOption
        );

        moonDriverDriver.StartRendering(Common.VGMProcSampleRate
                , new Tuple[] {new Tuple<String, Integer>("YMF278B", (int) 33868800)});
        moonDriverDriver.MusicSTART(0);

        return true;
    }

    private Stream appendFileReaderCallback(String arg) {

        String fn = Path.combine(
                Path.getDirectoryName(PlayingFileName)
                , arg
        );

        if (!File.exists(fn)) return null;

        FileStream strm;
        try {
            strm = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            strm = null;
        }

        return strm;
    }

    //public void WriteRegister(ChipDatum dat)
    //{
    //    //Log.WriteLine(LogLevel.TRACE, String.format("FM P{2} Out:Adr[{0:x02}] val[{1:x02}]", (int)dat.address, (int)dat.data, dat.port));
    //    //System.err.println("FM P{2} Out:Adr[{0:x02}] val[{1:x02}]", (int)dat.address, (int)dat.data, dat.port);
    //    outDatum od = null;

    //    if (pcmdata.size() > 0)
    //    {
    //        chipRegister.YMF278BSetRegister(od, count, 0, pcmdata.toArray());
    //        pcmdata.clear();
    //    }

    //    if (dat.addtionalData != null)
    //    {
    //        if (dat.addtionalData instanceof MmlDatum)
    //        {
    //            MmlDatum md = (MmlDatum)dat.addtionalData;
    //            if (md.linePos != null) md.linePos.srcMMLID = filename;
    //            od = new outDatum(md.type, md.args, md.linePos, (byte)md.dat);
    //        }

    //    }

    //    //if (od != null && od.linePos != null)
    //    //{
    //    //System.err.println("{0}", od.linePos.col);
    //    //}

    //    //chipRegister.YM2608SetRegister(od, (long)dat.time, 0, dat.port, dat.address, dat.data);
    //    chipRegister.YMF278BSetRegister(od, count, 0, dat.port, dat.address, dat.data);
    //}

    private void opl4Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYMF278BRegister(0, cd.port, cd.address, cd.data, model);
    }

    private void opl4WaitSend(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
            //JOptionPane.showConfirmDialog(String.format("elapsed:{0} size:{1}", elapsed, size));
            //int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic number)
            //Thread.sleep(n);
            return;
        }

        ////サイズと経過時間から、追加でウエイトする。
        //int m = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic number)
        //Thread.sleep(m);
    }

    public class MoonDriverChipRunnable extends ChipRunnable {
        private Consumer<ChipDatum> opl4Write;
        private BiConsumer<Long, Integer> opl4WaitSend;

        public MoonDriverChipRunnable(Consumer<ChipDatum> opl4Write, BiConsumer<Long, Integer> opl4WaitSend) {
            this.opl4Write = opl4Write;
            this.opl4WaitSend = opl4WaitSend;
        }

        @Override
        public String GetChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void WaitSend(long t1, int t2) {
            opl4WaitSend(t1, t2);
        }

        @Override
        public void WritePCMData(byte[] data, int startAddress, int endAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void WriteRegister(ChipDatum cd) {
            opl4Write(cd);
        }
    }
}
