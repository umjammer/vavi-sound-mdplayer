package mdplayer.form.kb.pcm;

import mdplayer.ScreenPanel;
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
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.chips.Rf5C164Chip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.chips.ScdPcm;

public class frmMegaCD extends frmChipBase<MDChipParams.RF5C164> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmMegaCD.class);

    public frmMegaCD(frmMain frm, int chipId, int zoom, MDChipParams.RF5C164 newParam, MDChipParams.RF5C164 oldParam) {
        super(frm, chipId, zoom, newParam, oldParam);

        initializeComponent();

        frameBuffer.Add(pbScreen, Resources.getPlaneC(), null, zoom);
        DrawBuff.screenInitRF5C164(frameBuffer);
        update();
    }

//    @Override

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosRf5c164()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosRf5c164()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneC().getWidth() * zoom, frameSizeH + Resources.getPlaneC().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneC().getWidth() * zoom, frameSizeH + Resources.getPlaneC().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneC().getWidth() * zoom, frameSizeH + Resources.getPlaneC().getHeight() * zoom));
        componentListener.componentResized(null);
    }

    private final ComponentListener componentListener = new ComponentAdapter() {
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
        Map<String, Object> rf5c164Register = audio.plugin.chipRegister.chip(Rf5C164Chip.class).getInfo(chipId);
        for (int ch = 0; ch < 8; ch++) {
            if ((boolean) rf5c164Register.get("channels." + ch + ".enable")) {
                newParam.channels[ch].note = searchRf5c164Note((int) rf5c164Register.get("channels." + ch + ".stepB"));
                newParam.channels[ch].volumeL = Math.clamp((int) rf5c164Register.get("channels." + ch + ".mulL") / 3, 0, 19);
                newParam.channels[ch].volumeR = Math.clamp((int) rf5c164Register.get("channels." + ch + ".mulR") / 3, 0, 19);
            } else {
                newParam.channels[ch].note = -1;
                newParam.channels[ch].volumeL = 0;
                newParam.channels[ch].volumeR = 0;
            }
            newParam.channels[ch].pan = (int) rf5c164Register.get("channels." + ch + ".pan");
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

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;
            int ch;
            // For top label row, do nothing
            if (py < 1 * 8) {
                // However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (ch = 0; ch < 8; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(Rf5C164Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(Rf5C164Chip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ev.getButton() == MouseEvent.BUTTON1) {
                parent.setChannelMask(Rf5C164Chip.class, chipId, ch);
                return;
            }

            for (ch = 0; ch < 8; ch++) parent.resetChannelMask(Rf5C164Chip.class, chipId, ch);
        }
    };

    private static int searchRf5c164Note(int freq) {
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
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmMegaCD));
        this.pbScreen = new ScreenPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneC();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 72));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmMegaCD
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 72));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage(Resources.getFeli128());
//        this.MaximizeBox = false;
        this.setMaximumSize(new Dimension(336, 111));
        this.setMinimumSize(new Dimension(336, 111));
        this.setName("frmMegaCD");
        this.setTitle("MEGA-CD");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
}

