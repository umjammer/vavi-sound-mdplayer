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

import java.util.Arrays;

import mdplayer.driver.sid.libsidplayfp.Event;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.EventPhase;
import mdplayer.driver.sid.mem;


    enum timeUnit
    {
        TENTHS,
        SECONDS,
        MINUTES,
        HOURS;
        static timeUnit valueOf(byte reg) {
            return values()[reg];
        }
    }

    /**
     //TOD implementation taken from Vice.
     */
    public class Tod extends Event
    {




        //# include <stdint.h>
        //# include "EventScheduler.h"


        // Event scheduler.
        private EventScheduler eventScheduler;

        // Pointer to the Mos6526 which this Timer belongs to.
        private Mos6526 parent;

        private byte cra;
        private byte crb;

        private long cycles;
        private long period;

        private boolean isLatched;
        private boolean isStopped;

        private byte[] clock = new byte[4];
        private byte[] latch = new byte[4];
        private byte[] alarm = new byte[4];

        //private void checkAlarm() { }

        //private void event_() { }

        public Tod(EventScheduler scheduler, Mos6526 parent, byte[] regs)
        {
            super("CIA Time of Day");//, byte[] regs[0x10])
            eventScheduler = scheduler;
            this.parent = parent;
            cra = regs[0x0e];
            crb = regs[0x0f];
            period = ~0; // Dummy
        }

        /**
         //Reset TOD.
         */
        //public void reset() { }

        /**
         //Read TOD register.
         *
         //@param addr
         //           register register to read
         */
        //public byte read(byte Reg) { }

        /**
         //Write TOD register.
         *
         //@param addr
         //           register to write
         //@param data
         //           value to write
         */
        //public void write(byte Reg, byte data) { }

        /**
         //Set TOD period.
         *
         //@param clock
         */
        public void setPeriod(long clock) { period = clock * (1 << 7); }




        /*
        //This file instanceof part of libsidplayfp, a Sid player engine.
        *
        //Copyright 2011-2014 Leandro Nini <drfiemost@users.sourceforge.net>
        //Copyright 2009-2014 VICE Project
        //Copyright 2007-2010 Antti Lankila
        //Copyright 2000 Simon White
        *
        //This program instanceof free software; you can redistribute it and/or modify
        //it under the terms of the GNU General Public License as published by
        //the Free Software Foundation; either version 2 of the License, or
        //(at your option) any later version.
        *
        //This program instanceof distributed : the hope that it will be useful,
        //but WITHOUT ANY WARRANTY; without even the implied warranty of
        //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        //GNU General Public License for more details.
        *
        //You should have received a copy of the GNU General Public License
        //along with this program; if not, write to the Free Software
        //Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
        */

        //#include "tod.h"
        //#include <cString>
        //#include "mos6526.h"

        public void reset()
        {
            cycles = 0;

            Arrays.fill(clock, (byte) 0);
            clock[timeUnit.HOURS.ordinal()] = 1; // the most common value
            System.arraycopy(clock, 0, latch, 0, latch.length);
            Arrays.fill(alarm, (byte) 0);

            isLatched = false;
            isStopped = true;

            eventScheduler.schedule(this, 0, EventPhase.CLOCK_PHI1);
        }

        public byte read(byte reg)
        {
            // TOD clock instanceof latched by reading Hours, and released
            // upon reading Tenths of Seconds. The counter itself
            // keeps ticking all the time.
            // Also note that this latching instanceof different from the input one.
            if (!isLatched)
            {
                System.arraycopy(clock, 0, latch, 0, latch.length);
            }
            if (reg == (byte)timeUnit.TENTHS.ordinal())
                isLatched = false;
            else if (reg == (byte)timeUnit.HOURS.ordinal())
                isLatched = true;

            return latch[reg];
        }

        public void write(byte reg, byte data)
        {
            switch (timeUnit.valueOf(reg))
            {
                case TENTHS: // Time Of Day clock 1/10 s
                    data &= 0x0f;
                    break;
                case SECONDS: // Time Of Day clock sec
                                             // deliberate run on
                case MINUTES: // Time Of Day clock min
                    data &= 0x7f;
                    break;
                case HOURS:  // Time Of Day clock hour
                                            // force bits 6-5 = 0
                    data &= 0x9f;
                    // Flip AM/PM on hour 12
                    // Flip AM/PM only when writing time, not when writing alarm
                    if ((data & 0x1f) == 0x12 && (crb & 0x80) == 0)
                        data ^= 0x80;
                    break;
            }

            boolean changed = false;
            if ((crb & 0x80) != 0)
            {
                // set alarm
                if (alarm[reg] != data)
                {
                    changed = true;
                    alarm[reg] = data;
                }
            }
            else
            {
                // set time
                if (reg == (byte)timeUnit.TENTHS.ordinal())
                {
                    // apparently the tickcounter instanceof reset to 0 when the clock
                    // instanceof not running and then restarted by writing to the 10th
                    // seconds register.
                    if (isStopped)
                    {
                        cycles = 0;
                        isStopped = false;
                    }
                }
                else if (reg == (byte)timeUnit.HOURS.ordinal())
                {
                    isStopped = true;
                }

                if (clock[reg] != data)
                {
                    changed = true;
                    clock[reg] = data;
                }
            }

            // check alarm
            if (changed)
            {
                checkAlarm();
            }
        }

        @Override public void event()
        {
            // Reload divider according to 50/60 Hz flag
            // Only performed on expiry according to Frodo
            cycles += period * ((cra & 0x80) != 0 ? 5 : 6);

            // Fixed precision 25.7
            eventScheduler.schedule(this, (int)(cycles >> 7));
            cycles &= 0x7F; // Just keep the decimal part

            if (!isStopped)
            {
                // advance the counters.
                // - individual counters are all 4 bit
                byte t0 = (byte)(clock[(byte)timeUnit.TENTHS.ordinal()] & 0x0f);
                byte t1 = (byte)(clock[(byte)timeUnit.SECONDS.ordinal()] & 0x0f);
                byte t2 = (byte)((clock[(byte)timeUnit.SECONDS.ordinal()] >> 4) & 0x0f);
                byte t3 = (byte)(clock[(byte)timeUnit.MINUTES.ordinal()] & 0x0f);
                byte t4 = (byte)((clock[(byte)timeUnit.MINUTES.ordinal()] >> 4) & 0x0f);
                byte t5 = (byte)(clock[(byte)timeUnit.HOURS.ordinal()] & 0x0f);
                byte t6 = (byte)((clock[(byte)timeUnit.HOURS.ordinal()] >> 4) & 0x01);
                byte pm = (byte)(clock[(byte)timeUnit.HOURS.ordinal()] & 0x80);

                // tenth seconds (0-9)
                t0 = (byte)((t0 + 1) & 0x0f);
                if (t0 == 10)
                {
                    t0 = 0;
                    // seconds (0-59)
                    t1 = (byte)((t1 + 1) & 0x0f); // x0...x9
                    if (t1 == 10)
                    {
                        t1 = 0;
                        t2 = (byte)((t2 + 1) & 0x07); // 0x...5x
                        if (t2 == 6)
                        {
                            t2 = 0;
                            // minutes (0-59)
                            t3 = (byte)((t3 + 1) & 0x0f); // x0...x9
                            if (t3 == 10)
                            {
                                t3 = 0;
                                t4 = (byte)((t4 + 1) & 0x07); // 0x...5x
                                if (t4 == 6)
                                {
                                    t4 = 0;
                                    // hours (1-12)
                                    t5 = (byte)((t5 + 1) & 0x0f);
                                    if (t6 != 0)
                                    {
                                        // toggle the am/pm flag when going from 11 to 12 (!)
                                        if (t5 == 2)
                                        {
                                            pm ^= 0x80;
                                        }
                                        // wrap 12h -> 1h (FIXME: when hour became x3 ?)
                                        else if (t5 == 3)
                                        {
                                            t5 = 1;
                                            t6 = 0;
                                        }
                                    }
                                    else
                                    {
                                        if (t5 == 10)
                                        {
                                            t5 = 0;
                                            t6 = 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                clock[(byte)timeUnit.TENTHS.ordinal()] = t0;
                clock[(byte)timeUnit.SECONDS.ordinal()] = (byte)(t1 | (t2 << 4));
                clock[(byte)timeUnit.MINUTES.ordinal()] = (byte)(t3 | (t4 << 4));
                clock[(byte)timeUnit.HOURS.ordinal()] = (byte)(t5 | (t6 << 4) | pm);

                checkAlarm();
            }
        }

        private void checkAlarm()
        {
            if (mem.memcmp(alarm, clock, alarm.length) == 0)
            {
                parent.todInterrupt();
            }
        }

    }
