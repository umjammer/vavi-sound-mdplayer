package mdplayer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.StreamReader;
import mdplayer.format.FileFormat;
import vavi.util.Debug;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;


public class M3U {

    public static PlayList loadM3U(String filename, String rootPath) {
        try {
            PlayList pl = new PlayList();

            try (StreamReader sr = new StreamReader(new FileStream(filename, FileMode.Open), Charset.forName("MS932"))) {
                String line;
                while ((line = sr.readLine()) != null) {

                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if (line.charAt(0) == '#') continue;

                    PlayList.Music ms = analyzeLine(line, rootPath);
                    if (ms != null) {
                        ms.format = FileFormat.getFileFormat(ms.fileName);
                        pl.getMusics().add(ms);
                    }
                }
            }

            return pl;

        } catch (Exception ex) {
            ex.printStackTrace();
            return new PlayList();
        }
    }

    public static PlayList loadM3U(Archive archive, Entry entry, String zipFileName) {
        try {
            PlayList pl = new PlayList();

            try (Scanner sr = new Scanner(archive.getInputStream(entry))) {
                String line;
                while (sr.hasNextLine()) {
                    line = sr.nextLine();
                    line = line.trim();
                    if (line.equals("")) continue;
                    if (line.charAt(0) == '#') continue;

                    PlayList.Music ms = analyzeLine(line, "");
                    ms.format = FileFormat.getFileFormat(ms.fileName);
                    ms.arcFileName = zipFileName;
                    pl.getMusics().add(ms);
                }
            }

            return pl;

        } catch (Exception ex) {
            ex.printStackTrace();
            return new PlayList();
        }
    }

    private static PlayList loadM3U(String archiveFile, String fileName, String zipFileName) {
        try {
            PlayList pl = new PlayList();
            Archive cmd = Archives.getArchive(new File(archiveFile));
            byte[] buf = cmd.getInputStream(cmd.getEntry(fileName)).readAllBytes();
            String[] text = new String(buf, Charset.forName("MS932")).split("\n");

            for (String txt : text) {
                String line = txt.trim();
                if (line.equals("")) continue;
                if (line.charAt(0) == '#') continue;

                PlayList.Music ms = analyzeLine(line, "");
                ms.format = FileFormat.getFileFormat(ms.fileName);
                ms.arcFileName = zipFileName;
                pl.getMusics().add(ms);
            }

            return pl;

        } catch (Exception ex) {
            ex.printStackTrace();
            return new PlayList();
        }
    }

    private static PlayList.Music analyzeLine(String line, String rootPath) {
        PlayList.Music ms = new PlayList.Music();

        try {
            // ::が無い場合は全てをファイル名として処理終了
            if (!line.contains("::")) {
                ms.fileName = line;
                if (!Path.isPathRooted(ms.fileName) && rootPath.isEmpty()) {
                    ms.fileName = Path.combine(rootPath, ms.fileName);
                }

                return ms;
            }

            String[] buf = line.split("::");

            ms.fileName = buf[0].trim();
            if (!Path.isPathRooted(ms.fileName) && !rootPath.isEmpty()) {
                ms.fileName = Path.combine(rootPath, ms.fileName);
            }
            if (buf.length < 1) return ms;

            buf = buf[1].split(",");
            List<String> lbuf = new ArrayList<>();
            for (int i = 0; i < buf.length; ) {
                StringBuilder s = new StringBuilder();
                boolean flg;
                do {
                    flg = false;
                    s.append(buf[i]);
                    if (buf[i].length() != 0 && buf[i].lastIndexOf('\\') == buf[i].length() - 1) {
                        s.append(",");
                        flg = true;
                    }
                    i++;
                } while (flg);
                lbuf.add(s.toString().replace("\\", ""));
            }
            buf = lbuf.toArray(String[]::new);

            String fType = buf[0].trim().toUpperCase();
            if (buf.length < 2) return ms;

            ms.songNo = analyzeSongNo(buf[1].trim()) - (fType.equals("NSF") ? 1 : 0);
            if (buf.length < 3) return ms;

            ms.title = buf[2].trim();
            ms.titleJ = buf[2].trim();
            if (buf.length < 4) return ms;

            ms.time = buf[3].trim();
            if (buf.length < 5) return ms;

            analyzeLoopTime(buf[4], ms);
            if (buf.length < 6) return ms;

            ms.fadeoutTime = buf[5].trim();
            if (buf.length < 7) return ms;

            try { ms.loopCount = Integer.parseInt(buf[6].trim()); } catch (NumberFormatException e) { ms.loopCount = -1; }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return ms;
    }

    private static int analyzeSongNo(String s) {
        int n;

        if (!s.isEmpty() && s.charAt(0) == '$') {
            try {
                n = Integer.parseInt(s.substring(1), 16);
            } catch (NumberFormatException e) {
                Debug.println(Level.WARNING, e);
                return -1;
            }
        } else {
            try {
                n = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                Debug.println(Level.WARNING, e);
                return -1;
            }
        }

        return n;
    }

    private static void analyzeLoopTime(String s, PlayList.Music ms) {
        ms.loopStartTime = "";
        ms.loopEndTime = "";

        if (!s.isEmpty() && s.charAt(s.length() - 1) == '-') {
            ms.loopStartTime = s.substring(0, s.length() - 1);
            return;
        }

        ms.loopEndTime = s;
    }
}
