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

import mdplayer.DrawBuff;
import mdplayer.MDChipParams;
import mdplayer.ScreenPanel;
import mdplayer.Tables;
import mdplayer.chips.K053260Chip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.instrument.K053260Inst;

public class frmK053260 extends frmChipBase<MDChipParams.K053260> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmK053260.class);

    public frmK053260(frmMain frm, int chipId, int zoom, MDChipParams.K053260 newParam, MDChipParams.K053260 oldParam) {
        super(frm, chipId, zoom, newParam, new MDChipParams.K053260());

        initializeComponent();

        bind(Resources.getPlaneK053260());
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosK053260()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosK053260()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneK053260().getWidth() * zoom, frameSizeH + Resources.getPlaneK053260().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneK053260().getWidth() * zoom, frameSizeH + Resources.getPlaneK053260().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneK053260().getWidth() * zoom, frameSizeH + Resources.getPlaneK053260().getHeight() * zoom));
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
                    for (ch = 0; ch < 4; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(K053260Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(K053260Chip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 4) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(K053260Chip.class, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 4; ch++) parent.resetChannelMask(K053260Chip.class, chipId, ch);
            }
        }
    };

    public void screenInit() {
        for (int ch = 0; ch < 4; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 33 + kx, ch * 8 + 8, kt, 0);
            }
            DrawBuff.PanType5(frameBuffer, 4 * 6 + 1, ch * 8 + 8, oldParam.channels[ch].panL, 0, 0);
            DrawBuff.PanType5(frameBuffer, 4 * 7 + 1, ch * 8 + 8, oldParam.channels[ch].panR, 0, 0);
        }
    }

    public void screenChangeParams() {
        Map<String, Object> info = audio.plugin.chipRegister.chip(K053260Chip.class).getInfo(chipId);
        if (info == null) return;

        int clock = clock(K053260Inst.class);
        for (int ch = 0; ch < 4; ch++) {
            newParam.channels[ch].freq = (int) info.get("channels." + ch + ".freq");
            newParam.channels[ch].eadr = (int) info.get("channels." + ch + ".size");
            newParam.channels[ch].sadr = (int) info.get("channels." + ch + ".start");
            newParam.channels[ch].bank = (int) info.get("channels." + ch + ".bank");
            newParam.channels[ch].pan = (int) info.get("channels." + ch + ".pan");
            newParam.channels[ch].panL = 8 - newParam.channels[ch].pan;
            newParam.channels[ch].panR = newParam.channels[ch].pan;
            int volume = (int) info.get("channels." + ch + ".volume");
            newParam.channels[ch].volume = volume / 2;
            newParam.channels[ch].bit[0] = (int) info.get("channels." + ch + ".play") != 0;
            newParam.channels[ch].bit[1] = (int) info.get("channels." + ch + ".dir") != 0;
            newParam.channels[ch].bit[2] = (int) info.get("channels." + ch + ".loop") != 0;
            newParam.channels[ch].bit[3] = (int) info.get("channels." + ch + ".ppcm") != 0;

            if (newParam.channels[ch].bit[0]) {
                newParam.channels[ch].volumeL = Math.min(volume * newParam.channels[ch].panL / 8 / 5 / 2, 19);
                newParam.channels[ch].volumeR = Math.min(volume * newParam.channels[ch].panR / 8 / 5 / 2, 19);
            } else {
                if (newParam.channels[ch].volumeL > 0) newParam.channels[ch].volumeL--;
                if (newParam.channels[ch].volumeR > 0) newParam.channels[ch].volumeR--;
            }
            newParam.channels[ch].panL /= 2;
            newParam.channels[ch].panR /= 2;

            int delta = (int) info.get("channels." + ch + ".delta");
            newParam.channels[ch].note = !newParam.channels[ch].bit[0] ? -1 : searchNote(delta, clock);
        }
    }

    private int searchNote(int freq, int clock) {
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            int a = (int) (0x10000
                    * 8000.0
                    * Tables.pcmMulTbl[i % 12 + 12]
                    * Math.pow(2, (i / 12 - 3 + 2))
                    / clock
                    * 6
                    * 2);

            if (freq > a) {
                n = i;
            }
        }
        return Math.min(Math.max(n - 2, 0), 95);
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 4; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 69 + 1, ch * 8 + 8, 0, oyc.freq, nyc.freq);
            oyc.bank = DrawBuff.font4HexByte(frameBuffer, 4 * 74 + 1, ch * 8 + 8, 0, oyc.bank, nyc.bank);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 77 + 1, ch * 8 + 8, 0, oyc.sadr, nyc.sadr);
            DrawBuff.font4Hex16Bit(frameBuffer, 4 * 82 + 1, ch * 8 + 8, 0, oyc.eadr, nyc.eadr);
            oyc.pan = DrawBuff.font4HexByte(frameBuffer, 4 * 87 + 1, ch * 8 + 8, 0, oyc.pan, nyc.pan);
            oyc.volume = DrawBuff.font4HexByte(frameBuffer, 4 * 90 + 1, ch * 8 + 8, 0, oyc.volume, nyc.volume);

            for (int b = 0; b < 4; b++) {
                DrawBuff.drawNESSw(frameBuffer, 64 * 4 + b * 4 + 1, ch * 8 + 8, oldParam.channels[ch].bit[b], newParam.channels[ch].bit[b]);
            }

            DrawBuff.PanType5(frameBuffer, 4 * 6 + 1, ch * 8 + 8, oyc.panL, nyc.panL, 0);
            DrawBuff.PanType5(frameBuffer, 4 * 7 + 1, ch * 8 + 8, oyc.panR, nyc.panR, 0);
            DrawBuff.VolumeXY1(frameBuffer, 4 * 92 + 1, ch * 8 + 8, 1, oyc.volumeL, nyc.volumeL, 0);
            DrawBuff.VolumeXY1(frameBuffer, 4 * 92 + 1, ch * 8 + 12, 1, oyc.volumeR, nyc.volumeR, 0);

            DrawBuff.KeyBoardXYFX(frameBuffer, 4 * 8 + 1, 4 * 103 + 1, ch * 8 + 8, oyc.note, nyc.note, 0);
            DrawBuff.ChK053260(frameBuffer, ch, oyc.mask, nyc.mask, 0);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        this.image = Resources.getPlaneK053260();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);

        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Resources.getFeli128());
        this.setName("frmK053260");
        this.setTitle("K053260");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;
}
