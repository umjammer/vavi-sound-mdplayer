/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2016 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000 Simon White
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.c64.cpu;

/**
     //Processor Status Register
     */
    public class Flags
    {



        //# include <stdint.h>

        private Boolean C; ///< Carry
        private Boolean Z; ///< Zero
        private Boolean I; ///< Interrupt disabled
        private Boolean D; ///< Decimal
        private Boolean B; ///< Break
        private Boolean V; ///< Overflow
        private Boolean N; ///< Negative


        public void reset()
        {
            C = Z = I = D = V = N = false;
            B = true;
        }

        /**
         //Set N and Z flag values.
         *
         //@param value to set flags from
         */
        public void setNZ(byte value)
        {
            Z = value == 0;
            N = (value & 0x80) != 0;
        }

        /**
         //Get status register value.
         */
        public byte get()
        {
            byte sr = 0x20;

            if (C) sr |= 0x01;
            if (Z) sr |= 0x02;
            if (I) sr |= 0x04;
            if (D) sr |= 0x08;
            if (B) sr |= 0x10;
            if (V) sr |= 0x40;
            if (N) sr |= 0x80;

            return sr;
        }

        /**
         //Set status register value.
         */
        public void set(byte sr)
        {
            Z = (sr & 0x02) != 0;
            C = (sr & 0x01) != 0;
            I = (sr & 0x04) != 0;
            D = (sr & 0x08) != 0;
            B = (sr & 0x10) != 0;
            V = (sr & 0x40) != 0;
            N = (sr & 0x80) != 0;
        }

        public Boolean getN() { return N; }
        public Boolean getC() { return C; }
        public Boolean getD() { return D; }
        public Boolean getZ() { return Z; }
        public Boolean getV() { return V; }
        public Boolean getI() { return I; }
        public Boolean getB() { return B; }

        public void setN(Boolean f) { N = f; }
        public void setC(Boolean f) { C = f; }
        public void setD(Boolean f) { D = f; }
        public void setZ(Boolean f) { Z = f; }
        public void setV(Boolean f) { V = f; }
        public void setI(Boolean f) { I = f; }
        public void setB(Boolean f) { B = f; }




    }
