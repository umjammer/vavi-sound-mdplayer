/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2013 Leandro Nini <drfiemost@users.sourceforge.net>
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

public class md5Gcrypt extends iMd5 {

    // private gcry_md_hd_t hd;

    public md5Gcrypt() {
        // if (gcry_check_version(GCRYPT_VERSION) == 0)
        // throw md5Error();

        // Disable secure memory.
        // if (gcry_control(GCRYCTL_DISABLE_SECMEM, 0) != 0)
        // throw md5Error();

        // Tell Libgcrypt that initialization has completed.
        // if (gcry_control(GCRYCTL_INITIALIZATION_FINISHED, 0) != 0)
        // throw md5Error();

        // if (gcry_md_open(&hd, GCRY_MD_MD5, 0) != 0)
        // throw md5Error();
    }

    protected void finalize() {
        // gcry_md_close(hd);

    }

    @Override
    public void append(byte[] data, int nbytes) {
        // gcry_md_write(hd, data, nbytes);
    }

    @Override
    public void finish() {
        // gcry_md_final(hd);
    }

    @Override
    public byte[] getDigest() {
        // return gcry_md_read(hd, 0);
        return null;
    }

    @Override
    public void reset() {
        // gcry_md_reset(hd);
    }
}
