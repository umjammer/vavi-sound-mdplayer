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
 * MP3FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MP3FileFormat extends BaseFileFormat implements FileFormat.SampledFileFormat, StreamFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mp3"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        music.title = "(%s)".formatted(Path.of(file).getFileName());
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

//        return new byte[] {(byte) 'M', (byte) 'P', (byte) '3'};

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(SampledPlugin.class);
    }

    @Override
    public int getMarkSize() {
        return 0;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
//        if (isCompressedStream(is)) return false;
//        return Arrays.stream(getExtensions()).anyMatch(e -> java.nio.file.Path.of(SoundUtil.getSource(is)).toString().toLowerCase().endsWith(e));
        return false;
    }
}
