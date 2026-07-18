
package mdplayer.form;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.JComponent;

import mdplayer.Setting;

import static java.lang.System.getLogger;


/**
 * The main screen's {@link FrameBuffer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-14 nsano initial version <br>
 */
public class DoubleBuffer implements Closeable {

    private static final Logger logger = getLogger(DoubleBuffer.class.getName());

    public FrameBuffer mainScreen;

    public Setting setting = null;

    public DoubleBuffer(JComponent pbMainScreen, BufferedImage initialImage, int zoom) {
        mainScreen = new FrameBuffer();
        mainScreen.add(pbMainScreen, initialImage, null, zoom);
    }

    @Override
    public void close() {
        if (mainScreen != null) {
            mainScreen.remove(null);
            mainScreen = null;
        }
    }

    public void refresh(Graphics g) {
        try {
            if (mainScreen != null) {
                mainScreen.refresh(g);
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
