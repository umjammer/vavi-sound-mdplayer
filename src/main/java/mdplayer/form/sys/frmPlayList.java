package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumnModel;

import dotnet4j.Tuple;
import dotnet4j.io.Directory;
import dotnet4j.io.File;
import dotnet4j.io.Path;
import javafx.scene.input.DragEvent;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmArcType;
import mdplayer.Common.FileFormat;
import mdplayer.Common.Tuple4;
import mdplayer.Log;
import mdplayer.MDChipParams;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.properties.Resources;
import org.intellij.lang.annotations.JdkConstants.FontStyle;


public class frmPlayList extends JFrame {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public Setting setting = null;

    public String playFilename = "";
    public String playArcFilename = "";
    public FileFormat playFormat = FileFormat.unknown;
    public EnmArcType playArcType = EnmArcType.unknown;
    public int playSongNum = -1;

    private PlayList playList = null;
    private frmMain frmMain = null;

    private Boolean playing = false;
    private int playIndex = -1;
    private int oldPlayIndex = -1;

    private Random rand = new Random();
    private Boolean IsInitialOpenFolder = true;

    static Preferences prefs = Preferences.userNodeForPackage(frmPlayList.class);

    private static final String[] sext = ".Vgm;.vgz;.zip;.lzh;.nrd;.Xgm;.Zgm;.s98;.Nsf;.Hes;.Sid;.mnd;.mgs;.mdr;.mdx;.mub;.muc;.m;.m2;.mz;.mml;.mid;.rcp;.wav;.mp3;.aiff;.m3u".split(new String[] {";"});

    public frmPlayList(frmMain frm) {
        frmMain = frm;
        setting = frm.setting;
        initializeComponent();

        playList = PlayList.Load(null);
        playList.SetDGV(dgvList.getModel());
        playIndex = -1;

        oldPlayIndex = -1;
    }

    public Boolean isPlaying() {
        return playing;
    }

    public int getMusicCount() {
        return playList.getLstMusic().size();
    }

    public PlayList getPlayList() {
        return playList;
    }

    public Tuple4<Integer, Integer, String, String> setStart(int n) {
        updatePlayingIndex(n);

        String fn = playList.getLstMusic().get(playIndex).fileName;
        String zfn = playList.getLstMusic().get(playIndex).arcFileName;
        int m = 0;
        int songNo = playList.getLstMusic().get(playIndex).songNo;

        if (playList.getLstMusic().get(playIndex).type != null && playList.getLstMusic().get(playIndex).type != "-") {
            m = playList.getLstMusic().get(playIndex).type.charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }

        return new Tuple4<>(m, songNo, fn, zfn);
    }

    public void play() {
        playing = true;
    }

    public void stop() {
        //updatePlayingIndex(-1);
        //playIndex = -1;

        playing = false;
    }

    public void Save() {
        if (setting.getOther().getEmptyPlayList()) {
            playList.setLstMusic(new ArrayList<>());
        }
        playList.save(null);
    }

//    @Override
    protected Boolean getShowWithoutActivation() {
        return true;
    }

    public List<Tuple<String, String>> randomStack = new ArrayList<Tuple<String, String>>();

//    @Override
//    protected void WndProc(Message m) {
//        if (frmMain != null) {
//            frmMain.windowsMessage(m);
//        }
//
//        super.WndProc(m);
//    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            isClosed = true;
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                setting.getLocation().setPPlayList(getLocation());
                setting.getLocation().setPPlayListWH(new Dimension(getWidth(), getHeight()));
            } else {
                setting.getLocation().setPPlayList(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
                setting.getLocation().setPPlayListWH(new Dimension(prefs.getInt("width", 320), prefs.getInt("height", 200)));
            }
            setVisible(false);
            e.Cancel = true;
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
            if (setting.getLocation().getPPlayList() != Setting.EmptyPoint)
                setLocation(setting.getLocation().getPPlayList());
            if (setting.getLocation().getPPlayListWH() != Setting.EmptyDimension)
                setPreferredSize(new Dimension(setting.getLocation().getPPlayListWH()));
        }
    };

    public void Refresh() {
        dgvList.Rows.clear();
        List<TableColumnModel> rows = playList.makeRow(playList.getLstMusic());
        for (TableColumnModel row : rows) {
            dgvList.Rows.add(row);
        }
    }

    public void updatePlayingIndex(int newPlayingIndex) {
        if (oldPlayIndex != -1) {
            ResetColor(oldPlayIndex);
        }

        if (newPlayingIndex >= 0 && newPlayingIndex < dgvList.Rows.size()) {
            SetColor(newPlayingIndex);
        } else if (newPlayingIndex == -1) {
            newPlayingIndex = dgvList.Rows.size() - 1;
            SetColor(newPlayingIndex);
        } else if (newPlayingIndex == -2) {
            newPlayingIndex = 0;
            SetColor(newPlayingIndex);
        }
        playIndex = newPlayingIndex;
        oldPlayIndex = newPlayingIndex;
    }

    private void SetColor(int rowIndex) {
        dgvList.Rows[rowIndex].Cells[dgvList.Columns["clmPlayingNow"].Index].Value = ">";
        for (int i = 0; i < dgvList.Rows[rowIndex].Cells.size(); i++) {
            dgvList.Rows[rowIndex].Cells[i].Style.ForeColor = Color.green.brighter();
            dgvList.Rows[rowIndex].Cells[i].Style.SelectionForeColor = Color.green.brighter();
        }
    }

    private static Color clrLightBlue = new Color(255, 192, 192, 255);

    private void ResetColor(int rowIndex) {
        dgvList.Rows[rowIndex].Cells[dgvList.Columns["clmPlayingNow"].Index].Value = " ";
        for (int i = 0; i < dgvList.Rows[rowIndex].Cells.size(); i++) {
            dgvList.Rows[rowIndex].Cells[i].Style.ForeColor = clrLightBlue;
            dgvList.Rows[rowIndex].Cells[i].Style.SelectionForeColor = Color.white;
        }
    }

    private MouseListener dgvList_CellMouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.RowIndex < 0) return;
            dgvList.Rows[e.RowIndex].Selected = true;

            if (e.getButton() == MouseEvent.BUTTON2) {
                if (dgvList.SelectedRows.size() > 1) {
                    tsmiDelThis.setText("選択した曲を除去");
                } else {
                    tsmiDelThis.setText("この曲を除去");
                }
                cmsPlayList.setVisible(true);
                Point p = Control.MousePosition;
                cmsPlayList.Top = p.y;
                cmsPlayList.Left = p.x;
            }
        }
    };

    private void tsmiDelThis_Click(ActionEvent ev) {
        if (dgvList.SelectedRows.size() < 1) return;

        List<Integer> sel = new ArrayList<>();
        for (JListRow r : dgvList.SelectedRows) {
            sel.add(r.Index);
        }
        Collections.sort(sel);

        for (int i = sel.size() - 1; i >= 0; i--) {
            if (oldPlayIndex >= dgvList.getSelectedRows()[i].Index) {
                oldPlayIndex--;
            }
            if (playIndex >= dgvList.getSelectedRows()[i].Index) {
                playIndex--;
            }
            playList.getLstMusic().RemoveAt(dgvList.getSelectedRows()[i].Index);
            dgvList.Rows.RemoveAt(dgvList.getSelectedRows()[i].Index);
        }
    }

    public void nextPlay() {
        if (!playing) return;
        if (dgvList.Rows.size() == playIndex + 1) return;

        int pi = playIndex;
        playing = false;

        pi++;

        String fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
        String zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;
        int m = 0;
        int songNo = 0;
        try {
            songNo = (int) dgvList.Rows[pi].Cells["clmSongNo"].Value;
        } catch (Exception e) {
            songNo = 0;
        }
        if (dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value != null && dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value.toString() != "-") {
            m = dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value.toString()[0] - 'A';
            if (m < 0 || m > 9) m = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        if (Audio.errMsg != "") {
            playing = false;
            return;
        }
        updatePlayingIndex(pi);
        playing = true;

        playFilename = fn;
        playArcFilename = zfn;
        playSongNum = songNo;
        //playFormat = dgvList.Rows[pi].Cells["clmSongNo"].Value;
        //playArcType = dgvList.Rows[pi].Cells["clmSongNo"].Value;
    }

    public void nextPlayMode(int mode) {
        if (!playing) {
            playIndex = -1;
        }

        int pi = playIndex;
        playing = false;
        String fn, zfn;

        switch (mode) {
        case 0:// 通常
            if (dgvList.Rows.size() <= playIndex + 1) return;
            pi++;
            break;
        case 1:// ランダム

            if (pi != -1) {
                //再生履歴の更新
                fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
                zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;

                randomStack.add(new Tuple<String, String>(fn, zfn));
                while (randomStack.size() > 1000)
                    randomStack.remove(0);
            }

            pi = rand.nextInt(dgvList.Rows.size());
            break;
        case 2:// 全曲ループ
            pi++;
            if (pi >= dgvList.Rows.size()) {
                pi = 0;
            }
            break;
        case 3:// １曲ループ
            break;
        }

        if (pi + 1 > dgvList.Rows.size()) {
            playing = false;
            return;
        }

        fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
        zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;
        int m = 0;
        if (dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value != null && dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value.toString() != "-") {
            m = dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value.toString()[0] - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo = 0;
        try {
            songNo = (int) dgvList.Rows[pi].Cells["clmSongNo"].Value;
        } catch (Exception e) {
            songNo = 0;
        }

        if (!frmMain.loadAndPlay(m, songNo, fn, zfn)) {
            playing = false;
            return;
        }

        updatePlayingIndex(pi);
        playing = true;
    }

    public void prevPlay(int mode) {
        if (!playing) return;
        if (mode != 1 && playIndex < 1) return;

        int pi = playIndex;
        playing = false;
        String fn, zfn;

        if (mode != 1) {
            pi--;
            fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
            zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;
        } else {
            pi = 0;
            if (randomStack.size() > 0) {
                while (true) {
                    String hfn = randomStack.get(randomStack.size() - 1).Item1;
                    String hzfn = randomStack.get(randomStack.size() - 1).Item2;
                    randomStack.remove(randomStack.size() - 1);

                    for (; pi < dgvList.Rows.size(); pi++) {
                        fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
                        zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;
                        if (hfn.equals(fn) && hzfn.equals(zfn)) {
                            break loopEx;
                        }
                    }

                    if (randomStack.size() == 0) break;
                }

                if (playIndex < 1) return;
                pi = playIndex - 1;
                fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
                zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;
            } else {
                pi = playIndex;
                pi--;
                if (pi < 0) pi = 0;
                fn = (String) dgvList.Rows[pi].Cells["clmFileName"].Value;
                zfn = (String) dgvList.Rows[pi].Cells["clmZipFileName"].Value;
            }
            loopEx:
            ;
        }

        int m = 0;
        if (dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value != null && dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value.toString() != "-") {
            m = dgvList.Rows[pi].Cells[dgvList.Columns["clmType"].Index].Value.toString()[0] - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo = 0;
        try {
            songNo = (int) dgvList.Rows[pi].Cells["clmSongNo"].Value;
        } catch (Exception e) {
            songNo = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        updatePlayingIndex(pi);
        playing = true;
    }

    private void dgvList_CellDoubleClick(ActionEvent e) {
        if (e.RowIndex < 0) return;

        dgvList.setEnabled(false);

        playing = false;

        try {
            String fn = (String) dgvList.Rows[e.RowIndex].Cells["clmFileName"].Value;
            String zfn = (String) dgvList.Rows[e.RowIndex].Cells["clmZipFileName"].Value;
            int m = 0;
            int songNo = 0;
            try {
                songNo = (int) dgvList.Rows[e.RowIndex].Cells["clmSongNo"].Value;
            } catch (Exception e) {
                songNo = 0;
            }
            if (dgvList.Rows[e.RowIndex].Cells[dgvList.Columns["clmType"].Index].Value != null && dgvList.Rows[e.RowIndex].Cells[dgvList.Columns["clmType"].Index].Value.toString() != "-") {
                m = dgvList.Rows[e.RowIndex].Cells[dgvList.Columns["clmType"].Index].Value.toString()[0] - 'A';
                if (m < 0 || m > 9) m = 0;
            }

            if (!frmMain.loadAndPlay(m, songNo, fn, zfn)) return;
            updatePlayingIndex(e.RowIndex);

            playing = true;
        } finally {
            //dgvList.MultiSelect = true;
            dgvList.setEnabled(true);
            dgvList.Rows[e.RowIndex].Selected = true;
        }
    }

    private void tsmiPlayThis_Click(ActionEvent ev) {
        if (dgvList.SelectedRows.size() < 0) return;

        playing = false;

        String fn = (String) dgvList.Rows[dgvList.getSelectedRows()[0]].Cells["clmFileName"].Value;
        String zfn = (String) dgvList.Rows[dgvList.getSelectedRows()[0]].Cells["clmZipFileName"].Value;
        int m = 0;
        if (dgvList.Rows[dgvList.getSelectedRows()[0]].Cells[dgvList.Columns["clmType"].Index].Value != null && dgvList.Rows[dgvList.getSelectedRows()[0]].Cells[dgvList.Columns["clmType"].Index].Value.toString() != "-") {
            m = dgvList.Rows[dgvList.getSelectedRows()[0]].Cells[dgvList.Columns["clmType"].Index].Value.toString()[0] - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo = 0;
        try {
            songNo = (int) dgvList.Rows[dgvList.getSelectedRows()[0]].Cells["clmSongNo"].Value;
        } catch (Exception e) {
            songNo = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        updatePlayingIndex(dgvList.getSelectedRows()[0]);

        playing = true;
    }

    private void tsmiDelAllMusic_Click(ActionEvent ev) {

        int res = JOptionPane.showConfirmDialog("プレイリストの全ての曲が除去されます。よろしいですか。", "PlayList", JOptionPane.OK_OPTIONCancel, MessageBoxIcon.Warning);
        if (res != JFileChooser.APPROVE_OPTION) return;

        playing = false;
        dgvList.Rows.clear();
        playList.getLstMusic().clear();
        playIndex = -1;
        oldPlayIndex = -1;

    }

    private void tsbOpenPlayList_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".xml"); }
            @Override public String getDescription() { return "XMLファイル(*.xml)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".m3u"); }
            @Override public String getDescription() { return "M3Uファイル(*.m3u)"; }
        });
        ofd.setDialogTitle("プレイリストファイルを選択");
        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(frmMain.setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
            ofd.setCurrentDirectory(new java.io.File(frmMain.setting.getOther().getDefaultDataPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;

        if (ofd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        IsInitialOpenFolder = false;

        try {
            PlayList pl = null;
            String filename = ofd.getSelectedFile().getPath();

            if (filename.toLowerCase().lastIndexOf(".m3u") == -1) {
                pl = PlayList.Load(filename);
                playing = false;
                playList = pl;
                playList.SetDGV(dgvList.getModel());
            } else {
                pl = PlayList.LoadM3U(filename);
                playing = false;
                playList.getLstMusic().clear();
                for (PlayList.Music ms : pl.getLstMusic()) {
                    playList.AddFile(ms.fileName);
                    //AddList(ms.fileName);
                }
            }

            playIndex = -1;
            oldPlayIndex = -1;

            Refresh();

        } catch (Exception ex) {
            Log.forcedWrite(ex);
            JOptionPane.showConfirmDialog(null, "ファイルの読み込みに失敗しました。");
        }
    }

    private void tsbSavePlayList_Click(ActionEvent ev) {

        JFileChooser sfd = new JFileChooser();
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".xml"); }
            @Override public String getDescription() { return "XMLファイル(*.xml)"; }
        });
        sfd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".m3u"); }
            @Override public String getDescription() { return "M3Uファイル(*.m3u)"; }
        });
        sfd.setDialogTitle("プレイリストファイルを保存");
        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(frmMain.setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
            sfd.setCurrentDirectory(new java.io.File(frmMain.setting.getOther().getDefaultDataPath()));
//        } else {
//            sfd.RestoreDirectory = true;
        }
//        sfd.CheckPathExists = true;

        if (sfd.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        IsInitialOpenFolder = false;
        String filename = sfd.getSelectedFile().getPath();

        switch (Common.getFilterIndex(sfd)) {
        case 1:
            if (Path.getExtension(filename).isEmpty()) {
                filename = Path.combine(filename, ".m3u");
            }
            break;
        case 0:
            if (Path.getExtension(filename).isEmpty()) {
                filename = Path.combine(filename, ".xml");
            }
            break;
        }

        try {
            if (filename.toLowerCase().lastIndexOf(".m3u") == -1)
                playList.save(filename);
            else
                playList.saveM3U(filename);

        } catch (Exception ex) {
            Log.forcedWrite(ex);
            JOptionPane.showConfirmDialog(null, "ファイルの保存に失敗しました。");
        }
    }

    private void tsbAddMusic_Click(ActionEvent ev) {

        JFileChooser ofd = new JFileChooser();
        Arrays.stream(Resources.getCntSupportFile().split("\\s")).forEach(l -> {
            String[] p = l.split("|");
            ofd.setFileFilter(new FileFilter() {
                @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(p[1]); }
                @Override public String getDescription() { return p[0]; }
            });
        });
        ofd.setDialogTitle("ファイルを選択してください");
        ofd.setFileFilter(ofd.getChoosableFileFilters()[setting.getOther().getFilterIndex()]);

        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(frmMain.setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
            ofd.setCurrentDirectory(new java.io.File(frmMain.setting.getOther().getDefaultDataPath()));
        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(true);

        if (ofd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        IsInitialOpenFolder = false;
        setting.getOther().setFilterIndex(Common.getFilterIndex(ofd));

        stop();

        try {
            for (java.io.File fn : ofd.getSelectedFiles()) {
                playList.AddFile(fn.getPath());
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }

        //Play();
    }

    static final String[] _exts = {
            ".VGM", ".VGZ", ".ZIP", ".NRD",
            ".XGM", ".S98", ".NSF", ".HES",
            ".SID", ".MID", ".RCP", ".M3U",
            ".MDR"
    };

    private void tsbAddFolder_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("フォルダーを指定してください。");
        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(frmMain.setting.getOther().getDefaultDataPath())) {
            fbd.setSelectedFile(new java.io.File(frmMain.setting.getOther().getDefaultDataPath()));
        }

        if (fbd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        stop();

        try {
            Files.list(Paths.get(fbd.getSelectedFile().getPath())).forEach(p -> {
                String ext = Path.getExtension(p.getFileName().toString()).toUpperCase();
                if (Arrays.asList(_exts).contains(ext)) {
                    playList.AddFile(p.toFile().getAbsolutePath());
                }
            });
        } catch (IOException ex) {
            Log.forcedWrite(ex);
        }

        frmMain.oldParam = new MDChipParams();

        play();
    }

    private void tsbUp_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 1 || dgvList.getSelectedRows()[0] < 1) {
            return;
        }

        int ind = dgvList.getSelectedRows()[0];
        PlayList.Music mus = playList.getLstMusic().get(ind - 1);
        JListRow row = dgvList.Rows[ind - 1];

        if (ind == playIndex) playIndex--;
        else if (ind == playIndex + 1) playIndex++;

        if (ind == oldPlayIndex) oldPlayIndex--;
        else if (ind == oldPlayIndex + 1) oldPlayIndex++;

        playList.getLstMusic().remove(ind - 1);
        dgvList.remove(ind - 1);

        playList.getLstMusic().add(ind, mus);
        dgvList.Rows.Insert(ind, row);
    }

    private void tsbDown_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() != 1 || dgvList.getSelectedRows()[0] >= dgvList.getRowCount() - 1) {
            return;
        }

        int ind = dgvList.getSelectedRows()[0];
        PlayList.Music mus = playList.getLstMusic().get(ind + 1);
        JListRow row = dgvList.Rows[ind + 1];

        if (ind == playIndex) playIndex++;
        else if (ind == playIndex - 1) playIndex--;

        if (ind == oldPlayIndex) oldPlayIndex++;
        else if (ind == oldPlayIndex - 1) oldPlayIndex--;

        playList.getLstMusic().remove(ind + 1);
        dgvList.Rows.RemoveAt(ind + 1);

        playList.getLstMusic().add(ind, mus);
        dgvList.Rows.Insert(ind, row);

    }

    private void toolStripButton1_Click(ActionEvent ev) {
        dgvList.getColumn("clmTitle").setVisible = !tsbJapanese.isSelected();
        dgvList.Columns["clmTitleJ"].Visible = tsbJapanese.isSelected();
        dgvList.Columns["clmGame"].Visible = !tsbJapanese.isSelected();
        dgvList.Columns["clmGameJ"].Visible = tsbJapanese.isSelected();
        dgvList.Columns["clmComposer"].Visible = !tsbJapanese.isSelected();
        dgvList.Columns["clmComposerJ"].Visible = tsbJapanese.isSelected();
    }

    private void frmPlayList_KeyDown(KeyEvent e) {
        //System.err.println("keycode{0} {1} {2}", e.KeyCode, e.KeyData, e.KeyValue);

        switch (e.getKeyCode()) {
        case 32: //Space
        case 13: //Enter
            if (dgvList.getSelectedRowCount() == 0) {
                return;
            }

            int index = dgvList.getSelectedRows()[0];

            e.Handled = true;

            playing = false;

            String fn = (String) dgvList.getSelectedRows()[0].Cells["clmFileName"].Value;
            String zfn = (String) dgvList.getSelectedRows()[0].Cells["clmZipFileName"].Value;
            int m = 0;
            if (dgvList.getSelectedRows()[0].Cells[dgvList.Columns["clmType"].Index].Value != null && dgvList.getSelectedRows()[0].Cells[dgvList.Columns["clmType"].Index].Value.toString() != "-") {
                m = dgvList.getSelectedRows()[0].Cells[dgvList.Columns["clmType"].Index].Value.toString()[0] - 'A';
                if (m < 0 || m > 9) m = 0;
            }
            int songNo = 0;
            try {
                songNo = (int) dgvList.getSelectedRows()[0].Cells["clmSongNo"].Value;
            } catch (Exception ex) {
                songNo = 0;
            }

            frmMain.loadAndPlay(m, songNo, fn, zfn);
            updatePlayingIndex(index);

            playing = true;
            break;
        case 46: //Delete
            e.Handled = true;
            tsmiDelThis_Click(null);
            break;
        }
    }

    private void dgvList_DragEnter(DragEvent e) {
        e.Effect = DragDropEffects.All;
        Point cp = dgvList.PointToClient(new Point(e.getX(), e.getY()));
        JList.HitTestInfo hti = dgvList.HitTest(cp.x, cp.y);
        if (hti.Type != JListHitTestType.Cell || hti.RowIndex < 0 || hti.RowIndex >= dgvList.Rows.size()) return;
        dgvList.MultiSelect = false;
        dgvList.MultiSelect = true;
        dgvList.Rows[hti.RowIndex].Selected = true;
    }

    private void dgvList_DragOver(DragEvent e) {
        e.Effect = DragDropEffects.All;
        Point cp = dgvList.PointToClient(new Point(e.getX(), e.getY()));
        JList.HitTestInfo hti = dgvList.HitTest(cp.x, cp.y);
        if (hti.Type != JListHitTestType.Cell || hti.RowIndex < 0 || hti.RowIndex >= dgvList.Rows.size()) return;
        dgvList.MultiSelect = false;
        dgvList.MultiSelect = true;
        dgvList.Rows[hti.RowIndex].Selected = true;
    }

    private final Object relock = new Object();
    private Boolean reent = false;

    public void dgvList_DragDrop(DragEvent e) {
        synchronized (relock) {
            if (reent) return;
            reent = true;
        }

        if (!e.Data.GetDataPresent(DataFormats.FileDrop)) return;

        try {
            this.setEnabled(false);
            this.timer1.setEnabled(false);

            String[] filename = ((String[]) e.Data.GetData(DataFormats.FileDrop));

            //ドロップされたアイテムがフォルダーの場合は下位フォルダー内も含めた
            //実際のファイルのリストを取得する
            List<String> result = new ArrayList<String>();
            GetTrueFileNameList(result, filename);
            //重複を取り除く
            filename = result.Distinct().toArray();

            int i = playList.getLstMusic().size();
            Point cp = dgvList.PointToClient(new Point(e.getX(), e.getY()));
            JList.HitTestInfo hti = dgvList.HitTest(cp.getX(), cp.getY());
            if (hti.Type == JListHitTestType.Cell && hti.RowIndex >= 0 && hti.RowIndex < dgvList.Rows.size()) {
                if (hti.RowIndex < playList.getLstMusic().size()) i = hti.RowIndex;
            }

            //曲を停止
            stop();
            frmMain.stop();
            while (!Audio.isStopped())
                Application.DoEvents();

            int buIndex = i;

            playList.InsertFile(i, filename);

            if (buIndex <= oldPlayIndex) {
                oldPlayIndex += i - buIndex;
            }
            i = buIndex;

            //選択位置の曲を再生する
            String fn = playList.getLstMusic().get(i).fileName;
            if (
                    fn.toLowerCase().lastIndexOf(".lzh") == -1
                            && fn.toLowerCase().lastIndexOf(".zip") == -1
                            && fn.toLowerCase().lastIndexOf(".m3u") == -1
                //&& fn.toLowerCase().lastIndexOf(".Sid") == -1
            ) {
                frmMain.loadAndPlay(0, 0, fn, null);
                setStart(i);// -1);
                frmMain.oldParam = new MDChipParams();
                play();
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            JOptionPane.showConfirmDialog(null, "ファイルの読み込みに失敗しました。");
        } finally {
            this.setEnabled(true);
            this.timer1.start();
            synchronized (relock) {
                reent = false;
            }
        }
    }

    private void GetTrueFileNameList(List<String> res, Iterable<String> files) {
        for (String f : files) {
            if (File.exists(f)) {
                if (!res.contains(f)) {
                    String ext = Path.getExtension(f).toLowerCase();
                    if (sext.contains(ext)) res.add(f);
                }
            } else {
                if (Directory.exists(f)) {
                    Iterable<String> fs = Directory.EnumerateFiles(f, "*", SearchOption.AllDirectories);
                    GetTrueFileNameList(res, fs);
                }
            }
        }
    }

    private void tsmiA_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 1) return;

        List<Integer> sel = new ArrayList<Integer>();
        for (int r : dgvList.getSelectedRows()) {
            playList.getLstMusic().get(r).type = ((ToolStripMenuItem) sender).Text;
            r.Cells["clmType"].Value = ((ToolStripMenuItem) sender).Text;
        }
    }

    String ofn = "";
    String oafn = "";
    String[][] exts = new String[3][];
    String text = "";
    String mml = "";
    String img = "";

    private void timer1_Tick(ActionEvent ev) {
        if (!playing) return;
        if (setting == null) return;

        String fn = "";
        String arcFn = "";

        Audio.getPlayingFileName(fn, arcFn);

        if (fn.equals(ofn) && arcFn.equals(oafn)) return;
        ofn = fn;
        oafn = arcFn;

        exts[0] = setting.getOther().getTextExt().split(";");
        exts[1] = setting.getOther().getMMLExt().split(";");
        exts[2] = setting.getOther().getImageExt().split(";");

        String bfn = Path.combine(Path.getDirectoryName(fn), Path.getFileNameWithoutExtension(fn));
        String bfnfld = Path.combine(Path.getDirectoryName(fn), Path.getFileName(Path.getDirectoryName(fn)));

        text = "";
        for (String ext : exts[0]) {
            if (File.exists(bfn + "." + ext)) {
                text = bfn + "." + ext;
                break;
            }
            if (File.exists(bfnfld + "." + ext)) {
                text = bfnfld + "." + ext;
                break;
            }
        }
        mml = "";
        for (String ext : exts[1]) {
            if (File.exists(bfn + "." + ext)) {
                mml = bfn + "." + ext;
                break;
            }
            if (File.exists(bfnfld + "." + ext)) {
                mml = bfnfld + "." + ext;
                break;
            }
        }
        img = "";
        for (String ext : exts[2]) {
            if (File.exists(bfn + "." + ext)) {
                img = bfn + "." + ext;
                break;
            }
            if (File.exists(bfnfld + "." + ext)) {
                img = bfnfld + "." + ext;
                break;
            }
        }

        tsbTextExt.setEnabled(!text.isEmpty());
        tsbMMLExt.setEnabled(!mml.isEmpty());
        tsbImgExt.setEnabled(!img.isEmpty());

        if (setting.getOther().getAutoOpenText() && !text.isEmpty()) tsbTextExt_Click(null);
        if (setting.getOther().getAutoOpenMML() && !mml.isEmpty()) tsbMMLExt_Click(null);
        if (setting.getOther().getAutoOpenImg() && !img.isEmpty()) tsbImgExt_Click(null);
    }

    private void tsbTextExt_Click(ActionEvent ev) {
        if (text.isEmpty()) return;
        try {
            new ProcessBuilder(text).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void tsbMMLExt_Click(ActionEvent ev) {
        if (mml == "") return;
        try {
            new ProcessBuilder(mml).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void tsbImgExt_Click(ActionEvent ev) {
        if (img == "") return;
        try {
            new ProcessBuilder(img).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PlayList.Music getPlayingSongInfo() {
        if (playIndex < 0 || dgvList.getRowCount() <= playIndex) return null;
        return (PlayList.Music) dgvList.Rows[playIndex].Tag;
    }

    private void tsmiOpenFolder_Click(ActionEvent ev) {
        try {
            String path = (String) dgvList.getSelectedRows()[0].Cells["clmFileName"].Value;
            path = Path.getDirectoryName(path);
            new ProcessBuilder(path).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void initializeComponent() {
//        this.components = new System.ComponentModel.Container();
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmPlayList));
        JTableCellStyle JListCellStyle1 = new JTableCellStyle();
        JTableCellStyle JListCellStyle3 = new JTableCellStyle();
        JTableCellStyle JListCellStyle4 = new JTableCellStyle();
        JTableCellStyle JListCellStyle2 = new JTableCellStyle();
        this.toolStripContainer1 = new JToolStripContainer();
        this.dgvList = new JTable();
        this.clmKey = new JTextArea();
        this.clmSongNo = new JTextArea();
        this.clmZipFileName = new JTextArea();
        this.clmFileName = new JTextArea();
        this.clmPlayingNow = new JTextArea();
        this.clmEXT = new JTextArea();
        this.clmType = new JTextArea();
        this.clmTitle = new JTextArea();
        this.clmTitleJ = new JTextArea();
        this.clmDispFileName = new JTextArea();
        this.clmGame = new JTextArea();
        this.clmGameJ = new JTextArea();
        this.clmComposer = new JTextArea();
        this.clmComposerJ = new JTextArea();
        this.clmVGMby = new JTextArea();
        this.clmConverted = new JTextArea();
        this.clmNotes = new JTextArea();
        this.clmDuration = new JTextArea();
        this.clmSpacer = new JTextArea();
        this.cmsPlayList = new JContextMenuStrip(this.components);
        this.type設定ToolStripMenuItem = new JMenuItem();
        this.tsmiA = new JMenuItem();
        this.tsmiB = new JMenuItem();
        this.tsmiC = new JMenuItem();
        this.tsmiD = new JMenuItem();
        this.tsmiE = new JMenuItem();
        this.tsmiF = new JMenuItem();
        this.tsmiG = new JMenuItem();
        this.tsmiH = new JMenuItem();
        this.tsmiI = new JMenuItem();
        this.tsmiJ = new JMenuItem();
        this.toolStripSeparator5 = new JSeparator();
        this.tsmiPlayThis = new JMenuItem();
        this.tsmiDelThis = new JMenuItem();
        this.toolStripSeparator3 = new JSeparator();
        this.tsmiDelAllMusic = new JMenuItem();
        this.tsmiOpenFolder = new JMenuItem();
        this.toolStrip1 = new JToolBar();
        this.tsbOpenPlayList = new JButton();
        this.tsbSavePlayList = new JButton();
        this.toolStripSeparator1 = new JSeparator();
        this.tsbAddMusic = new JButton();
        this.tsbAddFolder = new JButton();
        this.toolStripSeparator2 = new JSeparator();
        this.tsbUp = new JButton();
        this.tsbDown = new JButton();
        this.toolStripSeparator4 = new JSeparator();
        this.tsbJapanese = new JButton();
        this.toolStripSeparator6 = new JSeparator();
        this.tsbTextExt = new JButton();
        this.tsbMMLExt = new JButton();
        this.tsbImgExt = new JButton();
        this.timer1 = new Timer(this.components);
//        this.toolStripContainer1.ContentPanel.SuspendLayout();
//        this.toolStripContainer1.TopToolStripPanel.SuspendLayout();
//        this.toolStripContainer1.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvList)).BeginInit();
//        this.cmsPlayList.SuspendLayout();
//        this.toolStrip1.SuspendLayout();

        //
        // toolStripContainer1
        //
        //
        // toolStripContainer1.ContentPanel
        //
//        //resources.ApplyResources(this.toolStripContainer1.ContentPanel, "toolStripContainer1.ContentPanel");
        this.toolStripContainer1.ContentPanel.add(this.dgvList);
//        //resources.ApplyResources(this.toolStripContainer1, "toolStripContainer1");
        this.toolStripContainer1.setName("toolStripContainer1");
        //
        // toolStripContainer1.TopToolStripPanel
        //
        this.toolStripContainer1.TopToolStripPanel.add(this.toolStrip1);
        //
        // dgvList
        //
        this.dgvList.AllowDrop = true;
        this.dgvList.AllowUserToAddRows = false;
        this.dgvList.AllowUserToDeleteRows = false;
        this.dgvList.AllowUserToResizeRows = false;
        this.dgvList.BackgroundColor = Color.black;
        this.dgvList.BorderStyle = JBorderStyle.None;
        this.dgvList.CellBorderStyle = JTableCellBorderStyle.None;
        JListCellStyle1.Alignment = JTableContentAlignment.MiddleLeft;
        JListCellStyle1.setBackground(Color.black);
        JListCellStyle1.Font = new Font("メイリオ", 8.25F, FontStyle.Regular, GraphicsUnit.Point, ((byte) (128)));
        JListCellStyle1.ForeColor = Color.MenuHighlight;
        JListCellStyle1.Selectio.setBackground(Color.Highlight);
        JListCellStyle1.SelectionForeColor = Color.HighlightText;
        JListCellStyle1.WrapMode = JTableTriState.False;
        this.dgvList.ColumnHeadersDefaultCellStyle = JListCellStyle1;
//        //resources.ApplyResources(this.dgvList, "dgvList");
        this.dgvList.ColumnHeadersHeightSizeMode = JTableColumnHeadersHeightSizeMode.DisableResizing;
        this.dgvList.Columns.AddRange(new JTableColumn[] {
                this.clmKey,
                this.clmSongNo,
                this.clmZipFileName,
                this.clmFileName,
                this.clmPlayingNow,
                this.clmEXT,
                this.clmType,
                this.clmTitle,
                this.clmTitleJ,
                this.clmDispFileName,
                this.clmGame,
                this.clmGameJ,
                this.clmComposer,
                this.clmComposerJ,
                this.clmVGMby,
                this.clmConverted,
                this.clmNotes,
                this.clmDuration,
                this.clmSpacer});
        this.dgvList.EditMode = JTableEditMode.EditProgrammatically;
        this.dgvList.setName("dgvList");
        this.dgvList.RowHeadersBorderStyle = JTableHeaderBorderStyle.None;
        JListCellStyle3.Alignment = JTableContentAlignment.MiddleLeft;
        JListCellStyle3.setBackground(Color.black);
        JListCellStyle3.setFont(new Font("メイリオ", 8.25F, Font.BOLD, GraphicsUnit.Point, ((byte) (128))));
        JListCellStyle3.ForeColor = Color.Window;
        JListCellStyle3.Selectio.setBackground(Color.Highlight);
        JListCellStyle3.SelectionForeColor = Color.HighlightText;
        JListCellStyle3.WrapMode = JTableTriState.True;
        this.dgvList.RowHeadersDefaultCellStyle = JListCellStyle3;
        this.dgvList.RowHeadersVisible = false;
        JListCellStyle4.setBackground(Color.black);
        JListCellStyle4.Font = new Font("メイリオ", 8.25F, Font.BOLD, GraphicsUnit.Point, ((byte) (128)));
        JListCellStyle4.ForeColor = new Color(((byte) (192)), ((byte) (192)), ((byte) (255)));
        this.dgvList.RowsDefaultCellStyle = JListCellStyle4;
        this.dgvList.RowTemplate.ContextMenuStrip = this.cmsPlayList;
        this.dgvList.RowTemplate.DefaultCellStyle.Alignment = JTableContentAlignment.MiddleLeft;
        this.dgvList.RowTemplate.getHeight() = 10;
        this.dgvList.RowTemplate.setEditable(Xtrue);
        this.dgvList.SelectionMode = JTableSelectionMode.FullRowSelect;
        this.dgvList.ShowCellErrors = false;
        this.dgvList.ShowEditingIcon = false;
        this.dgvList.ShowRowErrors = false;
        this.dgvList.CellDoubleClick += new JTableCellEventHandler(this.dgvList_CellDoubleClick);
        this.dgvList.addMouseListener(this.dgvList_CellMouseClick);
        this.dgvList.addDragDrop += new JDragEventHandler(this.dgvList_DragDrop);
        this.dgvList.DragEnter += new JDragEventHandler(this.dgvList_DragEnter);
        this.dgvList.DragOver += new JDragEventHandler(this.dgvList_DragOver);
        //
        // clmKey
        //
        //resources.ApplyResources(this.clmKey, "clmKey");
        this.clmKey.setName("clmKey");
        this.clmKey.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmSongNo
        //
        //resources.ApplyResources(this.clmSongNo, "clmSongNo");
        this.clmSongNo.setName("clmSongNo");
        //
        // clmZipFileName
        //
        //resources.ApplyResources(this.clmZipFileName, "clmZipFileName");
        this.clmZipFileName.setName("clmZipFileName");
        //
        // clmFileName
        //
        //resources.ApplyResources(this.clmFileName, "clmFileName");
        this.clmFileName.setName("clmFileName");
        this.clmFileName.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmPlayingNow
        //
//            this.clmPlayingNow.AutoSizeMode = JTableAutoSizeColumnMode.None;
        //resources.ApplyResources(this.clmPlayingNow, "clmPlayingNow");
        this.clmPlayingNow.setName("clmPlayingNow");
        this.clmPlayingNow.Resizable = JTableTriState.False;
        this.clmPlayingNow.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmEXT
        //
        //resources.ApplyResources(this.clmEXT, "clmEXT");
        this.clmEXT.setName("clmEXT");
        this.clmEXT.setEditable(false);
        this.clmEXT.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmType
        //
        //resources.ApplyResources(this.clmType, "clmType");
        this.clmType.setName("clmType");
        this.clmType.setEditable(false);
        this.clmType.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmTitle
        //
        //resources.ApplyResources(this.clmTitle, "clmTitle");
        this.clmTitle.setName("clmTitle");
        this.clmTitle.setEditable(false);
        this.clmTitle.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmTitleJ
        //
        //resources.ApplyResources(this.clmTitleJ, "clmTitleJ");
        this.clmTitleJ.setName("clmTitleJ");
        this.clmTitleJ.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmDispFileName
        //
        //resources.ApplyResources(this.clmDispFileName, "clmDispFileName");
        this.clmDispFileName.setName("clmDispFileName");
        //
        // clmGame
        //
        //resources.ApplyResources(this.clmGame, "clmGame");
        this.clmGame.setName("clmGame");
        this.clmGame.setEditable(false);
        this.clmGame.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmGameJ
        //
        //resources.ApplyResources(this.clmGameJ, "clmGameJ");
        this.clmGameJ.setName("clmGameJ");
        this.clmGameJ.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmComposer
        //
        //resources.ApplyResources(this.clmComposer, "clmComposer");
        this.clmComposer.setName("clmComposer");
        this.clmComposer.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmComposerJ
        //
        //resources.ApplyResources(this.clmComposerJ, "clmComposerJ");
        this.clmComposerJ.setName("clmComposerJ");
//            this.clmComposerJ.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmVGMby
        //
        //resources.ApplyResources(this.clmVGMby, "clmVGMby");
        this.clmVGMby.setName("clmVGMby");
//            this.clmVGMby.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmConverted
        //
        //resources.ApplyResources(this.clmConverted, "clmConverted");
        this.clmConverted.setName("clmConverted");
//            this.clmConverted.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmNotes
        //
        //resources.ApplyResources(this.clmNotes, "clmNotes");
        this.clmNotes.setName("clmNotes");
        this.clmNotes.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmDuration
        //
        JListCellStyle2.Alignment = JTableContentAlignment.MiddleRight;
        this.clmDuration.DefaultCellStyle = JListCellStyle2;
        //resources.ApplyResources(this.clmDuration, "clmDuration");
        this.clmDuration.setName("clmDuration");
        this.clmDuration.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmSpacer
        //
        this.clmSpacer.AutoSizeMode = JTableAutoSizeColumnMode.Fill;
        //resources.ApplyResources(this.clmSpacer, "clmSpacer");
        this.clmSpacer.setName("clmSpacer");
        this.clmSpacer.setEditable(false);
        this.clmSpacer.SortMode = JTableColumnSortMode.NotSortable;
        //
        // cmsPlayList
        //
        this.cmsPlayList.Items.AddRange(new JToolStripItem[] {
                this.type設定ToolStripMenuItem,
                this.toolStripSeparator5,
                this.tsmiPlayThis,
                this.tsmiDelThis,
                this.toolStripSeparator3,
                this.tsmiDelAllMusic,
                this.tsmiOpenFolder});
        this.cmsPlayList.setName("cmsPlayList");
        //resources.ApplyResources(this.cmsPlayList, "cmsPlayList");
        //
        // type設定ToolStripMenuItem
        //
        this.type設定ToolStripMenuItem.DropDownItems.AddRange(new JToolStripItem[] {
                this.tsmiA,
                this.tsmiB,
                this.tsmiC,
                this.tsmiD,
                this.tsmiE,
                this.tsmiF,
                this.tsmiG,
                this.tsmiH,
                this.tsmiI,
                this.tsmiJ});
        this.type設定ToolStripMenuItem.setName("type設定ToolStripMenuItem");
        //resources.ApplyResources(this.type設定ToolStripMenuItem, "type設定ToolStripMenuItem");
        //
        // tsmiA
        //
        this.tsmiA.setName("tsmiA");
        //resources.ApplyResources(this.tsmiA, "tsmiA");
        this.tsmiA.addActionListener(this::tsmiA_Click);
        //
        // tsmiB
        //
        this.tsmiB.setName("tsmiB");
        //resources.ApplyResources(this.tsmiB, "tsmiB");
        this.tsmiB.addActionListener(this::tsmiA_Click);
        //
        // tsmiC
        //
        this.tsmiC.setName("tsmiC");
        //resources.ApplyResources(this.tsmiC, "tsmiC");
        this.tsmiC.addActionListener(this::tsmiA_Click);
        //
        // tsmiD
        //
        this.tsmiD.setName("tsmiD");
        //resources.ApplyResources(this.tsmiD, "tsmiD");
        this.tsmiD.addActionListener(this::tsmiA_Click);
        //
        // tsmiE
        //
        this.tsmiE.setName("tsmiE");
        //resources.ApplyResources(this.tsmiE, "tsmiE");
        this.tsmiE.addActionListener(this::tsmiA_Click);
        //
        // tsmiF
        //
        this.tsmiF.setName("tsmiF");
        //resources.ApplyResources(this.tsmiF, "tsmiF");
        this.tsmiF.addActionListener(this::tsmiA_Click);
        //
        // tsmiG
        //
        this.tsmiG.setName("tsmiG");
        //resources.ApplyResources(this.tsmiG, "tsmiG");
        this.tsmiG.addActionListener(this::tsmiA_Click);
        //
        // tsmiH
        //
        this.tsmiH.setName("tsmiH");
        //resources.ApplyResources(this.tsmiH, "tsmiH");
        this.tsmiH.addActionListener(this::tsmiA_Click);
        //
        // tsmiI
        //
        this.tsmiI.setName("tsmiI");
        //resources.ApplyResources(this.tsmiI, "tsmiI");
        this.tsmiI.addActionListener(this::tsmiA_Click);
        //
        // tsmiJ
        //
        this.tsmiJ.setName("tsmiJ");
        //resources.ApplyResources(this.tsmiJ, "tsmiJ");
        this.tsmiJ.addActionListener(this::tsmiA_Click);
        //
        // toolStripSeparator5
        //
        this.toolStripSeparator5.setName("toolStripSeparator5");
        //resources.ApplyResources(this.toolStripSeparator5, "toolStripSeparator5");
        //
        // tsmiPlayThis
        //
        this.tsmiPlayThis.setName("tsmiPlayThis");
        //resources.ApplyResources(this.tsmiPlayThis, "tsmiPlayThis");
        this.tsmiPlayThis.addActionListener(this::tsmiPlayThis_Click);
        //
        // tsmiDelThis
        //
        this.tsmiDelThis.setName("tsmiDelThis");
        //resources.ApplyResources(this.tsmiDelThis, "tsmiDelThis");
        this.tsmiDelThis.addActionListener(this::tsmiDelThis_Click);
        //
        // toolStripSeparator3
        //
        this.toolStripSeparator3.setName("toolStripSeparator3");
        //resources.ApplyResources(this.toolStripSeparator3, "toolStripSeparator3");
        //
        // tsmiDelAllMusic
        //
        this.tsmiDelAllMusic.setName("tsmiDelAllMusic");
        //resources.ApplyResources(this.tsmiDelAllMusic, "tsmiDelAllMusic");
        this.tsmiDelAllMusic.addActionListener(this::tsmiDelAllMusic_Click);
        //
        // tsmiOpenFolder
        //
        this.tsmiOpenFolder.setName("tsmiOpenFolder");
        //resources.ApplyResources(this.tsmiOpenFolder, "tsmiOpenFolder");
        this.tsmiOpenFolder.addActionListener(this::tsmiOpenFolder_Click);
        //
        // toolStrip1
        //
        //resources.ApplyResources(this.toolStrip1, "toolStrip1");
        this.toolStrip1.GripStyle = JToolStripGripStyle.Hidden;
        this.toolStrip1.Items.AddRange(new JToolStripItem[] {
                this.tsbOpenPlayList,
                this.tsbSavePlayList,
                this.toolStripSeparator1,
                this.tsbAddMusic,
                this.tsbAddFolder,
                this.toolStripSeparator2,
                this.tsbUp,
                this.tsbDown,
                this.toolStripSeparator4,
                this.tsbJapanese,
                this.toolStripSeparator6,
                this.tsbTextExt,
                this.tsbMMLExt,
                this.tsbImgExt});
        this.toolStrip1.setName("toolStrip1");
        this.toolStrip1.Stretch = true;
        //
        // tsbOpenPlayList
        //
        this.tsbOpenPlayList.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbOpenPlayList.setIcon(new ImageIcon(mdplayer.properties.Resources.getopenPL()));
        //resources.ApplyResources(this.tsbOpenPlayList, "tsbOpenPlayList");
        this.tsbOpenPlayList.setName("tsbOpenPlayList");
        this.tsbOpenPlayList.addActionListener(this::tsbOpenPlayList_Click);
        //
        // tsbSavePlayList
        //
        this.tsbSavePlayList.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbSavePlayList.setIcon(new ImageIcon(mdplayer.properties.Resources.getsavePL()));
        //resources.ApplyResources(this.tsbSavePlayList, "tsbSavePlayList");
        this.tsbSavePlayList.setName("tsbSavePlayList");
        this.tsbSavePlayList.addActionListener(this::tsbSavePlayList_Click);
        //
        // toolStripSeparator1
        //
        this.toolStripSeparator1.setName("toolStripSeparator1");
        //resources.ApplyResources(this.toolStripSeparator1, "toolStripSeparator1");
        //
        // tsbAddMusic
        //
        this.tsbAddMusic.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbAddMusic.setIcon(new ImageIcon(mdplayer.properties.Resources.getaddPL()));
        //resources.ApplyResources(this.tsbAddMusic, "tsbAddMusic");
        this.tsbAddMusic.setName("tsbAddMusic");
        this.tsbAddMusic.addActionListener(this::tsbAddMusic_Click);
        //
        // tsbAddFolder
        //
        this.tsbAddFolder.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbAddFolder.setIcon(new ImageIcon(mdplayer.properties.Resources.getaddFolderPL()));
        //resources.ApplyResources(this.tsbAddFolder, "tsbAddFolder");
        this.tsbAddFolder.setName("tsbAddFolder");
        this.tsbAddFolder.addActionListener(this::tsbAddFolder_Click);
        //
        // toolStripSeparator2
        //
        this.toolStripSeparator2.setName("toolStripSeparator2");
        //resources.ApplyResources(this.toolStripSeparator2, "toolStripSeparator2");
        //
        // tsbUp
        //
        this.tsbUp.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbUp.setIcon(new ImageIcon(mdplayer.properties.Resources.getupPL()));
        //resources.ApplyResources(this.tsbUp, "tsbUp");
        this.tsbUp.setName("tsbUp");
        this.tsbUp.addActionListener(this::tsbUp_Click);
        //
        // tsbDown
        //
        this.tsbDown.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbDown.setIcon(new ImageIcon(mdplayer.properties.Resources.getDownPL()));
        //resources.ApplyResources(this.tsbDown, "tsbDown");
        this.tsbDown.setName("tsbDown");
        this.tsbDown.addActionListener(this::tsbDown_Click);
        //
        // toolStripSeparator4
        //
        this.toolStripSeparator4.setName("toolStripSeparator4");
        //resources.ApplyResources(this.toolStripSeparator4, "toolStripSeparator4");
        //
        // tsbJapanese
        //
        this.tsbJapanese.CheckOnClick = true;
        this.tsbJapanese.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbJapanese.setIcon(new ImageIcon(mdplayer.properties.Resources.getjapPL()));
        //resources.ApplyResources(this.tsbJapanese, "tsbJapanese");
        this.tsbJapanese.setName("tsbJapanese");
        this.tsbJapanese.addActionListener(this::toolStripButton1_Click);
        //
        // toolStripSeparator6
        //
        this.toolStripSeparator6.setName("toolStripSeparator6");
        //resources.ApplyResources(this.toolStripSeparator6, "toolStripSeparator6");
        //
        // tsbTextExt
        //
        this.tsbTextExt.DisplayStyle = JToolStripItemDisplayStyle.Image;
        //resources.ApplyResources(this.tsbTextExt, "tsbTextExt");
        this.tsbTextExt.setIcon(new ImageIcon(mdplayer.properties.Resources.gettxtPL()));
        this.tsbTextExt.setName("tsbTextExt");
        this.tsbTextExt.addActionListener(this::tsbTextExt_Click);
        //
        // tsbMMLExt
        //
        this.tsbMMLExt.DisplayStyle = JToolStripItemDisplayStyle.Image;
        //resources.ApplyResources(this.tsbMMLExt, "tsbMMLExt");
        this.tsbMMLExt.setIcon(new ImageIcon(mdplayer.properties.Resources.getmmlPL()));
        this.tsbMMLExt.setName("tsbMMLExt");
        this.tsbMMLExt.addActionListener(this::tsbMMLExt_Click);
        //
        // tsbImgExt
        //
        this.tsbImgExt.DisplayStyle = JToolStripItemDisplayStyle.Image;
        //resources.ApplyResources(this.tsbImgExt, "tsbImgExt");
        this.tsbImgExt.setIcon(new ImageIcon(mdplayer.properties.Resources.getimgPL()));
        this.tsbImgExt.setName("tsbImgExt");
        this.tsbImgExt.addActionListener(this::tsbImgExt_Click);
        //
        // timer1
        //
        this.timer1.addActionListener(this::timer1_Tick);
        this.timer1.start();
        //
        // frmPlayList
        //
        //resources.ApplyResources(this, "$this");
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.getContentPane().add(this.toolStripContainer1);
        this.KeyPreview = true;
        this.setName("frmPlayList");
        this.setOpacity(0);
        this.addWindowListener(this.windowListener);
        this.addKeyListener(this.frmPlayList_KeyDown);
        // this.toolStripContainer1.ContentPanel.ResumeLayout(false);
        // this.toolStripContainer1.TopToolStripPanel.ResumeLayout(false);
        // this.toolStripContainer1.TopToolStripPanel.PerformLayout();
        // this.toolStripContainer1.ResumeLayout(false);
        // this.toolStripContainer1.PerformLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvList)).EndInit();
        // this.cmsPlayList.ResumeLayout(false);
        // this.toolStrip1.ResumeLayout(false);
        // this.toolStrip1.PerformLayout();
//        this.ResumeLayout(false);
    }

    private JTable dgvList;
    private JMenu cmsPlayList;
    private JMenuItem tsmiPlayThis;
    private JMenuItem tsmiDelThis;
    private JToolBar toolStripContainer1;
    private JToolBar toolStrip1;
    private JButton tsbOpenPlayList;
    private JButton tsbSavePlayList;
    private JSeparator toolStripSeparator1;
    private JButton tsbAddMusic;
    private JSeparator toolStripSeparator2;
    private JButton tsbUp;
    private JButton tsbDown;
    private JSeparator toolStripSeparator3;
    private JMenuItem tsmiDelAllMusic;
    private JButton tsbAddFolder;
    private JSeparator toolStripSeparator4;
    private JButton tsbJapanese;
    private JMenuItem type設定ToolStripMenuItem;
    private JMenuItem tsmiA;
    private JMenuItem tsmiB;
    private JMenuItem tsmiC;
    private JMenuItem tsmiD;
    private JMenuItem tsmiE;
    private JMenuItem tsmiF;
    private JMenuItem tsmiG;
    private JMenuItem tsmiH;
    private JMenuItem tsmiI;
    private JMenuItem tsmiJ;
    private JSeparator toolStripSeparator5;
    private JSeparator toolStripSeparator6;
    private JButton tsbTextExt;
    private JButton tsbMMLExt;
    private JButton tsbImgExt;
    private Timer timer1;
    private JMenuItem tsmiOpenFolder;
    private JTextArea clmKey;
    private JTextArea clmSongNo;
    private JTextArea clmZipFileName;
    private JTextArea clmFileName;
    private JTextArea clmPlayingNow;
    private JTextArea clmEXT;
    private JTextArea clmType;
    private JTextArea clmTitle;
    private JTextArea clmTitleJ;
    private JTextArea clmDispFileName;
    private JTextArea clmGame;
    private JTextArea clmGameJ;
    private JTextArea clmComposer;
    private JTextArea clmComposerJ;
    private JTextArea clmVGMby;
    private JTextArea clmConverted;
    private JTextArea clmNotes;
    private JTextArea clmDuration;
    private JTextArea clmSpacer;
}
