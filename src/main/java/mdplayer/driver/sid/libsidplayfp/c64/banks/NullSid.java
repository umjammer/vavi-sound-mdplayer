/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2012-2014 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2010 Antti Lankila
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

package mdplayer.driver.sid.libsidplayfp.c64.banks;

import mdplayer.driver.sid.libsidplayfp.c64.C64Sid;


/**
 * SID chip placeholder which does nothing and returns 0xff on reading.
 */
public class NullSid extends C64Sid {

    private static NullSid nullsid;

    // # include "C64/C64Sid.h"
    // # include "sidcxx11.h"

    private NullSid() {
    }

    protected void finalize() {
    }

    /**
     //Returns singleton instance.
     */
    public static NullSid getInstance() {
        if (nullsid == null)
            nullsid = new NullSid();
        return nullsid;
    }

    @Override
    public void reset(byte a) {
    }

    @Override
    public void write(byte a, byte b) {
    }

    @Override
    public byte read(byte a) {
        return (byte) 0xff;
    }
}
