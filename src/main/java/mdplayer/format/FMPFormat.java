/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.format;

import java.util.List;

import dotnet4j.io.Path;
import mdplayer.PlayList.Music;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.driver.fmp.FMP;
import mdplayer.plugin.AyPlugin;
import mdplayer.plugin.FMPPlugin;
import mdplayer.plugin.Plugin;
import moonDriver.common.GD3;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * FMPFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class FMPFormat extends BaseFileFormat implements FileFormat.SampledFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mpi", ".mvi", ".mzi", ".opi", ".ovi", ".ozi"};
    }

    @Override
    public List<Music> getMusic(String file, byte[] buf, String zipFile, Archive archive, Entry entry) {
        Music music = new Music();
        music.format = this;
        int index = 0;
        Vgm.Gd3 gd3 = new FMP(null).getGD3Info(buf, index);
        music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
        music.titleJ = gd3.trackNameJ.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        return List.of(music);
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(FMPPlugin.class);
    }
}
