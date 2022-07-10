package mdplayer.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.rcp.RCP;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.RCPPlugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * RCPFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class RCPFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".rcp"};
    }

    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = new RCP().getGD3Info(buf);
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

    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        Vgm.Gd3 gd3 = new RCP().getGD3Info(buf);
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
    public List<Tuple<String, byte[]>> getExtendFile(String fn, byte[] srcBuf, Archive archive, Entry entry) {
        List<Tuple<String, byte[]>> ret = new ArrayList<>();
        byte[] buf;

        String[] cm6 = new String[1], gsd = new String[1], gsd2 = new String[1];
        RCP.getControlFileName(srcBuf, cm6, gsd, gsd2);
        if (cm6[0] != null && !cm6[0].isEmpty()) {
            buf = getExtendFileAllBytes(fn, cm6[0], archive, entry);
            if (buf != null) ret.add(new Tuple<>(".cm6", buf));
        }
        if (gsd[0] != null && !gsd[0].isEmpty()) {
            buf = getExtendFileAllBytes(fn, gsd[0], archive, entry);
            if (buf != null) ret.add(new Tuple<>(".gsd", buf));
        }
        if (gsd2[0] != null && !gsd2[0].isEmpty()) {
            buf = getExtendFileAllBytes(fn, gsd2[0], archive, entry);
            if (buf != null) ret.add(new Tuple<>(".gsd", buf));
        }

        return ret;
    }

    @Override
    public Plugin getPlugin() {
        return new RCPPlugin();
    }
}
