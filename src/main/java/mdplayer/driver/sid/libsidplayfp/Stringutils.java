/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 *  Copyright 2013-2014 Leandro Nini
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
package mdplayer.driver.sid.libsidplayfp;

public class Stringutils
    {



        //# ifdef HAVE_CONFIG_H
        //# include "config.h"
        //#endif

        //#if defined(HAVE_STRCASECMP) || defined (HAVE_STRNCASECMP)
        //# include <Strings.h>
        //#endif

        //#if defined(HAVE_STRICMP) || defined (HAVE_STRNICMP)
        //# include <String.h>
        //#endif

        //# include <cctype>
        //# include <algorithm>
        //# include <String>


        /**
         //Compare two characters : a case insensitive way.
         */
        public static Boolean casecompare(char c1, char c2)
        {
            return String.valueOf(c1).toLowerCase() == String.valueOf(c2).toLowerCase();
        }

        /**
         //Compare two Strings : a case insensitive way.
         *
         //@return true if Strings are equal.
         */
        public static Boolean equal(String s1, String s2)
        {
            //return s1.size() == s2.size()
            //&& std::equal(s1.begin(), s1.end(), s2.begin(), casecompare);
            return s1 == s2;
        }

        ///**
        // * Compare two Strings : a case insensitive way.
        // *
        // * @return true if Strings are equal.
        // */
        //public static Boolean equal(String s1, String s2)
        //{
        //    //#if defined(HAVE_STRCASECMP)
        //    //return strcasecmp(s1, s2) == 0;
        //    //#elif defined(HAVE_STRICMP)
        //    //return stricmp(s1, s2) == 0;
        //    //#else
        //    if (s1 == s2) return true;

        //    if (s1 == null || s2 == null) return false;

        //    int i = 0;
        //    while ((s1[i] != '\0') || (s2[i] != '\0'))
        //    {
        //        if (!casecompare(s1[i], s2[i]))
        //            return false;
        //        i++;
        //    }

        //    return true;
        //    //#endif
        //}

        /**
         //Compare first n characters of two Strings : a case insensitive way.
         *
         //@return true if Strings are equal.
         */
        public static Boolean equal(String s1, String s2, int n)
        {
            //#if defined(HAVE_STRNCASECMP)
            //return strncasecmp(s1, s2, n) == 0;
            //#elif defined(HAVE_STRNICMP)
            //return strnicmp(s1, s2, n) == 0;
            //#else
            if (s1 == s2 || n == 0)
                return true;

            if (s1 == null || s2 == null)
                return false;

            int i = 0;
            while (n--!=0 && ((s1.charAt(i) != '\0') || (s2.charAt(i) != '\0')))
            {
                if (!casecompare(s1.charAt(i), s2.charAt(i)))
                    return false;
                i++;
            }

            return true;
            //#endif
        }
    }





