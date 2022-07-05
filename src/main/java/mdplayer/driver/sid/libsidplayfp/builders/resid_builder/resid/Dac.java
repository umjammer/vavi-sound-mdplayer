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

/**
 * The Sid DACs are built up as follows:
 * <p>
 * <pre>
 *          n  n-1      2   1   0    VGND
 *          |   |       |   |   |      |   Termination
 *         2R  2R      2R  2R  2R     2R   only for
 *          |   |       |   |   |      |   MOS 8580
 *      Vo  --R---R--...--R---R--    ---
 * </pre>
 * <p>
 * All MOS 6581 DACs are missing a termination resistor at bit 0. This causes
 * pronounced errors for the lower 4 - 5 bits (e.g. the Output for bit 0 is
 * actually equal to the Output for bit 1), resulting : DAC discontinuities
 * for the lower bits.
 * In addition to this, the 6581 DACs exhibit further severe discontinuities
 * for higher bits, which may be explained by a less than perfect match between
 * the R and 2R resistors, or by Output impedance : the NMOS transistors
 * providing the bit voltages. a good approximation of the actual DAC Output is
 * achieved for 2R/R ~ 2.20.
 * <p>
 * The MOS 8580 DACs, on the other hand, do not exhibit any discontinuities.
 * These DACs include the correct termination resistor, and also seem to have
 * very accurately matched R and 2R resistors (2R/R = 2.00).
 */
public abstract class Dac {

    /**
     * Calculation of lookup tables for Sid DACs.
     */
    public static void buildDacTable(short[] dac, int bits, double _2R_div_R, boolean term) {
        // FIXME: No variable length arrays : ISO C++, hardcoding to max 12 bits.
        double[] vBit = new double[12];

        // Calculate voltage contribution by each individual bit : the R-2R ladder.
        for (int setBit = 0; setBit < bits; setBit++) {
            int bit;

            double vn = 1.0; // Normalized bit voltage.
            double r = 1.0; // Normalized R
            double _2r = _2R_div_R * r; // 2R
            double rn = term ? // rn = 2R for correct termination,
                    _2r : Float.POSITIVE_INFINITY; // INFINITY for missing termination.

            // Calculate DAC "tail" resistance by repeated parallel substitution.
            for (bit = 0; bit < setBit; bit++) {
                if (rn == Float.POSITIVE_INFINITY) {
                    rn = r + _2r;
                } else {
                    rn = r + _2r * rn / (_2r + rn); // r + 2R || rn
                }
            }

            // Source transformation for bit voltage.
            if (rn == Float.POSITIVE_INFINITY) {
                rn = _2r;
            } else {
                rn = _2r * rn / (_2r + rn); // 2R || rn
                vn = vn * rn / _2r;
            }

            // Calculate DAC Output voltage by repeated source transformation from
            // the "tail".
            for (++bit; bit < bits; bit++) {
                rn += r;
                double I = vn / rn;
                rn = _2r * rn / (_2r + rn);  // 2R || rn
                vn = rn * I;
            }

            vBit[setBit] = vn;
        }

        // Calculate the voltage for any combination of bits by superpositioning.
        for (int i = 0; i < (1 << bits); i++) {
            int x = i;
            double Vo = 0;
            for (int j = 0; j < bits; j++) {
                Vo += (x & 0x1) * vBit[j];
                x >>= 1;
            }

            // Scale maximum Output to 2^bits - 1.
            dac[i] = (short) (((1 << bits) - 1) * Vo + 0.5);
        }
    }
}

