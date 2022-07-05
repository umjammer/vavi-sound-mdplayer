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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import dotnet4j.util.compat.TriFunction;
import mdplayer.driver.sid.Mem;


/**
 * Define the state of the MD5 Algorithm.
 *
 * @author Michael Schwendt <mschwendt@yahoo.com>
 * @author L. Peter Deutsch <ghost@aladdin.com> ... original work
 * @see "ftp://ftp.cs.wisc.edu/ghost/packages/md5.tar.gz"
 * @deprecated use {@link java.security.MessageDigest}
 */
@Deprecated
public class MD5 {

    /**
     * message length : bits, lsw first
     */
    private int[] count = new int[2];
    /**
     * digest buffer
     */
    private int[] abcd = new int[4];
    /**
     * accumulate block
     */
    private byte[] buf = new byte[64];

    private byte[] digest = new byte[16];

    private int[] tmpBuf = new int[16];
    private int[] x;

    // TODO eliminate
    private interface Md5Func extends TriFunction<Integer, Integer, Integer, Integer> {
    }

    private static int rotateLeft(int x, int n) {
        n %= 32;
        return (x << n) | (x >>> (32 - n));
    }

    private static int f(int x, int y, int z) {
        return (x & y) | (~x & z);
    }

    private static int g(int x, int y, int z) {
        return (x & z) | (y & ~z);
    }

    private static int h(int x, int y, int z) {
        return x ^ y ^ z;
    }

    private static int i(int x, int y, int z) {
        return y ^ (x | ~z);
    }

    private int set(Md5Func func, int a, int b, int c, int d, int k, int s, int Ti) {
        int t = a + func.apply(b, c, d) + x[k] + Ti;
        a = rotateLeft(t, s) + b;
        return a;
    }

    private static final int T1 = 0xd76aa478;
    private static final int T2 = 0xe8c7b756;
    private static final int T3 = 0x242070db;
    private static final int T4 = 0xc1bdceee;
    private static final int T5 = 0xf57c0faf;
    private static final int T6 = 0x4787c62a;
    private static final int T7 = 0xa8304613;
    private static final int T8 = 0xfd469501;
    private static final int T9 = 0x698098d8;
    private static final int T10 = 0x8b44f7af;
    private static final int T11 = 0xffff5bb1;
    private static final int T12 = 0x895cd7be;
    private static final int T13 = 0x6b901122;
    private static final int T14 = 0xfd987193;
    private static final int T15 = 0xa679438e;
    private static final int T16 = 0x49b40821;
    private static final int T17 = 0xf61e2562;
    private static final int T18 = 0xc040b340;
    private static final int T19 = 0x265e5a51;
    private static final int T20 = 0xe9b6c7aa;
    private static final int T21 = 0xd62f105d;
    private static final int T22 = 0x02441453;
    private static final int T23 = 0xd8a1e681;
    private static final int T24 = 0xe7d3fbc8;
    private static final int T25 = 0x21e1cde6;
    private static final int T26 = 0xc33707d6;
    private static final int T27 = 0xf4d50d87;
    private static final int T28 = 0x455a14ed;
    private static final int T29 = 0xa9e3e905;
    private static final int T30 = 0xfcefa3f8;
    private static final int T31 = 0x676f02d9;
    private static final int T32 = 0x8d2a4c8a;
    private static final int T33 = 0xfffa3942;
    private static final int T34 = 0x8771f681;
    private static final int T35 = 0x6d9d6122;
    private static final int T36 = 0xfde5380c;
    private static final int T37 = 0xa4beea44;
    private static final int T38 = 0x4bdecfa9;
    private static final int T39 = 0xf6bb4b60;
    private static final int T40 = 0xbebfbc70;
    private static final int T41 = 0x289b7ec6;
    private static final int T42 = 0xeaa127fa;
    private static final int T43 = 0xd4ef3085;
    private static final int T44 = 0x04881d05;
    private static final int T45 = 0xd9d4d039;
    private static final int T46 = 0xe6db99e5;
    private static final int T47 = 0x1fa27cf8;
    private static final int T48 = 0xc4ac5665;
    private static final int T49 = 0xf4292244;
    private static final int T50 = 0x432aff97;
    private static final int T51 = 0xab9423a7;
    private static final int T52 = 0xfc93a039;
    private static final int T53 = 0x655b59c3;
    private static final int T54 = 0x8f0ccc92;
    private static final int T55 = 0xffeff47d;
    private static final int T56 = 0x85845dd1;
    private static final int T57 = 0x6fa87e4f;
    private static final int T58 = 0xfe2ce6e0;
    private static final int T59 = 0xa3014314;
    private static final int T60 = 0x4e0811a1;
    private static final int T61 = 0xf7537e82;
    private static final int T62 = 0xbd3af235;
    private static final int T63 = 0x2ad7d2bb;
    private static final int T64 = 0xeb86d391;

    // Initialize the algorithm. Reset starting values.
    public MD5() {
        reset();
    }

    // Initialize the algorithm. Reset starting values.
    public void reset() {
        count[0] = count[1] = 0;
        abcd[0] = 0x67452301;
        abcd[1] = 0xefcdab89;
        abcd[2] = 0x98badcfe;
        abcd[3] = 0x10325476;
        Mem.memset(digest, (byte) 0, 16);
        Mem.memset(buf, (byte) 0, 64);
    }

    private void process(ByteBuffer data) {
        int a = abcd[0], b = abcd[1], c = abcd[2], d = abcd[3];

        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {

            // On big-endian machines, we must arrange the bytes : the right
            // order.  (This also works on machines of unknown byte order.)
            ByteBuffer xp = data;
            for (int i = 0; i < 16; ++i, xp.position(xp.position() + 4)) {
                tmpBuf[i] = (xp.get(0) & 0xFF) + ((xp.get(1) & 0xFF) << 8) +
                        ((xp.get(2) & 0xFF) << 16) + ((xp.get(3) & 0xFF) << 24);
            }
            x = tmpBuf;
        } else {
            // On little-endian machines, we can process properly aligned data
            // without copying it.
            if ((data.get(0) & 3) == 0) {
                /* data are properly aligned */
                x = new int[data.array().length / 4];
                for (int i = 0; i < 64 / 4; i++) {
                    x[i] = (data.get(3 + i * 4) & 0xFF) + ((data.get(2 + i * 4) & 0xFF) << 8) +
                            ((data.get(1 + i * 4) & 0xFF) << 16) + ((data.get(0 + i * 4) & 0xFF) << 24);
                }
            } else {
                /* not aligned */
                for (int i = 0; i < 64 / 4; i++) {
                    tmpBuf[i] = (data.get(3 + i * 4) & 0xFF) + ((data.get(2 + i * 4) & 0xFF) << 8) +
                            ((data.get(1 + i * 4) & 0xFF) << 16) + ((data.get(0 + i * 4) & 0xFF) << 24);
                }
                x = tmpBuf;
            }
        }

        // Round 1.
        //    Let [abcd k s i] denote the operation
        //       a = b + ((a + F(b,c,d) + x[k] + T[i]) <<< s).
        // Do the following 16 operations.
        a = set(MD5::f, a, b, c, d, 0, 7, T1);
        d = set(MD5::f, d, a, b, c, 1, 12, T2);
        c = set(MD5::f, c, d, a, b, 2, 17, T3);
        b = set(MD5::f, b, c, d, a, 3, 22, T4);
        a = set(MD5::f, a, b, c, d, 4, 7, T5);
        d = set(MD5::f, d, a, b, c, 5, 12, T6);
        c = set(MD5::f, c, d, a, b, 6, 17, T7);
        b = set(MD5::f, b, c, d, a, 7, 22, T8);
        a = set(MD5::f, a, b, c, d, 8, 7, T9);
        d = set(MD5::f, d, a, b, c, 9, 12, T10);
        c = set(MD5::f, c, d, a, b, 10, 17, T11);
        b = set(MD5::f, b, c, d, a, 11, 22, T12);
        a = set(MD5::f, a, b, c, d, 12, 7, T13);
        d = set(MD5::f, d, a, b, c, 13, 12, T14);
        c = set(MD5::f, c, d, a, b, 14, 17, T15);
        b = set(MD5::f, b, c, d, a, 15, 22, T16);

        // Round 2.
        //   Let [abcd k s i] denote the operation
        //      a = b + ((a + G(b,c,d) + x[k] + T[i]) <<< s).
        // Do the following 16 operations.
        a = set(MD5::g, a, b, c, d, 1, 5, T17);
        d = set(MD5::g, d, a, b, c, 6, 9, T18);
        c = set(MD5::g, c, d, a, b, 11, 14, T19);
        b = set(MD5::g, b, c, d, a, 0, 20, T20);
        a = set(MD5::g, a, b, c, d, 5, 5, T21);
        d = set(MD5::g, d, a, b, c, 10, 9, T22);
        c = set(MD5::g, c, d, a, b, 15, 14, T23);
        b = set(MD5::g, b, c, d, a, 4, 20, T24);
        a = set(MD5::g, a, b, c, d, 9, 5, T25);
        d = set(MD5::g, d, a, b, c, 14, 9, T26);
        c = set(MD5::g, c, d, a, b, 3, 14, T27);
        b = set(MD5::g, b, c, d, a, 8, 20, T28);
        a = set(MD5::g, a, b, c, d, 13, 5, T29);
        d = set(MD5::g, d, a, b, c, 2, 9, T30);
        c = set(MD5::g, c, d, a, b, 7, 14, T31);
        b = set(MD5::g, b, c, d, a, 12, 20, T32);

        // Round 3.
        //    Let [abcd k s t] denote the operation
        //       a = b + ((a + H(b,c,d) + x[k] + T[i]) <<< s).
        // Do the following 16 operations.
        a = set(MD5::h, a, b, c, d, 5, 4, T33);
        d = set(MD5::h, d, a, b, c, 8, 11, T34);
        c = set(MD5::h, c, d, a, b, 11, 16, T35);
        b = set(MD5::h, b, c, d, a, 14, 23, T36);
        a = set(MD5::h, a, b, c, d, 1, 4, T37);
        d = set(MD5::h, d, a, b, c, 4, 11, T38);
        c = set(MD5::h, c, d, a, b, 7, 16, T39);
        b = set(MD5::h, b, c, d, a, 10, 23, T40);
        a = set(MD5::h, a, b, c, d, 13, 4, T41);
        d = set(MD5::h, d, a, b, c, 0, 11, T42);
        c = set(MD5::h, c, d, a, b, 3, 16, T43);
        b = set(MD5::h, b, c, d, a, 6, 23, T44);
        a = set(MD5::h, a, b, c, d, 9, 4, T45);
        d = set(MD5::h, d, a, b, c, 12, 11, T46);
        c = set(MD5::h, c, d, a, b, 15, 16, T47);
        b = set(MD5::h, b, c, d, a, 2, 23, T48);

        // Round 4.
        //    Let [abcd k s t] denote the operation
        //       a = b + ((a + I(b,c,d) + x[k] + T[i]) <<< s).
        // Do the following 16 operations.
        a = set(MD5::i, a, b, c, d, 0, 6, T49);
        d = set(MD5::i, d, a, b, c, 7, 10, T50);
        c = set(MD5::i, c, d, a, b, 14, 15, T51);
        b = set(MD5::i, b, c, d, a, 5, 21, T52);
        a = set(MD5::i, a, b, c, d, 12, 6, T53);
        d = set(MD5::i, d, a, b, c, 3, 10, T54);
        c = set(MD5::i, c, d, a, b, 10, 15, T55);
        b = set(MD5::i, b, c, d, a, 1, 21, T56);
        a = set(MD5::i, a, b, c, d, 8, 6, T57);
        d = set(MD5::i, d, a, b, c, 15, 10, T58);
        c = set(MD5::i, c, d, a, b, 6, 15, T59);
        b = set(MD5::i, b, c, d, a, 13, 21, T60);
        a = set(MD5::i, a, b, c, d, 4, 6, T61);
        d = set(MD5::i, d, a, b, c, 11, 10, T62);
        c = set(MD5::i, c, d, a, b, 2, 15, T63);
        b = set(MD5::i, b, c, d, a, 9, 21, T64);

        // Then perform the following additions. (That instanceof increment each
        // of the four registers by the value it had before this block
        // was started.)
        abcd[0] += a;
        abcd[1] += b;
        abcd[2] += c;
        abcd[3] += d;
    }

    /** Append a String to the message. */
    public void append(Object data, int nBytes) {
        ByteBuffer p = ByteBuffer.wrap((byte[]) data);
        int left = nBytes;
        int offset = (count[0] >> 3) & 63;
        int nBits = nBytes << 3;

        if (nBytes <= 0)
            return;

        /* Update the message length. */
        count[1] += nBytes >> 29;
        count[0] += nBits;
        if (count[0] < nBits)
            count[1]++;

        /* Process an initial block. */
        ByteBuffer pBuf = ByteBuffer.wrap(buf);
        if (offset != 0) {
            int copy = (offset + nBytes > 64) ? (64 - offset) : nBytes;
            pBuf.position(pBuf.position() + offset);
            Mem.memcpy(pBuf, p, copy);
            pBuf.position(pBuf.position() - offset);
            if (offset + copy < 64)
                return;
            p.position(pBuf.position() + copy);
            left -= copy;
            process(pBuf);
        }

        /* Process full blocks. */
        for (; left >= 64; p.position(p.position() + 64), left -= 64)
            process(p);

        /* Process a final block. */
        if (left != 0)
            Mem.memcpy(pBuf, p, left);
    }

    /** Finish the message. */
    public void finish() {
        byte[] pad = new byte[] {
                (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        byte[] data = new byte[8];
        /* Save the length before padding. */
        for (int i = 0; i < 8; ++i)
            data[i] = (byte) ((count[i >> 2] >> ((i & 3) << 3)) & 0xff);
        /* Pad to 56 bytes mod 64. */
        append(pad, ((55 - (count[0] >> 3)) & 63) + 1);
        /* Append the length. */
        append(data, 8);
        for (int i = 0; i < 16; ++i)
            digest[i] = (byte) ((abcd[i >> 2] >> ((i & 3) << 3)) & 0xff);
    }

    /** Return pointer to 16-byte fingerprint. */
    public byte[] getDigest() {
        return digest;
    }
}
