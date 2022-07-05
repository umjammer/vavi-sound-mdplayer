package mdplayer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import dotnet4j.util.compat.Tuple;
import dotnet4j.io.File;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.StreamReader;
import mdplayer.Common.EnmArcType;
import mdplayer.Common.FileFormat;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;
import vavi.util.archive.zip.ZipEntry;
import vavi.util.serdes.Serdes;


public class PlayList implements Serializable {

    public static class Music {
        public FileFormat format;
        public String playingNow;
        public String fileName;
        public String arcFileName;
        public EnmArcType arcType = EnmArcType.unknown;
        public String type = "-";

        public String title;
        public String game;
        public String system;
        public String composer;
        public String titleJ;
        public String gameJ;
        public String systemJ;
        public String composerJ;

        public String converted;
        public String notes;
        public String vgmby;
        public String remark;
        public String duration;

        public String time = "";
        public String loopStartTime = "";
        public String loopEndTime = "";
        public String fadeoutTime = "";
        public int loopCount = -1;

        public int songNo = -1;
    }

    private List<Music> musics = new ArrayList<>();

    public List<Music> getMusics() {
        return musics;
    }

    public void setMusics(List<Music> value) {
        musics = value;
    }

    public PlayList copy() {
        PlayList playList = new PlayList();

        return playList;
    }

    public void save(String fileName) {
        String fullPath;

        if (fileName == null || fileName.isEmpty()) {
            fullPath = Common.settingFilePath;
            fullPath = Path.getFullPath("DefaultPlayList.xml");
        } else {
            fullPath = fileName;
        }

        try (ObjectOutputStream sw = new ObjectOutputStream(Files.newOutputStream(Paths.get(fullPath)))) {
            sw.writeObject(this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveM3U(String fileName) {
        String basePath = Path.getDirectoryName(fileName);

        try (PrintWriter sw = new PrintWriter(new FileWriter(fileName))) {
            for (Music ms : this.musics) {
                String path = Path.getDirectoryName(ms.fileName);
                if (path.equals(basePath)) {
                    sw.println(Path.getFileName(ms.fileName));
                } else {
                    sw.println(ms.fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PlayList Load(String fileName) {
        try {
            String fullPath;
            if (fileName == null || fileName.isEmpty()) {
                fullPath = Common.settingFilePath;
                fullPath = Path.getFullPath("DefaultPlayList.xml");
            } else {
                fullPath = fileName;
            }

            try (InputStream sr = Files.newInputStream(java.nio.file.Path.of(fullPath))) {
                PlayList pl = new PlayList();
                Serdes.Util.deserialize(sr, pl);
                return pl;
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            return new PlayList();
        }
    }

    public static PlayList LoadM3U(String filename) {
        try {
            PlayList pl = new PlayList();

            try (StreamReader sr = new StreamReader(new FileStream(filename, FileMode.Open), Charset.forName("MS932"))) {
                String line;
                while ((line = sr.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if (line.charAt(0) == '#') continue;

                    if (!Path.isPathRooted(line)) {
                        line = Path.combine(Path.getDirectoryName(filename), line);
                    }
                    Music ms = new Music();
                    ms.fileName = line;
                    pl.musics.add(ms);
                }
            }


            return pl;
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            return new PlayList();
        }
    }

    public List<Object[]> makeRow(List<Music> musics) {
        List<Object[]> ret = new ArrayList<>();

        for (Music music : musics) {
            Object[] row = new Object[] {
                " ", // clmPlayingNow
                0, // clmKey
                music.fileName, // clmFileName
                music.arcFileName, // clmZipFileName
                Path.getFileName(music.fileName), // clmDispFileName
                music.fileName, // clmDispFileName
                Path.getExtension(music.fileName).toUpperCase(), // clmEXT
                music.type, // clmType
                music.title, // clmTitle
                music.titleJ, // clmTitleJ
                music.game, // clmGame
                music.gameJ, // clmGameJ
//                music.remark, // clmRemark
                music.composer, // clmComposer
                music.composerJ, // clmComposerJ
                music.converted, // clmConverted
                music.notes, // clmNotes
                music.duration, // clmDuration
                music.vgmby, // clmVGMby
                music.songNo, // clmSongNo
            };
            ret.add(row);
        }
        return ret;
    }

    private String rootPath = "";
    private JTable dgvList;

    public void SetDGV(JTable dgv) {
        dgvList = dgv;
    }

    public void AddFile(String filename) {
        try {
            Music mc = new Music();
            mc.format = Common.FileFormat.checkExt(filename);
            mc.fileName = filename;
            rootPath = Path.getDirectoryName(filename);

            addFileLoop(mc, null, null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, String.format("ファイル追加に失敗しました。\n詳細\nMessage=%s", ex.getMessage())
                    , "エラー"
                    , JOptionPane.ERROR_MESSAGE);
            Log.forcedWrite(ex);
        }
    }

    public void InsertFile(int index, String[] filenames) {
        try {
            for (String filename : filenames) {
                Music mc = new Music();
                mc.format = FileFormat.checkExt(filename);
                mc.fileName = filename;
                rootPath = Path.getDirectoryName(filename);

                addFileLoop(index, mc, null, null);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, String.format("ファイル追加に失敗しました。\n詳細\nMessage=%s", ex.getMessage())
                    , "エラー"
                    , JOptionPane.ERROR_MESSAGE);
            Log.forcedWrite(ex);
        }
    }

    private void addFileLoop(Music mc, Archive archive, Entry entry/* = null*/) {
        switch (mc.format) {
        case unknown:
            break;
        case M3U:
            addFileM3U(mc, archive, entry);
            break;
        case MID:
            addFileMID(mc, archive, entry);
            break;
        case NRT:
            addFileNRT(mc, archive, entry);
            break;
        case NSF:
            addFileNSF(mc, archive, entry);
            break;
        case HES:
            addFileHES(mc, archive, entry);
            break;
        case SID:
            addFileSID(mc, archive, entry);
            break;
        case MDR:
            addFileMDR(mc, archive, entry);
            break;
        case MND:
            addFileMND(mc, archive, entry);
            break;
        case MDX:
            addFileMDX(mc, archive, entry);
            break;
        case MUB:
            addFileMUB(mc, archive, entry);
            break;
        case MUC:
            addFileMUC(mc, archive, entry);
            break;
        case MML:
            addFileMML(mc, archive, entry);
            break;
        case MGS:
            addFileMML(mc, archive, entry);
            break;
        case M:
            addFileM(mc, archive, entry);
            break;
        case RCP:
            addFileRCP(mc, archive, entry);
            break;
        case S98:
            addFileS98(mc, archive, entry);
            break;
        case VGM:
            addFileVGM(mc, archive, entry);
            break;
        case XGM:
            addFileXGM(mc, archive, entry);
            break;
        case ZGM:
            addFileZGM(mc, archive, entry);
            break;
        case ZIP:
            addFileZIP(mc, archive, entry);
            break;
        case LZH:
            addFileLZH(mc, archive, entry);
            break;
        case WAV:
            addFileWAV(mc, archive, entry);
            break;
        case MP3:
            addFileMP3(mc, archive, entry);
            break;
        case AIFF:
            addFileAIFF(mc, archive, entry);
            break;
        }
    }

    private void addFileLoop(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        switch (mc.format) {
        case unknown:
            break;
        case MID:
            addFileMID(index, mc, archive, entry);
            break;
        case NRT:
            addFileNRT(index, mc, archive, entry);
            break;
        case MDR:
            addFileMDR(index, mc, archive, entry);
            break;
        case MND:
            addFileMND(index, mc, archive, entry);
            break;
        case MDX:
            addFileMDX(index, mc, archive, entry);
            break;
        case MUB:
            addFileMUB(index, mc, archive, entry);
            break;
        case MUC:
            addFileMUC(index, mc, archive, entry);
            break;
        case MML:
            addFileMML(index, mc, archive, entry);
            break;
        case MGS:
            addFileMGS(index, mc, archive, entry);
            break;
        case M:
            addFileM(index, mc, archive, entry);
            break;
        case RCP:
            addFileRCP(index, mc, archive, entry);
            break;
        case S98:
            addFileS98(index, mc, archive, entry);
            break;
        case VGM:
            addFileVGM(index, mc, archive, entry);
            break;
        case XGM:
            addFileXGM(index, mc, archive, entry);
            break;
        case ZGM:
            addFileZGM(index, mc, archive, entry);
            break;
        case WAV:
            addFileWAV(index, mc, archive, entry);
            break;
        case MP3:
            addFileMP3(index, mc, archive, entry);
            break;
        case AIFF:
            addFileAIFF(index, mc, archive, entry);
            break;
        case ZIP:
            addFileZIP(index, mc, archive, entry);
            break;
        case LZH:
            addFileLZH(index, mc, archive, entry);
            break;
        case M3U:
            addFileM3U(index, mc, archive, entry);
            break;
        case NSF:
            addFileNSF(index, mc, archive, entry);
            break;
        case HES:
            addFileHES(index, mc, archive, entry);
            break;
        case SID:
            addFileSID(index, mc, archive, entry);
            break;
        }
    }

    /**
     * 汎用
     */
    private void addFileXxx(Music mc, Archive archive, Entry entry/*=null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                try {
                    buf = File.readAllBytes(mc.fileName);
                } catch (Exception ex) {
                    Log.forcedWrite(ex);
                    buf = null;
                }
                if (buf == null && mc.format == FileFormat.VGM) {
                    if (Path.getExtension(mc.fileName).equalsIgnoreCase(".vgm")) {
                        mc.fileName = Path.changeExtension(mc.fileName, ".vgz");
                    } else {
                        mc.fileName = Path.changeExtension(mc.fileName, ".Vgm");
                    }
                    try {
                        buf = File.readAllBytes(mc.fileName);
                    } catch (Exception ex) {
                        Log.forcedWrite(ex);
                        buf = null;
                    }
                }
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    try {
                        buf = reader.readAllBytes();
                    } catch (Exception ex) {
                        Log.forcedWrite(ex);
                        buf = null;
                    }
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);
            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).addRow(row);
            this.musics.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileXxx(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                try {
                    buf = File.readAllBytes(mc.fileName);
                } catch (Exception ex) {
                    Log.forcedWrite(ex);
                    buf = null;
                }
                if (buf == null && mc.format == FileFormat.VGM) {
                    if (Path.getExtension(mc.fileName).equalsIgnoreCase(".vgm")) {
                        mc.fileName = Path.changeExtension(mc.fileName, ".vgz");
                    } else {
                        mc.fileName = Path.changeExtension(mc.fileName, ".Vgm");
                    }
                    try {
                        buf = File.readAllBytes(mc.fileName);
                    } catch (Exception ex) {
                        Log.forcedWrite(ex);
                        buf = null;
                    }
                }
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    try {
                        buf = reader.readAllBytes();
                    } catch (Exception ex) {
                        Log.forcedWrite(ex);
                        buf = null;
                    }
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);
            List<Object[]> rows = makeRow(musics);

            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
            this.musics.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileMID(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMID(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileNRT(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileNRT(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileRCP(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileRCP(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileS98(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileS98(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileVGM(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileVGM(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileXGM(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileXGM(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileZGM(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileZGM(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMDR(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMDR(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMND(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMND(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMDX(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMDX(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMUB(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMUB(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMUC(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMUC(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMML(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMML(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileM(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileM(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMGS(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMGS(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileWAV(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileWAV(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileMP3(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileMP3(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileAIFF(Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(mc, archive, entry);
    }

    private void addFileAIFF(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        addFileXxx(index, mc, archive, entry);
    }

    private void addFileZIP(Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return;

        mc.arcFileName = mc.fileName;
        mc.arcType = EnmArcType.ZIP;
        List<String> zipMember = new ArrayList<>();
        List<Music> mMember = new ArrayList<>();
        for (Entry ent : archive.entries()) {
            if (Common.FileFormat.checkExt(ent.getName()) != FileFormat.M3U) {
                zipMember.add(ent.getName());
            } else {
                PlayList pl = M3U.LoadM3U(archive, ent, mc.arcFileName);
                mMember.addAll(pl.musics);
            }
        }

        for (String zm : zipMember) {
            boolean found = false;
            for (Music m : mMember) {
                if (m.fileName.equals(zm)) {
                    found = true;
                    break;
                }
            }
            if (!found && Common.FileFormat.checkExt(zm) == FileFormat.VGM) {
                String vzm;
                if (Path.getExtension(zm).equalsIgnoreCase(".vgm")) vzm = Path.changeExtension(zm, ".vgz");
                else vzm = Path.changeExtension(zm, ".vgm");
                for (Music m : mMember) {
                    if (m.fileName.equals(vzm)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Music zmc = new Music();
                zmc.fileName = zm;
                zmc.arcFileName = mc.arcFileName;
                zmc.arcType = mc.arcType;
                mMember.add(zmc);
            }
        }

        List<Music> tMember = new ArrayList<>();

        for (Entry ent : archive.entries()) {
            for (Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (ent.getName().equals(m.fileName) || ent.getName().equals(vzm)) {
                    m.format = Common.FileFormat.checkExt(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    addFileLoop(m, archive, ent);

                    //m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    //それを防ぐためここでbreakする
                    break;
                }
            }
        }
    }

    private void addFileZIP(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return;

        mc.arcFileName = mc.fileName;
        mc.arcType = EnmArcType.ZIP;
        List<String> zipMember = new ArrayList<>();
        List<Music> mMember = new ArrayList<>();
        for (Entry ent : archive.entries()) {
            if (Common.FileFormat.checkExt(ent.getName()) != FileFormat.M3U) {
                zipMember.add(ent.getName());
            } else {
                PlayList pl = M3U.LoadM3U(archive, ent, mc.arcFileName);
                mMember.addAll(pl.musics);
            }
        }

        for (String zm : zipMember) {
            boolean found = false;
            for (Music m : mMember) {
                if (m.fileName.equals(zm)) {
                    found = true;
                    break;
                }
            }
            if (!found && Common.FileFormat.checkExt(zm) == FileFormat.VGM) {
                String vzm;
                if (Path.getExtension(zm).equalsIgnoreCase(".vgm")) vzm = Path.changeExtension(zm, ".vgz");
                else vzm = Path.changeExtension(zm, ".vgm");
                for (Music m : mMember) {
                    if (m.fileName.equals(vzm)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Music zmc = new Music();
                zmc.fileName = zm;
                zmc.arcFileName = mc.arcFileName;
                zmc.arcType = mc.arcType;
                mMember.add(zmc);
            }
        }

        List<Music> tMember = new ArrayList<>();

        for (Entry ent : archive.entries()) {
            for (Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (ent.getName().equals(m.fileName) || ent.getName().equals(vzm)) {
                    m.format = Common.FileFormat.checkExt(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    addFileLoop(index, m, archive, ent);

                    //m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    //それを防ぐためここでbreakする
                    break;
                }
            }
        }
    }

    private void addFileLZH(Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return;

        mc.arcFileName = mc.fileName;
        mc.arcType = EnmArcType.LZH;
        List<String> zipMember = new ArrayList<>();
        List<Music> mMember = new ArrayList<>();

        for (Entry e : archive.entries()) {
            if (Common.FileFormat.checkExt(e.getName()) != FileFormat.M3U) {
                zipMember.add(entry.getName());
            } else {
                PlayList pl = M3U.LoadM3U(archive, e, mc.arcFileName);
                mMember.addAll(pl.musics);
            }
        }

        for (String zm : zipMember) {
            boolean found = false;
            for (Music m : mMember) {
                if (m.fileName.equals(zm)) {
                    found = true;
                    break;
                }
            }
            if (!found && Common.FileFormat.checkExt(zm) == FileFormat.VGM) {
                String vzm;
                if (Path.getExtension(zm).equalsIgnoreCase(".vgm")) vzm = Path.changeExtension(zm, ".vgz");
                else vzm = Path.changeExtension(zm, ".vgm");
                for (Music m : mMember) {
                    if (m.fileName.equals(vzm)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Music zmc = new Music();
                zmc.fileName = zm;
                zmc.arcFileName = mc.arcFileName;
                zmc.arcType = mc.arcType;
                mMember.add(zmc);
            }
        }

        for (Entry e : archive.entries()) {
            for (Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (e.getName().equals(m.fileName) || e.getName().equals(vzm)) {
                    m.format = Common.FileFormat.checkExt(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    addFileLoop(m, archive, e);

                    // m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    // それを防ぐためここでbreakする
                    break;
                }
            }
        }
    }

    private void addFileLZH(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        if (entry != null) return;

        mc.arcFileName = mc.fileName;
        mc.arcType = EnmArcType.LZH;
        List<String> zipMember = new ArrayList<>();
        List<Music> mMember = new ArrayList<>();

        for (Entry ent : archive.entries()) {
            if (Common.FileFormat.checkExt(ent.getName()) != FileFormat.M3U) {
                zipMember.add(ent.getName());
            } else {
                PlayList pl = M3U.LoadM3U(archive, ent, mc.arcFileName);
                mMember.addAll(pl.musics);
            }
        }

        for (String zm : zipMember) {
            boolean found = false;
            for (Music m : mMember) {
                if (m.fileName.equals(zm)) {
                    found = true;
                    break;
                }
            }
            if (!found && Common.FileFormat.checkExt(zm) == FileFormat.VGM) {
                String vzm;
                if (Path.getExtension(zm).equalsIgnoreCase(".vgm")) vzm = Path.changeExtension(zm, ".vgz");
                else vzm = Path.changeExtension(zm, ".vgm");
                for (Music m : mMember) {
                    if (m.fileName.equals(vzm)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Music zmc = new Music();
                zmc.fileName = zm;
                zmc.arcFileName = mc.arcFileName;
                zmc.arcType = mc.arcType;
                mMember.add(zmc);
            }
        }

        for (Entry ent : archive.entries()) {
            for (Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (ent.getName().equals(m.fileName) || ent.getName().equals(vzm)) {
                    m.format = Common.FileFormat.checkExt(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    addFileLoop(index, m, archive, ent);

                    // m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    // それを防ぐためここでbreakする
                    break;
                }
            }
        }
    }

    private void addFileM3U(Music mc, Archive archive, Entry entry/* = null*/) {

        PlayList pl;
        if (entry == null) pl = M3U.LoadM3U(mc.fileName, rootPath);
        else pl = M3U.LoadM3U(archive, entry, mc.arcFileName);
        if (pl == null) return;
        if (pl.musics == null || pl.musics.size() < 1) return;

        for (Music m : pl.musics) addFileLoop(m, archive, entry);
    }

    private void addFileM3U(int index, Music mc, Archive archive, Entry entry/* = null*/) {

        PlayList pl;
        if (entry == null) pl = M3U.LoadM3U(mc.fileName, rootPath);
        else pl = M3U.LoadM3U(archive, entry, mc.arcFileName);
        if (pl == null) return;
        if (pl.musics == null || pl.musics.size() < 1) return;

        for (Music m : pl.musics) addFileLoop(index, m, archive, entry);
    }

    private void addFileNSF(Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                    try (InputStream reader = archive.getInputStream(entry)) {
                        buf = reader.readAllBytes();
                    }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            if (mc.songNo != -1) {
                Music music;
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

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows) ((DefaultTableModel) dgvList.getModel()).addRow(row);
            this.musics.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileNSF(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    buf = reader.readAllBytes();
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            if (mc.songNo != -1) {
                Music music;
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

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
            this.musics.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileHES(Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    buf = reader.readAllBytes();
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            if (mc.songNo != -1) {
                Music music;
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

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows) ((DefaultTableModel) dgvList.getModel()).addRow(row);
            this.musics.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileHES(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    buf = reader.readAllBytes();
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            if (mc.songNo != -1) {
                Music music;
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

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
            this.musics.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileSID(Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    buf = reader.readAllBytes();
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            if (mc.songNo != -1) {
                Music music;
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

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows) ((DefaultTableModel) dgvList.getModel()).addRow(row);
            this.musics.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileSID(int index, Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                try (InputStream reader = archive.getInputStream(entry)) {
                    buf =   reader.readAllBytes();
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            if (mc.songNo != -1) {
                Music music;
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

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
            this.musics.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }
}
