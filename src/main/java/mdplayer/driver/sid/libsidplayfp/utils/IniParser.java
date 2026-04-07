/*
 *  Copyright (C) 2010-2015 Leandro Nini
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

package mdplayer.driver.sid.libsidplayfp.utils;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.StreamReader;

import static java.lang.System.getLogger;


public class IniParser {

    private static final Logger logger = getLogger(IniParser.class.getName());

    private final List<Tuple<String, List<Tuple<String, String>>>> sections = new ArrayList<>();
    private Tuple<String, List<Tuple<String, String>>> curSection;

    public static class ParseError extends RuntimeException {
    }

    private static String parseSection(String buffer) {
        int pos = buffer.indexOf(']');

        if (pos == -1) {
            throw new ParseError();
        }

        return buffer.substring(1, pos);
    }

    private static Tuple<String, String> parseKey(String buffer) {
        int pos = buffer.indexOf('=');

        if (pos == -1) {
            throw new ParseError();
        }

        String key = buffer.substring(0, pos).trim();
        String value = buffer.substring(pos + 1).trim();
        return new Tuple<>(key, value);
    }

    public boolean open(String fileName) {
        Tuple<String, List<Tuple<String, String>>> it = null;

        try {
            try (StreamReader iniFile = new StreamReader(new FileStream(fileName, FileMode.Open))) {

                String buffer;
                while ((buffer = iniFile.readLine()) != null) {

                    if (buffer.trim().isEmpty())
                        continue;

                    switch (buffer.charAt(0)) {
                    case ';':
                    case '#':
                        // skip comments
                        break;
                    case '[':
                        try {
                            String section = parseSection(buffer);
                            List<Tuple<String, String>> keys = new ArrayList<>();
                            it = new Tuple<>(section, keys);
                            sections.add(it);
                        } catch (ParseError e) {
                            logger.log(Level.WARNING, e);
                        }
                        break;
                    default:
                        try {
                            if (it != null)
                                it.getItem2().add(parseKey(buffer));
                        } catch (ParseError e) {
                            logger.log(Level.WARNING, e);
                        }
                        break;
                    }
                }

                return true;

            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
    }

    public void close() {
        sections.clear();
    }

    public boolean setSection(String section) {
        curSection = null;
        for (Tuple<String, List<Tuple<String, String>>> c : sections) {
            if (c.getItem1().equals(section)) {
                curSection = c;
                break;
            }
        }
        return (curSection != null);
    }

    public String getValue(byte[] key) {
        if (curSection == null) return null;
        String sKey = new String(key, StandardCharsets.US_ASCII).trim();
        for (Tuple<String, String> c : curSection.getItem2()) {
            if (c.getItem1().equals(sKey)) {
                return c.getItem2();
            }
        }
        return null;
    }
}
