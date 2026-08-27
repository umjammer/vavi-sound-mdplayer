package mdplayer.driver.rcp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.Common;
import mdplayer.PlayList;
import mdplayer.driver.BaseFileFormat;
import mdplayer.lib.rcp.RCS;
import mdplayer.driver.Plugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;
import vavi.util.compat.Tuple;


/**
 * RCS (Recomposer + PCM8) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-12 nsano initial version <br>
 */
public class RCSFileFormat extends BaseFileFormat {

    private static final Logger logger = System.getLogger(RCSFileFormat.class.getName());

    @Override
    public String[] getExtensions() {
        return new String[] {".rcs"};
    }

    @Override
    public MetaData getMetaData() {
        RcsDriver driver = new RcsDriver();
        // the title lives in the .RCP this .RCS names, so the driver needs to be able to find it:
        // either straight out of the extend files, or beside the .RCS by the name in its header
        driver.setFilename(filename);
        driver.setExtendFile(getExtendFiles());
        return driver.retrieveMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        // the playlist path never calls load(), and the title is in the .RCP found beside this file
        if (filename == null) filename = file;
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
        if (metaData != null) {
            music.title = metaData.getFirst(Tag.Title);
            music.titleJ = metaData.getFirst(Tag.TitleJ);
            music.game = metaData.getFirst(Tag.GameTitle);
            music.gameJ = metaData.getFirst(Tag.GameTitleJ);
            music.composer = metaData.getFirst(Tag.Composer);
            music.composerJ = metaData.getFirst(Tag.ComposerJ);
            music.vgmby = metaData.getFirst(Tag.Maker);

            music.converted = metaData.getFirst(Tag.Converter);
            music.notes = metaData.getFirst(Tag.Note);
        } else {
            music.title = "(%s)".formatted(Path.of(file).getFileName());
        }

        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.of(file).getFileName());
        }
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        // as above: without it the .RCP cannot be resolved and the title falls back to the file name
        if (filename == null) filename = ms.fileName;
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
        if (metaData != null) {
            music.title = metaData.getFirst(Tag.Title);
            music.titleJ = metaData.getFirst(Tag.TitleJ);
            music.game = metaData.getFirst(Tag.GameTitle);
            music.gameJ = metaData.getFirst(Tag.GameTitleJ);
            music.composer = metaData.getFirst(Tag.Composer);
            music.composerJ = metaData.getFirst(Tag.ComposerJ);
            music.vgmby = metaData.getFirst(Tag.Maker);

            music.converted = metaData.getFirst(Tag.Converter);
            music.notes = metaData.getFirst(Tag.Note);
        } else {
            music.title = "(%s)".formatted(Path.of(ms.fileName).getFileName());
        }
        if (music.title.isEmpty() && music.titleJ.isEmpty()) {
            music.title = "(%s)".formatted(Path.of(ms.fileName).getFileName());
        }

        musics.add(music);
        return musics;
    }

    /**
     * A .RCS is only the PCM bank plus a pointer to the .RCP that holds the actual sequence, so the
     * .RCP is an extend file like the .CM6/.GSD control files are -- and the control file names are
     * read out of that .RCP, not out of the .RCS. The tags are the ones {@link RcsDriver} looks for.
     */
    @Override
    public List<Tuple<String, byte[]>> getExtendFiles(byte[] srcBuf, Archive archive, Entry entry) {
        List<Tuple<String, byte[]>> ret = new ArrayList<>();
        byte[] buf;

        RCS rcs = new RCS();
        rcs.charset = Common.charset;
        String[] rcp = new String[1], cm6 = new String[1], gsd = new String[1], gsd2 = new String[1];
        try {
            rcs.getControlFileName(filename, null, srcBuf, rcp, cm6, gsd, gsd2);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return ret;
        }
        if (rcp[0] != null && !rcp[0].isEmpty()) {
            buf = getExtendFileAllBytes(filename, Path.of(rcp[0]).getFileName().toString(), archive, entry);
            if (buf != null) ret.add(new Tuple<>(".RCP", buf));
        }
        if (cm6[0] != null && !cm6[0].isEmpty()) {
            buf = getExtendFileAllBytes(filename, cm6[0], archive, entry);
            if (buf != null) ret.add(new Tuple<>(".CM6", buf));
        }
        if (gsd[0] != null && !gsd[0].isEmpty()) {
            buf = getExtendFileAllBytes(filename, gsd[0], archive, entry);
            if (buf != null) ret.add(new Tuple<>(".GSD", buf));
        }
        if (gsd2[0] != null && !gsd2[0].isEmpty()) {
            buf = getExtendFileAllBytes(filename, gsd2[0], archive, entry);
            if (buf != null) ret.add(new Tuple<>(".GSD", buf));
        }

        return ret;
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(RCSPlugin.class);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_RCS.mbc", "/resources/DefaultVolumeBalance_RCS.xml"};
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("RCS", "rcs");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("RCS", "rcs");
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
