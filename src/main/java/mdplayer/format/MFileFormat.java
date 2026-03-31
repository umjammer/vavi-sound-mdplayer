package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.pmd.PmdDriver;
import mdplayer.plugin.PMDPlugin;
import mdplayer.plugin.Plugin;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * PMD .M File Format.
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
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        int index = 0;
        Vgm.Gd3 gd3 = new PmdDriver().getGD3Info(buf, index, PmdDriver.PMDFileType.M);
        music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
        music.titleJ = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;
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
        return MdEncoding.PMD;
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
