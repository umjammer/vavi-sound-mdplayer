package mdplayer.driver.s98;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Log;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import vavi.util.Debug;


public class S98 extends BaseDriver {
    public S98(Setting setting) {
        this.setting = setting;
        musicStep = setting.getOutputDevice().getSampleRate() / 60.0;
    }

    public static final int FCC_S98 = 0x00383953; // "S98 "
    public static final int FCC_BOM = 0x00BFBBEF; // BOM

    public S98Info s98Info;
    private List<String> chips = null;
    private int musicPtr = 0;
    private double oneSyncTime;
    private double musicStep = 1;// setting.getoutputDevice().SampleRate / 60.0;
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
            if (Common.getLE24(buf, 0) != FCC_S98) return null;
            int Format = buf[3] - '0';
            int TAGAdr = Common.getLE32(buf, 0x10);
            if (Format < 2) {
                List<Byte> strLst = new ArrayList<>();
                String str;
                while (buf[TAGAdr] != 0x0a && buf[TAGAdr] != 0x00) {
                    strLst.add(buf[TAGAdr++]);
                }
                str = new String(mdsound.Common.toByteArray(strLst), Charset.forName("MS932"));
                gd3.trackName = str;
                gd3.trackNameJ = str;
            } else if (Format == 3) {
                if (buf[TAGAdr++] != 0x5b) return null;
                if (buf[TAGAdr++] != 0x53) return null;
                if (buf[TAGAdr++] != 0x39) return null;
                if (buf[TAGAdr++] != 0x38) return null;
                if (buf[TAGAdr++] != 0x5d) return null;
                boolean IsUTF8 = false;
                if (Common.getLE24(buf, TAGAdr) == FCC_BOM) {
                    IsUTF8 = true;
                    TAGAdr += 3;
                }

                while (buf.length > TAGAdr && buf[TAGAdr] != 0x00) {
                    List<Byte> strLst = new ArrayList<>();
                    String str;
                    while (buf[TAGAdr] != 0x0a && buf[TAGAdr] != 0x00) {
                        strLst.add(buf[TAGAdr++]);
                    }
                    if (IsUTF8) {
                        str = new String(mdsound.Common.toByteArray(strLst), StandardCharsets.UTF_8);
                    } else {
                        str = new String(mdsound.Common.toByteArray(strLst), Charset.forName("MS932"));
                    }
                    TAGAdr++;

                    if (str.toLowerCase().contains("artist=")) {
                        try {
                            gd3.composer = str.substring(str.indexOf("=") + 1);
                            gd3.composerJ = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                    if (str.toLowerCase().contains("s98by=")) {
                        try {
                            gd3.vgmBy = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (str.toLowerCase().contains("game=")) {
                        try {
                            gd3.gameName = str.substring(str.indexOf("=") + 1);
                            gd3.gameNameJ = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            e.printStackTrace();
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
                            e.printStackTrace();
                        }
                    }
                    if (str.toLowerCase().contains("title=")) {
                        try {
                            gd3.trackName = str.substring(str.indexOf("=") + 1);
                            gd3.trackNameJ = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (str.toLowerCase().contains("year=")) {
                        try {
                            gd3.converted = str.substring(str.indexOf("=") + 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            Debug.printf("S98のTAG情報取得中に例外発生 Message=[%s] StackTrace=[%s]", e.getMessage(), Arrays.toString(e.getStackTrace()));
            return null;
        }

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

        gd3 = getGD3Info(vgmBuf);
        //if (Gd3 == null) return false;

        if (!getInformationHeader()) return false;

        if (model == EnmModel.RealModel) {
            chipRegister.setYM2612SyncWait((byte) 0, 1);
            chipRegister.setYM2612SyncWait((byte) 1, 1);
        }

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
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
            ex.printStackTrace();
        }
    }

    private boolean getInformationHeader() {

        s98Info.FormatVersion = vgmBuf[3] - '0';
        s98Info.DeviceCount = Integer.MAX_VALUE;
        switch (s98Info.FormatVersion) {
        case 0:
        case 1:
            s98Info.SyncNumerator = Common.getLE32(vgmBuf, 4);
            if (s98Info.SyncNumerator == 0) s98Info.SyncNumerator = 10;
            s98Info.SyncDnumerator = 1000;
            s98Info.Compressing = Common.getLE32(vgmBuf, 0xc); // not support
            s98Info.TAGAddress = Common.getLE32(vgmBuf, 0x10);
            s98Info.DumpAddress = Common.getLE32(vgmBuf, 0x14);
            s98Info.LoopAddress = Common.getLE32(vgmBuf, 0x18);
            s98Info.DeviceCount = 0;
            break;
        case 2:
            s98Info.SyncNumerator = Common.getLE32(vgmBuf, 4);
            if (s98Info.SyncNumerator == 0) s98Info.SyncNumerator = 10;
            s98Info.SyncDnumerator = Common.getLE32(vgmBuf, 8);
            if (s98Info.SyncDnumerator == 0) s98Info.SyncDnumerator = 1000;
            s98Info.Compressing = Common.getLE32(vgmBuf, 0xc); // not support
            s98Info.TAGAddress = Common.getLE32(vgmBuf, 0x10);
            s98Info.DumpAddress = Common.getLE32(vgmBuf, 0x14);
            s98Info.LoopAddress = Common.getLE32(vgmBuf, 0x18);
            //0x1c Compressed data not support
            if (Common.getLE32(vgmBuf, 0x20) == 0) s98Info.DeviceCount = 0;
            break;
        case 3:
            s98Info.SyncNumerator = Common.getLE32(vgmBuf, 4);
            if (s98Info.SyncNumerator == 0) s98Info.SyncNumerator = 10;
            s98Info.SyncDnumerator = Common.getLE32(vgmBuf, 8);
            if (s98Info.SyncDnumerator == 0) s98Info.SyncDnumerator = 1000;
            s98Info.Compressing = Common.getLE32(vgmBuf, 0xc);
            s98Info.TAGAddress = Common.getLE32(vgmBuf, 0x10);
            s98Info.DumpAddress = Common.getLE32(vgmBuf, 0x14);
            s98Info.LoopAddress = Common.getLE32(vgmBuf, 0x18);
            s98Info.DeviceCount = Common.getLE32(vgmBuf, 0x1c);
            break;
        }

        byte[] devIDs = new byte[256];

        s98Info.DeviceInfos = new ArrayList<>();
        if (s98Info.DeviceCount == 0) {
            S98DevInfo info = new S98DevInfo();
            info.ChipID = 0;
            info.DeviceType = 4;
            info.clock = 7987200;
            info.Pan = 3;
            s98Info.DeviceInfos.add(info);
            chips.add("YM2608");
            s98Info.DeviceCount = 1;
        } else {
            if (s98Info.FormatVersion == 2) {
                int i = 0;
                while (Common.getLE32(vgmBuf, 0x20 + i * 0x10) != 0) {
                    S98DevInfo info = new S98DevInfo();
                    info.DeviceType = Common.getLE32(vgmBuf, 0x20 + i * 0x10);
                    if (devIDs[info.DeviceType] > 1) {
                        i++;
                        continue; // 同じchipは2こまで
                    }
                    info.clock = Common.getLE32(vgmBuf, 0x24 + i * 0x10);
                    switch (info.DeviceType) {
                    case 1:
                        chips.add("YM2149");
                        break;
                    case 2:
                        chips.add("YM2203");
                        break;
                    case 3:
                        chips.add("Ym2612");
                        break;
                    case 4:
                        chips.add("YM2608");
                        break;
                    case 5:
                        chips.add("YM2151");
                        break;
                    }

                    info.ChipID = devIDs[info.DeviceType]++;
                    s98Info.DeviceInfos.add(info);
                }
                s98Info.DeviceCount = i;
            } else {
                for (int i = 0; i < s98Info.DeviceCount; i++) {
                    S98DevInfo info = new S98DevInfo();
                    info.DeviceType = Common.getLE32(vgmBuf, 0x20 + i * 0x10);
                    if (devIDs[info.DeviceType] > 1) continue; // 同じchipは2こまで

                    info.clock = Common.getLE32(vgmBuf, 0x24 + i * 0x10);
                    info.Pan = Common.getLE32(vgmBuf, 0x28 + i * 0x10);
                    switch (info.DeviceType) {
                    case 1:
                        chips.add("YM2149");
                        break;
                    case 2:
                        chips.add("YM2203");
                        break;
                    case 3:
                        chips.add("Ym2612");
                        break;
                    case 4:
                        chips.add("YM2608");
                        break;
                    case 5:
                        chips.add("YM2151");
                        break;
                    case 6:
                        chips.add("YM2413");
                        break;
                    case 7:
                        chips.add("YM3526");
                        break;
                    case 8:
                        chips.add("YM3812");
                        break;
                    case 9:
                        chips.add("YMF262");
                        break;
                    case 15:
                        chips.add("AY8910");
                        break;
                    case 16:
                        chips.add("SN76489");
                        break;
                    }

                    info.ChipID = devIDs[info.DeviceType]++;
                    s98Info.DeviceInfos.add(info);
                }
            }
        }

        musicPtr = s98Info.DumpAddress;
        oneSyncTime = s98Info.SyncNumerator / (double) s98Info.SyncDnumerator;

        return true;
    }

    public static class S98Info {
        public int FormatVersion = 0;
        public int SyncNumerator = 0;
        public int SyncDnumerator = 0;
        public int Compressing = 0;
        public int TAGAddress = 0;
        public int DumpAddress = 0;
        public int LoopAddress = 0;
        public int DeviceCount = 0;
        public List<S98DevInfo> DeviceInfos = null;
    }

    public static class S98DevInfo {
        public byte ChipID = 0;
        public int DeviceType = 0;
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
            ex.printStackTrace();
        }
    }

    private int ym2608WaitCounter = 0;
    //private boolean ym2608WaitSw = false;

    private void oneFrameS98() {
        try {
            while (true) {
                if (vgmBuf == null) {
                    break;
                }

                byte cmd = vgmBuf[musicPtr++];

                //wait 1Sync
                if (cmd == 0xff) {
                    s98WaitCounter = 1;
                    ym2608WaitCounter = 0;
                    break;
                }

                //wait nSync
                if (cmd == 0xfe) {
                    s98WaitCounter = Common.getVv(vgmBuf, musicPtr);
                    ym2608WaitCounter = 0;
                    break;
                }

                //end/loop command
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
                if (devNo >= s98Info.DeviceInfos.size())// s98Info.DeviceCount)
                {
                    musicPtr += 2;
                    continue;
                }

                byte devPort = (byte) (cmd % 2);

                switch (s98Info.DeviceInfos.get(devNo).DeviceType) {
                case 1:
                    WriteAY8910(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 2:
                    WriteYM2203(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 3:
                    WriteYM2612(s98Info.DeviceInfos.get(devNo).ChipID, devPort, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 4:

                    if (model == EnmModel.RealModel) {
                        if (ym2608WaitCounter > 200) {
                            isDataBlock = true;
                            ym2608WaitCounter = 0;

                            try { Thread.sleep(10); } catch (InterruptedException e) {}
                            //while ((chipRegister.getYM2608Register(s98Info.DeviceInfos.get(devNo).ChipID, 0x1, 0x00, model) & 0xbf) != 0)
                            //{
                            //    Thread.sleep(0);
                            //}

                            isDataBlock = false;
                        }

                        //if (ym2608WaitCounter > 1000)
                        //{
                        //    ym2608WaitSw = true;
                        //}
                        //else if (ym2608WaitSw && ym2608WaitCounter == 1)
                        //{
                        //    chipRegister.sendDataYM2608(s98Info.DeviceInfos.get(devNo).ChipID, model);
                        //    ym2608WaitSw = false;
                        //}
                    }

                    WriteYM2608(s98Info.DeviceInfos.get(devNo).ChipID, devPort, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    ym2608WaitCounter++;
                    break;
                case 5:
                    WriteYM2151(s98Info.DeviceInfos.get(devNo).ChipID, devPort, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 6:
                    WriteYM2413(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 7:
                    WriteYM3526(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 8:
                    WriteYM3812(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 9:
                    WriteYMF262(s98Info.DeviceInfos.get(devNo).ChipID, devPort, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 15:
                    WriteAY8910(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr], vgmBuf[musicPtr + 1]);
                    break;
                case 16:
                    WriteSN76489(s98Info.DeviceInfos.get(devNo).ChipID, vgmBuf[musicPtr + 1]);
                    break;
                }
                musicPtr += 2;

            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            stopped = true;
        }
    }

    private void WriteYM2203(int chipID, byte adr, byte data) {
        chipRegister.setYM2203Register(chipID, adr, data, model);
    }

    private void WriteYM2612(int chipID, byte port, byte adr, byte data) {
        chipRegister.setYM2612Register(chipID, port, adr, data, model, 0);
    }

    private void WriteYM2608(int chipID, byte port, byte adr, byte data) {
        chipRegister.setYM2608Register(chipID, port, adr, data, model);
    }

    private void WriteYM2151(int chipID, byte port, byte adr, byte data) {
        chipRegister.setYM2151Register(chipID, port, adr, data, model, ym2151Hosei[chipID], 0);
    }

    private void WriteYM2413(int chipID, byte adr, byte data) {
        chipRegister.setYM2413Register(chipID, adr, data, model);
    }

    private void WriteYM3526(int chipID, byte adr, byte data) {
        chipRegister.setYM3526Register(chipID, adr, data, model);
    }

    private void WriteYM3812(int chipID, byte adr, byte data) {
        chipRegister.setYM3812Register(chipID, adr, data, model);
    }

    private void WriteAY8910(int chipID, byte adr, byte data) {
        chipRegister.setAY8910Register(chipID, adr, data, model);
    }

    private void WriteSN76489(int chipID, byte data) {
        chipRegister.setSN76489Register(chipID, data, model);
    }

    private void WriteYMF262(int chipID, byte port, byte adr, byte data) {
        chipRegister.setYMF262Register(chipID, port, adr, data, model);
    }
}
