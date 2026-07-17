package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.prefs.Preferences;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.form.ChipLEDs;
import mdplayer.Common;
import mdplayer.Common.EnmInstFormat;
import mdplayer.Common.EnmModel;
import mdplayer.form.DoubleBuffer;
import mdplayer.form.FrameBuffer;
import mdplayer.form.KeyboardHook;
import mdplayer.MDChipParams;
import mdplayer.MIDIParam;
import mdplayer.form.MmfControl;
import mdplayer.OpeManager;
import mdplayer.PlayList;
import mdplayer.Request;
import mdplayer.Request.enmRequest;
import mdplayer.form.ScreenPanel;
import mdplayer.Setting;
import mdplayer.TonePallet;
import mdplayer.YM2612MIDI;
import mdplayer.chips.*;
import mdplayer.chips.NesChip.DmcChip;
import mdplayer.chips.NpNesChip.Mmc5Chip;
import mdplayer.chips.NpNesChip.N163Chip;
import mdplayer.chips.NpNesChip.Vrc6Chip;
import mdplayer.chips.NpNesChip.Vrc7Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.form.Layouts;
import mdplayer.form.kb.driver.FormPPZ8;
import mdplayer.form.kb.FormMIDI;
import mdplayer.form.kb.FormRegTest;
import mdplayer.form.kb.FormYM2151;
import mdplayer.form.kb.FormYMZ280B;
import mdplayer.form.kb.nes.FormDMG;
import mdplayer.form.kb.nes.FormFDS;
import mdplayer.form.kb.nes.FormMMC5;
import mdplayer.form.kb.nes.FormN106;
import mdplayer.form.kb.nes.FormNESDMC;
import mdplayer.form.kb.nes.FormS5B;
import mdplayer.form.kb.nes.FormVRC6;
import mdplayer.form.kb.nes.FormVRC7;
import mdplayer.form.kb.opl.FormY8950;
import mdplayer.form.kb.opl.FormYM2413;
import mdplayer.form.kb.opl.FormYM3526;
import mdplayer.form.kb.opl.FormYM3812;
import mdplayer.form.kb.opl.FormYMF262;
import mdplayer.form.kb.opl.FormYMF278B;
import mdplayer.form.kb.opn.FormYM2203;
import mdplayer.form.kb.opn.FormYM2608;
import mdplayer.form.kb.opn.FormYM2610;
import mdplayer.form.kb.opn.FormYM2612;
import mdplayer.form.kb.opn.FormYM2612MIDI;
import mdplayer.form.kb.opx.FormYMF271;
import mdplayer.form.kb.pcm.FormC140;
import mdplayer.form.kb.pcm.FormC352;
import mdplayer.form.kb.pcm.FormGA20;
import mdplayer.form.kb.pcm.FormK053260;
import mdplayer.form.kb.pcm.FormK054539;
import mdplayer.form.kb.pcm.FormMegaCD;
import mdplayer.form.kb.pcm.FormMultiPCM;
import mdplayer.form.kb.pcm.FormOKIM6258;
import mdplayer.form.kb.pcm.FormOKIM6295;
import mdplayer.form.kb.pcm.FormQSound;
import mdplayer.form.kb.pcm.FormRf5c68;
import mdplayer.form.kb.pcm.FormSegaPCM;
import mdplayer.form.kb.psg.FormAY8910;
import mdplayer.form.kb.psg.FormSN76489;
import mdplayer.form.kb.wf.FormHuC6280;
import mdplayer.form.kb.wf.FormK051649;
import mdplayer.format.FileFormat;
import mdplayer.format.M3UFileFormat;
import mdplayer.format.ZIPFileFormat;
import mdplayer.plugin.BasePlugin;
import mdplayer.plugin.VGMPlugin;
import mdsound.Instrument;
import mdsound.np.chip.NesN106;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.archive.Archives;
import vavi.util.compat.Tuple;
import vavi.util.compat.Tuple4;
import vavi.util.event.GenericEvent;

import static java.lang.System.getLogger;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


public class FormMain extends JFrame {

    private static final Logger logger = getLogger(FormMain.class.getName());

    static final ResourceBundle rb = ResourceBundle.getBundle("mdplayer/form/sys/frmMain");
    private static final ResourceBundle rb2 = ResourceBundle.getBundle("mdplayer/properties/resources");

    public final ChipLEDs chipLED = new ChipLEDs();
    public final ChipLEDs chipLED_old = new ChipLEDs();

    YM2612MIDI ym2612Midi = new YM2612MIDI(null, null); // TODO

    static final Point empty = new Point(0, 0);

    private BufferedImage pbRf5c164Screen;
    private DoubleBuffer screen;
    private int pWidth = 0;
    private int pHeight = 0;

    private FormInfo frmInfo = null;
    private FormPlayList frmPlayList = null;
    private boolean faderMasterHover = false;
    private boolean faderMasterDrag = false;
    private int faderMasterVal = 0;
    private boolean faderTimeLineHover = false;
    private boolean faderTimeLineDrag = false;
    private int faderTimeLineVal = 0;
    private int visVolumeMaster = 0;

    private static final int[] masterVolTbl = {
            -192, -188, -184, -180, -176, -172, -169, -165, -161, -158,
            -154, -150, -146, -142, -139, -135, -131, -128, -123, -120,
            -116, -112, -108, -104, -101, -97, -93, -89, -86, -82,
            -78, -74, -70, -66, -62, -58, -54, -50, -46, -42,
            -38, -34, -30, -26, -22, -18, -15, -11, -7, -3,
            0, 2, 5, 9, 13, 17, 20
    };
//    private frmVSTeffectList frmVSTeffectList = null;

    private final FormMegaCD[] frmMCD = {null, null};
    private final FormRf5c68[] frmRf5c68 = {null, null};
    private final FormC140[] frmC140 = {null, null};
    private final FormPPZ8[] frmPPZ8 = {null, null};
    private final FormS5B[] frmS5B = {null, null};
    private final FormDMG[] frmDMG = {null, null};
    private final FormYMZ280B[] frmYMZ280B = {null, null};
    private final FormC352[] frmC352 = {null, null};
    private final FormMultiPCM[] frmMultiPCM = {null, null};
    private final FormGA20[] frmGA20 = {null, null};
    private final FormK053260[] frmK053260 = {null, null};
    private final FormK054539[] frmK054539 = {null, null};
    private final FormQSound[] frmQSound = {null, null};
    private final FormYM2608[] frmYM2608 = {null, null};
    private final FormYM2151[] frmYM2151 = {null, null};
    private final FormYM2203[] frmYM2203 = {null, null};
    private final FormYM2610[] frmYM2610 = {null, null};
    private final FormYM2612[] frmYM2612 = {null, null};
    private final FormYM3526[] frmYM3526 = {null, null};
    private final FormY8950[] frmY8950 = {null, null};
    private final FormYM3812[] frmYM3812 = {null, null};
    private final FormOKIM6258[] frmOKIM6258 = {null, null};
    private final FormOKIM6295[] frmOKIM6295 = {null, null};
    private final FormSN76489[] frmSN76489 = {null, null};
    private final FormSegaPCM[] frmSegaPCM = {null, null};
    private final FormAY8910[] frmAY8910 = {null, null};
    private final FormHuC6280[] frmHuC6280 = {null, null};
    private final FormK051649[] frmK051649 = {null, null};
    private final FormYM2413[] frmYM2413 = {null, null};
    private final FormYMF262[] frmYMF262 = {null, null};
    private final FormYMF271[] frmYMF271 = {null, null};
    private final FormYMF278B[] frmYMF278B = {null, null};
    private final FormMIDI[] frmMIDI = {null, null};
    private FormYM2612MIDI frmYM2612MIDI = null;
    private FormMixer2 frmMixer2 = null;
    private final FormNESDMC[] frmNESDMC = {null, null};
    private final FormFDS[] frmFDS = {null, null};
    private final FormMMC5[] frmMMC5 = {null, null};
    private final FormVRC6[] frmVRC6 = {null, null};
    private final FormVRC7[] frmVRC7 = {null, null};
    private final FormN106[] frmN106 = {null, null};
    private FormRegTest frmRegTest;
    private FormVisWave frmVisWave;

    private final List<JFrame[]> lstForm = new ArrayList<>();

    public MDChipParams oldParam = new MDChipParams();
    private final MDChipParams newParam = new MDChipParams();

    private final int[] oldButton = new int[18];
    private final int[] newButton = new int[18];
    private final int[] oldButtonMode = new int[18];
    private final int[] newButtonMode = new int[18];

    private boolean isRunning = false;
    private boolean stopped = false;

    private boolean isInitialOpenFolder = true;

    public Setting setting = Setting.load();

    /** the designer's captions, converted from frmMain.resx */
    private static final ResourceBundle resources = ResourceBundle.getBundle("mdplayer/form/sys/frmMain", Locale.getDefault());
    public final TonePallet tonePallet = TonePallet.load(null);

    private int frameSizeW = 0;
    private int frameSizeH = 0;

    private Transmitter midiin = null;
    private static final boolean forcedExit = false;
    private YM2612MIDI ym2612MIDI;
    private boolean flgReinit = false;
    public boolean reqAllScreenInit = true;

    private static final String[] modeTip = {
            "Mode\nNow:Step\nNext:Random",
            "Mode\nNow:Random\nNext:Loop",
            "Mode\nNow:Loop\nNext:LoopOne",
            "Mode\nNow:LoopOne\nNext:Step",
    };

    private static final String[] zoomTip = {
            "Zoom\nNow:x1\nNext:x2",
            "Zoom\nNow:x2\nNext:x3",
            "Zoom\nNow:x3\nNext:x4",
            "Zoom\nNow:x4\nNext:x1",
    };

    //private FileSystemWatcher watcher = null;
    private MmfControl mmf = null;
    private long now = 0;
    private String opeFolder = "";
    private final Object remoteLockObj = new Object();
    private boolean remoteBusy = false;
    private final List<String[]> remoteReq = new ArrayList<>();

    public FormMain() {
        logger.log(Level.INFO, "Startup process begins");
        logger.log(Level.INFO, "frmMain<init>:STEP 00");

        initializeComponent();
        FrameBuffer.init();

        lstForm.add(frmMCD);
        lstForm.add(frmC140);
        lstForm.add(frmPPZ8);
        lstForm.add(frmC352);
        lstForm.add(frmY8950);
        lstForm.add(frmYM2608);
        lstForm.add(frmYM2151);
        lstForm.add(frmYM2203);
        lstForm.add(frmYM2413);
        lstForm.add(frmYM2610);
        lstForm.add(frmYM2612);
        lstForm.add(frmYM3526);
        lstForm.add(frmYM3812);
        lstForm.add(frmYMF262);
        lstForm.add(frmYMF278B);
        lstForm.add(frmOKIM6258);
        lstForm.add(frmOKIM6295);
        lstForm.add(frmSN76489);
        lstForm.add(frmSegaPCM);
        lstForm.add(frmAY8910);
        lstForm.add(frmHuC6280);
        lstForm.add(frmK051649);
        lstForm.add(frmMIDI);
        lstForm.add(frmNESDMC);
        lstForm.add(frmFDS);
        lstForm.add(frmMMC5);
        lstForm.add(frmVRC6);
        lstForm.add(frmVRC7);

        logger.log(Level.INFO, "frmMain<init>:STEP 01");

        // Only if arguments are specified, does a process check, and if the same application as itself is running,
        // passes the arguments to it and terminates it.
//        if (Common.getCommandLineArgs().length > 1) {
//            Process prc = GetPreviousProcess();
//            if (prc != null) {
//                sendString(prc.MainWindowHandle, Environment.GetCommandLineArgs()[1]);
//                forcedExit = true;
//                try {
//                    this.setVisible(false);
//                } catch (Exception ignored) {
//                }
//                return;
//            }
//        }

        logger.log(Level.INFO, "frmMain<init>:STEP 02");

//        pbScreen.AllowDrop = true;

        logger.log(Level.INFO, "frmMain<init>:STEP 03");
        if (setting == null) {
            logger.log(Level.ERROR, "frmMain<init>:setting instanceof null");
        } else {
//            if ((Control.ModifierKeys & Keys.Shift) == Keys.Shift) {
//                int res = JOptionPane.showConfirmDialog(this,
//                        "Do you want to initialize window position information?",
//                        "MDPlayer",
//                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//                if (res == JOptionPane.OK_OPTION) {
//                    ClearWindowPos();
//                }
//            }
        }

        logger.log(Level.INFO, "Audio initialization process begins at startup");

        // Warm up the Java Sound system on a background thread to prevent latency when the first song starts
        new Thread(() -> {
            try {
                javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(44100, 16, 2, true, false);
                javax.sound.sampled.SourceDataLine line = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.close();
            } catch (Exception ignored) {
            }
        }, "mdplayer-audio-warmup").start();

//        ym2612MIDI = new mdplayer.YM2612MIDI(audio.plugin.chipRegister.plugin(MidiPlugin.class).mds, newParam);
//        ym2612MIDI.fadeout = this::fadeout;
//        ym2612MIDI.next = this::next;
//        ym2612MIDI.ff = this::ff;
//        ym2612MIDI.pause = this::pause;
//        ym2612MIDI.play = this::play;
//        ym2612MIDI.prev = this::prev;
//        ym2612MIDI.slow = this::slow;
//        ym2612MIDI.stop = this::stop;

        logger.log(Level.INFO, "Audio initialization process completed at startup");

        // the chips and the drivers tell the view what they are doing; this is the view listening
        audio.addGenericListener(this::viewEventHappened);

        startMIDIInMonitoring();

        logger.log(Level.INFO, "frmMain<init>:STEP 04");

        setVisible(true);

        // Swing fires windowActivated before windowOpened, and again on every focus gain, so the
        // load/shown sequence is driven from here instead: shown starts the render loop, and it
        // must not run before load has created the child windows it needs.
        frmMain_Load(null);
        frmMain_Shown(null);
    }

    private void clearWindowPos() {
        setting.setLocation(new Setting.Location());
    }

    /** A chip's panel: how to open it and how to close it. */
    private record ChipPanel(Class<? extends Chip> chip, BiConsumer<Integer, Boolean> open, IntConsumer close) {

    }

    /**
     * Every chip that has a panel of its own. Built lazily: the open and close methods capture
     * {@code this}, and this list is what {@link #autoOpenPanels} walks.
     */
    private List<ChipPanel> chipPanels() {
        return List.of(
                new ChipPanel(Ay8910Chip.class, this::openFormAY8910, this::CloseFormAY8910),
                new ChipPanel(C140Chip.class, this::openFormC140, this::closeFormC140),
                new ChipPanel(C352Chip.class, this::OpenFormC352, this::CloseFormC352),
                new ChipPanel(DmgChip.class, this::OpenFormDMG, this::CloseFormDMG),
                new ChipPanel(HuC6280Chip.class, this::OpenFormHuC6280, this::CloseFormHuC6280),
                new ChipPanel(K051649Chip.class, this::OpenFormK051649, this::CloseFormK051649),
                new ChipPanel(Rf5C164Chip.class, this::OpenFormMegaCD, this::CloseFormMegaCD),
                new ChipPanel(Rf5C68Chip.class, this::OpenFormRf5c68, this::CloseFormRf5c68),
                new ChipPanel(MultiPcmChip.class, this::OpenFormMultiPCM, this::CloseFormMultiPCM),
                new ChipPanel(Ga20Chip.class, this::OpenFormGA20, this::closeFormGA20),
                new ChipPanel(K053260Chip.class, this::openFormK053260, this::CloseFormK053260),
                new ChipPanel(K054539Chip.class, this::OpenFormK054539, this::CloseFormK054539),
                new ChipPanel(OkiM6258Chip.class, this::OpenFormOKIM6258, this::CloseFormOKIM6258),
                new ChipPanel(OkiM6295Chip.class, this::OpenFormOKIM6295, this::closeFormOKIM6295),
                new ChipPanel(Ppz8Chip.class, this::openFormPPZ8, this::closeFormPPZ8),
                new ChipPanel(QSoundChip.class, this::OpenFormQSound, this::CloseFormQSound),
                new ChipPanel(SegaPcmChip.class, this::OpenFormSegaPCM, this::CloseFormSegaPCM),
                new ChipPanel(Sn76489Chip.class, this::openFormSN76489, this::CloseFormSN76489),
                new ChipPanel(Y8950Chip.class, this::OpenFormY8950, this::closeFormY8950),
                new ChipPanel(Ym2151Chip.class, this::OpenFormYM2151, this::closeFormYM2151),
                new ChipPanel(Ym2203Chip.class, this::OpenFormYM2203, this::CloseFormYM2203),
                new ChipPanel(Ym2413Chip.class, this::OpenFormYM2413, this::CloseFormYM2413),
                new ChipPanel(Ym2608Chip.class, this::OpenFormYM2608, this::CloseFormYM2608),
                new ChipPanel(Ym2610Chip.class, this::OpenFormYM2610, this::CloseFormYM2610),
                new ChipPanel(Ym2612Chip.class, this::openFormYM2612, this::closeFormYM2612),
                new ChipPanel(Ym3526Chip.class, this::OpenFormYM3526, this::CloseFormYM3526),
                new ChipPanel(Ym3812Chip.class, this::openFormYM3812, this::CloseFormYM3812),
                new ChipPanel(YmF262Chip.class, this::openFormYMF262, this::CloseFormYMF262),
                new ChipPanel(YmF271Chip.class, this::OpenFormYMF271, this::CloseFormYMF271),
                new ChipPanel(YmF278BChip.class, this::OpenFormYMF278B, this::CloseFormYMF278B),
                new ChipPanel(YmZ280BChip.class, this::OpenFormYMZ280B, this::CloseFormYMZ280B),
                new ChipPanel(NesChip.class, this::openFormNESDMC, this::closeFormNESDMC),
                new ChipPanel(NpNesChip.FdsChip.class, this::openFormFDS, this::closeFormFDS),
                new ChipPanel(NpNesChip.Mmc5Chip.class, this::openFormMMC5, this::closeFormMMC5),
                new ChipPanel(NpNesChip.N163Chip.class, this::openFormN106, this::closeFormN106),
                new ChipPanel(NpNesChip.Vrc6Chip.class, this::openFormVRC6, this::closeFormVRC6),
                new ChipPanel(NpNesChip.Vrc7Chip.class, this::openFormVRC7, this::closeFormVRC7)
        );
    }

    /**
     * Is this chip one of the ones the song being played is made of? A chip has a panel whatever
     * the song, but only the chips the song actually drives have an instrument behind them.
     */
    private boolean songUses(Class<? extends Chip> chip, int chipId) {
        Chip c = audio.plugin.chipRegister.chip(chip);
        if (c == null) return false;

        for (Class<? extends Instrument> instrument : c.implementations()) {
            if (audio.plugin.mds.inst(instrument, chipId) != null) return true;
        }
        return false;
    }

    /** Shows a panel for each chip the song uses, and hides the rest. */
    private void autoOpenPanels() {
        for (ChipPanel panel : chipPanels()) {
            for (int chipId = 0; chipId < 2; chipId++) {
                if (songUses(panel.chip(), chipId)) {
                    panel.open().accept(chipId, true);
                } else {
                    panel.close().accept(chipId);
                }
            }
        }
    }

    /**
     * What the chips and the drivers have to say, as they play.
     * <p>
     * This runs on the audio thread, so it only records what happened — the screen loop is what
     * draws it.
     *
     * @see mdplayer.driver.BaseDriver#fireEventHappened
     */
    private void viewEventHappened(GenericEvent ev) {
        Object[] args = ev.getArguments();

        switch (ev.getName()) {
            case "led.on", "led.set" -> {
                int chipId = args != null && args.length > 0 && args[0] instanceof Number n ? n.intValue() : 0;
                chipLED.on(ev.getSource(), chipId);
            }
            case "led.reset" -> chipLED.clear();
            case "wave.buffer" -> {
                if (args == null || args.length < 2
                        || !(args[0] instanceof Number left) || !(args[1] instanceof Number right)) {
                    return;
                }

                visVolumeMaster = Math.max(Math.abs(left.intValue()), Math.abs(right.intValue()));

                if (frmVisWave != null) {
                    frmVisWave.push(left.shortValue(), right.shortValue());
                }
                if (frmMixer2 != null && !frmMixer2.isClosed) {
                    // the mixer's master meter is the loudest of what just came out
                    frmMixer2.visVolume.put("master", visVolumeMaster);
                }
            }
            default -> {
            }
        }
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            frmMain_FormClosed(e);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            frmMain_FormClosing(e);
        }

    };

    private void frmMain_Load(WindowEvent ev) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::SystemEvents_SessionEnding));

        logger.log(Level.INFO, "frmMain_Load:STEP 05");

        String testX = System.getProperty("mdplayer.test.x");
        String testY = System.getProperty("mdplayer.test.y");
        if (testX != null && testY != null) {
            this.setLocation(Integer.parseInt(testX), Integer.parseInt(testY));
        } else if (!setting.getLocation().getPMain().equals(empty)) {
            this.setLocation(setting.getLocation().getPMain());
        }

        // Creating a DoubleBuffer Object

        pbRf5c164Screen = new BufferedImage(320, 72, BufferedImage.TYPE_INT_ARGB);

        logger.log(Level.INFO, "frmMain_Load:STEP 06");

        screen = new DoubleBuffer(pbScreen, Common.getImage("planeMain"), 1);
        screen.setting = setting;
        //oldParam = new MDChipParams();
        //newParam = new MDChipParams();
        reqAllScreenInit = true;

        logger.log(Level.INFO, "frmMain_Load:STEP 07");

        pWidth = pbScreen.getWidth();
        pHeight = pbScreen.getHeight();

        frmPlayList = new FormPlayList(this);
        frmPlayList.setVisible(true);
        frmPlayList.setVisible(false);
        frmPlayList.setOpacity(1.0f);
        //frmPlayList.setLocation(new Point(this.getLocation().x + 328, this.getLocation().y + 264));
        frmPlayList.refresh();

//        frmVSTeffectList = new frmVSTeffectList(this, setting);
//        frmVSTeffectList.setVisible(true);
//        frmVSTeffectList.setVisible(false);
//        frmVSTeffectList.setOpacity(1.0f);
//        //frmVSTeffectList.setLocation(new Point(this.getLocation().x + 328, this.getLocation().y + 264));
//        frmVSTeffectList.repaint();

        if (setting.getLocation().getOPlayList()) dispPlayList();
        if (setting.getLocation().getOInfo()) openInfo();
        if (setting.getLocation().getOMixer()) openMixer();
        if (setting.getLocation().getOpenYm2612MIDI()) openMIDIKeyboard();
        if (setting.getLocation().getOpenVisWave()) openFormVisWave();

        for (int chipId = 0; chipId < 2; chipId++) {
            if (setting.getLocation().getOpenAY8910()[chipId]) openFormAY8910(chipId, false);
            if (setting.getLocation().getOpenC140()[chipId]) openFormC140(chipId, false);
            if (setting.getLocation().getOpenPPZ8()[chipId]) openFormPPZ8(chipId, false);
            if (setting.getLocation().getOpenS5B()[chipId]) OpenFormS5B(chipId, false);
            if (setting.getLocation().getOpenDMG()[chipId]) OpenFormDMG(chipId, false);
            if (setting.getLocation().getOpenYMZ280B()[chipId]) OpenFormYMZ280B(chipId, false);
            if (setting.getLocation().getOpenC352()[chipId]) OpenFormC352(chipId, false);
            if (setting.getLocation().getOpenMultiPCM()[chipId]) OpenFormMultiPCM(chipId, false);
            if (setting.getLocation().getOpenGA20()[chipId]) OpenFormGA20(chipId, false);
            if (setting.getLocation().getOpenK053260()[chipId]) openFormK053260(chipId, false);
            if (setting.getLocation().getOpenK054539()[chipId]) OpenFormK054539(chipId, false);
            if (setting.getLocation().getOpenQSound()[chipId]) OpenFormQSound(chipId, false);
            if (setting.getLocation().getOpenHuC6280()[chipId]) OpenFormHuC6280(chipId, false);
            if (setting.getLocation().getOpenK051649()[chipId]) OpenFormK051649(chipId, false);
            if (setting.getLocation().getOpenMIDI()[chipId]) OpenFormMIDI(chipId, false);
            if (setting.getLocation().getOpenNESDMC()[chipId]) openFormNESDMC(chipId, false);
            if (setting.getLocation().getOpenFDS()[chipId]) openFormFDS(chipId, false);
            if (setting.getLocation().getOpenMMC5()[chipId]) openFormMMC5(chipId, false);
            if (setting.getLocation().getOpenOKIM6258()[chipId]) OpenFormOKIM6258(chipId, false);
            if (setting.getLocation().getOpenOKIM6295()[chipId]) OpenFormOKIM6295(chipId, false);
            if (setting.getLocation().getOpenRf5c164()[chipId]) OpenFormMegaCD(chipId, false);
            if (setting.getLocation().getOpenRf5c68()[chipId]) OpenFormRf5c68(chipId, false);
            if (setting.getLocation().getOpenSN76489()[chipId]) openFormSN76489(chipId, false);
            if (setting.getLocation().getOpenSegaPCM()[chipId]) OpenFormSegaPCM(chipId, false);
            if (setting.getLocation().getOpenYm2151()[chipId]) OpenFormYM2151(chipId, false);
            if (setting.getLocation().getOpenYm2203()[chipId]) OpenFormYM2203(chipId, false);
            if (setting.getLocation().getOpenYm2413()[chipId]) OpenFormYM2413(chipId, false);
            if (setting.getLocation().getOpenYm2608()[chipId]) OpenFormYM2608(chipId, false);
            if (setting.getLocation().getOpenYm2610()[chipId]) OpenFormYM2610(chipId, false);
            if (setting.getLocation().getOpenYm2612()[chipId]) openFormYM2612(chipId, false);
            if (setting.getLocation().getOpenYm3526()[chipId]) OpenFormYM3526(chipId, false);
            if (setting.getLocation().getOpenY8950()[chipId]) OpenFormY8950(chipId, false);
            if (setting.getLocation().getOpenYm3812()[chipId]) openFormYM3812(chipId, false);
            if (setting.getLocation().getOpenYmf262()[chipId]) openFormYMF262(chipId, false);
            if (setting.getLocation().getOpenYMF271()[chipId]) OpenFormYMF271(chipId, false);
            if (setting.getLocation().getOpenYmf278b()[chipId]) OpenFormYMF278B(chipId, false);
            if (setting.getLocation().getOpenVrc6()[chipId]) openFormVRC6(chipId, false);
            if (setting.getLocation().getOpenVrc7()[chipId]) openFormVRC7(chipId, false);
            if (setting.getLocation().getOpenRegTest()[chipId]) openFormRegTest(chipId, null, false);
            if (setting.getLocation().getOpenN106()[chipId]) openFormN106(chipId, false);
        }

        logger.log(Level.INFO, "frmMain_Load:STEP 08");

        frameSizeW = this.getWidth() - this.getSize().width;
        frameSizeH = this.getHeight() - this.getSize().height;

        changeZoom();
        opeButtonMode.setToolTipText(modeTip[newButtonMode[9]]);
        lstOpeButtonControl = new JButton[] {
                opeButtonSetting,
                opeButtonStop,
                opeButtonPause,
                opeButtonFadeout,
                opeButtonPrevious,
                opeButtonSlow,
                opeButtonPlay,
                opeButtonFast,
                opeButtonNext,
                opeButtonMode,
                opeButtonOpen,
                opeButtonPlayList,
                opeButtonInformation,
                opeButtonMixer,
                opeButtonKBD,
//                opeButtonVST,
                opeButtonMIDIKBD,
                opeButtonZoom,
                opeButtonMode,
                opeButtonMode,
                opeButtonMode
        };

        logger.log(Level.INFO, "frmMain_Load:STEP 09");

        // Operation: Clear folder
        //opeFolder = mdplayer.Common.GetOperationFolder(true);
        //startWatch(opeFolder);
        mmf = new MmfControl(false, "MDPlayer", 1024 * 4);
    }

//    private void startWatch(String opeFolder) {
//        if (watcher != null) return;
//
//        watcher = new FileSystemWatcher();
//        watcher.Path = Path.GetDirectoryName(opeFolder);
//        watcher.NotifyFilter = (
//            NotifyFilters.LastAccess
//            | NotifyFilters.LastWrite
//            | NotifyFilters.FileName
//            | NotifyFilters.DirectoryName
//            | NotifyFilters.CreationTime
//            | NotifyFilters.Attributes
//            );
//        watcher.Filter = ""; //  Path.getFileName(opeFolder);
//        watcher.SynchronizingObject = this;
//
//        watcher.Changed += new FileSystemEventHandler(watcher_Changed);
//        watcher.Created += new FileSystemEventHandler(watcher_Changed);
//
//        watcher.EnableRaisingEvents = true;
//    }
//
//    private void stopWatch() {
//        watcher.EnableRaisingEvents = false;
//        watcher.dispose();
//        watcher = null;
//    }

    private void watcher_Changed(WatchEvent<?> e) {
        String trgFile = Path.of(opeFolder, "ope.txt").toString();

        synchronized (remoteLockObj) {
            if (remoteBusy) {
                try {
                    Files.delete(Path.of(trgFile));
                } catch (Exception deleteEx) {
                    logger.log(Level.ERROR, deleteEx.getMessage(), deleteEx);
                }
                return;
            }
            remoteBusy = true;
        }

        try {
            WatchEvent.Kind<?> kind = e.kind();
            if (kind == ENTRY_MODIFY || kind == ENTRY_CREATE) {

                long n = Instant.now().toEpochMilli() / 1_000_000L;
                if (now == n) {
                    try {
                        Files.delete(Path.of(trgFile));
                    } catch (Exception deleteEx) {
                        logger.log(Level.ERROR, deleteEx.getMessage(), deleteEx);
                    }
                    return;
                }
                now = n;

                if (!Files.exists(Path.of(trgFile))) return;
                List<String> lins = null;
                int retry = 30;
                while (retry > 0) {
                    try {
                        lins = Files.readAllLines(Paths.get(trgFile));
                        retry = 0;
                    } catch (IOException e1) {
                        logger.log(Level.WARNING, e);
                        Thread.sleep(100);
                        retry--;
                    }
                }

                try {
                    Files.delete(Path.of(trgFile));
                } catch (Exception deleteEx) {
                    logger.log(Level.ERROR, deleteEx.getMessage(), deleteEx);
                }

                remoteReq.add(lins.toArray(String[]::new));
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        } finally {
            synchronized (remoteLockObj) {
                remoteBusy = false;
            }
        }
    }

    private void remote(String line) {
        try {
            int n = Math.min(
                    line.indexOf(' ') == -1 ? Integer.MAX_VALUE : line.indexOf(' '),
                    line.indexOf('\t') == -1 ? Integer.MAX_VALUE : line.indexOf('\t')
            );
            String command = line;
            String optionLine = "";
            if (n != Integer.MAX_VALUE) {
                command = line.substring(0, n + 1).toUpperCase().trim();
                optionLine = line.substring(n).trim();
            }

            switch (command) {
                case "PLAY":
                    if (!optionLine.isEmpty()) {
                        if (optionLine.charAt(0) == '\"' && optionLine.charAt(optionLine.length() - 1) == '\"') {
                            optionLine = optionLine.substring(1, optionLine.length() - 2);
                        }
                        addFileAndPlay(new String[] {optionLine});
                    } else
                        tsmiPlay_Click(null);
                    break;
                case "STOP":
                    tsmiStop_Click(null);
                    break;
                case "NEXT":
                    tsmiNext_Click(null);
                    break;
                case "PREV":
                    opeButtonPrevious_Click(null);
                    break;
                case "FADEOUT":
                    tsmiFadeOut_Click(null);
                    break;
                case "FAST":
                    tsmiFf_Click(null);
                    break;
                case "SLOW":
                    tsmiSlow_Click(null);
                    break;
                case "PAUSE":
                    tsmiPause_Click(null);
                    break;
                case "CLOSE":
                    setVisible(false);
                    break;
                case "LOOP":
                    tsmiPlayMode_Click(null);
                    break;
                case "MIXER":
                    tsmiOpenMixer_Click(null);
                    break;
                case "INFO":
                    tsmiOpenInfo_Click(null);
                    break;
                case "SPLAY":

                    String lin = optionLine.trim();
                    String mName = lin.substring(0, lin.indexOf(" "));
                    lin = lin.substring(lin.indexOf(" ")).trim();
                    int count = Integer.parseInt(lin.substring(0, lin.indexOf(" ")));
                    lin = lin.substring(lin.indexOf(" ")).trim();
                    String path = lin.trim();
                    MmfControl mml2vgmMmf = new MmfControl(true, mName, count);
                    byte[] buf = mml2vgmMmf.getBytes();

                    bufferPlay(buf, path);

                    break;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void SystemEvents_SessionEnding() {
        this.setVisible(false);
    }

    private void changeZoom() {
        opeButtonZoom.setToolTipText(zoomTip[setting.getOther().getZoom() - 1]);

        int zoom = setting.getOther().getZoom();
        int w = Common.getImage("planeMain").getWidth() * zoom;
        int h = Common.getImage("planeMain").getHeight() * zoom;

        // the skin fills the client area; size the content pane to it and let pack() add the window
        // chrome. (The old code added frameSizeW/H, the WinForms "Width - ClientSize" chrome, but in
        // Swing getWidth() == getSize().width so that was always zero and the skin got clipped by the
        // title bar.)
        pbScreen.setBounds(0, 0, w, h);
        getContentPane().setPreferredSize(new Dimension(w, h));

        componentListener.componentResized(null);
        RelocateOpeButton(zoom);
        pack();

        if (frmMCD[0] != null && !frmMCD[0].isClosed) {
            tsmiPRF5C164_Click(null);
            tsmiPRF5C164_Click(null);
        }

        if (frmRf5c68[0] != null && !frmRf5c68[0].isClosed) {
            tsmiPRF5C68_Click(null);
            tsmiPRF5C68_Click(null);
        }

        if (frmRegTest != null && !frmRegTest.isClosed) {
            closeFormRegTest(0);
            openFormRegTest(0, null, false);
        }

        if (frmC140[0] != null && !frmC140[0].isClosed) {
            tsmiPC140_Click(null);
            tsmiPC140_Click(null);
        }

        if (frmS5B[0] != null && !frmS5B[0].isClosed) {
            tsmiPS5B_Click(null);
            tsmiPS5B_Click(null);
        }

        if (frmDMG[0] != null && !frmDMG[0].isClosed) {
            tsmiPDMG_Click(null);
            tsmiPDMG_Click(null);
        }

        if (frmPPZ8[0] != null && !frmPPZ8[0].isClosed) {
            tsmiPPPZ8_Click(null);
            tsmiPPPZ8_Click(null);
        }

        if (frmYMZ280B[0] != null && !frmYMZ280B[0].isClosed) {
            tsmiYMZ280B_Click(null);
            tsmiYMZ280B_Click(null);
        }

        if (frmC352[0] != null && !frmC352[0].isClosed) {
            tsmiPC352_Click(null);
            tsmiPC352_Click(null);
        }

        if (frmQSound[0] != null && !frmQSound[0].isClosed) {
            tsmiPQSound_Click(null);
            tsmiPQSound_Click(null);
        }

        if (frmYM2608[0] != null && !frmYM2608[0].isClosed) {
            tsmiPOPNA_Click(null);
            tsmiPOPNA_Click(null);
        }

        if (frmYM2151[0] != null && !frmYM2151[0].isClosed) {
            tsmiPOPM_Click(null);
            tsmiPOPM_Click(null);
        }

        if (frmYM2203[0] != null && !frmYM2203[0].isClosed) {
            tsmiPOPN_Click(null);
            tsmiPOPN_Click(null);
        }

        if (frmYM2413[0] != null && !frmYM2413[0].isClosed) {
            tsmiPOPLL_Click(null);
            tsmiPOPLL_Click(null);
        }

        if (frmYM2610[0] != null && !frmYM2610[0].isClosed) {
            tsmiPOPNB_Click(null);
            tsmiPOPNB_Click(null);
        }

        if (frmYM2612[0] != null && !frmYM2612[0].isClosed) {
            tsmiPOPN2_Click(null);
            tsmiPOPN2_Click(null);
        }

        if (frmYM3526[0] != null && !frmYM3526[0].isClosed) {
            tsmiPOPL_Click(null);
            tsmiPOPL_Click(null);
        }

        if (frmY8950[0] != null && !frmY8950[0].isClosed) {
            tsmiPY8950_Click(null);
            tsmiPY8950_Click(null);
        }

        if (frmYM3812[0] != null && !frmYM3812[0].isClosed) {
            tsmiPOPL2_Click(null);
            tsmiPOPL2_Click(null);
        }

        if (frmYMF262[0] != null && !frmYMF262[0].isClosed) {
            tsmiPOPL3_Click(null);
            tsmiPOPL3_Click(null);
        }

        if (frmYMF271[0] != null && !frmYMF271[0].isClosed) {
            tsmiPOPX_Click(null);
            tsmiPOPX_Click(null);
        }

        if (frmYMF278B[0] != null && !frmYMF278B[0].isClosed) {
            tsmiPOPL4_Click(null);
            tsmiPOPL4_Click(null);
        }

        if (frmOKIM6258[0] != null && !frmOKIM6258[0].isClosed) {
            tsmiPOKIM6258_Click(null);
            tsmiPOKIM6258_Click(null);
        }

        if (frmOKIM6295[0] != null && !frmOKIM6295[0].isClosed) {
            tsmiPOKIM6295_Click(null);
            tsmiPOKIM6295_Click(null);
        }

        if (frmSN76489[0] != null && !frmSN76489[0].isClosed) {
            tsmiPDCSG_Click(null);
            tsmiPDCSG_Click(null);
        }

        if (frmSegaPCM[0] != null && !frmSegaPCM[0].isClosed) {
            tsmiPSegaPCM_Click(null);
            tsmiPSegaPCM_Click(null);
        }

        if (frmAY8910[0] != null && !frmAY8910[0].isClosed) {
            tsmiPAY8910_Click(null);
            tsmiPAY8910_Click(null);
        }

        if (frmHuC6280[0] != null && !frmHuC6280[0].isClosed) {
            tsmiPHuC6280_Click(null);
            tsmiPHuC6280_Click(null);
        }

        if (frmK051649[0] != null && !frmK051649[0].isClosed) {
            tsmiPK051649_Click(null);
            tsmiPK051649_Click(null);
        }


        if (frmMCD[1] != null && !frmMCD[1].isClosed) {
            tsmiSRF5C164_Click(null);
            tsmiSRF5C164_Click(null);
        }

        if (frmRf5c68[1] != null && !frmRf5c68[1].isClosed) {
            tsmiSRF5C68_Click(null);
            tsmiSRF5C68_Click(null);
        }

        if (frmC140[1] != null && !frmC140[1].isClosed) {
            tsmiSC140_Click(null);
            tsmiSC140_Click(null);
        }

        if (frmS5B[1] != null && !frmS5B[1].isClosed) {
            tsmiSS5B_Click(null);
            tsmiSS5B_Click(null);
        }

        if (frmDMG[1] != null && !frmDMG[1].isClosed) {
            tsmiSDMG_Click(null);
            tsmiSDMG_Click(null);
        }

        if (frmPPZ8[1] != null && !frmPPZ8[1].isClosed) {
            tsmiSPPZ8_Click(null);
            tsmiSPPZ8_Click(null);
        }

        if (frmYMZ280B[1] != null && !frmYMZ280B[1].isClosed) {
            tsmiSYMZ280B_Click(null);
            tsmiSYMZ280B_Click(null);
        }

        if (frmC352[1] != null && !frmC352[1].isClosed) {
            tsmiSC352_Click(null);
            tsmiSC352_Click(null);
        }

        if (frmYM2608[1] != null && !frmYM2608[1].isClosed) {
            tsmiSOPNA_Click(null);
            tsmiSOPNA_Click(null);
        }

        if (frmYM2151[1] != null && !frmYM2151[1].isClosed) {
            tsmiSOPM_Click(null);
            tsmiSOPM_Click(null);
        }

        if (frmYM2203[1] != null && !frmYM2203[1].isClosed) {
            tsmiSOPN_Click(null);
            tsmiSOPN_Click(null);
        }

        if (frmYM3526[1] != null && !frmYM3526[1].isClosed) {
            tsmiSOPL_Click(null);
            tsmiSOPL_Click(null);
        }

        if (frmY8950[1] != null && !frmY8950[1].isClosed) {
            tsmiSY8950_Click(null);
            tsmiSY8950_Click(null);
        }

        if (frmYM3812[1] != null && !frmYM3812[1].isClosed) {
            tsmiSOPL2_Click(null);
            tsmiSOPL2_Click(null);
        }

        if (frmYM2413[1] != null && !frmYM2413[1].isClosed) {
            tsmiSOPLL_Click(null);
            tsmiSOPLL_Click(null);
        }

        if (frmYM2610[1] != null && !frmYM2610[1].isClosed) {
            tsmiSOPNB_Click(null);
            tsmiSOPNB_Click(null);
        }

        if (frmYM2612[1] != null && !frmYM2612[1].isClosed) {
            tsmiSOPN2_Click(null);
            tsmiSOPN2_Click(null);
        }

        if (frmYMF262[1] != null && !frmYMF262[1].isClosed) {
            tsmiSOPL3_Click(null);
            tsmiSOPL3_Click(null);
        }

        if (frmYMF271[1] != null && !frmYMF271[1].isClosed) {
            tsmiSOPX_Click(null);
            tsmiSOPX_Click(null);
        }

        if (frmYMF278B[1] != null && !frmYMF278B[1].isClosed) {
            tsmiSOPL4_Click(null);
            tsmiSOPL4_Click(null);
        }

        if (frmOKIM6258[1] != null && !frmOKIM6258[1].isClosed) {
            tsmiSOKIM6258_Click(null);
            tsmiSOKIM6258_Click(null);
        }

        if (frmOKIM6295[1] != null && !frmOKIM6295[1].isClosed) {
            tsmiSOKIM6295_Click(null);
            tsmiSOKIM6295_Click(null);
        }

        if (frmSN76489[1] != null && !frmSN76489[1].isClosed) {
            tsmiSDCSG_Click(null);
            tsmiSDCSG_Click(null);
        }

        if (frmSegaPCM[1] != null && !frmSegaPCM[1].isClosed) {
            tsmiSSegaPCM_Click(null);
            tsmiSSegaPCM_Click(null);
        }

        if (frmAY8910[1] != null && !frmAY8910[1].isClosed) {
            tsmiSAY8910_Click(null);
            tsmiSAY8910_Click(null);
        }

        if (frmHuC6280[1] != null && !frmHuC6280[1].isClosed) {
            tsmiSHuC6280_Click(null);
            tsmiSHuC6280_Click(null);
        }

        if (frmK051649[1] != null && !frmK051649[1].isClosed) {
            tsmiSK051649_Click(null);
            tsmiSK051649_Click(null);
        }

        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) {
            openMIDIKeyboard();
            openMIDIKeyboard();
        }

        if (frmMIDI[0] != null && !frmMIDI[0].isClosed) {
            OpenFormMIDI(0, false);
            OpenFormMIDI(0, false);
        }

        if (frmMIDI[1] != null && !frmMIDI[1].isClosed) {
            OpenFormMIDI(1, false);
            OpenFormMIDI(1, false);
        }

        if (frmVRC6[0] != null && !frmVRC6[0].isClosed) {
            openFormVRC6(0, false);
            openFormVRC6(0, false);
        }

        if (frmVRC6[1] != null && !frmVRC6[1].isClosed) {
            openFormVRC6(1, false);
            openFormVRC6(1, false);
        }

        if (frmVRC7[0] != null && !frmVRC7[0].isClosed) {
            openFormVRC7(0, false);
            openFormVRC7(0, false);
        }

        if (frmVRC7[1] != null && !frmVRC7[1].isClosed) {
            openFormVRC7(1, false);
            openFormVRC7(1, false);
        }

        if (frmNESDMC[0] != null && !frmNESDMC[0].isClosed) {
            openFormNESDMC(0, false);
            openFormNESDMC(0, false);
        }

        if (frmNESDMC[1] != null && !frmNESDMC[1].isClosed) {
            openFormNESDMC(1, false);
            openFormNESDMC(1, false);
        }

        if (frmN106[0] != null && !frmN106[0].isClosed) {
            openFormN106(0, false);
            openFormN106(0, false);
        }

        if (frmN106[1] != null && !frmN106[1].isClosed) {
            openFormN106(1, false);
            openFormN106(1, false);
        }

        if (frmMixer2 != null && !frmMixer2.isClosed) {
            openMixer();
            openMixer();
        }

    }

    private void frmMain_Shown(WindowEvent ev) {
        logger.log(Level.INFO, "frmMain_Shown:STEP 09");

        Thread trd = new Thread(this::screenMainLoop);
        trd.setPriority(Thread.MIN_PRIORITY);
        trd.start();

        new Thread(() -> {
            String[] args = Common.getCommandLineArgs();

            if (args.length < 1 || args[0].isBlank()) {
                return;
            }

            logger.log(Level.INFO, "frmMain_Shown:STEP 10");

            String fileName = args[0];

            try {
                // Do the slow file check / metadata reading in the background thread first
                FileFormat format = FileFormat.getFileFormat(fileName);
                format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(fileName)))), null);

                // Now update the UI and playlist on the EDT (block background thread while EDT updates)
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        frmPlayList.stop();

                        PlayList pl = frmPlayList.getPlayList();
                        if (pl.getMusics().isEmpty() || !Objects.equals(pl.getMusics().getLast().fileName, fileName)) {
                            pl.addFile(fileName);
                        }
                    } catch (Exception ex) {
                        logger.log(Level.ERROR, ex.getMessage(), ex);
                    }
                });

                // Call loadAndPlay in the background thread (takes 2 seconds, but does NOT block EDT)
                if (!loadAndPlay(0, 0, fileName, "")) {
                    SwingUtilities.invokeLater(() -> {
                        frmPlayList.stop();
                        OpeManager.requestToAudio(new Request(enmRequest.Stop, null, null));
                    });
                    return;
                }

                // Finalize play state on the EDT
                SwingUtilities.invokeLater(() -> {
                    frmPlayList.setStart(-1);
                    oldParam = new MDChipParams();
                    frmPlayList.play();
                });

            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(FormMain.this, "Failed to read file."));
            }

            logger.log(Level.INFO, "frmMain_Shown:STEP 11");
            logger.log(Level.INFO, "Startup process complete");
        }, "mdplayer-startup-loader").start();
    }

    private final ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            // Reallocate when resizing
//            if (screen != null) screen.setVisible(false);

            screen = new DoubleBuffer(pbScreen, Common.getImage("planeMain"), setting.getOther().getZoom());
            screen.setting = setting;
            reqAllScreenInit = true;
            //screen.screenInitAll();
        }
    };

    private void frmMain_FormClosing(WindowEvent e) {
        if (forcedExit) return;

        logger.log(Level.ERROR, "Termination process begins");
        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 00");

        frmPlayList.stop();
        frmPlayList.save();

        tonePallet.save(null);

        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 01");

        StopMIDIInMonitoring();
        Request req = new Request(enmRequest.Die, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {  // No callbacks for suicide requests
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }

        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 02");

        isRunning = false;
        while (!stopped) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
//            Application.DoEvents();
        }

        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 03");

        if (ym2612MIDI != null)
            ym2612MIDI.close();

        // release
        screen.close();

        setting.getLocation().setOInfo(false);
        setting.getLocation().setOPlayList(false);
        setting.getLocation().setOMixer(false);
        setting.getLocation().setOpenYm2612MIDI(false);
        setting.getLocation().setOpenVisWave(false);
        for (int chipId = 0; chipId < 2; chipId++) {
            setting.getLocation().getOpenAY8910()[chipId] = false;
            setting.getLocation().getOpenC140()[chipId] = false;
            setting.getLocation().getOpenPPZ8()[chipId] = false;
            setting.getLocation().getOpenS5B()[chipId] = false;
            setting.getLocation().getOpenDMG()[chipId] = false;
            setting.getLocation().getOpenYMZ280B()[chipId] = false;
            setting.getLocation().getOpenC352()[chipId] = false;
            setting.getLocation().getOpenGA20()[chipId] = false;
            setting.getLocation().getOpenK053260()[chipId] = false;
            setting.getLocation().getOpenK054539()[chipId] = false;
            setting.getLocation().getOpenQSound()[chipId] = false;
            setting.getLocation().getOpenHuC6280()[chipId] = false;
            setting.getLocation().getOpenK051649()[chipId] = false;
            setting.getLocation().getOpenMIDI()[chipId] = false;
            setting.getLocation().getOpenNESDMC()[chipId] = false;
            setting.getLocation().getOpenFDS()[chipId] = false;
            setting.getLocation().getOpenMMC5()[chipId] = false;
            setting.getLocation().getOpenVrc6()[chipId] = false;
            setting.getLocation().getOpenVrc7()[chipId] = false;
            setting.getLocation().getOpenN106()[chipId] = false;
            setting.getLocation().getOpenOKIM6258()[chipId] = false;
            setting.getLocation().getOpenOKIM6295()[chipId] = false;
            setting.getLocation().getOpenRf5c164()[chipId] = false;
            setting.getLocation().getOpenRf5c68()[chipId] = false;
            setting.getLocation().getOpenSegaPCM()[chipId] = false;
            setting.getLocation().getOpenSN76489()[chipId] = false;
            setting.getLocation().getOpenYm2151()[chipId] = false;
            setting.getLocation().getOpenYm2203()[chipId] = false;
            setting.getLocation().getOpenYm2413()[chipId] = false;
            setting.getLocation().getOpenYm2608()[chipId] = false;
            setting.getLocation().getOpenYm2610()[chipId] = false;
            setting.getLocation().getOpenYm2612()[chipId] = false;
            setting.getLocation().getOpenYm3526()[chipId] = false;
            setting.getLocation().getOpenY8950()[chipId] = false;
            setting.getLocation().getOpenYm3812()[chipId] = false;
            setting.getLocation().getOpenYmf262()[chipId] = false;
            setting.getLocation().getOpenYMF271()[chipId] = false;
            setting.getLocation().getOpenYmf278b()[chipId] = false;
            setting.getLocation().getOpenRegTest()[chipId] = false;
        }

        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 04");

        if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
            setting.getLocation().setPMain(getLocation());
        } else {
            setting.getLocation().setPMain(getBounds().getLocation());
        }
        if (frmPlayList != null && !frmPlayList.isClosed) {
            setting.getLocation().setPPlayList(frmPlayList.getLocation());
            setting.getLocation().setPPlayListWH(new Dimension(frmPlayList.getWidth(), frmPlayList.getHeight()));
            frmPlayList.setVisible(false);
            setting.getLocation().setOPlayList(true);
        }
        if (frmInfo != null && !frmInfo.isClosed) {
            setting.getLocation().setPInfo(frmInfo.getLocation());
            frmInfo.setVisible(false);
            setting.getLocation().setOInfo(true);
        }
        if (frmMixer2 != null && !frmMixer2.isClosed) {
            setting.getLocation().setPosMixer(frmMixer2.getLocation());
            frmMixer2.setVisible(false);
            setting.getLocation().setOMixer(true);
        }
        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) {
            setting.getLocation().setPosYm2612MIDI(frmYM2612MIDI.getLocation());
            frmYM2612MIDI.setVisible(false);
            setting.getLocation().setOpenYm2612MIDI(true);
        }

        for (int chipId = 0; chipId < 2; chipId++) {
            if (frmAY8910[chipId] != null && !frmAY8910[chipId].isClosed) {
                setting.getLocation().getPosAY8910()[chipId] = frmAY8910[chipId].getLocation();
                frmAY8910[chipId].setVisible(false);
                setting.getLocation().getOpenAY8910()[chipId] = true;
            }
            if (frmC140[chipId] != null && !frmC140[chipId].isClosed) {
                setting.getLocation().getPosC140()[chipId] = frmC140[chipId].getLocation();
                frmC140[chipId].setVisible(false);
                setting.getLocation().getOpenC140()[chipId] = true;
            }
            if (frmPPZ8[chipId] != null && !frmPPZ8[chipId].isClosed) {
                setting.getLocation().getPosPPZ8()[chipId] = frmPPZ8[chipId].getLocation();
                frmPPZ8[chipId].setVisible(false);
                setting.getLocation().getOpenPPZ8()[chipId] = true;
            }
            if (frmS5B[chipId] != null && !frmS5B[chipId].isClosed) {
                setting.getLocation().getPosS5B()[chipId] = frmS5B[chipId].getLocation();
                frmS5B[chipId].setVisible(false);
                setting.getLocation().getOpenS5B()[chipId] = true;
            }
            if (frmDMG[chipId] != null && !frmDMG[chipId].isClosed) {
                setting.getLocation().getPosDMG()[chipId] = frmDMG[chipId].getLocation();
                frmDMG[chipId].setVisible(false);
                setting.getLocation().getOpenDMG()[chipId] = true;
            }
            if (frmYMZ280B[chipId] != null && !frmYMZ280B[chipId].isClosed) {
                setting.getLocation().getPosYMZ280B()[chipId] = frmYMZ280B[chipId].getLocation();
                frmYMZ280B[chipId].setVisible(false);
                setting.getLocation().getOpenYMZ280B()[chipId] = true;
            }
            if (frmC352[chipId] != null && !frmC352[chipId].isClosed) {
                setting.getLocation().getPosC352()[chipId] = frmC352[chipId].getLocation();
                frmC352[chipId].setVisible(false);
                setting.getLocation().getOpenC352()[chipId] = true;
            }
            if (frmGA20[chipId] != null && !frmGA20[chipId].isClosed) {
                setting.getLocation().getPosGA20()[chipId] = frmGA20[chipId].getLocation();
                frmGA20[chipId].setVisible(false);
                setting.getLocation().getOpenGA20()[chipId] = true;
            }
            if (frmK053260[chipId] != null && !frmK053260[chipId].isClosed) {
                setting.getLocation().getPosK053260()[chipId] = frmK053260[chipId].getLocation();
                frmK053260[chipId].setVisible(false);
                setting.getLocation().getOpenK053260()[chipId] = true;
            }
            if (frmK054539[chipId] != null && !frmK054539[chipId].isClosed) {
                setting.getLocation().getPosK054539()[chipId] = frmK054539[chipId].getLocation();
                frmK054539[chipId].setVisible(false);
                setting.getLocation().getOpenK054539()[chipId] = true;
            }
            if (frmQSound[chipId] != null && !frmQSound[chipId].isClosed) {
                setting.getLocation().getPosQSound()[chipId] = frmQSound[chipId].getLocation();
                frmQSound[chipId].setVisible(false);
                setting.getLocation().getOpenQSound()[chipId] = true;
            }
            if (frmFDS[chipId] != null && !frmFDS[chipId].isClosed) {
                setting.getLocation().getPosFDS()[chipId] = frmFDS[chipId].getLocation();
                frmFDS[chipId].setVisible(false);
                setting.getLocation().getOpenFDS()[chipId] = true;
            }
            if (frmHuC6280[chipId] != null && !frmHuC6280[chipId].isClosed) {
                setting.getLocation().getPosHuC6280()[chipId] = frmHuC6280[chipId].getLocation();
                frmHuC6280[chipId].setVisible(false);
                setting.getLocation().getOpenHuC6280()[chipId] = true;
            }
            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) {
                setting.getLocation().getPosK051649()[chipId] = frmK051649[chipId].getLocation();
                frmK051649[chipId].setVisible(false);
                setting.getLocation().getOpenK051649()[chipId] = true;
            }
            if (frmMCD[chipId] != null && !frmMCD[chipId].isClosed) {
                setting.getLocation().getPosRf5c164()[chipId] = frmMCD[chipId].getLocation();
                frmMCD[chipId].setVisible(false);
                setting.getLocation().getOpenRf5c164()[chipId] = true;
            }
            if (frmRf5c68[chipId] != null && !frmRf5c68[chipId].isClosed) {
                setting.getLocation().getPosRf5c68()[chipId] = frmRf5c68[chipId].getLocation();
                frmRf5c68[chipId].setVisible(false);
                setting.getLocation().getOpenRf5c68()[chipId] = true;
            }
            if (frmMIDI[chipId] != null && !frmMIDI[chipId].isClosed) {
                setting.getLocation().getPosMIDI()[chipId] = frmMIDI[chipId].getLocation();
                frmMIDI[chipId].setVisible(false);
                setting.getLocation().getOpenMIDI()[chipId] = true;
            }
            if (frmMMC5[chipId] != null && !frmMMC5[chipId].isClosed) {
                setting.getLocation().getPosMMC5()[chipId] = frmMMC5[chipId].getLocation();
                frmMMC5[chipId].setVisible(false);
                setting.getLocation().getOpenMMC5()[chipId] = true;
            }
            if (frmVRC6[chipId] != null && !frmVRC6[chipId].isClosed) {
                setting.getLocation().getPosVrc6()[chipId] = frmVRC6[chipId].getLocation();
                frmVRC6[chipId].setVisible(false);
                setting.getLocation().getOpenVrc6()[chipId] = true;
            }
            if (frmVRC7[chipId] != null && !frmVRC7[chipId].isClosed) {
                setting.getLocation().getPosVrc7()[chipId] = frmVRC7[chipId].getLocation();
                frmVRC7[chipId].setVisible(false);
                setting.getLocation().getOpenVrc7()[chipId] = true;
            }
            if (frmN106[chipId] != null && !frmN106[chipId].isClosed) {
                setting.getLocation().getPosN106()[chipId] = frmN106[chipId].getLocation();
                frmN106[chipId].setVisible(false);
                setting.getLocation().getOpenN106()[chipId] = true;
            }
            if (frmNESDMC[chipId] != null && !frmNESDMC[chipId].isClosed) {
                setting.getLocation().getPosNESDMC()[chipId] = frmNESDMC[chipId].getLocation();
                frmNESDMC[chipId].setVisible(false);
                setting.getLocation().getOpenNESDMC()[chipId] = true;
            }
            if (frmOKIM6258[chipId] != null && !frmOKIM6258[chipId].isClosed) {
                setting.getLocation().getPosOKIM6258()[chipId] = frmOKIM6258[chipId].getLocation();
                frmOKIM6258[chipId].setVisible(false);
                setting.getLocation().getOpenOKIM6258()[chipId] = true;
            }
            if (frmOKIM6295[chipId] != null && !frmOKIM6295[chipId].isClosed) {
                setting.getLocation().getPosOKIM6295()[chipId] = frmOKIM6295[chipId].getLocation();
                frmOKIM6295[chipId].setVisible(false);
                setting.getLocation().getOpenOKIM6295()[chipId] = true;
            }
            if (frmSegaPCM[chipId] != null && !frmSegaPCM[chipId].isClosed) {
                setting.getLocation().getPosSegaPCM()[chipId] = frmSegaPCM[chipId].getLocation();
                frmSegaPCM[chipId].setVisible(false);
                setting.getLocation().getOpenSegaPCM()[chipId] = true;
            }
            if (frmSN76489[chipId] != null && !frmSN76489[chipId].isClosed) {
                setting.getLocation().getPosSN76489()[chipId] = frmSN76489[chipId].getLocation();
                frmSN76489[chipId].setVisible(false);
                setting.getLocation().getOpenSN76489()[chipId] = true;
            }
            if (frmYM2151[chipId] != null && !frmYM2151[chipId].isClosed) {
                setting.getLocation().getPosYm2151()[chipId] = frmYM2151[chipId].getLocation();
                frmYM2151[chipId].setVisible(false);
                setting.getLocation().getOpenYm2151()[chipId] = true;
            }
            if (frmYM2203[chipId] != null && !frmYM2203[chipId].isClosed) {
                setting.getLocation().getPosYm2203()[chipId] = frmYM2203[chipId].getLocation();
                frmYM2203[chipId].setVisible(false);
                setting.getLocation().getOpenYm2203()[chipId] = true;
            }
            if (frmYM2413[chipId] != null && !frmYM2413[chipId].isClosed) {
                setting.getLocation().getPosYm2413()[chipId] = frmYM2413[chipId].getLocation();
                frmYM2413[chipId].setVisible(false);
                setting.getLocation().getOpenYm2413()[chipId] = true;
            }
            if (frmYM2608[chipId] != null && !frmYM2608[chipId].isClosed) {
                setting.getLocation().getPosYm2608()[chipId] = frmYM2608[chipId].getLocation();
                frmYM2608[chipId].setVisible(false);
                setting.getLocation().getOpenYm2608()[chipId] = true;
            }
            if (frmYM2610[chipId] != null && !frmYM2610[chipId].isClosed) {
                setting.getLocation().getPosYm2610()[chipId] = frmYM2610[chipId].getLocation();
                frmYM2610[chipId].setVisible(false);
                setting.getLocation().getOpenYm2610()[chipId] = true;
            }
            if (frmYM2612[chipId] != null && !frmYM2612[chipId].isClosed) {
                setting.getLocation().getPosYm2612()[chipId] = frmYM2612[chipId].getLocation();
                frmYM2612[chipId].setVisible(false);
                setting.getLocation().getOpenYm2612()[chipId] = true;
            }
            if (frmYM3526[chipId] != null && !frmYM3526[chipId].isClosed) {
                setting.getLocation().getPosYm3526()[chipId] = frmYM3526[chipId].getLocation();
                frmYM3526[chipId].setVisible(false);
                setting.getLocation().getOpenYm3526()[chipId] = true;
            }
            if (frmY8950[chipId] != null && !frmY8950[chipId].isClosed) {
                setting.getLocation().getPosY8950()[chipId] = frmY8950[chipId].getLocation();
                frmY8950[chipId].setVisible(false);
                setting.getLocation().getOpenY8950()[chipId] = true;
            }
            if (frmYM3812[chipId] != null && !frmYM3812[chipId].isClosed) {
                setting.getLocation().getPosYm3812()[chipId] = frmYM3812[chipId].getLocation();
                frmYM3812[chipId].setVisible(false);
                setting.getLocation().getOpenYm3812()[chipId] = true;
            }
            if (frmYMF262[chipId] != null && !frmYMF262[chipId].isClosed) {
                setting.getLocation().getPosYmf262()[chipId] = frmYMF262[chipId].getLocation();
                frmYMF262[chipId].setVisible(false);
                setting.getLocation().getOpenYmf262()[chipId] = true;
            }
            if (frmYMF271[chipId] != null && !frmYMF271[chipId].isClosed) {
                setting.getLocation().getPosYMF271()[chipId] = frmYMF271[chipId].getLocation();
                frmYMF271[chipId].setVisible(false);
                setting.getLocation().getOpenYMF271()[chipId] = true;
            }
            if (frmYMF278B[chipId] != null && !frmYMF278B[chipId].isClosed) {
                setting.getLocation().getPosYmf278b()[chipId] = frmYMF278B[chipId].getLocation();
                frmYMF278B[chipId].setVisible(false);
                setting.getLocation().getOpenYmf278b()[chipId] = true;
            }

            if (frmRegTest != null && !frmRegTest.isClosed) {
                setting.getLocation().getPosRegTest()[chipId] = frmRegTest.getLocation();
                frmRegTest.setVisible(false);
                setting.getLocation().getOpenRegTest()[chipId] = true;
            }

            if (frmVisWave != null && !frmVisWave.isClosed) {
                setting.getLocation().setPosVisWave(frmVisWave.getLocation());
                frmVisWave.setVisible(false);
                setting.getLocation().setOpenVisWave(true);
            }
        }

        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 05");

        setting.save();

        logger.log(Level.ERROR, "frmMain_FormClosing:STEP 06");

        mmf.close();

        logger.log(Level.ERROR, "Termination process complete");
    }

    private final MouseMotionListener pbScreen_MouseMove = new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent ev) {
            int px = ev.getX() / setting.getOther().getZoom();
            int py = ev.getY() / setting.getOther().getZoom();

            checkMouseHover(px, py);

            if (py < 9) {
                Arrays.fill(newButton, 0);
                return;
            }

            for (int n = 0; n < newButton.length; n++) {
                //if (px >= 320 - (16 - n) * 16 && px < 320 - (15 - n) * 16) newButton[n] = 1;
                if (px >= n * 16 + 17 && px < n * 16 + 33) newButton[n] = 1;
                else newButton[n] = 0;
            }
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
            int px = ev.getX() / setting.getOther().getZoom();
            int py = ev.getY() / setting.getOther().getZoom();

            checkMouseHover(px, py);

            if (faderMasterDrag) {
                faderMasterVal = Common.range(px - 184, 0, 56);
                if (audio.plugin != null) {
                    audio.plugin.setMasterVolume(true, masterVolTbl[faderMasterVal]);
                }
            }

            if (faderTimeLineDrag) {
                faderTimeLineVal = Common.range(px - 184, 0, 56);
            }
        }
    };

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent ev) {
            showMainPopup(ev);

            int px = ev.getX() / setting.getOther().getZoom();
            int py = ev.getY() / setting.getOther().getZoom();

            checkMouseHover(px, py);

            if (faderMasterHover) {
                faderMasterDrag = true;
                faderMasterVal = Common.range(px - 184, 0, 56);
                if (audio.plugin != null) {
                    audio.plugin.setMasterVolume(true, masterVolTbl[faderMasterVal]);
                }
            }
            if (faderTimeLineHover) {
                faderTimeLineDrag = true;
                faderTimeLineVal = px - 184;
            }
        }

        @Override
        public void mouseReleased(MouseEvent ev) {
            showMainPopup(ev);

            int px = ev.getX() / setting.getOther().getZoom();
            int py = ev.getY() / setting.getOther().getZoom();

            checkMouseHover(px, py);

            if (faderTimeLineDrag) {
                faderTimeLineVal = Common.range(px - 184, 0, 56);
                audio.seek(faderTimeLineVal / 56.0);
            }

            faderMasterDrag = false;
            faderTimeLineDrag = false;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Arrays.fill(newButton, 0);
            faderMasterHover = false;
            faderTimeLineHover = false;
        }

        @Override
        public void mouseClicked(MouseEvent ev) {
        }
    };

    private void checkMouseHover(int px, int py) {
        faderMasterHover = (px >= 184 && px < 243 && py >= 13 && py < 20);
        faderTimeLineHover = (px >= 184 && px < 243 && py >= 29 && py < 36);
    }

    private void showMainPopup(MouseEvent ev) {
        if (ev.isPopupTrigger()) {
            cmsMenu.show(pbScreen, ev.getX(), ev.getY());
        }
    }

    private void tsmiPOPN_Click(ActionEvent ev) {
        OpenFormYM2203(0, false);
    }

    private void tsmiPOPN2_Click(ActionEvent ev) {
        openFormYM2612(0, false);
    }

    private void tsmiPOPNA_Click(ActionEvent ev) {
        OpenFormYM2608(0, false);
    }

    private void tsmiPOPNB_Click(ActionEvent ev) {
        OpenFormYM2610(0, false);
    }

    private void tsmiPOPM_Click(ActionEvent ev) {
        OpenFormYM2151(0, false);
    }

    private void tsmiPDCSG_Click(ActionEvent ev) {
        openFormSN76489(0, false);
    }

    private void tsmiPRF5C164_Click(ActionEvent ev) {
        OpenFormMegaCD(0, false);
    }

    private void tsmiPRF5C68_Click(ActionEvent ev) {
        OpenFormRf5c68(0, false);
    }

    private void tsmiPPWM_Click(ActionEvent ev) {

    }

    private void tsmiPOKIM6258_Click(ActionEvent ev) {
        OpenFormOKIM6258(0, false);
    }

    private void tsmiPOKIM6295_Click(ActionEvent ev) {
        OpenFormOKIM6295(0, false);
    }

    private void tsmiPC140_Click(ActionEvent ev) {
        openFormC140(0, false);
    }

    private void tsmiPPPZ8_Click(ActionEvent ev) {
        openFormPPZ8(0, false);
    }

    private void tsmiSPPZ8_Click(ActionEvent ev) {
        openFormPPZ8(1, false);
    }

    private void tsmiPS5B_Click(ActionEvent ev) {
        OpenFormS5B(0, false);
    }

    private void tsmiSS5B_Click(ActionEvent ev) {
        OpenFormS5B(1, false);
    }

    private void tsmiPDMG_Click(ActionEvent ev) {
        OpenFormDMG(0, false);
    }

    private void tsmiSDMG_Click(ActionEvent ev) {
        OpenFormDMG(1, false);
    }

    private void tsmiPC352_Click(ActionEvent ev) {
        OpenFormC352(0, false);
    }

    private void tsmiPMultiPCM_Click(ActionEvent ev) {
        OpenFormMultiPCM(0, false);
    }

    private void tsmiPQSound_Click(ActionEvent ev) {
        OpenFormQSound(0, false);
    }

    private void tsmiPSegaPCM_Click(ActionEvent ev) {
        OpenFormSegaPCM(0, false);
    }

    private void tsmiPAY8910_Click(ActionEvent ev) {
        openFormAY8910(0, false);
    }

    private void tsmiPOPLL_Click(ActionEvent ev) {
        OpenFormYM2413(0, false);
    }

    private void tsmiPOPL_Click(ActionEvent ev) {
        OpenFormYM3526(0, false);
    }

    private void tsmiPY8950_Click(ActionEvent ev) {
        OpenFormY8950(0, false);
    }

    private void tsmiPOPL2_Click(ActionEvent ev) {
        openFormYM3812(0, false);
    }

    private void tsmiPOPL3_Click(ActionEvent ev) {
        openFormYMF262(0, false);
    }

    private void tsmiPOPL4_Click(ActionEvent ev) {
        OpenFormYMF278B(0, false);
    }

    private void tsmiPOPX_Click(ActionEvent ev) {
        OpenFormYMF271(0, false);
    }

    private void tsmiPHuC6280_Click(ActionEvent ev) {
        OpenFormHuC6280(0, false);
    }

    private void tsmiPK051649_Click(ActionEvent ev) {
        OpenFormK051649(0, false);
    }

    private void tsmiPMMC5_Click(ActionEvent ev) {
        openFormMMC5(0, false);
    }

    private void tsmiSMMC5_Click(ActionEvent ev) {
        openFormMMC5(1, false);
    }

    private void tsmiSOPN_Click(ActionEvent ev) {
        OpenFormYM2203(1, false);
    }

    private void tsmiSOPN2_Click(ActionEvent ev) {
        openFormYM2612(1, false);
    }

    private void tsmiSOPNA_Click(ActionEvent ev) {
        OpenFormYM2608(1, false);
    }

    private void tsmiSOPNB_Click(ActionEvent ev) {
        OpenFormYM2610(1, false);
    }

    private void tsmiSOPM_Click(ActionEvent ev) {
        OpenFormYM2151(1, false);
    }

    private void tsmiSDCSG_Click(ActionEvent ev) {
        openFormSN76489(1, false);
    }

    private void tsmiSRF5C164_Click(ActionEvent ev) {
        OpenFormMegaCD(1, false);
    }

    private void tsmiSRF5C68_Click(ActionEvent ev) {
        OpenFormRf5c68(1, false);
    }

    private void tsmiSPWM_Click(ActionEvent ev) {
    }

    private void tsmiSOKIM6258_Click(ActionEvent ev) {
        OpenFormOKIM6258(1, false);
    }

    private void tsmiSOKIM6295_Click(ActionEvent ev) {
        OpenFormOKIM6295(1, false);
    }

    private void tsmiSC140_Click(ActionEvent ev) {
        openFormC140(1, false);
    }

    private void tsmiYMZ280B_Click(ActionEvent ev) {
        OpenFormYMZ280B(0, false);
    }

    private void tsmiSYMZ280B_Click(ActionEvent ev) {
        OpenFormYMZ280B(1, false);
    }

    private void tsmiSC352_Click(ActionEvent ev) {
        OpenFormC352(1, false);
    }

    private void tsmiSMultiPCM_Click(ActionEvent ev) {
        OpenFormMultiPCM(1, false);
    }

    private void tsmiSQSound_Click(ActionEvent ev) {
        OpenFormQSound(1, false);
    }

    private void tsmiSSegaPCM_Click(ActionEvent ev) {
        OpenFormSegaPCM(1, false);
    }

    private void tsmiSAY8910_Click(ActionEvent ev) {
        openFormAY8910(1, false);
    }

    private void tsmiSOPLL_Click(ActionEvent ev) {
        OpenFormYM2413(1, false);
    }

    private void tsmiSOPL_Click(ActionEvent ev) {
        OpenFormYM3526(1, false);
    }

    private void tsmiSY8950_Click(ActionEvent ev) {
        OpenFormY8950(1, false);
    }

    private void tsmiSOPL2_Click(ActionEvent ev) {
        openFormYM3812(1, false);
    }

    private void tsmiSOPL3_Click(ActionEvent ev) {
        openFormYMF262(1, false);
    }

    private void tsmiSOPL4_Click(ActionEvent ev) {
        OpenFormYMF278B(1, false);
    }

    private void tsmiSOPX_Click(ActionEvent ev) {
        OpenFormYMF271(1, false);
    }

    private void tsmiSHuC6280_Click(ActionEvent ev) {
        OpenFormHuC6280(1, false);
    }

    private void tsmiSK051649_Click(ActionEvent ev) {
        OpenFormK051649(1, false);
    }

    private void tsmiPMIDI_Click(ActionEvent ev) {
        OpenFormMIDI(0, false);
    }

    private void tsmiSMIDI_Click(ActionEvent ev) {
        OpenFormMIDI(1, false);
    }

    private void tsmiPNESDMC_Click(ActionEvent ev) {
        openFormNESDMC(0, false);
    }

    private void tsmiSNESDMC_Click(ActionEvent ev) {
        openFormNESDMC(1, false);
    }

    private void tsmiPFDS_Click(ActionEvent ev) {
        openFormFDS(0, false);
    }

    private void tsmiSFDS_Click(ActionEvent ev) {
        openFormFDS(1, false);
    }

    private void tsmiPVRC6_Click(ActionEvent ev) {
        openFormVRC6(0, false);
    }

    private void tsmiSVRC6_Click(ActionEvent ev) {
        openFormVRC6(1, false);
    }

    private void tsmiPVRC7_Click(ActionEvent ev) {
        openFormVRC7(0, false);
    }

    private void tsmiSVRC7_Click(ActionEvent ev) {
        openFormVRC7(1, false);
    }

    private void tsmiPN106_Click(ActionEvent ev) {
        openFormN106(0, false);
    }

    private void tsmiSN106_Click(ActionEvent ev) {
        openFormN106(1, false);
    }

    private void tsmiVisWave_Click(ActionEvent ev) {
        openFormVisWave();
    }

    private void tsmiConsole_Click(ActionEvent ev) {
        openConsole();
    }

    /** Shows what the player is logging. */
    private void openConsole() {
        if (frmConsole != null && !frmConsole.isClosed) {
            frmConsole.setVisible(false);
            frmConsole.dispose();
            frmConsole = null;
            return;
        }

        frmConsole = new FormConsole(this);
        frmConsole.setLocation(this.getLocation().x, this.getLocation().y + 100);
        frmConsole.setVisible(true);
    }


    private void OpenFormMegaCD(int chipId, boolean force /* = false */) {
        if (frmMCD[chipId] != null) {
            if (!force) {
                CloseFormMegaCD(chipId);
                return;
            } else
                return;
        }

        frmMCD[chipId] = new FormMegaCD(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId], audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164_old[chipId]);
        if (setting.getLocation().getPosRf5c164()[chipId].equals(empty)) {
            frmMCD[chipId].x = this.getLocation().x;
            frmMCD[chipId].y = this.getLocation().y + 264;
        } else {
            frmMCD[chipId].x = setting.getLocation().getPosRf5c164()[chipId].x;
            frmMCD[chipId].y = setting.getLocation().getPosRf5c164()[chipId].y;
        }

        frmMCD[chipId].setVisible(true);
        frmMCD[chipId].update();
        frmMCD[chipId].setTitle("RF5C164 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId] = new Rf5C164Chip.Params();

        checkAndSetForm(frmMCD[chipId]);
    }

    private void CloseFormMegaCD(int chipId) {
        if (frmMCD[chipId] == null) return;

        try {
            frmMCD[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);

        }
        try {
            frmMCD[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmMCD[chipId] = null;
    }

    private void OpenFormRf5c68(int chipId, boolean force /* = false */) {
        if (frmRf5c68[chipId] != null) {
            if (!force) {
                CloseFormRf5c68(chipId);
                return;
            } else
                return;
        }

        frmRf5c68[chipId] = new FormRf5c68(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId], audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId]);
        if (setting.getLocation().getPosRf5c68()[chipId].equals(empty)) {
            frmRf5c68[chipId].x = this.getLocation().x;
            frmRf5c68[chipId].y = this.getLocation().y + 264;
        } else {
            frmRf5c68[chipId].x = setting.getLocation().getPosRf5c68()[chipId].x;
            frmRf5c68[chipId].y = setting.getLocation().getPosRf5c68()[chipId].y;
        }

        frmRf5c68[chipId].setVisible(true);
        frmRf5c68[chipId].update();
        frmRf5c68[chipId].setTitle("RF5C68 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId] = new Rf5C68Chip.Params();

        checkAndSetForm(frmRf5c68[chipId]);
    }

    private void CloseFormRf5c68(int chipId) {
        if (frmRf5c68[chipId] == null) return;

        try {
            frmRf5c68[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);

        }
        try {
            frmRf5c68[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmRf5c68[chipId] = null;
    }


    private void OpenFormYMF271(int chipId, boolean force /* = false */) {
        if (frmYMF271[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                CloseFormYMF271(chipId);
                return;
            } else
                return;
        }

        frmYMF271[chipId] = new FormYMF271(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(YmF271Chip.class).ymf271[chipId], audio.plugin.chipRegister.chip(YmF271Chip.class).ymf271_old[chipId]);
        if (setting.getLocation().getPosYMF271()[chipId].equals(empty)) {
            frmYMF271[chipId].x = this.getLocation().x;
            frmYMF271[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMF271[chipId].x = setting.getLocation().getPosYMF271()[chipId].x;
            frmYMF271[chipId].y = setting.getLocation().getPosYMF271()[chipId].y;
        }

        frmYMF271[chipId].setVisible(true);
        frmYMF271[chipId].update();
        frmYMF271[chipId].setTitle("YMF271 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(YmF271Chip.class).ymf271_old[chipId] = new YmF271Chip.Params();

        checkAndSetForm(frmYMF271[chipId]);
    }

    private void CloseFormYMF271(int chipId) {
        if (frmYMF271[chipId] == null) return;

        try {
            frmYMF271[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);

        }
        try {
            frmYMF271[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYMF271[chipId] = null;
    }

    private void OpenFormYM2608(int chipId, boolean force /* = false */) {
        if (frmYM2608[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                CloseFormYM2608(chipId);
                return;
            } else
                return;
        }

        frmYM2608[chipId] = new FormYM2608(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId], audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId]);

        if (setting.getLocation().getPosYm2608()[chipId].equals(empty)) {
            frmYM2608[chipId].x = this.getLocation().x;
            frmYM2608[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2608[chipId].x = setting.getLocation().getPosYm2608()[chipId].x;
            frmYM2608[chipId].y = setting.getLocation().getPosYm2608()[chipId].y;
        }

        frmYM2608[chipId].setVisible(true);
        frmYM2608[chipId].update();
        frmYM2608[chipId].setTitle("YM2608 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId] = new Ym2608Chip.Params();

        checkAndSetForm(frmYM2608[chipId]);
    }

    private void CloseFormYM2608(int chipId) {
        if (frmYM2608[chipId] == null) return;

        try {
            frmYM2608[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM2608[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM2608[chipId] = null;
    }

    private void OpenFormYM2151(int chipId, boolean force /* = false */) {
        if (frmYM2151[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                closeFormYM2151(chipId);
                return;
            } else return;
        }

        frmYM2151[chipId] = new FormYM2151(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId], audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151_old[chipId]);

        if (setting.getLocation().getPosYm2151()[chipId].equals(empty)) {
            frmYM2151[chipId].x = this.getLocation().x;
            frmYM2151[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2151[chipId].x = setting.getLocation().getPosYm2151()[chipId].x;
            frmYM2151[chipId].y = setting.getLocation().getPosYm2151()[chipId].y;
        }

        frmYM2151[chipId].setVisible(true);
        frmYM2151[chipId].update();
        frmYM2151[chipId].setTitle("YM2151 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151_old[chipId] = new Ym2151Chip.Params();

        checkAndSetForm(frmYM2151[chipId]);
    }

    private void closeFormYM2151(int chipId) {
        if (frmYM2151[chipId] == null) return;

        try {
            frmYM2151[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM2151[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM2151[chipId] = null;
    }

    private void openFormC140(int chipId, boolean force /* = false */) {
        if (frmC140[chipId] != null) {
            if (!force) {
                closeFormC140(chipId);
                return;
            } else return;
        }

        frmC140[chipId] = new FormC140(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId], audio.plugin.chipRegister.chip(C140Chip.class).c140_old[chipId]);

        if (setting.getLocation().getPosC140()[chipId].equals(empty)) {
            frmC140[chipId].x = this.getLocation().x;
            frmC140[chipId].y = this.getLocation().y + 264;
        } else {
            frmC140[chipId].x = setting.getLocation().getPosC140()[chipId].x;
            frmC140[chipId].y = setting.getLocation().getPosC140()[chipId].y;
        }

        frmC140[chipId].setVisible(true);
        frmC140[chipId].update();
        frmC140[chipId].setTitle("C140Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(C140Chip.class).c140_old[chipId] = new C140Chip.Params();

        checkAndSetForm(frmC140[chipId]);
    }

    private void closeFormC140(int chipId) {
        if (frmC140[chipId] == null) return;

        try {
            frmC140[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmC140[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmC140[chipId] = null;
    }

    private void openFormPPZ8(int chipId, boolean force /* = false */) {
        if (frmPPZ8[chipId] != null) {
            if (!force) {
                closeFormPPZ8(chipId);
                return;
            } else return;
        }

        frmPPZ8[chipId] = new FormPPZ8(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId], audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8_old[chipId]);

        if (setting.getLocation().getPosPPZ8()[chipId].equals(empty)) {
            frmPPZ8[chipId].x = this.getLocation().x;
            frmPPZ8[chipId].y = this.getLocation().y + 264;
        } else {
            frmPPZ8[chipId].x = setting.getLocation().getPosPPZ8()[chipId].x;
            frmPPZ8[chipId].y = setting.getLocation().getPosPPZ8()[chipId].y;
        }

        frmPPZ8[chipId].setVisible(true);
        frmPPZ8[chipId].update();
        frmPPZ8[chipId].setTitle("Ppz8Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8_old[chipId] = new Ppz8Chip.Params();

        checkAndSetForm(frmPPZ8[chipId]);
    }

    private void closeFormPPZ8(int chipId) {
        if (frmPPZ8[chipId] == null) return;

        try {
            frmPPZ8[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmPPZ8[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmPPZ8[chipId] = null;
    }

    private void OpenFormS5B(int chipId, boolean force /* = false */) {
        if (frmS5B[chipId] != null) {
            if (!force) {
                CloseFormS5B(chipId);
                return;
            } else return;
        }

        frmS5B[chipId] = new FormS5B(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(NpNesChip.Fme7Chip.class).s5b[chipId], audio.plugin.chipRegister.chip(NpNesChip.Fme7Chip.class).s5b_old[chipId]);

        if (setting.getLocation().getPosS5B()[chipId].equals(empty)) {
            frmS5B[chipId].x = this.getLocation().x;
            frmS5B[chipId].y = this.getLocation().y + 264;
        } else {
            frmS5B[chipId].x = setting.getLocation().getPosS5B()[chipId].x;
            frmS5B[chipId].y = setting.getLocation().getPosS5B()[chipId].y;
        }

        frmS5B[chipId].setVisible(true);
        frmS5B[chipId].update();
        frmS5B[chipId].setTitle("S5B (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(NpNesChip.Fme7Chip.class).s5b_old[chipId] = new NpNesChip.Fme7Chip.Params();

        checkAndSetForm(frmS5B[chipId]);
    }

    private void CloseFormS5B(int chipId) {
        if (frmS5B[chipId] == null) return;

        try {
            frmS5B[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmS5B[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmS5B[chipId] = null;
    }

    private void OpenFormDMG(int chipId, boolean force /* = false */) {
        if (frmDMG[chipId] != null) {
            if (!force) {
                CloseFormDMG(chipId);
                return;
            } else return;
        }

        frmDMG[chipId] = new FormDMG(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId], audio.plugin.chipRegister.chip(DmgChip.class).dmg_old[chipId]);

        if (setting.getLocation().getPosDMG()[chipId].equals(empty)) {
            frmDMG[chipId].x = this.getLocation().x;
            frmDMG[chipId].y = this.getLocation().y + 264;
        } else {
            frmDMG[chipId].x = setting.getLocation().getPosDMG()[chipId].x;
            frmDMG[chipId].y = setting.getLocation().getPosDMG()[chipId].y;
        }

        frmDMG[chipId].setVisible(true);
        frmDMG[chipId].update();
        frmDMG[chipId].setTitle("DMG (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(DmgChip.class).dmg_old[chipId] = new DmgChip.Params();

        checkAndSetForm(frmDMG[chipId]);
    }

    private void CloseFormDMG(int chipId) {
        if (frmDMG[chipId] == null) return;

        try {
            frmDMG[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmDMG[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmDMG[chipId] = null;
    }

    private void OpenFormYMZ280B(int chipId, boolean force /* = false */) {
        if (frmYMZ280B[chipId] != null) {
            if (!force) {
                CloseFormYMZ280B(chipId);
                return;
            } else return;
        }

        frmYMZ280B[chipId] = new FormYMZ280B(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(YmZ280BChip.class).ymz280b[chipId], audio.plugin.chipRegister.chip(YmZ280BChip.class).ymz280b_old[chipId]);

        if (setting.getLocation().getPosYMZ280B()[chipId].equals(empty)) {
            frmYMZ280B[chipId].x = this.getLocation().x;
            frmYMZ280B[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMZ280B[chipId].x = setting.getLocation().getPosYMZ280B()[chipId].x;
            frmYMZ280B[chipId].y = setting.getLocation().getPosYMZ280B()[chipId].y;
        }

        frmYMZ280B[chipId].setVisible(true);
        frmYMZ280B[chipId].update();
        frmYMZ280B[chipId].setTitle("YMZ280B (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(YmZ280BChip.class).ymz280b_old[chipId] = new YmZ280BChip.Params();

        checkAndSetForm(frmYMZ280B[chipId]);
    }

    private void CloseFormYMZ280B(int chipId) {
        if (frmYMZ280B[chipId] == null) return;

        try {
            frmYMZ280B[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYMZ280B[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYMZ280B[chipId] = null;
    }

    private void OpenFormC352(int chipId, boolean force /* = false */) {
        if (frmC352[chipId] != null) {
            if (!force) {
                CloseFormC352(chipId);
                return;
            } else return;
        }

        frmC352[chipId] = new FormC352(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId], audio.plugin.chipRegister.chip(C352Chip.class).c352_old[chipId]);

        if (setting.getLocation().getPosC352()[chipId].equals(empty)) {
            frmC352[chipId].x = this.getLocation().x;
            frmC352[chipId].y = this.getLocation().y + 264;
        } else {
            frmC352[chipId].x = setting.getLocation().getPosC352()[chipId].x;
            frmC352[chipId].y = setting.getLocation().getPosC352()[chipId].y;
        }

        frmC352[chipId].setVisible(true);
        frmC352[chipId].update();
        frmC352[chipId].setTitle("C352Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(C352Chip.class).c352_old[chipId] = new C352Chip.Params();

        checkAndSetForm(frmC352[chipId]);
    }

    private void CloseFormC352(int chipId) {
        if (frmC352[chipId] == null) return;

        try {
            frmC352[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmC352[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmC352[chipId] = null;
    }

    private void OpenFormMultiPCM(int chipId, boolean force /* = false */) {
        if (frmMultiPCM[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                CloseFormMultiPCM(chipId);
                return;
            } else return;
        }

        frmMultiPCM[chipId] = new FormMultiPCM(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(MultiPcmChip.class).multiPCM[chipId], audio.plugin.chipRegister.chip(MultiPcmChip.class).multiPCM_old[chipId]);

        if (setting.getLocation().getPosMultiPCM()[chipId].equals(empty)) {
            frmMultiPCM[chipId].x = this.getLocation().x;
            frmMultiPCM[chipId].y = this.getLocation().y + 264;
        } else {
            frmMultiPCM[chipId].x = setting.getLocation().getPosMultiPCM()[chipId].x;
            frmMultiPCM[chipId].y = setting.getLocation().getPosMultiPCM()[chipId].y;
        }

        frmMultiPCM[chipId].setVisible(true);
        frmMultiPCM[chipId].update();
        frmMultiPCM[chipId].setTitle("MultiPCM (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(MultiPcmChip.class).multiPCM_old[chipId] = new MultiPcmChip.Params();

        checkAndSetForm(frmMultiPCM[chipId]);
    }

    private void CloseFormMultiPCM(int chipId) {
        if (frmMultiPCM[chipId] == null) return;

        try {
            frmMultiPCM[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmMultiPCM[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmMultiPCM[chipId] = null;
    }

    private void OpenFormGA20(int chipId, boolean force /* = false */) {
        if (frmGA20[chipId] != null) {
            if (!force) {
                closeFormGA20(chipId);
                return;
            } else return;
        }

        frmGA20[chipId] = new FormGA20(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ga20Chip.class).ga20[chipId], audio.plugin.chipRegister.chip(Ga20Chip.class).ga20_old[chipId]);

        if (setting.getLocation().getPosGA20()[chipId].equals(empty)) {
            frmGA20[chipId].x = this.getLocation().x;
            frmGA20[chipId].y = this.getLocation().y + 264;
        } else {
            frmGA20[chipId].x = setting.getLocation().getPosGA20()[chipId].x;
            frmGA20[chipId].y = setting.getLocation().getPosGA20()[chipId].y;
        }

        frmGA20[chipId].setVisible(true);
        frmGA20[chipId].update();
        frmGA20[chipId].setTitle("GA20 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ga20Chip.class).ga20_old[chipId] = new Ga20Chip.Params();

        checkAndSetForm(frmGA20[chipId]);
    }

    private void closeFormGA20(int chipId) {
        if (frmGA20[chipId] == null) return;

        try {
            frmGA20[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmGA20[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmGA20[chipId] = null;
    }

    private void openFormK053260(int chipId, boolean force /* = false */) {
        if (frmK053260[chipId] != null) {
            if (!force) {
                CloseFormK053260(chipId);
                return;
            } else return;
        }

        frmK053260[chipId] = new FormK053260(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(K053260Chip.class).k053260[chipId], audio.plugin.chipRegister.chip(K053260Chip.class).k053260_old[chipId]);

        if (setting.getLocation().getPosK053260()[chipId].equals(empty)) {
            frmK053260[chipId].x = this.getLocation().x;
            frmK053260[chipId].y = this.getLocation().y + 264;
        } else {
            frmK053260[chipId].x = setting.getLocation().getPosK053260()[chipId].x;
            frmK053260[chipId].y = setting.getLocation().getPosK053260()[chipId].y;
        }

        frmK053260[chipId].setVisible(true);
        frmK053260[chipId].update();
        frmK053260[chipId].setTitle("K053260 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(K053260Chip.class).k053260_old[chipId] = new K053260Chip.Params();

        checkAndSetForm(frmK053260[chipId]);
    }

    private void CloseFormK053260(int chipId) {
        if (frmK053260[chipId] == null) return;

        try {
            frmK053260[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmK053260[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmK053260[chipId] = null;
    }

    private void OpenFormK054539(int chipId, boolean force /* = false */) {
        if (frmK054539[chipId] != null) {
            if (!force) {
                CloseFormK054539(chipId);
                return;
            } else return;
        }

        frmK054539[chipId] = new FormK054539(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(K054539Chip.class).k054539[chipId], audio.plugin.chipRegister.chip(K054539Chip.class).k054539_old[chipId]);

        if (setting.getLocation().getPosK054539()[chipId].equals(empty)) {
            frmK054539[chipId].x = this.getLocation().x;
            frmK054539[chipId].y = this.getLocation().y + 264;
        } else {
            frmK054539[chipId].x = setting.getLocation().getPosK054539()[chipId].x;
            frmK054539[chipId].y = setting.getLocation().getPosK054539()[chipId].y;
        }

        frmK054539[chipId].setVisible(true);
        frmK054539[chipId].update();
        frmK054539[chipId].setTitle("K054539 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(K054539Chip.class).k054539_old[chipId] = new K054539Chip.Params();

        checkAndSetForm(frmK054539[chipId]);
    }

    private void CloseFormK054539(int chipId) {
        if (frmK054539[chipId] == null) return;

        try {
            frmK054539[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmK054539[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmK054539[chipId] = null;
    }

    private void OpenFormQSound(int chipId, boolean force /* = false */) {
        if (frmQSound[chipId] != null) {
            if (!force) {
                CloseFormQSound(chipId);
                return;
            } else return;
        }

        frmQSound[chipId] = new FormQSound(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId], audio.plugin.chipRegister.chip(QSoundChip.class).qSound_old[chipId]);

        if (setting.getLocation().getPosQSound()[chipId].equals(empty)) {
            frmQSound[chipId].x = this.getLocation().x;
            frmQSound[chipId].y = this.getLocation().y + 264;
        } else {
            frmQSound[chipId].x = setting.getLocation().getPosQSound()[chipId].x;
            frmQSound[chipId].y = setting.getLocation().getPosQSound()[chipId].y;
        }

        frmQSound[chipId].setVisible(true);
        frmQSound[chipId].update();
        frmQSound[chipId].setTitle("QSoundInst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(QSoundChip.class).qSound_old[chipId] = new QSoundChip.Params();

        checkAndSetForm(frmQSound[chipId]);
    }

    private void CloseFormQSound(int chipId) {
        if (frmQSound[chipId] == null) return;

        try {
            frmQSound[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmQSound[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmQSound[chipId] = null;
    }

    private void OpenFormYM2203(int chipId, boolean force /* = false */) {
        if (frmYM2203[chipId] != null) {
            if (!force) {
                CloseFormYM2203(chipId);
                return;
            } else return;
        }

        frmYM2203[chipId] = new FormYM2203(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId], audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId]);

        if (setting.getLocation().getPosYm2203()[chipId].equals(empty)) {
            frmYM2203[chipId].x = this.getLocation().x;
            frmYM2203[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2203[chipId].x = setting.getLocation().getPosYm2203()[chipId].x;
            frmYM2203[chipId].y = setting.getLocation().getPosYm2203()[chipId].y;
        }

        frmYM2203[chipId].setVisible(true);
        frmYM2203[chipId].update();
        frmYM2203[chipId].setTitle("YM2203 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId] = new Ym2203Chip.Params();

        checkAndSetForm(frmYM2203[chipId]);
    }

    private void CloseFormYM2203(int chipId) {
        if (frmYM2203[chipId] == null) return;

        try {
            frmYM2203[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM2203[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM2203[chipId] = null;
    }

    private void OpenFormYM2610(int chipId, boolean force /* = false */) {
        if (frmYM2610[chipId] != null) {
            if (!force) {
                CloseFormYM2610(chipId);
                return;
            } else return;
        }

        frmYM2610[chipId] = new FormYM2610(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId], audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId]);

        if (setting.getLocation().getPosYm2610()[chipId].equals(empty)) {
            frmYM2610[chipId].x = this.getLocation().x;
            frmYM2610[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2610[chipId].x = setting.getLocation().getPosYm2610()[chipId].x;
            frmYM2610[chipId].y = setting.getLocation().getPosYm2610()[chipId].y;
        }

        frmYM2610[chipId].setVisible(true);
        frmYM2610[chipId].update();
        frmYM2610[chipId].setTitle("YM2610 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId] = new Ym2610Chip.Params();

        checkAndSetForm(frmYM2610[chipId]);
    }

    private void CloseFormYM2610(int chipId) {
        if (frmYM2610[chipId] == null) return;

        try {
            frmYM2610[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM2610[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM2610[chipId] = null;
    }

    private void openFormYM2612(int chipId, boolean force /* = false */) {
        if (frmYM2612[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                closeFormYM2612(chipId);
                return;
            } else return;
        }

        audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId] = new Ym2612Chip.Params();
        for (int i = 0; i < audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels.length; i++) {
            audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels[i].mask = null;
        }
        frmYM2612[chipId] = new FormYM2612(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId], audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId]);

        if (setting.getLocation().getPosYm2612()[chipId].equals(empty)) {
            frmYM2612[chipId].x = this.getLocation().x;
            frmYM2612[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2612[chipId].x = setting.getLocation().getPosYm2612()[chipId].x;
            frmYM2612[chipId].y = setting.getLocation().getPosYm2612()[chipId].y;
        }

        frmYM2612[chipId].setVisible(true);
        frmYM2612[chipId].update();
        frmYM2612[chipId].setTitle("Ym2612Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmYM2612[chipId]);
    }

    private void closeFormYM2612(int chipId) {
        if (frmYM2612[chipId] == null) return;
        try {
            frmYM2612[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM2612[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM2612[chipId] = null;
    }

    private void OpenFormOKIM6258(int chipId, boolean force /* = false */) {
        if (frmOKIM6258[chipId] != null) {
            if (!force) {
                CloseFormOKIM6258(chipId);
                return;
            } else return;
        }

        frmOKIM6258[chipId] = new FormOKIM6258(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(OkiM6258Chip.class).okim6258[chipId]);

        if (setting.getLocation().getPosOKIM6258()[chipId].equals(empty)) {
            frmOKIM6258[chipId].x = this.getLocation().x;
            frmOKIM6258[chipId].y = this.getLocation().y + 264;
        } else {
            frmOKIM6258[chipId].x = setting.getLocation().getPosOKIM6258()[chipId].x;
            frmOKIM6258[chipId].y = setting.getLocation().getPosOKIM6258()[chipId].y;
        }

        frmOKIM6258[chipId].setVisible(true);
        frmOKIM6258[chipId].update();
        frmOKIM6258[chipId].setTitle("OKIM6258 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmOKIM6258[chipId]);
    }

    private void CloseFormOKIM6258(int chipId) {
        if (frmOKIM6258[chipId] == null) return;

        try {
            frmOKIM6258[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmOKIM6258[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmOKIM6258[chipId] = null;
    }

    private void OpenFormOKIM6295(int chipId, boolean force /* = false */) {
        if (frmOKIM6295[chipId] != null) {
            if (!force) {
                closeFormOKIM6295(chipId);
                return;
            } else return;
        }

        frmOKIM6295[chipId] = new FormOKIM6295(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId], audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295_old[chipId]);

        if (setting.getLocation().getPosOKIM6295()[chipId].equals(empty)) {
            frmOKIM6295[chipId].x = this.getLocation().x;
            frmOKIM6295[chipId].y = this.getLocation().y + 264;
        } else {
            frmOKIM6295[chipId].x = setting.getLocation().getPosOKIM6295()[chipId].x;
            frmOKIM6295[chipId].y = setting.getLocation().getPosOKIM6295()[chipId].y;
        }

        frmOKIM6295[chipId].setVisible(true);
        frmOKIM6295[chipId].update();
        frmOKIM6295[chipId].setTitle("OKIM6295 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmOKIM6295[chipId]);
    }

    private void closeFormOKIM6295(int chipId) {
        if (frmOKIM6295[chipId] == null) return;

        try {
            frmOKIM6295[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmOKIM6295[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmOKIM6295[chipId] = null;
    }

    private void openFormSN76489(int chipId, boolean force /* = false */) {
        if (frmSN76489[chipId] != null) {
            if (!force) {
                CloseFormSN76489(chipId);
                return;
            } else return;
        }

        frmSN76489[chipId] = new FormSN76489(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId], audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489_old[chipId]);

        if (setting.getLocation().getPosSN76489()[chipId].equals(empty)) {
            frmSN76489[chipId].x = this.getLocation().x;
            frmSN76489[chipId].y = this.getLocation().y + 264;
        } else {
            frmSN76489[chipId].x = setting.getLocation().getPosSN76489()[chipId].x;
            frmSN76489[chipId].y = setting.getLocation().getPosSN76489()[chipId].y;
        }

        frmSN76489[chipId].setVisible(true);
        frmSN76489[chipId].update();
        frmSN76489[chipId].setTitle("SN76489 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489_old[chipId] = new Sn76489Chip.Params();

        checkAndSetForm(frmSN76489[chipId]);
    }

    private void CloseFormSN76489(int chipId) {
        if (frmSN76489[chipId] == null) return;

        try {
            frmSN76489[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmSN76489[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmSN76489[chipId] = null;
    }

    private void OpenFormSegaPCM(int chipId, boolean force /* = false */) {
        if (frmSegaPCM[chipId] != null) {
            if (!force) {
                CloseFormSegaPCM(chipId);
                return;
            } else return;
        }

        frmSegaPCM[chipId] = new FormSegaPCM(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId], audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm_old[chipId]);

        if (setting.getLocation().getPosSegaPCM()[chipId].equals(empty)) {
            frmSegaPCM[chipId].x = this.getLocation().x;
            frmSegaPCM[chipId].y = this.getLocation().y + 264;
        } else {
            frmSegaPCM[chipId].x = setting.getLocation().getPosSegaPCM()[chipId].x;
            frmSegaPCM[chipId].y = setting.getLocation().getPosSegaPCM()[chipId].y;
        }

        frmSegaPCM[chipId].setVisible(true);
        frmSegaPCM[chipId].update();
        frmSegaPCM[chipId].setTitle("SegaPCM (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm_old[chipId] = new SegaPcmChip.Params();

        checkAndSetForm(frmSegaPCM[chipId]);
    }

    private void CloseFormSegaPCM(int chipId) {
        if (frmSegaPCM[chipId] == null) return;

        try {
            frmSegaPCM[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmSegaPCM[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmSegaPCM[chipId] = null;
    }

    private void openFormAY8910(int chipId, boolean force /* = false */) {
        if (frmAY8910[chipId] != null) {
            if (!force) {
                CloseFormAY8910(chipId);
                return;
            } else return;
        }

        frmAY8910[chipId] = new FormAY8910(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId], audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910_old[chipId]);

        if (setting.getLocation().getPosAY8910()[chipId].equals(empty)) {
            frmAY8910[chipId].x = this.getLocation().x;
            frmAY8910[chipId].y = this.getLocation().y + 264;
        } else {
            frmAY8910[chipId].x = setting.getLocation().getPosAY8910()[chipId].x;
            frmAY8910[chipId].y = setting.getLocation().getPosAY8910()[chipId].y;
        }

        frmAY8910[chipId].setVisible(true);
        frmAY8910[chipId].update();
        frmAY8910[chipId].setTitle("AY8910 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910_old[chipId] = new Ay8910Chip.Params();

        checkAndSetForm(frmAY8910[chipId]);
    }

    private void CloseFormAY8910(int chipId) {
        if (frmAY8910[chipId] == null) return;

        try {
            frmAY8910[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmAY8910[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmAY8910[chipId] = null;
    }

    private void OpenFormHuC6280(int chipId, boolean force /* = false */) {
        if (frmHuC6280[chipId] != null) {
            if (!force) {
                CloseFormHuC6280(chipId);
                return;
            } else return;
        }

        frmHuC6280[chipId] = new FormHuC6280(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId], audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280_old[chipId]);

        if (setting.getLocation().getPosHuC6280()[chipId].equals(empty)) {
            frmHuC6280[chipId].x = this.getLocation().x;
            frmHuC6280[chipId].y = this.getLocation().y + 264;
        } else {
            frmHuC6280[chipId].x = setting.getLocation().getPosHuC6280()[chipId].x;
            frmHuC6280[chipId].y = setting.getLocation().getPosHuC6280()[chipId].y;
        }

        frmHuC6280[chipId].setVisible(true);
        frmHuC6280[chipId].update();
        frmHuC6280[chipId].setTitle("OotakeHuC6280 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280_old[chipId] = new HuC6280Chip.Params();

        checkAndSetForm(frmHuC6280[chipId]);
    }

    private void CloseFormHuC6280(int chipId) {
        if (frmHuC6280[chipId] == null) return;

        try {
            frmHuC6280[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmHuC6280[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmHuC6280[chipId] = null;
    }

    private void OpenFormK051649(int chipId, boolean force /* = false */) {
        if (frmK051649[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                CloseFormK051649(chipId);
                return;
            } else return;
        }

        frmK051649[chipId] = new FormK051649(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(K051649Chip.class).k051649[chipId]);

        if (setting.getLocation().getPosK051649()[chipId].equals(empty)) {
            frmK051649[chipId].x = this.getLocation().x;
            frmK051649[chipId].y = this.getLocation().y + 264;
        } else {
            frmK051649[chipId].x = setting.getLocation().getPosK051649()[chipId].x;
            frmK051649[chipId].y = setting.getLocation().getPosK051649()[chipId].y;
        }

        frmK051649[chipId].setVisible(true);
        frmK051649[chipId].update();
        frmK051649[chipId].setTitle("K051649Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(K051649Chip.class).k051649_old[chipId] = new K051649Chip.Params();

        checkAndSetForm(frmK051649[chipId]);
    }

    private void CloseFormK051649(int chipId) {
        if (frmK051649[chipId] == null) return;

        try {
            frmK051649[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmK051649[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmK051649[chipId] = null;
    }

    private void OpenFormYM2413(int chipId, boolean force /* = false */) {
        if (frmYM2413[chipId] != null) {
            if (!force) {
                CloseFormYM2413(chipId);
                return;
            } else return;
        }

        frmYM2413[chipId] = new FormYM2413(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId], audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413_old[chipId]);

        if (setting.getLocation().getPosYm2413()[chipId].equals(empty)) {
            frmYM2413[chipId].x = this.getLocation().x;
            frmYM2413[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2413[chipId].x = setting.getLocation().getPosYm2413()[chipId].x;
            frmYM2413[chipId].y = setting.getLocation().getPosYm2413()[chipId].y;
        }

        frmYM2413[chipId].setVisible(true);
        frmYM2413[chipId].update();
        frmYM2413[chipId].setTitle("YM2413/VRC7 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413_old[chipId] = new Ym2413Chip.Params();

        checkAndSetForm(frmYM2413[chipId]);
    }

    private void CloseFormYM2413(int chipId) {
        if (frmYM2413[chipId] == null) return;

        try {
            frmYM2413[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM2413[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM2413[chipId] = null;
    }

    private void OpenFormYM3526(int chipId, boolean force /* = false */) {
        if (frmYM3526[chipId] != null) {
            if (!force) {
                CloseFormYM3526(chipId);
                return;
            } else return;
        }

        frmYM3526[chipId] = new FormYM3526(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId], audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526_old[chipId]);

        if (setting.getLocation().getPosYm3526()[chipId].equals(empty)) {
            frmYM3526[chipId].x = this.getLocation().x;
            frmYM3526[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM3526[chipId].x = setting.getLocation().getPosYm3526()[chipId].x;
            frmYM3526[chipId].y = setting.getLocation().getPosYm3526()[chipId].y;
        }

        frmYM3526[chipId].setVisible(true);
        frmYM3526[chipId].update();
        frmYM3526[chipId].setTitle("YM3526 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526_old[chipId] = new Ym3526Chip.Params();

        checkAndSetForm(frmYM3526[chipId]);
    }

    private void CloseFormYM3526(int chipId) {
        if (frmYM3526[chipId] == null) return;

        try {
            frmYM3526[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM3526[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM3526[chipId] = null;
    }

    private void OpenFormY8950(int chipId, boolean force /* = false */) {
        if (frmY8950[chipId] != null) {
            if (!force) {
                closeFormY8950(chipId);
                return;
            } else return;
        }

        frmY8950[chipId] = new FormY8950(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId]);

        if (setting.getLocation().getPosY8950()[chipId].equals(empty)) {
            frmY8950[chipId].x = this.getLocation().x;
            frmY8950[chipId].y = this.getLocation().y + 264;
        } else {
            frmY8950[chipId].x = setting.getLocation().getPosY8950()[chipId].x;
            frmY8950[chipId].y = setting.getLocation().getPosY8950()[chipId].y;
        }

        frmY8950[chipId].setVisible(true);
        frmY8950[chipId].update();
        frmY8950[chipId].setTitle("Y8950Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Y8950Chip.class).y8950_old[chipId] = new Y8950Chip.Params();

        checkAndSetForm(frmY8950[chipId]);
    }

    private void closeFormY8950(int chipId) {
        if (frmY8950[chipId] == null) return;

        try {
            frmY8950[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmY8950[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmY8950[chipId] = null;
    }

    private void openFormYM3812(int chipId, boolean force /* = false */) {
        if (frmYM3812[chipId] != null) {
            if (!force) {
                CloseFormYM3812(chipId);
                return;
            } else return;
        }

        frmYM3812[chipId] = new FormYM3812(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId], audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812_old[chipId]);

        if (setting.getLocation().getPosYm3812()[chipId].equals(empty)) {
            frmYM3812[chipId].x = this.getLocation().x;
            frmYM3812[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM3812[chipId].x = setting.getLocation().getPosYm3812()[chipId].x;
            frmYM3812[chipId].y = setting.getLocation().getPosYm3812()[chipId].y;
        }

        frmYM3812[chipId].setVisible(true);
        frmYM3812[chipId].update();
        frmYM3812[chipId].setTitle("YM3812 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812_old[chipId] = new Ym3812Chip.Params();

        checkAndSetForm(frmYM3812[chipId]);
    }

    private void CloseFormYM3812(int chipId) {
        if (frmYM3812[chipId] == null) return;

        try {
            frmYM3812[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYM3812[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYM3812[chipId] = null;
    }

    private void openFormYMF262(int chipId, boolean force /* = false */) {
        if (frmYMF262[chipId] != null) {
            if (!force) {
                CloseFormYMF262(chipId);
                return;
            } else return;
        }

        frmYMF262[chipId] = new FormYMF262(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId], audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262_old[chipId]);

        if (setting.getLocation().getPosYmf262()[chipId].equals(empty)) {
            frmYMF262[chipId].x = this.getLocation().x;
            frmYMF262[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMF262[chipId].x = setting.getLocation().getPosYmf262()[chipId].x;
            frmYMF262[chipId].y = setting.getLocation().getPosYmf262()[chipId].y;
        }

        frmYMF262[chipId].setVisible(true);
        frmYMF262[chipId].update();
        frmYMF262[chipId].setTitle("YMF262 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262_old[chipId] = new YmF262Chip.Params();

        checkAndSetForm(frmYMF262[chipId]);
    }

    private void CloseFormYMF262(int chipId) {
        if (frmYMF262[chipId] == null) return;

        try {
            frmYMF262[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYMF262[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYMF262[chipId] = null;
    }

    private void OpenFormYMF278B(int chipId, boolean force /* = false */) {
        if (frmYMF278B[chipId] != null) {
            if (!force) {
                CloseFormYMF278B(chipId);
                return;
            } else return;
        }

        frmYMF278B[chipId] = new FormYMF278B(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId]);

        if (setting.getLocation().getPosYmf278b()[chipId].equals(empty)) {
            frmYMF278B[chipId].x = this.getLocation().x;
            frmYMF278B[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMF278B[chipId].x = setting.getLocation().getPosYmf278b()[chipId].x;
            frmYMF278B[chipId].y = setting.getLocation().getPosYmf278b()[chipId].y;
        }

        frmYMF278B[chipId].setVisible(true);
        frmYMF278B[chipId].update();
        frmYMF278B[chipId].setTitle("YMF278B (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b_old[chipId] = new YmF278BChip.Params();

        checkAndSetForm(frmYMF278B[chipId]);
    }

    private void CloseFormYMF278B(int chipId) {
        if (frmYMF278B[chipId] == null) return;

        try {
            frmYMF278B[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmYMF278B[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmYMF278B[chipId] = null;
    }

    private void OpenFormMIDI(int chipId, boolean force /* = false */) {
        if (frmMIDI[chipId] != null) {
            if (!force) {
                closeFormMIDI(chipId);
                return;
            } else return;
        }

        frmMIDI[chipId] = new FormMIDI(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.plugin(MidiPlugin.class).midi[chipId]);

        if (setting.getLocation().getPosMIDI()[chipId].equals(empty)) {
            frmMIDI[chipId].x = this.getLocation().x;
            frmMIDI[chipId].y = this.getLocation().y + 264;
        } else {
            frmMIDI[chipId].x = setting.getLocation().getPosMIDI()[chipId].x;
            frmMIDI[chipId].y = setting.getLocation().getPosMIDI()[chipId].y;
        }

        frmMIDI[chipId].setVisible(true);
        frmMIDI[chipId].update();
        frmMIDI[chipId].setTitle("MIDI (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.plugin(MidiPlugin.class).midi_old[chipId] = new MIDIParam();

        checkAndSetForm(frmMIDI[chipId]);
    }

    private void closeFormMIDI(int chipId) {
        if (frmMIDI[chipId] == null) return;

        try {
            frmMIDI[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmMIDI[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmMIDI[chipId] = null;
    }

    private void openFormNESDMC(int chipId, boolean force /* = false */) {
        if (frmNESDMC[chipId] != null) {
            if (!force) {
                closeFormNESDMC(chipId);
                return;
            } else return;
        }

        frmNESDMC[chipId] = new FormNESDMC(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId]);

        if (setting.getLocation().getPosNESDMC()[chipId].equals(empty)) {
            frmNESDMC[chipId].x = this.getLocation().x;
            frmNESDMC[chipId].y = this.getLocation().y + 264;
        } else {
            frmNESDMC[chipId].x = setting.getLocation().getPosNESDMC()[chipId].x;
            frmNESDMC[chipId].y = setting.getLocation().getPosNESDMC()[chipId].y;
        }

        frmNESDMC[chipId].setVisible(true);
        frmNESDMC[chipId].update();
        frmNESDMC[chipId].setTitle("NES&DMC (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc_old[chipId] = new NpNesChip.DmcChip.Params();

        checkAndSetForm(frmNESDMC[chipId]);
    }

    private void closeFormNESDMC(int chipId) {
        if (frmNESDMC[chipId] == null) return;

        try {
            frmNESDMC[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmNESDMC[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmNESDMC[chipId] = null;
    }

    private void openFormFDS(int chipId, boolean force /* = false */) {
        if (frmFDS[chipId] != null) {
            if (!force) {
                closeFormFDS(chipId);
                return;
            } else return;
        }

        frmFDS[chipId] = new FormFDS(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds[chipId]);

        if (setting.getLocation().getPosFDS()[chipId].equals(empty)) {
            frmFDS[chipId].x = this.getLocation().x;
            frmFDS[chipId].y = this.getLocation().y + 264;
        } else {
            frmFDS[chipId].x = setting.getLocation().getPosFDS()[chipId].x;
            frmFDS[chipId].y = setting.getLocation().getPosFDS()[chipId].y;
        }

        frmFDS[chipId].setVisible(true);
        frmFDS[chipId].update();
        frmFDS[chipId].setTitle("FDS (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds_old[chipId] = new NpNesChip.FdsChip.Params();

        checkAndSetForm(frmFDS[chipId]);
    }

    private void closeFormFDS(int chipId) {
        if (frmFDS[chipId] == null) return;

        try {
            frmFDS[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmFDS[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmFDS[chipId] = null;
    }

    private void openFormVRC6(int chipId, boolean force /* = false */) {
        if (frmVRC6[chipId] != null) {
            if (!force) {
                closeFormVRC6(chipId);
                return;
            } else return;
        }

        audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6_old[chipId] = new NpNesChip.Vrc6Chip.Params();
        for (int i = 0; i < audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6_old[chipId].channels.length; i++) {
            audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6_old[chipId].channels[i].mask = null;
        }
        frmVRC6[chipId] = new FormVRC6(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6[chipId], audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6_old[chipId]);

        if (setting.getLocation().getPosVrc6()[chipId].equals(empty)) {
            frmVRC6[chipId].x = this.getLocation().x;
            frmVRC6[chipId].y = this.getLocation().y + 264;
        } else {
            frmVRC6[chipId].x = setting.getLocation().getPosVrc6()[chipId].x;
            frmVRC6[chipId].y = setting.getLocation().getPosVrc6()[chipId].y;
        }

        frmVRC6[chipId].setVisible(true);
        frmVRC6[chipId].update();
        frmVRC6[chipId].setTitle("Vrc6Inst (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6_old[chipId] = new NpNesChip.Vrc6Chip.Params();

        checkAndSetForm(frmVRC6[chipId]);
    }

    private void closeFormVRC6(int chipId) {
        if (frmVRC6[chipId] == null) return;

        try {
            frmVRC6[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmVRC6[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmVRC6[chipId] = null;
    }

    private void openFormVRC7(int chipId, boolean force /* = false */) {
        if (frmVRC7[chipId] != null) { // && frmInfo.isClosed)
            if (!force) {
                closeFormVRC7(chipId);
                return;
            } else return;
        }

        frmVRC7[chipId] = new FormVRC7(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Vrc7Chip.class).vrc7[chipId]);

        if (setting.getLocation().getPosVrc7()[chipId].equals(empty)) {
            frmVRC7[chipId].x = this.getLocation().x;
            frmVRC7[chipId].y = this.getLocation().y + 264;
        } else {
            frmVRC7[chipId].x = setting.getLocation().getPosVrc7()[chipId].x;
            frmVRC7[chipId].y = setting.getLocation().getPosVrc7()[chipId].y;
        }

        frmVRC7[chipId].setVisible(true);
        frmVRC7[chipId].update();
        frmVRC7[chipId].setTitle("VRC7 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Vrc7Chip.class).vrc7_old[chipId] = new NpNesChip.Vrc7Chip.Params();

        checkAndSetForm(frmVRC7[chipId]);
    }

    private void closeFormVRC7(int chipId) {
        if (frmVRC7[chipId] == null) return;

        try {
            frmVRC7[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmVRC7[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmVRC7[chipId] = null;
    }

    private void openFormMMC5(int chipId, boolean force /* = false */) {
        if (frmMMC5[chipId] != null) {
            if (!force) {
                closeFormMMC5(chipId);
                return;
            } else return;
        }

        frmMMC5[chipId] = new FormMMC5(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(Mmc5Chip.class).mmc5[chipId]);

        if (setting.getLocation().getPosMMC5()[chipId].equals(empty)) {
            frmMMC5[chipId].x = this.getLocation().x;
            frmMMC5[chipId].y = this.getLocation().y + 264;
        } else {
            frmMMC5[chipId].x = setting.getLocation().getPosMMC5()[chipId].x;
            frmMMC5[chipId].y = setting.getLocation().getPosMMC5()[chipId].y;
        }

        frmMMC5[chipId].setVisible(true);
        frmMMC5[chipId].update();
        frmMMC5[chipId].setTitle("MMC5 (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(Mmc5Chip.class).mmc5_old[chipId] = new NpNesChip.Mmc5Chip.Params();

        checkAndSetForm(frmMMC5[chipId]);
    }

    private void closeFormMMC5(int chipId) {
        if (frmMMC5[chipId] == null) return;

        try {
            frmMMC5[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmMMC5[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmMMC5[chipId] = null;
    }

    private void openFormRegTest(int chipId, Class<? extends Chip> selectedChip /* = null */, boolean force /* = false */) {
        if (frmRegTest != null) {
            frmRegTest.changeChip(selectedChip);
            return;
        }

        frmRegTest = new FormRegTest(this, chipId, selectedChip, setting.getOther().getZoom());

        if (setting.getLocation().getPosRegTest()[chipId].equals(empty)) {
            frmRegTest.x = this.getLocation().x;
            frmRegTest.y = this.getLocation().y + 264;
        } else {
            frmRegTest.x = setting.getLocation().getPosRegTest()[chipId].x;
            frmRegTest.y = setting.getLocation().getPosRegTest()[chipId].y;
        }

        frmRegTest.setVisible(true);
        frmRegTest.update();
        frmRegTest.changeChip(selectedChip);
        frmRegTest.setTitle("RegTest (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmRegTest);
    }

    private void openFormVisWave() {
        if (frmVisWave != null && !frmVisWave.isClosed) {
            frmVisWave.requestFocus();
            return;
        }

        frmVisWave = new FormVisWave(this);

        if (setting.getLocation().getPosVisWave().equals(empty)) {
            frmVisWave.x = this.getLocation().x;
            frmVisWave.y = this.getLocation().y + 264;
        } else {
            frmVisWave.x = setting.getLocation().getPosVisWave().x;
            frmVisWave.y = setting.getLocation().getPosVisWave().y;
        }

        frmVisWave.setVisible(true);

        checkAndSetForm(frmVisWave);
    }

    private void openFormN106(int chipId, boolean force /* = false */) {
        if (frmN106[chipId] != null) {
            if (!force) {
                closeFormN106(chipId);
                return;
            } else return;
        }

        audio.plugin.chipRegister.chip(N163Chip.class).n106_old[chipId] = new NpNesChip.N163Chip.Params();
        for (int i = 0; i < audio.plugin.chipRegister.chip(N163Chip.class).n106_old[chipId].channels.length; i++) {
            audio.plugin.chipRegister.chip(N163Chip.class).n106_old[chipId].channels[i].mask = null;
        }
        frmN106[chipId] = new FormN106(this, chipId, setting.getOther().getZoom(), audio.plugin.chipRegister.chip(N163Chip.class).n106[chipId], audio.plugin.chipRegister.chip(N163Chip.class).n106_old[chipId]);

        if (setting.getLocation().getPosN106()[chipId].equals(empty)) {
            frmN106[chipId].x = this.getLocation().x;
            frmN106[chipId].y = this.getLocation().y + 264;
        } else {
            frmN106[chipId].x = setting.getLocation().getPosN106()[chipId].x;
            frmN106[chipId].y = setting.getLocation().getPosN106()[chipId].y;
        }

        frmN106[chipId].setVisible(true);
        frmN106[chipId].update();
        frmN106[chipId].setTitle("N163(N106) (%s)".formatted(chipId == 0 ? "Primary" : "Secondary"));
        audio.plugin.chipRegister.chip(N163Chip.class).n106_old[chipId] = new NpNesChip.N163Chip.Params();

        checkAndSetForm(frmN106[chipId]);
    }

    private void closeFormN106(int chipId) {
        if (frmN106[chipId] == null) return;

        try {
            frmN106[chipId].setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmN106[chipId].dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmN106[chipId] = null;
    }

    private void closeFormRegTest(int chipId) {
        if (frmRegTest == null) return;

        try {
            frmRegTest.setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmRegTest.dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmRegTest = null;
    }

    private void closeFormVisWave() {
        if (frmVisWave == null) return;

        try {
            frmVisWave.setVisible(false);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        try {
            frmVisWave.dispose();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
        frmVisWave = null;
    }

    private void openInfo() {
        if (frmInfo != null && !frmInfo.isClosed) {
            try {
                frmInfo.setVisible(false);
                frmInfo.dispose();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                frmInfo = null;
            }
            return;
        }

        if (frmInfo != null) {
            try {
                frmInfo.setVisible(false);
                frmInfo.dispose();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                frmInfo = null;
            }
        }

        frmInfo = new FormInfo(this);
        if (setting.getLocation().getPInfo().equals(empty)) {
            frmInfo.x = this.getLocation().x + 328;
            frmInfo.y = this.getLocation().y;
        } else {
            frmInfo.x = setting.getLocation().getPInfo().x;
            frmInfo.y = setting.getLocation().getPInfo().y;
        }

        Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle rc = new Rectangle(frmInfo.getLocation(), frmInfo.getSize());
        if (s.contains(rc)) {
            frmInfo.setLocation(rc.getLocation());
            frmInfo.setPreferredSize(rc.getSize());
        } else {
            frmInfo.setLocation(new Point(100, 100));
        }

        frmInfo.setting = setting;
        frmInfo.setVisible(true);
        frmInfo.update();
    }

    private void openMIDIKeyboard() {
        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) {
            try {
                frmYM2612MIDI.setVisible(false);
                frmYM2612MIDI.dispose();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                frmYM2612MIDI = null;
            }
            return;
        }

        if (frmYM2612MIDI != null) {
            try {
                frmYM2612MIDI.setVisible(false);
                frmYM2612MIDI.dispose();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                frmYM2612MIDI = null;
            }
        }

        try {
            frmYM2612MIDI = new FormYM2612MIDI(this, setting.getOther().getZoom(), ym2612Midi.ym2612Midi);
            if (setting.getLocation().getPosYm2612MIDI().equals(empty)) {
                frmYM2612MIDI.x = this.getLocation().x + 328;
                frmYM2612MIDI.y = this.getLocation().y;
            } else {
                frmYM2612MIDI.x = setting.getLocation().getPosYm2612MIDI().x;
                frmYM2612MIDI.y = setting.getLocation().getPosYm2612MIDI().y;
            }

            Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Rectangle rc = new Rectangle(frmYM2612MIDI.getLocation(), frmYM2612MIDI.getSize());
            if (s.contains(rc)) {
                frmYM2612MIDI.setLocation(rc.getLocation());
                frmYM2612MIDI.setPreferredSize(rc.getSize());
            } else {
                frmYM2612MIDI.setLocation(new Point(100, 100));
            }

            frmYM2612MIDI.setVisible(true);
            frmYM2612MIDI.update();
            ym2612Midi.ym2612Midi = new YM2612MIDI.Params();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not open MIDI keyboard panel: " + e.getMessage());
            frmYM2612MIDI = null;
        }
    }

    private void openSetting() {
        try {
            FormSetting frm = new FormSetting(setting);
            if (frm.showDialog() == JFileChooser.APPROVE_OPTION) {
                flgReinit = true;
                reinit(frm.setting);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Could not open settings panel: " + e.getMessage(), e);
        }
    }

    final Audio audio = Audio.getInstance();

    private void reinit(Setting setting) {
        if (!flgReinit) return;

        StopMIDIInMonitoring();
        frmPlayList.stop();

        Request req = new Request(enmRequest.Stop, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }

        req = new Request(enmRequest.Die, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }

        //audio.Stop();
        audio.close();

        this.setting = setting;
        this.setting.save();

        screen.setting = this.setting;
        frmPlayList.setting = this.setting;
        //oldParam = new MDChipParams();
        //newParam = new MDChipParams();
        reqAllScreenInit = true;
        //screen.screenInitAll();

        logger.log(Level.ERROR, "The settings have been changed, so the audio initialization process will start again.");

        if (audio.plugin != null) {
            audio.plugin.init();
        }

        logger.log(Level.ERROR, "Audio initialization process complete");

//        frmVSTeffectList.dispPluginList();
        startMIDIInMonitoring();

        isInitialOpenFolder = true;
        flgReinit = false;

//        for (int i = 0; i < 5; i++) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            Application.DoEvents();
//        }
    }

    private void openMixer() {
        if (frmMixer2 != null && !frmMixer2.isClosed) {
            try {
                frmMixer2.setVisible(false);
                frmMixer2.dispose();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                frmMixer2 = null;
            }
            return;
        }

        if (frmMixer2 != null) {
            try {
                frmMixer2.setVisible(false);
                frmMixer2.dispose();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            } finally {
                frmMixer2 = null;
            }
        }

        frmMixer2 = new FormMixer2(this, setting.getOther().getZoom(), newParam);
        if (setting.getLocation().getPosMixer().equals(empty)) {
            frmMixer2.x = this.getLocation().x + 328;
            frmMixer2.y = this.getLocation().y;
        } else {
            frmMixer2.x = setting.getLocation().getPosMixer().x;
            frmMixer2.y = setting.getLocation().getPosMixer().y;
        }

//        Screen s = Screen.FromControl(frmMixer2);
        Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle rc = new Rectangle(frmMixer2.getLocation(), frmMixer2.getSize());
        if (s.contains(rc)) {
            frmMixer2.setLocation(rc.getLocation());
            frmMixer2.setPreferredSize(rc.getSize());
        } else {
            frmMixer2.setLocation(new Point(100, 100));
        }

        //frmMixer.setting = setting;
        //screen.AddMixer(frmMixer2.pbScreen, Properties.Resources.planeMixer);
        frmMixer2.setVisible(true);
        frmMixer2.update();
        //screen.screenInitMixer();
        oldParam = new MDChipParams();
    }

//    private void pbScreen_DragEnter(DragEvent e) {
//        e.Effect = DragDropEffects.All;
//    }

    private void pbScreen_DragDrop(List<File> files) {
        String filename = files.getFirst().getPath();

        try {
            // Stop the song
            frmPlayList.stop();
            this.stop();
//            while (!audio.isStopped())
//                Application.DoEvents();

            frmPlayList.getPlayList().addFile(filename);
            //frmPlayList.AddList(filename);

            if (filename.toLowerCase().lastIndexOf(".zip") == -1) {
                loadAndPlay(0, 0, filename, null);
                frmPlayList.setStart(-1);
                oldParam = new MDChipParams();

                frmPlayList.play();
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "Failed to read file.");
        }
    }

//    @Override protected boolean getShowWithoutActivation() {
//        return true;
//    }

    private void allScreenInit() {
        //oldParam = new MDChipParams();
        drawTimer(screen.mainScreen, 0, oldParam.Cminutes, oldParam.Csecond, oldParam.Cmillisecond, newParam.Cminutes, newParam.Csecond, newParam.Cmillisecond);
        drawTimer(screen.mainScreen, 1, oldParam.TCminutes, oldParam.TCsecond, oldParam.TCmillisecond, newParam.TCminutes, newParam.TCsecond, newParam.TCmillisecond);
        drawTimer(screen.mainScreen, 2, oldParam.LCminutes, oldParam.LCsecond, oldParam.LCmillisecond, newParam.LCminutes, newParam.LCsecond, newParam.LCmillisecond);
        screenInit(null);

        for (int i = 0; i < 2; i++) {
            if (frmAY8910[i] != null) frmAY8910[i].initScreen();
            //if (frmC140[i] != null) frmC140[i].screenInit();
            //if (frmC352[i] != null) frmC352[i].screenInit();
            if (frmFDS[i] != null) frmFDS[i].initScreen();
            //if (frmHuC6280[i] != null) frmHuC6280[i].screenInit();
            if (frmK051649[i] != null) frmK051649[i].initScreen();
            if (frmMCD[i] != null) frmMCD[i].initScreen();
            if (frmMIDI[i] != null) frmMIDI[i].screenInit();
            if (frmMMC5[i] != null) frmMMC5[i].initScreen();
            if (frmNESDMC[i] != null) frmNESDMC[i].initScreen();
            if (frmOKIM6258[i] != null) frmOKIM6258[i].initScreen();
            if (frmOKIM6295[i] != null) frmOKIM6295[i].initScreen();
            //if (frmSegaPCM[i] != null) frmSegaPCM[i].screenInit();
            //if (frmSN76489[i] != null) frmSN76489[i].screenInit();
            //if (frmYM2151[i] != null) frmYM2151[i].screenInit();
            //if (frmYM2203[i] != null) frmYM2203[i].screenInit();
            if (frmYM2413[i] != null) frmYM2413[i].initScreen();
            //if (frmYM2608[i] != null) frmYM2608[i].screenInit();
            //if (frmYM2610[i] != null) frmYM2610[i].screenInit();
            //if (frmYM2612[i] != null) frmYM2612[i].screenInit();
            if (frmYM3526[i] != null) frmYM3526[i].initScreen();
            if (frmY8950[i] != null) frmY8950[i].initScreen();
            if (frmYM3812[i] != null) frmYM3812[i].initScreen();
            if (frmYMF262[i] != null) frmYMF262[i].initScreen();
            if (frmYMF278B[i] != null) frmYMF278B[i].initScreen();
            if (frmVRC7[i] != null) frmVRC7[i].initScreen();

        }

        if (frmMixer2 != null) frmMixer2.screenInit();
        if (frmInfo != null) frmInfo.screenInit();
        //if (frmYM2612MIDI != null) frmYM2612MIDI.screenInit();

        if (frmRegTest != null) frmRegTest.initScreen();

        reqAllScreenInit = false;
    }

    /**
     * !!This method is not running on the main thread!!
     */
    private void screenMainLoop() {
        double nextFrame = (double) System.currentTimeMillis();
        isRunning = true;
        stopped = false;

        while (isRunning) {

            if (reqAllScreenInit) {
                allScreenInit();
            }

            float period = 1000f / (float) setting.getOther().getScreenFrameRate();
            double tickCount = (double) System.currentTimeMillis();

            if (tickCount < nextFrame) {
                if (nextFrame - tickCount > 1) {
                    try {
                        Thread.sleep((long) (nextFrame - tickCount));
                    } catch (InterruptedException ignored) {
                    }
                }
                continue;
            }

            try {
                screenChangeParams();
                screenChangeParamsForms();
            } catch (Exception e) {
                logger.log(Level.ERROR, "Exception in screenChangeParams loop: " + e.getMessage(), e);
            }

            if ((double) System.currentTimeMillis() >= nextFrame + period) {
                nextFrame += period;
                continue;
            }

            // this loop is not the EDT, so hand the drawing over to it
            SwingUtilities.invokeLater(() -> {
                screenDrawParams();
                screenDrawParamsForms();
            });

            nextFrame += period;

            // Audio.play() runs on its own thread, and does not clear the stopped flag until it has
            // torn the previous song down. Until it does, a stopped plugin means "this song has not
            // begun", not "this song has ended". This is about the audio, not about the play list.
            if (songStarting && audio.isRendering()) {
                songStarting = false;

                // Only now is it known which chips the song is made of: the instruments behind them
                // are not built until play() has prepared the plugin, which happens on that thread,
                // after playData() asked for the song.
                if (setting.getOther().getAutoOpen()) {
                    SwingUtilities.invokeLater(this::autoOpenPanels);
                }
            }

            if (frmPlayList != null && frmPlayList.isPlaying()) {
                if (!songStarting) {
                    if ((setting.getOther().getUseLoopTimes() && audio.plugin.getVgmCurLoopCounter() > setting.getOther().getLoopTimes() - 1)
                            || audio.plugin.getVGMStopped()) {
                        fadeout();
                    }
                    if (audio.plugin.isStopped()) {
                        nextPlayMode();
                    }
                }
            }

            if (getFatalError()) {
                logger.log(Level.ERROR, "A FatalError occurred in audio. Restart audio initialization process.");

                frmPlayList.stop();
                try {
                    Request req = new Request(enmRequest.Stop, null, null);
                    OpeManager.requestToAudio(req);
                    while (!req.getEnd()) Thread.sleep(1);
                    //audio.Stop();
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                }

                try {
                    audio.close();
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                }

                setFatalError(false);
                audio.plugin.init();

                logger.log(Level.ERROR, "Audio initialization process complete");
            }
        }

        stopped = true;
    }

    private void screenChangeParams() {
        if (audio.plugin == null) return;

        long w = audio.plugin.getCounter();
        double sec = (double) w / (double) mdplayer.Common.VGMProcSampleRate;
        newParam.Cminutes = (int) (sec / 60);
        sec -= newParam.Cminutes * 60;
        newParam.Csecond = (int) sec;
        sec -= newParam.Csecond;
        newParam.Cmillisecond = (int) (sec * 100.0);

        w = audio.plugin.getTotalCounter();
        sec = (double) w / (double) mdplayer.Common.VGMProcSampleRate;
        newParam.TCminutes = (int) (sec / 60);
        sec -= newParam.TCminutes * 60;
        newParam.TCsecond = (int) sec;
        sec -= newParam.TCsecond;
        newParam.TCmillisecond = (int) (sec * 100.0);

        w = audio.plugin.getLoopCounter();
        sec = (double) w / (double) mdplayer.Common.VGMProcSampleRate;
        newParam.LCminutes = (int) (sec / 60);
        sec -= newParam.LCminutes * 60;
        newParam.LCsecond = (int) sec;
        sec -= newParam.LCsecond;
        newParam.LCmillisecond = (int) (sec * 100.0);

        // Fader (Master Volume)
        int val;
        if (faderMasterDrag) {
            newParam.Master = Common.range(faderMasterVal, 0, 56);
        } else {
            val = Common.range(setting.getBalance().getMasterVolume(), -192, 20) + 192;
            val = (int) (val * ((7.0 * 8) / (20.0 - (-192))));
            newParam.Master = val;
        }

        val = Common.range(visVolumeMaster / 220, 0, 56);
        if (newParam.MasterVis > 0) newParam.MasterVis--;
        newParam.MasterVis = Math.max(newParam.MasterVis, val);

        newParam.MasterHover = faderMasterHover ? 0 : 1;
        newParam.MasterDrag = faderMasterDrag ? 0 : 1;

        // Fader (Timeline)
        double gc = (double) audio.plugin.getCounter();
        double tc = (double) audio.plugin.getTotalCounter();
        if (tc > 0.0) {
            newParam.TimeLineVis = (int) (57.0 * gc / tc) % 57;
        } else {
            newParam.TimeLineVis = 0;
        }

        if (faderTimeLineDrag) {
            newParam.TimeLine = faderTimeLineVal;
        } else {
            newParam.TimeLine = newParam.TimeLineVis;
        }

        newParam.TimeLineHover = faderTimeLineHover ? 0 : 1;
        newParam.TimeLineDrag = faderTimeLineDrag ? 0 : 1;

        updateOpeButtonActiveState();
    }

    private void screenChangeParamsForms() {
        for (int chipId = 0; chipId < 2; chipId++) {
            if (frmMCD[chipId] != null && !frmMCD[chipId].isClosed) frmMCD[chipId].changeScreenParams();
            else frmMCD[chipId] = null;

            if (frmRf5c68[chipId] != null && !frmRf5c68[chipId].isClosed) frmRf5c68[chipId].changeScreenParams();
            else frmRf5c68[chipId] = null;

            if (frmC140[chipId] != null && !frmC140[chipId].isClosed) frmC140[chipId].changeScreenParams();
            else frmC140[chipId] = null;

            if (frmS5B[chipId] != null && !frmS5B[chipId].isClosed) frmS5B[chipId].changeScreenParams();
            else frmS5B[chipId] = null;

            if (frmDMG[chipId] != null && !frmDMG[chipId].isClosed) frmDMG[chipId].changeScreenParams();
            else frmDMG[chipId] = null;

            if (frmPPZ8[chipId] != null && !frmPPZ8[chipId].isClosed) frmPPZ8[chipId].changeScreenParams();
            else frmPPZ8[chipId] = null;

            if (frmYMZ280B[chipId] != null && !frmYMZ280B[chipId].isClosed) frmYMZ280B[chipId].changeScreenParams();
            else frmYMZ280B[chipId] = null;

            if (frmC352[chipId] != null && !frmC352[chipId].isClosed) frmC352[chipId].changeScreenParams();
            else frmC352[chipId] = null;

            if (frmMultiPCM[chipId] != null && !frmMultiPCM[chipId].isClosed) frmMultiPCM[chipId].changeScreenParams();
            else frmMultiPCM[chipId] = null;

            if (frmGA20[chipId] != null && !frmGA20[chipId].isClosed) frmGA20[chipId].changeScreenParams();
            else frmGA20[chipId] = null;

            if (frmK053260[chipId] != null && !frmK053260[chipId].isClosed) frmK053260[chipId].changeScreenParams();
            else frmK053260[chipId] = null;

            if (frmK054539[chipId] != null && !frmK054539[chipId].isClosed) frmK054539[chipId].changeScreenParams();
            else frmK054539[chipId] = null;

            if (frmQSound[chipId] != null && !frmQSound[chipId].isClosed) frmQSound[chipId].changeScreenParams();
            else frmQSound[chipId] = null;

            if (frmYM2608[chipId] != null && !frmYM2608[chipId].isClosed) frmYM2608[chipId].changeScreenParams();
            else frmYM2608[chipId] = null;

            if (frmYM2151[chipId] != null && !frmYM2151[chipId].isClosed) frmYM2151[chipId].changeScreenParams();
            else frmYM2151[chipId] = null;

            if (frmYM2203[chipId] != null && !frmYM2203[chipId].isClosed) frmYM2203[chipId].changeScreenParams();
            else frmYM2203[chipId] = null;

            if (frmYM2413[chipId] != null && !frmYM2413[chipId].isClosed) frmYM2413[chipId].changeScreenParams();
            else frmYM2413[chipId] = null;

            if (frmYM2610[chipId] != null && !frmYM2610[chipId].isClosed) frmYM2610[chipId].changeScreenParams();
            else frmYM2610[chipId] = null;

            if (frmYM2612[chipId] != null && !frmYM2612[chipId].isClosed) frmYM2612[chipId].changeScreenParams();
            else frmYM2612[chipId] = null;

            if (frmYM3526[chipId] != null && !frmYM3526[chipId].isClosed) frmYM3526[chipId].changeScreenParams();
            else frmYM3526[chipId] = null;

            if (frmY8950[chipId] != null && !frmY8950[chipId].isClosed) frmY8950[chipId].changeScreenParams();
            else frmY8950[chipId] = null;

            if (frmYM3812[chipId] != null && !frmYM3812[chipId].isClosed) frmYM3812[chipId].changeScreenParams();
            else frmYM3812[chipId] = null;

            if (frmYMF262[chipId] != null && !frmYMF262[chipId].isClosed) frmYMF262[chipId].changeScreenParams();
            else frmYMF262[chipId] = null;

            if (frmYMF271[chipId] != null && !frmYMF271[chipId].isClosed) frmYMF271[chipId].changeScreenParams();
            else frmYMF271[chipId] = null;

            if (frmYMF278B[chipId] != null && !frmYMF278B[chipId].isClosed) frmYMF278B[chipId].changeScreenParams();
            else frmYMF278B[chipId] = null;

            if (frmOKIM6258[chipId] != null && !frmOKIM6258[chipId].isClosed) frmOKIM6258[chipId].changeScreenParams();
            else frmOKIM6258[chipId] = null;

            if (frmOKIM6295[chipId] != null && !frmOKIM6295[chipId].isClosed) frmOKIM6295[chipId].changeScreenParams();
            else frmOKIM6295[chipId] = null;

            if (frmSN76489[chipId] != null && !frmSN76489[chipId].isClosed) frmSN76489[chipId].changeScreenParams();
            else frmSN76489[chipId] = null;

            if (frmSegaPCM[chipId] != null && !frmSegaPCM[chipId].isClosed) frmSegaPCM[chipId].changeScreenParams();
            else frmSegaPCM[chipId] = null;

            if (frmAY8910[chipId] != null && !frmAY8910[chipId].isClosed) {
                frmAY8910[chipId].changeScreenParams();
            } else frmAY8910[chipId] = null;

            if (frmHuC6280[chipId] != null && !frmHuC6280[chipId].isClosed) frmHuC6280[chipId].changeScreenParams();
            else frmHuC6280[chipId] = null;

            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) frmK051649[chipId].changeScreenParams();
            else frmK051649[chipId] = null;

            if (frmMIDI[chipId] != null && !frmMIDI[chipId].isClosed) frmMIDI[chipId].screenChangeParams();
            else frmMIDI[chipId] = null;

            if (frmNESDMC[chipId] != null && !frmNESDMC[chipId].isClosed) frmNESDMC[chipId].changeScreenParams();
            else frmNESDMC[chipId] = null;

            if (frmFDS[chipId] != null && !frmFDS[chipId].isClosed) frmFDS[chipId].changeScreenParams();
            else frmFDS[chipId] = null;

            if (frmMMC5[chipId] != null && !frmMMC5[chipId].isClosed) frmMMC5[chipId].changeScreenParams();
            else frmMMC5[chipId] = null;

            if (frmVRC6[chipId] != null && !frmVRC6[chipId].isClosed) frmVRC6[chipId].changeScreenParams();
            else frmVRC6[chipId] = null;

            if (frmVRC7[chipId] != null && !frmVRC7[chipId].isClosed) frmVRC7[chipId].changeScreenParams();
            else frmVRC7[chipId] = null;

            if (frmN106[chipId] != null && !frmN106[chipId].isClosed) frmN106[chipId].changeScreenParams();
            else frmN106[chipId] = null;

        }
        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) frmYM2612MIDI.screenChangeParams();
        else frmYM2612MIDI = null;
        if (frmMixer2 != null && !frmMixer2.isClosed) frmMixer2.screenChangeParams();
        else frmMixer2 = null;

        if (frmRegTest != null && !frmRegTest.isClosed) frmRegTest.changeScreenParams();
        else frmRegTest = null;
    }

    private void screenDrawParams() {
        if (screen == null || screen.mainScreen == null) return;
        // drawing

        for (int i = 0; i < lstOpeButtonActive.length; i++) {
            if (lstOpeButtonActive[i] != lstOpeButtonActiveOld[i]) {
                lstOpeButtonActiveOld[i] = lstOpeButtonActive[i];
                redrawButton(lstOpeButtonControl[i], setting.getOther().getZoom(),
                        lstOpeButtonActive[i] ? lstOpeButtonActiveImage[i] : lstOpeButtonLeaveImage[i]
                );
            }
        }

        drawTimer(screen.mainScreen, 0, oldParam.Cminutes, oldParam.Csecond, oldParam.Cmillisecond, newParam.Cminutes, newParam.Csecond, newParam.Cmillisecond);
        drawTimer(screen.mainScreen, 1, oldParam.TCminutes, oldParam.TCsecond, oldParam.TCmillisecond, newParam.TCminutes, newParam.TCsecond, newParam.TCmillisecond);
        drawTimer(screen.mainScreen, 2, oldParam.LCminutes, oldParam.LCsecond, oldParam.LCmillisecond, newParam.LCminutes, newParam.LCsecond, newParam.LCmillisecond);

        // C# took these by ref, so drawTimer marked them itself; Java has to do it here or every
        // frame would redraw the digits
        oldParam.Cminutes = newParam.Cminutes;
        oldParam.Csecond = newParam.Csecond;
        oldParam.Cmillisecond = newParam.Cmillisecond;
        oldParam.TCminutes = newParam.TCminutes;
        oldParam.TCsecond = newParam.TCsecond;
        oldParam.TCmillisecond = newParam.TCmillisecond;
        oldParam.LCminutes = newParam.LCminutes;
        oldParam.LCsecond = newParam.LCsecond;
        oldParam.LCmillisecond = newParam.LCmillisecond;

        // nothing is loaded yet: the skin, the buttons and the timers are all there is to show
        if (audio.plugin == null) {
            screen.refresh(null);
            return;
        }

        // The chip-name lamps are not drawn: this skin (planeMain) spends that row on the timers,
        // which is why the original has its drawChipName() calls commented out too. The lamps are
        // still lit and faded — they are how we know which chips a song is actually using.
        chipLED.fade();

        screen.mainScreen.drawFont4(1, 9, 1, audio.plugin.isDataBlock(EnmModel.VirtualModel) ? "VD" : "  ");
        screen.mainScreen.drawFont4(321 - 16, 9, 1, isPcmRAMWrite(EnmModel.VirtualModel) ? "VP" : "  ");
        screen.mainScreen.drawFont4(1, 17, 1, audio.plugin.isDataBlock(EnmModel.RealModel) ? "RD" : "  ");
        screen.mainScreen.drawFont4(321 - 16, 17, 1, isPcmRAMWrite(EnmModel.RealModel) ? "RP" : "  ");

        if (setting.getDebug_DispFrameCounter()) {
            long v = audio.plugin.getVirtualFrameCounter();
            if (v != -1) screen.mainScreen.drawFont8(0, 0, 0, "EMU        : %12d ".formatted(v));
            long r = audio.plugin.getRealFrameCounter();
            if (r != -1) screen.mainScreen.drawFont8(0, 8, 0, "REAL CHIP  : %12d ".formatted(r));
            long d = r - v;
            if (r != -1 && v != -1)
                screen.mainScreen.drawFont8(0, 16, 0, "R.CHIP-EMU : %12d ".formatted(d));
            screen.mainScreen.drawFont8(0, 24, 0, "PROC TIME  : %12d ".formatted(audio.plugin.procTimePer1Frame));
        }

        int[] od = {oldParam.MasterDrag};
        int[] ov = {oldParam.MasterHover};
        int[] oval1 = {oldParam.Master};
        int[] oval2 = {oldParam.MasterVis};
        drawFaderH(screen.mainScreen, 23 * 8, 14,
                newParam.MasterDrag, newParam.MasterHover, newParam.Master, newParam.MasterVis,
                od, ov, oval1, oval2);
        oldParam.MasterDrag = od[0];
        oldParam.MasterHover = ov[0];
        oldParam.Master = oval1[0];
        oldParam.MasterVis = oval2[0];

        int[] tod = {oldParam.TimeLineDrag};
        int[] tov = {oldParam.TimeLineHover};
        int[] toval1 = {oldParam.TimeLine};
        int[] toval2 = {oldParam.TimeLineVis};
        drawFaderH(screen.mainScreen, 23 * 8, 30,
                newParam.TimeLineDrag, newParam.TimeLineHover, newParam.TimeLine, newParam.TimeLineVis,
                tod, tov, toval1, toval2);
        oldParam.TimeLineDrag = tod[0];
        oldParam.TimeLineHover = tov[0];
        oldParam.TimeLine = toval1[0];
        oldParam.TimeLineVis = toval2[0];

        screen.refresh(null);

        audio.plugin.updateVol();

        String newInfo;
        MetaData metaData = (audio.plugin.driverVirtual != null) ? audio.plugin.driverVirtual.metaData : null;
        if (metaData != null) {
            String title = metaData.getFirst(Tag.Title);
            String usedChips = metaData.getFirst(Tag.Chip);
            newInfo = "MDPlayer - [%s] %s".formatted(usedChips, title);
        } else {
            newInfo = "MDPlayer";
        }

        try {
            setTitle(newInfo);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /** Only a vgm writes to PCM RAM, and only once it has a driver behind it. */
    boolean isPcmRAMWrite(Common.EnmModel model) {
        return audio.plugin instanceof VGMPlugin vgmPlugin
                && vgmPlugin.driverVirtual != null
                && vgmPlugin.driverVirtual.vgm.isPcmRAMWrite;
    }

    private void screenDrawParamsForms() {
        for (int chipId = 0; chipId < 2; chipId++) {
            if (frmMCD[chipId] != null && !frmMCD[chipId].isClosed) {
                frmMCD[chipId].drawScreenParams();
                frmMCD[chipId].update();
            } else frmMCD[chipId] = null;

            if (frmRf5c68[chipId] != null && !frmRf5c68[chipId].isClosed) {
                frmRf5c68[chipId].drawScreenParams();
                frmRf5c68[chipId].update();
            } else frmRf5c68[chipId] = null;

            if (frmC140[chipId] != null && !frmC140[chipId].isClosed) {
                frmC140[chipId].drawScreenParams();
                frmC140[chipId].update();
            } else frmC140[chipId] = null;

            if (frmPPZ8[chipId] != null && !frmPPZ8[chipId].isClosed) {
                frmPPZ8[chipId].drawScreenParams();
                frmPPZ8[chipId].update();
            } else frmPPZ8[chipId] = null;

            if (frmS5B[chipId] != null && !frmS5B[chipId].isClosed) {
                frmS5B[chipId].drawScreenParams();
                frmS5B[chipId].update();
            } else frmS5B[chipId] = null;

            if (frmDMG[chipId] != null && !frmDMG[chipId].isClosed) {
                frmDMG[chipId].drawScreenParams();
                frmDMG[chipId].update();
            } else frmDMG[chipId] = null;

            if (frmYMZ280B[chipId] != null && !frmYMZ280B[chipId].isClosed) {
                frmYMZ280B[chipId].drawScreenParams();
                frmYMZ280B[chipId].update();
            } else frmYMZ280B[chipId] = null;

            if (frmC352[chipId] != null && !frmC352[chipId].isClosed) {
                frmC352[chipId].drawScreenParams();
                frmC352[chipId].update();
            } else frmC352[chipId] = null;

            if (frmMultiPCM[chipId] != null && !frmMultiPCM[chipId].isClosed) {
                frmMultiPCM[chipId].drawScreenParams();
                frmMultiPCM[chipId].update();
            } else frmMultiPCM[chipId] = null;

            if (frmGA20[chipId] != null && !frmGA20[chipId].isClosed) {
                frmGA20[chipId].drawScreenParams();
                frmGA20[chipId].update();
            } else frmGA20[chipId] = null;

            if (frmK053260[chipId] != null && !frmK053260[chipId].isClosed) {
                frmK053260[chipId].drawScreenParams();
                frmK053260[chipId].update();
            } else frmK053260[chipId] = null;

            if (frmK054539[chipId] != null && !frmK054539[chipId].isClosed) {
                frmK054539[chipId].drawScreenParams();
                frmK054539[chipId].update();
            } else frmK054539[chipId] = null;

            if (frmQSound[chipId] != null && !frmQSound[chipId].isClosed) {
                frmQSound[chipId].drawScreenParams();
                frmQSound[chipId].update();
            } else frmQSound[chipId] = null;

            if (frmYM2608[chipId] != null && !frmYM2608[chipId].isClosed) {
                frmYM2608[chipId].drawScreenParams();
                frmYM2608[chipId].update();
            } else frmYM2608[chipId] = null;

            if (frmYM2151[chipId] != null && !frmYM2151[chipId].isClosed) {
                frmYM2151[chipId].drawScreenParams();
                frmYM2151[chipId].update();
            } else frmYM2151[chipId] = null;

            if (frmYM2203[chipId] != null && !frmYM2203[chipId].isClosed) {
                frmYM2203[chipId].drawScreenParams();
                frmYM2203[chipId].update();
            } else frmYM2203[chipId] = null;

            if (frmYM2413[chipId] != null && !frmYM2413[chipId].isClosed) {
                frmYM2413[chipId].drawScreenParams();
                frmYM2413[chipId].update();
            } else frmYM2413[chipId] = null;

            if (frmYM2610[chipId] != null && !frmYM2610[chipId].isClosed) {
                frmYM2610[chipId].drawScreenParams();
                frmYM2610[chipId].update();
            } else frmYM2610[chipId] = null;

            if (frmYM2612[chipId] != null && !frmYM2612[chipId].isClosed) {
                frmYM2612[chipId].drawScreenParams();
                frmYM2612[chipId].update();
            } else frmYM2612[chipId] = null;

            if (frmYM3526[chipId] != null && !frmYM3526[chipId].isClosed) {
                frmYM3526[chipId].drawScreenParams();
                frmYM3526[chipId].update();
            } else frmYM3526[chipId] = null;

            if (frmY8950[chipId] != null && !frmY8950[chipId].isClosed) {
                frmY8950[chipId].drawScreenParams();
                frmY8950[chipId].update();
            } else frmY8950[chipId] = null;

            if (frmYM3812[chipId] != null && !frmYM3812[chipId].isClosed) {
                frmYM3812[chipId].drawScreenParams();
                frmYM3812[chipId].update();
            } else frmYM3812[chipId] = null;

            if (frmYMF262[chipId] != null && !frmYMF262[chipId].isClosed) {
                frmYMF262[chipId].drawScreenParams();
                frmYMF262[chipId].update();
            } else frmYMF262[chipId] = null;

            if (frmYMF271[chipId] != null && !frmYMF271[chipId].isClosed) {
                frmYMF271[chipId].drawScreenParams();
                frmYMF271[chipId].update();
            } else frmYMF271[chipId] = null;

            if (frmYMF278B[chipId] != null && !frmYMF278B[chipId].isClosed) {
                frmYMF278B[chipId].drawScreenParams();
                frmYMF278B[chipId].update();
            } else frmYMF278B[chipId] = null;

            if (frmOKIM6258[chipId] != null && !frmOKIM6258[chipId].isClosed) {
                frmOKIM6258[chipId].drawScreenParams();
                frmOKIM6258[chipId].update();
            } else frmOKIM6258[chipId] = null;

            if (frmOKIM6295[chipId] != null && !frmOKIM6295[chipId].isClosed) {
                frmOKIM6295[chipId].drawScreenParams();
                frmOKIM6295[chipId].update();
            } else frmOKIM6295[chipId] = null;

            if (frmSN76489[chipId] != null && !frmSN76489[chipId].isClosed) {
                frmSN76489[chipId].drawScreenParams();
                frmSN76489[chipId].update();
            } else frmSN76489[chipId] = null;

            if (frmSegaPCM[chipId] != null && !frmSegaPCM[chipId].isClosed) {
                frmSegaPCM[chipId].drawScreenParams();
                frmSegaPCM[chipId].update();
            } else frmSegaPCM[chipId] = null;

            if (frmAY8910[chipId] != null && !frmAY8910[chipId].isClosed) {
                frmAY8910[chipId].drawScreenParams();
                frmAY8910[chipId].update();
            } else frmAY8910[chipId] = null;

            if (frmHuC6280[chipId] != null && !frmHuC6280[chipId].isClosed) {
                frmHuC6280[chipId].drawScreenParams();
                frmHuC6280[chipId].update();
            } else frmHuC6280[chipId] = null;

            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) {
                frmK051649[chipId].drawScreenParams();
                frmK051649[chipId].update();
            } else frmK051649[chipId] = null;

            if (frmMIDI[chipId] != null && !frmMIDI[chipId].isClosed) {
                frmMIDI[chipId].screenDrawParams();
                frmMIDI[chipId].update();
            } else frmMIDI[chipId] = null;

            if (frmNESDMC[chipId] != null && !frmNESDMC[chipId].isClosed) {
                frmNESDMC[chipId].drawScreenParams();
                frmNESDMC[chipId].update();
            } else frmNESDMC[chipId] = null;

            if (frmVRC6[chipId] != null && !frmVRC6[chipId].isClosed) {
                frmVRC6[chipId].drawScreenParams();
                frmVRC6[chipId].update();
            } else frmVRC6[chipId] = null;

            if (frmVRC7[chipId] != null && !frmVRC7[chipId].isClosed) {
                frmVRC7[chipId].drawScreenParams();
                frmVRC7[chipId].update();
            } else frmVRC7[chipId] = null;

            if (frmFDS[chipId] != null && !frmFDS[chipId].isClosed) {
                frmFDS[chipId].drawScreenParams();
                frmFDS[chipId].update();
            } else frmFDS[chipId] = null;

            if (frmMMC5[chipId] != null && !frmMMC5[chipId].isClosed) {
                frmMMC5[chipId].drawScreenParams();
                frmMMC5[chipId].update();
            } else frmMMC5[chipId] = null;

            if (frmN106[chipId] != null && !frmN106[chipId].isClosed) {
                frmN106[chipId].drawScreenParams();
                frmN106[chipId].update();
            } else
                frmN106[chipId] = null;

        }
        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) {
            frmYM2612MIDI.screenDrawParams();
            frmYM2612MIDI.update();
        } else
            frmYM2612MIDI = null;
        if (frmMixer2 != null && !frmMixer2.isClosed) {
            frmMixer2.screenDrawParams();
            frmMixer2.update();
        } else
            frmMixer2 = null;

        if (frmRegTest != null && !frmRegTest.isClosed) {
            frmRegTest.drawScreenParams();
            frmRegTest.update();
        } else
            frmRegTest = null;
    }

    @Override
    public void setTitle(String newInfo) {
        if (!this.getTitle().equals(newInfo)) {
            super.setTitle(newInfo);
        }
    }

    private void screenInit(Object dmy) {

        // nothing is on the screen yet, so no lamp has been drawn
        chipLED_old.clear();
        chipLED.clear();

        //byte[] chips = audio.GetChipStatus();
        //screen.mainScreen.drawChipName(14 * 4, 0 * 8, 0,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOPN, chips[0]);
        //screen.mainScreen.drawChipName(18 * 4, 0 * 8, 1,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOPN2, chips[1]);
        //screen.mainScreen.drawChipName(23 * 4, 0 * 8, 2,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOPNA, chips[2]);
        //screen.mainScreen.drawChipName(28 * 4, 0 * 8, 3,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOPNB, chips[3]);
        //screen.mainScreen.drawChipName(33 * 4, 0 * 8, 4,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOPM, chips[4]);
        //screen.mainScreen.drawChipName(37 * 4, 0 * 8, 5,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriDCSG, chips[5]);
        //screen.mainScreen.drawChipName(42 * 4, 0 * 8, 6,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriRF5C, chips[6]);
        //screen.mainScreen.drawChipName(47 * 4, 0 * 8, 7,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriPWM, chips[7]);
        //screen.mainScreen.drawChipName(51 * 4, 0 * 8, 8,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOKI5, chips[8]);
        //screen.mainScreen.drawChipName(56 * 4, 0 * 8, 9,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOKI9, chips[9]);
        //screen.mainScreen.drawChipName(61 * 4, 0 * 8, 10,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriC140, chips[10]);
        //screen.mainScreen.drawChipName(66 * 4, 0 * 8, 11,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriSPCM, chips[11]);
        //screen.mainScreen.drawChipName(4 * 4, 0 * 8, 12,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriAY10, chips[12]);
        //screen.mainScreen.drawChipName(9 * 4, 0 * 8, 13,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriOPLL, chips[13]);
        //screen.mainScreen.drawChipName(71 * 4, 0 * 8, 14,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.PriHuC8, chips[14]);

        //screen.mainScreen.drawChipName(14 * 4, 1 * 8, 0,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOPN, chips[128 + 0]);
        //screen.mainScreen.drawChipName(18 * 4, 1 * 8, 1,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOPN2, chips[128 + 1]);
        //screen.mainScreen.drawChipName(23 * 4, 1 * 8, 2,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOPNA, chips[128 + 2]);
        //screen.mainScreen.drawChipName(28 * 4, 1 * 8, 3,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOPNB, chips[128 + 3]);
        //screen.mainScreen.drawChipName(33 * 4, 1 * 8, 4,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOPM, chips[128 + 4]);
        //screen.mainScreen.drawChipName(37 * 4, 1 * 8, 5,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecDCSG, chips[128 + 5]);
        //screen.mainScreen.drawChipName(42 * 4, 1 * 8, 6,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecRF5C, chips[128 + 6]);
        //screen.mainScreen.drawChipName(47 * 4, 1 * 8, 7,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecPWM, chips[128 + 7]);
        //screen.mainScreen.drawChipName(51 * 4, 1 * 8, 8,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOKI5, chips[128 + 8]);
        //screen.mainScreen.drawChipName(56 * 4, 1 * 8, 9,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOKI9, chips[128 + 9]);
        //screen.mainScreen.drawChipName(61 * 4, 1 * 8, 10,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecC140, chips[128 + 10]);
        //screen.mainScreen.drawChipName(66 * 4, 1 * 8, 11,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecSPCM, chips[128 + 11]);
        //screen.mainScreen.drawChipName(4 * 4, 1 * 8, 12,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecAY10, chips[128 + 12]);
        //screen.mainScreen.drawChipName(9 * 4, 1 * 8, 13,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecOPLL, chips[128 + 13]);
        //screen.mainScreen.drawChipName(71 * 4, 0 * 8, 14,audio.plugin.chipRegister.chip(chipLEDChip.class).chipLED_old.SecHuC8, chips[128 + 14]);

        screen.mainScreen.drawFont4(1, 9, 1, audio.plugin != null && audio.plugin.isDataBlock(EnmModel.VirtualModel) ? "VD" : "  ");
        screen.mainScreen.drawFont4(321 - 16, 9, 1, isPcmRAMWrite(EnmModel.VirtualModel) ? "VP" : "  ");
        screen.mainScreen.drawFont4(1, 17, 1, audio.plugin != null && audio.plugin.isDataBlock(EnmModel.RealModel) ? "RD" : "  ");
        screen.mainScreen.drawFont4(321 - 16, 17, 1, isPcmRAMWrite(EnmModel.RealModel) ? "RP" : "  ");

        oldParam.Cminutes = -1;
        oldParam.Csecond = -1;
        oldParam.Cmillisecond = -1;
        oldParam.TCminutes = -1;
        oldParam.TCsecond = -1;
        oldParam.TCmillisecond = -1;
        oldParam.LCminutes = -1;
        oldParam.LCsecond = -1;
        oldParam.LCmillisecond = -1;
    }

    public void stop() {
        if (audio.isPaused()) {
            audio.pause();
        }

        if (audio.plugin != null && audio.plugin.chipRegister != null) {
            RealChipPlugin realChip = audio.plugin.chipRegister.plugin(RealChipPlugin.class);
            if (realChip != null && realChip.isThreadStopped() && audio.plugin.isStopped()) {
                audio.plugin.resetTimeCounter();
            }
        }

        frmPlayList.stop();
        OpeManager.requestToAudio(new Request(enmRequest.Stop, null, this::screenInit));
        //audio.Stop();
        //screenInit();
    }

    public void pause() {
        audio.pause();
    }

    public void fadeout() {
        audio.fadeout();
    }

    public void prev() {
        if (audio.isPaused()) {
            audio.pause();
        }

        frmPlayList.prevPlay(newButtonMode[9]);
    }

    public void play() {

//        if (audio.isPaused()) {
//            audio.pause();
//        }

        String[] fn;
        Tuple4<Integer, Integer, String, String> playFn;

        frmPlayList.stop();

        if (frmPlayList.getMusicCount() < 1) {
            fn = fileOpen(false);
            if (fn == null) return;
            frmPlayList.getPlayList().addFile(fn[0]);
            //frmPlayList.AddList(fn[0]);
            playFn = frmPlayList.setStart(-1); // last
        } else {
            fn = new String[] {""};
            playFn = frmPlayList.setStart(-2); // first
        }

        reqAllScreenInit = true;

        if (loadAndPlay(playFn.getItem1(), playFn.getItem2(), playFn.getItem3(), playFn.getItem4())) {
            frmPlayList.play();
        }
    }

    /** the thread {@link Audio#play()} renders the current song on */
    private Thread audioThread;

    /** set while a song has been asked for but has not started coming out yet */
    private volatile boolean songStarting;

    /**
     * Starts rendering the loaded song.
     * <p>
     * {@link Audio#play()} does not return until the song ends — it is the render loop — so it
     * cannot be run on the event dispatch thread, or the whole GUI would freeze for the length of
     * the song. It stops whatever was playing before, so the previous thread ends by itself.
     */
    private void startAudio() {
        songStarting = true;
        audioThread = new Thread(() -> {
            try {
                if (!audio.play()) {
                    SwingUtilities.invokeLater(() -> {
                        frmPlayList.stop();
                        OpeManager.requestToAudio(new Request(enmRequest.Stop, null, null));
                    });
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }, "mdplayer-audio");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    private void playData() {
        try {

            if (audio.isPaused()) {
                audio.pause();
            }
            //stop();

            //for (int chipId = 0; chipId < 2; chipId++) {
            //    for (int ch = 0; ch < 3; ch++) ForceChannelMask(AY8910, chipId, ch, audio.plugin.chipRegister.chip(ay8910Chip.class).ay8910[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 8; ch++) ForceChannelMask(YM2151, chipId, ch, audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 9; ch++) ForceChannelMask(YM2203, chipId, ch, audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 14; ch++) ForceChannelMask(YM2413, chipId, ch, audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 14; ch++) ForceChannelMask(YM2608, chipId, ch, audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 14; ch++) ForceChannelMask(YM2610, chipId, ch, audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 6; ch++) ForceChannelMask(Ym2612Inst, chipId, ch, audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 4; ch++) ForceChannelMask(SN76489, chipId, ch, audio.plugin.chipRegister.chip(sn76489Chip.class).sn76489[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 8; ch++) ForceChannelMask(RF5C164, chipId, ch, audio.plugin.chipRegister.chip(rf5c164Chip.class).rf5c164[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 24; ch++) ForceChannelMask(C140Inst, chipId, ch, audio.plugin.chipRegister.chip(c140Chip.class).c140[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 32; ch++) ForceChannelMask(C352Inst, chipId, ch, audio.plugin.chipRegister.chip(c352Chip.class).c352[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 16; ch++) ForceChannelMask(SEGAPCM, chipId, ch, audio.plugin.chipRegister.chip(segaPcmChip.class).segaPcm[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 6; ch++) ForceChannelMask(OotakeHuC6280, chipId, ch, audio.plugin.chipRegister.chip(huc6280Chip.class).huc6280[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 4; ch++) ForceChannelMask(OKIM6295, chipId, ch, audio.plugin.chipRegister.chip(okim6295Chip.class).okim6295[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 2; ch++) ResetChannelMask(NES, chipId, ch);
            //    for (int ch = 0; ch < 3; ch++) ResetChannelMask(DMC, chipId, ch);
            //    for (int ch = 0; ch < 3; ch++) ResetChannelMask(MMC5, chipId, ch);
            //    ResetChannelMask(FDS, chipId, 0);
            //}

            //oldParam = new MDChipParams();
            //newParam = new MDChipParams();
            reqAllScreenInit = true;

            if (setting.getOther().getWavSwitch()) {
                if (!Files.exists(Path.of(setting.getOther().getWavPath()))) {
                    int res = JOptionPane.showConfirmDialog(this,
                            "The path set for the wav file output destination does not exist. Create it and continue playing?",
                            "Confirmation of Path Creation",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (res == JOptionPane.NO_OPTION) {
                        throw new IllegalStateException("cancel");
                    }
                    try {
                        Files.createDirectory(Path.of(setting.getOther().getWavPath()));
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this,
                                "Failed to create path. Stop playing.", "Creation failed", JOptionPane.ERROR_MESSAGE);
                        throw new IllegalStateException("cancel");
                    }
                }
            }

            startAudio();

            for (int chipId = 0; chipId < 2; chipId++) {
                for (int ch = 0; ch < 3; ch++)
                    forceChannelMask(Ay8910Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(Ym2151Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask);
                for (int ch = 0; ch < 9; ch++)
                    forceChannelMask(Ym2203Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[ch].mask);
                for (int ch = 0; ch < 14; ch++)
                    forceChannelMask(Ym2413Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask);
                for (int ch = 0; ch < 14; ch++)
                    forceChannelMask(Ym2608Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[ch].mask);
                for (int ch = 0; ch < 14; ch++)
                    forceChannelMask(Ym2610Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[ch].mask);
                for (int ch = 0; ch < 6; ch++)
                    forceChannelMask(Ym2612Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[ch].mask);
                for (int ch = 0; ch < 4; ch++)
                    forceChannelMask(Sn76489Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(Rf5C164Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(Rf5C68Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask);
                for (int ch = 0; ch < 24; ch++)
                    forceChannelMask(C140Chip.class, chipId, ch, audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask);
                for (int ch = 0; ch < 32; ch++)
                    forceChannelMask(C352Chip.class, chipId, ch, audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask);
                for (int ch = 0; ch < 16; ch++)
                    forceChannelMask(SegaPcmChip.class, chipId, ch, audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask);
                for (int ch = 0; ch < 19; ch++)
                    forceChannelMask(QSoundChip.class, chipId, ch, audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask);
                for (int ch = 0; ch < 6; ch++)
                    forceChannelMask(HuC6280Chip.class, chipId, ch, audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask);
                for (int ch = 0; ch < 4; ch++)
                    forceChannelMask(OkiM6295Chip.class, chipId, ch, audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask);
                for (int ch = 0; ch < 2; ch++)
                    ForceChannelMaskNES(NesChip.class, chipId, ch, audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc);
                for (int ch = 2; ch < 5; ch++)
                    ForceChannelMaskNES(DmcChip.class, chipId, ch, audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc);
                for (int ch = 0; ch < 3; ch++) resetChannelMask(NpNesChip.Mmc5Chip.class, chipId, ch);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(Ppz8Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId].channels[ch].mask);
                for (int ch = 0; ch < 4; ch++)
                    forceChannelMask(DmgChip.class, chipId, ch, audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask);
                for (int ch = 0; ch < 3; ch++)
                    forceChannelMask(Vrc6Chip.class, chipId, ch, audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(N163Chip.class, chipId, ch, audio.plugin.chipRegister.chip(N163Chip.class).n106[chipId].channels[ch].mask);
                resetChannelMask(NpNesChip.FdsChip.class, chipId, 0);
            }


//logger.log(Level.TRACE, "stopped: " + audio.stopped + ", " + audio.hashCode());

            if (frmInfo != null) {
                frmInfo.update();
            }

            // the panels the song wants are opened once it is actually playing, from the screen
            // loop — until then there is nothing to ask about which chips it uses
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public void ff() {
        if (audio.isPaused()) {
            audio.pause();
        }

        audio.plugin.ff();
    }

    public void next() {
        if (audio.isPaused()) {
            audio.pause();
        }

        Request req = new Request(enmRequest.Stop, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }
        //audio.Stop();

        screenInit(null);

        //frmPlayList.nextPlay();
        frmPlayList.nextPlayMode(newButtonMode[9]);
    }

    private void nextPlayMode() {
        frmPlayList.nextPlayMode(newButtonMode[9]);
    }

    public void slow() {
        if (audio.isPaused()) {
            // upstream STBL506 dropped the frame-advance button: slow while paused now creeps
            // forward at a hundredth of the speed rather than stepping a fixed number of frames
            audio.plugin.speed(0.01);
            audio.pause();
            return;
        }

        if (audio.plugin.isStopped()) {
            play();
        }

        audio.plugin.slow();
    }

    private void playMode() {
        newButtonMode[9]++;
        if (newButtonMode[9] > 3) newButtonMode[9] = 0;
        opeButtonMode.setToolTipText(modeTip[newButtonMode[9]]);
    }

    static final Preferences prefs = Preferences.userNodeForPackage(FormMain.class);

    private String[] fileOpen(boolean isMultiSelection) {
        JFileChooser ofd = new JFileChooser();

        Arrays.stream(rb2.getString("cntSupportFile").split("\\s")).forEach(l -> {
            String[] p = l.split("\\|");
            ofd.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(p[1]);
                }

                @Override
                public String getDescription() {
                    return p[0];
                }
            });
        });
        String lastPath = prefs.get("mdplayer.lasPath", null);
        if (lastPath != null) ofd.setCurrentDirectory(new File(lastPath));
        ofd.setDialogTitle("Select a file");
        int filterIndex = setting.getOther().getFilterIndex();
        javax.swing.filechooser.FileFilter[] filters = ofd.getChoosableFileFilters();
        if (filterIndex >= 0 && filterIndex < filters.length) {
            ofd.setFileFilter(filters[filterIndex]);
        }

        if (!setting.getOther().getDefaultDataPath().isEmpty() && Files.exists(Path.of(setting.getOther().getDefaultDataPath())) && isInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(setting.getOther().getDefaultDataPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(isMultiSelection);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        prefs.put("mdplayer.lasPath", ofd.getCurrentDirectory().getPath());

        isInitialOpenFolder = false;
        setting.getOther().setFilterIndex(Common.getFilterIndex(ofd));

        return ofd.getSelectedFile().list();
    }

    private void dispPlayList() {
        frmPlayList.setting = setting;
        //if (!setting.getlocation().PPlayList.equals(empty)) {
        //    frmPlayList.setLocation(setting.getlocation().PPlayList);
        //}
        //if (!setting.getlocation().PPlayListWH.equals(empty)) {
        //    frmPlayList.getWidth() = setting.getlocation().PPlayListWH.x;
        //    frmPlayList.getHeight() = setting.getlocation().PPlayListWH.y;
        //}
        frmPlayList.setVisible(!frmPlayList.isVisible());
        if (frmPlayList.isVisible()) {
            checkAndSetForm(frmPlayList);
            frmPlayList.toFront();
            frmPlayList.requestFocus();
        }
    }

    private void dispVSTList() {
//        frmVSTeffectList.setVisible(!frmVSTeffectList.isVisible());
//        if (frmVSTeffectList.isVisible()) checkAndSetForm(frmVSTeffectList);
//        frmVSTeffectList.toFront();
//        frmVSTeffectList.toBack();
    }

    private void showContextMenu() {
        // a JPopupMenu must be shown via show(invoker, x, y); setVisible(true) alone leaves it
        // without an invoker, so it comes up unplaced and mislaid-out
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point p = pi.getLocation();
        SwingUtilities.convertPointFromScreen(p, this);
        cmsOpenOtherPanel.show(this, p.x, p.y);
    }

    public void getInstCh(Class<? extends Chip> chip, int ch, int chipId) {
        try {
            ym2612MIDI.setVoiceFromChipRegister(chip, chipId, ch);

            if (!setting.getOther().getUseGetInst()) return;

            if (chip == Ym2413Chip.class) {
                if (setting.getOther().getInstFormat() == EnmInstFormat.MML2VGM) {

                } else if (setting.getOther().getInstFormat() == EnmInstFormat.SendMML2VGM) {
                    getInstChForSendMML2VGM(chip, ch, chipId);
                } else {
                    getInstChForMGSC(chip, ch, chipId);
                }
            } else if (chip == Ym3812Chip.class || chip == YmF262Chip.class || chip == YmF278BChip.class) {
                if (setting.getOther().getInstFormat() == EnmInstFormat.OPLI) {
                    getInstChForOPLI(chip, ch, chipId);
                } else {
                    getInstChForSendMML2VGM(chip, ch, chipId);
                }
            } else if (chip == Vrc7Chip.class) {
                getInstChForMGSC(chip, ch, chipId);
            } else if (chip == K051649Chip.class) {
                if (setting.getOther().getInstFormat() == EnmInstFormat.MGSCSCC_PLAIN) {
                    getInstChForMGSCSCCPLAIN(ch, chipId);
                } else {
                    getInstChForMGSC(chip, ch, chipId);
                }
            } else if (chip == N163Chip.class) {
                getInstChForMCK(chip, ch, chipId);
            } else {
                switch (setting.getOther().getInstFormat()) {
                    case FMP7:
                        getInstChForFMP7(chip, ch, chipId);
                        break;
                    case MDX:
                        getInstChForMDX(chip, ch, chipId);
                        break;
                    case MML2VGM:
                        getInstChForMML2VGM(chip, ch, chipId);
                        break;
                    case MUCOM88:
                        getInstChForMucom88(chip, ch, chipId);
                        break;
                    case MUSICLALF:
                        getInstChForMUSICLALF(chip, ch, chipId);
                        break;
                    case MUSICLALF2:
                        getInstChForMUSICLALF2(chip, ch, chipId);
                        break;
                    case TFI:
                        getInstChForTFI(chip, ch, chipId);
                        break;
                    case NRTDRV:
                        getInstChForNRTDRV(chip, ch, chipId);
                        break;
                    case HUSIC:
                        getInstChForHuSIC(chip, ch, chipId);
                        break;
                    case VOPM:
                        getInstChForVOPM(chip, ch, chipId);
                        break;
                    case PMD:
                        getInstChForPMD(chip, ch, chipId);
                        break;
                    case DMP:
                        getInstChForDMP(chip, ch, chipId);
                        break;
                    case OPNI:
                        getInstChForOPNI(chip, ch, chipId);
                        break;
                    case RYM2612:
                        getInstChForRYM2612(chip, ch, chipId);
                        break;
                    case SendMML2VGM:
                        getInstChForSendMML2VGM(chip, ch, chipId);
                        break;
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Sound output error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getInstChForFMP7(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class ?
                          new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null} :
                          (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("'@ FA xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,        // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,        // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,        // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f,        // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7  // AM
                ));
            }
            n.append("   ALG FB\n");
            n.append("'@ %3d,%3d\n".formatted(
                    fmRegister[p][0xb0 + c] & 0x07, // AL
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3 // FB
            ));
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");
            n.append("'@ FC xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AM\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,        // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,        // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,        // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f,        // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
            n.append("   ALG FB\n");
            n.append("'@ %3d,%3d\n".formatted(
                    ym2151Register[0x20 + ch] & 0x07,  // AL
                    (ym2151Register[0x20 + ch] & 0x38) >> 3 // FB
            ));
        }

        if (!n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForMDX(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("'@xx = {\n/* AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("   %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,        // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,        // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,        // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f,        // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        0,
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7  // AM
                ));
            }
            n.append("/* ALG FB  OP\n");
            n.append("   %3d,%3d,15\n}}\n".formatted(
                    fmRegister[p][0xb0 + c] & 0x07, // AL
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3  // FB
            ));
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("'@xx = {\n/* AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("   %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,        // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,        // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,        // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f,        // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
            n.append("/* ALG FB  OP\n");
            n.append("   %3d,%3d,15\n}}\n".formatted(
                    ym2151Register[0x20 + ch] & 0x07, // AL
                    (ym2151Register[0x20 + ch] & 0x38) >> 3  // FB
            ));
        }

        if (!n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForMML2VGM(Class<? extends Chip> chip, int ch, int chipId) {
        String n = getInstChForMML2VGMFormat(chip, ch, chipId);
        if (n != null && n.isEmpty()) Common.setClipboard(n);
    }

    private void getInstChForSendMML2VGM(Class<? extends Chip> chip, int ch, int chipId) {
        String n = getInstChForMML2VGMFormat(chip, ch, chipId);
        if (n == null || !n.isEmpty()) return;

        MmfControl mmf = new MmfControl(true, "mml2vgmFMVoicePool", 1024 * 4);
        try {
            mmf.sendMessage(String.join(":", "SendVoice", n));
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.TRACE, "Message too long");
        } catch (UncheckedIOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Could not find shared memory for mml2vgm");
        }
    }

    private static final int[] slot1Tbl = {0, 1, 2, 6, 7, 8, 12, 13, 14};
    private static final int[] slot2Tbl = {3, 4, 5, 9, 10, 11, 15, 16, 17};

    private String getInstChForMML2VGMFormat(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("'@ N xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM  SSG-EG\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,        // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,        // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,        // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f,        // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7, // AM
                        fmRegister[p][0x90 + ops + c] & 0x0f         // SG
                ));
            }
            n.append("   ALG FB\n");
            n.append("'@ %3d,%3d\n".formatted(
                    fmRegister[p][0xb0 + c] & 0x07, // AL
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3  // FB
            ));
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");
            n.append("'@ M xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,        // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,        // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,        // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f,        // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT1
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
            n.append("   ALG FB\n");
            n.append("'@ %3d,%3d\n".formatted(
                    ym2151Register[0x20 + ch] & 0x07, // AL
                    (ym2151Register[0x20 + ch] & 0x38) >> 3  // FB
            ));
        } else if (chip == HuC6280Chip.class) {
            Map<String, Object> huc6280Register = audio.plugin.chipRegister.chip(HuC6280Chip.class).getInfo(chipId);
            if (huc6280Register == null) return null;
            if (huc6280Register.get("channels." + ch + ".wave") == null) return null;
            int[] wave = (int[]) huc6280Register.get("channels." + ch + ".wave");
            if (wave.length != 32) return null;

            n.append("'@ H xx,\n   +0 +1 +2 +3 +4 +5 +6 +7\n");

            for (int i = 0; i < 32; i += 8) {
                n.append("'@ %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d\n".formatted(
                        (17 - wave[i + 0]),
                        (17 - wave[i + 1]),
                        (17 - wave[i + 2]),
                        (17 - wave[i + 3]),
                        (17 - wave[i + 4]),
                        (17 - wave[i + 5]),
                        (17 - wave[i + 6]),
                        (17 - wave[i + 7])
                ));
            }
        } else if (chip == Ym2413Chip.class) {
            // Ym2413
            int[] regs = (int[]) audio.plugin.chipRegister.chip(Ym2413Chip.class).getInfo(chipId).get("register");
        } else if (chip == Ym3812Chip.class) {
            // OPL2
            // '@ L No "Name"
            // '@ AR DR SL RR KSL TL MT AM VIB EGT KSR WS
            // '@ AR DR SL RR KSL TL MT AM VIB EGT KSR WS
            // '@ CNT FB

            int[] regs = (int[]) audio.plugin.chipRegister.chip(Ym3812Chip.class).getInfo(chipId).get("register");
            int slot;
            if (ch < 0 || ch > 8) return null;

            n.append("'@ L No \"MDP\"\n   AR DR SL RR KSL TL MT AM VIB EGT KSR WS\n");
            for (int i = 0; i < 2; i++) {
                if (i == 0) slot = slot1Tbl[ch];
                else slot = slot2Tbl[ch];

                slot = (slot % 6) + 8 * (slot / 6);
                n.append("'@ %2d,%2d,%2d,%2d, %2d,%2d,%2d,%2d, %2d, %2d, %2d,%2d\n".formatted(
                        regs[0x60 + slot] >> 4,
                        regs[0x60 + slot] & 0xf,
                        regs[0x80 + slot] >> 4,
                        regs[0x80 + slot] & 0xf,
                        regs[0x40 + slot] >> 6,
                        regs[0x40 + slot] & 0x3f,
                        regs[0x20 + slot] & 0xf,
                        regs[0x20 + slot] >> 7,
                        (regs[0x20 + slot] >> 6) & 1,
                        (regs[0x20 + slot] >> 5) & 1,
                        (regs[0x20 + slot] >> 4) & 1,
                        (regs[0xe0 + slot] & 3)
                ));
            }
            n.append("   CNT FB\n'@  %2d,%2d\n".formatted(
                    (regs[0xc0 + ch] & 1),
                    (regs[0xc0 + ch] >> 1) & 7
            ));
        }

        return n.toString();
    }

    private void getInstChForMUSICLALF(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("  @xx:{{\n  %3d %3d\n".formatted(
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3, // FB
                    fmRegister[p][0xb0 + c] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("  %3d %3d %3d %3d %3d %3d %3d %3d %3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, //A R
                        fmRegister[p][0x60 + ops + c] & 0x1f, // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f, // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f, // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f, // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f, // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4 // DT
                ));
            }
            n.append("  }\n");
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("  @xx:{{\n  %3d %3d\n".formatted(
                    (ym2151Register[0x20 + ch] & 0x38) >> 3, // FB
                    ym2151Register[0x20 + ch] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("  %3d %3d %3d %3d %3d %3d %3d %3d %3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f, // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f, // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f, // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f, // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f, // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 // DT
                ));
            }
            n.append("  }\n");
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForMucom88(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("  @xx:{{\n  %3d, %3d\n".formatted(
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3, // FB
                    fmRegister[p][0xb0 + c] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(("  %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d" + (i != 3 ? "\n" : "")).formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f, // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f, // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f, // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f, // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f, // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4 // DT
                ));
            }
            n.append(",\"MDP\"  }\n");
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("  @xx:{{\n  %3d, %3d\n".formatted(
                    (ym2151Register[0x20 + ch] & 0x38) >> 3, // FB
                    ym2151Register[0x20 + ch] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(("  %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d" + (i != 3 ? "\n" : "")).formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f, // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f, // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f, // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f, // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f, // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 // DT
                ));
            }
            n.append(",\"MDP\"  }\n");
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForMUSICLALF2(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("@%xxx\n");

            for (int i = 0; i < 6; i++) {
                n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                        fmRegister[p][0x30 + 0 + c + i * 0x10] & 0xff,
                        fmRegister[p][0x30 + 8 + c + i * 0x10] & 0xff,
                        fmRegister[p][0x30 + 16 + c + i * 0x10] & 0xff,
                        fmRegister[p][0x30 + 24 + c + i * 0x10] & 0xff
                ));
            }
            n.append("$%3x\n".formatted(
                    fmRegister[p][0xb0 + c] // FB/AL
            ));
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("@%xxx\n");

            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0x40 + 0 + ch] & 0x7f),  // DT/ML
                    (ym2151Register[0x40 + 8 + ch] & 0x7f),  // DT/ML
                    (ym2151Register[0x40 + 16 + ch] & 0x7f), // DT/ML
                    (ym2151Register[0x40 + 24 + ch] & 0x7f)  // DT/ML
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0x60 + 0 + ch] & 0x7f),  // TL
                    (ym2151Register[0x60 + 8 + ch] & 0x7f),  // TL
                    (ym2151Register[0x60 + 16 + ch] & 0x7f), // TL
                    (ym2151Register[0x60 + 24 + ch] & 0x7f)  // TL
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0x80 + 0 + ch] & 0xdf),  // KS/AR
                    (ym2151Register[0x80 + 8 + ch] & 0xdf),  // KS/AR
                    (ym2151Register[0x80 + 16 + ch] & 0xdf), // KS/AR
                    (ym2151Register[0x80 + 24 + ch] & 0xdf)  // KS/AR
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0xa0 + 0 + ch] & 0x9f),  // AM/DR
                    (ym2151Register[0xa0 + 8 + ch] & 0x9f),  // AM/DR
                    (ym2151Register[0xa0 + 16 + ch] & 0x9f), // AM/DR
                    (ym2151Register[0xa0 + 24 + ch] & 0x9f)  // AM/DR
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0xc0 + 0 + ch] & 0x1f),  // SR
                    (ym2151Register[0xc0 + 8 + ch] & 0x1f),  // SR
                    (ym2151Register[0xc0 + 16 + ch] & 0x1f), // SR
                    (ym2151Register[0xc0 + 24 + ch] & 0x1f)  // SR
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0xe0 + 0 + ch] & 0xff),  // SL/RR
                    (ym2151Register[0xe0 + 8 + ch] & 0xff),  // SL/RR
                    (ym2151Register[0xe0 + 16 + ch] & 0xff), // SL/RR
                    (ym2151Register[0xe0 + 24 + ch] & 0xff)  // SL/RR
            ));

            n.append("$%3x\n".formatted(ym2151Register[0x20 + ch])); // FB/AL
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForNRTDRV(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("@ xxxx {\n");
            n.append("000,%3d,%3d,015\n".formatted(
                    fmRegister[p][0xb0 + c] & 0x07, // AL
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3 // FB
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(" %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,        // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,        // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,        // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f,        // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        0,
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7  // AM
                ));
            }
            n.append("}\n");
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("@ xxxx {\n");
            n.append("000,%3d,%3d,015\n".formatted(
                    ym2151Register[0x20 + ch] & 0x07, // AL
                    (ym2151Register[0x20 + ch] & 0x38) >> 3 // FB
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(" %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,        // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,        // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,        // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f,        // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
            n.append("}\n");
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForHuSIC(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == HuC6280Chip.class) {
            Map<String, Object> huc6280Register = audio.plugin.chipRegister.chip(HuC6280Chip.class).getInfo(chipId);
            if (huc6280Register == null) return;
            if (huc6280Register.get("channels." + ch + ".wave") == null) return;
            int[] wave = (int[]) huc6280Register.get("channels." + ch + ".wave");
            if (wave.length != 32) return;

            n.append("@WTx={\n");

            for (int i = 0; i < 32; i += 8) {
                n.append("$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,\n".formatted(
                        (17 - wave[i + 0]),
                        (17 - wave[i + 1]),
                        (17 - wave[i + 2]),
                        (17 - wave[i + 3]),
                        (17 - wave[i + 4]),
                        (17 - wave[i + 5]),
                        (17 - wave[i + 6]),
                        (17 - wave[i + 7])
                ));
            }

            n = new StringBuilder(n.substring(0, n.length() - 3) + "\n}\n");
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForMGSC(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();
        int[] register = null;

        if (chip == Ym2413Chip.class) {
            register = (int[]) audio.plugin.chipRegister.chip(Ym2413Chip.class).getInfo(chipId).get("register");
        } else if (chip == Vrc7Chip.class) {
            int[] r = audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).readVrc7(chipId);
            if (r == null) return;
            register = new int[r.length];
            System.arraycopy(r, 0, register, 0, r.length);
        } else if (chip == K051649Chip.class) {
            getInstChForMGSCSCC(ch, chipId);
            return;
        }

        if (register == null) return;
        n.append("@vXX = { \n");
        n.append("   ;       TL FB\n");
        n.append("           %2d,%2d,\n".formatted(register[0x02] & 0x3f, register[0x03] & 0x7));
        n.append("   ;       AR DR SL RR KL MT AM VB EG KR DT\n");

        n.append("           %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,\n".formatted(
                (register[0x04] & 0xf0) >> 4,
                (register[0x04] & 0x0f),
                (register[0x06] & 0xf0) >> 4,
                (register[0x06] & 0x0f),
                (register[0x02] & 0xc0) >> 6,
                (register[0x00] & 0x0f),
                (register[0x00] & 0x80) >> 7,
                (register[0x00] & 0x40) >> 6,
                (register[0x00] & 0x20) >> 5,
                (register[0x00] & 0x10) >> 4,
                (register[0x03] & 0x08) >> 3
        ));

        n.append("           %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d }}\n".formatted(
                (register[0x05] & 0xf0) >> 4,
                (register[0x05] & 0x0f),
                (register[0x07] & 0xf0) >> 4,
                (register[0x07] & 0x0f),
                (register[0x03] & 0xc0) >> 6,
                (register[0x01] & 0x0f),
                (register[0x01] & 0x80) >> 7,
                (register[0x01] & 0x40) >> 6,
                (register[0x01] & 0x20) >> 5,
                (register[0x01] & 0x10) >> 4,
                (register[0x03] & 0x10) >> 4
        ));

        Common.setClipboard(n.toString());
    }

    private void getInstChForMGSCSCC(int ch, int chipId) {
        Map<String, Object> chip = audio.plugin.chipRegister.chip(K051649Chip.class).getInfo(chipId);
        if (chip == null) return;
        int[] register = new int[32];
        for (int i = 0; i < 32; i++) register[i] = (int) chip.get("channels." + ch + ".inst." + i);

        StringBuilder n = new StringBuilder("@sXX = {");
        for (int i = 0; i < 8; i++) {
            n.append(" %02x%02x%02x%02x".formatted(
                    (byte) register[i * 4 + 0], (byte) register[i * 4 + 1],
                    (byte) register[i * 4 + 2], (byte) register[i * 4 + 3]
            ));
        }
        n.append(" }\n");

        Common.setClipboard(n.toString());
    }

    private void getInstChForMGSCSCCPLAIN(int ch, int chipId) {
        Map<String, Object> chip = audio.plugin.chipRegister.chip(K051649Chip.class).getInfo(chipId);
        if (chip == null) return;
        int[] register = new int[32];
        for (int i = 0; i < 32; i++) register[i] = (int) chip.get("channels." + ch + ".inst." + i);

        StringBuilder n = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            n.append("%2x%2x%2x%2x".formatted(
                    (byte) register[i * 4 + 0], (byte) register[i * 4 + 1],
                    (byte) register[i * 4 + 2], (byte) register[i * 4 + 3]
            ));
        }
        n.append("\n");

        Common.setClipboard(n.toString());
    }

    private void getInstChForMCK(Class<? extends Chip> chip, int ch, int chipId) {
        if (chip == N163Chip.class) {
            NesN106.TrackInfo[] info = (NesN106.TrackInfo[]) audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).readN163(0);
            if (info == null) return;

            StringBuilder n = new StringBuilder("@Nxx = { ");
            n.append("%d ".formatted(info[ch].waveLen));
            for (int i = 0; i < info[ch].waveLen; i++) {
                n.append("%d ".formatted((byte) info[ch].wave[i]));
            }
            n.append("}\n");

            Common.setClipboard(n.toString());
        }
    }

    private void getInstChForTFI(Class<? extends Chip> chip, int ch, int chipId) {

        byte[] n = new byte[42];

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n[0] = (byte) (fmRegister[p][0xb0 + c] & 0x07); // AL
            n[1] = (byte) ((fmRegister[p][0xb0 + c] & 0x38) >> 3); // FB


            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 4 : ((i == 2) ? 8 : 12));
                int ops = i * 4;

                n[i * 10 + 2] = (byte) (fmRegister[p][0x30 + ops + c] & 0x0f); // ML
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4; // DT
                // 0>3  1>4  2>5  3>6  4>3  5>2  6>1  7>0
                dt = (dt < 4) ? (dt + 3) : (7 - dt);
                n[i * 10 + 3] = (byte) dt;
                n[i * 10 + 4] = (byte) (fmRegister[p][0x40 + ops + c] & 0x7f); // TL
                n[i * 10 + 5] = (byte) ((fmRegister[p][0x50 + ops + c] & 0xc0) >> 6); // KS
                n[i * 10 + 6] = (byte) (fmRegister[p][0x50 + ops + c] & 0x1f); // AR
                n[i * 10 + 7] = (byte) (fmRegister[p][0x60 + ops + c] & 0x1f); // DR
                n[i * 10 + 8] = (byte) (fmRegister[p][0x70 + ops + c] & 0x1f); // SR
                n[i * 10 + 9] = (byte) (fmRegister[p][0x80 + ops + c] & 0x0f); // RR
                n[i * 10 + 10] = (byte) ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4); // SL
                n[i * 10 + 11] = (byte) (fmRegister[p][0x90 + ops + c] & 0x0f); // SSG
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n[0] = (byte) (ym2151Register[0x20 + ch] & 0x07); // AL
            n[1] = (byte) ((ym2151Register[0x20 + ch] & 0x38) >> 3); // FB

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;

                n[i * 10 + 2] = (byte) (ym2151Register[0x40 + ops + ch] & 0x0f); // ML
                int dt = ((ym2151Register[0x40 + ops + ch] & 0x70) >> 4); // DT
                // 0>3  1>4  2>5  3>6  4>3  5>2  6>1  7>0
                dt = (dt < 4) ? (dt + 3) : (7 - dt);
                n[i * 10 + 3] = (byte) dt;
                n[i * 10 + 4] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 10 + 5] = (byte) ((ym2151Register[0x80 + ops + ch] & 0xc0) >> 6); // KS
                n[i * 10 + 6] = (byte) (ym2151Register[0x80 + ops + ch] & 0x1f); // AR
                n[i * 10 + 7] = (byte) (ym2151Register[0xa0 + ops + ch] & 0x1f); // DR
                n[i * 10 + 8] = (byte) (ym2151Register[0xc0 + ops + ch] & 0x1f); // SR
                n[i * 10 + 9] = (byte) (ym2151Register[0xe0 + ops + ch] & 0x0f); // RR
                n[i * 10 + 10] = (byte) ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4); // SL
                n[i * 10 + 11] = 0;
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.tfi"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".tfi");
            }

            @Override
            public String getDescription() {
                return "TFI File(*.tfi)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (OutputStream fs = Files.newOutputStream(Path.of(sfd.getSelectedFile().getName()))) {

            fs.write(n, 0, n.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void getInstChForDMP(Class<? extends Chip> chip, int ch, int chipId) {

        byte[] n = new byte[51];
        n[0] = 0x0b; // FILE_VERSION
        n[2] = 0x01; // Instrument Mode(1=FM)

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n[1] = 0x02; // SYSTEM_GENESIS

            n[3] = (byte) (fmRegister[p][0xb4 + c] & 0x03); // LFO (FMS on Ym2612Inst, PMS on YM2151)
            n[4] = (byte) ((fmRegister[p][0xb0 + c] & 0x38) >> 3); // FB
            n[5] = (byte) (fmRegister[p][0xb0 + c] & 0x07); // ALG
            n[6] = (byte) ((fmRegister[p][0xb4 + c] & 0x30) >> 4); // LFO2(AMS on Ym2612Inst, AMS on YM2151)

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 4 : ((i == 2) ? 8 : 12));
                int ops = i * 4;

                n[i * 11 + 7] = (byte) (fmRegister[p][0x30 + ops + c] & 0x0f); // ML
                n[i * 11 + 8] = (byte) (fmRegister[p][0x40 + ops + c] & 0x7f); // TL
                n[i * 11 + 9] = (byte) (fmRegister[p][0x50 + ops + c] & 0x1f); //AR
                n[i * 11 + 10] = (byte) (fmRegister[p][0x60 + ops + c] & 0x1f); //DR
                n[i * 11 + 11] = (byte) ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4); // SL
                n[i * 11 + 12] = (byte) (fmRegister[p][0x80 + ops + c] & 0x0f); //RR
                n[i * 11 + 13] = (byte) ((fmRegister[p][0x60 + ops + c] & 0x80) >> 7); //AM
                n[i * 11 + 14] = (byte) ((fmRegister[p][0x50 + ops + c] & 0xc0) >> 6); // KS
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4; // DT
                dt = (dt == 4) ? 0 : dt;
                // 0>5(-3)  1>6(-2)  2>7(-1)  3>0/4  4>1  5>2  6>3  7>3
                dt = (dt > 4) ? (dt - 5) : (dt + 3);
                n[i * 11 + 15] = (byte) (dt & 7);
                n[i * 11 + 16] = (byte) (fmRegister[p][0x70 + ops + c] & 0x1f); //SR
                n[i * 11 + 17] = (byte) (fmRegister[p][0x90 + ops + c] & 0x0f); // SSG
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n[1] = 0x08; // SYSTEM_YM2151

            n[3] = (byte) ((ym2151Register[0x38 + ch] & 0x70) >> 4); // LFO (FMS on Ym2612Inst, PMS on YM2151)
            n[4] = (byte) ((ym2151Register[0x20 + ch] & 0x38) >> 3); // FB
            n[5] = (byte) (ym2151Register[0x20 + ch] & 0x07); // AL
            n[6] = (byte) (ym2151Register[0x38 + ch] & 0x03); // LFO2(AMS on Ym2612Inst, AMS on YM2151)

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;

                n[i * 11 + 7] = (byte) (ym2151Register[0x40 + ops + ch] & 0x0f); // ML
                n[i * 11 + 8] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 11 + 9] = (byte) (ym2151Register[0x80 + ops + ch] & 0x1f); // AR
                n[i * 11 + 10] = (byte) (ym2151Register[0xa0 + ops + ch] & 0x1f); // DR
                n[i * 11 + 11] = (byte) ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4); // SL
                n[i * 11 + 12] = (byte) (ym2151Register[0xe0 + ops + ch] & 0x0f); // RR
                n[i * 11 + 13] = (byte) ((ym2151Register[0xa0 + ops + ch] & 0x80) >> 7); // AM
                n[i * 11 + 14] = (byte) ((ym2151Register[0x80 + ops + ch] & 0xc0) >> 6); // KS
                int dt = ((ym2151Register[0x40 + ops + ch] & 0x70) >> 4); // DT
                dt = (dt == 4) ? 0 : dt;
                // 0>5(-3)  1>6(-2)  2>7(-1)  3>0/4  4>1  5>2  6>3  7>3
                dt = (dt > 4) ? (dt - 5) : (dt + 3);
                int dt2 = (byte) ((ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6); // DT2
                n[i * 11 + 15] = (byte) ((dt & 0x7) | (dt2 << 4));
                n[i * 11 + 16] = (byte) (ym2151Register[0xc0 + ops + ch] & 0x1f); // SR
                n[i * 11 + 17] = 0;
            }

        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.dmp"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".dmp");
            }

            @Override
            public String getDescription() {
                return "DMP File(*.dmp)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (OutputStream fs = Files.newOutputStream(Path.of(sfd.getSelectedFile().getName()))) {

            fs.write(n, 0, n.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //
    //  I am using the following code for reference. Thank you!
    //
    //  Title:
    //      mucom88torym2612
    //  Author:
    //      千霧＠ぶっちぎりP(but80) 様
    //  URL:
    //      https://github.com/but80/mucom88torym2612/
    //      Github
    //        but80/mucom88torym2612
    //  License:
    //      MIT License
    //
    private void getInstChForRYM2612(Class<? extends Chip> chip, int ch, int chipId) {

        List<String>[] op = new List[] {new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        int[] muls = {0, 1054, 1581, 2635, 3689, 4743, 5797, 6851, 7905, 8959, 10013, 10540, 11594, 12648, 14229, 15000};
        StringBuilder buf = new StringBuilder("<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n");
        buf.append("\n");
        int alg = 0, fb = 0, ams = 0, pms = 0;

        MetaData metaData = (audio.plugin.driverVirtual != null) ? audio.plugin.driverVirtual.metaData : null;
        String patch_Name = "MDPlayer_%d";
        if (metaData != null) {
            String pn = metaData.getFirst(Tag.Title);
            if (pn == null || !pn.isEmpty()) pn = metaData.getFirst(Tag.TitleJ);
            if (pn != null && pn.isEmpty()) {
                patch_Name = pn + "_%d";
            }
        }
        patch_Name = patch_Name.formatted(Instant.now().toEpochMilli());
        buf.append("<RYM2612Params patchName = \"{patch_Name}\" category = \"Piano\" rating = \"3\" type = \"User\" >\n");

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")
            ));

            alg = (fmRegister[p][0xb0 + c] & 0x07) >> 0;
            fb = (fmRegister[p][0xb0 + c] & 0x38) >> 3;
            ams = (fmRegister[p][0xb4 + c] & 0x30) >> 4;
            pms = (fmRegister[p][0xb4 + c] & 0x07) >> 0;

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                int tl = 127 - ((fmRegister[p][0x40 + ops + c] & 0x7f) >> 0);
                int vel = 0;
                int ssg = fmRegister[p][0x90 + ops + c] & 0x0f;
                ssg = ((ssg & 0x8) == 0) ? 0 : ((ssg & 0x7) + 1);
                op[i].add("  <PARAM id=\"OP%dVel\" value=\"%d.0\"/>".formatted(i + 1, vel));
                op[i].add("  <PARAM id=\"OP%dTL\" value=\"%d.0\"/>".formatted(i + 1, tl));
                op[i].add("  <PARAM id=\"OP%dSSGEG\" value=\"%d.0\"/>".formatted(i + 1, ssg));
                op[i].add("  <PARAM id=\"OP%dRS\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6));
                op[i].add("  <PARAM id=\"OP%dRR\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x80 + ops + c] & 0x0f) >> 0));
                op[i].add("  <PARAM id=\"OP%dMW\" value=\"0.0\"/>".formatted(i + 1));
                op[i].add("  <PARAM id=\"OP%dMUL\" value=\"%d.0\"/>".formatted(i + 1, muls[(fmRegister[p][0x30 + ops + c] & 0x0f) >> 0]));
                op[i].add("  <PARAM id=\"OP%dFixed\" value=\"0.0\"/>".formatted(i + 1));
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4;
                dt = (dt >= 4) ? (4 - dt) : dt;
                op[i].add("  <PARAM id=\"OP%dDT\" value=\"%d.0\"/>".formatted(i + 1, dt));
                op[i].add("  <PARAM id=\"OP%dD2R\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x70 + ops + c] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dD2L\" value=\"%d.0\"/>".formatted(i + 1, 15 - ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4)));
                op[i].add("  <PARAM id=\"OP%dD1R\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x60 + ops + c] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAR\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x50 + ops + c] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAM\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x60 + ops + c] & 0x80) >> 7));
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            alg = (ym2151Register[0x20 + ch] & 0x07) >> 0;
            fb = (ym2151Register[0x20 + ch] & 0x38) >> 3;
            ams = (ym2151Register[0x38 + ch] & 0x03) >> 0;
            pms = (ym2151Register[0x38 + ch] & 0x70) >> 4;

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                int tl = 127 - ((ym2151Register[0x60 + ops + ch] & 0x7f) >> 0);
                int vel = 0;
                op[i].add("  <PARAM id=\"OP%dVel\" value=\"%d.0\"/>".formatted(i + 1, vel));
                op[i].add("  <PARAM id=\"OP%dTL\" value=\"%d.0\"/>".formatted(i + 1, tl));
                op[i].add("  <PARAM id=\"OP%dSSGEG\" value=\"0.0\"/>".formatted(i + 1));
                op[i].add("  <PARAM id=\"OP%dRS\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6));
                op[i].add("  <PARAM id=\"OP%dRR\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xe0 + ops + ch] & 0x0f) >> 0));
                op[i].add("  <PARAM id=\"OP%dMW\" value=\"0.0\"/>".formatted(i + 1));
                op[i].add("  <PARAM id=\"OP%dMUL\" value=\"%d.0\"/>".formatted(i + 1, muls[(ym2151Register[0x40 + ops + ch] & 0x0f) >> 0]));
                op[i].add("  <PARAM id=\"OP%dFixed\" value=\"0.0\"/>".formatted(i + 1));
                int dt = (ym2151Register[0x40 + ops + ch] & 0x70) >> 4;
                dt = (dt >= 4) ? (4 - dt) : dt;
                op[i].add("  <PARAM id=\"OP%dDT\" value=\"%d.0\"/>".formatted(i + 1, dt));
                op[i].add("  <PARAM id=\"OP%dD2R\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xc0 + ops + ch] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dD2L\" value=\"%d.0\"/>".formatted(i + 1, 15 - ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4)));
                op[i].add("  <PARAM id=\"OP%dD1R\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xa0 + ops + ch] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAR\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0x80 + ops + ch] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAM\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7));
            }
        }

        for (int i = 0; i < op[0].size(); i++) {
            buf.append(op[3].get(i)).append("\n");
            buf.append(op[2].get(i)).append("\n");
            buf.append(op[1].get(i)).append("\n");
            buf.append(op[0].get(i)).append("\n");
        }

        buf.append("  <PARAM id=\"volume\" value=\"0.699999988079071\"/>\n"); // -0.00db
        buf.append("  <PARAM id=\"Ladder_Effect\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"Output_Filtering\" value=\"1.0\"/>\n"); // Crystal clear
        buf.append("  <PARAM id=\"Polyphony\" value=\"6.0\"/>\n");
        buf.append("  <PARAM id=\"timerA\" value=\"0.0\"/>\n"); // RETRIG RATE 1200
        buf.append("  <PARAM id=\"Spec_Mode\" value=\"2.0\"/>\n"); // 1.0:float mode  2.0:int mode
        buf.append("  <PARAM id=\"Pitchbend_Range\" value=\"2.0\"/>\n");
        buf.append("  <PARAM id=\"Legato_Retrig\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"LFO_Speed\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"LFO_Enable\" value=\"%d.0\"/>\n".formatted((pms != 0 || ams != 0) ? 1 : 0));
        buf.append("  <PARAM id=\"Feedback\" value=\"%d.0\"/>\n".formatted(fb));
        buf.append("  <PARAM id=\"FMSMW\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"FMS\" value=\"%d.0\"/>\n".formatted(pms));
        buf.append("  <PARAM id=\"DAC_Prescaler\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"Algorithm\" value=\"%d.0\"/>\n".formatted(alg + 1));
        buf.append("  <PARAM id=\"AMS\" value=\"%d.0\"/>\n".formatted(ams));
        buf.append("  <PARAM id=\"masterTune\"/>\n");
        buf.append("</RYM2612Params>\n");


        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("{patch_Name}.rym2612"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".rym2612");
            }

            @Override
            public String getDescription() {
                return "RYM2612 File(*.rym2612";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (var sw = new PrintWriter(Files.newOutputStream(Path.of(sfd.getSelectedFile().getName())))) {
            sw.write(buf.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void getInstChForOPNI(Class<? extends Chip> chip, int ch, int chipId) {

        byte[] n = new byte[77];
        Arrays.fill(n, 0, n.length, (byte) 0);
        byte[] data = "WOPN2-INST".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 0, 10);
        n[10] = 0x00;
        n[11] = 0x00; // 0 - melodic, or 1 - percussion
        data = "MDPlayer".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 12, 8);
        n[12 + 32 + 0] = 0x00; // Big-Endian 16-bit signed integer, MIDI key offset value
        n[12 + 32 + 1] = 0x00;
        n[12 + 32 + 2] = 0x00; // 8-bit unsigned integer, Percussion instrument key number

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class ?
                          new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n[12 + 32 + 3] = (byte) (fmRegister[p][0xb0 + c] & 0x3f); // FB & ALG
            n[12 + 32 + 4] = 0x10; // 0x00:OPN2  0x10:OPNA

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 4 : ((i == 2) ? 8 : 12));
                int ops = i * 4;
                n[i * 7 + 12 + 32 + 5] = (byte) fmRegister[p][0x30 + ops + c]; // DT & ML
                n[i * 7 + 12 + 32 + 6] = (byte) (fmRegister[p][0x40 + ops + c] & 0x7f); // TL
                n[i * 7 + 12 + 32 + 7] = (byte) fmRegister[p][0x50 + ops + c]; // KS & AR
                n[i * 7 + 12 + 32 + 8] = (byte) fmRegister[p][0x60 + ops + c]; //AM & DR
                n[i * 7 + 12 + 32 + 9] = (byte) fmRegister[p][0x70 + ops + c]; //SR
                n[i * 7 + 12 + 32 + 10] = (byte) fmRegister[p][0x80 + ops + c]; // SL&RR
                n[i * 7 + 12 + 32 + 11] = (byte) fmRegister[p][0x90 + ops + c]; // SSG
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n[12 + 32 + 3] = (byte) ym2151Register[0x20 + ch]; // FB & ALG
            n[12 + 32 + 4] = 0x10; // 0x00:OPN2  0x10:OPNA

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;
                n[i * 7 + 12 + 32 + 5] = (byte) ym2151Register[0x40 + ops + ch]; // DT & ML
                n[i * 7 + 12 + 32 + 6] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 7 + 12 + 32 + 7] = (byte) ym2151Register[0x80 + ops + ch]; // KS & AR
                n[i * 7 + 12 + 32 + 8] = (byte) ym2151Register[0xa0 + ops + ch]; // AME DR
                n[i * 7 + 12 + 32 + 9] = (byte) ym2151Register[0xc0 + ops + ch]; // SR
                n[i * 7 + 12 + 32 + 10] = (byte) ym2151Register[0xe0 + ops + ch]; // SL&RR
                n[i * 7 + 12 + 32 + 11] = 0; // SSG

                //int dt2 = (byte)((ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6); //DT2
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.opni"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".opni");
            }

            @Override
            public String getDescription() {
                return "OPNI File(*.opni)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (OutputStream fs = Files.newOutputStream(Path.of(sfd.getSelectedFile().getName()))) {
            fs.write(n, 0, n.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void getInstChForOPLI(Class<? extends Chip> chip, int ch, int chipId) {
        if (chip != Ym3812Chip.class && chip != YmF262Chip.class && chip != YmF278BChip.class) return;

        int[][] reg;
        if (chip == YmF262Chip.class)
            reg = (int[][]) audio.plugin.chipRegister.chip(YmF262Chip.class).getInfo(chipId).get("register");
        else if (chip == YmF278BChip.class)
            reg = (int[][]) audio.plugin.chipRegister.chip(YmF278BChip.class).getInfo(chipId).get("register");
        else {
            int[] r = (int[]) audio.plugin.chipRegister.chip(Ym3812Chip.class).getInfo(chipId).get("register");
            reg = new int[1][];
            reg[0] = r;
        }

        byte[] n = new byte[76];
        Arrays.fill(n, 0, n.length, (byte) 0);
        byte[] data = "WOPL3-INST".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 0, 10);
        n[10] = 0x00;
        n[11] = 0x02; // Version 16bit-Integer LE
        n[12] = 0x00;
        n[13] = 0x00; // 0 - melodic, or 1 - percussion

        data = "MDPlayer".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 14, 8);
        n[14 + 32 + 0] = 0x00; // (mstr)Big-Endian 16-bit signed integer, MIDI key offset value
        n[14 + 32 + 1] = 0x00;
        n[14 + 32 + 2] = 0x00; // (sec)Big-Endian 16-bit signed integer, MIDI key offset value
        n[14 + 32 + 3] = 0x00;
        n[14 + 32 + 4] = 0x00; // 8-bit signed integer, MIDI Velocity offset
        n[14 + 32 + 5] = 0x00; // 8-bit signed integer, Second voice detune
        n[14 + 32 + 6] = 0x00; // 8-bit unsigned integer, Percussion instrument key number

        int[] op = {0, 0, 0, 0};
        boolean isOP4 = false;
        int c = ch;
        if (ch < 6) {
            c -= ch % 2;
            op[0] = c / 2;
            op[1] = op[0] + 3;
            op[2] = op[0] + 6;
            op[3] = op[0] + 9;
            isOP4 = (chip == Ym3812Chip.class) ? false : ((reg[1][0x04] & (0x1 << c)) != 0);
            if (!isOP4 && ch % 2 != 0) {
                c = ch;
                op[0] = op[2];
                op[1] = op[3];
            }
        } else if (ch < 9) {
            op[0] = ch + 6;
            op[1] = op[0] + 3;
            isOP4 = false;
        } else if (ch < 15) {
            c -= (ch - 9) % 2;
            op[0] = (c - 9) / 2 + 18;
            op[1] = op[0] + 3;
            op[2] = op[0] + 6;
            op[3] = op[0] + 9;
            isOP4 = (reg[1][0x04] & (0x1 << ((c - 9) + 3))) != 0;
            if (!isOP4 && (ch - 9) % 2 != 0) {
                c = ch;
                op[0] = op[2];
                op[1] = op[3];
            }
        } else if (ch < 18) {
            op[0] = ch + 15;
            op[1] = op[0] + 3;
            isOP4 = false;
        }

        n[14 + 32 + 7] = (byte) (isOP4 ? 1 : 0);

        int p = c / 9;
        //int adr = c % 9;
        int[] chTbl = {0, 3, 1, 4, 2, 5, 6, 7, 8};
        //adr = chTbl[adr];

        for (int i = 0; i < 4; i++) {
            op[i] -= (op[i] / 18) * 18;
            op[i] = (op[i] % 6) + 8 * (op[i] / 6);
        }

        // OPLI has op1<->op2 and op3<->op4 reversed (?)
        for (int i = 0; i < 2; i++) {
            int s = op[i * 2];
            op[i * 2] = op[i * 2 + 1];
            op[i * 2 + 1] = s;
        }

        if (!isOP4) {
            n[14 + 32 + 8] = (byte) (reg[p][0xc0 + chTbl[c % 9]]); // 8-bit unsigned integer, Feedback / Connection
            n[14 + 32 + 9] = 0x00;
        } else {
            n[14 + 32 + 8] = (byte) (reg[p][0xc0 + chTbl[c % 9]]); // 8-bit unsigned integer, Feedback / Connection
            n[14 + 32 + 9] = (byte) (reg[p][0xc0 + chTbl[(c + 1) % 9]]); // 8-bit unsigned integer, Feedback / Connection
        }

        for (int i = 0; i < 4; i++) {
            if (isOP4 || i < 2) {
                n[14 + 32 + 10 + i * 5] = (byte) reg[p][0x20 + op[i]]; // AM/Vib/Env/Ksr/FMult characteristics
                n[14 + 32 + 11 + i * 5] = (byte) reg[p][0x40 + op[i]]; // Key Scale Level / Total level register data
                n[14 + 32 + 12 + i * 5] = (byte) reg[p][0x60 + op[i]]; // Attack / Decay
                n[14 + 32 + 13 + i * 5] = (byte) reg[p][0x80 + op[i]]; // Sustain and Release register data
                n[14 + 32 + 14 + i * 5] = (byte) reg[p][0xe0 + op[i]]; // WS
            } else {
                n[14 + 32 + 10 + i * 5] = 0x00; // AM/Vib/Env/Ksr/FMult characteristics
                n[14 + 32 + 11 + i * 5] = 0x00; // Key Scale Level / Total level register data
                n[14 + 32 + 12 + i * 5] = 0x00; // Attack / Decay
                n[14 + 32 + 13 + i * 5] = 0x00; // Sustain and Release register data
                n[14 + 32 + 14 + i * 5] = 0x00; // WS
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.opli"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".opli");
            }

            @Override
            public String getDescription() {
                return "OPLI File(*.opli)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (OutputStream fs = Files.newOutputStream(Path.of(sfd.getSelectedFile().getName()))) {

            fs.write(n, 0, n.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void getInstChForVOPM(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class) ?
                    (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class ?
                          new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("@: n MDPlayer\n");
            n.append("LFO:  0   0   0   0   0\n");
            n.append("CH: 64  %2d  %2d   0   0 120   0\n".formatted(
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3, // FB
                    fmRegister[p][0xb0 + c] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("%s:%3d %3d %3d %3d %3d ".formatted(
                        "M1C1M2C2".substring(i * 2, 2),
                        fmRegister[p][0x50 + ops + c] & 0x1f,       // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,       // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,       // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,       // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4 // SL
                ));
                n.append("%3d %3d %3d %3d   0 %3d\n".formatted(
                        fmRegister[p][0x40 + ops + c] & 0x7f, // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7  // AM
                ));
            }
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("@: n MDPlayer\n");
            n.append("LFO:  0   0   0   0   0\n");
            n.append("CH: 64  %2d  %2d   0   0 120   0\n".formatted(
                    (ym2151Register[0x20 + ch] & 0x38) >> 3, // FB
                    ym2151Register[0x20 + ch] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("%s:%3d %3d %3d %3d %3d ".formatted(
                        "M1C1M2C2".substring(i * 2, 2),
                        ym2151Register[0x80 + ops + ch] & 0x1f,       // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,       // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,       // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,       // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 // SL
                ));
                n.append("%3d %3d %3d %3d %3d %3d\n".formatted(
                        ym2151Register[0x60 + ops + ch] & 0x7f, // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    private void getInstChForPMD(Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("; nm alg fbl\n");
            n.append("@xxx %3d %3d                            =      MDPlayer\n".formatted(
                    fmRegister[p][0xb0 + c] & 0x07, // AL
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3  // FB
            ));
            n.append("; ar  dr  sr  rr  sl  tl  ks  ml  dt ams   seg\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(" %3d %3d %3d %3d %3d %3d %3d %3d %3d %3d ; %3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,        // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,        // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,        // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f,        // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7, // AM
                        fmRegister[p][0x90 + ops + c] & 0x0f         // SG
                ));
            }
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");
            n.append("; nm alg fbl\n");
            n.append("@xxx %3d %3d                            =      MDPlayer\n".formatted(
                    ym2151Register[0x20 + ch] & 0x07, // AL
                    (ym2151Register[0x20 + ch] & 0x38) >> 3  // FB
            ));
            n.append("; ar  dr  sr  rr  sl  tl  ks  ml  dt ams   seg\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(" %3d %3d %3d %3d %3d %3d %3d %3d %3d %3d ; %3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f,   // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,          // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,          // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,          // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4,   // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f,          // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6,   // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,          // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4,   // DT
                        //(ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7,   // AM
                        0
                ));
            }
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }

    public boolean loadAndPlay(int m, int songNo, String fn, String zfn /* = null */) {
        try {
            // the plugin of the song played so far; there is none before the first one
            if (audio.plugin != null && audio.plugin.flgReinit) flgReinit = true;
            if (setting.getOther().getInitAlways()) flgReinit = true;
            reinit(setting);

            if (audio.isPaused()) {
                audio.pause();
            }

            String playingFileName = fn;
            String playingArcFileName = "";
            FileFormat format = FileFormat.getFileFormat(fn);
            List<Tuple<String, byte[]>> extFile;

            if (zfn != null && !zfn.isEmpty()) {
                playingArcFileName = zfn;
                playingFileName = fn;
                format = FileFormat.getFileFormat(zfn);
            }
            // .vgz and friends are compressed: reading the file raw fails the format's header check
            format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(fn)))), null);

            // Set the volume balance before playback
            loadPresetMixerBalance(playingFileName, playingArcFileName, format);

            BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
            plugin.setParams(format, Map.of(
                    "fileName", playingFileName,
                    "arcFileName", playingArcFileName,
                    "midiMode", m,
                    "songNo", songNo)
            );
            plugin.init();
            audio.init(plugin);
            audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[0].fileFormat = format;
            audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[1].fileFormat = format;

            SwingUtilities.invokeLater(this::playData);

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to load file.\nMessage=%s".formatted(ex.getMessage()),
                    "MDPlayer", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean bufferPlay(byte[] buf, String fullPath) {
        try {
            // the plugin of the song played so far; there is none before the first one
            if (audio.plugin != null && audio.plugin.flgReinit) flgReinit = true;
            if (setting.getOther().getInitAlways()) flgReinit = true;
            reinit(setting);

            if (audio.isPaused()) {
                audio.pause();
            }

            String playingFileName = fullPath;
            String playingArcFileName = "";
            FileFormat format = FileFormat.getFileFormat(fullPath);
            format.load(new ByteArrayInputStream(buf), null);

            // Set the volume balance before playback
            loadPresetMixerBalance(playingFileName, playingArcFileName, format);

            // TODO buf reaches the format, but setParams only names a file, so a plugin that reads
            //  the file itself still needs fullPath to exist. The SPLAY remote (mml2vgm preview)
            //  can pass a buffer with no file behind it.
            BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
            plugin.setParams(format, Map.of(
                    "fileName", playingFileName,
                    "arcFileName", playingArcFileName,
                    "midiMode", 0,
                    "songNo", 0)
            );
            audio.init(plugin);
            audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[0].fileFormat = format;
            audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[1].fileFormat = format;

            SwingUtilities.invokeLater(this::playData);

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to load file.\nMessage=%s".formatted(ex.getMessage()),
                    "MDPlayer", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        frmPlayList.play();
        return true;
    }

    public void setChannelMask(Class<? extends Chip> chip, int chipId, int ch) {
        if (chip.equals(Ym2203Chip.class)) {
            if (ch >= 0 && ch < 9) {
                audio.plugin.chipRegister.chip(Ym2203Chip.class).setMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[ch].mask = true;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[2].mask = true;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[6].mask = true;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[7].mask = true;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[8].mask = true;
                }
            }
        } else if (chip.equals(Ym2413Chip.class)) {
            if (ch >= 0 && ch < 14) {
                if (!audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(Ym2413Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym2413Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask;
            }
        } else if (chip.equals(Ym3526Chip.class)) {
            if (ch >= 0 && ch < 14) {
                if (!audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(Ym3526Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym3526Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId].channels[ch].mask;

            }
        } else if (chip.equals(Y8950Chip.class)) {
            if (ch >= 0 && ch < 15) {
                if (!audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(Y8950Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Y8950Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId].channels[ch].mask;

            }
        } else if (chip.equals(Ym3812Chip.class)) {
            if (ch >= 0 && ch < 14) {
                if (!audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(Ym3812Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym3812Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId].channels[ch].mask;

            }
        } else if (chip.equals(YmF262Chip.class)) {
            if (ch >= 0 && ch < 23) {
                if (!audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(YmF262Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(YmF262Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId].channels[ch].mask;

            }
        } else if (chip.equals(YmF278BChip.class)) {
            if (ch >= 0 && ch < 47) {
                if (!audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(YmF278BChip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(YmF278BChip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId].channels[ch].mask;

            }
        } else if (chip.equals(Ym2608Chip.class)) {
            if (ch >= 0 && ch < 14) {
                audio.plugin.chipRegister.chip(Ym2608Chip.class).setMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[ch].mask = true;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[2].mask = true;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[9].mask = true;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[10].mask = true;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[11].mask = true;
                }
            }
        } else if (chip.equals(Ym2610Chip.class)) {
            if (ch >= 0 && ch < 14) {
                int c = ch;
                if (ch == 12) c = 13;
                if (ch == 13) c = 12;

                audio.plugin.chipRegister.chip(Ym2610Chip.class).setMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[c].mask = true;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[2].mask = true;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[9].mask = true;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[10].mask = true;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[11].mask = true;
                }
            }
        } else if (chip.equals(Ym2612Chip.class)) {
            if (ch >= 0 && ch < 9) {
                audio.plugin.chipRegister.chip(Ym2612Chip.class).setMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[ch].mask = true;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[2].mask = true;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[6].mask = true;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[7].mask = true;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[8].mask = true;
                }
            }
        } else if (chip.equals(Sn76489Chip.class)) {
            if (!audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(Sn76489Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(Sn76489Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask;
        } else if (chip.equals(Rf5C164Chip.class)) {
            if (!audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(Rf5C164Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(Rf5C164Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask;
        } else if (chip.equals(Rf5C68Chip.class)) {
            if (!audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(Rf5C68Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(Rf5C68Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask;
        } else if (chip.equals(Ym2151Chip.class)) {
            if (!audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(Ym2151Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(Ym2151Chip.class).resetMask(chipId, ch, audio.plugin.stopped);
            }
            audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask;
        } else if (chip.equals(C140Chip.class)) {
            if (!audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(C140Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(C140Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask;
        } else if (chip.equals(Ppz8Chip.class)) {
            if (!audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(Ppz8Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(Ppz8Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId].channels[ch].mask;
        } else if (chip.equals(C352Chip.class)) {
            if (!audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(C352Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(C352Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask;
        } else if (chip.equals(SegaPcmChip.class)) {
            if (!audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(SegaPcmChip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(SegaPcmChip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask;
        } else if (chip.equals(QSoundChip.class)) {
            if (!audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(QSoundChip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(QSoundChip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask;
        } else if (chip.equals(Ay8910Chip.class)) {
            if (!audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(Ay8910Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(Ay8910Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask;
        } else if (chip.equals(HuC6280Chip.class)) {
            if (!audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(HuC6280Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(HuC6280Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask;
        } else if (chip.equals(OkiM6258Chip.class)) {
            if (!audio.plugin.chipRegister.chip(OkiM6258Chip.class).okim6258[chipId].mask || audio.plugin.chipRegister.chip(OkiM6258Chip.class).okim6258[chipId].mask == null) {
                audio.plugin.chipRegister.chip(OkiM6258Chip.class).setMask(chipId);
            } else {
                audio.plugin.chipRegister.chip(OkiM6258Chip.class).resetMask(chipId);
            }
            audio.plugin.chipRegister.chip(OkiM6258Chip.class).okim6258[chipId].mask = !audio.plugin.chipRegister.chip(OkiM6258Chip.class).okim6258[chipId].mask;
        } else if (chip.equals(OkiM6295Chip.class)) {
            if (!audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask == null) {
                audio.plugin.chipRegister.chip(OkiM6295Chip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(OkiM6295Chip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask;
        } else if (chip.equals(NesChip.class)) {
            if (!audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask || audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask == null) {
                audio.plugin.chipRegister.chip(NesChip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(NesChip.class).resetMask(chipId, ch);
            }
            audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask = !audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask;
        } else if (chip.equals(DmcChip.class)) {
            switch (ch) {
                case 0:
                    if (!audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask || audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask == null)
                        audio.plugin.chipRegister.chip(NesChip.DmcChip.class).setDmcMask(chipId, ch);
                    else audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, ch);
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask = !audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask;
                    break;
                case 1:
                    if (!audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask || audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask == null)
                        audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).setDmcMask(chipId, ch);
                    else audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).resetDmcMask(chipId, ch);
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask = !audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask;
                    break;
                case 2:
                    if (!audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask || audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask == null)
                        audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).setDmcMask(chipId, ch);
                    else audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).resetDmcMask(chipId, ch);
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask = !audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask;
                    break;
            }
        } else if (chip.equals(NpNesChip.FdsChip.class)) {
            if (!audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds[chipId].channel.mask || audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds[chipId].channel.mask == null)
                audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).setFdsMask(chipId);
            else audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).resetFdsMask(chipId);
            audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds[chipId].channel.mask = !audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds[chipId].channel.mask;
        } else if (chip.equals(NpNesChip.Mmc5Chip.class)) {
            switch (ch) {
                case 0:
                    if (!audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[0].mask || audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[ch].mask == null)
                        audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).setMmc5Mask(chipId, ch);
                    else audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).resetMmc5Mask(chipId, ch);
                    audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[0].mask = !audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[0].mask;
                    break;
                case 1:
                    if (!audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[1].mask || audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[ch].mask == null)
                        audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).setMmc5Mask(chipId, ch);
                    else audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).resetMmc5Mask(chipId, ch);
                    audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[1].mask = !audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[1].mask;
                    break;
                case 2:
                    if (!audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].pcmChannel.mask || audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].pcmChannel.mask == null)
                        audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).setMmc5Mask(chipId, ch);
                    else audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).resetMmc5Mask(chipId, ch);
                    audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].pcmChannel.mask = !audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].pcmChannel.mask;
                    break;
            }
        } else if (chip.equals(NpNesChip.Vrc7Chip.class)) {
            if (ch >= 0 && ch < 6) {
                if (!audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).vrc7[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).vrc7[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).setVrc7Mask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).resetVrc7Mask(chipId, ch);

                audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).vrc7[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).vrc7[chipId].channels[ch].mask;
            }
        } else if (chip.equals(K051649Chip.class)) {
            if (ch >= 0 && ch < 5) {
                if (!audio.plugin.chipRegister.chip(K051649Chip.class).k051649[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(K051649Chip.class).k051649[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(K051649Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(K051649Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(K051649Chip.class).k051649[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(K051649Chip.class).k051649[chipId].channels[ch].mask;
            }
        } else if (chip.equals(DmgChip.class)) {
            if (ch >= 0 && ch < 4) {
                if (!audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(DmgChip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(DmgChip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask;
            }
        } else if (chip.equals(Vrc6Chip.class)) {
            if (ch >= 0 && ch < 3) {
                if (!audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).vrc6[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).vrc6[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).setVrc6Mask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).resetVrc6Mask(chipId, ch);

                audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).vrc6[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).vrc6[chipId].channels[ch].mask;
            }
        } else if (chip.equals(N163Chip.class)) {
            if (ch >= 0 && ch < 8) {
                if (!audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).n106[chipId].channels[ch].mask || audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).n106[chipId].channels[ch].mask == null)
                    audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).setN163Mask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).resetN163Mask(chipId, ch);

                audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).n106[chipId].channels[ch].mask = !audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).n106[chipId].channels[ch].mask;
            }
        }
    }

    public void resetChannelMask(Class<? extends Chip> chip, int chipId, int ch) {
        if (chip.equals(Sn76489Chip.class)) {
            audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Sn76489Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Rf5C164Chip.class)) {
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Rf5C68Chip.class)) {
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Ym2151Chip.class)) {
            audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Ym2151Chip.class).resetMask(chipId, ch, audio.plugin.stopped);
        } else if (chip.equals(Ym2203Chip.class)) {
            if (ch >= 0 && ch < 9) {
                audio.plugin.chipRegister.chip(Ym2203Chip.class).resetMask(chipId, ch, audio.plugin.stopped);
                audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[ch].mask = false;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[2].mask = false;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[6].mask = false;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[7].mask = false;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[8].mask = false;
                }
            }
        } else if (chip.equals(Ym2413Chip.class)) {
            audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Ym2413Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Vrc7Chip.class)) {
            audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).vrc7[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(NpNesChip.Vrc7Chip.class).resetVrc7Mask(chipId, ch);
        } else if (chip.equals(Ym2608Chip.class)) {
            if (ch >= 0 && ch < 14) {
                audio.plugin.chipRegister.chip(Ym2608Chip.class).resetMask(chipId, ch, audio.plugin.stopped);
                audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[ch].mask = false;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[2].mask = false;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[9].mask = false;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[10].mask = false;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[11].mask = false;
                }
            }
        } else if (chip.equals(Ym2610Chip.class)) {
            if (ch >= 0 && ch < 14) {
                int c = ch;
                if (ch == 12) c = 13;
                if (ch == 13) c = 12;

                audio.plugin.chipRegister.chip(Ym2610Chip.class).resetMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[c].mask = false;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[2].mask = false;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[9].mask = false;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[10].mask = false;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[11].mask = false;
                }
            }
        } else if (chip.equals(Ym2612Chip.class)) {
            if (ch >= 0 && ch < 9) {
                audio.plugin.chipRegister.chip(Ym2612Chip.class).resetMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[ch].mask = false;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[2].mask = false;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[6].mask = false;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[7].mask = false;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[8].mask = false;
                }
            }
        } else if (chip.equals(Ym3526Chip.class)) {
            audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Ym3526Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Y8950Chip.class)) {
            audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Y8950Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Ym3812Chip.class)) {
            audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Ym3812Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(YmF262Chip.class)) {
            audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(YmF262Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(YmF278BChip.class)) {
            audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(YmF278BChip.class).resetMask(chipId, ch);
        } else if (chip.equals(C140Chip.class)) {
            audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask = false;
            if (ch < 24) audio.plugin.chipRegister.chip(C140Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(Ppz8Chip.class)) {
            audio.plugin.chipRegister.chip(Ppz8Chip.class).ppz8[chipId].channels[ch].mask = false;
            if (ch < 8) audio.plugin.chipRegister.chip(Ppz8Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(C352Chip.class)) {
            audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask = false;
            if (ch < 32) audio.plugin.chipRegister.chip(C352Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(SegaPcmChip.class)) {
            audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask = false;
            if (ch < 16) audio.plugin.chipRegister.chip(SegaPcmChip.class).resetMask(chipId, ch);
        } else if (chip.equals(QSoundChip.class)) {
            audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask = false;
            if (ch < 19) audio.plugin.chipRegister.chip(QSoundChip.class).resetMask(chipId, ch);
        } else if (chip.equals(Ay8910Chip.class)) {
            audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(Ay8910Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(HuC6280Chip.class)) {
            audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(HuC6280Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(K051649Chip.class)) {
            audio.plugin.chipRegister.chip(K051649Chip.class).k051649[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(K051649Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(OkiM6258Chip.class)) {
            audio.plugin.chipRegister.chip(OkiM6258Chip.class).okim6258[chipId].mask = false;
            audio.plugin.chipRegister.chip(OkiM6258Chip.class).resetMask(chipId);
        } else if (chip.equals(OkiM6295Chip.class)) {
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).resetMask(chipId, ch);
        } else if (chip.equals(NesChip.class)) {
            switch (ch) {
                case 0:
                case 1:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask = false;
                    audio.plugin.chipRegister.chip(NesChip.class).resetMask(chipId, ch);
                    break;
                case 2:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask = false;
                    audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 0);
                    break;
                case 3:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask = false;
                    audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 1);
                    break;
                case 4:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask = false;
                    audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 2);
                    break;
            }
        } else if (chip.equals(DmcChip.class)) {
            switch (ch) {
                case 0:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask = false;
                    audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 0);
                    break;
                case 1:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask = false;
                    audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 1);
                    break;
                case 2:
                    audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask = false;
                    audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 2);
                    break;
            }
        } else if (chip.equals(NpNesChip.FdsChip.class)) {
            audio.plugin.chipRegister.chip(NpNesChip.FdsChip.class).fds[chipId].channel.mask = false;
            audio.plugin.chipRegister.chip(NesChip.FdsChip.class).resetFdsMask(chipId);
        } else if (chip.equals(Mmc5Chip.class)) {
            switch (ch) {
                case 0:
                    audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[0].mask = false;
                    break;
                case 1:
                    audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].sqrChannels[1].mask = false;
                    break;
                case 2:
                    audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).mmc5[chipId].pcmChannel.mask = false;
                    break;
            }
            audio.plugin.chipRegister.chip(NpNesChip.Mmc5Chip.class).resetMmc5Mask(chipId, ch);
        } else if (chip.equals(DmgChip.class)) {
            audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(DmgChip.class).resetMask(chipId, ch);
        } else if (chip.equals(Vrc6Chip.class)) {
            audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).vrc6[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).resetVrc6Mask(chipId, ch);
        } else if (chip.equals(N163Chip.class)) {
            audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).n106[chipId].channels[ch].mask = false;
            audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).resetN163Mask(chipId, ch);
        }
    }

    public void forceChannelMask(Class<? extends Chip> chip, int chipId, int ch, boolean mask) {
        if (chip.equals(Ay8910Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(Ay8910Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(Ay8910Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(Ay8910Chip.class).ay8910_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(C140Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(C140Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(C140Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(C140Chip.class).c140[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(C140Chip.class).c140_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(C352Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(C352Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(C352Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(C352Chip.class).c352[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(C352Chip.class).c352_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(HuC6280Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(HuC6280Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(HuC6280Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(HuC6280Chip.class).huc6280_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(Rf5C164Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(Rf5C164Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(Rf5C164Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(Rf5C164Chip.class).rf5c164_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(Rf5C68Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(Rf5C68Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(Rf5C68Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(Rf5C68Chip.class).rf5c68_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(SegaPcmChip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(SegaPcmChip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(SegaPcmChip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(SegaPcmChip.class).segaPcm_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(QSoundChip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(QSoundChip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(QSoundChip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(QSoundChip.class).qSound[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(QSoundChip.class).qSound_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(Ym2151Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(Ym2151Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(Ym2151Chip.class).resetMask(chipId, ch, audio.plugin.stopped);
            audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(Ym2151Chip.class).ym2151_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(Ym2203Chip.class)) {
            if (ch >= 0 && ch < 9) {
                if (mask)
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).resetMask(chipId, ch, audio.plugin.stopped);

                audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId].channels[ch].mask = !mask;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[2].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[6].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[7].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203[chipId].channels[8].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId].channels[2].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId].channels[6].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId].channels[7].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2203Chip.class).ym2203_old[chipId].channels[8].mask = !mask;
                }
            }
        } else if (chip.equals(Ym2413Chip.class)) {
            if (ch >= 0 && ch < 14) {
                if (mask)
                    audio.plugin.chipRegister.chip(Ym2413Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym2413Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Ym2413Chip.class).ym2413_old[chipId].channels[ch].mask = !mask;
            }
        } else if (chip.equals(Ym2608Chip.class)) {
            if (ch >= 0 && ch < 14) {
                //if (mask)
                //    audio.setYM2608Mask(chipId, ch);
                //else
                //    audio.resetYM2608Mask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId].channels[ch].mask = !mask;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[2].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[9].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[10].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608[chipId].channels[11].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId].channels[2].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId].channels[9].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId].channels[10].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2608Chip.class).ym2608_old[chipId].channels[11].mask = !mask;
                }
            }
        } else if (chip.equals(Ym2610Chip.class)) {
            if (ch >= 0 && ch < 14) {
                int c = ch;
                if (ch == 12) c = 13;
                if (ch == 13) c = 12;

                if (mask)
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).resetMask(chipId, ch);
                audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[c].mask = mask;
                audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId].channels[c].mask = !mask;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[2].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[9].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[10].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610[chipId].channels[11].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId].channels[2].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId].channels[9].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId].channels[10].mask = !mask;
                    audio.plugin.chipRegister.chip(Ym2610Chip.class).ym2610_old[chipId].channels[11].mask = !mask;
                }
            }
        } else if (chip.equals(Ym2612Chip.class)) {
            if (ch >= 0 && ch < 9) {
                if (mask)
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels[ch].mask = null;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[2].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[6].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[7].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612[chipId].channels[8].mask = mask;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels[2].mask = null;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels[6].mask = null;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels[7].mask = null;
                    audio.plugin.chipRegister.chip(Ym2612Chip.class).ym2612_old[chipId].channels[8].mask = null;
                }
            }
        } else if (chip.equals(Ym3526Chip.class)) {
            if (ch >= 0 && ch < 14) {
                if (mask)
                    audio.plugin.chipRegister.chip(Ym3526Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym3526Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Ym3526Chip.class).ym3526_old[chipId].channels[ch].mask = !mask;
            }
        } else if (chip.equals(Y8950Chip.class)) {
            if (ch >= 0 && ch < 15) {
                if (mask)
                    audio.plugin.chipRegister.chip(Y8950Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Y8950Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Y8950Chip.class).y8950[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Y8950Chip.class).y8950_old[chipId].channels[ch].mask = !mask;
            }
        } else if (chip.equals(Ym3812Chip.class)) {
            if (ch >= 0 && ch < 14) {
                if (mask)
                    audio.plugin.chipRegister.chip(Ym3812Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(Ym3812Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(Ym3812Chip.class).ym3812_old[chipId].channels[ch].mask = !mask;
            }
        } else if (chip.equals(YmF262Chip.class)) {
            if (ch >= 0 && ch < 24) {
                if (mask)
                    audio.plugin.chipRegister.chip(YmF262Chip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(YmF262Chip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(YmF262Chip.class).ymf262_old[chipId].channels[ch].mask = !mask;
            }
        } else if (chip.equals(YmF278BChip.class)) {
            if (ch >= 0 && ch < 47) {
                if (mask)
                    audio.plugin.chipRegister.chip(YmF278BChip.class).setMask(chipId, ch);
                else
                    audio.plugin.chipRegister.chip(YmF278BChip.class).resetMask(chipId, ch);

                audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b[chipId].channels[ch].mask = mask;
                audio.plugin.chipRegister.chip(YmF278BChip.class).ymf278b_old[chipId].channels[ch].mask = !mask;
            }
        } else if (chip.equals(Sn76489Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(Sn76489Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(Sn76489Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(Sn76489Chip.class).sn76489_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(OkiM6295Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(OkiM6295Chip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(OkiM6295Chip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(OkiM6295Chip.class).okim6295_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(DmgChip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(DmgChip.class).setMask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(DmgChip.class).resetMask(chipId, ch);
            audio.plugin.chipRegister.chip(DmgChip.class).dmg[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(DmgChip.class).dmg_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(Vrc6Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).setVrc6Mask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(NpNesChip.Vrc6Chip.class).resetVrc6Mask(chipId, ch);
            audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(Vrc6Chip.class).vrc6_old[chipId].channels[ch].mask = !mask;
        } else if (chip.equals(N163Chip.class)) {
            if (mask)
                audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).setN163Mask(chipId, ch);
            else
                audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).resetN163Mask(chipId, ch);
            audio.plugin.chipRegister.chip(N163Chip.class).n106[chipId].channels[ch].mask = mask;
            audio.plugin.chipRegister.chip(N163Chip.class).n106_old[chipId].channels[ch].mask = !mask;
        }
    }

    public void ForceChannelMaskNES(Class<? extends Chip> chip, int chipId, int ch, NpNesChip.DmcChip.Params[] param) {
        if (ch == 0 || ch == 1) {
            if (param[chipId].sqrChannels[ch].mask) {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask = true;
                audio.plugin.chipRegister.chip(NesChip.class).setMask(chipId, ch);
            } else {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].sqrChannels[ch].mask = false;
                audio.plugin.chipRegister.chip(NesChip.class).resetMask(chipId, ch);
            }
        } else if (ch == 2) {
            if (param[chipId].triChannel.mask) {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask = true;
                audio.plugin.chipRegister.chip(NesChip.DmcChip.class).setDmcMask(chipId, 0);
            } else {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].triChannel.mask = false;
                audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 0);
            }

        } else if (ch == 3) {
            if (param[chipId].noiseChannel.mask) {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask = true;
                audio.plugin.chipRegister.chip(NesChip.DmcChip.class).setDmcMask(chipId, 1);
            } else {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].noiseChannel.mask = false;
                audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 1);
            }

        } else if (ch == 4) {
            if (param[chipId].dmcChannel.mask) {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask = true;
                audio.plugin.chipRegister.chip(NesChip.DmcChip.class).setDmcMask(chipId, 2);
            } else {
                audio.plugin.chipRegister.chip(NpNesChip.DmcChip.class).nesdmc[chipId].dmcChannel.mask = false;
                audio.plugin.chipRegister.chip(NesChip.DmcChip.class).resetDmcMask(chipId, 2);
            }
        }
    }

    private void startMIDIInMonitoring() {

        if (setting.getMidiKbd().getMidiInDeviceName().isEmpty()) {
            return;
        }

        if (midiin != null) {
            try {
                midiin.close();
                midiIn_MessageReceived.close();
                midiin = null;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                midiin = null;
            }
        }

        if (midiin == null) {
            MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
            for (var info : midiDeviceInfos) {
                MidiDevice device;
                try {
                    device = MidiSystem.getMidiDevice(info);
                } catch (MidiUnavailableException e) {
                    throw new RuntimeException(e);
                }
                if (device.getMaxTransmitters() == 0) {
                    continue;
                }
                if (setting.getMidiKbd().getMidiInDeviceName().equals(info.getName())) {
                    try {
                        midiin = device.getTransmitter();
                        midiin.setReceiver(midiIn_MessageReceived);
                    } catch (Exception e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                        midiin = null;
                    }
                }
            }
        }
    }

//    void midiIn_ErrorReceived(Object source, MidiInMessageEventArgs e) {
//        logger.log(Level.ERROR, "Error Time %s Message 0x%08x Event %s".formatted(e.Timestamp, e.RawMessage, e.MidiEvent));
//    }

    private void StopMIDIInMonitoring() {
        if (midiin != null) {
            try {
                midiin.close();
                this.midiIn_MessageReceived.close();
                midiin = null;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                midiin = null;
            }
        }
    }

    final Receiver midiIn_MessageReceived = new Receiver() {
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (!setting.getMidiKbd().getUseMIDIKeyboard()) return;
            ym2612MIDI.midiIn_MessageReceived(message);
        }

        @Override
        public void close() {
        }
    };

    public void ym2612Midi_ClearNoteLog() {
        ym2612MIDI.clearNoteLog();
    }

    public void ym2612Midi_ClearNoteLog(int ch) {
        ym2612MIDI.clearNoteLog(ch);
    }

    public void ym2612Midi_Log2MML(int ch) {
        ym2612MIDI.log2MML(ch);
    }

    public void ym2612Midi_Log2MML66(int ch) {
        ym2612MIDI.log2MML66(ch);
    }

    public void ym2612Midi_AllNoteOff() {
        ym2612MIDI.allNoteOff();
    }

    public void ym2612Midi_SetMode(int m) {
        ym2612MIDI.setMode(m);
    }

    public void ym2612Midi_SelectChannel(int ch) {
        ym2612MIDI.selectChannel(ch);
    }

    public void ym2612Midi_SetTonesToSetting() {
        ym2612MIDI.setTonesToSettng();
    }

    public void ym2612Midi_SetTonesFromSetting() {
        ym2612MIDI.setTonesFromSettng();
    }

    /**
     * @param tp 1 origin
     */
    public void ym2612Midi_SaveTonePallet(String fn, int tp) {
        ym2612MIDI.saveTonePallet(fn, tp, tonePallet);
    }

    /**
     * @param tp 1 origin
     */
    public void ym2612Midi_LoadTonePallet(String fn, int tp) {
        ym2612MIDI.loadTonePallet(fn, tp, tonePallet);
    }

    public void ym2612Midi_CopyToneToClipboard() {
        if (setting.getMidiKbd().isMono()) {
            ym2612MIDI.copyToneToClipboard(new int[] {setting.getMidiKbd().getUseMonoChannel()});
        } else {
            List<Integer> uc = new ArrayList<>();
            for (int i = 0; i < setting.getMidiKbd().getUseChannel().length; i++) {
                if (setting.getMidiKbd().getUseChannel()[i]) uc.add(i);
            }
            ym2612MIDI.copyToneToClipboard(uc.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public void ym2612Midi_PasteToneFromClipboard() {
        if (setting.getMidiKbd().isMono()) {
            ym2612MIDI.pasteToneFromClipboard(new int[] {setting.getMidiKbd().getUseMonoChannel()});
        } else {
            List<Integer> uc = new ArrayList<>();
            for (int i = 0; i < setting.getMidiKbd().getUseChannel().length; i++) {
                if (setting.getMidiKbd().getUseChannel()[i]) uc.add(i);
            }
            ym2612MIDI.pasteToneFromClipboard(uc.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public void ym2612Midi_CopyToneToClipboard(int ch) {
        ym2612MIDI.copyToneToClipboard(new int[] {ch});
    }

    public void ym2612Midi_PasteToneFromClipboard(int ch) {
        ym2612MIDI.pasteToneFromClipboard(new int[] {ch});
    }

    public void ym2612Midi_SetSelectInstParam(int ch, int n) {
        ym2612MIDI.ym2612Midi.selectCh = ch;
        ym2612MIDI.ym2612Midi.selectParam = n;
    }

    public void ym2612Midi_AddSelectInstParam(int n) {
        int p = ym2612MIDI.ym2612Midi.selectParam;
        p += n;
        if (p > 47) p = 0;
        ym2612MIDI.ym2612Midi.selectParam = p;
    }

    public void ym2612Midi_ChangeSelectedParamValue(int n) {
        ym2612MIDI.changeSelectedParamValue(n);
    }

    private void loadPresetMixerBalance(String playingFileName, String playingArcFileName, FileFormat format) {
        if (!setting.getAutoBalance().getUseThis()) return;

        try {
            Setting.Balance balance;
            Path fullPath = mdplayer.Common.settingFilePath;
            fullPath = fullPath.resolve("MixerBalance");
            if (!Files.exists(fullPath)) Files.createDirectory(fullPath);
            String fn = "";
            String defMbc = "";

            // Song-specific preset loading mode
            if (setting.getAutoBalance().getLoadSongBalance()) {
                if (setting.getAutoBalance().getSamePositionAsSongData()) {
                    fullPath = Path.of(playingFileName).getParent();
                    if (playingArcFileName != null && playingArcFileName.isEmpty()) {
                        fullPath = Path.of(playingArcFileName).getParent();
                    }
                }
                fn = Path.of(playingFileName).getFileName().toString();
                if (playingArcFileName != null && playingArcFileName.isEmpty()) {
                    fn = Path.of(playingArcFileName).getFileName().toString();
                }
                fn += ".mbc";
                if (!Files.exists(fullPath.resolve(fn))) {
                    fn = "";
                    fullPath = mdplayer.Common.settingFilePath;
                    fullPath = fullPath.resolve("MixerBalance");
                } else {
                    fullPath = fullPath.resolve(fn);
                }
            }

            // Driver-specific preset loading mode
            if (setting.getAutoBalance().getLoadDriverBalance() && fn.isEmpty()) {
                String[] fns = format.getPresetMixerBalance();
                if (fns != null) {
                    fn = fns[0];
                    defMbc = fns[1];

                    fullPath = fullPath.resolve(fn);
                }
            }

            if (fn == null || fn.isEmpty()) return;

            // Check for existence. If not, create it.
            if (!Files.exists(fullPath) && !defMbc.isEmpty()) Files.write(fullPath, defMbc.getBytes());
            // Read files in the data folder
            balance = Setting.Balance.load(fullPath);

            if (balance == null) return;

            // Mixer - Balance change processing
            final Setting.Balance finalBalance = balance;
            SwingUtilities.invokeLater(() -> {
                setting.setBalance(finalBalance);
                if (frmMixer2 != null) frmMixer2.update();
            });

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void ManualSavePresetMixerBalance(boolean isDriverBalance, String playingFileName, String playingArcFileName, FileFormat format, Setting.Balance balance) {
        if (!setting.getAutoBalance().getUseThis()) return;

        try {
            Path fullPath = mdplayer.Common.settingFilePath;
            fullPath = fullPath.resolve("MixerBalance");
            if (!Files.exists(fullPath)) Files.createDirectory(fullPath);
            String fn = "";

            if (isDriverBalance) {
                String[] fns = format.getPresetMixerBalance();
                if (fns != null) {
                    fn = fns[0];
                }

                fullPath = fullPath.resolve(fn);

            } else {

            }

            balance.save(fullPath);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public String SaveDriverBalance(Setting.Balance balance) {
        PlayList.Music music = frmPlayList.getPlayingSongInfo();
        if (music == null) {
            throw new IllegalStateException("Performance information could not be obtained. Please try again during or immediately after performance has finished.");
        }

        FileFormat fmt = music.format;
        ManualSavePresetMixerBalance(true, "", "", fmt, balance);

        return fmt.toString();
    }

    public PlayList.Music GetPlayingMusicInfo() {
        PlayList.Music music = frmPlayList.getPlayingSongInfo();
        return music;
    }

    public static Consumer<NativeKeyEvent> keyHookMeth = null;

    final NativeKeyListener keyboardHook1_KeyboardHooked = new NativeKeyListener() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            logger.log(Level.TRACE, "Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

            if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException nativeHookException) {
                    logger.log(Level.ERROR, nativeHookException.getMessage(), nativeHookException);
                }
            }

            if (keyHookMeth != null) {
                keyHookMeth.accept(e);
                return;
            }

            String k = String.valueOf(e.getKeyCode());
            boolean shift = (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
            boolean ctrl = (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
            boolean alt = (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0;
            Setting.KeyBoardHook.HookKeyInfo info;

            info = setting.getKeyboardHook().getStop();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                stop();
                return;
            }

            info = setting.getKeyboardHook().getPause();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                pause();
                return;
            }

            info = setting.getKeyboardHook().getFadeout();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                fadeout();
                return;
            }

            info = setting.getKeyboardHook().getPrev();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                prev();
                return;
            }

            info = setting.getKeyboardHook().getSlow();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                slow();
                return;
            }

            info = setting.getKeyboardHook().getPlay();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                play();
                return;
            }

            info = setting.getKeyboardHook().getNext();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                next();
                return;
            }

            info = setting.getKeyboardHook().getFast();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                ff();
            }
        }
    };

    private static void checkAndSetForm(JFrame frm) {
        Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle rc = new Rectangle(frm.getLocation(), frm.getSize());
        if (s.contains(rc)) {
            frm.setLocation(rc.getLocation());
            frm.setPreferredSize(rc.getSize());
        } else {
            frm.setLocation(new Point(100, 100));
        }
    }

    private void frmMain_FormClosed(WindowEvent e) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::SystemEvents_SessionEnding));
    }

    private void tsmiOpenFile_Click(ActionEvent ev) {
        String[] fn = fileOpen(true);

        if (fn != null)
            addFileAndPlay(fn);
    }

    private void addFileAndPlay(String[] fn) {
        if (audio.isPaused()) {
            audio.pause();
        }

        if (fn.length == 1) {
            frmPlayList.stop();

            frmPlayList.getPlayList().addFile(fn[0]);

            if (!(FileFormat.getFileFormat(fn[0]) instanceof M3UFileFormat) && !(FileFormat.getFileFormat(fn[0]) instanceof ZIPFileFormat)) {
                if (!loadAndPlay(0, 0, fn[0], "")) return;
                frmPlayList.setStart(-1);
            }
            oldParam = new MDChipParams();

            frmPlayList.play();
        } else {
            frmPlayList.stop();

            try {
                for (String f : fn) {
                    frmPlayList.getPlayList().addFile(f);
                }
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
            }
        }
    }

    private void tsmiExit_Click(ActionEvent ev) {
        this.setVisible(false);
    }

    private void tsmiPlay_Click(ActionEvent ev) {
        int n = frmPlayList.getMusicCount();

        if (newButtonMode[9] != 1 || n < 1) {
            play();
            oldParam = new MDChipParams();
        } else {
            next();
        }
    }

    private void tsmiStop_Click(ActionEvent ev) {
        frmPlayList.stop();
        stop();
    }

    private void tsmiPause_Click(ActionEvent ev) {
        pause();
    }

    private void tsmiFadeOut_Click(ActionEvent ev) {
        fadeout();
        frmPlayList.stop();
    }

    private void tsmiSlow_Click(ActionEvent ev) {
        slow();
    }

    private void tsmiFf_Click(ActionEvent ev) {
        ff();
    }

    private void tsmiNext_Click(ActionEvent ev) {
        next();
        oldParam = new MDChipParams();
    }

    private void tsmiPlayMode_Click(ActionEvent ev) {
        playMode();
    }

    private void tsmiOption_Click(ActionEvent ev) {
        openSetting();
    }

    private void tsmiPlayList_Click(ActionEvent ev) {
        dispPlayList();
    }

    private void tsmiOpenInfo_Click(ActionEvent ev) {
        openInfo();
    }

    private void tsmiOpenMixer_Click(ActionEvent ev) {
        openMixer();
    }

    private void tsmiChangeZoom_Click(ActionEvent ev) {
        if (ev != null && ev.getSource() == tsmiChangeZoomX1) setting.getOther().setZoom(1);
        else if (ev != null && ev.getSource() == tsmiChangeZoomX2) setting.getOther().setZoom(2);
        else if (ev != null && ev.getSource() == tsmiChangeZoomX3) setting.getOther().setZoom(3);
        else if (ev != null && ev.getSource() == tsmiChangeZoomX4) setting.getOther().setZoom(4);
        else
            setting.getOther().setZoom((setting.getOther().getZoom() == 4) ? 1 : (setting.getOther().getZoom() + 1));

        changeZoom();
    }

    private void tsmiVST_Click(ActionEvent ev) {
        dispVSTList();
    }

    private void tsmiMIDIkbd_Click(ActionEvent ev) {
        openMIDIKeyboard();
    }

    private void tsmiKBrd_Click(ActionEvent ev) {
        showContextMenu();
    }

    private void RegisterDumpMenuItem_Click(ActionEvent ev) {
        if (ev.getSource() == yM2612ToolStripMenuItem) openFormRegTest(0, Ym2612Chip.class, false);
        else if (ev.getSource() == ym2151ToolStripMenuItem) openFormRegTest(0, Ym2151Chip.class, false);
        else if (ev.getSource() == ym2203ToolStripMenuItem) openFormRegTest(0, Ym2203Chip.class, false);
        else if (ev.getSource() == ym2413ToolStripMenuItem) openFormRegTest(0, Ym2413Chip.class, false);
        else if (ev.getSource() == ym2608ToolStripMenuItem) openFormRegTest(0, Ym2608Chip.class, false);
        else if (ev.getSource() == yMF278BToolStripMenuItem) openFormRegTest(0, YmF278BChip.class, false);
        else if (ev.getSource() == yMF262ToolStripMenuItem) openFormRegTest(0, YmF262Chip.class, false);
        else if (ev.getSource() == yM2610ToolStripMenuItem) openFormRegTest(0, Ym2610Chip.class, false);
        else if (ev.getSource() == qSoundToolStripMenuItem) openFormRegTest(0, QSoundChip.class, false);
        else if (ev.getSource() == segaPCMToolStripMenuItem) openFormRegTest(0, SegaPcmChip.class, false);
        else if (ev.getSource() == yMZ280BToolStripMenuItem) openFormRegTest(0, YmZ280BChip.class, false);
        else if (ev.getSource() == sN76489ToolStripMenuItem) openFormRegTest(0, Sn76489Chip.class, false);
        else if (ev.getSource() == aY8910ToolStripMenuItem) openFormRegTest(0, Ay8910Chip.class, false);
        else if (ev.getSource() == c140ToolStripMenuItem) openFormRegTest(0, C140Chip.class, false);
        else if (ev.getSource() == c352ToolStripMenuItem) openFormRegTest(0, C352Chip.class, false);
        else if (ev.getSource() == yM3812ToolStripMenuItem) openFormRegTest(0, Ym3812Chip.class, false);
        else if (ev.getSource() == sIDToolStripMenuItem) openFormRegTest(0, SidChip.class, false);
        else openFormRegTest(0, null, false);
    }

    private final BufferedImage[] lstOpeButtonEnterImage = {
            Common.getImage("chSetting"),
            Common.getImage("chStop"),
            Common.getImage("chPause"),
            Common.getImage("chFadeout"),
            Common.getImage("chPrevious"),
            Common.getImage("chSlow"),
            Common.getImage("chPlay"),
            Common.getImage("chFast"),
            Common.getImage("chNext"),
            Common.getImage("chStep"),
            Common.getImage("chOpenFolder"),
            Common.getImage("chPlayList"),
            Common.getImage("chInformation"),
            Common.getImage("chMixer"),
            Common.getImage("chKBD"),
            Common.getImage("chVST"),
            Common.getImage("chMIDIKBD"),
            Common.getImage("chZoom"),
            Common.getImage("chRandom"),
            Common.getImage("chLoop"),
            Common.getImage("chLoopOne")
    };
    private final BufferedImage[] lstOpeButtonLeaveImage = {
            Common.getImage("ccSetting"),
            Common.getImage("ccStop"),
            Common.getImage("ccPause"),
            Common.getImage("ccFadeout"),
            Common.getImage("ccPrevious"),
            Common.getImage("ccSlow"),
            Common.getImage("ccPlay"),
            Common.getImage("ccFast"),
            Common.getImage("ccNext"),
            Common.getImage("ccStep"),
            Common.getImage("ccOpenFolder"),
            Common.getImage("ccPlayList"),
            Common.getImage("ccInformation"),
            Common.getImage("ccMixer"),
            Common.getImage("ccKBD"),
            Common.getImage("ccVST"),
            Common.getImage("ccMIDIKBD"),
            Common.getImage("ccZoom"),
            Common.getImage("ccRandom"),
            Common.getImage("ccLoop"),
            Common.getImage("ccLoopOne")
    };
    private final BufferedImage[] lstOpeButtonActiveImage = {
            Common.getImage("ciSetting"),
            Common.getImage("ciStop"),
            Common.getImage("ciPause"),
            Common.getImage("ciFadeout"),
            Common.getImage("ciPrevious"),
            Common.getImage("ciSlow"),
            Common.getImage("ciPlay"),
            Common.getImage("ciFast"),
            Common.getImage("ciNext"),
            Common.getImage("ciStep"),
            Common.getImage("ciOpenFolder"),
            Common.getImage("ciPlayList"),
            Common.getImage("ciInformation"),
            Common.getImage("ciMixer"),
            Common.getImage("ciKBD"),
            Common.getImage("ciVST"),
            Common.getImage("ciMIDIKBD"),
            Common.getImage("ciZoom"),
            Common.getImage("ciRandom"),
            Common.getImage("ciLoop"),
            Common.getImage("ciLoopOne")
    };
    private final boolean[] lstOpeButtonActive = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false
    };
    private final boolean[] lstOpeButtonActiveOld = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false
    };
    private JButton[] lstOpeButtonControl;

    private void RelocateOpeButton(int zoom) {
        // the skin lays the buttons out in two rows — transport on top (y 9), the rest below
        // (y 25) — not the single row the earlier port flattened them into; under the content
        // pane's null layout a bare setLocation() would also leave each button 0x0 and invisible,
        // so give each its 16x16 (times zoom) skin cell as bounds
        int s = 16 * zoom;
        opeButtonStop.setBounds((17 + 16 * 0) * zoom, 9 * zoom, s, s);
        opeButtonPause.setBounds((17 + 16 * 1) * zoom, 9 * zoom, s, s);
        opeButtonFadeout.setBounds((17 + 16 * 2) * zoom, 9 * zoom, s, s);
        opeButtonPrevious.setBounds((17 + 16 * 3) * zoom, 9 * zoom, s, s);
        opeButtonSlow.setBounds((17 + 16 * 4) * zoom, 9 * zoom, s, s);
        opeButtonPlay.setBounds((17 + 16 * 5) * zoom, 9 * zoom, s, s);
        opeButtonFast.setBounds((17 + 16 * 6) * zoom, 9 * zoom, s, s);
        opeButtonNext.setBounds((17 + 16 * 7) * zoom, 9 * zoom, s, s);
        opeButtonMode.setBounds((17 + 16 * 8) * zoom, 9 * zoom, s, s);

        opeButtonSetting.setBounds((17 + 16 * 0) * zoom, 25 * zoom, s, s);
        opeButtonOpen.setBounds((17 + 16 * 1) * zoom, 25 * zoom, s, s);
        opeButtonPlayList.setBounds((17 + 16 * 2) * zoom, 25 * zoom, s, s);
        opeButtonInformation.setBounds((17 + 16 * 3) * zoom, 25 * zoom, s, s);
        opeButtonMixer.setBounds((17 + 16 * 4) * zoom, 25 * zoom, s, s);
        opeButtonKBD.setBounds((17 + 16 * 5) * zoom, 25 * zoom, s, s);
        opeButtonVST.setBounds((17 + 16 * 6) * zoom, 25 * zoom, s, s);
        opeButtonMIDIKBD.setBounds((17 + 16 * 7) * zoom, 25 * zoom, s, s);
        opeButtonZoom.setBounds((17 + 16 * 8) * zoom, 25 * zoom, s, s);

        redrawButton(opeButtonSetting, setting.getOther().getZoom(), lstOpeButtonLeaveImage[0]);
        redrawButton(opeButtonStop, setting.getOther().getZoom(), lstOpeButtonLeaveImage[1]);
        redrawButton(opeButtonPause, setting.getOther().getZoom(), lstOpeButtonLeaveImage[2]);
        redrawButton(opeButtonFadeout, setting.getOther().getZoom(), lstOpeButtonLeaveImage[3]);
        redrawButton(opeButtonPrevious, setting.getOther().getZoom(), lstOpeButtonLeaveImage[4]);
        redrawButton(opeButtonSlow, setting.getOther().getZoom(), lstOpeButtonLeaveImage[5]);
        redrawButton(opeButtonPlay, setting.getOther().getZoom(), lstOpeButtonLeaveImage[6]);
        redrawButton(opeButtonFast, setting.getOther().getZoom(), lstOpeButtonLeaveImage[7]);
        redrawButton(opeButtonNext, setting.getOther().getZoom(), lstOpeButtonLeaveImage[8]);
        int m = newButtonMode[9] == 0 ? 9 : (newButtonMode[9] == 1 ? 18 : (newButtonMode[9] == 2 ? 19 : 20));
        redrawButton(opeButtonMode, setting.getOther().getZoom(), lstOpeButtonLeaveImage[m]);
        redrawButton(opeButtonOpen, setting.getOther().getZoom(), lstOpeButtonLeaveImage[10]);
        redrawButton(opeButtonPlayList, setting.getOther().getZoom(), lstOpeButtonLeaveImage[11]);
        redrawButton(opeButtonInformation, setting.getOther().getZoom(), lstOpeButtonLeaveImage[12]);
        redrawButton(opeButtonMixer, setting.getOther().getZoom(), lstOpeButtonLeaveImage[13]);
        redrawButton(opeButtonKBD, setting.getOther().getZoom(), lstOpeButtonLeaveImage[14]);
        redrawButton(opeButtonVST, setting.getOther().getZoom(), lstOpeButtonLeaveImage[15]);
        redrawButton(opeButtonMIDIKBD, setting.getOther().getZoom(), lstOpeButtonLeaveImage[16]);
        redrawButton(opeButtonZoom, setting.getOther().getZoom(), lstOpeButtonLeaveImage[17]);
    }

    final MouseListener opeButton_Mouse = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent ev) {
            JButton btn = (JButton) ev.getSource();
            int index = Integer.parseInt(btn.getActionCommand());
            int m = index;
            if (m == 9) {
                m = newButtonMode[9] == 0 ? 9 : (newButtonMode[9] == 1 ? 18 : (newButtonMode[9] == 2 ? 19 : 20));
            }
            redrawButton(btn, setting.getOther().getZoom(), lstOpeButtonEnterImage[m]);
        }

        @Override
        public void mouseExited(MouseEvent ev) {
            JButton btn = (JButton) ev.getSource();
            int index = Integer.parseInt(btn.getActionCommand());
            int m = index;
            if (m == 9) {
                m = newButtonMode[9] == 0 ? 9 : (newButtonMode[9] == 1 ? 18 : (newButtonMode[9] == 2 ? 19 : 20));
            }

            redrawButton(btn, setting.getOther().getZoom(), lstOpeButtonActive[m] ? lstOpeButtonActiveImage[m] : lstOpeButtonLeaveImage[m]);
        }
    };

    private static void redrawButton(JButton button, int zoom, BufferedImage image) {
        try {
            final int size = 16; // the sprites are 16x16 at 1x
            int dim = size * zoom;
            button.setPreferredSize(new Dimension(dim, dim));

            BufferedImage canvas = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setColor(Color.black);
            g.fillRect(0, 0, dim, dim);
            // scale the whole 16x16 sprite up to the zoomed button; the source rect must be the
            // sprite's own size, not the destination size, or only its top-left corner is copied and
            // the icon comes out at 1x in a corner of the cell
            g.drawImage(image, 0, 0, dim, dim, 0, 0, size, size, null);
            g.dispose();

            button.setIcon(new ImageIcon(canvas));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private void opeButtonSetting_Click(ActionEvent ev) {
        tsmiOption_Click(null);
    }

    private void opeButtonStop_Click(ActionEvent ev) {
        tsmiStop_Click(null);
    }

    private void opeButtonPause_Click(ActionEvent ev) {
        tsmiPause_Click(null);
    }

    private void opeButtonFadeout_Click(ActionEvent ev) {
        tsmiFadeOut_Click(null);
    }

    private void opeButtonPrevious_Click(ActionEvent ev) {
        prev();
        oldParam = new MDChipParams();
    }

    private void opeButtonSlow_Click(ActionEvent ev) {
        tsmiSlow_Click(null);
    }

    private void opeButtonPlay_Click(ActionEvent ev) {
        tsmiPlay_Click(null);
    }

    private void opeButtonFast_Click(ActionEvent ev) {
        tsmiFf_Click(null);
    }

    private void opeButtonNext_Click(ActionEvent ev) {
        tsmiNext_Click(null);
    }

    private void opeButtonMode_Click(ActionEvent ev) {
        tsmiPlayMode_Click(null);
        opeButton_Mouse.mouseEntered(new MouseEvent(opeButtonMode, 0, 0, 0, 0, 0, 0, 0, 0, false, 0)); // opeButtonMode
    }

    private void opeButtonOpen_Click(ActionEvent ev) {
        tsmiOpenFile_Click(null);
    }

    private void opeButtonPlayList_Click(ActionEvent ev) {
        tsmiPlayList_Click(null);
    }

    private void opeButtonInformation_Click(ActionEvent ev) {
        tsmiOpenInfo_Click(null);
    }

    private void opeButtonMixer_Click(ActionEvent ev) {
        tsmiOpenMixer_Click(null);
    }

    private void opeButtonKBD_Click(ActionEvent ev) {
        tsmiKBrd_Click(null);
    }

    private void opeButtonVST_Click(ActionEvent ev) {
        tsmiVST_Click(null);
    }

    private void opeButtonMIDIKBD_Click(ActionEvent ev) {
        tsmiMIDIkbd_Click(null);
    }

    private void opeButtonZoom_Click(ActionEvent ev) {
        tsmiChangeZoom_Click(null);
    }

    private void updateOpeButtonActiveState() {
        lstOpeButtonActive[1] = (audio.plugin.isStopped()); // STOP button
        lstOpeButtonActive[2] = audio.plugin.isStopped() ? false : audio.isPaused(); // PAUSE button
        lstOpeButtonActive[3] = audio.plugin.isStopped() ? false : audio.plugin.isFadeOut(); // Fade button
        lstOpeButtonActive[5] = audio.plugin.isSlow(); // Slowbutton
        lstOpeButtonActive[6] = audio.isPaused() ? false : (audio.plugin.isSlow() || audio.plugin.isFF() || audio.plugin.isFadeOut() ? false : !audio.plugin.isStopped()); // PLAY button
        lstOpeButtonActive[7] = audio.plugin.isFF(); // FFbutton
    }

    private void initializeComponent() {
        this.pbScreen = new ScreenPanel();
        this.cmsOpenOtherPanel = new JPopupMenu();
        this.primaryToolStripMenuItem = new JMenu();
        this.tsmiCPPSG = new JMenu();
        this.tsmiPAY8910 = new JMenuItem();
        this.tsmiPDCSG = new JMenuItem();
        this.tsmiCPWF = new JMenu();
        this.tsmiPHuC6280 = new JMenuItem();
        this.tsmiPK051649 = new JMenuItem();
        this.toolStripMenuItem2 = new JMenuItem();
        this.tsmiCPOPL = new JMenu();
        this.tsmiPOPLL = new JMenuItem();
        this.tsmiPOPL = new JMenuItem();
        this.tsmiPY8950 = new JMenuItem();
        this.tsmiPOPL2 = new JMenuItem();
        this.tsmiPOPL3 = new JMenuItem();
        this.tsmiPOPL4 = new JMenuItem();
        this.tsmiCPOPN = new JMenu();
        this.tsmiPOPN = new JMenuItem();
        this.tsmiPOPN2 = new JMenuItem();
        this.tsmiPOPNA = new JMenuItem();
        this.tsmiPOPNB = new JMenuItem();
        this.tsmiPOPM = new JMenuItem();
        this.tsmiPOPX = new JMenuItem();
        this.tsmiYMZ280B = new JMenuItem();
        this.tsmiCPPCM = new JMenu();
        this.tsmiPC140 = new JMenuItem();
        this.tsmiPC352 = new JMenuItem();
        this.tsmiPOKIM6258 = new JMenuItem();
        this.tsmiPOKIM6295 = new JMenuItem();
        this.tsmiPPWM = new JMenuItem();
        this.tsmiPQSound = new JMenuItem();
        this.tsmiPRF5C164 = new JMenuItem();
        this.tsmiPRF5C68 = new JMenuItem();
        this.tsmiPMultiPCM = new JMenuItem();
        this.tsmiPPPZ8 = new JMenuItem();
        this.tsmiPSegaPCM = new JMenuItem();
        this.tsmiPMIDI = new JMenuItem();
        this.tsmiCPNES = new JMenu();
        this.tsmiPNESDMC = new JMenuItem();
        this.tsmiPFDS = new JMenuItem();
        this.tsmiPMMC5 = new JMenuItem();
        this.tsmiPVRC6 = new JMenuItem();
        this.tsmiPVRC7 = new JMenuItem();
        this.tsmiPN106 = new JMenuItem();
        this.tsmiPS5B = new JMenuItem();
        this.tsmiPDMG = new JMenuItem();
        this.sencondryToolStripMenuItem = new JMenu();
        this.tsmiCSPSG = new JMenu();
        this.tsmiSAY8910 = new JMenuItem();
        this.tsmiSDCSG = new JMenuItem();
        this.tsmiCSWF = new JMenu();
        this.tsmiSHuC6280 = new JMenuItem();
        this.tsmiSK051649 = new JMenuItem();
        this.tsmiCSOPL = new JMenu();
        this.tsmiSOPLL = new JMenuItem();
        this.tsmiSOPL = new JMenuItem();
        this.tsmiSY8950 = new JMenuItem();
        this.tsmiSOPL2 = new JMenuItem();
        this.tsmiSOPL3 = new JMenuItem();
        this.tsmiSOPL4 = new JMenuItem();
        this.tsmiCSOPN = new JMenu();
        this.tsmiSOPN = new JMenuItem();
        this.tsmiSOPN2 = new JMenuItem();
        this.tsmiSOPNA = new JMenuItem();
        this.tsmiSOPNB = new JMenuItem();
        this.tsmiSOPM = new JMenuItem();
        this.tsmiSOPX = new JMenuItem();
        this.tsmiSYMZ280B = new JMenuItem();
        this.tsmiCSPCM = new JMenu();
        this.tsmiSC140 = new JMenuItem();
        this.tsmiSC352 = new JMenuItem();
        this.tsmiSOKIM6258 = new JMenuItem();
        this.tsmiSOKIM6295 = new JMenuItem();
        this.tsmiSPWM = new JMenuItem();
        this.tsmiSRF5C164 = new JMenuItem();
        this.tsmiSRF5C68 = new JMenuItem();
        this.tsmiSSegaPCM = new JMenuItem();
        this.tsmiSMultiPCM = new JMenuItem();
        this.tsmiSPPZ8 = new JMenuItem();
        this.tsmiSMIDI = new JMenuItem();
        this.tsmiCSNES = new JMenu();
        this.tsmiSFDS = new JMenuItem();
        this.tsmiSMMC5 = new JMenuItem();
        this.tsmiSNESDMC = new JMenuItem();
        this.tsmiSVRC6 = new JMenuItem();
        this.tsmiSVRC7 = new JMenuItem();
        this.tsmiSN106 = new JMenuItem();
        this.tsmiSS5B = new JMenuItem();
        this.tsmiSDMG = new JMenuItem();
        this.cmsMenu = new JPopupMenu();
        this.FileToolStripMenuItem = new JMenu();
        this.tsmiOpenFile = new JMenuItem();
        this.tsmiExit = new JMenuItem();
        this.OperationToolStripMenuItem = new JMenu();
        this.tsmiPlay = new JMenuItem();
        this.tsmiStop = new JMenuItem();
        this.tsmiPause = new JMenuItem();
        this.tsmiFadeOut = new JMenuItem();
        this.tsmiSlow = new JMenuItem();
        this.tsmiFf = new JMenuItem();
        this.tsmiNext = new JMenuItem();
        this.tsmiPlayMode = new JMenuItem();
        this.tsmiOption = new JMenuItem();
        this.tsmiPlayList = new JMenuItem();
        this.tsmiOpenInfo = new JMenuItem();
        this.tsmiOpenMixer = new JMenuItem();
        this.AnotherWindowDisplayToolStripMenuItem = new JMenu();
        this.tsmiKBrd = new JMenuItem();
        this.tsmiVST = new JMenuItem();
        this.tsmiMIDIkbd = new JMenuItem();
        this.tsmiChangeZoom = new JMenu();
        this.tsmiChangeZoomX1 = new JMenuItem();
        this.tsmiChangeZoomX2 = new JMenuItem();
        this.tsmiChangeZoomX3 = new JMenuItem();
        this.tsmiChangeZoomX4 = new JMenuItem();
        this.RegisterDumpDisplayToolStripMenuItem = new JMenu();
        this.yM2612ToolStripMenuItem = new JMenuItem();
        this.ym2151ToolStripMenuItem = new JMenuItem();
        this.ym2203ToolStripMenuItem = new JMenuItem();
        this.ym2413ToolStripMenuItem = new JMenuItem();
        this.ym2608ToolStripMenuItem = new JMenuItem();
        this.yM2610ToolStripMenuItem = new JMenuItem();
        this.yM3812ToolStripMenuItem = new JMenuItem();
        this.yMF262ToolStripMenuItem = new JMenuItem();
        this.yMF278BToolStripMenuItem = new JMenuItem();
        this.yMZ280BToolStripMenuItem = new JMenuItem();
        this.c140ToolStripMenuItem = new JMenuItem();
        this.c352ToolStripMenuItem = new JMenuItem();
        this.qSoundToolStripMenuItem = new JMenuItem();
        this.segaPCMToolStripMenuItem = new JMenuItem();
        this.sN76489ToolStripMenuItem = new JMenuItem();
        this.aY8910ToolStripMenuItem = new JMenuItem();
        this.sIDToolStripMenuItem = new JMenuItem();
        this.tsmiVisualizer = new JMenuItem();
        this.tsmiConsole = new JMenuItem();
        this.opeButtonSetting = new JButton();
        this.toolTip1 = new JToolTip();
        this.opeButtonStop = new JButton();
        this.opeButtonPause = new JButton();
        this.opeButtonFadeout = new JButton();
        this.opeButtonPrevious = new JButton();
        this.opeButtonSlow = new JButton();
        this.opeButtonPlay = new JButton();
        this.opeButtonFast = new JButton();
        this.opeButtonNext = new JButton();
        this.opeButtonZoom = new JButton();
        this.opeButtonMIDIKBD = new JButton();
        this.opeButtonVST = new JButton();
        this.opeButtonKBD = new JButton();
        this.opeButtonMixer = new JButton();
        this.opeButtonInformation = new JButton();
        this.opeButtonPlayList = new JButton();
        this.opeButtonOpen = new JButton();
        this.opeButtonMode = new JButton();
        this.keyboardHook1 = new KeyboardHook();

        //
        // pbScreen
        //
        this.pbScreen.setBackground(Color.black);
        new DropTarget(this.pbScreen, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        // the skin is blitted in as the frame buffer's background, see DoubleBuffer in frmMain_Load
        this.pbScreen.setName("pbScreen");
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        this.pbScreen.addMouseMotionListener(this.pbScreen_MouseMove);
        //
        // cmsOpenOtherPanel
        //
        this.cmsOpenOtherPanel.add(this.primaryToolStripMenuItem);
        this.cmsOpenOtherPanel.add(this.sencondryToolStripMenuItem);
        this.cmsOpenOtherPanel.setName("cmsOpenOtherPanel");
        //
        // primaryToolStripMenuItem
        //
        this.primaryToolStripMenuItem.add(this.tsmiCPPSG);
        this.primaryToolStripMenuItem.add(this.tsmiCPWF);
        this.primaryToolStripMenuItem.add(this.tsmiCPOPL);
        this.primaryToolStripMenuItem.add(this.tsmiCPOPN);
        this.primaryToolStripMenuItem.add(this.tsmiPOPM);
        this.primaryToolStripMenuItem.add(this.tsmiPOPX);
        this.primaryToolStripMenuItem.add(this.tsmiYMZ280B);
        this.primaryToolStripMenuItem.add(this.tsmiCPPCM);
        this.primaryToolStripMenuItem.add(this.tsmiPMIDI);
        this.primaryToolStripMenuItem.add(this.tsmiCPNES);
        this.primaryToolStripMenuItem.add(this.tsmiPDMG);
        this.primaryToolStripMenuItem.setName("primaryToolStripMenuItem");
        //
        // tsmiCPPSG
        //
        this.tsmiCPPSG.add(this.tsmiPAY8910);
        this.tsmiCPPSG.add(this.tsmiPDCSG);
        this.tsmiCPPSG.setName("tsmiCPPSG");
        //
        // tsmiPAY8910
        //
        this.tsmiPAY8910.setName("tsmiPAY8910");
        this.tsmiPAY8910.addActionListener(this::tsmiPAY8910_Click);
        //
        // tsmiPDCSG
        //
        this.tsmiPDCSG.setName("tsmiPDCSG");
        this.tsmiPDCSG.addActionListener(this::tsmiPDCSG_Click);
        //
        // tsmiCPWF
        //
        this.tsmiCPWF.add(this.tsmiPHuC6280);
        this.tsmiCPWF.add(this.tsmiPK051649);
        this.tsmiCPWF.add(this.toolStripMenuItem2);
        this.tsmiCPWF.setName("tsmiCPWF");
        //
        // tsmiPHuC6280
        //
        this.tsmiPHuC6280.setName("tsmiPHuC6280");
        this.tsmiPHuC6280.addActionListener(this::tsmiPHuC6280_Click);
        //
        // tsmiPK051649
        //
        this.tsmiPK051649.setName("tsmiPK051649");
        this.tsmiPK051649.addActionListener(this::tsmiPK051649_Click);
        //
        // toolStripMenuItem2
        //
        this.toolStripMenuItem2.setName("toolStripMenuItem2");
        //
        // tsmiCPOPL
        //
        this.tsmiCPOPL.add(this.tsmiPOPLL);
        this.tsmiCPOPL.add(this.tsmiPOPL);
        this.tsmiCPOPL.add(this.tsmiPY8950);
        this.tsmiCPOPL.add(this.tsmiPOPL2);
        this.tsmiCPOPL.add(this.tsmiPOPL3);
        this.tsmiCPOPL.add(this.tsmiPOPL4);
        this.tsmiCPOPL.setName("tsmiCPOPL");
        //
        // tsmiPOPLL
        //
        this.tsmiPOPLL.setName("tsmiPOPLL");
        this.tsmiPOPLL.addActionListener(this::tsmiPOPLL_Click);
        //
        // tsmiPOPL
        //
        this.tsmiPOPL.setName("tsmiPOPL");
        this.tsmiPOPL.addActionListener(this::tsmiPOPL_Click);
        //
        // tsmiPY8950
        //
        this.tsmiPY8950.setName("tsmiPY8950");
        this.tsmiPY8950.addActionListener(this::tsmiPY8950_Click);
        //
        // tsmiPOPL2
        //
        this.tsmiPOPL2.setName("tsmiPOPL2");
        this.tsmiPOPL2.addActionListener(this::tsmiPOPL2_Click);
        //
        // tsmiPOPL3
        //
        this.tsmiPOPL3.setName("tsmiPOPL3");
        this.tsmiPOPL3.addActionListener(this::tsmiPOPL3_Click);
        //
        // tsmiPOPL4
        //
        this.tsmiPOPL4.setName("tsmiPOPL4");
        this.tsmiPOPL4.addActionListener(this::tsmiPOPL4_Click);
        //
        // tsmiCPOPN
        //
        this.tsmiCPOPN.add(this.tsmiPOPN);
        this.tsmiCPOPN.add(this.tsmiPOPN2);
        this.tsmiCPOPN.add(this.tsmiPOPNA);
        this.tsmiCPOPN.add(this.tsmiPOPNB);
        this.tsmiCPOPN.setName("tsmiCPOPN");
        //
        // tsmiPOPN
        //
        this.tsmiPOPN.setName("tsmiPOPN");
        this.tsmiPOPN.addActionListener(this::tsmiPOPN_Click);
        //
        // tsmiPOPN2
        //
        this.tsmiPOPN2.setName("tsmiPOPN2");
        this.tsmiPOPN2.addActionListener(this::tsmiPOPN2_Click);
        //
        // tsmiPOPNA
        //
        this.tsmiPOPNA.setName("tsmiPOPNA");
        this.tsmiPOPNA.addActionListener(this::tsmiPOPNA_Click);
        //
        // tsmiPOPNB
        //
        this.tsmiPOPNB.setName("tsmiPOPNB");
        this.tsmiPOPNB.addActionListener(this::tsmiPOPNB_Click);
        //
        // tsmiPOPM
        //
        this.tsmiPOPM.setName("tsmiPOPM");
        this.tsmiPOPM.addActionListener(this::tsmiPOPM_Click);
        //
        // tsmiPOPX
        //
        this.tsmiPOPX.setName("tsmiPOPX");
        this.tsmiPOPX.addActionListener(this::tsmiPOPX_Click);
        //
        // tsmiYMZ280B
        //
        this.tsmiYMZ280B.setName("tsmiYMZ280B");
        this.tsmiYMZ280B.addActionListener(this::tsmiYMZ280B_Click);
        //
        // tsmiCPPCM
        //
        this.tsmiCPPCM.add(this.tsmiPC140);
        this.tsmiCPPCM.add(this.tsmiPC352);
        this.tsmiCPPCM.add(this.tsmiPOKIM6258);
        this.tsmiCPPCM.add(this.tsmiPOKIM6295);
        this.tsmiCPPCM.add(this.tsmiPPWM);
        this.tsmiCPPCM.add(this.tsmiPQSound);
        this.tsmiCPPCM.add(this.tsmiPRF5C164);
        this.tsmiCPPCM.add(this.tsmiPRF5C68);
        this.tsmiCPPCM.add(this.tsmiPMultiPCM);
        this.tsmiCPPCM.add(this.tsmiPPPZ8);
        this.tsmiCPPCM.add(this.tsmiPSegaPCM);
        this.tsmiCPPCM.setName("tsmiCPPCM");
        //
        // tsmiPC140
        //
        this.tsmiPC140.setName("tsmiPC140");
        this.tsmiPC140.addActionListener(this::tsmiPC140_Click);
        //
        // tsmiPC352
        //
        this.tsmiPC352.setName("tsmiPC352");
        this.tsmiPC352.addActionListener(this::tsmiPC352_Click);
        //
        // tsmiPOKIM6258
        //
        this.tsmiPOKIM6258.setName("tsmiPOKIM6258");
        this.tsmiPOKIM6258.addActionListener(this::tsmiPOKIM6258_Click);
        //
        // tsmiPOKIM6295
        //
        this.tsmiPOKIM6295.setName("tsmiPOKIM6295");
        this.tsmiPOKIM6295.addActionListener(this::tsmiPOKIM6295_Click);
        //
        // tsmiPPWM
        //
        this.tsmiPPWM.setName("tsmiPPWM");
        this.tsmiPPWM.addActionListener(this::tsmiPPWM_Click);
        //
        // tsmiPQSound
        //
        this.tsmiPQSound.setName("tsmiPQSound");
        this.tsmiPQSound.addActionListener(this::tsmiPQSound_Click);
        //
        // tsmiPRF5C164
        //
        this.tsmiPRF5C164.setName("tsmiPRF5C164");
        this.tsmiPRF5C164.addActionListener(this::tsmiPRF5C164_Click);
        //
        // tsmiPRF5C68
        //
        this.tsmiPRF5C68.setName("tsmiPRF5C68");
        this.tsmiPRF5C68.addActionListener(this::tsmiPRF5C68_Click);
        //
        // tsmiPMultiPCM
        //
        this.tsmiPMultiPCM.setName("tsmiPMultiPCM");
        this.tsmiPMultiPCM.addActionListener(this::tsmiPMultiPCM_Click);
        //
        // tsmiPPPZ8
        //
        this.tsmiPPPZ8.setName("tsmiPPPZ8");
        this.tsmiPPPZ8.addActionListener(this::tsmiPPPZ8_Click);
        //
        // tsmiPSegaPCM
        //
        this.tsmiPSegaPCM.setName("tsmiPSegaPCM");
        this.tsmiPSegaPCM.addActionListener(this::tsmiPSegaPCM_Click);
        //
        // tsmiPMIDI
        //
        this.tsmiPMIDI.setName("tsmiPMIDI");
        this.tsmiPMIDI.addActionListener(this::tsmiPMIDI_Click);
        //
        // tsmiCPNES
        //
        this.tsmiCPNES.add(this.tsmiPNESDMC);
        this.tsmiCPNES.add(this.tsmiPFDS);
        this.tsmiCPNES.add(this.tsmiPMMC5);
        this.tsmiCPNES.add(this.tsmiPVRC6);
        this.tsmiCPNES.add(this.tsmiPVRC7);
        this.tsmiCPNES.add(this.tsmiPN106);
        this.tsmiCPNES.add(this.tsmiPS5B);
        this.tsmiCPNES.setName("tsmiCPNES");
        //
        // tsmiPNESDMC
        //
        this.tsmiPNESDMC.setName("tsmiPNESDMC");
        this.tsmiPNESDMC.addActionListener(this::tsmiPNESDMC_Click);
        //
        // tsmiPFDS
        //
        this.tsmiPFDS.setName("tsmiPFDS");
        this.tsmiPFDS.addActionListener(this::tsmiPFDS_Click);
        //
        // tsmiPMMC5
        //
        this.tsmiPMMC5.setName("tsmiPMMC5");
        this.tsmiPMMC5.addActionListener(this::tsmiPMMC5_Click);
        //
        // tsmiPVRC6
        //
        this.tsmiPVRC6.setName("tsmiPVRC6");
        this.tsmiPVRC6.addActionListener(this::tsmiPVRC6_Click);
        //
        // tsmiPVRC7
        //
        this.tsmiPVRC7.setName("tsmiPVRC7");
        this.tsmiPVRC7.addActionListener(this::tsmiPVRC7_Click);
        //
        // tsmiPN106
        //
        this.tsmiPN106.setName("tsmiPN106");
        this.tsmiPN106.addActionListener(this::tsmiPN106_Click);
        //
        // tsmiPS5B
        //
        this.tsmiPS5B.setName("tsmiPS5B");
        this.tsmiPS5B.addActionListener(this::tsmiPS5B_Click);
        //
        // tsmiPDMG
        //
        this.tsmiPDMG.setName("tsmiPDMG");
        this.tsmiPDMG.addActionListener(this::tsmiPDMG_Click);
        //
        // sencondryToolStripMenuItem
        //
        this.sencondryToolStripMenuItem.add(this.tsmiCSPSG);
        this.sencondryToolStripMenuItem.add(this.tsmiCSWF);
        this.sencondryToolStripMenuItem.add(this.tsmiCSOPL);
        this.sencondryToolStripMenuItem.add(this.tsmiCSOPN);
        this.sencondryToolStripMenuItem.add(this.tsmiSOPM);
        this.sencondryToolStripMenuItem.add(this.tsmiSOPX);
        this.sencondryToolStripMenuItem.add(this.tsmiSYMZ280B);
        this.sencondryToolStripMenuItem.add(this.tsmiCSPCM);
        this.sencondryToolStripMenuItem.add(this.tsmiSMIDI);
        this.sencondryToolStripMenuItem.add(this.tsmiCSNES);
        this.sencondryToolStripMenuItem.add(this.tsmiSDMG);
        this.sencondryToolStripMenuItem.setName("sencondryToolStripMenuItem");
        //
        // tsmiCSPSG
        //
        this.tsmiCSPSG.add(this.tsmiSAY8910);
        this.tsmiCSPSG.add(this.tsmiSDCSG);
        this.tsmiCSPSG.setName("tsmiCSPSG");
        //
        // tsmiSAY8910
        //
        this.tsmiSAY8910.setName("tsmiSAY8910");
        this.tsmiSAY8910.addActionListener(this::tsmiSAY8910_Click);
        //
        // tsmiSDCSG
        //
        this.tsmiSDCSG.setName("tsmiSDCSG");
        this.tsmiSDCSG.addActionListener(this::tsmiSDCSG_Click);
        //
        // tsmiCSWF
        //
        this.tsmiCSWF.add(this.tsmiSHuC6280);
        this.tsmiCSWF.add(this.tsmiSK051649);
        this.tsmiCSWF.setName("tsmiCSWF");
        //
        // tsmiSHuC6280
        //
        this.tsmiSHuC6280.setName("tsmiSHuC6280");
        this.tsmiSHuC6280.addActionListener(this::tsmiSHuC6280_Click);
        //
        // tsmiSK051649
        //
        this.tsmiSK051649.setName("tsmiSK051649");
        this.tsmiSK051649.addActionListener(this::tsmiSK051649_Click);
        //
        // tsmiCSOPL
        //
        this.tsmiCSOPL.add(this.tsmiSOPLL);
        this.tsmiCSOPL.add(this.tsmiSOPL);
        this.tsmiCSOPL.add(this.tsmiSY8950);
        this.tsmiCSOPL.add(this.tsmiSOPL2);
        this.tsmiCSOPL.add(this.tsmiSOPL3);
        this.tsmiCSOPL.add(this.tsmiSOPL4);
        this.tsmiCSOPL.setName("tsmiCSOPL");
        //
        // tsmiSOPLL
        //
        this.tsmiSOPLL.setName("tsmiSOPLL");
        this.tsmiSOPLL.addActionListener(this::tsmiSOPLL_Click);
        //
        // tsmiSOPL
        //
        this.tsmiSOPL.setName("tsmiSOPL");
        this.tsmiSOPL.addActionListener(this::tsmiSOPL_Click);
        //
        // tsmiSY8950
        //
        this.tsmiSY8950.setName("tsmiSY8950");
        this.tsmiSY8950.addActionListener(this::tsmiSY8950_Click);
        //
        // tsmiSOPL2
        //
        this.tsmiSOPL2.setName("tsmiSOPL2");
        this.tsmiSOPL2.addActionListener(this::tsmiSOPL2_Click);
        //
        // tsmiSOPL3
        //
        this.tsmiSOPL3.setName("tsmiSOPL3");
        this.tsmiSOPL3.addActionListener(this::tsmiSOPL3_Click);
        //
        // tsmiSOPL4
        //
        this.tsmiSOPL4.setName("tsmiSOPL4");
        this.tsmiSOPL4.addActionListener(this::tsmiSOPL4_Click);
        //
        // tsmiCSOPN
        //
        this.tsmiCSOPN.add(this.tsmiSOPN);
        this.tsmiCSOPN.add(this.tsmiSOPN2);
        this.tsmiCSOPN.add(this.tsmiSOPNA);
        this.tsmiCSOPN.add(this.tsmiSOPNB);
        this.tsmiCSOPN.setName("tsmiCSOPN");
        //
        // tsmiSOPN
        //
        this.tsmiSOPN.setName("tsmiSOPN");
        this.tsmiSOPN.addActionListener(this::tsmiSOPN_Click);
        //
        // tsmiSOPN2
        //
        this.tsmiSOPN2.setName("tsmiSOPN2");
        this.tsmiSOPN2.addActionListener(this::tsmiSOPN2_Click);
        //
        // tsmiSOPNA
        //
        this.tsmiSOPNA.setName("tsmiSOPNA");
        this.tsmiSOPNA.addActionListener(this::tsmiSOPNA_Click);
        //
        // tsmiSOPNB
        //
        this.tsmiSOPNB.setName("tsmiSOPNB");
        this.tsmiSOPNB.addActionListener(this::tsmiSOPNB_Click);
        //
        // tsmiSOPM
        //
        this.tsmiSOPM.setName("tsmiSOPM");
        this.tsmiSOPM.addActionListener(this::tsmiSOPM_Click);
        //
        // tsmiSOPX
        //
        this.tsmiSOPX.setName("tsmiSOPX");
        this.tsmiSOPX.addActionListener(this::tsmiSOPX_Click);
        //
        // tsmiSYMZ280B
        //
        this.tsmiSYMZ280B.setName("tsmiSYMZ280B");
        this.tsmiSYMZ280B.addActionListener(this::tsmiSYMZ280B_Click);
        //
        // tsmiCSPCM
        //
        this.tsmiCSPCM.add(this.tsmiSC140);
        this.tsmiCSPCM.add(this.tsmiSC352);
        this.tsmiCSPCM.add(this.tsmiSOKIM6258);
        this.tsmiCSPCM.add(this.tsmiSOKIM6295);
        this.tsmiCSPCM.add(this.tsmiSPWM);
        this.tsmiCSPCM.add(this.tsmiSRF5C164);
        this.tsmiCSPCM.add(this.tsmiSRF5C68);
        this.tsmiCSPCM.add(this.tsmiSSegaPCM);
        this.tsmiCSPCM.add(this.tsmiSMultiPCM);
        this.tsmiCSPCM.add(this.tsmiSPPZ8);
        this.tsmiCSPCM.setName("tsmiCSPCM");
        //
        // tsmiSC140
        //
        this.tsmiSC140.setName("tsmiSC140");
        this.tsmiSC140.addActionListener(this::tsmiSC140_Click);
        //
        // tsmiSC352
        //
        this.tsmiSC352.setName("tsmiSC352");
        this.tsmiSC352.addActionListener(this::tsmiSC352_Click);
        //
        // tsmiSOKIM6258
        //
        this.tsmiSOKIM6258.setName("tsmiSOKIM6258");
        this.tsmiSOKIM6258.addActionListener(this::tsmiSOKIM6258_Click);
        //
        // tsmiSOKIM6295
        //
        this.tsmiSOKIM6295.setName("tsmiSOKIM6295");
        this.tsmiSOKIM6295.addActionListener(this::tsmiSOKIM6295_Click);
        //
        // tsmiSPWM
        //
        this.tsmiSPWM.setName("tsmiSPWM");
        this.tsmiSPWM.addActionListener(this::tsmiSPWM_Click);
        //
        // tsmiSRF5C164
        //
        this.tsmiSRF5C164.setName("tsmiSRF5C164");
        this.tsmiSRF5C164.addActionListener(this::tsmiSRF5C164_Click);
        //
        // tsmiSRF5C68
        //
        this.tsmiSRF5C68.setName("tsmiSRF5C68");
        this.tsmiSRF5C68.addActionListener(this::tsmiSRF5C68_Click);
        //
        // tsmiSSegaPCM
        //
        this.tsmiSSegaPCM.setName("tsmiSSegaPCM");
        this.tsmiSSegaPCM.addActionListener(this::tsmiSSegaPCM_Click);
        //
        // tsmiSMultiPCM
        //
        this.tsmiSMultiPCM.setName("tsmiSMultiPCM");
        this.tsmiSMultiPCM.addActionListener(this::tsmiSMultiPCM_Click);
        //
        // tsmiSPPZ8
        //
        this.tsmiSPPZ8.setName("tsmiSPPZ8");
        this.tsmiSPPZ8.addActionListener(this::tsmiSPPZ8_Click);
        //
        // tsmiSMIDI
        //
        this.tsmiSMIDI.setName("tsmiSMIDI");
        this.tsmiSMIDI.addActionListener(this::tsmiSMIDI_Click);
        //
        // tsmiCSNES
        //
        this.tsmiCSNES.add(this.tsmiSFDS);
        this.tsmiCSNES.add(this.tsmiSMMC5);
        this.tsmiCSNES.add(this.tsmiSNESDMC);
        this.tsmiCSNES.add(this.tsmiSVRC6);
        this.tsmiCSNES.add(this.tsmiSVRC7);
        this.tsmiCSNES.add(this.tsmiSN106);
        this.tsmiCSNES.add(this.tsmiSS5B);
        this.tsmiCSNES.setName("tsmiCSNES");
        //
        // tsmiSFDS
        //
        this.tsmiSFDS.setName("tsmiSFDS");
        this.tsmiSFDS.addActionListener(this::tsmiSFDS_Click);
        //
        // tsmiSMMC5
        //
        this.tsmiSMMC5.setName("tsmiSMMC5");
        this.tsmiSMMC5.addActionListener(this::tsmiSMMC5_Click);
        //
        // tsmiSNESDMC
        //
        this.tsmiSNESDMC.setName("tsmiSNESDMC");
        this.tsmiSNESDMC.addActionListener(this::tsmiSNESDMC_Click);
        //
        // tsmiSVRC6
        //
        this.tsmiSVRC6.setName("tsmiSVRC6");
        this.tsmiSVRC6.addActionListener(this::tsmiSVRC6_Click);
        //
        // tsmiSVRC7
        //
        this.tsmiSVRC7.setName("tsmiSVRC7");
        this.tsmiSVRC7.addActionListener(this::tsmiSVRC7_Click);
        //
        // tsmiSN106
        //
        this.tsmiSN106.setName("tsmiSN106");
        this.tsmiSN106.addActionListener(this::tsmiSN106_Click);
        //
        // tsmiSS5B
        //
        this.tsmiSS5B.setName("tsmiSS5B");
        this.tsmiSS5B.addActionListener(this::tsmiSS5B_Click);
        //
        // tsmiSDMG
        //
        this.tsmiSDMG.setName("tsmiSDMG");
        this.tsmiSDMG.addActionListener(this::tsmiSDMG_Click);
        //
        // cmsMenu
        //
        this.cmsMenu.add(this.FileToolStripMenuItem);
        this.cmsMenu.add(this.OperationToolStripMenuItem);
        this.cmsMenu.add(this.tsmiOption);
        this.cmsMenu.add(this.tsmiPlayList);
        this.cmsMenu.add(this.tsmiOpenInfo);
        this.cmsMenu.add(this.tsmiOpenMixer);
        this.cmsMenu.add(this.AnotherWindowDisplayToolStripMenuItem);
        this.cmsMenu.add(this.tsmiChangeZoom);
        this.cmsMenu.add(this.RegisterDumpDisplayToolStripMenuItem);
        this.cmsMenu.add(this.tsmiVisualizer);
        this.cmsMenu.add(this.tsmiConsole);
        this.cmsMenu.setName("contextMenuStrip1");
        //
        // FileToolStripMenuItem
        //
        this.FileToolStripMenuItem.add(this.tsmiOpenFile);
        this.FileToolStripMenuItem.add(this.tsmiExit);
        this.FileToolStripMenuItem.setIcon(new ImageIcon(Common.getImage("ccOpenFolder")));
        // the caption is keyed on the name the designer gave it
        this.FileToolStripMenuItem.setName("ファイルToolStripMenuItem");
        //
        // tsmiOpenFile
        //
        this.tsmiOpenFile.setName("tsmiOpenFile");
        this.tsmiOpenFile.addActionListener(this::tsmiOpenFile_Click);
        //
        // tsmiExit
        //
        this.tsmiExit.setName("tsmiExit");
        this.tsmiExit.addActionListener(this::tsmiExit_Click);
        //
        // OperationToolStripMenuItem
        //
        this.OperationToolStripMenuItem.add(this.tsmiPlay);
        this.OperationToolStripMenuItem.add(this.tsmiStop);
        this.OperationToolStripMenuItem.add(this.tsmiPause);
        this.OperationToolStripMenuItem.add(this.tsmiFadeOut);
        this.OperationToolStripMenuItem.add(this.tsmiSlow);
        this.OperationToolStripMenuItem.add(this.tsmiFf);
        this.OperationToolStripMenuItem.add(this.tsmiNext);
        this.OperationToolStripMenuItem.add(this.tsmiPlayMode);
        // the caption is keyed on the name the designer gave it
        this.OperationToolStripMenuItem.setName("操作ToolStripMenuItem");
        //
        // tsmiPlay
        //
        this.tsmiPlay.setIcon(new ImageIcon(Common.getImage("ccPlay")));
        this.tsmiPlay.setName("tsmiPlay");
        this.tsmiPlay.addActionListener(this::tsmiPlay_Click);
        //
        // tsmiStop
        //
        this.tsmiStop.setIcon(new ImageIcon(Common.getImage("ccStop")));
        this.tsmiStop.setName("tsmiStop");
        this.tsmiStop.addActionListener(this::tsmiStop_Click);
        //
        // tsmiPause
        //
        this.tsmiPause.setIcon(new ImageIcon(Common.getImage("ccPause")));
        this.tsmiPause.setName("tsmiPause");
        this.tsmiPause.addActionListener(this::tsmiPause_Click);
        //
        // tsmiFadeOut
        //
        this.tsmiFadeOut.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.tsmiFadeOut.setName("tsmiFadeOut");
        this.tsmiFadeOut.addActionListener(this::tsmiFadeOut_Click);
        //
        // tsmiSlow
        //
        this.tsmiSlow.setIcon(new ImageIcon(Common.getImage("ccSlow")));
        this.tsmiSlow.setName("tsmiSlow");
        this.tsmiSlow.addActionListener(this::tsmiSlow_Click);
        //
        // tsmiFf
        //
        this.tsmiFf.setIcon(new ImageIcon(Common.getImage("ccFast")));
        this.tsmiFf.setName("tsmiFf");
        this.tsmiFf.addActionListener(this::tsmiFf_Click);
        //
        // tsmiNext
        //
        this.tsmiNext.setIcon(new ImageIcon(Common.getImage("ccNext")));
        this.tsmiNext.setName("tsmiNext");
        this.tsmiNext.addActionListener(this::tsmiNext_Click);
        //
        // tsmiPlayMode
        //
        this.tsmiPlayMode.setIcon(new ImageIcon(Common.getImage("ccStep")));
        this.tsmiPlayMode.setName("tsmiPlayMode");
        this.tsmiPlayMode.addActionListener(this::tsmiPlayMode_Click);
        //
        // tsmiOption
        //
        this.tsmiOption.setIcon(new ImageIcon(Common.getImage("ccSetting")));
        this.tsmiOption.setName("tsmiOption");
        this.tsmiOption.addActionListener(this::tsmiOption_Click);
        //
        // tsmiPlayList
        //
        this.tsmiPlayList.setIcon(new ImageIcon(Common.getImage("ccPlayList")));
        this.tsmiPlayList.setName("tsmiPlayList");
        this.tsmiPlayList.addActionListener(this::tsmiPlayList_Click);
        //
        // tsmiOpenInfo
        //
        this.tsmiOpenInfo.setIcon(new ImageIcon(Common.getImage("ccInformation")));
        this.tsmiOpenInfo.setName("tsmiOpenInfo");
        this.tsmiOpenInfo.addActionListener(this::tsmiOpenInfo_Click);
        //
        // tsmiOpenMixer
        //
        this.tsmiOpenMixer.setIcon(new ImageIcon(Common.getImage("ccMixer")));
        this.tsmiOpenMixer.setName("tsmiOpenMixer");
        this.tsmiOpenMixer.addActionListener(this::tsmiOpenMixer_Click);
        //
        // AnotherWindowDisplayToolStripMenuItem
        //
        this.AnotherWindowDisplayToolStripMenuItem.add(this.tsmiKBrd);
        this.AnotherWindowDisplayToolStripMenuItem.add(this.tsmiVST);
        this.AnotherWindowDisplayToolStripMenuItem.add(this.tsmiMIDIkbd);
        // the caption is keyed on the name the designer gave it
        this.AnotherWindowDisplayToolStripMenuItem.setName("その他ウィンドウ表示ToolStripMenuItem");
        //
        // tsmiKBrd
        //
        this.tsmiKBrd.setIcon(new ImageIcon(Common.getImage("ccKBD")));
        this.tsmiKBrd.setName("tsmiKBrd");
        this.tsmiKBrd.addActionListener(this::tsmiKBrd_Click);
        //
        // tsmiVST
        //
        this.tsmiVST.setIcon(new ImageIcon(Common.getImage("ccVST")));
        this.tsmiVST.setName("tsmiVST");
        this.tsmiVST.addActionListener(this::tsmiVST_Click);
        //
        // tsmiMIDIkbd
        //
        this.tsmiMIDIkbd.setIcon(new ImageIcon(Common.getImage("ccMIDIKBD")));
        this.tsmiMIDIkbd.setName("tsmiMIDIkbd");
        this.tsmiMIDIkbd.addActionListener(this::tsmiMIDIkbd_Click);
        //
        // tsmiChangeZoom
        //
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX1);
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX2);
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX3);
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX4);
        this.tsmiChangeZoom.setIcon(new ImageIcon(Common.getImage("ccZoom")));
        this.tsmiChangeZoom.setName("tsmiChangeZoom");
        this.tsmiChangeZoom.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX1
        //
        this.tsmiChangeZoomX1.setName("tsmiChangeZoomX1");
        this.tsmiChangeZoomX1.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX2
        //
        this.tsmiChangeZoomX2.setName("tsmiChangeZoomX2");
        this.tsmiChangeZoomX2.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX3
        //
        this.tsmiChangeZoomX3.setName("tsmiChangeZoomX3");
        this.tsmiChangeZoomX3.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX4
        //
        this.tsmiChangeZoomX4.setName("tsmiChangeZoomX4");
        this.tsmiChangeZoomX4.addActionListener(this::tsmiChangeZoom_Click);
        //
        // RegisterDumpDisplayToolStripMenuItem
        //
        this.RegisterDumpDisplayToolStripMenuItem.add(this.yM2612ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.ym2151ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.ym2203ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.ym2413ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.ym2608ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.yM2610ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.yM3812ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.yMF262ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.yMF278BToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.yMZ280BToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.c140ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.c352ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.qSoundToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.segaPCMToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.sN76489ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.aY8910ToolStripMenuItem);
        this.RegisterDumpDisplayToolStripMenuItem.add(this.sIDToolStripMenuItem);
        // the caption is keyed on the name the designer gave it
        this.RegisterDumpDisplayToolStripMenuItem.setName("レジスタダンプ表示ToolStripMenuItem");
        //
        // yM2612ToolStripMenuItem
        //
        this.yM2612ToolStripMenuItem.setName("yM2612ToolStripMenuItem");
        this.yM2612ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2151ToolStripMenuItem
        //
        this.ym2151ToolStripMenuItem.setName("ym2151ToolStripMenuItem");
        this.ym2151ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2203ToolStripMenuItem
        //
        this.ym2203ToolStripMenuItem.setName("ym2203ToolStripMenuItem");
        this.ym2203ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2413ToolStripMenuItem
        //
        this.ym2413ToolStripMenuItem.setName("ym2413ToolStripMenuItem");
        this.ym2413ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2608ToolStripMenuItem
        //
        this.ym2608ToolStripMenuItem.setName("ym2608ToolStripMenuItem");
        this.ym2608ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yM2610ToolStripMenuItem
        //
        this.yM2610ToolStripMenuItem.setName("yM2610ToolStripMenuItem");
        this.yM2610ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yM3812ToolStripMenuItem
        //
        this.yM3812ToolStripMenuItem.setName("yM3812ToolStripMenuItem");
        this.yM3812ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yMF262ToolStripMenuItem
        //
        this.yMF262ToolStripMenuItem.setName("yMF262ToolStripMenuItem");
        this.yMF262ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yMF278BToolStripMenuItem
        //
        this.yMF278BToolStripMenuItem.setName("yMF278BToolStripMenuItem");
        this.yMF278BToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yMZ280BToolStripMenuItem
        //
        this.yMZ280BToolStripMenuItem.setName("yMZ280BToolStripMenuItem");
        this.yMZ280BToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // c140ToolStripMenuItem
        //
        this.c140ToolStripMenuItem.setName("c140ToolStripMenuItem");
        this.c140ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // c352ToolStripMenuItem
        //
        this.c352ToolStripMenuItem.setName("c352ToolStripMenuItem");
        this.c352ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // qSoundToolStripMenuItem
        //
        this.qSoundToolStripMenuItem.setName("qSoundToolStripMenuItem");
        this.qSoundToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // segaPCMToolStripMenuItem
        //
        this.segaPCMToolStripMenuItem.setName("segaPCMToolStripMenuItem");
        this.segaPCMToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // sN76489ToolStripMenuItem
        //
        this.sN76489ToolStripMenuItem.setName("sN76489ToolStripMenuItem");
        this.sN76489ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // aY8910ToolStripMenuItem
        //
        this.aY8910ToolStripMenuItem.setName("aY8910ToolStripMenuItem");
        this.aY8910ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // sIDToolStripMenuItem
        //
        this.sIDToolStripMenuItem.setName("sIDToolStripMenuItem");
        this.sIDToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // tsmiVisualizer
        //
        this.tsmiVisualizer.setName("tsmiVisualizer");
        this.tsmiVisualizer.addActionListener(this::tsmiVisWave_Click);
        //
        // tsmiConsole
        //
        this.tsmiConsole.setName("tsmiConsole");
        this.tsmiConsole.setText("Console");
        this.tsmiConsole.addActionListener(this::tsmiConsole_Click);
        //
        // opeButtonSetting
        //
//        this.opeButtonSetting.AllowDrop = true;
        new DropTarget(this.opeButtonSetting, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonSetting.setBackground(Color.black);
        this.opeButtonSetting.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonSetting.setName("opeButtonSetting");
        this.opeButtonSetting.setActionCommand("0");
        this.opeButtonSetting.setToolTipText(rb.getString("opeButtonSetting.ToolTip"));
        this.opeButtonSetting.addActionListener(this::opeButtonSetting_Click);
        this.opeButtonSetting.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonStop
        //
        new DropTarget(this.opeButtonStop, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonStop.setBackground(Color.black);
        this.opeButtonStop.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonStop.setName("opeButtonStop");
        this.opeButtonStop.setActionCommand("1");
        this.opeButtonStop.addActionListener(this::opeButtonStop_Click);
        this.opeButtonStop.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPause
        //
        new DropTarget(this.opeButtonPause, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPause.setBackground(Color.black);
        this.opeButtonPause.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonPause.setName("opeButtonPause");
        this.opeButtonPause.setActionCommand("2");
//        this.opeButtonPause.setToolTipText(Resources.getResourceManager().getString("opeButtonPause.ToolTip"));
        this.opeButtonPause.addActionListener(this::opeButtonPause_Click);
        this.opeButtonPause.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonFadeout
        //
        new DropTarget(this.opeButtonFadeout, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonFadeout.setBackground(Color.black);
        this.opeButtonFadeout.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonFadeout.setName("opeButtonFadeout");
        this.opeButtonFadeout.setActionCommand("3");
        this.opeButtonFadeout.addActionListener(this::opeButtonFadeout_Click);
        this.opeButtonFadeout.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPrevious
        //
        new DropTarget(this.opeButtonPrevious, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPrevious.setBackground(Color.black);
        this.opeButtonPrevious.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonPrevious.setName("opeButtonPrevious");
        this.opeButtonPrevious.setActionCommand("4");
        this.opeButtonPrevious.addActionListener(this::opeButtonPrevious_Click);
        this.opeButtonPrevious.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonSlow
        //
        new DropTarget(this.opeButtonSlow, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonSlow.setBackground(Color.black);
        this.opeButtonSlow.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonSlow.setName("opeButtonSlow");
        this.opeButtonSlow.setActionCommand("5");
//        this.opeButtonSlow.setToolTipText(Resources.getResourceManager().getString("opeButtonSlow.ToolTip"));
        this.opeButtonSlow.addActionListener(this::opeButtonSlow_Click);
        this.opeButtonSlow.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPlay
        //
        new DropTarget(this.opeButtonPlay, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPlay.setBackground(Color.black);
        this.opeButtonPlay.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonPlay.setName("opeButtonPlay");
        this.opeButtonPlay.setActionCommand("6");
//        this.opeButtonPlay.setToolTipText(Resources.getResourceManager().getString("opeButtonPlay.ToolTip"));
        this.opeButtonPlay.addActionListener(this::opeButtonPlay_Click);
        this.opeButtonPlay.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonFast
        //
        new DropTarget(this.opeButtonFast, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonFast.setBackground(Color.black);
        this.opeButtonFast.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonFast.setName("opeButtonFast");
        this.opeButtonFast.setActionCommand("7");
//        this.opeButtonFast.setToolTipText(Resources.getResourceManager().getString("opeButtonFast.ToolTip"));
        this.opeButtonFast.addActionListener(this::opeButtonFast_Click);
        this.opeButtonFast.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonNext
        //
        new DropTarget(this.opeButtonNext, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonNext.setBackground(Color.black);
        this.opeButtonNext.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonNext.setName("opeButtonNext");
        this.opeButtonNext.setActionCommand("8");
//        this.opeButtonNext.setToolTipText(Resources.getResourceManager().getString("opeButtonNext.ToolTip"));
        this.opeButtonNext.addActionListener(this::opeButtonNext_Click);
        this.opeButtonNext.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonZoom
        //
        new DropTarget(this.opeButtonZoom, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonZoom.setBackground(Color.black);
        this.opeButtonZoom.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonZoom.setName("opeButtonZoom");
        this.opeButtonZoom.setActionCommand("17");
//        this.opeButtonZoom.setToolTipText(Resources.getResourceManager().getString("opeButtonZoom.ToolTip"));
        this.opeButtonZoom.addActionListener(this::opeButtonZoom_Click);
        this.opeButtonZoom.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonMIDIKBD
        //
        new DropTarget(this.opeButtonMIDIKBD, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonMIDIKBD.setBackground(Color.black);
        this.opeButtonMIDIKBD.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonMIDIKBD.setName("opeButtonMIDIKBD");
        this.opeButtonMIDIKBD.setActionCommand("16");
//        this.opeButtonMIDIKBD.setToolTipText(Resources.getResourceManager().getString("opeButtonMIDIKBD.ToolTip"));
        this.opeButtonMIDIKBD.addActionListener(this::opeButtonMIDIKBD_Click);
        this.opeButtonMIDIKBD.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonVST
        //
        new DropTarget(this.opeButtonVST, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonVST.setBackground(Color.black);
        this.opeButtonVST.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonVST.setName("opeButtonVST");
        this.opeButtonVST.setActionCommand("15");
//        this.opeButtonVST.setToolTipText(Resources.getResourceManager().getString("opeButtonVST.ToolTip"));
        this.opeButtonVST.addActionListener(this::opeButtonVST_Click);
        this.opeButtonVST.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonKBD
        //
        new DropTarget(this.opeButtonKBD, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonKBD.setBackground(Color.black);
        this.opeButtonKBD.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonKBD.setName("opeButtonKBD");
        this.opeButtonKBD.setActionCommand("14");
//        this.opeButtonKBD.setToolTipText(Resources.getResourceManager().getString("opeButtonKBD.ToolTip"));
        this.opeButtonKBD.addActionListener(this::opeButtonKBD_Click);
        this.opeButtonKBD.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonMixer
        //
        new DropTarget(this.opeButtonMixer, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonMixer.setBackground(Color.black);
        this.opeButtonMixer.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonMixer.setName("opeButtonMixer");
        this.opeButtonMixer.setActionCommand("13");
//        this.opeButtonMixer.setToolTipText(Resources.getResourceManager().getString("opeButtonMixer.ToolTip"));
        this.opeButtonMixer.addActionListener(this::opeButtonMixer_Click);
        this.opeButtonMixer.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonInformation
        //
        new DropTarget(this.opeButtonInformation, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonInformation.setBackground(Color.black);
        this.opeButtonInformation.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonInformation.setName("opeButtonInformation");
        this.opeButtonInformation.setActionCommand("12");
//        this.opeButtonInformation.setToolTipText(Resources.getResourceManager().getString("opeButtonInformation.ToolTip"));
        this.opeButtonInformation.addActionListener(this::opeButtonInformation_Click);
        this.opeButtonInformation.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPlayList
        //
        new DropTarget(this.opeButtonPlayList, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPlayList.setBackground(Color.black);
        this.opeButtonPlayList.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonPlayList.setName("opeButtonPlayList");
        this.opeButtonPlayList.setActionCommand("11");
//        this.opeButtonPlayList.setToolTipText(Resources.getResourceManager().getString("opeButtonPlayList.ToolTip"));
        this.opeButtonPlayList.addActionListener(this::opeButtonPlayList_Click);
        this.opeButtonPlayList.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonOpen
        //
        new DropTarget(this.opeButtonOpen, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonOpen.setBackground(Color.black);
        this.opeButtonOpen.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonOpen.setName("opeButtonOpen");
        this.opeButtonOpen.setActionCommand("10");
//        this.opeButtonOpen.setToolTipText(Resources.getResourceManager().getString("opeButtonOpen.ToolTip"));
        this.opeButtonOpen.addActionListener(this::opeButtonOpen_Click);
        this.opeButtonOpen.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonMode
        //
        new DropTarget(this.opeButtonMode, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonMode.setBackground(Color.black);
        this.opeButtonMode.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.opeButtonMode.setName("opeButtonMode");
        this.opeButtonMode.setActionCommand("9");
//        this.opeButtonMode.setToolTipText(Resources.getResourceManager().getString("opeButtonMode.ToolTip"));
        this.opeButtonMode.addActionListener(this::opeButtonMode_Click);
        this.opeButtonMode.addMouseListener(this.opeButton_Mouse);
        //
        // keyboardHook1
        //
        this.keyboardHook1.addKeyboardHooked(this.keyboardHook1_KeyboardHooked);
        //
        // frmMain
        //
        JPanel main = new JPanel();
        main.setLayout(null);
        // the operation buttons sit on top of the skin at absolute pixel positions (see
        // RelocateOpeButton). They are children of pbScreen, not siblings of it: pbScreen is opaque
        // and repaints every frame, so a sibling drawn on top of it would be painted over each
        // time — as its children they are painted together with, and after, the skin.
        this.pbScreen.setLayout(null);
        this.pbScreen.add(this.opeButtonZoom);
        this.pbScreen.add(this.opeButtonMIDIKBD);
        this.pbScreen.add(this.opeButtonVST);
        this.pbScreen.add(this.opeButtonKBD);
        this.pbScreen.add(this.opeButtonMixer);
        this.pbScreen.add(this.opeButtonInformation);
        this.pbScreen.add(this.opeButtonPlayList);
        this.pbScreen.add(this.opeButtonOpen);
        this.pbScreen.add(this.opeButtonMode);
        this.pbScreen.add(this.opeButtonNext);
        this.pbScreen.add(this.opeButtonFast);
        this.pbScreen.add(this.opeButtonPlay);
        this.pbScreen.add(this.opeButtonSlow);
        this.pbScreen.add(this.opeButtonPrevious);
        this.pbScreen.add(this.opeButtonFadeout);
        this.pbScreen.add(this.opeButtonPause);
        this.pbScreen.add(this.opeButtonStop);
        this.pbScreen.add(this.opeButtonSetting);
        main.add(this.pbScreen);
        this.setContentPane(main);
        this.setName("frmMain");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        // the window is sized to the skin by changeZoom()/pack() (which derives the frame size from
        // the content pane plus the title-bar inset); an explicit frame preferred size here would
        // instead make pack() use it verbatim and clip the skin, so it is deliberately not set.
        // Like the WinForms original (FixedSingle), the window is not user-resizable.
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // the menu items were named but never captioned — their text was in the .resx, and the
        // ApplyResources() calls that would have fetched it did not survive the port, so without
        // this every entry of both menus comes up blank
        Layouts.captions(this.cmsMenu, resources);
        Layouts.captions(this.cmsOpenOtherPanel, resources);

        this.pack();
    }

    private ScreenPanel pbScreen;
    private JPopupMenu cmsOpenOtherPanel;
    private JMenu primaryToolStripMenuItem;
    private JMenuItem tsmiPOPN;
    private JMenuItem tsmiPOPN2;
    private JMenuItem tsmiPOPNA;
    private JMenuItem tsmiPOPNB;
    private JMenuItem tsmiPOPM;
    private JMenuItem tsmiPDCSG;
    private JMenuItem tsmiPRF5C164;
    private JMenuItem tsmiPPWM;
    private JMenuItem tsmiPOKIM6258;
    private JMenuItem tsmiPOKIM6295;
    private JMenuItem tsmiPC140;
    private JMenuItem tsmiPSegaPCM;
    private JMenu sencondryToolStripMenuItem;
    private JMenuItem tsmiSOPN;
    private JMenuItem tsmiSOPN2;
    private JMenuItem tsmiSOPNA;
    private JMenuItem tsmiSOPNB;
    private JMenuItem tsmiSOPM;
    private JMenuItem tsmiSDCSG;
    private JMenuItem tsmiSRF5C164;
    private JMenuItem tsmiSPWM;
    private JMenuItem tsmiSOKIM6258;
    private JMenuItem tsmiSOKIM6295;
    private JMenuItem tsmiSC140;
    private JMenuItem tsmiSSegaPCM;
    private JMenuItem tsmiPAY8910;
    private JMenuItem tsmiPOPLL;
    private JMenuItem tsmiSAY8910;
    private JMenuItem tsmiSOPLL;
    private JMenuItem tsmiPHuC6280;
    private JMenuItem tsmiSHuC6280;
    private JMenuItem tsmiPMIDI;
    private JMenuItem tsmiSMIDI;
    private JMenuItem tsmiPNESDMC;
    private JMenuItem tsmiSNESDMC;
    private JMenuItem tsmiPFDS;
    private JMenuItem tsmiSFDS;
    private JMenuItem tsmiPMMC5;
    private JMenuItem tsmiSMMC5;
    private JMenuItem tsmiPOPL4;
    private JMenuItem tsmiSOPL4;
    private JMenuItem tsmiPVRC7;
    private JMenuItem tsmiSVRC7;
    private JMenuItem tsmiPOPL3;
    private JMenuItem tsmiSOPL3;
    private JMenuItem tsmiPC352;
    private JMenuItem tsmiSC352;
    private JMenuItem tsmiPOPL2;
    private JMenuItem tsmiSOPL2;
    private KeyboardHook keyboardHook1;
    private JMenuItem tsmiPOPL;
    private JMenuItem tsmiSOPL;
    private JMenuItem tsmiPY8950;
    private JMenuItem tsmiSY8950;
    private JMenuItem tsmiPK051649;
    private JMenuItem tsmiSK051649;
    private JPopupMenu cmsMenu;
    private JMenu FileToolStripMenuItem;
    private JMenuItem tsmiOpenFile;
    private JMenuItem tsmiExit;
    private JMenu OperationToolStripMenuItem;
    private JMenuItem tsmiPlay;
    private JMenuItem tsmiStop;
    private JMenuItem tsmiPause;
    private JMenuItem tsmiFadeOut;
    private JMenuItem tsmiSlow;
    private JMenuItem tsmiFf;
    private JMenuItem tsmiNext;
    private JMenuItem tsmiPlayMode;
    private JMenuItem tsmiOption;
    private JMenuItem tsmiPlayList;
    private JMenuItem tsmiOpenInfo;
    private JMenuItem tsmiOpenMixer;
    private JMenu AnotherWindowDisplayToolStripMenuItem;
    private JMenuItem tsmiKBrd;
    private JMenuItem tsmiVST;
    private JMenuItem tsmiMIDIkbd;
    private JMenu tsmiChangeZoom;
    private JMenu RegisterDumpDisplayToolStripMenuItem;
    private JMenuItem tsmiPQSound;
    private JMenuItem tsmiChangeZoomX1;
    private JMenuItem tsmiChangeZoomX2;
    private JMenuItem tsmiChangeZoomX3;
    private JMenuItem tsmiChangeZoomX4;
    private JMenuItem tsmiYMZ280B;
    private JMenuItem tsmiSYMZ280B;
    private JMenuItem tsmiPMultiPCM;
    private JMenuItem tsmiSMultiPCM;

    private JMenuItem yM2612ToolStripMenuItem;
    private JMenuItem c140ToolStripMenuItem;
    private JMenuItem ym2151ToolStripMenuItem;
    private JMenuItem ym2203ToolStripMenuItem;
    private JMenuItem ym2413ToolStripMenuItem;
    private JMenuItem ym2608ToolStripMenuItem;
    private JMenuItem yM2610ToolStripMenuItem;
    private JMenuItem yMF262ToolStripMenuItem;
    private JMenuItem yMF278BToolStripMenuItem;
    private JMenuItem yMZ280BToolStripMenuItem;
    private JMenuItem c352ToolStripMenuItem;
    private JMenuItem qSoundToolStripMenuItem;
    private JMenuItem segaPCMToolStripMenuItem;
    private JMenuItem sN76489ToolStripMenuItem;
    private JMenuItem aY8910ToolStripMenuItem;
    private JMenuItem yM3812ToolStripMenuItem;

    private JMenuItem tsmiPVRC6;
    private JMenuItem tsmiSVRC6;
    private JMenuItem tsmiPN106;
    private JMenuItem tsmiSN106;
    private JMenuItem sIDToolStripMenuItem;
    private JMenuItem tsmiPPPZ8;
    private JMenuItem tsmiSPPZ8;
    private JMenuItem tsmiPS5B;
    private JMenuItem tsmiSS5B;
    private JMenuItem tsmiPDMG;
    private JMenuItem tsmiSDMG;
    private JMenuItem tsmiPRF5C68;
    private JMenuItem tsmiSRF5C68;
    private JMenuItem tsmiPOPX;
    private JMenuItem tsmiSOPX;
    private JMenu tsmiCPNES;
    private JMenu tsmiCPPCM;
    private JMenu tsmiCPOPN;
    private JMenu tsmiCPOPL;
    private JMenu tsmiCPPSG;
    private JMenu tsmiCPWF;
    private JMenuItem toolStripMenuItem2;
    private JMenu tsmiCSPSG;
    private JMenu tsmiCSWF;
    private JMenu tsmiCSOPL;
    private JMenu tsmiCSOPN;
    private JMenu tsmiCSPCM;
    private JMenu tsmiCSNES;
    private JButton opeButtonSetting;
    private JToolTip toolTip1;
    private JButton opeButtonStop;
    private JButton opeButtonPause;
    private JButton opeButtonFadeout;
    private JButton opeButtonPrevious;
    private JButton opeButtonSlow;
    private JButton opeButtonPlay;
    private JButton opeButtonFast;
    private JButton opeButtonNext;
    private JButton opeButtonZoom;
    private JButton opeButtonMIDIKBD;
    private JButton opeButtonVST;
    private JButton opeButtonKBD;
    private JButton opeButtonMixer;
    private JButton opeButtonInformation;
    private JButton opeButtonPlayList;
    private JButton opeButtonOpen;
    private JButton opeButtonMode;
    private JMenuItem tsmiVisualizer;
    private JMenuItem tsmiConsole;
    private FormConsole frmConsole;

    private int[] getChipStatus() {
        int[] chips = new int[256];
/* TODO led
        chips[0] = audio.plugin.chipLED.get("PriOPN");
        audio.plugin.chipLED.put("PriOPN", audio.plugin.chipLED.get("PriOPN"));
        chips[1] = audio.plugin.chipLED.get("PriOPN2");
        audio.plugin.chipLED.put("PriOPN2", audio.plugin.chipLED.get("PriOPN2"));
        chips[2] = audio.plugin.chipLED.get("PriOPNA");
        audio.plugin.chipLED.put("PriOPNA", audio.plugin.chipLED.get("PriOPNA"));
        chips[3] = audio.plugin.chipLED.get("PriOPNB");
        audio.plugin.chipLED.put("PriOPNB", audio.plugin.chipLED.get("PriOPNB"));

        chips[4] = audio.plugin.chipLED.get("PriOPM");
        audio.plugin.chipLED.put("PriOPM", audio.plugin.chipLED.get("PriOPM"));
        chips[5] = audio.plugin.chipLED.get("PriDCSG");
        audio.plugin.chipLED.put("PriDCSG", audio.plugin.chipLED.get("PriDCSG"));
        chips[6] = audio.plugin.chipLED.get("PriRF5C");
        audio.plugin.chipLED.put("PriRF5C", audio.plugin.chipLED.get("PriRF5C"));
        chips[7] = audio.plugin.chipLED.get("PriPWM");
        audio.plugin.chipLED.put("PriPWM", audio.plugin.chipLED.get("PriPWM"));

        chips[8] = audio.plugin.chipLED.get("PriOKI5");
        audio.plugin.chipLED.put("PriOKI5", audio.plugin.chipLED.get("PriOKI5"));
        chips[9] = audio.plugin.chipLED.get("PriOKI9");
        audio.plugin.chipLED.put("PriOKI9", audio.plugin.chipLED.get("PriOKI9"));
        chips[10] = audio.plugin.chipLED.get("PriC140");
        audio.plugin.chipLED.put("PriC140", audio.plugin.chipLED.get("PriC140"));
        chips[11] = audio.plugin.chipLED.get("PriSPCM");
        audio.plugin.chipLED.put("PriSPCM", audio.plugin.chipLED.get("PriSPCM"));

        chips[12] = audio.plugin.chipLED.get("PriAY10");
        audio.plugin.chipLED.put("PriAY10", audio.plugin.chipLED.get("PriAY10"));
        chips[13] = audio.plugin.chipLED.get("PriOPLL");
        audio.plugin.chipLED.put("PriOPLL", audio.plugin.chipLED.get("PriOPLL"));
        chips[14] = audio.plugin.chipLED.get("PriHuC");
        audio.plugin.chipLED.put("PriHuC", audio.plugin.chipLED.get("PriHuC"));
        chips[15] = audio.plugin.chipLED.get("PriC352");
        audio.plugin.chipLED.put("PriC352", audio.plugin.chipLED.get("PriC352"));
        chips[16] = audio.plugin.chipLED.get("PriK054539");
        audio.plugin.chipLED.put("PriK054539", audio.plugin.chipLED.get("PriK054539"));
        chips[17] = audio.plugin.chipLED.get("PriRF5C68");
        audio.plugin.chipLED.put("PriRF5C68", audio.plugin.chipLED.get("PriRF5C68"));


        chips[128 + 0] = audio.plugin.chipLED.get("SecOPN");
        audio.plugin.chipLED.put("SecOPN", audio.plugin.chipLED.get("SecOPN"));
        chips[128 + 1] = audio.plugin.chipLED.get("SecOPN2");
        audio.plugin.chipLED.put("SecOPN2", audio.plugin.chipLED.get("SecOPN2"));
        chips[128 + 2] = audio.plugin.chipLED.get("SecOPNA");
        audio.plugin.chipLED.put("SecOPNA", audio.plugin.chipLED.get("SecOPNA"));
        chips[128 + 3] = audio.plugin.chipLED.get("SecOPNB");
        audio.plugin.chipLED.put("SecOPNB", audio.plugin.chipLED.get("SecOPNB"));

        chips[128 + 4] = audio.plugin.chipLED.get("SecOPM");
        audio.plugin.chipLED.put("SecOPM", audio.plugin.chipLED.get("SecOPM"));
        chips[128 + 5] = audio.plugin.chipLED.get("SecDCSG");
        audio.plugin.chipLED.put("SecDCSG", audio.plugin.chipLED.get("SecDCSG"));
        chips[128 + 6] = audio.plugin.chipLED.get("SecRF5C");
        audio.plugin.chipLED.put("SecRF5C", audio.plugin.chipLED.get("SecRF5C"));
        chips[128 + 7] = audio.plugin.chipLED.get("SecPWM");
        audio.plugin.chipLED.put("SecPWM", audio.plugin.chipLED.get("SecPWM"));

        chips[128 + 8] = audio.plugin.chipLED.get("SecOKI5");
        audio.plugin.chipLED.put("SecOKI5", audio.plugin.chipLED.get("SecOKI5"));
        chips[128 + 9] = audio.plugin.chipLED.get("SecOKI9");
        audio.plugin.chipLED.put("SecOKI9", audio.plugin.chipLED.get("SecOKI9"));
        chips[128 + 10] = audio.plugin.chipLED.get("SecC140");
        audio.plugin.chipLED.put("SecC140", audio.plugin.chipLED.get("SecC140"));
        chips[128 + 11] = audio.plugin.chipLED.get("SecSPCM");
        audio.plugin.chipLED.put("SecSPCM", audio.plugin.chipLED.get("SecSPCM"));

        chips[128 + 12] = audio.plugin.chipLED.get("SecAY10");
        audio.plugin.chipLED.put("SecAY10", audio.plugin.chipLED.get("SecAY10"));
        chips[128 + 13] = audio.plugin.chipLED.get("SecOPLL");
        audio.plugin.chipLED.put("SecOPLL", audio.plugin.chipLED.get("SecOPLL"));
        chips[128 + 14] = audio.plugin.chipLED.get("SecHuC");
        audio.plugin.chipLED.put("SecHuC", audio.plugin.chipLED.get("SecHuC"));
        chips[128 + 15] = audio.plugin.chipLED.get("SecC352");
        audio.plugin.chipLED.put("SecC352", audio.plugin.chipLED.get("SecC352"));
        chips[128 + 16] = audio.plugin.chipLED.get("SecK054539");
        audio.plugin.chipLED.put("SecK054539", audio.plugin.chipLED.get("SecK054539"));
        chips[128 + 17] = audio.plugin.chipLED.get("SecRF5C68");
        audio.plugin.chipLED.put("SecRF5C68", audio.plugin.chipLED.get("SecRF5C68"));
*/
        return chips;
    }

    protected final Object lockObj = new Object();
    public boolean _fatalError = false;

    public boolean getFatalError() {
        synchronized (lockObj) {
            return _fatalError;
        }
    }

    public void setFatalError(boolean value) {
        synchronized (lockObj) {
            _fatalError = value;
        }
    }

//#region draw buffer

    private static void drawFaderH(FrameBuffer screen, int x, int y, int d, int v, int val1, int val2, int[] od, int[] ov, int[] oval1, int[] oval2) {
        if (d == od[0] && v == ov[0] && val1 == oval1[0] && val2 == oval2[0]) {
            return;
        }

        od[0] = d;
        ov[0] = v;
        oval1[0] = val1;
        oval2[0] = val2;

        drawFaderHP(screen, x, y, 2, v);
        for (int i = 0; i < 7 * 8 + 1; i++) {
            drawFaderHP(screen, x + 1 + i, y, (i < val2 ? (v == 0 ? 4 : 5) : 3), v);
        }
        drawFaderHP(screen, x + 2 + 7 * 8, y, 2, v);

        drawFaderHP(screen, x + val1 + 1, y, d, v);
    }

    private static void drawFaderHP(FrameBuffer screen, int x, int y, int c, int v) {
        c += v * 6;
        switch (c) {
            case 0:
                screen.drawByteArray(x - 1, y, FrameBuffer.rFaderH, 32, 0, 0, 3, 6);
                break;
            case 1:
                screen.drawByteArray(x - 1, y, FrameBuffer.rFaderH, 32, 3, 0, 3, 6);
                break;
            case 2:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 6, 0, 1, 6);
                break;
            case 3:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 7, 0, 1, 6);
                break;
            case 4:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 8, 0, 1, 6);
                break;
            case 5:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 9, 0, 1, 6);
                break;
            case 6:
                screen.drawByteArray(x - 1, y, FrameBuffer.rFaderH, 32, 0, 8, 3, 6);
                break;
            case 7:
                screen.drawByteArray(x - 1, y, FrameBuffer.rFaderH, 32, 3, 8, 3, 6);
                break;
            case 8:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 6, 8, 1, 6);
                break;
            case 9:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 7, 8, 1, 6);
                break;
            case 10:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 8, 8, 1, 6);
                break;
            case 11:
                screen.drawByteArray(x, y, FrameBuffer.rFaderH, 32, 9, 8, 1, 6);
                break;
        }
    }

    private static void drawTimer(FrameBuffer screen, int c, int ot1, int ot2, int ot3, int nt1, int nt2, int nt3) {
        if (ot1 != nt1) {
            // drawFont4Int2(mainScreen, 4 * 30 + c * 4 * 11, 0, 0, 3, nt1);
            drawFont8Int2(screen, 8 * 5 - 16 + c * 8 * 11 + 1, 1, 0, 3, nt1);
        }
        if (ot2 != nt2) {
            drawFont8Int2(screen, 8 * 9 - 16 + c * 8 * 11 + 1, 1, 0, 2, nt2);
            // drawFont4Int2(mainScreen, 4 * 34 + c * 4 * 11, 0, 0, 2, nt2);
        }
        if (ot3 != nt3) {
            drawFont8Int2(screen, 8 * 12 - 16 + c * 8 * 11 + 1, 1, 0, 2, nt3);
            // drawFont4Int2(mainScreen, 4 * 37 + c * 4 * 11, 0, 0, 2, nt3);
        }
    }

    private static void drawFont8Int2(FrameBuffer screen, int x, int y, int t, int k, int num) {
        if (screen == null)
            return;

        int n;
        if (k == 3) {
            n = num / 100;
            num -= n * 100;

            n = (n > 9) ? 0 : n;
            if (n == 0)
                screen.drawByteArray(x, y, FrameBuffer.rFont1[t], 128, 0, 0, 8, 8);
            else
                screen.drawByteArray(x, y, FrameBuffer.rFont1[t], 128, 0, 8, 8, 8);

            n = num / 10;
            num -= n * 10;
            x += 8;
            screen.drawByteArray(x, y, FrameBuffer.rFont1[t], 128, n * 8, 8, 8, 8);

            n = num / 1;
            x += 8;
            screen.drawByteArray(x, y, FrameBuffer.rFont1[t], 128, n * 8, 8, 8, 8);
            return;
        }

        n = num / 10;
        num -= n * 10;
        n = (n > 9) ? 0 : n;
        screen.drawByteArray(x, y, FrameBuffer.rFont1[t], 128, n * 8, 8, 8, 8);

        n = num / 1;
        x += 8;
        screen.drawByteArray(x, y, FrameBuffer.rFont1[t], 128, n * 8, 8, 8, 8);
    }

//#endregion
}
