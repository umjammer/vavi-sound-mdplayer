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


public class frmS5B extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.S5B newParam;
    private MDChipParams.S5B oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();
    static Preferences prefs = Preferences.userNodeForPackage(frmS5B.class);

    public frmS5B(frmMain frm, int chipID, int zoom, MDChipParams.S5B newParam, MDChipParams.S5B oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(this.pbScreen, Resources.getplaneS5B(), null, zoom);
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
                parent.setting.getLocation().getPosS5B()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosS5B()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneS5B().getWidth() * zoom, frameSizeH + Resources.getplaneS5B().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneS5B().getWidth() * zoom, frameSizeH + Resources.getplaneS5B().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneS5B().getWidth() * zoom, frameSizeH + Resources.getplaneS5B().getHeight() * zoom));
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
        byte[] S5BRegister = Audio.getS5BRegister(chipID);
        if (S5BRegister == null) return;

        for (int ch = 0; ch < 3; ch++) { //SSG
            MDChipParams.Channel channel = newParam.channels[ch];

            boolean t = (S5BRegister[0x07] & (0x1 << ch)) == 0;
            boolean n = (S5BRegister[0x07] & (0x8 << ch)) == 0;
            //System.err.println("r[8]=%x r[9]=%x r[10]=%x", S5BRegister[0x8], S5BRegister[0x9], S5BRegister[0xa]);
            channel.tn = (t ? 1 : 0) + (n ? 2 : 0);
            newParam.nfrq = S5BRegister[0x06] & 0x1f;
            newParam.efrq = S5BRegister[0x0c] * 0x100 + S5BRegister[0x0b];
            newParam.etype = (S5BRegister[0x0d] & 0xf);

            int v = (S5BRegister[0x08 + ch] & 0x1f);
            v = Math.min(v, 15);
            channel.volume = (int) (((t || n) ? 1 : 0) * v * (20.0 / 16.0));
            if (!t && !n && channel.volume > 0) {
                channel.volume--;
            }

            if (channel.volume == 0) {
                channel.note = -1;
            } else {
                int ft = S5BRegister[0x00 + ch * 2];
                int ct = S5BRegister[0x01 + ch * 2];
                int tp = (ct << 8) | ft;
                if (tp == 0) tp = 1;
                float ftone = Audio.clockS5B / (8.0f * (float) tp);
                channel.note = searchSSGNote(ftone);
            }
        }
    }

    public void screenDrawParams() {
        //int tp = setting.S5BType.UseScci ? 1 : 0;
        int tp = 0;

        for (int c = 0; c < 3; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
            DrawBuff.ToneNoise(frameBuffer, 6, 2, c, oyc.tn, nyc.tn, oyc.tntp, tp);

            DrawBuff.ChS5B(frameBuffer, c, oyc.mask, nyc.mask, tp);

        }

        DrawBuff.Nfrq(frameBuffer, 5, 8, oldParam.nfrq, newParam.nfrq);
        DrawBuff.Efrq(frameBuffer, 18, 8, oldParam.efrq, newParam.efrq);
        DrawBuff.Etype(frameBuffer, 33, 8, oldParam.etype, newParam.etype);
    }

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volume = -1;
            newParam.channels[c].tn = -1;
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, c * 8 + 8, kt, 0);
            }
        }
        newParam.nfrq = 0;
        newParam.efrq = 0;
        newParam.etype = 0;
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
                    for (int ch = 0; ch < 3; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.FME7, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.FME7, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 4 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    parent.setChannelMask(EnmChip.FME7, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 3; ch++) parent.resetChannelMask(EnmChip.FME7, chipID, ch);
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

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
//        this.pbScreen.setBackground(Color.ControlDarkDark);
        this.image = mdplayer.properties.Resources.getplaneS5B();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 40));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmS5B
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 40));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmS5B");
        this.setTitle("S5B(FME)");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
