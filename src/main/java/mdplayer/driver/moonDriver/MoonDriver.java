package mdplayer.driver.moonDriver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.YmF262Chip;
import mdplayer.chips.YmF278BChip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;
import musicDriverInterface.MetaData;
import musicDriverInterface.MmlDatum;
import vavi.util.ByteUtil;
import vavi.util.compat.Tuple;

import static java.lang.System.getLogger;


/**
 * @author kumatan
 */
public class MoonDriver extends BaseDriver {

    private static final Logger logger = getLogger(MoonDriver.class.getName());

    private ICompiler moonDriverCompiler = null;
    private IDriver moonDriverDriver = null;
    private MoonDriverFileType mtype;

    public MoonDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public MoonDriver() {
        this(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        mtype = checkFileType(buf);
logger.log(Level.DEBUG, "type: " + mtype);
        MetaData metaData;

        if (mtype == MoonDriverFileType.MDL) {
            moonDriverCompiler = ICompiler.factory("moonDriver.compiler.Compiler");
            metaData = moonDriverCompiler.getMetaData(buf);
        } else {
            moonDriverDriver = IDriver.factory("moonDriver.driver.Driver");
            metaData = moonDriverDriver.getMetaData(buf);
        }

        return metaData;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        // The plugin compiles the MDL source into an MDR binary (into its own dataBuf)
        // in initChips(), AFTER this driver was constructed. The constructor only took a
        // snapshot of the still-raw MDL, so re-read the plugin's now-compiled data here;
        // otherwise getMetaData() sees the MDL header and init routes to the dead initMDL().
        if (plugin != null) dataBuf = plugin.getData();

        metaData = getMetaData(dataBuf, 0);

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
//        // The actual chip thread skips processing (for debugging)
//        if (model == EnmModel.RealModel) return true;
//#endif

        if (mtype == MoonDriverFileType.MDL) initMDL();
        else initMDR();
    }

    @Override
    public void processOneFrame() {
//#if DEBUG
//        // The actual chip thread skips processing (for debugging)
//        if (model == EnmModel.RealModel) {
//            stopped = true;
//            return;
//        }
//#endif
        if (stopped) return;

        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                moonDriverDriver.render();

                counter++;
                frameCounter++;
            }

            int lp = moonDriverDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            curLoop = lp;

            if (moonDriverDriver.getStatus() < 1) {
//                if (moonDriverDriver.getStatus() == 0) {
//                    Thread.sleep((int) (latency * 2.0)); // Wait for latency*2 until the actual voice is fully pronounced
//                }
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public byte[] compile(byte[] vgmBuf) {
        if (moonDriverCompiler == null) moonDriverCompiler = ICompiler.factory("moonDriver.compiler.Compiler");
        moonDriverCompiler.init();
        // no "SRC": we only consume the returned binary, and it makes the compiler
        // dump intermediate effect.h/define.inc next to the mml.
        moonDriverCompiler.setCompileSwitch("MoonDriverOption=-i");
        moonDriverCompiler.setCompileSwitch("MoonDriverOption=%s".formatted(plugin.playingFileName));

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (InputStream sourceMML = new ByteArrayInputStream(vgmBuf)) {
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
                logger.log(Level.ERROR, "Compile error");
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

    private static MoonDriverFileType checkFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return MoonDriverFileType.unknown;
        }

        if (buf[0] == 'M' &&
                buf[1] == 'D' &&
                buf[2] == 'R' &&
                buf[3] == 'V') {
            return MoonDriverFileType.MDR;
        }

        return MoonDriverFileType.MDL;
    }

    private void initMDL() {
        moonDriverCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (InputStream sourceMML = new ByteArrayInputStream(dataBuf)) {
                ret = moonDriverCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = moonDriverCompiler.getCompilerInfo();

        } catch (Exception e) {
            throw new IllegalStateException("error in compiling", e);
        }

        if (!info.errorList.isEmpty()) {
//            if (model == EnmModel.VirtualModel) {
//                JOptionPane.showMessageDialog(null, "Compile error");
//            }
            throw new IllegalArgumentException("Compile error " + info.errorList);
        }

        if (moonDriverDriver == null) moonDriverDriver = IDriver.factory("moonDriver.driver.Driver");

        //boolean notSoundBoard2 = false;
        //boolean isLoadADPCM = true;
        //boolean loadADPCMOnly = false;

        ////mucomDriver.init(playingFileName,chipWriteRegister,chipWaitSend,
        //         notSoundBoard2,
        //         isLoadADPCM,
        //         loadADPCMOnly
        //   );
        //List<ChipRunnable> lca = new ArrayList<ChipRunnable>();
        //mucomChipAction ca;
        //ca = new mucomChipAction(OPNA1Write, null, OPNAWaitSend); lca.add(ca);
        //ca = new mucomChipAction(OPNA2Write, null, null); lca.add(ca);
        //ca = new mucomChipAction(OPNB1Write, WriteOPNB1PCMData, null); lca.add(ca);
        //ca = new mucomChipAction(OPNB2Write, WriteOPNB2PCMData, null); lca.add(ca);
        //ca = new mucomChipAction(OPM1Write, null, null); lca.add(ca);
        //moonDriverDriver.init(
        //    lca,
        //    ret,
        //    null,
        //    new Object[] {
        //          notSoundBoard2
        //        , isLoadADPCM
        //        , loadADPCMOnly
        //        , playingFileName
        //    });

        //moonDriverDriver.startRendering(Common.VGMProcSampleRate
        //    , new Tuple<String, int>[] { new Tuple<String, int>("", opnaBaseClock) });
        //moonDriverDriver.MusicSTART(0);
    }

    private void initMDR() {
        if (moonDriverDriver == null) moonDriverDriver = IDriver.factory("moonDriver.driver.Driver");

        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : dataBuf) buf.add(new MmlDatum(b & 0xff));

        List<ChipAction> lca = new ArrayList<>();
        ChipAction ca;
logger.log(Level.INFO, "useChip: " + plugin.getChips().stream().map(Class::getSimpleName).toList());
        if (plugin.contains(YmF278BChip.class)) {
            ca = new MoonDriverChipAction(this::opl4Write, this::opl4WaitSend);
        } else {
            ca = new MoonDriverChipAction(this::opl3Write, this::opl3WaitSend);
        }
        lca.add(ca);

        moonDriverDriver.init(
                lca,
                buf.toArray(MmlDatum[]::new),
                this::appendFileReaderCallback,
                plugin.playingFileName, (double) 44100, 0);

        moonDriverDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YMF278B", 33868800));
        moonDriverDriver.startMusic(0);
    }

    private InputStream appendFileReaderCallback(String arg) {

        Path fn = Path.of(plugin.playingFileName).getParent().resolve(arg);

        if (!Files.exists(fn)) return null;

        InputStream strm;
        try {
            strm = Files.newInputStream(fn);
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

        plugin.chipRegister.chip(YmF278BChip.class).write(0, cd.port, cd.address, cd.data, model);
    }

    private void opl4WaitSend(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
//            JOptionPane.showMessageDialog("elapsed:%d size:%d".formatted(elapsed, size));
//            int n = Math.max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
//            Thread.sleep(n);
        } else {
//            // Add additional weight based on size and elapsed time.
//            int m = Math.max((int)(size / 20 - elapsed), 0); // 20 Threshold (magic number)
//            Thread.sleep(m);
        }
    }

    private void opl3Write(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;
        if (cd.port > 1) return;

        plugin.chipRegister.chip(YmF262Chip.class).write(0, cd.port, cd.address, cd.data, model);
    }

    private void opl3WaitSend(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
//            JOptionPane.showMessageDialog("elapsed:%d size:%d".formatted(elapsed, size));
//            int n = Math.Max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
//            Thread.Sleep(n);
        } else {
//            // Add additional weight based on size and elapsed time.
//            int m = Math.Max((int) (size / 20 - elapsed), 0); // 20 Threshold (magic number)
//            Thread.Sleep(m);
        }
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
