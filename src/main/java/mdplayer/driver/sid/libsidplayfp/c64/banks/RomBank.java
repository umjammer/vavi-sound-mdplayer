/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2010 Antti Lankila
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

package mdplayer.driver.sid.libsidplayfp.c64.banks;

import java.nio.ByteBuffer;

import mdplayer.driver.sid.libsidplayfp.SidEndian;
import mdplayer.driver.sid.libsidplayfp.c64.cpu.OpCodes;


/**
 * ROM bank base class. N must be a power of two.
 */
public class RomBank implements IBank {

    /** template<int N> */
    private int N = 0x10000; // dummy

    public RomBank(int N) {
        this.N = N;
        rom = ByteBuffer.allocate(N);
    }

    /** The ROM array */
    protected ByteBuffer rom;

    /**
     * Set value at memory address.
     */
    protected void setVal(short address, byte val) {
        rom.put(address & (N - 1), val);
    }

    /**
     * Return value from memory address.
     */
    protected byte getVal(short address) {
        return rom.get(address & (N - 1));
    }

    /**
     * Return pointer to memory address.
     */
    protected ByteBuffer getPtr(short address) {
        // TODO: kuma アドレス自体を返しているっぽい
        return rom.slice(); // rom.ptr + (address & (N - 1)));
    }

    /**
     * Copy content from source buffer.
     */
    public void set(ByteBuffer source) {
        if (source != null) {
            rom = source.duplicate();
        }
    }

    /**
     * Writing to ROM instanceof a no-op.
     */
    public void poke(short a, byte b) {
    }

    /**
     * //Read from ROM.
     */
    public byte peek(short address) {
        return rom.get(address & (N - 1));
    }


    /**
     * Kernal ROM
     * <p>
     * Located at $E000-$FFFF
     */
    public static class KernalRomBank extends RomBank {
        public KernalRomBank() {
            super(0x2000);
        }

        private byte resetVectorLo; // 0xfffc

        private byte resetVectorHi; // 0xfffd

        @Override
        public void set(ByteBuffer kernal) {
            // RomBank < 0x2000 >::set(kernal);
            super.set(kernal);

            if (kernal == null) {
                // IRQ entry point
                setVal((short) 0xffa0, (byte) OpCodes.PHAn); // Save regs
                setVal((short) 0xffa1, (byte) OpCodes.TXAn);
                setVal((short) 0xffa2, (byte) OpCodes.PHAn);
                setVal((short) 0xffa3, (byte) OpCodes.TYAn);
                setVal((short) 0xffa4, (byte) OpCodes.PHAn);
                setVal((short) 0xffa5, (byte) OpCodes.JMPi); // Jump to IRQ routine
                setVal((short) 0xffa6, (byte) 0x14);
                setVal((short) 0xffa7, (byte) 0x03);

                // Halt
                setVal((short) 0xea39, (byte) 0x02);

                // Hardware vectors
                setVal((short) 0xfffa, (byte) 0x39); // NMI vector
                setVal((short) 0xfffb, (byte) 0xea);
                setVal((short) 0xfffc, (byte) 0x39); // RESET vector
                setVal((short) 0xfffd, (byte) 0xea);
                setVal((short) 0xfffe, (byte) 0xa0); // IRQ/BRK vector
                setVal((short) 0xffff, (byte) 0xff);
            }

            // Backup Reset Vector
            resetVectorLo = getVal((short) 0xfffc);
            resetVectorHi = getVal((short) 0xfffd);
        }

        public void reset() {
            // Restore original Reset Vector
            setVal((short) 0xfffc, resetVectorLo);
            setVal((short) 0xfffd, resetVectorHi);
        }

        /**
         * //Change the RESET vector.
         * <p>
         * //@param addr the new addres to point to
         */
        public void installResetHook(short addr) {
            setVal((short) 0xfffc, SidEndian.to16lo8(addr));
            setVal((short) 0xfffd, SidEndian.to16hi8(addr));
        }
    }

    /**
     * BASIC ROM
     * <p>
     * Located at $A000-$BFFF
     */
    public static class BasicRomBank extends RomBank {
        public BasicRomBank() {
            super(0x2000);
        }

        private byte[] trap = new byte[3];

        private byte[] subTune = new byte[11];

        @Override
        public void set(ByteBuffer basic) {
            // RomBank < 0x2000 >::set(basic);
            super.set(basic);

            // Backup BASIC Warm Start
            // memcpy(trap, getPtr(0xa7ae), sizeof(trap));
            for (int i = 0; i < trap.length; i++)
                trap[i] = getVal((short) (0xa7ae + i));

            // memcpy(subTune, getPtr(0xbf53), sizeof(subTune));
            for (int i = 0; i < subTune.length; i++)
                subTune[i] = getVal((short) (0xbf53 + i));
        }

        public void reset() {
            // Restore original BASIC Warm Start
            // memcpy(getPtr(0xa7ae), trap, sizeof(trap));
            for (int i = 0; i < trap.length; i++)
                setVal((short) (0xa7ae + i), trap[i]);

            // memcpy(getPtr(0xbf53), subTune, sizeof(subTune));
            for (int i = 0; i < subTune.length; i++)
                setVal((short) (0xbf53 + i), subTune[i]);
        }

        /**
         * //Set BASIC Warm Start address.
         * <p>
         * //@param addr
         */
        public void installTrap(short addr) {
            setVal((short) 0xa7ae, (byte) OpCodes.JMPw);
            setVal((short) 0xa7af, SidEndian.to16lo8(addr));
            setVal((short) 0xa7b0, SidEndian.to16hi8(addr));
        }

        public void setSubtune(byte tune) {
            setVal((short) 0xbf53, (byte) OpCodes.LDAb);
            setVal((short) 0xbf54, tune);
            setVal((short) 0xbf55, (byte) OpCodes.STAa);
            setVal((short) 0xbf56, (byte) 0x0c);
            setVal((short) 0xbf57, (byte) 0x03);
            setVal((short) 0xbf58, (byte) OpCodes.JSRw);
            setVal((short) 0xbf59, (byte) 0x2c);
            setVal((short) 0xbf5a, (byte) 0xa8);
            setVal((short) 0xbf5b, (byte) OpCodes.JMPw);
            setVal((short) 0xbf5c, (byte) 0xb1);
            setVal((short) 0xbf5d, (byte) 0xa7);
        }
    }

    /**
     * Character ROM
     * <p>
     * Located at $D000-$DFFF
     */
    public static class CharacterRomBank extends RomBank {
        public CharacterRomBank() {
            super(0x1000);
        }
    }
}