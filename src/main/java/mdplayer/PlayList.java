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

import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.StreamReader;
import mdplayer.Common.EnmArcType;
import mdplayer.format.FileFormat;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;
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
            throw new UncheckedIOException(e);
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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

    private JTable dgvList;

    public void setDGV(JTable dgv) {
        dgvList = dgv;
    }

    public void addFile(String filename) {
        try {
            Music mc = new Music();
            mc.format = FileFormat.getFileFormat(filename);
            mc.fileName = filename;

            addFileLoop(mc, null, null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, String.format("ファイル追加に失敗しました。\n詳細\nMessage=%s", ex.getMessage())
                    , "エラー"
                    , JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void insertFile(/*ref*/int[] index, String[] filenames) {
        try {
            for (String filename : filenames) {
                Music mc = new Music();
                mc.format = FileFormat.getFileFormat(filename);
                mc.fileName = filename;

                addFileLoop(index, mc, null, null);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, String.format("ファイル追加に失敗しました。\n詳細\nMessage=%s", ex.getMessage())
                    , "エラー"
                    , JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addFileLoop(Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            musics = mc.format.addFileLoop(mc, archive, entry);
            if (musics == null) return;;

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).addRow(row);
            this.musics.addAll(musics);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addFileLoop(int[] index, Music mc, Archive archive, Entry entry/* = null*/) {
        try {
            musics = mc.format.addFileLoop(index[0], mc, archive, entry);
            if (musics == null) return;;

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index[0], row);
            this.musics.addAll(index[0], musics);
            index[0] += rows.size();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
