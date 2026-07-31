/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.muap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

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
 * Muap MML.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-29 nsano initial version <br>
 */
public class MusFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mus"};
    }

    @Override
    public MetaData getMetaData() {
        return new MuapDriver().getMetaData(this.srcBuf, 0);
    }

    @Override
    public List<Music> getMusic(String file, byte[] buf, String zipFile, Archive archive, Entry entry) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
        music.title = metaData.getFirst(Tag.Title).isEmpty() ? Path.of(file).getFileName().toString() : metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ).isEmpty() ? Path.of(file).getFileName().toString() : metaData.getFirst(Tag.TitleJ);
        music.game = metaData.getFirst(Tag.GameTitle);
        music.gameJ = metaData.getFirst(Tag.GameTitleJ);
        music.composer = metaData.getFirst(Tag.Composer);
        music.composerJ = metaData.getFirst(Tag.ComposerJ);
        music.vgmby = metaData.getFirst(Tag.Maker);

        music.converted = metaData.getFirst(Tag.Converter);
        music.notes = metaData.getFirst(Tag.Note);

        return List.of(music);
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(MuapPlugin.class);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_MUAP.mbc", "/resources/DefaultVolumeBalance_MUAP.xml"};
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("MUAP", "mus");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("MUAP", "mus");
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
