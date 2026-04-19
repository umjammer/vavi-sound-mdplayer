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
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.Common;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.chips.YmF271Chip;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.instrument.YmF271Inst;


public class frmYMF271 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipId = 0;
    private int zoom = 1;

    private final MDChipParams.YMF271 newParam;
    private final MDChipParams.YMF271 oldParam;
    private final FrameBuffer frameBuffer = new FrameBuffer();

    static final Preferences prefs = Preferences.userNodeForPackage(frmYMF271.class);

    public frmYMF271(frmMain frm, int chipId, int zoom, MDChipParams.YMF271 newParam, MDChipParams.YMF271 oldParam) {
        super(frm);

        this.chipId = chipId;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneYMF271(), null, zoom);
        screenInitYMF271(frameBuffer);
        update();
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosYMF271()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosYMF271()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        frameBuffer.refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneYMF271().getWidth() * zoom, frameSizeH + Resources.getPlaneYMF271().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneYMF271().getWidth() * zoom, frameSizeH + Resources.getPlaneYMF271().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneYMF271().getWidth() * zoom, frameSizeH + Resources.getPlaneYMF271().getHeight() * zoom));
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

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
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
        Map<String, Object> reg = audio.plugin.chipRegister.chip(YmF271Chip.class).getInfo(chipId);
        for (int i = 0; i < 48; i++) {
            int slot = YmF271Inst.slotTbl[i];

            int volume = (int) reg.get("slots." + slot + ".volume");
            int ch0Level = (int) reg.get("slots." + slot + ".ch0Level");
            int ch1Level = (int) reg.get("slots." + slot + ".ch1Level");
            newParam.channels[slot].volumeL = Math.clamp(((long) volume * ch0Level) >> 23, 0, 19);
            newParam.channels[slot].volumeR = Math.clamp(((long) volume * ch1Level) >> 23, 0, 19);
            newParam.channels[slot].pan = (int) reg.get("slots." + slot + ".pan");
            newParam.channels[slot].pantp = (int) reg.get("slots." + slot + ".pantp");
            newParam.channels[slot].inst[0] = (int) reg.get("slots." + slot + ".inst.0");
            newParam.channels[slot].inst[1] = (int) reg.get("slots." + slot + ".inst.1");
            newParam.channels[slot].inst[2] = (int) reg.get("slots." + slot + ".inst.2");
            newParam.channels[slot].inst[3] = (int) reg.get("slots." + slot + ".inst.3");
            newParam.channels[slot].inst[4] = (int) reg.get("slots." + slot + ".inst.4");
            newParam.channels[slot].inst[5] = (int) reg.get("slots." + slot + ".inst.5");
            newParam.channels[slot].inst[6] = (int) reg.get("slots." + slot + ".inst.6");
            newParam.channels[slot].inst[7] = (int) reg.get("slots." + slot + ".inst.7");
            newParam.channels[slot].inst[8] = (int) reg.get("slots." + slot + ".inst.8");
            newParam.channels[slot].inst[9] = (int) reg.get("slots." + slot + ".inst.9");
            newParam.channels[slot].inst[10] = (int) reg.get("slots." + slot + ".inst.10");
            newParam.channels[slot].inst[11] = (int) reg.get("slots." + slot + ".inst.11");
            newParam.channels[slot].inst[12] = (int) reg.get("slots." + slot + ".inst.12");

            newParam.channels[slot].inst[13] = (int) reg.get("slots." + slot + ".inst.13");
            newParam.channels[slot].inst[14] = (int) reg.get("slots." + slot + ".inst.14");

            newParam.channels[slot].inst[15] = (int) reg.get("slots." + slot + ".inst.15");
            newParam.channels[slot].inst[16] = (int) reg.get("slots." + slot + ".inst.16");
            newParam.channels[slot].inst[17] = (int) reg.get("slots." + slot + ".inst.17");

            newParam.channels[slot].inst[18] = (int) reg.get("slots." + slot + ".inst.18");
            newParam.channels[slot].inst[19] = (int) reg.get("slots." + slot + ".inst.19");
            newParam.channels[slot].inst[20] = (int) reg.get("slots." + slot + ".inst.20");
            newParam.channels[slot].inst[21] = (int) reg.get("slots." + slot + ".inst.21");

            newParam.channels[slot].inst[22] = (int) reg.get("slots." + slot + ".inst.22");
            newParam.channels[slot].inst[23] = (int) reg.get("slots." + slot + ".inst.23");
            newParam.channels[slot].inst[24] = (int) reg.get("slots." + slot + ".inst.24");
            newParam.channels[slot].inst[25] = (int) reg.get("slots." + slot + ".inst.25");

            // note
            if ((boolean) reg.get("slots." + slot + ".active")) {
                newParam.channels[slot].volumeL = Math.clamp(((long) volume * ch0Level) >> 23, 0, 19);
                newParam.channels[slot].volumeR = Math.clamp(((long) volume * ch1Level) >> 23, 0, 19);
                newParam.channels[slot].note = Common.searchSSGNote(newParam.channels[slot].inst[14]) + (((newParam.channels[slot].inst[13] + 8) & 0xf) - 11) * 12 - 7;
            } else {
                newParam.channels[slot].volumeL += newParam.channels[slot].volumeL > 0 ? -1 : 0;
                newParam.channels[slot].volumeR += newParam.channels[slot].volumeR > 0 ? -1 : 0;
                newParam.channels[slot].note = -1;
            }

            if (i % 4 == 0) {
                newParam.channels[slot].tn = (int) reg.get("slots." + slot + ".sync");
            }
        }
    }

    public void screenDrawParams() {
        for (int i = 0; i < 48; i++) {
            int slot = YmF271Inst.slotTbl[i];

            MDChipParams.Channel orc = oldParam.channels[slot];
            MDChipParams.Channel nrc = newParam.channels[slot];

            orc.volumeL = DrawBuff.volume(frameBuffer, 273, 8 + i * 8, 1, orc.volumeL, nrc.volumeL, 0);
            orc.volumeR = DrawBuff.volume(frameBuffer, 273, 12 + i * 8, 1, orc.volumeR, nrc.volumeR, 0);
            DrawBuff.font4Int2(frameBuffer, 25, 8 + i * 8, 0, 2, orc.echo, slot + 1); // slotnum
            DrawBuff.PanType2(frameBuffer, 33, 8 + i * 8, orc.pan, nrc.pan, 0);
            DrawBuff.PanType2(frameBuffer, 41, 8 + i * 8, orc.pantp, nrc.pantp, 0);

            DrawBuff.KeyBoardXY(frameBuffer, 49, 8 + i * 8, orc.note, nrc.note, 0);

            DrawBuff.font4Int2(frameBuffer, 357, 8 + i * 8, 0, 2, orc.inst[0], nrc.inst[0]); // AR
            DrawBuff.font4Int2(frameBuffer, 365, 8 + i * 8, 0, 2, orc.inst[1], nrc.inst[1]); // DR
            DrawBuff.font4Int2(frameBuffer, 373, 8 + i * 8, 0, 2, orc.inst[2], nrc.inst[2]); // SR
            DrawBuff.font4Int2(frameBuffer, 381, 8 + i * 8, 0, 2, orc.inst[3], nrc.inst[3]); // RR
            DrawBuff.font4Int2(frameBuffer, 389, 8 + i * 8, 0, 2, orc.inst[4], nrc.inst[4]); // SL
            DrawBuff.font4Int3(frameBuffer, 397, 8 + i * 8, 0, 3, orc.inst[5], nrc.inst[5]); // TL
            DrawBuff.font4Int1(frameBuffer, 413, 8 + i * 8, 0, orc.inst[6], nrc.inst[6]); // KS
            DrawBuff.font4Int2(frameBuffer, 417, 8 + i * 8, 0, 2, orc.inst[7], nrc.inst[7]); // ML
            DrawBuff.font4Int1(frameBuffer, 429, 8 + i * 8, 0, orc.inst[8], nrc.inst[8]); // DT
            DrawBuff.font4Int1(frameBuffer, 437, 8 + i * 8, 0, orc.inst[9], nrc.inst[9]); // WF
            DrawBuff.font4Int1(frameBuffer, 445, 8 + i * 8, 0, orc.inst[10], nrc.inst[10]); // FB
            DrawBuff.font4Int1(frameBuffer, 449, 8 + i * 8, 0, orc.inst[11], nrc.inst[11]); // accon
            DrawBuff.font4Int2(frameBuffer, 453, 8 + i * 8, 0, 2, orc.inst[12], nrc.inst[12]); // algorithm
            DrawBuff.font4Int2(frameBuffer, 465, 8 + i * 8, 0, 2, orc.inst[13], nrc.inst[13]); // algorithm
            DrawBuff.font4Hex12Bit(frameBuffer, 477, 8 + i * 8, 0, orc.inst[14], nrc.inst[14]); // fns
            DrawBuff.font4Hex24Bit(frameBuffer, 497, 8 + i * 8, 0, orc.inst[15], nrc.inst[15]); // startaddr
            DrawBuff.font4Hex24Bit(frameBuffer, 525, 8 + i * 8, 0, orc.inst[16], nrc.inst[16]); // endaddr
            DrawBuff.font4Hex24Bit(frameBuffer, 553, 8 + i * 8, 0, orc.inst[17], nrc.inst[17]); // loopaddr
            DrawBuff.font4Int1(frameBuffer, 581, 8 + i * 8, 0, orc.inst[18], nrc.inst[18]); // fs
            DrawBuff.font4Int1(frameBuffer, 585, 8 + i * 8, 0, orc.inst[19], nrc.inst[19]); // bits
            DrawBuff.font4Int1(frameBuffer, 589, 8 + i * 8, 0, orc.inst[20], nrc.inst[20]); // srcnote
            DrawBuff.font4Int1(frameBuffer, 593, 8 + i * 8, 0, orc.inst[21], nrc.inst[21]); // srcb

            DrawBuff.font4Int3(frameBuffer, 601, 8 + i * 8, 0, 3, orc.inst[22], nrc.inst[22]); // lfofreq
            DrawBuff.font4Int1(frameBuffer, 617, 8 + i * 8, 0, orc.inst[23], nrc.inst[23]); // lfowave
            DrawBuff.font4Int1(frameBuffer, 621, 8 + i * 8, 0, orc.inst[24], nrc.inst[24]); // pms
            DrawBuff.font4Int1(frameBuffer, 625, 8 + i * 8, 0, orc.inst[25], nrc.inst[25]); // ams

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
        this.image = mdplayer.properties.Resources.getPlaneYMF271();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(656, 394));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYMF271
        //
//        this.AutoScaleDimensions = new DimensionF(6F, 12F);
//        this.AutoScaleMode = JAutoScaleMode.Font;
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
