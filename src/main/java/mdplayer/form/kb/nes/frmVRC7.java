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


public class frmVRC7 extends frmBase {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.VRC7 newParam;
    private MDChipParams.VRC7 oldParam = new MDChipParams.VRC7();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmVRC7.class);

    public frmVRC7(frmMain frm, int chipID, int zoom, MDChipParams.VRC7 newParam) {
        super(frm);

        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getplaneVRC7(), null, zoom);
        boolean VRC7Type = false;
        int tp = VRC7Type ? 1 : 0;
        DrawBuff.screenInitVRC7(frameBuffer, tp);
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
                parent.setting.getLocation().getPosVrc7()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosVrc7()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneVRC7().getWidth() * zoom, frameSizeH + Resources.getplaneVRC7().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneVRC7().getWidth() * zoom, frameSizeH + Resources.getplaneVRC7().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneVRC7().getWidth() * zoom, frameSizeH + Resources.getplaneVRC7().getHeight() * zoom));
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
        byte[] vrc7Register = Audio.getVRC7Register(chipID);
        if (vrc7Register == null) return;

        //キーオン(ワンショット)があったかを取得する
        mdplayer.ChipRegister.ChipKeyInfo ki = Audio.getVRC7KeyInfo(chipID);

        for (int ch = 0; ch < 6; ch++) {
            MDChipParams.Channel nyc = newParam.channels[ch];

            //音色番号
            nyc.inst[0] = (vrc7Register[0x30 + ch] & 0xf0) >> 4;
            //サスティンの取得
            nyc.inst[1] = (vrc7Register[0x20 + ch] & 0x20) >> 5;
            //現在のキーオン状態
            nyc.inst[2] = (vrc7Register[0x20 + ch] & 0x10) >> 4;
            //ボリューム
            nyc.inst[3] = (vrc7Register[0x30 + ch] & 0x0f);

            //再生周波数
            int freq = vrc7Register[0x10 + ch] + ((vrc7Register[0x20 + ch] & 0x1) << 8);
            //オクターブ
            int oct = ((vrc7Register[0x20 + ch] & 0xe) >> 1);
            //周波数とオクターブ情報から近似する音程を取得する
            nyc.note = mdplayer.Common.searchSegaPCMNote(freq / 172.0) + (oct - 4) * 12;


            //ワンショット(前回の処理から比較してキーオンが1度以上発生している状態)の場合
            if (ki.On[ch]) {
                //ボリュームメーターを振る
                nyc.volumeL = (19 - nyc.inst[3]);
            } else {
                //ワンショットが無く、現在もキーオンしていない場合は音程を無しにする。
                //ワンショットが無くても、キーオン状態ならば音程をリセットしない。
                //(持続している場合やベンドやスラーをしていることが考えられる為。)
                //また、この処理はワンショットが発生しているときは実施しない。
                //ワンショットが有り、現在はキーオンしていない場合に対応するため。
                //上記ケースは、ボリュームメータを振り、音程表示は一瞬だけ表示する動きになる
                if (nyc.inst[2] == 0) nyc.note = -1;

                //ボリュームメータの減衰処理(音色設定を無視し常に一定)
                nyc.volumeL--;
                if (nyc.volumeL < 0) nyc.volumeL = 0;
            }
        }

        newParam.channels[0].inst[4] = (vrc7Register[0x02] & 0x3f);//TL
        newParam.channels[0].inst[5] = (vrc7Register[0x03] & 0x07);//FB

        newParam.channels[0].inst[6] = (vrc7Register[0x04] & 0xf0) >> 4;//AR
        newParam.channels[0].inst[7] = (vrc7Register[0x04] & 0x0f);//DR
        newParam.channels[0].inst[8] = (vrc7Register[0x06] & 0xf0) >> 4;//SL
        newParam.channels[0].inst[9] = (vrc7Register[0x06] & 0x0f);//RR
        newParam.channels[0].inst[10] = (vrc7Register[0x02] & 0x80) >> 7;//KL
        newParam.channels[0].inst[11] = (vrc7Register[0x00] & 0x0f);//MT
        newParam.channels[0].inst[12] = (vrc7Register[0x00] & 0x80) >> 7;//AM
        newParam.channels[0].inst[13] = (vrc7Register[0x00] & 0x40) >> 6;//VB
        newParam.channels[0].inst[14] = (vrc7Register[0x00] & 0x20) >> 5;//EG
        newParam.channels[0].inst[15] = (vrc7Register[0x00] & 0x10) >> 4;//KR
        newParam.channels[0].inst[16] = (vrc7Register[0x03] & 0x08) >> 3;//DM
        newParam.channels[0].inst[17] = (vrc7Register[0x05] & 0xf0) >> 4;//AR
        newParam.channels[0].inst[18] = (vrc7Register[0x05] & 0x0f);//DR
        newParam.channels[0].inst[19] = (vrc7Register[0x07] & 0xf0) >> 4;//SL
        newParam.channels[0].inst[20] = (vrc7Register[0x07] & 0x0f);//RR
        newParam.channels[0].inst[21] = (vrc7Register[0x03] & 0x80) >> 7;//KL
        newParam.channels[0].inst[22] = (vrc7Register[0x01] & 0x0f);//MT
        newParam.channels[0].inst[23] = (vrc7Register[0x01] & 0x80) >> 7;//AM
        newParam.channels[0].inst[24] = (vrc7Register[0x01] & 0x40) >> 6;//VB
        newParam.channels[0].inst[25] = (vrc7Register[0x01] & 0x20) >> 5;//EG
        newParam.channels[0].inst[26] = (vrc7Register[0x01] & 0x10) >> 4;//KR
        newParam.channels[0].inst[27] = (vrc7Register[0x03] & 0x10) >> 4;//DC
    }

    public void screenDrawParams() {
        int tp = 0;

        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int c = 0; c < 6; c++) {

            oyc = oldParam.channels[c];
            nyc = newParam.channels[c];

            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);

            DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 37, (c / 3) * 2 + 16, oyc.inst[0], nyc.inst[0]);
            DrawBuff.susFlag(frameBuffer, (c % 3) * 16 + 41, (c / 3) * 2 + 16, 0, oyc.inst[1], nyc.inst[1]);
            DrawBuff.susFlag(frameBuffer, (c % 3) * 16 + 44, (c / 3) * 2 + 16, 0, oyc.inst[2], nyc.inst[2]);
            DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 46, (c / 3) * 2 + 16, oyc.inst[3], nyc.inst[3]);

            DrawBuff.chYM2413(frameBuffer, c, oyc.mask, nyc.mask, tp);
        }

        oyc = oldParam.channels[0];
        nyc = newParam.channels[0];
        DrawBuff.drawInstNumber(frameBuffer, 9, 14, oyc.inst[4], nyc.inst[4]); //TL
        DrawBuff.drawInstNumber(frameBuffer, 14, 14, oyc.inst[5], nyc.inst[5]); //FB

        for (int c = 0; c < 11; c++) {
            DrawBuff.drawInstNumber(frameBuffer, c * 3, 18, oyc.inst[6 + c], nyc.inst[6 + c]);
            DrawBuff.drawInstNumber(frameBuffer, c * 3, 20, oyc.inst[17 + c], nyc.inst[17 + c]);
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

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (int ch = 0; ch < 6; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.VRC7, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.VRC7, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤
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
                    //マスク
                    parent.setChannelMask(EnmChip.VRC7, chipID, ch);
                    return;
                }

                //マスク解除
                for (ch = 0; ch < 6; ch++) parent.resetChannelMask(EnmChip.VRC7, chipID, ch);
                return;
            }

            //音色欄
            if (py < 15 * 8 && px < 16 * 8) {
                //クリップボードに音色をコピーする
                parent.getInstCh(EnmChip.VRC7, 0, chipID);
            }
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmVRC7));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneVRC7();
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
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmVRC7");
        this.setTitle("VRC7");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
