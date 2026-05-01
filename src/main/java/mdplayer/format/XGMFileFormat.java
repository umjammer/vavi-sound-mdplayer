package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.PlayList;
import mdplayer.driver.Xgm2;
import mdplayer.driver.Xgm2Driver;
import mdplayer.driver.XgmDriver;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.XGMPlugin;
import mdplayer.properties.Resources;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * Mega Drive XGM.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class XGMFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".xgm"};
    }

    @Override
    public MetaData getMetaData() {
        if (!Xgm2.checkXGM2(this.srcBuf)) {
            return new XgmDriver().getMetaData(this.srcBuf);
        } else {
            return new Xgm2Driver().getMetaData(this.srcBuf);
        }
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
        music.title = metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ);
        music.game = metaData.getFirst(Tag.GameTitle);
        music.gameJ = metaData.getFirst(Tag.GameTitleJ);
        music.composer = metaData.getFirst(Tag.Composer);
        music.composerJ = metaData.getFirst(Tag.ComposerJ);
        music.vgmby = metaData.getFirst(Tag.Maker);

        music.converted = metaData.getFirst(Tag.Converter);
        music.notes = metaData.getFirst(Tag.Note);

        if (music.title.isEmpty() && music.titleJ.isEmpty() && music.game.isEmpty() && music.gameJ.isEmpty() && music.composer.isEmpty() && music.composerJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.of(file).getFileName());
        }

        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
        music.title = metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ);
        music.game = metaData.getFirst(Tag.GameTitle);
        music.gameJ = metaData.getFirst(Tag.GameTitleJ);
        music.composer = metaData.getFirst(Tag.Composer);
        music.composerJ = metaData.getFirst(Tag.ComposerJ);
        music.vgmby = metaData.getFirst(Tag.Maker);

        music.converted = metaData.getFirst(Tag.Converter);
        music.notes = metaData.getFirst(Tag.Note);

        if (music.title.isEmpty() && music.titleJ.isEmpty() && music.game.isEmpty() && music.gameJ.isEmpty() && music.composer.isEmpty() && music.composerJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.of(ms.fileName).getFileName());
        }

        musics.add(music);
        return musics;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_XGM.mbc",
                Resources.getDefaultVolumeBalance_XGM()
        };
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(XGMPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("XGM", "xgm,xgm2");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("XGM", "xgm, xgm2");
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
