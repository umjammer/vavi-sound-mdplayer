/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 *  Copyright (C) 2012-2016 Leandro Nini
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mdplayer.driver.sid.libsidplayfp.c64;

import mdplayer.driver.sid.libsidplayfp.c64.cpu.Mos6510;


public class C64Cpu extends Mos6510 {

    private final C64Env env;

    @Override
    protected byte cpuRead(short addr) {
        return env.cpuRead(addr);
    }

    @Override
    protected void writeCpu(short addr, byte data) {
        env.cpuWrite(addr, data);
    }

    public C64Cpu(C64Env env) {
        super(env.scheduler());
        this.env = env;
    }

// # if PC64_TESTSUITE
//@Override public void loadFile(String file)  { m_env.loadFile(file); }
// #endif
}
