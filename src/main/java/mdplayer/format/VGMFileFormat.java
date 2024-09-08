package mdplayer.format;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.MemoryStream;
import dotnet4j.io.compression.CompressionMode;
import dotnet4j.io.compression.GZipStream;
import mdplayer.PlayList;
import mdplayer.plugin.Plugin;
import mdplayer.plugin.VGMPlugin;
import mdplayer.properties.Resources;
import vavi.util.ByteUtil;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * VGMFileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class VGMFileFormat extends BaseFileFormat {

    @Override
    public String[] getExtensions() { return new String[] { ".vgm", ".vgz" }; }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        return Collections.singletonList(music);
    }

    static boolean isX() {
        return false;
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    static final int FCC_VGM = 0x206D6756; // "Vgm "

    @Override
    public byte[] getAllBytes(String filename) {
        // .VGMの場合はヘッダの確認とGzipで解凍後のファイルのヘッダの確認
        byte[] buf = super.getAllBytes(filename);
        int vgm = ByteUtil.readLeInt(buf);
        if (vgm == FCC_VGM) {
            return buf;
        }

        int num;
        buf = new byte[1024]; // 1Kbytesずつ処理する

        try (FileStream inStream // 入力ストリーム
                     = new FileStream(filename, FileMode.Open, FileAccess.Read);

             GZipStream decompStream // 解凍ストリーム
                     = new GZipStream(
                     inStream, // 入力元となるストリームを指定
                     CompressionMode.Decompress); // 解凍（圧縮解除）を指定

             MemoryStream outStream // 出力ストリーム
                     = new MemoryStream()

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
    public Plugin getPlugin() {
        return new VGMPlugin();
    }
}
