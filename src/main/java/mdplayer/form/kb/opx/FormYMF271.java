package mdplayer.form.kb.opx;

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
import mdplayer.chips.YmF271Chip.Params;
import mdplayer.form.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.ScreenPanel;
import mdplayer.Tables;
import mdplayer.chips.YmF271Chip;
import mdplayer.form.kb.FormChipBase;
import mdplayer.form.sys.FormMain;
import mdsound.instrument.YmF271Inst;

import static mdplayer.form.FrameBuffer.getByteArray;


public class FormYMF271 extends FormChipBase<Params> {

    static final Preferences prefs = Preferences.userNodeForPackage(FormYMF271.class);

    public FormYMF271(FormMain frm, int chipId, int zoom, YmF271Chip.Params newParam, YmF271Chip.Params oldParam) {
        super(frm, chipId, zoom, newParam, oldParam);

        rType_YMF271 = getByteArray(Common.getImage("rType_YMF271"));

        initializeComponent();

        frameBuffer.add(pbScreen, Common.getImage("planeYMF271"), null, zoom);
        screenInitYMF271(frameBuffer);
        update();
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosYMF271()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosYMF271()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Common.getImage("planeYMF271").getWidth() * zoom, frameSizeH + Common.getImage("planeYMF271").getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Common.getImage("planeYMF271").getWidth() * zoom, frameSizeH + Common.getImage("planeYMF271").getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Common.getImage("planeYMF271").getWidth() * zoom, frameSizeH + Common.getImage("planeYMF271").getHeight() * zoom));
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
        }
    };

    public void screenInitYMF271(FrameBuffer screen) {
        for (int ch = 0; ch < 48; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                screen.drawKbn(49 + kx, ch * 8 + 8, kt, 0);
            }
            screen.drawFont8(313, ch * 8 + 8, 1, "   ");
            screen.drawPanType2P(24, ch * 8 + 8, 0, 0);

            oldParam.channels[ch].tn = -1;
        }
    }

    public void changeScreenParams() {
        Map<String, Object> reg = audio.plugin.chipRegister.chip(YmF271Chip.class).getInfo(chipId);
        for (int i = 0; i < 48; i++) {
            int slot = YmF271Inst.slotTbl[i];

            int volume = (int) reg.get("slots." + slot + ".volume");
            int ch0Level = (int) reg.get("slots." + slot + ".ch0Level");
            int ch1Level = (int) reg.get("slots." + slot + ".ch1Level");
            newParam.channels[slot].volumeL = Math.clamp(((long) volume * ch0Level) >> 23, 0, 19);
            newParam.channels[slot].volumeR = Math.clamp(((long) volume * ch1Level) >> 23, 0, 19);
            newParam.channels[slot].pan = (int) reg.get("slots." + slot + ".pan");
            newParam.channels[slot].pantp = (int) reg.get("slots." + slot + ".pantp");
            newParam.channels[slot].inst[0] = (int) reg.get("slots." + slot + ".inst.0");
            newParam.channels[slot].inst[1] = (int) reg.get("slots." + slot + ".inst.1");
            newParam.channels[slot].inst[2] = (int) reg.get("slots." + slot + ".inst.2");
            newParam.channels[slot].inst[3] = (int) reg.get("slots." + slot + ".inst.3");
            newParam.channels[slot].inst[4] = (int) reg.get("slots." + slot + ".inst.4");
            newParam.channels[slot].inst[5] = (int) reg.get("slots." + slot + ".inst.5");
            newParam.channels[slot].inst[6] = (int) reg.get("slots." + slot + ".inst.6");
            newParam.channels[slot].inst[7] = (int) reg.get("slots." + slot + ".inst.7");
            newParam.channels[slot].inst[8] = (int) reg.get("slots." + slot + ".inst.8");
            newParam.channels[slot].inst[9] = (int) reg.get("slots." + slot + ".inst.9");
            newParam.channels[slot].inst[10] = (int) reg.get("slots." + slot + ".inst.10");
            newParam.channels[slot].inst[11] = (int) reg.get("slots." + slot + ".inst.11");
            newParam.channels[slot].inst[12] = (int) reg.get("slots." + slot + ".inst.12");

            newParam.channels[slot].inst[13] = (int) reg.get("slots." + slot + ".inst.13");
            newParam.channels[slot].inst[14] = (int) reg.get("slots." + slot + ".inst.14");

            newParam.channels[slot].inst[15] = (int) reg.get("slots." + slot + ".inst.15");
            newParam.channels[slot].inst[16] = (int) reg.get("slots." + slot + ".inst.16");
            newParam.channels[slot].inst[17] = (int) reg.get("slots." + slot + ".inst.17");

            newParam.channels[slot].inst[18] = (int) reg.get("slots." + slot + ".inst.18");
            newParam.channels[slot].inst[19] = (int) reg.get("slots." + slot + ".inst.19");
            newParam.channels[slot].inst[20] = (int) reg.get("slots." + slot + ".inst.20");
            newParam.channels[slot].inst[21] = (int) reg.get("slots." + slot + ".inst.21");

            newParam.channels[slot].inst[22] = (int) reg.get("slots." + slot + ".inst.22");
            newParam.channels[slot].inst[23] = (int) reg.get("slots." + slot + ".inst.23");
            newParam.channels[slot].inst[24] = (int) reg.get("slots." + slot + ".inst.24");
            newParam.channels[slot].inst[25] = (int) reg.get("slots." + slot + ".inst.25");

            // note
            if ((boolean) reg.get("slots." + slot + ".active")) {
                newParam.channels[slot].volumeL = Math.clamp(((long) volume * ch0Level) >> 23, 0, 19);
                newParam.channels[slot].volumeR = Math.clamp(((long) volume * ch1Level) >> 23, 0, 19);
                newParam.channels[slot].note = Common.searchSSGNote(newParam.channels[slot].inst[14]) + (((newParam.channels[slot].inst[13] + 8) & 0xf) - 11) * 12 - 7;
            } else {
                newParam.channels[slot].volumeL += newParam.channels[slot].volumeL > 0 ? -1 : 0;
                newParam.channels[slot].volumeR += newParam.channels[slot].volumeR > 0 ? -1 : 0;
                newParam.channels[slot].note = -1;
            }

            if (i % 4 == 0) {
                newParam.channels[slot].tn = (int) reg.get("slots." + slot + ".sync");
            }
        }
    }

    public void drawScreenParams() {
        for (int i = 0; i < 48; i++) {
            int slot = YmF271Inst.slotTbl[i];

            MDChipParams.Channel orc = oldParam.channels[slot];
            MDChipParams.Channel nrc = newParam.channels[slot];

            orc.volumeL = frameBuffer.drawVolumeM(273, 8 + i * 8, 1, orc.volumeL, nrc.volumeL, 0);
            orc.volumeR = frameBuffer.drawVolumeM(273, 12 + i * 8, 1, orc.volumeR, nrc.volumeR, 0);
            orc.echo = frameBuffer.font4Int2(25, 8 + i * 8, 0, 2, orc.echo, slot + 1); // slotnum
            orc.pan = frameBuffer.PanType2(33, 8 + i * 8, orc.pan, nrc.pan, 0);
            orc.pantp = frameBuffer.PanType2(41, 8 + i * 8, orc.pantp, nrc.pantp, 0);

            orc.note = drawKeyBoardXY(frameBuffer, 49, 8 + i * 8, orc.note, nrc.note, 0);

            orc.inst[0] = frameBuffer.font4Int2(357, 8 + i * 8, 0, 2, orc.inst[0], nrc.inst[0]); // AR
            orc.inst[1] = frameBuffer.font4Int2(365, 8 + i * 8, 0, 2, orc.inst[1], nrc.inst[1]); // DR
            orc.inst[2] = frameBuffer.font4Int2(373, 8 + i * 8, 0, 2, orc.inst[2], nrc.inst[2]); // SR
            orc.inst[3] = frameBuffer.font4Int2(381, 8 + i * 8, 0, 2, orc.inst[3], nrc.inst[3]); // RR
            orc.inst[4] = frameBuffer.font4Int2(389, 8 + i * 8, 0, 2, orc.inst[4], nrc.inst[4]); // SL
            orc.inst[5] = frameBuffer.font4Int3(397, 8 + i * 8, 0, 3, orc.inst[5], nrc.inst[5]); // TL
            orc.inst[6] = frameBuffer.font4Int1(413, 8 + i * 8, 0, orc.inst[6], nrc.inst[6]);       // KS
            orc.inst[7] = frameBuffer.font4Int2(417, 8 + i * 8, 0, 2, orc.inst[7], nrc.inst[7]); // ML
            orc.inst[8] = frameBuffer.font4Int1(429, 8 + i * 8, 0, orc.inst[8], nrc.inst[8]);       // DT
            orc.inst[9] = frameBuffer.font4Int1(437, 8 + i * 8, 0, orc.inst[9], nrc.inst[9]);       // WF
            orc.inst[10] = frameBuffer.font4Int1(445, 8 + i * 8, 0, orc.inst[10], nrc.inst[10]);     // FB
            orc.inst[11] = frameBuffer.font4Int1(449, 8 + i * 8, 0, orc.inst[11], nrc.inst[11]);     // accon
            orc.inst[12] = frameBuffer.font4Int2(453, 8 + i * 8, 0, 2, orc.inst[12], nrc.inst[12]); // algorithm
            orc.inst[13] = frameBuffer.font4Int2(465, 8 + i * 8, 0, 2, orc.inst[13], nrc.inst[13]); // algorithm
            orc.inst[14] = frameBuffer.font4Hex12Bit(477, 8 + i * 8, 0, orc.inst[14], nrc.inst[14]); // fns
            orc.inst[15] = frameBuffer.font4Hex24Bit(497, 8 + i * 8, 0, orc.inst[15], nrc.inst[15]); // startaddr
            orc.inst[16] = frameBuffer.font4Hex24Bit(525, 8 + i * 8, 0, orc.inst[16], nrc.inst[16]); // endaddr
            orc.inst[17] = frameBuffer.font4Hex24Bit(553, 8 + i * 8, 0, orc.inst[17], nrc.inst[17]); // loopaddr
            orc.inst[18] = frameBuffer.font4Int1(581, 8 + i * 8, 0, orc.inst[18], nrc.inst[18]); // fs
            orc.inst[19] = frameBuffer.font4Int1(585, 8 + i * 8, 0, orc.inst[19], nrc.inst[19]); // bits
            orc.inst[20] = frameBuffer.font4Int1(589, 8 + i * 8, 0, orc.inst[20], nrc.inst[20]); // srcnote
            orc.inst[21] = frameBuffer.font4Int1(593, 8 + i * 8, 0, orc.inst[21], nrc.inst[21]); // srcb

            orc.inst[22] = frameBuffer.font4Int3(601, 8 + i * 8, 0, 3, orc.inst[22], nrc.inst[22]); // lfofreq
            orc.inst[23] = frameBuffer.font4Int1(617, 8 + i * 8, 0, orc.inst[23], nrc.inst[23]); // lfowave
            orc.inst[24] = frameBuffer.font4Int1(621, 8 + i * 8, 0, orc.inst[24], nrc.inst[24]); // pms
            orc.inst[25] = frameBuffer.font4Int1(625, 8 + i * 8, 0, orc.inst[25], nrc.inst[25]); // ams

            if (i % 4 == 0) {
                orc.tn = drawOpxOP(frameBuffer, 17, 8 + i * 8, 0, orc.tn, nrc.tn & 3); // sync
            }
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        //
        // pbScreen
        //
        this.image = Common.getImage("planeYMF271");
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(656, 394));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYMF271
        //
        this.setPreferredSize(new Dimension(689, 477));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Common.getImage("Feli128"));
        this.setName("frmYMF271");
        this.setTitle("YMF271");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;

//#region draw buffer

    private static int drawKeyBoardXY(FrameBuffer screen, int x, int y, int ot, int nt, int tp) {
        if (ot == nt)
            return ot;

        int kx;
        int kt;

        if (ot >= 0 && ot < 12 * 8) {
            kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
            kt = Tables.kbl[(ot % 12) * 2 + 1];
            screen.drawKbn(x + kx, y, kt, tp);
        }

        if (nt >= 0 && nt < 12 * 8) {
            kx = Tables.kbl[(nt % 12) * 2] + nt / 12 * 28;
            kt = Tables.kbl[(nt % 12) * 2 + 1] + 4;
            screen.drawKbn(x + kx, y, kt, tp);
        }

        screen.drawFont8(264 + x, y, 1, "   ");

        if (nt >= 0) {
            screen.drawFont8(264 + x, y, 1, Tables.kbn[nt % 12]);
            if (nt / 12 < 10) {
                screen.drawFont8(280 + x, y, 1, Tables.kbo[nt / 12]);
            }
        }

        ot = nt;
        return ot;
    }

    private static int drawOpxOP(FrameBuffer screen, int x, int y, int t, int ot, int nt) {
        if (ot != nt) {
            screen.drawByteArray(x, y, rType_YMF271, 32, nt * 8, 0, 8, 32);

            ot = nt;
        }

        return ot;
    }

    private static byte[] rType_YMF271;

//#endregion
}
