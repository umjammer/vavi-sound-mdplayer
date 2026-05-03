package mdplayer.driver.s98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import mdplayer.Common;
import vavi.util.ByteUtil;
import vavi.util.compat.QuadConsumer;
import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;


public class S98 {

    private static final Logger logger = getLogger(S98.class.getName());

    public static final int FCC_S98 = 0x00383953; // "S98 "
    public static final int FCC_BOM = 0x00BFBBEF; // BOM

    public S98Info s98Info;
    List<String> chips = null;
    private int musicPtr = 0;
    private double oneSyncTime;
    double musicStep = 1; // setting.getoutputDevice().SampleRate / 60.0;
    private double musicDownCounter = 0.0;
    private int s98WaitCounter;
    public int SSGVolumeFromTAG = -1;

    int sampleRate;
    boolean isRealModel;
    Consumer<Boolean> dataBlock;
    Runnable stop;
    Runnable loop;
    TriConsumer<Integer, Integer, Integer> writeYM2203;
    QuadConsumer<Integer, Integer, Integer, Integer> writeYM2612;
    QuadConsumer<Integer, Integer, Integer, Integer> writeYM2608;
    QuadConsumer<Integer, Integer, Integer, Integer> writeYM2151;
    TriConsumer<Integer, Integer, Integer> writeYM2413;
    TriConsumer<Integer, Integer, Integer> writeYM3526;
    TriConsumer<Integer, Integer, Integer> writeYM3812;
    TriConsumer<Integer, Integer, Integer> writeAY8910;
    BiConsumer<Integer, Integer> writeSN76489;
    QuadConsumer<Integer, Integer, Integer, Integer> writeYMF262;

    boolean getInformationHeader(byte[] data) {

        s98Info.formatVersion = (data[3] & 0xff) - '0';
        s98Info.deviceCount = Integer.MAX_VALUE;
        switch (s98Info.formatVersion) {
        case 0:
        case 1:
            s98Info.syncNumerator = ByteUtil.readLeInt(data, 4);
            if (s98Info.syncNumerator == 0) s98Info.syncNumerator = 10;
            s98Info.syncDNumerator = 1000;
            s98Info.compressing = ByteUtil.readLeInt(data, 0xc); // not support
            s98Info.tagAddress = ByteUtil.readLeInt(data, 0x10);
            s98Info.dumpAddress = ByteUtil.readLeInt(data, 0x14);
            s98Info.loopAddress = ByteUtil.readLeInt(data, 0x18);
            s98Info.deviceCount = 0;
            break;
        case 2:
            s98Info.syncNumerator = ByteUtil.readLeInt(data, 4);
            if (s98Info.syncNumerator == 0) s98Info.syncNumerator = 10;
            s98Info.syncDNumerator = ByteUtil.readLeInt(data, 8);
            if (s98Info.syncDNumerator == 0) s98Info.syncDNumerator = 1000;
            s98Info.compressing = ByteUtil.readLeInt(data, 0xc); // not support
            s98Info.tagAddress = ByteUtil.readLeInt(data, 0x10);
            s98Info.dumpAddress = ByteUtil.readLeInt(data, 0x14);
            s98Info.loopAddress = ByteUtil.readLeInt(data, 0x18);
            //0x1c Compressed data not support
            if (ByteUtil.readLeInt(data, 0x20) == 0) s98Info.deviceCount = 0;
            break;
        case 3:
            s98Info.syncNumerator = ByteUtil.readLeInt(data, 4);
            if (s98Info.syncNumerator == 0) s98Info.syncNumerator = 10;
            s98Info.syncDNumerator = ByteUtil.readLeInt(data, 8);
            if (s98Info.syncDNumerator == 0) s98Info.syncDNumerator = 1000;
            s98Info.compressing = ByteUtil.readLeInt(data, 0xc);
            s98Info.tagAddress = ByteUtil.readLeInt(data, 0x10);
            s98Info.dumpAddress = ByteUtil.readLeInt(data, 0x14);
            s98Info.loopAddress = ByteUtil.readLeInt(data, 0x18);
            s98Info.deviceCount = ByteUtil.readLeInt(data, 0x1c);
            break;
        }

        byte[] devIDs = new byte[256];
        s98Info.deviceInfos = new ArrayList<>();
        if (s98Info.deviceCount == 0) {
            S98DevInfo info = new S98DevInfo();
            info.chipId = 0;
            info.deviceType = 4;
            info.clock = 7987200;
            info.pan = 3;
            s98Info.deviceInfos.add(info);
            chips.add("YM2608");
            s98Info.deviceCount = 1;
        } else {
            if (s98Info.formatVersion == 2) {
                int i = 0;
                while (ByteUtil.readLeInt(data, 0x20 + i * 0x10) != 0) {
                    S98DevInfo info = new S98DevInfo();
                    info.deviceType = ByteUtil.readLeInt(data, 0x20 + i * 0x10);
                    if (devIDs[info.deviceType] > 1) {
                        i++;
                        continue; // Up to 2 of the same chip
                    }
                    info.clock = ByteUtil.readLeInt(data, 0x24 + i * 0x10);
                    switch (info.deviceType) {
                        case 1 -> chips.add("YM2149");
                        case 2 -> chips.add("YM2203");
                        case 3 -> chips.add("Ym2612");
                        case 4 -> chips.add("YM2608");
                        case 5 -> chips.add("YM2151");
                    }

                    info.chipId = devIDs[info.deviceType]++;
                    s98Info.deviceInfos.add(info);
                }
                s98Info.deviceCount = i;
            } else {
                for (int i = 0; i < s98Info.deviceCount; i++) {
                    S98DevInfo info = new S98DevInfo();
                    info.deviceType = ByteUtil.readLeInt(data, 0x20 + i * 0x10);
                    if (devIDs[info.deviceType] > 1) continue; // Up to 2 of the same chip

                    info.clock = ByteUtil.readLeInt(data, 0x24 + i * 0x10);
                    info.pan = ByteUtil.readLeInt(data, 0x28 + i * 0x10);
                    switch (info.deviceType) {
                        case 1 -> chips.add("YM2149");
                        case 2 -> chips.add("YM2203");
                        case 3 -> chips.add("Ym2612");
                        case 4 -> chips.add("YM2608");
                        case 5 -> chips.add("YM2151");
                        case 6 -> chips.add("YM2413");
                        case 7 -> chips.add("YM3526");
                        case 8 -> chips.add("YM3812");
                        case 9 -> chips.add("YMF262");
                        case 15 -> chips.add("AY8910");
                        case 16 -> chips.add("SN76489");
                    }

                    info.chipId = devIDs[info.deviceType]++;
                    s98Info.deviceInfos.add(info);
                }
            }
        }

        musicPtr = s98Info.dumpAddress;
        oneSyncTime = s98Info.syncNumerator / (double) s98Info.syncDNumerator;

        return true;
    }

    public static class S98Info {
        public int formatVersion = 0;
        public int syncNumerator = 0;
        public int syncDNumerator = 0;
        public int compressing = 0;
        public int tagAddress = 0;
        public int dumpAddress = 0;
        public int loopAddress = 0;
        public int deviceCount = 0;
        public List<S98DevInfo> deviceInfos = null;
    }

    public static class S98DevInfo {
        public int chipId = 0;
        public int deviceType = 0;
        public int clock = 0;
        public int pan = 0;
    }

    void oneFrameMain(byte[] data) {
        try {
            musicStep = sampleRate * oneSyncTime;

            if (musicDownCounter <= 0.0) {
                s98WaitCounter--;
                if (s98WaitCounter <= 0) oneFrameS98(data);
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private int ym2608WaitCounter = 0;
//    private boolean ym2608WaitSw = false;

    private void oneFrameS98(byte[] data) {
        try {
            while (true) {
                if (data == null || musicPtr == data.length) {
                    break;
                }

                int cmd = data[musicPtr++] & 0xff;

                // wait 1Sync
                if (cmd == 0xff) {
                    s98WaitCounter = 1;
                    ym2608WaitCounter = 0;
                    break;
                }

                // wait nSync
                if (cmd == 0xfe) {
                    int[] tmp = new int[] {musicPtr};
                    s98WaitCounter = Common.getVv(data, tmp);
                    musicPtr = tmp[0];
                    ym2608WaitCounter = 0;
                    break;
                }

                // end/loop command
                if (cmd == 0xfd) {
                    if (s98Info.loopAddress != 0) {
                        musicPtr = s98Info.loopAddress;
                        loop.run();
                        continue;
                    } else {
                        stop.run();
                        break;
                    }
                }

                int devNo = cmd / 2;
                if (devNo >= s98Info.deviceInfos.size()) {
                    musicPtr += 2;
                    continue;
                }

                int devPort = cmd % 2;

                switch (s98Info.deviceInfos.get(devNo).deviceType) {
                case 1:
                    writeAY8910.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 2:
                    writeYM2203.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 3:
                    writeYM2612.accept(s98Info.deviceInfos.get(devNo).chipId, devPort, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 4:

                    if (isRealModel) {
                        if (ym2608WaitCounter > 200) {
                            dataBlock.accept(true);
                            ym2608WaitCounter = 0;

                            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
//                            while ((plugin.audio.chipRegister.getYM2608Register(s98Info.deviceInfos.get(devNo).chipId, 0x1, 0x00, model) & 0xbf) != 0) {
//                                Thread.sleep(0);
//                            }

                            dataBlock.accept(false);
                        }

//                        if (ym2608WaitCounter > 1000) {
//                            ym2608WaitSw = true;
//                        } else if (ym2608WaitSw && ym2608WaitCounter == 1) {
//                            plugin.audio.chipRegister.sendDataYM2608(s98Info.deviceInfos.get(devNo).chipId, model);
//                            ym2608WaitSw = false;
//                        }
                    }

                    writeYM2608.accept(s98Info.deviceInfos.get(devNo).chipId, devPort, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    ym2608WaitCounter++;
                    break;
                case 5:
                    writeYM2151.accept(s98Info.deviceInfos.get(devNo).chipId, devPort, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 6:
                    writeYM2413.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 7:
                    writeYM3526.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 8:
                    writeYM3812.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 9:
                    writeYMF262.accept(s98Info.deviceInfos.get(devNo).chipId, devPort, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 15:
                    writeAY8910.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr] & 0xff, data[musicPtr + 1] & 0xff);
                    break;
                case 16:
                    writeSN76489.accept(s98Info.deviceInfos.get(devNo).chipId, data[musicPtr + 1] & 0xff);
                    break;
                }
                musicPtr += 2;

            }
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            stop.run();
        }
    }
}
