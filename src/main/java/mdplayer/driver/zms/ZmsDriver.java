package mdplayer.driver.zms;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.MPcmChip;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.driver.mxdrv.MXDRV.Pcm8Interface;
import mdplayer.driver.zms.Zms.MPcmInterface;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * ZMUSIC X68000
 * <pre>
 *               | source | compiled
 * --------------+--------+----------
 * play data	 |  ZMS   |   ZMD
 * sampling data |  CNF   |   ZPD
 * </pre>
 * system property
 * <li>"mdplayer.zms.zpd" ... zpd file location</li>
 *
 * @author kumatan
 */
public class ZmsDriver extends BaseDriver {

    private static final Logger logger = getLogger(ZmsDriver.class.getName());

    private final Zms zms;

    public ZmsDriver() {
        this.zms = new Zms();
        zms.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).hosei[0], vgmFrameCounter);
        zms.midiSend = (l, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, l, d, 0);
        zms.loop = l -> vgmCurLoop = l;
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

    public void setPlayingFileName(String value) {
        zms.playingFileName = value;
    }

    public void setPlayingArcFileName(String value) {
        zms.playingArcFileName = value;
    }

    public byte[] getCompiledData() {
        return zms.compiledData;
    }

    public void setCompiledData(byte[] value) {
        zms.compiledData = value;
    }

    public boolean compile(byte[] vgmBuf, String fn) {
        return zms.compile(vgmBuf, fn);
    }

    public boolean compileV2(byte[] vgmBuf, String fn) {
        return zms.compileV2(vgmBuf, fn);
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (zms.playingFileName.toUpperCase().endsWith(".ZMS")) {
            return getGD3InfoZMS(buf);
        } else if (zms.playingFileName.toUpperCase().endsWith(".ZMD")) {
            return getGD3InfoZMD(buf);
        } else {
            return new Gd3();
        }
    }

    private static Gd3 getGD3InfoZMS(byte[] buf) {
        String text = new String(buf, charset);
        String[] texts = text.split("\r\n");
        String cmt = "";
        String comment = ".COMMENT";
        for (String s : texts) {
            if (!s.toUpperCase().trim().contains(comment)) continue;
            cmt = s.trim().substring(s.toUpperCase().trim().indexOf(comment) + comment.length()).trim();
            break;
        }
        Gd3 gd3 = new Gd3();
        if (cmt != null && !cmt.isEmpty()) {
            gd3.trackName = cmt;
            gd3.trackNameJ = cmt;
        }
        return gd3;
    }

    private Gd3 getGD3InfoZMD(byte[] buf) {
        Gd3 gd3 = new Gd3();

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

        String cmt = "";
        try {
            if (zms.version == 3) {
                int ptr = (buf[9 * 4 + 0] & 0xFF) * 0x100_0000 + (buf[9 * 4 + 1] & 0xFF) * 0x1_0000 +
                        (buf[9 * 4 + 2] & 0xFF) * 0x100 + (buf[9 * 4 + 3] & 0xFF) + 40;
                int ePtr = ptr;
                while (buf[ePtr] != 0x00) {
                    if (buf[ePtr] == 0x0d && buf[ePtr + 1] == 0x0a) break;
                    ePtr++;
                }

                cmt = new String(buf, ptr, ePtr - ptr, charset);
            }
        } catch (Exception e) {
            // Do nothing
        }

        if (cmt != null && !cmt.isEmpty()) {
            gd3.trackName = cmt;
            gd3.trackNameJ = cmt;
        }
        return gd3;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        gd3 = getGD3Info(vgmBuf, 0);
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        zms.setZPDSearchPath();

        try {
            zms.run(vgmBuf);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            if (zms.waitNextPlay-- > 0) return;

            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                if (vgmFrameCounter > -1) {
                    counter++;

                    zms.trap();
                }
                vgmFrameCounter++;
            }

//            if (SkipSwitchPianoRoll) return;
            zms.clock();
            //vgmCurLoop = mm.readShort(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
