/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 */

package mdplayer.driver.sid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import libsidplay.common.SamplingRate;
import libsidplay.config.IConfig;
import libsidplay.sidtune.SidTune;
import libsidplay.sidtune.SidTuneError;
import sidplay.Player;
import sidplay.audio.Audio;
import sidplay.audio.AudioDriver;
import sidplay.audio.JWAVDriver.JWAVStreamDriver;
import sidplay.ini.IniConfig;
import sidplay.player.State;
import vavi.util.ByteUtil;


/**
 * Reference: render same SID file via JSIDPlay2 with voice 1 and 2 muted,
 * capturing voice 0 only. Compare with our Java port output to find the bug.
 */
public class JsidplayDiagTest {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: JsidplayDiagTest <sid_file> <muteMode 0=all|1=v0|2=v1|3=v2> [outRaw]");
            return;
        }
        String filename = args[0];
        int muteMode = Integer.parseInt(args[1]);
        String outRaw = args.length > 2 ? args[2] : null;
        int durationMs = 3000;

        byte[] data = Files.readAllBytes(Paths.get(filename));

        SidTune sidTune = SidTune.load("ref", new ByteArrayInputStream(data));
        IConfig sidConfig = new IniConfig();
        sidConfig.getAudioSection().setAudio(Audio.STREAM);
        sidConfig.getAudioSection().setSamplingRate(SamplingRate.LOW); // 44100

        Player sidPlayer = new Player(sidConfig);
        sidPlayer.setTune(sidTune);

        AudioDriver ad = sidConfig.getAudioSection().getAudio().getAudioDriver();
        if (!(ad instanceof JWAVStreamDriver streamDriver)) {
            throw new IllegalStateException("Driver=" + ad);
        }

        java.io.FileOutputStream rawOut = outRaw != null ? new java.io.FileOutputStream(outRaw) : null;
        OutputStream os = new OutputStream() {
            @Override public void write(int b) throws IOException {}
            @Override public void write(byte[] b, int off, int len) throws IOException {
                // jWAV writes a WAV stream; skip header bytes (first 44) on first chunk
                int skip = headerSkipped ? 0 : Math.min(len, 44);
                if (rawOut != null) rawOut.write(b, off + skip, len - skip);
                headerSkipped = true;
            }
            boolean headerSkipped = false;
        };
        streamDriver.setOut(os);

        sidPlayer.play(sidTune);
        while (sidPlayer.stateProperty().get() != State.PLAY) {
            Thread.sleep(10L);
        }

        // mute voices for chip 0
        if (muteMode == 1) { // only v0 -> mute v1, v2
            sidPlayer.configureSID(0, sid -> sid.setVoiceMute(1, true));
            sidPlayer.configureSID(0, sid -> sid.setVoiceMute(2, true));
        } else if (muteMode == 2) {
            sidPlayer.configureSID(0, sid -> sid.setVoiceMute(0, true));
            sidPlayer.configureSID(0, sid -> sid.setVoiceMute(2, true));
        } else if (muteMode == 3) {
            sidPlayer.configureSID(0, sid -> sid.setVoiceMute(0, true));
            sidPlayer.configureSID(0, sid -> sid.setVoiceMute(1, true));
        }

        Thread.sleep(durationMs);
        sidPlayer.stopC64();
        if (rawOut != null) rawOut.close();
        System.out.println("done");
        System.exit(0);
    }
}
