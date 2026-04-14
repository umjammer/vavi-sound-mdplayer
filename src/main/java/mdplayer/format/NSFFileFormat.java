package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.driver.nsf.NsfDriver;
import mdplayer.driver.nsf.NsfMdDriver2;
import mdplayer.plugin.NSFPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * NSFFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class NSFFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".nsf"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        NsfDriver nsf = new NsfMdDriver2();
        MetaData md = nsf.getMetaData(buf);

        if (md != null) {
            for (int s = 0; s < nsf.getSongs(); s++) {
                music = new PlayList.Music();
                music.format = this;
                music.fileName = file;
                music.arcFileName = zipFile;
                music.arcType = EnmArcType.unknown;
                if (zipFile != null && zipFile.isEmpty())
                    music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
                music.title = "%s - Trk %d".formatted(md.getFirst(Tag.Title), s + 1);
                music.titleJ = "%s - Trk %d".formatted(md.getFirst(Tag.TitleJ), s + 1);
                music.game = md.getFirst(Tag.GameTitle);
                music.gameJ = md.getFirst(Tag.GameTitleJ);
                music.composer = md.getFirst(Tag.Composer);
                music.composerJ = md.getFirst(Tag.ComposerJ);
                music.vgmby = md.getFirst(Tag.Maker);
                music.converted = md.getFirst(Tag.Converter);
                music.notes = md.getFirst(Tag.Note);
                music.songNo = s;

                musics.add(music);
            }
        } else {
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.game = "unknown";
            music.type = "-";
            music.title = "(%s)".formatted(Path.getFileName(file));
            musics.add(music);
        }

        return musics;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        NsfDriver nsf = new NsfMdDriver2();
        MetaData md = nsf.getMetaData(buf);

        if (md != null) {
            if (ms.songNo == -1) {
                for (int s = 0; s < nsf.getSongs(); s++) {
                    music = new PlayList.Music();
                    music.format = this;
                    music.fileName = ms.fileName;
                    music.arcFileName = zipFile;
                    music.title = "%s - Trk %d".formatted(md.getFirst(Tag.GameTitle), s);
                    music.titleJ = "%s - Trk %d".formatted(md.getFirst(Tag.GameTitleJ), s);
                    music.game = md.getFirst(Tag.GameTitle);
                    music.gameJ = md.getFirst(Tag.GameTitleJ);
                    music.composer = md.getFirst(Tag.Composer);
                    music.composerJ = md.getFirst(Tag.ComposerJ);
                    music.vgmby = md.getFirst(Tag.Maker);
                    music.converted = md.getFirst(Tag.Converter);
                    music.notes = md.getFirst(Tag.Note);
                    music.songNo = s;

                    musics.add(music);
                }

                return musics;

            } else {
                music.format = this;
                music.fileName = ms.fileName;
                music.arcFileName = zipFile;
                music.title = ms.title;
                music.titleJ = ms.titleJ;
                music.game = md.getFirst(Tag.GameTitle);
                music.gameJ = md.getFirst(Tag.GameTitleJ);
                music.composer = md.getFirst(Tag.Composer);
                music.composerJ = md.getFirst(Tag.ComposerJ);
                music.vgmby = md.getFirst(Tag.Maker);
                music.converted = md.getFirst(Tag.Converter);
                music.notes = md.getFirst(Tag.Note);
                music.songNo = ms.songNo;
            }
        } else {
            music.format = this;
            music.fileName = ms.fileName;
            music.arcFileName = zipFile;
            music.game = "unknown";
            music.type = "-";
            music.title = "(%s)".formatted(Path.getFileName(ms.fileName));
        }

        musics.add(music);
        return musics;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_NSF.mbc",
                Resources.getDefaultVolumeBalance_NSF()
        };
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(NSFPlugin.class);
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry/* = null*/) throws IOException {
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
        return new MdEncoding("NSF", "nsf");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("NSF", "nsf");
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
