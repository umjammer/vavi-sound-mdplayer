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
 * The song title on the file bar, which has the PCM1 bar to its right to stay clear of.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
class FileBarTest {

    /** the title starts here, and the PCM1 bar that shares its row starts at 463 */
    private static final int NAME_X = 137, PCM1_X = 463, ROW_Y = 324, ROW_H = 8;

    private BufferedImage render(String filename) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setDataSource(new FmDspDataSource() {
            @Override public FftDataSource fft() { return null; }
            @Override public LevelDataSource level() { return null; }
            @Override public TrackStatusSource trackStatus() { return null; }
            @Override public WorkStateSource work() {
                return new WorkStateSource() {
                    @Override public long generatedFrames() { return 0; }
                    @Override public long timerBCount() { return 0; }
                    @Override public int timerB() { return 0; }
                    @Override public int loopCount() { return 0; }
                    @Override public long loopTimerBCount() { return 0; }
                    @Override public long timerBCountLoop() { return 0; }
                    @Override public boolean playing() { return true; }
                    @Override public boolean paused() { return false; }
                    @Override public String filename() { return filename; }
                };
            }
        });
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        vis.setSize(640, 400);
        vis.paint(g);
        g.dispose();
        return image;
    }

    /**
     * The rightmost pixel the title itself put on the row. The PCM1 bar and label are painted at
     * 463 whatever the title is, so the title's own ink is what differs from an empty bar.
     */
    private int rightEdge(BufferedImage image) {
        BufferedImage blank = render(null);
        int edge = -1;
        for (int y = ROW_Y; y < ROW_Y + ROW_H; y++) {
            for (int x = NAME_X; x < 640; x++) {
                if (image.getRGB(x, y) != blank.getRGB(x, y)) edge = Math.max(edge, x);
            }
        }
        return edge;
    }

    @Test
    @DisplayName("a title too long for the bar is cut short of the PCM1 label")
    void testLongTitleIsClipped() {
        // the name of the sample that overran it
        String name = "05 Tomboyish Girl in Love (Stage 2 Boss - Cirno's Theme).vgz";
        int edge = rightEdge(render(name));
        assertTrue(edge > NAME_X, "nothing was drawn");
        assertTrue(edge < PCM1_X, "the title reaches x=" + edge + ", over the PCM1 bar at " + PCM1_X);
    }

    @Test
    @DisplayName("a title that fits is drawn whole")
    void testShortTitleUntouched() {
        BufferedImage shortName = render("02 Theme.vgz");
        BufferedImage clipped = render("02 Theme.vgz...............................................");
        // the short one must not have been touched: it ends well before the bar
        assertTrue(rightEdge(shortName) < PCM1_X);
        // and a longer one really is longer, so the clip is width driven and not a fixed cut
        assertTrue(rightEdge(clipped) > rightEdge(shortName));
        assertTrue(rightEdge(clipped) < PCM1_X);
    }

    @Test
    @DisplayName("a full width title is measured by what it advances, not its length")
    void testFullWidthTitle() {
        // the ANK fonts draw nothing for these but the cursor still advances 14 px each
        int edge = rightEdge(render("東方紅魔郷 チルノのテーマ おてんば恋娘 ".repeat(4)));
        assertTrue(edge < PCM1_X, "a full width title reaches x=" + edge);
    }
}
