package mdplayer.format;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import mdplayer.Common;
import mdplayer.M3U;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;


/**
 * LZHFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class LZHFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".lzh"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry /* = null */) {
        if (entry != null) return null;

        mc.arcFileName = mc.fileName;
        mc.arcType = Common.EnmArcType.LZH;
        List<String> zipMember = new ArrayList<>();
        List<PlayList.Music> mMember = new ArrayList<>();

        for (Entry e : archive.entries()) {
            if (!(FileFormat.getFileFormat(e.getName()) instanceof M3UFileFormat)) {
                zipMember.add(entry.getName());
            } else {
                PlayList pl = M3U.loadM3U(archive, e, mc.arcFileName);
                mMember.addAll(pl.getMusics());
            }
        }

        for (String zm : zipMember) {
            boolean found = false;
            for (PlayList.Music m : mMember) {
                if (m.fileName.equals(zm)) {
                    found = true;
                    break;
                }
            }
            if (!found && FileFormat.getFileFormat(zm) instanceof VGMFileFormat) {
                String vzm;
                if (Path.getExtension(zm).equalsIgnoreCase(".vgm")) vzm = Path.changeExtension(zm, ".vgz");
                else vzm = Path.changeExtension(zm, ".vgm");
                for (PlayList.Music m : mMember) {
                    if (m.fileName.equals(vzm)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                PlayList.Music zmc = new PlayList.Music();
                zmc.fileName = zm;
                zmc.arcFileName = mc.arcFileName;
                zmc.arcType = mc.arcType;
                mMember.add(zmc);
            }
        }

        List<PlayList.Music> musics = new ArrayList<>();

        for (Entry e : archive.entries()) {
            for (PlayList.Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (e.getName().equals(m.fileName) || e.getName().equals(vzm)) {
                    m.format = FileFormat.getFileFormat(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    musics.addAll(addFileLoop(m, archive, e));

                    // When multiple m3u files are included, there are cases where multiple files with the same name are added.
                    // To prevent this, we will break here.
                    break;
                }
            }
        }

        return musics;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return null;

        mc.arcFileName = mc.fileName;
        mc.arcType = Common.EnmArcType.LZH;
        List<String> zipMember = new ArrayList<>();
        List<PlayList.Music> mMember = new ArrayList<>();

        for (Entry ent : archive.entries()) {
            if (!(FileFormat.getFileFormat(ent.getName()) instanceof M3UFileFormat)) {
                zipMember.add(ent.getName());
            } else {
                PlayList pl = M3U.loadM3U(archive, ent, mc.arcFileName);
                mMember.addAll(pl.getMusics());
            }
        }

        for (String zm : zipMember) {
            boolean found = false;
            for (PlayList.Music m : mMember) {
                if (m.fileName.equals(zm)) {
                    found = true;
                    break;
                }
            }
            if (!found && FileFormat.getFileFormat(zm) instanceof VGMFileFormat) {
                String vzm;
                if (Path.getExtension(zm).equalsIgnoreCase(".vgm")) vzm = Path.changeExtension(zm, ".vgz");
                else vzm = Path.changeExtension(zm, ".vgm");
                for (PlayList.Music m : mMember) {
                    if (m.fileName.equals(vzm)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                PlayList.Music zmc = new PlayList.Music();
                zmc.fileName = zm;
                zmc.arcFileName = mc.arcFileName;
                zmc.arcType = mc.arcType;
                mMember.add(zmc);
            }
        }

        List<PlayList.Music> musics = new ArrayList<>();

        for (Entry ent : archive.entries()) {
            for (PlayList.Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (ent.getName().equals(m.fileName) || ent.getName().equals(vzm)) {
                    m.format = FileFormat.getFileFormat(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    musics.addAll(addFileLoop(index, m, archive, ent));

                    // When multiple m3u files are included, there are cases where multiple files with the same name are added.
                    // To prevent this, we will break here.
                    break;
                }
            }
        }
        return musics;
    }

    @Override
    public void load(String archiveFilename, String fn) throws IOException {
        FileFormat format = FileFormat.getFileFormat(fn);
        if (format != FileFormat.unknown) {
            Archive archive = Archives.getArchive(new java.io.File(archiveFilename));
            Entry entry = archive.getEntry(fn);
            byte[] srcBuf = archive.getInputStream(entry).readAllBytes();
            this.extendFiles = ((BaseFileFormat) format).getExtendFiles(fn, srcBuf, archive, entry);
        } else {
            throw new FileNotFoundException(fn);
        }
    }

    private static final byte[] magic = {0x2D, 0x6C, 0x68}; // TODO more?

    @Override
    public int getMarkSize() {
        return magic.length;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        byte[] buf = new byte[getMarkSize()];
        is.readNBytes(buf, 0, buf.length);
        return Arrays.equals(magic, buf);
    }
}
