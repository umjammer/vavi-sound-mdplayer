/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.sound.sampled.AudioFormat;

import mdplayer.format.BaseFileFormat;

import static java.lang.System.getLogger;


/**
 * Encodings used by the MDPlayer audio decoder.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 260330 nsano initial version <br>
 */
public class MdEncoding extends AudioFormat.Encoding {

    private static final Logger logger = getLogger(MdEncoding.class.getName());

    private final String extensions;

    /**
     * Constructs a new encoding.
     *
     * @param name Name of the MDPlayer audio encoding.
     */
    public MdEncoding(String name, String extensions) {
        super(name);
        this.extensions = extensions;
    }

    public String getExtensions() {
        return extensions;
    }

    static final List<MdEncoding> encodings = new ArrayList<>();

    static {
        for (var fileFormat : ServiceLoader.load(BaseFileFormat.class)) {
            if (fileFormat.getEncoding() != null) {
                encodings.add((MdEncoding) fileFormat.getEncoding());
            }
        }
    }

    public static MdEncoding valueOf(String name) {
logger.log(Level.DEBUG, name);
        return encodings.stream().filter(e -> name.equalsIgnoreCase(e.toString())).findFirst().orElseThrow();
    }
}
