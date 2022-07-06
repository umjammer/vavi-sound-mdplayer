package mdplayer.form.kb.wf;

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
import java.util.Arrays;
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
import mdsound.OotakePsg;


public class frmHuC6280 extends frmBase {

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmHuC6280));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneHuC6280();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 151));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmHuC6280
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 151));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmHuC6280");
        this.setTitle("Huc6280");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;

    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.HuC6280 newParam;
    private MDChipParams.HuC6280 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmHuC6280.class);

    public frmHuC6280(frmMain frm, int chipID, int zoom, MDChipParams.HuC6280 newParam, MDChipParams.HuC6280 oldParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneHuC6280(), null, zoom);
        DrawBuff.screenInitHuC6280(frameBuffer);
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
                parent.setting.getLocation().getPosHuC6280()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosHuC6280()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneHuC6280().getWidth() * zoom, frameSizeH + Resources.getPlaneHuC6280().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneHuC6280().getWidth() * zoom, frameSizeH + Resources.getPlaneHuC6280().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneHuC6280().getWidth() * zoom, frameSizeH + Resources.getPlaneHuC6280().getHeight() * zoom));
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

        OotakePsg.HuC6280State chip = Audio.getHuC6280Register(chipID);
        if (chip == null) return;

        //System.err.println("%d  %d", chip.MainVolumeL,chip.MainVolumeR);
        for (int ch = 0; ch < 6; ch++) {
            OotakePsg.HuC6280State.Psg psg = chip.psgs[ch];
            if (psg == null) continue;
            MDChipParams.Channel channel = newParam.channels[ch];
            //System.err.println("%d  %d",psg.outVolumeL, psg.outVolumeR);
            channel.volumeL = psg.outVolumeL >> 10;
            channel.volumeR = psg.outVolumeR >> 10;
            channel.volumeL = Math.min(channel.volumeL, 19);
            channel.volumeR = Math.min(channel.volumeR, 19);

            channel.pan = (psg.volumeL & 0xf) | ((psg.volumeR & 0xf) << 4);

            channel.inst = psg.wave;

            channel.dda = psg.dda;

            int tp = psg.frq;
            if (tp == 0) tp = 1;

            float ftone = 3579545.0f / 32.0f / (float) tp;
            channel.note = searchSSGNote(ftone);
            if (channel.volumeL == 0 && channel.volumeR == 0) channel.note = -1;

            if (ch < 4) continue;

            channel.noise = psg.bNoiseOn;
            channel.nfrq = psg.noiseFrq;
        }

        newParam.mvolL = chip.mainVolumeL;
        newParam.mvolR = chip.mainVolumeR;
        newParam.LfoCtrl = chip.lfoControl;
        newParam.LfoFrq = chip.lfoFreq;
    }

    public void screenDrawParams() {
        int tp = parent.setting.getHuC6280Type()[0].getUseReal()[0] ? 1 : 0;

        for (int c = 0; c < 6; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);

            DrawBuff.VolumeToHuC6280(frameBuffer, c, 1, oyc.volumeL, nyc.volumeL);
            DrawBuff.VolumeToHuC6280(frameBuffer, c, 2, oyc.volumeR, nyc.volumeR);
            DrawBuff.PanType2(frameBuffer, c, oyc.pan, nyc.pan, tp);

            DrawBuff.WaveFormToHuC6280(frameBuffer, c, oyc.inst, nyc.inst);
            DrawBuff.DDAToHuC6280(frameBuffer, c, oyc.dda, nyc.dda);

            DrawBuff.ChHuC6280(frameBuffer, c, oyc.mask, nyc.mask, tp);

            if (c < 4) continue;

            DrawBuff.NoiseToHuC6280(frameBuffer, c, oyc.noise, nyc.noise);
            DrawBuff.NoiseFrqToHuC6280(frameBuffer, c, oyc.nfrq, nyc.nfrq);
        }

        DrawBuff.MainVolumeToHuC6280(frameBuffer, 0, oldParam.mvolL, newParam.mvolL);
        DrawBuff.MainVolumeToHuC6280(frameBuffer, 1, oldParam.mvolR, newParam.mvolR);

        DrawBuff.LfoCtrlToHuC6280(frameBuffer, oldParam.LfoCtrl, newParam.LfoCtrl);
        DrawBuff.LfoFrqToHuC6280(frameBuffer, oldParam.LfoFrq, newParam.LfoFrq);
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
                    for (int ch = 0; ch < 6; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.HuC6280, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.HuC6280, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 7 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    parent.setChannelMask(EnmChip.HuC6280, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 6; ch++) parent.resetChannelMask(EnmChip.HuC6280, chipID, ch);
                return;
            }

            //音色で右クリックした場合は何もしない
            if (ev.getButton() == MouseEvent.BUTTON2) return;

            // 音色表示欄の判定
            int h = (py - 7 * 8) / (5 * 8);
            int w = Math.min(px / (13 * 8), 2);
            int instCh = h * 3 + w;

            if (instCh < 6) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.HuC6280, instCh, chipID);
            }
        }
    };

    private int searchSSGNote(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            //if (freq < Tables.freqTbl[i]) break;
            //n = i;
            float a = Math.abs(freq - Tables.freqTbl[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volumeL = -1;
            newParam.channels[c].volumeR = -1;
            newParam.channels[c].pan = -1;
            Arrays.fill(newParam.channels[c].inst, 0);
            newParam.channels[c].dda = false;
            newParam.channels[c].noise = false;
            newParam.channels[c].nfrq = 0;
        }
        newParam.mvolL = 0;
        newParam.mvolR = 0;
        newParam.LfoCtrl = 0;
        newParam.LfoFrq = 0;
    }
}
