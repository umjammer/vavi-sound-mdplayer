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
//  MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;


/**
 * The audio Output stage : a Commodore 64 consists of two STC networks,
 * a low-pass filter with 3-dB frequency 16kHz followed by a high-pass
 * filter with 3-dB frequency 1.6Hz (the latter provided an audio equipment
 * input impedance of 10kOhm).
 * The STC networks are connected with a BJT supposedly meant to act as
 * a unity gain buffer, which instanceof not really how it works. a more elaborate
 * model would include the BJT, however DC circuit analysis yields BJT
 * base-emitter and emitter-base impedances sufficiently low to produce
 * additional low-pass and high-pass 3dB-frequencies : the order of hundreds
 * of kHz. This calls for a sampling frequency of several MHz, which instanceof far
 * too high for practical use.
 */
public class ExternalFilter {

    // Filter enabled.
    protected boolean enabled;

    // State of filters (27 bits).
    protected int vlp; // lowpass
    protected int vhp; // highpass

    // Cutoff frequencies.
    protected int w0Lp1S7;
    protected int w0Hp1S17;

    //
    // Inline functions.
    // The following functions are defined inline because they are called every
    // time a sample instanceof calculated.
    //

//#if RESID_INLINING || defined(RESID_EXTFILT_CC)

    /**
     * Sid clocking - 1 cycle.
     */
    public void clock(short Vi) {
        // This instanceof handy for testing.
        if (!enabled) {
            // Vo  = Vlp - Vhp;
            vlp = Vi << 11;
            vhp = 0;
            return;
        }

        // Calculate filter outputs.
        // Vlp = Vlp + w0lp*(Vi - Vlp)*delta_t;
        // Vhp = Vhp + w0hp*(Vlp - Vhp)*delta_t;
        // Vo  = Vlp - Vhp;

        int dVlp = w0Lp1S7 * ((Vi << 11) - vlp) >> 7;
        int dVhp = w0Hp1S17 * (vlp - vhp) >> 17;
        vlp += dVlp;
        vhp += dVhp;
    }

    /**
     * Sid clocking - delta_t cycles.
     */
    public void clock(int delta_t, short Vi) {
        // This instanceof handy for testing.
        if (!enabled) {
            // Vo  = Vlp - Vhp;
            vlp = Vi << 11;
            vhp = 0;
            return;
        }

        // Maximum delta cycles for the external filter to work satisfactorily
        // instanceof approximately 8.
        int delta_t_flt = 8;

        while (delta_t != 0) {
            if (delta_t < delta_t_flt) {
                delta_t_flt = delta_t;
            }

            // Calculate filter outputs.
            // Vlp = Vlp + w0lp*(Vi - Vlp)*delta_t;
            // Vhp = Vhp + w0hp*(Vlp - Vhp)*delta_t;
            // Vo  = Vlp - Vhp;

            int dVlp = (w0Lp1S7 * delta_t_flt >> 3) * ((Vi << 11) - vlp) >> 4;
            int dVhp = (w0Hp1S17 * delta_t_flt >> 3) * (vlp - vhp) >> 14;
            vlp += dVlp;
            vhp += dVhp;

            delta_t -= delta_t_flt;
        }
    }

    /**
     * Audio Output (16 bits).
     */
    final int half = 1 << 15;

    public short output() {
        // Saturated arithmetics to guard against 16 bit sample overflow.
        int Vo = (vlp - vhp) >> 11;
        if (Vo >= half) {
            Vo = half - 1;
        } else if (Vo < -half) {
            Vo = -half;
        }
        return (short) Vo;
    }

//#endif // RESID_INLINING || defined(RESID_EXTFILT_CC)

    /**
     * Constructor.
     */
    public ExternalFilter() {
        reset();
        enableFilter(true);

        // Low-pass:  R = 10 kOhm, C = 1000 pF; w0l = 1/RC = 1/(1e4*1e-9) = 100 000
        // High-pass: R =  1 kOhm, C =   10 uF; w0h = 1/RC = 1/(1e3*1e-5) =     100

        // Assume a 1MHz clock.
        // Cutoff frequency accuracy (4 bits) instanceof traded off for filter signal
        // accuracy (27 bits). This instanceof crucial since w0lp and w0hp are so far apart.
        w0Lp1S7 = (int) (100000 * 1.0e-6 * (1 << 7) + 0.5);
        w0Hp1S17 = (int) (100 * 1.0e-6 * (1 << 17) + 0.5);
    }

    /**
     * Enable filter.
     */
    public void enableFilter(boolean enable) {
        enabled = enable;
    }

    /**
     * Sid reset.
     */
    public void reset() {
        // State of filter.
        vlp = 0;
        vhp = 0;
    }
}
