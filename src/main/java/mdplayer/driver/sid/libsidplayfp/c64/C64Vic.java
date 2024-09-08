/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.c64;

import mdplayer.driver.sid.libsidplayfp.c64.banks.IBank;
import mdplayer.driver.sid.libsidplayfp.c64.vic_ii.Mos656X;
import mdplayer.driver.sid.libsidplayfp.SidEndian;


/**
 * VIC-II
 *
 * Located at $D000-$D3FF
 */
public class C64Vic extends Mos656X implements IBank {

    // The VIC emulation instanceof very generic and here we need to effectively
    // wire it into the computer (like adding a chips to a PCB).

    private final C64Env env;

    @Override
    protected void interrupt(boolean state) {
        env.interruptIRQ(state);
    }

    @Override
    protected void setBA(boolean state) {
        env.setBA(state);
    }

    public C64Vic(C64Env env) {
        super(env.scheduler());
        this.env = env;
    }

    @Override
    public void poke(int address, byte value) {
        write(SidEndian.to16lo8((short) address), value);
    }

    @Override
    public byte peek(int address) {
        return read(SidEndian.to16lo8((short) address));
    }
}
