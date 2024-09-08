package mdplayer.format;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.M3U;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;


/**
 * ZIPFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class ZIPFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() {
        return new String[] {".zip"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile/* = null*/, Archive archive, Entry entry/* = null*/) {
        PlayList.Music music = new PlayList.Music();
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return null;

        mc.arcFileName = mc.fileName;
        mc.arcType = Common.EnmArcType.ZIP;
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

        List<PlayList.Music> tMember = new ArrayList<>();

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
                    tMember.addAll(addFileLoop(m, archive, ent));

                    // m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    // それを防ぐためここでbreakする
                    break;
                }
            }
        }

        return tMember;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return null;

        mc.arcFileName = mc.fileName;
        mc.arcType = Common.EnmArcType.ZIP;
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

        List<PlayList.Music> tMember = new ArrayList<>();

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
                    tMember.addAll(addFileLoop(index, m, archive, ent));

                    // m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    // それを防ぐためここでbreakする
                    break;
                }
            }
        }

        return tMember;
    }

    @Override
    public Tuple<byte[], List<Tuple<String, byte[]>>> load(String archiveFilename, String fn) throws IOException {
        Archive archive = Archives.getArchive(new java.io.File(archiveFilename));
        Entry entry = archive.getEntry(fn);

        FileFormat format = FileFormat.getFileFormat(fn);
        if (format != FileFormat.unknown) {
            String[] arcFn = new String[1];
            byte[] srcBuf = getBytesFromZipFile(archive, entry, arcFn);
            if (!arcFn[0].isEmpty()) fn = arcFn[0];
            List<Tuple<String, byte[]>> extFile = format.getExtendFile(fn, srcBuf, archive, entry);
            return new Tuple<>(srcBuf, extFile);
        } else {
            throw new FileNotFoundException(fn);
        }
    }
}
