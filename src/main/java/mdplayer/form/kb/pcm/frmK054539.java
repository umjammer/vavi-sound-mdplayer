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
import mdplayer.DrawBuff;
import mdplayer.MDChipParams;
import mdplayer.ScreenPanel;
import mdplayer.Tables;
import mdplayer.chips.K054539Chip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.instrument.K054539Inst;

public class frmK054539 extends frmChipBase<MDChipParams.K054539> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmK054539.class);

    private static final int[] pantbl = new int[] {
            0 * 5 + 4, 1 * 5 + 4, 1 * 5 + 4, 2 * 5 + 4, 2 * 5 + 4, 3 * 5 + 4, 3 * 5 + 4,
            4 * 5 + 4,
            4 * 5 + 3, 4 * 5 + 3, 4 * 5 + 2, 4 * 5 + 2, 4 * 5 + 1, 4 * 5 + 1, 4 * 5 + 0,
    };

    public frmK054539(frmMain frm, int chipId, int zoom, MDChipParams.K054539 newParam, MDChipParams.K054539 oldParam) {
        super(frm, chipId, zoom, newParam, new MDChipParams.K054539());

        initializeComponent();

        bind(Resources.getPlaneK054539());
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosK054539()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosK054539()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneK054539().getWidth() * zoom, frameSizeH + Resources.getPlaneK054539().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneK054539().getWidth() * zoom, frameSizeH + Resources.getPlaneK054539().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneK054539().getWidth() * zoom, frameSizeH + Resources.getPlaneK054539().getHeight() * zoom));
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
            if (py < 1 * 8) {
                if (px < 8) {
                    for (ch = 0; ch < 8; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(K054539Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(K054539Chip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 8) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(K054539Chip.class, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 8; ch++) parent.resetChannelMask(K054539Chip.class, chipId, ch);
            }
        }
    };

    public void screenInit() {
        int tp = 0;
        for (int ch = 0; ch < 8; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
        }
    }

    private int searchK054539Note(int freq, int clock) {
        if (clock >= 1000000) clock /= 384;
        int hz = (int) (clock / (0x10000 / (double) freq));

        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            int a = (int) (4000.0
                    * Tables.pcmMulTbl[i % 12 + 12]
                    * Math.pow(2, (i / 12 - 3 + 2)));

            if (hz > a) {
                n = i;
            }
        }
        return n + 1;
    }

    public void screenChangeParams() {
        Map<String, Object> info = audio.plugin.chipRegister.chip(K054539Chip.class).getInfo(chipId);
        if (info == null) return;

        int[] regs = (int[]) info.get("regs");
        int clock = clock(K054539Inst.class);

        for (int ch = 0; ch < 8; ch++) {
            int pan = regs[0x20 * ch + 0x05];
            if (pan >= 0x81 && pan <= 0x8f)
                pan -= 0x81;
            else if (pan >= 0x11 && pan <= 0x1f)
                pan -= 0x11;
            else
                pan = 0x18 - 0x11;
            newParam.channels[ch].pan = pantbl[pan];

            newParam.channels[ch].sadr = regs[0x20 * ch + 0x0c]
                    + (regs[0x20 * ch + 0x0d] << 8)
                    + (regs[0x20 * ch + 0x0e] << 16);
            newParam.channels[ch].eadr = regs[0x20 * ch + 0x08]
                    + (regs[0x20 * ch + 0x09] << 8)
                    + (regs[0x20 * ch + 0x0a] << 16);
            newParam.channels[ch].echo = regs[0x20 * ch + 0x00]
                    + (regs[0x20 * ch + 0x01] << 8)
                    + (regs[0x20 * ch + 0x02] << 16);
            newParam.channels[ch].freq = regs[0x20 * ch + 0x06]
                    + (regs[0x20 * ch + 0x07] << 8);
            newParam.channels[ch].kf = regs[0x20 * ch + 0x04];
            newParam.channels[ch].bank = (regs[0x200 + 0x2 * ch] & 0xc) >> 2;
            newParam.channels[ch].loopFlg = (regs[0x200 + 0x2 * ch + 1] & 0x1) != 0;
            newParam.channels[ch].ex = (regs[0x200 + 0x2 * ch] & 0x20) != 0;
            newParam.channels[ch].volume = regs[0x20 * ch + 0x03];
            int vol = 0x40 - Common.range(newParam.channels[ch].volume, 0, 0x40);
            newParam.channels[ch].dda = (regs[0x214] & (1 << ch)) != 0;
            newParam.channels[ch].noise = (regs[0x215] & (1 << ch)) != 0;

            if ((regs[0x22c] & (1 << ch)) != 0) {
                newParam.channels[ch].volumeL = Common.range(vol * 19 * (newParam.channels[ch].pan / 5) / 4 / 0x40, 0, 19);
                newParam.channels[ch].volumeR = Common.range(vol * 19 * (newParam.channels[ch].pan % 5) / 4 / 0x40, 0, 19);
                newParam.channels[ch].note = searchK054539Note(newParam.channels[ch].echo, clock);
            } else {
                if (newParam.channels[ch].volumeL > 0) newParam.channels[ch].volumeL--;
                if (newParam.channels[ch].volumeR > 0) newParam.channels[ch].volumeR--;
                newParam.channels[ch].note = -1;
            }
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 8; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 9, ch * 8 + 8 * 10, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 17, ch * 8 + 8 * 10, 0, oyc.eadr, nyc.eadr);
            DrawBuff.font4Hex24Bit(frameBuffer, 4 * 25, ch * 8 + 8 * 10, 0, oyc.echo, nyc.echo);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 33, ch * 8 + 8 * 10, 0, oyc.freq, nyc.freq);
            oyc.kf = DrawBuff.font4HexByte(frameBuffer, 4 * 39, ch * 8 + 8 * 10, 0, oyc.kf, nyc.kf);
            oyc.bank = DrawBuff.font4HexByte(frameBuffer, 4 * 43, ch * 8 + 8 * 10, 0, oyc.bank, nyc.bank);
            DrawBuff.drawNESSw(frameBuffer, 4 * 46, ch * 8 + 8 * 10, oyc.loopFlg, nyc.loopFlg);
            DrawBuff.drawNESSw(frameBuffer, 4 * 48, ch * 8 + 8 * 10, oyc.ex, nyc.ex);
            oyc.volume = DrawBuff.font4HexByte(frameBuffer, 4 * 51, ch * 8 + 8 * 10, 0, oyc.volume, nyc.volume);
            DrawBuff.drawNESSw(frameBuffer, 4 * 54, ch * 8 + 8 * 10, oyc.dda, nyc.dda);
            DrawBuff.drawNESSw(frameBuffer, 4 * 56, ch * 8 + 8 * 10, oyc.noise, nyc.noise);

            DrawBuff.PanType4(frameBuffer, 4 * 6, ch * 8 + 8 * 1, oyc.pan, nyc.pan, 0);
            DrawBuff.KeyBoard(frameBuffer, ch, oyc.note, nyc.note, 0);
            DrawBuff.Volume(frameBuffer, 4 * 64, ch * 8 + 8, 1, oyc.volumeL, nyc.volumeL, 0);
            DrawBuff.Volume(frameBuffer, 4 * 64, ch * 8 + 12, 1, oyc.volumeR, nyc.volumeR, 0);
            DrawBuff.ChC352(frameBuffer, ch, oyc.mask, nyc.mask, 0);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        this.image = Resources.getPlaneK054539();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);

        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Resources.getFeli128());
        this.setName("frmK054539");
        this.setTitle("K054539");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;
}
