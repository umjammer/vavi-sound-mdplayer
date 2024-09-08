package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.SampledPlugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * WAVFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class WAVFileFormat extends BaseFileFormat implements FileFormat.SampledFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".wav"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        music.title = String.format("(%s)", Path.getFileName(file));
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public byte[] getAllBytes(String filename) {
        return new byte[] {(byte) 'W', (byte) 'A', (byte) 'V'};
    }

    @Override
    public Plugin getPlugin() {
        return new SampledPlugin();
    }
}
