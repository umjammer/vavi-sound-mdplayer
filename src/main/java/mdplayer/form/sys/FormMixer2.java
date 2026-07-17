package mdplayer.form.sys;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileFilter;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.form.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.PlayList;
import mdplayer.form.ScreenPanel;
import mdplayer.Setting;
import mdplayer.form.VisVolume;
import mdplayer.chips.*;
import mdplayer.chips.NpNesChip.DmcChip;
import mdplayer.chips.NpNesChip.FdsChip;
import mdplayer.chips.NpNesChip.Fme7Chip;
import mdplayer.chips.NpNesChip.Mmc5Chip;
import mdplayer.chips.NpNesChip.N163Chip;
import mdplayer.chips.NpNesChip.Vrc6Chip;
import mdplayer.chips.NpNesChip.Vrc7Chip;
import mdplayer.form.kb.wf.FormHuC6280;
import vavi.util.compat.Tuple;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


public class FormMixer2 extends JFrame {

    private static final Logger logger = getLogger(FormMixer2.class.getName());

    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public final FormMain parent;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private final int zoom;
    private int chipn = -1;

    private final MDChipParams newParam;
    private final MDChipParams oldParam = new MDChipParams();
    private final FrameBuffer frameBuffer = new FrameBuffer();

    static final Preferences prefs = Preferences.userNodeForPackage(FormHuC6280.class);
    final Audio audio = Audio.getInstance();

    public FormMixer2(FormMain frm, int zoom, MDChipParams newParam) {
        parent = frm;
        this.zoom = zoom;

        initializeComponent();
        pbScreen.addMouseWheelListener(this.pbScreen_MouseWheel);

        this.newParam = newParam;
        frameBuffer.add(pbScreen, Common.getImage("planeMixer"), null, zoom);
        screenInitMixer(frameBuffer);
        update();
        changeZoom();
    }

    private final MouseWheelListener pbScreen_MouseWheel = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            chipn = px / 20 + (py / 72) * 16;
            int delta = (int) Math.signum(ev.getWheelRotation());
            setVolume(chipn, false, delta);
        }
    };

    private void setVolume(int i, boolean isAbs, int delta) {
        if (i == 0) {
            audio.plugin.setMasterVolume(isAbs, delta);
        } else if (i == setVolume.length) {
            audio.plugin.chipRegister.plugin(RealChipPlugin.class).setGimicOPNVolume(false, delta);
        } else if (i == setVolume.length + 1) {
            audio.plugin.chipRegister.plugin(RealChipPlugin.class).setGimicOPNAVolume(false, delta);
        } else if (i > 0 && i < setVolume.length) {
            var t = setVolume[i];
            if (t != null) {
                audio.plugin.setVolume(t.getItem1(), t.getItem2(), isAbs, delta);
            }
        }
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
                parent.setting.getLocation().setPosMixer(getLocation());
            } else {
                parent.setting.getLocation().setPosMixer(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
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
        int w = Common.getImage("planeMixer").getWidth() * zoom;
        int h = Common.getImage("planeMixer").getHeight() * zoom;

        // size the skin panel and let pack() add the title-bar inset; the old code added
        // frameSizeW/H (the WinForms Width-minus-ClientSize chrome, always zero in Swing) and never
        // packed, so the window came up the wrong height and the skin was clipped
        pbScreen.setPreferredSize(new Dimension(w, h));
        setResizable(false);
        componentListener.componentResized(null);
        setPreferredSize(null); // clear any explicit frame size so pack() derives it from the content
        pack();
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

    /** the meters: master is fed by frmMain from the rendered wave, the per-chip ones by nothing yet */
    public final VisVolume visVolume = new VisVolume();

    private void updateVisualVolumes() {
        if (audio.plugin == null || audio.plugin.chipRegister == null) {
            return;
        }

        Function<Object, Integer> getVol = (volObj) -> {
            if (volObj == null) return 0;
            if (volObj instanceof int[]) {
                int max = 0;
                for (int v : (int[]) volObj) if (v > max) max = v;
                return max;
            } else if (volObj instanceof int[][]) {
                int max = 0;
                for (int[] row : (int[][]) volObj) {
                    if (row != null) {
                        for (int v : row) if (v > max) max = v;
                    }
                }
                return max;
            }
            return 0;
        };

        BiFunction<Class<? extends Chip>, String, Object> chipInfo = (chipClass, key) -> {
            try {
                mdplayer.Chip chip = audio.plugin.chipRegister.chip(chipClass);
                if (!(chip instanceof BaseChip)) return null;
                Map<String, Object> info = ((BaseChip) chip).getInfo(0);
                if (info == null) return null;
                return info.get(key);
            } catch (Exception e) {
                return null;
            }
        };

        Function<Class<? extends mdplayer.Chip>, Integer> getChipVol = (chipClass) -> {
            try {
                mdplayer.Chip chip = audio.plugin.chipRegister.chip(chipClass);
                if (!(chip instanceof BaseChip)) return 0;
                Map<String, Object> info = ((BaseChip) chip).getInfo(0);
                if (info == null) return 0;
                Object volObj = info.get("volume");
                return getVol.apply(volObj);
            } catch (Exception e) {
                return 0;
            }
        };

        visVolume.put("ym2151", getChipVol.apply(Ym2151Chip.class) * 5);

        int ym2203FMVal = getChipVol.apply(Ym2203Chip.class) * 5;
        int ym2203SSGVal = 0;
        try {
            int[] ym2203Reg = (int[]) chipInfo.apply(Ym2203Chip.class, "register");
            if (ym2203Reg != null) {
                int mixer = ym2203Reg[0x07];
                for (int ch = 0; ch < 3; ch++) {
                    boolean toneOn = (mixer & (0x01 << ch)) == 0;
                    boolean noiseOn = (mixer & (0x08 << ch)) == 0;
                    if (toneOn || noiseOn) {
                        int v = (ym2203Reg[0x08 + ch] & 0xf) * 600;
                        if (v > ym2203SSGVal) ym2203SSGVal = v;
                    }
                }
            }
        } catch (Exception e) {
        }
        visVolume.put("ym2203FM", ym2203FMVal);
        visVolume.put("ym2203SSG", ym2203SSGVal);
        visVolume.put("ym2203", Math.max(ym2203FMVal, ym2203SSGVal));

        visVolume.put("ym2612", getChipVol.apply(Ym2612Chip.class) * 5);

        int ym2608FMVal = getChipVol.apply(Ym2608Chip.class) * 5;
        int ym2608SSGVal = 0;
        try {
            int[][] ym2608Reg2D = (int[][]) chipInfo.apply(Ym2608Chip.class, "register");
            if (ym2608Reg2D != null && ym2608Reg2D.length > 0) {
                int mixer = ym2608Reg2D[0][0x07];
                for (int ch = 0; ch < 3; ch++) {
                    boolean toneOn = (mixer & (0x01 << ch)) == 0;
                    boolean noiseOn = (mixer & (0x08 << ch)) == 0;
                    if (toneOn || noiseOn) {
                        int v = (ym2608Reg2D[0][0x08 + ch] & 0xf) * 600;
                        if (v > ym2608SSGVal) ym2608SSGVal = v;
                    }
                }
            }
        } catch (Exception e) {
        }
        int ym2608APCMVal = 0;
        try {
            Object ym2608APCMVol = chipInfo.apply(Ym2608Chip.class, "adpcmVolume");
            ym2608APCMVal = getVol.apply(ym2608APCMVol) * 5;
        } catch (Exception e) {
        }
        int ym2608RtmVal = 0;
        try {
            Object ym2608RtmVol = chipInfo.apply(Ym2608Chip.class, "rythmVolume");
            ym2608RtmVal = getVol.apply(ym2608RtmVol) * 5;
        } catch (Exception e) {
        }
        visVolume.put("ym2608FM", ym2608FMVal);
        visVolume.put("ym2608SSG", ym2608SSGVal);
        visVolume.put("ym2608APCM", ym2608APCMVal);
        visVolume.put("ym2608Rtm", ym2608RtmVal);
        visVolume.put("ym2608", Math.max(Math.max(ym2608FMVal, ym2608SSGVal), Math.max(ym2608APCMVal, ym2608RtmVal)));

        int ym2610FMVal = getChipVol.apply(Ym2610Chip.class) * 5;
        int ym2610SSGVal = 0;
        try {
            int[][] ym2610Reg2D = (int[][]) chipInfo.apply(Ym2610Chip.class, "register");
            if (ym2610Reg2D != null && ym2610Reg2D.length > 0) {
                int mixer = ym2610Reg2D[0][0x07];
                for (int ch = 0; ch < 3; ch++) {
                    boolean toneOn = (mixer & (0x01 << ch)) == 0;
                    boolean noiseOn = (mixer & (0x08 << ch)) == 0;
                    if (toneOn || noiseOn) {
                        int v = (ym2610Reg2D[0][0x08 + ch] & 0xf) * 600;
                        if (v > ym2610SSGVal) ym2610SSGVal = v;
                    }
                }
            }
        } catch (Exception e) {
        }
        int ym2610APCMAVal = 0;
        try {
            Object ym2610APCMAVol = chipInfo.apply(Ym2610Chip.class, "adpcmAVolume");
            ym2610APCMAVal = getVol.apply(ym2610APCMAVol) * 5;
        } catch (Exception e) {
        }
        int ym2610APCMBVal = 0;
        try {
            Object ym2610APCMBVol = chipInfo.apply(Ym2610Chip.class, "adpcmBVolume");
            ym2610APCMBVal = getVol.apply(ym2610APCMBVol) * 5;
        } catch (Exception e) {
        }
        visVolume.put("ym2610FM", ym2610FMVal);
        visVolume.put("ym2610SSG", ym2610SSGVal);
        visVolume.put("ym2610APCMA", ym2610APCMAVal);
        visVolume.put("ym2610APCMB", ym2610APCMBVal);
        visVolume.put("ym2610", Math.max(Math.max(ym2610FMVal, ym2610SSGVal), Math.max(ym2610APCMAVal, ym2610APCMBVal)));

        visVolume.put("ym2413", getChipVol.apply(Ym2413Chip.class) * 5);
        visVolume.put("ym3526", getChipVol.apply(Ym3526Chip.class) * 5);
        visVolume.put("y8950", getChipVol.apply(Y8950Chip.class) * 5);
        visVolume.put("ym3812", getChipVol.apply(Ym3812Chip.class) * 5);
        visVolume.put("ymf262", getChipVol.apply(YmF262Chip.class) * 5);
        visVolume.put("ymf278b", getChipVol.apply(YmF278BChip.class) * 5);
        visVolume.put("ymz280b", getChipVol.apply(YmZ280BChip.class) * 5);
        visVolume.put("ymf271", getChipVol.apply(YmF271Chip.class) * 5);
        visVolume.put("ay8910", getChipVol.apply(Ay8910Chip.class) * 5);
        visVolume.put("sn76489", getChipVol.apply(Sn76489Chip.class) * 5);
        visVolume.put("huc6280", getChipVol.apply(HuC6280Chip.class) * 5);
        visVolume.put("rf5c164", getChipVol.apply(Rf5C164Chip.class) * 5);
        visVolume.put("rf5c68", getChipVol.apply(Rf5C68Chip.class) * 5);
        visVolume.put("pwm", getChipVol.apply(PwmChip.class) * 5);
        visVolume.put("okim6258", getChipVol.apply(OkiM6258Chip.class) * 5);
        visVolume.put("okim6295", getChipVol.apply(OkiM6295Chip.class) * 5);
        visVolume.put("c140", getChipVol.apply(C140Chip.class) * 5);
        visVolume.put("c352", getChipVol.apply(C352Chip.class) * 5);
        visVolume.put("saa1099", getChipVol.apply(Saa1099Chip.class) * 5);
        visVolume.put("ppz8", getChipVol.apply(Ppz8Chip.class) * 5);
        int segaPCMVal = 0;
        try {
            byte[] segapcmReg = (byte[]) chipInfo.apply(SegaPcmChip.class, "register");
            if (segapcmReg != null) {
                for (int ch = 0; ch < 16; ch++) {
                    int v = 0;
                    if ((segapcmReg[0x86 + ch * 8] & 1) == 0) {
                        int l = segapcmReg[ch * 8 + 2] & 0x7f;
                        int r = segapcmReg[ch * 8 + 3] & 0x7f;
                        v = Math.max(l, r) * 70;
                    }
                    if (v > segaPCMVal) segaPCMVal = v;
                }
            }
        } catch (Exception e) {
        }
        visVolume.put("segaPCM", segaPCMVal * 3);

        int multiPCMVal = 0;
        try {
            mdplayer.Chip chip = audio.plugin.chipRegister.chip(MultiPcmChip.class);
            if (chip instanceof BaseChip) {
                java.util.Map<String, Object> info = ((BaseChip) chip).getInfo(0);
                if (info != null) {
                    for (int ch = 0; ch < 28; ch++) {
                        Boolean bit = (Boolean) info.get("channels." + ch + ".bit");
                        if (bit != null && bit) {
                            Integer inst1 = (Integer) info.get("channels." + ch + ".inst.1");
                            Integer pan = (Integer) info.get("channels." + ch + ".pan");
                            if (inst1 != null && pan != null) {
                                int panL = (pan >> 4) & 0xf;
                                int panR = pan & 0xf;
                                int l = (0x7f - inst1) * panL / 0xf;
                                int r = (0x7f - inst1) * panR / 0xf;
                                int v = Math.max(l, r) * 70;
                                if (v > multiPCMVal) multiPCMVal = v;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        visVolume.put("multiPCM", multiPCMVal);
        visVolume.put("k051649", getChipVol.apply(K051649Chip.class) * 5);
        visVolume.put("k053260", getChipVol.apply(K053260Chip.class) * 5);
        visVolume.put("k054539", getChipVol.apply(K054539Chip.class) * 5);
        visVolume.put("qSound", getChipVol.apply(QSoundChip.class) * 5);
        visVolume.put("ga20", getChipVol.apply(Ga20Chip.class) * 5);

        NpNesChip npNesChip = null;
        try {
            mdplayer.Chip chip = audio.plugin.chipRegister.chip(NpNesChip.class);
            if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(DmcChip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(FdsChip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(N163Chip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(Vrc6Chip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(Mmc5Chip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(Fme7Chip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
            if (npNesChip == null) {
                chip = audio.plugin.chipRegister.chip(Vrc7Chip.class);
                if (chip instanceof NpNesChip) npNesChip = (NpNesChip) chip;
            }
        } catch (Exception e) {
        }

        if (npNesChip != null) {
            try {
                visVolume.put("APU", npNesChip.getVolume(0) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("DMC", npNesChip.getVolume(1) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("FDS", npNesChip.getVolume(2) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("N160", npNesChip.getVolume(3) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("VRC6", npNesChip.getVolume(4) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("MMC5", npNesChip.getVolume(5) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("FME7", npNesChip.getVolume(6) * 15);
            } catch (Exception e) {
            }
            try {
                visVolume.put("VRC7", npNesChip.getVolume(7) * 15);
            } catch (Exception e) {
            }
        }

        visVolume.put("DMG", getChipVol.apply(DmgChip.class) * 5);
    }

    public void screenChangeParams() {
        updateVisualVolumes();

        newParam.MasterVolume.Volume = parent.setting.getBalance().getMasterVolume();
        audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym2203Chip.class);
        audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.Volume = parent.setting.getBalance().getVolume("FM", Ym2203Chip.class);
        audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.Volume = parent.setting.getBalance().getVolume("PSG", Ym2203Chip.class);
        audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.Volume = parent.setting.getBalance().getVolume("FM", Ym2608Chip.class);
        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.Volume = parent.setting.getBalance().getVolume("PSG", Ym2608Chip.class);
        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.Volume = parent.setting.getBalance().getVolume("Rhythm", Ym2608Chip.class);
        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.Volume = parent.setting.getBalance().getVolume("Adpcm", Ym2608Chip.class);
        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym2610Chip.class);
        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.Volume = parent.setting.getBalance().getVolume("FM", Ym2610Chip.class);
        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.Volume = parent.setting.getBalance().getVolume("PSG", Ym2610Chip.class);
        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.Volume = parent.setting.getBalance().getVolume("AdpcmA", Ym2610Chip.class);
        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.Volume = parent.setting.getBalance().getVolume("AdpcmB", Ym2610Chip.class);

        audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
        audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym3526Chip.class);
        audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Y8950Chip.class);
        audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ym3812Chip.class);
        audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, YmF262Chip.class);
        audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, YmF278BChip.class);
        audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, YmZ280BChip.class);
        audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, YmF271Chip.class);
        audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
        audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class);
        audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, HuC6280Chip.class);

        audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Rf5C164Chip.class);
        audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Rf5C68Chip.class);
        audio.plugin.chipRegister.chip(PwmChip.class).PWM.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, PwmChip.class);
        audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, OkiM6258Chip.class);
        audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, OkiM6295Chip.class);
        audio.plugin.chipRegister.chip(C140Chip.class).C140_.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, C140Chip.class);
        audio.plugin.chipRegister.chip(C352Chip.class).C352.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, C352Chip.class);
        audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Saa1099Chip.class);
        audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ppz8Chip.class);
        audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, SegaPcmChip.class);
        audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, MultiPcmChip.class);
        audio.plugin.chipRegister.chip(K051649Chip.class).K051649.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
        audio.plugin.chipRegister.chip(K053260Chip.class).K053260.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, K053260Chip.class);
        audio.plugin.chipRegister.chip(K054539Chip.class).K054539.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, K054539Chip.class);
        audio.plugin.chipRegister.chip(QSoundChip.class).QSound.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, QSoundChip.class);
        audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Ga20Chip.class);

        audio.plugin.chipRegister.chip(NpNesChip.class).APU.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, NesChip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).DMC.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, DmcChip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).FDS.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, FdsChip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).MMC5.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Mmc5Chip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).N160.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, N163Chip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Vrc6Chip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Vrc7Chip.class);
        audio.plugin.chipRegister.chip(NpNesChip.class).FME7.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, Fme7Chip.class);
        audio.plugin.chipRegister.chip(DmgChip.class).DMG.Volume = parent.setting.getBalance().getVolume(MAIN_TAG, DmgChip.class);

        newParam.GimicOPN.Volume = parent.setting.getBalance().getGimicOPNVolume();
        newParam.GimicOPNA.Volume = parent.setting.getBalance().getGimicOPNAVolume();


        newParam.MasterVolume.VisVolume1 = Common.range(visVolume.get("master") / 250, 0, 44);
        if (newParam.MasterVolume.VisVolume2 <= newParam.MasterVolume.VisVolume1) {
            newParam.MasterVolume.VisVolume2 = newParam.MasterVolume.VisVolume1;
            newParam.MasterVolume.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.VisVolume1 = Common.range(visVolume.get("ym2151") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.VisVolume2 = audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.VisVolume1 = Common.range(visVolume.get("ym2203") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.VisVolume2 = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.VisVolume1 = Common.range(visVolume.get("ym2203FM") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.VisVolume2 = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.VisVolume1 = Common.range(visVolume.get("ym2203SSG") / 120, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.VisVolume2 = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.VisVolume1 = Common.range(visVolume.get("ym2612") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.VisVolume2 = audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.VisVolume1 = Common.range(visVolume.get("ym2608") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.VisVolume2 = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.VisVolume1 = Common.range(visVolume.get("ym2608FM") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.VisVolume2 = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.VisVolume1 = Common.range(visVolume.get("ym2608SSG") / 120, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.VisVolume2 = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.VisVolume1 = Common.range(visVolume.get("ym2608Rtm") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.VisVolume2 = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.VisVolume1 = Common.range(visVolume.get("ym2608APCM") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.VisVolume2 = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.VisVolume1 = Common.range(visVolume.get("ym2610") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.VisVolume2 = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.VisVolume1 = Common.range(visVolume.get("ym2610FM") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.VisVolume2 = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.VisVolume1 = Common.range(visVolume.get("ym2610SSG") / 120, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.VisVolume2 = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.VisVolume1 = Common.range(visVolume.get("ym2610APCMA") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.VisVolume2 = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.VisVolume1 = Common.range(visVolume.get("ym2610APCMB") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.VisVolume2 = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB.VisVol2Cnt = 30;
        }


        audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.VisVolume1 = Common.range(visVolume.get("ym2413") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.VisVolume2 <= audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.VisVolume2 = audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.VisVolume1;
            audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.VisVolume1 = Common.range(visVolume.get("ym3526") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.VisVolume2 <= audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.VisVolume2 = audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.VisVolume1;
            audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.VisVolume1 = Common.range(visVolume.get("y8950") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.VisVolume2 <= audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.VisVolume1) {
            audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.VisVolume2 = audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.VisVolume1;
            audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.VisVolume1 = Common.range(visVolume.get("ym3812") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.VisVolume2 <= audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.VisVolume1) {
            audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.VisVolume2 = audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.VisVolume1;
            audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.VisVolume1 = Common.range(visVolume.get("ymf262") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.VisVolume2 <= audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.VisVolume1) {
            audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.VisVolume2 = audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.VisVolume1;
            audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.VisVolume1 = Common.range(visVolume.get("ymf278b") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.VisVolume2 <= audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.VisVolume1) {
            audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.VisVolume2 = audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.VisVolume1;
            audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.VisVolume1 = Common.range(visVolume.get("ymz280b") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.VisVolume2 <= audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.VisVolume1) {
            audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.VisVolume2 = audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.VisVolume1;
            audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.VisVolume1 = Common.range(visVolume.get("ymf271") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.VisVolume2 <= audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.VisVolume1) {
            audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.VisVolume2 = audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.VisVolume1;
            audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.VisVolume1 = Common.range(visVolume.get("ay8910") / 120, 0, 44);
        if (audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.VisVolume2 <= audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.VisVolume1) {
            audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.VisVolume2 = audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.VisVolume1;
            audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.VisVolume1 = Common.range(visVolume.get("sn76489") / 120, 0, 44);
        if (audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.VisVolume2 <= audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.VisVolume1) {
            audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.VisVolume2 = audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.VisVolume1;
            audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.VisVolume1 = Common.range(visVolume.get("huc6280") / 120, 0, 44);
        if (audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.VisVolume2 <= audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.VisVolume1) {
            audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.VisVolume2 = audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.VisVolume1;
            audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280.VisVol2Cnt = 30;
        }


        audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.VisVolume1 = Common.range(visVolume.get("rf5c164") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.VisVolume2 <= audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.VisVolume1) {
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.VisVolume2 = audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.VisVolume1;
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.VisVolume1 = Common.range(visVolume.get("rf5c68") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.VisVolume2 <= audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.VisVolume1) {
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.VisVolume2 = audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.VisVolume1;
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(PwmChip.class).PWM.VisVolume1 = Common.range(visVolume.get("pwm") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(PwmChip.class).PWM.VisVolume2 <= audio.plugin.chipRegister.chip(PwmChip.class).PWM.VisVolume1) {
            audio.plugin.chipRegister.chip(PwmChip.class).PWM.VisVolume2 = audio.plugin.chipRegister.chip(PwmChip.class).PWM.VisVolume1;
            audio.plugin.chipRegister.chip(PwmChip.class).PWM.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.VisVolume1 = Common.range(visVolume.get("okim6258") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.VisVolume2 <= audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.VisVolume1) {
            audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.VisVolume2 = audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.VisVolume1;
            audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.VisVolume1 = Common.range(visVolume.get("okim6295") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.VisVolume2 <= audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.VisVolume1) {
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.VisVolume2 = audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.VisVolume1;
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(C140Chip.class).C140_.VisVolume1 = Common.range(visVolume.get("c140") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(C140Chip.class).C140_.VisVolume2 <= audio.plugin.chipRegister.chip(C140Chip.class).C140_.VisVolume1) {
            audio.plugin.chipRegister.chip(C140Chip.class).C140_.VisVolume2 = audio.plugin.chipRegister.chip(C140Chip.class).C140_.VisVolume1;
            audio.plugin.chipRegister.chip(C140Chip.class).C140_.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(C352Chip.class).C352.VisVolume1 = Common.range(visVolume.get("c352") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(C352Chip.class).C352.VisVolume2 <= audio.plugin.chipRegister.chip(C352Chip.class).C352.VisVolume1) {
            audio.plugin.chipRegister.chip(C352Chip.class).C352.VisVolume2 = audio.plugin.chipRegister.chip(C352Chip.class).C352.VisVolume1;
            audio.plugin.chipRegister.chip(C352Chip.class).C352.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.VisVolume1 = Common.range(visVolume.get("saa1099") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.VisVolume2 <= audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.VisVolume1) {
            audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.VisVolume2 = audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.VisVolume1;
            audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.VisVolume1 = Common.range(visVolume.get("ppz8") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.VisVolume2 <= audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.VisVolume1) {
            audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.VisVolume2 = audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.VisVolume1;
            audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.VisVolume1 = Common.range(visVolume.get("segaPCM") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.VisVolume2 <= audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.VisVolume1) {
            audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.VisVolume2 = audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.VisVolume1;
            audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.VisVolume1 = Common.range(visVolume.get("multiPCM") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.VisVolume2 <= audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.VisVolume1) {
            audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.VisVolume2 = audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.VisVolume1;
            audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(K051649Chip.class).K051649.VisVolume1 = Common.range(visVolume.get("k051649") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(K051649Chip.class).K051649.VisVolume2 <= audio.plugin.chipRegister.chip(K051649Chip.class).K051649.VisVolume1) {
            audio.plugin.chipRegister.chip(K051649Chip.class).K051649.VisVolume2 = audio.plugin.chipRegister.chip(K051649Chip.class).K051649.VisVolume1;
            audio.plugin.chipRegister.chip(K051649Chip.class).K051649.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(K053260Chip.class).K053260.VisVolume1 = Common.range(visVolume.get("k053260") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(K053260Chip.class).K053260.VisVolume2 <= audio.plugin.chipRegister.chip(K053260Chip.class).K053260.VisVolume1) {
            audio.plugin.chipRegister.chip(K053260Chip.class).K053260.VisVolume2 = audio.plugin.chipRegister.chip(K053260Chip.class).K053260.VisVolume1;
            audio.plugin.chipRegister.chip(K053260Chip.class).K053260.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVolume1 = Common.range(visVolume.get("k054539") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVolume2 <= audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVolume1) {
            audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVolume2 = audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVolume1;
            audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(QSoundChip.class).QSound.VisVolume1 = Common.range(visVolume.get("qSound") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(QSoundChip.class).QSound.VisVolume2 <= audio.plugin.chipRegister.chip(QSoundChip.class).QSound.VisVolume1) {
            audio.plugin.chipRegister.chip(QSoundChip.class).QSound.VisVolume2 = audio.plugin.chipRegister.chip(QSoundChip.class).QSound.VisVolume1;
            audio.plugin.chipRegister.chip(QSoundChip.class).QSound.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.VisVolume1 = Common.range(visVolume.get("ga20") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.VisVolume2 <= audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.VisVolume1) {
            audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.VisVolume2 = audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.VisVolume1;
            audio.plugin.chipRegister.chip(Ga20Chip.class).GA20.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).APU.VisVolume1 = Common.range(visVolume.get("APU") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).APU.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).APU.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).APU.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).APU.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).APU.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).DMC.VisVolume1 = Common.range(visVolume.get("DMC") / 350, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).DMC.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).DMC.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).DMC.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).DMC.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).DMC.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).FDS.VisVolume1 = Common.range(visVolume.get("FDS") / 200, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).FDS.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).FDS.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).FDS.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).FDS.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).FDS.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).MMC5.VisVolume1 = Common.range(visVolume.get("MMC5") / 50, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).MMC5.VisVolume2 <= audio.plugin.chipRegister.chip(K054539Chip.class).K054539.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).MMC5.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).MMC5.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).MMC5.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).N160.VisVolume1 = Common.range(visVolume.get("N160") / 50, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).N160.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).N160.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).N160.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).N160.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).N160.VisVol2Cnt = 30;
        }
        audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.VisVolume1 = Common.range(visVolume.get("Vrc6Inst") / 50, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).VRC6.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.VisVolume1 = Common.range(visVolume.get("VRC7") / 50, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).VRC7.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(NpNesChip.class).FME7.VisVolume1 = Common.range(visVolume.get("FME7") / 50, 0, 44);
        if (audio.plugin.chipRegister.chip(NpNesChip.class).FME7.VisVolume2 <= audio.plugin.chipRegister.chip(NpNesChip.class).FME7.VisVolume1) {
            audio.plugin.chipRegister.chip(NpNesChip.class).FME7.VisVolume2 = audio.plugin.chipRegister.chip(NpNesChip.class).FME7.VisVolume1;
            audio.plugin.chipRegister.chip(NpNesChip.class).FME7.VisVol2Cnt = 30;
        }

        audio.plugin.chipRegister.chip(DmgChip.class).DMG.VisVolume1 = Common.range(visVolume.get("DMG") / 50, 0, 44);
        if (audio.plugin.chipRegister.chip(DmgChip.class).DMG.VisVolume2 <= audio.plugin.chipRegister.chip(DmgChip.class).DMG.VisVolume1) {
            audio.plugin.chipRegister.chip(DmgChip.class).DMG.VisVolume2 = audio.plugin.chipRegister.chip(DmgChip.class).DMG.VisVolume1;
            audio.plugin.chipRegister.chip(DmgChip.class).DMG.VisVol2Cnt = 30;
        }
    }

    public void screenDrawParams() {
        int num;
        MDChipParams.VolumeInfo oVI, nVI;

        num = 0;
        oVI = oldParam.MasterVolume;
        nVI = newParam.MasterVolume;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151_old;
        nVI = audio.plugin.chipRegister.chip(Ym2151Chip.class).YM2151;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203_old;
        nVI = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM_old;
        nVI = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203FM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG_old;
        nVI = audio.plugin.chipRegister.chip(Ym2203Chip.class).YM2203PSG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612_old;
        nVI = audio.plugin.chipRegister.chip(Ym2612Chip.class).YM2612;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608_old;
        nVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM_old;
        nVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608FM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG_old;
        nVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608PSG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm_old;
        nVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Rhythm;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm_old;
        nVI = audio.plugin.chipRegister.chip(Ym2608Chip.class).YM2608Adpcm;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610_old;
        nVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM_old;
        nVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610FM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG_old;
        nVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610PSG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA_old;
        nVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmA;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB_old;
        nVI = audio.plugin.chipRegister.chip(Ym2610Chip.class).YM2610AdpcmB;
        drawVolAndFader(num, oVI, nVI);

        num++;
        oVI = audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413_old;
        nVI = audio.plugin.chipRegister.chip(Ym2413Chip.class).YM2413;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526_old;
        nVI = audio.plugin.chipRegister.chip(Ym3526Chip.class).YM3526;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950_old;
        nVI = audio.plugin.chipRegister.chip(Y8950Chip.class).Y8950;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812_old;
        nVI = audio.plugin.chipRegister.chip(Ym3812Chip.class).YM3812;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262_old;
        nVI = audio.plugin.chipRegister.chip(YmF262Chip.class).YMF262;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B_old;
        nVI = audio.plugin.chipRegister.chip(YmF278BChip.class).YMF278B;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B_old;
        nVI = audio.plugin.chipRegister.chip(YmZ280BChip.class).YMZ280B;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271_old;
        nVI = audio.plugin.chipRegister.chip(YmF271Chip.class).YMF271;
        drawVolAndFader(num, oVI, nVI);
        num++;
        num++;
        oVI = audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910_old;
        nVI = audio.plugin.chipRegister.chip(Ay8910Chip.class).AY8910;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489_old;
        nVI = audio.plugin.chipRegister.chip(Sn76489Chip.class).SN76489;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280_old;
        nVI = audio.plugin.chipRegister.chip(HuC6280Chip.class).HuC6280;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099_old;
        nVI = audio.plugin.chipRegister.chip(Saa1099Chip.class).SAA1099;
        drawVolAndFader(num, oVI, nVI);
        num++;
        num++;
        num++;

        num++;
        num++;
        num++;
        oVI = audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164_old;
        nVI = audio.plugin.chipRegister.chip(Rf5C164Chip.class).RF5C164;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68_old;
        nVI = audio.plugin.chipRegister.chip(Rf5C68Chip.class).RF5C68;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(PwmChip.class).PWM_old;
        nVI = audio.plugin.chipRegister.chip(PwmChip.class).PWM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258_old;
        nVI = audio.plugin.chipRegister.chip(OkiM6258Chip.class).OKIM6258;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295_old;
        nVI = audio.plugin.chipRegister.chip(OkiM6295Chip.class).OKIM6295;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(C140Chip.class).C140_old;
        nVI = audio.plugin.chipRegister.chip(C140Chip.class).C140_;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(C352Chip.class).C352_old;
        nVI = audio.plugin.chipRegister.chip(C352Chip.class).C352;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM_old;
        nVI = audio.plugin.chipRegister.chip(SegaPcmChip.class).SEGAPCM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM_old;
        nVI = audio.plugin.chipRegister.chip(MultiPcmChip.class).MultiPCM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(K051649Chip.class).K051649_old;
        nVI = audio.plugin.chipRegister.chip(K051649Chip.class).K051649;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(K053260Chip.class).K053260_old;
        nVI = audio.plugin.chipRegister.chip(K053260Chip.class).K053260;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(K054539Chip.class).K054539_old;
        nVI = audio.plugin.chipRegister.chip(K054539Chip.class).K054539;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(QSoundChip.class).QSound_old;
        nVI = audio.plugin.chipRegister.chip(QSoundChip.class).QSound;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(Ga20Chip.class).GA20_old;
        nVI = audio.plugin.chipRegister.chip(Ga20Chip.class).GA20;
        drawVolAndFader(num, oVI, nVI);

        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).APU_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).APU;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).DMC_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).DMC;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).FDS_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).FDS;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).MMC5_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).MMC5;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).N160_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).N160;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).VRC6_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).VRC6;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).VRC7_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).VRC7;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(NpNesChip.class).FME7_old;
        nVI = audio.plugin.chipRegister.chip(NpNesChip.class).FME7;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = audio.plugin.chipRegister.chip(DmgChip.class).DMG_old;
        nVI = audio.plugin.chipRegister.chip(DmgChip.class).DMG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        num++;
        num++;
        num++;
        num++;
        oVI = audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8_old;
        nVI = audio.plugin.chipRegister.chip(Ppz8Chip.class).PPZ8;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.GimicOPN;
        nVI = newParam.GimicOPN;
        drawGVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.GimicOPNA;
        nVI = newParam.GimicOPNA;
        drawGVolAndFader(num, oVI, nVI);
    }

    private void drawVolAndFader(int num, MDChipParams.VolumeInfo oVI, MDChipParams.VolumeInfo nVI) {
        drawFader(
                frameBuffer,
                5 + (num % 16) * 20,
                16 + (num / 16) * 8 * 9,
                num == 0 ? 0 : 1,
                oVI.Volume,
                nVI.Volume);
        nVI.VisVol2Cnt--;
        if (nVI.VisVol2Cnt == 0) {
            nVI.VisVol2Cnt = 1;
            if (nVI.VisVolume2 > 0) nVI.VisVolume2--;
        }
        MixerVolume(
                frameBuffer,
                2 + (num % 16) * 20,
                10 + (num / 16) * 8 * 9,
                oVI.VisVolume1,
                nVI.VisVolume1,
                oVI.VisVolume2,
                nVI.VisVolume2);
        oVI.Volume = nVI.Volume;
        oVI.VisVolume1 = nVI.VisVolume1;
        oVI.VisVolume2 = nVI.VisVolume2;
    }

    private void drawGVolAndFader(int num, MDChipParams.VolumeInfo oVI, MDChipParams.VolumeInfo nVI) {
        drawGFader(
                frameBuffer,
                5 + (num % 16) * 20,
                16 + (num / 16) * 8 * 9,
                num == 0 ? 0 : 1,
                oVI.Volume,
                nVI.Volume);
        nVI.VisVol2Cnt--;
        if (nVI.VisVol2Cnt == 0) {
            nVI.VisVol2Cnt = 1;
            if (nVI.VisVolume2 > 0) nVI.VisVolume2--;
        }
        MixerVolume(
                frameBuffer,
                2 + (num % 16) * 20,
                10 + (num / 16) * 8 * 9,
                oVI.VisVolume1,
                nVI.VisVolume1,
                oVI.VisVolume2,
                nVI.VisVolume2);
        oVI.Volume = nVI.Volume;
        oVI.VisVolume1 = nVI.VisVolume1;
        oVI.VisVolume2 = nVI.VisVolume2;
    }

    public void screenInit() {
        visVolume.put("master", -1);
        visVolume.put("ym2151", -1);
        visVolume.put("ym2203", -1);
        visVolume.put("ym2203FM", -1);
        visVolume.put("ym2203SSG", -1);
        visVolume.put("ym2612", -1);
        visVolume.put("ym2608", -1);
        visVolume.put("ym2608APCM", -1);
        visVolume.put("ym2608FM", -1);
        visVolume.put("ym2608Rtm", -1);
        visVolume.put("ym2608SSG", -1);
        visVolume.put("ym2610", -1);
        visVolume.put("ym2610APCMA", -1);
        visVolume.put("ym2610APCMB", -1);
        visVolume.put("ym2610FM", -1);
        visVolume.put("ym2610SSG", -1);

        visVolume.put("ym2413", -1);
        visVolume.put("ym3526", -1);
        visVolume.put("y8950", -1);
        visVolume.put("ym3812", -1);
        visVolume.put("ymf262", -1);
        visVolume.put("ymf278b", -1);
        visVolume.put("ymz280b", -1);
        visVolume.put("ymf271", -1);
        visVolume.put("ay8910", -1);
        visVolume.put("sn76489", -1);
        visVolume.put("huc6280", -1);

        visVolume.put("rf5c164", -1);
        visVolume.put("rf5c68", -1);
        visVolume.put("pwm", -1);
        visVolume.put("okim6258", -1);
        visVolume.put("okim6295", -1);
        visVolume.put("c140", -1);
        visVolume.put("c352", -1);
        visVolume.put("saa1099", -1);
        visVolume.put("ppz8", -1);
        visVolume.put("segaPCM", -1);
        visVolume.put("multiPCM", -1);
        visVolume.put("k051649", -1);
        visVolume.put("k053260", -1);
        visVolume.put("k054539", -1);
        visVolume.put("qSound", -1);
        visVolume.put("ga20", -1);

        visVolume.put("APU", 0);
        visVolume.put("DMC", 0);
        visVolume.put("FDS", 0);
        visVolume.put("MMC5", 0);
        visVolume.put("N160", 0);
        visVolume.put("Vrc6Inst", 0);
        visVolume.put("VRC7", 0);
        visVolume.put("FME7", 0);
        visVolume.put("DMG", -1);
    }

    private final KeyListener frmMixer2_KeyDown = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
        }
    };

    private final MouseListener frmMixer2_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();

            chipn = px / 20 + (py / 72) * 16;
        }
    };

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            chipn = px / 20 + (py / 72) * 16;
            if (ev.getButton() == MouseEvent.BUTTON3) {
                setVolume(chipn, true, 0);
            }
        }

        @Override
        public void mouseEntered(MouseEvent ev) {
            pbScreen.requestFocus();
        }
    };

    private final MouseMotionListener pbScreen_MouseDrag = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            chipn = px / 20 + (py / 72) * 16;
            py = py % 72;

            int n;
            if (chipn < 62) {
                if (py < 18) {
                    n = Math.min((18 - py), 8);
                    n = (int) (n * 2.5);
                } else if (py == 18) {
                    n = 0;
                } else {
                    n = Math.max((18 - py), -35);
                    n = (int) (n * (192.0 / 35.0));
                }
            } else {
                n = (int) ((72 - py) * (127.0 / 72.0));
            }

            setVolume(chipn, true, n);
        }
    };

    private void tsmiLoadDriverBalance_Click(ActionEvent ev) {

    }

    private void tsmiLoadSongBalance_Click(ActionEvent ev) {

    }

    private void tsmiSaveDriverBalance_Click(ActionEvent ev) {
        try {
            String retMsg = parent.SaveDriverBalance(parent.setting.getBalance().clone());
            if (!retMsg.isEmpty()) {
                JOptionPane.showMessageDialog(null, "The driver's Mixer-Balance [%s] has been saved to the settings folder.".formatted(retMsg), "Save", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "%s".formatted(ex.getMessage()), "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tsmiSaveSongBalance_Click(ActionEvent ev) {
        try {
            Setting.Balance bln = parent.setting.getBalance().clone();
            PlayList.Music ms = parent.GetPlayingMusicInfo();
            if (ms == null) {
                JOptionPane.showMessageDialog(null, "Performance information could not be retrieved.\nPlease try again during or immediately after the performance.",
                        "Information acquisition failure", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser sfd = new JFileChooser();
            sfd.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".mbc");
                }

                @Override
                public String getDescription() {
                    return " Mixer - Balance(*.mbc)";
                }
            });
            sfd.setDialogTitle(" Mixer - Save Balance");
            sfd.setCurrentDirectory(Path.of(ms.arcFileName == null || ms.arcFileName.isEmpty() ? ms.fileName : ms.arcFileName).getParent().toFile());
            if (!parent.setting.getAutoBalance().getSamePositionAsSongData())
                sfd.setCurrentDirectory(new File((Common.settingFilePath = java.nio.file.Path.of("MixerBalance")).toString()));

//            sfd.RestoreDirectory = false;
            sfd.setSelectedFile(Path.of(Path.of((ms.arcFileName == null || ms.arcFileName.isEmpty() ? ms.fileName : ms.arcFileName)).getFileName() + ".mbc").toFile());
//            sfd.CheckPathExists = true;

            if (sfd.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            bln.save(java.nio.file.Path.of(sfd.getSelectedFile().getPath()));
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "%s".formatted(ex.getMessage()), "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();
        this.ctxtMenu = new JPopupMenu();
        this.tsmiLoadDriverBalance = new JMenuItem();
        this.tsmiLoadSongBalance = new JMenuItem();
        this.toolStripSeparator1 = new JSeparator();
        this.tsmiSaveDriverBalance = new JMenuItem();
        this.tsmiSaveSongBalance = new JMenuItem();

        //
        // pbScreen
        //
        this.image = Common.getImage("planeMixer");
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 288));
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick); // TODO
        this.pbScreen.addMouseMotionListener(this.pbScreen_MouseDrag);
        this.pbScreen.addMouseWheelListener(this.pbScreen_MouseWheel); // TODO
        //
        // ctxtMenu
        //
        this.ctxtMenu.add(this.tsmiLoadDriverBalance);
        this.ctxtMenu.add(this.tsmiLoadSongBalance);
        this.ctxtMenu.add(this.toolStripSeparator1);
        this.ctxtMenu.add(this.tsmiSaveDriverBalance);
        this.ctxtMenu.add(this.tsmiSaveSongBalance);
        this.ctxtMenu.setName("ctxtMenu");
        this.ctxtMenu.setPreferredSize(new Dimension(224, 98));
        //
        // tsmiLoadDriverBalance
        //
        this.tsmiLoadDriverBalance.setEnabled(false);
        this.tsmiLoadDriverBalance.setName("tsmiLoadDriverBalance");
        this.tsmiLoadDriverBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiLoadDriverBalance.setText("Load Driver Mixer - Balance");
        this.tsmiLoadDriverBalance.addActionListener(this::tsmiLoadDriverBalance_Click);
        //
        // tsmiLoadSongBalance
        //
        this.tsmiLoadSongBalance.setEnabled(false);
        this.tsmiLoadSongBalance.setName("tsmiLoadSongBalance");
        this.tsmiLoadSongBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiLoadSongBalance.setText("Load Song Mixer - Balance");
        this.tsmiLoadSongBalance.addActionListener(this::tsmiLoadSongBalance_Click);
        //
        // toolStripSeparator1
        //
        this.toolStripSeparator1.setName("toolStripSeparator1");
        this.toolStripSeparator1.setPreferredSize(new Dimension(220, 6));
        //
        // tsmiSaveDriverBalance
        //
        this.tsmiSaveDriverBalance.setName("tsmiSaveDriverBalance");
        this.tsmiSaveDriverBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiSaveDriverBalance.setText("Save Driver Mixer - Balance");
        this.tsmiSaveDriverBalance.addActionListener(this::tsmiSaveDriverBalance_Click);
        //
        // tsmiSaveSongBalance
        //
        this.tsmiSaveSongBalance.setName("tsmiSaveSongBalance");
        this.tsmiSaveSongBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiSaveSongBalance.setText("Save Song Mixer - Balance");
        this.tsmiSaveSongBalance.addActionListener(this::tsmiSaveSongBalance_Click);
        //
        // frmMixer2
        //
        this.setPreferredSize(new Dimension(320, 288));
        this.getContentPane().add(this.pbScreen);
        this.setIconImage(Common.getImage("Feli128"));
        this.setName("frmMixer2");
        this.setTitle("Mixer");
        this.addWindowListener(this.windowListener);
        this.addKeyListener(this.frmMixer2_KeyDown);
        this.addMouseListener(this.frmMixer2_MouseClick); // TODO
        this.addMouseWheelListener(this.pbScreen_MouseWheel); // TODO
        this.addComponentListener(this.componentListener);
    }

    BufferedImage image;
    public ScreenPanel pbScreen;
    private JPopupMenu ctxtMenu;
    private JMenuItem tsmiSaveDriverBalance;
    private JMenuItem tsmiSaveSongBalance;
    private JMenuItem tsmiLoadDriverBalance;
    private JMenuItem tsmiLoadSongBalance;
    private JSeparator toolStripSeparator1;

    @SuppressWarnings("unchecked")
    private final Tuple<String, Class<? extends Chip>>[] setVolume = Arrays.<Tuple<String, Class<? extends Chip>>>asList(
            null, // master
            new Tuple<>(MAIN_TAG, Ym2151Chip.class),
            new Tuple<>(MAIN_TAG, Ym2203Chip.class),
            new Tuple<>("FM", Ym2203Chip.class),
            new Tuple<>("PSG", Ym2203Chip.class),
            new Tuple<>(MAIN_TAG, Ym2612Chip.class),
            new Tuple<>(MAIN_TAG, Ym2608Chip.class),
            new Tuple<>("FM", Ym2608Chip.class),
            new Tuple<>("PSG", Ym2608Chip.class),
            new Tuple<>("Rhythm", Ym2608Chip.class),
            new Tuple<>("Adpcm", Ym2608Chip.class),
            new Tuple<>(MAIN_TAG, Ym2610Chip.class),
            new Tuple<>("FM", Ym2610Chip.class),
            new Tuple<>("PSG", Ym2610Chip.class),
            new Tuple<>("AdpcmA", Ym2610Chip.class),
            new Tuple<>("AdpcmB", Ym2610Chip.class),
            new Tuple<>(MAIN_TAG, Ym2413Chip.class),
            new Tuple<>(MAIN_TAG, Ym3526Chip.class),
            new Tuple<>(MAIN_TAG, Y8950Chip.class),
            new Tuple<>(MAIN_TAG, Ym3812Chip.class),
            new Tuple<>(MAIN_TAG, YmF262Chip.class),
            new Tuple<>(MAIN_TAG, YmF278BChip.class),
            new Tuple<>(MAIN_TAG, YmZ280BChip.class),
            new Tuple<>(MAIN_TAG, YmF271Chip.class),
            null,
            new Tuple<>(MAIN_TAG, Ay8910Chip.class),
            new Tuple<>(MAIN_TAG, Sn76489Chip.class),
            new Tuple<>(MAIN_TAG, HuC6280Chip.class),
            new Tuple<>(MAIN_TAG, Saa1099Chip.class),
            null,
            null,
            null,
            null,
            null,
            new Tuple<>(MAIN_TAG, Rf5C164Chip.class),
            new Tuple<>(MAIN_TAG, Rf5C68Chip.class),
            new Tuple<>(MAIN_TAG, PwmChip.class),
            new Tuple<>(MAIN_TAG, OkiM6258Chip.class),
            new Tuple<>(MAIN_TAG, OkiM6295Chip.class),
            new Tuple<>(MAIN_TAG, C140Chip.class),
            new Tuple<>(MAIN_TAG, C352Chip.class),
            new Tuple<>(MAIN_TAG, SegaPcmChip.class),
            new Tuple<>(MAIN_TAG, MultiPcmChip.class),
            new Tuple<>(MAIN_TAG, K051649Chip.class),
            new Tuple<>(MAIN_TAG, K053260Chip.class),
            new Tuple<>(MAIN_TAG, K054539Chip.class),
            new Tuple<>(MAIN_TAG, QSoundChip.class),
            new Tuple<>(MAIN_TAG, Ga20Chip.class),
            new Tuple<>(MAIN_TAG, NesChip.class),
            new Tuple<>(MAIN_TAG, DmcChip.class),
            new Tuple<>(MAIN_TAG, FdsChip.class),
            new Tuple<>(MAIN_TAG, Mmc5Chip.class),
            new Tuple<>(MAIN_TAG, N163Chip.class),
            new Tuple<>(MAIN_TAG, Vrc6Chip.class),
            new Tuple<>(MAIN_TAG, Vrc7Chip.class),
            new Tuple<>(MAIN_TAG, Fme7Chip.class),
            new Tuple<>(MAIN_TAG, DmgChip.class),
            null,
            null,
            null,
            null,
            new Tuple<>(MAIN_TAG, Ppz8Chip.class)
    ).toArray(Tuple[]::new);

//#region draw buffer

    private static void screenInitMixer(FrameBuffer screen) {
    }

    private static void drawFader(FrameBuffer screen, int x, int y, int t, int od, int nd) {
        if (od == nd)
            return;

        drawFaderSlitP(screen, x, y - 8);
        drawFont4IntM(screen, x, y + 48, 3, nd);

        int n;

        if (nd >= 0) {
            n = -(int) (nd / 20.0 * 8.0);
        } else {
            n = -(int) (nd / 192.0 * 35.0);
        }

        y += n;

        drawFaderP(screen, x, y, t);

        od = nd;
    }

    private static void drawGFader(FrameBuffer screen, int x, int y, int t, int od, int nd) {
        if (od == nd)
            return;

        drawFaderSlitP(screen, x, y - 8);
        drawFont4IntM(screen, x, y + 48, 3, nd);

        int n = 35 - (int) (nd / 127.0 * 43.0);
        y += n;

        drawFaderP(screen, x, y, t);

        od = nd;
    }

    private static void MixerVolume(FrameBuffer screen, int x, int y, int od, int nd, int ov, int nv) {
        if (od == nd && ov == nv)
            return;

        for (int i = 0; i < 44; i++) {
            int t = i < 8 ? 0 : 1;
            if (i % 2 != 0)
                t = 2;
            else if (44 - i > nd)
                t = 2;

            drawMixerVolumeP(screen, x, y + i, t);
        }

        drawMixerVolumeP(screen, x, y + (44 - nv), nv > 36 ? 0 : 1);

        od = nd;
        ov = nv;
    }

    private static void drawFont4IntM(FrameBuffer screen, int x, int y, int k, int num) {
        if (screen == null)
            return;

        int t = 0;
        int n;

        if (num < 0) {
            num = -num;
            screen.drawByteArray(x - 4, y, FrameBuffer.rFont2[t], 128, 52, 1, 4, 7);
        } else {
            if (num != 0)
                t = 1;
            screen.drawByteArray(x - 4, y, FrameBuffer.rFont2[t], 128, 24, 1, 4, 7);
        }

        if (k == 3) {
            boolean f = false;
            n = num / 100;
            num -= n * 100;
            n = (n > 9) ? 0 : n;
            if (n != 0) {
                screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, n * 4 + 64, 1, 4, 7);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, 0, 1, 4, 7);
            }

            n = num / 10;
            num -= n * 10;
            x += 4;
            if (n != 0 || f) {
                screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, n * 4 + 64, 1, 4, 7);
                if (n != 0) {
                    f = true;
                }
            } else {
                screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, 0, 1, 4, 7);
            }

            n = num / 1;
            x += 4;
            screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, n * 4 + 64, 1, 4, 7);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        if (n != 0) {
            screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, n * 4 + 64, 1, 4, 7);
        } else {
            screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, 0, 1, 4, 7);
        }

        n = num / 1;
        x += 4;
        screen.drawByteArray(x, y, FrameBuffer.rFont2[t], 128, n * 4 + 64, 1, 4, 7);
    }

    private static void drawFaderSlitP(FrameBuffer screen, int x, int y) {
        screen.drawByteArray(x, y, FrameBuffer.rFader, 32, 16, 0, 8, 8);
        screen.drawByteArray(x, y + 8, FrameBuffer.rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 16, FrameBuffer.rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 24, FrameBuffer.rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 32, FrameBuffer.rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 40, FrameBuffer.rFader, 32, 16, 8, 8, 8);
        screen.drawByteArray(x, y + 48, FrameBuffer.rFader, 32, 24, 0, 8, 8);
    }

    private static void drawFaderP(FrameBuffer screen, int x, int y, int t) {
        screen.drawByteArray(x, y, FrameBuffer.rFader, 32, t == 0 ? 0 : 8, 0, 8, 13);
    }

    private static void drawMixerVolumeP(FrameBuffer screen, int x, int y, int t) {
        screen.drawByteArray(x, y, FrameBuffer.rFader, 32, 24, 8 + t, 2, 1);
    }

//#endregion
}
