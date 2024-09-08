package mdplayer.form.kb.opn;

import java.awt.Dimension;
import java.awt.Image;
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
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import dotnet4j.io.Directory;
import mdplayer.Common;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.form.sys.frmTPGet;
import mdplayer.form.sys.frmTPPut;
import mdplayer.properties.Resources;


public class frmYM2612MIDI extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int zoom = 1;

    private MDChipParams.YM2612MIDI newParam;
    private MDChipParams.YM2612MIDI oldParam = new MDChipParams.YM2612MIDI();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYM2612MIDI.class);

    public frmYM2612MIDI(frmMain frm, int zoom, MDChipParams.YM2612MIDI newParam) {
        super(frm);
        this.zoom = zoom;

        initializeComponent();
        this.addMouseWheelListener(this::frmYM2612MIDI_MouseWheel);

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneYM2612MIDI(), null, zoom);
        DrawBuff.screenInitYM2612MIDI(frameBuffer);
        update();
    }

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().setPosYm2612MIDI(getLocation());
            } else {
                parent.setting.getLocation().setPosYm2612MIDI(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
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
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneYM2612MIDI().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2612MIDI().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneYM2612MIDI().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2612MIDI().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneYM2612MIDI().getWidth() * zoom, frameSizeH + Resources.getPlaneYM2612MIDI().getHeight() * zoom));
        componentListener.componentResized(null);
    }

    private ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentMoved(ComponentEvent e) {
            prefs.putInt("x", e.getComponent().getX());
            prefs.putInt("y", e.getComponent().getY());
        }
        @Override
        public void componentResized(ComponentEvent e) {
        }
    };

    public void screenChangeParams() {
        int[][] fmRegister = audio.getYM2612MIDIRegister();
        //int[] fmKey = audio.GetFMKeyOn();

        newParam.IsMONO = parent.setting.getMidiKbd().isMono();
        if (parent.setting.getMidiKbd().isMono()) {
            for (int i = 0; i < 6; i++) {
                newParam.useChannel[i] = (parent.setting.getMidiKbd().getUseMonoChannel() == i);
            }
        } else {
            for (int i = 0; i < 6; i++) {
                newParam.useChannel[i] = parent.setting.getMidiKbd().getUseChannel()[i];
            }
        }

        newParam.useFormat = parent.setting.getMidiKbd().getUseFormat();

        for (int ch = 0; ch < 6; ch++) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                newParam.channels[ch].inst[i * 11 + 0] = fmRegister[p][0x50 + ops + c] & 0x1f; //AR
                newParam.channels[ch].inst[i * 11 + 1] = fmRegister[p][0x60 + ops + c] & 0x1f; //DR
                newParam.channels[ch].inst[i * 11 + 2] = fmRegister[p][0x70 + ops + c] & 0x1f; //SR
                newParam.channels[ch].inst[i * 11 + 3] = fmRegister[p][0x80 + ops + c] & 0x0f; //RR
                newParam.channels[ch].inst[i * 11 + 4] = (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4;//SL
                newParam.channels[ch].inst[i * 11 + 5] = fmRegister[p][0x40 + ops + c] & 0x7f;//TL
                newParam.channels[ch].inst[i * 11 + 6] = (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6;//KS
                newParam.channels[ch].inst[i * 11 + 7] = fmRegister[p][0x30 + ops + c] & 0x0f;//ML
                newParam.channels[ch].inst[i * 11 + 8] = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4;//DT
                newParam.channels[ch].inst[i * 11 + 9] = (fmRegister[p][0x60 + ops + c] & 0x80) >> 7;//AM
                newParam.channels[ch].inst[i * 11 + 10] = fmRegister[p][0x90 + ops + c] & 0x0f;//SG
            }
            newParam.channels[ch].inst[44] = fmRegister[p][0xb0 + c] & 0x07;//AL
            newParam.channels[ch].inst[45] = (fmRegister[p][0xb0 + c] & 0x38) >> 3;//FB
            newParam.channels[ch].inst[46] = (fmRegister[p][0xb4 + c] & 0x38) >> 4;//AMS
            newParam.channels[ch].inst[47] = fmRegister[p][0xb4 + c] & 0x07;//FMS

            newParam.channels[ch].pan = (fmRegister[p][0xb4 + c] & 0xc0) >> 6;

            if (newParam.selectCh != -1 && newParam.selectParam != -1) {
                if (oldParam.selectCh != -1 && oldParam.selectParam != -1) {
                    newParam.channels[oldParam.selectCh].typ[oldParam.selectParam] = 0;
                }
                newParam.channels[newParam.selectCh].typ[newParam.selectParam] = 1;
                oldParam.selectCh = newParam.selectCh;
                oldParam.selectParam = newParam.selectParam;
            }

            //int freq = 0;
            //int octav = 0;
            //int n = -1;
            //freq = fmRegister[p][0xa0 + c] + (fmRegister[p][0xa4 + c] & 0x07) * 0x100;
            //octav = (fmRegister[p][0xa4 + c] & 0x38) >> 3;

            //if (fmKey[ch] > 0) n = Math.min(Math.max(octav * 12 + searchFMNote(freq), 0), 95);

            //newParam.channels[ch].volumeL = Math.min(Math.max(fmVol[ch][0] / 80, 0), 19);
            //newParam.channels[ch].volumeR = Math.min(Math.max(fmVol[ch][1] / 80, 0), 19);
            //newParam.channels[ch].note = n;
        }
    }

    public void screenDrawParams() {
        for (int c = 0; c < 6; c++) {

            MDChipParams.Channel oyc = oldParam.channels[c];
            MDChipParams.Channel nyc = newParam.channels[c];

            boolean YM2612type = parent.setting.getYM2612Type()[0].getUseReal()[0];
            int tp = YM2612type ? 1 : 0;

            DrawBuff.Inst(frameBuffer, 1, 6 + (c > 2 ? 3 : 0), c, oyc.inst, nyc.inst, oyc.typ, nyc.typ);

            int[] onl = oldParam.noteLog[c];
            int[] nnl = newParam.noteLog[c];

            for (int n = 0; n < 10; n++) {
                DrawBuff.NoteLogYM2612MIDI(frameBuffer, (c % 3) * 13 * 8 + 2 * 8 + n * 8, (c / 3) * 18 * 4 + 24 * 4, onl[n], nnl[n]);
            }

            DrawBuff.UseChannelYM2612MIDI(frameBuffer, (c % 3) * 13 * 8, (c / 3) * 9 * 8 + 4 * 8, oldParam.useChannel[c], newParam.useChannel[c]);
        }

        DrawBuff.MONOPOLYYM2612MIDI(frameBuffer, oldParam.IsMONO, newParam.IsMONO);

        DrawBuff.LfoSw(frameBuffer, 16, 176, oldParam.lfoSw, newParam.lfoSw);
        DrawBuff.LfoFrq(frameBuffer, 64, 176, oldParam.lfoFrq, newParam.lfoFrq);
        DrawBuff.ToneFormat(frameBuffer, 16, 6, oldParam.useFormat, newParam.useFormat);
    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();

            //上部ラベル
            if (py < 8) return;

            if (py < 16) {
                //System.err.println("鍵盤");
            } else if (py < 32) {
                //System.err.println("各機能メニュー");
                int u = (py - 16) / 8;
                int p = -1;
                if (px >= 1 * 8 && px < 6 * 8) p = 0;
                else if (px >= 8 * 8 && px < 14 * 8) p = 1;
                else if (px >= 15 * 8 && px < 20 * 8) p = 2;
                else if (px >= 23 * 8 && px < 29 * 8) p = 3;
                else if (px >= 32 * 8 && px < 38 * 8) p = 4;

                if (p == -1) return;

                switch (u * 5 + p) {
                case 0:
                    //System.err.println("MONO");
                    cmdSetMode(0);
                    break;
                case 1:
                    break;
                case 2:
                    //System.err.println("PANIC");
                    cmdAllNoteOff();
                    break;
                case 3:
                    //System.err.println("TP.PUT");
                    cmdTPPut();
                    break;
                case 4:
                    //System.err.println("T.LOAD");
                    cmdTLoad();
                    break;
                case 5:
                    //System.err.println("POLY");
                    cmdSetMode(1);
                    break;
                case 6:
                    parent.setting.getMidiKbd().setUseFormat(parent.setting.getMidiKbd().getUseFormat() + 1);
                    if (parent.setting.getMidiKbd().getUseFormat() > 4) parent.setting.getMidiKbd().setUseFormat(0);
                    break;
                case 7:
                    //System.err.println("L.CLS");
                    cmdLogClear();
                    break;
                case 8:
                    //System.err.println("TP.GET");
                    cmdTPGet();
                    break;
                case 9:
                    //System.err.println("T.SAVE");
                    cmdTSave();
                    break;
                }
            } else if (py < 40) {
                if ((px / 8) % 13 == 0) {
                    //System.err.println("チャンネル選択");
                    cmdSelectChannel(px / 8 / 13);
                } else {
                    //System.err.println("音色選択(1-3Ch)");
                    cmdSelectTone(px, py, ev);// / 8 / 13, e);
                }
            } else if (py < 80) {
                //System.err.println("音色選択(1-3Ch)");
                cmdSelectTone(px, py, ev);
            } else if (py < 104) {
                if (py < 88 && (px / 8) % 13 == 3) {
                    //System.err.println("ログクリア");
                    cmdLogClear(px / 8 / 13);
                } else {
                    //System.err.println("ログ->MML変換(1-3Ch)");
                    cmdLog2MML(px / 8 / 13);
                }
            } else if (py < 112) {
                if ((px / 8) % 13 == 0) {
                    //System.err.println("チャンネル選択");
                    cmdSelectChannel((px / 8 / 13) + 3);
                } else {
                    //System.err.println("音色選択(4-6Ch)");
                    cmdSelectTone(px, py, ev);
                }
            } else if (py < 152) {
                //System.err.println("音色選択(4-6Ch)");
                cmdSelectTone(px, py, ev);
            } else if (py < 176) {
                if (py < 160 && (px / 8) % 13 == 3) {
                    //System.err.println("ログクリア");
                    cmdLogClear((px / 8 / 13) + 3);
                } else {
                    //System.err.println("ログ->MML変換(4-6Ch)");
                    cmdLog2MML((px / 8 / 13) + 3);
                }
            }
        }
    };

    /**
     * MONO/POLY
     */
    private void cmdSetMode(int m) {
        parent.ym2612Midi_SetMode(m);
    }

    /**
     * PANIC
     */
    private void cmdAllNoteOff() {
        parent.ym2612Midi_AllNoteOff();
    }

    /**
     * L.CLS
     */
    private void cmdLogClear() {
        parent.ym2612Midi_ClearNoteLog();
    }

    /**
     * LogClear
     */
    private void cmdLogClear(int ch) {
        parent.ym2612Midi_ClearNoteLog(ch);
    }

    /**
     * MML変換
     */
    private void cmdLog2MML(int ch) {
        parent.ym2612Midi_Log2MML(ch);
    }

    /**
     * 
     */
    private void cmdSelectChannel(int ch) {
        parent.ym2612Midi_SelectChannel(ch);
    }

    private void cmdTPPut() {
        parent.ym2612Midi_SetTonesToSetting();
        frmTPPut frmTPPut = new frmTPPut();
        frmTPPut.ShowDialog(parent.setting, parent.tonePallet);
    }

    private void cmdTPGet() {
        parent.ym2612Midi_SetTonesToSetting();
        frmTPGet frmTPGet = new frmTPGet();
        frmTPGet.ShowDialog(parent.setting, parent.tonePallet);
        parent.ym2612Midi_SetTonesFromSetting();
    }

    private boolean IsInitialOpenFolder = true;

    private void cmdTSave() {
        JFileChooser sfd = new JFileChooser();
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".xml"); }
            @Override public String getDescription() { return "XMLファイル(*.xml)"; }
        });
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".gwi"); }
            @Override public String getDescription() { return "MML2VGMファイル(*.gwi)"; }
        });
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mwi"); }
            @Override public String getDescription() { return "FMP7ファイル(*.mwi)"; }
        });
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mml"); }
            @Override public String getDescription() { return "NRTDRVファイル(*.mml)"; }
        });
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mml"); }
            @Override public String getDescription() { return "MXDRVファイル(*.mml)"; }
        });
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mml"); }
            @Override public String getDescription() { return "MusicLALFファイル(*.mml)"; }
        });
        sfd.setDialogTitle("TonePalletファイルを保存");
        if (!parent.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(parent.setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
            sfd.setCurrentDirectory(new File(parent.setting.getOther().getDefaultDataPath()));
        } else {
//            sfd.RestoreDirectory = true;
        }
//        sfd.CheckPathExists = true;

        if (sfd.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        IsInitialOpenFolder = false;

        try {
            parent.ym2612Midi_SaveTonePallet(sfd.getSelectedFile().getPath(), Common.getFilterIndex(sfd) + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "ファイルの保存に失敗しました。");
        }
    }

    private void cmdTLoad() {
        JFileChooser ofd = new JFileChooser();
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".xml"); }
            @Override public String getDescription() { return "XMLファイル(*.xml)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".gwi"); }
            @Override public String getDescription() { return "MML2VGMファイル(*.gwi)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mwi"); }
            @Override public String getDescription() { return "FMP7ファイル(*.mwi)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mml"); }
            @Override public String getDescription() { return "NRTDRVファイル(*.mml)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mml"); }
            @Override public String getDescription() { return "MXDRVファイル(*.mml)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mml"); }
            @Override public String getDescription() { return "MusicLALFファイル(*.mml)"; }
        });
        ofd.setDialogTitle("TonePalletファイルの読込");
        if (!parent.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(parent.setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(parent.setting.getOther().getDefaultDataPath()));
        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;

        if (ofd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        IsInitialOpenFolder = false;

        try {
            parent.ym2612Midi_LoadTonePallet(ofd.getSelectedFile().getPath(), Common.getFilterIndex(ofd) + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "ファイルの読込に失敗しました。");
        }
    }

    private void cmdSelectTone(int px, int py, MouseEvent e) {
        int ch = px / 8 / 13 + (py < 104 ? 0 : 3);
        int row = -1;
        int col = 0;
        int n = -1;

        if (e.getButton() != MouseEvent.BUTTON2) {
            px %= 8 * 13;
            py = py >= 104 ? (py - 104) : (py - 32);
            col = px / 4;

            if (py < 8) {
                row = 0;
                switch (col) {
                case 10:
                case 11:
                    n = 44;
                    break;
                case 14:
                case 15:
                    n = 45;
                    break;
                case 19:
                case 20:
                    n = 46;
                    break;
                case 24:
                case 25:
                    n = 47;
                    break;
                }
            } else if (py < 16) {
                return;
            } else if (py < 48) {
                row = (py - 16) / 8 + 1;
                if (col < 12) {
                    n = col / 2;
                    if (n < 1) return;
                    n--;
                } else if (col < 15) {
                    n = 5;
                } else if (col < 25) {
                    n = (col + 1) / 2;
                    n -= 2;
                } else {
                    return;
                }
                n += (row - 1) * 11;
            }

            //System.err.println("row=%d col=%d ch=%d n=%d", row, col, ch, n);
            parent.ym2612Midi_SetSelectInstParam(ch, n);
            return;
        }

        cmsMIDIKBD.setActionCommand(String.valueOf(ch));
        cmsMIDIKBD.setLocation(e.getX(), e.getY());
        cmsMIDIKBD.setVisible(true);
    }

    private KeyListener frmYM2612MIDI_KeyDown = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.isControlDown()) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    parent.ym2612Midi_CopyToneToClipboard();
                } else if (e.getKeyCode() == KeyEvent.VK_V) {
                    parent.ym2612Midi_PasteToneFromClipboard();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                parent.ym2612Midi_AddSelectInstParam(1);
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                parent.ym2612Midi_AddSelectInstParam(11);
            }
        }
    };

    private void ctsmiCopy_Click(ActionEvent ev) {
        parent.ym2612Midi_CopyToneToClipboard(Integer.parseInt(cmsMIDIKBD.getActionCommand()));
    }

    private void ctsmiPaste_Click(ActionEvent ev) {
        parent.ym2612Midi_PasteToneFromClipboard(Integer.parseInt(cmsMIDIKBD.getActionCommand()));
    }

    private void frmYM2612MIDI_MouseWheel(MouseWheelEvent ev) {
        parent.ym2612Midi_ChangeSelectedParamValue((int) Math.signum(ev.getScrollAmount()));
    }

    private void initializeComponent() {
//            this.components = new System.ComponentModel.Container();
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYM2612MIDI));
        this.pbScreen = new JPanel();
        this.cmsMIDIKBD = new JMenu();
        this.ctsmiCopy = new JMenuItem();
        this.ctsmiPaste = new JMenuItem();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();
//        this.cmsMIDIKBD.SuspendLayout();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneYM2612MIDI();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 184));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // cmsMIDIKBD
        //
        this.cmsMIDIKBD.add(this.ctsmiCopy);
        this.cmsMIDIKBD.add(this.ctsmiPaste);
        this.cmsMIDIKBD.setName("cmsMIDIKBD");
        this.cmsMIDIKBD.setPreferredSize(new Dimension(131, 48));
        //
        // ctsmiCopy
        //
        this.ctsmiCopy.setName("ctsmiCopy");
        this.ctsmiCopy.setPreferredSize(new Dimension(130, 22));
        this.ctsmiCopy.setText("コピー(&C)");
        this.ctsmiCopy.addActionListener(this::ctsmiCopy_Click);
        //
        // ctsmiPaste
        //
        this.ctsmiPaste.setName("ctsmiPaste");
        this.ctsmiPaste.setPreferredSize(new Dimension(130, 22));
        this.ctsmiPaste.setText("貼り付け(&p)");
        this.ctsmiPaste.addActionListener(this::ctsmiPaste_Click);
        //
        // frmYM2612MIDI
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(320, 184));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYM2612MIDI");
        this.setTitle("MIDI(Ym2612Inst)");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        this.addKeyListener(this.frmYM2612MIDI_KeyDown);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
        // this.cmsMIDIKBD.ResumeLayout(false);
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
    private JMenu cmsMIDIKBD;
    private JMenuItem ctsmiCopy;
    private JMenuItem ctsmiPaste;
}
