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
import mdplayer.chips.Ga20Chip;
import mdplayer.form.kb.frmChipBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.instrument.Ga20Inst;

public class frmGA20 extends frmChipBase<MDChipParams.GA20> {

    static final Preferences prefs = Preferences.userNodeForPackage(frmGA20.class);

    public frmGA20(frmMain frm, int chipId, int zoom, MDChipParams.GA20 newParam, MDChipParams.GA20 oldParam) {
        super(frm, chipId, zoom, newParam, new MDChipParams.GA20());

        initializeComponent();

        bind(Resources.getPlaneGA20());
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosGA20()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosGA20()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneGA20().getWidth() * zoom, frameSizeH + Resources.getPlaneGA20().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneGA20().getWidth() * zoom, frameSizeH + Resources.getPlaneGA20().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneGA20().getWidth() * zoom, frameSizeH + Resources.getPlaneGA20().getHeight() * zoom));
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
                    for (ch = 0; ch < 4; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(Ga20Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(Ga20Chip.class, chipId, ch);
                    }
                }
                return;
            }

            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch < 4) {
                if (ev.getButton() == MouseEvent.BUTTON1) {
                    parent.setChannelMask(Ga20Chip.class, chipId, ch);
                    return;
                }

                for (ch = 0; ch < 4; ch++) parent.resetChannelMask(Ga20Chip.class, chipId, ch);
            }
        }
    };

    public void screenInit() {
        int tp = 0;
        for (int ch = 0; ch < 32; ch++) {
            for (int ot = 0; ot < 12 * 8; ot++) {
                int kx = Tables.kbl[(ot % 12) * 2] + ot / 12 * 28;
                int kt = Tables.kbl[(ot % 12) * 2 + 1];
                DrawBuff.drawKbn(frameBuffer, 32 + kx, ch * 8 + 8, kt, tp);
            }
            DrawBuff.drawPanType2P(frameBuffer, 24, ch * 8 + 8, 0, tp);
            DrawBuff.ChC140_P(frameBuffer, 0, 8 + ch * 8, ch, false, tp);
        }
    }

    private int searchGA20Note(int freq, int clock) {
        int hz = clock / (256 - freq);

        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            int a = (int) (4000.0
                    * Tables.pcmMulTbl[i % 12 + 12]
                    * Math.pow(2, (i / 12 - 3 + 2)));

            if (hz > a) {
                n = i;
            }
        }
        return n;
    }

    public void screenChangeParams() {
        Map<String, Object> info = audio.plugin.chipRegister.chip(Ga20Chip.class).getInfo(chipId);
        if (info == null) return;

        int clock = clock(Ga20Inst.class) / 4;
        for (int ch = 0; ch < 4; ch++) {
            newParam.channels[ch].freq = (int) info.get("channels." + ch + ".freq");
            newParam.channels[ch].sadr = (int) info.get("channels." + ch + ".sadr");
            newParam.channels[ch].eadr = (int) info.get("channels." + ch + ".eadr");
            newParam.channels[ch].ladr = (int) info.get("channels." + ch + ".ladr");
            newParam.channels[ch].volume = (int) info.get("channels." + ch + ".volume");
            newParam.channels[ch].note = searchGA20Note(newParam.channels[ch].freq, clock);

            boolean play = (boolean) info.get("channels." + ch + ".play");
            if (play) {
                newParam.channels[ch].volumeL = Common.range((256 - newParam.channels[ch].volume) / 13, 0, 19);
            } else {
                if (newParam.channels[ch].volumeL > 0) newParam.channels[ch].volumeL--;
                else newParam.channels[ch].note = -1;
            }
        }
    }

    public void screenDrawParams() {
        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        for (int ch = 0; ch < 4; ch++) {
            oyc = oldParam.channels[ch];
            nyc = newParam.channels[ch];

            oyc.sadr = DrawBuff.font4Hex20Bit(frameBuffer, 4 * 65, ch * 8 + 8, 0, oyc.sadr, nyc.sadr);
            oyc.eadr = DrawBuff.font4Hex20Bit(frameBuffer, 4 * 71, ch * 8 + 8, 0, oyc.eadr, nyc.eadr);
            oyc.ladr = DrawBuff.font4Hex20Bit(frameBuffer, 4 * 77, ch * 8 + 8, 0, oyc.ladr, nyc.ladr);
            oyc.freq = DrawBuff.font4HexByte(frameBuffer, 4 * 83, ch * 8 + 8, 0, oyc.freq, nyc.freq);
            oyc.volume = DrawBuff.font4HexByte(frameBuffer, 4 * 86, ch * 8 + 8, 0, oyc.volume, nyc.volume);
            DrawBuff.KeyBoardToGA20(frameBuffer, ch, oyc.note, nyc.note, 0);
            DrawBuff.Volume(frameBuffer, 4 * 88, ch * 8 + 8, 0, oyc.volumeL, nyc.volumeL, 0);
            DrawBuff.ChC352(frameBuffer, ch, oyc.mask, nyc.mask, 0);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();

        this.image = Resources.getPlaneGA20();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);

        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Resources.getFeli128());
        this.setName("frmGA20");
        this.setTitle("GA20");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;
}
