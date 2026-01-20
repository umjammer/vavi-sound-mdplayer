/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.sid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import libsidplay.common.SamplingRate;
import libsidplay.config.IConfig;
import libsidplay.sidtune.SidTune;
import libsidplay.sidtune.SidTuneError;
import libsidplay.sidtune.SidTuneInfo;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;
import mdsound.VisWaveBuffer;
import sidplay.Player;
import sidplay.audio.Audio;
import sidplay.audio.AudioDriver;
import sidplay.audio.JWAVDriver.JWAVStreamDriver;
import sidplay.ini.IniConfig;
import sidplay.player.State;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * Sid2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public class Sid2 extends BaseDriver implements SidDriver {

    private static final Logger logger = getLogger(Sid2.class.getName());

    private static final int FCC_PSID = 0x44495350;
    private static final int FCC_RSID = 0x44495352;
    public int songs;
    private int song;

    private SidTune sidTune;
    private Player sidPlayer;
    private IConfig sidConfig;
    private SidTuneInfo tuneInfo;

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        if (ByteUtil.readLeInt(buf, 0) != FCC_PSID && ByteUtil.readLeInt(buf, 0) != FCC_RSID) {
            return null;
        }

        songs = Common.getBE16(buf, 0x0e);

        Vgm.Gd3 gd3 = new Vgm.Gd3();
        try {
            gd3.trackName = new String(buf, 0x16, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.trackName.indexOf((char) 0);
            if (idx != -1) gd3.trackName = gd3.trackName.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            gd3.composer = new String(buf, 0x36, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.composer.indexOf((char) 0);
            if (idx != -1) gd3.composer = gd3.composer.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            gd3.notes = new String(buf, 0x56, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.notes.indexOf((char) 0);
            if (idx != -1) gd3.notes = gd3.notes.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        if (model == EnmModel.RealModel) {
            stopped = true;
            vgmCurLoop = 9999;
            return true;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        gd3 = getGD3Info(vgmBuf);

        return init(vgmBuf);
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;
        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    counter++;
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private final BlockingDeque<Short> deque = new LinkedBlockingDeque<>(Integer.MAX_VALUE);

    private final OutputStream os = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            byte[] buf = new byte[] {(byte) b};
            write(buf, 0, 1);
        }

        private byte[] bb;

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                byte[] x;
                if (bb != null) {
                    x = new byte[len + bb.length];
                    System.arraycopy(bb, 0, x, 0, bb.length);
                    System.arraycopy(b, off, x, bb.length, len);
                    off = 0;
                    len = x.length;
                } else {
                    x = b;
                }
                for (int i = 0; i < len / 4; i++) {
                    deque.offer(ByteUtil.readLeShort(x, off + i * 4 + 0));
                    deque.offer(ByteUtil.readLeShort(x, off + i * 4 + 2));
                }
                int mod = x.length % 4;
                if (mod != 0) {
                    bb = new byte[mod];
                    System.arraycopy(x, x.length - mod, bb, 0, mod);
                }
                else bb = null;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
    };

    private boolean init(byte[] vgmBuf) {

        byte[] aryKernel;
        byte[] aryBasic;
        byte[] aryCharacter;
        if (File.exists(setting.getSid().romKernalPath))
            try (FileStream fs = new FileStream(setting.getSid().romKernalPath, FileMode.Open, FileAccess.Read)) {
                aryKernel = new byte[(int) fs.getLength()];
                fs.read(aryKernel, 0, aryKernel.length);
            }
        if (File.exists(setting.getSid().romBasicPath))
            try (FileStream fs = new FileStream(setting.getSid().romBasicPath, FileMode.Open, FileAccess.Read)) {
                aryBasic = new byte[(int) fs.getLength()];
                fs.read(aryBasic, 0, aryBasic.length);
            }
        if (File.exists(setting.getSid().romCharacterPath))
            try (FileStream fs = new FileStream(setting.getSid().romCharacterPath, FileMode.Open, FileAccess.Read)) {
                aryCharacter = new byte[(int) fs.getLength()];
                fs.read(aryCharacter, 0, aryCharacter.length);
            }

        try {
            sidTune = SidTune.load("mdsound", new ByteArrayInputStream(vgmBuf));

            sidConfig = new IniConfig();
            sidConfig.getAudioSection().setAudio(Audio.STREAM);
            SamplingRate samplingRate = SamplingRate.getByFrequency(setting.getOutputDevice().getSampleRate());
logger.log(Level.TRACE, "sampleRate: " + setting.getOutputDevice().getSampleRate());
            sidConfig.getAudioSection().setSamplingRate(samplingRate);

            sidPlayer = new Player(sidConfig);
            sidPlayer.setTune(sidTune);

            AudioDriver audioDriver = sidConfig.getAudioSection().getAudio().getAudioDriver();
logger.log(Level.TRACE, "audioDriver: " + audioDriver);
            if (!(audioDriver instanceof JWAVStreamDriver streamDriver)) {
                throw new IllegalStateException("unsupported audio driver: " + audioDriver);
            }

            streamDriver.setOut(os);

            sidPlayer.play(sidTune);
            while (sidPlayer.stateProperty().get() != State.PLAY) {
                try { Thread.sleep(10L); } catch (InterruptedException ex) { /* noop */ }
            }

            // Get tune details
            tuneInfo = sidTune.getInfo();
        } catch (IOException | SidTuneError e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }

        plugin.audio.chipRegister.chip(SidChip.class).setDriver(this);

        return true;
    }

    VisWaveBuffer visWB = new VisWaveBuffer();

    @Override
    public Integer[][] getRegisterFromSid() {
        return null;
    }

    @Override
    public Map<String, Object> getInfo() {
        return Map.of();
    }

    @Override
    public void setSong(int songNo) {
        this.song = songNo;
    }

int CC;
static final int INTERVAL = 1024;

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        if (vgmFrameCounter < 0) {
            vgmFrameCounter += sampleCount / 2;
            return sampleCount;
        }

        int c = 0;

        try {
            for (int i = 0; i < sampleCount / 2 && !deque.isEmpty(); i++) {
                processOneFrame();
                buffer[c + 0] = deque.take();
                buffer[c + 1] = deque.take();
if (CC++ % INTERVAL == 0) { logger.log(Level.DEBUG, "SID: %d, %d".formatted(buffer[c + 0], buffer[c + 1])); }
                visWB.enq(buffer[c + 0], buffer[c + 1]);
                c += 2;
            }
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return c;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        visWB.copy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }
}
