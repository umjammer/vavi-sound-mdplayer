/*
 * scratch: the fmdsp MML columns (GT / DT / M) as the generic chip source measures them
 */

package mdplayer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackInfo;
import vavi.sound.visualizer.fmdsp.TrackStatus;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


public class ChipStatusProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/PMD/SC3/GAY_07.M");
        int seconds = Integer.getInteger("probe.seconds", 20);
        boolean verbose = Boolean.getBoolean("probe.verbose");

        for (Path p : list(file)) {
            try {
                one(p, seconds, verbose);
            } catch (Exception e) {
                System.err.printf("%-32s FAILED %s%n", p.getFileName(), e);
            }
        }
    }

    static List<Path> list(String file) throws Exception {
        Path p = Path.of(file);
        if (!Files.isDirectory(p)) return List.of(p);
        try (var s = Files.walk(p)) {
            return s.filter(Files::isRegularFile).limit(12).toList();
        }
    }

    static void one(Path file, int seconds, boolean verbose) throws Exception {
        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Files.newInputStream(file), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin.chipRegister, plugin::getDriver);
        source.setFilename(file.getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        TrackStatus status = new TrackStatus();
        short[] buffer = new short[2 * 1024];
        int gates = 0, detunes = 0, slides = 0, lfos = 0, rows = 0, tones = 0;
        for (int i = 0; i < Common.VGMProcSampleRate * seconds / 1024; i++) {
            driver.render(buffer, 0, 1024);
            source.update(new vavi.util.event.GenericEvent(driver, "master", buffer, 0));
            if (i % 8 != 0) continue;
            StringBuilder b = new StringBuilder();
            for (TrackId t : TrackId.values()) {
                source.readStatus(t, status);
                if (!status.playing) continue;
                rows++;
                if (status.gate > 0) gates++;
                if (status.detune != 0) detunes++;
                if (status.status.charAt(7) == 'P') slides++;
                if (status.status.charAt(6) == 'H') lfos++;
                if (status.toneNum != 0) tones++;
                b.append("  %-9s k=%02x tn=%3d gt=%3d dt=%4d m=%s%n".formatted(
                        t, status.key, status.toneNum, status.gate, status.detune, status.status));
            }
            if (verbose && i % 128 == 0) {
                System.err.printf("--- %.1f s%n%s", i * 1024.0 / Common.VGMProcSampleRate, b);
            }
        }
        System.err.printf("%-32s rows=%-6d tone=%-6d gate=%-6d detune=%-6d slide=%-5d hwLfo=%d%n",
                file.getFileName(), rows, tones, gates, detunes, slides, lfos);

        String out = System.getProperty("probe.out");
        if (out == null || Files.exists(Path.of(out))) return;
        shot(source, out);
    }

    static void shot(ChipFmDspSource source, String out) throws Exception {

        var visualizer = new vavi.sound.visualizer.fmdsp.FmDspVisualizer(60);
        visualizer.setDataSource(source);
        visualizer.setSize(visualizer.getPreferredSize());
        visualizer.doLayout();
        var image = new java.awt.image.BufferedImage(visualizer.getWidth(), visualizer.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 120; i++) visualizer.paint(image.getGraphics());
        javax.imageio.ImageIO.write(image, "png", Path.of(out).toFile());
        System.err.printf("wrote %s%n", out);
    }
}
