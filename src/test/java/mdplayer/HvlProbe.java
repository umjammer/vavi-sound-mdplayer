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

import mdplayer.driver.BaseDriver;
import mdplayer.lib.hvl.HVL;
import mdplayer.driver.hvl.HvlDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Headless probe of the HVL replayer's voices. Not a regression test. */
class HvlProbe {

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

        HvlDriver driver = (HvlDriver) plugin.getDriver();
        short[] buffer = new short[1024];
        int shown = 0;
        for (int block = 0; block < 400 && shown < 6; block++) {
            for (int i = 0; i < 4; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
            }
            HVL.Tune tune = driver.getTune();
            if (tune != null) {
                boolean any = false;
                for (int v = 0; v < tune.ht_Channels; v++) {
                    HVL.Voice x = tune.ht_Voices[v];
                    if (x.vc_TrackPeriod != 0 || x.vc_AudioPeriod != 0 || x.vc_NoteMaxVolume != 0) any = true;
                }
                if (!any) continue;
                shown++;
            }
            System.err.println("--- block " + block + " channels=" + (tune == null ? -1 : tune.ht_Channels));
            if (tune == null) continue;
            for (int v = 0; v < tune.ht_Channels; v++) {
                HVL.Voice voice = tune.ht_Voices[v];
                if (voice.vc_TrackPeriod == 0 && voice.vc_AudioPeriod == 0) continue;
                System.err.printf("  v%d track=%d instr=%d audio=%d noteMaxVol=%d voiceVol=%d adsr=%d%n",
                        v, voice.vc_TrackPeriod, voice.vc_InstrPeriod, voice.vc_AudioPeriod,
                        voice.vc_NoteMaxVolume, voice.vc_VoiceVolume, voice.vc_ADSRVolume);
            }
        }
    }
}
