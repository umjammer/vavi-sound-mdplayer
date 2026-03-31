package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.mid.MidiDriver;
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
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = new MidiDriver().getGD3Info(buf);
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
            music.title = "(%s)".formatted(Path.getFileName(file));
        }

        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.getFileName(file));
        }
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        Vgm.Gd3 gd3 = new MidiDriver().getGD3Info(buf);
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
            music.title = "(%s)".formatted(Path.getFileName(ms.fileName));
        }

        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.getFileName(ms.fileName));
        }

        musics.add(music);
        return musics;
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(MIDPlugin.class);
    }

    @Override
    public int getMarkSize() {
        return 0;
    }

    private static final byte[] magic = {0x4D, 0x54, 0x68, 0x64};

    @Override
    public boolean isSupported(InputStream is) throws IOException {
//        byte[] buf = new byte[getMarkSize()];
//        is.readNBytes(buf, 0, buf.length);
//        return Arrays.equals(magic, buf);
        return false;
    }
}
