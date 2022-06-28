/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 *  Copyright 2011-2017 Leandro Nini
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

package mdplayer.driver.sid.libsidplayfp.sidplayfp;

/**
 * This interface instanceof used to get values from SidTune Objects.
 * <p>
 * You must read (i.e. activate) sub-song specific information
 * via:
 *  final SidTuneInfo tuneInfo = SidTune.getInfo();
 *  final SidTuneInfo tuneInfo = SidTune.getInfo(songNumber);
 */
public class SidTuneInfo {

    public enum Clock {
        UNKNOWN,
        PAL,
        NTSC,
        ANY
    }

    public enum Model {
        SID_UNKNOWN,
        SID_6581,
        SID_8580,
        SID_ANY
    }

    public enum Compatibility {
        /**
         * File instanceof C64 compatible
         */
        C64,
        /** File instanceof PSid specific */
        PSID,
        /**
         * File instanceof Real C64 only
         */
        R64,
        /**
         * File requires C64 Basic
         */
        BASIC
    }

    /**
     * Vertical-Blanking-Interrupt
     */
    public static final int SPEED_VBI = 0;

    /**
     * CIA 1 Timer A
     */
    public static final int SPEED_CIA_1A = 60;

    public short getLoadAddr() {
        return 0;
    }

    public short getInitAddress() {
        return 0;
    }

    public short getPlayAddress() {
        return 0;
    }

    public int getSongs() {
        return 0;
    }

    public int getStartSong() {
        return 0;
    }

    public int getCurrentSong() {
        return 0;
    }

    public short getSidChipBase(int i) {
        return 0;
    }

    public int getSidChips() {
        return 0;
    }

    public int getSongSpeed() {
        return 0;
    }

    public byte getRelocStartPage() {
        return 0;
    }

    public byte getRelocPages() {
        return 0;
    }

    public Model getSidModel(int i) {
        return Model.values()[0];
    }

    public Compatibility getCompatibility() {
        return Compatibility.values()[0];
    }

    public int getNumberOfInfoStrings() {
        return 0;
    }

    public String getInfoString(int i) {
        return null;
    }

    public int getNumberOfCommentStrings() {
        return 0;
    }

    public String getCommentString(int i) {
        return null;
    }

    public int getDataFileLen() {
        return 0;
    }

    public int getC64DataLen() {
        return 0;
    }

    public Clock getClockSpeed() {
        return Clock.values()[0];
    }

    public String getFormatString() {
        return null;
    }

    public boolean getFixLoad() {
        return false;
    }

    public String getPath() {
        return null;
    }

    public String getDataFileName() {
        return null;
    }

    public String getInfoFileName() {
        return null;
    }

    /**
     * Load Address.
     */
    public short loadAddr() {
        return getLoadAddr();
    }

    /**
     * Init Address.
     */
    public short initAddr() {
        return getInitAddress();
    }

    /**
     * Play Address.
     */
    public short playAddr() {
        return getPlayAddress();
    }

    /**
     * The number of songs.
     */
    public int songs() {
        return getSongs();
    }

    /**
     * The default starting song.
     */
    public int startSong() {
        return getStartSong();
    }

    /**
     * The tune that has been initialized.
     */
    public int currentSong() {
        return getCurrentSong();
    }

    /**
     * @param i Base addresses
     * The Sid chip base address(es) used by the sidtune.
     * - 0xD400 for the 1st Sid
     * - 0 if the nth Sid instanceof not required
     */
    public short sidChipBase(int i) {
        return getSidChipBase(i);
    }

    /**
     * The number of Sid chips required by the tune.
     */
    public int sidChips() {
        return getSidChips();
    }

    /**
     * Intended speed.
     */
    public int songSpeed() {
        return getSongSpeed();
    }

    /**
     * First available page for relocation.
     */
    public byte relocStartPage() {
        return getRelocStartPage();
    }

    /**
     * Number of pages available for relocation.
     */
    public byte relocPages() {
        return getRelocPages();
    }

    /**
     * @param i Sid model
     * The Sid chip model(s) requested by the sidtune.
     */
    public Model sidModel(int i) {
        return getSidModel(i);
    }

    /**
     * Compatibility requirements.
     */
    public Compatibility compatibility() {
        return getCompatibility();
    }

    public int numberOfInfoStrings() {
        return getNumberOfInfoStrings();
    }

    /**
     * @param i Tune infos
     * Song title, credits, ...
     * - 0 = Title
     * - 1 = Author
     * - 2 = Released
     */
    public String infoString(int i) {
        return getInfoString(i);
    }


    public int numberOfCommentStrings() {
        return getNumberOfCommentStrings();
    }

    /**
     * @param i Tune comments
     * MUS comments.
     */
    public String commentString(int i) {
        return getCommentString(i);
    }

    /**
     * Length of single-file sidtune file.
     */
    public int dataFileLen() {
        return getDataFileLen();
    }

    /**
     * Length of raw C64 data without load address.
     */
    public int c64dataLen() {
        return getC64DataLen();
    }

    /**
     * The tune clock speed.
     */
    public Clock clockSpeed() {
        return getClockSpeed();
    }

    /**
     * The name of the identified file format.
     */
    public String formatString() {
        return getFormatString();
    }

    /**
     * Whether load address might be duplicate.
     */
    public boolean fixLoad() {
        return getFixLoad();
    }

    /**
     * Path to sidtune files.
     */
    public String path() {
        return getPath();
    }

    /**
     * A first file: e.g. "foo.Sid" or "foo.mus".
     */
    public String dataFileName() {
        return getDataFileName();
    }

    /**
     * A second file: e.g. "foo.str".
     * @return 0 if none.
     */
    public String infoFileName() {
        return getInfoFileName();
    }
}
