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

import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmSegaPCM extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipId = 0;
    private int zoom = 1;

    private MDChipParams.SegaPcm newParam = null;
    private MDChipParams.SegaPcm oldParam = null;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmSegaPCM.class);

    public frmSegaPCM(frmMain frm, int chipId, int zoom, MDChipParams.SegaPcm newParam, MDChipParams.SegaPcm oldParam) {
        super(frm);

        this.chipId = chipId;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneSEGAPCM(), null, zoom);
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
                parent.setting.getLocation().getPosSegaPCM()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosSegaPCM()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneSEGAPCM().getWidth() * zoom, frameSizeH + Resources.getPlaneSEGAPCM().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneSEGAPCM().getWidth() * zoom, frameSizeH + Resources.getPlaneSEGAPCM().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneSEGAPCM().getWidth() * zoom, frameSizeH + Resources.getPlaneSEGAPCM().getHeight() * zoom));
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
                    for (ch = 0; ch < 16; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.SEGAPCM, chipId, ch);
                        else
                            parent.setChannelMask(EnmChip.SEGAPCM, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 16) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.SEGAPCM, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 16; ch++) parent.resetChannelMask(EnmChip.SEGAPCM, chipId, ch);

            }
        }
    };

    public void screenInit() {
        boolean SEGAPCMType = (chipId == 0) ? parent.setting.getSEGAPCMType()[0].getUseReal()[0] : parent.setting.getSEGAPCMType()[1].getUseReal()[0];
        int tp = SEGAPCMType ? 1 : 0;
        for (int ch = 0; ch < 16; ch++) {
            int o = -1;
            o = DrawBuff.volume(frameBuffer, 256, 8 + ch * 8, 1, o, 0, tp);
            o = -1;
            o = DrawBuff.volume(frameBuffer, 256, 8 + ch * 8, 2, o, 0, tp);
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            DrawBuff.drawFont8(frameBuffer, 296, ch * 8 + 8, 1, "   ");
            DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
            DrawBuff.ChSegaPCM_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
        }
    }

    public void screenChangeParams() {
//        MDSound.segapcm.segapcm_state segapcmState = audio.GetSegaPCMRegister(chipId);
//        if (segapcmState != null && segapcmState.ram != null && segapcmState.rom != null) {
//            for (int ch = 0; ch < 16; ch++) {
//                int l = segapcmState.ram[ch * 8 + 2] & 0x7f;
//                int r = segapcmState.ram[ch * 8 + 3] & 0x7f;
//                int dt = segapcmState.ram[ch * 8 + 7];
//                double ml = dt / 256.0;
//
//                int ptrRom = segapcmState.ptrRom + ((segapcmState.ram[ch * 8 + 0x86] & segapcmState.bankmask) << segapcmState.bankshift);
//                int addr = (int) ((segapcmState.ram[ch * 8 + 0x85] << 16) | (segapcmState.ram[ch * 8 + 0x84] << 8) | segapcmState.low[ch]);
//                int vdt = 0;
//                if (ptrRom + ((addr >> 8) & segapcmState.rgnmask) < segapcmState.rom.length) {
//                    vdt = Math.abs((byte) (segapcmState.rom[ptrRom + ((addr >> 8) & segapcmState.rgnmask)]) - 0x80);
//                }
//                byte end = (byte) (segapcmState.ram[ch * 8 + 6] + 1);
//                if ((segapcmState.ram[ch * 8 + 0x86] & 1) != 0) vdt = 0;
//                if ((addr >> 16) == end) {
//                    if ((segapcmState.ram[ch * 8 + 0x86] & 2) == 0)
//                        ml = 0;
//                }
//
//                newParam.channels[ch].volumeL = Math.min(Math.max((l * vdt) >> 8, 0), 19);
//                newParam.channels[ch].volumeR = Math.min(Math.max((r * vdt) >> 8, 0), 19);
//                if (newParam.channels[ch].volumeL == 0 && newParam.channels[ch].volumeR == 0) {
//                    ml = 0;
//                }
//                newParam.channels[ch].note = (ml == 0 || vdt == 0) ? -1 : (common.searchSegaPCMNote(ml));
//                newParam.channels[ch].pan = (r >> 3) * 0x10 + (l >> 3);
//            }
//        }

        byte[] segapcmReg = audio.getSEGAPCMRegister(chipId);
        boolean[] segapcmKeyOn = audio.getSEGAPCMKeyOn(chipId);
        if (segapcmReg != null) {
            for (int ch = 0; ch < 16; ch++) {
                int l = segapcmReg[ch * 8 + 2] & 0x7f;
                int r = segapcmReg[ch * 8 + 3] & 0x7f;
                int dt = segapcmReg[ch * 8 + 7];
                double ml = dt / 256.0;

                if (segapcmKeyOn[ch]) {
                    newParam.channels[ch].note = Common.searchSegaPCMNote(ml);
                    newParam.channels[ch].volumeL = Math.min(Math.max((l * 1) >> 1, 0), 19);
                    newParam.channels[ch].volumeR = Math.min(Math.max((r * 1) >> 1, 0), 19);
                } else {
                    newParam.channels[ch].volumeL -= newParam.channels[ch].volumeL > 0 ? 1 : 0;
                    newParam.channels[ch].volumeR -= newParam.channels[ch].volumeR > 0 ? 1 : 0;

                    if (newParam.channels[ch].volumeL == 0 && newParam.channels[ch].volumeR == 0) {
                        newParam.channels[ch].note = -1;
                    }
                }

                newParam.channels[ch].pan = ((l >> 3) & 0xf) | (((r >> 3) & 0xf) << 4);

                segapcmKeyOn[ch] = false;
            }
        }
    }

    public void screenDrawParams() {
        int tp = ((chipId == 0) ? parent.setting.getSEGAPCMType()[0].getUseReal()[0] : parent.setting.getSEGAPCMType()[1].getUseReal()[0]) ? 1 : 0;

        for (int c = 0; c < 16; c++) {

            MDChipParams.Channel orc = oldParam.channels[c];
            MDChipParams.Channel nrc = newParam.channels[c];

            orc.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 1, orc.volumeL, nrc.volumeL, tp);
            orc.volumeR = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 2, orc.volumeR, nrc.volumeR, tp);
            DrawBuff.keyBoard(frameBuffer, c, orc.note, nrc.note, tp);
            DrawBuff.PanType2(frameBuffer, c, orc.pan, nrc.pan, tp);

            DrawBuff.ChSegaPCM(frameBuffer, c, orc.mask, nrc.mask, tp);
        }
    }


    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmSegaPCM));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneSEGAPCM();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 136));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmSegaPCM
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 136));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmSegaPCM");
        this.setTitle("SegaPCM");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
