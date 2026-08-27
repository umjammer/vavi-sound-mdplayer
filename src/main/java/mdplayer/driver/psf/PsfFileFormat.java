/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.PlayList.Music;
import mdplayer.driver.BaseFileFormat;
import mdplayer.driver.Plugin;
import mdplayer.lib.psf.PsfFile;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;
import vavi.util.compat.Tuple;


/**
 * PSF1 (Sony PlayStation) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class PsfFileFormat extends BaseFileFormat {

    /** the version byte a PSF1 carries */
    public int version() {
        return 1;
    }

    @Override
    public String[] getExtensions() {
        return new String[] {".psf", ".minipsf"};
    }

    @Override
    public MetaData getMetaData() {
        return new PsfDriver().retrieveMetaData(this.srcBuf);
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
            music.game = md.getFirst(Tag.GameTitle);
            music.gameJ = md.getFirst(Tag.GameTitleJ);
            music.composer = md.getFirst(Tag.Composer);
            music.composerJ = md.getFirst(Tag.ComposerJ);
            music.vgmby = md.getFirst(Tag.Maker);
            music.converted = md.getFirst(Tag.Converter);
            music.notes = md.getFirst(Tag.Note);
            music.duration = md.getFirst(Tag.Duration);
        }
        music.songNo = 0;

        return Collections.singletonList(music);
    }

    @Override
    public List<Music> getMusic(Music ms, byte[] buf, String zipFile) {
        return getMusicCommon(ms, buf, zipFile);
    }

    /**
     * A ".minipsf" holds only what makes it different from a library, and names that library
     * in its "_lib" tags. They come along as extend files, keyed by the name the tag holds.
     */
    @Override
    protected List<Tuple<String, byte[]>> getExtendFiles(byte[] srcBuf, Archive archive, Entry entry) {
        List<Tuple<String, byte[]>> ret = new ArrayList<>();

        PsfFile psf;
        try {
            psf = PsfFile.decode(srcBuf);
        } catch (IOException e) {
            return ret;
        }

        for (int i = 0; i < PsfFile.MAX_LIBS; i++) {
            String tag = i == 0 ? "_lib" : "_lib" + (i + 1);
            String name = psf.tag(tag);
            if (name == null || name.isEmpty()) {
                continue;
            }
            byte[] buf = getExtendFileAllBytes(filename, name, archive, entry);
            if (buf != null) {
                ret.add(new Tuple<>(name, buf));
            }
        }

        return ret;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_PSF.mbc", "/resources/DefaultVolumeBalance_PSF.xml"};
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(PsfPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("PSF", "psf");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("PSF", "psf");
    }

    @Override
    public int getMarkSize() {
        return 16;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        byte[] header = new byte[4];
        int r = is.read(header);
        if (r < 4) return false;
        return header[0] == 'P' && header[1] == 'S' && header[2] == 'F' && (header[3] & 0xff) == version();
    }
}
