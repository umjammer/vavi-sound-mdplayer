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
import java.util.ArrayList;
import java.util.List;


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
        int p = path.getFileName().toString().lastIndexOf('.');
        String base = path.getFileName().toString().substring(0, p);
        String ext0 = path.getFileName().toString().substring(p).toLowerCase();
        Path parent = path.getParent();

        List<String> trials = new ArrayList<>();

        // File.ext
        trials.add(base + ext0);
        // File.EXT
        trials.add(base + ext0.toUpperCase());
        // file.ext
        trials.add(base.toLowerCase() + ext0);
        // file.EXT
        trials.add(base.toLowerCase() + ext0.toUpperCase());
        // FILE.ext
        trials.add(base.toUpperCase() + ext0);
        // FILE.EXT
        trials.add(base.toUpperCase() + ext0.toUpperCase());

        Path r = trials.stream()
                .map(filename -> parent != null ? parent.resolve(filename) : Path.of(filename))
                .filter(Files::exists)
                .findFirst()
                .orElse(null);

if (r == null) {
 logger.log(Level.WARNING, path + " not found in " + trials, new Exception(path + " not found in " + trials));
}
        return r;
    }
}
