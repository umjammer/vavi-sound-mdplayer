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

import mdplayer.chips.NesChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the raw NES APU registers. Not a regression test. */
class NesProbe {

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

        NesChip chip = plugin.chipRegister.chip(NesChip.class);
        short[] buffer = new short[1024];
        for (int block = 0; block < 4; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            int[] apu = (int[]) chip.getInfo(0).get("register");
            int[] dmc =(int[]) chip.getInfo(0).get("dmcRegister");
            System.err.printf("--- block %d  apuStatus(0x15)=%02x dmcStatus(0x0d)=%02x%n",
                    block, apu[0x15], dmc[0x0d]);
            for (int ch = 0; ch < 2; ch++) {
                int b = ch * 4;
                System.err.printf("  pulse%d: %02x %02x %02x %02x  vol=%d timer=%d%n", ch + 1,
                        apu[b], apu[b + 1], apu[b + 2], apu[b + 3], apu[b] & 0x0f,
                        (apu[b + 2] & 0xff) | ((apu[b + 3] & 0x07) << 8));
            }
            System.err.printf("  tri:    %02x %02x %02x %02x%n", dmc[0], dmc[1], dmc[2], dmc[3]);
            System.err.printf("  noise:  %02x %02x %02x %02x%n", dmc[4], dmc[5], dmc[6], dmc[7]);
        }
    }
}
