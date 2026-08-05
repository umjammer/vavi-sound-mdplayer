/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.boids;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import vavi.sound.visualizer.boids.BoidsParams.Param;
import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.sound.visualizer.fmdsp.FmDspDataSource;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.sound.visualizer.fmdsp.TrackStatusSource;
import vavi.sound.visualizer.fmdsp.WorkStateSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Paints {@link BoidsVisualizer} into an image and measures what came out: that it draws at all,
 * that the colour follows the key, that a note swells the flock and that the frame keeps the boids
 * in.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
class BoidsVisualizerTest {

    static final int W = 400;
    static final int H = 300;

    /** the step every painted frame stands for */
    static final double STEP = 1 / 60d;

    /** One track whose key and level the test writes directly. */
    static class Manual implements FmDspDataSource, TrackStatusSource, LevelDataSource, WorkStateSource {

        int key = 0xff;
        int level;

        @Override public FftDataSource fft() { return null; }
        @Override public LevelDataSource level() { return this; }
        @Override public TrackStatusSource trackStatus() { return this; }
        @Override public WorkStateSource work() { return this; }

        @Override
        public TrackId[] displayTracks() {
            return new TrackId[] {TrackId.FM_1};
        }

        @Override
        public void readStatus(TrackId track, TrackStatus out) {
            out.playing = key != 0xff;
            out.key = key;
            out.actualKey = key;
            out.volume = level * 127 / 32767;
        }

        @Override public int level(int channel) { return channel == 0 ? level : 0; }
        @Override public Pan pan(int channel) { return Pan.CENTER; }
        @Override public TrackId track(int channel) { return channel == 0 ? TrackId.FM_1 : null; }

        @Override public long generatedFrames() { return 0; }
        @Override public long timerBCount() { return 0; }
        @Override public int timerB() { return 200; }
        @Override public int loopCount() { return 0; }
        @Override public long loopTimerBCount() { return 0; }
        @Override public long timerBCountLoop() { return 0; }
        @Override public boolean playing() { return key != 0xff; }
        @Override public boolean paused() { return false; }
    }

    /** Paints one frame into {@code image}. */
    static void paint(BoidsVisualizer vis, BufferedImage image) {
        Graphics2D g = image.createGraphics();
        try {
            vis.paint(g);
        } finally {
            g.dispose();
        }
    }

    static BoidsVisualizer visualizer(FmDspDataSource source) {
        BoidsVisualizer vis = new BoidsVisualizer(60, W, H);
        vis.setDataSource(source);
        vis.setSize(W, H);
        vis.setFixedTimeStep(STEP);
        return vis;
    }

    /** pixels that are not the background */
    static int lit(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0xffffff) != 0) count++;
            }
        }
        return count;
    }

    /**
     * The average hue of what was drawn, 0..1, averaged round the circle so that red does not come
     * out as cyan. Dim pixels are left out: the trails and the glow are the same hue but they carry
     * the least of it.
     */
    static double hue(BufferedImage image) {
        double sx = 0, sy = 0;
        float[] hsb = new float[3];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                java.awt.Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, hsb);
                if (hsb[2] < 0.3 || hsb[1] < 0.3) continue;
                double a = hsb[0] * 2 * Math.PI;
                sx += Math.cos(a) * hsb[2];
                sy += Math.sin(a) * hsb[2];
            }
        }
        double a = Math.atan2(sy, sx) / (2 * Math.PI);
        return a < 0 ? a + 1 : a;
    }

    /** How far the lit pixels lie from their own middle, in px: the size of the flock as drawn. */
    static double spread(BufferedImage image) {
        double sx = 0, sy = 0;
        int n = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0xffffff) == 0) continue;
                sx += x;
                sy += y;
                n++;
            }
        }
        if (n == 0) return 0;
        double cx = sx / n, cy = sy / n, sum = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0xffffff) == 0) continue;
                sum += Math.hypot(x - cx, y - cy);
            }
        }
        return sum / n;
    }

    @Test
    @DisplayName("a playing source puts something on the screen")
    void draws() {
        SyntheticSongSource source = new SyntheticSongSource();
        BoidsVisualizer vis = visualizer(source);
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 120; i++) {
            source.advance(STEP);
            paint(vis, image);
        }

        assertTrue(lit(image) > 200, "nothing was drawn: " + lit(image) + " px");
    }

    @Test
    @DisplayName("the key picks the colour")
    void colourFollowsKey() {
        // C and F#, half the scale apart, which is half the colour wheel apart at the default span
        double hueC = hueOf(0x40);
        double hueFs = hueOf(0x46);
        double apart = Math.abs(hueC - hueFs);
        apart = Math.min(apart, 1 - apart);
        assertTrue(apart > 0.35, "C and F# came out the same colour: " + hueC + " vs " + hueFs);

        // and the same key twice is the same colour
        assertEquals(hueC, hueOf(0x40), 0.02);
    }

    private double hueOf(int key) {
        Manual source = new Manual();
        source.key = key;
        source.level = 24000;
        BoidsVisualizer vis = visualizer(source);
        vis.getParams().set(Param.LABELS, 0); // the label is text, not flock
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 60; i++) paint(vis, image);
        return hue(image);
    }

    @Test
    @DisplayName("a note swells the flock and it gathers again afterwards")
    void beatExpandsThenShrinks() {
        Manual source = new Manual();
        BoidsVisualizer vis = visualizer(source);
        BoidsParams p = vis.getParams();
        p.set(Param.TRAIL, 0);      // measure this frame, not the last second of them
        p.set(Param.LABELS, 0);
        p.set(Param.GLOW, 0);
        p.set(Param.WANDER, 0);     // the swell should be the only thing moving them
        p.set(Param.ORBIT_SPEED, 0);
        p.set(Param.LINK_ALPHA, 0);
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        // at rest
        source.key = 0x40;
        source.level = 2000;
        for (int i = 0; i < 240; i++) paint(vis, image);
        double resting = spread(image);

        // a note lands
        source.key = 0x47;
        source.level = 32000;
        for (int i = 0; i < 12; i++) paint(vis, image); // 0.2 s, about the top of the swell
        double swollen = spread(image);

        // and dies away
        source.key = 0x47;
        source.level = 2000;
        for (int i = 0; i < 240; i++) paint(vis, image);
        double gathered = spread(image);

        assertTrue(swollen > resting * 1.2,
                "the beat did not spread the flock: %.1f -> %.1f".formatted(resting, swollen));
        assertTrue(gathered < swollen * 0.9,
                "the flock never gathered again: %.1f -> %.1f".formatted(swollen, gathered));
    }

    @Test
    @DisplayName("the frame keeps them in, however hard they are thrown at it")
    void staysInFrame() {
        Manual source = new Manual();
        BoidsVisualizer vis = visualizer(source);
        BoidsParams p = vis.getParams();
        p.set(Param.TRAIL, 0);
        p.set(Param.MAX_SPEED, Param.MAX_SPEED.max);
        p.set(Param.PULSE_KICK, Param.PULSE_KICK.max);
        p.set(Param.BOUNCE, Param.BOUNCE.max);
        p.set(Param.WANDER, Param.WANDER.max);
        p.set(Param.ORBIT_PULL, 0);
        p.set(Param.CROSSOVER, 0);
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        int blank = 0;
        for (int i = 0; i < 600; i++) {
            // a note every ten frames, each one kicking the flock at the walls again
            source.key = 0x40 + (i / 10) % 12;
            source.level = 32000;
            paint(vis, image);
            if (i > 60 && lit(image) == 0) blank++;
        }
        // a boid that got past the frame would never come back, and the screen would stay empty
        assertEquals(0, blank, "the flock left the window on " + blank + " frames");
    }

    @Test
    @DisplayName("parameters clamp, round trip and answer to presets")
    void parameters() throws Exception {
        BoidsParams p = new BoidsParams();
        assertEquals(Param.COUNT.def, p.get(Param.COUNT));

        p.set(Param.COUNT, 1e6);
        assertEquals(Param.COUNT.max, p.get(Param.COUNT));
        p.set(Param.COUNT, -5);
        assertEquals(Param.COUNT.min, p.get(Param.COUNT));
        p.set(Param.SHAPE, 1.6);
        assertEquals(2, p.getInt(Param.SHAPE), "a mode is a whole number");

        AtomicInteger fired = new AtomicInteger();
        p.addListener(fired::incrementAndGet);
        p.set(Param.TRAIL, 0.5);
        assertEquals(1, fired.get());
        p.set(Param.TRAIL, 0.5);
        assertEquals(1, fired.get(), "setting the same value again is not a change");

        Properties props = p.toProperties();
        BoidsParams other = new BoidsParams();
        other.fromProperties(props);
        for (Param q : Param.values()) assertEquals(p.get(q), other.get(q), 1e-9, q.name());

        Path file = Files.createTempFile("boids", ".properties");
        try {
            p.save(file);
            BoidsParams loaded = new BoidsParams();
            loaded.load(file);
            assertEquals(p.get(Param.TRAIL), loaded.get(Param.TRAIL), 1e-9);
        } finally {
            Files.deleteIfExists(file);
        }

        for (String name : BoidsParams.presetNames()) p.applyPreset(name);
        p.applyPreset("swarm");
        assertNotEquals(Param.COUNT.def, p.get(Param.COUNT), "a preset should change something");
        p.reset();
        assertEquals(Param.COUNT.def, p.get(Param.COUNT));
    }
}
