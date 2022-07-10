package mdplayer.format;

import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.zgm.Zgm;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * ZGMFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class ZGMFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".zgm"};
    }

    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = new Zgm().getGD3Info(buf);
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        if (music.title.isEmpty() && music.titleJ.isEmpty() && music.game.isEmpty() && music.gameJ.isEmpty() && music.composer.isEmpty() && music.composerJ.isEmpty()) {
            music.title = String.format("(%s)", Path.getFileName(file));
        }
        return Collections.singletonList(music);
    }

    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_ZGM.mbc",
                Resources.getDefaultVolumeBalance_ZGM()
        };
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }
}
