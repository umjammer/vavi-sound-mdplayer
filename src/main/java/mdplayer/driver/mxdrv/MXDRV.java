// X68k MXDRV Music driver version 2.06+17 Rel.X5-S
// (c)1988-92 milk.,K.MAEKAWA, Missy.M, Yatsube
//
// Converted for Win32 [MXDRVg] V1.50a
// Copyright (C) 2000 GORRY.
// Converted for MDPlayer Vx.xx
// Copyright (C) 2018 Kumatan.

// ;=============================================
// ;  Filename mxdrv17.x
// ;  Time Stamp Sun Mar 15 11:52:06 1998
// ;
// ;  Base address 000000
// ;  Exec address 0017ea
// ;  Text size    001ba6 bytes
// ;  Data size    000000 byte(s)
// ;  Bss  size    0006a2 byte(s)
// ;  438 Labels
// ;  Code Generate date Wed May 06 12:59:13 1998
// ;  Command Line D:\FTOOL\dis.x -C2 --overwrite -7 -m 68040 -M -s8192 -e -g mxdrv17.x mxdrv17.dis
// ;          DIS version 2.75
// ;=============================================

package mdplayer.driver.mxdrv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dotnet4j.Tuple;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.Log;
import mdsound.Ym2151X68Sound;
import mdsound.x68sound.X68Sound;


// MXDRV.DLL header
// Copyright (C) 2000 GORRY.
public class MXDRV extends BaseDriver {

    public interface MXWORK_CH {
        int S0000 = 0; // Ptr
        int S0004_b = 4;// PCM bank
        int S0004 = 5; //byte[] S0004; // voice ptr
        int S0008 = 9; //public int S0008;    // bend delta
        int S000c = 13; //public int S000c;    // bend offset
        int S0010 = 17; //public short S0010;    // D
        int S0012 = 19; //public short S0012;    // note+D ( >> 6でnote )
        int S0014 = 21; //public short S0014;    // note+D+bend+Pitch LFO offset
        int S0016 = 23; //public byte S0016;    // flags b3=keyon/off
        int S0017 = 24; //public byte S0017;    // flags b0:volChg
        int S0018 = 25; //public byte S0018;    // ch
        int S0019 = 26; //public byte S0019;    // carrier slot
        int S001a = 27; //public byte S001a;    // len
        int S001b = 28; //public byte S001b;    // gate
        int S001c = 29; //public byte S001c;    // p + fb + con
        int S001d = 30; //public byte S001d;    // keyon slot
        int S001e = 31; //public byte S001e;    // Q
        int S001f = 32; //public byte S001f;    // Keyon delay
        int S0020 = 33; //public byte S0020;    // Keyon delay counter
        int S0021 = 34; //public byte S0021;    // PMS/AMS
        int S0022 = 35; //public byte S0022;    // v
        int S0023 = 36; //public byte S0023;    // v last
        int S0024 = 37; //public byte S0024;    // LFO delay
        int S0025 = 38; //public byte S0025;    // LFO delay counter
        int S0026 = 39; //volatile public byte[] S0026; // Pitch LFO Type
        int S002a = 43; //public int S002a;    // Pitch LFO offset start
        int S002e = 47; //public int S002e;    // Pitch LFO delta start
        int S0032 = 51; //public int S0032;    // Pitch LFO delta
        int S0036 = 55; //public int S0036;    // Pitch LFO offset
        int S003a = 59; //public short S003a;    // Pitch LFO length (cooked)
        int S003c = 61; //public short S003c;    // Pitch LFO length
        int S003e = 63; //public short S003e;    // Pitch LFO length counter
        int S0040 = 65; //volatile public byte[] S0040; // Volume LFO Type
        int S0044 = 69; //public short S0044;    // Volume LFO delta start
        int S0046 = 71; //public short S0046;    // Volume LFO delta (cooked)
        int S0048 = 73; //public short S0048;    // Volume LFO delta
        int S004a = 75; //public short S004a;    // Volume LFO offset
        int S004c = 77; //public short S004c;    // Volume LFO length
        int S004e = 79; //public short S004e;    // Volume LFO length counter
        int Length = 81;
    }

    public interface MXWORK_GLOBAL {
        int L001ba6 = 0; //public short L001ba6;
        int L001ba8 = 2; //public int L001ba8;
        int L001bac = 6; //volatile public byte[] L001bac;
        int L001bb4 = 10; //public byte[] L001bb4 = new byte[16];
        int L001df4 = 26; //byte L001df4;
        int L001df6 = 27; //new byte[16];
        int L001e06 = 43; //public short L001e06;  // Channel Mask (true)
        int L001e08 = 45; //public byte L001e08;
        int L001e09 = 46; //public byte L001e09;
        int L001e0a = 47; //public byte L001e0a;
        int L001e0b = 48; //public byte L001e0b;
        int L001e0c = 49; //byte L001e0c;  // @t
        int L001e0d = 50; //public byte L001e0d;
        int L001e0e = 51; //public Ref<Byte> L001e0e;
        int L001e10 = 52; //public byte L001e10;
        int L001e12 = 53; //public byte L001e12;  // Paused
        int L001e13 = 54; //public byte L001e13;  // End
        int L001e14 = 55; //byte L001e14;  // Fadeout Offset
        int L001e15 = 56; //public byte L001e15;
        int L001e17 = 57; //public byte L001e17;  // Fadeout Enable
        int L001e18 = 58; //public byte L001e18;
        int L001e19 = 59; //public byte L001e19;
        int L001e1a = 60; //public short L001e1a;  // Channel Enable
        int L001e1c = 62; //public short L001e1c;  // Channel Mask
        int L001e1e = 64; //short[2];   // Fadeout Speed
        int L001e22 = 72; //public short L001e22;
        int L001e24 = 74; //volatile public byte[] L001e24;
        int L001e28 = 78; //volatile public byte[] L001e28;
        int L001e2c = 82; //volatile public byte[] L001e2c;
        int L001e30 = 86; //volatile public byte[] L001e30;
        int L001e34 = 90; //volatile public byte[] L001e34;
        int L001e38 = 94; //volatile public byte[] L001e38;
        int L00220c = 98; //public int L00220c;
        int L002218 = 102; //volatile public byte[] L002218;
        int L00221c = 106; //volatile public byte[] L00221c;
        int L002220 = 110; //public int L002220; // L_MDXSIZE
        int L002224 = 114; //public int L002224; // L_PDXSIZE
        int L002228 = 118; //volatile public byte[] L002228;   // voice data
        int L00222c = 122; //volatile public byte[] L00222c;
        int L002230 = 126; //public byte L002230;
        int L002231 = 127; //public byte L002231;
        int L002232 = 128; //public byte L002232;
        int L002233 = 129; //public byte[] L002233 = new byte[9];
        int L00223c = 138; //new byte[12];
        int L002245 = 150; //public byte L002245;
        int L002246 = 151; //public short L002246; // loop count
        int FATALERROR = 153; //int FATALERROR;
        int FATALERRORADR = 157; //int FATALERRORADR;
        int PLAYTIME = 161; //public int PLAYTIME; // 演奏時間
        int MUSICTIMER = 165; //public byte MUSICTIMER;  // 演奏時間タイマー定数
        int STOPMUSICTIMER = 166; //public byte STOPMUSICTIMER;  // 演奏時間タイマー停止
        int MEASURETIMELIMIT = 167; //public int MEASURETIMELIMIT; // 演奏時間計測中止時間
        int Length = 171;
    }

    public interface MXWORK_KEY {
        int OPT1 = 0;
        int OPT2 = 1;
        int SHIFT = 2;
        int CTRL = 3;
        int XF3 = 4;
        int XF4 = 5;
        int XF5 = 6;
        int Length = 7;
    }

    public interface MXWORK_OPM {
        int Length = 256;
    }

    public interface MXCALLBACK_OPMINTFUNC extends Runnable {
    }

    public enum MXDRV_WORK {
        FM,      // FM8ch+PCM1ch
        PCM,         // PCM7ch
        GLOBAL,
        KEY,
        OPM,
        PCM8,
        CREDIT,
        CALLBACK_OPMINT;

        static MXDRV_WORK valueOf(int v) {
            return Arrays.stream(values()).filter(e -> e.ordinal() == v).findFirst().get();
        }
    }

    public enum MXDRV_ERR {
        MEMORY(1);
        final int v;

        MXDRV_ERR(int v) {
            this.v = v;
        }
    }

    private void MXDRV_Call(int a) {
        X68Reg reg = new X68Reg();

        reg.d0 = (a);
        reg.d1 = 0x00;
        MXDRV_(reg);
    }


    private void MXDRV_Call_2(int a, int b) {
        X68Reg reg = new X68Reg();

        reg.d0 = (a);
        reg.d1 = (b);
        MXDRV_(reg);
    }


    private void MXDRV_Replay() {
        MXDRV_Call(0x0f);
    }

    private void MXDRV_Stop() {
        MXDRV_Call(0x05);
    }

    private void MXDRV_Pause() {
        MXDRV_Call(0x06);
    }

    private void MXDRV_Cont() {
        MXDRV_Call(0x07);
    }

    private void MXDRV_Fadeout() {
        MXDRV_Call_2(0x0c, 19);
    }

    private void MXDRV_Fadeout2(int a) {
        MXDRV_Call_2(0x0c, (a));
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        Gd3 gd3 = new Gd3();

        List<Byte> lst = new ArrayList<>();
        int i = 0;
        while ((buf[i] != 0xd && buf[i] != 0xa) && i < buf.length) {
            lst.add(buf[i]);
            i++;
        }
        String n = new String(mdsound.Common.toByteArray(lst), Charset.forName("MS932"));
        gd3.trackName = n;
        gd3.trackNameJ = n;
        byte[] mdx;
        int mdxsize;
        String pdxFileName;
        makeMdxBuf(buf, mdx, mdxsize, pdxFileName);

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

        gd3 = getGD3Info(vgmBuf, 0);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

        for (int chipID = 0; chipID < 2; chipID++) {
            ym2151Hosei[chipID] = Common.GetYM2151Hosei(4000000, 3579545);
            if (model == EnmModel.RealModel) {
                ym2151Hosei[chipID] = 0;
                int clock = chipRegister.getYM2151Clock((byte) chipID);
                if (clock != -1) {
                    ym2151Hosei[chipID] = Common.GetYM2151Hosei(4000000, clock);
                }
            }
        }

        //byte[] mdx;
        //int mdxsize;
        //int mdxPtr;
        //byte[] pdx;
        //int pdxsize;
        //int pdxPtr;
        //String pdxFileName;
        //MakeMdxBuf(vgmBuf,  mdx,  mdxsize,  pdxFileName);
        //MakePdxBuf(pdxFileName,  pdx,  pdxsize);
        //if (pdxFileName != null && pdxFileName.isEmpty() && pdx == null) return false;

        //int ret;
        //ret = MXDRV_Start(setting.getoutputDevice().SampleRate, 0, 0, 0, 64 * 1024, 1024 * 1024, 0);

        //int memind = (int)mm.mm.length;
        //mdxPtr = memind;
        //memind += mdxsize;
        //pdxPtr = memind;
        //memind += pdxsize;
        //mm.realloc(memind);
        //for (int i = 0; i < mdxsize; i++) mm.Write(mdxPtr + i, mdx[i]);
        //for (int i = 0; i < pdxsize; i++) mm.Write(pdxPtr + i, pdx[i]);

        //chipRegister.x68Sound_MountMemory(mm.mm,model);

        //int playtime=MXDRV_MeasurePlayTime(mdx, mdxsize, mdxPtr, pdx, pdxsize, pdxPtr, 1, Depend.TRUE);
        //System.err.println("(%d:%02d) %d", playtime / 1000 / 60, playtime / 1000 % 60, "");
        //MXDRV_Play(mdx, mdxsize, mdxPtr, pdx, pdxsize, pdxPtr);

        //System.err.println("********************");

        return true;
    }

    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime, Ym2151X68Sound mdxPCM) {
        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;
        this.mdxPCM = mdxPCM;

        gd3 = getGD3Info(vgmBuf, 0);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

        for (int chipID = 0; chipID < 2; chipID++) {
            ym2151Hosei[chipID] = Common.GetYM2151Hosei(4000000, 3579545);
            if (model == EnmModel.RealModel) {
                ym2151Hosei[chipID] = 0;
                int clock = chipRegister.getYM2151Clock((byte) chipID);
                if (clock != -1) {
                    ym2151Hosei[chipID] = Common.GetYM2151Hosei(4000000, clock);
                }
            }
        }

        byte[][] mdx = new byte[1][];
        int[] mdxsize = new int[1];
        int mdxPtr;
        byte[][] pdx = new byte[1][];
        int[] pdxsize = new int[1];
        int pdxPtr;
        String[] pdxFileName = new String[1];
        makeMdxBuf(vgmBuf, mdx, mdxsize, pdxFileName);
        makePdxBuf(pdxFileName[0], pdx, pdxsize);
        if ((pdxFileName[0] == null || pdxFileName[0].isEmpty()) && pdx[0] == null) {
            errMsg = String.format("PCMファイル[%s]の読み込みに失敗しました。", pdxFileName[0]);
            return false;
        }

        int ret;
        if (model == EnmModel.VirtualModel) {
            //ret = MXDRV_Start(setting.getoutputDevice().SampleRate, 0, 0, 0, 64 * 1024, 1024 * 1024, 0, -1, model == enmModel.VirtualModel ? 1 : -1);
            ret = MXDRV_Start(setting.getOutputDevice().getSampleRate(), 0, 0, 0, mdxsize[0], pdxsize[0], 0, -1, 1);
        } else {
            ret = MXDRV_Start(setting.getOutputDevice().getSampleRate(), 0, 0, 0, mdxsize[0], pdxsize[0], 0, -1, -1);
        }
        int memind = mm.mm.length;
        mdxPtr = memind;
        memind += mdxsize[0];
        pdxPtr = memind;
        memind += pdxsize[0];
        mm.realloc(memind);
        for (int i = 0; i < mdxsize[0]; i++) mm.write(mdxPtr + i, mdx[0][i]);
        for (int i = 0; i < pdxsize[0]; i++) mm.write(pdxPtr + i, pdx[0][i]);

        mdxPCM.x68sound[0].MountMemory(mm.mm);

        int playtime = MXDRV_MeasurePlayTime(mdx[0], mdxsize[0], mdxPtr, pdx[0], pdxsize[0], pdxPtr, 1, Depend.TRUE);
        //System.err.println("(%d:%02d) %d", playtime / 1000 / 60, playtime / 1000 % 60, "");
        totalCounter = (long) playtime * setting.getOutputDevice().getSampleRate() / 1000;
        terminatePlay = false;
        MXDRV_Play(mdx[0], mdxsize[0], mdxPtr, pdx[0], pdxsize[0], pdxPtr);

        //System.err.println("********************");

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    short[] dummyBuf = new short[2];

    @Override
    public void oneFrameProc() {
        Render(dummyBuf, 0, 2);
    }

    public void oneFrameProc2(Runnable timer, boolean firstFlg) {

        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    timer.run();
                    if (firstFlg) {
                        counter++;
                        vgmFrameCounter++;
                    }
                } else {
                    if (firstFlg)
                        vgmFrameCounter++;
                }
            }

            MXDRV_MeasurePlayTime_OPMINT();
            vgmCurLoop = loopCount;
            if (terminatePlay) {
                stopped = true;
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    public int Render(short[] buffer, int offset, int sampleCount) {
        if (mdxPCM == null) {
            return 0;
        }
        int ret = mdxPCM.x68sound[0].getPcm(buffer, offset, sampleCount, this::oneFrameProc2);

        //System.err.println("0:%08x", mm.Readint(MXWORK_CHBUF_FM[8] + MXWORK_CH.S0012));
        //System.err.println("1:%04x", mm.Readshort(MXWORK_CHBUF_PCM[0] + MXWORK_CH.S0012) >> 6);
        //System.err.println("1a:%04x", mm.Readshort(MXWORK_CHBUF_PCM[0] + MXWORK_CH.S0014) >> 6);
        //System.err.println("2:%d", mm.Readint(MXWORK_CHBUF_PCM[1] + MXWORK_CH.S0004));
        //System.err.println("3:%d", mm.Readint(MXWORK_CHBUF_PCM[2] + MXWORK_CH.S0004));
        //System.err.println("4:%d", mm.Readint(MXWORK_CHBUF_PCM[3] + MXWORK_CH.S0004));
        //System.err.println("5:%d", mm.Readint(MXWORK_CHBUF_PCM[4] + MXWORK_CH.S0004));
        //System.err.println("6:%d", mm.Readint(MXWORK_CHBUF_PCM[5] + MXWORK_CH.S0004));
        //System.err.println("7:%d", mm.Readint(MXWORK_CHBUF_PCM[6] + MXWORK_CH.S0004));

        return ret;
    }

    public MXDRV() {
        ini();
    }

    /**
     * @param mdx OUT
     * @param mdxSize OUT
     * @param pdxFileName OUT
     */
    private void makeMdxBuf(byte[] buf, byte[][] mdx, int[] mdxSize, String[] pdxFileName) {
        // タイトルをスキップ
        int p = 8;
        byte c = 0;
        mdx[0] = new byte[buf.length + 8];
        System.arraycopy(buf, 0, mdx[0], 8, mdxSize[0]);

        while (true) {
            c = mdx[0][p++];
            if (c == 0x0d) break;
            if (c == 0x0a) break;
            if (c < 0x20) {
                if (c != 0x1b) throw new IllegalStateException();
            }
        }
        p--;
        mdx[0][p++] = 0x00;
        if ((p & 0x01) != 0) {
            mdx[0][p++] = 0x00;
        }
        int p2 = p;
        if (c != 0x0d) {
            while (mdx[0][p2++] != 0x0d) ;
        }

        // PDXを読み込む
        byte havepdx = (byte) 0xff;
        List<Byte> lstPdxfileName;
        while (mdx[0][p2++] != 0x1a) ;
        if (mdx[0][p2] != 0) {
            havepdx = 0x00;
            lstPdxfileName = new ArrayList<>();
            while (mdx[0][p2] != 0x00) {
                lstPdxfileName.add(mdx[0][p2]);
                p2++;
            }
            pdxFileName[0] = new String(mdsound.Common.toByteArray(lstPdxfileName), Charset.forName("MS932"));
        }
        p2++;

        // MDXをMXDRVへ渡せるよう加工する
        int mdxBodyPtr = p;
        while (p2 < mdx[0].length) {
            mdx[0][p++] = mdx[0][p2++];
        }
        mdxSize[0] = p;

        mdx[0][0] = 0x00;
        mdx[0][1] = 0x00;
        mdx[0][2] = havepdx;
        mdx[0][3] = havepdx;
        mdx[0][4] = (byte) (mdxBodyPtr >> 8);
        mdx[0][5] = (byte) mdxBodyPtr;
        mdx[0][6] = 0x00;
        mdx[0][7] = 0x08;
    }

    /**
     * @param pdx OUT
     * @param pdxsize OUT
     */
    private void makePdxBuf(String pdxFileName, byte[][] pdx, int[] pdxsize) {
        if (extendFile == null) return;

        pdx[0] = new byte[extendFile.Item2.length + pdxFileName.length() + 8 + 1];
        System.arraycopy(pdxFileName.getBytes(StandardCharsets.US_ASCII), 0, pdx[0], 8, pdxFileName.length());
        System.arraycopy(extendFile.Item2, 0, pdx[0], 8 + pdxFileName.length() + 1, extendFile.Item2.length);
        pdx[0][0] = 0x00;
        pdx[0][1] = 0x00;
        pdx[0][2] = 0x00;
        pdx[0][3] = 0x00;
        pdx[0][4] = (byte) (((8 + pdxFileName.length() + 2) & 0xfffffffe) >> 8);
        pdx[0][5] = (byte) ((8 + pdxFileName.length() + 2) & 0xfffffffe);
        pdx[0][4] = (byte) ((8 + pdxFileName.length() + 1) >> 8);
        pdx[0][5] = (byte) (8 + pdxFileName.length() + 1);
        pdx[0][6] = (byte) ((pdxFileName.length() + 1) >> 8);
        pdx[0][7] = (byte) (pdxFileName.length() + 1);
        pdxsize[0] = pdx[0].length;
    }

    public static void getPDXFileName(byte[] buf, String[] pdx) {
        int p = 0;
        byte c;
        while (true) {
            c = buf[p++];
            if (c == 0x0d || c == 0x0a) break;
            if (c < 0x20 && c != 0x1b) throw new IllegalStateException();
        }
        if ((p & 0x01) != 0) p++;
        if (c != 0x0d) while (buf[p++] != 0x0d) ;
        while (buf[p++] != 0x1a) ;
        if (buf[p] == 0) return;
        List<Byte> lstPdxfileName = new ArrayList<>();
        while (buf[p] != 0x00) lstPdxfileName.add(buf[p++]);
        pdx[0] = new String(mdsound.Common.toByteArray(lstPdxfileName), Charset.forName("MS932"));
    }

    //private double deltaCnt = 0;
    private XMemory mm = null;
    public String playingFileName = "";
    public Tuple<String, byte[]> extendFile = null;
    public int timerA = 0, timerB = 0;
    mdsound.Ym2151X68Sound mdxPCM = null;

    // OPM レジスタ $1B の内容
    private byte opmReg1B;

    private static final String MXWORK_CREDIT
            = "X68k MXDRV Music driver version 2.06+17 Rel.X5-S (c)1988-92 milk.,K.MAEKAWA, Missy.M, Yatsube\nConverted for Win32 [MXDRVg] V2.00a Copyright (C) 2000-2002 GORRY.\nConverted for MDPlayer Vx.xx Copyright (C) 2018-2018 Kumatan.";

    private int[] MXWORK_CHBUF_FM = new int[9];
    private int[] MXWORK_CHBUF_PCM = new int[7];

    //        static  MXWORK_GLOBAL MXWORK_GLOBALBUF;
    private int G = 0;

    //        static  MXWORK_KEY MXWORK_KEYBUF;
    private int KEY = 0;

    //        static  MXWORK_OPM MXWORK_OPMBUF;
    public int OPMBUF = 0;

    //        static  byte MXWORK_PCM8;
    //private MXWORK_PCM8 PCM8 = null;
    private int PCM8 = 1;

    //

    private int FAKEA6S0004 = 0;

    private int D0;
    private int D1;
    private int D2;
    private int D3;
    private int D4;
    private int D5;
    private int D6;
    private int D7;

    private int A0;
    private int A1;
    private int A2;
    private int A3;
    private int A4;
    private int A5;
    private int A6;
    private int A7;

    private Byte DisposeStack_L00122e;

    private Runnable OPMINT_FUNC;
    private Runnable MXCALLBACK_OPMINT;

    private boolean measurePlayTime;

    private final Object CS_OPMINT = new Object();

    private static final byte[] CarrierSlot = new byte[] {
            0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f,
    };

    private static final byte[] KeyCode = new byte[] {
            0x00, 0x01, 0x02, 0x04, 0x05, 0x06, 0x08, 0x09,
            0x0a, 0x0c, 0x0d, 0x0e, 0x10, 0x11, 0x12, 0x14,
            0x15, 0x16, 0x18, 0x19, 0x1a, 0x1c, 0x1d, 0x1e,
            0x20, 0x21, 0x22, 0x24, 0x25, 0x26, 0x28, 0x29,
            0x2a, 0x2c, 0x2d, 0x2e, 0x30, 0x31, 0x32, 0x34,
            0x35, 0x36, 0x38, 0x39, 0x3a, 0x3c, 0x3d, 0x3e,
            0x40, 0x41, 0x42, 0x44, 0x45, 0x46, 0x48, 0x49,
            0x4a, 0x4c, 0x4d, 0x4e, 0x50, 0x51, 0x52, 0x54,
            0x55, 0x56, 0x58, 0x59, 0x5a, 0x5c, 0x5d, 0x5e,
            0x60, 0x61, 0x62, 0x64, 0x65, 0x66, 0x68, 0x69,
            0x6a, 0x6c, 0x6d, 0x6e, 0x70, 0x71, 0x72, 0x74,
            0x75, 0x76, 0x78, 0x79, 0x7a, 0x7c, 0x7d, 0x7e,
    };

    private static final byte[] Volume = new byte[] {
            0x2a, 0x28, 0x25, 0x22, 0x20, 0x1d, 0x1a, 0x18,
            0x15, 0x12, 0x10, 0x0d, 0x0a, 0x08, 0x05, 0x02,
    };

    private static final byte[] PCMVolume = new byte[] {
            0x0f, 0x0f, 0x0f, 0x0e, 0x0e, 0x0e, 0x0d, 0x0d,
            0x0d, 0x0c, 0x0c, 0x0b, 0x0b, 0x0b, 0x0a, 0x0a,
            0x0a, 0x09, 0x09, 0x08, 0x08, 0x08, 0x07, 0x07,
            0x07, 0x06, 0x06, 0x05, 0x05, 0x05, 0x04, 0x04,
            0x04, 0x03, 0x03, 0x02, 0x02, 0x02, 0x01, 0x01,
            0x01, 0x00, 0x00, (byte) 0xff,
    };

    private int L0019b2;
    Runnable[] L001252;
    Runnable[] jumptable;
    Runnable[] L0016aa;
    Runnable[] L0010b4_Table;
    Runnable[] L001116Table;

    private void ini() {

        L001252 = new Runnable[] {
                this::L001292,    // @@ @t
                this::L0012a6,
                this::L0012be,    // @@ @
                this::L0012e6,    // @@ p
                this::L00131c,    // @@ v
                this::L001328,
                this::L001344,
                this::L001364,
                this::L00136a,
                this::L001372,
                this::L001376,
                this::L00139a,
                this::L0013ba,    // @@ D
                this::L0013c6,
                this::L0013dc,
                this::L001492,
                this::L001498,
                this::L0014b0,
                this::L0014dc,
                this::L0014fc,
                this::L001590,
                this::L0015fe,
                this::L001656,
                this::L00165c,
                this::L001694,
                this::L001442,
                this::L001442,
                this::L001442,
                this::L001442,
                this::L001442,
                this::L001442,
                this::L001442,
        };

        jumptable = new Runnable[] {
                this::L_FREE,
                this::L_ERROR,
                this::L_SETMDX,
                this::L_SETPDX,
                this::L_PLAY,
                this::L_STOP,
                this::L_PAUSE,
                this::L_CONT,
                this::L_08,
                this::L_09,
                this::L_0A,
                this::L_0B,
                this::L_0C,
                this::L_0D,
                this::L_0E,
                this::L_0F,
                this::L_10,
                this::L_11,
                this::L_12,
                this::L_13,
                this::L_14,
                this::L_15,
                this::L_16,
                this::L_17,
                this::L_18,
                this::L_19,
                this::L_1A,
                this::L_1B,
                this::L_1C,
                this::L_1D,
                this::L_1E,
                this::L_1F,
        };

        L0016aa = new Runnable[] {
                        this::L001442,
                        this::L0016b8,
                        this::L0016c6,
                        this::L0016fa,
                        this::L00170e,
                        this::L00178a,
                        this::L0017a0,
                };

        L0010b4_Table = new Runnable[] {
                        this::L00095a,
                        this::L0010be,
                        this::L0010d4,
                        this::L0010ea,
                        this::L001100,
                };

        L001116Table = new Runnable[] {
                this::L00095a,
                this::L001120,
                this::L001138,
                this::L00114e,
                this::L001164,
        };
    }

    private int MXDRV_Start(
            int samprate,
            int betw,
            int pcmbuf,
            int late,
            int mdxbuf,
            int pdxbuf,
            int opmmode,
            int opmflag/*=1*/,
            int adpcmflag/*=1*/
    ) {
        int ret;
        CS_OPMINT = new Object();

        int memInd = 0;
        G = memInd;
        memInd += MXWORK_GLOBAL.Length;
        KEY = memInd;
        memInd += MXWORK_KEY.Length;
        OPMBUF = memInd;
        memInd += MXWORK_OPM.Length;
        for (int i = 0; i < MXWORK_CHBUF_FM.length; i++) {
            MXWORK_CHBUF_FM[i] = memInd;
            memInd += MXWORK_CH.Length;
        }
        for (int i = 0; i < MXWORK_CHBUF_PCM.length; i++) {
            MXWORK_CHBUF_PCM[i] = memInd;
            memInd += MXWORK_CH.Length;
        }

        L0019b2 = memInd;
        memInd += 3;
        mm = new XMemory();
        mm.alloc(memInd);

        mm.write(G + MXWORK_GLOBAL.MEASURETIMELIMIT, (int) ((long) ((1000 * (60 * 20 - 2))) * 4000 / 1024)); // 20min-2sec
        mm.write(L0019b2 + 0, 0x7f);
        mm.write(L0019b2 + 1, 0xf1);
        mm.write(L0019b2 + 2, 0x00);

        ret = 0;// x68Sound.Load();
        if (ret != 0) {
            switch (ret) {
            case (int) X68Sound.SNDERR_DLL:
            case (int) X68Sound.SNDERR_FUNC:
            default:
                return (10000 + ret);
            }
        }

        if (opmmode > 1) opmmode = 0;
        if (opmmode < 0) opmmode = 0;

        if (betw != 0) {
            ret = mdxPCM.x68sound[0].start(samprate, opmmode + 1, 1, betw, pcmbuf, late, 1.0);
        } else {
            ret = mdxPCM.x68sound[0].startPcm(samprate, opmflag, adpcmflag, pcmbuf);
        }
        if (ret != 0) {
            switch (ret) {
            case (int) X68Sound.SNDERR_PCMOUT:
            case (int) X68Sound.SNDERR_TIMER:
            case (int) X68Sound.SNDERR_MEMORY:
                return (10100 + ret);
            }
        }

        mdxPCM.sound_Iocs[0].init();
        ret = initialize(mdxbuf, pdxbuf, memInd);
        if (ret != 0) {
            return MXDRV_ERR.MEMORY.ordinal();
        }

        return (0);
    }

    private void MXDRV_End() {
        mdxPCM.x68sound[0].opmInt(null);
        MXCALLBACK_OPMINT = null;
        OPMINT_FUNC = null;

        //if (G.L001e34 != null) {
        //    G.L001e34 = null;
        //}
        //if (G.L001e38 != null) {
        //    G.L001e38 = null;
        //}
        //if (G.L001bac != null) {
        //    G.L001bac = null;
        //}

        //G = new MXWORK_GLOBAL();
        //KEY = new MXWORK_KEY();
        //FAKEA6S0004 = new byte[FAKEA6S0004.Length];
        //D0 = D1 = D2 = D3 = D4 = D5 = D6 = D7 = null;
        //A0 = A1 = A2 = A3 = A4 = A5 = A7 = null;
        //A6 = null;
        DisposeStack_L00122e = null;
        //MXWORK_CHBUF_FM = new MXWORK_CH[MXWORK_CHBUF_FM.Length];
        //MXWORK_CHBUF_PCM =new MXWORK_CH[MXWORK_CHBUF_PCM.Length];
        //OPMBUF = new byte[OPMBUF.Length];
        //PCM8 = null;

        CS_OPMINT = null;

        mdxPCM.x68sound[0].free();
    }

    private int MXDRV_GetPCM(short[] buf, int len) {
        return mdxPCM.x68sound[0].getPcm(buf, 0, len);
    }

    private int MXDRV_TotalVolume(int vol) {
        return mdxPCM.x68sound[0].totalVolume(vol);
    }

    private void MXDRV_Play(
            byte[] mdx,
            int mdxsize,
            int mdxPtr,
            byte[] pdx,
            int pdxsize,
            int pdxPtr
    ) {
        X68Reg reg = new X68Reg();

        reg.d0 = 0x02;
        reg.d1 = mdxsize;
        reg.a1 = mdxPtr;
        MXDRV_(reg);

        if (pdx != null) {
            reg.d0 = 0x03;
            reg.d1 = pdxsize;
            reg.a1 = pdxPtr;
            MXDRV_(reg);
        } else {
            mm.write(G + MXWORK_GLOBAL.L002231, (byte) Depend.CLR);
        }

        reg.d0 = 0x0f;
        reg.d1 = 0x00;
        MXDRV_(reg);
    }

    private Object MXDRV_GetWork(int i) {
        switch (MXDRV_WORK.valueOf(i)) {
        case FM:
            return MXWORK_CHBUF_FM[0];
        case PCM:
            return MXWORK_CHBUF_PCM[0];
        case GLOBAL:
            return G; // MXWORK_GLOBALBUF;
        case KEY:
            return KEY; // MXWORK_KEYBUF;
        case OPM:
            return OPMBUF; // MXWORK_OPMBUF;
        case PCM8:
            return PCM8; // MXWORK_PCM8;
        case CREDIT:
            return MXWORK_CREDIT;
        case CALLBACK_OPMINT:
            return MXCALLBACK_OPMINT;
        }
        return null;
    }

    //

    private boolean terminatePlay;
    private int loopCount;
    private int loopLimit;
    private boolean fadeoutStart;
    private boolean reqFadeout;

    private void MXDRV_MeasurePlayTime_OPMINT() {
        if (mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) >= mm.readInt(G + MXWORK_GLOBAL.MEASURETIMELIMIT)) {
            terminatePlay = true;
        }
        if (mm.readByte(G + MXWORK_GLOBAL.L001e13) != 0) {
            terminatePlay = true;
        }
        if (mm.readShort(G + MXWORK_GLOBAL.L002246) == 65535) {
            terminatePlay = true;
        } else {
            loopCount = mm.readShort(G + MXWORK_GLOBAL.L002246);
            if (!fadeoutStart) {
                if (loopCount >= loopLimit) {
                    if (reqFadeout) {
                        fadeoutStart = true;
                        MXDRV_Fadeout();
                    } else {
                        terminatePlay = true;
                    }
                }
            }
        }
    }

    private int MXDRV_MeasurePlayTime(
            byte[] mdx,
            int mdxsize,
            int mdxPtr,
            byte[] pdx,
            int pdxsize,
            int pdxPtr,
            int loop,
            int fadeout
    ) {
        X68Reg reg = new X68Reg();
        Runnable opmintback;

        mdxPCM.x68sound[0].opmInt(null);

        measurePlayTime = true;
        terminatePlay = false;
        loopCount = 0;
        loopLimit = loop;
        fadeoutStart = false;
        reqFadeout = fadeout != 0;

        opmintback = MXCALLBACK_OPMINT;
        MXCALLBACK_OPMINT = this::MXDRV_MeasurePlayTime_OPMINT;

        reg.d0 = 0x02;
        reg.d1 = mdxsize;
        reg.a1 = mdxPtr;
        MXDRV_(reg);

        if (pdx != null) {
            reg.d0 = 0x03;
            reg.d1 = pdxsize;
            reg.a1 = pdxPtr;
            MXDRV_(reg);
        } else {
            mm.write(G + MXWORK_GLOBAL.L002231, (byte) Depend.CLR);
        }

        reg.d0 = 0x0f;
        reg.d1 = 0xffffffff;
        MXDRV_(reg);

        while (!terminatePlay) OPMINTFUNC();

        MXDRV_Stop();

        MXCALLBACK_OPMINT = opmintback;
        measurePlayTime = false;
        mdxPCM.x68sound[0].opmInt(this::OPMINTFUNC);

        return ((int) (mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) * (long) 1024 / 4000 + (1 - Math.ulp(1.0))) + 2000);
    }

    //
    private void MXDRV_PlayAt(
            int playat,
            int loop,
            int fadeout
    ) {
        X68Reg reg = new X68Reg();
        Runnable opmintback;
        short chmaskback;
        int opmwaitback;

        mdxPCM.x68sound[0].opmInt(null);

        terminatePlay = false;
        loopCount = 0;
        loopLimit = loop;
        fadeoutStart = false;
        reqFadeout = fadeout != 0;

        playat = (int) (playat * (long) 4000 / 1024);

        opmintback = MXCALLBACK_OPMINT;
        MXCALLBACK_OPMINT = this::MXDRV_MeasurePlayTime_OPMINT;
        chmaskback = mm.readShort(G + MXWORK_GLOBAL.L001e1c);

        reg.d0 = 0x0f;
        reg.d1 = 0xffffffff;
        MXDRV_(reg);

        opmwaitback = mdxPCM.x68sound[0].opmWait(-1);
        mdxPCM.x68sound[0].opmWait(1);
        while (mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) < playat) {
            if (terminatePlay) break;
            OPMINTFUNC();
        }
        mdxPCM.x68sound[0].opmWait(opmwaitback);

        mm.write(G + MXWORK_GLOBAL.L001e1c, chmaskback);
        MXCALLBACK_OPMINT = opmintback;
        mdxPCM.x68sound[0].opmInt(this::OPMINTFUNC);
    }

    //

    private void PCM8_SUB() {
        if (measurePlayTime) return;

        switch (D0 & 0xfff0) {
        case 0x0000:
            //x68Sound.Pcm8_Out((int)D0 & 0xff, A1, (int)D1, (int)D2);
            mdxPCM.x68sound[0].pcm8Out(D0 & 0xff, null, A1, D1, D2);
            break;
        case 0x0100:
            switch (D0 & 0xffff) {
            case 0x0100:
                mdxPCM.x68sound[0].pcm8Out(D0 & 0xff, null, 0, 0, 0);
                break;
            case 0x0101:
                mdxPCM.x68sound[0].pcm8Abort();
                break;
            }
            break;
        case 0x01F0:
            switch (D0 & 0xffff) {
            case 0x01FC:
                D0 = 1;
                break;
            }
            break;
        }
    }

    //
    private void OPM_SUB() {
// #if LOGOPM
//                    FILE* fout;
//                    fout = fopen("c:\\temp\\opm.Log", "ab+");
//                    fprintf(fout, "%02X %02X\n", D1 & 0xff, D2 & 0xff);
//                    fclose(fout);
// #endif
        if (measurePlayTime) return;

        //Debug.WriteLine("%02x %02x", D1 & 0xff, D2 & 0xff);

        mdxPCM.sound_Iocs[0].opmSet((byte) D1, (byte) D2);
        chipRegister.setYM2151Register(0, 0, D1, D2, model, ym2151Hosei[0], 0);

        if (D1 == 0x10) {
            timerA = ((byte) D2 << 2) + (timerA & 0x3);
        } else if (D1 == 0x11) {
            timerA = (D2 & 0x3) + (timerA & 0x3fc);
        } else if (D1 == 0x12) {
            timerB = (byte) D2;
        } else if (D1 == 0x14) {
            //TimerABFlag = (byte)D2;
        }
    }

    //
    private void ADPCMOUT() {
        mdxPCM.sound_Iocs[0].adpcmOut(A1, D1, D2);
    }

    private void ADPCMMOD_STOP() {
        mdxPCM.sound_Iocs[0].adpcmMod(1);
    }

    private void ADPCMMOD_END() {
        mdxPCM.sound_Iocs[0].adpcmMod(0);
    }

    //
    private void OPMINTFUNC() {
        synchronized (CS_OPMINT) {
            OPMINT_FUNC.run();
            if (mm.readByte(G + MXWORK_GLOBAL.STOPMUSICTIMER) == 0) {
                mm.write(G + MXWORK_GLOBAL.PLAYTIME,
                        mm.readInt(G + MXWORK_GLOBAL.PLAYTIME)
                                + (256 - mm.readByte(G + MXWORK_GLOBAL.MUSICTIMER))
                ); // OPMBUF[0x12];
            }
            if (MXCALLBACK_OPMINT != null) MXCALLBACK_OPMINT.run();
        }

    }

    private void SETOPMINT(Runnable func) {
        OPMINT_FUNC = func;
        mdxPCM.x68sound[0].opmInt(this::OPMINTFUNC);
    }

    //
    private void MX_ABORT() {
    }

    //
        /*
        L000000:;
                .dc.b   'EX17'
        L000004:;
                .dc.b   'mxdrv206'
        */

    //
        /*
        L00000c:;
                movem.l d1-d7/a0-a6,-(sp)
                lea.l   L00220c(pc),a5
                cmp.b   // #$20,d0                 ;' '
                bcc     L000024
                add.w   d0,d0
                move.w  L00002a(pc,d0.w),d0
                jsr     L00002a(pc,d0.w)
        L000024:;
                movem.l (sp)+,d1-d7/a0-a6
                rte

        L00002a:;
                .dc.w   L_FREE-L00002a
                .dc.w   L_ERROR-L00002a
                .dc.w   L_SETMDX-L00002a
                .dc.w   L_SETPDX-L00002a
                .dc.w   L_PLAY-L00002a
                .dc.w   L_STOP-L00002a
                .dc.w   L_PAUSE-L00002a
                .dc.w   L_CONT-L00002a
                .dc.w   L_08-L00002a
                .dc.w   L_09-L00002a
                .dc.w   L_0A-L00002a
                .dc.w   L_0B-L00002a
                .dc.w   L_0C-L00002a
                .dc.w   L_0D-L00002a
                .dc.w   L_0E-L00002a
                .dc.w   L_0F-L00002a
                .dc.w   L_10-L00002a
                .dc.w   L_11-L00002a
                .dc.w   L_12-L00002a
                .dc.w   L_13-L00002a
                .dc.w   L_14-L00002a
                .dc.w   L_15-L00002a
                .dc.w   L_16-L00002a
                .dc.w   L_17-L00002a
                .dc.w   L_18-L00002a
                .dc.w   L_19-L00002a
                .dc.w   L_1A-L00002a
                .dc.w   L_1B-L00002a
                .dc.w   L_1C-L00002a
                .dc.w   L_1D-L00002a
                .dc.w   L_1E-L00002a
                .dc.w   L_1F-L00002a
        */
    private void MXDRV_(X68Reg reg) {
        D0 = reg.d0;
        D1 = reg.d1;
        D2 = reg.d2;
        D3 = reg.d3;
        D4 = reg.d4;
        D5 = reg.d5;
        D6 = reg.d6;
        D7 = reg.d7;
        A0 = reg.a0;
        A1 = reg.a1;
        A2 = reg.a2;
        A3 = reg.a3;
        A4 = reg.a4;
        A5 = reg.a5;
        A6 = reg.a6;
        A7 = reg.a7;

        if (D0 >= 0x20) return;
        jumptable[D0].run();

        reg.d0 = D0;
        reg.d1 = D1;
        reg.d2 = D2;
        reg.d3 = D3;
        reg.d4 = D4;
        reg.d5 = D5;
        reg.d6 = D6;
        reg.d7 = D7;
        reg.a0 = A0;
        reg.a1 = A1;
        reg.a2 = A2;
        reg.a3 = A3;
        reg.a4 = A4;
        reg.a5 = A5;
        reg.a6 = A6;
        reg.a7 = A7;
    }

    //
        /**
        L_0A:;
                move.b  d1,(L001e14)
                rts
        */
    private void L_0A() {
        mm.write(G + MXWORK_GLOBAL.L001e14, (byte) D1);
    }

    //
        /**
        L_0B:;
                move.b  d1,(L001e15)
                rts
        */
    private void L_0B() {
        mm.write(G + MXWORK_GLOBAL.L001e15, (byte) D1);
    }

    //
        /*
        L_0C:;
                move.w  d1,(L001e1e)
                st.b    (L001e17)
                rts
        */
    private void L_0C() {
        mm.write(G + MXWORK_GLOBAL.L001e1e, (short) D1);
        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
    }

    //
        /*
        L_0E:;
                move.w  d1,(L001e1c)
                rts
        */
    private void L_0E() {
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) D1);
    }

    //
        /*
        L_10:;
                lea.l   OPMBUF(pc),a0
                move.l  a0,d0
                rts
        */
    private void L_10() {
        A0 = OPMBUF;
        D0 = A0;
    }

    //
        /*
        L_11:;
                lea.l   L001e0e(pc),a0
                tst.l   d1
                bmi     L00009a
                move.b  d1,(a0)
                rts

        L00009a:;
                move.b  (a0),d0
                rts
        */
    private void L_11() {
        if (D1 < 0) {
            mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) D1);
        } else {
            D0 = mm.readByte(G + MXWORK_GLOBAL.L001e0e);
        }
    }

    //
        /*
        L_12:;
                move.b  (L001e12),-(sp)
                move.w  (sp)+,d0
                move.b  (L001e13),d0
                rts
        */
    private void L_12() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e12) * 256
                + mm.readByte(G + MXWORK_GLOBAL.L001e13);
    }

    //
        /*
        L_13:;
                move.b  (L001e0a),d0
                move.b  d1,(L001e0a)
                rts
        */
    private void L_13() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e0a);
        mm.write(G + MXWORK_GLOBAL.L001e0a, (byte) D1);
    }

    //
        /*
        L_14:;
                move.w  (L001e06),d0
                not.w   d0
                rts
        */
    private void L_14() {
        D0 = (short) ~mm.readShort(G + MXWORK_GLOBAL.L001e06);
    }

    //
        /*
        L_15:;
                move.b  (L001e0b),d0
                move.b  d1,(L001e0b)
                rts
        */
    private void L_15() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e0b);
        mm.write(G + MXWORK_GLOBAL.L001e0b, (byte) D1);
    }


    //
        /*
        L_16:;
                move.b  (L001e08),d0
                move.b  d1,(L001e08)
                bsr     L_STOP
                rts
        */
    private void L_16() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e08);
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) D1);
        L_STOP();
    }

    //
    private void L_17() {
            /*
                                                                    move.b  (L001e08),d0
                                                                    beq     L0001ee
            */
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e08);
        if (D0 == 0) {

                /*L0001ee:;

                                                                        move.b  (L001e12),-(sp)
                                                                        move.w  (sp)+,d0
                                                                        move.b  (L001e13),d0
                                                                        rts
                */
            D0 = mm.readByte(G + MXWORK_GLOBAL.L001e12) * 256
                    + mm.readByte(G + MXWORK_GLOBAL.L001e13);
            return;
        }

            /*
                                                                    ; fall down;
            */

        L0000dc();
    }

    private void L0000dc() {
        int[] a0_w = new int[2];
            /*L0000dc:;

                                                                    movem.l d1-d7/a0-a6,-(sp)
                                                                    st.b    (L002245)
                                                                    lea.l   L001e1e(pc),a0
                                                                    lea.l   L001e17(pc),a1
                                                                    tst.b   (a1)
                                                                    beq     L000174
                                                                    bpl     L0000fc
                                                                    move.b  // #$7f,(a1)
                                                                    move.w  (a0),$0002(a0)
            */
        int d1 = D1;
        int d2 = D2;
        int d3 = D3;
        int d4 = D4;
        int d5 = D5;
        int d6 = D6;
        int d7 = D7;
        int a0 = A0;
        int a1 = A1;
        int a2 = A2;
        int a3 = A3;
        int a4 = A4;
        int a5 = A5;
        int a6 = A6;

        mm.write(G + MXWORK_GLOBAL.L002245, (byte) Depend.SET);
        a0_w[0] = G + MXWORK_GLOBAL.L001e1e;
        A1 = G + MXWORK_GLOBAL.L001e17;
L000174:
        if (mm.readByte(A1) != 0) { // break L000174;
            if (mm.readByte(A1) < 0) { // break L0000fc;
                mm.write(A1, (byte) 0x7f);
                a0_w[1] = a0_w[0];
            }
// L0000fc:
            /*
                                                                    tst.w   $0002(a0)
                                                                    bmi     L000108
                                                                    subq.w  // #2,$0002(a0)
                                                                    bra     L000174
            */
            if (a0_w[1] >= 0) { // break L000108;
                a0_w[1] -= 2;
//                break L000174;
            } else {
// L000108:
            /*
                                                                    lea.l   L001e14(pc),a1
                                                                    cmpi.b  // #$0a,(a1)
                                                                    bge     L000120
            */
                A1 = G + MXWORK_GLOBAL.L001e14;
                if (mm.readByte(A1) <= 0x0a) { // break L000120;
                    mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.SET);
                }

// L000112:
            /*
                                                                    cmpi.b  // #$3e,(a1)               ;'>'
                                                                    bge     L000126
                                                                    addq.b  // #1,(a1)
                                                                    move.w  (a0),$0002(a0)
                                                                    bra     L000174
            */
                if (mm.readByte(A1) >= 0x3e) { // break L000126;
                    mm.write(A1, (byte) (mm.readByte(A1) + 1));
                    a0_w[1] = a0_w[0];
                    break L000174;
// L000120:
            /*
                                                                    st.b    (L001e15)
                                                                    bra     L000112
            */
//                    mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.SET);
//                    break L000112;
                }
// L000126:
            /*
                                                                    tst.b   (L001e18)
                                                                    beq     L000134
                                                                    bsr     L00077a
                                                                    bra     L0001d6
            */
                if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) { // break L000134;
                    L00077a(); //A1 dif
//                break L0001d6;
                } else {
// L000134:
            /*
                                                                    move.b  // #$7f,(a1)
                                                                    clr.b   (L001e17)
                                                                    move.b  // #$01,(L001e13)
                                                                    bsr     L_PAUSE
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L00015e
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    bne     L000164
            */
                    mm.write(A1, (byte) (0x7f));
                    mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.CLR);
                    mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
                    L_PAUSE_();  // L_PAUSE()のタイマーを止めない
                    if (PCM8 != 0) { // break L000164;

            /*L00015e:;

                                                                    move.w  // #$0100,d0
                                                                    trap    // #2
            */
                        D0 = 0x0100;
                        PCM8_SUB();
                    }
// L000164:
            /*
                                                                    tst.b   (L001df4)
                                                                    beq     L000174
                                                                    move.w  // #$01ff,d0
                                                                    trap    // #2
                                                                    clr.b   (L001df4)
            */
                    if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L000174;
                        D0 = 0x01ff;
                        PCM8_SUB();
                        mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
                    }
                }
            }
// L000174:
            /*
                                                                    lea.l   L001e0c(pc),a0
                                                                    move.b  (a0),d2
                                                                    moveq.l // #$12,d1
                                                                    tst.b   (L001e13)
                                                                    bne     L0001d6
                                                                    addq.w  // #1,(L001ba6)
                                                                    lea.l   CHBUF_FM(pc),a6
                                                                    moveq.l // #$00,d7
            */
            A0 = G + MXWORK_GLOBAL.L001e0c;
            D2 = mm.readByte(A0);
            D1 = 0x12;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e13) == 0) { // break L0001d6;
                mm.write(G + MXWORK_GLOBAL.L001ba6, (short) (mm.readShort(G + MXWORK_GLOBAL.L001ba6) + 1));
                A6 = MXWORK_CHBUF_FM[0];
                D7 = 0x00;

// L00018c:
                do {
            /*
                                                                    bsr     L001050
                                                                    bsr     L0011b4
                                                                    move.w  L001e1c(pc),d0
                                                                    btst.l  d7,d0
                                                                    bne     L0001a0
                                                                    bsr     L000c66
            */
                    L001050();
                    L0011b4();
                    D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c);
                    if ((D0 & (1 << D7)) == 0) { // break L0001a0;
                        L000c66();
                    }
// L0001a0:
            /*
                                                                    lea.l   $0050(a6),a6
                                                                    addq.w  // #1,d7
                                                                    cmp.w   // #$0009,d7
                                                                    bcs     L00018c
                                                                    tst.b   (L001df4)
                                                                    beq     L0001d6
                                                                    lea.l   CHBUF_PCM(pc),a6
            */
                    A6 += MXWORK_CH.Length;
                    D7++;
                } while (D7 < 0x0009); // break L00018c;
                if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L0001d6;
                    A6 = MXWORK_CHBUF_PCM[0];
                }
// L0001b6:
                do {
            /*
                                                                    bsr     L001050
                                                                    bsr     L0011b4
                                                                    move.w  L001e1c(pc),d0
                                                                    btst.l  d7,d0
                                                                    bne     L0001ca
                                                                    bsr     L000c66
            */
                    L001050();
                    L0011b4();
                    D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c);
                    if ((D0 & (1 << D7)) == 0) { // break L0001ca;
                        L000c66();
                    }
// L0001ca:
            /*
                                                                    lea.l   $0050(a6),a6
                                                                    addq.w  // #1,d7
                                                                    cmp.w   // #$0010,d7
                                                                    bcs     L0001b6
            */
                    A6 += MXWORK_CH.Length;
                    D7++;
                } while (D7 >= 0x0010); // break L0001b6;
            }
        }
// L0001d6:
            /*
                                                                    tst.b   $00e90003
                                                                    bmi     L0001d6
                                                                    move.b  // #$1b,$00e90001
                                                                    clr.b   (L002245)
                                                                    movem.l (sp)+,d1-d7/a0-a6
            */
        mm.write(G + MXWORK_GLOBAL.L002245, (byte) Depend.CLR);
        D1 = d1;
        D2 = d2;
        D3 = d3;
        D4 = d4;
        D5 = d5;
        D6 = d6;
        D7 = d7;
        A0 = a0;
        A1 = a1;
        A2 = a2;
        A3 = a3;
        A4 = a4;
        A5 = a5;
        A6 = a6;

            /*L0001ee:;

                                                                    move.b  (L001e12),-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (L001e13),d0
                                                                    rts
            */
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e12) * 256
                + mm.readByte(G + MXWORK_GLOBAL.L001e13);

    }

    //
        /*
        L_18:;
                lea.l   CHBUF_PCM(pc),a0
                move.l  a0,d0
                rts
        */
    private void L_18() {
        A0 = MXWORK_CHBUF_PCM[0];
        D0 = A0;
    }

    //
        /*
        L_19:;
                lea.l   L001bb4(pc),a0
                move.l  a0,d0
                rts
        */
    private void L_19() {
        A0 = G + MXWORK_GLOBAL.L001bb4;
        D0 = A0;
    }

    //
    private void L_1A() {
            /*
                                                                    bsr     L000216
                                                                    tst.l   d0
                                                                    bmi     L000214
                                                                    move.l  d1,$0004(sp)
            L000214:;
                                                                    rts
            */
        L000216();
    }

    //
    private void L000216() {
        int a2_l;
        int t0;

            /*L000216:;

                                                                    movem.l d2-d4/a0-a2,-(sp)
                                                                    movea.l a0,a1
                                                                    moveq.l // #$00,d0
                                                                    moveq.l // #$ff,d3
                                                                    moveq.l // #$00,d1
            */
        int d2 = D2;
        int d3 = D3;
        int d4 = D4;
        int a0 = A0;
        int a1 = A1;
        int a2 = A2;
        int a1_l = A0;
        D0 = 0x00000000;
        D3 = 0xffffffff;
        D1 = 0x00000000;

// L000222:
        boolean L000260 = false;
        boolean L000266 = false;
        while (true) {
            /*
                                                                    move.l  (a1)+,d4
                                                                    move.l  (a1)+,d2
                                                                    and.l   // #$00ffffff,d4
                                                                    beq     L00025a
                                                                    cmp.l   -$0008(a1),d4
                                                                    bne     L000260
                                                                    and.l   // #$00ffffff,d2
                                                                    beq     L00025a
                                                                    cmp.l   -$0004(a1),d2
                                                                    bne     L000260
                                                                    add.l   d4,d2
                                                                    cmp.l   d1,d2
                                                                    bcs     L00024a
                                                                    move.l  d2,d1
            */
            D4 = Depend.getblong(mm, a1_l);
            a1_l++;
            D2 = Depend.getblong(mm, a1_l);
            a1_l++;
            D4 &= 0x00ffffff;
            if (D4 != 0) { // break L00025a;
                t0 = Depend.getblong(mm, a1_l - 2);
                if (t0 != D4) { L000260 = true; break; } // L000260;
                D2 &= 0x00ffffff;
                if (D2 != 0) { // break L00025a;
                    t0 = Depend.getblong(mm, a1_l - 1);
                    if (t0 != D2) { L000260 = true; break; } // L000260;
                    D2 += D4;
                    if (D1 < D2) { // break L00024a;
                        D1 = D2;
                    }
// L00024a:
            /*
                                                                    cmp.l   d4,d3
                                                                    bcs     L000250
                                                                    move.l  d4,d3
            */
                    if (D4 <= D3) { // break L000250;
                        D3 = D4;
                    }
// L000250:
            /*
                                                                    lea.l   $00(a0,d3.l),a2
                                                                    cmpa.l  a2,a1
                                                                    beq     L00025e
                                                                    bhi     L000266
            */
                    a2_l = (A0 + D3);
                    if (a2_l == a1_l) break; // L00025e;
                    if (a2_l < a1_l) { L000266 = true; break; } // L000266;
                }
            }
// L00025a:
            /*
                                                                    addq.w  // #1,d0
                                                                    bra     L000222
            */
            D0++;
//            break L000222;
        }
// L00025e:
        if (!L000260 && !L000266) {
            /*
                                                                    addq.w  // #1,d0
            */
            D0++;
        }
// L000260:
        if (!L000266) {
            /*
                                                                    movem.l (sp)+,d2-d4/a0-a2
                                                                    rts
            */
            D2 = d2;
            D3 = d3;
            D4 = d4;
            A0 = a0;
            A1 = a1;
            A2 = a2;
            return;
        }
// L000266:
            /*
                                                                    moveq.l // #$ff,d0
                                                                    movem.l (sp)+,d2-d4/a0-a2
                                                                    rts
            */
        D0 = 0xffffffff;
        D2 = d2;
        D3 = d3;
        D4 = d4;
        A0 = a0;
        A1 = a1;
        A2 = a2;
    }


    //
    private void L_1B() {
        int d1, d2, d3, d4, d5;
        int a0, a1, a2;
        int a0_l, a1_l, a2_l;
        short a1_w, a2_w;

            /*L_1B:;

                                                                    movem.l d1-d5/a0-a2,-(sp)
                                                                    bsr     L000216
                                                                    move.l  d0,d2
                                                                    bmi     L0002e4
                                                                    move.l  d0,d5
                                                                    lsl.l   // #3,d0
                                                                    moveq.l // #$60,d3
            */
        d1 = D1;
        d2 = D2;
        d3 = D3;
        d4 = D4;
        d5 = D5;
        a0 = A0;
        a1 = A1;
        a2 = A2;
        L000216();
        D2 = D0;
        if (d2 <= 0) { // break L0002e4;
            D5 = D0;
            D0 <<= 3;
            D3 = 0x60;
        }
// L00027e:
        do {
            /*
                                                                    sub.l   d3,d2
                                                                    bcc     L00027e
                                                                    add.l   d3,d2
                                                                    beq     L0002e4
                                                                    sub.l   d2,d3
                                                                    move.l  d3,d4
                                                                    lsl.l   // #3,d3
                                                                    move.l  d1,d2
                                                                    addq.l  // #1,d2
                                                                    and.w   // #$fffe,d2
                                                                    lea.l   $00(a0,d2.l),a2
                                                                    add.l   d3,d1
                                                                    lea.l   $00(a0,d1.l),a1
                                                                    sub.l   d0,d2
                                                                    lsr.l   // #1,d2
                                                                    move.l  d2,d0
                                                                    lsr.l   // #1,d0
                                                                    subq.l  // #1,d0
                                                                    swap.w  d0
            */
            D2 -= D3;
        } while (d2 >= 0); // break L00027e;
        D2 += D3;
        if (D2 != 0) { // break L0002e4;
            D3 -= D2;
            D4 = D3;
            D3 <<= 3;
            D2 = D1;
            D2++;
            D2 &= 0xfffffffe;
            a2_l = (A0 + D2);
            D1 += D3;
            a1_l = (A0 + D1);
            D2 -= D0;
            D2 >>= 1;
            D0 = D2;
            D0 >>= 1;
            D0--;

            /*L0002aa:;

                                                                    swap.w  d0
            */

// L0002ac:
            do {
            /*
                                                                    move.l  -(a2),-(a1)
                                                                    dbra    d0,L0002ac
                                                                    swap.w  d0
                                                                    dbra    d0,L0002aa
                                                                    and.w   // #$0001,d2
                                                                    beq     L0002c0
                                                                    move.w  -(a2),-(a1)
            */
                --a1_l;
                --a2_l;
                a1_l = a2_l;
            } while ((D0--) != 0); // break L0002ac;
            D2 &= 0xffff0001;
            if ((D2 & 0xffff) != 0) { // break L0002c0;
                a1_w = (short) a1_l;
                a2_w = (short) a1_l;
                --a1_w;
                --a2_w;
                a1_w = a2_w;
                a1_l = a1_w;
                a2_l = a2_w;
                A2 = (byte) a2_l;
            }
// L0002c0:
            /*
                                                                    subq.w  // #1,d4
            */
            D4--;
            D4 &= 0xffff;

// L0002c2:
            do {
            /*
                                                                    clr.l   -(a1)
                                                                    clr.l   -(a1)
                                                                    dbra    d4,L0002c2
                                                                    subq.l  // #1,d5
                                                                    swap.w  d5
            */
                --a1_l;
                mm.write(a1_l, (byte) Depend.CLR);
                --a1_l;
                mm.write(a1_l, (byte) Depend.CLR);
            } while ((D4--) != 0); // break L0002c2;
            A1 = (byte) a1_l;
            D5--;
            d5 = D5;

            a0_l = A0;
            /*L0002ce:;

                                                                    swap.w  d5
            */

// L0002d0:
            do {
            /*
                                                                    move.l  (a0)+,d0
                                                                    beq     L0002d8
                                                                    add.l   d3,-$0004(a0)
            */
                D0 = Depend.getblong(mm, a0_l);
                a0_l++;
                if (D0 != 0) { // break L0002d8;
                    Depend.putblong(mm, a0_l - 1, D0 + D3);
                }
// L0002d8:
            /*
                                                                    addq.w  // #4,a0
                                                                    dbra    d5,L0002d0
                                                                    swap.w  d5
                                                                    dbra    d5,L0002ce
            */
                a0_l++;
            } while ((D5--) != 0); // break L0002d0;
            A0 = (byte) a0_l;
        }
// L0002e4:
            /*
                                                                    move.l  d1,d0
                                                                    movem.l (sp)+,d1-d5/a0-a2
                                                                    rts
            */
        D1 = D0;
        D1 = d1;
        D2 = d2;
        D3 = d3;
        D4 = d4;
        D5 = d5;
        A0 = a0;
        A1 = a1;
        A2 = a2;
    }

    //
    private void L_1C() {
        int t0;
        int c0;
        int d1, d2, d3, d4, d5, d6, d7;
        int a0, a1, a2, a3, a4;
        int a1_l, a2_l, a3_l, a4_l;
        short a1_w, a2_w, a3_w;

            /*L_1C:;

                                                                    movem.l d1-d7/a0-a4,-(sp)
                                                                    bsr     L000216
                                                                    tst.l   d0
                                                                    bmi     L000462
                                                                    add.l   a0,d1
                                                                    addq.l  // #1,d1
                                                                    and.w   // #$fffe,d1
                                                                    move.l  d1,d3
                                                                    move.l  d3,d7
                                                                    move.l  d0,d2
                                                                    exg.l   a0,a1
                                                                    bsr     L000216
                                                                    tst.l   d0
                                                                    bmi     L000462
                                                                    add.l   d1,d3
                                                                    add.l   a0,d1
                                                                    move.l  d1,d6
                                                                    move.l  d0,d1
                                                                    lsl.l   // #3,d1
                                                                    add.l   a0,d1
                                                                    move.l  d1,d4
                                                                    suba.l  d3,a2
                                                                    bcs     L00045a
                                                                    moveq.l // #$60,d1
                                                                    move.l  d2,d3
            */
        d1 = D1;
        d2 = D2;
        d3 = D3;
        d4 = D4;
        d5 = D5;
        d6 = D6;
        d7 = D7;
        a0 = A0;
        a1 = A1;
        a2 = A2;
        a3 = A3;
        a4 = A4;
        try {
            L000216();
            if (D0 < 0) { // break L000462;
                D0 = 0xfffffffd;
                return;
            }
            D1 += A0;
            D1++;
            D1 &= 0xfffffffe;
            D3 = D1;
            D7 = D3;
            D2 = D0;
            t0 = A0;
            A0 = A1;
            A1 = t0;
            L000216();
            if (D0 < 0) { // break L000462;
                D0 = 0xfffffffd;
                return;
            }
            D3 += D1;
            D1 += A0;
            D6 = D1;
            D1 = D0;
            D1 <<= 3;
            D1 += A0;
            D4 = D1;
            A2 -= D3;
            if (D3 < 0) { // break L00045a;
                D0 = 0xffffffff;
                return;
            }
            D1 = 0x60;
            D3 = D2;

// L00032c:
            do {
        /*
                                                                sub.l   d1,d3
                                                                bcc     L00032c
                                                                add.l   d1,d3
                                                                beq     L000342
                                                                sub.l   d1,d3
                                                                neg.l   d3
                                                                move.l  d3,d1
                                                                lsl.l   // #3,d3
                                                                cmp.l   a2,d3
                                                                bhi     L00045a
        */
                D3 -= D1;
            } while (D3 >= 0); // break L00032c;
            D3 += D1;
            if (D3 != 0) { // break L000342;
                D3 -= D1;
                D3 = -((int) D3);
                D1 = D3;
                D3 <<= 3;
                if (A2 < D3) { // break L00045a;
                    D0 = 0xffffffff;
                    return;
                }
            }
// L000342:
        /*
                                                                add.l   d0,d3
                                                                lsl.l   // #3,d3
                                                                add.l   d7,d3
                                                                movea.l a0,a4
                                                                cmp.l   a0,d3
                                                                bcs     L00037a
                                                                move.l  d0,d1
                                                                lsl.l   // #3,d1
                                                                cmp.l   (L001ba8),d1
                                                                bhi     L00045e
                                                                movea.l (L001bac),a4
                                                                movea.l a0,a3
                                                                move.l  d0,d1
                                                                subq.l  // #1,d1
                                                                swap.w  d1
        */
            D3 += D0;
            D3 <<= 3;
            D3 += D7;
            A4 = A0;
            if (A0 <= D3) { // break L00037a;
                D1 = D0;
                D1 <<= 3;
                if (mm.readInt(G + MXWORK_GLOBAL.L001ba8) < D1) { // break L00045e;
                    D0 = 0xfffffffe;
                    return;
                }
                A4 = mm.readInt(G + MXWORK_GLOBAL.L001bac);
                A3 = A0;
                D1 = D0;
                D1--;

                a3_l = A3;
                a4_l = A4;
        /*L000366:;

                                                                swap.w  d1
        */

// L000368:
                do {
        /*
                                                                move.l  (a3)+,(a4)+
                                                                move.l  (a3)+,(a4)+
                                                                dbra    d1,L000368
                                                                swap.w  d1
                                                                dbra    d1,L000366
                                                                movea.l (L001bac),a4
        */
                    a4_l = a3_l;
                    a4_l++;
                    a3_l++;
                    a4_l = a3_l;
                    a4_l++;
                    a3_l++;
                } while ((D1--) != 0); // break L000368;
                A4 = mm.readInt(G + MXWORK_GLOBAL.L001bac);
                A3 = a3_l;
            }
// L00037a:
        /*
                                                                lsl.l   // #3,d0
                                                                move.l  d0,d5
                                                                exg.l   a0,a1
                                                                bsr     L_1B
                                                                tst.l   d0
                                                                bmi     L000466
                                                                bsr     L000216
                                                                move.l  d0,d2
                                                                bmi     L000462
                                                                add.l   a0,d1
                                                                addq.l  // #1,d1
                                                                and.w   // #$fffe,d1
                                                                movea.l d1,a2
                                                                add.l   d5,d1
                                                                lsl.l   // #3,d0
                                                                add.l   a0,d0
                                                                add.l   d5,d0
                                                                movea.l d1,a3
                                                                movea.l a3,a1
                                                                sub.l   d0,d1
                                                                move.l  d1,d7
                                                                lsr.l   // #2,d1
                                                                move.w  sr,-(sp)
                                                                subq.l  // #1,d1
                                                                swap.w  d1
        */
            D0 <<= 3;
            D5 = D0;
            t0 = A0;
            A0 = A1;
            A1 = t0;
            L_1B();
            if (D0 < 0) { // break L000466;
                D0 = 0xfffffffc;
                return;
            }
            L000216();
            D2 = D0;
            if (D2 < 0) { //break L000462;
                D0 = 0xfffffffd;
                return;
            }
            D1 += A0;
            D1++;
            D1 &= 0xfffffffe;
            A2 = D1;
            D1 += D5;
            D0 <<= 3;
            D0 += A0;
            D0 += D5;
            A3 = D1;
            A1 = A3;
            D1 -= D0;
            D7 = D1;
            D1 >>= 1;
            c0 = D1 & 1;
            D1 >>= 1;
            D1--;

            a2_l = A2;
            a3_l = A3;
        /*L0003b6:;

                                                                swap.w  d1
        */

// L0003b8:
            do {
        /*
                                                                move.l  -(a2),-(a3)
                                                                dbra    d1,L0003b8
                                                                swap.w  d1
                                                                dbra    d1,L0003b6
                                                                move.w  (sp)+,sr
                                                                bcc     L0003ca
                                                                move.w  -(a2),-(a3)
        */
                --a3_l;
                --a2_l;
                a3_l = a2_l;
            } while ((D1--) != 0); // break L0003b8;
            if (c0 != 0) { // break L0003ca;
                a2_w = (short) a2_l;
                a3_w = (short) a3_l;
                --a3_w;
                --a2_w;
                a3_w = a2_w;
                A2 = (byte) a2_w;
                A3 = (byte) a3_w;
            }
// L0003ca:
        /*
                                                                movea.l d0,a2
                                                                suba.l  d5,a2
                                                                cmpa.l  a2,a4
                                                                beq     L0003ea
                                                                move.l  d5,d1
                                                                lsr.l   // #3,d1
                                                                subq.l  // #1,d1
                                                                swap.w  d1
        */
            A2 = (byte) D0;
            A2 -= D5;
            if (A2 != A4) { // break L0003ea;
                D1 = D5;
                D1 >>= 3;
                D1--;
                a2_l = A2;
                a4_l = A4;

        /*L0003da:;

                                                                swap.w  d1
        */

// L0003dc:
                do {
        /*
                                                                move.l  (a4)+,(a2)+
                                                                move.l  (a4)+,(a2)+
                                                                dbra    d1,L0003dc
                                                                swap.w  d1
                                                                dbra    d1,L0003da
        */
                    a2_l++;
                    a4_l++;
                    a2_l = a4_l;
                    a2_l++;
                    a4_l++;
                    a2_l = a4_l;
                } while ((D1--) > 0); // break L0003dc;
                A2 = (byte) a2_l;
                A4 = (byte) a4_l;
            }
// L0003ea:
        /*
                                                                movea.l d4,a2
                                                                sub.l   d4,d6
                                                                move.l  d6,d1
                                                                lsr.l   // #2,d1
                                                                subq.l  // #1,d1
                                                                swap.w  d1
        */
            A2 = (byte) D4;
            D6 -= D4;
            D1 = D6;
            D2 >>= 2;
            D1--;
            a2_l = A2;
            a1_l = A1;

        /*L0003f6:;

                                                                swap.w  d1
        */

// L0003f8:
            do {
        /*
                                                                move.l  (a2)+,(a1)+
                                                                dbra    d1,L0003f8
                                                                swap.w  d1
                                                                dbra    d1,L0003f6
                                                                move.w  d6,d1
                                                                and.w   // #$0002,d1
                                                                beq     L00040e
                                                                move.w  (a2)+,(a1)+
        */
                a1_l++;
                a2_l++;
                a1_l = a2_l;
            } while ((D1--) > 0); // break L0003f8;
            D1 = D6;
            D1 &= 0x00000002;
            if (D1 != 0) { // break L00040e;
                a1_w = (short) a1_l;
                a2_w = (short) a2_l;
                a1_w++;
                a2_w++;
                a1_w = a2_w;
                A1 = (byte) a1_w;
                A2 = (byte) a2_w;
            }
// L00040e:
        /*
                                                                and.w   // #$0001,d6
                                                                beq     L000416
                                                                move.b  (a2)+,(a1)+
        */
            D6 &= 0x00000001;
            if (D1 != 0) { // break L000416;
                a1++;
                a2++;
                A1 = A2;
            }
// L000416:
        /*
                                                                suba.l  a0,a1
                                                                move.l  d5,d1
                                                                move.l  d2,d0
                                                                lsl.l   // #3,d0
                                                                add.l   d0,d7
                                                                subq.l  // #1,d2
                                                                swap.w  d2
        */
            A1 -= A0;
            D1 = D5;
            D0 = D2;
            D0 <<= 3;
            D7 += D0;
            D2--;

        /*L000424:;

                                                                swap.w  d2
        */

// L000426:
            do {
        /*
                                                                move.l  (a0),d0
                                                                beq     L00042c
                                                                add.l   d1,(a0)
        */
                D0 = Depend.getblong(mm, A0);
                if (D0 != 0) { // break L00042c;
                    Depend.putblong(mm, A0, D0 + D1);
                }
// L00042c:
        /*
                                                                addq.w  // #8,a0
                                                                dbra    d2,L000426
                                                                swap.w  d2
                                                                dbra    d2,L000424
                                                                lsr.l   // #3,d5
                                                                subq.l  // #1,d5
                                                                swap.w  d5
        */
                A0 = (byte) (A0 + 2);
                if ((D2--) > 0) continue; // break L000426;
                D5 >>= 3;
                D5--;

        /*L00043e:;

                                                                swap.w  d5
        */

        /*L000440:;

                                                                move.l  (a0),d0
                                                                beq     L000446
                                                                add.l   d7,(a0)
        */
                D0 = Depend.getblong(mm, A0);
                if (D0 != 0) { // break L000446;
                    Depend.putblong(mm, A0, D0 + D7);
                }
// L000446:
        /*
                                                                addq.w  // #8,a0
                                                                dbra    d5,L000440
                                                                swap.w  d5
                                                                dbra    d5,L00043e
                                                                move.l  a1,d0
        */
                A0 = (byte) (A0 + 2);
            } while ((D2--) > 0); // break L000426;
            D5 >>= 3;
            D5--;
            D0 = A1;

// L000454:
        } finally {
        /*
                                                                movem.l (sp)+,d1-d7/a0-a4
                                                                rts
        */
            D1 = d1;
            D2 = d2;
            D3 = d3;
            D4 = d4;
            D5 = d5;
            D6 = d6;
            D7 = d7;
            A0 = a0;
            A1 = a1;
            A2 = a2;
            A3 = a3;
            A4 = a4;
        }
// L00045a:
        /*
                                                                moveq.l // #$ff,d0
                                                                bra     L000454
        */
//            D0 = 0xffffffff;
//            break L000454;

// L00045e:
        /*
                                                                moveq.l // #$fe,d0
                                                                bra     L000454
        */
//            D0 = 0xfffffffe;
//            break L000454;
// L000462:
            /*
                                                                    moveq.l // #$fd,d0
                                                                    bra     L000454
            */
//        D0 = 0xfffffffd;
//        break L000454;
// L000466:
            /*
                                                                    moveq.l // #$fc,d0
                                                                    bra     L000454
            */
//        D0 = 0xfffffffc;
//        break L000454;
    }

    //
    private void L_1D() {
        int d2, d3, d4;
            /*L_1D:;

                                                                    move.b  (L001e08),d4
                                                                    move.w  d1,d3
                                                                    st.b    (L001e08)
                                                                    move.w  // #$ffff,d1
                                                                    movem.l d2-d4,-(sp)
                                                                    bsr     L_0F
                                                                    movem.l (sp)+,d2-d4
                                                                    bra     L000496
            */
        D4 = mm.readByte(G + MXWORK_GLOBAL.L001e08);
        D3 = D1;
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) Depend.SET);
        D1 = 0xffff;
        d2 = D2;
        d3 = D3;
        d4 = D4;
        L_0F();
        D4 = d4;
        D3 = d3;
        D2 = d2;
        L000496();
    }

    //
    private void L_1E() {
        //int d2_, d3_, d4_;

        //L_1E:;
            /*
                    move.b  (L001e08),d4
                    move.w  d1,d3
                    move.w  // #$ffff,(L001e1c)
                    st.b    (L001e08)
            */

        D4 = mm.readByte(G + MXWORK_GLOBAL.L001e08);
        D3 = D1;
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) 0xffff);
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) Depend.SET);
        L000496();
    }

    //
    private void L000496() {
        int d2, d3, d4;

        //L000496:;
            /*
                                                                    ori.w   // #$0700,sr
                                                                    andi.b  // #$f7,$00e88009
                                                                    andi.w  // #$f8ff,sr
                                                                    tst.b   (L001e13)
                                                                    bne     L0004c0
                                                                    subq.w  // #1,d2
                                                                    bcs     L0004c0
                                                                    movem.l d2-d4,-(sp)
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L001e13) == 0) { // break L0004c0;
            if ((D2--) != 0) { // break L0004c0;
                d2 = D2;
                d3 = D3;
                d4 = D4;

// L0004b4:
                do {
            /*
                                                                    bsr     L0000dc
                                                                    dbra    d2,L0004b4
                                                                    movem.l (sp)+,d2-d4
            */
                    L0000dc();
                } while ((D2--) != 0); // break L0004b4;
                D4 = d4;
                D3 = d3;
                D2 = d2;
            }
        }
// L0004c0:
            /*
                                                                    move.w  d3,(L001e1c)
                                                                    move.b  d4,(L001e08)
                                                                    bne     L_1F
                                                                    tst.b   (L001e13)
                                                                    bne     L_1F
                                                                    bsr     L00056a
                                                                    moveq.l // #$12,d1
                                                                    move.b  (L001e0c),d2
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$14,d1
                                                                    moveq.l // #$3a,d2
                                                                    bsr     L_WRITEOPM
            */
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) D3);
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) D4);
        if (D4 != 0) {
            L_1F();
            return;
        }
        if (mm.readByte(G + MXWORK_GLOBAL.L001e13) != 0) {
            L_1F();
            return;
        }
        L00056a();
        D1 = 0x00000012;
        D2 = mm.readByte(G + MXWORK_GLOBAL.L001e0c);
        L_WRITEOPM();
        D1 = 0x00000014;
        D2 = 0x0000003a;
        L_WRITEOPM();

            /*
                                                                    ; fall down
            */
        L_1F();
    }

    //
    private void L_1F() {
            /*
                                                                    move.w  (L001ba6),d0
                                                                    rts
            */
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001ba6);
    }

    //
    private void L_0D() {
            /*
                                                                    cmp.b   // #$f0,d1
                                                                    beq     L000552
                                                                    cmp.b   // #$fc,d1
                                                                    beq     L00052e
                                                                    tst.l   d1
                                                                    bmi     L000534
                                                                    tst.b   (L001e18)
                                                                    bne     L_ERROR
                                                                    move.l  a2,(L001e30)
                                                                    move.l  a1,(L001e24)
                                                                    move.l  a1,(L001e28)
            */
        if ((byte) D1 == 0xf0) {
            L000552();
            return;
        }
        if ((byte) D1 != 0xfc) { // break L00052e;
            if (D1 < 0) {
                L000534();
                return;
            }
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
                L_ERROR();
                return;
            }
            mm.write(G + MXWORK_GLOBAL.L001e30, A2);
            mm.write(G + MXWORK_GLOBAL.L001e24, A1);
            mm.write(G + MXWORK_GLOBAL.L001e28, A1);

// L000510:
            while (true) {
            /*
                                                                    tst.w   (a1)
                                                                    beq     L000518
                                                                    addq.w  // #6,a1
                                                                    bra     L000510
            */
                if (Depend.getbword(mm, A1) == 0) break; // L000518;
                A1 = (byte) (((short) A1) + 3);
//                break L000510;
            }
// L000518:
            /*
                                                                    subq.w  // #6,a1
                                                                    move.l  a1,(L001e2c)
                                                                    st.b    (L001e18)
                                                                    st.b    (L001e19)
                                                                    movea.l L001e24(pc),a0
                                                                    bra     L000788
            */
            A1 = (byte) (((short) A1) - 3);
            mm.write(G + MXWORK_GLOBAL.L001e2c, A1);
            mm.write(G + MXWORK_GLOBAL.L001e18, (byte) Depend.SET);
            mm.write(G + MXWORK_GLOBAL.L001e19, (byte) Depend.SET);
            A0 = mm.readInt(G + MXWORK_GLOBAL.L001e24);
            L000788();
            return;
        }
// L00052e:
            /*
                                                                    move.b  L001e19(pc),d0
                                                                    rts
            */
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e19);
    }

    //
    private void L000534() {
            /*
                                                                    clr.b   (L001e18)
                                                                    clr.b   (L002230)
                                                                    clr.b   (L002231)
                                                                    movea.l L001e34(pc),a0
                                                                    move.l  (a0),(L002218)
                                                                    move.l  $0004(a0),(L00221c)
                                                                    bra     L00063e
            */
        mm.write(G + MXWORK_GLOBAL.L001e18, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L002230, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L002231, (byte) Depend.CLR);
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e34);
        mm.write(G + MXWORK_GLOBAL.L002218, Depend.getblong(mm, A0));
        mm.write(G + MXWORK_GLOBAL.L00221c, Depend.getblong(mm, A0 + 4));
        L00063e();
    }

    //
    private void L000552() {
            /*
                                                                    bsr     L000534
            */
        L000534();
            /*
                                                                    ; fall down
            */
        L000554();
    }

    //
    private void L000554() {
            /*
                                                                    movea.l L001e30(pc),a0
                                                                    pea.l   (a0)
                                                                    DOS     _MFREE
                                                                    addq.w  // #4,sp
                                                                    moveq.l // #$00,d0
                                                                    move.l  d0,(L001e30)
                                                                    move.b  d0,(L001e19)
                                                                    rts
            */
        D0 = 0;
        mm.write(G + MXWORK_GLOBAL.L001e30, D0);//?
        mm.write(G + MXWORK_GLOBAL.L001e19, (byte) D0);//?
    }

    /**
     * 割り込み
     */
    private void L00056a() {
            /*
                                                                    move.w  sr,-(sp)
                                                                    ori.w   // #$0700,sr
                                                                    clr.b   (L001e13)
                                                                    tst.b   (L001e08)
                                                                    bne     L000596
                                                                    lea.l   L_OPMINT(pc),a1
                                                                    suba.l  a0,a0
                                                                    move.l  a1,$010c(a0)
                                                                    movea.l // #$00e88000,a0
                                                                    ori.b   // #$08,$0009(a0)
                                                                    ori.b   // #$08,$0015(a0)
            */
        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) Depend.CLR);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e08) == 0) { // break L000596;
            SETOPMINT(this::L_OPMINT);
        }
//        L000596:
            /*
                                                                    move.w  (sp)+,sr
                                                                    rts
            */
    }

    //
    private void L_FREE() {
            /*
                                                                    bsr     L00063e
                                                                    move.l  (L00220c),$0090(a0)
                                                                    pea.l   L000000-$0000f0(pc)
                                                                    DOS     _MFREE
                                                                    addq.w  // #4,sp
                                                                    tst.b   (L001e19)
                                                                    beq     L0005b2
                                                                    bra     L000554
            */
        L00063e();
        if (mm.readByte(G + MXWORK_GLOBAL.L001e19) != 0) { // break L0005b2;
            L000554();
        }
// L0005b2:
            /*
                    rts
            */
    }

    //
    private void L_SETMDX() {
        int d1;
        int a1;
            /*
                                                                    tst.b   (L001e18)
                                                                    beq     L0005c4
                                                                    movem.l d1/a1,-(sp)
                                                                    bsr     L000552
                                                                    movem.l (sp)+,d1/a1
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) { // break L0005c4;
            d1 = D1;
            a1 = A1;
            L000552();
            A1 = a1;
            D1 = d1;
        }
// L0005c4:
            /*
                                                                    lea.l   (L002230),a2
                                                                    movea.l L001e34(pc),a0
                                                                    move.l  a0,(L002218)
                                                                    move.l  (L002220),d0
                                                                    bra     L0005f8
            */
        A2 = G + MXWORK_GLOBAL.L002230;
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e34);
        mm.write(G + MXWORK_GLOBAL.L002218, A0);
        D0 = mm.readInt(G + MXWORK_GLOBAL.L002220);
        L0005f8();
    }

    //
    private void L_SETPDX() {
        int d1;
        int a1;
            /*
                                                                    tst.b   (L001e18)
                                                                    beq     L0005e8
                                                                    movem.l d1/a1,-(sp)
                                                                    bsr     L000552
                                                                    movem.l (sp)+,d1/a1
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) { // break L0005e8;
            d1 = D1;
            a1 = A1;
            L000552();
            A1 = a1;
            D1 = d1;
        }
// L0005e8:
            /*
                                                                    lea.l   (L002231),a2
                                                                    movea.l L001e38(pc),a0
                                                                    move.l  a0,(L00221c)
                                                                    move.l  (L002224),d0
            */
        A2 = G + MXWORK_GLOBAL.L002231;
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e38);
        mm.write(G + MXWORK_GLOBAL.L00221c, A0);
        D0 = mm.readInt(G + MXWORK_GLOBAL.L002224);
            /*
                                                                    ; fall down
            */
        L0005f8();
    }

    //
    private void L0005f8() {
        int d1;
        int a0, a1, a2;
        int a1_l, a0_l;
        //byte a2_b, a1_b, a0_b;
            /*
                                                                    cmp.l   d1,d0
                                                                    bcs     L000630
                                                                    movem.l d1/a0-a2,-(sp)
                                                                    bsr     L00063e
                                                                    movem.l (sp)+,d1/a0-a2
                                                                    move.w  d1,d0
                                                                    andi.w  // #$0003,d0
                                                                    lsr.l   // #2,d1
                                                                    swap.w  d1
            */
        if (D1 <= D0) { // break L000630;
            d1 = D1;
            a0 = A0;
            a1 = A1;
            a2 = A2;
            L00063e();
            A2 = a2;
            A1 = a1;
            A0 = a0;
            D1 = d1;
            D0 = D1;
            D0 &= 0x0003;
            a1_l = A1;
            a0_l = A0;
            D1 >>= 2;

            //L000610:;
            /*
                                                                    swap.w  d1
            */

// L000612:
            do {
            /*
                                                                    move.l  (a1)+,(a0)+
                                                                    dbra    d1,L000612
                                                                    swap.w  d1
                                                                    dbra    d1,L000610
                                                                    tst.w   d0
                                                                    beq     L00062a
                                                                    subq.w  // #1,d0
            */
                mm.write(a0_l, mm.readInt(a1_l));
                a0_l += 4;
                a1_l += 4;
            } while (--D1 != 0); // break L000612;
            A1 = a1_l;
            A0 = a0_l;
            if (D0 != 0) { // break L00062a;
                D0--;

// L000624:
                do {
            /*
                                                                    move.b  (a1)+,(a0)+
                                                                    dbra    d0,L000624
            */
                    mm.write(A0, mm.readByte(A1));
                    A0++;
                    A1++;
                } while (D0-- != 0); // break L000624;
            }
// L00062a:
            /*
                                                                    st.b    (a2)
                                                                    moveq.l // #$00,d0
                                                                    rts
            */
            mm.write(A2, (byte) Depend.SET);
            D0 = 0;
            return;
        }
// L000630:
            /*
                                                                    bset.l  // #$1f,d0
                                                                    rts
            */
        D0 |= (1 << 0x1f);
    }

    //
    private void L_STOP() {
            /*
                                                                    tst.b   (L001e18)
                                                                    bne     L000552
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
            L000552();
            return;
        }

            /*
                                                                    ; fall down
            */
        L00063e();
    }

    //
    private void L00063e() {
            /*
                                                                    move.b  // #$01,(L001e13)
                                                                    move.w  sr,-(sp)
                                                                    ori.w   // #$0700,sr
                                                                    bsr     L0006c4
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L000664
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    bne     L00066a
            */
        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
        L0006c4();
        if (PCM8 != 0) { // break L00066a;

            //L000664:;
            /*
                                                                    move.w  // #$0100,d0
                                                                    trap    // #2
            */
            D0 = 0x0100;
            PCM8_SUB();
        }
// L00066a:
            /*
                                                                    tst.b   (L001df4)
                                                                    beq     L00067a
                                                                    move.w  // #$01ff,d0
                                                                    trap    // #2
                                                                    clr.b   (L001df4)
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L00067a;
            D0 = 0x01ff;
            PCM8_SUB();
            mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
        }
// L00067a:
            /*
                                                                    moveq.l // #$0f,d2
                                                                    moveq.l // #$e0,d1
            */
        D2 = 0x0f;
        D1 = 0xe0;

// L00067e:
        do {
            /*
                                                                    bsr     L_WRITEOPM
                                                                    addq.b  // #1,d1
                                                                    bne     L00067e
                                                                    lea.l   L00223c(pc),a0
                                                                    lea.l   L001bb4(pc),a1
                                                                    moveq.l // #$07,d3
                                                                    moveq.l // #$00,d2
                                                                    moveq.l // #$08,d1
            */
            L_WRITEOPM();
            D1++;
        } while ((byte) D1 != 0); // break L00067e;
        A0 = G + MXWORK_GLOBAL.L00223c;
        A1 = G + MXWORK_GLOBAL.L001bb4;
        D3 = 0x07;
        D2 = 0x00;
        D1 = 0x08;

// L000694:
        do {
            /*
                                                                    bsr     L_WRITEOPM
                                                                    move.b  d2,(a0)+
                                                                    move.b  d2,(a1)+
                                                                    addq.b  // #1,d2
                                                                    dbra    d3,L000694
                                                                    movea.l // #$00e88000,a0
                                                                    andi.b  // #$f7,$0009(a0)
                                                                    andi.b  // #$f7,$0015(a0)
                                                                    suba.l  a0,a0
                                                                    move.l  $0004(a5),$010c(a0)
                                                                    move.w  (sp)+,sr
                                                                    rts
            */
            L_WRITEOPM();
            mm.write(A0, (byte) D2);
            A0++;
            mm.write(A1, (byte) D2);
            A1++;
            D2++;
        } while (D3-- != 0); // break L000694;
    }

    //
    private void L_PAUSE() {
            /*
                                                                    st.b    (L001e12)
            */
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.SET);
        mm.write(G + MXWORK_GLOBAL.STOPMUSICTIMER, (byte) Depend.SET);
            /*
                                                                    ; fall down
            */
        L0006c4();
    }

    //
    private void L_PAUSE_() {
            /*
                                                                    st.b    (L001e12)
            */
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.SET);

            /*
                                                                    ; fall down
            */
        L0006c4();
    }

    //
    private void L0006c4() {
            /*
                                                                    moveq.l // #$07,d7
                                                                    lea.l   CHBUF_FM(pc),a6
            */
        D7 = 0x07;
        A6 = MXWORK_CHBUF_FM[0];

// L0006ca:
        do {
            /*
                                                                    moveq.l // #$7f,d0
                                                                    bsr     L000e28
                                                                    lea.l   $0050(a6),a6
                                                                    dbra    d7,L0006ca
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L0006f0
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    bne     L00070c
            */
            D0 = 0x7f;
            L000e28();
            A6 += MXWORK_CH.Length;
        } while (D7-- != 0); // break L0006ca;
        if (PCM8 != 0) { // break L00070c;

            //L0006f0:;
            /*
                                                                    move.w  // #$01fc,d0
                                                                    moveq.l // #$ff,d1
                                                                    trap    // #2
                                                                    cmp.b   // #$01,d0
                                                                    bne     L000706
                                                                    move.w  // #$0101,d0
                                                                    trap    // #2
                                                                    rts
            */
            D0 = 0x01fc;
            D1 = 0xffffffff;
            PCM8_SUB();
            if ((byte) D0 == 0x01) { // break L000706;
                D0 = 0x0101;
                PCM8_SUB();
                return;
            }
// L000706:
            /*
                                                                    moveq.l // #$67,d0
                                                                    moveq.l // #$01,d1
                                                                    trap    // #15
            */
            ADPCMMOD_STOP();
        }
// L00070c:
            /*
                                                                    moveq.l // #$67,d0
                                                                    moveq.l // #$00,d1
                                                                    trap    // #15
                                                                    rts
            */
        ADPCMMOD_END();
    }

    //
    private void L_CONT() {
            /*
                                                                    clr.b   (L001e12)
                                                                    moveq.l // #$07,d7
                                                                    lea.l   CHBUF_FM(pc),a6
            */
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.STOPMUSICTIMER, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.MUSICTIMER, mm.readByte(G + MXWORK_GLOBAL.L001e0c));
        D7 = 0x07;
        A6 = MXWORK_CHBUF_FM[0];

// L00071e:
        do {
            /*
                                                                    bsr     L000dfe
                                                                    lea.l   $0050(a6),a6
                                                                    dbra    d7,L00071e
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L000742
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    bne     L000756
            */
            L000dfe();
            A6 += MXWORK_CH.Length;
        } while (D7-- != 0); // break L00071e;
        if (PCM8 == 0) {
            L000756();
            return;
        }

        //L000742:;
            /*
                                                                    move.w  // #$01fc,d0
                                                                    moveq.l // #$ff,d1
                                                                    trap    // #2
                                                                    cmp.b   // #$01,d0
                                                                    bne     L000756
                                                                    move.w  // #$0102,d0
                                                                    trap    // #2
            */
        D0 = 0x01fc;
        D1 = 0xffffffff;
        PCM8_SUB();
        if ((byte) D0 != 0x01) {
            L000756();
            return;
        }
        D0 = 0x0102;
        PCM8_SUB();

            /*
                                                                    ; fall down
            */
        L000756();
    }

    //
    private void L000756() {
            /*
                                                                    moveq.l // #$30,d2
                                                                    move.b  L001e08(pc),d1
                                                                    bne     L000760
                                                                    moveq.l // #$3a,d2
            */
        D2 = 0x30;
        D1 = mm.readByte(G + MXWORK_GLOBAL.L001e08);
        if (D1 == 0) { // break L000760;
            D2 = 0x3a;
        }
// L000760:
            /*
                                                                    moveq.l // #$14,d1
                                                                    bra     L_WRITEOPM
            */
        // タイマー動作制御 (0x30/0x3aをセット)
        D1 = 0x14;
        L_WRITEOPM();
    }

    //
    private void L000766() {
            /*
                                                                    movea.l L001e28(pc),a0
                                                                    movea.l L001e24(pc),a1
                                                                    subq.w  // #6,a0
                                                                    cmpa.l  a1,a0
                                                                    bcc     L000788
                                                                    movea.l L001e2c(pc),a0
                                                                    bra     L000788
            */
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e28);
        A1 = mm.readInt(G + MXWORK_GLOBAL.L001e24);
        A0 -= 0x06;
        if (A1 <= A0) {
            L000788();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e2c);
        L000788();
    }

    //
    private void L00077a() {
            /*
                                                                    movea.l L001e28(pc),a0
                                                                    addq.w  // #6,a0
                                                                    tst.w   (a0)
                                                                    bne     L000788
                                                                    movea.l L001e24(pc),a0
            */
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e28);
        A0 += 0x06;
        if (Depend.getbword(mm, A0) != 0) {
            L000788();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e24);

            /*
                                                                    ; fall down
            */
        L000788();
    }

    //
    private void L000788() {
            /*
                                                                    move.l  a0,(L001e28)
                                                                    move.w  (a0),(L001e22)
                                                                    movea.l $0002(a0),a1
                                                                    move.l  (a1),(L00221c)
                                                                    addq.w  // #4,a1
                                                                    move.w  (a1),d0
                                                                    not.w   d0
                                                                    move.w  $0002(a1),d1
                                                                    not.w   d1
                                                                    move.b  d0,(L002230)
                                                                    move.b  d1,(L002231)
                                                                    move.l  a1,(L002218)
                                                                    clr.w   (L001e1c)
                                                                    bra     L0007c0
            */
        mm.write(G + MXWORK_GLOBAL.L001e28, A0);
        mm.write(G + MXWORK_GLOBAL.L001e22, Depend.getbword(mm, A0));
        A1 = Depend.getblong(mm, A0 + 2);
        mm.write(G + MXWORK_GLOBAL.L00221c, Depend.getblong(mm, A1));
        A1 += 4;
        D0 = Depend.getbword(mm, A1);
        D0 = ~D0;
        D1 = Depend.getbword(mm, A1 + 2);
        D1 = ~D1;
        mm.write(G + MXWORK_GLOBAL.L002230, (byte) D0);
        mm.write(G + MXWORK_GLOBAL.L002231, (byte) D1);
        mm.write(G + MXWORK_GLOBAL.L002218, A1);
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) Depend.CLR);
        L0007c0();
    }

    //
    private void L_PLAY() {
            /*
                                                                    clr.w   (L001e1c)
                                                                    bra     L0007c0
            */
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) Depend.CLR);
        L0007c0();
    }

    //
    private void L_0F() {
            /*
                                                                    move.w  d1,(L001e1c)
            */
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) D1);
            /*
                                                                    ; fall down
            */
        L0007c0();
    }

    //
    private void L0007c0() {
        mm.write(G + MXWORK_GLOBAL.PLAYTIME, 0);
        // checker
        mm.write(G + MXWORK_GLOBAL.FATALERROR, 0);
        // checker end
            /*
                                                                    clr.b   (L001e14)
                                                                    clr.b   (L001e15)
                                                                    clr.b   (L001e17)
                                                                    clr.b   (L001e13)
                                                                    tst.b   (L001e12)
                                                                    beq     L0007f4
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L0007ee
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    bne     L0007f4
            */
        mm.write(G + MXWORK_GLOBAL.L001e14, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) Depend.CLR);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0) { // break L0007f4;
            if (PCM8 != 0) { // break L0007f4;
                //L0007ee:;
            /*
                                                                    move.w  // #$0100,d0
                                                                    trap    // #2
            */
                D0 = 0x0100;
                PCM8_SUB();
            }
        }
// L0007f4:
            /*
                                                                    clr.b   (L001e12)
                                                                    clr.b   (L001df4)
                                                                    move.w  // #$01ff,(L001e1a)
                                                                    move.w  // #$01ff,(L001e06)
                                                                    clr.w   (L002246)
                                                                    clr.w   (L001ba6)
                                                                    move.b  (L002230),d0
                                                                    beq     L_ERROR
                                                                    bsr     L00063e
                                                                    movea.l (L002218),a2
                                                                    move.w  $0002(a2),d1
                                                                    bmi     L000848
                                                                    tst.b   (L002231)
                                                                    beq     L_ERROR
                                                                    movea.l (L00221c),a0
                                                                    bra     L00083c
            */
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.STOPMUSICTIMER, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) 0x01ff);
        mm.write(G + MXWORK_GLOBAL.L001e06, (short) 0x01ff);
        mm.write(G + MXWORK_GLOBAL.L002246, (short) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001ba6, (short) Depend.CLR);
        D0 = mm.readByte(G + MXWORK_GLOBAL.L002230);
        if (D0 == 0) {
            L_ERROR();
            return;
        }
        L00063e();
        A2 = mm.readInt(G + MXWORK_GLOBAL.L002218);
        D1 = Depend.getbword(mm, A2 + 2);
        if ((short) D1 >= 0) { // break L000848;
            if (mm.readByte(G + MXWORK_GLOBAL.L002231) == 0) {
                L_ERROR();
                return;
            }
            A0 = mm.readInt(G + MXWORK_GLOBAL.L00221c);
//            break L00083c;
            while (D1-- != 0) {
// L000834:
            /*
                                                                    tst.l   (a0)
                                                                    beq     L_ERROR
                                                                    adda.l  (a0),a0
            */
                if (Depend.getblong(mm, A0) == 0) {
                    L_ERROR();
                    return;
                }
                A0 += Depend.getblong(mm, A0);

// L00083c:
            /*
                                                                    dbra    d1,L000834
                                                                    adda.w  $0004(a0),a0
                                                                    move.l  a0,(L00222c)

            */
            } // if (D1-- != 0); break L000834;
            A0 += Depend.getbword(mm, A0 + 4);
            mm.write(G + MXWORK_GLOBAL.L00222c, A0);
        }
// L000848:
            /*
                                                                    adda.w  $0004(a2),a2
                                                                    movea.l a2,a1
                                                                    movea.l a2,a0
                                                                    moveq.l // #$00,d0
                                                                    move.w  (a1)+,d0
                                                                    adda.l  d0,a2
                                                                    move.l  a2,(L002228)
                                                                    lea.l   CHBUF_FM(pc),a6
                                                                    lea.l   L00095a(pc),a3
                                                                    moveq.l // #$ff,d6
                                                                    moveq.l // #$00,d7
            */
        A2 += Depend.getbword(mm, A2 + 4);
        A1 = A2;
        A0 = A2;
        D0 = 0x00000000;
        D0 = Depend.getbword(mm, A1);
        A1 += 2;
        A2 += D0;
        mm.write(G + MXWORK_GLOBAL.L002228, A2);
        A6 = MXWORK_CHBUF_FM[0];
        A3 = (byte) 0;
        D6 = 0xffffffff;
        D7 = 0x00000000;

// L000866:
        while (true) { //トラックのパラメータを初期化(exPCM含む)
            /*
                                                                    movea.l a0,a2
                                                                    move.w  (a1)+,d0
                                                                    adda.l  d0,a2
                                                                    move.l  a2,(a6)
                                                                    move.l  a3,$0026(a6)
                                                                    move.l  a3,$0040(a6)
                                                                    move.w  d6,$0014(a6)
                                                                    move.b  d6,$0023(a6)
                                                                    move.b  d7,$0018(a6)
                                                                    move.b  // #$00,$001d(a6)
                                                                    move.b  // #$01,$001a(a6)
                                                                    move.b  // #$08,$0022(a6)
                                                                    move.b  // #$c0,$001c(a6)
                                                                    move.b  // #$08,$001e(a6)
                                                                    clr.w   $0036(a6)
                                                                    clr.w   $004a(a6)
                                                                    clr.w   $0010(a6)
                                                                    clr.b   $0024(a6)
                                                                    clr.b   $001f(a6)
                                                                    clr.b   $0019(a6)
                                                                    clr.w   $0016(a6)
                                                                    cmp.w   // #$0008,d7
                                                                    bcc     L0008d4
                                                                    moveq.l // #$38,d1
                                                                    add.b   d7,d1
                                                                    moveq.l // #$00,d2
                                                                    bsr     L_WRITEOPM
                                                                    addq.w  // #1,d7
                                                                    lea.l   $0050(a6),a6
                                                                    bra     L000866
            */
            A2 = A0;
            D0 = Depend.getbword(mm, A1);
            A1 += 2;
            A2 += D0;
            mm.write(A6 + MXWORK_CH.S0000, A2);
            mm.write(A6 + MXWORK_CH.S0026, A3);
            mm.write(A6 + MXWORK_CH.S0040, A3);
            mm.write(A6 + MXWORK_CH.S0014, (short) D6);
            mm.write(A6 + MXWORK_CH.S0023, (byte) D6);
            mm.write(A6 + MXWORK_CH.S0018, (byte) D7);
            //mm.Write(A6 + MXWORK_CH.S0014, (byte)D6);
            //mm.Write(A6 + MXWORK_CH.S0023, (byte)D6);
            //mm.Write(A6 + MXWORK_CH.S0018, (byte)D7);
            mm.write(A6 + MXWORK_CH.S001d, (byte) 0x00);
            mm.write(A6 + MXWORK_CH.S001a, (byte) 0x01);
            mm.write(A6 + MXWORK_CH.S0022, (byte) 0x08);
            mm.write(A6 + MXWORK_CH.S001c, (byte) 0xc0);
            mm.write(A6 + MXWORK_CH.S001e, (byte) 0x08);
            mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S0036) & 0xffff);
            mm.write(A6 + MXWORK_CH.S004a, (short) Depend.CLR);
            mm.write(A6 + MXWORK_CH.S0010, (short) Depend.CLR);
            mm.write(A6 + MXWORK_CH.S0024, (byte) Depend.CLR);
            mm.write(A6 + MXWORK_CH.S001f, (byte) Depend.CLR);
            mm.write(A6 + MXWORK_CH.S0019, (byte) Depend.CLR);
            mm.write(A6 + MXWORK_CH.S0016, (byte) Depend.CLR);
            mm.write(A6 + MXWORK_CH.S0017, (byte) Depend.CLR);
            if (D7 < 0x0008) { // break L0008d4;
                D1 = 0x38;
                D1 += D7;
                D2 = 0x00;
                L_WRITEOPM();
                D7++;
                A6 += MXWORK_CH.Length;
                continue; // break L000866;
            }
// L0008d4:
            /*
                                                                    move.b  // #$10,$001c(a6)
                                                                    move.b  // #$08,$0022(a6)
                                                                    move.b  d7,$0018(a6)
                                                                    andi.b  // #$07,$0018(a6)
                                                                    ori.b   // #$80,$0018(a6)
                                                                    clr.b   $0004_b(a6)
                                                                    cmp.w   // #$000f,d7
                                                                    beq     L000910
                                                                    addq.w  // #1,d7
                                                                    lea.l   $0050(a6),a6
                                                                    cmp.w   // #$0009,d7
                                                                    bne     L000866
                                                                    lea.l   CHBUF_PCM(pc),a6
                                                                    bra     L000866
            */
            mm.write(A6 + MXWORK_CH.S001c, (byte) 0x10);
            mm.write(A6 + MXWORK_CH.S0022, (byte) 0x08);
            mm.write(A6 + MXWORK_CH.S0018, (byte) D7);
            mm.write(A6 + MXWORK_CH.S0018, (byte) (mm.readByte(A6 + MXWORK_CH.S0018) & 0x07));
            mm.write(A6 + MXWORK_CH.S0018, (byte) (mm.readByte(A6 + MXWORK_CH.S0018) | 0x80));
            mm.write(A6 + MXWORK_CH.S0004_b, (byte) 0x00);
            if (D7 == 0x000f) break; // L000910;
            D7++;
            A6 += MXWORK_CH.Length;
            if (D7 != 0x09) continue; // break L000866;
            A6 = MXWORK_CHBUF_PCM[0];
//            break L000866;
        }
// L000910:
            /*
                                                                    lea.l   (L001df6),a0
                                                                    moveq.l // #$0f,d0
            */
        A0 = G + MXWORK_GLOBAL.L001df6 + 0;
        D0 = 0x0f;

// L000916:
        do {
            /*
                                                                    clr.b   (a0)+
                                                                    dbra    d0,L000916
                                                                    clr.b   (L002232)
                                                                    moveq.l // #$00,d2
                                                                    moveq.l // #$01,d1
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$0f,d1
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$19,d1
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$80,d2
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$c8,d2
                                                                    moveq.l // #$12,d1
                                                                    move.b  d2,(L001e0c)
                                                                    tst.b   (L001e08)
                                                                    bne     L00094c
                                                                    bsr     L_WRITEOPM
            */
            mm.write(A0, (byte) Depend.CLR);
            A0++;
        } while (D0-- != 0); // break L000916;
        mm.write(G + MXWORK_GLOBAL.L002232, (byte) Depend.CLR);

        //LFO SW OFF
        D2 = 0x00;
        D1 = 0x01;
        L_WRITEOPM();

        //Noise OFF
        D1 = 0x0f;
        L_WRITEOPM();

        //LFO AMD初期化
        D1 = 0x19;
        L_WRITEOPM();

        //LFO PMD初期化
        D2 = 0x80;
        L_WRITEOPM();
        //Timer-B初期化
        D2 = 0xc8;
        D1 = 0x12;
        mm.write(G + MXWORK_GLOBAL.L001e0c, (byte) D2);
        mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e08) == 0) { // break L00094c;
            L_WRITEOPM();
        }
// L00094c:
            /*
                                                                    bsr     L00056a
                                                                    bsr     L000756
                                                                    moveq.l // #$00,d0
                                                                    rts
            */
        L00056a();
        L000756();
        D0 = 0;
    }

    //
    private void L_ERROR() {
            /*
                                                                    moveq.l // #$ff,d0
            */
        D0 = 0xffffffff;

            /*
                                                                    ; fall down
            */
        L00095a();
    }

    //
    private void L00095a() {
            /*
                                                                    rts
            */
    }

    //
    private void L_08() {
            /*
                                                                    tst.b   (L002230)
                                                                    beq     L000998
                                                                    movea.l (L002218),a0
                                                                    bra     L00096e
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L002230) == 0) {
            L000998();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L002218);
//        break L00096e;
        while (D1-- != 0) {
// L000968:
            /*
                                                                    tst.w   (a0)
                                                                    beq     L000998
                                                                    adda.w  (a0),a0
            */
            if (Depend.getbword(mm, A0) == 0) {
                L000998();
                return;
            }
            A0 += Depend.getbword(mm, A0);

// L00096e:
            /*
                                                                    dbra    d1,L000968
                                                                    adda.w  $0006(a0),a0
                                                                    move.l  a0,d0
                                                                    rts
            */
//        if (D1-- != 0) break L000968;
        }
        A0 += Depend.getbword(mm, A0 + 6);
        D0 = A0;
    }

    //
    private void L_09() {

        //L_09:;
            /*
                                                                    tst.b   (L002231)
                                                                    beq     L000998
                                                                    movea.l (L00221c),a0
                                                                    bra     L00098c
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L002231) == 0) {
            L000998();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L00221c);
//        break L00098c;
        while (D1-- != 0) {
// L000986:
            /*
                                                                    tst.l   (a0)
                                                                    beq     L000998
                                                                    adda.l  (a0),a0
            */
            if (Depend.getblong(mm, A0) == 0) {
                L000998();
                return;
            }
            A0 += Depend.getblong(mm, A0);

// L00098c:
            /*
                                                                    dbra    d1,L000986
                                                                    adda.w  $0006(a0),a0
                                                                    move.l  a0,d0
                                                                    rts
            */
//        if (D1-- != 0) break L000986;
        }
        A0 += Depend.getbword(mm, A0 + 6);
        D0 = A0;
    }

    //
    private void L000998() {
        /*
                                                                    moveq.l // #$00,d0
                                                                    rts
        */
        D0 = 0;
    }

    //
    private void L_OPMINT() {
        int d0, d1, d2, d3, d4, d5, d6, d7;
        int a0, a1, a2, a3, a4, a5;
        int a6;
        int a0_w;

        if (mm.readInt(G + MXWORK_GLOBAL.FATALERROR) != 0) {
            return;
        }

// #if LOGINT
//                    FILE* fout;
//                    fout = fopen("c:\\temp\\int.Log", "ab+");
//                    fprintf(fout, "%lu\n", timeGetTime());
//                    fclose(fout);
// #endif

        //L_OPMINT:;
            /*
                                                                    andi.b  // #$f7,$00e88015
                                                                    move.l  a6,-(sp)
                                                                    movea.l $0006(sp),a6
                                                                    cmpa.l  $01a8.w,a6
                                                                    bne     L0009be
                                                                    pea.l   L0009b8(pc)
                                                                    move.w  sr,-(sp)
                                                                    jmp     (a6)
            */


        //L0009b8:;
            /*
                                                                    movea.l (sp)+,a6
                                                                    addq.w  // #6,sp
                                                                    move.l  a6,-(sp)
            */


        //L0009be:;
            /*
                                                                    andi.w  // #$faff,sr
                                                                    movem.l d0-d7/a0-a5,-(sp)
                                                                    lea.l   L00220c(pc),a5
                                                                    st.b    (L002245)
                                                                    tst.b   (L001e12)
                                                                    bne     L000a66
                                                                    lea.l   L001e1e(pc),a0
                                                                    lea.l   L001e17(pc),a1
                                                                    tst.b   (a1)
                                                                    beq     L000a66
                                                                    bpl     L0009ee
                                                                    move.b  // #$7f,(a1)
                                                                    move.w  (a0),$0002(a0)
            */
        d0 = D0;
        d1 = D1;
        d2 = D2;
        d3 = D3;
        d4 = D4;
        d5 = D5;
        d6 = D6;
        d7 = D7;
        a0 = A0;
        a1 = A1;
        a2 = A2;
        a3 = A3;
        a4 = A4;
        a5 = A5;
        a6 = A6;
        mm.write(G + MXWORK_GLOBAL.L002245, (byte) Depend.SET);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0) break L000a66;
        a0_w = G + MXWORK_GLOBAL.L001e1e + 0;
        A1 = G + MXWORK_GLOBAL.L001e17;
        if (mm.readByte(A1) == 0) break L000a66;
        if (mm.readByte(A1) >= 0) break L0009ee;
        mm.write(A1, (byte) 0x7f);
        mm.write(a0_w + 2, mm.readShort(a0_w + 0));

        L0009ee:
        ;
            /*
                                                                    tst.w   $0002(a0)
                                                                    bmi     L0009fa
                                                                    subq.w  // #2,$0002(a0)
                                                                    bra     L000a66
            */
        if (mm.readShort(a0_w + 2) >= 0) { // break L0009fa;
            mm.write(a0_w + 2, (short) (mm.readShort(a0_w + 2) - 2));
            break L000a66;
        }
// L0009fa:
            /*
                                                                    lea.l   L001e14(pc),a1
                                                                    cmpi.b  // #$0a,(a1)
                                                                    bge     L000a12
            */
        A1 = G + MXWORK_GLOBAL.L001e14;
        if (mm.readByte(A1) >= 0x0a) break L000a12;

// L000a04:
        while (true) {
            /*
                                                                    cmpi.b  // #$3e,(a1)               ;'>'
                                                                    bge     L000a18
                                                                    addq.b  // #1,(a1)
                                                                    move.w  (a0),$0002(a0)
                                                                    bra     L000a66
            */
            if (mm.readByte(A1) >= 0x3e) break; // L000a18;
            mm.write(A1, (byte) (mm.readByte(A1) + 1));
            mm.write(a0_w + 2, mm.readShort(a0_w + 0));
            break L000a66;

// L000a12:
            /*
                                                                    st.b    (L001e15)
                                                                    bra     L000a04
            */
            mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.SET);
//            break L000a04;
        }
// L000a18:
            /*
                                                                    tst.b   (L001e18)
                                                                    beq     L000a26
                                                                    bsr     L00077a
                                                                    bra     L000c40
            */
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) { // break L000a26;
            L00077a();
//            break L000c40;
        } else {
// L000a26:
            /*
                                                                    move.b  // #$7f,(a1)
                                                                    clr.b   (L001e17)
                                                                    move.b  // #$01,(L001e13)
                                                                    bsr     L_PAUSE
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L000a50
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    bne     L000a56
            */
            mm.write(A1, (byte) 0x7f);
            mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.CLR);
            mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
            L_PAUSE_();  // L_PAUSE()のタイマーを止めない
            if (PCM8 != 0) { // break L000a56;

                //L000a50:;
            /*
                                                                    move.w  // #$0100,d0
                                                                    trap    // #2
            */
                D0 = 0x0100;
                PCM8_SUB();
            }
// L000a56:
            /*
                                                                    tst.b   (L001df4)
                                                                    beq     L000a66
                                                                    move.w  // #$01ff,d0
                                                                    trap    // #2
                                                                    clr.b   (L001df4)
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L000a66;
                D0 = 0x01ff;
                PCM8_SUB();
                mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
            }
// L000a66:
            /*
                                                                    lea.l   L001e0c(pc),a0
                                                                    move.b  (a0),d2
                                                                    moveq.l // #$12,d1
                                                                    tst.b   (L001e0a)
                                                                    bne     L000bd0
                                                                    move.b  $080e.w,d7
                                                                    and.b   // #$0f,d7
                                                                    cmp.b   // #$05,d7  ; OPT1+SHIFT
                                                                    beq     L000b16
                                                                    cmp.b   // #$09,d7  ; OPT2+SHIFT
                                                                    beq.s   L000b0a
                                                                    cmp.b   // #$06,d7  ; OPT1+CTRL
                                                                    beq     L000b04
                                                                    cmp.b   // #$0a,d7  ; OPT2+CTRL
                                                                    beq     L000afc
                                                                    cmp.b   // #$04,d7  ; OPT1
                                                                    beq     L000b6c
                                                                    cmp.b   // #$08,d7  ; OPT2
                                                                    beq     L000b20
                                                                    cmp.b   // #$02,d7  ; CTRL
                                                                    bne     L000bd0
                                                                    move.b  $080b.w,d7
                                                                    and.b   // #$03,d7
                                                                    cmpi.b  // #$80,$080a.w  ; XF3
                                                                    beq     L000aec
                                                                    cmp.b   // #$02,d7  ; XF5
                                                                    beq     L000adc
                                                                    cmp.b   // #$01,d7  ; XF4
                                                                    bne     L000bd0
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    subq.b  // #1,$0002(a0)  ; L001e0e
                                                                    bra     L000bca
            */
            A0 = G + MXWORK_GLOBAL.L001e0c;
            D2 = mm.readByte(A0);
            D1 = 0x12;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e0a) != 0) break L000bd0;
            if (mm.readShort(KEY + MXWORK_KEY.OPT1) != 0 && mm.readShort(KEY + MXWORK_KEY.SHIFT) != 0) break L000b16;
            if (mm.readShort(KEY + MXWORK_KEY.OPT2) != 0 && mm.readShort(KEY + MXWORK_KEY.SHIFT) != 0) break L000b0a;
            if (mm.readShort(KEY + MXWORK_KEY.OPT1) != 0 && mm.readShort(KEY + MXWORK_KEY.CTRL) != 0) break L000b04;
            if (mm.readShort(KEY + MXWORK_KEY.OPT2) != 0 && mm.readShort(KEY + MXWORK_KEY.CTRL) != 0) break L000afc;
            if (mm.readShort(KEY + MXWORK_KEY.OPT1) != 0) break L000b6c;
            if (mm.readShort(KEY + MXWORK_KEY.OPT2) != 0) break L000b20;
            if (mm.readShort(KEY + MXWORK_KEY.CTRL) == 0) break L000bd0;
            if (mm.readShort(KEY + MXWORK_KEY.XF3) != 0) break L000aec;
            if (mm.readShort(KEY + MXWORK_KEY.XF5) != 0) break L000adc;
            if (mm.readShort(KEY + MXWORK_KEY.XF4) == 0) break L000bd0;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) (mm.readByte(G + MXWORK_GLOBAL.L001e0e) - 1));
            break L000bca;

            L000adc:
            ;
            /*
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    addq.b  // #1,$0002(a0)  ; L001e0e
                                                                    bra     L000bca
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) (mm.readByte(G + MXWORK_GLOBAL.L001e0e) + 1));
            break L000bca;

            L000aec:
            ;
            /*
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    clr.b   $0002(a0)  ; L001e0e
                                                                    bra     L000bca
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) Depend.CLR);
            break L000bca;

            L000afc:
            ;
            /*
                                                                    move.b  // #$ff,d2
                                                                    bra     L000bd8
            */
            D2 = 0xff;
            break L000bd8;

            L000b04:
            ;
            /*
                                                                    moveq.l // #$00,d2
                                                                    bra     L000bd8
            */
            D2 = 0x00;
            break L000bd8;

            L000b0a:
            ;
            /*
                                                                    neg.b   d2
                                                                    lsr.b   // #2,d2
                                                                    addq.b  // #1,d2
                                                                    neg.b   d2
                                                                    bra     L000bd8
            */
            D2 = (byte) -D2;
            D2 >>= 2;
            D2++;
            D2 = (byte) -D2;
            break L000bd8;

            L000b16:
            ;
            /*
                                                                    neg.b   d2
                                                                    add.b   d2,d2
                                                                    neg.b   d2
                                                                    bra     L000bd8
            */
            D2 = (byte) -D2;
            D2 += D2;
            D2 = (byte) -D2;
            break L000bd8;

            L000b20:
            ;
            /*
                                                                    btst.b  // #$00,$080b.w  ; XF4
                                                                    bne     L000b56
                                                                    btst.b  // #$01,$080b.w  ; XF5
                                                                    beq     L000bd0
                                                                    tst.b   (L001e12)
                                                                    beq     L000b4a
                                                                    bpl     L000bbe
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    bsr     L_CONT
                                                                    bra     L000bbe
            */
            if (mm.readShort(KEY + MXWORK_KEY.XF4) != 0) break L000b56;
            if (mm.readShort(KEY + MXWORK_KEY.XF5) == 0) break L000bd0;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e12) == 0) break L000b4a;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e12) >= 0) break L000bbe;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bbe;
            L_CONT();
            break L000bbe;

            L000b4a:
            ;
            /*
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    bsr     L_PAUSE
                                                                    bra     L000bbe
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            L_PAUSE();
            break L000bbe;

            L000b56:
            ;
            /*
                                                                    tst.b   (L001e12)
                                                                    beq     L000bca
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    st.b    (L001e10)
                                                                    moveq.l // #$00,d2
                                                                    bra     L000bec
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e12) == 0) break L000bca;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.SET);
            D2 = 0;
            break L000bec;

            L000b6c:
            ;
            /*
                                                                    move.b  $080b.w,d7
                                                                    beq     L000b82  ; !XF4 && !XF5
                                                                    and.b   // #$03,d7
                                                                    cmp.b   // #$02,d7  ; XF5
                                                                    beq     L000bae
                                                                    cmp.b   // #$01,d7  ; XF4
                                                                    beq     L000b9c
            */
            if (mm.readShort(KEY + MXWORK_KEY.XF4) == 0 && mm.readShort(KEY + MXWORK_KEY.XF5) == 0) break L000b82;
            if (mm.readShort(KEY + MXWORK_KEY.XF4) != 0) break L000bae;
            if (mm.readShort(KEY + MXWORK_KEY.XF5) != 0) break L000b9c;

            L000b82:
            ;
            /*
                                                                    cmpi.b  // #$80,$080a.w ; XF3
                                                                    bne     L000bd0
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
            */
            if (mm.readShort(KEY + MXWORK_KEY.XF3) == 0) break L000bd0;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;

            //L000b90:;
            /*
                                                                    move.w  // #$0011,(L001e1e)
                                                                    st.b    (L001e17)
                                                                    bra     L000bca
            */
            mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) 0x0011);
            mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
            break L000bca;

            L000b9c:
            ;
            /*
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    tst.b   (L001e18)
                                                                    beq     L000bc4
                                                                    bsr     L000766
                                                                    bra     L000bbe
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) == 0) break L000bc4;
            L000766();
            break L000bbe;

            L000bae:
            ;
            /*
                                                                    tst.b   (L001e10)
                                                                    bne     L000bca
                                                                    tst.b   (L001e18)
                                                                    beq     L000bc4
                                                                    bsr     L00077a
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) break L000bca;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) == 0) break L000bc4;
            L00077a();

            L000bbe:
            ;
            /*
                                                                    st.b    (L001e10)
                                                                    bra     L000c40
            */
            mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.SET);
            break L000c40;

            L000bc4:
            ;
            /*
                                                                    bsr     L_PLAY
                                                                    bra     L000bbe
            */
            L_PLAY();
            break L000bbe;

            L000bca:
            ;
            /*
                                                                    st.b    (L001e10)
                                                                    bra     L000bd4
            */
            mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.SET);
            break L000bd4;

            L000bd0:
            ;
            /*
                                                                    clr.b   (L001e10)
            */
            mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.CLR);

            L000bd4:
            ;
            /*
                                                                    add.b   $0002(a0),d2  ; L001e0e
            */
            D2 += mm.readByte(G + MXWORK_GLOBAL.L001e0e);

            L000bd8:
            ;
            /*
                                                                    tst.b   (L001e12)
                                                                    bne     L000be4
                                                                    tst.b   (L001e13)
                                                                    beq     L000bec
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0) break L000be4;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e13) == 0) break L000bec;

            L000be4:
            ;
            /*
                                                                    moveq.l // #$00,d2
                                                                    bsr     L_WRITEOPM
                                                                    bra     L000c40
            */
            D2 = 0x00;
            mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
            L_WRITEOPM();
            break L000c40;

            L000bec:
            ;
            /*
                                                                    bsr     L_WRITEOPM
                                                                    addq.w  // #1,(L001ba6)
                                                                    lea.l   CHBUF_FM(pc),a6
                                                                    moveq.l // #$00,d7
            */
            //	G.MUSICTIMER = D2; // ここは入れない
            L_WRITEOPM();
            mm.write(G + MXWORK_GLOBAL.L001ba6, (short) (mm.readShort(G + MXWORK_GLOBAL.L001ba6) + 1));
            A6 = MXWORK_CHBUF_FM[0];
            D7 = 0x00;

// L000bfa:
            do {
                //System.err.println("Ch%02d Adr:%04x",D7,mm.Readint(A6+MXWORK_CH.S0000));
            /*
                                                                    bsr     L001050
                                                                    bsr     L0011b4
                                                                    move.w  L001e1c(pc),d0
                                                                    btst.l  d7,d0
                                                                    bne     L000c0c
                                                                    bsr     L000c66
            */
                L001050();
                L0011b4();
                D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c);
                if ((D0 & (1 << D7)) == 0) { // break L000c0c;
                    L000c66();
                }
// L000c0c:
            /*
                                                                    lea.l   $0050(a6),a6
                                                                    addq.w  // #1,d7
                                                                    cmp.w   // #$0009,d7
                                                                    bcs     L000bfa
                                                                    tst.b   (L001df4)
                                                                    beq     L000c40
                                                                    lea.l   CHBUF_PCM(pc),a6
            */
                A6 += MXWORK_CH.Length;
                D7++;
            } while (D7 < 0x0009); // break L000bfa;
            if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L000c40;
                A6 = MXWORK_CHBUF_PCM[0];

// L000c22:
                do {
            /*
                                                                    bsr     L001050
                                                                    bsr     L0011b4
                                                                    move.w  L001e1c(pc),d0
                                                                    btst.l  d7,d0
                                                                    bne     L000c34
                                                                    bsr     L000c66
            */
                    L001050();
                    L0011b4();
                    D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c);
                    if ((D0 & (1 << D7)) == 0) { // break L000c34;
                        L000c66();
                    }
// L000c34:
            /*
                                                                    lea.l   $0050(a6),a6
                                                                    addq.w  // #1,d7
                                                                    cmp.w   // #$0010,d7
                                                                    bcs     L000c22
            */
                    A6 += MXWORK_CH.Length;
                    D7++;
                } while (D7 < 0x0010); // break L000c22;
            }
        }
// L000c40:
            /*
                                                                    bsr     L000756
            */
        L000756();

        //L000c44:;
            /*
                                                                    tst.b   $00e90003
                                                                    bmi     L000c44
                                                                    move.b  // #$1b,$00e90001
                                                                    clr.b   (L002245)
                                                                    movem.l (sp)+,d0-d7/a0-a6
                                                                    ori.b   // #$08,$00e88015
                                                                    rte
            */
        D1 = 0x14;
        D2 = 0x1b;
        L_WRITEOPM();
        mm.write(G + MXWORK_GLOBAL.L002245, (byte) Depend.CLR);
        D0 = d0;
        D1 = d1;
        D2 = d2;
        D3 = d3;
        D4 = d4;
        D5 = d5;
        D6 = d6;
        D7 = d7;
        A0 = a0;
        A1 = a1;
        A2 = a2;
        A3 = a3;
        A4 = a4;
        A5 = a5;
        A6 = a6;
    }

    //
    private void L000c66() {
            /*
                                                                    btst.b  // #$00,$0016(a6)
                                                                    beq     L000cce
                                                                    tst.b   $0020(a6)
                                                                    bne     L000cca
                                                                    tst.b   $0018(a6)
                                                                    bmi     L000cbe
                                                                    bsr     L000d84
                                                                    bsr     L000e66
                                                                    btst.b  // #$03,$0016(a6)
                                                                    bne     L000cb4
                                                                    move.b  $0024(a6),$0025(a6)
                                                                    beq     L000c9e
                                                                    clr.l   $0036(a6)
                                                                    clr.w   $004a(a6)
                                                                    bsr     L001094
            */
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 0)) != 0) { // break L000cce;
            if (mm.readByte(A6 + MXWORK_CH.S0020) == 0) { // break L000cca;
                if (mm.readByte(A6 + MXWORK_CH.S0018) >= 0) { // break L000cbe;
                    L000d84();//音色送信
                    L000e66();//音色送信(PAN　FB CON)
                    if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 3)) == 0) { // break L000cb4;
                        mm.write(A6 + MXWORK_CH.S0025, mm.readByte(A6 + MXWORK_CH.S0024));//LFO のdelayCounterをセット
                        if (mm.readByte(A6 + MXWORK_CH.S0025) != 0) { // break L000c9e;
                            mm.write(A6 + MXWORK_CH.S0036, Depend.CLR);//LFO Pitch
                            mm.write(A6 + MXWORK_CH.S004a, (short) Depend.CLR);//LFO Volume
                            L001094();
                        }
// L000c9e:
            /*
                                                                    btst.b  // #$01,$0016(a6)
                                                                    beq     L000cb4
                                                                    moveq.l // #$01,d1
                                                                    moveq.l // #$02,d2
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$00,d2
                                                                    bsr     L_WRITEOPM
            */
                        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 1)) != 0) { // break L000cb4;
                            D1 = 0x01;
                            D2 = 0x02;
                            L_WRITEOPM();//HardLFO Reset
                            D2 = 0;
                            L_WRITEOPM();
                        }
                    }
// L000cb4:
            /*
                                                                    clr.l   $000c(a6)
                                                                    bsr     L000cdc
                                                                    bsr     L000dfe
            */
                    mm.write(A6 + MXWORK_CH.S000c, Depend.CLR);//Bend
                    L000cdc();//KF KC
                    L000dfe();//Volume送信
                }
//L000cbe:
            /*
                                                                    bsr     L000e7e
                                                                    andi.b  // #$fe,$0016(a6)
                                                                    rts
            */
                L000e7e();//KEY ON
                mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xfe));
                return;
            }
// L000cca:
            /*
                                                                    subq.b  // #1,$0020(a6)
            */
            mm.write(A6 + MXWORK_CH.S0020, (byte) (mm.readByte(A6 + MXWORK_CH.S0020) - 1));
        }
// L000cce:
            /*
                                                                    tst.b   $0018(a6)
                                                                    bmi     L000cda
                                                                    bsr     L000cdc
                                                                    bsr     L000dfe
            */
        if (mm.readByte(A6 + MXWORK_CH.S0018) >= 0) { // break L000cda;
            L000cdc();
            L000dfe();
        }
// L000cda:
            /*
                                                                    rts
            */
    }


    //

    private void L000cdc() {
            /*
            L000d24:;
                    .dc.b   $00,$01,$02,$04,$05,$06,$08,$09
                    .dc.b   $0a,$0c,$0d,$0e,$10,$11,$12,$14
                    .dc.b   $15,$16,$18,$19,$1a,$1c,$1d,$1e
                    .dc.b   $20,$21,$22,$24,$25,$26,$28,$29
                    .dc.b   $2a,$2c,$2d,$2e,$30,$31,$32,$34
                    .dc.b   $35,$36,$38,$39,$3a,$3c,$3d,$3e
                    .dc.b   $40,$41,$42,$44,$45,$46,$48,$49
                    .dc.b   $4a,$4c,$4d,$4e,$50,$51,$52,$54
                    .dc.b   $55,$56,$58,$59,$5a,$5c,$5d,$5e
                    .dc.b   $60,$61,$62,$64,$65,$66,$68,$69
                    .dc.b   $6a,$6c,$6d,$6e,$70,$71,$72,$74
                    .dc.b   $75,$76,$78,$79,$7a,$7c,$7d,$7e
            */

        //L000cdc:;
            /*
                                                                    move.w  $0012(a6),d2
                                                                    add.w   $000c(a6),d2	; long
                                                                    add.w   $0036(a6),d2	; long
                                                                    cmp.w   $0014(a6),d2
                                                                    beq     L000d22
                                                                    move.w  d2,$0014(a6)
                                                                    move.w  // #$17ff,d1
                                                                    cmp.w   d1,d2
                                                                    bls     L000d04
                                                                    tst.w   d2
                                                                    bpl     L000d02
                                                                    moveq.l // #$00,d2
                                                                    bra     L000d04
            */
        D2 = mm.readShort(A6 + MXWORK_CH.S0012); // note+D
        D2 = (short) ((short) D2 + (short) (mm.readInt(A6 + MXWORK_CH.S000c) >> 16)); // +bend
        D2 = (short) ((short) D2 + (short) (mm.readInt(A6 + MXWORK_CH.S0036) >> 16)); // +LfoPitch
        if (D2 != mm.readShort(A6 + MXWORK_CH.S0014)) { // break L000d22; // 直前の値と同じか比較
            mm.write(A6 + MXWORK_CH.S0014, (short) D2);
            D1 = 0x17ff;
            if (D1 < D2) { // break L000d04;
                if ((short) D2 <= 0) { // break L000d02;
                    D2 = 0;
//                    break L000d04;
                } else {
// L000d02:
            /*
                                                                    move.w  d1,d2
            */
                    D2 = (short) D1;
                }
            }
// L000d04:
            /*
                                                                    add.w   d2,d2
                                                                    add.w   d2,d2
                                                                    moveq.l // #$30,d1
                                                                    add.b   $0018(a6),d1
                                                                    bsr     L_WRITEOPM
                                                                    subq.b  // #8,d1
                                                                    move.w  d2,-(sp)
                                                                    moveq.l // #$00,d2
                                                                    move.b  (sp)+,d2
                                                                    move.b  L000d24(pc,d2.w),d2
                                                                    bsr     L_WRITEOPM
            */
            D2 = (short) (D2 * 4);
            D1 = 0x30;
            D1 += mm.readByte(A6 + MXWORK_CH.S0018);
            L_WRITEOPM();//KF
            D1 -= 8;
            D2 >>= 8;
            D2 = KeyCode[D2];
            L_WRITEOPM();//KC(OCT+NOTE)
        }
// L000d22:
            /*
                                                                    rts
            */
    }

    //

    private void L000d84() {
        byte c0;

        //L000d84:;
            /*
                                                                    bclr.b  // #$01,$0017(a6)
                                                                    beq     L000df4
                                                                    movea.l $0004(a6),a0
                                                                    andi.b  // #$c0,$001c(a6)
                                                                    move.b  (a0)+,d0
                                                                    or.b    d0,$001c(a6)
                                                                    and.w   // #$0007,d0
                                                                    move.b  L000df6(pc,d0.w),d3
                                                                    move.b  d3,$0019(a6)
                                                                    move.b  (a0)+,d0
                                                                    lsl.b   // #3,d0
                                                                    or.b    $0018(a6),d0
                                                                    move.b  d0,$001d(a6)
                                                                    moveq.l // #$40,d1
                                                                    add.b   $0018(a6),d1
                                                                    moveq.l // #$03,d0
            */
        c0 = (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & (1 << 1));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & ~(1 << 1)));
        if (c0 != 0) { // break L000df4;
            A0 = mm.readInt(A6 + MXWORK_CH.S0004);
            if (A0 == 0) A0 = FAKEA6S0004 + 0;
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) & 0xc0));
            D0 = mm.readByte(A0);
            A0++;
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) | D0));
            D0 &= 0x0007;
            D3 = CarrierSlot[D0];
            mm.write(A6 + MXWORK_CH.S0019, (byte) D3);
            D0 = mm.readByte(A0);
            A0++;
            D0 <<= 3;
            D0 |= mm.readByte(A6 + MXWORK_CH.S0018);
            mm.write(A6 + MXWORK_CH.S001d, (byte) D0);
            D1 = 0x40;
            D1 += mm.readByte(A6 + MXWORK_CH.S0018);
            D0 = 0x03;

// L000dbc:
            do {
            /*
                                                                    move.b  (a0)+,d2
                                                                    bsr     L_WRITEOPM
                                                                    addq.b  // #8,d1
                                                                    dbra    d0,L000dbc
                                                                    moveq.l // #$03,d0
            */
                D2 = mm.readByte(A0);
                A0++;
                L_WRITEOPM();
                D1 += 8;
            } while (D0-- != 0); // break L000dbc;
            D0 = 0x03;

// L000dca:
            do {
            /*
                                                                    move.b  (a0)+,d2
                                                                    lsr.b   // #1,d3
                                                                    bcc     L000dd2
                                                                    moveq.l // #$7f,d2
            */
                D2 = mm.readByte(A0);
                A0++;
                c0 = (byte) (D3 & 1);
                D3 >>= 1;
                if (c0 != 0) { // break L000dd2;
                    D2 = 0x7f;
                }
// L000dd2:
            /*
                                                                    bsr     L_WRITEOPM
                                                                    addq.b  // #8,d1
                                                                    dbra    d0,L000dca
                                                                    moveq.l // #$0f,d0
            */
                L_WRITEOPM();
                D1 += 8;
            } while (D0-- != 0); // break L000dca;
            D0 = 0x0f;

// L000dde:
            do {
            /*
                                                                    move.b  (a0)+,d2
                                                                    bsr     L_WRITEOPM
                                                                    addq.b  // #8,d1
                                                                    dbra    d0,L000dde
                                                                    st.b    $0023(a6)
                                                                    ori.b   // #$64,$0017(a6)
            */
                D2 = mm.readByte(A0);
                A0++;
                L_WRITEOPM();
                D1 += 8;
            } while (D0-- != 0); // break L000dde;
            mm.write(A6 + MXWORK_CH.S0023, (byte) Depend.SET);
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x64));
        }
// L000df4:
            /*
                                                                    rts
            L000df6:;
                    .dc.b   $08,$08,$08,$08,$0c,$0e,$0e,$0f
            */
    }


    //

    private void L000dfe() {
        int c0;
            /*
            L000e56:;
                                                                    .dc.b   $2a,$28,$25,$22,$20,$1d,$1a,$18
                                                                    .dc.b   $15,$12,$10,$0d,$0a,$08,$05,$02
            */
            /*
                                                                    moveq.l // #$00,d0
                                                                    move.b  $0022(a6),d0
                                                                    bclr.l  // #$07,d0
                                                                    bne     L000e0e
                                                                    move.b  L000e56(pc,d0.w),d0
            */
        D0 = 0x00;
        D0 = mm.readByte(A6 + MXWORK_CH.S0022);
        c0 = D0 & (1 << 7);
        D0 &= 0xff7f; // (~(1 << 7));
        if (c0 == 0) { // break L000e0e;
            D0 = Volume[D0];
        }
// L000e0e:
            /*
                                                                    add.b   L001e14(pc),d0
                                                                    bcs     L000e16
                                                                    bpl     L000e18
            */
        D0 += mm.readByte(G + MXWORK_GLOBAL.L001e14);
        if (D0 > 0xff || // break L000e16;
            (byte) D0 < 0) { // break L000e18;
// L000e16:
            /*
                                                                    moveq.l // #$7f,d0
            */
            D0 = 0x7f;
        }
// L000e18:
            /*
                                                                    add.b   $004a(a6),d0	; word
                                                                    bcs     L000e20
                                                                    bpl     L000e22
            */
        D0 += (byte) (mm.readShort(A6 + MXWORK_CH.S004a) >> 8);
        if (D0 > 0xff || // ) break L000e20;
           (byte) D0 < 0) { // break L000e22;

// L000e20:
            /*
                                                                    moveq.l // #$7f,d0
            */
            D0 = 0x7f;
        }
// L000e22:
            /*
                                                                    cmp.b   $0023(a6),d0
                                                                    beq     L000e54
            */
        if (mm.readByte(A6 + MXWORK_CH.S0023) == (byte) D0) {
            return;
        }

            /*
                                                                    ; fall down
            */
        L000e28();
    }

    private void L000e28() {
        byte c0;
            /*
                                                                    move.b  d0,$0023(a6)
                                                                    movea.l $0004(a6),a0
                                                                    addq.w  // #6,a0
                                                                    move.b  $0019(a6),d3
                                                                    moveq.l // #$60,d1
                                                                    add.b   $0018(a6),d1
                                                                    moveq.l // #$03,d4
            */
        mm.write(A6 + MXWORK_CH.S0023, (byte) D0);
        A0 = mm.readInt(A6 + MXWORK_CH.S0004);
        if (A0 == 0) A0 = FAKEA6S0004 + 0;
        A0 += 6;//TLの位置まで移動
        D3 = mm.readByte(A6 + MXWORK_CH.S0019);
        D1 = 0x60;
        D1 += mm.readByte(A6 + MXWORK_CH.S0018);
        D4 = 0x03;

// L000e3e:
        do {
            /*
                                                                    move.b  (a0)+,d2
                                                                    lsr.b   // #1,d3
                                                                    bcc     L000e4e
                                                                    add.b   d0,d2
                                                                    bpl     L000e4a
                                                                    moveq.l // #$7f,d2
            */
            D2 = mm.readByte(A0++);
            c0 = (byte) (D3 & 1);
            D3 >>= 1;
            if (c0 != 0) { // break L000e4e;
                D2 += D0;
                if ((byte) D2 < 0) { // break L000e4a;
                    D2 = 0x7f;
                }
// L000e4a:
            /*
                                                                    bsr     L_WRITEOPM
            */
                L_WRITEOPM();
            }
// L000e4e:
            /*
                                                                    addq.b  // #8,d1
                                                                    dbra    d4,L000e3e
            */
            D1 += 8;
        } while (D4-- != 0); // break L000e3e;

        //L000e54:;
            /*
                                                                    rts
            */
    }


    //

    private void L000e66() {
        byte c0;
            /*
                                                                    bclr.b  // #$02,$0017(a6)
                                                                    beq     L000e7c
                                                                    move.b  $001c(a6),d2
                                                                    moveq.l // #$20,d1
                                                                    add.b   $0018(a6),d1
                                                                    bra     L_WRITEOPM
            */
        c0 = (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & (1 << 2));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & ~(1 << 2)));
        if (c0 != 0) { // break L000e7c;
            D2 = mm.readByte(A6 + MXWORK_CH.S001c);
            D1 = 0x20;
            D1 += mm.readByte(A6 + MXWORK_CH.S0018);
            L_WRITEOPM();
        }
// L000e7c:
            /*
                                                                    rts
            */
    }

    //
    private void L000e7e() {
        byte c0;
            /*
            L000fba:;
                                                                    .dc.b   $0f,$0f,$0f,$0e,$0e,$0e,$0d,$0d
                                                                    .dc.b   $0d,$0c,$0c,$0b,$0b,$0b,$0a,$0a
                                                                    .dc.b   $0a,$09,$09,$08,$08,$08,$07,$07
                                                                    .dc.b   $07,$06,$06,$05,$05,$05,$04,$04
                                                                    .dc.b   $04,$03,$03,$02,$02,$02,$01,$01
                                                                    .dc.b   $01,$00,$00,$ff
            */
            /*
                                                                    bset.b  // #$03,$0016(a6)
                                                                    bne     L000e7c
                                                                    btst.b  // #$04,$0016(a6)
                                                                    beq     L000e92
                                                                    bsr     L000ff6
            */
        c0 = (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 3));
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | (1 << 3)));
        if (c0 != 0) return;
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 4)) != 0) { // break L000e92;
            L000ff6();
        }
// L000e92:
            /*
                                                                    tst.b   $0018(a6)
                                                                    bmi     L000eb2
                                                                    move.b  $001d(a6),d2
                                                                    lea.l   L00223c(pc),a2
                                                                    move.b  d2,$00(a2,d7.w)
                                                                    lea.l   L001bb4(pc),a2
                                                                    move.b  d2,$00(a2,d7.w)
                                                                    moveq.l // #$08,d1
                                                                    bra     L_WRITEOPM
            */
        if (mm.readByte(A6 + MXWORK_CH.S0018) >= 0) { // break L000eb2;
            D2 = mm.readByte(A6 + MXWORK_CH.S001d);
            A2 = G + MXWORK_GLOBAL.L00223c;
            mm.write(A2 + D7, (byte) D2);
            A2 = G + MXWORK_GLOBAL.L001bb4;
            mm.write(A2 + D7, (byte) D2);
            D1 = 0x08;
            L_WRITEOPM();
            return;
        }
// L000eb2:
            /*
                                                                    move.b  (L002231),d0
                                                                    beq     L000f26
                                                                    tst.b   (L001e09)
                                                                    bne     L000f26
                                                                    moveq.l // #$00,d0
                                                                    move.w  $0012(a6),d0
                                                                    lsr.w   // #6,d0
                                                                    move.b  $001c(a6),d2
                                                                    move.b  d2,d1
                                                                    and.w   // #$0003,d1
                                                                    beq     L000ed8
                                                                    cmp.w   // #$0003,d1
                                                                    bne     L000edc
            */
        D0 = mm.readByte(G + MXWORK_GLOBAL.L002231);
        if (D0 == 0) return; // break L000f26;
        if (mm.readByte(G + MXWORK_GLOBAL.L001e09) != 0) return; // break L000f26;
        D0 = 0x00;
        D0 = mm.readShort(A6 + MXWORK_CH.S0012);
        D0 >>= 6;
        D2 = mm.readByte(A6 + MXWORK_CH.S001c);
        D1 = D2;
        D1 &= 0x0003;
        if (D1 == 0 || // break L000ed8;
            D1 == 0x0003) { // break L000edc;
// L000ed8:
    /*
                                                            eori.w  // #$0003,d1
    */
                D1 ^= 0x0003;
        }
// L000edc:
    /*
                                                            and.w   // #$001c,d2
                                                            lsl.w   // #6,d2
                                                            or.w    d1,d2
                                                            tst.b   (L001df4)
                                                            bne     L000f28
                                                            tst.b   (L001e15)
                                                            beq     L000ef4
                                                            andi.b  // #$fc,d2
    */
        D2 &= 0x001c;
        D2 <<= 6;
        D2 |= D1;
        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) == 0) { // break L000f28;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e15) != 0) { // break L000ef4;
                D2 &= 0xfc;
            }
// L000ef4:
    /*
                                                            lsl.w   // #3,d0
                                                            movea.l (L00222c),a1
                                                            lea.l   $00(a1,d0.w),a0
                                                            adda.l  (a0)+,a1
                                                            addq.w  // #2,a0
                                                            move.w  (a0)+,d3
                                                            beq     L000f26
                                                            moveq.l // #$67,d0
                                                            moveq.l // #$00,d1
                                                            trap    // #15
                                                            moveq.l // #$60,d0
                                                            move.w  d2,d1
                                                            moveq.l // #$00,d2
                                                            move.w  d3,d2
                                                            trap    // #15
                                                            lea.l   L00223c(pc),a2
                                                            clr.b   $00(a2,d7.w)
                                                            lea.l   L001bb4(pc),a2
                                                            clr.b   $00(a2,d7.w)
    */
            D0 <<= 3;
            A1 = mm.readInt(G + MXWORK_GLOBAL.L00222c);
            A0 = A1 + D0;
            A1 += Depend.getblong(mm, A0);
            A0 += 4;
            A0 += 2;
            D3 = Depend.getbword(mm, A0);
            A0 += 2;
            if (D3 == 0x0000) return; // break L000f26;
            ADPCMMOD_END();
            D1 = D2;
            D2 = D3;
            if (D2 > 0xff00) D2 = 0xff00;  // DMAサイズ制限
            ADPCMOUT();
            A2 = G + MXWORK_GLOBAL.L00223c + 0;
            mm.write(A2 + D7, (byte) Depend.CLR);
            A2 = G + MXWORK_GLOBAL.L001bb4 + 0;
            mm.write(A2 + D7, (byte) Depend.CLR);
        }
// L000f26:
            /*
                                                                    rts
            */
//        return;

// L000f28:
            /*
                                                                    moveq.l // #$00,d1
                                                                    move.b  $0004_b(a6),d1
                                                                    lsl.l   // #5,d1
                                                                    add.l   d1,d0
                                                                    add.l   d1,d1
                                                                    add.l   d1,d0
                                                                    lsl.l   // #3,d0
                                                                    movea.l (L00222c),a1
                                                                    lea.l   $00(a1,d0.l),a0
                                                                    move.l  $0004(a0),d3
                                                                    beq     L000f26
                                                                    adda.l  (a0),a1
                                                                    lea.l   L002248(pc),a0	; 不要
                                                                    cmpa.l  a0,a1			; 不要
                                                                    bcs     L000fb8			; 不要
                                                                    move.l  a1,d0			; 不要
                                                                    add.l   d3,d0			; 不要
                                                                    bcs     L000fb8			; 不要
                                                                    cmp.l   (L001bb0),d0	; 不要
                                                                    bcc     L000fb8			; 不要
                                                                    move.b  $0018(a6),d0
                                                                    and.w   // #$0007,d0
                                                                    moveq.l // #$00,d1
                                                                    move.b  $0022(a6),d1
                                                                    bclr.l  // #$07,d1
                                                                    bne     L000f78
                                                                    lea.l   L000e56(pc),a2
                                                                    move.b  $00(a2,d1.w),d1
            */
        D1 = 0x00;
        D1 = mm.readByte(A6 + MXWORK_CH.S0004_b);
        D1 <<= 5;
        D0 += D1;
        D1 += D1;
        D0 += D1;
        D0 <<= 3;
        A1 = mm.readInt(G + MXWORK_GLOBAL.L00222c);
        A0 = A1 + D0;
        D3 = Depend.getblong(mm, A0 + 4);
        if (D3 == 0) return; // break L000f26;
        A1 += Depend.getblong(mm, A0);
        D0 = (D0 & 0xffffff00) + mm.readByte(A6 + MXWORK_CH.S0018);
        D0 &= 0xffff0007;
        D1 = 0x00;
        D1 = mm.readByte(A6 + MXWORK_CH.S0022);
        c0 = (byte) (D1 & (1 << 7));
        D1 &= 0xffffff7f;// (~(1 << 7));
        if (c0 == 0) { // break L000f78;
            //A2 = Volume[0];
            //D1 = A2[D1];
            D1 = Volume[D1];
        }
// L000f78:
            /*
                                                                    add.b   L001e14(pc),d1
                                                                    bmi     L000f84
                                                                    cmp.b   // #$2b,d1                 ;'+'
                                                                    bcs     L000f8a
            */
        D1 += mm.readByte(G + MXWORK_GLOBAL.L001e14);
        if ((byte) D1 < 0 || // ) break L000f84;
             D1 >= 0x2b) { // break L000f8a;

// L000f84:
            /*
                                                                    moveq.l // #$00,d1
                                                                    clr.b   d2
                                                                    bra     L000f8e
            */
            D1 = 0x00;
            D2 &= 0xffffff00;
//            break L000f8e;
        } else {
// L000f8a:
            /*
                                                                    move.b  L000fba(pc,d1.w),d1
            */
            D1 = PCMVolume[D1];
        }
// L000f8e:
            /*
                                                                    swap.w  d1
                                                                    move.w  d2,d1
                                                                    moveq.l // #$00,d2
                                                                    trap    // #2
                                                                    move.b  $0018(a6),d0
                                                                    and.w   // #$0007,d0
                                                                    move.l  d3,d2
                                                                    andi.l  // #$00ffffff,d2
                                                                    trap    // #2
                                                                    lea.l   L00223c(pc),a2
                                                                    clr.b   $0008(a2)
                                                                    lea.l   L001bb4(pc),a2
                                                                    clr.b   $00(a2,d7.w)
            */
        D1 <<= 16;
        D1 |= (D2 & 0xffff);
        D2 = 0x00;
        PCM8_SUB();
        D0 = (D0 & 0xffffff00) + mm.readByte(A6 + MXWORK_CH.S0018);
        D0 &= 0xffff0007;
        D2 = D3;
        D2 &= 0xffffff;
        PCM8_SUB();
        A2 = G + MXWORK_GLOBAL.L00223c + 0;
        mm.write(A2 + 0x0008, (byte) Depend.CLR);
        A2 = G + MXWORK_GLOBAL.L001bb4 + 0;
        mm.write(A2 + D7, (byte) Depend.CLR);

        //L000fb8:;
            /*
                                                                    rts
            */
    }

    //
    private void L000fe6() {
        byte c0;
            /*
                                                                    bclr.b  // #$03,$0016(a6)
                                                                    beq     L00103a
                                                                    btst.b  // #$04,$0016(a6)
                                                                    bne     L00103a
            */
        c0 = (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 3));
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & ~(1 << 3)));
        if (c0 == 0) return;
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 4)) != 0) return;
            /*
                                                                    ; fall down;
            */
        L000ff6();
    }

    private void L000ff6() {
            /*
                                                                    move.b  $0018(a6),d2
                                                                    bmi     L001012
                                                                    moveq.l // #$08,d1
                                                                    lea.l   L00223c(pc),a2
                                                                    move.b  d2,$00(a2,d7.w)
                                                                    lea.l   L001bb4(pc),a2
                                                                    move.b  d2,$00(a2,d7.w)
                                                                    bra     L_WRITEOPM
            */
        D2 = mm.readByte(A6 + MXWORK_CH.S0018);
        if ((byte) D2 >= 0) { // break L001012;
            D1 = 0x08;
            A2 = G + MXWORK_GLOBAL.L00223c + 0;
            mm.write(A2 + D7, (byte) D2);
            A2 = G + MXWORK_GLOBAL.L001bb4 + 0;
            mm.write(A2 + D7, (byte) D2);
            L_WRITEOPM();
            return;
        }
// L001012:
            /*
                                                                    move.b  (L002231),d0
                                                                    beq     L00103a
                                                                    tst.b   (L001e09)
                                                                    bne     L00103a
                                                                    tst.b   (L001df4)
                                                                    beq     L00103c
                                                                    move.b  $0018(a6),d0
                                                                    and.w   // #$0007,d0
                                                                    moveq.l // #$00,d1
                                                                    move.b  $0022(a6),d1
                                                                    swap.w  d1
                                                                    move.w  d2,d1
                                                                    moveq.l // #$00,d2
                                                                    trap    // #2
            */
        D0 = mm.readByte(G + MXWORK_GLOBAL.L002231);
        if (D0 == 0) return; // break L00103a;
        if (mm.readByte(G + MXWORK_GLOBAL.L001e09) != 0) return; // break L00103a;
        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L00103c;
            D0 = mm.readByte(A6 + MXWORK_CH.S0018);
            D0 &= 0x0007;
            D1 = 0x00;
            D1 = mm.readByte(A6 + MXWORK_CH.S0022);
            D1 <<= 16;
            D1 |= (D2 & 0xffff);
            D2 = 0;
            PCM8_SUB();

// L00103a:
            /*
                                                                    rts
            */
            return;
        }
// L00103c:
            /*
                                                                    tst.b   $0017(a6)
                                                                    bne     L001048
                                                                    moveq.l // #$67,d0
                                                                    moveq.l // #$01,d1
                                                                    trap    // #15
            */
        if (mm.readByte(A6 + MXWORK_CH.S0017) == 0) { // break L001048;
            ADPCMMOD_STOP();
        }
// L001048:
            /*
                                                                    moveq.l // #$67,d0
                                                                    moveq.l // #$00,d1
                                                                    trap    // #15
                                                                    rts
            */
        ADPCMMOD_END();
    }

    //
    private void L001050() {
            /*
                                                                    tst.b   $0018(a6)
                                                                    bmi     L001092
                                                                    tst.b   $0016(a6)
                                                                    bpl     L00106a
                                                                    tst.b   $0020(a6)
                                                                    bne     L00106a
                                                                    move.l  $0008(a6),d0
                                                                    add.l   d0,$000c(a6)
            */
        if (mm.readByte(A6 + MXWORK_CH.S0018) < 0) return; // break L001092;
        if (mm.readByte(A6 + MXWORK_CH.S0016) < 0) { // break L00106a;
            if (mm.readByte(A6 + MXWORK_CH.S0020) == 0) { // break L00106a;
                D0 = mm.readInt(A6 + MXWORK_CH.S0008);
                mm.write(A6 + MXWORK_CH.S000c, mm.readInt(A6 + MXWORK_CH.S000c) + D0);
            }
        }
// L00106a:
            /*
                                                                    tst.b   $0024(a6)
                                                                    beq     L00107c
                                                                    tst.b   $0020(a6)
                                                                    bne     L001092
                                                                    tst.b   $0025(a6)
                                                                    bne     L001094
            */
        if (mm.readByte(A6 + MXWORK_CH.S0024) != 0) { // break L00107c;
            if (mm.readByte(A6 + MXWORK_CH.S0020) != 0) return; // break L001092;
            if (mm.readByte(A6 + MXWORK_CH.S0025) != 0) {
                L001094();
                return;
            }
        }
// L00107c:
            /*
                                                                    btst.b  // #$05,$0016(a6)
                                                                    beq     L001086
                                                                    bsr     L0010b4
            */
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 5)) != 0) { // break L001086;
            L0010b4();
        }
// L001086:
            /*
                                                                    btst.b  // #$06,$0016(a6)
                                                                    beq     L001092
                                                                    bsr     L001116
            */
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 6)) == 0) return; // break L001092;
        L001116();
// L001092:
            /*
                                                                    rts

            */
    }

    private void L001094() {
            /*
                                                                    subq.b  // #1,$0025(a6)
                                                                    bne     L0010b2
                                                                    btst.b  // #$05,$0016(a6)
                                                                    beq     L0010a6
                                                                    bsr     L001562
            */
        mm.write(A6 + MXWORK_CH.S0025, (byte) (mm.readByte(A6 + MXWORK_CH.S0025) - 1));
        if (mm.readByte(A6 + MXWORK_CH.S0025) != 0) return; // break L0010b2;
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 5)) != 0) { // break L0010a6;
            //	L001562();

            //L001562:;
            /*
                                                                    move.w  $003a(a6),$003e(a6)
                                                                    move.l  $002e(a6),$0032(a6)
                                                                    move.l  $002a(a6),$0036(a6)
                                                                    rts
            */
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003a));
            mm.write(A6 + MXWORK_CH.S0032, mm.readInt(A6 + MXWORK_CH.S002e));
            mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S002a));
        }
// L0010a6:
            /*
                                                                    btst.b  // #$06,$0016(a6)
                                                                    beq     L0010b2
                                                                    bsr     L0015d0
            */
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 6)) != 0) { // break L0010b2;
            L0015d0();
        }
// L0010b2:
            /*
                                                                    rts
            */
    }

    //
    private void L0010b4() {
            /*
                                                                    move.l  $0032(a6),d1
                                                                    movea.l $0026(a6),a0
                                                                    jmp     (a0)
            */
        D1 = mm.readInt(A6 + MXWORK_CH.S0032);
        A0 = mm.readInt(A6 + MXWORK_CH.S0026);
        if (A0 < 0x05) {
            L0010b4_Table[A0].run();
        }
        MX_ABORT();
    }

    //
    private void L0010be() {
            /*
                                                                    add.l   d1,$0036(a6)
                                                                    subq.w  // #1,$003e(a6)
                                                                    bne     L0010d2
                                                                    move.w  $003c(a6),$003e(a6)
                                                                    neg.l   $0036(a6)
            */
        mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S0036) + D1);
        mm.write(A6 + MXWORK_CH.S003e, (short) (mm.readShort(A6 + MXWORK_CH.S003e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) { // break L0010d2;
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
            mm.write(A6 + MXWORK_CH.S0036, -mm.readInt(A6 + MXWORK_CH.S0036));
        }
// L0010d2:
            /*
                                                                    rts
            */
    }

    //
    private void L0010d4() {
            /*
                                                                    move.l  d1,$0036(a6)
                                                                    subq.w  // #1,$003e(a6)
                                                                    bne     L0010e8
                                                                    move.w  $003c(a6),$003e(a6)
                                                                    neg.l   $0032(a6)
            */
        mm.write(A6 + MXWORK_CH.S0036, D1);
        mm.write(A6 + MXWORK_CH.S003e, (short) (mm.readShort(A6 + MXWORK_CH.S003e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) { // break L0010e8;
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
            mm.write(A6 + MXWORK_CH.S0032, -mm.readInt(A6 + MXWORK_CH.S0032));
        }
// L0010e8:
            /*
                                                                    rts
            */
    }

    //
    private void L0010ea() {
            /*
                                                                    add.l   d1,$0036(a6)
                                                                    subq.w  // #1,$003e(a6)
                                                                    bne     L0010fe
                                                                    move.w  $003c(a6),$003e(a6)
                                                                    neg.l   $0032(a6)
            */
        mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S0036) + D1);
        mm.write(A6 + MXWORK_CH.S003e, (short) (mm.readShort(A6 + MXWORK_CH.S003e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) { // break L0010fe;
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
            mm.write(A6 + MXWORK_CH.S0032, -mm.readInt(A6 + MXWORK_CH.S0032));
        }
// L0010fe:
            /*
                                                                    rts
            */
    }

    //
    private void L001100() {
            /*
                                                                    subq.w  // #1,$003e(a6)
                                                                    bne     L001114
                                                                    bsr     L00117a
                                                                    muls.w  d1,d0
                                                                    move.l  d0,$0036(a6)
                                                                    move.w  $003c(a6),$003e(a6)
            */
        mm.write(A6 + MXWORK_CH.S003e, (short) (mm.readShort(A6 + MXWORK_CH.S003e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) { // break L001114;
            L00117a();
            D0 = (short) D0 * (short) D1;
            mm.write(A6 + MXWORK_CH.S0036, D0);
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
        }
// L001114:
            /*
                                                                    rts
            */
    }

    //
    private void L001116() {
            /*
                                                                    move.w  $0048(a6),d1
                                                                    movea.l $0040(a6),a0
                                                                    jmp     (a0)
            */
        D1 = mm.readShort(A6 + MXWORK_CH.S0048);
        A0 = mm.readInt(A6 + MXWORK_CH.S0040);
        if (A0 < 0x05) {
            L001116Table[A0].run();
        }
        MX_ABORT();
    }

    //
    private void L001120() {
            /*
                                                                    add.w   d1,$004a(a6)
                                                                    subq.w  // #1,$004e(a6)
                                                                    bne     L001136
                                                                    move.w  $004c(a6),$004e(a6)
                                                                    move.w  $0046(a6),$004a(a6)
            */
        mm.write(A6 + MXWORK_CH.S004a, (short) (mm.readShort(A6 + MXWORK_CH.S004a) + (short) D1));
        mm.write(A6 + MXWORK_CH.S004e, (short) (mm.readShort(A6 + MXWORK_CH.S004e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) { // break L001136;
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S004a, mm.readShort(A6 + MXWORK_CH.S0046));
        }
// L001136:
            /*
                                                                    rts
            */
    }

    //
    private void L001138() {
            /*
                                                                    subq.w  // #1,$004e(a6)
                                                                    bne     L00114c
                                                                    move.w  $004c(a6),$004e(a6)
                                                                    add.w   d1,$004a(a6)
                                                                    neg.w   $0048(a6)
            */
        mm.write(A6 + MXWORK_CH.S004e, (short) (mm.readShort(A6 + MXWORK_CH.S004e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) { // break L00114c;
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S004a, (short) (mm.readShort(A6 + MXWORK_CH.S004a) + (short) D1));
            mm.write(A6 + MXWORK_CH.S0048, (short) (-mm.readShort(A6 + MXWORK_CH.S0048)));
        }
// L00114c:
            /*
                                                                    rts
            */
    }

    //
    private void L00114e() {
            /*
                                                                    add.w   d1,$004a(a6)
                                                                    subq.w  // #1,$004e(a6)
                                                                    bne     L001162
                                                                    move.w  $004c(a6),$004e(a6)
                                                                    neg.w   $0048(a6)
            */
        mm.write(A6 + MXWORK_CH.S004a, (short) (mm.readShort(A6 + MXWORK_CH.S004a) + (short) D1));
        mm.write(A6 + MXWORK_CH.S004e, (short) (mm.readShort(A6 + MXWORK_CH.S004e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) { // break L001162;
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S0048, (short) (-mm.readShort(A6 + MXWORK_CH.S0048)));
        }
// L001162:
            /*
                                                                    rts
            */
    }

    //
    private void L001164() {
            /*
                                                                    subq.w  // #1,$004e(a6)
                                                                    bne     L001178
                                                                    bsr     L00117a
                                                                    muls.w  d0,d1
                                                                    move.w  $004c(a6),$004e(a6)
                                                                    move.w  d1,$004a(a6)
            */
        mm.write(A6 + MXWORK_CH.S004e, (short) (mm.readShort(A6 + MXWORK_CH.S004e) - 1));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) { // break L001178;
            L00117a();
            D1 = (short) D1 * (short) D0;
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S004a, (short) D1);
        }
// L001178:
            /*
                                                                    rts
            */
    }

    //
    private void L00117a() {
            /*
                                                                    .dc.w   $1234
            */
        short L001190 = 0x1234;
            /*
                                                                    move.w  (L001190),d0
                                                                    mulu.w  // #$c549,d0
                                                                    add.l   // #$0000000c,d0
                                                                    move.w  d0,(L001190)
                                                                    lsr.l   // #8,d0
                                                                    rts
            */
        D0 = L001190;
        D0 *= 0xc549;
        D0 += 0x0c;
        L001190 = (short) D0;
        D0 >>= 8;
    }

    //
    private void L001192() {
            /*
                                                                    lea.l   L001df6(pc),a0
                                                                    tst.b   $00(a0,d7.w)
                                                                    bne     L00119e
                                                                    rts
            */
        A0 = G + MXWORK_GLOBAL.L001df6 + 0;
        if (mm.readByte(A0 + D7) == 0) { // break L00119e;
            return;
        }
//        L00119e:
            /*
                                                                    clr.b   $00(a0,d7.w)
                                                                    cmp.w   // #$0009,d7
                                                                    bcc     L0011ac
                                                                    clr.b   $27(a5,d7.w)	; L002233(d7.w)
            */
        mm.write(A0 + D7, (byte) Depend.CLR);
        if (D7 < 0x09) { // break L0011ac;
            mm.write(G + MXWORK_GLOBAL.L002233 + D7, (byte) Depend.CLR);
        }
//        L0011ac:
            /*
                                                                    andi.b  // #$f7,$0017(a6)
                                                                    bra     L0011d4
            */
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & 0xf7));
        L0011d4();
    }

    //
    private void L0011b4() {
            /*
                                                                    btst.b  // #$03,$0017(a6)
                                                                    bne     L001192
                                                                    btst.b  // #$02,$0016(a6)
                                                                    bne     L0011ce
                                                                    subq.b  // #1,$001b(a6)
                                                                    bne     L0011ce
                                                                    bsr     L000fe6
                                                                    ; fall down
            */
        if ((mm.readByte(A6 + MXWORK_CH.S0017) & (byte) (1 << 3)) != 0) {
            L001192();
            return;
        }
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (byte) (1 << 2)) != 0) {
            L0011ce();
            return;
        }
        mm.write(A6 + MXWORK_CH.S001b, (byte) (mm.readByte(A6 + MXWORK_CH.S001b) - 1));
        if (mm.readByte(A6 + MXWORK_CH.S001b) != 0) {
            L0011ce();
            return;
        }
        L000fe6();
        L0011ce();
    }

    //
    private void L0011ce() {
            /*
                                                                    subq.b  // #1,$001a(a6)
                                                                    bne     L001224
                                                                    ; fall down
            */
        mm.write(A6 + MXWORK_CH.S001a, (byte) (mm.readByte(A6 + MXWORK_CH.S001a) - 1));
        if (mm.readByte(A6 + MXWORK_CH.S001a) != 0) return;
        L0011d4();
    }

    //
    private void L0011d4() {
        /*
        L001252:;
                .dc.w   L001292-L001252
                .dc.w   L0012a6-L001252
                .dc.w   L0012be-L001252
                .dc.w   L0012e6-L001252
                .dc.w   L00131c-L001252
                .dc.w   L001328-L001252
                .dc.w   L001344-L001252
                .dc.w   L001364-L001252
                .dc.w   L00136a-L001252
                .dc.w   L001372-L001252
                .dc.w   L001376-L001252
                .dc.w   L00139a-L001252
                .dc.w   L0013ba-L001252
                .dc.w   L0013c6-L001252
                .dc.w   L0013dc-L001252
                .dc.w   L001492-L001252
                .dc.w   L001498-L001252
                .dc.w   L0014b0-L001252
                .dc.w   L0014dc-L001252
                .dc.w   L0014fc-L001252
                .dc.w   L001590-L001252
                .dc.w   L0015fe-L001252
                .dc.w   L001656-L001252
                .dc.w   L00165c-L001252
                .dc.w   L001694-L001252
                .dc.w   L001442-L001252
                .dc.w   L001442-L001252
                .dc.w   L001442-L001252
                .dc.w   L001442-L001252
                .dc.w   L001442-L001252
                .dc.w   L001442-L001252
                .dc.w   L001442-L001252
        */
            /*
                                                                    movea.l (a6),a4
                                                                    andi.b  // #$7b,$0016(a6)
            */
        A4 = mm.readInt(A6 + MXWORK_CH.S0000);
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0x7b));

// L0011dc:
        while (true) {
            /*
                                                                    moveq.l // #$00,d0
                                                                    moveq.l // #$00,d1
                                                                    move.b  (a4)+,d0
                                                                    move.b  d0,d1
                                                                    bpl     L001216
                                                                    cmp.b   // #$e0,d0
                                                                    bcc     L00122e
                                                                    and.w   // #$007f,d0
                                                                    lsl.w   // #6,d0
                                                                    addq.w  // #5,d0
                                                                    add.w   $0010(a6),d0
                                                                    move.w  d0,$0012(a6)
                                                                    ori.b   // #$01,$0016(a6)
                                                                    move.b  $001f(a6),$0020(a6)
                                                                    moveq.l // #$00,d0
                                                                    move.b  (a4)+,d0
                                                                    move.b  $001e(a6),d1
                                                                    bmi     L001226
                                                                    mulu.w  d0,d1
                                                                    lsr.w   // #3,d1
            */

// #if LOGSEQ
//                FILE* fout;
//                fout = fopen("c:\\temp\\seq.Log", "ab+");
//                fprintf(fout, "%2d %08lX %02X\n", A6->S0018, (DWORD)A4, *(A4));
//                fclose(fout);
// #endif

            D0 = 0x00;
            D1 = 0x00;
            D0 = mm.readByte(A4++);
            D1 = D0;
            if ((byte) D1 >= 0) break L001216;
            if (D0 < 0xe0) { // break L00122e;
                D0 &= 0x007f;
                D0 <<= 6;
                D0 += 5;
                D0 += mm.readShort(A6 + MXWORK_CH.S0010);
                mm.write(A6 + MXWORK_CH.S0012, (short) D0);
                mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x01));
                mm.write(A6 + MXWORK_CH.S0020, mm.readByte(A6 + MXWORK_CH.S001f));
                D0 = 0x00;
                D0 = mm.readByte(A4++);
                D1 = mm.readByte(A6 + MXWORK_CH.S001e);
                if ((byte) D1 >= 0) break L001226;
                D1 *= D0;
                D1 = (D1 & 0xffff) >> 3;

// L001216:
                while (true) {
            /*
                                                                    addq.w  // #1,d1
                                                                    move.b  d1,$001b(a6)
                                                                    addq.w  // #1,d0
                                                                    move.b  d0,$001a(a6)
                                                                    move.l  a4,(a6)
            */
                    D1++;
                    mm.write(A6 + MXWORK_CH.S001b, (byte) D1);
                    D0++;
                    mm.write(A6 + MXWORK_CH.S001a, (byte) D0);
                    mm.write(A6 + MXWORK_CH.S0000, A4);

                    //L001224:;
            /*
                                                                    rts
            */
                    return;

// L001226:
            /*
                                                                    add.b   d0,d1
                                                                    bcs     L001216
                                                                    moveq.l // #$00,d1
                                                                    bra     L001216
            */
                    D1 &= 0xff;
                    D1 += (D0 & 0xff);
                    if (D1 >= 0x100) continue; // break L001216;
                    D1 = 0x00;
//                break L001216;
                }
            }
// L00122e:
            /*
                                                                    ext.w   d0
                                                                    not.w   d0
                                                                    add.w   d0,d0
                                                                    move.w  L001252(pc,d0.w),d0
                                                                    pea.l   L0011dc(pc)
                                                                    jmp     L001252(pc,d0.w)
            */
            D0 ^= 0xff;
            DisposeStack_L00122e = Depend.FALSE;
            L001252[D0].run();
            if (DisposeStack_L00122e != Depend.FALSE) return;
//            break L0011dc;
        }
    }

    //
// #if false // 本体側へ移動
//        static void L001240(
//        	void
//        ) {
//
//        L001240:;
//        /
//        														ext.w   d0
//        														not.w   d0
//        														add.w   d0,d0
//        														move.w  L001252(pc,d0.w),d0
//        														pea.l   L00177a(pc)
//        														jmp     L001252(pc,d0.w)
//
//        	D0 ^= 0xff;
//        	DisposeStack_L00122e = FALSE;
//        	L001252[D0]();
//        	if ( DisposeStack_L00122e ) return;
//        	L00177a();
//        }
// #endif

    //
    private void L001292() {                               // @@ @t
            /*
                                                                    moveq.l // #$12,d1
                                                                    move.b  (a4)+,d2
                                                                    move.b  d2,(L001e0c)
                                                                    tst.b   (L001e08)
                                                                    bne     L0012a4
                                                                    bra     L_WRITEOPM
            */
        D1 = 0x12;
        D2 = mm.readByte(A4++);
        mm.write(G + MXWORK_GLOBAL.L001e0c, (byte) D2);
        mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e08) == 0) { // break L0012a4;
            L_WRITEOPM();
        }
// L0012a4:
            /*
                                                                    rts
            */
    }

    //
    private void L0012a6() {
            /*
                                                                    move.b  (a4)+,d1
                                                                    move.b  (a4)+,d2
                                                                    cmp.b   // #$12,d1
                                                                    bne     L0012ba
                                                                    tst.b   (L001e08)
                                                                    bne     L0012a4	; rts
                                                                    move.b  d2,(L001e0c)
            */
        D1 = mm.readByte(A4++);
        D2 = mm.readByte(A4++);
        if (D1 == 0x12) { // break L0012ba;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e08) != 0) return;
            mm.write(G + MXWORK_GLOBAL.L001e0c, (byte) D2);
            mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
        }
// L0012ba:
            /*
                                                                    bra     L_WRITEOPM
            */
        L_WRITEOPM();
    }

    //
    private void L0012be() {                                  // @@ @
            /*
                                                                    tst.b   $0018(a6)
                                                                    bmi     L0012e0
                                                                    move.b  (a4)+,d0
                                                                    movea.l (L002228),a0
                                                                    bra     L0012d0
            */
        if (mm.readByte(A6 + MXWORK_CH.S0018) >= 0) { // break L0012e0;
            D0 = mm.readByte(A4++);
            A0 = mm.readInt(G + MXWORK_GLOBAL.L002228);
//            break L0012d0;
            boolean L0012d0 = true;

// L0012cc:
            do {
            /*
                                                                    lea.l   $001a(a0),a0
            */
                if (!L0012d0) {
                    A0 += 0x1a;
                    L0012d0 = false;
                }
// L0012d0:
                // checker
                if (A0 >= mm.readInt(G + MXWORK_GLOBAL.L001e34) + mm.readInt(G + MXWORK_GLOBAL.L002220)) {
                    //		G.FATALERROR = 0x0012d0;
                    //		G.FATALERRORADR = (int)A4;
                    return;
                }
                // checker end
            /*
                                                                    cmp.b   (a0)+,d0
                                                                    bne     L0012cc
                                                                    move.l  a0,$0004(a6)
                                                                    ori.b   // #$02,$0017(a6)
                                                                    rts
            */
            } while (mm.readByte(A0++) != (byte) D0); // break L0012cc;
            mm.write(A6 + MXWORK_CH.S0004, A0);
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x02));
            return;
        }
// L0012e0:
            /*
                                                                    move.b  (a4)+,$0004_b(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S0004_b, mm.readByte(A4++));
    }

    //
    private void L0012e6()                                  // @@ p
    {
            /*
                                                                    tst.b   $0018(a6)
                                                                    bmi     L001302
                                                                    move.b  $001c(a6),d0
                                                                    ror.w   // #6,d0
                                                                    move.b  (a4)+,d0
                                                                    rol.w   // #6,d0
                                                                    move.b  d0,$001c(a6)
                                                                    ori.b   // #$04,$0017(a6)
                                                                    rts
            */
        if (mm.readByte(A6 + MXWORK_CH.S0018) >= 0) { // break L001302;
            D0 = mm.readByte(A6 + MXWORK_CH.S001c);
            D0 &= 0x3f;
            D0 |= (short) ((mm.readByte(A4++)) << 6);
            mm.write(A6 + MXWORK_CH.S001c, (byte) D0);
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x04));
            return;
        }
// L001302:
            /*
                                                                    move.b  (a4)+,d0
                                                                    beq     L00130c
                                                                    cmp.b   // #$03,d0
                                                                    bne     L001310
            */
        D0 = mm.readByte(A4++);
        if (D0 == 0 || // ) break L00130c;
            D0 == 0x03) { // break L001310;

// L00130c:
            /*
                                                                    eori.b  // #$03,d0
            */
            D0 ^= 0x03;
        }
// L001310:
            /*
                                                                    andi.b  // #$fc,$001c(a6)
                                                                    or.b    d0,$001c(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) & 0xfc));
        mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) | D0));
    }

    //
    private void L00131c() {                              // @@ v volume(0xFB)
            /*
                                                                    move.b  (a4)+,$0022(a6)
                                                                    ori.b   // #$01,$0017(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S0022, mm.readByte(A4++));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
    }

    //
    private void L001328() {
            /*
                                                                    move.b  $0022(a6),d2
                                                                    bmi     L00133c
                                                                    beq     L00133a
            */
        D2 = mm.readByte(A6 + MXWORK_CH.S0022);
        if ((byte) D2 >= 0) { // break L00133c;
            if (D2 != 0) { // break L00133a;

                //L001330:;
            /*
                                                                    subq.b  // #1,$0022(a6)
                                                                    ori.b   // #$01,$0017(a6)
            */
                mm.write(A6 + MXWORK_CH.S0022, (byte) (mm.readByte(A6 + MXWORK_CH.S0022) - 1));
                mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
            }
// L00133a:
            /*
                                                                    rts
            */
            return;
        }
// L00133c:
            /*
                                                                    cmp.b   // #-$01,d2
                                                                    bne     L001350
                                                                    rts
            */
        if (D2 == 0xff) return;

        //L001350:;
            /*
                                                                    addq.b  // #1,$0022(a6)
                                                                    ori.b   // #$01,$0017(a6)
            */
        mm.write(A6 + MXWORK_CH.S0022, (byte) (mm.readByte(A6 + MXWORK_CH.S0022) + 1));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
    }

    //
    private void L001330() {
            /*
                                                                    subq.b  // #1,$0022(a6)
                                                                    ori.b   // #$01,$0017(a6)
            */
        mm.write(A6 + MXWORK_CH.S0022, (byte) (mm.readByte(A6 + MXWORK_CH.S0022) - 1));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x17));// 01?

        //L00133a:;
            /*
                                                                    rts
            */

        ////L00133c:;
        ///*
        //                                                        cmp.b   // #-$01,d2
        //                                                        bne     L001350
        //                                                        rts
        //*/
        //if ((byte)D2 == 0xff) return;

        ////L001350:;
        ///*
        //                                                        addq.b  // #1,$0022(a6)
        //                                                        ori.b   // #$01,$0017(a6)
        //*/
        //mm.Write(A6 + MXWORK_CH.S0022, (byte)(mm.ReadByte(A6 + MXWORK_CH.S0022) + 1));
        //mm.Write(A6 + MXWORK_CH.S0017, (byte)(mm.ReadByte(A6 + MXWORK_CH.S0017) | 0x01));
    }

    //
    private void L001344() {
            /*
                                                                    move.b  $0022(a6),d2
                                                                    bmi     L00135c
                                                                    cmp.b   // #$0f,d2
                                                                    beq     L00135a
            */
        D2 = mm.readByte(A6 + MXWORK_CH.S0022);
        if ((byte) D2 >= 0) { // break L00135c;
            if (D2 != 0x0f) { // break L00135a;

                //L001350:;
            /*
                                                                    addq.b  // #1,$0022(a6)
                                                                    ori.b   // #$01,$0017(a6)
            */
                mm.write(A6 + MXWORK_CH.S0022, (byte) (mm.readByte(A6 + MXWORK_CH.S0022) + 1));
                mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
            }
// L00135a:
            /*
                                                                    rts
            */
            return;
        }
// L00135c:
            /*
                                                                    cmp.b   // #$80,d2
                                                                    bne     L001330
                                                                    rts
            */
        if ((D2 & 0xff) != 0x80) {
            L001330();
        }
    }

    //
    private void L001364() {
            /*
                                                                    move.b  (a4)+,$001e(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S001e, mm.readByte(A4++));
    }

    //
    private void L00136a() {
            /*
                                                                    ori.b   // #$04,$0016(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x04));
    }

    //
    private void L001372() {
        byte t0;
            /*
                                                                    move.b  (a4)+,(a4)+
                                                                    rts
            */
        t0 = mm.readByte(A4++);
        mm.write(A4++, t0);
    }

    //
    private void L001376() {
            /*
                                                                    moveq.l // #$ff,d0
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    subq.b  // #1,-$01(a4,d0.l)
                                                                    beq     L001398
                                                                    tst.b   (L001e0b)
                                                                    beq     L001396
                                                                    cmpi.b  // #$f1,(a4)
                                                                    bne     L001396
                                                                    tst.b   $0001(a4)
                                                                    beq     L0013e6
            */
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        D0 = (D0 ^ 0xffff) + 1;
        mm.write(A4 - D0 - 1, (byte) (mm.readByte(A4 - D0 - 1) - 1));
        if (mm.readByte(A4 - D0 - 1) != 0) { // break L001398;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e0b) != 0) { // break L001396;
                if ((mm.readByte(A4) & 0xff) == 0xf1) { // break L001396;
                    if (mm.readByte(A4 + 1) == 0) {
                        L0013e6();
                        return;
                    }
                }
            }
// L001396:
            // checker
            if ((A4 - D0) < (G + MXWORK_GLOBAL.L001e34)) {
                mm.write(G + MXWORK_GLOBAL.FATALERROR, 0x001396);
                mm.write(G + MXWORK_GLOBAL.FATALERRORADR, A4);
                return;
            }
            // checker end
            /*
                                                                    adda.w  d0,a4
            */
            A4 -= D0;
        }
// L001398:
            /*
                                                                    rts
            */
    }

    //
    private void L00139a() {
            /*
                                                                    moveq.l // #$00,d0
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    lea.l   $00(a4,d0.l),a0
                                                                    moveq.l // #$ff,d0
                                                                    move.b  (a0)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a0)+,d0
                                                                    cmpi.b  // #$01,-$01(a0,d0.l)
                                                                    bne     L0013b8
                                                                    movea.l a0,a4
            */
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        A0 = A4 + D0;
        D0 = 0xffffffff;
        D0 = Depend.getbword(mm, A0);
        A0 += 2;
        D0 = (D0 ^ 0xffff) + 1;
        if (mm.readByte(A0 - D0 - 1) == 0x01) { // break L0013b8;
            A4 = A0;
        }
// L0013b8:
            /*
                                                                    rts
            */
    }

    //
    private void L0013ba()                                   // @@ D
    {
            /*
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    move.w  d0,$0010(a6)
                                                                    rts
            */
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        mm.write(A6 + MXWORK_CH.S0010, (short) D0);
    }

    //
    private void L0013c6() {
            /*
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    ext.l   d0
                                                                    asl.l   // #8,d0
                                                                    move.l  d0,$0008(a6)
                                                                    ori.b   // #$80,$0016(a6)
                                                                    rts
            */
        D0 = 0;
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        D0 = (short) D0;
        D0 <<= 8;
        mm.write(A6 + MXWORK_CH.S0008, D0);
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x80));
    }

    //
    private void L0013dc() {
            /*
                                                                    moveq.l // #$ff,d0
                                                                    move.b  (a4)+,-(sp)
                                                                    beq     L001440
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
            */
        if (mm.readByte(A4++) == 0) {
            L001440();
            return;
        }
        A4--;
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        D0 = (D0 ^ 0xffff) + 1;
            /*
                                                                    fall down
            */
        L0013e6();
    }

    //
    private void L0013e6() {
        // checker
        if ((A4 - D0) < (G + MXWORK_GLOBAL.L001e34)) {
            mm.write(G + MXWORK_GLOBAL.FATALERROR, 0x0013e6);
            mm.write(G + MXWORK_GLOBAL.FATALERRORADR, A4);
            return;
        }
        // checker end

            /*
                                                                    adda.l  d0,a4
                                                                    move.w  L001e1a(pc),d0
                                                                    bclr.l  d7,d0
                                                                    move.w  d0,(L001e1a)
                                                                    and.w   L001e06(pc),d0
                                                                    bne     L00143e
                                                                    tst.b   (L001e18)
                                                                    bne     L001416
                                                                    move.w  // #$01ff,(L001e1a)
                                                                    tst.b   (L001df4)
                                                                    beq     L001410
                                                                    ori.w   // #$fe00,(L001e1a)
            */
        A4 -= D0;
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1a);
        D0 &= ~(1 << D7);
        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) D0);
        D0 &= mm.readShort(G + MXWORK_GLOBAL.L001e06);
        if (D0 == 0) { // break L00143e;
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) == 0) { // break L001416;
                mm.write(G + MXWORK_GLOBAL.L001e1a, (short) 0x01ff);
                if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L001410;
                    mm.write(G + MXWORK_GLOBAL.L001e1a, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e1a) | 0xfe00));
                }
// L001410:
            /*
                                                                    addq.w  // #1,(L002246)
                                                                    bra     L00143e
            */
                mm.write(G + MXWORK_GLOBAL.L002246, (short) (mm.readShort(G + MXWORK_GLOBAL.L002246) + 1));
//                break L00143e;
            } else {
// L001416:
            /*
                                                                    tst.b   (L001e17)
                                                                    bne     L00143e
                                                                    move.w  // #$01ff,(L001e1a)
                                                                    tst.b   (L001df4)
                                                                    beq     L00142e
                                                                    ori.w   // #$fe00,(L001e1a)
            */
                if (mm.readByte(G + MXWORK_GLOBAL.L001e17) == 0) { // break L00143e;
                    mm.write(G + MXWORK_GLOBAL.L001e1a, (short) 0x01ff);
                    if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L00142e;
                        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e1a) | 0xfe00));
                    }
// L00142e:
            /*
                                                                    subq.w  // #1,(L001e22)
                                                                    bne     L00143e
                                                                    move.w  // #$0011,(L001e1e)
                                                                    st.b    (L001e17)
            */
                    mm.write(G + MXWORK_GLOBAL.L001e22, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e22) - 1));
                    if (mm.readShort(G + MXWORK_GLOBAL.L001e22) == 0) { // break L00143e;
                        mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) (0x0011));
                        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
                    }
                }
            }
        }
// L00143e:
            /*
                                                                    rts
            */
    }

    //
    private void L001440() {
            /*
                                                                    addq.w  // #2,sp
            */
            /*
                                                                    ; fall down
            */
        L001442();
    }

    //
    private void L001442() {
        //byte[] L0019b2 = new byte[] {
        //0x7f,0xf1,0x00,
        //};
            /*
                                                                    lea.l   L0019b2(pc),a4
                                                                    move.w  L001e1a(pc),d0
                                                                    bclr.l  d7,d0
                                                                    move.w  d0,(L001e1a)
                                                                    move.w  L001e06(pc),d0
                                                                    bclr.l  d7,d0
                                                                    move.w  d0,(L001e06)
                                                                    bne     L001490
                                                                    move.b  // #$01,(L001e13)
                                                                    tst.b   (L001df4)
                                                                    beq     L001472
                                                                    move.w  // #$01ff,d0
                                                                    trap    // #2
                                                                    clr.b   (L001df4)
            */
        A4 = L0019b2;
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1a);
        D0 &= ~(1 << D7);
        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) D0);
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e06);
        D0 &= ~(1 << D7);
        mm.write(G + MXWORK_GLOBAL.L001e06, (short) D0);
        if (D0 == 0) { // break L001490;
            mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
            if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) { // break L001472;
                D0 = 0x01ff;
                PCM8_SUB();
                mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
            }
// L001472:
            /*
                                                                    tst.b   (L001e18)
                                                                    bne     L001480
                                                                    move.w  // #$ffff,(L002246)
                                                                    bra     L001490
            */
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) == 0) { // break L001480;
                mm.write(G + MXWORK_GLOBAL.L002246, (short) 0xffff);
//                break L001490;
            } else {
// L001480:
            /*
                                                                    move.w  // #$ffff,(L001e1e)
                                                                    st.b    (L001e17)
                                                                    move.w  // #$0037,(L001e14)       ;'7'
            */
                mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) 0xffff);
                mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
                mm.write(G + MXWORK_GLOBAL.L001e14, (byte) 0x00);
                mm.write(G + MXWORK_GLOBAL.L001e15, (byte) 0x37);
            }
        }
// L001490:
            /*
                                                                    rts
            */
    }

    //
    private void L001492() {
            /*
                    move.b  (a4)+,$001f(a6)
                    rts
            */
        mm.write(A6 + MXWORK_CH.S001f, mm.readByte(A4++));
    }

    //
    private void L001498() {
            /*
                                                                    moveq.l // #$00,d0
                                                                    move.b  (a4)+,d0
                                                                    lea.l   L001df6(pc),a0
                                                                    st.b    $00(a0,d0.w)
                                                                    cmp.w   // #$0009,d0
                                                                    bcc     L0014ae
                                                                    st.b    $27(a5,d0.w)	; L002233(d7.w)
            */
        D0 = mm.readByte(A4++);
        A0 = mm.readByte(G + MXWORK_GLOBAL.L001df6 + 0);
        mm.write(A0 + D0, (byte) Depend.SET);
        if (D0 < 0x0009) { // break L0014ae;
            mm.write(G + MXWORK_GLOBAL.L002233 + D7, (byte) Depend.SET);
        }
// L0014ae:
            /*
                                                                    rts
            */
    }


    //

    private void L0014b0() {
            /*
                                                                    lea.l   L001df6(pc),a0
                                                                    tst.b   $00(a0,d7.w)
                                                                    beq     L0014d0
                                                                    clr.b   $00(a0,d7.w)
                                                                    cmp.w   // #$0009,d7
                                                                    bcc     L0014c8
                                                                    clr.b   $27(a5,d7.w)	; L002233(d7.w)
            */
        A0 = G + MXWORK_GLOBAL.L001df6 + 0;
        if (mm.readByte(A0 + D7) != 0) { // break L0014d0;
            mm.write(A0 + D7, (byte) Depend.CLR);
            if (D7 < 0x0009) { // break L0014c8;
                mm.write(G + MXWORK_GLOBAL.L002233 + D7, (byte) Depend.CLR);
            }
// L0014c8:
            /*
                                                                    andi.b  // #$f7,$0017(a6)
                                                                    rts
            */
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & 0xf7));
            return;
        }
// L0014d0:
            /*
                                                                    ori.b   // #$08,$0017(a6)
                                                                    move.l  a4,(a6)
                                                                    addq.w  // #4,sp
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x08));
        mm.write(A6 + MXWORK_CH.S0000, A4);
        DisposeStack_L00122e = Depend.TRUE;
    }

    //
    private void L0014dc() {
            /*
                                                                    move.b  (a4)+,d2
                                                                    tst.b   $0018(a6)
                                                                    bmi     L0014ee
                                                                    move.b  d2,(L002232)
                                                                    moveq.l // #$0f,d1
                                                                    bra     L_WRITEOPM
            */
        D2 = mm.readByte(A4++);
        if (mm.readByte(A6 + MXWORK_CH.S0018) >= 0x00) { // break L0014ee;
            mm.write(G + MXWORK_GLOBAL.L002232, (byte) D2);
            D1 = 0x0f;
            L_WRITEOPM();
//            return;
        } else {
// L0014ee:
            /*
                                                                    lsl.b   // #2,d2
                                                                    andi.b  // #$03,$001c(a6)
                                                                    or.b    d2,$001c(a6)
                                                                    rts
            */
            D2 <<= 2;
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) & 0x03));
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) | D2));
        }
    }

    //
    private void L0014fc() {
        int d1;
            /*
            L001588:;
                                                                    .dc.w   L0010be-L001588
                                                                    .dc.w   L0010d4-L001588
                                                                    .dc.w   L0010ea-L001588
                                                                    .dc.w   L001100-L001588
            */
            /*
                                                                    ori.b   // #$20,$0016(a6)
                                                                    moveq.l // #$00,d1
                                                                    move.b  (a4)+,d1
                                                                    bmi     L001576
                                                                    move.w  d1,-(sp)
                                                                    andi.b  // #$03,d1
                                                                    add.w   d1,d1
                                                                    move.w  L001588(pc,d1.w),d0
                                                                    lea.l   L001588(pc,d0.w),a0
                                                                    move.l  a0,$0026(a6)
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d2
                                                                    move.b  (a4)+,d2
                                                                    move.w  d2,$003c(a6)
                                                                    cmp.b   // #$02,d1
                                                                    beq     L001536
                                                                    lsr.w   // #1,d2
                                                                    cmpi.b  // #$06,d1
                                                                    bne     L001536
                                                                    moveq.l // #$01,d2
            */
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x20));
        D1 = mm.readByte(A4++);
        if ((byte) D1 >= 0) { // break L001576;
            d1 = D1;
            D1 &= 0x03;
            D1 += D1;
            A0 = ((D1 / 2) + 1);
            mm.write(A6 + MXWORK_CH.S0026, A0);
            D2 = Depend.getbword(mm, A4);
            A4 += 2;
            mm.write(A6 + MXWORK_CH.S003c, (short) D2);
            if (D1 != 0x02) { // break L001536;
                D2 >>= 1;
                if (D1 == 0x06) { // break L001536;
                    D2 = 0x01;
                }
            }
// L001536:
            /*
                                                                    move.w  d2,$003a(a6)
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    ext.l   d0
                                                                    asl.l   // #8,d0
                                                                    move.w  (sp)+,d1
                                                                    cmpi.b  // #$04,d1
                                                                    bcs     L001552
                                                                    asl.l   // #8,d0
                                                                    andi.b  // #$03,d1
            */
            mm.write(A6 + MXWORK_CH.S003a, (short) D2);
            D0 = Depend.getbword(mm, A4);
            A4 += 2;
            D0 = (short) D0;
            D0 = D0 << 8;
            D1 = d1;
            if (D1 >= 0x04) { // break L001552;
                D0 = D0 << 8;
                D1 &= 0x03;
            }
// L001552:
            /*
                                                                    move.l  d0,$002e(a6)
                                                                    cmp.b   // #$02,d1
                                                                    beq     L00155e
                                                                    moveq.l // #$00,d0
            */
            mm.write(A6 + MXWORK_CH.S002e, D0);
            if (D1 != 0x02) { // break L00155e;
                D0 = 0;
            }
// L00155e:
            /*
                                                                    move.l  d0,$002a(a6)
            */
            mm.write(A6 + MXWORK_CH.S002a, D0);

            L001562:
            ;
            /*
                                                                    move.w  $003a(a6),$003e(a6)
                                                                    move.l  $002e(a6),$0032(a6)
                                                                    move.l  $002a(a6),$0036(a6)
                                                                    rts
            */
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003a));
            mm.write(A6 + MXWORK_CH.S0032, mm.readInt(A6 + MXWORK_CH.S002e));
            mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S002a));
            return;
        }
// L001576:
            /*
                                                                    and.b   // #$01,d1
                                                                    bne     L001562
                                                                    andi.b  // #$df,$0016(a6)
                                                                    clr.l   $0036(a6)
                                                                    rts
            */
        D1 &= 0x01;
        if (D1 != 0) break L001562;
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xdf));
        mm.write(A6 + MXWORK_CH.S0036, Depend.CLR);
    }

    //
    private void L001590() {
            /*
            L0015f6:;
                    .dc.w   L001120-L0015f6
                    .dc.w   L001138-L0015f6
                    .dc.w   L00114e-L0015f6
                    .dc.w   L001164-L0015f6
            */
            /*
                                                                    ori.b   // #$40,$0016(a6)
                                                                    moveq.l // #$00,d2
                                                                    move.b  (a4)+,d2
                                                                    bmi     L0015e4
                                                                    add.w   d2,d2
                                                                    move.w  L0015f6(pc,d2.w),d0
                                                                    lea.l   L0015f6(pc,d0.w),a0
                                                                    move.l  a0,$0040(a6)
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d1
                                                                    move.b  (a4)+,d1
                                                                    move.w  d1,$004c(a6)
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    move.w  d0,$0044(a6)
                                                                    btst.l  // #$01,d2
                                                                    bne     L0015c6
                                                                    muls.w  d1,d0
            */
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x40));
        D2 = mm.readByte(A4++);
        if ((byte) D2 < 0) {
            L0015e4();
            return;
        }
        D2 += D2;
        A0 = ((D2 / 2) + 1);
        mm.write(A6 + MXWORK_CH.S0040, A0);
        D1 = Depend.getbword(mm, A4);
        A4 += 2;
        mm.write(A6 + MXWORK_CH.S004c, (short) D1);
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        mm.write(A6 + MXWORK_CH.S0044, (short) D0);
        if ((D2 & (1 << 1)) == 0) { // break L0015c6;
            D0 = D0 * (short) D1;
        }
// L0015c6:
            /*
                                                                    neg.w   d0
                                                                    bpl     L0015cc
                                                                    moveq.l // #$00,d0
            */
        D0 = -((short) D0);
        if ((short) D0 < 0) { // break L0015cc;
            D0 = 0;
        }
// L0015cc:
            /*
                                                                    move.w  d0,$0046(a6)
            */
        mm.write(A6 + MXWORK_CH.S0046, (short) D0);

            /*
                                                                    ; fall down
            */
        L0015d0();
    }

    //
    private void L0015d0() {
            /*
                    move.w  $004c(a6),$004e(a6)
                    move.w  $0044(a6),$0048(a6)
                    move.w  $0046(a6),$004a(a6)
                    rts
            */
        mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
        mm.write(A6 + MXWORK_CH.S0048, mm.readShort(A6 + MXWORK_CH.S0044));
        mm.write(A6 + MXWORK_CH.S004a, mm.readShort(A6 + MXWORK_CH.S0046));
    }

    //
    private void L0015e4() {
           /*
                                                                    and.b   // #$01,d2
                                                                    bne     L0015d0
                                                                    andi.b  // #$bf,$0016(a6)
                                                                    clr.w   $004a(a6)
                                                                    rts
            */
        D2 &= 0x01;
        if (D2 != 0) {
            L0015d0();
            return;
        }
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xbf));
        mm.write(A6 + MXWORK_CH.S004a, (short) Depend.CLR);
    }

    //
    private void L0015fe() {
        try {
            byte c0;
            /*
                                                                    move.b  (a4)+,d2
                                                                    bmi     L00164a
                                                                    andi.b  // #$fd,$0016(a6)
                                                                    bclr.l  // #$06,d2
                                                                    beq     L001614
                                                                    ori.b   // #$02,$0016(a6)
            */
            D2 = mm.readByte(A4++);
            if ((byte) D2 >= 0) { // break L00164a;
                mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xfd));
                c0 = (byte) (D2 & (1 << 6));
                D2 &= 0xffffffbf;// ~(1 << 6);
                if (c0 != 0) { // break L001614;
                    mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x02));
                }
// L001614:
            /*
                                                                    move.b  $09da.w,d0
                                                                    and.b   // #$c0,d0
                                                                    or.b    d0,d2
                                                                    moveq.l // #$1b,d1
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$18,d1
                                                                    move.b  (a4)+,d2
                                                                    bsr     L_WRITEOPM
                                                                    moveq.l // #$19,d1
                                                                    move.b  (a4)+,d2
                                                                    bsr     L_WRITEOPM
                                                                    move.b  (a4)+,d2
                                                                    bsr     L_WRITEOPM
                                                                    move.b  (a4)+,d2
                                                                    move.b  d2,$0021(a6)
            */
                D0 = opmReg1B;
                D0 &= 0xc0;
                D2 |= D0;
                D1 = 0x1b;
                L_WRITEOPM();
                D1 = 0x18;
                D2 = mm.readByte(A4++);
                L_WRITEOPM();
                D1 = 0x19;
                D2 = mm.readByte(A4++);
                L_WRITEOPM();
                D2 = mm.readByte(A4++);
                L_WRITEOPM();
                D2 = mm.readByte(A4++);
                mm.write(A6 + MXWORK_CH.S0021, (byte) D2);
//                return;
            } else {
L00164a:
            /*
                                                                    and.b   // #$01,d2
                                                                    beq     L001640
                                                                    move.b  $0021(a6),d2
                                                                    bra     L001640
            */
                D2 &= 0x01;
                if (D2 == 0) return; // break L001640;
                D2 = mm.readByte(A6 + MXWORK_CH.S0021);
//                break L001640;
            }
        } finally {
// L001640:
            /*
                                                                    moveq.l // #$38,d1
                                                                    add.b   $0018(a6),d1
                                                                    bra     L_WRITEOPM
            */
            D1 = 0x38;
            D1 += mm.readByte(A6 + MXWORK_CH.S0018);
            L_WRITEOPM();
        }
    }

    //
    private void L001656() {
            /*
                                                                    move.b  (a4)+,$0024(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S0024, mm.readByte(A4++));
    }

    //
    private void L00165c() {
            /*
                                                                    movea.l $0088.w,a0
                                                                    cmpa.l  // #$00f00000,a0
                                                                    bcc     L001692
                                                                    cmpi.l  // #$50434d34,-$0008(a0)   ;'PCM4'
                                                                    beq     L00167c
                                                                    cmpi.l  // #$50434d38,-$0008(a0)   ;'PCM8'
                                                                    bne     L001692
            */
        if (PCM8 != 0) { // break L001692;
            //L00167c:;
            /*
                                                                    st.b    (L001df4)
                                                                    move.w  // #$01fe,d0
                                                                    trap    // #2
                                                                    ori.w   // #$fe00,(L001e1a)
                                                                    ori.w   // #$fe00,(L001e06)
            */
            mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.SET);
            D0 = 0x01fe;
            PCM8_SUB();
            mm.write(G + MXWORK_GLOBAL.L001e1a, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e1a) | 0xfe00));
            mm.write(G + MXWORK_GLOBAL.L001e06, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e06) | 0xfe00));
        }
// L001692:
            /*
                                                                    rts
            */
    }

    //
    private void L001694() {
            /*
            L0016aa:;
                    .dc.w   L001442-L0016aa
                    .dc.w   L0016b8-L0016aa
                    .dc.w   L0016c6-L0016aa
                    .dc.w   L0016fa-L0016aa
                    .dc.w   L00170e-L0016aa
                    .dc.w   L00178a-L0016aa
                    .dc.w   L0017a0-L0016aa
            */
        //L001694:;
            /*
                    moveq.l // #$00,d0
                    move.b  (a4)+,d0
                    cmp.w   // #$0007,d0
                    bcc     L001442
                    add.w   d0,d0
                    move.w  L0016aa(pc,d0.w),d0
                    jmp     L0016aa(pc,d0.w)
            */
        D0 = mm.readByte(A4++);
        if (D0 > 7) {
            L001442();
            return;
        }
        L0016aa[D0].run();
    }

    //
    private void L0016b8() {
            /*
                                                                    moveq.l // #$00,d0
                                                                    move.b  (a4)+,d0
                                                                    move.w  d0,(L001e1e)
                                                                    st.b    (L001e17)
                                                                    rts
            */
        D0 = mm.readByte(A4++);
        mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) D0);
        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
    }

    //
    private void L0016c6() {
            /*
                                                                    movea.l $0088.w,a0
                                                                    move.l  -$0008(a0),d0
                                                                    cmp.l   // #$50434d38,d0           ;'PCM8'
                                                                    beq     L0016e2
                                                                    cmp.l   // #$50434d34,d0           ;'PCM4'
                                                                    beq     L0016e2
                                                                    addq.w  // #6,a4
                                                                    rts
            */
        if (PCM8 == 0) { // break L0016e2;
            A4 += 6;
            return;
        }
// L0016e2:
            /*
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d0
                                                                    move.b  (a4)+,d0
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d1
                                                                    move.b  (a4)+,d1
                                                                    swap.w  d1
                                                                    move.b  (a4)+,-(sp)
                                                                    move.w  (sp)+,d1
                                                                    move.b  (a4)+,d1
                                                                    trap    // #2
                                                                    rts
            */
        D0 = Depend.getbword(mm, A4);
        A4 += 2;
        D1 = Depend.getblong(mm, A4);
        A4 += 4;
        PCM8_SUB();
    }

    //
    private void L0016fa() {
            /*
                                                                    tst.b   (a4)+
                                                                    beq     L001706
                                                                    ori.b   // #$10,$0016(a6)
                                                                    rts
            */
        if (mm.readByte(A4++) != 0) { // break L001706;
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x10));
//            return;
        } else {
// L001706:
            /*
                                                                    andi.b  // #$ef,$0016(a6)
                                                                    rts
            */
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xef));
        }
    }

    //
    private void L00170e() {
        int d1;
        int a6;
        int d7;
        int a6s0000;
            /*
                                                                    move.b  (a4)+,d0
                                                                    movem.l d7/a6,-(sp)
                                                                    lea.l   CHBUF_FM(pc),a6
                                                                    move.w  d0,d7
                                                                    cmp.b   // #$09,d0
                                                                    bcs     L001724
                                                                    lea.l   CHBUF_PCM-($50*9)(pc),a6
            */
        D0 = mm.readByte(A4++);
        a6 = A6;
        d7 = D7;
        D7 = D0;
        if (D0 < 0x09) {
            A6 = MXWORK_CHBUF_FM[D0];
        } else {
            A6 = MXWORK_CHBUF_PCM[D0 - 9];
        }

        //L001724:;
            /*
                                                                    mulu.w  // #$0050,d0
                                                                    adda.w  d0,a6
                                                                    move.l  (a6),-(sp)
                                                                    andi.b  // #$7b,$0016(a6)
                                                                    moveq.l // #$00,d0
                                                                    moveq.l // #$00,d1
                                                                    move.b  (a4)+,d0
                                                                    move.b  d0,d1
                                                                    bpl     L00176e
                                                                    cmp.b   // #$e0,d0
                                                                    bcc     L001240
                                                                    and.w   // #$007f,d0
                                                                    lsl.w   // #6,d0
                                                                    addq.w  // #5,d0
                                                                    add.w   $0010(a6),d0
                                                                    move.w  d0,$0012(a6)
                                                                    ori.b   // #$01,$0016(a6)
                                                                    move.b  $001f(a6),$0020(a6)
                                                                    moveq.l // #$00,d0
                                                                    move.b  (a4)+,d0
                                                                    move.b  $001e(a6),d1
                                                                    bmi     L001782
                                                                    mulu.w  d0,d1
                                                                    lsr.w   // #3,d1
            */
        a6s0000 = A6 + MXWORK_CH.S0000;
        mm.write(A6 + MXWORK_CH.S0016, (short) (mm.readByte(A6 + MXWORK_CH.S0016) & 0x7b));
        D0 = 0;
        D1 = 0;
        D0 = mm.readByte(A4++);
        D1 = D0;
        if ((byte) D0 >= 0) break L00176e;
        if (D0 < 0xe0) { // break L001240;
            D0 &= 0x007f;
            D0 <<= 6;
            D0 += 0x05;
            D0 += mm.readShort(A6 + MXWORK_CH.S0010);
            mm.write(A6 + MXWORK_CH.S0012, (short) D0);
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x01));
            mm.write(A6 + MXWORK_CH.S0020, mm.readByte(A6 + MXWORK_CH.S001f));
            D0 = mm.readByte(A4++);
            D1 = mm.readByte(A6 + MXWORK_CH.S001e);
            if ((byte) D1 >= 0) { // break L001782;
                D1 = (short) (D1 * (short) D0);
                D1 >>= 3;

// L00176e:
            /*
                                                                    addq.w  // #1,d1
                                                                    move.b  d1,$001b(a6)
                                                                    addq.w  // #1,d0
                                                                    move.b  d0,$001a(a6)
            */
                D1++;
                mm.write(A6 + MXWORK_CH.S001b, (byte) D1);
                D0++;
                mm.write(A6 + MXWORK_CH.S001a, (byte) D0);

 // L00177a:
            /*
                                                                    move.l  (sp)+,(a6)
                                                                    movem.l (sp)+,d7/a6
                                                                    rts
            */
                mm.write(A6 + MXWORK_CH.S0000, a6s0000);
                D7 = d7;
                A6 = a6;
                return;

            }
// L001782:
            /*
                                                                    add.b   d0,d1
                                                                    bcs     L00176e
                                                                    moveq.l // #$00,d1
                                                                    bra     L00176e
            */
            d1 = (byte) D1;
            d1 += (byte) D0;
            if (d1 >= 0x100) {
                D1 = (D1 & 0xffffff00) | (d1 & 0x000000ff);
                break L00176e;
            }
            D1 = 0x00;
            break L00176e;
        }
// L001240:
            /*
                                                                    ext.w   d0
                                                                    not.w   d0
                                                                    add.w   d0,d0
                                                                    move.w  L001252(pc,d0.w),d0
                                                                    pea.l   L00177a(pc)
                                                                    jmp     L001252(pc,d0.w)
            */
        D0 ^= 0xff;
        DisposeStack_L00122e = Depend.FALSE;
        L001252[D0].run();
        if (DisposeStack_L00122e == Depend.TRUE) return;
        break L00177a;
    }

    //
    private void L001216() {
            /*
                                                                    addq.w  // #1,d1
                                                                    move.b  d1,$001b(a6)
                                                                    addq.w  // #1,d0
                                                                    move.b  d0,$001a(a6)
                                                                    move.l  a4,(a6)
            */
        D1++;
        mm.write(A6 + MXWORK_CH.S001b, (byte) D1);
        D0++;
        mm.write(A6 + MXWORK_CH.S001a, (byte) D0);
        mm.write(A6 + MXWORK_CH.S0000, A4);
            /*
                                                                    rts
            */
    }

    private void L00178a() {
            /*
                                                                    move.b  (a4)+,d0
                                                                    move.b  d0,d1
                                                                    bsr     L001216
                                                                    andi.b  // #$fe,$0016(a6)
                                                                    bsr     L000e7e
                                                                    addq.w  // #4,sp
                                                                    rts
            */
        D0 = mm.readByte(A4++);
        D1 = D0;
        L001216();
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xfe));
        L000e7e();
        DisposeStack_L00122e = Depend.TRUE;
    }

    //
    private void L0017a0() {
            /*
                                                                    tst.b   (a4)+
                                                                    beq     L0017ac
                                                                    ori.b   // #$80,$0017(a6)
                                                                    rts
            */
        if (mm.readByte(A4++) != 0) { // break L0017ac;
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x80));
        }
// L0017ac:
            /*
                                                                    andi.b  // #$7f,$0017(a6)
                                                                    rts
            */
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & 0x7f));
    }

    //
    private void L_WRITEOPM() {
            /*
                                                                    ori.w   // #$0300,sr
                                                                    tst.b   $00e90003
                                                                    bmi     L_WRITEOPM
                                                                    move.b  d1,$00e90001
                                                                    and.w   // #$00ff,d1
                                                                    lea.l   OPMBUF(pc),a2
                                                                    move.b  d2,$00(a2,d1.w)
                                                                    move.b  d2,$00e90003
                                                                    andi.w  // #$faff,sr
                                                                    cmp.b   // #$1b,d1
                                                                    beq     L0017e4
                                                                    rts
            */
        OPM_SUB();
        D1 &= 0xff;
        mm.write(OPMBUF + D1, (byte) D2);
        if (D1 != 0x1b) { // break L0017e4;
            return;
        }
// L0017e4:
            /*
                                                                    move.b  d2,$09da.w
                                                                    rts
            */
        opmReg1B = (byte) D2;
    }

    //
    private int initialize(
            int mdxbuf,
            int pdxbuf,
            int memInd
    ) {
        mm.write(G + MXWORK_GLOBAL.L002220, mdxbuf != 0 ? mdxbuf : 0x10000);
        mm.write(G + MXWORK_GLOBAL.L002224, pdxbuf != 0 ? pdxbuf : 0x100000);
        mm.write(G + MXWORK_GLOBAL.L001ba8, 0x600);

        //mdx
        mm.write(G + MXWORK_GLOBAL.L001e34, memInd);
        memInd += mm.readInt(G + MXWORK_GLOBAL.L002220);
        mm.realloc(memInd);
        if (mm.readInt(G + MXWORK_GLOBAL.L001e34) == 0) {
            return 0xffffffff;
        }
        //Arrays.fill(G.L001e34, 0, (int)G.L002220);
        //pdx
        mm.write(G + MXWORK_GLOBAL.L001e38, memInd);
        memInd += mm.readInt(G + MXWORK_GLOBAL.L002224);
        mm.realloc(memInd);
        if (mm.readInt(G + MXWORK_GLOBAL.L001e38) == 0) {
            mm.write(G + MXWORK_GLOBAL.L001e34, 0);
            return 0xffffffff;
        }
        //Arrays.fill(G.L001e38, 0, (int)G.L002224);
        mm.write(G + MXWORK_GLOBAL.L001bac, memInd);
        memInd += mm.readInt(G + MXWORK_GLOBAL.L001ba8);
        mm.realloc(memInd);
        if (mm.readInt(G + MXWORK_GLOBAL.L001bac) == 0) {
            mm.write(G + MXWORK_GLOBAL.L001e34, 0);
            mm.write(G + MXWORK_GLOBAL.L001e38, 0);
            return 0xffffffff;
        }
        //Arrays.fill(G.L001bac, 0, (int)G.L001ba8);

        return 0;
    }
}


