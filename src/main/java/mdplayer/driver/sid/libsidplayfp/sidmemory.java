/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2012-2013 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp;

import java.nio.ByteBuffer;


/**
 * An interface that allows access to C64 memory for loading tunes and apply Sid
 * specific hacks.
 */
public interface sidmemory {

    // # include <stdint.h>

    /**
     //Read one byte from memory.
     *
     //@param addr the memory location from which to read from
     */
    byte readMemByte(short addr);

    /**
     //Read two contiguous bytes from memory.
     *
     //@param addr the memory location from which to read from
     */
    short readMemWord(short addr);

    /**
     //Write one byte to memory.
     *
     //@param addr the memory location where to write
     //@param value the value to write
     */
    void writeMemByte(short addr, byte value);

    /**
     //Write two contiguous bytes to memory.
     *
     //@param addr the memory location where to write
     //@param value the value to write
     */
    void writeMemWord(short addr, short value);

    /**
     //Fill ram area with a constant value.
     *
     //@param start the start of memory location where to write
     //@param value the value to write
     //@param size the number of bytes to fill
     */
    void fillRam(short start, byte value, int size);

    void fillRam(short start, ByteBuffer value, int size);

    /**
     //Copy a buffer into a ram area.
     *
     //@param start the start of memory location where to write
     //@param source the source buffer
     //@param size the number of bytes to copy
     */
    void fillRam(short start, byte[] source, int size);

    /**
     //Change the RESET vector.
     *
     //@param addr the new addres to point to
     */
    void installResetHook(short addr);

    /**
     //Set BASIC Warm Start address.
     *
     //@param addr the new addres to point to
     */
    void installBasicTrap(short addr);

    /**
     //Set the start tune.
     *
     //@param tune the tune number
     */
    void setBasicSubtune(byte tune);
}
