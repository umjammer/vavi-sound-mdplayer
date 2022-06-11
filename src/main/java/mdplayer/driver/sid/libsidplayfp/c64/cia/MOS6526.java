/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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

import java.util.Arrays;

import mdplayer.driver.sid.libsidplayfp.EventCallback;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.event_phase_t;
import mdplayer.driver.sid.libsidplayfp.c64.cia.InterruptSource.INTERRUPT;
import mdplayer.driver.sid.libsidplayfp.sidendian;


/**
//This class instanceof heavily based on the ciacore/ciatimer source code from VICE.
//The CIA state machine instanceof lifted as-is. Big thanks to VICE project!
*
//@author alankila
*/
public class MOS6526
{
    //# include <memory>
    //# include <stdint.h>
    //# include "Interrupt.h"
    //# include "timer.h"
    //# include "tod.h"
    //# include "SerialPort.h"
    //# include "EventScheduler.h"
    //# include "sidcxx11.h"

    enum Reg
    {
        PRA,
        PRB,
        DDRA,
        DDRB,
        TAL,
        TAH,
        TBL,
        TBH,
        TOD_TEN,
        TOD_SEC,
        TOD_MIN,
        TOD_HR,
        SDR,
        ICR,
        IDR,
        CRA,
        CRB;
        static Reg valueOf(int v) { return Arrays.stream(values()).filter(e -> e.ordinal() == v).findFirst().get(); }
    };


    /**
     //This instanceof the timer A of this CIA.
     *
     //@author Ken Händel
     *
     */
     public class TimerA extends Timer
    {
        /**
         //Signal underflows of Timer A to Timer B.
         */
        @Override public void underFlow()
        {
            parent.underflowA();
        }

        @Override public void serialPort()
        {
            parent.handleSerialPort();
        }

        /**
         //Create timer A.
         */
        public TimerA(EventScheduler scheduler, MOS6526 parent)
        {
            super("CIA Timer A", scheduler, parent);
        }

    }

    /**
 * This instanceof the timer B of this CIA.
 *
 * @author Ken Händel
 *
 */
    class TimerB extends Timer
    {
        @Override public void underFlow()
        {
            parent.underflowB();
        }

        /**
         //Create timer B.
         */
        public TimerB(EventScheduler scheduler, MOS6526 parent)
        {
            super("CIA Timer B", scheduler, parent);
        }

        /**
         //Receive an underflow from Timer A.
         */
        public void cascade()
        {
            // we pretend that we are CPU doing a write to ctrl register
            syncWithCpu();
            state |= CIAT_STEP;
            wakeUpAfterSyncWithCpu();
        }

        /**
         //Check if start flag instanceof set.
         *
         //@return true if start flag instanceof set, false otherwise
         */
        public Boolean started() { return (state & CIAT_CR_START) != 0; }
    }


    /**
     //InterruptSource that acts like new CIA
     */
    class InterruptSource6526A extends InterruptSource
    {
        public InterruptSource6526A(EventScheduler scheduler, MOS6526 parent)
        {
            super(scheduler, parent);
        }

        @Override public void trigger(byte interruptMask)
        {
            super.trigger(interruptMask);

            if (interruptMasked() && interruptTriggered())
            {
                triggerInterrupt();
                parent.interrupt(true);
            }
        }

        @Override public byte clear()
        {
            if (!interruptTriggered())
            {
                parent.interrupt(false);
            }

            return super.clear();
        }

        @Override public void event_()
        {
            throw new RuntimeException("6526A event called unexpectedly");
        }
    }


    /**
     //InterruptSource that acts like old CIA
     */
    class InterruptSource6526 extends InterruptSource
    {
        // Have we already scheduled CIA->CPU Interrupt transition?
        private Boolean scheduled;

        /**
         //Schedules an IRQ asserting state transition for next cycle.
         */
        private void schedule()
        {
            if (!scheduled)
            {
                eventScheduler.schedule(this, 1, event_phase_t.EVENT_CLOCK_PHI1);
                scheduled = true;
            }
        }

        public InterruptSource6526(EventScheduler scheduler, MOS6526 parent)
        {
            super(scheduler, parent);
        }

        @Override public void trigger(byte interruptMask)
        {
            super.trigger(interruptMask);

            if (interruptMasked() && interruptTriggered())
            {
                schedule();
            }
        }

        @Override public byte clear()
        {
            if (scheduled)
            {
                eventScheduler.cancel(this);
                scheduled = false;
            }

            if (!interruptTriggered())
            {
                parent.interrupt(false);
            }

            return super.clear();
        }

        /**
         //Signal Interrupt to CPU.
         */
        @Override public void event_()
        {
            triggerInterrupt();
            parent.interrupt(true);

            scheduled = false;
        }

        @Override public void reset()
        {
            super.reset();

            scheduled = false;
        }

    }

        //friend class InterruptSource6526;
        //friend class InterruptSource6526A;
        //friend class TimerA;
        //friend class TimerB;
        //friend class Tod;

        //private String credit;

        // Event context.
        protected EventScheduler eventScheduler;

        // Ports
        //@{
        protected byte pra, prb, ddra, ddrb;
        //@}

        // These are all CIA registers.
        protected byte[] regs = new byte[0x10];

        // Timers A and B.
        //@{
        protected TimerA timerA;
        protected TimerB timerB;
        //@}

        // Interrupt Source
        //std::unique_ptr<InterruptSource> interruptSource;
        protected InterruptSource interruptSource;

        // TOD
        protected Tod tod;

        // Serial Data Registers
        protected SerialPort serialPort;

        // Have we already scheduled CIA->CPU Interrupt transition?
        protected Boolean triggerScheduled;

        // Events
        //@{
        protected EventCallback<MOS6526> bTickEvent;
        //@}

        /**
         //Trigger an Interrupt from TOD.
         */
        public void todInterrupt()
        {
            interruptSource.trigger((byte)INTERRUPT.INTERRUPT_ALARM.v);
        }

        /**
         //This event exists solely to break the ambiguity of what scheduling on
         //top of PHI1 causes, because there instanceof no ordering between events on
         //same phase. Thus it instanceof scheduled : PHI2 to ensure the b.event() is
         //run once before the value changes.
         *
         //- PHI1 a.event() (which calls underFlow())
         //- PHI1 b.event()
         //- PHI2 bTick.event()
         //- PHI1 a.event()
         //- PHI1 b.event()
         */
        private void bTick() {
            timerB.cascade();
        }

        /**
         //Timer A underflow.
         */
        public void underflowA()
        {
            interruptSource.trigger((byte)INTERRUPT.INTERRUPT_UNDERFLOW_A.v);

            if ((regs[(int)Reg.CRB.ordinal()] & 0x41) == 0x41)
            {
                if (timerB.started())
                {
                    eventScheduler.schedule(bTickEvent, 0, event_phase_t.EVENT_CLOCK_PHI2);
                }
            }
        }

        /** Timer B underflow. */
        public void underflowB()
        {
            interruptSource.trigger((byte)INTERRUPT.INTERRUPT_UNDERFLOW_B.v);
        }

        /**
         //Handle the serial port.
         */
        public void handleSerialPort()
        {
            if ((regs[(int)Reg.CRA.ordinal()] & 0x40)!=0)
            {
                serialPort.handle(regs[(int)Reg.SDR.ordinal()]);
            }
        }

        /**
         //Create a new CIA.
         *
         //@param context the event context
         */
        protected MOS6526(EventScheduler scheduler)
        {
            eventScheduler = scheduler;
            pra = regs[(int)Reg.PRA.ordinal()];
            prb = regs[(int)Reg.PRB.ordinal()];
            ddra = regs[(int)Reg.DDRA.ordinal()];
            ddrb = regs[(int)Reg.DDRB.ordinal()];
            timerA = new TimerA(scheduler, this);
            timerB = new TimerB(scheduler, this);
            interruptSource = new InterruptSource6526(scheduler, this);
            tod = new Tod(scheduler, this, regs);
            serialPort = new SerialPort(interruptSource);
            bTickEvent=new EventCallback<MOS6526>("CIA B counts A", this, this::bTick);

            reset();
        }

        protected void finalize()
        {
        }

        /**
         //Signal Interrupt.
         *
         //@param state
         //           Interrupt state
         */
        public void interrupt(Boolean state) { }

        protected void portA() { }
        protected void portB() { }

        /**
         //Read CIA register.
         *
         //@param addr
         //           register address to read (lowest 4 bits)
         */
        protected byte read(byte addr)
        {
            addr &= 0x0f;

            timerA.syncWithCpu();
            timerA.wakeUpAfterSyncWithCpu();
            timerB.syncWithCpu();
            timerB.wakeUpAfterSyncWithCpu();

            switch (Reg.valueOf(addr))
            {
                case PRA: // Simulate a serial port
                    return (byte)(regs[(int)Reg.PRA.ordinal()] | ~regs[(int)Reg.DDRA.ordinal()]);
                case PRB:
                    {
                        byte data = (byte)(regs[(int)Reg.PRB.ordinal()] | ~regs[(int)Reg.DDRB.ordinal()]);
                        // Timers can appear on the port
                        if ((regs[(int)Reg.CRA.ordinal()] & 0x02) != 0)
                        {
                            data &= 0xbf;
                            if (timerA.getPb(regs[(int)Reg.CRA.ordinal()]))
                                data |= 0x40;
                        }
                        if ((regs[(int)Reg.CRB.ordinal()] & 0x02) != 0)
                        {
                            data &= 0x7f;
                            if (timerB.getPb(regs[(int)Reg.CRB.ordinal()]))
                                data |= 0x80;
                        }
                        return data;
                    }
                case TAL:
                    return sidendian.endian_16lo8(timerA.getTimer());
                case TAH:
                    return sidendian.endian_16hi8(timerA.getTimer());
                case TBL:
                    return sidendian.endian_16lo8(timerB.getTimer());
                case TBH:
                    return sidendian.endian_16hi8(timerB.getTimer());
                case TOD_TEN:
                case TOD_SEC:
                case TOD_MIN:
                case TOD_HR:
                    return tod.read((byte)(addr - (int)Reg.TOD_TEN.ordinal()));
                case IDR:
                    return interruptSource.clear();
                case CRA:
                    return (byte)((regs[(int)Reg.CRA.ordinal()] & 0xfe) | (byte)(timerA.getState() & 1));
                case CRB:
                    return (byte)((regs[(int)Reg.CRB.ordinal()] & 0xfe) | (byte)(timerB.getState() & 1));
                default:
                    return regs[addr];
            }
        }

        /**
         //Write CIA register.
         *
         //@param addr
         //           register address to write (lowest 4 bits)
         //@param data
         //           value to write
         */
        protected void write(byte addr, byte data) {
            addr &= 0x0f;

            timerA.syncWithCpu();
            timerB.syncWithCpu();

            byte oldData = regs[addr];
            regs[addr] = data;

            switch (Reg.valueOf(addr))
            {
                case PRA:
                case DDRA:
                    portA();
                    break;
                case PRB:
                case DDRB:
                    portB();
                    break;
                case TAL:
                    timerA.latchLo(data);
                    break;
                case TAH:
                    timerA.latchHi(data);
                    break;
                case TBL:
                    timerB.latchLo(data);
                    break;
                case TBH:
                    timerB.latchHi(data);
                    break;
                case TOD_TEN:
                case TOD_SEC:
                case TOD_MIN:
                case TOD_HR:
                    tod.write((byte)(addr - (int)Reg.TOD_TEN.ordinal()), data);
                    break;
                case SDR:
                    if ((regs[(int)Reg.CRA.ordinal()] & 0x40)!=0)
                        serialPort.setBuffered();
                    break;
                case ICR:
                    interruptSource.set(data);
                    break;
                case CRA:
                    if ((data & 1)!=0 && (oldData & 1)==0)
                    {
                        // Reset the underflow flipflop for the data port
                        timerA.setPbToggle(true);
                    }
                    timerA.setControlRegister(data);
                    break;
                case CRB:
                    if ((data & 1)!=0 && (oldData & 1)==0)
                    {
                        // Reset the underflow flipflop for the data port
                        timerB.setPbToggle(true);
                    }
                    timerB.setControlRegister((byte)(data | (data & 0x40) >> 1));
                    break;
            }

            timerA.wakeUpAfterSyncWithCpu();
            timerB.wakeUpAfterSyncWithCpu();
        }

        /**
         //Reset CIA.
         */
        public void reset()
        {
            for (int i = 0; i < regs.length; i++) regs[i] = 0;

            serialPort.reset();

            // Reset timers
            timerA.reset();
            timerB.reset();

            // Reset interruptSource
            interruptSource.reset();

            // Reset tod
            tod.reset();

            triggerScheduled = false;

            eventScheduler.cancel(bTickEvent);
        }

        /**
         //Get the credits.
         *
         //@return the credits
         */
        public String credits()
        {
            return
                    "MOS6526/6526A (CIA) Emulation:\n"
            + "\tCopyright (C) 2001-2004 Simon White\n"
            + "\tCopyright (C) 2007-2010 Antti S. Lankila\n"
            + "\tCopyright (C) 2009-2014 VICE Project\n"
            + "\tCopyright (C) 2011-2015 Leandro Nini\n";
        }

        /**
         //Set day-of-time event occurence of rate.
         *
         //@param clock
         */
        public void setDayOfTimeRate(int clock)
        {
            tod.setPeriod(clock);
        }


    }



