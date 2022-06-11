/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2013 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder;

import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.sidemu;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;


/**
 * ReSID Builder Class
 */
public class ReSIDBuilder extends SidBuilder {

    private Setting setting;

    public ReSIDBuilder(String name, Setting setting) {
        super(name);

        this.setting = setting;
    }

    /**
     * Available sids.
     *
     * @return the number of available sids, 0 = endless.
     */
    @Override
    public int availDevices() {
        return 0;
    }

    protected void finalize() {   // Remove all SID emulations
        remove();
    }

    /** Create a new Sid emulation. */
    @Override
    public int create(int sids) {
        m_status = true;

        // Check available devices
        int count = availDevices();

        if (count != 0 && (count < sids))
            sids = count;

        for (count = 0; count < sids; count++) {
            try {
                sidobjs.add(new ReSID(this, setting));
            }
            // Memory alloc failed?
            catch (Exception e) {
                m_errorBuffer = name() + " ERROR: Unable to create ReSID Object";
                m_status = false;
                break;
            }
        }
        return count;
    }

    @Override
    public String credits() {
        return ReSID.getCredits();
    }

    @Override
    public void filter(Boolean enable) {
        for (sidemu o : sidobjs) {
            ((ReSID) o).filter(enable); // ようはこういうこと？
        }
    }

    public void bias(double dac_bias) {
        for (sidemu o : sidobjs) {
            ((ReSID) o).bias(dac_bias); // ようはこういうこと？
        }
    }
}

