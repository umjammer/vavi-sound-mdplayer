package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.PlayList;
import mdplayer.driver.ndp.NdpDriver;
import mdplayer.plugin.NDPPlugin;
import mdplayer.plugin.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * NDP (MSX) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-10 nsano initial version <br>
 */
public class NDPFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".ndp"};
    }

    @Override
    public MetaData getMetaData(byte[] buf) {
        return new NdpDriver().getMetaData(buf, 8);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData(buf);
        music.title = metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ);
        music.game = "";
        music.gameJ = "";
        music.composer = "";
        music.composerJ = "";
        music.vgmby = "";

        music.converted = "";
        music.notes = "";

        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData(buf);
        music.title = metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ);
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
        return Plugin.getPlugin(NDPPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("NDP", "ndp");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("NDP", "ndp");
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
