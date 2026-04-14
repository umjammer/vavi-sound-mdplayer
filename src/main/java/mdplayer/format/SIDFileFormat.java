package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.File;
import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.driver.sid.SidDriver;
import mdplayer.driver.sid.SidMdDriver2;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.SIDPlugin;
import mdplayer.properties.Resources;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * SID (Commodore) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class SIDFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".sid"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();

        SidDriver sid = new SidMdDriver2();
        MetaData metaData = sid.getMetaData(buf);
        for (int s = 0; s < sid.getSongs(); s++) {
            PlayList.Music music = new PlayList.Music();
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.arcType = EnmArcType.unknown;
            if (zipFile != null && zipFile.isEmpty())
                music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
            music.title = "%s - Trk %d".formatted(metaData.getFirst(Tag.Title), s + 1);
            music.titleJ = "%s - Trk %d".formatted(metaData.getFirst(Tag.Title), s + 1);
            music.game = "";
            music.gameJ = "";
            music.composer = metaData.getFirst(Tag.Composer);
            music.composerJ = metaData.getFirst(Tag.Composer);
            music.vgmby = "";
            music.converted = "";
            music.notes = metaData.getFirst(Tag.Note);
            music.songNo = s;

            musics.add(music);
        }

        return musics;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_SID.mbc",
                Resources.getDefaultVolumeBalance_SID()
        };
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(SIDPlugin.class);
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry /* = null */) throws IOException {
        byte[] buf;
        if (entry == null) {
            buf = File.readAllBytes(mc.fileName);
        } else {
            try (InputStream reader = archive.getInputStream(entry)) {
                buf = reader.readAllBytes();
            }
        }

        List<PlayList.Music> musics;
        if (entry == null) musics = getMusic(mc.fileName, buf, null, null, null);
        else musics = getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

        if (mc.songNo != -1) {
            PlayList.Music music;
            if (!musics.isEmpty()) {
                music = musics.getFirst();
                music.songNo = mc.songNo;
                music.title = mc.title;
                music.titleJ = mc.titleJ;

                musics.clear();
                musics.add(music);
            } else {
                musics.clear();
            }
        }

        return musics;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry/* = null*/) throws IOException {
        byte[] buf;
        if (entry == null) {
            buf = File.readAllBytes(mc.fileName);
        } else {
            try (InputStream reader = archive.getInputStream(entry)) {
                buf = reader.readAllBytes();
            }
        }

        List<PlayList.Music> musics;
        if (entry == null) musics = getMusic(mc.fileName, buf, null, null, null);
        else musics = getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

        if (mc.songNo != -1) {
            PlayList.Music music;
            if (!musics.isEmpty()) {
                music = musics.getFirst();
                music.songNo = mc.songNo;
                music.title = mc.title;
                music.titleJ = mc.titleJ;

                musics.clear();
                musics.add(music);
            } else {
                musics.clear();
            }
        }

        return musics;
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("SID", "sid");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("SID", "sid");
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
