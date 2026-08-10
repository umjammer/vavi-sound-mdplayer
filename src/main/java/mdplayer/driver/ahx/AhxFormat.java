/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ahx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.PlayList.Music;
import mdplayer.driver.BaseFileFormat;
import mdplayer.driver.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * AHX (Amiga) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-03 nsano initial version <br>
 */
public class AhxFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".ahx", ".thx"};
    }

    @Override
    public MetaData getMetaData() {
        return new AhxDriver().getMetaData(this.srcBuf);
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
            music.title = songs == 1 ? md.getFirst(Tag.Title) : "%s - Trk %d".formatted(md.getFirst(Tag.Title), s + 1);
            music.titleJ = music.title;
            music.game = "";
            music.gameJ = "";
            music.composer = "";
            music.composerJ = "";
            music.vgmby = "";
            music.converted = "";
            music.notes = "";
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
        return new String[] {"DriverBalance_AHX.mbc", "/resources/DefaultVolumeBalance_AHX.xml"};
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(AhxPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("AHX", "ahx");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("AHX", "ahx");
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
