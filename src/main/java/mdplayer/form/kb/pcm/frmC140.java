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
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.plugin.BasePlugin;
import mdplayer.properties.Resources;


public class frmC140 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.C140 newParam = null;
    private MDChipParams.C140 oldParam = new MDChipParams.C140();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmC140.class);

    public frmC140(frmMain frm, int chipID, int zoom, MDChipParams.C140 newParam, MDChipParams.C140 oldParam) {
        super(frm);
        initializeComponent();

        parent = frm;
        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getPlaneF(), null, zoom);
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
                parent.setting.getLocation().getPosC140()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosC140()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneF().getWidth() * zoom, frameSizeH + Resources.getPlaneF().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneF().getWidth() * zoom, frameSizeH + Resources.getPlaneF().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneF().getWidth() * zoom, frameSizeH + Resources.getPlaneF().getHeight() * zoom));
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
                    for (ch = 0; ch < 24; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.C140, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.C140, chipID, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 24) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.C140, chipID, ch);
                    return;
                }

                for (ch = 0; ch < 24; ch++) parent.resetChannelMask(EnmChip.C140, chipID, ch);
            }
        }
    };

    private int searchC140Note(int freq) {
        double m = Double.MAX_VALUE;

        int clock = audio.clockC140;
        if (clock >= 1000000)
            clock = clock / 384;

        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            //double a = Math.abs(freq - ((0x0800 << 2) * Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((int)(i / 12) - 4))));
            int a = (int) (
                    65536.0
                            / 2.0
                            / clock
                    //8000.0
                    //Tables.pcmMulTbl[i % 12 + 12]
                    //Math.pow(2, (i / 12 - 3))
            );
            if (freq > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }


    public void screenInit() {
        boolean C140Type = (chipID == 0) ? parent.setting.getC140Type()[0].getUseReal()[0] : parent.setting.getC140Type()[1].getUseReal()[0];
        int tp = C140Type ? 1 : 0;
        for (int ch = 0; ch < 24; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            DrawBuff.drawFont8(frameBuffer, 396, ch * 8 + 8, 1, "   ");
            DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
            DrawBuff.ChC140_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
            int d = 99;
            DrawBuff.VolumeToC140(frameBuffer, ch, 1, d, 0, tp);
            d = 99;
            DrawBuff.VolumeToC140(frameBuffer, ch, 2, d, 0, tp);
        }
    }

    public void screenChangeParams() {
        byte[] c140State = audio.getC140Register(chipID);
        boolean[] c140KeyOn = audio.getC140KeyOn(chipID);
        if (c140State != null) {
            for (int ch = 0; ch < 24; ch++) {
                int frequency = c140State[ch * 16 + 2] * 256 + c140State[ch * 16 + 3];
                int l = c140State[ch * 16 + 1];
                int r = c140State[ch * 16 + 0];

                newParam.channels[ch].note = searchC140Note(frequency) + 1;
                if (c140KeyOn[ch]) {
                    newParam.channels[ch].volumeL = Math.min(Math.max((int) (l / 13.4) * 3, 0), 19);
                    newParam.channels[ch].volumeR = Math.min(Math.max((int) (r / 13.4) * 3, 0), 19);
                } else {
                    newParam.channels[ch].volumeL -= newParam.channels[ch].volumeL > 0 ? 1 : 0;
                    newParam.channels[ch].volumeR -= newParam.channels[ch].volumeR > 0 ? 1 : 0;
                    if (newParam.channels[ch].volumeL == 0 && newParam.channels[ch].volumeR == 0) {
                        if (c140State[ch * 16 + 5] == 0) {
                            newParam.channels[ch].note = -1;
                        }
                        newParam.channels[ch].volumeL = 0;
                        newParam.channels[ch].volumeR = 0;
                    }
                }
                newParam.channels[ch].pan = ((l >> 4) & 0xf) | (((r >> 4) & 0xf) << 4);

                c140KeyOn[ch] = false;

                newParam.channels[ch].freq = (c140State[ch * 16 + 2] << 8) | c140State[ch * 16 + 3];
                newParam.channels[ch].bank = c140State[ch * 16 + 4];
                byte d = c140State[ch * 16 + 5];
                newParam.channels[ch].bit[0] = (d & 0x10) != 0;
                newParam.channels[ch].bit[1] = (d & 0x08) != 0;
                newParam.channels[ch].sadr = (c140State[ch * 16 + 6] << 8) | c140State[ch * 16 + 7];
                newParam.channels[ch].eadr = (c140State[ch * 16 + 8] << 8) | c140State[ch * 16 + 9];
                newParam.channels[ch].ladr = (c140State[ch * 16 + 10] << 8) | c140State[ch * 16 + 11];

            }
        }
    }

    public void screenDrawParams() {
        int tp = ((chipID == 0) ? parent.setting.getC140Type()[0].getUseReal()[0] : parent.setting.getC140Type()[1].getUseReal()[0]) ? 1 : 0;

        for (int c = 0; c < 24; c++) {

            MDChipParams.Channel orc = oldParam.channels[c];
            MDChipParams.Channel nrc = newParam.channels[c];

            DrawBuff.VolumeToC140(frameBuffer, c, 1, orc.volumeL, nrc.volumeL, tp);
            DrawBuff.VolumeToC140(frameBuffer, c, 2, orc.volumeR, nrc.volumeR, tp);
            DrawBuff.KeyBoardToC140(frameBuffer, c, orc.note, nrc.note, tp);
            DrawBuff.PanType2(frameBuffer, c, orc.pan, nrc.pan, tp);

            DrawBuff.ChC140(frameBuffer, c, orc.mask, nrc.mask, tp);

            DrawBuff.drawNESSw(frameBuffer, 64 * 4, c * 8 + 8, oldParam.channels[c].bit[0], newParam.channels[c].bit[0]);
            DrawBuff.drawNESSw(frameBuffer, 65 * 4, c * 8 + 8, oldParam.channels[c].bit[1], newParam.channels[c].bit[1]);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 67, c * 8 + 8, 0, orc.freq, nrc.freq);
            DrawBuff.font4HexByte(frameBuffer, 4 * 72, c * 8 + 8, 0, orc.bank, nrc.bank);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 75, c * 8 + 8, 0, orc.sadr, nrc.sadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 80, c * 8 + 8, 0, orc.eadr, nrc.eadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 85, c * 8 + 8, 0, orc.ladr, nrc.ladr);
        }
    }

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmC140));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneF();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 201));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmC140
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 201));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setMaximumSize(new Dimension(336, 240));
        this.setMinimumSize(new Dimension(336, 240));
        this.setName("frmC140");
        this.setTitle("C140Inst");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
