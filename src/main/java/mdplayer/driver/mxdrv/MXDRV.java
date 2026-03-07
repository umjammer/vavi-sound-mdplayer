// X68k MXDRV Music driver version 2.06+17 Rel.X5-s
// (c)1988-92 milk.,K.MAEKAWA, Missy.M, Yatsube
// 
// Converted for Win32 [MXDRVg] V1.50a
// Copyright (C) 2000 GORRY.
// Converted for MDPlayer Vx.xx
// Copyright (C) 2018 Kumatan.

package mdplayer.driver.mxdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.x68sound.X68Sound;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


// 
// Filename mxdrv17.x
// Time Stamp Sun Mar 15 11:52:06 1998
// 
// Base address 000000
// Exec address 0017ea
// Text size    001ba6 bytes
// data size    000000 byte(s)
// Bss  size    0006a2 byte(s)
// 438 Labels
// Code Generate date Wed May 06 12:59:13 1998
// Command Line D:\FTOOL\dis.x -C2 --overwrite -7 -m 68040 -M -s8192 -e -g mxdrv17.x mxdrv17.dis
//         DIS version 2.75
// 
public class MXDRV extends BaseDriver {

    private static final Logger logger = getLogger(MXDRV.class.getName());

    public static class Pcm8St {
        public int tablePtr = 0;
        public int mode = 0;
        public int length = 0;
        public boolean Keyon = false;
    }

    final Pcm8St[] pcm8St = {
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St(),
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St()
    };

    interface MXWORK_CH {
        int S0000 = 0; // Ptr
        int S0004_b = 4; // PCM bank
        int S0004 = 5; // voice ptr
        int S0008 = 9; // bend delta
        int S000c = 13; // bend offset
        int S0010 = 17; // D
        int S0012 = 19; // note+D (>> 6 Note)
        int S0014 = 21; // note+D+bend+Pitch LFO offset
        int S0016 = 23; // Flags b3=keyon/off
        int S0017 = 24; // Flags b0:volChg
        int S0018 = 25; // ch
        int S0019 = 26; // carrier slot
        int S001a = 27; // len
        int S001b = 28; // gate
        int S001c = 29; // p + fb + con
        int S001d = 30; // keyon slot
        int S001e = 31; // Q
        int S001f = 32; // keyOn delay
        int S0020 = 33; // keyOn delay counter
        int S0021 = 34; // PMS/AMS
        int S0022 = 35; // v
        int S0023 = 36; // v last
        int S0024 = 37; // LFO delay
        int S0025 = 38; // LFO delay counter
        int S0026 = 39; // Pitch LFO Type
        int S002a = 43; // Pitch LFO offset start
        int S002e = 47; // Pitch LFO delta start
        int S0032 = 51; // Pitch LFO delta
        int S0036 = 55; // Pitch LFO offset
        int S003a = 59; // Pitch LFO length (cooked)
        int S003c = 61; // Pitch LFO length
        int S003e = 63; // Pitch LFO length counter
        int S0040 = 65; // Volume LFO Type
        int S0044 = 69; // Volume LFO delta start
        int S0046 = 71; // Volume LFO delta (cooked)
        int S0048 = 73; // Volume LFO delta
        int S004a = 75; // Volume LFO offset
        int S004c = 77; // Volume LFO length
        int S004e = 79; // Volume LFO length counter
        int Length = 81;
    }

    public interface MXWORK_GLOBAL {
        int L001ba6 = 0; // short
        int L001ba8 = 2; // int
        int L001bac = 6; // byte[]
        int L001bb4 = 10; // byte[16]
        int L001df4 = 26; // byte
        int L001df6 = 27; // byte[16]
        int L001e06 = 43; // Channel Mask (true)
        int L001e08 = 45; // byte
        int L001e09 = 46; // byte
        int L001e0a = 47; // byte
        int L001e0b = 48; // byte
        int L001e0c = 49; // @t
        int L001e0d = 50; // byte L001e0d;
        int L001e0e = 51; // Ref<Byte>
        int L001e10 = 52; // byte L001e10;
        int L001e12 = 53; // Paused
        int L001e13 = 54; // End
        int L001e14 = 55; // Fadeout Offset
        int L001e15 = 56; // byte
        int L001e17 = 57; // Fadeout Enable
        int L001e18 = 58; // byte
        int L001e19 = 59; // byte
        int L001e1a = 60; // Channel Enable
        int L001e1c = 62; // Channel Mask
        int L001e1e = 64; // Fadeout Speed
        int L001e22 = 72; // short
        int L001e24 = 74; // volatile byte[]
        int L001e28 = 78; // volatile byte[]
        int L001e2c = 82; // volatile byte[]
        int L001e30 = 86; // volatile byte[]
        int L001e34 = 90; // volatile byte[]
        int L001e38 = 94; // volatile byte[]
        int L00220c = 98; // int
        int L002218 = 102; // volatile byte[]
        int L00221c = 106; // volatile byte[]
        int L002220 = 110; // L_MDXSIZE
        int L002224 = 114; // L_PDXSIZE
        int L002228 = 118; // voice data
        int L00222c = 122; // volatile byte[]
        int L002230 = 126; // byte
        int L002231 = 127; // byte
        int L002232 = 128; // byte
        int L002233 = 129; // byte[9]
        int L00223c = 138; // byte[12]
        int L002245 = 150; // byte L002245;
        int L002246 = 151; // loop count
        int FATALERROR = 153; //
        int FATALERRORADR = 157; //
        int PLAYTIME = 161; // Duration
        int MUSICTIMER = 165; // Duration timer constant
        int STOPMUSICTIMER = 166; // Stop playing time timer
        int MEASURETIMELIMIT = 167; // Time measurement stop time
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
        FM, // FM8ch+PCM1ch
        PCM, // PCM7ch
        GLOBAL,
        KEY,
        OPM,
        PCM8,
        CREDIT,
        CALLBACK_OPMINT
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

        reg.d0 = a;
        reg.d1 = 0x00;
        MXDRV_(reg);
    }

    private void MXDRV_Call_2(int a, int b) {
        X68Reg reg = new X68Reg();

        reg.d0 = a;
        reg.d1 = b;
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
        MXDRV_Call_2(0x0c, a);
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Gd3 gd3 = new Gd3();

        List<Byte> lst = new ArrayList<>();
        int i = 0;
        while (i < buf.length && (buf[i] != 0xd && buf[i] != 0xa)) {
            lst.add(buf[i]);
            i++;
        }
        String n = new String(ByteUtil.toByteArray(lst), charset);
        gd3.trackName = n;
        gd3.trackNameJ = n;
        byte[][] mdx = new byte[1][];
        int[] mdxSize = new int[1];
        String[] pdxFileName = new String[1];
        makeMdxBuf(buf, mdx, mdxSize, pdxFileName);

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

        gd3 = getGD3Info(vgmBuf);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

        for (int chipId = 0; chipId < 2; chipId++) {
            ym2151Hosei[chipId] = Common.getYM2151Hosei(4000000, 3579545);
            if (model == EnmModel.RealModel) {
                ym2151Hosei[chipId] = 0;
                int clock = plugin.audio.chipRegister.chip(Ym2151Chip.class).getClock(chipId);
                if (clock != -1) {
                    ym2151Hosei[chipId] = Common.getYM2151Hosei(4000000, clock);
                }
            }
        }

        return true;
    }

    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, X68kYm2151Inst mdxPCM, Pcm8PPInst pcm8pp) {
        init(vgmBuf, plugin, model, useChip, latency, waitTime);
        this.pcm8pp = pcm8pp;
        this.mdxPCM = mdxPCM;

        byte[][] mdx = new byte[1][];
        int[] mdxSize = new int[1];
        int mdxPtr;
        byte[][] pdx = new byte[1][];
        int[] pdxSize = new int[1];
        int pdxPtr;
        String[] pdxFileName = new String[1];
        makeMdxBuf(vgmBuf, mdx, mdxSize, pdxFileName);
        makePdxBuf(pdxFileName[0], pdx, pdxSize);
        if ((pdxFileName[0] != null && !pdxFileName[0].isEmpty()) && pdx[0] == null) {
logger.log(Level.WARNING, "pdxFileName: %s, pdx: %s".formatted(pdxFileName[0], pdx[0]));
            errMsg = "Failed to load PCM file [%s].".formatted(pdxFileName[0]);
            return false;
        }

        int ret;
        if (model == EnmModel.VirtualModel) {
            ret = MXDRV_Start(setting.getOutputDevice().getSampleRate(), 0, 0, 0, mdxSize[0], pdxSize[0], 0, -1, 1);
        } else {
            ret = MXDRV_Start(setting.getOutputDevice().getSampleRate(), 0, 0, 0, mdxSize[0], pdxSize[0], 0, -1, -1);
        }
        int memind = mm.mm.length;
        mdxPtr = memind;
        memind += mdxSize[0];
        pdxPtr = memind;
        memind += pdxSize[0];
        mm.realloc(memind);
        for (int i = 0; i < mdxSize[0]; i++) mm.write(mdxPtr + i, mdx[0][i]);
        for (int i = 0; i < pdxSize[0]; i++) mm.write(pdxPtr + i, pdx[0][i]);

        if (mdxPCM != null) mdxPCM.chips[0].mountMemory(mm.mm);
        if (pcm8pp != null) pcm8pp.writePcm(0, mm.mm, 0, mm.mm.length);

        int playtime = MXDRV_MeasurePlayTime(mdx[0], mdxSize[0], mdxPtr, pdx[0], pdxSize[0], pdxPtr, 1, Depend.TRUE);
//logger.log(Level.TRACE, "(%d:%02d) %d".formatted(playtime / 1000 / 60, playtime / 1000 % 60, ""));
        totalCounter = (long) playtime * setting.getOutputDevice().getSampleRate() / 1000;
        terminatePlay = false;
        MXDRV_Play(mdx[0], mdxSize[0], mdxPtr, pdx[0], pdxSize[0], pdxPtr);

//logger.log(Level.TRACE, "********************");

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }

    short[] dummyBuf = new short[2];

    @Override
    public void processOneFrame() {
        render(dummyBuf, 0, 2);
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public int render_(short[] buffer, int offset, int sampleCount) {
        if (mdxPCM == null) {
            return 0;
        }
        int ret = mdxPCM.chips[0].getPcm(buffer, offset, sampleCount, this::oneFrameProc2);

        //logger.log(Level.TRACE, "0:%08x".formatted(mm.readint(MXWORK_CHBUF_FM[8] + MXWORK_CH.S0012)));
        //logger.log(Level.TRACE, "1:%04x".formatted(mm.readshort(MXWORK_CHBUF_PCM[0] + MXWORK_CH.S0012) >> 6));
        //logger.log(Level.TRACE, "1a:%04x".formatted(mm.readshort(MXWORK_CHBUF_PCM[0] + MXWORK_CH.S0014) >> 6));
        //logger.log(Level.TRACE, "2:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[1] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "3:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[2] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "4:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[3] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "5:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[4] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "6:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[5] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "7:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[6] + MXWORK_CH.S0004)));

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
        // Skip title
        int p = 8;
        int c;
        mdxSize[0] = buf.length;
        mdx[0] = new byte[buf.length + 8];
        System.arraycopy(buf, 0, mdx[0], 8, mdxSize[0]);

        while (true) {
            c = mdx[0][p++] & 0xff;
            if (c == 0x0d) break;
            if (c == 0x0a) break;
            if (c < 0x20) {
                if (c != 0x1b) throw new IllegalArgumentException("illegal char: " + Integer.toHexString(c));
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

        // Load PDX
        byte havePdx = (byte) 0xff;
        List<Byte> lstPdxFileName;
        while (mdx[0][p2++] != 0x1a) ;
        if (mdx[0][p2] != 0) {
            havePdx = 0x00;
            lstPdxFileName = new ArrayList<>();
            while (mdx[0][p2] != 0x00) {
                lstPdxFileName.add(mdx[0][p2]);
                p2++;
            }
            pdxFileName[0] = new String(ByteUtil.toByteArray(lstPdxFileName), charset);
        }
        p2++;

        // Process MDX so that it can be passed to MXDRV
        int mdxBodyPtr = p;
        while (p2 < mdx[0].length) {
            mdx[0][p++] = mdx[0][p2++];
        }
        mdxSize[0] = p;

        mdx[0][0] = 0x00;
        mdx[0][1] = 0x00;
        mdx[0][2] = havePdx;
        mdx[0][3] = havePdx;
        mdx[0][4] = (byte) ((mdxBodyPtr & 0xffff) >>> 8);
        mdx[0][5] = (byte) (mdxBodyPtr & 0xff);
        mdx[0][6] = 0x00;
        mdx[0][7] = 0x08;
    }

    /**
     * @param pdx OUT
     * @param pdxSize OUT
     */
    private void makePdxBuf(String pdxFileName, byte[][] pdx, int[] pdxSize) {
        if (extendFile == null) {
logger.log(Level.DEBUG, "extendFiles is null");
            return;
        }

        pdx[0] = new byte[extendFile.getItem2().length + pdxFileName.length() + 8 + 1];
        System.arraycopy(pdxFileName.getBytes(StandardCharsets.US_ASCII), 0, pdx[0], 8, pdxFileName.length());
        System.arraycopy(extendFile.getItem2(), 0, pdx[0], 8 + pdxFileName.length() + 1, extendFile.getItem2().length);
        pdx[0][0] = 0x00;
        pdx[0][1] = 0x00;
        pdx[0][2] = 0x00;
        pdx[0][3] = 0x00;
        pdx[0][4] = (byte) (((8 + pdxFileName.length() + 2) & 0xffff_fffe) >>> 8);
        pdx[0][5] = (byte) ((8 + pdxFileName.length() + 2) & 0xffff_fffe);
        pdx[0][4] = (byte) (((8 + pdxFileName.length() + 1) & 0xffff) >>> 8);
        pdx[0][5] = (byte) ((8 + pdxFileName.length() + 1) & 0xff);
        pdx[0][6] = (byte) (((pdxFileName.length() + 1) & 0xff00) >>> 8);
        pdx[0][7] = (byte) ((pdxFileName.length() + 1) & 0xff);
        pdxSize[0] = pdx[0].length;
    }

    public static void getPDXFileName(byte[] buf, String[] pdx) {
        int p = 0;
        int c;
        while (true) {
            c = buf[p++] & 0xff;
            if (c == 0x0d || c == 0x0a) break;
            if (c < 0x20 && c != 0x1b) throw new IllegalArgumentException("illegal char: " + Integer.toHexString(c));
        }
        if ((p & 0x01) != 0) p++;
        if (c != 0x0d) while (buf[p++] != 0x0d) ;
        while (buf[p++] != 0x1a) ;
        if (buf[p] == 0) return;
        List<Byte> lstPdxFileName = new ArrayList<>();
        while (buf[p] != 0x00) lstPdxFileName.add(buf[p++]);
        pdx[0] = new String(ByteUtil.toByteArray(lstPdxFileName), charset);
    }

    // private double deltaCnt = 0;
    private XMemory mm = null;
    private String playingFileName = "";
    public Tuple<String, byte[]> extendFile = null;
    private int timerA = 0, timerB = 0;
    private X68kYm2151Inst mdxPCM = null;
    private Pcm8PPInst pcm8pp;
    public int pcm8type = 0;

    // Contents of OPM register $1B
    private byte opmReg1B;

    private static final String MXWORK_CREDIT = """
            X68k MXDRV Music driver version 2.06+17 Rel.X5-s (c)1988-92 milk.,K.MAEKAWA, Missy.M, Yatsube
            Converted for Win32 [MXDRVg] V2.00a Copyright (C) 2000-2002 GORRY.
            Converted for MDPlayer Vx.xx Copyright (C) 2018-2018 Kumatan.
            """;

    private final int[] MXWORK_CHBUF_FM = new int[9];
    private final int[] MXWORK_CHBUF_PCM = new int[7];

    //static MXWORK_GLOBAL MXWORK_GLOBALBUF;
    private int G = 0;

    //static MXWORK_KEY MXWORK_KEYBUF;
    private int KEY = 0;

    //static MXWORK_OPM MXWORK_OPMBUF;
    public int OPMBUF = 0;

    //static byte MXWORK_PCM8;
    //private MXWORK_PCM8 PCM8 = null;
    private static final int PCM8 = 1;

    // 

    private static final int FAKEA6S0004 = 0;

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

    private static final int[] CarrierSlot = {
            0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f,
    };

    private static final int[] KeyCode = {
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

    private static final int[] Volume = {
            0x2a, 0x28, 0x25, 0x22, 0x20, 0x1d, 0x1a, 0x18,
            0x15, 0x12, 0x10, 0x0d, 0x0a, 0x08, 0x05, 0x02,
    };

    private static final int[] PCMVolume = {
            0x0f, 0x0f, 0x0f, 0x0e, 0x0e, 0x0e, 0x0d, 0x0d,
            0x0d, 0x0c, 0x0c, 0x0b, 0x0b, 0x0b, 0x0a, 0x0a,
            0x0a, 0x09, 0x09, 0x08, 0x08, 0x08, 0x07, 0x07,
            0x07, 0x06, 0x06, 0x05, 0x05, 0x05, 0x04, 0x04,
            0x04, 0x03, 0x03, 0x02, 0x02, 0x02, 0x01, 0x01,
            0x01, 0x00, 0x00, 0xff,
    };

    private int L0019b2;
    Runnable[] L001252;
    Runnable[] jumpTable;
    Runnable[] L0016aa;
    Runnable[] L0010b4_Table;
    Runnable[] L001116Table;

    private void ini() {

        L001252 = new Runnable[] {
                this::L001292, // @@ @t
                this::L0012a6,
                this::L0012be, // @@ @
                this::L0012e6, // @@ p
                this::L00131c, // @@ v
                this::L001328,
                this::L001344,
                this::L001364,
                this::L00136a,
                this::L001372,
                this::L001376,
                this::L00139a,
                this::L0013ba, // @@ D
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

        jumpTable = new Runnable[] {
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
            int opmflag /* = 1 */,
            int adpcmflag /* = 1 */
    ) {
        int ret;

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

        mm.write(G + MXWORK_GLOBAL.MEASURETIMELIMIT, (int) (((1000L * (60 * 20 - 2))) * 4000 / 1024)); // 20min-2sec
        mm.write(L0019b2 + 0, (byte) 0x7f);
        mm.write(L0019b2 + 1, (byte) 0xf1);
        mm.write(L0019b2 + 2, (byte) 0x00);

        ret = 0; // x68Sound.Load();
        if (ret != 0) {
            switch (ret) {
            case X68Sound.SNDERR_DLL:
            case X68Sound.SNDERR_FUNC:
            default:
                return 10000 + ret;
            }
        }

        if (opmmode > 1) opmmode = 0;
        if (opmmode < 0) opmmode = 0;

        if (betw != 0) {
            if (mdxPCM != null)
                ret = mdxPCM.chips[0].start(samprate, opmmode + 1, 1, betw, pcmbuf, late, 1.0);
        } else {
            if (mdxPCM != null)
                ret = mdxPCM.chips[0].startPcm(samprate, opmflag, adpcmflag, pcmbuf);
        }
        if (ret != 0) {
            switch (ret) {
            case X68Sound.SNDERR_PCMOUT:
            case X68Sound.SNDERR_TIMER:
            case X68Sound.SNDERR_MEMORY:
                return 10100 + ret;
            }
        }

        if (mdxPCM != null) mdxPCM.soundIocs[0].init();
        ret = initialize(mdxbuf, pdxbuf, memInd);
        if (ret != 0) {
            return MXDRV_ERR.MEMORY.ordinal();
        }

        return 0;
    }

    private void MXDRV_End() {
        if (mdxPCM != null) mdxPCM.chips[0].opmInt(null);
        MXCALLBACK_OPMINT = null;
        OPMINT_FUNC = null;

        DisposeStack_L00122e = null;

        if (mdxPCM != null) mdxPCM.chips[0].free();
    }

    private int MXDRV_GetPCM(short[] buf, int len) {
        if (mdxPCM == null) return 0;
        return mdxPCM.chips[0].getPcm(buf, 0, len);
    }

    private int MXDRV_TotalVolume(int vol) {
        if (mdxPCM == null) return 0;
        return mdxPCM.chips[0].totalVolume(vol);
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
        switch (MXDRV_WORK.values()[i]) {
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
        if ((mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) & 0xffff_ffffL) >= (mm.readInt(G + MXWORK_GLOBAL.MEASURETIMELIMIT) & 0xffff_ffffL)) {
            terminatePlay = true;
        }
        if (mm.readByte(G + MXWORK_GLOBAL.L001e13) != 0) {
            terminatePlay = true;
        }
        if (mm.readShort(G + MXWORK_GLOBAL.L002246) == (short) 65535) {
            terminatePlay = true;
        } else {
            loopCount = mm.readShort(G + MXWORK_GLOBAL.L002246) & 0xffff;
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
        Runnable opmIntBack;

        if (mdxPCM != null) mdxPCM.chips[0].opmInt(null);

        measurePlayTime = true;
        terminatePlay = false;
        loopCount = 0;
        loopLimit = loop;
        fadeoutStart = false;
        reqFadeout = fadeout != 0;

        opmIntBack = MXCALLBACK_OPMINT;
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
        reg.d1 = 0xffff_ffff;
        MXDRV_(reg);

        while (!terminatePlay) OPMINTFUNC();

        MXDRV_Stop();

        MXCALLBACK_OPMINT = opmIntBack;
        measurePlayTime = false;
        if (mdxPCM != null) mdxPCM.chips[0].opmInt(this::OPMINTFUNC);

        return (int) (mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) * (long) 1024 / 4000. + (1 - Math.ulp(1.0))) + 2000;
    }

    // 
    private void MXDRV_PlayAt(
            int playat,
            int loop,
            int fadeout
    ) {
        X68Reg reg = new X68Reg();
        Runnable opmIntBack;
        short chMaskBack;
        int opmWaitBack;

        if (mdxPCM != null) mdxPCM.chips[0].opmInt(null);

        terminatePlay = false;
        loopCount = 0;
        loopLimit = loop;
        fadeoutStart = false;
        reqFadeout = fadeout != 0;

        playat = (int) (playat * (long) 4000 / 1024);

        opmIntBack = MXCALLBACK_OPMINT;
        MXCALLBACK_OPMINT = this::MXDRV_MeasurePlayTime_OPMINT;
        chMaskBack = mm.readShort(G + MXWORK_GLOBAL.L001e1c);

        reg.d0 = 0x0f;
        reg.d1 = 0xffff_ffff;
        MXDRV_(reg);

        if (mdxPCM == null) opmWaitBack = 0;
        else opmWaitBack = mdxPCM.chips[0].opmWait(-1);
        if (mdxPCM != null) mdxPCM.chips[0].opmWait(1);
        while (mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) < playat) {
            if (terminatePlay) break;
            OPMINTFUNC();
        }
        if (mdxPCM != null) mdxPCM.chips[0].opmWait(opmWaitBack);

        mm.write(G + MXWORK_GLOBAL.L001e1c, chMaskBack);
        MXCALLBACK_OPMINT = opmIntBack;
        if (mdxPCM != null) mdxPCM.chips[0].opmInt(this::OPMINTFUNC);
    }

    // 

    private void PCM8_SUB() {
        if (measurePlayTime) return;

        int ch;

        switch (D0 & 0xfff0) {
        case 0x0000:
            if (pcm8type == 0) if (mdxPCM != null) mdxPCM.chips[0].pcm8Out(D0 & 0xff, null, A1, D1, D2); // Start of specified channel sound
            else if (pcm8pp != null) pcm8pp.keyOn(0, D0 & 0xff, A1, D1 + 0x0800, D2); // Start of specified channel sound
            ch = (D0 & 0xff) % 8;
            pcm8St[ch].tablePtr = A1;
            pcm8St[ch].mode = D1;
            pcm8St[ch].length = D2;
            pcm8St[ch].Keyon = true;
            break;
        case 0x0100:
            switch (D0 & 0xffff) {
            case 0x0100:
                ch = (D0 & 0xff) % 8;
                pcm8St[ch].tablePtr = 0;
                pcm8St[ch].mode = 0;
                pcm8St[ch].length = 0;
                pcm8St[ch].Keyon = false;
                if (pcm8type == 0) if (mdxPCM != null) mdxPCM.chips[0].pcm8Out(D0 & 0xff, null, 0, 0, 0); // Stop the specified channel
                else if (pcm8pp != null) pcm8pp.keyOff(0, D0 & 0xff); // Stop the specified channel
                break;
            case 0x0101:
                if (mdxPCM != null) mdxPCM.chips[0].pcm8Abort(); // Stop all channels
                break;
            }
            break;
        case 0x01f0:
            switch (D0 & 0xffff) {
            case 0x01fc:
                D0 = 1;
                break;
            }
            break;
        default:
            break;
        }
    }

    // 
    private void OPM_SUB() {
        if (measurePlayTime) return;

        //logger.log(Level.TRACE, "%02x %02x".formatted(D1 & 0xff, D2 & 0xff));

        if (mdxPCM != null) mdxPCM.soundIocs[0].opmSet(D1 & 0xff, D2 & 0xff);
        plugin.audio.chipRegister.chip(Ym2151Chip.class).write(0, 0, D1, D2, model, ym2151Hosei[0], vgmFrameCounter);

        if (D1 == 0x10) {
            timerA = ((D2 & 0xff) << 2) + (timerA & 0x3);
        } else if (D1 == 0x11) {
            timerA = (D2 & 0x3) + (timerA & 0x3fc);
        } else if (D1 == 0x12) {
            timerB = D2 & 0xff;
        } else if (D1 == 0x14) {
            //timerABFlag = (byte) D2;
        }
    }

    // 
    private void ADPCMOUT() {
        if (pcm8type == 0) if (mdxPCM != null) mdxPCM.soundIocs[0].adpcmOut(A1, D1, D2);
        else if (pcm8pp != null) pcm8pp.keyOn(0, 0, A1, D1 + 0x0c00, D2);
    }

    private void ADPCMMOD_STOP() {
        if (pcm8type == 0) if (mdxPCM != null) mdxPCM.soundIocs[0].adpcmMod(1);
        else if (pcm8pp != null) pcm8pp.keyOff(0, 0);
    }

    private void ADPCMMOD_END() {
        if (pcm8type == 0) if (mdxPCM != null) mdxPCM.soundIocs[0].adpcmMod(0);
        else if (pcm8pp != null) pcm8pp.keyOff(0, 0);
    }

    // 
    private void OPMINTFUNC() {
        synchronized (CS_OPMINT) {
            OPMINT_FUNC.run();
            if (mm.readByte(G + MXWORK_GLOBAL.STOPMUSICTIMER) == 0) {
                mm.write(G + MXWORK_GLOBAL.PLAYTIME,
                        mm.readInt(G + MXWORK_GLOBAL.PLAYTIME) +
                                (256 - (mm.readByte(G + MXWORK_GLOBAL.MUSICTIMER) & 0xff))
                ); // OPMBUF[0x12];
            }
            if (MXCALLBACK_OPMINT != null) MXCALLBACK_OPMINT.run();
        }
    }

    private void SETOPMINT(Runnable func) {
        OPMINT_FUNC = func;
        if (mdxPCM != null) mdxPCM.chips[0].opmInt(this::OPMINTFUNC);
    }

    // 
    private void MX_ABORT() {
    }

    /** */
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
        jumpTable[D0].run();

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
    private void L_0A() {
        mm.write(G + MXWORK_GLOBAL.L001e14, (byte) D1);
    }

    // 
    private void L_0B() {
        mm.write(G + MXWORK_GLOBAL.L001e15, (byte) D1);
    }

    // 
    private void L_0C() {
        mm.write(G + MXWORK_GLOBAL.L001e1e, (short) D1);
        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
    }

    // 
    private void L_0E() {
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) D1);
    }

    // 
    private void L_10() {
        A0 = OPMBUF;
        D0 = A0;
    }

    // 
    private void L_11() {
        if (D1 < 0) {
            mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) D1);
        } else {
            D0 = mm.readByte(G + MXWORK_GLOBAL.L001e0e) & 0xff;
        }
    }

    // 
    private void L_12() {
        D0 = (mm.readByte(G + MXWORK_GLOBAL.L001e12) & 0xff) * 256 +
                (mm.readByte(G + MXWORK_GLOBAL.L001e13) & 0xff);
    }

    // 
    private void L_13() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e0a) & 0xff;
        mm.write(G + MXWORK_GLOBAL.L001e0a, (byte) D1);
    }

    // 
    private void L_14() {
        D0 = (~(mm.readShort(G + MXWORK_GLOBAL.L001e06) & 0xffff)) & 0xffff;
    }

    // 
    private void L_15() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e0b) & 0xff;
        mm.write(G + MXWORK_GLOBAL.L001e0b, (byte) D1);
    }

    // 
    private void L_16() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e08) & 0xff;
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) D1);
        L_STOP();
    }

    // 
    private void L_17() {
        D0 = mm.readByte(G + MXWORK_GLOBAL.L001e08) & 0xff;
        if (D0 == 0) {
            D0 = (mm.readByte(G + MXWORK_GLOBAL.L001e12) & 0xff) * 256 +
                    (mm.readByte(G + MXWORK_GLOBAL.L001e13) & 0xff);
            return;
        }
        L0000dc();
    }

    private void L0000dc() {
        int[] a0_w = new int[2];
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
exit:
        if (mm.readByte(A1) != 0) {
            if (/* signed */ mm.readByte(A1) < 0) {
                mm.write(A1, (byte) 0x7f);
                a0_w[1] = a0_w[0];
            }
            if (/* signed */ a0_w[1] >= 0) {
                a0_w[1] -= 2;
            } else {
                A1 = G + MXWORK_GLOBAL.L001e14;
                if (/* signed */ mm.readByte(A1) >= 0x0a) {
                    mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.SET);
                }

                if (/* signed */ mm.readByte(A1) < 0x3e) {
                    mm.write(A1, (byte) ((mm.readByte(A1) + 1) & 0xff));
                    a0_w[1] = a0_w[0];
                } else {
                    if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
                        L00077a(); // A1 dif
                        break exit;
                    }

                    mm.write(A1, (byte) 0x7f);
                    mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.CLR);
                    mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
                    L_PAUSE_(); // Do not stop the L_PAUSE() timer

                    if (PCM8 != 0) {
                        D0 = 0x0100;
                        PCM8_SUB();
                    }

                    if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                        D0 = 0x01ff;
                        PCM8_SUB();
                        mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
                    }
                }
            }

            A0 = G + MXWORK_GLOBAL.L001e0c;
            D2 = mm.readByte(A0) & 0xff;
            D1 = 0x12;

            if (mm.readByte(G + MXWORK_GLOBAL.L001e13) == 0) {

                mm.write(G + MXWORK_GLOBAL.L001ba6, (short) ((mm.readShort(G + MXWORK_GLOBAL.L001ba6) + 1) & 0xffff));
                A6 = MXWORK_CHBUF_FM[0];
                D7 = 0x00;

                do {
                    L001050();
                    L0011b4();
                    D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c) & 0xffff;
                    if ((D0 & (1 << D7)) == 0) {
                        L000c66();
                    }
                    A6 += MXWORK_CH.Length;
                    D7++;
                } while (D7 < 0x0009);

                if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                    A6 = MXWORK_CHBUF_PCM[0];

                    do {
                        L001050();
                        L0011b4();
                        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c) & 0xffff;
                        if ((D0 & (1 << D7)) == 0) {
                            L000c66();
                        }
                        A6 += MXWORK_CH.Length;
                        D7++;
                    } while (D7 < 0x0010);
                }
            }
        }

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

        D0 = (mm.readByte(G + MXWORK_GLOBAL.L001e12) & 0xff) * 256 +
                (mm.readByte(G + MXWORK_GLOBAL.L001e13) & 0xff);
    }

    // 
    private void L_18() {
        A0 = MXWORK_CHBUF_PCM[0];
        D0 = A0;
    }

    // 
    private void L_19() {
        A0 = G + MXWORK_GLOBAL.L001bb4;
        D0 = A0;
    }

    // 
    private void L_1A() {
        L000216();
    }

    // 
    private void L000216() {
        int a2_l;
        int t0;

        int d2 = D2;
        int d3 = D3;
        int d4 = D4;
        int a0 = A0;
        int a1 = A1;
        int a2 = A2;
        int a1_l = A0;
        D0 = 0x0000_0000;
        D3 = 0xffff_ffff;
        D1 = 0x0000_0000;

exit:   {
            while (true) {
                D4 = Depend.getBLong(mm, a1_l);
                a1_l++;
                D2 = Depend.getBLong(mm, a1_l);
                a1_l++;
                D4 &= 0x00ff_ffff;
                if (D4 != 0) {
                    t0 = Depend.getBLong(mm, a1_l - 2);
                    if (t0 != D4) { break; }
                    D2 &= 0x00ff_ffff;
                    if (D2 != 0) {
                        t0 = Depend.getBLong(mm, a1_l - 1);
                        if (t0 != D2) { break; }
                        D2 += D4;
                        if (Integer.compareUnsigned(D1, D2) <= 0) {
                            D1 = D2;
                        }
                        if (Integer.compareUnsigned(D4, D3) <= 0) {
                            D3 = D4;
                        }
                        a2_l = (A0 + D3);
                        if (a2_l == a1_l) { D0++; break; }
                        if (a2_l < a1_l) { break exit; }
                    }
                }
                D0++;
            }
            D2 = d2;
            D3 = d3;
            D4 = d4;
            A0 = a0;
            A1 = a1;
            A2 = a2;
            return;
        }
        D0 = 0xffff_ffff;
        D2 = d2;
        D3 = d3;
        D4 = d4;
        A0 = a0;
        A1 = a1;
        A2 = a2;
    }

    // 
    private void L_1B() {
        int d1 = D1;
        int d2 = D2;
        int d3 = D3;
        int d4 = D4;
        int d5 = D5;
        int a0 = A0;
        int a1 = A1;
        int a2 = A2;
        L000216();
        D2 = D0;

        if (d2 >= 0) {
            D5 = D0;
            D0 <<= 3;
            D3 = 0x60;

            do {
                D2 -= D3;
            } while (D2 >= 0);

            D2 += D3;
            if (D2 != 0) {
                D3 -= D2;
                D4 = D3;
                D3 <<= 3;
                D2 = D1;
                D2++;
                D2 &= 0xffff_fffe;
                int a2_l = (A0 + D2);
                D1 += D3;
                int a1_l = (A0 + D1);
                D2 -= D0;
                D2 >>>= 1;
                D0 = D2;
                D0 >>>= 1;
                D0--;

                do {
                    a2_l -= 4;
                    a1_l -= 4;
                    mm.write(a1_l, mm.readInt(a2_l));
                } while (D0-- != 0);

                D2 &= 0xffff_0001;
                if ((D2 & 0xffff) != 0) {
                    a1_l -= 2;
                    a2_l -= 2;
                    mm.write(a1_l, mm.readShort(a2_l));
                    A2 = a2_l & 0xff;
                }

                D4--;
                D4 &= 0xffff;

                do {
                    --a1_l;
                    mm.write(a1_l, (byte) Depend.CLR);
                    --a1_l;
                    mm.write(a1_l, (byte) Depend.CLR);
                } while (D4-- != 0);

                A1 = a1_l & 0xff;
                D5--;
                d5 = D5;

                int a0_l = A0;

                do {
                    D0 = Depend.getBLong(mm, a0_l);
                    a0_l++;
                    if (D0 != 0) {
                        Depend.putBLong(mm, a0_l - 1, D0 + D3);
                    }
                    a0_l++;
                } while (D5-- != 0);
                A0 = a0_l & 0xff;
            }
        }

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
        try {
            L000216();
            if (D0 < 0) { // break L000462;
                D0 = 0xffff_fffd;
                return;
            }
            D1 += A0;
            D1++;
            D1 &= 0xffff_fffe;
            D3 = D1;
            D7 = D3;
            D2 = D0;
            int t0 = A0;
            A0 = A1;
            A1 = t0;
            L000216();
            if (D0 < 0) { // break L000462;
                D0 = 0xffff_fffd;
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
                D0 = 0xffff_ffff;
                return;
            }
            D1 = 0x60;
            D3 = D2;

            do {
                D3 -= D1;
            } while (D3 >= 0);
            D3 += D1;
            if (D3 != 0) {
                D3 -= D1;
                D3 = -D3;
                D1 = D3;
                D3 <<= 3;
                if (A2 < D3) { // break L00045a;
                    D0 = 0xffff_ffff;
                    return;
                }
            }
            D3 += D0;
            D3 <<= 3;
            D3 += D7;
            A4 = A0;
            if (Integer.compareUnsigned(A0, D3) <= 0) {
                D1 = D0;
                D1 <<= 3;
                if (mm.readInt(G + MXWORK_GLOBAL.L001ba8) < D1) {
                    D0 = 0xffff_fffe;
                    return;
                }
                A4 = mm.readInt(G + MXWORK_GLOBAL.L001bac);
                A3 = A0;
                D1 = D0;
                D1--;

                int a3_l = A3;
                int a4_l = A4;

                do {
                    mm.write(a4_l, mm.readInt(a3_l));
                    a4_l += 4;
                    a3_l += 4;
                    mm.write(a4_l, mm.readInt(a3_l));
                    a4_l += 4;
                    a3_l += 4;
                } while (D1-- != 0);
                A4 = mm.readInt(G + MXWORK_GLOBAL.L001bac);
                A3 = a3_l;
            }

            D0 <<= 3;
            D5 = D0;
            t0 = A0;
            A0 = A1;
            A1 = t0;
            L_1B();
            if (D0 < 0) {
                D0 = 0xffff_fffc;
                return;
            }
            L000216();
            D2 = D0;
            if (D2 < 0) { // break L000462;
                D0 = 0xffff_fffd;
                return;
            }
            D1 += A0;
            D1++;
            D1 &= 0xffff_fffe;
            A2 = D1;
            D1 += D5;
            D0 <<= 3;
            D0 += A0;
            D0 += D5;
            A3 = D1;
            A1 = A3;
            D1 -= D0;
            D7 = D1;
            D1 >>>= 1;
            int c0 = D1 & 1;
            D1 >>>= 1;
            D1--;

            int a2_l = A2;
            int a3_l = A3;

            do {
                a2_l -= 4;
                a3_l -= 4;
                mm.write(a3_l, mm.readInt(a2_l));
            } while (D1-- != 0);
            if (c0 != 0) {
                a2_l -= 2;
                a3_l -= 2;
                mm.write(a3_l, mm.readShort(a2_l));
                A2 = a2_l & 0xff;
                A3 = a3_l & 0xff;
            }

            A2 = D0 & 0xff;
            A2 -= D5;
            if (A2 != A4) {
                D1 = D5;
                D1 >>>= 3;
                D1--;
                a2_l = A2;
                int a4_l = A4;

                do {
                    mm.write(a2_l, mm.readInt(a4_l));
                    a4_l += 4;
                    a2_l += 4;
                    mm.write(a2_l, mm.readInt(a4_l));
                    a4_l += 4;
                    a2_l += 4;
                } while (D1-- > 0);
                A2 = a2_l & 0xff;
                A4 = a4_l & 0xff;
            }

            A2 = D4 & 0xff;
            D6 -= D4;
            D1 = D6;
            D2 >>>= 2;
            D1--;
            a2_l = A2;
            int a1_l = A1;

            do {
                mm.write(a1_l, mm.readInt(a2_l));
                a1_l += 4;
                a2_l += 4;
            } while (D1-- > 0);
            D1 = D6;
            D1 &= 0x0000_0002;
            if (D1 != 0) {
                mm.write(a1_l, mm.readShort(a2_l));
                a1_l += 2;
                a2_l += 2;
                A1 = a1_l;
                A2 = a2_l;
            }

            D6 &= 0x0000_0001;
            if (D1 != 0) {
                a1++;
                a2++;
                A1 = A2;
            }

            A1 -= A0;
            D1 = D5;
            D0 = D2;
            D0 <<= 3;
            D7 += D0;
            D2--;

            do {
                D0 = Depend.getBLong(mm, A0);
                if (D0 != 0) {
                    Depend.putBLong(mm, A0, D0 + D1);
                }

                A0 = (A0 + 2) & 0xff;
                if (D2-- <= 0) {
                    D5 >>>= 3;
                    D5--;

                    D0 = Depend.getBLong(mm, A0);
                    if (D0 != 0) {
                        Depend.putBLong(mm, A0, D0 + D7);
                    }

                    A0 = (A0 + 2) & 0xff;
                }
            } while (D2-- > 0);
            D5 >>>= 3;
            D5--;
            D0 = A1;

        } finally {
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
    }

    // 
    private void L_1D() {
        D4 = mm.readByte(G + MXWORK_GLOBAL.L001e08) & 0xff;
        D3 = D1;
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) Depend.SET);
        D1 = 0xffff;
        int d2 = D2;
        int d3 = D3;
        int d4 = D4;
        L_0F();
        D4 = d4;
        D3 = d3;
        D2 = d2;
        L000496();
    }

    // 
    private void L_1E() {
        D4 = mm.readByte(G + MXWORK_GLOBAL.L001e08) & 0xff;
        D3 = D1;
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) 0xffff);
        mm.write(G + MXWORK_GLOBAL.L001e08, (byte) Depend.SET);
        L000496();
    }

    // 
    private void L000496() {
        if (mm.readByte(G + MXWORK_GLOBAL.L001e13) == 0) {
            if (D2-- != 0) {
                int d2 = D2;
                int d3 = D3;
                int d4 = D4;

                do {
                    L0000dc();
                } while ((D2--) != 0);
                D4 = d4;
                D3 = d3;
                D2 = d2;
            }
        }

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
        D1 = 0x0000_0012;
        D2 = mm.readByte(G + MXWORK_GLOBAL.L001e0c) & 0xff;
        L_WRITEOPM();
        D1 = 0x0000_0014;
        D2 = 0x0000_003a;
        L_WRITEOPM();

        L_1F();
    }

    // 
    private void L_1F() {
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001ba6) & 0xffff;
    }

    // 
    private void L_0D() {
        if ((D1 & 0xff) == 0xf0) {
            L000552();
            return;
        }
        if ((D1 & 0xff) == 0xfc) {
            D0 = mm.readByte(G + MXWORK_GLOBAL.L001e19) & 0xff;
            return;
        }
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

        while (Depend.getBWord(mm, A1) != 0) {
            A1 = ((A1 & 0xffff) + 3) & 0xff;
        }
        A1 = ((A1 & 0xffff) - 3) & 0xff;
        mm.write(G + MXWORK_GLOBAL.L001e2c, A1);
        mm.write(G + MXWORK_GLOBAL.L001e18, (byte) Depend.SET);
        mm.write(G + MXWORK_GLOBAL.L001e19, (byte) Depend.SET);
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e24);
        L000788();
    }

    // 
    private void L000534() {
        mm.write(G + MXWORK_GLOBAL.L001e18, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L002230, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L002231, (byte) Depend.CLR);
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e34);
        mm.write(G + MXWORK_GLOBAL.L002218, Depend.getBLong(mm, A0));
        mm.write(G + MXWORK_GLOBAL.L00221c, Depend.getBLong(mm, A0 + 4));
        L00063e();
    }

    // 
    private void L000552() {
        L000534();
        L000554();
    }

    // 
    private void L000554() {
        D0 = 0;
        mm.write(G + MXWORK_GLOBAL.L001e30, D0); // ?
        mm.write(G + MXWORK_GLOBAL.L001e19, (byte) D0); // ?
    }

    /**
     * interrupt
     */
    private void L00056a() {
        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) Depend.CLR);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e08) == 0) {
            SETOPMINT(this::L_OPMINT);
        }
    }

    // 
    private void L_FREE() {
        L00063e();
        if (mm.readByte(G + MXWORK_GLOBAL.L001e19) != 0) {
            L000554();
        }
    }

    // 
    private void L_SETMDX() {
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
            int d1 = D1;
            int a1 = A1;
            L000552();
            A1 = a1;
            D1 = d1;
        }
        A2 = G + MXWORK_GLOBAL.L002230;
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e34);
        mm.write(G + MXWORK_GLOBAL.L002218, A0);
        D0 = mm.readInt(G + MXWORK_GLOBAL.L002220);
        L0005f8();
    }

    // 
    private void L_SETPDX() {
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
            int d1 = D1;
            int a1 = A1;
            L000552();
            A1 = a1;
            D1 = d1;
        }
        A2 = G + MXWORK_GLOBAL.L002231;
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e38);
        mm.write(G + MXWORK_GLOBAL.L00221c, A0);
        D0 = mm.readInt(G + MXWORK_GLOBAL.L002224);
        L0005f8();
    }

    // 
    private void L0005f8() {
        if (D1 <= D0) {
            int d1 = D1;
            int a0 = A0;
            int a1 = A1;
            int a2 = A2;
            L00063e();
            A2 = a2;
            A1 = a1;
            A0 = a0;
            D1 = d1;
            D0 = D1;
            D0 &= 0x0003;
            int a1_l = A1;
            int a0_l = A0;
            D1 >>>= 2;

            do {
                mm.write(a0_l, mm.readInt(a1_l));
                a0_l += 4;
                a1_l += 4;
            } while (--D1 != 0);
            A1 = a1_l;
            A0 = a0_l;
            if (D0 != 0) {
                D0--;

                do {
                    mm.write(A0, mm.readByte(A1));
                    A0++;
                    A1++;
                } while (D0-- != 0);
            }

            mm.write(A2, (byte) Depend.SET);
            D0 = 0;
        } else {
            D0 |= (1 << 0x1f);
        }
    }

    // 
    private void L_STOP() {
        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
            L000552();
        } else {
            L00063e();
        }
    }

    // 
    private void L00063e() {
        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
        L0006c4();
        if (PCM8 != 0) {
            D0 = 0x0100;
            PCM8_SUB();
        }
        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
            D0 = 0x01ff;
            PCM8_SUB();
            mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
        }
        D2 = 0x0f;
        D1 = 0xe0;

        do {
            L_WRITEOPM();
            D1++;
        } while ((D1 & 0xff) != 0);
        A0 = G + MXWORK_GLOBAL.L00223c;
        A1 = G + MXWORK_GLOBAL.L001bb4;
        D3 = 0x07;
        D2 = 0x00;
        D1 = 0x08;

        do {
            L_WRITEOPM();
            mm.write(A0, (byte) D2);
            A0++;
            mm.write(A1, (byte) D2);
            A1++;
            D2++;
        } while (D3-- != 0);
    }

    // 
    private void L_PAUSE() {
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.SET);
        mm.write(G + MXWORK_GLOBAL.STOPMUSICTIMER, (byte) Depend.SET);
        L0006c4();
    }

    // 
    private void L_PAUSE_() {
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.SET);
        L0006c4();
    }

    // 
    private void L0006c4() {
        D7 = 0x07;
        A6 = MXWORK_CHBUF_FM[0];

        do {
            D0 = 0x7f;
            L000e28();
            A6 += MXWORK_CH.Length;
        } while (D7-- != 0);
        if (PCM8 != 0) {
            D0 = 0x01fc;
            D1 = 0xffff_ffff;
            PCM8_SUB();
            if ((D0 & 0xff) == 0x01) {
                D0 = 0x0101;
                PCM8_SUB();
                return;
            }
            ADPCMMOD_STOP();
        }
        ADPCMMOD_END();
    }

    // 
    private void L_CONT() {
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.STOPMUSICTIMER, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.MUSICTIMER, mm.readByte(G + MXWORK_GLOBAL.L001e0c));
        D7 = 0x07;
        A6 = MXWORK_CHBUF_FM[0];

        do {
            L000dfe();
            A6 += MXWORK_CH.Length;
        } while (D7-- != 0);
        if (PCM8 == 0) {
            L000756();
            return;
        }

        D0 = 0x01fc;
        D1 = 0xffff_ffff;
        PCM8_SUB();
        if ((D0 & 0xff) != 0x01) {
            L000756();
            return;
        }
        D0 = 0x0102;
        PCM8_SUB();
        L000756();
    }

    // 
    private void L000756() {
        D2 = 0x30;
        D1 = mm.readByte(G + MXWORK_GLOBAL.L001e08) & 0xff;
        if (D1 == 0) {
            D2 = 0x3a;
        }
        // Timer operation control (set 0x30/0x3a)
        D1 = 0x14;
        L_WRITEOPM();
    }

    // 
    private void L000766() {
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
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e28);
        A0 += 0x06;
        if (Depend.getBWord(mm, A0) != 0) {
            L000788();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L001e24);

        L000788();
    }

    // 
    private void L000788() {
        mm.write(G + MXWORK_GLOBAL.L001e28, A0);
        mm.write(G + MXWORK_GLOBAL.L001e22, Depend.getBWord(mm, A0));
        A1 = Depend.getBLong(mm, A0 + 2);
        mm.write(G + MXWORK_GLOBAL.L00221c, Depend.getBLong(mm, A1));
        A1 += 4;
        D0 = Depend.getBWord(mm, A1) & 0xffff;
        D0 = ~D0;
        D1 = Depend.getBWord(mm, A1 + 2) & 0xffff;
        D1 = ~D1;
        mm.write(G + MXWORK_GLOBAL.L002230, (byte) D0);
        mm.write(G + MXWORK_GLOBAL.L002231, (byte) D1);
        mm.write(G + MXWORK_GLOBAL.L002218, A1);
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) Depend.CLR);
        L0007c0();
    }

    // 
    private void L_PLAY() {
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) Depend.CLR);
        L0007c0();
    }

    // 
    private void L_0F() {
        mm.write(G + MXWORK_GLOBAL.L001e1c, (short) D1);
        L0007c0();
    }

    // 
    private void L0007c0() {
        mm.write(G + MXWORK_GLOBAL.PLAYTIME, 0);
        // checker
        mm.write(G + MXWORK_GLOBAL.FATALERROR, 0);
        // checker end
        mm.write(G + MXWORK_GLOBAL.L001e14, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) Depend.CLR);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0 && PCM8 != 0) {
            D0 = 0x0100;
            PCM8_SUB();
        }
        mm.write(G + MXWORK_GLOBAL.L001e12, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.STOPMUSICTIMER, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) 0x01ff);
        mm.write(G + MXWORK_GLOBAL.L001e06, (short) 0x01ff);
        mm.write(G + MXWORK_GLOBAL.L002246, (short) Depend.CLR);
        mm.write(G + MXWORK_GLOBAL.L001ba6, (short) Depend.CLR);
        D0 = mm.readByte(G + MXWORK_GLOBAL.L002230) & 0xff;
        if (D0 == 0) {
            L_ERROR();
            return;
        }
        L00063e();
        A2 = mm.readInt(G + MXWORK_GLOBAL.L002218);
        D1 = Depend.getBWord(mm, A2 + 2) & 0xffff;
        if ((short) D1 >= 0) { // break L000848;
            if (mm.readByte(G + MXWORK_GLOBAL.L002231) == 0) {
                L_ERROR();
                return;
            }
            A0 = mm.readInt(G + MXWORK_GLOBAL.L00221c);
            while (D1-- != 0) {
                if (Depend.getBLong(mm, A0) == 0) {
                    L_ERROR();
                    return;
                }
                A0 += Depend.getBLong(mm, A0);
            }
            A0 += Depend.getBWord(mm, A0 + 4) & 0xffff;
            mm.write(G + MXWORK_GLOBAL.L00222c, A0);
        }
        A2 += Depend.getBWord(mm, A2 + 4) & 0xffff;
        A1 = A2;
        A0 = A2;
        D0 = 0x0000_0000;
        D0 = Depend.getBWord(mm, A1) & 0xffff;
        A1 += 2;
        A2 += D0;
        mm.write(G + MXWORK_GLOBAL.L002228, A2);
        A6 = MXWORK_CHBUF_FM[0];
        A3 = (byte) 0;
        D6 = 0xffff_ffff;
        D7 = 0x0000_0000;

        while (true) { // Initialize track parameters (including exPCM)
            A2 = A0;
            D0 = Depend.getBWord(mm, A1) & 0xffff;
            A1 += 2;
            A2 += D0;
            mm.write(A6 + MXWORK_CH.S0000, A2);
            mm.write(A6 + MXWORK_CH.S0026, A3);
            mm.write(A6 + MXWORK_CH.S0040, A3);
            mm.write(A6 + MXWORK_CH.S0014, (short) D6);
            mm.write(A6 + MXWORK_CH.S0023, (byte) D6);
            mm.write(A6 + MXWORK_CH.S0018, (byte) D7);
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
            if (D7 < 0x0008) {
                D1 = 0x38;
                D1 += D7;
                D2 = 0x00;
                L_WRITEOPM();
                D7++;
                A6 += MXWORK_CH.Length;
            } else {
                mm.write(A6 + MXWORK_CH.S001c, (byte) 0x10);
                mm.write(A6 + MXWORK_CH.S0022, (byte) 0x08);
                mm.write(A6 + MXWORK_CH.S0018, (byte) D7);
                mm.write(A6 + MXWORK_CH.S0018, (byte) (mm.readByte(A6 + MXWORK_CH.S0018) & 0x07));
                mm.write(A6 + MXWORK_CH.S0018, (byte) (mm.readByte(A6 + MXWORK_CH.S0018) | 0x80));
                mm.write(A6 + MXWORK_CH.S0004_b, (byte) 0x00);
                if (D7 == 0x000f) break;
                D7++;
                A6 += MXWORK_CH.Length;
                if (D7 == 0x09) {
                    A6 = MXWORK_CHBUF_PCM[0];
                }
            }
        }

        A0 = G + MXWORK_GLOBAL.L001df6 + 0;
        D0 = 0x0f;

        do {
            mm.write(A0, (byte) Depend.CLR);
            A0++;
        } while (D0-- >= 0);
        mm.write(G + MXWORK_GLOBAL.L002232, (byte) Depend.CLR);

        // LFO SW OFF
        D2 = 0x00;
        D1 = 0x01;
        L_WRITEOPM();

        // Noise OFF
        D1 = 0x0f;
        L_WRITEOPM();

        // LFO AMD initialization
        D1 = 0x19;
        L_WRITEOPM();

        // LFO PMD initialization
        D2 = 0x80;
        L_WRITEOPM();

        // Timer-B initialization
        D2 = 0xc8;
        D1 = 0x12;
        mm.write(G + MXWORK_GLOBAL.L001e0c, (byte) D2);
        mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e08) == 0) {
            L_WRITEOPM();
        }
        L00056a();
        L000756();
        D0 = 0;
    }

    // 
    private void L_ERROR() {
        D0 = 0xffff_ffff;
        L00095a();
    }

    // 
    private void L00095a() {
    }

    // 
    private void L_08() {
        if (mm.readByte(G + MXWORK_GLOBAL.L002230) == 0) {
            L000998();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L002218);
        do {
            if (Depend.getBWord(mm, A0) == 0) {
                L000998();
                return;
            }
            A0 += Depend.getBWord(mm, A0) & 0xffff;
        } while (D1-- >= 0);
        A0 += Depend.getBWord(mm, A0 + 6) & 0xffff;
        D0 = A0;
    }

    // 
    private void L_09() {
        if (mm.readByte(G + MXWORK_GLOBAL.L002231) == 0) {
            L000998();
            return;
        }
        A0 = mm.readInt(G + MXWORK_GLOBAL.L00221c);
        do {
            if (Depend.getBLong(mm, A0) == 0) {
                L000998();
                return;
            }
            A0 += Depend.getBLong(mm, A0);
        } while (D1-- >= 0);
        A0 += Depend.getBWord(mm, A0 + 6) & 0xffff;
        D0 = A0;
    }

    // 
    private void L000998() {
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
        boolean executeLoops = true;

exit: {
        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) == 0) {
            a0_w = G + MXWORK_GLOBAL.L001e1e + 0;
            A1 = G + MXWORK_GLOBAL.L001e17;
            if (mm.readByte(A1) != 0) {
                if (/* signed */ mm.readByte(A1) < 0) {
                    mm.write(A1, (byte) 0x7f);
                    mm.write(a0_w + 2, mm.readShort(a0_w + 0));
                }

                if (mm.readShort(a0_w + 2) >= 0) {
                    mm.write(a0_w + 2, (short) ((mm.readShort(a0_w + 2) - 2) & 0xffff));
                } else {
                    A1 = G + MXWORK_GLOBAL.L001e14;
                    if (/* signed */ mm.readByte(A1) >= 0x0a) {
                        mm.write(G + MXWORK_GLOBAL.L001e15, (byte) Depend.SET);
                    }
                    if (/* signed */ mm.readByte(A1) < 0x3e) {
                        mm.write(A1, (byte) ((mm.readByte(A1) + 1) & 0xff));
                        mm.write(a0_w + 2, mm.readShort(a0_w + 0));
                    } else {
                        if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
                            L00077a();
                            executeLoops = false;
                            break exit;
                        }
                        mm.write(A1, (byte) 0x7f);
                        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.CLR);
                        mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
                        L_PAUSE_(); // Do not stop the L_PAUSE() timer
                        if (PCM8 != 0) {
                            D0 = 0x0100;
                            PCM8_SUB();
                        }
                        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                            D0 = 0x01ff;
                            PCM8_SUB();
                            mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
                        }
                    }
                }
            }
        }

        A0 = G + MXWORK_GLOBAL.L001e0c;
        D2 = mm.readByte(A0) & 0xff;
        D1 = 0x12;
IL_892: { // java doesn't have goto statement, so what?
IL_836: { // don't say those blocks ugly.
IL_815: { // can you port the spagetti code like this method to java w/o blocks?
IL_800: { // c# break is able to refer after, what's the hell. is it different from goto?
        if (mm.readByte(G + MXWORK_GLOBAL.L001e0a) == 0) {
            if (mm.readShort(KEY + MXWORK_KEY.OPT1) != 0 && mm.readShort(KEY + MXWORK_KEY.SHIFT) != 0) {
                D2 = (-D2) & 0xff;
                D2 += D2;
                D2 = (-D2) & 0xff;
                break IL_836;
            }
            if (mm.readShort(KEY + MXWORK_KEY.OPT2) != 0 && mm.readShort(KEY + MXWORK_KEY.SHIFT) != 0) {
                D2 = (-D2) & 0xff;
                D2 >>>= 2;
                D2++;
                D2 = (-D2) & 0xff;
                break IL_836;
            }
            if (mm.readShort(KEY + MXWORK_KEY.OPT1) != 0 && mm.readShort(KEY + MXWORK_KEY.CTRL) != 0) {
                D2 = 0x00;
                break IL_836;
            }
            if (mm.readShort(KEY + MXWORK_KEY.OPT2) == 0 || mm.readShort(KEY + MXWORK_KEY.CTRL) == 0) {
IL_7E5: { // dnSpy helps me a lot.
IL_7BF: { // w/o that, i couldn't port this method to java.
                if (mm.readShort(KEY + MXWORK_KEY.OPT1) == 0) {
                    if (mm.readShort(KEY + MXWORK_KEY.OPT2) == 0) {
                        if (mm.readShort(KEY + MXWORK_KEY.CTRL) == 0) {
                            break IL_800;
                        }
                        if (mm.readShort(KEY + MXWORK_KEY.XF3) == 0) {
                            if (mm.readShort(KEY + MXWORK_KEY.XF5) == 0) {
                                if (mm.readShort(KEY + MXWORK_KEY.XF4) == 0) {
                                    break IL_800;
                                }
                                if (mm.readByte(G + MXWORK_GLOBAL.L001e10) == 0) {
                                    mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) ((mm.readByte(G + MXWORK_GLOBAL.L001e0e) - 1) & 0xff));
                                    break IL_7E5;
                                }
                                break IL_7E5;
                            } else {
                                if (mm.readByte(G + MXWORK_GLOBAL.L001e10) == 0) {
                                    mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) ((mm.readByte(G + MXWORK_GLOBAL.L001e0e) + 1) & 0xff));
                                    break IL_7E5;
                                }
                                break IL_7E5;
                            }
                        } else {
                            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) == 0) {
                                mm.write(G + MXWORK_GLOBAL.L001e0e, (byte) Depend.CLR);
                                break IL_7E5;
                            }
                            break IL_7E5;
                        }
                    } else if (mm.readShort(KEY + MXWORK_KEY.XF4) == 0) {
                        if (mm.readShort(KEY + MXWORK_KEY.XF5) == 0) {
                            break IL_800;
                        }
                        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0) {
                            if (/* signed */ mm.readByte(G + MXWORK_GLOBAL.L001e12) < 0
                                    && mm.readByte(G + MXWORK_GLOBAL.L001e10) == 0) {
                                L_CONT();
                            }
                        } else {
                            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) {
                                break IL_7E5;
                            }
                            L_PAUSE();
                        }
                    } else {
                        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0
                                && mm.readByte(G + MXWORK_GLOBAL.L001e10) == 0) {
                            mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.SET);
                            D2 = 0;
                            break IL_892;
                        }
                        break IL_7E5;
                    }
                } else {
IL_6F4: { // btw dnSpy is discontinued, why every free decompiler get trouble?
                    if (mm.readShort(KEY + MXWORK_KEY.XF4) != 0 || mm.readShort(KEY + MXWORK_KEY.XF5) != 0) { // break L000b82;
                        if (mm.readShort(KEY + MXWORK_KEY.XF4) == 0) {
                            if (mm.readShort(KEY + MXWORK_KEY.XF5) == 0) {
                                break IL_6F4;
                            }
                            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) {
                                break IL_7E5;
                            }
                            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
                                L000766();
                                break IL_7BF;
                            }
                        } else {
                            if (mm.readByte(G + MXWORK_GLOBAL.L001e10) != 0) {
                                break IL_7E5;
                            }
                            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) != 0) {
                                L00077a();
                                break IL_7BF;
                            }
                        }
                        L_PLAY();
                        break IL_7BF;
                    }
/*IL_6F4:*/}
                    if (mm.readShort(KEY + MXWORK_KEY.XF3) == 0) {
                        break IL_800;
                    }
                    if (mm.readByte(G + MXWORK_GLOBAL.L001e10) == 0) {
                        mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) 0x0011);
                        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
                        break IL_7E5;
                    }
                    break IL_7E5;
                }
/*IL_7BF:*/}
                mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.SET);
                executeLoops = false;
                break exit;
/*IL_7E5:*/}
                mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.SET);
                break IL_815;
            }
            D2 = 0xff;
            break IL_836;
        }
/*IL_800:*/}
        mm.write(G + MXWORK_GLOBAL.L001e10, (byte) Depend.CLR);
/*IL_815:*/}
        D2 += mm.readByte(G + MXWORK_GLOBAL.L001e0e) & 0xff;
/*IL_836:*/}
        if (mm.readByte(G + MXWORK_GLOBAL.L001e12) != 0 ||
            mm.readByte(G + MXWORK_GLOBAL.L001e13) != 0) {
            D2 = 0x00;
            mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
            L_WRITEOPM();
            executeLoops = false;
            break exit;
        }
/*IL_892:*/}
        //G.MUSICTIMER = D2; // You can't enter here
        if (executeLoops) {
            L_WRITEOPM();
            mm.write(G + MXWORK_GLOBAL.L001ba6, (short) ((mm.readShort(G + MXWORK_GLOBAL.L001ba6) + 1) & 0xffff));
            A6 = MXWORK_CHBUF_FM[0];
            D7 = 0x00;

            do {
                //logger.log(Level.TRACE, "Ch%02d adr:%04x".formatted(D7, mm.readInt(A6 + MXWORK_CH.S0000)));
                L001050();
                L0011b4();
                D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c) & 0xffff;
                if ((D0 & (1 << D7)) == 0) {
                    L000c66();
                }
                A6 += MXWORK_CH.Length;
                D7++;
            } while (D7 < 0x0009);
            if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                A6 = MXWORK_CHBUF_PCM[0];

                do {
                    L001050();
                    L0011b4();
                    D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1c) & 0xffff;
                    if ((D0 & (1 << D7)) == 0) {
                        L000c66();
                    }

                    A6 += MXWORK_CH.Length;
                    D7++;
                } while (D7 < 0x0010);
            }
        }
/*IL_9C4:*/}
        L000756();

//L000c44:
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
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 0)) != 0) {
            if (mm.readByte(A6 + MXWORK_CH.S0020) == 0) {
                if (/* signed */ (byte) mm.readByte(A6 + MXWORK_CH.S0018) >= 0) {
                    L000d84(); // Tone Transmission
                    L000e66(); // Tone Transmission (PAN　FB CON)
                    if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 3)) == 0) {
                        mm.write(A6 + MXWORK_CH.S0025, mm.readByte(A6 + MXWORK_CH.S0024)); // Set the LFO delayCounter
                        if (mm.readByte(A6 + MXWORK_CH.S0025) != 0) {
                            mm.write(A6 + MXWORK_CH.S0036, Depend.CLR); // LFO Pitch
                            mm.write(A6 + MXWORK_CH.S004a, (short) Depend.CLR); // LFO Volume
                            L001094();
                        }
                        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 1)) != 0) {
                            D1 = 0x01;
                            D2 = 0x02;
                            L_WRITEOPM(); // HardLFO Reset
                            D2 = 0;
                            L_WRITEOPM();
                        }
                    }
                    mm.write(A6 + MXWORK_CH.S000c, Depend.CLR); // Bend
                    L000cdc(); // KF KC
                    L000dfe(); // Volume Sending
                }
                L000e7e(); // KEY ON
                mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xfe));
                return;
            }
            mm.write(A6 + MXWORK_CH.S0020, (byte) ((mm.readByte(A6 + MXWORK_CH.S0020) - 1) & 0xff));
        }
        if (/* signed */ (byte) mm.readByte(A6 + MXWORK_CH.S0018) >= 0) {
            L000cdc();
            L000dfe();
        }
    }

    // 
    private void L000cdc() {
        D2 = mm.readShort(A6 + MXWORK_CH.S0012) & 0xffff; // note+D
        D2 = ((D2 & 0xffff) + ((mm.readInt(A6 + MXWORK_CH.S000c) & 0xffff_0000) >>> 16)) & 0xffff; // +bend
        D2 = ((D2 & 0xffff) + ((mm.readInt(A6 + MXWORK_CH.S0036) & 0xffff_0000) >>> 16)) & 0xffff; // +LfoPitch
        if (D2 != (mm.readShort(A6 + MXWORK_CH.S0014) & 0xffff)) { // Compare if same as previous value
            mm.write(A6 + MXWORK_CH.S0014, (short) D2);
            D1 = 0x17ff;
            if (D1 < D2) {
                if ((short) D2 < 0) {
                    D2 = 0;
                } else {
                    D2 = D1 & 0xffff;
                }
            }
            D2 = (D2 * 4) & 0xffff;
            D1 = 0x30;
            D1 += (mm.readByte(A6 + MXWORK_CH.S0018) & 0xff);
            L_WRITEOPM(); // KF
            D1 -= 8;
            D2 >>>= 8;
            D2 = KeyCode[D2];
            L_WRITEOPM(); // KC(OCT+NOTE)
        }
    }

    // 
    private void L000d84() {
        boolean c0 = (mm.readByte(A6 + MXWORK_CH.S0017) & (1 << 1)) != 0;
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & ~(1 << 1)));
        if (c0) {
            A0 = mm.readInt(A6 + MXWORK_CH.S0004);
            if (A0 == 0) A0 = FAKEA6S0004 + 0;
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) & 0xc0));
            D0 = mm.readByte(A0) & 0xff;
            A0++;
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) | D0));
            D0 &= 0x0007;
            D3 = CarrierSlot[D0];
            mm.write(A6 + MXWORK_CH.S0019, (byte) D3);
            D0 = mm.readByte(A0) & 0xff;
            A0++;
            D0 <<= 3;
            D0 |= mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
            mm.write(A6 + MXWORK_CH.S001d, (byte) D0);
            D1 = 0x40;
            D1 += mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
            D0 = 0x03;

            do {
                D2 = mm.readByte(A0) & 0xff;
                A0++;
                L_WRITEOPM();
                D1 += 8;
            } while (D0-- != 0);
            D0 = 0x03;

            do {
                D2 = mm.readByte(A0) & 0xff;
                A0++;
                c0 = (D3 & 1) != 0;
                D3 >>>= 1;
                if (c0) {
                    D2 = 0x7f;
                }
                L_WRITEOPM();
                D1 += 8;
            } while (D0-- != 0);
            D0 = 0x0f;

            do {
                D2 = mm.readByte(A0) & 0xff;
                A0++;
                L_WRITEOPM();
                D1 += 8;
            } while (D0-- != 0);
            mm.write(A6 + MXWORK_CH.S0023, (byte) Depend.SET);
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x64));
        }
    }

    // 
    private void L000dfe() {
        D0 = 0x00;
        D0 = mm.readByte(A6 + MXWORK_CH.S0022) & 0xff;
        boolean c0 = (D0 & (1 << 7)) != 0;
        D0 &= 0xff7f; // (~(1 << 7));
        if (!c0) {
            D0 = Volume[D0];
        }
        D0 += mm.readByte(G + MXWORK_GLOBAL.L001e14) & 0xff;
        if (D0 > 0xff || /* signed */ (byte) D0 < 0) {
            D0 = 0x7f;
        }
        D0 += ((mm.readShort(A6 + MXWORK_CH.S004a) & 0xff00) >>> 8);
        if (D0 > 0xff || /* signed */ (byte) D0 < 0) {
            D0 = 0x7f;
        }
        if (mm.readByte(A6 + MXWORK_CH.S0023) != (byte) D0) {
            L000e28();
        }
    }

    private void L000e28() {
        mm.write(A6 + MXWORK_CH.S0023, (byte) D0);
        A0 = mm.readInt(A6 + MXWORK_CH.S0004);
        if (A0 == 0) {
            A0 = FAKEA6S0004 + 0;
        }
        A0 += 6; // Move to TL position
        D3 = mm.readByte(A6 + MXWORK_CH.S0019) & 0xff;
        D1 = 0x60;
        D1 += mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
        D4 = 0x03;

        do {
            D2 = mm.readByte(A0++) & 0xff;
            boolean c0 = (D3 & 1) != 0;
            D3 >>>= 1;
            if (c0) {
                D2 += D0;
                if (/* signed */ (byte) D2 < 0) {
                    D2 = 0x7f;
                }
                L_WRITEOPM();
            }
            D1 += 8;
        } while (D4-- != 0);
    }

    // 
    private void L000e66() {
        boolean c0 = (mm.readByte(A6 + MXWORK_CH.S0017) & (1 << 2)) != 0;
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & ~(1 << 2)));
        if (c0) {
            D2 = mm.readByte(A6 + MXWORK_CH.S001c) & 0xff;
            D1 = 0x20;
            D1 += mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
            L_WRITEOPM();
        }
    }

    // 
    private void L000e7e() {
        boolean c0 = (mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 3)) != 0;
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | (1 << 3)));
        if (c0) {
            return;
        }
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 4)) != 0) {
            L000ff6();
        }
        if (/* signed */ (byte) mm.readByte(A6 + MXWORK_CH.S0018) >= 0) {
            D2 = mm.readByte(A6 + MXWORK_CH.S001d) & 0xff;
            A2 = G + MXWORK_GLOBAL.L00223c;
            mm.write(A2 + D7, (byte) D2);
            A2 = G + MXWORK_GLOBAL.L001bb4;
            mm.write(A2 + D7, (byte) D2);
            D1 = 0x08;
            L_WRITEOPM();
            return;
        }
        D0 = mm.readByte(G + MXWORK_GLOBAL.L002231) & 0xff;
        if (D0 == 0 || mm.readByte(G + MXWORK_GLOBAL.L001e09) != 0) {
            return;
        }

        D0 = 0x00;
        D0 = mm.readShort(A6 + MXWORK_CH.S0012) & 0xffff;
        D0 >>>= 6;
        D2 = mm.readByte(A6 + MXWORK_CH.S001c) & 0xff;
        D1 = D2;
        D1 &= 0x0003;
        if (D1 == 0 || D1 == 0x0003) {
                D1 ^= 0x0003;
        }
        D2 &= 0x001c;
        D2 <<= 6;
        D2 |= D1;
        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) == 0) {
            if (mm.readByte(G + MXWORK_GLOBAL.L001e15) != 0) {
                D2 &= 0xfc;
            }
            D0 <<= 3;
            A1 = mm.readInt(G + MXWORK_GLOBAL.L00222c);
            A0 = A1 + D0;
            A1 += Depend.getBLong(mm, A0);
            A0 += 4;
            A0 += 2;
            D3 = Depend.getBWord(mm, A0) & 0xffff;
            A0 += 2;
            if (D3 != 0x0000) {
                ADPCMMOD_END();
                D1 = D2;
                D2 = D3;
                if (D2 > 0xff00) D2 = 0xff00;  // DMA Size Limit
                ADPCMOUT();
                A2 = G + MXWORK_GLOBAL.L00223c + 0;
                mm.write(A2 + D7, (byte) Depend.CLR);
                A2 = G + MXWORK_GLOBAL.L001bb4 + 0;
                mm.write(A2 + D7, (byte) Depend.CLR);
            }
        } else {
            D1 = 0x00;
            D1 = mm.readByte(A6 + MXWORK_CH.S0004_b) & 0xff;
            D1 <<= 5;
            D0 += D1;
            D1 += D1;
            D0 += D1;
            D0 <<= 3;
            A1 = mm.readInt(G + MXWORK_GLOBAL.L00222c);
            A0 = A1 + D0;
            D3 = Depend.getBLong(mm, A0 + 4);
            if (D3 != 0) {
                A1 += Depend.getBLong(mm, A0);
                D0 = (D0 & 0xffff_ff00) + (mm.readByte(A6 + MXWORK_CH.S0018) & 0xff);
                D0 &= 0xffff_0007;
                D1 = 0x00;
                D1 = mm.readByte(A6 + MXWORK_CH.S0022) & 0xff;
                c0 = (D1 & (1 << 7)) != 0;
                D1 &= 0xffff_ff7f; // (~(1 << 7));
                if (!c0) {
                    // A2 = Volume[0];
                    // D1 = A2[D1];
                    D1 = Volume[D1];
                }
                D1 += mm.readByte(G + MXWORK_GLOBAL.L001e14) & 0xff;
                if (/* signed */ (byte) D1 < 0 || /* signed */ D1 >= 0x2b) {
                    D1 = 0x00;
                    D2 &= 0xffff_ff00;
                } else {
                    D1 = PCMVolume[D1];
                }
                D1 <<= 16;
                D1 |= (D2 & 0xffff);
                D2 = 0x00;
                PCM8_SUB();
                D0 = (D0 & 0xffff_ff00) + (mm.readByte(A6 + MXWORK_CH.S0018) & 0xff);
                D0 &= 0xffff_0007;
                D2 = D3;
                D2 &= 0xff_ffff;
                PCM8_SUB();
                A2 = G + MXWORK_GLOBAL.L00223c + 0;
                mm.write(A2 + 0x0008, (byte) Depend.CLR);
                A2 = G + MXWORK_GLOBAL.L001bb4 + 0;
                mm.write(A2 + D7, (byte) Depend.CLR);
            }
        }
    }

    // 
    private void L000fe6() {
        boolean c0 = (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 3)) != 0;
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & ~(1 << 3)));
        if (c0 && (mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 4)) == 0) {
            L000ff6();
        }
    }

    private void L000ff6() {
        D2 = mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
        if (/* signed */ (byte) D2 >= 0) {
            D1 = 0x08;
            A2 = G + MXWORK_GLOBAL.L00223c + 0;
            mm.write(A2 + D7, (byte) D2);
            A2 = G + MXWORK_GLOBAL.L001bb4 + 0;
            mm.write(A2 + D7, (byte) D2);
            L_WRITEOPM();
            return;
        }

        D0 = mm.readByte(G + MXWORK_GLOBAL.L002231) & 0xff;
        if (D0 == 0) {
            return;
        }
        if (mm.readByte(G + MXWORK_GLOBAL.L001e09) != 0) {
            return;
        }
        if (mm.readByte(G + MXWORK_GLOBAL.L001df4) == 0) {
            if (mm.readByte(A6 + MXWORK_CH.S0017) == 0) {
                ADPCMMOD_STOP();
            }
            ADPCMMOD_END();
        } else {
            D0 = mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
            D0 &= 0x0007;
            D1 = 0x00;
            D1 = mm.readByte(A6 + MXWORK_CH.S0022) & 0xff;
            D1 <<= 16;
            D1 |= (D2 & 0xffff);
            D2 = 0;
            PCM8_SUB();
        }
    }

    // 
    private void L001050() {
        if (/* signed */ mm.readByte(A6 + MXWORK_CH.S0018) < 0) {
            return;
        }
        if (/* signed */ mm.readByte(A6 + MXWORK_CH.S0016) < 0) {
            if (mm.readByte(A6 + MXWORK_CH.S0020) == 0) {
                D0 = mm.readInt(A6 + MXWORK_CH.S0008);
                mm.write(A6 + MXWORK_CH.S000c, mm.readInt(A6 + MXWORK_CH.S000c) + D0);
            }
        }
        if (mm.readByte(A6 + MXWORK_CH.S0024) != 0) {
            if (mm.readByte(A6 + MXWORK_CH.S0020) != 0) {
                return;
            }
            if (mm.readByte(A6 + MXWORK_CH.S0025) != 0) {
                L001094();
                return;
            }
        }

//L00107c:
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 5)) != 0) {
            L0010b4();
        }
        if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 6)) != 0) {
            L001116();
        }
    }

    private void L001094() {
        mm.write(A6 + MXWORK_CH.S0025, (byte) ((mm.readByte(A6 + MXWORK_CH.S0025) - 1) & 0xff));
        if (mm.readByte(A6 + MXWORK_CH.S0025) == 0) {
            if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 5)) != 0) {
                mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003a));
                mm.write(A6 + MXWORK_CH.S0032, mm.readInt(A6 + MXWORK_CH.S002e));
                mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S002a));
            }
            if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 6)) != 0) {
                L0015d0();
            }
        }
    }

    // 
    private void L0010b4() {
        D1 = mm.readInt(A6 + MXWORK_CH.S0032);
        A0 = mm.readInt(A6 + MXWORK_CH.S0026);
        if (A0 < 0x05) {
            L0010b4_Table[A0].run();
        }
        MX_ABORT();
    }

    // 
    private void L0010be() {
        mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S0036) + D1);
        mm.write(A6 + MXWORK_CH.S003e, (short) ((mm.readShort(A6 + MXWORK_CH.S003e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) {
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
            mm.write(A6 + MXWORK_CH.S0036, -mm.readInt(A6 + MXWORK_CH.S0036));
        }
    }

    // 
    private void L0010d4() {
        mm.write(A6 + MXWORK_CH.S0036, D1);
        mm.write(A6 + MXWORK_CH.S003e, (short) ((mm.readShort(A6 + MXWORK_CH.S003e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) {
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
            mm.write(A6 + MXWORK_CH.S0032, -mm.readInt(A6 + MXWORK_CH.S0032));
        }
    }

    // 
    private void L0010ea() {
        mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S0036) + D1);
        mm.write(A6 + MXWORK_CH.S003e, (short) ((mm.readShort(A6 + MXWORK_CH.S003e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) {
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
            mm.write(A6 + MXWORK_CH.S0032, -mm.readInt(A6 + MXWORK_CH.S0032));
        }
    }

    // 
    private void L001100() {
        mm.write(A6 + MXWORK_CH.S003e, (short) ((mm.readShort(A6 + MXWORK_CH.S003e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S003e) == 0) {
            L00117a();
            D0 = (short) D0 * (short) D1;
            mm.write(A6 + MXWORK_CH.S0036, D0);
            mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003c));
        }
    }

    // 
    private void L001116() {
        D1 = mm.readShort(A6 + MXWORK_CH.S0048) & 0xffff;
        A0 = mm.readInt(A6 + MXWORK_CH.S0040);
        if (A0 < 0x05) {
            L001116Table[A0].run();
        }
        MX_ABORT();
    }

    // 
    private void L001120() {
        mm.write(A6 + MXWORK_CH.S004a, (short) ((mm.readShort(A6 + MXWORK_CH.S004a) + (D1 & 0xffff)) & 0xffff));
        mm.write(A6 + MXWORK_CH.S004e, (short) ((mm.readShort(A6 + MXWORK_CH.S004e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) {
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S004a, mm.readShort(A6 + MXWORK_CH.S0046));
        }
    }

    // 
    private void L001138() {
        mm.write(A6 + MXWORK_CH.S004e, (short) ((mm.readShort(A6 + MXWORK_CH.S004e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) {
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S004a, (short) ((mm.readShort(A6 + MXWORK_CH.S004a) + (D1 & 0xffff)) & 0xffff));
            mm.write(A6 + MXWORK_CH.S0048, (short) (-mm.readShort(A6 + MXWORK_CH.S0048)));
        }
    }

    // 
    private void L00114e() {
        mm.write(A6 + MXWORK_CH.S004a, (short) ((mm.readShort(A6 + MXWORK_CH.S004a) + (D1 & 0xffff)) & 0xffff));
        mm.write(A6 + MXWORK_CH.S004e, (short) ((mm.readShort(A6 + MXWORK_CH.S004e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) {
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S0048, (short) (-mm.readShort(A6 + MXWORK_CH.S0048)));
        }
    }

    // 
    private void L001164() {
        mm.write(A6 + MXWORK_CH.S004e, (short) ((mm.readShort(A6 + MXWORK_CH.S004e) - 1) & 0xffff));
        if (mm.readShort(A6 + MXWORK_CH.S004e) == 0) {
            L00117a();
            D1 = (short) D1 * (short) D0;
            mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
            mm.write(A6 + MXWORK_CH.S004a, (short) D1);
        }
    }

    short L001190 = 0x1234;

    //
    private void L00117a() {
        D0 = L001190;
        D0 *= 0xc549;
        D0 += 0x0c;
        L001190 = (short) D0;
        D0 >>>= 8;
    }

    // 
    private void L001192() {
        A0 = G + MXWORK_GLOBAL.L001df6 + 0;
        if (mm.readByte(A0 + D7) != 0) {
            mm.write(A0 + D7, (byte) Depend.CLR);
            if (D7 < 0x09) {
                mm.write(G + MXWORK_GLOBAL.L002233 + D7, (byte) Depend.CLR);
            }
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & 0xf7));
            L0011d4();
        }
    }

    // 
    private void L0011b4() {
        if ((mm.readByte(A6 + MXWORK_CH.S0017) & (1 << 3)) != 0) {
            L001192();
        } else if ((mm.readByte(A6 + MXWORK_CH.S0016) & (1 << 2)) != 0) {
            L0011ce();
        } else {
            mm.write(A6 + MXWORK_CH.S001b, (byte) ((mm.readByte(A6 + MXWORK_CH.S001b) - 1) & 0xff));
            if (mm.readByte(A6 + MXWORK_CH.S001b) != 0) {
                L0011ce();
            } else {
                L000fe6();
                L0011ce();
            }
        }
    }

    // 
    private void L0011ce() {
        mm.write(A6 + MXWORK_CH.S001a, (byte) ((mm.readByte(A6 + MXWORK_CH.S001a) - 1) & 0xff));
        if (mm.readByte(A6 + MXWORK_CH.S001a) != 0) return;
        L0011d4();
    }

    // 
    private void L0011d4() {
        A4 = mm.readInt(A6 + MXWORK_CH.S0000);
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0x7b));

exit:   {
            while (true) {
//L0011dc:
                D0 = 0x00;
                D1 = 0x00;
                D0 = mm.readByte(A4++) & 0xff;
                D1 = D0;
                if (/* signed */ (byte) D1 >= 0) { // goto L001216;
                    break exit;
                }
                if (D0 < 0xe0) { // goto L00122e;
                    break;
                }

//L00122e:
                D0 ^= 0xff;
                DisposeStack_L00122e = Depend.FALSE;
                L001252[D0 & 0xff].run();
                if (DisposeStack_L00122e != Depend.FALSE) {
                    return;
                }
//                goto L0011dc;
            }
            D0 &= 0x007f;
            D0 <<= 6;
            D0 += 5;
            D0 += mm.readShort(A6 + MXWORK_CH.S0010) & 0xffff;
            mm.write(A6 + MXWORK_CH.S0012, (short) D0);
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x01));
            mm.write(A6 + MXWORK_CH.S0020, mm.readByte(A6 + MXWORK_CH.S001f));
            D0 = 0x00;
            D0 = mm.readByte(A4++) & 0xff;
            D1 = mm.readByte(A6 + MXWORK_CH.S001e) & 0xff;
            if ((byte) D1 >= 0) { // goto L001226;
                D1 *= D0;
                D1 = (D1 & 0xffff) >>> 3;
                break exit; // L001216
            }
//L001226:
            D1 &= 0xff;
            D1 += (D0 & 0xff);
            if (D1 < 0x100) { // goto L001216;
                D1 = 0x00;
            }
            // goto L001216;
        }

//L001216:
        D1++;
        mm.write(A6 + MXWORK_CH.S001b, (byte) D1);
        D0++;
        mm.write(A6 + MXWORK_CH.S001a, (byte) D0);
        mm.write(A6 + MXWORK_CH.S0000, A4);
//L001224:
//        return;
    }

    // @@ @t
    private void L001292() {
        D1 = 0x12;
        D2 = mm.readByte(A4++) & 0xff;
        mm.write(G + MXWORK_GLOBAL.L001e0c, (byte) D2);
        mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
        if (mm.readByte(G + MXWORK_GLOBAL.L001e08) == 0) {
            L_WRITEOPM();
        }
    }

    // 
    private void L0012a6() {
        D1 = mm.readByte(A4++) & 0xff;
        D2 = mm.readByte(A4++) & 0xff;
        if (D1 == 0x12) {
            if (mm.readByte(G + MXWORK_GLOBAL.L001e08) != 0) return;
            mm.write(G + MXWORK_GLOBAL.L001e0c, (byte) D2);
            mm.write(G + MXWORK_GLOBAL.MUSICTIMER, (byte) D2);
        }
        L_WRITEOPM();
    }

    // @@ @
    private void L0012be() {
        if (/* signed */ mm.readByte(A6 + MXWORK_CH.S0018) >= 0) {
            D0 = mm.readByte(A4++) & 0xff;
            A0 = mm.readInt(G + MXWORK_GLOBAL.L002228);
            // checker
            while (A0 < mm.readInt(G + MXWORK_GLOBAL.L001e34) + mm.readInt(G + MXWORK_GLOBAL.L002220)) {
                if (mm.readByte(A0++) == (byte) D0) {
                    mm.write(A6 + MXWORK_CH.S0004, A0);
                    mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x02));
                    break;
                }
                A0 += 0x1a;
            }
            // checker end
        } else {
            mm.write(A6 + MXWORK_CH.S0004_b, mm.readByte(A4++));
        }
    }

    // @@ p
    private void L0012e6() {
        if (/* signed */ mm.readByte(A6 + MXWORK_CH.S0018) >= 0) {
            D0 = mm.readByte(A6 + MXWORK_CH.S001c) & 0xff;
            D0 &= 0x3f;
            D0 |= ((mm.readByte(A4++) & 0xff) << 6) & 0xffff;
            mm.write(A6 + MXWORK_CH.S001c, (byte) D0);
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x04));
        } else {
            D0 = mm.readByte(A4++) & 0xff;
            if (D0 == 0 || D0 == 0x03) {
                D0 ^= 0x03;
            }
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) & 0xfc));
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) | D0));
        }
    }

    // @@ v volume(0xFB)
    private void L00131c() {
        mm.write(A6 + MXWORK_CH.S0022, mm.readByte(A4++));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
    }

    // 
    private void L001328() {
        D2 = mm.readByte(A6 + MXWORK_CH.S0022) & 0xff;
        if (/* signed */ (byte) D2 >= 0) {
            if (D2 != 0) {
                mm.write(A6 + MXWORK_CH.S0022, (byte) ((mm.readByte(A6 + MXWORK_CH.S0022) - 1) & 0xff));
                mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
            }
        } else {
            if (D2 != (byte) 0xff) {
                mm.write(A6 + MXWORK_CH.S0022, (byte) ((mm.readByte(A6 + MXWORK_CH.S0022) + 1) & 0xff));
                mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
            }
        }
    }

    // 
    private void L001330() {
        mm.write(A6 + MXWORK_CH.S0022, (byte) ((mm.readByte(A6 + MXWORK_CH.S0022) - 1) & 0xff));
        mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x17)); // 01?
    }

    // 
    private void L001344() {
        D2 = mm.readByte(A6 + MXWORK_CH.S0022) & 0xff;
        if (/* signed */ (byte) D2 >= 0) {
            if (D2 != 0x0f) {
                mm.write(A6 + MXWORK_CH.S0022, (byte) ((mm.readByte(A6 + MXWORK_CH.S0022) + 1) & 0xff));
                mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x01));
            }
        } else {
            if ((D2 & 0xff) != 0x80) {
                L001330();
            }
        }
    }

    // 
    private void L001364() {
        mm.write(A6 + MXWORK_CH.S001e, mm.readByte(A4++));
    }

    // 
    private void L00136a() {
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x04));
    }

    // 
    private void L001372() {
        byte t0;
        t0 = mm.readByte(A4++);
        mm.write(A4++, t0);
    }

    // 
    private void L001376() {
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        D0 = (D0 ^ 0xffff) + 1;
        mm.write(A4 - D0 - 1, (byte) ((mm.readByte(A4 - D0 - 1) - 1) & 0xff));
        if (mm.readByte(A4 - D0 - 1) != 0) {
            if (mm.readByte(G + MXWORK_GLOBAL.L001e0b) != 0 &&
                mm.readByte(A4) == (byte) 0xf1 &&
                mm.readByte(A4 + 1) == 0) {
                        L0013e6();
            } else {
                // checker
                if ((A4 - D0) < (G + MXWORK_GLOBAL.L001e34)) {
                    mm.write(G + MXWORK_GLOBAL.FATALERROR, 0x001396);
                    mm.write(G + MXWORK_GLOBAL.FATALERRORADR, A4);
                // checker end
                } else {
                    A4 -= D0;
                }
            }
        }
    }

    // 
    private void L00139a() {
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        A0 = A4 + D0;
        D0 = 0xffff_ffff;
        D0 = Depend.getBWord(mm, A0) & 0xffff;
        A0 += 2;
        D0 = (D0 ^ 0xffff) + 1;
        if (mm.readByte(A0 - D0 - 1) == 0x01) {
            A4 = A0;
        }
    }

    // @@ D
    private void L0013ba() {
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        mm.write(A6 + MXWORK_CH.S0010, (short) D0);
    }

    // 
    private void L0013c6() {
        D0 = 0;
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        D0 = (short) D0;
        D0 <<= 8;
        mm.write(A6 + MXWORK_CH.S0008, D0);
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x80));
    }

    // 
    private void L0013dc() {
        if (mm.readByte(A4++) == 0) {
            L001440();
            return;
        }
        A4--;
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        D0 = (D0 ^ 0xffff) + 1;
        L0013e6();
    }

    // 
    private void L0013e6() {
        // checker
        if (((A4 - D0) & 0xffff_ffffL) < ((G + MXWORK_GLOBAL.L001e34) & 0xffff_ffffL)) {
            mm.write(G + MXWORK_GLOBAL.FATALERROR, 0x0013e6);
            mm.write(G + MXWORK_GLOBAL.FATALERRORADR, A4);
            return;
        }
        // checker end

        A4 -= D0;
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1a) & 0xffff;
        D0 &= ~(1 << D7);
        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) D0);
        D0 &= (mm.readShort(G + MXWORK_GLOBAL.L001e06) & 0xffff);
        if (D0 == 0) {
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) == 0) {
                mm.write(G + MXWORK_GLOBAL.L001e1a, (short) 0x01ff);
                if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                    mm.write(G + MXWORK_GLOBAL.L001e1a, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e1a) | 0xfe00));
                }
                mm.write(G + MXWORK_GLOBAL.L002246, (short) ((mm.readShort(G + MXWORK_GLOBAL.L002246) + 1) & 0xffff));
            } else if (mm.readByte(G + MXWORK_GLOBAL.L001e17) == 0) {
                mm.write(G + MXWORK_GLOBAL.L001e1a, (short) 0x01ff);
                if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                    mm.write(G + MXWORK_GLOBAL.L001e1a, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e1a) | 0xfe00));
                }
                mm.write(G + MXWORK_GLOBAL.L001e22, (short) ((mm.readShort(G + MXWORK_GLOBAL.L001e22) - 1) & 0xffff));
                if (mm.readShort(G + MXWORK_GLOBAL.L001e22) == 0) {
                    mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) (0x0011));
                    mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
                }
            }
        }
    }

    // 
    private void L001440() {
        L001442();
    }

    // 
    private void L001442() {
        A4 = L0019b2;
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e1a) & 0xffff;
        D0 &= ~(1 << D7);
        mm.write(G + MXWORK_GLOBAL.L001e1a, (short) D0);
        D0 = mm.readShort(G + MXWORK_GLOBAL.L001e06) & 0xffff;
        D0 &= ~(1 << D7);
        mm.write(G + MXWORK_GLOBAL.L001e06, (short) D0);
        if (D0 == 0) {
            mm.write(G + MXWORK_GLOBAL.L001e13, (byte) 0x01);
            if (mm.readByte(G + MXWORK_GLOBAL.L001df4) != 0) {
                D0 = 0x01ff;
                PCM8_SUB();
                mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.CLR);
            }
            if (mm.readByte(G + MXWORK_GLOBAL.L001e18) == 0) {
                mm.write(G + MXWORK_GLOBAL.L002246, (short) 0xffff);
            } else {
                mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) 0xffff);
                mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
                mm.write(G + MXWORK_GLOBAL.L001e14, (byte) 0x00);
                mm.write(G + MXWORK_GLOBAL.L001e15, (byte) 0x37);
            }
        }
    }

    // 
    private void L001492() {
        mm.write(A6 + MXWORK_CH.S001f, mm.readByte(A4++));
    }

    // 
    private void L001498() {
        D0 = mm.readByte(A4++) & 0xff;
        A0 = mm.readByte(G + MXWORK_GLOBAL.L001df6 + 0) & 0xff;
        mm.write(A0 + D0, (byte) Depend.SET);
        if (D0 < 0x0009) {
            mm.write(G + MXWORK_GLOBAL.L002233 + D7, (byte) Depend.SET);
        }
    }

    // 
    private void L0014b0() {
        A0 = G + MXWORK_GLOBAL.L001df6 + 0;
        if (mm.readByte(A0 + D7) != 0) {
            mm.write(A0 + D7, (byte) Depend.CLR);
            if (D7 < 0x0009) {
                mm.write(G + MXWORK_GLOBAL.L002233 + D7, (byte) Depend.CLR);
            }
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & 0xf7));
        } else {
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x08));
            mm.write(A6 + MXWORK_CH.S0000, A4);
            DisposeStack_L00122e = Depend.TRUE;
        }
    }

    // 
    private void L0014dc() {
        D2 = mm.readByte(A4++) & 0xff;
        if (/* signed */ mm.readByte(A6 + MXWORK_CH.S0018) >= 0x00) {
            mm.write(G + MXWORK_GLOBAL.L002232, (byte) D2);
            D1 = 0x0f;
            L_WRITEOPM();
        } else {
            D2 <<= 2;
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) & 0x03));
            mm.write(A6 + MXWORK_CH.S001c, (byte) (mm.readByte(A6 + MXWORK_CH.S001c) | D2));
        }
    }

    // 
    private void L0014fc() {
        int d1;
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x20));
        D1 = mm.readByte(A4++) & 0xff;
        if (/* signed */ (byte) D1 >= 0) {
            d1 = D1;
            D1 &= 0x03;
            D1 += D1;
            A0 = ((D1 / 2) + 1);
            mm.write(A6 + MXWORK_CH.S0026, A0);
            D2 = Depend.getBWord(mm, A4) & 0xffff;
            A4 += 2;
            mm.write(A6 + MXWORK_CH.S003c, (short) D2);
            if (D1 != 0x02) {
                D2 >>>= 1;
                if (D1 == 0x06) {
                    D2 = 0x01;
                }
            }
            mm.write(A6 + MXWORK_CH.S003a, (short) D2);
            D0 = Depend.getBWord(mm, A4) & 0xffff;
            A4 += 2;
            D0 = (short) D0;
            D0 = D0 << 8;
            D1 = d1;
            if (D1 >= 0x04) {
                D0 = D0 << 8;
                D1 &= 0x03;
            }
            mm.write(A6 + MXWORK_CH.S002e, D0);
            if (D1 != 0x02) {
                D0 = 0;
            }
            mm.write(A6 + MXWORK_CH.S002a, D0);
        } else {
            D1 &= 0x01;
            if (D1 == 0) {
                mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xdf));
                mm.write(A6 + MXWORK_CH.S0036, Depend.CLR);
                return;
            }
        }
        mm.write(A6 + MXWORK_CH.S003e, mm.readShort(A6 + MXWORK_CH.S003a));
        mm.write(A6 + MXWORK_CH.S0032, mm.readInt(A6 + MXWORK_CH.S002e));
        mm.write(A6 + MXWORK_CH.S0036, mm.readInt(A6 + MXWORK_CH.S002a));
    }

    // 
    private void L001590() {
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x40));
        D2 = mm.readByte(A4++) & 0xff;
        if (/* signed */ (byte) D2 < 0) {
            L0015e4();
            return;
        }
        D2 += D2;
        A0 = ((D2 / 2) + 1);
        mm.write(A6 + MXWORK_CH.S0040, A0);
        D1 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        mm.write(A6 + MXWORK_CH.S004c, (short) D1);
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        mm.write(A6 + MXWORK_CH.S0044, (short) D0);
        if ((D2 & (1 << 1)) == 0) {
            D0 = D0 * (short) D1;
        }

        D0 = -((short) D0);
        if ((short) D0 < 0) {
            D0 = 0;
        }

        mm.write(A6 + MXWORK_CH.S0046, (short) D0);

        L0015d0();
    }

    // 
    private void L0015d0() {
        mm.write(A6 + MXWORK_CH.S004e, mm.readShort(A6 + MXWORK_CH.S004c));
        mm.write(A6 + MXWORK_CH.S0048, mm.readShort(A6 + MXWORK_CH.S0044));
        mm.write(A6 + MXWORK_CH.S004a, mm.readShort(A6 + MXWORK_CH.S0046));
    }

    // 
    private void L0015e4() {
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
        D2 = mm.readByte(A4++) & 0xff;
        if (/* signed */ (byte) D2 >= 0) {
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xfd));
            boolean c0 = (D2 & (1 << 6)) != 0;
            D2 &= 0xffff_ffbf; // ~(1 << 6);
            if (c0) {
                mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x02));
            }

            D0 = opmReg1B;
            D0 &= 0xc0;
            D2 |= D0;
            D1 = 0x1b;
            L_WRITEOPM();
            D1 = 0x18;
            D2 = mm.readByte(A4++) & 0xff;
            L_WRITEOPM();
            D1 = 0x19;
            D2 = mm.readByte(A4++) & 0xff;
            L_WRITEOPM();
            D2 = mm.readByte(A4++) & 0xff;
            L_WRITEOPM();
            D2 = mm.readByte(A4++) & 0xff;
            mm.write(A6 + MXWORK_CH.S0021, (byte) D2);
        } else {
            D2 &= 0x01;
            if (D2 != 0) {
                D2 = mm.readByte(A6 + MXWORK_CH.S0021) & 0xff;
            }
        }
        D1 = 0x38;
        D1 += mm.readByte(A6 + MXWORK_CH.S0018) & 0xff;
        L_WRITEOPM();
    }

    // 
    private void L001656() {
        mm.write(A6 + MXWORK_CH.S0024, mm.readByte(A4++));
    }

    // 
    private void L00165c() {
        if (PCM8 != 0) {
            mm.write(G + MXWORK_GLOBAL.L001df4, (byte) Depend.SET);
            D0 = 0x01fe;
            PCM8_SUB();
            mm.write(G + MXWORK_GLOBAL.L001e1a, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e1a) | 0xfe00));
            mm.write(G + MXWORK_GLOBAL.L001e06, (short) (mm.readShort(G + MXWORK_GLOBAL.L001e06) | 0xfe00));
        }
    }

    // 
    private void L001694() {
        D0 = mm.readByte(A4++) & 0xff;
        if (D0 > 7) {
            L001442();
            return;
        }
        L0016aa[D0].run();
    }

    // 
    private void L0016b8() {
        D0 = mm.readByte(A4++) & 0xff;
        mm.write(G + MXWORK_GLOBAL.L001e1e + 0, (short) D0);
        mm.write(G + MXWORK_GLOBAL.L001e17, (byte) Depend.SET);
    }

    // 
    private void L0016c6() {
        if (PCM8 == 0) {
            A4 += 6;
            return;
        }
        D0 = Depend.getBWord(mm, A4) & 0xffff;
        A4 += 2;
        D1 = Depend.getBLong(mm, A4);
        A4 += 4;
        PCM8_SUB();
    }

    // 
    private void L0016fa() {
        if (mm.readByte(A4++) != 0) {
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x10));
        } else {
            mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xef));
        }
    }

    // 
    private void L00170e() {
        D0 = mm.readByte(A4++) & 0xff;
        int a6 = A6;
        int d7 = D7;
        D7 = D0;
        if (D0 < 0x09) {
            A6 = MXWORK_CHBUF_FM[D0];
        } else {
            A6 = MXWORK_CHBUF_PCM[D0 - 9];
        }

        int a6s0000 = A6 + MXWORK_CH.S0000;
        mm.write(A6 + MXWORK_CH.S0016, (short) (mm.readByte(A6 + MXWORK_CH.S0016) & 0x7b));
        D0 = 0;
        D1 = 0;
        D0 = mm.readByte(A4++) & 0xff;
        D1 = D0;

exit:   {
            if (/* signed */ (byte) D0 < 0) {
                if (D0 < 0xe0) {
                    D0 &= 0x007f;
                    D0 <<= 6;
                    D0 += 0x05;
                    D0 += mm.readShort(A6 + MXWORK_CH.S0010) & 0xffff;
                    mm.write(A6 + MXWORK_CH.S0012, (short) D0);
                    mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) | 0x01));
                    mm.write(A6 + MXWORK_CH.S0020, mm.readByte(A6 + MXWORK_CH.S001f));
                    D0 = mm.readByte(A4++) & 0xff;
                    D1 = mm.readByte(A6 + MXWORK_CH.S001e) & 0xff;
                    if (/* signed */ (byte) D1 >= 0) {
                        D1 = (D1 * (D0 & 0xffff)) & 0xffff;
                        D1 >>>= 3;
                    } else {
                        int d1 = D1 & 0xff;
                        d1 += D0 & 0xff;
                        if (d1 >= 0x100) {
                            D1 = (D1 & 0xffff_ff00) | (d1 & 0x0000_00ff);
                        } else {
                            D1 = 0x00;
                        }
                    }
                } else {
                    D0 ^= 0xff;
                    DisposeStack_L00122e = Depend.FALSE;
                    L001252[D0 & 0xff].run();
                    if (DisposeStack_L00122e == Depend.TRUE) {
                        return;
                    }
                    break exit;
                }
            }
            D1++;
            mm.write(A6 + MXWORK_CH.S001b, (byte) D1);
            D0++;
            mm.write(A6 + MXWORK_CH.S001a, (byte) D0);
        }
        mm.write(A6 + MXWORK_CH.S0000, a6s0000);
        D7 = d7;
        A6 = a6;
    }

    // 
    private void L001216() {
        D1++;
        mm.write(A6 + MXWORK_CH.S001b, (byte) D1);
        D0++;
        mm.write(A6 + MXWORK_CH.S001a, (byte) D0);
        mm.write(A6 + MXWORK_CH.S0000, A4);
    }

    private void L00178a() {
        D0 = mm.readByte(A4++) & 0xff;
        D1 = D0;
        L001216();
        mm.write(A6 + MXWORK_CH.S0016, (byte) (mm.readByte(A6 + MXWORK_CH.S0016) & 0xfe));
        L000e7e();
        DisposeStack_L00122e = Depend.TRUE;
    }

    // 
    private void L0017a0() {
        if (mm.readByte(A4++) != 0) {
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) | 0x80));
        } else {
            mm.write(A6 + MXWORK_CH.S0017, (byte) (mm.readByte(A6 + MXWORK_CH.S0017) & 0x7f));
        }
    }

    // 
    private void L_WRITEOPM() {
        OPM_SUB();
        D1 &= 0xff;
        mm.write(OPMBUF + D1, (byte) D2);
        if (D1 != 0x1b) {
            return;
        }
        opmReg1B = (byte) D2;
    }

    // 
    private int initialize(
            int mdxbuf,
            int pdxbuf,
            int memInd
    ) {
        mm.write(G + MXWORK_GLOBAL.L002220, mdxbuf != 0 ? mdxbuf : 0x1_0000);
        mm.write(G + MXWORK_GLOBAL.L002224, pdxbuf != 0 ? pdxbuf : 0x10_0000);
        mm.write(G + MXWORK_GLOBAL.L001ba8, 0x600);

        // mdx
        mm.write(G + MXWORK_GLOBAL.L001e34, memInd);
        memInd += mm.readInt(G + MXWORK_GLOBAL.L002220);
        mm.realloc(memInd);
        if (mm.readInt(G + MXWORK_GLOBAL.L001e34) == 0) {
            return 0xffff_ffff;
        }
        // pdx
        mm.write(G + MXWORK_GLOBAL.L001e38, memInd);
        memInd += mm.readInt(G + MXWORK_GLOBAL.L002224);
        mm.realloc(memInd);
        if (mm.readInt(G + MXWORK_GLOBAL.L001e38) == 0) {
            mm.write(G + MXWORK_GLOBAL.L001e34, 0);
            return 0xffff_ffff;
        }
        mm.write(G + MXWORK_GLOBAL.L001bac, memInd);
        memInd += mm.readInt(G + MXWORK_GLOBAL.L001ba8);
        mm.realloc(memInd);
        if (mm.readInt(G + MXWORK_GLOBAL.L001bac) == 0) {
            mm.write(G + MXWORK_GLOBAL.L001e34, 0);
            mm.write(G + MXWORK_GLOBAL.L001e38, 0);
            return 0xffff_ffff;
        }

        return 0;
    }

    // TODO separate from implementation

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        plugin.audio.mds.setIncFlag();
//        vstDelta = 0;
        int cnt;
        for (int i = 0; i < sampleCount; i += 2) {
            cnt = render_(buffer, offset + i, 1);
            plugin.audio.mds.update(buffer, offset + i, 2, null);
        }
        //cnt = (int)((MXDRV.MXDRV) driverVirtual).render(buffer, offset , sampleCount);
        //mds.update(buffer, offset , sampleCount, null);
        return sampleCount;
    }
}
