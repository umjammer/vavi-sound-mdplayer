package mdplayer.driver.muap;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileShare;
import dotnet4j.io.FileStream;
import dotnet4j.io.IOException;
import dotnet4j.io.MemoryStream;
import dotnet4j.io.Path;
import dotnet4j.io.Stream;
import dotnet4j.util.compat.TriConsumer;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Cs4231Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.mucom.MucomDriver;
import mdplayer.plugin.BasePlugin;
import muap.driver.Ems.EMS_AllocMemory;
import muap.driver.Ems.EMS_GetHandleName;
import muap.driver.Ems.EMS_Map;
import muap.driver.Ems.EMS_SetHandleName;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;
import musicDriverInterface.MetaData;
import musicDriverInterface.MmlDatum;
import vavi.util.ByteUtil;


/**
 * @author kumatan
 */
public class MuapDriver extends BaseDriver {

    private static final Logger logger = System.getLogger(MuapDriver.class.getName());

    private ICompiler muapCompiler = null;
    private IDriver muapDriver = null;
    public byte[] toneBuff;
    public int[] labelAdr;

    public String playingFileName;

    public String getPlayingFileName() {
        return playingFileName;
    }

    public MuapDriver() {
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        // muap basically has no tag information
        // (There is data that uses the lyrics function to display the title.)
        MetaData md = new MetaData();
        return md;
    }

    @Override
    public void init(byte[] dataBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        metaData = getMetaData(dataBuf);

        this.dataBuf = dataBuf;
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
        // Skip processing for real chip thread (for debugging)
        //if (model == EnmModel.RealModel) return true;
//#endif

        initO();
    }

    @Override
    public void processOneFrame() {
//#if DEBUG
        // Skip processing for real chip thread (for debugging)
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

                muapDriver.render();

                counter++;
                frameCounter++;
            }

            int lp = muapDriver.getNowLoopCounter();
            lp = lp < 0 ? 0 : lp;
            curLoop = lp;

            if (muapDriver.getStatus() < 1) {
                //if (mucomDriver.GetStatus() == 0 && !Stopped) {
                //    Thread.Sleep((int)(setting.outputDevice.SampleRate/latency * 2.0)); // Wait for latency*2 until the actual sound is fully played
                //}
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public byte[] compile(byte[] vgmBuf) {
        if (muapCompiler == null) muapCompiler = ICompiler.factory("muap.compiler.Compiler");
        muapCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info = null;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = muapCompiler.compile(sourceMML, this::appendFileReaderCallback); // wrkMUCFullPath, disp);
            }

            info = muapCompiler.getCompilerInfo();

        } catch (Exception e) {
            ret = null;
            info = null;
        }

        if (ret == null && info == null) return null;
        if (info != null && !info.errorList.isEmpty()) {
            for (var error : info.errorList) {
                logger.log(Level.ERROR, error.getItem3());
            }
            if (model == EnmModel.VirtualModel) {
                logger.log(Level.ERROR, "Compile error");
            }
            return null;
        }
        if (ret == null) {
            return null;
        }

        List<Byte> dest = new ArrayList<>();
        for (MmlDatum md : ret) {
            dest.add(md != null ? (byte) md.dat : (byte) 0);
        }

        toneBuff = null;
        labelAdr = null;
        if (ret != null && ret.length > 0 && ret[0].args != null) {
            int i = 0;
            while (i < ret[0].args.size() && ret[0].args.get(i) != null && !(ret[0].args.get(i) instanceof byte[])) {
                i++;
            }

            if (ret[0].args.get(i) != null && ret[0].args.get(i) instanceof byte[]) {
                toneBuff = (byte[]) ret[0].args.get(i);
            }
            if (ret[0].args.get(i + 1) != null && ret[0].args.get(i + 1) instanceof int[]) {
                labelAdr = (int[]) ret[0].args.get(i + 1);
            }
        }

        return ByteUtil.toByteArray(dest);
    }

    private Stream appendFileReaderCallback(String arg) {

        String fn = Path.combine(Path.getDirectoryName(playingFileName), arg);

        if (!File.exists(fn)) return null;

        FileStream strm;
        try {
            strm = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            strm = null;
        }

        return strm;
    }

    private void initO() {
        if (muapDriver == null) muapDriver = IDriver.factory("muap.driver.Driver");

        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : dataBuf) buf.add(new MmlDatum(b & 0xff));

        List<ChipAction> lca = new ArrayList<>();
        MuapChipAction ca;
        ca = new MuapChipAction(this::OPNAWriteP, null, null);
        lca.add(ca);
        ca = new MuapChipAction(this::OPN2WriteP, null, null);
        lca.add(ca);
        ca = new MuapChipAction(this::CS4231Write, null, null);
        lca.add(ca);
        String pfn = plugin.playingFileName;
        String _ = plugin.playingArcFileName;
        if (pfn != null && !pfn.isEmpty()) {
            pfn = Path.getDirectoryName(Path.getFullPath(pfn));
        }
        muapDriver.init(
                lca,
                buf.toArray(MmlDatum[]::new),
                null,
                (Function<Byte, Byte>) this::CS4231Read,
                (Supplier<byte[]>) this::CS4231EMS_GetCurrentMapBuf,
                (EMS_Map) this::CS4231EMS_Map,
                (Supplier<Integer>) this::CS4231EMS_GetPageMap,
                (EMS_GetHandleName) this::CS4231EMS_GetHandleName,
                (EMS_SetHandleName) this::CS4231EMS_SetHandleName,
                (EMS_AllocMemory) this::CS4231EMS_AllocMemory,
                toneBuff,
                setting.getMuapJava().soundDeviceMode,
                labelAdr,
                pfn
        );

        muapDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("YM2608", MucomDriver.opnaBaseClock));
        muapDriver.startMusic(0);
        Object[] work = (Object[]) muapDriver.getWork();
        plugin.chipRegister.chip(Cs4231Chip.class).setFifoBuf(0, (byte[]) work[0]);
        //chipRegister.setCS4231Int0bEnt(0, (Action)work[1], model);
    }

    private static class MuapChipAction implements ChipAction {
        private final Consumer<ChipDatum> write;
        private final TriConsumer<byte[], Integer, Integer> writePCMData;
        private final BiConsumer<Long, Integer> sendWait;

        public MuapChipAction(Consumer<ChipDatum> write, TriConsumer<byte[], Integer, Integer> writePCMData, BiConsumer<Long, Integer> sendWait) {
            this.write = write;
            this.writePCMData = writePCMData;
            this.sendWait = sendWait;
        }

        @Override
        public String getChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void waitSend(long t1, int t2) {
            sendWait.accept(t1, t2);
        }

        @Override
        public void writePCMData(byte[] data, int startAddress, int endAddress) {
            writePCMData.accept(data, startAddress, endAddress);
        }

        @Override
        public void writeRegister(ChipDatum cd) {
            write.accept(cd);
        }
    }

    void OPNAWriteP(ChipDatum dat) {
        //logger.log(Level.Trace, "Write OPNA : Prt:%02x Adr:%02x Dat:%02x".formatted(dat.port, dat.address, dat.data));
        OPNAWrite(0, dat);
    }

    void OPN2WriteP(ChipDatum dat) {
        //if (dat.address > 0xff) {
        //    ;
        //}
        //if (dat.port > 0) {
        //    ;
        //}
        //logger.log(Level.Trace, "Write OPN2 : Prt:$%02x Adr:$%02x Dat:$%02x".formatted(dat.port, dat.address, dat.data));
        OPN2Write(0, dat);
    }

    void OPNAWrite(int chipId, ChipDatum dat) {
        if (dat != null && dat.additionalData != null) {
            MmlDatum md = (MmlDatum) dat.additionalData;
            if (md.linePos != null) {
                //logger.og(Level.TRACE, "! OPNA i%d r%d c%d".formatted(chipId, md.linePos.row, md.linePos.col));
            }
        }

        if (dat.port == -1) return;
        //logger.log(Level.TRACE, "Out ChipA:%d Port:%d Adr:[%02x] val[%02x]".formatted(chipId, dat.port, dat.address, dat.data));

        plugin.chipRegister.chip(Ym2608Chip.class).write(chipId, dat.port, dat.address, dat.data, model /*, frameCounter */);
    }

    void OPN2Write(int chipId, ChipDatum dat) {
        if (dat != null && dat.additionalData != null) {
            MmlDatum md = (MmlDatum) dat.additionalData;
            if (md.linePos != null) {
                //logger.log(Level.TRACE, "! OPNA i%d r%d c%d".formatted(chipId, md.linePos.row, md.linePos.col));
            }
        }

        if (dat.port == -1) return;
        //Debug.WriteLine(string.Format("Out ChipA:%d Port:%d Adr:[{%02x] val[%02x]", chipId, dat.port, dat.address, dat.data));

        plugin.chipRegister.chip(Ym2612Chip.class).write(chipId, dat.port, dat.address, dat.data, model, frameCounter);
    }

    void CS4231Write(ChipDatum dat) {
        if (dat != null && dat.additionalData != null) {
            MmlDatum md = (MmlDatum) dat.additionalData;
            if (md.linePos != null) {
                //logger.log(Level.TRACE, "! OPNA i%d r%d c%d".formatted(chipId, md.linePos.row, md.linePos.col));
            }
        }

        if (dat.port == -1) return;
        //logger.log(Level.TRACE, "Out ChipA:%d Port:%d} Adr:[%02x] val[%02x]".formatted(chipId, dat.port, dat.address, dat.data));

        plugin.chipRegister.chip(Cs4231Chip.class).write(0, dat.port, dat.address, dat.data);
    }

    byte CS4231Read(byte adr) {
        return (byte) plugin.chipRegister.chip(Cs4231Chip.class).read(0, adr & 0xff);
    }

    byte[] CS4231EMS_GetCurrentMapBuf() {
        return plugin.chipRegister.chip(Cs4231Chip.class).EMS_GetCurrentMapBuf(0, 0);
    }

    void CS4231EMS_Map(int al, byte[] ah, int bx, int dx) {
        plugin.chipRegister.chip(Cs4231Chip.class).EMS_Map(0, 0, al, ah, bx, dx);
    }

    int CS4231EMS_GetPageMap() {
        return plugin.chipRegister.chip(Cs4231Chip.class).EMS_GetPageMap(0, 0);
    }

    void CS4231EMS_GetHandleName(byte[] ah, int dx, String[] buf) {
        plugin.chipRegister.chip(Cs4231Chip.class).EMS_GetHandleName(0, ah, dx, buf);
    }

    void CS4231EMS_SetHandleName(byte[] ah, int dx, String emsName2) {
        plugin.chipRegister.chip(Cs4231Chip.class).EMS_SetHandleName(0, ah, dx, emsName2);
    }

    void CS4231EMS_AllocMemory(byte[] ah, int[] dx, int bx) {
        plugin.chipRegister.chip(Cs4231Chip.class).EMS_AllocMemory(0, ah, dx, bx);
    }

    public List<Tuple<String, String>> getTags() {
        if (plugin.chipRegister == null) return null;
        return muapDriver.getTags();
    }
}
