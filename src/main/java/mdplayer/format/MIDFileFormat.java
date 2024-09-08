package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.mid.MID;
import mdplayer.plugin.MIDPlugin;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MIDFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MIDFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mid"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = new MID().getGD3Info(buf);
        if (gd3 != null) {
            music.title = gd3.trackName;
            music.titleJ = gd3.trackNameJ;
            music.game = gd3.gameName;
            music.gameJ = gd3.gameNameJ;
            music.composer = gd3.composer;
            music.composerJ = gd3.composerJ;
            music.vgmby = gd3.vgmBy;

            music.converted = gd3.converted;
            music.notes = gd3.notes;
        } else {
            music.title = String.format("(%s)", Path.getFileName(file));
        }

        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = String.format("(%s)", Path.getFileName(file));
        }
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        Vgm.Gd3 gd3 = new MID().getGD3Info(buf);
        if (gd3 != null) {
            music.title = gd3.trackName;
            music.titleJ = gd3.trackNameJ;
            music.game = gd3.gameName;
            music.gameJ = gd3.gameNameJ;
            music.composer = gd3.composer;
            music.composerJ = gd3.composerJ;
            music.vgmby = gd3.vgmBy;

            music.converted = gd3.converted;
            music.notes = gd3.notes;
        } else {
            music.title = String.format("(%s)", Path.getFileName(ms.fileName));
        }

        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = String.format("(%s)", Path.getFileName(ms.fileName));
        }

        musics.add(music);
        return musics;
    }

    @Override
    public Plugin getPlugin() {
        return new MIDPlugin();
    }
}
