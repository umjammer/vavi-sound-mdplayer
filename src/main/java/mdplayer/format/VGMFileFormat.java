package mdplayer.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.MemoryStream;
import dotnet4j.io.Path;
import dotnet4j.io.compression.CompressionMode;
import dotnet4j.io.compression.GZipStream;
import mdplayer.PlayList;
import mdplayer.driver.Vgm;
import mdplayer.driver.VgmDriver;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.VGMPlugin;
import mdplayer.properties.Resources;
import musicDriverInterface.MetaData;
import vavi.sound.sampled.md.MdEncoding;
import vavi.sound.sampled.md.MdFileFormatType;
import vavi.util.ByteUtil;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.archive.Entry;


/**
 * VGMFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class VGMFileFormat extends BaseFileFormat {

    private static final Logger logger = System.getLogger(VGMFileFormat.class.getName());

    @Override
    public String[] getExtensions() {
        return new String[] {".vgm", ".vgz"};
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

    static final int FCC_VGM = 0x206D6756; // "Vgm "

    @Override
    public byte[] getAllBytes(String filename) {
        // For .VGM, check the header and the header of the file after decompression with Gzip
        byte[] buf = super.getAllBytes(filename);
        int vgm = ByteUtil.readLeInt(buf);
        if (vgm == FCC_VGM) {
            return buf;
        }

        int num;
        buf = new byte[1024]; // Process 1Kbytes at a time

        try (FileStream inStream = new FileStream(filename, FileMode.Open, FileAccess.Read); // Input Stream
             GZipStream decompStream = new GZipStream( // Decompressed Stream
                     inStream, // Specify the input source stream
                     CompressionMode.Decompress); // Specify decompression (uncompression)
             MemoryStream outStream = new MemoryStream() // Output Stream
        ) {
            while ((num = decompStream.read(buf, 0, buf.length)) > 0) {
                outStream.write(buf, 0, num);
            }

            return outStream.getBuffer();
        } catch (java.io.IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_VGM.mbc",
                Resources.getDefaultVolumeBalance_VGM()
        };
    }

    @Override
    protected boolean isMatchFcc(int fcc) {
        return fcc == Vgm.FCC_VGM;
    }

    @Override
    protected boolean isMatchMetaData(int fcc) {
        return fcc == Vgm.FCC_GD3;
    }

    @Override
    public MetaData getMetaData() {
        int vgmGd3 = ByteUtil.readLeInt(this.srcBuf, 0x14);
        return new VgmDriver().getMetaData(this.srcBuf, vgmGd3);
    }

    @Override
    protected MetaData getMetaData(byte[] buf, Object... args) {
        return new VgmDriver().getMetaData(buf, args);
    }

    @Override
    protected byte[] getBytesFromZipFileInternal(Archive archive, Entry entry, byte[] buf) {
        try {
            int vgm = ByteUtil.readLeInt(buf);
            if (vgm != VGMFileFormat.FCC_VGM) {

                try (InputStream inStream = archive.getInputStream(entry);
                     InputStream decompStream = Archives.getInputStream(new BufferedInputStream(inStream))
                ) {
                    return decompStream.readAllBytes();
                }
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    protected byte[] addFileLoopInternal(PlayList.Music mc) {
        if (Path.getExtension(mc.fileName).equalsIgnoreCase(".vgm")) {
            mc.fileName = Path.changeExtension(mc.fileName, ".vgz");
        } else {
            mc.fileName = Path.changeExtension(mc.fileName, ".Vgm");
        }
        try {
            return File.readAllBytes(mc.fileName);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(VGMPlugin.class);
    }

    @Override
    public Encoding getEncoding() {
        return new MdEncoding("VGM", "vgm,zgm");
    }

    @Override
    public Type getType() {
        return new MdFileFormatType("VGM", "vgm");
//        return new MdFileFormatType("VGZ", "vgz");
    }

    @Override
    public int getMarkSize() {
        return 128;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
//logger.log(Level.INFO, "\n" + StringUtil.getDump(is, 0, 32));
        byte[] buf = new byte[getMarkSize()];
        is.readNBytes(buf, 0, buf.length);
//logger.log(Level.INFO, "%x, %x, %s".formatted(FCC_VGM, ByteUtil.readLeInt(buf), StringUtil.getDump(buf)));
        return FCC_VGM == ByteUtil.readLeInt(buf);
    }
}
