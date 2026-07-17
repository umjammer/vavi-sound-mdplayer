package mdplayer.form.kb.pcm;

import java.awt.Dimension;
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

import mdplayer.Common;
import mdplayer.chips.C140Chip.Params;
import mdplayer.form.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.ScreenPanel;
import mdplayer.Tables;
import mdplayer.chips.C140Chip;
import mdplayer.form.kb.FormChipBase;
import mdplayer.form.sys.FormMain;
import mdsound.instrument.C140Inst;

import static mdplayer.form.FrameBuffer.rType;


public class FormC140 extends FormChipBase<Params> {

    static final Preferences prefs = Preferences.userNodeForPackage(FormC140.class);

    public FormC140(FormMain frm, int chipId, int zoom, C140Chip.Params newParam, C140Chip.Params oldParam) {
        super(frm, chipId, zoom, newParam, new C140Chip.Params());
        initializeComponent();

        bind(Common.getImage("planeF"));
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosC140()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosC140()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Common.getImage("planeF").getWidth() * zoom, frameSizeH + Common.getImage("planeF").getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Common.getImage("planeF").getWidth() * zoom, frameSizeH + Common.getImage("planeF").getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Common.getImage("planeF").getWidth() * zoom, frameSizeH + Common.getImage("planeF").getHeight() * zoom));
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

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;
            int ch;
            // For top label row, do nothing
            if (py < 1 * 8) {
                // However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (ch = 0; ch < 24; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(C140Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(C140Chip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 24) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(C140Chip.class, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 24; ch++) parent.resetChannelMask(C140Chip.class, chipId, ch);
            }
        }
    };

    private int searchC140Note(int freq) {
        double m = Double.MAX_VALUE;

        int clock = clock(C140Inst.class);
        if (clock >= 1000000)
            clock = clock / 384;

        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            //double a = Math.abs(freq - ((0x0800 << 2) * Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((int)(i / 12) - 4))));
            int a = (int) (
                    65536.0
                            / 2.0
                            / clock
                    //8000.0
                    //Tables.pcmMulTbl[i % 12 + 12]
                    //Math.pow(2, (i / 12 - 3))
            );
            if (freq > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public void initScreen() {
        boolean C140Type = (chipId == 0) ? parent.setting.getC140Type()[0].getUseReal()[0] : parent.setting.getC140Type()[1].getUseReal()[0];
        int tp = C140Type ? 1 : 0;
        for (int ch = 0; ch < 24; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                frameBuffer.drawKbn(32 + kx, ch * 8 + 8, kt, tp);
            }
            frameBuffer.drawFont8(396, ch * 8 + 8, 1, "   ");
            frameBuffer.drawPanType2P(24, ch * 8 + 8, 0, tp);
            drawChC140_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
            int d = 99;
            d = drawVolumeToC140(frameBuffer, ch, 1, d, 0, tp);
            d = 99;
            d = drawVolumeToC140(frameBuffer, ch, 2, d, 0, tp);
        }
    }

    public void changeScreenParams() {
        byte[] c140State = audio.plugin.chipRegister.chip(C140Chip.class).read(chipId);
        boolean[] c140KeyOn = (boolean[]) audio.plugin.chipRegister.chip(C140Chip.class).getInfo(chipId).get("keyOn");
        if (c140State != null) {
            for (int ch = 0; ch < 24; ch++) {
                int frequency = c140State[ch * 16 + 2] * 256 + c140State[ch * 16 + 3];
                int l = c140State[ch * 16 + 1];
                int r = c140State[ch * 16 + 0];

                newParam.channels[ch].note = searchC140Note(frequency) + 1;
                if (c140KeyOn[ch]) {
                    newParam.channels[ch].volumeL = Math.clamp((int) (l / 13.4) * 3L, 0, 19);
                    newParam.channels[ch].volumeR = Math.clamp((int) (r / 13.4) * 3L, 0, 19);
                } else {
                    newParam.channels[ch].volumeL -= newParam.channels[ch].volumeL > 0 ? 1 : 0;
                    newParam.channels[ch].volumeR -= newParam.channels[ch].volumeR > 0 ? 1 : 0;
                    if (newParam.channels[ch].volumeL == 0 && newParam.channels[ch].volumeR == 0) {
                        if (c140State[ch * 16 + 5] == 0) {
                            newParam.channels[ch].note = -1;
                        }
                        newParam.channels[ch].volumeL = 0;
                        newParam.channels[ch].volumeR = 0;
                    }
                }
                newParam.channels[ch].pan = ((l >> 4) & 0xf) | (((r >> 4) & 0xf) << 4);

                c140KeyOn[ch] = false;

                newParam.channels[ch].freq = ((c140State[ch * 16 + 2] & 0xff) << 8) | (c140State[ch * 16 + 3] & 0xff);
                newParam.channels[ch].bank = c140State[ch * 16 + 4];
                byte d = c140State[ch * 16 + 5];
                newParam.channels[ch].bit[0] = (d & 0x10) != 0;
                newParam.channels[ch].bit[1] = (d & 0x08) != 0;
                newParam.channels[ch].sadr = ((c140State[ch * 16 + 6] & 0xff) << 8) | (c140State[ch * 16 + 7] & 0xff);
                newParam.channels[ch].eadr = ((c140State[ch * 16 + 8] & 0xff) << 8) | (c140State[ch * 16 + 9] & 0xff);
                newParam.channels[ch].ladr = ((c140State[ch * 16 + 10] & 0xff) << 8) | (c140State[ch * 16 + 11] & 0xff);

            }
        }
    }

    public void drawScreenParams() {
        int tp = ((chipId == 0) ? parent.setting.getC140Type()[0].getUseReal()[0] : parent.setting.getC140Type()[1].getUseReal()[0]) ? 1 : 0;

        for (int c = 0; c < 24; c++) {

            MDChipParams.Channel orc = oldParam.channels[c];
            MDChipParams.Channel nrc = newParam.channels[c];

            orc.volumeL = drawVolumeToC140(frameBuffer, c, 1, orc.volumeL, nrc.volumeL, tp);
            orc.volumeR = drawVolumeToC140(frameBuffer, c, 2, orc.volumeR, nrc.volumeR, tp);
            orc.note = drawKeyBoardToC140(frameBuffer, c, orc.note, nrc.note, tp);
            orc.pan = frameBuffer.PanType2(c, orc.pan, nrc.pan, tp);

            orc.mask = drawChC140(frameBuffer, c, orc.mask, nrc.mask, tp);

            oldParam.channels[c].bit[0] = frameBuffer.drawNESSw(64 * 4, c * 8 + 8, oldParam.channels[c].bit[0], newParam.channels[c].bit[0]);
            oldParam.channels[c].bit[1] = frameBuffer.drawNESSw(65 * 4, c * 8 + 8, oldParam.channels[c].bit[1], newParam.channels[c].bit[1]);
            orc.freq = frameBuffer.font4Hex16Bit(4 * 67, c * 8 + 8, 0, orc.freq, nrc.freq);
            orc.bank = frameBuffer.font4HexByte(4 * 72, c * 8 + 8, 0, orc.bank, nrc.bank);
            orc.sadr = frameBuffer.font4Hex16Bit(4 * 75, c * 8 + 8, 0, orc.sadr, nrc.sadr);
            orc.eadr = frameBuffer.font4Hex16Bit(4 * 80, c * 8 + 8, 0, orc.eadr, nrc.eadr);
            orc.ladr = frameBuffer.font4Hex16Bit(4 * 85, c * 8 + 8, 0, orc.ladr, nrc.ladr);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        //
        // pbScreen
        //
        this.image = Common.getImage("planeF");
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 201));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmC140
        //
        this.setPreferredSize(new Dimension(320, 201));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Common.getImage("Feli128"));
        this.setMaximumSize(new Dimension(336, 240));
        this.setMinimumSize(new Dimension(336, 240));
        this.setName("frmC140");
        this.setTitle("C140Inst");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;

//#region draw buffer

    private static int drawVolumeToC140(FrameBuffer screen, int y, int c, /* ref */ int ov, int nv, int tp) {
        if (ov == nv)
            return ov;

        int t = 0;
        int sy = 0;
        if (c == 1 || c == 2) {
            t = 4;
        }
        if (c == 2) {
            sy = 4;
        }
        y = (y + 1) * 8;

        for (int i = 0; i <= 19; i++) {
            screen.drawVolumeP(356 + i * 2, y + sy, (1 + t), tp);
        }

        for (int i = 0; i <= nv; i++) {
            screen.drawVolumeP(356 + i * 2, y + sy, i > 17 ? (2 + t) : (0 + t), tp);
        }

        ov = nv;
        return ov;
    }

    private static int drawKeyBoardToC140(FrameBuffer screen, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return ot;

        int kx;
        int kt;

        y = (y + 1) * 8;

        if (ot >= 0) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            screen.drawKbn(32 + kx, y, kt, tp);
        }

        if (nt >= 0) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            screen.drawKbn(32 + kx, y, kt, tp);
            screen.drawFont8(396, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 8) {
                screen.drawFont8(412, y, 1, Tables.kbo[nt / 12]);
            }
        } else {
            screen.drawFont8(396, y, 1, "   ");
        }

        ot = nt;
        return ot;
    }

    private static Boolean drawChC140(FrameBuffer screen, int ch, Boolean om, Boolean nm, int tp) {
        if (om == nm) {
            return om;
        }

        drawChC140_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
        return om;
    }

    public static void drawChC140_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        //if (ch < 9) drawFont8(screen, x + 16, y, mask ? 1 : 0, (1 + ch).toString());
        //else
        screen.drawFont4(x + 16, y, mask ? 1 : 0, "%2d".formatted(1 + ch));
    }

//#endregion
}
