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
import mdsound.np.chip.NesN106;


public class frmN106 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.N106 newParam = null;
    private MDChipParams.N106 oldParam = null;
    private FrameBuffer frameBuffer = new FrameBuffer();
    static Preferences prefs = Preferences.userNodeForPackage(frmN106.class);

    public frmN106(frmMain frm, int chipID, int zoom, MDChipParams.N106 newParam, MDChipParams.N106 oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getplaneN106(), null, zoom);
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
                parent.setting.getLocation().getPosN106()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosN106()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneN106().getWidth() * zoom, frameSizeH + Resources.getplaneN106().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneN106().getWidth() * zoom, frameSizeH + Resources.getplaneN106().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneN106().getWidth() * zoom, frameSizeH + Resources.getplaneN106().getHeight() * zoom));
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
                    for (ch = 0; ch < 8; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.N163, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.N163, chipID, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;
            int m = ch % 3;
            ch /= 3;

            if (ev.getButton() == MouseEvent.BUTTON2) {
                for (int i = 0; i < 8; i++) {
                    //マスク解除
                    parent.resetChannelMask(EnmChip.N163, chipID, i);
                }
                return;
            }

            if (m != 0) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.N163, ch, chipID);
            } else {
                //マスク
                parent.setChannelMask(EnmChip.N163, chipID, ch);
            }
        }
    };

    public void screenInit() {
        boolean N106Type = false;
        int tp = N106Type ? 1 : 0;
        for (int ch = 0; ch < 8; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 24 + 8, kt, tp);
            }
        }
    }

    public void screenChangeParams() {
        NesN106.TrackInfo[] info = (NesN106.TrackInfo[]) Audio.getN106Register(0);
        if (info == null) return;

        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 8; ch++) {
            nyc = newParam.channels[ch];

            nyc.bit[0] = info[ch].getKeyStatus();
            nyc.bit[1] = info[ch].getHalt();

            int v = info[ch].getVolume() * 2;
            nyc.volume = Math.min(v, 19);
            nyc.volumeR = info[ch].getVolume();

            nyc.freq = info[ch].getFreq();
            v = info[ch].getNote(info[ch].getFreqHz()) - 4 * 12;
            nyc.note = (nyc.volumeL == 0 || !nyc.bit[0]) ? -1 : v;

            nyc.bank = info[ch].wavelen & 127;
            nyc.bank = nyc.bank <= 0 ? (info[ch].wavelen > 127 ? 127 : 0) : nyc.bank;
            if (nyc.aryWave16bit == null) nyc.aryWave16bit = new short[280];
            for (int i = 0; i < 280; i++) {
                if (i < nyc.bank) {
                    nyc.aryWave16bit[i] = info[ch].wave[i];
                } else {
                    if (i != 279) nyc.aryWave16bit[i] = nyc.aryWave16bit[i + 1];
                    else {
                        int w = ((byte) info[ch].getOutput() >> 4) + 8;
                        nyc.aryWave16bit[i] = (short) (w + 16);
                    }
                }
            }
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 8; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            //Enable
            DrawBuff.drawNESSw(frameBuffer, 6 * 4, ch * 24 + 8
                    , oldParam.channels[ch].bit[1], newParam.channels[ch].bit[1]);

            //Key
            DrawBuff.drawNESSw(frameBuffer, 7 * 4, ch * 24 + 8
                    , oldParam.channels[ch].bit[0], newParam.channels[ch].bit[0]);

            //vol
            DrawBuff.volume(frameBuffer, 256, 8 + ch * 3 * 8, 0, oyc.volume, nyc.volume, 0);
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 4, ch * 24 + 16, 0, oyc.volumeR, nyc.volumeR);

            //freq
            DrawBuff.font4Hex20Bit(frameBuffer, 4 * 4, ch * 24 + 24, 0, oyc.freq, nyc.freq);

            //Note
            DrawBuff.keyBoard(frameBuffer, ch * 3, oyc.note, nyc.note, 0);

            if (oyc.aryWave16bit == null && nyc.aryWave16bit != null)
                oyc.aryWave16bit = new short[nyc.aryWave16bit.length];
            DrawBuff.WaveFormToN106(frameBuffer, 10 * 4, ch * 24 + 16, oyc.aryWave16bit, nyc.aryWave16bit);
            DrawBuff.ChN163(frameBuffer, ch, oldParam.channels[ch].mask, newParam.channels[ch].mask, 0);

        }
    }

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneN106();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 198));
        // this.pbScreen.TabIndex = 3
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmN106
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 198));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmN106");
        this.setTitle("N163(N106)");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
