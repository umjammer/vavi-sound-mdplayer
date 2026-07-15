
package mdplayer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import static java.lang.System.getLogger;


/**
 * The pixel store the skinned screens are drawn into.
 * <p>
 * {@link DrawBuff} blits sprites by writing raw bytes into {@link #baPlaneBuffer}. That array is the
 * backing store of {@link #bmpPlane}, so a write is immediately visible to Swing and no copy is
 * needed per frame; {@link #refresh} only has to ask the component to repaint.
 * <p>
 * Pixels are 4 interleaved bytes in {@code R,G,B,A} order — the layout {@link #clearScreen} and
 * {@link #drawByteArrayTransp}'s green colour key already assume.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-14 nsano initial version <br>
 */
public class FrameBuffer {

    private static final Logger logger = getLogger(FrameBuffer.class.getName());

    /** bytes per pixel: R, G, B, A */
    private static final int BPP = 4;

    public JComponent pbScreen;
    public BufferedImage bmpPlane;
    public int bmpPlaneW = 0;
    public int bmpPlaneH = 0;
    public byte[] baPlaneBuffer;
    public int zoom = 1;
    public Dimension imageSize = new Dimension(0, 0);

    /**
     * Binds this buffer to the component that presents it, sized after the skin image, which is
     * blitted in as the background.
     */
    public void Add(JComponent pbScreen, BufferedImage initialImage, Consumer<Graphics> p, int zoom) {
        this.zoom = Math.max(1, zoom);
        this.pbScreen = pbScreen;

        bmpPlaneW = initialImage.getWidth();
        bmpPlaneH = initialImage.getHeight();
        imageSize = new Dimension(bmpPlaneW, bmpPlaneH);

        // baPlaneBuffer is bmpPlane's pixel store, so DrawBuff writes straight into the image
        DataBufferByte dataBuffer = new DataBufferByte(bmpPlaneW * bmpPlaneH * BPP);
        baPlaneBuffer = dataBuffer.getData();
        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
                bmpPlaneW, bmpPlaneH, bmpPlaneW * BPP, BPP, new int[] {0, 1, 2, 3}, null);
        ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        bmpPlane = new BufferedImage(colorModel, raster, false, null);

        clearScreen();
        drawImage(initialImage);

        if (pbScreen instanceof ScreenPanel screenPanel) {
            screenPanel.bind(bmpPlane, this.zoom);
        } else if (pbScreen != null) {
            logger.log(Level.WARNING, "not a ScreenPanel, nothing will be painted: " + pbScreen.getClass().getName());
        }
    }

    /** Blits the skin into the buffer as the background. */
    private void drawImage(BufferedImage image) {
        int w = Math.min(bmpPlaneW, image.getWidth());
        int h = Math.min(bmpPlaneH, image.getHeight());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = image.getRGB(x, y);
                int adr = (bmpPlaneW * y + x) * BPP;
                baPlaneBuffer[adr] = (byte) (argb >> 16); // R
                baPlaneBuffer[adr + 1] = (byte) (argb >> 8); // G
                baPlaneBuffer[adr + 2] = (byte) argb; // B
                baPlaneBuffer[adr + 3] = (byte) (argb >> 24); // A
            }
        }
    }

    public void remove(Consumer<Graphics> p) {
        if (bmpPlane != null) {
            bmpPlane.flush();
            bmpPlane = null;
        }
        pbScreen = null;
        baPlaneBuffer = null;
    }

    public void clearScreen() {
        if (baPlaneBuffer == null) return;

        for (int i = 0; i < baPlaneBuffer.length; i += BPP) {
            baPlaneBuffer[i] = 0x00; // R
            baPlaneBuffer[i + 1] = 0x00; // G
            baPlaneBuffer[i + 2] = 0x00; // B
            baPlaneBuffer[i + 3] = (byte) 0xff; // a
        }
    }

    /** Presents what has been drawn since the last call. */
    public void refresh(Graphics g) {
        JComponent c = pbScreen;
        if (c == null) return;

        SwingUtilities.invokeLater(c::repaint);
    }

    public void drawByteArray(int x, int y, byte[] src, int srcWidth, int imgX, int imgY, int imgWidth, int imgHeight) {
        if (bmpPlane == null) {
            return;
        }

        try {
            int adr1;
            int adr2;
            int wid = bmpPlaneW * 4;
            adr1 = wid * y + x * 4;
            adr2 = srcWidth * 4 * imgY + imgX * 4;
            for (int i = 0; i < imgHeight; i++) {
                if (adr1 >= 0 && adr2 >= 0) {
                    for (int j = 0; j < imgWidth * 4; j++) {
                        if (baPlaneBuffer == null) {
                            continue;
                        }

                        if (adr1 + j >= baPlaneBuffer.length) {
                            continue;
                        }
                        if (adr2 + j >= src.length) {
                            continue;
                        }
                        baPlaneBuffer[adr1 + j] = src[adr2 + j];
                    }
                }

                adr1 += wid;
                adr2 += srcWidth * 4;

            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public void drawByteArrayTransp(int x, int y, byte[] src, int srcWidth, int imgX, int imgY, int imgWidth, int imgHeight) {
        if (bmpPlane == null) {
            return;
        }

        try {
            int adr1;
            int adr2;
            int wid = bmpPlaneW * 4;
            adr1 = wid * y + x * 4;
            adr2 = srcWidth * 4 * imgY + imgX * 4;
            for (int i = 0; i < imgHeight; i++) {
                if (adr1 >= 0 && adr2 >= 0) {
                    for (int j = 0; j < imgWidth * 4; j += 4) {
                        if (baPlaneBuffer == null) {
                            continue;
                        }

                        if (adr1 + j >= baPlaneBuffer.length) {
                            continue;
                        }
                        if (adr2 + j >= src.length) {
                            continue;
                        }

                        if (src[adr2 + j + 0] == 0x00 && (src[adr2 + j + 1] & 0xff) == 0xff && src[adr2 + j + 2] == 0x00)
                            continue;

                        baPlaneBuffer[adr1 + j + 0] = src[adr2 + j + 0];
                        baPlaneBuffer[adr1 + j + 1] = src[adr2 + j + 1];
                        baPlaneBuffer[adr1 + j + 2] = src[adr2 + j + 2];
                        baPlaneBuffer[adr1 + j + 3] = src[adr2 + j + 3];
                    }
                }

                adr1 += wid;
                adr2 += srcWidth * 4;

            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
