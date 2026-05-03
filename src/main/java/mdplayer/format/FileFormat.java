package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import vavi.util.compat.Tuple;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
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

    byte[] getAllBytes(String filename);

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
     * @return {@link UnknownFileFormat} when not found
     */
    static FileFormat getFileFormat(String filename) {
        assert filename != null : "specify file name";
        ServiceLoader<FileFormat> loader = ServiceLoader.load(FileFormat.class);
        for (FileFormat fileFormat : loader) {
            if (fileFormat.getExtensions() != null) {
                if (Arrays.stream(fileFormat.getExtensions()).anyMatch(ex -> filename.toLowerCase().endsWith(ex))) {
                    return fileFormat;
                }
            }
        }
        return unknown; // TODO check
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
