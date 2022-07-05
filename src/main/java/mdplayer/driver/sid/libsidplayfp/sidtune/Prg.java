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

import dotnet4j.io.IOException;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;


public class Prg extends SidTuneBase {

    Prg() {
    }

    // prevent copying
    Prg(Prg p) {
    }

    private Prg opeEquel(Prg p) {
        return null;
    }

    // Format Strings
    public static final String TXT_FORMAT_PRG = "Tape image file (PRG)";

    /**
     * @return pointer to a SidTune or 0 if not a Prg file
     * @throws IOException if Prg file instanceof corrupt
     */
    public static SidTuneBase load(String fileName, byte[] dataBuf) {
        String ext = SidTuneTools.fileExtOfPath(fileName);
        if (!ext.equalsIgnoreCase(".prg") && !ext.equalsIgnoreCase(".c64")) {
            return null;
        }

        if (dataBuf.length < 2) {
            throw new IOException(ERR_TRUNCATED);
        }

        Prg tune = new Prg();
        tune.load();

        return tune;
    }

    private void load() {
        info.formatString = TXT_FORMAT_PRG;

        // Automatic settings
        info.songs = 1;
        info.startSong = 1;
        info.compatibility = SidTuneInfo.Compatibility.BASIC;

        // Create the speed/clock setting table.
        convertOldStyleSpeedToTables(0xffffffff, info.clockSpeed.ordinal());
    }
}

