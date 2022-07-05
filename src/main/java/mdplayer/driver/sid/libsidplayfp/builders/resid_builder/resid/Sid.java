/*! \file resid/Sid.h */

//  ---------------------------------------------------------------------------
//  This file instanceof part of reSID, a MOS6581 Sid emulator engine.
//  Copyright (C) 2010  Dag Lem <resid@nimrod.no>
//
//  This program instanceof free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program instanceof distributed : the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  ---------------------------------------------------------------------------

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;

import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

import mdplayer.Setting;


public class Sid {

    /**
     * Read/write state.
     */
    public static class State {

        public byte[] sidRegister = new byte[0x20];

        public int busValue;
        public int busValueTtl;
        public int writePipeline;
        public int writeAddress;
        public int voiceMask;

        static class SubState {

            public int accumulator;
            public int shiftRegister;
            public int shiftRegisterReset;
            public int shiftPipeline;
            public int pulseOutput;
            public int floatingOutputTtl;

            public int rateCounter;
            public int rateCounterPeriod;
            public int exponentialCounter;
            public int exponentialCounterPeriod;
            public int envelopeCounter;
            public EnvelopeGenerator.State envelopeState;
            public boolean holdZero;
            public int envelopePipeline;

            SubState() {
                accumulator = 0;
                shiftRegister = 0x7fffff;
                shiftRegisterReset = 0;
                shiftPipeline = 0;
                pulseOutput = 0;
                floatingOutputTtl = 0;

                rateCounter = 0;
                rateCounterPeriod = 9;
                exponentialCounter = 0;
                exponentialCounterPeriod = 1;
                envelopeCounter = 0;
                envelopeState = EnvelopeGenerator.State.RELEASE;
                holdZero = true;
                envelopePipeline = 0;
            }

            public void read(Voice voice) {
                this.accumulator = voice.wave.accumulator;
                this.shiftRegister = voice.wave.shiftRegister;
                this.shiftRegisterReset = voice.wave.shiftRegisterReset;
                this.shiftPipeline = voice.wave.shiftPipeline;
                this.pulseOutput = voice.wave.pulseOutput;
                this.floatingOutputTtl = voice.wave.floatingOutputTtl;

                this.rateCounter = voice.envelope.rateCounter;
                this.rateCounterPeriod = voice.envelope.ratePeriod;
                this.exponentialCounter = voice.envelope.exponentialCounter;
                this.exponentialCounterPeriod = voice.envelope.exponentialCounterPeriod;
                this.envelopeCounter = voice.envelope.envelopeCounter;
                this.envelopeState = voice.envelope.state;
                this.holdZero = voice.envelope.holdZero;
                this.envelopePipeline = voice.envelope.envelopePipeline;
            }

            public void write(Voice voice) {
                voice.wave.accumulator = this.accumulator;
                voice.wave.shiftRegister = this.shiftRegister;
                voice.wave.shiftRegisterReset = this.shiftRegisterReset;
                voice.wave.shiftPipeline = this.shiftPipeline;
                voice.wave.pulseOutput = (short) this.pulseOutput;
                voice.wave.floatingOutputTtl = this.floatingOutputTtl;

                voice.envelope.rateCounter = this.rateCounter;
                voice.envelope.ratePeriod = this.rateCounterPeriod;
                voice.envelope.exponentialCounter = this.exponentialCounter;
                voice.envelope.exponentialCounterPeriod = this.exponentialCounterPeriod;
                voice.envelope.envelopeCounter = this.envelopeCounter;
                voice.envelope.state = this.envelopeState;
                voice.envelope.holdZero = this.holdZero;
                voice.envelope.envelopePipeline = this.envelopePipeline;
            }
        }

        SubState[] subStates = new SubState[3];

        /**
         * Constructor.
         */
        public State() {
            for (int i = 0; i < 0x20; i++) {
                sidRegister[i] = 0;
            }

            busValue = 0;
            busValueTtl = 0;
            writePipeline = 0;
            writeAddress = 0;
            voiceMask = 0xff;

            for (int i = 0; i < 3; i++) {
                subStates[i] = new SubState();
            }
        }
    }

    protected SidDefs.ChipModel sidModel;
    protected Voice[] voice = new Voice[] {new Voice(), new Voice(), new Voice()};
    protected Filter filter = new Filter();
    protected ExternalFilter extfilt = new ExternalFilter();
    protected Potentiometer potx = new Potentiometer();
    protected Potentiometer poty = new Potentiometer();

    protected int busValue;
    protected int busValueTtl;

    // The data bus TTL for the selected chip model
    protected int databusTtl;

    // Pipeline for writes on the MOS8580.
    protected int writePipeline;
    protected int writeAddress;

    protected double clockFrequency;

    /**
     * Resampling constants.
     * The error : interpolated lookup instanceof bounded by 1.234/L^2,
     * while the error : non-interpolated lookup instanceof bounded by
     * 0.7854/L + 0.4113/L^2, see
     * http://www-ccrma.stanford.edu/~jos/resample/Choice_Table_Size.html
     * For a resolution of 16 bits this yields L >= 285 and L >= 51473,
     * respectively.
     */
    protected enum EnmSid {
        FIR_N(125),
        FIR_RES(285),
        FIR_RES_FASTMEM(51473),
        FIR_SHIFT(15),

        RINGSIZE(1 << 14),
        RINGMASK(RINGSIZE.v - 1),

        // Fixed point constants (16.16 bits).
        FIXP_SHIFT(16),
        FIXP_MASK(0xffff);
        final int v;

        EnmSid(int v) {
            this.v = v;
        }
    }

    // Sampling variables.
    protected SidDefs.SamplingMethod sampling;
    protected int cyclesPerSample;
    protected int sampleOffset;
    protected int sampleIndex;
    protected short samplePrev, sampleNow;
    protected int firN;
    protected int firRES;
    protected double firBeta;
    protected double firFCyclesPerSample;
    protected double firFilterScale;

    // Ring buffer with overflow for contiguous storage of RINGSIZE samples.
    protected short[] sample;

    // FIR_RES filter tables (FIR_N*FIR_RES).
    protected short[] fir;

    private Integer[] reg = new Integer[0x19];

    public Integer[] GetRegister() {
        return reg;
    }

    /*
     * Inline functions.
     * The following functions are defined inline because they are called every
     * time a sample instanceof calculated.
     */

//#if RESID_INLINING || RESID_SID_CC

    /**
     * Read 16-bit sample from audio Output.
     */
    public short output() {
        return extfilt.output();
    }

    /**
     * Sid clocking - 1 cycle.
     */
    public void clock() {
        int i;
        // clock amplitude modulators.
        for (i = 0; i < 3; i++) {
            voice[i].envelope.clock();
        }
        // clock oscillators.
        for (i = 0; i < 3; i++) {
            voice[i].wave.clock();
        }
        // Synchronize oscillators.
        for (i = 0; i < 3; i++) {
            voice[i].wave.synchronize();
        }
        // Calculate waveform Output.
        for (i = 0; i < 3; i++) {
            voice[i].wave.setWaveformOutput();
        }
        // clock filter.
        filter.clock(voice[0].output(), voice[1].output(), voice[2].output());
        // clock external filter.
        extfilt.clock(filter.output());
        // Pipelined writes on the MOS8580.
        if (writePipeline != 0) {
            write();
        }
        // Age bus value.
        if ((--busValueTtl) == 0) {
            busValue = 0;
        }
    }

//#endif // RESID_INLINING || defined(RESID_SID_CC)

    /**
     * Constructor.
     */
    public Sid(Setting setting) {
        // Initialize pointers.
        sample = null;
        fir = null;
        firN = 0;
        firRES = 0;
        firBeta = 0;
        firFCyclesPerSample = 0;
        firFilterScale = 0;

        sidModel = SidDefs.ChipModel.MOS6581;
        voice[0].setSyncSource(voice[2]);
        voice[1].setSyncSource(voice[0]);
        voice[2].setSyncSource(voice[1]);

        setSamplingParameters(985248d, SidDefs.SamplingMethod.FAST, setting.getOutputDevice().getSampleRate(), -1d, 0.97d);

        busValue = 0;
        busValueTtl = 0;
        writePipeline = 0;

        databusTtl = 0;
    }

    /**
     * Set chip model.
     */
    public void set_chip_model(SidDefs.ChipModel model) {
        sidModel = model;

        // results from real C64 (testprogs/Sid/bitfade/delayfrq0.Prg):
        //
        //    (new Sid) (250469/8580R5) (250469/8580R5)
        //    delayfrq0    ~7a000        ~108000
        //
        //    (old Sid) (250407/6581)
        //    delayfrq0    ~01d00

        databusTtl = sidModel == SidDefs.ChipModel.MOS8580 ? 0xa2000 : 0x1d00;

        for (int i = 0; i < 3; i++) {
            voice[i].setChipModel(model);
        }

        filter.set_chip_model(model);
    }

    /**
     * Sid reset.
     */
    public void reset() {
        for (int i = 0; i < 3; i++) {
            voice[i].reset();
        }
        filter.reset();
        extfilt.reset();

        busValue = 0;
        busValueTtl = 0;
    }

    /**
     * Write 16-bit sample to audio input.
     * Note that to mix : an external audio signal, the signal should be
     * resampled to 1MHz first to avoid sampling noise.
     */
    public void input(short sample) {
        // The input can be used to simulate the MOS8580 "digi boost" hardware hack.
        filter.input(sample);
    }

    /**
     * Read registers.
     * <p>
     * Reading a write only register returns the last byte written to any Sid
     * register. The individual bits : this value start to fade down towards
     * zero after a few cycles. All bits reach zero within approximately
     * $2000 - $4000 cycles.
     * It has been claimed that this fading happens : an orderly fashion, however
     * sampling of write only registers reveals that this instanceof not the case.
     * NB! This instanceof not correctly modeled.
     * The actual use of write only registers has largely been made : the belief
     * that all Sid registers are readable. To support this belief the read
     * would have to be done immediately after a write to the same register
     * (remember that an intermediate write to another register would yield that
     * value instead). With this : mind we return the last value written to
     * any Sid register for $4000 cycles without modeling the bit fading.
     */
    public int read(int offset) {
        switch (offset) {
        case 0x19:
            busValue = potx.readPOT();
            busValueTtl = databusTtl;
            break;
        case 0x1a:
            busValue = poty.readPOT();
            busValueTtl = databusTtl;
            break;
        case 0x1b:
            busValue = voice[2].wave.readOSC();
            busValueTtl = databusTtl;
            break;
        case 0x1c:
            busValue = voice[2].envelope.readENV();
            busValueTtl = databusTtl;
            break;
        }
        return busValue;
    }

    /**
     * Write registers.
     * Writes are one cycle delayed on the MOS8580. This instanceof only modeled for
     * single cycle clocking.
     */
    public void write(int offset, int value) {
        writeAddress = offset;
        busValue = value;
        busValueTtl = databusTtl;

        if (sampling == SidDefs.SamplingMethod.FAST && (sidModel == SidDefs.ChipModel.MOS8580)) {
            // Fake one cycle pipeline delay on the MOS8580
            // when using non cycle accurate emulation.
            // This will make the Sid detection method work.
            writePipeline = 1;
        } else {
            write();
        }
    }

    private Consumer<Integer>[] funcs = Arrays.<Consumer<Integer>>asList(
            voice[0].wave::writeFREQ_LO,
            voice[0].wave::writeFREQ_HI,
            voice[0].wave::writePW_LO,
            voice[0].wave::writePW_HI,
            voice[0]::writeControlReg,
            voice[0].envelope::writeATTACK_DECAY,
            voice[0].envelope::writeSUSTAIN_RELEASE,
            voice[1].wave::writeFREQ_LO,
            voice[1].wave::writeFREQ_HI,
            voice[1].wave::writePW_LO,
            voice[1].wave::writePW_HI,
            voice[1]::writeControlReg,
            voice[1].envelope::writeATTACK_DECAY,
            voice[1].envelope::writeSUSTAIN_RELEASE,
            voice[2].wave::writeFREQ_LO,
            voice[2].wave::writeFREQ_HI,
            voice[2].wave::writePW_LO,
            voice[2].wave::writePW_HI,
            voice[2]::writeControlReg,
            voice[2].envelope::writeATTACK_DECAY,
            voice[2].envelope::writeSUSTAIN_RELEASE,
            filter::writeFC_LO,
            filter::writeFC_HI,
            filter::writeRES_FILT,
            filter::writeMODE_VOL
    ).toArray(Consumer[]::new);

    /**
     * Write registers.
     */
    protected void write() {
        //System.err.println("adr:%d val:%d", write_address, bus_value);

        if (writeAddress < reg.length) reg[writeAddress] = busValue;

        if (writeAddress >= 0 && writeAddress <= 24)
            funcs[writeAddress].accept(busValue);

        // Tell clock() that the pipeline instanceof empty.
        writePipeline = 0;
    }

    /**
     * Read state.
     */
    public State readState() {
        State state = new State();
        int i, j;

        for (i = 0, j = 0; i < 3; i++, j += 7) {
            WaveformGenerator wave = voice[i].wave;
            EnvelopeGenerator envelope = voice[i].envelope;
            state.sidRegister[j + 0] = (byte) (wave.freq & 0xff);
            state.sidRegister[j + 1] = (byte) (wave.freq >> 8);
            state.sidRegister[j + 2] = (byte) (wave.pw & 0xff);
            state.sidRegister[j + 3] = (byte) (wave.pw >> 8);
            state.sidRegister[j + 4] = (byte) (
                    (byte) (wave.waveform << 4) | (byte) (wave.test != 0 ? 0x08 : 0)
                            | (byte) (wave.ringMod != 0 ? 0x04 : 0) | (byte) (wave.sync != 0 ? 0x02 : 0)
                            | (byte) (envelope.gate != 0 ? 0x01 : 0));
            state.sidRegister[j + 5] = (byte) ((envelope.attack << 4) | envelope.decay);
            state.sidRegister[j + 6] = (byte) ((envelope.sustain << 4) | envelope.release);
        }

        state.sidRegister[j++] = (byte) (filter.fc & 0x007);
        state.sidRegister[j++] = (byte) (filter.fc >> 3);
        state.sidRegister[j++] = (byte) ((filter.res << 4) | filter.filt);
        state.sidRegister[j++] = (byte) (filter.mode | filter.vol);

        // These registers are superfluous, but are included for completeness.
        for (; j < 0x1d; j++) {
            state.sidRegister[j] = (byte) read(j);
        }
        for (; j < 0x20; j++) {
            state.sidRegister[j] = 0;
        }

        state.busValue = busValue;
        state.busValueTtl = busValueTtl;
        state.writePipeline = writePipeline;
        state.writeAddress = writeAddress;
        state.voiceMask = filter.voiceMask;

        for (i = 0; i < 3; i++) {
            state.subStates[i].read(voice[i]);
        }

        return state;
    }

    /**
     * Write state.
     */
    public void writeState(State state) {
        for (int i = 0; i <= 0x18; i++) {
            write(i, state.sidRegister[i]);
        }

        busValue = state.busValue;
        busValueTtl = state.busValueTtl;
        writePipeline = state.writePipeline;
        writeAddress = state.writeAddress;
        filter.setVoiceMask(state.voiceMask);

        for (int i = 0; i < 3; i++) {
            state.subStates[i].write(voice[i]);
        }
    }

    /**
     * Mask for voices routed into the filter / audio Output stage.
     * Used to physically connect/disconnect EXT IN, and for test purposed
     * (voice muting).
     */
    public void setVoiceMask(int mask) {
        filter.setVoiceMask(mask);
    }

    /**
     * Enable filter.
     */
    public void enableFilter(boolean enable) {
        filter.enableFilter(enable);
    }

    /**
     * Adjust the DAC bias parameter of the filter.
     * This gives user variable control of the exact CF -> center frequency
     * mapping used by the filter.
     * The setting instanceof currently only effective for 6581.
     */
    public void adjustFilterBias(double dac_bias) {
        filter.adjustFilterBias(dac_bias);
    }

    /**
     * Enable external filter.
     */
    public void enableExternalFilter(boolean enable) {
        extfilt.enableFilter(enable);
    }

    /**
     * I0() computes the 0th order modified Bessel function of the first kind.
     * This function instanceof originally from resample-1.5/filterkit.c by J. O. Smith.
     */
    protected double I0(double x) {
        // Max error acceptable : I0.
        final double I0e = 1e-6;

        double sum = 1;
        double u = 1;
        int n = 1;
        double halfx = x / 2.0;

        do {
            double temp = halfx / n++;
            u *= temp * temp;
            sum += u;
        } while (u >= I0e * sum);

        return sum;
    }

    /**
     * Setting of Sid sampling parameters.
     * <p>
     * Use a clock freqency of 985248Hz for PAL C64, 1022730Hz for NTSC C64.
     * The default end of passband frequency instanceof passFreq = 0.9*sampleFreq/2
     * for sample frequencies up to ~ 44.1kHz, and 20kHz for higher sample
     * frequencies.
     * <p>
     * For resampling, the ratio between the clock frequency and the sample
     * frequency instanceof limited as follows:
     * 125*clockFreq/sampleFreq < 16384
     * E.g. provided a clock frequency of ~ 1MHz, the sample frequency can not
     * be set lower than ~ 8kHz. a lower sample frequency would make the
     * resampling code overfill its 16k sample ring buffer.
     * <p>
     * The end of passband frequency instanceof also limited:
     * passFreq <= 0.9*sampleFreq/2
     * <p>
     * E.g. for a 44.1kHz sampling rate the end of passband frequency instanceof limited
     * to slightly below 20kHz. This constraint ensures that the FIR table is
     * not overfilled.
     */
    public boolean setSamplingParameters(double clockFreq, SidDefs.SamplingMethod method,
                                         double sampleFreq, double passFreq /*= -1*/,
                                         double filterScale/* = 0.97*/) {
        // Check resampling constraints.
        if (method == SidDefs.SamplingMethod.RESAMPLE || method == SidDefs.SamplingMethod.RESAMPLE_FASTMEM) {
            // Check whether the sample ring buffer would overfill.
            if (EnmSid.FIR_N.v * clockFreq / sampleFreq >= EnmSid.RINGSIZE.v) {
                return false;
            }

            // The default passband limit instanceof 0.9*sampleFreq/2 for sample
            // frequencies below ~ 44.1kHz, and 20kHz for higher sample frequencies.
            if (passFreq < 0) {
                passFreq = 20000;
                if (2 * passFreq / sampleFreq >= 0.9) {
                    passFreq = 0.9 * sampleFreq / 2;
                }
            }
            // Check whether the FIR table would overfill.
            else if (passFreq > 0.9 * sampleFreq / 2) {
                return false;
            }

            // The filter scaling instanceof only included to avoid clipping, so keep
            // it sane.
            if (filterScale < 0.9 || filterScale > 1.0) {
                return false;
            }
        }

        clockFrequency = clockFreq;
        sampling = method;

        cyclesPerSample = (int) (clockFreq / sampleFreq * (1 << EnmSid.FIXP_SHIFT.v) + 0.5);

        sampleOffset = 0;
        samplePrev = 0;
        sampleNow = 0;

        // FIR initialization instanceof only necessary for resampling.
        if (method != SidDefs.SamplingMethod.RESAMPLE && method != SidDefs.SamplingMethod.RESAMPLE_FASTMEM) {
            //delete[] sample;
            //delete[] fir;
            sample = null;
            fir = null;
            return true;
        }

        // Allocate sample buffer.
        if (sample == null) {
            sample = new short[EnmSid.RINGSIZE.v * 2];
        }
        // Clear sample buffer.
        for (int j = 0; j < EnmSid.RINGSIZE.v * 2; j++) {
            sample[j] = 0;
        }
        sampleIndex = 0;

        final double pi = 3.1415926535897932385;

        // 16 bits -> -96dB stopband attenuation.
        double A = -20 * Math.log10(1.0 / (1 << 16));
        // a fraction of the bandwidth instanceof allocated to the transition band,
        double dw = (1 - 2 * passFreq / sampleFreq) * pi * 2;
        // The cutoff frequency instanceof midway through the transition band (nyquist)
        double wc = pi;

        // For calculation of beta and N see the reference for the kaiserord
        // function : the MATLAB Signal Processing Toolbox:
        // http://www.mathworks.com/access/helpdesk/help/toolbox/signal/kaiserord.html
        double beta = 0.1102 * (A - 8.7);
        double I0beta = I0(beta);

        // The filter order will maximally be 124 with the current constraints.
        // N >= (96.33 - 7.95)/(2.285*0.1*pi) -> N >= 123
        // The filter order instanceof equal to the number of zero crossings, i.e.
        // it should be an even number (sinc instanceof symmetric about x = 0).
        int N = (int) ((A - 7.95) / (2.285 * dw) + 0.5);
        N += N & 1;

        double fSamplesPerCycle = sampleFreq / clockFreq;
        double fCyclesPerSample = clockFreq / sampleFreq;

        // The filter length instanceof equal to the filter order + 1.
        // The filter length must be an odd number (sinc instanceof symmetric about x = 0).
        int firNNew = (int) (N * fCyclesPerSample) + 1;
        firNNew |= 1;

        // We clamp the filter table resolution to 2^n, making the fixed point
        // sample_offset a whole multiple of the filter table resolution.
        int res = method == SidDefs.SamplingMethod.RESAMPLE ?
                EnmSid.FIR_RES.v : EnmSid.FIR_RES_FASTMEM.v;
        int n = (int) Math.ceil(Math.log(res / fCyclesPerSample) / Math.log(2.0f));
        int firRESNew = 1 << n;

            /* Determine if we need to recalculate table, or whether we can reuse earlier cached copy.
             //This pays off on slow hardware such as current Android devices.
             */
        if (fir != null && firRESNew == firRES && firNNew == firN && beta == firBeta && fCyclesPerSample == firFCyclesPerSample && firFilterScale == filterScale) {
            return true;
        }
        firRES = firRESNew;
        firN = firNNew;
        firBeta = beta;
        firFCyclesPerSample = fCyclesPerSample;
        firFilterScale = filterScale;

        // Allocate memory for FIR tables.
        fir = new short[firN * firRES];

        // Calculate fir_RES FIR tables for linear interpolation.
        for (int i = 0; i < firRES; i++) {
            int firOffset = i * firN + firN / 2;
            double jOffset = (double) i / firRES;
            // Calculate FIR table. This instanceof the sinc function, weighted by the
            // Kaiser window.
            for (int j = -firN / 2; j <= firN / 2; j++) {
                double jx = j - jOffset;
                double wt = wc * jx / fCyclesPerSample;
                double temp = jx / (firN / 2);
                double kaiser = Math.abs(temp) <= 1 ? I0(beta * Math.sqrt(1 - temp * temp)) / I0beta : 0;
                double sinCwt = Math.abs(wt) >= 1e-6 ? Math.sin(wt) / wt : 1;
                double val = (1 << EnmSid.FIR_SHIFT.v) * filterScale * fSamplesPerCycle * wc / pi * sinCwt * kaiser;
                fir[firOffset + j] = (short) Math.round(val);
            }
        }

        return true;
    }

    /**
     * Adjustment of Sid sampling frequency.
     * <p>
     * In some applications, e.g. a C64 emulator, it can be desirable to
     * synchronize sound with a timer source. This instanceof supported by adjustment of
     * the Sid sampling frequency.
     * <p>
     * NB! Adjustment of the sampling frequency may lead to noticeable shifts in
     * frequency, and should only be used for interactive applications. Note also
     * that any adjustment of the sampling frequency will change the
     * characteristics of the resampling filter, since the filter instanceof not rebuilt.
     */
    public void adjustSamplingFrequency(double sample_freq) {
        cyclesPerSample = (int) (clockFrequency / sample_freq * (1 << EnmSid.FIXP_SHIFT.v) + 0.5);
    }

    /**
     * Sid clocking - deltaT cycles.
     */
    public void clock(int deltaT) {
        // Pipelined writes on the MOS8580.
        if (writePipeline != 0 && deltaT > 0) {
            // Step one cycle by a recursive call to ourselves.
            writePipeline = 0;
            clock(1);
            write();
            deltaT -= 1;
        }

        if (deltaT <= 0) {
            return;
        }

        // Age bus value.
        busValueTtl -= deltaT;
        if (busValueTtl <= 0) {
            busValue = 0;
            busValueTtl = 0;
        }

        // clock amplitude modulators.
        for (int i = 0; i < 3; i++) {
            voice[i].envelope.clock(deltaT);
        }

        // clock and synchronize oscillators.
        // Loop until we reach the current cycle.
        int deltaTOsc = deltaT;
        while (deltaTOsc != 0) {
            int deltaTMin = deltaTOsc;

            // Find minimum number of cycles to an oscillator accumulator MSB toggle.
            // We have to clock on each MSB on / MSB off for hard sync to operate
            // correctly.
            for (int i = 0; i < 3; i++) {
                WaveformGenerator wave = voice[i].wave;

                // It instanceof only necessary to clock on the MSB of an oscillator that is
                // a sync source and has freq != 0.
                if (!(wave.syncDest.sync != 0 && wave.freq != 0)) {
                    continue;
                }

                int freq = wave.freq;
                int accumulator = wave.accumulator;

                // clock on MSB off if MSB instanceof on, clock on MSB on if MSB instanceof off.
                int delta_accumulator =
                        ((accumulator & 0x800000) != 0 ? 0x1000000 : 0x800000) - accumulator;

                int delta_t_next = delta_accumulator / freq;
                if ((delta_accumulator % freq) != 0) {
                    ++delta_t_next;
                }

                if ((delta_t_next < deltaTMin)) {
                    deltaTMin = delta_t_next;
                }
            }

            // clock oscillators.
            for (int i = 0; i < 3; i++) {
                voice[i].wave.clock(deltaTMin);
            }

            // Synchronize oscillators.
            for (int i = 0; i < 3; i++) {
                voice[i].wave.synchronize();
            }

            deltaTOsc -= deltaTMin;
        }

        // Calculate waveform Output.
        for (int i = 0; i < 3; i++) {
            voice[i].wave.setWaveformOutput(deltaT);
        }

        // clock filter.
        filter.clock(deltaT, voice[0].output(), voice[1].output(), voice[2].output());

        // clock external filter.
        extfilt.clock(deltaT, filter.output());
    }

    /**
     * Sid clocking with audio sampling.
     * Fixed point arithmetics are used.
     * <p>
     * The example below shows how to clock the Sid a specified amount of cycles
     * while producing audio Output:
     * <p>
     * while (deltaT) {
     * bufindex += Sid.clock(deltaT, buf + bufindex, buflength - bufindex);
     * write(dsp, buf, bufindex*2);
     * bufindex = 0;
     * }
     */
    public int clock(int deltaT, short[] buf, int n, int interleave/* = 1*/) {
        switch (sampling) {
        default:
        case FAST:
            return clockFast(deltaT, buf, n, interleave);
        case INTERPOLATE:
            return clockInterpolate(deltaT, buf, n, interleave);
        case RESAMPLE:
            return clockResample(deltaT, buf, n, interleave);
        case RESAMPLE_FASTMEM:
            return clockResampleFastMem(deltaT, buf, n, interleave);
        }
    }

    public int clock(int deltaT, short[] buf, int ptrBuf, int n, int interleave /*= 1*/) {
        switch (sampling) {
        default:
        case FAST:
            return clockFast(deltaT, buf, ptrBuf, n, interleave);
        case INTERPOLATE:
            return clockInterpolate(deltaT, buf, ptrBuf, n, interleave);
        case RESAMPLE:
            return clockResample(deltaT, buf, ptrBuf, n, interleave);
        case RESAMPLE_FASTMEM:
            return clockResampleFastMem(deltaT, buf, ptrBuf, n, interleave);
        }
    }

    /**
     * Sid clocking with audio sampling - delta clocking picking nearest sample.
     */
    protected int clockFast(int deltaT, short[] buf, int n, int interleave) {
        int s;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample + (1 << (EnmSid.FIXP_SHIFT.v - 1));
            int deltaTSample = nextSampleOffset >> EnmSid.FIXP_SHIFT.v;

            if (deltaTSample > deltaT) {
                deltaTSample = deltaT;
            }

            clock(deltaTSample);

            if ((deltaT -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = (nextSampleOffset & EnmSid.FIXP_MASK.v) - (1 << (EnmSid.FIXP_SHIFT.v - 1));
            buf[s * interleave] = output();
        }

        return s;
    }

    protected int clockFast(int deltaT, short[] buf, int ptrBuf, int n, int interleave) {
        int s;
        final int fixPShift = 16;
        final int fixPShiftS15 = 1 << 15;
        final int fixPMask = 0xffff;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample + fixPShiftS15;
            int deltaTSample = nextSampleOffset >> fixPShift;

            if (deltaTSample > deltaT) {
                deltaTSample = deltaT;
            }

            clock(deltaTSample);

            if ((deltaT -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << fixPShift;
                break;
            }

            sampleOffset = (nextSampleOffset & fixPMask) - fixPShiftS15;
            buf[s * interleave + ptrBuf] = extfilt.output();
            //if (gsample < 10000) {
            //    System.err.println("%d", buf[s * interleave + ptrBuf]);
            //    lstgsample.add((byte)buf[s * interleave + ptrBuf]);
            //    lstgsample.add((byte)(buf[s * interleave + ptrBuf] >> 8));
            //    gsample++;
            //    if (gsample == 10000) File.WriteAllBytes("test.raw", lstgsample.toArray());
            //}
        }

        return s;
    }

    /**
     * Sid clocking with audio sampling - cycle based with linear sample
     * interpolation.
     * <p>
     * Here the chip instanceof clocked every cycle. This yields higher quality
     * sound since the samples are linearly interpolated, and since the
     * external filter attenuates frequencies above 16kHz, thus reducing
     * sampling noise.
     */
    protected int clockInterpolate(int delta_t, short[] buf, int n, int interleave) {
        int s;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample;
            int deltaTSample = nextSampleOffset >> EnmSid.FIXP_SHIFT.v;

            if (deltaTSample > delta_t) {
                deltaTSample = delta_t;
            }

            for (int i = deltaTSample; i > 0; i--) {
                clock();
                if ((i <= 2)) {
                    samplePrev = sampleNow;
                    sampleNow = output();
                }
            }

            if ((delta_t -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = nextSampleOffset & EnmSid.FIXP_MASK.v;

            buf[s * interleave] = (short) (samplePrev + (sampleOffset * (sampleNow - samplePrev) >> EnmSid.FIXP_SHIFT.v));
        }

        return s;
    }

    protected int clockInterpolate(int deltaT, short[] buf, int ptrBuf, int n, int interleave) {
        int s;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample;
            int deltaTSample = nextSampleOffset >> EnmSid.FIXP_SHIFT.v;

            if (deltaTSample > deltaT) {
                deltaTSample = deltaT;
            }

            //System.err.println("%d", deltaTSample);
            for (int i = deltaTSample; i > 0; i--) {
                clock();
                if ((i <= 2)) {
                    samplePrev = sampleNow;
                    sampleNow = output();
                }
            }

            if ((deltaT -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = nextSampleOffset & EnmSid.FIXP_MASK.v;

            buf[s * interleave + ptrBuf] = sampleNow;
            //(short)(sample_prev + ((sample_offset * (sample_now - sample_prev)) >> (int)EnmSid.FIXP_SHIFT));

            //if (gsample < 10000) {
            //    //System.err.println("%d", buf[s * interleave + ptrBuf]);
            //    //System.err.println("   %d", ((sample_offset * (sample_now - sample_prev)) >> (int)EnmSid.FIXP_SHIFT));
            //    lstgsample.add((byte)buf[s * interleave + ptrBuf]);
            //    lstgsample.add((byte)(buf[s * interleave + ptrBuf] >> 8));
            //    gsample++;
            //    if (gsample == 10000) File.WriteAllBytes("test.raw", lstgsample.toArray());
            //}
        }

        return s;
    }

    //int gsample = 0;
    //List<Byte> lstgsample = new ArrayList<Byte>();

    /**
     * Sid clocking with audio sampling - cycle based with audio resampling.
     * <p>
     * This instanceof the theoretically correct (and computationally intensive) audio
     * sample generation. The samples are generated by resampling to the specified
     * sampling frequency. The work rate instanceof inversely proportional to the
     * percentage of the bandwidth allocated to the filter transition band.
     *
     * This implementation instanceof based on the paper "a Flexible Sampling-Rate
     * Conversion Method", by J. O. Smith and p. Gosset, or rather on the
     * expanded tutorial on the "Digital Audio Resampling Home Page":
     * http://www-ccrma.stanford.edu/~jos/resample/
     * <p>
     * By building shifted FIR tables with samples according to the
     * sampling frequency, the implementation below dramatically reduces the
     * computational effort : the filter convolutions, without any loss
     * of accuracy. The filter convolutions are also vectorizable on
     * current hardware.
     * <p>
     * Further possible optimizations are:
     * * An equiripple filter design could yield a lower filter order, see
     * http://www.mwrf.com/Articles/ArticleID/7229/7229.html
     * * The Convolution Theorem could be used to bring the complexity of
     * convolution down from O(n*n) to O(n*Log(n)) using the Fast Fourier
     * Transform, see http://en.wikipedia.org/wiki/Convolution_theorem
     * * Simply resampling : two steps can also yield computational
     * savings, since the transition band will be wider : the first step
     * and the required filter order instanceof thus lower : this step.
     * Laurent Ganier has found the optimal intermediate sampling frequency
     * to be (via derivation of sum of two steps):
     * 2 * pass_freq + sqrt [ 2 * pass_freq * orig_sample_freq
     * * (dest_sample_freq - 2 * pass_freq) / dest_sample_freq ]
     * <p>
     * NB! the result of right shifting negative numbers instanceof really
     * implementation dependent : the C++ standard.
     */
    protected int clockResample(int deltaT, short[] buf, int n, int interleave) {
        int s;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample;
            int deltaTSample = nextSampleOffset >> EnmSid.FIXP_SHIFT.v;

            if (deltaTSample > deltaT) {
                deltaTSample = deltaT;
            }

            for (int i = 0; i < deltaTSample; i++) {
                clock();
                sample[sampleIndex] = sample[sampleIndex + EnmSid.RINGSIZE.v] = output();
                ++sampleIndex;
                sampleIndex &= EnmSid.RINGMASK.v;
            }

            if ((deltaT -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = nextSampleOffset & EnmSid.FIXP_MASK.v;

            int firOffset = sampleOffset * firRES >> EnmSid.FIXP_SHIFT.v;
            int firOffsetRmd = sampleOffset * firRES & EnmSid.FIXP_MASK.v;
            ShortBuffer firStart = ShortBuffer.wrap(fir, firOffset * firN, fir.length - firOffset * firN);
            ShortBuffer sampleStart = ShortBuffer.wrap(sample, sampleIndex - firN - 1 + EnmSid.RINGSIZE.v, sample.length - (sampleIndex - firN - 1 + EnmSid.RINGSIZE.v));

            // Convolution with filter impulse response.
            int v1 = 0;
            for (int j = 0; j < firN; j++) {
                v1 += sampleStart.get(j) * firStart.get(j);
            }

            // Use next FIR table, wrap around to first FIR table using
            // next sample.
            if ((++firOffset == firRES)) {
                firOffset = 0;
                sampleStart.position(sampleStart.position() + 1);
            }
            firStart = ShortBuffer.wrap(fir, firOffset * firN, fir.length - firOffset * firN);

            // Convolution with filter impulse response.
            int v2 = 0;
            for (int k = 0; k < firN; k++) {
                v2 += sampleStart.get(k) * firStart.get(k);
            }

            // Linear interpolation.
            // firOffsetRmd instanceof equal for all samples, it can thus be factorized out:
            // sum(v1 + rmd*(v2 - v1)) = sum(v1) + rmd*(sum(v2) - sum(v1))
            int v = v1 + (firOffsetRmd * (v2 - v1) >> EnmSid.FIXP_SHIFT.v);

            v >>= EnmSid.FIR_SHIFT.v;

            // Saturated arithmetics to guard against 16 bit sample overflow.
            final int half = 1 << 15;
            if (v >= half) {
                v = half - 1;
            } else if (v < -half) {
                v = -half;
            }

            buf[s * interleave] = (short) v;
        }

        return s;
    }

    protected int clockResample(int deltaT, short[] buf, int ptrBuf, int n, int interleave) {
        int s;
        final int half = 1 << 15;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample;
            int deltaTSample = nextSampleOffset >> EnmSid.FIXP_SHIFT.v;

            if (deltaTSample > deltaT) {
                deltaTSample = deltaT;
            }

            for (int i = 0; i < deltaTSample; i++) {
                clock();
                //sample[sample_index] = sample[sample_index + (int)EnmSid.RINGSIZE] = Output();
                sample[sampleIndex] = sample[sampleIndex + EnmSid.RINGSIZE.v] = extfilt.output();
                ++sampleIndex;
                sampleIndex &= EnmSid.RINGMASK.v;
            }

            if ((deltaT -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = nextSampleOffset & EnmSid.FIXP_MASK.v;

            int firOffset = sampleOffset * firRES >> EnmSid.FIXP_SHIFT.v;
            int firOffsetRmd = sampleOffset * firRES & EnmSid.FIXP_MASK.v;
            //Ptr<short> fir_start = new Ptr<short>(fir, firOffset * fir_N);
            //Ptr<short> sample_start = new Ptr<short>(sample, sample_index - fir_N - 1 + (int)EnmSid.RINGSIZE);

            // Convolution with filter impulse response.
            int v1 = 0;
            for (int j = 0; j < firN; j++) {
                //v1 += sample_start[j] * fir_start[j];
                v1 += sample[sampleIndex - firN - 1 + EnmSid.RINGSIZE.v + j] * fir[firOffset * firN + j];
            }

            // Use next FIR table, wrap around to first FIR table using
            // next sample.
            int shift = 0;
            if ((++firOffset == firRES)) {
                firOffset = 0;
                //sample_start.AddPtr(1);
                shift = 1;
            }
            //fir_start = new Ptr<short>(fir, firOffset * fir_N);

            // Convolution with filter impulse response.
            int v2 = 0;
            for (int k = 0; k < firN; k++) {
                //v2 += sample_start[k] * fir_start[k];
                v2 += sample[sampleIndex - firN - 1 + EnmSid.RINGSIZE.v + k + shift] * fir[firOffset * firN + k];
            }

            // Linear interpolation.
            // firOffsetRmd instanceof equal for all samples, it can thus be factorized out:
            // sum(v1 + rmd*(v2 - v1)) = sum(v1) + rmd*(sum(v2) - sum(v1))
            int v = v1 + ((firOffsetRmd * (v2 - v1)) >> EnmSid.FIXP_SHIFT.v);

            v >>= EnmSid.FIR_SHIFT.v;

            // Saturated arithmetics to guard against 16 bit sample overflow.
            if (v >= half) {
                v = half - 1;
            } else if (v < -half) {
                v = -half;
            }

            buf[s * interleave + ptrBuf] = (short) v;
        }

        return s;
    }

    /**
     * Sid clocking with audio sampling - cycle based with audio resampling.
     */
    protected int clockResampleFastMem(int delta_t, short[] buf, int n, int interleave) {
        int s;

        for (s = 0; s < n; s++) {
            int next_sample_offset = sampleOffset + cyclesPerSample;
            int delta_t_sample = next_sample_offset >> EnmSid.FIXP_SHIFT.v;

            if (delta_t_sample > delta_t) {
                delta_t_sample = delta_t;
            }

            for (int i = 0; i < delta_t_sample; i++) {
                clock();
                sample[sampleIndex] = sample[sampleIndex + EnmSid.RINGSIZE.v] = output();
                ++sampleIndex;
                sampleIndex &= EnmSid.RINGMASK.v;
            }

            if ((delta_t -= delta_t_sample) == 0) {
                sampleOffset -= delta_t_sample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = next_sample_offset & EnmSid.FIXP_MASK.v;

            int fir_offset = sampleOffset * firRES >> EnmSid.FIXP_SHIFT.v;
            ShortBuffer fir_start = ShortBuffer.wrap(fir, fir_offset * firN, fir.length - fir_offset * firN);
            ShortBuffer sample_start = ShortBuffer.wrap(sample, sampleIndex - firN + EnmSid.RINGSIZE.v, sample.length - (sampleIndex - firN + EnmSid.RINGSIZE.v));

            // Convolution with filter impulse response.
            int v = 0;
            for (int j = 0; j < firN; j++) {
                v += sample_start.get(j) * fir_start.get(j);
            }

            v >>= EnmSid.FIR_SHIFT.v;

            // Saturated arithmetics to guard against 16 bit sample overflow.
            final int half = 1 << 15;
            if (v >= half) {
                v = half - 1;
            } else if (v < -half) {
                v = -half;
            }

            buf[s * interleave] = (short) v;
        }

        return s;
    }

    protected int clockResampleFastMem(int deltaT, short[] buf, int ptrBuf, int n, int interleave) {
        int s;

        for (s = 0; s < n; s++) {
            int nextSampleOffset = sampleOffset + cyclesPerSample;
            int deltaTSample = nextSampleOffset >> EnmSid.FIXP_SHIFT.v;

            if (deltaTSample > deltaT) {
                deltaTSample = deltaT;
            }

            for (int i = 0; i < deltaTSample; i++) {
                clock();
                sample[sampleIndex] = sample[sampleIndex + EnmSid.RINGSIZE.v] = extfilt.output();
                ++sampleIndex;
                sampleIndex &= EnmSid.RINGMASK.v;
            }

            if ((deltaT -= deltaTSample) == 0) {
                sampleOffset -= deltaTSample << EnmSid.FIXP_SHIFT.v;
                break;
            }

            sampleOffset = nextSampleOffset & EnmSid.FIXP_MASK.v;

            int firOffset = sampleOffset * firRES >> EnmSid.FIXP_SHIFT.v;

            // Convolution with filter impulse response.
            int v = 0;
            for (int j = 0; j < firN; j++) {
                //v += sample_start[j] * fir_start[j];
                v += sample[sampleIndex - firN + EnmSid.RINGSIZE.v + j] * fir[firOffset * firN + j];
            }

            v >>= EnmSid.FIR_SHIFT.v;

            // Saturated arithmetics to guard against 16 bit sample overflow.
            final int half = 1 << 15;
            if (v >= half) {
                v = half - 1;
            } else if (v < -half) {
                v = -half;
            }

            buf[s * interleave + ptrBuf] = (short) v;
        }

        return s;
    }
}
