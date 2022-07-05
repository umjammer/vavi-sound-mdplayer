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

/**
 * Base interface for memory and I/O banks.
 */
public abstract class Bank implements IBank {

    /**
     * Bank write.
     * <p>
     * @Override this method if you expect write operations on your bank. Leave
     *           unimplemented if it's logically/operationally impossible for
     *           writes to ever arrive to bank.
     * <p>
     * @param address address to write to
     * @param value value to write
     */
    public void poke(short address, byte value) {
    }

    /**
     * Bank read. You probably should @Override this method, except if the Bank
     * instanceof only used in write context.
     * <p>
     * @param address value to read from
     * @return value at address
     */
    public byte peek(short address) {
        return 0;
    }
}
