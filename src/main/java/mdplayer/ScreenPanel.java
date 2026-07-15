/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


/**
 * The component the skinned pixel screen is presented on.
 * <p>
 * {@link DrawBuff} blits sprites into a {@link FrameBuffer}'s pixel store; this panel is the other
 * end of that pipe, painting the resulting image magnified by an integer zoom. Magnification uses
 * nearest neighbour so the pixel art stays crisp.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-14 nsano initial version <br>
 */
public class ScreenPanel extends JPanel {

    /** the frame buffer's image, painted as is */
    private BufferedImage image;

    /** integer magnification */
    private int zoom = 1;

    public ScreenPanel() {
        setOpaque(true);
    }

    /** Binds the frame buffer image this panel presents. */
    public void bind(BufferedImage image, int zoom) {
        this.image = image;
        this.zoom = Math.max(1, zoom);
        setPreferredSize(new Dimension(image.getWidth() * this.zoom, image.getHeight() * this.zoom));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(image, 0, 0, image.getWidth() * zoom, image.getHeight() * zoom, null);
        } finally {
            g2d.dispose();
        }
    }
}
