package mdplayer.format;

import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.mucom.MucomDotNET;
import mdplayer.plugin.MucomPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MUCFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MUCFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".muc"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = new MucomDotNET().getGD3Info(buf);
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
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_MUC.mbc",
                Resources.getDefaultVolumeBalance_MUC()
        };
    }

    @Override
    public Plugin getPlugin() {
        return new MucomPlugin();
    }
}
