package mdplayer.driver.mucom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

import dotnet4j.util.compat.TriConsumer;
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
import mucom88.compiler.Compiler;
import mucom88.driver.Driver;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.CompilerInfo;
import musicDriverInterface.GD3Tag;
import musicDriverInterface.MmlDatum;
import musicDriverInterface.Tag;
import musicDriverInterface.ICompiler;
import musicDriverInterface.IDriver;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;


public class MucomDotNET extends BaseDriver {
    private ICompiler mucomCompiler = null;
    private IDriver mucomDriver = null;

    private String PlayingFileName;

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    public static final int opnaBaseClock = 7987200;
    public static final int opnbBaseClock = 8000000;
    public static final int opmBaseClock = 3579545;
    private MUCOMFileType mType;

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        mType = checkFileType(buf);
        GD3Tag tag;

        if (mType == MUCOMFileType.MUC) {
            mucomCompiler = new Compiler();
            tag = mucomCompiler.getGD3TagInfo(buf);
        } else {
            mucomDriver = new Driver();
            tag = mucomDriver.getGD3TagInfo(buf);
        }

        Vgm.Gd3 g = new Vgm.Gd3();
        g.trackName = tag.items.containsKey(Tag.Title) ? tag.items.get(Tag.Title)[0] : "";
        g.trackNameJ = tag.items.containsKey(Tag.TitleJ) ? tag.items.get(Tag.TitleJ)[0] : "";
        g.composer = tag.items.containsKey(Tag.Composer) ? tag.items.get(Tag.Composer)[0] : "";
        g.composerJ = tag.items.containsKey(Tag.ComposerJ) ? tag.items.get(Tag.ComposerJ)[0] : "";
        g.vgmBy = tag.items.containsKey(Tag.Artist) ? tag.items.get(Tag.Artist)[0] : "";
        g.converted = tag.items.containsKey(Tag.ReleaseDate) ? tag.items.get(Tag.ReleaseDate)[0] : "";

        return g;
    }

    public EnmChip[] useChipsFromMub(byte[] buf) {
        List<EnmChip> chips = new ArrayList<>();
        chips.add(EnmChip.YM2608);
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);

        // 標準的な mub ファイル
        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x43
                && buf[3] == 0x38) {
            return chips.toArray(EnmChip[]::new);
        }
        // 標準的な mub ファイル
        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x42
                && buf[3] == 0x38) {
            return chips.toArray(EnmChip[]::new);
        }
        // 拡張 mub ファイル？
        if (buf[0] != 'm'
                || buf[1] != 'u'
                || buf[2] != 'P'
                || buf[3] != 'b') {
            // 見知らぬファイル
            return null;
        }

        int chipsCount = buf[0x0009];
        int p = 0x0022;
        byte[] partCount = new byte[chipsCount];
        byte[][] pageCount = new byte[chipsCount][];
        Integer[][][] pageLength = new Integer[chipsCount][][];
        for (int i = 0; i < chipsCount; i++) {
            partCount[i] = buf[p + 0x16];
            int instCount = buf[p + 0x17];
            p += 2 * instCount + 0x18;
            int pcmCount = buf[p];
            p += 2 * pcmCount + 1;
        }

        for (int i = 0; i < chipsCount; i++) {
            pageCount[i] = new byte[partCount[i]];
            pageLength[i] = new Integer[partCount[i]][];
            for (int j = 0; j < partCount[i]; j++) {
                pageCount[i][j] = buf[p++];
            }
        }

        for (int i = 0; i < chipsCount; i++) {
            for (int j = 0; j < partCount[i]; j++) {
                pageLength[i][j] = new Integer[pageCount[i][j]];
                for (int k = 0; k < pageCount[i][j]; k++) {
                    pageLength[i][j][k] = Common.getLE32(buf, p);
                    p += 8;
                }
            }
        }

        chips.clear();
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);
        chips.add(EnmChip.Unuse);

        if (chipsCount > 0) {
            if (partCount[0] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[0]; i++) {
                    n += pageCount[0][i];
                }
                if (n > 0) chips.set(0, EnmChip.YM2608);
            }
        }

        if (chipsCount > 1) {
            if (partCount[1] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[1]; i++) {
                    n += pageCount[1][i];
                }
                if (n > 0) chips.set(1, EnmChip.S_YM2608);
            }
        }

        if (chipsCount > 2) {
            if (partCount[2] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[2]; i++) {
                    n += pageCount[2][i];
                }
                if (n > 0) chips.set(2, EnmChip.YM2610);
            }
        }

        if (chipsCount > 3) {
            if (partCount[3] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[3]; i++) {
                    n += pageCount[3][i];
                }
                if (n > 0) chips.set(3, EnmChip.S_YM2610);
            }
        }

        if (chipsCount > 4) {
            if (partCount[4] > 0) {
                int n = 0;
                for (int i = 0; i < partCount[4]; i++) {
                    n += pageCount[4][i];
                }
                if (n > 0) chips.set(4, EnmChip.YM2151);
            }
        }

        return chips.toArray(EnmChip[]::new);
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        gd3 = getGD3Info(vgmBuf);

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
        //実チップスレッドは処理をスキップ(デバッグ向け)
        //if (model == EnmModel.RealModel) return true;
//#endif

        if (mType == MUCOMFileType.MUC) return initMUC();
        else return initMUB();
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    @Override
    public void processOneFrame() {

//#if DEBUG
//        // 実チップスレッドは処理をスキップ(デバッグ向け)
//        if (model == EnmModel.RealModel) {
//            Stopped = true;
//            return;
//        }
//#endif

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                mucomDriver.render();

                counter++;
                vgmFrameCounter++;
            }

            int lp = mucomDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            vgmCurLoop = lp;

            if (mucomDriver.getStatus() < 1) {
                if (mucomDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0));//実際の音声が発音しきるまでlatency*2の分だけ待つ
                }
                stopped = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum MUCOMFileType {
        unknown,
        MUB,
        MUC
    }

    public byte[] compile(byte[] vgmBuf) {
        if (mucomCompiler == null) mucomCompiler = new mucom88.compiler.Compiler();
        mucomCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = mucomCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = mucomCompiler.getCompilerInfo();

        } catch (Exception e) {
            e.printStackTrace();
            ret = null;
            info = null;
        }

        if (ret == null || info == null) return null;
        if (info.errorList.size() > 0) {
            if (model == EnmModel.VirtualModel) {
                JOptionPane.showMessageDialog(null, "Compile error");
            }
            return null;
        }

        List<Byte> dest = new ArrayList<>();
        for (MmlDatum md : ret) {
            dest.add(md != null ? (byte) (md.dat & 0xff) : (byte) 0);
        }

        return toByteArray(dest);
    }

    private MUCOMFileType checkFileType(byte[] buf) {
        if (buf == null || buf.length < 4) {
            return MUCOMFileType.unknown;
        }

        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x43
                && buf[3] == 0x38) {
            return MUCOMFileType.MUB;
        }
        if (buf[0] == 0x4d
                && buf[1] == 0x55
                && buf[2] == 0x42
                && buf[3] == 0x38) {
            return MUCOMFileType.MUB;
        }
        if (buf[0] == 'm'
                && buf[1] == 'u'
                && buf[2] == 'P'
                && buf[3] == 'b') {
            return MUCOMFileType.MUB;
        }

        return MUCOMFileType.MUC;
    }

    private boolean initMUC() {
        mucomCompiler.init();

        MmlDatum[] ret;
        CompilerInfo info;
        try {
            try (MemoryStream sourceMML = new MemoryStream(vgmBuf)) {
                ret = mucomCompiler.compile(sourceMML, this::appendFileReaderCallback);
            }

            info = mucomCompiler.getCompilerInfo();

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

        if (mucomDriver == null) mucomDriver = new mucom88.driver.Driver();

        boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;

        List<ChipAction> actions = new ArrayList<>();
        mucomChipAction action;
        action = new mucomChipAction(this::writeOPNA1, null, this::sendOPNAWait);
        actions.add(action);
        action = new mucomChipAction(this::writeOPNA2, null, null);
        actions.add(action);
        action = new mucomChipAction(this::writeOPNB1, this::writeOPNB1PCMData, null);
        actions.add(action);
        action = new mucomChipAction(this::writeOPNB2, this::writeOPNB2PCMData, null);
        actions.add(action);
        action = new mucomChipAction(this::writeOPM1, null, null);
        actions.add(action);
        mucomDriver.init(actions, ret, null,
                notSoundBoard2, isLoadADPCM, loadADPCMOnly, PlayingFileName);

        mucomDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("", opnaBaseClock));
        mucomDriver.startMusic(0);

        return true;
    }

    private boolean initMUB() {
        if (mucomDriver == null) mucomDriver = new mucom88.driver.Driver();

        boolean notSoundBoard2 = false;
        boolean isLoadADPCM = true;
        boolean loadADPCMOnly = false;
        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : vgmBuf) buf.add(new MmlDatum(b & 0xff));

        List<ChipAction> actions = new ArrayList<>();
        mucomChipAction action;
        action = new mucomChipAction(this::writeOPNA1, null, this::sendOPNAWait);
        actions.add(action);
        action = new mucomChipAction(this::writeOPNA2, null, null);
        actions.add(action);
        action = new mucomChipAction(this::writeOPNB1, this::writeOPNB1PCMData, null);
        actions.add(action);
        action = new mucomChipAction(this::writeOPNB2, this::writeOPNB2PCMData, null);
        actions.add(action);
        action = new mucomChipAction(this::writeOPM1, null, null);
        actions.add(action);
        mucomDriver.init(actions, buf.toArray(MmlDatum[]::new),null,
                notSoundBoard2, isLoadADPCM, loadADPCMOnly, PlayingFileName);

        mucomDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("", opnaBaseClock));
        mucomDriver.startMusic(0);

        return true;
    }

    private void writeOPNA1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2608Register(0, cd.port, cd.address, cd.data, model);
    }

    private void writeOPNA2(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2608Register(1, cd.port, cd.address, cd.data, model);
    }

    private void writeOPNB1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2610Register(0, cd.port, cd.address, cd.data, model);
    }

    private void writeOPNB2(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;
        if (cd.port == -1) return;

        chipRegister.setYM2610Register(1, cd.port, cd.address, cd.data, model);
    }

    private void writeOPM1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;

        chipRegister.setYM2151Register(0, cd.port, cd.address, cd.data, model, 0, 0);
    }

    private void writeOPNB1PCMData(byte[] dat, int v, int v2) {
        if (v == 0)
            chipRegister.writeYm2610_SetAdpcmA(0, dat, EnmModel.VirtualModel);
        else
            chipRegister.WriteYM2610_SetAdpcmB(0, dat, EnmModel.VirtualModel);
    }

    private void writeOPNB2PCMData(byte[] dat, int v, int v2) {
        if (v == 0)
            chipRegister.writeYm2610_SetAdpcmA(1, dat, EnmModel.VirtualModel);
        else
            chipRegister.WriteYM2610_SetAdpcmB(1, dat, EnmModel.VirtualModel);
    }

    private void sendOPNAWait(long size, int elapsed) {
        if (model == EnmModel.VirtualModel) {
            //JOptionPane.showMessageDialog(String.format("elapsed:%d size:%d", elapsed, size));
            //int n = Math.max((int)(size / 20 - elapsed), 0); // 20: threshold (magic number)
            //Thread.sleep(n);
            return;
        }

        // サイズと経過時間から、追加でウエイトする。
        int m = Math.max((int) (size / 20 - elapsed), 0); // 20: threshold (magic number)
        try { Thread.sleep(m); } catch (InterruptedException e) {}
    }

    public static class mucomChipAction implements ChipAction {
        private Consumer<ChipDatum> write;
        private TriConsumer<byte[], Integer, Integer> writePCMData;
        private BiConsumer<Long, Integer> sendWait;

        public mucomChipAction(Consumer<ChipDatum> write, TriConsumer<byte[], Integer, Integer> writePCMData, BiConsumer<Long, Integer> sendWait) {
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

    //private void chipWaitSend(long elapsed, int size) {
    //    if (model == EnmModel.VirtualModel) {
    //        //JOptionPane.showMessageDialog(String.format("elapsed:%d size:%d", elapsed, size));
    //        //int n = Math.max((int)(size / 20 - elapsed), 0); // 20: threshold (magic number)
    //        //Thread.sleep(n);
    //        return;
    //    }

    //    //サイズと経過時間から、追加でウエイトする。
    //    int m = Math.max((int)(size / 20 - elapsed), 0); // 20: threshold (magic number)
    //    Thread.sleep(m);
    //}

    //private void chipWriteRegister(ChipDatum dat) {
    //    if (dat == null) return;
    //    if (dat.address == -1) return;
    //    if (dat.data == -1) return;
    //    if (dat.port == -1) return;

    //    chipRegister.setYM2608Register(0, dat.port, dat.address, dat.data, model);
    //    //Debug.println("%d %d", dat.address, dat.data);
    //}

    private Stream appendFileReaderCallback(String arg) {

        String fn = Path.combine(Path.getDirectoryName(PlayingFileName), arg);

        if (!File.exists(fn)) return null;

        FileStream stream;
        try {
            stream = new FileStream(fn, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (IOException e) {
            e.printStackTrace();
            stream = null;
        }

        return stream;
    }
}
