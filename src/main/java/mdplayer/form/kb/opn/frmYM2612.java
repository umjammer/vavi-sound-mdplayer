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
import mdplayer.Common.FileFormat;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.driver.Xgm;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmYM2612 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YM2612 newParam = null;
    private MDChipParams.YM2612 oldParam = null;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYM2612.class);

    public frmYM2612(frmMain frm, int chipID, int zoom, MDChipParams.YM2612 newParam, MDChipParams.YM2612 oldParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getplaneYM2612(), null, zoom);
        screenInit();
        update();
    }

    public void screenInit() {
        boolean YM2612Type = (chipID == 0) ? parent.setting.getYM2612Type()[0].getUseReal()[0] : parent.setting.getYM2612Type()[1].getUseReal()[0];
        int tp = YM2612Type ? 1 : 0;
        DrawBuff.screenInitYM2612(frameBuffer, tp, (chipID == 0)
                        ? parent.setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()
                        : parent.setting.getYM2612Type()[1].getRealChipInfo()[0].getOnlyPCMEmulation()
                , newParam.fileFormat == FileFormat.XGM);
        newParam.channels[5].pcmBuff = 100;
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
                parent.setting.getLocation().getPosYm2612()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYm2612()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneYM2612().getWidth() * zoom, frameSizeH + Resources.getplaneYM2612().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneYM2612().getWidth() * zoom, frameSizeH + Resources.getplaneYM2612().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneYM2612().getWidth() * zoom, frameSizeH + Resources.getplaneYM2612().getHeight() * zoom));
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

    public void screenChangeParams() {
        int[][] fmRegister = Audio.getFMRegister(chipID);
        int[] fmVol = Audio.getFMVolume(chipID);
        int[] fmCh3SlotVol = Audio.getFMCh3SlotVolume(chipID);
        int[] fmKey = Audio.getFMKeyOn(chipID);

        boolean isFmEx = (fmRegister[0][0x27] & 0x40) != 0;
        newParam.channels[2].ex = isFmEx;

        newParam.lfoSw = (fmRegister[0][0x22] & 0x8) != 0;
        newParam.lfoFrq = (fmRegister[0][0x22] & 0x7);
        newParam.timerA = fmRegister[0][0x24] | ((fmRegister[0][0x25] & 0x3) << 8);
        newParam.timerB = fmRegister[0][0x26];

        //int masterClock = Audio.clockYM2612;
        //int defaultMasterClock = 8000000;
        //float mul = 1.0f;
        //if (masterClock != 0)
        //    mul = masterClock / (float)defaultMasterClock;

        int defaultMasterClock = 8000000;
        float ssgMul = 1.0f;
        int masterClock = defaultMasterClock;
        if (Audio.clockYM2612 != 0) {
            ssgMul = Audio.clockYM2612 / (float) defaultMasterClock;
            masterClock = Audio.clockYM2612;
        }

        float fmDiv = 6;
        float ssgDiv = 4;
        ssgMul = ssgMul * ssgDiv / 4;

        for (int ch = 0; ch < 6; ch++) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            newParam.channels[ch].slot = (byte) (fmKey[ch] >> 4);
            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                newParam.channels[ch].inst[i * 11 + 0] = fmRegister[p][0x50 + ops + c] & 0x1f; //AR
                newParam.channels[ch].inst[i * 11 + 1] = fmRegister[p][0x60 + ops + c] & 0x1f; //DR
                newParam.channels[ch].inst[i * 11 + 2] = fmRegister[p][0x70 + ops + c] & 0x1f; //SR
                newParam.channels[ch].inst[i * 11 + 3] = fmRegister[p][0x80 + ops + c] & 0x0f; //RR
                newParam.channels[ch].inst[i * 11 + 4] = (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4;//SL
                newParam.channels[ch].inst[i * 11 + 5] = fmRegister[p][0x40 + ops + c] & 0x7f;//TL
                newParam.channels[ch].inst[i * 11 + 6] = (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6;//KS
                newParam.channels[ch].inst[i * 11 + 7] = fmRegister[p][0x30 + ops + c] & 0x0f;//ML
                newParam.channels[ch].inst[i * 11 + 8] = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4;//DT
                newParam.channels[ch].inst[i * 11 + 9] = (fmRegister[p][0x60 + ops + c] & 0x80) >> 7;//AM
                newParam.channels[ch].inst[i * 11 + 10] = fmRegister[p][0x90 + ops + c] & 0x0f;//SG
            }
            newParam.channels[ch].inst[44] = fmRegister[p][0xb0 + c] & 0x07;//AL
            newParam.channels[ch].inst[45] = (fmRegister[p][0xb0 + c] & 0x38) >> 3;//FB
            newParam.channels[ch].inst[46] = (fmRegister[p][0xb4 + c] & 0x38) >> 4;//AMS
            newParam.channels[ch].inst[47] = fmRegister[p][0xb4 + c] & 0x07;//FMS

            newParam.channels[ch].pan = (fmRegister[p][0xb4 + c] & 0xc0) >> 6;

            int freq = 0;
            int octav = 0;
            int n = -1;
            if (ch != 2 || !isFmEx) {
                freq = fmRegister[p][0xa0 + c] + (fmRegister[p][0xa4 + c] & 0x07) * 0x100;
                octav = (fmRegister[p][0xa4 + c] & 0x38) >> 3;
                newParam.channels[ch].freq = (freq & 0x7ff) | ((octav & 7) << 11);
                float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKey[ch] & 1) != 0) {
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);
                    //if (ch == 0)
                    //{
                    //    System.err.println("freq:%d  masterClock:%d  fmDiv:%d  octav:%d ff:%d  n:%d", freq, masterClock, fmDiv, octav,ff,n);
                    //}
                }

                byte con = (byte) (fmKey[ch]);
                int v = 127;
                int m = md[fmRegister[p][0xb0 + c] & 7];

                //OP1
                v = (((con & 0x10) != 0) && ((m & 0x10) != 0) && v > (fmRegister[p][0x40 + c] & 0x7f)) ? (fmRegister[p][0x40 + c] & 0x7f) : v;
                //OP3
                v = (((con & 0x20) != 0) && ((m & 0x20) != 0) && v > (fmRegister[p][0x44 + c] & 0x7f)) ? (fmRegister[p][0x44 + c] & 0x7f) : v;
                //OP2
                v = (((con & 0x40) != 0) && ((m & 0x40) != 0) && v > (fmRegister[p][0x48 + c] & 0x7f)) ? (fmRegister[p][0x48 + c] & 0x7f) : v;
                //OP4
                v = (((con & 0x80) != 0) && ((m & 0x80) != 0) && v > (fmRegister[p][0x4c + c] & 0x7f)) ? (fmRegister[p][0x4c + c] & 0x7f) : v;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ((fmRegister[p][0xb4 + c] & 0x80) != 0 ? 1 : 0) * fmVol[ch] / 80.0), 0), 19);
                newParam.channels[ch].volumeR = Math.min(Math.max((int) ((127 - v) / 127.0 * ((fmRegister[p][0xb4 + c] & 0x40) != 0 ? 1 : 0) * fmVol[ch] / 80.0), 0), 19);
            } else {
                int m = md[fmRegister[0][0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                freq = fmRegister[0][0xa9] + (fmRegister[0][0xad] & 0x07) * 0x100;
                octav = (fmRegister[0][0xad] & 0x38) >> 3;
                newParam.channels[2].freq = (freq & 0x7ff) | ((octav & 7) << 11);
                float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                ff /= 1038f;

                if ((fmKey[2] & 0x10) != 0 && ((m & 0x10) != 0))
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);

                int v = ((m & 0x10) != 0) ? fmRegister[p][0x40 + c] : 127;
                newParam.channels[2].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * ((fmRegister[0][0xb4 + 2] & 0x80) != 0 ? 1 : 0) * fmCh3SlotVol[0] / 80.0), 0), 19);
                newParam.channels[2].volumeR = Math.min(Math.max((int) ((127 - v) / 127.0 * ((fmRegister[0][0xb4 + 2] & 0x40) != 0 ? 1 : 0) * fmCh3SlotVol[0] / 80.0), 0), 19);
            }
            newParam.channels[ch].note = n;


        }

        for (int ch = 6; ch < 9; ch++) {
            //Operator 1′s frequency instanceof : A9 and ADH
            //Operator 2′s frequency instanceof : AA and AEH
            //Operator 3′s frequency instanceof : A8 and ACH
            //Operator 4′s frequency instanceof : A2 and A6H

            int[] exReg = new int[] {2, 0, -6};
            int c = exReg[ch - 6];

            newParam.channels[ch].pan = 0;

            if (isFmEx) {
                int m = md[fmRegister[0][0xb0 + 2] & 7];
                if (parent.setting.getOther().getExAll()) m = 0xf0;
                int op = ch - 5;
                op = op == 1 ? 2 : (op == 2 ? 1 : op);

                int freq = fmRegister[0][0xa8 + c] + (fmRegister[0][0xac + c] & 0x07) * 0x100;
                int octav = (fmRegister[0][0xac + c] & 0x38) >> 3;
                newParam.channels[ch].freq = (freq & 0x7ff) | ((octav & 7) << 11);
                int n = -1;
                if ((fmKey[2] & (0x10 << (ch - 5))) != 0 && ((m & (0x10 << op)) != 0)) {
                    float ff = freq / ((2 << 20) / (masterClock / (24 * fmDiv))) * (2 << (octav + 2));
                    ff /= 1038f;
                    n = Math.min(Math.max(Common.searchYM2608Adpcm(ff) - 1, 0), 95);
                }
                newParam.channels[ch].note = n;

                int v = ((m & (0x10 << op)) != 0) ? fmRegister[0][0x42 + op * 4] : 127;
                newParam.channels[ch].volumeL = Math.min(Math.max((int) ((127 - v) / 127.0 * fmCh3SlotVol[ch - 5] / 80.0), 0), 19);
            } else {
                newParam.channels[ch].note = -1;
                newParam.channels[ch].volumeL = 0;
            }
        }

        newParam.channels[5].pcmMode = (fmRegister[0][0x2b] & 0x80) >> 7;
        if (newParam.channels[5].pcmBuff > 0)
            newParam.channels[5].pcmBuff--;
        if (newParam.channels[5].pcmMode != 0) {
            newParam.channels[5].volumeL = Math.min(Math.max(fmVol[5] / 80, 0), 19);
            newParam.channels[5].volumeR = Math.min(Math.max(fmVol[5] / 80, 0), 19);
        }

        if (newParam.fileFormat == FileFormat.XGM && Audio.driverVirtual instanceof Xgm) {

            if (Audio.driverVirtual != null && ((Xgm) Audio.driverVirtual).xgmpcm != null) {
                for (int i = 0; i < 4; i++) {
                    if (((Xgm) Audio.driverVirtual).xgmpcm[i].isPlaying) {
                        newParam.xpcmInst[i] = ((Xgm) Audio.driverVirtual).xgmpcm[i].inst;
                        int d = (((Xgm) Audio.driverVirtual).xgmpcm[i].data / 6);
                        d = Math.min(d, 19);
                        newParam.xpcmVolL[i] = d;
                        newParam.xpcmVolR[i] = d;
                    } else {
                        newParam.xpcmInst[i] = 0;
                        newParam.xpcmVolL[i] = 0;
                        newParam.xpcmVolR[i] = 0;
                    }
                }
            }
        }
    }

    public void screenDrawParams() {
        for (int c = 0; c < 9; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            boolean YM2612type = (chipID == 0)
                    ? parent.setting.getYM2612Type()[0].getUseReal()[0]
                    : parent.setting.getYM2612Type()[1].getUseReal()[0];
            int tp = YM2612type ? 1 : 0;

            if (c == 2) {
                DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp);
                DrawBuff.Pan(frameBuffer, 25, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp);
                DrawBuff.KeyBoardOPNM(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.InstOPN2(frameBuffer, 13, 96, c, oyc.inst, nyc.inst);
                DrawBuff.Ch3YM2612(frameBuffer, c, oyc.mask, nyc.mask, oyc.ex, nyc.ex, tp);
                DrawBuff.Slot(frameBuffer, 1 + 4 * 64, 8 + c * 8, oyc.slot, nyc.slot);
                DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
            } else if (c < 5) {
                DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp);
                DrawBuff.Pan(frameBuffer, 25, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp);
                DrawBuff.KeyBoardOPNM(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.InstOPN2(frameBuffer, 13, 96, c, oyc.inst, nyc.inst);
                DrawBuff.ChYM2612(frameBuffer, c, oyc.mask, nyc.mask, tp);
                DrawBuff.Slot(frameBuffer, 1 + 4 * 64, 8 + c * 8, oyc.slot, nyc.slot);
                DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
            } else if (c == 5) {
                int tp6 = tp;
                int tp6v = tp;
                if (tp6 == 1 && parent.setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                    tp6v = newParam.channels[5].pcmMode == 0 ? 1 : 0;//volumeのみモードの判定を行う
                    //tp6 = 0;
                }

                DrawBuff.Pan(frameBuffer, 25, 8 + c * 8, oyc.pan, nyc.pan, oyc.pantp, tp6v);
                DrawBuff.InstOPN2(frameBuffer, 13, 96, c, oyc.inst, nyc.inst);

                if (newParam.fileFormat != FileFormat.XGM) {
                    if (oldParam.fileFormat != newParam.fileFormat) {
                        //
                        oyc.pcmMode = 1;
                        nyc.pcmMode = 0;
                        DrawBuff.Ch6YM2612XGM(frameBuffer, nyc.pcmBuff, oyc.pcmMode, nyc.pcmMode, oyc.mask, nyc.mask, oyc.tp, tp6v);
                        oldParam.fileFormat = newParam.fileFormat;
                    }

                    DrawBuff.Ch6YM2612(frameBuffer, nyc.pcmBuff, oyc.pcmMode, nyc.pcmMode, oyc.mask, nyc.mask, oyc.tp, tp6v);
                    DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp6v);
                    DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp6v);
                    DrawBuff.KeyBoardOPNM(frameBuffer, c, oyc.note, nyc.note, tp6v);
                    DrawBuff.Slot(frameBuffer, 1 + 4 * 64, 8 + c * 8, oyc.slot, nyc.slot);
                    DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
                } else {
                    if (oldParam.fileFormat != newParam.fileFormat) {
                        screenInit();
                        oldParam.fileFormat = newParam.fileFormat;
                    }

                    DrawBuff.Ch6YM2612XGM(frameBuffer, nyc.pcmBuff, oyc.pcmMode, nyc.pcmMode, oyc.mask, nyc.mask, oyc.tp, tp6v);
                    if (newParam.channels[5].pcmMode == 0) {
                        DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 1, oyc.volumeL, nyc.volumeL, tp6v);
                        DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 2, oyc.volumeR, nyc.volumeR, tp6v);
                        DrawBuff.KeyBoardOPNM(frameBuffer, c, oyc.note, nyc.note, tp6v);
                        DrawBuff.Slot(frameBuffer, 1 + 4 * 64, 8 + c * 8, oyc.slot, nyc.slot);
                        DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
                    } else {
                        for (int i = 0; i < 4; i++) {
                            DrawBuff.VolumeXYOPN2(frameBuffer, (13 + i * 17) * 4 + 1, 12 * 4, 1, oldParam.xpcmVolL[i], newParam.xpcmVolL[i], tp6v);
                            DrawBuff.VolumeXYOPN2(frameBuffer, (13 + i * 17) * 4 + 1, 12 * 4, 2, oldParam.xpcmVolR[i], newParam.xpcmVolR[i], tp6v);
                            if (oldParam.xpcmInst[i] != newParam.xpcmInst[i]) {
                                DrawBuff.drawFont4Int2(frameBuffer, 45 + i * 17 * 4, 48, tp6v, 2, newParam.xpcmInst[i]);
                                oldParam.xpcmInst[i] = newParam.xpcmInst[i];
                            }
                        }
                    }
                }
            } else {
                DrawBuff.volume(frameBuffer, 289, 8 + c * 8, 0, oyc.volumeL, nyc.volumeL, tp);
                DrawBuff.KeyBoardOPNM(frameBuffer, c, oyc.note, nyc.note, tp);
                DrawBuff.ChYM2612(frameBuffer, c, oyc.mask, nyc.mask, tp);
                oyc.freq = 0;
                DrawBuff.font4Hex16Bit(frameBuffer, 1 + 4 * 68, 8 + c * 8, 0, oyc.freq, nyc.freq);
            }

        }

        DrawBuff.LfoSw(frameBuffer, 16 + 1, 176, oldParam.lfoSw, newParam.lfoSw);
        DrawBuff.LfoFrq(frameBuffer, 64 + 1, 176, oldParam.lfoFrq, newParam.lfoFrq);
        DrawBuff.font4Hex12Bit(frameBuffer, 1 + 29 * 4, 44 * 4, 0, oldParam.timerA, newParam.timerA);
        DrawBuff.font4HexByte(frameBuffer, 1 + 43 * 4, 44 * 4, 0, oldParam.timerB, newParam.timerB);
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
                            parent.resetChannelMask(EnmChip.YM2612, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YM2612, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 10 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;
                if (6 <= ch && ch <= 8) {
                    ch = 2;
                }

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    if (newParam.channels[ch].mask)
                        parent.resetChannelMask(EnmChip.YM2612, chipID, ch);
                    else
                        parent.setChannelMask(EnmChip.YM2612, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 6; ch++) parent.resetChannelMask(EnmChip.YM2612, chipID, ch);
                return;
            }

            //音色で右クリックした場合は何もしない
            if (ev.getButton() == MouseEvent.BUTTON2) return;

            // 音色表示欄の判定
            int h = (py - 10 * 8) / (6 * 8);
            int w = Math.min(px / (29 * 4), 2);
            int instCh = h * 3 + w;

            if (instCh < 6) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.YM2612, instCh, chipID);
            }
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYM2612));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneYM2612();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 184));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYM2612
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 184));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYM2612");
        this.setTitle("Ym2612");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
