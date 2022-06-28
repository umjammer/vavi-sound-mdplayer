package mdplayer.form.kb.opl;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmYMF262 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YMF262 newParam = null;
    private MDChipParams.YMF262 oldParam = null;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYMF262.class);

    public frmYMF262(frmMain frm, int chipID, int zoom, MDChipParams.YMF262 newParam, MDChipParams.YMF262 oldParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getplaneYMF262(), null, zoom);
        boolean YMF262Type = (chipID == 0)
                ? parent.setting.getYMF262Type()[0].getUseReal()[0]
                : parent.setting.getYMF262Type()[1].getUseReal()[0];
        int YMF262SoundLocation = (chipID == 0)
                ? parent.setting.getYMF262Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYMF262Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YMF262Type ? 0 : (YMF262SoundLocation < 0 ? 2 : 1);
        DrawBuff.screenInitYMF262(frameBuffer, tp);
        update();
    }

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosYmf262()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYmf262()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
            }
            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent e) {
            setLocation(new Point(x, y));

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneYMF262().getWidth() * zoom, frameSizeH + Resources.getplaneYMF262().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneYMF262().getWidth() * zoom, frameSizeH + Resources.getplaneYMF262().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneYMF262().getWidth() * zoom, frameSizeH + Resources.getplaneYMF262().getHeight() * zoom));
        componentListener.componentResized(null);
    }

    private ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentMoved(ComponentEvent e) {
            prefs.putInt("x", e.getComponent().getX());
            prefs.putInt("y", e.getComponent().getY());
        }
        @Override
        public void componentResized(ComponentEvent e) {
        }
    };

    private static final int[] slot1Tbl = new int[] {0, 6, 1, 7, 2, 8, 12, 13, 14, 18, 24, 19, 25, 20, 26, 30, 31, 32};
    private static final int[] slot2Tbl = new int[] {3, 9, 4, 10, 5, 11, 15, 16, 17, 21, 27, 22, 28, 23, 29, 33, 34, 35};
    private static final int[] chTbl = new int[] {0, 3, 1, 4, 2, 5, 6, 7, 8};

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
        }
        boolean YMF262Type = (chipID == 0)
                ? parent.setting.getYMF262Type()[0].getUseReal()[0]
                : parent.setting.getYMF262Type()[1].getUseReal()[0];
        int YMF262SoundLocation = (chipID == 0)
                ? parent.setting.getYMF262Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYMF262Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YMF262Type ? 0 : (YMF262SoundLocation < 0 ? 2 : 1);
        DrawBuff.screenInitYMF262(frameBuffer, tp);
    }

    public void screenChangeParams() {
        int[][] ymf262Register = Audio.getYMF262Register(chipID);
        MDChipParams.Channel nyc;
        int slot = 0;
        int slotP = 0;

        //FM
        for (int c = 0; c < 18; c++) {
            nyc = newParam.channels[c];
            for (int i = 0; i < 2; i++) {

                if (i == 0) {
                    slot = slot1Tbl[c] % 18;
                    slotP = slot1Tbl[c] / 18;
                } else {
                    slot = slot2Tbl[c] % 18;
                    slotP = slot2Tbl[c] / 18;
                }
                slot = (slot % 6) + 8 * (slot / 6);

                //AR
                nyc.inst[0 + i * 17] = ymf262Register[slotP][0x60 + slot] >> 4;
                //DR
                nyc.inst[1 + i * 17] = ymf262Register[slotP][0x60 + slot] & 0xf;
                //SL
                nyc.inst[2 + i * 17] = ymf262Register[slotP][0x80 + slot] >> 4;
                //RR
                nyc.inst[3 + i * 17] = ymf262Register[slotP][0x80 + slot] & 0xf;
                //KL
                nyc.inst[4 + i * 17] = ymf262Register[slotP][0x40 + slot] >> 6;
                //TL
                nyc.inst[5 + i * 17] = ymf262Register[slotP][0x40 + slot] & 0x3f;
                //MT
                nyc.inst[6 + i * 17] = ymf262Register[slotP][0x20 + slot] & 0xf;
                //AM
                nyc.inst[7 + i * 17] = ymf262Register[slotP][0x20 + slot] >> 7;
                //VB
                nyc.inst[8 + i * 17] = (ymf262Register[slotP][0x20 + slot] >> 6) & 1;
                //EG
                nyc.inst[9 + i * 17] = (ymf262Register[slotP][0x20 + slot] >> 5) & 1;
                //KR
                nyc.inst[10 + i * 17] = (ymf262Register[slotP][0x20 + slot] >> 4) & 1;
                //WS
                nyc.inst[13 + i * 17] = (ymf262Register[slotP][0xe0 + slot] & 7);
            }
        }

        newParam.channels[18].dda = ((ymf262Register[1][0xbd] >> 7) & 0x01) != 0; // DA
        newParam.channels[19].dda = ((ymf262Register[1][0xbd] >> 6) & 0x01) != 0; // DV

        // ConnectSelect
        for (int c = 0; c < 6; c++) {
            newParam.channels[c].dda = (ymf262Register[1][0x04] & (0x1 << c)) != 0;
            newParam.channels[c].inst[34] = newParam.channels[c].dda ? 1 : 0; // [
            newParam.channels[c].inst[35] = newParam.channels[c].dda ? 2 : 0; // ]
            if (newParam.channels[c].dda) {
                // OP4 mode
                int ch = (c < 3) ? c * 2 : ((c - 3) * 2 + 9);
                // cnt=14
                int a = newParam.channels[ch].inst[14] * 2 + newParam.channels[ch].inst[14 + 17];
                // mod=16
                switch (a) {
                case 0:
                    newParam.channels[ch].inst[16] = 0;
                    newParam.channels[ch].inst[16 + 17] = 0;
                    newParam.channels[ch + 1].inst[16] = 0;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                case 1:
                    newParam.channels[ch].inst[16] = 0;
                    newParam.channels[ch].inst[16 + 17] = 1;
                    newParam.channels[ch + 1].inst[16] = 0;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                case 2:
                    newParam.channels[ch].inst[16] = 1;
                    newParam.channels[ch].inst[16 + 17] = 0;
                    newParam.channels[ch + 1].inst[16] = 0;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                case 3:
                    newParam.channels[ch].inst[16] = 1;
                    newParam.channels[ch].inst[16 + 17] = 0;
                    newParam.channels[ch + 1].inst[16] = 1;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                }
            }
        }

        int ko = Audio.getYMF262FMKeyON(chipID);

        for (int c = 0; c < 18; c++) {
            nyc = newParam.channels[c];

            int p = c / 9;
            boolean isOp4 = false;
            int adr = c % 9;
            if (adr < 6) {
                if (newParam.channels[(adr / 2) + p * 3].dda) isOp4 = true;
            }
            int kadr = isOp4 ? (adr / 2) : adr;
            adr = chTbl[adr];

            //BL
            nyc.inst[11] = (ymf262Register[p][0xb0 + adr] >> 2) & 7;
            //FNUM
            nyc.inst[12] = ymf262Register[p][0xa0 + adr]
                    + ((ymf262Register[p][0xb0 + adr] & 3) << 8);

            //FB
            nyc.inst[15] = (ymf262Register[p][0xc0 + adr] >> 1) & 7;
            //CN
            nyc.inst[14] = (ymf262Register[p][0xc0 + adr] & 1);
            //PAN
            nyc.inst[36] = ymf262Register[p][0xc0 + adr] & 0x30;
            nyc.inst[36] = ((nyc.inst[36] >> 5) & 1) | ((nyc.inst[36] >> 3) & 2); //00RL0000 -> 000000LR
            //modFlg
            int n = ymf262Register[p][0xc0 + adr] & 1;
            nyc.inst[16] = n == 0 ? 0 : 1;
            nyc.inst[33] = 1;

            int nt = Common.searchSegaPCMNote(nyc.inst[12] / 344.0) + (nyc.inst[11] - 4) * 12;
            if ((ko & (1 << (adr + p * 9))) != 0) {
                if (nyc.note != nt) {
                    nyc.note = nt;
                    int tl1 = nyc.inst[5 + 0 * 17];
                    int tl2 = nyc.inst[5 + 1 * 17];
                    int tl = tl2;
                    if (n != 0) {
                        tl = Math.min(tl1, tl2);
                    }
                    nyc.volumeL = (nyc.inst[36] & 2) != 0 ? (19 * (64 - tl) / 64) : 0;
                    nyc.volumeR = (nyc.inst[36] & 1) != 0 ? (19 * (64 - tl) / 64) : 0;
                } else {
                    nyc.volumeL--;
                    if (nyc.volumeL < 0) nyc.volumeL = 0;
                    nyc.volumeR--;
                    if (nyc.volumeR < 0) nyc.volumeR = 0;
                }
            } else {
                nyc.note = -1;
                nyc.volumeL--;
                if (nyc.volumeL < 0) {
                    nyc.volumeL = 0;
                }
                nyc.volumeR--;
                if (nyc.volumeR < 0) {
                    nyc.volumeR = 0;
                }
            }


        }

        // // #region リズム情報の取得

        int r = Audio.getYMF262RyhthmKeyON(chipID);

        //slot14 TL 0x51 HH
        //slot15 TL 0x52 TOM
        //slot16 TL 0x53 BD
        //slot17 TL 0x54 SD
        //slot18 TL 0x55 CYM

        //BD
        if ((r & 0x10) != 0) {
            newParam.channels[18].volume = 19 - ((ymf262Register[0][0x53] & 0x3f) >> 2);
        } else {
            newParam.channels[18].volume--;
            if (newParam.channels[18].volume < 0) newParam.channels[18].volume = 0;
        }

        //SD
        if ((r & 0x08) != 0) {
            newParam.channels[19].volume = 19 - ((ymf262Register[0][0x54] & 0x3f) >> 2);
        } else {
            newParam.channels[19].volume--;
            if (newParam.channels[19].volume < 0) newParam.channels[19].volume = 0;
        }

        //TOM
        if ((r & 0x04) != 0) {
            newParam.channels[20].volume = 19 - ((ymf262Register[0][0x52] & 0x3f) >> 2);
        } else {
            newParam.channels[20].volume--;
            if (newParam.channels[20].volume < 0) newParam.channels[20].volume = 0;
        }

        //CYM
        if ((r & 0x02) != 0) {
            newParam.channels[21].volume = 19 - ((ymf262Register[0][0x55] & 0x3f) >> 2);
        } else {
            newParam.channels[21].volume--;
            if (newParam.channels[21].volume < 0) newParam.channels[21].volume = 0;
        }

        //HH
        if ((r & 0x01) != 0) {
            newParam.channels[22].volume = 19 - ((ymf262Register[0][0x51] & 0x3f) >> 2);
        } else {
            newParam.channels[22].volume--;
            if (newParam.channels[22].volume < 0) newParam.channels[22].volume = 0;
        }

        //Audio.resetYMF278BRyhthmKeyON(chipID);

        // // #endregion

    }

    public void screenDrawParams() {
        boolean ChipType2 = (chipID == 0)
                ? parent.setting.getYMF262Type()[0].getUseReal()[0]
                : parent.setting.getYMF262Type()[1].getUseReal()[0];
        int chipSoundLocation = (chipID == 0)
                ? parent.setting.getYMF262Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYMF262Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !ChipType2 ? 0 : (chipSoundLocation < 0 ? 2 : 1);
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        //FM
        for (int c = 0; c < 18; c++) {

            oyc = oldParam.channels[c];
            nyc = newParam.channels[c];

            for (int i = 0; i < 2; i++) {
                DrawBuff.susFlag(frameBuffer, 2 + i * 33, c * 2 + 42, 1, oyc.inst[16 + i * 17], nyc.inst[16 + i * 17]);
                DrawBuff.font4Int2(frameBuffer, 16 + 4 + i * 132, c * 8 + 168, 0, 0, oyc.inst[0 + i * 17], nyc.inst[0 + i * 17]);//AR
                DrawBuff.font4Int2(frameBuffer, 16 + 12 + i * 132, c * 8 + 168, 0, 0, oyc.inst[1 + i * 17], nyc.inst[1 + i * 17]);//DR
                DrawBuff.font4Int2(frameBuffer, 16 + 20 + i * 132, c * 8 + 168, 0, 0, oyc.inst[2 + i * 17], nyc.inst[2 + i * 17]);//SL
                DrawBuff.font4Int2(frameBuffer, 16 + 28 + i * 132, c * 8 + 168, 0, 0, oyc.inst[3 + i * 17], nyc.inst[3 + i * 17]);//RR

                DrawBuff.font4Int2(frameBuffer, 16 + 40 + i * 132, c * 8 + 168, 0, 0, oyc.inst[4 + i * 17], nyc.inst[4 + i * 17]);//KL
                DrawBuff.font4Int2(frameBuffer, 16 + 48 + i * 132, c * 8 + 168, 0, 0, oyc.inst[5 + i * 17], nyc.inst[5 + i * 17]);//TL

                DrawBuff.font4Int2(frameBuffer, 16 + 60 + i * 132, c * 8 + 168, 0, 0, oyc.inst[6 + i * 17], nyc.inst[6 + i * 17]);//MT

                DrawBuff.font4Int2(frameBuffer, 16 + 72 + i * 132, c * 8 + 168, 0, 0, oyc.inst[7 + i * 17], nyc.inst[7 + i * 17]);//AM
                DrawBuff.font4Int2(frameBuffer, 16 + 80 + i * 132, c * 8 + 168, 0, 0, oyc.inst[8 + i * 17], nyc.inst[8 + i * 17]);//VB
                DrawBuff.font4Int2(frameBuffer, 16 + 88 + i * 132, c * 8 + 168, 0, 0, oyc.inst[9 + i * 17], nyc.inst[9 + i * 17]);//EG
                DrawBuff.font4Int2(frameBuffer, 16 + 96 + i * 132, c * 8 + 168, 0, 0, oyc.inst[10 + i * 17], nyc.inst[10 + i * 17]);//KR
                DrawBuff.font4Int2(frameBuffer, 16 + 108 + i * 132, c * 8 + 168, 0, 0, oyc.inst[13 + i * 17], nyc.inst[13 + i * 17]);//WS
            }

            DrawBuff.font4Int2(frameBuffer, 16 + 4 * 64, c * 8 + 168, 0, 0, oyc.inst[11], nyc.inst[11]);//BL
            DrawBuff.font4Hex12Bit(frameBuffer, 16 + 4 * 68, c * 8 + 168, 0, oyc.inst[12], nyc.inst[12]);//F-Num
            DrawBuff.font4Int2(frameBuffer, 16 + 4 * 72, c * 8 + 168, 0, 0, oyc.inst[14], nyc.inst[14]);//CN
            DrawBuff.font4Int2(frameBuffer, 16 + 4 * 75, c * 8 + 168, 0, 0, oyc.inst[15], nyc.inst[15]);//FB
            int dmy = 99;
            DrawBuff.Pan(frameBuffer, 24, 8 + c * 8, oyc.inst[36], nyc.inst[36], dmy, 0);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
            DrawBuff.VolumeXY(frameBuffer, 64, c * 2 + 2, 1, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.VolumeXY(frameBuffer, 64, c * 2 + 3, 1, oyc.volumeR, nyc.volumeR, tp);
            DrawBuff.ChYMF262(frameBuffer, c, oyc.mask, nyc.mask, tp);

        }

        for (int c = 0; c < 6; c++) {
            //CS
            DrawBuff.drawNESSw(frameBuffer, 4 * 4 + c * 4, 39 * 8, oldParam.channels[c].dda, newParam.channels[c].dda);
            int ch = (c < 3) ? c * 2 : ((c - 3) * 2 + 9);
            DrawBuff.Kakko(frameBuffer, 4 * 0, (c < 3 ? 0 : 24) + c * 16 + 168, 0, oldParam.channels[c].inst[34], newParam.channels[c].inst[34]);
            DrawBuff.Kakko(frameBuffer, 4 * 163, (c < 3 ? 0 : 24) + c * 16 + 168, 0, oldParam.channels[c].inst[35], newParam.channels[c].inst[35]);
        }
        DrawBuff.drawNESSw(frameBuffer, 13 * 4, 39 * 8, oldParam.channels[18].dda, newParam.channels[18].dda);//DA
        DrawBuff.drawNESSw(frameBuffer, 17 * 4, 39 * 8, oldParam.channels[19].dda, newParam.channels[19].dda);//DV

        for (int c = 18; c < 23; c++) {
            DrawBuff.ChYMF262(frameBuffer, c, oldParam.channels[c].mask, newParam.channels[c].mask, tp);
            DrawBuff.VolumeXY(frameBuffer, 6 + (c - 18) * 15, 19 * 2, 0, oldParam.channels[c].volume, newParam.channels[c].volume, tp);
        }

    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;
            int ch;
            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (ch = 0; ch < 23; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.YMF262, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YMF262, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤 FM & RHM
            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch > 18) {
                if (ch >= 20 && ch < 38) {
                    //音色欄をクリック
                    //クリップボードに音色をコピーする
                    if (ev.getButton() == MouseEvent.BUTTON1)
                        parent.getInstCh(EnmChip.YMF262, ch - 20, chipID);
                }
                return;
            }

            if (ch == 18) {
                int x = (px / 4 - 4);
                if (x < 0) return;
                x /= 15;
                if (x > 4) return;
                ch += x;
            }

            if (ch > 22) return;

            if (ev.getButton() == MouseEvent.BUTTON1) {
                //マスク
                parent.setChannelMask(EnmChip.YMF262, chipID, ch);
                return;
            }

            //マスク解除
            for (ch = 0; ch < 18 + 5; ch++) parent.resetChannelMask(EnmChip.YMF262, chipID, ch);
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYMF262));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneYMF262();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(328, 320));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYMF262
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(328, 320));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYMF262");
        this.setTitle("YMF262");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
