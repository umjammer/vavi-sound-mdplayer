/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 *  Copyright 2013-2014 Leandro Nini
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

public class StringUtils {

    /**
     * Compare two characters : a case insensitive way.
     */
    public static boolean casecompare(char c1, char c2) {
        return String.valueOf(c1).equalsIgnoreCase(String.valueOf(c2));
    }

    /**
     * Compare two Strings : a case insensitive way.
     * <p>
     * @return true if Strings are equal.
     */
    public static boolean equal(String s1, String s2) {
        return s1.equals(s2);
    }

    /**
     * Compare first n characters of two Strings : a case insensitive way.
     *
     * @return true if Strings are equal.
     */
    public static boolean equal(String s1, String s2, int n) {
        if (s1 == null || s2 == null)
            return false;

        if (s1.equals(s2) || n == 0)
            return true;

        int i = 0;
        while (n-- != 0 && ((s1.charAt(i) != '\0') || (s2.charAt(i) != '\0'))) {
            if (!casecompare(s1.charAt(i), s2.charAt(i)))
                return false;
            i++;
        }

        return true;
    }
}





