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
import mdplayer.chips.OkiM6258Chip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;

public class frmOKIM6258 extends frmChipBase<MDChipParams.OKIM6258> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmOKIM6258.class);

    public frmOKIM6258(frmMain frm, int chipId, int zoom, MDChipParams.OKIM6258 newParam) {
        super(frm, chipId, zoom, newParam, new MDChipParams.OKIM6258());

        initializeComponent();

        frameBuffer.Add(pbScreen, Resources.getPlaneMSM6258(), null, zoom);
        DrawBuff.screenInitOKIM6258(frameBuffer);
        update();
    }

//    @Override

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosOKIM6258()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosOKIM6258()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneMSM6258().getWidth() * zoom, frameSizeH + Resources.getPlaneMSM6258().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneMSM6258().getWidth() * zoom, frameSizeH + Resources.getPlaneMSM6258().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneMSM6258().getWidth() * zoom, frameSizeH + Resources.getPlaneMSM6258().getHeight() * zoom));
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
        Map<String, Object> okim6258State = audio.plugin.chipRegister.chip(OkiM6258Chip.class).getInfo(chipId);
        if (okim6258State == null) return;

        switch (((int) okim6258State.get("pan")) & 0x3) {
        case 0:
        case 3:
            newParam.pan = 3;
            break;
        case 1:
            newParam.pan = 2;
            break;
        case 2:
            newParam.pan = 1;
            break;
        }

        newParam.masterFreq = (int) okim6258State.get("masterFreq");
        newParam.divider = (int) okim6258State.get("divider");
        newParam.pbFreq = (int) okim6258State.get("pbFreq");

        int v = (int) (((Math.abs(((int) okim6258State.get("dataIn")) - 128) * 2) >> 3) * 1.2);
        if ((((int) okim6258State.get("status")) & 0x2) == 0) v = 0;
        v = Math.min(v, 38);
        if (newParam.volumeL < v && ((newParam.pan & 0x2) != 0)) {
            newParam.volumeL = v;
        } else {
            newParam.volumeL--;
        }
        if (newParam.volumeR < v && ((newParam.pan & 0x1) != 0)) {
            newParam.volumeR = v;
        } else {
            newParam.volumeR--;
        }
    }

    public void screenDrawParams() {
        MDChipParams.OKIM6258 ost = oldParam;
        MDChipParams.OKIM6258 nst = newParam;

        DrawBuff.PanToOKIM6258(frameBuffer, ost.pan, nst.pan, ost.pantp, 0);

        if (ost.masterFreq != nst.masterFreq) {
            DrawBuff.drawFont4(frameBuffer, 12 * 4, 8, 0, "{%5d}".formatted(nst.masterFreq));
            ost.masterFreq = nst.masterFreq;
        }

        if (ost.divider != nst.divider) {
            DrawBuff.drawFont4(frameBuffer, 19 * 4, 8, 0, "{%5d}".formatted(nst.divider));
            ost.divider = nst.divider;
        }

        if (ost.pbFreq != nst.pbFreq) {
            DrawBuff.drawFont4(frameBuffer, 26 * 4, 8, 0, "{%5d}".formatted(nst.pbFreq));
            ost.pbFreq = nst.pbFreq;
        }

        ost.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + 0 * 8, 1, ost.volumeL, nst.volumeL / 2, 0);
        ost.volumeR = DrawBuff.volume(frameBuffer, 256, 8 + 0 * 8, 2, ost.volumeR, nst.volumeR / 2, 0);

        DrawBuff.ChOKIM6258(frameBuffer, ost.mask, nst.mask, 0);
    }

    public void screenInit() {
        newParam.pan = 3;
        newParam.masterFreq = 0;
        newParam.divider = 0;
        newParam.pbFreq = 0;
        newParam.volumeL = 0;
        newParam.volumeR = 0;
    }

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            //  For top label row, do nothing
            if (py < 1 * 8) {
                //  However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    if (newParam.mask)
                        parent.resetChannelMask(OkiM6258Chip.class, chipId, 0);
                    else
                        parent.setChannelMask(OkiM6258Chip.class, chipId, 0);
                }
                return;
            }

            // keyboard
            if (py < 2 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // mask
                    parent.setChannelMask(OkiM6258Chip.class, chipId, 0);
                    return;
                }

                //  Unmask.
                parent.resetChannelMask(OkiM6258Chip.class, chipId, 0);
            }
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmOKIM6258));
        this.pbScreen = new ScreenPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneMSM6258();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 16));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmOKIM6258
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 16));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage(Resources.getFeli128());
//        this.MaximizeBox = false;
        this.setName("frmOKIM6258");
        this.setTitle("OKIM6258");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
}
