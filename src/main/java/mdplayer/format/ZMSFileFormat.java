package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.zms.Zms;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.ZMSPlugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * ZMS (X68000) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-11 nsano initial version <br>
 */
public class ZMSFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".zmd", ".zms"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        int index = 8;
        Vgm.Gd3 gd3 = (new Zms()).getGD3Info(buf, index);
        music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
        music.titleJ = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        int index = 8;
        Vgm.Gd3 gd3 = (new Zms()).getGD3Info(buf, index);
        music.title = gd3.trackName.isEmpty() ? Path.getFileName(zipFile) : gd3.trackName;
        music.titleJ = gd3.trackName.isEmpty() ? Path.getFileName(zipFile) : gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        musics.add(music);
        return musics;
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(ZMSPlugin.class);
    }
}
