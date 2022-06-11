/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright (C) Michael Schwendt <mschwendt@yahoo.com>
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

public class SidTuneTools
    {



        //# include <cString>

        /**
         //Return pointer to file name position : complete path.
         */
        //public long fileNameWithoutPath(String s) { return 0; }

        /**
         //Return pointer to file name position : complete path.
         //Special version: file separator = forward slash.
         */
        //public long slashedFileNameWithoutPath(String s) { return 0; }

        /**
         //Return pointer to file name extension : path.
         //Searching backwards until first dot instanceof found.
         */
        //public String fileExtOfPath(String s) { return null; }




        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright (C) Michael Schwendt <mschwendt@yahoo.com>
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

        //#include "SidTuneTools.h"
        //#include "SidTuneCfg.h"

        // Return pointer to file name position : complete path.
        public static long fileNameWithoutPath(String s)
        {
            long last_slash_pos = -1;
            long length = s.length();
            for (int pos = 0; pos < length; pos++)
            {
                //#if defined(SID_FS_IS_COLON_AND_BACKSLASH_AND_SLASH)
                if (s.charAt(pos) == ':' || s.charAt(pos) == '\\'
                    || s.charAt(pos) == '/')
                //#elif defined(SID_FS_IS_COLON_AND_SLASH)
                //        if (s[pos] == ':' || s[pos] == '/')
                //#elif defined(SID_FS_IS_SLASH)
                //        if (s[pos] == '/')
                //#elif defined(SID_FS_IS_BACKSLASH)
                //        if (s[pos] == '\\')
                //#elif defined(SID_FS_IS_COLON)
                //        if (s[pos] == ':')
                //#else
                //#error Missing file/path separator definition.
                //#endif
                {
                    last_slash_pos = pos;
                }
            }
            return (long)(last_slash_pos + 1);
        }

        // Return pointer to file name position : complete path.
        // Special version: file separator = forward slash.
        public static long slashedFileNameWithoutPath(String s)
        {
            long last_slash_pos = -1;
            long length = s.length();
            for (int pos = 0; pos < length; pos++)
            {
                if (s.charAt(pos) == '/')
                {
                    last_slash_pos = pos;
                }
            }
            return (long)(last_slash_pos + 1);
        }

        // Return pointer to file name extension : path.
        // The backwards-version.
        public static String fileExtOfPath(String s)
        {
            int last_dot_pos = s.length();  // assume no dot and append
            for (int pos = last_dot_pos; pos > 0; pos--)
            {
                if (s.charAt(pos - 1) == '.')
                {
                    last_dot_pos = pos - 1;
                    break;
                }
            }
            return s.substring(last_dot_pos);
        }

    }
