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

import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.chips.MultiPCM;


public class frmMultiPCM extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipId = 0;
    private int zoom = 1;
    private MDChipParams.MultiPCM newParam = null;
    private MDChipParams.MultiPCM oldParam = new MDChipParams.MultiPCM();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmMultiPCM.class);

    public frmMultiPCM(frmMain frm, int chipId, int zoom, MDChipParams.MultiPCM newParam, MDChipParams.MultiPCM oldParam) {
        super(frm);

        initializeComponent();

        this.chipId = chipId;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getPlaneMultiPCM(), null, zoom);
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
                parent.setting.getLocation().getPosMultiPCM()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosMultiPCM()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneMultiPCM().getWidth() * zoom, frameSizeH + Resources.getPlaneMultiPCM().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneMultiPCM().getWidth() * zoom, frameSizeH + Resources.getPlaneMultiPCM().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneMultiPCM().getWidth() * zoom, frameSizeH + Resources.getPlaneMultiPCM().getHeight() * zoom));
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
                    for (ch = 0; ch < 28; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.MultiPCM, chipId, ch);
                        else
                            parent.setChannelMask(EnmChip.MultiPCM, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 28) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.MultiPCM, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 28; ch++) parent.resetChannelMask(EnmChip.MultiPCM, chipId, ch);

            }
        }
    };

    public void screenInit() {
        boolean multiPCMType = false;// (chipId == 0) ? parent.setting.multiPCMType.UseScci : parent.setting.MultiPCMSType.UseScci;
        int tp = multiPCMType ? 1 : 0;
        for (int ch = 0; ch < 28; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            //DrawBuff.drawFont8(frameBuffer, 296, ch * 8 + 8, 1, "   ");
            //DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
            //DrawBuff.ChMultiPCM_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
            //DrawBuff.Volume(frameBuffer, ch, 1,d, 0, tp);
            //DrawBuff.Volume(frameBuffer, ch, 2,d, 0, tp);
        }
    }

    private int searchMultiPCMNote(int freq) {
        //double m = Double.MAX_VALUE;

        //int clock = audio.clockMultiPCM;

        int n = 0;
        //for (int i = 0; i < 12 * 8; i++)
        //{
        //    int a = (int)(
        //        0x10000 //1sample進むのに必要なカウント数
        //        * 8000.0
        //        * Tables.pcmMulTbl[i % 12 + 12]
        //        * Math.pow(2, (i / 12 - 3 + 2))
        //        / clock
        //        );

        //    if (freq > a)
        //    {
        //        m = a;
        //        n = i;
        //    }
        //}
        return n;
    }

    public void screenChangeParams() {
        MultiPCM multiPCMRegister = audio.getMultiPCMRegister(chipId);
        if (multiPCMRegister == null) return;

        for (int ch = 0; ch < 28; ch++) {
            int oct = ((multiPCMRegister.getSlot(ch).regs[3] >> 4) - 1) & 0xf;
            oct = ((oct & 0x8) != 0) ? (oct - 16) : oct;
            oct = oct + 4; // 基音を o5 にしてます
            int pitch = ((multiPCMRegister.getSlot(ch).regs[3] & 0xf) << 6) | (multiPCMRegister.getSlot(ch).regs[2] >> 2);

            int nt = Math.max(Math.min(oct * 12 + pitch / 85, 7 * 12), 0);
            newParam.channels[ch].note = nt;

            int d = multiPCMRegister.getSlot(ch).pan;
            d = (d == 0) ? 0xf : d;
            newParam.channels[ch].pan = ((((d & 0xc) >> 2) * 4) << 4) | (((d & 0x3) * 4) << 0);

            newParam.channels[ch].bit[0] = (multiPCMRegister.getSlot(ch).regs[4] & 0x80) != 0;
            newParam.channels[ch].freq = ((multiPCMRegister.getSlot(ch).regs[3] & 0xf) << 6) | (multiPCMRegister.getSlot(ch).regs[2] >> 2);
            newParam.channels[ch].bit[1] = (multiPCMRegister.getSlot(ch).regs[5] & 1) != 0; // TL Interpolation
            newParam.channels[ch].inst[1] = (multiPCMRegister.getSlot(ch).regs[5] >> 1) & 0x7f; // TL
            newParam.channels[ch].inst[2] = (multiPCMRegister.getSlot(ch).regs[6] >> 3) & 7; // LFO freq
            newParam.channels[ch].inst[3] = (multiPCMRegister.getSlot(ch).regs[6]) & 7; // PLFO
            newParam.channels[ch].inst[4] = (multiPCMRegister.getSlot(ch).regs[7]) & 7; // ALFO

            if (multiPCMRegister.getSlot(ch).sample != null) {
                newParam.channels[ch].inst[0] = multiPCMRegister.getSlot(ch).regs[1];
                newParam.channels[ch].sadr = multiPCMRegister.getSlot(ch).sample.start;
                newParam.channels[ch].eadr = multiPCMRegister.getSlot(ch).sample.end;
                newParam.channels[ch].ladr = multiPCMRegister.getSlot(ch).sample.loop;
                newParam.channels[ch].inst[5] = multiPCMRegister.getSlot(ch).sample.lfoVib;
                newParam.channels[ch].inst[6] = multiPCMRegister.getSlot(ch).sample.ar;
                newParam.channels[ch].inst[7] = multiPCMRegister.getSlot(ch).sample.dr1;
                newParam.channels[ch].inst[8] = multiPCMRegister.getSlot(ch).sample.dr2;
                newParam.channels[ch].inst[9] = multiPCMRegister.getSlot(ch).sample.dl;
                newParam.channels[ch].inst[10] = multiPCMRegister.getSlot(ch).sample.rr;
                newParam.channels[ch].inst[11] = multiPCMRegister.getSlot(ch).sample.krs;
                newParam.channels[ch].inst[12] = multiPCMRegister.getSlot(ch).sample.am;
            }

            if (newParam.channels[ch].bit[0]) {
                newParam.channels[ch].volumeL =
                        Math.min((int) (((0x7f - newParam.channels[ch].inst[1]) * ((newParam.channels[ch].pan >> 4) & 0xf) / (double) 0xf) / 4.5)
                                , 19);
                newParam.channels[ch].volumeR =
                        Math.min((int) (((0x7f - newParam.channels[ch].inst[1]) * ((newParam.channels[ch].pan) & 0xf) / (double) 0xf) / 4.5)
                                , 19);
            } else {
                newParam.channels[ch].note = -1;
                if (newParam.channels[ch].volumeL > 0) newParam.channels[ch].volumeL--;
                if (newParam.channels[ch].volumeR > 0) newParam.channels[ch].volumeR--;
            }
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 28; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            DrawBuff.PanType2(frameBuffer, ch, oyc.pan, nyc.pan, 0);

            DrawBuff.drawNESSw(frameBuffer, 64 * 4, ch * 8 + 8, oldParam.channels[ch].bit[0], newParam.channels[ch].bit[0]);
            oyc.inst[0] = DrawBuff.font4HexByte(frameBuffer, 4 * 66, ch * 8 + 8, 0, oyc.inst[0], nyc.inst[0]);
            DrawBuff.font4Hex12Bit(frameBuffer, 4 * 69, ch * 8 + 8, 0, oyc.freq, nyc.freq);
            DrawBuff.drawNESSw(frameBuffer, 72 * 4, ch * 8 + 8, oldParam.channels[ch].bit[1], newParam.channels[ch].bit[1]); // TL Interpolation
            oyc.inst[1] = DrawBuff.font4HexByte(frameBuffer, 4 * 74, ch * 8 + 8, 0, oyc.inst[1], nyc.inst[1]); // TL
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 77, ch * 8 + 8, 0, oyc.inst[2], nyc.inst[2]); // LFO freq
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 79, ch * 8 + 8, 0, oyc.inst[3], nyc.inst[3]); // PLFO
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 81, ch * 8 + 8, 0, oyc.inst[4], nyc.inst[4]); // ALFO
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 83, ch * 8 + 8, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 90, ch * 8 + 8, 0, oyc.eadr, nyc.eadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 95, ch * 8 + 8, 0, oyc.ladr, nyc.ladr);
            oyc.inst[5] = DrawBuff.font4HexByte(frameBuffer, 4 * 100, ch * 8 + 8, 0, oyc.inst[5], nyc.inst[5]); // LFOVIB
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 103, ch * 8 + 8, 0, oyc.inst[6], nyc.inst[6]); // AR
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 105, ch * 8 + 8, 0, oyc.inst[7], nyc.inst[7]); // DR1
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 107, ch * 8 + 8, 0, oyc.inst[8], nyc.inst[8]); // DR2
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 109, ch * 8 + 8, 0, oyc.inst[9], nyc.inst[9]); // DL
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 111, ch * 8 + 8, 0, oyc.inst[10], nyc.inst[10]); // RR
            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 113, ch * 8 + 8, 0, oyc.inst[11], nyc.inst[11]); // KRS
            oyc.inst[12] = DrawBuff.font4HexByte(frameBuffer, 4 * 115, ch * 8 + 8, 0, oyc.inst[12], nyc.inst[12]); // AM

            DrawBuff.VolumeXY(frameBuffer, 117, ch * 2 + 2, 1, oyc.volumeL, nyc.volumeL, 0); // Front
            DrawBuff.VolumeXY(frameBuffer, 117, ch * 2 + 3, 1, oyc.volumeR, nyc.volumeR, 0); // Front

            DrawBuff.KeyBoardToMultiPCM(frameBuffer, ch, oyc.note, nyc.note, 0);
            //DrawBuff.ChMultiPCM(frameBuffer, ch,oyc.mask, nyc.mask, 0);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new JPanel();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneMultiPCM();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(527, 225));
        //this.pbScreen.TabIndex = 0
        //this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmMultiPCM
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(527, 225));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmMultiPCM");
        this.setTitle("MultiPCM");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
