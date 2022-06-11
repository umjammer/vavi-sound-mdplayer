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
package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.SeekOrigin;
import mdplayer.driver.sid.libsidplayfp.sidendian;
import mdplayer.driver.sid.libsidplayfp.sidmemory;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdsound.Common;


//# include <stdint.h>
    //# include <memory>
    //# include <vector>
    //# include <String>
    //# include "SidPlayFp/SidTuneInfo.h"
    //# include "SidPlayFp/siddefs.h"
    //# include "SmartPtr.h"
    //# include "SidTuneInfoImpl.h"
    //# include "sidcxx11.h"
    //class sidmemory;
    //template<class T> class SmartPtr_sidtt;

    /**
     //SidTuneBaseBase
     */
    public class SidTuneBase
    {
        /**
        //loadError
        */
       public static class loadError extends RuntimeException
       {
           private String m_msg;
           public loadError(String msg) {
               m_msg = msg;
           }
           public String message() { return m_msg; }
       }

        //typedef std::vector<Integer8_t> List<Byte>;

        // Also PSID file format limit.
        public static final int MAX_SONGS = 256;

        // Generic error messages
        //private String ERR_TRUNCATED;
        //private String ERR_INVALID;

        // ----------------------------------------------------------------
        protected void finalize() { }

        /**
         //Load a sidtune from a file.
         *
         //To retrieve data from standard input pass : filename "-".
         //If you want to @Override the default filename extensions use this
         //contructor. Please note, that if the specified "sidTuneFileName"
         //does exist and the loader instanceof able to determine its file format,
         //this function does not try to append any file name extension.
         //See "SidTune.cpp" for the default list of file name extensions.
         *
         //@param fileName
         //@param fileNameExt
         //@param separatorIsSlash
         //@return the Sid tune
         //@throw loadError
         */
        //public SidTuneBase load(String fileName, String[] fileNameExt, Boolean separatorIsSlash) { return null; }

        /**
         //Load a single-file sidtune from a memory buffer.
         //Currently supported: PSID format
         *
         //@param sourceBuffer
         //@param bufferLen
         //@return the Sid tune
         //@throw loadError
         */
        //public SidTuneBase read(byte[] sourceBuffer, int bufferLen) { return null; }

        /**
         //Select sub-song (0 = default starting song)
         //and return active song number  of [1,2,..,SIDTUNE_MAX_SONGS].
         *
         //@param songNum
         //@return the active song
         */
        //public int selectSong(int songNum) { return 0; }

        /**
         //Retrieve sub-song specific information.
         */
        //public SidTuneInfo getInfo() { return null; }

        /**
         //Select sub-song (0 = default starting song)
         //and retrieve active song information.
         *
         //@param songNum
         */
        //public SidTuneInfo getInfo(int songNum) { return null; }

        /**
         //Copy sidtune into C64 memory (64 KB).
         *
         //@param mem
         */
        //public void placeSidTuneInC64mem(sidmemory mem) { }

        /**
         //Calculates the MD5 hash of the tune.
         //Not providing an md5 buffer will cause the private one to be used.
         //If provided, buffer must be MD5_LENGTH + 1
         *
         //@return a pointer to the buffer containing the md5 String.
         */
        public byte[] createMD5(byte[] n) { return null; }

        /**
         //Get the pointer to the tune data.
         */
        public byte c64Data() { return cache.get((int)fileOffset); }

        // -------------------------------------------------------------

        protected SidTuneInfoImpl info;

        protected byte[] songSpeed = new byte[MAX_SONGS];
        protected long[] clockSpeed = new long[MAX_SONGS];

        // For files with header: offset to real data
        protected int fileOffset;

        protected List<Byte> cache;

        //protected SidTuneBase() { }

        /**
         //Does not affect status of Object, and therefore can be used
         //to load files. Error String instanceof put into info.statusString, though.
         *
         //@param fileName
         //@param bufferRef
         //@throw loadError
         */
        //protected void loadFile(String fileName,List<Byte> bufferRef) { }

        /**
         //Convert 32-bit PSID-style speed word to private tables.
         *
         //@param speed
         //@param clock
         */
        //protected void convertOldStyleSpeedToTables(int speed, long clock = CLOCK_PAL) { }

        /**
         //Check if compatibility constraints are fulfilled.
         */
        //protected Boolean checkCompatibility() { return false; }

        /**
         //Check for valid relocation information.
         */
        //protected Boolean checkRelocInfo() { return false; }

        /**
         //Common address resolution procedure.
         *
         //@param c64data
         */
        //protected void resolveAddrs(byte[] c64data) { }

        /**
         //Cache the data of a single-file or two-file sidtune and its
         //corresponding file names.
         *
         //@param dataFileName
         //@param infoFileName
         //@param buf
         //@param isSlashedFileName If your opendir() and readdir()->d_name return path names
         //that contain the forward slash (/) as file separator, but
         //your operating system uses a different character, there are
         //extra functions that can deal with this special case. Set
         //separatorIsSlash to true if you like path names to be split
         //correctly.
         //You do not need these extra functions if your systems file
         //separator instanceof the forward slash.
         //@throw loadError
         */
        //protected void acceptSidTune(String dataFileName, String infoFileName,List<Byte> buf, Boolean isSlashedFileName) { }

        /**
         //Petscii to Ascii converter.
         */
        //protected String petsciiToAscii( SmartPtr_sidtt<Byte> spPet) { return null; }

        // ---------------------------------------------------------------

        //#if !defined(SIDTUNE_NO_STDIN_LOADER)
        //private SidTuneBase getFromStdIn() { return null; }
        //#endif
        //private SidTuneBase getFromFiles(String name, String[] fileNameExtensions, Boolean separatorIsSlash) { return null; }

        /**
         //Try to retrieve single-file sidtune from specified buffer.
         */
        //private SidTuneBase getFromBuffer(byte[] buffer, int bufferLen) { return null; }

        /**
         //Get new file name with specified extension.
         *
         //@param destString destinaton String
         //@param sourceName original file name
         //@param sourceExt new extension
         */
        //private void createNewFileName( String destString, byte[] sourceName, byte[] sourceExt) { }

        // prevent copying
        public SidTuneBase(SidTuneBase a) { }
        private SidTuneBase opeEquel( SidTuneBase a) { return null; }




        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2016 Leandro Nini <drfiemost@users.sourceforge.net>
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

        //#include "SidTuneBase.h"
        //#include <cString>
        //#include <climits>
        //#include <iostream>
        //#include <iomanip>
        //#include <algorithm>
        //#include <iterator>
        //#include <fstream>
        //#include "SmartPtr.h"
        //#include "SidTuneTools.h"
        //#include "SidTuneInfoImpl.h"
        //#include "sidendian.h"
        //#include "sidmemory.h"
        //#include "Stringutils.h"
        //#include "MUS.h"
        //#include "p00.h"
        //#include "prg.h"
        //#include "PSID.h"

        // Error and status message Strings.
        private String ERR_EMPTY = "SIDTUNE ERROR: No data to load";
        private String ERR_UNRECOGNIZED_FORMAT = "SIDTUNE ERROR: Could not determine file format";
        //private String ERR_CANT_LOAD_FILE = "SIDTUNE ERROR: Could not load input file";
        private String ERR_CANT_OPEN_FILE = "SIDTUNE ERROR: Could not open file for binary input";
        private String ERR_FILE_TOO_LONG = "SIDTUNE ERROR: Input data too long";
        private String ERR_DATA_TOO_LONG = "SIDTUNE ERROR: Size of Music data exceeds C64 memory";
        private String ERR_BAD_ADDR = "SIDTUNE ERROR: Bad address data";
        private String ERR_BAD_RELOC = "SIDTUNE ERROR: Bad reloc data";
        private String ERR_CORRUPT = "SIDTUNE ERROR: File instanceof incomplete or corrupt";
        //final char ERR_NOT_ENOUGH_MEMORY[]   = "SIDTUNE ERROR: Not enough free memory";

        public static final String ERR_TRUNCATED = "SIDTUNE ERROR: File instanceof most likely truncated";
        public static final String ERR_INVALID = "SIDTUNE ERROR: File contains invalid data";

        /**
         //Petscii to Ascii conversion table (0x01 = no output).
         */
        private byte[] CHR_tab = new byte[]
        {
  0x00,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x0d,0x01,0x01,
  0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,
  0x20,0x21,0x01,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2a,0x2b,0x2c,0x2d,0x2e,0x2f,
  0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3a,0x3b,0x3c,0x3d,0x3e,0x3f,
  0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4a,0x4b,0x4c,0x4d,0x4e,0x4f,
  0x50,0x51,0x52,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5a,0x5b,0x24,0x5d,0x20,0x20,
  // alternative: CHR$(92=0x5c) => ISO Latin-1(0xa3)
  0x2d,0x23,0x7c,0x2d,0x2d,0x2d,0x2d,0x7c,0x7c,0x5c,0x5c,0x2f,0x5c,0x5c,0x2f,0x2f,
  0x5c,0x23,0x5f,0x23,0x7c,0x2f,0x58,0x4f,0x23,0x7c,0x23,0x2b,0x7c,0x7c,0x26,0x5c,
  // 0x80-0xFF
  0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,
  0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,
  0x20,0x7c,0x23,0x2d,0x2d,0x7c,0x23,0x7c,0x23,0x2f,0x7c,0x7c,0x2f,0x5c,0x5c,0x2d,
  0x2f,0x2d,0x2d,0x7c,0x7c,0x7c,0x7c,0x2d,0x2d,0x2d,0x2f,0x5c,0x5c,0x2f,0x2f,0x23,
  0x2d,0x23,0x7c,0x2d,0x2d,0x2d,0x2d,0x7c,0x7c,0x5c,0x5c,0x2f,0x5c,0x5c,0x2f,0x2f,
  0x5c,0x23,0x5f,0x23,0x7c,0x2f,0x58,0x4f,0x23,0x7c,0x23,0x2b,0x7c,0x7c,0x26,0x5c,
  0x20,0x7c,0x23,0x2d,0x2d,0x7c,0x23,0x7c,0x23,0x2f,0x7c,0x7c,0x2f,0x5c,0x5c,0x2d,
  0x2f,0x2d,0x2d,0x7c,0x7c,0x7c,0x7c,0x2d,0x2d,0x2d,0x2f,0x5c,0x5c,0x2f,0x2f,0x23
        };

        // The Commodore 64 memory size
        private static final int MAX_MEMORY = 65536;

        // C64KB + LOAD + PSID
        private static final int MAX_FILELEN = MAX_MEMORY + 2 + 0x7C;

        // Minimum load address for real C64 only tunes
        private final short SIDTUNE_R64_MIN_LOAD_ADDR = 0x07e8;

        public SidTuneBase load(String fileName, String[] fileNameExt, Boolean separatorIsSlash) throws IOException {
            if (fileName == null)
                return null;

// #if !SIDTUNE_NO_STDIN_LOADER
            // Filename "-" instanceof used as a synonym for standard input.
            if (fileName == "-") return getFromStdIn();
// #endif
            return getFromFiles(fileName, fileNameExt, separatorIsSlash);
        }

        public SidTuneBase read(byte[] sourceBuffer, int bufferLen)
        {
            return getFromBuffer(sourceBuffer, bufferLen);
        }

        public SidTuneInfo getInfo()
        {
            return info;//.get();
        }

        public SidTuneInfo getInfo(int songNum)
        {
            selectSong(songNum);
            return info;//.get();
        }

        public int selectSong(int selectedSong)
        {
            // Check whether selected song instanceof valid, use start song if not
            int song = (selectedSong == 0 || selectedSong > info.m_songs) ? info.m_startSong : selectedSong;

            // Copy any song-specific variable information
            // such a speed/clock setting to the info structure.
            info.m_currentSong = song;

            // Retrieve song speed definition.
            switch (info.m_compatibility)
            {
                case COMPATIBILITY_R64:
                    info.m_songSpeed = SidTuneInfo.SPEED_CIA_1A;
                    break;
                case COMPATIBILITY_PSID:
                    // This does not take into account the PlaySID bug upon evaluating the
                    // SPEED field. It would most likely break compatibility to lots of
                    // sidtunes, which have been converted from .SID format and vice versa.
                    // The .SID format does the bit-wise/song-wise evaluation of the SPEED
                    // value correctly, like it instanceof described : the PlaySID documentation.
                    info.m_songSpeed = songSpeed[(song - 1) & 31];
                    break;
                default:
                    info.m_songSpeed = songSpeed[song - 1];
                    break;
            }

            info.m_clockSpeed = SidTuneInfo.clock_t.values()[(int) clockSpeed[song - 1]];

            return info.m_currentSong;
        }

        // ------------------------------------------------- private member functions

        public void placeSidTuneInC64mem( sidmemory mem)
        {
            // The Basic ROM sets these values on loading a file.
            // Program end address
            short start = info.m_loadAddr;
            short end = (short)(start + info.m_c64dataLen);
            mem.writeMemWord((byte) 0x2d, end); // Variables start
            mem.writeMemWord((byte) 0x2f, end); // Arrays start
            mem.writeMemWord((byte) 0x31, end); // Strings start
            mem.writeMemWord((byte) 0xac, start);
            mem.writeMemWord((byte) 0xae, end);

            // Copy data from cache to the correct destination.
            mem.fillRam(info.m_loadAddr,ByteBuffer.wrap(mdsound.Common.toByteArray(cache), fileOffset, info.m_c64dataLen), info.m_c64dataLen);
        }

        protected void loadFile(String fileName,List<Byte> bufferRef)
        {
            bufferRef = null;
            try
            {
                try (FileStream inFile = new FileStream(fileName, FileMode.Open, FileAccess.Read))
                {
                    inFile.seek(0, SeekOrigin.End);
                    long fileLen = inFile.getPosition();
                    if (fileLen < 0) throw new loadError(ERR_EMPTY);
                    inFile.seek(0, SeekOrigin.Begin);

                    byte[] fileBuf = new byte[(int)fileLen];
                    inFile.read(fileBuf, 0, (int)fileLen);
                    bufferRef = mdplayer.Common.toArray(fileBuf);
                }
            }
            catch (Exception e)
            {
                throw new loadError(ERR_CANT_OPEN_FILE);
            }

            //std::ifstream inFile(fileName, std::ifstream::binary);

            //if (!inFile.is_open())
            //{
            //    throw new loadError(ERR_CANT_OPEN_FILE);
            //}

            //inFile.seekg(0, inFile.end);
            ////int fileLen = inFile.tellg();

            //if (fileLen <= 0)
            //{
            //    throw new loadError(ERR_EMPTY);
            //}

            //inFile.seekg(0, inFile.beg);

            ////List<Byte> fileBuf;
            //fileBuf.reserve(fileLen);

            //try
            //{
            //    fileBuf.assign(std::istreambuf_iterator<char>(inFile), std::istreambuf_iterator<char>());
            //}
            //catch (Exception ex)
            //{
            //    throw new loadError(ex.Message);
            //}

            //if (inFile.bad())
            //{
            //    throw new loadError(ERR_CANT_LOAD_FILE);
            //}

            //inFile.close();

            //bufferRef.swap(fileBuf);
        }

        protected SidTuneBase()
        {
            info = new SidTuneInfoImpl();
            fileOffset = 0;
            // Initialize the Object with some safe defaults.
            for (int si = 0; si < MAX_SONGS; si++)
            {
                songSpeed[si] = (byte)info.m_songSpeed;
                clockSpeed[si] = (byte)info.m_clockSpeed.ordinal();
            }
        }

// #if !SIDTUNE_NO_STDIN_LOADER

        private SidTuneBase getFromStdIn() throws IOException {
            List<Byte> fileBuf = new ArrayList<>();

            // We only read as much as fits : the buffer.
            // This way we avoid choking on huge data.
            int datb;
            while ((datb = System.in.read()) != -1 && fileBuf.size() < MAX_FILELEN)
            {
                fileBuf.add((byte)datb);
            }

            return getFromBuffer(mdsound.Common.toByteArray(fileBuf), (int)fileBuf.size());
        }

// #endif

        private SidTuneBase getFromBuffer(byte[] buffer, int bufferLen)
        {
            if (buffer == null || bufferLen == 0)
            {
                throw new loadError(ERR_EMPTY);
            }

            if (bufferLen > MAX_FILELEN)
            {
                throw new loadError(ERR_FILE_TOO_LONG);
            }

            byte[] buf1 = buffer;//, buffer +bufferLen);

            // Here test for the possible single file formats.
            SidTuneBase s = PSID.load(buf1);
            if (s == null) s = (new MUS()).load(buf1, true);
            if (s == null) throw new loadError(ERR_UNRECOGNIZED_FORMAT);

            List<Byte> lstBuf1 = mdplayer.Common.toArray(buf1);
            s.acceptSidTune("-", "-",lstBuf1, false);
            return s;
        }

        protected void acceptSidTune(String dataFileName, String infoFileName,List<Byte> buf, Boolean isSlashedFileName)
        {
            // Make a copy of the data file name and path, if available.
            if (dataFileName != null)
            {
                int fileNamePos = (int)(isSlashedFileName ?
                    SidTuneTools.slashedFileNameWithoutPath(dataFileName) :
                    SidTuneTools.fileNameWithoutPath(dataFileName));
                info.m_path = dataFileName.substring(0, fileNamePos);
                info.m_dataFileName = dataFileName.substring(fileNamePos);
            }

            // Make a copy of the info file name, if available.
            if (infoFileName != null)
            {
                int fileNamePos = (int)(isSlashedFileName ?
                    SidTuneTools.slashedFileNameWithoutPath(infoFileName) :
                    SidTuneTools.fileNameWithoutPath(infoFileName));
                info.m_infoFileName = infoFileName.substring(fileNamePos);
            }

            // Fix bad sidtune set up.
            if (info.m_songs > MAX_SONGS)
            {
                info.m_songs = MAX_SONGS;
            }
            else if (info.m_songs == 0)
            {
                info.m_songs = 1;
            }

            if (info.m_startSong == 0
                || info.m_startSong > info.m_songs)
            {
                info.m_startSong = 1;
            }

            info.m_dataFileLen = buf.size();
            info.m_c64dataLen = buf.size() - fileOffset;

            // Calculate any remaining addresses and then
            // confirm all the file details are correct
            resolveAddrs(buf, (int)fileOffset);

            if (checkRelocInfo() == false)
            {
                throw new loadError(ERR_BAD_RELOC);
            }
            if (checkCompatibility() == false)
            {
                throw new loadError(ERR_BAD_ADDR);
            }

            if (info.m_dataFileLen >= 2)
            {
                // We only detect an offset of two. Some position independent
                // sidtunes contain a load address of 0xE000, but are loaded
                // to 0x0FFE and call player at 0x1000.
                info.m_fixLoad = (sidendian.endian_little16(ByteBuffer.wrap(Common.toByteArray(buf), (int)fileOffset, buf.size() - fileOffset)) == (info.m_loadAddr + 2));
            }

            // Check the size of the data.
            if (info.m_c64dataLen > MAX_MEMORY)
            {
                throw new loadError(ERR_DATA_TOO_LONG);
            }
            else if (info.m_c64dataLen == 0)
            {
                throw new loadError(ERR_EMPTY);
            }

            cache = new ArrayList<Byte>(buf);
        }

        private String createNewFileName(byte[] sourceName, byte[] sourceExt)
        {
            String destString = new String(sourceName, StandardCharsets.US_ASCII);
            destString = destString.substring(0, destString.lastIndexOf('.'));
            destString += new String(sourceExt, StandardCharsets.US_ASCII);
            return destString;
        }

        // Initializing the Object based upon what we find : the specified file.

        private SidTuneBase getFromFiles(String fileName, String[] fileNameExtensions, Boolean separatorIsSlash)
        {
            List<Byte> fileBuf1 = new ArrayList<>();

            loadFile(fileName,fileBuf1);

            // File loaded. Now check if it instanceof : a valid single-file-format.
            byte[] aryFileBuf1 = mdsound.Common.toByteArray(fileBuf1);
            SidTuneBase s = PSID.load( aryFileBuf1);
            fileBuf1 = mdplayer.Common.toArray(aryFileBuf1);
            if (s == null)
            {
                // Try some native C64 file formats
                s = (new MUS()).load(mdsound.Common.toByteArray(fileBuf1), true);
                if (s != null)
                {
                    // Try to find second file.
                    String fileName2;
                    int n = 0;
                    while (fileNameExtensions[n] != null)
                    {
                        fileName2 = createNewFileName(fileName.getBytes(StandardCharsets.US_ASCII), fileNameExtensions[n].getBytes(StandardCharsets.US_ASCII));
                        // 1st data file was loaded into "fileBuf1",
                        // so we load the 2nd one into "fileBuf2".
                        // Do not load the first file again if names are equal.
                        if (!fileName.substring(0, fileName2.length()).equals(fileName2.substring(0, fileName2.length())))
                        {
                            try
                            {
                                List<Byte> fileBuf2 = new ArrayList<Byte>();

                                loadFile(fileName2,fileBuf2);
                                // Check if tunes : wrong order and therefore swap them here
                                if (fileNameExtensions[n].equals(".mus"))
                                {
                                    SidTuneBase s2 = (new MUS()).load(mdsound.Common.toByteArray(fileBuf2), mdsound.Common.toByteArray(fileBuf1), 0, true);
                                    if (s2 != null)
                                    {
                                        s2.acceptSidTune(fileName2, fileName,fileBuf2, separatorIsSlash);
                                        return s2;
                                    }
                                }
                                else
                                {
                                    SidTuneBase s2 = (new MUS()).load(mdsound.Common.toByteArray(fileBuf1), true);
                                    if (s2 != null)
                                    {
                                        s2.acceptSidTune(fileName, fileName2,fileBuf1, separatorIsSlash);
                                        return s2;
                                    }
                                }
                                // The first tune loaded ok, so ignore errors on the
                                // second tune, may find an ok one later
                            }
                            catch (loadError e)
                            {
                            }
                        }
                        n++;
                    }
                }
            }
            if (s == null) s = p00.load(fileName, aryFileBuf1);
            if (s == null) s = prg.load(fileName, aryFileBuf1);
            if (s == null) throw new loadError(ERR_UNRECOGNIZED_FORMAT);

            s.acceptSidTune(fileName, null,fileBuf1, separatorIsSlash);
            return s;
        }

        protected void convertOldStyleSpeedToTables(int speed, long clock /*= (long)SidTuneInfo.clock_t.CLOCK_PAL*/)
        {
            // Create the speed/clock setting tables.
            //
            // This routine implements the PSIDv2NG compliant speed conversion. All tunes
            // above 32 use the same song speed as tune 32
            // NOTE: The cast here instanceof used to avoid undefined references
            // as the std::min function takes its parameters by reference
            int toDo = Math.min(info.m_songs, (int)MAX_SONGS);
            for (int s = 0; s < toDo; s++)
            {
                clockSpeed[s] = clock;
                songSpeed[s] = (byte)((speed & 1) != 0 ? SidTuneInfo.SPEED_CIA_1A : SidTuneInfo.SPEED_VBI);

                if (s < 31)
                {
                    speed >>= 1;
                }
            }
        }

        protected Boolean checkRelocInfo()
        {
            // Fix relocation information
            if (info.m_relocStartPage == 0xFF)
            {
                info.m_relocPages = 0;
                return true;
            }
            else if (info.m_relocPages == 0)
            {
                info.m_relocStartPage = 0;
                return true;
            }

            // Calculate start/end page
            byte startp = info.m_relocStartPage;
            byte endp = (byte)((startp + info.m_relocPages - 1) & 0xff);
            if (endp < startp)
            {
                return false;
            }

            {    // Check against load range
                byte startlp = (byte)(info.m_loadAddr >> 8);
                byte endlp = (byte)(startlp + (byte)((info.m_c64dataLen - 1) >> 8));

                if (((startp <= startlp) && (endp >= startlp))
                    || ((startp <= endlp) && (endp >= endlp)))
                {
                    return false;
                }
            }

            // Check that the relocation information does not use the following
            // memory areas: 0x0000-0x03FF, 0xA000-0xBFFF and 0xD000-0xFFFF
            if ((startp < 0x04)
                || (((byte) 0xa0 <= startp) && (startp <= (byte) 0xbf))
                || (startp >= (byte) 0xd0)
                || (((byte) 0xa0 <= endp) && (endp <= (byte) 0xbf))
                || (endp >= (byte) 0xd0))
            {
                return false;
            }

            return true;
        }

        protected void resolveAddrs(List<Byte> c64data, int ptr/* = 0*/)
        {
            // Originally used as a first attempt at an RSID
            // style format. Now reserved for future use
            if (info.m_playAddr == (short) 0xffff)
            {
                info.m_playAddr = 0;
            }

            // loadAddr = 0 means, the address instanceof stored : front of the C64 data.
            if (info.m_loadAddr == 0)
            {
                if (info.m_c64dataLen < 2)
                {
                    throw new loadError(ERR_CORRUPT);
                }

                info.m_loadAddr = sidendian.endian_16(c64data.get(ptr+1), c64data.get(ptr+0));
                fileOffset += 2;
                info.m_c64dataLen -= 2;
            }

            if (info.m_compatibility == SidTuneInfo.compatibility_t.COMPATIBILITY_BASIC)
            {
                if (info.m_initAddr != 0)
                {
                    throw new loadError(ERR_BAD_ADDR);
                }
            }
            else if (info.m_initAddr == 0)
            {
                info.m_initAddr = info.m_loadAddr;
            }
        }

        protected Boolean checkCompatibility()
        {
            if (info.m_compatibility == SidTuneInfo.compatibility_t.COMPATIBILITY_R64)
            {
                // Check valid init address
                switch (info.m_initAddr >> 12)
                {
                    case 0x0A:
                    case 0x0B:
                    case 0x0D:
                    case 0x0E:
                    case 0x0F:
                        return false;
                    default:
                        if ((info.m_initAddr < info.m_loadAddr)
                            || (info.m_initAddr > (info.m_loadAddr + info.m_c64dataLen - 1)))
                        {
                            return false;
                        }
                        break;
                }

                // Check tune instanceof loadable on a real C64
                if (info.m_loadAddr < SIDTUNE_R64_MIN_LOAD_ADDR)
                {
                    return false;
                }
            }

            return true;
        }

        protected String petsciiToAscii( ByteBuffer spPet)
        {
            List<Byte> buffer = new ArrayList<Byte>();

            do
            {
                byte petsciiChar = spPet.array()[spPet.arrayOffset()];
                spPet.position(spPet.position() + 1);

                if ((petsciiChar == 0x00) || (petsciiChar == 0x0d))
                    break;

                // If character instanceof 0x9d (left arrow key) then move back.
                if ((petsciiChar == (byte) 0x9d) && buffer.size() != 0)
                {
                    buffer.remove(buffer.size() - 1);
                }
                else
                {
                    // ASCII CHR$ conversion
                    byte asciiChar = CHR_tab[petsciiChar];
                    if ((asciiChar >= 0x20) && (buffer.size() <= 31))
                        buffer.add(asciiChar);
                }
            }
            while (spPet.array().length > spPet.arrayOffset());

            return new String(mdsound.Common.toByteArray(buffer), StandardCharsets.US_ASCII);
        }

    }
