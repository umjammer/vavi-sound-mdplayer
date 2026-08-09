/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;

import java.util.Arrays;


/**
 * The PS2 sound processing unit: two PS1 style cores, 24 voices each, with a reverb unit per core.
 * <p>
 * ported from aosdk eng_psf/peops2, which is Pete Bernert's PEOpS SPU2 with Neill Corlett's
 * envelope and reverb models. That code is GPL v2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class PeopsSpu2 implements Spu2, SpuVoices {

    private static final int MAXCHAN = 48;

    // the PS2 register map, per core
    private static final int PS2_C0_SPUaddr_Hi = 0x000 + 0x1A8;
    private static final int PS2_C0_SPUaddr_Lo = 0x000 + 0x1AA;
    private static final int PS2_C1_SPUaddr_Hi = 0x400 + 0x1A8;
    private static final int PS2_C1_SPUaddr_Lo = 0x400 + 0x1AA;
    private static final int PS2_C0_SPUdata = 0x000 + 0x1AC;
    private static final int PS2_C1_SPUdata = 0x400 + 0x1AC;
    private static final int PS2_C0_SPUstat = 0x000 + 0x344;
    private static final int PS2_C1_SPUstat = 0x400 + 0x344;
    private static final int PS2_C0_ReverbAddr_Hi = 0x000 + 0x2E0;
    private static final int PS2_C0_ReverbAddr_Lo = 0x000 + 0x2E2;
    private static final int PS2_C1_ReverbAddr_Hi = 0x400 + 0x2E0;
    private static final int PS2_C1_ReverbAddr_Lo = 0x400 + 0x2E2;
    private static final int PS2_C0_ReverbAEnd_Hi = 0x000 + 0x33C;
    private static final int PS2_C1_ReverbAEnd_Hi = 0x400 + 0x33C;
    private static final int PS2_C0_DryL1 = 0x000 + 0x188;
    private static final int PS2_C1_DryL1 = 0x400 + 0x188;
    private static final int PS2_C0_DryL2 = 0x000 + 0x18A;
    private static final int PS2_C1_DryL2 = 0x400 + 0x18A;
    private static final int PS2_C0_DryR1 = 0x000 + 0x190;
    private static final int PS2_C1_DryR1 = 0x400 + 0x190;
    private static final int PS2_C0_DryR2 = 0x000 + 0x192;
    private static final int PS2_C1_DryR2 = 0x400 + 0x192;
    private static final int PS2_C0_ATTR = 0x000 + 0x19A;
    private static final int PS2_C1_ATTR = 0x400 + 0x19A;
    private static final int PS2_C0_ADMAS = 0x000 + 0x1B0;
    private static final int PS2_C1_ADMAS = 0x400 + 0x1B0;
    private static final int PS2_C0_SPUirqAddr_Hi = 0x000 + 0x19C;
    private static final int PS2_C0_SPUirqAddr_Lo = 0x000 + 0x19D;
    private static final int PS2_C1_SPUirqAddr_Hi = 0x400 + 0x19C;
    private static final int PS2_C1_SPUirqAddr_Lo = 0x400 + 0x19D;
    private static final int PS2_C0_SPUrvolL = 0x000 + 0x764;
    private static final int PS2_C0_SPUrvolR = 0x000 + 0x766;
    private static final int PS2_C1_SPUrvolL = 0x028 + 0x764;
    private static final int PS2_C1_SPUrvolR = 0x028 + 0x766;
    private static final int PS2_C0_SPUon1 = 0x000 + 0x1A0;
    private static final int PS2_C0_SPUon2 = 0x000 + 0x1A2;
    private static final int PS2_C1_SPUon1 = 0x400 + 0x1A0;
    private static final int PS2_C1_SPUon2 = 0x400 + 0x1A2;
    private static final int PS2_C0_SPUoff1 = 0x000 + 0x1A4;
    private static final int PS2_C0_SPUoff2 = 0x000 + 0x1A6;
    private static final int PS2_C1_SPUoff1 = 0x400 + 0x1A4;
    private static final int PS2_C1_SPUoff2 = 0x400 + 0x1A6;
    private static final int PS2_C0_FMod1 = 0x000 + 0x180;
    private static final int PS2_C0_FMod2 = 0x000 + 0x182;
    private static final int PS2_C1_FMod1 = 0x400 + 0x180;
    private static final int PS2_C1_FMod2 = 0x400 + 0x182;
    private static final int PS2_C0_Noise1 = 0x000 + 0x184;
    private static final int PS2_C0_Noise2 = 0x000 + 0x186;
    private static final int PS2_C1_Noise1 = 0x400 + 0x184;
    private static final int PS2_C1_Noise2 = 0x400 + 0x186;
    private static final int PS2_C0_RVBon1_L = 0x000 + 0x18C;
    private static final int PS2_C0_RVBon2_L = 0x000 + 0x18E;
    private static final int PS2_C0_RVBon1_R = 0x000 + 0x194;
    private static final int PS2_C0_RVBon2_R = 0x000 + 0x196;
    private static final int PS2_C1_RVBon1_L = 0x400 + 0x18C;
    private static final int PS2_C1_RVBon2_L = 0x400 + 0x18E;
    private static final int PS2_C1_RVBon1_R = 0x400 + 0x194;
    private static final int PS2_C1_RVBon2_R = 0x400 + 0x196;
    private static final int PS2_C0_Reverb = 0x000 + 0x2E4;
    private static final int PS2_C1_Reverb = 0x400 + 0x2E4;
    private static final int PS2_C0_ReverbX = 0x000 + 0x774;
    private static final int PS2_C1_ReverbX = 0x028 + 0x774;
    private static final int PS2_C0_SPUend1 = 0x000 + 0x340;
    private static final int PS2_C0_SPUend2 = 0x000 + 0x342;
    private static final int PS2_C1_SPUend1 = 0x400 + 0x340;
    private static final int PS2_C1_SPUend2 = 0x400 + 0x342;

    /** what pCurr holds once a voice has run off the end of its sample */
    private static final int STOP = -1;

    /** see {@link PeopsSpu}, the table is padded so a bad predictor cannot read past its end */
    private static final int[][] f = {
            {0, 0}, {60, 0}, {115, -52}, {98, -55}, {122, -60},
            {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0},
            {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0},
    };

    private static final int[] rateTable = new int[160];

    static {
        int r = 3, rs = 1, rd = 0;
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

    private static class Channel {
        boolean bNew;

        int iSBPos;
        int spos;
        int sinc;
        final int[] SB = new int[32 + 1];
        int sval;

        int pStart;
        int pCurr;
        int pLoop;

        int iStartAdr;
        int iLoopAdr;
        int iNextAdr;

        boolean bOn;
        boolean bStop;
        int iActFreq;
        int iUsedFreq;
        int iLeftVolume;
        int iLeftVolRaw;
        boolean bIgnoreLoop;
        int iRightVolume;
        int iRightVolRaw;
        int iRawPitch;
        boolean iIrqDone;
        int s_1;
        int s_2;
        int bReverbL;
        int bReverbR;
        boolean bRVBActive;
        int bVolumeL;
        int bVolumeR;
        boolean bNoise;
        int bFMod;
        int iOldNoise;
        boolean iMute;

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
            Arrays.fill(SB, 0);
            pStart = pCurr = pLoop = 0;
            iStartAdr = iLoopAdr = iNextAdr = 0;
            bOn = bStop = false;
            iActFreq = iUsedFreq = 0;
            iLeftVolume = iLeftVolRaw = iRightVolume = iRightVolRaw = 0;
            bIgnoreLoop = false;
            iRawPitch = 0;
            iIrqDone = false;
            s_1 = s_2 = 0;
            bReverbL = bReverbR = bVolumeL = bVolumeR = 0;
            bRVBActive = false;
            bNoise = false;
            bFMod = 0;
            iOldNoise = 0;
            iMute = false;
            state = 0;
            attackModeExp = sustainModeExp = sustainIncrease = releaseModeExp = false;
            attackRate = decayRate = sustainLevel = sustainRate = releaseRate = 0;
            envelopeVol = 0;
            lVolume = 0;
        }
    }

    /** one of the two reverb units. Its address fields are 20 bit, unlike the PS1's */
    private static class Reverb {
        int StartAddr;
        int EndAddr;
        int CurrAddr;
        int iCnt;

        int VolLeft;
        int VolRight;
        int iLastRVBLeft;
        int iLastRVBRight;
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
            StartAddr = EndAddr = CurrAddr = iCnt = 0;
            VolLeft = VolRight = iLastRVBLeft = iLastRVBRight = iRVBLeft = iRVBRight = 0;
            FB_SRC_A = FB_SRC_B = IIR_ALPHA = ACC_COEF_A = ACC_COEF_B = ACC_COEF_C = 0;
            ACC_COEF_D = IIR_COEF = FB_ALPHA = FB_X = IIR_DEST_A0 = IIR_DEST_A1 = 0;
            ACC_SRC_A0 = ACC_SRC_A1 = ACC_SRC_B0 = ACC_SRC_B1 = IIR_SRC_A0 = IIR_SRC_A1 = 0;
            IIR_DEST_B0 = IIR_DEST_B1 = ACC_SRC_C0 = ACC_SRC_C1 = ACC_SRC_D0 = ACC_SRC_D1 = 0;
            IIR_SRC_B1 = IIR_SRC_B0 = MIX_DEST_A0 = MIX_DEST_A1 = MIX_DEST_B0 = MIX_DEST_B1 = 0;
            IN_COEF_L = IN_COEF_R = 0;
        }
    }

    /** 2 MB of sound ram, as the 16 bit words the hardware addresses */
    private final short[] spuMem = new short[1024 * 1024];
    private final short[] regArea = new short[32 * 1024];

    /** channel 48 exists only so a frequency modulator on channel 47 has somewhere to write */
    private final Channel[] chan = new Channel[MAXCHAN + 1];
    private final Reverb[] rvb = {new Reverb(), new Reverb()};

    private int iVolume = 3;

    private int dwNoiseVal = 1;
    private final int[] spuCtrl2 = new int[2];
    private final int[] spuStat2 = new int[2];
    private final int[] spuIrq2 = new int[2];
    private final int[] spuAddr2 = new int[2];
    private final int[] spuRvbAddr2 = new int[2];
    private final int[] spuRvbAEnd2 = new int[2];
    private final int[] pSpuIrq = new int[2];
    private final int[] dwNewChannel2 = new int[2];
    private final int[] dwEndChannel2 = new int[2];
    private boolean bSPUIsOpen;

    /** the per core reverb input, one stereo sample each */
    private final int[][] sRVB = new int[2][2];

    private int ssumL, ssumR;

    /** where an SPU irq interrupted the channel loop, so the next call can pick it up */
    private int lastch = -1;

    private int[] psxRam;

    /** key ons per voice, which is how a view spots a note being struck */
    private final int[] keyOns = new int[MAXCHAN + 1];

    /** the last sample {@link #sample()} produced */
    public int left;
    public int right;

    public PeopsSpu2() {
        for (int i = 0; i < chan.length; i++) {
            chan[i] = new Channel();
        }
    }

    public void setPsxRam(int[] psxRam) {
        this.psxRam = psxRam;
    }

    // ---- sound ram ----

    private int spuByte(int byteOffset) {
        int w = spuMem[(byteOffset >> 1) & 0xfffff] & 0xffff;
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
        rvb[0].clear();
        rvb[1].clear();
        Arrays.fill(keyOns, 0);
    }

    public void open() {
        if (bSPUIsOpen) {
            return;
        }
        iVolume = 3;
        Arrays.fill(keyOns, 0);
        for (Channel c : chan) {
            c.clear();
        }
        pSpuIrq[0] = pSpuIrq[1] = 0;
        dwNewChannel2[0] = dwNewChannel2[1] = 0;
        dwEndChannel2[0] = dwEndChannel2[1] = 0;
        spuCtrl2[0] = spuCtrl2[1] = 0;
        spuStat2[0] = spuStat2[1] = 0;
        spuIrq2[0] = spuIrq2[1] = 0;
        spuAddr2[0] = spuAddr2[1] = 0xffffffff;
        spuRvbAddr2[0] = spuRvbAddr2[1] = 0;
        spuRvbAEnd2[0] = spuRvbAEnd2[1] = 0;

        sRVB[0][0] = sRVB[0][1] = 0;
        sRVB[1][0] = sRVB[1][1] = 0;
        ssumL = ssumR = 0;
        lastch = -1;

        for (int i = 0; i < MAXCHAN; i++) {
            chan[i].sustainLevel = 1024;
            chan[i].iMute = false;
            chan[i].iIrqDone = false;
            chan[i].pLoop = 0;
            chan[i].pStart = 0;
            chan[i].pCurr = 0;
        }

        bSPUIsOpen = true;
    }

    public void close() {
        bSPUIsOpen = false;
    }

    // ---- envelope, the same model the PS1 uses ----

    private static void startADSR(Channel c) {
        c.lVolume = 1;
        c.state = 0;
        c.envelopeVol = 0;
    }

    private static int mixADSR(Channel c) {
        if (c.bStop) {
            if (c.releaseModeExp) {
                c.envelopeVol -= rateTable[(4 * (c.releaseRate ^ 0x1F)) - 0x18 + 32
                        + sexyTable[(c.envelopeVol >>> 28) & 0x7]];
            } else {
                c.envelopeVol -= rateTable[(4 * (c.releaseRate ^ 0x1F)) - 0x0C + 32];
            }

            if (c.envelopeVol < 0) {
                c.envelopeVol = 0;
                c.bOn = false;
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

    private void startReverb(int ch) {
        int core = ch / 24;
        if ((chan[ch].bReverbL != 0 || chan[ch].bReverbR != 0) && (spuCtrl2[core] & 0x80) != 0) {
            chan[ch].bRVBActive = true;
        } else {
            chan[ch].bRVBActive = false;
        }
    }

    private void initReverb() {
        sRVB[0][0] = sRVB[0][1] = 0;
        sRVB[1][0] = sRVB[1][1] = 0;
    }

    private void storeReverb(int ch) {
        int core = ch / 24;
        Channel c = chan[ch];
        int iRxl = (c.sval * c.iLeftVolume * c.bReverbL) / 0x4000;
        int iRxr = (c.sval * c.iRightVolume * c.bReverbR) / 0x4000;

        sRVB[core][0] += iRxl;
        sRVB[core][1] += iRxr;
    }

    private int gBuffer(int iOff, int core) {
        iOff = iOff + rvb[core].CurrAddr;
        while (iOff > rvb[core].EndAddr) {
            iOff = rvb[core].StartAddr + (iOff - (rvb[core].EndAddr + 1));
        }
        while (iOff < rvb[core].StartAddr) {
            iOff = rvb[core].EndAddr - (rvb[core].StartAddr - iOff);
        }
        return spuMem[iOff & 0xfffff];
    }

    private void sBuffer(int iOff, int iVal, int core) {
        iOff = iOff + rvb[core].CurrAddr;
        while (iOff > rvb[core].EndAddr) {
            iOff = rvb[core].StartAddr + (iOff - (rvb[core].EndAddr + 1));
        }
        while (iOff < rvb[core].StartAddr) {
            iOff = rvb[core].EndAddr - (rvb[core].StartAddr - iOff);
        }
        if (iVal < -32768) iVal = -32768;
        if (iVal > 32767) iVal = 32767;
        spuMem[iOff & 0xfffff] = (short) iVal;
    }

    private void sBuffer1(int iOff, int iVal, int core) {
        iOff = iOff + rvb[core].CurrAddr + 1;
        while (iOff > rvb[core].EndAddr) {
            iOff = rvb[core].StartAddr + (iOff - (rvb[core].EndAddr + 1));
        }
        while (iOff < rvb[core].StartAddr) {
            iOff = rvb[core].EndAddr - (rvb[core].StartAddr - iOff);
        }
        if (iVal < -32768) iVal = -32768;
        if (iVal > 32767) iVal = 32767;
        spuMem[iOff & 0xfffff] = (short) iVal;
    }

    private int mixReverbLeft(int core) {
        Reverb v = rvb[core];

        if (v.StartAddr == 0 || v.EndAddr == 0 || v.StartAddr >= v.EndAddr) { // reverb is off
            v.iLastRVBLeft = v.iLastRVBRight = v.iRVBLeft = v.iRVBRight = 0;
            return 0;
        }

        v.iCnt++;

        // every second sample, i.e. 22 kHz
        if ((v.iCnt & 1) != 0) {
            if ((spuCtrl2[core] & 0x80) != 0) {
                int INPUT_SAMPLE_L = sRVB[core][0];
                int INPUT_SAMPLE_R = sRVB[core][1];

                int IIR_INPUT_A0 = (gBuffer(v.IIR_SRC_A0, core) * v.IIR_COEF) / 32768
                        + (INPUT_SAMPLE_L * v.IN_COEF_L) / 32768;
                int IIR_INPUT_A1 = (gBuffer(v.IIR_SRC_A1, core) * v.IIR_COEF) / 32768
                        + (INPUT_SAMPLE_R * v.IN_COEF_R) / 32768;
                int IIR_INPUT_B0 = (gBuffer(v.IIR_SRC_B0, core) * v.IIR_COEF) / 32768
                        + (INPUT_SAMPLE_L * v.IN_COEF_L) / 32768;
                int IIR_INPUT_B1 = (gBuffer(v.IIR_SRC_B1, core) * v.IIR_COEF) / 32768
                        + (INPUT_SAMPLE_R * v.IN_COEF_R) / 32768;

                int IIR_A0 = (IIR_INPUT_A0 * v.IIR_ALPHA) / 32768
                        + (gBuffer(v.IIR_DEST_A0, core) * (32768 - v.IIR_ALPHA)) / 32768;
                int IIR_A1 = (IIR_INPUT_A1 * v.IIR_ALPHA) / 32768
                        + (gBuffer(v.IIR_DEST_A1, core) * (32768 - v.IIR_ALPHA)) / 32768;
                int IIR_B0 = (IIR_INPUT_B0 * v.IIR_ALPHA) / 32768
                        + (gBuffer(v.IIR_DEST_B0, core) * (32768 - v.IIR_ALPHA)) / 32768;
                int IIR_B1 = (IIR_INPUT_B1 * v.IIR_ALPHA) / 32768
                        + (gBuffer(v.IIR_DEST_B1, core) * (32768 - v.IIR_ALPHA)) / 32768;

                sBuffer1(v.IIR_DEST_A0, IIR_A0, core);
                sBuffer1(v.IIR_DEST_A1, IIR_A1, core);
                sBuffer1(v.IIR_DEST_B0, IIR_B0, core);
                sBuffer1(v.IIR_DEST_B1, IIR_B1, core);

                int ACC0 = (gBuffer(v.ACC_SRC_A0, core) * v.ACC_COEF_A) / 32768
                        + (gBuffer(v.ACC_SRC_B0, core) * v.ACC_COEF_B) / 32768
                        + (gBuffer(v.ACC_SRC_C0, core) * v.ACC_COEF_C) / 32768
                        + (gBuffer(v.ACC_SRC_D0, core) * v.ACC_COEF_D) / 32768;
                int ACC1 = (gBuffer(v.ACC_SRC_A1, core) * v.ACC_COEF_A) / 32768
                        + (gBuffer(v.ACC_SRC_B1, core) * v.ACC_COEF_B) / 32768
                        + (gBuffer(v.ACC_SRC_C1, core) * v.ACC_COEF_C) / 32768
                        + (gBuffer(v.ACC_SRC_D1, core) * v.ACC_COEF_D) / 32768;

                int FB_A0 = gBuffer(v.MIX_DEST_A0 - v.FB_SRC_A, core);
                int FB_A1 = gBuffer(v.MIX_DEST_A1 - v.FB_SRC_A, core);
                int FB_B0 = gBuffer(v.MIX_DEST_B0 - v.FB_SRC_B, core);
                int FB_B1 = gBuffer(v.MIX_DEST_B1 - v.FB_SRC_B, core);

                sBuffer(v.MIX_DEST_A0, ACC0 - (FB_A0 * v.FB_ALPHA) / 32768, core);
                sBuffer(v.MIX_DEST_A1, ACC1 - (FB_A1 * v.FB_ALPHA) / 32768, core);

                sBuffer(v.MIX_DEST_B0, (v.FB_ALPHA * ACC0) / 32768
                        - (FB_A0 * (v.FB_ALPHA ^ 0xFFFF8000)) / 32768
                        - (FB_B0 * v.FB_X) / 32768, core);
                sBuffer(v.MIX_DEST_B1, (v.FB_ALPHA * ACC1) / 32768
                        - (FB_A1 * (v.FB_ALPHA ^ 0xFFFF8000)) / 32768
                        - (FB_B1 * v.FB_X) / 32768, core);

                v.iLastRVBLeft = v.iRVBLeft;
                v.iLastRVBRight = v.iRVBRight;

                v.iRVBLeft = (gBuffer(v.MIX_DEST_A0, core) + gBuffer(v.MIX_DEST_B0, core)) / 3;
                v.iRVBRight = (gBuffer(v.MIX_DEST_A1, core) + gBuffer(v.MIX_DEST_B1, core)) / 3;

                v.iRVBLeft = (v.iRVBLeft * v.VolLeft) / 0x4000;
                v.iRVBRight = (v.iRVBRight * v.VolRight) / 0x4000;

                v.CurrAddr++;
                if (v.CurrAddr > v.EndAddr) {
                    v.CurrAddr = v.StartAddr;
                }

                return v.iLastRVBLeft + (v.iRVBLeft - v.iLastRVBLeft) / 2;
            } else { // reverb off
                v.iLastRVBLeft = v.iLastRVBRight = v.iRVBLeft = v.iRVBRight = 0;
            }

            v.CurrAddr++;
            if (v.CurrAddr > v.EndAddr) {
                v.CurrAddr = v.StartAddr;
            }
        }

        return v.iLastRVBLeft;
    }

    private int mixReverbRight(int core) {
        Reverb v = rvb[core];
        int i = v.iLastRVBRight + (v.iRVBRight - v.iLastRVBRight) / 2;
        v.iLastRVBRight = v.iRVBRight;
        return i;
    }

    // ---- the gauss interpolation window, which lives packed as shorts inside SB[29] and SB[30] ----

    private static int gval(Channel c, int i) {
        return (short) ((c.SB[29 + (i >> 1)] >>> ((i & 1) * 16)) & 0xffff);
    }

    private static void setGval(Channel c, int i, int v) {
        int idx = 29 + (i >> 1);
        int shift = (i & 1) * 16;
        c.SB[idx] = (c.SB[idx] & ~(0xffff << shift)) | ((v & 0xffff) << shift);
    }

    // ---- mixing ----

    private void startSound(int ch) {
        Channel c = chan[ch];
        keyOns[ch]++;

        dwNewChannel2[ch / 24] &= ~(1 << (ch % 24));
        dwEndChannel2[ch / 24] &= ~(1 << (ch % 24));

        startADSR(c);
        startReverb(ch);

        c.pCurr = c.pStart;

        c.s_1 = 0;
        c.s_2 = 0;
        c.iSBPos = 28;

        c.bNew = false;
        c.bStop = false;
        c.bOn = true;

        c.SB[29] = 0;
        c.SB[30] = 0;

        // gauss interpolation, so decoding starts a little earlier
        c.spos = 0x30000;
        c.SB[28] = 0;
    }

    /** produces one stereo sample into {@link #left} and {@link #right} */
    public void sample() {
        int voldiv = iVolume;

        int chStart = 0;
        boolean goon = false;
        if (lastch >= 0) { // an SPU irq stopped the last call part way through
            chStart = lastch;
            lastch = -1;
            goon = true;
        }

        for (int ch = chStart; ch < MAXCHAN; ch++) {
            Channel c = chan[ch];
            int fa = 0;
            boolean ended = false;

            if (!goon) {
                if (c.bNew) {
                    startSound(ch);
                }
                if (!c.bOn) {
                    continue;
                }

                if (c.iActFreq != c.iUsedFreq) {
                    c.iUsedFreq = c.iActFreq;
                    c.sinc = c.iRawPitch << 4;
                    if (c.sinc == 0) {
                        c.sinc = 1;
                    }
                }
            }

            while (goon || c.spos >= 0x10000) {
                if (!goon && c.iSBPos == 28) {
                    int start = c.pCurr;

                    if (start == STOP) {
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

                    boolean irqReturn = false;
                    if ((spuCtrl2[ch / 24] & 0x40) != 0) {
                        if ((pSpuIrq[ch / 24] > start - 16 && pSpuIrq[ch / 24] <= start)
                                || ((flags & 1) != 0
                                    && pSpuIrq[ch / 24] > c.pLoop - 16 && pSpuIrq[ch / 24] <= c.pLoop)) {
                            c.iIrqDone = true;

                            if (ch < 24) {
                                interruptDMA4();
                            } else {
                                interruptDMA7();
                            }

                            irqReturn = true; // iSPUIRQWait
                        }
                    }

                    if ((flags & 4) != 0 && !c.bIgnoreLoop) {
                        c.pLoop = start - 16;
                    }

                    if ((flags & 1) != 0) {
                        dwEndChannel2[ch / 24] |= (1 << (ch % 24));

                        if (flags != 3 || c.pLoop == STOP) {
                            start = STOP;
                        } else {
                            start = c.pLoop;
                        }
                    }

                    c.pCurr = start;
                    c.s_1 = s_1;
                    c.s_2 = s_2;

                    if (irqReturn) {
                        // the caller keeps the sample it had; the next call picks up right here
                        lastch = ch;
                        return;
                    }
                }
                goon = false;

                fa = c.SB[c.iSBPos++];

                if (fa > 32767) fa = 32767;
                if (fa < -32767) fa = -32767;

                int gpos = c.SB[28];
                setGval(c, gpos, fa);
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

                fa = c.iOldNoise
                        + ((fa - c.iOldNoise) / ((0x001f - ((spuCtrl2[ch / 24] & 0x3f00) >> 9)) + 1));
                if (fa > 32767) fa = 32767;
                if (fa < -32767) fa = -32767;
                c.iOldNoise = fa;
            } else {
                int vl = (c.spos >> 6) & ~3;
                int gpos = c.SB[28];
                int vr = (Gauss.table[vl] * gval(c, gpos)) & ~2047;
                vr += (Gauss.table[vl + 1] * gval(c, (gpos + 1) & 3)) & ~2047;
                vr += (Gauss.table[vl + 2] * gval(c, (gpos + 2) & 3)) & ~2047;
                vr += (Gauss.table[vl + 3] * gval(c, (gpos + 3) & 3)) & ~2047;
                fa = vr >> 11;
            }

            c.sval = (mixADSR(c) * fa) / 1023;

            if (c.bFMod == 2) { // this channel modulates the next one's frequency
                int NP = chan[ch + 1].iRawPitch;
                NP = ((32768 + c.sval) * NP) / 32768;

                if (NP > 0x3fff) NP = 0x3fff;
                if (NP < 0x1) NP = 0x1;

                double intr = 48000.0f / 44100.0f * NP;
                NP = (int) intr;

                NP = (44100 * NP) / 4096;

                chan[ch + 1].iActFreq = NP;
                chan[ch + 1].iUsedFreq = NP;
                chan[ch + 1].sinc = ((NP / 10) << 16) / 4410;
                if (chan[ch + 1].sinc == 0) {
                    chan[ch + 1].sinc = 1;
                }
            } else {
                if (c.iMute) {
                    c.sval = 0;
                } else {
                    if (c.bVolumeL != 0) {
                        ssumL += (c.sval * c.iLeftVolume) / 0x4000;
                    }
                    if (c.bVolumeR != 0) {
                        ssumR += (c.sval * c.iRightVolume) / 0x4000;
                    }
                }

                if (c.bRVBActive) {
                    storeReverb(ch);
                }
            }

            c.spos += c.sinc;
        }

        ssumL += mixReverbLeft(0);
        ssumL += mixReverbLeft(1);
        ssumR += mixReverbRight(0);
        ssumR += mixReverbRight(1);

        int d = ssumL / voldiv;
        ssumL = 0;
        int d2 = ssumR / voldiv;
        ssumR = 0;

        if (d < -32767) d = -32767;
        if (d > 32767) d = 32767;
        if (d2 < -32767) d2 = -32767;
        if (d2 > 32767) d2 = 32767;

        left = d;
        right = d2;

        initReverb();
    }

    // ---- registers ----

    @Override
    public void write(int reg, int val) {
        val &= 0xffff;
        int r = reg & 0xffff;

        regArea[r >> 1] = (short) val;

        if ((r >= 0x0000 && r < 0x0180) || (r >= 0x0400 && r < 0x0580)) { // a channel register
            int ch = (r >> 4) & 0x1f;
            if (r >= 0x400) {
                ch += 24;
            }
            Channel c = chan[ch];

            switch (r & 0x0f) {
            case 0 -> setVolumeL(ch, (short) val);
            case 2 -> setVolumeR(ch, (short) val);
            case 4 -> setPitch(ch, val);
            case 6 -> {
                c.attackModeExp = (val & 0x8000) != 0;
                c.attackRate = (val >> 8) & 0x007f;
                c.decayRate = (val >> 4) & 0x000f;
                c.sustainLevel = val & 0x000f;
            }
            case 8 -> {
                c.sustainModeExp = (val & 0x8000) != 0;
                c.sustainIncrease = (val & 0x4000) == 0;
                c.sustainRate = (val >> 6) & 0x007f;
                c.releaseModeExp = (val & 0x0020) != 0;
                c.releaseRate = val & 0x001f;
            }
            default -> { /* nothing */ }
            }
            return;
        }

        if ((r >= 0x01c0 && r < 0x02E0) || (r >= 0x05c0 && r < 0x06E0)) { // a channel address
            int ch = 0;
            int rx = r;
            if (rx >= 0x400) {
                ch = 24;
                rx -= 0x400;
            }
            ch += (rx - 0x1c0) / 12;
            rx -= (ch % 24) * 12;

            Channel c = chan[ch];
            switch (rx) {
            case 0x1C0 -> {
                c.iStartAdr = ((val & 0xf) << 16) | (c.iStartAdr & 0xFFFF);
                c.pStart = c.iStartAdr << 1;
            }
            case 0x1C2 -> {
                c.iStartAdr = (c.iStartAdr & 0xF0000) | (val & 0xFFFF);
                c.pStart = c.iStartAdr << 1;
            }
            case 0x1C4 -> {
                c.iLoopAdr = ((val & 0xf) << 16) | (c.iLoopAdr & 0xFFFF);
                c.pLoop = c.iLoopAdr << 1;
                c.bIgnoreLoop = true;
            }
            case 0x1C6 -> {
                c.iLoopAdr = (c.iLoopAdr & 0xF0000) | (val & 0xFFFF);
                c.pLoop = c.iLoopAdr << 1;
                c.bIgnoreLoop = true;
            }
            case 0x1C8 -> c.iNextAdr = ((val & 0xf) << 16) | (c.iNextAdr & 0xFFFF);
            case 0x1CA -> c.iNextAdr = (c.iNextAdr & 0xF0000) | (val & 0xFFFF);
            default -> { /* nothing */ }
            }
            return;
        }

        // the two reverb blocks are laid out the same, so they are set by index
        if (r >= PS2_C0_Reverb && r < PS2_C0_Reverb + 88) {
            setReverbAddress(0, r - PS2_C0_Reverb, val);
            return;
        }
        if (r >= PS2_C1_Reverb && r < PS2_C1_Reverb + 88) {
            setReverbAddress(1, r - PS2_C1_Reverb, val);
            return;
        }
        if (r >= PS2_C0_ReverbX && r < PS2_C0_ReverbX + 20) {
            setReverbCoefficient(0, r - PS2_C0_ReverbX, (short) val);
            return;
        }
        if (r >= PS2_C1_ReverbX && r < PS2_C1_ReverbX + 20) {
            setReverbCoefficient(1, r - PS2_C1_ReverbX, (short) val);
            return;
        }

        switch (r) {
        case PS2_C0_SPUaddr_Hi -> spuAddr2[0] = ((val & 0xf) << 16) | (spuAddr2[0] & 0xFFFF);
        case PS2_C0_SPUaddr_Lo -> spuAddr2[0] = (spuAddr2[0] & 0xF0000) | (val & 0xFFFF);
        case PS2_C1_SPUaddr_Hi -> spuAddr2[1] = ((val & 0xf) << 16) | (spuAddr2[1] & 0xFFFF);
        case PS2_C1_SPUaddr_Lo -> spuAddr2[1] = (spuAddr2[1] & 0xF0000) | (val & 0xFFFF);
        case PS2_C0_SPUdata -> {
            spuMem[spuAddr2[0] & 0xfffff] = (short) val;
            spuAddr2[0]++;
            if (Integer.compareUnsigned(spuAddr2[0], 0xfffff) > 0) {
                spuAddr2[0] = 0;
            }
        }
        case PS2_C1_SPUdata -> {
            spuMem[spuAddr2[1] & 0xfffff] = (short) val;
            spuAddr2[1]++;
            if (Integer.compareUnsigned(spuAddr2[1], 0xfffff) > 0) {
                spuAddr2[1] = 0;
            }
        }
        case PS2_C0_ATTR -> spuCtrl2[0] = val;
        case PS2_C1_ATTR -> spuCtrl2[1] = val;
        case PS2_C0_SPUstat -> spuStat2[0] = val;
        case PS2_C1_SPUstat -> spuStat2[1] = val;
        case PS2_C0_ReverbAddr_Hi -> {
            spuRvbAddr2[0] = ((val & 0xf) << 16) | (spuRvbAddr2[0] & 0xFFFF);
            setReverbAddr(0);
        }
        case PS2_C0_ReverbAddr_Lo -> {
            spuRvbAddr2[0] = (spuRvbAddr2[0] & 0xF0000) | (val & 0xFFFF);
            setReverbAddr(0);
        }
        case PS2_C1_ReverbAddr_Hi -> {
            spuRvbAddr2[1] = ((val & 0xf) << 16) | (spuRvbAddr2[1] & 0xFFFF);
            setReverbAddr(1);
        }
        case PS2_C1_ReverbAddr_Lo -> {
            spuRvbAddr2[1] = (spuRvbAddr2[1] & 0xF0000) | (val & 0xFFFF);
            setReverbAddr(1);
        }
        case PS2_C0_ReverbAEnd_Hi -> {
            spuRvbAEnd2[0] = ((val & 0xf) << 16) | 0xFFFF;
            rvb[0].EndAddr = spuRvbAEnd2[0];
        }
        case PS2_C1_ReverbAEnd_Hi -> {
            spuRvbAEnd2[1] = ((val & 0xf) << 16) | 0xFFFF;
            rvb[1].EndAddr = spuRvbAEnd2[1];
        }
        case PS2_C0_SPUirqAddr_Hi -> {
            spuIrq2[0] = ((val & 0xf) << 16) | (spuIrq2[0] & 0xFFFF);
            pSpuIrq[0] = spuIrq2[0] << 1;
        }
        case PS2_C0_SPUirqAddr_Lo -> {
            spuIrq2[0] = (spuIrq2[0] & 0xF0000) | (val & 0xFFFF);
            pSpuIrq[0] = spuIrq2[0] << 1;
        }
        case PS2_C1_SPUirqAddr_Hi -> {
            spuIrq2[1] = ((val & 0xf) << 16) | (spuIrq2[1] & 0xFFFF);
            pSpuIrq[1] = spuIrq2[1] << 1;
        }
        case PS2_C1_SPUirqAddr_Lo -> {
            spuIrq2[1] = (spuIrq2[1] & 0xF0000) | (val & 0xFFFF);
            pSpuIrq[1] = spuIrq2[1] << 1;
        }
        case PS2_C0_SPUrvolL -> rvb[0].VolLeft = val;
        case PS2_C0_SPUrvolR -> rvb[0].VolRight = val;
        case PS2_C1_SPUrvolL -> rvb[1].VolLeft = val;
        case PS2_C1_SPUrvolR -> rvb[1].VolRight = val;
        case PS2_C0_SPUon1 -> soundOn(0, 16, val);
        case PS2_C0_SPUon2 -> soundOn(16, 24, val);
        case PS2_C1_SPUon1 -> soundOn(24, 40, val);
        case PS2_C1_SPUon2 -> soundOn(40, 48, val);
        case PS2_C0_SPUoff1 -> soundOff(0, 16, val);
        case PS2_C0_SPUoff2 -> soundOff(16, 24, val);
        case PS2_C1_SPUoff1 -> soundOff(24, 40, val);
        case PS2_C1_SPUoff2 -> soundOff(40, 48, val);
        case PS2_C0_SPUend1, PS2_C0_SPUend2 -> {
            if (val != 0) {
                dwEndChannel2[0] = 0;
            }
        }
        case PS2_C1_SPUend1, PS2_C1_SPUend2 -> {
            if (val != 0) {
                dwEndChannel2[1] = 0;
            }
        }
        case PS2_C0_FMod1 -> fModOn(0, 16, val);
        case PS2_C0_FMod2 -> fModOn(16, 24, val);
        case PS2_C1_FMod1 -> fModOn(24, 40, val);
        case PS2_C1_FMod2 -> fModOn(40, 48, val);
        case PS2_C0_Noise1 -> noiseOn(0, 16, val);
        case PS2_C0_Noise2 -> noiseOn(16, 24, val);
        case PS2_C1_Noise1 -> noiseOn(24, 40, val);
        case PS2_C1_Noise2 -> noiseOn(40, 48, val);
        case PS2_C0_DryL1 -> volumeOn(0, 16, val, false);
        case PS2_C0_DryL2 -> volumeOn(16, 24, val, false);
        case PS2_C1_DryL1 -> volumeOn(24, 40, val, false);
        case PS2_C1_DryL2 -> volumeOn(40, 48, val, false);
        case PS2_C0_DryR1 -> volumeOn(0, 16, val, true);
        case PS2_C0_DryR2 -> volumeOn(16, 24, val, true);
        case PS2_C1_DryR1 -> volumeOn(24, 40, val, true);
        case PS2_C1_DryR2 -> volumeOn(40, 48, val, true);
        case PS2_C0_RVBon1_L -> reverbOn(0, 16, val, false);
        case PS2_C0_RVBon2_L -> reverbOn(16, 24, val, false);
        case PS2_C1_RVBon1_L -> reverbOn(24, 40, val, false);
        case PS2_C1_RVBon2_L -> reverbOn(40, 48, val, false);
        case PS2_C0_RVBon1_R -> reverbOn(0, 16, val, true);
        case PS2_C0_RVBon2_R -> reverbOn(16, 24, val, true);
        case PS2_C1_RVBon1_R -> reverbOn(24, 40, val, true);
        case PS2_C1_RVBon2_R -> reverbOn(40, 48, val, true);
        default -> { /* nothing */ }
        }
    }

    /** the 20 bit reverb work addresses, written as a high and a low half each */
    private void setReverbAddress(int core, int offset, int val) {
        Reverb v = rvb[core];
        int index = offset / 4;
        boolean high = (offset & 2) == 0;

        int old = switch (index) {
            case 0 -> v.FB_SRC_A;
            case 1 -> v.FB_SRC_B;
            case 2 -> v.IIR_DEST_A0;
            case 3 -> v.IIR_DEST_A1;
            case 4 -> v.ACC_SRC_A0;
            case 5 -> v.ACC_SRC_A1;
            case 6 -> v.ACC_SRC_B0;
            case 7 -> v.ACC_SRC_B1;
            case 8 -> v.IIR_SRC_A0;
            case 9 -> v.IIR_SRC_A1;
            case 10 -> v.IIR_DEST_B0;
            case 11 -> v.IIR_DEST_B1;
            case 12 -> v.ACC_SRC_C0;
            case 13 -> v.ACC_SRC_C1;
            case 14 -> v.ACC_SRC_D0;
            case 15 -> v.ACC_SRC_D1;
            case 16 -> v.IIR_SRC_B1;
            case 17 -> v.IIR_SRC_B0;
            case 18 -> v.MIX_DEST_A0;
            case 19 -> v.MIX_DEST_A1;
            case 20 -> v.MIX_DEST_B0;
            default -> v.MIX_DEST_B1;
        };

        int now = high ? ((val & 0xf) << 16) | (old & 0xFFFF) : (old & 0xF0000) | (val & 0xFFFF);

        switch (index) {
        case 0 -> v.FB_SRC_A = now;
        case 1 -> v.FB_SRC_B = now;
        case 2 -> v.IIR_DEST_A0 = now;
        case 3 -> v.IIR_DEST_A1 = now;
        case 4 -> v.ACC_SRC_A0 = now;
        case 5 -> v.ACC_SRC_A1 = now;
        case 6 -> v.ACC_SRC_B0 = now;
        case 7 -> v.ACC_SRC_B1 = now;
        case 8 -> v.IIR_SRC_A0 = now;
        case 9 -> v.IIR_SRC_A1 = now;
        case 10 -> v.IIR_DEST_B0 = now;
        case 11 -> v.IIR_DEST_B1 = now;
        case 12 -> v.ACC_SRC_C0 = now;
        case 13 -> v.ACC_SRC_C1 = now;
        case 14 -> v.ACC_SRC_D0 = now;
        case 15 -> v.ACC_SRC_D1 = now;
        case 16 -> v.IIR_SRC_B1 = now;
        case 17 -> v.IIR_SRC_B0 = now;
        case 18 -> v.MIX_DEST_A0 = now;
        case 19 -> v.MIX_DEST_A1 = now;
        case 20 -> v.MIX_DEST_B0 = now;
        default -> v.MIX_DEST_B1 = now;
        }
    }

    private void setReverbCoefficient(int core, int offset, short val) {
        Reverb v = rvb[core];
        switch (offset / 2) {
        case 0 -> v.IIR_ALPHA = val;
        case 1 -> v.ACC_COEF_A = val;
        case 2 -> v.ACC_COEF_B = val;
        case 3 -> v.ACC_COEF_C = val;
        case 4 -> v.ACC_COEF_D = val;
        case 5 -> v.IIR_COEF = val;
        case 6 -> v.FB_ALPHA = val;
        case 7 -> v.FB_X = val;
        case 8 -> v.IN_COEF_L = val;
        default -> v.IN_COEF_R = val;
        }
    }

    @Override
    public int read(int reg) {
        int r = reg & 0xffff;

        if ((r >= 0x0000 && r < 0x0180) || (r >= 0x0400 && r < 0x0580)) {
            if ((r & 0x0f) == 10) { // the current envelope volume
                int ch = (r >> 4) & 0x1f;
                if (r >= 0x400) {
                    ch += 24;
                }
                Channel c = chan[ch];
                if (c.bNew) {
                    return 1; // started but not processed yet
                }
                if (c.lVolume != 0 && c.envelopeVol == 0) {
                    return 1; // no sample decoded yet, so no envelope yet
                }
                return (c.envelopeVol >>> 16) & 0xffff;
            }
        }

        if ((r >= 0x01c0 && r < 0x02E0) || (r >= 0x05c0 && r < 0x06E0)) {
            int ch = 0;
            int rx = r;
            if (rx >= 0x400) {
                ch = 24;
                rx -= 0x400;
            }
            ch += (rx - 0x1c0) / 12;
            rx -= (ch % 24) * 12;

            Channel c = chan[ch];
            switch (rx) {
            case 0x1C4:
                return (c.pLoop >> 17) & 0xF;
            case 0x1C6:
                return (c.pLoop >> 1) & 0xFFFF;
            case 0x1C8:
                return (c.pCurr >> 17) & 0xF;
            case 0x1CA:
                return (c.pCurr >> 1) & 0xFFFF;
            default:
                break;
            }
        }

        switch (r) {
        case PS2_C0_SPUend1:
            return dwEndChannel2[0] & 0xFFFF;
        case PS2_C0_SPUend2:
            return (dwEndChannel2[0] >>> 16) & 0xffff;
        case PS2_C1_SPUend1:
            return dwEndChannel2[1] & 0xFFFF;
        case PS2_C1_SPUend2:
            return (dwEndChannel2[1] >>> 16) & 0xffff;
        case PS2_C0_ATTR:
            return spuCtrl2[0];
        case PS2_C1_ATTR:
            return spuCtrl2[1];
        case PS2_C0_SPUstat:
            return spuStat2[0];
        case PS2_C1_SPUstat:
            return spuStat2[1];
        case PS2_C0_SPUdata: {
            int s = spuMem[spuAddr2[0] & 0xfffff] & 0xffff;
            spuAddr2[0]++;
            if (Integer.compareUnsigned(spuAddr2[0], 0xfffff) > 0) {
                spuAddr2[0] = 0;
            }
            return s;
        }
        case PS2_C1_SPUdata: {
            int s = spuMem[spuAddr2[1] & 0xfffff] & 0xffff;
            spuAddr2[1]++;
            if (Integer.compareUnsigned(spuAddr2[1], 0xfffff) > 0) {
                spuAddr2[1] = 0;
            }
            return s;
        }
        case PS2_C0_SPUaddr_Hi:
            return (spuAddr2[0] >>> 16) & 0xF;
        case PS2_C0_SPUaddr_Lo:
            return spuAddr2[0] & 0xFFFF;
        case PS2_C1_SPUaddr_Hi:
            return (spuAddr2[1] >>> 16) & 0xF;
        case PS2_C1_SPUaddr_Lo:
            return spuAddr2[1] & 0xFFFF;
        default:
            break;
        }

        return regArea[r >> 1] & 0xffff;
    }

    private void soundOn(int start, int end, int val) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            if ((val & 1) != 0 && chan[ch].pStart != 0) {
                chan[ch].bIgnoreLoop = false;
                chan[ch].bNew = true;
                dwNewChannel2[ch / 24] |= (1 << (ch % 24));
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
                    chan[ch].bFMod = 1;
                    chan[ch - 1].bFMod = 2;
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

    private void reverbOn(int start, int end, int val, boolean right) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            if (right) {
                chan[ch].bReverbR = (val & 1) != 0 ? 1 : 0;
            } else {
                chan[ch].bReverbL = (val & 1) != 0 ? 1 : 0;
            }
        }
    }

    private void volumeOn(int start, int end, int val, boolean right) {
        for (int ch = start; ch < end; ch++, val >>= 1) {
            if (right) {
                chan[ch].bVolumeR = (val & 1) != 0 ? 1 : 0;
            } else {
                chan[ch].bVolumeL = (val & 1) != 0 ? 1 : 0;
            }
        }
    }

    /** the sweep is not emulated, the volume is just moved half way instead */
    private void setVolumeL(int ch, short volIn) {
        chan[ch].iLeftVolume = computeVolume(ch, volIn, false);
    }

    private void setVolumeR(int ch, short volIn) {
        chan[ch].iRightVolume = computeVolume(ch, volIn, true);
    }

    private int computeVolume(int ch, short volIn, boolean right) {
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
            vol = (short) (((vol & 0x7f) + 1) / 2);
            vol += vol / (2 * sInc);
            vol = (short) (vol * 128);
        } else {
            if ((vol & 0x4000) != 0) {
                vol = 0x3fff - (vol & 0x3fff);
            }
        }

        return vol & 0x3fff;
    }

    private void setPitch(int ch, int val) {
        int NP = Math.min(val, 0x3fff);

        double intr = 48000.0f / 44100.0f * NP;
        NP = (int) intr;

        chan[ch].iRawPitch = NP;

        NP = (44100 * NP) / 4096;
        if (NP < 1) {
            NP = 1;
        }
        chan[ch].iActFreq = NP;
    }

    private void setReverbAddr(int core) {
        int val = spuRvbAddr2[core];

        if (rvb[core].StartAddr != val) {
            if (val <= 0x27ff) {
                rvb[core].StartAddr = rvb[core].CurrAddr = 0;
            } else {
                rvb[core].StartAddr = val;
                rvb[core].CurrAddr = rvb[core].StartAddr;
            }
        }
    }


    // ---- the voice view, for the visualizer ----

    @Override
    public int voiceCount() {
        return MAXCHAN;
    }

    @Override
    public boolean on(int voice) {
        return chan[voice].bOn;
    }

    @Override
    public boolean released(int voice) {
        return chan[voice].bStop;
    }

    @Override
    public int envelopeLevel(int voice) {
        return chan[voice].envelopeVol;
    }

    @Override
    public int envelopePhase(int voice) {
        Channel c = chan[voice];
        return c.bStop ? RELEASE : c.state;
    }

    @Override
    public int leftVolume(int voice) {
        return chan[voice].iLeftVolume;
    }

    @Override
    public int rightVolume(int voice) {
        return chan[voice].iRightVolume;
    }

    @Override
    public int pitch(int voice) {
        return chan[voice].iRawPitch;
    }

    @Override
    public int sampleStart(int voice) {
        return chan[voice].pStart;
    }

    @Override
    public int sampleLoop(int voice) {
        return chan[voice].pLoop;
    }

    @Override
    public boolean noise(int voice) {
        return chan[voice].bNoise;
    }

    @Override
    public int keyOnCount(int voice) {
        return keyOns[voice];
    }

    @Override
    public boolean reverb(int voice) {
        return chan[voice].bRVBActive;
    }

    // ---- dma ----

    @Override
    public void readDMA4Mem(int psxAddress, int size) {
        for (int i = 0; i < size; i++) {
            setRamHalf(psxAddress, spuMem[spuAddr2[0] & 0xfffff] & 0xffff);
            psxAddress += 2;
            spuAddr2[0]++;
            if (Integer.compareUnsigned(spuAddr2[0], 0xfffff) > 0) {
                spuAddr2[0] = 0;
            }
        }

        spuAddr2[0] += 0x20;

        regArea[PS2_C0_ADMAS >> 1] = 0; // auto dma complete
        spuStat2[0] = 0x80;             // dma complete
    }

    @Override
    public void readDMA7Mem(int psxAddress, int size) {
        for (int i = 0; i < size; i++) {
            setRamHalf(psxAddress, spuMem[spuAddr2[1] & 0xfffff] & 0xffff);
            psxAddress += 2;
            spuAddr2[1]++;
            if (Integer.compareUnsigned(spuAddr2[1], 0xfffff) > 0) {
                spuAddr2[1] = 0;
            }
        }

        spuAddr2[1] += 0x20;

        regArea[PS2_C1_ADMAS >> 1] = 0;
        spuStat2[1] = 0x80;
    }

    @Override
    public void writeDMA4Mem(int psxAddress, int size) {
        for (int i = 0; i < size; i++) {
            spuMem[spuAddr2[0] & 0xfffff] = (short) ramHalf(psxAddress);
            psxAddress += 2;
            spuAddr2[0]++;
            if (Integer.compareUnsigned(spuAddr2[0], 0xfffff) > 0) {
                spuAddr2[0] = 0;
            }
        }

        spuStat2[0] = 0x80;
    }

    @Override
    public void writeDMA7Mem(int psxAddress, int size) {
        for (int i = 0; i < size; i++) {
            spuMem[spuAddr2[1] & 0xfffff] = (short) ramHalf(psxAddress);
            psxAddress += 2;
            spuAddr2[1]++;
            if (Integer.compareUnsigned(spuAddr2[1], 0xfffff) > 0) {
                spuAddr2[1] = 0;
            }
        }

        spuStat2[1] = 0x80;
    }

    @Override
    public void interruptDMA4() {
        spuCtrl2[0] &= ~0x30;
        regArea[PS2_C0_ADMAS >> 1] = 0;
        spuStat2[0] |= 0x80;
    }

    @Override
    public void interruptDMA7() {
        spuCtrl2[1] &= ~0x30;
        regArea[PS2_C1_ADMAS >> 1] = 0;
        spuStat2[1] |= 0x80;
    }
}
