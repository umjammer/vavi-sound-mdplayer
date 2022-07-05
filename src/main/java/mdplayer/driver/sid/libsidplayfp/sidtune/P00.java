/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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
 *  MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import dotnet4j.io.IOException;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.Mem;


public class P00 extends SidTuneBase {

    P00() {
    }

    // prevent copying
    private P00(P00 p) {
    }

    private P00 opeEquel(P00 p) {
        return null;
    }

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

    private static class X00Header {
        // 'C64File' (ASCII)
        public byte[] id = new byte[X00_ID_LEN];
        // C64 name (PETSCII)
        public byte[] name = new byte[X00_NAME_LEN];
        // Rel files only (Bytes/Record),
        public byte length;
        // should be 0 for all other types
    }

    private enum X00Format {
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

    /**
     * @return pointer to a SidTune or 0 if not a PC64 file
     * @throws IOException if PC64 file instanceof corrupt
     */
    public static SidTuneBase load(String fileName, byte[] dataBuf) {
        String ext = SidTuneTools.fileExtOfPath(fileName);

        // Combined extension & magic field identification
        if (ext.length() != 4)
            return null;

        if ("0123456789".indexOf(ext.charAt(2)) < 0 || "0123456789".indexOf(ext.charAt(3)) < 0)
            return null;

        String format;
        X00Format type;

        switch (String.valueOf(ext.charAt(1)).toUpperCase().charAt(0)) {
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
        Mem.memcpy(pHeader.id, dataBuf, X00_ID_LEN);
        ByteBuffer p = ByteBuffer.wrap(pHeader.name);
        Mem.memcpy(p, ByteBuffer.wrap(dataBuf, 0, X00_ID_LEN), X00_NAME_LEN);
        pHeader.length = dataBuf[X00_ID_LEN + X00_NAME_LEN];

        if (new String(pHeader.id, StandardCharsets.US_ASCII).equals(P00_ID))
            return null;

        // File types current supported
        if (type != X00Format.X00_PRG)
            throw new IOException("Not a PRG inside X00");

        if (bufLen < 26 + 2)
            throw new IOException(ERR_TRUNCATED);

        P00 tune = new P00();
        tune.load(format.getBytes(StandardCharsets.US_ASCII), pHeader);

        return tune;
    }

    public void load(byte[] format, X00Header pHeader) {
        info.formatString = new String(format, StandardCharsets.US_ASCII);

        {   // Decode file name
            ByteBuffer spPet = ByteBuffer.wrap(pHeader.name, 0, X00_NAME_LEN);
            info.infoString.add(petsciiToAscii(spPet));
        }

        // Automatic settings
        fileOffset = X00_ID_LEN + X00_NAME_LEN + 1;
        info.songs = 1;
        info.startSong = 1;
        info.compatibility = SidTuneInfo.Compatibility.BASIC;

        // Create the speed/clock setting table.
        convertOldStyleSpeedToTables(0xffffffff, info.clockSpeed.ordinal());
    }
}

