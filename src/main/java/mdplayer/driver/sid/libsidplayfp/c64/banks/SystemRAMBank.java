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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.c64.banks;

import java.util.Arrays;


/**
 * Area backed by RAM.
 *
 * @author Antti Lankila
 */
public class SystemRAMBank implements IBank {

    // C64 RAM area
    public byte[] ram = new byte[0x10000];

    /**
     //Initialize RAM with powerup pattern.
     */
    public void reset() {
        Arrays.fill(ram, (byte) 0);
        for (int i = 0x40; i < 0x10000; i += 0x80) {
            for (int j = 0; j < 0x40; j++)
                ram[i + j] = (byte) 0xff;
        }
    }

    public byte peek(short address) {
        return ram[address];
    }

    public void poke(short address, byte value) {
        ram[address] = value;
    }
}
