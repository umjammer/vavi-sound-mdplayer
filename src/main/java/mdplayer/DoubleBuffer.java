
package mdplayer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.JComponent;

import static java.lang.System.getLogger;


public class DoubleBuffer implements Closeable {

    private static final Logger logger = getLogger(DoubleBuffer.class.getName());

    public FrameBuffer mainScreen;

    public Setting setting = null;

    public DoubleBuffer(JComponent pbMainScreen, BufferedImage initialImage, int zoom) {
        this.close();

        mainScreen = new FrameBuffer();
        mainScreen.Add(pbMainScreen, initialImage, g -> {}, zoom);
    }

    @Override
    public void close() {
        if (mainScreen != null)
            mainScreen.Remove(g -> {});
    }

    private void paint(Graphics g) {
        Refresh(g);
    }

    public void Refresh(Graphics g) {
        try {
            if (mainScreen != null) {
                try {
                    mainScreen.refresh(g);
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                    mainScreen.Remove(g2 -> {});
                    mainScreen = null;
                }
            }

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
