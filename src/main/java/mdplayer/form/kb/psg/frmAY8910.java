
package mdplayer.form.kb.psg;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import mdplayer.Audio;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.Tables;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmAY8910 extends frmChipBase {
    JPanel pbScreen;
    BufferedImage image;

    private void initializeComponent() {
        this.pbScreen = new JPanel();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getplaneAY8910();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 40));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmAY8910
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
//            //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 40));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
        this.setName("frmAY8910");
        this.setTitle("AY8910");
        this.addWindowListener(this.windowListener);
        // ((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);

    }

    public frmAY8910(frmMain frm, int chipID, int zoom, MDChipParams.AY8910 newParam, MDChipParams.AY8910 oldParam) {
        super(frm, chipID, zoom, newParam);

        initializeComponent();

        parent = frm;
        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;

        frameBuffer.Add(this.pbScreen, Resources.getplaneAY8910(), null, zoom);

        Boolean AY8910Type = (chipID == 0) ? parent.setting.getAY8910Type()[0].getUseReal()[0] : parent.setting.getAY8910Type()[1].getUseReal()[0];
        int AY8910SoundLocation = (chipID == 0) ? parent.setting.getAY8910Type()[0].getRealChipInfo()[0].getSoundLocation()
                                                : parent.setting.getAY8910Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !AY8910Type ? 0 : (AY8910SoundLocation < 0 ? 2 : 1);

        screenInitAY8910(frameBuffer, tp);
        update();
    }

    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosAY8910()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosAY8910()[chipID] = getBounds().getLocation();
            }
            isClosed = true;
        }

        public void windowOpened(WindowEvent ev) {
            setLocation(new Point(x, y));

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneAY8910().getWidth() * zoom,
                                          frameSizeH + Resources.getplaneAY8910().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneAY8910().getWidth() * zoom,
                                          frameSizeH + Resources.getplaneAY8910().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneAY8910().getWidth() * zoom,
                                            frameSizeH + Resources.getplaneAY8910().getHeight() * zoom));
    }

    @Override
    public void screenChangeParams() {
        int[] AY8910Register = Audio.getAY8910Register(chipID);

        for (int ch = 0; ch < 3; ch++) // SSG
        {
            MDChipParams.Channel channel = newParam.channels[ch];

            Boolean t = (AY8910Register[0x07] & (0x1 << ch)) == 0;
            Boolean n = (AY8910Register[0x07] & (0x8 << ch)) == 0;
            // System.err.println("r[8]={0:x} r[9]={1:x} r[10]={2:x}",
            // AY8910Register[0x8], AY8910Register[0x9], AY8910Register[0xa]);
            channel.tn = (t ? 1 : 0) + (n ? 2 : 0);
            newParam.nfrq = AY8910Register[0x06] & 0x1f;
            newParam.efrq = AY8910Register[0x0c] * 0x100 + AY8910Register[0x0b];
            newParam.etype = (AY8910Register[0x0d] & 0xf);

            int v = (AY8910Register[0x08 + ch] & 0x1f);
            v = v > 15 ? 15 : v;
            channel.volume = (int) (((t || n) ? 1 : 0) * v * (20.0 / 16.0));
            if (!t && !n && channel.volume > 0) {
                channel.volume--;
            }

            if (channel.volume == 0) {
                channel.note = -1;
            } else {
                int ft = AY8910Register[0x00 + ch * 2];
                int ct = AY8910Register[0x01 + ch * 2];
                int tp = (ct << 8) | ft;
                if (tp == 0)
                    tp = 1;
                float ftone = Audio.clockAY8910 / (8.0f * (float) tp);
                channel.note = searchSSGNote(ftone);
            }

        }
    }

    public static void screenInitAY8910(FrameBuffer screen, int tp) {
        for (int ch = 0; ch < 3; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(screen, 32 + kx, ch * 8 + 8, kt, tp);
            }
            DrawBuff.drawFont8(screen, 296, ch * 8 + 8, 1, "   ");

            // Volume
            int d = 99;
            DrawBuff.volume(screen, 256, 8 + ch * 8, 0, d, 0, tp);

            Boolean db = null;
            DrawBuff.ChAY8910(screen, ch, db, false, tp);
        }
    }

    @Override
    public void screenDrawParams() {
        Boolean AY8910Type = (chipID == 0) ? parent.setting.getAY8910Type()[0].getUseReal()[0] : parent.setting.getAY8910Type()[1].getUseReal()[0];
        int AY8910SoundLocation = (chipID == 0) ? parent.setting.getAY8910Type()[0].getRealChipInfo()[0].getSoundLocation()
                                                : parent.setting.getAY8910Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !AY8910Type ? 0 : (AY8910SoundLocation < 0 ? 2 : 1);

        for (int c = 0; c < 3; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
            DrawBuff.ToneNoise(frameBuffer, 6, 2, c, oyc.tn, nyc.tn, oyc.tntp, tp * 2 + (nyc.mask == true ? 1 : 0));

            DrawBuff.ChAY8910(frameBuffer, c, oyc.mask, nyc.mask, tp);

        }

        DrawBuff.Nfrq(frameBuffer, 5, 8, oldParam.nfrq, newParam.nfrq);
        DrawBuff.Efrq(frameBuffer, 18, 8, oldParam.efrq, newParam.efrq);
        DrawBuff.Etype(frameBuffer, 33, 8, oldParam.etype, newParam.etype);
    }

    @Override
    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volume = -1;
            newParam.channels[c].tn = -1;
        }
        newParam.nfrq = 0;
        newParam.efrq = 0;
        newParam.etype = 0;

        Boolean AY8910Type = (chipID == 0) ? parent.setting.getAY8910Type()[0].getUseReal()[0] : parent.setting.getAY8910Type()[1].getUseReal()[0];
        int AY8910SoundLocation = (chipID == 0) ? parent.setting.getAY8910Type()[0].getRealChipInfo()[0].getSoundLocation()
                                                : parent.setting.getAY8910Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !AY8910Type ? 0 : (AY8910SoundLocation < 0 ? 2 : 1);

        screenInitAY8910(frameBuffer, tp);
        update();
    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            // 上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                // 但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (int ch = 0; ch < 3; ch++) {
                        if (newParam.channels[ch].mask == true)
                            parent.resetChannelMask(EnmChip.AY8910, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.AY8910, chipID, ch);
                    }
                }
                return;
            }

            // 鍵盤
            if (py < 4 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0)
                    return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // マスク
                    parent.setChannelMask(EnmChip.AY8910, chipID, ch);
                    return;
                }

                // マスク解除
                for (ch = 0; ch < 3; ch++)
                    parent.resetChannelMask(EnmChip.AY8910, chipID, ch);
                return;
            }
        }
    };

    private int searchSSGNote(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            // if (freq < Tables.freqTbl[i]) break;
            // n = i;
            float a = Math.abs(freq - Tables.freqTbl[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }
}
