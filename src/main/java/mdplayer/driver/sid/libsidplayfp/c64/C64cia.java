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
import mdplayer.driver.sid.libsidplayfp.c64.cia.Mos6526;
import mdplayer.driver.sid.libsidplayfp.SidEndian;


/**
 * CIA 1
 * <p>
 * Generates IRQs
 * <p>
 * Located at $DC00-$DCFF
 */
class C64Cia1 extends Mos6526 implements IBank {

    // The CIA emulations are very generic and here we need to effectively
    // wire them into the computer (like adding a chips to a PCB).

    private final C64Env env;
    private short lastTA;

    @Override
    public void interrupt(boolean state) {
        env.interruptIRQ(state);
    }

    @Override
    protected void portB() {
        env.lightpen(((prb | ~ddrb) & 0x10) != 0);
    }

    public C64Cia1(C64Env env) {
        super(env.scheduler());
        this.env = env;
    }

    @Override
    public void poke(int address, byte value) {
        write(SidEndian.to16lo8((short) address), value);

        // Save the value written to Timer a
        if ((address & 0xffff) == 0xDC04 || (address & 0xffff) == 0xDC05) {
            if (timerA.getTimer() != 0)
                lastTA = timerA.getTimer();
        }
    }

    @Override
    public byte peek(int address) {
        return read(SidEndian.to16lo8((short) address));
    }

    @Override
    public void reset() {
        lastTA = 0;
        super.reset();
    }

    public short getTimerA() {
        return lastTA;
    }
}

/**
 * CIA 2
 * <p>
 * Generates NMIs
 * <p>
 * Located at $DD00-$DDFF
 */
class C64Cia2 extends Mos6526 implements IBank {

    private final C64Env env;

    @Override
    public void interrupt(boolean state) {
        if (state)
            env.interruptNMI();
    }

    public C64Cia2(C64Env env) {
        super(env.scheduler());
        this.env = env;
    }

    @Override
    public void poke(int address, byte value) {
        write((byte) address, value);
    }

    @Override
    public byte peek(int address) {
        return read((byte) address);
    }
}



