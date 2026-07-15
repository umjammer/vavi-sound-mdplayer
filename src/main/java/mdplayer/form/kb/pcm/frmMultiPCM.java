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
import mdplayer.Tables;
import mdplayer.chips.MultiPcmChip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.chips.MultiPCM;

public class frmMultiPCM extends frmChipBase<MDChipParams.MultiPCM> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmMultiPCM.class);

    public frmMultiPCM(frmMain frm, int chipId, int zoom, MDChipParams.MultiPCM newParam, MDChipParams.MultiPCM oldParam) {
        super(frm, chipId, zoom, newParam, new MDChipParams.MultiPCM());

        initializeComponent();

        bind(Resources.getPlaneMultiPCM());
    }

//    @Override

    private final WindowListener windowListener = new WindowAdapter() {
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
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;
            int ch;
            // For top label row, do nothing
            if (py < 1 * 8) {
                // However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (ch = 0; ch < 28; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(MultiPcmChip.class, chipId, ch);
                        else
                            parent.setChannelMask(MultiPcmChip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 28) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(MultiPcmChip.class, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 28; ch++) parent.resetChannelMask(MultiPcmChip.class, chipId, ch);

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

    private static int searchMultiPCMNote(int freq) {
        //double m = Double.MAX_VALUE;

        //int clock = audio.clockMultiPCM;

        int n = 0;
        //for (int i = 0; i < 12 * 8; i++) {
        //    int a = (int)(
        //        0x10000 // Number of counts required to advance 1 sample
        //        * 8000.0
        //        * Tables.pcmMulTbl[i % 12 + 12]
        //        * Math.pow(2, (i / 12 - 3 + 2))
        //        / clock
        //        );

        //    if (freq > a) {
        //        m = a;
        //        n = i;
        //    }
        //}
        return n;
    }

    public void screenChangeParams() {
        Map<String, Object> info = audio.plugin.chipRegister.chip(MultiPcmChip.class).getInfo(chipId);
        if (info == null) return;

        for (int ch = 0; ch < 28; ch++) {
            newParam.channels[ch].pan = (int) info.get("channels." + ch + ".pan");

            newParam.channels[ch].bit[0] = (boolean) info.get("channels." + ch + ".bit.0");
            newParam.channels[ch].freq = (int) info.get("channels." + ch + ".freq");
            newParam.channels[ch].bit[1] = (boolean) info.get("channels." + ch + ".bit.1");
            newParam.channels[ch].inst[1] = (int) info.get("channels." + ch + ".inst.1");
            newParam.channels[ch].inst[2] = (int) info.get("channels." + ch + ".inst.2");
            newParam.channels[ch].inst[3] = (int) info.get("channels." + ch + ".inst.3");
            newParam.channels[ch].inst[4] = (int) info.get("channels." + ch + ".inst.4");

            if (info.get("channels." + ch + ".sadr") != null) {
                newParam.channels[ch].inst[0] = (int) info.get("channels." + ch + ".inst.0");
                newParam.channels[ch].sadr = (int) info.get("channels." + ch + ".sadr");
                newParam.channels[ch].eadr = (int) info.get("channels." + ch + ".eadr");
                newParam.channels[ch].ladr = (int) info.get("channels." + ch + ".ladr");
                newParam.channels[ch].inst[5] = (int) info.get("channels." + ch + ".inst.5");
                newParam.channels[ch].inst[6] = (int) info.get("channels." + ch + ".inst.6");
                newParam.channels[ch].inst[7] = (int) info.get("channels." + ch + ".inst.7");
                newParam.channels[ch].inst[8] = (int) info.get("channels." + ch + ".inst.8");
                newParam.channels[ch].inst[9] = (int) info.get("channels." + ch + ".inst.9");
                newParam.channels[ch].inst[10] = (int) info.get("channels." + ch + ".inst.10");
                newParam.channels[ch].inst[11] = (int) info.get("channels." + ch + ".inst.11");
                newParam.channels[ch].inst[12] = (int) info.get("channels." + ch + ".inst.12");
            }

            if (newParam.channels[ch].bit[0]) {
                newParam.channels[ch].volumeL =
                        Math.min((int) (((0x7f - newParam.channels[ch].inst[1]) * ((newParam.channels[ch].pan >> 4) & 0xf) / (double) 0xf) / 4.5), 19);
                newParam.channels[ch].volumeR =
                        Math.min((int) (((0x7f - newParam.channels[ch].inst[1]) * ((newParam.channels[ch].pan) & 0xf) / (double) 0xf) / 4.5), 19);
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
        this.pbScreen = new ScreenPanel();

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
        this.setIconImage(Resources.getFeli128());
//        this.MaximizeBox = false;
        this.setName("frmMultiPCM");
        this.setTitle("MultiPCM");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
}
