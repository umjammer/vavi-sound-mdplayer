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

import mdplayer.chips.NpNesChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the NES APU as an NSF drives it. Not a regression test. */
class NsfProbe {

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

        NpNesChip apu = plugin.chipRegister.chip(NpNesChip.class);
        NpNesChip.DmcChip dmc = plugin.chipRegister.chip(NpNesChip.DmcChip.class);
        System.err.println("plugin=" + plugin.getClass().getSimpleName()
                + " driver=" + plugin.getDriver().getClass().getSimpleName());
        System.err.println("apu.nsf=" + apu.nsf + " dmc.nsf=" + dmc.nsf);
        // every panel of the family now gets a machine: none of them may throw on a song that
        // does not have their chip
        for (Class<? extends mdplayer.Chip> c : java.util.List.of(
                NpNesChip.class, NpNesChip.DmcChip.class, NpNesChip.FdsChip.class,
                NpNesChip.Mmc5Chip.class, NpNesChip.Vrc6Chip.class, NpNesChip.Vrc7Chip.class,
                NpNesChip.N163Chip.class, NpNesChip.Fme7Chip.class)) {
            try {
                System.err.println("  " + c.getSimpleName() + ".getInfo -> "
                        + ((mdplayer.chips.BaseChip) plugin.chipRegister.chip(c)).getInfo(0).keySet());
            } catch (Exception e) {
                System.err.println("  " + c.getSimpleName() + ".getInfo THREW " + e);
            }
        }

        short[] buffer = new short[1024];
        for (int block = 0; block < 3; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            int[] a = (int[]) apu.getInfo(0).get("register");
            int[] d = (int[]) dmc.getInfo(0).get("register");
            System.err.printf("--- block %d apu=%s dmc=%s%n", block,
                    a == null ? "null" : "len " + a.length, d == null ? "null" : "len " + d.length);
            if (a != null) {
                for (int ch = 0; ch < 2; ch++) {
                    int timer = (a[ch * 4 + 2] & 0xff) | ((a[ch * 4 + 3] & 0x07) << 8);
                    System.err.printf("  pulse%d vol=%2d timer=%4d on=%s -> note %d%n",
                            ch, a[ch * 4] & 0x0f, timer, (a[0x15] & (1 << ch)) != 0,
                            timer == 0 ? -1 : mdplayer.fmdsp.Notes.noteOf(1789773.0 / (16 * (timer + 1))));
                }
            }
            if (d != null) {
                int timer = (d[2] & 0xff) | ((d[3] & 0x07) << 8);
                System.err.printf("  triangle timer=%4d on=%s -> note %d   noise vol=%2d on=%s%n",
                        timer, (d[0x0d] & 0x04) != 0,
                        timer == 0 ? -1 : mdplayer.fmdsp.Notes.noteOf(1789773.0 / (32 * (timer + 1))),
                        d[4] & 0x0f, (d[0x0d] & 0x08) != 0);
            }
        }
    }
}
