package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.rcp.RCP;
import mdplayer.driver.rcp.RcpDriver;
import mdplayer.driver.rcp.RcsDriver;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.RCSPlugin;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * RCS (Recomposer + PCM8) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-12 nsano initial version <br>
 */
public class RCSFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".rcs"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = new RcsDriver().getGD3Info(buf);
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
        Vgm.Gd3 gd3 = new RcpDriver().getGD3Info(buf);
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
        return Plugin.getPlugin(RCSPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("RCS", "rcs");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("RCS", "rcs");
    }

    @Override
    public int getMarkSize() {
        return 0;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        return Arrays.stream(getExtensions()).anyMatch(e -> java.nio.file.Path.of(SoundUtil.getSource(is)).toString().toLowerCase().endsWith(e));
    }
}
