/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2013 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000-2001 Simon White
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

package mdplayer.driver.sid.libsidplayfp.sidplayfp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.SidEmu;


/**
 * Base class for Sid builders.
 */
public class SidBuilder {

    private String name;

    protected String errorBuffer;

    protected List<SidEmu> sidobjs = new ArrayList<>();

    protected boolean status;

    protected static class applyParameter_LibsidplayfpReSID_bool {
        protected boolean m_param;

        public interface dlgM_method extends Consumer<Boolean> {
        }

        public dlgM_method m_method;

        public applyParameter_LibsidplayfpReSID_bool(dlgM_method method, boolean param) {
            m_param = param;
            m_method = method;
        }

        public void opeKakko(SidEmu e) {
            m_method.accept(m_param);
        }
    }

    protected static class applyParameter_LibsidplayfpReSID_double {
        protected double m_param;

        public interface dlgM_method extends Consumer<Double> {
        }

        public dlgM_method m_method;

        public applyParameter_LibsidplayfpReSID_double(dlgM_method method, double param) {
            m_param = param;
            m_method = method;
        }

        public void opeKakko(SidEmu e) {
            m_method.accept(m_param);
        }
    }

    public SidBuilder(String name) {
        this.name = name;
        errorBuffer = "N/a";
        status = (true);
    }

    /**
     * The number of used devices.
     *
     * @return number of used sids, 0 if none.
     */
    public int usedDevices() {
        return sidobjs.size();
    }

    /**
     * Available devices.
     *
     * @return the number of available sids, 0 = endless.
     */
    public int availDevices() {
        return 0;
    }

    /**
     * Create the Sid emu.
     *
     * @param sids the number of required Sid emu
     * @return the number of actually created Sid emus
     */
    public int create(int sids) {
        return 0;
    }

    /**
     * Get the builder's name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Error message.
     *
     * @return String error message.
     */
    public String error() {
        return errorBuffer;
    }

    /**
     * Determine current state of Object.
     *
     * @return true = okay, false = error
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Get the builder's credits.
     *
     * @return credits
     */
    public String credits() {
        return null;
    }

    /**
     * Toggle Sid filter emulation.
     *
     * @param enable true = enable, false = disable
     */
    public void filter(boolean enable) {
    }

    /**
     * Find a free Sid of the required specs
     *
     * @param scheduler the event context
     * @param model the required Sid model
     * @return pointer to the locked Sid emu
     */
    public SidEmu lock(EventScheduler scheduler, SidConfig.SidModel model) {
        status = true;

        for (SidEmu it : sidobjs) {
            SidEmu sid = it;
            if (sid.lock(scheduler)) {
                sid.model(model);
                return sid;
            }
        }

        // Unable to locate free Sid
        status = false;
        errorBuffer = name() + " ERROR: No available SIDs to lock";
        return null;
    }

    /**
     * Release this Sid.
     *
     * @param device the Sid emu to unlock
     */
    public void unlock(SidEmu device) {
        SidEmu oldSe = null;
        for (SidEmu se : sidobjs) {
            if (oldSe != se) {
                se.unlock();
            }
            oldSe = se;
        }
    }

    public <T> void Delete(T s) {
        s = null;
    }

    /**
     * Remove all Sid emulations.
     */
    public void remove() {
        sidobjs.clear();
    }
}
