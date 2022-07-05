/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2009-2014 VICE Project
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

package mdplayer.driver.sid.libsidplayfp.c64.cia;

import mdplayer.driver.sid.libsidplayfp.c64.cia.InterruptSource.INTERRUPT;


public class SerialPort {

    private InterruptSource interruptSource;
    private int count;
    private boolean buffered;
    private byte out_;

    public SerialPort(InterruptSource intSource) {
        interruptSource = intSource;
    }

    public void reset() {
        out_ = 0;
        count = 0;
        buffered = false;
    }

    public void setBuffered() {
        buffered = true;
    }

    public void handle(byte serialDataReg) {
        if (count != 0 && --count == 0) {
            interruptSource.trigger((byte) INTERRUPT.INTERRUPT_SP.v);
        }

        if (count == 0 && buffered) {
            out_ = serialDataReg;
            buffered = false;
            count = 16;
            // Output rate 8 bits at ta / 2
        }
    }
}
