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
import java.util.Map;
import java.util.prefs.Preferences;

import mdplayer.Common;
import mdplayer.chips.Rf5C68Chip.Params;
import mdplayer.form.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.ScreenPanel;
import mdplayer.Tables;
import mdplayer.chips.Rf5C68Chip;
import mdplayer.form.kb.FormChipBase;
import mdplayer.form.sys.FormMain;


public class FormRf5c68 extends FormChipBase<Params> {

    static final Preferences prefs = Preferences.userNodeForPackage(FormRf5c68.class);

    public FormRf5c68(FormMain frm, int chipId, int zoom, Rf5C68Chip.Params newParam, Rf5C68Chip.Params oldParam) {
        super(frm, chipId, zoom, newParam, oldParam);

        initializeComponent();

        frameBuffer.add(pbScreen, Common.getImage("planeC"), null, zoom);
        drawScreenInitRF5C68(frameBuffer);
        update();
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosRf5c68()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosRf5c68()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Common.getImage("planeC").getWidth() * zoom, frameSizeH + Common.getImage("planeC").getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Common.getImage("planeC").getWidth() * zoom, frameSizeH + Common.getImage("planeC").getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Common.getImage("planeC").getWidth() * zoom, frameSizeH + Common.getImage("planeC").getHeight() * zoom));
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

    public void changeScreenParams() {
        Map<String, Object> rf5c68Register = audio.plugin.chipRegister.chip(Rf5C68Chip.class).getInfo(chipId);
        if (rf5c68Register != null) {
            //int[][] rf5c164Vol = audio.GetRf5c164Volume(chipId);
            for (int ch = 0; ch < 8; ch++) {
                if (newParam.channels[ch].volume > 0) newParam.channels[ch].volume--;

                if ((boolean) rf5c68Register.get("Channel" + ch + ".enable")) {
                    newParam.channels[ch].note = searchRf5c68Note((int) rf5c68Register.get("Channel" + ch + ".step"));
                    if ((boolean) rf5c68Register.get("Channel" + ch + ".keyOn")) {
                        newParam.channels[ch].volume = (int) rf5c68Register.get("Channel" + ch + ".env");
                        rf5c68Register.put("Channel" + ch + ".keyOn", false);
                    }
                    int MUL_L = (newParam.channels[ch].volume * ((int) rf5c68Register.get("Channel" + ch + ".pan") & 0x0F)) >> 5;
                    int MUL_R = (newParam.channels[ch].volume * ((int) rf5c68Register.get("Channel" + ch + ".pan") >> 4)) >> 5;
                    newParam.channels[ch].volumeL = Math.clamp(MUL_L / 3, 0, 19);
                    newParam.channels[ch].volumeR = Math.clamp(MUL_R / 3, 0, 19);
                } else {
                    newParam.channels[ch].volume = 0;
                    newParam.channels[ch].volumeL = 0;
                    newParam.channels[ch].volumeR = 0;
                }
                if (newParam.channels[ch].volumeL == 0 && newParam.channels[ch].volumeR == 0)
                    newParam.channels[ch].note = -1;
                else if (!(boolean) rf5c68Register.get("Channel" + ch + ".key")) {
                    newParam.channels[ch].note = -1;
                    newParam.channels[ch].volume = 0;
                    newParam.channels[ch].volumeL = 0;
                    newParam.channels[ch].volumeR = 0;
                }

                newParam.channels[ch].pan = (int) rf5c68Register.get("Channel" + ch + ".pan");
            }
        }
    }

    public void drawScreenParams() {
        for (int c = 0; c < 8; c++) {

            MDChipParams.Channel orc = oldParam.channels[c];
            MDChipParams.Channel nrc = newParam.channels[c];

            orc.volumeL = frameBuffer.drawVolumeM(256, 8 + c * 8, 1, orc.volumeL, nrc.volumeL, 0);
            orc.volumeR = frameBuffer.drawVolumeM(256, 8 + c * 8, 2, orc.volumeR, nrc.volumeR, 0);
            orc.note = frameBuffer.drawKeyBoard(c, orc.note, nrc.note, 0);
            orc.pan = frameBuffer.PanType2(c, orc.pan, nrc.pan, 0);
            orc.mask = drawChRF5C164(frameBuffer, c, orc.mask, nrc.mask, 0);
        }
    }

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
                    for (ch = 0; ch < 8; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(Rf5C68Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(Rf5C68Chip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ev.getButton() == MouseEvent.BUTTON1) {
                parent.setChannelMask(Rf5C68Chip.class, chipId, ch);
                return;
            }

            for (ch = 0; ch < 8; ch++) parent.resetChannelMask(Rf5C68Chip.class, chipId, ch);
        }
    };

    private static int searchRf5c68Note(int freq) {
        double m = Double.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            double a = Math.abs(freq - (0x0800 * Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((i / 12) - 4))));
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public void initScreen() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volumeL = -1;
            newParam.channels[c].volumeR = -1;
            newParam.channels[c].pan = -1;
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        //
        // pbScreen
        //
        this.image = Common.getImage("planeC");
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 72));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmRf5c68
        //
        this.setPreferredSize(new Dimension(320, 72));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Common.getImage("Feli128"));
        this.setMaximumSize(new Dimension(336, 111));
        this.setMinimumSize(new Dimension(336, 111));
        this.setName("frmRf5c68");
        this.setTitle("RF5C68");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;

//#region draw buffer

    private static void drawScreenInitRF5C68(FrameBuffer screen) {
        // RF5C164
        for (int ch = 0; ch < 8; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                screen.drawKbn(32 + kx, ch * 8 + 8, kt, 0);
            }
            screen.drawFont8(296, ch * 8 + 8, 1, "   ");
            screen.drawPanType2P(24, ch * 8 + 8, 0, 0);
        }
    }

    private static Boolean drawChRF5C164(FrameBuffer screen, int ch, Boolean om, Boolean nm, int tp) {
        if (om == nm) {
            return om;
        }

        drawChRF5C164_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
        return om;
    }

    private static void drawChRF5C164_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, FrameBuffer.rType[tp * 2 + (mask ? 1 : 0)], 128, 16, 0, 16, 8);
        screen.drawFont8(x + 16, y, mask ? 1 : 0, String.valueOf(ch + 1));
    }

//#endregion
}
