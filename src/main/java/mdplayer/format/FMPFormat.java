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

import dotnet4j.io.Path;
import dotnet4j.util.compat.StringUtilities;
import mdplayer.PlayList.Music;
import mdplayer.driver.fmp.FmpDriver;
import mdplayer.plugin.FMPPlugin;
import mdplayer.plugin.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * FMP (PC-9801) Format.
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
    public MetaData getMetaData() {
        if (!isMml()) {
            return new FmpDriver().getMetaData(this.srcBuf, 0);
        } else {
            return null;
        }
    }

    @Override
    public boolean isMml() {
        if (StringUtilities.isNullOrEmpty(this.filename)) return false;
        String ext = this.filename.substring(this.filename.lastIndexOf('.'));
        if (StringUtilities.isNullOrEmpty(ext)) return false;
        ext = ext.toLowerCase();
        return ext.length() > 3 && ext.charAt(1) == 'm';
    }

    public String getCompiledFilename() {
        String ext = this.filename.substring(this.filename.lastIndexOf('.'));
        return Path.changeExtension(this.filename,
                ext.equals(".mpi") ? ".opi" : (ext.equals(".mvi") ? ".ovi" : ".ozi"));
    }

    @Override
    public List<Music> getMusic(String file, byte[] buf, String zipFile, Archive archive, Entry entry) {
        Music music = new Music();

        music.format = this;
        MetaData metaData = getMetaData();
        music.title = metaData.getFirst(Tag.Title).isEmpty() ? Path.getFileName(file) : metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ).isEmpty() ? Path.getFileName(file) : metaData.getFirst(Tag.TitleJ);
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
        return Plugin.getPlugin(FMPPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("FMP", "mpi,opi,mvi,ovi,mzi,ozi");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("FMP", "mpi,opi,mvi,ovi,mzi,ozi");
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
