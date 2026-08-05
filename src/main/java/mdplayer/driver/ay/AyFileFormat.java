package mdplayer.driver.ay;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.driver.BaseFileFormat;
import mdplayer.driver.FileFormat.SampledFileFormat;
import mdplayer.driver.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;

import static java.lang.System.getLogger;


/**
 * ZX Spectrum (AY) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-19 nsano initial version <br>
 */
public class AyFileFormat extends BaseFileFormat implements SampledFileFormat {

    private static final Logger logger = getLogger(AyFileFormat.class.getName());

    @Override
    public String[] getExtensions() {
        return new String[] {".ay"};
    }

    @Override
    public MetaData getMetaData() {
        return new AyDriver().getMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();

        AyDriver driver = new AyDriver();
        int songs;
        try {
            songs = Integer.parseInt(driver.getMetaData(buf).getFirst(Tag.NumberOfSongs));
        } catch (Exception e) {
            // a file this header reader cannot make sense of still belongs in the play list, under
            // its file name, the way it did before there was anything to read out of it
            logger.log(Level.WARNING, "no song info: " + file + ": " + e);
            PlayList.Music music = new PlayList.Music();
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.arcType = EnmArcType.unknown;
            return List.of(music);
        }

        for (int s = 0; s < songs; s++) {
            MetaData md = driver.getMetaData(buf, s);

            PlayList.Music music = new PlayList.Music();
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.arcType = EnmArcType.unknown;
            if (zipFile != null && !zipFile.isEmpty())
                music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
            music.title = songs > 1 ? "%s - Trk %d".formatted(md.getFirst(Tag.Title), s + 1) : md.getFirst(Tag.Title);
            music.titleJ = music.title;
            music.composer = md.getFirst(Tag.Composer);
            music.composerJ = music.composer;
            music.notes = md.getFirst(Tag.Note);
            music.duration = md.getFirst(Tag.Duration);
            music.songNo = s;

            musics.add(music);
        }

        return musics;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(AyPlugin.class);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_AY.mbc", "/resources/DefaultVolumeBalance_AY.xml"};
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("AY", "ay");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("AY", "ay");
    }

    @Override
    public int getMarkSize() {
        return 0;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        return Arrays.stream(getExtensions()).anyMatch(e -> Path.of(SoundUtil.getSource(is)).toString().toLowerCase().endsWith(e));
    }
}
