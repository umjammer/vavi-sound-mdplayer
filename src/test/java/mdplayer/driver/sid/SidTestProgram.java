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

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static vavi.sound.SoundUtil.volume;


/**
 * SidTestProgram.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-01-25 nsano initial version <br>
 */
public class SidTestProgram {

    private static final int SamplingRate = 44100;

    private SourceDataLine audioOutput = null;
    private Thread playbackThread;
    private volatile boolean isPlaying = false;

    private playSidFp engine;
    private SidTune tune;
    private volatile int validBuffers = 0;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: SidTestProgram <sid_file> [song_no]");
            return;
        }
        int song = 1;
        if (args.length > 1) {
            song = Integer.parseInt(args[1]);
        }

        new SidTestProgram().play(args[0], song);
    }

    public void play(String filename, int song) throws Exception {
        byte[] fileBuffer = Files.readAllBytes(Paths.get(filename));

        init(fileBuffer, song);

        audioOutput = AudioSystem.getSourceDataLine(new AudioFormat(SamplingRate, 16, 2, true, false));
        audioOutput.open();
        volume(audioOutput, Double.parseDouble(System.getProperty("mdsound.volume", "0.2")));
        audioOutput.start();

        isPlaying = true;
        playbackThread = new Thread(this::playbackLoop);
        playbackThread.start();

        System.out.println("Playing " + filename + " song " + song);
        System.out.println("Press Enter to stop...");
        System.in.read();

        isPlaying = false;
        playbackThread.join();
        audioOutput.close();

        if (validBuffers == 0) {
            throw new RuntimeException("FAILURE: No valid audio signal detected at all! (RMS > 50 and Peak > 2000 not met)");
        }
        
        System.out.println("SUCCESS: Audio signal detected.");
    }

    private void init(byte[] buf, int song) {
        // Mock Setting for ReSidBuilder
        Setting setting = mock(Setting.class, Mockito.RETURNS_DEEP_STUBS);
        when(setting.getOutputDevice().getSampleRate()).thenReturn(SamplingRate);

        engine = new playSidFp(SamplingRate);
        engine.setRoms(null, null, null); // No ROMs for now

        ReSidBuilder rs = new ReSidBuilder("ReSid", setting.getOutputDevice().getSampleRate());
        rs.create(1); // Create 1 SID

        tune = new SidTune(buf, buf.length);
        tune.selectSong(song);

        if (!engine.load(tune)) {
            System.err.println("Error loading tune: " + engine.error());
            return;
        }

        SidConfig cfg = new SidConfig(SamplingRate);
        cfg.frequency = SamplingRate;
        cfg.samplingMethod = SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
        cfg.fastSampling = false;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.sidEmulation = rs;

        if (!engine.config(cfg)) {
            System.err.println("Error configuring engine: " + engine.error());
        }
    }

    private void playbackLoop() {
        int bufferSize = 2048; // samples
        short[] sampleBuffer = new short[bufferSize * 2]; // stereo
        byte[] audioBuffer = new byte[bufferSize * 4]; // 16-bit stereo

        long totalSquared = 0;
        int totalSamples = 0;
        int peak = 0;
        int bufferCount = 0;
        validBuffers = 0;

        while (isPlaying) {
            int produced = engine.play(sampleBuffer, bufferSize * 2);

            for (int i = 0; i < produced; i++) {
                short val = sampleBuffer[i];
                totalSquared += (long) val * val;
                int absVal = Math.abs(val);
                if (absVal > peak) peak = absVal;
                totalSamples++;

                audioBuffer[i * 2] = (byte) (val & 0xff);
                audioBuffer[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
            }

            bufferCount++;
            if (bufferCount % 10 == 0 && totalSamples > 0) { // Check every ~0.9s
                double rms = Math.sqrt((double) totalSquared / totalSamples);
//                System.err.printf("Stats: RMS=%.2f Peak=%d Samples=%d%n", rms, peak, totalSamples);
                
                // Strict assertion: expect significant signal (RMS > 50, Peak > 2000)
                // Ignore first few buffers to avoid transients/clicks (user requirement)
                if (bufferCount > 20) {
                    if (rms > 50.0 && peak > 2000) {
                        validBuffers++;
                    }
                }

                totalSquared = 0;
                totalSamples = 0;
                peak = 0;
            }

            if (produced > 0) {

                audioOutput.write(audioBuffer, 0, produced * 2);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
