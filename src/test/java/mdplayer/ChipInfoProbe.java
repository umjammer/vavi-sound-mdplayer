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
import java.util.TreeMap;

import mdplayer.chips.BaseChip;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Dumps whatever a chip's {@code getInfo} says while a file plays, so a reader can be written
 * against what the emulator really reports rather than a guess. Not a regression test.
 * <p>
 * {@code -Dvavi.test=diag -Ddiag.file=<path> -Ddiag.chip=<simple class name of the chip>}
 */
class ChipInfoProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() throws Exception {
        String file = System.getProperty("diag.file");
        String want = System.getProperty("diag.chip");
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        BaseChip chip = null;
        for (Class<? extends mdplayer.Chip> c : plugin.chipRegister.chips()) {
            if (c.getSimpleName().equalsIgnoreCase(want)
                    && plugin.chipRegister.chip(c) instanceof BaseChip b) {
                chip = b;
            }
        }
        if (chip == null) {
            System.err.println("no such chip: " + want);
            return;
        }

        short[] buffer = new short[1024];
        for (int block = 0; block < 4; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            Map<String, Object> info;
            try {
                info = chip.getInfo(0);
            } catch (Exception e) {
                System.err.println("--- block " + block + " THREW " + e);
                continue;
            }
            System.err.println("--- block " + block + (info == null ? " null" : ""));
            if (info == null) continue;
            new TreeMap<>(info).forEach((k, v) -> {
                // the wave tables and the like are noise here
                if (k.contains(".inst. ")) return;
                if (v instanceof int[] a) v = java.util.Arrays.toString(a);
                else if (v instanceof boolean[] a) v = java.util.Arrays.toString(a);
                else if (v instanceof byte[] a) v = java.util.Arrays.toString(a);
                else if (v instanceof int[][] a) v = java.util.Arrays.deepToString(a);
                System.err.println("  " + k + " = " + v);
            });
        }
    }
}
