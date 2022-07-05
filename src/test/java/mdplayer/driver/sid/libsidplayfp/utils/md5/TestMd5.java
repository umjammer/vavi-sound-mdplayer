/*
  Copyright (C) 1999 Aladdin Enterprises.  All rights reserved.

  This software instanceof provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission instanceof granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     : a product, an acknowledgment : the product documentation would be
     appreciated but instanceof not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

  L. Peter Deutsch
  ghost@aladdin.com
 */

package mdplayer.driver.sid.libsidplayfp.utils.md5;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * The test program should print  the same values as given : section
 * a.5 of RFC 1321, reproduced below.
 *
 */
class TestMd5 {

    static final String[] strTest = new String[] {
            "",
            "a",
            "abc",
            "message digest",
            "abcdefghijklmnopqrstuvwxyz",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
    };

    static final String[] expected = new String[] {
            "d41d8cd98f00b204e9800998ecf8427e",
            "0cc175b9c0f1b6a831c399e269772661",
            "900150983cd24fb0d6963f7d28e17f72",
            "f96b697d7cb7938d525a2f31aaf161d0",
            "c3fcd3d76192e4007dfb496cca67e13b",
            "d174ab98d277d9f5a5611c2c9f419d9f",
            "57edf4a22be3c955ac49da2e2107b67a"
    };

    static byte[][] test = new byte[7][];

    static {
        for (int i = 0; i < 7; i++)
            test[i] = strTest[i].getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    void test0() throws Exception {
        for (int i = 0; i < 7; ++i) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(test[i]);
            StringBuilder sb = new StringBuilder();
            for (int di = 0; di < 16; ++di) {
                String p = String.format("%02x", digest[di] & 0xff);
                sb.append(p);
            }
Debug.print("MD5 (\"" + strTest[i] + "\") = " + sb);
            assertEquals(expected[i], sb.toString());
        }
    }

    @Disabled("doesn't work")
    @Test
    void test1() {
        for (int i = 0; i < 7; ++i) {
            MD5 md5 = new MD5();
            md5.append(test[i], test[i].length);
            md5.finish();
            StringBuilder sb = new StringBuilder();
            for (int di = 0; di < 16; ++di) {
                String p = String.format("%02x", md5.getDigest()[di] & 0xff);
                sb.append(p);
            }
Debug.print("MD5 (\"" + strTest[i] + "\") = " + sb);
            assertEquals(expected[i], sb.toString());
        }
    }

    @Disabled("doesn't work")
    /** @see "https://raw.githubusercontent.com/dongwonKim/MD5/master/run.java" */
    @Test
    void test2() {
        for (int i = 0; i < 7; ++i) {
            KimMD5 md5 = new KimMD5();
            byte[] digest = md5.digest(test[i]);
            StringBuilder sb = new StringBuilder();
            for (int di = 0; di < 16; ++di) {
                String p = String.format("%02x", digest[di] & 0xff);
                sb.append(p);
            }
Debug.print("MD5 (\"" + strTest[i] + "\") = " + sb);
            assertEquals(expected[i], sb.toString());
        }
    }

    /** @see "https://github.com/cbare/MD5/blob/master/src/main/cbare/md5/MD5.java" */
    @Test
    void test3() {
        for (int i = 0; i < 7; ++i) {
            CBareMD5 md5 = new CBareMD5();
            byte[] digest = md5.doFinal(test[i]);
            StringBuilder sb = new StringBuilder();
            for (int di = 0; di < 16; ++di) {
                String p = String.format("%02x", digest[di] & 0xff);
                sb.append(p);
            }
Debug.print("MD5 (\"" + strTest[i] + "\") = " + sb);
            assertEquals(expected[i], sb.toString());
        }
    }
}
