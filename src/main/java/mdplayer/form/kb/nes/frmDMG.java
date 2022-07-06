package mdplayer.form.kb.nes;

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
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmDMG extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.DMG newParam;
    private MDChipParams.DMG oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();
    static Preferences prefs = Preferences.userNodeForPackage(frmDMG.class);

    public frmDMG(frmMain frm, int chipID, int zoom, MDChipParams.DMG newParam, MDChipParams.DMG oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(this.pbScreen, Resources.getPlaneDMG(), null, zoom);
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
                parent.setting.getLocation().getPosDMG()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosDMG()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneDMG().getWidth() * zoom, frameSizeH + Resources.getPlaneDMG().getHeight() * zoom));
        setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneDMG().getWidth() * zoom, frameSizeH + Resources.getPlaneDMG().getHeight() * zoom));
        setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneDMG().getWidth() * zoom, frameSizeH + Resources.getPlaneDMG().getHeight() * zoom));
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
        mdsound.Gb.GbSound dat = Audio.getDMGRegister(chipID);
        if (dat == null) return;

        // pan
        newParam.channels[0].pan = (dat.controller.mode1Left * 2) + dat.controller.mode1Right;
        newParam.channels[1].pan = (dat.controller.mode2Left * 2) + dat.controller.mode2Right;
        newParam.channels[2].pan = (dat.controller.mode3Left * 2) + dat.controller.mode3Right;
        newParam.channels[3].pan = (dat.controller.mode4Left * 2) + dat.controller.mode4Right;

        // freq
        newParam.channels[0].freq = dat.sound1.frequency;
        newParam.channels[1].freq = dat.sound2.frequency;
        newParam.channels[2].freq = dat.sound3.frequency;
        newParam.channels[3].freq = dat.sound4.registers[3] & 0x7; // pfq
        newParam.channels[3].bit[47] = (dat.sound4.registers[3] & 0x8) != 0; // poly
        newParam.channels[3].srcFreq = (dat.sound4.registers[3] & 0xf0) >> 4; // pc

        // CC
        newParam.channels[0].bit[0] = dat.sound1.lengthEnabled;
        newParam.channels[1].bit[0] = dat.sound2.lengthEnabled;
        newParam.channels[2].bit[0] = dat.sound3.lengthEnabled;
        newParam.channels[3].bit[0] = dat.sound4.lengthEnabled;

        // Ini
        newParam.channels[0].bit[1] = (dat.sound1.registers[4] & 0x80) != 0;
        newParam.channels[1].bit[1] = (dat.sound2.registers[4] & 0x80) != 0;
        newParam.channels[2].bit[1] = (dat.sound3.registers[4] & 0x80) != 0;
        newParam.channels[3].bit[1] = (dat.sound4.registers[4] & 0x80) != 0;

        // Env.Dir
        newParam.channels[0].bit[2] = dat.sound1.envelopeDirection == 1;
        newParam.channels[1].bit[2] = dat.sound2.envelopeDirection == 1;
        // newParam.channels[2].bit[2] = ないよ
        newParam.channels[3].bit[2] = dat.sound4.envelopeDirection == 1;

        // Sweep Dec
        newParam.channels[0].bit[3] = dat.sound1.sweepDirection == -1;

        // Env.Spd
        newParam.channels[0].inst[0] = dat.sound1.envelopeTime;
        newParam.channels[1].inst[0] = dat.sound2.envelopeTime;
        // newParam.channels[2].inst[0] = ないよ
        newParam.channels[3].inst[0] = dat.sound4.envelopeTime;

        // Env.Vol
        newParam.channels[0].inst[1] = dat.sound1.envelopeValue;
        newParam.channels[1].inst[1] = dat.sound2.envelopeValue;
        // newParam.channels[2].inst[1] = ないよ
        newParam.channels[3].inst[1] = dat.sound4.envelopeValue;

        // Len
        newParam.channels[0].inst[2] = dat.sound1.length;
        newParam.channels[1].inst[2] = dat.sound2.length;
        // newParam.channels[2].inst[2] = ないよ
        newParam.channels[3].inst[2] = dat.sound4.length;

        // Duty
        newParam.channels[0].inst[3] = dat.sound1.duty;
        newParam.channels[1].inst[3] = dat.sound2.duty;
        // newParam.channels[2].inst[3] = ないよ
        // newParam.channels[3].inst[3] = ないよ

        // Sweep time
        newParam.channels[0].inst[4] = dat.sound1.sweepTime;
        // Sweep shift
        newParam.channels[0].inst[5] = dat.sound1.sweepShift;

        // Len
        newParam.channels[2].inst[4] = dat.sound3.length;
        // Vol
        newParam.channels[2].inst[5] = dat.sound3.level;

        // wf
        for (int i = 0; i < 16; i++) {
            newParam.wf[i * 2] = (byte) ((dat.registers[0x20 + i] >> 4) & 0xf);
            newParam.wf[i * 2 + 1] = (byte) (dat.registers[0x20 + i] & 0xf);
        }

        int r = 10;
        newParam.channels[0].volumeL = Math.min((dat.sound1.envelopeValue * dat.controller.mode1Left) * 16 / r, 19);
        newParam.channels[0].volumeR = Math.min((dat.sound1.envelopeValue * dat.controller.mode1Right) * 16 / r, 19);
        newParam.channels[1].volumeL = Math.min((dat.sound2.envelopeValue * dat.controller.mode2Left) * 16 / r, 19);
        newParam.channels[1].volumeR = Math.min((dat.sound2.envelopeValue * dat.controller.mode2Right) * 16 / r, 19);
        int lvl = dat.sound3.level == 0 ? 0 : (19 >> (dat.sound3.level - 1));
        newParam.channels[2].volumeL = Math.min(lvl * dat.controller.mode3Left * 19 / r, 19);
        newParam.channels[2].volumeR = Math.min(lvl * dat.controller.mode3Right * 19 / r, 19);
        newParam.channels[3].volumeL = Math.min((dat.sound4.envelopeValue * dat.controller.mode4Left) * 16 / r, 19);
        newParam.channels[3].volumeR = Math.min((dat.sound4.envelopeValue * dat.controller.mode4Right) * 16 / r, 19);

        float ftone;

        for (int i = 0; i < 3; i++) {
            newParam.channels[i].note = -1;
            if (newParam.channels[i].volumeL != 0 || newParam.channels[i].volumeR != 0) {
                ftone = 4194304.0f / (4 * 2 * (2048.0f - (float) newParam.channels[i].freq));
                newParam.channels[i].note = Math.max(Math.min(searchSSGNote(ftone), 8 * 12), 0);
            }
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc = oldParam.channels[0];
        MDChipParams.Channel nyc = newParam.channels[0];
        DrawBuff.Pan(frameBuffer, 24, 8, oyc.pan, nyc.pan, oyc.pantp, 0);
        DrawBuff.font4Hex12Bit(frameBuffer, 260, 8, 0, oyc.freq, nyc.freq);
        DrawBuff.VolumeXY(frameBuffer, 68, 2, 1, oyc.volumeL, nyc.volumeL, 0);
        DrawBuff.VolumeXY(frameBuffer, 68, 3, 1, oyc.volumeR, nyc.volumeR, 0);
        DrawBuff.drawNESSw(frameBuffer, 60, 40, oyc.bit[0], nyc.bit[0]); // CC
        DrawBuff.drawNESSw(frameBuffer, 60, 48, oyc.bit[1], nyc.bit[1]); // Ini
        DrawBuff.drawNESSw(frameBuffer, 28, 64, oyc.bit[2], nyc.bit[2]); // Env.Dir
        DrawBuff.drawNESSw(frameBuffer, 88, 56, oyc.bit[3], nyc.bit[3]); // Sweep Dec
        DrawBuff.font4Int1(frameBuffer, 28, 48, 0, oyc.inst[0], nyc.inst[0]); // Env. Spd
        DrawBuff.font4Int2(frameBuffer, 24, 56, 0, 1, oyc.inst[1], nyc.inst[1]); // Env. Vol
        DrawBuff.font4Int2(frameBuffer, 56, 64, 0, 1, oyc.inst[2], nyc.inst[2]); // Len
        DrawBuff.font4Int1(frameBuffer, 60, 56, 0, oyc.inst[3], nyc.inst[3]); // Duty
        DrawBuff.font4Int1(frameBuffer, 88, 48, 0, oyc.inst[4], nyc.inst[4]); // Sweep time
        DrawBuff.font4Int1(frameBuffer, 88, 64, 0, oyc.inst[5], nyc.inst[5]); // Sweep shift
        DrawBuff.KeyBoardDMG(frameBuffer, 0, oyc.note, nyc.note, 0);
        DrawBuff.ChDMG(frameBuffer, 0, oyc.mask, nyc.mask, 0);

        oyc = oldParam.channels[1];
        nyc = newParam.channels[1];
        DrawBuff.Pan(frameBuffer, 24, 16, oyc.pan, nyc.pan, oyc.pantp, 0);
        DrawBuff.font4Hex12Bit(frameBuffer, 260, 16, 0, oyc.freq, nyc.freq);
        DrawBuff.VolumeXY(frameBuffer, 68, 4, 1, oyc.volumeL, nyc.volumeL, 0);
        DrawBuff.VolumeXY(frameBuffer, 68, 5, 1, oyc.volumeR, nyc.volumeR, 0);
        DrawBuff.drawNESSw(frameBuffer, 152, 40, oyc.bit[0], nyc.bit[0]); // CC
        DrawBuff.drawNESSw(frameBuffer, 152, 48, oyc.bit[1], nyc.bit[1]); // Ini
        DrawBuff.drawNESSw(frameBuffer, 120, 64, oyc.bit[2], nyc.bit[2]); // Env.Dir
        DrawBuff.font4Int1(frameBuffer, 120, 48, 0, oyc.inst[0], nyc.inst[0]); // Env. Spd
        DrawBuff.font4Int2(frameBuffer, 116, 56, 0, 1, oyc.inst[1], nyc.inst[1]); // Env. Vol
        DrawBuff.font4Int2(frameBuffer, 148, 64, 0, 1, oyc.inst[2], nyc.inst[2]); // Len
        DrawBuff.font4Int1(frameBuffer, 152, 56, 0, oyc.inst[3], nyc.inst[3]); // Duty
        DrawBuff.KeyBoardDMG(frameBuffer, 1, oyc.note, nyc.note, 0);
        DrawBuff.ChDMG(frameBuffer, 1, oyc.mask, nyc.mask, 0);

        oyc = oldParam.channels[2];
        nyc = newParam.channels[2];
        DrawBuff.Pan(frameBuffer, 24, 24, oyc.pan, nyc.pan, oyc.pantp, 0);
        DrawBuff.font4Hex12Bit(frameBuffer, 260, 24, 0, oyc.freq, nyc.freq);
        DrawBuff.VolumeXY(frameBuffer, 68, 6, 1, oyc.volumeL, nyc.volumeL, 0);
        DrawBuff.VolumeXY(frameBuffer, 68, 7, 1, oyc.volumeR, nyc.volumeR, 0);
        DrawBuff.drawNESSw(frameBuffer, 228, 40, oyc.bit[0], nyc.bit[0]); // CC
        DrawBuff.drawNESSw(frameBuffer, 228, 48, oyc.bit[1], nyc.bit[1]); // Ini
        // Env.Dirなし
        DrawBuff.font4Int2(frameBuffer, 220, 56, 0, 3, oyc.inst[4], nyc.inst[4]); // Len
        DrawBuff.font4Int1(frameBuffer, 228, 64, 0, oyc.inst[5], nyc.inst[5]); // Vol
        DrawBuff.KeyBoardDMG(frameBuffer, 2, oyc.note, nyc.note, 0);
        DrawBuff.ChDMG(frameBuffer, 2, oyc.mask, nyc.mask, 0);

        oyc = oldParam.channels[3];
        nyc = newParam.channels[3];
        DrawBuff.Pan(frameBuffer, 24, 32, oyc.pan, nyc.pan, oyc.pantp, 0);
        DrawBuff.VolumeXY(frameBuffer, 68, 8, 1, oyc.volumeL, nyc.volumeL, 0);
        DrawBuff.VolumeXY(frameBuffer, 68, 9, 1, oyc.volumeR, nyc.volumeR, 0);
        DrawBuff.font4Int1(frameBuffer, 316, 40, 0, oyc.freq, nyc.freq);
        DrawBuff.drawNESSw(frameBuffer, 316, 48, oyc.bit[47], nyc.bit[47]);
        DrawBuff.font4Int2(frameBuffer, 312, 56, 0, 1, oyc.srcFreq, nyc.srcFreq);
        DrawBuff.drawNESSw(frameBuffer, 288, 40, oyc.bit[0], nyc.bit[0]); // CC
        DrawBuff.drawNESSw(frameBuffer, 288, 48, oyc.bit[1], nyc.bit[1]); // Ini
        DrawBuff.drawNESSw(frameBuffer, 260, 64, oyc.bit[2], nyc.bit[2]); // Env.Dir
        DrawBuff.font4Int1(frameBuffer, 260, 48, 0, oyc.inst[0], nyc.inst[0]); // Env. Spd
        DrawBuff.font4Int2(frameBuffer, 256, 56, 0, 1, oyc.inst[1], nyc.inst[1]); // Env. Vol
        DrawBuff.font4Int2(frameBuffer, 284, 64, 0, 1, oyc.inst[2], nyc.inst[2]); // Len
        DrawBuff.ChDMG(frameBuffer, 3, oyc.mask, nyc.mask, 0);

        DrawBuff.WaveFormToDMG(frameBuffer, 168, 58, oldParam.wf, newParam.wf); // wave form
    }

    public void screenInit() {
        for (int c = 0; c < 3; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volume = -1;
            newParam.channels[c].tn = -1;
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, c * 8 + 8, kt, 0);
            }
        }
    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            // 上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                // 但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (int ch = 0; ch < 4; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.DMG, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.DMG, chipID, ch);
                    }
                }
                return;
            }

            // 鍵盤
            if (py < 5 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // マスク
                    parent.setChannelMask(EnmChip.DMG, chipID, ch);
                    return;
                }

                // マスク解除
                for (ch = 0; ch < 4; ch++) parent.resetChannelMask(EnmChip.DMG, chipID, ch);
            }
        }
    };

    private int searchSSGNote(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 9; i++) {
            float a = Math.abs((freq / (1 << (6 - 4))) - Tables.freqTbl[i]);// 6:正規の範囲   4:補正
            if (m > a) {
                m = a;
                n = i;
            } else break;
        }
        return n;
    }

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        // ((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        // 
        // pbScreen
        // 
//        this.pbScreen.setBackground(Color.ControlDarkDark);
        this.image = mdplayer.properties.Resources.getPlaneDMG();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(336, 72));
        // this.pbScreen.TabIndex = 2
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        // 
        // frmDMG
        // 
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        // this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(336, 72));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmDMG");
        this.setTitle("DMG");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        // ((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
