
package mdplayer;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import mdplayer.properties.Resources;


public class DrawBuff {

    private static byte[][] rChipName;
    private static byte[][] rFont1;
    private static byte[][] rFont2;
    private static byte[][] rFont3;
    private static byte[][] rKBD;
    private static byte[][] rMenuButtons;
    private static byte[][] rPan;
    private static byte[][] rPan2;
    private static byte[] rPSGEnv;
    private static byte[][] rPSGMode;
    private static byte[][] rType;
    private static byte[][] rVol;
    private static byte[] rWavGraph;
    private static byte[] rWavGraph2;
    private static byte[] rFader;
    private static byte[][] rMIDILCD_Fader;
    private static byte[] rMIDILCD_KBD;
    private static byte[][] rMIDILCD_Vol;
    public static byte[][] rMIDILCD;
    private static byte[][] rMIDILCD_Font;
    public static byte[][] rPlane_MIDI;
    private static byte[] rNESDMC;
    private static byte[] rKakko;
    private static byte[] rType_YMF271;
    private static BufferedImage[] bitmapMIDILyric = null;
    private static Graphics[] gMIDILyric = null;
    private static Font[] fntMIDILyric = null;

    public static void Init() {
        rChipName = new byte[3][];
        rChipName[0] = getByteArray(Resources.getRChipName_01());
        rChipName[1] = getByteArray(Resources.getRChipName_02());
        rChipName[2] = getByteArray(Resources.getRChipName_03());

        rFont1 = new byte[2][];
        rFont1[0] = getByteArray(Resources.getRFont_01());
        rFont1[1] = getByteArray(Resources.getRFont_02());
        rFont2 = new byte[5][];
        rFont2[0] = getByteArray(Resources.getRFont_03());
        rFont2[1] = getByteArray(Resources.getRFont_04());
        rFont2[2] = getByteArray(Resources.getRMIDILCD_Font_04());
        rFont2[3] = getByteArray(Resources.getRMIDILCD_Font_05());
        rFont2[4] = getByteArray(Resources.getRMIDILCD_Font_06());
        rFont3 = new byte[2][];
        rFont3[0] = getByteArray(Resources.getRFont_05());
        rFont3[1] = getByteArray(Resources.getRFont_06());

        rKBD = new byte[3][];
        rKBD[0] = getByteArray(Resources.getRKBD_01());
        rKBD[1] = getByteArray(Resources.getRKBD_02());
        rKBD[2] = getByteArray(Resources.getRKBD_03());

        rMenuButtons = new byte[2][];
        rMenuButtons[0] = getByteArray(Resources.getRMenuButtons_01());
        rMenuButtons[1] = getByteArray(Resources.getRMenuButtons_02());

        rPan = new byte[3][];
        rPan[0] = getByteArray(Resources.getRPan_01());
        rPan[1] = getByteArray(Resources.getRPan_02());
        rPan[2] = getByteArray(Resources.getRPan_03());

        rPan2 = new byte[2][];
        rPan2[0] = getByteArray(Resources.getRPan2_01());
        rPan2[1] = getByteArray(Resources.getRPan2_02());

        rPSGEnv = getByteArray(Resources.getRPSGEnv());

        rPSGMode = new byte[6][];
        rPSGMode[0] = getByteArray(Resources.getRPSGMode_01());
        rPSGMode[1] = getByteArray(Resources.getRPSGMode_02());
        rPSGMode[2] = getByteArray(Resources.getRPSGMode_03());
        rPSGMode[3] = getByteArray(Resources.getRPSGMode_04());
        rPSGMode[4] = getByteArray(Resources.getRPSGMode_05());
        rPSGMode[5] = getByteArray(Resources.getRPSGMode_06());

        rType = new byte[6][];
        rType[0] = getByteArray(Resources.getRType_01());
        rType[1] = getByteArray(Resources.getRType_02());
        rType[2] = getByteArray(Resources.getRType_03());
        rType[3] = getByteArray(Resources.getRType_04());
        rType[4] = getByteArray(Resources.getRType_05());
        rType[5] = getByteArray(Resources.getRType_06());

        rVol = new byte[3][];
        rVol[0] = getByteArray(Resources.getRVol_01());
        rVol[1] = getByteArray(Resources.getRVol_02());
        rVol[2] = getByteArray(Resources.getRVol_03());

        rWavGraph = getByteArray(Resources.getRWavGraph());
        rWavGraph2 = getByteArray(Resources.getRWavGraph2());
        rFader = getByteArray(Resources.getRFader());
        rNESDMC = getByteArray(Resources.getRNESDMC());

        rKakko = getByteArray(Resources.getRKakko_00());

        rMIDILCD_Fader = new byte[3][];
        rMIDILCD_Fader[0] = getByteArray(Resources.getRMIDILCD_Fader_01());
        rMIDILCD_Fader[1] = getByteArray(Resources.getRMIDILCD_Fader_02());
        rMIDILCD_Fader[2] = getByteArray(Resources.getRMIDILCD_Fader_03());

        rMIDILCD_KBD = getByteArray(Resources.getRMIDILCD_KBD_01());

        rMIDILCD_Vol = new byte[3][];
        rMIDILCD_Vol[0] = getByteArray(Resources.getRMIDILCD_Vol_01());
        rMIDILCD_Vol[1] = getByteArray(Resources.getRMIDILCD_Vol_02());
        rMIDILCD_Vol[2] = getByteArray(Resources.getRMIDILCD_Vol_03());

        rMIDILCD = new byte[3][];
        rMIDILCD[0] = getByteArray(Resources.getRMIDILCD_01());
        rMIDILCD[1] = getByteArray(Resources.getRMIDILCD_02());
        rMIDILCD[2] = getByteArray(Resources.getRMIDILCD_03());

        rMIDILCD_Font = new byte[3][];
        rMIDILCD_Font[0] = getByteArray(Resources.getRMIDILCD_Font_01());
        rMIDILCD_Font[1] = getByteArray(Resources.getRMIDILCD_Font_02());
        rMIDILCD_Font[2] = getByteArray(Resources.getRMIDILCD_Font_03());

        rPlane_MIDI = new byte[3][];
        rPlane_MIDI[0] = getByteArray(Resources.getPlaneMIDI_GM());
        rPlane_MIDI[1] = getByteArray(Resources.getPlaneMIDI_XG());
        rPlane_MIDI[2] = getByteArray(Resources.getPlaneMIDI_GS());

        bitmapMIDILyric = new BufferedImage[2];
        bitmapMIDILyric[0] = new BufferedImage(200, 24, BufferedImage.TYPE_INT_ARGB);
        bitmapMIDILyric[1] = new BufferedImage(200, 24, BufferedImage.TYPE_INT_ARGB);
        gMIDILyric = new Graphics[2];
        gMIDILyric[0] = bitmapMIDILyric[0].getGraphics();
        gMIDILyric[1] = bitmapMIDILyric[1].getGraphics();
        fntMIDILyric = new Font[2];
        fntMIDILyric[0] = new Font("MS UI Gothic", Font.PLAIN, 8);// , Font.BOLD);
        fntMIDILyric[1] = new Font("MS UI Gothic", Font.PLAIN, 8);// , Font.BOLD);

        rType_YMF271 = getByteArray(Resources.getRType_YMF271());
    }

    public static void screenInitHuC6280(FrameBuffer screen) {
        for (int ch = 0; ch < 6; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, ch * 8 + 8, kt, 0);
            }
            drawFont8(screen, 296, ch * 8 + 8, 1, "   ");
        }
    }

    public static void screenInitK051649(FrameBuffer screen) {
        for (int ch = 0; ch < 5; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, ch * 8 + 8, kt, 0);
            }
            drawFont8(screen, 296, ch * 8 + 8, 1, "   ");
        }
    }

    public static void screenInitRF5C164(FrameBuffer screen) {
        // RF5C164
        for (int ch = 0; ch < 8; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, ch * 8 + 8, kt, 0);
            }
            drawFont8(screen, 296, ch * 8 + 8, 1, "   ");
            drawPanType2P(screen, 24, ch * 8 + 8, 0, 0);
        }
    }

    public static void screenInitRF5C68(FrameBuffer screen) {
        // RF5C164
        for (int ch = 0; ch < 8; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, ch * 8 + 8, kt, 0);
            }
            drawFont8(screen, 296, ch * 8 + 8, 1, "   ");
            drawPanType2P(screen, 24, ch * 8 + 8, 0, 0);
        }
    }

    public static void screenInitMIDI(FrameBuffer screen) {
    }

    public static void screenInitMixer(FrameBuffer screen) {
    }

    public static void screenInitOKIM6258(FrameBuffer screen) {
        int o;
        int n;

        o = 0;
        n = 3;
        PanToOKIM6258(screen, o, n, o, 0);

        drawFont4(screen, 12 * 4, 8, 0, String.format("%5d", 0));
        drawFont4(screen, 19 * 4, 8, 0, String.format("%5d", 0));
        drawFont4(screen, 26 * 4, 8, 0, String.format("%5d", 0));

        o = 0;
        n = 38;
        o = volume(screen, 256, 8 + 0 * 8, 1, o, n / 2, 0);
        o = 0;
        n = 38;
        o = volume(screen, 256, 8 + 0 * 8, 2, o, n / 2, 0);
    }

    public static void screenInitOKIM6295(FrameBuffer screen) {
    }

    public static void screenInitSN76489(FrameBuffer screen, int tp) {

        for (int ch = 0; ch < 4; ch++) {
            if (ch != 3) {
                for (int ot = 0; ot < 12 * 8; ot++) {
                    int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                    int kt = Tables.kbl[(ot % 12) * 2 + 1];
                    drawKbn(screen, 32 + kx, ch * 8 + 8, kt, tp);
                }
            } else {
            }

            DrawBuff.drawFont8(screen, 296, ch * 8 + 8, 1, "   ");
            DrawBuff.ChSN76489_P(screen, 0, ch * 8 + 8, ch, false, tp);

            int d = 99;
            d = DrawBuff.volume(screen, 256, 8 + ch * 8, 0, d, 0, tp);
        }
    }

    public static void screenInitYM2203(FrameBuffer screen, int tp) {
        if (screen == null)
            return;

        // YM2203
        for (int y = 0; y < 3 + 3 + 3; y++) {

            drawFont8(screen, 296, y * 8 + 8, 1, "   ");
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 0, tp);

            if (y < 9) {
                ChYM2203_P(screen, 0, y * 8 + 8, y, false, tp);
            }
        }

    }

    public static void screenInitYM2608(FrameBuffer screen, int tp) {
        // YM2608
        for (int y = 0; y < 6 + 3 + 3 + 1; y++) {

            drawFont8(screen, 296, y * 8 + 8, 1, "   ");
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 33 + kx, y * 8 + 8, kt, tp);
            }

            if (y < 13) {
                ChYM2608_P(screen, 1, y * 8 + 8, y, false, tp);
            }

            if (y < 6 || y == 12) {
                drawPanP(screen, 25, y * 8 + 8, 3, tp);
            }

            int d = 99;
            if (y > 5 && y < 9) {
                d = volume(screen, 289, 8 + y * 8, 0, d, 0, tp);
            } else {
                d = volume(screen, 289, 8 + y * 8, 1, d, 0, tp);
                d = 99;
                d = volume(screen, 289, 8 + y * 8, 2, d, 0, tp);
            }
        }

        for (int y = 0; y < 6; y++) {
            int d = 99;
            PanYM2608Rhythm(screen, y, d, 3, d, tp);
            d = 99;
            VolumeYM2608Rhythm(screen, y, 1, d, 0, tp);
            d = 99;
            VolumeYM2608Rhythm(screen, y, 2, d, 0, tp);
        }
    }

    public static void screenInitYM2612(FrameBuffer screen, int tp, boolean onlyPCM, boolean isXGM) {
        if (screen == null)
            return;

        for (int y = 0; y < 9; y++) {

            int d;
            // boolean YM2612type = chipId==0 ?
            // parent.setting.YM2612Type.UseScci : setting.YM2612SType.UseScci;
            int tp6 = tp;
            if (tp6 == 1 && onlyPCM) {
                // tp6 = 0;
            }

            // note
            drawFont8(screen, 297, y * 8 + 8, 1, "   ");

            // keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                if (y != 5) {
                    drawKbn(screen, 33 + kx, y * 8 + 8, kt, tp);
                } else {
                    if (!isXGM)
                        drawKbn(screen, 33 + kx, y * 8 + 8, kt, tp6);
                }
            }

            if (isXGM) {
                Ch6YM2612XGM_P(screen, 1, 48, 0, false, tp6);
            }

            if (y != 5) {
                d = -1;
                d = volume(screen, 289, 8 + y * 8, 0, d, 0, tp);
            }

            if (y < 6) {
                d = 99;
                DrawBuff.Pan(screen, 25, 8 + y * 8, d, 3, d, tp);
                int b = 255;
                b = slot(screen, 257, 8 + y * 8, b, 0);
            }
            d = 1;
            font4Hex16Bit(screen, 273, 8 + y * 8, 0, d, 0);

            if (y != 5) {
                // ChYM2612_P(screen, 1, y * 8 + 8, y, false, tp);
            } else {
                // Ch6YM2612_P(screen, 1, y * 8 + 8, 0, false, tp6);
                d = -1;
                d = volume(screen, 289, 8 + y * 8, 0, d, 0, tp6);
                d = -1;
                DrawBuff.Pan(screen, 25, 8 + y * 8, d, 3, d, tp6);
            }

        }
    }

    public static void screenInitYM3526(FrameBuffer screen, int tp) {
        for (int y = 0; y < 9; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            boolean dm = true;
            DrawBuff.ChYM3526(screen, y, dm, false, tp);

            // Volume
            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 19, tp);
        }
    }

    public static void screenInitY8950(FrameBuffer screen, int tp) {
        for (int y = 0; y < 10; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            // Volume
            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 19, tp);
        }
    }

    public static void screenInitYM3812(FrameBuffer screen, int tp) {
        for (int y = 0; y < 9; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            boolean dm = true;
            DrawBuff.ChYM3812(screen, y, dm, false, tp);

            // Volume
            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 19, tp);
        }
    }

    public static void screenInitYMF262(FrameBuffer screen, int tp) {
        if (screen == null)
            return;

        for (int y = 0; y < 18; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            // boolean bd = false;
            // ChYMF262(screen, y,bd, true, tp);
            // ChYMF262(screen, y,bd, false, tp);
            drawPanP(screen, 24, y * 8 + 8, 3, tp);

            // Volume
            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 19, tp);
            d = volume(screen, 256, 8 + y * 8, 0, d, 0, tp);
        }

    }

    public static void screenInitYMF278B(FrameBuffer screen, int tp) {
        for (int y = 0; y < 18; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            // Volume
            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 19, tp);
        }

        for (int y = 19; y < 19 + 24; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 15 * 12; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            // Volume
            int d = 99;
            VolumeSt(screen, 512 - 4 * 15, y, 1, d, 19);
            d = 99;
            VolumeSt(screen, 512 - 4 * 15, y + 4, 1, d, 19);
        }

    }

    public static void screenInitYM2612MIDI(FrameBuffer screen) {
        if (screen == null)
            return;

        for (int c = 0; c < 6; c++) {
            for (int n = 0; n < 10; n++) {
                drawFont4V(screen, (c % 3) * 13 * 8 + 2 * 8 + n * 8, (c / 3) * 18 * 4 + 24 * 4, 0, "   ");
            }
        }
    }

    public static void screenInitNESDMC(FrameBuffer screen) {
        if (screen == null)
            return;

        for (int ch = 0; ch < 3; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, ch * 16 + 8, kt, 0);
            }
            drawFont8(screen, 296, ch * 16 + 8, 1, "   ");
            boolean m = true;
            ChNESDMC(screen, ch, m, false, 0);
        }
    }

    public static void screenInitFDS(FrameBuffer screen) {
        if (screen == null)
            return;

        for (int ot = 0; ot < 12 * 8; ot++) {
            int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            int kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, 8, kt, 0);
        }
        drawFont8(screen, 296, 8, 1, "   ");
        boolean m = true;
        ChFDS(screen, 0, m, false, 0);
    }

    public static void screenInitVRC7(FrameBuffer screen, int tp) {
        for (int y = 0; y < 6; y++) {
            // Note
            drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            // Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            // Volume
            int d = 99;
            d = volume(screen, 256, 8 + y * 8, 0, d, 0, tp);
        }
    }

    public static void InstOPN2(FrameBuffer screen, int x, int y, int c, int[] oi, int[] ni) {
        int sx = (c % 3) * 4 * 29 + x;
        int sy = (c / 3) * 8 * 6 + y;

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 11; i++) {
                if (oi[i + j * 11] != ni[i + j * 11]) {
                    drawFont4Int(screen, sx + i * 8 + (i > 5 ? 4 : 0), sy + j * 8, 0, (i == 5) ? 3 : 2, ni[i + j * 11]);
                    oi[i + j * 11] = ni[i + j * 11];
                }
            }
        }

        if (oi[44] != ni[44]) {
            drawFont4Int(screen, sx + 8 * 4, sy - 16, 0, 2, ni[44]);
            oi[44] = ni[44];
        }
        if (oi[45] != ni[45]) {
            drawFont4Int(screen, sx + 8 * 6, sy - 16, 0, 2, ni[45]);
            oi[45] = ni[45];
        }
        if (oi[46] != ni[46]) {
            drawFont4Int(screen, sx + 8 * 8 + 4, sy - 16, 0, 2, ni[46]);
            oi[46] = ni[46];
        }
        if (oi[47] != ni[47]) {
            drawFont4Int(screen, sx + 8 * 11, sy - 16, 0, 2, ni[47]);
            oi[47] = ni[47];
        }
    }

    public static void Inst(FrameBuffer screen, int x, int y, int c, int[] oi, int[] ni) {
        int sx = (c % 3) * 8 * 13 + x * 8;
        int sy = (c / 3) * 8 * 6 + 8 * y;

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 11; i++) {
                if (oi[i + j * 11] != ni[i + j * 11]) {
                    drawFont4Int(screen, sx + i * 8 + (i > 5 ? 4 : 0), sy + j * 8, 0, (i == 5) ? 3 : 2, ni[i + j * 11]);
                    oi[i + j * 11] = ni[i + j * 11];
                }
            }
        }

        if (oi[44] != ni[44]) {
            drawFont4Int(screen, sx + 8 * 4, sy - 16, 0, 2, ni[44]);
            oi[44] = ni[44];
        }
        if (oi[45] != ni[45]) {
            drawFont4Int(screen, sx + 8 * 6, sy - 16, 0, 2, ni[45]);
            oi[45] = ni[45];
        }
        if (oi[46] != ni[46]) {
            drawFont4Int(screen, sx + 8 * 8 + 4, sy - 16, 0, 2, ni[46]);
            oi[46] = ni[46];
        }
        if (oi[47] != ni[47]) {
            drawFont4Int(screen, sx + 8 * 11, sy - 16, 0, 2, ni[47]);
            oi[47] = ni[47];
        }
    }

    public static void InstOPNA(FrameBuffer screen, int x, int y, int c, int[] oi, int[] ni) {
        int sx = (c % 3) * 4 * 25 + x;
        int sy = (c / 3) * 8 * 6 + y;

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 11; i++) {
                if (oi[i + j * 11] != ni[i + j * 11]) {
                    drawFont4Int(screen, sx + i * 8 + (i > 5 ? 4 : 0), sy + j * 8, 0, (i == 5) ? 3 : 2, ni[i + j * 11]);
                    oi[i + j * 11] = ni[i + j * 11];
                }
            }
        }

        if (oi[44] != ni[44]) {
            drawFont4Int(screen, sx + 8 * 4, sy - 16, 0, 2, ni[44]);
            oi[44] = ni[44];
        }
        if (oi[45] != ni[45]) {
            drawFont4Int(screen, sx + 8 * 6, sy - 16, 0, 2, ni[45]);
            oi[45] = ni[45];
        }
        if (oi[46] != ni[46]) {
            drawFont4Int(screen, sx + 8 * 8 + 4, sy - 16, 0, 2, ni[46]);
            oi[46] = ni[46];
        }
        if (oi[47] != ni[47]) {
            drawFont4Int(screen, sx + 8 * 11, sy - 16, 0, 2, ni[47]);
            oi[47] = ni[47];
        }
    }

    public static void Inst(FrameBuffer screen, int x, int y, int c, int[] oi, int[] ni, int[] ot, int[] nt) {
        int sx = (c % 3) * 8 * 13 + x * 8;
        int sy = (c / 3) * 8 * 6 + 8 * y;

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 11; i++) {
                if (oi[i + j * 11] != ni[i + j * 11] || ot[i + j * 11] != nt[i + j * 11]) {
                    drawFont4Int(screen,
                            sx + i * 8 + (i > 5 ? 4 : 0),
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
            drawFont4Int(screen, sx + 8 * 4, sy - 16, nt[44], 2, ni[44]);
            oi[44] = ni[44];
            ot[44] = nt[44];
        }
        if (oi[45] != ni[45] || ot[45] != nt[45]) {
            drawFont4Int(screen, sx + 8 * 6, sy - 16, nt[45], 2, ni[45]);
            oi[45] = ni[45];
            ot[45] = nt[45];
        }
        if (oi[46] != ni[46] || ot[46] != nt[46]) {
            drawFont4Int(screen, sx + 8 * 8 + 4, sy - 16, nt[46], 2, ni[46]);
            oi[46] = ni[46];
            ot[46] = nt[46];
        }
        if (oi[47] != ni[47] || ot[47] != nt[47]) {
            drawFont4Int(screen, sx + 8 * 11, sy - 16, nt[47], 2, ni[47]);
            oi[47] = ni[47];
            ot[47] = nt[47];
        }
    }

    public static int slot(FrameBuffer screen, int x, int y, /* ref */ int os, int ns) {
        if (os == ns)
            return os;

        screen.drawByteArray(x + 0, y, rNESDMC, 64, ((ns & 1) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);
        screen.drawByteArray(x + 4, y, rNESDMC, 64, ((ns & 2) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);
        screen.drawByteArray(x + 8, y, rNESDMC, 64, ((ns & 4) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);
        screen.drawByteArray(x + 12, y, rNESDMC, 64, ((ns & 8) != 0 ? 1 : 0) * 4 + 32, 0, 4, 8);

        os = ns;
        return os;
    }

    public static int drawInstNumber(FrameBuffer screen, int x, int y, /* ref */ int oi, int ni) {
        if (oi != ni) {
            drawFont4Int(screen, x * 4, y * 4, 0, 2, ni);
            oi = ni;
        }
        return oi;
    }

    /**
     * ボリュームメータ描画
     * @param screen 描画対象バッファ@param
     * @param x x座標(x1)
     * @param y y座標(x1)
     * @param c 0:Mono 1:Stereo(L) 2:Stereo(R)
     * @param ov 前回の値(ref)
     * @param nv 今回の値
     * @param tp 0:EMU 1:Real
     */
    public static int volume(FrameBuffer screen, int x, int y, int c, /* ref */ int ov, int nv, int tp) {
        if (ov == nv)
            return ov;

//        int t = 0;
//        int sy = 0;
//        if (c == 1 || c == 2) { t = 4; }
//        if (c == 2) { sy = 4; }
//        y = (y + 1) * 8;

//        for (int i = 0; i <= 19; i++) {
//            VolumeP(screen, 256 + i * 2, y + sy, (1 + t), tp);
//        }

//        for (int i = 0; i <= nv; i++) {
//            VolumeP(screen, 256 + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
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
            VolumeP(screen, x + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
        return ov;
    }

    public static int VolumeToC140(FrameBuffer screen, int y, int c, /* ref */ int ov, int nv, int tp) {
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
        y = (y + 1) * 8;

        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, 356 + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, 356 + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
        return ov;
    }

    public static void VolumeSt(FrameBuffer screen, int x, int y, int c, int ov, int nv) {
        if (ov == nv)
            return;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }
        y = (y + 1) * 8;

        // x=256
        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, x + i * 2, y + sy, (1 + t), 0);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), 0);
        }

        ov = nv;
    }

    public static void VolumeToHuC6280(FrameBuffer screen, int y, int c, int ov, int nv) {
        if (ov == nv)
            return;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }
        y = (y + 1) * 8;

        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, 256 + i * 2, y + sy, (1 + t), 0);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, 256 + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), 0);
        }

        ov = nv;
    }

    public static int VolumeToOKIM6295(FrameBuffer screen, int y, /* ref */ int ov, int nv) {
        if (ov == nv)
            return ov;

        int t = 0;
        int sy = 0;
        y = (y + 1) * 8;

        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, 80 + i * 2, y + sy, (1 + t), 0);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, 80 + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), 0);
        }

        ov = nv;
        return ov;
    }

    public static void VolumeLCDToMIDILCD(FrameBuffer screen,
                                          int MIDImodule,
                                          int x,
                                          int y,
                                          int oldValue1,
                                          int value1,
                                          int oldValue2,
                                          int value2) {
        if (oldValue1 == value1 && oldValue2 == value2)
            return;

        int s;
        int vy = y;
        // for (int n = (Math.min(oldValue1, value1) / 8); n < 16; n++)
        for (int n = 0; n < 16; n++) {
            s = (value1 / 8) < n ? 8 : 0;
            screen.drawByteArray(x, vy, rMIDILCD[MIDImodule], 136, 8 * 16, s, 8, (n % 2 == 0 ? 2 : 3));
            vy -= (n % 2 == 0 ? 4 : 3);
        }

        s = value2 / 8;
        screen.drawByteArray(x, y - s * 3 - (s + 1) / 2, rMIDILCD[MIDImodule], 136, 8 * 16, 0, 8, (s % 2 == 0 ? 2 : 3));

        oldValue1 = value1;
        oldValue2 = value2;
    }

    public static int VolumeToMIDILCD(FrameBuffer screen, int MIDImodule, int x, int y, /* ref */ int oldValue, int value) {
        if (oldValue == value)
            return oldValue;

        int s;
        for (int n = (Math.min(oldValue, value) / 5); n < (Math.max(oldValue, value) / 5) + 1; n++) {
            s = (value / 5) < n ? 2 : 0;
            screen.drawByteArray(n * 2 + x, y, rMIDILCD_Vol[MIDImodule], 32, 0 + (n > 23 ? 4 : 0) + s, 0, 2, 8);
        }

        oldValue = value;
        return oldValue;
    }

    public static void VolumeXY(FrameBuffer screen, int x, int y, int c, int ov, int nv, int tp) {
        if (ov == nv)
            return;

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
            VolumeP(screen, x + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
    }

    public static void VolumeXYOPN2(FrameBuffer screen, int x, int y, int c, int ov, int nv, int tp) {
        if (ov == nv)
            return;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }

        // y *= 4;
        // x *= 4;

        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, x + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, x + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
    }

    public static void VolumeYM2608Rhythm(FrameBuffer screen, int x, int c, int ov, int nv, int tp) {
        if (ov == nv)
            return;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }
        x = x * 4 * 15 + 20;

        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, x + i * 2, sy + 8 * 14, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, x + i * 2, sy + 8 * 14, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
    }

    public static void VolumeYM2610Rhythm(FrameBuffer screen, int x, int c, int ov, int nv, int tp) {

        if (ov == nv)
            return;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }
        x = x * 4 * 13 + 8 * 2;

        for (int i = 0; i <= 19; i++) {
            VolumeP(screen, x + i * 2, sy + 8 * 13, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            VolumeP(screen, x + i * 2, sy + 8 * 13, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
    }

    public static void keyBoard(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        drawFont8(screen, 296, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(screen, 296, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                drawFont8(screen, 312, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
    }

    public static void KeyBoardXY(FrameBuffer screen, int x, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, x + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, x + kx, y, kt, tp);
        }

        drawFont8(screen, 264 + x, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(screen, 264 + x, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                drawFont8(screen, 280 + x, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
    }

    public static void KeyBoardOPNA(FrameBuffer screen, int x, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, x + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, x + kx, y, kt, tp);
        }

        drawFont8(screen, 296 + x, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(screen, 296 + x, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                drawFont8(screen, 312 + x, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
    }

    public static void KeyBoardOPNM(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 33 + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 33 + kx, y, kt, tp);
        }

        drawFont8(screen, 329, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(screen, 329, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                drawFont8(screen, 345, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
    }

    public static void KeyBoardDMG(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        drawFont8(screen, 312, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(screen, 312, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                drawFont8(screen, 328, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
    }

    public static void KeyBoardToC140(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        if (nt >= 0) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
            drawFont8(screen, 396, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 8) {
                drawFont8(screen, 412, y, 1, Tables.kbo[nt / 12]);
            }
        } else {
            drawFont8(screen, 396, y, 1, "   ");
        }

        ot = nt;
    }

    public static void KeyBoardToC352(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        if (nt >= 0) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
            drawFont8(screen, 500, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 8) {
                drawFont8(screen, 516, y, 1, Tables.kbo[nt / 12]);
            }
        } else {
            drawFont8(screen, 500, y, 1, "   ");
        }

        ot = nt;
    }

    public static void KeyBoardToMultiPCM(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        if (nt >= 0) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
            drawFont8(screen, 63 * 8 + 4, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 8) {
                drawFont8(screen, 65 * 8 + 4, y, 1, Tables.kbo[nt / 12]);
            }
        } else {
            drawFont8(screen, 63 * 8 + 4, y, 1, "   ");
        }

        ot = nt;
    }

    public static void KeyBoardToYMF278BPCM(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0 && ot < 12 * 15) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 15) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        drawFont8(screen, 300 + 8 * 24, y, 1, "   ");

        if (nt >= 0) {
            drawFont8(screen, 300 + 8 * 24, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 15) {
                drawFont8(screen, 300 + 8 * 26, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
    }

    public static void KeyBoardToQSound(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            drawKbn(screen, 32 + kx, y, kt, tp);
        }

        int x = 52;
        if (nt >= 0) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            drawKbn(screen, 32 + kx, y, kt, tp);
            drawFont8(screen, x * 8, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 8) {
                drawFont8(screen, (x + 2) * 8, y, 1, Tables.kbo[nt / 12]);
            }
        } else {
            drawFont8(screen, x * 8, y, 1, "   ");
        }

        ot = nt;
    }

    public static void Pan(FrameBuffer screen, int x, int y, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        drawPanP(screen, x, y, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void PanType2(FrameBuffer screen, int c, int ot, int nt, int tp) {

        if (ot == nt) {
            return;
        }

        drawPanType2P(screen, 24, 8 + c * 8, nt, tp);
        ot = nt;
    }

    public static void PanType3(FrameBuffer screen, int c, int ot, int nt, int tp) {

        if (ot == nt) {
            return;
        }

        drawPanType3P(screen, 24, 8 + c * 8, nt, tp);
        ot = nt;
    }

    public static void PanType2(FrameBuffer screen, int x, int y, int ot, int nt, int tp) {

        if (ot == nt) {
            return;
        }

        drawPanType2P(screen, x, y, nt, tp);
        ot = nt;
    }

    public static void PanToOKIM6258(FrameBuffer screen, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        drawPanP(screen, 24, 8, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void PanYM2608Rhythm(FrameBuffer screen, int c, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        drawPanP(screen, c * 4 * 15 + 12, 8 * 14, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void PanYM2610Rhythm(FrameBuffer screen, int c, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        drawPanP(screen, c * 4 * 13 + 8, 8 * 13, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void ChAY8910(FrameBuffer screen, int ch, Boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChAY8910_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChS5B(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChS5B_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChC140(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChC140_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChQSound(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }
        if (ch < 16) {
            ChQSound_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        } else {
            ChQSoundAdpcm_P(screen, 224, 8 + ch * 8, ch - 16, nm != null && nm, tp);
        }
        om = nm;
    }

    public static void ChC352(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChC352_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChHuC6280(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChHuC6280_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static boolean ChOKIM6295(FrameBuffer screen, int ch, /* ref */ boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return om;
        }

        ChOKIM6295_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
        return om;
    }

    public static void ChK051649(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChK051649_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChRF5C164(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChRF5C164_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChOKIM6258(FrameBuffer screen, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChOKIM6258_P(screen, 0, 8 + 0 * 8, nm != null && nm, tp);
        om = nm;
    }

    public static void ChSegaPCM(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChSegaPCM_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChSN76489(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChSN76489_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChSN76489Noise(FrameBuffer screen, MDChipParams.Channel osc, MDChipParams.Channel nsc, int tp) {
        if (osc.note == nsc.note)
            return;

        drawFont4(screen, 56, 32, tp, (nsc.note & 0x4) != 0 ? "WHITE   " : "PERIODIC");
        drawFont4(screen,
                120,
                32,
                tp,
                (nsc.note & 0x3) == 0 ? "0  " : ((nsc.note & 0x3) == 1 ? "1  " : ((nsc.note & 0x3) == 2 ? "2  " : "CH3")));

        osc.note = nsc.note;
    }

    public static void ChYM2151(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2151_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
    }

    public static void ChYM2203(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2203_P(screen, 0, 8 + ch * 8, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void Ch3YM2203(FrameBuffer screen, int ch, boolean om, Boolean nm, boolean oe, boolean ne, int tp) {

        if (om == nm && oe == ne) {
            return;
        }

        Ch3YM2612_P(screen, 0, 8 + ch * 8, ch, nm == null ? false : nm, ne, tp);
        om = nm;
        oe = ne;
    }

    public static void chYM2413(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2413_P(screen, 0, ch < 9 ? (8 + ch * 8) : (8 + 9 * 8), ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChY8950(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }
        ChY8950_P(screen,
                0,
                ch < 9 ? (8 + ch * 8) : (ch < 14 ? (8 + 10 * 8) : (8 + 9 * 8)),
                ch,
                nm == null ? false : nm,
                tp);
        om = nm;
    }

    public static void ChYM3526(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }
        ChYM3526_P(screen, 0, ch < 9 ? (8 + ch * 8) : (8 + 9 * 8), ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChYM3812(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }
        ChYM3812_P(screen, 0, ch < 9 ? (8 + ch * 8) : (8 + 9 * 8), ch, nm == null ? false : nm, tp);
        om = nm;
    }

    private static final byte[] YMF262Ch = new byte[] {
            0, 3, 1, 4, 2, 5, 6, 7, 8, 9, 12, 10, 13, 11, 14, 15, 16, 17, 18, 19, 20, 21, 22
    };

    public static void ChYMF262(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }
        ChYMF262_P(screen, 0, ch < 18 ? (8 + ch * 8) : (8 + 18 * 8), YMF262Ch[ch], nm == null ? false : nm, tp);
        om = nm;
    }

    private static final byte[] YMF278BCh = new byte[] {
            0, 3, 1, 4, 2, 5, 6, 7, 8, 9, 12, 10, 13, 11, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46
    };

    public static void ChYMF278B(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }
        ChYMF278B_P(screen,
                0,
                ch < 18 ? (8 + ch * 8) : (ch < 23 ? (8 + 18 * 8) : (8 + (ch - 4) * 8)),
                YMF278BCh[ch],
                nm == null ? false : nm,
                tp);
        om = nm;
    }

    public static void ChYM2608(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2608_P(screen, 1, 8 + ch * 8, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void Ch3YM2608(FrameBuffer screen, int ch, boolean om, Boolean nm, boolean oe, boolean ne, int tp) {

        if (om == nm && oe == ne) {
            return;
        }

        Ch3YM2612_P(screen, 1, 8 + ch * 8, ch, nm == null ? false : nm, ne, tp);
        om = nm;
        oe = ne;
    }

    public static void ChYM2608Rhythm(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2608Rhythm_P(screen, 0, 8 * 14, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChYM2610(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2610_P(screen, 0, 8 + ch * 8, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void Ch3YM2610(FrameBuffer screen, int ch, boolean om, Boolean nm, boolean oe, boolean ne, int tp) {

        if (om == nm && oe == ne) {
            return;
        }

        Ch3YM2612_P(screen, 0, 8 + ch * 8, ch, nm == null ? false : nm, ne, tp);
        om = nm;
        oe = ne;
    }

    public static void ChYM2610Rhythm(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2610Rhythm_P(screen, 0, 8 * 13, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChYM2612(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChYM2612_P(screen, 1, 8 + ch * 8, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void Ch3YM2612(FrameBuffer screen, int ch, boolean om, Boolean nm, boolean oe, boolean ne, int tp) {

        if (om == nm && oe == ne) {
            return;
        }

        Ch3YM2612_P(screen, 1, 8 + ch * 8, ch, nm == null ? false : nm, ne, tp);
        om = nm;
        oe = ne;
    }

    public static void Ch6YM2612(FrameBuffer screen, int buff, int ot, int nt, boolean om, Boolean nm, int otp, int ntp) {
        if (buff == 0) {
            if (ot == nt && om == nm && otp == ntp) {
                return;
            }
        }

        Ch6YM2612_P(screen, 1, 48, nt, nm == null ? false : nm, ntp);
        ot = nt;
        om = nm;
        otp = ntp;
    }

    public static void Ch6YM2612XGM(FrameBuffer screen, int buff, int ot, int nt, boolean om, Boolean nm, int otp, int ntp) {
        if (buff == 0) {
            if (ot == nt && om == nm && otp == ntp) {
                return;
            }
        }

        Ch6YM2612XGM_P(screen, 1, 48, nt, nm == null ? false : nm, ntp);
        ot = nt;
        om = nm;
        otp = ntp;
    }

    public static void ChNESDMC(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChNESDMC_P(screen, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChFDS(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChFDS_P(screen, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChMMC5(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChMMC5_P(screen, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChDMG(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {
        if (om == nm)
            return;

        ChDMG_P(screen, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void chVRC6(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChVRC6_P(screen, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ChN163(FrameBuffer screen, int ch, boolean om, Boolean nm, int tp) {

        if (om == nm) {
            return;
        }

        ChN163_P(screen, ch, nm == null ? false : nm, tp);
        om = nm;
    }

    public static void ToneNoise(FrameBuffer screen, int x, int y, int c, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        ToneNoiseP(screen, x * 4, y * 4 + c * 8, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void Nfrq(FrameBuffer screen, int x, int y, int onfrq, int nnfrq) {
        if (onfrq == nnfrq) {
            return;
        }

        x *= 4;
        y *= 4;
        drawFont4Int(screen, x, y, 0, 2, nnfrq);

        onfrq = nnfrq;
    }

    public static void Efrq(FrameBuffer screen, int x, int y, int oefrq, int nefrq) {
        if (oefrq == nefrq) {
            return;
        }

        x *= 4;
        y *= 4;
        drawFont4(screen, x, y, 0, String.format("%5d", nefrq));

        oefrq = nefrq;
    }

    public static void Etype(FrameBuffer screen, int x, int y, int oetype, int netype) {
        if (oetype == netype) {
            return;
        }

        x *= 4;
        y *= 4;

        drawEtypeP(screen, x, y, netype);
        oetype = netype;
    }

    public static void WaveFormToHuC6280(FrameBuffer screen, int c, int[] oi, int[] ni) {
        for (int i = 0; i < 32; i++) {
            if (oi[i] == ni[i])
                continue;

            int n = (17 - ni[i]);
            int x = i + (((c > 2) ? c - 3 : c) * 8 * 13) + 4 * 7;
            int y = (((c > 2) ? 1 : 0) * 8 * 5) + 4 * 22;

            int m;
            m = (n > 7) ? 8 : n;
            screen.drawByteArray(x, y, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 15) ? 8 : (Math.max((n - 8), 0));
            screen.drawByteArray(x, y - 8, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 23) ? 8 : (Math.max((n - 16), 0));
            screen.drawByteArray(x, y - 16, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 31) ? 8 : (Math.max((n - 24), 0));
            screen.drawByteArray(x, y - 23, rWavGraph, 64, m + 1, 0, 1, 7);

            oi[i] = ni[i];
        }
    }

    public static void WaveFormToK051649(FrameBuffer screen, int c, int[] oi, int[] ni) {
        for (int i = 0; i < 32; i++) {
            if (oi[i] == ni[i])
                continue;

            int n = (ni[i] / 8) + 16;
            int x = c % 3;
            x = x * 104 + i + 4;
            int y = c / 3;
            y = y * 48 + 80;

            int m;
            m = (n > 7) ? 8 : n;
            screen.drawByteArray(x, y, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 15) ? 8 : (Math.max((n - 8), 0));
            screen.drawByteArray(x, y - 8, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 23) ? 8 : (Math.max((n - 16), 0));
            screen.drawByteArray(x, y - 16, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 31) ? 8 : (Math.max((n - 24), 0));
            screen.drawByteArray(x, y - 23, rWavGraph, 64, m + 1, 0, 1, 7);

            oi[i] = ni[i];
        }
    }

    public static void WaveFormToFDS(FrameBuffer screen, int c, int[] oi, int[] ni) {
        for (int i = 0; i < 32; i++) {
            if (oi[i] == ni[i])
                continue;

            int n = ni[i];
            int x = i + c * 4 * 31 + 8;
            int y = 8 * 6;

            int m;
            m = (n > 7) ? 8 : n;
            screen.drawByteArray(x, y, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 15) ? 8 : (Math.max((n - 8), 0));
            screen.drawByteArray(x, y - 8, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 23) ? 8 : (Math.max((n - 16), 0));
            screen.drawByteArray(x, y - 16, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 31) ? 8 : (Math.max((n - 24), 0));
            screen.drawByteArray(x, y - 23, rWavGraph, 64, m + 1, 0, 1, 7);

            oi[i] = ni[i];
        }
    }

    public static void WaveFormToN106(FrameBuffer screen, int x, int y, short[] oi, short[] ni) {
        if (ni == null)
            return;

        for (int i = 0; i < ni.length; i++) {
            if (oi[i] == ni[i])
                continue;

            screen.drawByteArray(x + i, y, rWavGraph2, 33, ni[i] % 33, 0, 1, 16);

            oi[i] = ni[i];
        }
    }

    public static void WaveFormToDMG(FrameBuffer screen, int x, int y, byte[] oi, byte[] ni) {
        for (int i = 0; i < 32; i++) {
            if (oi[i] == ni[i])
                continue;

            int n = ni[i];

            int m;
            m = (n > 7) ? 8 : n;
            screen.drawByteArray(x + i, y, rWavGraph, 64, m, 0, 1, 8);
            m = (n > 15) ? 8 : (Math.max((n - 8), 0));
            screen.drawByteArray(x + i, y - 8, rWavGraph, 64, m, 0, 1, 8);
            // m = (n > 23) ? 8 : ((n - 16) < 0 ? 0 : (n - 16));
            // screen.drawByteArray(x + i, y - 16, rWavGraph, 64, m, 0, 1, 8);
            // m = (n > 31) ? 8 : ((n - 24) < 0 ? 0 : (n - 24));
            // screen.drawByteArray(x + i, y - 23, rWavGraph, 64, m + 1, 0, 1,
            // 7);

            oi[i] = ni[i];
        }
    }

    public static void DDAToHuC6280(FrameBuffer screen, int c, boolean od, boolean nd) {
        if (od == nd)
            return;

        int x = (((c > 2) ? c - 3 : c) * 8 * 13) + 4 * 22;
        int y = (((c > 2) ? 1 : 0) * 8 * 5) + 4 * 18;

        drawFont4(screen, x, y, 0, nd ? "ON " : "OFF");
        od = nd;
    }

    public static void NoiseToHuC6280(FrameBuffer screen, int c, boolean od, boolean nd) {
        if (od == nd)
            return;

        int x = (((c > 2) ? c - 3 : c) * 8 * 13) + 4 * 22;
        int y = (((c > 2) ? 1 : 0) * 8 * 5) + 4 * 20;

        drawFont4(screen, x, y, 0, nd ? "ON " : "OFF");
        od = nd;
    }

    public static void NoiseFrqToHuC6280(FrameBuffer screen, int c, int od, int nd) {
        if (od == nd)
            return;

        int x = (((c > 2) ? c - 3 : c) * 8 * 13) + 4 * 22;
        int y = (((c > 2) ? 1 : 0) * 8 * 5) + 4 * 22;

        drawFont4(screen, x, y, 0, String.format("%2d", nd));
        od = nd;
    }

    public static void MainVolumeToHuC6280(FrameBuffer screen, int c, int od, int nd) {
        if (od == nd)
            return;

        int x = 8 * 9;
        int y = c * 8 + 8 * 17;

        drawFont4(screen, x, y, 0, String.format("%2d", nd));
        od = nd;
    }

    public static void LfoCtrlToHuC6280(FrameBuffer screen, int od, int nd) {
        if (od == nd)
            return;

        int x = 8 * 17;
        int y = 8 * 17;

        drawFont4(screen, x, y, 0, String.format("%1d", nd));
        od = nd;
    }

    public static void LfoFrqToHuC6280(FrameBuffer screen, int od, int nd) {
        if (od == nd)
            return;

        int x = 8 * 16;
        int y = 8 * 18;

        drawFont4(screen, x, y, 0, String.format("%3d", nd));
        od = nd;
    }

    public static byte drawMIDILCD_Fader(FrameBuffer screen,
                                         int MIDImodule,
                                         int faderType,
                                         int x,
                                         int y,
                                         /* ref */ byte oldValue,
                                         byte value) {
        if (oldValue == value)
            return oldValue;
        oldValue = value;

        int v;
        switch (faderType) {
        case 0:
            v = Math.max(value - 8, 0) / 8;
            drawMIDILCD_FaderP(screen, MIDImodule, 0, x, y, v);
            break;
        case 1:
            v = value / 8;
            drawMIDILCD_FaderP(screen, MIDImodule, 1, x, y, v);
            break;
        }
        return oldValue;
    }

    public static short drawMIDILCD_Fader(FrameBuffer screen,
                                         int MIDImodule,
                                         int faderType,
                                         int x,
                                         int y,
                                         /* ref */ short oldValue,
                                         short value) {
        if (oldValue == value)
            return oldValue;
        oldValue = value;

        int v;
        switch (faderType) {
        case 0:
            v = Math.max(value - 0x1ff, 0) / 0x3ff;
            drawMIDILCD_FaderP(screen, MIDImodule, 0, x, y, v);
            break;
        case 1:
            break;
        }
        return oldValue;
    }

    public static byte drawMIDILCD_Kbd(FrameBuffer screen, int x, int y, int note, /* ref */ byte oldVel, byte vel) {
        if (oldVel == vel)
            return oldVel;
        oldVel = vel;

        drawMIDILCD_KbdP(screen, x, y, note, vel);
        return oldVel;
    }

    public static String drawFont4MIDINotes(FrameBuffer screen, int x, int y, int t, /* ref */ String oldnotes, String notes) {
        if (oldnotes.equals(notes))
            return oldnotes;
        oldnotes = notes;

        if (screen == null)
            return oldnotes;

        drawFont4(screen, x, y, t, notes);
        return oldnotes;
    }

    public static void drawMIDI_Lyric(FrameBuffer screen, int chipId, int x, int y, String oldValue1, String value1) {
        // if (oldValue1 == value1) return;

//        gMIDILyric[chipId].clear(Color.black);
//        JTextRenderer
//                .DrawText(gMIDILyric[chipId], value1, fntMIDILyric[chipId], new Point(0, 0), Color.white);
        byte[] bit = getByteArray(bitmapMIDILyric[chipId]);
        screen.drawByteArray(x, y, bit, 200, 0, 0, 200, 24);

        oldValue1 = value1;
    }

    public static void drawMIDI_MacroXG(FrameBuffer screen,
                                        int MIDImodule,
                                        int macroType,
                                        int x,
                                        int y,
                                        int oldValue1,
                                        int value1) {
        // if (oldValue1 == value1) return;

        drawFont4(screen, x, y, 2 + MIDImodule, Tables.tblMIDIEffectXG[macroType][value1]);

        oldValue1 = value1;
    }

    public static void drawMIDI_MacroGS(FrameBuffer screen,
                                        int MIDImodule,
                                        int macroType,
                                        int x,
                                        int y,
                                        int oldValue1,
                                        int value1) {
        // if (oldValue1 == value1) return;

        drawFont4(screen, x, y, 2 + MIDImodule, Tables.tblMIDIEffectGS[macroType][value1]);

        oldValue1 = value1;
    }

    public static void drawMIDILCD_Letter(FrameBuffer screen, int MIDImodule, int x, int y, byte[] oldValue, int len) {
        for (int i = 0; i < 16; i++) {
            if (oldValue[i] == Tables.spc[i])
                continue;
            oldValue[i] = Tables.spc[i];

            if (screen == null)
                return;

            int cd;
            // if (i < len)
            cd = Tables.spc[i] - ' ';

            screen.drawByteArray(x + i * 8, y, rMIDILCD_Font[MIDImodule], 128, (cd % 16) * 8, (cd / 16) * 8, 8, 8);
        }

    }

    public static void drawMIDILCD_Letter(FrameBuffer screen,
                                          int MIDImodule,
                                          int x,
                                          int y,
                                          byte[] oldValue,
                                          byte[] value,
                                          int len) {
        for (int i = 0; i < 20; i++) {
            if (oldValue[i] == value[i])
                continue;
            oldValue[i] = value[i];

            if (screen == null)
                return;

            int cd;
            // if (i < len)
            cd = value[i] - ' ';

            screen.drawByteArray(x + i * 8, y, rMIDILCD_Font[MIDImodule], 128, (cd % 16) * 8, (cd / 16) * 8, 8, 8);
        }

    }

    public static void drawFont4IntMIDI(FrameBuffer screen, int x, int y, int t, byte oldnum, byte num) {
        if (oldnum == num)
            return;
        oldnum = num;

        if (screen == null)
            return;

        int n;

        n = num / 100;
        num -= (byte) (n * 100);
        // n = (n > 9) ? 0 : n;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 10;
        num -= (byte) (n * 10);
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

    }

    public static void drawFont4IntMIDIInstrument(FrameBuffer screen, int x, int y, int t, byte oldnum, byte num) {
        if (oldnum == num)
            return;
        oldnum = num;

        if (screen == null)
            return;

        drawFont4(screen, x, y + 8, t, Tables.tblMIDIInstrumentGM[num]);

        int n;

        n = num / 100;
        num -= (byte) (n * 100);
        // n = (n > 9) ? 0 : n;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 10;
        num -= (byte) (n * 10);
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

    }

    public static void drawFader(FrameBuffer screen, int x, int y, int t, int od, int nd) {
        if (od == nd)
            return;

        drawFaderSlitP(screen, x, y - 8);
        drawFont4IntM(screen, x, y + 48, 3, nd);

        int n;

        if (nd >= 0) {
            n = -(int) (nd / 20.0 * 8.0);
        } else {
            n = -(int) (nd / 192.0 * 35.0);
        }

        y += n;

        drawFaderP(screen, x, y, t);

        od = nd;
    }

    public static void drawGFader(FrameBuffer screen, int x, int y, int t, int od, int nd) {
        if (od == nd)
            return;

        drawFaderSlitP(screen, x, y - 8);
        drawFont4IntM(screen, x, y + 48, 3, nd);

        int n = 35 - (int) (nd / 127.0 * 43.0);
        y += n;

        drawFaderP(screen, x, y, t);

        od = nd;
    }

    public static void MixerVolume(FrameBuffer screen, int x, int y, int od, int nd, int ov, int nv) {
        if (od == nd && ov == nv)
            return;

        for (int i = 0; i < 44; i++) {
            int t = i < 8 ? 0 : 1;
            if (i % 2 != 0)
                t = 2;
            else if (44 - i > nd)
                t = 2;

            drawMixerVolumeP(screen, x, y + i, t);
        }

        drawMixerVolumeP(screen, x, y + (44 - nv), nv > 36 ? 0 : 1);

        od = nd;
        ov = nv;
    }

    public static void KfYM2151(FrameBuffer screen, int ch, int ok, int nk) {
        if (ok == nk) {
            return;
        }

        int x = (ch % 4) * 4 * 3 + 4 * 67;
        int y = (ch / 4) * 8 + 8 * 22;
        drawFont4Int(screen, x, y, 0, 2, nk);
        ok = nk;
    }

    public static void NeYM2151(FrameBuffer screen, int one, int nne) {
        if (one == nne) {
            return;
        }

        int x = 4 * 60;
        int y = 8 * 22;
        drawFont4Int(screen, x, y, 0, 1, nne);

        one = nne;
    }

    public static void NfrqYM2151(FrameBuffer screen, int onfrq, int nnfrq) {
        if (onfrq == nnfrq) {
            return;
        }

        int x = 4 * 60;
        int y = 8 * 23;
        drawFont4Int(screen, x, y, 0, 2, nnfrq);

        onfrq = nnfrq;
    }

    public static void LfrqYM2151(FrameBuffer screen, int olfrq, int nlfrq) {
        if (olfrq == nlfrq) {
            return;
        }

        int x = 4 * 59;
        int y = 8 * 24;
        drawFont4Int(screen, x, y, 0, 3, nlfrq);

        olfrq = nlfrq;
    }

    public static void AmdYM2151(FrameBuffer screen, int oamd, int namd) {
        if (oamd == namd) {
            return;
        }

        int x = 4 * 59;
        int y = 8 * 26;
        drawFont4Int(screen, x, y, 0, 3, namd);

        oamd = namd;
    }

    public static void PmdYM2151(FrameBuffer screen, int opmd, int npmd) {
        if (opmd == npmd) {
            return;
        }

        int x = 4 * 59;
        int y = 8 * 25;
        drawFont4Int(screen, x, y, 0, 3, npmd);

        opmd = npmd;
    }

    public static void WaveFormYM2151(FrameBuffer screen, int owaveform, int nwaveform) {
        if (owaveform == nwaveform) {
            return;
        }

        int x = 4 * 68;
        int y = 8 * 24;
        drawFont4Int(screen, x, y, 0, 1, nwaveform);

        owaveform = nwaveform;
    }

    public static void LfoSyncYM2151(FrameBuffer screen, int olfosync, int nlfosync) {
        if (olfosync == nlfosync) {
            return;
        }

        int x = 4 * 68;
        int y = 8 * 25;
        drawFont4Int(screen, x, y, 0, 1, nlfosync);

        olfosync = nlfosync;
    }

    public static void Tn(FrameBuffer screen, int x, int y, int c, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        drawTnP(screen, x * 4, y * 4 + c * 8, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void TnOPNA(FrameBuffer screen, int x, int y, int c, int ot, int nt, int otp, int ntp) {

        if (ot == nt && otp == ntp) {
            return;
        }

        drawTnP(screen, x * 4 + 1, y * 4 + c * 8, nt, ntp);
        ot = nt;
        otp = ntp;
    }

    public static void flag16Bit(FrameBuffer screen, int x, int y, int t, int oi, int ni) {
        if (oi != ni) {
            drawFont4(screen, x + 0, y, t, (ni & 0x8000) == 0 ? "-" : "*");
            drawFont4(screen, x + 4, y, t, (ni & 0x4000) == 0 ? "-" : "*");
            drawFont4(screen, x + 8, y, t, (ni & 0x2000) == 0 ? "-" : "*");
            drawFont4(screen, x + 12, y, t, (ni & 0x1000) == 0 ? "-" : "*");
            drawFont4(screen, x + 16, y, t, (ni & 0x0800) == 0 ? "-" : "*");
            drawFont4(screen, x + 20, y, t, (ni & 0x0400) == 0 ? "-" : "*");
            drawFont4(screen, x + 24, y, t, (ni & 0x0200) == 0 ? "-" : "*");
            drawFont4(screen, x + 28, y, t, (ni & 0x0100) == 0 ? "-" : "*");
            drawFont4(screen, x + 32, y, t, (ni & 0x0080) == 0 ? "-" : "*");
            drawFont4(screen, x + 36, y, t, (ni & 0x0040) == 0 ? "-" : "*");
            drawFont4(screen, x + 40, y, t, (ni & 0x0020) == 0 ? "-" : "*");
            drawFont4(screen, x + 44, y, t, (ni & 0x0010) == 0 ? "-" : "*");
            drawFont4(screen, x + 48, y, t, (ni & 0x0008) == 0 ? "-" : "*");
            drawFont4(screen, x + 52, y, t, (ni & 0x0004) == 0 ? "-" : "*");
            drawFont4(screen, x + 56, y, t, (ni & 0x0002) == 0 ? "-" : "*");
            drawFont4(screen, x + 60, y, t, (ni & 0x0001) == 0 ? "-" : "*");
            oi = ni;
        }
    }

    public static void susFlag(FrameBuffer screen, int x, int y, int t, int oi, int ni) {
        if (oi != ni) {
            drawFont4(screen, x * 4, y * 4, t, ni == 0 ? "-" : "*");
            oi = ni;
        }
    }

    public static void Kakko(FrameBuffer screen, int x, int y, int t, int ot, int nt) {
        if (ot != nt) {
            screen.drawByteArray(x, y, rKakko, 16, nt * 4, 0, 4, 8);
            for (int n = 0; n < t; n++) {
                screen.drawByteArray(x, y + n * 8 + 8, rKakko, 16, nt * 4, 8, 4, 8);
            }
            screen.drawByteArray(x, y + t * 8 + 8, rKakko, 16, nt * 4, 16, 4, 8);

            ot = nt;
        }
    }

    public static void OpxOP(FrameBuffer screen, int x, int y, int t, int ot, int nt) {
        if (ot != nt) {
            screen.drawByteArray(x, y, rType_YMF271, 32, nt * 8, 0, 8, 32);

            ot = nt;
        }
    }

    public static void LfoSw(FrameBuffer screen, int x, int y, boolean olfosw, boolean nlfosw) {
        if (olfosw == nlfosw) {
            return;
        }

        drawFont4(screen, x, y, 0, nlfosw ? "ON " : "OFF");

        olfosw = nlfosw;
    }

    public static void LfoFrq(FrameBuffer screen, int x, int y, int olfofrq, int nlfofrq) {
        if (olfofrq == nlfofrq) {
            return;
        }

        drawFont4Int(screen, x, y, 0, 1, nlfofrq);

        olfofrq = nlfofrq;
    }

    public static void NoteLogYM2612MIDI(FrameBuffer screen, int x, int y, int oln, int nln) {
        if (oln == nln)
            return;
        if (nln == -1) {
            drawFont4V(screen, x, y, 0, "   ");
        } else {
            drawFont4V(screen, x, y, 0, Tables.kbnp[nln % 12]);
            drawFont4V(screen, x, y - 2 * 4, 0, Tables.kbo[nln / 12]);
        }
        oln = nln;
    }

    public static void UseChannelYM2612MIDI(FrameBuffer screen, int x, int y, boolean olm, boolean nlm) {
        // if (olm == nlm) return;

        drawFont8(screen, x, y, 1, nlm ? "^" : "-");

        olm = nlm;
    }

    public static void MONOPOLYYM2612MIDI(FrameBuffer screen, boolean olm, boolean nlm) {
        if (olm == nlm)
            return;

        drawFont8(screen, 8, 16, 1, nlm ? "^" : "-");
        drawFont8(screen, 8, 24, 1, nlm ? "-" : "^");

        olm = nlm;
    }

    public static void ToneFormat(FrameBuffer screen, int x, int y, int oToneFormat, int nToneFormat) {
        if (oToneFormat == nToneFormat) {
            return;
        }

        x *= 4;
        y *= 4;

        drawToneFormatP(screen, x, y, nToneFormat);

        oToneFormat = nToneFormat;
    }

    public static void drawChipName(FrameBuffer screen, int x, int y, int t, byte oc, byte nc) {
        if (oc == nc)
            return;

        drawChipNameP(screen, x, y, t, nc);

        oc = nc;
    }

    public static void drawTimer(FrameBuffer screen, int c, int ot1, int ot2, int ot3, int nt1, int nt2, int nt3) {
        if (ot1 != nt1) {
            // drawFont4Int2(mainScreen, 4 * 30 + c * 4 * 11, 0, 0, 3, nt1);
            DrawBuff.drawFont8Int2(screen, 8 * 3 - 16 + c * 8 * 11 + 1, 1, 0, 3, nt1);
            ot1 = nt1;
        }
        if (ot2 != nt2) {
            DrawBuff.drawFont8Int2(screen, 8 * 7 - 16 + c * 8 * 11 + 1, 1, 0, 2, nt2);
            // drawFont4Int2(mainScreen, 4 * 34 + c * 4 * 11, 0, 0, 2, nt2);
            ot2 = nt2;
        }
        if (ot3 != nt3) {
            DrawBuff.drawFont8Int2(screen, 8 * 10 - 16 + c * 8 * 11 + 1, 1, 0, 2, nt3);
            // drawFont4Int2(mainScreen, 4 * 37 + c * 4 * 11, 0, 0, 2, nt3);
            ot3 = nt3;
        }
    }

    public static void drawButtonP(FrameBuffer mainScreen, int x, int y, int t, int m) {
        if (mainScreen == null)
            return;

        int n = t % 18;
        t /= 18;
        switch (n) {
        case 0:
            // setting
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 5 * 16, 1 * 16, 16, 16);
            break;
        case 1:
            // stop
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 0 * 16, 0 * 16, 16, 16);
            break;
        case 2:
            // pause
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 1 * 16, 0 * 16, 16, 16);
            break;
        case 3:
            // fadeout
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 4 * 16, 1 * 16, 16, 16);
            break;
        case 4:
            // PREV
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 6 * 16, 1 * 16, 16, 16);
            break;
        case 5:
            // slow
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 2 * 16, 0 * 16, 16, 16);
            break;
        case 6:
            // play
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 3 * 16, 0 * 16, 16, 16);
            break;
        case 7:
            // fast
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 4 * 16, 0 * 16, 16, 16);
            break;
        case 8:
            // NEXT
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 7 * 16, 1 * 16, 16, 16);
            break;
        case 9:
            // loopmode
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 1 * 16 + m * 16, 2 * 16, 16, 16);
            break;
        case 10:
            // folder
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 5 * 16, 0 * 16, 16, 16);
            break;
        case 11:
            // List
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 0 * 16, 2 * 16, 16, 16);
            break;
        case 12:
            // info
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 0 * 16, 1 * 16, 16, 16);
            break;
        case 13:
            // mixer
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 2 * 16, 1 * 16, 16, 16);
            break;
        case 14:
            // panel
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 5 * 16, 2 * 16, 16, 16);
            break;
        case 15:
            // VST List
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 7 * 16, 0 * 16, 16, 16);
            break;
        case 16:
            // MIDI Keyboard
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 3 * 16, 1 * 16, 16, 16);
            break;
        case 17:
            // zoom
            mainScreen.drawByteArray(x, y, rMenuButtons[t], 128, 6 * 16, 2 * 16, 16, 16);
            break;
        }
    }

    public static void drawButton(FrameBuffer mainScreen, int c, int ot, int nt, int om, int nm) {
        if (ot == nt && om == nm) {
            return;
        }

        drawFont8(mainScreen, 17 + c * 16, 9, 0, "  ");
        drawFont8(mainScreen, 17 + c * 16, 17, 0, "  ");
        drawButtonP(mainScreen, 17 + c * 16, 9, nt * 18 + c, nm);

        ot = nt;
        om = nm;
    }

    public static void drawButtons(FrameBuffer mainScreen,
                                   int[] oldButton,
                                   int[] newButton,
                                   int[] oldButtonMode,
                                   int[] newButtonMode) {

        for (int i = 0; i < newButton.length; i++) {
            drawButton(mainScreen, i, oldButton[i], newButton[i], oldButtonMode[i], newButtonMode[i]);
        }

    }

    public static void drawDuty(FrameBuffer screen, int x, int y, int op, int np) {
        if (op == np)
            return;

        screen.drawByteArray(x, y, rNESDMC, 64, np * 8, 0, 8, 8);

        op = np;
    }

    public static void drawNESSw(FrameBuffer screen, int x, int y, boolean os, boolean ns) {
        if (os == ns)
            return;

        screen.drawByteArray(x, y, rNESDMC, 64, (ns ? 1 : 0) * 4 + 32, 0, 4, 8);

        os = ns;
    }

    public static void font4Int1(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Int1(screen, x, y, t, nn);
        on = nn;
    }

    public static void font4Int2(FrameBuffer screen, int x, int y, int t, int k, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Int2(screen, x, y, t, k, nn);
        on = nn;
    }

    public static void font4Int3(FrameBuffer screen, int x, int y, int t, int k, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Int3(screen, x, y, t, k, nn);
        on = nn;
    }

    public static void font4Hex4Bit(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Hex4Bit(screen, x, y, t, nn);
        on = nn;
    }

    public static int font4HexByte(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return on;

        drawFont4HexByte(screen, x, y, t, nn);
        on = nn;
        return on;
    }

    public static void font4Hex12Bit(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Hex12Bit(screen, x, y, t, nn);
        on = nn;
    }

    public static void font4Hex16Bit(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Hex16Bit(screen, x, y, t, nn);
        on = nn;
    }

    public static int font4Hex20Bit(FrameBuffer screen, int x, int y, int t, /* ref */ int on, int nn) {
        if (on == nn)
            return on;

        drawFont4Hex20Bit(screen, x, y, t, nn);
        on = nn;
        return on;
    }

    public static void font4Hex24Bit(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Hex24Bit(screen, x, y, t, nn);
        on = nn;
    }

    public static void font4Hex32Bit(FrameBuffer screen, int x, int y, int t, int on, int nn) {
        if (on == nn)
            return;

        drawFont4Hex32Bit(screen, x, y, t, nn);
        on = nn;
    }

    private static byte[] getByteArray(Image img) {
//        BufferedImage bitmap = new BufferedImage(img);
//        BufferedImageData bitmapData = bitmap
//                .LockBits(new Rectangle(0, 0, bitmap.getWidth(), bitmap.getHeight()), ImageLockMode.readOnly, bitmap.PixelFormat);
//        byte[] byteArray = new byte[bitmapData.Stride * bitmap.getHeight()];
//        System.arraycopy(bitmapData.Scan0, byteArray, 0, byteArray.length);
//        bitmap.UnlockBits(bitmapData);
//        bitmap.dispose();

        return null; // byteArray;
    }

    private static void VolumeP(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null)
            return;
        screen.drawByteArray(x, y, rVol[tp], 32, 2 * t, 0, 2, 8 - (t / 4) * 4);
    }

    public static void drawKbn(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null) {
            return;
        }

        switch (t) {
        case 0:
            screen.drawByteArray(x, y, rKBD[tp], 32, 0, 0, 4, 8);
            break;
        case 1:
            screen.drawByteArray(x, y, rKBD[tp], 32, 4, 0, 3, 8);
            break;
        case 2:
            screen.drawByteArray(x, y, rKBD[tp], 32, 8, 0, 4, 8);
            break;
        case 3:
            screen.drawByteArray(x, y, rKBD[tp], 32, 12, 0, 4, 8);
            break;
        case 4:
            screen.drawByteArray(x, y, rKBD[tp], 32, 0 + 16, 0, 4, 8);
            break;
        case 5:
            screen.drawByteArray(x, y, rKBD[tp], 32, 4 + 16, 0, 3, 8);
            break;
        case 6:
            screen.drawByteArray(x, y, rKBD[tp], 32, 8 + 16, 0, 4, 8);
            break;
        case 7:
            screen.drawByteArray(x, y, rKBD[tp], 32, 12 + 16, 0, 4, 8);
            break;
        }
    }

    private static void ToneNoiseP(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null)
            return;
        screen.drawByteArray(x, y, rPSGMode[tp], 32, 8 * t, 0, 8, 8);
    }

    public static void drawFont8(FrameBuffer screen, int x, int y, int t, String msg) {
        if (screen == null) {
            return;
        }

        for (char c : msg.toCharArray()) {
            int cd = c - 'A' + 0x20 + 1;
            screen.drawByteArray(x, y, rFont1[t], 128, (cd % 16) * 8, (cd / 16) * 8, 8, 8);
            x += 8;
        }
    }

    public static void drawFont8Int(FrameBuffer screen, int x, int y, int t, int k, int num) {
        if (screen == null)
            return;

        int n;
        if (k == 3) {
            boolean f = false;
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            if (n != 0) {
                screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
            }

            n = num / 10;
            num -= n * 10;
            x += 8;
            if (n != 0 || f) {
                screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
            }

            n = num / 1;
            num -= n * 1;
            x += 8;
            screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        if (n != 0) {
            screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
        } else {
            screen.drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
        }

        n = num / 1;
        num -= n * 1;
        x += 8;
        screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
    }

    public static void drawFont8Int2(FrameBuffer screen, int x, int y, int t, int k, int num) {
        if (screen == null)
            return;

        int n;
        if (k == 3) {
            n = num / 100;
            num -= n * 100;

            n = (n > 9) ? 0 : n;
            if (n == 0)
                screen.drawByteArray(x, y, rFont1[t], 128, 0, 0, 8, 8);
            else
                screen.drawByteArray(x, y, rFont1[t], 128, 0, 8, 8, 8);

            n = num / 10;
            num -= n * 10;
            x += 8;
            screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);

            n = num / 1;
            x += 8;
            screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);

        n = num / 1;
        x += 8;
        screen.drawByteArray(x, y, rFont1[t], 128, n * 8, 8, 8, 8);
    }

    public static void drawFont4(FrameBuffer screen, int x, int y, int t, String msg) {
        if (screen == null)
            return;

        for (char c : msg.toCharArray()) {
            int cd = c - 'A' + 0x20 + 1;
            screen.drawByteArray(x, y, rFont2[t], 128, (cd % 32) * 4, (cd / 32) * 8, 4, 8);
            x += 4;
        }
    }

    private static void drawFont4Int(FrameBuffer screen, int x, int y, int t, int k, int num) {
        if (screen == null)
            return;

        int n;
        if (k == 3) {
            boolean f = false;
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            if (n != 0) {
                screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, rFont2[t], 128, 0, 0, 4, 8);
            }

            n = num / 10;
            num -= n * 10;
            x += 4;
            if (n != 0 || f) {
                screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, rFont2[t], 128, 0, 0, 4, 8);
            }

            n = num / 1;
            x += 4;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        if (n != 0) {
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
        } else {
            screen.drawByteArray(x, y, rFont2[t], 128, 0, 0, 4, 8);
        }

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public static void drawFont4IntM(FrameBuffer screen, int x, int y, int k, int num) {
        if (screen == null)
            return;

        int t = 0;
        int n;

        if (num < 0) {
            num = -num;
            screen.drawByteArray(x - 4, y, rFont2[t], 128, 52, 1, 4, 7);
        } else {
            if (num != 0)
                t = 1;
            screen.drawByteArray(x - 4, y, rFont2[t], 128, 24, 1, 4, 7);
        }

        if (k == 3) {
            boolean f = false;
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            if (n != 0) {
                screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 1, 4, 7);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, rFont2[t], 128, 0, 1, 4, 7);
            }

            n = num / 10;
            num -= n * 10;
            x += 4;
            if (n != 0 || f) {
                screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 1, 4, 7);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, rFont2[t], 128, 0, 1, 4, 7);
            }

            n = num / 1;
            x += 4;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 1, 4, 7);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        if (n != 0) {
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 1, 4, 7);
        } else {
            screen.drawByteArray(x, y, rFont2[t], 128, 0, 1, 4, 7);
        }

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 1, 4, 7);
    }

    public static void drawFont4Int1(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        n = num % 10;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public static void drawFont4Int2(FrameBuffer screen, int x, int y, int t, int k, int num) {
        if (screen == null)
            return;

        int n;
        if (k == 3) {
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            screen.drawByteArray(x, y, rFont2[t], 128, (n * 4 + 64), 0, 4, 8);

            n = num / 10;
            num -= n * 10;
            x += 4;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

            n = num / 1;
            x += 4;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public static void drawFont4Int3(FrameBuffer screen, int x, int y, int t, int k, int num) {
        if (screen == null)
            return;

        int n;
        if (k == 3) {
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

            n = num / 10;
            num -= n * 10;
            x += 4;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

            n = num / 1;
            x += 4;
            screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, rFont2[t], 128, n * 4 + 64, 0, 4, 8);
    }

    public static void drawFont4Hex4Bit(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range((byte) num, 0, 15);

        n = num;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4HexByte(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range((byte) num, 0, 255);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4Hex12Bit(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range(num, 0, 0xfff);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4Hex16Bit(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range(num, 0, 0xffff);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4Hex20Bit(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range(num, 0, 0xf_ffff);

        n = num / 0x1_0000;
        num -= n * 0x1_0000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4Hex24Bit(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range(num, 0, 0xff_ffff);

        n = num / 0x10_0000;
        num -= n * 0x10_0000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x1_0000;
        num -= n * 0x1_0000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4Hex32Bit(FrameBuffer screen, int x, int y, int t, int num) {
        if (screen == null)
            return;

        int n;
        num = Common.range(num, 0, 0xffff_ffff);

        n = num / 0x1000_0000;
        num -= n * 0x1000_0000;
        n = (n > 0xf) ? 0 : n;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x100_0000;
        num -= n * 0x100_0000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x10_0000;
        num -= n * 0x10_0000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x1_0000;
        num -= n * 0x1_0000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x1000;
        num -= n * 0x1000;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x100;
        num -= n * 0x100;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 0x10;
        num -= n * 0x10;
        n = (n > 0xf) ? 0 : n;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

        n = num / 1;
        x += 4;
        drawFont4(screen, x, y, t, Tables.hexCh[n]);

    }

    public static void drawFont4V(FrameBuffer screen, int x, int y, int t, String msg) {
        if (screen == null)
            return;

        for (char c : msg.toCharArray()) {
            int cd = c - 'A' + 0x20 + 1;
            screen.drawByteArray(x, y, rFont3[t], 128, (cd % 16) * 8, (cd / 16) * 4, 8, 4);
            y -= 4;
        }
    }

    private static void drawEtypeP(FrameBuffer screen, int x, int y, int t) {
        if (screen == null)
            return;
        screen.drawByteArray(x, y, rPSGEnv, 128, 8 * t, 0, 8, 8);
        drawFont4Int2(screen, x + 12, y, 0, 2, t);
    }

    public static void drawPanP(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null)
            return;
        screen.drawByteArray(x, y, rPan[tp], 32, 8 * t, 0, 8, 8);
    }

    public static void drawPanType2P(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null) {
            return;
        }

        int p = (t & 0x0f);
        p = p == 0 ? 0 : (1 + p / 4);
        screen.drawByteArray(x, y, rPan2[tp], 32, p * 4, 0, 4, 8);
        p = ((t & 0xf0) >> 4);
        p = p == 0 ? 0 : (1 + p / 4);
        screen.drawByteArray(x + 4, y, rPan2[tp], 32, p * 4, 0, 4, 8);

    }

    public static void drawPanType3P(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null) {
            return;
        }

        int p = (t & 0x0f);
        p = p == 0 ? 0 : ((p + 1) / 4);
        screen.drawByteArray(x, y, rPan2[tp], 32, p * 4, 0, 4, 8);
        p = ((t & 0xf0) >> 4);
        p = p == 0 ? 0 : ((p + 1) / 4);
        screen.drawByteArray(x + 4, y, rPan2[tp], 32, p * 4, 0, 4, 8);

    }

    private static void drawMIDILCD_FaderP(FrameBuffer screen, int MIDImodule, int faderType, int x, int y, int value) {
        screen.drawByteArray(x, y, rMIDILCD_Fader[MIDImodule], 64, value * 4, faderType * 16, 4, 16);
    }

    private static void drawMIDILCD_KbdP(FrameBuffer screen, int x, int y, int note, int vel) {
        screen.drawByteArrayTransp(x + Tables.kbdl[note % 12] + note / 12 * 28,
                y,
                rMIDILCD_KBD,
                16,
                Tables.kbl2[note % 12],
                vel / 16 * 8,
                4,
                8);
    }

    private static void ChAY8910_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    private static void ChS5B_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    public static void ChC140_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        // if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 +
        // ch).toString());
        // else
        drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
    }

    public static void ChQSound_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        // if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 +
        // ch).toString());
        // else
        drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
    }

    public static void ChQSoundAdpcm_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 88, 0, 20, 8);
        // if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 +
        // ch).toString());
        // else
        drawFont4(screen, x + 20, y, mask ? 1 : 0, String.format("%1d", 1 + ch));
    }

    public static void ChC352_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        // if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 +
        // ch).toString());
        // else
        drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
    }

    private static void ChHuC6280_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 112, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    private static void ChOKIM6295_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 64, 0, 24, 8);
        drawFont8(screen, x + 24, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    private static void ChK051649_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 112, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    private static void ChRF5C164_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(ch + 1));
    }

    private static void ChOKIM6258_P(FrameBuffer screen, int x, int y, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 8 * 8, 0, 24, 8);
    }

    public static void ChSegaPCM_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        // if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 +
        // ch).toString());
        // else
        drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
    }

    private static void ChSN76489_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    public static void ChYM2151_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
        drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

    private static void ChYM2203_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 3) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else if (ch < 6) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch - 3));
        } else if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32 * (ch - 5), 24, 32, 8);
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 24, 8);
        }
    }

    private static void ChYM2413_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else {
            switch (ch) {
            case 9:
                drawFont4(screen, (ch - 9) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "BD");
                break;
            case 10:
                drawFont4(screen, (ch - 9) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "SD");
                break;
            case 11:
                drawFont4(screen, (ch - 9) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "TM");
                break;
            case 12:
                drawFont4(screen, (ch - 9) * 4 * 15 + 3 * 4, y, mask ? 1 : 0, "CYM");// 3
                // character
                break;
            case 13:
                drawFont4(screen, (ch - 9) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "HH");
                break;
            }
        }
    }

    private static void ChYM3526_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else if (ch < 14) {
            switch (ch) {
            case 9:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "BD");
                break;
            case 10:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "SD");
                break;
            case 11:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "TM");
                break;
            case 12:
                drawFont4(screen, (ch - 9) * 4 * 15 + 0 * 4, y, mask ? 1 : 0, "CYM");// 3
                // character
                break;
            case 13:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "HH");
                break;
            }
        }
    }

    private static void ChY8950_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else if (ch < 14) {
            switch (ch) {
            case 9:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "BD");
                break;
            case 10:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "SD");
                break;
            case 11:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "TM");
                break;
            case 12:
                drawFont4(screen, (ch - 9) * 4 * 15 + 0 * 4, y, mask ? 1 : 0, "CYM");// 3
                // character
                break;
            case 13:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "HH");
                break;
            }
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 64, 0, 24, 8);
        }
    }

    private static void ChYM3812_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else if (ch < 14) {
            switch (ch) {
            case 9:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "BD");
                break;
            case 10:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "SD");
                break;
            case 11:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "TM");
                break;
            case 12:
                drawFont4(screen, (ch - 9) * 4 * 15 + 0 * 4, y, mask ? 1 : 0, "CYM");// 3
                // character
                break;
            case 13:
                drawFont4(screen, (ch - 9) * 4 * 15 + 1 * 4, y, mask ? 1 : 0, "HH");
                break;
            }
        }
    }

    private static void ChYMF262_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 18) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            // if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 +
            // ch).toString());
            // else
            drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
        } else if (ch < 23) {
            switch (ch) {
            case 18:
                drawFont4(screen, (ch - 18) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "BD");
                break;
            case 19:
                drawFont4(screen, (ch - 18) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "SD");
                break;
            case 20:
                drawFont4(screen, (ch - 18) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "TM");
                break;
            case 21:
                drawFont4(screen, (ch - 18) * 4 * 15 + 3 * 4, y, mask ? 1 : 0, "CYM");// 3
                // character
                break;
            case 22:
                drawFont4(screen, (ch - 18) * 4 * 15 + 4 * 4, y, mask ? 1 : 0, "HH");
                break;
            }
        }
    }

    private static void ChYMF278B_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 18) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
        } else if (ch < 23) {
            switch (ch) {
            case 18:
                drawFont4(screen, (ch - 18) * 4 * 13 + 10 * 4, y, mask ? 1 : 0, "BD");
                break;
            case 19:
                drawFont4(screen, (ch - 18) * 4 * 13 + 10 * 4, y, mask ? 1 : 0, "SD");
                break;
            case 20:
                drawFont4(screen, (ch - 18) * 4 * 13 + 10 * 4, y, mask ? 1 : 0, "TM");
                break;
            case 21:
                drawFont4(screen, (ch - 18) * 4 * 13 + 9 * 4, y, mask ? 1 : 0, "CYM");// 3
                // character
                break;
            case 22:
                drawFont4(screen, (ch - 18) * 4 * 13 + 10 * 4, y, mask ? 1 : 0, "HH");
                break;
            }
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
            ch -= 23;
            drawFont4(screen, x + 16, y, mask ? 1 : 0, String.format("%2d", 1 + ch));
        }
    }

    private static void ChYM2608_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 6) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch - 6));
        } else if (ch < 12) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32 * (ch - 8), 24, 32, 8);
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 64, 0, 24, 8);
        }
    }

    private static void ChYM2608Rhythm_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        drawFont4(screen, x + 0 * 4, y, mask ? 1 : 0, "B");
        drawFont4(screen, x + 15 * 4, y, mask ? 1 : 0, "s");
        drawFont4(screen, x + 30 * 4, y, mask ? 1 : 0, "C");
        drawFont4(screen, x + 45 * 4, y, mask ? 1 : 0, "H");
        drawFont4(screen, x + 60 * 4, y, mask ? 1 : 0, "T");
        drawFont4(screen, x + 75 * 4, y, mask ? 1 : 0, "R");
    }

    public static void ChYM2610_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        if (ch < 6) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
        } else if (ch < 9) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch - 6));
        } else if (ch < 12) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32 * (ch - 8), 24, 32, 8);
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 88, 0, 24, 8);
        }
    }

    private static void ChYM2610Rhythm_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        drawFont4(screen, x + 0 * 4, y, mask ? 1 : 0, "A1");
        drawFont4(screen, x + 14 * 4, y, mask ? 1 : 0, "2");
        drawFont4(screen, x + 27 * 4, y, mask ? 1 : 0, "3");
        drawFont4(screen, x + 40 * 4, y, mask ? 1 : 0, "4");
        drawFont4(screen, x + 53 * 4, y, mask ? 1 : 0, "5");
        drawFont4(screen, x + 66 * 4, y, mask ? 1 : 0, "6");
    }

    private static void Ch6YM2612_P(FrameBuffer screen, int x, int y, int m, boolean mask, int tp) {
        if (m == 0) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, "6");
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
            drawFont8(screen, x + 16, y, 0, " ");
        }
    }

    private static void ChYM2612_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (ch == 5) {
            return;
        }

        if (ch < 5) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(ch + 1));
        } else if (ch < 10) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 32 * (ch - 5), 24, 32, 8);
        }
    }

    private static void Ch3YM2612_P(FrameBuffer screen, int x, int y, int ch, boolean mask, boolean ex, int tp) {
        if (!ex) {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, String.valueOf(ch + 1));
        } else {
            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 24, 24, 8);
        }
    }

    private static void Ch6YM2612XGM_P(FrameBuffer screen, int x, int y, int m, boolean mask, int tp) {
        if (m == 0) {
            // FM mode

            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 0, 16, 8);
            drawFont8(screen, x + 16, y, mask ? 1 : 0, "6");
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                drawKbn(screen, 33 + kx, y, kt, tp);
            }
        } else {
            // PCM mode

            screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
            drawFont8(screen, x + 16, y, 0, " ");
            drawFont4(screen, x + 32, y, 0, " 1C00             2C00             3C00             4C00                ");
        }
    }

    private static void ChNESDMC_P(FrameBuffer screen, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        switch (ch) {
        case 0:
            screen.drawByteArray(0, 8, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 8, mask ? 1 : 0, "1");
            break;
        case 1:
            screen.drawByteArray(0, 24, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 24, mask ? 1 : 0, "2");
            break;
        case 2:
            screen.drawByteArray(0, 40, rType[tp * 2 + (mask ? 1 : 0)], 128, 64, 8, 32, 8);
            break;
        case 3:
            screen.drawByteArray(112, 32, rType[tp * 2 + (mask ? 1 : 0)], 128, 96, 8, 24, 8);
            break;
        case 4:
            screen.drawByteArray(112, 48, rType[tp * 2 + (mask ? 1 : 0)], 128, 0, 16, 16, 8);
            break;
        }
    }

    private static void ChFDS_P(FrameBuffer screen, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(0, 8, rType[tp * 2 + (mask ? 1 : 0)], 128, 14 * 8, 0 * 8, 16, 8);
    }

    private static void ChMMC5_P(FrameBuffer screen, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        switch (ch) {
        case 0:
            screen.drawByteArray(0, 8, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 8, mask ? 1 : 0, "1");
            break;
        case 1:
            screen.drawByteArray(0, 24, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 24, mask ? 1 : 0, "2");
            break;
        case 2:
            screen.drawByteArray(112, 32, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
            break;
        }
    }

    private static void ChDMG_P(FrameBuffer screen, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        switch (ch) {
        case 0:
            screen.drawByteArray(0, 8, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 8, mask ? 1 : 0, "1");
            break;
        case 1:
            screen.drawByteArray(0, 16, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 16, mask ? 1 : 0, "2");
            break;
        case 2:
            screen.drawByteArray(0, 24, rType[tp * 2 + (mask ? 1 : 0)], 128, 112, 0, 16, 8);
            break;
        case 3:
            screen.drawByteArray(0, 32, rType[tp * 2 + (mask ? 1 : 0)], 128, 96, 8, 24, 8);
            break;
        }
    }

    private static void ChVRC6_P(FrameBuffer screen, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        switch (ch) {
        case 0:
            screen.drawByteArray(0, 8, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 8, mask ? 1 : 0, "1");
            break;
        case 1:
            screen.drawByteArray(0, 24, rType[tp * 2 + (mask ? 1 : 0)], 128, 48, 8, 16, 8);
            drawFont8(screen, 16, 24, mask ? 1 : 0, "2");
            break;
        case 2:
            screen.drawByteArray(0, 40, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 16, 16, 8);
            break;
        }
    }

    private static void ChN163_P(FrameBuffer screen, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(0, ch * 8 * 3 + 8, rType[tp * 2 + (mask ? 1 : 0)], 128, 112, 0, 16, 8);
        drawFont8(screen, 16, ch * 8 * 3 + 8, mask ? 1 : 0, String.valueOf(ch + 1));
    }

    private static void drawFaderSlitP(FrameBuffer screen, int x, int y) {
        screen.drawByteArray(x, y, rFader, 32, 16, 0, 8, 8);
        screen.drawByteArray(x, y + 8, rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 16, rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 24, rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 32, rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 40, rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 48, rFader, 32, 24, 0, 8, 8);
    }

    private static void drawFaderP(FrameBuffer screen, int x, int y, int t) {
        screen.drawByteArray(x, y, rFader, 32, t == 0 ? 0 : 8, 0, 8, 13);
    }

    private static void drawMixerVolumeP(FrameBuffer screen, int x, int y, int t) {
        screen.drawByteArray(x, y, rFader, 32, 24, 8 + t, 2, 1);
    }

    private static void drawTnP(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null)
            return;
        screen.drawByteArray(x, y, rPSGMode[tp], 32, 8 * t, 0, 8, 8);
    }

    private static void drawToneFormatP(FrameBuffer screen, int x, int y, int toneFormat) {
        screen.drawByteArray(x, y, rMenuButtons[1], 128, (toneFormat % 3) * 5 * 8, (6 + toneFormat / 3) * 8, 40, 8);
    }

    private static void drawChipNameP(FrameBuffer screen, int x, int y, int t, int c) {
        if (screen == null) {
            return;
        }

        screen.drawByteArray(x, y, rChipName[c], 128, (t % 8) * 16, (t / 8) * 8, 8 * 2, 8);
    }
}
