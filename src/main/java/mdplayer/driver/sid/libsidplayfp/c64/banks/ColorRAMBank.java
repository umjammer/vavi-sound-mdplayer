/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2012-2013 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2010 Antti Lankila
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

package mdplayer.driver.sid.libsidplayfp.c64.banks;

import java.util.Arrays;


/**
 * Color RAM.
 *
 * 1K x 4-bit Static RAM that stores text screen color information.
 *
 * Located at $D800-$DBFF (last 24 bytes are unused)
 */
public class ColorRAMBank implements IBank {

    private byte[] ram = new byte[0x400];

    public void reset() {
        Arrays.fill(ram, (byte) 0);
    }

    public void poke(short address, byte value) {
        ram[address & 0x3ff] = (byte) (value & 0xf);
    }

    public byte peek(short address) {
        return ram[address & 0x3ff];
    }
}
