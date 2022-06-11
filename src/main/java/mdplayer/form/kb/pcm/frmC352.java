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
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmC352 extends frmBase {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.C352 newParam = null;
    private MDChipParams.C352 oldParam = new MDChipParams.C352();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmC352.class);

    public frmC352(frmMain frm, int chipID, int zoom, MDChipParams.C352 newParam, MDChipParams.C352 oldParam) {
        super(frm);

        initializeComponent();

        parent = frm;
        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getplaneC352(), null, zoom);
        screenInit();
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
                parent.setting.getLocation().getPosC352()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosC352()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneC352().getWidth() * zoom, frameSizeH + Resources.getplaneC352().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneC352().getWidth() * zoom, frameSizeH + Resources.getplaneC352().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneC352().getWidth() * zoom, frameSizeH + Resources.getplaneC352().getHeight() * zoom));
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
                    for (ch = 0; ch < 32; ch++) {
                        if (newParam.channels[ch].mask == true)
                            parent.resetChannelMask(EnmChip.C352, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.C352, chipID, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 32) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.C352, chipID, ch);
                    return;
                }

                for (ch = 0; ch < 32; ch++) parent.resetChannelMask(EnmChip.C352, chipID, ch);
                return;

            }
        }
    };

    public void screenInit() {
        Boolean C352Type = false;// (chipID == 0) ? parent.setting.C352Type.UseScci : parent.setting.C352SType.UseScci;
        int tp = C352Type ? 1 : 0;
        for (int ch = 0; ch < 32; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            //DrawBuff.drawFont8(frameBuffer, 296, ch * 8 + 8, 1, "   ");
            DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
            DrawBuff.ChC140_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
            //DrawBuff.Volume(frameBuffer, ch, 1,d, 0, tp);
            //DrawBuff.Volume(frameBuffer, ch, 2,d, 0, tp);
        }
    }

    private int searchC352Note(int freq) {
        double m = Double.MAX_VALUE;

        int clock = Audio.clockC352;

        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            int a = (int) (
                    0x10000 //1sample進むのに必要なカウント数
                            //8000.0
                            //Tables.pcmMulTbl[i % 12 + 12]
                            //Math.pow(2, (i / 12 - 3 + 2))
                            / clock
            );

            if (freq > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public void screenChangeParams() {
        int[] c352Register = Audio.getC352Register(chipID);
        int[] c352key = Audio.getC352KeyOn(chipID);

        for (int ch = 0; ch < 32; ch++) {
            newParam.channels[ch].note = searchC352Note(c352Register[ch * 8 + 2]);

            if (c352key != null) {
                newParam.channels[ch].pan = ((c352Register[ch * 8 + 0] >> 12) & 0xf) | (((((short) c352Register[ch * 8 + 0] & 0xff) >> 4) & 0xf) << 4);
                if ((c352Register[ch * 8 + 3] & 0x4000) != 0 && (c352key[ch] & 0x8000) != 0) {
                    newParam.channels[ch].volumeL = Common.Range((int) (((short) c352Register[ch * 8 + 0] >> 8) / 11.7), 0, 19);
                    newParam.channels[ch].volumeR = Common.Range((int) (((short) c352Register[ch * 8 + 0] & 0xff) / 11.7), 0, 19);
                    newParam.channels[ch].volumeRL = Common.Range((int) (((short) c352Register[ch * 8 + 1] >> 8) / 11.7), 0, 19);
                    newParam.channels[ch].volumeRR = Common.Range((int) (((short) c352Register[ch * 8 + 1] & 0xff) / 11.7), 0, 19);
                }

                if (newParam.channels[ch].mask == null || newParam.channels[ch].mask == true) {
                    newParam.channels[ch].pan = 0;
                }

                if ((c352key[ch] & 0x8000) == 0) {
                    newParam.channels[ch].note = -1;

                    c352Register[ch * 8 + 3] = (int) (c352Register[ch * 8 + 3] & 0xbfff);
                    if (newParam.channels[ch].volumeL > 0) newParam.channels[ch].volumeL--;
                    if (newParam.channels[ch].volumeR > 0) newParam.channels[ch].volumeR--;
                    if (newParam.channels[ch].volumeRL > 0) newParam.channels[ch].volumeRL--;
                    if (newParam.channels[ch].volumeRR > 0) newParam.channels[ch].volumeRR--;
                }
            }

            int d = c352Register[ch * 8 + 3];
            newParam.channels[ch].bit[0] = (d & 0x8000) != 0;
            newParam.channels[ch].bit[1] = (d & 0x4000) != 0;
            newParam.channels[ch].bit[2] = (d & 0x2000) != 0;
            newParam.channels[ch].bit[3] = (d & 0x1000) != 0;
            newParam.channels[ch].bit[4] = (d & 0x0800) != 0;
            newParam.channels[ch].bit[5] = (d & 0x0400) != 0;
            newParam.channels[ch].bit[6] = (d & 0x0200) != 0;
            newParam.channels[ch].bit[7] = (d & 0x0100) != 0;
            newParam.channels[ch].bit[8] = (d & 0x0080) != 0;
            newParam.channels[ch].bit[9] = (d & 0x0040) != 0;
            newParam.channels[ch].bit[10] = (d & 0x0020) != 0;
            newParam.channels[ch].bit[11] = (d & 0x0010) != 0;
            newParam.channels[ch].bit[12] = (d & 0x0008) != 0;
            newParam.channels[ch].bit[13] = (d & 0x0004) != 0;
            newParam.channels[ch].bit[14] = (d & 0x0002) != 0;
            newParam.channels[ch].bit[15] = (d & 0x0001) != 0;

            newParam.channels[ch].freq = c352Register[ch * 8 + 2];
            newParam.channels[ch].bank = c352Register[ch * 8 + 4];
            newParam.channels[ch].sadr = c352Register[ch * 8 + 5];
            newParam.channels[ch].eadr = c352Register[ch * 8 + 6];
            newParam.channels[ch].ladr = c352Register[ch * 8 + 7];
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 32; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            DrawBuff.VolumeXY(frameBuffer, 105, ch * 2 + 2, 1, oyc.volumeL, nyc.volumeL, 0);//Front
            DrawBuff.VolumeXY(frameBuffer, 105, ch * 2 + 3, 1, oyc.volumeR, nyc.volumeR, 0);//Front
            DrawBuff.VolumeXY(frameBuffer, 115, ch * 2 + 2, 1, oyc.volumeRL, nyc.volumeRL, 0);//Rear
            DrawBuff.VolumeXY(frameBuffer, 115, ch * 2 + 3, 1, oyc.volumeRR, nyc.volumeRR, 0);//Rear
            for (int b = 0; b < 16; b++) {
                DrawBuff.drawNESSw(frameBuffer, 64 * 4 + b * 4, ch * 8 + 8
                        , oldParam.channels[ch].bit[b], newParam.channels[ch].bit[b]);
            }
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 81, ch * 8 + 8, 0, oyc.freq, nyc.freq);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 86, ch * 8 + 8, 0, oyc.bank, nyc.bank);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 91, ch * 8 + 8, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 96, ch * 8 + 8, 0, oyc.eadr, nyc.eadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 101, ch * 8 + 8, 0, oyc.ladr, nyc.ladr);
            DrawBuff.KeyBoardToC352(frameBuffer, ch, oyc.note, nyc.note, 0);
            DrawBuff.ChC352(frameBuffer, ch, oyc.mask, nyc.mask, 0);
            DrawBuff.PanType2(frameBuffer, ch, oyc.pan, nyc.pan, 0);
        }
    }

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmC352));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneC352();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(524, 264));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmC352
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(524, 264));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmC352");
        this.setTitle("C352");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
