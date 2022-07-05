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

package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.util.ArrayList;
import java.util.List;

import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;


/**
 * The implementation of the SidTuneInfo interface.
 */
public class SidTuneInfoImpl extends SidTuneInfo {

    public String formatString;

    public int songs;
    public int startSong;
    public int currentSong;
    public int songSpeed;
    public Clock clockSpeed;
    public Compatibility compatibility;
    public int dataFileLen;
    public int c64DataLen;
    public short loadAddress;
    public short initAddress;
    public short playAddress;
    public byte relocatedStartPage;
    public byte relocatedPages;
    public String path;
    public String dataFileName;
    public String infoFileName;
    public List<Model> sidModels;
    public List<Short> sidChipAddresses;
    public List<String> infoString = new ArrayList<>();
    public List<String> commentString;
    public boolean fixLoad;

    // prevent copying
    private SidTuneInfoImpl(SidTuneInfoImpl s) {
    }

    private SidTuneInfoImpl opeEquel(SidTuneInfoImpl s) {
        return null;
    }

    public SidTuneInfoImpl() {
        formatString = "N/a";
        songs = 0;
        startSong = 0;
        currentSong = 0;
        songSpeed = SPEED_VBI;
        clockSpeed = Clock.UNKNOWN;
        compatibility = Compatibility.C64;
        dataFileLen = 0;
        c64DataLen = 0;
        loadAddress = 0;
        initAddress = 0;
        playAddress = 0;
        relocatedStartPage = 0;
        relocatedPages = 0;
        fixLoad = false;

        sidModels = new ArrayList<>();
        sidModels.add(Model.SID_UNKNOWN);
        sidChipAddresses = new ArrayList<>();
        sidChipAddresses.add((short) 0xd400);
    }

    @Override
    public short getLoadAddr() {
        return loadAddress;
    }

    @Override
    public short getInitAddress() {
        return initAddress;
    }

    @Override
    public short getPlayAddress() {
        return playAddress;
    }

    @Override
    public int getSongs() {
        return songs;
    }

    @Override
    public int getStartSong() {
        return startSong;
    }

    @Override
    public int getCurrentSong() {
        return currentSong;
    }

    @Override
    public short getSidChipBase(int i) {
        return (short) (i < sidChipAddresses.size() ? sidChipAddresses.get(i) : 0);
    }

    @Override
    public int getSidChips() {
        return sidChipAddresses.size();
    }

    @Override
    public int getSongSpeed() {
        return songSpeed;
    }

    @Override
    public byte getRelocStartPage() {
        return relocatedStartPage;
    }

    @Override
    public byte getRelocPages() {
        return relocatedPages;
    }

    @Override
    public Model getSidModel(int i) {
        return i < sidModels.size() ? sidModels.get(i) : Model.SID_UNKNOWN;
    }

    @Override
    public Compatibility getCompatibility() {
        return compatibility;
    }

    @Override
    public int getNumberOfInfoStrings() {
        return infoString.size();
    }

    @Override
    public String getInfoString(int i) {
        return i < getNumberOfInfoStrings() ? infoString.get(i) : "";
    }

    @Override
    public int getNumberOfCommentStrings() {
        return commentString.size();
    }

    @Override
    public String getCommentString(int i) {
        return i < getNumberOfCommentStrings() ? commentString.get(i) : "";
    }

    @Override
    public int getDataFileLen() {
        return dataFileLen;
    }

    @Override
    public int getC64DataLen() {
        return c64DataLen;
    }

    @Override
    public Clock getClockSpeed() {
        return clockSpeed;
    }

    @Override
    public String getFormatString() {
        return formatString;
    }

    @Override
    public boolean getFixLoad() {
        return fixLoad;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDataFileName() {
        return dataFileName;
    }

    @Override
    public String getInfoFileName() {
        return !infoFileName.isEmpty() ? infoFileName : null;
    }
}
