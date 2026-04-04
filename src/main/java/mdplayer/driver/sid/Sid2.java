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
import mdplayer.Setting;
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
 * TODO eliminate mdplayer packages
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public class Sid2 {

    private static final Logger logger = getLogger(Sid2.class.getName());

    static final int FCC_PSID = 0x44495350;
    static final int FCC_RSID = 0x44495352;
    public int songs;
    int song;

    private SidTune sidTune;
    private Player sidPlayer;
    private IConfig sidConfig;
    private SidTuneInfo tuneInfo;

    final BlockingDeque<Short> deque = new LinkedBlockingDeque<>(Integer.MAX_VALUE);

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

    void init(byte[] data, Setting setting) {

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
            sidTune = SidTune.load("mdsound", new ByteArrayInputStream(data));

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
            throw new IllegalStateException(e);
        }
    }

    final VisWaveBuffer visWB = new VisWaveBuffer();
}
