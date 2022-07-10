package mdplayer.form.kb.nes;

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


public class frmNESDMC extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    //
    private MDChipParams.NESDMC newParam = null;
    private MDChipParams.NESDMC oldParam = new MDChipParams.NESDMC();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmNESDMC.class);

    public frmNESDMC(frmMain frm, int chipID, int zoom, MDChipParams.NESDMC newParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneNESDMC(), null, zoom);
        DrawBuff.screenInitNESDMC(frameBuffer);
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
                parent.setting.getLocation().getPosNESDMC()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosNESDMC()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneNESDMC().getWidth() * zoom, frameSizeH + Resources.getPlaneNESDMC().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneNESDMC().getWidth() * zoom, frameSizeH + Resources.getPlaneNESDMC().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneNESDMC().getWidth() * zoom, frameSizeH + Resources.getPlaneNESDMC().getHeight() * zoom));
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

    public void screenChangeParams() {
        final double LOG2_440 = 8.7813597135246596040696824762152;
        final double LOG_2 = 0.69314718055994530941723212145818;
        final int NOTE_440HZ = 12 * 4 + 9;

        byte[] reg = audio.getAPURegister(chipID);
        int freq;
        int vol;
        int note;
        if (reg != null) {
            for (int i = 0; i < 2; i++) {
                freq = (reg[3 + i * 4] & 0x07) * 0x100 + reg[2 + i * 4];
                vol = reg[i * 4] & 0xf;
                note = 104 - (int) ((12 * (Math.log(freq) / LOG_2 - LOG2_440) + NOTE_440HZ + 0.5));
                note = vol == 0 ? -1 : note;
                newParam.sqrChannels[i].note = note;
                newParam.sqrChannels[i].volume = Math.min((int) ((vol) * 1.33), 19);
                newParam.sqrChannels[i].nfrq = (reg[1 + i * 4] & 0x70) >> 4;//Period
                newParam.sqrChannels[i].pan = (reg[1 + i * 4] & 0x07);//Shift
                newParam.sqrChannels[i].pantp = (reg[3 + i * 4] & 0xf8) >> 3;//Length counter load
                newParam.sqrChannels[i].kf = (reg[i * 4] & 0xc0) >> 6;//Duty
                newParam.sqrChannels[i].dda = ((reg[i * 4] & 0x20) >> 5) != 0;//LengthCounter
                newParam.sqrChannels[i].noise = ((reg[i * 4] & 0x10) >> 4) != 0;//constantVolume
                newParam.sqrChannels[i].volumeL = ((reg[1 + i * 4] & 0x80) >> 7);//Sweep unit enabled
                newParam.sqrChannels[i].volumeR = ((reg[1 + i * 4] & 0x08) >> 3);//negate
            }
        }

        byte[] reg2 = audio.getDMCRegister(chipID);
        if (reg2 == null) return;

        int tri = reg2[0x10];
        int noi = reg2[0x11];
        int dpc = reg2[0x12];

        freq = (reg2[3] & 0x07) * 0x100 + reg2[2];
        note = 92 - (int) ((12 * (Math.log(freq) / LOG_2 - LOG2_440) + NOTE_440HZ + 0.5));
        newParam.triChannel.note = (reg2[0] & 0x7f) == 0 ? -1 : note;
        if ((reg2[0] & 0x80) == 0) {
            if ((reg2[13] & 0x04) == 0)
                //if ((reg2[step + 1] & 0x4) == 0)
                newParam.triChannel.note = -1;
        }

        newParam.dmcChannel.volumeR = (reg2[9] & 0x7f); //Load counter
        tri = tri == 0 ? 0 : (10 + (128 - newParam.dmcChannel.volumeR) / 128 * 9);
        newParam.triChannel.volume = newParam.triChannel.note < 0 ? 0 : tri;
        newParam.triChannel.dda = (reg2[0] & 0x80) != 0;//LengthCounterHalt
        newParam.triChannel.nfrq = (reg2[0] & 0x7f);// linear counter load (R)
        newParam.triChannel.pantp = (reg2[3] & 0xf8) >> 3;//Length counter load

        newParam.noiseChannel.volume = Math.min((int) ((reg2[4] & 0xf) * 1.33), 19);
        newParam.noiseChannel.dda = (reg2[4] & 0x20) != 0; //Envelope loop / length counter halt
        newParam.noiseChannel.noise = (reg2[4] & 0x10) != 0; //constant volume
        newParam.noiseChannel.volumeL = (reg2[6] & 0x80) >> 7; //Loop noise
        newParam.noiseChannel.volumeR = reg2[6] & 0x0f; //noise period
        newParam.noiseChannel.nfrq = (reg2[7] & 0xf8) >> 3; //Length counter load
        //newParam.noiseChannel.volume = ((reg2[1] & 0x8) != 0) ? newParam.noiseChannel.volume : 0;
        noi = noi == 0 ? 0 : 1;// (10 + (128 - newParam.dmcChannel.volumeR) / 128 * 9);
        //newParam.noiseChannel.volume =
        //((reg2[13] & 0x8) != 0)
        //? ((reg2[4] & 0x10) != 0 ? newParam.noiseChannel.volume : (10 + (128 - newParam.dmcChannel.volumeR) / 128 * 9))
        //: 0;
        newParam.noiseChannel.volume =
                ((reg2[13] & 0x8) != 0)
                        ? ((reg2[4] & 0x10) != 0 ? newParam.noiseChannel.volume * noi : ((10 + (128 - newParam.dmcChannel.volumeR) / 128 * 9) * noi))
                        : 0;

        dpc = dpc == 0 ? 0 : (10 + (128 - newParam.dmcChannel.volumeR) / 128 * 9);
        newParam.dmcChannel.dda = (reg2[8] & 0x80) != 0; //IRQ enable
        newParam.dmcChannel.noise = (reg2[8] & 0x40) != 0; //loop
        newParam.dmcChannel.volumeL = (reg2[8] & 0x0f); //frequency
        newParam.dmcChannel.nfrq = reg2[10]; //Sample address
        newParam.dmcChannel.pantp = reg2[11]; //Sample length
        newParam.dmcChannel.volume =
                ((reg2[13] & 0x10) == 0)
                        ? 0
                        : dpc;
    }

    public void screenDrawParams() {
        boolean ob;
        for (int i = 0; i < 2; i++) {
            DrawBuff.keyBoard(frameBuffer, i * 2, oldParam.sqrChannels[i].note, newParam.sqrChannels[i].note, 0);
            DrawBuff.volume(frameBuffer, 256, 8 + i * 2 * 8, 0, oldParam.sqrChannels[i].volume, newParam.sqrChannels[i].volume, 0);
            DrawBuff.font4Int2(frameBuffer, 16 * 4, (2 + i * 2) * 8, 0, 2, oldParam.sqrChannels[i].nfrq, newParam.sqrChannels[i].nfrq);
            DrawBuff.font4Int2(frameBuffer, 19 * 4, (2 + i * 2) * 8, 0, 2, oldParam.sqrChannels[i].pan, newParam.sqrChannels[i].pan);
            DrawBuff.font4Int2(frameBuffer, 22 * 4, (2 + i * 2) * 8, 0, 2, oldParam.sqrChannels[i].pantp, newParam.sqrChannels[i].pantp);
            DrawBuff.drawDuty(frameBuffer, 24, (1 + i * 2) * 8, oldParam.sqrChannels[i].kf, newParam.sqrChannels[i].kf);
            DrawBuff.drawNESSw(frameBuffer, 32, (2 + i * 2) * 8, oldParam.sqrChannels[i].dda, newParam.sqrChannels[i].dda);
            DrawBuff.drawNESSw(frameBuffer, 40, (2 + i * 2) * 8, oldParam.sqrChannels[i].noise, newParam.sqrChannels[i].noise);
            ob = oldParam.sqrChannels[i].volumeL != 0;
            DrawBuff.drawNESSw(frameBuffer, 48, (2 + i * 2) * 8, ob, newParam.sqrChannels[i].volumeL != 0);
            oldParam.sqrChannels[i].volumeL = ob ? 1 : 0;
            ob = oldParam.sqrChannels[i].volumeR != 0;
            DrawBuff.drawNESSw(frameBuffer, 56, (2 + i * 2) * 8, ob, newParam.sqrChannels[i].volumeR != 0);
            oldParam.sqrChannels[i].volumeR = ob ? 1 : 0;
            DrawBuff.ChNESDMC(frameBuffer, i, oldParam.sqrChannels[i].mask, newParam.sqrChannels[i].mask, 0);

        }

        DrawBuff.keyBoard(frameBuffer, 4, oldParam.triChannel.note, newParam.triChannel.note, 0);
        DrawBuff.volume(frameBuffer, 256, 8 + 4 * 8, 0, oldParam.triChannel.volume, newParam.triChannel.volume, 0);
        DrawBuff.drawNESSw(frameBuffer, 36, 6 * 8, oldParam.triChannel.dda, newParam.triChannel.dda);
        DrawBuff.font4Int3(frameBuffer, 13 * 4, 6 * 8, 0, 3, oldParam.triChannel.nfrq, newParam.triChannel.nfrq);
        DrawBuff.font4Int2(frameBuffer, 19 * 4, 6 * 8, 0, 2, oldParam.triChannel.pantp, newParam.triChannel.pantp);
        DrawBuff.ChNESDMC(frameBuffer, 2, oldParam.triChannel.mask, newParam.triChannel.mask, 0);

        DrawBuff.volume(frameBuffer, 256, 8 + 3 * 8, 0, oldParam.noiseChannel.volume, newParam.noiseChannel.volume, 0);
        DrawBuff.drawNESSw(frameBuffer, 228, 32, oldParam.noiseChannel.dda, newParam.noiseChannel.dda);
        DrawBuff.drawNESSw(frameBuffer, 144, 32, oldParam.noiseChannel.noise, newParam.noiseChannel.noise);
        ob = oldParam.noiseChannel.volumeL != 0;
        DrawBuff.drawNESSw(frameBuffer, 160, 32, ob, newParam.noiseChannel.volumeL != 0);
        oldParam.noiseChannel.volumeL = ob ? 1 : 0;
        DrawBuff.font4Int2(frameBuffer, 176, 32, 0, 2, oldParam.noiseChannel.volumeR, newParam.noiseChannel.volumeR);
        DrawBuff.font4Int2(frameBuffer, 196, 32, 0, 2, oldParam.noiseChannel.nfrq, newParam.noiseChannel.nfrq);
        DrawBuff.ChNESDMC(frameBuffer, 3, oldParam.noiseChannel.mask, newParam.noiseChannel.mask, 0);

        DrawBuff.volume(frameBuffer, 256, 8 + 5 * 8, 0, oldParam.dmcChannel.volume, newParam.dmcChannel.volume, 0);
        DrawBuff.drawNESSw(frameBuffer, 144, 48, oldParam.dmcChannel.dda, newParam.dmcChannel.dda);
        DrawBuff.drawNESSw(frameBuffer, 152, 48, oldParam.dmcChannel.dda, newParam.dmcChannel.noise);
        DrawBuff.font4Int2(frameBuffer, 176, 48, 0, 2, oldParam.dmcChannel.volumeL, newParam.dmcChannel.volumeL);
        DrawBuff.font4Int3(frameBuffer, 192, 48, 0, 3, oldParam.dmcChannel.volumeR, newParam.dmcChannel.volumeR);
        DrawBuff.font4HexByte(frameBuffer, 220, 48, 0, oldParam.dmcChannel.nfrq, newParam.dmcChannel.nfrq);
        DrawBuff.font4HexByte(frameBuffer, 244, 48, 0, oldParam.dmcChannel.pantp, newParam.dmcChannel.pantp);
        DrawBuff.ChNESDMC(frameBuffer, 4, oldParam.dmcChannel.mask, newParam.dmcChannel.mask, 0);
    }

    public void screenInit() {
        for (int c = 0; c < newParam.sqrChannels.length; c++) {
            newParam.sqrChannels[c].note = -1;
            newParam.sqrChannels[c].volume = 0;
            newParam.sqrChannels[c].pan = 0;
            newParam.sqrChannels[c].pantp = 0;
            newParam.sqrChannels[c].kf = 0;
            newParam.sqrChannels[c].dda = false;
            newParam.sqrChannels[c].noise = false;
            newParam.sqrChannels[c].volumeL = 0;
            newParam.sqrChannels[c].volumeR = 0;
        }
        newParam.triChannel.dda = false;
        newParam.triChannel.note = -1;
        newParam.triChannel.volume = 0;
        newParam.triChannel.nfrq = 0;
        newParam.triChannel.pantp = 0;

        newParam.noiseChannel.volume = 0;
        newParam.noiseChannel.dda = false;
        newParam.noiseChannel.noise = false;
        newParam.noiseChannel.volumeL = 0;
        newParam.noiseChannel.volumeR = 0;
        newParam.noiseChannel.nfrq = 0;
        newParam.noiseChannel.volume = 0;

        newParam.dmcChannel.dda = false;
        newParam.dmcChannel.noise = false;
        newParam.dmcChannel.volumeL = 0;
        newParam.dmcChannel.volumeR = 0;
        newParam.dmcChannel.nfrq = 0;
        newParam.dmcChannel.pantp = 0;
        newParam.dmcChannel.volume = 0;
    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (int ch = 0; ch < 2; ch++) {
                        if (newParam.sqrChannels[ch].mask)
                            parent.resetChannelMask(EnmChip.NES, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.NES, chipID, ch);
                    }

                    if (newParam.triChannel.mask)
                        parent.resetChannelMask(EnmChip.DMC, chipID, 0);
                    else
                        parent.setChannelMask(EnmChip.DMC, chipID, 0);

                    if (newParam.noiseChannel.mask)
                        parent.resetChannelMask(EnmChip.DMC, chipID, 1);
                    else
                        parent.setChannelMask(EnmChip.DMC, chipID, 1);

                    if (newParam.dmcChannel.mask)
                        parent.resetChannelMask(EnmChip.DMC, chipID, 2);
                    else
                        parent.setChannelMask(EnmChip.DMC, chipID, 2);
                }
                return;
            }

            //鍵盤
            if (py < 7 * 8) {
                if (ev.getButton() == MouseEvent.BUTTON2) {
                    for (int i = 0; i < 5; i++) {
                        //マスク解除
                        if (i < 2) parent.resetChannelMask(EnmChip.NES, chipID, i);
                        else parent.resetChannelMask(EnmChip.DMC, chipID, i - 2);
                    }

                    return;
                }

                int ch = (py / 8) - 1;
                if (ch == 1) return;
                ch = ch == 3 ? 3 : (ch == 5 ? 4 : ch / 2);
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    if (ch < 2) parent.setChannelMask(EnmChip.NES, chipID, ch);
                    else parent.setChannelMask(EnmChip.DMC, chipID, ch - 2);

                }
            }
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmNESDMC));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
//        this.pbScreen.setBackground(Color.ControlDarkDark);
        this.image = mdplayer.properties.Resources.getPlaneNESDMC();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 56));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmNESDMC
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(320, 56));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmNESDMC");
        this.setTitle("NES & DMC");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
