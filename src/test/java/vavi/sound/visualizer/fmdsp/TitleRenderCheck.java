/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Headless diagnostic: renders the header band and dumps it as ASCII, to eyeball the custom
 * title and version against the original logo sprites. Not a regression test.
 * <p>
 * Run with {@code -Dvavi.test=diag -Ddiag.title=MDPLAYER -Ddiag.version=0.0.19}.
 */
class TitleRenderCheck {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        String title = System.getProperty("diag.title");
        String version = System.getProperty("diag.version");
        if (title != null) vis.setTitle(title);
        if (version != null) vis.setVersion(version);

        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        vis.setSize(640, 400);
        vis.paint(g);
        g.dispose();

        // the header band: the logo, the "MUSIC FILE SELECTOR" line and the version
        System.err.println("title=" + title + " version=" + version);
        for (int y = 0; y < 16; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 305; x < 640; x++) {
                sb.append((image.getRGB(x, y) & 0xffffff) != 0 ? '#' : '.');
            }
            System.err.println(sb);
        }
    }
}
