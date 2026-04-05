package mdplayer.format;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat.Encoding;

import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.driver.Vgm;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.driver.mxdrv.MxDriver;
import mdplayer.plugin.MDXPlugin;
import mdplayer.plugin.Plugin;
import mdplayer.properties.Resources;
import vavi.sound.SoundUtil;
import vavi.sound.sampled.md.MdEncoding;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


/**
 * MXDRV (X68000) FileFormat.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class MDXFileFormat extends BaseFileFormat {

    private static final Logger logger = System.getLogger(MDXFileFormat.class.getName());

    @Override
    public String[] getExtensions() {
        return new String[] {".mdx"};
    }

    @Override
    public List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile /* = null */, Archive archive, Entry entry /* = null */) {
        PlayList.Music music = new PlayList.Music();
        music.format = this;
        Vgm.Gd3 gd3 = (new MxDriver()).getGD3Info(buf);
        music.title = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackName;
        music.titleJ = gd3.trackName.isEmpty() ? Path.getFileName(file) : gd3.trackNameJ;
        music.game = gd3.gameName;
        music.gameJ = gd3.gameNameJ;
        music.composer = gd3.composer;
        music.composerJ = gd3.composerJ;
        music.vgmby = gd3.vgmBy;

        music.converted = gd3.converted;
        music.notes = gd3.notes;
        return Collections.singletonList(music);
    }

    @Override
    public List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile /* = null */) {
        return getMusicCommon(ms, buf, zipFile);
    }

    @Override
    public List<Tuple<String, byte[]>> getExtendFile(String fn, byte[] srcBuf, Archive archive, Entry entry) {
        List<Tuple<String, byte[]>> ret = new ArrayList<>();
        byte[] buf;

        String[] PDX = new String[1];
        MxDriver.getPDXFileName(srcBuf, PDX, Common.charset);
        if (PDX[0] != null && !PDX[0].isEmpty()) {
            buf = getExtendFileAllBytes(fn, PDX[0], archive, entry);
            if (buf == null) {
                buf = getExtendFileAllBytes(fn, Path.changeExtension(PDX[0], ".PDX"), archive, entry);
                if (buf != null) {
                    logger.log(Level.TRACE, "found pdx: " + Path.changeExtension(PDX[0], ".PDX"));
                }
                if (buf == null) {
                    buf = getExtendFileAllBytes(fn, Path.changeExtension(PDX[0], ".pdx"), archive, entry);
                    if (buf != null) {
                        logger.log(Level.TRACE, "found pdx: " + Path.changeExtension(PDX[0], ".pdx"));
                    }
                }
                if (buf == null) {
                    buf = getExtendFileAllBytes(fn, Path.changeExtension(PDX[0].toUpperCase(), ".PDX"), archive, entry);
                    if (buf != null) {
                        logger.log(Level.TRACE, "found pdx: " + Path.changeExtension(PDX[0].toUpperCase(), ".PDX"));
                    }
                }
                if (buf == null) {
                    buf = getExtendFileAllBytes(fn, Path.changeExtension(PDX[0].toLowerCase(), ".pdx"), archive, entry);
                    if (buf != null) {
                        logger.log(Level.TRACE, "found pdx: " + Path.changeExtension(PDX[0].toLowerCase(), ".pdx"));
                    }
                }
            }
            if (buf != null) ret.add(new Tuple<>(".PDX", buf));
        }

        return ret;
    }

    @Override
    public String[] getPresetMixerBalance() {
        return new String[] {
                "DriverBalance_MDX.mbc",
                Resources.getDefaultVolumeBalance_MDX()
        };
    }

    @Override
    public Plugin getPlugin() {
        return Plugin.getPlugin(MDXPlugin.class);
    }

    /**
     * @throws IllegalArgumentException sampling late must be set as 44.1kHz.
     */
    @Override
    public Tuple<byte[], List<Tuple<String, byte[]>>> load(String archive, String fn) throws IOException {
        var r = super.load(archive, fn);
        if (Path.getExtension(fn).equalsIgnoreCase(".MDX")) {
            if (Setting.getInstance().getOutputDevice().getSampleRate() != 44100) {
                throw new IllegalStateException("When playing MDX files, set the sampling rate to 44.1kHz.");
            }
        }
        return r;
    }

    @Override
    public Encoding getEncoding() {
        return MdEncoding.MXDRV;
    }

    @Override
    public int getMarkSize() {
        return 0;
    }

    @Override
    public boolean isSupported(InputStream is) throws IOException {
        if (isCompressedStream(is)) return false;
        return Arrays.stream(getExtensions()).anyMatch(e -> java.nio.file.Path.of(SoundUtil.getSource(is)).toString().toLowerCase().endsWith(e));
    }
}
