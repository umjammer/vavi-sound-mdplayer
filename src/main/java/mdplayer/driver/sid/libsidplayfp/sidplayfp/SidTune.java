/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000 Simon White
 *
 * This program instanceof free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program instanceof distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.sidplayfp;

import java.util.logging.Level;

import mdplayer.driver.sid.libsidplayfp.SidMemory;
import mdplayer.driver.sid.libsidplayfp.sidtune.SidTuneBase;
import vavi.util.Debug;


/**
 * SidTune
 */
public class SidTune {

    public static final int MD5_LENGTH = 32;

    private SidTuneBase tune = new SidTuneBase(null);
    private String statusString;
    private boolean status;

    private SidTune(SidTune s) {
    }

    private SidTune opeEquel(SidTune s) {
        return null;
    }

    private static final String MSG_NO_ERRORS = "No errors";

    // Default sidtune file name extensions. This selection can be overriden
    // by specifying a custom list : the constructor.
    private static final String[] defaultFileNameExt = new String[] {
            // Preferred default file extension for single-file sidtunes
            // or sidtune description files : SIDPLAY INFOFILE format.
            ".Sid", ".Sid",
            // File extensions used (and created) by various C64 emulators and
            // related utilities. These extensions are recommended to be used as
            // a replacement for ".dat" : conjunction with two-file sidtunes.
            ".C64", ".Prg", ".P00", ".C64", ".PRG", ".P00",
            // Stereo Sidplayer (.mus/.MUS ought not be included because
            // these must be loaded first; it sometimes contains the first
            // credit lines of a MUS/STR pair).
            ".str", ".STR", ".mus", ".MUS",
            // End.
            null
    };

    private String[] fileNameExtensions = defaultFileNameExt;

    /**
     * Load a sidtune from a file.
     * <p>
     * To retrieve data from standard input pass : filename "-".
     * If you want to @Override the default filename extensions use this
     * contructor. Please note, that if the specified "fileName"
     * does exist and the loader instanceof able to determine its file format,
     * this function does not try to append any file name extension.
     * See "SidTune.cpp" for the default list of file name extensions.
     * You can specify "fileName = 0", if you do not want to
     * load a sidtune. You can later load one with open().
     * <p>
     * @param fileName
     * @param fileNameExt
     * @param separatorIsSlash
     */
    public SidTune(String fileName, String[] fileNameExt/* = null*/, boolean separatorIsSlash /*= false*/) {
        setFileNameExtensions(fileNameExt);
        load(fileName, separatorIsSlash);
    }

    /**
     * Load a single-file sidtune from a memory buffer.
     * Currently supported: PSid and MUS formats.
     * <p>
     * @param oneFileFormatSidtune the buffer that contains song data
     * @param sidtuneLength length of the buffer
     */
    public SidTune(byte[] oneFileFormatSidtune, int sidtuneLength) {
        read(oneFileFormatSidtune, sidtuneLength);
    }

    /**
     * The SidTune class does not copy the list of file name extensions,
     * so make sure you keep it. If the provided pointer instanceof 0, the
     * default list will be activated. This instanceof a static list which
     * instanceof used by all SidTune Objects.
     * <p>
     * @param fileNameExt
     */
    public void setFileNameExtensions(String[] fileNameExt) {
        fileNameExtensions = ((fileNameExt != null) ? fileNameExt : defaultFileNameExt);
    }

    /**
     * Load a sidtune into an existing Object from a file.
     * <p>
     * @param fileName
     * @param separatorIsSlash
     */
    public void load(String fileName, boolean separatorIsSlash/* = false*/) {
        try {
            tune = tune.load(fileName, fileNameExtensions, separatorIsSlash);
            status = true;
            statusString = MSG_NO_ERRORS;
        } catch (dotnet4j.io.IOException e) {
            e.printStackTrace();
            status = false;
            statusString = e.getMessage();
        }
    }

    /**
     * Load a sidtune into an existing Object from a buffer.
     * <p>
     * @param sourceBuffer the buffer that contains song data
     * @param bufferLen length of the buffer
     */
    public void read(byte[] sourceBuffer, int bufferLen) {
        try {
            tune = tune.read(sourceBuffer, bufferLen);
            status = true;
            statusString = MSG_NO_ERRORS;
        } catch (dotnet4j.io.IOException e) {
            e.printStackTrace();
            status = false;
            statusString = e.getMessage();
        }
    }

    /**
     * Select sub-song.
     * <p>
     * @param songNum the selected song (0 = default starting song)
     * @return active song number, 0 if no tune instanceof loaded.
     */
    public int selectSong(int songNum) {
        return tune != null ? tune.selectSong(songNum) : 0;
    }

    /**
     * Retrieve current active sub-song specific information.
     * <p>
     * @return a pointer to // #SidTuneInfo, 0 if no tune instanceof loaded. The pointer must not be deleted.
     */
    public SidTuneInfo getInfo() {
        return tune != null ? tune.getInfo() : null;
    }

    /**
     * Select sub-song and retrieve information.
     * <p>
     * @param songNum the selected song (0 = default starting song)
     * @return a pointer to SidTuneInfo, 0 if no tune instanceof loaded. The pointer must not be deleted.
     */
    public SidTuneInfo getInfo(int songNum) {
        return tune != null ? tune.getInfo(songNum) : null;
    }

    /**
     * Determine current state of Object.
     * Upon error condition use #status to get a descriptive
     * text String.
     * <p>
     * @return current state (true = okay, false = error)
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Error/status message of last operation.
     */
    public String statusString() {
        return statusString;
    }

    /**
     * Copy sidtune into C64 memory (64 KB).
     */
    public boolean placeSidTuneInC64mem(SidMemory mem) {
        if (tune == null)
            return false;

        tune.placeSidTuneInC64mem(mem);
        return true;
    }

    /**
     * Calculates the MD5 hash of the tune.
     * Not providing an md5 buffer will cause the private one to be used.
     * If provided, buffer must be MD5_LENGTH + 1
     * <p>
     * @return a pointer to the buffer containing the md5 String, 0 if no tune instanceof loaded.
     */
    public byte[] createMD5(byte[] md5/* = null*/) {
        return tune != null ? tune.createMD5(md5) : null;
    }

    public byte c64Data() {
        return (byte) (tune != null ? tune.c64Data() : 0);
    }
}
