package mdplayer.driver.zgm;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.zgm.zgmChip.ChipFactory;
import mdplayer.driver.zgm.zgmChip.ZgmChip;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * ZGM
 *
 * @author kumatan
 */
public class Zgm extends BaseDriver {

    private static final Logger logger = getLogger(Zgm.class.getName());

    public static final int FCC_ZGM = 0x204D475A;  // "ZGM "
    public static final int FCC_GD3 = 0x20336447;  // "Gd3 "
    public static final int FCC_DEF = 0x666544;  // "Def"
    public static final int FCC_TRK = 0x6b7254;  // "Trk"

    private int vgmEof;
    private long vgmLoopOffset = 0;
    private int chipCommandSize = 1;
    private long vgmDataOffset = 0;

    public interface RefRunnable<T1, T2> extends BiConsumer<T1, T2> {
    }

    private final Map<Integer, RefRunnable<Byte, Integer>> vgmCmdTbl = new HashMap<>();

    public Zgm(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public Zgm() {
        this(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        getZGMGD3Info(buf);
        return metaData;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
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
        speedCounter = 0;

        getZGMInfo(dataBuf);
    }

    @Override
    public void processOneFrame() {
        throw new UnsupportedOperationException();
    }

    private void getZGMGD3Info(byte[] buf) {
        if (buf == null) throw new IllegalArgumentException("null buffer");

        int vgmGd3 = ByteUtil.readLeInt(buf, (byte) 0x18);
        if (vgmGd3 == 0) throw new IllegalArgumentException("invalid cgm gd3 value");
        int vgmGd3Id = ByteUtil.readLeInt(buf, vgmGd3);
        if (vgmGd3Id != FCC_GD3) throw new IllegalArgumentException("data is not gd3");

        vgmEof = ByteUtil.readLeInt(dataBuf, (byte) 0x04);

        int version = ByteUtil.readLeInt(dataBuf, 0x08);
        // Version Check
        if (version < 10) throw new IllegalArgumentException("invalid version");
        this.version = "%d.%d%d".formatted((version & 0xf00) / 0x100, (version & 0xf0) / 0x10, (version & 0xf));

        totalCounter = ByteUtil.readLeInt(dataBuf, 0x0c);
        if (totalCounter < 0) throw new IllegalArgumentException("invalid total counter");
        vgmLoopOffset = ByteUtil.readLeInt(dataBuf, 0x14);
        loopCounter = ByteUtil.readLeInt(dataBuf, 0x10);

        int defineAddress = ByteUtil.readLeInt(dataBuf, 0x1c);
        int defineCount = ByteUtil.readLeShort(dataBuf, 0x24);
        // Check number of sound source definitions
        if (defineCount < 1) throw new IllegalArgumentException("invalid define count");

        chipCommandSize = (defineCount > 128) ? 2 : 1;

        int trackAddress = ByteUtil.readLeInt(dataBuf, 0x20);
        int trackCounter = ByteUtil.readLeShort(dataBuf, 0x26);
        vgmDataOffset = trackAddress + 11;
        // Track Count Check
        if (trackCounter != 1) throw new IllegalArgumentException("invalid track counter");
        int fcc = ByteUtil.readLe24(dataBuf, trackAddress);
        if (fcc != FCC_TRK) throw new IllegalArgumentException("invalid fcc track value");
        int trackLength = ByteUtil.readLeInt(dataBuf, trackAddress + 3);
        vgmLoopOffset = ByteUtil.readLeInt(dataBuf, trackAddress + 7);
        if (vgmLoopOffset != 0) loopCounter = 1;
        vgmEof = trackAddress + trackLength;

        int pos = defineAddress;

        Map<String, Integer> chipCount = new HashMap<>();
        for (int i = 0; i < defineCount; i++) {
            fcc = ByteUtil.readLe24(dataBuf, pos);
            if (fcc != FCC_DEF) throw new IllegalArgumentException("invalid fcc def value");
            int chipNum = ByteUtil.readLeInt(dataBuf, pos + 0x4);
            ZgmChip chip = (new ChipFactory()).create(chipNum, plugin.chipRegister, setting, dataBuf);
            if (chip == null) {
                throw new IllegalArgumentException("not supported chip: " + chipNum);
            }

            if (!chipCount.containsKey(chip.name)) chipCount.put(chip.name, -1);
            chipCount.put(chip.name, chipCount.get(chip.name) + 1);

            chip.setUp(chipCount.get(chip.name), pos, vgmCmdTbl);
            //chips.add(chips);
        }

        //usedChips = getUsedChipsString(chips);

        vgmGd3 += 12; // + 0x14;
        metaData = Common.getMetaData(buf, vgmGd3);
        metaData.set(Tag.Chip, usedChips);
    }

    private void getZGMInfo(byte[] vgmBuf) {
        if (vgmBuf == null) throw new IllegalArgumentException("null buffer");

        try {
            if (ByteUtil.readLeInt(vgmBuf, 0) != FCC_ZGM) throw new IllegalArgumentException("buffer is not zgm");

            getZGMGD3Info(vgmBuf);
        } catch (Exception e) {
logger.log(Level.ERROR, "An exception occurred while getting XGM information. Message=[%s]".formatted(e.getMessage()), e);
            throw e;
        }
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

        //public ClsChip chips = null;
        public int offset = 0;
    }
}
