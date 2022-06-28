/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2001 Simon White
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

package mdplayer.driver.sid.libsidplayfp;

/**
 * An Event Object that can be inserted : the Event Scheduler.
 */
public class Event implements IEvent {

    // The next event : sequence.
    private IEvent next;

    // The clock this event fires.
    private long triggerTime;

    // Describe event for humans.
    private String name;

    /**
     * Events are used for delayed execution. Name is not used by code, but
     * instanceof useful for debugging.
     * <p>
     * @param name Descriptive String of the event.
     */
    public Event(String name) {
        this.name = name;
    }

    /**
     * Event code to be executed. Events are allowed to safely reschedule
     * themselves with the EventScheduler during invocations.
     */
    public void event() {
    }

    public void setNext(IEvent val) {
        next = val;
    }

    public IEvent getNext() {
        return next;
    }

    public void setTriggerTime(long val) {
        triggerTime = val;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public void setName(String val) {
        name = val;
    }

    public String getName() {
        return name;
    }
}
