/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Verifies that the duration / loop progress bar at Y=70 in the right pane
 * renders and animates the progress slider block.
 */
class DurationBarTest {

    private static BufferedImage renderDuration(long timerBCountLoop, long loopTimerBCount, int loopCount) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        vis.setDataSource(new FmDspDataSource() {
            @Override public FftDataSource fft() { return null; }
            @Override public LevelDataSource level() { return null; }
            @Override public TrackStatusSource trackStatus() { return null; }
            @Override public WorkStateSource work() {
                return new WorkStateSource() {
                    @Override public long generatedFrames() { return 1000; }
                    @Override public long timerBCount() { return timerBCountLoop; }
                    @Override public int timerB() { return 200; }
                    @Override public int loopCount() { return loopCount; }
                    @Override public long loopTimerBCount() { return loopTimerBCount; }
                    @Override public long timerBCountLoop() { return timerBCountLoop; }
                    @Override public boolean playing() { return true; }
                    @Override public boolean paused() { return false; }
                };
            }
        });

        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        vis.setSize(640, 400);
        for (int i = 0; i < 20; i++) {
            vis.paint(g);
        }
        g.dispose();
        return image;
    }

    @Test
    @DisplayName("Duration bar slider block advances as timerBCountLoop increases")
    void testDurationBarSliderAdvances() {
        // Render at 10% progress vs 80% progress
        BufferedImage imgStart = renderDuration(100, 1000, 1);
        BufferedImage imgEnd = renderDuration(800, 1000, 1);

        // Check slider position difference in region Y=70..74, X=352..496
        boolean startBlockDrawn = false;
        boolean endBlockDrawn = false;
        for (int y = 68; y <= 74; y++) {
            for (int x = 352; x < 496; x++) {
                if (imgStart.getRGB(x, y) != imgEnd.getRGB(x, y)) {
                    if (x < 400) startBlockDrawn = true;
                    if (x > 440) endBlockDrawn = true;
                }
            }
        }

        assertTrue(startBlockDrawn, "Start region should differ between 10% and 80% progress");
        assertTrue(endBlockDrawn, "End region should differ between 10% and 80% progress");
    }
}
