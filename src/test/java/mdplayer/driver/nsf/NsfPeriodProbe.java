/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Diagnostic: renders an NSF flat out and measures how periodic the audio really is, to tell a
 * genuine musical loop from a song whose player routine is stuck repeating a short phrase (which
 * is what a wrong song index looks like - a one-song NSF asked for song 1 instead of song 0).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class NsfPeriodProbe {

    static final String file = System.getProperty("nsf",
            "../simplevgm/tmp/Thunder Force IV - Sand Hell [5-N163].nsf");

    static final int songNo = Integer.getInteger("nsf.song", 0);

    static final int duration = Integer.getInteger("nsf.duration", 90);

    static boolean nsfExists() {
        return Files.exists(Path.of(file));
    }

    @Test
    @EnabledIf("nsfExists")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    @SuppressWarnings("unchecked")
    public void probe() throws Exception {
        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Files.newInputStream(Path.of(file)), null);
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "songNo", songNo));
        plugin.prepare();
        BaseDriver driver = plugin.getDriver();

        int rate = setting.getOutputDevice().getSampleRate();
        int total = rate * duration;
        float[] mono = new float[total];
        short[] buf = new short[8192];
        int n = 0;
        while (n < total) {
            driver.render(buf, 0, buf.length);
            for (int i = 0; i < buf.length / 2 && n < total; i++, n++)
                mono[n] = (buf[i * 2] + buf[i * 2 + 1]) / 2f;
        }
        plugin.stop();
        plugin.close();

        // per-5s RMS: a flat line means nothing is progressing
        for (int s = 0; s + 5 <= duration; s += 5) {
            double sum = 0;
            for (int i = s * rate; i < (s + 5) * rate; i++) sum += (double) mono[i] * mono[i];
            System.err.printf("rms[%3d-%3ds] = %8.1f%n", s, s + 5, Math.sqrt(sum / (5 * rate)));
        }

        // normalized correlation of a 5 s window at 20 s against every lag up to 60 s
        int win = 5 * rate;
        int base = 20 * rate;
        System.err.println("-- correlation of [20s,25s) against lag --");
        double best = -2;
        int bestLag = 0;
        for (int lagMs = 500; lagMs <= 60000; lagMs += 20) {
            int lag = (int) ((long) lagMs * rate / 1000);
            if (base + lag + win > total) break;
            double num = 0, a = 0, b = 0;
            for (int i = 0; i < win; i += 4) {
                double x = mono[base + i], y = mono[base + lag + i];
                num += x * y; a += x * x; b += y * y;
            }
            double c = num / Math.sqrt(a * b + 1e-9);
            if (c > best) { best = c; bestLag = lagMs; }
            if (c > 0.9) System.err.printf("  lag=%6dms corr=%.4f%n", lagMs, c);
        }
        System.err.printf("best lag=%dms corr=%.4f%n", bestLag, best);
    }
}
