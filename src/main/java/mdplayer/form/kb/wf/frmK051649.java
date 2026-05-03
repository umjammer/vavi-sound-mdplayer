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
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.chips.K051649Chip;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;
import mdsound.instrument.K051649Inst;

import static mdplayer.Common.searchSSGNote;


public class frmK051649 extends frmBase {

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmK051649));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneK051649();
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
        this.setTitle("K051649Inst");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    // //#endregion

    BufferedImage image;
    public JPanel pbScreen;
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private final int chipId;
    private final int zoom;

    private final MDChipParams.K051649 newParam;
    private final MDChipParams.K051649 oldParam = new MDChipParams.K051649();
    private final FrameBuffer frameBuffer = new FrameBuffer();

    static final Preferences prefs = Preferences.userNodeForPackage(frmK051649.class);

    public frmK051649(frmMain frm, int chipId, int zoom, MDChipParams.K051649 newParam) {
        super(frm);

        this.chipId = chipId;
        this.zoom = zoom;

        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneK051649(), null, zoom);
        DrawBuff.screenInitK051649(frameBuffer);
        update();
    }

    public void update() {
        frameBuffer.refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosK051649()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosK051649()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneK051649().getWidth() * zoom, frameSizeH + Resources.getPlaneK051649().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneK051649().getWidth() * zoom, frameSizeH + Resources.getPlaneK051649().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneK051649().getWidth() * zoom, frameSizeH + Resources.getPlaneK051649().getHeight() * zoom));
        componentListener.componentResized(null);
    }

    private final ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
        }
    };

    public void screenChangeParams() {
        Map<String, Object> chip = audio.plugin.chipRegister.chip(K051649Chip.class).getInfo(chipId);
        if (chip == null) return;

        for (int ch = 0; ch < 5; ch++) {
            if (chip.get("channels." + ch + ".freq") == null) continue;

            MDChipParams.Channel channel = newParam.channels[ch];
            for (int i = 0; i < 32; i++) channel.inst[i] = (int) chip.get("channels." + ch + ".inst." + i);
            float fTone = audio.plugin.mds.getChipInfo(K051649Inst.class).clock / (8.0f * (float) chip.get("channels." + ch + ".frequency"));
            channel.freq = (int) chip.get("channels." + ch + ".frequency");
            channel.volume = (int) chip.get("channels." + ch + ".volume");
            channel.volumeL = (int) chip.get("channels." + ch + ".volumeL");
            channel.note = ((int) chip.get("channels." + ch + ".key") != 0 && channel.volume != 0) ? searchSSGNote(fTone) : -1;
            channel.dda = (boolean) chip.get("channels." + ch + ".dda");
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
            oyc.volume = DrawBuff.volume(frameBuffer, 256, 8 + c * 8, 0, oyc.volume, nyc.volume, tp);
            DrawBuff.font4Hex12Bit(frameBuffer, x * 4 * 26 + 4 * 14, y * 8 * 6 + 8 * 11, tp, oyc.freq, nyc.freq);
            DrawBuff.font4Hex4Bit(frameBuffer, x * 4 * 26 + 4 * 22, y * 8 * 6 + 8 * 11, tp, oyc.volumeL, nyc.volumeL);
            DrawBuff.drawNESSw(frameBuffer, x * 4 * 26 + 4 * 25, y * 8 * 6 + 8 * 11, oyc.dda, nyc.dda);
            DrawBuff.WaveFormToK051649(frameBuffer, c, oyc.typ, nyc.inst);

            DrawBuff.ChK051649(frameBuffer, c, oyc.mask, nyc.mask, tp);

            for (int i = 0; i < 32; i++) {
                int fx = i % 8;
                int fy = i / 8;
                oyc.inst[i] = DrawBuff.font4HexByte(frameBuffer, x * 4 * 26 + 4 * 10 + fx * 8, y * 8 * 6 + 8 * 7 + fy * 8, 0, oyc.inst[i], nyc.inst[i]);
            }
        }
    }

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;

            // For top label row, do nothing
            if (py < 1 * 8) {
                // However, if you click on ch, the mask will be inverted.
                if (px < 8) {
                    for (int ch = 0; ch < 5; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(K051649Chip.class, chipId, ch);
                        else
                            parent.setChannelMask(K051649Chip.class, chipId, ch);
                    }
                }
                return;
            }

            // keyboard
            if (py < 6 * 8) {
                int ch = (py / 8) - 1;
                if (ch < 0) return;

                if (ev.getButton() == MouseEvent.BUTTON1) {
                    // Mask.
                    parent.setChannelMask(K051649Chip.class, chipId, ch);
                    return;
                }

                // Unmask.
                for (ch = 0; ch < 5; ch++) parent.resetChannelMask(K051649Chip.class, chipId, ch);
                return;
            }

            // Right-clicking on a tone does nothing
            if (ev.getButton() == MouseEvent.BUTTON2) return;

            // Judgment of tone display column
            int instCh = ((py < 12 * 8) ? 0 : 3) + px / (8 * 13);

            if (instCh < 5) {
                // Copying a tone to the clipboard
                parent.getInstCh(K051649Chip.class, instCh, chipId);
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
}
