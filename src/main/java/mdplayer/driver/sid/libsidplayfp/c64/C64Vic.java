/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2001 Simon White
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

package mdplayer.driver.sid.libsidplayfp.c64;

import mdplayer.driver.sid.libsidplayfp.c64.banks.IBank;
import mdplayer.driver.sid.libsidplayfp.c64.vic_ii.MOS656X;
import mdplayer.driver.sid.libsidplayfp.sidendian;


/**
 * VIC-II
 *
 * Located at $D000-$D3FF
 */
public class C64Vic extends MOS656X implements IBank {

    // The VIC emulation instanceof very generic and here we need to effectively
    // wire it into the computer (like adding a chip to a PCB).

    private C64Env m_env;

    @Override
    protected void interrupt(Boolean state) {
        m_env.interruptIRQ(state);
    }

    @Override
    protected void setBA(Boolean state) {
        m_env.setBA(state);
    }

    public C64Vic(C64Env env) {
        super(env.scheduler());
        m_env = env;
    }

    public void poke(short address, byte value) {
        write(sidendian.endian_16lo8(address), value);
    }

    public byte peek(short address) {
        return read(sidendian.endian_16lo8(address));
    }
}
