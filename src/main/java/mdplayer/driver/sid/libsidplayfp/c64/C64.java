/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import dotnet4j.Tuple;
import dotnet4j.io.FileStream;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.event_phase_t;
import mdplayer.driver.sid.libsidplayfp.c64.banks.ColorRAMBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.DisconnectedBusBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.ExtraSidBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.IOBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.SidBank;
import mdplayer.driver.sid.libsidplayfp.c64.vic_ii.MOS656X;
import mdplayer.driver.sid.libsidplayfp.sidmemory;


/**
 * Commodore 64 emulation core.
 * <p>
 * It consists of the following chips:
 * - CPU 6510
 * - VIC-II 6567/6569/6572
 * - CIA 6526
 * - SID 6581/8580
 * - PLA 7700/82S100
 * - Color RAM 2114
 * - System RAM 4164-20/50464-150
 * - Character ROM 2332
 * - Basic ROM 2364
 * - Kernal ROM 2364
 */
public class C64 extends C64Env {
    public enum model_t {
        PAL_B     ///< PAL C64
        , NTSC_M       ///< NTSC C64
        , OLD_NTSC_M   ///< Old NTSC C64
        , PAL_N        ///< C64 Drean
    }

    //private HashMap<Integer, Banks.ExtraSidBank> sidBankMap_t;
    //typedef std::map<Integer, ExtraSidBank*> sidBankMap_t;

    // System clock frequency
    private double cpuFrequency;

    // Number of sources asserting IRQ
    private int irqCount;

    // BA state
    private Boolean oldBAState;

    // System event context
    private EventScheduler eventScheduler;

    // CPU
    private C64Cpu cpu;

    // CIA1
    private C64cia1 cia1;

    // CIA2
    private C64cia2 cia2;

    // VIC II
    private C64Vic vic;

    // Color RAM
    private ColorRAMBank colorRAMBank = new ColorRAMBank();

    // SID
    private SidBank sidBank = new SidBank();

    // Extra SIDs
    private List<Tuple<Integer, ExtraSidBank>> extraSidBanks = new ArrayList<>();

    // I/O Area // #1 and // #2
    private DisconnectedBusBank disconnectedBusBank = new DisconnectedBusBank();

    // I/O Area
    private IOBank ioBank = new IOBank();

    // MMU chip
    private MMU mmu;

    //private double getCpuFreq(model_t model) { return 0; }

    /**
     * Access memory as seen by CPU.
     * <p>
     * @param addr the address where to read from
     * @return value at address
     */
    @Override
    public byte cpuRead(short addr) {
        return mmu.cpuRead(addr);
    }

    /**
     * Access memory as seen by CPU.
     * <p>
     * @param addr the address where to write to
     * @param data the value to write
     */
    @Override
    public void cpuWrite(short addr, byte data) {
        mmu.cpuWrite(addr, data);
    }

    /*
     * IRQ trigger signal.
     *
     * Calls permitted any time, but normally originated by chips at PHI1.
     */
    //@Override public void interruptIRQ(Boolean state) { }

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

    /*
     * BA signal.
     * <p>
     * Calls permitted during PHI1.
     * <p>
     * @param state
     */
    //@Override public void setBA(Boolean state) { }

    //@Override public void lightpen(Boolean state) { }

    //public void resetIoBank() { }

    protected void finalize() {
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
        return (int) (eventScheduler.getTime(event_phase_t.EVENT_CLOCK_PHI1) / cpuFrequency);
    }

    /**
     * Clock the emulation.
     *
     * //@throws haltInstruction
     */
    public void clock() {
        eventScheduler.clock();
    }

    public void debug(Boolean enable, FileStream out_) {
        cpu.debug(enable, out_);
    }

    //public void reset() { }
    public void resetCpu() {
        cpu.reset();
    }

    /**
     * Set the C64 model.
     */
    //public void setModel(model_t model) { }
    public void setRoms(byte[] kernal, byte[] basic, byte[] character) {
        ByteBuffer k = null, b = null, c = null;
        if (kernal != null) k = ByteBuffer.wrap(kernal);
        if (basic != null) b = ByteBuffer.wrap(basic);
        if (character != null) c = ByteBuffer.wrap(character);

        mmu.setRoms(k, b, c);
    }

    /**
     * Get the CPU clock speed.
     * <p>
     * @return the speed : Hertz
     */
    public double getMainCpuSpeed() {
        return cpuFrequency;
    }

    /*
     * Set the base SID.
     *
     * @param s the Sid emu to set
     */
    //public void setBaseSid(C64Sid s) { }

    /*
     * Add an extra SID.
     *
     * @param s the Sid emu to set
     * @param sidAddress
     *           base address (e.g. 0xd420)
     */
     //@return false if address instanceof unsupported

    //public Boolean addExtraSid(C64Sid s, int address) { return false; }

    /*
     * Remove all the SIDs.
     */
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

    public sidmemory getMemInterface() {
        return mmu;
    }

    public short getCia1TimerA() {
        return cia1.getTimerA();
    }

    @Override
    public void interruptIRQ(Boolean state) {
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

    @Override
    public void setBA(Boolean state) {
        // only react to changes : state
        if (state == oldBAState)
            return;

        oldBAState = state;

        // Signal changes : BA to interested parties
        cpu.setRDY(state);
    }

    @Override
    public void lightpen(Boolean state) {
        if (state)
            vic.triggerLightpen();
        else
            vic.clearLightpen();
    }

    public class Model {
        public double colorBurst;         ///< Colorburst frequency : Herz
        public double divider;            ///<.Clock frequency divider
        public double powerFreq;          ///< Power line frequency : Herz
        public MOS656X.model_t vicModel; ///< Video chip model

        public Model(double colorBurst, double divider, double powerFreq, MOS656X.model_t vicModel) {
            this.colorBurst = colorBurst;
            this.divider = divider;
            this.powerFreq = powerFreq;
            this.vicModel = vicModel;
        }
    }

    /*
     * Color burst frequencies:
     *
     * NTSC  - 3.579545455 MHz = 315/88 MHz
     * PAL-B - 4.43361875 MHz = 283.75 * 15625 Hz + 25 Hz.
     * PAL-M - 3.57561149 MHz
     * PAL-N - 3.58205625 MHz
     */
    public Model[] modelData;

    public double getCpuFreq(model_t model) {
        // The crystal clock that drives the VIC II chip instanceof four times
        // the color burst frequency
        double crystalFreq = modelData[(int) model.ordinal()].colorBurst * 4.0;

        // The VIC II produces the two-phase system clock
        // by running the input clock through a divider
        return crystalFreq / modelData[(int) model.ordinal()].divider;
    }

    public C64() {
        super(new EventScheduler());
        eventScheduler = super.eventScheduler;
        modelData = new Model[] {
                new Model(4433618.75, 18.0, 50.0, MOS656X.model_t.MOS6569),      // PAL-B
                new Model(3579545.455, 14.0, 60.0, MOS656X.model_t.MOS6567R8),    // NTSC-M
                new Model(3579545.455, 14.0, 60.0, MOS656X.model_t.MOS6567R56A),  // Old NTSC-M
                new Model(3582056.25, 14.0, 50.0, MOS656X.model_t.MOS6572)       // PAL-N
        };

        cpuFrequency = getCpuFreq(model_t.PAL_B);
        cpu = new C64Cpu(this);
        cia1 = new C64cia1(this);
        cia2 = new C64cia2(this);
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

    // template<typename T>
    public void resetSID(ExtraSidBank e) {
        //e.second.reset();
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
            resetSID(b.Item2);
        }

        irqCount = 0;
        oldBAState = true;
    }

    public void setModel(model_t model) {
        cpuFrequency = getCpuFreq(model);
        vic.chip(modelData[model.ordinal()].vicModel);

        int rate = (int) (cpuFrequency / modelData[(int) model.ordinal()].powerFreq);
        cia1.setDayOfTimeRate(rate);
        cia2.setDayOfTimeRate(rate);
    }

    public void setBaseSid(C64Sid s) {
        sidBank.setSID(s);
    }

    public Boolean addExtraSid(C64Sid s, int address) {
        // Check for valid address : the IO area range ($dxxx)
        if ((address & 0xf000) != 0xd000)
            return false;

        int idx = (address >> 8) & 0xf;

        // Only allow second SID chip : SID area ($d400-$d7ff)
        // or IO Area ($de00-$dfff)
        if (idx < 0x4 || (idx > 0x7 && idx < 0xe))
            return false;

        // Add new SID bank
        //extraSidBanks.clear();
        //for (int i = 0; i < 16; i++) extraSidBanks.add(new Tuple<Integer, Banks.ExtraSidBank>(i, new Banks.ExtraSidBank()));

        boolean fnd = false;
        for (Tuple<Integer, ExtraSidBank> ite : extraSidBanks) {
            if (ite.Item1 == idx) {
                fnd = true;
                ExtraSidBank extraSidBank = ite.Item2;
                extraSidBank.addSID(s, address);
                break;
            }
        }
        if (!fnd) {
            extraSidBanks.add(new Tuple<Integer, ExtraSidBank>(idx, new ExtraSidBank()));
            ExtraSidBank extraSidBank = extraSidBanks.get(extraSidBanks.size() - 1).Item2;
            extraSidBank.resetSIDMapper(ioBank.getBank(idx));
            ioBank.setBank(idx, extraSidBank);
            extraSidBank.addSID(s, address);
        }

        //Tuple<Integer, Banks.ExtraSidBank> it = extraSidBanks[idx];
        //if (idx != extraSidBanks.size() - 1) {
        //    Banks.ExtraSidBank extraSidBank = it.Item2;
        //    extraSidBank.addSID(s, address);
        //} else {
        //    extraSidBanks.add(new Tuple<Integer, Banks.ExtraSidBank>(idx, new Banks.ExtraSidBank()));
        //    Banks.ExtraSidBank extraSidBank = extraSidBanks[extraSidBanks.size() - 1].Item2;
        //    extraSidBank.resetSIDMapper(ioBank.getBank(idx));
        //    ioBank.setBank(idx, extraSidBank);
        //    extraSidBank.addSID(s, address);
        //}

        return true;
    }

    public void clearSids() {
        sidBank.setSID(null);

        resetIoBank();

        for (int i = 0; i < extraSidBanks.size(); i++) {
            extraSidBanks.set(i, new Tuple<>(extraSidBanks.get(i).Item1, null));

            extraSidBanks.clear();
        }
    }
}
