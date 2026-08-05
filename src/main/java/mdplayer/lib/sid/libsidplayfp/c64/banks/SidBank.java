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

package mdplayer.lib.sid.libsidplayfp.c64.banks;

import mdplayer.lib.sid.libsidplayfp.c64.C64Sid;


/**
 * Sid
 *
 * Located at $D400-$D7FF, mirrored each 32 bytes
 */
public class SidBank implements IBank {

    /** what a tune's writes to this bank are reported to, the way a chiptune player watches registers */
    public interface WriteListener {
        void write(int addr, int data);
    }

    // Sid chips
    private C64Sid sid;

    private WriteListener writeListener;

    public SidBank() {
        sid = NullSid.getInstance();

    }

    /**
     * Listen in on what the tune writes here - a Sid never signals its end, so the only thing
     * telling a player that the song came around again is the register writes repeating.
     *
     * @param listener null to stop listening
     */
    public void setWriteListener(WriteListener listener) {
        writeListener = listener;
    }

    public void reset() {
        sid.reset((byte) 0xf);
    }

    @Override
    public byte peek(int addr) {
        return sid.peek(addr);
    }

    @Override
    public void poke(int addr, byte data) {
        sid.poke(addr, data);
        if (writeListener != null)
            writeListener.write(addr & 0x1f, data & 0xff);
    }

    /**
     * Set Sid emulation.
     *
     * @param s the emulation, nullptr to remove current Sid
     */
    public void setSID(C64Sid s) {
        sid = (s != null) ? s : NullSid.getInstance();
    }
}
