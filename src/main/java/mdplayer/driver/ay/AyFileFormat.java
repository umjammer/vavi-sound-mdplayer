package mdplayer.driver.ay;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.driver.BaseFileFormat;
import mdplayer.driver.FileFormat.SampledFileFormat;
import mdplayer.driver.Plugin;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * ZX Spectrum (AY) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-19 nsano initial version <br>
 */
public class AyFileFormat extends BaseFileFormat implements SampledFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".ay"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        music.arcFileName = zipFile;
        music.arcType = EnmArcType.unknown;

        return List.of(music);
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
