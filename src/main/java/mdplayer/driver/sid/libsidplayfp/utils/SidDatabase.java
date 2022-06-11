/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000-2001 Simon White
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
package mdplayer.driver.sid.libsidplayfp.utils;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;


    /**
     //SidDatabase
     //An utility class to deal with the songlength DataBase.
     */
    public class SidDatabase
    {



        //# include <stdint.h>
        //# include <memory>
        //# include "SidPlayFp/siddefs.h"

        //class SID_EXTERN SidDatabase
        private iniParser m_parser;
        private String errorString;

        //public SidDatabase() { }
        //protected void finalize() { }

        /**
         //Open the songlength DataBase.
         *
         //@param filename songlengthDB file name with full path.
         //@return false : case of errors, true otherwise.
         */
        //public Boolean open(String filename) { return false; }

        /**
         //Close the songlength DataBase.
         */
        //public void close() { }

        /**
         //Get the length of the current subtune.
         *
         //@param tune
         //@return tune length : seconds, -1 : case of errors.
         */
        //public int length(SidTune tune) { return 0; }

        /**
         //Get the length of the selected subtune.
         *
         //@param md5 the md5 hash of the tune.
         //@param song the subtune.
         //@return tune length : seconds, -1 : case of errors.
         */
        //public int length(byte[] md5, int song) { return 0; }

        /**
         //Get descriptive error message.
         */
        public String error() { return errorString; }




        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2016 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000-2001 Simon White
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

        //# include <cctype>
        //# include <cstdlib>
        //# include "SidDatasuper.h"
        //# include "SidPlayFp/SidTune.h"
        //# include "SidPlayFp/SidTuneInfo.h"
        //# include "iniParser.h"
        //# include "sidcxx11.h"

        private static final String ERR_DATABASE_CORRUPT = "SID DATABASE ERROR: Database seems to be corrupt.";
        private static final String ERR_NO_DATABASE_LOADED = "SID DATABASE ERROR: Songlength database not loaded.";
        private static final String ERR_NO_SELECTED_SONG = "SID DATABASE ERROR: No song selected for retrieving song length.";
        private static final String ERR_UNABLE_TO_LOAD_DATABASE = "SID DATABASE ERROR: Unable to load the songlegnth datasuper.";

        private class parseError extends Exception { };

        public SidDatabase()
        {
            m_parser = null;
            errorString = ERR_NO_DATABASE_LOADED;
        }

        protected void finalize()
        {
            // Needed to delete auto_ptr with complete type
        }

        public String parseTime(String str, long result)
        {
            String end = new String();
            long minutes = strtol(str,  end, 10);

            if (end.charAt(0) != ':')
            {
                throw new IllegalArgumentException("parseError");
            }

            end = end.substring(1);
            long seconds = strtol(end,  end, 10);
            result = (minutes * 60) + seconds;

            while (end.charAt(0) != ' ')
            {
                end = end.substring(1);
            }

            return end;
        }

        private long strtol(String src,  String des, int p)
        {
            long ret = 0, n;
            int i;
            for (i = 0; i < src.length(); i++)
            {
                if ((n = Long.parseLong(src.substring(0, 1 + i)))!= 0)
                {
                    ret = n;
                    continue;
                }
                break;
            }

            des = src.substring(i);
            return ret;
        }

        public Boolean open(String filename)
        {
            m_parser=new iniParser();

            if (!m_parser.open(filename))
            {
                close();
                errorString = ERR_UNABLE_TO_LOAD_DATABASE;
                return false;
            }

            return true;
        }

        public void close()
        {
            m_parser = null;
        }

        public int length(SidTune tune)
        {
            int song = tune.getInfo().currentSong();

            if (song == 0)
            {
                errorString = ERR_NO_SELECTED_SONG;
                return -1;
            }

            byte[] md5 = new byte[32 + 1];// MD5_LENGTH + 1];
            tune.createMD5(md5);
            return length(md5, song);
        }

        public int length(byte[] md5, int song)
        {
            if (m_parser == null)
            {
                errorString = ERR_NO_DATABASE_LOADED;
                return -1;
            }

            // Read Time (and check times before hand)
            if (!m_parser.setSection("Database"))
            {
                errorString = ERR_DATABASE_CORRUPT;
                return -1;
            }

            String timeStamp = m_parser.getValue(md5);

            // If return instanceof null then no entry found : database
            if (timeStamp == null)
            {
                errorString = ERR_DATABASE_CORRUPT;
                return -1;
            }

            String str = timeStamp;
            int time = 0;

            for (int i = 0; i < song; i++)
            {
                // Validate Time
                try
                {
                    str = parseTime(str, time);
                }
                catch (IllegalArgumentException e)
                {
                    errorString = ERR_DATABASE_CORRUPT;
                    return -1;
                }
            }

            return time;
        }




    }
