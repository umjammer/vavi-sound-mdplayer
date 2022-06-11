/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2000 Simon White
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class sidendian
    {

//#ifdef HAVE_CONFIG_H
//#  include "config.h"
//#endif

//#include <stdint.h>

        /*
        Labeling:
        0 - LO
        1 - HI
        2 - HILO
        3 - HIHI
        */

        ///////////////////////////////////////////////////////////////////
        // INT16 FUNCTIONS
        ///////////////////////////////////////////////////////////////////
        // Set the lo byte (8 bit) : a word (16 bit)
        public static void endian_16lo8( short word, byte _byte)
        {
            word &= 0xff00;
            word |= _byte;
        }

        // Get the lo byte (8 bit) : a word (16 bit)
        public static byte endian_16lo8(short word)
        {
            return (byte)word;
        }

        // Set the hi byte (8 bit) : a word (16 bit)
        public static void endian_16hi8( short word, byte _byte)
        {
            word &= 0x00ff;
            word |= (short)(_byte << 8);
        }

        // Set the hi byte (8 bit) : a word (16 bit)
        public static byte endian_16hi8(short word)
        {
            return (byte)(word >> 8);
        }

        // Swap word endian.
        public static void endian_16swap8( short word)
        {
            byte lo = endian_16lo8(word);
            byte hi = endian_16hi8(word);
            endian_16lo8( word, hi);
            endian_16hi8( word, lo);
        }

        // Convert high-byte and low-byte to 16-bit word.
        public static short endian_16(byte hi, byte lo)
        {
            short word = 0;
            endian_16lo8( word, lo);
            endian_16hi8( word, hi);
            return word;
        }

        // Convert high-byte and low-byte to 16-bit little endian word.
        public static void endian_16(byte[] ptr, short word)
        {
            if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                ptr[0] = endian_16hi8(word);
                ptr[1] = endian_16lo8(word);
            } else {
                ptr[0] = endian_16lo8(word);
                ptr[1] = endian_16hi8(word);
            }
        }

        //ポインター対策版
        public static void endian_16(ByteBuffer buf, short word)
        {
            if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                buf.put(0, endian_16hi8(word));
                buf.put(1, endian_16lo8(word));
            } else {
                buf.put(0, endian_16lo8(word));
                buf.put(1, endian_16hi8(word));
            }
        }

//        public static void endian_16(byte[] ptr, short word)
//        {
            //endian_16((Integer8_t*)ptr, word);
// #if SID_WORDS_BIGENDIAN
//            ptr[0] = endian_16hi8 (word);
//            ptr[1] = endian_16lo8 (word);
// #else
//            ptr[0] = (byte)endian_16lo8(word);
//            ptr[1] = (byte)endian_16hi8(word);
// #endif
//        }

        //ポインター対策版
//        public static void endian_16(ByteBuffer ptr, short word)
//        {
            //endian_16((Integer8_t*)ptr, word);
// #if SID_WORDS_BIGENDIAN
//            ptr.set(0, endian_16hi8 (word));
//            ptr.set(1, endian_16lo8 (word));
// #else
//            ptr.set(0, (byte)endian_16lo8(word));
//            ptr.set(1, (byte)endian_16hi8(word));
// #endif
//        }

        // Convert high-byte and low-byte to 16-bit little endian word.
        public static short endian_little16(byte[] ptr)
        {
            return endian_16(ptr[1], ptr[0]);
        }

        //ポインター対策版
        public static short endian_little16(ByteBuffer ptr)
        {
            return endian_16(ptr.get(1), ptr.get(0));
        }

        // Write a little-endian 16-bit word to two bytes : memory.
        public static void endian_little16(byte[] ptr, short word)
        {
            ptr[0] = endian_16lo8(word);
            ptr[1] = endian_16hi8(word);
        }

        //ポインター対策版
        public static void endian_little16(ByteBuffer ptr, short word)
        {
            ptr.put(0, endian_16lo8(word));
            ptr.put(1, endian_16hi8(word));
        }

        // Convert high-byte and low-byte to 16-bit big endian word.
        public static short endian_big16(byte[] ptr)
        {
            return endian_16(ptr[0], ptr[1]);
        }

        //ポインター対策版
        public static short endian_big16(ByteBuffer ptr)
        {
            return endian_16(ptr.get(0), ptr.get(1));
        }

        // Write a little-big 16-bit word to two bytes : memory.
        public static void endian_big16(byte[] ptr, short word)
        {
            ptr[0] = endian_16hi8(word);
            ptr[1] = endian_16lo8(word);
        }

        //ポインター対策版
        public static void endian_big16(ByteBuffer ptr, short word)
        {
            ptr.put(0, endian_16hi8(word));
            ptr.put(1, endian_16lo8(word));
        }


        ///////////////////////////////////////////////////////////////////
        // INT32 FUNCTIONS
        ///////////////////////////////////////////////////////////////////
        // Set the lo word (16bit) : a dword (32 bit)
        public static void endian_32lo16( int dword, short word)
        {
            dword &= (int)0xffff0000;
            dword |= word;
        }

        // Get the lo word (16bit) : a dword (32 bit)
        public static short endian_32lo16(int dword)
        {
            return (short)(dword & 0xffff);
        }

        // Set the hi word (16bit) : a dword (32 bit)
        public static void endian_32hi16( int dword, short word)
        {
            dword &= (int)0x0000ffff;
            dword |= (int)(word << 16);
            //#endif
        }

        // Get the hi word (16bit) : a dword (32 bit)
        public static short endian_32hi16(int dword)
        {
            return (short)(dword >> 16);
        }

        // Set the lo byte (8 bit) : a dword (32 bit)
        public static void endian_32lo8( int dword, byte _byte)
        {
            dword &= (int)0xffffff00;
            dword |= (int)_byte;
        }

        // Get the lo byte (8 bit) : a dword (32 bit)
        public static byte endian_32lo8(int dword)
        {
            return (byte)dword;
        }

        // Set the hi byte (8 bit) : a dword (32 bit)
        public static void endian_32hi8( int dword, byte _byte)
        {
            dword &= (int)0xffff00ff;
            dword |= (int)(_byte << 8);
        }

        // Get the hi byte (8 bit) : a dword (32 bit)
        public static byte endian_32hi8(int dword)
        {
            return (byte)(dword >> 8);
        }

        // Swap hi and lo words endian : 32 bit dword.
        public static void endian_32swap16( int dword)
        {
            short lo = endian_32lo16(dword);
            short hi = endian_32hi16(dword);
            endian_32lo16( dword, hi);
            endian_32hi16( dword, lo);
        }

        // Swap word endian.
        public static void endian_32swap8( int dword)
        {
            short lo, hi;
            lo = endian_32lo16(dword);
            hi = endian_32hi16(dword);
            endian_16swap8( lo);
            endian_16swap8( hi);
            endian_32lo16( dword, hi);
            endian_32hi16( dword, lo);
        }

        // Convert high-byte and low-byte to 32-bit word.
        public static int endian_32(byte hihi, byte hilo, byte hi, byte lo)
        {
            int dword = 0;
            short word = 0;
            endian_32lo8( dword, lo);
            endian_32hi8( dword, hi);
            endian_16lo8( word, hilo);
            endian_16hi8( word, hihi);
            endian_32hi16( dword, word);
            return dword;
        }

        // Convert high-byte and low-byte to 32-bit little endian word.
        public static int endian_little32(byte[] ptr)
        {
            return endian_32(ptr[3], ptr[2], ptr[1], ptr[0]);
        }

        //ポインター対策版
        public static int endian_little32(ByteBuffer ptr)
        {
            return endian_32(ptr.get(3), ptr.get(2), ptr.get(1), ptr.get(0));
        }

        // Write a little-endian 32-bit word to four bytes : memory.
        public static void endian_little32(byte[] ptr, int dword)
        {
            short word;
            ptr[0] = endian_32lo8(dword);
            ptr[1] = endian_32hi8(dword);
            word = endian_32hi16(dword);
            ptr[2] = endian_16lo8(word);
            ptr[3] = endian_16hi8(word);
        }

        //ポインター対策版
        public static void endian_little32(ByteBuffer ptr, int dword)
        {
            short word;
            ptr.put(0, endian_32lo8(dword));
            ptr.put(1, endian_32hi8(dword));
            word = endian_32hi16(dword);
            ptr.put(2, endian_16lo8(word));
            ptr.put(3, endian_16hi8(word));
        }

        // Convert high-byte and low-byte to 32-bit big endian word.
        public static int endian_big32(byte[] ptr)
        {
            return endian_32(ptr[0], ptr[1], ptr[2], ptr[3]);
        }

        //ポインター対策版
        public static int endian_big32(ByteBuffer ptr)
        {
            return endian_32(ptr.get(0), ptr.get(1), ptr.get(2), ptr.get(3));
        }

        // Write a big-endian 32-bit word to four bytes : memory.
        public static void endian_big32(byte[] ptr, int dword)
        {
            short word;
            word = endian_32hi16(dword);
            ptr[1] = endian_16lo8(word);
            ptr[0] = endian_16hi8(word);
            ptr[2] = endian_32hi8(dword);
            ptr[3] = endian_32lo8(dword);
        }

        //ポインター対策版
        public static void endian_big32(ByteBuffer ptr, int dword)
        {
            short word;
            word = endian_32hi16(dword);
            ptr.put(1, endian_16lo8(word));
            ptr.put(0, endian_16hi8(word));
            ptr.put(2, endian_32hi8(dword));
            ptr.put(3, endian_32lo8(dword));
        }
    }
