/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.chips.Saa1099Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the raw SAA1099 registers. Not a regression test. */
class SaaProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() throws Exception {
        String file = System.getProperty("diag.file");
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        Saa1099Chip chip = plugin.chipRegister.chip(Saa1099Chip.class);
        short[] buffer = new short[1024];
        for (int block = 0; block < 3; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            Map<String, Object> info = chip.getInfo(0);
            System.err.printf("--- block %d  enabled=%s%n", block, info.get("enabled"));
            for (int ch = 0; ch < 6; ch++) {
                int oct = (int) info.get("channels." + ch + ".octave");
                int fnum = (int) info.get("channels." + ch + ".frequency");
                double hz = 8000000.0 / 256 * Math.pow(2, oct) / (511 - fnum);
                System.err.printf("  ch%d vol=%2d/%2d fnum=%3d oct=%d tone=%s noise=%s -> %8.1f Hz  midiish=%d%n",
                        ch, info.get("channels." + ch + ".volumeL"), info.get("channels." + ch + ".volumeR"),
                        fnum, oct, info.get("channels." + ch + ".tone"), info.get("channels." + ch + ".noise"),
                        hz, mdplayer.fmdsp.Notes.noteOf(hz));
            }
        }
    }
}
