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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * Verifies dynamic highlighting of "FILE PCM 1 2 3 4" on the visualizer.
 */
class FilePcmHighlightTest {

    private static BufferedImage renderFloppy(String[] pcmNames, boolean[] pcmErrors) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
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
                    @Override public String pcmType(int i) { return i < 4 ? "PCM" + (i + 1) : null; }
                    @Override public String pcmFilename(int i) { return pcmNames != null && i < pcmNames.length ? pcmNames[i] : null; }
                    @Override public boolean pcmError(int i) { return pcmErrors != null && i < pcmErrors.length && pcmErrors[i]; }
                };
            }
        });
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        for (int i = 0; i < 20; i++) vis.paint(g);
        g.dispose();
        return image;
    }

    @Test
    @DisplayName("FILE PCM 1 2 are highlighted when slots 0 and 1 are loaded")
    void testFilePcmHighlighting() {
        BufferedImage noPcm = renderFloppy(null, null);
        BufferedImage slots01 = renderFloppy(new String[] {"PVI_FILE", "PPZ_FILE", null, null}, null);

        // FLOPPY_X = 432, FLOPPY_Y = 87
        // "FILE PCM" is at FLOPPY_X + 9..49 (X=441..481, including 'M' at X=477..481)
        // "1" is at FLOPPY_X + 52..54 (X=484..486)
        // "2" is at FLOPPY_X + 57..61 (X=489..493)
        // "3" is at FLOPPY_X + 63..67 (X=495..499)
        // "4" is at FLOPPY_X + 69..73 (X=501..505)

        // 'M' of "PCM" (X=477..481, Y=87..93) should be highlighted when PCM files exist
        boolean letterMHighlighted = false;
        for (int y = 87; y < 94; y++) {
            for (int x = 477; x <= 481; x++) {
                if (noPcm.getRGB(x, y) != slots01.getRGB(x, y)) {
                    letterMHighlighted = true;
                    break;
                }
            }
        }
        assertEquals(true, letterMHighlighted, "'M' of 'PCM' section (X=477..481) should be highlighted when PCM files exist");

        // Slot 0 ('1') section (X=484..486, Y=87..93) should be highlighted
        boolean slot0Highlighted = false;
        for (int y = 87; y < 94; y++) {
            for (int x = 484; x <= 486; x++) {
                if (noPcm.getRGB(x, y) != slots01.getRGB(x, y)) {
                    slot0Highlighted = true;
                    break;
                }
            }
        }
        assertEquals(true, slot0Highlighted, "Slot 0 ('1') should be highlighted when slot 0 is loaded");

        // Slot 3 ('4') section (X=501..505, Y=87..93) should NOT differ between noPcm and slots01
        boolean slot3Highlighted = false;
        for (int y = 87; y < 94; y++) {
            for (int x = 501; x <= 505; x++) {
                if (noPcm.getRGB(x, y) != slots01.getRGB(x, y)) {
                    slot3Highlighted = true;
                    break;
                }
            }
        }
        assertEquals(false, slot3Highlighted, "Slot 3 ('4') should NOT be highlighted when slot 3 is empty");
    }

    @Test
    @DisplayName("Slot with PCM error is highlighted in red color")
    void testFilePcmErrorHighlighting() {
        BufferedImage normal = renderFloppy(new String[] {"PVI_FILE", null, null, null}, new boolean[] {false});
        BufferedImage error = renderFloppy(new String[] {"PVI_FILE", null, null, null}, new boolean[] {true});

        // Slot 0 ('1') pixel color should differ when slot 0 has an error (red) vs normal (cyan)
        boolean errorColorDiffers = false;
        for (int y = 87; y < 94; y++) {
            for (int x = 484; x <= 486; x++) {
                if (normal.getRGB(x, y) != error.getRGB(x, y)) {
                    errorColorDiffers = true;
                    break;
                }
            }
        }
        assertEquals(true, errorColorDiffers, "Slot 0 ('1') color should differ on error");
    }
}
