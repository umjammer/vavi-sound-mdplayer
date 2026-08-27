/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ym;

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
 * YM (Atari ST, stsound) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-03 nsano initial version <br>
 */
public class YmFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".ym"};
    }

    @Override
    public MetaData getMetaData() {
        return new YmDriver().retrieveMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();

        MetaData md = getMetaData();

        PlayList.Music music = new PlayList.Music();
        music.format = this;
        music.fileName = file;
        music.arcFileName = zipFile;
        music.arcType = EnmArcType.unknown;
        if (zipFile != null && zipFile.isEmpty())
            music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
        music.title = md.getFirst(Tag.Title);
        music.titleJ = music.title;
        music.game = "";
        music.gameJ = "";
        music.composer = md.getFirst(Tag.Composer);
        music.composerJ = music.composer;
        music.vgmby = "";
        music.converted = "";
        music.notes = md.getFirst(Tag.Note);

        musics.add(music);

        return musics;
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_YM.mbc", "/resources/DefaultVolumeBalance_YM.xml"};
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(YmPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("YM", "ym");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("YM", "ym");
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
