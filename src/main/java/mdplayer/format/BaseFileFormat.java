package mdplayer.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.driver.Vgm;
import vavi.util.ByteUtil;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;
import vavi.util.archive.zip.JdkZipEntry;

import static dotnet4j.io.Path.getDirectoryName;
import static java.lang.System.getLogger;


public abstract class BaseFileFormat implements FileFormat {

    private static final Logger logger = getLogger(BaseFileFormat.class.getName());

    protected List<PlayList.Music> getMusicCommon(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        if (buf.length < 0x40) {
            musics.add(music);
            return musics;
        }
        if (ByteUtil.readLeInt(buf, 0x00) != Vgm.FCC_VGM) {
            musics.add(music);
            return musics;
        }

        music.format = this;
        int version = ByteUtil.readLeInt(buf, 0x08);
        String _version = "%d.%d%d".formatted((version & 0xf00) / 0x100, (version & 0xf0) / 0x10, (version & 0xf));

        int vgmGd3 = ByteUtil.readLeInt(buf, 0x14);
        Vgm.Gd3 gd3 = new Vgm.Gd3();
        if (vgmGd3 != 0) {
            int vgmGd3Id = ByteUtil.readLeInt(buf, vgmGd3 + 0x14);
            if (vgmGd3Id != Vgm.FCC_GD3) {
                musics.add(music);
                return musics;
            }
            gd3 = (new Vgm()).getGD3Info(buf, vgmGd3);
        }

        int totalCounter = ByteUtil.readLeInt(buf, 0x18);
        int vgmLoopOffset = ByteUtil.readLeInt(buf, 0x1c);
        int loopCounter = ByteUtil.readLeInt(buf, 0x20);

        music.title = gd3.trackName;
        music.titleJ = gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;

        double sec = (double) totalCounter / (double) Setting.getInstance().getOutputDevice().getSampleRate();
        int tcMminutes = (int) (sec / 60);
        sec -= tcMminutes * 60;
        int tcSecond = (int) sec;
        sec -= tcSecond;
        int tcMillisecond = (int) (sec * 100.0);
        music.duration = "%2d:%2d:%2d".formatted(tcMminutes, tcSecond, tcMillisecond);

        musics.add(music);
        return musics;
    }

    @Override
    public byte[] getAllBytes(String filename) {
        return File.readAllBytes(filename);
    }

    @Override
    public List<Tuple<String, byte[]>> getExtendFile(String fn, byte[] srcBuf, Archive archive, Entry entry) {
        return null;
    }

    protected byte[] getExtendFileAllBytes(String srcFn, String extFn, Archive archive, Entry entry) {
        try {
            if (entry == null) {
logger.log(Level.DEBUG, "try: " + extFn);
                return BaseFileFormat.getFileSearchPathList(srcFn).stream()
                        .map(dirPath -> dirPath.resolve(extFn))
                        .filter(Files::exists).findFirst()
                        .map(Object::toString)
                        .map(File::readAllBytes).orElse(null);
            } else {
                String trgFn = Path.combine(getDirectoryName(srcFn), extFn);
                trgFn = trgFn.replace("\\", "/").trim();

                if (entry instanceof JdkZipEntry) {
                    String[] arcFn = new String[1];
                    return getBytesFromZipFile(archive, entry, arcFn);
                } else {
                    return archive.getInputStream(entry).readAllBytes();
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    private static List<java.nio.file.Path> getFileSearchPathList(String srcFn) {
        List<java.nio.file.Path> result = new ArrayList<>();
        result.add(java.nio.file.Path.of(srcFn).getParent());
        String fileSearchPathList = Setting.getInstance().getFileSearchPathList() != null ? Setting.getInstance().getFileSearchPathList() : "";
        Arrays.stream(fileSearchPathList.split(";"))
                .filter(path -> path != null && !path.isEmpty())
                .map(java.nio.file.Path::of)
                .forEach(result::add);
logger.log(Level.DEBUG, result);
        return result;
    }

    /**
     * @param archive
     * @param entry
     * @param arcFn   OUT entry file name resolved by the entry
     * @return extracted
     */
    public byte[] getBytesFromZipFile(Archive archive, Entry entry, String[] arcFn) {
        byte[] buf;
        if (entry == null) return null;
        arcFn[0] = entry.getName();
        try (InputStream reader = archive.getInputStream(entry)) {
            buf = reader.readAllBytes();
        } catch (java.io.IOException e) {
            throw new UncheckedIOException(e);
        }

        if (FileFormat.getFileFormat(entry.getName()) instanceof VGMFileFormat) {
            try {
                int vgm = ByteUtil.readLeInt(buf);
                if (vgm != VGMFileFormat.FCC_VGM) {

                    try (InputStream inStream = archive.getInputStream(entry);
                         InputStream decompStream = Archives.getInputStream(new BufferedInputStream(inStream))
                    ) {
                        buf = decompStream.readAllBytes();
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                buf = null;
            }
        }

        return buf;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return null;
    }

    /**
     * General purpose
     */
    @Override
    public List<PlayList.Music> addFileLoop(PlayList.Music mc, Archive archive, Entry entry/*=null*/) throws IOException {
        byte[] buf;
        if (entry == null) {
            try {
                buf = File.readAllBytes(mc.fileName);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                buf = null;
            }
            if (buf == null && mc.format instanceof VGMFileFormat) {
                if (Path.getExtension(mc.fileName).equalsIgnoreCase(".vgm")) {
                    mc.fileName = Path.changeExtension(mc.fileName, ".vgz");
                } else {
                    mc.fileName = Path.changeExtension(mc.fileName, ".Vgm");
                }
                try {
                    buf = File.readAllBytes(mc.fileName);
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                    buf = null;
                }
            }
        } else {
            try (InputStream reader = archive.getInputStream(entry)) {
                try {
                    buf = reader.readAllBytes();
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                    buf = null;
                }
            }
        }

        List<PlayList.Music> musics;
        if (entry == null) musics = getMusic(mc.fileName, buf, null, null, null);
        else musics = getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

        return musics;
    }

    @Override
    public List<PlayList.Music> addFileLoop(int index, PlayList.Music mc, Archive archive, Entry entry/* = null*/) throws IOException {
        byte[] buf;
        if (entry == null) {
            try {
                buf = File.readAllBytes(mc.fileName);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                buf = null;
            }
            if (buf == null && mc.format instanceof VGMFileFormat) {
                if (Path.getExtension(mc.fileName).equalsIgnoreCase(".vgm")) {
                    mc.fileName = Path.changeExtension(mc.fileName, ".vgz");
                } else {
                    mc.fileName = Path.changeExtension(mc.fileName, ".Vgm");
                }
                try {
                    buf = File.readAllBytes(mc.fileName);
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                    buf = null;
                }
            }

            List<PlayList.Music> musics;
            if (entry == null) musics = getMusic(mc.fileName, buf, null, null, null);
            else musics = getMusic(mc.fileName, buf, mc.arcFileName, archive, entry);

            return musics;
        } else {
            try (InputStream reader = archive.getInputStream(entry)) {
                buf = reader.readAllBytes();
            }
            return null;
        }
    }

    @Override
    public Tuple<byte[], List<Tuple<String, byte[]>>> load(String archive, String fn) throws IOException {
        byte[] srcBuf = getAllBytes(fn);
        return new Tuple<>(srcBuf, getExtendFile(fn, srcBuf, null, null));
    }
}