/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


/**
 * The two digit number beside a track row's name, which a source sets with
 * {@link TrackStatusSource#trackNumber}.
 * <p>
 * The digits are seven segment glyphs that always draw every segment: a lit one in the bright
 * colour and an unlit one in the dim one. So two different digits cover exactly the same pixels
 * and differ only in which of the two colours each pixel has - comparing them as set against
 * unset says every digit is the same digit, which is what this guards against.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class RowNumberRenderTest {

    /** the first row's number, drawn at NUM_X, y + 1 */
    private static final int NUM_X = 31;
    private static final int NUM_W = 8;
    private static final int NUM_H = 11;

    /** the glyph the row shows in place of a number while the channel is masked */
    private static final int MASKED_GLYPH = 10;

    @Test
    void aTwoDigitNumberDrawsItsTwoDigits() {
        BufferedImage image = render(42, false);

        assertArrayEquals(glyph(4), digit(image, 0), "the tens digit should be a 4");
        assertArrayEquals(glyph(2), digit(image, 1), "the units digit should be a 2");
    }

    @Test
    void everyDigitDrawsItself() {
        for (int n = 0; n <= 9; n++) {
            BufferedImage image = render(n, false);
            assertArrayEquals(glyph(0), digit(image, 0), n + ": the tens digit should be a 0");
            assertArrayEquals(glyph(n), digit(image, 1), n + ": the units digit should be itself");
        }
    }

    @Test
    void aMaskedRowDrawsNoNumber() {
        BufferedImage image = render(42, true);

        assertArrayEquals(glyph(MASKED_GLYPH), digit(image, 0));
        assertArrayEquals(glyph(MASKED_GLYPH), digit(image, 1));
    }

    /** paints one frame whose first row carries {@code number} */
    private static BufferedImage render(int number, boolean masked) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        vis.setDataSource(new FmDspDataSource() {
            @Override public FftDataSource fft() { return null; }
            @Override public LevelDataSource level() { return null; }
            @Override public WorkStateSource work() { return null; }
            @Override public TrackStatusSource trackStatus() {
                return new TrackStatusSource() {
                    @Override public void readStatus(TrackId track, TrackStatus out) {
                        out.playing = track == TrackId.FM_1;
                    }
                    @Override public boolean masked(TrackId track) {
                        return masked && track == TrackId.FM_1;
                    }
                    @Override public int trackNumber(TrackId track) {
                        return track == TrackId.FM_1 ? number : -1;
                    }
                    @Override public TrackId[] displayTracks() {
                        return new TrackId[] {TrackId.FM_1};
                    }
                };
            }
        });

        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        // the palette fades in over the first frames, so one paint would come out black
        for (int i = 0; i < 130; i++) vis.paint(g);
        g.dispose();
        return image;
    }

    /** the painted digit {@code i} of the first row, as its two colours */
    private static int[] digit(BufferedImage image, int i) {
        int[] out = new int[NUM_W * NUM_H];
        for (int y = 0; y < NUM_H; y++) {
            for (int x = 0; x < NUM_W; x++) {
                out[y * NUM_W + x] = image.getRGB(NUM_X + NUM_W * i + x, 1 + y) & 0xffffff;
            }
        }
        return out;
    }

    /** the same glyph out of the sprite table, in the colours the palette gives its indices */
    private static int[] glyph(int digit) {
        int[] out = new int[NUM_W * NUM_H];
        for (int i = 0; i < out.length; i++) {
            out[i] = colorOf(FmDspSprites.s_num[digit * NUM_W * NUM_H + i]);
        }
        return out;
    }

    /** the three palette entries the number sprites use */
    private static int colorOf(byte index) {
        return switch (index) {
            case 2 -> 0x6688ff;
            case 3 -> 0x444477;
            default -> 0x000000;
        };
    }
}
