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

import mdplayer.chips.C140Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the raw C140 registers. Not a regression test. */
class C140Probe {

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

        C140Chip chip = plugin.chipRegister.chip(C140Chip.class);

        short[] buffer = new short[1024];
        for (int block = 0; block < 3; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            byte[] r = (byte[]) chip.getInfo(0).get("register");
            System.err.println("--- block " + block);
            for (int ch = 0; ch < 8; ch++) {
                int b = ch * 16;
                StringBuilder sb = new StringBuilder("  ch%2d: ".formatted(ch));
                for (int i = 0; i < 16; i++) sb.append("%02x ".formatted(r[b + i] & 0xff));
                int freq = ((r[b + 2] & 0xff) << 8) | (r[b + 3] & 0xff);
                sb.append(" freq=%5d ratio=%.4f note=%d".formatted(freq, freq / 65536.0,
                        mdplayer.fmdsp.Notes.noteOfRatio(freq / 65536.0)));
                System.err.println(sb);
            }
        }
    }
}
