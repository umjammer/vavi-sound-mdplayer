/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the AY player: what the play list gets, and when the song ends. Not a regression test. */
class AyProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() throws Exception {
        String file = System.getProperty("diag.file");
        int songNo = Integer.parseInt(System.getProperty("diag.song", "0"));
        int limit = Integer.parseInt(System.getProperty("diag.limit", "180")); // seconds of emulated time

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);

        List<PlayList.Music> musics = format.getMusic(file, format.getData(), null, null, null);
        System.err.println("--- play list (" + musics.size() + ")");
        for (PlayList.Music m : musics) {
            System.err.printf("  songNo=%d title=[%s] game=[%s] composer=[%s] notes=[%s] duration=[%s]%n",
                    m.songNo, m.title, m.game, m.composer, m.notes, m.duration);
        }

        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "songNo", songNo));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        BaseDriver driver = plugin.getDriver();
        int rate = Setting.getInstance().getOutputDevice().getSampleRate();
        System.err.printf("--- driver %s totalCounter=%d (%.1fs) loopCounter=%d%n",
                driver.getClass().getSimpleName(), driver.totalCounter, driver.totalCounter / (double) rate, driver.loopCounter);

        short[] buffer = new short[1024];
        long rendered = 0;
        double sum = 0;
        int peak = 0;
        while (!driver.stopped && driver.curLoop < 1 && rendered < (long) limit * rate) {
            driver.render(buffer, 0, buffer.length);
            rendered += buffer.length / 2;
            for (short s : buffer) {
                sum += (double) s * s;
                peak = Math.max(peak, Math.abs(s));
            }
        }
        System.err.printf("--- ended after %.1fs (played=%.1fs) stopped=%s curLoop=%d counter=%d totalCounter=%d rms=%.0f peak=%d%n",
                rendered / (double) rate, driver.counter / (double) Common.VGMProcSampleRate,
                driver.stopped, driver.curLoop, driver.counter, driver.totalCounter,
                Math.sqrt(sum / Math.max(1, rendered * 2)), peak);
    }
}
