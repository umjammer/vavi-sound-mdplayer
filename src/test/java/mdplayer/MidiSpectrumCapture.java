/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;

import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Headless diagnostic: plays a song at its own speed with the display drawn over it, printing the
 * analyzer bars twice a second, and saves the last frame as a png. Not a regression test -
 * {@link MidiSpectrumTest} is.
 * <p>
 * Two things it is here to show, neither of which one frame can: that the bars are there at all,
 * and that they move. A spectrum that stands still is as wrong as an empty one - and for a source
 * with no PCM behind it, standing still is the failure it falls into. The final frame is measured
 * off the pixels rather than trusted to the eye, since the bars are 3 px wide over a 2 px grid and
 * a scaled down screenshot flattens them into an even texture whichever way they stand.
 * <p>
 * Run with {@code -Dvavi.test=diag}, optionally {@code -Ddiag.file=<path>},
 * {@code -Ddiag.seconds=<n>} and {@code -Ddiag.dir=<directory>}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-31 nsano initial version <br>
 */
class MidiSpectrumCapture {

    /** where the renderer puts the analyzer: its left edge and its baseline */
    private static final int SPECTRUM_X = 352, SPECTRUM_Y = 207;

    /** how much of the song to play, and the 1024 short blocks that takes */
    private static final int SECONDS = Integer.getInteger("diag.seconds", 12);
    private static final int BLOCKS_PER_SECOND = Common.VGMProcSampleRate * 2 / 1024;

    @BeforeEach
    void setup() throws Exception {
        LocalProperties.bind();
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void capture() throws Exception {
        Path file = Path.of(System.getProperty("diag.file", MidiSpectrumTest.FILE));
        if (!Files.exists(file)) {
            System.err.println(file + " is not there");
            return;
        }

        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(file))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);
        source.setFilename(file.getFileName().toString());

        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);
        visualizer.setSize(640, 400);
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        short[] buffer = new short[1024]; // interleaved stereo, so half of it is 512 frames
        long blockNanos = 1_000_000_000L * (buffer.length / 2) / Common.VGMProcSampleRate;
        int[] bars = new int[FftDataSource.LENGTH];
        int peak = 0;
        try {
            // Painted as it goes and paced to real time. Both matter: a frame taken from a source
            // that was never drawn from shows the first frame of an animation, and the bars' fall
            // is timed off the wall clock, so a song rendered flat out plays a minute of music
            // under a second of ballistics and the display never moves.
            long started = System.nanoTime();
            for (int i = 0; i < SECONDS * BLOCKS_PER_SECOND; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
                source.snapshot();
                visualizer.paint(g);
                source.fft().readFft(bars);
                peak = 0;
                for (int bar : bars) peak = Math.max(peak, bar);
                // twice a second, what the bars are doing - a wall of them that never changes is
                // as broken as an empty one, and neither says so in a single frame
                if (i % (BLOCKS_PER_SECOND / 2) == 0) {
                    System.err.println(profile(bars));
                    System.err.println(keys(plugin.chipRegister.plugin(MidiPlugin.class)));
                }
                long sleep = started + i * blockNanos - System.nanoTime();
                if (sleep > 0) Thread.sleep(sleep / 1_000_000, (int) (sleep % 1_000_000));
            }
            // the palette fades in over a couple of seconds, and until it has, every frame is black
            for (int i = 0; i < 120; i++) visualizer.paint(g);
        } finally {
            g.dispose();
            try {
                plugin.stop();
                plugin.chipRegister.plugin(MidiPlugin.class).allSoundOff();
                plugin.close();
            } catch (Exception ignore) {
                // a driver that will not shut down cleanly is not this diagnostic's business
            }
        }

        File out = new File(System.getProperty("diag.dir", "tmp"), "midi_spectrum.png");
        ImageIO.write(image, "png", out);
        System.err.println("peak bar: " + peak + ", saved: " + out);

        // how tall each bar stands in the frame that was saved, read back off the pixels
        StringBuilder sb = new StringBuilder("drawn: ");
        for (int x = 0; x < FftDataSource.LENGTH; x++) {
            int lit = 0;
            for (int y = 0; y < 32; y++) {
                // three colours down a column: the lit part, the empty part above it, and the
                // peak marker hanging over both. Only the first is warm
                int rgb = image.getRGB(SPECTRUM_X + x * 4, SPECTRUM_Y - y * 2);
                if (((rgb >> 16) & 0xff) > 0x50) lit = y + 1;
            }
            sb.append(Character.forDigit(Math.min(lit, 31), 32));
        }
        System.err.println(sb);
    }

    /**
     * Keys each of the sixteen channels is holding, which is the other half of reading the bars: a
     * stream is not obliged to let go of anything, and a ZMS drum channel collects hits it never
     * sends a note off for. A count that only ever climbs is where a spectrum drawn from held
     * notes goes wrong - see {@link mdplayer.fmdsp.MidiReader#spectral}.
     */
    private static String keys(MidiPlugin midi) {
        StringBuilder sb = new StringBuilder("keys:  ");
        for (int ch = 0; ch < 16; ch++) {
            int held = 0;
            for (int note = 0; note < 128; note++) {
                if (midi.key(ch, note)) held++;
            }
            sb.append(held).append(' ');
        }
        return sb.toString();
    }

    /** one frame of bars as a line, a character each: base 32, so {@code 0}-{@code v} is the axis */
    private static String profile(int[] bars) {
        StringBuilder sb = new StringBuilder("bars:  ");
        for (int bar : bars) sb.append(Character.forDigit(Math.clamp(bar, 0, 31), 32));
        return sb.toString();
    }
}
