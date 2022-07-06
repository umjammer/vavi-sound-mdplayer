package mdplayer.form.kb.opn;

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


public class frmYM2203 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YM2203 newParam;
    private MDChipParams.YM2203 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYM2203.class);

    public frmYM2203(frmMain frm, int chipID, int zoom, MDChipParams.YM2203 newParam, MDChipParams.YM2203 oldParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneYM2203(), null, zoom);
        boolean YM2203Type = (chipID == 0)
                ? parent.setting.getYM2203Type()[0].getUseReal()[0]
                : parent.setting.getYM2203Type()[1].getUseReal()[0];
        int YM2203SoundLocation = (chipID == 0)
                ? parent.setting.getYM2203Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2203Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2203Type ? 0 : (YM2203SoundLocation < 0 ? 2 : 1);
        DrawBuff.screenInitYM2203(frameBuffer, tp);
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
                parent.setting.getLocation().getPosYm2203()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYm2203()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneYM2203().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2203().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneYM2203().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2203().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneYM2203().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2203().getHeight() * zoom));
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

    private static final byte[] md = new byte[] {
            (byte) (0x08 << 4),
            (byte) (0x08 << 4),
            (byte) (0x08 << 4),
            (byte) (0x08 << 4),
            (byte) (0x0c << 4),
            (byte) (0x0e << 4),
            (byte) (0x0e << 4),
            (byte) (0x0f << 4)
    };

    private static final float[] fmDivTbl = new float[] {6, 3, 2};
    private static final float[] ssgDivTbl = new float[] {4, 2, 1};

    public void screenChangeParams() {
        boolean isFmEx;
        int[] ym2203Register = Audio.getYm2203Register(chipID);
        int[] fmKeyYM2203 = Audio.getYM2203KeyOn(chipID);
        int[] ym2203Vol = Audio.getYM2203Volume(chipID);
        int[] ym2203Ch3SlotVol = Audio.getYM2203Ch3SlotVolume(chipID);

        isFmEx = (ym2203Register[0x27] & 0x40) > 0;
        newParam.channels[2].ex = isFmEx;

        int defaultMasterClock = 7987200 / 2;
        float ssgMul = 1.0f;
        int masterClock = defaultMasterClock;
        if (Audio.clockYM2203 != 0) {
            ssgMul = Audio.clockYM2203 / (float) defaultMasterClock;
            masterClock = Audio.clockYM2203;
        }

        int divInd = ym2203Register[0x2d];
        if (divInd < 0 || divInd > 2) divInd = 0;
        float fmDiv = fmDivTbl[divInd];
        float ssgDiv = ssgDivTbl[divInd];
        ssgMul = ssgMul * ssgDiv / 4;

        for (int ch = 0; ch < 3; ch++) {
            int c = ch;
            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                newParam.channels[ch].inst[i * 11 + 0] = ym2203Register[0x50 + ops + c] & 0x1f; // AR
                newParam.channels[ch].inst[i * 11 + 1] = ym2203Register[0x60 + ops + c] & 0x1f; // DR
                newParam.channels[ch].inst[i * 11 + 2] = ym2203Register[0x70 + ops + c] & 0x1f; // SR
                newParam.channels[ch].inst[i * 11 + 3] = ym2203Register[0x80 + ops + c] & 0x0f; // RR
                newParam.channels[ch].inst[i * 11 + 4] = (ym2203Register[0x80 + ops + c] & 0xf0) >> 4; // SL
                newParam.channels[ch].inst[i * 11 + 5] = ym2203Register[0x40 + ops + c] & 0x7f; // TL
                newParam.channels[ch].inst[i * 11 + 6] = (ym2203Register[0x50 + ops + c] & 0xc0) >> 6; // KS
                newParam.channels[ch].inst[i * 11 + 7] = ym2203Register[0x30 + ops + c] & 0x0f; // ML
                newParam.channels[ch].inst[i * 11 + 8] = (ym2203Register[0x30 + ops + c] & 0x70) >> 4; // DT
                newParam.channels[ch].inst[i * 11 + 9] = (ym2203Register[0x60 + ops + c] & 0x80) >> 7; // AM
                newParam.channels[ch].inst[i * 11 + 10] = ym2203Register[0x90 + ops + c] & 0x0f;//SG
            }
            newParam.channels[ch].inst[44] = ym2203Register[0xb0 + c] & 0x07; // AL
            newParam.channels[ch].inst[45] = (ym2203Register[0xb0 + c] & 0x38) >> 3; // FB
            newParam.channels[ch].inst[46] = (ym2203Register[0xb4 + c] & 0x38) >> 4; // AMS
            newParam.channels[ch].inst[47] = ym2203Register[0xb4 + c] & 0x07; // FMS

            newParam.channels[ch].pan = 3;

            int freq = 0;
            int octav = 0;
            int n = -1;
            if (ch != 2 || !isFmEx) {
                octav = (ym2203Register[0xa4 + c] & 0x38) >> 3;
                freq = ym2203Register[0xa0 + c] + (ym2203Register[0xa4 + c] & 0x07) * 0x100;
                float ff = freq / ((2 << 20) / (masterClock / (12 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKeyYM2203[ch] & 1) != 0)
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                byte con = (byte) (fmKeyYM2203[ch]);
                int v = 127;
                int m = md[ym2203Register[0xb0 + c] & 7];
                //OP1
                v = (((con & 0x10) != 0) && ((m & 0x10) != 0) && v > (ym2203Register[0x40 + c] & 0x7f)) ? (ym2203Register[0x40 + c] & 0x7f) : v;
                //OP3
                v = (((con & 0x20) != 0) && ((m & 0x20) != 0) && v > (ym2203Register[0x44 + c] & 0x7f)) ? (ym2203Register[0x44 + c] & 0x7f) : v;
                //OP2
                v = (((con & 0x40) != 0) && ((m & 0x40) != 0) && v > (ym2203Register[0x48 + c] & 0x7f)) ? (ym2203Register[0x48 + c] & 0x7f) : v;
                //OP4
                v = (((con & 0x80) != 0) && ((m & 0x80) != 0) && v > (ym2203Register[0x4c + c] & 0x7f)) ? (ym2203Register[0x4c + c] & 0x7f) : v;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ym2203Vol[ch] / 80.0), 0), 19);
            } else {
                int m = md[ym2203Register[0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                freq = ym2203Register[0xa9] + (ym2203Register[0xad] & 0x07) * 0x100;
                octav = (ym2203Register[0xad] & 0x38) >> 3;
                float ff = freq / ((2 << 20) / (masterClock / (12 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKeyYM2203[2] & 0x10) != 0 && ((m & 0x10) != 0))
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                int v = ((m & 0x10) != 0) ? ym2203Register[0x40 + c] : 127;
                newParam.channels[2].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ym2203Ch3SlotVol[0] / 80.0), 0), 19);
            }
            newParam.channels[ch].note = n;


        }

        for (int ch = 3; ch < 6; ch++) //FM EX
        {
            int[] exReg = new int[] {2, 0, -6};
            int c = exReg[ch - 3];

            newParam.channels[ch].pan = 0;

            if (isFmEx) {
                int m = md[ym2203Register[0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                int op = ch - 2;
                op = op == 1 ? 2 : (op == 2 ? 1 : op);

                int freq = ym2203Register[0xa8 + c] + (ym2203Register[0xac + c] & 0x07) * 0x100;
                int octav = (ym2203Register[0xac + c] & 0x38) >> 3;
                int n = -1;
                if ((fmKeyYM2203[2] & (0x20 << (ch - 3))) != 0 && ((m & (0x10 << op)) != 0)) {
                    float ff = freq / ((2 << 20) / (masterClock / (12 * fmDiv))) * (2 << (octav + 2));
                    ff /= 1038f;
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);
                }
                newParam.channels[ch].note = n;

                int v = ((m & (0x10 << op)) != 0) ? ym2203Register[0x42 + op * 4] : 127;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ym2203Ch3SlotVol[ch - 2] / 80.0), 0), 19);
            } else {
                newParam.channels[ch].note = -1;
                newParam.channels[ch].volumeL = 0;
            }
        }

        for (int ch = 0; ch < 3; ch++) { //SSG
            MDChipParams.Channel channel = newParam.channels[ch + 6];

            boolean t = (ym2203Register[0x07] & (0x1 << ch)) == 0;
            boolean n = (ym2203Register[0x07] & (0x8 << ch)) == 0;
            channel.tn = (t ? 1 : 0) + (n ? 2 : 0);
            channel.volume = (int) (((t || n) ? 1 : 0) * (ym2203Register[0x08 + ch] & 0xf) * (20.0 / 16.0));
            if (!t && !n && channel.volume > 0) {
                channel.volume--;
            }

            if (channel.volume == 0) {
                channel.note = -1;
            } else {
                int ft = ym2203Register[0x00 + ch * 2];
                int ct = ym2203Register[0x01 + ch * 2];
                int tp = (ct << 8) | ft;
                if (tp == 0) tp = 1;
                float ftone = 7987200.0f / (64.0f * (float) tp) * ssgMul; // 7987200 = MasterClock(↓のメソッドが7987200を基準としたテーブルの為)
                channel.note = Common.searchSSGNote(ftone);
            }
        }

        newParam.nfrq = ym2203Register[0x06] & 0x1f;
        newParam.efrq = ym2203Register[0x0c] * 0x100 + ym2203Register[0x0b];
        newParam.etype = (ym2203Register[0x0d] & 0xf);
    }


    public void screenDrawParams() {
        boolean YM2203Type = (chipID == 0)
                ? parent.setting.getYM2203Type()[0].getUseReal()[0]
                : parent.setting.getYM2203Type()[1].getUseReal()[0];
        int YM2203SoundLocation = (chipID == 0)
                ? parent.setting.getYM2203Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2203Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2203Type ? 0 : (YM2203SoundLocation < 0 ? 2 : 1);

        for (int c = 0; c < 6; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            if (c == 2) {
                DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.Inst(frameBuffer, 1, 12, c, oyc.inst, nyc.inst);
                DrawBuff.Ch3YM2203(frameBuffer, c, oyc.mask, nyc.mask, oyc.ex, nyc.ex, tp);
            } else if (c < 3) {
                DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.Inst(frameBuffer, 1, 12, c, oyc.inst, nyc.inst);
                DrawBuff.ChYM2203(frameBuffer, c, oyc.mask, nyc.mask, tp);
            } else {
                DrawBuff.volume(frameBuffer, 256, 8 + (c + 3) * 8, 0, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.keyBoard(frameBuffer, c + 3, oyc.note, nyc.note, tp);
                DrawBuff.ChYM2203(frameBuffer, c, oyc.mask, nyc.mask, tp);
            }
        }

        for (int c = 0; c < 3; c++) {
            MDChipParams.Channel oyc = oldParam.channels[c + 6];
            MDChipParams.Channel nyc = newParam.channels[c + 6];

            DrawBuff.volume(frameBuffer, 256, 8 + (c + 3) * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.keyBoard(frameBuffer, c + 3, oyc.note, nyc.note, tp);
            DrawBuff.Tn(frameBuffer, 6, 2, c + 3, oyc.tn, nyc.tn, oyc.tntp, tp * 2);

            DrawBuff.ChYM2203(frameBuffer, c + 6, oyc.mask, nyc.mask, tp);
        }

        DrawBuff.Nfrq(frameBuffer, 5, 32, oldParam.nfrq, newParam.nfrq);
        DrawBuff.Efrq(frameBuffer, 18, 32, oldParam.efrq, newParam.efrq);
        DrawBuff.Etype(frameBuffer, 33, 32, oldParam.etype, newParam.etype);
    }

    public void screenInit() {
        boolean YM2203Type = (chipID == 0)
                ? parent.setting.getYM2203Type()[0].getUseReal()[0]
                : parent.setting.getYM2203Type()[1].getUseReal()[0];
        int YM2203SoundLocation = (chipID == 0)
                ? parent.setting.getYM2203Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2203Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2203Type ? 0 : (YM2203SoundLocation < 0 ? 2 : 1);

        for (int ch = 0; ch < 3; ch++) {
            for (int i = 0; i < 4; i++) {
                newParam.channels[ch].inst[i * 11 + 0] = 0;
                newParam.channels[ch].inst[i * 11 + 1] = 0;
                newParam.channels[ch].inst[i * 11 + 2] = 0;
                newParam.channels[ch].inst[i * 11 + 3] = 0;
                newParam.channels[ch].inst[i * 11 + 4] = 0;
                newParam.channels[ch].inst[i * 11 + 5] = 0;
                newParam.channels[ch].inst[i * 11 + 6] = 0;
                newParam.channels[ch].inst[i * 11 + 7] = 0;
                newParam.channels[ch].inst[i * 11 + 8] = 0;
                newParam.channels[ch].inst[i * 11 + 9] = 0;
                newParam.channels[ch].inst[i * 11 + 10] = 0;
            }
            newParam.channels[ch].inst[44] = 0;
            newParam.channels[ch].inst[45] = 0;
            newParam.channels[ch].inst[46] = 0;
            newParam.channels[ch].inst[47] = 0;
            newParam.channels[ch].pan = 3;
            newParam.channels[ch].volumeL = 0;
            newParam.channels[ch].note = -1;
        }

        for (int ch = 3; ch < 6; ch++) { //FM EX
            newParam.channels[ch].pan = 0;
            newParam.channels[ch].note = -1;
            newParam.channels[ch].volumeL = 0;
            newParam.channels[ch].note = -1;
        }

        for (int ch = 0; ch < 3; ch++) { // SSG
            MDChipParams.Channel channel = newParam.channels[ch + 6];
            channel.tn = 0;
            channel.volume = 0;
            channel.note = -1;
        }

        newParam.nfrq = 0;
        newParam.efrq = 0;
        newParam.etype = 0;

        DrawBuff.screenInitYM2203(frameBuffer, tp);
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
                    for (int ch = 0; ch < 6; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.YM2203, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YM2203, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 10 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    if (newParam.channels[ch].mask)
                        parent.resetChannelMask(EnmChip.YM2203, chipID, ch);
                    else
                        parent.setChannelMask(EnmChip.YM2203, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 9; ch++) parent.resetChannelMask(EnmChip.YM2203, chipID, ch);
                return;
            }

            //音色で右クリックした場合は何もしない
            if (ev.getButton() == MouseEvent.BUTTON2) return;

            // 音色表示欄の判定
            int instCh = Math.min(px / (13 * 8), 2);

            if (instCh < 3) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.YM2203, instCh, chipID);
            }
        }
    };

    private void initializeComponent() {
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYM2203));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneYM2203();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 136));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYM2203
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 136));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYM2203");
        this.setTitle("YM2203");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}

