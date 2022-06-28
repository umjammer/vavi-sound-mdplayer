package mdplayer.driver.zgm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Log;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.zgm.zgmChip.ChipFactory;
import mdplayer.driver.zgm.zgmChip.ZgmChip;
import mdplayer.driver.Vgm.Gd3;


public class Zgm extends BaseDriver {

    public static final int FCC_ZGM = 0x204D475A;    // "ZGM "
    public static final int FCC_GD3 = 0x20336447;  // "Gd3 "
    public static final int FCC_DEF = 0x666544;  // "Def"
    public static final int FCC_TRK = 0x6b7254;  // "Trk"

    private int vgmEof;
    private long vgmLoopOffset = 0;
    private int chipCommandSize = 1;
    private long vgmDataOffset = 0;

    public interface RefRunnable<T1, T2> extends BiConsumer<T1, T2> {
    }

    private Map<Integer, RefRunnable<Byte, Integer>> vgmCmdTbl = new HashMap<>();

    @Override
    public Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        getZGMGD3Info(buf);
        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
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
        vgmSpeedCounter = 0;

        return getZGMInfo(vgmBuf);
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    @Override
    public void oneFrameProc() {
        throw new UnsupportedOperationException();
    }

    private boolean getZGMGD3Info(byte[] buf) {
        if (buf == null) return false;

        int vgmGd3 = Common.getLE32(buf, (byte) 0x18);
        if (vgmGd3 == 0) return false;
        int vgmGd3Id = Common.getLE32(buf, vgmGd3);
        if (vgmGd3Id != FCC_GD3) throw new IndexOutOfBoundsException();

        vgmEof = Common.getLE32(vgmBuf, (byte) 0x04);

        int version = Common.getLE32(vgmBuf, 0x08);
        //バージョンチェック
        if (version < 10) return false;
        this.version = String.format("%d.%d%d", (version & 0xf00) / 0x100, (version & 0xf0) / 0x10, (version & 0xf));

        totalCounter = Common.getLE32(vgmBuf, 0x0c);
        if (totalCounter < 0) return false;
        vgmLoopOffset = Common.getLE32(vgmBuf, 0x14);
        loopCounter = Common.getLE32(vgmBuf, 0x10);

        int defineAddress = Common.getLE32(vgmBuf, 0x1c);
        int defineCount = Common.getLE16(vgmBuf, 0x24);
        //音源定義数チェック
        if (defineCount < 1) return false;

        chipCommandSize = (defineCount > 128) ? 2 : 1;

        int trackAddress = Common.getLE32(vgmBuf, 0x20);
        int trackCounter = Common.getLE16(vgmBuf, 0x26);
        vgmDataOffset = trackAddress + 11;
        //トラック数チェック
        if (trackCounter != 1) return false;
        int fcc = Common.getLE24(vgmBuf, trackAddress);
        if (fcc != FCC_TRK) return false;
        int trackLength = Common.getLE32(vgmBuf, trackAddress + 3);
        vgmLoopOffset = Common.getLE32(vgmBuf, trackAddress + 7);
        if (vgmLoopOffset != 0) loopCounter = 1;
        vgmEof = trackAddress + trackLength;

        int pos = defineAddress;

        Map<String, Integer> chipCount = new HashMap<>();
        for (int i = 0; i < defineCount; i++) {
            fcc = Common.getLE24(vgmBuf, pos);
            if (fcc != FCC_DEF) return false;
            ZgmChip chip = (new ChipFactory()).Create(Common.getLE32(vgmBuf, pos + 0x4), chipRegister, setting, vgmBuf);
            if (chip == null) return false;//non support

            if (!chipCount.containsKey(chip.name)) chipCount.put(chip.name, -1);
            chipCount.put(chip.name, chipCount.get(chip.name) + 1);

            chip.setup(chipCount.get(chip.name), pos, vgmCmdTbl);
            //chips.add(chip);
        }

        //usedChips = getUsedChipsString(chips);

        vgmGd3 += 12; // + 0x14;
        gd3 = Common.getGD3Info(buf, vgmGd3);
        gd3.usedChips = usedChips;

        return true;
    }

    private boolean getZGMInfo(byte[] vgmBuf) {
        if (vgmBuf == null) return false;

        try {
            if (Common.getLE32(vgmBuf, 0) != FCC_ZGM) return false;

            if (!getZGMGD3Info(vgmBuf)) return false;
        } catch (Exception e) {
            Log.write(String.format("XGMの情報取得中に例外発生 Message=[%s] StackTrace=[%s]", e.getMessage(), Arrays.toString(e.getStackTrace())));
            return false;
        }

        return true;
    }

    static class TrackInfo {
        public int offset = 0;
    }

    public static class DefineInfo {
        public byte length = 14;
        public int chipIdentNo = 0;
        public int commandNo = 0;
        public int clock = 0;
        public byte[] option = null;

        //public ClsChip chip = null;
        public int offset = 0;
    }
}


