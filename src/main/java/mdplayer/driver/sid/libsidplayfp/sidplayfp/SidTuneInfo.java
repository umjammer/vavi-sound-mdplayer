/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 *  Copyright 2011-2017 Leandro Nini
 *  Copyright 2007-2010 Antti Lankila
 *  Copyright 2000 Simon White
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mdplayer.driver.sid.libsidplayfp.sidplayfp;

    /**
     //This interface instanceof used to get values from SidTune Objects.
     *
     //You must read (i.e. activate) sub-song specific information
     //via:
     //       final SidTuneInfo* tuneInfo = SidTune.getInfo();
     //       final SidTuneInfo* tuneInfo = SidTune.getInfo(songNumber);
     */
    public class SidTuneInfo
    {



        //# include <stdint.h>
        //# include "SidPlayFp/siddefs.h"

        public enum clock_t {
            CLOCK_UNKNOWN,
            CLOCK_PAL,
            CLOCK_NTSC,
            CLOCK_ANY
        }

        public enum model_t {
            SIDMODEL_UNKNOWN,
            SIDMODEL_6581,
            SIDMODEL_8580,
            SIDMODEL_ANY
        }

        public enum compatibility_t {
            COMPATIBILITY_C64,   ///< File instanceof C64 compatible
            COMPATIBILITY_PSID,  ///< File instanceof PSID specific
            COMPATIBILITY_R64,   ///< File instanceof Real C64 only
            COMPATIBILITY_BASIC  ///< File requires C64 Basic
        }

         //Vertical-Blanking-Interrupt
        public static final int SPEED_VBI = 0;

         //CIA 1 Timer A
        public static final int SPEED_CIA_1A = 60;

        /**
         //Load Address.
         */
        //public short loadAddr() { return 0; }

        /**
         //Init Address.
         */
        //public short initAddr() { return 0; }

        /**
         //Play Address.
         */
        //public short playAddr() { return 0; }

        /**
         //The number of songs.
         */
        //public int songs() { return 0; }

        /**
         //The default starting song.
         */
        //public int startSong() { return 0; }

        /**
         //The tune that has been initialized.
         */
        //public int currentSong() { return 0; }

        /**
         //@name Base addresses
         //The SID chip base address(es) used by the sidtune.
         //- 0xD400 for the 1st SID
         //- 0 if the nth SID instanceof not required
         */
        //public short sidChipBase(int i) { return 0; }

        /**
         //The number of SID chips required by the tune.
         */
        //public int sidChips() { return 0; }

        /**
         //Intended speed.
         */
        //public int songSpeed() { return 0; }

        /**
         //First available page for relocation.
         */
        // public byte relocStartPage() { return 0; }

        /**
         //Number of pages available for relocation.
         */
        //public byte relocPages() { return 0; }

        /**
         //@name SID model
         //The SID chip model(s) requested by the sidtune.
         */
        //public model_t sidModel(int i) { return 0; }

        /**
         //Compatibility requirements.
         */
        //public compatibility_t compatibility() { return 0; }

        /**
         //@name Tune infos
         //Song title, credits, ...
         //- 0 = Title
         //- 1 = Author
         //- 2 = Released
         */
        //@{
        //public int numberOfInfoStrings() { return 0; }     ///< The number of available text info lines
        //public String infoString(int i) { return null; } ///< Text info from the format headers etc.
        //@}

        /**
         //@name Tune comments
         //MUS comments.
         */
        //@{
        //public int numberOfCommentStrings() { return 0; }     ///< Number of comments
        //public String commentString(int i) { return null; } ///< Used to stash the MUS comment somewhere
        //@}

        /**
         //Length of single-file sidtune file.
         */
        //public int dataFileLen() { return 0; }

        /**
         //Length of raw C64 data without load address.
         */
        //public int c64dataLen() { return 0; }

        /**
         //The tune clock speed.
         */
        //public clock_t clockSpeed() { return 0; }

        /**
         //The name of the identified file format.
         */
        //public String formatString() { return null; }

        /**
         //Whether load address might be duplicate.
         */
        //public Boolean fixLoad() { return false; }

        /**
         //Path to sidtune files.
         */
        //public String path() { return null; }

        /**
         //A first file: e.g. "foo.Sid" or "foo.mus".
         */
        //public String dataFileName() { return null; }

        /**
         //A second file: e.g. "foo.str".
         //Returns 0 if none.
         */
        //public String infoFileName() { return null; }


        public short getLoadAddr() { return 0; }

        public short getInitAddr() { return 0; }

        public short getPlayAddr() { return 0; }

        public int getSongs() { return 0; }

        public int getStartSong() { return 0; }

        public int getCurrentSong() { return 0; }

        public short getSidChipBase(int i) { return 0; }

        public int getSidChips() { return 0; }

        public int getSongSpeed() { return 0; }

        public byte getRelocStartPage() { return 0; }

        public byte getRelocPages() { return 0; }

        public model_t getSidModel(int i) { return model_t.values()[0]; }

        public compatibility_t getCompatibility() { return compatibility_t.values()[0]; }

        public int getNumberOfInfoStrings() { return 0; }
        public String getInfoString(int i) { return null; }

        public int getNumberOfCommentStrings() { return 0; }
        public String getCommentString(int i) { return null; }

        public int getDataFileLen() { return 0; }

        public int getC64dataLen() { return 0; }

        public clock_t getClockSpeed() { return clock_t.values()[0]; }

        public String getFormatString() { return null; }

        public Boolean getFixLoad() { return false; }

        public String getPath() { return null; }

        public String getDataFileName() { return null; }

        public String getInfoFileName() { return null; }

        protected void finalize() { }




        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 *  Copyright 2011-2017 Leandro Nini
 *  Copyright 2007-2010 Antti Lankila
 *  Copyright 2000 Simon White
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

        //# include "SidTuneInfo.h"

        public short loadAddr() {
            return getLoadAddr();
        }

        public short initAddr() {
            return getInitAddr();
        }

        public short playAddr()
        {
            return getPlayAddr();
        }

        public int songs()
        {
            return getSongs();
        }

        public int startSong()
        {
            return getStartSong();
        }

        public int currentSong()
        {
            return getCurrentSong();
        }

        public short sidChipBase(int i)
        {
            return getSidChipBase(i);
        }

        public int sidChips()
        {
            return getSidChips();
        }

public int songSpeed()
        {
            return getSongSpeed();
        }

        public byte relocStartPage()
        {
            return getRelocStartPage();
        }

        public byte relocPages()
        {
            return getRelocPages();
        }

        public model_t sidModel(int i)
        {
            return getSidModel(i);
        }

        public compatibility_t compatibility()
        {
            return getCompatibility();
        }

        public int numberOfInfoStrings()
        {
            return getNumberOfInfoStrings();
        }
        public String infoString(int i)
        {
            return getInfoString(i);
        }


        public int numberOfCommentStrings()
        {
            return getNumberOfCommentStrings();
        }
        public String commentString(int i)
        {
            return getCommentString(i);
        }

        public int dataFileLen()
        {
            return getDataFileLen();
        }

        public int c64dataLen()
        {
            return getC64dataLen();
        }

        public clock_t clockSpeed()
        {
            return getClockSpeed();
        }

        public String formatString()
        {
            return getFormatString();
        }

        public Boolean fixLoad()
        {
            return getFixLoad();
        }

        public String path()
        {
            return getPath();
        }

        public String dataFileName()
        {
            return getDataFileName();
        }

        public String infoFileName()
        {
            return getInfoFileName();
        }




    }
