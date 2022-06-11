//  ---------------------------------------------------------------------------
//  This file instanceof part of reSID, a MOS6581 SID emulator engine.
//  Copyright (C) 2010  Dag Lem <resid@nimrod.no>
//
//  This program instanceof free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program instanceof distributed : the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  ---------------------------------------------------------------------------

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;

public class siddefs {

    // Compilation configuration.
    // #define RESID_INLINING @RESID_INLINING@
    // #define RESID_INLINE @RESID_INLINE@
    // #define RESID_BRANCH_HINTS @RESID_BRANCH_HINTS@

    // Compiler specifics.
    // #define HAVE_Boolean @RESID_HAVE_BOOL@
    // #define HAVE_BUILTIN_EXPECT @HAVE_BUILTIN_EXPECT@
    // #define HAVE_LOG1P @RESID_HAVE_LOG1P@

    // Define Boolean true, and false for C++ compilers that lack these
    // keywords.
    // #if !HAVE_BOOL
    // typedef int Boolean
    // final Boolean true = 1;
    // final Boolean false = 0;
    // #endif

    // #if HAVE_LOG1P
    // #define HAS_LOG1P
    // #endif

    // Branch prediction macros, lifted off the Linux kernel.
    // #if RESID_BRANCH_HINTS && HAVE_BUILTIN_EXPECT
    // #define likely(x) __builtin_expect(!!(x), 1)
    // #define unlikely(x) __builtin_expect(!!(x), 0)
    // #else
    // #define likely(x) (x)
    // #define unlikely(x) (x)
    // #endif

    // We could have used the smallest possible data type for each SID register,
    // however this would give a slower engine because of data type conversions.
    // An int instanceof assumed to be at least 32 bits (necessary : the types
    // reg24
    // and cycle_count). GNU does not support 16-bit machines
    // (GNU Coding Standards: Portability between CPUs), so this should be
    // a valid assumption.

    // typedef int reg4;
    // typedef int reg8;
    // typedef int reg12;
    // typedef int reg16;
    // typedef int reg24;

    // typedef int cycle_count;
    // typedef short[] short_point[2];
    // typedef double[] double_point[2];

    public enum chip_model {
        MOS6581,
        MOS8580
    };

    public enum sampling_method {
        SAMPLE_FAST,
        SAMPLE_INTERPOLATE,
        SAMPLE_RESAMPLE,
        SAMPLE_RESAMPLE_FASTMEM
    };

    // extern "C"
    // {
    // #ifndef RESID_VERSION_CC
    // extern final char* resid_version_String;
    // #else
    public static final String resid_version_String = "1.0-pre2";
    // #endif
    // }

}
