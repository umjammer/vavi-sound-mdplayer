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
import mdsound.np.chip.DeviceInfo.BasicTrackInfo;


public class frmVRC6 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.VRC6 newParam = null;
    private MDChipParams.VRC6 oldParam = null;
    private FrameBuffer frameBuffer = new FrameBuffer();
    static Preferences prefs = Preferences.userNodeForPackage(frmVRC6.class);

    public frmVRC6(frmMain frm, int chipID, int zoom, MDChipParams.VRC6 newParam, MDChipParams.VRC6 oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getPlaneVRC6(), null, zoom);
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
                parent.setting.getLocation().getPosVrc6()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosVrc6()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneVRC6().getWidth() * zoom, frameSizeH + Resources.getPlaneVRC6().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneVRC6().getWidth() * zoom, frameSizeH + Resources.getPlaneVRC6().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneVRC6().getWidth() * zoom, frameSizeH + Resources.getPlaneVRC6().getHeight() * zoom));
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
            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (ch = 0; ch < 3; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.VRC6, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.VRC6, chipID, ch);
                    }
                }
                return;
            }

            if (ev.getButton() == MouseEvent.BUTTON2) {
                for (int i = 0; i < 3; i++) {
                    //マスク解除
                    parent.resetChannelMask(EnmChip.VRC6, chipID, i);
                }

                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;
            ch /= 2;
            if (ev.getButton() == MouseEvent.BUTTON1) {
                //マスク
                parent.setChannelMask(EnmChip.VRC6, chipID, ch);
            }
        }
    };

    public void screenInit() {
        boolean VRC6Type = false;
        int tp = VRC6Type ? 1 : 0;
        for (int ch = 0; ch < 3; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 16 + 8, kt, tp);
            }
        }
    }

    public void screenChangeParams() {
        BasicTrackInfo[] info = (BasicTrackInfo[]) Audio.getVRC6Register(0);
        if (info == null) return;

        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 3; ch++) {
            nyc = newParam.channels[ch];
            nyc.kf = info[ch].getTone();
            nyc.volumeR = info[ch].getTone() / 4;
            nyc.volumeL = info[ch].getVolume();
            int v = info[ch].getVolume();
            v = ch < 2 ? v * 2 : v / 3;
            nyc.volume = Math.min(v, 19);
            nyc.bit[0] = info[ch].getKeyStatus();
            nyc.freq = info[ch].getFreqP();
            nyc.bit[1] = info[ch].getHalt();
            v = info[ch].getNote(info[ch].getFreqHz()) - 4 * 12;
            nyc.note = nyc.volumeL == 0 ? -1 : v;
            nyc.sadr = info[ch].getFreqShift();
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 3; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            DrawBuff.keyBoard(frameBuffer, ch * 2, oyc.note, nyc.note, 0);
            if (ch < 2) {
                DrawBuff.drawDuty(frameBuffer, 24, (1 + ch * 2) * 8, oyc.volumeR, nyc.volumeR);
                DrawBuff.font4Int2(frameBuffer, 6 * 4, ch * 16 + 16, 0, 2, oyc.kf, nyc.kf);
                DrawBuff.font4Int2(frameBuffer, 10 * 4, ch * 16 + 16, 0, 2, oyc.volumeL, nyc.volumeL);
                DrawBuff.volume(frameBuffer, 256, 8 + ch * 2 * 8, 0, oyc.volume, nyc.volume, 0);
                DrawBuff.chVRC6(frameBuffer, ch, oldParam.channels[ch].mask, newParam.channels[ch].mask, 0);
            } else {
                DrawBuff.font4Int2(frameBuffer, 9 * 4, ch * 16 + 16, 0, 3, oyc.volumeL, nyc.volumeL);
                DrawBuff.volume(frameBuffer, 256, 8 + ch * 2 * 8, 0, oyc.volume, nyc.volume, 0);
                DrawBuff.drawNESSw(frameBuffer, 55 * 4, ch * 16 + 16
                        , oldParam.channels[ch].bit[1], newParam.channels[ch].bit[1]);
                DrawBuff.font4Int1(frameBuffer, 62 * 4, ch * 16 + 16, 0, oyc.sadr, nyc.sadr);
                DrawBuff.chVRC6(frameBuffer, ch, oldParam.channels[ch].mask, newParam.channels[ch].mask, 0);
            }

            DrawBuff.drawNESSw(frameBuffer, 13 * 4, ch * 16 + 16
                    , oldParam.channels[ch].bit[0], newParam.channels[ch].bit[0]);

            DrawBuff.font4Hex12Bit(frameBuffer, 16 * 4, ch * 16 + 16, 0, oyc.freq, nyc.freq);

        }
    }

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmVRC6));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneVRC6();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 56));
        // this.pbScreen.TabIndex = 2
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmVRC6
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 56));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmVRC6");
        this.setTitle("VRC6");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
