package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.mgsdrv.MGSDRV;
import mdplayer.plugin.MGSPlugin;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MGSFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MGSFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mgs"};
    }

    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        int index = 8;
        Vgm.Gd3 gd3 = (new MGSDRV()).getGD3Info(buf, index);
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = "";
        music.gameJ = "";
        music.composer = "";
        music.composerJ = "";
        music.vgmby = "";

        music.converted = "";
        music.notes = "";
        return Collections.singletonList(music);
    }

    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        int index = 8;
        Vgm.Gd3 gd3 = (new MGSDRV()).getGD3Info(buf, index);
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = "";
        music.gameJ = "";
        music.composer = "";
        music.composerJ = "";
        music.vgmby = "";

        music.converted = "";
        music.notes = "";

        musics.add(music);
        return musics;
    }

    @Override
    public Plugin getPlugin() {
        return new MGSPlugin();
    }
}
