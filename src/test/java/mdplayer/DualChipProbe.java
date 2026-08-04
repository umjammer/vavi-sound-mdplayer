/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import javax.imageio.ImageIO;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * What a song's rows show, row by row, plus a frame of the view - for the VGMs that declare two of
 * one chip, where the second chip's channels take the rows the first one leaves. Not a regression
 * test.
 * <p>
 * {@code -Dvavi.test=diag -Ddiag.file=<song> [-Ddiag.seconds=12] [-Ddiag.shots=8.0]
 * [-Ddiag.out=<dir>]}. The times are rendered ones - the buffer is a short array, two of them to
 * the frame, so a second of them is half a second of music.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-04 nsano initial version <br>
 */
class DualChipProbe {

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

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);

        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        vis.setDataSource(source);
        double[] shots = Arrays.stream(System.getProperty("diag.shots", "").split(","))
                .filter(s -> !s.isEmpty()).mapToDouble(Double::parseDouble).toArray();
        int shot = 0;

        TrackStatus status = new TrackStatus();
        short[] buffer = new short[512];
        double seconds = Double.parseDouble(System.getProperty("diag.seconds", "12"));
        int chunks = (int) (seconds * 44100 / buffer.length);
        int[] keyOns = new int[TrackId.COUNT];
        int[] prevKeys = new int[TrackId.COUNT];
        boolean[] prevPlaying = new boolean[TrackId.COUNT];
        String[] names = new String[TrackId.COUNT];
        Arrays.fill(prevKeys, 0xff);

        for (int chunk = 0; chunk < chunks; chunk++) {
            plugin.getDriver().render(buffer, 0, buffer.length);
            double time = chunk * buffer.length / 44100.0;
            source.snapshot();

            for (TrackId track : TrackId.values()) {
                source.readStatus(track, status);
                int row = track.ordinal();
                if (status.playing && status.key != 0xff
                        && (!prevPlaying[row] || status.key != prevKeys[row])) {
                    keyOns[row]++;
                    names[row] = source.trackTypeName(track) + source.trackNumber(track);
                }
                prevKeys[row] = status.playing ? status.key : 0xff;
                prevPlaying[row] = status.playing;
            }

            if (shot < shots.length && time >= shots[shot]) {
                BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
                var g = image.createGraphics();
                for (int i = 0; i < 120; i++) vis.paint(g); // the palette fades in
                g.dispose();
                Path out = Path.of(System.getProperty("diag.out", "tmp"),
                        "fmdsp-%.1fs.png".formatted(shots[shot]));
                ImageIO.write(image, "png", out.toFile());
                System.err.println("wrote " + out);
                shot++;
            }
        }

        System.err.println("--- rows that played, and the key ons each showed");
        for (TrackId track : TrackId.values()) {
            int row = track.ordinal();
            if (keyOns[row] == 0) continue;
            System.err.printf("  %-12s %-6s %d%n", track, names[row], keyOns[row]);
        }
    }
}
