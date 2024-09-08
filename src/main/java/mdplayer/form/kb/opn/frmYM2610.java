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

import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmYM2610 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipId = 0;
    private int zoom = 1;

    private MDChipParams.YM2610 newParam = null;
    private MDChipParams.YM2610 oldParam = null;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYM2610.class);

    public frmYM2610(frmMain frm, int chipId, int zoom, MDChipParams.YM2610 newParam, MDChipParams.YM2610 oldParam) {
        super(frm);
        this.chipId = chipId;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneYM2610(), null, zoom);
        screenInit();
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
                parent.setting.getLocation().getPosYm2610()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosYm2610()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneYM2610().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2610().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneYM2610().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2610().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneYM2610().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2610().getHeight() * zoom));
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

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;
            int ch;
            int c;

            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (ch = 0; ch < 14; ch++) {
                        if (ch >= 9 && ch <= 11) continue;

                        c = ch;
                        if (ch == 12) c = 13;
                        if (ch == 13) c = 12;

                        if (newParam.channels[c].mask)
                            parent.resetChannelMask(EnmChip.YM2610, chipId, ch);
                        else
                            parent.setChannelMask(EnmChip.YM2610, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            c = ch;
            if (ch == 12) c = 13;
            if (ch == 13) c = 12;

            if (ch < 0) return;

            if (ch < 14) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    if (newParam.channels[c].mask)
                        parent.resetChannelMask(EnmChip.YM2610, chipId, ch);
                    else
                        parent.setChannelMask(EnmChip.YM2610, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 14; ch++) parent.resetChannelMask(EnmChip.YM2610, chipId, ch);
                return;
            }

            // 音色表示欄の判定

            int h = (py - 15 * 8) / (6 * 8);
            int w = Math.min(px / (13 * 8), 2);
            int instCh = h * 3 + w;

            if (instCh < 6) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.YM2610, instCh, chipId);
            }
        }
    };

    public void screenInit() {
        int tp = (
                (chipId == 0)
                        ? (parent.setting.getYM2610Type()[0].getUseReal()[0] || (parent.setting.getYM2610Type()[0].getUseReal().length > 1 && parent.setting.getYM2610Type()[0].getUseReal()[1]))
                        : (parent.setting.getYM2610Type()[1].getUseReal()[0] || (parent.setting.getYM2610Type()[1].getUseReal().length > 1 && parent.setting.getYM2610Type()[1].getUseReal()[1]))
        )
                ? 1
                : 0;

        for (int y = 0; y < 14; y++) {
            DrawBuff.drawFont8(frameBuffer, 296, y * 8 + 8, 1, "   ");
            for (int i = 0; i < 96; i++) {
                int kx = Tables.kbl[(i % 12) * 2] + i / 12 * 28;
                int kt = Tables.kbl[(i % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, y * 8 + 8, kt, tp);
            }

            if (y < 13) {
                DrawBuff.ChYM2610_P(frameBuffer, 0, y * 8 + 8, y, false, tp);
            }

            if (y < 6 || y == 13) {
                DrawBuff.drawPanP(frameBuffer, 24, y * 8 + 8, 3, tp);
            }

            int d = 99;
            if (y > 5 && y < 9) {
                d = DrawBuff.volume(frameBuffer, 256, 8 + y * 8, 0, d, 0, tp);
            } else {
                d = DrawBuff.volume(frameBuffer, 256, 8 + y * 8, 1, d, 0, tp);
                d = 99;
                d = DrawBuff.volume(frameBuffer, 256, 8 + y * 8, 2, d, 0, tp);
            }
        }

        for (int y = 0; y < 6; y++) {
            int d = 99;
            DrawBuff.PanYM2610Rhythm(frameBuffer, y, d, 3, d, tp);
            d = 99;
            DrawBuff.VolumeYM2610Rhythm(frameBuffer, y, 1, d, 0, tp);
            d = 99;
            DrawBuff.VolumeYM2610Rhythm(frameBuffer, y, 2, d, 0, tp);
        }
        boolean f = true;
        DrawBuff.ChYM2610Rhythm(frameBuffer, 0, f, false, tp);
    }

    private static byte[] md = new byte[]
            {
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
        int delta;
        float frq;

        int[][] YM2610Register = audio.getYM2610Register(chipId);
        int[] fmKeyYM2610 = audio.getYM2610KeyOn(chipId);
        int[] YM2610Vol = audio.getYM2610Volume(chipId);
        int[] YM2610Ch3SlotVol = audio.getYM2610Ch3SlotVolume(chipId);
        int[][] YM2610Rhythm = audio.getYM2610RhythmVolume(chipId);
        int[] YM2610AdpcmVol = audio.getYM2610AdpcmVolume(chipId);

        boolean isFmEx = (YM2610Register[chipId][0x27] & 0x40) > 0;
        newParam.channels[2].ex = isFmEx;

        int defaultMasterClock = 8000000;
        float ssgMul = 1.0f;
        int masterClock = defaultMasterClock;
        if (audio.clockYM2610 != 0) {
            ssgMul = audio.clockYM2610 / (float) defaultMasterClock;
            masterClock = audio.clockYM2610;
        }

        int divInd = YM2610Register[0][0x2d];
        if (divInd < 0 || divInd > 2) divInd = 0;
        float fmDiv = fmDivTbl[divInd];
        float ssgDiv = ssgDivTbl[divInd];
        ssgMul = ssgMul * ssgDiv / 4;

        //int masterClock = audio.clockYM2610;
        //int defaultMasterClock = 8000000;
        //float mul = 1.0f;
        //if (masterClock != 0)
        //    mul = masterClock / (float)defaultMasterClock;

        newParam.lfoSw = (YM2610Register[0][0x22] & 0x8) != 0;
        newParam.lfoFrq = (YM2610Register[0][0x22] & 0x7);

        for (int ch = 0; ch < 6; ch++) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                newParam.channels[ch].inst[i * 11 + 0] = YM2610Register[p][0x50 + ops + c] & 0x1f; //AR
                newParam.channels[ch].inst[i * 11 + 1] = YM2610Register[p][0x60 + ops + c] & 0x1f; //DR
                newParam.channels[ch].inst[i * 11 + 2] = YM2610Register[p][0x70 + ops + c] & 0x1f; //SR
                newParam.channels[ch].inst[i * 11 + 3] = YM2610Register[p][0x80 + ops + c] & 0x0f; //RR
                newParam.channels[ch].inst[i * 11 + 4] = (YM2610Register[p][0x80 + ops + c] & 0xf0) >> 4;//SL
                newParam.channels[ch].inst[i * 11 + 5] = YM2610Register[p][0x40 + ops + c] & 0x7f;//TL
                newParam.channels[ch].inst[i * 11 + 6] = (YM2610Register[p][0x50 + ops + c] & 0xc0) >> 6;//KS
                newParam.channels[ch].inst[i * 11 + 7] = YM2610Register[p][0x30 + ops + c] & 0x0f;//ML
                newParam.channels[ch].inst[i * 11 + 8] = (YM2610Register[p][0x30 + ops + c] & 0x70) >> 4;//DT
                newParam.channels[ch].inst[i * 11 + 9] = (YM2610Register[p][0x60 + ops + c] & 0x80) >> 7;//AM
                newParam.channels[ch].inst[i * 11 + 10] = YM2610Register[p][0x90 + ops + c] & 0x0f;//SG
            }
            newParam.channels[ch].inst[44] = YM2610Register[p][0xb0 + c] & 0x07;//AL
            newParam.channels[ch].inst[45] = (YM2610Register[p][0xb0 + c] & 0x38) >> 3;//FB
            newParam.channels[ch].inst[46] = (YM2610Register[p][0xb4 + c] & 0x38) >> 4;//AMS
            newParam.channels[ch].inst[47] = YM2610Register[p][0xb4 + c] & 0x07;//FMS

            newParam.channels[ch].pan = (YM2610Register[p][0xb4 + c] & 0xc0) >> 6;

            int freq = 0;
            int octav = 0;
            int n = -1;
            if (ch != 2 || !isFmEx) {
                freq = YM2610Register[p][0xa0 + c] + (YM2610Register[p][0xa4 + c] & 0x07) * 0x100;
                octav = (YM2610Register[p][0xa4 + c] & 0x38) >> 3;
                float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKeyYM2610[ch] & 1) != 0)
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                byte con = (byte) (fmKeyYM2610[ch]);
                int v = 127;
                int m = md[YM2610Register[p][0xb0 + c] & 7];
                //OP1
                v = (((con & 0x10) != 0) && ((m & 0x10) != 0) && v > (YM2610Register[p][0x40 + c] & 0x7f)) ? (YM2610Register[p][0x40 + c] & 0x7f) : v;
                //OP3
                v = (((con & 0x20) != 0) && ((m & 0x20) != 0) && v > (YM2610Register[p][0x44 + c] & 0x7f)) ? (YM2610Register[p][0x44 + c] & 0x7f) : v;
                //OP2
                v = (((con & 0x40) != 0) && ((m & 0x40) != 0) && v > (YM2610Register[p][0x48 + c] & 0x7f)) ? (YM2610Register[p][0x48 + c] & 0x7f) : v;
                //OP4
                v = (((con & 0x80) != 0) && ((m & 0x80) != 0) && v > (YM2610Register[p][0x4c + c] & 0x7f)) ? (YM2610Register[p][0x4c + c] & 0x7f) : v;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ((YM2610Register[p][0xb4 + c] & 0x80) != 0 ? 1 : 0) * YM2610Vol[ch] / 80.0), 0), 19);
                newParam.channels[ch].volumeR = Math.min(Math.max((int) ((127 - v) / 127.0 * ((YM2610Register[p][0xb4 + c] & 0x40) != 0 ? 1 : 0) * YM2610Vol[ch] / 80.0), 0), 19);
            } else {
                int m = md[YM2610Register[0][0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                freq = YM2610Register[0][0xa9] + (YM2610Register[0][0xad] & 0x07) * 0x100;
                octav = (YM2610Register[0][0xad] & 0x38) >> 3;
                float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKeyYM2610[2] & 0x10) != 0 && ((m & 0x10) != 0))
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                int v = ((m & 0x10) != 0) ? YM2610Register[p][0x40 + c] : 127;
                newParam.channels[2].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ((YM2610Register[0][0xb4 + 2] & 0x80) != 0 ? 1 : 0) * YM2610Ch3SlotVol[0] / 80.0), 0), 19);
                newParam.channels[2].volumeR = Math.min(Math.max((int) ((127 - v) / 127.0 * ((YM2610Register[0][0xb4 + 2] & 0x40) != 0 ? 1 : 0) * YM2610Ch3SlotVol[0] / 80.0), 0), 19);
            }
            newParam.channels[ch].note = n;


        }

        for (int ch = 6; ch < 9; ch++) //FM EX
        {
            int[] exReg = new int[] {2, 0, -6};
            int c = exReg[ch - 6];

            newParam.channels[ch].pan = 0;

            if (isFmEx) {
                int m = md[YM2610Register[0][0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                int op = ch - 5;
                op = op == 1 ? 2 : (op == 2 ? 1 : op);

                int freq = YM2610Register[0][0xa8 + c] + (YM2610Register[0][0xac + c] & 0x07) * 0x100;
                int octav = (YM2610Register[0][0xac + c] & 0x38) >> 3;
                int n = -1;
                if ((fmKeyYM2610[2] & (0x10 << (ch - 5))) != 0 && ((m & (0x10 << op)) != 0)) {
                    float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                    ff /= 1038f;
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);
                }
                newParam.channels[ch].note = n;

                int v = ((m & (0x10 << op)) != 0) ? YM2610Register[0][0x42 + op * 4] : 127;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * YM2610Ch3SlotVol[ch - 5] / 80.0), 0), 19);
            } else {
                newParam.channels[ch].note = -1;
                newParam.channels[ch].volumeL = 0;
            }
        }

        for (int ch = 0; ch < 3; ch++) //SSG
        {
            MDChipParams.Channel channel = newParam.channels[ch + 9];

            boolean t = (YM2610Register[0][0x07] & (0x1 << ch)) == 0;
            boolean n = (YM2610Register[0][0x07] & (0x8 << ch)) == 0;
            channel.tn = (t ? 1 : 0) + (n ? 2 : 0);

            channel.volume = (int) (((t || n) ? 1 : 0) * (YM2610Register[0][0x08 + ch] & 0xf) * (20.0 / 16.0));
            if (!t && !n && channel.volume > 0) {
                channel.volume--;
            }

            if (channel.volume == 0) {
                channel.note = -1;
            } else {
                int ft = YM2610Register[0][0x00 + ch * 2];
                int ct = YM2610Register[0][0x01 + ch * 2];
                int tp = (ct << 8) | ft;
                if (tp == 0) {
                    channel.note = -1;
                    //channel.volume = 0;
                } else {
                    float ftone = masterClock / (64.0f * (float) tp) * ssgMul;// 7987200 = MasterClock
                    channel.note = Common.searchSSGNote(ftone);
                }
            }

        }

        newParam.nfrq = YM2610Register[0][0x06] & 0x1f;
        newParam.efrq = YM2610Register[0][0x0c] * 0x100 + YM2610Register[0][0x0b];
        newParam.etype = (YM2610Register[0][0x0d] & 0xf);

        //ADPCM B
        newParam.channels[12].pan = (YM2610Register[0][0x11] & 0xc0) >> 6;
        if (YM2610AdpcmVol[0] != 0) {
            newParam.channels[12].volumeL = Math.min(Math.max(YM2610AdpcmVol[0] * YM2610Register[0][0x1b], 0), 19);
        } else {
            if (newParam.channels[12].volumeL > 0) newParam.channels[12].volumeL--;
        }
        if (YM2610AdpcmVol[1] != 0) {
            newParam.channels[12].volumeR = Math.min(Math.max(YM2610AdpcmVol[1] * YM2610Register[0][0x1b], 0), 19);
        } else {
            if (newParam.channels[12].volumeR > 0) newParam.channels[12].volumeR--;
        }
        delta = (YM2610Register[0][0x1a] << 8) | YM2610Register[0][0x19];
        frq = delta / 9447.0f; // Delta=9447 at freq=8kHz
        newParam.channels[12].note = (YM2610Register[0][0x10] & 0x80) != 0 ? Common.searchYM2608Adpcm(frq) : -1;
        if ((YM2610Register[0][0x11] & 0xc0) == 0) {
            newParam.channels[12].note = -1;
        }


        int tl = YM2610Register[1][0x01] & 0x3f;
        for (int ch = 13; ch < 19; ch++) // ADPCM a
        {
            newParam.channels[ch].pan = (YM2610Register[1][0x08 + ch - 13] & 0xc0) >> 6;
            //newParam.channels[ch].volumeL = Math.min(Math.max(YM2610Rhythm[ch - 13][0] / 80, 0), 19);
            //newParam.channels[ch].volumeR = Math.min(Math.max(YM2610Rhythm[ch - 13][1] / 80, 0), 19);
            int il = YM2610Register[1][0x08 + ch - 13] & 0x1f;

            if (YM2610Rhythm[ch - 13][0] != 0) {
                newParam.channels[ch].volumeL = Math.min(Math.max(YM2610Rhythm[ch - 13][0] * tl * il / 128, 0), 19);
                //newParam.channels[12].volumeR = Math.min(Math.max(YM2610AdpcmVol[1] * YM2610Register[0][0x1b], 0), 19);
            } else {
                if (newParam.channels[ch].volumeL > 0) newParam.channels[ch].volumeL--;
            }
            if (YM2610Rhythm[ch - 13][1] != 0) {
                newParam.channels[ch].volumeR = Math.min(Math.max(YM2610Rhythm[ch - 13][1] * tl * il / 128, 0), 19);
                //newParam.channels[12].volumeR = Math.min(Math.max(YM2610AdpcmVol[1] * YM2610Register[0][0x1b], 0), 19);
            } else {
                if (newParam.channels[ch].volumeR > 0) newParam.channels[ch].volumeR--;
            }
        }
    }

    public void screenDrawParams() {
        int tp = (
                (chipId == 0)
                        ? (parent.setting.getYM2610Type()[0].getUseReal()[0] || (parent.setting.getYM2610Type()[0].getUseReal().length > 1 && parent.setting.getYM2610Type()[0].getUseReal()[1]))
                        : (parent.setting.getYM2610Type()[1].getUseReal()[0] || (parent.setting.getYM2610Type()[1].getUseReal().length > 1 && parent.setting.getYM2610Type()[1].getUseReal()[1]))
        )
                ? 1
                : 0;

        // FM - SSG
        for (int c = 0; c < 9; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            if (c == 2) {
                oyc.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp);
                oyc.volumeR = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp);
                DrawBuff.Pan(frameBuffer, 24, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp);
                DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.Inst(frameBuffer, 1, 17, c, oyc.inst, nyc.inst);
                DrawBuff.Ch3YM2610(frameBuffer, c, oyc.mask, nyc.mask, oyc.ex, nyc.ex, tp);
            } else if (c < 6) {
                oyc.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp);
                oyc.volumeR = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp);
                DrawBuff.Pan(frameBuffer, 24, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp);
                DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.Inst(frameBuffer, 1, 17, c, oyc.inst, nyc.inst);
                DrawBuff.ChYM2610(frameBuffer, c, oyc.mask, nyc.mask, tp);
            } else {
                oyc.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + (c + 3) * 8, 0, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.keyBoard(frameBuffer, c + 3, oyc.note, nyc.note, tp);
                DrawBuff.ChYM2610(frameBuffer, c, oyc.mask, nyc.mask, tp);
            }
        }

        // FMex
        for (int c = 0; c < 3; c++) {
            MDChipParams.Channel oyc = oldParam.channels[c + 9];
            MDChipParams.Channel nyc = newParam.channels[c + 9];

            oyc.volume = DrawBuff.volume(frameBuffer, 256, 8 + (c + 6) * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.keyBoard(frameBuffer, c + 6, oyc.note, nyc.note, tp);
            DrawBuff.Tn(frameBuffer, 6, 2, c + 6, oyc.tn, nyc.tn, oyc.tntp, tp);

            DrawBuff.ChYM2610(frameBuffer, c + 9, oyc.mask, nyc.mask, tp);
        }

        // ADPCM B
        oldParam.channels[12].volumeL = DrawBuff.volume(frameBuffer, 256, 8 + 13 * 8, 1, oldParam.channels[12].volumeL, newParam.channels[12].volumeL, tp);
        oldParam.channels[12].volumeR = DrawBuff.volume(frameBuffer, 256, 8 + 13 * 8, 2, oldParam.channels[12].volumeR, newParam.channels[12].volumeR, tp);
        DrawBuff.Pan(frameBuffer, 24, 8 + 13 * 8, oldParam.channels[12].pan, newParam.channels[12].pan, oldParam.channels[12].pantp, tp);
        DrawBuff.keyBoard(frameBuffer, 13, oldParam.channels[12].note, newParam.channels[12].note, tp);
        DrawBuff.ChYM2610(frameBuffer, 13, oldParam.channels[12].mask, newParam.channels[12].mask, tp);

        // ADPCM a(Rhythm)
        for (int c = 0; c < 6; c++) {
            MDChipParams.Channel oyc = oldParam.channels[c + 13];
            MDChipParams.Channel nyc = newParam.channels[c + 13];

            DrawBuff.VolumeYM2610Rhythm(frameBuffer, c, 1, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.VolumeYM2610Rhythm(frameBuffer, c, 2, oyc.volumeR, nyc.volumeR, tp);
            DrawBuff.PanYM2610Rhythm(frameBuffer, c, oyc.pan, nyc.pan, oyc.pantp, tp);
        }
        DrawBuff.ChYM2610Rhythm(frameBuffer, 0, oldParam.channels[13].mask, newParam.channels[13].mask, tp);

        //HardLFO NOISE ENV
        DrawBuff.LfoSw(frameBuffer, 16, 216, oldParam.lfoSw, newParam.lfoSw);
        DrawBuff.LfoFrq(frameBuffer, 64, 216, oldParam.lfoFrq, newParam.lfoFrq);

        DrawBuff.Nfrq(frameBuffer, 25, 54, oldParam.nfrq, newParam.nfrq);
        DrawBuff.Efrq(frameBuffer, 38, 54, oldParam.efrq, newParam.efrq);
        DrawBuff.Etype(frameBuffer, 53, 54, oldParam.etype, newParam.etype);
    }

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYM2610));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneYM2610();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 224));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYM2610
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 224));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYM2610");
        this.setTitle("YM2610");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
