package mdplayer.format;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import dotnet4j.util.compat.Tuple;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


public interface FileFormat {

    FileFormat unknown = null;  // TODO UnknownFileFormat

    String[] getExtensions();

    List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/);

    List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/);

    List<Tuple<String, byte[]>> getExtendFile(String fn, byte[] srcBuf, Archive archive/* = null*/, Entry entry/* = null*/);

    String[] getPresetMixerBalance();

    byte[] getAllBytes(String filename);

    Plugin getPlugin();

    List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry/* = null*/) throws IOException;

    /** for insert TODO index might not use */
    List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry/* = null*/) throws IOException;

    Tuple<byte[], List<Tuple<String, byte[]>>> load(String archive, String fn) throws IOException;

    interface SampledFileFormat {}

    default boolean isSampled() {
        return this instanceof SampledFileFormat;
    }

    static FileFormat getFileFormat(String filename) {
        ServiceLoader<FileFormat> loader = ServiceLoader.load(FileFormat.class);
        for (FileFormat e : loader) {
            if (e.getExtensions() != null) {
                if (Arrays.stream(e.getExtensions()).anyMatch(ex -> filename.toLowerCase().lastIndexOf(ex) != -1)) {
                    return e;
                }
            }
        }
        return unknown;
    }
}
