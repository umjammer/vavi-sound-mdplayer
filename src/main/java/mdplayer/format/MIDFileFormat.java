package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mdplayer.PlayList;
import mdplayer.driver.mid.MidiDriver;
import mdplayer.plugin.MIDPlugin;
import mdplayer.plugin.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
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
    public MetaData getMetaData() {
        return new MidiDriver().getMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
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
            music.title = "(%s)".formatted(Path.of(file).getFileName());
        }
        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
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
            music.title = "(%s)".formatted(Path.of(ms.fileName).getFileName());
        }

        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.of(ms.fileName).getFileName());
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
