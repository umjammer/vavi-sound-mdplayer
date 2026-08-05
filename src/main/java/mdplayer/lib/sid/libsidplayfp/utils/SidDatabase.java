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

package mdplayer.lib.sid.libsidplayfp.utils;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidTune;

import static java.lang.System.getLogger;


/**
 * SidDatabase
 * An utility class to deal with the songlength dataBase.
 */
public class SidDatabase {

    private static final Logger logger = getLogger(SidDatabase.class.getName());

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
    private static final String ERR_NO_ENTRY = "Sid DATABASE ERROR: No song length entry for this tune.";
    private static final String ERR_UNABLE_TO_LOAD_DATABASE = "Sid DATABASE ERROR: Unable to load the songlegnth datasuper.";

    public SidDatabase() {
        parser = null;
        errorString = ERR_NO_DATABASE_LOADED;
    }

    /**
     * Reads one of the times a database entry is a list of.
     * <p>
     * A time is {@code m:ss} or, since the entries were given millisecond precision,
     * {@code m:ss.mmm}; the fraction is optional and may be one to three digits.
     *
     * @param str the time, on its own
     * @return the time : milliseconds
     * @throws IllegalArgumentException if it is not a time
     */
    public static int parseTime(String str) {
        int colon = str.indexOf(':');
        if (colon == -1) {
            throw new IllegalArgumentException("ParseError: " + str);
        }

        try {
            int minutes = Integer.parseInt(str.substring(0, colon));
            String rest = str.substring(colon + 1);

            int dot = rest.indexOf('.');
            int seconds = Integer.parseInt(dot == -1 ? rest : rest.substring(0, dot));
            // ".5" is half a second, not five milliseconds
            double fraction = dot == -1 ? 0 : Double.parseDouble("0" + rest.substring(dot));

            return (int) (((minutes * 60L) + seconds) * 1000 + Math.round(fraction * 1000));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ParseError: " + str, e);
        }
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
        int ms = lengthMs(tune);
        return ms < 0 ? -1 : ms / 1000;
    }

    /**
     * Get the length of the current subtune.
     * <p>
     * Both fingerprints are tried, so that either the {@code Songlengths.md5} the collection
     * ships now or an older {@code Songlengths.txt} kept alongside it can answer.
     *
     * @param tune
     * @return tune length : milliseconds, -1 : case of errors.
     */
    public int lengthMs(SidTune tune) {
        int song = tune.getInfo().currentSong();

        if (song == 0) {
            errorString = ERR_NO_SELECTED_SONG;
            return -1;
        }

        byte[] md5 = new byte[SidTune.MD5_LENGTH + 1];
        int length = tune.createMD5New(md5) != null ? lengthMs(md5, song) : -1;
        if (length < 0 && tune.createMD5(md5) != null) {
            length = lengthMs(md5, song);
        }
        return length;
    }

    /**
     * Get the length of the selected subtune.
     *
     * @param md5 the md5 hash of the tune.
     * @param song the subtune.
     * @return tune length : seconds, -1 : case of errors.
     */
    public int length(byte[] md5, int song) {
        int ms = lengthMs(md5, song);
        return ms < 0 ? -1 : ms / 1000;
    }

    /**
     * Get the length of the selected subtune.
     *
     * @param md5 the md5 hash of the tune.
     * @param song the subtune.
     * @return tune length : milliseconds, -1 : case of errors.
     */
    public int lengthMs(byte[] md5, int song) {
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
            errorString = ERR_NO_ENTRY;
            return -1;
        }

        // an entry is one time per subtune, "1:53.500 2:04.000 ..."
        String[] times = timeStamp.split("\\s+");
        if (song > times.length) {
            errorString = ERR_DATABASE_CORRUPT;
            return -1;
        }

        try {
            return parseTime(times[song - 1]);
        } catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            errorString = ERR_DATABASE_CORRUPT;
            return -1;
        }
    }
}
