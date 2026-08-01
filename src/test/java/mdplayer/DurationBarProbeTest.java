/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdplayer.Setting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.sound.visualizer.fmdsp.FmDspDataSource;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.WorkStateSource;
import vavi.util.archive.Archives;
import vavi.util.event.GenericEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Probes the duration bar for various real files via full plugin pipeline.
 * Prints WorkStateSource values each second and asserts the bar moves.
 *
 * Run with: mvn test -Dtest=DurationBarProbeTest -Dvavi.test=ai
 */
public class DurationBarProbeTest {

    /**
     * Load a file through the standard plugin pipeline, render 5 seconds of audio,
     * check that timerBCount advances and the rendered duration bar moves.
     */
    private void probe(String file) throws Exception {
        System.err.printf("%n=== PROBE: %s ===%n", Path.of(file).getFileName());

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "midiMode", 0, "songNo", 0));
        plugin.prepare();

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);

        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setDataSource((FmDspDataSource) source);
        vis.setSize(640, 400);

        // Paint initial frame (before any audio)
        BufferedImage imgBefore = paintFrame(vis);

        int sampleRate = Setting.getInstance().getOutputDevice().getSampleRate();
        // render in ~120 Hz chunks (same rate as the player's visualize interval)
        int chunkSamples = sampleRate / 120;
        short[] pcmBuf = new short[chunkSamples * 2]; // stereo

        System.err.println("  sec | timerBCount | timerBCountLoop | loopTimerBCount | totalTimerBCount | loopLen | pos");

        int totalSec = 5;
        int chunksPerSec = sampleRate / chunkSamples;

        for (int sec = 0; sec < totalSec; sec++) {
            for (int c = 0; c < chunksPerSec; c++) {
                // render one chunk
                plugin.getDriver().render(pcmBuf, 0, pcmBuf.length);
                // feed it to source as a "master" event so snapshot() fires
                source.update(new GenericEvent(plugin.getDriver(), "master", pcmBuf, 0));
            }

            WorkStateSource w = source;
            long tbc  = w.timerBCount();
            long tbcl = w.timerBCountLoop();
            long ltbc = w.loopTimerBCount();
            long ttbc = w.totalTimerBCount();

            // replicate FmDspVisualizer pos computation
            long loopLen = ltbc, loopPos = tbcl;
            if (loopLen <= 0) { loopLen = ttbc; loopPos = tbc; }
            if (loopLen <= 0) { loopLen = 1200; loopPos = tbc; }
            int pos = loopLen > 0 ? (int) ((loopPos % loopLen) * 69 / loopLen) : 0;

            System.err.printf("  %3d | %11d | %15d | %15d | %16d | %7d | %3d%n",
                    sec + 1, tbc, tbcl, ltbc, ttbc, loopLen, pos);
        }

        // Paint frame after 5 s of audio
        BufferedImage imgAfter = paintFrame(vis);

        // Assert the slider block moved somewhere in Y=68..74, X=352..495
        boolean moved = false;
        outer:
        for (int y = 68; y <= 74; y++) {
            for (int x = 352; x < 495; x++) {
                if (imgBefore.getRGB(x, y) != imgAfter.getRGB(x, y)) { moved = true; break outer; }
            }
        }
        System.err.println("  -> bar " + (moved ? "MOVED: OK" : "DID NOT MOVE: FAIL"));
        assertTrue(moved, "Duration bar should move after 5 seconds for: " + Path.of(file).getFileName());
    }

    private static BufferedImage paintFrame(FmDspVisualizer vis) {
        BufferedImage img = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        for (int i = 0; i < 20; i++) vis.paint(g);
        g.dispose();
        return img;
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probeVgzCirno() throws Exception {
        probe("../simplevgm/tmp/Touhou_Koumakyou_~_the_Embodiment_of_Scarlet_Devil._(IBM_PC_AT)/05 Tomboyish Girl in Love (Stage 2 Boss - Cirno's Theme).vgz");
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probeVgzOutRun() throws Exception {
        probe("../simplevgm/tmp/Out_Run_(Arcade)/01 Magical Sound Shower.vgz");
    }

    @Test
    @org.junit.jupiter.api.Disabled("FmpDriver requires FMP.COM in the Maven CWD; run manually from the FMP disk directory")
    void probeFmpFile() throws Exception {
        probe("/Users/nsano/Public/np2/FMP/FMPD1/76THSTAR.OZI");
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probePmdFile() throws Exception {
        probe("/Users/nsano/Public/np2/PMD/SC3/POP_TM.MZ");
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probeMoonDriver() throws Exception {
        probe("../vavi-sound-moon/tmp/TIMESUP/TIMESUP.MDR");
    }
}
