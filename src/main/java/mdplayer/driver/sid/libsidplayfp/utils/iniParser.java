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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mdplayer.driver.sid.libsidplayfp.utils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.Tuple;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.StreamReader;


    public class iniParser
    {



        //# include <String>
        //# include <map>
        //# include <utility>

        //private typedef std::map<std::String, std::String> keys_t;
        //private typedef std::map<std::String, keys_t> sections_t;
        private List<Tuple<String, List<Tuple<String, String>>>> sections=new ArrayList<Tuple<String, List<Tuple<String, String>>>>();// sections_t sections;
        private Tuple<String, List<Tuple<String, String>>> curSection;// sections_t::const_iterator curSection;
                                                                      //private String parseSection( String buffer) { return null; }
                                                                      //private List<Tuple<String, String>> parseKey( String buffer) { return null; }

        //public Boolean open(String fName) { return false; }
        //public void close() { }
        //public Boolean setSection(String section) { return false; }
        //public String getValue(String key) { return null; }




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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

        //#include "iniParser.h"
        //#include "sidcxx11.h"
        //#include <fstream>

        public class parseError extends RuntimeException { };

        private String parseSection( String buffer)
        {
            int pos = buffer.indexOf(']');

            if (pos == -1)
            {
                throw new parseError();
            }

            return buffer.substring(1, pos - 1);
        }

        private Tuple<String, String> parseKey( String buffer)
        {
            int pos = buffer.indexOf('=');

            if (pos == -1)
            {
                throw new parseError();
            }

            String key = buffer.substring(0, buffer.lastIndexOf(' ', pos - 1) + 1);
            String value = buffer.substring(pos + 1);
            return new Tuple<String, String>(key, value);
        }

        public Boolean open(String fName)
        {
            Tuple<String, List<Tuple<String, String>>> mIt = null;

            try
            {
                try (StreamReader iniFile = new StreamReader(new FileStream(fName, FileMode.Open)))
                {

                    String buffer;
                    while (iniFile.read() >= 0)
                    {
                        buffer = iniFile.readLine();

                        if (buffer == "")
                            continue;

                        switch (buffer.charAt(0))
                        {
                            case ';':
                            case '#':
                                // skip comments
                                break;
                            case '[':
                                try
                                {
                                    String section = parseSection( buffer);
                                    List<Tuple<String, String>> keys = null;
                                    sections.add(
                                        new Tuple<String, List<Tuple<String, String>>>(section, keys)
                                        );
                                    mIt = sections.get(0);
                                }
                                catch (parseError e)
                                {
                                }
                                break;
                            default:
                                try
                                {
                                    mIt.Item2.add(parseKey( buffer));
                                }
                                catch (parseError e)
                                {
                                }
                                break;
                        }
                    }

                    return true;

                }
            }
            catch (Exception e)
            {
                return false;

            }

        }

        public void close()
        {
            sections.clear();
        }

        public Boolean setSection(String section)
        {
            curSection = null;
            for (Tuple<String, List<Tuple<String, String>>> c : sections)
            {
                if (c.Item1 == section)
                {
                    curSection = c;
                    break;
                }
            }
            return (curSection != sections.get(sections.size() - 1));
        }

        public String getValue(byte[] key)
        {
            Tuple<String, String> keyIt = null;
            for (Tuple<String, String> c : curSection.Item2)
            {
                if (c.Item1 ==new String(key,  StandardCharsets.US_ASCII))
                {
                    keyIt = c;
                    break;
                }
            }
            return (keyIt != curSection.Item2.get(curSection.Item2.size() - 1)) ? keyIt.Item2 : null;
        }

    }
