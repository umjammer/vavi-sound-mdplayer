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


public class frmFDS extends frmBase {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID;
    private int zoom;

    private MDChipParams.FDS newParam;
    private MDChipParams.FDS oldParam = new MDChipParams.FDS();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmFDS.class);

    public frmFDS(frmMain frm, int chipID, int zoom, MDChipParams.FDS newParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getplaneFDS(), null, zoom);
        DrawBuff.screenInitFDS(frameBuffer);
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
                parent.setting.getLocation().getPosFDS()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosFDS()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneFDS().getWidth() * zoom, frameSizeH + Resources.getplaneFDS().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneFDS().getWidth() * zoom, frameSizeH + Resources.getplaneFDS().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneFDS().getWidth() * zoom, frameSizeH + Resources.getplaneFDS().getHeight() * zoom));
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

        mdsound.np.NpNesFds reg = Audio.getFDSRegister(chipID);
        int freq;
        int vol;
        int note;
        if (reg != null) {
            freq = (int) reg.lastFreq;
            vol = (int) reg.lastVol;
            note = -15 + (int) ((12 * (Math.log(freq) / LOG_2 - LOG2_440) + NOTE_440HZ + 0.5));
            note = note < 0 ? -1 : (note > 120 ? -1 : note);
            note = vol == 0 ? -1 : note;
            vol = note == -1 ? 0 : vol;
            newParam.channel.note = note;
            newParam.channel.volume = Math.min((int) ((vol) * 0.5), 19);

            for (int i = 0; i < 32; i++) {
                newParam.wave[i] = (reg.wave[1][i * 2 + 0] + reg.wave[1][i * 2 + 1]) >> 2;
                newParam.mod[i] = (reg.wave[0][i * 2 + 0] + reg.wave[0][i * 2 + 1]) << 1;
            }

            newParam.VolDir = reg.envMode[1];
            newParam.VolSpd = (int) reg.envSpeed[1];
            newParam.VolGain = (int) reg.envOut[1];
            newParam.VolDi = reg.envHalt;
            newParam.VolFrq = (int) reg.freq[1];
            newParam.VolHlR = reg.wavHalt;

            newParam.ModDir = reg.envMode[0];
            newParam.ModSpd = (int) reg.envSpeed[0];
            newParam.ModGain = (int) reg.envOut[0];
            newParam.ModDi = reg.modHalt;
            newParam.ModFrq = (int) reg.freq[0];
            newParam.ModCnt = (int) reg.modPos;

            newParam.EnvSpd = (int) reg.masterEnvSpeed;
            newParam.EnvVolSw = !reg.envDisable[1];
            newParam.EnvModSw = !reg.envDisable[0];

            newParam.MasterVol = reg.masterVol;
            newParam.WE = reg.wavWrite;
        }
    }

    public void screenDrawParams() {
        DrawBuff.keyBoard(frameBuffer, 0, oldParam.channel.note, newParam.channel.note, 0);
        DrawBuff.volume(frameBuffer, 256, 8 + 0 * 8, 0, oldParam.channel.volume, newParam.channel.volume, 0);

        DrawBuff.WaveFormToFDS(frameBuffer, 0, oldParam.wave, newParam.wave);
        DrawBuff.WaveFormToFDS(frameBuffer, 1, oldParam.mod, newParam.mod);

        DrawBuff.drawNESSw(frameBuffer, 20 * 4, 6 * 4, oldParam.VolDir, newParam.VolDir);
        DrawBuff.font4Int2(frameBuffer, 19 * 4, 8 * 4, 0, 2, oldParam.VolSpd, newParam.VolSpd);
        DrawBuff.font4Int2(frameBuffer, 19 * 4, 10 * 4, 0, 2, oldParam.VolGain, newParam.VolGain);
        DrawBuff.drawNESSw(frameBuffer, 20 * 4, 12 * 4, oldParam.VolDi, newParam.VolDi);
        DrawBuff.font4Hex12Bit(frameBuffer, 26 * 4, 6 * 4, 0, oldParam.VolFrq, newParam.VolFrq);
        DrawBuff.drawNESSw(frameBuffer, 28 * 4, 8 * 4, oldParam.VolHlR, newParam.VolHlR);

        DrawBuff.drawNESSw(frameBuffer, 48 * 4, 6 * 4, oldParam.ModDir, newParam.ModDir);
        DrawBuff.font4Int2(frameBuffer, 47 * 4, 8 * 4, 0, 2, oldParam.ModSpd, newParam.ModSpd);
        DrawBuff.font4Int2(frameBuffer, 47 * 4, 10 * 4, 0, 2, oldParam.ModGain, newParam.ModGain);
        DrawBuff.drawNESSw(frameBuffer, 48 * 4, 12 * 4, oldParam.ModDi, newParam.ModDi);
        DrawBuff.font4Hex12Bit(frameBuffer, 54 * 4, 6 * 4, 0, oldParam.ModFrq, newParam.ModFrq);
        DrawBuff.font4Int3(frameBuffer, 54 * 4, 8 * 4, 0, 3, oldParam.ModCnt, newParam.ModCnt);

        DrawBuff.font4Int3(frameBuffer, 65 * 4, 6 * 4, 0, 3, oldParam.EnvSpd, newParam.EnvSpd);
        DrawBuff.drawNESSw(frameBuffer, 67 * 4, 8 * 4, oldParam.EnvVolSw, newParam.EnvVolSw);
        DrawBuff.drawNESSw(frameBuffer, 67 * 4, 10 * 4, oldParam.EnvModSw, newParam.EnvModSw);

        DrawBuff.font4Int2(frameBuffer, 76 * 4, 6 * 4, 0, 2, oldParam.MasterVol, newParam.MasterVol);
        DrawBuff.drawNESSw(frameBuffer, 77 * 4, 8 * 4, oldParam.WE, newParam.WE);

        DrawBuff.ChFDS(frameBuffer, 0, oldParam.channel.mask, newParam.channel.mask, 0);
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
                    if (newParam.channel.mask)
                        parent.resetChannelMask(EnmChip.FDS, chipID, 0);
                    else
                        parent.setChannelMask(EnmChip.FDS, chipID, 0);
                }
                return;
            }

            //鍵盤
            if (py < 2 * 8) {
                if (ev.getButton() == MouseEvent.BUTTON2) {
                    //マスク解除
                    parent.resetChannelMask(EnmChip.FDS, chipID, 0);
                    return;
                }

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    parent.setChannelMask(EnmChip.FDS, chipID, 0);
                    return;
                }
            }
        }
    };

    public void screenInit() {
        newParam.channel.note = -1;
        newParam.channel.volume = -1;
        for (int i = 0; i < 32; i++) {
            newParam.wave[i] = 0;
            newParam.mod[i] = 0;
        }

        newParam.VolDir = false;
        newParam.VolSpd = 0;
        newParam.VolGain = 0;
        newParam.VolDi = false;
        newParam.VolFrq = 0;
        newParam.VolHlR = false;

        newParam.ModDir = false;
        newParam.ModSpd = 0;
        newParam.ModGain = 0;
        newParam.ModDi = false;
        newParam.ModFrq = 0;
        newParam.ModCnt = 0;

        newParam.EnvSpd = 0;
        newParam.EnvVolSw = false;
        newParam.EnvModSw = false;

        newParam.MasterVol = 0;
        newParam.WE = false;
    }

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneFDS();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 56));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmFDS
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(320, 56));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmFDS");
        this.setTitle("FDS");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    private JPanel pbScreen;
}
