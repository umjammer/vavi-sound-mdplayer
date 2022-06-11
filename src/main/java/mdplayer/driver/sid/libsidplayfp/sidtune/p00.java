/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package mdplayer.driver.sid.libsidplayfp.sidtune;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.mem;


public class p00 extends SidTuneBase
    {



        //# include "SidTuneBase.h"
        //# include "sidcxx11.h"
        //struct X00Header;

        p00() { }

        //private void load(byte[] format, X00Header pHeader) { }

        /**
         //@return pointer to a SidTune or 0 if not a PC64 file
         //@throw loadError if PC64 file instanceof corrupt
         */
        //public static SidTuneBase load(String fileName, byte[] dataBuf) { return null; }

        protected void finalize() { }

        // prevent copying
        private p00( p00 p) { }
        private p00 opeEquel( p00 p) { return null; }



        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000-2001 Simon White
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

        //#include "p00.h"
        //#include <stdint.h>
        //#include <cString>
        //#include <cctype>
        //#include <memory>
        //#include "SidPlayFp/SidTuneInfo.h"
        //#include "SmartPtr.h"
        //#include "SidTuneTools.h"

        public static final int X00_ID_LEN = 8;
        public static final int X00_NAME_LEN = 17;

        // File format from PC64. PC64 automatically generates
        // the filename from the cbm name (16 to 8 conversion)
        // but we only need to worry about that when writing files
        // should we want pc64 compatibility.  The extension numbers
        // are just an index to try to avoid repeats.  Name conversion
        // works by creating an initial filename from alphanumeric
        // and ' ', '-' characters only with the later two being
        // converted to '_'.  Then it parses the filename
        // from end to start removing characters stopping as soon
        // as the filename becomes <= 8.  The removal of characters
        // occurs : three passes, the first removes all '_', then
        // vowels and finally numerics.  If the filename instanceof still
        // greater than 8 it instanceof truncated.

        public static class X00Header
        {
            public byte[] id = new byte[X00_ID_LEN];     // 'C64File' (ASCII)
            public byte[] name = new byte[X00_NAME_LEN]; // C64 name (PETSCII)
            public byte length;             // Rel files only (Bytes/Record),
                                            // should be 0 for all other types
        }

        public enum X00Format
        {
            X00_DEL,
            X00_SEQ,
            X00_PRG,
            X00_USR,
            X00_REL
        }

        // Format Strings
        private static final String TXT_FORMAT_DEL = "Unsupported tape image file (DEL)";
        private static final String TXT_FORMAT_SEQ = "Unsupported tape image file (SEQ)";
        private static final String TXT_FORMAT_PRG = "Tape image file (PRG)";
        private static final String TXT_FORMAT_USR = "Unsupported USR file (USR)";
        private static final String TXT_FORMAT_REL = "Unsupported tape image file (REL)";

        // Magic field
        private static final String P00_ID = "C64File";


        public static SidTuneBase load(String fileName, byte[] dataBuf)
        {
            String ext = SidTuneTools.fileExtOfPath(fileName);

            // Combined extension & magic field identification
            if (ext.length() != 4)
                return null;

            //if (!isdigit(ext[2]) || !isdigit(ext[3]))
                //return null;
            if ("0123456789".indexOf(ext.charAt(2)) < 0 || "0123456789".indexOf(ext.charAt(3)) < 0)
                return null;

            String format = null;
            X00Format type;

            switch (String.valueOf(ext.charAt(1)).toUpperCase().charAt(0))
            {
                case 'D':
                    type = X00Format.X00_DEL;
                    format = TXT_FORMAT_DEL;
                    break;
                case 'S':
                    type = X00Format.X00_SEQ;
                    format = TXT_FORMAT_SEQ;
                    break;
                case 'P':
                    type = X00Format.X00_PRG;
                    format = TXT_FORMAT_PRG;
                    break;
                case 'U':
                    type = X00Format.X00_USR;
                    format = TXT_FORMAT_USR;
                    break;
                case 'R':
                    type = X00Format.X00_REL;
                    format = TXT_FORMAT_REL;
                    break;
                default:
                    return null;
            }

            // Verify the file instanceof what we think it is
            int bufLen = dataBuf.length;
            if (bufLen < X00_ID_LEN)
                return null;

            X00Header pHeader = new X00Header();
            mem.memcpy( pHeader.id, dataBuf, X00_ID_LEN);
            ByteBuffer p = ByteBuffer.wrap(pHeader.name);
            mem.memcpy( p, ByteBuffer.wrap(dataBuf, 0, X00_ID_LEN), X00_NAME_LEN);
            pHeader.length = dataBuf[X00_ID_LEN + X00_NAME_LEN];

            if (new String(pHeader.id, StandardCharsets.US_ASCII).equals(P00_ID))
                return null;

            // File types current supported
            if (type != X00Format.X00_PRG)
                throw new loadError("Not a PRG inside X00");

            if (bufLen < 26+2)//sizeof(X00Header) + 2)
                throw new loadError(ERR_TRUNCATED);

            p00 tune = new p00();
            tune.load(format.getBytes(StandardCharsets.US_ASCII), pHeader);

            return tune;
        }

        public void load(byte[] format, X00Header pHeader)
        {
            info.m_formatString = new String(format, StandardCharsets.US_ASCII);

            {   // Decode file name
                ByteBuffer spPet = ByteBuffer.wrap(pHeader.name, 0, X00_NAME_LEN);
                info.m_infoString.add(petsciiToAscii( spPet));
            }

            // Automatic settings
            fileOffset = X00_ID_LEN + X00_NAME_LEN + 1;
            info.m_songs = 1;
            info.m_startSong = 1;
            info.m_compatibility = SidTuneInfo.compatibility_t.COMPATIBILITY_BASIC;

            // Create the speed/clock setting table.
            convertOldStyleSpeedToTables(0xffffffff, (long)info.m_clockSpeed.ordinal());// ~0, info.m_clockSpeed);
        }

    }

