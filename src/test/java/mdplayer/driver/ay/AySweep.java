/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ay;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import mdplayer.PlayList.Music;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Loads every .ay under a directory and renders a moment of it. Not a regression test. */
class AySweep {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void sweep() throws Exception {
        String dir = System.getProperty("diag.dir");
        int seconds = Integer.parseInt(System.getProperty("diag.limit", "2"));
        int rate = Setting.getInstance().getOutputDevice().getSampleRate();

        int[] count = {0, 0, 0, 0}; // files, failed, silent, no title
        try (Stream<Path> files = Files.walk(Path.of(dir))) {
            for (Path p : files.filter(f -> f.toString().toLowerCase().endsWith(".ay")).sorted().toList()) {
                count[0]++;
                String file = p.toString();
                try {
                    FileFormat format = FileFormat.getFileFormat(file);
                    format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(p))), null);
                    List<Music> musics = format.getMusic(file, format.getData(), null, null, null);
                    if (musics.stream().allMatch(m -> m.title == null || m.title.isBlank())) count[3]++;

                    @SuppressWarnings("unchecked")
                    var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
                    plugin.setParams(format, Map.of("fileName", file, "songNo", Math.max(0, musics.getFirst().songNo)));
                    plugin.prepare();
                    plugin.stopped = false;
                    plugin.paused = false;
                    plugin.fadeout = false;

                    BaseDriver driver = plugin.getDriver();
                    short[] buffer = new short[1024];
                    int peak = 0;
                    for (long n = 0; n < (long) seconds * rate && !driver.stopped; n += buffer.length / 2) {
                        driver.render(buffer, 0, buffer.length);
                        for (short s : buffer) peak = Math.max(peak, Math.abs(s));
                    }
                    if (peak < 16) {
                        count[2]++;
                        System.err.println("SILENT " + file);
                    }
                } catch (Exception | StackOverflowError e) {
                    count[1]++;
                    System.err.println("FAILED " + file + ": " + e);
                }
            }
        }
        System.err.printf("--- files=%d failed=%d silent=%d untitled=%d%n", count[0], count[1], count[2], count[3]);
    }
}
