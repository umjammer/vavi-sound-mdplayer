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

package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.util.ArrayList;
import java.util.List;

import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;


/**
 * The implementation of the SidTuneInfo interface.
 */
public class SidTuneInfoImpl extends SidTuneInfo {

    // # include <stdint.h>
    // # include <vector>
    // # include <String>
    // # include "SidPlayFp/SidTuneInfo.h"
    // # include "sidcxx11.h"

    public String m_formatString;

    public int m_songs;
    public int m_startSong;
    public int m_currentSong;
    public int m_songSpeed;
    public clock_t m_clockSpeed;
    public compatibility_t m_compatibility;
    public int m_dataFileLen;
    public int m_c64dataLen;
    public short m_loadAddr;
    public short m_initAddr;
    public short m_playAddr;
    public byte m_relocStartPage;
    public byte m_relocPages;
    public String m_path;
    public String m_dataFileName;
    public String m_infoFileName;
    public List<model_t> m_sidModels;// vector
    public List<Short> m_sidChipAddresses;// vector
    public List<String> m_infoString = new ArrayList<String>();// vector
    public List<String> m_commentString;// vector
    public Boolean m_fixLoad;

    // prevent copying
    private SidTuneInfoImpl(SidTuneInfoImpl s) {
    }

    private SidTuneInfoImpl opeEquel(SidTuneInfoImpl s) {
        return null;
    }

    public SidTuneInfoImpl() {
        m_formatString = "N/A";
        m_songs = 0;
        m_startSong = 0;
        m_currentSong = 0;
        m_songSpeed = SPEED_VBI;
        m_clockSpeed = clock_t.CLOCK_UNKNOWN;
        m_compatibility = compatibility_t.COMPATIBILITY_C64;
        m_dataFileLen = 0;
        m_c64dataLen = 0;
        m_loadAddr = 0;
        m_initAddr = 0;
        m_playAddr = 0;
        m_relocStartPage = 0;
        m_relocPages = 0;
        m_fixLoad = false;

        m_sidModels = new ArrayList<model_t>();
        m_sidModels.add(model_t.SIDMODEL_UNKNOWN);
        m_sidChipAddresses = new ArrayList<Short>();
        m_sidChipAddresses.add((short) 0xd400);
    }

    @Override
    public short getLoadAddr() {
        return m_loadAddr;
    }

    @Override
    public short getInitAddr() {
        return m_initAddr;
    }

    @Override
    public short getPlayAddr() {
        return m_playAddr;
    }

    @Override
    public int getSongs() {
        return m_songs;
    }

    @Override
    public int getStartSong() {
        return m_startSong;
    }

    @Override
    public int getCurrentSong() {
        return m_currentSong;
    }

    @Override
    public short getSidChipBase(int i) {
        return (short) (i < m_sidChipAddresses.size() ? m_sidChipAddresses.get((int) i) : 0);
    }

    @Override
    public int getSidChips() {
        return m_sidChipAddresses.size();
    }

    @Override
    public int getSongSpeed() {
        return m_songSpeed;
    }

    @Override
    public byte getRelocStartPage() {
        return m_relocStartPage;
    }

    @Override
    public byte getRelocPages() {
        return m_relocPages;
    }

    @Override
    public model_t getSidModel(int i) {
        return i < m_sidModels.size() ? m_sidModels.get((int) i) : model_t.SIDMODEL_UNKNOWN;
    }

    @Override
    public compatibility_t getCompatibility() {
        return m_compatibility;
    }

    @Override
    public int getNumberOfInfoStrings() {
        return (int) m_infoString.size();
    }

    @Override
    public String getInfoString(int i) {
        return i < getNumberOfInfoStrings() ? m_infoString.get((int) i) : "";
    }

    @Override
    public int getNumberOfCommentStrings() {
        return (int) m_commentString.size();
    }

    @Override
    public String getCommentString(int i) {
        return i < getNumberOfCommentStrings() ? m_commentString.get((int) i) : "";
    }

    @Override
    public int getDataFileLen() {
        return m_dataFileLen;
    }

    @Override
    public int getC64dataLen() {
        return m_c64dataLen;
    }

    @Override
    public clock_t getClockSpeed() {
        return m_clockSpeed;
    }

    @Override
    public String getFormatString() {
        return m_formatString;
    }

    @Override
    public Boolean getFixLoad() {
        return m_fixLoad;
    }

    @Override
    public String getPath() {
        return m_path;
    }

    @Override
    public String getDataFileName() {
        return m_dataFileName;
    }

    @Override
    public String getInfoFileName() {
        return m_infoFileName != "" ? m_infoFileName : null;
    }
}
