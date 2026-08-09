/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;


/**
 * The PS1 sound processing unit: 24 voices of ADPCM with an ADSR envelope and one reverb unit.
 * <p>
 * ported from aosdk eng_psf/peops, which is Pete Bernert's PEOpS with Neill Corlett's
 * envelope and reverb models and kode54's gaussian interpolation. That code is GPL v2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class PeopsSpu implements Spu {

    private static final int MAXCHAN = 24;

    // register addresses, relative to the 0x1f801xxx page
    private static final int H_SPUReverbAddr = 0x0da2;
    private static final int H_SPUirqAddr = 0x0da4;
    private static final int H_SPUaddr = 0x0da6;
    private static final int H_SPUdata = 0x0da8;
    private static final int H_SPUctrl = 0x0daa;
    private static final int H_SPUstat = 0x0dae;
    private static final int H_SPUrvolL = 0x0d84;
    private static final int H_SPUrvolR = 0x0d86;
    private static final int H_SPUon1 = 0x0d88;
    private static final int H_SPUon2 = 0x0d8a;
    private static final int H_SPUoff1 = 0x0d8c;
    private static final int H_SPUoff2 = 0x0d8e;
    private static final int H_FMod1 = 0x0d90;
    private static final int H_FMod2 = 0x0d92;
    private static final int H_Noise1 = 0x0d94;
    private static final int H_Noise2 = 0x0d96;
    private static final int H_RVBon1 = 0x0d98;
    private static final int H_RVBon2 = 0x0d9a;
    private static final int H_Reverb = 0x0dc0;

    /** what pCurr holds once a voice has run off the end of its sample */
    private static final int STOP = -1;

    /**
     * the ADPCM predictor coefficients. The hardware has five, and a byte can name sixteen,
     * so the rest predict nothing rather than reading past the end of the table the way the
     * C does.
     */
    private static final int[][] f = {
            {0, 0}, {60, 0}, {115, -52}, {98, -55}, {122, -60},
            {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0},
            {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0},
    };

    /** the envelope rate table, doubling every four entries */
    private static final int[] rateTable = new int[160];

    static {
        int r = 3, rs = 1, rd = 0;
        // the first 32 entries stay zero, the real values start at 32
        for (int i = 32; i < 160; i++) {
            if (r < 0x3FFFFFFF) {
                r += rs;
                rd++;
                if (rd == 5) {
                    rd = 1;
                    rs *= 2;
                }
            }
            if (r > 0x3FFFFFFF) {
                r = 0x3FFFFFFF;
            }
            rateTable[i] = r;
        }
    }

    private static final int[] sexyTable = {0, 4, 6, 8, 9, 10, 11, 12};

    /** one voice */
    private static class Channel {
        boolean bNew;      // start flag

        int iSBPos;        // mixing stuff
        int spos;
        int sinc;
        final int[] SB = new int[32 + 1];
        int sval;

        int pStart;        // byte offset into sound ram
        int pCurr;
        int pLoop;

        boolean bOn;       // is the channel playing a sample
        boolean bStop;     // has it been keyed off, i.e. is it in its release
        int iActFreq;      // current psx pitch
        int iUsedFreq;     // current mixer pitch
        int iLeftVolume;
        int iLeftVolRaw;
        boolean bIgnoreLoop;
        int iRightVolume;
        int iRightVolRaw;
        int iRawPitch;
        boolean iIrqDone;
        int s_1;           // last decoding infos
        int s_2;
        boolean bNoise;
        int bFMod;         // 0 off, 1 sound channel, 2 freq channel
        int iOldNoise;

        // the envelope
        int state;
        boolean attackModeExp;
        int attackRate;
        int decayRate;
        int sustainLevel;
        boolean sustainModeExp;
        boolean sustainIncrease;
        int sustainRate;
        boolean releaseModeExp;
        int releaseRate;
        int envelopeVol;
        int lVolume;

        void clear() {
            bNew = false;
            iSBPos = spos = sinc = sval = 0;
            java.util.Arrays.fill(SB, 0);
            pStart = pCurr = pLoop = 0;
            bOn = bStop = false;
            iActFreq = iUsedFreq = 0;
            iLeftVolume = iLeftVolRaw = iRightVolume = iRightVolRaw = 0;
            bIgnoreLoop = false;
            iRawPitch = 0;
            iIrqDone = false;
            s_1 = s_2 = 0;
            bNoise = false;
            bFMod = 0;
            iOldNoise = 0;
            state = 0;
            attackModeExp = sustainModeExp = sustainIncrease = releaseModeExp = false;
            attackRate = decayRate = sustainLevel = sustainRate = releaseRate = 0;
            envelopeVol = 0;
            lVolume = 0;
        }
    }

    /** the one reverb unit */
    private static class Reverb {
        int StartAddr;  // in samples
        int CurrAddr;

        int Enabled;
        int VolLeft;
        int VolRight;
        int iRVBLeft;
        int iRVBRight;

        int FB_SRC_A, FB_SRC_B;
        int IIR_ALPHA, ACC_COEF_A, ACC_COEF_B, ACC_COEF_C, ACC_COEF_D;
        int IIR_COEF, FB_ALPHA, FB_X;
        int IIR_DEST_A0, IIR_DEST_A1;
        int ACC_SRC_A0, ACC_SRC_A1, ACC_SRC_B0, ACC_SRC_B1;
        int IIR_SRC_A0, IIR_SRC_A1;
        int IIR_DEST_B0, IIR_DEST_B1;
        int ACC_SRC_C0, ACC_SRC_C1, ACC_SRC_D0, ACC_SRC_D1;
        int IIR_SRC_B1, IIR_SRC_B0;
        int MIX_DEST_A0, MIX_DEST_A1, MIX_DEST_B0, MIX_DEST_B1;
        int IN_COEF_L, IN_COEF_R;

        void clear() {
            StartAddr = CurrAddr = Enabled = VolLeft = VolRight = iRVBLeft = iRVBRight = 0;
            FB_SRC_A = FB_SRC_B = IIR_ALPHA = ACC_COEF_A = ACC_COEF_B = ACC_COEF_C = 0;
            ACC_COEF_D = IIR_COEF = FB_ALPHA = FB_X = IIR_DEST_A0 = IIR_DEST_A1 = 0;
            ACC_SRC_A0 = ACC_SRC_A1 = ACC_SRC_B0 = ACC_SRC_B1 = IIR_SRC_A0 = IIR_SRC_A1 = 0;
            IIR_DEST_B0 = IIR_DEST_B1 = ACC_SRC_C0 = ACC_SRC_C1 = ACC_SRC_D0 = ACC_SRC_D1 = 0;
            IIR_SRC_B1 = IIR_SRC_B0 = MIX_DEST_A0 = MIX_DEST_A1 = MIX_DEST_B0 = MIX_DEST_B1 = 0;
            IN_COEF_L = IN_COEF_R = 0;
        }
    }

    /** 512 kB of sound ram, as the 16 bit words the hardware addresses */
    private final short[] spuMem = new short[256 * 1024];
    private final int[] regArea = new int[0x200];

    /** channel 24 exists only so a frequency modulator on channel 23 has somewhere to write */
    private final Channel[] chan = new Channel[MAXCHAN + 1];
    private final Reverb rvb = new Reverb();

    private int iVolume = 255;

    private int dwNoiseVal = 1;
    private int spuCtrl;
    private int spuStat;
    private int spuIrq;
    private int spuAddr = 0xffffffff;
    private int pSpuIrq;
    private boolean bSPUIsOpen;

    // the reverb resampler state
    private final int[][] downbuf = new int[2][8];
    private final int[][] upbuf = new int[2][8];
    private int dbpos, ubpos;
    private static final int[] downcoeffs = {1283, 5344, 10895, 15243, 15243, 10895, 5344, 1283};

    /** IOP ram, which DMA moves samples to and from */
    private int[] psxRam;

    /** the last sample {@link #sample()} produced */
    public int left;
    public int right;

    public PeopsSpu() {
        for (int i = 0; i < chan.length; i++) {
            chan[i] = new Channel();
        }
    }

    public void setPsxRam(int[] psxRam) {
        this.psxRam = psxRam;
    }

    // ---- sound ram, byte and word wise ----

    private int spuByte(int byteOffset) {
        int w = spuMem[(byteOffset >> 1) & 0x3ffff] & 0xffff;
        return ((byteOffset & 1) != 0 ? w >>> 8 : w) & 0xff;
    }

    private int ramHalf(int byteAddress) {
        int a = (byteAddress >> 1) & 0xfffff;
        int w = psxRam[(a >> 1) & (psxRam.length - 1)];
        return ((a & 1) != 0 ? w >>> 16 : w) & 0xffff;
    }

    private void setRamHalf(int byteAddress, int value) {
        int a = (byteAddress >> 1) & 0xfffff;
        int i = (a >> 1) & (psxRam.length - 1);
        if ((a & 1) != 0) {
            psxRam[i] = (psxRam[i] & 0x0000ffff) | (value << 16);
        } else {
            psxRam[i] = (psxRam[i] & 0xffff0000) | (value & 0xffff);
        }
    }

    // ---- init ----

    public void init() {
        for (Channel c : chan) {
            c.clear();
        }
        rvb.clear();
        java.util.Arrays.fill(regArea, 0);
        java.util.Arrays.fill(spuMem, (short) 0);
    }

    public void open() {
        if (bSPUIsOpen) {
            return;
        }
        spuIrq = 0;
        spuStat = 0;
        spuCtrl = 0;
        spuAddr = 0xffffffff;
        dwNoiseVal = 1;

        for (Channel c : chan) {
            c.clear();
        }
        pSpuIrq = 0;

        iVolume = 255;
        setupStreams();

        bSPUIsOpen = true;
    }

    private void setupStreams() {
        for (int i = 0; i < MAXCHAN; i++) {
            chan[i].sustainLevel = 1024;
            chan[i].iIrqDone = false;
            chan[i].pLoop = 0;
            chan[i].pStart = 0;
            chan[i].pCurr = 0;
        }
    }

    public void close() {
        bSPUIsOpen = false;
    }

    // ---- envelope ----

    private static void startADSR(Channel c) {
        c.lVolume = 1;
        c.state = 0;
        c.envelopeVol = 0;
    }

    private static int mixADSR(Channel c) {
        if (c.bStop) {
            // release
            if (c.releaseModeExp) {
                c.envelopeVol -= rateTable[(4 * (c.releaseRate ^ 0x1F)) - 0x18 + 32
                        + sexyTable[(c.envelopeVol >>> 28) & 0x7]];
            } else {
                c.envelopeVol -= rateTable[(4 * (c.releaseRate ^ 0x1F)) - 0x0C + 32];
            }

            if (c.envelopeVol < 0) {
                c.envelopeVol = 0;
                c.bOn = false;
                c.bNoise = false;
            }

            c.lVolume = c.envelopeVol >> 21;
            return c.lVolume;
        }

        if (c.state == 0) { // attack
            if (c.attackModeExp) {
                if (c.envelopeVol < 0x60000000) {
                    c.envelopeVol += rateTable[(c.attackRate ^ 0x7F) - 0x10 + 32];
                } else {
                    c.envelopeVol += rateTable[(c.attackRate ^ 0x7F) - 0x18 + 32];
                }
            } else {
                c.envelopeVol += rateTable[(c.attackRate ^ 0x7F) - 0x10 + 32];
            }

            if (c.envelopeVol < 0) {
                c.envelopeVol = 0x7FFFFFFF;
                c.state = 1;
            }

            c.lVolume = c.envelopeVol >> 21;
            return c.lVolume;
        }

        if (c.state == 1) { // decay
            c.envelopeVol -= rateTable[(4 * (c.decayRate ^ 0x1F)) - 0x18 + 32
                    + sexyTable[(c.envelopeVol >>> 28) & 0x7]];

            if (c.envelopeVol < 0) {
                c.envelopeVol = 0;
            }
            if (((c.envelopeVol >>> 27) & 0xF) <= c.sustainLevel) {
                c.state = 2;
            }

            c.lVolume = c.envelopeVol >> 21;
            return c.lVolume;
        }

        if (c.state == 2) { // sustain
            if (c.sustainIncrease) {
                if (c.sustainModeExp) {
                    if (c.envelopeVol < 0x60000000) {
                        c.envelopeVol += rateTable[(c.sustainRate ^ 0x7F) - 0x10 + 32];
                    } else {
                        c.envelopeVol += rateTable[(c.sustainRate ^ 0x7F) - 0x18 + 32];
                    }
                } else {
                    c.envelopeVol += rateTable[(c.sustainRate ^ 0x7F) - 0x10 + 32];
                }

                if (c.envelopeVol < 0) {
                    c.envelopeVol = 0x7FFFFFFF;
                }
            } else {
                if (c.sustainModeExp) {
                    c.envelopeVol -= rateTable[(c.sustainRate ^ 0x7F) - 0x1B + 32
                            + sexyTable[(c.envelopeVol >>> 28) & 0x7]];
                } else {
                    c.envelopeVol -= rateTable[(c.sustainRate ^ 0x7F) - 0x0F + 32];
                }

                if (c.envelopeVol < 0) {
                    c.envelopeVol = 0;
                }
            }
            c.lVolume = c.envelopeVol >> 21;
            return c.lVolume;
        }

        return 0;
    }

    // ---- reverb ----

    private int gBuffer(int iOff) {
        iOff = (iOff * 4) + rvb.CurrAddr;
        while (iOff > 0x3FFFF) {
            iOff = rvb.StartAddr + (iOff - 0x40000);
        }
        while (iOff < rvb.StartAddr) {
            iOff = 0x3ffff - (rvb.StartAddr - iOff);
        }
        return spuMem[iOff & 0x3ffff];
    }

    private void sBuffer(int iOff, int iVal) {
        iOff = (iOff * 4) + rvb.CurrAddr;
        while (iOff > 0x3FFFF) {
            iOff = rvb.StartAddr + (iOff - 0x40000);
        }
        while (iOff < rvb.StartAddr) {
            iOff = 0x3ffff - (rvb.StartAddr - iOff);
        }
        if (iVal < -32768) iVal = -32768;
        if (iVal > 32767) iVal = 32767;
        spuMem[iOff & 0x3ffff] = (short) iVal;
    }

    private void sBuffer1(int iOff, int iVal) {
        iOff = (iOff * 4) + rvb.CurrAddr + 1;
        while (iOff > 0x3FFFF) {
            iOff = rvb.StartAddr + (iOff - 0x40000);
        }
        while (iOff < rvb.StartAddr) {
            iOff = 0x3ffff - (rvb.StartAddr - iOff);
        }
        if (iVal < -32768) iVal = -32768;
        if (iVal > 32767) iVal = 32767;
        spuMem[iOff & 0x3ffff] = (short) iVal;
    }

    /** the reverb runs at 22 kHz, so the input is downsampled and the wet output upsampled */
    private void mixReverbLeftRight(int[] out, int inLeft, int inRight) {
        if (rvb.StartAddr == 0) { // reverb is off
            rvb.iRVBLeft = rvb.iRVBRight = 0;
            return;
        }

        downbuf[0][dbpos] = inLeft;
        downbuf[1][dbpos] = inRight;
        dbpos = (dbpos + 1) & 7;

        if ((dbpos & 1) != 0) { // every second sample, i.e. 22 kHz
            if ((spuCtrl & 0x80) != 0) {
                int INPUT_SAMPLE_L = 0;
                int INPUT_SAMPLE_R = 0;

                for (int x = 0; x < 8; x++) {
                    // the digits that do not matter go, so the accumulator cannot overflow
                    INPUT_SAMPLE_L += (downbuf[0][(dbpos + x) & 7] * downcoeffs[x]) >> 8;
                    INPUT_SAMPLE_R += (downbuf[1][(dbpos + x) & 7] * downcoeffs[x]) >> 8;
                }

                INPUT_SAMPLE_L >>= (16 - 8);
                INPUT_SAMPLE_R >>= (16 - 8);

                long IIR_INPUT_A0 = ((long) gBuffer(rvb.IIR_SRC_A0) * rvb.IIR_COEF >> 15)
                        + ((INPUT_SAMPLE_L * rvb.IN_COEF_L) >> 15);
                long IIR_INPUT_A1 = ((long) gBuffer(rvb.IIR_SRC_A1) * rvb.IIR_COEF >> 15)
                        + ((INPUT_SAMPLE_R * rvb.IN_COEF_R) >> 15);
                long IIR_INPUT_B0 = ((long) gBuffer(rvb.IIR_SRC_B0) * rvb.IIR_COEF >> 15)
                        + ((INPUT_SAMPLE_L * rvb.IN_COEF_L) >> 15);
                long IIR_INPUT_B1 = ((long) gBuffer(rvb.IIR_SRC_B1) * rvb.IIR_COEF >> 15)
                        + ((INPUT_SAMPLE_R * rvb.IN_COEF_R) >> 15);

                long IIR_A0 = (IIR_INPUT_A0 * rvb.IIR_ALPHA >> 15)
                        + ((long) gBuffer(rvb.IIR_DEST_A0) * (32768 - rvb.IIR_ALPHA) >> 15);
                long IIR_A1 = (IIR_INPUT_A1 * rvb.IIR_ALPHA >> 15)
                        + ((long) gBuffer(rvb.IIR_DEST_A1) * (32768 - rvb.IIR_ALPHA) >> 15);
                long IIR_B0 = (IIR_INPUT_B0 * rvb.IIR_ALPHA >> 15)
                        + ((long) gBuffer(rvb.IIR_DEST_B0) * (32768 - rvb.IIR_ALPHA) >> 15);
                long IIR_B1 = (IIR_INPUT_B1 * rvb.IIR_ALPHA >> 15)
                        + ((long) gBuffer(rvb.IIR_DEST_B1) * (32768 - rvb.IIR_ALPHA) >> 15);

                sBuffer1(rvb.IIR_DEST_A0, (int) IIR_A0);
                sBuffer1(rvb.IIR_DEST_A1, (int) IIR_A1);
                sBuffer1(rvb.IIR_DEST_B0, (int) IIR_B0);
                sBuffer1(rvb.IIR_DEST_B1, (int) IIR_B1);

                int ACC0 = (int) (((long) gBuffer(rvb.ACC_SRC_A0) * rvb.ACC_COEF_A >> 15)
                        + ((long) gBuffer(rvb.ACC_SRC_B0) * rvb.ACC_COEF_B >> 15)
                        + ((long) gBuffer(rvb.ACC_SRC_C0) * rvb.ACC_COEF_C >> 15)
                        + ((long) gBuffer(rvb.ACC_SRC_D0) * rvb.ACC_COEF_D >> 15));
                int ACC1 = (int) (((long) gBuffer(rvb.ACC_SRC_A1) * rvb.ACC_COEF_A >> 15)
                        + ((long) gBuffer(rvb.ACC_SRC_B1) * rvb.ACC_COEF_B >> 15)
                        + ((long) gBuffer(rvb.ACC_SRC_C1) * rvb.ACC_COEF_C >> 15)
                        + ((long) gBuffer(rvb.ACC_SRC_D1) * rvb.ACC_COEF_D >> 15));

                int FB_A0 = gBuffer(rvb.MIX_DEST_A0 - rvb.FB_SRC_A);
                int FB_A1 = gBuffer(rvb.MIX_DEST_A1 - rvb.FB_SRC_A);
                int FB_B0 = gBuffer(rvb.MIX_DEST_B0 - rvb.FB_SRC_B);
                int FB_B1 = gBuffer(rvb.MIX_DEST_B1 - rvb.FB_SRC_B);

                sBuffer(rvb.MIX_DEST_A0, ACC0 - ((FB_A0 * rvb.FB_ALPHA) >> 15));
                sBuffer(rvb.MIX_DEST_A1, ACC1 - ((FB_A1 * rvb.FB_ALPHA) >> 15));

                sBuffer(rvb.MIX_DEST_B0, ((rvb.FB_ALPHA * ACC0) >> 15)
                        - ((FB_A0 * (rvb.FB_ALPHA ^ 0xFFFF8000)) >> 15)
                        - ((FB_B0 * rvb.FB_X) >> 15));
                sBuffer(rvb.MIX_DEST_B1, ((rvb.FB_ALPHA * ACC1) >> 15)
                        - ((FB_A1 * (rvb.FB_ALPHA ^ 0xFFFF8000)) >> 15)
                        - ((FB_B1 * rvb.FB_X) >> 15));

                rvb.iRVBLeft = (gBuffer(rvb.MIX_DEST_A0) + gBuffer(rvb.MIX_DEST_B0)) / 3;
                rvb.iRVBRight = (gBuffer(rvb.MIX_DEST_A1) + gBuffer(rvb.MIX_DEST_B1)) / 3;

                rvb.iRVBLeft = (int) ((long) rvb.iRVBLeft * rvb.VolLeft >> 14);
                rvb.iRVBRight = (int) ((long) rvb.iRVBRight * rvb.VolRight >> 14);

                upbuf[0][ubpos] = rvb.iRVBLeft;
                upbuf[1][ubpos] = rvb.iRVBRight;
                ubpos = (ubpos + 1) & 7;
            } else { // reverb off
                rvb.iRVBLeft = rvb.iRVBRight = 0;
                return;
            }
            rvb.CurrAddr++;
            if (rvb.CurrAddr > 0x3ffff) {
                rvb.CurrAddr = rvb.StartAddr;
            }
        } else {
            upbuf[0][ubpos] = 0;
            upbuf[1][ubpos] = 0;
            ubpos = (ubpos + 1) & 7;
        }

        int retl = 0, retr = 0;
        for (int x = 0; x < 8; x++) {
            retl += (upbuf[0][(ubpos + x) & 7] * downcoeffs[x]) >> 8;
            retr += (upbuf[1][(ubpos + x) & 7] * downcoeffs[x]) >> 8;
        }
        retl >>= (16 - 8 - 1); // the -1 adjusts for the null padding
        retr >>= (16 - 8 - 1);

        out[0] += retl;
        out[1] += retr;
    }

    // ---- mixing ----

    private static void startSound(Channel c) {
        startADSR(c);

        c.pCurr = c.pStart;

        c.s_1 = 0;
        c.s_2 = 0;
        c.iSBPos = 28;

        c.bNew = false;
        c.bStop = false;
        c.bOn = true;

        c.SB[29] = 0; // the interpolation helpers
        c.SB[30] = 0;

        c.spos = 0x40000;
        c.SB[28] = 0; // so decoding starts right away
    }

    private final int[] mix = new int[2];

    /** produces one stereo sample into {@link #left} and {@link #right} */
    public void sample() {
        int volmul = iVolume;

        int revLeft = 0, revRight = 0;
        int sl = 0, sr = 0;

        for (int ch = 0; ch < MAXCHAN; ch++) {
            Channel c = chan[ch];

            if (c.bNew) {
                startSound(c);
            }
            if (!c.bOn) {
                continue;
            }

            if (c.iActFreq != c.iUsedFreq) { // a new psx frequency
                c.iUsedFreq = c.iActFreq;
                c.sinc = c.iRawPitch << 4;
                if (c.sinc == 0) {
                    c.sinc = 1;
                }
            }

            int fa;
            boolean ended = false;

            while (c.spos >= 0x10000) {
                if (c.iSBPos == 28) {
                    int start = c.pCurr;

                    if (start == STOP) { // the sample said it was the last block
                        c.bOn = false;
                        c.lVolume = 0;
                        c.envelopeVol = 0;
                        ended = true;
                        break;
                    }

                    c.iSBPos = 0;

                    int s_1 = c.s_1;
                    int s_2 = c.s_2;

                    int predictNr = spuByte(start);
                    start++;
                    int shiftFactor = predictNr & 0xf;
                    predictNr >>= 4;
                    int flags = spuByte(start);
                    start++;

                    // decode 28 samples into SB[0..27]
                    for (int nSample = 0; nSample < 28; start++) {
                        int d = spuByte(start);
                        int s = (d & 0xf) << 12;
                        if ((s & 0x8000) != 0) {
                            s |= 0xffff0000;
                        }

                        fa = (s >> shiftFactor);
                        fa = fa + ((s_1 * f[predictNr][0]) >> 6) + ((s_2 * f[predictNr][1]) >> 6);
                        s_2 = s_1;
                        s_1 = fa;
                        s = (d & 0xf0) << 8;

                        c.SB[nSample++] = fa;

                        if ((s & 0x8000) != 0) {
                            s |= 0xffff0000;
                        }
                        fa = (s >> shiftFactor);
                        fa = fa + ((s_1 * f[predictNr][0]) >> 6) + ((s_2 * f[predictNr][1]) >> 6);
                        s_2 = s_1;
                        s_1 = fa;

                        c.SB[nSample++] = fa;
                    }

                    // the irq check, which aosdk's SPUirq does nothing with
                    if ((spuCtrl & 0x40) != 0) {
                        if ((pSpuIrq > start - 16 && pSpuIrq <= start)
                                || ((flags & 1) != 0
                                    && pSpuIrq > c.pLoop - 16 && pSpuIrq <= c.pLoop)) {
                            c.iIrqDone = true;
                        }
                    }

                    if ((flags & 4) != 0 && !c.bIgnoreLoop) {
                        c.pLoop = start - 16;
                    }

                    if ((flags & 1) != 0) { // stop or loop, after this block has played
                        // checking for exactly 3 is what keeps DQ4 and friends from hanging
                        if (flags != 3 || c.pLoop == STOP) {
                            start = STOP;
                        } else {
                            start = c.pLoop;
                        }
                    }

                    c.pCurr = start;
                    c.s_1 = s_1;
                    c.s_2 = s_2;
                }

                fa = c.SB[c.iSBPos++];

                if ((spuCtrl & 0x4000) == 0) {
                    fa = 0; // muted
                } else {
                    if (fa > 32767) fa = 32767;
                    if (fa < -32767) fa = -32767;
                }

                int gpos = c.SB[28];
                c.SB[29 + gpos] = fa;
                gpos = (gpos + 1) & 3;
                c.SB[28] = gpos;

                c.spos -= 0x10000;
            }

            if (ended) {
                continue;
            }

            if (c.bNoise) {
                dwNoiseVal <<= 1;
                if ((dwNoiseVal & 0x80000000) != 0) {
                    dwNoiseVal ^= 0x0040001;
                    fa = (dwNoiseVal >>> 2) & 0x7fff;
                    fa = -fa;
                } else {
                    fa = (dwNoiseVal >>> 2) & 0x7fff;
                }

                // the noise frequency decides how big a step away from the last value may be
                fa = c.iOldNoise + ((fa - c.iOldNoise) / ((0x001f - ((spuCtrl & 0x3f00) >> 9)) + 1));
                if (fa > 32767) fa = 32767;
                if (fa < -32767) fa = -32767;
                c.iOldNoise = fa;
            } else {
                int vl = (c.spos >> 6) & ~3;
                int gpos = c.SB[28];
                int vr = (Gauss.table[vl] * c.SB[29 + gpos]) >> 9;
                vr += (Gauss.table[vl + 1] * c.SB[29 + ((gpos + 1) & 3)]) >> 9;
                vr += (Gauss.table[vl + 2] * c.SB[29 + ((gpos + 2) & 3)]) >> 9;
                vr += (Gauss.table[vl + 3] * c.SB[29 + ((gpos + 3) & 3)]) >> 9;
                fa = vr >> 2;
            }

            c.sval = (mixADSR(c) * fa) >> 10;

            if (c.bFMod == 2) { // this channel modulates the next one's frequency
                int NP = chan[ch + 1].iRawPitch;
                NP = ((32768 + c.sval) * NP) >> 15;

                if (NP > 0x3fff) NP = 0x3fff;
                if (NP < 0x1) NP = 0x1;

                NP = (44100 * NP) / 4096;

                chan[ch + 1].iActFreq = NP;
                chan[ch + 1].iUsedFreq = NP;
                chan[ch + 1].sinc = ((NP / 10) << 16) / 4410;
                if (chan[ch + 1].sinc == 0) {
                    chan[ch + 1].sinc = 1;
                }
            } else {
                int tmpl = (c.sval * c.iLeftVolume) >> 14;
                int tmpr = (c.sval * c.iRightVolume) >> 14;

                sl += tmpl;
                sr += tmpr;

                if (((rvb.Enabled >> ch) & 1) != 0 && (spuCtrl & 0x80) != 0) {
                    revLeft += tmpl;
                    revRight += tmpr;
                }
            }

            c.spos += c.sinc;
        }

        mix[0] = sl;
        mix[1] = sr;
        mixReverbLeftRight(mix, revLeft, revRight);
        sl = (mix[0] * volmul) >> 8;
        sr = (mix[1] * volmul) >> 8;

        if (sl > 32767) sl = 32767;
        if (sl < -32767) sl = -32767;
        if (sr > 32767) sr = 32767;
        if (sr < -32767) sr = -32767;

        left = sl;
        right = sr;
    }

    // ---- registers ----

    @Override
    public void writeRegister(int reg, int val) {
        val &= 0xffff;
        int r = reg & 0xfff;
        regArea[(r - 0xc00) >> 1] = val;

        if (r >= 0x0c00 && r < 0x0d80) { // a channel register
            int ch = (r >> 4) - 0xc0;
            Channel c = chan[ch];

            switch (r & 0x0f) {
            case 0 -> setVolumeLR(false, ch, (short) val);
            case 2 -> setVolumeLR(true, ch, (short) val);
            case 4 -> setPitch(ch, val);
            case 6 -> c.pStart = val << 3;
            case 8 -> {
                c.attackModeExp = (val & 0x8000) != 0;
                c.attackRate = (val >> 8) & 0x007f;
                c.decayRate = (val >> 4) & 0x000f;
                c.sustainLevel = val & 0x000f;
            }
            case 10 -> {
                c.sustainModeExp = (val & 0x8000) != 0;
                c.sustainIncrease = (val & 0x4000) == 0;
                c.sustainRate = (val >> 6) & 0x007f;
                c.releaseModeExp = (val & 0x0020) != 0;
                c.releaseRate = val & 0x001f;
            }
            case 0xE -> {
                c.pLoop = val << 3;
                c.bIgnoreLoop = true;
            }
            default -> { /* nothing */ }
            }
            return;
        }

        switch (r) {
        case H_SPUaddr -> spuAddr = val << 3;
        case H_SPUdata -> {
            spuMem[(spuAddr >> 1) & 0x3ffff] = (short) val;
            spuAddr += 2;
            if (Integer.compareUnsigned(spuAddr, 0x7ffff) > 0) {
                spuAddr = 0;
            }
        }
        case H_SPUctrl -> spuCtrl = val;
        case H_SPUstat -> spuStat = val & 0xf800;
        case H_SPUReverbAddr -> {
            if (val == 0xFFFF || val <= 0x200) {
                rvb.StartAddr = rvb.CurrAddr = 0;
            } else {
                int iv = val << 2;
                if (rvb.StartAddr != iv) {
                    rvb.StartAddr = iv;
                    rvb.CurrAddr = rvb.StartAddr;
                }
            }
        }
        case H_SPUirqAddr -> {
            spuIrq = val;
            pSpuIrq = val << 3;
        }
        case H_SPUrvolL -> rvb.VolLeft = (short) val;
        case H_SPUrvolR -> rvb.VolRight = (short) val;
        case H_SPUon1 -> soundOn(0, 16, val);
        case H_SPUon2 -> soundOn(16, 24, val);
        case H_SPUoff1 -> soundOff(0, 16, val);
        case H_SPUoff2 -> soundOff(16, 24, val);
        case H_FMod1 -> fModOn(0, 16, val);
        case H_FMod2 -> fModOn(16, 24, val);
        case H_Noise1 -> noiseOn(0, 16, val);
        case H_Noise2 -> noiseOn(16, 24, val);
        case H_RVBon1 -> {
            rvb.Enabled &= ~0xFFFF;
            rvb.Enabled |= val;
        }
        case H_RVBon2 -> {
            rvb.Enabled &= 0xFFFF;
            rvb.Enabled |= val << 16;
        }
        case H_Reverb -> rvb.FB_SRC_A = val;
        case H_Reverb + 2 -> rvb.FB_SRC_B = (short) val;
        case H_Reverb + 4 -> rvb.IIR_ALPHA = (short) val;
        case H_Reverb + 6 -> rvb.ACC_COEF_A = (short) val;
        case H_Reverb + 8 -> rvb.ACC_COEF_B = (short) val;
        case H_Reverb + 10 -> rvb.ACC_COEF_C = (short) val;
        case H_Reverb + 12 -> rvb.ACC_COEF_D = (short) val;
        case H_Reverb + 14 -> rvb.IIR_COEF = (short) val;
        case H_Reverb + 16 -> rvb.FB_ALPHA = (short) val;
        case H_Reverb + 18 -> rvb.FB_X = (short) val;
        case H_Reverb + 20 -> rvb.IIR_DEST_A0 = (short) val;
        case H_Reverb + 22 -> rvb.IIR_DEST_A1 = (short) val;
        case H_Reverb + 24 -> rvb.ACC_SRC_A0 = (short) val;
        case H_Reverb + 26 -> rvb.ACC_SRC_A1 = (short) val;
        case H_Reverb + 28 -> rvb.ACC_SRC_B0 = (short) val;
        case H_Reverb + 30 -> rvb.ACC_SRC_B1 = (short) val;
        case H_Reverb + 32 -> rvb.IIR_SRC_A0 = (short) val;
        case H_Reverb + 34 -> rvb.IIR_SRC_A1 = (short) val;
        case H_Reverb + 36 -> rvb.IIR_DEST_B0 = (short) val;
        case H_Reverb + 38 -> rvb.IIR_DEST_B1 = (short) val;
        case H_Reverb + 40 -> rvb.ACC_SRC_C0 = (short) val;
        case H_Reverb + 42 -> rvb.ACC_SRC_C1 = (short) val;
        case H_Reverb + 44 -> rvb.ACC_SRC_D0 = (short) val;
        case H_Reverb + 46 -> rvb.ACC_SRC_D1 = (short) val;
        case H_Reverb + 48 -> rvb.IIR_SRC_B1 = (short) val;
        case H_Reverb + 50 -> rvb.IIR_SRC_B0 = (short) val;
        case H_Reverb + 52 -> rvb.MIX_DEST_A0 = (short) val;
        case H_Reverb + 54 -> rvb.MIX_DEST_A1 = (short) val;
        case H_Reverb + 56 -> rvb.MIX_DEST_B0 = (short) val;
        case H_Reverb + 58 -> rvb.MIX_DEST_B1 = (short) val;
        case H_Reverb + 60 -> rvb.IN_COEF_L = (short) val;
        case H_Reverb + 62 -> rvb.IN_COEF_R = (short) val;
        default -> { /* nothing */ }
        }
    }

    @Override
    public int readRegister(int reg) {
        int r = reg & 0xfff;

        if (r >= 0x0c00 && r < 0x0d80) {
            int ch = (r >> 4) - 0xc0;
            Channel c = chan[ch];
            switch (r & 0x0f) {
            case 0xC: { // the current envelope volume
                if (c.bNew) {
                    return 1; // started but not processed yet
                }
                if (c.lVolume != 0 && c.envelopeVol == 0) {
                    return 1; // no sample decoded yet, so no envelope yet
                }
                return (c.envelopeVol >>> 16) & 0xffff;
            }
            case 0xE: // the loop address
                return (c.pLoop >> 3) & 0xffff;
            default:
                break;
            }
        }

        switch (r) {
        case H_SPUctrl:
            return spuCtrl;
        case H_SPUstat:
            return spuStat;
        case H_SPUaddr:
            return (spuAddr >> 3) & 0xffff;
        case H_SPUdata: {
            int s = spuMem[(spuAddr >> 1) & 0x3ffff] & 0xffff;
            spuAddr += 2;
            if (Integer.compareUnsigned(spuAddr, 0x7ffff) > 0) {
                spuAddr = 0;
            }
            return s;
        }
        case H_SPUirqAddr:
            return spuIrq;
        default:
            break;
        }

        return regArea[(r - 0xc00) >> 1];
    }

    private void soundOn(int start, int end, int val) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            // the start address has to be set before the key on
            if ((val & 1) != 0 && chan[ch].pStart != 0) {
                chan[ch].bIgnoreLoop = false;
                chan[ch].bNew = true;
            }
        }
    }

    private void soundOff(int start, int end, int val) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            if ((val & 1) != 0) {
                chan[ch].bStop = true;
            }
        }
    }

    private void fModOn(int start, int end, int val) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            if ((val & 1) != 0) {
                if (ch > 0) {
                    chan[ch].bFMod = 1;     // the modulated channel
                    chan[ch - 1].bFMod = 2; // the modulating one
                }
            } else {
                chan[ch].bFMod = 0;
            }
        }
    }

    private void noiseOn(int start, int end, int val) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            chan[ch].bNoise = (val & 1) != 0;
        }
    }

    /** the sweep is not emulated, the volume is just moved half way instead */
    private void setVolumeLR(boolean right, int ch, short volIn) {
        int vol = volIn;

        if (right) {
            chan[ch].iRightVolRaw = vol;
        } else {
            chan[ch].iLeftVolRaw = vol;
        }

        if ((vol & 0x8000) != 0) { // a sweep
            int sInc = 1;
            if ((vol & 0x2000) != 0) {
                sInc = -1;
            }
            if ((vol & 0x1000) != 0) {
                vol = (short) (vol ^ 0xffff);
            }
            vol = (short) (((vol & 0x7f) + 1) / 2); // 0..127 -> 0..64
            vol += vol / (2 * sInc);
            vol *= 128;
            vol &= 0x3fff;
        } else {
            if ((vol & 0x4000) != 0) {
                vol = (vol & 0x3FFF) - 0x4000;
            } else {
                vol &= 0x3FFF;
            }
        }

        if (right) {
            chan[ch].iRightVolume = vol;
        } else {
            chan[ch].iLeftVolume = vol;
        }
    }

    private void setPitch(int ch, int val) {
        int NP = Math.min(val, 0x3fff);

        chan[ch].iRawPitch = NP;

        NP = (44100 * NP) / 4096;
        if (NP < 1) {
            NP = 1;
        }
        chan[ch].iActFreq = NP;
    }

    // ---- dma ----

    @Override
    public void readDMAMem(int psxAddress, int size) {
        for (int i = 0; i < size; i++) {
            setRamHalf(psxAddress, spuMem[(spuAddr >> 1) & 0x3ffff] & 0xffff);
            psxAddress += 2;
            spuAddr += 2;
            if (Integer.compareUnsigned(spuAddr, 0x7ffff) > 0) {
                spuAddr = 0;
            }
        }
    }

    @Override
    public void writeDMAMem(int psxAddress, int size) {
        for (int i = 0; i < size; i++) {
            spuMem[(spuAddr >> 1) & 0x3ffff] = (short) ramHalf(psxAddress);
            psxAddress += 2;
            spuAddr += 2;
            if (Integer.compareUnsigned(spuAddr, 0x7ffff) > 0) {
                spuAddr = 0;
            }
        }
    }
}
