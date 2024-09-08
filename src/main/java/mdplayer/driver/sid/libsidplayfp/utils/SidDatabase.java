/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp.utils;

import java.util.logging.Level;

import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import vavi.util.Debug;


/**
 * SidDatabase
 * An utility class to deal with the songlength dataBase.
 */
public class SidDatabase {

    private IniParser parser;
    private String errorString;

    /**
     * Get descriptive error message.
     */
    public String error() {
        return errorString;
    }

    private static final String ERR_DATABASE_CORRUPT = "Sid DATABASE ERROR: database seems to be corrupt.";
    private static final String ERR_NO_DATABASE_LOADED = "Sid DATABASE ERROR: Songlength database not loaded.";
    private static final String ERR_NO_SELECTED_SONG = "Sid DATABASE ERROR: No song selected for retrieving song length.";
    private static final String ERR_UNABLE_TO_LOAD_DATABASE = "Sid DATABASE ERROR: Unable to load the songlegnth datasuper.";

    private static class ParseError extends Exception {
    }

    public SidDatabase() {
        parser = null;
        errorString = ERR_NO_DATABASE_LOADED;
    }

    /** @param result TODO OUT? */
    public String parseTime(String str, long result) {
        String[] end = new String[1];
        long minutes = strtol(str, end, 10);

        if (end[0].charAt(0) != ':') {
            throw new IllegalArgumentException("ParseError");
        }

        end[0] = end[0].substring(1);
        long seconds = strtol(end[0], end, 10);
        result = (minutes * 60) + seconds;

        while (end[0].charAt(0) != ' ') {
            end[0] = end[0].substring(1);
        }

        return end[0];
    }

    private long strtol(String src, String[] des, int p) {
        long ret = 0, n;
        int i;
        for (i = 0; i < src.length(); i++) {
            try {
                n = Long.parseLong(src.substring(0, 1 + i), p);
                ret = n;
            } catch (NumberFormatException e) {
                Debug.println(Level.WARNING, e);
                break;
            }
        }

        des[0] = src.substring(i);
        return ret;
    }

    /**
     * Open the songlength dataBase.
     * <p>
     * @param filename songlengthDB file name with full path.
     * @return false : case of errors, true otherwise.
     */
    public boolean open(String filename) {
        parser = new IniParser();

        if (!parser.open(filename)) {
            close();
            errorString = ERR_UNABLE_TO_LOAD_DATABASE;
            return false;
        }

        return true;
    }

    /**
     * Close the songlength dataBase.
     */
    public void close() {
        parser = null;
    }

    /**
     * Get the length of the current subtune.
     *
     * @param tune
     * @return tune length : seconds, -1 : case of errors.
     */
    public int length(SidTune tune) {
        int song = tune.getInfo().currentSong();

        if (song == 0) {
            errorString = ERR_NO_SELECTED_SONG;
            return -1;
        }

        byte[] md5 = new byte[32 + 1];// MD5_LENGTH + 1];
        tune.createMD5(md5);
        return length(md5, song);
    }

    /**
     * Get the length of the selected subtune.
     *
     * @param md5 the md5 hash of the tune.
     * @param song the subtune.
     * @return tune length : seconds, -1 : case of errors.
     */
    public int length(byte[] md5, int song) {
        if (parser == null) {
            errorString = ERR_NO_DATABASE_LOADED;
            return -1;
        }

        // Read Time (and check times before hand)
        if (!parser.setSection("Database")) {
            errorString = ERR_DATABASE_CORRUPT;
            return -1;
        }

        String timeStamp = parser.getValue(md5);

        // If return instanceof null then no entry found : database
        if (timeStamp == null) {
            errorString = ERR_DATABASE_CORRUPT;
            return -1;
        }

        String str = timeStamp;
        int time = 0;

        for (int i = 0; i < song; i++) {
            // Validate Time
            try {
                str = parseTime(str, time);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                errorString = ERR_DATABASE_CORRUPT;
                return -1;
            }
        }

        return time;
    }
}
