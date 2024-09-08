package mdplayer.format;

import java.util.Collections;
import java.util.List;

import mdplayer.PlayList;
import mdplayer.plugin.MDLPlugin;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MDLFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MDLFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return new MDLPlugin();
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry) {
        return null;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry) {
        return null;
    }
}
