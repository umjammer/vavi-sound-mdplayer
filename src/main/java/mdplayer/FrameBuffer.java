
package mdplayer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JPanel;

import vavi.util.Debug;


public class FrameBuffer {
    public JComponent pbScreen;
    public BufferedImage bmpPlane;
    public int bmpPlaneW = 0;
    public int bmpPlaneH = 0;
    public byte[] baPlaneBuffer;
    public Graphics2D bgPlane;
    public int zoom = 1;
    public Dimension imageSize = new Dimension(0, 0);

    public void Add(JComponent pbScreen, BufferedImage initialImage, Consumer<Graphics> p, int zoom) {
        this.zoom = zoom;
        this.pbScreen = pbScreen;
//        Graphics2DContext currentContext;
//        currentContext = Graphics2DManager.Current;
//        imageSize = new Dimension(initialImage.getWidth(), initialImage.getHeight());
//
//        pbScreen.setPreferredSize(new Dimension(imageSize.width * zoom, imageSize.height * zoom));
//
//        bgPlane = currentContext.Allocate(pbScreen.CreateGraphics(), pbScreen.DisplayRectangle);
//        if (p != null)
//            pbScreen.Graphics2D += new JPaintEventHandler(p);
//        bmpPlane = new BufferedImage(imageSize.getWidth(), imageSize.getHeight(), PixelFormat.Format32bppArgb);
//        bmpPlaneW = imageSize.width;
//        bmpPlaneH = imageSize.height;
//        BufferedImageData bdPlane = bmpPlane
//                .LockBits(new Rectangle(0, 0, bmpPlane.getWidth(), bmpPlane.getHeight()), ImageLockMode.readOnly, bmpPlane.PixelFormat);
//        baPlaneBuffer = new byte[bdPlane.Stride * bmpPlane.getHeight()];
//        System.Runtime.InteropServices.Marshal.Copy(bdPlane.Scan0, baPlaneBuffer, 0, baPlaneBuffer.length);
//        bmpPlane.UnlockBits(bdPlane);
//        bgPlane.Graphics.InterpolationMode = Drawing2D.InterpolationMode.NearestNeighbor;
//        bgPlane.Graphics.DrawImage(initialImage, 0, 0, imageSize.getWidth() * zoom, imageSize.getHeight() * zoom);
    }

    public void Remove(Consumer<Graphics> p) {
        if (bmpPlane != null) {
            bmpPlane.flush();
            bmpPlane = null;
        }
        if (bgPlane != null) {
//            bgPlane.flush();
            bgPlane = null;
        }
        try {
//            if (pbScreen != null)
//                pbScreen.Graphics2D -= new JPaintEventHandler(p);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        pbScreen = null;

        baPlaneBuffer = null;
    }

    private void drawScreen() {
        if (bmpPlane == null) return;

//        BufferedImage bdPlane = bmpPlane.LockBits(new Rectangle(0, 0, bmpPlane.getWidth(), bmpPlane.getHeight()), ImageLockMode.WriteOnly, bmpPlane.PixelFormat);
////            unsafe
//        {
//            byte[] bdP = (byte[])bdPlane.Scan0;
//            int adr;
//            for (int y = 0; y < bdPlane.getHeight(); y++) {
//                adr = bdPlane.Stride * y;
//                for (int x = 0; x < bdPlane.Stride; x++) {
//                    bdP[adr + x] = baPlaneBuffer[bdPlane.Stride * y + x];
//                }
//            }
//        }
//        bmpPlane.UnlockBits(bdPlane);

        bgPlane.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        bgPlane.drawImage(bmpPlane, 0, 0, bmpPlane.getWidth() * zoom, bmpPlane.getHeight() * zoom, null);

        //IntPtr hBmp = bmpPlane.GetHbitmap();
        //IntPtr hFormDC = bgPlane.Graphics.GetHdc(), hDC = CreateCompatibleDC(hFormDC);
        //IntPtr hPrevBmp = SelectObject(hDC, hBmp);
        //BitBlt(hFormDC, 0, 0, bmpPlane.getWidth(), bmpPlane.getHeight(), hDC, 0, 0, SRCCOPY);
        //bgPlane.Graphics.ReleaseHdc(hFormDC);
        //SelectObject(hDC, hPrevBmp);
        //DeleteDC(hDC);
        //DeleteObject(hBmp);
    }

    public void clearScreen() {
        for (int i = 0; i < baPlaneBuffer.length; i += 4) {
            baPlaneBuffer[i] = 0x00; // R
            baPlaneBuffer[i + 1] = 0x00; // G
            baPlaneBuffer[i + 2] = 0x00; // B
            baPlaneBuffer[i + 3] = (byte) 0xFF; // a
        }
        // Arrays.fill(baPlaneBuffer, 0, baPlaneBuffer.length);
    }

    public void Refresh(Graphics g) {
        Runnable act;

//        if (pbScreen == null) return;
//        if (pbScreen.IsDisposed) return;
//
//        try {
//            pbScreen.Invoke(act = () -> {
//                try {
//                    drawScreen();
//                } catch (Exception ex) {
//                    Log.forcedWrite(ex);
//                    Remove(p);
//                }
//                if (bgPlane != null) bgPlane.Render();
//            });
//        } catch (Exception e) {
//             // 握りつぶす
//        }
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
        } catch (Exception ex) {
            ex.printStackTrace();
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

                        if (src[adr2 + j + 0] == 0x00 && src[adr2 + j + 1] == 0xff && src[adr2 + j + 2] == 0x00)
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
