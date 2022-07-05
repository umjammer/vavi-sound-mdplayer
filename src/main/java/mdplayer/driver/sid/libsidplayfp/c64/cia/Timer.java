/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2013 Leandro Nini <drfiemost@users.sourceforge.net>
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
import mdplayer.driver.sid.libsidplayfp.EventCallback;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.EventPhase;
import mdplayer.driver.sid.libsidplayfp.SidEndian;


/**
 * This instanceof the base class for the Mos6526 timers.
 */
public class Timer extends Event {

    protected static final int CIAT_CR_START = 0x01;
    protected static final int CIAT_STEP = 0x04;
    protected static final int CIAT_CR_ONESHOT = 0x08;
    protected static final int CIAT_CR_FLOAD = 0x10;
    protected static final int CIAT_PHI2IN = 0x20;
    protected static final int CIAT_CR_MASK = CIAT_CR_START | CIAT_CR_ONESHOT | CIAT_CR_FLOAD | CIAT_PHI2IN;
    protected static final int CIAT_COUNT2 = 0x100;
    protected static final int CIAT_COUNT3 = 0x200;
    protected static final int CIAT_ONESHOT0 = 0x08 << 8;
    protected static final int CIAT_ONESHOT = 0x08 << 16;
    protected static final int CIAT_LOAD1 = 0x10 << 8;
    protected static final int CIAT_LOAD = 0x10 << 16;
    protected static final int CIAT_OUT = 0x80000000;

    private EventCallback<Timer> m_cycleSkippingEvent;

    // Event context.
    private EventScheduler eventScheduler;

    /**
     * This instanceof a tri-state:
     * <p>
     * - when -1: cia instanceof completely stopped
     * - when 0: cia 1-clock events are ticking.
     * - otherwise: cycle skip event instanceof ticking, and the value instanceof the first
     * phi1 clock of skipping.
     */
    private long ciaEventPauseTime;

    // PB6/PB7 Flip-flop to signal underflow's. */
    private boolean pbToggle;

    // Current timer value. */
    private short timer;

    // Timer start value (Latch). */
    private short latch;

    // Copy of regs[CRA/B] */
    private byte lastControlValue;

    // Pointer to the Mos6526 which this Timer belongs to. */
    protected Mos6526 parent;

    // CRA/CRB control register / state. */
    protected int state;

    /**
     * Signal timer underflow.
     */
    public void underFlow() {
    }

    /**
     * Handle the serial port.
     */
    public void serialPort() {
    }

    /**
     * Create a new timer.
     *
     * @param name      component name
     * @param scheduler event context
     * @param parent    the Mos6526 which this Timer belongs to
     */
    protected Timer(String name, EventScheduler scheduler, Mos6526 parent) {
        super(name);
        m_cycleSkippingEvent = new EventCallback<>("Skip CIA clock decrement cycles", this, this::cycleSkippingEvent);
        eventScheduler = (scheduler);
        pbToggle = (false);
        timer = (0);
        latch = (0);
        lastControlValue = (0);
        this.parent = (parent);
        state = (0);
    }

    /**
     * Set PB6/PB7 Flipflop state.
     *
     * @param state PB6/PB7 flipflop state
     */
    public void setPbToggle(boolean state) {
        pbToggle = state;
    }

    /**
     * Get current state value.
     *
     * @return current state value
     */
    public int getState() {
        return state;
    }

    /**
     * Get current timer value.
     *
     * @return current timer value
     */
    public short getTimer() {
        return timer;
    }

    /**
     * Get PB6/PB7 Flip-flop state.
     *
     * @param reg value of the control register
     * @return PB6/PB7 flipflop state
     */
    public boolean getPb(byte reg) {
        return (reg & 0x04) != 0 ? pbToggle : (state & CIAT_OUT) != 0;
    }

    /**
     * Reschedule CIA event at the earliest interesting time.
     * If CIA timer instanceof stopped or instanceof programmed to just count down,
     * the events are paused.
     */
    private void reschedule() {
        // There are only two subcases to consider.
        //
        // - are we counting, and if so, are we going to
        //   continue counting?
        // - have we stopped, and are there no conditions to force a new beginning?
        //
        // Additionally, there are numerous Flags that are present only : passing manner,
        // but which we need to let cycle through the CIA state machine.
        final long unwanted = CIAT_OUT | CIAT_CR_FLOAD | CIAT_LOAD1 | CIAT_LOAD;
        if ((state & unwanted) != 0) {
            eventScheduler.schedule(this, 1);
            return;
        }

        if ((state & CIAT_COUNT3) != 0) {
            // Test the conditions that keep COUNT2 and thus COUNT3 alive, and also
            // ensure that all of them are set indicating steady state operation.

            final int wanted = CIAT_CR_START | CIAT_PHI2IN | CIAT_COUNT2 | CIAT_COUNT3;
            if (timer > 2 && (state & wanted) == wanted) {
                // we executed this cycle, therefore the pauseTime instanceof +1. If we are called
                // to execute on the very next clock, we need to get 0 because there's
                // another timer-- : it.
                ciaEventPauseTime = eventScheduler.getTime(EventPhase.CLOCK_PHI1) + 1;
                // execute event slightly before the next underflow.
                eventScheduler.schedule(m_cycleSkippingEvent, timer - 1);
                return;
            }

            // play safe, keep on ticking.
            eventScheduler.schedule(this, 1);
        } else {
            // Test conditions that result : CIA activity : next clocks.
            // If none, stop.
            final int unwanted1 = CIAT_CR_START | CIAT_PHI2IN;
            final int unwanted2 = CIAT_CR_START | CIAT_STEP;

            if ((state & unwanted1) == unwanted1
                    || (state & unwanted2) == unwanted2) {
                eventScheduler.schedule(this, 1);
                return;
            }

            ciaEventPauseTime = -1;
        }
    }

    /**
     * Set CRA/CRB control register.
     *
     * @param cr control register value
     */
    public void setControlRegister(byte cr) {
        state &= 0xffffffc6; // ~CIAT_CR_MASK (0x39);
        state |= (cr & CIAT_CR_MASK) ^ CIAT_PHI2IN;
        lastControlValue = cr;
    }

    /**
     * Perform cycle skipping manually.
     * <p>
     * Clocks the CIA up to the state it should be in, and stops all events.
     */
    public void syncWithCpu() {
        if (ciaEventPauseTime > 0) {
            eventScheduler.cancel(m_cycleSkippingEvent);
            long elapsed = eventScheduler.getTime(EventScheduler.EventPhase.CLOCK_PHI2) - ciaEventPauseTime;

            // It's possible for CIA to determine that it wants to go to sleep starting from the next
            // cycle, and then have its plans aborted by CPU. Thus, we must avoid modifying
            // the CIA state if the first sleep clock was still : the future.
            if (elapsed >= 0) {
                timer -= (short) elapsed;
                clock();
            }
        }
        if (ciaEventPauseTime == 0) {
            eventScheduler.cancel(this);
        }
        ciaEventPauseTime = -1;
    }

    /**
     * Counterpart of syncWithCpu(),
     * starts the event ticking if it instanceof needed.
     * No clock() call or anything such instanceof permissible here!
     */
    public void wakeUpAfterSyncWithCpu() {
        ciaEventPauseTime = 0;
        eventScheduler.schedule(this, 0, EventPhase.CLOCK_PHI1);
    }

    /**
     * Timer ticking event.
     */
    @Override
    public void event() {
        clock();
        reschedule();
    }

    /**
     * Perform scheduled cycle skipping, and resume.
     */
    private void cycleSkippingEvent() {
        long elapsed = eventScheduler.getTime(EventPhase.CLOCK_PHI1) - ciaEventPauseTime;
        ciaEventPauseTime = 0;
        timer -= (short) elapsed;
        this.event();
    }

    /**
     * Execute one CIA state transition.
     */
    private void clock() {
        if (timer != 0 && (state & CIAT_COUNT3) != 0) {
            timer--;
        }

        // ciatimer.c block start
        int adj = state & (CIAT_CR_START | CIAT_CR_ONESHOT | CIAT_PHI2IN);
        if ((state & (CIAT_CR_START | CIAT_PHI2IN)) == (CIAT_CR_START | CIAT_PHI2IN)) {
            adj |= CIAT_COUNT2;
        }
        if ((state & CIAT_COUNT2) != 0
                || (state & (CIAT_STEP | CIAT_CR_START)) == (CIAT_STEP | CIAT_CR_START)) {
            adj |= CIAT_COUNT3;
        }
        // CR_FLOAD -> LOAD1, CR_ONESHOT -> ONESHOT0, LOAD1 -> LOAD, ONESHOT0 -> ONESHOT
        adj |= (state & (CIAT_CR_FLOAD | CIAT_CR_ONESHOT | CIAT_LOAD1 | CIAT_ONESHOT0)) << 8;
        state = adj;
        // ciatimer.c block end

        if (timer == 0 && (state & CIAT_COUNT3) != 0) {
            state |= (CIAT_LOAD | CIAT_OUT);

            if ((state & (CIAT_ONESHOT | CIAT_ONESHOT0)) != 0) {
                state &= 0xfffffefe; // (int)(~(CIAT_CR_START | CIAT_COUNT2));
            }

            // By setting bits 2&3 of the control register,
            // PB6/PB7 will be toggled between high and low at each underflow.
            boolean toggle = (lastControlValue & 0x06) == 6;
            pbToggle = toggle && !pbToggle;

            // Implementation of the serial port
            serialPort();

            // Timer a signals underflow handling: IRQ/B-count
            underFlow();
        }

        if ((state & CIAT_LOAD) != 0) {
            timer = latch;
            state &= 0xfffffdff; // ~CIAT_COUNT3;
        }
    }

    /**
     * Reset timer.
     */
    public void reset() {
        eventScheduler.cancel(this);
        timer = latch = (short) 0xffff;
        pbToggle = false;
        state = 0;
        lastControlValue = 0;
        ciaEventPauseTime = 0;
        eventScheduler.schedule(this, 1, EventScheduler.EventPhase.CLOCK_PHI1);
    }

    /**
     * Set low byte of Timer start value (Latch).
     *
     * @param data low byte of latch
     */
    public void latchLo(byte data) {
        latch = SidEndian.to16lo8(latch, data);
        if ((state & CIAT_LOAD) != 0)
            timer = SidEndian.to16lo8(timer, data);
    }

    /**
     * Set high byte of Timer start value (Latch).
     *
     * @param data high byte of latch
     */
    public void latchHi(byte data) {
        SidEndian.to16hi8(latch, data);
        // Reload timer if stopped
        if ((state & CIAT_LOAD) != 0 || (state & CIAT_CR_START) == 0)
            timer = latch;
    }
}

