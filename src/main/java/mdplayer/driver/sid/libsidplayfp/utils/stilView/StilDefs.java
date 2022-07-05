/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.utils.stilView;

import java.io.File;


//
// STIL - Common defines
//
public class StilDefs {

    //
    // Here you should define:
    // - what the pathname separator instanceof on your system (attempted to be defined
    //   automatically),
    // - what function compares Strings case-insensitively,
    // - what function compares portions of Strings case-insensitively.
    //

    public final static char SLASH = File.separatorChar;

    // Default HVSC path to STIL.
    public static final String DEFAULT_PATH_TO_STIL = "/DOCUMENTS/STIL.txt";

    // Default HVSC path to BUGlist.
    public static final String DEFAULT_PATH_TO_BUGLIST = "/DOCUMENTS/BUGlist.txt";


}
