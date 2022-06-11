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


public class frmQSound extends frmBase {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;
    private MDChipParams.QSound newParam;
    private MDChipParams.QSound oldParam;
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmQSound.class);

    public frmQSound(frmMain frm, int chipID, int zoom, MDChipParams.QSound newParam, MDChipParams.QSound oldParam) {
        super(frm);

        initializeComponent();

        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(pbScreen, Resources.getPlaneQSound(), null, zoom);
        screenInit();
        update();
    }

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected Boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosQSound()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosQSound()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneQSound().getWidth() * zoom, frameSizeH + Resources.getPlaneQSound().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneQSound().getWidth() * zoom, frameSizeH + Resources.getPlaneQSound().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneQSound().getWidth() * zoom, frameSizeH + Resources.getPlaneQSound().getHeight() * zoom));
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
                    for (ch = 0; ch < 19; ch++) {
                        if (newParam.channels[ch].mask == true)
                            parent.resetChannelMask(EnmChip.QSound, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.QSound, chipID, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 19) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(EnmChip.QSound, chipID, ch);
                    return;
                }

                for (ch = 0; ch < 19; ch++) parent.resetChannelMask(EnmChip.QSound, chipID, ch);
                return;
            }
        }
    };

    private void screenInit() {
        for (int ch = 0; ch < 16; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, 0);
            }
        }
    }

    public void screenChangeParams() {
        int[] QSoundRegister = Audio.getQSoundRegister(chipID);

        //PCM 16ch
        for (int ch = 0; ch < 16; ch++) {
            newParam.channels[ch].echo = QSoundRegister[ch + 0xba];
            newParam.channels[ch].freq = QSoundRegister[(ch << 3) + 2];
            newParam.channels[ch].bank = QSoundRegister[(((ch + 15) % 16) << 3) + 0];
            newParam.channels[ch].sadr = QSoundRegister[(ch << 3) + 1];
            newParam.channels[ch].eadr = QSoundRegister[(ch << 3) + 5];
            newParam.channels[ch].ladr = QSoundRegister[(ch << 3) + 4];
            //newParam.channels[ch].ladr = QSoundRegister[(ch << 3) + 3];
            int vol = QSoundRegister[(ch << 3) + 6];
            int pan = QSoundRegister[ch + 0x80] - 0x110;
            if (pan >= 97) pan = 16;//center?
            int panL = (int) (15.0 / 16.0 * (pan > 16 ? (16 - (33 - pan)) : 16));
            int panR = (int) (15.0 / 16.0 * (pan < 16 ? (16 - pan) : 16));
            newParam.channels[ch].pan = (panR << 4) | panL;
            newParam.channels[ch].volumeL = Math.min(Math.max(vol * panL / 256 / 16, 0), 19);
            newParam.channels[ch].volumeR = Math.min(Math.max(vol * panR / 256 / 16, 0), 19);

            newParam.channels[ch].note = Math.max(Math.min(Common.searchSegaPCMNote(newParam.channels[ch].freq / 16.0 / 166.0), 7 * 12), 0);
            if (vol == 0) newParam.channels[ch].note = -1;
        }
        //ADPCM 3ch
        for (int ch = 0; ch < 3; ch++) {
            newParam.channels[ch + 16].bank = QSoundRegister[(ch << 2) + 0xcc];
            newParam.channels[ch + 16].sadr = QSoundRegister[(ch << 2) + 0xca];
            newParam.channels[ch + 16].eadr = QSoundRegister[(ch << 2) + 0xcb];
            int vol = (QSoundRegister[(ch << 2) + 0xcd] >> 16);
            int pan = QSoundRegister[ch + 16 + 0x80] - 0x110;
            if (pan >= 97) pan = 16;//center?
            int panL = (int) (15.0 / 16.0 * (pan > 16 ? (16 - (33 - pan)) : 16));
            int panR = (int) (15.0 / 16.0 * (pan < 16 ? (16 - pan) : 16));
            newParam.channels[ch + 16].pan = (panR << 4) | panL;
            newParam.channels[ch + 16].volumeL = Math.min(Math.max(vol * panL / 10, 0), 19);
            newParam.channels[ch + 16].volumeR = Math.min(Math.max(vol * panR / 10, 0), 19);
        }

        //echo
        newParam.channels[0].inst[0] = QSoundRegister[0x93];//feedback
        newParam.channels[0].inst[1] = QSoundRegister[0xd9];//end_pos
        newParam.channels[0].inst[2] = QSoundRegister[0xe2];//delay_update
        newParam.channels[0].inst[3] = QSoundRegister[0xe3];//next_state
        //Wet
        newParam.channels[0].inst[4] = QSoundRegister[0xde];//delay left
        newParam.channels[0].inst[5] = QSoundRegister[0xe0];//delay right
        newParam.channels[0].inst[6] = QSoundRegister[0xe4];//volume_left
        newParam.channels[0].inst[7] = QSoundRegister[0xe6];//volume right
        //Dry
        newParam.channels[0].inst[8] = QSoundRegister[0xdf];//delay left
        newParam.channels[0].inst[9] = QSoundRegister[0xe1];//delay right
        newParam.channels[0].inst[10] = QSoundRegister[0xe5];//volume_left
        newParam.channels[0].inst[11] = QSoundRegister[0xe7];//volume right
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        //PCM 16ch
        for (int ch = 0; ch < 16; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 65, ch * 8 + 8, 0, oyc.echo, nyc.echo);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 70, ch * 8 + 8, 0, oyc.freq, nyc.freq);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 75, ch * 8 + 8, 0, oyc.bank, nyc.bank);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 80, ch * 8 + 8, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 85, ch * 8 + 8, 0, oyc.eadr, nyc.eadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 90, ch * 8 + 8, 0, oyc.ladr, nyc.ladr);
            DrawBuff.PanType2(frameBuffer, ch, oyc.pan, nyc.pan, 0);
            DrawBuff.VolumeXY(frameBuffer, 94, ch * 2 + 2, 1, oyc.volumeL, nyc.volumeL, 0);
            DrawBuff.VolumeXY(frameBuffer, 94, ch * 2 + 3, 1, oyc.volumeR, nyc.volumeR, 0);
            DrawBuff.KeyBoardToQSound(frameBuffer, ch, oyc.note, nyc.note, 0);

            DrawBuff.ChQSound(frameBuffer, ch, oyc.mask, nyc.mask, 0);
        }
        //ADPCM 3ch
        for (int ch = 0; ch < 3; ch++) {
            oyc = oldParam.channels[ch + 16];
            nyc = newParam.channels[ch + 16];
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 75, (ch + 16) * 8 + 8, 0, oyc.bank, nyc.bank);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 80, (ch + 16) * 8 + 8, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 85, (ch + 16) * 8 + 8, 0, oyc.eadr, nyc.eadr);
            //DrawBuff.PanType2(frameBuffer, (ch + 16),oyc.pan, nyc.pan, 0);
            DrawBuff.VolumeXY(frameBuffer, 94, (ch + 16) * 2 + 2, 1, oyc.volumeL, nyc.volumeL, 0);
            DrawBuff.VolumeXY(frameBuffer, 94, (ch + 16) * 2 + 3, 1, oyc.volumeR, nyc.volumeR, 0);

            DrawBuff.ChQSound(frameBuffer, ch + 16, oyc.mask, nyc.mask, 0);
        }

        //echo
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 36, 17 * 8 + 8, 0, oldParam.channels[0].inst[0], newParam.channels[0].inst[0]);//feedback
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 36, 18 * 8 + 8, 0, oldParam.channels[0].inst[1], newParam.channels[0].inst[1]);//end_pos
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 51, 17 * 8 + 8, 0, oldParam.channels[0].inst[2], newParam.channels[0].inst[2]);//delay_update
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 51, 18 * 8 + 8, 0, oldParam.channels[0].inst[3], newParam.channels[0].inst[3]);//next_state

        //Wet
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 07, 17 * 8 + 8, 0, oldParam.channels[0].inst[4], newParam.channels[0].inst[4]);//delay l
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 12, 17 * 8 + 8, 0, oldParam.channels[0].inst[5], newParam.channels[0].inst[5]);//delay r
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 07, 18 * 8 + 8, 0, oldParam.channels[0].inst[6], newParam.channels[0].inst[6]);//vol l
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 12, 18 * 8 + 8, 0, oldParam.channels[0].inst[7], newParam.channels[0].inst[7]);//vol r

        //Dry
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 18, 17 * 8 + 8, 0, oldParam.channels[0].inst[8], newParam.channels[0].inst[8]);//delay l
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 23, 17 * 8 + 8, 0, oldParam.channels[0].inst[9], newParam.channels[0].inst[9]);//delay r
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 18, 18 * 8 + 8, 0, oldParam.channels[0].inst[10], newParam.channels[0].inst[10]);//vol l
        DrawBuff.font4Hex16Bit(frameBuffer, 4 * 23, 18 * 8 + 8, 0, oldParam.channels[0].inst[11], newParam.channels[0].inst[11]);//vol r
    }

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmQSound));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
//        this.pbScreen.setBackground(Color.ControlDarkDark);
        this.image = mdplayer.properties.Resources.getPlaneQSound();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(440, 177));
        // this.pbScreen.TabIndex = 2
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmQSound
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(440, 177));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmQSound");
        this.setTitle("QSound");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
