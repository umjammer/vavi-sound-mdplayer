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

import mdplayer.chips.C352Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Headless probe of the C352 registers as the chip itself has them. Not a regression test.
 * <p>
 * The point of interest is the busy bit: it is the chip that raises and drops it, so a shadow of
 * the driver's writes would never show it move.
 */
class C352Probe {

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

        C352Chip chip = plugin.chipRegister.chip(C352Chip.class);
        short[] buffer = new short[1024];
        for (int block = 0; block < 3; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            Map<String, Object> info = chip.getInfo(0);
            if (info.isEmpty()) {
                System.err.println("--- block " + block + ": no c352");
                continue;
            }
            int[] r = (int[]) info.get("register");
            System.err.println("--- block " + block);
            for (int ch = 0; ch < 32; ch++) {
                int flags = r[ch * 8 + 3];
                if ((flags & 0x8000) == 0) continue; // not sounding
                int front = r[ch * 8];
                int freq = r[ch * 8 + 2] & 0xffff;
                System.err.printf("  ch%2d vol=%3d/%3d flags=%04x bank=%02x freq=%5d ratio=%.4f note=%d%n",
                        ch, (front >> 8) & 0xff, front & 0xff, flags, r[ch * 8 + 4], freq,
                        freq / 65536.0, mdplayer.fmdsp.Notes.noteOfRatio(freq / 65536.0));
            }
        }
    }
}
