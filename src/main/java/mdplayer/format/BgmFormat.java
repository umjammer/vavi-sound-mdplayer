/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.PlayList.Music;
import mdplayer.driver.musica.MusicaDriver;
import mdplayer.plugin.MuSICAPlugin;
import mdplayer.plugin.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MuSICA (MSX) BGM (OBJ) Format.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class BgmFormat extends BaseFileFormat implements FileFormat.SampledFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".bgm"};
    }

    @Override
    public MetaData getMetaData() {
        return new MusicaDriver().getMetaData(this.srcBuf);
    }

    @Override
    public List<Music> getMusic(String file, byte[] buf, String zipFile, Archive archive, Entry entry) {
        Music music = new Music();

        music.format = this;
        MetaData metaData = getMetaData();
        music.title = metaData.getFirst(Tag.Title).isEmpty() ? Path.of(file).getFileName().toString() : metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ).isEmpty() ? Path.of(file).getFileName().toString() : metaData.getFirst(Tag.TitleJ);
        music.game = "";
        music.gameJ = "";
        music.composer = "";
        music.composerJ = "";
        music.vgmby = "";
        music.converted = "";
        music.notes = metaData.getFirst(Tag.Note);

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
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_MuSICA.mbc", "/resources/DefaultVolumeBalance_MuSICA.xml"};
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("MuSICA", "bgm");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("MuSICA", "bgm");
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
