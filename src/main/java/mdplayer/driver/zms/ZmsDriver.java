package mdplayer.driver.zms;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.MPcmChip;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.mxdrv.MXDRV.Pcm8Interface;
import mdplayer.driver.zms.Zms.MPcmInterface;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * ZMUSIC X68000
 * <pre>
 *               | source | compiled
 * --------------+--------+----------
 * play data	 |  ZMS   |   ZMD
 * sampling data |  CNF   |   ZPD
 * </pre>
 * system property
 * <li>{@code mdplayer.zms.dir} ... zmusic.x etc. location, default {@code $HOME}</li>
 * <li>{@code mdplayer.zms.zpd} ... zpd file search location, nullable and multipliable by {@code ;} separation</li>
 *
 * @author kumatan
 */
public class ZmsDriver extends BaseDriver {

    private static final Logger logger = getLogger(ZmsDriver.class.getName());

    private final Zms zms;

    public ZmsDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.zms = new Zms();
        zms.charset = Common.charset;
        zms.frequency = Common.VGMProcSampleRate;
        zms.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).corrections[0], frameCounter);
        zms.midiSend = (l, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, l, d, 0);
        zms.loop = l -> curLoop = l;
        zms.stop = () -> stopped = true;
        zms.wait = () -> (int) (setting.getOutputDevice().getSampleRate() * (double) setting.getZMusic().waitNextPlay / 1000.0);
        zms.pcm8 = new Pcm8Interface() {
            @Override
            public void writePcm(byte[] pcm, int offset, int length) {
                plugin.chipRegister.chip(Pcm8Chip.class).writePcm(0, 0, 0, pcm, model);
            }

            @Override
            public void keyOn(int ch, int addr, int mode, int len) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOn(0, ch, addr, mode, len);
            }

            @Override
            public void keyOff(int ch) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOff(0, ch);
            }

            @Override
            public void abort() {
                plugin.chipRegister.chip(Pcm8Chip.class).abort(0);
            }
        };
        zms.mpcm = new MPcmInterface() {
            @Override
            public void keyOn(int ch) {
                plugin.chipRegister.chip(MPcmChip.class).keyOn(0, ch);
            }

            @Override
            public void keyOff(int ch) {
                plugin.chipRegister.chip(MPcmChip.class).keyOff(0, ch);
            }

            @Override
            public void writePcm(int ch, Object pcm, Object mem, Object reg, int n) {
                plugin.chipRegister.chip(MPcmChip.class).writePcm(0, ch, pcm, mem, n, n);
            }

            @Override
            public void setFreq(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setFreq(0, ch, value);
            }

            @Override
            public void setPitch(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setPitch(0, ch, value);
            }

            @Override
            public void setVol(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setVol(0, ch, value);
            }

            @Override
            public void setPan(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setPan(0, ch, value);
            }

            @Override
            public void reset() {
                plugin.chipRegister.chip(MPcmChip.class).reset(0);
            }

            @Override
            public void setVolTable(int type) {
                plugin.chipRegister.chip(MPcmChip.class).setVolTable(0, type);
            }

            @Override
            public void setVolTable(int type, int[] vtbl) {
                plugin.chipRegister.chip(MPcmChip.class).setVolTable(0, type, vtbl);
            }
        };
        zms.dir = System.getProperty("mdplayer.zms.dir", System.getProperty("user.dir"));
        zms.zpd = System.getProperty("mdplayer.zms.zpd");
    }

    public ZmsDriver() {
        this(null); // gross
    }

    public int getVersion() {
        return zms.version;
    }

    public void setVersion(int version) {
        zms.version = version;
    }

    public void setSupportFileBinaryAndName(List<Tuple<byte[], String>> supportFileBinary) {
        zms.supportFileBinaryAndName = supportFileBinary;
    }

    public byte[] getCompiledData() {
        return zms.compiledData;
    }

    public void setCompiledData(byte[] value) {
        this.dataBuf = value; // TODO gross
        zms.compiledData = value;
    }

    public boolean compile(byte[] vgmBuf, String fn) {
        return zms.compile(vgmBuf, fn);
    }

    public boolean compileV2(byte[] vgmBuf, String fn) {
        return zms.compileV2(vgmBuf, fn);
    }

    /**
     * @param args 0: offset, 1: filename
     */
    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        String filename = args.length > 1 ? (String) args[1] : plugin.playingFileName;
        if (filename.toUpperCase().endsWith(".ZMS")) {
            return getMetaDataZMS(buf);
        } else if (filename.toUpperCase().endsWith(".ZMD")) {
            return getMetaDataZMD(buf);
        } else {
            return new MetaData();
        }
    }

    private static MetaData getMetaDataZMS(byte[] buf) {
        String text = new String(buf, Common.charset);
        String[] texts = text.split("\r\n");
        String cmt = "";
        String comment = ".COMMENT";
        for (String s : texts) {
            if (!s.toUpperCase().trim().contains(comment)) continue;
            cmt = s.trim().substring(s.toUpperCase().trim().indexOf(comment) + comment.length()).trim();
            break;
        }
        MetaData md = new MetaData();
        if (cmt != null && !cmt.isEmpty()) {
            md.set(Tag.Title, cmt);
            md.set(Tag.TitleJ, cmt);
        }
        return md;
    }

    private MetaData getMetaDataZMD(byte[] buf) {
        MetaData md = new MetaData();

        if (buf.length < 8) {
            throw new IllegalArgumentException("Unknown zmd file");
        } else {
            int chkID1 = (buf[0] & 0xFF) * 0x100_0000 + (buf[1] & 0xFF) * 0x1_0000 + (buf[2] & 0xFF) * 0x100 + (buf[3] & 0xFF);
            int chkID2 = (buf[4] & 0xFF) * 0x100_0000 + (buf[5] & 0xFF) * 0x1_0000 + (buf[6] & 0xFF) * 0x100 + (buf[7] & 0xFF);
            logger.log(Level.TRACE, "Zms Version Check: chkID1=%08x, chkID2=%08x%n".formatted(chkID1, chkID2));
            if (chkID1 == 0x1a5a_6d75 && chkID2 == 0x5369_4330) zms.version = 3;
            if (chkID1 == 0x105a_6d75 && chkID2 != 0x5369_4330) zms.version = 2;
            logger.log(Level.TRACE, "Zms Version Detected: " + version);

            if (zms.version == 0) {
                throw new IllegalArgumentException("Version check error");
            }
        }

        String cmt = null;
        try {
            if (zms.version == 3) {
                int ptr = (buf[9 * 4 + 0] & 0xFF) * 0x100_0000 + (buf[9 * 4 + 1] & 0xFF) * 0x1_0000 +
                        (buf[9 * 4 + 2] & 0xFF) * 0x100 + (buf[9 * 4 + 3] & 0xFF) + 40;
                int ePtr = ptr;
                while (buf[ePtr] != 0x00) {
                    if (buf[ePtr] == 0x0d && buf[ePtr + 1] == 0x0a) break;
                    ePtr++;
                }

                cmt = new String(buf, ptr, ePtr - ptr, Common.charset);
            }
        } catch (Exception e) {
            // Do nothing
        }

        if (cmt != null && !cmt.isEmpty()) {
            md.set(Tag.Title, cmt);
            md.set(Tag.TitleJ, cmt);
        }
        return md;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        metaData = getMetaData(dataBuf, 0);
        loopCounter = 0;
        curLoop = 0;
        this.model = model;

        frameCounter = -latency - waitTime;
        speed = 1;

        zms.setZPDSearchPath();
        zms.playingFileName = plugin.playingFileName;
        zms.playingArcFileName = plugin.playingArcFileName;

        try {
            zms.run(dataBuf);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            if (zms.waitNextPlay-- > 0) return;

            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                if (frameCounter > -1) {
                    counter++;

                    zms.trap();
                }
                frameCounter++;
            }

//            if (SkipSwitchPianoRoll) return;
            zms.clock();
            //curLoop = mm.readShort(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
