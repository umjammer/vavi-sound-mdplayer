package mdplayer.driver.mucom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.Log;


public class MucomDotNET extends BaseDriver {
    private InstanceMarker im = null;
    private iCompiler mucomCompiler = null;
    private iDriver mucomDriver = null;

    private String PlayingFileName;

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    public static final int OPNAbaseclock = 7987200;
    public static final int OPNBbaseclock = 8000000;
    public static final int OPMbaseclock = 3579545;
    private enmMUCOMFileType mtype;

    public MucomDotNET(InstanceMarker mucomDotNET_Im) {
        im = mucomDotNET_Im;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        mtype = CheckFileType(buf);
        GD3Tag gt;

        if (mtype == enmMUCOMFileType.MUC) {
            mucomCompiler = im.GetCompiler("mucomDotNET.Compiler.Compiler");
            gt = mucomCompiler.GetGD3TagInfo(buf);
        } else {
            mucomDriver = im.GetDriver("mucomDotNET.Driver.Driver");
            gt = mucomDriver.GetGD3TagInfo(buf);
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

    public EnmChip[] useChipsFromMub(byte[] buf) {
        List<EnmChip> ret = new ArrayList<>();
        ret.add(EnmChip.YM2608);
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);

        //標準的なmubファイル
        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x43
                && buf[3] == 0x38) {
            return ret.toArray(new EnmChip[0]);
        }
        //標準的なmubファイル
        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x42
                && buf[3] == 0x38) {
            return ret.toArray(new EnmChip[0]);
        }
        //拡張mubファイル？
        if (buf[0] != 'm'
                || buf[1] != 'u'
                || buf[2] != 'P'
                || buf[3] != 'b') {
            //見知らぬファイル
            return null;
        }

        int chipsCount = buf[0x0009];
        int ptr = 0x0022;
        byte[] partCount = new byte[chipsCount];
        byte[][] pageCount = new byte[chipsCount][];
        Integer[][][] pageLength = new Integer[chipsCount][][];
        for (int i = 0; i < chipsCount; i++) {
            partCount[i] = buf[ptr + 0x16];
            int instCount = buf[ptr + 0x17];
            ptr += 2 * instCount + 0x18;
            int pcmCount = buf[ptr];
            ptr += 2 * pcmCount + 1;
        }

        for (int i = 0; i < chipsCount; i++) {
            pageCount[i] = new byte[partCount[i]];
            pageLength[i] = new Integer[partCount[i]][];
            for (int j = 0; j < partCount[i]; j++) {
                pageCount[i][j] = buf[ptr++];
            }
        }

        for (int i = 0; i < chipsCount; i++) {
            for (int j = 0; j < partCount[i]; j++) {
                pageLength[i][j] = new Integer[pageCount[i][j]];
                for (int k = 0; k < pageCount[i][j]; k++) {
                    pageLength[i][j][k] = Common.getLE32(buf, (int) ptr);
                    ptr += 8;
                }
            }
        }

        ret.clear();
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);
        ret.add(EnmChip.Unuse);

        if (chipsCount > 0) {
            if (partCount[0] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[0]; i++) {
                    n += pageCount[0][i];
                }
                if (n > 0) ret.set(0, EnmChip.YM2608);
            }
        }

        if (chipsCount > 1) {
            if (partCount[1] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[1]; i++) {
                    n += pageCount[1][i];
                }
                if (n > 0) ret.set(1, EnmChip.S_YM2608);
            }
        }

        if (chipsCount > 2) {
            if (partCount[2] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[2]; i++) {
                    n += pageCount[2][i];
                }
                if (n > 0) ret.set(2, EnmChip.YM2610);
            }
        }

        if (chipsCount > 3) {
            if (partCount[3] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[3]; i++) {
                    n += pageCount[3][i];
                }
                if (n > 0) ret.set(3, EnmChip.S_YM2610);
            }
        }

        if (chipsCount > 4) {
            if (partCount[4] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[4]; i++) {
                    n += pageCount[4][i];
                }
                if (n > 0) ret.set(4, EnmChip.YM2151);
            }
        }

        return ret.toArray(new EnmChip[0]);
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
        //if (model == EnmModel.RealModel) return true;
// #endif

        if (mtype == enmMUCOMFileType.MUC) return initMUC();
        else return initMUB();
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
            //Stopped = true;
            //return;
        }
// #endif

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                mucomDriver.rendering();

                counter++;
                vgmFrameCounter++;
            }

            int lp = mucomDriver.getNowLoopCounter();
            lp = lp < 0 ? 0 : lp;
            vgmCurLoop = (int) lp;

            if (mucomDriver.getStatus() < 1) {
                if (mucomDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0));//実際の音声が発音しきるまでlatency*2の分だけ待つ
                }
                stopped = true;
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }


    public enum enmMUCOMFileType {
        unknown,
        MUB,
        MUC
    }

    public byte[] Compile(byte[] vgmBuf) {
        if (mucomCompiler == null) mucomCompiler = im.GetCompiler("mucomDotNET.Compiler.Compiler");
        mucomCompiler.Init();

        MmlDatum[] ret;
        CompilerInfo info = null;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = mucomCompiler.Compile(sourceMML, appendFileReaderCallback);// wrkMUCFullPath, disp);
            }

            info = mucomCompiler.GetCompilerInfo();

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


    private enmMUCOMFileType CheckFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return enmMUCOMFileType.unknown;
        }

        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x43
                && buf[3] == 0x38) {
            return enmMUCOMFileType.MUB;
        }
        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x42
                && buf[3] == 0x38) {
            return enmMUCOMFileType.MUB;
        }
        if (buf[0] == 'm'
                && buf[1] == 'u'
                && buf[2] == 'P'
                && buf[3] == 'b') {
            return enmMUCOMFileType.MUB;
        }

        return enmMUCOMFileType.MUC;
    }


    private Boolean initMUC() {
        mucomCompiler.Init();

        MmlDatum[] ret;
        CompilerInfo info = null;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = mucomCompiler.Compile(sourceMML, appendFileReaderCallback);// wrkMUCFullPath, disp);
            }

            info = mucomCompiler.GetCompilerInfo();

        } catch (Exception e) {
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return false;
        if (info.errorList.size() > 0) {
            if (model == EnmModel.VirtualModel) {
                JOptionPane.showConfirmDialog("Compile error");
            }
            return false;
        }

        if (mucomDriver == null) mucomDriver = im.GetDriver("mucomDotNET.Driver.Driver");

        Boolean notSoundBoard2 = false;
        Boolean isLoadADPCM = true;
        Boolean loadADPCMOnly = false;

        //mucomDriver.Init(PlayingFileName,chipWriteRegister,chipWaitSend, ret, new Object[] {
        //          notSoundBoard2
        //        , isLoadADPCM
        //        , loadADPCMOnly
        //    });
        List<ChipRunnable> lca = new ArrayList<>();
        mucomChipRunnable ca;
        ca = new mucomChipRunnable(OPNA1Write, null, OPNAWaitSend);
        lca.add(ca);
        ca = new mucomChipRunnable(OPNA2Write, null, null);
        lca.add(ca);
        ca = new mucomChipRunnable(OPNB1Write, WriteOPNB1PCMData, null);
        lca.add(ca);
        ca = new mucomChipRunnable(OPNB2Write, WriteOPNB2PCMData, null);
        lca.add(ca);
        ca = new mucomChipRunnable(OPM1Write, null, null);
        lca.add(ca);
        mucomDriver.Init(
                lca,
                ret
                , null
                , new Object[] {
                        notSoundBoard2
                        , isLoadADPCM
                        , loadADPCMOnly
                        , PlayingFileName
                });

        mucomDriver.StartRendering(Common.VGMProcSampleRate
                , new Tuple<String, Integer>[] {new Tuple<>("", OPNAbaseclock)});
        mucomDriver.MusicSTART(0);

        return true;
    }

    private Boolean initMUB() {
        if (mucomDriver == null) mucomDriver = im.GetDriver("mucomDotNET.Driver.Driver");

        Boolean notSoundBoard2 = false;
        Boolean isLoadADPCM = true;
        Boolean loadADPCMOnly = false;
        List<MmlDatum> buf = new ArrayList<MmlDatum>();
        for (byte b : vgmBuf) buf.add(new MmlDatum(b));
        //mucomDriver.Init(PlayingFileName, chipWriteRegister, chipWaitSend, buf.toArray(), new Object[] {
        //          notSoundBoard2
        //        , isLoadADPCM
        //        , loadADPCMOnly
        //    });

        List<ChipRunnable> lca = new ArrayList<>();
        mucomChipRunnable ca;
        ca = new mucomChipRunnable(OPNA1Write, null, OPNAWaitSend);
        lca.add(ca);
        ca = new mucomChipRunnable(OPNA2Write, null, null);
        lca.add(ca);
        ca = new mucomChipRunnable(OPNB1Write, WriteOPNB1PCMData, null);
        lca.add(ca);
        ca = new mucomChipRunnable(OPNB2Write, WriteOPNB2PCMData, null);
        lca.add(ca);
        ca = new mucomChipRunnable(OPM1Write, null, null);
        lca.add(ca);
        mucomDriver.Init(
                lca,
                buf.toArray()
                , null
                , new Object[] {
                        notSoundBoard2
                        , isLoadADPCM
                        , loadADPCMOnly
                        , PlayingFileName
                });

        mucomDriver.StartRendering(Common.VGMProcSampleRate
                , new Tuple<String, Integer>[] {new Tuple<String, Integer>("", OPNAbaseclock)});
        mucomDriver.MusicSTART(0);

        return true;
    }

    private void OPNA1Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2608Register(0, cd.port, cd.address, cd.data, model);
    }

    private void OPNA2Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2608Register(1, cd.port, cd.address, cd.data, model);

    }

    private void OPNB1Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2610Register(0, cd.port, cd.address, cd.data, model);
    }

    private void OPNB2Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2610Register(1, cd.port, cd.address, cd.data, model);
    }

    private void OPM1Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;

        chipRegister.setYM2151Register(0, cd.port, cd.address, cd.data, model, 0, 0);
    }

    private void WriteOPNB1PCMData(byte[] dat, int v, int v2) {
        if (v == 0)
            chipRegister.writeYM2610_SetAdpcmA(0, dat, EnmModel.VirtualModel);
        else
            chipRegister.WriteYM2610_SetAdpcmB(0, dat, EnmModel.VirtualModel);
    }

    private void WriteOPNB2PCMData(byte[] dat, int v, int v2) {
        if (v == 0)
            chipRegister.writeYM2610_SetAdpcmA(1, dat, EnmModel.VirtualModel);
        else
            chipRegister.WriteYM2610_SetAdpcmB(1, dat, EnmModel.VirtualModel);
    }

    private void OPNAWaitSend(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
            //JOptionPane.showConfirmDialog(String.format("elapsed:{0} size:{1}", elapsed, size));
            //int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic number)
            //Thread.sleep(n);
            return;
        }

        //サイズと経過時間から、追加でウエイトする。
        int m = Math.max((int) (size / 20 - elapsed), 0);//20 閾値(magic number)
        Thread.sleep(m);
    }

    public class mucomChipRunnable extends ChipRunnable {
        private Consumer<ChipDatum> _Write;
        private mdsound.Common.TriConsumer<byte[], Integer, Integer> _WritePCMData;
        private BiConsumer<Long, Integer> _WaitSend;

        public mucomChipRunnable(Consumer<ChipDatum> Write, mdsound.Common.TriConsumer<byte[], Integer, Integer> WritePCMData, BiConsumer<Long, Integer> WaitSend) {
            _Write = Write;
            _WritePCMData = WritePCMData;
            _WaitSend = WaitSend;
        }

        @Override
        public String GetChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void WaitSend(long t1, int t2) {
            _WaitSend.Invoke(t1, t2);
        }

        @Override
        public void WritePCMData(byte[] data, int startAddress, int endAddress) {
            _WritePCMData.Invoke(data, startAddress, endAddress);
        }

        @Override
        public void WriteRegister(ChipDatum cd) {
            _Write.Invoke(cd);
        }
    }

    //private void chipWaitSend(long elapsed, int size)
    //{
    //    if (model == EnmModel.VirtualModel)
    //    {
    //        //JOptionPane.showConfirmDialog(String.format("elapsed:{0} size:{1}", elapsed, size));
    //        //int n = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic number)
    //        //Thread.sleep(n);
    //        return;
    //    }

    //    //サイズと経過時間から、追加でウエイトする。
    //    int m = Math.max((int)(size / 20 - elapsed), 0);//20 閾値(magic number)
    //    Thread.sleep(m);
    //}

    //private void chipWriteRegister(ChipDatum dat)
    //{
    //    if (dat == null) return;
    //    if (dat.address == -1) return;
    //    if (dat.data == -1) return;
    //    if (dat.port == -1) return;

    //    chipRegister.setYM2608Register(0, dat.port, dat.address, dat.data, model);
    //    //System.err.println("{0} {1}", dat.address, dat.data);
    //}

    private Stream appendFileReaderCallback(String arg) {

        String fn = Path.combine(
                Path.getDirectoryName(PlayingFileName)
                , arg
        );

        if (!File.exists(fn)) return null;

        FileStream strm;
        try {
            strm = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.read);
        } catch (IOException e) {
            strm = null;
        }

        return strm;
    }
}
