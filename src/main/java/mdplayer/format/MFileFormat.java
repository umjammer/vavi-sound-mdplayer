package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.pmd.PmdDriver;
import mdplayer.plugin.PMDPlugin;
import mdplayer.plugin.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * PC98 PMD .M File Format.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".m", ".m2", ".mz"};
    }

    @Override
    public MetaData getMetaData(byte[] buf) {
        return new PmdDriver().getMetaData(buf, PmdDriver.PMDFileType.M);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData(buf);
        music.title = metaData.getFirst(Tag.Title).isEmpty() ? Path.getFileName(file) : metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ).isEmpty() ? Path.getFileName(file) : metaData.getFirst(Tag.TitleJ);
        music.game = metaData.getFirst(Tag.GameTitle);
        music.gameJ = metaData.getFirst(Tag.GameTitleJ);
        music.composer = metaData.getFirst(Tag.Composer);
        music.composerJ = metaData.getFirst(Tag.ComposerJ);
        music.vgmby = metaData.getFirst(Tag.Maker);

        music.converted = metaData.getFirst(Tag.Converter);
        music.notes = metaData.getFirst(Tag.Note);

        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(PMDPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("PMD", "m,m2,mz");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("PMD", "m,m2,mz");
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
