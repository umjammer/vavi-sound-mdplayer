/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2014 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp.utils;

import mdplayer.driver.sid.libsidplayfp.utils.md5.MD5;


public class PrivateMd5 implements IMd5 {

    private MD5 hd = new MD5();

    @Override
    public void append(byte[] data, int nbytes) {
        hd.append(data, nbytes);
    }

    @Override
    public void finish() {
        hd.finish();
    }

    @Override
    public byte[] getDigest() {
        return hd.getDigest();
    }

    @Override
    public void reset() {
        hd.reset();
    }

}
