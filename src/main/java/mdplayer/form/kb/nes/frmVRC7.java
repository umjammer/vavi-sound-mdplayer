package mdplayer.form.kb.nes;

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
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.Chip.ChipKeyInfo;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.chips.NpNesChip;
import mdplayer.chips.NpNesChip.Vrc7Chip;
import mdplayer.chips.SegaPcmChip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;

public class frmVRC7 extends frmChipBase<MDChipParams.VRC7> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmVRC7.class);

    public frmVRC7(frmMain frm, int chipId, int zoom, MDChipParams.VRC7 newParam) {
        super(frm, chipId, zoom, newParam, new MDChipParams.VRC7());

        initializeComponent();

        frameBuffer.Add(pbScreen, Resources.getPlaneVRC7(), null, zoom);
        boolean VRC7Type = false;
        int tp = VRC7Type ? 1 : 0;
        DrawBuff.screenInitVRC7(frameBuffer, tp);
        update();
    }

//    @Override

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosVrc7()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosVrc7()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneVRC7().getWidth() * zoom, frameSizeH + Resources.getPlaneVRC7().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneVRC7().getWidth() * zoom, frameSizeH + Resources.getPlaneVRC7().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneVRC7().getWidth() * zoom, frameSizeH + Resources.getPlaneVRC7().getHeight() * zoom));
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
        int[] vrc7Register = audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).readVrc7(chipId);
        if (vrc7Register == null) return;

        // Get whether there was a key-on (one-shot)
        ChipKeyInfo ki = audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).getVRC7KeyInfo(chipId);

        for (int ch = 0; ch < 6; ch++) {
            MDChipParams.Channel nyc = newParam.channels[ch];

            // Tone Number
            nyc.inst[0] = (vrc7Register[0x30 + ch] & 0xf0) >> 4;
            // Get sustain
            nyc.inst[1] = (vrc7Register[0x20 + ch] & 0x20) >> 5;
            // Current key-on state
            nyc.inst[2] = (vrc7Register[0x20 + ch] & 0x10) >> 4;
            // Volume
            nyc.inst[3] = (vrc7Register[0x30 + ch] & 0x0f);

            // Playback frequency
            int freq = vrc7Register[0x10 + ch] + ((vrc7Register[0x20 + ch] & 0x1) << 8);
            // Octave
            int oct = ((vrc7Register[0x20 + ch] & 0xe) >> 1);
            // Get the approximate pitch from the frequency and octave information
            nyc.note = SegaPcmChip.searchSegaPCMNote(freq / 172.0) + (oct - 4) * 12;

            // In case of one-shot (a state where key-on has occurred at least once since the last process)
            if (ki.on[ch]) {
                // Swing the volume meter
                nyc.volumeL = (19 - nyc.inst[3]);
            } else {
                // If there is no one-shot and the key is not currently on, set the pitch to none.
                // Even if there is no one-shot, if the key is on, do not reset the pitch.
                // (Because it may be sustained, bent, or slurred.)
                // Also, this process is not performed when a one-shot has occurred.
                // This is to handle the case where there was a one-shot but the key is not currently on.
                // In the above case, the volume meter will swing and the pitch display will be shown for a moment.
                if (nyc.inst[2] == 0) nyc.note = -1;

                // Volume meter decay process (ignores tone settings and is always constant)
                nyc.volumeL--;
                if (nyc.volumeL < 0) nyc.volumeL = 0;
            }
        }

        newParam.channels[0].inst[4] = (vrc7Register[0x02] & 0x3f); // TL
        newParam.channels[0].inst[5] = (vrc7Register[0x03] & 0x07); // FB

        newParam.channels[0].inst[6] = (vrc7Register[0x04] & 0xf0) >> 4;  // AR
        newParam.channels[0].inst[7] = (vrc7Register[0x04] & 0x0f);       // DR
        newParam.channels[0].inst[8] = (vrc7Register[0x06] & 0xf0) >> 4;  // SL
        newParam.channels[0].inst[9] = (vrc7Register[0x06] & 0x0f);       // RR
        newParam.channels[0].inst[10] = (vrc7Register[0x02] & 0x80) >> 7; // KL
        newParam.channels[0].inst[11] = (vrc7Register[0x00] & 0x0f);      // MT
        newParam.channels[0].inst[12] = (vrc7Register[0x00] & 0x80) >> 7; // AM
        newParam.channels[0].inst[13] = (vrc7Register[0x00] & 0x40) >> 6; // VB
        newParam.channels[0].inst[14] = (vrc7Register[0x00] & 0x20) >> 5; // EG
        newParam.channels[0].inst[15] = (vrc7Register[0x00] & 0x10) >> 4; // KR
        newParam.channels[0].inst[16] = (vrc7Register[0x03] & 0x08) >> 3; // DM
        newParam.channels[0].inst[17] = (vrc7Register[0x05] & 0xf0) >> 4; // AR
        newParam.channels[0].inst[18] = (vrc7Register[0x05] & 0x0f);      // DR
        newParam.channels[0].inst[19] = (vrc7Register[0x07] & 0xf0) >> 4; // SL
        newParam.channels[0].inst[20] = (vrc7Register[0x07] & 0x0f);      // RR
        newParam.channels[0].inst[21] = (vrc7Register[0x03] & 0x80) >> 7; // KL
        newParam.channels[0].inst[22] = (vrc7Register[0x01] & 0x0f);      // MT
        newParam.channels[0].inst[23] = (vrc7Register[0x01] & 0x80) >> 7; // AM
        newParam.channels[0].inst[24] = (vrc7Register[0x01] & 0x40) >> 6; // VB
        newParam.channels[0].inst[25] = (vrc7Register[0x01] & 0x20) >> 5; // EG
        newParam.channels[0].inst[26] = (vrc7Register[0x01] & 0x10) >> 4; // KR
        newParam.channels[0].inst[27] = (vrc7Register[0x03] & 0x10) >> 4; // DC
    }

    public void screenDrawParams() {
        int tp = 0;

        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int c = 0; c < 6; c++) {

            oyc = oldParam.channels[c];
            nyc = newParam.channels[c];

            oyc.volumeL = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);

            oyc.inst[0] = DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 37, (c / 3) * 2 + 16, oyc.inst[0], nyc.inst[0]);
            DrawBuff.susFlag(frameBuffer, (c % 3) * 16 + 41, (c / 3) * 2 + 16, 0, oyc.inst[1], nyc.inst[1]);
            DrawBuff.susFlag(frameBuffer, (c % 3) * 16 + 44, (c / 3) * 2 + 16, 0, oyc.inst[2], nyc.inst[2]);
            oyc.inst[3] = DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 46, (c / 3) * 2 + 16, oyc.inst[3], nyc.inst[3]);

            DrawBuff.chYM2413(frameBuffer, c, oyc.mask, nyc.mask, tp);
        }

        oyc = oldParam.channels[0];
        nyc = newParam.channels[0];
        oyc.inst[4] = DrawBuff.drawInstNumber(frameBuffer, 9, 14, oyc.inst[4], nyc.inst[4]); // TL
        DrawBuff.drawInstNumber(frameBuffer, 14, 14, oyc.inst[5], nyc.inst[5]); // FB

        for (int c = 0; c < 11; c++) {
            oyc.inst[6 + c] = DrawBuff.drawInstNumber(frameBuffer, c * 3, 18, oyc.inst[6 + c], nyc.inst[6 + c]);
            oyc.inst[17 + c] = DrawBuff.drawInstNumber(frameBuffer, c * 3, 20, oyc.inst[17 + c], nyc.inst[17 + c]);
        }
    }

    public void screenInit() {
        for (int ch = 0; ch < 6; ch++) {
            newParam.channels[ch].inst[0] = 0;
            newParam.channels[ch].inst[1] = 0;
            newParam.channels[ch].inst[2] = 0;
            newParam.channels[ch].inst[3] = 0;
            newParam.channels[ch].note = -1;
            newParam.channels[ch].volumeL = 0;
            newParam.channels[ch].mask = false;
        }

        newParam.channels[0].inst[4] = 0;
        newParam.channels[0].inst[5] = 0;
        newParam.channels[0].inst[6] = 0;
        newParam.channels[0].inst[7] = 0;
        newParam.channels[0].inst[8] = 0;
        newParam.channels[0].inst[9] = 0;
        newParam.channels[0].inst[10] = 0;
        newParam.channels[0].inst[11] = 0;
        newParam.channels[0].inst[12] = 0;
        newParam.channels[0].inst[13] = 0;
        newParam.channels[0].inst[14] = 0;
        newParam.channels[0].inst[15] = 0;
        newParam.channels[0].inst[16] = 0;
        newParam.channels[0].inst[17] = 0;
        newParam.channels[0].inst[18] = 0;
        newParam.channels[0].inst[19] = 0;
        newParam.channels[0].inst[20] = 0;
        newParam.channels[0].inst[21] = 0;
        newParam.channels[0].inst[22] = 0;
        newParam.channels[0].inst[23] = 0;
        newParam.channels[0].inst[24] = 0;
        newParam.channels[0].inst[25] = 0;
        newParam.channels[0].inst[26] = 0;
        newParam.channels[0].inst[27] = 0;
    }

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            // For top label row, do nothing
            if (py < 1 * 8) {
                // However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (int ch = 0; ch < 6; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(Vrc7Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(Vrc7Chip.class, chipId, ch);
                    }
                }
                return;
            }

            // keyboard
            if (py < 7 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ch == 9) {
                    int x = (px / 4 - 4);
                    if (x < 0) return;
                    x /= 15;
                    if (x > 4) return;
                    ch += x;
                }

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // Mask.
                    parent.setChannelMask(Vrc7Chip.class, chipId, ch);
                    return;
                }

                // Unmask.
                for (ch = 0; ch < 6; ch++) parent.resetChannelMask(Vrc7Chip.class, chipId, ch);
                return;
            }

            // Tone column
            if (py < 15 * 8 && px < 16 * 8) {
                // Copying a tone to the clipboard
                parent.getInstCh(Vrc7Chip.class, 0, chipId);
            }
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmVRC7));
        this.pbScreen = new ScreenPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneVRC7();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 88));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmVRC7
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 88));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage(Resources.getFeli128());
//        this.MaximizeBox = false;
        this.setName("frmVRC7");
        this.setTitle("VRC7");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
}
