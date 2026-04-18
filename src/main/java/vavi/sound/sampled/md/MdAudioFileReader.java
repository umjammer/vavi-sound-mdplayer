/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.format.UnknownFileFormat;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.util.archive.Archives;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.getLogger;
import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;


/**
 * Provider for mdplayer audio file reading services. This implementation can parse
 * the format information from emulator audio file, and can produce audio input
 * streams from files of this type.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 260330 nsano initial version <br>
 */
public class MdAudioFileReader extends AudioFileReader {

    private static final Logger logger = getLogger(MdAudioFileReader.class.getName());

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()), 8192)) {
            return getAudioFileFormat(inputStream, Math.toIntExact(file.length()));
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        try (InputStream inputStream = new BufferedInputStream(url.openStream(), 8192)) {
            return getAudioFileFormat(inputStream, NOT_SPECIFIED);
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(stream, NOT_SPECIFIED);
    }

    /**
     * Return the AudioFileFormat from the given InputStream. Implementation.
     *
     * @param bitStream input to decode, mark must be supported and required buffer size ({@code 8192})
     * @param mediaLength unused
     * @return an AudioInputStream object based on the audio file data contained
     * in the input stream.
     * @throws UnsupportedAudioFileException if the File does not point to a
     *                                       valid audio file data recognized by the system.
     * @throws IOException                   if an I/O exception occurs.
     */
    protected static AudioFileFormat getAudioFileFormat(InputStream bitStream, int mediaLength) throws UnsupportedAudioFileException, IOException {
logger.log(DEBUG, "enter: available: " + bitStream.available() + ", " + bitStream);
        if (!bitStream.markSupported()) {
            throw new IllegalArgumentException("input stream not supported mark");
        }
        BasePlugin<? extends BaseDriver> plugin;
        Encoding encoding;
        float samplingRate = 44100;
        int channels = 2;
        AudioFileFormat.Type type;
        MetaData metaData;
        try {
            bitStream.mark(10); // *1
            InputStream in = Archives.getInputStream(bitStream);
logger.log(Level.TRACE, "input stream M: " + in + ", " + in.available());
            if (!in.markSupported()) {
                in = new BufferedInputStream(in, 20 * 1024 * 1024);
            }

            FileFormat fileFormat = FileFormat.getFileFormat(in);
logger.log(DEBUG, "format: " + fileFormat.getClass().getSimpleName());
            if (fileFormat instanceof UnknownFileFormat) throw new UnsupportedAudioFileException("not supported format");

            URI source = SoundUtil.getSource(bitStream);
            String fn = source != null && source.getScheme().equals("file") ? source.getPath() : null;
            encoding = fileFormat.getEncoding();
            type = fileFormat.getType();
            var r = fileFormat.load(in, fn);
            metaData = fileFormat.getMetaData(r.getItem1());
            plugin = (BasePlugin<? extends BaseDriver>) fileFormat.getPlugin();
logger.log(DEBUG, "plugin: " + plugin);
logger.log(DEBUG, "filename: " + fn);
            plugin.setBuffer(fileFormat, r.getItem1(), fn, null, 0, 0, r.getItem2());

        } catch (IllegalArgumentException | NoSuchElementException e) {
            bitStream.reset(); // *1
logger.log(DEBUG, "error exit: available: " + bitStream.available() + ", " + bitStream);
logger.log(TRACE, e.getMessage(), e);
            throw (UnsupportedAudioFileException) new UnsupportedAudioFileException().initCause(e);
        }
        Map<String, Object> props = new HashMap<>();
        props.put("vavi.sound.sampled.md", plugin);
        fillProps(props, metaData);
        AudioFormat format = new AudioFormat(encoding, samplingRate, NOT_SPECIFIED, channels, NOT_SPECIFIED, NOT_SPECIFIED, false, props);
        return new AudioFileFormat(type, format, NOT_SPECIFIED);
    }

    private static void fillProps(Map<String, Object> props, MetaData metaData) {
        if (metaData != null) {
            props.put("md.title", metaData.getFirst(Tag.Title));
            props.put("md.artist", metaData.getFirst(Tag.Maker));
            props.put("md.composer", metaData.getFirst(Tag.Composer));
            props.put("md.album", metaData.getFirst(Tag.GameTitle));
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()), 8192);
        try {
            return getAudioInputStream(inputStream, (int) file.length());
        } catch (UnsupportedAudioFileException | IOException e) {
            inputStream.close();
            throw e;
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
        try {
            return getAudioInputStream(inputStream, NOT_SPECIFIED);
        } catch (UnsupportedAudioFileException | IOException e) {
            inputStream.close();
            throw e;
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(stream, NOT_SPECIFIED);
    }

    /**
     * Obtains an audio input stream from the input stream provided. The stream
     * must point to valid audio file data.
     *
     * @param inputStream the input stream from which the AudioInputStream
     *                    should be constructed.
     * @param mediaLength unused
     * @return an AudioInputStream object based on the audio file data contained
     * in the input stream.
     * @throws UnsupportedAudioFileException if the File does not point to a
     *                                       valid audio file data recognized by the system.
     * @throws IOException                   if an I/O exception occurs.
     */
    protected static AudioInputStream getAudioInputStream(InputStream inputStream, int mediaLength) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat audioFileFormat = getAudioFileFormat(inputStream, mediaLength);
        return new AudioInputStream(inputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
    }
}
