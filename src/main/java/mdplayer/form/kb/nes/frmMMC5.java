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

import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmMMC5 extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipId;
    private int zoom;

    //
    private MDChipParams.MMC5 newParam;
    private MDChipParams.MMC5 oldParam = new MDChipParams.MMC5();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmMMC5.class);

    public frmMMC5(frmMain frm, int chipId, int zoom, MDChipParams.MMC5 newParam) {
        super(frm);

        this.chipId = chipId;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneMMC5(), null, zoom);
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
                parent.setting.getLocation().getPosMMC5()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosMMC5()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneMMC5().getWidth() * zoom, frameSizeH + Resources.getPlaneMMC5().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneMMC5().getWidth() * zoom, frameSizeH + Resources.getPlaneMMC5().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneMMC5().getWidth() * zoom, frameSizeH + Resources.getPlaneMMC5().getHeight() * zoom));
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

        byte[] reg = audio.getMMC5Register(chipId);
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
                newParam.sqrChannels[i].pantp = (reg[3 + i * 4] & 0xf8) >> 3;//Length counter load
                newParam.sqrChannels[i].kf = (reg[i * 4] & 0xc0) >> 6;//Duty
                newParam.sqrChannels[i].dda = ((reg[i * 4] & 0x20) >> 5) != 0;//LengthCounter
                newParam.sqrChannels[i].noise = ((reg[i * 4] & 0x10) >> 4) != 0;//constantVolume
            }

            newParam.pcmChannel.dda = (reg[8] & 0x80) != 0;
            newParam.pcmChannel.noise = (reg[8] & 0x01) != 0;
            newParam.pcmChannel.note = (reg[9] & 0xff);
            newParam.pcmChannel.volume = (reg[9] & 0xff) >> 3;
            newParam.pcmChannel.volume = Math.min(newParam.pcmChannel.volume, 19);
        }
    }

    public void screenDrawParams() {
        for (int i = 0; i < 2; i++) {
            DrawBuff.keyBoard(frameBuffer, i * 2, oldParam.sqrChannels[i].note, newParam.sqrChannels[i].note, 0);
            oldParam.sqrChannels[i].volume = DrawBuff.volume(frameBuffer, 256, 8 + i * 2 * 8, 0, oldParam.sqrChannels[i].volume, newParam.sqrChannels[i].volume, 0);
            DrawBuff.font4Int2(frameBuffer, 22 * 4, (2 + i * 2) * 8, 0, 2, oldParam.sqrChannels[i].pantp, newParam.sqrChannels[i].pantp);
            DrawBuff.drawDuty(frameBuffer, 24, (1 + i * 2) * 8, oldParam.sqrChannels[i].kf, newParam.sqrChannels[i].kf);
            DrawBuff.drawNESSw(frameBuffer, 32, (2 + i * 2) * 8, oldParam.sqrChannels[i].dda, newParam.sqrChannels[i].dda);
            DrawBuff.drawNESSw(frameBuffer, 40, (2 + i * 2) * 8, oldParam.sqrChannels[i].noise, newParam.sqrChannels[i].noise);
            DrawBuff.ChMMC5(frameBuffer, i, oldParam.sqrChannels[i].mask, newParam.sqrChannels[i].mask, 0);
        }

        oldParam.pcmChannel.volume = DrawBuff.volume(frameBuffer, 256, 8 + 3 * 8, 0, oldParam.pcmChannel.volume, newParam.pcmChannel.volume, 0);
        DrawBuff.drawNESSw(frameBuffer, 148, 32, oldParam.pcmChannel.dda, newParam.pcmChannel.dda);
        DrawBuff.drawNESSw(frameBuffer, 160, 32, oldParam.pcmChannel.noise, newParam.pcmChannel.noise);
        oldParam.pcmChannel.note = DrawBuff.font4HexByte(frameBuffer, 196, 32, 0, oldParam.pcmChannel.note, newParam.pcmChannel.note);
        DrawBuff.ChMMC5(frameBuffer, 2, oldParam.pcmChannel.mask, newParam.pcmChannel.mask, 0);

    }

    public void screenInit() {
        for (int c = 0; c < newParam.sqrChannels.length; c++) {
            newParam.sqrChannels[c].note = -1;
            newParam.sqrChannels[c].volume = 0;
            newParam.sqrChannels[c].pantp = 0;
            newParam.sqrChannels[c].kf = 0;
            newParam.sqrChannels[c].dda = false;
            newParam.sqrChannels[c].noise = false;
        }
        newParam.pcmChannel.dda = false;
        newParam.pcmChannel.noise = false;
        newParam.pcmChannel.note = -1;
        newParam.pcmChannel.volume = 0;
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
                            parent.resetChannelMask(EnmChip.MMC5, chipId, ch);
                        else
                            parent.setChannelMask(EnmChip.MMC5, chipId, ch);
                    }

                    if (newParam.pcmChannel.mask)
                        parent.resetChannelMask(EnmChip.MMC5, chipId, 0);
                    else
                        parent.setChannelMask(EnmChip.MMC5, chipId, 0);

                }
                return;
            }

            //鍵盤
            if (py < 5 * 8) {
                if (ev.getButton() == MouseEvent.BUTTON2) {
                    for (int i = 0; i < 3; i++) {
                        //マスク解除
                        if (i < 3) parent.resetChannelMask(EnmChip.MMC5, chipId, i);
                    }

                    return;
                }

                int ch = (py / 8) - 1;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    switch (ch) {
                    case 0:
                        ch = 0;
                        break;
                    case 2:
                        ch = 1;
                        break;
                    case 3:
                        ch = 2;
                        break;
                    default:
                        return;
                    }
                    //マスク
                    parent.setChannelMask(EnmChip.MMC5, chipId, ch);

                }
            }
        }
    };

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneMMC5();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(286, 40));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmMMC5
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(286, 40));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmMMC5");
        this.setTitle("MMC5");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    private JPanel pbScreen;
}
