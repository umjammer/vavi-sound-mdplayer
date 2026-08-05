/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * Stand-alone demo that wires {@link FmDspVisualizer} to a synthetic data
 * source. Press F1..F10 to switch palette, F11 / Shift+F11 to cycle the left /
 * right layout, ESC to exit.
 */
public final class FmDspVisualizerDemo {

    private FmDspVisualizerDemo() {}

    static void main(String[] args) {
        SwingUtilities.invokeLater(FmDspVisualizerDemo::launch);
    }

    private static void launch() {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        SyntheticSource data = new SyntheticSource();
        vis.setDataSource(data);

        JFrame frame = new JFrame("FMDSP / Java Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(vis, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F10) {
                    vis.setPaletteIndex(code - KeyEvent.VK_F1);
                } else if (code == KeyEvent.VK_F11) {
                    if (e.isShiftDown()) {
                        RightMode[] r = RightMode.values();
                        vis.setRightMode(r[(vis.getRightMode().ordinal() + 1) % r.length]);
                    } else {
                        LeftMode[] l = LeftMode.values();
                        vis.setLeftMode(l[(vis.getLeftMode().ordinal() + 1) % l.length]);
                    }
                } else if (code == KeyEvent.VK_SPACE) {
                    data.togglePause();
                } else if (code == KeyEvent.VK_ESCAPE) {
                    frame.dispose();
                }
            }
        });
        frame.requestFocusInWindow();

        vis.start();
    }

    /**
     * Synthetic data source that drives the visualizer with sine-derived
     * spectrum, randomized track activity and a moving level meter so the
     * UI elements can be exercised standalone.
     */
    private static final class SyntheticSource implements FmDspDataSource, FftDataSource, LevelDataSource, TrackStatusSource, WorkStateSource {

        private final Random rnd = new Random(0xc001cafeL);
        private final long startNanos = System.nanoTime();
        private boolean paused;
        private long pauseStartNanos;
        private long pausedAccumNanos;

        void togglePause() {
            if (!paused) {
                paused = true;
                pauseStartNanos = System.nanoTime();
            } else {
                paused = false;
                pausedAccumNanos += System.nanoTime() - pauseStartNanos;
            }
        }

        private double t() {
            long now = paused ? pauseStartNanos : System.nanoTime();
            return (now - startNanos - pausedAccumNanos) / 1.0e9;
        }

        @Override public FftDataSource fft() { return this; }
        @Override public LevelDataSource level() { return this; }
        @Override public TrackStatusSource trackStatus() { return this; }
        @Override public WorkStateSource work() { return this; }

        // ----- FFT -----
        @Override
        public void readFft(int[] out) {
            double t = t();
            for (int i = 0; i < FftDataSource.LENGTH; i++) {
                double f = i / (double) FftDataSource.LENGTH;
                double envelope = Math.exp(-f * 2.5) * 28;
                double wob = Math.sin(t * 2.0 + i * 0.3) * 0.5 + 0.5;
                int v = (int) Math.clamp(envelope * wob + rnd.nextInt(3), 0, 31);
                out[i] = v;
            }
        }

        // ----- Level -----
        @Override
        public int level(int channel) {
            double t = t() + channel * 0.3;
            double a = Math.abs(Math.sin(t * 1.5)) * Math.exp(-((channel - 9) * (channel - 9)) / 80.0);
            // 0..32767, with some channels muted-ish
            return (int) (a * 28000);
        }

        @Override
        public Pan pan(int channel) {
            int idx = (channel + (int) (t() * 0.5)) % 5;
            return switch (Math.floorMod(idx, 5)) {
                case 0 -> Pan.LEFT;
                case 1 -> Pan.MID_LEFT;
                case 2 -> Pan.CENTER;
                case 3 -> Pan.MID_RIGHT;
                default -> Pan.RIGHT;
            };
        }

        // ----- Tracks -----
        @Override
        public void readStatus(TrackId track, TrackStatus out) {
            double t = t() + track.ordinal() * 0.13;
            boolean playing = Math.sin(t) > -0.4 && track.ordinal() < 13;
            out.playing = playing;
            out.info = TrackInfo.NORMAL;
            out.ticks = 64;
            out.ticksLeft = (int) ((Math.sin(t * 2) * 0.5 + 0.5) * 64);
            // synthesize a key (C major arpeggio).
            int[] notes = {0, 4, 7, 11, 7, 4};
            int idx = (int) (t * 4) & 7;
            int n = notes[idx % notes.length];
            int oct = 3 + (track.ordinal() % 5);
            out.key = playing ? ((oct << 4) | n) : 0xff;
            out.actualKey = out.key;
            out.toneNum = 1 + track.ordinal();
            out.volume = 100 + (int) (Math.sin(t) * 20);
            out.gate = 200;
            out.detune = (int) (Math.sin(t * 3) * 50);
            out.status = playing ? "PLAY" : "STOP";
            out.ssgTone = false;
            out.ssgNoise = false;
            out.ssgNoiseFreq = 0;
        }

        @Override
        public boolean masked(TrackId track) {
            return track == TrackId.FM_6;
        }

        // ----- Work -----
        @Override
        public long generatedFrames() {
            return (long) (t() * sampleRate());
        }

        @Override public long timerBCount() { return (long) (t() * 200); }
        @Override public int timerB() { return 200; }
        @Override public int loopCount() { return ((int) (t() / 30)); }
        @Override public long loopTimerBCount() { return 6000; }
        @Override public long timerBCountLoop() { return ((long) (t() * 200)) % 6000; }
        @Override public boolean playing() { return !paused; }
        @Override public boolean paused() { return paused; }
        @Override public String driverName() { return "DEMO"; }
        @Override public String chips() { return "YM2608"; }
        @Override public String filename() { return "DEMO.M2"; }

        @Override
        public String comment(int line) {
            return switch (line) {
                case 0 -> "FMDSP Java Swing port - demo source";
                case 1 -> "F1..F10 palette  /  F11 layout  /  SPACE pause";
                case 2 -> "ESC quit";
                default -> null;
            };
        }
    }
}
