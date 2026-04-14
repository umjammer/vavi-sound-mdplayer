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
import mdplayer.Common;
import mdplayer.PlayList;
import mdplayer.driver.rcp.RCP;
import mdplayer.driver.rcp.RcpDriver;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.RCPPlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * RCP (Recomposer) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class RCPFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".rcp"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = new RcpDriver().getMetaData(buf);
        if (metaData != null) {
            music.title = metaData.getFirst(Tag.Title);
            music.titleJ = metaData.getFirst(Tag.TitleJ);
            music.game = metaData.getFirst(Tag.GameTitle);
            music.gameJ = metaData.getFirst(Tag.GameTitleJ);
            music.composer = metaData.getFirst(Tag.Composer);
            music.composerJ = metaData.getFirst(Tag.ComposerJ);
            music.vgmby = metaData.getFirst(Tag.Maker);

            music.converted = metaData.getFirst(Tag.Converter);
            music.notes = metaData.getFirst(Tag.Note);
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
        MetaData metaData  = new RcpDriver().getMetaData(buf);
        if (metaData != null) {
            music.title = metaData.getFirst(Tag.Title);
            music.titleJ = metaData.getFirst(Tag.TitleJ);
            music.game = metaData.getFirst(Tag.GameTitle);
            music.gameJ = metaData.getFirst(Tag.GameTitleJ);
            music.composer = metaData.getFirst(Tag.Composer);
            music.composerJ = metaData.getFirst(Tag.ComposerJ);
            music.vgmby = metaData.getFirst(Tag.Maker);

            music.converted = metaData.getFirst(Tag.Converter);
            music.notes = metaData.getFirst(Tag.Note);
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
        RCP.getControlFileName(srcBuf, cm6, gsd, gsd2, Common.charset);
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
        return Plugin.getPlugin(RCPPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("RCP", "rcp");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("RCP", "rcp");
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
