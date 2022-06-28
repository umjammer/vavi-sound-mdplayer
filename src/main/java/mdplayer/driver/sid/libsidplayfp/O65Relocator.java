/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright (C) 2013-2014 Leandro Nini
 * Copyright (C) 2001 Dag Lem
 * Copyright (C) 1989-1997 Andr� Fachat (a.fachat@physik.tu-chemnitz.de)
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
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
    Modified by Dag Lem <resid@nimrod.no>
    Relocate and extract text segment from memory buffer instead of file.
    For use with VICE VSID.

    Ported to c++ by Leandro Nini
*/

package mdplayer.driver.sid.libsidplayfp;

import java.nio.ByteBuffer;

import mdplayer.driver.sid.mem;


/**
 * O65Relocator -- A part of xa65 - 65xx/65816 cross-assembler and utility suite
 * o65 file relocator.
 */
public class O65Relocator {

    public enum Segment {
        WHOLE,
        TEXT,
        DATA,
        BSS,
        ZEROPAGE
    }

    private int tBase, dBase, bBase, zBase;
    private int tDiff, dDiff, bDiff, zDiff;
    private boolean tFlag, dFlag, bFlag, zFlag;

    private Segment extract;

    /**
     * 16 bit header
     */
    private static final int HEADER_SIZE = 8 + 9 * 2;

    /**
     * Magic number
     */
    private static final byte[] magic = new byte[] {1, 0, (byte) 'o', (byte) '6', (byte) '5'};

    /**
     * Read a 16 bit word from a buffer at specific location.
     *
     * @param buffer
     * @param idx
     */
    private int getWord(byte[] buffer, int idx) {
        return buffer[idx] | (buffer[idx + 1] << 8);
    }

    private int getWord(ByteBuffer buffer, int idx) {
        return buffer.get(idx) | (buffer.get(idx + 1) << 8);
    }

    /**
     * Write a 16 bit word into a buffer at specific location.
     *
     * @param buffer
     * @param idx
     * @param value
     */
    private void setWord(byte[] buffer, int idx, int value) {
        buffer[idx] = (byte) (value & 0xff);
        buffer[idx + 1] = (byte) ((value >> 8) & 0xff);
    }

    private void setWord(ByteBuffer buffer, int idx, int value) {
        buffer.put(idx, (byte) (value & 0xff));
        buffer.put(idx + 1, (byte) ((value >> 8) & 0xff));
    }

    /**
     * Get the size of header options section.
     *
     * @param buf
     */
    private int readOptions(byte[] buf, int ptr) {
        int l = 0;

        byte c = buf[0 + ptr];
        while (c != 0) {
            l += c;
            c = buf[l + ptr];
        }
        return ++l;
    }

    /**
     * Get the size of undefined references list.
     *
     * @param buf
     */
    private int readUndef(ByteBuffer buf) {
        int l = 2;

        int n = getWord(buf, 0);
        while (n != 0) {
            n--;
            while (buf.get(l++) == 0) {
            }
        }
        return l;
    }

    public O65Relocator() {
        tBase = 0;
        dBase = 0;
        bBase = 0;
        zBase = 0;
        tFlag = false;
        dFlag = false;
        bFlag = false;
        zFlag = false;
        extract = Segment.WHOLE;
    }

    /**
     * Select segment to relocate.
     * 
     * @param type the segment to relocate
     * @param address new address
     */
    public void setReloc(Segment type, int address) {
        switch (type) {
        case TEXT:
            tFlag = true;
            tBase = address;
            break;
        case DATA:
            dFlag = true;
            dBase = address;
            break;
        case BSS:
            bFlag = true;
            bBase = address;
            break;
        case ZEROPAGE:
            zFlag = true;
            zBase = address;
            break;
        default:
            break;
        }
    }

    /**
     * Select segment to extract.
     * 
     * @param type the segment to extract
     */
    public void setExtract(Segment type) {
        extract = type;
    }

    /**
     * Do the relocation.
     *
     * @param buffer beffer containing o65 data
     * @param size size of the data TODO out?
     */
    public boolean relocate(byte[] buffer, int size) {
        ByteBuffer buf = ByteBuffer.wrap(buffer);
        int fSize = size;

        ByteBuffer tmpBuf = buf;

        if (mem.memcmp(tmpBuf.array(), tmpBuf.arrayOffset(), magic, 0, 5) != 0) {
            return false;
        }

        int mode = getWord(tmpBuf, 6);
        if ((mode & 0x2000) != 0 // 32 bit size not supported
                || (mode & 0x4000) != 0) // pagewise relocation not supported
        {
            return false;
        }

        int hLen = HEADER_SIZE + readOptions(tmpBuf.array(), tmpBuf.position() + HEADER_SIZE);

        int tBase = getWord(tmpBuf, 8);
        int tLen = getWord(tmpBuf, 10);
        tDiff = tFlag ? this.tBase - tBase : 0;

        int dBase = getWord(tmpBuf, 12);
        int dLen = getWord(tmpBuf, 14);
        dDiff = dFlag ? this.dBase - dBase : 0;

        int bBase = getWord(tmpBuf, 16);
        int bLen = getWord(tmpBuf, 18);
        bDiff = bFlag ? this.bBase - bBase : 0;

        int zBase = getWord(tmpBuf, 20);
        int zLen = getWord(tmpBuf, 21);
        zDiff = zFlag ? this.zBase - zBase : 0;

        ByteBuffer segt = ByteBuffer.wrap(tmpBuf.array(), tmpBuf.position(), hLen);                    // Text segment
        ByteBuffer segd = ByteBuffer.wrap(segt.array(), segt.position(), tLen);                      // Data segment
        ByteBuffer utab = ByteBuffer.wrap(segd.array(), segd.position(), dLen);                      // Undefined references list

        ByteBuffer rttab = ByteBuffer.wrap(utab.array(), utab.position(), readUndef(utab));         // Text relocation table

        ByteBuffer rdtab = relocateSegment(segt, tLen, rttab);    // Data relocation table
        ByteBuffer extab = relocateSegment(segd, dLen, rdtab);    // Exported globals list

        reloccateGlobals(extab);

        if (tFlag) {
            setWord(tmpBuf, 8, this.tBase);
        }
        if (dFlag) {
            setWord(tmpBuf, 12, this.dBase);
        }
        if (bFlag) {
            setWord(tmpBuf, 16, this.bBase);
        }
        if (zFlag) {
            setWord(tmpBuf, 20, this.zBase);
        }

        boolean ret;
        switch (extract) {
        case WHOLE:
            ret = true;
            break;
        case TEXT:
            buf = segt;
            fSize = tLen;
            ret = true;
            break;
        case DATA:
            buf = segd;
            fSize = dLen;
            ret = true;
            break;
        default:
            return false;
        }

        buffer = new byte[fSize];
        size = fSize;
        for (int i = 0; i < fSize; i++) buffer[i] = buf.get(i);

        return ret;
    }

    private int relDiff(byte s) {
        switch (s) {
        case 2:
            return tDiff;
        case 3:
            return dDiff;
        case 4:
            return bDiff;
        case 5:
            return zDiff;
        default:
            return 0;
        }
    }

    /**
     * Relocate segment.
     *
     * @param buf segment
     * @param len segment size
     * @param table relocation table
     * @return a pointer to the next section
     */
    private ByteBuffer relocateSegment(ByteBuffer buf, int len, ByteBuffer table) {
        int adress = -1;
        while (table.get(0) != 0) {
            if ((table.get(0) & 255) == 255) {
                adress += 254;
                table.position(table.position() + 1);
            } else {
                adress += table.get(0) & 255;
                table.position(table.position() + 1);
                byte type = (byte) (table.get(0) & 0xe0);
                byte seg = (byte) (table.get(0) & 0x07);
                table.position(table.position() + 1);
                switch (type & 0xff) {
                case 0x80: {
                    int oldVal = getWord(buf, adress);
                    int newVal = oldVal + relDiff(seg);
                    setWord(buf, adress, newVal);
                    break;
                }
                case 0x40: {
                    int oldVal = buf.get(adress) * 256 + table.get(0);
                    int newVal = oldVal + relDiff(seg);
                    buf.put(adress, (byte) ((newVal >> 8) & 255));
                    table.put(0, (byte) (newVal & 255));
                    table.position(table.position() + 1);
                    break;
                }
                case 0x20: {
                    int oldVal = buf.get(adress);
                    int newVal = oldVal + relDiff(seg);
                    buf.put(adress, (byte) (newVal & 255));
                    break;
                }
                }
                if (seg == 0) {
                    table.position(table.position() + 2);
                }
            }
            //System.err.println("buf[%d]=%d",adress,buf.get(adress));
            if (adress > len) {
                // Warning: relocation table entries past segment end!
            }
        }

        table.position(table.position() + 1);
        return table;
    }

    /**
     * Relocate exported globals list.
     *
     * @param buf exported globals list
     * @return a pointer to the next section
     */
    private ByteBuffer reloccateGlobals(ByteBuffer buf) {
        int n = getWord(buf, 0);
        buf.position(buf.position() + 2);

        while (n != 0) {
            while (buf.get(buf.position()) != 0) {
                buf.position(buf.position() + 1);
            }
            byte seg = buf.get(buf.position());
            int oldVal = getWord(buf, 1);
            int newVal = oldVal + relDiff(seg);
            setWord(buf, 1, newVal);
            buf.position(buf.position() + 3);
            n--;
        }

        return buf;
    }
}
