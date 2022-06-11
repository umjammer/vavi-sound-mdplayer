/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 *  Copyright 2011-2015 Leandro Nini
 *  Copyright 2007-2010 Antti Lankila
 *  Copyright 2000 Simon White
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

import java.util.ArrayList;
import java.util.List;

import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidInfo;


/**
 * The implementation of the SidInfo interface.
 */
public class SidInfoImpl extends SidInfo {

    public String m_name;

    public String m_version;

    public List<String> m_credits = new ArrayList<>();

    public String m_speedString;

    public String m_kernalDesc;

    public String m_basicDesc;

    public String m_chargenDesc;

    public int m_maxsids;

    public int m_channels;

    public short m_driverAddr;

    public short m_driverLength;

    // prevent copying
    private SidInfoImpl(SidInfoImpl s) {
    }

    private SidInfoImpl opeEquel(SidInfoImpl s) {
        return null;
    }

    public SidInfoImpl() {
        m_name = Const.PACKAGE_NAME;
        m_version = Const.PACKAGE_VERSION;
        m_maxsids = (Mixer.MAX_SIDS);
        m_channels = (1);
        m_driverAddr = (0);
        m_driverLength = (0);

        m_credits.add(Const.PACKAGE_NAME + " V" + Const.PACKAGE_VERSION + " Engine:\n" + "\tCopyright (C) 2000 Simon White\n" +
                      "\tCopyright (C) 2007-2010 Antti Lankila\n" + "\tCopyright (C) 2010-2015 Leandro Nini\n" + "\t" +
                      Const.PACKAGE_URL + "\n");
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String getVersion() {
        return m_version;
    }

    @Override
    public int getNumberOfCredits() {
        return (int) m_credits.size();
    }

    @Override
    public String getCredits(int i) {
        return i < m_credits.size() ? m_credits.get((int) i) : "";
    }

    @Override
    public int getMaxsids() {
        return m_maxsids;
    }

    @Override
    public int getChannels() {
        return m_channels;
    }

    @Override
    public short getDriverAddr() {
        return m_driverAddr;
    }

    @Override
    public short getDriverLength() {
        return m_driverLength;
    }

    @Override
    public String getSpeedString() {
        return m_speedString;
    }

    @Override
    public String getKernalDesc() {
        return m_kernalDesc;
    }

    @Override
    public String getBasicDesc() {
        return m_basicDesc;
    }

    @Override
    public String getChargenDesc() {
        return m_chargenDesc;
    }

}
