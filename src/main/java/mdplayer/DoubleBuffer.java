
package mdplayer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Closeable;


public class DoubleBuffer implements Closeable {

    public FrameBuffer mainScreen = null;

    public Setting setting = null;

    public DoubleBuffer(BufferedImage pbMainScreen, Image initialImage, int zoom) {
        this.close();

        mainScreen = new FrameBuffer();
        mainScreen.Add(pbMainScreen, initialImage, this.Graphics2D, zoom);
    }

    protected void finalize() {
        close();
    }

    public void close() {
        if (mainScreen != null)
            mainScreen.Remove(this.Graphics2D);
    }

    private void paint(Object sender, PaintEventArgs e) {
        Refresh();
    }

    public void Refresh() {
        try {
            if (mainScreen != null) {
                try {
                    mainScreen.Refresh(this.Graphics2D);
                } catch (Exception ex) {
                    Log.forcedWrite(ex);
                    mainScreen.Remove(this.Graphics2D);
                    mainScreen = null;
                }
            }

        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

}
