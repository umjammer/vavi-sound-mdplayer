package mdplayer.form.kb.psg;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import mdplayer.Common;
import mdplayer.chips.Ay8910Chip.Params;
import mdplayer.form.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.ScreenPanel;
import mdplayer.Tables;
import mdplayer.chips.Ay8910Chip;
import mdplayer.form.kb.FormChipBase;
import mdplayer.form.sys.FormMain;
import mdsound.instrument.Ay8910Inst;

import static mdplayer.Common.searchSSGNote;


public class FormAY8910 extends FormChipBase<Params> {

    BufferedImage image;

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        //
        // pbScreen
        //
        this.image = Common.getImage("planeAY8910");
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 40));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmAY8910
        //
        this.setPreferredSize(new Dimension(320, 40));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Common.getImage("Feli128"));
        this.setName("frmAY8910");
        this.setTitle("AY8910");
        this.addWindowListener(this.windowListener);
    }

    public FormAY8910(FormMain frm, int chipId, int zoom, Ay8910Chip.Params newParam, Ay8910Chip.Params oldParam) {
        super(frm, chipId, zoom, newParam, oldParam);

        initializeComponent();

        frameBuffer.add(this.pbScreen, Common.getImage("planeAY8910"), null, zoom);

        boolean AY8910Type = (chipId == 0) ? parent.setting.getAY8910Type()[0].getUseReal()[0] : parent.setting.getAY8910Type()[1].getUseReal()[0];
        int AY8910SoundLocation = (chipId == 0) ? parent.setting.getAY8910Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getAY8910Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !AY8910Type ? 0 : (AY8910SoundLocation < 0 ? 2 : 1);

        screenInitAY8910(frameBuffer, tp);
        update();
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosAY8910()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosAY8910()[chipId] = getBounds().getLocation();
            }
            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent ev) {
            setLocation(new Point(x, y));

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Common.getImage("planeAY8910").getWidth() * zoom,
                frameSizeH + Common.getImage("planeAY8910").getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Common.getImage("planeAY8910").getWidth() * zoom,
                frameSizeH + Common.getImage("planeAY8910").getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Common.getImage("planeAY8910").getWidth() * zoom,
                frameSizeH + Common.getImage("planeAY8910").getHeight() * zoom));
    }

    @Override
    public void changeScreenParams() {
        int[] AY8910Register = audio.plugin.chipRegister.chip(Ay8910Chip.class).read(chipId);

        for (int ch = 0; ch < 3; ch++) { // SSG

            MDChipParams.Channel channel = newParam.channels[ch];

            boolean t = (AY8910Register[0x07] & (0x1 << ch)) == 0;
            boolean n = (AY8910Register[0x07] & (0x8 << ch)) == 0;
//logger.log(Level.TRACE, "r[8]=%x r[9]=%x r[10]=%x".formatted(AY8910Register[0x8], AY8910Register[0x9], AY8910Register[0xa]);
            channel.tn = (t ? 1 : 0) + (n ? 2 : 0);
            newParam.nfrq = AY8910Register[0x06] & 0x1f;
            newParam.efrq = AY8910Register[0x0c] * 0x100 + AY8910Register[0x0b];
            newParam.etype = (AY8910Register[0x0d] & 0xf);

            int v = (AY8910Register[0x08 + ch] & 0x1f);
            v = Math.min(v, 15);
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
                float fTone = clock(Ay8910Inst.class) / (8.0f * (float) tp);
                channel.note = searchSSGNote(fTone);
            }
        }
    }

    public static void screenInitAY8910(FrameBuffer screen, int tp) {
        for (int ch = 0; ch < 3; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                screen.drawKbn(32 + kx, ch * 8 + 8, kt, tp);
            }
            screen.drawFont8(296, ch * 8 + 8, 1, "   ");

            // Volume
            int d = 99;
            d = screen.drawVolumeM(256, 8 + ch * 8, 0, d, 0, tp);

            Boolean db = null;
            db = drawChAY8910(screen, ch, db, false, tp);
        }
    }

    @Override
    public void drawScreenParams() {
        boolean AY8910Type = (chipId == 0) ? parent.setting.getAY8910Type()[0].getUseReal()[0] : parent.setting.getAY8910Type()[1].getUseReal()[0];
        int AY8910SoundLocation = (chipId == 0) ? parent.setting.getAY8910Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getAY8910Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !AY8910Type ? 0 : (AY8910SoundLocation < 0 ? 2 : 1);

        for (int c = 0; c < 3; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            oyc.volume = frameBuffer.drawVolumeM(256, 8 + c * 8, 0, oyc.volume, nyc.volume, tp);
            oyc.note = frameBuffer.drawKeyBoard(c, oyc.note, nyc.note, tp);
            int[] r = drawToneNoise(frameBuffer, 6, 2, c, oyc.tn, nyc.tn, oyc.tntp, tp * 2 + (nyc.mask ? 1 : 0));
            oyc.tn = r[0]; oyc.tntp = r[1];

            drawChAY8910(frameBuffer, c, oyc.mask, nyc.mask, tp);
        }

        oldParam.nfrq = frameBuffer.Nfrq(5, 8, oldParam.nfrq, newParam.nfrq);
        oldParam.efrq = frameBuffer.drawEfrq(18, 8, oldParam.efrq, newParam.efrq);
        oldParam.etype = frameBuffer.drawEType(33, 8, oldParam.etype, newParam.etype);
    }

    @Override
    public void initScreen() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
            newParam.channels[c].volume = -1;
            newParam.channels[c].tn = -1;
        }
        newParam.nfrq = 0;
        newParam.efrq = 0;
        newParam.etype = 0;

        boolean AY8910Type = (chipId == 0) ? parent.setting.getAY8910Type()[0].getUseReal()[0] : parent.setting.getAY8910Type()[1].getUseReal()[0];
        int AY8910SoundLocation = (chipId == 0) ? parent.setting.getAY8910Type()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getAY8910Type()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = !AY8910Type ? 0 : (AY8910SoundLocation < 0 ? 2 : 1);

        screenInitAY8910(frameBuffer, tp);
        update();
    }

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            //  For top label row, do nothing
            if (py < 1 * 8) {
                //  However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (int ch = 0; ch < 3; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(Ay8910Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(Ay8910Chip.class, chipId, ch);
                    }
                }
                return;
            }

            // keyboard
            if (py < 4 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0)
                    return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // mask
                    parent.setChannelMask(Ay8910Chip.class, chipId, ch);
                    return;
                }

                //  Unmask.
                for (ch = 0; ch < 3; ch++)
                    parent.resetChannelMask(Ay8910Chip.class, chipId, ch);
            }
        }
    };

//#region draw buffer

    private static Boolean drawChAY8910(FrameBuffer screen, int ch, Boolean om, Boolean nm, int tp) {
        if (om == nm) {
            return om;
        }

        drawChAY8910_P(screen, 0, 8 + ch * 8, ch, nm != null && nm, tp);
        om = nm;
        return om;
    }

    private static int[] drawToneNoise(FrameBuffer screen, int x, int y, int c, int ot, int nt, int otp, int ntp) {
        if (ot == nt && otp == ntp) {
            return new int[] {ot, otp};
        }

        drawToneNoiseP(screen, x * 4, y * 4 + c * 8, nt, ntp);
        ot = nt;
        otp = ntp;
        return new int[] {ot, otp};
    }

    private static void drawToneNoiseP(FrameBuffer screen, int x, int y, int t, int tp) {
        if (screen == null)
            return;
        screen.drawByteArray(x, y, FrameBuffer.rPSGMode[tp], 32, 8 * t, 0, 8, 8);
    }

    private static void drawChAY8910_P(FrameBuffer screen, int x, int y, int ch, boolean mask, int tp) {
        if (screen == null)
            return;

        screen.drawByteArray(x, y, FrameBuffer.rType[tp * 2 + (mask ? 1 : 0)], 128, 32, 0, 16, 8);
        screen.drawFont8(x + 16, y, mask ? 1 : 0, String.valueOf(1 + ch));
    }

//#endregion
}
