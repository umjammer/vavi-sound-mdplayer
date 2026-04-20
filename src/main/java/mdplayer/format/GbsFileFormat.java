package mdplayer.format;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.PlayList.Music;
import mdplayer.driver.gbs.Gbs;
import mdplayer.plugin.GbsPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * GBS GameBoy.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-10 nsano initial version <br>
 */
public class GbsFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".gbs"};
    }

    @Override
    public MetaData getMetaData() {
        return new Gbs().getMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();

        MetaData md = getMetaData();
        int songs = Integer.parseInt(md.getFirst(Tag.NumberOfSongs));

        for (int s = 0; s < songs; s++) {
            PlayList.Music music = new PlayList.Music();
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.arcType = EnmArcType.unknown;
            if (zipFile != null && zipFile.isEmpty())
                music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
            music.title = "%s - Trk %d".formatted(md.getFirst(Tag.Title), s + 1);
            music.titleJ = "%s - Trk %d".formatted(md.getFirst(Tag.TitleJ), s + 1);
            music.game = md.getFirst(Tag.GameTitle);
            music.gameJ = md.getFirst(Tag.GameTitleJ);
            music.composer = md.getFirst(Tag.Composer);
            music.composerJ = md.getFirst(Tag.ComposerJ);
            music.vgmby = md.getFirst(Tag.Maker);
            music.converted = md.getFirst(Tag.Converter);
            music.notes = md.getFirst(Tag.Note);
            music.songNo = s;

            musics.add(music);
        }

        return musics;
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_GBS.mbc",
                Resources.getDefaultVolumeBalance_GBS()
        };
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(GbsPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("GBS", "gbs");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("GBS", "gbs");
    }

    @Override
    public int getMarkSize() {
        return 0;
    }

    @Override
    public boolean isSupported(InputStream is) {
        if (isCompressedStream(is)) return false;
        return Arrays.stream(getExtensions()).anyMatch(e -> java.nio.file.Path.of(SoundUtil.getSource(is)).toString().toLowerCase().endsWith(e));
    }
}
