/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp;

import mdplayer.driver.sid.libsidplayfp.utils.iMd5;
import mdplayer.driver.sid.libsidplayfp.utils.md5Factory;


/**
 * A wrapper around the md5 implementation that provides
 * an hex formatted digest
 */
public class sidmd5 {

    private iMd5 m_md5;

    /**
     * @throw md5Error
     */
    public sidmd5() {
        m_md5 = (new md5Factory()).get();
    }

    /**
     * Append a String to the message.
     */
    public void append(byte[] data, int nbytes) {
        m_md5.append(data, nbytes);
    }

    /**
     * Finish the message.
     */
    public void finish() {
        m_md5.finish();
    }

    /**
     * Initialize the algorithm. Reset starting values.
     */
    public void reset() {
        m_md5.reset();
    }

    /**
     * Return pointer to 32-byte hex fingerprint.
     */
    public String getDigest() {
        byte[] digest = m_md5.getDigest();
        if (digest == null)
            return "";// std::String();

        // Construct fingerprint.
        //std::oStringstream ss;
        //ss.fill('0');
        //ss.flags(std::ios::hex);
        String ss = "";

        for (int di = 0; di < 16; ++di) {
            ss += String.format("0:x02", digest[di]);
        }

        return ss;
    }
}