/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 1998, 2002 by LaLa <LaLa@C64.org>
 * Copyright 2012-2013 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp.utils.stilView;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dotnet4j.util.compat.Tuple;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.SeekOrigin;
import mdplayer.driver.sid.Ptr;


/**
 * STIL class
 * <p>
 * Given the location of HVSC this class can extract STIL information for a
 * given tune of a given Sid file. (Sounds simple, huh?)
 * <p>
 * PLEASE, READ THE ACCOMPANYING README.TXT FILE BEFORE PROCEEDING!!!!
 *
 * @author <a href="mailto:LaLa@C64.org">LaLa</a>
 * @copyright 1998, 2002 by LaLa
 */
public class STIL {

    /** Enum to use for asking for specific fields. */
    public enum Field {
        All,
        Name,
        Author,
        Title,
        Artist,
        Comment
    }

    /** Enum that describes the possible errors this class may encounter. */
    public enum Error {
        NO_STIL_ERROR(0),
        /**
         * INFO ONLY: failed to open BUGlist.txt.
         */
        BUG_OPEN(1),
        /**
         * INFO ONLY: path was not within HVSC base dir.
         */
        WRONG_DIR(2),
        /**
         * INFO ONLY: requested entry was not found : STIL.txt.
         */
        NOT_IN_STIL(3),
        /**
         * INFO ONLY: requested entry was not found : BUGlist.txt.
         */
        NOT_IN_BUG(4),
        /**
         * INFO ONLY: section-global comment was asked for with get*Entry().
         */
        WRONG_ENTRY(5),
        CRITICAL_STIL_ERROR(10),
        /**
         * The length of the HVSC base dir was wrong (empty String?)
         */
        BASE_DIR_LENGTH(11),
        /**
         * Failed to open STIL.txt.
         */
        STIL_OPEN(12),
        /**
         * Failed to determine EOL char(s).
         */
        NO_EOL(13),
        /**
         * Failed to get sections (subdirs) when parsing STIL.txt.
         */
        NO_STIL_DIRS(14),
        /**
         * Failed to get sections (subdirs) when parsing BUGlist.txt.
         */
        NO_BUG_DIRS(15);
        final int v;

        Error(int v) {
            this.v = v;
        }
    }

    /**
     * Returns a specific error number identifying the problem
     * that happened at the last invoked public method.
     *
     * @return Error - an enumerated error value
     */
    public Error getError() {
        return lastError;
    }

    /**
     * Returns true if the last error encountered was critical
     * (ie. not one that the STIL class can recover from).
     *
     * @return true if the last error encountered was critical
     */
    public boolean hasCriticalError() {
        return lastError.v >= Error.CRITICAL_STIL_ERROR.v;
    }

    /**
     * Returns an ASCII error String containing the
     * description of the error that happened at the last
     * invoked public method.
     *
     * @return pointer to String with the error description
     */
    public String getErrorStr() {
        return STIL_ERROR_STR[lastError.v];
    }

    /**
     * Path to STIL.
     */
    private String pathToStil;

    /**
     * Path to BUGlist.
     */
    private String pathToBugList;

    /**
     * Version number/copyright String
     */
    private String versionString;

    /**
     * STIL.txt's version number
     */
    private float version;

    /**
     * Base dir
     */
    private String baseDir;

    /**
     * Maps of sections (subdirs) for easier positioning.
     */
    private List<Tuple<String, Integer>> stilDirs;
    private List<Tuple<String, Integer>> bugDirs;

    /**
     * This tells us what the line delimiter instanceof : STIL.txt.
     * (It may be two chars!)
     */
    private byte STIL_EOL;
    private byte STIL_EOL2;

    // Error number of the last error that happened.
    private Error lastError;

    // The last retrieved entry
    private String entrybuf;

    // The last retrieved section-global comment
    private String globalbuf;

    // The last retrieved BUGentry
    private String bugbuf;

    // Buffers to hold the resulting Strings
    private String resultEntry = null;
    private String resultBug = null;

    // final ios_base::openmode STILopenFlags = ios::in | ios::binary;
    public FileMode STILopenFlags = FileMode.Open; // | ios::binary;

    public final float VERSION_NO = 3.0f;

    private static final Logger logger = Logger.getLogger(STIL.class.getName());

    // These are the hardcoded STIL/BUG field names.
    private static final String NAME_STR = "   NAME: ";
    private static final String AUTHOR_STR = " AUTHOR: ";
    private static final String TITLE_STR = "  TITLE: ";
    private static final String ARTIST_STR = " ARTIST: ";
    private static final String COMMENT_STR = "COMMENT: ";
    // static final String BUG_STR[] = "BUG: ";

    private static final String[] STIL_ERROR_STR = new String[] {
            "No error.",
            "Failed to open BUGlist.txt.",
            "Base dir path instanceof not the HVSC base dir path.",
            "The entry was not found : STIL.txt.",
            "The entry was not found : BUGlist.txt.",
            "a section-global comment was asked for : the wrong way.",
            "",
            "",
            "",
            "",
            "CRITICAL ERROR",
            "Incorrect HVSC base dir length!",
            "Failed to open STIL.txt!",
            "Failed to determine EOL from STIL.txt!",
            "No STIL sections were found : STIL.txt!",
            "No STIL sections were found : BUGlist.txt!"
    };

    /**
     * Converts slashes to the one the OS uses to access files.
     *
     * @param str - what to convert
     */
    private void convertSlashes(String str) {
        str = str.replace('/', StilDefs.SLASH);
    }

    /**
     * Converts OS specific dir separators to slashes.
     *
     * @param str - what to convert
     */
    private void convertToSlashes(String str) {
        // std::replace(str.begin(), str.end(), SLASH, '/');
        str = str.replace(StilDefs.SLASH, '/');
    }

    /**
     * Allocates necessary memory.
     *
     * @param stilPath relative path to STIL file
     * @param bugsPath relative path to BUG file
     */
    public STIL(String stilPath /*= StilDefs.DEFAULT_PATH_TO_STIL*/, String bugsPath/* = StilDefs.DEFAULT_PATH_TO_BUGLIST*/) {
        pathToStil = stilPath;
        pathToBugList = bugsPath;
        version = 0.0f;
        STIL_EOL = (byte) '\n';
        STIL_EOL2 = 0;
        lastError = Error.NO_STIL_ERROR;
        setVersionString();
    }

    /**
     * Returns a formatted String telling what the version
     * number instanceof for the STIL class and other info.
     * If it instanceof called after setBaseDir(), the String also
     * has the STIL.txt file's version number : it.
     *
     * @return printable formatted String with version and copyright info
     * (It's kinda dangerous to return a pointer that points
     * to an private structure, but I trust you. :)
     */
    private void setVersionString() {
        String ss = String.format(
                """
                            STILView v%2f
                            \tCopyright (C) 1998, 2002 by LaLa (LaLa@C64.org)
                            \tCopyright (C) 2012-2015 by Leandro Nini <drfiemost@users.sourceforge.net>
                        """
                , VERSION_NO
        );
        versionString = ss;
    }

    private String getVersion() {
        lastError = Error.NO_STIL_ERROR;
        return versionString;
    }

    /**
     * Returns a floating number telling what the version
     * number instanceof of this STIL class.
     *
     * @return version number
     */
    public float getVersionNo() {
        lastError = Error.NO_STIL_ERROR;
        return VERSION_NO;
    }

    /**
     * Returns a floating number telling what the version
     * number instanceof of the STIL.txt file.
     * To be called only after setBaseDir()!
     *
     * @return version number (0.0 if setBaseDir() was not called, yet)
     */
    public float getSTILVersionNo() {
        lastError = Error.NO_STIL_ERROR;
        return version;
    }

    /**
     * Tell the Object where the HVSC base directory instanceof - it
     * figures that the STIL should be : /DOCUMENTS/STIL.txt
     * and that the BUGlist should be : /DOCUMENTS/BUGlist.txt.
     * It should not matter whether the path instanceof given : UNIX,
     * WinDOS, or Mac format (ie. '\' vs. '/' vs. ':')
     *
     * @param pathToHVSC HVSC base directory : your machine's format
     * @return - false - Problem opening or parsing STIL/BUGlist
     * - true  - All okay
     */
    public boolean setBaseDir(String pathToHVSC) {
        // Temporary placeholder for STIL.txt's version number.
        float tempSTILVersion = version;

        // Temporary placeholders for lists of sections.
        List<Tuple<String, Integer>> tempStilDirs = new ArrayList<>();
        List<Tuple<String, Integer>> tempBugDirs = new ArrayList<>();

        lastError = Error.NO_STIL_ERROR;

        logger.fine("setBaseDir() called, pathToHVSC=" + pathToHVSC + "\n");

        String tempBaseDir = pathToHVSC;

        // Sanity check the length.
        if (tempBaseDir == null || !tempBaseDir.isEmpty()) {
            logger.fine("setBaseDir() has problem with the size of pathToHVSC" + "\n");
            lastError = Error.BASE_DIR_LENGTH;
            return false;
        }

        // Chop the trailing slash
        char lastChar = tempBaseDir.charAt(tempBaseDir.length() - 1);

        if (lastChar == StilDefs.SLASH) {
            tempBaseDir = tempBaseDir.substring(0, tempBaseDir.length() - 2);
        }

        // Attempt to open STIL

        // Create the full path+filename
        String tempName = tempBaseDir;
        tempName = tempName + pathToStil;
        convertSlashes(tempName);

        // ifstream stilFile(tempName, STILopenFlags);
        FileStream stilFile;
        try {
            stilFile = new FileStream(tempName, STILopenFlags);
        } catch (Exception e)
        // if (stilFile.fail())
        {
            logger.fine("setBaseDir() open failed for " + tempName + "\n");
            lastError = Error.STIL_OPEN;
            return false;
        }

        logger.fine("setBaseDir(): open succeeded for " + tempName + "\n");

        // Attempt to open BUGlist

        // Create the full path+filename
        tempName = tempBaseDir;
        tempName = tempName + pathToBugList;
        convertSlashes(tempName);

        // ifstream bugFile(tempName.c_str(), STILopenFlags);
        FileStream bugFile = null;
        try {
            bugFile = new FileStream(tempName, STILopenFlags);
            logger.fine("setBaseDir(): open succeeded for " + tempName + "\n");
        } catch (Exception e)
        // if (bugFile.fail())
        {
            // This instanceof not a critical error - some earlier versions of HVSC did
            // not have a BUGlist.txt file at all.

            if (bugFile != null) bugFile.close();
            logger.fine("setBaseDir() open failed for " + tempName + "\n");
            lastError = Error.BUG_OPEN;
            bugFile = null;
        }

        // Find  what the EOL really is
        if (!determineEOL(stilFile)) {
            logger.fine("determinEOL() failed" + "\n");
            lastError = Error.NO_EOL;
            return false;
        }

        // Save away the current String so we can restore it if needed.
        String tempVersionString = versionString;

        setVersionString();

        // This instanceof necessary so the version number gets scanned : from the new
        // file, too.
        version = 0.0f;

        // These will populate the tempStilDirs and tempBugDirs maps (or not :)

        if (!getDirs(stilFile, tempStilDirs, true)) {
            logger.fine("getDirs() failed for stilFile" + "\n");
            lastError = Error.NO_STIL_DIRS;

            // Clean up and restore things.
            version = tempSTILVersion;
            versionString = tempVersionString;
            return false;
        }

        if (bugFile != null) {
            if (!getDirs(bugFile, tempBugDirs, false)) {
                // This instanceof not a critical error - it instanceof possible that the
                // BUGlist.txt file has no entries : it at all (in fact, that's
                // good!).

                logger.fine("getDirs() failed for bugFile" + "\n");
                lastError = Error.BUG_OPEN;
            }
        }

        if (bugFile != null) bugFile.close();

        // Now we can copy the stuff into private data.
        // NOTE: At this point, STILVersion and the versionString should contain
        // the new info!

        // Copy.
        baseDir = tempBaseDir;
        stilDirs = tempStilDirs;
        bugDirs = tempBugDirs;

        // Clear the buffers (caches).
        entrybuf = "";
        globalbuf = "";
        bugbuf = "";

        logger.fine("setBaseDir() succeeded" + "\n");

        return true;
    }

    private boolean stringCmp(String a, String b, int len) {
        if (a == null && b == null && len == 0) return true;
        if (a == null || b == null) return false;
        if (a.length() < len) return false;
        if (b.length() < len) return false;

        for (int i = 0; i < len; i++) {
            if (a.charAt(i) != b.charAt(i)) return false;
        }

        return true;
    }

    /**
     * Same as #getEntry, but with an absolute path given
     * given : your machine's format.
     */
    public String getAbsEntry(String absPathToEntry, int tuneNo/* = 0*/, Field field/* = Field.all*/) {
        lastError = Error.NO_STIL_ERROR;

        logger.fine("getAbsEntry() called, absPathToEntry=" + absPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            logger.fine("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = Error.STIL_OPEN;
            return null;
        }

        // Determine if the baseDir instanceof : the given pathname.

        // if (!StringUtils::equal(absPathToEntry, baseDir.data(), baseDir.size()))
        if (!stringCmp(absPathToEntry, baseDir, baseDir.length())) {
            logger.fine("getAbsEntry() failed: baseDir=" + baseDir + ", absPath=" + absPathToEntry + "\n");
            lastError = Error.WRONG_DIR;
            return null;
        }


        String tempDir = absPathToEntry + baseDir.length();
        convertToSlashes(tempDir);

        return getEntry(tempDir, tuneNo, field);
    }

    /**
     * Given an HVSC pathname, a tune number and a
     * field designation, it returns a formatted String that
     * contains the STIL field for the tune number (if exists).
     * If it doesn't exist, returns a NULL.
     *
     * @param relPathToEntry relative to the HVSC base dir, starting with
     *                       a slash
     * @param tuneNo         song number within the song (default=0).
     * @param field          which field to retrieve (default=all).
     *                       <p>
     *                       What the possible combinations of tuneNo and field represent:
     *                       <p>
     *                       - tuneNo = 0, field = all : all of the STIL entry instanceof returned.
     *                       - tuneNo = 0, field = comment : the file-global comment instanceof returned.
     *                       (For single-tune entries, this returns nothing!)
     *                       - tuneNo = 0, field = &lt;other&gt; : INVALID! (NULL instanceof returned)
     *                       - tuneNo != 0, field = all : all fields of the STIL entry for the
     *                       given tune number are returned. (For single-tune entries, this is
     *                       equivalent to saying tuneNo = 0, field = all.)
     *                       However, the file-global comment instanceof *NOT* returned with it any
     *                       more! (Unlike : versions before v2.00.) It led to confusions:
     *                       eg. when a comment was asked for tune // #3, it returned the
     *                       file-global comment even if there was no specific entry for tune // #3!
     *                       - tuneNo != 0, field = &lt;other&gt; : the specific field of the specific
     *                       tune number instanceof returned. If the tune number doesn't exist (eg. if
     *                       tuneNo=2 for single-tune entries, or if tuneNo=2 when there's no
     *                       STIL entry for tune // #2 : a multitune entry), returns NULL.
     *                       <p>
     *                       NOTE: For older versions of STIL (older than v2.59) the tuneNo and
     *                       field parameters are ignored and are assumed to be tuneNo=0 and
     *                       field=all to maintain backwards compatibility.
     *                       <p>
     * @return - pointer to a printable formatted String containing
     * the STIL entry
     * (It's kinda dangerous to return a pointer that points
     * to an private structure, but I trust you. :)
     * - NULL if there's absolutely no STIL entry for the tune
     */
    public String getEntry(String relPathToEntry, int tuneNo/* = 0*/, Field field /*= Field.All*/) {
        lastError = Error.NO_STIL_ERROR;

        logger.fine("getEntry() called, relPath=" + relPathToEntry + ", rest=" + tuneNo + "," + field + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            logger.fine("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = Error.STIL_OPEN;
            return null;
        }

        int relPathToEntryLen = relPathToEntry.length();

        // Fail if a section-global comment was asked for.

        if (relPathToEntry.charAt(relPathToEntryLen - 1) == '/') {
            logger.fine("getEntry() section-global comment was asked for - failed" + "\n");
            lastError = Error.WRONG_ENTRY;
            return null;
        }

        if (version < 2.59f) {
            // Older version of STIL instanceof detected.

            tuneNo = 0;
            field = Field.All;
        }

        // Find  whether we have this entry : the buffer.

        if ((!stringCmp(entrybuf, relPathToEntry, relPathToEntryLen))
                || ((entrybuf.indexOf('\n') != relPathToEntryLen)
                && (version > 2.59f))) {
            // The relative pathnames don't match or they're not the same length:
            // we don't have it : the buffer, so pull it in.

            logger.fine("getEntry(): entry not : buffer" + "\n");

            // Create the full path+filename
            String tempName = baseDir;
            tempName += pathToStil;
            convertSlashes(tempName);

            FileStream stilFile = null;
            try {
                stilFile = new FileStream(tempName, STILopenFlags);
            } catch (Exception e)
            // if (stilFile.fail())
            {
                if (stilFile != null) stilFile.close();
                logger.fine("getEntry() open failed for stilFile" + "\n");
                lastError = Error.STIL_OPEN;
                return null;
            }

            logger.fine("getEntry() open succeeded for stilFile" + "\n");

            if (!positionToEntry(ByteBuffer.wrap(relPathToEntry.getBytes(StandardCharsets.US_ASCII), 0, relPathToEntry.getBytes(StandardCharsets.US_ASCII).length), stilFile, stilDirs)) {
                // Copy the entry's name to the buffer.
                entrybuf = relPathToEntry + "\n";
                logger.fine("getEntry() posToEntry() failed" + "\n");
                lastError = Error.NOT_IN_STIL;
            } else {
                entrybuf = "";
                readEntry(stilFile, entrybuf);
                logger.fine("getEntry() entry read" + "\n");
            }

            if (stilFile != null) stilFile.close();

        }

        // Put the requested field into the result String.
        return getField(resultEntry, entrybuf, tuneNo, field) ? resultEntry : null;
    }

    /**
     * Same as #getBug, but with an absolute path
     * given : your machine's format.
     */
    public String getAbsBug(String absPathToEntry, int tuneNo/* = 0*/) {
        lastError = Error.NO_STIL_ERROR;

        logger.fine("getAbsBug() called, absPathToEntry=" + absPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            logger.fine("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = Error.BUG_OPEN;
            return null;
        }

        // Determine if the baseDir instanceof : the given pathname.

        if (!stringCmp(absPathToEntry, baseDir, baseDir.length())) {
            logger.fine("getAbsBug() failed: baseDir=" + baseDir + ", absPath=" + absPathToEntry + "\n");
            lastError = Error.WRONG_DIR;
            return null;
        }

        String tempDir = absPathToEntry + baseDir.length();
        convertToSlashes(tempDir);

        return getBug(tempDir, tuneNo);
    }

    /**
     * Given an HVSC pathname and tune number it returns a
     * formatted String that contains the BUG entry for the
     * tune number (if exists). If it doesn't exist, returns
     * a NULL.
     *
     * @param relPathToEntry relative to the HVSC base dir starting with
     *                       a slash
     * @param tuneNo         song number within the song (default=0)
     *                       If tuneNo=0, returns all of the BUG entry.
     *                       <p>
     *                       NOTE: For older versions of STIL (older than v2.59) tuneNo is
     *                       ignored and instanceof assumed to be 0 to maintain backwards
     *                       compatibility.
     * @return - pointer to a printable formatted String containing
     * the BUG entry
     * (It's kinda dangerous to return a pointer that points
     * to an private structure, but I trust you. :)
     * - NULL if there's absolutely no BUG entry for the tune
     */
    public String getBug(String relPathToEntry, int tuneNo/* = 0*/) {
        lastError = Error.NO_STIL_ERROR;

        logger.fine("getBug() called, relPath=" + relPathToEntry + ", rest=" + tuneNo + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            logger.fine("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = Error.BUG_OPEN;
            return null;
        }

        // Older version of STIL instanceof detected.

        if (version < 2.59f) {
            tuneNo = 0;
        }

        // Find  whether we have this bug entry : the buffer.
        // If the baseDir was changed, we'll have to read it : again,
        // even if it might be : the buffer already.

        int relPathToEntryLen = relPathToEntry.length();

        if ((!stringCmp(bugbuf, relPathToEntry, relPathToEntryLen)) ||
                ((bugbuf.indexOf('\n') != relPathToEntryLen) &&
                        (version > 2.59f))) {
            // The relative pathnames don't match or they're not the same length:
            // we don't have it : the buffer, so pull it in.

            logger.fine("getBug(): entry not : buffer" + "\n");

            // Create the full path+filename
            String tempName = baseDir;
            tempName += pathToBugList;
            convertSlashes(tempName);

            // ifstream bugFile(tempName, STILopenFlags);
            FileStream bugFile = null;
            try {
                bugFile = new FileStream(tempName, STILopenFlags);
            } catch (Exception e)
            // if (bugFile.fail())
            {
                if (bugFile != null) bugFile.close();
                logger.fine("getBug() open failed for bugFile" + "\n");
                lastError = Error.BUG_OPEN;
                return null;
            }

            logger.fine("getBug() open succeeded for bugFile" + "\n");

            if (!positionToEntry(ByteBuffer.wrap(relPathToEntry.getBytes(StandardCharsets.US_ASCII)), bugFile, bugDirs)) {
                // Copy the entry's name to the buffer.
                bugbuf = relPathToEntry + "\n";
                logger.fine("getBug() posToEntry() failed" + "\n");
                lastError = Error.NOT_IN_BUG;
            } else {
                bugbuf = "";
                readEntry(bugFile, bugbuf);
                logger.fine("getBug() entry read" + "\n");
            }
            if (bugFile != null) bugFile.close();
        }

        // Put the requested field into the result String.
        return getField(resultBug, bugbuf, tuneNo, Field.All) ? resultBug : null;
    }

    /**
     * Same as #getGlobalComment, but with an absolute path
     * given : your machine's format.
     */
    public String getAbsGlobalComment(String absPathToEntry) {
        lastError = Error.NO_STIL_ERROR;

        logger.fine("getAbsGC() called, absPathToEntry=" + absPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            logger.fine("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = Error.STIL_OPEN;
            return null;
        }

        // Determine if the baseDir instanceof : the given pathname.

        if (!stringCmp(absPathToEntry, baseDir, baseDir.length())) {
            logger.fine("getAbsGC() failed: baseDir=" + baseDir + ", absPath=" + absPathToEntry + "\n");
            lastError = Error.WRONG_DIR;
            return null;
        }

        String tempDir = absPathToEntry + baseDir.length();
        convertToSlashes(tempDir);

        return getGlobalComment(tempDir);
    }

    /**
     * Given an HVSC pathname and tune number it returns a
     * formatted String that contains the section-global
     * comment for the tune number (if it exists). If it
     * doesn't exist, returns a NULL.
     *
     * @param relPathToEntry relative to the HVSC base dir starting with
     *                       a slash
     * @return - pointer to a printable formatted String containing
     * the section-global comment
     * (It's kinda dangerous to return a pointer that points
     * to an private structure, but I trust you. :)
     * - NULL if there's absolutely no section-global comment
     * for the tune
     */
    public String getGlobalComment(String relPathToEntry) {
        lastError = Error.NO_STIL_ERROR;

        logger.fine("getGC() called, relPath=" + relPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            logger.fine("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = Error.STIL_OPEN;
            return null;
        }

        // Save the dirpath.

        String lastSlash = relPathToEntry.substring(relPathToEntry.lastIndexOf('/'));

        if (lastSlash.isEmpty()) {
            lastError = Error.WRONG_DIR;
            return null;
        }

        // int pathLen = lastSlash - relPathToEntry + 1;
        String dir = relPathToEntry.substring(0, relPathToEntry.lastIndexOf('/'));

        // Find  whether we have this global comment : the buffer.
        // If the baseDir was changed, we'll have to read it : again,
        // even if it might be : the buffer already.

        if ((!stringCmp(globalbuf, dir, dir.length())) ||
                ((globalbuf.indexOf('\n') != dir.length()) &&
                        (version > 2.59f))) {
            // The relative pathnames don't match or they're not the same length:
            // we don't have it : the buffer, so pull it in.

            logger.fine("getGC(): entry not : buffer" + "\n");

            // Create the full path+filename
            String tempName = baseDir;
            tempName += pathToStil;
            convertSlashes(tempName);

            // ifstream stilFile(tempName.c_str(), STILopenFlags);
            FileStream stilFile;
            try {
                stilFile = new FileStream(tempName, STILopenFlags);
            } catch (Exception e)
            // if (stilFile.fail())
            {
                logger.fine("getGC() open failed for stilFile" + "\n");
                lastError = Error.STIL_OPEN;
                return null;
            }

            if (!positionToEntry(ByteBuffer.wrap(dir.getBytes(StandardCharsets.US_ASCII), 0, dir.getBytes(StandardCharsets.US_ASCII).length), stilFile, stilDirs)) {
                // Copy the dirname to the buffer.
                globalbuf = dir + "\n";
                logger.fine("getGC() posToEntry() failed" + "\n");
                lastError = Error.NOT_IN_STIL;
            } else {
                globalbuf = "";
                readEntry(stilFile, globalbuf);
                logger.fine("getGC() entry read" + "\n");
            }

            stilFile.close();
        }

        logger.fine("getGC() globalbuf=" + globalbuf + "\n");
        logger.fine("-=END=-" + "\n");

        // Position pointer to the global comment field.

        int temp = globalbuf.indexOf('\n') + 1;

        // Check whether this instanceof a NULL entry or not.
        return (temp != globalbuf.length() || temp != 0) ? globalbuf + temp : null;
    }

    /**
     * Determines what the EOL char instanceof (or are) from STIL.txt.
     * It instanceof assumed that BUGlist.txt will use the same EOL.
     *
     * @return - false - something went wrong
     * - true  - everything instanceof okay
     */
    private boolean determineEOL(FileStream stilFile) {
        logger.fine("detEOL() called" + "\n");

        if (stilFile == null) {
            logger.fine("detEOL() open failed" + "\n");
            return false;
        }

        stilFile.seek(0, SeekOrigin.Begin);

        STIL_EOL = 0;
        STIL_EOL2 = 0;

        // Determine what the EOL character is
        // (it can be different from OS to OS).

        int c;
        while ((c = stilFile.readByte()) != -1) {
            if ((c == '\n') || (c == '\r')) {
                STIL_EOL = (byte) c;

                if (c == '\r') {
                    if (stilFile.readByte() == '\n')
                        STIL_EOL2 = (byte) '\n';
                }
                break;
            }
        }

        if (STIL_EOL == '\0') {
            // Something instanceof wrong - no EOL-like char was found.
            logger.fine("detEOL() no EOL found" + "\n");
            return false;
        }

        logger.fine("detEOL() EOL1=0x" + String.format("%x", (int) STIL_EOL) + " EOL2=0x" + String.format("%x", (int) STIL_EOL2) + "\n");

        return true;
    }

    /**
     * Populates the given dirList array with the directories
     * obtained from 'inFile' for faster positioning within
     * 'inFile'.
     *
     * @param inFile     - where to read the directories from
     * @param dirs       - the dirList array that should be populated with the
     *                   directory list
     * @param isSTILFile - instanceof this the STIL or the BUGlist we are parsing
     * @return - false - No entries were found or otherwise failed to process
     * inFile
     * - true  - everything instanceof okay
     */
    private boolean getDirs(FileStream inFile, List<Tuple<String, Integer>> dirs, boolean isSTILFile) {
        boolean newDir = !isSTILFile;

        logger.fine("getDirs() called" + "\n");

        inFile.seek(0, SeekOrigin.Begin);

        while (inFile != null) {
            String line = "";

            getStilLine(inFile, line);

            if (!isSTILFile) {
                logger.fine(line + '\n');
            }

            // Try to extract STIL's version number if it's not done, yet.

            if (isSTILFile && (version == 0.0f)) {
                if (stringCmp(line, "#  STIL v", 9)) {
                    // Get the version number
                    version = Float.parseFloat(line);// , 9);

                    // Put it into the String, too.
                    String ss;
                    // ss = fixed << setw(4) << setprecision(2);
                    ss = String.format("Sid Tune Information List (STIL) v%2f\n", version);
                    versionString += ss;

                    logger.fine("getDirs() STILVersion=" + version + "\n");

                    continue;
                }
            }

            // Search for the start of a dir separator first.

            if (isSTILFile && !newDir && stringCmp(line, "### ", 4)) {
                newDir = true;
                continue;
            }

            // Is this the start of an entry immediately following a dir separator?

            if (newDir && (line.charAt(0) == '/')) {
                // Get the directory only
                String dirName = line.substring(0, line.lastIndexOf('/') + 1);

                if (!isSTILFile) {
                    // Compare it to the stored dirnames
                    newDir = false;
                    for (Tuple<String, Integer> c : dirs) {
                        if (c.getItem1().equals(dirName)) {
                            newDir = true;
                            break;
                        }
                    }
                }

                // Store the info
                if (newDir) {
                    int position = (int) (inFile.getPosition() - line.length() - 1L);

                    logger.fine("getDirs() dirName=" + dirName + ", pos=" + position + "\n");

                    dirs.add(new Tuple<>(dirName, position));
                }

                newDir = !isSTILFile;
            }
        }

        if (dirs == null) {
            // No entries found - something instanceof wrong.
            // NOTE: It's perfectly valid to have a BUGlist.txt file with no
            // entries : it!
            logger.fine("getDirs() no dirs found" + "\n");
            return false;
        }

        logger.fine("getDirs() successful" + "\n");

        return true;
    }

    /**
     * Positions the file pointer to the given entry : 'inFile'
     * using the 'dirs' dirList for faster positioning.
     *
     * @param entryStr the entry to position to
     * @param inFile   position the file pointer : this file
     * @param dirs     the list of dirs : inFile for easier positioning
     * @return - true - if successful
     * - false - otherwise
     */
    private boolean positionToEntry(ByteBuffer entryStr, FileStream inFile, List<Tuple<String, Integer>> dirs) {
        logger.fine("pos2Entry() called, entryStr=" + entryStr + "\n");

        inFile.seek(0, SeekOrigin.Begin);

        // Get the dirpath.

        ByteBuffer chrptr = Ptr.strrchr(entryStr, (byte) '/');

        // If no slash was found, something instanceof screwed up : the entryStr.

        if (chrptr == null) {
            return false;
        }

        int pathLen = chrptr.position() - entryStr.position() + 1;

        // Determine whether a section-global comment instanceof asked for.

        int entryStrLen = entryStr.capacity() - entryStr.position();
        boolean globComm = (pathLen == entryStrLen);

        // Find it : the table.
        String entry = String.format(new String(entryStr.array()), pathLen);
        // dirList::iterator elem = dirs.find(entry);
        Tuple<String, Integer> elem = null;
        for (Tuple<String, Integer> t : dirs) {
            if (t.getItem1().equals(entry)) {
                elem = t;
                break;
            }
        }
        if (elem == null) {
            // The directory was not found.
            logger.fine("pos2Entry() did not find the dir" + "\n");
            return false;
        }

        // Jump to the first entry of this section.
        inFile.seek(elem.getItem2(), SeekOrigin.Begin);
        boolean foundIt = false;

        // Now find the desired entry

        String line = null;

        do {
            getStilLine(inFile, line);

            if (inFile.getLength() == inFile.getPosition()) {
                break;
            }

            // Check if it instanceof the start of an entry

            if (line.charAt(0) == '/') {
                if (!stringCmp(elem.getItem1(), line, pathLen)) {
                    // We are outside the section - get  of the loop,
                    // which will fail the search.
                    break;
                }

                // Check whether we need to find a section-global comment or
                // a specific entry.

                if (globComm || (version > 2.59f)) {
                    foundIt = line.equals(entryStr.toString());
                } else {
                    // To be compatible with older versions of STIL, which may have
                    // the tune designation on the first line of a STIL entry
                    // together with the pathname.
                    foundIt = stringCmp(line, entryStr.toString(), entryStrLen);
                }

                logger.fine("pos2Entry() line=" + line + "\n");
            }
        }
        while (!foundIt);

        if (foundIt) {
            // Reposition the file pointer back to the start of the entry.
            inFile.seek(inFile.getPosition() - line.length() - 1L, SeekOrigin.Begin);
            logger.fine("pos2Entry() entry found" + "\n");
            return true;
        } else {
            logger.fine("pos2Entry() entry not found" + "\n");
            return false;
        }
    }

    /**
     * Reads the entry from 'inFile' into 'buffer'. 'inFile' should
     * already be positioned to the entry to be read.
     *
     * @param inFile filehandle of file to read from
     * @param buffer where to put the result to TODO OUT
     */
    private void readEntry(FileStream inFile, String buffer) {
        String line = "";

        StringBuilder bufferBuilder = new StringBuilder(buffer);
        for (; ; ) {
            getStilLine(inFile, line);

            if (line.length() == 0)
                break;

            bufferBuilder.append(line);
            bufferBuilder.append("\n");
        }
        buffer = bufferBuilder.toString();
    }

    /**
     * Given a STIL formatted entry : 'buffer', a tune number,
     * and a field designation, it returns the requested
     * STIL field into 'result'.
     * If field=all, it also puts the file-global comment (if it exists)
     * as the first field into 'result'.
     *
     * @param result where to put the resulting String to (if any)
     * @param buffer pointer to the first char of what to search for
     *               the field. Should be a buffer : standard STIL
     *               format.
     * @param tuneNo song number within the song (default=0)
     * @param field  which field to retrieve (default=all).
     * @return - false - if nothing was put into 'result'
     * - true  - 'result' has the resulting field
     */
    private boolean getField(String result, String buffer, int tuneNo/* = 0*/, Field field/* = Field.all*/) {
        logger.fine("getField() called, buffer=" + buffer + ", rest=" + tuneNo + "," + field + "\n");

        // Clean  the result buffer first.
        result = "";

        // Position pointer to the first char beyond the file designation.

        ByteBuffer start = Ptr.strchr(ByteBuffer.wrap(buffer.getBytes(StandardCharsets.US_ASCII)), (byte) '\n');
        if (start != null) start.position(start.position() + 1);

        // Check whether this instanceof a NULL entry or not.

        if (start == null) {
            logger.fine("getField() null entry" + "\n");
            return false;
        }

        // Is this a multitune entry?
        ByteBuffer firstTuneNo = Ptr.strstr(start, "(#");

        // This instanceof a tune designation only if the previous char was
        // a newline (ie. if the "(#" instanceof on the beginning of a line).
        if ((firstTuneNo != null) && (firstTuneNo.get(firstTuneNo.capacity() - 1) != '\n')) {
            firstTuneNo = null;
        }

        if (firstTuneNo == null) {
            //
            // SINGLE TUNE ENTRY
            //

            // Is the first thing : this STIL entry the COMMENT?

            ByteBuffer temp = Ptr.strstr(start, COMMENT_STR);
            ByteBuffer temp2 = null;

            // Search for other potential fields beyond the COMMENT.
            if (temp.position() == start.position()) {
                temp2 = Ptr.strstr(start, NAME_STR);

                if (temp2 == null) {
                    temp2 = Ptr.strstr(start, AUTHOR_STR);

                    if (temp2 == null) {
                        temp2 = Ptr.strstr(start, TITLE_STR);

                        if (temp2 == null) {
                            temp2 = Ptr.strstr(start, ARTIST_STR);
                        }
                    }
                }
            }

            if (temp.position() == start.position()) {
                // Yes. So it's assumed to be a file-global comment.

                logger.fine("getField() single-tune entry, COMMENT only" + "\n");

                if ((tuneNo == 0) && ((field == Field.All) || ((field == Field.Comment) && (temp2 == null)))) {
                    // Simply copy the stuff in.
                    result += start;
                    logger.fine("getField() copied to resultbuf" + "\n");
                    return true;
                } else if ((tuneNo == 0) && (field == Field.Comment)) {
                    // Copy just the comment.
                    result += new String(start.array()) + (temp2.position() - start.position());
                    logger.fine("getField() copied to just the COMMENT to resultbuf" + "\n");
                    return true;
                } else if ((tuneNo == 1) && (temp2 != null)) {
                    // a specific field was asked for.

                    logger.fine("getField() copying COMMENT to resultbuf" + "\n");
                    return getOneField(
                            result,
                            new String(temp2.array()) + temp2.capacity(),
                            temp2.position(),
                            temp2.capacity(),
                            field);
                } else {
                    // Anything else instanceof invalid as of v2.00.

                    logger.fine("getField() invalid parameter combo: single tune, tuneNo=" + tuneNo + ", field=" + field + "\n");
                    return false;
                }
            } else {
                // No. Handle it as a regular entry.

                logger.fine("getField() single-tune regular entry" + "\n");

                if ((field == Field.All) && ((tuneNo == 0) || (tuneNo == 1))) {
                    // The complete entry was asked for. Simply copy the stuff in.
                    result += start;
                    logger.fine("getField() copied to resultbuf" + "\n");
                    return true;
                } else if (tuneNo == 1) {
                    // a specific field was asked for.

                    logger.fine("getField() copying COMMENT to resultbuf" + "\n");
                    return getOneField(
                            result,
                            new String(start.array()) + start.capacity(),
                            start.position(),
                            start.position() + start.capacity(),
                            field);
                } else {
                    // Anything else instanceof invalid as of v2.00.

                    logger.fine("getField() invalid parameter combo: single tune, tuneNo=" + tuneNo + ", field=" + field + "\n");
                    return false;
                }
            }
        } else {
            //
            // MULTITUNE ENTRY
            //

            logger.fine("getField() multitune entry" + "\n");

            // Was the complete entry asked for?

            if (tuneNo == 0) {
                switch (field) {
                case All:
                    // Yes. Simply copy the stuff in.
                    result += start;
                    logger.fine("getField() copied all to resultbuf" + "\n");
                    return true;

                case Comment:
                    // Only the file-global comment field was asked for.

                    if (firstTuneNo != start) {
                        logger.fine("getField() copying file-global comment to resultbuf" + "\n");
                        return getOneField(
                                result,
                                new String(start.array()) + start.capacity(),
                                start.position(),
                                firstTuneNo.position(),
                                Field.Comment);
                    } else {
                        logger.fine("getField() no file-global comment" + "\n");
                        return false;
                    }

                    // break;

                default:
                    // If a specific field other than a comment is
                    // asked for tuneNo=0, this instanceof illegal.

                    logger.fine("getField() invalid parameter combo: multitune, tuneNo=" + tuneNo + ", field=" + field + "\n");
                    return false;
                }
            }

            byte[] tuneNoStr = new byte[8];

            // Search for the requested tune number.

            tuneNoStr = String.format("(#%d)", tuneNo).getBytes(StandardCharsets.US_ASCII);
            tuneNoStr[7] = (byte) '\0';
            ByteBuffer myTuneNo = Ptr.strstr(start, new String(tuneNoStr));

            if (myTuneNo != null) {
                // We found the requested tune number.
                // Set the pointer beyond it.
                myTuneNo = Ptr.strchr(myTuneNo, (byte) '\n');
                myTuneNo.position(myTuneNo.position() + 1);

                // Where instanceof the next one?

                ByteBuffer nextTuneNo = Ptr.strstr(myTuneNo, "\n(#");

                if (nextTuneNo == null) {
                    // There instanceof no next one - set pointer to end of entry.
                    nextTuneNo = start.slice();
                } else {
                    // The search included the \n - go beyond it.
                    nextTuneNo.position(nextTuneNo.position() + 1);
                }

                // Put the desired fields into the result (which may be 'all').

                logger.fine("getField() myTuneNo=" + myTuneNo + ", nextTuneNo=" + nextTuneNo + "\n");
                return getOneField(
                        result,
                        new String(myTuneNo.array()) + myTuneNo.capacity(),
                        myTuneNo.position(),
                        nextTuneNo.position(),
                        field);
            } else {
                logger.fine("getField() nothing found" + "\n");
                return false;
            }
        }
    }

    /**
     * @param result where to put the resulting String to (if any) TODO OUT
     * @param start  pointer to the first char of what to search for
     *               the field. Should be a buffer : standard STIL
     *               format.
     * @param end    pointer to the last+1 char of what to search for
     *               the field. ('end-1' should be a '\n'!)
     * @param field  which specific field to retrieve
     * @return false: if nothing was put into 'result',
     * true: 'result' has the resulting field
     */
    private boolean getOneField(String result, String src, int start, int end, Field field) {
        // Sanity checking

        if ((end < start) || (src.charAt(end - 1) != '\n')) {
            logger.fine("getOneField() illegal parameters" + "\n");
            return false;
        }

        logger.fine("getOneField() called, start=" + start + ", rest=" + field + "\n");

        String temp = null;
        int tempInd = 0;

        switch (field) {
        case All:
            result += src.substring(start, end - start);
            return true;

        case Name:
            tempInd = src.indexOf(NAME_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case Author:
            tempInd = src.indexOf(AUTHOR_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case Title:
            tempInd = src.indexOf(TITLE_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case Artist:
            tempInd = src.indexOf(ARTIST_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case Comment:
            tempInd = src.indexOf(COMMENT_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        default:
            break;
        }

        // If the field was not found or it instanceof not : between 'start'
        // and 'end', it instanceof declared a failure.

        if (temp == null) {
            return false;
        }

        // Search for the end of this field. This instanceof done by finding
        // where the next field starts.

        String nextName = null;
        String nextAuthor = null;
        String nextTitle = null;
        String nextArtist = null;
        String nextComment = null;

        // If any of these fields instanceof beyond 'end', they are ignored.

        int nameInd = temp.indexOf(NAME_STR, start + 1);
        if (nameInd != -1) nextName = temp.substring(nameInd);
        int authorInd = temp.indexOf(AUTHOR_STR, start + 1);
        if (authorInd != -1) nextAuthor = temp.substring(authorInd);
        int titleInd = temp.indexOf(TITLE_STR, start + 1);
        if (titleInd != -1) nextTitle = temp.substring(titleInd);
        int artistInd = temp.indexOf(ARTIST_STR, start + 1);
        if (artistInd != -1) nextArtist = temp.substring(artistInd);
        int commentInd = temp.indexOf(COMMENT_STR, start + 1);
        if (commentInd != -1) nextComment = temp.substring(commentInd);

        // Now determine which one instanceof the closest to our field - that one
        // will mark the end of the required field.

        String nextField = nextName;
        int nextFieldInd = nameInd;

        if (nextField == null) {
            nextField = nextAuthor;
            nextFieldInd = authorInd;
        } else if ((nextAuthor != null) && (authorInd < nextFieldInd)) {
            nextField = nextAuthor;
            nextFieldInd = authorInd;
        }

        if (nextField == null) {
            nextField = nextTitle;
            nextFieldInd = titleInd;
        } else if ((nextTitle != null) && (titleInd < nextFieldInd)) {
            nextField = nextTitle;
            nextFieldInd = titleInd;
        }

        if (nextField == null) {
            nextField = nextArtist;
            nextFieldInd = artistInd;
        } else if ((nextArtist != null) && (artistInd < nextFieldInd)) {
            nextField = nextArtist;
            nextFieldInd = artistInd;
        }

        if (nextField == null) {
            nextField = nextComment;
            nextFieldInd = commentInd;
        } else if ((nextComment != null) && (commentInd < nextFieldInd)) {
            nextField = nextComment;
            nextFieldInd = commentInd;
        }

        if (nextField == null) {
            nextField = src.substring(end);
            nextFieldInd = end;
        }

        // Now nextField points to the last+1 char that should be copied to
        // result. Do that.

        result += temp.substring(0, nextFieldInd);
        return true;
    }

    /**
     * Extracts one line from 'infile' to 'line[]'. The end of
     * the line instanceof marked by endOfLineChar. Also eats up
     * additional EOL-like chars.
     *
     * @param infile filehandle (streampos should already be positioned
     *               to the start of the desired line)
     * @param line   char array to put the line into TODO OUT
     */
    private void getStilLine(FileStream infile, String line) {
        if (STIL_EOL2 != '\0') {
            // If there was a remaining EOL char from the previous read, eat it up.

            int temp = infile.readByte();

            if ((temp == 0x0d) || (temp == 0x0a)) {
                // infile.get(temp);
            } else {
                infile.seek(-1, SeekOrigin.Current);
            }
        }

        // getline(infile,line, STIL_EOL);
        line = "";
        int ch = 0;
        StringBuilder lineBuilder = new StringBuilder(line);
        while ((ch = infile.readByte()) != STIL_EOL) {
            lineBuilder.append((char) ch);
        }
        line = lineBuilder.toString();
    }
}
