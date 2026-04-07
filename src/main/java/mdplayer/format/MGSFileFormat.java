package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.PlayList;
import mdplayer.driver.mgsdrv.MgsDriver;
import mdplayer.plugin.MGSPlugin;
import mdplayer.plugin.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MGSDRV (MSX) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MGSFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mgs"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        int index = 8;
        MetaData metaData = new MgsDriver().getMetaData(buf, index);
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
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        int index = 8;
        MetaData metaData = new MgsDriver().getMetaData(buf, index);
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
    public Plugin getPlugin() {
        return Plugin.getPlugin(MGSPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("MGSDRV", "mgs");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("MGSDRV", "mgs");
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
