/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Headless diagnostic: dumps the "FMDSP" logo sprites so the glyph column ranges can be read off.
 * Not a regression test.
 */
class LogoGlyphCheck {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() {
        dump("logo_fm", FmDspSprites.s_logo_fm, 31);
        dump("logo_ds", FmDspSprites.s_logo_ds, 32);
        dump("logo_p", FmDspSprites.s_logo_p, 15);
    }

    private static void dump(String name, byte[] data, int w) {
        int h = data.length / w;
        System.err.println("=== " + name + " " + w + "x" + h + " (" + data.length + " bytes)");
        for (int y = 0; y < h; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < w; x++) {
                sb.append(data[y * w + x] != 0 ? '#' : '.');
            }
            System.err.println(sb);
        }
        StringBuilder cols = new StringBuilder("occupied cols: ");
        for (int x = 0; x < w; x++) {
            boolean on = false;
            for (int y = 0; y < h && !on; y++) {
                on = data[y * w + x] != 0;
            }
            cols.append(on ? '#' : '.');
        }
        System.err.println(cols);
        // the palette values used, so the blit can reproduce them
        Set<Integer> vals = new TreeSet<>();
        for (byte b : data) vals.add(b & 0xff);
        System.err.println("values: " + vals);
    }
}
