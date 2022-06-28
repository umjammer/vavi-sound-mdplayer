package mdplayer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

import com.sun.tools.javac.file.ZipArchive;
import dotnet4j.Tuple;
import dotnet4j.io.File;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.StreamReader;
import mdplayer.Common.EnmArcType;
import mdplayer.Common.FileFormat;
import vavi.util.archive.Archive;
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

    private List<Music> _lstMusic = new ArrayList<>();

    public List<Music> getLstMusic() {
        return _lstMusic;
    }

    public void setLstMusic(List<Music> value) {
        _lstMusic = value;
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
            for (Music ms : this._lstMusic) {
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
                    pl._lstMusic.add(ms);
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

            addFileLoop(mc, null);
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(null, String.format("ファイル追加に失敗しました。\n詳細\nMessage=%s", ex.getMessage())
                    , "エラー"
                    , JOptionPane.YES_NO_OPTION
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

                addFileLoop(index, mc, null);
            }
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(null, String.format("ファイル追加に失敗しました。\n詳細\nMessage=%s", ex.getMessage())
                    , "エラー"
                    , JOptionPane.YES_NO_OPTION
                    , JOptionPane.ERROR_MESSAGE);
            Log.forcedWrite(ex);
        }
    }

    private void addFileLoop(Music mc, Object entry/* = null*/) {
        switch (mc.format) {
        case unknown:
            break;
        case M3U:
            addFileM3U(mc, entry);
            break;
        case MID:
            addFileMID(mc, entry);
            break;
        case NRT:
            addFileNRT(mc, entry);
            break;
        case NSF:
            addFileNSF(mc, entry);
            break;
        case HES:
            addFileHES(mc, entry);
            break;
        case SID:
            addFileSID(mc, entry);
            break;
        case MDR:
            addFileMDR(mc, entry);
            break;
        case MND:
            addFileMND(mc, entry);
            break;
        case MDX:
            addFileMDX(mc, entry);
            break;
        case MUB:
            addFileMUB(mc, entry);
            break;
        case MUC:
            addFileMUC(mc, entry);
            break;
        case MML:
            addFileMML(mc, entry);
            break;
        case MGS:
            addFileMML(mc, entry);
            break;
        case M:
            addFileM(mc, entry);
            break;
        case RCP:
            addFileRCP(mc, entry);
            break;
        case S98:
            addFileS98(mc, entry);
            break;
        case VGM:
            addFileVGM(mc, entry);
            break;
        case XGM:
            addFileXGM(mc, entry);
            break;
        case ZGM:
            addFileZGM(mc, entry);
            break;
        case ZIP:
            addFileZIP(mc, entry);
            break;
        case LZH:
            addFileLZH(mc, entry);
            break;
        case WAV:
            addFileWAV(mc, entry);
            break;
        case MP3:
            addFileMP3(mc, entry);
            break;
        case AIFF:
            addFileAIFF(mc, entry);
            break;
        }
    }

    private void addFileLoop(int index, Music mc, Object entry/* = null*/) {
        switch (mc.format) {
        case unknown:
            break;
        case MID:
            addFileMID(index, mc, entry);
            break;
        case NRT:
            addFileNRT(index, mc, entry);
            break;
        case MDR:
            addFileMDR(index, mc, entry);
            break;
        case MND:
            addFileMND(index, mc, entry);
            break;
        case MDX:
            addFileMDX(index, mc, entry);
            break;
        case MUB:
            addFileMUB(index, mc, entry);
            break;
        case MUC:
            addFileMUC(index, mc, entry);
            break;
        case MML:
            addFileMML(index, mc, entry);
            break;
        case MGS:
            addFileMGS(index, mc, entry);
            break;
        case M:
            addFileM(index, mc, entry);
            break;
        case RCP:
            addFileRCP(index, mc, entry);
            break;
        case S98:
            addFileS98(index, mc, entry);
            break;
        case VGM:
            addFileVGM(index, mc, entry);
            break;
        case XGM:
            addFileXGM(index, mc, entry);
            break;
        case ZGM:
            addFileZGM(index, mc, entry);
            break;
        case WAV:
            addFileWAV(index, mc, entry);
            break;
        case MP3:
            addFileMP3(index, mc, entry);
            break;
        case AIFF:
            addFileAIFF(index, mc, entry);
            break;
        case ZIP:
            addFileZIP(index, mc, entry);
            break;
        case LZH:
            addFileLZH(index, mc, entry);
            break;
        case M3U:
            addFileM3U(index, mc, entry);
            break;
        case NSF:
            addFileNSF(index, mc, entry);
            break;
        case HES:
            addFileHES(index, mc, entry);
            break;
        case SID:
            addFileSID(index, mc, entry, null);
            break;
        }
    }

    /**
     * 汎用
     */
    private void addFileXxx(Music mc, Object entry/*=null*/) {
        try {
            byte[] buf = null;
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
                if (entry instanceof ZipEntry) {
                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        try {
                            buf = reader.readBytes((int) ((ZipEntry) entry).length);
                        } catch (Exception ex) {
                            Log.forcedWrite(ex);
                            buf = null;
                        }
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);
            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).addRow(row);
            _lstMusic.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileXxx(int index, Music mc, Object entry/* = null*/) {
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
                if (entry instanceof ZipEntry) {
                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        try {
                            buf = reader.readBytes((int) ((ZipEntry) entry).length);
                        } catch (Exception ex) {
                            Log.forcedWrite(ex);
                            buf = null;
                        }
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);
            List<Object[]> rows = makeRow(musics);

            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
            _lstMusic.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileMID(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMID(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileNRT(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileNRT(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileRCP(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileRCP(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileS98(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileS98(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileVGM(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileVGM(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileXGM(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileXGM(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileZGM(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileZGM(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMDR(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMDR(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMND(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMND(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMDX(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMDX(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMUB(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMUB(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMUC(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMUC(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMML(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMML(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileM(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileM(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMGS(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMGS(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileWAV(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileWAV(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileMP3(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileMP3(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileAIFF(Music mc, Object entry/* = null*/) {
        addFileXxx(mc, entry);
    }

    private void addFileAIFF(int index, Music mc, Object entry/* = null*/) {
        addFileXxx(index, mc, entry);
    }

    private void addFileZIP(Music mc, Object entry/* = null*/) {
        if (entry != null) return;

        try (ZipArchive archive = ZipFile.OpenRead(mc.fileName)) {
            mc.arcFileName = mc.fileName;
            mc.arcType = EnmArcType.ZIP;
            List<String> zipMember = new ArrayList<>();
            List<Music> mMember = new ArrayList<>();
            for (ZipEntry ent : archive.Entries) {
                if (Common.FileFormat.checkExt(ent.FullName) != FileFormat.M3U) {
                    zipMember.add(ent.FullName);
                } else {
                    PlayList pl = M3U.LoadM3U(ent, mc.arcFileName);
                    mMember.addAll(pl._lstMusic);
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

            for (ZipEntry ent : archive.Entries) {
                for (Music m : mMember) {
                    String vzm = "";
                    if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                        vzm = Path.changeExtension(m.fileName, ".vgz");
                    else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                        vzm = Path.changeExtension(m.fileName, ".Vgm");

                    if (ent.FullName == m.fileName || ent.FullName == vzm) {
                        m.format = Common.FileFormat.checkExt(m.fileName);
                        m.arcFileName = mc.arcFileName;
                        m.arcType = mc.arcType;
                        addFileLoop(m, ent);

                        //m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                        //それを防ぐためここでbreakする
                        break;
                    }
                }
            }
        }
    }

    private void addFileZIP(int index, Music mc, Object entry/* = null*/) {
        if (entry != null) return;

        try (ZipArchive archive = ZipFile.OpenRead(mc.fileName)) {
            mc.arcFileName = mc.fileName;
            mc.arcType = EnmArcType.ZIP;
            List<String> zipMember = new ArrayList<>();
            List<Music> mMember = new ArrayList<>();
            for (ZipEntry ent : archive.Entries) {
                if (Common.FileFormat.checkExt(ent.FullName) != FileFormat.M3U) {
                    zipMember.add(ent.FullName);
                } else {
                    PlayList pl = M3U.LoadM3U(ent, mc.arcFileName);
                    mMember.addAll(pl._lstMusic);
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

            for (ZipEntry ent : archive.Entries) {
                for (Music m : mMember) {
                    String vzm = "";
                    if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                        vzm = Path.changeExtension(m.fileName, ".vgz");
                    else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                        vzm = Path.changeExtension(m.fileName, ".Vgm");

                    if (ent.FullName == m.fileName || ent.FullName == vzm) {
                        m.format = Common.FileFormat.checkExt(m.fileName);
                        m.arcFileName = mc.arcFileName;
                        m.arcType = mc.arcType;
                        addFileLoop(index, m, ent);

                        //m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                        //それを防ぐためここでbreakする
                        break;
                    }
                }
            }
        }
    }

    private void addFileLZH(Music mc, Object entry/* = null*/) {
        if (entry != null) return;

        UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
        List<Tuple<String, Long>> res = cmd.GetFileList(mc.fileName, "*.*");
        mc.arcFileName = mc.fileName;
        mc.arcType = EnmArcType.LZH;
        List<String> zipMember = new ArrayList<>();
        List<Music> mMember = new ArrayList<>();

        for (Tuple<String, Long> ent : res) {
            if (Common.FileFormat.checkExt(ent.Item1) != FileFormat.M3U) {
                zipMember.add(ent.Item1);
            } else {
                PlayList pl = M3U.LoadM3U(ent, mc.arcFileName);
                mMember.addAll(pl._lstMusic);
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

        for (Tuple<String, Long> ent : res) {
            for (Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (ent.Item1.equals(m.fileName) || ent.Item1.equals(vzm)) {
                    m.format = Common.FileFormat.checkExt(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    addFileLoop(m, new Tuple<>(m.arcFileName, ent.Item1));

                    // m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    // それを防ぐためここでbreakする
                    break;
                }
            }
        }
    }

    private void addFileLZH(int index, Music mc, Object entry/* = null*/) {
        if (entry != null) return;

        UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
        List<Tuple<String, Long>> res = cmd.GetFileList(mc.fileName, "*.*");
        mc.arcFileName = mc.fileName;
        mc.arcType = EnmArcType.LZH;
        List<String> zipMember = new ArrayList<>();
        List<Music> mMember = new ArrayList<>();

        for (Tuple<String, Long> ent : res) {
            if (Common.FileFormat.checkExt(ent.Item1) != FileFormat.M3U) {
                zipMember.add(ent.Item1);
            } else {
                PlayList pl = M3U.LoadM3U(ent, mc.arcFileName);
                mMember.addAll(pl._lstMusic);
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

        for (Tuple<String, Long> ent : res) {
            for (Music m : mMember) {
                String vzm = "";
                if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgm"))
                    vzm = Path.changeExtension(m.fileName, ".vgz");
                else if (Path.getExtension(m.fileName).equalsIgnoreCase(".vgz"))
                    vzm = Path.changeExtension(m.fileName, ".Vgm");

                if (ent.Item1.equals(m.fileName) || ent.Item1.equals(vzm)) {
                    m.format = Common.FileFormat.checkExt(m.fileName);
                    m.arcFileName = mc.arcFileName;
                    m.arcType = mc.arcType;
                    addFileLoop(index, m, new Tuple<>(m.arcFileName, ent.Item1));

                    //m3uが複数同梱されている時、同名のファイルが多数追加されることになるケースがある。
                    //それを防ぐためここでbreakする
                    break;
                }
            }
        }
    }

    private void addFileM3U(Music mc, Object entry/* = null*/) {

        PlayList pl;
        if (entry == null) pl = M3U.LoadM3U(mc.fileName, rootPath);
        else pl = M3U.LoadM3U(entry, mc.arcFileName);
        if (pl == null) return;
        if (pl._lstMusic == null || pl._lstMusic.size() < 1) return;

        for (Music m : pl._lstMusic) addFileLoop(m, entry);
    }

    private void addFileM3U(int index, Music mc, Object entry/* = null*/) {

        PlayList pl;
        if (entry == null) pl = M3U.LoadM3U(mc.fileName, rootPath);
        else pl = M3U.LoadM3U(entry, mc.arcFileName);
        if (pl == null) return;
        if (pl._lstMusic == null || pl._lstMusic.size() < 1) return;

        for (Music m : pl._lstMusic) addFileLoop(index, m, entry);
    }

    private void addFileNSF(Music mc, Object entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                if (entry instanceof ZipEntry) {

                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        buf = reader.readBytes((int) ((ZipEntry) entry).length);
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);

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
            _lstMusic.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileNSF(int index, Music mc, Object entry/* = null*/) {
        try {
            byte[] buf = null;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                if (entry instanceof ZipEntry) {

                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        buf = reader.readBytes((int) ((ZipEntry) entry).length);
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }

            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);

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
            _lstMusic.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileHES(Music mc, Object entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                if (entry instanceof ZipEntry) {

                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        buf = reader.readBytes((int) ((ZipEntry) entry).length);
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);

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
            _lstMusic.addAll(musics);
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileHES(int index, Music mc, Object entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                if (entry instanceof ZipEntry) {

                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        buf = reader.readBytes((int) ((ZipEntry) entry).length);
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);

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
            _lstMusic.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    private void addFileSID(Music mc, Object entry/* = null*/) {
        try {
            byte[] buf;
            if (entry == null) {
                buf = File.readAllBytes(mc.fileName);
            } else {
                if (entry instanceof ZipEntry) {

                    try (BinaryReader reader = new BinaryReader(((ZipEntry) entry).Open())) {
                        buf = reader.readBytes((int) ((ZipEntry) entry).length);
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);

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
            _lstMusic.addAll(musics);
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
                if (entry instanceof ZipEntry) {

                    try (InputStream reader = archive.getInputStream(entry)) {
                        buf =   reader.readBytes((int) ((ZipEntry) entry).length);
                    }
                } else {
                    UnlhaWrap.UnlhaCmd cmd = new UnlhaWrap.UnlhaCmd();
                    buf = cmd.GetFileByte(((Tuple<String, String>) entry).Item1, ((Tuple<String, String>) entry).Item2);
                }
            }

            List<Music> musics;
            if (entry == null) musics = Audio.getMusic(mc.fileName, buf, null, null);
            else musics = Audio.getMusic(mc.fileName, buf, mc.arcFileName, entry);

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
            _lstMusic.addAll(index, musics);
            index += rows.size();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }
}
