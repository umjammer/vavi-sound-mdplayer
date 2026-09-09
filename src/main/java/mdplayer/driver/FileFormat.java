package mdplayer.driver;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import vavi.util.compat.Tuple;
import mdplayer.PlayList;
import musicDriverInterface.MetaData;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


public interface FileFormat {

    Logger logger = System.getLogger(FileFormat.class.getName());

    FileFormat unknown = new UnknownFileFormat();

    String[] getExtensions();

    MetaData getMetaData();

    List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */);

    List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */);

    boolean isMml();

    String getCompiledFilename();

    byte[] getData();

    List<Tuple<String, byte[]>> getExtendFiles();

    // TODO move logic from form to here
    String[] getPresetMixerBalance();

    Plugin getPlugin();

    List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry /* = null */) throws IOException;

    /** for insert TODO index might not use */
    List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry /* = null */) throws IOException;

    interface SampledFileFormat {}

    interface StreamFileFormat {}

    /**
     * Loads audio file data w/ related files also.
     * @param filename sub filename (e.g. inside an archive)
     */
    void load(InputStream is, String filename) throws IOException;

    /** for SPI */
    boolean isSupported(InputStream is) throws IOException;

    /** for SPI */
    Encoding getEncoding();

    /** for SPI */
    Type getType();

    /**
     * Tells this format apart from another one that claims the same extension.
     * <p>
     * Extensions are not owned: ".mus" is PC98 MUAP's MML and it is also MSX MGSDRV's, and only
     * what is written inside says which. A format that shares an extension with another answers
     * this; one that does not, never has to.
     *
     * @param filename what the file is called, which is how a format that reads more than one
     *                 extension knows which of them it is being asked about
     * @param head     the first bytes of the file, empty when they could not be read - in which
     *                 case a format should not decline, since guessing from nothing is worse
     *                 than the extension it already matched
     * @return false to let the other format have it
     */
    default boolean accepts(String filename, byte[] head) {
        return true;
    }

    /** how much of a file the {@link #accepts} of the formats that share an extension get to see */
    int SNIFF_SIZE = 4096;

    /**
     * @return {@link UnknownFileFormat} when not found
     */
    static FileFormat getFileFormat(String filename) {
        assert filename != null : "specify file name";
        ServiceLoader<FileFormat> loader = ServiceLoader.load(FileFormat.class);
        List<FileFormat> candidates = new ArrayList<>();
        for (FileFormat fileFormat : loader) {
            if (fileFormat.getExtensions() != null) {
                if (Arrays.stream(fileFormat.getExtensions()).anyMatch(ex -> filename.toLowerCase().endsWith(ex))) {
                    candidates.add(fileFormat);
                }
            }
        }
        if (candidates.isEmpty()) {
            return unknown; // TODO check
        }
        if (candidates.size() == 1) {
            return candidates.getFirst();
        }
        // more than one format reads this extension, so the file itself has to break the tie.
        // the name may be an entry in an archive rather than a path, and then there is nothing
        // to read and the first candidate keeps it, as it did before there was a tie to break
        byte[] head = sniff(filename);
        return candidates.stream().filter(f -> f.accepts(filename, head)).findFirst().orElse(candidates.getFirst());
    }

    /** the first {@link #SNIFF_SIZE} bytes of the file, or empty when it is not one */
    private static byte[] sniff(String filename) {
        Path path;
        try {
            path = Path.of(filename);
        } catch (InvalidPathException e) {
            return new byte[0];
        }
        if (!Files.isRegularFile(path)) {
            return new byte[0];
        }
        try (InputStream is = Files.newInputStream(path)) {
            return is.readNBytes(SNIFF_SIZE);
        } catch (IOException e) {
logger.log(Level.DEBUG, "sniff: " + filename + ": " + e.getMessage());
            return new byte[0];
        }
    }

    /** for SPI */
    int getMarkSize();

    /**
     * for SPI
     * @return {@link UnknownFileFormat} when not found
     */
    static FileFormat getFileFormat(InputStream is) throws IOException {
        ServiceLoader<FileFormat> loader = ServiceLoader.load(FileFormat.class);
        for (FileFormat fileFormat : loader) {
logger.log(Level.TRACE, "FORMAT: " + fileFormat.getClass().getName());
            try {
                is.mark(fileFormat.getMarkSize());
                if (fileFormat.isSupported(is)) {
                    return fileFormat;
                }
            } finally {
                is.reset();
logger.log(Level.TRACE, "input stream A: " + is + ", " + is.available());
            }
        }
        return unknown; // TODO check
    }
}
