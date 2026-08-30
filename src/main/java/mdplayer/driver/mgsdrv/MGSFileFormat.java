package mdplayer.driver.mgsdrv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.PlayList;
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
 * MGSDRV (MSX) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MGSFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mgs", ".mus"};
    }

    /**
     * A ".mus" is the MML the ".mgs" is compiled from, and the plugin compiles it on the way to
     * playing it - see {@link mdplayer.lib.mgsc.MgscCompiler}.
     */
    @Override
    public boolean isMml() {
        return filename != null && filename.toLowerCase().endsWith(".mus");
    }

    @Override
    public String getCompiledFilename() {
        return isMml() ? changeExtension(filename, ".mgs") : filename;
    }

    /** ".mus" is MUAP98's MML as well, so a ".mus" is only ours when it is written in MGSC's */
    @Override
    public boolean accepts(String filename, byte[] head) {
        return !filename.toLowerCase().endsWith(".mus") || MgscCompiler.isMgsMml(head);
    }

    @Override
    public MetaData getMetaData() {
        return isMml() ? mmlMetaData() : new MgsDriver().retrieveMetaData(this.srcBuf, 8);
    }

    /**
     * What the MML says about itself, which is its {@code #title} - the same text the compiler
     * copies into the ".mgs" header, but readable before there is one.
     */
    private MetaData mmlMetaData() {
        MetaData md = new MetaData();
        if (this.srcBuf == null) {
            return md;
        }
        String mml = new String(this.srcBuf, Charset.forName("MS932"));
        Matcher matcher = Pattern.compile("^[ \\t]*#title[ \\t]*\\{([^}]*)}", Pattern.MULTILINE).matcher(mml);
        if (matcher.find()) {
            List<String> lines = Arrays.stream(matcher.group(1).split("\\R"))
                    .map(l -> l.strip().replaceAll("^\"|\"$", "").strip())
                    .filter(l -> !l.isEmpty())
                    .toList();
            if (!lines.isEmpty()) {
                md.set(Tag.Title, lines.getFirst());
                md.set(Tag.TitleJ, lines.getFirst());
            }
            if (lines.size() > 1) {
                md.set(Tag.Composer, lines.get(1));
                md.set(Tag.ComposerJ, lines.get(1));
            }
            md.setAll(Tag.Comments, lines);
        }
        return md;
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        MetaData metaData = getMetaData();
        music.title = metaData.getFirst(Tag.Title);
        music.titleJ = metaData.getFirst(Tag.TitleJ);
        music.game = metaData.getFirst(Tag.GameTitle);
        music.gameJ = metaData.getFirst(Tag.GameTitleJ);
        music.composer = metaData.getFirst(Tag.Composer);
        music.composerJ = metaData.getFirst(Tag.ComposerJ);
        music.vgmby = metaData.getFirst(Tag.Maker);

        music.converted = metaData.getFirst(Tag.Converter);
        music.notes = metaData.getFirst(Tag.Note);

        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(MGSPlugin.class);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {"DriverBalance_MGS.mbc", "/resources/DefaultVolumeBalance_MGS.xml"};
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("MGSDRV", "mgs");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("MGSDRV", "mgs");
    }

    @Override
    public int getMarkSize() {
        return SNIFF_SIZE; // ".mus" is shared with MUAP98, so accepts() has to see inside the file
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        String name = Path.of(SoundUtil.getSource(is)).toString().toLowerCase();
        if (Arrays.stream(getExtensions()).noneMatch(name::endsWith)) return false;
        return accepts(name, is.readNBytes(SNIFF_SIZE));
    }
}
