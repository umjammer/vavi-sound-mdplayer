package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.Path;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.Xgm2;
import mdplayer.driver.Xgm2Driver;
import mdplayer.driver.XgmDriver;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.XGMPlugin;
import mdplayer.properties.Resources;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * XGMFileFormat.
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
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3;
        if (!Xgm2.checkXGM2(buf)) {
            gd3 = new XgmDriver().getGD3Info(buf, 0);
        } else {
            gd3 = new Xgm2Driver().getGD3Info(buf, 0);
        }
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        if (music.title.isEmpty() && music.titleJ.isEmpty() && music.game.isEmpty() && music.gameJ.isEmpty() && music.composer.isEmpty() && music.composerJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.getFileName(file));
        }
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        Vgm.Gd3 gd3;
        if (!Xgm2.checkXGM2(buf)) {
            gd3 = new XgmDriver().getGD3Info(buf, 0);
        } else {
            gd3 = new Xgm2Driver().getGD3Info(buf, 0);
        }
        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        if (music.title.isEmpty() && music.titleJ.isEmpty() && music.game.isEmpty() && music.gameJ.isEmpty() && music.composer.isEmpty() && music.composerJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.getFileName(ms.fileName));
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
        return MdEncoding.XGM;
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
