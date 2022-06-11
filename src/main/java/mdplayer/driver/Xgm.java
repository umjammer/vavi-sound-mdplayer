package mdplayer.driver;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.Log;
import mdplayer.Setting;


public class Xgm extends BaseDriver {

    public Xgm(Setting setting) {
        this.setting = setting;
        musicStep = Common.VGMProcSampleRate / 60.0;// setting.getoutputDevice().SampleRate / 60.0;
        pcmStep = setting.getOutputDevice().getSampleRate() / 14000.0;
    }

    public static final int FCC_XGM = 0x204d4758;    // "XGM "
    public static final int FCC_GD3 = 0x20336447;  // "Gd3 "

    private class XGMSampleID {
        public int addr = 0;
        public int size = 0;
    }

    private XGMSampleID[] sampleID = new XGMSampleID[63];
    private int sampleDataBlockSize = 0;
    private int sampleDataBlockAddr = 0;
    private int musicDataBlockSize = 0;
    private int musicDataBlockAddr = 0;
    private byte versionInformation = 0;
    private byte dataInformation = 0;
    private Boolean isNTSC = false;
    private Boolean existGD3 = false;
    private Boolean multiTrackFile = false;
    private int gd3InfoStartAddr = 0;


    @Override
    public boolean init(byte[] xgmBuf, ChipRegister chipRegister, EnmModel model, Common.EnmChip[] useChip, int latency, int waitTime) {

        this.vgmBuf = xgmBuf;
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

        if (!getXGMInfo(vgmBuf)) return false;

        if (model == EnmModel.RealModel) {
            chipRegister.setYM2612SyncWait((byte) 0, 1);
            chipRegister.setYM2612SyncWait((byte) 1, 1);
        }

        //Driverの初期化
        musicPtr = musicDataBlockAddr;
        xgmpcm = new XgmPcm[] {new XgmPcm(), new XgmPcm(), new XgmPcm(), new XgmPcm()};
        DACEnable = 0;

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, Common.EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    @Override
    public void oneFrameProc() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }

            pcmSpeedCounter++;//= (double)Common.VGMProcSampleRate / setting.getoutputDevice().SampleRate * vgmSpeed;
            while (pcmSpeedCounter >= 1.0 && !stopped) {
                pcmSpeedCounter -= 1.0;
                onePCMFrameMain();
            }

            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        getXGMInfo(buf);
        return gd3;
    }

    private Vgm.Gd3 getGD3Info(byte[] vgmBuf) {

        if (!existGD3) return new Vgm.Gd3();

        Vgm.Gd3 GD3 = Common.getGD3Info(vgmBuf, gd3InfoStartAddr + 12);
        GD3.usedChips = usedChips;

        return GD3;

    }

    private Boolean getXGMInfo(byte[] vgmBuf) {
        if (vgmBuf == null) return false;

        try {
            if (Common.getLE32(vgmBuf, 0) != FCC_XGM) return false;

            for (int i = 0; i < 63; i++) {
                sampleID[i] = new XGMSampleID();
                sampleID[i].addr = (Common.getLE16(vgmBuf, i * 4 + 4) * 256);
                sampleID[i].size = (Common.getLE16(vgmBuf, i * 4 + 6) * 256);
            }

            sampleDataBlockSize = Common.getLE16(vgmBuf, 0x100);

            versionInformation = vgmBuf[0x102];

            dataInformation = vgmBuf[0x103];

            isNTSC = (dataInformation & 0x1) == 0;

            existGD3 = (dataInformation & 0x2) != 0;

            multiTrackFile = (dataInformation & 0x4) != 0;

            sampleDataBlockAddr = 0x104;

            musicDataBlockSize = Common.getLE32(vgmBuf, sampleDataBlockAddr + sampleDataBlockSize * 256);

            musicDataBlockAddr = sampleDataBlockAddr + sampleDataBlockSize * 256 + 4;

            gd3InfoStartAddr = musicDataBlockAddr + musicDataBlockSize;

            gd3 = getGD3Info(vgmBuf);

            if (musicDataBlockSize == 0) {
                return false;
            }
        } catch (Exception e) {
            Log.write(String.format("XGMの情報取得中に例外発生 Message=[%s] StackTrace=[%s]", e.getMessage(), e.getStackTrace()));
            return false;
        }

        return true;
    }

    public Boolean isPlaying() {
        return true;
    }


    private double musicStep = 1;// setting.getoutputDevice().SampleRate / 60.0;
    private double pcmStep = 1;// setting.getoutputDevice().SampleRate / 14000.0;
    private double musicDownCounter = 0.0;
    private double pcmDownCounter = 0.0;
    private int musicPtr = 0;
    private byte DACEnable = 0;

    private void oneFrameMain() {
        try {
            //if (model == EnmModel.RealModel) return;

            counter++;
            vgmFrameCounter++;

            musicStep = Common.VGMProcSampleRate / (isNTSC ? 60.0 : 50.0);

            if (musicDownCounter <= 0.0) {
                //xgm処理
                oneFrameXGM();
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

            //if (pcmDownCounter <= 0.0)
            //{
            //    //pcm処理
            //    oneFramePCM();
            //    pcmDownCounter += pcmStep;
            //}
            //pcmDownCounter -= 1.0;

        } catch (Exception ex) {
            Log.forcedWrite(ex);

        }
    }

    private void onePCMFrameMain() {
        try {
            if (pcmDownCounter <= 0.0) {
                //pcm処理
                oneFramePCM();
                pcmDownCounter += pcmStep;
            }
            pcmDownCounter -= 1.0;

        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void oneFrameXGM() {
        while (true) {

            byte cmd = vgmBuf[musicPtr++];

            //wait
            if (cmd == 0) break;

            //loop command
            if (cmd == 0x7e) {
                musicPtr = musicDataBlockAddr + Common.getLE24(vgmBuf, musicPtr);
                vgmCurLoop++;
                continue;
            }

            //end command
            if (cmd == 0x7f) {
                stopped = true;
                break;
            }

            byte X = (byte) (cmd & 0xf);
            cmd &= 0xf0;

            if (cmd == 0x10) {
                //PSG register write:
                writePSG(X);
            } else if (cmd == 0x20) {
                //YM2612 port 0 register write:
                writeYM2612P0(X);
            } else if (cmd == 0x30) {
                //YM2612 port 1 register write:
                writeYM2612P1(X);
            } else if (cmd == 0x40) {
                //YM2612 key off/on ($28) command write:
                writeYM2612Key(X);
            } else if (cmd == 0x50) {
                //PCM play command:
                playPCM(X);
            }
        }
    }

    private void writePSG(byte X) {
        for (int i = 0; i < X + 1; i++) {
            byte data = vgmBuf[musicPtr++];
            chipRegister.setSN76489Register(0, data, model);
        }
    }

    private void writeYM2612P0(byte X) {
        for (int i = 0; i < X + 1; i++) {
            byte adr = vgmBuf[musicPtr++];
            byte val = vgmBuf[musicPtr++];
            if (adr == 0x2b) DACEnable = (byte) (val & 0x80);
            chipRegister.setYM2612Register(0, 0, adr, val, model, vgmFrameCounter);
        }
    }

    private void writeYM2612P1(byte X) {
        for (int i = 0; i < X + 1; i++) {
            byte adr = vgmBuf[musicPtr++];
            byte val = vgmBuf[musicPtr++];
            chipRegister.setYM2612Register(0, 1, adr, val, model, vgmFrameCounter);
        }
    }

    private void writeYM2612Key(byte X) {
        for (int i = 0; i < X + 1; i++) {
            byte val = vgmBuf[musicPtr++];
            chipRegister.setYM2612Register(0, 0, 0x28, val, model, vgmFrameCounter);
        }
    }

    public static class XgmPcm {
        public int priority = 0;
        public int startAddr = 0;
        public int endAddr = 0;
        public int addr = 0;
        public int inst = 0;
        public Boolean isPlaying = false;
        public byte data = 0;
    }

    public XgmPcm[] xgmpcm = null;
    private double pcmSpeedCounter;

    private void playPCM(byte X) {
        byte priority = (byte) (X & 0xc);
        byte channel = (byte) (X & 0x3);
        byte id = vgmBuf[musicPtr++];

        //優先度が高い場合または消音中の場合のみ発音できる
        if (xgmpcm[channel].priority <= priority || !xgmpcm[channel].isPlaying) {
            if (id == 0 || sampleID[id - 1].size == 0) {
                //IDが0の場合や、定義されていないIDが指定された場合は発音を停止する
                xgmpcm[channel].priority = 0;
                //xgmpcm[channel].startAddr = 0;
                //xgmpcm[channel].endAddr = 0;
                //xgmpcm[channel].addr = 0;
                //xgmpcm[channel].inst = id;
                xgmpcm[channel].isPlaying = false;
            } else {
                xgmpcm[channel].priority = priority;
                xgmpcm[channel].startAddr = sampleDataBlockAddr + sampleID[id - 1].addr;
                xgmpcm[channel].endAddr = sampleDataBlockAddr + sampleID[id - 1].addr + sampleID[id - 1].size;
                xgmpcm[channel].addr = sampleDataBlockAddr + sampleID[id - 1].addr;
                xgmpcm[channel].inst = id;
                xgmpcm[channel].isPlaying = true;
            }
        }
    }

    private void oneFramePCM() {
        if (DACEnable == 0) return;

        short o = 0;

        for (int i = 0; i < 4; i++) {
            if (!xgmpcm[i].isPlaying) continue;
            byte d = vgmBuf[xgmpcm[i].addr++];
            o += d;
            xgmpcm[i].data = (byte) Math.abs(d);
            if (xgmpcm[i].addr >= xgmpcm[i].endAddr) {
                xgmpcm[i].isPlaying = false;
                xgmpcm[i].data = 0;
            }
        }
        o = (short) Math.min(Math.max(o, Byte.MIN_VALUE + 1), Byte.MAX_VALUE);
        o += 0x80;

        chipRegister.setYM2612Register(0, 0, 0x2a, o, model, vgmFrameCounter);
    }
}

