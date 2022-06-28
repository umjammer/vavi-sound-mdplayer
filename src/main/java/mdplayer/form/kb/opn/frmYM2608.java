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


public class frmYM2608 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YM2608 newParam;
    private MDChipParams.YM2608 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYM2608.class);

    public frmYM2608(frmMain frm, int chipID, int zoom, MDChipParams.YM2608 newParam, MDChipParams.YM2608 oldParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneD(), null, zoom);
        boolean YM2608Type = (chipID == 0)
                ? parent.setting.getYM2608Type()[0].getUseReal()[0]
                : parent.setting.getYM2608Type()[1].getUseReal()[0];
        int YM2608SoundLocation = (chipID == 0)
                ? parent.setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2608Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2608Type ? 0 : (YM2608SoundLocation < 0 ? 2 : 1);
        DrawBuff.screenInitYM2608(frameBuffer, tp);
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
                parent.setting.getLocation().getPosYm2608()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYm2608()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneD().getWidth() * zoom, frameSizeH + Resources.getPlaneD().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneD().getWidth() * zoom, frameSizeH + Resources.getPlaneD().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneD().getWidth() * zoom, frameSizeH + Resources.getPlaneD().getHeight() * zoom));
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

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
        }
        boolean YM2608Type = (chipID == 0)
                ? parent.setting.getYM2608Type()[0].getUseReal()[0]
                : parent.setting.getYM2608Type()[1].getUseReal()[0];
        int YM2608SoundLocation = (chipID == 0)
                ? parent.setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2608Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !YM2608Type ? 0 : (YM2608SoundLocation < 0 ? 2 : 1);
        DrawBuff.screenInitYM2608(frameBuffer, tp);
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
        boolean isFmEx;
        int[][] ym2608Register = Audio.getYM2608Register(chipID);
        int[] fmKeyYM2608 = Audio.getYM2608KeyOn(chipID);
        int[] ym2608Vol = Audio.getYM2608Volume(chipID);
        int[] ym2608Ch3SlotVol = Audio.getYM2608Ch3SlotVolume(chipID);
        int[][] ym2608Rhythm = Audio.getYM2608RhythmVolume(chipID);
        int[] ym2608AdpcmVol = Audio.getYM2608AdpcmVolume(chipID);

        newParam.timerA = ym2608Register[0][0x24] | ((ym2608Register[0][0x25] & 0x3) << 8);
        newParam.timerB = ym2608Register[0][0x26];
        newParam.rhythmTotalLevel = ym2608Register[0][0x11];
        newParam.adpcmLevel = ym2608Register[1][0x0b];

        isFmEx = (ym2608Register[0][0x27] & 0x40) > 0;
        newParam.channels[2].ex = isFmEx;

        int defaultMasterClock = 7987200;
        float ssgMul = 1.0f;
        int masterClock = defaultMasterClock;
        if (Audio.clockYM2608 != 0) {
            ssgMul = Audio.clockYM2608 / (float) defaultMasterClock;
            masterClock = Audio.clockYM2608;
        }

        int divInd = ym2608Register[0][0x2d];
        if (divInd < 0 || divInd > 2) divInd = 0;
        float fmDiv = fmDivTbl[divInd];
        float ssgDiv = ssgDivTbl[divInd];
        ssgMul = ssgMul * ssgDiv / 4;

        newParam.lfoSw = (ym2608Register[0][0x22] & 0x8) != 0;
        newParam.lfoFrq = (ym2608Register[0][0x22] & 0x7);

        for (int ch = 0; ch < 6; ch++) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                newParam.channels[ch].inst[i * 11 + 0] = ym2608Register[p][0x50 + ops + c] & 0x1f; //AR
                newParam.channels[ch].inst[i * 11 + 1] = ym2608Register[p][0x60 + ops + c] & 0x1f; //DR
                newParam.channels[ch].inst[i * 11 + 2] = ym2608Register[p][0x70 + ops + c] & 0x1f; //SR
                newParam.channels[ch].inst[i * 11 + 3] = ym2608Register[p][0x80 + ops + c] & 0x0f; //RR
                newParam.channels[ch].inst[i * 11 + 4] = (ym2608Register[p][0x80 + ops + c] & 0xf0) >> 4;//SL
                newParam.channels[ch].inst[i * 11 + 5] = ym2608Register[p][0x40 + ops + c] & 0x7f;//TL
                newParam.channels[ch].inst[i * 11 + 6] = (ym2608Register[p][0x50 + ops + c] & 0xc0) >> 6;//KS
                newParam.channels[ch].inst[i * 11 + 7] = ym2608Register[p][0x30 + ops + c] & 0x0f;//ML
                newParam.channels[ch].inst[i * 11 + 8] = (ym2608Register[p][0x30 + ops + c] & 0x70) >> 4;//DT
                newParam.channels[ch].inst[i * 11 + 9] = (ym2608Register[p][0x60 + ops + c] & 0x80) >> 7;//AM
                newParam.channels[ch].inst[i * 11 + 10] = ym2608Register[p][0x90 + ops + c] & 0x0f;//SG
            }
            newParam.channels[ch].inst[44] = ym2608Register[p][0xb0 + c] & 0x07;//AL
            newParam.channels[ch].inst[45] = (ym2608Register[p][0xb0 + c] & 0x38) >> 3;//FB
            newParam.channels[ch].inst[46] = (ym2608Register[p][0xb4 + c] & 0x38) >> 4;//AMS
            newParam.channels[ch].inst[47] = ym2608Register[p][0xb4 + c] & 0x07;//FMS

            newParam.channels[ch].pan = (ym2608Register[p][0xb4 + c] & 0xc0) >> 6;
            newParam.channels[ch].slot = (byte) (fmKeyYM2608[ch] >> 4);

            int freq;
            int octav;
            int n = -1;
            if (ch != 2 || !isFmEx) {
                octav = (ym2608Register[p][0xa4 + c] & 0x38) >> 3;
                freq = ym2608Register[p][0xa0 + c] + (ym2608Register[p][0xa4 + c] & 0x07) * 0x100;
                newParam.channels[ch].freq = (freq & 0x7ff) | ((octav & 7) << 11);
                float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKeyYM2608[ch] & 1) != 0)
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                byte con = (byte) (fmKeyYM2608[ch]);
                int v = 127;
                int m = md[ym2608Register[p][0xb0 + c] & 7];
                //OP1
                v = (((con & 0x10) != 0) && ((m & 0x10) != 0) && v > (ym2608Register[p][0x40 + c] & 0x7f)) ? (ym2608Register[p][0x40 + c] & 0x7f) : v;
                //OP3
                v = (((con & 0x20) != 0) && ((m & 0x20) != 0) && v > (ym2608Register[p][0x44 + c] & 0x7f)) ? (ym2608Register[p][0x44 + c] & 0x7f) : v;
                //OP2
                v = (((con & 0x40) != 0) && ((m & 0x40) != 0) && v > (ym2608Register[p][0x48 + c] & 0x7f)) ? (ym2608Register[p][0x48 + c] & 0x7f) : v;
                //OP4
                v = (((con & 0x80) != 0) && ((m & 0x80) != 0) && v > (ym2608Register[p][0x4c + c] & 0x7f)) ? (ym2608Register[p][0x4c + c] & 0x7f) : v;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ((ym2608Register[p][0xb4 + c] & 0x80) != 0 ? 1 : 0) * ym2608Vol[ch] / 80.0), 0), 19);
                newParam.channels[ch].volumeR = Math.min(Math.max((int) ((127 - v) / 127.0 * ((ym2608Register[p][0xb4 + c] & 0x40) != 0 ? 1 : 0) * ym2608Vol[ch] / 80.0), 0), 19);

            } else {
                int m = md[ym2608Register[0][0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                freq = ym2608Register[0][0xa9] + (ym2608Register[0][0xad] & 0x07) * 0x100;
                octav = (ym2608Register[0][0xad] & 0x38) >> 3;
                newParam.channels[2].freq = (freq & 0x7ff) | ((octav & 7) << 11);
                float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKeyYM2608[2] & 0x10) > 0 && ((m & 0x10) != 0))
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                int v = ((m & 0x10) != 0) ? ym2608Register[p][0x40 + c] : 127;
                newParam.channels[2].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ((ym2608Register[0][0xb4 + 2] & 0x80) != 0 ? 1 : 0) * ym2608Ch3SlotVol[0] / 80.0), 0), 19);
                newParam.channels[2].volumeR = Math.min(Math.max((int) ((127 - v) / 127.0 * ((ym2608Register[0][0xb4 + 2] & 0x40) != 0 ? 1 : 0) * ym2608Ch3SlotVol[0] / 80.0), 0), 19);
            }
            newParam.channels[ch].note = n;


        }

        for (int ch = 6; ch < 9; ch++) { //FM EX
            int[] exReg = new int[] {2, 0, -6};
            int c = exReg[ch - 6];

            newParam.channels[ch].pan = 0;

            if (isFmEx) {
                int m = md[ym2608Register[0][0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                int op = ch - 5;
                op = op == 1 ? 2 : (op == 2 ? 1 : op);

                int freq = ym2608Register[0][0xa8 + c] + (ym2608Register[0][0xac + c] & 0x07) * 0x100;
                int octav = (ym2608Register[0][0xac + c] & 0x38) >> 3;
                newParam.channels[ch].freq = (freq & 0x7ff) | ((octav & 7) << 11);
                int n = -1;
                if ((fmKeyYM2608[2] & (0x10 << (ch - 5))) != 0 && ((m & (0x10 << op)) != 0)) {
                    float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                    ff /= 1038f;
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);
                }
                newParam.channels[ch].note = n;

                int v = ((m & (0x10 << op)) != 0) ? ym2608Register[0][0x42 + op * 4] : 127;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ym2608Ch3SlotVol[ch - 5] / 80.0), 0), 19);
            } else {
                newParam.channels[ch].note = -1;
                newParam.channels[ch].volumeL = 0;
            }
        }

        for (int ch = 0; ch < 3; ch++) { // SSG
            MDChipParams.Channel channel = newParam.channels[ch + 9];

            boolean t = (ym2608Register[0][0x07] & (0x1 << ch)) == 0;
            boolean n = (ym2608Register[0][0x07] & (0x8 << ch)) == 0;
            channel.tn = (t ? 1 : 0) + (n ? 2 : 0);

            channel.volume = (int) (((t || n) ? 1 : 0) * (ym2608Register[0][0x08 + ch] & 0xf) * (20.0 / 16.0));
            if (!t && !n && channel.volume > 0) {
                channel.volume--;
            }

            if (channel.volume == 0) {
                channel.note = -1;
            } else {
                int ft = ym2608Register[0][0x00 + ch * 2];
                int ct = ym2608Register[0][0x01 + ch * 2];
                int tp = (ct << 8) | ft;
                channel.freq = tp;
                if (tp == 0) {
                    channel.note = -1;
                    //channel.volume = 0;
                } else {
                    float ftone = masterClock / (64.0f * (float) tp) * ssgMul;// 7987200 = MasterClock
                    channel.note = Common.searchSSGNote(ftone);
                }
            }
        }

        newParam.nfrq = ym2608Register[0][0x06] & 0x1f;
        newParam.efrq = ym2608Register[0][0x0c] * 0x100 + ym2608Register[0][0x0b];
        newParam.etype = (ym2608Register[0][0x0d] & 0xf);

        //ADPCM
        newParam.channels[12].pan = (ym2608Register[1][0x01] & 0xc0) >> 6; // ((ym2608Register[1][0x01] & 0xc0) >> 6) != 0 ? ((ym2608Register[1][0x01] & 0xc0) >> 6) : newParam.channels[12].pan;
        newParam.channels[12].volumeL = Math.min(Math.max(ym2608AdpcmVol[0] / 80, 0), 19);
        newParam.channels[12].volumeR = Math.min(Math.max(ym2608AdpcmVol[1] / 80, 0), 19);
        int delta = (ym2608Register[1][0x0a] << 8) | ym2608Register[1][0x09];
        newParam.channels[12].freq = delta;
        float frq = delta / 9447.0f;
        newParam.channels[12].note = (ym2608Register[1][0x00] & 0x80) != 0 ? (Common.searchYM2608Adpcm(frq) - 1) : -1;
        if ((ym2608Register[1][0x01] & 0xc0) == 0) {
            newParam.channels[12].note = -1;
        }

        for (int ch = 13; ch < 19; ch++) { // RHYTHM
            newParam.channels[ch].pan = (ym2608Register[0][0x18 + ch - 13] & 0xc0) >> 6;
            newParam.channels[ch].volumeL = Math.min(Math.max(ym2608Rhythm[ch - 13][0] / 80, 0), 19);
            newParam.channels[ch].volumeR = Math.min(Math.max(ym2608Rhythm[ch - 13][1] / 80, 0), 19);
            newParam.channels[ch].volumeRL = ym2608Register[0][ch - 13 + 0x18] & 0x1f;
        }
    }

    public void screenDrawParams() {
        boolean ChipType2 = (chipID == 0)
                ? parent.setting.getYM2608Type()[0].getUseReal()[0]
                : parent.setting.getYM2608Type()[1].getUseReal()[0];
        int chipSoundLocation = (chipID == 0)
                ? parent.setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYM2608Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !ChipType2 ? 0 : (chipSoundLocation < 0 ? 2 : 1);

        for (int c = 0; c < 9; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            if (c == 2) {
                DrawBuff.volume(frameBuffer, 288 + 1, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.volume(frameBuffer, 288 + 1, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp);
                DrawBuff.Pan(frameBuffer, 25, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp);
                DrawBuff.KeyBoardOPNA(frameBuffer, 33, 8 + c * 8, oyc.note, nyc.note, tp);
                DrawBuff.InstOPNA(frameBuffer, 4, 17 * 8, c, oyc.inst, nyc.inst);
                DrawBuff.Ch3YM2608(frameBuffer, c, oyc.mask, nyc.mask, oyc.ex, nyc.ex, tp);
                DrawBuff.Slot(frameBuffer, 1 + 4 * 64, 8 + c * 8, oyc.slot, nyc.slot);
                DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
            } else if (c < 6) {
                DrawBuff.volume(frameBuffer, 288 + 1, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.volume(frameBuffer, 288 + 1, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp);
                DrawBuff.Pan(frameBuffer, 25, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp);
                DrawBuff.KeyBoardOPNA(frameBuffer, 33, 8 + c * 8, oyc.note, nyc.note, tp);
                DrawBuff.InstOPNA(frameBuffer, 4, 17 * 8, c, oyc.inst, nyc.inst);
                DrawBuff.ChYM2608(frameBuffer, c, oyc.mask, nyc.mask, tp);
                DrawBuff.Slot(frameBuffer, 1 + 4 * 64, 8 + c * 8, oyc.slot, nyc.slot);
                DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
            } else {
                DrawBuff.volume(frameBuffer, 288 + 1, 8 + (c + 3) * 8, 0, oyc.volumeL, nyc.volumeL, tp);
                //if (c == 7 && oyc.note != nyc.note) {
                //System.err.println("note:%d", nyc.note);
                //int[][] ym2608Register = Audio.GetYM2608Register(chipID);
                //int freq1 = ym2608Register[0][0xa9] + (ym2608Register[0][0xad] ) * 0x100;
                //int freq2 = ym2608Register[0][0xa8] + (ym2608Register[0][0xac] ) * 0x100;
                //int freq3 = ym2608Register[0][0xaa] + (ym2608Register[0][0xae] ) * 0x100;
                //int freq4 = ym2608Register[0][0xa2] + (ym2608Register[0][0xa6] ) * 0x100;
                //System.err.println("frq:%4x %4x %4x %4x", freq1, freq2, freq3, freq4);
                //}
                DrawBuff.KeyBoardOPNA(frameBuffer, 33, 8 + (c + 3) * 8, oyc.note, nyc.note, tp);
                DrawBuff.ChYM2608(frameBuffer, c, oyc.mask, nyc.mask, tp);
                DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + (c + 3) * 8, 0, oyc.freq, nyc.freq);
            }


        }
        // SSG
        for (int c = 0; c < 3; c++) {
            MDChipParams.Channel oyc = oldParam.channels[c + 9];
            MDChipParams.Channel nyc = newParam.channels[c + 9];

            DrawBuff.volume(frameBuffer, 289, 8 + (c + 6) * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.KeyBoardOPNA(frameBuffer, 33, (c + 6) * 8 + 8, oyc.note, nyc.note, tp);
            DrawBuff.TnOPNA(frameBuffer, 6, 2, c + 6, oyc.tn, nyc.tn, oyc.tntp, tp * 2);

            DrawBuff.ChYM2608(frameBuffer, c + 9, oyc.mask, nyc.mask, tp);
            DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + (c + 6) * 8, 0, oyc.freq, nyc.freq);
        }

        // ADPCM
        DrawBuff.volume(frameBuffer, 289, 8 + 12 * 8, 1, oldParam.channels[12].volumeL, newParam.channels[12].volumeL, tp);
        DrawBuff.volume(frameBuffer, 289, 8 + 12 * 8, 2, oldParam.channels[12].volumeR, newParam.channels[12].volumeR, tp);
        DrawBuff.Pan(frameBuffer, 25, 8 + 12 * 8, oldParam.channels[12].pan, newParam.channels[12].pan, oldParam.channels[12].pantp, tp);
        DrawBuff.KeyBoardOPNA(frameBuffer, 33, 8 + 12 * 8, oldParam.channels[12].note, newParam.channels[12].note, tp);
        DrawBuff.ChYM2608(frameBuffer, 12, oldParam.channels[12].mask, newParam.channels[12].mask, tp);
        DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + 12 * 8, 0, oldParam.channels[12].freq, newParam.channels[12].freq);

        // Rhythm
        for (int c = 0; c < 6; c++) {
            MDChipParams.Channel oyc = oldParam.channels[c + 13];
            MDChipParams.Channel nyc = newParam.channels[c + 13];

            DrawBuff.VolumeYM2608Rhythm(frameBuffer, c, 1, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.VolumeYM2608Rhythm(frameBuffer, c, 2, oyc.volumeR, nyc.volumeR, tp);
            DrawBuff.PanYM2608Rhythm(frameBuffer, c, oyc.pan, nyc.pan, oyc.pantp, tp);
            DrawBuff.font4Int2(frameBuffer, c * 4 * 15 + 4, 28 * 4, 0, 0, oyc.volumeRL, nyc.volumeRL);
        }
        DrawBuff.ChYM2608Rhythm(frameBuffer, 0, oldParam.channels[13].mask, newParam.channels[13].mask, tp);

        DrawBuff.font4Hex12Bit(frameBuffer, 85 * 4, 30 * 4, 0, oldParam.timerA, newParam.timerA);
        DrawBuff.font4HexByte(frameBuffer, 85 * 4, 32 * 4, 0, oldParam.timerB, newParam.timerB);

        DrawBuff.LfoSw(frameBuffer, 84 * 4, 18 * 8, oldParam.lfoSw, newParam.lfoSw);
        DrawBuff.LfoFrq(frameBuffer, 84 * 4, 19 * 8, oldParam.lfoFrq, newParam.lfoFrq);

        DrawBuff.Nfrq(frameBuffer, 84, 42, oldParam.nfrq, newParam.nfrq);
        DrawBuff.Efrq(frameBuffer, 84, 44, oldParam.efrq, newParam.efrq);
        DrawBuff.Etype(frameBuffer, 84, 46, oldParam.etype, newParam.etype);

        DrawBuff.font4Int2(frameBuffer, 84 * 4, 50 * 4, 0, 0, oldParam.rhythmTotalLevel, newParam.rhythmTotalLevel);
        DrawBuff.font4Int3(frameBuffer, 84 * 4, 52 * 4, 0, 3, oldParam.adpcmLevel, newParam.adpcmLevel);
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
                    for (ch = 0; ch < 14; ch++) {
                        if (ch >= 9 && ch <= 11) continue;

                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.YM2608, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YM2608, chipID, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;

            if (ch < 0) return;

            if (ch < 14) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    if (newParam.channels[ch].mask)
                        parent.resetChannelMask(EnmChip.YM2608, chipID, ch);
                    else
                        parent.setChannelMask(EnmChip.YM2608, chipID, ch);
                    return;
                }

                for (ch = 0; ch < 14; ch++) parent.resetChannelMask(EnmChip.YM2608, chipID, ch);
                return;
            }

            // 音色表示欄の判定

            int h = (py - 15 * 8) / (6 * 8);
            int w = Math.min(px / (13 * 8), 2);
            int instCh = h * 3 + w;

            if (instCh < 6) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.YM2608, instCh, chipID);
            }
        }
    };

    private void initializeComponent() {
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYM2608));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneD();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(360, 216));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYM2608
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(360, 216));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYM2608");
        this.setTitle("OPNA");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
