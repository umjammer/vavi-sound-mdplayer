/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.PlayList.Music;
import mdplayer.driver.Vgm;
import mdplayer.driver.musica.MusicaDriver;
import mdplayer.driver.musica.MusicaK4Driver;
import mdplayer.plugin.MuSICAPlugin;
import mdplayer.plugin.Plugin;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MuSICA (MSX) Format.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class MuSICAFormat extends BaseFileFormat implements FileFormat.SampledFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".msd", ".bgm"};
    }

    @Override
    public List<Music> getMusic(String file, byte[] buf, String zipFile, Archive archive, Entry entry) {
        Music music = new Music();
        String ext = file.substring(file.lastIndexOf('.'));
        if (ext.equalsIgnoreCase(".bgm")) {
            music.format = this;
            Vgm.Gd3 gd3 = (new MusicaDriver()).getGD3Info(buf, null);
            music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
            music.titleJ = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
            music.game = "";
            music.gameJ = "";
            music.composer = "";
            music.composerJ = "";
            music.vgmby = "";
            music.converted = "";
            music.notes = gd3.notes.isEmpty() ? "" : gd3.notes;

        } else if (ext.equalsIgnoreCase(".msd")) {
            music.format = this;
            String vcd = Path.changeExtension(music.fileName, ".vcd");
            byte[] vcdBuf = null;
            if (File.exists(vcd)) {
                vcdBuf = File.readAllBytes(vcd);
            }
            Vgm.Gd3 gd3 = (new MusicaK4Driver()).getGD3Info(buf, vcdBuf);
            if (gd3 == null) {
                //logger.log(Level.WARNING, ".MSD compilation failed", "PlayList", MessageBoxButtons.OK, MessageBoxIcon.Error);
                music.title = Path.getFileName(file);
                music.titleJ = Path.getFileName(file);
                music.notes = "";
            } else {
                music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
                music.titleJ = gd3.trackNameJ.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
                music.notes = gd3.notes.isEmpty() ? "" : gd3.notes;
            }
            music.game = "";
            music.gameJ = "";
            music.composer = "";
            music.composerJ = "";
            music.vgmby = "";
            music.converted = "";
        }
        return List.of(music);
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(MuSICAPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return MdEncoding.MUSICA;
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
