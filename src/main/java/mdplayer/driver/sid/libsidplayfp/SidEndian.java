/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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


/**
 * Labeling:
 * 0 - LO
 * 1 - HI
 * 2 - HILO
 * 3 - HIHI
 */
public class SidEndian {

    // INT16 FUNCTIONS

    /** Set the lo byte (8 bit) : a word (16 bit) */
    public static short to16lo8(short word, byte _byte) {
        word &= 0xff00;
        word |= _byte;
        return word;
    }

    /** Get the lo byte (8 bit) : a word (16 bit) */
    public static byte to16lo8(short word) {
        return (byte) word;
    }

    /** Set the hi byte (8 bit) : a word (16 bit) */
    public static short to16hi8(short word, byte _byte) {
        word &= 0x00ff;
        word |= (short) (_byte << 8);
        return word;
    }

    /** Set the hi byte (8 bit) : a word (16 bit) */
    public static byte to16hi8(short word) {
        return (byte) (word >> 8);
    }

    /** Swap word endian. */
    public static short to16swap8(short word) {
        byte lo = to16lo8(word);
        byte hi = to16hi8(word);
        word = to16lo8(word, hi);
        word = to16hi8(word, lo);
        return word;
    }

    /** Convert high-byte and low-byte to 16-bit word. */
    public static short to16(byte hi, byte lo) {
        short word = 0;
        word = to16lo8(word, lo);
        word = to16hi8(word, hi);
        return word;
    }

    /** Convert high-byte and low-byte to 16-bit little endian word. */
    public static void to16(byte[] ptr, short word) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            ptr[0] = to16hi8(word);
            ptr[1] = to16lo8(word);
        } else {
            ptr[0] = to16lo8(word);
            ptr[1] = to16hi8(word);
        }
    }

    /** ポインター対策版 */
    public static void to16(ByteBuffer buf, short word) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            buf.put(0, to16hi8(word));
            buf.put(1, to16lo8(word));
        } else {
            buf.put(0, to16lo8(word));
            buf.put(1, to16hi8(word));
        }
    }

    /** Convert high-byte and low-byte to 16-bit little endian word. */
    public static short toLittle16(byte[] ptr) {
        return to16(ptr[1], ptr[0]);
    }

    /** ポインター対策版 */
    public static short toLittle16(ByteBuffer ptr) {
        return to16(ptr.get(1), ptr.get(0));
    }

    /** Write a little-endian 16-bit word to two bytes : memory. */
    public static void toLittle16(byte[] ptr, short word) {
        ptr[0] = to16lo8(word);
        ptr[1] = to16hi8(word);
    }

    /** ポインター対策版 */
    public static void toLittle16(ByteBuffer ptr, short word) {
        ptr.put(0, to16lo8(word));
        ptr.put(1, to16hi8(word));
    }

    /** Convert high-byte and low-byte to 16-bit big endian word. */
    public static short toBig16(byte[] ptr) {
        return to16(ptr[0], ptr[1]);
    }

    /** ポインター対策版 */
    public static short toBig16(ByteBuffer ptr) {
        return to16(ptr.get(0), ptr.get(1));
    }

    /** Write a little-big 16-bit word to two bytes : memory. */
    public static void toBig16(byte[] ptr, short word) {
        ptr[0] = to16hi8(word);
        ptr[1] = to16lo8(word);
    }

    /** ポインター対策版 */
    public static void toBig16(ByteBuffer ptr, short word) {
        ptr.put(0, to16hi8(word));
        ptr.put(1, to16lo8(word));
    }

    // INT32 FUNCTIONS

    /** Set the lo word (16bit) : a dword (32 bit) */
    public static int to32lo16(int dword, short word) {
        dword &= 0xffff0000;
        dword |= word;
        return dword;
    }

    /** Get the lo word (16bit) : a dword (32 bit) */
    public static short to32lo16(int dword) {
        return (short) (dword & 0xffff);
    }

    /** Set the hi word (16bit) : a dword (32 bit) */
    public static int to32hi16(int dword, short word) {
        dword &= 0x0000ffff;
        dword |= word << 16;
        return dword;
    }

    /** Get the hi word (16bit) : a dword (32 bit) */
    public static short to32hi16(int dword) {
        return (short) (dword >> 16);
    }

    /** Set the lo byte (8 bit) : a dword (32 bit) */
    public static int to32lo8(int dword, byte _byte) {
        dword &= 0xffffff00;
        dword |= _byte;
        return dword;
    }

    /** Get the lo byte (8 bit) : a dword (32 bit) */
    public static byte to32lo8(int dword) {
        return (byte) dword;
    }

    /** Set the hi byte (8 bit) : a dword (32 bit) */
    public static int to32hi8(int dword, byte _byte) {
        dword &= 0xffff00ff;
        dword |= _byte << 8;
        return dword;
    }

    /** Get the hi byte (8 bit) : a dword (32 bit) */
    public static byte to32hi8(int dword) {
        return (byte) (dword >> 8);
    }

    /** Swap hi and lo words endian : 32 bit dword. */
    public static int to32swap16(int dword) {
        short lo = to32lo16(dword);
        short hi = to32hi16(dword);
        dword = to32lo16(dword, hi);
        dword = to32hi16(dword, lo);
        return dword;
    }

    /** Swap word endian. */
    public static int to32swap8(int dword) {
        short lo, hi;
        lo = to32lo16(dword);
        hi = to32hi16(dword);
        lo = to16swap8(lo);
        hi = to16swap8(hi);
        dword = to32lo16(dword, hi);
        dword = to32hi16(dword, lo);
        return dword;
    }

    /** Convert high-byte and low-byte to 32-bit word. */
    public static int to32(byte hihi, byte hilo, byte hi, byte lo) {
        int dword = 0;
        short word = 0;
        dword = to32lo8(dword, lo);
        dword = to32hi8(dword, hi);
        word = to16lo8(word, hilo);
        word = to16hi8(word, hihi);
        dword = to32hi16(dword, word);
        return dword;
    }

    /** Convert high-byte and low-byte to 32-bit little endian word. */
    public static int toLittle32(byte[] ptr) {
        return to32(ptr[3], ptr[2], ptr[1], ptr[0]);
    }

    /** ポインター対策版 */
    public static int toLittle32(ByteBuffer ptr) {
        return to32(ptr.get(3), ptr.get(2), ptr.get(1), ptr.get(0));
    }

    /** Write a little-endian 32-bit word to four bytes : memory. */
    public static void toLittle32(byte[] ptr, int dword) {
        short word;
        ptr[0] = to32lo8(dword);
        ptr[1] = to32hi8(dword);
        word = to32hi16(dword);
        ptr[2] = to16lo8(word);
        ptr[3] = to16hi8(word);
    }

    /** ポインター対策版 */
    public static void toLittle32(ByteBuffer ptr, int dword) {
        short word;
        ptr.put(0, to32lo8(dword));
        ptr.put(1, to32hi8(dword));
        word = to32hi16(dword);
        ptr.put(2, to16lo8(word));
        ptr.put(3, to16hi8(word));
    }

    /** Convert high-byte and low-byte to 32-bit big endian word. */
    public static int toBig32(byte[] ptr) {
        return to32(ptr[0], ptr[1], ptr[2], ptr[3]);
    }

    /** ポインター対策版 */
    public static int toBig32(ByteBuffer ptr) {
        return to32(ptr.get(0), ptr.get(1), ptr.get(2), ptr.get(3));
    }

    /** Write a big-endian 32-bit word to four bytes : memory. */
    public static void toBig32(byte[] ptr, int dword) {
        short word;
        word = to32hi16(dword);
        ptr[1] = to16lo8(word);
        ptr[0] = to16hi8(word);
        ptr[2] = to32hi8(dword);
        ptr[3] = to32lo8(dword);
    }

    /** ポインター対策版 */
    public static void toBig32(ByteBuffer ptr, int dword) {
        short word;
        word = to32hi16(dword);
        ptr.put(1, to16lo8(word));
        ptr.put(0, to16hi8(word));
        ptr.put(2, to32hi8(dword));
        ptr.put(3, to32lo8(dword));
    }
}
