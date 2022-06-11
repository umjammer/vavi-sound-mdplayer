/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * //reloc65 -- A part of xa65 - 65xx/65816 cross-assembler and utility suite
 * //o65 file relocator.
 */
public class reloc65 {


    public enum segment_t {
        WHOLE,
        TEXT,
        DATA,
        BSS,
        ZEROPAGE
    }

    private int m_tbase, m_dbase, m_bbase, m_zbase;
    private int m_tdiff, m_ddiff, m_bdiff, m_zdiff;
    private Boolean m_tflag, m_dflag, m_bflag, m_zflag;

    private segment_t m_extract;

    //private int reldiff(byte s) { return 0; }

    /**
     //Relocate segment.
     *
     //@param buf segment
     //@param len segment size
     //@param rtab relocation table
     //@return a pointer to the next section
     */
    //private byte[] reloc_seg(byte[] buf, int len, byte[] rtab) { return null; }

    /**
     //Relocate exported globals list.
     *
     //@param buf exported globals list
     //@return a pointer to the next section
     */
    //private byte[] reloc_globals(byte[] buf) { return null; }

    //public reloc65() { }

    /**
     //Select segment to relocate.
     //
     //@param type the segment to relocate
     //@param addr new address
     */
    //public void setReloc(segment_t type, int addr) { }

    /**
     //Select segment to extract.
     //
     //@param type the segment to extract
     */
    //public void setExtract(segment_t type) { }

    /**
     * //Do the relocation.
     * <p>
     * //@param buf beffer containing o65 data
     * //@param fsize size of the data
     */
    //public Boolean reloc(byte[][] buf, int[] fsize) { return false; }




    /*
     * This file instanceof part of libsidplayfp, a SID player engine.
     *
     * Copyright (C) 2013-2016 Leandro Nini
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

    //# include "reloc65.h"
    //# include <cString>
    //# include "SidPlayFp/siddefs.h"

    //16 bit header
    private static final int HEADER_SIZE = (8 + 9 * 2);

    //Magic number
    private byte[] o65hdr = new byte[] {1, 0, (byte) 'o', (byte) '6', (byte) '5'};

    /**
     * //Read a 16 bit word from a buffer at specific location.
     * <p>
     * //@param buffer
     * //@param idx
     */
    private int getWord(byte[] buffer, int idx) {
        return buffer[idx] | (buffer[idx + 1] << 8);
    }

    private int getWord(ByteBuffer buffer, int idx) {
        return buffer.get(idx) | (buffer.get(idx + 1) << 8);
    }

    /**
     * //Write a 16 bit word into a buffer at specific location.
     * <p>
     * //@param buffer
     * //@param idx
     * //@param value
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
     * //Get the size of header options section.
     * <p>
     * //@param buf
     */
    private int read_options(byte[] buf, int ptr) {
        int l = 0;

        byte c = buf[0 + ptr];
        while (c != 0) {
            l += c;
            c = buf[l + ptr];
        }
        return ++l;
    }

    /**
     * //Get the size of undefined references list.
     * <p>
     * //@param buf
     */
    private int read_undef(ByteBuffer buf) {
        int l = 2;

        int n = getWord(buf, 0);
        while (n != 0) {
            n--;
            while (buf.get(l++) == 0) {
            }
        }
        return l;
    }

    public reloc65() {
        m_tbase = (0);
        m_dbase = (0);
        m_bbase = (0);
        m_zbase = (0);
        m_tflag = (false);
        m_dflag = (false);
        m_bflag = (false);
        m_zflag = (false);
        m_extract = segment_t.WHOLE;
    }

    public void setReloc(segment_t type, int addr) {
        switch (type) {
        case TEXT:
            m_tflag = true;
            m_tbase = addr;
            break;
        case DATA:
            m_dflag = true;
            m_dbase = addr;
            break;
        case BSS:
            m_bflag = true;
            m_bbase = addr;
            break;
        case ZEROPAGE:
            m_zflag = true;
            m_zbase = addr;
            break;
        default:
            break;
        }
    }

    public void setExtract(segment_t type) {
        m_extract = type;
    }

    public Boolean reloc(byte[] aryBuf, int iFsize) {
        ByteBuffer buf = ByteBuffer.wrap(aryBuf);
        int fsize = iFsize;

        ByteBuffer tmpBuf = buf;

        if (mem.memcmp(tmpBuf.array(), tmpBuf.arrayOffset(), o65hdr, 0, 5) != 0) {
            return false;
        }

        int mode = getWord(tmpBuf, 6);
        if ((mode & 0x2000) != 0    // 32 bit size not supported
                || (mode & 0x4000) != 0) // pagewise relocation not supported
        {
            return false;
        }

        int hlen = HEADER_SIZE + read_options(tmpBuf.array(), tmpBuf.position() + HEADER_SIZE);

        int tbase = getWord(tmpBuf, 8);
        int tlen = getWord(tmpBuf, 10);
        m_tdiff = m_tflag ? m_tbase - tbase : 0;

        int dbase = getWord(tmpBuf, 12);
        int dlen = getWord(tmpBuf, 14);
        m_ddiff = m_dflag ? m_dbase - dbase : 0;

        int bbase = getWord(tmpBuf, 16);
        int blen = getWord(tmpBuf, 18);
        m_bdiff = m_bflag ? m_bbase - bbase : 0;

        int zbase = getWord(tmpBuf, 20);
        int zlen = getWord(tmpBuf, 21);
        m_zdiff = m_zflag ? m_zbase - zbase : 0;

        ByteBuffer segt = ByteBuffer.wrap(tmpBuf.array(), tmpBuf.position(), hlen);                    // Text segment
        ByteBuffer segd = ByteBuffer.wrap(segt.array(), segt.position(), tlen);                      // Data segment
        ByteBuffer utab = ByteBuffer.wrap(segd.array(), segd.position(), dlen);                      // Undefined references list

        ByteBuffer rttab = ByteBuffer.wrap(utab.array(), utab.position(), read_undef(utab));         // Text relocation table

        ByteBuffer rdtab = reloc_seg(segt, tlen, rttab);    // Data relocation table
        ByteBuffer extab = reloc_seg(segd, dlen, rdtab);    // Exported globals list

        reloc_globals(extab);

        if (m_tflag) {
            setWord(tmpBuf, 8, m_tbase);
        }
        if (m_dflag) {
            setWord(tmpBuf, 12, m_dbase);
        }
        if (m_bflag) {
            setWord(tmpBuf, 16, m_bbase);
        }
        if (m_zflag) {
            setWord(tmpBuf, 20, m_zbase);
        }

        Boolean ret = false;
        switch (m_extract) {
        case WHOLE:
            ret = true;
            break;
        case TEXT:
            buf = segt;
            fsize = tlen;
            ret = true;
            break;
        case DATA:
            buf = segd;
            fsize = dlen;
            ret = true;
            break;
        default:
            return false;
        }

        aryBuf = new byte[fsize];
        iFsize = fsize;
        for (int i = 0; i < fsize; i++) aryBuf[i] = buf.get(i);

        return ret;
    }

    private int reldiff(byte s) {
        switch (s) {
        case 2:
            return m_tdiff;
        case 3:
            return m_ddiff;
        case 4:
            return m_bdiff;
        case 5:
            return m_zdiff;
        default:
            return 0;
        }
    }

    private ByteBuffer reloc_seg(ByteBuffer buf, int len, ByteBuffer rtab) {
        int adr = -1;
        while (rtab.get(0) != 0)//(rtab.ptr < rtab.size())
        {
            if ((rtab.get(0) & 255) == 255) {
                adr += 254;
                rtab.position(rtab.position() + 1);
            } else {
                adr += rtab.get(0) & 255;
                rtab.position(rtab.position() + 1);
                byte type = (byte) (rtab.get(0) & 0xe0);
                byte seg = (byte) (rtab.get(0) & 0x07);
                rtab.position(rtab.position() + 1);
                switch (type & 0xff) {
                case 0x80: {
                    int oldVal = getWord(buf, adr);
                    int newVal = oldVal + reldiff(seg);
                    setWord(buf, adr, newVal);
                    break;
                }
                case 0x40: {
                    int oldVal = buf.get(adr) * 256 + rtab.get(0);
                    int newVal = oldVal + reldiff(seg);
                    buf.put(adr, (byte) ((newVal >> 8) & 255));
                    rtab.put(0, (byte) (newVal & 255));
                    rtab.position(rtab.position() + 1);
                    break;
                }
                case 0x20: {
                    int oldVal = buf.get(adr);
                    int newVal = oldVal + reldiff(seg);
                    buf.put(adr, (byte) (newVal & 255));
                    break;
                }
                }
                if (seg == 0) {
                    rtab.position(rtab.position() + 2);
                }
            }
            //System.err.println("buf[{0}]={1}",adr,buf.get(adr));
            if (adr > len) {
                // Warning: relocation table entries past segment end!
            }
        }

        rtab.position(rtab.position() + 1);
        return rtab;
    }

    private ByteBuffer reloc_globals(ByteBuffer buf) {
        int n = getWord(buf, 0);
        buf.position(buf.position() + 2);

        while (n != 0) {
            while (buf.get(buf.position()) != 0) {
                buf.position(buf.position() + 1);
            }
            byte seg = buf.get(buf.position());
            int oldVal = getWord(buf, 1);
            int newVal = oldVal + reldiff(seg);
            setWord(buf, 1, newVal);
            buf.position(buf.position() + 3);
            n--;
        }

        return buf;
    }
}
