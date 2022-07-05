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

package mdplayer.driver.sid.libsidplayfp.c64.cpu;

import dotnet4j.io.FileStream;
import jdk.jfr.Unsigned;
import mdplayer.driver.sid.libsidplayfp.EventCallback;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.EventPhase;

import static mdplayer.driver.sid.libsidplayfp.SidEndian.to16;
import static mdplayer.driver.sid.libsidplayfp.SidEndian.to16hi8;
import static mdplayer.driver.sid.libsidplayfp.SidEndian.to16lo8;


/**
 * Cycle-exact 6502/6510 emulation core.
 * <p>
 * Code instanceof based on work by Simon a. White <sidplay2@yahoo.com>.
 * Original Java port by Ken Händel. Later on, it has been hacked to
 * improve compatibility with Lorenz suite on VICE's test suite.
 *
 * @author alankila
 */
public class Mos6510 {

    /*
     * Define this to get correct emulation of SHA/SHX/SHY/SHS instructions
     * (see VICE CPU tests).
     * This will slow down the emulation a bit with no real benefit
     * for Sid playing so we keep it disabled.
     */
    //#define CORRECT_SH_INSTRUCTIONS

    public static class haltInstruction extends RuntimeException {
    }

    /**
     * IRQ/NMI magic limit values.
     * Need to be larger than about 0x103 << 3,
     * but can't be min/max for Integer type.
     */
    private static final int MAX = 65536;

    /** Stack page location */
    private final byte SP_PAGE = 0x01;

    /** Status register Interrupt bit. */
    public static final int SR_INTERRUPT = 2;

    private static class ProcessorCycle {
        public interface dlgFunc extends Runnable {
        }

        public dlgFunc func;
        public boolean nosteal;

        public ProcessorCycle() {
            func = null;
            nosteal = false;
        }
    }

    /** Event scheduler */
    private EventScheduler eventScheduler;

    /** Current instruction and subcycle within instruction */
    private int cycleCount;

    /** When IRQ was triggered. -MAX means "during some previous instruction", MAX means "no IRQ" */
    private int interruptCycle;

    /** IRQ asserted on CPU */
    private boolean irqAssertedOnPin;

    /** NMI requested? */
    private boolean nmiFlag;

    /** RST requested? */
    private boolean rstFlag;

    /** RDY pin state (stop CPU on read) */
    private boolean rdy;

    /** Address Low summer carry */
    private boolean adlCarry;

    // #if CORRECT_SH_INSTRUCTIONS
    /** The RDY pin state during last throw away read. */
    private boolean rdyOnThrowAwayRead;
// #endif

    /** Status register */
    private Flags flags = new Flags();

    // Data regarding current instruction
    private @Unsigned short registerProgramCounter;
    private @Unsigned short cycleEffectiveAddress;
    private @Unsigned short cyclePointer;

    /** unsigned */
    private @Unsigned byte cycleData;
    /** unsigned */
    private @Unsigned byte registerStackPointer;
    /** unsigned */
    private @Unsigned byte registerAccumulator;
    /** unsigned */
    private @Unsigned byte registerX;
    /** unsigned */
    private @Unsigned byte registerY;

// #if DEBUG
    // Debug info
//        private short instrStartPC;
//        private short instrOperand;

    //private FileStream m_fdbg;

//        private boolean dodump;
// #endif

    /** Table of CPU opcode implementations */
    private ProcessorCycle[] instrTable = new ProcessorCycle[0x101 << 3];


    /** Represents an instruction subcycle that writes */
    private EventCallback<Mos6510> noSteal;

    /** Represents an instruction subcycle that reads */
    private EventCallback<Mos6510> steal;

    /**
     * Get data from system environment.
     *
     * @param address
     * @return data byte CPU requested
     */
    protected byte cpuRead(short address) {
        return 0;
    }

    /**
     * Write data to system environment.
     *
     * @param address
     * @param data
     */
    protected void writeCpu(short address, byte data) {
    }

// #if PC64_TESTSUITE
    public void loadFile(String file) {
    }

    /**
     * CHR$ conversion table (0x01 = no Output)
     */
    public static final byte[] CHRtab = new byte[] {
            0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x0d, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x20, 0x21, 0x01, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
            0x40, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,
            0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x5b, 0x24, 0x5d, 0x20, 0x20,
            // alternative: CHR$(92=0x5c) => ISO Latin-1(0xa3)
            0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d, 0x7c, 0x7c, 0x5c, 0x5c, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f,
            0x5c, 0x23, 0x5f, 0x23, 0x7c, 0x2f, 0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c, 0x7c, 0x26, 0x5c,
            // 0x80-0xFF
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c, 0x23, 0x7c, 0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d,
            0x2f, 0x2d, 0x2d, 0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x23,
            0x20, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x2b, 0x7c, 0x7c, 0x26, 0x5c,
            0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c, 0x23, 0x7c, 0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d,
            0x2f, 0x2d, 0x2d, 0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x23
    };

    /**
     * Magic value for lxa and ane undocumented instructions.
     * Magic may be EE, EF, FE or FF, but most emulators seem to use EE.
     * Based on tests on a couple of chips at
     * http://visual6502.org/wiki/index.php?title=6502_Opcode_8B_(XAA,_ANE)
     * the value of magic for the MOS 6510 instanceof FF.
     * However the Lorentz test suite assumes this to be EE.
     */
    public static final byte magic = (byte) 0xff;

    /**
     * When AEC signal instanceof high, no stealing instanceof possible.
     */
    private void eventWithoutSteals() {
        ProcessorCycle instr = instrTable[cycleCount++];
        instr.func.run();
        eventScheduler.schedule(noSteal, 1);
    }

    /**
     * When AEC signal instanceof low, steals permitted.
     */
    private void eventWithSteals() {
        if (instrTable[cycleCount].nosteal) {
            ProcessorCycle instr = instrTable[cycleCount++];
            instr.func.run();
            eventScheduler.schedule(steal, 1);
        } else {
            // Even while stalled, the CPU can still process first clock of
            // Interrupt delay, but only the first one.
            if (interruptCycle == cycleCount) {
                interruptCycle--;
            }
        }
    }

    /**
     * Handle bus access signals. When RDY line instanceof asserted, the CPU
     * will pause when executing the next read operation.
     *
     * @param newRDY new state for RDY signal
     */
    public void setRDY(boolean newRDY) {
        rdy = newRDY;

        if (rdy) {
            eventScheduler.cancel(steal);
            eventScheduler.schedule(noSteal, 0, EventPhase.CLOCK_PHI2);
        } else {
            eventScheduler.cancel(noSteal);
            eventScheduler.schedule(steal, 0, EventPhase.CLOCK_PHI2);
        }
    }

    /**
     * Push p on stack, decrement s.
     */
    private void pushSR() {
        short addr = to16(SP_PAGE, registerStackPointer);
        writeCpu(addr, flags.get());
        registerStackPointer--;
    }

    /**
     * increment s, Pop p off stack.
     */
    private void PopSR() {
        // Get status register off stack
        registerStackPointer++;
        short addr = to16(SP_PAGE, registerStackPointer);
        flags.set(cpuRead(addr));
        flags.setB(true);

        calculateInterruptTriggerCycle();
    }

    // Interrupt Routines

    /**
     * This forces the CPU to abort whatever it instanceof doing and immediately
     * enter the RST Interrupt handling sequence. The implementation is
     * not compatible: instructions actually get aborted mid-execution.
     * However, there instanceof no possible way to trigger this signal from
     * programs, so it's OK.
     */
    public void triggerRST() {
        initialise();
        cycleCount = OpCodes.BRKn << 3;
        rstFlag = true;
        calculateInterruptTriggerCycle();
    }

    /**
     * Trigger NMI Interrupt on the CPU. Calling this method
     * Flags that CPU must enter the NMI routine at earliest
     * opportunity. There instanceof no way to cancel NMI request once
     * given.
     */
    public void triggerNMI() {
        nmiFlag = true;
        calculateInterruptTriggerCycle();

        /* maybe process 1 clock of Interrupt delay. */
        if (!rdy) {
            eventScheduler.cancel(steal);
            eventScheduler.schedule(steal, 0, EventPhase.CLOCK_PHI2);
        }
    }

    /**
     * Pull IRQ line low on CPU.
     */
    public void triggerIRQ() {
        irqAssertedOnPin = true;
        calculateInterruptTriggerCycle();

        /* maybe process 1 clock of Interrupt delay. */
        if (!rdy && interruptCycle == cycleCount) {
            eventScheduler.cancel(steal);
            eventScheduler.schedule(steal, 0, EventPhase.CLOCK_PHI2);
        }
    }

    /**
     * Inform CPU that IRQ instanceof no longer pulled low.
     */
    public void clearIRQ() {
        irqAssertedOnPin = false;
        calculateInterruptTriggerCycle();
    }

    private void interruptsAndNextOpcode() {
        if (cycleCount > interruptCycle + 2) {
// # if DEBUG
            //long cycles = eventScheduler.getTime(EventPhase.EVENT_CLOCK_PHI2);
            //System.err.printf("****************************************************\n");
            //System.err.printf(" Interrupt (%d)\n", cycles);
            //System.err.printf("****************************************************\n");
            //DumpState((long)cycles, this);
// #endif
            cpuRead(registerProgramCounter);
            cycleCount = OpCodes.BRKn << 3;
            flags.setB(false);
            interruptCycle = MAX;
        } else {
            fetchNextOpcode();
        }
    }

    private void fetchNextOpcode() {
//            DumpState((long)eventScheduler.getTime(EventPhase.EVENT_CLOCK_PHI2), this);
//            instrStartPC = Register_ProgramCounter;

// # if CORRECT_SH_INSTRUCTIONS
        rdyOnThrowAwayRead = true;
// #endif
        cycleCount = cpuRead(registerProgramCounter) << 3;
        registerProgramCounter++;

        if (!rstFlag && !nmiFlag && !(!flags.getI() && irqAssertedOnPin)) {
            interruptCycle = MAX;
        }
        if (interruptCycle != MAX) {
            interruptCycle = -MAX;
        }
    }

    /**
     * Evaluate when to execute an Interrupt. Calling this method can also
     * result : the decision that no Interrupt at all needs to be scheduled.
     */
    private void calculateInterruptTriggerCycle() {
        /* Interrupt cycle not going to trigger? */
        if (interruptCycle == MAX) {
            if (rstFlag || nmiFlag || (!flags.getI() && irqAssertedOnPin)) {
                interruptCycle = cycleCount;
            }
        }
    }

    private void irqLoRequest() {
        registerProgramCounter = to16lo8(registerProgramCounter, cpuRead(cycleEffectiveAddress));
    }

    private void irqHiRequest() {
        to16hi8(registerProgramCounter, cpuRead((short) (cycleEffectiveAddress + 1)));
    }

    /**
     * Read the next opcode byte from memory (and throw it away)
     */
    private void throwAwayFetch() {
        cpuRead(registerProgramCounter);
    }

    /**
     * Issue throw-away read and fix address.
     * Some people use these to ACK CIA IRQs.
     */
    private void throwAwayRead() {
        cpuRead(cycleEffectiveAddress);
        if (adlCarry)
            cycleEffectiveAddress += 0x100;
    }

    /**
     * Fetch value, increment pc.
     * <p>
     * Addressing Modes:
     * - Immediate
     * - Relative
     */
    private void FetchDataByte() {
        cycleData = cpuRead(registerProgramCounter);
        if (flags.getB()) {
            registerProgramCounter++;
        }

// # if DEBUG
//            instrOperand = Cycle_Data;
// #endif
    }

    /**
     * Fetch low address byte, increment pc.
     * <p>
     * Addressing Modes:
     * - Stack Manipulation
     * - Absolute
     * - Zero Page
     * - Zero Page Indexed
     * - Absolute Indexed
     * - Absolute Indirect
     */
    private void fetchLowAddr() {
        cycleEffectiveAddress = cpuRead(registerProgramCounter);
        registerProgramCounter++;

// # if DEBUG
//            instrOperand = Cycle_EffectiveAddress;
// #endif
    }

    /**
     * Read from address, add index register x to it.
     * <p>
     * Addressing Modes:
     * - Zero Page Indexed
     */
    private void fetchLowAddrX() {
        fetchLowAddr();
        cycleEffectiveAddress = (short) ((cycleEffectiveAddress + registerX) & 0xFF);
    }

    /**
     * Read from address, add index register y to it.
     * <p>
     * Addressing Modes:
     * - Zero Page Indexed
     */
    private void fetchLowAddrY() {
        fetchLowAddr();
        cycleEffectiveAddress = (short) ((cycleEffectiveAddress + registerY) & 0xFF);
    }

    /**
     * Fetch high address byte, increment pc (Absolute Addressing).
     * Low byte must have been obtained first!
     * <p>
     * Addressing Modes:
     * - Absolute
     */
    private void fetchHighAddr() {   // Get the high byte of an address from memory
        cycleEffectiveAddress = to16hi8(cycleEffectiveAddress, cpuRead(registerProgramCounter));
        registerProgramCounter++;

// # if DEBUG
//            SidEndian.endian_16hi8( instrOperand, SidEndian.endian_16hi8(Cycle_EffectiveAddress));
// #endif
    }

    /**
     * Fetch high byte of address, add index register x to low address byte,
     * increment pc.
     * <p>
     * Addressing Modes:
     * - Absolute Indexed
     */
    private void fetchHighAddrX() {
        cycleEffectiveAddress += registerX;
        adlCarry = cycleEffectiveAddress > 0xff;
        fetchHighAddr();
    }

    /**
     * Same as // #FetchHighAddrX except doesn't worry about page crossing.
     */
    private void FetchHighAddrX2() {
        fetchHighAddrX();
        if (!adlCarry)
            cycleCount++;
    }

    /**
     * Fetch high byte of address, add index register y to low address byte,
     * increment pc.
     * <p>
     * Addressing Modes:
     * - Absolute Indexed
     */
    private void fetchHighAddrY() {
        cycleEffectiveAddress += registerY;
        adlCarry = cycleEffectiveAddress > 0xff;
        fetchHighAddr();
    }

    /**
     * Same as // #FetchHighAddrY except doesn't worry about page crossing.
     */
    private void fetchHighAddrY2() {
        fetchHighAddrY();
        if (!adlCarry)
            cycleCount++;
    }

    /**
     * Fetch pointer address low, increment pc.
     * <p>
     * Addressing Modes:
     * - Absolute Indirect
     * - Indirect indexed (post y)
     */
    private void fetchlowpointer() {
        cyclePointer = cpuRead(registerProgramCounter);
        registerProgramCounter++;

// # if DEBUG
//            instrOperand = Cycle_Pointer;
// #endif
    }

    /**
     * Add x to it.
     * <p>
     * Addressing Modes:
     * - Indexed Indirect (pre x)
     */
    private void fetchLowPointerX() {
        cyclePointer = to16lo8(cyclePointer, (byte) ((cyclePointer + registerX) & 0xFF));
    }

    /**
     * Fetch pointer address high, increment pc.
     * <p>
     * Addressing Modes:
     * - Absolute Indirect
     */
    private void fetchHighPointer() {
        to16hi8(cyclePointer, cpuRead(registerProgramCounter));
        registerProgramCounter++;

// # if DEBUG
//            SidEndian.endian_16hi8( instrOperand, SidEndian.endian_16hi8(Cycle_Pointer));
// #endif
    }

    /**
     * Fetch effective address low.
     * <p>
     * Addressing Modes:
     * - Indirect
     * - Indexed Indirect (pre x)
     * - Indirect indexed (post y)
     */
    private void fetchLowEffAddr() {
        cycleEffectiveAddress = cpuRead(cyclePointer);
    }

    /**
     * Fetch effective address high.
     * <p>
     * Addressing Modes:
     * - Indirect
     * - Indexed Indirect (pre x)
     */
    private void fetchHighEffAddr() {
        cyclePointer = to16lo8(cyclePointer, (byte) ((cyclePointer + 1) & 0xff));
        to16hi8(cycleEffectiveAddress, cpuRead(cyclePointer));
    }

    /**
     * Fetch effective address high, add y to low byte of effective address.
     * <p>
     * Addressing Modes:
     * - Indirect indexed (post y)
     */
    private void fetchHighEffAddrY() {
        cycleEffectiveAddress += registerY;
        adlCarry = cycleEffectiveAddress > 0xff;
        fetchHighEffAddr();
    }


    /**
     * Same as // #FetchHighEffAddrY except doesn't worry about page crossing.
     */
    private void fetchHighEffAddrY2() {
        fetchHighEffAddrY();
        if (!adlCarry)
            cycleCount++;
    }

    // Common Data Accessing Routines
    // Data Accessing operations as described : 64doc by John West and
    // Marko Makela

    private void fetchEffAddrDataByte() {
        cycleData = cpuRead(cycleEffectiveAddress);
    }

    /**
     * Write Cycle_Data to effective address.
     */
    private void putEffAddrDataByte() {
        writeCpu(cycleEffectiveAddress, cycleData);
    }

    /**
     * Push Program Counter Low Byte on stack, decrement s.
     */
    private void pushLowPC() {
        short addr = to16(SP_PAGE, registerStackPointer);
        writeCpu(addr, to16lo8(registerProgramCounter));
        registerStackPointer--;
    }

    /**
     * Push Program Counter High Byte on stack, decrement s.
     */
    private void pushHighPC() {
        short addr = to16(SP_PAGE, registerStackPointer);
        writeCpu(addr, to16hi8(registerProgramCounter));
        registerStackPointer--;
    }

    /**
     * Increment stack and pull program counter low byte from stack.
     */
    private void PopLowPC() {
        registerStackPointer++;
        short addr = to16(SP_PAGE, registerStackPointer);
        cycleEffectiveAddress = to16lo8(cycleEffectiveAddress, cpuRead(addr));
    }

    /**
     * Increment stack and pull program counter high byte from stack.
     */
    private void PopHighPC() {
        registerStackPointer++;
        short addr = to16(SP_PAGE, registerStackPointer);
        to16hi8(cycleEffectiveAddress, cpuRead(addr));
    }

    private void wasteCycle() {
    }

    private void brkPushLowPC() {
        pushLowPC();
        if (rstFlag) {
            /* rst = %10x */
            cycleEffectiveAddress = (short) 0xfffc;
        } else if (nmiFlag) {
            /* nmi = %01x */
            cycleEffectiveAddress = (short) 0xfffa;
        } else {
            /* irq = %11x */
            cycleEffectiveAddress = (short) 0xfffe;
        }

        rstFlag = false;
        nmiFlag = false;
        calculateInterruptTriggerCycle();
    }

    // Common Instruction Opcodes
    // See and 6510 Assembly Book for more information on these instructions

    private void brkInstr() {
        pushSR();
        flags.setB(true);
        flags.setI(true);
    }

    private void cldInstr() {
        flags.setD(false);
        interruptsAndNextOpcode();
    }

    private void cliInstr() {
        flags.setI(false);
        calculateInterruptTriggerCycle();
        interruptsAndNextOpcode();
    }

    private void jmpInstr() {
        doJSR();
        interruptsAndNextOpcode();
    }

    private void doJSR() {
        registerProgramCounter = cycleEffectiveAddress;

// # if PC64_TESTSUITE
//            // trap handlers
//            if (Register_ProgramCounter == 0xffd2)
//            {
//                // Print character
//                byte ch = CHRtab[Register_Accumulator];
//                switch (ch)
//                {
//                    case 0:
//                        break;
//                    case 1:
//                        System.err.printf(" ");
//                        break;
//                    case 0xd:
//                        System.err.printf( "\n");
//                        filepos = 0;
//                        break;
//                    default:
//                        filetmp[filepos++] = ch;
//                        System.err.printf( "%d", ch);
//                }
//            }
//            else if (Register_ProgramCounter == 0xe16f)
//            {
//                // Load
//                filetmp[filepos] = '\0';
//                loadFile(filetmp);
//            }
//            else if (Register_ProgramCounter == 0x8000
//                || Register_ProgramCounter == 0xa474)
//            {
//                // Stop
//                exit(0);
//            }
// #endif // PC64_TESTSUITE
    }

    private void phaInstr() {
        short addr = to16(SP_PAGE, registerStackPointer);
        writeCpu(addr, registerAccumulator);
        registerStackPointer--;
    }

    /**
     * RTI does not delay the IRQ I flag change as it instanceof set 3 cycles before
     * the end of the opcode, and thus the 6510 has enough time to call the
     * Interrupt routine as soon as the opcode ends, if necessary.
     */
    private void rtiInstr() {
// # if DEBUG
        //if (dodump)
        //    System.err.printf("****************************************************\n\n");
// #endif
        registerProgramCounter = cycleEffectiveAddress;
        interruptsAndNextOpcode();
    }

    private void rtsInstr() {
        cpuRead(cycleEffectiveAddress);
        registerProgramCounter = cycleEffectiveAddress;
        registerProgramCounter++;
    }

    private void sedInstr() {
        flags.setD(true);
        interruptsAndNextOpcode();
    }

    private void seiInstr() {
        flags.setI(true);
        interruptsAndNextOpcode();
        if (!rstFlag && !nmiFlag && interruptCycle != MAX)
            interruptCycle = MAX;
    }

    private void staInstr() {
        cycleData = registerAccumulator;
        putEffAddrDataByte();
    }

    private void stxInstr() {
        cycleData = registerX;
        putEffAddrDataByte();
    }

    private void styInstr() {
        cycleData = registerY;
        putEffAddrDataByte();
    }

    // Common Instruction Undocumented Opcodes
    // See documented 6502-nmo.opc by Adam Vardy for more details

    /**
     * Perform the SH* instructions.
     * <p>
     *
     * @param offset the index added to the address
     */
    private void shInstr(byte offset) {
        byte tmp = (byte) (cycleData & (to16hi8((short) (cycleEffectiveAddress - offset)) + 1));

// # if CORRECT_SH_INSTRUCTIONS
            /*
             //When a DMA instanceof going on (the CPU instanceof halted by the VIC-II)
             //while the instruction sha/shx/shy executes then the last
             //term of the ANDing (ADH+1) drops off.
             *
             //http://sourceforge.net/p/vice-emu/bugs/578/
             */
        if (rdyOnThrowAwayRead) {
            cycleData = tmp;
        }
// #endif

            /*
             //When the addressing/indexing causes a page boundary crossing
             //the highbyte of the target address becomes equal to the value stored.
             */
        if (adlCarry)
            to16hi8(cycleEffectiveAddress, tmp);
        putEffAddrDataByte();
    }

    /**
     * Undocumented - This opcode stores the result of a AND x AND ADH+1 : memory.
     */
    private void axaInstr() {
        cycleData = (byte) (registerX & registerAccumulator);
        shInstr(registerY);
    }

    /**
     * Undocumented - This opcode ANDs the contents of the y register with ADH+1 and stores the
     * result : memory.
     */
    private void sayInstr() {
        cycleData = registerY;
        shInstr(registerX);
    }

    /**
     * Undocumented - This opcode ANDs the contents of the x register with ADH+1 and stores the
     * result : memory.
     */
    private void xasInstr() {
        cycleData = registerX;
        shInstr(registerY);
    }

    /**
     * Undocumented - AXS ANDs the contents of the a and x registers (without changing the
     * contents of either register) and stores the result : memory.
     * AXS does not affect any Flags : the processor status register.
     */
    private void axsInstr() {
        cycleData = (byte) (registerAccumulator & registerX);
        putEffAddrDataByte();
    }

    /**
     * BCD adding.
     */
    private void doADC() {
        int c = flags.getC() ? 1 : 0;
        int a = registerAccumulator & 0xff;
        int s = cycleData & 0xff;
        int regAC2 = a + s + c;

        if (flags.getD()) {   // BCD mode
            int lo = (a & 0x0f) + (s & 0x0f) + c;
            int hi = (a & 0xf0) + (s & 0xf0);
            if (lo > 0x09)
                lo += 0x06;
            if (lo > 0x0f)
                hi += 0x10;

            flags.setZ((regAC2 & 0xff) == 0);
            flags.setN((hi & 0x80) != 0);
            flags.setV(((hi ^ a) & 0x80) != 0 && ((a ^ s) & 0x80) == 0);
            if (hi > 0x90)
                hi += 0x60;

            flags.setC(hi > 0xff);
            registerAccumulator = (byte) (hi | (lo & 0x0f));
        } else { // Binary mode
            flags.setC(regAC2 > 0xff);
            flags.setV(((regAC2 ^ a) & 0x80) != 0 && ((a ^ s) & 0x80) == 0);
            flags.setNZ(registerAccumulator = (byte) (regAC2 & 0xff));
        }
    }

    /**
     * BCD subtracting.
     */
    private void doSBC() {
        int c = flags.getC() ? 0 : 1;
        int a = registerAccumulator & 0xff;
        int s = cycleData & 0xff;
        int regAC2 = a - s - c;

        flags.setC(regAC2 < 0x100);
        flags.setV(((regAC2 ^ a) & 0x80) != 0 && ((a ^ s) & 0x80) == 0);
        flags.setNZ((byte) regAC2);

        if (flags.getD()) { // BCD mode
            int lo = (a & 0x0f) - (s & 0x0f) - c;
            int hi = (a & 0xf0) - (s & 0xf0);
            if ((lo & 0x10) != 0) {
                lo -= 0x06;
                hi -= 0x10;
            }
            if ((hi & 0x100) != 0)
                hi -= 0x60;
            registerAccumulator = (byte) (hi | (lo & 0x0f));
        } else { // Binary mode
            registerAccumulator = (byte) (regAC2 & 0xff);
        }
    }

    // Generic Instruction Addressing Routines

    // Generic Instruction Opcodes
    // See and 6510 Assembly Book for more information on these instructions

    private void adcInstr() {
        doADC();
        interruptsAndNextOpcode();
    }

    private void andInstr() {
        flags.setNZ(registerAccumulator &= cycleData);
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - For a detailed explanation of this opcode look at:
     * http://visual6502.org/wiki/index.php?title=6502_Opcode_8B_(XAA,_ANE)
     */
    private void aneInstr() {
        flags.setNZ(registerAccumulator = (byte) ((registerAccumulator | magic) & registerX & cycleData));
        interruptsAndNextOpcode();
    }

    private void aslInstr() {
        putEffAddrDataByte();
        flags.setC((cycleData & 0x80) != 0);
        flags.setNZ(cycleData <<= 1);
    }

    private void aslaInstr() {
        flags.setC((registerAccumulator & 0x80) != 0);
        flags.setNZ(registerAccumulator <<= 1);
        interruptsAndNextOpcode();
    }

    private void fixBranch() {
        // Throw away read
        cpuRead(cycleEffectiveAddress);

        // Fix address
        registerProgramCounter += (short) ((cycleData & 0xff) < 0x80 ? 0x0100 : 0xff00);
    }

    private void branch_instr(boolean condition) {
        // 2 cycles spent before arriving here. spend 0 - 2 cycles here;
        // - condition false: Continue immediately to FetchNextInstr.
        // Otherwise read the byte following the opcode (which instanceof already scheduled to occur on this cycle).
        // This effort instanceof wasted. Then calculate address of the branch target. If branch instanceof on same page,
        // then continue at that insn on next cycle (this delays IRQs by 1 clock for some reason, allegedly).
        // If the branch instanceof on different memory page, issue a spurious read with wrong high byte before
        // continuing at the correct address.
        if (condition) {
            // issue the spurious read for next insn here.
            cpuRead(registerProgramCounter);

            cycleEffectiveAddress = to16lo8(registerProgramCounter);
            cycleEffectiveAddress += cycleData;
            adlCarry = ((cycleEffectiveAddress & 0xffff) > 0xff) != ((cycleData & 0xff) > 0x7f);
            cycleEffectiveAddress = to16hi8(cycleEffectiveAddress, to16hi8(registerProgramCounter));

            registerProgramCounter = cycleEffectiveAddress;

            // Check for page boundary crossing
            if (!adlCarry) {
                // Skip next throw away read
                cycleCount++;

                // Hack: delay the Interrupt past this instruction.
                if (interruptCycle >> 3 == cycleCount >> 3)
                    interruptCycle += 2;
            }
        } else {
            // branch not taken: skip the following spurious read insn and go to FetchNextInstr immediately.
            interruptsAndNextOpcode();
        }
    }

    private void bccInstr() {
        branch_instr(!flags.getC());
    }

    private void bcsInstr() {
        branch_instr(flags.getC());
    }

    private void beqInstr() {
        branch_instr(flags.getZ());
    }

    private void bitInstr() {
        flags.setZ((registerAccumulator & cycleData) == 0);
        flags.setN((cycleData & 0x80) != 0);
        flags.setV((cycleData & 0x40) != 0);
        interruptsAndNextOpcode();
    }

    private void bmiInstr() {
        branch_instr(flags.getN());
    }

    private void bneInstr() {
        branch_instr(!flags.getZ());
    }

    private void bplInstr() {
        branch_instr(!flags.getN());
    }

    private void bvcInstr() {
        branch_instr(!flags.getV());
    }

    private void bvsInstr() {
        branch_instr(flags.getV());
    }

    private void clcInstr() {
        flags.setC(false);
        interruptsAndNextOpcode();
    }

    private void clvInstr() {
        flags.setV(false);
        interruptsAndNextOpcode();
    }

    private void cmpInstr() {
        short tmp = (short) ((registerAccumulator & 0xff) - (cycleData & 0xff));
        flags.setNZ((byte) tmp);
        flags.setC(tmp < 0x100);
        interruptsAndNextOpcode();
    }

    private void cpxInstr() {
        short tmp = (short) ((registerX & 0xff) - (cycleData & 0xff));
        flags.setNZ((byte) tmp);
        flags.setC(tmp < 0x100);
        interruptsAndNextOpcode();
    }

    private void cpyInstr() {
        short tmp = (short) ((registerY & 0xff) - (cycleData & 0xff));
        flags.setNZ((byte) tmp);
        flags.setC(tmp < 0x1009);
        interruptsAndNextOpcode();
    }

    private void decInstr() {
        putEffAddrDataByte();
        flags.setNZ(--cycleData);
    }

    private void dexInstr() {
        flags.setNZ(--registerX);
        interruptsAndNextOpcode();
    }

    private void deyInstr() {
        flags.setNZ(--registerY);
        interruptsAndNextOpcode();
    }

    private void eorInstr() {
        flags.setNZ(registerAccumulator ^= cycleData);
        interruptsAndNextOpcode();
    }

    private void incInstr() {
        putEffAddrDataByte();
        flags.setNZ(++cycleData);
    }

    private void inxInstr() {
        flags.setNZ(++registerX);
        interruptsAndNextOpcode();
    }

    private void inyInstr() {
        flags.setNZ(++registerY);
        interruptsAndNextOpcode();
    }

    private void ldaInstr() {
        flags.setNZ(registerAccumulator = cycleData);
        interruptsAndNextOpcode();
    }

    private void ldxInstr() {
        flags.setNZ(registerX = cycleData);
        interruptsAndNextOpcode();
    }

    private void ldyInstr() {
        flags.setNZ(registerY = cycleData);
        interruptsAndNextOpcode();
    }

    private void lsrInstr() {
        putEffAddrDataByte();
        flags.setC((cycleData & 0x01) != 0);
        flags.setNZ(cycleData >>= 1);
    }

    private void lsraInstr() {
        flags.setC((registerAccumulator & 0x01) != 0);
        flags.setNZ(registerAccumulator >>= 1);
        interruptsAndNextOpcode();
    }

    private void oraInstr() {
        flags.setNZ(registerAccumulator |= cycleData);
        interruptsAndNextOpcode();
    }

    private void plaInstr() {
        registerStackPointer++;
        short addr = to16(SP_PAGE, registerStackPointer);
        flags.setNZ(registerAccumulator = cpuRead(addr));
    }

    private void plpInstr() {
        interruptsAndNextOpcode();
    }

    private void rolInstr() {
        byte newC = (byte) (cycleData & 0x80);
        putEffAddrDataByte();
        cycleData <<= 1;
        if (flags.getC())
            cycleData |= 0x01;
        flags.setNZ(cycleData);
        flags.setC(newC != 0);
    }

    private void rolaInstr() {
        byte newC = (byte) (registerAccumulator & 0x80);
        registerAccumulator <<= 1;
        if (flags.getC())
            registerAccumulator |= 0x01;
        flags.setNZ(registerAccumulator);
        flags.setC(newC != 0);
        interruptsAndNextOpcode();
    }

    private void rorInstr() {
        byte newC = (byte) (cycleData & 0x01);
        putEffAddrDataByte();
        cycleData >>= 1;
        if (flags.getC())
            cycleData |= 0x80;
        flags.setNZ(cycleData);
        flags.setC(newC != 0);
    }

    private void roraInstr() {
        byte newC = (byte) (registerAccumulator & 0x01);
        registerAccumulator >>= 1;
        if (flags.getC())
            registerAccumulator |= 0x80;
        flags.setNZ(registerAccumulator);
        flags.setC(newC != 0);
        interruptsAndNextOpcode();
    }

    private void sbxInstr() {
        int tmp = (registerX & registerAccumulator) - cycleData;
        flags.setNZ(registerX = (byte) (tmp & 0xff));
        flags.setC(tmp < 0x100);
        interruptsAndNextOpcode();
    }

    private void sbcInstr() {
        doSBC();
        interruptsAndNextOpcode();
    }

    private void secInstr() {
        flags.setC(true);
        interruptsAndNextOpcode();
    }

    private void shsInstr() {
        registerStackPointer = (byte) (registerAccumulator & registerX);
        cycleData = registerStackPointer;
        shInstr(registerY);
    }

    private void taxInstr() {
        flags.setNZ(registerX = registerAccumulator);
        interruptsAndNextOpcode();
    }

    private void tayInstr() {
        flags.setNZ(registerY = registerAccumulator);
        interruptsAndNextOpcode();
    }

    private void tsxInstr() {
        flags.setNZ(registerX = registerStackPointer);
        interruptsAndNextOpcode();
    }

    private void txaInstr() {
        flags.setNZ(registerAccumulator = registerX);
        interruptsAndNextOpcode();
    }

    private void txsInstr() {
        registerStackPointer = registerX;
        interruptsAndNextOpcode();
    }

    private void tyaInstr() {
        flags.setNZ(registerAccumulator = registerY);
        interruptsAndNextOpcode();
    }

    /**
     * @throws haltInstruction
     */
    private void invalidOpcode() {
        throw new haltInstruction();
    }

    // Generic Instruction Undocumented Opcodes
    // See documented 6502-nmo.opc by Adam Vardy for more details

    /**
     * Undocumented - This opcode ANDs the contents of the a register with an immediate value and
     * then LSRs the result.
     */
    private void alrInstr() {
        registerAccumulator &= cycleData;
        flags.setC((registerAccumulator & 0x01) != 0);
        flags.setNZ(registerAccumulator >>= 1);
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - ANC ANDs the contents of the a register with an immediate value and then
     * moves bit 7 of a into the Carry flag.  This opcode works basically
     * identically to AND #immed. except that the Carry flag instanceof set to the same
     * state that the Negative flag instanceof set to.
     */
    private void ancInstr() {
        flags.setNZ(registerAccumulator &= cycleData);
        flags.setC(flags.getN());
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - This opcode ANDs the contents of the a register with an immediate value and
     * then RORs the result. (Implementation based on that of Frodo C64 Emulator)
     */
    private void arrInstr() {
        byte data = (byte) (cycleData & registerAccumulator);
        registerAccumulator = (byte) (data >> 1);
        if (flags.getC())
            registerAccumulator |= 0x80;

        if (flags.getD()) {
            flags.setN(flags.getC());
            flags.setZ(registerAccumulator == 0);
            flags.setV(((data ^ registerAccumulator) & 0x40) != 0);

            if ((data & 0x0f) + (data & 0x01) > 5)
                registerAccumulator = (byte) ((registerAccumulator & 0xf0) | ((registerAccumulator + 6) & 0x0f));
            flags.setC(((data + (data & 0x10)) & 0x1f0) > 0x50);
            if (flags.getC())
                registerAccumulator += 0x60;
        } else {
            flags.setNZ(registerAccumulator);
            flags.setC((registerAccumulator & 0x40) != 0);
            flags.setV(((registerAccumulator & 0x40) ^ ((registerAccumulator & 0x20) << 1)) != 0);
        }
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - This opcode ASLs the contents of a memory location and then ORs the result
     * with the accumulator.
     */
    private void asoInstr() {
        putEffAddrDataByte();
        flags.setC((cycleData & 0x80) != 0);
        cycleData <<= 1;
        flags.setNZ(registerAccumulator |= cycleData);
    }

    /**
     * Undocumented - This opcode DECs the contents of a memory location and then CMPs the result
     * with the a register.
     */
    private void dcmInstr() {
        putEffAddrDataByte();
        cycleData--;
        short tmp = (short) (registerAccumulator - cycleData);
        flags.setNZ((byte) tmp);
        flags.setC(tmp < 0x100);
    }

    /**
     * Undocumented - This opcode INCs the contents of a memory location and then SBCs the result
     * from the a register.
     */
    private void insInstr() {
        putEffAddrDataByte();
        cycleData++;
        doSBC();
    }

    /**
     * Undocumented - This opcode ANDs the contents of a memory location with the contents of the
     * stack pointer register and stores the result : the accumulator, the x
     * register, and the stack pointer. Affected Flags: N Z.
     */
    private void lasInstr() {
        flags.setNZ(cycleData &= registerStackPointer);
        registerAccumulator = cycleData;
        registerX = cycleData;
        registerStackPointer = cycleData;
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - This opcode loads both the accumulator and the x register with the contents
     * of a memory location.
     */
    private void laxInstr() {
        flags.setNZ(registerAccumulator = registerX = cycleData);
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - LSE LSRs the contents of a memory location and then EORs the result with
     * the accumulator.
     */
    private void lseInstr() {
        putEffAddrDataByte();
        flags.setC((cycleData & 0x01) != 0);
        cycleData >>= 1;
        flags.setNZ(registerAccumulator ^= cycleData);
    }

    /**
     * Undocumented - This opcode ORs the a register with // #xx (the "magic" value),
     * ANDs the result with an immediate value, and then stores the result : both a and x.
     */
    private void oalInstr() {
        flags.setNZ(registerX = (registerAccumulator = (byte) (cycleData & (registerAccumulator | magic))));
        interruptsAndNextOpcode();
    }

    /**
     * Undocumented - RLA ROLs the contents of a memory location and then ANDs the result with
     * the accumulator.
     */
    private void rlaInstr() {
        byte newC = (byte) (cycleData & 0x80);
        putEffAddrDataByte();
        cycleData = (byte) (cycleData << 1);
        if (flags.getC())
            cycleData |= 0x01;
        flags.setC(newC != 0);
        flags.setNZ(registerAccumulator &= cycleData);
    }

    /**
     * Undocumented - RRA RORs the contents of a memory location and then ADCs the result with
     * the accumulator.
     */
    private void rraInstr() {
        byte newC = (byte) (cycleData & 0x01);
        putEffAddrDataByte();
        cycleData >>= 1;
        if (flags.getC())
            cycleData |= 0x80;
        flags.setC(newC != 0);
        doADC();
    }


    /**
     * Create new CPU emu.
     * <p>
     *
     * @param scheduler The Event Context
     */
    protected Mos6510(EventScheduler scheduler) {
        eventScheduler = scheduler;
// #if DEBUG
        //m_fdbg = stdout;
// #endif
        noSteal = new EventCallback<>("CPU-nosteal", this, this::eventWithoutSteals);
        steal = new EventCallback<>("CPU-steal", this, this::eventWithSteals);
        buildInstructionTable();

        // Intialise Processor Registers
        registerAccumulator = 0;
        registerX = 0;
        registerY = 0;

        cycleEffectiveAddress = 0;
        cycleData = 0;
// # if DEBUG
//            dodump = false;
// #endif
        initialise();
    }

    public enum AccessMode {WRITE, READ}

    /**
     * Build up the processor instruction table.
     */
    private void buildInstructionTable() {
        for (int i = 0; i < instrTable.length; i++) instrTable[i] = new ProcessorCycle();

        for (int i = 0; i < 0x100; i++) {
// #if DEBUG
            //System.err.printf("Building Command %d[%02x]... ", i, i);
// #endif

             //So: what cycles are marked as stealable? Rules are:
             //- CPU performs either read or write at every cycle. Reads are
             //  always stealable. Writes are rare.
             //- Every instruction begins with a sequence of reads. Writes,
             //  if any, are at the end for most instructions.

            int buildCycle = i << 3;

            AccessMode access = AccessMode.WRITE;
            boolean legalMode = true;
            boolean legalInstr = true;

            switch (i) {
            // Accumulator or Implied addressing
            case OpCodes.ASLn:
            case OpCodes.CLCn:
            case OpCodes.CLDn:
            case OpCodes.CLIn:
            case OpCodes.CLVn:
            case OpCodes.DEXn:
            case OpCodes.DEYn:
            case OpCodes.INXn:
            case OpCodes.INYn:
            case OpCodes.LSRn:
            case OpCodes.NOPn:
            case 0x1A:
            case 0x3A:
            case 0x5A:
            case 0x7A:
            case 0xDA:
            case 0xFA:
            case OpCodes.PHAn:
            case OpCodes.PHPn:
            case OpCodes.PLAn:
            case OpCodes.PLPn:
            case OpCodes.ROLn:
            case OpCodes.RORn:
            case OpCodes.SECn:
            case OpCodes.SEDn:
            case OpCodes.SEIn:
            case OpCodes.TAXn:
            case OpCodes.TAYn:
            case OpCodes.TSXn:
            case OpCodes.TXAn:
            case OpCodes.TXSn:
            case OpCodes.TYAn:
                instrTable[buildCycle++].func = this::throwAwayFetch;
                break;

            // Immediate and Relative Addressing Mode Handler
            case OpCodes.ADCb:
            case OpCodes.ANDb:
            case OpCodes.ANCb:
            case 0x2B:
            case OpCodes.ANEb:
            case OpCodes.ASRb:
            case OpCodes.ARRb:
            case OpCodes.BCCr:
            case OpCodes.BCSr:
            case OpCodes.BEQr:
            case OpCodes.BMIr:
            case OpCodes.BNEr:
            case OpCodes.BPLr:
            case OpCodes.BRKn:
            case OpCodes.BVCr:
            case OpCodes.BVSr:
            case OpCodes.CMPb:
            case OpCodes.CPXb:
            case OpCodes.CPYb:
            case OpCodes.EORb:
            case OpCodes.LDAb:
            case OpCodes.LDXb:
            case OpCodes.LDYb:
            case OpCodes.LXAb:
            case OpCodes.NOPb:
            case 0x82:
            case 0xC2:
            case 0xE2:
            case 0x89:
            case OpCodes.ORAb:
            case OpCodes.SBCb:
            case 0XEB:
            case OpCodes.SBXb:
            case OpCodes.RTIn:
            case OpCodes.RTSn:
                instrTable[buildCycle++].func = this::FetchDataByte;
                break;

            // Zero Page Addressing Mode Handler - Read & RMW
            case OpCodes.ADCz:
            case OpCodes.ANDz:
            case OpCodes.BITz:
            case OpCodes.CMPz:
            case OpCodes.CPXz:
            case OpCodes.CPYz:
            case OpCodes.EORz:
            case OpCodes.LAXz:
            case OpCodes.LDAz:
            case OpCodes.LDXz:
            case OpCodes.LDYz:
            case OpCodes.ORAz:
            case OpCodes.NOPz:
            case 0x44:
            case 0x64:
            case OpCodes.SBCz:
            case OpCodes.ASLz:
            case OpCodes.DCPz:
            case OpCodes.DECz:
            case OpCodes.INCz:
            case OpCodes.ISBz:
            case OpCodes.LSRz:
            case OpCodes.ROLz:
            case OpCodes.RORz:
            case OpCodes.SREz:
            case OpCodes.SLOz:
            case OpCodes.RLAz:
            case OpCodes.RRAz:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddr;
                break;
            case OpCodes.SAXz:
            case OpCodes.STAz:
            case OpCodes.STXz:
            case OpCodes.STYz:
                instrTable[buildCycle++].func = this::fetchLowAddr;
                break;

            // Zero Page with x Offset Addressing Mode Handler
            // these issue extra reads on the 0 page, but we don't care about it
            // because there are no detectable effects from them. These reads
            // occur during the "wasted" cycle.
            case OpCodes.ADCzx:
            case OpCodes.ANDzx:
            case OpCodes.CMPzx:
            case OpCodes.EORzx:
            case OpCodes.LDAzx:
            case OpCodes.LDYzx:
            case OpCodes.NOPzx:
            case 0x34:
            case 0x54:
            case 0x74:
            case 0xD4:
            case 0xF4:
            case OpCodes.ORAzx:
            case OpCodes.SBCzx:
            case OpCodes.ASLzx:
            case OpCodes.DCPzx:
            case OpCodes.DECzx:
            case OpCodes.INCzx:
            case OpCodes.ISBzx:
            case OpCodes.LSRzx:
            case OpCodes.RLAzx:
            case OpCodes.ROLzx:
            case OpCodes.RORzx:
            case OpCodes.RRAzx:
            case OpCodes.SLOzx:
            case OpCodes.SREzx:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddrX;
                // operates on 0 page : read mode. Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                break;
            case OpCodes.STAzx:
            case OpCodes.STYzx:
                instrTable[buildCycle++].func = this::fetchLowAddrX;
                // operates on 0 page : read mode. Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                break;

            // Zero Page with y Offset Addressing Mode Handler
            case OpCodes.LDXzy:
            case OpCodes.LAXzy:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddrY;
                // operates on 0 page : read mode. Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                break;
            case OpCodes.STXzy:
            case OpCodes.SAXzy:
                instrTable[buildCycle++].func = this::fetchLowAddrY;
                // operates on 0 page : read mode. Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                break;

            // Absolute Addressing Mode Handler
            case OpCodes.ADCa:
            case OpCodes.ANDa:
            case OpCodes.BITa:
            case OpCodes.CMPa:
            case OpCodes.CPXa:
            case OpCodes.CPYa:
            case OpCodes.EORa:
            case OpCodes.LAXa:
            case OpCodes.LDAa:
            case OpCodes.LDXa:
            case OpCodes.LDYa:
            case OpCodes.NOPa:
            case OpCodes.ORAa:
            case OpCodes.SBCa:
            case OpCodes.ASLa:
            case OpCodes.DCPa:
            case OpCodes.DECa:
            case OpCodes.INCa:
            case OpCodes.ISBa:
            case OpCodes.LSRa:
            case OpCodes.ROLa:
            case OpCodes.RORa:
            case OpCodes.SLOa:
            case OpCodes.SREa:
            case OpCodes.RLAa:
            case OpCodes.RRAa:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddr;
                break;
            case OpCodes.JMPw:
            case OpCodes.SAXa:
            case OpCodes.STAa:
            case OpCodes.STXa:
            case OpCodes.STYa:
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddr;
                break;

            case OpCodes.JSRw:
                instrTable[buildCycle++].func = this::fetchLowAddr;
                break;

            // Absolute With x Offset Addressing Mode Handler (Read)
            case OpCodes.ADCax:
            case OpCodes.ANDax:
            case OpCodes.CMPax:
            case OpCodes.EORax:
            case OpCodes.LDAax:
            case OpCodes.LDYax:
            case OpCodes.NOPax:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
            case OpCodes.ORAax:
            case OpCodes.SBCax:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::FetchHighAddrX2;
                // this cycle instanceof skipped if the address instanceof already correct.
                // otherwise, it will be read and ignored.
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;

            // Absolute x (RMW; no page crossing handled, always reads before writing)
            case OpCodes.ASLax:
            case OpCodes.DCPax:
            case OpCodes.DECax:
            case OpCodes.INCax:
            case OpCodes.ISBax:
            case OpCodes.LSRax:
            case OpCodes.RLAax:
            case OpCodes.ROLax:
            case OpCodes.RORax:
            case OpCodes.RRAax:
            case OpCodes.SLOax:
            case OpCodes.SREax:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddrX;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;
            case OpCodes.SHYax:
            case OpCodes.STAax:
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddrX;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;

            // Absolute With y Offset Addresing Mode Handler (Read)
            case OpCodes.ADCay:
            case OpCodes.ANDay:
            case OpCodes.CMPay:
            case OpCodes.EORay:
            case OpCodes.LASay:
            case OpCodes.LAXay:
            case OpCodes.LDAay:
            case OpCodes.LDXay:
            case OpCodes.ORAay:
            case OpCodes.SBCay:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddrY2;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;

            // Absolute y (No page crossing handled)
            case OpCodes.DCPay:
            case OpCodes.ISBay:
            case OpCodes.RLAay:
            case OpCodes.RRAay:
            case OpCodes.SLOay:
            case OpCodes.SREay:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddrY;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;
            case OpCodes.SHAay:
            case OpCodes.SHSay:
            case OpCodes.SHXay:
            case OpCodes.STAay:
                instrTable[buildCycle++].func = this::fetchLowAddr;
                instrTable[buildCycle++].func = this::fetchHighAddrY;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;

            // Absolute Indirect Addressing Mode Handler
            case OpCodes.JMPi:
                instrTable[buildCycle++].func = this::fetchlowpointer;
                instrTable[buildCycle++].func = this::fetchHighPointer;
                instrTable[buildCycle++].func = this::fetchLowEffAddr;
                instrTable[buildCycle++].func = this::fetchHighEffAddr;
                break;

            // Indexed with x Preinc Addressing Mode Handler
            case OpCodes.ADCix:
            case OpCodes.ANDix:
            case OpCodes.CMPix:
            case OpCodes.EORix:
            case OpCodes.LAXix:
            case OpCodes.LDAix:
            case OpCodes.ORAix:
            case OpCodes.SBCix:
            case OpCodes.DCPix:
            case OpCodes.ISBix:
            case OpCodes.SLOix:
            case OpCodes.SREix:
            case OpCodes.RLAix:
            case OpCodes.RRAix:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchlowpointer;
                instrTable[buildCycle++].func = this::fetchLowPointerX;
                instrTable[buildCycle++].func = this::fetchLowEffAddr;
                instrTable[buildCycle++].func = this::fetchHighEffAddr;
                break;
            case OpCodes.SAXix:
            case OpCodes.STAix:
                instrTable[buildCycle++].func = this::fetchlowpointer;
                instrTable[buildCycle++].func = this::fetchLowPointerX;
                instrTable[buildCycle++].func = this::fetchLowEffAddr;
                instrTable[buildCycle++].func = this::fetchHighEffAddr;
                break;

            // Indexed with y Postinc Addressing Mode Handler (Read)
            case OpCodes.ADCiy:
            case OpCodes.ANDiy:
            case OpCodes.CMPiy:
            case OpCodes.EORiy:
            case OpCodes.LAXiy:
            case OpCodes.LDAiy:
            case OpCodes.ORAiy:
            case OpCodes.SBCiy:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchlowpointer;
                instrTable[buildCycle++].func = this::fetchLowEffAddr;
                instrTable[buildCycle++].func = this::fetchHighEffAddrY2;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;

            // Indexed y (No page crossing handled)
            case OpCodes.DCPiy:
            case OpCodes.ISBiy:
            case OpCodes.RLAiy:
            case OpCodes.RRAiy:
            case OpCodes.SLOiy:
            case OpCodes.SREiy:
                access = AccessMode.READ;
                instrTable[buildCycle++].func = this::fetchlowpointer;
                instrTable[buildCycle++].func = this::fetchLowEffAddr;
                instrTable[buildCycle++].func = this::fetchHighEffAddrY;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;
            case OpCodes.SHAiy:
            case OpCodes.STAiy:
                instrTable[buildCycle++].func = this::fetchlowpointer;
                instrTable[buildCycle++].func = this::fetchLowEffAddr;
                instrTable[buildCycle++].func = this::fetchHighEffAddrY;
                instrTable[buildCycle++].func = this::throwAwayRead;
                break;

            default:
                legalMode = false;
                break;
            }

            if (access == AccessMode.READ) {
                instrTable[buildCycle++].func = this::fetchEffAddrDataByte;
            }

            //
            // Addressing Modes Finished, other cycles are instruction dependent
            //
            switch (i) {
            case OpCodes.ADCz:
            case OpCodes.ADCzx:
            case OpCodes.ADCa:
            case OpCodes.ADCax:
            case OpCodes.ADCay:
            case OpCodes.ADCix:
            case OpCodes.ADCiy:
            case OpCodes.ADCb:
                instrTable[buildCycle++].func = this::adcInstr;
                break;

            case OpCodes.ANCb:
            case 0x2B:
                instrTable[buildCycle++].func = this::ancInstr;
                break;

            case OpCodes.ANDz:
            case OpCodes.ANDzx:
            case OpCodes.ANDa:
            case OpCodes.ANDax:
            case OpCodes.ANDay:
            case OpCodes.ANDix:
            case OpCodes.ANDiy:
            case OpCodes.ANDb:
                instrTable[buildCycle++].func = this::andInstr;
                break;

            case OpCodes.ANEb: // Also known as XAA
                instrTable[buildCycle++].func = this::aneInstr;
                break;

            case OpCodes.ARRb:
                instrTable[buildCycle++].func = this::arrInstr;
                break;

            case OpCodes.ASLn:
                instrTable[buildCycle++].func = this::aslaInstr;
                break;

            case OpCodes.ASLz:
            case OpCodes.ASLzx:
            case OpCodes.ASLa:
            case OpCodes.ASLax:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::aslInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.ASRb: // Also known as ALR
                instrTable[buildCycle++].func = this::alrInstr;
                break;

            case OpCodes.BCCr:
                instrTable[buildCycle++].func = this::bccInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BCSr:
                instrTable[buildCycle++].func = this::bcsInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BEQr:
                instrTable[buildCycle++].func = this::beqInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BITz:
            case OpCodes.BITa:
                instrTable[buildCycle++].func = this::bitInstr;
                break;

            case OpCodes.BMIr:
                instrTable[buildCycle++].func = this::bmiInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BNEr:
                instrTable[buildCycle++].func = this::bneInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BPLr:
                instrTable[buildCycle++].func = this::bplInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BRKn:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::pushHighPC;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::brkPushLowPC;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::brkInstr;
                instrTable[buildCycle++].func = this::irqLoRequest;
                instrTable[buildCycle++].func = this::irqHiRequest;
                instrTable[buildCycle++].func = this::fetchNextOpcode;
                break;

            case OpCodes.BVCr:
                instrTable[buildCycle++].func = this::bvcInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.BVSr:
                instrTable[buildCycle++].func = this::bvsInstr;
                instrTable[buildCycle++].func = this::fixBranch;
                break;

            case OpCodes.CLCn:
                instrTable[buildCycle++].func = this::clcInstr;
                break;

            case OpCodes.CLDn:
                instrTable[buildCycle++].func = this::cldInstr;
                break;

            case OpCodes.CLIn:
                instrTable[buildCycle++].func = this::cliInstr;
                break;

            case OpCodes.CLVn:
                instrTable[buildCycle++].func = this::clvInstr;
                break;

            case OpCodes.CMPz:
            case OpCodes.CMPzx:
            case OpCodes.CMPa:
            case OpCodes.CMPax:
            case OpCodes.CMPay:
            case OpCodes.CMPix:
            case OpCodes.CMPiy:
            case OpCodes.CMPb:
                instrTable[buildCycle++].func = this::cmpInstr;
                break;

            case OpCodes.CPXz:
            case OpCodes.CPXa:
            case OpCodes.CPXb:
                instrTable[buildCycle++].func = this::cpxInstr;
                break;

            case OpCodes.CPYz:
            case OpCodes.CPYa:
            case OpCodes.CPYb:
                instrTable[buildCycle++].func = this::cpyInstr;
                break;

            case OpCodes.DCPz:
            case OpCodes.DCPzx:
            case OpCodes.DCPa:
            case OpCodes.DCPax:
            case OpCodes.DCPay:
            case OpCodes.DCPix:
            case OpCodes.DCPiy: // Also known as DCM
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::dcmInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.DECz:
            case OpCodes.DECzx:
            case OpCodes.DECa:
            case OpCodes.DECax:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::decInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.DEXn:
                instrTable[buildCycle++].func = this::dexInstr;
                break;

            case OpCodes.DEYn:
                instrTable[buildCycle++].func = this::deyInstr;
                break;

            case OpCodes.EORz:
            case OpCodes.EORzx:
            case OpCodes.EORa:
            case OpCodes.EORax:
            case OpCodes.EORay:
            case OpCodes.EORix:
            case OpCodes.EORiy:
            case OpCodes.EORb:
                instrTable[buildCycle++].func = this::eorInstr;
                break;
            case OpCodes.INCz:
            case OpCodes.INCzx:
            case OpCodes.INCa:
            case OpCodes.INCax:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::incInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.INXn:
                instrTable[buildCycle++].func = this::inxInstr;
                break;

            case OpCodes.INYn:
                instrTable[buildCycle++].func = this::inyInstr;
                break;

            case OpCodes.ISBz:
            case OpCodes.ISBzx:
            case OpCodes.ISBa:
            case OpCodes.ISBax:
            case OpCodes.ISBay:
            case OpCodes.ISBix:
            case OpCodes.ISBiy: // Also known as INS
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::insInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.JSRw:
                instrTable[buildCycle++].func = this::wasteCycle;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::pushHighPC;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::pushLowPC;
                instrTable[buildCycle++].func = this::fetchHighAddr;
                instrTable[buildCycle++].func = this::jmpInstr;
                break;
            case OpCodes.JMPw:
            case OpCodes.JMPi:
                instrTable[buildCycle++].func = this::jmpInstr;
                break;

            case OpCodes.LASay:
                instrTable[buildCycle++].func = this::lasInstr;
                break;

            case OpCodes.LAXz:
            case OpCodes.LAXzy:
            case OpCodes.LAXa:
            case OpCodes.LAXay:
            case OpCodes.LAXix:
            case OpCodes.LAXiy:
                instrTable[buildCycle++].func = this::laxInstr;
                break;

            case OpCodes.LDAz:
            case OpCodes.LDAzx:
            case OpCodes.LDAa:
            case OpCodes.LDAax:
            case OpCodes.LDAay:
            case OpCodes.LDAix:
            case OpCodes.LDAiy:
            case OpCodes.LDAb:
                instrTable[buildCycle++].func = this::ldaInstr;
                break;

            case OpCodes.LDXz:
            case OpCodes.LDXzy:
            case OpCodes.LDXa:
            case OpCodes.LDXay:
            case OpCodes.LDXb:
                instrTable[buildCycle++].func = this::ldxInstr;
                break;

            case OpCodes.LDYz:
            case OpCodes.LDYzx:
            case OpCodes.LDYa:
            case OpCodes.LDYax:
            case OpCodes.LDYb:
                instrTable[buildCycle++].func = this::ldyInstr;
                break;

            case OpCodes.LSRn:
                instrTable[buildCycle++].func = this::lsraInstr;
                break;

            case OpCodes.LSRz:
            case OpCodes.LSRzx:
            case OpCodes.LSRa:
            case OpCodes.LSRax:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::lsrInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.NOPn:
            case 0x1A:
            case 0x3A:
            case 0x5A:
            case 0x7A:
            case 0xDA:
            case 0xFA:
            case OpCodes.NOPb:
            case 0x82:
            case 0xC2:
            case 0xE2:
            case 0x89:
            case OpCodes.NOPz:
            case 0x44:
            case 0x64:
            case OpCodes.NOPzx:
            case 0x34:
            case 0x54:
            case 0x74:
            case 0xD4:
            case 0xF4:
            case OpCodes.NOPa:
            case OpCodes.NOPax:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
                // NOPb NOPz NOPzx - Also known as SKBn
                // NOPa NOPax      - Also known as SKWn
                break;

            case OpCodes.LXAb: // Also known as OAL
                instrTable[buildCycle++].func = this::oalInstr;
                break;

            case OpCodes.ORAz:
            case OpCodes.ORAzx:
            case OpCodes.ORAa:
            case OpCodes.ORAax:
            case OpCodes.ORAay:
            case OpCodes.ORAix:
            case OpCodes.ORAiy:
            case OpCodes.ORAb:
                instrTable[buildCycle++].func = this::oraInstr;
                break;

            case OpCodes.PHAn:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::phaInstr;
                break;

            case OpCodes.PHPn:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::pushSR;
                break;

            case OpCodes.PLAn:
                // should read the value at current stack register.
                // Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                instrTable[buildCycle++].func = this::plaInstr;
                break;

            case OpCodes.PLPn:
                // should read the value at current stack register.
                // Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                instrTable[buildCycle++].func = this::PopSR;
                instrTable[buildCycle++].func = this::plpInstr;
                break;

            case OpCodes.RLAz:
            case OpCodes.RLAzx:
            case OpCodes.RLAix:
            case OpCodes.RLAa:
            case OpCodes.RLAax:
            case OpCodes.RLAay:
            case OpCodes.RLAiy:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::rlaInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.ROLn:
                instrTable[buildCycle++].func = this::rolaInstr;
                break;

            case OpCodes.ROLz:
            case OpCodes.ROLzx:
            case OpCodes.ROLa:
            case OpCodes.ROLax:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::rolInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.RORn:
                instrTable[buildCycle++].func = this::roraInstr;
                break;

            case OpCodes.RORz:
            case OpCodes.RORzx:
            case OpCodes.RORa:
            case OpCodes.RORax:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::rorInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.RRAa:
            case OpCodes.RRAax:
            case OpCodes.RRAay:
            case OpCodes.RRAz:
            case OpCodes.RRAzx:
            case OpCodes.RRAix:
            case OpCodes.RRAiy:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::rraInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.RTIn:
                // should read the value at current stack register.
                // Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                instrTable[buildCycle++].func = this::PopSR;
                instrTable[buildCycle++].func = this::PopLowPC;
                instrTable[buildCycle++].func = this::PopHighPC;
                instrTable[buildCycle++].func = this::rtiInstr;
                break;

            case OpCodes.RTSn:
                // should read the value at current stack register.
                // Truly side-effect free.
                instrTable[buildCycle++].func = this::wasteCycle;
                instrTable[buildCycle++].func = this::PopLowPC;
                instrTable[buildCycle++].func = this::PopHighPC;
                instrTable[buildCycle++].func = this::rtsInstr;
                break;

            case OpCodes.SAXz:
            case OpCodes.SAXzy:
            case OpCodes.SAXa:
            case OpCodes.SAXix: // Also known as AXS
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::axsInstr;
                break;

            case OpCodes.SBCz:
            case OpCodes.SBCzx:
            case OpCodes.SBCa:
            case OpCodes.SBCax:
            case OpCodes.SBCay:
            case OpCodes.SBCix:
            case OpCodes.SBCiy:
            case OpCodes.SBCb:
            case 0XEB:
                instrTable[buildCycle++].func = this::sbcInstr;
                break;

            case OpCodes.SBXb:
                instrTable[buildCycle++].func = this::sbxInstr;
                break;

            case OpCodes.SECn:
                instrTable[buildCycle++].func = this::secInstr;
                break;

            case OpCodes.SEDn:
                instrTable[buildCycle++].func = this::sedInstr;
                break;

            case OpCodes.SEIn:
                instrTable[buildCycle++].func = this::seiInstr;
                break;

            case OpCodes.SHAay:
            case OpCodes.SHAiy: // Also known as AXA
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::axaInstr;
                break;

            case OpCodes.SHSay: // Also known as TAS
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::shsInstr;
                break;

            case OpCodes.SHXay: // Also known as XAS
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::xasInstr;
                break;

            case OpCodes.SHYax: // Also known as SAY
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::sayInstr;
                break;

            case OpCodes.SLOz:
            case OpCodes.SLOzx:
            case OpCodes.SLOa:
            case OpCodes.SLOax:
            case OpCodes.SLOay:
            case OpCodes.SLOix:
            case OpCodes.SLOiy: // Also known as ASO
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::asoInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.SREz:
            case OpCodes.SREzx:
            case OpCodes.SREa:
            case OpCodes.SREax:
            case OpCodes.SREay:
            case OpCodes.SREix:
            case OpCodes.SREiy: // Also known as LSE
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::lseInstr;
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::putEffAddrDataByte;
                break;

            case OpCodes.STAz:
            case OpCodes.STAzx:
            case OpCodes.STAa:
            case OpCodes.STAax:
            case OpCodes.STAay:
            case OpCodes.STAix:
            case OpCodes.STAiy:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::staInstr;
                break;

            case OpCodes.STXz:
            case OpCodes.STXzy:
            case OpCodes.STXa:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::stxInstr;
                break;

            case OpCodes.STYz:
            case OpCodes.STYzx:
            case OpCodes.STYa:
                instrTable[buildCycle].nosteal = true;
                instrTable[buildCycle++].func = this::styInstr;
                break;

            case OpCodes.TAXn:
                instrTable[buildCycle++].func = this::taxInstr;
                break;

            case OpCodes.TAYn:
                instrTable[buildCycle++].func = this::tayInstr;
                break;

            case OpCodes.TSXn:
                instrTable[buildCycle++].func = this::tsxInstr;
                break;

            case OpCodes.TXAn:
                instrTable[buildCycle++].func = this::txaInstr;
                break;

            case OpCodes.TXSn:
                instrTable[buildCycle++].func = this::txsInstr;
                break;

            case OpCodes.TYAn:
                instrTable[buildCycle++].func = this::tyaInstr;
                break;

            default:
                legalInstr = false;
                break;
            }

            // Missing an addressing mode or implementation makes opcode invalid.
            // These are normally called HLT instructions. In the hardware, the
            // CPU state machine locks up and will never recover.
            if (!(legalMode && legalInstr)) {
                instrTable[buildCycle++].func = this::invalidOpcode;
            }

            // check for IRQ triggers or fetch next opcode...
            instrTable[buildCycle].func = this::interruptsAndNextOpcode;

// #if DEBUG
            //System.err.printf("Done [%d Cycles]\n", buildCycle - (i << 3));
// #endif
        }
    }

    /**
     * Initialise CPU Emulation (Registers).
     */
    private void initialise() {
        // Reset stack
        registerStackPointer = (byte) 0xFF;

        // Reset Cycle Count
        cycleCount = (OpCodes.BRKn << 3) + 6; // fetchNextOpcode

        // Reset Status Register
        flags.reset();

        // Set pc to some value
        registerProgramCounter = 0;

        // IRQs pending check
        irqAssertedOnPin = false;
        nmiFlag = false;
        rstFlag = false;
        interruptCycle = MAX;

        // Signals
        rdy = true;

        eventScheduler.schedule(noSteal, 0, EventPhase.CLOCK_PHI2);
    }

    /**
     * Reset CPU Emulation.
     */
    public void reset() {
        // private Stuff
        initialise();

        // Set processor port to the default values
        writeCpu((byte) 0, (byte) 0x2f);
        writeCpu((byte) 1, (byte) 0x37);

        // Requires External Bits
        // Read from reset vector for program entry point
        cycleEffectiveAddress = to16lo8(cycleEffectiveAddress, cpuRead((short) 0xfffc));
        cycleEffectiveAddress = to16hi8(cycleEffectiveAddress, cpuRead((short) 0xfffd));
        registerProgramCounter = cycleEffectiveAddress;
    }

    /**
     * Module Credits.
     */
    public String credits() {
        return """
            Mos6510 Cycle Exact Emulation
            \t(C) 2000 Simon A. White
            \t(C) 2008-2010 Antti S. Lankila
            \t(C) 2011-2017 Leandro Nini
        """;
    }

    public void debug(boolean enable, FileStream out_) {
// #if DEBUG
//            dodump = enable;
        //if (!(out_ != null && enable))
        //m_fdbg = stdout;
        //else
        //m_fdbg = out_;
// #endif
    }
}
