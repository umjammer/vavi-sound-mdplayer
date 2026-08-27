/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp7;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
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

import static vavi.util.compat.Util.changeExtension;


/**
 * FMP7 (".owi") FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 */
public class Fmp7FileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".owi", ".mwi"};
    }

    /**
     * A ".mwi" is the MML the ".owi" is compiled from, and the plugin compiles it on the way to
     * playing it - see {@link mdplayer.lib.fmp7.Fmp7Compiler}.
     */
    @Override
    public boolean isMml() {
        return filename != null && filename.toLowerCase().endsWith(".mwi");
    }

    @Override
    public String getCompiledFilename() {
        return isMml() ? changeExtension(filename, ".owi") : filename;
    }

    @Override
    public MetaData getMetaData() {
        return new Fmp7Driver().retrieveMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        MetaData md = getMetaData();

        PlayList.Music music = new PlayList.Music();
        music.format = this;
        music.fileName = file;
        music.arcFileName = zipFile;
        music.arcType = EnmArcType.unknown;
        if (zipFile != null && !zipFile.isEmpty())
            music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;

        if (md != null) {
            music.title = md.getFirst(Tag.Title);
            music.titleJ = md.getFirst(Tag.TitleJ);
            music.composer = md.getFirst(Tag.Composer);
            music.composerJ = md.getFirst(Tag.ComposerJ);
            music.vgmby = md.getFirst(Tag.Maker);
            music.notes = md.getFirst(Tag.Note);
        }
        music.songNo = 0;

        return Collections.singletonList(music);
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_FMP7.mbc", "/resources/DefaultVolumeBalance_FMP7.xml"};
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(Fmp7Plugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("FMP7", "owi,mwi");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("FMP7", "owi,mwi");
    }

    @Override
    public int getMarkSize() {
        return 8;
    }

    /**
     * An ".owi" says so in its first four bytes; a ".mwi" is plain MML text with nothing at the
     * front to recognise, so that one is taken on its name - which is all its own compiler has
     * to go on either.
     */
    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        if (Path.of(SoundUtil.getSource(is)).toString().toLowerCase().endsWith(".mwi")) return true;
        byte[] header = new byte[4];
        int r = is.read(header);
        if (r < 4) return false;
        return header[0] == 'O' && header[1] == 'W' && header[2] == 'I' && header[3] == '0';
    }
}
