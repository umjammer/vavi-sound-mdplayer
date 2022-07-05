/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.c64.cpu;

/**
 * Processor Status Register
 */
public class Flags {

    /** Carry */
    private boolean c;
    /** Zero */
    private boolean z;
    /** Interrupt disabled */
    private boolean i;
    /** Decimal */
    private boolean d;
    /** Break */
    private boolean b;
    /** Overflow */
    private boolean v;
    /** Negative */
    private boolean n;

    public void reset() {
        c = z = i = d = v = n = false;
        b = true;
    }

    /**
     * Set N and Z flag values.
     * <p>
     * @param value to set Flags from
     */
    public void setNZ(byte value) {
        z = value == 0;
        n = (value & 0x80) != 0;
    }

    /**
     * Get status register value.
     */
    public byte get() {
        byte sr = 0x20;

        if (c) sr |= 0x01;
        if (z) sr |= 0x02;
        if (i) sr |= 0x04;
        if (d) sr |= 0x08;
        if (b) sr |= 0x10;
        if (v) sr |= 0x40;
        if (n) sr |= 0x80;

        return sr;
    }

    /**
     * Set status register value.
     */
    public void set(byte sr) {
        z = (sr & 0x02) != 0;
        c = (sr & 0x01) != 0;
        i = (sr & 0x04) != 0;
        d = (sr & 0x08) != 0;
        b = (sr & 0x10) != 0;
        v = (sr & 0x40) != 0;
        n = (sr & 0x80) != 0;
    }

    public boolean getN() {
        return n;
    }

    public boolean getC() {
        return c;
    }

    public boolean getD() {
        return d;
    }

    public boolean getZ() {
        return z;
    }

    public boolean getV() {
        return v;
    }

    public boolean getI() {
        return i;
    }

    public boolean getB() {
        return b;
    }

    public void setN(boolean f) {
        n = f;
    }

    public void setC(boolean f) {
        c = f;
    }

    public void setD(boolean f) {
        d = f;
    }

    public void setZ(boolean f) {
        z = f;
    }

    public void setV(boolean f) {
        v = f;
    }

    public void setI(boolean f) {
        i = f;
    }

    public void setB(boolean f) {
        b = f;
    }
}
