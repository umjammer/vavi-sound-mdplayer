/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.common;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Utils.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-14 nsano initial version <br>
 */
public class Utils {

    private static final Logger logger = System.getLogger(Utils.class.getName());

    /** */
    public static Path fileExistsIgnoreCase(Path path) {
logger.log(Level.TRACE, "check existence case insensitive: " + path);
        int p = path.getFileName().toString().lastIndexOf('.');
        String base = path.getFileName().toString().substring(0, p);
        String ext0 = path.getFileName().toString().substring(p).toLowerCase();
        Path parent = path.getParent();

        // File.ext
        String filename = base + ext0;
        Path trial = parent != null ? parent.resolve(filename) : Path.of(filename);
        if (Files.exists(trial)) {
            logger.log(Level.TRACE, "found file: " + trial);
            return trial;
        }

        // File.EXT
        filename = base + ext0.toUpperCase();
        trial = parent != null ? parent.resolve(filename) : Path.of(filename);
        if (Files.exists(trial)) {
            logger.log(Level.TRACE, "found file: " + trial);
            return trial;
        }

        // file.ext
        filename = base.toLowerCase() + ext0;
        trial = parent != null ? parent.resolve(filename) : Path.of(filename);
        if (Files.exists(trial)) {
            logger.log(Level.TRACE, "found file: " + trial);
            return trial;
        }

        // file.EXT
        filename = base.toLowerCase() + ext0.toUpperCase();
        trial = parent != null ? parent.resolve(filename) : Path.of(filename);
        if (Files.exists(trial)) {
            logger.log(Level.TRACE, "found file: " + trial);
            return trial;
        }

        // FILE.ext
        filename = base.toUpperCase() + ext0;
        trial = parent != null ? parent.resolve(filename) : Path.of(filename);
        if (Files.exists(trial)) {
            logger.log(Level.TRACE, "found file: " + trial);
            return trial;
        }

        // FILE.EXT
        filename = base.toUpperCase() + ext0.toUpperCase();
        trial = parent != null ? parent.resolve(filename) : Path.of(filename);
        if (Files.exists(trial)) {
            logger.log(Level.TRACE, "found file: " + trial);
            return trial;
        }

logger.log(Level.WARNING, path + " not found");
        return null;
    }
}
