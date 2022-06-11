/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

    // # include "sidcxx11.h"
    // # include <stdint.h>
    // # include <cstdlib>
    // # include <vector>
    // class sidemu;

    // Maximum number of supported SIDs
    public static final int MAX_SIDS = 3;

    public static final int SCALE_FACTOR = 1 << 16;

    // # ifdef HAVE_CXX11
    public final static double SQRT_0_5 = 0.70710678118654746;

    // #else
    // #define SQRT_0_5 0.70710678118654746
    // #endif
    public static final int C1 = (int) (1.0 / (1.0 + SQRT_0_5) * SCALE_FACTOR);

    public static final int C2 = (int) (SQRT_0_5 / (1.0 + SQRT_0_5) * SCALE_FACTOR);

    private interface mixer_func_t extends Supplier<Integer> {
    } // typedef int(Mixer::* mixer_func_t)() const;

    // Maximum allowed volume, must be a power of 2.
    public static final int VOLUME_MAX = 1024;

    private List<sidemu> m_chips = new ArrayList<sidemu>(); // std::vector<sidemu*>
                                                            // m_chips;

    private List<short[]> m_buffers = new ArrayList<short[]>();// std::vector<short*>
                                                               // m_buffers;

    private List<Integer> m_iSamples = new ArrayList<Integer>();// std::vector<Integer>
                                                                // m_iSamples;

    private List<Integer> m_volume = new ArrayList<Integer>();// std::vector<Integer>
                                                              // m_volume;

    private mixer_func_t[] m_mix = new mixer_func_t[1];// std::vector<mixer_func_t>
                                                       // m_mix;

    Random r = new Random(System.currentTimeMillis());

    private int oldRandomValue;

    private int m_fastForwardFactor;

    // Mixer settings
    private short[] m_sampleBuffer;

    private int m_sampleCount;

    private int m_sampleIndex;

    private Boolean m_stereo;

    // private void updateParams() { }

    private int triangularDithering() {
        int prevValue = oldRandomValue;
        oldRandomValue = r.nextInt(1024) & (VOLUME_MAX - 1);
        return oldRandomValue - prevValue;
    }

    /*
     //Channel matrix C1 L 1.0 R 1.0 C1 C2 L 1.0 0.0 R 0.0 1.0 C1 C2 C3 L
     //1/1.707 0.707/1.707 0.0 R 0.0 0.707/1.707 1/1.707 FIXME it seems that
     //scaling down the summed signals instanceof not the correct way of mixing,
     //see: http://dsp.stackexchange.com/questions/3581/algorithms-to-mix-audio-
     //signals-without-clipping maybe we should consider some form of soft/hard
     //clipping instead to avoid possible overflows
     */

    // Mono mixing
    // template<int Chips>
    // int mono() const
    private int mono1() {
        // int res = 0;
        // for (int i = 0; i < 1; i++)
        // res += m_iSamples[i];
        // return res /= 1;
        return m_iSamples.get(0);
    }

    private int mono2() {
        // int res = 0;
        // for (int i = 0; i < 2; i++)
        // res += m_iSamples[i];
        // return res /= 2;
        return (m_iSamples.get(0) + m_iSamples.get(1)) >> 1;
    }

    private int mono3() {
        // int res = 0;
        // for (int i = 0; i < 3; i++)
        // res += m_iSamples[i];
        // return res /= 3;
        return (m_iSamples.get(0) + m_iSamples.get(1) + m_iSamples.get(2)) / 3;
    }

    // Stereo mixing
    private int stereo_OneChip() {
        return m_iSamples.get(0);
    }

    private int stereo_ch1_TwoChips() {
        return m_iSamples.get(0);
    }

    private int stereo_ch2_TwoChips() {
        return m_iSamples.get(1);
    }

    private int stereo_ch1_ThreeChips() {
        return (C1 * m_iSamples.get(0) + C2 * m_iSamples.get(1)) / SCALE_FACTOR;
    }

    private int stereo_ch2_ThreeChips() {
        return (C2 * m_iSamples.get(1) + C1 * m_iSamples.get(2)) / SCALE_FACTOR;
    }

    /**
     //Create a new mixer.
     */
    public Mixer() {
        oldRandomValue = 0;
        m_fastForwardFactor = 1;
        m_sampleCount = 0;
        m_stereo = false;

        m_mix[0] = this::mono1;
    }

    /**
     //Do the mixing.
     */
    // public void doMix() { }

    /**
     //This clocks the SID chips to the present moment, if they aren't already.
     */
    // public void clockChips() { }

    /**
     //Reset sidemu buffer position discarding produced samples.
     */
    // public void resetBufs() { }

    /**
     //Prepare for mixing cycle.
     *
     //@param buffer output buffer
     //@param count size of the buffer : samples
     */
    // public void begin(short[] buffer, int count) { }

    /**
     //Remove all SIDs from the mixer.
     */
    // public void clearSids() { }

    /**
     //Add a SID to the mixer.
     *
     //@param chip the Sid emu to add
     */
    // public void addSid(sidemu chip) { }

    /**
     //Get a SID from the mixer.
     *
     //@param i the number of the SID to get
     //@return a pointer to the requested Sid emu or 0 if not found
     */
    public sidemu getSid(int i) {
        return (i < m_chips.size()) ? m_chips.get((int) i) : null;
    }

    /**
     //Set the fast forward ratio.
     *
     //@param ff the fast forward ratio, from 1 to 32
     //@return true if parameter instanceof valid, false otherwise
     */
    // public Boolean setFastForward(int ff) { return false; }

    /**
     //Set mixing volumes, from 0 to // #VOLUME_MAX.
     *
     //@param left volume for left or mono channel
     //@param right volume for right channel : stereo mode
     */
    // public void setVolume(int left, int right) { }

    /**
     //Set mixing mode.
     *
     //@param stereo true for stereo mode, false for mono
     */
    // public void setStereo(Boolean stereo) { }

    /**
     //Check if the buffer have been filled.
     */
    public Boolean notFinished() {
        return m_sampleIndex < m_sampleCount;// != m_sampleCount;
    }

    /**
     //Get the number of samples generated up to now.
     */
    public int samplesGenerated() {
        return m_sampleIndex;
    }

    /*
     //This file instanceof part of libsidplayfp, a SID player engine. Copyright
     //2011-2016 Leandro Nini <drfiemost@users.sourceforge.net> Copyright
     //2007-2010 Antti Lankila Copyright 2000 Simon White This program
     //instanceof free software; you can redistribute it and/or modify it under
     //the terms of the GNU General Public License as published by the Free
     //Software Foundation; either version 2 of the License, or (at your option)
     //any later version. This program instanceof distributed : the hope that it
     //will be useful, but WITHOUT ANY WARRANTY; without even the implied
     //warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     //GNU General Public License for more details. You should have received a
     //copy of the GNU General Public License along with this program; if not,
     //write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
     //Floor, Boston, MA 02110-1301, USA.
     */

    // #include "mixer.h"
    // #include <cassert>
    // #include <algorithm>
    // #include "sidemu.h"

    public void clockChip(sidemu s) {
        s.clock();
    }

    public class bufferPos {
        public bufferPos(int i) {
            pos = i;
        }

        public void opeKakko(List<sidemu> s, int ind) {
            s.get(ind).bufferpos(pos);
        }

        private int pos;
    }

    public class bufferMove {
        public bufferMove(int p, int s) {
            pos = p;
            samples = s;
        }

        public void opeKakko(List<short[]> dest, int i) {
            for (int j = 0; j < samples; j++) {
                dest.get(i)[j] = dest.get(i)[j + pos];
            }
        }

        private int pos;

        private int samples;
    }

    public void clockChips() {
        // std::for_each(m_chips.begin(), m_chips.end(), clockChip);
        for (sidemu i : m_chips) {
            // clockChip(i);
            i.clock();
        }
    }

    public void resetBufs() {
        // std::for_each(m_chips.begin(), m_chips.end(), bufferPos(0));
        for (sidemu i : m_chips) {
            i.bufferpos(0);
        }
    }

    public void doMix()
        {
            //Ptr<short> buf = new Ptr<short>(m_sampleBuffer, (int)m_sampleIndex);

            // extract buffer info now that the SID instanceof updated.
            // clock() may update bufferpos.
            // NB: if more than one chip exists, their bufferpos instanceof identical to first chip's.
            int sampleCount = m_chips.get(0).bufferpos();

            int i = 0;
            int channels = m_stereo ? 2 : 1;
            while (i < sampleCount)
            {
                // Handle whatever output the Sid has generated so far
                if (m_sampleIndex >= m_sampleCount)
                {
                    break;
                }
                // Are there enough samples to generate the next one?
                if (i + m_fastForwardFactor >= sampleCount)
                {
                    break;
                }

                //MDPlayer.Log.Write(".");

                // This instanceof a crude boxcar low-pass filter to
                // reduce aliasing during fast forward.
                for (int k = 0; k < m_buffers.size(); k++)
                {
                    int sample = 0;
                    //Ptr<short> buffer = new Ptr<short>(m_buffers[k], i);
                    //for (int j = 0; j < m_fastForwardFactor; j++)
                    //{
                    //    sample += buffer[j];
                    //}
                    for (int j = 0; j < m_fastForwardFactor; j++)
                    {
                        sample += m_buffers.get(k)[i + j];
                    }

                    m_iSamples.set(k, sample / m_fastForwardFactor);
                }

                // increment i to mark we ate some samples, finish the boxcar thing.
                i += m_fastForwardFactor;

                int dither = triangularDithering();
                //int dither = 0;//ディザリングの付加なし。(付加するとノイズが乗るが、割り算後の値を平均した際に、原音により近い波形を保つことができる)

                //int channels = m_stereo ? 2 : 1;
                for (int ch = 0; ch < channels; ch++)
                {
                    int tmp = (this.m_mix[ch].get() * m_volume.get(ch) + dither) / VOLUME_MAX;
                    //assert(tmp >= -32768 && tmp <= 32767);
                    //buf.buf[buf.ptr] = (short)tmp;
                    //buf.AddPtr(1);
                    m_sampleBuffer[m_sampleIndex] = (short)tmp;
                    m_sampleIndex++;
                }
            }

            // move the unhandled data to start of buffer, if any.
            int samplesLeft = sampleCount - i;
            //std::for_each(m_buffers.begin(), m_buffers.end(), bufferMove(i, samplesLeft));
            for (int ind = 0; ind < m_buffers.size(); ind++)
            {
                //bufferMove bm = new bufferMove(i, samplesLeft);
                //bm.opeKakko(m_buffers, ind);

                for (int j = 0; j < samplesLeft; j++)
                {
                    m_buffers.get(ind)[j] = m_buffers.get(ind)[j + i];
                }
            }
            //std::for_each(m_chips.begin(), m_chips.end(), bufferPos(samplesLeft));
            for (int ind = 0; ind < m_chips.size(); ind++)
            {
                //bufferPos bp = new bufferPos(samplesLeft);
                //bp.opeKakko(m_chips, ind);

                m_chips.get(ind).bufferpos(samplesLeft);
            }
        }

    public void begin(short[] buffer, int count) {
        m_sampleIndex = 0;
        m_sampleCount = count;
        m_sampleBuffer = buffer;
    }

    private void updateParams() {
        switch (m_buffers.size()) {
        case 1:
            // m_mix[0] = m_stereo ? (mixer_func_t)stereo_OneChip :
            // (mixer_func_t)mono1;
            // if (m_stereo) m_mix[1] = stereo_OneChip;
            if (m_stereo) {
                m_mix[0] = this::stereo_OneChip;
                m_mix[1] = this::stereo_OneChip;
            } else {
                m_mix[0] = this::mono1;
            }
            break;
        case 2:
            // m_mix[0] = m_stereo ? (mixer_func_t)stereo_ch1_TwoChips :
            // (mixer_func_t)mono2;
            // if (m_stereo) m_mix[1] = (mixer_func_t)stereo_ch2_TwoChips;
            if (m_stereo) {
                m_mix[0] = this::stereo_ch1_TwoChips;
                m_mix[1] = this::stereo_ch2_TwoChips;
            } else {
                m_mix[0] = this::mono2;
            }
            break;
        case 3:
            // m_mix[0] = m_stereo ? (mixer_func_t)stereo_ch1_ThreeChips :
            // (mixer_func_t)mono3;
            // if (m_stereo) m_mix[1] = (mixer_func_t)stereo_ch2_ThreeChips;
            if (m_stereo) {
                m_mix[0] = this::stereo_ch1_ThreeChips;
                m_mix[1] = this::stereo_ch2_ThreeChips;
            } else {
                m_mix[0] = this::mono3;
            }
            break;
        }
    }

    public void clearSids() {
        m_chips.clear();
        m_buffers.clear();
    }

    public void addSid(sidemu chip) {
        if (chip != null) {
            m_chips.add(chip);
            m_buffers.add(chip.buffer());
            if (m_iSamples == null)
                m_iSamples = new ArrayList<Integer>();// m_buffers.size());
            m_iSamples.add(0);

            if (m_mix.length > 0)
                updateParams();
        }
    }

    public void setStereo(Boolean stereo) {
        if (m_stereo != stereo) {
            m_stereo = stereo;

            m_mix = new mixer_func_t[m_stereo ? 2 : 1];

            updateParams();
        }
    }

    public Boolean setFastForward(int ff) {
        if (ff < 1 || ff > 32)
            return false;

        m_fastForwardFactor = ff;
        return true;
    }

    public void setVolume(int left, int right) {
        m_volume.clear();
        m_volume.add(left);
        m_volume.add(right);
    }

    private Integer[][] regs = new Integer[5][];

    public Integer[][] GetSidRegister() {
        for (int i = 0; i < Math.min(m_chips.size(), 5); i++) {
            regs[i] = m_chips.get(i).GetRegister();
        }
        return regs;
    }
}
