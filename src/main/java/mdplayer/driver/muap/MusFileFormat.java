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
import mdplayer.lib.mgsc.MgscCompiler;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;

import static vavi.util.compat.Util.changeExtension;


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

    /**
     * A ".mus" is the MML the ".o" is compiled from, and the plugin compiles it on the way to
     * playing it - see {@link MuapDriver#compile}.
     */
    @Override
    public boolean isMml() {
        return true;
    }

    @Override
    public String getCompiledFilename() {
        return filename != null ? changeExtension(filename, ".o") : null;
    }

    /** MSX MGSDRV's MML is called ".mus" too, and it is the one with the "#" directives */
    @Override
    public boolean accepts(String filename, byte[] head) {
        return !MgscCompiler.isMgsMml(head);
    }

    @Override
    public MetaData getMetaData() {
        return new MuapDriver().retrieveMetaData(this.srcBuf, 0);
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
        return SNIFF_SIZE; // ".mus" is shared with MSX MGSDRV, so accepts() has to see inside the file
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        String name = Path.of(SoundUtil.getSource(is)).toString().toLowerCase();
        if (Arrays.stream(getExtensions()).noneMatch(name::endsWith)) return false;
        return accepts(name, is.readNBytes(SNIFF_SIZE));
    }
}
