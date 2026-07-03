/*
 * ST-Sound ( YM files player library )
 *
 * Copyright (C) 1995-1999 Arnaud Carre ( http://leonard.oxg.free.fr )
 *
 * This file is part of ST-Sound
 *
 * ST-Sound is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ST-Sound is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ST-Sound; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mdplayer.driver.ym;

import java.io.ByteArrayOutputStream;
import java.util.Scanner;


final class DigiDrum {

    static final int MAX_DIGIDRUM = 40;

    /** */
    static final byte[][] sampleAddress = new byte[MAX_DIGIDRUM][];

    /** */
    static final int[] sampleLen = {
            631, 631, 490, 490, 699, 505, 727, 480,
            2108, 4231, 378, 1527, 258, 258, 451, 1795,
            271, 633, 1379, 147, 139, 85, 150, 507,
            230, 120, 271, 293, 391, 391, 391, 407,
            407, 407, 317, 407, 311, 459, 329, 656
    };

    static {
        try {
            for (int i = 0; i < MAX_DIGIDRUM; i++) {
                Scanner s = new Scanner(DigiDrum.class.getResourceAsStream("sample%02d.dat".formatted(i)));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (s.hasNextByte(16)) {
                    baos.write(s.nextByte(16) & 0xff);
                }
                sampleAddress[i] = baos.toByteArray();
                assert baos.size() == sampleLen[i];
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
