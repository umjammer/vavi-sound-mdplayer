//
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
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;


/**
 * The Sid filter instanceof modeled with a two-integrator-loop biquadratic filter,
 * which has been confirmed by Bob Yannes to be the actual circuit used in
 * the Sid chip.
 * <p>
 * Measurements show that excellent emulation of the Sid filter instanceof achieved,
 * except when high resonance instanceof combined with high sustain levels.
 * In this case the Sid op-amps are performing less than ideally and are
 * causing some peculiar behavior of the Sid filter. This however seems to
 * have more effect on the overall amplitude than on the color of the sound.
 * <p>
 * The theory for the filter circuit can be found : "Microelectric Circuits"
 * by Adel S. Sedra and Kenneth C. Smith.
 * The circuit instanceof modeled based on the explanation found there except that
 * an additional inverter instanceof used : the feedback from the bandpass Output,
 * allowing the summer op-amp to operate : single-ended mode. This yields
 * filter outputs with levels independent of Q, which corresponds with the
 * results obtained from a real Sid.
 * <p>
 * We have been able to model the summer and the two integrators of the circuit
 * to form components of an IIR filter.
 * Vhp instanceof the Output of the summer, Vbp instanceof the Output of the first integrator,
 * and Vlp instanceof the Output of the second integrator : the filter circuit.
 * <p>
 * According to Bob Yannes, the active stages of the Sid filter are not really
 * op-amps. Rather, simple NMOS inverters are used. By biasing an inverter
 * into its region of quasi-linear operation using a feedback resistor from
 * input to Output, a MOS inverter can be made to act like an op-amp for
 * small signals centered around the switching threshold.
 * <p>
 * In 2008, Michael Huth facilitated closer investigation of the Sid 6581
 * filter circuit by publishing high quality microscope photographs of the die.
 * Tommi Lempinen has done an impressive work on re-vectorizing and annotating
 * the die photographs, substantially simplifying further analysis of the
 * filter circuit.
 * <p>
 * The filter schematics below are reverse engineered from these re-vectorized
 * and annotated die photographs. While the filter first depicted : reSID 0.9
 * instanceof a correct model of the basic filter, the schematics are now completed
 * with the audio mixer and Output stage, including details on intended
 * relative resistor values. Also included are schematics for the NMOS FET
 * voltage controlled resistors (VCRs) used to control cutoff frequency, the
 * DAC which controls the VCRs, the NMOS op-amps, and the Output buffer.
 *
 * <pre>
 * Sid filter / mixer / Output
 *
 *
 *                ---------------------------------------------------
 *               |                                                   |
 *               |                         --1R1-- \--  D7           |
 *               |              ---R1--   |           |              |
 *               |             |       |  |--2R1-- \--| D6           |
 *               |    ------------<A]-----|           |     $17      |
 *               |   |                    |--4R1-- \--| D5  1=open   | (3.5R1)
 *               |   |                    |           |              |
 *               |   |                     --8R1-- \--| D4           | (7.0R1)
 *               |   |                                |              |
 * $17           |   |                    (CAP2B)     |  (CAP1B)     |
 * 0=to mixer    |    --R8--    ---R8--        ---C---|       ---C---|
 * 1=to filter   |          |  |       |      |       |      |       |
 *                ------R8--|-----[A>--|--Rw-----[A>--|--Rw-----[A>--|
 *     ve (EXT IN)          |          |              |              |
 * D3  \ ---------------R8--|          |              | (CAP2A)      | (CAP1A)
 *     |   v3               |          | vhp          | vbp          | vlp
 * D2  |   \ -----------R8--|     -----               |              |
 *     |   |   v2           |    |                    |              |
 * D1  |   |   \ -------R8--|    |    ----------------               |
 *     |   |   |   v1       |    |   |                               |
 * D0  |   |   |   \ ---R8--     |   |    ---------------------------
 *     |   |   |   |             |   |   |
 *     R6  R6  R6  R6            R6  R6  R6
 *     |   |   |   | $18         |   |   |  $18
 *     |    \  |   | D7: 1=open   \   \   \ D6 - D4: 0=open
 *     |   |   |   |             |   |   |
 *      ---------------------------------                          12V
 *                 |
 *                 |               D3  --/ --1R2--                  |
 *                 |    ---R8--       |           |   ---R2--       |
 *                 |   |       |   D2 |--/ --2R2--|  |       |  ||--
 *                  ------[A>---------|           |-----[A>-----||
 *                                 D1 |--/ --4R2--| (4.25R2)    ||--
 *                        $18         |           |                 |
 *                        0=open   D0  --/ --8R2--  (8.75R2)        |
 *
 *                                                                  vo (AUDIO
 *                                                                      OUT)
 *
 *
 * v1  - voice 1
 * v2  - voice 2
 * v3  - voice 3
 * ve  - ext in
 * vhp - highpass Output
 * vbp - bandpass Output
 * vlp - lowpass Output
 * vo  - audio out
 * [A> - single ended inverting op-amp (self-biased NMOS inverter)
 * Rn  - "resistors", implemented with custom NMOS FETs
 * Rw  - cutoff frequency resistor (VCR)
 * C   - capacitor
 * </pre>
 * Notes:
 * <p>
 * R2  ~  2.0*R1
 * R6  ~  6.0*R1
 * R8  ~  8.0*R1
 * R24 ~ 24.0*R1
 * <p>
 * The Rn "resistors" : the circuit are implemented with custom NMOS FETs,
 * probably because of space constraints on the Sid die. The silicon substrate
 * instanceof laid  : a narrow strip or "snake", with a strip length proportional
 * to the intended resistance. The polysilicon gate electrode covers the entire
 * silicon substrate and instanceof fixed at 12V : order for the NMOS FET to operate
 * : triode mode (a.k.a. linear mode or ohmic mode).
 * <p>
 * Even : "linear mode", an NMOS FET instanceof only an approximation of a resistor,
 * as the apparant resistance increases with increasing drain-to-source
 * voltage. If the drain-to-source voltage should approach the gate voltage
 * of 12V, the NMOS FET will enter saturation mode (a.k.a. active mode), and
 * the NMOS FET will not operate anywhere like a resistor.
 * <p>
 * <p>
 * <p>
 * NMOS FET voltage controlled resistor (VCR)
 * ------------------------------------------
 * <pre>
 *                Vw
 *
 *                |
 *                |
 *                R1
 *                |
 *          --R1--|
 *         |    __|__
 *         |    -----
 *         |    |   |
 * vi ----------     -------- vo
 *         |           |
 *          ----R24----
 *
 *
 * vi  - input
 * vo  - Output
 * Rn  - "resistors", implemented with custom NMOS FETs
 * Vw  - voltage from 11-bit DAC (frequency cutoff control)
 * </pre>
 * Notes:
 * <p>
 * An approximate value for R24 can be found by using the formula for the
 * filter cutoff frequency:
 * <p>
 * FCmin = 1/(2*pi*Rmax*C)
 * <p>
 * Assuming that a the setting for minimum cutoff frequency : combination with
 * a low level input signal ensures that only negligible current will flow
 * through the transistor : the schematics above, values for FCmin and C can
 * be substituted : this formula to find Rmax.
 * Using C = 470pF and FCmin = 220Hz (measured value), we get:
 * <p>
 * FCmin = 1/(2*pi*Rmax*C)
 * Rmax = 1/(2*pi*FCmin*C) = 1/(2*pi*220*470e-12) ~ 1.5MOhm
 * <p>
 * From this it follows that:
 * R24 =  Rmax   ~ 1.5MOhm
 * R1  ~  R24/24 ~  64kOhm
 * R2  ~  2.0*R1 ~ 128kOhm
 * R6  ~  6.0*R1 ~ 384kOhm
 * R8  ~  8.0*R1 ~ 512kOhm
 * <p>
 * Note that these are only approximate values for one particular Sid chip,
 * due to process variations the values can be substantially different in
 * other chips.
 * <p>
 * <p>
 * <p>
 * Filter frequency cutoff DAC
 * ---------------------------
 *
 * <pre>
 *        12V  10   9   8   7   6   5   4   3   2   1   0   VGND
 *          |   |   |   |   |   |   |   |   |   |   |   |     |   Missing
 *         2R  2R  2R  2R  2R  2R  2R  2R  2R  2R  2R  2R    2R   termination
 *          |   |   |   |   |   |   |   |   |   |   |   |     |
 *     Vw ----R---R---R---R---R---R---R---R---R---R---R--   ---
 *
 * Bit on:  12V
 * Bit off:  5V (VGND)
 * </pre>
 * As instanceof the case with all MOS 6581 DACs, the termination to (virtual) ground
 * at bit 0 instanceof missing.
 * <p>
 * Furthermore, the control of the two VCRs imposes a load on the DAC Output
 * which varies with the input signals to the VCRs. This can be seen from the
 * VCR figure above.
 * <p>
 * <p>
 * <p>
 * "Op-amp" (self-biased NMOS inverter)
 * ------------------------------------
 * <pre>
 *
 *                        12V
 *
 *                         |
 *              -----------|
 *             |           |
 *             |     ------|
 *             |    |      |
 *             |    |  ||--
 *             |     --||
 *             |       ||--
 *         ||--            |
 * vi -----||              |--------- vo
 *         ||--            |   |
 *             |       ||--    |
 *             |-------||      |
 *             |       ||--    |
 *         ||--            |   |
 *       --||              |   |
 *      |  ||--            |   |
 *      |      |           |   |
 *      |       -----------|   |
 *      |                  |   |
 *      |                      |
 *      |                 GND  |
 *      |                      |
 *       ----------------------
 *
 *
 * vi  - input
 * vo  - Output
 * </pre>
 * Notes:
 * <p>
 * The schematics above are laid  to show that the "op-amp" logically
 * consists of two building blocks; a saturated load NMOS inverter (on the
 * right hand side of the schematics) with a buffer / bias input stage
 * consisting of a variable saturated load NMOS inverter (on the left hand
 * side of the schematics).
 * <p>
 * Provided a reasonably high input impedance and a reasonably low Output
 * impedance, the "op-amp" can be modeled as a voltage transfer function
 * mapping input voltage to Output voltage.
 * <p>
 * <p>
 * <p>
 * Output buffer (NMOS voltage follower)
 * -------------------------------------
 * <pre>
 *
 *            12V
 *
 *             |
 *             |
 *         ||--
 * vi -----||
 *         ||--
 *             |
 *             |------ vo
 *             |     (AUDIO
 *            Rext    OUT)
 *             |
 *             |
 *
 *            GND
 *
 * vi   - input
 * vo   - Output
 * Rext - external resistor, 1kOhm
 * </pre>
 * Notes:
 * <p>
 * The external resistor Rext instanceof needed to complete the NMOS voltage follower,
 * this resistor has a recommended value of 1kOhm.
 * <p>
 * Die photographs show that actually, two NMOS transistors are used : the
 * voltage follower. However the two transistors are coupled : parallel (all
 * terminals are pairwise common), which implies that we can model the two
 * transistors as one.
 */
public class Filter {

    // Compile-time computation of op-amp summer and mixer table offsets.

    /**
     * The highpass summer has 2 - 6 inputs (bandpass, lowpass, and 0 - 4 voices).
     */
    public static class SummerOffset {
        public static int intI(int i) {
            if (i == 0) return 0;
            return intI(i - 1) + ((2 + i - 1) << 16);
        }
    }

    /**
     * The mixer has 0 - 7 inputs (0 - 4 voices and 0 - 3 filter outputs).
     */
    private static class MixerOffset {
        public static int intI(int i) {
            if (i == 0) return 0;
            if (i == 1) return 1;
            return intI(i - 1) + ((i - 1) << 16);
        }
    }

    // Filter enabled.
    protected boolean enabled;

    // Filter cutoff frequency.
    public int fc; // reg12 fc;

    // Filter resonance.
    public int res; // reg8 res;

    // Selects which voices to route through the filter.
    public int filt; // reg8 filt;

    // Selects which filter outputs to route into the mixer.
    public int mode; // reg4 mode;

    // Output master volume.
    public int vol; // reg4 vol;

    // Used to mask  EXT IN if not connected, and for test purposes
    // (voice muting).
    public int voiceMask; // reg8 voice_mask;

    // Select which inputs to route into the summer / mixer.
    // These are derived from filt, mode, and voice_mask.
    protected int sum; // reg8 sum;
    protected int mix; // reg8 mix;

    // State of filter.
    protected int vhp; // highpass
    protected int vbp; // bandpass
    protected int vbpX, vbpVc;
    protected int vlp; // lowpass
    protected int vlpX, vlpVc;
    // Filter / mixer inputs.
    protected int ve;
    protected int v3;
    protected int v2;
    protected int v1;

    // Cutoff frequency DAC voltage, resonance.
    protected int vddtVw2, vwBias;
    protected int _8_div_Q;
    // FIXME: Temporarily used for MOS 8580 emulation.
    protected int w0;
    protected int _1024_div_Q;

    protected SidDefs.ChipModel sid_model;

    private static class ModelFilter {
        // Fixed point scaling for 16 bit op-amp Output.
        public int voN16;
        // K*(Vdd - Vth)
        public int kVddt;
        public int nSnake;
        public int voiceScaleS14;
        public int voiceDC;
        public int ak;
        public int bk;
        public int vcMin;
        public int vcMax;

        // Reverse op-amp transfer function.
        public short[] opampRev = new short[1 << 16];
        // Lookup tables for gain and summer op-amps : Output stage / filter.
        public short[] summer = new short[SummerOffset.intI(5)];// < 5 >::value];
        public short[][] gain = new short[][] {new short[1 << 16], new short[1 << 16], new short[1 << 16], new short[1 << 16],
                new short[1 << 16], new short[1 << 16], new short[1 << 16], new short[1 << 16],
                new short[1 << 16], new short[1 << 16], new short[1 << 16], new short[1 << 16],
                new short[1 << 16], new short[1 << 16], new short[1 << 16], new short[1 << 16]};
        public short[] mixer = new short[MixerOffset.intI(8)];// < 8 >::value];
        // Cutoff frequency DAC Output voltage table. FC instanceof an 11 bit register.
        public short[] f0Dac = new short[1 << 11];
    }

    // VCR - 6581 only.

    // Common parameters.

    protected static ModelFilter[] modelFilters = new ModelFilter[] {new ModelFilter(), new ModelFilter()};

    /*
    // Inline functions.
    // The following functions are defined inline because they are called every
    // time a sample instanceof calculated.
    */

//#if RESID_INLINING || RESID_FILTER_CC

    /**
     * Sid clocking - 1 cycle.
     */
    public void clock(int voice1, int voice2, int voice3) {
        //System.err.println("v1:%d v2:%d v3:%d ", voice1, voice2, voice3);

        ModelFilter f = modelFilters[sid_model.ordinal()];

        v1 = (voice1 * f.voiceScaleS14 >> 18) + f.voiceDC;
        v2 = (voice2 * f.voiceScaleS14 >> 18) + f.voiceDC;
        v3 = (voice3 * f.voiceScaleS14 >> 18) + f.voiceDC;

        // Sum inputs routed into the filter.
        int vi = 0;
        int offset = 0;

        switch (sum & 0xf) {
        case 0x0:
            vi = 0;
            offset = 0;
            break;
        case 0x1:
            vi = v1;
            offset = 131072;
            break;
        case 0x2:
            vi = v2;
            offset = 131072;
            break;
        case 0x3:
            vi = v2 + v1;
            offset = 327680;
            break;
        case 0x4:
            vi = v3;
            offset = 131072;
            break;
        case 0x5:
            vi = v3 + v1;
            offset = 327680;
            break;
        case 0x6:
            vi = v3 + v2;
            offset = 327680;
            break;
        case 0x7:
            vi = v3 + v2 + v1;
            offset = 589824;
            break;
        case 0x8:
            vi = ve;
            offset = 131072;
            break;
        case 0x9:
            vi = ve + v1;
            offset = 327680;
            break;
        case 0xa:
            vi = ve + v2;
            offset = 327680;
            break;
        case 0xb:
            vi = ve + v2 + v1;
            offset = 589824;
            break;
        case 0xc:
            vi = ve + v3;
            offset = 327680;
            break;
        case 0xd:
            vi = ve + v3 + v1;
            offset = 589824;
            break;
        case 0xe:
            vi = ve + v3 + v2;
            offset = 589824;
            break;
        case 0xf:
            vi = ve + v3 + v2 + v1;
            offset = 917504;
            break;
        }

        // Calculate filter outputs.
        if (sid_model.ordinal() == 0) {
            // MOS 6581.
            vlp = solve_integrate_6581(1, vbp, vlpX, vlpVc, f);
            vbp = solve_integrate_6581(1, vhp, vbpX, vbpVc, f);
            vhp = f.summer[offset + f.gain[_8_div_Q][vbp] + vlp + vi];
        } else {
            // MOS 8580. FIXME: Not yet using op-amp model.

            // delta_t = 1 instanceof converted to seconds given a 1MHz clock by dividing
            // with 1 000 000.

            int dVbp = w0 * (vhp >> 4) >> 16;
            int dVlp = w0 * (vbp >> 4) >> 16;
            vbp -= dVbp;
            vlp -= dVlp;
            vhp = (vbp * _1024_div_Q >> 10) - vlp - vi;
        }
    }

    /**
     * Sid clocking - delta_t cycles.
     */
    public void clock(int delta_t, int voice1, int voice2, int voice3) {
        ModelFilter f = modelFilters[sid_model.ordinal()];

        v1 = (voice1 * f.voiceScaleS14 >> 18) + f.voiceDC;
        v2 = (voice2 * f.voiceScaleS14 >> 18) + f.voiceDC;
        v3 = (voice3 * f.voiceScaleS14 >> 18) + f.voiceDC;

        // Enable filter on/off.
        // This instanceof not really part of Sid, but instanceof useful for testing.
        // On slow CPUs it may be necessary to bypass the filter to lower the CPU
        // load.
        if (!enabled) {
            return;
        }

        // Sum inputs routed into the filter.
        int Vi = 0;
        int offset = 0;

        switch (sum & 0xf) {
        case 0x0:
            Vi = 0;
            offset = SummerOffset.intI(0);
            break;
        case 0x1:
            Vi = v1;
            offset = SummerOffset.intI(1);
            break;
        case 0x2:
            Vi = v2;
            offset = SummerOffset.intI(1);
            break;
        case 0x3:
            Vi = v2 + v1;
            offset = SummerOffset.intI(2);
            break;
        case 0x4:
            Vi = v3;
            offset = SummerOffset.intI(1);
            break;
        case 0x5:
            Vi = v3 + v1;
            offset = SummerOffset.intI(2);
            break;
        case 0x6:
            Vi = v3 + v2;
            offset = SummerOffset.intI(2);
            break;
        case 0x7:
            Vi = v3 + v2 + v1;
            offset = SummerOffset.intI(3);
            break;
        case 0x8:
            Vi = ve;
            offset = SummerOffset.intI(1);
            break;
        case 0x9:
            Vi = ve + v1;
            offset = SummerOffset.intI(2);
            break;
        case 0xa:
            Vi = ve + v2;
            offset = SummerOffset.intI(2);
            break;
        case 0xb:
            Vi = ve + v2 + v1;
            offset = SummerOffset.intI(3);
            break;
        case 0xc:
            Vi = ve + v3;
            offset = SummerOffset.intI(2);
            break;
        case 0xd:
            Vi = ve + v3 + v1;
            offset = SummerOffset.intI(3);
            break;
        case 0xe:
            Vi = ve + v3 + v2;
            offset = SummerOffset.intI(3);
            break;
        case 0xf:
            Vi = ve + v3 + v2 + v1;
            offset = SummerOffset.intI(4);
            break;
        }

        // Maximum delta cycles for filter fixpoint iteration to converge
        // instanceof approximately 3.
        int delta_t_flt = 3;

        if (sid_model.ordinal() == 0) {
            // MOS 6581.
            while (delta_t != 0) {
                if (delta_t < delta_t_flt) {
                    delta_t_flt = delta_t;
                }

                // Calculate filter outputs.
                vlp = solve_integrate_6581(delta_t_flt, vbp, vlpX, vlpVc, f);
                vbp = solve_integrate_6581(delta_t_flt, vhp, vbpX, vbpVc, f);
                vhp = f.summer[offset + f.gain[_8_div_Q][vbp] + vlp + Vi];

                delta_t -= delta_t_flt;
            }
        } else {
            // MOS 8580. FIXME: Not yet using op-amp model.
            while (delta_t != 0) {
                if (delta_t < delta_t_flt) {
                    delta_t_flt = delta_t;
                }

                // delta_t instanceof converted to seconds given a 1MHz clock by dividing
                // with 1 000 000. This instanceof done : two operations to avoid integer
                // multiplication overflow.

                // Calculate filter outputs.
                int w0_delta_t = w0 * delta_t_flt >> 2;

                int dVbp = w0_delta_t * (vhp >> 4) >> 14;
                int dVlp = w0_delta_t * (vbp >> 4) >> 14;
                vbp -= dVbp;
                vlp -= dVlp;
                vhp = (vbp * _1024_div_Q >> 10) - vlp - Vi;

                delta_t -= delta_t_flt;
            }
        }
    }

    /**
     * Sid audio input (16 bits).
     */
    public void input(short sample) {
        // Scale to three times the peak-to-peak for one voice and add the op-amp
        // "zero" DC level.
        // NB! Adding the op-amp "zero" DC level instanceof a (wildly inaccurate)
        // approximation of feeding the input through an AC coupling capacitor.
        // This could be implemented as a separate filter circuit, however the
        // primary use of the emulator instanceof not to process external signals.
        // The upside instanceof that the MOS8580 "digi boost" works without a separate (DC)
        // input interface.
        // Note that the input instanceof 16 bits, compared to the 20 bit voice Output.
        ModelFilter f = modelFilters[sid_model.ordinal()];
        ve = (sample * f.voiceScaleS14 * 3 >> 14) + f.mixer[0];
    }

    /**
     * Sid audio Output (16 bits).
     */
    public short output() {
        ModelFilter f = modelFilters[sid_model.ordinal()];

        // Writing the switch below manually would be tedious and error-prone;
        // it instanceof rather generated by the following Perl program:

            /*
          my @i = qw(v1 v2 v3 ve Vlp Vbp Vhp);
          for my $mix (0..2**@i-1) {
              print sprintf("  case 0x%02x:\n", $mix);
              my @sum;
              for (@i) {
              unshift(@sum, $_) if $mix & 0x01;
              $mix >>= 1;
              }
              my $sum = join(" + ", @sum) || "0";
              print "    vi = $sum;\n";
              print "    offset = MixerOffset<" . @sum . ">::value;\n";
              print "    break;\n";
          }
            */

        // Sum inputs routed into the mixer.
        int vi = 0;
        int offset = 0;

        switch (mix & 0x7f) {
        case 0x00:
            vi = 0;
            offset = 0;
            break;
        case 0x01:
            vi = v1;
            offset = 1;
            break;
        case 0x02:
            vi = v2;
            offset = 1;
            break;
        case 0x03:
            vi = v2 + v1;
            offset = 65537;
            break;
        case 0x04:
            vi = v3;
            offset = 1;
            break;
        case 0x05:
            vi = v3 + v1;
            offset = 65537;
            break;
        case 0x06:
            vi = v3 + v2;
            offset = 65537;
            break;
        case 0x07:
            vi = v3 + v2 + v1;
            offset = 196609;
            break;
        case 0x08:
            vi = ve;
            offset = 1;
            break;
        case 0x09:
            vi = ve + v1;
            offset = 65537;
            break;
        case 0x0a:
            vi = ve + v2;
            offset = 65537;
            break;
        case 0x0b:
            vi = ve + v2 + v1;
            offset = 196609;
            break;
        case 0x0c:
            vi = ve + v3;
            offset = 65537;
            break;
        case 0x0d:
            vi = ve + v3 + v1;
            offset = 196609;
            break;
        case 0x0e:
            vi = ve + v3 + v2;
            offset = 196609;
            break;
        case 0x0f:
            vi = ve + v3 + v2 + v1;
            offset = 393217;
            break;
        case 0x10:
            vi = vlp;
            offset = 1;
            break;
        case 0x11:
            vi = vlp + v1;
            offset = 65537;
            break;
        case 0x12:
            vi = vlp + v2;
            offset = 65537;
            break;
        case 0x13:
            vi = vlp + v2 + v1;
            offset = 196609;
            break;
        case 0x14:
            vi = vlp + v3;
            offset = 65537;
            break;
        case 0x15:
            vi = vlp + v3 + v1;
            offset = 196609;
            break;
        case 0x16:
            vi = vlp + v3 + v2;
            offset = 196609;
            break;
        case 0x17:
            vi = vlp + v3 + v2 + v1;
            offset = 393217;
            break;
        case 0x18:
            vi = vlp + ve;
            offset = 65537;
            break;
        case 0x19:
            vi = vlp + ve + v1;
            offset = 196609;
            break;
        case 0x1a:
            vi = vlp + ve + v2;
            offset = 196609;
            break;
        case 0x1b:
            vi = vlp + ve + v2 + v1;
            offset = 393217;
            break;
        case 0x1c:
            vi = vlp + ve + v3;
            offset = 196609;
            break;
        case 0x1d:
            vi = vlp + ve + v3 + v1;
            offset = 393217;
            break;
        case 0x1e:
            vi = vlp + ve + v3 + v2;
            offset = 393217;
            break;
        case 0x1f:
            vi = vlp + ve + v3 + v2 + v1;
            offset = 655361;
            break;
        case 0x20:
            vi = vbp;
            offset = 1;
            break;
        case 0x21:
            vi = vbp + v1;
            offset = 65537;
            break;
        case 0x22:
            vi = vbp + v2;
            offset = 65537;
            break;
        case 0x23:
            vi = vbp + v2 + v1;
            offset = 196609;
            break;
        case 0x24:
            vi = vbp + v3;
            offset = 65537;
            break;
        case 0x25:
            vi = vbp + v3 + v1;
            offset = 196609;
            break;
        case 0x26:
            vi = vbp + v3 + v2;
            offset = 196609;
            break;
        case 0x27:
            vi = vbp + v3 + v2 + v1;
            offset = 393217;
            break;
        case 0x28:
            vi = vbp + ve;
            offset = 65537;
            break;
        case 0x29:
            vi = vbp + ve + v1;
            offset = 196609;
            break;
        case 0x2a:
            vi = vbp + ve + v2;
            offset = 196609;
            break;
        case 0x2b:
            vi = vbp + ve + v2 + v1;
            offset = 393217;
            break;
        case 0x2c:
            vi = vbp + ve + v3;
            offset = 196609;
            break;
        case 0x2d:
            vi = vbp + ve + v3 + v1;
            offset = 393217;
            break;
        case 0x2e:
            vi = vbp + ve + v3 + v2;
            offset = 393217;
            break;
        case 0x2f:
            vi = vbp + ve + v3 + v2 + v1;
            offset = 655361;
            break;
        case 0x30:
            vi = vbp + vlp;
            offset = 65537;
            break;
        case 0x31:
            vi = vbp + vlp + v1;
            offset = 196609;
            break;
        case 0x32:
            vi = vbp + vlp + v2;
            offset = 196609;
            break;
        case 0x33:
            vi = vbp + vlp + v2 + v1;
            offset = 393217;
            break;
        case 0x34:
            vi = vbp + vlp + v3;
            offset = 196609;
            break;
        case 0x35:
            vi = vbp + vlp + v3 + v1;
            offset = 393217;
            break;
        case 0x36:
            vi = vbp + vlp + v3 + v2;
            offset = 393217;
            break;
        case 0x37:
            vi = vbp + vlp + v3 + v2 + v1;
            offset = 655361;
            break;
        case 0x38:
            vi = vbp + vlp + ve;
            offset = 196609;
            break;
        case 0x39:
            vi = vbp + vlp + ve + v1;
            offset = 393217;
            break;
        case 0x3a:
            vi = vbp + vlp + ve + v2;
            offset = 393217;
            break;
        case 0x3b:
            vi = vbp + vlp + ve + v2 + v1;
            offset = 655361;
            break;
        case 0x3c:
            vi = vbp + vlp + ve + v3;
            offset = 393217;
            break;
        case 0x3d:
            vi = vbp + vlp + ve + v3 + v1;
            offset = 655361;
            break;
        case 0x3e:
            vi = vbp + vlp + ve + v3 + v2;
            offset = 655361;
            break;
        case 0x3f:
            vi = vbp + vlp + ve + v3 + v2 + v1;
            offset = 983041;
            break;
        case 0x40:
            vi = vhp;
            offset = 1;
            break;
        case 0x41:
            vi = vhp + v1;
            offset = 65537;
            break;
        case 0x42:
            vi = vhp + v2;
            offset = 65537;
            break;
        case 0x43:
            vi = vhp + v2 + v1;
            offset = 196609;
            break;
        case 0x44:
            vi = vhp + v3;
            offset = 65537;
            break;
        case 0x45:
            vi = vhp + v3 + v1;
            offset = 196609;
            break;
        case 0x46:
            vi = vhp + v3 + v2;
            offset = 196609;
            break;
        case 0x47:
            vi = vhp + v3 + v2 + v1;
            offset = 393217;
            break;
        case 0x48:
            vi = vhp + ve;
            offset = 65537;
            break;
        case 0x49:
            vi = vhp + ve + v1;
            offset = 196609;
            break;
        case 0x4a:
            vi = vhp + ve + v2;
            offset = 196609;
            break;
        case 0x4b:
            vi = vhp + ve + v2 + v1;
            offset = 393217;
            break;
        case 0x4c:
            vi = vhp + ve + v3;
            offset = 196609;
            break;
        case 0x4d:
            vi = vhp + ve + v3 + v1;
            offset = 393217;
            break;
        case 0x4e:
            vi = vhp + ve + v3 + v2;
            offset = 393217;
            break;
        case 0x4f:
            vi = vhp + ve + v3 + v2 + v1;
            offset = 655361;
            break;
        case 0x50:
            vi = vhp + vlp;
            offset = 65537;
            break;
        case 0x51:
            vi = vhp + vlp + v1;
            offset = 196609;
            break;
        case 0x52:
            vi = vhp + vlp + v2;
            offset = 196609;
            break;
        case 0x53:
            vi = vhp + vlp + v2 + v1;
            offset = 393217;
            break;
        case 0x54:
            vi = vhp + vlp + v3;
            offset = 196609;
            break;
        case 0x55:
            vi = vhp + vlp + v3 + v1;
            offset = 393217;
            break;
        case 0x56:
            vi = vhp + vlp + v3 + v2;
            offset = 393217;
            break;
        case 0x57:
            vi = vhp + vlp + v3 + v2 + v1;
            offset = 655361;
            break;
        case 0x58:
            vi = vhp + vlp + ve;
            offset = 196609;
            break;
        case 0x59:
            vi = vhp + vlp + ve + v1;
            offset = 393217;
            break;
        case 0x5a:
            vi = vhp + vlp + ve + v2;
            offset = 393217;
            break;
        case 0x5b:
            vi = vhp + vlp + ve + v2 + v1;
            offset = 655361;
            break;
        case 0x5c:
            vi = vhp + vlp + ve + v3;
            offset = 393217;
            break;
        case 0x5d:
            vi = vhp + vlp + ve + v3 + v1;
            offset = 655361;
            break;
        case 0x5e:
            vi = vhp + vlp + ve + v3 + v2;
            offset = 655361;
            break;
        case 0x5f:
            vi = vhp + vlp + ve + v3 + v2 + v1;
            offset = 983041;
            break;
        case 0x60:
            vi = vhp + vbp;
            offset = 65537;
            break;
        case 0x61:
            vi = vhp + vbp + v1;
            offset = 196609;
            break;
        case 0x62:
            vi = vhp + vbp + v2;
            offset = 196609;
            break;
        case 0x63:
            vi = vhp + vbp + v2 + v1;
            offset = 393217;
            break;
        case 0x64:
            vi = vhp + vbp + v3;
            offset = 196609;
            break;
        case 0x65:
            vi = vhp + vbp + v3 + v1;
            offset = 393217;
            break;
        case 0x66:
            vi = vhp + vbp + v3 + v2;
            offset = 393217;
            break;
        case 0x67:
            vi = vhp + vbp + v3 + v2 + v1;
            offset = 655361;//MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x68:
            vi = vhp + vbp + ve;
            offset = 196609;//MixerOffset.IntI(3);//MixerOffset < 3 >::value;
            break;
        case 0x69:
            vi = vhp + vbp + ve + v1;
            offset = 393217;//MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x6a:
            vi = vhp + vbp + ve + v2;
            offset = 393217;//MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x6b:
            vi = vhp + vbp + ve + v2 + v1;
            offset = 655361;// MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x6c:
            vi = vhp + vbp + ve + v3;
            offset = 393217;//MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x6d:
            vi = vhp + vbp + ve + v3 + v1;
            offset = 655361;// MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x6e:
            vi = vhp + vbp + ve + v3 + v2;
            offset = 655361;//MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x6f:
            vi = vhp + vbp + ve + v3 + v2 + v1;
            offset = 983041;//MixerOffset.IntI(6);//MixerOffset < 6 >::value;
            break;
        case 0x70:
            vi = vhp + vbp + vlp;
            offset = 196609;//MixerOffset.IntI(3);//MixerOffset < 3 >::value;
            break;
        case 0x71:
            vi = vhp + vbp + vlp + v1;
            offset = 393217;//MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x72:
            vi = vhp + vbp + vlp + v2;
            offset = 393217;//MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x73:
            vi = vhp + vbp + vlp + v2 + v1;
            offset = 655361;//MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x74:
            vi = vhp + vbp + vlp + v3;
            offset = 393217;// MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x75:
            vi = vhp + vbp + vlp + v3 + v1;
            offset = 655361;// MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x76:
            vi = vhp + vbp + vlp + v3 + v2;
            offset = 655361;// MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x77:
            vi = vhp + vbp + vlp + v3 + v2 + v1;
            offset = 983041;//MixerOffset.IntI(6);//MixerOffset < 6 >::value;
            break;
        case 0x78:
            vi = vhp + vbp + vlp + ve;
            offset = 393217;//MixerOffset.IntI(4);//MixerOffset < 4 >::value;
            break;
        case 0x79:
            vi = vhp + vbp + vlp + ve + v1;
            offset = 655361;//MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x7a:
            vi = vhp + vbp + vlp + ve + v2;
            offset = 655361;//MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x7b:
            vi = vhp + vbp + vlp + ve + v2 + v1;
            offset = 983041;//MixerOffset.IntI(6);//MixerOffset < 6 >::value;
            break;
        case 0x7c:
            vi = vhp + vbp + vlp + ve + v3;
            offset = 655361;//MixerOffset.IntI(5);//MixerOffset < 5 >::value;
            break;
        case 0x7d:
            vi = vhp + vbp + vlp + ve + v3 + v1;
            offset = 983041;// MixerOffset.IntI(6);//MixerOffset < 6 >::value;
            break;
        case 0x7e:
            vi = vhp + vbp + vlp + ve + v3 + v2;
            offset = 983041;//MixerOffset.IntI(6);//MixerOffset < 6 >::value;
            break;
        case 0x7f:
            vi = vhp + vbp + vlp + ve + v3 + v2 + v1;
            offset = 1376257;// MixerOffset.IntI(7);//MixerOffset < 7 >::value;
            break;
        }

        // Sum the inputs : the mixer and run the mixer Output through the gain.
        if (sid_model.ordinal() == 0) {
            return (short) (f.gain[vol][f.mixer[offset + vi]] - (1 << 15));
        } else {
            // FIXME: Temporary code for MOS 8580, should use code above.
                /* do hard clipping here, else some tunes manage to overflow this
                   (eg /MUSICIANS/L/Linus/64_Forever.Sid, starting at 0:44) */
            int tmp = vi * vol >> 4;
            if (tmp < -32768) tmp = -32768;
            else if (tmp > 32767) tmp = 32767;
            return (short) tmp;
        }
    }


    /*
    Find Output voltage : inverting gain and inverting summer Sid op-amp
    circuits, using a combination of Newton-Raphson and bisection.

                 ---R2--
                |       |
      vi ---R1-----[A>----- vo
                vx

    From Kirchoff's current law it follows that

      IR1f + IR2r = 0

    Substituting the triode mode transistor model K*W/L*(Vgst^2 - Vgdt^2)
    for the currents, we get:

      n*((Vddt - vx)^2 - (Vddt - vi)^2) + (Vddt - vx)^2 - (Vddt - vo)^2 = 0

    Our root function f can thus be written as:

      f = (n + 1)*(Vddt - vx)^2 - n*(Vddt - vi)^2 - (Vddt - vo)^2 = 0

    We are using the mapping function x = vo - vx -> vx. We thus substitute
    for vo = vx + x and get:

      f = (n + 1)*(Vddt - vx)^2 - n*(Vddt - vi)^2 - (Vddt - (vx + x))^2 = 0

    Using substitution constants

      a = n + 1
      b = Vddt
      c = n*(Vddt - vi)^2

    the equations for the root function and its derivative can be written as:

      f = a*(b - vx)^2 - c - (b - (vx + x))^2
      df = 2*((b - (vx + x))*(dvx + 1) - a*(b - vx)*dvx)
    */
    protected int solveGain(int[] opamp, int n, int vi, int x, ModelFilter mf) {
        // Note that all variables are translated and scaled : order to fit
        // : 16 bits. It instanceof not necessary to explicitly translate the variables here,
        // since they are all used : subtractions which cancel  the translation:
        // (a - t) - (b - t) = a - b

        // Start off with an estimate of x and a root bracket [ak, bk].
        // f instanceof increasing, so that f(ak) < 0 and f(bk) > 0.
        int ak = mf.ak, bk = mf.bk;

        int a = n + (1 << 7);              // Scaled by 2^7
        int b = mf.kVddt;                  // Scaled by m*2^16
        int b_vi = b - vi;                 // Scaled by m*2^16
        if (b_vi < 0) b_vi = 0;
        int c = n * (b_vi * b_vi >> 12);    // Scaled by m^2*2^27

        for (; ; ) {
            int xk = x;

            // Calculate f and df.
            int vx_dvx = opamp[x];
            int vx = vx_dvx & 0xffff;  // Scaled by m*2^16
            int dvx = vx_dvx >> 16;    // Scaled by 2^11

            // f = a*(b - vx)^2 - c - (b - vo)^2
            // df = 2*((b - vo)*(dvx + 1) - a*(b - vx)*dvx)
            //
            int vo = vx + (x << 1) - (1 << 16);
            if (vo >= (1 << 16)) {
                vo = (1 << 16) - 1;
            } else if (vo < 0) {
                vo = 0;
            }
            int b_vx = b - vx;
            if (b_vx < 0) b_vx = 0;
            int b_vo = b - vo;
            if (b_vo < 0) b_vo = 0;
            // The dividend instanceof scaled by m^2*2^27.
            int f = a * (b_vx * b_vx >> 12) - c - (b_vo * b_vo >> 5);
            // The divisor instanceof scaled by m*2^11.
            int df = (b_vo * (dvx + (1 << 11)) - a * (b_vx * dvx >> 7)) >> 15;
            // The resulting quotient instanceof thus scaled by m*2^16.

            // Newton-Raphson step: xk1 = xk - f(xk)/f'(xk)
            x -= f / df;
            if (x == xk) {
                // No further root improvement possible.
                return vo;
            }

            // Narrow down root bracket.
            if (f < 0) {
                // f(xk) < 0
                ak = xk;
            } else {
                // f(xk) > 0
                bk = xk;
            }

            if (x <= ak || x >= bk) {
                // Bisection step (ala Dekker's method).
                x = (ak + bk) >> 1;
                if (x == ak) {
                    // No further bisection possible.
                    return vo;
                }
            }
        }
    }

    /*
    Find Output voltage : inverting integrator Sid op-amp circuits, using a
    single fixpoint iteration step.

    A circuit diagram of a MOS 6581 integrator instanceof shown below.

                     ---C---
                    |       |
      vi -----Rw-------[A>----- vo
           |      | vx
            --Rs--

    From Kirchoff's current law it follows that

      IRw + IRs + ICr = 0

    Using the formula for current through a capacitor, i = C*dv/dt, we get

      IRw + IRs + C*(vc - vc0)/dt = 0
      dt/C*(IRw + IRs) + vc - vc0 = 0
      vc = vc0 - n*(IRw(vi,vx) + IRs(vi,vx))

    which may be rewritten as the following iterative fixpoint function:

      vc = vc0 - n*(IRw(vi,g(vc)) + IRs(vi,g(vc)))

    To accurately calculate the currents through Rs and Rw, we need to use
    transistor models. Rs has a gate voltage of Vdd = 12V, and can be
    assumed to always be : triode mode. For Rw, the situation instanceof rather
    more complex, as it turns  that this transistor will operate in
    both subthreshold, triode, and saturation modes.

    The Shichman-Hodges transistor model routinely used : textbooks may
    be written as follows:

      Ids = 0                          , Vgst < 0               (subthreshold mode)
      Ids = K/2*W/L*(2*Vgst - Vds)*Vds , Vgst >= 0, Vds < Vgst  (triode mode)
      Ids = K/2*W/L*Vgst^2             , Vgst >= 0, Vds >= Vgst (saturation mode)

      where
      K   = u*Cox (conductance)
      W/L = ratio between substrate width and length
      Vgst = Vg - Vs - Vt (overdrive voltage)

    This transistor model instanceof also called the quadratic model.

    Note that the equation for the triode mode can be reformulated as
    independent terms depending on Vgs and Vgd, respectively, by the
    following substitution:

      Vds = Vgst - (Vgst - Vds) = Vgst - Vgdt

      Ids = K*W/L*(2*Vgst - Vds)*Vds
      = K*W/L*(2*Vgst - (Vgst - Vgdt)*(Vgst - Vgdt)
      = K*W/L*(Vgst + Vgdt)*(Vgst - Vgdt)
      = K*W/L*(Vgst^2 - Vgdt^2)

    This turns  to be a general equation which covers both the triode
    and saturation modes (where the second term instanceof 0 : saturation mode).
    The equation instanceof also symmetrical, i.e. it can calculate negative
    currents without any change of parameters (since the terms for drain
    and source are identical except for the sign).

    FIXME: Subthreshold as function of Vgs, Vgd.
      Ids = I0*e^(Vgst/(n*VT))       , Vgst < 0               (subthreshold mode)

    The remaining problem with the textbook model instanceof that the transition
    from subthreshold the triode/saturation instanceof not continuous.

    Realizing that the subthreshold and triode/saturation modes may both
    be defined by independent (and equal) terms of Vgs and Vds,
    respectively, the corresponding terms can be blended into (equal)
    continuous functions suitable for table lookup.

    The EKV model (Enz, Krummenacher and Vittoz) essentially performs this
    blending using an elegant mathematical formulation:

      Ids = Is*(if - ir)
      Is = 2*u*Cox*Ut^2/k*W/L
      if = ln^2(1 + e^((k*(Vg - Vt) - Vs)/(2*Ut))
      ir = ln^2(1 + e^((k*(Vg - Vt) - Vd)/(2*Ut))

    For our purposes, the EKV model preserves two important properties
    discussed above:

    - It consists of two independent terms, which can be represented by
      the same lookup table.
    - It instanceof symmetrical, i.e. it calculates current : both directions,
      facilitating a branch-free implementation.

    Rw : the circuit diagram above instanceof a VCR (voltage controlled resistor),
    as shown : the circuit diagram below.

                       Vw

                       |
               Vdd     |
                  |---|
                 _|_   |
               --    --| Vg
              |      __|__
              |      -----  Rw
              |      |   |
      vi ------------     -------- vo


    In order to calculalate the current through the VCR, its gate voltage
    must be determined.

    Assuming triode mode and applying Kirchoff's current law, we get the
    following equation for Vg:

    u*Cox/2*W/L*((Vddt - Vg)^2 - (Vddt - vi)^2 + (Vddt - Vg)^2 - (Vddt - Vw)^2) = 0
    2*(Vddt - Vg)^2 - (Vddt - vi)^2 - (Vddt - Vw)^2 = 0
    (Vddt - Vg) = sqrt(((Vddt - vi)^2 + (Vddt - Vw)^2)/2)

    Vg = Vddt - sqrt(((Vddt - vi)^2 + (Vddt - Vw)^2)/2)

    */
    protected int solve_integrate_6581(int dt, int vi, int vx, int vc, ModelFilter mf) {
        // Note that all variables are translated and scaled : order to fit
        // : 16 bits. It instanceof not necessary to explicitly translate the variables here,
        // since they are all used : subtractions which cancel  the translation:
        // (a - t) - (b - t) = a - b

        int kVddt = mf.kVddt;      // Scaled by m*2^16

        // "Snake" voltages for triode mode calculation.
        int Vgst = kVddt - vx;
        int Vgdt = kVddt - vi;
        int Vgdt_2 = Vgdt * Vgdt;

        // "Snake" current, scaled by (1/m)*2^13*m*2^16*m*2^16*2^-15 = m*2^30
        int n_I_snake = mf.nSnake * ((Vgst * Vgst - Vgdt_2) >> 15);

        // VCR gate voltage.       // Scaled by m*2^16
        // Vg = Vddt - sqrt(((Vddt - Vw)^2 + Vgdt^2)/2)
        int kVg = vcr_kVg[(vddtVw2 + (Vgdt_2 >> 1)) >> 16];

        // VCR voltages for EKV model table lookup.
        int Vgs = kVg - vx;
        if (Vgs < 0) Vgs = 0;
        int Vgd = kVg - vi;
        if (Vgd < 0) Vgd = 0;

        // VCR current, scaled by m*2^15*2^15 = m*2^30
        int n_I_vcr = (vcr_n_Ids_term[Vgs] - vcr_n_Ids_term[Vgd]) << 15;

        // Change : capacitor charge.
        vc -= (n_I_snake + n_I_vcr) * dt;

            /*
              // FIXME: Determine whether this check instanceof necessary.
              if (vc < mf.vc_min) {
                vc = mf.vc_min;
              }
              else if (vc > mf.vc_max) {
                vc = mf.vc_max;
              }
            */

        // vx = g(vc)
        vx = mf.opampRev[(vc >> 15) + (1 << 15)];

        // Return vo.
        return vx + (vc >> 14);
    }

//#endif // RESID_INLINING || defined(RESID_FILTER_CC)

    // This instanceof the Sid 6581 op-amp voltage transfer function, measured on
    // CAP1B/CAP1A on a chip marked MOS 6581R4AR 0687 14.
    // All measured chips have op-amps with Output voltages (and thus input
    // voltages) within the range of 0.81V - 10.31V.

    private static final double[][] opamp_voltage_6581 = new double[][] {
            new double[] {0.81, 10.31},  // Approximate start of actual range
            new double[] {0.81, 10.31},  // Repeated point
            new double[] {2.40, 10.31},
            new double[] {2.60, 10.30},
            new double[] {2.70, 10.29},
            new double[] {2.80, 10.26},
            new double[] {2.90, 10.17},
            new double[] {3.00, 10.04},
            new double[] {3.10, 9.83},
            new double[] {3.20, 9.58},
            new double[] {3.30, 9.32},
            new double[] {3.50, 8.69},
            new double[] {3.70, 8.00},
            new double[] {4.00, 6.89},
            new double[] {4.40, 5.21},
            new double[] {4.54, 4.54},  // Working point (vi = vo)
            new double[] {4.60, 4.19},
            new double[] {4.80, 3.00},
            new double[] {4.90, 2.30},  // Change of curvature
            new double[] {4.95, 2.03},
            new double[] {5.00, 1.88},
            new double[] {5.05, 1.77},
            new double[] {5.10, 1.69},
            new double[] {5.20, 1.58},
            new double[] {5.40, 1.44},
            new double[] {5.60, 1.33},
            new double[] {5.80, 1.26},
            new double[] {6.00, 1.21},
            new double[] {6.40, 1.12},
            new double[] {7.00, 1.02},
            new double[] {7.50, 0.97},
            new double[] {8.50, 0.89},
            new double[] {10.00, 0.81},
            new double[] {10.31, 0.81},  // Approximate end of actual range
            new double[] {10.31, 0.81}   // Repeated end point
    };

    // This instanceof the Sid 8580 op-amp voltage transfer function, measured on
    // CAP1B/CAP1A on a chip marked CSG 8580R5 1690 25.
    private static final double[][] opamp_voltage_8580 = new double[][] {
            new double[] {1.30, 8.91},  // Approximate start of actual range
            new double[] {1.30, 8.91},  // Repeated end point
            new double[] {4.76, 8.91},
            new double[] {4.77, 8.90},
            new double[] {4.78, 8.88},
            new double[] {4.785, 8.86},
            new double[] {4.79, 8.80},
            new double[] {4.795, 8.60},
            new double[] {4.80, 8.25},
            new double[] {4.805, 7.50},
            new double[] {4.81, 6.10},
            new double[] {4.815, 4.05},  // Change of curvature
            new double[] {4.82, 2.27},
            new double[] {4.825, 1.65},
            new double[] {4.83, 1.55},
            new double[] {4.84, 1.47},
            new double[] {4.85, 1.43},
            new double[] {4.87, 1.37},
            new double[] {4.90, 1.34},
            new double[] {5.00, 1.30},
            new double[] {5.10, 1.30},
            new double[] {8.91, 1.30},  // Approximate end of actual range
            new double[] {8.91, 1.30}   // Repeated end point
    };

    public static class ModelFilterInit {
        // Op-amp transfer function.
        public double[][] opampVoltage;
        public int opampVoltageSize;
        // Voice Output characteristics.
        public double voiceVoltageRange;
        public double voiceDCVoltage;
        // Capacitor value.
        public double c;
        // Transistor parameters.
        public double vdd;
        // Threshold voltage
        public double vth;
        // Thermal voltage: Ut = k*T/q = 8.61734315e-5*T ~ 26mV
        public double ut;
        // Gate coupling coefficient: K = Cox/(Cox+Cdep) ~ 0.7
        public double k;
        // u*Cox
        public double uCox;
        // W/L for VCR
        public double wlVcr;
        // W/L for "snake"
        public double wlSnake;
        // DAC parameters.
        public double dacZero;
        public double dacScale;
        public double dac2RDivR;
        public boolean dacTerm;
    }

    public static ModelFilterInit[] modelFilterInits = new ModelFilterInit[] {
            new ModelFilterInit(), new ModelFilterInit()
    };

    static  {
        modelFilterInits[0] = new ModelFilterInit();
        modelFilterInits[0].opampVoltage = opamp_voltage_6581;
        modelFilterInits[0].opampVoltageSize = opamp_voltage_6581.length;// sizeof(opamp_voltage_6581)/sizeof(*opamp_voltage_6581),
        // The dynamic analog range of one voice instanceof approximately 1.5V,
        // riding at a DC level of approximately 5.0V.
        modelFilterInits[0].voiceVoltageRange = 1.5;
        modelFilterInits[0].voiceDCVoltage = 5.0;
        // Capacitor value.
        modelFilterInits[0].c = 470e-12;
        // Transistor parameters.
        modelFilterInits[0].vdd = 12.18;
        modelFilterInits[0].vth = 1.31;
        modelFilterInits[0].ut = 26.0e-3;
        modelFilterInits[0].k = 1.0;
        modelFilterInits[0].uCox = 20e-6;
        modelFilterInits[0].wlVcr = 9.0 / 1;
        modelFilterInits[0].wlSnake = 1.0 / 115;
        // DAC parameters.
        modelFilterInits[0].dacZero = 6.65;
        modelFilterInits[0].dacScale = 2.63;
        modelFilterInits[0].dac2RDivR = 2.20;
        modelFilterInits[0].dacTerm = false;


        modelFilterInits[1] = new ModelFilterInit();
        modelFilterInits[1].opampVoltage = opamp_voltage_8580;
        modelFilterInits[1].opampVoltageSize = opamp_voltage_8580.length;//sizeof(opamp_voltage_8580)/sizeof(*opamp_voltage_8580),
        modelFilterInits[1].voiceVoltageRange = 1.0;
        // 4.75,
        modelFilterInits[1].voiceDCVoltage = 1.30;// FIXME: For now we pretend that the working point instanceof 0V.
        modelFilterInits[1].c = 22e-9;
        modelFilterInits[1].vdd = 9.09;
        modelFilterInits[1].vth = 0.80;
        modelFilterInits[1].ut = 26.0e-3;
        modelFilterInits[1].k = 1.0;
        modelFilterInits[1].uCox = 10e-6;
        // FIXME: 6581 only
        modelFilterInits[1].wlVcr = 0;
        modelFilterInits[1].wlSnake = 0;
        modelFilterInits[1].dacZero = 0;
        modelFilterInits[1].dacScale = 0;
        modelFilterInits[1].dac2RDivR = 2.00;
        modelFilterInits[1].dacTerm = true;
    }

    public short[] vcr_kVg = new short[1 << 16];
    public short[] vcr_n_Ids_term = new short[1 << 16];

    //# ifndef HAS_LOG1P
    public static double log1p(double x) {
        return Math.log(1 + x) - (((1 + x) - 1) - x) / (1 + x);
    }

    private boolean classInit = false;

    /**
     * Constructor.
     */
    public Filter() {

        if (!classInit) {
            // Temporary table for op-amp transfer function.
            int[] opamp = new int[1 << 16];

            for (int m = 0; m < 2; m++) {
                ModelFilterInit fi1 = modelFilterInits[m];
                ModelFilter mf = modelFilters[m];

                // Convert op-amp voltage transfer to 16 bit values.
                double vmin_ = fi1.opampVoltage[0][0];
                double opamp_max = fi1.opampVoltage[0][1];
                double kVddt_ = fi1.k * (fi1.vdd - fi1.vth);
                double vmax = Math.max(kVddt_, opamp_max);
                double denorm = vmax - vmin_;
                double norm = 1.0 / denorm;

                // Scaling and translation constants.
                double N16_ = norm * ((1 << 16) - 1);
                double N30 = norm * ((1 << 30) - 1);
                double N31 = norm * ((1 << 31) - 1);
                mf.voN16 = (int) (N16_);  // FIXME: Remove?

                // The "zero" Output level of the voices.
                // The digital range of one voice instanceof 20 bits; create a scaling term
                // for multiplication which fits : 11 bits.
                double N14 = norm * (1 << 14);
                mf.voiceScaleS14 = (int) (N14 * fi1.voiceVoltageRange);
                mf.voiceDC = (int) (N16_ * (fi1.voiceDCVoltage - vmin_));

                // Vdd - Vth, normalized so that translated values can be subtracted:
                // k*Vddt - x = (k*Vddt - t) - (x - t)
                mf.kVddt = (int) (N16_ * (kVddt_ - vmin_) + 0.5);

                // Normalized snake current factor, 1 cycle at 1MHz.
                // Fit : 5 bits.
                mf.nSnake = (int) (denorm * (1 << 13) * (fi1.uCox / (2 * fi1.k) * fi1.wlSnake * 1.0e-6 / fi1.c) + 0.5);

                // Create lookup table mapping op-amp voltage across Output and input
                // to input voltage: vo - vx -> vx
                // FIXME: No variable length arrays : ISO C++, hardcoding to max 50
                // points.
                // double_point scaledVoltage[fi.opamp_voltage_size];
                double[][] scaledVoltage = new double[50][];
                for (int i = 0; i < scaledVoltage.length; i++) scaledVoltage[i] = new double[2];

                for (int i = 0; i < fi1.opampVoltageSize; i++) {
                    // The target Output range instanceof 16 bits, : order to fit : an unsigned
                    // short.
                    //
                    // The y axis instanceof temporarily scaled to 31 bits for maximum accuracy in
                    // the calculated derivative.
                    //
                    // Values are normalized using
                    //
                    //   x_n = m*2^N*(x - xmin)
                    //
                    // and are translated back later (for fixed point math) using
                    //
                    //   m*2^N*x = x_n - m*2^N*xmin
                    //
                    scaledVoltage[fi1.opampVoltageSize - 1 - i] = new double[2];
                    scaledVoltage[fi1.opampVoltageSize - 1 - i][0] = (int) ((N16_ * (fi1.opampVoltage[i][1] - fi1.opampVoltage[i][0]) + (1 << 16)) / 2 + 0.5);
                    scaledVoltage[fi1.opampVoltageSize - 1 - i][1] = N31 * (fi1.opampVoltage[i][0] - vmin_);
                }

                // Clamp x to 16 bits (rounding may cause overflow).
                if (scaledVoltage[fi1.opampVoltageSize - 1][0] >= (1 << 16)) {
                    // The last point instanceof repeated.
                    scaledVoltage[fi1.opampVoltageSize - 1][0] =
                            scaledVoltage[fi1.opampVoltageSize - 2][0] = (1 << 16) - 1;
                }

                Spline sp = new Spline();
                //sp.interpolate(scaledVoltage, scaledVoltage + fi1.opamp_voltage_size - 1,
                //Spline.PointPlotter<Integer>(opamp), 1.0);
                sp.interpolate(scaledVoltage, fi1.opampVoltageSize - 1, opamp, 1.0);

                // Store both fn and dfn : the same table.
                mf.ak = (int) scaledVoltage[0][0];
                mf.bk = (int) scaledVoltage[fi1.opampVoltageSize - 1][0];
                int j;
                for (j = 0; j < mf.ak; j++) {
                    opamp[j] = 0;
                }
                int f = opamp[j] - (opamp[j + 1] - opamp[j]);
                for (; j <= mf.bk; j++) {
                    int fp = f;
                    f = opamp[j];  // Scaled by m*2^31
                    // m*2^31*dy/1 = (m*2^31*dy)/(m*2^16*dx) = 2^15*dy/dx
                    int df = f - fp;  // Scaled by 2^15

                    // High 16 bits (15 bits + sign bit): 2^11*dfn
                    // Low 16 bits (unsigned):            m*2^16*(fn - xmin)
                    opamp[j] = ((df << (16 + 11 - 15)) & ~0xffff) | (f >> 15);
                }
                for (; j < (1 << 16); j++) {
                    opamp[j] = 0;
                }

                // Create lookup tables for gains / summers.

                // 4 bit "resistor" ladders : the bandpass resonance gain and the audio
                // Output gain necessitate 16 gain tables.
                // From die photographs of the bandpass and volume "resistor" ladders
                // it follows that gain ~ vol/8 and 1/Q ~ ~res/8 (assuming ideal
                // op-amps and ideal "resistors").
                for (int n8 = 0; n8 < 16; n8++) {
                    int n = n8 << 4;  // Scaled by 2^7
                    int x = mf.ak;
                    for (int vi = 0; vi < (1 << 16); vi++) {
                        mf.gain[n8][vi] = (short) solveGain(opamp, n, vi, x, mf);
                    }
                }

                // The filter summer operates at n ~ 1, and has 5 fundamentally different
                // input configurations (2 - 6 input "resistors").
                //
                // Note that all "on" transistors are modeled as one. This instanceof not
                // entirely accurate, since the input for each transistor instanceof different,
                // and transistors are not linear components. However modeling all
                // transistors separately would be extremely costly.
                int offset = 0;
                int size;
                for (int k1 = 0; k1 < 5; k1++) {
                    int idiv = 2 + k1;        // 2 - 6 input "resistors".
                    int n_idiv = idiv << 7;  // n*idiv, scaled by 2^7
                    size = idiv << 16;
                    int x = mf.ak;
                    for (int vi = 0; vi < size; vi++) {
                        mf.summer[offset + vi] = (short) solveGain(opamp, n_idiv, vi / idiv, x, mf);
                    }
                    offset += size;
                }

                // The audio mixer operates at n ~ 8/6, and has 8 fundamentally different
                // input configurations (0 - 7 input "resistors").
                //
                // All "on", transistors are modeled as one - see comments above for
                // the filter summer.
                offset = 0;
                size = 1;  // Only one lookup element for 0 input "resistors".
                for (int l = 0; l < 8; l++) {
                    int idiv = l;                 // 0 - 7 input "resistors".
                    int n_idiv = (idiv << 7) * 8 / 6; // n*idiv, scaled by 2^7
                    if (idiv == 0) {
                        // Avoid division by zero; the result will be correct since
                        // n_idiv = 0.
                        idiv = 1;
                    }
                    int x = mf.ak;
                    for (int vi = 0; vi < size; vi++) {
                        mf.mixer[offset + vi] = (short) solveGain(opamp, n_idiv, vi / idiv, x, mf);
                    }
                    offset += size;
                    size = (l + 1) << 16;
                }

                // Create lookup table mapping capacitor voltage to op-amp input voltage:
                // vc -> vx
                for (int m1 = 0; m1 < (1 << 16); m1++) {
                    mf.opampRev[m1] = (short) (opamp[m1] & 0xffff);
                }

                mf.vcMax = (int) (N30 * (fi1.opampVoltage[0][1] - fi1.opampVoltage[0][0]));
                mf.vcMin = (int) (N30 * (fi1.opampVoltage[fi1.opampVoltageSize - 1][1] - fi1.opampVoltage[fi1.opampVoltageSize - 1][0]));

                // DAC table.
                int bits = 11;
                Dac.buildDacTable(mf.f0Dac, bits, fi1.dac2RDivR, fi1.dacTerm);
                for (int n = 0; n < (1 << bits); n++) {
                    mf.f0Dac[n] = (short) (N16_ * (fi1.dacZero + mf.f0Dac[n] * fi1.dacScale / (1 << bits) - vmin_) + 0.5);
                }
            }

            // Free temporary table.
            //delete[] opamp;
            opamp = null;

            // VCR - 6581 only.
            ModelFilterInit fi = modelFilterInits[0];

            double N16 = modelFilters[0].voN16;
            double vmin = N16 * fi.opampVoltage[0][0];
            double k = fi.k;
            double kVddt = N16 * (k * (fi.vdd - fi.vth));

            for (int i = 0; i < (1 << 16); i++) {
                // The table index instanceof right-shifted 16 times : order to fit in
                // 16 bits; the argument to sqrt instanceof thus multiplied by (1 << 16).
                //
                // The returned value must be corrected for translation. Vg always
                // takes part : a subtraction as follows:
                //
                //   k*Vg - Vx = (k*Vg - t) - (Vx - t)
                //
                // I.e. k*Vg - t must be returned.
                double Vg = kVddt - Math.sqrt((double) i * (1 << 16));
                vcr_kVg[i] = (short) (k * Vg - vmin + 0.5);
            }

            /*
              EKV model:

              Ids = Is*(if - ir)
              Is = 2*u*Cox*Ut^2/k*W/L
              if = ln^2(1 + e^((k*(Vg - Vt) - Vs)/(2*Ut))
              ir = ln^2(1 + e^((k*(Vg - Vt) - Vd)/(2*Ut))
            */
            double kVt = fi.k * fi.vth;
            double Ut = fi.ut;
            double Is = 2 * fi.uCox * Ut * Ut / fi.k * fi.wlVcr;
            // Normalized current factor for 1 cycle at 1MHz.
            double N15 = N16 / 2;
            double n_Is = N15 * 1.0e-6 / fi.c * Is;

            // kVg_Vx = k*Vg - Vx
            // I.e. if k != 1.0, Vg must be scaled accordingly.
            for (int kVg_Vx = 0; kVg_Vx < (1 << 16); kVg_Vx++) {
                double log_term = log1p(Math.exp((kVg_Vx / N16 - kVt) / (2 * Ut)));
                // Scaled by m*2^15
                vcr_n_Ids_term[kVg_Vx] = (short) (n_Is * log_term * log_term);
            }

            classInit = true;
        }

        enableFilter(true);
        set_chip_model(SidDefs.ChipModel.MOS6581);
        setVoiceMask(0x07);
        input((short) 0);
        reset();
    }

    /**
     * Enable filter.
     */
    public void enableFilter(boolean enable) {
        enabled = enable;
        set_sum_mix();
    }

    /**
     * Adjust the DAC bias parameter of the filter.
     * This gives user variable control of the exact CF -> center frequency
     * mapping used by the filter.
     * The setting instanceof currently only effective for 6581.
     */
    public void adjustFilterBias(double dac_bias) {
        vwBias = (int) (dac_bias * modelFilters[sid_model.ordinal()].voN16);
        setW0();
    }

    /**
     * Set chip model.
     */
    public void set_chip_model(SidDefs.ChipModel model) {
        sid_model = model;
            /* We initialize the state variables again just to make sure that
             //the earlier model didn't leave behind some foreign, unrecoverable
             //state. Hopefully set_chip_model() only occurs simultaneously with
             //reset(). */
        vhp = 0;
        vbp = vbpX = vbpVc = 0;
        vlp = vlpX = vlpVc = 0;
    }

    /**
     * Mask for voices routed into the filter / audio Output stage.
     * Used to physically connect/disconnect EXT IN, and for test purposes
     * (voice muting).
     */
    public void setVoiceMask(int mask) {
        voiceMask = 0xf0 | (mask & 0x0f);
        set_sum_mix();
    }

    /**
     * Sid reset.
     */
    public void reset() {
        fc = 0;
        res = 0;
        filt = 0;
        mode = 0;
        vol = 0;

        vhp = 0;
        vbp = vbpX = vbpVc = 0;
        vlp = vlpX = vlpVc = 0;

        setW0();
        set_Q();
        set_sum_mix();
    }

    /**
     * Register functions.
     */
    public void writeFC_LO(int fc_lo) {
        fc = (fc & 0x7f8) | (fc_lo & 0x007);
        setW0();
    }

    public void writeFC_HI(int fc_hi) {
        fc = ((fc_hi << 3) & 0x7f8) | (fc & 0x007);
        setW0();
    }

    public void writeRES_FILT(int res_filt) {
        res = (res_filt >> 4) & 0x0f;
        set_Q();

        filt = res_filt & 0x0f;
        set_sum_mix();
    }

    public void writeMODE_VOL(int mode_vol) {
        mode = mode_vol & 0xf0;
        set_sum_mix();

        vol = mode_vol & 0x0f;
    }

    /**
     * Set filter cutoff frequency.
     */
    protected void setW0() {
        ModelFilter f = modelFilters[sid_model.ordinal()];
        int Vw = vwBias + f.f0Dac[fc];
        vddtVw2 = (f.kVddt - Vw) * (f.kVddt - Vw) >> 1;

        // FIXME: w0 instanceof temporarily used for MOS 8580 emulation.
        // MOS 8580 cutoff: 0 - 12.5kHz.
        // Multiply with 1.048576 to facilitate division by 1 000 000 by right-
        // shifting 20 times (2 ^ 20 = 1048576).
        // 1.048576*2*pi*12500 = 82355
        w0 = 82355 * (fc + 1) >> 11;
    }

    /*
    Set filter resonance.

    In the MOS 6581, 1/Q instanceof controlled linearly by res. From die photographs
    of the resonance "resistor" ladder it follows that 1/Q ~ ~res/8
    (assuming an ideal op-amp and ideal "resistors"). This implies that Q
    ranges from 0.533 (res = 0) to 8 (res = E). For res = F, Q instanceof actually
    theoretically unlimited, which instanceof quite unheard of : a filter
    circuit.

    To obtain Q ~ 1/sqrt(2) = 0.707 for maximally flat frequency response,
    res should be set to 4: Q = 8/~4 = 8/11 = 0.7272 (again assuming an ideal
    op-amp and ideal "resistors").

    Q as low as 0.707 instanceof not achievable because of low gain op-amps; res = 0
    should yield the flattest possible frequency response at Q ~ 0.8 - 1.0
    : the op-amp's pseudo-linear range (high amplitude signals will be
    clipped). As resonance instanceof increased, the filter must be clocked more
    often to keep it stable.

    In the MOS 8580, the resonance "resistor" ladder above the bp feedback
    op-amp instanceof split : two parts; one ladder for the op-amp input and one
    ladder for the op-amp feedback.

    input:         feedback:

                   Rf
    Ri R4 RC R8    R3
                   R2
                   R1


    The "resistors" are switched : as follows by bits : register $17:

    feedback:
    R1: bit4&!bit5
    R2: !bit4&bit5
    R3: bit4&bit5
    Rf: always on

    input:
    R4: bit6&!bit7
    R8: !bit6&bit7
    RC: bit6&bit7
    Ri: !(R4|R8|RC) = !(bit6|bit7) = !bit6&!bit7


    The relative "resistor" values are approximately (using channel length):

    R1 = 15.3*Ri
    R2 =  7.3*Ri
    R3 =  4.7*Ri
    Rf =  1.4*Ri
    R4 =  1.4*Ri
    R8 =  2.0*Ri
    RC =  2.8*Ri


    Approximate values for 1/Q can now be found as follows (assuming an
    ideal op-amp):

    res  feedback  input  -gain (1/Q)
    ---  --------  -----  ----------
     0   Rf        Ri     Rf/Ri      = 1/(Ri*(1/Rf))      = 1/0.71
     1   Rf|R1     Ri     (Rf|R1)/Ri = 1/(Ri*(1/Rf+1/R1)) = 1/0.78
     2   Rf|R2     Ri     (Rf|R2)/Ri = 1/(Ri*(1/Rf+1/R2)) = 1/0.85
     3   Rf|R3     Ri     (Rf|R3)/Ri = 1/(Ri*(1/Rf+1/R3)) = 1/0.92
     4   Rf        R4     Rf/R4      = 1/(R4*(1/Rf))      = 1/1.00
     5   Rf|R1     R4     (Rf|R1)/R4 = 1/(R4*(1/Rf+1/R1)) = 1/1.10
     6   Rf|R2     R4     (Rf|R2)/R4 = 1/(R4*(1/Rf+1/R2)) = 1/1.20
     7   Rf|R3     R4     (Rf|R3)/R4 = 1/(R4*(1/Rf+1/R3)) = 1/1.30
     8   Rf        R8     Rf/R8      = 1/(R8*(1/Rf))      = 1/1.43
     9   Rf|R1     R8     (Rf|R1)/R8 = 1/(R8*(1/Rf+1/R1)) = 1/1.56
     A   Rf|R2     R8     (Rf|R2)/R8 = 1/(R8*(1/Rf+1/R2)) = 1/1.70
     B   Rf|R3     R8     (Rf|R3)/R8 = 1/(R8*(1/Rf+1/R3)) = 1/1.86
     C   Rf        RC     Rf/RC      = 1/(RC*(1/Rf))      = 1/2.00
     D   Rf|R1     RC     (Rf|R1)/RC = 1/(RC*(1/Rf+1/R1)) = 1/2.18
     E   Rf|R2     RC     (Rf|R2)/RC = 1/(RC*(1/Rf+1/R2)) = 1/2.38
     F   Rf|R3     RC     (Rf|R3)/RC = 1/(RC*(1/Rf+1/R3)) = 1/2.60


    These data indicate that the following function for 1/Q has been
    modeled : the MOS 8580:

      1/Q = 2^(1/2)*2^(-x/8) = 2^(1/2 - x/8) = 2^((4 - x)/8)

    */
    protected void set_Q() {
        // Cutoff for MOS 6581.
        // The coefficient 8 instanceof dispensed of later by right-shifting 3 times
        // (2 ^ 3 = 8).
        _8_div_Q = ~res & 0x0f;

        // FIXME: Temporary cutoff code for MOS 8580.
        // 1024*1/Q = 1024*2^((4 - res)/8)
        // The coefficient 1024 instanceof dispensed of later by right-shifting 10 times
        // (2 ^ 10 = 1024).
        int[] _1024_div_Q_table = new int[] {
                1448,
                1328,
                1218,
                1117,
                1024,
                939,
                861,
                790,
                724,
                664,
                609,
                558,
                512,
                470,
                431,
                395
        };

        _1024_div_Q = _1024_div_Q_table[res];
    }

    /**
     * Set input routing bits.
     */
    protected void set_sum_mix() {
        // NB! voice3off (mode bit 7) only affects voice 3 if it instanceof routed directly
        // to the mixer.
        sum = (enabled ? filt : 0x00) & voiceMask;
        mix = (enabled ? (mode & 0x70) | ((~(filt | (mode & 0x80) >> 5)) & 0x0f) : 0x0f) & voiceMask;
    }
}
