
package mdplayer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import javax.swing.JComponent;


public class DoubleBuffer implements Closeable {

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
                    mainScreen.Refresh(g);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mainScreen.Remove(g2 -> {});
                    mainScreen = null;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
