package mdplayer.format;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Common;
import mdplayer.Common.EnmArcType;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.driver.Vgm;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;
import vavi.util.archive.zip.JdkZipEntry;


/**
 * UnknownFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class UnknownFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        music.fileName = file;
        music.arcFileName = zipFile;
        music.arcType = EnmArcType.unknown;
        if (zipFile != null && !zipFile.isEmpty())
            music.arcType = zipFile.toLowerCase().lastIndexOf(".zip") != -1 ? EnmArcType.ZIP : EnmArcType.LZH;
        music.title = "unknown";
        music.game = "unknown";
        music.type = "-";

        if (buf.length < 0x40) {
            musics.add(music);
            return musics;
        }
        if (Common.getLE32(buf, 0x00) != Vgm.FCC_VGM) {
            // musics.add(Music);
            // return musics;
            // VGZかもしれないので確認する
            try {
                int num;
                buf = new byte[1024]; // 1Kbytesずつ処理する

                if (entry == null || entry instanceof JdkZipEntry) {
                    if (archive == null && entry == null) {
                        archive = Archives.getArchive(new java.io.File(zipFile));
                        entry = archive.getEntry(file);
                    }
                    try (InputStream inStream = archive.getInputStream(entry);
                         InputStream decompStream = Archives.getInputStream(inStream);
                         ByteArrayOutputStream outStream = new ByteArrayOutputStream()
                    ) {
                        while ((num = decompStream.read(buf, 0, buf.length)) > 0) {
                            outStream.write(buf, 0, num);
                        }
                        buf = outStream.toByteArray();
                    }
                } else {
                    buf = archive.getInputStream(entry).readAllBytes();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // vgzではなかった
            }
        }

        if (Common.getLE32(buf, 0x00) != Vgm.FCC_VGM) {
            musics.add(music);
            return musics;
        }

        music.format = null; // TODO VGM
        int version = Common.getLE32(buf, 0x08);
        String _version = String.format("%d.%d%d", (version & 0xf00) / 0x100, (version & 0xf0) / 0x10, (version & 0xf));

        int vgmGd3 = Common.getLE32(buf, 0x14);
        Vgm.Gd3 gd3 = new Vgm.Gd3();
        if (vgmGd3 != 0) {
            int vgmGd3Id = Common.getLE32(buf, vgmGd3 + 0x14);
            if (vgmGd3Id != Vgm.FCC_GD3) {
                musics.add(music);
                return musics;
            }
            gd3 = (new Vgm()).getGD3Info(buf, vgmGd3);
        }

        int TotalCounter = Common.getLE32(buf, 0x18);
        int vgmLoopOffset = Common.getLE32(buf, 0x1c);
        int loopCounter = Common.getLE32(buf, 0x20);

        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        double sec = (double) TotalCounter / (double) Setting.getInstance().getOutputDevice().getSampleRate();
        int TCminutes = (int) (sec / 60);
        sec -= TCminutes * 60;
        int TCsecond = (int) sec;
        sec -= TCsecond;
        int TCmillisecond = (int) (sec * 100.0);
        music.duration = String.format("%2d:%2d:%2d", TCminutes, TCsecond, TCmillisecond);

        return musics;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = this;
        music.fileName = ms.fileName;
        music.arcFileName = zipFile;
        music.title = "unknown";
        music.game = "unknown";
        music.type = "-";

        musics.add(music);
        return musics;
    }

    @Override
    public Plugin getPlugin() {
        return null; // TODO
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry) {
        return null;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry) {
        return null;
    }
}
