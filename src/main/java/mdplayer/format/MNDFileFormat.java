package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import mdplayer.PlayList;
import mdplayer.driver.mndrv.MnDriver;
import mdplayer.plugin.MNDPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;
import vavi.util.compat.Tuple;


/**
 * MNDRV (X68000) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MNDFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".mnd"};
    }

    @Override
    public MetaData getMetaData() {
        return new MnDriver().getMetaData(this.srcBuf);
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
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

        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public List<Tuple<String, byte[]>> getExtendFiles(byte[] srcBuf, Archive archive, Entry entry) {
        List<Tuple<String, byte[]>> ret = new ArrayList<>();
        byte[] buf;

        int hs = ((srcBuf[0x06] & 0xff) << 8) + (srcBuf[0x07] & 0xff);
        int[] pcmptr = new int[] {((srcBuf[0x14] & 0xff) << 24) + ((srcBuf[0x15] & 0xff) << 16) + ((srcBuf[0x16] & 0xff) << 8) + (srcBuf[0x17] & 0xff)};
        if (hs < 0x18) pcmptr[0] = 0;
        if (pcmptr[0] != 0) {
            int pcmnum = ((srcBuf[pcmptr[0]] & 0xff) << 8) + (srcBuf[pcmptr[0] + 1] & 0xff);
            pcmptr[0] += 2;
            for (int i = 0; i < pcmnum; i++) {
                String mndPcmFn = mdplayer.Common.getNRDString(srcBuf, pcmptr);
                buf = getExtendFileAllBytes(filename, mndPcmFn, archive, entry);
                if (buf != null) ret.add(new Tuple<>(".PND", buf));
            }
        }

        return ret;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_MND.mbc",
                Resources.getDefaultVolumeBalance_MND()
        };
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(MNDPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("MNDRV", "mnd");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("MNDRV", "mnd");
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
