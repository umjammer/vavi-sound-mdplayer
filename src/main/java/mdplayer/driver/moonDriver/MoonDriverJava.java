package mdplayer.driver.moonDriver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

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
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.GD3Tag;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;
import musicDriverInterface.MmlDatum;
import musicDriverInterface.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class MoonDriverJava extends BaseDriver {

    private static final Logger logger = getLogger(MoonDriverJava.class.getName());

    private ICompiler moonDriverCompiler = null;
    private IDriver moonDriverDriver = null;
    private MoonDriverFileType mtype;

    private String PlayingFileName;

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    public MoonDriverJava() {
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        mtype = checkFileType(buf);
        GD3Tag gt;

        if (mtype == MoonDriverFileType.MDL) {
            moonDriverCompiler = ICompiler.factory("moonDriver.compiler.Compiler");
            gt = moonDriverCompiler.getGD3TagInfo(buf);
        } else {
            moonDriverDriver = IDriver.factory("moonDriver.driver.Driver");
            gt = moonDriverDriver.getGD3TagInfo(buf);
        }

        Vgm.Gd3 g = new Vgm.Gd3();
        g.trackName = gt.items.containsKey(Tag.Title) ? gt.items.get(Tag.Title)[0] : "";
        g.trackNameJ = gt.items.containsKey(Tag.TitleJ) ? gt.items.get(Tag.TitleJ)[0] : "";
        g.composer = gt.items.containsKey(Tag.Composer) ? gt.items.get(Tag.Composer)[0] : "";
        g.composerJ = gt.items.containsKey(Tag.ComposerJ) ? gt.items.get(Tag.ComposerJ)[0] : "";
        g.vgmBy = gt.items.containsKey(Tag.Artist) ? gt.items.get(Tag.Artist)[0] : "";
        g.converted = gt.items.containsKey(Tag.ReleaseDate) ? gt.items.get(Tag.ReleaseDate)[0] : "";

        return g;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        gd3 = getGD3Info(vgmBuf);

        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
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
        // The actual chip thread skips processing (for debugging)
        if (model == EnmModel.RealModel) return true;
//#endif

        if (mtype == MoonDriverFileType.MDL) return initMDL();
        else return initMDR();
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
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
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                moonDriverDriver.render();

                counter++;
                vgmFrameCounter++;
            }

            int lp = moonDriverDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            vgmCurLoop = lp;

            if (moonDriverDriver.getStatus() < 1) {
                if (moonDriverDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0)); // Wait for latency*2 until the actual voice is fully pronounced
                }
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public byte[] compile(byte[] vgmBuf) {
        if (moonDriverCompiler == null) moonDriverCompiler = ICompiler.factory("moonDriver.compiler.Compiler");
        moonDriverCompiler.init();
        moonDriverCompiler.setCompileSwitch("SRC");
        moonDriverCompiler.setCompileSwitch("MoonDriverOption=-i");
        moonDriverCompiler.setCompileSwitch("MoonDriverOption=%s".formatted(PlayingFileName));

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = moonDriverCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = moonDriverCompiler.getCompilerInfo();

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return null;
        if (!info.errorList.isEmpty()) {
            if (model == EnmModel.VirtualModel) {
//                JOptionPane.showMessageDialog(null, "Compile error");
                throw new IllegalStateException("Compile error");
            }
            return null;
        }

        List<Byte> dest = new ArrayList<>();
        for (MmlDatum md : ret) {
            dest.add(md != null ? (byte) (md.dat & 0xff) : (byte) 0);
        }

        return ByteUtil.toByteArray(dest);
    }

    public enum MoonDriverFileType {
        unknown,
        MDR,
        MDL
    }

    private MoonDriverFileType checkFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return MoonDriverFileType.unknown;
        }

        if (buf[0] == 'M'
                && buf[1] == 'D'
                && buf[2] == 'R'
                && buf[3] == 'V') {
            return MoonDriverFileType.MDR;
        }

        return MoonDriverFileType.MDL;
    }

    private boolean initMDL() {
        moonDriverCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = moonDriverCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = moonDriverCompiler.getCompilerInfo();

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return false;
        if (!info.errorList.isEmpty()) {
            if (model == EnmModel.VirtualModel) {
                JOptionPane.showMessageDialog(null, "Compile error");
            }
            return false;
        }

        if (moonDriverDriver == null) moonDriverDriver = IDriver.factory("moonDriver.driver.Driver");

        //boolean notSoundBoard2 = false;
        //boolean isLoadADPCM = true;
        //boolean loadADPCMOnly = false;

        ////mucomDriver.Init(playingFileName,chipWriteRegister,chipWaitSend,
        //         notSoundBoard2
        //       , isLoadADPCM
        //       , loadADPCMOnly
        //   );
        //List<ChipRunnable> lca = new ArrayList<ChipRunnable>();
        //mucomChipAction ca;
        //ca = new mucomChipAction(OPNA1Write, null, OPNAWaitSend); lca.add(ca);
        //ca = new mucomChipAction(OPNA2Write, null, null); lca.add(ca);
        //ca = new mucomChipAction(OPNB1Write, WriteOPNB1PCMData, null); lca.add(ca);
        //ca = new mucomChipAction(OPNB2Write, WriteOPNB2PCMData, null); lca.add(ca);
        //ca = new mucomChipAction(OPM1Write, null, null); lca.add(ca);
        //moonDriverDriver.Init(
        //    lca,
        //    ret
        //    , null
        //    , new Object[] {
        //          notSoundBoard2
        //        , isLoadADPCM
        //        , loadADPCMOnly
        //        , playingFileName
        //    });

        //moonDriverDriver.startRendering(Common.VGMProcSampleRate
        //    , new Tuple<String, int>[] { new Tuple<String, int>("", opnaBaseClock) });
        //moonDriverDriver.MusicSTART(0);

        return true;
    }

    private boolean initMDR() {
        if (moonDriverDriver == null) moonDriverDriver = IDriver.factory("moonDriver.driver.Driver");

        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : vgmBuf) buf.add(new MmlDatum(b));

        List<ChipAction> lca = new ArrayList<>();
        ChipAction ca = new MoonDriverChipAction(this::opl4Write, this::opl4WaitSend);
        lca.add(ca);
        moonDriverDriver.init(lca, buf.toArray(MmlDatum[]::new), this::appendFileReaderCallback
                , PlayingFileName, (double) 44100, 0);

        moonDriverDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YMF278B", 33868800));
        moonDriverDriver.startMusic(0);

        return true;
    }

    private Stream appendFileReaderCallback(String arg) {

        String fn = Path.combine(Path.getDirectoryName(PlayingFileName), arg);

        if (!File.exists(fn)) return null;

        FileStream strm;
        try {
            strm = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            strm = null;
        }

        return strm;
    }

//    public void writeRegister(ChipDatum dat) {
////logger.log(Level.TRACE, "FM p%d Out:Adr[%02x] val[%02x]".formatted((int) dat.address, (int) dat.data, dat.port)));
////logger.log(Level.TRACE, "FM p%d Out:Adr[%02x] val[%02x]".formatted((int) dat.address, (int) dat.data, dat.port)));
//        outDatum od = null;
//
//        if (pcmdata.size() > 0) {
//            plugin.audio.chipRegister.YMF278BSetRegister(od, count, 0, pcmdata.toArray());
//            pcmdata.clear();
//        }
//
//        if (dat.additionalData != null) {
//            if (dat.additionalData instanceof MmlDatum) {
//                MmlDatum md = (MmlDatum) dat.additionalData;
//                if (md.linePos != null) md.linePos.srcMMLID = filename;
//                od = new outDatum(md.type, md.args, md.linePos, (byte) md.dat);
//            }
//        }
//
////if (od != null && od.linePos != null) {
//// logger.log(Level.TRACE, "%d".formatted(od.linePos.col));
////}
//
////        plugin.audio.chipRegister.YM2608SetRegister(od, (long)dat.time, 0, dat.port, dat.address, dat.data);
//        plugin.audio.chipRegister.YMF278BSetRegister(od, count, 0, dat.port, dat.address, dat.data);
//    }

    private void opl4Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        plugin.audio.chipRegister.chip(YmF278BChip.class).setYMF278BRegister(0, cd.port, cd.address, cd.data, model);
    }

    private void opl4WaitSend(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
//            JOptionPane.showMessageDialog("elapsed:%d size:%d".formatted(elapsed, size));
//            int n = Math.max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
//            Thread.sleep(n);
        }

//        // Add additional weight based on size and elapsed time.
//        int m = Math.max((int)(size / 20 - elapsed), 0); // 20 Threshold (magic number)
//        Thread.sleep(m);
    }

    public static class MoonDriverChipAction implements ChipAction {
        private final Consumer<ChipDatum> opl4Write;
        private final BiConsumer<Long, Integer> opl4WaitSend;

        public MoonDriverChipAction(Consumer<ChipDatum> opl4Write, BiConsumer<Long, Integer> opl4WaitSend) {
            this.opl4Write = opl4Write;
            this.opl4WaitSend = opl4WaitSend;
        }

        @Override
        public String getChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void waitSend(long t1, int t2) {
            opl4WaitSend.accept(t1, t2);
        }

        @Override
        public void writePCMData(byte[] data, int startAddress, int endAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeRegister(ChipDatum cd) {
            opl4Write.accept(cd);
        }
    }
}
