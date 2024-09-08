package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.nrtdrv.NRTDRV;
import mdplayer.plugin.NRTPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * NRTFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class NRTFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".nrd"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        int index = 42;
        Vgm.Gd3 gd3 = (new NRTDRV()).getGD3Info(buf, index);
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
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
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        int index = 42;
        Vgm.Gd3 gd3 = (new NRTDRV()).getGD3Info(buf, index);
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
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
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_NRT.mbc",
                Resources.getDefaultVolumeBalance_NRT()
        };
    }

    @Override
    public Plugin getPlugin() {
        return new NRTPlugin();
    }
}
