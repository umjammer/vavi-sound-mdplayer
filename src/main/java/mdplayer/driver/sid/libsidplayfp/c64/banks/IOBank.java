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
 * IO region handler. 4k region, 16 chips, 256b banks.
 *
 * Located at $D000-$DFFF
 *
 * @author Antti Lankila
 */
public class IOBank implements IBank {

    //# include <stdint.h>
    //# include "Bank.h"
    //# include "sidcxx11.h"

    private IBank[] map = new IBank[16];

    public void setBank(int num, IBank bank) {
        map[num] = bank;
    }

    public IBank getBank(int num) {
        return map[num];
    }

    @Override
    public byte peek(int addr) {
        return map[addr >> 8 & 0xf].peek(addr);
    }

    @Override
    public void poke(int addr, byte data) {
        // System.err.println("%d", addr >> 8 & 0xf);
        map[addr >> 8 & 0xf].poke(addr, data);
    }

}
