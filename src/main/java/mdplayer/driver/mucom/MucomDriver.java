package mdplayer.driver.mucom;

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
import dotnet4j.util.compat.TriConsumer;
import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.MetaData;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;
import musicDriverInterface.MmlDatum;
import vavi.util.ByteUtil;
import vavi.util.StringUtil;

import static java.lang.System.getLogger;


/**
 * @author kumatan
 */
public class MucomDriver extends BaseDriver {

    private static final Logger logger = getLogger(MucomDriver.class.getName());

    private ICompiler mucomCompiler = null;
    private IDriver mucomDriver = null;

    public static final int opnaBaseClock = 7987200;
    public static final int opnbBaseClock = 8000000;
    public static final int opmBaseClock = 3579545;
    private MUCOMFileType mType;

    public MucomDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public MucomDriver() {
        super(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        mType = checkFileType(buf);
        MetaData metaData;

        if (mType == MUCOMFileType.MUC) {
            mucomCompiler = ICompiler.factory("mucom88.compiler.Compiler");
            metaData = mucomCompiler.getMetaData(buf);
        } else {
            mucomDriver = IDriver.factory("mucom88.driver.Driver");
            metaData = mucomDriver.getMetaData(buf);
        }

        return metaData;
    }

    public static Class<? extends Chip>[] useChipsFromMub(byte[] buf) {
        List<Class<? extends Chip>> chips = new ArrayList<>();
        chips.add(Ym2608Chip.class);
        chips.add(Unused.class);
        chips.add(Unused.class);
        chips.add(Unused.class);
        chips.add(Unused.class);

        // Standard mub files
        if (buf[0] == 0x4d &&
                buf[1] == 0x55 &&
                buf[2] == 0x43 &&
                buf[3] == 0x38) {
            return chips.toArray(Class[]::new);
        }
        // Standard mub files
        if (buf[0] == 0x4d &&
                buf[1] == 0x55 &&
                buf[2] == 0x42 &&
                buf[3] == 0x38) {
            return chips.toArray(Class[]::new);
        }
        // Extended mub file?
        if (buf[0] != 'm' ||
                buf[1] != 'u' ||
                buf[2] != 'P' ||
                buf[3] != 'b') {
            // Unknown files
            throw new IllegalArgumentException("Extended mub file?\n" + StringUtil.getDump(buf, 4));
        }

        int chipsCount = buf[0x0009];
        int p = 0x0022;
        byte[] partCount = new byte[chipsCount];
        byte[][] pageCount = new byte[chipsCount][];
        int[][][] pageLength = new int[chipsCount][][];
        for (int i = 0; i < chipsCount; i++) {
            partCount[i] = buf[p + 0x16];
            int instCount = buf[p + 0x17];
            p += 2 * instCount + 0x18;
            int pcmCount = buf[p];
            p += 2 * pcmCount + 1;
        }

        for (int i = 0; i < chipsCount; i++) {
            pageCount[i] = new byte[partCount[i]];
            pageLength[i] = new int[partCount[i]][];
            for (int j = 0; j < partCount[i]; j++) {
                pageCount[i][j] = buf[p++];
            }
        }

        for (int i = 0; i < chipsCount; i++) {
            for (int j = 0; j < partCount[i]; j++) {
                pageLength[i][j] = new int[pageCount[i][j]];
                for (int k = 0; k < pageCount[i][j]; k++) {
                    pageLength[i][j][k] = ByteUtil.readLeInt(buf, p);
                    p += 8;
                }
            }
        }

        chips.clear();
        chips.add(Unused.class);
        chips.add(Unused.class);
        chips.add(Unused.class);
        chips.add(Unused.class);
        chips.add(Unused.class);

        if (chipsCount > 0) {
            if (partCount[0] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[0]; i++) {
                    n += pageCount[0][i];
                }
                if (n > 0) chips.set(0, Ym2608Chip.class);
            }
        }

        if (chipsCount > 1) {
            if (partCount[1] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[1]; i++) {
                    n += pageCount[1][i];
                }
                if (n > 0) chips.set(1, Ym2608Chip.class);
            }
        }

        if (chipsCount > 2) {
            if (partCount[2] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[2]; i++) {
                    n += pageCount[2][i];
                }
                if (n > 0) chips.set(2, Ym2610Chip.class);
            }
        }

        if (chipsCount > 3) {
            if (partCount[3] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[3]; i++) {
                    n += pageCount[3][i];
                }
                if (n > 0) chips.set(3, Ym2610Chip.class);
            }
        }

        if (chipsCount > 4) {
            if (partCount[4] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[4]; i++) {
                    n += pageCount[4][i];
                }
                if (n > 0) chips.set(4, Ym2151Chip.class);
            }
        }

        return chips.toArray(Class[]::new);
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        metaData = getMetaData(dataBuf);

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
        //if (model == EnmModel.RealModel) return true;
//#endif

        if (mType == MUCOMFileType.MUC) initMUC();
        else initMUB();
    }

    @Override
    public void processOneFrame() {

//#if DEBUG
//        // The actual chip thread skips processing (for debugging)
//        if (model == EnmModel.RealModel) {
//            Stopped = true;
//            return;
//        }
//#endif

        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                mucomDriver.render();

                counter++;
                frameCounter++;
            }

            int lp = mucomDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            curLoop = lp;

            if (mucomDriver.getStatus() < 1) {
                if (mucomDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0)); // Wait for latency*2 until the actual voice is fully pronounced
                }
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public enum MUCOMFileType {
        unknown,
        MUB,
        MUC
    }

    public byte[] compile(byte[] vgmBuf) {
        if (mucomCompiler == null) mucomCompiler = ICompiler.factory("mucom88.compiler.Compiler");
        mucomCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = mucomCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = mucomCompiler.getCompilerInfo();

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return null;
        if (!info.errorList.isEmpty()) {
            if (model == EnmModel.VirtualModel) {
                JOptionPane.showMessageDialog(null, "Compile error");
            }
            return null;
        }

        List<Byte> dest = new ArrayList<>();
        for (MmlDatum md : ret) {
            dest.add(md != null ? (byte) (md.dat & 0xff) : (byte) 0);
        }

        return ByteUtil.toByteArray(dest);
    }

    private static MUCOMFileType checkFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return MUCOMFileType.unknown;
        }

        if (buf[0] == 0x4d &&
                buf[1] == 0x55 &&
                buf[2] == 0x43 &&
                buf[3] == 0x38) {
            return MUCOMFileType.MUB;
        }
        if (buf[0] == 0x4d &&
                buf[1] == 0x55 &&
                buf[2] == 0x42 &&
                buf[3] == 0x38) {
            return MUCOMFileType.MUB;
        }
        if (buf[0] == 'm' &&
                buf[1] == 'u' &&
                buf[2] == 'P' &&
                buf[3] == 'b') {
            return MUCOMFileType.MUB;
        }

        return MUCOMFileType.MUC;
    }

    private boolean initMUC() {
        mucomCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (MemoryStream sourceMML = new MemoryStream(dataBuf)) {
                ret = mucomCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = mucomCompiler.getCompilerInfo();

        } catch (Exception e) {
            throw new IllegalStateException("error in compiling", e);
        }

        if (!info.errorList.isEmpty()) {
//            if (model == EnmModel.VirtualModel) {
//                JOptionPane.showMessageDialog(null, "Compile error");
//            }
            throw new IllegalArgumentException("Compile error: " + info.errorList);
        }

        if (mucomDriver == null) mucomDriver = IDriver.factory("mucom88.driver.Driver");

        boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;

        List<ChipAction> actions = new ArrayList<>();
        MucomChipAction action;
        action = new MucomChipAction(this::writeOPNA1, null, this::sendOPNAWait);
        actions.add(action);
        action = new MucomChipAction(this::writeOPNA2, null, null);
        actions.add(action);
        action = new MucomChipAction(this::writeOPNB1, this::writeOPNB1PCMData, null);
        actions.add(action);
        action = new MucomChipAction(this::writeOPNB2, this::writeOPNB2PCMData, null);
        actions.add(action);
        action = new MucomChipAction(this::writeOPM1, null, null);
        actions.add(action);
        mucomDriver.init(actions, ret, null,
                notSoundBoard2, isLoadADPCM, loadADPCMOnly, plugin.playingFileName);

        mucomDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("", opnaBaseClock));
        mucomDriver.startMusic(0);

        return true;
    }

    private void initMUB() {
        if (mucomDriver == null) mucomDriver = new mucom88.driver.Driver();

        boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;
        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : dataBuf) buf.add(new MmlDatum(b & 0xff));

        List<ChipAction> actions = new ArrayList<>();
        MucomChipAction action;
        action = new MucomChipAction(this::writeOPNA1, null, this::sendOPNAWait);
        actions.add(action);
        action = new MucomChipAction(this::writeOPNA2, null, null);
        actions.add(action);
        action = new MucomChipAction(this::writeOPNB1, this::writeOPNB1PCMData, null);
        actions.add(action);
        action = new MucomChipAction(this::writeOPNB2, this::writeOPNB2PCMData, null);
        actions.add(action);
        action = new MucomChipAction(this::writeOPM1, null, null);
        actions.add(action);
        mucomDriver.init(actions, buf.toArray(MmlDatum[]::new),null,
                notSoundBoard2, isLoadADPCM, loadADPCMOnly, plugin.playingFileName);

        mucomDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("", opnaBaseClock));
        mucomDriver.startMusic(0);
    }

    private void writeOPNA1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        plugin.chipRegister.chip(Ym2608Chip.class).write(0, cd.port, cd.address, cd.data, model);
    }

    private void writeOPNA2(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        plugin.chipRegister.chip(Ym2608Chip.class).write(1, cd.port, cd.address, cd.data, model);
    }

    private void writeOPNB1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        plugin.chipRegister.chip(Ym2610Chip.class).write(0, cd.port, cd.address, cd.data, model);
    }

    private void writeOPNB2(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        plugin.chipRegister.chip(Ym2610Chip.class).write(1, cd.port, cd.address, cd.data, model);
    }

    private void writeOPM1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;

        plugin.chipRegister.chip(Ym2151Chip.class).write(0, cd.port, cd.address, cd.data, model, 0, 0);
    }

    private void writeOPNB1PCMData(byte[] dat, int v, int v2) {
        if (v == 0)
            plugin.chipRegister.chip(Ym2610Chip.class).writeAdpcmA(0, dat, EnmModel.VirtualModel);
        else
            plugin.chipRegister.chip(Ym2610Chip.class).writeAdpcmB(0, dat, EnmModel.VirtualModel);
    }

    private void writeOPNB2PCMData(byte[] dat, int v, int v2) {
        if (v == 0)
            plugin.chipRegister.chip(Ym2610Chip.class).writeAdpcmA(1, dat, EnmModel.VirtualModel);
        else
            plugin.chipRegister.chip(Ym2610Chip.class).writeAdpcmB(1, dat, EnmModel.VirtualModel);
    }

    private void sendOPNAWait(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
//            JOptionPane.showMessageDialog("elapsed:%d size:%d".formatted(elapsed, size));
//            int n = Math.max((int) (size / 20 - elapsed), 0); // 20: threshold (magic number)
//            Thread.sleep(n);
            return;
        }

        // Add additional weight based on size and elapsed time.
        int m = Math.max((int) (size / 20 - elapsed), 0); // 20: threshold (magic number)
        try { Thread.sleep(m); } catch (InterruptedException _) {}
    }

    private static class MucomChipAction implements ChipAction {
        private final Consumer<ChipDatum> write;
        private final TriConsumer<byte[], Integer, Integer> writePCMData;
        private final BiConsumer<Long, Integer> sendWait;

        public MucomChipAction(Consumer<ChipDatum> write, TriConsumer<byte[], Integer, Integer> writePCMData, BiConsumer<Long, Integer> sendWait) {
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

//    private void chipWaitSend(long elapsed, int size) {
//        if (model == EnmModel.VirtualModel) {
//            //JOptionPane.showMessageDialog("elapsed:%d size:%d".formatted(elapsed, size));
//            //int n = Math.max((int)(size / 20 - elapsed), 0); // 20: threshold (magic number)
//            //Thread.sleep(n);
//            return;
//        }
//
//        // Add additional weight based on size and elapsed time.
//        int m = Math.max((int) (size / 20 - elapsed), 0); // 20: threshold (magic number)
//        Thread.sleep(m);
//    }

//    private void chipWriteRegister(ChipDatum dat) {
//        if (dat == null) return;
//        if (dat.address == -1) return;
//        if (dat.data == -1) return;
//        if (dat.port == -1) return;
//
//        plugin.audio.chipRegister.setYM2608Register(0, dat.port, dat.address, dat.data, model);
//        //logger.log(Level.TRACE, "%d %d".formatted(dat.address, dat.data));
//    }

    private Stream appendFileReaderCallback(String arg) {

        String fn = Path.combine(Path.getDirectoryName(plugin.playingFileName), arg);

        if (!File.exists(fn)) return null;

        FileStream stream;
        try {
            stream = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            stream = null;
        }

        return stream;
    }
}
