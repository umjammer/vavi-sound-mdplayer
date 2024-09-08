package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.M3U;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * M3UFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class M3UFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".m3u"};
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
        return null;
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry/* = null*/) {

        String rootPath = Path.getDirectoryName(mc.fileName);
        PlayList pl;
        if (entry == null) pl = M3U.loadM3U(mc.fileName, rootPath);
        else pl = M3U.loadM3U(archive, entry, mc.arcFileName);
        if (pl == null) return null;
        if (pl.getMusics() == null || pl.getMusics().size() < 1) return null;

        List<PlayList.Music> musics = new ArrayList<>();
        for (PlayList.Music m : pl.getMusics()) musics.addAll(addFileLoop(m, archive, entry));
        return musics;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry/* = null*/) {

        String rootPath = Path.getDirectoryName(mc.fileName);
        PlayList pl;
        if (entry == null) pl = M3U.loadM3U(mc.fileName, rootPath);
        else pl = M3U.loadM3U(archive, entry, mc.arcFileName);
        if (pl == null) return null;
        if (pl.getMusics() == null || pl.getMusics().size() < 1) return null;

        List<PlayList.Music> musics = new ArrayList<>();
        for (PlayList.Music m : pl.getMusics()) musics.addAll(addFileLoop(index, m, archive, entry));
        return musics;
    }
}
