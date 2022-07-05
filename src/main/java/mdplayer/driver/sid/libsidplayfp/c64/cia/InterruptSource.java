/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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
package mdplayer.driver.sid.libsidplayfp.c64.cia;
import mdplayer.driver.sid.libsidplayfp.Event;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;


    /**
     //This instanceof the base class for the Mos6526 Interrupt sources.
     */
    public class InterruptSource extends Event
    {

        public enum INTERRUPT
        {
            INTERRUPT_NONE ( 0),
            INTERRUPT_UNDERFLOW_A ( 1 << 0),
            INTERRUPT_UNDERFLOW_B ( 1 << 1),
            INTERRUPT_ALARM ( 1 << 2),
            INTERRUPT_SP ( 1 << 3),
            INTERRUPT_FLAG ( 1 << 4),
            INTERRUPT_REQUEST ( 1 << 7);
            final int v;
            INTERRUPT(int v) { this.v = v; }
        }


         //Pointer to the Mos6526 which this Interrupt belongs to.
        protected Mos6526 parent;

         //Event scheduler.
        protected EventScheduler eventScheduler;

         //Interrupt control register
        private byte icr;

         //Interrupt data register
        private byte idr;

        protected boolean interruptMasked() { return (icr & idr) != 0; }

        protected boolean interruptTriggered() { return (idr & (byte)INTERRUPT.INTERRUPT_REQUEST.v) == 0; }

        protected void triggerInterrupt() { idr |= (byte)INTERRUPT.INTERRUPT_REQUEST.v; }


        /**
         //Create a new InterruptSource.
         *
         //@param scheduler event scheduler
         //@param parent the Mos6526 which this Interrupt belongs to
         */
        protected InterruptSource(EventScheduler scheduler, Mos6526 parent) {
        super("CIA Interrupt");

            this.parent = parent;
            eventScheduler = scheduler;
            icr = 0;
            idr = 0;
        }

        /**
         //Trigger an Interrupt.
         //
         //@param interruptMask Interrupt flag number
         */
        public void trigger(byte interruptMask) { idr |= interruptMask; }

        /**
         //Clear Interrupt state.
         //
         //@return old Interrupt state
         */
        public byte clear()
        {
            byte old = idr;
            idr = 0;
            return old;
        }

        /**
         //Clear pending interrupts, but do not signal to CPU we lost them.
         //It instanceof assumed that all components get reset() calls : synchronous manner.
         */
        public void reset()
        {
            icr = 0;
            idr = 0;
            eventScheduler.cancel(this);
        }

        /**
         //Set Interrupt control mask bits.
         *
         //@param interruptMask control mask bits
         */
        public void set(byte interruptMask)
        {
            if ((interruptMask & 0x80) != 0)
            {
                icr |= (byte)(interruptMask & 0x7f);// ~INTERRUPT.INTERRUPT_REQUEST);
                trigger((byte)INTERRUPT.INTERRUPT_NONE.v);
            }
            else
            {
                icr &= (byte)~interruptMask;
            }
        }

    }
