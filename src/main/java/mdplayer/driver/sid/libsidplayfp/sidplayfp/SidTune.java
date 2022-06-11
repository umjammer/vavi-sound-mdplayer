/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package mdplayer.driver.sid.libsidplayfp.sidplayfp;

import mdplayer.driver.sid.libsidplayfp.sidmemory;
import mdplayer.driver.sid.libsidplayfp.sidtune.SidTuneBase;
import mdplayer.driver.sid.libsidplayfp.sidtune.SidTuneBase.loadError;

/**
     //SidTune
     */
    public class SidTune
    {



        //# include <stdint.h>
        //# include <memory>
        //# include "SidPlayFp/siddefs.h"

        //class SidTuneInfo;

        //class SidTuneBase;
        //class sidmemory;

        public static final int MD5_LENGTH = 32;

        // Filename extensions to append for various file types.
        //private String[] fileNameExtensions;

        // -------------------------------------------------------------
        private SidTuneBase tune = new SidTuneBase(null);

        private String m_statusString;

        private Boolean m_status;

        // ----------------------------------------------------------------

        /**
         //Load a sidtune from a file.
         *
         //To retrieve data from standard input pass : filename "-".
         //If you want to @Override the default filename extensions use this
         //contructor. Please note, that if the specified "fileName"
         //does exist and the loader instanceof able to determine its file format,
         //this function does not try to append any file name extension.
         //See "SidTune.cpp" for the default list of file name extensions.
         //You can specify "fileName = 0", if you do not want to
         //load a sidtune. You can later load one with open().
         *
         //@param fileName
         //@param fileNameExt
         //@param separatorIsSlash
         */
        //public SidTune(String fileName, String[] fileNameExt = null, Boolean separatorIsSlash = false) { }

        /**
         //Load a single-file sidtune from a memory buffer.
         //Currently supported: PSID and MUS formats.
         *
         //@param oneFileFormatSidtune the buffer that contains song data
         //@param sidtuneLength length of the buffer
         */
        //public SidTune(byte[] oneFileFormatSidtune, int sidtuneLength) { }

        //protected void finalize() { }

        /**
         //The SidTune class does not copy the list of file name extensions,
         //so make sure you keep it. If the provided pointer instanceof 0, the
         //default list will be activated. This instanceof a static list which
         //instanceof used by all SidTune Objects.
         *
         //@param fileNameExt
         */
        //public void setFileNameExtensions(String[] fileNameExt) { }

        /**
         //Load a sidtune into an existing Object from a file.
         *
         //@param fileName
         //@param separatorIsSlash
         */
        //public void load(String fileName, Boolean separatorIsSlash = false) { }

        /**
         //Load a sidtune into an existing Object from a buffer.
         *
         //@param sourceBuffer the buffer that contains song data
         //@param bufferLen length of the buffer
         */
        //public void read(byte[] sourceBuffer, int bufferLen) { }

        /**
         //Select sub-song.
         *
         //@param songNum the selected song (0 = default starting song)
         //@return active song number, 0 if no tune instanceof loaded.
         */
        //public int selectSong(int songNum) { return 0; }

        /**
         //Retrieve current active sub-song specific information.
         *
         //@return a pointer to // #SidTuneInfo, 0 if no tune instanceof loaded. The pointer must not be deleted.
         */
        //public SidTuneInfo getInfo() { return null; }

        /**
         //Select sub-song and retrieve information.
         *
         //@param songNum the selected song (0 = default starting song)
         //@return a pointer to // #SidTuneInfo, 0 if no tune instanceof loaded. The pointer must not be deleted.
         */
        //public SidTuneInfo getInfo(int songNum) { return null; }

        /**
         //Determine current state of Object.
         //Upon error condition use // #statusString to get a descriptive
         //text String.
         *
         //@return current state (true = okay, false = error)
         */
        //public Boolean getStatus() { return false; }

        /**
         //Error/status message of last operation.
         */
        //public String statusString() { return null; }

        /**
         //Copy sidtune into C64 memory (64 KB).
         */
        //public Boolean placeSidTuneInC64mem( libsidplayfp.sidmemory mem) { return false; }

        /**
         //Calculates the MD5 hash of the tune.
         //Not providing an md5 buffer will cause the private one to be used.
         //If provided, buffer must be MD5_LENGTH + 1
         *
         //@return a pointer to the buffer containing the md5 String, 0 if no tune instanceof loaded.
         */
        //public String createMD5(byte[] md5 = null) { return null; }

        //public byte[] c64Data() { return null; }

        // prevent copying
        private SidTune( SidTune s) {
        }
        private SidTune opeEquel( SidTune s) { return null; }




        /*
        //This file instanceof part of libsidplayfp, a SID player engine.
        *
        //Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
        *
        //This program instanceof free software; you can redistribute it and/or modify
        //it under the terms of the GNU General Public License as published by
        //the Free Software Foundation; either version 2 of the License, or
        //(at your option) any later version.
        *
        //This program instanceof distributed : the hope that it will be useful,
        //but WITHOUT ANY WARRANTY; without even the implied warranty of
        //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        //GNU General Public License for more details.
        *
        //You should have received a copy of the GNU General Public License
        //along with this program; if not, write to the Free Software
        //Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
        */

        //#include "SidTune.h"
        //#include "sidtune/SidTuneBase.h"
        //#include "sidcxx11.h"

        private String MSG_NO_ERRORS = "No errors";

        // Default sidtune file name extensions. This selection can be overriden
        // by specifying a custom list : the constructor.
        private static String[] defaultFileNameExt = new String[]
{
    // Preferred default file extension for single-file sidtunes
    // or sidtune description files : SIDPLAY INFOFILE format.
    ".Sid", ".SID",
    // File extensions used (and created) by various C64 emulators and
    // related utilities. These extensions are recommended to be used as
    // a replacement for ".dat" : conjunction with two-file sidtunes.
    ".C64", ".prg", ".p00", ".C64", ".PRG", ".P00",
    // Stereo Sidplayer (.mus/.MUS ought not be included because
    // these must be loaded first; it sometimes contains the first
    // credit lines of a MUS/STR pair).
    ".str", ".STR", ".mus", ".MUS",
    // End.
    null
};

        private String[] fileNameExtensions = defaultFileNameExt;

        public SidTune(String fileName, String[] fileNameExt/* = null*/, Boolean separatorIsSlash /*= false*/)
        {
            setFileNameExtensions(fileNameExt);
            load(fileName, separatorIsSlash);
        }

        public SidTune(byte[] oneFileFormatSidtune, int sidtuneLength)
        {
            read(oneFileFormatSidtune, sidtuneLength);
        }

        protected void finalize()
        {
            // Needed to delete auto_ptr with complete type
        }

        public void setFileNameExtensions(String[] fileNameExt)
        {
            fileNameExtensions = ((fileNameExt != null) ? fileNameExt : defaultFileNameExt);
        }

        public void load(String fileName, Boolean separatorIsSlash/* = false*/)
        {
            try
            {
                tune = tune.load(fileName, fileNameExtensions, separatorIsSlash);
                m_status = true;
                m_statusString = MSG_NO_ERRORS;
            }
            catch (loadError e)
            {
                m_status = false;
                m_statusString = e.message();
            }
        }

        public void read(byte[] sourceBuffer, int bufferLen)
        {
            try
            {
                tune= tune.read(sourceBuffer, bufferLen);
                m_status = true;
                m_statusString = MSG_NO_ERRORS;
            }
            catch (loadError e)
            {
                m_status = false;
                m_statusString = e.message();
            }
        }

        public int selectSong(int songNum)
        {
            return tune != null ? tune.selectSong(songNum) : 0;
        }

        public SidTuneInfo getInfo()
        {
            return tune != null ? tune.getInfo() : null;
        }

        public SidTuneInfo getInfo(int songNum)
        {
            return tune != null ? tune.getInfo(songNum) : null;
        }

        public Boolean getStatus() { return m_status; }

        public String statusString() { return m_statusString; }

        public Boolean placeSidTuneInC64mem( sidmemory mem)
        {
            if (tune == null)
                return false;

            tune.placeSidTuneInC64mem( mem);
            return true;
        }

        public byte[] createMD5(byte[] md5/* = null*/)
        {
            return tune != null ? tune.createMD5(md5) : null;
        }

        public byte c64Data()
        {
            return (byte)(tune != null ? tune.c64Data() : 0);
        }

    }
