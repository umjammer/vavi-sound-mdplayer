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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * {@link FmDspVisualizer#setTitle}, which spells a title out of the "FMDSP" logo art when it can.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
class LogoTitleTest {

    /** the logo band, x 312..396 over the 12 rows the art occupies */
    private static final int X = 312, Y = 1, W = 85, H = 12;

    /** renders one frame headless and returns the logo band */
    private static int[] band(String title) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        if (title != null) vis.setTitle(title);
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        vis.setSize(640, 400);
        vis.paint(g);
        g.dispose();

        int[] band = new int[W * H];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                band[y * W + x] = image.getRGB(X + x, Y + y);
            }
        }
        return band;
    }

    private static int ink(int[] band) {
        int n = 0;
        for (int p : band) {
            if ((p & 0xffffff) != 0) n++;
        }
        return n;
    }

    @Test
    @DisplayName("a title the art can spell is the art itself")
    void testFmdspIsPixelIdentical() {
        // setting the logo's own name must reproduce the sprites exactly, letter for letter
        assertArrayEquals(band(null), band("FMDSP"));
    }

    @Test
    @DisplayName("MDDSP reuses the logo's M, D, S and P")
    void testMddsp() {
        int[] logo = band(null);
        int[] mddsp = band("MDDSP");
        assertFalse(java.util.Arrays.equals(logo, mddsp), "MDDSP is not FMDSP");

        // it is the art, not the font: same stroke weight, so a comparable amount of ink
        int inked = ink(mddsp);
        assertTrue(inked > ink(logo) * 0.8, "too little ink for the artwork: " + inked);

        // the D is the same glyph twice: columns 17..31 repeat 15+2 columns later
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < 15; x++) {
                assertArrayEquals(
                        new int[] {mddsp[y * W + 17 + x]},
                        new int[] {mddsp[y * W + 34 + x]},
                        "the two D glyphs differ at " + x + "," + y);
            }
        }
    }

    @Test
    @DisplayName("a title the art cannot spell falls back to the font")
    void testFallback() {
        // Y, L, A and R are not in "FMDSP", so this one is drawn with the vector font
        int[] band = band("MDPLAYER");
        assertTrue(ink(band) > 0, "nothing was drawn");
        assertFalse(java.util.Arrays.equals(band(null), band), "should not be the logo art");
    }

    @Test
    @DisplayName("neither path runs past the header text beside it")
    void testStaysInItsBand() {
        // x 397 is where "MUSIC FILE SELECTOR" starts; the title must not reach it
        for (String title : new String[] {"MDDSP", "FMDSP", "MDPLAYER", "MDPLAYER FOR JAVA"}) {
            int[] band = band(title);
            for (int y = 0; y < H; y++) {
                for (int x = 397 - X; x < W; x++) {
                    assertTrue((band[y * W + x] & 0xffffff) == 0,
                            title + " reaches x=" + (X + x));
                }
            }
        }
    }
}
