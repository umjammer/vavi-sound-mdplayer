package mdplayer.form.kb.driver;

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


public class frmPPZ8 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.PPZ8 newParam = null;
    private MDChipParams.PPZ8 oldParam = new MDChipParams.PPZ8();
    private FrameBuffer frameBuffer = new FrameBuffer();
    static Preferences prefs = Preferences.userNodeForPackage(frmPPZ8.class);

    public frmPPZ8(frmMain frm, int chipID, int zoom, MDChipParams.PPZ8 newParam, MDChipParams.PPZ8 oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getplanePPZ8(), null, zoom);
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
                parent.setting.getLocation().getPosPPZ8()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosPPZ8()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplanePPZ8().getWidth() * zoom, frameSizeH + Resources.getplanePPZ8().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplanePPZ8().getWidth() * zoom, frameSizeH + Resources.getplanePPZ8().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplanePPZ8().getWidth() * zoom, frameSizeH + Resources.getplanePPZ8().getHeight() * zoom));
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
            //int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            int ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 8) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.PPZ8, chipID, ch);
                    return;
                }

                for (ch = 0; ch < 8; ch++) parent.resetChannelMask(EnmChip.PPZ8, chipID, ch);
            }
        }
    };

    private int searchPPZ8Note(int freq) {
        double m = Double.MAX_VALUE;

        int clock = Audio.clockPPZ8;
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
        boolean PPZ8Type = false;//  (chipID == 0) ? parent.setting.PPZ8Type.UseScci : parent.setting.PPZ8SType.UseScci;
        int tp = PPZ8Type ? 1 : 0;
        for (int ch = 0; ch < 8; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            DrawBuff.drawFont8(frameBuffer, 4 * 754, ch * 8 + 8, 1, "   ");
            DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
            //DrawBuff.ChPPZ8_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
            //int d = 99;
            //DrawBuff.VolumeToPPZ8(frameBuffer, ch, 1,d, 0, tp);
            //d = 99;
            //DrawBuff.VolumeToPPZ8(frameBuffer, ch, 2,d, 0, tp);
        }
    }

    public void screenChangeParams() {
        mdsound.PPZ8.PPZ8Status.Channel[] ppz8State = Audio.getPPZ8Register(chipID);
        if (ppz8State == null) return;

        for (int ch = 0; ch < 8; ch++) {
            if (ppz8State.length < ch + 1) continue;
            if (ppz8State[ch] == null) continue;

            newParam.channels[ch].pan =
                    ((ppz8State[ch].pan < 6) ? 0xf : (4 * (9 - ppz8State[ch].pan)))
                            | (((ppz8State[ch].pan > 4) ? 0xf : (4 * ppz8State[ch].pan)) * 0x10);

            if (ppz8State[ch].KeyOn) {
                newParam.channels[ch].volumeL = Math.min((ppz8State[ch].volume * (newParam.channels[ch].pan & 0xf)) / 8, 19);
                newParam.channels[ch].volumeR = Math.min((ppz8State[ch].volume * ((newParam.channels[ch].pan & 0xf0) >> 4)) / 8, 19);
            } else {
                newParam.channels[ch].volumeL -= newParam.channels[ch].volumeL > 0 ? 1 : 0;
                newParam.channels[ch].volumeR -= newParam.channels[ch].volumeR > 0 ? 1 : 0;
            }

            newParam.channels[ch].srcFreq = ppz8State[ch].srcFrequency;
            newParam.channels[ch].freq = ppz8State[ch].frequency;

            newParam.channels[ch].note = Common.searchSegaPCMNote(ppz8State[ch].frequency / (double) 0x8000);
            if (!ppz8State[ch].playing) newParam.channels[ch].note = -1;

            newParam.channels[ch].dda = ppz8State[ch].bank != 0;
            newParam.channels[ch].flg16 = ppz8State[ch].num;

            newParam.channels[ch].sadr = ppz8State[ch].ptr;
            newParam.channels[ch].eadr = ppz8State[ch].end;
            newParam.channels[ch].ladr = ppz8State[ch].loopStartOffset;
            newParam.channels[ch].leadr = ppz8State[ch].loopEndOffset;
            newParam.channels[ch].volumeRL = ppz8State[ch].volume;
            newParam.channels[ch].volumeRR = ppz8State[ch].pan;
        }
    }

    public void screenDrawParams() {
        int tp = 0;// ((chipID == 0) ? parent.setting.PPZ8Type.UseScci : parent.setting.PPZ8SType.UseScci) ? 1 : 0;

        for (int c = 0; c < 8; c++) {

            MDChipParams.Channel orc = oldParam.channels[c];
            MDChipParams.Channel nrc = newParam.channels[c];

            DrawBuff.VolumeXY(frameBuffer, 64, c * 2 + 2, 1, orc.volumeL, nrc.volumeL, tp);
            DrawBuff.VolumeXY(frameBuffer, 64, c * 2 + 3, 1, orc.volumeR, nrc.volumeR, tp);
            DrawBuff.keyBoard(frameBuffer, c, orc.note, nrc.note, tp);
            DrawBuff.PanType3(frameBuffer, c, orc.pan, nrc.pan, tp);

            //DrawBuff.ChC140(frameBuffer, c,orc.mask, nrc.mask, tp);

            DrawBuff.drawNESSw(frameBuffer, 4 * 4, c * 8 + 8 * 10, oldParam.channels[c].dda, newParam.channels[c].dda);

            int dmy;

            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 9, c * 8 + 8 * 10, 0, orc.flg16, nrc.flg16);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 15, c * 8 + 8 * 10, 0, orc.srcFreq, nrc.srcFreq);

            dmy = orc.freq;
            DrawBuff.font4Hex32Bit(frameBuffer, 4 * 21, c * 8 + 8 * 10, 0, dmy, nrc.freq);
            orc.freq = dmy;

            dmy = orc.sadr;
            DrawBuff.font4Hex32Bit(frameBuffer, 4 * 31, c * 8 + 8 * 10, 0, dmy, nrc.sadr);
            orc.sadr = dmy;

            dmy = orc.eadr;
            DrawBuff.font4Hex32Bit(frameBuffer, 4 * 41, c * 8 + 8 * 10, 0, dmy, nrc.eadr);
            orc.eadr = dmy;

            dmy = orc.ladr;
            DrawBuff.font4Hex32Bit(frameBuffer, 4 * 51, c * 8 + 8 * 10, 0, dmy, nrc.ladr);
            orc.ladr = dmy;

            dmy = orc.leadr;
            DrawBuff.font4Hex32Bit(frameBuffer, 4 * 61, c * 8 + 8 * 10, 0, dmy, nrc.leadr);
            orc.leadr = dmy;

            DrawBuff.font4HexByte(frameBuffer, 4 * 71, c * 8 + 8 * 10, 0, orc.volumeRL, nrc.volumeRL);
            DrawBuff.font4HexByte(frameBuffer, 4 * 75, c * 8 + 8 * 10, 0, orc.volumeRR, nrc.volumeRR);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new JPanel();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplanePPZ8();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(321, 145));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmPPZ8
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(321, 145));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmPPZ8");
        this.setTitle("PPZ8");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
    }

    BufferedImage image;
    public JPanel pbScreen;
}
