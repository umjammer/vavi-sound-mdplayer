package mdplayer.form.kb.wf;

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
import java.util.Arrays;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.chips.HuC6280Chip;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.chips.OotakeHuC6280;

import static mdplayer.Common.searchSSGNote;


public class frmHuC6280 extends frmBase {

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmHuC6280));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneHuC6280();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 151));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmHuC6280
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 151));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmHuC6280");
        this.setTitle("Huc6280");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;

    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private final int chipId;
    private final int zoom;

    private final MDChipParams.HuC6280 newParam;
    private final MDChipParams.HuC6280 oldParam;
    private final FrameBuffer frameBuffer = new FrameBuffer();

    static final Preferences prefs = Preferences.userNodeForPackage(frmHuC6280.class);

    public frmHuC6280(frmMain frm, int chipId, int zoom, MDChipParams.HuC6280 newParam, MDChipParams.HuC6280 oldParam) {
        super(frm);

        this.chipId = chipId;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        this.oldParam = oldParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneHuC6280(), null, zoom);
        DrawBuff.screenInitHuC6280(frameBuffer);
        update();
    }

    public void update() {
        frameBuffer.refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosHuC6280()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosHuC6280()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneHuC6280().getWidth() * zoom, frameSizeH + Resources.getPlaneHuC6280().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneHuC6280().getWidth() * zoom, frameSizeH + Resources.getPlaneHuC6280().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneHuC6280().getWidth() * zoom, frameSizeH + Resources.getPlaneHuC6280().getHeight() * zoom));
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

    public void screenChangeParams() {

        OotakeHuC6280 chip = audio.plugin.chipRegister.chip(HuC6280Chip.class).getChip(chipId);
        if (chip == null) return;

        //logger.log(Level.TRACE, "%d  %d".formatted(chips.MainVolumeL,chips.MainVolumeR));
        for (int ch = 0; ch < 6; ch++) {
            OotakeHuC6280.Psg psg = chip.getPsg(ch);
            if (psg == null) continue;
            MDChipParams.Channel channel = newParam.channels[ch];
            //logger.log(Level.TRACE, "%d  %d".formatted(psg.outVolumeL, psg.outVolumeR));
            channel.volumeL = psg.outVolumeL >> 10;
            channel.volumeR = psg.outVolumeR >> 10;
            channel.volumeL = Math.min(channel.volumeL, 19);
            channel.volumeR = Math.min(channel.volumeR, 19);

            channel.pan = (psg.volumeL & 0xf) | ((psg.volumeR & 0xf) << 4);

            channel.inst = psg.wave;

            channel.dda = psg.dda;

            int tp = psg.frq;
            if (tp == 0) tp = 1;

            float ftone = 3579545.0f / 32.0f / (float) tp;
            channel.note = searchSSGNote(ftone);
            if (channel.volumeL == 0 && channel.volumeR == 0) channel.note = -1;

            if (ch < 4) continue;

            channel.noise = psg.bNoiseOn;
            channel.nfrq = psg.noiseFrq;
        }

        newParam.mvolL = chip.mainVolumeL;
        newParam.mvolR = chip.mainVolumeR;
        newParam.LfoCtrl = chip.lfoControl;
        newParam.LfoFrq = chip.lfoFreq;
    }

    public void screenDrawParams() {
        int tp = parent.setting.getHuC6280Type()[0].getUseReal()[0] ? 1 : 0;

        for (int c = 0; c < 6; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);

            DrawBuff.VolumeToHuC6280(frameBuffer, c, 1, oyc.volumeL, nyc.volumeL);
            DrawBuff.VolumeToHuC6280(frameBuffer, c, 2, oyc.volumeR, nyc.volumeR);
            DrawBuff.PanType2(frameBuffer, c, oyc.pan, nyc.pan, tp);

            DrawBuff.WaveFormToHuC6280(frameBuffer, c, oyc.inst, nyc.inst);
            DrawBuff.DDAToHuC6280(frameBuffer, c, oyc.dda, nyc.dda);

            DrawBuff.ChHuC6280(frameBuffer, c, oyc.mask, nyc.mask, tp);

            if (c < 4) continue;

            DrawBuff.NoiseToHuC6280(frameBuffer, c, oyc.noise, nyc.noise);
            DrawBuff.NoiseFrqToHuC6280(frameBuffer, c, oyc.nfrq, nyc.nfrq);
        }

        DrawBuff.MainVolumeToHuC6280(frameBuffer, 0, oldParam.mvolL, newParam.mvolL);
        DrawBuff.MainVolumeToHuC6280(frameBuffer, 1, oldParam.mvolR, newParam.mvolR);

        DrawBuff.LfoCtrlToHuC6280(frameBuffer, oldParam.LfoCtrl, newParam.LfoCtrl);
        DrawBuff.LfoFrqToHuC6280(frameBuffer, oldParam.LfoFrq, newParam.LfoFrq);
    }

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int py = ev.getY() / zoom;
            int px = ev.getX() / zoom;

            // For top label row, do nothing
            if (py < 1 * 8) {
                // However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (int ch = 0; ch < 6; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(HuC6280Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(HuC6280Chip.class, chipId, ch);
                    }
                }
                return;
            }

            // keyboard
            if (py < 7 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // Mask.
                    parent.setChannelMask(HuC6280Chip.class, chipId, ch);
                    return;
                }

                // Unmask.
                for (ch = 0; ch < 6; ch++) parent.resetChannelMask(HuC6280Chip.class, chipId, ch);
                return;
            }

            // Right-clicking on a tone does nothing
            if (ev.getButton() == MouseEvent.BUTTON2) return;

            // Judgment of tone display column
            int h = (py - 7 * 8) / (5 * 8);
            int w = Math.min(px / (13 * 8), 2);
            int instCh = h * 3 + w;

            if (instCh < 6) {
                // Copying a tone to the clipboard
                parent.getInstCh(HuC6280Chip.class, instCh, chipId);
            }
        }
    };

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volumeL = -1;
            newParam.channels[c].volumeR = -1;
            newParam.channels[c].pan = -1;
            Arrays.fill(newParam.channels[c].inst, 0);
            newParam.channels[c].dda = false;
            newParam.channels[c].noise = false;
            newParam.channels[c].nfrq = 0;
        }
        newParam.mvolL = 0;
        newParam.mvolR = 0;
        newParam.LfoCtrl = 0;
        newParam.LfoFrq = 0;
    }
}
