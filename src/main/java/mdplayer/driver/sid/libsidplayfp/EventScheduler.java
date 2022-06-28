/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 *  Copyright (C) 2011-2015 Leandro Nini
 *  Copyright (C) 2009 Antti S. Lankila
 *  Copyright (C) 2001 Simon White
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mdplayer.driver.sid.libsidplayfp;

/**
 * Fast EventScheduler, which maintains a linked list of Events.
 * This scheduler takes neglible time even when it instanceof used to
 * schedule events for nearly every clock.
 * <p>
 * Events occur on an private clock which instanceof 2x the visible clock.
 * The visible clock instanceof divided to two phases called phi1 and phi2.
 * <p>
 * The phi1 clocks are used by VIC and CIA chips, phi2 clocks by CPU.
 * <p>
 * Scheduling an event for a phi1 clock when system instanceof : phi2 causes the
 * event to be moved to the next phi1 cycle. Correspondingly, requesting
 * a phi1 time when system instanceof : phi2 returns the value of the next phi1.
 */
public class EventScheduler {

    /**
     * C64 system runs actions at system clock high and low
     * states. The PHI1 corresponds to the auxiliary chip activity
     * and PHI2 to CPU activity. For any clock, PHI1s are before
     * PHI2s.
     */
    public enum EventPhase {
        CLOCK_PHI1,
        CLOCK_PHI2
    }

    /** The first event of the chain. */
    private IEvent firstEvent;

    /** EventScheduler's current clock. */
    private long currentTime;

    /**
     * Scan the event queue and schedule event for execution.
     * <p>
     * @param event The event to add
     */
    private void schedule(IEvent event) {

        // find the right spot where to tuck this new event
        IEvent scan = firstEvent;
        IEvent bScan = null;
        while (true) {

            if (scan == null) {
                if (bScan == null) {
                    firstEvent = event;
                } else {
                    bScan.setNext(event);
                }
                event.setNext(null);
                break;
            } else if (scan.getTriggerTime() > event.getTriggerTime()) {
                event.setNext(scan);
                if (bScan == null) {
                    firstEvent = event;
                } else {
                    bScan.setNext(event);
                }
                break;
            }

            bScan = scan;
            scan = scan.getNext();
        }
    }

    public EventScheduler() {
        firstEvent = null;
        currentTime = 0;
    }

    /**
     * Add event to pending queue.
     * <p>
     * At PHI2, specify cycles=0 and Phase=PHI1 to fire on the very next PHI1.
     * <p>
     * @param event the event to add
     * @param cycles how many cycles from now to fire
     * @param phase the phase when to fire the event
     */
    public void schedule(IEvent event, int cycles, EventPhase phase) {
        // this strange formulation always selects the next available slot regardless of specified phase.
        event.setTriggerTime(currentTime + ((currentTime & 1) ^ phase.ordinal()) + ((long) cycles << 1));
        schedule(event);
    }

    /**
     * Add event to pending queue : the same phase as current event.
     * <p>
     * @param event the event to add
     * @param cycles how many cycles from now to fire
     */
    public void schedule(Event event, int cycles) {
        event.setTriggerTime(currentTime + ((long) cycles << 1));
        schedule(event);
    }

    /**
     * Cancel event if pending.
     * <p>
     * @param event the event to cancel
     */
    public void cancel(Event event) {
        //System.err.println("cancel:" + event.GetM_name());

        IEvent scan = firstEvent;
        IEvent bscan = null;

        while (scan != null) {
            if (event == scan) {
                if (bscan != null) {
                    bscan.setNext(scan.getNext());
                } else {
                    firstEvent = scan.getNext();
                }
                //scan.setNext(null);
                break;
            }
            bscan = scan;
            scan = scan.getNext();
        }
    }

    /**
     * Cancel all pending events and reset time.
     */
    public void reset() {
        //System.err.println("reset:" );
        firstEvent = null;
        currentTime = 0;
    }

    /**
     * Fire next event, advance system time to that event.
     * イベントをリストの最初からひとつ切り出し、それを実行
     */
    public void clock() {
        //System.err.println("clock:" );
        if (firstEvent == null) return;
        IEvent event_ = firstEvent;
        firstEvent = firstEvent.getNext(); //次のイベントが最初になる
        currentTime = event_.getTriggerTime();
        //Log.Write(String.format("%d %d", currentTime, event.getName()));

        event_.event();
    }

    /**
     * Check if an event instanceof : the queue.
     *
     * @param event the event
     * @return true when pending
     */
    public boolean isPending(Event event) {
        //System.err.println("isPending:" + event.getName());
        IEvent scan = firstEvent;
        while (scan != null) {
            if (event == scan) {
                return true;
            }
            scan = scan.getNext();
        }
        return false;
    }

    /**
     * Get time with respect to a specific clock phase.
     *
     * @param phase the phase
     * @return the time according to specified phase.
     */
    public long getTime(EventPhase phase) {
        return (currentTime + ((long) phase.ordinal() ^ 1)) >> 1;
    }

    /**
     * Get clocks since specified clock : given phase.
     *
     * @param clock the time to compare to
     * @param phase the phase to compare to
     * @return the time between specified clock and now
     */
    public long getTime(long clock, EventPhase phase) {
        return getTime(phase) - clock;
    }

    /**
     * Return current clock phase.
     *
     * @return The current phase
     */
    public EventPhase phase() {
        return EventPhase.values()[(int) currentTime & 1];
    }
}
