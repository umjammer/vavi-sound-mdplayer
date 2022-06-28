/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2014 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2009-2014 VICE Project
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2001 Simon White
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

package mdplayer.driver.sid.libsidplayfp.c64.vic_ii;

import mdplayer.driver.sid.mem;


/**
 * Sprites handling.
 */
public class Sprites {

    public static final int SPRITES = 8;
    private byte enable, yExpansion;
    private byte expFlop;
    private byte dma;
    private byte[] mcBase = new byte[SPRITES];
    private byte[] mc = new byte[SPRITES];

    public Sprites(byte[] regs) {
        enable = regs[0x15];
        yExpansion = regs[0x17];
    }

    public void reset() {
        expFlop = (byte) 0xff;
        dma = 0;

        mem.memset(mcBase, (byte) 0, mcBase.length);
        mem.memset(mc, (byte) 0, mc.length);
    }

    /**
     * Update mc values : one pass //after the dma has been processed
     */
    public void updateMc() {
        byte mask = 1;
        for (int i = 0; i < SPRITES; i++, mask <<= 1) {
            if ((dma & mask) != 0)
                mc[i] = (byte) ((mc[i] + 3) & 0x3f);
        }
    }

    /**
     * Update mc base value.
     */
    public void updateMcBase() {
        byte mask = 1;
        for (int i = 0; i < SPRITES; i++, mask <<= 1) {
            if ((expFlop & mask) != 0) {
                mcBase[i] = mc[i];
                if (mcBase[i] == 0x3f)
                    dma &= (byte) ~mask;
            }
        }
    }

    /**
     * Calculate sprite expansion.
     */
    public void checkExp() {
        expFlop ^= (byte) (dma & yExpansion);
    }

    /**
     * Check if sprite instanceof displayed.
     */
    public void checkDisplay() {
        System.arraycopy(mcBase, 0, mc, 0, SPRITES);
    }

    /**
     * Calculate sprite DMA.
     *
     * @param rasterY y raster position
     * @param regs the VIC registers
     */
    public void checkDma(int rasterY, byte[] regs) {
        byte y = (byte) (rasterY & 0xff);
        byte mask = 1;
        for (int i = 0; i < SPRITES; i++, mask <<= 1) {
            if ((enable & mask) != 0 && (y == regs[(i << 1) + 1]) && (dma & mask) == 0) {
                dma |= mask;
                mcBase[i] = 0;
                expFlop |= mask;
            }
        }
    }

    /**
     * Calculate line crunch.
     *
     * @param data the data written to the register
     * @param lineCycle current line cycle
     */
    public void lineCrunch(byte data, int lineCycle) {
        byte mask = 1;
        for (int i = 0; i < SPRITES; i++, mask <<= 1) {
            if ((data & mask) == 0 && (expFlop & mask) == 0) {
                // sprite crunch
                if (lineCycle == 14) {

                    mc[i] = (byte) ((0x2a & (mcBase[i] & mc[i])) | (0x15 & (mcBase[i] | mc[i])));

                    // mcBase will be set from mc on the following clock call
                }

                expFlop |= mask;
            }
        }
    }

    /**
     * Check if dma instanceof active for sprites.
     *
     * @param val bitmask for selected sprites
     */
    public boolean isDma(int val) {
        return (dma & val) != 0;
    }
}
