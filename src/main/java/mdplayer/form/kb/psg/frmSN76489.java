package mdplayer.form.kb.psg;

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


public class frmSN76489 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.SN76489 newParam;
    private MDChipParams.SN76489 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmSN76489.class);

    public frmSN76489(frmMain frm, int chipID, int zoom, MDChipParams.SN76489 newParam, MDChipParams.SN76489 oldParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneSN76489(), null, zoom);
        boolean SN76489Type = (chipID == 0) ? parent.setting.getSN76489Type()[0].getUseReal()[0] : parent.setting.getSN76489Type()[1].getUseReal()[0];
        int tp = SN76489Type ? 1 : 0;
        DrawBuff.screenInitSN76489(frameBuffer, tp);
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
                parent.setting.getLocation().getPosSN76489()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosSN76489()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneSN76489().getWidth() * zoom, frameSizeH + Resources.getPlaneSN76489().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneSN76489().getWidth() * zoom, frameSizeH + Resources.getPlaneSN76489().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneSN76489().getWidth() * zoom, frameSizeH + Resources.getPlaneSN76489().getHeight() * zoom));
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
        int[] psgRegister = Audio.getPSGRegister(chipID);
        int[] psgRegister1 = null;
        int psgRegisterPan = Audio.getPSGRegisterGGPanning(chipID);
        int[][] psgVol = Audio.getPSGVolume(chipID);
        int[][] psgVol1 = null;
        boolean NGPFlag = Audio.getSn76489NGPFlag();

        if (NGPFlag && chipID == 1) {
            for (int ch = 0; ch < 4; ch++) {
                newParam.channels[ch].note = -1;

                newParam.channels[ch].volumeL = 0;
                newParam.channels[ch].volumeR = 0;
                newParam.channels[ch].pan = 0;
            }
            //Noise Ch
            newParam.channels[3].freq = 0;
        } else {
            if (psgRegister != null) {
                if (NGPFlag) {
                    psgVol1 = Audio.getPSGVolume(1);
                    psgRegister1 = Audio.getPSGRegister(1);

                    //Tone Ch
                    for (int ch = 0; ch < 3; ch++) {
                        if (psgRegister[ch * 2 + 1] != 15) {
                            float ftone = Audio.clockSN76489 / (2.0f * psgRegister[ch * 2] * 16.0f);

                            newParam.channels[ch].note = searchSSGNote(ftone);// searchPSGNote(psgRegister[ch * 2]);
                        } else {
                            newParam.channels[ch].note = -1;
                        }

                        newParam.channels[ch].volumeL = Math.min(Math.max((int) ((psgVol[ch][0]) / (15.0 / 19.0)), 0), 19);
                        newParam.channels[ch].volumeR = Math.min(Math.max((int) ((psgVol1[ch][0]) / (15.0 / 19.0)), 0), 19);
                        newParam.channels[ch].pan = Math.min(Math.max(newParam.channels[ch].volumeR, 0), 15) * 0x10
                                + Math.min(Math.max(newParam.channels[ch].volumeL, 0), 15);
                    }

                    //Noise Ch
                    newParam.channels[3].note = psgRegister1[6];
                    newParam.channels[3].freq = psgRegister1[4];//ch3Freq
                    newParam.channels[3].volumeL = Math.min(Math.max((int) ((psgVol[3][0]) / (15.0 / 19.0)), 0), 19);
                    newParam.channels[3].volumeR = Math.min(Math.max((int) ((psgVol1[3][0]) / (15.0 / 19.0)), 0), 19);
                    newParam.channels[3].pan = Math.min(Math.max(newParam.channels[3].volumeR, 0), 15) * 0x10
                            + Math.min(Math.max(newParam.channels[3].volumeL, 0), 15);
                } else {
                    //Tone Ch
                    for (int ch = 0; ch < 3; ch++) {
                        if (psgRegister[ch * 2 + 1] != 15) {
                            newParam.channels[ch].note = searchPSGNote(psgRegister[ch * 2]);
                        } else {
                            newParam.channels[ch].note = -1;
                        }

                        newParam.channels[ch].volumeL = Math.min(Math.max((int) ((psgVol[ch][0]) / (15.0 / 19.0)), 0), 19);
                        newParam.channels[ch].volumeR = Math.min(Math.max((int) ((psgVol[ch][1]) / (15.0 / 19.0)), 0), 19);
                        newParam.channels[ch].pan = (psgRegisterPan >> ch) & 0x11;
                        newParam.channels[ch].pan = ((newParam.channels[ch].pan) & 0x1) | (newParam.channels[ch].pan >> 3);
                    }

                    //Noise Ch
                    newParam.channels[3].note = psgRegister[6];
                    newParam.channels[3].freq = psgRegister[4];//ch3Freq
                    newParam.channels[3].volumeL = Math.min(Math.max((int) ((psgVol[3][0]) / (15.0 / 19.0)), 0), 19);
                    newParam.channels[3].volumeR = Math.min(Math.max((int) ((psgVol[3][1]) / (15.0 / 19.0)), 0), 19);
                    newParam.channels[3].pan = (psgRegisterPan >> 3) & 0x11;
                    newParam.channels[3].pan = ((newParam.channels[3].pan) & 0x1) | (newParam.channels[3].pan >> 3);
                }
            }
        }

    }

    public void screenDrawParams() {
        boolean SN76489Type = (chipID == 0) ? parent.setting.getSN76489Type()[0].getUseReal()[0] : parent.setting.getSN76489Type()[1].getUseReal()[0];
        int tp = SN76489Type ? 1 : 0;
        MDChipParams.Channel osc;
        MDChipParams.Channel nsc;
        boolean NGPFlag = Audio.getSn76489NGPFlag();

        for (int c = 0; c < 3; c++) {
            osc = oldParam.channels[c];
            nsc = newParam.channels[c];

            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 1, osc.volumeL, nsc.volumeL, tp);
            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 2, osc.volumeR, nsc.volumeR, tp);
            DrawBuff.keyBoard(frameBuffer, c, osc.note, nsc.note, tp);
            DrawBuff.ChSN76489(frameBuffer, c, osc.mask, nsc.mask, tp);
            if (NGPFlag) {
                DrawBuff.PanType2(frameBuffer, c, osc.pan, nsc.pan, tp);
            } else {
                DrawBuff.Pan(frameBuffer, 24, 8 + c * 8, osc.pan, nsc.pan, osc.pantp, tp);
            }
        }

        osc = oldParam.channels[3];
        nsc = newParam.channels[3];
        DrawBuff.volume(frameBuffer, 256, 8 + 3 * 8, 1, osc.volumeL, nsc.volumeL, tp);
        DrawBuff.volume(frameBuffer, 256, 8 + 3 * 8, 2, osc.volumeR, nsc.volumeR, tp);
        DrawBuff.ChSN76489(frameBuffer, 3, osc.mask, nsc.mask, tp);
        DrawBuff.ChSN76489Noise(frameBuffer, osc, nsc, tp);
        if (NGPFlag) {
            DrawBuff.PanType2(frameBuffer, 3, osc.pan, nsc.pan, tp);
        } else {
            DrawBuff.Pan(frameBuffer, 24, 8 + 3 * 8, osc.pan, nsc.pan, osc.pantp, tp);
        }
        if (osc.freq != nsc.freq) {
            DrawBuff.drawFont4(frameBuffer, 172, 32, 0, String.format("%04d", nsc.freq));
            osc.freq = nsc.freq;
        }
    }

    public void screenInit() {
        for (int ch = 0; ch < 3; ch++) {
            newParam.channels[ch].note = -1;

            newParam.channels[ch].volumeL = 0;
            newParam.channels[ch].volumeR = 0;
            newParam.channels[ch].pan = 0;
        }

        newParam.channels[3].note = 0;
        newParam.channels[3].freq = 0;
        newParam.channels[3].volumeL = 0;
        newParam.channels[3].volumeR = 0;
        newParam.channels[3].pan = 0;
    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int py = ev.getY() / zoom;
            int px = ev.getX() / zoom;

            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (int ch = 0; ch < 4; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.SN76489, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.SN76489, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 5 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                boolean NGPFlag = Audio.getSn76489NGPFlag();

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    parent.setChannelMask(EnmChip.SN76489, chipID, ch);
                    if (NGPFlag && chipID == 0) parent.setChannelMask(EnmChip.SN76489, 1, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 4; ch++) {
                    parent.resetChannelMask(EnmChip.SN76489, chipID, ch);
                    if (NGPFlag && chipID == 0) parent.resetChannelMask(EnmChip.SN76489, 1, ch);
                }
            }
        }
    };

    private int searchPSGNote(int freq) {
        int m = Integer.MAX_VALUE;
        int n = 0;

        for (int i = 0; i < 12 * 8; i++) {
            int a = Math.abs(freq - Tables.PsgFNum[i]);

            if (m > a) {
                m = a;
                n = i;
            }
        }

        return n;
    }

    private int searchSSGNote(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            float a = Math.abs(freq - Tables.freqTbl[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneSN76489();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 40));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmSN76489
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 40));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmSN76489");
        this.setTitle("SN76489");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
