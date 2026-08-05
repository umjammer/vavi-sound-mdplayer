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

import mdplayer.chips.Rf5C68Chip;
import mdplayer.chips.Ym2612Chip;
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
 * Headless probe of a Sega VGM's RF5C68 and its two OPN2s: every key on the chips took against
 * every key on the fmdsp rows showed, plus a frame of the view itself. Not a regression test.
 * <p>
 * {@code -Dvavi.test=diag -Ddiag.file=<song> [-Ddiag.seconds=12] [-Ddiag.shots=1.3,8.0]
 * [-Ddiag.out=<dir>]}. The times are rendered ones - the buffer is a short array, two of them to
 * the frame, so a second of them is half a second of music.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
class Rf5c68Probe {

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

        // the same view the player shows, painted off screen at the times asked for
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        vis.setDataSource(source);
        double[] shots = Arrays.stream(System.getProperty("diag.shots", "").split(","))
                .filter(s -> !s.isEmpty()).mapToDouble(Double::parseDouble).toArray();
        int shot = 0;

        Rf5C68Chip rf = plugin.chipRegister.chip(Rf5C68Chip.class);
        Ym2612Chip opn2 = plugin.chipRegister.chip(Ym2612Chip.class);
        TrackStatus status = new TrackStatus();
        // 64 samples is fine enough to be the ground truth the display's own polling is judged by
        short[] buffer = new short[64];
        double seconds = Double.parseDouble(System.getProperty("diag.seconds", "12"));
        int chunks = (int) (seconds * 44100 / buffer.length);
        int[] rawKeys = new int[12];
        int[] rawEdges = new int[12];
        int[] shownEdges = new int[12];
        int[] prevShownKey = new int[12];
        boolean[] prevShownPlaying = new boolean[12];
        Arrays.fill(prevShownKey, 0xff);

        for (int chunk = 0; chunk < chunks; chunk++) {
            plugin.getDriver().render(buffer, 0, buffer.length);
            double time = chunk * buffer.length / 44100.0;

            // ground truth: every key on the chips' caches ever held
            for (int chip = 0; chip < 2; chip++) {
                int[] keys = opn2.keyOn[chip];
                if (keys == null) continue;
                for (int ch = 0; ch < 6; ch++) {
                    int index = chip * 6 + ch;
                    if (keys[ch] == rawKeys[index]) continue;
                    if ((keys[ch] & 0xf0) != 0 && (rawKeys[index] & 0xf0) == 0) {
                        rawEdges[index]++;
                        System.err.printf("%7.3f raw  ym%d ch%d key on (%d)%n", time, chip, ch + 1, rawEdges[index]);
                    }
                    rawKeys[index] = keys[ch];
                }
            }

            if (chunk % 6 != 0) continue; // the rate the display polls at
            source.snapshot();
            TrackId[] tracks = source.displayTracks();
            for (TrackId track : tracks != null ? tracks : TrackId.values()) {
                source.readStatus(track, status);
                String name = source.trackTypeName(track);
                int num = source.trackNumber(track);
                if (!"FM".equals(name) || num < 1 || num > 12) continue;
                int index = num - 1;
                boolean started = status.playing && status.key != 0xff
                        && (!prevShownPlaying[index] || status.key != prevShownKey[index]);
                if (started) {
                    shownEdges[index]++;
                    System.err.printf("%7.3f show FM%d key %02x (%d)%n", time, num, status.key, shownEdges[index]);
                }
                prevShownKey[index] = status.playing ? status.key : 0xff;
                prevShownPlaying[index] = status.playing;
            }

            if (shot < shots.length && time >= shots[shot]) {
                BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
                var g = image.createGraphics();
                for (int i = 0; i < 120; i++) vis.paint(g); // the palette fades in: one paint is black
                g.dispose();
                Path out = Path.of(System.getProperty("diag.out", "tmp"),
                        "fmdsp-%.1fs.png".formatted(shots[shot]));
                ImageIO.write(image, "png", out.toFile());
                System.err.println("wrote " + out);
                shot++;
            }
        }

        System.err.println("--- key ons in " + seconds + "s: chip cache vs what the rows showed");
        for (int ch = 0; ch < 12; ch++) {
            System.err.printf("  ym%d ch%d raw=%d shown as FM%d=%d%n", ch / 6, ch % 6 + 1,
                    rawEdges[ch], ch + 1, shownEdges[ch]);
        }
        Map<String, Object> info = rf.getInfo(0);
        System.err.println("--- rf5c68 at the end");
        for (int ch = 0; ch < 8; ch++) {
            System.err.printf("  ch%d enable=%s key=%s env=%s step=%s pan=%s%n", ch,
                    info.get("channel." + ch + ".enable"), info.get("channel." + ch + ".key"),
                    info.get("channel." + ch + ".env"), info.get("channel." + ch + ".step"),
                    info.get("channel." + ch + ".pan"));
        }
    }
}
