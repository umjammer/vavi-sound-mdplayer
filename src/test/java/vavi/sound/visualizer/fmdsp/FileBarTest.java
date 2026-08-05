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

    private static BufferedImage render(String filename) {
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
        for (int i = 0; i < 20; i++) {
            vis.paint(g);
        }
        g.dispose();
        return image;
    }

    /**
     * The rightmost pixel the title itself put on the row. The PCM1 bar and label are painted at
     * 463 whatever the title is, so the title's own ink is what differs from an empty bar.
     */
    private static int rightEdge(BufferedImage image) {
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

    private static BufferedImage renderPcm(String pcm1, boolean pcm1Err, String pcm2, boolean pcm2Err) {
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
                    @Override public String pcmFilename(int index) { return index == 0 ? pcm1 : pcm2; }
                    @Override public boolean pcmError(int index) { return index == 0 ? pcm1Err : pcm2Err; }
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
    @DisplayName("PCM1 and PCM2 filenames are rendered on file bar")
    void testPcmFilenamesRendered() {
        BufferedImage blank = renderPcm(null, false, null, false);
        BufferedImage withPcm = renderPcm("DEMO.PPC", false, "DEMO.PPZ", false);

        boolean pcm1Drawn = false;
        for (int y = 320; y < 332; y++) {
            for (int x = 497; x < 549; x++) {
                if (withPcm.getRGB(x, y) != blank.getRGB(x, y)) {
                    pcm1Drawn = true;
                    break;
                }
            }
        }
        assertTrue(pcm1Drawn, "PCM1 filename was not drawn on the bar");

        boolean pcm2Drawn = false;
        for (int y = 320; y < 332; y++) {
            for (int x = 585; x < 638; x++) {
                if (withPcm.getRGB(x, y) != blank.getRGB(x, y)) {
                    pcm2Drawn = true;
                    break;
                }
            }
        }
        assertTrue(pcm2Drawn, "PCM2 filename was not drawn on the bar");
    }

    @Test
    @DisplayName("PCM error changes text color")
    void testPcmErrorColor() {
        BufferedImage normal = renderPcm("ERR.PPC", false, null, false);
        BufferedImage error = renderPcm("ERR.PPC", true, null, false);

        boolean colorDiff = false;
        for (int y = 320; y < 332; y++) {
            for (int x = 497; x < 549; x++) {
                if (normal.getRGB(x, y) != error.getRGB(x, y)) {
                    colorDiff = true;
                    break;
                }
            }
        }
        assertTrue(colorDiff, "PCM error did not change text color");
    }

    @Test
    @DisplayName("PCM filenames with paths and extensions are stripped to name only")
    void testPcmFilenamesWithPathAndExtensionStripped() {
        BufferedImage withPathAndExt = renderPcm("/path/to/DEMO.PPC", false, "C:\\DOS\\PATH\\DEMO.PPZ", false);
        BufferedImage simpleNameOnly = renderPcm("DEMO", false, "DEMO", false);

        boolean identical = true;
        for (int y = 320; y < 332; y++) {
            for (int x = 463; x < 640; x++) {
                if (withPathAndExt.getRGB(x, y) != simpleNameOnly.getRGB(x, y)) {
                    identical = false;
                    break;
                }
            }
        }
        assertTrue(identical, "Rendered PCM filenames with path and extension should match simple name only (path & ext stripped)");
    }

    private static BufferedImage renderFourPcm(String[] names, String[] types, boolean[] errors) {
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
                    @Override public String pcmType(int index) { return types != null && index < types.length ? types[index] : null; }
                    @Override public String pcmFilename(int index) { return names != null && index < names.length ? names[index] : null; }
                    @Override public boolean pcmError(int index) { return errors != null && index < errors.length ? errors[index] : false; }
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
    @DisplayName("4 PMD PCM slots (PPC, PPZ1, PPZ2, PPS) are rendered dynamically")
    void testFourPcmSlotsRendered() {
        String[] types = {"PPC", "PPZ1", "PPZ2", "PPS"};
        String[] names = {"DEMO.PPC", "VOICE1.PPZ", "VOICE2.PPZ", "DRUM.PPS"};
        boolean[] errors = {false, false, false, false};

        BufferedImage blank = renderFourPcm(null, null, null);
        BufferedImage fourPcm = renderFourPcm(names, types, errors);

        // Check that each of the 4 slots drawn something at its expected region
        // Slot 0 (PPC): X=287..375
        boolean slot0Drawn = false;
        for (int y = 320; y < 332; y++) {
            for (int x = 287; x < 370; x++) {
                if (fourPcm.getRGB(x, y) != blank.getRGB(x, y)) { slot0Drawn = true; break; }
            }
        }
        assertTrue(slot0Drawn, "PPC slot (slot 0) was not drawn");

        // Slot 3 (PPS): X=551..640
        boolean slot3Drawn = false;
        for (int y = 320; y < 332; y++) {
            for (int x = 551; x < 638; x++) {
                if (fourPcm.getRGB(x, y) != blank.getRGB(x, y)) { slot3Drawn = true; break; }
            }
        }
        assertTrue(slot3Drawn, "PPS slot (slot 3) was not drawn");
    }
}
