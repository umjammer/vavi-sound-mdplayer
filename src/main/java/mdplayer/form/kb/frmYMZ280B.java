package mdplayer.form.kb;

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


public class frmYMZ280B extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.YMZ280B newParam = null;
    private MDChipParams.YMZ280B oldParam = new MDChipParams.YMZ280B();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYMZ280B.class);

    public frmYMZ280B(frmMain frm, int chipID, int zoom, MDChipParams.YMZ280B newParam, MDChipParams.YMZ280B oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getplaneYMZ280B(), null, zoom);
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
                parent.setting.getLocation().getPosYMZ280B()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYMZ280B()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneYMZ280B().getWidth() * zoom, frameSizeH + Resources.getplaneYMZ280B().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneYMZ280B().getWidth() * zoom, frameSizeH + Resources.getplaneYMZ280B().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneYMZ280B().getWidth() * zoom, frameSizeH + Resources.getplaneYMZ280B().getHeight() * zoom));
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
                            parent.resetChannelMask(EnmChip.YMZ280B, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YMZ280B, chipID, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 8) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.YMZ280B, chipID, ch);
                    return;
                }

                for (ch = 0; ch < 8; ch++) parent.resetChannelMask(EnmChip.YMZ280B, chipID, ch);
            }
        }
    };

    public void screenInit() {
    }

    public void screenChangeParams() {
        int[] reg = Audio.getYMZ280BRegister(chipID);
        if (reg == null) return;

        for (int ch = 0; ch < 8; ch++) {
            newParam.channels[ch].freq = (byte) reg[0x0 + ch * 4]
                    + ((reg[0x1 + ch * 4] & 1) << 8);
            newParam.channels[ch].nfrq = (byte) reg[0x2 + ch * 4];
            newParam.channels[ch].pan = (byte) (reg[0x3 + ch * 4] & 0xf);
            newParam.channels[ch].sadr = ((byte) reg[0x20 + ch * 4] << 16)
                    + ((byte) reg[0x40 + ch * 4] << 8)
                    + (byte) reg[0x60 + ch * 4];
            newParam.channels[ch].ladr = ((byte) reg[0x21 + ch * 4] << 16)
                    + ((byte) reg[0x41 + ch * 4] << 8)
                    + (byte) reg[0x61 + ch * 4];
            newParam.channels[ch].leadr = ((byte) reg[0x22 + ch * 4] << 16)
                    + ((byte) reg[0x42 + ch * 4] << 8)
                    + (byte) reg[0x62 + ch * 4];
            newParam.channels[ch].eadr = ((byte) reg[0x23 + ch * 4] << 16)
                    + ((byte) reg[0x43 + ch * 4] << 8)
                    + (byte) reg[0x63 + ch * 4];

            newParam.channels[ch].dda = (reg[0x1 + ch * 4] & 0x80) != 0;
            newParam.channels[ch].ex = (reg[0x1 + ch * 4] & 0x40) != 0;
            newParam.channels[ch].noise = (reg[0x1 + ch * 4] & 0x20) != 0;
            newParam.channels[ch].loopFlg = (reg[0x1 + ch * 4] & 0x10) != 0;
        }
    }

    public void screenDrawParams() {
        for (int ch = 0; ch < 8; ch++) {
            MDChipParams.Channel orc = oldParam.channels[ch];
            MDChipParams.Channel nrc = newParam.channels[ch];

            DrawBuff.font4Hex4Bit(frameBuffer, 4 * 7, ch * 8 + 8, 0, orc.pan, nrc.pan);
            DrawBuff.drawNESSw(frameBuffer, 4 * 8, ch * 8 + 8, orc.dda, nrc.dda);
            DrawBuff.drawNESSw(frameBuffer, 4 * 9, ch * 8 + 8, orc.ex, nrc.ex);
            DrawBuff.drawNESSw(frameBuffer, 4 * 10, ch * 8 + 8, orc.noise, nrc.noise);
            DrawBuff.drawNESSw(frameBuffer, 4 * 11, ch * 8 + 8, orc.loopFlg, nrc.loopFlg);
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 13, ch * 8 + 8, 0, orc.sadr, nrc.sadr);
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 20, ch * 8 + 8, 0, orc.ladr, nrc.ladr);
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 27, ch * 8 + 8, 0, orc.leadr, nrc.leadr);
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 34, ch * 8 + 8, 0, orc.eadr, nrc.eadr);
            DrawBuff.font4Hex12Bit(frameBuffer, 4 * 41, ch * 8 + 8, 0, orc.freq, nrc.freq);//PITCH
            DrawBuff.font4HexByte(frameBuffer, 4 * 45, ch * 8 + 8, 0, orc.nfrq, nrc.nfrq);//TL
        }
    }

    private void initializeComponent() {
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYMZ280B));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneYMZ280B();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(240, 72));
        // this.pbScreen.TabIndex = 2
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYMZ280B
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(240, 72));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYMZ280B");
        this.setTitle("YMZ280B");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//        this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
