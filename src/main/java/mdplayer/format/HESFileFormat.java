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
import mdplayer.driver.hes.Hes;
import mdplayer.plugin.HESPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * HESFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class HESFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".hes"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        Hes hes = new Hes();
        Vgm.Gd3 gd3 = hes.getGD3Info(buf);

        for (int s = 0; s < 256; s++) {
            PlayList.Music music = new PlayList.Music();
            music.format = this;
            music.fileName = file;
            music.arcFileName = zipFile;
            music.arcType = EnmArcType.unknown;
            if (zipFile != null && zipFile.isEmpty())
                music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
            music.title = String.format("%s - Trk %d", Path.getFileName(file), s + 1);
            music.titleJ = String.format("%s - Trk %d", Path.getFileName(file), s + 1);
            music.game = "";
            music.gameJ = "";
            music.composer = "";
            music.composerJ = "";
            music.vgmby = "";
            music.converted = "";
            music.notes = "";
            music.songNo = s;

            musics.add(music);
        }
        return musics;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_HES.mbc",
                Resources.getDefaultVolumeBalance_HES()
        };
    }

    @Override
    public Plugin getPlugin() {
        return new HESPlugin();
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
