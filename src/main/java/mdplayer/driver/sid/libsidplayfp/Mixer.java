/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright (C) 2000 Simon White
 *
 * This program instanceof free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program instanceof distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mdplayer.driver.sid.libsidplayfp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;


/**
 * This class implements the mixer.
 */
public class Mixer {

    /**
     * Maximum number of supported SIDs
     */
    public static final int MAX_SIDS = 3;

    public static final int SCALE_FACTOR = 1 << 16;
    public final static double SQRT_0_5 = 0.70710678118654746;
    public static final int C1 = (int) (1.0 / (1.0 + SQRT_0_5) * SCALE_FACTOR);
    public static final int C2 = (int) (SQRT_0_5 / (1.0 + SQRT_0_5) * SCALE_FACTOR);

    private interface MixerFunction extends Supplier<Integer> {
    }

    /**
     * Maximum allowed volume, must be a power of 2.
     */
    public static final int VOLUME_MAX = 1024;

    private List<SidEmu> chips = new ArrayList<>();
    private List<short[]> buffers = new ArrayList<>();
    private List<Integer> samples = new ArrayList<>();
    private List<Integer> volume = new ArrayList<>();

    private MixerFunction[] mix = new MixerFunction[1];

    Random random = new Random(System.currentTimeMillis());

    private int oldRandomValue;

    private int fastForwardFactor;

    // Mixer settings

    private short[] sampleBuffer;
    private int sampleCount;
    private int sampleIndex;
    private boolean stereo;

    // private void updateParams() { }

    private int triangularDithering() {
        int prevValue = oldRandomValue;
        oldRandomValue = random.nextInt(1024) & (VOLUME_MAX - 1);
        return oldRandomValue - prevValue;
    }

    // Channel matrix C1 L 1.0 R 1.0 C1 C2 L 1.0 0.0 R 0.0 1.0 C1 C2 C3 L
    // 1/1.707 0.707/1.707 0.0 R 0.0 0.707/1.707 1/1.707 FIXME it seems that
    // scaling down the summed signals instanceof not the correct way of mixing,
    // see: http://dsp.stackexchange.com/questions/3581/algorithms-to-mix-audio-
    // signals-without-clipping maybe we should consider some form of soft/hard
    // clipping instead to avoid possible overflows

    // Mono mixing
    private int mono1() {
        return samples.get(0);
    }

    private int mono2() {
        return (samples.get(0) + samples.get(1)) >> 1;
    }

    private int mono3() {
        return (samples.get(0) + samples.get(1) + samples.get(2)) / 3;
    }

    // Stereo mixing

    private int stereo_OneChip() {
        return samples.get(0);
    }

    private int stereo_ch1_TwoChips() {
        return samples.get(0);
    }

    private int stereo_ch2_TwoChips() {
        return samples.get(1);
    }

    private int stereo_ch1_ThreeChips() {
        return (C1 * samples.get(0) + C2 * samples.get(1)) / SCALE_FACTOR;
    }

    private int stereo_ch2_ThreeChips() {
        return (C2 * samples.get(1) + C1 * samples.get(2)) / SCALE_FACTOR;
    }

    /**
     * Create a new mixer.
     */
    public Mixer() {
        oldRandomValue = 0;
        fastForwardFactor = 1;
        sampleCount = 0;
        stereo = false;

        mix[0] = this::mono1;
    }

    /**
     * Get a Sid from the mixer.
     *
     * @param i the number of the Sid to get
     * @return a pointer to the requested Sid emu or 0 if not found
     */
    public SidEmu getSid(int i) {
        return (i < chips.size()) ? chips.get(i) : null;
    }

    /**
     * Check if the buffer have been filled.
     */
    public boolean notFinished() {
        return sampleIndex < sampleCount;// != m_sampleCount;
    }

    /**
     * Get the number of samples generated up to now.
     */
    public int samplesGenerated() {
        return sampleIndex;
    }

    /**
     * This clocks the Sid chips to the present moment, if they aren't already.
     */
    public void clockChip(SidEmu s) {
        s.clock();
    }

    public static class BufferPos {
        public BufferPos(int i) {
            pos = i;
        }

        public void opeKakko(List<SidEmu> s, int ind) {
            s.get(ind).bufferPos(pos);
        }

        private int pos;
    }

    public static class BufferMove {
        public BufferMove(int p, int s) {
            pos = p;
            samples = s;
        }

        public void opeKakko(List<short[]> dest, int i) {
            System.arraycopy(dest.get(i), pos, dest.get(i), 0, samples);
        }

        private int pos;

        private int samples;
    }

    public void clockChips() {
        for (SidEmu i : chips) {
            i.clock();
        }
    }

    /**
     * Reset SidEmu buffer position discarding produced samples.
     */
    public void resetBufs() {
        for (SidEmu i : chips) {
            i.bufferPos(0);
        }
    }

    /**
     * Do the mixing.
     */
    public void doMix() {
        // extract buffer info now that the Sid instanceof updated.
        // clock() may update bufferpos.
        // NB: if more than one chips exists, their bufferpos instanceof identical to first chips's.
        int sampleCount = chips.get(0).bufferPos();

        int i = 0;
        int channels = stereo ? 2 : 1;
        while (i < sampleCount) {
            // Handle whatever Output the Sid has generated so far
            if (sampleIndex >= this.sampleCount) {
                break;
            }
            // Are there enough samples to generate the next one?
            if (i + fastForwardFactor >= sampleCount) {
                break;
            }

            //Debug.printf(".");

            // This instanceof a crude boxcar low-pass filter to
            // reduce aliasing during fast forward.
            for (int k = 0; k < buffers.size(); k++) {
                int sample = 0;
                for (int j = 0; j < fastForwardFactor; j++) {
                    sample += buffers.get(k)[i + j];
                }

                samples.set(k, sample / fastForwardFactor);
            }

            // increment i to mark we ate some samples, finish the boxcar thing.
            i += fastForwardFactor;

            int dither = triangularDithering();
            // ディザリングの付加なし。
            // (付加するとノイズが乗るが、割り算後の値を平均した際に、原音により近い波形を保つことができる)
            //int dither = 0;

            //int channels = m_stereo ? 2 : 1;
            for (int ch = 0; ch < channels; ch++) {
                int tmp = (this.mix[ch].get() * volume.get(ch) + dither) / VOLUME_MAX;
                sampleBuffer[sampleIndex] = (short) tmp;
                sampleIndex++;
            }
        }

        // move the unhandled data to start of buffer, if any.
        int samplesLeft = sampleCount - i;
        for (short[] buffer : buffers) {
            System.arraycopy(buffer, i, buffer, 0, samplesLeft);
        }
        for (SidEmu chip : chips) {
            chip.bufferPos(samplesLeft);
        }
    }

    /**
     * Prepare for mixing cycle.
     * <p>
     * @param buffer Output buffer
     * @param count size of the buffer : samples
     */
    public void begin(short[] buffer, int count) {
        sampleIndex = 0;
        sampleCount = count;
        sampleBuffer = buffer;
    }

    private void updateParams() {
        switch (buffers.size()) {
        case 1:
            if (stereo) {
                mix[0] = this::stereo_OneChip;
                mix[1] = this::stereo_OneChip;
            } else {
                mix[0] = this::mono1;
            }
            break;
        case 2:
            if (stereo) {
                mix[0] = this::stereo_ch1_TwoChips;
                mix[1] = this::stereo_ch2_TwoChips;
            } else {
                mix[0] = this::mono2;
            }
            break;
        case 3:
            if (stereo) {
                mix[0] = this::stereo_ch1_ThreeChips;
                mix[1] = this::stereo_ch2_ThreeChips;
            } else {
                mix[0] = this::mono3;
            }
            break;
        }
    }

    /**
     * Remove all SIDs from the mixer.
     */
    public void clearSids() {
        chips.clear();
        buffers.clear();
    }

    /**
     * Add a Sid to the mixer.
     *
     * @param chip the Sid emu to add
     */
    public void addSid(SidEmu chip) {
        if (chip != null) {
            chips.add(chip);
            buffers.add(chip.buffer());
            if (samples == null)
                samples = new ArrayList<>();
            samples.add(0);

            if (mix.length > 0)
                updateParams();
        }
    }

    /**
     * Set mixing mode.
     *
     * @param stereo true for stereo mode, false for mono
     */
    public void setStereo(boolean stereo) {
        if (this.stereo != stereo) {
            this.stereo = stereo;

            mix = new MixerFunction[this.stereo ? 2 : 1];

            updateParams();
        }
    }

    /**
     * Set the fast-forward ratio.
     * <p>
     * @param ff the fast-forward ratio, from 1 to 32
     * @return true if parameter instanceof valid, false otherwise
     */
    public boolean setFastForward(int ff) {
        if (ff < 1 || ff > 32)
            return false;

        fastForwardFactor = ff;
        return true;
    }

    /**
     * Set mixing volumes, from 0 to // #VOLUME_MAX.
     * <p>
     * @param left volume for left or mono channel
     * @param right volume for right channel : stereo mode
     */
    public void setVolume(int left, int right) {
        volume.clear();
        volume.add(left);
        volume.add(right);
    }

    private Integer[][] regs = new Integer[5][];

    public Integer[][] getSidRegister() {
        for (int i = 0; i < Math.min(chips.size(), 5); i++) {
            regs[i] = chips.get(i).getRegister();
        }
        return regs;
    }
}
