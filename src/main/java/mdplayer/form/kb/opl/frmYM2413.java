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
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmYM2413 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YM2413 newParam;
    private MDChipParams.YM2413 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYM2413.class);

    public frmYM2413(frmMain frm, int chipID, int zoom, MDChipParams.YM2413 newParam, MDChipParams.YM2413 oldParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getplaneYM2413(), null, zoom);

        boolean YM2413Type = (chipID == 0)
                ? parent.setting.getYM2413Type()[0].getUseReal()[0]
                : parent.setting.getYM2413Type()[1].getUseReal()[0];
        int YM2413SoundLocation = (chipID == 0)
                ? parent.setting.getYM2413Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2413Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2413Type ? 0 : (YM2413SoundLocation < 0 ? 2 : 1);

        screenInitYM2413(frameBuffer, tp);
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
                parent.setting.getLocation().getPosYm2413()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYm2413()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneYM2413().getWidth() * zoom, frameSizeH + Resources.getplaneYM2413().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneYM2413().getWidth() * zoom, frameSizeH + Resources.getplaneYM2413().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneYM2413().getWidth() * zoom, frameSizeH + Resources.getplaneYM2413().getHeight() * zoom));
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

    public void screenChangeParams() {
        int[] ym2413Register = Audio.getYM2413Register(chipID);
        MDChipParams.Channel nyc;
        mdplayer.ChipRegister.ChipKeyInfo ki = Audio.getYM2413KeyInfo(chipID);

        for (int ch = 0; ch < 9; ch++) {
            nyc = newParam.channels[ch];

            nyc.inst[0] = (ym2413Register[0x30 + ch] & 0xf0) >> 4;
            nyc.inst[1] = (ym2413Register[0x20 + ch] & 0x20) >> 5;
            nyc.inst[2] = (ym2413Register[0x20 + ch] & 0x10) >> 4;
            nyc.inst[3] = (ym2413Register[0x30 + ch] & 0x0f);

            int freq = ym2413Register[0x10 + ch] + ((ym2413Register[0x20 + ch] & 0x1) << 8);
            int oct = ((ym2413Register[0x20 + ch] & 0xe) >> 1);

            nyc.note = Common.searchSegaPCMNote(freq / 172.0) + (oct - 4) * 12;

            if (ki.On[ch]) {
                nyc.volumeL = (19 - nyc.inst[3]);
            } else {
                if (nyc.inst[2] == 0) nyc.note = -1;
                nyc.volumeL--;
                if (nyc.volumeL < 0) nyc.volumeL = 0;
            }

        }

        //int r = Audio.getYM2413RyhthmKeyON(chipID);

        //BD
        if (ki.On[9]) {
            newParam.channels[9].volume = (19 - (ym2413Register[0x36] & 0x0f));
        } else {
            newParam.channels[9].volume--;
            if (newParam.channels[9].volume < 0) newParam.channels[9].volume = 0;
        }

        //SD
        if (ki.On[10]) {
            newParam.channels[10].volume = (19 - (ym2413Register[0x37] & 0x0f));
        } else {
            newParam.channels[10].volume--;
            if (newParam.channels[10].volume < 0) newParam.channels[10].volume = 0;
        }

        //TOM
        if (ki.On[11]) {
            newParam.channels[11].volume = 19 - ((ym2413Register[0x38] & 0xf0) >> 4);
        } else {
            newParam.channels[11].volume--;
            if (newParam.channels[11].volume < 0) newParam.channels[11].volume = 0;
        }

        //CYM
        if (ki.On[12]) {
            newParam.channels[12].volume = 19 - (ym2413Register[0x38] & 0x0f);
        } else {
            newParam.channels[12].volume--;
            if (newParam.channels[12].volume < 0) newParam.channels[12].volume = 0;
        }

        //HH
        if (ki.On[13]) {
            newParam.channels[13].volume = 19 - ((ym2413Register[0x37] & 0xf0) >> 4);
        } else {
            newParam.channels[13].volume--;
            if (newParam.channels[13].volume < 0) newParam.channels[13].volume = 0;
        }


        newParam.channels[0].inst[4] = (ym2413Register[0x02] & 0x3f);//TL
        newParam.channels[0].inst[5] = (ym2413Register[0x03] & 0x07);//FB

        newParam.channels[0].inst[6] = (ym2413Register[0x04] & 0xf0) >> 4;//AR
        newParam.channels[0].inst[7] = (ym2413Register[0x04] & 0x0f);//DR
        newParam.channels[0].inst[8] = (ym2413Register[0x06] & 0xf0) >> 4;//SL
        newParam.channels[0].inst[9] = (ym2413Register[0x06] & 0x0f);//RR
        newParam.channels[0].inst[10] = (ym2413Register[0x02] & 0x80) >> 7;//KL
        newParam.channels[0].inst[11] = (ym2413Register[0x00] & 0x0f);//MT
        newParam.channels[0].inst[12] = (ym2413Register[0x00] & 0x80) >> 7;//AM
        newParam.channels[0].inst[13] = (ym2413Register[0x00] & 0x40) >> 6;//VB
        newParam.channels[0].inst[14] = (ym2413Register[0x00] & 0x20) >> 5;//EG
        newParam.channels[0].inst[15] = (ym2413Register[0x00] & 0x10) >> 4;//KR
        newParam.channels[0].inst[16] = (ym2413Register[0x03] & 0x08) >> 3;//DM
        newParam.channels[0].inst[17] = (ym2413Register[0x05] & 0xf0) >> 4;//AR
        newParam.channels[0].inst[18] = (ym2413Register[0x05] & 0x0f);//DR
        newParam.channels[0].inst[19] = (ym2413Register[0x07] & 0xf0) >> 4;//SL
        newParam.channels[0].inst[20] = (ym2413Register[0x07] & 0x0f);//RR
        newParam.channels[0].inst[21] = (ym2413Register[0x03] & 0x80) >> 7;//KL
        newParam.channels[0].inst[22] = (ym2413Register[0x01] & 0x0f);//MT
        newParam.channels[0].inst[23] = (ym2413Register[0x01] & 0x80) >> 7;//AM
        newParam.channels[0].inst[24] = (ym2413Register[0x01] & 0x40) >> 6;//VB
        newParam.channels[0].inst[25] = (ym2413Register[0x01] & 0x20) >> 5;//EG
        newParam.channels[0].inst[26] = (ym2413Register[0x01] & 0x10) >> 4;//KR
        newParam.channels[0].inst[27] = (ym2413Register[0x03] & 0x10) >> 4;//DC

    }


    public void screenInitYM2413(FrameBuffer screen, int tp) {

        for (int y = 0; y < 9; y++) {
            //Note
            DrawBuff.drawFont8(screen, 296, y * 8 + 8, 1, "   ");

            //Keyboard
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                DrawBuff.drawKbn(screen, 32 + kx, y * 8 + 8, kt, tp);
            }

            //Volume
            int d = 99;
            DrawBuff.volume(screen, 256, 8 + y * 8, 0, d, 0, tp);

            boolean db = null;
            DrawBuff.chYM2413(screen, y, db, newParam.channels[y].mask, tp);
        }

        boolean dm;
        dm = null;
        DrawBuff.chYM2413(frameBuffer, 9, dm, newParam.channels[9].mask, tp);
        dm = null;
        DrawBuff.chYM2413(frameBuffer, 10, dm, newParam.channels[10].mask, tp);
        dm = null;
        DrawBuff.chYM2413(frameBuffer, 11, dm, newParam.channels[11].mask, tp);
        dm = null;
        DrawBuff.chYM2413(frameBuffer, 12, dm, newParam.channels[12].mask, tp);
        dm = null;
        DrawBuff.chYM2413(frameBuffer, 13, dm, newParam.channels[13].mask, tp);
        int dv;
        dv = 99;
        DrawBuff.VolumeXY(frameBuffer, 6, 20, 0, dv, newParam.channels[9].volume, tp);
        dv = 99;
        DrawBuff.VolumeXY(frameBuffer, 21, 20, 0, dv, newParam.channels[10].volume, tp);
        dv = 99;
        DrawBuff.VolumeXY(frameBuffer, 36, 20, 0, dv, newParam.channels[11].volume, tp);
        dv = 99;
        DrawBuff.VolumeXY(frameBuffer, 51, 20, 0, dv, newParam.channels[12].volume, tp);
        dv = 99;
        DrawBuff.VolumeXY(frameBuffer, 66, 20, 0, dv, newParam.channels[13].volume, tp);

    }

    public void screenDrawParams() {
        boolean YM2413Type = (chipID == 0)
                ? parent.setting.getYM2413Type()[0].getUseReal()[0]
                : parent.setting.getYM2413Type()[1].getUseReal()[0];
        int YM2413SoundLocation = (chipID == 0)
                ? parent.setting.getYM2413Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2413Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2413Type ? 0 : (YM2413SoundLocation < 0 ? 2 : 1);

        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int c = 0; c < 9; c++) {

            oyc = oldParam.channels[c];
            nyc = newParam.channels[c];

            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);

            DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 37, (c / 3) * 2 + 24, oyc.inst[0], nyc.inst[0]);
            DrawBuff.susFlag(frameBuffer, (c % 3) * 16 + 41, (c / 3) * 2 + 24, 0, oyc.inst[1], nyc.inst[1]);
            DrawBuff.susFlag(frameBuffer, (c % 3) * 16 + 44, (c / 3) * 2 + 24, 0, oyc.inst[2], nyc.inst[2]);
            DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 46, (c / 3) * 2 + 24, oyc.inst[3], nyc.inst[3]);

            DrawBuff.chYM2413(frameBuffer, c, oyc.mask, nyc.mask, tp);

        }

        DrawBuff.chYM2413(frameBuffer, 9, oldParam.channels[9].mask, newParam.channels[9].mask, tp);
        DrawBuff.chYM2413(frameBuffer, 10, oldParam.channels[10].mask, newParam.channels[10].mask, tp);
        DrawBuff.chYM2413(frameBuffer, 11, oldParam.channels[11].mask, newParam.channels[11].mask, tp);
        DrawBuff.chYM2413(frameBuffer, 12, oldParam.channels[12].mask, newParam.channels[12].mask, tp);
        DrawBuff.chYM2413(frameBuffer, 13, oldParam.channels[13].mask, newParam.channels[13].mask, tp);
        DrawBuff.VolumeXY(frameBuffer, 6, 20, 0, oldParam.channels[9].volume, newParam.channels[9].volume, tp);
        DrawBuff.VolumeXY(frameBuffer, 21, 20, 0, oldParam.channels[10].volume, newParam.channels[10].volume, tp);
        DrawBuff.VolumeXY(frameBuffer, 36, 20, 0, oldParam.channels[11].volume, newParam.channels[11].volume, tp);
        DrawBuff.VolumeXY(frameBuffer, 51, 20, 0, oldParam.channels[12].volume, newParam.channels[12].volume, tp);
        DrawBuff.VolumeXY(frameBuffer, 66, 20, 0, oldParam.channels[13].volume, newParam.channels[13].volume, tp);

        oyc = oldParam.channels[0];
        nyc = newParam.channels[0];
        DrawBuff.drawInstNumber(frameBuffer, 9, 22, oyc.inst[4], nyc.inst[4]); //TL
        DrawBuff.drawInstNumber(frameBuffer, 14, 22, oyc.inst[5], nyc.inst[5]); //FB

        for (int c = 0; c < 11; c++) {
            DrawBuff.drawInstNumber(frameBuffer, c * 3, 26, oyc.inst[6 + c], nyc.inst[6 + c]);
            DrawBuff.drawInstNumber(frameBuffer, c * 3, 28, oyc.inst[17 + c], nyc.inst[17 + c]);
        }
    }

    public void screenInit() {
        for (int ch = 0; ch < 9; ch++) {
            newParam.channels[ch].inst[0] = 0;
            newParam.channels[ch].inst[1] = 0;
            newParam.channels[ch].inst[2] = 0;
            newParam.channels[ch].inst[3] = 0;
            newParam.channels[ch].note = -1;
            newParam.channels[ch].volumeL = 0;
        }

        newParam.channels[9].volume = 0;
        newParam.channels[10].volume = 0;
        newParam.channels[11].volume = 0;
        newParam.channels[12].volume = 0;
        newParam.channels[13].volume = 0;

        newParam.channels[0].inst[4] = 0;
        newParam.channels[0].inst[5] = 0;
        newParam.channels[0].inst[6] = 0;
        newParam.channels[0].inst[7] = 0;
        newParam.channels[0].inst[8] = 0;
        newParam.channels[0].inst[9] = 0;
        newParam.channels[0].inst[10] = 0;
        newParam.channels[0].inst[11] = 0;
        newParam.channels[0].inst[12] = 0;
        newParam.channels[0].inst[13] = 0;
        newParam.channels[0].inst[14] = 0;
        newParam.channels[0].inst[15] = 0;
        newParam.channels[0].inst[16] = 0;
        newParam.channels[0].inst[17] = 0;
        newParam.channels[0].inst[18] = 0;
        newParam.channels[0].inst[19] = 0;
        newParam.channels[0].inst[20] = 0;
        newParam.channels[0].inst[21] = 0;
        newParam.channels[0].inst[22] = 0;
        newParam.channels[0].inst[23] = 0;
        newParam.channels[0].inst[24] = 0;
        newParam.channels[0].inst[25] = 0;
        newParam.channels[0].inst[26] = 0;
        newParam.channels[0].inst[27] = 0;

        boolean YM2413Type = (chipID == 0)
                ? parent.setting.getYM2413Type()[0].getUseReal()[0]
                : parent.setting.getYM2413Type()[1].getUseReal()[0];
        int YM2413SoundLocation = (chipID == 0)
                ? parent.setting.getYM2413Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2413Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2413Type ? 0 : (YM2413SoundLocation < 0 ? 2 : 1);

        screenInitYM2413(frameBuffer, tp);
        update();
    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (int ch = 0; ch < 14; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.YM2413, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YM2413, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 11 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ch == 9) {
                    int x = (px / 4 - 4);
                    if (x < 0) return;
                    x /= 15;
                    if (x > 4) return;
                    ch += x;
                }

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    parent.setChannelMask(EnmChip.YM2413, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 14; ch++) parent.resetChannelMask(EnmChip.YM2413, chipID, ch);
                return;
            }

            //音色欄
            if (py < 15 * 8 && px < 16 * 8) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.YM2413, 0, chipID);
            }
        }
    };

    private void initializeComponent() {
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYM2413));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneYM2413();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 120));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYM2413
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 120));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYM2413");
        this.setTitle("YM2413");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);

    }

    BufferedImage image;
    public JPanel pbScreen;
}
