/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2014-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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

public class md5Factory {

    public iMd5 get() {
        return
// #if GCRYPT_WITH_MD5
                new md5Gcrypt();
// #else
//        new md5Internal();
// #endif

    }
}


