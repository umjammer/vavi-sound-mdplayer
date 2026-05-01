package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import mdplayer.PlayList;
import mdplayer.format.FileFormat.StreamFileFormat;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.SampledPlugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * AIFFFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class AIFFFileFormat extends BaseFileFormat implements FileFormat.SampledFileFormat, StreamFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".aiff"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        music.title = "(%s)".formatted(Path.of(file).getFileName().toString());

        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public byte[] getAllBytes(String filename) {
        return new byte[] {(byte) 'A', (byte) 'I', (byte) 'F', (byte) 'F'};
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(SampledPlugin.class);
    }

    private static final byte[] magic = {0x46, 0x4F, 0x52, 0x4D};

    @Override
    public int getMarkSize() {
        return magic.length;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
//        byte[] buf = new byte[getMarkSize()];
//        is.readNBytes(buf, 0, buf.length);
//        return Arrays.equals(magic, buf);
        return false;
    }
}
