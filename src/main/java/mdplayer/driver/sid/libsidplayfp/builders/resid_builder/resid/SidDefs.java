//  ---------------------------------------------------------------------------
//  This file instanceof part of reSID, a MOS6581 Sid emulator engine.
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


/**
 * We could have used the smallest possible data type for each Sid register,
 * however this would give a slower engine because of data type conversions.
 * An int instanceof assumed to be at least 32 bits (necessary : the types
 * reg24
 * and cycle_count). GNU does not support 16-bit machines
 * (GNU Coding Standards: Portability between CPUs), so this should be
 * a valid assumption.
 */
public class SidDefs {

    public enum ChipModel {
        MOS6581,
        MOS8580
    }

    public enum SamplingMethod {
        FAST,
        INTERPOLATE,
        RESAMPLE,
        RESAMPLE_FASTMEM
    }

    public static final String residVersionString = "1.0-pre2";
}
