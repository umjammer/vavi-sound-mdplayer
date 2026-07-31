/*
 * ST-Sound ( YM files player library )
 *
 * Copyright (C) 1995-1999 Arnaud Carre ( http://leonard.oxg.free.fr )
 *
 * This file is part of ST-Sound
 *
 * ST-Sound is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ST-Sound is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ST-Sound; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mdplayer.lib.ym;

import java.util.Arrays;


public class Ym2149Ex {

    //
    // Constants
    //
    public static final long AMSTRAD_CLOCK   = 1000000L;
    public static final long ATARI_CLOCK     = 2000000L;
    public static final long SPECTRUM_CLOCK  = 1773400L;
    public static final long MFP_CLOCK       = 2457600L;
    public static final int  DRUM_PREC       = 15;

    //
    // Envelope shapes (static data)
    //
    private static final int[] Env00xx = {1,0,0,0,0,0,0,0};
    private static final int[] Env01xx = {0,1,0,0,0,0,0,0};
    private static final int[] Env1000 = {1,0,1,0,1,0,1,0};
    private static final int[] Env1001 = {1,0,0,0,0,0,0,0};
    private static final int[] Env1010 = {1,0,0,1,1,0,0,1};
    private static final int[] Env1011 = {1,0,1,1,1,1,1,1};
    private static final int[] Env1100 = {0,1,0,1,0,1,0,1};
    private static final int[] Env1101 = {0,1,1,1,1,1,1,1};
    private static final int[] Env1110 = {0,1,1,0,0,1,1,0};
    private static final int[] Env1111 = {0,1,0,0,0,0,0,0};

    private static final int[][] EnvWave = {
        Env00xx, Env00xx, Env00xx, Env00xx,
        Env01xx, Env01xx, Env01xx, Env01xx,
        Env1000, Env1001, Env1010, Env1011,
        Env1100, Env1101, Env1110, Env1111
    };

    // Volume table (will be scaled in constructor)
    private static final int[] ymVolumeTable = {
        62, 161, 265, 377, 580, 774, 1155, 1575,
        2260, 3088, 4570, 6233, 9330, 13187, 21220, 32767
    };

    //
    // DC Adjuster (inner class)
    //
    private static final int DC_ADJUST_BUFFERLEN = 512;

    private static class DcAdjuster {
        private final int[] buffer = new int[DC_ADJUST_BUFFERLEN];
        private int pos = 0;
        private int sum = 0;

        DcAdjuster() {
            reset();
        }

        void reset() {
            Arrays.fill(buffer, 0);
            pos = 0;
            sum = 0;
        }

        void addSample(int sample) {
            sum -= buffer[pos];
            sum += sample;
            buffer[pos] = sample;
            pos = (pos + 1) & (DC_ADJUST_BUFFERLEN - 1);
        }

        int getDcLevel() {
            return sum / DC_ADJUST_BUFFERLEN;
        }
    }

    //
    // Instance state
    //
    private final DcAdjuster m_dcAdjust = new DcAdjuster();

    private final int replayFrequency;
    private long internalClock;          // unsigned 32-bit in C++, use long for range

    private final int[] registers = new int[14];

    // Tone generators
    private int stepA, stepB, stepC;     // unsigned 32-bit
    private int posA, posB, posC;        // unsigned 32-bit
    private int volA, volB, volC, volE;
    private int mixerTA, mixerTB, mixerTC;
    private int mixerNA, mixerNB, mixerNC;
    private int pVolA, pVolB, pVolC;     // pointers to volumes (we use indices)

    // Noise generator
    private int noiseStep;               // unsigned 32-bit
    private int noisePos;                // unsigned 32-bit
    private int rndRack;                 // unsigned 32-bit
    private int currentNoise;            // unsigned 32-bit

    // Envelope generator
    private int envStep;                 // unsigned 32-bit
    private int envPos;                  // unsigned 32-bit
    private int envPhase;
    private int envShape;
    private final int[][][] envData = new int[16][2][32]; // 16*2*32 = 1024 entries

    // Special effects
    private static class YmSpecialEffect {
        boolean bDrum;
        int drumSize;          // unsigned 32-bit
        byte[] drumData;        // array of unsigned 8-bit values (stored as int)
        int drumPos;           // unsigned 32-bit
        int drumStep;          // unsigned 32-bit

        boolean bSid;
        int sidPos;            // unsigned 32-bit
        int sidStep;           // unsigned 32-bit
        int sidVol;
    }
    private final YmSpecialEffect[] specialEffect = new YmSpecialEffect[3];

    private boolean bSyncBuzzer;
    private int syncBuzzerStep;    // unsigned 32-bit
    private int syncBuzzerPhase;   // unsigned 32-bit
    private int syncBuzzerShape;   // unused

    // Low-pass filter
    private final int[] m_lowPassFilter = new int[2];

    //
    // Constructor
    //
    public Ym2149Ex() {
        this(ATARI_CLOCK, 1, 44100);
    }

    public Ym2149Ex(long masterClock, int prediv, int playRate) {
        // Scale volume table if needed (original hack)
        if (ymVolumeTable[15] == 32767) {
            for (int i = 0; i < 16; i++) {
                ymVolumeTable[i] = (ymVolumeTable[i] * 2) / 6;
            }
        }

        // Build envelope data
        int idx = 0; // flat index into envData[][][]
        for (int env = 0; env < 16; env++) {
            int[] pse = EnvWave[env];
            // Fill 4 phases (each 16 entries) into the 2x32 array
            for (int phase = 0; phase < 4; phase++) {
                int a = pse[phase * 2];
                int b = pse[phase * 2 + 1];
                int d = b - a;
                a *= 15;
                for (int i = 0; i < 16; i++) {
                    // Store sequentially: phase0 -> [env][0][0..15], phase1 -> [env][0][16..31],
                    // phase2 -> [env][1][0..15], phase3 -> [env][1][16..31]
                    int row = (phase < 2) ? 0 : 1;
                    int col = (phase % 2) * 16 + i;
                    envData[env][row][col] = a;
                    a += d;
                }
            }
        }

        internalClock = masterClock / prediv;
        replayFrequency = playRate;

        // Initialize specialEffect array
        for (int i = 0; i < 3; i++) {
            specialEffect[i] = new YmSpecialEffect();
        }

        // Reset the chip
        reset();
    }

    //
    // Public API
    //
    public void setClock(long clock) {
        internalClock = clock;
    }

    public void reset() {
        for (int i = 0; i < 14; i++) {
            writeRegister(i, 0);
        }
        writeRegister(7, 0xff);

        currentNoise = 0xffff;
        rndRack = 1;
        sidStop(0);
        sidStop(1);
        sidStop(2);

        envShape = 0;
        envPhase = 0;
        envPos = 0;

        m_dcAdjust.reset();

        for (YmSpecialEffect effect : specialEffect) {
            effect.bDrum = false;
            effect.bSid = false;
            effect.drumData = null;
            effect.drumPos = 0;
            effect.drumStep = 0;
            effect.drumSize = 0;
            effect.sidPos = 0;
            effect.sidStep = 0;
            effect.sidVol = 0;
        }

        syncBuzzerStop();

        m_lowPassFilter[0] = 0;
        m_lowPassFilter[1] = 0;
    }

    public void update(short[] pSampleBuffer, int nbSample) {
        for (int i = 0; i < nbSample; i++) {
            pSampleBuffer[i] = (short) nextSample();
        }
    }

    public void writeRegister(int reg, int data) {
        data &= 0xff; // ensure 8-bit
        switch (reg) {
            case 0:
                registers[0] = data & 0xff;
                stepA = toneStepCompute(registers[1], registers[0]);
                if (stepA == 0) posA = 0x80000000; // assume output always 1
                break;
            case 2:
                registers[2] = data & 0xff;
                stepB = toneStepCompute(registers[3], registers[2]);
                if (stepB == 0) posB = 0x80000000;
                break;
            case 4:
                registers[4] = data & 0xff;
                stepC = toneStepCompute(registers[5], registers[4]);
                if (stepC == 0) posC = 0x80000000;
                break;
            case 1:
                registers[1] = data & 0x0f;
                stepA = toneStepCompute(registers[1], registers[0]);
                if (stepA == 0) posA = 0x80000000;
                break;
            case 3:
                registers[3] = data & 0x0f;
                stepB = toneStepCompute(registers[3], registers[2]);
                if (stepB == 0) posB = 0x80000000;
                break;
            case 5:
                registers[5] = data & 0x0f;
                stepC = toneStepCompute(registers[5], registers[4]);
                if (stepC == 0) posC = 0x80000000;
                break;
            case 6:
                registers[6] = data & 0x1f;
                noiseStep = noiseStepCompute(registers[6]);
                if (noiseStep == 0) {
                    noisePos = 0;
                    currentNoise = 0xffff;
                }
                break;
            case 7:
                registers[7] = data & 0xff;
                mixerTA = ((data & (1 << 0)) != 0) ? 0xffff : 0;
                mixerTB = ((data & (1 << 1)) != 0) ? 0xffff : 0;
                mixerTC = ((data & (1 << 2)) != 0) ? 0xffff : 0;
                mixerNA = ((data & (1 << 3)) != 0) ? 0xffff : 0;
                mixerNB = ((data & (1 << 4)) != 0) ? 0xffff : 0;
                mixerNC = ((data & (1 << 5)) != 0) ? 0xffff : 0;
                break;
            case 8:
                registers[8] = data & 0x1f;
                volA = ymVolumeTable[data & 0x0f];
                pVolA = ((data & 0x10) != 0) ? 0 : 1; // 0 = volE, 1 = volA
                break;
            case 9:
                registers[9] = data & 0x1f;
                volB = ymVolumeTable[data & 0x0f];
                pVolB = ((data & 0x10) != 0) ? 0 : 1;
                break;
            case 10:
                registers[10] = data & 0x1f;
                volC = ymVolumeTable[data & 0x0f];
                pVolC = ((data & 0x10) != 0) ? 0 : 1;
                break;
            case 11:
                registers[11] = data & 0xff;
                envStep = envStepCompute(registers[12], registers[11]);
                break;
            case 12:
                registers[12] = data & 0xff;
                envStep = envStepCompute(registers[12], registers[11]);
                break;
            case 13:
                registers[13] = data & 0x0f;
                envPos = 0;
                envPhase = 0;
                envShape = data & 0x0f;
                break;
        }
    }

    public int readRegister(int reg) {
        if (reg >= 0 && reg < 14) return registers[reg];
        return -1;
    }

    public void drumStart(int voice, byte[] drumBuffer, int drumSize, int drumFreq) {
        YmSpecialEffect eff = specialEffect[voice];
        eff.drumData = drumBuffer;
        eff.drumPos = 0;
        eff.drumSize = drumSize;
        eff.drumStep = (drumFreq << DRUM_PREC) / replayFrequency;
        eff.bDrum = true;
    }

    public void drumStop(int voice) {
        specialEffect[voice].bDrum = false;
    }

    public void sidStart(int voice, int timerFreq, int vol) {
        long tmp = (long) timerFreq * ((1L << 31) / replayFrequency);
        YmSpecialEffect eff = specialEffect[voice];
        eff.sidStep = (int) tmp;
        eff.sidVol = vol & 0x0f;
        eff.bSid = true;
    }

    public void sidSinStart(int voice, int timerFreq, int sinPattern) {
        // TODO
    }

    public void sidStop(int voice) {
        specialEffect[voice].bSid = false;
    }

    public void syncBuzzerStart(int timerFreq, int envShape) {
        long tmp = (long) timerFreq * ((1L << 31) / replayFrequency);
        // C++ keeps the current envShape (set via register 13) and ignores the parameter
        this.envShape = this.envShape & 0x0f;
        syncBuzzerStep = (int) tmp;
        syncBuzzerPhase = 0;
        bSyncBuzzer = true;
    }

    public void syncBuzzerStop() {
        bSyncBuzzer = false;
        syncBuzzerPhase = 0;
        syncBuzzerStep = 0;
    }

    //
    // Internal methods
    //
    private int toneStepCompute(int rHigh, int rLow) {
        int per = (rHigh & 0x0f) << 8 | (rLow & 0xff);
        if (per <= 5) return 0;

        // Use long arithmetic to avoid overflow
        long step = internalClock << (15 + 16 - 3); // shift 28
        step /= (per * replayFrequency);
        return (int) step; // cast to 32-bit unsigned
    }

    private int noiseStepCompute(int rNoise) {
        int per = rNoise & 0x1f;
        if (per < 3) return 0;

        long step = internalClock << (16 - 1 - 3); // shift 12? Actually 16-1-3=12
        step /= (per * replayFrequency);
        return (int) step;
    }

    private int envStepCompute(int rHigh, int rLow) {
        int per = (rHigh << 8) | (rLow & 0xff);
        if (per < 3) return 0;

        long step = internalClock << (16 + 16 - 9); // shift 23
        step /= (per * replayFrequency);
        return (int) step;
    }

    private int rndCompute() {
        int rBit = ((rndRack & 1) ^ ((rndRack >> 2) & 1)) & 1;
        rndRack = (rndRack >> 1) | (rBit << 16);
        return (rBit != 0) ? 0 : 0xffff;
    }

    private int lowPassFilter(int in) {
        int out = (m_lowPassFilter[0] >> 2) + (m_lowPassFilter[1] >> 1) + (in >> 2);
        m_lowPassFilter[0] = m_lowPassFilter[1];
        m_lowPassFilter[1] = in;
        return out;
    }

    private int nextSample() {
        // Update noise
        if ((noisePos & 0xffff0000) != 0) {
            currentNoise ^= rndCompute();
            noisePos &= 0xffff;
        }
        int bn = currentNoise;

        // Envelope volume
        int envIndex = (envPos >>> 27) & 0x1f; // envPos >> (32-5)
        volE = ymVolumeTable[envData[envShape][envPhase][envIndex]];

        // Apply SID/Drum effects (they may alter volumes and mixers)
        // We'll compute volumes directly via sidVolumeCompute and update pVol* variables
        // Since pVol* are ints, we'll use them as volume values.
        // We'll call sidVolumeCompute for each voice, but it modifies registers, so we need to
        // re-read the volume values after.
        // Instead, we'll handle sid/drum inside nextSample manually.
        // For simplicity, we'll replicate the logic inline.

        // Voice A SID/Drum (inlined sidVolumeCompute; C++ goes through writeRegister(8+voice, ...))
        YmSpecialEffect eff0 = specialEffect[0];
        if (eff0.bSid) {
            volA = ymVolumeTable[(((eff0.sidPos & 0x80000000) != 0) ? eff0.sidVol : 0) & 0x0f];
            pVolA = 1; // use volA
        } else if (eff0.bDrum) {
            volA = (eff0.drumData[eff0.drumPos >>> DRUM_PREC] & 0xFF) * 255 / 6;
            pVolA = 1;
            mixerTA = 0xffff;
            mixerNA = 0xffff;
            eff0.drumPos += eff0.drumStep;
            if ((eff0.drumPos >>> DRUM_PREC) >= eff0.drumSize) {
                eff0.bDrum = false;
            }
        }

        // Voice B
        YmSpecialEffect eff1 = specialEffect[1];
        if (eff1.bSid) {
            volB = ymVolumeTable[(((eff1.sidPos & 0x80000000) != 0) ? eff1.sidVol : 0) & 0x0f];
            pVolB = 1;
        } else if (eff1.bDrum) {
            volB = (eff1.drumData[eff1.drumPos >>> DRUM_PREC] & 0xFF) * 255 / 6;
            pVolB = 1;
            mixerTB = 0xffff;
            mixerNB = 0xffff;
            eff1.drumPos += eff1.drumStep;
            if ((eff1.drumPos >>> DRUM_PREC) >= eff1.drumSize) {
                eff1.bDrum = false;
            }
        }

        // Voice C
        YmSpecialEffect eff2 = specialEffect[2];
        if (eff2.bSid) {
            volC = ymVolumeTable[(((eff2.sidPos & 0x80000000) != 0) ? eff2.sidVol : 0) & 0x0f];
            pVolC = 1;
        } else if (eff2.bDrum) {
            volC = (eff2.drumData[eff2.drumPos >>> DRUM_PREC] & 0xFF) * 255 / 6;
            pVolC = 1;
            mixerTC = 0xffff;
            mixerNC = 0xffff;
            eff2.drumPos += eff2.drumStep;
            if ((eff2.drumPos >>> DRUM_PREC) >= eff2.drumSize) {
                eff2.bDrum = false;
            }
        }

        // Compute tone+noise+env for each voice
        int bt, vol;

        // Voice A
        int signA = ((posA & 0x80000000) != 0) ? 0xffff : 0;
        bt = (signA | mixerTA) & (bn | mixerNA);
        vol = (pVolA == 0) ? volE : volA;
        vol &= bt;

        // Voice B
        int signB = ((posB & 0x80000000) != 0) ? 0xffff : 0;
        bt = (signB | mixerTB) & (bn | mixerNB);
        int volBVal = (pVolB == 0) ? volE : volB;
        vol += volBVal & bt;

        // Voice C
        int signC = ((posC & 0x80000000) != 0) ? 0xffff : 0;
        bt = (signC | mixerTC) & (bn | mixerNC);
        int volCVal = (pVolC == 0) ? volE : volC;
        vol += volCVal & bt;

        // Increment counters
        posA += stepA;
        posB += stepB;
        posC += stepC;
        noisePos += noiseStep;
        envPos += envStep;
        if (envPhase == 0) {
            // envPos and envStep are wrapping unsigned 32-bit counters; envPos < envStep
            // (unsigned) right after the addition means the counter overflowed.
            if (Integer.compareUnsigned(envPos, envStep) < 0) {
                envPhase = 1;
            }
        }

        // Sync buzzer
        syncBuzzerPhase += syncBuzzerStep;
        if ((syncBuzzerPhase & 0x80000000) != 0) {
            envPos = 0;
            envPhase = 0;
            syncBuzzerPhase &= 0x7fffffff;
        }

        // SID positions
        specialEffect[0].sidPos += specialEffect[0].sidStep;
        specialEffect[1].sidPos += specialEffect[1].sidStep;
        specialEffect[2].sidPos += specialEffect[2].sidStep;

        // DC adjust and low-pass filter
        m_dcAdjust.addSample(vol);
        int in = vol - m_dcAdjust.getDcLevel();
        int out = lowPassFilter(in);

        // Clamp to short range
        if (out < -32768) out = -32768;
        if (out > 32767) out = 32767;
        return out;
    }
}
