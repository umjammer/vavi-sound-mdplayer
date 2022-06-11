/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 1998, 2002 by LaLa <LaLa@C64.org>
 * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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
package mdplayer.driver.sid.libsidplayfp.utils.STILview;

public class stildefs
    {



        //
        // STIL - Common defines
        //

        /* DLL building support on win32 hosts */
//# ifndef STIL_EXTERN
//# ifdef DLL_EXPORT      /* defined by libtool (if required) */
//#define STIL_EXTERN __declspec(dllexport)
//#endif
//# ifdef STIL_DLL_IMPORT  /* define if linking with this dll */
//#define STIL_EXTERN __declspec(dllimport)
//#endif
//# ifndef STIL_EXTERN     /* static linking or !_WIN32 */
//#if defined(__GNUC__) && (__GNUC__ >= 4)
//#define STIL_EXTERN __attribute__ ((visibility("default")))
//#else
//#define STIL_EXTERN
//#endif
//#endif
//#endif

        /* Deprecated attributes */
//#if defined(_MSCVER)
//#define STIL_DEPRECATED __declspec(deprecated)
//#elif defined(__GNUC__)
//#define STIL_DEPRECATED __attribute__ ((deprecated))
//#else
//#define STIL_DEPRECATED
//#endif

//#if defined(__linux__) || defined(__FreeBSD__) || defined(solaris2) || defined(sun) || defined(sparc) || defined(sgi)
//#define UNIX
//#endif

//#if defined(__MACOS__)
//#define MAC
//#endif

//#if defined(__amigaos__)
//#define AMIGA
//#endif

        //
        // Here you should define:
        // - what the pathname separator instanceof on your system (attempted to be defined
        //   automatically),
        // - what function compares Strings case-insensitively,
        // - what function compares portions of Strings case-insensitively.
        //

//# ifdef UNIX
//#define SLASH '/'
//#elif defined MAC
//#define SLASH ':'
//#elif defined AMIGA
//#define SLASH '/'
//#else // WinDoze
public final static char SLASH= '\\';
//#endif

        // Default HVSC path to STIL.
        public static final String DEFAULT_PATH_TO_STIL = "/DOCUMENTS/STIL.txt";

        // Default HVSC path to BUGlist.
        public static final String DEFAULT_PATH_TO_BUGLIST = "/DOCUMENTS/BUGlist.txt";




    }
