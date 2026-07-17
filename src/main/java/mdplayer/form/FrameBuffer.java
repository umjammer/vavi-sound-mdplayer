package mdplayer.form;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
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

import mdplayer.Common;
import mdplayer.Tables;

import static java.lang.System.getLogger;
import static mdplayer.Common.getImage;


/**
 * The pixel store the skinned screens are drawn into.
 * <p>
 * DrawBuff blits sprites by writing raw bytes into {@link #baPlaneBuffer}. That array is the
 * backing store of {@link #bmpPlane}, so a write is immediately visible to Swing and no copy is
 * needed per frame; {@link #refresh} only has to ask the component to repaint.
 * <p>
 * Pixels are 4 interleaved bytes in {@code R,G,B,A} order — the layout {@link #clearScreen} and
 * {@link #drawByteArrayTransp}'s green colour key already assume.
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
    public void add(JComponent pbScreen, BufferedImage initialImage, Consumer<Graphics> p, int zoom) {
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

    public static byte[][] rChipName;
    public static byte[][] rFont1;
    public static byte[][] rFont2;
    public static byte[][] rFont3;
    private static byte[][] rKBD;
    public static byte[][] rMenuButtons;
    public static byte[][] rPan;
    public static byte[][] rPan2;
    private static byte[] rPSGEnv;
    public static byte[][] rPSGMode;
    public static byte[][] rType;
    private static byte[][] rVol;
    public static byte[] rWavGraph;
    public static byte[] rWavGraph2;
    public static byte[] rFader;
    public static byte[] rFaderH;
    public static byte[][] rMIDILCD_Fader;
    public static byte[] rMIDILCD_KBD;
    public static byte[][] rMIDILCD_Vol;
    public static byte[][] rMIDILCD;
    public static byte[][] rMIDILCD_Font;
    public static byte[][] rPlane_MIDI;
    private static byte[] rNESDMC;
    public static byte[] rKakko;
    public static BufferedImage[] bitmapMIDILyric = null;
    private static Graphics[] gMIDILyric = null;
    private static Font[] fntMIDILyric = null;

    public static void init() {
        rChipName = new byte[3][];

        rChipName[0] = getByteArray(getImage("rChipName_01"));
        rChipName[1] = getByteArray(getImage("rChipName_02"));
        rChipName[2] = getByteArray(getImage("rChipName_03"));

        rFont1 = new byte[2][];
        rFont1[0] = getByteArray(getImage("rFont_01"));
        rFont1[1] = getByteArray(getImage("rFont_02"));
        rFont2 = new byte[5][];
        rFont2[0] = getByteArray(getImage("rFont_03"));
        rFont2[1] = getByteArray(getImage("rFont_04"));
        rFont2[2] = getByteArray(getImage("rMIDILCD_Font_04"));
        rFont2[3] = getByteArray(getImage("rMIDILCD_Font_05"));
        rFont2[4] = getByteArray(getImage("rMIDILCD_Font_06"));
        rFont3 = new byte[2][];
        rFont3[0] = getByteArray(getImage("rFont_05"));
        rFont3[1] = getByteArray(getImage("rFont_06"));

        rKBD = new byte[3][];
        rKBD[0] = getByteArray(getImage("rKBD_01"));
        rKBD[1] = getByteArray(getImage("rKBD_02"));
        rKBD[2] = getByteArray(getImage("rKBD_03"));

        rMenuButtons = new byte[2][];
        rMenuButtons[0] = getByteArray(getImage("rMenuButtons_01"));
        rMenuButtons[1] = getByteArray(getImage("rMenuButtons_02"));

        rPan = new byte[3][];
        rPan[0] = getByteArray(getImage("rPan_01"));
        rPan[1] = getByteArray(getImage("rPan_02"));
        rPan[2] = getByteArray(getImage("rPan_03"));

        rPan2 = new byte[2][];
        rPan2[0] = getByteArray(getImage("rPan2_01"));
        rPan2[1] = getByteArray(getImage("rPan2_02"));

        rPSGEnv = getByteArray(getImage("rPSGEnv"));

        rPSGMode = new byte[6][];
        rPSGMode[0] = getByteArray(getImage("rPSGMode_01"));
        rPSGMode[1] = getByteArray(getImage("rPSGMode_02"));
        rPSGMode[2] = getByteArray(getImage("rPSGMode_03"));
        rPSGMode[3] = getByteArray(getImage("rPSGMode_04"));
        rPSGMode[4] = getByteArray(getImage("rPSGMode_05"));
        rPSGMode[5] = getByteArray(getImage("rPSGMode_06"));

        rType = new byte[6][];
        rType[0] = getByteArray(getImage("rType_01"));
        rType[1] = getByteArray(getImage("rType_02"));
        rType[2] = getByteArray(getImage("rType_03"));
        rType[3] = getByteArray(getImage("rType_04"));
        rType[4] = getByteArray(getImage("rType_05"));
        rType[5] = getByteArray(getImage("rType_06"));

        rVol = new byte[3][];
        rVol[0] = getByteArray(getImage("rVol_01"));
        rVol[1] = getByteArray(getImage("rVol_02"));
        rVol[2] = getByteArray(getImage("rVol_03"));

        rWavGraph = getByteArray(getImage("rWavGraph"));
        rWavGraph2 = getByteArray(getImage("rWavGraph2"));
        rFader = getByteArray(getImage("rFader"));
        rFaderH = getByteArray(getImage("rFaderH"));
        rNESDMC = getByteArray(getImage("rNESDMC"));
        rKakko = getByteArray(getImage("rKakko_00"));

        rMIDILCD_Fader = new byte[3][];
        rMIDILCD_Fader[0] = getByteArray(getImage("rMIDILCD_Fader_01"));
        rMIDILCD_Fader[1] = getByteArray(getImage("rMIDILCD_Fader_02"));
        rMIDILCD_Fader[2] = getByteArray(getImage("rMIDILCD_Fader_03"));
        rMIDILCD_KBD = getByteArray(getImage("rMIDILCD_KBD_01"));

        rMIDILCD_Vol = new byte[3][];
        rMIDILCD_Vol[0] = getByteArray(getImage("rMIDILCD_Vol_01"));
        rMIDILCD_Vol[1] = getByteArray(getImage("rMIDILCD_Vol_02"));
        rMIDILCD_Vol[2] = getByteArray(getImage("rMIDILCD_Vol_03"));

        rMIDILCD = new byte[3][];
        rMIDILCD[0] = getByteArray(getImage("rMIDILCD_01"));
        rMIDILCD[1] = getByteArray(getImage("rMIDILCD_02"));
        rMIDILCD[2] = getByteArray(getImage("rMIDILCD_03"));

        rMIDILCD_Font = new byte[3][];
        rMIDILCD_Font[0] = getByteArray(getImage("rMIDILCD_Font_01"));
        rMIDILCD_Font[1] = getByteArray(getImage("rMIDILCD_Font_02"));
        rMIDILCD_Font[2] = getByteArray(getImage("rMIDILCD_Font_03"));

        rPlane_MIDI = new byte[3][];

        rPlane_MIDI[0] = getByteArray(getImage("planeMIDI_GM"));
        rPlane_MIDI[1] = getByteArray(getImage("planeMIDI_XG"));

        rPlane_MIDI[2] = getByteArray(getImage("planeMIDI_GS"));

        bitmapMIDILyric = new BufferedImage[2];
        bitmapMIDILyric[0] = new BufferedImage(200, 24, BufferedImage.TYPE_INT_ARGB);
        bitmapMIDILyric[1] = new BufferedImage(200, 24, BufferedImage.TYPE_INT_ARGB);
        gMIDILyric = new Graphics[2];
        gMIDILyric[0] = bitmapMIDILyric[0].getGraphics();
        gMIDILyric[1] = bitmapMIDILyric[1].getGraphics();
        fntMIDILyric = new Font[2];
        fntMIDILyric[0] = new Font("MS UI Gothic", Font.PLAIN, 8);// , Font.BOLD);
        fntMIDILyric[1] = new Font("MS UI Gothic", Font.PLAIN, 8);// , Font.BOLD);
    }

    public void drawInst(int x, int y, int c, int[] oi, int[] ni) {
        int sx = (c % 3) * 8 * 13 + x * 8;
        int sy = (c / 3) * 8 * 6 + 8 * y;

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 11; i++) {
                if (oi[i + j * 11] != ni[i + j * 11]) {
                    drawFont4Int(sx + i * 8 + (i > 5 ? 4 : 0), sy + j * 8, 0, (i == 5) ? 3 : 2, ni[i + j * 11]);
                    oi[i + j * 11] = ni[i + j * 11];
                }
            }
        }

        if (oi[44] != ni[44]) {
            drawFont4Int(sx + 8 * 4, sy - 16, 0, 2, ni[44]);
            oi[44] = ni[44];
        }
        if (oi[45] != ni[45]) {
            drawFont4Int(sx + 8 * 6, sy - 16, 0, 2, ni[45]);
            oi[45] = ni[45];
        }
        if (oi[46] != ni[46]) {
            drawFont4Int(sx + 8 * 8 + 4, sy - 16, 0, 2, ni[46]);
            oi[46] = ni[46];
        }
        if (oi[47] != ni[47]) {
            drawFont4Int(sx + 8 * 11, sy - 16, 0, 2, ni[47]);
            oi[47] = ni[47];
        }
    }

    public void drawInst(int x, int y, int c, int[] oi, int[] ni, int[] ot, int[] nt) {
        int sx = (c % 3) * 8 * 13 + x * 8;
        int sy = (c / 3) * 8 * 6 + 8 * y;

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 11; i++) {
                if (oi[i + j * 11] != ni[i + j * 11] || ot[i + j * 11] != nt[i + j * 11]) {
                    drawFont4Int(sx + i * 8 + (i > 5 ? 4 : 0),
                            sy + j * 8,
                            nt[i + j * 11],
                            (i == 5) ? 3 : 2,
                            ni[i + j * 11]);
                    oi[i + j * 11] = ni[i + j * 11];
                    ot[i + j * 11] = nt[i + j * 11];
                }
            }
        }

        if (oi[44] != ni[44] || ot[44] != nt[44]) {
            drawFont4Int(sx + 8 * 4, sy - 16, nt[44], 2, ni[44]);
            oi[44] = ni[44];
            ot[44] = nt[44];
        }
        if (oi[45] != ni[45] || ot[45] != nt[45]) {
            drawFont4Int(sx + 8 * 6, sy - 16, nt[45], 2, ni[45]);
            oi[45] = ni[45];
            ot[45] = nt[45];
        }
        if (oi[46] != ni[46] || ot[46] != nt[46]) {
            drawFont4Int(sx + 8 * 8 + 4, sy - 16, nt[46], 2, ni[46]);
            oi[46] = ni[46];
            ot[46] = nt[46];
        }
        if (oi[47] != ni[47] || ot[47] != nt[47]) {
            drawFont4Int(sx + 8 * 11, sy - 16, nt[47], 2, ni[47]);
            oi[47] = ni[47];
            ot[47] = nt[47];
        }
    }

    public int drawSlot(int x, int y, /* ref */ int os, int ns) {
        if (os == ns)
            return os;

        drawByteArray(x + 0, y, rNESDMC, 64, ((ns & 1) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);
        drawByteArray(x + 4, y, rNESDMC, 64, ((ns & 2) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);
        drawByteArray(x + 8, y, rNESDMC, 64, ((ns & 4) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);
        drawByteArray(x + 12, y, rNESDMC, 64, ((ns & 8) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);

        os = ns;
        return os;
    }

    /**
     * Volume meter drawing
     *
     * @param x      x-coordinate (x1)
     * @param y      y-coordinate (x1)
     * @param c      0:Mono 1:Stereo(L) 2:Stereo(R)
     * @param ov     Previous value
     * @param nv     This value
     * @param tp     0:EMU 1:Real
     */
    public int drawVolumeM(int x, int y, int c, int ov, int nv, int tp) {
        if (ov == nv)
            return ov;

//        int t = 0;
//        int sy = 0;
//        if (c == 1 || c == 2) { t = 4; }
//        if (c == 2) { sy = 4; }
//        y = (y + 1) * 8;

//        for (int i = 0; i <= 19; i++) {
//            VolumeP(256 + i * 2, y + sy, (1 + t), tp);
//        }

//        for (int i = 0; i <= nv; i++) {
//            VolumeP(256 + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
//        }

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }
        // y = (y + 1) * 8;

        for (int i = 0; i <= 19; i++) {
            drawVolumeP(x + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            drawVolumeP(x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
        return ov;
    }

    public int drawVolumeXY(int x, int y, int c, int ov, int nv, int tp) {
        if (ov == nv)
            return ov;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }

        y *= 4;
        x *= 4;

        for (int i = 0; i <= 19; i++) {
            drawVolumeP(x + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            drawVolumeP(x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
        return ov;
    }

    public int drawVolume(int x, int y, int c, int ov, int nv, int tp) {
        if (ov == nv)
            return ov;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }

        for (int i = 0; i <= 19; i++) {
            drawVolumeP(x + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            drawVolumeP(x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
        return ov;
    }

    public int drawKeyBoard(int y, int ot, int nt, int tp) {
        if (ot == nt)
            return ot;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(32 + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(32 + kx, y, kt, tp);
        }

        drawFont8(296, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(296, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                drawFont8(312, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
        return ot;
    }

    public int[] Pan(int x, int y, int ot, int nt, int otp, int ntp) {
        if (ot == nt && otp == ntp) {
            return new int[] {ot, otp};
        }

        drawPanP(x, y, nt, ntp);
        ot = nt;
        otp = ntp;
        return new int[] {ot, otp};
    }

    public int PanType2(int c, int ot, int nt, int tp) {
        if (ot == nt) {
            return ot;
        }

        drawPanType2P(24, 8 + c * 8, nt, tp);
        ot = nt;
        return ot;
    }

    public int PanType2(int x, int y, int ot, int nt, int tp) {
        if (ot == nt) {
            return ot;
        }

        drawPanType2P(x, y, nt, tp);
        ot = nt;
        return ot;
    }

    public int Nfrq(int x, int y, int onfrq, int nnfrq) {
        if (onfrq == nnfrq) {
            return onfrq;
        }

        x *= 4;
        y *= 4;
        drawFont4Int(x, y, 0, 2, nnfrq);

        onfrq = nnfrq;
        return onfrq;
    }

    public int drawEfrq(int x, int y, int oefrq, int nefrq) {
        if (oefrq == nefrq) {
            return oefrq;
        }

        x *= 4;
        y *= 4;
        drawFont4(x, y, 0, "%5d".formatted(nefrq));

        oefrq = nefrq;
        return oefrq;
    }

    public int drawEType(int x, int y, int oetype, int netype) {
        if (oetype == netype) {
            return oetype;
        }

        x *= 4;
        y *= 4;

        drawEtypeP(x, y, netype);
        oetype = netype;
        return oetype;
    }

    public int flag16Bit(int x, int y, int t, int oi, int ni) {
        if (oi != ni) {
            drawFont4(x + 0, y, t, (ni & 0x8000) == 0 ? "-" : "*");
            drawFont4(x + 4, y, t, (ni & 0x4000) == 0 ? "-" : "*");
            drawFont4(x + 8, y, t, (ni & 0x2000) == 0 ? "-" : "*");
            drawFont4(x + 12, y, t, (ni & 0x1000) == 0 ? "-" : "*");
            drawFont4(x + 16, y, t, (ni & 0x0800) == 0 ? "-" : "*");
            drawFont4(x + 20, y, t, (ni & 0x0400) == 0 ? "-" : "*");
            drawFont4(x + 24, y, t, (ni & 0x0200) == 0 ? "-" : "*");
            drawFont4(x + 28, y, t, (ni & 0x0100) == 0 ? "-" : "*");
            drawFont4(x + 32, y, t, (ni & 0x0080) == 0 ? "-" : "*");
            drawFont4(x + 36, y, t, (ni & 0x0040) == 0 ? "-" : "*");
            drawFont4(x + 40, y, t, (ni & 0x0020) == 0 ? "-" : "*");
            drawFont4(x + 44, y, t, (ni & 0x0010) == 0 ? "-" : "*");
            drawFont4(x + 48, y, t, (ni & 0x0008) == 0 ? "-" : "*");
            drawFont4(x + 52, y, t, (ni & 0x0004) == 0 ? "-" : "*");
            drawFont4(x + 56, y, t, (ni & 0x0002) == 0 ? "-" : "*");
            drawFont4(x + 60, y, t, (ni & 0x0001) == 0 ? "-" : "*");
            oi = ni;
        }
        return oi;
    }

    public int drawSusFlag(int x, int y, int t, int oi, int ni) {
        if (oi != ni) {
            drawFont4(x * 4, y * 4, t, ni == 0 ? "-" : "*");
            oi = ni;
        }
        return oi;
    }

    public boolean drawLfoSw(int x, int y, boolean olfosw, boolean nlfosw) {
        if (olfosw == nlfosw) {
            return olfosw;
        }

        drawFont4(x, y, 0, nlfosw ? "ON " : "OFF");

        olfosw = nlfosw;
        return olfosw;
    }

    public int drawLfoFrq(int x, int y, int olfofrq, int nlfofrq) {
        if (olfofrq == nlfofrq) {
            return olfofrq;
        }

        drawFont4Int(x, y, 0, 1, nlfofrq);

        olfofrq = nlfofrq;
        return olfofrq;
    }

    public byte drawChipName(int x, int y, int t, byte oc, byte nc) {
        if (oc == nc)
            return oc;

        drawChipNameP(x, y, t, nc);

        oc = nc;
        return oc;
    }

    public void drawButtonP(int x, int y, int t, int m) {
        if (bmpPlane == null)
            return;

        int n = t % 18;
        t /= 18;
        switch (n) {
            case 0:
                // setting
                drawByteArray(x, y, rMenuButtons[t], 128, 5 * 16, 1 * 16, 16, 16);
                break;
            case 1:
                // stop
                drawByteArray(x, y, rMenuButtons[t], 128, 0 * 16, 0 * 16, 16, 16);
                break;
            case 2:
                // pause
                drawByteArray(x, y, rMenuButtons[t], 128, 1 * 16, 0 * 16, 16, 16);
                break;
            case 3:
                // fadeout
                drawByteArray(x, y, rMenuButtons[t], 128, 4 * 16, 1 * 16, 16, 16);
                break;
            case 4:
                // PREV
                drawByteArray(x, y, rMenuButtons[t], 128, 6 * 16, 1 * 16, 16, 16);
                break;
            case 5:
                // slow
                drawByteArray(x, y, rMenuButtons[t], 128, 2 * 16, 0 * 16, 16, 16);
                break;
            case 6:
                // play
                drawByteArray(x, y, rMenuButtons[t], 128, 3 * 16, 0 * 16, 16, 16);
                break;
            case 7:
                // fast
                drawByteArray(x, y, rMenuButtons[t], 128, 4 * 16, 0 * 16, 16, 16);
                break;
            case 8:
                // NEXT
                drawByteArray(x, y, rMenuButtons[t], 128, 7 * 16, 1 * 16, 16, 16);
                break;
            case 9:
                // loopmode
                drawByteArray(x, y, rMenuButtons[t], 128, 1 * 16 + m * 16, 2 * 16, 16, 16);
                break;
            case 10:
                // folder
                drawByteArray(x, y, rMenuButtons[t], 128, 5 * 16, 0 * 16, 16, 16);
                break;
            case 11:
                // List
                drawByteArray(x, y, rMenuButtons[t], 128, 0 * 16, 2 * 16, 16, 16);
                break;
            case 12:
                // info
                drawByteArray(x, y, rMenuButtons[t], 128, 0 * 16, 1 * 16, 16, 16);
                break;
            case 13:
                // mixer
                drawByteArray(x, y, rMenuButtons[t], 128, 2 * 16, 1 * 16, 16, 16);
                break;
            case 14:
                // panel
                drawByteArray(x, y, rMenuButtons[t], 128, 5 * 16, 2 * 16, 16, 16);
                break;
            case 15:
                // VST List
                drawByteArray(x, y, rMenuButtons[t], 128, 7 * 16, 0 * 16, 16, 16);
                break;
            case 16:
                // MIDI Keyboard
                drawByteArray(x, y, rMenuButtons[t], 128, 3 * 16, 1 * 16, 16, 16);
                break;
            case 17:
                // zoom
                drawByteArray(x, y, rMenuButtons[t], 128, 6 * 16, 2 * 16, 16, 16);
                break;
        }
    }

    public int[] drawButton(int c, int ot, int nt, int om, int nm) {
        if (ot == nt && om == nm) {
            return new int[] {ot, om};
        }

        drawFont8(17 + c * 16, 9, 0, "  ");
        drawFont8(17 + c * 16, 17, 0, "  ");
        drawButtonP(17 + c * 16, 9, nt * 18 + c, nm);

        ot = nt;
        om = nm;
        return new int[] {ot, om};
    }

    public void drawButtons(int[] oldButton,
                            int[] newButton,
                            int[] oldButtonMode,
                            int[] newButtonMode) {

        for (int i = 0; i < newButton.length; i++) {
            int[] r = drawButton(i, oldButton[i], newButton[i], oldButtonMode[i], newButtonMode[i]);
            oldButton[i] = r[0];
            oldButtonMode[i] = r[1];
        }
    }

    public int drawDuty(int x, int y, int op, int np) {
        if (op == np)
            return op;

        drawByteArray(x, y, rNESDMC, 64, np * 8, 0, 8, 8);

        op = np;
        return op;
    }

    public boolean drawNESSw(int x, int y, boolean os, boolean ns) {
        if (os == ns)
            return os;

        drawByteArray(x, y, rNESDMC, 64, (ns ? 1 : 0) * 4 + 32, 0, 4, 8);

        os = ns;
        return os;
    }

    public int font4Int1(int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Int1(x, y, t, nn);
        on = nn;
        return on;
    }

    public int font4Int2(int x, int y, int t, int k, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Int2(x, y, t, k, nn);
        on = nn;
        return on;
    }

    public int font4Int3(int x, int y, int t, int k, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Int3(x, y, t, k, nn);
        on = nn;
        return on;
    }

    public int font4Hex4Bit(int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Hex4Bit(x, y, t, nn);
        on = nn;
        return on;
    }

    public int font4HexByte(int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4HexByte(x, y, t, nn);
        on = nn;
        return on;
    }

    public int font4Hex12Bit(int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Hex12Bit(x, y, t, nn);
        on = nn;
        return on;
    }

    public int font4Hex16Bit(int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Hex16Bit(x, y, t, nn);
        on = nn;
        return on;
    }

    public int font4Hex20Bit(int x, int y, int t, /* ref */ int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Hex20Bit(x, y, t, nn);
        on = nn;
        return on;
    }

    public int font4Hex24Bit(int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Hex24Bit(x, y, t, nn);
        on = nn;
        return on;
    }

    /**
     * Flattens a sprite sheet into the {@code R,G,B,A} byte layout {@link FrameBuffer} blits,
     * with a stride of {@code width * 4}.
     */
    public static byte[] getByteArray(Image img) {
        BufferedImage bitmap = toBufferedImage(img);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] argb = bitmap.getRGB(0, 0, w, h, null, 0, w);
        byte[] byteArray = new byte[w * h * 4];
        for (int i = 0; i < argb.length; i++) {
            int p = argb[i];
            int adr = i * 4;
            byteArray[adr] = (byte) (p >> 16); // R
            byteArray[adr + 1] = (byte) (p >> 8); // G
            byteArray[adr + 2] = (byte) p; // B
            byteArray[adr + 3] = (byte) (p >>> 24); // A
        }
        return byteArray;
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }

        BufferedImage bufferedImage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        try {
            g.drawImage(img, 0, 0, null);
        } finally {
            g.dispose();
        }
        return bufferedImage;
    }

    public void drawVolumeP(int x, int y, int t, int tp) {
        if (bmpPlane == null)
            return;
        drawByteArray(x, y, rVol[tp], 32, 2 * t, 0, 2, 8 - (t / 4) * 4);
    }

    public void drawKbn(int x, int y, int t, int tp) {
        if (bmpPlane == null) {
            return;
        }

        switch (t) {
            case 0:
                drawByteArray(x, y, rKBD[tp], 32, 0, 0, 4, 8);
                break;
            case 1:
                drawByteArray(x, y, rKBD[tp], 32, 4, 0, 3, 8);
                break;
            case 2:
                drawByteArray(x, y, rKBD[tp], 32, 8, 0, 4, 8);
                break;
            case 3:
                drawByteArray(x, y, rKBD[tp], 32, 12, 0, 4, 8);
                break;
            case 4:
                drawByteArray(x, y, rKBD[tp], 32, 0 + 16, 0, 4, 8);
                break;
            case 5:
                drawByteArray(x, y, rKBD[tp], 32, 4 + 16, 0, 3, 8);
                break;
            case 6:
                drawByteArray(x, y, rKBD[tp], 32, 8 + 16, 0, 4, 8);
                break;
            case 7:
                drawByteArray(x, y, rKBD[tp], 32, 12 + 16, 0, 4, 8);
                break;
        }
    }

    public void drawFont8(int x, int y, int t, String msg) {
        if (bmpPlane == null) {
            return;
        }

        for (char c : msg.toCharArray()) {
            int cd = c - 'A' + 0x20 + 1;
            drawByteArray(x, y, rFont1[t], 128, (cd % 16) * 8, (cd / 16) * 8, 8, 8);
            x += 8;
        }
    }

    public void drawFont8Int(int x, int y, int t, int k, int num) {
        if (bmpPlane == null)
            return;

        int n;
        if (k == 3) {
            boolean f = false;
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            if (n != 0) {
                drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
            }

            n = num / 10;
            num -= n * 10;
            x += 8;
            if (n != 0 || f) {
                drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
            }

            n = num / 1;
            num -= n * 1;
            x += 8;
            drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        if (n != 0) {
            drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
        } else {
            drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
        }

        n = num / 1;
        num -= n * 1;
        x += 8;
        drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
    }

    public void drawFont4(int x, int y, int t, String msg) {
        if (bmpPlane == null)
            return;

        for (char c : msg.toCharArray()) {
            int cd = c - 'A' + 0x20 + 1;
            drawByteArray(x, y, rFont2[t], 128, (cd % 32) * 4, (cd / 32) * 8, 4, 8);
            x += 4;
        }
    }

    public void drawFont4Int(int x, int y, int t, int k, int num) {
        if (bmpPlane == null)
            return;

        int n;
        if (k == 3) {
            boolean f = false;
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            if (n != 0) {
                drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                drawByteArray(x, y, rFont2[t], 128, 0, 0, 4, 8);
            }

            n = num / 10;
            num -= n * 10;
            x += 4;
            if (n != 0 || f) {
                drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                drawByteArray(x, y, rFont2[t], 128, 0, 0, 4, 8);
            }

            n = num / 1;
            x += 4;
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        if (n != 0) {
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
        } else {
            drawByteArray(x, y, rFont2[t], 128, 0, 0, 4, 8);
        }

        n = num / 1;
        x += 4;
        drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public void drawFont4Int1(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        n = num % 10;
        drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public void drawFont4Int2(int x, int y, int t, int k, int num) {
        if (bmpPlane == null)
            return;

        int n;
        if (k == 3) {
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            drawByteArray(x, y, rFont2[t], 128, (n * 4 + 64), 0, 4, 8);

            n = num / 10;
            num -= n * 10;
            x += 4;
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

            n = num / 1;
            x += 4;
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 1;
        x += 4;
        drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public void drawFont4Int3(int x, int y, int t, int k, int num) {
        if (bmpPlane == null)
            return;

        int n;
        if (k == 3) {
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

            n = num / 10;
            num -= n * 10;
            x += 4;
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

            n = num / 1;
            x += 4;
            drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 1;
        x += 4;
        drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public void drawFont4Hex4Bit(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        num = Common.range((byte) num, 0, 15);

        n = num;
        drawFont4(x, y, t, Tables.hexCh[n]);
    }

    public void drawFont4HexByte(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        num = Common.range((byte) num, 0, 255);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);
    }

    public void drawFont4Hex12Bit(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        num = Common.range(num, 0, 0xfff);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);
    }

    public void drawFont4Hex16Bit(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        num = Common.range(num, 0, 0xffff);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);
    }

    public void drawFont4Hex20Bit(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        num = Common.range(num, 0, 0xf_ffff);

        n = num / 0x1_0000;
        num -= n * 0x1_0000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);
    }

    public void drawFont4Hex24Bit(int x, int y, int t, int num) {
        if (bmpPlane == null)
            return;

        int n;
        num = Common.range(num, 0, 0xff_ffff);

        n = num / 0x10_0000;
        num -= n * 0x10_0000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x1_0000;
        num -= n * 0x1_0000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(x, y, t, Tables.hexCh[n]);
    }

    private void drawEtypeP(int x, int y, int t) {
        if (bmpPlane == null)
            return;
        drawByteArray(x, y, rPSGEnv, 128, 8 * t, 0, 8, 8);
        drawFont4Int2(x + 12, y, 0, 2, t);
    }

    public void drawPanP(int x, int y, int t, int tp) {
        if (bmpPlane == null)
            return;
        drawByteArray(x, y, rPan[tp], 32, 8 * t, 0, 8, 8);
    }

    public void drawPanType2P(int x, int y, int t, int tp) {
        if (bmpPlane == null) {
            return;
        }

        int p = (t & 0x0f);
        p = p == 0 ? 0 : (1 + p / 4);
        drawByteArray(x, y, rPan2[tp], 32, p * 4, 0, 4, 8);
        p = ((t & 0xf0) >> 4);
        p = p == 0 ? 0 : (1 + p / 4);
        drawByteArray(x + 4, y, rPan2[tp], 32, p * 4, 0, 4, 8);
    }

    public void drawTnP(int x, int y, int t, int tp) {
        if (bmpPlane == null)
            return;
        drawByteArray(x, y, rPSGMode[tp], 32, 8 * t, 0, 8, 8);
    }

    private void drawChipNameP(int x, int y, int t, int c) {
        if (bmpPlane == null) {
            return;
        }

        drawByteArray(x, y, rChipName[c], 128, (t % 8) * 16, (t / 8) * 8, 8 * 2, 8);
    }
}
