package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.nsf.Nsf;
import mdplayer.plugin.NSFPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
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
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();
        Nsf nsf = new Nsf();
        Vgm.Gd3 gd3 = nsf.getGD3Info(buf);

        if (gd3 != null) {
            for (int s = 0; s < nsf.songs; s++) {
                music = new PlayList.Music();
                music.format = this;
                music.fileName = file;
                music.arcFileName = zipFile;
                music.arcType = EnmArcType.unknown;
                if (zipFile != null && zipFile.isEmpty())
                    music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
                music.title = String.format("%s - Trk %d", gd3.gameName, s + 1);
                music.titleJ = String.format("%s - Trk %d", gd3.gameNameJ, s + 1);
                music.game = gd3.gameName;
                music.gameJ = gd3.gameNameJ;
                music.composer = gd3.composer;
                music.composerJ = gd3.composerJ;
                music.vgmby = gd3.vgmBy;
                music.converted = gd3.converted;
                music.notes = gd3.notes;
                music.songNo = s;

                musics.add(music);
            }
        } else {
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.game = "unknown";
            music.type = "-";
            music.title = String.format("(%s)", Path.getFileName(file));
            musics.add(music);
        }

        return musics;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();
        Nsf nsf = new Nsf();
        Vgm.Gd3 gd3 = nsf.getGD3Info(buf);

        if (gd3 != null) {
            if (ms.songNo == -1) {
                for (int s = 0; s < nsf.songs; s++) {
                    music = new PlayList.Music();
                    music.format = this;
                    music.fileName = ms.fileName;
                    music.arcFileName = zipFile;
                    music.title = String.format("%s - Trk %d", gd3.gameName, s);
                    music.titleJ = String.format("%s - Trk %d", gd3.gameNameJ, s);
                    music.game = gd3.gameName;
                    music.gameJ = gd3.gameNameJ;
                    music.composer = gd3.composer;
                    music.composerJ = gd3.composerJ;
                    music.vgmby = gd3.vgmBy;
                    music.converted = gd3.converted;
                    music.notes = gd3.notes;
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
                music.game = gd3.gameName;
                music.gameJ = gd3.gameNameJ;
                music.composer = gd3.composer;
                music.composerJ = gd3.composerJ;
                music.vgmby = gd3.vgmBy;
                music.converted = gd3.converted;
                music.notes = gd3.notes;
                music.songNo = ms.songNo;
            }
        } else {
            music.format = this;
            music.fileName = ms.fileName;
            music.arcFileName = zipFile;
            music.game = "unknown";
            music.type = "-";
            music.title = String.format("(%s)", Path.getFileName(ms.fileName));
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
        return new NSFPlugin();
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
            if (musics.size() > 0) {
                music = musics.get(0);
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
            if (musics.size() > 0) {
                music = musics.get(0);
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
}
