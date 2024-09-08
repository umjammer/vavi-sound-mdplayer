package mdplayer.form.kb.pcm;

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

import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.chips.Rf5c68;


public class frmRf5c68 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipId = 0;
    private int zoom = 1;

    private MDChipParams.RF5C68 newParam;
    private MDChipParams.RF5C68 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmRf5c68.class);

    public frmRf5c68(frmMain frm, int chipId, int zoom, MDChipParams.RF5C68 newParam, MDChipParams.RF5C68 oldParam) {
        super(frm);
        this.chipId = chipId;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneC(), null, zoom);
        DrawBuff.screenInitRF5C68(frameBuffer);
        update();
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosRf5c68()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosRf5c68()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneC().getWidth() * zoom, frameSizeH + Resources.getPlaneC().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneC().getWidth() * zoom, frameSizeH + Resources.getPlaneC().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneC().getWidth() * zoom, frameSizeH + Resources.getPlaneC().getHeight() * zoom));
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
        Rf5c68 rf5c68Register = audio.getRf5c68Register(chipId);
        if (rf5c68Register != null) {
            //int[][] rf5c164Vol = audio.GetRf5c164Volume(chipId);
            for (int ch = 0; ch < 8; ch++) {
                if (newParam.channels[ch].volume > 0) newParam.channels[ch].volume--;

                if (rf5c68Register.getChannel(ch).enable != 0) {
                    newParam.channels[ch].note = searchRf5c68Note(rf5c68Register.getChannel(ch).step);
                    if (rf5c68Register.getChannel(ch).keyOn) {
                        newParam.channels[ch].volume = rf5c68Register.getChannel(ch).env;
                        rf5c68Register.getChannel(ch).keyOn = false;
                    }
                    int MUL_L = (newParam.channels[ch].volume * (rf5c68Register.getChannel(ch).pan & 0x0F)) >> 5;
                    int MUL_R = (newParam.channels[ch].volume * (rf5c68Register.getChannel(ch).pan >> 4)) >> 5;
                    newParam.channels[ch].volumeL = Math.min(Math.max(MUL_L / 3, 0), 19);
                    newParam.channels[ch].volumeR = Math.min(Math.max(MUL_R / 3, 0), 19);
                } else {
                    newParam.channels[ch].volume = 0;
                    newParam.channels[ch].volumeL = 0;
                    newParam.channels[ch].volumeR = 0;
                }
                if (newParam.channels[ch].volumeL == 0 && newParam.channels[ch].volumeR == 0)
                    newParam.channels[ch].note = -1;
                else if (!rf5c68Register.getChannel(ch).key) {
                    newParam.channels[ch].note = -1;
                    newParam.channels[ch].volume = 0;
                    newParam.channels[ch].volumeL = 0;
                    newParam.channels[ch].volumeR = 0;
                }

                newParam.channels[ch].pan = rf5c68Register.getChannel(ch).pan;
            }
        }
    }

    public void screenDrawParams() {
        for (int c = 0; c < 8; c++) {

            MDChipParams.Channel orc = oldParam.channels[c];
            MDChipParams.Channel nrc = newParam.channels[c];

            orc.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 1, orc.volumeL, nrc.volumeL, 0);
            orc.volumeR = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 2, orc.volumeR, nrc.volumeR, 0);
            DrawBuff.keyBoard(frameBuffer, c, orc.note, nrc.note, 0);
            DrawBuff.PanType2(frameBuffer, c, orc.pan, nrc.pan, 0);
            DrawBuff.ChRF5C164(frameBuffer, c, orc.mask, nrc.mask, 0);
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
                    for (ch = 0; ch < 8; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.RF5C68, chipId, ch);
                        else
                            parent.setChannelMask(EnmChip.RF5C68, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ev.getButton() == MouseEvent.BUTTON1) {
                parent.setChannelMask(EnmChip.RF5C68, chipId, ch);
                return;
            }

            for (ch = 0; ch < 8; ch++) parent.resetChannelMask(EnmChip.RF5C68, chipId, ch);
        }
    };

    private int searchRf5c68Note(int freq) {
        double m = Double.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            double a = Math.abs(freq - (0x0800 * Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((i / 12) - 4))));
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
        }
    }


    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmRf5c68));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneC();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 72));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmRf5c68
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 72));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setMaximumSize(new Dimension(336, 111));
        this.setMinimumSize(new Dimension(336, 111));
        this.setName("frmRf5c68");
        this.setTitle("RF5C68");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
