package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import dotnet4j.io.Directory;
import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileNotFoundException;
import dotnet4j.io.FileStream;
import dotnet4j.io.IOException;
import dotnet4j.io.Path;
import dotnet4j.io.StreamWriter;
import dotnet4j.util.compat.Tuple;
import dotnet4j.util.compat.Tuple4;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmInstFormat;
import mdplayer.Common.EnmModel;
import mdplayer.DoubleBuffer;
import mdplayer.DrawBuff;
import mdplayer.KeyboardHook;
import mdplayer.MDChipParams;
import mdplayer.MIDIParam;
import mdplayer.MmfControl;
import mdplayer.OpeManager;
import mdplayer.PlayList;
import mdplayer.Request;
import mdplayer.Request.enmRequest;
import mdplayer.Setting;
import mdplayer.TonePallet;
import mdplayer.YM2612MIDI;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.form.kb.driver.frmPPZ8;
import mdplayer.form.kb.frmMIDI;
import mdplayer.form.kb.frmRegTest;
import mdplayer.form.kb.frmYM2151;
import mdplayer.form.kb.frmYMZ280B;
import mdplayer.form.kb.nes.frmDMG;
import mdplayer.form.kb.nes.frmFDS;
import mdplayer.form.kb.nes.frmMMC5;
import mdplayer.form.kb.nes.frmN106;
import mdplayer.form.kb.nes.frmNESDMC;
import mdplayer.form.kb.nes.frmS5B;
import mdplayer.form.kb.nes.frmVRC6;
import mdplayer.form.kb.nes.frmVRC7;
import mdplayer.form.kb.opl.frmY8950;
import mdplayer.form.kb.opl.frmYM2413;
import mdplayer.form.kb.opl.frmYM3526;
import mdplayer.form.kb.opl.frmYM3812;
import mdplayer.form.kb.opl.frmYMF262;
import mdplayer.form.kb.opl.frmYMF278B;
import mdplayer.form.kb.opn.frmYM2203;
import mdplayer.form.kb.opn.frmYM2608;
import mdplayer.form.kb.opn.frmYM2610;
import mdplayer.form.kb.opn.frmYM2612;
import mdplayer.form.kb.opn.frmYM2612MIDI;
import mdplayer.form.kb.opx.frmYMF271;
import mdplayer.form.kb.pcm.frmC140;
import mdplayer.form.kb.pcm.frmC352;
import mdplayer.form.kb.pcm.frmMegaCD;
import mdplayer.form.kb.pcm.frmMultiPCM;
import mdplayer.form.kb.pcm.frmOKIM6258;
import mdplayer.form.kb.pcm.frmOKIM6295;
import mdplayer.form.kb.pcm.frmQSound;
import mdplayer.form.kb.pcm.frmRf5c68;
import mdplayer.form.kb.pcm.frmSegaPCM;
import mdplayer.form.kb.psg.frmAY8910;
import mdplayer.form.kb.psg.frmSN76489;
import mdplayer.form.kb.wf.frmHuC6280;
import mdplayer.form.kb.wf.frmK051649;
import mdplayer.format.FileFormat;
import mdplayer.format.M3UFileFormat;
import mdplayer.format.ZIPFileFormat;
import mdplayer.plugin.BasePlugin;
import mdplayer.properties.Resources;
import mdsound.chips.OotakeHuC6280;
import mdsound.chips.K051649;
import mdsound.np.chip.NesN106;
import vavi.util.Debug;

import static dotnet4j.io.Path.getDirectoryName;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class frmMain extends JFrame {

    ResourceBundle rb = ResourceBundle.getBundle("mdplayer/form/sys/frmMain", Locale.getDefault());

    static final Point empty = new Point(0, 0);

    private BufferedImage pbRf5c164Screen;
    private DoubleBuffer screen;
    private int pWidth = 0;
    private int pHeight = 0;

    private frmInfo frmInfo = null;
    private frmPlayList frmPlayList = null;
//    private frmVSTeffectList frmVSTeffectList = null;

    private frmMegaCD[] frmMCD = new frmMegaCD[] {null, null};
    private frmRf5c68[] frmRf5c68 = new frmRf5c68[] {null, null};
    private frmC140[] frmC140 = new frmC140[] {null, null};
    private frmPPZ8[] frmPPZ8 = new frmPPZ8[] {null, null};
    private frmS5B[] frmS5B = new frmS5B[] {null, null};
    private frmDMG[] frmDMG = new frmDMG[] {null, null};
    private frmYMZ280B[] frmYMZ280B = new frmYMZ280B[] {null, null};
    private frmC352[] frmC352 = new frmC352[] {null, null};
    private frmMultiPCM[] frmMultiPCM = new frmMultiPCM[] {null, null};
    private frmQSound[] frmQSound = new frmQSound[] {null, null};
    private frmYM2608[] frmYM2608 = new frmYM2608[] {null, null};
    private frmYM2151[] frmYM2151 = new frmYM2151[] {null, null};
    private frmYM2203[] frmYM2203 = new frmYM2203[] {null, null};
    private frmYM2610[] frmYM2610 = new frmYM2610[] {null, null};
    private frmYM2612[] frmYM2612 = new frmYM2612[] {null, null};
    private frmYM3526[] frmYM3526 = new frmYM3526[] {null, null};
    private frmY8950[] frmY8950 = new frmY8950[] {null, null};
    private frmYM3812[] frmYM3812 = new frmYM3812[] {null, null};
    private frmOKIM6258[] frmOKIM6258 = new frmOKIM6258[] {null, null};
    private frmOKIM6295[] frmOKIM6295 = new frmOKIM6295[] {null, null};
    private frmSN76489[] frmSN76489 = new frmSN76489[] {null, null};
    private frmSegaPCM[] frmSegaPCM = new frmSegaPCM[] {null, null};
    private frmAY8910[] frmAY8910 = new frmAY8910[] {null, null};
    private frmHuC6280[] frmHuC6280 = new frmHuC6280[] {null, null};
    private frmK051649[] frmK051649 = new frmK051649[] {null, null};
    private frmYM2413[] frmYM2413 = new frmYM2413[] {null, null};
    private frmYMF262[] frmYMF262 = new frmYMF262[] {null, null};
    private frmYMF271[] frmYMF271 = new frmYMF271[] {null, null};
    private frmYMF278B[] frmYMF278B = new frmYMF278B[] {null, null};
    private frmMIDI[] frmMIDI = new frmMIDI[] {null, null};
    private frmYM2612MIDI frmYM2612MIDI = null;
    private frmMixer2 frmMixer2 = null;
    private frmNESDMC[] frmNESDMC = new frmNESDMC[] {null, null};
    private frmFDS[] frmFDS = new frmFDS[] {null, null};
    private frmMMC5[] frmMMC5 = new frmMMC5[] {null, null};
    private frmVRC6[] frmVRC6 = new frmVRC6[] {null, null};
    private frmVRC7[] frmVRC7 = new frmVRC7[] {null, null};
    private frmN106[] frmN106 = new frmN106[] {null, null};
    private frmRegTest frmRegTest;
    private frmVisWave frmVisWave;

    private List<JFrame[]> lstForm = new ArrayList<>();

    public MDChipParams oldParam = new MDChipParams();
    private MDChipParams newParam = new MDChipParams();

    private int[] oldButton = new int[18];
    private int[] newButton = new int[18];
    private int[] oldButtonMode = new int[18];
    private int[] newButtonMode = new int[18];

    private boolean isRunning = false;
    private boolean stopped = false;

    private boolean isInitialOpenFolder = true;

    private byte[] srcBuf;

    public Setting setting = Setting.load();
    public TonePallet tonePallet = TonePallet.load(null);

    private int frameSizeW = 0;
    private int frameSizeH = 0;

    private Transmitter midiin = null;
    private boolean forcedExit = false;
    private YM2612MIDI ym2612MIDI;
    private boolean flgReinit = false;
    public boolean reqAllScreenInit = true;

    private static final String[] modeTip = new String[] {
            "Mode\nNow:Step\nNext:Random",
            "Mode\nNow:Random\nNext:Loop",
            "Mode\nNow:Loop\nNext:LoopOne",
            "Mode\nNow:LoopOne\nNext:Step",
    };

    private static final String[] zoomTip = new String[] {
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
    private List<String[]> remoteReq = new ArrayList<>();

    public frmMain() {
        Debug.println(Level.SEVERE, "起動処理開始");
        Debug.println(Level.SEVERE, "frmMain(コンストラクタ):STEP 00");

        initializeComponent();
        DrawBuff.Init();

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

        Debug.println(Level.SEVERE, "frmMain(コンストラクタ):STEP 01");

        // 引数が指定されている場合のみプロセスチェックを行い、自分と同じアプリケーションが実行中ならばそちらに引数を渡し終了する
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

        Debug.println(Level.SEVERE, "frmMain(コンストラクタ):STEP 02");

//        pbScreen.AllowDrop = true;

        Debug.println(Level.SEVERE, "frmMain(コンストラクタ):STEP 03");
        if (setting == null) {
            Debug.println(Level.SEVERE, "frmMain(コンストラクタ):setting instanceof null");
        } else {
//            if ((Control.ModifierKeys & Keys.Shift) == Keys.Shift) {
//                int res = JOptionPane.showConfirmDialog(this,
//                        "ウィンドウの位置情報を初期化しますか？",
//                        "MDPlayer",
//                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//                if (res == JOptionPane.OK_OPTION) {
//                    ClearWindowPos();
//                }
//            }
        }

        Debug.println(Level.SEVERE, "起動時のAudio初期化処理開始");

        audio.init();

        ym2612MIDI = new mdplayer.YM2612MIDI(audio.audio.mdsMIDI, newParam);
        ym2612MIDI.fadeout = this::fadeout;
        ym2612MIDI.next = this::next;
        ym2612MIDI.ff = this::ff;
        ym2612MIDI.pause = this::pause;
        ym2612MIDI.play = this::play;
        ym2612MIDI.prev = this::prev;
        ym2612MIDI.slow = this::slow;
        ym2612MIDI.stop = this::stop;

        Debug.println(Level.SEVERE, "起動時のAudio初期化処理完了");

        StartMIDIInMonitoring();

        Debug.println(Level.SEVERE, "frmMain(コンストラクタ):STEP 04");

setVisible(true);
    }

    private void ClearWindowPos() {
        setting.setLocation(new Setting.Location());
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            frmMain_FormClosed(e);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            frmMain_FormClosing(e);
        }

        @Override
        public void windowOpened(WindowEvent e) {
            frmMain_Shown(e);
        }

        @Override
        public void windowActivated(WindowEvent e) {
            frmMain_Load(e);
        }
    };

    private void frmMain_Load(WindowEvent ev) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::SystemEvents_SessionEnding));

        Debug.println(Level.SEVERE, "frmMain_Load:STEP 05");

        if (!setting.getLocation().getPMain().equals(empty))
            this.setLocation(setting.getLocation().getPMain());

        // DoubleBufferオブジェクトの作成

        pbRf5c164Screen = new BufferedImage(320, 72, BufferedImage.TYPE_INT_ARGB);

        Debug.println(Level.SEVERE, "frmMain_Load:STEP 06");

        screen = new DoubleBuffer(pbScreen, Resources.getPlaneControl(), 1);
        screen.setting = setting;
        //oldParam = new MDChipParams();
        //newParam = new MDChipParams();
        reqAllScreenInit = true;

        Debug.println(Level.SEVERE, "frmMain_Load:STEP 07");

        pWidth = pbScreen.getWidth();
        pHeight = pbScreen.getHeight();

        frmPlayList = new frmPlayList(this);
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
            if (setting.getLocation().getOpenAY8910()[chipId]) OpenFormAY8910(chipId, false);
            if (setting.getLocation().getOpenC140()[chipId]) OpenFormC140(chipId, false);
            if (setting.getLocation().getOpenPPZ8()[chipId]) OpenFormPPZ8(chipId, false);
            if (setting.getLocation().getOpenS5B()[chipId]) OpenFormS5B(chipId, false);
            if (setting.getLocation().getOpenDMG()[chipId]) OpenFormDMG(chipId, false);
            if (setting.getLocation().getOpenYMZ280B()[chipId]) OpenFormYMZ280B(chipId, false);
            if (setting.getLocation().getOpenC352()[chipId]) OpenFormC352(chipId, false);
            if (setting.getLocation().getOpenMultiPCM()[chipId]) OpenFormMultiPCM(chipId, false);
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
            if (setting.getLocation().getOpenSN76489()[chipId]) OpenFormSN76489(chipId, false);
            if (setting.getLocation().getOpenSegaPCM()[chipId]) OpenFormSegaPCM(chipId, false);
            if (setting.getLocation().getOpenYm2151()[chipId]) OpenFormYM2151(chipId, false);
            if (setting.getLocation().getOpenYm2203()[chipId]) OpenFormYM2203(chipId, false);
            if (setting.getLocation().getOpenYm2413()[chipId]) OpenFormYM2413(chipId, false);
            if (setting.getLocation().getOpenYm2608()[chipId]) OpenFormYM2608(chipId, false);
            if (setting.getLocation().getOpenYm2610()[chipId]) OpenFormYM2610(chipId, false);
            if (setting.getLocation().getOpenYm2612()[chipId]) openFormYM2612(chipId, false);
            if (setting.getLocation().getOpenYm3526()[chipId]) OpenFormYM3526(chipId, false);
            if (setting.getLocation().getOpenY8950()[chipId]) OpenFormY8950(chipId, false);
            if (setting.getLocation().getOpenYm3812()[chipId]) OpenFormYM3812(chipId, false);
            if (setting.getLocation().getOpenYmf262()[chipId]) OpenFormYMF262(chipId, false);
            if (setting.getLocation().getOpenYMF271()[chipId]) OpenFormYMF271(chipId, false);
            if (setting.getLocation().getOpenYmf278b()[chipId]) OpenFormYMF278B(chipId, false);
            if (setting.getLocation().getOpenVrc6()[chipId]) openFormVRC6(chipId, false);
            if (setting.getLocation().getOpenVrc7()[chipId]) openFormVRC7(chipId, false);
            if (setting.getLocation().getOpenRegTest()[chipId]) openFormRegTest(chipId, null, false);
            if (setting.getLocation().getOpenN106()[chipId]) openFormN106(chipId, false);
        }

        Debug.println(Level.SEVERE, "frmMain_Load:STEP 08");

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
//                        opeButtonVST,
                        opeButtonMIDIKBD,
                        opeButtonZoom,
                        opeButtonMode,
                        opeButtonMode,
                        opeButtonMode
                };

        Debug.println(Level.SEVERE, "frmMain_Load:STEP 09");

         // //operationフォルダクリア
        //opeFolder = mdplayer.Common.GetOperationFolder(true);
        //startWatch(opeFolder);
        mmf = new MmfControl(false, "MDPlayer", 1024 * 4);
    }

    //private void startWatch(String opeFolder) {
    //    if (watcher != null) return;

    //    watcher = new FileSystemWatcher();
    //    watcher.Path = Path.GetDirectoryName(opeFolder);
    //    watcher.NotifyFilter = (
    //        NotifyFilters.LastAccess
    //        | NotifyFilters.LastWrite
    //        | NotifyFilters.FileName
    //        | NotifyFilters.DirectoryName
    //        | NotifyFilters.CreationTime
    //        | NotifyFilters.Attributes
    //        );
    //    watcher.Filter = ""; //  Path.getFileName(opeFolder);
    //    watcher.SynchronizingObject = this;

    //    watcher.Changed += new FileSystemEventHandler(watcher_Changed);
    //    watcher.Created += new FileSystemEventHandler(watcher_Changed);

    //    watcher.EnableRaisingEvents = true;
    //}

    //private void stopWatch() {
    //    watcher.EnableRaisingEvents = false;
    //    watcher.dispose();
    //    watcher = null;
    //}

    private void watcher_Changed(WatchEvent<?> e) {
        String trgFile = Path.combine(opeFolder, "ope.txt");

        synchronized (remoteLockObj) {
            if (remoteBusy) {
                try {
                    File.delete(trgFile);
                } catch (Exception deleteEx) {
                    deleteEx.printStackTrace();
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
                        File.delete(trgFile);
                    } catch (Exception deleteEx) {
                        deleteEx.printStackTrace();
                    }
                    return;
                }
                now = n;

                if (!File.exists(trgFile)) return;
                List<String> lins = null;
                int retry = 30;
                while (retry > 0) {
                    try {
                        lins = Files.readAllLines(Paths.get(trgFile));
                        retry = 0;
                    } catch (IOException e1) {
                        Debug.println(Level.WARNING, e);
                        Thread.sleep(100);
                        retry--;
                    }
                }

                try {
                    File.delete(trgFile);
                } catch (Exception deleteEx) {
                    deleteEx.printStackTrace();
                }

                remoteReq.add(lins.toArray(String[]::new));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }

    private void SystemEvents_SessionEnding() {
        this.setVisible(false);
    }

    private void changeZoom() {
        opeButtonZoom.setToolTipText(zoomTip[setting.getOther().getZoom() - 1]);

        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneControl().getWidth() * setting.getOther().getZoom(), frameSizeH + Resources.getPlaneControl().getHeight() * setting.getOther().getZoom()));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneControl().getWidth() * setting.getOther().getZoom(), frameSizeH + Resources.getPlaneControl().getHeight() * setting.getOther().getZoom()));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneControl().getWidth() * setting.getOther().getZoom(), frameSizeH + Resources.getPlaneControl().getHeight() * setting.getOther().getZoom()));
        componentListener.componentResized(null);
        RelocateOpeButton(setting.getOther().getZoom());

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
        Debug.println(Level.SEVERE, "frmMain_Shown:STEP 09");

        Thread trd = new Thread(this::screenMainLoop);
        trd.setPriority(Thread.MIN_PRIORITY);
        trd.start();
        String[] args = Common.getCommandLineArgs();

//        Application.DoEvents();
//        Activate();

        if (args.length < 2) {
            return;
        }

        Debug.println(Level.SEVERE, "frmMain_Shown:STEP 10");

        try {

            frmPlayList.stop();

            PlayList pl = frmPlayList.getPlayList();
            if (pl.getMusics().size() < 1 || !pl.getMusics().get(pl.getMusics().size() - 1).fileName.equals(args[1])) {
                pl.addFile(args[1]);
                //frmPlayList.AddList(args[1]);
            }

            if (!loadAndPlay(0, 0, args[1], "")) {
                frmPlayList.stop();
                OpeManager.requestToAudio(new Request(enmRequest.Stop, null, null));
                //audio.audio.Stop();
                return;
            }

            frmPlayList.setStart(-1);

            oldParam = new MDChipParams();
            frmPlayList.play();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "ファイルの読み込みに失敗しました。");
        }

        Debug.println(Level.SEVERE, "frmMain_Shown:STEP 11");
        Debug.println(Level.SEVERE, "起動処理完了");
    }

    private ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            // リサイズ時は再確保
//            if (screen != null) screen.setVisible(false);

            screen = new DoubleBuffer(pbScreen, Resources.getPlaneControl(), setting.getOther().getZoom());
            screen.setting = setting;
            reqAllScreenInit = true;
            //screen.screenInitAll();
        }
    };

    private void frmMain_FormClosing(WindowEvent e) {
        if (forcedExit) return;

        Debug.println(Level.SEVERE, "終了処理開始");
        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 00");

        frmPlayList.stop();
        frmPlayList.save();

        tonePallet.save(null);

        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 01");

        StopMIDIInMonitoring();
        Request req = new Request(enmRequest.Die, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {  // 自殺リクエストはコールバック無し
            Thread.yield();
        }

        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 02");

        isRunning = false;
        while (!stopped) {
                Thread.yield();
//            Application.DoEvents();
        }

        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 03");

        ym2612MIDI.close();

        // 解放
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

        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 04");

        if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
            setting.getLocation().setPMain(getLocation());
        } else {
            setting.getLocation().setPMain(getBounds().getLocation());
        }
        if (frmPlayList != null && !frmPlayList.isClosed) {
            frmPlayList.setVisible(false);
            setting.getLocation().setOPlayList(true);
        }
        if (frmInfo != null && !frmInfo.isClosed) {
            frmInfo.setVisible(false);
            setting.getLocation().setOInfo(true);
        }
        if (frmMixer2 != null && !frmMixer2.isClosed) {
            frmMixer2.setVisible(false);
            setting.getLocation().setOMixer(true);
        }
        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) {
            frmYM2612MIDI.setVisible(false);
            setting.getLocation().setOpenYm2612MIDI(true);
        }
//        if (frmVSTeffectList != null && !frmVSTeffectList.isClosed) {
//            frmVSTeffectList.setVisible(false);
//            setting.getLocation().setOpenVSTeffectList(true);
//        }

        for (int chipId = 0; chipId < 2; chipId++) {
            if (frmAY8910[chipId] != null && !frmAY8910[chipId].isClosed) {
                frmAY8910[chipId].setVisible(false);
                setting.getLocation().getOpenAY8910()[chipId] = true;
            }
            if (frmC140[chipId] != null && !frmC140[chipId].isClosed) {
                frmC140[chipId].setVisible(false);
                setting.getLocation().getOpenC140()[chipId] = true;
            }
            if (frmPPZ8[chipId] != null && !frmPPZ8[chipId].isClosed) {
                frmPPZ8[chipId].setVisible(false);
                setting.getLocation().getOpenPPZ8()[chipId] = true;
            }
            if (frmS5B[chipId] != null && !frmS5B[chipId].isClosed) {
                frmS5B[chipId].setVisible(false);
                setting.getLocation().getOpenS5B()[chipId] = true;
            }
            if (frmDMG[chipId] != null && !frmDMG[chipId].isClosed) {
                frmDMG[chipId].setVisible(false);
                setting.getLocation().getOpenDMG()[chipId] = true;
            }
            if (frmYMZ280B[chipId] != null && !frmYMZ280B[chipId].isClosed) {
                frmYMZ280B[chipId].setVisible(false);
                setting.getLocation().getOpenYMZ280B()[chipId] = true;
            }
            if (frmC352[chipId] != null && !frmC352[chipId].isClosed) {
                frmC352[chipId].setVisible(false);
                setting.getLocation().getOpenC352()[chipId] = true;
            }
            if (frmQSound[chipId] != null && !frmQSound[chipId].isClosed) {
                frmQSound[chipId].setVisible(false);
                setting.getLocation().getOpenQSound()[chipId] = true;
            }
            if (frmFDS[chipId] != null && !frmFDS[chipId].isClosed) {
                frmFDS[chipId].setVisible(false);
                setting.getLocation().getOpenFDS()[chipId] = true;
            }
            if (frmHuC6280[chipId] != null && !frmHuC6280[chipId].isClosed) {
                frmHuC6280[chipId].setVisible(false);
                setting.getLocation().getOpenHuC6280()[chipId] = true;
            }
            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) {
                frmK051649[chipId].setVisible(false);
                setting.getLocation().getOpenK051649()[chipId] = true;
            }
            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) {
                frmK051649[chipId].setVisible(false);
                setting.getLocation().getOpenK051649()[chipId] = true;
            }
            if (frmMCD[chipId] != null && !frmMCD[chipId].isClosed) {
                frmMCD[chipId].setVisible(false);
                setting.getLocation().getOpenRf5c164()[chipId] = true;
            }
            if (frmRf5c68[chipId] != null && !frmRf5c68[chipId].isClosed) {
                frmRf5c68[chipId].setVisible(false);
                setting.getLocation().getOpenRf5c68()[chipId] = true;
            }
            if (frmMIDI[chipId] != null && !frmMIDI[chipId].isClosed) {
                frmMIDI[chipId].setVisible(false);
                setting.getLocation().getOpenMIDI()[chipId] = true;
            }
            if (frmMMC5[chipId] != null && !frmMMC5[chipId].isClosed) {
                frmMMC5[chipId].setVisible(false);
                setting.getLocation().getOpenMMC5()[chipId] = true;
            }
            if (frmVRC6[chipId] != null && !frmVRC6[chipId].isClosed) {
                frmVRC6[chipId].setVisible(false);
                setting.getLocation().getOpenVrc6()[chipId] = true;
            }
            if (frmVRC7[chipId] != null && !frmVRC7[chipId].isClosed) {
                frmVRC7[chipId].setVisible(false);
                setting.getLocation().getOpenVrc7()[chipId] = true;
            }
            if (frmN106[chipId] != null && !frmN106[chipId].isClosed) {
                frmN106[chipId].setVisible(false);
                setting.getLocation().getOpenN106()[chipId] = true;
            }
            if (frmNESDMC[chipId] != null && !frmNESDMC[chipId].isClosed) {
                frmNESDMC[chipId].setVisible(false);
                setting.getLocation().getOpenNESDMC()[chipId] = true;
            }
            if (frmOKIM6258[chipId] != null && !frmOKIM6258[chipId].isClosed) {
                frmOKIM6258[chipId].setVisible(false);
                setting.getLocation().getOpenOKIM6258()[chipId] = true;
            }
            if (frmOKIM6295[chipId] != null && !frmOKIM6295[chipId].isClosed) {
                frmOKIM6295[chipId].setVisible(false);
                setting.getLocation().getOpenOKIM6295()[chipId] = true;
            }
            if (frmSegaPCM[chipId] != null && !frmSegaPCM[chipId].isClosed) {
                frmSegaPCM[chipId].setVisible(false);
                setting.getLocation().getOpenSegaPCM()[chipId] = true;
            }
            if (frmSN76489[chipId] != null && !frmSN76489[chipId].isClosed) {
                frmSN76489[chipId].setVisible(false);
                setting.getLocation().getOpenSN76489()[chipId] = true;
            }
            if (frmYM2151[chipId] != null && !frmYM2151[chipId].isClosed) {
                frmYM2151[chipId].setVisible(false);
                setting.getLocation().getOpenYm2151()[chipId] = true;
            }
            if (frmYM2203[chipId] != null && !frmYM2203[chipId].isClosed) {
                frmYM2203[chipId].setVisible(false);
                setting.getLocation().getOpenYm2203()[chipId] = true;
            }
            if (frmYM2413[chipId] != null && !frmYM2413[chipId].isClosed) {
                frmYM2413[chipId].setVisible(false);
                setting.getLocation().getOpenYm2413()[chipId] = true;
            }
            if (frmYM2608[chipId] != null && !frmYM2608[chipId].isClosed) {
                frmYM2608[chipId].setVisible(false);
                setting.getLocation().getOpenYm2608()[chipId] = true;
            }
            if (frmYM2610[chipId] != null && !frmYM2610[chipId].isClosed) {
                frmYM2610[chipId].setVisible(false);
                setting.getLocation().getOpenYm2610()[chipId] = true;
            }
            if (frmYM2612[chipId] != null && !frmYM2612[chipId].isClosed) {
                frmYM2612[chipId].setVisible(false);
                setting.getLocation().getOpenYm2612()[chipId] = true;
            }
            if (frmYM3526[chipId] != null && !frmYM3526[chipId].isClosed) {
                frmYM3526[chipId].setVisible(false);
                setting.getLocation().getOpenYm3526()[chipId] = true;
            }
            if (frmY8950[chipId] != null && !frmY8950[chipId].isClosed) {
                frmY8950[chipId].setVisible(false);
                setting.getLocation().getOpenY8950()[chipId] = true;
            }
            if (frmYM3812[chipId] != null && !frmYM3812[chipId].isClosed) {
                frmYM3812[chipId].setVisible(false);
                setting.getLocation().getOpenYm3812()[chipId] = true;
            }
            if (frmYMF262[chipId] != null && !frmYMF262[chipId].isClosed) {
                frmYMF262[chipId].setVisible(false);
                setting.getLocation().getOpenYmf262()[chipId] = true;
            }
            if (frmYMF271[chipId] != null && !frmYMF271[chipId].isClosed) {
                frmYMF271[chipId].setVisible(false);
                setting.getLocation().getOpenYMF271()[chipId] = true;
            }
            if (frmYMF278B[chipId] != null && !frmYMF278B[chipId].isClosed) {
                frmYMF278B[chipId].setVisible(false);
                setting.getLocation().getOpenYmf278b()[chipId] = true;
            }

            if (frmRegTest != null && !frmRegTest.isClosed) {
                frmRegTest.setVisible(false);
                setting.getLocation().getOpenRegTest()[chipId] = true;
            }

            if (frmVisWave != null && !frmVisWave.isClosed) {
                frmVisWave.setVisible(false);
                setting.getLocation().setOpenVisWave(true);
            }
        }

        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 05");

        setting.save();

        Debug.println(Level.SEVERE, "frmMain_FormClosing:STEP 06");

        mmf.close();

        Debug.println(Level.SEVERE, "終了処理完了");
    }

    private MouseMotionListener pbScreen_MouseMove = new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent ev) {
            int px = ev.getX() / setting.getOther().getZoom();
            int py = ev.getY() / setting.getOther().getZoom();

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
    };

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            Arrays.fill(newButton, 0);
        }

        @Override
        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == MouseEvent.BUTTON2) {
                cmsMenu.setVisible(true);
                cmsMenu.setLocation(ev.getX(), ev.getY());
            }

            //int px = ev.getX() / setting.getother().getZoom();
            //int py = ev.getY() / setting.getother().getZoom();

            //if (py < 16) {
            //    if (px < 8 * 2) return;
            //    if (px < 8 * 5 + 4) {
            //        if (py < 8) tsmiPAY8910_Click(null);
            //        else tsmiSAY8910_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 7) {
            //        if (py < 8) tsmiPOPLL_Click(null);
            //        else tsmiSOPLL_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 9) {
            //        if (py < 8) tsmiPOPN_Click(null);
            //        else tsmiSOPN_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 11) {
            //        if (py < 8) tsmiPOPN2_Click(null);
            //        else tsmiSOPN2_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 13 + 4) {
            //        if (py < 8) tsmiPOPNA_Click(null);
            //        else tsmiSOPNA_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 16) {
            //        if (py < 8) tsmiPOPNB_Click(null);
            //        else tsmiSOPNB_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 18 + 4) {
            //        if (py < 8) tsmiPOPM_Click(null);
            //        else tsmiSOPM_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 20 + 4) {
            //        if (py < 8) tsmiPDCSG_Click(null);
            //        else tsmiSDCSG_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 23) {
            //        if (py < 8) tsmiPRF5C164_Click(null);
            //        else tsmiSRF5C164_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 25 + 4) {
            //        return;
            //    }
            //    if (px < 8 * 27 + 4) {
            //        if (py < 8) tsmiPOKIM6258_Click(null);
            //        else tsmiSOKIM6258_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 30) {
            //        if (py < 8) tsmiPOKIM6295_Click(null);
            //        else tsmiSOKIM6295_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 32 + 4) {
            //        if (py < 8) tsmiPC140_Click(null);
            //        else tsmiSC140_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 35) {
            //        if (py < 8) tsmiPSegaPCM_Click(null);
            //        else tsmiSSegaPCM_Click(null);
            //        return;
            //    }
            //    if (px < 8 * 37 + 4) {
            //        if (py < 8) tsmiPHuC6280_Click(null);
            //        else tsmiSHuC6280_Click(null);
            //        return;
            //    }
            //    return;
            //}
        }
    };

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
        OpenFormSN76489(0, false);
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
        OpenFormC140(0, false);
    }

    private void tsmiPPPZ8_Click(ActionEvent ev) {
        OpenFormPPZ8(0, false);
    }

    private void tsmiSPPZ8_Click(ActionEvent ev) {
        OpenFormPPZ8(1, false);
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
        OpenFormAY8910(0, false);
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
        OpenFormYM3812(0, false);
    }

    private void tsmiPOPL3_Click(ActionEvent ev) {
        OpenFormYMF262(0, false);
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
        OpenFormSN76489(1, false);
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
        OpenFormC140(1, false);
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
        OpenFormAY8910(1, false);
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
        OpenFormYM3812(1, false);
    }

    private void tsmiSOPL3_Click(ActionEvent ev) {
        OpenFormYMF262(1, false);
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


    private void OpenFormMegaCD(int chipId, boolean force/* = false*/) {
        if (frmMCD[chipId] != null) {
            if (!force) {
                CloseFormMegaCD(chipId);
                return;
            } else
                return;
        }

        frmMCD[chipId] = new frmMegaCD(this, chipId, setting.getOther().getZoom(), newParam.rf5c164[chipId], oldParam.rf5c164[chipId]);
        if (setting.getLocation().getPosRf5c164()[chipId].equals(empty)) {
            frmMCD[chipId].x = this.getLocation().x;
            frmMCD[chipId].y = this.getLocation().y + 264;
        } else {
            frmMCD[chipId].x = setting.getLocation().getPosRf5c164()[chipId].x;
            frmMCD[chipId].y = setting.getLocation().getPosRf5c164()[chipId].y;
        }

        frmMCD[chipId].setVisible(true);
        frmMCD[chipId].update();
        frmMCD[chipId].setTitle(String.format("RF5C164 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.rf5c164[chipId] = new MDChipParams.RF5C164();

        checkAndSetForm(frmMCD[chipId]);
    }

    private void CloseFormMegaCD(int chipId) {
        if (frmMCD[chipId] == null) return;

        try {
            frmMCD[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        try {
            frmMCD[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmMCD[chipId] = null;
    }

    private void OpenFormRf5c68(int chipId, boolean force/* = false*/) {
        if (frmRf5c68[chipId] != null) {
            if (!force) {
                CloseFormRf5c68(chipId);
                return;
            } else
                return;
        }

        frmRf5c68[chipId] = new frmRf5c68(this, chipId, setting.getOther().getZoom(), newParam.rf5c68[chipId], oldParam.rf5c68[chipId]);
        if (setting.getLocation().getPosRf5c68()[chipId].equals(empty)) {
            frmRf5c68[chipId].x = this.getLocation().x;
            frmRf5c68[chipId].y = this.getLocation().y + 264;
        } else {
            frmRf5c68[chipId].x = setting.getLocation().getPosRf5c68()[chipId].x;
            frmRf5c68[chipId].y = setting.getLocation().getPosRf5c68()[chipId].y;
        }

        frmRf5c68[chipId].setVisible(true);
        frmRf5c68[chipId].update();
        frmRf5c68[chipId].setTitle(String.format("RF5C68 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.rf5c68[chipId] = new MDChipParams.RF5C68();

        checkAndSetForm(frmRf5c68[chipId]);
    }

    private void CloseFormRf5c68(int chipId) {
        if (frmRf5c68[chipId] == null) return;

        try {
            frmRf5c68[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        try {
            frmRf5c68[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmRf5c68[chipId] = null;
    }


    private void OpenFormYMF271(int chipId, boolean force/* = false*/) {
        if (frmYMF271[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                CloseFormYMF271(chipId);
                return;
            } else
                return;
        }

        frmYMF271[chipId] = new frmYMF271(this, chipId, setting.getOther().getZoom(), newParam.ymf271[chipId], oldParam.ymf271[chipId]);
        if (setting.getLocation().getPosYMF271()[chipId].equals(empty)) {
            frmYMF271[chipId].x = this.getLocation().x;
            frmYMF271[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMF271[chipId].x = setting.getLocation().getPosYMF271()[chipId].x;
            frmYMF271[chipId].y = setting.getLocation().getPosYMF271()[chipId].y;
        }

        frmYMF271[chipId].setVisible(true);
        frmYMF271[chipId].update();
        frmYMF271[chipId].setTitle(String.format("YMF271 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ymf271[chipId] = new MDChipParams.YMF271();

        checkAndSetForm(frmYMF271[chipId]);
    }

    private void CloseFormYMF271(int chipId) {
        if (frmYMF271[chipId] == null) return;

        try {
            frmYMF271[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        try {
            frmYMF271[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYMF271[chipId] = null;
    }

    private void OpenFormYM2608(int chipId, boolean force/* = false*/) {
        if (frmYM2608[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                CloseFormYM2608(chipId);
                return;
            } else
                return;
        }

        frmYM2608[chipId] = new frmYM2608(this, chipId, setting.getOther().getZoom(), newParam.ym2608[chipId], oldParam.ym2608[chipId]);

        if (setting.getLocation().getPosYm2608()[chipId].equals(empty)) {
            frmYM2608[chipId].x = this.getLocation().x;
            frmYM2608[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2608[chipId].x = setting.getLocation().getPosYm2608()[chipId].x;
            frmYM2608[chipId].y = setting.getLocation().getPosYm2608()[chipId].y;
        }

        frmYM2608[chipId].setVisible(true);
        frmYM2608[chipId].update();
        frmYM2608[chipId].setTitle(String.format("YM2608 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym2608[chipId] = new MDChipParams.YM2608();

        checkAndSetForm(frmYM2608[chipId]);
    }

    private void CloseFormYM2608(int chipId) {
        if (frmYM2608[chipId] == null) return;

        try {
            frmYM2608[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM2608[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM2608[chipId] = null;
    }

    private void OpenFormYM2151(int chipId, boolean force/* = false*/) {
        if (frmYM2151[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                CloseFormYM2151(chipId);
                return;
            } else return;
        }

        frmYM2151[chipId] = new frmYM2151(this, chipId, setting.getOther().getZoom(), newParam.ym2151[chipId], oldParam.ym2151[chipId]);

        if (setting.getLocation().getPosYm2151()[chipId].equals(empty)) {
            frmYM2151[chipId].x = this.getLocation().x;
            frmYM2151[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2151[chipId].x = setting.getLocation().getPosYm2151()[chipId].x;
            frmYM2151[chipId].y = setting.getLocation().getPosYm2151()[chipId].y;
        }

        frmYM2151[chipId].setVisible(true);
        frmYM2151[chipId].update();
        frmYM2151[chipId].setTitle(String.format("YM2151 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym2151[chipId] = new MDChipParams.YM2151();

        checkAndSetForm(frmYM2151[chipId]);
    }

    private void CloseFormYM2151(int chipId) {
        if (frmYM2151[chipId] == null) return;

        try {
            frmYM2151[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM2151[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM2151[chipId] = null;
    }

    private void OpenFormC140(int chipId, boolean force/* = false*/) {
        if (frmC140[chipId] != null) {
            if (!force) {
                CloseFormC140(chipId);
                return;
            } else return;
        }

        frmC140[chipId] = new frmC140(this, chipId, setting.getOther().getZoom(), newParam.c140[chipId], oldParam.c140[chipId]);

        if (setting.getLocation().getPosC140()[chipId].equals(empty)) {
            frmC140[chipId].x = this.getLocation().x;
            frmC140[chipId].y = this.getLocation().y + 264;
        } else {
            frmC140[chipId].x = setting.getLocation().getPosC140()[chipId].x;
            frmC140[chipId].y = setting.getLocation().getPosC140()[chipId].y;
        }

        frmC140[chipId].setVisible(true);
        frmC140[chipId].update();
        frmC140[chipId].setTitle(String.format("C140Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.c140[chipId] = new MDChipParams.C140();

        checkAndSetForm(frmC140[chipId]);
    }

    private void CloseFormC140(int chipId) {
        if (frmC140[chipId] == null) return;

        try {
            frmC140[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmC140[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmC140[chipId] = null;
    }

    private void OpenFormPPZ8(int chipId, boolean force/* = false*/) {
        if (frmPPZ8[chipId] != null) {
            if (!force) {
                CloseFormPPZ8(chipId);
                return;
            } else return;
        }

        frmPPZ8[chipId] = new frmPPZ8(this, chipId, setting.getOther().getZoom(), newParam.ppz8[chipId], oldParam.ppz8[chipId]);

        if (setting.getLocation().getPosPPZ8()[chipId].equals(empty)) {
            frmPPZ8[chipId].x = this.getLocation().x;
            frmPPZ8[chipId].y = this.getLocation().y + 264;
        } else {
            frmPPZ8[chipId].x = setting.getLocation().getPosPPZ8()[chipId].x;
            frmPPZ8[chipId].y = setting.getLocation().getPosPPZ8()[chipId].y;
        }

        frmPPZ8[chipId].setVisible(true);
        frmPPZ8[chipId].update();
        frmPPZ8[chipId].setTitle(String.format("Ppz8Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ppz8[chipId] = new MDChipParams.PPZ8();

        checkAndSetForm(frmPPZ8[chipId]);
    }

    private void CloseFormPPZ8(int chipId) {
        if (frmPPZ8[chipId] == null) return;

        try {
            frmPPZ8[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmPPZ8[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmPPZ8[chipId] = null;
    }

    private void OpenFormS5B(int chipId, boolean force/* = false*/) {
        if (frmS5B[chipId] != null) {
            if (!force) {
                CloseFormS5B(chipId);
                return;
            } else return;
        }

        frmS5B[chipId] = new frmS5B(this, chipId, setting.getOther().getZoom(), newParam.s5b[chipId], oldParam.s5b[chipId]);

        if (setting.getLocation().getPosS5B()[chipId].equals(empty)) {
            frmS5B[chipId].x = this.getLocation().x;
            frmS5B[chipId].y = this.getLocation().y + 264;
        } else {
            frmS5B[chipId].x = setting.getLocation().getPosS5B()[chipId].x;
            frmS5B[chipId].y = setting.getLocation().getPosS5B()[chipId].y;
        }

        frmS5B[chipId].setVisible(true);
        frmS5B[chipId].update();
        frmS5B[chipId].setTitle(String.format("S5B (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.s5b[chipId] = new MDChipParams.S5B();

        checkAndSetForm(frmS5B[chipId]);
    }

    private void CloseFormS5B(int chipId) {
        if (frmS5B[chipId] == null) return;

        try {
            frmS5B[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmS5B[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmS5B[chipId] = null;
    }

    private void OpenFormDMG(int chipId, boolean force/* = false*/) {
        if (frmDMG[chipId] != null) {
            if (!force) {
                CloseFormDMG(chipId);
                return;
            } else return;
        }

        frmDMG[chipId] = new frmDMG(this, chipId, setting.getOther().getZoom(), newParam.dmg[chipId], oldParam.dmg[chipId]);

        if (setting.getLocation().getPosDMG()[chipId].equals(empty)) {
            frmDMG[chipId].x = this.getLocation().x;
            frmDMG[chipId].y = this.getLocation().y + 264;
        } else {
            frmDMG[chipId].x = setting.getLocation().getPosDMG()[chipId].x;
            frmDMG[chipId].y = setting.getLocation().getPosDMG()[chipId].y;
        }

        frmDMG[chipId].setVisible(true);
        frmDMG[chipId].update();
        frmDMG[chipId].setTitle(String.format("DMG (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.dmg[chipId] = new MDChipParams.DMG();

        checkAndSetForm(frmDMG[chipId]);
    }

    private void CloseFormDMG(int chipId) {
        if (frmDMG[chipId] == null) return;

        try {
            frmDMG[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmDMG[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmDMG[chipId] = null;
    }

    private void OpenFormYMZ280B(int chipId, boolean force/* = false*/) {
        if (frmYMZ280B[chipId] != null) {
            if (!force) {
                CloseFormYMZ280B(chipId);
                return;
            } else return;
        }

        frmYMZ280B[chipId] = new frmYMZ280B(this, chipId, setting.getOther().getZoom(), newParam.ymz280b[chipId], oldParam.ymz280b[chipId]);

        if (setting.getLocation().getPosYMZ280B()[chipId].equals(empty)) {
            frmYMZ280B[chipId].x = this.getLocation().x;
            frmYMZ280B[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMZ280B[chipId].x = setting.getLocation().getPosYMZ280B()[chipId].x;
            frmYMZ280B[chipId].y = setting.getLocation().getPosYMZ280B()[chipId].y;
        }

        frmYMZ280B[chipId].setVisible(true);
        frmYMZ280B[chipId].update();
        frmYMZ280B[chipId].setTitle(String.format("YMZ280B (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ymz280b[chipId] = new MDChipParams.YMZ280B();

        checkAndSetForm(frmYMZ280B[chipId]);
    }

    private void CloseFormYMZ280B(int chipId) {
        if (frmYMZ280B[chipId] == null) return;

        try {
            frmYMZ280B[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYMZ280B[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYMZ280B[chipId] = null;
    }

    private void OpenFormC352(int chipId, boolean force/* = false*/) {
        if (frmC352[chipId] != null) {
            if (!force) {
                CloseFormC352(chipId);
                return;
            } else return;
        }

        frmC352[chipId] = new frmC352(this, chipId, setting.getOther().getZoom(), newParam.c352[chipId], oldParam.c352[chipId]);

        if (setting.getLocation().getPosC352()[chipId].equals(empty)) {
            frmC352[chipId].x = this.getLocation().x;
            frmC352[chipId].y = this.getLocation().y + 264;
        } else {
            frmC352[chipId].x = setting.getLocation().getPosC352()[chipId].x;
            frmC352[chipId].y = setting.getLocation().getPosC352()[chipId].y;
        }

        frmC352[chipId].setVisible(true);
        frmC352[chipId].update();
        frmC352[chipId].setTitle(String.format("C352Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.c352[chipId] = new MDChipParams.C352();

        checkAndSetForm(frmC352[chipId]);
    }

    private void CloseFormC352(int chipId) {
        if (frmC352[chipId] == null) return;

        try {
            frmC352[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmC352[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmC352[chipId] = null;
    }

    private void OpenFormMultiPCM(int chipId, boolean force/* = false*/) {
        if (frmMultiPCM[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                CloseFormMultiPCM(chipId);
                return;
            } else return;
        }

        frmMultiPCM[chipId] = new frmMultiPCM(this, chipId, setting.getOther().getZoom(), newParam.multiPCM[chipId], oldParam.multiPCM[chipId]);

        if (setting.getLocation().getPosMultiPCM()[chipId].equals(empty)) {
            frmMultiPCM[chipId].x = this.getLocation().x;
            frmMultiPCM[chipId].y = this.getLocation().y + 264;
        } else {
            frmMultiPCM[chipId].x = setting.getLocation().getPosMultiPCM()[chipId].x;
            frmMultiPCM[chipId].y = setting.getLocation().getPosMultiPCM()[chipId].y;
        }

        frmMultiPCM[chipId].setVisible(true);
        frmMultiPCM[chipId].update();
        frmMultiPCM[chipId].setTitle(String.format("MultiPCM (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.multiPCM[chipId] = new MDChipParams.MultiPCM();

        checkAndSetForm(frmMultiPCM[chipId]);
    }

    private void CloseFormMultiPCM(int chipId) {
        if (frmMultiPCM[chipId] == null) return;

        try {
            frmMultiPCM[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmMultiPCM[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmMultiPCM[chipId] = null;
    }

    private void OpenFormQSound(int chipId, boolean force/* = false*/) {
        if (frmQSound[chipId] != null) {
            if (!force) {
                CloseFormQSound(chipId);
                return;
            } else return;
        }

        frmQSound[chipId] = new frmQSound(this, chipId, setting.getOther().getZoom(), newParam.qSound[chipId], oldParam.qSound[chipId]);

        if (setting.getLocation().getPosQSound()[chipId].equals(empty)) {
            frmQSound[chipId].x = this.getLocation().x;
            frmQSound[chipId].y = this.getLocation().y + 264;
        } else {
            frmQSound[chipId].x = setting.getLocation().getPosQSound()[chipId].x;
            frmQSound[chipId].y = setting.getLocation().getPosQSound()[chipId].y;
        }

        frmQSound[chipId].setVisible(true);
        frmQSound[chipId].update();
        frmQSound[chipId].setTitle(String.format("QSoundInst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.qSound[chipId] = new MDChipParams.QSound();

        checkAndSetForm(frmQSound[chipId]);
    }

    private void CloseFormQSound(int chipId) {
        if (frmQSound[chipId] == null) return;

        try {
            frmQSound[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmQSound[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmQSound[chipId] = null;
    }

    private void OpenFormYM2203(int chipId, boolean force/* = false*/) {
        if (frmYM2203[chipId] != null) {
            if (!force) {
                CloseFormYM2203(chipId);
                return;
            } else return;
        }

        frmYM2203[chipId] = new frmYM2203(this, chipId, setting.getOther().getZoom(), newParam.ym2203[chipId], oldParam.ym2203[chipId]);

        if (setting.getLocation().getPosYm2203()[chipId].equals(empty)) {
            frmYM2203[chipId].x = this.getLocation().x;
            frmYM2203[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2203[chipId].x = setting.getLocation().getPosYm2203()[chipId].x;
            frmYM2203[chipId].y = setting.getLocation().getPosYm2203()[chipId].y;
        }

        frmYM2203[chipId].setVisible(true);
        frmYM2203[chipId].update();
        frmYM2203[chipId].setTitle(String.format("YM2203 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym2203[chipId] = new MDChipParams.YM2203();

        checkAndSetForm(frmYM2203[chipId]);
    }

    private void CloseFormYM2203(int chipId) {
        if (frmYM2203[chipId] == null) return;

        try {
            frmYM2203[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM2203[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM2203[chipId] = null;
    }

    private void OpenFormYM2610(int chipId, boolean force/* = false*/) {
        if (frmYM2610[chipId] != null) {
            if (!force) {
                CloseFormYM2610(chipId);
                return;
            } else return;
        }

        frmYM2610[chipId] = new frmYM2610(this, chipId, setting.getOther().getZoom(), newParam.ym2610[chipId], oldParam.ym2610[chipId]);

        if (setting.getLocation().getPosYm2610()[chipId].equals(empty)) {
            frmYM2610[chipId].x = this.getLocation().x;
            frmYM2610[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2610[chipId].x = setting.getLocation().getPosYm2610()[chipId].x;
            frmYM2610[chipId].y = setting.getLocation().getPosYm2610()[chipId].y;
        }

        frmYM2610[chipId].setVisible(true);
        frmYM2610[chipId].update();
        frmYM2610[chipId].setTitle(String.format("YM2610 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym2610[chipId] = new MDChipParams.YM2610();

        checkAndSetForm(frmYM2610[chipId]);
    }

    private void CloseFormYM2610(int chipId) {
        if (frmYM2610[chipId] == null) return;

        try {
            frmYM2610[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM2610[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM2610[chipId] = null;
    }

    private void openFormYM2612(int chipId, boolean force/* = false*/) {
        if (frmYM2612[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                closeFormYM2612(chipId);
                return;
            } else return;
        }

        oldParam.ym2612[chipId] = new MDChipParams.YM2612();
        for (int i = 0; i < oldParam.ym2612[chipId].channels.length; i++) {
            oldParam.ym2612[chipId].channels[i].mask = null;
        }
        frmYM2612[chipId] = new frmYM2612(this, chipId, setting.getOther().getZoom(), newParam.ym2612[chipId], oldParam.ym2612[chipId]);

        if (setting.getLocation().getPosYm2612()[chipId].equals(empty)) {
            frmYM2612[chipId].x = this.getLocation().x;
            frmYM2612[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2612[chipId].x = setting.getLocation().getPosYm2612()[chipId].x;
            frmYM2612[chipId].y = setting.getLocation().getPosYm2612()[chipId].y;
        }

        frmYM2612[chipId].setVisible(true);
        frmYM2612[chipId].update();
        frmYM2612[chipId].setTitle(String.format("Ym2612Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmYM2612[chipId]);
    }

    private void closeFormYM2612(int chipId) {
        if (frmYM2612[chipId] == null) return;
        try {
            frmYM2612[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM2612[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM2612[chipId] = null;
    }

    private void OpenFormOKIM6258(int chipId, boolean force/* = false*/) {
        if (frmOKIM6258[chipId] != null) {
            if (!force) {
                CloseFormOKIM6258(chipId);
                return;
            } else return;
        }

        frmOKIM6258[chipId] = new frmOKIM6258(this, chipId, setting.getOther().getZoom(), newParam.okim6258[chipId]);

        if (setting.getLocation().getPosOKIM6258()[chipId].equals(empty)) {
            frmOKIM6258[chipId].x = this.getLocation().x;
            frmOKIM6258[chipId].y = this.getLocation().y + 264;
        } else {
            frmOKIM6258[chipId].x = setting.getLocation().getPosOKIM6258()[chipId].x;
            frmOKIM6258[chipId].y = setting.getLocation().getPosOKIM6258()[chipId].y;
        }

        frmOKIM6258[chipId].setVisible(true);
        frmOKIM6258[chipId].update();
        frmOKIM6258[chipId].setTitle(String.format("OKIM6258 (%s)", chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmOKIM6258[chipId]);
    }

    private void CloseFormOKIM6258(int chipId) {
        if (frmOKIM6258[chipId] == null) return;

        try {
            frmOKIM6258[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmOKIM6258[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmOKIM6258[chipId] = null;
    }

    private void OpenFormOKIM6295(int chipId, boolean force/* = false*/) {
        if (frmOKIM6295[chipId] != null) {
            if (!force) {
                CloseFormOKIM6295(chipId);
                return;
            } else return;
        }

        frmOKIM6295[chipId] = new frmOKIM6295(this, chipId, setting.getOther().getZoom(), newParam.okim6295[chipId], oldParam.okim6295[chipId]);

        if (setting.getLocation().getPosOKIM6295()[chipId].equals(empty)) {
            frmOKIM6295[chipId].x = this.getLocation().x;
            frmOKIM6295[chipId].y = this.getLocation().y + 264;
        } else {
            frmOKIM6295[chipId].x = setting.getLocation().getPosOKIM6295()[chipId].x;
            frmOKIM6295[chipId].y = setting.getLocation().getPosOKIM6295()[chipId].y;
        }

        frmOKIM6295[chipId].setVisible(true);
        frmOKIM6295[chipId].update();
        frmOKIM6295[chipId].setTitle(String.format("OKIM6295 (%s)", chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmOKIM6295[chipId]);
    }

    private void CloseFormOKIM6295(int chipId) {
        if (frmOKIM6295[chipId] == null) return;

        try {
            frmOKIM6295[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmOKIM6295[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmOKIM6295[chipId] = null;
    }

    private void OpenFormSN76489(int chipId, boolean force/* = false*/) {
        if (frmSN76489[chipId] != null) {
            if (!force) {
                CloseFormSN76489(chipId);
                return;
            } else return;
        }

        frmSN76489[chipId] = new frmSN76489(this, chipId, setting.getOther().getZoom(), newParam.sn76489[chipId], oldParam.sn76489[chipId]);

        if (setting.getLocation().getPosSN76489()[chipId].equals(empty)) {
            frmSN76489[chipId].x = this.getLocation().x;
            frmSN76489[chipId].y = this.getLocation().y + 264;
        } else {
            frmSN76489[chipId].x = setting.getLocation().getPosSN76489()[chipId].x;
            frmSN76489[chipId].y = setting.getLocation().getPosSN76489()[chipId].y;
        }

        frmSN76489[chipId].setVisible(true);
        frmSN76489[chipId].update();
        frmSN76489[chipId].setTitle(String.format("SN76489 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.sn76489[chipId] = new MDChipParams.SN76489();

        checkAndSetForm(frmSN76489[chipId]);
    }

    private void CloseFormSN76489(int chipId) {
        if (frmSN76489[chipId] == null) return;

        try {
            frmSN76489[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmSN76489[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmSN76489[chipId] = null;
    }

    private void OpenFormSegaPCM(int chipId, boolean force/* = false*/) {
        if (frmSegaPCM[chipId] != null) {
            if (!force) {
                CloseFormSegaPCM(chipId);
                return;
            } else return;
        }

        frmSegaPCM[chipId] = new frmSegaPCM(this, chipId, setting.getOther().getZoom(), newParam.segaPcm[chipId], oldParam.segaPcm[chipId]);

        if (setting.getLocation().getPosSegaPCM()[chipId].equals(empty)) {
            frmSegaPCM[chipId].x = this.getLocation().x;
            frmSegaPCM[chipId].y = this.getLocation().y + 264;
        } else {
            frmSegaPCM[chipId].x = setting.getLocation().getPosSegaPCM()[chipId].x;
            frmSegaPCM[chipId].y = setting.getLocation().getPosSegaPCM()[chipId].y;
        }

        frmSegaPCM[chipId].setVisible(true);
        frmSegaPCM[chipId].update();
        frmSegaPCM[chipId].setTitle(String.format("SegaPCM (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.segaPcm[chipId] = new MDChipParams.SegaPcm();

        checkAndSetForm(frmSegaPCM[chipId]);
    }

    private void CloseFormSegaPCM(int chipId) {
        if (frmSegaPCM[chipId] == null) return;

        try {
            frmSegaPCM[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmSegaPCM[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmSegaPCM[chipId] = null;
    }


    private void OpenFormAY8910(int chipId, boolean force /*= false*/) {
        if (frmAY8910[chipId] != null) {
            if (!force) {
                CloseFormAY8910(chipId);
                return;
            } else return;
        }

        frmAY8910[chipId] = new frmAY8910(this, chipId, setting.getOther().getZoom(), newParam.ay8910[chipId], oldParam.ay8910[chipId]);

        if (setting.getLocation().getPosAY8910()[chipId].equals(empty)) {
            frmAY8910[chipId].x = this.getLocation().x;
            frmAY8910[chipId].y = this.getLocation().y + 264;
        } else {
            frmAY8910[chipId].x = setting.getLocation().getPosAY8910()[chipId].x;
            frmAY8910[chipId].y = setting.getLocation().getPosAY8910()[chipId].y;
        }

        frmAY8910[chipId].setVisible(true);
        frmAY8910[chipId].update();
        frmAY8910[chipId].setTitle(String.format("AY8910 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ay8910[chipId] = new MDChipParams.AY8910();

        checkAndSetForm(frmAY8910[chipId]);
    }

    private void CloseFormAY8910(int chipId) {
        if (frmAY8910[chipId] == null) return;

        try {
            frmAY8910[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmAY8910[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmAY8910[chipId] = null;
    }

    private void OpenFormHuC6280(int chipId, boolean force/* = false*/) {
        if (frmHuC6280[chipId] != null) {
            if (!force) {
                CloseFormHuC6280(chipId);
                return;
            } else return;
        }

        frmHuC6280[chipId] = new frmHuC6280(this, chipId, setting.getOther().getZoom(), newParam.huc6280[chipId], oldParam.huc6280[chipId]);

        if (setting.getLocation().getPosHuC6280()[chipId].equals(empty)) {
            frmHuC6280[chipId].x = this.getLocation().x;
            frmHuC6280[chipId].y = this.getLocation().y + 264;
        } else {
            frmHuC6280[chipId].x = setting.getLocation().getPosHuC6280()[chipId].x;
            frmHuC6280[chipId].y = setting.getLocation().getPosHuC6280()[chipId].y;
        }

        frmHuC6280[chipId].setVisible(true);
        frmHuC6280[chipId].update();
        frmHuC6280[chipId].setTitle(String.format("OotakeHuC6280 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.huc6280[chipId] = new MDChipParams.HuC6280();

        checkAndSetForm(frmHuC6280[chipId]);
    }

    private void CloseFormHuC6280(int chipId) {
        if (frmHuC6280[chipId] == null) return;

        try {
            frmHuC6280[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmHuC6280[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmHuC6280[chipId] = null;
    }

    private void OpenFormK051649(int chipId, boolean force/* = false*/) {
        if (frmK051649[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                CloseFormK051649(chipId);
                return;
            } else return;
        }

        frmK051649[chipId] = new frmK051649(this, chipId, setting.getOther().getZoom(), newParam.k051649[chipId]);

        if (setting.getLocation().getPosK051649()[chipId].equals(empty)) {
            frmK051649[chipId].x = this.getLocation().x;
            frmK051649[chipId].y = this.getLocation().y + 264;
        } else {
            frmK051649[chipId].x = setting.getLocation().getPosK051649()[chipId].x;
            frmK051649[chipId].y = setting.getLocation().getPosK051649()[chipId].y;
        }

        frmK051649[chipId].setVisible(true);
        frmK051649[chipId].update();
        frmK051649[chipId].setTitle(String.format("K051649Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.k051649[chipId] = new MDChipParams.K051649();

        checkAndSetForm(frmK051649[chipId]);
    }

    private void CloseFormK051649(int chipId) {
        if (frmK051649[chipId] == null) return;

        try {
            frmK051649[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmK051649[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmK051649[chipId] = null;
    }

    private void OpenFormYM2413(int chipId, boolean force/* = false*/) {
        if (frmYM2413[chipId] != null) {
            if (!force) {
                CloseFormYM2413(chipId);
                return;
            } else return;
        }

        frmYM2413[chipId] = new frmYM2413(this, chipId, setting.getOther().getZoom(), newParam.ym2413[chipId], oldParam.ym2413[chipId]);

        if (setting.getLocation().getPosYm2413()[chipId].equals(empty)) {
            frmYM2413[chipId].x = this.getLocation().x;
            frmYM2413[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM2413[chipId].x = setting.getLocation().getPosYm2413()[chipId].x;
            frmYM2413[chipId].y = setting.getLocation().getPosYm2413()[chipId].y;
        }

        frmYM2413[chipId].setVisible(true);
        frmYM2413[chipId].update();
        frmYM2413[chipId].setTitle(String.format("YM2413/VRC7 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym2413[chipId] = new MDChipParams.YM2413();

        checkAndSetForm(frmYM2413[chipId]);
    }

    private void CloseFormYM2413(int chipId) {
        if (frmYM2413[chipId] == null) return;

        try {
            frmYM2413[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM2413[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM2413[chipId] = null;
    }

    private void OpenFormYM3526(int chipId, boolean force/* = false*/) {
        if (frmYM3526[chipId] != null) {
            if (!force) {
                CloseFormYM3526(chipId);
                return;
            } else return;
        }

        frmYM3526[chipId] = new frmYM3526(this, chipId, setting.getOther().getZoom(), newParam.ym3526[chipId], oldParam.ym3526[chipId]);

        if (setting.getLocation().getPosYm3526()[chipId].equals(empty)) {
            frmYM3526[chipId].x = this.getLocation().x;
            frmYM3526[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM3526[chipId].x = setting.getLocation().getPosYm3526()[chipId].x;
            frmYM3526[chipId].y = setting.getLocation().getPosYm3526()[chipId].y;
        }

        frmYM3526[chipId].setVisible(true);
        frmYM3526[chipId].update();
        frmYM3526[chipId].setTitle(String.format("YM3526 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym3526[chipId] = new MDChipParams.YM3526();

        checkAndSetForm(frmYM3526[chipId]);
    }

    private void CloseFormYM3526(int chipId) {
        if (frmYM3526[chipId] == null) return;

        try {
            frmYM3526[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM3526[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM3526[chipId] = null;
    }

    private void OpenFormY8950(int chipId, boolean force/* = false*/) {
        if (frmY8950[chipId] != null) {
            if (!force) {
                CloseFormY8950(chipId);
                return;
            } else return;
        }

        frmY8950[chipId] = new frmY8950(this, chipId, setting.getOther().getZoom(), newParam.y8950[chipId]);

        if (setting.getLocation().getPosY8950()[chipId].equals(empty)) {
            frmY8950[chipId].x = this.getLocation().x;
            frmY8950[chipId].y = this.getLocation().y + 264;
        } else {
            frmY8950[chipId].x = setting.getLocation().getPosY8950()[chipId].x;
            frmY8950[chipId].y = setting.getLocation().getPosY8950()[chipId].y;
        }

        frmY8950[chipId].setVisible(true);
        frmY8950[chipId].update();
        frmY8950[chipId].setTitle(String.format("Y8950Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.y8950[chipId] = new MDChipParams.Y8950();

        checkAndSetForm(frmY8950[chipId]);
    }

    private void CloseFormY8950(int chipId) {
        if (frmY8950[chipId] == null) return;

        try {
            frmY8950[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmY8950[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmY8950[chipId] = null;
    }

    private void OpenFormYM3812(int chipId, boolean force/* = false*/) {
        if (frmYM3812[chipId] != null) {
            if (!force) {
                CloseFormYM3812(chipId);
                return;
            } else return;
        }

        frmYM3812[chipId] = new frmYM3812(this, chipId, setting.getOther().getZoom(), newParam.ym3812[chipId], oldParam.ym3812[chipId]);

        if (setting.getLocation().getPosYm3812()[chipId].equals(empty)) {
            frmYM3812[chipId].x = this.getLocation().x;
            frmYM3812[chipId].y = this.getLocation().y + 264;
        } else {
            frmYM3812[chipId].x = setting.getLocation().getPosYm3812()[chipId].x;
            frmYM3812[chipId].y = setting.getLocation().getPosYm3812()[chipId].y;
        }

        frmYM3812[chipId].setVisible(true);
        frmYM3812[chipId].update();
        frmYM3812[chipId].setTitle(String.format("YM3812 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ym3812[chipId] = new MDChipParams.YM3812();

        checkAndSetForm(frmYM3812[chipId]);
    }

    private void CloseFormYM3812(int chipId) {
        if (frmYM3812[chipId] == null) return;

        try {
            frmYM3812[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYM3812[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYM3812[chipId] = null;
    }

    private void OpenFormYMF262(int chipId, boolean force/* = false*/) {
        if (frmYMF262[chipId] != null) {
            if (!force) {
                CloseFormYMF262(chipId);
                return;
            } else return;
        }

        frmYMF262[chipId] = new frmYMF262(this, chipId, setting.getOther().getZoom(), newParam.ymf262[chipId], oldParam.ymf262[chipId]);

        if (setting.getLocation().getPosYmf262()[chipId].equals(empty)) {
            frmYMF262[chipId].x = this.getLocation().x;
            frmYMF262[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMF262[chipId].x = setting.getLocation().getPosYmf262()[chipId].x;
            frmYMF262[chipId].y = setting.getLocation().getPosYmf262()[chipId].y;
        }

        frmYMF262[chipId].setVisible(true);
        frmYMF262[chipId].update();
        frmYMF262[chipId].setTitle(String.format("YMF262 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ymf262[chipId] = new MDChipParams.YMF262();

        checkAndSetForm(frmYMF262[chipId]);
    }

    private void CloseFormYMF262(int chipId) {
        if (frmYMF262[chipId] == null) return;

        try {
            frmYMF262[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYMF262[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYMF262[chipId] = null;
    }

    private void OpenFormYMF278B(int chipId, boolean force/* = false*/) {
        if (frmYMF278B[chipId] != null) {
            if (!force) {
                CloseFormYMF278B(chipId);
                return;
            } else return;
        }

        frmYMF278B[chipId] = new frmYMF278B(this, chipId, setting.getOther().getZoom(), newParam.ymf278b[chipId]);

        if (setting.getLocation().getPosYmf278b()[chipId].equals(empty)) {
            frmYMF278B[chipId].x = this.getLocation().x;
            frmYMF278B[chipId].y = this.getLocation().y + 264;
        } else {
            frmYMF278B[chipId].x = setting.getLocation().getPosYmf278b()[chipId].x;
            frmYMF278B[chipId].y = setting.getLocation().getPosYmf278b()[chipId].y;
        }

        frmYMF278B[chipId].setVisible(true);
        frmYMF278B[chipId].update();
        frmYMF278B[chipId].setTitle(String.format("YMF278B (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.ymf278b[chipId] = new MDChipParams.YMF278B();

        checkAndSetForm(frmYMF278B[chipId]);
    }

    private void CloseFormYMF278B(int chipId) {
        if (frmYMF278B[chipId] == null) return;

        try {
            frmYMF278B[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmYMF278B[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmYMF278B[chipId] = null;
    }

    private void OpenFormMIDI(int chipId, boolean force/* = false*/) {
        if (frmMIDI[chipId] != null) {
            if (!force) {
                closeFormMIDI(chipId);
                return;
            } else return;
        }

        frmMIDI[chipId] = new frmMIDI(this, chipId, setting.getOther().getZoom(), newParam.midi[chipId]);

        if (setting.getLocation().getPosMIDI()[chipId].equals(empty)) {
            frmMIDI[chipId].x = this.getLocation().x;
            frmMIDI[chipId].y = this.getLocation().y + 264;
        } else {
            frmMIDI[chipId].x = setting.getLocation().getPosMIDI()[chipId].x;
            frmMIDI[chipId].y = setting.getLocation().getPosMIDI()[chipId].y;
        }

        frmMIDI[chipId].setVisible(true);
        frmMIDI[chipId].update();
        frmMIDI[chipId].setTitle(String.format("MIDI (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.midi[chipId] = new MIDIParam();

        checkAndSetForm(frmMIDI[chipId]);
    }

    private void closeFormMIDI(int chipId) {
        if (frmMIDI[chipId] == null) return;

        try {
            frmMIDI[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmMIDI[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmMIDI[chipId] = null;
    }

    private void openFormNESDMC(int chipId, boolean force/* = false*/) {
        if (frmNESDMC[chipId] != null) {
            if (!force) {
                closeFormNESDMC(chipId);
                return;
            } else return;
        }

        frmNESDMC[chipId] = new frmNESDMC(this, chipId, setting.getOther().getZoom(), newParam.nesdmc[chipId]);

        if (setting.getLocation().getPosNESDMC()[chipId].equals(empty)) {
            frmNESDMC[chipId].x = this.getLocation().x;
            frmNESDMC[chipId].y = this.getLocation().y + 264;
        } else {
            frmNESDMC[chipId].x = setting.getLocation().getPosNESDMC()[chipId].x;
            frmNESDMC[chipId].y = setting.getLocation().getPosNESDMC()[chipId].y;
        }

        frmNESDMC[chipId].setVisible(true);
        frmNESDMC[chipId].update();
        frmNESDMC[chipId].setTitle(String.format("NES&DMC (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.nesdmc[chipId] = new MDChipParams.NESDMC();

        checkAndSetForm(frmNESDMC[chipId]);
    }

    private void closeFormNESDMC(int chipId) {
        if (frmNESDMC[chipId] == null) return;

        try {
            frmNESDMC[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmNESDMC[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmNESDMC[chipId] = null;
    }

    private void openFormFDS(int chipId, boolean force/* = false*/) {
        if (frmFDS[chipId] != null) {
            if (!force) {
                closeFormFDS(chipId);
                return;
            } else return;
        }

        frmFDS[chipId] = new frmFDS(this, chipId, setting.getOther().getZoom(), newParam.fds[chipId]);

        if (setting.getLocation().getPosFDS()[chipId].equals(empty)) {
            frmFDS[chipId].x = this.getLocation().x;
            frmFDS[chipId].y = this.getLocation().y + 264;
        } else {
            frmFDS[chipId].x = setting.getLocation().getPosFDS()[chipId].x;
            frmFDS[chipId].y = setting.getLocation().getPosFDS()[chipId].y;
        }

        frmFDS[chipId].setVisible(true);
        frmFDS[chipId].update();
        frmFDS[chipId].setTitle(String.format("FDS (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.fds[chipId] = new MDChipParams.FDS();

        checkAndSetForm(frmFDS[chipId]);
    }

    private void closeFormFDS(int chipId) {
        if (frmFDS[chipId] == null) return;

        try {
            frmFDS[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmFDS[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmFDS[chipId] = null;
    }

    private void openFormVRC6(int chipId, boolean force/* = false*/) {
        if (frmVRC6[chipId] != null) {
            if (!force) {
                closeFormVRC6(chipId);
                return;
            } else return;
        }

        oldParam.vrc6[chipId] = new MDChipParams.VRC6();
        for (int i = 0; i < oldParam.vrc6[chipId].channels.length; i++) {
            oldParam.vrc6[chipId].channels[i].mask = null;
        }
        frmVRC6[chipId] = new frmVRC6(this, chipId, setting.getOther().getZoom(), newParam.vrc6[chipId], oldParam.vrc6[chipId]);

        if (setting.getLocation().getPosVrc6()[chipId].equals(empty)) {
            frmVRC6[chipId].x = this.getLocation().x;
            frmVRC6[chipId].y = this.getLocation().y + 264;
        } else {
            frmVRC6[chipId].x = setting.getLocation().getPosVrc6()[chipId].x;
            frmVRC6[chipId].y = setting.getLocation().getPosVrc6()[chipId].y;
        }

        frmVRC6[chipId].setVisible(true);
        frmVRC6[chipId].update();
        frmVRC6[chipId].setTitle(String.format("Vrc6Inst (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.vrc6[chipId] = new MDChipParams.VRC6();

        checkAndSetForm(frmVRC6[chipId]);
    }

    private void closeFormVRC6(int chipId) {
        if (frmVRC6[chipId] == null) return;

        try {
            frmVRC6[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmVRC6[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmVRC6[chipId] = null;
    }

    private void openFormVRC7(int chipId, boolean force/* = false*/) {
        if (frmVRC7[chipId] != null)// && frmInfo.isClosed)
        {
            if (!force) {
                closeFormVRC7(chipId);
                return;
            } else return;
        }

        frmVRC7[chipId] = new frmVRC7(this, chipId, setting.getOther().getZoom(), newParam.vrc7[chipId]);

        if (setting.getLocation().getPosVrc7()[chipId].equals(empty)) {
            frmVRC7[chipId].x = this.getLocation().x;
            frmVRC7[chipId].y = this.getLocation().y + 264;
        } else {
            frmVRC7[chipId].x = setting.getLocation().getPosVrc7()[chipId].x;
            frmVRC7[chipId].y = setting.getLocation().getPosVrc7()[chipId].y;
        }

        frmVRC7[chipId].setVisible(true);
        frmVRC7[chipId].update();
        frmVRC7[chipId].setTitle(String.format("VRC7 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.vrc7[chipId] = new MDChipParams.VRC7();

        checkAndSetForm(frmVRC7[chipId]);
    }

    private void closeFormVRC7(int chipId) {
        if (frmVRC7[chipId] == null) return;

        try {
            frmVRC7[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmVRC7[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmVRC7[chipId] = null;
    }

    private void openFormMMC5(int chipId, boolean force/* = false*/) {
        if (frmMMC5[chipId] != null) {
            if (!force) {
                closeFormMMC5(chipId);
                return;
            } else return;
        }

        frmMMC5[chipId] = new frmMMC5(this, chipId, setting.getOther().getZoom(), newParam.mmc5[chipId]);

        if (setting.getLocation().getPosMMC5()[chipId].equals(empty)) {
            frmMMC5[chipId].x = this.getLocation().x;
            frmMMC5[chipId].y = this.getLocation().y + 264;
        } else {
            frmMMC5[chipId].x = setting.getLocation().getPosMMC5()[chipId].x;
            frmMMC5[chipId].y = setting.getLocation().getPosMMC5()[chipId].y;
        }

        frmMMC5[chipId].setVisible(true);
        frmMMC5[chipId].update();
        frmMMC5[chipId].setTitle(String.format("MMC5 (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.mmc5[chipId] = new MDChipParams.MMC5();

        checkAndSetForm(frmMMC5[chipId]);
    }

    private void closeFormMMC5(int chipId) {
        if (frmMMC5[chipId] == null) return;

        try {
            frmMMC5[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmMMC5[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmMMC5[chipId] = null;
    }

    private void openFormRegTest(int chipId, EnmChip selectedChip/* = EnmChip.Unuse*/, boolean force/* = false*/) {
        if (frmRegTest != null) {
            frmRegTest.changeChip(selectedChip);
            return;
        }

        frmRegTest = new frmRegTest(this, chipId, selectedChip, setting.getOther().getZoom());

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
        frmRegTest.setTitle(String.format("RegTest (%s)", chipId == 0 ? "Primary" : "Secondary"));

        checkAndSetForm(frmRegTest);
    }

    private void openFormVisWave() {
        if (frmVisWave != null && !frmVisWave.isClosed) {
            frmVisWave.requestFocus();
            return;
        }

        frmVisWave = new frmVisWave(this);

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

    private void openFormN106(int chipId, boolean force/* = false*/) {
        if (frmN106[chipId] != null) {
            if (!force) {
                closeFormN106(chipId);
                return;
            } else return;
        }

        oldParam.n106[chipId] = new MDChipParams.N106();
        for (int i = 0; i < oldParam.n106[chipId].channels.length; i++) {
            oldParam.n106[chipId].channels[i].mask = null;
        }
        frmN106[chipId] = new frmN106(this, chipId, setting.getOther().getZoom(), newParam.n106[chipId], oldParam.n106[chipId]);

        if (setting.getLocation().getPosN106()[chipId].equals(empty)) {
            frmN106[chipId].x = this.getLocation().x;
            frmN106[chipId].y = this.getLocation().y + 264;
        } else {
            frmN106[chipId].x = setting.getLocation().getPosN106()[chipId].x;
            frmN106[chipId].y = setting.getLocation().getPosN106()[chipId].y;
        }

        frmN106[chipId].setVisible(true);
        frmN106[chipId].update();
        frmN106[chipId].setTitle(String.format("N163(N106) (%s)", chipId == 0 ? "Primary" : "Secondary"));
        oldParam.n106[chipId] = new MDChipParams.N106();

        checkAndSetForm(frmN106[chipId]);
    }

    private void closeFormN106(int chipId) {
        if (frmN106[chipId] == null) return;

        try {
            frmN106[chipId].setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmN106[chipId].dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmN106[chipId] = null;
    }

    private void closeFormRegTest(int chipId) {
        if (frmRegTest == null) return;

        try {
            frmRegTest.setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmRegTest.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmRegTest = null;
    }

    private void closeFormVisWave() {
        if (frmVisWave == null) return;

        try {
            frmVisWave.setVisible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            frmVisWave.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frmVisWave = null;
    }

    private void openInfo() {
        if (frmInfo != null && !frmInfo.isClosed) {
            try {
                frmInfo.setVisible(false);
                frmInfo.dispose();
            } catch (Exception e) {
                e.printStackTrace();
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
                e.printStackTrace();
            } finally {
                frmInfo = null;
            }
        }

        frmInfo = new frmInfo(this);
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
                e.printStackTrace();
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
                e.printStackTrace();
            } finally {
                frmYM2612MIDI = null;
            }
        }

        frmYM2612MIDI = new frmYM2612MIDI(this, setting.getOther().getZoom(), newParam.ym2612Midi);
        if (setting.getLocation().getPosYm2612MIDI().equals(empty)) {
            frmYM2612MIDI.x = this.getLocation().x + 328;
            frmYM2612MIDI.y = this.getLocation().y;
        } else {
            frmYM2612MIDI.x = setting.getLocation().getPosYm2612MIDI().x;
            frmYM2612MIDI.y = setting.getLocation().getPosYm2612MIDI().y;
        }

//        Screen s = Screen.FromControl(frmYM2612MIDI);
        Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle rc = new Rectangle(frmYM2612MIDI.getLocation(), frmYM2612MIDI.getSize());
        if (s.contains(rc)) {
            frmYM2612MIDI.setLocation(rc.getLocation());
            frmYM2612MIDI.setPreferredSize(rc.getSize());
        } else {
            frmYM2612MIDI.setLocation(new Point(100, 100));
        }

        //frmYM2612MIDI.setting = setting;
        frmYM2612MIDI.setVisible(true);
        frmYM2612MIDI.update();
        oldParam.ym2612Midi = new MDChipParams.YM2612MIDI();
    }

    private void openSetting() {
        frmSetting frm = new frmSetting(setting);
        if (frm.showDialog() == JFileChooser.APPROVE_OPTION) {
            flgReinit = true;
            reinit(frm.setting);
        }
    }

    BasePlugin audio = new BasePlugin() {
        @Override
        public boolean play(String playingFileName, FileFormat format) {
            return false;
        }
    };

    private void reinit(Setting setting) {
        if (!flgReinit) return;

        StopMIDIInMonitoring();
        frmPlayList.stop();

        Request req = new Request(enmRequest.Stop, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {
                Thread.yield();
        }

        req = new Request(enmRequest.Die, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {
                Thread.yield();
        }

        //audio.audio.Stop();
        audio.audio.close();

        this.setting = setting;
        this.setting.save();

        screen.setting = this.setting;
        frmPlayList.setting = this.setting;
        //oldParam = new MDChipParams();
        //newParam = new MDChipParams();
        reqAllScreenInit = true;
        //screen.screenInitAll();

        Debug.println(Level.SEVERE, "設定が変更されたため、再度Audio初期化処理開始");

        audio.init();

        Debug.println(Level.SEVERE, "Audio初期化処理完了");

//        frmVSTeffectList.dispPluginList();
        StartMIDIInMonitoring();

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
                e.printStackTrace();
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
                e.printStackTrace();
            } finally {
                frmMixer2 = null;
            }
        }

        frmMixer2 = new frmMixer2(this, setting.getOther().getZoom(), newParam.mixer);
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
        oldParam.mixer = new MDChipParams.Mixer();
    }

//    private void pbScreen_DragEnter(DragEvent e) {
//        e.Effect = DragDropEffects.All;
//    }

    private void pbScreen_DragDrop(List<java.io.File> files) {
        String filename = files.get(0).getPath();

        try {
             // 曲を停止
            frmPlayList.stop();
            this.stop();
//            while (!audio.audio.isStopped())
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "ファイルの読み込みに失敗しました。");
        }
    }

//    @Override protected boolean getShowWithoutActivation() {
//        return true;
//    }

    private void allScreenInit() {
        //oldParam = new MDChipParams();
        DrawBuff.drawTimer(screen.mainScreen, 0, oldParam.Cminutes, oldParam.Csecond, oldParam.Cmillisecond, newParam.Cminutes, newParam.Csecond, newParam.Cmillisecond);
        DrawBuff.drawTimer(screen.mainScreen, 1, oldParam.TCminutes, oldParam.TCsecond, oldParam.TCmillisecond, newParam.TCminutes, newParam.TCsecond, newParam.TCmillisecond);
        DrawBuff.drawTimer(screen.mainScreen, 2, oldParam.LCminutes, oldParam.LCsecond, oldParam.LCmillisecond, newParam.LCminutes, newParam.LCsecond, newParam.LCmillisecond);
        screenInit(null);

        for (int i = 0; i < 2; i++) {
            if (frmAY8910[i] != null) frmAY8910[i].screenInit();
            //if (frmC140[i] != null) frmC140[i].screenInit();
            //if (frmC352[i] != null) frmC352[i].screenInit();
            if (frmFDS[i] != null) frmFDS[i].screenInit();
            //if (frmHuC6280[i] != null) frmHuC6280[i].screenInit();
            if (frmK051649[i] != null) frmK051649[i].screenInit();
            if (frmMCD[i] != null) frmMCD[i].screenInit();
            if (frmMIDI[i] != null) frmMIDI[i].screenInit();
            if (frmMMC5[i] != null) frmMMC5[i].screenInit();
            if (frmNESDMC[i] != null) frmNESDMC[i].screenInit();
            if (frmOKIM6258[i] != null) frmOKIM6258[i].screenInit();
            if (frmOKIM6295[i] != null) frmOKIM6295[i].screenInit();
            //if (frmSegaPCM[i] != null) frmSegaPCM[i].screenInit();
            //if (frmSN76489[i] != null) frmSN76489[i].screenInit();
            //if (frmYM2151[i] != null) frmYM2151[i].screenInit();
            //if (frmYM2203[i] != null) frmYM2203[i].screenInit();
            if (frmYM2413[i] != null) frmYM2413[i].screenInit();
            //if (frmYM2608[i] != null) frmYM2608[i].screenInit();
            //if (frmYM2610[i] != null) frmYM2610[i].screenInit();
            //if (frmYM2612[i] != null) frmYM2612[i].screenInit();
            if (frmYM3526[i] != null) frmYM3526[i].screenInit();
            if (frmY8950[i] != null) frmY8950[i].screenInit();
            if (frmYM3812[i] != null) frmYM3812[i].screenInit();
            if (frmYMF262[i] != null) frmYMF262[i].screenInit();
            if (frmYMF278B[i] != null) frmYMF278B[i].screenInit();
            if (frmVRC7[i] != null) frmVRC7[i].screenInit();

        }

        if (frmMixer2 != null) frmMixer2.screenInit();
        if (frmInfo != null) frmInfo.screenInit();
        //if (frmYM2612MIDI != null) frmYM2612MIDI.screenInit();

        if (frmRegTest != null) frmRegTest.screenInit();

        reqAllScreenInit = false;
    }

    /**
     * ！！このメソッドはメインスレッドで動いていません！！
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
                        Thread.sleep((int) (nextFrame - tickCount));
                    } catch (InterruptedException ignored) {
                    }
                }
                continue;
            }

            screenChangeParams();
            screenChangeParamsForms();

            if ((double) System.currentTimeMillis() >= nextFrame + period) {
                nextFrame += period;
                continue;
            }

//            this.screenDrawParams.run();
//            this.Invoke((Runnable) (screenDrawParamsForms));

            nextFrame += period;

            if (frmPlayList.isPlaying()) {
                if ((setting.getOther().getUseLoopTimes() && audio.getVgmCurLoopCounter() > setting.getOther().getLoopTimes() - 1)
                        || audio.getVGMStopped()) {
                    fadeout();
                }
                if (audio.isStopped() && frmPlayList.isPlaying()) {
                    nextPlayMode();
                }
            }

            if (audio.audio.getFatalError()) {
                Debug.println(Level.SEVERE, "AudioでFatalErrorが発生。再度Audio初期化処理開始");

                frmPlayList.stop();
                try {
                    Request req = new Request(enmRequest.Stop, null, null);
                    OpeManager.requestToAudio(req);
                    while (!req.getEnd()) Thread.sleep(1);
                    //audio.audio.Stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    audio.audio.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                audio.audio.setFatalError(false);
                audio.init();

                Debug.println(Level.SEVERE, "Audio初期化処理完了");
            }
        }

        stopped = true;
    }

    private void screenChangeParams() {

        long w = audio.getCounter();
        double sec = (double) w / (double) mdplayer.Common.VGMProcSampleRate;
        newParam.Cminutes = (int) (sec / 60);
        sec -= newParam.Cminutes * 60;
        newParam.Csecond = (int) sec;
        sec -= newParam.Csecond;
        newParam.Cmillisecond = (int) (sec * 100.0);

        w = audio.getTotalCounter();
        sec = (double) w / (double) mdplayer.Common.VGMProcSampleRate;
        newParam.TCminutes = (int) (sec / 60);
        sec -= newParam.TCminutes * 60;
        newParam.TCsecond = (int) sec;
        sec -= newParam.TCsecond;
        newParam.TCmillisecond = (int) (sec * 100.0);

        w = audio.getLoopCounter();
        sec = (double) w / (double) mdplayer.Common.VGMProcSampleRate;
        newParam.LCminutes = (int) (sec / 60);
        sec -= newParam.LCminutes * 60;
        newParam.LCsecond = (int) sec;
        sec -= newParam.LCsecond;
        newParam.LCmillisecond = (int) (sec * 100.0);

        updateOpeButtonActiveState();
    }

    private void screenChangeParamsForms() {
        for (int chipId = 0; chipId < 2; chipId++) {
            if (frmMCD[chipId] != null && !frmMCD[chipId].isClosed) frmMCD[chipId].screenChangeParams();
            else frmMCD[chipId] = null;

            if (frmRf5c68[chipId] != null && !frmRf5c68[chipId].isClosed) frmRf5c68[chipId].screenChangeParams();
            else frmRf5c68[chipId] = null;

            if (frmC140[chipId] != null && !frmC140[chipId].isClosed) frmC140[chipId].screenChangeParams();
            else frmC140[chipId] = null;

            if (frmS5B[chipId] != null && !frmS5B[chipId].isClosed) frmS5B[chipId].screenChangeParams();
            else frmS5B[chipId] = null;

            if (frmDMG[chipId] != null && !frmDMG[chipId].isClosed) frmDMG[chipId].screenChangeParams();
            else frmDMG[chipId] = null;

            if (frmPPZ8[chipId] != null && !frmPPZ8[chipId].isClosed) frmPPZ8[chipId].screenChangeParams();
            else frmPPZ8[chipId] = null;

            if (frmYMZ280B[chipId] != null && !frmYMZ280B[chipId].isClosed) frmYMZ280B[chipId].screenChangeParams();
            else frmYMZ280B[chipId] = null;

            if (frmC352[chipId] != null && !frmC352[chipId].isClosed) frmC352[chipId].screenChangeParams();
            else frmC352[chipId] = null;

            if (frmMultiPCM[chipId] != null && !frmMultiPCM[chipId].isClosed) frmMultiPCM[chipId].screenChangeParams();
            else frmMultiPCM[chipId] = null;

            if (frmQSound[chipId] != null && !frmQSound[chipId].isClosed) frmQSound[chipId].screenChangeParams();
            else frmQSound[chipId] = null;

            if (frmYM2608[chipId] != null && !frmYM2608[chipId].isClosed) frmYM2608[chipId].screenChangeParams();
            else frmYM2608[chipId] = null;

            if (frmYM2151[chipId] != null && !frmYM2151[chipId].isClosed) frmYM2151[chipId].screenChangeParams();
            else frmYM2151[chipId] = null;

            if (frmYM2203[chipId] != null && !frmYM2203[chipId].isClosed) frmYM2203[chipId].screenChangeParams();
            else frmYM2203[chipId] = null;

            if (frmYM2413[chipId] != null && !frmYM2413[chipId].isClosed) frmYM2413[chipId].screenChangeParams();
            else frmYM2413[chipId] = null;

            if (frmYM2610[chipId] != null && !frmYM2610[chipId].isClosed) frmYM2610[chipId].screenChangeParams();
            else frmYM2610[chipId] = null;

            if (frmYM2612[chipId] != null && !frmYM2612[chipId].isClosed) frmYM2612[chipId].screenChangeParams();
            else frmYM2612[chipId] = null;

            if (frmYM3526[chipId] != null && !frmYM3526[chipId].isClosed) frmYM3526[chipId].screenChangeParams();
            else frmYM3526[chipId] = null;

            if (frmY8950[chipId] != null && !frmY8950[chipId].isClosed) frmY8950[chipId].screenChangeParams();
            else frmY8950[chipId] = null;

            if (frmYM3812[chipId] != null && !frmYM3812[chipId].isClosed) frmYM3812[chipId].screenChangeParams();
            else frmYM3812[chipId] = null;

            if (frmYMF262[chipId] != null && !frmYMF262[chipId].isClosed) frmYMF262[chipId].screenChangeParams();
            else frmYMF262[chipId] = null;

            if (frmYMF271[chipId] != null && !frmYMF271[chipId].isClosed) frmYMF271[chipId].screenChangeParams();
            else frmYMF271[chipId] = null;

            if (frmYMF278B[chipId] != null && !frmYMF278B[chipId].isClosed) frmYMF278B[chipId].screenChangeParams();
            else frmYMF278B[chipId] = null;

            if (frmOKIM6258[chipId] != null && !frmOKIM6258[chipId].isClosed) frmOKIM6258[chipId].screenChangeParams();
            else frmOKIM6258[chipId] = null;

            if (frmOKIM6295[chipId] != null && !frmOKIM6295[chipId].isClosed) frmOKIM6295[chipId].screenChangeParams();
            else frmOKIM6295[chipId] = null;

            if (frmSN76489[chipId] != null && !frmSN76489[chipId].isClosed) frmSN76489[chipId].screenChangeParams();
            else frmSN76489[chipId] = null;

            if (frmSegaPCM[chipId] != null && !frmSegaPCM[chipId].isClosed) frmSegaPCM[chipId].screenChangeParams();
            else frmSegaPCM[chipId] = null;

            if (frmAY8910[chipId] != null && !frmAY8910[chipId].isClosed) {
                frmAY8910[chipId].screenChangeParams();
            } else frmAY8910[chipId] = null;

            if (frmHuC6280[chipId] != null && !frmHuC6280[chipId].isClosed) frmHuC6280[chipId].screenChangeParams();
            else frmHuC6280[chipId] = null;

            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) frmK051649[chipId].screenChangeParams();
            else frmK051649[chipId] = null;

            if (frmMIDI[chipId] != null && !frmMIDI[chipId].isClosed) frmMIDI[chipId].screenChangeParams();
            else frmMIDI[chipId] = null;

            if (frmNESDMC[chipId] != null && !frmNESDMC[chipId].isClosed) frmNESDMC[chipId].screenChangeParams();
            else frmNESDMC[chipId] = null;

            if (frmFDS[chipId] != null && !frmFDS[chipId].isClosed) frmFDS[chipId].screenChangeParams();
            else frmFDS[chipId] = null;

            if (frmMMC5[chipId] != null && !frmMMC5[chipId].isClosed) frmMMC5[chipId].screenChangeParams();
            else frmMMC5[chipId] = null;

            if (frmVRC6[chipId] != null && !frmVRC6[chipId].isClosed) frmVRC6[chipId].screenChangeParams();
            else frmVRC6[chipId] = null;

            if (frmVRC7[chipId] != null && !frmVRC7[chipId].isClosed) frmVRC7[chipId].screenChangeParams();
            else frmVRC7[chipId] = null;

            if (frmN106[chipId] != null && !frmN106[chipId].isClosed) frmN106[chipId].screenChangeParams();
            else frmN106[chipId] = null;

        }
        if (frmYM2612MIDI != null && !frmYM2612MIDI.isClosed) frmYM2612MIDI.screenChangeParams();
        else frmYM2612MIDI = null;
        if (frmMixer2 != null && !frmMixer2.isClosed) frmMixer2.screenChangeParams();
        else frmMixer2 = null;

        if (frmRegTest != null && !frmRegTest.isClosed) frmRegTest.screenChangeParams();
        else frmRegTest = null;
    }

    private void screenDrawParams() {
        // 描画

        for (int i = 0; i < lstOpeButtonActive.length; i++) {
            if (lstOpeButtonActive[i] != lstOpeButtonActiveOld[i]) {
                lstOpeButtonActiveOld[i] = lstOpeButtonActive[i];
                RedrawButton(lstOpeButtonControl[i]
                        , setting.getOther().getZoom()
                        , lstOpeButtonActive[i] ? lstOpeButtonActiveImage[i] : lstOpeButtonLeaveImage[i]
                );
            }
        }

        DrawBuff.drawTimer(screen.mainScreen, 0, oldParam.Cminutes, oldParam.Csecond, oldParam.Cmillisecond, newParam.Cminutes, newParam.Csecond, newParam.Cmillisecond);
        DrawBuff.drawTimer(screen.mainScreen, 1, oldParam.TCminutes, oldParam.TCsecond, oldParam.TCmillisecond, newParam.TCminutes, newParam.TCsecond, newParam.TCmillisecond);
        DrawBuff.drawTimer(screen.mainScreen, 2, oldParam.LCminutes, oldParam.LCsecond, oldParam.LCmillisecond, newParam.LCminutes, newParam.LCsecond, newParam.LCmillisecond);

        //byte[] chips = audio.audio.getChipStatus();
        //DrawBuff.drawChipName(screen.mainScreen, 14 * 4, 0 * 8, 0,oldParam.chipLED.PriOPN, chips[0]);
        //DrawBuff.drawChipName(screen.mainScreen, 18 * 4, 0 * 8, 1,oldParam.chipLED.PriOPN2, chips[1]);
        //DrawBuff.drawChipName(screen.mainScreen, 23 * 4, 0 * 8, 2,oldParam.chipLED.PriOPNA, chips[2]);
        //DrawBuff.drawChipName(screen.mainScreen, 28 * 4, 0 * 8, 3,oldParam.chipLED.PriOPNB, chips[3]);
        //DrawBuff.drawChipName(screen.mainScreen, 33 * 4, 0 * 8, 4,oldParam.chipLED.PriOPM, chips[4]);
        //DrawBuff.drawChipName(screen.mainScreen, 37 * 4, 0 * 8, 5,oldParam.chipLED.PriDCSG, chips[5]);
        //DrawBuff.drawChipName(screen.mainScreen, 42 * 4, 0 * 8, 6,oldParam.chipLED.PriRF5C, chips[6]);
        //DrawBuff.drawChipName(screen.mainScreen, 47 * 4, 0 * 8, 7,oldParam.chipLED.PriPWM, chips[7]);
        //DrawBuff.drawChipName(screen.mainScreen, 51 * 4, 0 * 8, 8,oldParam.chipLED.PriOKI5, chips[8]);
        //DrawBuff.drawChipName(screen.mainScreen, 56 * 4, 0 * 8, 9,oldParam.chipLED.PriOKI9, chips[9]);
        //DrawBuff.drawChipName(screen.mainScreen, 61 * 4, 0 * 8, 10,oldParam.chipLED.PriC140, chips[10]);
        //DrawBuff.drawChipName(screen.mainScreen, 66 * 4, 0 * 8, 11,oldParam.chipLED.PriSPCM, chips[11]);
        //DrawBuff.drawChipName(screen.mainScreen, 4 * 4, 0 * 8, 12,oldParam.chipLED.PriAY10, chips[12]);
        //DrawBuff.drawChipName(screen.mainScreen, 9 * 4, 0 * 8, 13,oldParam.chipLED.PriOPLL, chips[13]);
        //DrawBuff.drawChipName(screen.mainScreen, 71 * 4, 0 * 8, 14,oldParam.chipLED.PriHuC8, chips[14]);

        //DrawBuff.drawChipName(screen.mainScreen, 14 * 4, 1 * 8, 0,oldParam.chipLED.SecOPN, chips[128 + 0]);
        //DrawBuff.drawChipName(screen.mainScreen, 18 * 4, 1 * 8, 1,oldParam.chipLED.SecOPN2, chips[128 + 1]);
        //DrawBuff.drawChipName(screen.mainScreen, 23 * 4, 1 * 8, 2,oldParam.chipLED.SecOPNA, chips[128 + 2]);
        //DrawBuff.drawChipName(screen.mainScreen, 28 * 4, 1 * 8, 3,oldParam.chipLED.SecOPNB, chips[128 + 3]);
        //DrawBuff.drawChipName(screen.mainScreen, 33 * 4, 1 * 8, 4,oldParam.chipLED.SecOPM, chips[128 + 4]);
        //DrawBuff.drawChipName(screen.mainScreen, 37 * 4, 1 * 8, 5,oldParam.chipLED.SecDCSG, chips[128 + 5]);
        //DrawBuff.drawChipName(screen.mainScreen, 42 * 4, 1 * 8, 6,oldParam.chipLED.SecRF5C, chips[128 + 6]);
        //DrawBuff.drawChipName(screen.mainScreen, 47 * 4, 1 * 8, 7,oldParam.chipLED.SecPWM, chips[128 + 7]);
        //DrawBuff.drawChipName(screen.mainScreen, 51 * 4, 1 * 8, 8,oldParam.chipLED.SecOKI5, chips[128 + 8]);
        //DrawBuff.drawChipName(screen.mainScreen, 56 * 4, 1 * 8, 9,oldParam.chipLED.SecOKI9, chips[128 + 9]);
        //DrawBuff.drawChipName(screen.mainScreen, 61 * 4, 1 * 8, 10,oldParam.chipLED.SecC140, chips[128 + 10]);
        //DrawBuff.drawChipName(screen.mainScreen, 66 * 4, 1 * 8, 11,oldParam.chipLED.SecSPCM, chips[128 + 11]);
        //DrawBuff.drawChipName(screen.mainScreen, 4 * 4, 1 * 8, 12,oldParam.chipLED.SecAY10, chips[128 + 12]);
        //DrawBuff.drawChipName(screen.mainScreen, 9 * 4, 1 * 8, 13,oldParam.chipLED.SecOPLL, chips[128 + 13]);
        //DrawBuff.drawChipName(screen.mainScreen, 71 * 4, 0 * 8, 14,oldParam.chipLED.SecHuC8, chips[128 + 14]);

        DrawBuff.drawFont4(screen.mainScreen, 1, 9, 1, audio.getIsDataBlock(EnmModel.VirtualModel) ? "VD" : "  ");
        DrawBuff.drawFont4(screen.mainScreen, 321 - 16, 9, 1, audio.getIsPcmRAMWrite(EnmModel.VirtualModel) ? "VP" : "  ");
        DrawBuff.drawFont4(screen.mainScreen, 1, 17, 1, audio.getIsDataBlock(EnmModel.RealModel) ? "RD" : "  ");
        DrawBuff.drawFont4(screen.mainScreen, 321 - 16, 17, 1, audio.getIsPcmRAMWrite(EnmModel.RealModel) ? "RP" : "  ");

        if (setting.getDebug_DispFrameCounter()) {
            long v = audio.getVirtualFrameCounter();
            if (v != -1) DrawBuff.drawFont8(screen.mainScreen, 0, 0, 0, String.format("EMU        : %12d ", v));
            long r = audio.getRealFrameCounter();
            if (r != -1) DrawBuff.drawFont8(screen.mainScreen, 0, 8, 0, String.format("REAL CHIP  : %12d ", r));
            long d = r - v;
            if (r != -1 && v != -1)
                DrawBuff.drawFont8(screen.mainScreen, 0, 16, 0, String.format("R.CHIP-EMU : %12d ", d));
            DrawBuff.drawFont8(screen.mainScreen, 0, 24, 0, String.format("PROC TIME  : %12d ", audio.audio.procTimePer1Frame));
        }

        screen.Refresh(null);

        audio.updateVol();

        String newInfo;
        Gd3 gd3 = audio.audio.getGd3();
        if (gd3 != null) {
            String title = gd3.trackName;
            String usedChips = gd3.usedChips;
            newInfo = String.format("MDPlayer - [%s] %s", usedChips, title);
        } else {
            newInfo = "MDPlayer";
        }

        try {
            setTitle(newInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void screenDrawParamsForms() {
        for (int chipId = 0; chipId < 2; chipId++) {
            if (frmMCD[chipId] != null && !frmMCD[chipId].isClosed) {
                frmMCD[chipId].screenDrawParams();
                frmMCD[chipId].update();
            } else frmMCD[chipId] = null;

            if (frmRf5c68[chipId] != null && !frmRf5c68[chipId].isClosed) {
                frmRf5c68[chipId].screenDrawParams();
                frmRf5c68[chipId].update();
            } else frmRf5c68[chipId] = null;

            if (frmC140[chipId] != null && !frmC140[chipId].isClosed) {
                frmC140[chipId].screenDrawParams();
                frmC140[chipId].update();
            } else frmC140[chipId] = null;

            if (frmPPZ8[chipId] != null && !frmPPZ8[chipId].isClosed) {
                frmPPZ8[chipId].screenDrawParams();
                frmPPZ8[chipId].update();
            } else frmPPZ8[chipId] = null;

            if (frmS5B[chipId] != null && !frmS5B[chipId].isClosed) {
                frmS5B[chipId].screenDrawParams();
                frmS5B[chipId].update();
            } else frmS5B[chipId] = null;

            if (frmDMG[chipId] != null && !frmDMG[chipId].isClosed) {
                frmDMG[chipId].screenDrawParams();
                frmDMG[chipId].update();
            } else frmDMG[chipId] = null;

            if (frmYMZ280B[chipId] != null && !frmYMZ280B[chipId].isClosed) {
                frmYMZ280B[chipId].screenDrawParams();
                frmYMZ280B[chipId].update();
            } else frmYMZ280B[chipId] = null;

            if (frmC352[chipId] != null && !frmC352[chipId].isClosed) {
                frmC352[chipId].screenDrawParams();
                frmC352[chipId].update();
            } else frmC352[chipId] = null;

            if (frmMultiPCM[chipId] != null && !frmMultiPCM[chipId].isClosed) {
                frmMultiPCM[chipId].screenDrawParams();
                frmMultiPCM[chipId].update();
            } else frmMultiPCM[chipId] = null;

            if (frmQSound[chipId] != null && !frmQSound[chipId].isClosed) {
                frmQSound[chipId].screenDrawParams();
                frmQSound[chipId].update();
            } else frmQSound[chipId] = null;

            if (frmYM2608[chipId] != null && !frmYM2608[chipId].isClosed) {
                frmYM2608[chipId].screenDrawParams();
                frmYM2608[chipId].update();
            } else frmYM2608[chipId] = null;

            if (frmYM2151[chipId] != null && !frmYM2151[chipId].isClosed) {
                frmYM2151[chipId].screenDrawParams();
                frmYM2151[chipId].update();
            } else frmYM2151[chipId] = null;

            if (frmYM2203[chipId] != null && !frmYM2203[chipId].isClosed) {
                frmYM2203[chipId].screenDrawParams();
                frmYM2203[chipId].update();
            } else frmYM2203[chipId] = null;

            if (frmYM2413[chipId] != null && !frmYM2413[chipId].isClosed) {
                frmYM2413[chipId].screenDrawParams();
                frmYM2413[chipId].update();
            } else frmYM2413[chipId] = null;

            if (frmYM2610[chipId] != null && !frmYM2610[chipId].isClosed) {
                frmYM2610[chipId].screenDrawParams();
                frmYM2610[chipId].update();
            } else frmYM2610[chipId] = null;

            if (frmYM2612[chipId] != null && !frmYM2612[chipId].isClosed) {
                frmYM2612[chipId].screenDrawParams();
                frmYM2612[chipId].update();
            } else frmYM2612[chipId] = null;

            if (frmYM3526[chipId] != null && !frmYM3526[chipId].isClosed) {
                frmYM3526[chipId].screenDrawParams();
                frmYM3526[chipId].update();
            } else frmYM3526[chipId] = null;

            if (frmY8950[chipId] != null && !frmY8950[chipId].isClosed) {
                frmY8950[chipId].screenDrawParams();
                frmY8950[chipId].update();
            } else frmY8950[chipId] = null;

            if (frmYM3812[chipId] != null && !frmYM3812[chipId].isClosed) {
                frmYM3812[chipId].screenDrawParams();
                frmYM3812[chipId].update();
            } else frmYM3812[chipId] = null;

            if (frmYMF262[chipId] != null && !frmYMF262[chipId].isClosed) {
                frmYMF262[chipId].screenDrawParams();
                frmYMF262[chipId].update();
            } else frmYMF262[chipId] = null;

            if (frmYMF271[chipId] != null && !frmYMF271[chipId].isClosed) {
                frmYMF271[chipId].screenDrawParams();
                frmYMF271[chipId].update();
            } else frmYMF271[chipId] = null;

            if (frmYMF278B[chipId] != null && !frmYMF278B[chipId].isClosed) {
                frmYMF278B[chipId].screenDrawParams();
                frmYMF278B[chipId].update();
            } else frmYMF278B[chipId] = null;

            if (frmOKIM6258[chipId] != null && !frmOKIM6258[chipId].isClosed) {
                frmOKIM6258[chipId].screenDrawParams();
                frmOKIM6258[chipId].update();
            } else frmOKIM6258[chipId] = null;

            if (frmOKIM6295[chipId] != null && !frmOKIM6295[chipId].isClosed) {
                frmOKIM6295[chipId].screenDrawParams();
                frmOKIM6295[chipId].update();
            } else frmOKIM6295[chipId] = null;

            if (frmSN76489[chipId] != null && !frmSN76489[chipId].isClosed) {
                frmSN76489[chipId].screenDrawParams();
                frmSN76489[chipId].update();
            } else frmSN76489[chipId] = null;

            if (frmSegaPCM[chipId] != null && !frmSegaPCM[chipId].isClosed) {
                frmSegaPCM[chipId].screenDrawParams();
                frmSegaPCM[chipId].update();
            } else frmSegaPCM[chipId] = null;

            if (frmAY8910[chipId] != null && !frmAY8910[chipId].isClosed) {
                frmAY8910[chipId].screenDrawParams();
                frmAY8910[chipId].update();
            } else frmAY8910[chipId] = null;

            if (frmHuC6280[chipId] != null && !frmHuC6280[chipId].isClosed) {
                frmHuC6280[chipId].screenDrawParams();
                frmHuC6280[chipId].update();
            } else frmHuC6280[chipId] = null;

            if (frmK051649[chipId] != null && !frmK051649[chipId].isClosed) {
                frmK051649[chipId].screenDrawParams();
                frmK051649[chipId].update();
            } else frmK051649[chipId] = null;

            if (frmMIDI[chipId] != null && !frmMIDI[chipId].isClosed) {
                frmMIDI[chipId].screenDrawParams();
                frmMIDI[chipId].update();
            } else frmMIDI[chipId] = null;

            if (frmNESDMC[chipId] != null && !frmNESDMC[chipId].isClosed) {
                frmNESDMC[chipId].screenDrawParams();
                frmNESDMC[chipId].update();
            } else frmNESDMC[chipId] = null;

            if (frmVRC6[chipId] != null && !frmVRC6[chipId].isClosed) {
                frmVRC6[chipId].screenDrawParams();
                frmVRC6[chipId].update();
            } else frmVRC6[chipId] = null;

            if (frmVRC7[chipId] != null && !frmVRC7[chipId].isClosed) {
                frmVRC7[chipId].screenDrawParams();
                frmVRC7[chipId].update();
            } else frmVRC7[chipId] = null;

            if (frmFDS[chipId] != null && !frmFDS[chipId].isClosed) {
                frmFDS[chipId].screenDrawParams();
                frmFDS[chipId].update();
            } else frmFDS[chipId] = null;

            if (frmMMC5[chipId] != null && !frmMMC5[chipId].isClosed) {
                frmMMC5[chipId].screenDrawParams();
                frmMMC5[chipId].update();
            } else frmMMC5[chipId] = null;

            if (frmN106[chipId] != null && !frmN106[chipId].isClosed) {
                frmN106[chipId].screenDrawParams();
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
            frmRegTest.screenDrawParams();
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

        oldParam.chipLED.put("PriOPN", 255);
        oldParam.chipLED.put("PriOPN2", 255);
        oldParam.chipLED.put("PriOPNA", 255);
        oldParam.chipLED.put("PriOPNB", 255);
        oldParam.chipLED.put("PriOPM", 255);
        oldParam.chipLED.put("PriDCSG", 255);
        oldParam.chipLED.put("PriRF5C", 255);
        oldParam.chipLED.put("PriRF5C68", 255);
        oldParam.chipLED.put("PriPWM", 255);
        oldParam.chipLED.put("PriOKI5", 255);
        oldParam.chipLED.put("PriOKI9", 255);
        oldParam.chipLED.put("PriC140", 255);
        oldParam.chipLED.put("PriSPCM", 255);
        oldParam.chipLED.put("PriAY10", 255);
        oldParam.chipLED.put("PriOPLL", 255);
        oldParam.chipLED.put("PriHuC8", 255);
        oldParam.chipLED.put("SecOPN", 255);
        oldParam.chipLED.put("SecOPN2", 255);
        oldParam.chipLED.put("SecOPNA", 255);
        oldParam.chipLED.put("SecOPNB", 255);
        oldParam.chipLED.put("SecOPM", 255);
        oldParam.chipLED.put("SecDCSG", 255);
        oldParam.chipLED.put("SecRF5C", 255);
        oldParam.chipLED.put("SecRF5C68", 255);
        oldParam.chipLED.put("SecPWM", 255);
        oldParam.chipLED.put("SecOKI5", 255);
        oldParam.chipLED.put("SecOKI9", 255);
        oldParam.chipLED.put("SecC140", 255);
        oldParam.chipLED.put("SecSPCM", 255);
        oldParam.chipLED.put("SecAY10", 255);
        oldParam.chipLED.put("SecOPLL", 255);
        oldParam.chipLED.put("SecHuC8", 255);

        //byte[] chips = audio.audio.GetChipStatus();
        //DrawBuff.drawChipName(screen.mainScreen, 14 * 4, 0 * 8, 0,oldParam.chipLED.PriOPN, chips[0]);
        //DrawBuff.drawChipName(screen.mainScreen, 18 * 4, 0 * 8, 1,oldParam.chipLED.PriOPN2, chips[1]);
        //DrawBuff.drawChipName(screen.mainScreen, 23 * 4, 0 * 8, 2,oldParam.chipLED.PriOPNA, chips[2]);
        //DrawBuff.drawChipName(screen.mainScreen, 28 * 4, 0 * 8, 3,oldParam.chipLED.PriOPNB, chips[3]);
        //DrawBuff.drawChipName(screen.mainScreen, 33 * 4, 0 * 8, 4,oldParam.chipLED.PriOPM, chips[4]);
        //DrawBuff.drawChipName(screen.mainScreen, 37 * 4, 0 * 8, 5,oldParam.chipLED.PriDCSG, chips[5]);
        //DrawBuff.drawChipName(screen.mainScreen, 42 * 4, 0 * 8, 6,oldParam.chipLED.PriRF5C, chips[6]);
        //DrawBuff.drawChipName(screen.mainScreen, 47 * 4, 0 * 8, 7,oldParam.chipLED.PriPWM, chips[7]);
        //DrawBuff.drawChipName(screen.mainScreen, 51 * 4, 0 * 8, 8,oldParam.chipLED.PriOKI5, chips[8]);
        //DrawBuff.drawChipName(screen.mainScreen, 56 * 4, 0 * 8, 9,oldParam.chipLED.PriOKI9, chips[9]);
        //DrawBuff.drawChipName(screen.mainScreen, 61 * 4, 0 * 8, 10,oldParam.chipLED.PriC140, chips[10]);
        //DrawBuff.drawChipName(screen.mainScreen, 66 * 4, 0 * 8, 11,oldParam.chipLED.PriSPCM, chips[11]);
        //DrawBuff.drawChipName(screen.mainScreen, 4 * 4, 0 * 8, 12,oldParam.chipLED.PriAY10, chips[12]);
        //DrawBuff.drawChipName(screen.mainScreen, 9 * 4, 0 * 8, 13,oldParam.chipLED.PriOPLL, chips[13]);
        //DrawBuff.drawChipName(screen.mainScreen, 71 * 4, 0 * 8, 14,oldParam.chipLED.PriHuC8, chips[14]);

        //DrawBuff.drawChipName(screen.mainScreen, 14 * 4, 1 * 8, 0,oldParam.chipLED.SecOPN, chips[128 + 0]);
        //DrawBuff.drawChipName(screen.mainScreen, 18 * 4, 1 * 8, 1,oldParam.chipLED.SecOPN2, chips[128 + 1]);
        //DrawBuff.drawChipName(screen.mainScreen, 23 * 4, 1 * 8, 2,oldParam.chipLED.SecOPNA, chips[128 + 2]);
        //DrawBuff.drawChipName(screen.mainScreen, 28 * 4, 1 * 8, 3,oldParam.chipLED.SecOPNB, chips[128 + 3]);
        //DrawBuff.drawChipName(screen.mainScreen, 33 * 4, 1 * 8, 4,oldParam.chipLED.SecOPM, chips[128 + 4]);
        //DrawBuff.drawChipName(screen.mainScreen, 37 * 4, 1 * 8, 5,oldParam.chipLED.SecDCSG, chips[128 + 5]);
        //DrawBuff.drawChipName(screen.mainScreen, 42 * 4, 1 * 8, 6,oldParam.chipLED.SecRF5C, chips[128 + 6]);
        //DrawBuff.drawChipName(screen.mainScreen, 47 * 4, 1 * 8, 7,oldParam.chipLED.SecPWM, chips[128 + 7]);
        //DrawBuff.drawChipName(screen.mainScreen, 51 * 4, 1 * 8, 8,oldParam.chipLED.SecOKI5, chips[128 + 8]);
        //DrawBuff.drawChipName(screen.mainScreen, 56 * 4, 1 * 8, 9,oldParam.chipLED.SecOKI9, chips[128 + 9]);
        //DrawBuff.drawChipName(screen.mainScreen, 61 * 4, 1 * 8, 10,oldParam.chipLED.SecC140, chips[128 + 10]);
        //DrawBuff.drawChipName(screen.mainScreen, 66 * 4, 1 * 8, 11,oldParam.chipLED.SecSPCM, chips[128 + 11]);
        //DrawBuff.drawChipName(screen.mainScreen, 4 * 4, 1 * 8, 12,oldParam.chipLED.SecAY10, chips[128 + 12]);
        //DrawBuff.drawChipName(screen.mainScreen, 9 * 4, 1 * 8, 13,oldParam.chipLED.SecOPLL, chips[128 + 13]);
        //DrawBuff.drawChipName(screen.mainScreen, 71 * 4, 0 * 8, 14,oldParam.chipLED.SecHuC8, chips[128 + 14]);

        DrawBuff.drawFont4(screen.mainScreen, 1, 9, 1, audio.getIsDataBlock(EnmModel.VirtualModel) ? "VD" : "  ");
        DrawBuff.drawFont4(screen.mainScreen, 321 - 16, 9, 1, audio.getIsPcmRAMWrite(EnmModel.VirtualModel) ? "VP" : "  ");
        DrawBuff.drawFont4(screen.mainScreen, 1, 17, 1, audio.getIsDataBlock(EnmModel.RealModel) ? "RD" : "  ");
        DrawBuff.drawFont4(screen.mainScreen, 321 - 16, 17, 1, audio.getIsPcmRAMWrite(EnmModel.RealModel) ? "RP" : "  ");

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
        if (audio.audio.isPaused()) {
            audio.audio.pause();
        }

        if (audio.audio.getTrdStopped() && audio.isStopped()) {
            audio.resetTimeCounter();
        }

        frmPlayList.stop();
        OpeManager.requestToAudio(new Request(enmRequest.Stop, null, this::screenInit));
        //audio.audio.Stop();
        //screenInit();
    }

    public void pause() {
        audio.audio.pause();
    }

    public void fadeout() {
        audio.audio.fadeoutCommand();
    }

    public void prev() {
        if (audio.audio.isPaused()) {
            audio.audio.pause();
        }

        frmPlayList.prevPlay(newButtonMode[9]);
    }

    public void play() {

        if (audio.audio.isPaused()) {
            audio.audio.pause();
        }

        String[] fn;
        Tuple4<Integer, Integer, String, String> playFn;

        frmPlayList.stop();

        //if (srcBuf == null && frmPlayList.getMusicCount() < 1)
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

    private void playData() {
        try {

            if (srcBuf == null) {

                audio.audio.errMsg = "cancel";
                return;
            }

            if (audio.audio.isPaused()) {
                audio.audio.pause();
            }
            //stop();

            //for (int chipId = 0; chipId < 2; chipId++) {
            //    for (int ch = 0; ch < 3; ch++) ForceChannelMask(EnmChip.AY8910, chipId, ch, newParam.ay8910[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 8; ch++) ForceChannelMask(EnmChip.YM2151, chipId, ch, newParam.ym2151[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 9; ch++) ForceChannelMask(EnmChip.YM2203, chipId, ch, newParam.ym2203[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 14; ch++) ForceChannelMask(EnmChip.YM2413, chipId, ch, newParam.ym2413[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 14; ch++) ForceChannelMask(EnmChip.YM2608, chipId, ch, newParam.ym2608[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 14; ch++) ForceChannelMask(EnmChip.YM2610, chipId, ch, newParam.ym2610[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 6; ch++) ForceChannelMask(EnmChip.Ym2612Inst, chipId, ch, newParam.ym2612[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 4; ch++) ForceChannelMask(EnmChip.SN76489, chipId, ch, newParam.sn76489[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 8; ch++) ForceChannelMask(EnmChip.RF5C164, chipId, ch, newParam.rf5c164[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 24; ch++) ForceChannelMask(EnmChip.C140Inst, chipId, ch, newParam.c140[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 32; ch++) ForceChannelMask(EnmChip.C352Inst, chipId, ch, newParam.c352[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 16; ch++) ForceChannelMask(EnmChip.SEGAPCM, chipId, ch, newParam.segaPcm[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 6; ch++) ForceChannelMask(EnmChip.OotakeHuC6280, chipId, ch, newParam.huc6280[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 4; ch++) ForceChannelMask(EnmChip.OKIM6295, chipId, ch, newParam.okim6295[chipId].channels[ch].mask);
            //    for (int ch = 0; ch < 2; ch++) ResetChannelMask(EnmChip.NES, chipId, ch);
            //    for (int ch = 0; ch < 3; ch++) ResetChannelMask(EnmChip.DMC, chipId, ch);
            //    for (int ch = 0; ch < 3; ch++) ResetChannelMask(EnmChip.MMC5, chipId, ch);
            //    ResetChannelMask(EnmChip.FDS, chipId, 0);
            //}

            //oldParam = new MDChipParams();
            //newParam = new MDChipParams();
            reqAllScreenInit = true;

            if (setting.getOther().getWavSwitch()) {
                if (!Directory.exists(setting.getOther().getWavPath())) {
                    int res = JOptionPane.showConfirmDialog(this,
                            "wavファイル出力先に設定されたパスが存在しません。作成し演奏を続けますか。"
                            , "パス作成確認"
                            , JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (res == JOptionPane.NO_OPTION) {
                        audio.audio.errMsg = "cancel";
                        return;
                    }
                    try {
                        Directory.createDirectory(setting.getOther().getWavPath());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this,
                                "パスの作成に失敗しました。演奏を停止します。"
                                , "作成失敗"
                                , JOptionPane.ERROR_MESSAGE);
                        audio.audio.errMsg = "cancel";
                        return;
                    }
                }
            }

            if (!audio.play()) {
                try {
                    frmPlayList.stop();
                    Request req = new Request(enmRequest.Stop, null, null);
                    OpeManager.requestToAudio(req);
                    //while (!req.end) Thread.sleep(1);
                    //audio.audio.Stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (audio.audio.errMsg.isEmpty()) throw new Exception();
                else {
                    JOptionPane.showMessageDialog(this, audio.audio.errMsg, "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            for (int chipId = 0; chipId < 2; chipId++) {
                for (int ch = 0; ch < 3; ch++)
                    forceChannelMask(EnmChip.AY8910, chipId, ch, newParam.ay8910[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(EnmChip.YM2151, chipId, ch, newParam.ym2151[chipId].channels[ch].mask);
                for (int ch = 0; ch < 9; ch++)
                    forceChannelMask(EnmChip.YM2203, chipId, ch, newParam.ym2203[chipId].channels[ch].mask);
                for (int ch = 0; ch < 14; ch++)
                    forceChannelMask(EnmChip.YM2413, chipId, ch, newParam.ym2413[chipId].channels[ch].mask);
                for (int ch = 0; ch < 14; ch++)
                    forceChannelMask(EnmChip.YM2608, chipId, ch, newParam.ym2608[chipId].channels[ch].mask);
                for (int ch = 0; ch < 14; ch++)
                    forceChannelMask(EnmChip.YM2610, chipId, ch, newParam.ym2610[chipId].channels[ch].mask);
                for (int ch = 0; ch < 6; ch++)
                    forceChannelMask(EnmChip.YM2612, chipId, ch, newParam.ym2612[chipId].channels[ch].mask);
                for (int ch = 0; ch < 4; ch++)
                    forceChannelMask(EnmChip.SN76489, chipId, ch, newParam.sn76489[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(EnmChip.RF5C164, chipId, ch, newParam.rf5c164[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(EnmChip.RF5C68, chipId, ch, newParam.rf5c68[chipId].channels[ch].mask);
                for (int ch = 0; ch < 24; ch++)
                    forceChannelMask(EnmChip.C140, chipId, ch, newParam.c140[chipId].channels[ch].mask);
                for (int ch = 0; ch < 32; ch++)
                    forceChannelMask(EnmChip.C352, chipId, ch, newParam.c352[chipId].channels[ch].mask);
                for (int ch = 0; ch < 16; ch++)
                    forceChannelMask(EnmChip.SEGAPCM, chipId, ch, newParam.segaPcm[chipId].channels[ch].mask);
                for (int ch = 0; ch < 19; ch++)
                    forceChannelMask(EnmChip.QSound, chipId, ch, newParam.qSound[chipId].channels[ch].mask);
                for (int ch = 0; ch < 6; ch++)
                    forceChannelMask(EnmChip.HuC6280, chipId, ch, newParam.huc6280[chipId].channels[ch].mask);
                for (int ch = 0; ch < 4; ch++)
                    forceChannelMask(EnmChip.OKIM6295, chipId, ch, newParam.okim6295[chipId].channels[ch].mask);
                for (int ch = 0; ch < 2; ch++) ForceChannelMaskNES(EnmChip.NES, chipId, ch, newParam.nesdmc);
                for (int ch = 2; ch < 5; ch++) ForceChannelMaskNES(EnmChip.DMC, chipId, ch, newParam.nesdmc);
                for (int ch = 0; ch < 3; ch++) resetChannelMask(EnmChip.MMC5, chipId, ch);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(EnmChip.PPZ8, chipId, ch, newParam.ppz8[chipId].channels[ch].mask);
                for (int ch = 0; ch < 4; ch++)
                    forceChannelMask(EnmChip.DMG, chipId, ch, newParam.dmg[chipId].channels[ch].mask);
                for (int ch = 0; ch < 3; ch++)
                    forceChannelMask(EnmChip.VRC6, chipId, ch, newParam.vrc6[chipId].channels[ch].mask);
                for (int ch = 0; ch < 8; ch++)
                    forceChannelMask(EnmChip.N163, chipId, ch, newParam.n106[chipId].channels[ch].mask);
                resetChannelMask(EnmChip.FDS, chipId, 0);
            }

            audio.go();

            if (frmInfo != null) {
                frmInfo.update();
            }

            if (setting.getOther().getAutoOpen()) {

                if (audio.audio.chipLED.get("PriOPM") != 0) OpenFormYM2151(0, true);
                else CloseFormYM2151(0);
                if (audio.audio.chipLED.get("SecOPM") != 0) OpenFormYM2151(1, true);
                else CloseFormYM2151(1);

                if (audio.audio.chipLED.get("PriOPN") != 0) OpenFormYM2203(0, true);
                else CloseFormYM2203(0);
                if (audio.audio.chipLED.get("SecOPN") != 0) OpenFormYM2203(1, true);
                else CloseFormYM2203(1);

                if (audio.audio.chipLED.get("PriOPLL") != 0) OpenFormYM2413(0, true);
                else CloseFormYM2413(0);
                if (audio.audio.chipLED.get("SecOPLL") != 0) OpenFormYM2413(1, true);
                else CloseFormYM2413(1);

                if (audio.audio.chipLED.get("PriOPNA") != 0) OpenFormYM2608(0, true);
                else CloseFormYM2608(0);
                if (audio.audio.chipLED.get("SecOPNA") != 0) OpenFormYM2608(1, true);
                else CloseFormYM2608(1);

                if (audio.audio.chipLED.get("PriOPNB") != 0) OpenFormYM2610(0, true);
                else CloseFormYM2610(0);
                if (audio.audio.chipLED.get("SecOPNB") != 0) OpenFormYM2610(1, true);
                else CloseFormYM2610(1);

                if (audio.audio.chipLED.get("PriOPN2") != 0) openFormYM2612(0, true);
                else closeFormYM2612(0);
                if (audio.audio.chipLED.get("SecOPN2") != 0) openFormYM2612(1, true);
                else closeFormYM2612(1);

                if (audio.audio.chipLED.get("PriDCSG") != 0) OpenFormSN76489(0, true);
                else CloseFormSN76489(0);
                if (audio.audio.chipLED.get("SecDCSG") != 0) {
                    if (!audio.audio.getSn76489NGPFlag()) OpenFormSN76489(1, true);
                } else CloseFormSN76489(1);

                if (audio.audio.chipLED.get("PriPPZ8") != 0) OpenFormPPZ8(0, true);
                else CloseFormPPZ8(0);
                if (audio.audio.chipLED.get("SecPPZ8") != 0) OpenFormPPZ8(1, true);
                else CloseFormPPZ8(1);

                if (audio.audio.chipLED.get("PriFME7") != 0) OpenFormS5B(0, true);
                else CloseFormS5B(0);
                if (audio.audio.chipLED.get("SecFME7") != 0) OpenFormS5B(1, true);
                else CloseFormS5B(1);

                if (audio.audio.chipLED.get("PriDMG") != 0) OpenFormDMG(0, true);
                else CloseFormDMG(0);
                if (audio.audio.chipLED.get("SecDMG") != 0) OpenFormDMG(1, true);
                else CloseFormDMG(1);

                if (audio.audio.chipLED.get("PriRF5C") != 0) OpenFormMegaCD(0, true);
                else CloseFormMegaCD(0);
                if (audio.audio.chipLED.get("SecRF5C") != 0) OpenFormMegaCD(1, true);
                else CloseFormMegaCD(1);

                if (audio.audio.chipLED.get("PriRF5C68") != 0) OpenFormRf5c68(0, true);
                else CloseFormRf5c68(0);
                if (audio.audio.chipLED.get("SecRF5C68") != 0) OpenFormRf5c68(1, true);
                else CloseFormRf5c68(1);

                if (audio.audio.chipLED.get("PriOKI5") != 0) OpenFormOKIM6258(0, true);
                else CloseFormOKIM6258(0);
                if (audio.audio.chipLED.get("SecOKI5") != 0) OpenFormOKIM6258(1, true);
                else CloseFormOKIM6258(1);

                if (audio.audio.chipLED.get("PriOKI9") != 0) OpenFormOKIM6295(0, true);
                else CloseFormOKIM6295(0);
                if (audio.audio.chipLED.get("SecOKI9") != 0) OpenFormOKIM6295(1, true);
                else CloseFormOKIM6295(1);

                if (audio.audio.chipLED.get("PriC140") != 0) OpenFormC140(0, true);
                else CloseFormC140(0);
                if (audio.audio.chipLED.get("SecC140") != 0) OpenFormC140(1, true);
                else CloseFormC140(1);

                if (audio.audio.chipLED.get("PriYMZ") != 0) OpenFormYMZ280B(0, true);
                else CloseFormYMZ280B(0);
                if (audio.audio.chipLED.get("SecYMZ") != 0) OpenFormYMZ280B(1, true);
                else CloseFormYMZ280B(1);

                if (audio.audio.chipLED.get("PriC352") != 0) OpenFormC352(0, true);
                else CloseFormC352(0);
                if (audio.audio.chipLED.get("SecC352") != 0) OpenFormC352(1, true);
                else CloseFormC352(1);

                if (audio.audio.chipLED.get("PriMPCM") != 0) OpenFormMultiPCM(0, true);
                else CloseFormMultiPCM(0);
                if (audio.audio.chipLED.get("SecMPCM") != 0) OpenFormMultiPCM(1, true);
                else CloseFormMultiPCM(1);

                if (audio.audio.chipLED.get("PriQsnd") != 0) OpenFormQSound(0, true);
                else CloseFormQSound(0);
                if (audio.audio.chipLED.get("SecQsnd") != 0) OpenFormQSound(1, true);
                else CloseFormQSound(1);

                if (audio.audio.chipLED.get("PriSPCM") != 0) OpenFormSegaPCM(0, true);
                else CloseFormSegaPCM(0);
                if (audio.audio.chipLED.get("SecSPCM") != 0) OpenFormSegaPCM(1, true);
                else CloseFormSegaPCM(1);

                if (audio.audio.chipLED.get("PriAY10") != 0) OpenFormAY8910(0, true);
                else CloseFormAY8910(0);
                if (audio.audio.chipLED.get("SecAY10") != 0) OpenFormAY8910(1, true);
                else CloseFormAY8910(1);

                if (audio.audio.chipLED.get("PriHuC") != 0) OpenFormHuC6280(0, true);
                else CloseFormHuC6280(0);
                if (audio.audio.chipLED.get("SecHuC") != 0) OpenFormHuC6280(1, true);
                else CloseFormHuC6280(1);

                if (audio.audio.chipLED.get("PriK051649") != 0) OpenFormK051649(0, true);
                else CloseFormK051649(0);
                if (audio.audio.chipLED.get("SecK051649") != 0) OpenFormK051649(1, true);
                else CloseFormK051649(1);

                if (audio.audio.chipLED.get("PriMID") != 0) OpenFormMIDI(0, true);
                else closeFormMIDI(0);
                //if (audio.audio.chipLED.get("SecMID") != 0) OpenFormMIDI(1, true); else CloseFormMIDI(1);

                if (audio.audio.chipLED.get("PriNES") != 0 || audio.audio.chipLED.get("PriDMC") != 0) openFormNESDMC(0, true);
                else closeFormNESDMC(0);
                if (audio.audio.chipLED.get("SecNES") != 0 || audio.audio.chipLED.get("SecDMC") != 0) openFormNESDMC(1, true);
                else closeFormNESDMC(1);

                if (audio.audio.chipLED.get("PriFDS") != 0) openFormFDS(0, true);
                else closeFormFDS(0);
                if (audio.audio.chipLED.get("SecFDS") != 0) openFormFDS(1, true);
                else closeFormFDS(1);

                if (audio.audio.chipLED.get("PriVRC6") != 0) openFormVRC6(0, true);
                else closeFormVRC6(0);
                if (audio.audio.chipLED.get("SecVRC6") != 0) openFormVRC6(1, true);
                else closeFormVRC6(1);

                if (audio.audio.chipLED.get("PriVRC7") != 0) openFormVRC7(0, true);
                else closeFormVRC7(0);
                if (audio.audio.chipLED.get("SecVRC7") != 0) openFormVRC7(1, true);
                else closeFormVRC7(1);

                if (audio.audio.chipLED.get("PriMMC5") != 0) openFormMMC5(0, true);
                else closeFormMMC5(0);
                if (audio.audio.chipLED.get("SecMMC5") != 0) openFormMMC5(1, true);
                else closeFormMMC5(1);

                if (audio.audio.chipLED.get("PriN106") != 0) openFormN106(0, true);
                else closeFormN106(0);
                if (audio.audio.chipLED.get("SecN106") != 0) openFormN106(1, true);
                else closeFormN106(1);

                if (audio.audio.chipLED.get("PriOPL") != 0) OpenFormYM3526(0, true);
                else CloseFormYM3526(0);
                if (audio.audio.chipLED.get("SecOPL") != 0) OpenFormYM3526(1, true);
                else CloseFormYM3526(1);

                if (audio.audio.chipLED.get("PriY8950") != 0) OpenFormY8950(0, true);
                else CloseFormY8950(0);
                if (audio.audio.chipLED.get("SecY8950") != 0) OpenFormY8950(1, true);
                else CloseFormY8950(1);

                if (audio.audio.chipLED.get("PriOPL2") != 0) OpenFormYM3812(0, true);
                else CloseFormYM3812(0);
                if (audio.audio.chipLED.get("SecOPL2") != 0) OpenFormYM3812(1, true);
                else CloseFormYM3812(1);

                if (audio.audio.chipLED.get("PriOPL3") != 0) OpenFormYMF262(0, true);
                else CloseFormYMF262(0);
                if (audio.audio.chipLED.get("SecOPL3") != 0) OpenFormYMF262(1, true);
                else CloseFormYMF262(1);

                if (audio.audio.chipLED.get("PriOPL4") != 0) OpenFormYMF278B(0, true);
                else CloseFormYMF278B(0);
                if (audio.audio.chipLED.get("SecOPL4") != 0) OpenFormYMF278B(1, true);
                else CloseFormYMF278B(1);

                if (audio.audio.chipLED.get("PriOPX") != 0) OpenFormYMF271(0, true);
                else CloseFormYMF271(0);
                if (audio.audio.chipLED.get("SecOPX") != 0) OpenFormYMF271(1, true);
                else CloseFormYMF271(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            audio.audio.errMsg = e.getMessage();
        }
    }

    public void ff() {
        if (audio.audio.isPaused()) {
            audio.audio.pause();
        }

        audio.ff();
    }

    public void next() {
        if (audio.audio.isPaused()) {
            audio.audio.pause();
        }

        Request req = new Request(enmRequest.Stop, null, null);
        OpeManager.requestToAudio(req);
        while (!req.getEnd()) {
                Thread.yield();
        }
        //audio.audio.Stop();

        screenInit(null);

        //frmPlayList.nextPlay();
        frmPlayList.nextPlayMode(newButtonMode[9]);
    }

    private void nextPlayMode() {
        frmPlayList.nextPlayMode(newButtonMode[9]);
    }

    public void slow() {
        if (audio.audio.isPaused()) {
            audio.stepPlay(4000);
            audio.audio.pause();
            return;
        }

        if (audio.isStopped()) {
            play();
        }

        audio.slow();
    }

    private void playMode() {
        newButtonMode[9]++;
        if (newButtonMode[9] > 3) newButtonMode[9] = 0;
        opeButtonMode.setToolTipText(modeTip[newButtonMode[9]]);
    }

    private String[] fileOpen(boolean flg) {
        JFileChooser ofd = new JFileChooser();
        Arrays.stream(Resources.getCntSupportFile().split("\\s")).forEach(l -> {
            String[] p = l.split("\\|");
            ofd.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(java.io.File f) {
                    return f.getName().toLowerCase().endsWith(p[1]);
                }

                @Override
                public String getDescription() {
                    return p[0];
                }
            });
        });
        ofd.setDialogTitle("ファイルを選択してください");
        ofd.setFileFilter(ofd.getChoosableFileFilters()[setting.getOther().getFilterIndex()]);

        if (!setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(setting.getOther().getDefaultDataPath()) && isInitialOpenFolder) {
            ofd.setCurrentDirectory(new java.io.File(setting.getOther().getDefaultDataPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(flg);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

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
        if (frmPlayList.isVisible()) checkAndSetForm(frmPlayList);
        frmPlayList.toFront();
        frmPlayList.toBack();
    }

    private void dispVSTList() {
//        frmVSTeffectList.setVisible(!frmVSTeffectList.isVisible());
//        if (frmVSTeffectList.isVisible()) checkAndSetForm(frmVSTeffectList);
//        frmVSTeffectList.toFront();
//        frmVSTeffectList.toBack();
    }

    private void showContextMenu() {
        cmsOpenOtherPanel.setVisible(true);
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point p = pi.getLocation();
        cmsOpenOtherPanel.setLocation(p.x, p.y);
    }

    public void getInstCh(EnmChip chip, int ch, int chipId) {
        try {
            ym2612MIDI.setVoiceFromChipRegister(chip, chipId, ch);

            if (!setting.getOther().getUseGetInst()) return;

            if (chip == EnmChip.YM2413) {
                if (setting.getOther().getInstFormat() == EnmInstFormat.MML2VGM) {

                } else if (setting.getOther().getInstFormat() == EnmInstFormat.SendMML2VGM) {
                    getInstChForSendMML2VGM(chip, ch, chipId);
                } else {
                    getInstChForMGSC(chip, ch, chipId);
                }
            } else if (chip == EnmChip.YM3812 || chip == EnmChip.YMF262 || chip == EnmChip.YMF278B) {
                if (setting.getOther().getInstFormat() == EnmInstFormat.OPLI) {
                    getInstChForOPLI(chip, ch, chipId);
                } else {
                    getInstChForSendMML2VGM(chip, ch, chipId);
                }
            } else if (chip == EnmChip.VRC7) {
                getInstChForMGSC(chip, ch, chipId);
            } else if (chip == EnmChip.K051649) {
                if (setting.getOther().getInstFormat() == EnmInstFormat.MGSCSCC_PLAIN) {
                    getInstChForMGSCSCCPLAIN(ch, chipId);
                } else {
                    getInstChForMGSC(chip, ch, chipId);
                }
            } else if (chip == EnmChip.N163) {
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
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "音色出力エラー", "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getInstChForFMP7(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("'@ FA xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , fmRegister[p][0x50 + ops + c] & 0x1f  // AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f  // DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f  // SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f  // RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4 // SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f // TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6 // KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f // ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4 // DT
                        , (fmRegister[p][0x60 + ops + c] & 0x80) >> 7 // AM
                ));
            }
            n.append("   ALG FB\n");
            n.append(String.format("'@ %3d,%3d\n"
                    , fmRegister[p][0xb0 + c] & 0x07 // AL
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3 // FB
            ));
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);
            n.append("'@ FC xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AM\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , ym2151Register[0x80 + ops + ch] & 0x1f  // AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f  // DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f  // SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f  // RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4  // SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f  // TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6  // KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f  // ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4  // DT
                        , (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6  // DT2
                        , (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
            n.append("   ALG FB\n");
            n.append(String.format("'@ %3d,%3d\n"
                    , ym2151Register[0x20 + ch] & 0x07  // AL
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3 // FB
            ));
        }

        if (n.length() != 0) Common.setClipboard(n.toString());
    }

    private void getInstChForMDX(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("'@xx = {\n/* AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format("   %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , fmRegister[p][0x50 + ops + c] & 0x1f  // AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f  // DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f  // SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f  // RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4 // SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f // TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6 // KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f//ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4//DT
                        , 0
                        , (fmRegister[p][0x60 + ops + c] & 0x80) >> 7//AM
                ));
            }
            n.append("/* ALG FB  OP\n");
            n.append(String.format("   %3d,%3d,15\n}}\n"
                    , fmRegister[p][0xb0 + c] & 0x07//AL
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3//FB
            ));
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n.append("'@xx = {\n/* AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format("   %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , ym2151Register[0x80 + ops + ch] & 0x1f //AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f //DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f //SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f //RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 //SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f //TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 //KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f //ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 //DT
                        , (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6 //DT2
                        , (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7 //AM
                ));
            }
            n.append("/* ALG FB  OP\n");
            n.append(String.format("   %3d,%3d,15\n}}\n"
                    , ym2151Register[0x20 + ch] & 0x07 //AL
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3//FB
            ));
        }

        if (n.length() != 0) Common.setClipboard(n.toString());
    }

    private void getInstChForMML2VGM(EnmChip chip, int ch, int chipId) {
        String n = getInstChForMML2VGMFormat(chip, ch, chipId);
        if (n != null && n.isEmpty()) Common.setClipboard(n);
    }

    private void getInstChForSendMML2VGM(EnmChip chip, int ch, int chipId) {
        String n = getInstChForMML2VGMFormat(chip, ch, chipId);
        if (n == null || !n.isEmpty()) return;

        MmfControl mmf = new MmfControl(true, "mml2vgmFMVoicePool", 1024 * 4);
        try {
            mmf.sendMessage(String.join(":", "SendVoice", n));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.err.println("メッセージが長すぎ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "mml2vgmの共有メモリが見つかりませんでした");
        }
    }

    private static final int[] slot1Tbl = new int[] {0, 1, 2, 6, 7, 8, 12, 13, 14};
    private static final int[] slot2Tbl = new int[] {3, 4, 5, 9, 10, 11, 15, 16, 17};

    private String getInstChForMML2VGMFormat(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("'@ N xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM  SSG-EG\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , fmRegister[p][0x50 + ops + c] & 0x1f //AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f //DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f //SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f //RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4//SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f//TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6//KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f//ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4//DT
                        , (fmRegister[p][0x60 + ops + c] & 0x80) >> 7//AM
                        , fmRegister[p][0x90 + ops + c] & 0x0f//SG
                ));
            }
            n.append("   ALG FB\n");
            n.append(String.format("'@ %3d,%3d\n"
                    , fmRegister[p][0xb0 + c] & 0x07//AL
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3//FB
            ));
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);
            n.append("'@ M xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , ym2151Register[0x80 + ops + ch] & 0x1f //AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f //DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f //SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f //RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 //SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f //TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 //KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f //ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 //DT1
                        , (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6 //DT2
                        , (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7 //AM
                ));
            }
            n.append("   ALG FB\n");
            n.append(String.format("'@ %3d,%3d\n"
                    , ym2151Register[0x20 + ch] & 0x07 //AL
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3//FB
            ));
        } else if (chip == EnmChip.HuC6280) {
            OotakeHuC6280 huc6280Register = audio.audio.getHuC6280Register(chipId);
            if (huc6280Register == null) return null;
            OotakeHuC6280.Psg psg = huc6280Register.getPsg(ch);
            if (psg == null) return null;
            if (psg.wave == null) return null;
            if (psg.wave.length != 32) return null;

            n.append("'@ H xx,\n   +0 +1 +2 +3 +4 +5 +6 +7\n");

            for (int i = 0; i < 32; i += 8) {
                n.append(String.format("'@ %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d\n"
                        , (17 - psg.wave[i + 0])
                        , (17 - psg.wave[i + 1])
                        , (17 - psg.wave[i + 2])
                        , (17 - psg.wave[i + 3])
                        , (17 - psg.wave[i + 4])
                        , (17 - psg.wave[i + 5])
                        , (17 - psg.wave[i + 6])
                        , (17 - psg.wave[i + 7])
                ));
            }
        } else if (chip == EnmChip.YM2413) {
            //Ym2413
            int[] regs = audio.audio.getYM2413Register(chipId);
        } else if (chip == EnmChip.YM3812) {
            //OPL2
            //'@ L No "Name"
            //'@ AR DR SL RR KSL TL MT AM VIB EGT KSR WS
            //'@ AR DR SL RR KSL TL MT AM VIB EGT KSR WS
            //'@ CNT FB

            int[] regs = audio.audio.getYM3812Register(chipId);
            int slot;
            if (ch < 0 || ch > 8) return null;

            n.append("'@ L No \"MDP\"\n   AR DR SL RR KSL TL MT AM VIB EGT KSR WS\n");
            for (int i = 0; i < 2; i++) {
                if (i == 0) slot = slot1Tbl[ch];
                else slot = slot2Tbl[ch];

                slot = (slot % 6) + 8 * (slot / 6);
                n.append(String.format("'@ %2d,%2d,%2d,%2d, %2d,%2d,%2d,%2d, %2d, %2d, %2d,%2d\n"
                        , regs[0x60 + slot] >> 4
                        , regs[0x60 + slot] & 0xf
                        , regs[0x80 + slot] >> 4
                        , regs[0x80 + slot] & 0xf
                        , regs[0x40 + slot] >> 6
                        , regs[0x40 + slot] & 0x3f
                        , regs[0x20 + slot] & 0xf
                        , regs[0x20 + slot] >> 7
                        , (regs[0x20 + slot] >> 6) & 1
                        , (regs[0x20 + slot] >> 5) & 1
                        , (regs[0x20 + slot] >> 4) & 1
                        , (regs[0xe0 + slot] & 3)
                ));
            }
            n.append(String.format("   CNT FB\n'@  %2d,%2d\n"
                    , (regs[0xc0 + ch] & 1)
                    , (regs[0xc0 + ch] >> 1) & 7
            ));
        }

        return n.toString();
    }

    private void getInstChForMUSICLALF(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append(String.format("  @xx:{{\n  %3d %3d\n"
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3//FB
                    , fmRegister[p][0xb0 + c] & 0x07//AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format("  %3d %3d %3d %3d %3d %3d %3d %3d %3d\n"
                        , fmRegister[p][0x50 + ops + c] & 0x1f //AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f //DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f //SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f //RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4//SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f//TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6//KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f//ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4//DT
                ));
            }
            n.append("  }\n");
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n.append(String.format("  @xx:{{\n  %3d %3d\n"
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3//FB
                    , ym2151Register[0x20 + ch] & 0x07 //AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format("  %3d %3d %3d %3d %3d %3d %3d %3d %3d\n"
                        , ym2151Register[0x80 + ops + ch] & 0x1f //AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f //DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f //SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f //RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 //SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f //TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 //KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f //ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 //DT
                ));
            }
            n.append("  }\n");
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    private void getInstChForMucom88(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append(String.format("  @xx:{{\n  %3d, %3d\n"
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3//FB
                    , fmRegister[p][0xb0 + c] & 0x07//AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format("  %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d" + (i != 3 ? "\n" : "")
                        , fmRegister[p][0x50 + ops + c] & 0x1f //AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f //DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f //SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f //RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4//SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f//TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6//KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f//ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4//DT
                ));
            }
            n.append(",\"MDP\"  }\n");
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n.append(String.format("  @xx:{{\n  %3d, %3d\n"
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3//FB
                    , ym2151Register[0x20 + ch] & 0x07 //AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format("  %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d, %3d" + (i != 3 ? "\n" : "")
                        , ym2151Register[0x80 + ops + ch] & 0x1f //AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f //DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f //SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f //RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 //SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f //TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 //KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f //ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 //DT
                ));
            }
            n.append(",\"MDP\"  }\n");
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    private void getInstChForMUSICLALF2(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("@%xxx\n");

            for (int i = 0; i < 6; i++) {
                n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                        , fmRegister[p][0x30 + 0 + c + i * 0x10] & 0xff
                        , fmRegister[p][0x30 + 8 + c + i * 0x10] & 0xff
                        , fmRegister[p][0x30 + 16 + c + i * 0x10] & 0xff
                        , fmRegister[p][0x30 + 24 + c + i * 0x10] & 0xff
                ));
            }
            n.append(String.format("$%3x\n"
                    , fmRegister[p][0xb0 + c] //FB/AL
            ));
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n.append("@%xxx\n");

            n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                    , (ym2151Register[0x40 + 0 + ch] & 0x7f) //DT/ML
                    , (ym2151Register[0x40 + 8 + ch] & 0x7f) //DT/ML
                    , (ym2151Register[0x40 + 16 + ch] & 0x7f)//DT/ML
                    , (ym2151Register[0x40 + 24 + ch] & 0x7f)//DT/ML
            ));
            n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                    , (ym2151Register[0x60 + 0 + ch] & 0x7f) //TL
                    , (ym2151Register[0x60 + 8 + ch] & 0x7f) //TL
                    , (ym2151Register[0x60 + 16 + ch] & 0x7f)//TL
                    , (ym2151Register[0x60 + 24 + ch] & 0x7f)//TL
            ));
            n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                    , (ym2151Register[0x80 + 0 + ch] & 0xdf) //KS/AR
                    , (ym2151Register[0x80 + 8 + ch] & 0xdf) //KS/AR
                    , (ym2151Register[0x80 + 16 + ch] & 0xdf)//KS/AR
                    , (ym2151Register[0x80 + 24 + ch] & 0xdf)//KS/AR
            ));
            n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                    , (ym2151Register[0xa0 + 0 + ch] & 0x9f) //AM/DR
                    , (ym2151Register[0xa0 + 8 + ch] & 0x9f) //AM/DR
                    , (ym2151Register[0xa0 + 16 + ch] & 0x9f)//AM/DR
                    , (ym2151Register[0xa0 + 24 + ch] & 0x9f)//AM/DR
            ));
            n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                    , (ym2151Register[0xc0 + 0 + ch] & 0x1f) //SR
                    , (ym2151Register[0xc0 + 8 + ch] & 0x1f) //SR
                    , (ym2151Register[0xc0 + 16 + ch] & 0x1f)//SR
                    , (ym2151Register[0xc0 + 24 + ch] & 0x1f)//SR
            ));
            n.append(String.format("$%3x,$%3x,$%3x,$%3x\n"
                    , (ym2151Register[0xe0 + 0 + ch] & 0xff) //SL/RR
                    , (ym2151Register[0xe0 + 8 + ch] & 0xff) //SL/RR
                    , (ym2151Register[0xe0 + 16 + ch] & 0xff)//SL/RR
                    , (ym2151Register[0xe0 + 24 + ch] & 0xff)//SL/RR
            ));

            n.append(String.format("$%3x\n"
                    , ym2151Register[0x20 + ch] //FB/AL
            ));
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    private void getInstChForNRTDRV(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("@ xxxx {\n");
            n.append(String.format("000,%3d,%3d,015\n"
                    , fmRegister[p][0xb0 + c] & 0x07//AL
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3//FB
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format(" %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , fmRegister[p][0x50 + ops + c] & 0x1f //AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f //DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f //SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f //RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4//SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f//TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6//KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f//ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4//DT
                        , 0
                        , (fmRegister[p][0x60 + ops + c] & 0x80) >> 7//AM
                ));
            }
            n.append("}\n");
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n.append("@ xxxx {\n");
            n.append(String.format("000,%3d,%3d,015\n"
                    , ym2151Register[0x20 + ch] & 0x07 //AL
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3//FB
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format(" %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n"
                        , ym2151Register[0x80 + ops + ch] & 0x1f //AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f //DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f //SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f //RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 //SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f //TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 //KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f //ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 //DT
                        , (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6 //DT2
                        , (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7 //AM
                ));
            }
            n.append("}\n");
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    private void getInstChForHuSIC(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.HuC6280) {
            OotakeHuC6280 huc6280Register = audio.audio.getHuC6280Register(chipId);
            if (huc6280Register == null) return;
            OotakeHuC6280.Psg psg = huc6280Register.getPsg(ch);
            if (psg == null) return;
            if (psg.wave == null) return;
            if (psg.wave.length != 32) return;

            n.append("@WTx={\n");

            for (int i = 0; i < 32; i += 8) {
                n.append(String.format("$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,\n"
                        , (17 - psg.wave[i + 0])
                        , (17 - psg.wave[i + 1])
                        , (17 - psg.wave[i + 2])
                        , (17 - psg.wave[i + 3])
                        , (17 - psg.wave[i + 4])
                        , (17 - psg.wave[i + 5])
                        , (17 - psg.wave[i + 6])
                        , (17 - psg.wave[i + 7])
                ));
            }

            n = new StringBuilder(n.substring(0, n.length() - 3) + "\n}\n");
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    private void getInstChForMGSC(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();
        int[] register = null;

        if (chip == EnmChip.YM2413) {
            register = audio.audio.getYM2413Register(chipId);
        } else if (chip == EnmChip.VRC7) {
            byte[] r = audio.audio.getVRC7Register(chipId);
            if (r == null) return;
            register = new int[r.length];
            for (int i = 0; i < r.length; i++) {
                register[i] = r[i];
            }
        } else if (chip == EnmChip.K051649) {
            getInstChForMGSCSCC(ch, chipId);
            return;
        }

        if (register == null) return;
        n.append("@vXX = { \n");
        n.append("   ;       TL FB\n");
        n.append(String.format("           %2d,%2d,\n"
                , register[0x02] & 0x3f
                , register[0x03] & 0x7
        ));
        n.append("   ;       AR DR SL RR KL MT AM VB EG KR DT\n");

        n.append(String.format("           %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,\n"
                , (register[0x04] & 0xf0) >> 4
                , (register[0x04] & 0x0f)
                , (register[0x06] & 0xf0) >> 4
                , (register[0x06] & 0x0f)
                , (register[0x02] & 0xc0) >> 6
                , (register[0x00] & 0x0f)
                , (register[0x00] & 0x80) >> 7
                , (register[0x00] & 0x40) >> 6
                , (register[0x00] & 0x20) >> 5
                , (register[0x00] & 0x10) >> 4
                , (register[0x03] & 0x08) >> 3
        ));

        n.append(String.format("           %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d }}\n"
                , (register[0x05] & 0xf0) >> 4
                , (register[0x05] & 0x0f)
                , (register[0x07] & 0xf0) >> 4
                , (register[0x07] & 0x0f)
                , (register[0x03] & 0xc0) >> 6
                , (register[0x01] & 0x0f)
                , (register[0x01] & 0x80) >> 7
                , (register[0x01] & 0x40) >> 6
                , (register[0x01] & 0x20) >> 5
                , (register[0x01] & 0x10) >> 4
                , (register[0x03] & 0x10) >> 4
        ));

        Common.setClipboard(n.toString());
    }

    private void getInstChForMGSCSCC(int ch, int chipId) {
        K051649 chip = audio.audio.getK051649Register(chipId);
        if (chip == null) return;
        int[] register = new int[32];
        for (int i = 0; i < 32; i++) register[i] = chip.getWaveRam(ch, i);

        StringBuilder n = new StringBuilder("@sXX = {");
        for (int i = 0; i < 8; i++) {
            n.append(String.format(" %02x%02x%02x%02x",
                    (byte) register[i * 4 + 0], (byte) register[i * 4 + 1],
                    (byte) register[i * 4 + 2], (byte) register[i * 4 + 3]
            ));
        }
        n.append(" }\n");

        Common.setClipboard(n.toString());
    }

    private void getInstChForMGSCSCCPLAIN(int ch, int chipId) {
        K051649 chip = audio.audio.getK051649Register(chipId);
        if (chip == null) return;
        int[] register = new int[32];
        for (int i = 0; i < 32; i++) register[i] = chip.getWaveRam(ch, i);

        StringBuilder n = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            n.append(String.format("%2x%2x%2x%2x",
                    (byte) register[i * 4 + 0], (byte) register[i * 4 + 1],
                    (byte) register[i * 4 + 2], (byte) register[i * 4 + 3]
            ));
        }
        n.append("\n");

        Common.setClipboard(n.toString());
    }

    private void getInstChForMCK(EnmChip chip, int ch, int chipId) {
        if (chip == EnmChip.N163) {
            NesN106.TrackInfo[] info = (NesN106.TrackInfo[]) audio.audio.getN106Register(0);
            if (info == null) return;

            StringBuilder n = new StringBuilder("@Nxx = { ");
            n.append(String.format("%d ", info[ch].wavelen));
            for (int i = 0; i < info[ch].wavelen; i++) {
                n.append(String.format("%d ", (byte) info[ch].wave[i]));
            }
            n.append("}\n");

            Common.setClipboard(n.toString());
        }
    }

    private void getInstChForTFI(EnmChip chip, int ch, int chipId) {

        byte[] n = new byte[42];

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

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
                n[i * 10 + 6] = (byte) (fmRegister[p][0x50 + ops + c] & 0x1f); //AR
                n[i * 10 + 7] = (byte) (fmRegister[p][0x60 + ops + c] & 0x1f); //DR
                n[i * 10 + 8] = (byte) (fmRegister[p][0x70 + ops + c] & 0x1f); //SR
                n[i * 10 + 9] = (byte) (fmRegister[p][0x80 + ops + c] & 0x0f); //RR
                n[i * 10 + 10] = (byte) ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4); // SL
                n[i * 10 + 11] = (byte) (fmRegister[p][0x90 + ops + c] & 0x0f); // SSG
            }

        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

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
                n[i * 10 + 6] = (byte) (ym2151Register[0x80 + ops + ch] & 0x1f); //AR
                n[i * 10 + 7] = (byte) (ym2151Register[0xa0 + ops + ch] & 0x1f); //DR
                n[i * 10 + 8] = (byte) (ym2151Register[0xc0 + ops + ch] & 0x1f); //SR
                n[i * 10 + 9] = (byte) (ym2151Register[0xe0 + ops + ch] & 0x0f); //RR
                n[i * 10 + 10] = (byte) ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4); // SL
                n[i * 10 + 11] = 0;
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new java.io.File("音色ファイル.tfi"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.getName().toLowerCase().endsWith(".tfi");
            }

            @Override
            public String getDescription() {
                return "TFIファイル(*.tfi)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("名前を付けて保存");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileStream fs = new FileStream(
                sfd.getSelectedFile().getName(),
                FileMode.Create,
                FileAccess.Write)) {

            fs.write(n, 0, n.length);
        }
    }

    private void getInstChForDMP(EnmChip chip, int ch, int chipId) {

        byte[] n = new byte[51];
        n[0] = 0x0b; // FILE_VERSION
        n[2] = 0x01; // Instrument Mode(1=FM)

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

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

        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

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
                n[i * 11 + 9] = (byte) (ym2151Register[0x80 + ops + ch] & 0x1f); //AR
                n[i * 11 + 10] = (byte) (ym2151Register[0xa0 + ops + ch] & 0x1f); //DR
                n[i * 11 + 11] = (byte) ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4); // SL
                n[i * 11 + 12] = (byte) (ym2151Register[0xe0 + ops + ch] & 0x0f); //RR
                n[i * 11 + 13] = (byte) ((ym2151Register[0xa0 + ops + ch] & 0x80) >> 7); //AM
                n[i * 11 + 14] = (byte) ((ym2151Register[0x80 + ops + ch] & 0xc0) >> 6); // KS
                int dt = ((ym2151Register[0x40 + ops + ch] & 0x70) >> 4); // DT
                dt = (dt == 4) ? 0 : dt;
                // 0>5(-3)  1>6(-2)  2>7(-1)  3>0/4  4>1  5>2  6>3  7>3
                dt = (dt > 4) ? (dt - 5) : (dt + 3);
                int dt2 = (byte) ((ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6); //DT2
                n[i * 11 + 15] = (byte) ((dt & 0x7) | (dt2 << 4));
                n[i * 11 + 16] = (byte) (ym2151Register[0xc0 + ops + ch] & 0x1f); //SR
                n[i * 11 + 17] = 0;
            }

        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new java.io.File("音色ファイル.dmp"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.getName().toLowerCase().endsWith(".dmp");
            }

            @Override
            public String getDescription() {
                return "DMPファイル(*.dmp)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("名前を付けて保存");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileStream fs = new FileStream(
            sfd.getSelectedFile().getName(),
            FileMode.Create,
            FileAccess.Write)) {

            fs.write(n, 0, n.length);
        }
    }

    //
    //  以下のコードを使用、参考にさせていただいております。ありがとうございます！
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
    private void getInstChForRYM2612(EnmChip chip, int ch, int chipId) {

        List<String>[] op = new List[] {new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        int[] muls = new int[] {0, 1054, 1581, 2635, 3689, 4743, 5797, 6851, 7905, 8959, 10013, 10540, 11594, 12648, 14229, 15000};
        StringBuilder buf = new StringBuilder("<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n");
        buf.append("\n");
        int alg = 0, fb = 0, ams = 0, pms = 0;

        Vgm.Gd3 gd3 = audio.audio.getGd3();
        String patch_Name = "MDPlayer_%d";
        if (gd3 != null) {
            String pn = gd3.trackName;
            if (pn == null || !pn.isEmpty()) pn = gd3.trackNameJ;
            if (pn != null && pn.isEmpty()) {
                patch_Name = pn + "_%d";
            }
        }
        patch_Name = String.format(patch_Name, Instant.now().toEpochMilli());
        buf.append("<RYM2612Params patchName = \"{patch_Name}\" category = \"Piano\" rating = \"3\" type = \"User\" >\n");

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612)
                    ? audio.audio.getFMRegister(chipId)
                    : (chip == EnmChip.YM2608
                    ? audio.audio.getYM2608Register(chipId)
                    : (chip == EnmChip.YM2203
                    ? new int[][] {audio.audio.getYm2203Register(chipId), null}
                    : audio.audio.getYM2610Register(chipId)
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
                op[i].add(String.format("  <PARAM id=\"OP%dVel\" value=\"%d.0\"/>", i + 1, vel));
                op[i].add(String.format("  <PARAM id=\"OP%dTL\" value=\"%d.0\"/>", i + 1, tl));
                op[i].add(String.format("  <PARAM id=\"OP%dSSGEG\" value=\"%d.0\"/>", i + 1, ssg));
                op[i].add(String.format("  <PARAM id=\"OP%dRS\" value=\"%d.0\"/>", i + 1, (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6));
                op[i].add(String.format("  <PARAM id=\"OP%dRR\" value=\"%d.0\"/>", i + 1, (fmRegister[p][0x80 + ops + c] & 0x0f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dMW\" value=\"0.0\"/>", i + 1));
                op[i].add(String.format("  <PARAM id=\"OP%dMUL\" value=\"%d.0\"/>", i + 1, muls[(fmRegister[p][0x30 + ops + c] & 0x0f) >> 0]));
                op[i].add(String.format("  <PARAM id=\"OP%dFixed\" value=\"0.0\"/>", i + 1));
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4;
                dt = (dt >= 4) ? (4 - dt) : dt;
                op[i].add(String.format("  <PARAM id=\"OP%dDT\" value=\"%d.0\"/>", i + 1, dt));
                op[i].add(String.format("  <PARAM id=\"OP%dD2R\" value=\"%d.0\"/>", i + 1, (fmRegister[p][0x70 + ops + c] & 0x1f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dD2L\" value=\"%d.0\"/>", i + 1, 15 - ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4)));
                op[i].add(String.format("  <PARAM id=\"OP%dD1R\" value=\"%d.0\"/>", i + 1, (fmRegister[p][0x60 + ops + c] & 0x1f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dAR\" value=\"%d.0\"/>", i + 1, (fmRegister[p][0x50 + ops + c] & 0x1f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dAM\" value=\"%d.0\"/>", i + 1, (fmRegister[p][0x60 + ops + c] & 0x80) >> 7));
            }

        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            alg = (ym2151Register[0x20 + ch] & 0x07) >> 0;
            fb = (ym2151Register[0x20 + ch] & 0x38) >> 3;
            ams = (ym2151Register[0x38 + ch] & 0x03) >> 0;
            pms = (ym2151Register[0x38 + ch] & 0x70) >> 4;

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                int tl = 127 - ((ym2151Register[0x60 + ops + ch] & 0x7f) >> 0);
                int vel = 0;
                op[i].add(String.format("  <PARAM id=\"OP%dVel\" value=\"%d.0\"/>", i + 1, vel));
                op[i].add(String.format("  <PARAM id=\"OP%dTL\" value=\"%d.0\"/>", i + 1, tl));
                op[i].add(String.format("  <PARAM id=\"OP%dSSGEG\" value=\"0.0\"/>", i + 1));
                op[i].add(String.format("  <PARAM id=\"OP%dRS\" value=\"%d.0\"/>", i + 1, (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6));
                op[i].add(String.format("  <PARAM id=\"OP%dRR\" value=\"%d.0\"/>", i + 1, (ym2151Register[0xe0 + ops + ch] & 0x0f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dMW\" value=\"0.0\"/>", i + 1));
                op[i].add(String.format("  <PARAM id=\"OP%dMUL\" value=\"%d.0\"/>", i + 1, muls[(ym2151Register[0x40 + ops + ch] & 0x0f) >> 0]));
                op[i].add(String.format("  <PARAM id=\"OP%dFixed\" value=\"0.0\"/>", i + 1));
                int dt = (ym2151Register[0x40 + ops + ch] & 0x70) >> 4;
                dt = (dt >= 4) ? (4 - dt) : dt;
                op[i].add(String.format("  <PARAM id=\"OP%dDT\" value=\"%d.0\"/>", i + 1, dt));
                op[i].add(String.format("  <PARAM id=\"OP%dD2R\" value=\"%d.0\"/>", i + 1, (ym2151Register[0xc0 + ops + ch] & 0x1f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dD2L\" value=\"%d.0\"/>", i + 1, 15 - ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4)));
                op[i].add(String.format("  <PARAM id=\"OP%dD1R\" value=\"%d.0\"/>", i + 1, (ym2151Register[0xa0 + ops + ch] & 0x1f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dAR\" value=\"%d.0\"/>", i + 1, (ym2151Register[0x80 + ops + ch] & 0x1f) >> 0));
                op[i].add(String.format("  <PARAM id=\"OP%dAM\" value=\"%d.0\"/>", i + 1, (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7));
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
        buf.append(String.format("  <PARAM id=\"LFO_Enable\" value=\"%d.0\"/>\n", (pms != 0 || ams != 0) ? 1 : 0));
        buf.append(String.format("  <PARAM id=\"Feedback\" value=\"%d.0\"/>\n", fb));
        buf.append("  <PARAM id=\"FMSMW\" value=\"0.0\"/>\n");
        buf.append(String.format("  <PARAM id=\"FMS\" value=\"%d.0\"/>\n", pms));
        buf.append("  <PARAM id=\"DAC_Prescaler\" value=\"0.0\"/>\n");
        buf.append(String.format("  <PARAM id=\"Algorithm\" value=\"%d.0\"/>\n", alg + 1));
        buf.append(String.format("  <PARAM id=\"AMS\" value=\"%d.0\"/>\n", ams));
        buf.append("  <PARAM id=\"masterTune\"/>\n");
        buf.append("</RYM2612Params>\n");


        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new java.io.File("{patch_Name}.rym2612"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.getName().toLowerCase().endsWith(".rym2612");
            }

            @Override
            public String getDescription() {
                return "RYM2612ファイル(*.rym2612";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("名前を付けて保存");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileStream fs = new FileStream(
                sfd.getSelectedFile().getName(),
                FileMode.Create,
                FileAccess.Write)) {
            try (StreamWriter sw = new StreamWriter(fs)) {
                sw.write(buf.toString());
            } catch (java.io.IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void getInstChForOPNI(EnmChip chip, int ch, int chipId) {

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

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

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

        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n[12 + 32 + 3] = (byte) ym2151Register[0x20 + ch]; // FB & ALG
            n[12 + 32 + 4] = 0x10; // 0x00:OPN2  0x10:OPNA

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;
                n[i * 7 + 12 + 32 + 5] = (byte) ym2151Register[0x40 + ops + ch]; // DT & ML
                n[i * 7 + 12 + 32 + 6] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 7 + 12 + 32 + 7] = (byte) ym2151Register[0x80 + ops + ch]; //KS & AR
                n[i * 7 + 12 + 32 + 8] = (byte) ym2151Register[0xa0 + ops + ch]; //AME DR
                n[i * 7 + 12 + 32 + 9] = (byte) ym2151Register[0xc0 + ops + ch]; //SR
                n[i * 7 + 12 + 32 + 10] = (byte) ym2151Register[0xe0 + ops + ch]; // SL&RR
                n[i * 7 + 12 + 32 + 11] = 0; // SSG

                //int dt2 = (byte)((ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6); //DT2
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new java.io.File("音色ファイル.opni"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.getName().toLowerCase().endsWith(".opni");
            }

            @Override
            public String getDescription() {
                return "OPNIファイル(*.opni)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("名前を付けて保存");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileStream fs = new FileStream(
                sfd.getSelectedFile().getName(),
                FileMode.Create,
                FileAccess.Write)) {

            fs.write(n, 0, n.length);
        }
    }

    private void getInstChForOPLI(EnmChip chip, int ch, int chipId) {
        if (chip != EnmChip.YM3812 && chip != EnmChip.YMF262 && chip != EnmChip.YMF278B) return;

        int[][] reg;
        if (chip == EnmChip.YMF262) reg = audio.audio.getYMF262Register(chipId);
        else if (chip == EnmChip.YMF278B) reg = audio.audio.getYMF278BRegister(chipId);
        else {
            int[] r = audio.audio.getYM3812Register(chipId);
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

        int[] op = new int[] {0, 0, 0, 0};
        boolean isOP4 = false;
        int c = ch;
        if (ch < 6) {
            c -= ch % 2;
            op[0] = c / 2;
            op[1] = op[0] + 3;
            op[2] = op[0] + 6;
            op[3] = op[0] + 9;
            isOP4 = (chip == EnmChip.YM3812) ? false : ((reg[1][0x04] & (0x1 << c)) != 0);
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
        int[] chTbl = new int[] {0, 3, 1, 4, 2, 5, 6, 7, 8};
        //adr = chTbl[adr];

        for (int i = 0; i < 4; i++) {
            op[i] -= (op[i] / 18) * 18;
            op[i] = (op[i] % 6) + 8 * (op[i] / 6);
        }

        //OPLIはop1<->op2  op3<->op4がそれぞれ逆(?)
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

        sfd.setSelectedFile(new java.io.File("音色ファイル.opli"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.getName().toLowerCase().endsWith(".opli");
            }

            @Override
            public String getDescription() {
                return "OPLIファイル(*.opli)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("名前を付けて保存");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileStream fs = new FileStream(
                sfd.getSelectedFile().getName(),
                FileMode.Create,
                FileAccess.Write)) {

            fs.write(n, 0, n.length);
        }
    }

    private void getInstChForVOPM(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("@: n MDPlayer\n");
            n.append("LFO:  0   0   0   0   0\n");
            n.append(String.format("CH: 64  %2d  %2d   0   0 120   0\n"
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3 // FB
                    , fmRegister[p][0xb0 + c] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format("%s:%3d %3d %3d %3d %3d "
                        , "M1C1M2C2".substring(i * 2, 2)
                        , fmRegister[p][0x50 + ops + c] & 0x1f // AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f // DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f // SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f // RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4 // SL
                ));
                n.append(String.format("%3d %3d %3d %3d   0 %3d\n"
                        , fmRegister[p][0x40 + ops + c] & 0x7f // TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6 // KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f // ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4 //DT
                        , (fmRegister[p][0x60 + ops + c] & 0x80) >> 7 //AM
                ));
            }
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);

            n.append("@: n MDPlayer\n");
            n.append("LFO:  0   0   0   0   0\n");
            n.append(String.format("CH: 64  %2d  %2d   0   0 120   0\n"
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3 // FB
                    , ym2151Register[0x20 + ch] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format("%s:%3d %3d %3d %3d %3d "
                        , "M1C1M2C2".substring(i * 2, 2)
                        , ym2151Register[0x80 + ops + ch] & 0x1f // AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f // DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f // SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f // RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 // SL
                ));
                n.append(String.format("%3d %3d %3d %3d %3d %3d\n"
                        , ym2151Register[0x60 + ops + ch] & 0x7f // TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 // KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f // ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 // DT
                        , (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6 // DT2
                        , (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7 // AM
                ));
            }
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    private void getInstChForPMD(EnmChip chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2203 || chip == EnmChip.YM2610) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == EnmChip.YM2612) ? audio.audio.getFMRegister(chipId) : (chip == EnmChip.YM2608 ? audio.audio.getYM2608Register(chipId) : (chip == EnmChip.YM2203 ? new int[][] {audio.audio.getYm2203Register(chipId), null} : audio.audio.getYM2610Register(chipId)));

            n.append("; nm alg fbl\n");
            n.append(String.format("@xxx %3d %3d                            =      MDPlayer\n"
                    , fmRegister[p][0xb0 + c] & 0x07 // AL
                    , (fmRegister[p][0xb0 + c] & 0x38) >> 3 // FB
            ));
            n.append("; ar  dr  sr  rr  sl  tl  ks  ml  dt ams   seg\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append(String.format(" %3d %3d %3d %3d %3d %3d %3d %3d %3d %3d ; %3d\n"
                        , fmRegister[p][0x50 + ops + c] & 0x1f // AR
                        , fmRegister[p][0x60 + ops + c] & 0x1f // DR
                        , fmRegister[p][0x70 + ops + c] & 0x1f // SR
                        , fmRegister[p][0x80 + ops + c] & 0x0f // RR
                        , (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4 // SL
                        , fmRegister[p][0x40 + ops + c] & 0x7f // TL
                        , (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6 // KS
                        , fmRegister[p][0x30 + ops + c] & 0x0f // ML
                        , (fmRegister[p][0x30 + ops + c] & 0x70) >> 4 // DT
                        , (fmRegister[p][0x60 + ops + c] & 0x80) >> 7 // AM
                        , fmRegister[p][0x90 + ops + c] & 0x0f // SG
                ));
            }
        } else if (chip == EnmChip.YM2151) {
            int[] ym2151Register = audio.audio.getYM2151Register(chipId);
            n.append("; nm alg fbl\n");
            n.append(String.format("@xxx %3d %3d                            =      MDPlayer\n"
                    , ym2151Register[0x20 + ch] & 0x07 // AL
                    , (ym2151Register[0x20 + ch] & 0x38) >> 3 // FB
            ));
            n.append("; ar  dr  sr  rr  sl  tl  ks  ml  dt ams   seg\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append(String.format(" %3d %3d %3d %3d %3d %3d %3d %3d %3d %3d ; %3d\n"
                        , ym2151Register[0x80 + ops + ch] & 0x1f // AR
                        , ym2151Register[0xa0 + ops + ch] & 0x1f // DR
                        , ym2151Register[0xc0 + ops + ch] & 0x1f // SR
                        , ym2151Register[0xe0 + ops + ch] & 0x0f // RR
                        , (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 // SL
                        , ym2151Register[0x60 + ops + ch] & 0x7f // TL
                        , (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6 // KS
                        , ym2151Register[0x40 + ops + ch] & 0x0f // ML
                        , (ym2151Register[0x40 + ops + ch] & 0x70) >> 4 // DT
                        //, (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6 // DT2
                        , (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7 // AM
                        , 0
                ));
            }
        }

        if (n.length() == 0) Common.setClipboard(n.toString());
    }

    public boolean loadAndPlay(int m, int songNo, String fn, String zfn/* = null*/) {
        try {
            if (audio.flgReinit) flgReinit = true;
            if (setting.getOther().getInitAlways()) flgReinit = true;
            reinit(setting);

            if (audio.audio.isPaused()) {
                audio.audio.pause();
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
            var r = format.load(zfn, fn);
            srcBuf = r.getItem1();
            extFile = r.getItem2();

            // 再生前に音量のバランスを設定する
            loadPresetMixerBalance(playingFileName, playingArcFileName, format);

            audio = (BasePlugin) format.getPlugin();
            audio.setVGMBuffer(format, srcBuf, playingFileName, playingArcFileName, m, songNo, extFile);
            newParam.ym2612[0].fileFormat = format;
            newParam.ym2612[1].fileFormat = format;

            if (srcBuf != null) {
                SwingUtilities.invokeLater(this::playData);
                if (!audio.audio.errMsg.isEmpty()) return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            srcBuf = null;
            JOptionPane.showMessageDialog(this,
                    String.format("ファイルの読み込みに失敗しました。\nメッセージ=%s", ex.getMessage()),
                    "MDPlayer", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean bufferPlay(byte[] buf, String fullPath) {
        try {
            if (audio.flgReinit) flgReinit = true;
            if (setting.getOther().getInitAlways()) flgReinit = true;
            reinit(setting);

            if (audio.audio.isPaused()) {
                audio.audio.pause();
            }

            String playingFileName = fullPath;
            String playingArcFileName = "";
            List<Tuple<String, byte[]>> extFile = null;
            FileFormat format = FileFormat.getFileFormat(fullPath);
            srcBuf = buf;

            //再生前に音量のバランスを設定する
            loadPresetMixerBalance(playingFileName, playingArcFileName, format);

            audio.setVGMBuffer(format, srcBuf, playingFileName, playingArcFileName, 0, 0, extFile);
            newParam.ym2612[0].fileFormat = format;
            newParam.ym2612[1].fileFormat = format;

            if (srcBuf != null) {
                SwingUtilities.invokeLater(this::playData);
                if (!audio.audio.errMsg.isEmpty()) return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            srcBuf = null;
            JOptionPane.showMessageDialog(this,
                    String.format("ファイルの読み込みに失敗しました。\nメッセージ=%s", ex.getMessage()),
                    "MDPlayer", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        frmPlayList.play();
        return true;
    }

    public void setChannelMask(EnmChip chip, int chipId, int ch) {
        switch (chip) {
        case YM2203:
            if (ch >= 0 && ch < 9) {
                audio.audio.setYM2203Mask(chipId, ch);
                newParam.ym2203[chipId].channels[ch].mask = true;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    newParam.ym2203[chipId].channels[2].mask = true;
                    newParam.ym2203[chipId].channels[6].mask = true;
                    newParam.ym2203[chipId].channels[7].mask = true;
                    newParam.ym2203[chipId].channels[8].mask = true;
                }
            }
            break;
        case YM2413:
            if (ch >= 0 && ch < 14) {
                if (!newParam.ym2413[chipId].channels[ch].mask || newParam.ym2413[chipId].channels[ch].mask == null)
                    audio.audio.setYM2413Mask(chipId, ch);
                else
                    audio.audio.resetYM2413Mask(chipId, ch);

                newParam.ym2413[chipId].channels[ch].mask = !newParam.ym2413[chipId].channels[ch].mask;
            }
            break;
        case YM3526:
            if (ch >= 0 && ch < 14) {
                if (!newParam.ym3526[chipId].channels[ch].mask || newParam.ym3526[chipId].channels[ch].mask == null)
                    audio.audio.setYM3526Mask(chipId, ch);
                else
                    audio.audio.resetYM3526Mask(chipId, ch);

                newParam.ym3526[chipId].channels[ch].mask = !newParam.ym3526[chipId].channels[ch].mask;

            }
            break;
        case Y8950:
            if (ch >= 0 && ch < 15) {
                if (!newParam.y8950[chipId].channels[ch].mask || newParam.y8950[chipId].channels[ch].mask == null)
                    audio.audio.setY8950Mask(chipId, ch);
                else
                    audio.audio.resetY8950Mask(chipId, ch);

                newParam.y8950[chipId].channels[ch].mask = !newParam.y8950[chipId].channels[ch].mask;

            }
            break;
        case YM3812:
            if (ch >= 0 && ch < 14) {
                if (!newParam.ym3812[chipId].channels[ch].mask || newParam.ym3812[chipId].channels[ch].mask == null)
                    audio.audio.setYM3812Mask(chipId, ch);
                else
                    audio.audio.resetYM3812Mask(chipId, ch);

                newParam.ym3812[chipId].channels[ch].mask = !newParam.ym3812[chipId].channels[ch].mask;

            }
            break;
        case YMF262:
            if (ch >= 0 && ch < 23) {
                if (!newParam.ymf262[chipId].channels[ch].mask || newParam.ymf262[chipId].channels[ch].mask == null)
                    audio.audio.setYMF262Mask(chipId, ch);
                else
                    audio.audio.resetYMF262Mask(chipId, ch);

                newParam.ymf262[chipId].channels[ch].mask = !newParam.ymf262[chipId].channels[ch].mask;

            }
            break;
        case YMF278B:
            if (ch >= 0 && ch < 47) {
                if (!newParam.ymf278b[chipId].channels[ch].mask || newParam.ymf278b[chipId].channels[ch].mask == null)
                    audio.audio.setYMF278BMask(chipId, ch);
                else
                    audio.audio.resetYMF278BMask(chipId, ch);

                newParam.ymf278b[chipId].channels[ch].mask = !newParam.ymf278b[chipId].channels[ch].mask;

            }
            break;
        case YM2608:
            if (ch >= 0 && ch < 14) {
                audio.audio.setYM2608Mask(chipId, ch);
                newParam.ym2608[chipId].channels[ch].mask = true;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    newParam.ym2608[chipId].channels[2].mask = true;
                    newParam.ym2608[chipId].channels[9].mask = true;
                    newParam.ym2608[chipId].channels[10].mask = true;
                    newParam.ym2608[chipId].channels[11].mask = true;
                }
            }
            break;
        case YM2610:
            if (ch >= 0 && ch < 14) {
                int c = ch;
                if (ch == 12) c = 13;
                if (ch == 13) c = 12;

                audio.audio.setYM2610Mask(chipId, ch);
                newParam.ym2610[chipId].channels[c].mask = true;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    newParam.ym2610[chipId].channels[2].mask = true;
                    newParam.ym2610[chipId].channels[9].mask = true;
                    newParam.ym2610[chipId].channels[10].mask = true;
                    newParam.ym2610[chipId].channels[11].mask = true;
                }
            }
            break;
        case YM2612:
            if (ch >= 0 && ch < 9) {
                audio.audio.setYM2612Mask(chipId, ch);
                newParam.ym2612[chipId].channels[ch].mask = true;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    newParam.ym2612[chipId].channels[2].mask = true;
                    newParam.ym2612[chipId].channels[6].mask = true;
                    newParam.ym2612[chipId].channels[7].mask = true;
                    newParam.ym2612[chipId].channels[8].mask = true;
                }
            }
            break;
        case SN76489:
            if (!newParam.sn76489[chipId].channels[ch].mask || newParam.sn76489[chipId].channels[ch].mask == null) {
                audio.audio.setSN76489Mask(chipId, ch);
            } else {
                audio.audio.resetSN76489Mask(chipId, ch);
            }
            newParam.sn76489[chipId].channels[ch].mask = !newParam.sn76489[chipId].channels[ch].mask;
            break;
        case RF5C164:
            if (!newParam.rf5c164[chipId].channels[ch].mask || newParam.rf5c164[chipId].channels[ch].mask == null) {
                audio.audio.setRF5C164Mask(chipId, ch);
            } else {
                audio.audio.resetRF5C164Mask(chipId, ch);
            }
            newParam.rf5c164[chipId].channels[ch].mask = !newParam.rf5c164[chipId].channels[ch].mask;
            break;
        case RF5C68:
            if (!newParam.rf5c68[chipId].channels[ch].mask || newParam.rf5c68[chipId].channels[ch].mask == null) {
                audio.audio.setRF5C68Mask(chipId, ch);
            } else {
                audio.audio.resetRF5C68Mask(chipId, ch);
            }
            newParam.rf5c68[chipId].channels[ch].mask = !newParam.rf5c68[chipId].channels[ch].mask;
            break;
        case YM2151:
            if (!newParam.ym2151[chipId].channels[ch].mask || newParam.ym2151[chipId].channels[ch].mask == null) {
                audio.audio.setYM2151Mask(chipId, ch);
            } else {
                audio.audio.resetYM2151Mask(chipId, ch);
            }
            newParam.ym2151[chipId].channels[ch].mask = !newParam.ym2151[chipId].channels[ch].mask;
            break;
        case C140:
            if (!newParam.c140[chipId].channels[ch].mask || newParam.c140[chipId].channels[ch].mask == null) {
                audio.audio.setC140Mask(chipId, ch);
            } else {
                audio.audio.resetC140Mask(chipId, ch);
            }
            newParam.c140[chipId].channels[ch].mask = !newParam.c140[chipId].channels[ch].mask;
            break;
        case PPZ8:
            if (!newParam.ppz8[chipId].channels[ch].mask || newParam.ppz8[chipId].channels[ch].mask == null) {
                audio.audio.setPPZ8Mask(chipId, ch);
            } else {
                audio.audio.resetPPZ8Mask(chipId, ch);
            }
            newParam.ppz8[chipId].channels[ch].mask = !newParam.ppz8[chipId].channels[ch].mask;
            break;
        case C352:
            if (!newParam.c352[chipId].channels[ch].mask || newParam.c352[chipId].channels[ch].mask == null) {
                audio.audio.setC352Mask(chipId, ch);
            } else {
                audio.audio.resetC352Mask(chipId, ch);
            }
            newParam.c352[chipId].channels[ch].mask = !newParam.c352[chipId].channels[ch].mask;
            break;
        case SEGAPCM:
            if (!newParam.segaPcm[chipId].channels[ch].mask || newParam.segaPcm[chipId].channels[ch].mask == null) {
                audio.audio.setSegaPCMMask(chipId, ch);
            } else {
                audio.audio.resetSegaPCMMask(chipId, ch);
            }
            newParam.segaPcm[chipId].channels[ch].mask = !newParam.segaPcm[chipId].channels[ch].mask;
            break;
        case QSound:
            if (!newParam.qSound[chipId].channels[ch].mask || newParam.qSound[chipId].channels[ch].mask == null) {
                audio.audio.setQSoundMask(chipId, ch);
            } else {
                audio.audio.resetQSoundMask(chipId, ch);
            }
            newParam.qSound[chipId].channels[ch].mask = !newParam.qSound[chipId].channels[ch].mask;
            break;
        case AY8910:
            if (!newParam.ay8910[chipId].channels[ch].mask || newParam.ay8910[chipId].channels[ch].mask == null) {
                audio.audio.setAY8910Mask(chipId, ch);
            } else {
                audio.audio.resetAY8910Mask(chipId, ch);
            }
            newParam.ay8910[chipId].channels[ch].mask = !newParam.ay8910[chipId].channels[ch].mask;
            break;
        case HuC6280:
            if (!newParam.huc6280[chipId].channels[ch].mask || newParam.huc6280[chipId].channels[ch].mask == null) {
                audio.audio.setHuC6280Mask(chipId, ch);
            } else {
                audio.audio.resetHuC6280Mask(chipId, ch);
            }
            newParam.huc6280[chipId].channels[ch].mask = !newParam.huc6280[chipId].channels[ch].mask;
            break;
        case OKIM6258:
            if (!newParam.okim6258[chipId].mask || newParam.okim6258[chipId].mask == null) {
                audio.audio.setOKIM6258Mask(chipId);
            } else {
                audio.audio.resetOKIM6258Mask(chipId);
            }
            newParam.okim6258[chipId].mask = !newParam.okim6258[chipId].mask;
            break;
        case OKIM6295:
            if (!newParam.okim6295[chipId].channels[ch].mask || newParam.okim6295[chipId].channels[ch].mask == null) {
                audio.audio.setOKIM6295Mask(chipId, ch);
            } else {
                audio.audio.resetOKIM6295Mask(chipId, ch);
            }
            newParam.okim6295[chipId].channels[ch].mask = !newParam.okim6295[chipId].channels[ch].mask;
            break;
        case NES:
            if (!newParam.nesdmc[chipId].sqrChannels[ch].mask || newParam.nesdmc[chipId].sqrChannels[ch].mask == null) {
                audio.audio.setNESMask(chipId, ch);
            } else {
                audio.audio.resetNESMask(chipId, ch);
            }
            newParam.nesdmc[chipId].sqrChannels[ch].mask = !newParam.nesdmc[chipId].sqrChannels[ch].mask;
            break;
        case DMC:
            switch (ch) {
            case 0:
                if (!newParam.nesdmc[chipId].triChannel.mask || newParam.nesdmc[chipId].triChannel.mask == null)
                    audio.audio.setDMCMask(chipId, ch);
                else audio.audio.resetDMCMask(chipId, ch);
                newParam.nesdmc[chipId].triChannel.mask = !newParam.nesdmc[chipId].triChannel.mask;
                break;
            case 1:
                if (!newParam.nesdmc[chipId].noiseChannel.mask || newParam.nesdmc[chipId].noiseChannel.mask == null)
                    audio.audio.setDMCMask(chipId, ch);
                else audio.audio.resetDMCMask(chipId, ch);
                newParam.nesdmc[chipId].noiseChannel.mask = !newParam.nesdmc[chipId].noiseChannel.mask;
                break;
            case 2:
                if (!newParam.nesdmc[chipId].dmcChannel.mask || newParam.nesdmc[chipId].dmcChannel.mask == null)
                    audio.audio.setDMCMask(chipId, ch);
                else audio.audio.resetDMCMask(chipId, ch);
                newParam.nesdmc[chipId].dmcChannel.mask = !newParam.nesdmc[chipId].dmcChannel.mask;
                break;
            }
            break;
        case FDS:
            if (!newParam.fds[chipId].channel.mask || newParam.fds[chipId].channel.mask == null)
                audio.audio.setFDSMask(chipId);
            else audio.audio.resetFDSMask(chipId);
            newParam.fds[chipId].channel.mask = !newParam.fds[chipId].channel.mask;
            break;
        case MMC5:
            switch (ch) {
            case 0:
                if (!newParam.mmc5[chipId].sqrChannels[0].mask || newParam.mmc5[chipId].sqrChannels[ch].mask == null)
                    audio.audio.setMMC5Mask(chipId, ch);
                else audio.audio.resetMMC5Mask(chipId, ch);
                newParam.mmc5[chipId].sqrChannels[0].mask = !newParam.mmc5[chipId].sqrChannels[0].mask;
                break;
            case 1:
                if (!newParam.mmc5[chipId].sqrChannels[1].mask || newParam.mmc5[chipId].sqrChannels[ch].mask == null)
                    audio.audio.setMMC5Mask(chipId, ch);
                else audio.audio.resetMMC5Mask(chipId, ch);
                newParam.mmc5[chipId].sqrChannels[1].mask = !newParam.mmc5[chipId].sqrChannels[1].mask;
                break;
            case 2:
                if (!newParam.mmc5[chipId].pcmChannel.mask || newParam.mmc5[chipId].pcmChannel.mask == null)
                    audio.audio.setMMC5Mask(chipId, ch);
                else audio.audio.resetMMC5Mask(chipId, ch);
                newParam.mmc5[chipId].pcmChannel.mask = !newParam.mmc5[chipId].pcmChannel.mask;
                break;
            }
            break;
        case VRC7:
            if (ch >= 0 && ch < 6) {
                if (!newParam.vrc7[chipId].channels[ch].mask || newParam.vrc7[chipId].channels[ch].mask == null)
                    audio.audio.setVRC7Mask(chipId, ch);
                else
                    audio.audio.resetVRC7Mask(chipId, ch);

                newParam.vrc7[chipId].channels[ch].mask = !newParam.vrc7[chipId].channels[ch].mask;
            }
            break;
        case K051649:
            if (ch >= 0 && ch < 5) {
                if (!newParam.k051649[chipId].channels[ch].mask || newParam.k051649[chipId].channels[ch].mask == null)
                    audio.audio.setK051649Mask(chipId, ch);
                else
                    audio.audio.resetK051649Mask(chipId, ch);

                newParam.k051649[chipId].channels[ch].mask = !newParam.k051649[chipId].channels[ch].mask;
            }
            break;
        case DMG:
            if (ch >= 0 && ch < 4) {
                if (!newParam.dmg[chipId].channels[ch].mask || newParam.dmg[chipId].channels[ch].mask == null)
                    audio.audio.setDMGMask(chipId, ch);
                else
                    audio.audio.resetDMGMask(chipId, ch);

                newParam.dmg[chipId].channels[ch].mask = !newParam.dmg[chipId].channels[ch].mask;
            }
            break;
        case VRC6:
            if (ch >= 0 && ch < 3) {
                if (!newParam.vrc6[chipId].channels[ch].mask || newParam.vrc6[chipId].channels[ch].mask == null)
                    audio.audio.setVRC6Mask(chipId, ch);
                else
                    audio.audio.resetVRC6Mask(chipId, ch);

                newParam.vrc6[chipId].channels[ch].mask = !newParam.vrc6[chipId].channels[ch].mask;
            }
            break;
        case N163:
            if (ch >= 0 && ch < 8) {
                if (!newParam.n106[chipId].channels[ch].mask || newParam.n106[chipId].channels[ch].mask == null)
                    audio.audio.setN163Mask(chipId, ch);
                else
                    audio.audio.resetN163Mask(chipId, ch);

                newParam.n106[chipId].channels[ch].mask = !newParam.n106[chipId].channels[ch].mask;
            }
            break;
        }
    }

    public void resetChannelMask(EnmChip chip, int chipId, int ch) {
        switch (chip) {
        case SN76489:
            newParam.sn76489[chipId].channels[ch].mask = false;
            audio.audio.resetSN76489Mask(chipId, ch);
            break;
        case RF5C164:
            newParam.rf5c164[chipId].channels[ch].mask = false;
            audio.audio.resetRF5C164Mask(chipId, ch);
            break;
        case RF5C68:
            newParam.rf5c68[chipId].channels[ch].mask = false;
            audio.audio.resetRF5C68Mask(chipId, ch);
            break;
        case YM2151:
            newParam.ym2151[chipId].channels[ch].mask = false;
            audio.audio.resetYM2151Mask(chipId, ch);
            break;
        case YM2203:
            if (ch >= 0 && ch < 9) {
                audio.audio.resetYM2203Mask(chipId, ch);
                newParam.ym2203[chipId].channels[ch].mask = false;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    newParam.ym2203[chipId].channels[2].mask = false;
                    newParam.ym2203[chipId].channels[6].mask = false;
                    newParam.ym2203[chipId].channels[7].mask = false;
                    newParam.ym2203[chipId].channels[8].mask = false;
                }
            }
            break;
        case YM2413:
            newParam.ym2413[chipId].channels[ch].mask = false;
            audio.audio.resetYM2413Mask(chipId, ch);
            break;
        case VRC7:
            newParam.vrc7[chipId].channels[ch].mask = false;
            audio.audio.resetVRC7Mask(chipId, ch);
            break;
        case YM2608:
            if (ch >= 0 && ch < 14) {
                audio.audio.resetYM2608Mask(chipId, ch);
                newParam.ym2608[chipId].channels[ch].mask = false;

                // FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    newParam.ym2608[chipId].channels[2].mask = false;
                    newParam.ym2608[chipId].channels[9].mask = false;
                    newParam.ym2608[chipId].channels[10].mask = false;
                    newParam.ym2608[chipId].channels[11].mask = false;
                }
            }
            break;
        case YM2610:
            if (ch >= 0 && ch < 14) {
                int c = ch;
                if (ch == 12) c = 13;
                if (ch == 13) c = 12;

                audio.audio.resetYM2610Mask(chipId, ch);
                newParam.ym2610[chipId].channels[c].mask = false;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    newParam.ym2610[chipId].channels[2].mask = false;
                    newParam.ym2610[chipId].channels[9].mask = false;
                    newParam.ym2610[chipId].channels[10].mask = false;
                    newParam.ym2610[chipId].channels[11].mask = false;
                }
            }
            break;
        case YM2612:
            if (ch >= 0 && ch < 9) {
                audio.audio.resetYM2612Mask(chipId, ch);
                newParam.ym2612[chipId].channels[ch].mask = false;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    newParam.ym2612[chipId].channels[2].mask = false;
                    newParam.ym2612[chipId].channels[6].mask = false;
                    newParam.ym2612[chipId].channels[7].mask = false;
                    newParam.ym2612[chipId].channels[8].mask = false;
                }
            }
            break;
        case YM3526:
            newParam.ym3526[chipId].channels[ch].mask = false;
            audio.audio.resetYM3526Mask(chipId, ch);
            break;
        case Y8950:
            newParam.y8950[chipId].channels[ch].mask = false;
            audio.audio.resetY8950Mask(chipId, ch);
            break;
        case YM3812:
            newParam.ym3812[chipId].channels[ch].mask = false;
            audio.audio.resetYM3812Mask(chipId, ch);
            break;
        case YMF262:
            newParam.ymf262[chipId].channels[ch].mask = false;
            audio.audio.resetYMF262Mask(chipId, ch);
            break;
        case YMF278B:
            newParam.ymf278b[chipId].channels[ch].mask = false;
            audio.audio.resetYMF278BMask(chipId, ch);
            break;
        case C140:
            newParam.c140[chipId].channels[ch].mask = false;
            if (ch < 24) audio.audio.resetC140Mask(chipId, ch);
            break;
        case PPZ8:
            newParam.ppz8[chipId].channels[ch].mask = false;
            if (ch < 8) audio.audio.resetPPZ8Mask(chipId, ch);
            break;
        case C352:
            newParam.c352[chipId].channels[ch].mask = false;
            if (ch < 32) audio.audio.resetC352Mask(chipId, ch);
            break;
        case SEGAPCM:
            newParam.segaPcm[chipId].channels[ch].mask = false;
            if (ch < 16) audio.audio.resetSegaPCMMask(chipId, ch);
            break;
        case QSound:
            newParam.qSound[chipId].channels[ch].mask = false;
            if (ch < 19) audio.audio.resetQSoundMask(chipId, ch);
            break;
        case AY8910:
            newParam.ay8910[chipId].channels[ch].mask = false;
            audio.audio.resetAY8910Mask(chipId, ch);
            break;
        case HuC6280:
            newParam.huc6280[chipId].channels[ch].mask = false;
            audio.audio.resetHuC6280Mask(chipId, ch);
            break;
        case K051649:
            newParam.k051649[chipId].channels[ch].mask = false;
            audio.audio.resetK051649Mask(chipId, ch);
            break;
        case OKIM6258:
            newParam.okim6258[chipId].mask = false;
            audio.audio.resetOKIM6258Mask(chipId);
            break;
        case OKIM6295:
            newParam.okim6295[chipId].channels[ch].mask = false;
            audio.audio.resetOKIM6295Mask(chipId, ch);
            break;
        case NES:
            switch (ch) {
            case 0:
            case 1:
                newParam.nesdmc[chipId].sqrChannels[ch].mask = false;
                audio.audio.resetNESMask(chipId, ch);
                break;
            case 2:
                newParam.nesdmc[chipId].triChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 0);
                break;
            case 3:
                newParam.nesdmc[chipId].noiseChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 1);
                break;
            case 4:
                newParam.nesdmc[chipId].dmcChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 2);
                break;
            }
            break;
        case DMC:
            switch (ch) {
            case 0:
                newParam.nesdmc[chipId].triChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 0);
                break;
            case 1:
                newParam.nesdmc[chipId].noiseChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 1);
                break;
            case 2:
                newParam.nesdmc[chipId].dmcChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 2);
                break;
            }
            break;
        case FDS:
            newParam.fds[chipId].channel.mask = false;
            audio.audio.resetFDSMask(chipId);
            break;
        case MMC5:
            switch (ch) {
            case 0:
                newParam.mmc5[chipId].sqrChannels[0].mask = false;
                break;
            case 1:
                newParam.mmc5[chipId].sqrChannels[1].mask = false;
                break;
            case 2:
                newParam.mmc5[chipId].pcmChannel.mask = false;
                break;
            }
            audio.audio.resetMMC5Mask(chipId, ch);
            break;
        case DMG:
            newParam.dmg[chipId].channels[ch].mask = false;
            audio.audio.resetDMGMask(chipId, ch);
            break;
        case VRC6:
            newParam.vrc6[chipId].channels[ch].mask = false;
            audio.audio.resetVRC6Mask(chipId, ch);
            break;
        case N163:
            newParam.n106[chipId].channels[ch].mask = false;
            audio.audio.resetN163Mask(chipId, ch);
            break;
        }
    }

    public void forceChannelMask(EnmChip chip, int chipId, int ch, boolean mask) {
        switch (chip) {
        case AY8910:
            if (mask)
                audio.audio.setAY8910Mask(chipId, ch);
            else
                audio.audio.resetAY8910Mask(chipId, ch);
            newParam.ay8910[chipId].channels[ch].mask = mask;
            oldParam.ay8910[chipId].channels[ch].mask = !mask;
            break;
        case C140:
            if (mask)
                audio.audio.setC140Mask(chipId, ch);
            else
                audio.audio.resetC140Mask(chipId, ch);
            newParam.c140[chipId].channels[ch].mask = mask;
            oldParam.c140[chipId].channels[ch].mask = !mask;
            break;
        case C352:
            if (mask)
                audio.audio.setC352Mask(chipId, ch);
            else
                audio.audio.resetC352Mask(chipId, ch);
            newParam.c352[chipId].channels[ch].mask = mask;
            oldParam.c352[chipId].channels[ch].mask = !mask;
            break;
        case HuC6280:
            if (mask)
                audio.audio.setHuC6280Mask(chipId, ch);
            else
                audio.audio.resetHuC6280Mask(chipId, ch);
            newParam.huc6280[chipId].channels[ch].mask = mask;
            oldParam.huc6280[chipId].channels[ch].mask = !mask;
            break;
        case RF5C164:
            if (mask)
                audio.audio.setRF5C164Mask(chipId, ch);
            else
                audio.audio.resetRF5C164Mask(chipId, ch);
            newParam.rf5c164[chipId].channels[ch].mask = mask;
            oldParam.rf5c164[chipId].channels[ch].mask = !mask;
            break;
        case RF5C68:
            if (mask)
                audio.audio.setRF5C68Mask(chipId, ch);
            else
                audio.audio.resetRF5C68Mask(chipId, ch);
            newParam.rf5c68[chipId].channels[ch].mask = mask;
            oldParam.rf5c68[chipId].channels[ch].mask = !mask;
            break;
        case SEGAPCM:
            if (mask)
                audio.audio.setSegaPCMMask(chipId, ch);
            else
                audio.audio.resetSegaPCMMask(chipId, ch);
            newParam.segaPcm[chipId].channels[ch].mask = mask;
            oldParam.segaPcm[chipId].channels[ch].mask = !mask;
            break;
        case QSound:
            if (mask)
                audio.audio.setQSoundMask(chipId, ch);
            else
                audio.audio.resetQSoundMask(chipId, ch);
            newParam.qSound[chipId].channels[ch].mask = mask;
            oldParam.qSound[chipId].channels[ch].mask = !mask;
            break;
        case YM2151:
            if (mask)
                audio.audio.setYM2151Mask(chipId, ch);
            else
                audio.audio.resetYM2151Mask(chipId, ch);
            newParam.ym2151[chipId].channels[ch].mask = mask;
            oldParam.ym2151[chipId].channels[ch].mask = !mask;
            break;
        case YM2203:
            if (ch >= 0 && ch < 9) {
                if (mask)
                    audio.audio.setYM2203Mask(chipId, ch);
                else
                    audio.audio.resetYM2203Mask(chipId, ch);

                newParam.ym2203[chipId].channels[ch].mask = mask;
                oldParam.ym2203[chipId].channels[ch].mask = !mask;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    newParam.ym2203[chipId].channels[2].mask = mask;
                    newParam.ym2203[chipId].channels[6].mask = mask;
                    newParam.ym2203[chipId].channels[7].mask = mask;
                    newParam.ym2203[chipId].channels[8].mask = mask;
                    oldParam.ym2203[chipId].channels[2].mask = !mask;
                    oldParam.ym2203[chipId].channels[6].mask = !mask;
                    oldParam.ym2203[chipId].channels[7].mask = !mask;
                    oldParam.ym2203[chipId].channels[8].mask = !mask;
                }
            }
            break;
        case YM2413:
            if (ch >= 0 && ch < 14) {
                if (mask)
                    audio.audio.setYM2413Mask(chipId, ch);
                else
                    audio.audio.resetYM2413Mask(chipId, ch);

                newParam.ym2413[chipId].channels[ch].mask = mask;
                oldParam.ym2413[chipId].channels[ch].mask = !mask;
            }
            break;
        case YM2608:
            if (ch >= 0 && ch < 14) {
                //if (mask)
                //    audio.audio.setYM2608Mask(chipId, ch);
                //else
                //    audio.audio.resetYM2608Mask(chipId, ch);

                newParam.ym2608[chipId].channels[ch].mask = mask;
                oldParam.ym2608[chipId].channels[ch].mask = !mask;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    newParam.ym2608[chipId].channels[2].mask = mask;
                    newParam.ym2608[chipId].channels[9].mask = mask;
                    newParam.ym2608[chipId].channels[10].mask = mask;
                    newParam.ym2608[chipId].channels[11].mask = mask;
                    oldParam.ym2608[chipId].channels[2].mask = !mask;
                    oldParam.ym2608[chipId].channels[9].mask = !mask;
                    oldParam.ym2608[chipId].channels[10].mask = !mask;
                    oldParam.ym2608[chipId].channels[11].mask = !mask;
                }
            }
            break;
        case YM2610:
            if (ch >= 0 && ch < 14) {
                int c = ch;
                if (ch == 12) c = 13;
                if (ch == 13) c = 12;

                if (mask)
                    audio.audio.setYM2610Mask(chipId, ch);
                else
                    audio.audio.resetYM2610Mask(chipId, ch);
                newParam.ym2610[chipId].channels[c].mask = mask;
                oldParam.ym2610[chipId].channels[c].mask = !mask;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 9 && ch < 12)) {
                    newParam.ym2610[chipId].channels[2].mask = mask;
                    newParam.ym2610[chipId].channels[9].mask = mask;
                    newParam.ym2610[chipId].channels[10].mask = mask;
                    newParam.ym2610[chipId].channels[11].mask = mask;
                    oldParam.ym2610[chipId].channels[2].mask = !mask;
                    oldParam.ym2610[chipId].channels[9].mask = !mask;
                    oldParam.ym2610[chipId].channels[10].mask = !mask;
                    oldParam.ym2610[chipId].channels[11].mask = !mask;
                }
            }
            break;
        case YM2612:
            if (ch >= 0 && ch < 9) {
                if (mask)
                    audio.audio.setYM2612Mask(chipId, ch);
                else
                    audio.audio.resetYM2612Mask(chipId, ch);

                newParam.ym2612[chipId].channels[ch].mask = mask;
                oldParam.ym2612[chipId].channels[ch].mask = null;

                //FM(2ch) FMex
                if ((ch == 2) || (ch >= 6 && ch < 9)) {
                    newParam.ym2612[chipId].channels[2].mask = mask;
                    newParam.ym2612[chipId].channels[6].mask = mask;
                    newParam.ym2612[chipId].channels[7].mask = mask;
                    newParam.ym2612[chipId].channels[8].mask = mask;
                    oldParam.ym2612[chipId].channels[2].mask = null;
                    oldParam.ym2612[chipId].channels[6].mask = null;
                    oldParam.ym2612[chipId].channels[7].mask = null;
                    oldParam.ym2612[chipId].channels[8].mask = null;
                }
            }
            break;
        case YM3526:
            if (ch >= 0 && ch < 14) {
                if (mask)
                    audio.audio.setYM3526Mask(chipId, ch);
                else
                    audio.audio.resetYM3526Mask(chipId, ch);

                newParam.ym3526[chipId].channels[ch].mask = mask;
                oldParam.ym3526[chipId].channels[ch].mask = !mask;
            }
            break;
        case Y8950:
            if (ch >= 0 && ch < 15) {
                if (mask)
                    audio.audio.setY8950Mask(chipId, ch);
                else
                    audio.audio.resetY8950Mask(chipId, ch);

                newParam.y8950[chipId].channels[ch].mask = mask;
                oldParam.y8950[chipId].channels[ch].mask = !mask;
            }
            break;
        case YM3812:
            if (ch >= 0 && ch < 14) {
                if (mask)
                    audio.audio.setYM3812Mask(chipId, ch);
                else
                    audio.audio.resetYM3812Mask(chipId, ch);

                newParam.ym3812[chipId].channels[ch].mask = mask;
                oldParam.ym3812[chipId].channels[ch].mask = !mask;
            }
            break;
        case YMF262:
            if (ch >= 0 && ch < 24) {
                if (mask)
                    audio.audio.setYMF262Mask(chipId, ch);
                else
                    audio.audio.resetYMF262Mask(chipId, ch);

                newParam.ymf262[chipId].channels[ch].mask = mask;
                oldParam.ymf262[chipId].channels[ch].mask = !mask;
            }
            break;
        case YMF278B:
            if (ch >= 0 && ch < 47) {
                if (mask)
                    audio.audio.setYMF278BMask(chipId, ch);
                else
                    audio.audio.resetYMF278BMask(chipId, ch);

                newParam.ymf278b[chipId].channels[ch].mask = mask;
                oldParam.ymf278b[chipId].channels[ch].mask = !mask;
            }
            break;
        case SN76489:
            if (mask)
                audio.audio.setSN76489Mask(chipId, ch);
            else
                audio.audio.resetSN76489Mask(chipId, ch);
            newParam.sn76489[chipId].channels[ch].mask = mask;
            oldParam.sn76489[chipId].channels[ch].mask = !mask;
            break;
        case OKIM6295:
            if (mask)
                audio.audio.setOKIM6295Mask(chipId, ch);
            else
                audio.audio.resetOKIM6295Mask(chipId, ch);
            newParam.okim6295[chipId].channels[ch].mask = mask;
            oldParam.okim6295[chipId].channels[ch].mask = !mask;
            break;
        case DMG:
            if (mask)
                audio.audio.setDMGMask(chipId, ch);
            else
                audio.audio.resetDMGMask(chipId, ch);
            newParam.dmg[chipId].channels[ch].mask = mask;
            oldParam.dmg[chipId].channels[ch].mask = !mask;
            break;
        case VRC6:
            if (mask)
                audio.audio.setVRC6Mask(chipId, ch);
            else
                audio.audio.resetVRC6Mask(chipId, ch);
            newParam.vrc6[chipId].channels[ch].mask = mask;
            oldParam.vrc6[chipId].channels[ch].mask = !mask;
            break;
        case N163:
            if (mask)
                audio.audio.setN163Mask(chipId, ch);
            else
                audio.audio.resetN163Mask(chipId, ch);
            newParam.n106[chipId].channels[ch].mask = mask;
            oldParam.n106[chipId].channels[ch].mask = !mask;
            break;
        }
    }

    public void ForceChannelMaskNES(EnmChip chip, int chipId, int ch, MDChipParams.NESDMC[] param) {
        if (ch == 0 || ch == 1) {
            if (param[chipId].sqrChannels[ch].mask) {
                newParam.nesdmc[chipId].sqrChannels[ch].mask = true;
                audio.audio.setNESMask(chipId, ch);
            } else {
                newParam.nesdmc[chipId].sqrChannels[ch].mask = false;
                audio.audio.resetNESMask(chipId, ch);
            }
        } else if (ch == 2) {
            if (param[chipId].triChannel.mask) {
                newParam.nesdmc[chipId].triChannel.mask = true;
                audio.audio.setDMCMask(chipId, 0);
            } else {
                newParam.nesdmc[chipId].triChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 0);
            }

        } else if (ch == 3) {
            if (param[chipId].noiseChannel.mask) {
                newParam.nesdmc[chipId].noiseChannel.mask = true;
                audio.audio.setDMCMask(chipId, 1);
            } else {
                newParam.nesdmc[chipId].noiseChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 1);
            }

        } else if (ch == 4) {
            if (param[chipId].dmcChannel.mask) {
                newParam.nesdmc[chipId].dmcChannel.mask = true;
                audio.audio.setDMCMask(chipId, 2);
            } else {
                newParam.nesdmc[chipId].dmcChannel.mask = false;
                audio.audio.resetDMCMask(chipId, 2);
            }
        }
    }

    private void StartMIDIInMonitoring() {

        if (setting.getMidiKbd().getMidiInDeviceName().isEmpty()) {
            return;
        }

        if (midiin != null) {
            try {
                midiin.close();
                midiIn_MessageReceived.close();
                midiin = null;
            } catch (Exception e) {
                e.printStackTrace();
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
                        e.printStackTrace();
                        midiin = null;
                    }
                }
            }
        }
    }

//    void midiIn_ErrorReceived(Object source, MidiInMessageEventArgs e) {
//        System.err.printf("Error Time %s Message 0x%08x Event %s",
//                e.Timestamp, e.RawMessage, e.MidiEvent));
//    }

    private void StopMIDIInMonitoring() {
        if (midiin != null) {
            try {
                midiin.close();
                this.midiIn_MessageReceived.close();
                midiin = null;
            } catch (Exception e) {
                e.printStackTrace();
                midiin = null;
            }
        }
    }

    Receiver midiIn_MessageReceived = new Receiver() {
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
        ym2612MIDI.newParam.ym2612Midi.selectCh = ch;
        ym2612MIDI.newParam.ym2612Midi.selectParam = n;
    }

    public void ym2612Midi_AddSelectInstParam(int n) {
        int p = ym2612MIDI.newParam.ym2612Midi.selectParam;
        p += n;
        if (p > 47) p = 0;
        ym2612MIDI.newParam.ym2612Midi.selectParam = p;
    }

    public void ym2612Midi_ChangeSelectedParamValue(int n) {
        ym2612MIDI.changeSelectedParamValue(n);
    }

    private void loadPresetMixerBalance(String playingFileName, String playingArcFileName, FileFormat format) {
        if (!setting.getAutoBalance().getUseThis()) return;

        try {
            Setting.Balance balance;
            String fullPath = mdplayer.Common.settingFilePath;
            fullPath = Path.combine(fullPath, "MixerBalance");
            if (!Directory.exists(fullPath)) Directory.createDirectory(fullPath);
            String fn = "";
            String defMbc = "";

            // 曲ごとのプリセットを読み込むモード
            if (setting.getAutoBalance().getLoadSongBalance()) {
                if (setting.getAutoBalance().getSamePositionAsSongData()) {
                    fullPath = getDirectoryName(playingFileName);
                    if (playingArcFileName != null && playingArcFileName.isEmpty()) {
                        fullPath = getDirectoryName(playingArcFileName);
                    }
                }
                fn = Path.getFileName(playingFileName);
                if (playingArcFileName != null && playingArcFileName.isEmpty()) {
                    fn = Path.getFileName(playingArcFileName);
                }
                fn += ".mbc";
                if (!File.exists(Path.combine(fullPath, fn))) {
                    fn = "";
                    fullPath = mdplayer.Common.settingFilePath;
                    fullPath = Path.combine(fullPath, "MixerBalance");
                } else {
                    fullPath = Path.combine(fullPath, fn);
                }
            }

            // ドライバごとのプリセットを読み込むモード
            if (setting.getAutoBalance().getLoadDriverBalance() && fn.isEmpty()) {
                String[] fns = format.getPresetMixerBalance();
                if (fns != null) {
                    fn = fns[0];
                    defMbc = fns[1];

                    fullPath = Path.combine(fullPath, fn);
                }
            }

            if (fn == null || fn.isEmpty()) return;

            // 存在確認。無い場合は作成。
            if (!File.exists(fullPath) && !defMbc.equals("")) File.writeAllText(fullPath, defMbc);
            // データフォルダに存在するファイルを読み込む
            balance = Setting.Balance.load(fullPath);

            if (balance == null) return;

            // ミキサーバランス変更処理
            setting.setBalance(balance);
            if (frmMixer2 != null) frmMixer2.update();
//            Application.DoEvents();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ManualSavePresetMixerBalance(boolean isDriverBalance, String playingFileName, String playingArcFileName, FileFormat format, Setting.Balance balance) {
        if (!setting.getAutoBalance().getUseThis()) return;

        try {
            String fullPath = mdplayer.Common.settingFilePath;
            fullPath = Path.combine(fullPath, "MixerBalance");
            if (!Directory.exists(fullPath)) Directory.createDirectory(fullPath);
            String fn = "";

            if (isDriverBalance) {
                String[] fns = format.getPresetMixerBalance();
                if (fns != null) {
                    fn = fns[0];
                }

                fullPath = Path.combine(fullPath, fn);

            } else {

            }

            balance.save(fullPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String SaveDriverBalance(Setting.Balance balance) {
        PlayList.Music music = frmPlayList.getPlayingSongInfo();
        if (music == null) {
            throw new IllegalStateException("演奏情報が取得できませんでした。\n演奏中又は演奏完了直後に再度お試しください。");
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

    NativeKeyListener keyboardHook1_KeyboardHooked = new NativeKeyListener() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

            if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException nativeHookException) {
                    nativeHookException.printStackTrace();
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

            info = setting.getKeyBoardHook().getStop();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                stop();
                return;
            }

            info = setting.getKeyBoardHook().getPause();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                pause();
                return;
            }

            info = setting.getKeyBoardHook().getFadeout();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                fadeout();
                return;
            }

            info = setting.getKeyBoardHook().getPrev();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                prev();
                return;
            }

            info = setting.getKeyBoardHook().getSlow();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                slow();
                return;
            }

            info = setting.getKeyBoardHook().getPlay();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                play();
                return;
            }

            info = setting.getKeyBoardHook().getNext();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                next();
                return;
            }

            info = setting.getKeyBoardHook().getFast();
            if (info.getKey().equals(k) && info.getShift() == shift && info.getCtrl() == ctrl && info.getAlt() == alt) {
                ff();
            }
        }
    };

    private void checkAndSetForm(JFrame frm) {
        Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle rc = new Rectangle(frmInfo.getLocation(), frmInfo.getSize());
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
        if (audio.audio.isPaused()) {
            audio.audio.pause();
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
                ex.printStackTrace();
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
        if (ev.getSource() == tsmiChangeZoomX1) setting.getOther().setZoom(1);
        else if (ev.getSource() == tsmiChangeZoomX2) setting.getOther().setZoom(2);
        else if (ev.getSource() == tsmiChangeZoomX3) setting.getOther().setZoom(3);
        else if (ev.getSource() == tsmiChangeZoomX4) setting.getOther().setZoom(4);
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
        if (ev.getSource() == yM2612ToolStripMenuItem) openFormRegTest(0, EnmChip.YM2612, false);
        else if (ev.getSource() == ym2151ToolStripMenuItem) openFormRegTest(0, EnmChip.YM2151, false);
        else if (ev.getSource() == ym2203ToolStripMenuItem) openFormRegTest(0, EnmChip.YM2203, false);
        else if (ev.getSource() == ym2413ToolStripMenuItem) openFormRegTest(0, EnmChip.YM2413, false);
        else if (ev.getSource() == ym2608ToolStripMenuItem) openFormRegTest(0, EnmChip.YM2608, false);
        else if (ev.getSource() == yMF278BToolStripMenuItem) openFormRegTest(0, EnmChip.YMF278B, false);
        else if (ev.getSource() == yMF262ToolStripMenuItem) openFormRegTest(0, EnmChip.YMF262, false);
        else if (ev.getSource() == yM2610ToolStripMenuItem) openFormRegTest(0, EnmChip.YM2610, false);
        else if (ev.getSource() == qSoundToolStripMenuItem) openFormRegTest(0, EnmChip.QSound, false);
        else if (ev.getSource() == segaPCMToolStripMenuItem) openFormRegTest(0, EnmChip.SEGAPCM, false);
        else if (ev.getSource() == yMZ280BToolStripMenuItem) openFormRegTest(0, EnmChip.YMZ280B, false);
        else if (ev.getSource() == sN76489ToolStripMenuItem) openFormRegTest(0, EnmChip.SN76489, false);
        else if (ev.getSource() == aY8910ToolStripMenuItem) openFormRegTest(0, EnmChip.AY8910, false);
        else if (ev.getSource() == c140ToolStripMenuItem) openFormRegTest(0, EnmChip.C140, false);
        else if (ev.getSource() == c352ToolStripMenuItem) openFormRegTest(0, EnmChip.C352, false);
        else if (ev.getSource() == yM3812ToolStripMenuItem) openFormRegTest(0, EnmChip.YM3812, false);
        else if (ev.getSource() == sIDToolStripMenuItem) openFormRegTest(0, EnmChip.SID, false);
        else openFormRegTest(0, EnmChip.Unuse, false);
    }

    private BufferedImage[] lstOpeButtonEnterImage = new BufferedImage[] {
            Resources.getChSetting(),
            Resources.getChStop(),
            Resources.getChPause(),
            Resources.getChFadeout(),
            Resources.getChPrevious(),
            Resources.getChSlow(),
            Resources.getChPlay(),
            Resources.getChFast(),
            Resources.getChNext(),
            Resources.getChStep(),
            Resources.getChOpenFolder(),
            Resources.getChPlayList(),
            Resources.getChInformation(),
            Resources.getChMixer(),
            Resources.getChKBD(),
            Resources.getChVST(),
            Resources.getChMIDIKBD(),
            Resources.getChZoom(),
            Resources.getChRandom(),
            Resources.getChLoop(),
            Resources.getChLoopOne()
    };
    private BufferedImage[] lstOpeButtonLeaveImage = new BufferedImage[] {
            Resources.getCcSetting(),
            Resources.getCcStop(),
            Resources.getCcPause(),
            Resources.getCcFadeout(),
            Resources.getCcPrevious(),
            Resources.getCcSlow(),
            Resources.getCcPlay(),
            Resources.getCcFast(),
            Resources.getCcNext(),
            Resources.getCcStep(),
            Resources.getCcOpenFolder(),
            Resources.getCcPlayList(),
            Resources.getCcInformation(),
            Resources.getCcMixer(),
            Resources.getCcKBD(),
            Resources.getCcVST(),
            Resources.getCcMIDIKBD(),
            Resources.getCcZoom(),
            Resources.getCcRandom(),
            Resources.getCcLoop(),
            Resources.getCcLoopOne()
    };
    private BufferedImage[] lstOpeButtonActiveImage = new BufferedImage[] {
            Resources.getCiSetting(),
            Resources.getCiStop(),
            Resources.getCiPause(),
            Resources.getCiFadeout(),
            Resources.getCiPrevious(),
            Resources.getCiSlow(),
            Resources.getCiPlay(),
            Resources.getCiFast(),
            Resources.getCiNext(),
            Resources.getCiStep(),
            Resources.getCiOpenFolder(),
            Resources.getCiPlayList(),
            Resources.getCiInformation(),
            Resources.getCiMixer(),
            Resources.getCiKBD(),
            Resources.getCiVST(),
            Resources.getCiMIDIKBD(),
            Resources.getCiZoom(),
            Resources.getCiRandom(),
            Resources.getCiLoop(),
            Resources.getCiLoopOne()
    };
    private boolean[] lstOpeButtonActive = new boolean[] {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false
    };
    private boolean[] lstOpeButtonActiveOld = new boolean[] {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false
    };
    private JButton[] lstOpeButtonControl;

    private void RelocateOpeButton(int zoom) {
        opeButtonSetting.setLocation(new Point((17 + 0) * zoom, 9 * zoom));
        opeButtonStop.setLocation(new Point((17 + 16 * 1) * zoom, 9 * zoom));
        opeButtonPause.setLocation(new Point((17 + 16 * 2) * zoom, 9 * zoom));
        opeButtonFadeout.setLocation(new Point((17 + 16 * 3) * zoom, 9 * zoom));
        opeButtonPrevious.setLocation(new Point((17 + 16 * 4) * zoom, 9 * zoom));
        opeButtonSlow.setLocation(new Point((17 + 16 * 5) * zoom, 9 * zoom));
        opeButtonPlay.setLocation(new Point((17 + 16 * 6) * zoom, 9 * zoom));
        opeButtonFast.setLocation(new Point((17 + 16 * 7) * zoom, 9 * zoom));
        opeButtonNext.setLocation(new Point((17 + 16 * 8) * zoom, 9 * zoom));
        opeButtonMode.setLocation(new Point((17 + 16 * 9) * zoom, 9 * zoom));
        opeButtonOpen.setLocation(new Point((17 + 16 * 10) * zoom, 9 * zoom));
        opeButtonPlayList.setLocation(new Point((17 + 16 * 11) * zoom, 9 * zoom));
        opeButtonInformation.setLocation(new Point((17 + 16 * 12) * zoom, 9 * zoom));
        opeButtonMixer.setLocation(new Point((17 + 16 * 13) * zoom, 9 * zoom));
        opeButtonKBD.setLocation(new Point((17 + 16 * 14) * zoom, 9 * zoom));
        opeButtonVST.setLocation(new Point((17 + 16 * 15) * zoom, 9 * zoom));
        opeButtonMIDIKBD.setLocation(new Point((17 + 16 * 16) * zoom, 9 * zoom));
        opeButtonZoom.setLocation(new Point((17 + 16 * 17) * zoom, 9 * zoom));

        RedrawButton(opeButtonSetting, setting.getOther().getZoom(), lstOpeButtonLeaveImage[0]);
        RedrawButton(opeButtonStop, setting.getOther().getZoom(), lstOpeButtonLeaveImage[1]);
        RedrawButton(opeButtonPause, setting.getOther().getZoom(), lstOpeButtonLeaveImage[2]);
        RedrawButton(opeButtonFadeout, setting.getOther().getZoom(), lstOpeButtonLeaveImage[3]);
        RedrawButton(opeButtonPrevious, setting.getOther().getZoom(), lstOpeButtonLeaveImage[4]);
        RedrawButton(opeButtonSlow, setting.getOther().getZoom(), lstOpeButtonLeaveImage[5]);
        RedrawButton(opeButtonPlay, setting.getOther().getZoom(), lstOpeButtonLeaveImage[6]);
        RedrawButton(opeButtonFast, setting.getOther().getZoom(), lstOpeButtonLeaveImage[7]);
        RedrawButton(opeButtonNext, setting.getOther().getZoom(), lstOpeButtonLeaveImage[8]);
        int m = newButtonMode[9] == 0 ? 9 : (newButtonMode[9] == 1 ? 18 : (newButtonMode[9] == 2 ? 19 : 20));
        RedrawButton(opeButtonMode, setting.getOther().getZoom(), lstOpeButtonLeaveImage[m]);
        RedrawButton(opeButtonOpen, setting.getOther().getZoom(), lstOpeButtonLeaveImage[10]);
        RedrawButton(opeButtonPlayList, setting.getOther().getZoom(), lstOpeButtonLeaveImage[11]);
        RedrawButton(opeButtonInformation, setting.getOther().getZoom(), lstOpeButtonLeaveImage[12]);
        RedrawButton(opeButtonMixer, setting.getOther().getZoom(), lstOpeButtonLeaveImage[13]);
        RedrawButton(opeButtonKBD, setting.getOther().getZoom(), lstOpeButtonLeaveImage[14]);
        RedrawButton(opeButtonVST, setting.getOther().getZoom(), lstOpeButtonLeaveImage[15]);
        RedrawButton(opeButtonMIDIKBD, setting.getOther().getZoom(), lstOpeButtonLeaveImage[16]);
        RedrawButton(opeButtonZoom, setting.getOther().getZoom(), lstOpeButtonLeaveImage[17]);
    }

    MouseListener opeButton_Mouse = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent ev) {
            JButton btn = (JButton) ev.getSource();
            int index = Integer.parseInt(btn.getActionCommand());
            int m = index;
            if (m == 9) {
                m = newButtonMode[9] == 0 ? 9 : (newButtonMode[9] == 1 ? 18 : (newButtonMode[9] == 2 ? 19 : 20));
            }
            RedrawButton(btn, setting.getOther().getZoom(), lstOpeButtonEnterImage[m]);
        }

        @Override
        public void mouseExited(MouseEvent ev) {
            JButton btn = (JButton) ev.getSource();
            int index = Integer.parseInt(btn.getActionCommand());
            int m = index;
            if (m == 9) {
                m = newButtonMode[9] == 0 ? 9 : (newButtonMode[9] == 1 ? 18 : (newButtonMode[9] == 2 ? 19 : 20));
            }

            RedrawButton(btn, setting.getOther().getZoom(), lstOpeButtonActive[m] ? lstOpeButtonActiveImage[m] : lstOpeButtonLeaveImage[m]);
        }
    };

    private void RedrawButton(JButton button, int zoom, BufferedImage image) {
        try {
            final int size = 16;
            if (button.getSize().width != size * zoom) button.setPreferredSize(new Dimension(size * zoom, size * zoom));

            Image canvas;
            if (button.getIcon() != null && button.getIcon().getIconWidth() == size * zoom)
                canvas = ((ImageIcon) button.getIcon()).getImage();
            else
                canvas = new BufferedImage(button.getWidth(), button.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) canvas.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setColor(Color.black);
            g.clearRect(0, 0, canvas.getWidth(null), canvas.getHeight(null));
            g.drawImage(image, 0, 0, canvas.getWidth(null), canvas.getHeight(null), 0, 0, size * zoom, size * zoom, null);

            button.setIcon(new ImageIcon(canvas));
        } catch (Exception e) {
            e.printStackTrace();
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
        opeButton_Mouse.mouseEntered(null); // opeButtonMode
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
        lstOpeButtonActive[1] = (audio.isStopped()); // STOP button
        lstOpeButtonActive[2] = audio.isStopped() ? false : audio.audio.isPaused(); // PAUSE button
        lstOpeButtonActive[3] = audio.isStopped() ? false : audio.isFadeOut(); // Fade button
        lstOpeButtonActive[5] = audio.isSlow(); // Slowbutton
        lstOpeButtonActive[6] = audio.audio.isPaused() ? false : (audio.isSlow() || audio.isFF() || audio.isFadeOut() ? false : !audio.isStopped()); // PLAY button
        lstOpeButtonActive[7] = audio.isFF(); // FFbutton
    }

    private void initializeComponent() {
//        this.components = new System.ComponentModel.Container();
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmMain));
        this.pbScreen = new JLabel();
        this.cmsOpenOtherPanel = new JPopupMenu();
        this.primaryToolStripMenuItem = new JMenuItem();
        this.tsmiCPPSG = new JMenuItem();
        this.tsmiPAY8910 = new JMenuItem();
        this.tsmiPDCSG = new JMenuItem();
        this.tsmiCPWF = new JMenuItem();
        this.tsmiPHuC6280 = new JMenuItem();
        this.tsmiPK051649 = new JMenuItem();
        this.toolStripMenuItem2 = new JMenuItem();
        this.tsmiCPOPL = new JMenuItem();
        this.tsmiPOPLL = new JMenuItem();
        this.tsmiPOPL = new JMenuItem();
        this.tsmiPY8950 = new JMenuItem();
        this.tsmiPOPL2 = new JMenuItem();
        this.tsmiPOPL3 = new JMenuItem();
        this.tsmiPOPL4 = new JMenuItem();
        this.tsmiCPOPN = new JMenuItem();
        this.tsmiPOPN = new JMenuItem();
        this.tsmiPOPN2 = new JMenuItem();
        this.tsmiPOPNA = new JMenuItem();
        this.tsmiPOPNB = new JMenuItem();
        this.tsmiPOPM = new JMenuItem();
        this.tsmiPOPX = new JMenuItem();
        this.tsmiYMZ280B = new JMenuItem();
        this.tsmiCPPCM = new JMenuItem();
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
        this.tsmiCPNES = new JMenuItem();
        this.tsmiPNESDMC = new JMenuItem();
        this.tsmiPFDS = new JMenuItem();
        this.tsmiPMMC5 = new JMenuItem();
        this.tsmiPVRC6 = new JMenuItem();
        this.tsmiPVRC7 = new JMenuItem();
        this.tsmiPN106 = new JMenuItem();
        this.tsmiPS5B = new JMenuItem();
        this.tsmiPDMG = new JMenuItem();
        this.sencondryToolStripMenuItem = new JMenu();
        this.tsmiCSPSG = new JMenuItem();
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
        this.tsmiCSOPN = new JMenuItem();
        this.tsmiSOPN = new JMenuItem();
        this.tsmiSOPN2 = new JMenuItem();
        this.tsmiSOPNA = new JMenuItem();
        this.tsmiSOPNB = new JMenuItem();
        this.tsmiSOPM = new JMenuItem();
        this.tsmiSOPX = new JMenuItem();
        this.tsmiSYMZ280B = new JMenuItem();
        this.tsmiCSPCM = new JMenuItem();
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
        this.tsmiCSNES = new JMenuItem();
        this.tsmiSFDS = new JMenuItem();
        this.tsmiSMMC5 = new JMenuItem();
        this.tsmiSNESDMC = new JMenuItem();
        this.tsmiSVRC6 = new JMenuItem();
        this.tsmiSVRC7 = new JMenuItem();
        this.tsmiSN106 = new JMenuItem();
        this.tsmiSS5B = new JMenuItem();
        this.tsmiSDMG = new JMenuItem();
        this.cmsMenu = new JPopupMenu();
        this.ファイルToolStripMenuItem = new JMenuItem();
        this.tsmiOpenFile = new JMenuItem();
        this.tsmiExit = new JMenuItem();
        this.操作ToolStripMenuItem = new JMenuItem();
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
        this.その他ウィンドウ表示ToolStripMenuItem = new JMenuItem();
        this.tsmiKBrd = new JMenuItem();
        this.tsmiVST = new JMenuItem();
        this.tsmiMIDIkbd = new JMenuItem();
        this.tsmiChangeZoom = new JMenuItem();
        this.tsmiChangeZoomX1 = new JMenuItem();
        this.tsmiChangeZoomX2 = new JMenuItem();
        this.tsmiChangeZoomX3 = new JMenuItem();
        this.tsmiChangeZoomX4 = new JMenuItem();
        this.レジスタダンプ表示ToolStripMenuItem = new JMenuItem();
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
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();
//        this.cmsOpenOtherPanel.SuspendLayout();
//        this.cmsMenu.SuspendLayout();

        //
        // pbScreen
        //
        this.pbScreen.setBackground(Color.black);
        new DropTarget(this.pbScreen, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        //resources.ApplyResources(this.pbScreen, "pbScreen");
        this.pbScreen.setIcon(new ImageIcon(mdplayer.properties.Resources.getPlaneControl()));
        this.pbScreen.setName("pbScreen");
        // this.pbScreen.TabStop = false;
//        this.pbScreen.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.pbScreen.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
//        this.pbScreen.addMouseListener(this.pbScreen_MouseLeave);
        this.pbScreen.addMouseMotionListener(this.pbScreen_MouseMove);
        //
        // cmsOpenOtherPanel
        //
//        this.cmsOpenOtherPanel.ImageScalingSize(new Dimension(20, 20));
        this.cmsOpenOtherPanel.add(this.primaryToolStripMenuItem);
        this.cmsOpenOtherPanel.add(this.sencondryToolStripMenuItem);
        this.cmsOpenOtherPanel.setName("cmsOpenOtherPanel");
        //resources.ApplyResources(this.cmsOpenOtherPanel, "cmsOpenOtherPanel");
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
        //resources.ApplyResources(this.primaryToolStripMenuItem, "primaryToolStripMenuItem");
        //
        // tsmiCPPSG
        //
        this.tsmiCPPSG.add(this.tsmiPAY8910);
        this.tsmiCPPSG.add(this.tsmiPDCSG);
        this.tsmiCPPSG.setName("tsmiCPPSG");
        //resources.ApplyResources(this.tsmiCPPSG, "tsmiCPPSG");
        //
        // tsmiPAY8910
        //
        this.tsmiPAY8910.setName("tsmiPAY8910");
        //resources.ApplyResources(this.tsmiPAY8910, "tsmiPAY8910");
        this.tsmiPAY8910.addActionListener(this::tsmiPAY8910_Click);
        //
        // tsmiPDCSG
        //
        this.tsmiPDCSG.setName("tsmiPDCSG");
        //resources.ApplyResources(this.tsmiPDCSG, "tsmiPDCSG");
        this.tsmiPDCSG.addActionListener(this::tsmiPDCSG_Click);
        //
        // tsmiCPWF
        //
        this.tsmiCPWF.add(this.tsmiPHuC6280);
        this.tsmiCPWF.add(this.tsmiPK051649);
        this.tsmiCPWF.add(this.toolStripMenuItem2);
        this.tsmiCPWF.setName("tsmiCPWF");
        //resources.ApplyResources(this.tsmiCPWF, "tsmiCPWF");
        //
        // tsmiPHuC6280
        //
        this.tsmiPHuC6280.setName("tsmiPHuC6280");
        //resources.ApplyResources(this.tsmiPHuC6280, "tsmiPHuC6280");
        this.tsmiPHuC6280.addActionListener(this::tsmiPHuC6280_Click);
        //
        // tsmiPK051649
        //
        this.tsmiPK051649.setName("tsmiPK051649");
        //resources.ApplyResources(this.tsmiPK051649, "tsmiPK051649");
        this.tsmiPK051649.addActionListener(this::tsmiPK051649_Click);
        //
        // toolStripMenuItem2
        //
        this.toolStripMenuItem2.setName("toolStripMenuItem2");
        //resources.ApplyResources(this.toolStripMenuItem2, "toolStripMenuItem2");
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
        //resources.ApplyResources(this.tsmiCPOPL, "tsmiCPOPL");
        //
        // tsmiPOPLL
        //
        this.tsmiPOPLL.setName("tsmiPOPLL");
        //resources.ApplyResources(this.tsmiPOPLL, "tsmiPOPLL");
        this.tsmiPOPLL.addActionListener(this::tsmiPOPLL_Click);
        //
        // tsmiPOPL
        //
        this.tsmiPOPL.setName("tsmiPOPL");
        //resources.ApplyResources(this.tsmiPOPL, "tsmiPOPL");
        this.tsmiPOPL.addActionListener(this::tsmiPOPL_Click);
        //
        // tsmiPY8950
        //
        this.tsmiPY8950.setName("tsmiPY8950");
        //resources.ApplyResources(this.tsmiPY8950, "tsmiPY8950");
        this.tsmiPY8950.addActionListener(this::tsmiPY8950_Click);
        //
        // tsmiPOPL2
        //
        this.tsmiPOPL2.setName("tsmiPOPL2");
        //resources.ApplyResources(this.tsmiPOPL2, "tsmiPOPL2");
        this.tsmiPOPL2.addActionListener(this::tsmiPOPL2_Click);
        //
        // tsmiPOPL3
        //
        this.tsmiPOPL3.setName("tsmiPOPL3");
        //resources.ApplyResources(this.tsmiPOPL3, "tsmiPOPL3");
        this.tsmiPOPL3.addActionListener(this::tsmiPOPL3_Click);
        //
        // tsmiPOPL4
        //
        this.tsmiPOPL4.setName("tsmiPOPL4");
        //resources.ApplyResources(this.tsmiPOPL4, "tsmiPOPL4");
        this.tsmiPOPL4.addActionListener(this::tsmiPOPL4_Click);
        //
        // tsmiCPOPN
        //
        this.tsmiCPOPN.add(this.tsmiPOPN);
        this.tsmiCPOPN.add(this.tsmiPOPN2);
        this.tsmiCPOPN.add(this.tsmiPOPNA);
        this.tsmiCPOPN.add(this.tsmiPOPNB);
        this.tsmiCPOPN.setName("tsmiCPOPN");
        //resources.ApplyResources(this.tsmiCPOPN, "tsmiCPOPN");
        //
        // tsmiPOPN
        //
        this.tsmiPOPN.setName("tsmiPOPN");
        //resources.ApplyResources(this.tsmiPOPN, "tsmiPOPN");
        this.tsmiPOPN.addActionListener(this::tsmiPOPN_Click);
        //
        // tsmiPOPN2
        //
        this.tsmiPOPN2.setName("tsmiPOPN2");
        //resources.ApplyResources(this.tsmiPOPN2, "tsmiPOPN2");
        this.tsmiPOPN2.addActionListener(this::tsmiPOPN2_Click);
        //
        // tsmiPOPNA
        //
        this.tsmiPOPNA.setName("tsmiPOPNA");
        //resources.ApplyResources(this.tsmiPOPNA, "tsmiPOPNA");
        this.tsmiPOPNA.addActionListener(this::tsmiPOPNA_Click);
        //
        // tsmiPOPNB
        //
        this.tsmiPOPNB.setName("tsmiPOPNB");
        //resources.ApplyResources(this.tsmiPOPNB, "tsmiPOPNB");
        this.tsmiPOPNB.addActionListener(this::tsmiPOPNB_Click);
        //
        // tsmiPOPM
        //
        this.tsmiPOPM.setName("tsmiPOPM");
        //resources.ApplyResources(this.tsmiPOPM, "tsmiPOPM");
        this.tsmiPOPM.addActionListener(this::tsmiPOPM_Click);
        //
        // tsmiPOPX
        //
        this.tsmiPOPX.setName("tsmiPOPX");
        //resources.ApplyResources(this.tsmiPOPX, "tsmiPOPX");
        this.tsmiPOPX.addActionListener(this::tsmiPOPX_Click);
        //
        // tsmiYMZ280B
        //
        this.tsmiYMZ280B.setName("tsmiYMZ280B");
        //resources.ApplyResources(this.tsmiYMZ280B, "tsmiYMZ280B");
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
        //resources.ApplyResources(this.tsmiCPPCM, "tsmiCPPCM");
        //
        // tsmiPC140
        //
        this.tsmiPC140.setName("tsmiPC140");
        //resources.ApplyResources(this.tsmiPC140, "tsmiPC140");
        this.tsmiPC140.addActionListener(this::tsmiPC140_Click);
        //
        // tsmiPC352
        //
        this.tsmiPC352.setName("tsmiPC352");
        //resources.ApplyResources(this.tsmiPC352, "tsmiPC352");
        this.tsmiPC352.addActionListener(this::tsmiPC352_Click);
        //
        // tsmiPOKIM6258
        //
        this.tsmiPOKIM6258.setName("tsmiPOKIM6258");
        //resources.ApplyResources(this.tsmiPOKIM6258, "tsmiPOKIM6258");
        this.tsmiPOKIM6258.addActionListener(this::tsmiPOKIM6258_Click);
        //
        // tsmiPOKIM6295
        //
        this.tsmiPOKIM6295.setName("tsmiPOKIM6295");
        //resources.ApplyResources(this.tsmiPOKIM6295, "tsmiPOKIM6295");
        this.tsmiPOKIM6295.addActionListener(this::tsmiPOKIM6295_Click);
        //
        // tsmiPPWM
        //
        this.tsmiPPWM.setName("tsmiPPWM");
        //resources.ApplyResources(this.tsmiPPWM, "tsmiPPWM");
        this.tsmiPPWM.addActionListener(this::tsmiPPWM_Click);
        //
        // tsmiPQSound
        //
        this.tsmiPQSound.setName("tsmiPQSound");
        //resources.ApplyResources(this.tsmiPQSound, "tsmiPQSound");
        this.tsmiPQSound.addActionListener(this::tsmiPQSound_Click);
        //
        // tsmiPRF5C164
        //
        this.tsmiPRF5C164.setName("tsmiPRF5C164");
        //resources.ApplyResources(this.tsmiPRF5C164, "tsmiPRF5C164");
        this.tsmiPRF5C164.addActionListener(this::tsmiPRF5C164_Click);
        //
        // tsmiPRF5C68
        //
        this.tsmiPRF5C68.setName("tsmiPRF5C68");
        //resources.ApplyResources(this.tsmiPRF5C68, "tsmiPRF5C68");
        this.tsmiPRF5C68.addActionListener(this::tsmiPRF5C68_Click);
        //
        // tsmiPMultiPCM
        //
        this.tsmiPMultiPCM.setName("tsmiPMultiPCM");
        //resources.ApplyResources(this.tsmiPMultiPCM, "tsmiPMultiPCM");
        this.tsmiPMultiPCM.addActionListener(this::tsmiPMultiPCM_Click);
        //
        // tsmiPPPZ8
        //
        this.tsmiPPPZ8.setName("tsmiPPPZ8");
        //resources.ApplyResources(this.tsmiPPPZ8, "tsmiPPPZ8");
        this.tsmiPPPZ8.addActionListener(this::tsmiPPPZ8_Click);
        //
        // tsmiPSegaPCM
        //
        this.tsmiPSegaPCM.setName("tsmiPSegaPCM");
        //resources.ApplyResources(this.tsmiPSegaPCM, "tsmiPSegaPCM");
        this.tsmiPSegaPCM.addActionListener(this::tsmiPSegaPCM_Click);
        //
        // tsmiPMIDI
        //
        this.tsmiPMIDI.setName("tsmiPMIDI");
        //resources.ApplyResources(this.tsmiPMIDI, "tsmiPMIDI");
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
        //resources.ApplyResources(this.tsmiCPNES, "tsmiCPNES");
        //
        // tsmiPNESDMC
        //
        this.tsmiPNESDMC.setName("tsmiPNESDMC");
        //resources.ApplyResources(this.tsmiPNESDMC, "tsmiPNESDMC");
        this.tsmiPNESDMC.addActionListener(this::tsmiPNESDMC_Click);
        //
        // tsmiPFDS
        //
        this.tsmiPFDS.setName("tsmiPFDS");
        //resources.ApplyResources(this.tsmiPFDS, "tsmiPFDS");
        this.tsmiPFDS.addActionListener(this::tsmiPFDS_Click);
        //
        // tsmiPMMC5
        //
        this.tsmiPMMC5.setName("tsmiPMMC5");
        //resources.ApplyResources(this.tsmiPMMC5, "tsmiPMMC5");
        this.tsmiPMMC5.addActionListener(this::tsmiPMMC5_Click);
        //
        // tsmiPVRC6
        //
        this.tsmiPVRC6.setName("tsmiPVRC6");
        //resources.ApplyResources(this.tsmiPVRC6, "tsmiPVRC6");
        this.tsmiPVRC6.addActionListener(this::tsmiPVRC6_Click);
        //
        // tsmiPVRC7
        //
        this.tsmiPVRC7.setName("tsmiPVRC7");
        //resources.ApplyResources(this.tsmiPVRC7, "tsmiPVRC7");
        this.tsmiPVRC7.addActionListener(this::tsmiPVRC7_Click);
        //
        // tsmiPN106
        //
        this.tsmiPN106.setName("tsmiPN106");
        //resources.ApplyResources(this.tsmiPN106, "tsmiPN106");
        this.tsmiPN106.addActionListener(this::tsmiPN106_Click);
        //
        // tsmiPS5B
        //
        this.tsmiPS5B.setName("tsmiPS5B");
        //resources.ApplyResources(this.tsmiPS5B, "tsmiPS5B");
        this.tsmiPS5B.addActionListener(this::tsmiPS5B_Click);
        //
        // tsmiPDMG
        //
        this.tsmiPDMG.setName("tsmiPDMG");
        //resources.ApplyResources(this.tsmiPDMG, "tsmiPDMG");
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
        //resources.ApplyResources(this.sencondryToolStripMenuItem, "sencondryToolStripMenuItem");
        //
        // tsmiCSPSG
        //
        this.tsmiCSPSG.add(this.tsmiSAY8910);
        this.tsmiCSPSG.add(this.tsmiSDCSG);
        this.tsmiCSPSG.setName("tsmiCSPSG");
        //resources.ApplyResources(this.tsmiCSPSG, "tsmiCSPSG");
        //
        // tsmiSAY8910
        //
        this.tsmiSAY8910.setName("tsmiSAY8910");
        //resources.ApplyResources(this.tsmiSAY8910, "tsmiSAY8910");
        this.tsmiSAY8910.addActionListener(this::tsmiSAY8910_Click);
        //
        // tsmiSDCSG
        //
        this.tsmiSDCSG.setName("tsmiSDCSG");
        //resources.ApplyResources(this.tsmiSDCSG, "tsmiSDCSG");
        this.tsmiSDCSG.addActionListener(this::tsmiSDCSG_Click);
        //
        // tsmiCSWF
        //
        this.tsmiCSWF.add(this.tsmiSHuC6280);
        this.tsmiCSWF.add(this.tsmiSK051649);
        this.tsmiCSWF.setName("tsmiCSWF");
        //resources.ApplyResources(this.tsmiCSWF, "tsmiCSWF");
        //
        // tsmiSHuC6280
        //
        this.tsmiSHuC6280.setName("tsmiSHuC6280");
        //resources.ApplyResources(this.tsmiSHuC6280, "tsmiSHuC6280");
        this.tsmiSHuC6280.addActionListener(this::tsmiSHuC6280_Click);
        //
        // tsmiSK051649
        //
        this.tsmiSK051649.setName("tsmiSK051649");
        //resources.ApplyResources(this.tsmiSK051649, "tsmiSK051649");
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
        //resources.ApplyResources(this.tsmiCSOPL, "tsmiCSOPL");
        //
        // tsmiSOPLL
        //
        this.tsmiSOPLL.setName("tsmiSOPLL");
        //resources.ApplyResources(this.tsmiSOPLL, "tsmiSOPLL");
        this.tsmiSOPLL.addActionListener(this::tsmiSOPLL_Click);
        //
        // tsmiSOPL
        //
        this.tsmiSOPL.setName("tsmiSOPL");
        //resources.ApplyResources(this.tsmiSOPL, "tsmiSOPL");
        this.tsmiSOPL.addActionListener(this::tsmiSOPL_Click);
        //
        // tsmiSY8950
        //
        this.tsmiSY8950.setName("tsmiSY8950");
        //resources.ApplyResources(this.tsmiSY8950, "tsmiSY8950");
        this.tsmiSY8950.addActionListener(this::tsmiSY8950_Click);
        //
        // tsmiSOPL2
        //
        this.tsmiSOPL2.setName("tsmiSOPL2");
        //resources.ApplyResources(this.tsmiSOPL2, "tsmiSOPL2");
        this.tsmiSOPL2.addActionListener(this::tsmiSOPL2_Click);
        //
        // tsmiSOPL3
        //
        this.tsmiSOPL3.setName("tsmiSOPL3");
        //resources.ApplyResources(this.tsmiSOPL3, "tsmiSOPL3");
        this.tsmiSOPL3.addActionListener(this::tsmiSOPL3_Click);
        //
        // tsmiSOPL4
        //
        this.tsmiSOPL4.setName("tsmiSOPL4");
        //resources.ApplyResources(this.tsmiSOPL4, "tsmiSOPL4");
        this.tsmiSOPL4.addActionListener(this::tsmiSOPL4_Click);
        //
        // tsmiCSOPN
        //
        this.tsmiCSOPN.add(this.tsmiSOPN);
        this.tsmiCSOPN.add(this.tsmiSOPN2);
        this.tsmiCSOPN.add(this.tsmiSOPNA);
        this.tsmiCSOPN.add(this.tsmiSOPNB);
        this.tsmiCSOPN.setName("tsmiCSOPN");
        //resources.ApplyResources(this.tsmiCSOPN, "tsmiCSOPN");
        //
        // tsmiSOPN
        //
        this.tsmiSOPN.setName("tsmiSOPN");
        //resources.ApplyResources(this.tsmiSOPN, "tsmiSOPN");
        this.tsmiSOPN.addActionListener(this::tsmiSOPN_Click);
        //
        // tsmiSOPN2
        //
        this.tsmiSOPN2.setName("tsmiSOPN2");
        //resources.ApplyResources(this.tsmiSOPN2, "tsmiSOPN2");
        this.tsmiSOPN2.addActionListener(this::tsmiSOPN2_Click);
        //
        // tsmiSOPNA
        //
        this.tsmiSOPNA.setName("tsmiSOPNA");
        //resources.ApplyResources(this.tsmiSOPNA, "tsmiSOPNA");
        this.tsmiSOPNA.addActionListener(this::tsmiSOPNA_Click);
        //
        // tsmiSOPNB
        //
        this.tsmiSOPNB.setName("tsmiSOPNB");
        //resources.ApplyResources(this.tsmiSOPNB, "tsmiSOPNB");
        this.tsmiSOPNB.addActionListener(this::tsmiSOPNB_Click);
        //
        // tsmiSOPM
        //
        this.tsmiSOPM.setName("tsmiSOPM");
        //resources.ApplyResources(this.tsmiSOPM, "tsmiSOPM");
        this.tsmiSOPM.addActionListener(this::tsmiSOPM_Click);
        //
        // tsmiSOPX
        //
        this.tsmiSOPX.setName("tsmiSOPX");
        //resources.ApplyResources(this.tsmiSOPX, "tsmiSOPX");
        this.tsmiSOPX.addActionListener(this::tsmiSOPX_Click);
        //
        // tsmiSYMZ280B
        //
        this.tsmiSYMZ280B.setName("tsmiSYMZ280B");
        //resources.ApplyResources(this.tsmiSYMZ280B, "tsmiSYMZ280B");
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
        //resources.ApplyResources(this.tsmiCSPCM, "tsmiCSPCM");
        //
        // tsmiSC140
        //
        this.tsmiSC140.setName("tsmiSC140");
        //resources.ApplyResources(this.tsmiSC140, "tsmiSC140");
        this.tsmiSC140.addActionListener(this::tsmiSC140_Click);
        //
        // tsmiSC352
        //
        this.tsmiSC352.setName("tsmiSC352");
        //resources.ApplyResources(this.tsmiSC352, "tsmiSC352");
        this.tsmiSC352.addActionListener(this::tsmiSC352_Click);
        //
        // tsmiSOKIM6258
        //
        this.tsmiSOKIM6258.setName("tsmiSOKIM6258");
        //resources.ApplyResources(this.tsmiSOKIM6258, "tsmiSOKIM6258");
        this.tsmiSOKIM6258.addActionListener(this::tsmiSOKIM6258_Click);
        //
        // tsmiSOKIM6295
        //
        this.tsmiSOKIM6295.setName("tsmiSOKIM6295");
        //resources.ApplyResources(this.tsmiSOKIM6295, "tsmiSOKIM6295");
        this.tsmiSOKIM6295.addActionListener(this::tsmiSOKIM6295_Click);
        //
        // tsmiSPWM
        //
        this.tsmiSPWM.setName("tsmiSPWM");
        //resources.ApplyResources(this.tsmiSPWM, "tsmiSPWM");
        this.tsmiSPWM.addActionListener(this::tsmiSPWM_Click);
        //
        // tsmiSRF5C164
        //
        this.tsmiSRF5C164.setName("tsmiSRF5C164");
        //resources.ApplyResources(this.tsmiSRF5C164, "tsmiSRF5C164");
        this.tsmiSRF5C164.addActionListener(this::tsmiSRF5C164_Click);
        //
        // tsmiSRF5C68
        //
        this.tsmiSRF5C68.setName("tsmiSRF5C68");
        //resources.ApplyResources(this.tsmiSRF5C68, "tsmiSRF5C68");
        this.tsmiSRF5C68.addActionListener(this::tsmiSRF5C68_Click);
        //
        // tsmiSSegaPCM
        //
        this.tsmiSSegaPCM.setName("tsmiSSegaPCM");
        //resources.ApplyResources(this.tsmiSSegaPCM, "tsmiSSegaPCM");
        this.tsmiSSegaPCM.addActionListener(this::tsmiSSegaPCM_Click);
        //
        // tsmiSMultiPCM
        //
        this.tsmiSMultiPCM.setName("tsmiSMultiPCM");
        //resources.ApplyResources(this.tsmiSMultiPCM, "tsmiSMultiPCM");
        this.tsmiSMultiPCM.addActionListener(this::tsmiSMultiPCM_Click);
        //
        // tsmiSPPZ8
        //
        this.tsmiSPPZ8.setName("tsmiSPPZ8");
        //resources.ApplyResources(this.tsmiSPPZ8, "tsmiSPPZ8");
        this.tsmiSPPZ8.addActionListener(this::tsmiSPPZ8_Click);
        //
        // tsmiSMIDI
        //
        this.tsmiSMIDI.setName("tsmiSMIDI");
        //resources.ApplyResources(this.tsmiSMIDI, "tsmiSMIDI");
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
        //resources.ApplyResources(this.tsmiCSNES, "tsmiCSNES");
        //
        // tsmiSFDS
        //
        this.tsmiSFDS.setName("tsmiSFDS");
        //resources.ApplyResources(this.tsmiSFDS, "tsmiSFDS");
        this.tsmiSFDS.addActionListener(this::tsmiSFDS_Click);
        //
        // tsmiSMMC5
        //
        this.tsmiSMMC5.setName("tsmiSMMC5");
        //resources.ApplyResources(this.tsmiSMMC5, "tsmiSMMC5");
        this.tsmiSMMC5.addActionListener(this::tsmiSMMC5_Click);
        //
        // tsmiSNESDMC
        //
        this.tsmiSNESDMC.setName("tsmiSNESDMC");
        //resources.ApplyResources(this.tsmiSNESDMC, "tsmiSNESDMC");
        this.tsmiSNESDMC.addActionListener(this::tsmiSNESDMC_Click);
        //
        // tsmiSVRC6
        //
        this.tsmiSVRC6.setName("tsmiSVRC6");
        //resources.ApplyResources(this.tsmiSVRC6, "tsmiSVRC6");
        this.tsmiSVRC6.addActionListener(this::tsmiSVRC6_Click);
        //
        // tsmiSVRC7
        //
        this.tsmiSVRC7.setName("tsmiSVRC7");
        //resources.ApplyResources(this.tsmiSVRC7, "tsmiSVRC7");
        this.tsmiSVRC7.addActionListener(this::tsmiSVRC7_Click);
        //
        // tsmiSN106
        //
        this.tsmiSN106.setName("tsmiSN106");
        //resources.ApplyResources(this.tsmiSN106, "tsmiSN106");
        this.tsmiSN106.addActionListener(this::tsmiSN106_Click);
        //
        // tsmiSS5B
        //
        this.tsmiSS5B.setName("tsmiSS5B");
        //resources.ApplyResources(this.tsmiSS5B, "tsmiSS5B");
        this.tsmiSS5B.addActionListener(this::tsmiSS5B_Click);
        //
        // tsmiSDMG
        //
        this.tsmiSDMG.setName("tsmiSDMG");
        //resources.ApplyResources(this.tsmiSDMG, "tsmiSDMG");
        this.tsmiSDMG.addActionListener(this::tsmiSDMG_Click);
        //
        // cmsMenu
        //
//        this.cmsMenu.ImageScalingSize(new Dimension(20, 20));
        this.cmsMenu.add(this.ファイルToolStripMenuItem);
        this.cmsMenu.add(this.操作ToolStripMenuItem);
        this.cmsMenu.add(this.tsmiOption);
        this.cmsMenu.add(this.tsmiPlayList);
        this.cmsMenu.add(this.tsmiOpenInfo);
        this.cmsMenu.add(this.tsmiOpenMixer);
        this.cmsMenu.add(this.その他ウィンドウ表示ToolStripMenuItem);
        this.cmsMenu.add(this.tsmiChangeZoom);
        this.cmsMenu.add(this.レジスタダンプ表示ToolStripMenuItem);
        this.cmsMenu.add(this.tsmiVisualizer);
        this.cmsMenu.setName("contextMenuStrip1");
        //resources.ApplyResources(this.cmsMenu, "cmsMenu");
        //
        // ファイルToolStripMenuItem
        //
        this.ファイルToolStripMenuItem.add(this.tsmiOpenFile);
        this.ファイルToolStripMenuItem.add(this.tsmiExit);
        this.ファイルToolStripMenuItem.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcOpenFolder()));
        this.ファイルToolStripMenuItem.setName("ファイルToolStripMenuItem");
        //resources.ApplyResources(this.ファイルToolStripMenuItem, "ファイルToolStripMenuItem");
        //
        // tsmiOpenFile
        //
        this.tsmiOpenFile.setName("tsmiOpenFile");
        //resources.ApplyResources(this.tsmiOpenFile, "tsmiOpenFile");
        this.tsmiOpenFile.addActionListener(this::tsmiOpenFile_Click);
        //
        // tsmiExit
        //
        this.tsmiExit.setName("tsmiExit");
        //resources.ApplyResources(this.tsmiExit, "tsmiExit");
        this.tsmiExit.addActionListener(this::tsmiExit_Click);
        //
        // 操作ToolStripMenuItem
        //
        this.操作ToolStripMenuItem.add(this.tsmiPlay);
        this.操作ToolStripMenuItem.add(this.tsmiStop);
        this.操作ToolStripMenuItem.add(this.tsmiPause);
        this.操作ToolStripMenuItem.add(this.tsmiFadeOut);
        this.操作ToolStripMenuItem.add(this.tsmiSlow);
        this.操作ToolStripMenuItem.add(this.tsmiFf);
        this.操作ToolStripMenuItem.add(this.tsmiNext);
        this.操作ToolStripMenuItem.add(this.tsmiPlayMode);
        this.操作ToolStripMenuItem.setName("操作ToolStripMenuItem");
        //resources.ApplyResources(this.操作ToolStripMenuItem, "操作ToolStripMenuItem");
        //
        // tsmiPlay
        //
        this.tsmiPlay.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcPlay()));
        this.tsmiPlay.setName("tsmiPlay");
        //resources.ApplyResources(this.tsmiPlay, "tsmiPlay");
        this.tsmiPlay.addActionListener(this::tsmiPlay_Click);
        //
        // tsmiStop
        //
        this.tsmiStop.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcStop()));
        this.tsmiStop.setName("tsmiStop");
        //resources.ApplyResources(this.tsmiStop, "tsmiStop");
        this.tsmiStop.addActionListener(this::tsmiStop_Click);
        //
        // tsmiPause
        //
        this.tsmiPause.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcPause()));
        this.tsmiPause.setName("tsmiPause");
        //resources.ApplyResources(this.tsmiPause, "tsmiPause");
        this.tsmiPause.addActionListener(this::tsmiPause_Click);
        //
        // tsmiFadeOut
        //
        this.tsmiFadeOut.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        this.tsmiFadeOut.setName("tsmiFadeOut");
        //resources.ApplyResources(this.tsmiFadeOut, "tsmiFadeOut");
        this.tsmiFadeOut.addActionListener(this::tsmiFadeOut_Click);
        //
        // tsmiSlow
        //
        this.tsmiSlow.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcSlow()));
        this.tsmiSlow.setName("tsmiSlow");
        //resources.ApplyResources(this.tsmiSlow, "tsmiSlow");
        this.tsmiSlow.addActionListener(this::tsmiSlow_Click);
        //
        // tsmiFf
        //
        this.tsmiFf.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFast()));
        this.tsmiFf.setName("tsmiFf");
        //resources.ApplyResources(this.tsmiFf, "tsmiFf");
        this.tsmiFf.addActionListener(this::tsmiFf_Click);
        //
        // tsmiNext
        //
        this.tsmiNext.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcNext()));
        this.tsmiNext.setName("tsmiNext");
        //resources.ApplyResources(this.tsmiNext, "tsmiNext");
        this.tsmiNext.addActionListener(this::tsmiNext_Click);
        //
        // tsmiPlayMode
        //
        this.tsmiPlayMode.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcStep()));
        this.tsmiPlayMode.setName("tsmiPlayMode");
        //resources.ApplyResources(this.tsmiPlayMode, "tsmiPlayMode");
        this.tsmiPlayMode.addActionListener(this::tsmiPlayMode_Click);
        //
        // tsmiOption
        //
        this.tsmiOption.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcSetting()));
        this.tsmiOption.setName("tsmiOption");
        //resources.ApplyResources(this.tsmiOption, "tsmiOption");
        this.tsmiOption.addActionListener(this::tsmiOption_Click);
        //
        // tsmiPlayList
        //
        this.tsmiPlayList.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcPlayList()));
        this.tsmiPlayList.setName("tsmiPlayList");
        //resources.ApplyResources(this.tsmiPlayList, "tsmiPlayList");
        this.tsmiPlayList.addActionListener(this::tsmiPlayList_Click);
        //
        // tsmiOpenInfo
        //
        this.tsmiOpenInfo.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcInformation()));
        this.tsmiOpenInfo.setName("tsmiOpenInfo");
        //resources.ApplyResources(this.tsmiOpenInfo, "tsmiOpenInfo");
        this.tsmiOpenInfo.addActionListener(this::tsmiOpenInfo_Click);
        //
        // tsmiOpenMixer
        //
        this.tsmiOpenMixer.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcMixer()));
        this.tsmiOpenMixer.setName("tsmiOpenMixer");
        //resources.ApplyResources(this.tsmiOpenMixer, "tsmiOpenMixer");
        this.tsmiOpenMixer.addActionListener(this::tsmiOpenMixer_Click);
        //
        // その他ウィンドウ表示ToolStripMenuItem
        //
        this.その他ウィンドウ表示ToolStripMenuItem.add(this.tsmiKBrd);
        this.その他ウィンドウ表示ToolStripMenuItem.add(this.tsmiVST);
        this.その他ウィンドウ表示ToolStripMenuItem.add(this.tsmiMIDIkbd);
        this.その他ウィンドウ表示ToolStripMenuItem.setName("その他ウィンドウ表示ToolStripMenuItem");
        //resources.ApplyResources(this.その他ウィンドウ表示ToolStripMenuItem, "その他ウィンドウ表示ToolStripMenuItem");
        //
        // tsmiKBrd
        //
        this.tsmiKBrd.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcKBD()));
        this.tsmiKBrd.setName("tsmiKBrd");
        //resources.ApplyResources(this.tsmiKBrd, "tsmiKBrd");
        this.tsmiKBrd.addActionListener(this::tsmiKBrd_Click);
        //
        // tsmiVST
        //
        this.tsmiVST.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcVST()));
        this.tsmiVST.setName("tsmiVST");
        //resources.ApplyResources(this.tsmiVST, "tsmiVST");
        this.tsmiVST.addActionListener(this::tsmiVST_Click);
        //
        // tsmiMIDIkbd
        //
        this.tsmiMIDIkbd.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcMIDIKBD()));
        this.tsmiMIDIkbd.setName("tsmiMIDIkbd");
        //resources.ApplyResources(this.tsmiMIDIkbd, "tsmiMIDIkbd");
        this.tsmiMIDIkbd.addActionListener(this::tsmiMIDIkbd_Click);
        //
        // tsmiChangeZoom
        //
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX1);
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX2);
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX3);
        this.tsmiChangeZoom.add(this.tsmiChangeZoomX4);
        this.tsmiChangeZoom.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcZoom()));
        this.tsmiChangeZoom.setName("tsmiChangeZoom");
        //resources.ApplyResources(this.tsmiChangeZoom, "tsmiChangeZoom");
        this.tsmiChangeZoom.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX1
        //
        this.tsmiChangeZoomX1.setName("tsmiChangeZoomX1");
        //resources.ApplyResources(this.tsmiChangeZoomX1, "tsmiChangeZoomX1");
        this.tsmiChangeZoomX1.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX2
        //
        this.tsmiChangeZoomX2.setName("tsmiChangeZoomX2");
        //resources.ApplyResources(this.tsmiChangeZoomX2, "tsmiChangeZoomX2");
        this.tsmiChangeZoomX2.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX3
        //
        this.tsmiChangeZoomX3.setName("tsmiChangeZoomX3");
        //resources.ApplyResources(this.tsmiChangeZoomX3, "tsmiChangeZoomX3");
        this.tsmiChangeZoomX3.addActionListener(this::tsmiChangeZoom_Click);
        //
        // tsmiChangeZoomX4
        //
        this.tsmiChangeZoomX4.setName("tsmiChangeZoomX4");
        //resources.ApplyResources(this.tsmiChangeZoomX4, "tsmiChangeZoomX4");
        this.tsmiChangeZoomX4.addActionListener(this::tsmiChangeZoom_Click);
        //
        // レジスタダンプ表示ToolStripMenuItem
        //
        this.レジスタダンプ表示ToolStripMenuItem.add(this.yM2612ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.ym2151ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.ym2203ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.ym2413ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.ym2608ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.yM2610ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.yM3812ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.yMF262ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.yMF278BToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.yMZ280BToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.c140ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.c352ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.qSoundToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.segaPCMToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.sN76489ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.aY8910ToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.add(this.sIDToolStripMenuItem);
        this.レジスタダンプ表示ToolStripMenuItem.setName("レジスタダンプ表示ToolStripMenuItem");
//        //resources.ApplyResources(this.レジスタダンプ表示ToolStripMenuItem, "レジスタダンプ表示ToolStripMenuItem");
        //
        // yM2612ToolStripMenuItem
        //
        this.yM2612ToolStripMenuItem.setName("yM2612ToolStripMenuItem");
//        //resources.ApplyResources(this.yM2612ToolStripMenuItem, "yM2612ToolStripMenuItem");
        this.yM2612ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2151ToolStripMenuItem
        //
        this.ym2151ToolStripMenuItem.setName("ym2151ToolStripMenuItem");
//        //resources.ApplyResources(this.ym2151ToolStripMenuItem, "ym2151ToolStripMenuItem");
        this.ym2151ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2203ToolStripMenuItem
        //
        this.ym2203ToolStripMenuItem.setName("ym2203ToolStripMenuItem");
//        //resources.ApplyResources(this.ym2203ToolStripMenuItem, "ym2203ToolStripMenuItem");
        this.ym2203ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2413ToolStripMenuItem
        //
        this.ym2413ToolStripMenuItem.setName("ym2413ToolStripMenuItem");
//        //resources.ApplyResources(this.ym2413ToolStripMenuItem, "ym2413ToolStripMenuItem");
        this.ym2413ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // ym2608ToolStripMenuItem
        //
        this.ym2608ToolStripMenuItem.setName("ym2608ToolStripMenuItem");
//        //resources.ApplyResources(this.ym2608ToolStripMenuItem, "ym2608ToolStripMenuItem");
        this.ym2608ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yM2610ToolStripMenuItem
        //
        this.yM2610ToolStripMenuItem.setName("yM2610ToolStripMenuItem");
//        //resources.ApplyResources(this.yM2610ToolStripMenuItem, "yM2610ToolStripMenuItem");
        this.yM2610ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yM3812ToolStripMenuItem
        //
        this.yM3812ToolStripMenuItem.setName("yM3812ToolStripMenuItem");
//        //resources.ApplyResources(this.yM3812ToolStripMenuItem, "yM3812ToolStripMenuItem");
        this.yM3812ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yMF262ToolStripMenuItem
        //
        this.yMF262ToolStripMenuItem.setName("yMF262ToolStripMenuItem");
        //resources.ApplyResources(this.yMF262ToolStripMenuItem, "yMF262ToolStripMenuItem");
        this.yMF262ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yMF278BToolStripMenuItem
        //
        this.yMF278BToolStripMenuItem.setName("yMF278BToolStripMenuItem");
        //resources.ApplyResources(this.yMF278BToolStripMenuItem, "yMF278BToolStripMenuItem");
        this.yMF278BToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // yMZ280BToolStripMenuItem
        //
        this.yMZ280BToolStripMenuItem.setName("yMZ280BToolStripMenuItem");
        //resources.ApplyResources(this.yMZ280BToolStripMenuItem, "yMZ280BToolStripMenuItem");
        this.yMZ280BToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // c140ToolStripMenuItem
        //
        this.c140ToolStripMenuItem.setName("c140ToolStripMenuItem");
        //resources.ApplyResources(this.c140ToolStripMenuItem, "c140ToolStripMenuItem");
        this.c140ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // c352ToolStripMenuItem
        //
        this.c352ToolStripMenuItem.setName("c352ToolStripMenuItem");
        //resources.ApplyResources(this.c352ToolStripMenuItem, "c352ToolStripMenuItem");
        this.c352ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // qSoundToolStripMenuItem
        //
        this.qSoundToolStripMenuItem.setName("qSoundToolStripMenuItem");
        //resources.ApplyResources(this.qSoundToolStripMenuItem, "qSoundToolStripMenuItem");
        this.qSoundToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // segaPCMToolStripMenuItem
        //
        this.segaPCMToolStripMenuItem.setName("segaPCMToolStripMenuItem");
        //resources.ApplyResources(this.segaPCMToolStripMenuItem, "segaPCMToolStripMenuItem");
        this.segaPCMToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // sN76489ToolStripMenuItem
        //
        this.sN76489ToolStripMenuItem.setName("sN76489ToolStripMenuItem");
        //resources.ApplyResources(this.sN76489ToolStripMenuItem, "sN76489ToolStripMenuItem");
        this.sN76489ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // aY8910ToolStripMenuItem
        //
        this.aY8910ToolStripMenuItem.setName("aY8910ToolStripMenuItem");
        //resources.ApplyResources(this.aY8910ToolStripMenuItem, "aY8910ToolStripMenuItem");
        this.aY8910ToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // sIDToolStripMenuItem
        //
        this.sIDToolStripMenuItem.setName("sIDToolStripMenuItem");
        //resources.ApplyResources(this.sIDToolStripMenuItem, "sIDToolStripMenuItem");
        this.sIDToolStripMenuItem.addActionListener(this::RegisterDumpMenuItem_Click);
        //
        // tsmiVisualizer
        //
        this.tsmiVisualizer.setName("tsmiVisualizer");
        //resources.ApplyResources(this.tsmiVisualizer, "tsmiVisualizer");
        this.tsmiVisualizer.addActionListener(this::tsmiVisWave_Click);
        //
        // opeButtonSetting
        //
//        this.opeButtonSetting.AllowDrop = true;
        new DropTarget(this.opeButtonSetting, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonSetting.setBackground(Color.black);
        this.opeButtonSetting.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonSetting, "opeButtonSetting");
//            //this.opeButtonSetting.FlatAppearance.BorderColor = Color.black;
//            //this.opeButtonSetting.FlatAppearance.Borde.setPreferredSize(0);
//            //this.opeButtonSetting.FlatAppearance.MouseDow.setBackground(Color.black);
//            //this.opeButtonSetting.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonSetting.setName("opeButtonSetting");
        this.opeButtonSetting.setActionCommand("0");
        this.opeButtonSetting.setToolTipText(rb.getString("opeButtonSetting.ToolTip"));
        // this.opeButtonSetting.UseVisualStyl.setBackground(false);
        this.opeButtonSetting.addActionListener(this::opeButtonSetting_Click);
//        this.opeButtonSetting.addDragAndDropListenr(this::pbScreen_DragDrop);
//        this.opeButtonSetting.addDragAndDropListenr(this::pbScreen_DragEnter);
        this.opeButtonSetting.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonStop
        //
//        this.opeButtonStop.AllowDrop = true;
        new DropTarget(this.opeButtonStop, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonStop.setBackground(Color.black);
        this.opeButtonStop.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonStop, "opeButtonStop");
//            //this.opeButtonStop.FlatAppearance.BorderColor = Color.black;
//            //this.opeButtonStop.FlatAppearance.Borde.setPreferredSize(0);
//            //this.opeButtonStop.FlatAppearance.MouseDow.setBackground(Color.black);
//            //this.opeButtonStop.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonStop.setName("opeButtonStop");
        this.opeButtonStop.setActionCommand("1");
//        this.opeButtonStop.setToolTipText(Resources.getResourceManager().getString("opeButtonStop.ToolTip"));
        // this.opeButtonStop.UseVisualStyl.setBackground(false);
        this.opeButtonStop.addActionListener(this::opeButtonStop_Click);
//        this.opeButtonStop.addDragAndDropListenr(this::pbScreen_DragDrop);
//        this.opeButtonStop.addDragAndDropListenr(this::pbScreen_DragEnter);
        this.opeButtonStop.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPause
        //
//        this.opeButtonPause.AllowDrop = true;
        new DropTarget(this.opeButtonPause, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPause.setBackground(Color.black);
        this.opeButtonPause.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonPause, "opeButtonPause");
//            //this.opeButtonPause.FlatAppearance.BorderColor = Color.black;
//            //this.opeButtonPause.FlatAppearance.Borde.setPreferredSize(0);
//            //this.opeButtonPause.FlatAppearance.MouseDow.setBackground(Color.black);
//            //this.opeButtonPause.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonPause.setName("opeButtonPause");
        this.opeButtonPause.setActionCommand("2");
//        this.opeButtonPause.setToolTipText(Resources.getResourceManager().getString("opeButtonPause.ToolTip"));
        // this.opeButtonPause.UseVisualStyl.setBackground(false);
        this.opeButtonPause.addActionListener(this::opeButtonPause_Click);
//        this.opeButtonPause.addDragAndDropListenr(this::pbScreen_DragDrop);
//        this.opeButtonPause.addDragAndDropListenr(this::pbScreen_DragEnter);
        this.opeButtonPause.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonFadeout
        //
//        this.opeButtonFadeout.AllowDrop = true;
        new DropTarget(this.opeButtonFadeout, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonFadeout.setBackground(Color.black);
        this.opeButtonFadeout.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonFadeout, "opeButtonFadeout");
        //this.opeButtonFadeout.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonFadeout.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonFadeout.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonFadeout.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonFadeout.setName("opeButtonFadeout");
        this.opeButtonFadeout.setActionCommand("3");
//        this.opeButtonFadeout.setToolTipText(Resources.getResourceManager().getString("opeButtonFadeout.ToolTip"));
        // this.opeButtonFadeout.UseVisualStyl.setBackground(false);
        this.opeButtonFadeout.addActionListener(this::opeButtonFadeout_Click);
//        this.opeButtonFadeout.addDragAndDropListenr(this::pbScreen_DragDrop);
//        this.opeButtonFadeout.addDragAndDropListenr(this::pbScreen_DragEnter);
        this.opeButtonFadeout.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPrevious
        //
//        this.opeButtonPrevious.AllowDrop = true;
        new DropTarget(this.opeButtonPrevious, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPrevious.setBackground(Color.black);
        this.opeButtonPrevious.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonPrevious, "opeButtonPrevious");
        //this.opeButtonPrevious.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonPrevious.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonPrevious.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonPrevious.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonPrevious.setName("opeButtonPrevious");
        this.opeButtonPrevious.setActionCommand("4");
//        this.opeButtonPrevious.setToolTipText(Resources.getResourceManager().getString("opeButtonPrevious.ToolTip"));
        // this.opeButtonPrevious.UseVisualStyl.setBackground(false);
        this.opeButtonPrevious.addActionListener(this::opeButtonPrevious_Click);
//        this.opeButtonPrevious.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonPrevious.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonPrevious.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonSlow
        //
//        this.opeButtonSlow.AllowDrop = true;
        new DropTarget(this.opeButtonSlow, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonSlow.setBackground(Color.black);
        this.opeButtonSlow.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonSlow, "opeButtonSlow");
        //this.opeButtonSlow.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonSlow.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonSlow.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonSlow.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonSlow.setName("opeButtonSlow");
        this.opeButtonSlow.setActionCommand("5");
//        this.opeButtonSlow.setToolTipText(Resources.getResourceManager().getString("opeButtonSlow.ToolTip"));
        // this.opeButtonSlow.UseVisualStyl.setBackground(false);
        this.opeButtonSlow.addActionListener(this::opeButtonSlow_Click);
//        this.opeButtonSlow.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonSlow.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonSlow.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPlay
        //
//        this.opeButtonPlay.AllowDrop = true;
        new DropTarget(this.opeButtonPlay, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPlay.setBackground(Color.black);
        this.opeButtonPlay.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonPlay, "opeButtonPlay");
        //this.opeButtonPlay.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonPlay.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonPlay.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonPlay.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonPlay.setName("opeButtonPlay");
        this.opeButtonPlay.setActionCommand("6");
//        this.opeButtonPlay.setToolTipText(Resources.getResourceManager().getString("opeButtonPlay.ToolTip"));
        // this.opeButtonPlay.UseVisualStyl.setBackground(false);
        this.opeButtonPlay.addActionListener(this::opeButtonPlay_Click);
//        this.opeButtonPlay.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonPlay.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonPlay.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonFast
        //
//        this.opeButtonFast.AllowDrop = true;
        new DropTarget(this.opeButtonFast, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonFast.setBackground(Color.black);
        this.opeButtonFast.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonFast, "opeButtonFast");
        //this.opeButtonFast.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonFast.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonFast.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonFast.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonFast.setName("opeButtonFast");
        this.opeButtonFast.setActionCommand("7");
//        this.opeButtonFast.setToolTipText(Resources.getResourceManager().getString("opeButtonFast.ToolTip"));
        // this.opeButtonFast.UseVisualStyl.setBackground(false);
        this.opeButtonFast.addActionListener(this::opeButtonFast_Click);
//        this.opeButtonFast.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonFast.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonFast.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonNext
        //
//        this.opeButtonNext.AllowDrop = true;
        new DropTarget(this.opeButtonNext, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonNext.setBackground(Color.black);
        this.opeButtonNext.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonNext, "opeButtonNext");
        //this.opeButtonNext.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonNext.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonNext.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonNext.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonNext.setName("opeButtonNext");
        this.opeButtonNext.setActionCommand("8");
//        this.opeButtonNext.setToolTipText(Resources.getResourceManager().getString("opeButtonNext.ToolTip"));
        // this.opeButtonNext.UseVisualStyl.setBackground(false);
        this.opeButtonNext.addActionListener(this::opeButtonNext_Click);
//        this.opeButtonNext.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonNext.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonNext.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonZoom
        //
//        this.opeButtonZoom.AllowDrop = true;
        new DropTarget(this.opeButtonZoom, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonZoom.setBackground(Color.black);
        this.opeButtonZoom.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonZoom, "opeButtonZoom");
        //this.opeButtonZoom.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonZoom.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonZoom.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonZoom.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonZoom.setName("opeButtonZoom");
        this.opeButtonZoom.setActionCommand("17");
//        this.opeButtonZoom.setToolTipText(Resources.getResourceManager().getString("opeButtonZoom.ToolTip"));
        // this.opeButtonZoom.UseVisualStyl.setBackground(false);
        this.opeButtonZoom.addActionListener(this::opeButtonZoom_Click);
//        this.opeButtonZoom.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonZoom.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonZoom.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonMIDIKBD
        //
//        this.opeButtonMIDIKBD.AllowDrop = true;
        new DropTarget(this.opeButtonMIDIKBD, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonMIDIKBD.setBackground(Color.black);
        this.opeButtonMIDIKBD.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonMIDIKBD, "opeButtonMIDIKBD");
        //this.opeButtonMIDIKBD.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonMIDIKBD.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonMIDIKBD.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonMIDIKBD.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonMIDIKBD.setName("opeButtonMIDIKBD");
        this.opeButtonMIDIKBD.setActionCommand("16");
//        this.opeButtonMIDIKBD.setToolTipText(Resources.getResourceManager().getString("opeButtonMIDIKBD.ToolTip"));
        // this.opeButtonMIDIKBD.UseVisualStyl.setBackground(false);
        this.opeButtonMIDIKBD.addActionListener(this::opeButtonMIDIKBD_Click);
//        this.opeButtonMIDIKBD.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonMIDIKBD.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonMIDIKBD.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonVST
        //
//        this.opeButtonVST.AllowDrop = true;
        new DropTarget(this.opeButtonVST, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonVST.setBackground(Color.black);
        this.opeButtonVST.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonVST, "opeButtonVST");
        //this.opeButtonVST.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonVST.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonVST.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonVST.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonVST.setName("opeButtonVST");
        this.opeButtonVST.setActionCommand("15");
//        this.opeButtonVST.setToolTipText(Resources.getResourceManager().getString("opeButtonVST.ToolTip"));
        // this.opeButtonVST.UseVisualStyl.setBackground(false);
        this.opeButtonVST.addActionListener(this::opeButtonVST_Click);
//        this.opeButtonVST.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonVST.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonVST.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonKBD
        //
//        this.opeButtonKBD.AllowDrop = true;
        new DropTarget(this.opeButtonKBD, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonKBD.setBackground(Color.black);
        this.opeButtonKBD.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonKBD, "opeButtonKBD");
        //this.opeButtonKBD.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonKBD.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonKBD.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonKBD.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonKBD.setName("opeButtonKBD");
        this.opeButtonKBD.setActionCommand("14");
//        this.opeButtonKBD.setToolTipText(Resources.getResourceManager().getString("opeButtonKBD.ToolTip"));
        // this.opeButtonKBD.UseVisualStyl.setBackground(false);
        this.opeButtonKBD.addActionListener(this::opeButtonKBD_Click);
//        this.opeButtonKBD.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonKBD.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonKBD.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonMixer
        //
//            this.opeButtonMixer.AllowDrop = true;
        new DropTarget(this.opeButtonMixer, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonMixer.setBackground(Color.black);
        this.opeButtonMixer.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonMixer, "opeButtonMixer");
        //this.opeButtonMixer.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonMixer.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonMixer.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonMixer.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonMixer.setName("opeButtonMixer");
        this.opeButtonMixer.setActionCommand("13");
//        this.opeButtonMixer.setToolTipText(Resources.getResourceManager().getString("opeButtonMixer.ToolTip"));
        // this.opeButtonMixer.UseVisualStyl.setBackground(false);
        this.opeButtonMixer.addActionListener(this::opeButtonMixer_Click);
//        this.opeButtonMixer.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonMixer.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonMixer.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonInformation
        //
//            this.opeButtonInformation.AllowDrop = true;
        new DropTarget(this.opeButtonInformation, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonInformation.setBackground(Color.black);
        this.opeButtonInformation.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonInformation, "opeButtonInformation");
        //this.opeButtonInformation.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonInformation.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonInformation.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonInformation.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonInformation.setName("opeButtonInformation");
        this.opeButtonInformation.setActionCommand("12");
//        this.opeButtonInformation.setToolTipText(Resources.getResourceManager().getString("opeButtonInformation.ToolTip"));
        // this.opeButtonInformation.UseVisualStyl.setBackground(false);
        this.opeButtonInformation.addActionListener(this::opeButtonInformation_Click);
//        this.opeButtonInformation.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonInformation.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonInformation.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonPlayList
        //
//        this.opeButtonPlayList.AllowDrop = true;
        new DropTarget(this.opeButtonPlayList, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonPlayList.setBackground(Color.black);
        this.opeButtonPlayList.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonPlayList, "opeButtonPlayList");
        //this.opeButtonPlayList.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonPlayList.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonPlayList.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonPlayList.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonPlayList.setName("opeButtonPlayList");
        this.opeButtonPlayList.setActionCommand("11");
//        this.opeButtonPlayList.setToolTipText(Resources.getResourceManager().getString("opeButtonPlayList.ToolTip"));
        // this.opeButtonPlayList.UseVisualStyl.setBackground(false);
        this.opeButtonPlayList.addActionListener(this::opeButtonPlayList_Click);
//        this.opeButtonPlayList.addDragDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonPlayList.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonPlayList.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonOpen
        //
//        this.opeButtonOpen.AllowDrop = true;
        new DropTarget(this.opeButtonOpen, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonOpen.setBackground(Color.black);
        this.opeButtonOpen.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonOpen, "opeButtonOpen");
        //this.opeButtonOpen.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonOpen.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonOpen.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonOpen.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonOpen.setName("opeButtonOpen");
        this.opeButtonOpen.setActionCommand("10");
//        this.opeButtonOpen.setToolTipText(Resources.getResourceManager().getString("opeButtonOpen.ToolTip"));
        // this.opeButtonOpen.UseVisualStyl.setBackground(false);
        this.opeButtonOpen.addActionListener(this::opeButtonOpen_Click);
//        this.opeButtonOpen.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonOpen.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonOpen.addMouseListener(this.opeButton_Mouse);
        //
        // opeButtonMode
        //
//        this.opeButtonMode.AllowDrop = true;
        new DropTarget(this.opeButtonMode, DnDConstants.ACTION_COPY_OR_MOVE, new Common.DTListener(this::pbScreen_DragDrop), true);
        this.opeButtonMode.setBackground(Color.black);
        this.opeButtonMode.setIcon(new ImageIcon(mdplayer.properties.Resources.getCcFadeout()));
        //resources.ApplyResources(this.opeButtonMode, "opeButtonMode");
        //this.opeButtonMode.FlatAppearance.BorderColor = Color.black;
        //this.opeButtonMode.FlatAppearance.Borde.setPreferredSize(0);
        //this.opeButtonMode.FlatAppearance.MouseDow.setBackground(Color.black);
        //this.opeButtonMode.FlatAppearance.MouseOve.setBackground(Color.black);
        this.opeButtonMode.setName("opeButtonMode");
        this.opeButtonMode.setActionCommand("9");
//        this.opeButtonMode.setToolTipText(Resources.getResourceManager().getString("opeButtonMode.ToolTip"));
        // this.opeButtonMode.UseVisualStyl.setBackground(false);
        this.opeButtonMode.addActionListener(this::opeButtonMode_Click);
//        this.opeButtonMode.addDragAndDropListenr(this.pbScreen_DragDrop);
//        this.opeButtonMode.addDragAndDropListenr(this.pbScreen_DragEnter);
        this.opeButtonMode.addMouseListener(this.opeButton_Mouse);
        //
        // keyboardHook1
        //
        this.keyboardHook1.addKeyboardHooked(this.keyboardHook1_KeyboardHooked);
        //
        // frmMain
        //
        //resources.ApplyResources(this, "$this");
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.getContentPane().add(this.opeButtonZoom);
        this.getContentPane().add(this.opeButtonMIDIKBD);
        this.getContentPane().add(this.opeButtonVST);
        this.getContentPane().add(this.opeButtonKBD);
        this.getContentPane().add(this.opeButtonMixer);
        this.getContentPane().add(this.opeButtonInformation);
        this.getContentPane().add(this.opeButtonPlayList);
        this.getContentPane().add(this.opeButtonOpen);
        this.getContentPane().add(this.opeButtonMode);
        this.getContentPane().add(this.opeButtonNext);
        this.getContentPane().add(this.opeButtonFast);
        this.getContentPane().add(this.opeButtonPlay);
        this.getContentPane().add(this.opeButtonSlow);
        this.getContentPane().add(this.opeButtonPrevious);
        this.getContentPane().add(this.opeButtonFadeout);
        this.getContentPane().add(this.opeButtonPause);
        this.getContentPane().add(this.opeButtonStop);
        this.getContentPane().add(this.opeButtonSetting);
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
//        this.MaximizeBox = false;
        this.setName("frmMain");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
        // this.cmsOpenOtherPanel.ResumeLayout(false);
        // this.cmsMenu.ResumeLayout(false);
//        this.ResumeLayout(false);
    }

    private JLabel pbScreen;
    private JPopupMenu cmsOpenOtherPanel;
    private JMenuItem primaryToolStripMenuItem;
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
    private JMenuItem ファイルToolStripMenuItem;
    private JMenuItem tsmiOpenFile;
    private JMenuItem tsmiExit;
    private JMenuItem 操作ToolStripMenuItem;
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
    private JMenuItem その他ウィンドウ表示ToolStripMenuItem;
    private JMenuItem tsmiKBrd;
    private JMenuItem tsmiVST;
    private JMenuItem tsmiMIDIkbd;
    private JMenuItem tsmiChangeZoom;
    private JMenuItem レジスタダンプ表示ToolStripMenuItem;
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
    private JMenuItem tsmiCPNES;
    private JMenuItem tsmiCPPCM;
    private JMenuItem tsmiCPOPN;
    private JMenuItem tsmiCPOPL;
    private JMenuItem tsmiCPPSG;
    private JMenuItem tsmiCPWF;
    private JMenuItem toolStripMenuItem2;
    private JMenuItem tsmiCSPSG;
    private JMenu tsmiCSWF;
    private JMenu tsmiCSOPL;
    private JMenuItem tsmiCSOPN;
    private JMenuItem tsmiCSPCM;
    private JMenuItem tsmiCSNES;
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

    private int[] getChipStatus() {
        int[] chips = new int[256];

        chips[0] = audio.audio.chipRegister.chipLED.get("PriOPN");
        audio.audio.chipRegister.chipLED.put("PriOPN", audio.audio.chipLED.get("PriOPN"));
        chips[1] = audio.audio.chipRegister.chipLED.get("PriOPN2");
        audio.audio.chipRegister.chipLED.put("PriOPN2", audio.audio.chipLED.get("PriOPN2"));
        chips[2] = audio.audio.chipRegister.chipLED.get("PriOPNA");
        audio.audio.chipRegister.chipLED.put("PriOPNA", audio.audio.chipLED.get("PriOPNA"));
        chips[3] = audio.audio.chipRegister.chipLED.get("PriOPNB");
        audio.audio.chipRegister.chipLED.put("PriOPNB", audio.audio.chipLED.get("PriOPNB"));

        chips[4] = audio.audio.chipRegister.chipLED.get("PriOPM");
        audio.audio.chipRegister.chipLED.put("PriOPM", audio.audio.chipLED.get("PriOPM"));
        chips[5] = audio.audio.chipRegister.chipLED.get("PriDCSG");
        audio.audio.chipRegister.chipLED.put("PriDCSG", audio.audio.chipLED.get("PriDCSG"));
        chips[6] = audio.audio.chipRegister.chipLED.get("PriRF5C");
        audio.audio.chipRegister.chipLED.put("PriRF5C", audio.audio.chipLED.get("PriRF5C"));
        chips[7] = audio.audio.chipRegister.chipLED.get("PriPWM");
        audio.audio.chipRegister.chipLED.put("PriPWM", audio.audio.chipLED.get("PriPWM"));

        chips[8] = audio.audio.chipRegister.chipLED.get("PriOKI5");
        audio.audio.chipRegister.chipLED.put("PriOKI5", audio.audio.chipLED.get("PriOKI5"));
        chips[9] = audio.audio.chipRegister.chipLED.get("PriOKI9");
        audio.audio.chipRegister.chipLED.put("PriOKI9", audio.audio.chipLED.get("PriOKI9"));
        chips[10] = audio.audio.chipRegister.chipLED.get("PriC140");
        audio.audio.chipRegister.chipLED.put("PriC140", audio.audio.chipLED.get("PriC140"));
        chips[11] = audio.audio.chipRegister.chipLED.get("PriSPCM");
        audio.audio.chipRegister.chipLED.put("PriSPCM", audio.audio.chipLED.get("PriSPCM"));

        chips[12] = audio.audio.chipRegister.chipLED.get("PriAY10");
        audio.audio.chipRegister.chipLED.put("PriAY10", audio.audio.chipLED.get("PriAY10"));
        chips[13] = audio.audio.chipRegister.chipLED.get("PriOPLL");
        audio.audio.chipRegister.chipLED.put("PriOPLL", audio.audio.chipLED.get("PriOPLL"));
        chips[14] = audio.audio.chipRegister.chipLED.get("PriHuC");
        audio.audio.chipRegister.chipLED.put("PriHuC", audio.audio.chipLED.get("PriHuC"));
        chips[15] = audio.audio.chipRegister.chipLED.get("PriC352");
        audio.audio.chipRegister.chipLED.put("PriC352", audio.audio.chipLED.get("PriC352"));
        chips[16] = audio.audio.chipRegister.chipLED.get("PriK054539");
        audio.audio.chipRegister.chipLED.put("PriK054539", audio.audio.chipLED.get("PriK054539"));
        chips[17] = audio.audio.chipRegister.chipLED.get("PriRF5C68");
        audio.audio.chipRegister.chipLED.put("PriRF5C68", audio.audio.chipLED.get("PriRF5C68"));


        chips[128 + 0] = audio.audio.chipRegister.chipLED.get("SecOPN");
        audio.audio.chipRegister.chipLED.put("SecOPN", audio.audio.chipLED.get("SecOPN"));
        chips[128 + 1] = audio.audio.chipRegister.chipLED.get("SecOPN2");
        audio.audio.chipRegister.chipLED.put("SecOPN2", audio.audio.chipLED.get("SecOPN2"));
        chips[128 + 2] = audio.audio.chipRegister.chipLED.get("SecOPNA");
        audio.audio.chipRegister.chipLED.put("SecOPNA", audio.audio.chipLED.get("SecOPNA"));
        chips[128 + 3] = audio.audio.chipRegister.chipLED.get("SecOPNB");
        audio.audio.chipRegister.chipLED.put("SecOPNB", audio.audio.chipLED.get("SecOPNB"));

        chips[128 + 4] = audio.audio.chipRegister.chipLED.get("SecOPM");
        audio.audio.chipRegister.chipLED.put("SecOPM", audio.audio.chipLED.get("SecOPM"));
        chips[128 + 5] = audio.audio.chipRegister.chipLED.get("SecDCSG");
        audio.audio.chipRegister.chipLED.put("SecDCSG", audio.audio.chipLED.get("SecDCSG"));
        chips[128 + 6] = audio.audio.chipRegister.chipLED.get("SecRF5C");
        audio.audio.chipRegister.chipLED.put("SecRF5C", audio.audio.chipLED.get("SecRF5C"));
        chips[128 + 7] = audio.audio.chipRegister.chipLED.get("SecPWM");
        audio.audio.chipRegister.chipLED.put("SecPWM", audio.audio.chipLED.get("SecPWM"));

        chips[128 + 8] = audio.audio.chipRegister.chipLED.get("SecOKI5");
        audio.audio.chipRegister.chipLED.put("SecOKI5", audio.audio.chipLED.get("SecOKI5"));
        chips[128 + 9] = audio.audio.chipRegister.chipLED.get("SecOKI9");
        audio.audio.chipRegister.chipLED.put("SecOKI9", audio.audio.chipLED.get("SecOKI9"));
        chips[128 + 10] = audio.audio.chipRegister.chipLED.get("SecC140");
        audio.audio.chipRegister.chipLED.put("SecC140", audio.audio.chipLED.get("SecC140"));
        chips[128 + 11] = audio.audio.chipRegister.chipLED.get("SecSPCM");
        audio.audio.chipRegister.chipLED.put("SecSPCM", audio.audio.chipLED.get("SecSPCM"));

        chips[128 + 12] = audio.audio.chipRegister.chipLED.get("SecAY10");
        audio.audio.chipRegister.chipLED.put("SecAY10", audio.audio.chipLED.get("SecAY10"));
        chips[128 + 13] = audio.audio.chipRegister.chipLED.get("SecOPLL");
        audio.audio.chipRegister.chipLED.put("SecOPLL", audio.audio.chipLED.get("SecOPLL"));
        chips[128 + 14] = audio.audio.chipRegister.chipLED.get("SecHuC");
        audio.audio.chipRegister.chipLED.put("SecHuC", audio.audio.chipLED.get("SecHuC"));
        chips[128 + 15] = audio.audio.chipRegister.chipLED.get("SecC352");
        audio.audio.chipRegister.chipLED.put("SecC352", audio.audio.chipLED.get("SecC352"));
        chips[128 + 16] = audio.audio.chipRegister.chipLED.get("SecK054539");
        audio.audio.chipRegister.chipLED.put("SecK054539", audio.audio.chipLED.get("SecK054539"));
        chips[128 + 17] = audio.audio.chipRegister.chipLED.get("SecRF5C68");
        audio.audio.chipRegister.chipLED.put("SecRF5C68", audio.audio.chipLED.get("SecRF5C68"));

        return chips;
    }
}
