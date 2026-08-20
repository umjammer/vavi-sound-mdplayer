/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vavi.sound.visualizer.fmdsp.WorkStateSource.VolumePart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * The {@code VOLUME DOWN} counter, which 98fmplayer's fmdsp draws the frame of and never fills.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
class VolumeDownTest {

    /** the counter row: three digits, the sign place, and the part name, as the visualizer has them */
    private static final int ROW_Y = 22 + 19 * 4;
    private static final int NUM_X = 568 + 8 * 5;
    private static final int SIGN_X = NUM_X - 8;
    private static final int PART_X = NUM_X - 27;

    /** the middle segment of a digit cell, which is the counter's minus */
    private static final int BAR_X = 3;
    private static final int BAR_Y = 5;

    /** palette 0, fully faded in */
    private static final int[] RGB = {
            new Color(0, 0, 0).getRGB(), new Color(170, 170, 153).getRGB(),
            new Color(102, 136, 255).getRGB(), new Color(68, 68, 119).getRGB(),
    };

    /** a source whose four volume corrections the test dictates */
    private static class Work implements WorkStateSource {
        final Map<VolumePart, Integer> down = new EnumMap<>(VolumePart.class);
        @Override public long generatedFrames() { return 0; }
        @Override public long timerBCount() { return 0; }
        @Override public int timerB() { return 200; }
        @Override public int loopCount() { return 0; }
        @Override public long loopTimerBCount() { return 0; }
        @Override public long timerBCountLoop() { return 0; }
        @Override public boolean playing() { return true; }
        @Override public boolean paused() { return false; }
        @Override public int volumeDown(VolumePart part) { return down.getOrDefault(part, 0); }
    }

    private static FmDspVisualizer visualizer(Work work) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        vis.setDataSource(new FmDspDataSource() {
            @Override public FftDataSource fft() { return null; }
            @Override public LevelDataSource level() { return null; }
            @Override public TrackStatusSource trackStatus() { return null; }
            @Override public WorkStateSource work() { return work; }
        });
        return vis;
    }

    private static BufferedImage render(FmDspVisualizer vis, int frames) {
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        for (int i = 0; i < frames; i++) {
            vis.paint(g);
        }
        g.dispose();
        return image;
    }

    /**
     * The digit an 8x11 cell holds, by matching every pixel against the sprite - a seven segment
     * glyph draws all of its segments, the lit ones only differing in colour, so nothing less than
     * the whole cell tells two digits apart. 10 is the blank glyph, -1 no match at all.
     */
    private static int digitAt(BufferedImage image, int x, int y) {
        for (int g = 0; g < 11; g++) {
            if (matches(image, x, y, g, false)) return g;
        }
        // the blank place the minus sits in is the blank glyph with its middle segment lit
        return matches(image, x, y, 10, true) ? 10 : -1;
    }

    /** whether a cell is the sprite of {@code g}, optionally leaving its middle segment out of it */
    private static boolean matches(BufferedImage image, int x, int y, int g, boolean anyMidBar) {
        for (int dy = 0; dy < 11; dy++) {
            for (int dx = 0; dx < 8; dx++) {
                if (anyMidBar && dy == BAR_Y && dx >= BAR_X && dx < BAR_X + 2) continue;
                int want = RGB[FmDspSprites.s_num[g * 88 + dy * 8 + dx] & 0xff];
                if (image.getRGB(x + dx, y + dy) != want) return false;
            }
        }
        return true;
    }

    /** the digits read as one number, blank places skipped; -1 when a cell is neither */
    private static int valueOf(BufferedImage image) {
        int v = 0;
        for (int i = 0; i < 3; i++) {
            int d = digitAt(image, NUM_X + 8 * i, ROW_Y);
            if (d < 0) return -1;
            if (d == 10) continue; // a place the number does not reach
            v = v * 10 + d;
        }
        return v;
    }

    /** whether a cell's middle segment is lit, which is the counter's minus */
    private static boolean minusAt(BufferedImage image, int x) {
        return image.getRGB(x + BAR_X, ROW_Y + BAR_Y) == RGB[2]
                && image.getRGB(x + BAR_X + 1, ROW_Y + BAR_Y) == RGB[2];
    }

    /** the digit place the minus is in, 0 the sign place beside the field; -1 when there is none */
    private static int minusPlace(BufferedImage image) {
        if (minusAt(image, SIGN_X)) return 0;
        for (int i = 0; i < 3; i++) {
            if (digitAt(image, NUM_X + 8 * i, ROW_Y) == 10 && minusAt(image, NUM_X + 8 * i)) {
                return i + 1;
            }
        }
        return -1;
    }

    /** the number of blank glyphs in front of the number */
    private static int blanks(BufferedImage image) {
        int n = 0;
        for (int i = 0; i < 3 && digitAt(image, NUM_X + 8 * i, ROW_Y) == 10; i++) {
            n++;
        }
        return n;
    }

    /** the part label's pixels, for comparing one label against another */
    private static long labelOf(BufferedImage image) {
        long hash = 0;
        for (int dy = 0; dy < 6; dy++) {
            for (int dx = 0; dx < 15; dx++) {
                hash = hash * 31 + image.getRGB(PART_X + dx, ROW_Y + 5 + dy);
            }
        }
        return hash;
    }

    @Test
    @DisplayName("a part turned down shows its name, the amount, and the minus in front of it")
    void showsTheCorrection() {
        Work work = new Work();
        work.down.put(VolumePart.FM, -18);
        BufferedImage image = render(visualizer(work), 120);

        assertEquals(18, valueOf(image), "the amount");
        assertEquals(1, blanks(image), "no leading zero, one blank place in front of the two digits");
        assertEquals(1, minusPlace(image), "and the minus in that place, not beside the field");
    }

    @Test
    @DisplayName("a number that fills the field pushes the minus out beside it")
    void threeDigitsMoveTheSign() {
        Work work = new Work();
        work.down.put(VolumePart.FM, -192);
        BufferedImage image = render(visualizer(work), 120);

        assertEquals(192, valueOf(image));
        assertEquals(0, blanks(image), "all three places are the number");
        assertEquals(0, minusPlace(image), "so the sign is in the place beside the field");
    }

    @Test
    @DisplayName("no correction at all: a bare zero, no leading zeros and no sign")
    void showsZero() {
        BufferedImage image = render(visualizer(new Work()), 120);

        assertEquals(0, valueOf(image));
        assertEquals(2, blanks(image), "one digit, so two blank places");
        assertEquals(-1, minusPlace(image), "nothing was turned down, so no minus anywhere");
    }

    @Test
    @DisplayName("a part turned up is the number alone, the counter having no plus to show")
    void showsAnIncreaseUnsigned() {
        Work work = new Work();
        work.down.put(VolumePart.SSG, 6);
        BufferedImage image = render(visualizer(work), 120);

        assertEquals(6, valueOf(image));
        assertEquals(-1, minusPlace(image));
    }

    @Test
    @DisplayName("the counter opens on the part that has a correction, not on the first part")
    void opensOnTheCorrectedPart() {
        Work fm = new Work();
        fm.down.put(VolumePart.FM, -12);
        Work pcm = new Work();
        pcm.down.put(VolumePart.PCM, -12);

        BufferedImage first = render(visualizer(fm), 120);
        BufferedImage second = render(visualizer(pcm), 120);

        assertEquals(12, valueOf(first));
        assertEquals(12, valueOf(second));
        assertNotEquals(labelOf(first), labelOf(second), "FM and PCM are not the same label");
    }

    @Test
    @DisplayName("it switches to whichever part moved last")
    void switchesToTheChangedPart() {
        Work work = new Work();
        work.down.put(VolumePart.FM, -12);
        FmDspVisualizer vis = visualizer(work);
        BufferedImage onFm = render(vis, 120);
        assertEquals(12, valueOf(onFm));

        work.down.put(VolumePart.RHY, -7);
        BufferedImage onRhy = render(vis, 1);

        assertEquals(7, valueOf(onRhy), "the counter followed the part that moved");
        assertNotEquals(labelOf(onFm), labelOf(onRhy), "and so did its label");

        // and it stays there while nothing else moves
        BufferedImage still = render(vis, 5);
        assertEquals(7, valueOf(still));
        assertEquals(labelOf(onRhy), labelOf(still));
    }

    @Test
    @DisplayName("a source with nothing to say leaves the counter at zero")
    void noSourceIsZero() {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        BufferedImage image = render(vis, 120);

        assertEquals(0, valueOf(image));
        assertEquals(-1, minusPlace(image));
    }
}
