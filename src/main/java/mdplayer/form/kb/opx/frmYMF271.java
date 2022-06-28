package mdplayer.form.kb.opx;

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
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.Ymf271;


public class frmYMF271 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YMF271 newParam;
    private MDChipParams.YMF271 oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYMF271.class);

    private static final int[] slotTbl = new int[] {
            0, 24, 12, 36,
            1, 25, 13, 37,
            2, 26, 14, 38,
            3, 27, 15, 39,

            4, 28, 16, 40,
            5, 29, 17, 41,
            6, 30, 18, 42,
            7, 31, 19, 43,

            8, 32, 20, 44,
            9, 33, 21, 45,
            10, 34, 22, 46,
            11, 35, 23, 47,
    };

    public frmYMF271(frmMain frm, int chipID, int zoom, MDChipParams.YMF271 newParam, MDChipParams.YMF271 oldParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getplaneYMF271(), null, zoom);
        screenInitYMF271(frameBuffer);
        update();
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosYMF271()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYMF271()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneYMF271().getWidth() * zoom, frameSizeH + Resources.getplaneYMF271().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneYMF271().getWidth() * zoom, frameSizeH + Resources.getplaneYMF271().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneYMF271().getWidth() * zoom, frameSizeH + Resources.getplaneYMF271().getHeight() * zoom));
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
        }
    };

    public void screenInitYMF271(FrameBuffer screen) {
        for (int ch = 0; ch < 48; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(screen, 49 + kx, ch * 8 + 8, kt, 0);
            }
            DrawBuff.drawFont8(screen, 313, ch * 8 + 8, 1, "   ");
            DrawBuff.drawPanType2P(screen, 24, ch * 8 + 8, 0, 0);

            oldParam.channels[ch].tn = -1;
        }
    }

    public void screenChangeParams() {
        Ymf271.YMF271Chip reg = Audio.getYMF271Register(chipID);
        if (reg != null) {
            for (int i = 0; i < 48; i++) {
                int slot = slotTbl[i];

                MDChipParams.Channel nrc = newParam.channels[slot];
                Ymf271.YMF271Chip.Slot slt = reg.slots[slot];
                nrc.volumeL = Math.min(Math.max((slt.volume * slt.ch0Level) >> 23, 0), 19);
                nrc.volumeR = Math.min(Math.max((slt.volume * slt.ch1Level) >> 23, 0), 19);
                nrc.pan = (slt.ch1Level << 4) | (slt.ch0Level & 0xf);
                nrc.pantp = (slt.ch3Level & 0xf0) | ((slt.ch2Level >> 4) & 0xf);
                nrc.inst[0] = slt.ar;
                nrc.inst[1] = slt.decay1rate;
                nrc.inst[2] = slt.decay2rate;
                nrc.inst[3] = slt.relrate;
                nrc.inst[4] = slt.decay1lvl;
                nrc.inst[5] = slt.tl;
                nrc.inst[6] = slt.keyScale;
                nrc.inst[7] = slt.multiple;
                nrc.inst[8] = slt.detune;
                nrc.inst[9] = slt.waveForm;
                nrc.inst[10] = slt.feedback;
                nrc.inst[11] = slt.accon;
                nrc.inst[12] = slt.algorithm;

                nrc.inst[13] = slt.block;
                nrc.inst[14] = slt.fns;

                nrc.inst[15] = slt.startAddr;
                nrc.inst[16] = slt.endAddr;
                nrc.inst[17] = slt.loopAddr;

                nrc.inst[18] = slt.fs;
                nrc.inst[19] = slt.bits == 12 ? 1 : 0;
                nrc.inst[20] = slt.srcNote;
                nrc.inst[21] = slt.srcb;

                nrc.inst[22] = slt.lfoFreq;
                nrc.inst[23] = slt.lfoWave;
                nrc.inst[24] = slt.pms;
                nrc.inst[25] = slt.ams;

                //note
                if (slt.active != 0) {
                    nrc.volumeL = Math.min(Math.max((slt.volume * slt.ch0Level) >> 23, 0), 19);
                    nrc.volumeR = Math.min(Math.max((slt.volume * slt.ch1Level) >> 23, 0), 19);
                    nrc.note = Common.searchSSGNote(nrc.inst[14]) + (((nrc.inst[13] + 8) & 0xf) - 11) * 12 - 7;
                } else {
                    nrc.volumeL += nrc.volumeL > 0 ? -1 : 0;
                    nrc.volumeR += nrc.volumeR > 0 ? -1 : 0;
                    nrc.note = -1;
                }

                if (i % 4 == 0) {
                    nrc.tn = reg.groups[i / 4].sync;
                }
            }
        }
    }

    public void screenDrawParams() {
        for (int i = 0; i < 48; i++) {
            int slot = slotTbl[i];

            MDChipParams.Channel orc = oldParam.channels[slot];
            MDChipParams.Channel nrc = newParam.channels[slot];

            DrawBuff.volume(frameBuffer, 273, 8 + i * 8, 1, orc.volumeL, nrc.volumeL, 0);
            DrawBuff.volume(frameBuffer, 273, 12 + i * 8, 1, orc.volumeR, nrc.volumeR, 0);
            DrawBuff.font4Int2(frameBuffer, 25, 8 + i * 8, 0, 2, orc.echo, slot + 1);//slotnum
            DrawBuff.PanType2(frameBuffer, 33, 8 + i * 8, orc.pan, nrc.pan, 0);
            DrawBuff.PanType2(frameBuffer, 41, 8 + i * 8, orc.pantp, nrc.pantp, 0);

            DrawBuff.KeyBoardXY(frameBuffer, 49, 8 + i * 8, orc.note, nrc.note, 0);

            DrawBuff.font4Int2(frameBuffer, 357, 8 + i * 8, 0, 2, orc.inst[0], nrc.inst[0]);//AR
            DrawBuff.font4Int2(frameBuffer, 365, 8 + i * 8, 0, 2, orc.inst[1], nrc.inst[1]);//DR
            DrawBuff.font4Int2(frameBuffer, 373, 8 + i * 8, 0, 2, orc.inst[2], nrc.inst[2]);//SR
            DrawBuff.font4Int2(frameBuffer, 381, 8 + i * 8, 0, 2, orc.inst[3], nrc.inst[3]);//RR
            DrawBuff.font4Int2(frameBuffer, 389, 8 + i * 8, 0, 2, orc.inst[4], nrc.inst[4]);//SL
            DrawBuff.font4Int3(frameBuffer, 397, 8 + i * 8, 0, 3, orc.inst[5], nrc.inst[5]);//TL
            DrawBuff.font4Int1(frameBuffer, 413, 8 + i * 8, 0, orc.inst[6], nrc.inst[6]);//KS
            DrawBuff.font4Int2(frameBuffer, 417, 8 + i * 8, 0, 2, orc.inst[7], nrc.inst[7]);//ML
            DrawBuff.font4Int1(frameBuffer, 429, 8 + i * 8, 0, orc.inst[8], nrc.inst[8]);//DT
            DrawBuff.font4Int1(frameBuffer, 437, 8 + i * 8, 0, orc.inst[9], nrc.inst[9]);//WF
            DrawBuff.font4Int1(frameBuffer, 445, 8 + i * 8, 0, orc.inst[10], nrc.inst[10]);//FB
            DrawBuff.font4Int1(frameBuffer, 449, 8 + i * 8, 0, orc.inst[11], nrc.inst[11]);//accon
            DrawBuff.font4Int2(frameBuffer, 453, 8 + i * 8, 0, 2, orc.inst[12], nrc.inst[12]);//algorithm
            DrawBuff.font4Int2(frameBuffer, 465, 8 + i * 8, 0, 2, orc.inst[13], nrc.inst[13]);//algorithm
            DrawBuff.font4Hex12Bit(frameBuffer, 477, 8 + i * 8, 0, orc.inst[14], nrc.inst[14]);//fns
            DrawBuff.font4Hex24Bit(frameBuffer, 497, 8 + i * 8, 0, orc.inst[15], nrc.inst[15]);//startaddr
            DrawBuff.font4Hex24Bit(frameBuffer, 525, 8 + i * 8, 0, orc.inst[16], nrc.inst[16]);//endaddr
            DrawBuff.font4Hex24Bit(frameBuffer, 553, 8 + i * 8, 0, orc.inst[17], nrc.inst[17]);//loopaddr
            DrawBuff.font4Int1(frameBuffer, 581, 8 + i * 8, 0, orc.inst[18], nrc.inst[18]);//fs
            DrawBuff.font4Int1(frameBuffer, 585, 8 + i * 8, 0, orc.inst[19], nrc.inst[19]);//bits
            DrawBuff.font4Int1(frameBuffer, 589, 8 + i * 8, 0, orc.inst[20], nrc.inst[20]);//srcnote
            DrawBuff.font4Int1(frameBuffer, 593, 8 + i * 8, 0, orc.inst[21], nrc.inst[21]);//srcb

            DrawBuff.font4Int3(frameBuffer, 601, 8 + i * 8, 0, 3, orc.inst[22], nrc.inst[22]);//lfofreq
            DrawBuff.font4Int1(frameBuffer, 617, 8 + i * 8, 0, orc.inst[23], nrc.inst[23]);//lfowave
            DrawBuff.font4Int1(frameBuffer, 621, 8 + i * 8, 0, orc.inst[24], nrc.inst[24]);//pms
            DrawBuff.font4Int1(frameBuffer, 625, 8 + i * 8, 0, orc.inst[25], nrc.inst[25]);//ams

            if (i % 4 == 0) {
                DrawBuff.OpxOP(frameBuffer, 17, 8 + i * 8, 0, orc.tn, nrc.tn & 3);//sync
            }
        }
    }

    private void initializeComponent() {
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYMF271));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneYMF271();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(656, 394));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYMF271
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(689, 477));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYMF271");
        this.setTitle("YMF271");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
