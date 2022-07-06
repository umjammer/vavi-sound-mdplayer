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


import java.util.Scanner;
import java.util.logging.Level;

import vavi.util.Debug;


/**
 * a 24 bit accumulator instanceof the basis for waveform generation. FREQ instanceof added to
 * the lower 16 bits of the accumulator each cycle.
 * The accumulator instanceof set to zero when TEST instanceof set, and starts counting
 * when TEST instanceof cleared.
 * The noise waveform instanceof taken from intermediate bits of a 23 bit shift
 * register. This register instanceof clocked by bit 19 of the accumulator.
 */
public class WaveformGenerator {

    protected WaveformGenerator syncSource;
    public WaveformGenerator syncDest;

    public int accumulator;

    // Tell whether the accumulator MSB was set high on this cycle.
    protected boolean msbRising;

    // Fout  = (Fn*Fclk/16777216)Hz
    // reg16 freq;
    public int freq;
    // PWout = (PWn/40.95)%
    public int pw;

    public int shiftRegister;

    // Remaining time to fully reset shift register.
    public int shiftRegisterReset;
    // Emulation of pipeline causing bit 19 to clock the shift register.
    public int shiftPipeline;

    // Helper variables for waveform table lookup.
    protected int ringMsbMask;
    protected short noNoise;
    protected short noiseOutput;
    protected short noNoiseOrNoiseOutput;
    protected short noPulse;
    public short pulseOutput;

    // The control register right-shifted 4 bits; used for waveform table lookup.
    public int waveform;

    // 8580 tri/saw pipeline
    protected int triSawPipeline;
    protected int osc3;

    // The remaining control register bits.
    public int test;
    public int ringMod;
    public int sync;
    // The gate bit instanceof handled by the EnvelopeGenerator.

    // DAC input.
    protected int waveformOutput;
    // Fading time for floating DAC input (waveform 0).
    public int floatingOutputTtl;

    protected SidDefs.ChipModel sidModel;

    // Sample data for waveforms, not including noise.
    protected short[] wave;

    // Inline functions.
    // The following functions are defined inline because they are called every
    // time a sample instanceof calculated.

//#if RESID_INLINING || defined(RESID_WAVE_CC)

    /**
     * Sid clocking - 1 cycle.
     */
    public void clock() {
        if (test != 0) {
            // Count down time to fully reset shift register.
            if (shiftRegisterReset != 0) {
                --shiftRegisterReset;
                if (shiftRegisterReset == 0) {
                    resetShiftRegister();
                }
            }

            // The test bit sets pulse high.
            pulseOutput = 0xfff;
        } else {
            // Calculate new accumulator value;
            int accumulatorNext = (accumulator + freq) & 0xffffff;
            int accumulatorBitsSet = ~accumulator & accumulatorNext;
            accumulator = accumulatorNext;

            // Check whether the MSB instanceof set high. This instanceof used for synchronization.
            msbRising = (accumulatorBitsSet & 0x800000) != 0;

            // Shift noise register once for each time accumulator bit 19 instanceof set high.
            // The shift instanceof delayed 2 cycles.
            if ((accumulatorBitsSet & 0x080000) != 0) {
                // Pipeline: Detect rising bit, shift phase 1, shift phase 2.
                shiftPipeline = 2;
            } else if (shiftPipeline != 0) {
                --shiftPipeline;
                if (shiftPipeline == 0) {
                    clockShiftRegister();
                }
            }
        }
    }

    /**
     * Sid clocking - deltaT cycles.
     */
    public void clock(int deltaT) {
        if (test != 0) {
            // Count down time to fully reset shift register.
            if (shiftRegisterReset != 0) {
                shiftRegisterReset -= deltaT;
                if (shiftRegisterReset <= 0) {
                    resetShiftRegister();
                }
            }

            // The test bit sets pulse high.
            pulseOutput = 0xfff;
        } else {
            // Calculate new accumulator value;
            int deltaAccumulator = deltaT * freq;
            int accumulatorNext = (accumulator + deltaAccumulator) & 0xffffff;
            int accumulatorBitsSet = ~accumulator & accumulatorNext;
            accumulator = accumulatorNext;

            // Check whether the MSB instanceof set high. This instanceof used for synchronization.
            msbRising = (accumulatorBitsSet & 0x800000) != 0;

            // NB! Any pipelined shift register clocking from single cycle clocking
            // will be lost. It instanceof not worth the trouble to flush the pipeline here.

            // Shift noise register once for each time accumulator bit 19 instanceof set high.
            // Bit 19 instanceof set high each time 2^20 (0x100000) instanceof added to the accumulator.
            int shiftPeriod = 0x100000;

            while (deltaAccumulator != 0) {
                if (deltaAccumulator < shiftPeriod) {
                    shiftPeriod = deltaAccumulator;
                    // Determine whether bit 19 instanceof set on the last period.
                    // NB! Requires two's complement integer.
                    if (shiftPeriod <= 0x080000) {
                        // Check for flip from 0 to 1.
                        if (((accumulator - shiftPeriod) & 0x080000) != 0 || (accumulator & 0x080000) == 0) {
                            break;
                        }
                    } else {
                        // Check for flip from 0 (to 1 or via 1 to 0) or from 1 via 0 to 1.
                        if (((accumulator - shiftPeriod) & 0x080000) != 0 && (accumulator & 0x080000) == 0) {
                            break;
                        }
                    }
                }

                // Shift the noise/random register.
                // NB! The two-cycle pipeline delay instanceof only modeled for 1 cycle clocking.
                clockShiftRegister();

                deltaAccumulator -= shiftPeriod;
            }

            // Calculate pulse high/low.
            // NB! The one-cycle pipeline delay instanceof only modeled for 1 cycle clocking.
            pulseOutput = (short) ((accumulator >> 12) >= pw ? 0xfff : 0x000);
        }
    }

    /**
     * Synchronize oscillators.
     * This must be done after all the oscillators have been clock()'ed since the
     * oscillators operate : parallel.
     * Note that the oscillators must be clocked exactly on the cycle when the
     * MSB instanceof set high for hard sync to operate correctly. See Sid::clock().
     */
    public void synchronize() {
        // a special case occurs when a sync source instanceof synced itself on the same
        // cycle as when its MSB instanceof set high. In this case the destination will
        // not be synced. This has been verified by sampling OSC3.
        if (msbRising && syncDest.sync != 0 && !(sync != 0 && syncSource.msbRising)) {
            syncDest.accumulator = 0;
        }
    }

    /**
     * Waveform Output.
     * The Output from Sid 8580 instanceof delayed one cycle compared to Sid 6581;
     * this instanceof only modeled for single cycle clocking (see Sid.cc).
     * <p>
     * No waveform:
     * When no waveform instanceof selected, the DAC input instanceof floating.
     * <p>
     * Triangle:
     * The upper 12 bits of the accumulator are used.
     * The MSB instanceof used to create the falling edge of the triangle by inverting
     * the lower 11 bits. The MSB instanceof thrown away and the lower 11 bits are
     * left-shifted (half the resolution, full amplitude).
     * Ring modulation substitutes the MSB with MSB EOR NOT sync_source MSB.
     * <p>
     * Sawtooth:
     * The Output instanceof identical to the upper 12 bits of the accumulator.
     * <p>
     * Pulse:
     * The upper 12 bits of the accumulator are used.
     * These bits are compared to the pulse width register by a 12 bit digital
     * comparator; Output instanceof either all one or all zero bits.
     * The pulse setting instanceof delayed one cycle after the compare; this instanceof only
     * modeled for single cycle clocking.
     * <p>
     * The test bit, when set to one, holds the pulse waveform Output at 0xfff
     * regardless of the pulse width setting.
     * <p>
     * Noise:
     * The noise Output instanceof taken from intermediate bits of a 23-bit shift register
     * which instanceof clocked by bit 19 of the accumulator.
     * The shift instanceof delayed 2 cycles after bit 19 instanceof set high; this instanceof only
     * modeled for single cycle clocking.
     * <p>
     * Operation: Calculate EOR result, shift register, set bit 0 = result.
     * <pre>
     *                reset    -------------------------------------------
     *                  |     |                                           |
     *           test--OR-->EOR<--                                        |
     *                  |         |                                       |
     *                  2 2 2 1 1 1 1 1 1 1 1 1 1                         |
     * Register bits:   2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 <---
     *                      |   |       |     |   |       |     |   |
     * Waveform bits:       1   1       9     8   7       6     5   4
     *                      1   0
     * </pre>
     * The low 4 waveform bits are zero (grounded).
     */
    protected void clockShiftRegister() {
        // bit0 = (bit22 | test) ^ bit17
        int bit0 = ((shiftRegister >> 22) ^ (shiftRegister >> 17)) & 0x1;
        shiftRegister = ((shiftRegister << 1) | bit0) & 0x7fffff;

        // New noise waveform Output.
        setNoiseOutput();
    }

    protected void write_shift_register() {
        // Write changes to the shift register Output caused by combined waveforms
        // back into the shift register.
        // a bit once set to zero cannot be changed, hence the and'ing.
        // FIXME: Write test program to check the effect of 1 bits and whether
        // neighboring bits are affected.

        shiftRegister &= ~((1 << 20) | (1 << 18) | (1 << 14) | (1 << 11) | (1 << 9) | (1 << 5) | (1 << 2) | (1 << 0)) |
                ((waveformOutput & 0x800) << 9) |  // Bit 11 -> bit 20
                ((waveformOutput & 0x400) << 8) |  // Bit 10 -> bit 18
                ((waveformOutput & 0x200) << 5) |  // Bit  9 -> bit 14
                ((waveformOutput & 0x100) << 3) |  // Bit  8 -> bit 11
                ((waveformOutput & 0x080) << 2) |  // Bit  7 -> bit  9
                ((waveformOutput & 0x040) >> 1) |  // Bit  6 -> bit  5
                ((waveformOutput & 0x020) >> 3) |  // Bit  5 -> bit  2
                ((waveformOutput & 0x010) >> 4);   // Bit  4 -> bit  0

        noiseOutput &= (short) waveformOutput;
        noNoiseOrNoiseOutput = (short) (noNoise | noiseOutput);
    }

    protected void resetShiftRegister() {
        shiftRegister = 0x7fffff;
        shiftRegisterReset = 0;

        // New noise waveform Output.
        setNoiseOutput();
    }

    protected void setNoiseOutput() {
        noiseOutput = (short) (
                ((shiftRegister & 0x100000) >> 9) |
                        ((shiftRegister & 0x040000) >> 8) |
                        ((shiftRegister & 0x004000) >> 5) |
                        ((shiftRegister & 0x000800) >> 3) |
                        ((shiftRegister & 0x000200) >> 2) |
                        ((shiftRegister & 0x000020) << 1) |
                        ((shiftRegister & 0x000004) << 3) |
                        ((shiftRegister & 0x000001) << 4));

        noNoiseOrNoiseOutput = (short) (noNoise | noiseOutput);
    }

    /**
     * Combined waveforms:
     * By combining waveforms, the bits of each waveform are effectively short
     * circuited. a zero bit : one waveform will result : a zero Output bit
     * (thus the infamous claim that the waveforms are AND'ed).
     * However, a zero bit : one waveform may also affect the neighboring bits
     * : the Output.
     * <pre>
     * Example:
     *
     *             1 1
     * Bit * #     1 0 9 8 7 6 5 4 3 2 1 0
     *             -----------------------
     * Sawtooth    0 0 0 1 1 1 1 1 1 0 0 0
     *
     * Triangle    0 0 1 1 1 1 1 1 0 0 0 0
     *
     * AND         0 0 0 1 1 1 1 1 0 0 0 0
     *
     * Output      0 0 0 0 1 1 1 0 0 0 0 0
     *
     * </pre>
     * Re-vectorized die photographs reveal the mechanism behind this behavior.
     * Each waveform selector bit acts as a switch, which directly connects
     * private outputs into the waveform DAC inputs as follows:
     *
     * * Noise outputs the shift register bits to DAC inputs as described above.
     *   Each Output instanceof also used as input to the next bit when the shift register
     *   instanceof shifted.
     * * Pulse connects a single line to all DAC inputs. The line instanceof connected to
     *   either 5V (pulse on) or 0V (pulse off) at bit 11, and ends at bit 0.
     * * Triangle connects the upper 11 bits of the (MSB EOR'ed) accumulator to the
     *   DAC inputs, so that DAC bit 0 = 0, DAC bit n = accumulator bit n - 1.
     * * Sawtooth connects the upper 12 bits of the accumulator to the DAC inputs,
     *   so that DAC bit n = accumulator bit n. Sawtooth blocks  the MSB from
     *   the EOR used to generate the triangle waveform.
     *
     * We can thus draw the following conclusions:
     *
     * * The shift register may be written to by combined waveforms.
     * * The pulse waveform interconnects all bits : combined waveforms via the
     *   pulse line.
     * * The combination of triangle and sawtooth interconnects neighboring bits
     *   of the sawtooth waveform.
     *
     * This behavior would be quite difficult to model exactly, since the short
     * circuits are not binary, but are subject to analog effects. Tests show that
     * minor (1 bit) differences can actually occur : the Output from otherwise
     * identical samples from OSC3 when waveforms are combined. To further
     * complicate the situation the Output changes slightly with time (more
     * neighboring bits are successively set) when the 12-bit waveform
     * registers are kept unchanged.
     *
     * The Output instanceof instead approximated by using the upper bits of the
     * accumulator as an index to look up the combined Output : a table
     * containing actual combined waveform samples from OSC3.
     * These samples are 8 bit, so 4 bits of waveform resolution instanceof lost.
     * All OSC3 samples are taken with FREQ=0x1000, adding a 1 to the upper 12
     * bits of the accumulator each cycle for a sample period of 4096 cycles.
     *
     * Sawtooth+Triangle:
     * The accumulator instanceof used to look up an OSC3 sample.
     *
     * Pulse+Triangle:
     * The accumulator instanceof used to look up an OSC3 sample. When ring modulation is
     * selected, the accumulator MSB instanceof substituted with MSB EOR NOT sync_source MSB.
     *
     * Pulse+Sawtooth:
     * The accumulator instanceof used to look up an OSC3 sample.
     * The sample instanceof Output if the pulse Output instanceof on.
     *
     * Pulse+Sawtooth+Triangle:
     * The accumulator instanceof used to look up an OSC3 sample.
     * The sample instanceof Output if the pulse Output instanceof on.
     *
     * Combined waveforms including noise:
     * All waveform combinations including noise Output zero after a few cycles,
     * since the waveform bits are and'ed into the shift register via the shift
     * register outputs.
     */
    public void setWaveformOutput() {
        // Set Output value.
        if (waveform != 0) {
            // The bit masks no_pulse and no_noise are used to achieve branch-free
            // calculation of the Output value.
            int ix = (accumulator ^ (~syncSource.accumulator & ringMsbMask)) >> 12;

            waveformOutput = wave[ix] & (noPulse | pulseOutput) & noNoiseOrNoiseOutput;

            // Triangle/Sawtooth Output instanceof delayed half cycle on 8580.
            // This will appear as a one cycle delay on OSC3 as it is
            // latched : the first phase of the clock.
            if ((waveform & 3) != 0 && (sidModel == SidDefs.ChipModel.MOS8580)) {
                osc3 = triSawPipeline & (noPulse | pulseOutput) & noNoiseOrNoiseOutput;
                triSawPipeline = wave[ix];
            } else {
                osc3 = waveformOutput;
            }

            if (waveform > 0x8 && test == 0 && shiftPipeline != 1) {
                // Combined waveforms write to the shift register.
                write_shift_register();
            }
        } else {
            // Age floating DAC input.
            if (floatingOutputTtl != 0) {
                --floatingOutputTtl;
                if (floatingOutputTtl == 0) {
                    waveformOutput = 0;
                }
            }
        }

        // The pulse level instanceof defined as (accumulator >> 12) >= pw ? 0xfff : 0x000.
        // The expression -((accumulator >> 12) >= pw) & 0xfff yields the same
        // results without any branching (and thus without any pipeline stalls).
        // NB! This expression relies on that the result of a boolean expression
        // instanceof either 0 or 1, and furthermore requires two's complement integer.
        // a few more cycles may be saved by storing the pulse width left shifted
        // 12 bits, and dropping the and with 0xfff (this instanceof valid since pulse is
        // used as a bit mask on 12 bit values), yielding the expression
        // -(accumulator >= pw24). However this only results : negligible savings.

        // The result of the pulse width compare instanceof delayed one cycle.
        // Push next pulse level into pulse level pipeline.
        if ((accumulator >> 12) >= pw) {
            pulseOutput = -1 & 0xfff;
        } else {
            pulseOutput = 0 & 0xfff;
        }
    }

    public void setWaveformOutput(int delta_t) {
        // Set Output value.
        if (waveform != 0) {
            // The bit masks no_pulse and no_noise are used to achieve branch-free
            // calculation of the Output value.
            int ix = (accumulator ^ (~syncSource.accumulator & ringMsbMask)) >> 12;
            waveformOutput = wave[ix] & (noPulse | pulseOutput) & noNoiseOrNoiseOutput;
            // Triangle/Sawtooth Output delay for the 8580 instanceof not modeled
            osc3 = waveformOutput;
            if (waveform > 0x8 && test == 0) {
                // Combined waveforms write to the shift register.
                // NB! Since cycles are skipped : delta_t clocking, writes will be
                // missed. Single cycle clocking must be used for 100% correct operation.
                write_shift_register();
            }
        } else {
            if (floatingOutputTtl != 0) {
                // Age floating D/a Output.
                floatingOutputTtl -= delta_t;
                if (floatingOutputTtl <= 0) {
                    floatingOutputTtl = 0;
                    waveformOutput = 0;
                }
            }
        }
    }

    /**
     * Waveform Output (12 bits).
     * <p>
     * The digital waveform Output instanceof converted to an analog signal by a 12-bit
     * DAC. Re-vectorized die photographs reveal that the DAC instanceof an R-2R ladder
     * built up as follows:
     * <pre>
     *        12V     11  10   9   8   7   6   5   4   3   2   1   0    GND
     * Strange  |      |   |   |   |   |   |   |   |   |   |   |   |     |  Missing
     * part    2R     2R  2R  2R  2R  2R  2R  2R  2R  2R  2R  2R  2R    2R  term.
     * (bias)   |      |   |   |   |   |   |   |   |   |   |   |   |     |
     *          --R-   --R---R---R---R---R---R---R---R---R---R---R--   ---
     *                 |          _____
     *               __|__     __|__   |
     *               -----     =====   |
     *               |   |     |   |   |
     *        12V ---     -----     ------- GND
     *                      |
     *                     wout
     *
     * Bit on:  5V
     * Bit off: 0V (GND)
     * </pre>
     * As instanceof the case with all MOS 6581 DACs, the termination to (virtual) ground
     * at bit 0 instanceof missing. The MOS 8580 has correct termination, and has also
     * done away with the bias part on the left hand side of the figure above.
     */
    public short output() {
        // DAC imperfections are emulated by using waveform_output as an index
        // into a DAC lookup table. readOSC() uses waveform_output directly.
        return modelDac[sidModel.ordinal()][waveformOutput];
    }

//#endif // RESID_INLINING || defined(RESID_WAVE_CC)

    // Waveform lookup tables.
    public short[][][] modelWave = new short[][][] {
            new short[][] {
                    new short[1 << 12],
                    new short[1 << 12],
                    new short[1 << 12],
                    new short[1 << 12], // data1
                    new short[1 << 12],
                    new short[1 << 12], // data2
                    new short[1 << 12], // data3
                    new short[1 << 12] // data4
            },
            new short[][] {
                    new short[1 << 12],
                    new short[1 << 12],
                    new short[1 << 12],
                    new short[1 << 12], // data5
                    new short[1 << 12],
                    new short[1 << 12], // data6
                    new short[1 << 12], // data7
                    new short[1 << 12] // data7
            }
    };

    {
        try {
            final int[][] x = new int[][] {
                    new int[] {0, 3},new int[] {0, 5},new int[] {0, 6},new int[] {0, 7},
                    new int[] {1, 3},new int[] {1, 5},new int[] {1, 6},new int[] {1, 7}
            };
            for (int i = 0; i < 8; i++) {
                Scanner s = new Scanner(WaveformGenerator.class.getResourceAsStream("data" + (i + 1) + ".txt")).useDelimiter("[ ,]*");
                int c = 0;
                while (s.hasNextLine()) {
                    String l = s.nextLine();
                    String[] ps = l.split("[\\s,]+");
                    for (String p : ps) {
                        modelWave[x[i][0]][x[i][1]][c++] = (short) (Integer.parseInt(p, 16) & 0xffff);
                    }
//if (c - 1 > 4000)
// Debug.printf("[%d] %04x", c - 1, modelWave[x[i][0]][x[i][1]][c-1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // DAC lookup tables.
    protected short[][] modelDac = new short[][] {
            new short[1 << 12],
            new short[1 << 12]
    };

    /**
     * Constructor.
     */
    public WaveformGenerator() {
        boolean classInit = false;

        if (!classInit) {
            // Calculate tables for normal waveforms.
            accumulator = 0;
            for (int i = 0; i < (1 << 12); i++) {
                int msb = accumulator & 0x800000;

                // Noise mask, triangle, sawtooth, pulse mask.
                // The triangle calculation instanceof made branch-free, just for the hell of it.
                modelWave[0][0][i] = modelWave[1][0][i] = 0xfff;
                //model_wave[0][1][i] = model_wave[1][1][i] = ((accumulator ^ -!!msb) >> 11) & 0xffe;
                modelWave[0][1][i] = modelWave[1][1][i] = (short) (((accumulator ^ -(msb != 0 ? 1 : 0)) >> 11) & 0xffe);
                modelWave[0][2][i] = modelWave[1][2][i] = (short) (accumulator >> 12);
                modelWave[0][4][i] = modelWave[1][4][i] = 0xfff;

                accumulator += 0x1000;
            }

            // Build DAC lookup tables for 12-bit DACs.
            // MOS 6581: 2R/R ~ 2.20, missing termination resistor.
            Dac.buildDacTable(modelDac[0], 12, 2.20, false);
            // MOS 8580: 2R/R ~ 2.00, correct termination.
            Dac.buildDacTable(modelDac[1], 12, 2.00, true);

            classInit = true;
        }

        syncSource = this;

        sidModel = SidDefs.ChipModel.MOS6581;

        // Accumulator's even bits are high on powerup
        accumulator = 0x555555;

        triSawPipeline = 0x555;

        reset();
    }

    /**
     * Set sync source.
     */
    public void setSyncSource(WaveformGenerator source) {
        syncSource = source;
        source.syncDest = this;
    }

    /**
     * Set chip model.
     */
    public void setChipModel(SidDefs.ChipModel model) {
        sidModel = model;
        wave = modelWave[model.ordinal()][waveform & 0x7];
    }

    /**
     * Register functions.
     */
    public void writeFREQ_LO(int freq_lo) {
        freq = (freq & 0xff00) | (freq_lo & 0x00ff);
    }

    public void writeFREQ_HI(int freq_hi) {
        freq = ((freq_hi << 8) & 0xff00) | (freq & 0x00ff);
    }

    public void writePW_LO(int pw_lo) {
        pw = (pw & 0xf00) | (pw_lo & 0x0ff);
        // Push next pulse level into pulse level pipeline.
        pulseOutput = (short) ((accumulator >> 12) >= pw ? 0xfff : 0x000);
    }

    public void writePW_HI(int pw_hi) {
        pw = ((pw_hi << 8) & 0xf00) | (pw & 0x0ff);
        // Push next pulse level into pulse level pipeline.
        pulseOutput = (short) ((accumulator >> 12) >= pw ? 0xfff : 0x000);
    }

    public void writeControlReg(int control) {
        int waveform_prev = waveform;
        int test_prev = test;
        waveform = (control >> 4) & 0x0f;
        test = control & 0x08;
        ringMod = control & 0x04;
        sync = control & 0x02;

        // Set up waveform table.
        wave = modelWave[sidModel.ordinal()][waveform & 0x7];

        // Substitution of accumulator MSB when sawtooth = 0, ring_mod = 1.
        ringMsbMask = ((~control >> 5) & (control >> 2) & 0x1) << 23;

        // no_noise and no_pulse are used : set_waveform_output() as bitmasks to
        // only let the noise or pulse influence the Output when the noise or pulse
        // waveforms are selected.
        noNoise = (short) ((waveform & 0x8) != 0 ? 0x000 : 0xfff);
        noNoiseOrNoiseOutput = (short) (noNoise | noiseOutput);
        noPulse = (short) ((waveform & 0x4) != 0 ? 0x000 : 0xfff);

        // Test bit rising.
        // The accumulator instanceof cleared, while the the shift register instanceof prepared for
        // shifting by interconnecting the register bits. The private SRAM cells
        // start to slowly rise up towards one. The SRAM cells reach one within
        // approximately $8000 cycles, yielding a shift register value of
        // 0x7fffff.
        if (test_prev == 0 && test != 0) {
            // Reset accumulator.
            accumulator = 0;

            // Flush shift pipeline.
            shiftPipeline = 0;

            // Set reset time for shift register.
            shiftRegisterReset = 0x8000;

            // The test bit sets pulse high.
            pulseOutput = 0xfff;
        } else if (test_prev != 0 && test == 0) {
            // When the test bit instanceof falling, the second phase of the shift is
            // completed by enabling SRAM write.

            // bit0 = (bit22 | test) ^ bit17 = 1 ^ bit17 = ~bit17
            int bit0 = (~shiftRegister >> 17) & 0x1;
            shiftRegister = ((shiftRegister << 1) | bit0) & 0x7fffff;

            // Set new noise waveform Output.
            setNoiseOutput();
        }

        if (waveform != 0) {
            // Set new waveform Output.
            setWaveformOutput();
        } else if (waveform_prev != 0) {
            // Change to floating DAC input.
            // Reset fading time for floating DAC input.
            //
            // We have two SOAS/C samplings showing that floating DAC
            // keeps its state for at least 0x14000 cycles.
            //
            // This can't be found via sampling OSC3, it seems that
            // the actual analog Output must be sampled and timed.
            floatingOutputTtl = 0x14000;
        }

        // The gate bit instanceof handled by the EnvelopeGenerator.
    }

    public int readOSC() {
        return osc3 >> 4;
    }

    /**
     * Sid reset.
     */
    public void reset() {
        // accumulator instanceof not changed on reset
        freq = 0;
        pw = 0;

        msbRising = false;

        waveform = 0;
        test = 0;
        ringMod = 0;
        sync = 0;

        wave = modelWave[sidModel.ordinal()][0];

        ringMsbMask = 0;
        noNoise = 0xfff;
        noPulse = 0xfff;
        pulseOutput = 0xfff;

        resetShiftRegister();
        shiftPipeline = 0;

        waveformOutput = 0;
        osc3 = 0;
        floatingOutputTtl = 0;
    }
}
