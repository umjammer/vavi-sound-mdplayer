/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2017 Leandro Nini <drfiemost@users.sourceforge.net>
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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.c64;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import dotnet4j.io.FileStream;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.EventPhase;
import mdplayer.driver.sid.libsidplayfp.SidMemory;
import mdplayer.driver.sid.libsidplayfp.c64.banks.ColorRAMBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.DisconnectedBusBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.ExtraSidBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.IOBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.SidBank;
import mdplayer.driver.sid.libsidplayfp.c64.vic_ii.Mos656X;


/**
 * Commodore 64 emulation core.
 * <p>
 * It consists of the following chips:
 * - CPU 6510
 * - VIC-II 6567/6569/6572
 * - CIA 6526
 * - Sid 6581/8580
 * - PLA 7700/82S100
 * - Color RAM 2114
 * - System RAM 4164-20/50464-150
 * - Character ROM 2332
 * - Basic ROM 2364
 * - Kernal ROM 2364
 */
public class C64 extends C64Env {

    public enum Clock {
        /**
         * PAL C64
         */
        PAL_B,
        /**
         * NTSC C64
         */
        NTSC_M,
        /**
         * Old NTSC C64
         */
        OLD_NTSC_M,
        /**
         * C64 Drean
         */
        PAL_N
    }

    /**
     * System clock frequency
     */
    private double cpuFrequency;

    /** Number of sources asserting IRQ */
    private int irqCount;

    /** BA state */
    private boolean oldBAState;

    /** System event context */
    private final EventScheduler eventScheduler;

    /** CPU */
    private final C64Cpu cpu;

    /** CIA1 */
    private final C64Cia1 cia1;

    /** CIA2 */
    private final C64Cia2 cia2;

    /** VIC II */
    private final C64Vic vic;

    /** Color RAM */
    private final ColorRAMBank colorRAMBank = new ColorRAMBank();

    /** Sid */
    private final SidBank sidBank = new SidBank();

    /** Extra SIDs */
    private final List<Tuple<Integer, ExtraSidBank>> extraSidBanks = new ArrayList<>();

    /** I/O Area #1 and #2 */
    private final DisconnectedBusBank disconnectedBusBank = new DisconnectedBusBank();

    /** I/O Area */
    private final IOBank ioBank = new IOBank();

    /** MMU chip */
    private final MMU mmu;

    /**
     * Access memory as seen by CPU.
     *
     * @param address the address where to read from
     * @return value at address
     */
    @Override
    public byte cpuRead(short address) {
        return mmu.cpuRead(address);
    }

    /**
     * Access memory as seen by CPU.
     *
     * @param address the address where to write to
     * @param data the value to write
     */
    @Override
    public void cpuWrite(short address, byte data) {
        mmu.cpuWrite(address, data);
    }

    /**
     * NMI trigger signal.
     * <p>
     * Calls permitted any time, but normally originated by chips at PHI1.
     */
    @Override
    public void interruptNMI() {
        cpu.triggerNMI();
    }

    /**
     * Reset signal.
     */
    @Override
    public void interruptRST() {
        cpu.triggerRST();
    }

    /**
     * Get C64's event scheduler
     *
     * @return the scheduler
     */
    public EventScheduler getEventScheduler() {
        return eventScheduler;
    }

    public int getTime() {
        return (int) (eventScheduler.getTime(EventPhase.CLOCK_PHI1) / cpuFrequency);
    }

    /**
     * Clock the emulation.
     *
     * @throws "haltInstruction"
     */
    public void clock() {
        eventScheduler.clock();
    }

    public void debug(boolean enable, FileStream out_) {
        cpu.debug(enable, out_);
    }

    public void resetCpu() {
        cpu.reset();
    }

    public void setRoms(byte[] kernal, byte[] basic, byte[] character) {
        ByteBuffer k = null, b = null, c = null;
        if (kernal != null) k = ByteBuffer.wrap(kernal);
        if (basic != null) b = ByteBuffer.wrap(basic);
        if (character != null) c = ByteBuffer.wrap(character);

        mmu.setRoms(k, b, c);
    }

    /**
     * Get the CPU clock speed.
     *
     * @return the speed : Hertz
     */
    public double getMainCpuSpeed() {
        return cpuFrequency;
    }

    //public void clearSids() { }

    /**
     * Get the components credits
     */
    public String cpuCredits() {
        return cpu.credits();
    }

    public String ciaCredits() {
        return cia1.credits();
    }

    public String vicCredits() {
        return vic.credits();
    }

    public SidMemory getMemInterface() {
        return mmu;
    }

    public short getCia1TimerA() {
        return cia1.getTimerA();
    }

    /**
     * IRQ trigger signal.
     * <p>
     * Calls permitted any time, but normally originated by chips at PHI1.
     */
    @Override
    public void interruptIRQ(boolean state) {
        if (state) {
            if (irqCount == 0)
                cpu.triggerIRQ();

            irqCount++;
        } else {
            irqCount--;
            if (irqCount == 0)
                cpu.clearIRQ();
        }
    }

    /**
     * BA signal.
     * <p>
     * Calls permitted during PHI1.
     *
     * @param state
     */
    @Override
    public void setBA(boolean state) {
        // only react to changes : state
        if (state == oldBAState)
            return;

        oldBAState = state;

        // Signal changes : BA to interested parties
        cpu.setRDY(state);
    }

    @Override
    public void lightpen(boolean state) {
        if (state)
            vic.triggerLightpen();
        else
            vic.clearLightpen();
    }

    private static class Model {
        /**
         * Colorburst frequency : Herz
         */
        public double colorBurst;
        /**
         * .Clock frequency divider
         */
        public double divider;
        /**
         * Power line frequency : Herz
         */
        public double powerFreq;
        /**
         * Video chip model
         */
        public Mos656X.Model vicModel;

        public Model(double colorBurst, double divider, double powerFreq, Mos656X.Model vicModel) {
            this.colorBurst = colorBurst;
            this.divider = divider;
            this.powerFreq = powerFreq;
            this.vicModel = vicModel;
        }
    }

    /**
     * Color burst frequencies:
     * <p>
     * NTSC  - 3.579545455 MHz = 315/88 MHz
     * PAL-B - 4.43361875 MHz = 283.75 * 15625 Hz + 25 Hz.
     * PAL-M - 3.57561149 MHz
     * PAL-N - 3.58205625 MHz
     */
    public Model[] modelData;

    public double getCpuFreq(Clock model) {
        // The crystal clock that drives the VIC II chip instanceof four times
        // the color burst frequency
        double crystalFreq = modelData[model.ordinal()].colorBurst * 4.0;

        // The VIC II produces the two-phase system clock
        // by running the input clock through a divider
        return crystalFreq / modelData[model.ordinal()].divider;
    }

    public C64() {
        super(new EventScheduler());
        eventScheduler = super.eventScheduler;
        modelData = new Model[] {
                new Model(4433618.75, 18.0, 50.0, Mos656X.Model.MOS6569), // PAL-B
                new Model(3579545.455, 14.0, 60.0, Mos656X.Model.MOS6567R8), // NTSC-M
                new Model(3579545.455, 14.0, 60.0, Mos656X.Model.MOS6567R56A), // Old NTSC-M
                new Model(3582056.25, 14.0, 50.0, Mos656X.Model.MOS6572) // PAL-N
        };

        cpuFrequency = getCpuFreq(Clock.PAL_B);
        cpu = new C64Cpu(this);
        cia1 = new C64Cia1(this);
        cia2 = new C64Cia2(this);
        vic = new C64Vic(this);
        mmu = new MMU(eventScheduler, ioBank);
        resetIoBank();
    }

    public void resetIoBank() {
        ioBank.setBank(0x0, vic);
        ioBank.setBank(0x1, vic);
        ioBank.setBank(0x2, vic);
        ioBank.setBank(0x3, vic);
        ioBank.setBank(0x4, sidBank);
        ioBank.setBank(0x5, sidBank);
        ioBank.setBank(0x6, sidBank);
        ioBank.setBank(0x7, sidBank);
        ioBank.setBank(0x8, colorRAMBank);
        ioBank.setBank(0x9, colorRAMBank);
        ioBank.setBank(0xa, colorRAMBank);
        ioBank.setBank(0xb, colorRAMBank);
        ioBank.setBank(0xc, cia1);
        ioBank.setBank(0xd, cia2);
        ioBank.setBank(0xe, disconnectedBusBank);
        ioBank.setBank(0xf, disconnectedBusBank);
    }

    public void resetSID(ExtraSidBank e) {
        e.reset();
    }

    public void reset() {
        eventScheduler.reset();

        //cpu.reset();
        cia1.reset();
        cia2.reset();
        vic.reset();
        sidBank.reset();
        colorRAMBank.reset();
        mmu.reset();

        for (Tuple<Integer, ExtraSidBank> b : extraSidBanks) {
            resetSID(b.getItem2());
        }

        irqCount = 0;
        oldBAState = true;
    }

    /**
     * Set the C64 model.
     */
    public void setModel(Clock model) {
        cpuFrequency = getCpuFreq(model);
        vic.chip(modelData[model.ordinal()].vicModel);

        int rate = (int) (cpuFrequency / modelData[model.ordinal()].powerFreq);
        cia1.setDayOfTimeRate(rate);
        cia2.setDayOfTimeRate(rate);
    }

    /**
     * Set the base Sid.
     *
     * @param s the Sid emu to set
     */
    public void setBaseSid(C64Sid s) {
        sidBank.setSID(s);
    }

    /**
     * Add an extra Sid.
     *
     * @param s       the Sid emu to set
     * @param address base address (e.g. 0xd420)
     * @return false if address instanceof unsupported
     */
    public boolean addExtraSid(C64Sid s, int address) {
        // Check for valid address : the IO area range ($dxxx)
        if ((address & 0xf000) != 0xd000)
            return false;

        int index = (address >> 8) & 0xf;

        // Only allow second Sid chip : Sid area ($d400-$d7ff)
        // or IO Area ($de00-$dfff)
        if (index < 0x4 || (index > 0x7 && index < 0xe))
            return false;

        // Add new Sid bank

        boolean found = false;
        for (Tuple<Integer, ExtraSidBank> ite : extraSidBanks) {
            if (ite.getItem1() == index) {
                found = true;
                ExtraSidBank extraSidBank = ite.getItem2();
                extraSidBank.addSID(s, address);
                break;
            }
        }
        if (!found) {
            extraSidBanks.add(new Tuple<>(index, new ExtraSidBank()));
            ExtraSidBank extraSidBank = extraSidBanks.get(extraSidBanks.size() - 1).getItem2();
            extraSidBank.resetSIDMapper(ioBank.getBank(index));
            ioBank.setBank(index, extraSidBank);
            extraSidBank.addSID(s, address);
        }

        return true;
    }

    /**
     * Remove all the SIDs.
     */
    public void clearSids() {
        sidBank.setSID(null);

        resetIoBank();

        for (int i = 0; i < extraSidBanks.size(); i++) {
            extraSidBanks.set(i, new Tuple<>(extraSidBanks.get(i).getItem1(), null));

            extraSidBanks.clear();
        }
    }
}
