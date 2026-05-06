/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.Timer;

import net.sf.saxon.trans.SymbolicName.F;


/**
 * Swing port of the {@code fmdsp-pacc} sound visualizer used by 98fmplayer.
 * <p>
 * The component is purely a renderer: it pulls data through the
 * {@link FmDspDataSource} interface on every frame and never depends on the
 * chip emulator. Layout and palette mirror the original 640x400 FMDSP screen,
 * but rendering uses Java2D primitives instead of pre-baked sprites.
 *
 * <h2>Logical layout (640x400)</h2>
 * <ul>
 *   <li>0..319    : 10 (or 13) per-track strips, each with a piano-style key
 *                  display, ticks bar, and text status row.</li>
 *   <li>320..639  : right side - spectrum (top), level meter strip (mid),
 *                  time/clock counters and title (bottom).</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * The component reads from its data source on the EDT each repaint. If your
 * data source is updated from another thread, make sure those updates are
 * either atomic or guarded.
 */
public class FmDspVisualizer extends JComponent {

    // ---------- canvas / palette ----------
    public static final int CANVAS_W = 640;
    public static final int CANVAS_H = 400;

    // ---------- per-track strip layout (lifted from fmdsp_sprites.h) ----------
    private static final int TRACK_H = 32;
    private static final int TRACK_H_S = 24;
    private static final int TINFO_X = 47;
    private static final int TDETAIL_X = 67;
    private static final int NUM_X = 31;
    private static final int NUM_W = 8;
    private static final int NUM_H = 11;
    private static final int KEY_X = 7;
    private static final int KEY_W = 35;
    private static final int KEY_H = 17;
    private static final int KEY_LEFT_W = 6;
    private static final int KEY_RIGHT_W = 11;
    private static final int KEY_OCTAVES = 8;
    private static final int KEY_Y = 14;
    private static final int BAR_L_X = 66;
    private static final int BAR_L_W = 14;
    private static final int BAR_X = BAR_L_X + BAR_L_W;
    private static final int BAR_Y = 1;
    private static final int BAR_W = 2;
    private static final int BAR_H = 4;

    // ---------- right-half visualizer layout ----------
    private static final int SPECTRUM_X = 352;
    private static final int SPECTRUM_Y = 207;
    private static final int LEVEL_X = 353 - 16;
    private static final int LEVEL_Y = 227;
    private static final int LEVEL_DISP_W = 14;
    private static final int LEVEL_W = 16;
    private static final int PANPOT_W = 15;
    private static final int PANPOT_H = 15;
    private static final int PANPOT_Y = LEVEL_Y + 64;
    private static final int LEVEL_TRACK_Y = LEVEL_Y - 9;
    private static final int LEVEL_PROG_Y = PANPOT_Y + PANPOT_H;
    private static final int LEVEL_KEY_Y = LEVEL_PROG_Y + 7;
    private static final int TIME_TEXT_X = 530;
    private static final int TIME_X = TIME_TEXT_X + 38;
    private static final int TIME_Y = 22;
    private static final int CLOCK_Y = TIME_Y + 19;
    private static final int TIMERB_Y = CLOCK_Y + 19;
    private static final int LOOPCNT_Y = TIMERB_Y + 19;
    private static final int CPU_X = 320;
    private static final int CPU_NUM_X = CPU_X + 56;
    private static final int CPU_NUM_Y = TIME_Y;
    private static final int FPS_NUM_X = CPU_X + 100 + 61;
    private static final int CIRCLE_X = 312;
    private static final int CIRCLE_Y = 70;
    private static final int CIRCLE_W = 31;
    private static final int CIRCLE_H = 31;

    // ---------- modes ----------
    private static final TrackId[] TRACKS_OPNA = {
        TrackId.FM_1, TrackId.FM_2, TrackId.FM_3, TrackId.FM_4, TrackId.FM_5, TrackId.FM_6,
        TrackId.SSG_1, TrackId.SSG_2, TrackId.SSG_3, TrackId.ADPCM,
    };
    private static final TrackId[] TRACKS_OPN = {
        TrackId.FM_1, TrackId.FM_2, TrackId.FM_3,
        TrackId.FM_3_EX_1, TrackId.FM_3_EX_2, TrackId.FM_3_EX_3,
        TrackId.SSG_1, TrackId.SSG_2, TrackId.SSG_3, TrackId.ADPCM,
    };
    private static final TrackId[] TRACKS_PPZ8 = {
        TrackId.PPZ8_1, TrackId.PPZ8_2, TrackId.PPZ8_3, TrackId.PPZ8_4,
        TrackId.PPZ8_5, TrackId.PPZ8_6, TrackId.PPZ8_7, TrackId.PPZ8_8,
        TrackId.ADPCM,
    };
    private static final TrackId[] TRACKS_13 = {
        TrackId.FM_1, TrackId.FM_2, TrackId.FM_3,
        TrackId.FM_3_EX_1, TrackId.FM_3_EX_2, TrackId.FM_3_EX_3,
        TrackId.FM_4, TrackId.FM_5, TrackId.FM_6,
        TrackId.SSG_1, TrackId.SSG_2, TrackId.SSG_3,
        TrackId.ADPCM,
    };

    private static final String[] NOTE_NAMES = {
        "C", "C+", "D", "D+", "E", "F", "F+", "G", "G+", "A", "A+", "B",
    };

    /** Which keys (0..11) are black on a piano. */
    private static final boolean[] BLACK = {
        false, true, false, true, false, false, true, false, true, false, true, false,
    };

    // ---------- mutable state ----------
    private FmDspDataSource source;
    private final Palette palette = new Palette();
    private LeftMode leftMode = LeftMode.OPNA;
    private RightMode rightMode = RightMode.DEFAULT;

    // Peak-hold state for FFT and level meter, mirrors the C code's
    // count/dropdiv/data triplets and the divtab.
    private final int[] fftBars = new int[FftDataSource.LENGTH];
    private final int[] fftPeak = new int[FftDataSource.LENGTH];
    private final int[] fftPeakCnt = new int[FftDataSource.LENGTH];
    private final int[] fftPeakDiv = new int[FftDataSource.LENGTH];
    private final int[] levelBars = new int[LevelDataSource.COUNT];
    private final int[] levelPeak = new int[LevelDataSource.COUNT];
    private final int[] levelPeakCnt = new int[LevelDataSource.COUNT];
    private final int[] levelPeakDiv = new int[LevelDataSource.COUNT];

    private static final int[] DROP_DIVTAB = {
        32, 16, 8, 8, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 2,
    };

    private long frameCount;
    private final TrackStatus scratch = new TrackStatus();

    private final Timer timer;

    private Font num9;
    private Font num12;
    private Font title;
    private Font txt9;
    private Font txt10;
    private Font txt11;
    private Font txt12;

    /**
     * @param fps target frame rate, e.g. {@code 60}.
     */
    public FmDspVisualizer(int fps) {
        if (fps <= 0) fps = 60;
        setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
        setBackground(Color.BLACK);
        setOpaque(true);
        timer = new Timer(1000 / fps, e -> repaint());
        timer.setRepeats(true);

        try {
//            Font textBase = Font.createFont(Font.TRUETYPE_FONT, Files.newInputStream(Path.of("/Users/nsano/.config/sdlnp21kai/pc-9800.ttf")));
            Font textBase = new Font("monospace", Font.PLAIN, 9);
            Font numberBase = Font.createFont(Font.TRUETYPE_FONT, FmDspVisualizer.class.getResourceAsStream("/DSEG7Classic-BoldItalic.ttf"));
            num9 = numberBase.deriveFont(Font.ITALIC, 9);
            num12 = numberBase.deriveFont(Font.ITALIC, 12);
            txt9 = textBase.deriveFont(Font.PLAIN, 9);
            title = textBase.deriveFont(Font.PLAIN, 14);
            txt10 = textBase.deriveFont(Font.PLAIN, 10);
            txt11 = textBase.deriveFont(Font.PLAIN, 11);
            txt12 = textBase.deriveFont(Font.PLAIN, 12);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public FmDspVisualizer() {
        this(60);
    }

    /** Set the data source. May be replaced at any time. */
    public void setDataSource(FmDspDataSource source) {
        this.source = source;
    }

    public LeftMode getLeftMode() {
        return leftMode;
    }

    public void setLeftMode(LeftMode mode) {
        this.leftMode = mode;
    }

    public RightMode getRightMode() {
        return rightMode;
    }

    public void setRightMode(RightMode mode) {
        this.rightMode = mode;
    }

    /** Switch palette index (0..{@link Palette#COUNT}-1). */
    public void setPaletteIndex(int index) {
        palette.select(index);
    }

    /** Start the repaint timer. */
    public void start() {
        timer.start();
    }

    /** Stop the repaint timer. */
    public void stop() {
        timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // Logical 640x400 canvas - scale to component size.
            double sx = getWidth() / (double) CANVAS_W;
            double sy = getHeight() / (double) CANVAS_H;
            g2.scale(sx, sy);

            palette.tick();

            // Background.
            g2.setColor(palette.color(0));
            g2.fillRect(0, 0, CANVAS_W, CANVAS_H);

            renderLeft(g2);
            renderRight(g2);

            frameCount++;
        } finally {
            g2.dispose();
        }
    }

    // ---------- left half ----------

    private void renderLeft(Graphics2D g2) {
        TrackId[] tracks;
        int trackHeight;
        switch (leftMode) {
        case OPN:
            tracks = TRACKS_OPN;
            trackHeight = TRACK_H;
            break;
        case THIRTEEN:
            tracks = TRACKS_13;
            trackHeight = TRACK_H_S;
            break;
        case PPZ8:
            tracks = TRACKS_PPZ8;
            trackHeight = TRACK_H;
            break;
        case OPNA:
        default:
            tracks = TRACKS_OPNA;
            trackHeight = TRACK_H;
            break;
        }
        TrackStatusSource src = source != null ? source.trackStatus() : null;
        for (int i = 0; i < tracks.length; i++) {
            TrackId t = tracks[i];
            if (src != null) {
                src.readStatus(t, scratch);
            } else {
                resetStatus(scratch);
            }
            renderTrackRow(g2, 0, i * trackHeight, t, scratch, src != null && src.masked(t), trackHeight);
        }
    }

    private static void resetStatus(TrackStatus s) {
        s.playing = false;
        s.info = TrackInfo.NORMAL;
        s.ticks = 0;
        s.ticksLeft = 0;
        s.key = 0xff;
        s.actualKey = 0xff;
        s.toneNum = 0;
        s.volume = 0;
        s.gate = 0;
        s.detune = 0;
        s.status = "";
        Arrays.fill(s.fmSlotMask, false);
        s.ppz8Ch = 0;
        s.ssgTone = false;
        s.ssgNoise = false;
    }

    private void renderTrackRow(Graphics2D g2, int x, int y, TrackId track, TrackStatus st, boolean masked, int trackHeight) {
        Color cText = palette.color(1);
        Color cAccent = palette.color(2);
        Color cDim = palette.color(3);
        Color cMute = palette.color(7);

        FontMetrics fm = g2.getFontMetrics(txt9);

        // Track type tag (FM/SSG/ADPCM/PPZ8) + 2-digit number.
        TrackType type = trackTypeOf(track);
        int num1 = trackTypeNumber(track);
        g2.setFont(txt9);
        g2.setColor(masked ? cMute : cAccent);
        g2.drawString(typeAbbrev(type), x + 1, y + fm.getAscent());

        // 2-digit number (mute -> dashes).
        g2.setColor(masked ? cMute : cText);
        String numStr = masked ? "--" : String.format("%02d", num1);
        g2.setFont(num9);
        g2.drawString(numStr, x + NUM_X, y + 1 + fm.getAscent());

        // info badge (PPZ8/PDZF/EFF/EX...).
        if (st.playing || st.info == TrackInfo.SSGEFF) {
            g2.setColor(cDim);
            switch (st.info) {
            case PPZ8:
                g2.drawString("PPZ8", x + TINFO_X, y + 6 + fm.getAscent());
                break;
            case PDZF:
                g2.drawString("PDZF", x + TINFO_X, y + 6 + fm.getAscent());
                break;
            case SSGEFF:
                g2.drawString("EFF", x + TINFO_X + 2, y + fm.getAscent());
                if (st.ssgNoise) {
                    g2.drawString((st.ssgTone ? "M" : "N") + "..", x + TINFO_X + 2, y + 6 + fm.getAscent());
                }
                break;
            case SSG:
                if (st.ssgNoise) {
                    g2.drawString((st.ssgTone ? "M" : "N") + "..", x + TINFO_X + 2, y + 6 + fm.getAscent());
                }
                break;
            case FM3EX:
                g2.drawString("EX", x + TINFO_X + 5, y + fm.getAscent());
                StringBuilder slots = new StringBuilder(4);
                for (int i = 0; i < 4; i++) slots.append(st.fmSlotMask[i] ? ' ' : (char) ('1' + i));
                g2.drawString(slots.toString(), x + TINFO_X, y + 6 + fm.getAscent());
                break;
            default:
                break;
            }
        }

        // Detail labels + values (KN: TN: Vl: GT: DT: M:).
        g2.setColor(cText);
        int textY = y + 6 + fm.getAscent();
        g2.drawString("KN:", x + TDETAIL_X, textY);
        g2.drawString("TN:", x + TDETAIL_X + 41, textY);
        g2.drawString("Vl:", x + TDETAIL_X + 74, textY);
        g2.drawString("GT:", x + TDETAIL_X + 105, textY);
        g2.drawString("DT:", x + TDETAIL_X + 141, textY);
        g2.drawString("M:", x + 249, textY);

        g2.setColor(cDim);
        String keyStr;
        if (!st.playing) {
            keyStr = "S";
        } else if ((st.key & 0xf) == 0xf) {
            keyStr = "R";
        } else {
            int oct = (st.key >> 4) & 0xf;
            int n = st.key & 0xf;
            keyStr = "o" + oct + (n < NOTE_NAMES.length ? NOTE_NAMES[n] : "");
        }
        g2.drawString(keyStr, x + TDETAIL_X + 13, textY);
        g2.drawString(String.format("%03d", st.toneNum), x + TDETAIL_X + 54, textY);
        g2.drawString(String.format("%03d", st.volume), x + TDETAIL_X + 87, textY);
        g2.drawString(String.format("%03d", st.gate), x + TDETAIL_X + 118, textY);
        g2.drawString(String.format("%03d", Math.abs(st.detune)), x + TDETAIL_X + 158, textY);
        if (st.detune != 0) {
            g2.setColor(cAccent);
            g2.drawString(st.detune < 0 ? "-" : "+", x + TDETAIL_X + 154, textY);
        }
        g2.setColor(cDim);
        g2.drawString(st.status == null ? "" : st.status, x + 257, textY);

        // Ticks bar (small horizontal indicator under the text row).
        int barY = y + BAR_Y;
        g2.setColor(cDim);
        g2.fillRect(x + BAR_L_X, barY, BAR_L_W - 1, BAR_H);
        int width = Math.max(0, Math.min(64, st.ticksLeft >> 2));
        Color barCol = (((st.key & 0xf) == 0xf) || masked) ? cMute : (st.playing ? cAccent : cDim);
        g2.setColor(barCol);
        g2.fillRect(x + BAR_X, barY, BAR_W * width, BAR_H);
        // current play head line.
        int head = Math.max(0, Math.min(64, st.ticks >> 2));
        g2.setColor(cMute);
        g2.fillRect(x + BAR_X + BAR_W * head, barY, BAR_W, BAR_H);

        // Mini keyboard.
        renderKeyboard(g2, x + KEY_X, y + KEY_Y, st, masked, trackHeight == TRACK_H_S);
    }

    private void renderKeyboard(Graphics2D g2, int kx, int ky, TrackStatus st, boolean masked, boolean small) {
        int kh = small ? KEY_H - 8 : KEY_H;
        Color white = palette.color(4);
        Color black = palette.color(5);
        Color border = palette.color(3);
        Color hilite = masked ? palette.color(7) : palette.color(6);
        Color shadow = palette.color(8);

        // Backgrounds: left edge, octaves, right edge.
        g2.setColor(white);
        g2.fillRect(kx - KEY_LEFT_W, ky, KEY_LEFT_W, kh);
        for (int o = 0; o < KEY_OCTAVES; o++) {
            g2.fillRect(kx + KEY_W * o, ky, KEY_W, kh);
        }
        g2.fillRect(kx + KEY_W * KEY_OCTAVES, ky, KEY_RIGHT_W, kh);

        // Black-key strip.
        int blackH = (kh * 3) / 5;
        for (int o = 0; o < KEY_OCTAVES; o++) {
            int ox = kx + KEY_W * o;
            for (int n = 0; n < 12; n++) {
                if (BLACK[n]) {
                    int wx = ox + (n * KEY_W) / 12;
                    g2.setColor(black);
                    g2.fillRect(wx, ky, KEY_W / 12 + 1, blackH);
                }
            }
            // octave separator.
            g2.setColor(border);
            g2.drawLine(ox, ky, ox, ky + kh - 1);
        }

        // Currently played note hilite (actual_key = pitch-bend-adjusted).
        if (st.playing || st.info == TrackInfo.SSGEFF) {
            paintKey(g2, kx, ky, kh, st.actualKey, shadow);
            paintKey(g2, kx, ky, kh, st.key, hilite);
        }

        // Border around the whole keyboard.
        g2.setColor(border);
        g2.drawRect(kx - KEY_LEFT_W, ky, KEY_LEFT_W + KEY_W * KEY_OCTAVES + KEY_RIGHT_W - 1, kh - 1);
    }

    private static void paintKey(Graphics2D g2, int kx, int ky, int kh, int key, Color color) {
        if (key == 0xff) return;
        int oct = (key >> 4) & 0xf;
        int n = key & 0xf;
        if (oct >= KEY_OCTAVES || n >= 12) return;
        int ox = kx + KEY_W * oct + (n * KEY_W) / 12;
        int w = KEY_W / 12 + 1;
        int h = BLACK[n] ? (kh * 3) / 5 : kh;
        Graphics2D g3 = (Graphics2D) g2.create();
        try {
            g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g3.setColor(color);
            g3.fillRect(ox, ky, w, h);
        } finally {
            g3.dispose();
        }
    }

    private static TrackType trackTypeOf(TrackId t) {
        switch (t) {
        case FM_1: case FM_2: case FM_3:
        case FM_3_EX_1: case FM_3_EX_2: case FM_3_EX_3:
        case FM_4: case FM_5: case FM_6:
            return TrackType.FM;
        case SSG_1: case SSG_2: case SSG_3:
            return TrackType.SSG;
        case ADPCM:
            return TrackType.ADPCM;
        default:
            return TrackType.PPZ8;
        }
    }

    private static int trackTypeNumber(TrackId t) {
        switch (t) {
        case FM_1: return 1;
        case FM_2: return 2;
        case FM_3: case FM_3_EX_1: case FM_3_EX_2: case FM_3_EX_3: return 3;
        case FM_4: return 4;
        case FM_5: return 5;
        case FM_6: return 6;
        case SSG_1: case PPZ8_1: return 1;
        case SSG_2: case PPZ8_2: return 2;
        case SSG_3: case PPZ8_3: return 3;
        case PPZ8_4: return 4;
        case PPZ8_5: return 5;
        case PPZ8_6: return 6;
        case PPZ8_7: return 7;
        case PPZ8_8: return 8;
        case ADPCM: default: return 1;
        }
    }

    private static String typeAbbrev(TrackType t) {
        switch (t) {
        case FM: return "FM";
        case SSG: return "SSG";
        case ADPCM: return "ADPCM";
        case PPZ8: return "PPZ8";
        default: return "";
        }
    }

    // ---------- right half ----------

    private void renderRight(Graphics2D g2) {
        // Spectrum (always drawn).
        renderFft(g2);
        // Level meter strip with panpot dot, program number, key.
        renderLevelMeter(g2);
        // Time / clock counters and progress bar.
        renderTimeBlock(g2);
        // Rotating circle under the title.
        renderCircle(g2);
        // Title / filename.
        renderTitle(g2);
    }

    private void renderFft(Graphics2D g2) {
        // Gather raw bars.
        int[] raw = new int[FftDataSource.LENGTH];
        if (source != null && source.fft() != null) {
            source.fft().readFft(raw);
        }
        // Decay the bar values themselves down to incoming values. The C code
        // stores raw[i] each frame and does the peak-hold for the dot only;
        // the bars track raw[i] directly.
        for (int i = 0; i < raw.length; i++) {
            int v = Math.max(0, Math.min(FftDataSource.MAX, raw[i]));
            fftBars[i] = v;
            // Peak hold + drop, mirrors the C divtab decay.
            if (fftPeak[i] <= v) {
                fftPeak[i] = v;
                fftPeakCnt[i] = 30;
                fftPeakDiv[i] = 0;
            } else if (fftPeakCnt[i] > 0) {
                fftPeakCnt[i]--;
            } else if (fftPeak[i] > 0) {
                if (fftPeakDiv[i] > 0) {
                    fftPeakDiv[i]--;
                } else {
                    fftPeakDiv[i] = DROP_DIVTAB[Math.min(15, fftPeak[i] / 2)];
                    fftPeak[i]--;
                }
            }
        }

        Color barCol = palette.color(2);
        Color peakCol = palette.color(7);
        for (int i = 0; i < FftDataSource.LENGTH; i++) {
            int h = fftBars[i] * 2;
            g2.setColor(barCol);
            g2.fillRect(SPECTRUM_X + i * 4, SPECTRUM_Y - 62 + (32 - fftBars[i]) * 2, 3, h);
            g2.setColor(peakCol);
            g2.fillRect(SPECTRUM_X + i * 4, SPECTRUM_Y - fftPeak[i] * 2, 3, 1);
        }
    }

    private void renderLevelMeter(Graphics2D g2) {
        LevelDataSource lvl = source != null ? source.level() : null;
        TrackStatusSource ts = source != null ? source.trackStatus() : null;

        Color bar = palette.color(2);
        Color peak = palette.color(7);
        Color label = palette.color(1);
        Color labelDim = palette.color(3);
        Color panActive = palette.color(1);
        Color panMute = palette.color(5);

        for (int c = 0; c < LevelDataSource.COUNT; c++) {
            int level = lvl != null ? Math.clamp(lvl.level(c), 0, 32767) : 0;
            int displayed = 0;
            if (level > 0) {
                double db = 20.0 * Math.log10(level / 32768.0);
                double f = (db / 48.0 + 1.0) * 32.0;
                displayed = (int) Math.clamp(f, 0, 32);
            }
            // Peak hold.
            if (levelPeak[c] <= displayed) {
                levelPeak[c] = displayed;
                levelPeakCnt[c] = 30;
                levelPeakDiv[c] = 0;
            } else if (levelPeakCnt[c] > 0) {
                levelPeakCnt[c]--;
            } else if (levelPeak[c] > 0) {
                if (levelPeakDiv[c] > 0) {
                    levelPeakDiv[c]--;
                } else {
                    levelPeakDiv[c] = DROP_DIVTAB[Math.min(15, levelPeak[c] / 2)];
                    levelPeak[c]--;
                }
            }
            levelBars[c] = displayed;

            int x = LEVEL_X + LEVEL_W * c;
            // bar
            g2.setColor(bar);
            g2.fillRect(x, LEVEL_Y + (64 - displayed * 2), LEVEL_DISP_W, displayed * 2);
            // peak line
            g2.setColor(peak);
            g2.fillRect(x, LEVEL_Y + (62 - levelPeak[c] * 2), LEVEL_DISP_W, 1);

            // panpot dot
            LevelDataSource.Pan p = lvl != null ? lvl.pan(c) : LevelDataSource.Pan.NONE;
            renderPanIndicator(g2, x - 1, PANPOT_Y, p, p == LevelDataSource.Pan.NONE ? panMute : panActive);

            // program / key (channel 9 is drum, no program number)
            TrackId tid = levelToTrack(c);
            int prog = 0;
            int key = 0xff;
            if (tid != null && ts != null) {
                ts.readStatus(tid, scratch);
                prog = scratch.toneNum;
                key = scratch.key;
            }
            g2.setColor(labelDim);
            if (c != 9) {
                g2.drawString(String.format("%03d", prog), x, LEVEL_PROG_Y + 8);
            } else {
                g2.drawString("DRM", x, LEVEL_PROG_Y + 8);
            }
            g2.setColor(label);
            String k;
            if (key == 0xff) k = "---";
            else if ((key & 0xf) == 0xf) k = " R ";
            else k = "o" + ((key >> 4) & 0xf) + NOTE_NAMES[Math.min(11, key & 0xf)];
            g2.drawString(k, x, LEVEL_KEY_Y + 8);
        }

        // 0 dB tick line across the meter strip.
        g2.setColor(palette.color(8));
        g2.drawLine(LEVEL_X, LEVEL_Y, LEVEL_X + LEVEL_W * LevelDataSource.COUNT, LEVEL_Y);

        // header label
        g2.setColor(palette.color(1));
        g2.drawString("LEVEL", LEVEL_X, LEVEL_TRACK_Y + 6);
    }

    private void renderPanIndicator(Graphics2D g2, int px, int py, LevelDataSource.Pan p, Color color) {
        if (p == null) return;
        int cx = px + PANPOT_W / 2;
        int cy = py + PANPOT_H / 2;
        // background tick
        g2.setColor(palette.color(3));
        g2.drawLine(px + 2, cy, px + PANPOT_W - 2, cy);
        if (p == LevelDataSource.Pan.NONE) return;
        int dot;
        switch (p) {
        case LEFT: dot = px + 2; break;
        case MID_LEFT: dot = px + (PANPOT_W / 2 + px) / 2 - 1; break;
        case CENTER: dot = cx; break;
        case MID_RIGHT: dot = (cx + px + PANPOT_W - 2) / 2 + 1; break;
        case RIGHT: dot = px + PANPOT_W - 3; break;
        default: dot = cx;
        }
        g2.setColor(color);
        g2.fillOval(dot - 2, cy - 2, 4, 4);
    }

    private static TrackId levelToTrack(int channel) {
        switch (channel) {
        case 0: return TrackId.FM_1;
        case 1: return TrackId.FM_2;
        case 2: return TrackId.FM_3;
        case 3: return TrackId.FM_4;
        case 4: return TrackId.FM_5;
        case 5: return TrackId.FM_6;
        case 6: return TrackId.SSG_1;
        case 7: return TrackId.SSG_2;
        case 8: return TrackId.SSG_3;
        case 9: return null; // rhythm
        case 10: return TrackId.ADPCM;
        case 11: return TrackId.PPZ8_1;
        case 12: return TrackId.PPZ8_2;
        case 13: return TrackId.PPZ8_3;
        case 14: return TrackId.PPZ8_4;
        case 15: return TrackId.PPZ8_5;
        case 16: return TrackId.PPZ8_6;
        case 17: return TrackId.PPZ8_7;
        case 18: return TrackId.PPZ8_8;
        default: return null;
        }
    }

    private void renderTimeBlock(Graphics2D g2) {
        WorkStateSource w = source != null ? source.work() : null;
        Color label = palette.color(1);
        Color value = palette.color(2);

        long frames = w != null ? w.generatedFrames() : 0L;
        int srate = w != null ? Math.max(1, w.sampleRate()) : 55467;
        int ssec = (int) ((frames % srate) * 100 / srate);
        long sec = frames / srate;
        long min = sec / 60;
        sec %= 60;
        boolean blink = (sec & 1) == 0;

        g2.setColor(label);
        g2.setFont(txt12);
        g2.drawString("TIME", TIME_TEXT_X - 32, TIME_Y + 9);
        g2.setColor(value);
        g2.setFont(num12);
        g2.drawString(String.format("%02d%s%02d.%02d", min, blink ? ":" : " ", sec, ssec), TIME_X - 8, TIME_Y + 9);

        long clock = w != null ? w.timerBCount() : 0L;
        int timerB = w != null ? w.timerB() : 0;
        int loop = w != null ? w.loopCount() : 0;

        g2.setColor(label);
        g2.setFont(txt12);
        g2.drawString("CLOCK", TIME_TEXT_X - 32, CLOCK_Y + 9);
        g2.drawString("TIMERB", TIME_TEXT_X - 32, TIMERB_Y + 9);
        g2.drawString("LOOP", TIME_TEXT_X - 32, LOOPCNT_Y + 9);
        g2.setColor(value);
        g2.setFont(num12);
        g2.drawString(String.format("%08d", clock), TIME_X - 8, CLOCK_Y + 9);
        g2.drawString(String.format("%03d", timerB), TIME_X + 32, TIMERB_Y + 9);
        g2.drawString(String.format("%04d", loop), TIME_X + 24, LOOPCNT_Y + 9);

        // Loop progress bar.
        long loopLen = w != null ? w.loopTimerBCount() : 0L;
        long loopPos = w != null ? w.timerBCountLoop() : 0L;
        int pos = 0;
        if (loopLen > 0) pos = (int) (loopPos * (72 + 1 - 4) / loopLen);
        g2.setColor(palette.color(3));
        g2.fillRect(352, 70, 144, 4);
        if (w != null && w.playing()) {
            g2.setColor(palette.color(2));
            g2.fillRect(352 + pos * 2, 70, 8, 4);
        }
        g2.setColor(loop > 0 ? palette.color(7) : palette.color(3));
        g2.fillRect(496, 70, 16, 4);

        // Transport buttons.
        boolean playing = w != null && w.playing() && !w.paused();
        boolean stopped = w == null || !w.playing();
        boolean paused = w != null && w.paused();
        drawPlay(g2, CPU_NUM_X + 50, CPU_NUM_Y, playing ? value : palette.color(3));
        drawStop(g2, CPU_NUM_X + 70, CPU_NUM_Y, stopped ? value : palette.color(3));
        drawPause(g2, CPU_NUM_X + 90, CPU_NUM_Y, paused ? value : palette.color(3));

        // Reuse FPS_NUM_X to avoid unused-warning.
        if (FPS_NUM_X >= 0) {
            // no-op - field exists for layout reference.
        }
    }

    private void drawPlay(Graphics2D g2, int x, int y, Color c) {
        Polygon p = new Polygon(new int[] {x, x + 11, x}, new int[] {y, y + 6, y + 12}, 3);
        g2.setColor(c);
        g2.fillPolygon(p);
    }

    private void drawStop(Graphics2D g2, int x, int y, Color c) {
        g2.setColor(c);
        g2.fillRect(x, y, 12, 12);
    }

    private void drawPause(Graphics2D g2, int x, int y, Color c) {
        g2.setColor(c);
        g2.fillRect(x, y, 4, 12);
        g2.fillRect(x + 7, y, 4, 12);
    }

    private void renderCircle(Graphics2D g2) {
        WorkStateSource w = source != null ? source.work() : null;
        boolean spinning = w != null && w.playing() && !w.paused();
        int phase = 8;
        if (w != null) {
            if (w.paused() && (frameCount % 32) >= 16) {
                phase = 8;
            } else if (spinning) {
                phase = (int) ((w.timerBCount() / 8) % 8);
            }
        }
        Graphics2D g3 = (Graphics2D) g2.create();
        try {
            g3.setStroke(new BasicStroke(2));
            g3.setColor(palette.color(spinning ? 6 : 3));
            int cx = CIRCLE_X + CIRCLE_W / 2;
            int cy = CIRCLE_Y + CIRCLE_H / 2;
            g3.drawOval(CIRCLE_X, CIRCLE_Y, CIRCLE_W - 1, CIRCLE_H - 1);
            if (phase < 8) {
                AffineTransform t = AffineTransform.getRotateInstance(phase * Math.PI / 4.0, cx, cy);
                g3.transform(t);
                g3.setColor(palette.color(2));
                g3.drawLine(cx, cy, cx + CIRCLE_W / 2 - 2, cy);
            }
        } finally {
            g3.dispose();
        }
    }

    private void renderTitle(Graphics2D g2) {
        WorkStateSource w = source != null ? source.work() : null;
        g2.setFont(title);
        g2.setColor(palette.color(2));
        g2.drawString("FMDSP", 352, 22);
        if (w != null && w.filename() != null) {
            g2.setFont(txt11);
            g2.setColor(palette.color(1));
            g2.drawString(w.filename(), 352 + 60, 22);
        }
        // 3 comment lines, if any.
        if (w != null) {
            g2.setFont(txt10);
            for (int i = 0; i < 3; i++) {
                String line = w.comment(i);
                if (line != null) {
                    g2.setColor(palette.color(2));
                    g2.drawString(line, 8, 350 + i * 12);
                }
            }
        }
    }
}
