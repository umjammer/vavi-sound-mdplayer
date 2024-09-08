/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2013-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp.c64;

import mdplayer.driver.sid.libsidplayfp.c64.banks.IBank;

/**
 * Sid interface.
 */
public class C64Sid implements IBank {

    public byte read(byte addr) {
        return 0;
    }

    public void write(byte addr, byte data) {
    }

    public void reset(byte volume) {
    }

    public void reset() {
        reset((byte) 0);
    }

    // Bank functions
    @Override
    public void poke(int address, byte value) {
        write((byte) (address & 0x1f), value);
    }

    @Override
    public byte peek(int address) {
        return read((byte) (address & 0x1f));
    }
}
