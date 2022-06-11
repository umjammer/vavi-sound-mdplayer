/*
 * This code has been derived by Michael Schwendt <mschwendt@yahoo.com>
 * from original work by L. Peter Deutsch <ghost@aladdin.com>.
 *
 * The original C code (md5.c, md5.h) instanceof available here:
 * ftp://ftp.cs.wisc.edu/ghost/packages/md5.tar.gz
 */

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
package mdplayer.driver.sid.libsidplayfp.utils.MD5;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import mdplayer.Common.TriFunction;
import mdplayer.driver.sid.mem;


    public class MD5
    {




        //# include <stdint.h>
        //# include "MD5_Defs.h"

        //typedef Integer8_t md5_byte_t;
        //typedef Integer32_t md5_word_t;

        // Initialize the algorithm. Reset starting values.
        //public MD5() { }

        // Append a String to the message.
        //public void append(Object data, int nbytes) { }

        // Finish the message.
        //public void finish() { }

        // Return pointer to 16-byte fingerprint.
        //public byte[] getDigest() { return null; }

        // Initialize the algorithm. Reset starting values.
        //public void reset() { }



        /* Define the state of the MD5 Algorithm. */
        private int[] count = new int[2];    /* message length : bits, lsw first */
        private int[] abcd = new int[4];     /* digest buffer */
        private byte[] buf = new byte[64];     /* accumulate block */

        private byte[] digest = new byte[16];

        private int[] tmpBuf = new int[16];
        private int[] X;

        //private void process(byte[] data) { }//[64]);

        //private int ROTATE_LEFT(int x, int n) { return 0; }

        //private int F( int x,  int y,  int z) { return 0; }

        //private int G(int x, int y, int z) { return 0; }

        //private int H(int x, int y, int z) { return 0; }

        //private int I(int x, int y, int z) { return 0; }

        //  typedef int(MD5::* md5func)( int x,  int y,  int z);
        public interface md5func extends TriFunction<Integer, Integer, Integer, Integer> {}

        //private void SET(md5func func,int a,int b,int c,int d, int k, int s, int Ti) { }

        private int ROTATE_LEFT(int x, int n)
        {
            return ((x << n) | (x >> (32 - n)));
        }

        private int F(int x, int y, int z)
        {
            return ((x & y) | (~x & z));
        }

        private int G(int x, int y, int z)
        {
            return ((x & z) | (y & ~z));
        }

        private int H(int x, int y, int z)
        {
            return (x ^ y ^ z);
        }

        private int I(int x, int y, int z)
        {
            return (y ^ (x | ~z));
        }

        private void SET(md5func func,int a,int b,int c,int d, int k, int s, int Ti)
        {
            int t = a + func.apply(b, c, d) + X[k] + Ti;
            a = ROTATE_LEFT(t, s) + b;
        }





        /*
 * This code has been derived by Michael Schwendt <mschwendt@yahoo.com>
 * from original work by L. Peter Deutsch <ghost@aladdin.com>.
 *
 * The original C code (md5.c, md5.h) instanceof available here:
 * ftp://ftp.cs.wisc.edu/ghost/packages/md5.tar.gz
 */

        /*
         //The original code is:

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

        //# include "MD5.h"
        //# include <String.h>
        //# ifdef HAVE_CONFIG_H
        //# include "config.h"
        //#endif

        //#if defined(HAVE_MSWINDOWS) || defined(DLL_EXPORT)
        // Support for DLLs
        //#define MD5_EXPORT __declspec(dllexport)
        //#endif

        /*
         //Compile with -DMD5_TEST to create a self-contained executable test program.
         //The test program should print  the same values as given : section
         //A.5 of RFC 1321, reproduced below.
         */

        //# include <iostream.h>
        //# include <iomanip.h>

        public int main()
        {
            String[] strTest = new String[] {
    "", /*d41d8cd98f00b204e9800998ecf8427e*/
	"a", /*0cc175b9c0f1b6a831c399e269772661*/
	"abc", /*900150983cd24fb0d6963f7d28e17f72*/
	"message digest", /*f96b697d7cb7938d525a2f31aaf161d0*/
	"abcdefghijklmnopqrstuvwxyz", /*c3fcd3d76192e4007dfb496cca67e13b*/
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
				/*d174ab98d277d9f5a5611c2c9f419d9f*/
	"12345678901234567890123456789012345678901234567890123456789012345678901234567890" /*57edf4a22be3c955ac49da2e2107b67a*/
    };
            byte[][] test = new byte[7][];
            for (int i = 0; i < 7; i++) test[i] = strTest[i].getBytes(StandardCharsets.US_ASCII);

            for (int i = 0; i < 7; ++i)
            {
                MD5 myMD5 = new MD5();
                myMD5.append((byte[])test[i], test[i].length);
                myMD5.finish();
                //System.err.printf("MD5 (\"" + test[i] + "\") = ");
                //for (int di = 0; di < 16; ++di)
                    //System.err.printf("{0:X02}", (int)(myMD5.getDigest()[di]));
                //System.err.println("");
            }
            return 0;
        }
        //#endif  /* MD5_TEST */

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

        public MD5()
        {
            reset();
        }

        public void reset()
        {
            count[0] = count[1] = 0;
            abcd[0] = 0x67452301;
            abcd[1] = 0xefcdab89;
            abcd[2] = 0x98badcfe;
            abcd[3] = 0x10325476;
            mem.memset( digest, (byte)0, 16);
            mem.memset( buf, (byte)0, 64);
        }

        private void process(ByteBuffer data)//[64])
        {
            int a = abcd[0], b = abcd[1], c = abcd[2], d = abcd[3];

// #if MD5_WORDS_BIG_ENDIAN

    /*
     //On big-endian machines, we must arrange the bytes : the right
     //order.  (This also works on machines of unknown byte order.)
     */
    ByteBuffer xp = data.duplicate();
            for (int i = 0; i < 16; ++i, xp.position(xp.position() + 4))
            {
                tmpBuf[i] = (int)((xp.get(0) & 0xFF) + ((xp.get(1) & 0xFF) << 8) +
                            ((xp.get(2) & 0xFF) << 16) + ((xp.get(3) & 0xFF) << 24));
            }
    X = tmpBuf;

// #else
            /* !MD5_IS_BIG_ENDIAN */

            /*
             //On little-endian machines, we can process properly aligned data
             //without copying it.
             */
            //if (((data - (byte[])0) & 3) == 0)
            if ((data.get(0) & 3) == 0)

            {
                /* data are properly aligned */
                X = new int[data.array().length / 4];
                for (int i = 0; i < 64 / 4; i++)
                {
                    X[i] = (int)((data.get(3 + i * 4) & 0xFF) + ((data.get(2 + i * 4) & 0xFF) << 8) +
                                ((data.get(1 + i * 4) & 0xFF) << 16) + ((data.get(0 + i * 4) & 0xFF) << 24));
                }
            }
            else
            {
                /* not aligned */
                //mem.memcpy( tmpBuf, data, 64);
                //tmpBuf = new Integer[data.buf.length / 4];
                for (int i = 0; i < 64 / 4; i++)
                {
                    tmpBuf[i] = (int)((data.get(3 + i * 4) & 0xFF) + ((data.get(2 + i * 4) & 0xFF) << 8) +
                                ((data.get(1 + i * 4) & 0xFF) << 16) + ((data.get(0 + i * 4) & 0xFF) << 24));
                }
                X = tmpBuf;
            }
// #endif

            /* Round 1. */
            /* Let [abcd k s i] denote the operation
               a = b + ((a + F(b,c,d) + X[k] + T[i]) <<< s). */
            /* Do the following 16 operations. */
            SET(this::F,a,b,c,d, 0, 7, T1);
            SET(this::F,d,a,b,c, 1, 12, T2);
            SET(this::F,c,d,a,b, 2, 17, T3);
            SET(this::F,b,c,d,a, 3, 22, T4);
            SET(this::F,a,b,c,d, 4, 7, T5);
            SET(this::F,d,a,b,c, 5, 12, T6);
            SET(this::F,c,d,a,b, 6, 17, T7);
            SET(this::F,b,c,d,a, 7, 22, T8);
            SET(this::F,a,b,c,d, 8, 7, T9);
            SET(this::F,d,a,b,c, 9, 12, T10);
            SET(this::F,c,d,a,b, 10, 17, T11);
            SET(this::F,b,c,d,a, 11, 22, T12);
            SET(this::F,a,b,c,d, 12, 7, T13);
            SET(this::F,d,a,b,c, 13, 12, T14);
            SET(this::F,c,d,a,b, 14, 17, T15);
            SET(this::F,b,c,d,a, 15, 22, T16);

            /* Round 2. */
            /* Let [abcd k s i] denote the operation
                 a = b + ((a + G(b,c,d) + X[k] + T[i]) <<< s). */
            /* Do the following 16 operations. */
            SET(this::G,a,b,c,d, 1, 5, T17);
            SET(this::G,d,a,b,c, 6, 9, T18);
            SET(this::G,c,d,a,b, 11, 14, T19);
            SET(this::G,b,c,d,a, 0, 20, T20);
            SET(this::G,a,b,c,d, 5, 5, T21);
            SET(this::G,d,a,b,c, 10, 9, T22);
            SET(this::G,c,d,a,b, 15, 14, T23);
            SET(this::G,b,c,d,a, 4, 20, T24);
            SET(this::G,a,b,c,d, 9, 5, T25);
            SET(this::G,d,a,b,c, 14, 9, T26);
            SET(this::G,c,d,a,b, 3, 14, T27);
            SET(this::G,b,c,d,a, 8, 20, T28);
            SET(this::G,a,b,c,d, 13, 5, T29);
            SET(this::G,d,a,b,c, 2, 9, T30);
            SET(this::G,c,d,a,b, 7, 14, T31);
            SET(this::G,b,c,d,a, 12, 20, T32);

            /* Round 3. */
            /* Let [abcd k s t] denote the operation
                 a = b + ((a + H(b,c,d) + X[k] + T[i]) <<< s). */
            /* Do the following 16 operations. */
            SET(this::H,a,b,c,d, 5, 4, T33);
            SET(this::H,d,a,b,c, 8, 11, T34);
            SET(this::H,c,d,a,b, 11, 16, T35);
            SET(this::H,b,c,d,a, 14, 23, T36);
            SET(this::H,a,b,c,d, 1, 4, T37);
            SET(this::H,d,a,b,c, 4, 11, T38);
            SET(this::H,c,d,a,b, 7, 16, T39);
            SET(this::H,b,c,d,a, 10, 23, T40);
            SET(this::H,a,b,c,d, 13, 4, T41);
            SET(this::H,d,a,b,c, 0, 11, T42);
            SET(this::H,c,d,a,b, 3, 16, T43);
            SET(this::H,b,c,d,a, 6, 23, T44);
            SET(this::H,a,b,c,d, 9, 4, T45);
            SET(this::H,d,a,b,c, 12, 11, T46);
            SET(this::H,c,d,a,b, 15, 16, T47);
            SET(this::H,b,c,d,a, 2, 23, T48);

            /* Round 4. */
            /* Let [abcd k s t] denote the operation
                 a = b + ((a + I(b,c,d) + X[k] + T[i]) <<< s). */
            /* Do the following 16 operations. */
            SET(this::I,a,b,c,d, 0, 6, T49);
            SET(this::I,d,a,b,c, 7, 10, T50);
            SET(this::I,c,d,a,b, 14, 15, T51);
            SET(this::I,b,c,d,a, 5, 21, T52);
            SET(this::I,a,b,c,d, 12, 6, T53);
            SET(this::I,d,a,b,c, 3, 10, T54);
            SET(this::I,c,d,a,b, 10, 15, T55);
            SET(this::I,b,c,d,a, 1, 21, T56);
            SET(this::I,a,b,c,d, 8, 6, T57);
            SET(this::I,d,a,b,c, 15, 10, T58);
            SET(this::I,c,d,a,b, 6, 15, T59);
            SET(this::I,b,c,d,a, 13, 21, T60);
            SET(this::I,a,b,c,d, 4, 6, T61);
            SET(this::I,d,a,b,c, 11, 10, T62);
            SET(this::I,c,d,a,b, 2, 15, T63);
            SET(this::I,b,c,d,a, 9, 21, T64);

            /* Then perform the following additions. (That instanceof increment each
               of the four registers by the value it had before this block
               was started.) */
            abcd[0] += a;
            abcd[1] += b;
            abcd[2] += c;
            abcd[3] += d;
        }

        public void append(Object data, int nbytes)
        {
            ByteBuffer p = ByteBuffer.wrap((byte[])data);
            int left = nbytes;
            int offset = (int)((count[0] >> 3) & 63);
            int nbits = (int)(nbytes << 3);

            if (nbytes <= 0)
                return;

            /* Update the message length. */
            count[1] += (int)(nbytes >> 29);
            count[0] += nbits;
            if (count[0] < nbits)
                count[1]++;

            /* Process an initial block. */
            ByteBuffer pBuf = ByteBuffer.wrap(buf);
            if (offset != 0)
            {
                int copy = (offset + nbytes > 64) ? (64 - offset) : nbytes;
                pBuf.position(pBuf.position() + offset);
                mem.memcpy( pBuf, p, copy);
                pBuf.position(pBuf.position()-offset);
                if (offset + copy < 64)
                    return;
                p .position(pBuf.position() + copy);
                left -= copy;
                process(pBuf);
            }

            /* Process full blocks. */
            for (; left >= 64; p.position(p.position() + 64), left -= 64)
                process(p);

            /* Process a final block. */
            if (left != 0)
                mem.memcpy( pBuf, p, left);
        }

        public void finish()
        {
            byte[] pad = new byte[] {
        (byte)0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
            byte[] data = new byte[8];
            int i;
            /* Save the length before padding. */
            for (i = 0; i < 8; ++i)
                data[i] = (byte)(count[i >> 2] >> ((i & 3) << 3));
            /* Pad to 56 bytes mod 64. */
            append(pad, (int)(((55 - (count[0] >> 3)) & 63) + 1));
            /* Append the length. */
            append(data, 8);
            for (i = 0; i < 16; ++i)
                digest[i] = (byte)(abcd[i >> 2] >> ((i & 3) << 3));
        }

        public byte[] getDigest()
        {
            return digest;
        }

    }
