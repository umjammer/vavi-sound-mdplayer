/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.lang.System.Logger;
import java.util.Arrays;
import javax.sound.sampled.AudioFileFormat;

import static java.lang.System.getLogger;


/**
 * FileFormatTypes used by the MDPlayer audio decoder.
 *
 * TODO sub type for each
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 260330 nsano initial version <br>
 */
public class MdFileFormatType extends AudioFileFormat.Type {

    private static final Logger logger = getLogger(MdFileFormatType.class.getName());

    /**
     * Specifies an MDPlayer audio file.
     */
    public static final MdFileFormatType VGM = new MdFileFormatType("VGM", "vgm,vgz");

    /**
     * Constructs a file type.
     *
     * @param name      the name of the MDPlayer audio File Format.
     * @param extension the file extension for this MDPlayer audio File Format.
     */
    private MdFileFormatType(String name, String extension) {
        super(name, extension);
    }

    private static final MdFileFormatType[] types = {VGM};

    public static MdFileFormatType valueOf(String name) {
        return Arrays.stream(types).filter(t -> name.equalsIgnoreCase(t.toString())).findFirst().orElseThrow();
    }
}
