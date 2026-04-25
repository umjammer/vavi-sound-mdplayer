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
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.PlayList.Music;
import mdplayer.driver.musica.MusicaK4Driver;
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
 * MuSICA (MSX) MSD (MML) Format.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-20 nsano initial version <br>
 */
public class MsdFormat extends BaseFileFormat implements FileFormat.SampledFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".msd"};
    }

    @Override
    public MetaData getMetaData() {
        String vcd = Path.changeExtension(filename, ".vcd");
        byte[] vcdBuf = null;
        if (File.exists(vcd)) {
            vcdBuf = File.readAllBytes(vcd);
        }
        return new MusicaK4Driver().getMetaData(this.srcBuf, vcdBuf);
    }

    @Override
    public List<Music> getMusic(String file, byte[] buf, String zipFile, Archive archive, Entry entry) {
        Music music = new Music();

        music.format = this;
        MetaData metaData = getMetaData();
        if (metaData == null) {
            //logger.log(Level.WARNING, ".MSD compilation failed", "PlayList", MessageBoxButtons.OK, MessageBoxIcon.Error);
            music.title = Path.getFileName(file);
            music.titleJ = Path.getFileName(file);
            music.notes = "";
        } else {
            music.title = metaData.getFirst(Tag.Title).isEmpty() ? Path.getFileName(file) : metaData.getFirst(Tag.Title);
            music.titleJ = metaData.getFirst(Tag.TitleJ).isEmpty() ? Path.getFileName(file) : metaData.getFirst(Tag.TitleJ);
            music.notes = metaData.getFirst(Tag.Note);
        }
        music.game = "";
        music.gameJ = "";
        music.composer = "";
        music.composerJ = "";
        music.vgmby = "";
        music.converted = "";

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
        return new MdEncoding("MuSICA", "msd");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("MuSICA", "msd");
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
