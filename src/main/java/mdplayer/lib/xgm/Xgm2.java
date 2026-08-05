package mdplayer.lib.xgm;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import mdplayer.lib.xgm.Xgm.XGMSampleID;
import vavi.util.ByteUtil;
import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;


/**
 * MegaDrive SGDK XGM2
 */
public class Xgm2 {

    private static final Logger logger = getLogger(Xgm2.class.getName());

    public static final int FCC_XGM2 = 0x324d4758; // "XGM2"
    public static final int FCC_GD3 = 0x20336447;  // "Gd3 "

    private double musicStep = 1; // setting.outputDevice.SampleRate / 60.0;
    public double pcmStep = 1; // setting.outputDevice.SampleRate / 14000.0;
    private double musicDownCounter = 0.0;
    private double pcmDownCounter = 0.0;
    private int fmMusicPtr = 0;
    private int psgMusicPtr = 0;
    private byte dacEnable = 0;
    private boolean ch3spEnable = false;
    private boolean isNTSC = false;
    private boolean existGD3 = false;
    private boolean multiTrack = false;
    private boolean packedData = false;

    private int sampleDataBlockSize = 0;
    private int fmDataBlockSize = 0;
    private int psgDataBlockSize = 0;
    private int sampleDataBlockAddr = 0;
    private int fmDataBlockAddr = 0;
    private int psgDataBlockAddr = 0;
    private int gd3DataBlockAddr = 0;

    private XGMSampleID[] sampleID = null;
    private int[] fmID = null;
    private int[] psgID = null;
    private int[] gd3ID = null;

    public Xgm2Pcm[] xgm2pcm = null;
    private double pcmSpeedCounter;

    private int fmWaitCnt = 0;
    private boolean endFm = false;
    private int fmLoopCnt = 0;
    private int psgWaitCnt = 0;
    private boolean endPsg = false;
    private int psgLoopCnt = 0;

    private final byte[] vd = new byte[30];
    private final byte[][][] fmTL = {
            {new byte[4], new byte[4], new byte[4]},
            {new byte[4], new byte[4], new byte[4]}
    };
    private final byte[][][] fmSLRR = {
            {new byte[4], new byte[4], new byte[4]},
            {new byte[4], new byte[4], new byte[4]}
    };
    private final byte[][] fmALG = {
            new byte[3],
            new byte[3]
    };
    private final byte[][] fmPanAmsPms = {
            new byte[3],
            new byte[3]
    };
    private final int[][][] fmFreq = {
            {new int[4], new int[4], new int[4]},
            {new int[4], new int[4], new int[4]}
    };

    private final int[] psgFreq = new int[4];
    private final int[] psgVol = new int[4];

    private int pendingFrame;
    private static final byte[] ch3FnumAdr = {(byte) 0xad, (byte) 0xae, (byte) 0xac, (byte) 0xa6}; // op1:0xad op2:0xae op3:0xac op4:0xa6
    private byte ch3KeyOn = 0;
    private boolean vi = true;

    public static class Xgm2Pcm {

        public int priority = 0;
        public int speed = 0;
        public int speedWait = 0;
        public int startAddr = 0;
        public int endAddr = 0;
        public int addr = 0;
        public int inst = 0;
        public boolean isPlaying = false;
        public byte data = 0;
    }

    public static boolean checkXGM2(byte[] buf) {
        return ByteUtil.readLeInt(buf, 0) == Xgm2.FCC_XGM2;
    }

    public Xgm2() {
        musicStep = mdplayer.Common.VGMProcSampleRate / 60.0; // setting.outputDevice.SampleRate / 60.0;
    }

    public byte[] xgmBuf;
    public Consumer<String> version;
    public BiConsumer<Boolean, Integer> tag;
    public Runnable stop;
    public IntConsumer loop;
    public TriConsumer<Integer, Integer, Integer> ym2612Write;
    public IntConsumer sn76489Write;

    public void init() {
        fmMusicPtr = fmDataBlockAddr;
        psgMusicPtr = psgDataBlockAddr;
        xgm2pcm = new Xgm2Pcm[] {new Xgm2Pcm(), new Xgm2Pcm(), new Xgm2Pcm(), new Xgm2Pcm()};

        dacEnable = 0;
        ch3spEnable = false;

        fmWaitCnt = 0;
        fmLoopCnt = 0;
        psgWaitCnt = 0;
        psgLoopCnt = 0;
        pendingFrame = 0;
        endFm = fmDataBlockSize == 0;
        endPsg = psgDataBlockSize == 0;
        vi = true;
    }

    public void clockVi() {
        if (vi) {
            writeYM2612P0(0x2b, 0x80);
            writeYM2612P0(0x2a, 0x80);
            writeYM2612P0(0x2b, 0x00);
            writeYM2612P0(0x27, 0x05);
            vi = false;
        }
    }

    public void clock(boolean stopped) {
        pcmSpeedCounter++; //= (double) Common.VGMProcSampleRate / setting.outputDevice.SampleRate * speed;
        while (pcmSpeedCounter >= 1.0 && !stopped) {
            pcmSpeedCounter -= 1.0;
            onePCMFrameMain();
        }
    }

    public void getXGM2Info(byte[] vgmBuf) {
        if (vgmBuf == null) throw new IllegalArgumentException("null buffer");

        try {
            if (ByteUtil.readLeInt(vgmBuf, 0x0000) != FCC_XGM2) throw new IllegalArgumentException("data is not xgm2");

            version.accept("%d".formatted(vgmBuf[0x0004] & 0xff));
            byte formatDesc = vgmBuf[0x0005];
            isNTSC = (formatDesc & 0x1) == 0;
            multiTrack = (formatDesc & 0x2) != 0;
            existGD3 = (formatDesc & 0x4) != 0;
            packedData = (formatDesc & 0x8) != 0;

            sampleDataBlockSize = (ByteUtil.readLeShort(vgmBuf, 0x0006) & 0xffff) * 256;
            fmDataBlockSize = (ByteUtil.readLeShort(vgmBuf, 0x0008) & 0xffff) * 256;
            psgDataBlockSize = (ByteUtil.readLeShort(vgmBuf, 0x000a) & 0xffff) * 256;

            int ptr = 0x000c;
            sampleID = new XGMSampleID[((multiTrack ? 504 : 248) - 12) / 2]; // The last 12 bytes are used for PCM SFX
            for (int i = 0; i < sampleID.length; i++) {
                sampleID[i] = new XGMSampleID();
                sampleID[i].addr = (ByteUtil.readLeShort(vgmBuf, ptr + i * 2) & 0xffff) * 256; // 0xff_ff00 is empty
                sampleID[i].size = (ByteUtil.readLeShort(vgmBuf, ptr + i * 2 + 2) & 0xffff) * 256;

                if (sampleID[i].size == 0xff_ff00) {
                    sampleID[i].size = sampleDataBlockSize - sampleID[i].addr;
                    break;
                }
                sampleID[i].size -= sampleID[i].addr;

            }

            ptr += sampleID.length * 2;
            ptr += 12;

            if (multiTrack) {
                fmID = new int[128];
                for (int i = 0; i < 128; i++) {
                    fmID[i] = (ByteUtil.readLeShort(vgmBuf, ptr) & 0xffff) * 256; // 0xff_ff00 is empty
                    ptr += 2;
                }
                psgID = new int[128];
                for (int i = 0; i < 128; i++) {
                    psgID[i] = (ByteUtil.readLeShort(vgmBuf, ptr) & 0xffff) * 256; // 0xff_ff00 is empty
                    ptr += 2;
                }
            }

            sampleDataBlockAddr = ptr;
            ptr += sampleDataBlockSize;
            fmDataBlockAddr = ptr;
            ptr += fmDataBlockSize;
            psgDataBlockAddr = ptr;
            ptr += psgDataBlockSize;

            if (multiTrack) {
                gd3ID = new int[128];
                for (int i = 0; i < 128; i++) {
                    gd3ID[i] = (ByteUtil.readLeShort(vgmBuf, ptr) & 0xffff) * 256; // 0xffff00 is empty
                    ptr += 2;
                }
            }
            gd3DataBlockAddr = ptr;

            tag.accept(existGD3, gd3DataBlockAddr);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void oneFrameMain() {
        try {
            //if (model == EnmModel.RealModel) return;

            musicStep = mdplayer.Common.VGMProcSampleRate / (isNTSC ? 60.0 : 50.0);

            if (musicDownCounter <= 0.0) {
                // process xgm
                oneFrameXGM();
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            stop.run();
        }
    }

    private void onePCMFrameMain() {
        try {
            if (pcmDownCounter <= 0.0) {
                // process pcm
                oneFramePCM();
                pcmDownCounter += pcmStep;
            }
            pcmDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFramePCM() {
        if (dacEnable == 0) return;

        int o = 0;

        for (int i = 0; i < 4; i++) {
            if (!xgm2pcm[i].isPlaying) continue;
            if (xgm2pcm[i].addr == xgmBuf.length) {
                xgm2pcm[i].isPlaying = false;
                xgm2pcm[i].data = 0;
                continue;
            }
            byte d = xgmBuf[xgm2pcm[i].addr]; // signed
            if (xgm2pcm[i].speed == 0) xgm2pcm[i].addr++;
            else {
                xgm2pcm[i].speedWait++;
                xgm2pcm[i].speedWait %= 2;
                if (xgm2pcm[i].speedWait == 0) xgm2pcm[i].addr++;
            }
            o += d;
            xgm2pcm[i].data = (byte) Math.abs(d);
            if (xgm2pcm[i].addr >= xgm2pcm[i].endAddr) {
                xgm2pcm[i].isPlaying = false;
                xgm2pcm[i].data = 0;
            }
        }
        o = (short) Math.clamp(o, Byte.MIN_VALUE + 1, Byte.MAX_VALUE);
        o += 0x80;

        ym2612Write.accept(0, 0x2a, o);
    }

    private void oneFrameXGM() {
        if (!endFm) oneFrameFM();
        if (!endPsg) oneFramePsg();
        if (endFm && endPsg) stop.run();
        loop.accept(Math.min(fmLoopCnt, psgLoopCnt));
    }

    private void oneFrameFM() {
        if (fmWaitCnt-- > 0) return;
        if (xgmBuf == null) return;
        while (true) {
            if (fmMusicPtr >= xgmBuf.length) {
                endFm = true;
                return;
            }
            byte dat = xgmBuf[fmMusicPtr++];

            int cmd = dat & 0xf0;
            byte val = (byte) (dat & 0x0f);
            int id, cs, port, keyOffOn, pan, slot;
            byte tl, adr;

            switch (cmd) {
                case 0x00: // wait
                    fmWaitCnt = val & 0xff;
                    if (fmWaitCnt == 15) fmWaitCnt = (xgmBuf[fmMusicPtr++] & 0xff) + 15;
                    return;
                case 0x10: // pcm play
                    id = xgmBuf[fmMusicPtr++] & 0xff;
                    playPCM(val, id);
                    break;
                case 0x20: // ym2612 load instrument
                    cs = val & 0x3;
                    port = (byte) ((val & 0x4) >> 2);
                    for (int i = 0; i < 30; i++) vd[i] = xgmBuf[fmMusicPtr++];
                    sendInst(cs, port, vd);
                    break;
                case 0x30: // YM2612 frequency set + key OFF/ON
                    fmFreqSetAndKeyOffOn(val);
                    break;
                case 0x40: // YM2612 key OFF/ON ($28)
                    cs = val & 0x7; // 0x28 includes port
                    keyOffOn = (val & 0x8) >> 3;
                    writeYM2612P0(0x28, ((keyOffOn != 0 ? 0xf0 : 0x00) | cs) & 0xff);
                    if (cs == 2)
                        ch3KeyOn = (byte) (keyOffOn != 0 ? 0xf0 : 0x00);
                    break;
                case 0x50: // YM2612 key sequence ($28)
                    cs = val & 0x7;
                    keyOffOn = (val & 0x8) >> 3;
                    if (keyOffOn == 0) {
                        writeYM2612P0(0x28, (0x00 | cs) & 0xff); // OFF
                        writeYM2612P0(0x28, (0xf0 | cs) & 0xff); // ON
                    } else {
                        writeYM2612P0(0x28, (0xf0 | cs) & 0xff); // ON
                        writeYM2612P0(0x28, (0x00 | cs) & 0xff); // OFF
                    }
                    if (cs == 2)
                        ch3KeyOn = (byte) (keyOffOn != 0 ? 0xf0 : 0x00);
                    break;
                case 0x60: // YM2612 port 0 panning
                    cs = val & 0x3;
                    pan = (val & 0xc) << 4;
                    fmPanAmsPms[0][cs] = (byte) ((fmPanAmsPms[0][cs] & 0x3f) | pan);
                    writeYM2612P0((0xb4 + cs) & 0xff, fmPanAmsPms[0][cs] & 0xff);
                    break;
                case 0x70: // YM2612 port 1 panning
                    cs = val & 0x3;
                    pan = (val & 0xc) << 4;
                    fmPanAmsPms[1][cs] = (byte) ((fmPanAmsPms[1][cs] & 0x3f) | pan);
                    writeYM2612P1((0xb4 + cs) & 0xff, fmPanAmsPms[1][cs] & 0xff);
                    break;
                case 0x80: // YM2612 frequency set + key OFF/ON + end of frame
                    fmFreqSetAndKeyOffOn(val);
                    return;
                case 0x90: // YM2612 TL set
                    cs = val & 0x3;
                    slot = (val & 0xc) >> 2;
                    port = xgmBuf[fmMusicPtr] & 0x1;
                    tl = (byte) ((xgmBuf[fmMusicPtr] & 0xff) >> 1);
                    fmMusicPtr += 1;
                    fmTL[port][cs][slot] = tl;
                    writeYM2612(port == 0, (0x40 + slot * 4 + cs) & 0xff, fmTL[port][cs][slot] & 0xff);
                    break;
                case 0xa0: // YM2612 frequency delta
                    fmFreqDeltaSet(val);
                    break;
                case 0xb0: // YM2612 frequency delta + end of frame
                    fmFreqDeltaSet(val);
                    return;
                case 0xc0: // YM2612 TL delta
                    fmTlDeltaSet(val);
                    break;
                case 0xd0: // YM2612 TL delta + end of frame
                    fmTlDeltaSet(val);
                    return;
                case 0xe0: // YM2612 general register write
                    cs = (val & 0x7) + 1;
                    port = (val & 0x8) >> 3;
                    for (int i = 0; i < cs; i++) {
                        adr = xgmBuf[fmMusicPtr++];
                        dat = xgmBuf[fmMusicPtr++];
                        writeYM2612(port == 0, adr & 0xff, dat & 0xff);
                    }
                    break;
                case 0xf0:
                    switch (val) {
                        case 0x00: // Frame splitter (for too long frame) - increment 'frame to process' counter
                            pendingFrame++;
                            return;
                        case 0x08: // YM2612 advanced $28 (key) register write (not ALL OFF/ON)
                            dat = xgmBuf[fmMusicPtr];
                            fmMusicPtr += 1;
                            writeYM2612P0(0x28, dat & 0xff);
                            break;
                        case 0x09: // YM2612 register $22 (LFO) write
                            dat = xgmBuf[fmMusicPtr];
                            fmMusicPtr += 1;
                            writeYM2612P0(0x22, dat & 0xff);
                            break;
                        case 0x0a: // YM2612 register $27.6 = 1 (CH3 special mode enable)
                            writeYM2612P0(0x27, 0x45);
                            ch3spEnable = true;
                            break;
                        case 0x0b: // YM2612 register $27.6 = 0 (CH3 special mode disable)
                            writeYM2612P0(0x27, 0x05);
                            ch3spEnable = false;
                            break;
                        case 0x0c: // YM2612 register $2B = 80 (DAC enable)
                            writeYM2612P0(0x2b, 0x80);
                            dacEnable = 1;
                            break;
                        case 0x0d: // YM2612 register $2B = 00 (DAC disable)
                            writeYM2612P0(0x2b, 0x00);
                            dacEnable = 0;
                            break;
                        case 0x0f:
                            int loopAdr = ByteUtil.readLe24(xgmBuf, fmMusicPtr);
                            if (loopAdr == 0xff_ffff) endFm = true;
                            fmMusicPtr = fmDataBlockAddr + loopAdr;
                            fmLoopCnt++;
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }
                    break;
            }
        }
    }

    private void fmTlDeltaSet(byte val) {
        int cs = val & 0x3;
        int slot = (val & 0xc) >> 2;
        int port = xgmBuf[fmMusicPtr] & 0x1;
        int addOrSub = (xgmBuf[fmMusicPtr] & 0x2) >> 1;
        byte tl = (byte) (((xgmBuf[fmMusicPtr] & 0xff) >> 2) + 1);
        fmMusicPtr += 1;
        fmTL[port][cs][slot] = (byte) ((fmTL[port][cs][slot] & 0xff) + (addOrSub == 0 ? 1 : -1) * (tl & 0xff));
        writeYM2612(port == 0, (0x40 + slot * 4 + cs) & 0xff, fmTL[port][cs][slot] & 0xff);
    }

    private void fmFreqDeltaSet(byte val) {
        int cs = val & 0x3;
        int port = (val & 0x4) >> 2;
        int ch3m = (val & 0x8) >> 3;
        int addOrsub = xgmBuf[fmMusicPtr] & 0x1;
        byte freq = (byte) (((xgmBuf[fmMusicPtr] & 0xff) >> 1) + 1);
        fmMusicPtr += 1;

        if (ch3m == 0) {
            fmFreq[port][cs][0] = fmFreq[port][cs][0] + (addOrsub == 0 ? 1 : -1) * (freq & 0xff);
            writeYM2612(port == 0, (0xa4 + cs) & 0xff, (fmFreq[port][cs][0] & 0xff00) >> 8);
            writeYM2612(port == 0, (0xa0 + cs) & 0xff, fmFreq[port][cs][0] & 0xff);
            return;
        }

        fmFreq[port][2][cs] = fmFreq[port][2][cs] + (addOrsub == 0 ? 1 : -1) * (freq & 0xff);
        writeYM2612(true, ch3FnumAdr[cs] & 0xff, (fmFreq[port][2][cs] & 0xff00) >> 8);
        writeYM2612(true, ((ch3FnumAdr[cs] & 0xff) - 4) & 0xff, (fmFreq[port][2][cs] & 0xff));
    }

    private void fmFreqSetAndKeyOffOn(byte val) {
        int cs = val & 0x3;
        int port = (val & 0x4) >> 2;
        int ch3m = (val & 0x8) >> 3;
        int keyOff = (xgmBuf[fmMusicPtr] & 0x40) >> 6;
        int keyOn = (xgmBuf[fmMusicPtr] & 0x80) >> 7;
        int freq = ByteUtil.readBeShort(xgmBuf, fmMusicPtr) & 0x3fff;
        fmMusicPtr += 2;

        if (ch3m == 0) {
            fmFreq[port][cs][0] = freq;
            if (keyOff != 0) {
                if (cs == 2 && port == 0)
                    ch3KeyOn = 0x00;
                writeYM2612P0(0x28, (0x00 | cs | (port << 2)) & 0xff);
            }
            writeYM2612(port == 0, (0xa4 + cs) & 0xff, (freq & 0xff00) >> 8);
            writeYM2612(port == 0, (0xa0 + cs) & 0xff, freq & 0xff);
            if (keyOn != 0) {
                if (cs == 2 && port == 0)
                    ch3KeyOn = (byte) 0xf0;
                writeYM2612P0(0x28, (0xf0 | cs | (port << 2)) & 0xff);
            }
            return;
        }

        fmFreq[port][2][cs] = freq;

        //byte m = (byte) (0x20 << cs);
        //if (keyOff != 0) {
        //    ch3KeyOn = (byte) ((ch3KeyOn & (~m)) & 0xff);
        //    writeYM2612P0(0x28, (ch3KeyOn | 2) & 0xff);
        //}
        writeYM2612(true, ch3FnumAdr[cs] & 0xff, (fmFreq[port][2][cs] & 0xff00) >> 8);
        writeYM2612(true, ((ch3FnumAdr[cs] & 0xff) - 4) & 0xff, fmFreq[port][2][cs] & 0xff);
        //if (keyOn != 0) {
        //    ch3KeyOn = (byte) (ch3KeyOn | m);
        //    writeYM2612P0(0x28, (ch3KeyOn | 2) & 0xff);
        //}
    }

    private void sendInst(int cs, int port, byte[] vd) {
        // ml/dt
        ym2612Write.accept(port, 0x30 + cs, vd[0] & 0xff);
        ym2612Write.accept(port, 0x34 + cs, vd[1] & 0xff);
        ym2612Write.accept(port, 0x38 + cs, vd[2] & 0xff);
        ym2612Write.accept(port, 0x3c + cs, vd[3] & 0xff);
        // tl
        fmTL[port][cs][0] = vd[4];
        fmTL[port][cs][1] = vd[5];
        fmTL[port][cs][2] = vd[6];
        fmTL[port][cs][3] = vd[7];
        // AR/SR
        ym2612Write.accept(port, 0x50 + cs, vd[8] & 0xff);
        ym2612Write.accept(port, 0x54 + cs, vd[9] & 0xff);
        ym2612Write.accept(port, 0x58 + cs, vd[10] & 0xff);
        ym2612Write.accept(port, 0x5c + cs, vd[11] & 0xff);
        // DR/AM
        ym2612Write.accept(port, 0x60 + cs, vd[12] & 0xff);
        ym2612Write.accept(port, 0x64 + cs, vd[13] & 0xff);
        ym2612Write.accept(port, 0x68 + cs, vd[14] & 0xff);
        ym2612Write.accept(port, 0x6c + cs, vd[15] & 0xff);
        // SR
        ym2612Write.accept(port, 0x70 + cs, vd[16] & 0xff);
        ym2612Write.accept(port, 0x74 + cs, vd[17] & 0xff);
        ym2612Write.accept(port, 0x78 + cs, vd[18] & 0xff);
        ym2612Write.accept(port, 0x7c + cs, vd[19] & 0xff);
        // SL/RR
        fmSLRR[port][cs][0] = vd[20];
        fmSLRR[port][cs][1] = vd[21];
        fmSLRR[port][cs][2] = vd[22];
        fmSLRR[port][cs][3] = vd[23];
        // SSGEG
        ym2612Write.accept(port, 0x90 + cs, vd[24] & 0xff);
        ym2612Write.accept(port, 0x94 + cs, vd[25] & 0xff);
        ym2612Write.accept(port, 0x98 + cs, vd[26] & 0xff);
        ym2612Write.accept(port, 0x9c + cs, vd[27] & 0xff);
        // FB/ALG
        ym2612Write.accept(port, 0xb0 + cs, vd[28] & 0xff);
        fmALG[port][cs] = (byte) (vd[28] & 0xf);

        ym2612Write.accept(port, 0x40 + cs, vd[4] & 0xff);
        ym2612Write.accept(port, 0x44 + cs, vd[5] & 0xff);
        ym2612Write.accept(port, 0x48 + cs, vd[6] & 0xff);
        ym2612Write.accept(port, 0x4c + cs, vd[7] & 0xff);
        ym2612Write.accept(port, 0x80 + cs, vd[20] & 0xff);
        ym2612Write.accept(port, 0x84 + cs, vd[21] & 0xff);
        ym2612Write.accept(port, 0x88 + cs, vd[22] & 0xff);
        ym2612Write.accept(port, 0x8c + cs, vd[23] & 0xff);

        // pan/ams/pms
        ym2612Write.accept(port, 0xb4 + cs, vd[29] & 0xff);
        fmPanAmsPms[port][cs] = vd[29];
    }

    private void oneFramePsg() {
        if (psgWaitCnt-- > 0) return;
        if (xgmBuf == null) return;
        while (true) {
            if (psgMusicPtr >= xgmBuf.length) {
                endPsg = true;
                return;
            }
            byte dat = xgmBuf[psgMusicPtr++];
            int cmd = dat & 0xf0;
            byte val = (byte) (dat & 0x0f);

            boolean eof, addOrSub;
            int td, ch, delta, env;

            switch (cmd) {
                case 0x00: // wait
                    psgWaitCnt = val & 0xff;
                    if (psgWaitCnt == 14) psgWaitCnt = (xgmBuf[psgMusicPtr++] & 0xff) + 14;
                    else if (psgWaitCnt == 15) {
                        int loopAdr = ByteUtil.readLe24(xgmBuf, psgMusicPtr);
                        if (loopAdr == 0xff_ffff) endPsg = true;
                        psgMusicPtr = psgDataBlockAddr + loopAdr;
                        psgLoopCnt++;
                        break;
                    }
                    return;
                case 0x10: // PSG freq/tone low update + end of frame
                    eof = (val & 1) != 0;
                    dat = xgmBuf[psgMusicPtr++];
                    sn76489Write.accept(dat & 0xff);
                    ch = (dat & 0x60) >> 5;
                    psgFreq[ch] = (psgFreq[ch] & 0x3f0) | (dat & 0xf);
                    if (eof) return;
                    break;
                case 0x20: // PSG freq/tone update
                    td = val & 3;
                    ch = (val & 0xc) >> 2;
                    dat = xgmBuf[psgMusicPtr++];
                    psgFreq[ch] = (dat & 0xff) | (td << 8);
                    sn76489Write.accept((0x80 | (ch << 5) | (psgFreq[ch] & 0xf)) & 0xff);
                    sn76489Write.accept(((psgFreq[ch] & 0x3f0) >> 4) & 0xff);
                    break;
                case 0x30: // PSG freq/tone update + end of frame
                    td = val & 3;
                    ch = (val & 0xc) >> 2;
                    dat = xgmBuf[psgMusicPtr++];
                    psgFreq[ch] = (dat & 0xff) | (td << 8);
                    sn76489Write.accept((0x80 |  (ch << 5) | (psgFreq[ch] & 0xf)) & 0xff);
                    sn76489Write.accept(((psgFreq[ch] & 0x3f0) >> 4) & 0xff);
                    return;
                case 0x40:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 0;
                    psgFreq[ch] = psgFreq[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x80 | (ch << 5) | (psgFreq[ch] & 0xf)) & 0xff);
                    sn76489Write.accept(((psgFreq[ch] & 0x3f0) >> 4) & 0xff);
                    if (eof) return;
                    break;
                case 0x50:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 1;
                    psgFreq[ch] = psgFreq[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x80 | (ch << 5) | (psgFreq[ch] & 0xf)) & 0xff);
                    sn76489Write.accept(((psgFreq[ch] & 0x3f0) >> 4) & 0xff);
                    if (eof) return;
                    break;
                case 0x60:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 2;
                    psgFreq[ch] = psgFreq[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x80 | (ch << 5) | (psgFreq[ch] & 0xf)) & 0xff);
                    sn76489Write.accept(((psgFreq[ch] & 0x3f0) >> 4) & 0xff);
                    if (eof) return;
                    break;
                case 0x70:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 3;
                    psgFreq[ch] = psgFreq[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x80 | (ch << 5) | (psgFreq[ch] & 0xf)) & 0xff);
                    sn76489Write.accept(((psgFreq[ch] & 0x3f0) >> 4) & 0xff);
                    if (eof) return;
                    break;
                case 0x80: // PSG ch0 vol/env update
                    env = val & 0xff;
                    ch = 0;
                    psgVol[ch] = env;
                    sn76489Write.accept((0x90 | (ch << 5) | (psgVol[ch] & 0xf)) & 0xff);
                    break;
                case 0x90:
                    env = val & 0xff;
                    ch = 1;
                    psgVol[ch] = env;
                    sn76489Write.accept((0x90 | (ch << 5) | (psgVol[ch] & 0xf)) & 0xff);
                    break;
                case 0xa0:
                    env = val & 0xff;
                    ch = 2;
                    psgVol[ch] = env;
                    sn76489Write.accept((0x90 | (ch << 5) | (psgVol[ch] & 0xf)) & 0xff);
                    break;
                case 0xb0:
                    env = val & 0xff;
                    ch = 3;
                    psgVol[ch] = env;
                    sn76489Write.accept((0x90 | (ch << 5) |  (psgVol[ch] & 0xf)) & 0xff);
                    break;
                case 0xc0:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 0;
                    psgVol[ch] = psgVol[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x90 | (ch << 5) | (psgVol[ch] & 0xf)) & 0xff);
                    if (eof) return;
                    break;
                case 0xd0:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 1;
                    psgVol[ch] = psgVol[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x90 | (ch << 5) | psgVol[ch] & 0xf) & 0xff);
                    if (eof) return;
                    break;
                case 0xe0:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 2;
                    psgVol[ch] = psgVol[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x90 | (ch << 5) | (psgVol[ch] & 0xf)) & 0xff);
                    if (eof) return;
                    break;
                case 0xf0:
                    delta = (val & 3) + 1;
                    addOrSub = (val & 4) != 0; // false:add  true:sub
                    eof = (val & 8) != 0;
                    ch = 3;
                    psgVol[ch] = psgVol[ch] + (addOrSub ? -1 : 1) * delta;
                    sn76489Write.accept((0x90 | (ch << 5) | (psgVol[ch] & 0xf)) & 0xff);
                    if (eof) return;
                    break;
            }
        }
    }

    private void writeYM2612(boolean isP0, int adr, int val) {
        if (isP0) writeYM2612P0(adr, val);
        else writeYM2612P1(adr, val);

        if (adr >= 0x40 && adr < 0x50) {
            int slot = (adr - 0x40) / 4;
            int cs = (adr - 0x40) % 4;
            fmTL[isP0 ? 0 : 1][cs][slot] = (byte) val;
        }
    }

    private void writeYM2612P0(int adr, int val) {
        if (adr == 0x2b) dacEnable = (byte) (val & 0x80);
        else if (adr == 0x27) ch3spEnable = ((val & 0x40) != 0);

        ym2612Write.accept(0, adr, val);
    }

    private void writeYM2612P1(int adr, int val) {
        ym2612Write.accept(1, adr, val);
    }

    private void playPCM(byte x, int id) {
        if (id != 0 && sampleID.length <= (id - 1) && sampleID[id - 1].addr == 0xff_ff00) return;

        int priority = x & 0x8;
        int speed = x & 0x4;
        int channel = x & 0x3;

        // Can only be played if priority is high or if muted
        if (xgm2pcm[channel].priority > priority && xgm2pcm[channel].isPlaying) return;

        if (id == 0 || sampleID[id - 1].size == 0) {
            // If the ID is 0 or an undefined ID is specified, the sound will stop.
            xgm2pcm[channel].priority = 0;
            xgm2pcm[channel].isPlaying = false;
            return;
        }

        xgm2pcm[channel].priority = priority;
        xgm2pcm[channel].speed = speed;
        xgm2pcm[channel].speedWait = 0;
        xgm2pcm[channel].startAddr = sampleDataBlockAddr + sampleID[id - 1].addr;
        xgm2pcm[channel].endAddr = sampleDataBlockAddr + sampleID[id - 1].addr + sampleID[id - 1].size;
        xgm2pcm[channel].addr = sampleDataBlockAddr + sampleID[id - 1].addr;
        xgm2pcm[channel].inst = id;
        xgm2pcm[channel].isPlaying = true;
    }
}
