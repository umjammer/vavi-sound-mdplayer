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

import mdplayer.Audio;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.OkiM6295;


public class frmOKIM6295 extends frmBase {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.OKIM6295 newParam;
    private MDChipParams.OKIM6295 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmOKIM6295.class);

    public frmOKIM6295(frmMain frm, int chipID, int zoom, MDChipParams.OKIM6295 newParam, MDChipParams.OKIM6295 oldParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getplaneMSM6295(), null, zoom);
        DrawBuff.screenInitOKIM6295(frameBuffer);
        update();
    }

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected Boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosOKIM6295()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosOKIM6295()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneMSM6295().getWidth() * zoom, frameSizeH + Resources.getplaneMSM6295().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneMSM6295().getWidth() * zoom, frameSizeH + Resources.getplaneMSM6295().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneMSM6295().getWidth() * zoom, frameSizeH + Resources.getplaneMSM6295().getHeight() * zoom));
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
        OkiM6295.OkiM6295State.ChannelInfo info = Audio.getOKIM6295Info(chipID);
        if (info == null) return;

        for (int c = 0; c < 4; c++) {
            MDChipParams.Channel nyc = newParam.channels[c];

            if (info.keyon[c]) {
                nyc.volume = 19;
            } else {
                nyc.volume -= (nyc.volume > 0) ? 1 : 0;
            }
            nyc.sadr = info.chInfo[c].stAdr;
            nyc.eadr = info.chInfo[c].edAdr;
        }

        newParam.masterClock = info.masterClock;
        newParam.pin7State = info.pin7State;
        newParam.nmkBank[0] = info.nmkBank[0];
        newParam.nmkBank[1] = info.nmkBank[1];
        newParam.nmkBank[2] = info.nmkBank[2];
        newParam.nmkBank[3] = info.nmkBank[3];
    }

    public void screenDrawParams() {
        int tp = parent.setting.getHuC6280Type()[0].getUseReal()[0] ? 1 : 0;

        for (int c = 0; c < 4; c++) {
            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            DrawBuff.ChOKIM6295(frameBuffer, c, oyc.mask, nyc.mask, tp);
            DrawBuff.VolumeToOKIM6295(frameBuffer, c, oyc.volume, nyc.volume);
            DrawBuff.font4Hex20Bit(frameBuffer, 36, 8 + c * 8, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex20Bit(frameBuffer, 60, 8 + c * 8, 0, oyc.eadr, nyc.eadr);

            DrawBuff.font4HexByte(frameBuffer, 36 + c * 16, 48, 0, oldParam.nmkBank[c], newParam.nmkBank[c]);
        }

        DrawBuff.font4Hex32Bit(frameBuffer, 24, 40, 0, oldParam.masterClock, newParam.masterClock);
        DrawBuff.font4HexByte(frameBuffer, 80, 40, 0, oldParam.pin7State, newParam.pin7State);
    }

    public void screenInit() {
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
                    for (int ch = 0; ch < 4; ch++) {
                        if (newParam.channels[ch].mask == true)
                            parent.resetChannelMask(EnmChip.OKIM6295, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.OKIM6295, chipID, ch);
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
                    parent.setChannelMask(EnmChip.OKIM6295, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 4; ch++) parent.resetChannelMask(EnmChip.OKIM6295, chipID, ch);
                return;
            }
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmOKIM6295));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneMSM6295();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 40));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmOKIM6295
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 40));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmOKIM6295");
        this.setTitle("OKIM6295");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
