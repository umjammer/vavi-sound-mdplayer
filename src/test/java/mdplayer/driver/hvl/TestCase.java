/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.hvl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vavi.sound.SoundUtil.volume;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-02 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String hvl;

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 1000 * 1000 : 10 * 1000;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {

        Path path = Path.of(hvl);

        SourceDataLine line = AudioSystem.getSourceDataLine(new AudioFormat(44100, 16, 1, true, false));
        line.open();
        volume(line, volume);
        line.start();

        HVL.hvl_InitReplayer();
        HVL.Tune ht = HVL.hvl_LoadTune(path.toString(), 44100, 0, (int) Files.size(path));

        int samples = ht.ht_Frequency / 50 / ht.ht_SpeedMultiplier;
        int totalFrameSamples = samples * ht.ht_SpeedMultiplier;
        short[] bufL = new short[totalFrameSamples];
        short[] bufR = new short[totalFrameSamples];
        byte[] byteBuf = new byte[totalFrameSamples * 2];

        long totalSquared = 0;
        int totalSamples = 0;
        int peak = 0;

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < time) {
            HVL.hvl_DecodeFrame(ht, bufL, 0, bufR, 0, 2);

            for (int i = 0; i < totalFrameSamples; i++) {
                int sample = (bufL[i] + bufR[i]) / 2;
                if (sample > 32767) {
                    sample = 32767;
                } else if (sample < -32768) {
                    sample = -32768;
                }

                totalSquared += (long) sample * sample;
                int absVal = Math.abs(sample);
                if (absVal > peak) peak = absVal;
                totalSamples++;

                byteBuf[i * 2 + 0] = (byte) (sample & 0xff);
                byteBuf[i * 2 + 1] = (byte) ((sample >>> 8) & 0xff);
            }

            line.write(byteBuf, 0, byteBuf.length);
        }

        line.drain();
        line.close();

        double rms = Math.sqrt((double) totalSquared / totalSamples);
        System.out.printf("Stats: RMS=%.2f Peak=%d Samples=%d%n", rms, peak, totalSamples);

        assertTrue(rms > 50.0, "RMS is too low: " + rms);
        assertTrue(peak > 2000, "Peak is too low: " + peak);
    }
}
