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
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.Audio;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.K051649;


public class frmK051649 extends frmBase {

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmK051649));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneK051649();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 144));
        // this.pbScreen.TabIndex = 1
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmK051649
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 144));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmK051649");
        this.setTitle("K051649");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    // // #endregion

    BufferedImage image;
    public JPanel pbScreen;
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.K051649 newParam = null;
    private MDChipParams.K051649 oldParam = new MDChipParams.K051649();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmK051649.class);

    public frmK051649(frmMain frm, int chipID, int zoom, MDChipParams.K051649 newParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getplaneK051649(), null, zoom);
        DrawBuff.screenInitK051649(frameBuffer);
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
                parent.setting.getLocation().getPosK051649()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosK051649()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneK051649().getWidth() * zoom, frameSizeH + Resources.getplaneK051649().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneK051649().getWidth() * zoom, frameSizeH + Resources.getplaneK051649().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneK051649().getWidth() * zoom, frameSizeH + Resources.getplaneK051649().getHeight() * zoom));
        componentListener.componentResized(null);
    }

    private ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
        }
    };

    public void screenChangeParams() {
        K051649.K051649State chip = Audio.getK051649Register(chipID);
        if (chip == null) return;

        for (int ch = 0; ch < 5; ch++) {
            K051649.K051649State.Channel psg = chip.channelList[ch];
            if (psg == null) continue;

            MDChipParams.Channel channel = newParam.channels[ch];
            for (int i = 0; i < 32; i++) channel.inst[i] = (int) psg.waveRam[i];
            float ftone = Audio.clockK051649 / (8.0f * (float) psg.frequency);
            channel.freq = psg.frequency;
            channel.volume = psg.key != 0 ? (int) (psg.volume * 1.33) : 0;
            channel.volumeL = psg.volume;
            channel.note = (psg.key != 0 && channel.volume != 0) ? searchSSGNote(ftone) : -1;
            channel.dda = psg.key != 0;

        }
    }

    public void screenDrawParams() {
        int tp = parent.setting.getK051649Type()[0].getUseReal()[0] ? 1 : 0;

        for (int c = 0; c < 5; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];
            int x = c % 3;
            int y = c / 3;

            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.font4Hex12Bit(frameBuffer, x * 4 * 26 + 4 * 14, y * 8 * 6 + 8 * 11, tp, oyc.freq, nyc.freq);
            DrawBuff.font4Hex4Bit(frameBuffer, x * 4 * 26 + 4 * 22, y * 8 * 6 + 8 * 11, tp, oyc.volumeL, nyc.volumeL);
            DrawBuff.drawNESSw(frameBuffer, x * 4 * 26 + 4 * 25, y * 8 * 6 + 8 * 11, oyc.dda, nyc.dda);
            DrawBuff.WaveFormToK051649(frameBuffer, c, oyc.typ, nyc.inst);

            DrawBuff.ChK051649(frameBuffer, c, oyc.mask, nyc.mask, tp);

            for (int i = 0; i < 32; i++) {
                int fx = i % 8;
                int fy = i / 8;
                DrawBuff.font4HexByte(frameBuffer, x * 4 * 26 + 4 * 10 + fx * 8, y * 8 * 6 + 8 * 7 + fy * 8, 0, oyc.inst[i], nyc.inst[i]);
            }
        }
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
                    for (int ch = 0; ch < 5; ch++) {
                        if (newParam.channels[ch].mask == true)
                            parent.resetChannelMask(EnmChip.K051649, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.K051649, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
            if (py < 6 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    //マスク
                    parent.setChannelMask(EnmChip.K051649, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 5; ch++) parent.resetChannelMask(EnmChip.K051649, chipID, ch);
                return;
            }

            //音色で右クリックした場合は何もしない
            if (ev.getButton() == MouseEvent.BUTTON2) return;

            // 音色表示欄の判定
            int instCh = ((py < 12 * 8) ? 0 : 3) + px / (8 * 13);

            if (instCh < 5) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.K051649, instCh, chipID);
            }
        }
    };

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volume = -1;
            newParam.channels[c].volumeL = -1;
        }
    }

    private int searchSSGNote(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            //if (freq < Tables.freqTbl[i]) break;
            //n = i;
            float a = Math.abs(freq - Tables.freqTbl[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }
}
