/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2012-2014 Leandro Nini <drfiemost@users.sourceforge.net>
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

import java.util.ArrayList;
import java.util.List;

import mdplayer.driver.sid.libsidplayfp.c64.C64Sid;


/**
 * Extra Sid bank.
 */
public class ExtraSidBank implements IBank {

    /**
     * size of mapping table. Each 32 bytes another Sid chip base address can be
     * assigned to.
     */
    private static final int MAPPER_SIZE = 8;

    /**
     * Sid mapping table. Maps a Sid chip base address to a Sid or to the
     * underlying bank.
     */
    private IBank[] mapper = new IBank[MAPPER_SIZE];

    private List<C64Sid> sids = new ArrayList<>();

    private static void resetSID(C64Sid e) {
        e.reset((byte) 0xf);
    }

    private int mapperIndex(int address) {
        return address >> 5 & (MAPPER_SIZE - 1);
    }

    public void reset() {
        for (C64Sid v : sids) {
            resetSID(v);
        }
    }

    public void resetSIDMapper(IBank bank) {
        for (int i = 0; i < MAPPER_SIZE; i++)
            mapper[i] = bank;
    }

    public byte peek(short addr) {
        return mapper[mapperIndex(addr)].peek(addr);
    }

    public void poke(short addr, byte data) {
        mapper[mapperIndex(addr)].poke(addr, data);
    }

    /**
     * Set Sid emulation.
     * <p>
     * @param s the emulation
     * @param address the address where to put the chip
     */
    public void addSID(C64Sid s, int address) {
        sids.add(s);
        mapper[mapperIndex(address)] = s;
    }
}
