/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.utils.STILview;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.Tuple;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.SeekOrigin;
import mdplayer.driver.sid.Ptr;


/**
 * STIL class
 * <p>
 * @author LaLa <LaLa@C64.org>
 * @copyright 1998, 2002 by LaLa
 * <p>
 * <p>
 * Given the location of HVSC this class can extract STIL information for a
 * given tune of a given SID file. (Sounds simple, huh?)
 * <p>
 * PLEASE, READ THE ACCOMPANYING README.TXT FILE BEFORE PROCEEDING!!!!
 */
public class STIL {

    //Enum to use for asking for specific fields.
    public enum STILField {
        all,
        name,
        author,
        title,
        artist,
        comment
    }

    //Enum that describes the possible errors this class may encounter.
    public enum STILerror {
        NO_STIL_ERROR(0),
        BUG_OPEN(1),           ///< INFO ONLY: failed to open BUGlist.txt.
        WRONG_DIR(2),          ///< INFO ONLY: path was not within HVSC base dir.
        NOT_IN_STIL(3),        ///< INFO ONLY: requested entry was not found : STIL.txt.
        NOT_IN_BUG(4),         ///< INFO ONLY: requested entry was not found : BUGlist.txt.
        WRONG_ENTRY(5),        ///< INFO ONLY: section-global comment was asked for with get*Entry().
        CRITICAL_STIL_ERROR(10),
        BASE_DIR_LENGTH(11),    ///< The length of the HVSC base dir was wrong (empty String?)
        STIL_OPEN(12),          ///< Failed to open STIL.txt.
        NO_EOL(13),             ///< Failed to determine EOL char(s).
        NO_STIL_DIRS(14),       ///< Failed to get sections (subdirs) when parsing STIL.txt.
        NO_BUG_DIRS(15);         ///< Failed to get sections (subdirs) when parsing BUGlist.txt.
        int v;

        STILerror(int v) {
            this.v = v;
        }
    }

    //To turn debug output on
    public Boolean STIL_DEBUG;

    //----//

    /**
     //Allocates necessary memory.
     *
     //@param stilPath relative path to STIL file
     //@param bugsPath relative path to BUG file
     */
    //public STIL(String stilPath = stildefs.DEFAULT_PATH_TO_STIL, String bugsPath = stildefs.DEFAULT_PATH_TO_BUGLIST) { }

    /**
     //Returns a formatted String telling what the version
     //number instanceof for the STIL class and other info.
     //If it instanceof called after setBaseDir(), the String also
     //has the STIL.txt file's version number : it.
     *
     //@return
     //    printable formatted String with version and copyright
     //    info
     //    (It's kinda dangerous to return a pointer that points
     //    to an private structure, but I trust you. :)
     */
    //public String getVersion() { return null; }

    /**
     //Returns a floating number telling what the version
     //number instanceof of this STIL class.
     *
     //@return
     //    version number
     */
    //public float getVersionNo() { return 0; }

    /**
     //Tell the Object where the HVSC base directory instanceof - it
     //figures that the STIL should be : /DOCUMENTS/STIL.txt
     //and that the BUGlist should be : /DOCUMENTS/BUGlist.txt.
     //It should not matter whether the path instanceof given : UNIX,
     //WinDOS, or Mac format (ie. '\' vs. '/' vs. ':')
     *
     //@param  pathToHVSC = HVSC base directory : your machine's format
     //@return
     //     - false - Problem opening or parsing STIL/BUGlist
     //     - true  - All okay
     */
    //public Boolean setBaseDir(String pathToHVSC) { return false; }

    /**
     //Returns a floating number telling what the version
     //number instanceof of the STIL.txt file.
     //To be called only after setBaseDir()!
     *
     //@return
     //    version number (0.0 if setBaseDir() was not called, yet)
     */
    //public float getSTILVersionNo() { return 0; }

    /**
     //Given an HVSC pathname, a tune number and a
     //field designation, it returns a formatted String that
     //contains the STIL field for the tune number (if exists).
     //If it doesn't exist, returns a NULL.
     *
     //@param relPathToEntry = relative to the HVSC base dir, starting with
     //                        a slash
     //@param tuneNo         = song number within the song (default=0).
     //@param field          = which field to retrieve (default=all).
     *
     //What the possible combinations of tuneNo and field represent:
     *
     //- tuneNo = 0, field = all : all of the STIL entry instanceof returned.
     //- tuneNo = 0, field = comment : the file-global comment instanceof returned.
     //  (For single-tune entries, this returns nothing!)
     //- tuneNo = 0, field = <other> : INVALID! (NULL instanceof returned)
     //- tuneNo != 0, field = all : all fields of the STIL entry for the
     //  given tune number are returned. (For single-tune entries, this is
     //  equivalent to saying tuneNo = 0, field = all.)
     //  However, the file-global comment instanceof *NOT* returned with it any
     //  more! (Unlike : versions before v2.00.) It led to confusions:
     //  eg. when a comment was asked for tune // #3, it returned the
     //  file-global comment even if there was no specific entry for tune // #3!
     //- tuneNo != 0, field = <other> : the specific field of the specific
     //  tune number instanceof returned. If the tune number doesn't exist (eg. if
     //  tuneNo=2 for single-tune entries, or if tuneNo=2 when there's no
     //  STIL entry for tune // #2 : a multitune entry), returns NULL.
     *
     //NOTE: For older versions of STIL (older than v2.59) the tuneNo and
     //field parameters are ignored and are assumed to be tuneNo=0 and
     //field=all to maintain backwards compatibility.
     *
     //@return
     //     - pointer to a printable formatted String containing
     //       the STIL entry
     //       (It's kinda dangerous to return a pointer that points
     //       to an private structure, but I trust you. :)
     //     - NULL if there's absolutely no STIL entry for the tune
     */
    //public String getEntry(String relPathToEntry, int tuneNo = 0, STILField field = STILField.all) { return null; }

    /**
     //Same as // #getEntry, but with an absolute path given
     //given : your machine's format.
     */
    //public String getAbsEntry(String absPathToEntry, int tuneNo = 0, STILField field = STILField.all) { return null; }

    /**
     //Given an HVSC pathname and tune number it returns a
     //formatted String that contains the section-global
     //comment for the tune number (if it exists). If it
     //doesn't exist, returns a NULL.
     *
     //@param relPathToEntry = relative to the HVSC base dir starting with
     //                      a slash
     //@return
     //     - pointer to a printable formatted String containing
     //       the section-global comment
     //       (It's kinda dangerous to return a pointer that points
     //       to an private structure, but I trust you. :)
     //     - NULL if there's absolutely no section-global comment
     //       for the tune
     */
    //public String getGlobalComment(String relPathToEntry) { return null; }

    /**
     //Same as // #getGlobalComment, but with an absolute path
     //given : your machine's format.
     */
    //public String getAbsGlobalComment(String absPathToEntry) { return null; }

    /**
     //Given an HVSC pathname and tune number it returns a
     //formatted String that contains the BUG entry for the
     //tune number (if exists). If it doesn't exist, returns
     //a NULL.
     *
     //@param relPathToEntry = relative to the HVSC base dir starting with
     //                        a slash
     //@param tuneNo         = song number within the song (default=0)
     //                        If tuneNo=0, returns all of the BUG entry.
     *
     //     NOTE: For older versions of STIL (older than v2.59) tuneNo is
     //     ignored and instanceof assumed to be 0 to maintain backwards
     //     compatibility.
     *
     //@return
     //     - pointer to a printable formatted String containing
     //       the BUG entry
     //       (It's kinda dangerous to return a pointer that points
     //       to an private structure, but I trust you. :)
     //     - NULL if there's absolutely no BUG entry for the tune
     */
    //public String getBug(String relPathToEntry, int tuneNo = 0) { return null; }

    /**
     //Same as // #getBug, but with an absolute path
     //given : your machine's format.
     */
    //public String getAbsBug(String absPathToEntry, int tuneNo = 0) { return null; }

    /**
     * //Returns a specific error number identifying the problem
     * //that happened at the last invoked public method.
     * <p>
     * //@return
     * //     STILerror - an enumerated error value
     */
    public STILerror getError() {
        return (lastError);
    }

    /**
     * //Returns true if the last error encountered was critical
     * //(ie. not one that the STIL class can recover from).
     * <p>
     * //@return
     * //     true if the last error encountered was critical
     */
    public Boolean hasCriticalError() {
        return ((lastError.v >= STILerror.CRITICAL_STIL_ERROR.v) ? true : false);
    }

    /**
     * //Returns an ASCII error String containing the
     * //description of the error that happened at the last
     * //invoked public method.
     * <p>
     * //@return
     * //     pointer to String with the error description
     */
    public String getErrorStr() {
        return STIL_ERROR_STR[(int) lastError.v];
    }

    //typedef std::map<std::String, std::streampos> dirList;

    //Path to STIL.
    private String PATH_TO_STIL;

    //Path to BUGlist.
    private String PATH_TO_BUGLIST;

    //Version number/copyright String
    private String versionString;

    //STIL.txt's version number
    private float STILVersion;

    //Base dir
    private String baseDir;

    //Maps of sections (subdirs) for easier positioning.
    //@{
    //dirList stilDirs;
    //dirList bugDirs;
    private List<Tuple<String, Integer>> stilDirs;
    private List<Tuple<String, Integer>> bugDirs;
    //@}

    /**
     * This tells us what the line delimiter instanceof : STIL.txt.
     * (It may be two chars!)
     */
    private byte STIL_EOL;
    private byte STIL_EOL2;

    //Error number of the last error that happened.
    private STILerror lastError;

    //Error Strings containing the description of the possible errors : STIL.
    //private String[] STIL_ERROR_STR;

    ////////////////

    //The last retrieved entry
    private String entrybuf;

    //The last retrieved section-global comment
    private String globalbuf;

    //The last retrieved BUGentry
    private String bugbuf;

    //Buffers to hold the resulting Strings
    private String resultEntry = null;
    private String resultBug = null;

    ////////////////

    //private void setVersionString() { }

    /**
     //Determines what the EOL char instanceof (or are) from STIL.txt.
     //It instanceof assumed that BUGlist.txt will use the same EOL.
     *
     //@return
     //     - false - something went wrong
     //     - true  - everything instanceof okay
     */
    //private Boolean determineEOL(FileStream stilFile) { return false; }

    /**
     //Populates the given dirList array with the directories
     //obtained from 'inFile' for faster positioning within
     //'inFile'.
     *
     //@param inFile - where to read the directories from
     //@param dirs   - the dirList array that should be populated with the
     //                directory list
     //@param isSTILFile - instanceof this the STIL or the BUGlist we are parsing
     //@return
     //     - false - No entries were found or otherwise failed to process
     //               inFile
     //     - true  - everything instanceof okay
     */
    //private Boolean getDirs(FileStream inFile, List<Tuple<String, int>> dirs, Boolean isSTILFile) { return false; }

    /**
     //Positions the file pointer to the given entry : 'inFile'
     //using the 'dirs' dirList for faster positioning.
     *
     //@param entryStr - the entry to position to
     //@param inFile   - position the file pointer : this file
     //@param dirs     - the list of dirs : inFile for easier positioning
     //@return
     //     - true - if successful
     //     - false - otherwise
     */
    //private Boolean positionToEntry(String entryStr, FileStream inFile, List<Tuple<String, int>> dirs) { return false; }

    /**
     //Reads the entry from 'inFile' into 'buffer'. 'inFile' should
     //already be positioned to the entry to be read.
     *
     //@param inFile   - filehandle of file to read from
     //@param entryStr - the entry needed to be read
     //@param buffer   - where to put the result to
     */
    //private void readEntry(FileStream inFile, String buffer) { }

    /**
     //Given a STIL formatted entry : 'buffer', a tune number,
     //and a field designation, it returns the requested
     //STIL field into 'result'.
     //If field=all, it also puts the file-global comment (if it exists)
     //as the first field into 'result'.
     *
     //@param result - where to put the resulting String to (if any)
     //@param buffer - pointer to the first char of what to search for
     //                the field. Should be a buffer : standard STIL
     //                format.
     //@param tuneNo - song number within the song (default=0)
     //@param field  - which field to retrieve (default=all).
     //@return
     //     - false - if nothing was put into 'result'
     //     - true  - 'result' has the resulting field
     */
    //private Boolean getField(String result, String buffer, int tuneNo = 0, STILField field = STILField.all) { return false; }

    /**
     //@param result - where to put the resulting String to (if any)
     //@param start  - pointer to the first char of what to search for
     //                the field. Should be a buffer : standard STIL
     //                format.
     //@param end    - pointer to the last+1 char of what to search for
     //                the field. ('end-1' should be a '\n'!)
     //@param field  - which specific field to retrieve
     //@return
     //     - false - if nothing was put into 'result'
     //     - true  - 'result' has the resulting field
     */
    //private Boolean getOneField(String result, String start, String end, STILField field) { return false; }

    /**
     * //Extracts one line from 'infile' to 'line[]'. The end of
     * //the line instanceof marked by endOfLineChar. Also eats up
     * //additional EOL-like chars.
     * <p>
     * //@param infile - filehandle (streampos should already be positioned
     * //                to the start of the desired line)
     * //@param line   - char array to put the line into
     */
    //private void getStilLine(FileStream infile, String line) { }






    /*
     * This file instanceof part of libsidplayfp, a SID player engine.
     *
     * Copyright 1998, 2002 by LaLa <LaLa@C64.org>
     * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
     *
     * This program instanceof free software; you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation; either version 2 of the License, or
     * (at your option) any later version.
     *
     * This program instanceof distributed : the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License
     * along with this program; if not, write to the Free Software
     * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
     */

    //
    // STIL class - Implementation file
    //
    // AUTHOR: LaLa
    // Email : LaLa@C64.org
    // Copyright (C) 1998, 2002 by LaLa
    //

    //#include "stil.h"
    //#include <cstdlib>
    //#include <cString>
    //#include <cstdio>      // For snprintf() and NULL
    //#include <iostream>
    //#include <iomanip>
    //#include <fstream>
    //#include <sstream>
    //#include <utility>
    //#include "Stringutils.h"

    //final ios_base::openmode STILopenFlags = ios::in | ios::binary;
    public FileMode STILopenFlags = FileMode.Open; //| ios::binary;

    public final float VERSION_NO = 3.0f;

    public void CERR_STIL_DEBUG(String str) {
        if (STIL_DEBUG) {
            //cerr << "Line // #" << __LINE__ << " STIL::"
            System.err.printf("Line // #" + str);
        }
    }

    // These are the hardcoded STIL/BUG field names.
    private String _NAME_STR = "   NAME: ";
    private String _AUTHOR_STR = " AUTHOR: ";
    private String _TITLE_STR = "  TITLE: ";
    private String _ARTIST_STR = " ARTIST: ";
    private String _COMMENT_STR = "COMMENT: ";
    //final char     _BUG_STR[] = "BUG: ";

    private String[] STIL_ERROR_STR = new String[] {
            "No error.",
            "Failed to open BUGlist.txt.",
            "Base dir path instanceof not the HVSC base dir path.",
            "The entry was not found : STIL.txt.",
            "The entry was not found : BUGlist.txt.",
            "A section-global comment was asked for : the wrong way.",
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
     * //Converts slashes to the one the OS uses to access files.
     * <p>
     * //@param
     * //     str - what to convert
     */
    private void convertSlashes(String str) {
        //std::replace(str.begin(), str.end(), '/', SLASH);
        str = str.replace('/', stildefs.SLASH);
    }

    /**
     * //Converts OS specific dir separators to slashes.
     * <p>
     * //@param
     * //     str - what to convert
     */
    private void convertToSlashes(String str) {
        //std::replace(str.begin(), str.end(), SLASH, '/');
        str = str.replace(stildefs.SLASH, '/');
    }


    // CONSTRUCTOR
    public STIL(String stilPath /*= stildefs.DEFAULT_PATH_TO_STIL*/, String bugsPath/* = stildefs.DEFAULT_PATH_TO_BUGLIST*/) {
        STIL_DEBUG = false;
        PATH_TO_STIL = stilPath;
        PATH_TO_BUGLIST = bugsPath;
        STILVersion = 0.0f;
        STIL_EOL = (byte) '\n';
        STIL_EOL2 = 0;
        lastError = STILerror.NO_STIL_ERROR;
        setVersionString();
    }

    private void setVersionString() {
        String ss = String.format(
                "STILView v{0:F2}\n"
                        + "\tCopyright (C) 1998, 2002 by LaLa (LaLa@C64.org)\n"
                        + "\tCopyright (C) 2012-2015 by Leandro Nini <drfiemost@users.sourceforge.net>\n"
                , VERSION_NO
        );
        //ss << fixed << setw(4) << setprecision(2);
        //ss << "STILView v" << VERSION_NO << endl;
        //ss << "\tCopyright (C) 1998, 2002 by LaLa (LaLa@C64.org)" << endl;
        //ss << "\tCopyright (C) 2012-2015 by Leandro Nini <drfiemost@users.sourceforge.net>" << endl;
        versionString = ss;
    }

    private String getVersion() {
        lastError = STILerror.NO_STIL_ERROR;
        return versionString;
    }

    public float getVersionNo() {
        lastError = STILerror.NO_STIL_ERROR;
        return VERSION_NO;
    }

    public float getSTILVersionNo() {
        lastError = STILerror.NO_STIL_ERROR;
        return STILVersion;
    }

    public Boolean setBaseDir(String pathToHVSC) {
        // Temporary placeholder for STIL.txt's version number.
        float tempSTILVersion = STILVersion;

        // Temporary placeholders for lists of sections.
        List<Tuple<String, Integer>> tempStilDirs = new ArrayList<Tuple<String, Integer>>();
        List<Tuple<String, Integer>> tempBugDirs = new ArrayList<Tuple<String, Integer>>();

        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("setBaseDir() called, pathToHVSC=" + pathToHVSC + "\n");

        String tempBaseDir = (pathToHVSC);

        // Sanity check the length.
        if (tempBaseDir == null || !tempBaseDir.isEmpty()) {
            CERR_STIL_DEBUG("setBaseDir() has problem with the size of pathToHVSC" + "\n");
            lastError = STILerror.BASE_DIR_LENGTH;
            return false;
        }

        // Chop the trailing slash
        char lastChar = tempBaseDir.charAt(tempBaseDir.length() - 1);

        if (lastChar == stildefs.SLASH) {
            tempBaseDir = tempBaseDir.substring(0, tempBaseDir.length() - 2);
        }

        // Attempt to open STIL

        // Create the full path+filename
        String tempName = tempBaseDir;
        tempName = tempName + PATH_TO_STIL;
        convertSlashes(tempName);

        //ifstream stilFile(tempName, STILopenFlags);
        FileStream stilFile;
        try {
            stilFile = new FileStream(tempName, STILopenFlags);
        } catch (Exception e)
        //if (stilFile.fail())
        {
            CERR_STIL_DEBUG("setBaseDir() open failed for " + tempName + "\n");
            lastError = STILerror.STIL_OPEN;
            return false;
        }

        CERR_STIL_DEBUG("setBaseDir(): open succeeded for " + tempName + "\n");

        // Attempt to open BUGlist

        // Create the full path+filename
        tempName = tempBaseDir;
        tempName = tempName + PATH_TO_BUGLIST;
        convertSlashes(tempName);

        //ifstream bugFile(tempName.c_str(), STILopenFlags);
        FileStream bugFile = null;
        try {
            bugFile = new FileStream(tempName, STILopenFlags);
            CERR_STIL_DEBUG("setBaseDir(): open succeeded for " + tempName + "\n");
        } catch (Exception e)
        //if (bugFile.fail())
        {
            // This instanceof not a critical error - some earlier versions of HVSC did
            // not have a BUGlist.txt file at all.

            if (bugFile != null) bugFile.close();
            CERR_STIL_DEBUG("setBaseDir() open failed for " + tempName + "\n");
            lastError = STILerror.BUG_OPEN;
            bugFile = null;
        }

        // Find  what the EOL really is
        if (determineEOL(stilFile) != true) {
            CERR_STIL_DEBUG("determinEOL() failed" + "\n");
            lastError = STILerror.NO_EOL;
            return false;
        }

        // Save away the current String so we can restore it if needed.
        String tempVersionString = versionString;

        setVersionString();

        // This instanceof necessary so the version number gets scanned : from the new
        // file, too.
        STILVersion = 0.0f;

        // These will populate the tempStilDirs and tempBugDirs maps (or not :)

        if (getDirs(stilFile, tempStilDirs, true) != true) {
            CERR_STIL_DEBUG("getDirs() failed for stilFile" + "\n");
            lastError = STILerror.NO_STIL_DIRS;

            // Clean up and restore things.
            STILVersion = tempSTILVersion;
            versionString = tempVersionString;
            return false;
        }

        if (bugFile != null) {
            if (getDirs(bugFile, tempBugDirs, false) != true) {
                // This instanceof not a critical error - it instanceof possible that the
                // BUGlist.txt file has no entries : it at all (in fact, that's
                // good!).

                CERR_STIL_DEBUG("getDirs() failed for bugFile" + "\n");
                lastError = STILerror.BUG_OPEN;
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

        CERR_STIL_DEBUG("setBaseDir() succeeded" + "\n");

        return true;
    }

    private Boolean StringCmp(String a, String b, int len) {
        if (a == null && b == null && len == 0) return true;
        if (a == null || b == null) return false;
        if (a.length() < len) return false;
        if (b.length() < len) return false;

        for (int i = 0; i < len; i++) {
            if (a.charAt(i) != b.charAt(i)) return false;
        }

        return true;
    }

    public String getAbsEntry(String absPathToEntry, int tuneNo/* = 0*/, STILField field/* = STILField.all*/) {
        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("getAbsEntry() called, absPathToEntry=" + absPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            CERR_STIL_DEBUG("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = STILerror.STIL_OPEN;
            return null;
        }

        // Determine if the baseDir instanceof : the given pathname.

        //if (!Stringutils::equal(absPathToEntry, baseDir.data(), baseDir.size()))
        if (!StringCmp(absPathToEntry, baseDir, baseDir.length())) {
            CERR_STIL_DEBUG("getAbsEntry() failed: baseDir=" + baseDir + ", absPath=" + absPathToEntry + "\n");
            lastError = STILerror.WRONG_DIR;
            return null;
        }


        String tempDir = absPathToEntry + baseDir.length();
        convertToSlashes(tempDir);

        return getEntry(tempDir, tuneNo, field);
    }

    public String getEntry(String relPathToEntry, int tuneNo/* = 0*/, STILField field /*= STILField.all*/) {
        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("getEntry() called, relPath=" + relPathToEntry + ", rest=" + tuneNo + "," + field + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            CERR_STIL_DEBUG("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = STILerror.STIL_OPEN;
            return null;
        }

        int relPathToEntryLen = relPathToEntry.length();

        // Fail if a section-global comment was asked for.

        if (relPathToEntry.charAt(relPathToEntryLen - 1) == '/') {
            CERR_STIL_DEBUG("getEntry() section-global comment was asked for - failed" + "\n");
            lastError = STILerror.WRONG_ENTRY;
            return null;
        }

        if (STILVersion < 2.59f) {
            // Older version of STIL instanceof detected.

            tuneNo = 0;
            field = STILField.all;
        }

        // Find  whether we have this entry : the buffer.

        //if ((!Stringutils::equal(entrybuf.data(), relPathToEntry, relPathToEntryLen))
        //|| ((entrybuf.find_first_of('\n') != relPathToEntryLen)
        //&& (STILVersion > 2.59f)))
        if ((!StringCmp(entrybuf, relPathToEntry, relPathToEntryLen))
                || ((entrybuf.indexOf('\n') != relPathToEntryLen)
                && (STILVersion > 2.59f))) {
            // The relative pathnames don't match or they're not the same length:
            // we don't have it : the buffer, so pull it in.

            CERR_STIL_DEBUG("getEntry(): entry not : buffer" + "\n");

            // Create the full path+filename
            String tempName = baseDir;
            tempName += PATH_TO_STIL;
            convertSlashes(tempName);

            //ifstream stilFile(tempName.c_str(), STILopenFlags);

            FileStream stilFile = null;
            try {
                stilFile = new FileStream(tempName, STILopenFlags);
            } catch (Exception e)
            //if (stilFile.fail())
            {
                if (stilFile != null) stilFile.close();
                CERR_STIL_DEBUG("getEntry() open failed for stilFile" + "\n");
                lastError = STILerror.STIL_OPEN;
                return null;
            }

            CERR_STIL_DEBUG("getEntry() open succeeded for stilFile" + "\n");

            if (positionToEntry(ByteBuffer.wrap(relPathToEntry.getBytes(StandardCharsets.US_ASCII), 0, relPathToEntry.getBytes(StandardCharsets.US_ASCII).length), stilFile, stilDirs) == false) {
                // Copy the entry's name to the buffer.
                entrybuf = relPathToEntry + "\n";
                CERR_STIL_DEBUG("getEntry() posToEntry() failed" + "\n");
                lastError = STILerror.NOT_IN_STIL;
            } else {
                entrybuf = "";
                readEntry(stilFile, entrybuf);
                CERR_STIL_DEBUG("getEntry() entry read" + "\n");
            }

            if (stilFile != null) stilFile.close();

        }

        // Put the requested field into the result String.
        return getField(resultEntry, entrybuf, tuneNo, field) ? resultEntry : null;
    }

    public String getAbsBug(String absPathToEntry, int tuneNo/* = 0*/) {
        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("getAbsBug() called, absPathToEntry=" + absPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            CERR_STIL_DEBUG("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = STILerror.BUG_OPEN;
            return null;
        }

        // Determine if the baseDir instanceof : the given pathname.

        if (!StringCmp(absPathToEntry, baseDir, baseDir.length())) {
            CERR_STIL_DEBUG("getAbsBug() failed: baseDir=" + baseDir + ", absPath=" + absPathToEntry + "\n");
            lastError = STILerror.WRONG_DIR;
            return null;
        }

        String tempDir = absPathToEntry + baseDir.length();
        convertToSlashes(tempDir);

        return getBug(tempDir, tuneNo);
    }

    public String getBug(String relPathToEntry, int tuneNo/* = 0*/) {
        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("getBug() called, relPath=" + relPathToEntry + ", rest=" + tuneNo + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            CERR_STIL_DEBUG("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = STILerror.BUG_OPEN;
            return null;
        }

        // Older version of STIL instanceof detected.

        if (STILVersion < 2.59f) {
            tuneNo = 0;
        }

        // Find  whether we have this bug entry : the buffer.
        // If the baseDir was changed, we'll have to read it : again,
        // even if it might be : the buffer already.

        int relPathToEntryLen = relPathToEntry.length();

        if ((!StringCmp(bugbuf, relPathToEntry, relPathToEntryLen)) ||
                ((bugbuf.indexOf('\n') != relPathToEntryLen) &&
                        (STILVersion > 2.59f))) {
            // The relative pathnames don't match or they're not the same length:
            // we don't have it : the buffer, so pull it in.

            CERR_STIL_DEBUG("getBug(): entry not : buffer" + "\n");

            // Create the full path+filename
            String tempName = baseDir;
            tempName += PATH_TO_BUGLIST;
            convertSlashes(tempName);

            //ifstream bugFile(tempName, STILopenFlags);
            FileStream bugFile = null;
            try {
                bugFile = new FileStream(tempName, STILopenFlags);
            } catch (Exception e)
            //if (bugFile.fail())
            {
                if (bugFile != null) bugFile.close();
                CERR_STIL_DEBUG("getBug() open failed for bugFile" + "\n");
                lastError = STILerror.BUG_OPEN;
                return null;
            }

            CERR_STIL_DEBUG("getBug() open succeeded for bugFile" + "\n");

            if (positionToEntry(ByteBuffer.wrap(relPathToEntry.getBytes(StandardCharsets.US_ASCII)), bugFile, bugDirs) == false) {
                // Copy the entry's name to the buffer.
                bugbuf = relPathToEntry + "\n";
                CERR_STIL_DEBUG("getBug() posToEntry() failed" + "\n");
                lastError = STILerror.NOT_IN_BUG;
            } else {
                bugbuf = "";
                readEntry(bugFile, bugbuf);
                CERR_STIL_DEBUG("getBug() entry read" + "\n");
            }
            if (bugFile != null) bugFile.close();
        }

        // Put the requested field into the result String.
        return getField(resultBug, bugbuf, tuneNo, STILField.all) ? resultBug : null;
    }

    public String getAbsGlobalComment(String absPathToEntry) {
        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("getAbsGC() called, absPathToEntry=" + absPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            CERR_STIL_DEBUG("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = STILerror.STIL_OPEN;
            return null;
        }

        // Determine if the baseDir instanceof : the given pathname.

        if (!StringCmp(absPathToEntry, baseDir, baseDir.length())) {
            CERR_STIL_DEBUG("getAbsGC() failed: baseDir=" + baseDir + ", absPath=" + absPathToEntry + "\n");
            lastError = STILerror.WRONG_DIR;
            return null;
        }

        String tempDir = absPathToEntry + baseDir.length();
        convertToSlashes(tempDir);

        return getGlobalComment(tempDir);
    }

    public String getGlobalComment(String relPathToEntry) {
        lastError = STILerror.NO_STIL_ERROR;

        CERR_STIL_DEBUG("getGC() called, relPath=" + relPathToEntry + "\n");

        if (baseDir == null || !baseDir.isEmpty()) {
            CERR_STIL_DEBUG("HVSC baseDir instanceof not yet set!" + "\n");
            lastError = STILerror.STIL_OPEN;
            return null;
        }

        // Save the dirpath.

        String lastSlash = relPathToEntry.substring(relPathToEntry.lastIndexOf('/'));

        if (lastSlash == null) {
            lastError = STILerror.WRONG_DIR;
            return null;
        }

        //int pathLen = lastSlash - relPathToEntry + 1;
        String dir = relPathToEntry.substring(0, relPathToEntry.lastIndexOf('/'));

        // Find  whether we have this global comment : the buffer.
        // If the baseDir was changed, we'll have to read it : again,
        // even if it might be : the buffer already.

        if ((!StringCmp(globalbuf, dir, dir.length())) ||
                ((globalbuf.indexOf('\n') != dir.length()) &&
                        (STILVersion > 2.59f))) {
            // The relative pathnames don't match or they're not the same length:
            // we don't have it : the buffer, so pull it in.

            CERR_STIL_DEBUG("getGC(): entry not : buffer" + "\n");

            // Create the full path+filename
            String tempName = baseDir;
            tempName += PATH_TO_STIL;
            convertSlashes(tempName);

            //ifstream stilFile(tempName.c_str(), STILopenFlags);
            FileStream stilFile;
            try {
                stilFile = new FileStream(tempName, STILopenFlags);
            } catch (Exception e)
            //if (stilFile.fail())
            {
                CERR_STIL_DEBUG("getGC() open failed for stilFile" + "\n");
                lastError = STILerror.STIL_OPEN;
                return null;
            }

            if (positionToEntry(ByteBuffer.wrap(dir.getBytes(StandardCharsets.US_ASCII), 0, dir.getBytes(StandardCharsets.US_ASCII).length), stilFile, stilDirs) == false) {
                // Copy the dirname to the buffer.
                globalbuf = dir + "\n";
                CERR_STIL_DEBUG("getGC() posToEntry() failed" + "\n");
                lastError = STILerror.NOT_IN_STIL;
            } else {
                globalbuf = "";
                readEntry(stilFile, globalbuf);
                CERR_STIL_DEBUG("getGC() entry read" + "\n");
            }

            stilFile.close();
        }

        CERR_STIL_DEBUG("getGC() globalbuf=" + globalbuf + "\n");
        CERR_STIL_DEBUG("-=END=-" + "\n");

        // Position pointer to the global comment field.

        int temp = globalbuf.indexOf('\n') + 1;

        // Check whether this instanceof a NULL entry or not.
        return (temp != globalbuf.length() || temp != 0) ? globalbuf + temp : null;
    }

    ///// * PRIVATE

    private Boolean determineEOL(FileStream stilFile) {
        CERR_STIL_DEBUG("detEOL() called" + "\n");

        if (stilFile == null) {
            CERR_STIL_DEBUG("detEOL() open failed" + "\n");
            return false;
        }

        stilFile.seek(0, SeekOrigin.Begin);

        STIL_EOL = 0;
        STIL_EOL2 = 0;

        // Determine what the EOL character is
        // (it can be different from OS to OS).
        //istream::sentry se(stilFile, true);
        //if (se)
        //{
        //streambuf* sb = stilFile.rdbuf();

        //int eof = char_traits < char >::eof();

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
        //}

        if (STIL_EOL == '\0') {
            // Something instanceof wrong - no EOL-like char was found.
            CERR_STIL_DEBUG("detEOL() no EOL found" + "\n");
            return false;
        }

        CERR_STIL_DEBUG("detEOL() EOL1=0x" + String.format("{0:x}", (int) STIL_EOL) + " EOL2=0x" + String.format("{0:x}", (int) STIL_EOL2) + "\n");

        return true;
    }

    private Boolean getDirs(FileStream inFile, List<Tuple<String, Integer>> dirs, Boolean isSTILFile) {
        Boolean newDir = !isSTILFile;

        CERR_STIL_DEBUG("getDirs() called" + "\n");

        inFile.seek(0, SeekOrigin.Begin);

        while (inFile != null) {
            String line = "";

            getStilLine(inFile, line);

            if (!isSTILFile) {
                CERR_STIL_DEBUG(line + '\n');
            }

            // Try to extract STIL's version number if it's not done, yet.

            if (isSTILFile && (STILVersion == 0.0f)) {
                if (StringCmp(line, "#  STIL v", 9)) {
                    // Get the version number
                    STILVersion = Float.parseFloat(line);// , 9);

                    // Put it into the String, too.
                    String ss;
                    //ss = fixed << setw(4) << setprecision(2);
                    ss = String.format("SID Tune Information List (STIL) v{0:f2}\n", STILVersion);
                    versionString += ss;

                    CERR_STIL_DEBUG("getDirs() STILVersion=" + STILVersion + "\n");

                    continue;
                }
            }

            // Search for the start of a dir separator first.

            if (isSTILFile && !newDir && StringCmp(line, "### ", 4)) {
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
                        if (c.Item1 == dirName) {
                            newDir = true;
                            break;
                        }
                    }
                }

                // Store the info
                if (newDir) {
                    int position = (int) (inFile.getPosition() - line.length() - 1L);

                    CERR_STIL_DEBUG("getDirs() dirName=" + dirName + ", pos=" + position + "\n");

                    dirs.add(new Tuple<String, Integer>(dirName, position));
                }

                newDir = !isSTILFile;
            }
        }

        if (dirs == null) {
            // No entries found - something instanceof wrong.
            // NOTE: It's perfectly valid to have a BUGlist.txt file with no
            // entries : it!
            CERR_STIL_DEBUG("getDirs() no dirs found" + "\n");
            return false;
        }

        CERR_STIL_DEBUG("getDirs() successful" + "\n");

        return true;
    }

    private Boolean positionToEntry(ByteBuffer entryStr, FileStream inFile, List<Tuple<String, Integer>> dirs) {
        CERR_STIL_DEBUG("pos2Entry() called, entryStr=" + entryStr + "\n");

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
        Boolean globComm = (pathLen == entryStrLen);

        // Find it : the table.
        String entry = String.format(new String(entryStr.array()), pathLen);
        //dirList::iterator elem = dirs.find(entry);
        Tuple<String, Integer> elem = null;
        for (Tuple<String, Integer> t : dirs) {
            if (t.Item1 == entry) {
                elem = t;
                break;
            }
        }
        if (elem == null) {
            // The directory was not found.
            CERR_STIL_DEBUG("pos2Entry() did not find the dir" + "\n");
            return false;
        }

        // Jump to the first entry of this section.
        inFile.seek(elem.Item2, SeekOrigin.Begin);
        Boolean foundIt = false;

        // Now find the desired entry

        String line = null;

        do {
            getStilLine(inFile, line);

            if (inFile.getLength() == inFile.getPosition()) {
                break;
            }

            // Check if it instanceof the start of an entry

            if (line.charAt(0) == '/') {
                if (!StringCmp(elem.Item1, line, pathLen)) {
                    // We are outside the section - get  of the loop,
                    // which will fail the search.
                    break;
                }

                // Check whether we need to find a section-global comment or
                // a specific entry.

                if (globComm || (STILVersion > 2.59f)) {
                    foundIt = line == entryStr.toString();
                } else {
                    // To be compatible with older versions of STIL, which may have
                    // the tune designation on the first line of a STIL entry
                    // together with the pathname.
                    foundIt = StringCmp(line, entryStr.toString(), entryStrLen);
                }

                CERR_STIL_DEBUG("pos2Entry() line=" + line + "\n");
            }
        }
        while (!foundIt);

        if (foundIt) {
            // Reposition the file pointer back to the start of the entry.
            inFile.seek(inFile.getPosition() - line.length() - 1L, SeekOrigin.Begin);
            CERR_STIL_DEBUG("pos2Entry() entry found" + "\n");
            return true;
        } else {
            CERR_STIL_DEBUG("pos2Entry() entry not found" + "\n");
            return false;
        }
    }

    private void readEntry(FileStream inFile, String buffer) {
        String line = "";

        for (; ; ) {
            getStilLine(inFile, line);

            if (line.length() == 0)
                break;

            buffer += line;
            buffer += "\n";
        }
    }

    private Boolean getField(String result, String buffer, int tuneNo/* = 0*/, STILField field/* = STILField.all*/) {
        CERR_STIL_DEBUG("getField() called, buffer=" + buffer + ", rest=" + tuneNo + "," + field + "\n");

        // Clean  the result buffer first.
        result = "";

        // Position pointer to the first char beyond the file designation.

        ByteBuffer start = Ptr.strchr(ByteBuffer.wrap(buffer.getBytes(StandardCharsets.US_ASCII)), (byte) '\n');
        if (start != null) start.position(start.position() + 1);

        // Check whether this instanceof a NULL entry or not.

        if (start == null) {
            CERR_STIL_DEBUG("getField() null entry" + "\n");
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
            //-------------------//
            // SINGLE TUNE ENTRY //
            //-------------------//

            // Is the first thing : this STIL entry the COMMENT?

            ByteBuffer temp = Ptr.strstr(start, _COMMENT_STR);
            ByteBuffer temp2 = null;

            // Search for other potential fields beyond the COMMENT.
            if (temp.position() == start.position()) {
                temp2 = Ptr.strstr(start, _NAME_STR);

                if (temp2 == null) {
                    temp2 = Ptr.strstr(start, _AUTHOR_STR);

                    if (temp2 == null) {
                        temp2 = Ptr.strstr(start, _TITLE_STR);

                        if (temp2 == null) {
                            temp2 = Ptr.strstr(start, _ARTIST_STR);
                        }
                    }
                }
            }

            if (temp.position() == start.position()) {
                // Yes. So it's assumed to be a file-global comment.

                CERR_STIL_DEBUG("getField() single-tune entry, COMMENT only" + "\n");

                if ((tuneNo == 0) && ((field == STIL.STILField.all) || ((field == STIL.STILField.comment) && (temp2 == null)))) {
                    // Simply copy the stuff in.
                    result += start;
                    CERR_STIL_DEBUG("getField() copied to resultbuf" + "\n");
                    return true;
                } else if ((tuneNo == 0) && (field == STIL.STILField.comment)) {
                    // Copy just the comment.
                    result += new String(start.array()) + (temp2.position() - start.position());
                    CERR_STIL_DEBUG("getField() copied to just the COMMENT to resultbuf" + "\n");
                    return true;
                } else if ((tuneNo == 1) && (temp2 != null)) {
                    // A specific field was asked for.

                    CERR_STIL_DEBUG("getField() copying COMMENT to resultbuf" + "\n");
                    return getOneField(
                            result,
                            new String(temp2.array()) + temp2.capacity(),
                            temp2.position(),
                            temp2.capacity(),
                            field);
                } else {
                    // Anything else instanceof invalid as of v2.00.

                    CERR_STIL_DEBUG("getField() invalid parameter combo: single tune, tuneNo=" + tuneNo + ", field=" + field + "\n");
                    return false;
                }
            } else {
                // No. Handle it as a regular entry.

                CERR_STIL_DEBUG("getField() single-tune regular entry" + "\n");

                if ((field == STIL.STILField.all) && ((tuneNo == 0) || (tuneNo == 1))) {
                    // The complete entry was asked for. Simply copy the stuff in.
                    result += start;
                    CERR_STIL_DEBUG("getField() copied to resultbuf" + "\n");
                    return true;
                } else if (tuneNo == 1) {
                    // A specific field was asked for.

                    CERR_STIL_DEBUG("getField() copying COMMENT to resultbuf" + "\n");
                    return getOneField(
                            result,
                            new String(start.array()) + start.capacity(),
                            start.position(),
                            start.position() + start.capacity(),
                            field);
                } else {
                    // Anything else instanceof invalid as of v2.00.

                    CERR_STIL_DEBUG("getField() invalid parameter combo: single tune, tuneNo=" + tuneNo + ", field=" + field + "\n");
                    return false;
                }
            }
        } else {
            //-------------------//
            // MULTITUNE ENTRY
            //-------------------//

            CERR_STIL_DEBUG("getField() multitune entry" + "\n");

            // Was the complete entry asked for?

            if (tuneNo == 0) {
                switch (field) {
                case all:
                    // Yes. Simply copy the stuff in.
                    result += start;
                    CERR_STIL_DEBUG("getField() copied all to resultbuf" + "\n");
                    return true;

                case comment:
                    // Only the file-global comment field was asked for.

                    if (firstTuneNo != start) {
                        CERR_STIL_DEBUG("getField() copying file-global comment to resultbuf" + "\n");
                        return getOneField(
                                result,
                                new String(start.array()) + start.capacity(),
                                start.position(),
                                firstTuneNo.position(),
                                STIL.STILField.comment);
                    } else {
                        CERR_STIL_DEBUG("getField() no file-global comment" + "\n");
                        return false;
                    }

                    //break;

                default:
                    // If a specific field other than a comment is
                    // asked for tuneNo=0, this instanceof illegal.

                    CERR_STIL_DEBUG("getField() invalid parameter combo: multitune, tuneNo=" + tuneNo + ", field=" + field + "\n");
                    return false;
                }
            }

            byte[] tuneNoStr = new byte[8];

            // Search for the requested tune number.

            tuneNoStr = String.format("(#{0})", tuneNo).getBytes(StandardCharsets.US_ASCII);
            tuneNoStr[7] = (byte) '\0';
            ByteBuffer myTuneNo = Ptr.strstr(start, tuneNoStr.toString());

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

                CERR_STIL_DEBUG("getField() myTuneNo=" + myTuneNo + ", nextTuneNo=" + nextTuneNo + "\n");
                return getOneField(
                        result,
                        new String(myTuneNo.array()) + myTuneNo.capacity(),
                        myTuneNo.position(),
                        nextTuneNo.position(),
                        field);
            } else {
                CERR_STIL_DEBUG("getField() nothing found" + "\n");
                return false;
            }
        }
    }

    private Boolean getOneField(String result, String src, int start, int end, STILField field) {
        // Sanity checking

        if ((end < start) || (src.charAt(end - 1) != '\n')) {
            CERR_STIL_DEBUG("getOneField() illegal parameters" + "\n");
            return false;
        }

        CERR_STIL_DEBUG("getOneField() called, start=" + start + ", rest=" + field + "\n");

        String temp = null;
        int tempInd = 0;

        switch (field) {
        case all:
            result += src.substring(start, end - start);
            return true;

        case name:
            tempInd = src.indexOf(_NAME_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case author:
            tempInd = src.indexOf(_AUTHOR_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case title:
            tempInd = src.indexOf(_TITLE_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case artist:
            tempInd = src.indexOf(_ARTIST_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        case comment:
            tempInd = src.indexOf(_COMMENT_STR, start);
            if (tempInd != -1) temp = src.substring(tempInd);
            break;

        default:
            break;
        }

        // If the field was not found or it instanceof not : between 'start'
        // and 'end', it instanceof declared a failure.

        //if ((temp == null) || (temp.ptr < start.ptr) || (temp.ptr > end.ptr))
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

        int nameInd = temp.indexOf(_NAME_STR, start + 1);
        if (nameInd != -1) nextName = temp.substring(nameInd);
        int authorInd = temp.indexOf(_AUTHOR_STR, start + 1);
        if (authorInd != -1) nextAuthor = temp.substring(authorInd);
        int titleInd = temp.indexOf(_TITLE_STR, start + 1);
        if (titleInd != -1) nextTitle = temp.substring(titleInd);
        int artistInd = temp.indexOf(_ARTIST_STR, start + 1);
        if (artistInd != -1) nextArtist = temp.substring(artistInd);
        int commentInd = temp.indexOf(_COMMENT_STR, start + 1);
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

    private void getStilLine(FileStream infile, String line) {
        if (STIL_EOL2 != '\0') {
            // If there was a remaining EOL char from the previous read, eat it up.

            int temp = infile.readByte();

            if ((temp == 0x0d) || (temp == 0x0a)) {
                //infile.get(temp);
            } else {
                infile.seek(-1, SeekOrigin.Current);
            }
        }

        //getline(infile,line, STIL_EOL);
        line = "";
        int ch = 0;
        while ((ch = infile.readByte()) != STIL_EOL) {
            line += (char) ch;
        }
    }
}
