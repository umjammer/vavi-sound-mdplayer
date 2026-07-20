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

import mdplayer.chips.MultiPcmChip;
import mdplayer.chips.OkiM6295Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the OKIM6295 and MultiPCM channel state. Not a regression test. */
class OkiProbe {

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

        OkiM6295Chip oki = plugin.chipRegister.chip(OkiM6295Chip.class);
        MultiPcmChip mpcm = plugin.chipRegister.chip(MultiPcmChip.class);
        short[] buffer = new short[1024];
        for (int block = 0; block < 4; block++) {
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            System.err.println("--- block " + block);
            Map<String, Object> o = oki.getInfo(0);
            System.err.println("  oki=" + (o == null ? "null" : "rate=" + o.get("sampleRate")
                    + " clock=" + o.get("masterClock") + " pin7=" + o.get("pin7State")));
            if (o != null) {
                for (int ch = 0; ch < 4; ch++) {
                    System.err.printf("    ch%d playing=%s vol=%s sadr=%s%n", ch,
                            o.get("channels." + ch + ".playing"), o.get("channels." + ch + ".volume"),
                            o.get("channels." + ch + ".sadr"));
                }
            }
            Map<String, Object> m = mpcm.getInfo(0);
            if (m != null) {
                for (int ch = 0; ch < 28; ch++) {
                    Object playing = m.get("channels." + ch + ".playing");
                    if (playing == null || !(boolean) playing) continue;
                    System.err.printf("    mpcm ch%2d note=%s tl=%s pan=%s inst=%s%n", ch,
                            m.get("channels." + ch + ".note"), m.get("channels." + ch + ".totalLevel"),
                            m.get("channels." + ch + ".panpot"), m.get("channels." + ch + ".inst.0"));
                }
            }
        }
    }
}
