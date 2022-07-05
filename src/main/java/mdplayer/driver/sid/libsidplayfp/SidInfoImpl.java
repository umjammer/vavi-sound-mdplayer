/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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
 *  MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
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

    public String name;

    public String version;

    public List<String> credits = new ArrayList<>();

    public String speedString;

    public String kernalDesc;

    public String basicDesc;

    public String chargenDesc;

    public int maxSids;

    public int channels;

    public short driverAddress;

    public short driverLength;

    // prevent copying
    private SidInfoImpl(SidInfoImpl s) {
    }

    private SidInfoImpl opeEquel(SidInfoImpl s) {
        return null;
    }

    public SidInfoImpl() {
        name = Const.PACKAGE_NAME;
        version = Const.PACKAGE_VERSION;
        maxSids = (Mixer.MAX_SIDS);
        channels = 1;
        driverAddress = 0;
        driverLength = 0;

        credits.add(Const.PACKAGE_NAME + " V" + Const.PACKAGE_VERSION + " Engine:\n" + "\tCopyright (C) 2000 Simon White\n" +
                      "\tCopyright (C) 2007-2010 Antti Lankila\n" + "\tCopyright (C) 2010-2015 Leandro Nini\n" + "\t" +
                      Const.PACKAGE_URL + "\n");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int getNumberOfCredits() {
        return credits.size();
    }

    @Override
    public String getCredits(int i) {
        return i < credits.size() ? credits.get(i) : "";
    }

    @Override
    public int getMaxsids() {
        return maxSids;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public short getDriverAddress() {
        return driverAddress;
    }

    @Override
    public short getDriverLength() {
        return driverLength;
    }

    @Override
    public String getSpeedString() {
        return speedString;
    }

    @Override
    public String getKernalDesc() {
        return kernalDesc;
    }

    @Override
    public String getBasicDesc() {
        return basicDesc;
    }

    @Override
    public String getChargenDesc() {
        return chargenDesc;
    }
}
