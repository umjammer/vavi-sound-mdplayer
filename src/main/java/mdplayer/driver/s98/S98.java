package mdplayer.driver.s98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Ym3526Chip;
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import vavi.util.ByteUtil;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;
import static java.lang.System.getLogger;


public class S98 extends BaseDriver {

    private static final Logger logger = getLogger(S98.class.getName());

    public S98() {
        this.setting = Setting.getInstance();
        musicStep = setting.getOutputDevice().getSampleRate() / 60.0;
    }

    public static final int FCC_S98 = 0x00383953; // "S98 "
    public static final int FCC_BOM = 0x00BFBBEF; // BOM

    public S98Info s98Info;
    private List<String> chips = null;
    private int musicPtr = 0;
    private double oneSyncTime;
    private double musicStep = 1; // setting.getoutputDevice().SampleRate / 60.0;
    private double musicDownCounter = 0.0;
    private int s98WaitCounter;
    public int SSGVolumeFromTAG = -1;

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        Vgm.Gd3 gd3 = new Gd3();
        s98Info = new S98Info();
        chips = new ArrayList<>();

        try {
            if (ByteUtil.readLe24(buf, 0) != FCC_S98) return null;
            int format = buf[3] - '0';
            int tagAdr = ByteUtil.readLeInt(buf, 0x10);
            if (format < 2) {
                List<Byte> strLst = new ArrayList<>();
                String str;
                while (buf[tagAdr] != 0x0a && buf[tagAdr] != 0x00) {
                    strLst.add(buf[tagAdr++]);
                }
                str = new String(toByteArray(strLst), Charset.forName("MS932"));
                gd3.trackName = str;
                gd3.trackNameJ = str;
            } else if (format == 3) {
                if (buf[tagAdr++] != 0x5b) return null;
                if (buf[tagAdr++] != 0x53) return null;
                if (buf[tagAdr++] != 0x39) return null;
                if (buf[tagAdr++] != 0x38) return null;
                if (buf[tagAdr++] != 0x5d) return null;
                boolean isUTF8 = false;
                if (ByteUtil.readLe24(buf, tagAdr) == FCC_BOM) {
                    isUTF8 = true;
                    tagAdr += 3;
                }

                while (buf.length > tagAdr && buf[tagAdr] != 0x00) {
                    List<Byte> strLst = new ArrayList<>();
                    String str;
                    while (buf[tagAdr] != 0x0a && buf[tagAdr] != 0x00) {
                        strLst.add(buf[tagAdr++]);
                    }
                    if (isUTF8) {
                        str = new String(toByteArray(strLst), StandardCharsets.UTF_8);
                    } else {
                        str = new String(toByteArray(strLst), Charset.forName("MS932"));
                    }
                    tagAdr++;

                    if (str.toLowerCase().contains("artist=")) {
                        try {
                            gd3.composer = str.substring(str.indexOf("=") + 1);
                            gd3.composerJ = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            logger.log(Level.ERROR, e.getMessage(), e);

                        }
                    }
                    if (str.toLowerCase().contains("s98by=")) {
                        try {
                            gd3.vgmBy = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                    if (str.toLowerCase().contains("game=")) {
                        try {
                            gd3.gameName = str.substring(str.indexOf("=") + 1);
                            gd3.gameNameJ = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                    SSGVolumeFromTAG = -1;
                    if (str.toLowerCase().contains("system=")) {
                        try {
                            gd3.systemName = str.substring(str.indexOf("=") + 1);
                            gd3.systemNameJ = str.substring(str.indexOf("=") + 1);

                            if (gd3.systemName.indexOf("8801") > 0) SSGVolumeFromTAG = 63;
                            else if (gd3.systemName.indexOf("9801") > 0) SSGVolumeFromTAG = 31;
                        } catch (Exception e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                    if (str.toLowerCase().contains("title=")) {
                        try {
                            gd3.trackName = str.substring(str.indexOf("=") + 1);
                            gd3.trackNameJ = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                    if (str.toLowerCase().contains("year=")) {
                        try {
                            gd3.converted = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }

        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
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
        vgmSpeedCounter = 0;

        gd3 = getGD3Info(vgmBuf);
        //if (Gd3 == null) return false;

        if (!getInformationHeader()) return false;

        if (model == EnmModel.RealModel) {
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 0, 1);
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 1, 1);
        }

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private boolean getInformationHeader() {

        s98Info.FormatVersion = (vgmBuf[3] & 0xff) - '0';
        s98Info.DeviceCount = Integer.MAX_VALUE;
        switch (s98Info.FormatVersion) {
        case 0:
        case 1:
            s98Info.SyncNumerator = ByteUtil.readLeInt(vgmBuf, 4);
            if (s98Info.SyncNumerator == 0) s98Info.SyncNumerator = 10;
            s98Info.SyncDNumerator = 1000;
            s98Info.Compressing = ByteUtil.readLeInt(vgmBuf, 0xc); // not support
            s98Info.TAGAddress = ByteUtil.readLeInt(vgmBuf, 0x10);
            s98Info.DumpAddress = ByteUtil.readLeInt(vgmBuf, 0x14);
            s98Info.LoopAddress = ByteUtil.readLeInt(vgmBuf, 0x18);
            s98Info.DeviceCount = 0;
            break;
        case 2:
            s98Info.SyncNumerator = ByteUtil.readLeInt(vgmBuf, 4);
            if (s98Info.SyncNumerator == 0) s98Info.SyncNumerator = 10;
            s98Info.SyncDNumerator = ByteUtil.readLeInt(vgmBuf, 8);
            if (s98Info.SyncDNumerator == 0) s98Info.SyncDNumerator = 1000;
            s98Info.Compressing = ByteUtil.readLeInt(vgmBuf, 0xc); // not support
            s98Info.TAGAddress = ByteUtil.readLeInt(vgmBuf, 0x10);
            s98Info.DumpAddress = ByteUtil.readLeInt(vgmBuf, 0x14);
            s98Info.LoopAddress = ByteUtil.readLeInt(vgmBuf, 0x18);
            //0x1c Compressed data not support
            if (ByteUtil.readLeInt(vgmBuf, 0x20) == 0) s98Info.DeviceCount = 0;
            break;
        case 3:
            s98Info.SyncNumerator = ByteUtil.readLeInt(vgmBuf, 4);
            if (s98Info.SyncNumerator == 0) s98Info.SyncNumerator = 10;
            s98Info.SyncDNumerator = ByteUtil.readLeInt(vgmBuf, 8);
            if (s98Info.SyncDNumerator == 0) s98Info.SyncDNumerator = 1000;
            s98Info.Compressing = ByteUtil.readLeInt(vgmBuf, 0xc);
            s98Info.TAGAddress = ByteUtil.readLeInt(vgmBuf, 0x10);
            s98Info.DumpAddress = ByteUtil.readLeInt(vgmBuf, 0x14);
            s98Info.LoopAddress = ByteUtil.readLeInt(vgmBuf, 0x18);
            s98Info.DeviceCount = ByteUtil.readLeInt(vgmBuf, 0x1c);
            break;
        }

        byte[] devIDs = new byte[256];

        s98Info.DeviceInfos = new ArrayList<>();
        if (s98Info.DeviceCount == 0) {
            S98DevInfo info = new S98DevInfo();
            info.chipId = 0;
            info.deviceType = 4;
            info.clock = 7987200;
            info.Pan = 3;
            s98Info.DeviceInfos.add(info);
            chips.add("YM2608");
            s98Info.DeviceCount = 1;
        } else {
            if (s98Info.FormatVersion == 2) {
                int i = 0;
                while (ByteUtil.readLeInt(vgmBuf, 0x20 + i * 0x10) != 0) {
                    S98DevInfo info = new S98DevInfo();
                    info.deviceType = ByteUtil.readLeInt(vgmBuf, 0x20 + i * 0x10);
                    if (devIDs[info.deviceType] > 1) {
                        i++;
                        continue; // Up to 2 of the same chip
                    }
                    info.clock = ByteUtil.readLeInt(vgmBuf, 0x24 + i * 0x10);
                    switch (info.deviceType) {
                        case 1 -> chips.add("YM2149");
                        case 2 -> chips.add("YM2203");
                        case 3 -> chips.add("Ym2612");
                        case 4 -> chips.add("YM2608");
                        case 5 -> chips.add("YM2151");
                    }

                    info.chipId = devIDs[info.deviceType]++;
                    s98Info.DeviceInfos.add(info);
                }
                s98Info.DeviceCount = i;
            } else {
                for (int i = 0; i < s98Info.DeviceCount; i++) {
                    S98DevInfo info = new S98DevInfo();
                    info.deviceType = ByteUtil.readLeInt(vgmBuf, 0x20 + i * 0x10);
                    if (devIDs[info.deviceType] > 1) continue; // Up to 2 of the same chip

                    info.clock = ByteUtil.readLeInt(vgmBuf, 0x24 + i * 0x10);
                    info.Pan = ByteUtil.readLeInt(vgmBuf, 0x28 + i * 0x10);
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
                    s98Info.DeviceInfos.add(info);
                }
            }
        }

        musicPtr = s98Info.DumpAddress;
        oneSyncTime = s98Info.SyncNumerator / (double) s98Info.SyncDNumerator;

        return true;
    }

    public static class S98Info {
        public int FormatVersion = 0;
        public int SyncNumerator = 0;
        public int SyncDNumerator = 0;
        public int Compressing = 0;
        public int TAGAddress = 0;
        public int DumpAddress = 0;
        public int LoopAddress = 0;
        public int DeviceCount = 0;
        public List<S98DevInfo> DeviceInfos = null;
    }

    public static class S98DevInfo {
        public int chipId = 0;
        public int deviceType = 0;
        public int clock = 0;
        public int Pan = 0;
    }

    private void oneFrameMain() {
        try {

            counter++;
            vgmFrameCounter++;

            musicStep = setting.getOutputDevice().getSampleRate() * oneSyncTime;

            if (musicDownCounter <= 0.0) {
                s98WaitCounter--;
                if (s98WaitCounter <= 0) oneFrameS98();
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private int ym2608WaitCounter = 0;
//    private boolean ym2608WaitSw = false;

    private void oneFrameS98() {
        try {
            while (true) {
                if (vgmBuf == null || musicPtr == vgmBuf.length) {
                    break;
                }

                int cmd = vgmBuf[musicPtr++] & 0xff;

                // wait 1Sync
                if (cmd == 0xff) {
                    s98WaitCounter = 1;
                    ym2608WaitCounter = 0;
                    break;
                }

                // wait nSync
                if (cmd == 0xfe) {
                    int[] tmp = new int[] {musicPtr};
                    s98WaitCounter = Common.getVv(vgmBuf, tmp);
                    musicPtr = tmp[0];
                    ym2608WaitCounter = 0;
                    break;
                }

                // end/loop command
                if (cmd == 0xfd) {
                    if (s98Info.LoopAddress != 0) {
                        musicPtr = s98Info.LoopAddress;
                        vgmCurLoop++;
                        continue;
                    } else {
                        stopped = true;
                        break;
                    }
                }

                int devNo = cmd / 2;
                if (devNo >= s98Info.DeviceInfos.size()) {
                    musicPtr += 2;
                    continue;
                }

                int devPort = cmd % 2;

                switch (s98Info.DeviceInfos.get(devNo).deviceType) {
                case 1:
                    writeAY8910(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 2:
                    writeYM2203(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 3:
                    writeYM2612(s98Info.DeviceInfos.get(devNo).chipId, devPort, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 4:

                    if (model == EnmModel.RealModel) {
                        if (ym2608WaitCounter > 200) {
                            isDataBlock = true;
                            ym2608WaitCounter = 0;

                            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
//                            while ((plugin.audio.chipRegister.getYM2608Register(s98Info.DeviceInfos.get(devNo).chipId, 0x1, 0x00, model) & 0xbf) != 0) {
//                                Thread.sleep(0);
//                            }

                            isDataBlock = false;
                        }

//                        if (ym2608WaitCounter > 1000) {
//                            ym2608WaitSw = true;
//                        } else if (ym2608WaitSw && ym2608WaitCounter == 1) {
//                            plugin.audio.chipRegister.sendDataYM2608(s98Info.DeviceInfos.get(devNo).chipId, model);
//                            ym2608WaitSw = false;
//                        }
                    }

                    writeYM2608(s98Info.DeviceInfos.get(devNo).chipId, devPort, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    ym2608WaitCounter++;
                    break;
                case 5:
                    writeYM2151(s98Info.DeviceInfos.get(devNo).chipId, devPort, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 6:
                    writeYM2413(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 7:
                    writeYM3526(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 8:
                    writeYM3812(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 9:
                    writeYMF262(s98Info.DeviceInfos.get(devNo).chipId, devPort, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 15:
                    writeAY8910(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr] & 0xff, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                case 16:
                    writeSN76489(s98Info.DeviceInfos.get(devNo).chipId, vgmBuf[musicPtr + 1] & 0xff);
                    break;
                }
                musicPtr += 2;

            }
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            stopped = true;
        }
    }

    private void writeYM2203(int chipId, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym2203Chip.class).write(chipId, adr, data, model);
    }

    private void writeYM2612(int chipId, int port, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym2612Chip.class).write(chipId, port, adr, data, model, 0);
    }

    private void writeYM2608(int chipId, int port, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym2608Chip.class).write(chipId, port, adr, data, model);
    }

    private void writeYM2151(int chipId, int port, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym2151Chip.class).write(chipId, port, adr, data, model, ym2151Hosei[chipId], 0);
    }

    private void writeYM2413(int chipId, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym2413Chip.class).write(chipId, adr, data, model);
    }

    private void writeYM3526(int chipId, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym3526Chip.class).write(chipId, adr, data, model);
    }

    private void writeYM3812(int chipId, int adr, int data) {
        plugin.audio.chipRegister.chip(Ym3812Chip.class).write(chipId, adr, data, model);
    }

    private void writeAY8910(int chipId, int adr, int data) {
        plugin.audio.chipRegister.chip(Ay8910Chip.class).write(chipId, adr, data, model);
    }

    private void writeSN76489(int chipId, int data) {
        plugin.audio.chipRegister.chip(Sn76489Chip.class).write(chipId, data, model);
    }

    private void writeYMF262(int chipId, int port, int adr, int data) {
        plugin.audio.chipRegister.chip(YmF262Chip.class).write(chipId, port, adr, data, model);
    }
}
