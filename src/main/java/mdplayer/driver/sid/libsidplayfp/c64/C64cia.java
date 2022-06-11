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
import mdplayer.driver.sid.libsidplayfp.c64.cia.MOS6526;
import mdplayer.driver.sid.libsidplayfp.sidendian;


/**
 * CIA 1
 * <p>
 * Generates IRQs
 * <p>
 * Located at $DC00-$DCFF
 */
class C64cia1 extends MOS6526 implements IBank {


    // The CIA emulations are very generic and here we need to effectively
    // wire them into the computer (like adding a chip to a PCB).

    private C64Env m_env;
    private short last_ta;

    @Override
    public void interrupt(Boolean state) {
        m_env.interruptIRQ(state);
    }

    @Override
    protected void portB() {
        m_env.lightpen(((prb | ~ddrb) & 0x10) != 0);
    }

    public C64cia1(C64Env env) {
        super(env.scheduler());
        m_env = env;
    }

    public void poke(short address, byte value) {
        write(sidendian.endian_16lo8(address), value);

        // Save the value written to Timer A
        if (address == 0xDC04 || address == 0xDC05) {
            if (timerA.getTimer() != 0)
                last_ta = timerA.getTimer();
        }
    }

    public byte peek(short address) {
        return read(sidendian.endian_16lo8(address));
    }

    @Override
    public void reset() {
        last_ta = 0;
        super.reset();
    }

    public short getTimerA() {
        return last_ta;
    }


}

/**
 * CIA 2
 * <p>
 * Generates NMIs
 * <p>
 * Located at $DD00-$DDFF
 */
class C64cia2 extends MOS6526 implements IBank {
    private C64Env m_env;

    @Override
    public void interrupt(Boolean state) {
        if (state)
            m_env.interruptNMI();
    }

    public C64cia2(C64Env env) {
        super(env.scheduler());
        m_env = env;
    }

    public void poke(short address, byte value) {
        write((byte) address, value);
    }

    public byte peek(short address) {
        return read((byte) address);
    }
}



