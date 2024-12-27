package mdplayer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.StreamReader;
import mdplayer.Common.EnmArcType;
import mdplayer.format.FileFormat;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;
import vavi.util.serdes.Serdes;

import static java.lang.System.getLogger;


public class PlayList implements Serializable {

    private static final Logger logger = getLogger(PlayList.class.getName());

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
        Path fullPath;

        if (fileName == null || fileName.isEmpty()) {
            fullPath = Common.settingFilePath;
            fullPath = fullPath != null ? fullPath.resolve("DefaultPlayList.xml") : Path.of("DefaultPlayList.xml");
        } else {
            fullPath = Path.of(fileName);
        }

        try (ObjectOutputStream sw = new ObjectOutputStream(Files.newOutputStream(fullPath))) {
            sw.writeObject(this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveM3U(String fileName) {
        Path basePath = Path.of(fileName).getParent();

        try (PrintWriter sw = new PrintWriter(new FileWriter(fileName))) {
            for (Music ms : this.musics) {
                Path path = Path.of(ms.fileName).getParent();
                if (path.equals(basePath)) {
                    sw.println(Path.of(ms.fileName).getFileName());
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
            Path fullPath;
            if (fileName == null || fileName.isEmpty()) {
                fullPath = Common.settingFilePath;
                fullPath = fullPath.resolve("DefaultPlayList.xml");
            } else {
                fullPath = Path.of(fileName);
            }

            try (InputStream sr = Files.newInputStream(fullPath)) {
                PlayList pl = new PlayList();
                Serdes.Util.deserialize(sr, pl);
                return pl;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
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

                    if (Path.of(line).getParent() != null) {
                        line = Path.of(filename).getParent().resolve(line).toString();
                    }
                    Music ms = new Music();
                    ms.fileName = line;
                    pl.musics.add(ms);
                }
            }

            return pl;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
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
                Path.of(music.fileName).getFileName().toString(), // clmDispFileName
                music.fileName, // clmDispFileName
                Path.of(music.fileName).toString().substring(0, music.fileName.lastIndexOf('.') + 1).toUpperCase(), // clmEXT
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
            JOptionPane.showMessageDialog(null, "Failed to add a file.\nDetail\nMessage=%s".formatted(ex.getMessage())
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE);
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public void insertFile(/* ref */ int[] index, String[] filenames) {
        try {
            for (String filename : filenames) {
                Music mc = new Music();
                mc.format = FileFormat.getFileFormat(filename);
                mc.fileName = filename;

                addFileLoop(index, mc, null, null);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to add a file.\nDetail\nMessage=%s".formatted(ex.getMessage())
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE);
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void addFileLoop(Music mc, Archive archive, Entry entry /* = null */) {
        try {
            musics = mc.format.addFileLoop(mc, archive, entry);
            if (musics == null) return;

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).addRow(row);
            this.musics.addAll(musics);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void addFileLoop(int[] index, Music mc, Archive archive, Entry entry /* = null */) {
        try {
            musics = mc.format.addFileLoop(index[0], mc, archive, entry);
            if (musics == null) return;

            List<Object[]> rows = makeRow(musics);
            for (Object[] row : rows)
                ((DefaultTableModel) dgvList.getModel()).insertRow(index[0], row);
            this.musics.addAll(index[0], musics);
            index[0] += rows.size();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
