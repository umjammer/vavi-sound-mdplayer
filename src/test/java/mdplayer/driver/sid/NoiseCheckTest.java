/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.sid;

import java.nio.file.Files;
import java.nio.file.Paths;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * NoiseCheckTest.
 * Checks if the audio output is consistent and doesn't drop to silence.
 */
public class NoiseCheckTest {

    private static final int SamplingRate = 44100;
    private playSidFp engine;
    private SidTune tune;

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void testNoise() throws Exception {
        String sidFile = System.getProperty("sid");
        if (sidFile == null) {
            Debug.println("No sid file specified via -Dsid=...");
            return;
        }
        playAndCheck(sidFile, 1);
    }

    public void playAndCheck(String filename, int song) throws Exception {
        byte[] fileBuffer = Files.readAllBytes(Paths.get(filename));

        init(fileBuffer, song);

        int bufferSize = 2048; // samples
        short[] sampleBuffer = new short[bufferSize * 2]; // stereo

        int totalChecks = 0;
        int validChecks = 0;
        int silenceStreak = 0;
        int maxSilenceStreak = 0;

        System.out.println("Running noise check for 10 seconds...");

        // Run for ~10 seconds
        // 44100 samples/sec * 10 sec = 441000 samples
        // 441000 / 2048 per buffer = ~215 buffers
        
        for (int b = 0; b < 220; b++) {
            int produced = engine.play(sampleBuffer, bufferSize * 2);
            
            long totalSquared = 0;
            int peak = 0;
            int samples = 0;

            for (int i = 0; i < produced; i++) {
                short val = sampleBuffer[i];
                totalSquared += (long) val * val;
                int absVal = Math.abs(val);
                if (absVal > peak) peak = absVal;
                samples++;
            }

            if (samples > 0) {
                double rms = Math.sqrt((double) totalSquared / samples);
                
                // Check every 10th buffer to reduce log spam, but track validity every buffer
                if (b % 10 == 0) {
                    System.out.printf("Buffer %d: RMS=%.2f Peak=%d%n", b, rms, peak);
                }

                // Definition of valid signal: RMS > 50 and Peak > 2000
                if (rms > 50.0 && peak > 2000) {
                    validChecks++;
                    silenceStreak = 0;
                } else {
                    silenceStreak++;
                }
                
                maxSilenceStreak = Math.max(maxSilenceStreak, silenceStreak);
                totalChecks++;
            }
        }

        System.out.printf("Total Checks: %d, Valid Checks: %d, Max Silence Streak: %d%n", totalChecks, validChecks, maxSilenceStreak);

        // Fail if signal is mostly silence (less than 50% valid)
        assertTrue(validChecks > totalChecks * 0.5, "Audio signal dropped to silence or was too weak! Valid buffers: " + validChecks + "/" + totalChecks);
        
        // Fail if there was a long streak of silence (e.g. > 20 buffers ~ 1 second)
        // after the beginning. Assuming song starts within first 20 buffers.
        // But the song might have breaks. Let's stick to the 50% rule for now as a robust metric.
    }

    private void init(byte[] buf, int song) {
        Setting setting = mock(Setting.class, Mockito.RETURNS_DEEP_STUBS);
        when(setting.getOutputDevice().getSampleRate()).thenReturn(SamplingRate);

        engine = new playSidFp(SamplingRate);
        engine.setRoms(null, null, null);

        ReSidBuilder rs = new ReSidBuilder("ReSid", setting);
        rs.create(1);

        tune = new SidTune(buf, buf.length);
        tune.selectSong(song);

        if (!engine.load(tune)) {
            throw new RuntimeException("Error loading tune: " + engine.error());
        }

        SidConfig cfg = new SidConfig(SamplingRate);
        cfg.frequency = SamplingRate;
        cfg.samplingMethod = SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
        cfg.fastSampling = false;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.sidEmulation = rs;

        if (!engine.config(cfg)) {
            throw new RuntimeException("Error configuring engine: " + engine.error());
        }
    }
}
