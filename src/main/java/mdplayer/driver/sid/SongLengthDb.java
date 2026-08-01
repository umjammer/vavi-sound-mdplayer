/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.sid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.lib.sid.libsidplayfp.utils.SidDatabase;

import static java.lang.System.getLogger;


/**
 * The songlength database, the one thing that knows how long a Sid tune plays for.
 * <p>
 * A tune has no end of its own — its play routine is called forever — so everything else the
 * driver does about ending one ({@link SidMdDriver} watching the register writes come around
 * again) is a guess. The collection ships {@code DOCUMENTS/Songlengths.md5}, where every tune in
 * it is listed by the md5 of its file with a time for each of its subtunes; where that answers,
 * nothing else needs to.
 * <p>
 * It is tens of thousands of entries, so it is parsed once and kept — songs are loaded one
 * after another out of the same collection — and re-read when the file or the path to it changes.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-01 nsano initial version <br>
 */
final class SongLengthDb {

    private static final Logger logger = getLogger(SongLengthDb.class.getName());

    private SongLengthDb() {
    }

    /** what {@link #database} was read from, and when that was last written */
    private static String path;
    private static long modified;
    private static SidDatabase database;

    /**
     * How long the tune's current subtune plays for.
     *
     * @param path the songlength database, empty or unset if the user has none
     * @param tune the tune, with its subtune selected
     * @return the length : milliseconds, -1 if the database has nothing to say about it
     */
    static synchronized int lengthMs(String path, SidTune tune) {
        SidDatabase database = get(path);
        if (database == null || tune == null) return -1;

        int length = database.lengthMs(tune);
        if (length < 0) {
logger.log(Level.DEBUG, "songlength: " + database.error());
        }
        return length;
    }

    /** The parsed database, read afresh if the file has changed under it. */
    private static SidDatabase get(String path) {
        if (path == null || path.isEmpty()) {
            database = null;
            SongLengthDb.path = null;
            return null;
        }

        try {
            Path file = Path.of(path);
            if (Files.isDirectory(file) || !Files.exists(file)) {
logger.log(Level.WARNING, "songlength database not found: " + path);
                database = null;
                SongLengthDb.path = null;
                return null;
            }

            long modified = Files.getLastModifiedTime(file).toMillis();
            if (database != null && path.equals(SongLengthDb.path) && modified == SongLengthDb.modified) {
                return database;
            }

            SidDatabase read = new SidDatabase();
            if (!read.open(path)) {
logger.log(Level.WARNING, read.error() + ": " + path);
                database = null;
                SongLengthDb.path = null;
                return null;
            }
logger.log(Level.INFO, "songlength database: " + path);

            database = read;
            SongLengthDb.path = path;
            SongLengthDb.modified = modified;
            return database;
        } catch (Exception e) {
logger.log(Level.WARNING, e.getMessage(), e);
            database = null;
            SongLengthDb.path = null;
            return null;
        }
    }
}
