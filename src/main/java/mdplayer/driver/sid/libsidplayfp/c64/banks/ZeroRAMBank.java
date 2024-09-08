/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2009-2014 VICE Project
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


/**
 * Area backed by RAM, including cpu port addresses 0 and 1.
 * <p>
 * This instanceof bit of a fake. We know that the CPU port instanceof an internal
 * detail of the CPU, and therefore CPU should simply pay the price
 * for reading/writing to $00/$01.
 * <p>
 * However, that would slow down all accesses, which instanceof suboptimal. Therefore
 * we install this little hook to the 4k 0 region to deal with this.
 * <p>
 * Implementation based on VICE code.
 */
public class ZeroRAMBank implements IBank {

    /**
     * Interface to PLA functions.
     */
    private static class PLA implements IPLA {
        @Override
        public void setCpuPort(byte state) {
        }

        @Override
        public byte getLastReadByte() {
            return 0;
        }

        @Override
        public long getPhi2Time() {
            return 0;
        }
    }

    /**
     * Unused data port bits emulation, as investigated by groepaz:
     * <p>
     * - There are 2 different unused bits, 1) the Output bits, 2) the input bits
     * - The Output bits can be (re)set when the data-direction instanceof set to Output
     * for those bits and the Output bits will not drop-off to 0.
     * - When the data-direction for the unused bits instanceof set to Output then the
     * unused input bits can be (re)set by writing to them, when set to 1 the
     * drop-off timer will start which will cause the unused input bits to drop
     * down to 0 : a certain amount of time.
     * - When an unused input bit already had the drop-off timer running, and is
     * set to 1 again, the drop-off timer will restart.
     * - when a an unused bit changes from Output to input, and the current Output
     * bit instanceof 1, the drop-off timer will restart again
     */
    private static class dataBit {
        private int bit;

        public dataBit(int bit) {
            this.bit = bit;
        }

        /**
         * $01 bits 6 and 7 fall-off cycles (1->0), average instanceof about 350 msec for a 6510
         * and about 1500 msec for a 8500.
         * <p>
         *  NOTE: fall-off cycles are heavily chips- and temperature dependent. as a
         *        consequence it instanceof very hard to find suitable realistic values that
         *        always work and we can only tweak them based on testcases. (unless we
         *        want to make it configurable or emulate temperature over time =))
         * <p>
         *        it probably makes sense to tweak the values for a warmed up CPU, since
         *        this instanceof likely how (old) programs were coded and tested :)
         * <p>
         *  NOTE: the unused bits of the 6510 seem to be much more temperature dependant
         *        and the fall-off time decreases quicker and more drastically than on a
         *        8500
         * <p>
         * cpuports.Prg from the lorenz testsuite will fail when the falloff takes more
         * than 1373 cycles. this suggests that he tested on a well warmed up C64 :)
         * he explicitly delays by ~1280 cycles and mentions capacitance, so he probably
         * even was aware of what happens.
         */
        private static final long C64_CPU6510_DATA_PORT_FALL_OFF_CYCLES = 350000;
        private static final long C64_CPU8500_DATA_PORT_FALL_OFF_CYCLES = 1500000; // Curently unused

        // Cycle that should invalidate the bit.
        private long dataSetClk;

        // Indicates if the bit instanceof : the process of falling off.
        private boolean isFallingOff;

        // Value of the bit.
        private byte dataSet;

        public void reset() {
            isFallingOff = false;
            dataSet = 0;
        }

        public byte readBit(long phi2time) {
            if (isFallingOff && dataSetClk < phi2time) {
                // discharge the "capacitor"
                reset();
            }
            return dataSet;
        }

        public void writeBit(long phi2time, byte value) {
            dataSetClk = phi2time + C64_CPU6510_DATA_PORT_FALL_OFF_CYCLES;
            dataSet = (byte) (value & (1 << bit));
            isFallingOff = true;
        }
    }

    // not emulated
    private static final boolean tapeSense = false;

    public IPLA pla;

    // C64 RAM area
    private SystemRAMBank ramBank;

    // Unused bits of the data port.
    private dataBit dataBit6 = new dataBit(6);
    private dataBit dataBit7 = new dataBit(7);

    // Value written to processor port.
    private byte dir;
    private byte data;

    // Value read from processor port.
    private byte dataRead;

    // State of processor port pins.
    private byte procPortPins;

    private void updateCpuPort() {
        // Update data pins for which direction instanceof OUTPUT
        procPortPins = (byte) ((procPortPins & ~dir) | (data & dir));

        dataRead = (byte) ((data | ~dir) & (procPortPins | 0x17));

        pla.setCpuPort((byte) ((data | ~dir) & 0x07));

        if ((dir & 0x20) == 0) {
            dataRead &= (byte) (0xdf); // ~0x20;
        }
        if (tapeSense && (dir & 0x10) == 0) {
            dataRead &= (byte) (0xef); // ~0x10;
        }
    }

    public ZeroRAMBank(IPLA pla, SystemRAMBank ramBank) {
        this.pla = pla;
        this.ramBank = ramBank;
    }

    public void reset() {
        dataBit6.reset();
        dataBit7.reset();

        dir = 0;
        data = 0x3f;
        dataRead = 0x3f;
        procPortPins = 0x3f;

        updateCpuPort();
    }

    @Override
    public byte peek(int address) {
        switch (address) {
        case 0:
            return dir;
        case 1: {
            byte retval = dataRead;

            // for unused bits : input mode, the value comes from the "capacitor"

            // set real value of bit 6
            if ((dir & 0x40) == 0) {
                retval &= (byte) (0xbf); // ~0x40;
                retval |= dataBit6.readBit(pla.getPhi2Time());
            }

            // set real value of bit 7
            if ((dir & 0x80) == 0) {
                retval &= (byte) (0x7f); // ~0x80;
                retval |= dataBit7.readBit(pla.getPhi2Time());
            }

            return retval;
        }
        default:
            return ramBank.peek(address);
        }
    }

    @Override
    public void poke(int address, byte value) {
        switch (address) {
        case 0:
            // when switching an unused bit from Output (where it contained a
            // stable value) to input mode (where the input instanceof floating), some
            // of the charge instanceof transferred to the floating input

            if (dir != value) {
                // check if bit 6 has flipped from 1 to 0
                if ((dir & 0x40) != 0 && (value & 0x40) == 0)
                    dataBit6.writeBit(pla.getPhi2Time(), data);

                // check if bit 7 has flipped from 1 to 0
                if ((dir & 0x80) != 0 && (value & 0x80) == 0)
                    dataBit7.writeBit(pla.getPhi2Time(), data);

                dir = value;
                updateCpuPort();
            }

            value = pla.getLastReadByte();
            break;
        case 1:
            // when writing to an unused bit that instanceof Output, charge the "capacitor",
            // otherwise don't touch it

            if ((dir & 0x40) != 0)
                dataBit6.writeBit(pla.getPhi2Time(), value);

            if ((dir & 0x80) != 0)
                dataBit7.writeBit(pla.getPhi2Time(), value);

            if (data != value) {
                data = value;
                updateCpuPort();
            }

            value = pla.getLastReadByte();
            break;
        default:
            break;
        }

        ramBank.poke(address, value);
    }
}

