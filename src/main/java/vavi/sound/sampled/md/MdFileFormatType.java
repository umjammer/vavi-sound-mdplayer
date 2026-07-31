/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.sound.sampled.AudioFileFormat;

import mdplayer.driver.BaseFileFormat;


/**
 * FileFormatTypes used by the MDPlayer audio decoder.
 *
 * TODO sub type for each
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 260330 nsano initial version <br>
 */
public class MdFileFormatType extends AudioFileFormat.Type {

    /**
     * Constructs a file type.
     *
     * @param name      the name of the MDPlayer audio File Format.
     * @param extension the file extension for this MDPlayer audio File Format.
     */
    public MdFileFormatType(String name, String extension) {
        super(name, extension);
    }

    private static final List<MdFileFormatType> types = new ArrayList<>();

    static {
        for (var fileFormat : ServiceLoader.load(BaseFileFormat.class)) {
            if (fileFormat.getType() != null) {
                types.add((MdFileFormatType) fileFormat.getType());
            }
        }
    }

    public static MdFileFormatType valueOf(String name) {
        return types.stream().filter(t -> name.equalsIgnoreCase(t.toString())).findFirst().orElseThrow();
    }
}
