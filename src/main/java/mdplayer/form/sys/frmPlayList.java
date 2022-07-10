package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import dotnet4j.io.Directory;
import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import dotnet4j.util.compat.Tuple4;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmArcType;
import mdplayer.MDChipParams;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.format.FileFormat;
import mdplayer.properties.Resources;
import vavi.awt.dnd.BasicDTListener;


public class frmPlayList extends JFrame {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public Setting setting;

    public String playFilename = "";
    public String playArcFilename = "";
    public FileFormat playFormat = FileFormat.unknown;
    public EnmArcType playArcType = EnmArcType.unknown;
    public int playSongNum = -1;

    private PlayList playList = null;
    private frmMain frmMain = null;

    private boolean playing = false;
    private int playIndex = -1;
    private int oldPlayIndex = -1;

    private Random rand = new Random();
    private boolean IsInitialOpenFolder = true;

    static Preferences prefs = Preferences.userNodeForPackage(frmPlayList.class);

    private static final String[] sext = ".vgm;.vgz;.zip;.lzh;.nrd;.xgm;.zgm;.s98;.nsf;.hes;.sid;.mnd;.mgs;.mdr;.mdx;.mub;.muc;.m;.m2;.mz;.mml;.mid;.rcp;.wav;.mp3;.aiff;.m3u".split(";");

    public frmPlayList(frmMain frm) {
        frmMain = frm;
        setting = frm.setting;
        initializeComponent();

        playList = PlayList.Load(null);
        playList.setDGV(dgvList);
        playIndex = -1;

        oldPlayIndex = -1;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getMusicCount() {
        return playList.getMusics().size();
    }

    public PlayList getPlayList() {
        return playList;
    }

    public Tuple4<Integer, Integer, String, String> setStart(int n) {
        updatePlayingIndex(n);

        String fn = playList.getMusics().get(playIndex).fileName;
        String zfn = playList.getMusics().get(playIndex).arcFileName;
        int m = 0;
        int songNo = playList.getMusics().get(playIndex).songNo;

        if (playList.getMusics().get(playIndex).type != null && !playList.getMusics().get(playIndex).type.equals("-")) {
            m = playList.getMusics().get(playIndex).type.charAt(0) - 'A';
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

    public void save() {
        if (setting.getOther().getEmptyPlayList()) {
            playList.setMusics(new ArrayList<>());
        }
        playList.save(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    public List<Tuple<String, String>> randomStack = new ArrayList<>();

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
//            e.Cancel = true;
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

    public void refresh() {
        DefaultTableModel m = (DefaultTableModel) dgvList.getModel();
        m.setRowCount(0);
        List<Object[]> rows = playList.makeRow(playList.getMusics());
        for (Object[] row : rows) {
            m.addRow(row);
        }
    }

    public void updatePlayingIndex(int newPlayingIndex) {
        if (oldPlayIndex != -1) {
            ResetColor(oldPlayIndex);
        }

        if (newPlayingIndex >= 0 && newPlayingIndex < dgvList.getRowCount()) {
            SetColor(newPlayingIndex);
        } else if (newPlayingIndex == -1) {
            newPlayingIndex = dgvList.getRowCount() - 1;
            SetColor(newPlayingIndex);
        } else if (newPlayingIndex == -2) {
            newPlayingIndex = 0;
            SetColor(newPlayingIndex);
        }
        playIndex = newPlayingIndex;
        oldPlayIndex = newPlayingIndex;
    }

    private void SetColor(int rowIndex) {
        dgvList.setValueAt(">", rowIndex, cols.clmPlayingNow.ordinal());
        for (int i = 0; i < dgvList.getColumnCount(); i++) {
            Component c = dgvList.getCellRenderer(rowIndex, i).getTableCellRendererComponent(dgvList, dgvList.getValueAt(rowIndex, i), true, false, rowIndex, i);
            c.setForeground(Color.green.brighter());
//            dgvList.Rows[rowIndex].Cells[i].Style.SelectionForeColor = Color.green.brighter();
        }
    }

    private static Color clrLightBlue = new Color(255, 192, 192, 255);

    private void ResetColor(int rowIndex) {
        dgvList.setValueAt(" ", rowIndex, cols.clmPlayingNow.ordinal());
        for (int i = 0; i < dgvList.getColumnCount(); i++) {
            Component c = dgvList.getCellRenderer(rowIndex, i).getTableCellRendererComponent(dgvList, dgvList.getValueAt(rowIndex, i), true, false, rowIndex, i);
            c.setForeground(clrLightBlue);
//            dgvList.Rows[rowIndex].Cells[i].Style.SelectionForeColor = Color.white;
        }
    }

    private MouseListener dgvList_CellMouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (dgvList.getSelectedRowCount() < 0) return;
            dgvList.setRowSelectionInterval(dgvList.getSelectedRow(), dgvList.getSelectedRow()); // TODO

            if (e.getButton() == MouseEvent.BUTTON2) {
                if (dgvList.getSelectedRowCount() > 1) {
                    tsmiDelThis.setText("選択した曲を除去");
                } else {
                    tsmiDelThis.setText("この曲を除去");
                }
                cmsPlayList.setLocation(new Point(e.getX(), e.getY()));
                cmsPlayList.setVisible(true);
            }
        }
    };

    private void tsmiDelThis_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 1) return;

        List<Integer> sel = new ArrayList<>();
        for (int r : dgvList.getSelectedRows()) {
            sel.add(r);
        }
        Collections.sort(sel);

        for (int i = sel.size() - 1; i >= 0; i--) {
            if (oldPlayIndex >= dgvList.getSelectedRows()[i]) {
                oldPlayIndex--;
            }
            if (playIndex >= dgvList.getSelectedRows()[i]) {
                playIndex--;
            }
            playList.getMusics().remove(dgvList.getSelectedRows()[i]);
            ((DefaultTableModel) dgvList.getModel()).removeRow(dgvList.getSelectedRows()[i]);
        }
    }

    public void nextPlay() {
        if (!playing) return;
        if (dgvList.getRowCount() == playIndex + 1) return;

        int pi = playIndex;
        playing = false;

        pi++;

        String fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
        String zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
        int m = 0;
        int songNo = 0;
        try {
            songNo = (int) dgvList.getValueAt(pi, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
            songNo = 0;
        }
        if (dgvList.getValueAt(pi, cols.clmType.ordinal()) != null && !dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().equals("-")) {
            m = dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        if (!Audio.getInstance().errMsg.isEmpty()) {
            playing = false;
            return;
        }
        updatePlayingIndex(pi);
        playing = true;

        playFilename = fn;
        playArcFilename = zfn;
        playSongNum = songNo;
        //playFormat = dgvList.Rows[pi].Cells[cols.clmSongNo.ordinal()].Value;
        //playArcType = dgvList.Rows[pi].Cells[cols.clmSongNo.ordinal()].Value;
    }

    public void nextPlayMode(int mode) {
        if (!playing) {
            playIndex = -1;
        }

        int pi = playIndex;
        playing = false;
        String fn, zfn;

        switch (mode) {
        case 0: // 通常
            if (dgvList.getRowCount() <= playIndex + 1) return;
            pi++;
            break;
        case 1: // ランダム

            if (pi != -1) {
                // 再生履歴の更新
                fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
                zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());

                randomStack.add(new Tuple<>(fn, zfn));
                while (randomStack.size() > 1000)
                    randomStack.remove(0);
            }

            pi = rand.nextInt(dgvList.getRowCount());
            break;
        case 2: // 全曲ループ
            pi++;
            if (pi >= dgvList.getRowCount()) {
                pi = 0;
            }
            break;
        case 3: // １曲ループ
            break;
        }

        if (pi + 1 > dgvList.getRowCount()) {
            playing = false;
            return;
        }

        fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
        zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
        int m = 0;
        if (dgvList.getValueAt(pi, cols.clmType.ordinal()) != null && !dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().equals("-")) {
            m = dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo = 0;
        try {
            songNo = (int) dgvList.getValueAt(pi, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
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
            fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
            zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
        } else {
            pi = 0;
loopEx:
            if (randomStack.size() > 0) {
                while (true) {
                    String hfn = randomStack.get(randomStack.size() - 1).getItem1();
                    String hzfn = randomStack.get(randomStack.size() - 1).getItem2();
                    randomStack.remove(randomStack.size() - 1);

                    for (; pi < dgvList.getRowCount(); pi++) {
                        fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
                        zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
                        if (hfn.equals(fn) && hzfn.equals(zfn)) {
                            break loopEx;
                        }
                    }

                    if (randomStack.size() == 0) break;
                }

                if (playIndex < 1) return;
                pi = playIndex - 1;
                fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
                zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
            } else {
                pi = playIndex;
                pi--;
                if (pi < 0) pi = 0;
                fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
                zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
            }
        }

        int m = 0;
        if (dgvList.getValueAt(pi, cols.clmType.ordinal()) != null && !dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().equals("-")) {
            m = dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo = 0;
        try {
            songNo = (int) dgvList.getValueAt(pi, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
            songNo = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        updatePlayingIndex(pi);
        playing = true;
    }

    private void dgvList_CellDoubleClick(ActionEvent e) {
//        if (e.RowIndex < 0) return;

        dgvList.setEnabled(false);

        playing = false;

//        try {
//            String fn = (String) dgvList.getValueAt(e.RowIndex, cols.clmFileName.ordinal());
//            String zfn = (String) dgvList.getValueAt(e.RowIndex, cols.clmZipFileName.ordinal());
//            int m = 0;
//            int songNo = 0;
//            try {
//                songNo = (int) dgvList.getValueAt(e.RowIndex, cols.clmSongNo.ordinal());
//            } catch (Exception ex) {
//                songNo = 0;
//            }
//            if (dgvList.getValueAt(e.RowIndex, cols.clmType.ordinal()) != null && !dgvList.getValueAt(e.RowIndex, cols.clmType.ordinal()).toString().equals("-")) {
//                m = dgvList.getValueAt(e.RowIndex, cols.clmType.ordinal()).toString()[0] - 'A';
//                if (m < 0 || m > 9) m = 0;
//            }
//
//            if (!frmMain.loadAndPlay(m, songNo, fn, zfn)) return;
//            updatePlayingIndex(e.RowIndex);
//
//            playing = true;
//        } finally {
//            //dgvList.MultiSelect = true;
//            dgvList.setEnabled(true);
//            dgvList.Rows[e.RowIndex].Selected = true;
//        }
    }

    private void tsmiPlayThis_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 0) return;

        playing = false;

        String fn = (String) dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmFileName.ordinal());
        String zfn = (String) dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmZipFileName.ordinal());
        int m = 0;
        if (dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmType.ordinal()) != null && !dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmType.ordinal()).toString().equals("-")) {
            m = dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmType.ordinal()).toString().charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo;
        try {
            songNo = (int) dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmSongNo.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
            songNo = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        updatePlayingIndex(dgvList.getSelectedRows()[0]);

        playing = true;
    }

    private void tsmiDelAllMusic_Click(ActionEvent ev) {

        int res = JOptionPane.showConfirmDialog(null, "プレイリストの全ての曲が除去されます。よろしいですか。", "PlayList",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res != JFileChooser.APPROVE_OPTION) return;

        playing = false;
        ((DefaultTableModel) dgvList.getModel()).setRowCount(0);
        playList.getMusics().clear();
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
                playList.setDGV(dgvList);
            } else {
                pl = PlayList.LoadM3U(filename);
                playing = false;
                playList.getMusics().clear();
                for (PlayList.Music ms : pl.getMusics()) {
                    playList.addFile(ms.fileName);
                    //addList(ms.fileName);
                }
            }

            playIndex = -1;
            oldPlayIndex = -1;

            refresh();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "ファイルの読み込みに失敗しました。");
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "ファイルの保存に失敗しました。");
        }
    }

    private void tsbAddMusic_Click(ActionEvent ev) {

        JFileChooser ofd = new JFileChooser();
        Arrays.stream(Resources.getCntSupportFile().split("\\s")).forEach(l -> {
            String[] p = l.split("\\|");
            ofd.setFileFilter(new FileFilter() {
                @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(p[1]); }
                @Override public String getDescription() { return p[0]; }
            });
        });
        ofd.setDialogTitle("ファイルを選択してください");
        ofd.setFileFilter(ofd.getChoosableFileFilters()[setting.getOther().getFilterIndex()]);

        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(frmMain.setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
            ofd.setCurrentDirectory(new java.io.File(frmMain.setting.getOther().getDefaultDataPath()));
//        } else {
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
                playList.addFile(fn.getPath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Play();
    }

    static final String[] _exts = {
            ".vgm", ".vgz", ".zip", ".nrd",
            ".xgm", ".s98", ".nsf", ".hes",
            ".sid", ".mid", ".rcp", ".m3u",
            ".mdr"
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
                    playList.addFile(p.toFile().getAbsolutePath());
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        frmMain.oldParam = new MDChipParams();

        play();
    }

    private void tsbUp_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 1 || dgvList.getSelectedRows()[0] < 1) {
            return;
        }

        int ind = dgvList.getSelectedRows()[0];
        PlayList.Music mus = playList.getMusics().get(ind - 1);
        DefaultTableModel m = (DefaultTableModel) dgvList.getModel();
        int row = ind - 1;

        if (ind == playIndex) playIndex--;
        else if (ind == playIndex + 1) playIndex++;

        if (ind == oldPlayIndex) oldPlayIndex--;
        else if (ind == oldPlayIndex + 1) oldPlayIndex++;

        playList.getMusics().remove(ind - 1);

        playList.getMusics().add(ind, mus);
        m.moveRow(row, row, ind);
    }

    private void tsbDown_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() != 1 || dgvList.getSelectedRows()[0] >= dgvList.getRowCount() - 1) {
            return;
        }

        int ind = dgvList.getSelectedRows()[0];
        PlayList.Music mus = playList.getMusics().get(ind + 1);
        DefaultTableModel m = (DefaultTableModel) dgvList.getModel();
        int row = ind + 1;

        if (ind == playIndex) playIndex++;
        else if (ind == playIndex - 1) playIndex--;

        if (ind == oldPlayIndex) oldPlayIndex++;
        else if (ind == oldPlayIndex - 1) oldPlayIndex--;

        playList.getMusics().remove(ind + 1);

        playList.getMusics().add(ind, mus);
        m.moveRow(row, row, ind);
    }

    private void toolStripButton1_Click(ActionEvent ev) {
//        dgvList.getColumn(cols.clmTitle.ordinal()).setVisible = !tsbJapanese.isSelected();
//        dgvList.Columns[cols.clmTitleJ.ordinal()].Visible = tsbJapanese.isSelected();
//        dgvList.Columns[cols.clmGame.ordinal()].Visible = !tsbJapanese.isSelected();
//        dgvList.Columns[cols.clmGameJ.ordinal()].Visible = tsbJapanese.isSelected();
//        dgvList.Columns[cols.clmComposer.ordinal()].Visible = !tsbJapanese.isSelected();
//        dgvList.Columns[cols.clmComposerJ.ordinal()].Visible = tsbJapanese.isSelected();
    }

    private KeyListener frmPlayList_KeyDown = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            //System.err.println("keycode%d %d %d", e.KeyCode, e.KeyData, e.KeyValue);

            switch (e.getKeyCode()) {
            case 32: // Space
            case 13: // Enter
                if (dgvList.getSelectedRowCount() == 0) {
                    return;
                }

                int index = dgvList.getSelectedRows()[0];

//                e.Handled = true;

                playing = false;

                String fn = (String) dgvList.getValueAt(index, cols.clmFileName.ordinal());
                String zfn = (String) dgvList.getValueAt(index, cols.clmZipFileName.ordinal());
                int m = 0;
                if (dgvList.getValueAt(index, cols.clmType.ordinal()) != null && !dgvList.getValueAt(index, cols.clmType.ordinal()).toString().equals("-")) {
                    m = dgvList.getValueAt(index, cols.clmType.ordinal()).toString().charAt(0) - 'A';
                    if (m < 0 || m > 9) m = 0;
                }
                int songNo = 0;
                try {
                    songNo = (int) dgvList.getValueAt(index, cols.clmSongNo.ordinal());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    songNo = 0;
                }

                frmMain.loadAndPlay(m, songNo, fn, zfn);
                updatePlayingIndex(index);

                playing = true;
                break;
            case 46: // Delete
//                e.Handled = true;
                tsmiDelThis_Click(null);
                break;
            }
        }
    };

    private BasicDTListener dgvList_DragDrop = new BasicDTListener() {
        @Override
        protected boolean isDragFlavorSupported(DropTargetDragEvent ev) {
            return ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        protected DataFlavor chooseDropFlavor(DropTargetDropEvent ev) {
            if (ev.isLocalTransfer() && ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return DataFlavor.javaFileListFlavor;
            }
            DataFlavor chosen = null;
            if (ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                chosen = DataFlavor.javaFileListFlavor;
            }
            return chosen;
        }

        @Override
        public void dragEnter(DropTargetDragEvent e) {
//            e.Effect = DragDropEffects.All;
//            Point cp = dgvList.PointToClient(new Point(e.getX(), e.getY()));
//            JList.HitTestInfo hti = dgvList.HitTest(cp.x, cp.y);
//            if (hti.Type != JListHitTestType.Cell || hti.RowIndex < 0 || hti.RowIndex >= dgvList.Rows.size()) return;
//            dgvList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//            dgvList.MultiSelect = true;
//            dgvList.Rows[hti.RowIndex].Selected = true;
            super.dragEnter(e);
        }

        @Override
        public void dragOver(DropTargetDragEvent e) {
//            e.Effect = DragDropEffects.All;
//            Point cp = dgvList.PointToClient(e.getLocation());
//            JList.HitTestInfo hti = dgvList.HitTest(cp.x, cp.y);
//            if (hti.Type != JListHitTestType.Cell || hti.RowIndex < 0 || hti.RowIndex >= dgvList.Rows.size()) return;
//            dgvList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//            dgvList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//            dgvList.Rows[hti.RowIndex].Selected = true;
            super.dragOver(e);
        }

        private final Object relock = new Object();
        private boolean reent = false;

        @Override
        protected boolean dropImpl(DropTargetDropEvent e, Object data) {
            synchronized (relock) {
                if (reent) return false;
                reent = true;
            }

//            if (!e.Data.GetDataPresent(DataFormats.FileDrop)) return false;

//            try {
//                this.setEnabled(false);
//                this.timer1.setEnabled(false);
//
//                String[] filename = ((String[]) e.Data.GetData(DataFormats.FileDrop));
//
//                // ドロップされたアイテムがフォルダーの場合は下位フォルダー内も含めた
//                // 実際のファイルのリストを取得する
//                List<String> result = new ArrayList<>();
//                GetTrueFileNameList(result, Arrays.asList(filename));
//                // 重複を取り除く
//                filename = result.stream().distinct().toArray(String[]::new);
//
//                int i = playList.getMusics().size();
//                Point cp = dgvList.PointToClient(new Point(e.getX(), e.getY()));
//                JList.HitTestInfo hti = dgvList.HitTest(cp.getX(), cp.getY());
//                if (hti.Type == JListHitTestType.Cell && hti.RowIndex >= 0 && hti.RowIndex < dgvList.getRowCount()) {
//                    if (hti.RowIndex < playList.getMusics().size()) i = hti.RowIndex;
//                }
//
//                // 曲を停止
//                stop();
//                frmMain.stop();
//                while (!audio.isStopped())
//                    Application.DoEvents();
//
//                int buIndex = i;
//
//                playList.InsertFile(i, filename);
//
//                if (buIndex <= oldPlayIndex) {
//                    oldPlayIndex += i - buIndex;
//                }
//                i = buIndex;
//
//                // 選択位置の曲を再生する
//                String fn = playList.getMusics().get(i).fileName;
//                if (fn.toLowerCase().lastIndexOf(".lzh") == -1
//                                && fn.toLowerCase().lastIndexOf(".zip") == -1
//                                && fn.toLowerCase().lastIndexOf(".m3u") == -1
//                    //&& fn.toLowerCase().lastIndexOf(".Sid") == -1
//                ) {
//                    frmMain.loadAndPlay(0, 0, fn, null);
//                    setStart(i);// -1);
//                    frmMain.oldParam = new MDChipParams();
//                    play();
//                }
//            } catch (Exception ex) {
//                Log.forcedWrite(ex);
//                JOptionPane.showMessageDialog(null, "ファイルの読み込みに失敗しました。");
//            } finally {
//                this.setEnabled(true);
//                this.timer1.start();
//                synchronized (relock) {
//                    reent = false;
//                }
//            }
            return false;
        }
    };

    private void getTrueFileNameList(List<String> res, List<String> files) {
        for (String f : files) {
            if (File.exists(f)) {
                if (!res.contains(f)) {
                    String ext = Path.getExtension(f).toLowerCase();
                    if (Arrays.asList(sext).contains(ext)) res.add(f);
                }
            } else {
                if (Directory.exists(f)) {
                    try {
                        List<String> fs = Files.list(Paths.get(f)).map(java.nio.file.Path::toString).collect(Collectors.toList());
                        getTrueFileNameList(res, fs);
                    } catch (IOException ev) {
                        throw new UncheckedIOException(ev);
                    }
                }
            }
        }
    }

    private void tsmiA_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 1) return;

        List<Integer> sel = new ArrayList<>();
        for (int r : dgvList.getSelectedRows()) {
//            playList.getMusics().get(r).type = ((JPopupMenu) ev.getSource()).Text;
//            r.Cells[cols.clmType.ordinal()].Value = ((JMenuItem) ev.getSource()).Text;
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

        Audio.getInstance().getPlayingFileName(fn, arcFn);

        if (fn.equals(ofn) && arcFn.equals(oafn)) return;
        ofn = fn;
        oafn = arcFn;

        exts[0] = setting.getOther().getTextExt().split(";");
        exts[1] = setting.getOther().getMMLExt().split(";");
        exts[2] = setting.getOther().getImageExt().split(";");

        String bfn = Path.combine(Path.getDirectoryName(fn), Path.getFileNameWithoutExtension(fn));
        String bfnFld = Path.combine(Path.getDirectoryName(fn), Path.getFileName(Path.getDirectoryName(fn)));

        text = "";
        for (String ext : exts[0]) {
            if (File.exists(bfn + "." + ext)) {
                text = bfn + "." + ext;
                break;
            }
            if (File.exists(bfnFld + "." + ext)) {
                text = bfnFld + "." + ext;
                break;
            }
        }
        mml = "";
        for (String ext : exts[1]) {
            if (File.exists(bfn + "." + ext)) {
                mml = bfn + "." + ext;
                break;
            }
            if (File.exists(bfnFld + "." + ext)) {
                mml = bfnFld + "." + ext;
                break;
            }
        }
        img = "";
        for (String ext : exts[2]) {
            if (File.exists(bfn + "." + ext)) {
                img = bfn + "." + ext;
                break;
            }
            if (File.exists(bfnFld + "." + ext)) {
                img = bfnFld + "." + ext;
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
        if (mml.equals("")) return;
        try {
            new ProcessBuilder(mml).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void tsbImgExt_Click(ActionEvent ev) {
        if (img.equals("")) return;
        try {
            new ProcessBuilder(img).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PlayList.Music getPlayingSongInfo() {
        if (playIndex < 0 || dgvList.getRowCount() <= playIndex) return null;
//        return ((PlayList.Music) dgvList.getValueAt(dgvList.getSelectedRows()[0], playIndex)).Tag;
        return null; // TODO
    }

    private void tsmiOpenFolder_Click(ActionEvent ev) {
        try {
            String path = (String) dgvList.getValueAt(dgvList.getSelectedRows()[0], cols.clmFileName.ordinal());
            path = Path.getDirectoryName(path);
            new ProcessBuilder(path).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    enum cols {
        __dummy__,
        clmKey,
        clmSongNo,
        clmZipFileName,
        clmFileName,
        clmPlayingNow,
        clmEXT,
        clmType,
        clmTitle,
        clmTitleJ,
        clmDispFileName,
        clmGame,
        clmGameJ,
        clmComposer,
        clmComposerJ,
        clmVGMby,
        clmConverted,
        clmNotes,
        clmDuration,
        clmSpacer
    }

    private void initializeComponent() {
//        this.components = new System.ComponentModel.Container();
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmPlayList));
//        JTableCellStyle JListCellStyle1 = new JTableCellStyle();
//        JTableCellStyle JListCellStyle3 = new JTableCellStyle();
//        JTableCellStyle JListCellStyle4 = new JTableCellStyle();
//        JTableCellStyle JListCellStyle2 = new JTableCellStyle();
        this.toolStripContainer1 = new JPopupMenu();
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
        this.cmsPlayList = new JMenu();
        this.type設定ToolStripMenuItem = new JMenu();
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
        this.toolStrip1 = new JPopupMenu();
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
        this.timer1 = new Timer(0, null);
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
        this.toolStripContainer1.add(this.dgvList);
//        //resources.ApplyResources(this.toolStripContainer1, "toolStripContainer1");
        this.toolStripContainer1.setName("toolStripContainer1");
        //
        // toolStripContainer1.TopToolStripPanel
        //
        this.toolStripContainer1.add(this.toolStrip1);
        //
        // dgvList
        //
//        this.dgvList.AllowDrop = true;
//        this.dgvList.AllowUserToAddRows = false;
//        this.dgvList.AllowUserToDeleteRows = false;
//        this.dgvList.AllowUserToResizeRows = false;
//        this.dgvList.BackgroundColor = Color.black;
//        this.dgvList.BorderStyle = JBorderStyle.None;
//        this.dgvList.CellBorderStyle = JTableCellBorderStyle.None;
//        JListCellStyle1.Alignment = JTableContentAlignment.MiddleLeft;
//        JListCellStyle1.setBackground(Color.black);
//        JListCellStyle1.setFont(new Font("メイリオ", 8.25F, FontStyle.Regular, GraphicsUnit.Point, ((byte) (128))));
//        JListCellStyle1.setForeColor = Color.MenuHighlight;
//        JListCellStyle1.Selectio.setBackground(Color.Highlight);
//        JListCellStyle1.SelectionForeColor = Color.HighlightText;
//        JListCellStyle1.WrapMode = JTableTriState.False;
//        this.dgvList.ColumnHeadersDefaultCellStyle = JListCellStyle1;
//        //resources.ApplyResources(this.dgvList, "dgvList");
//        this.dgvList.ColumnHeadersHeightSizeMode = JTableColumnHeadersHeightSizeMode.DisableResizing;
//        this.dgvList.EditMode = JTableEditMode.EditProgrammatically;
//        this.dgvList.setName("dgvList");
//        this.dgvList.RowHeadersBorderStyle = JTableHeaderBorderStyle.None;
//        JListCellStyle3.setHoAlignment = JTableContentAlignment.MiddleLeft;
//        JListCellStyle3.setBackground(Color.black);
//        JListCellStyle3.setFont(new Font("メイリオ", 8.25F, Font.BOLD, GraphicsUnit.Point, ((byte) (128))));
//        JListCellStyle3.ForeColor = Color.Window;
//        JListCellStyle3.Selectio.setBackground(Color.Highlight);
//        JListCellStyle3.SelectionForeColor = Color.HighlightText;
//        JListCellStyle3.WrapMode = JTableTriState.True;
//        this.dgvList.RowHeadersDefaultCellStyle = JListCellStyle3;
//        this.dgvList.RowHeadersVisible = false;
//        JListCellStyle4.setBackground(Color.black);
//        JListCellStyle4.setFont(new Font("メイリオ", 8.25F, Font.BOLD, GraphicsUnit.Point, ((byte) (128))));
//        JListCellStyle4.setForeColor = new Color(((byte) (192)), ((byte) (192)), ((byte) (255)));
//        this.dgvList.RowsDefaultCellStyle = JListCellStyle4;
//        this.dgvList.RowTemplate.ContextMenuStrip = this.cmsPlayList;
//        this.dgvList.RowTemplate.DefaultCellStyle.Alignment = JTableContentAlignment.MiddleLeft;
//        this.dgvList.RowTemplate.getHeight() = 10;
//        this.dgvList.RowTemplate.setEditable(Xtrue);
//        this.dgvList.setSelectionMode(FullRowSelect);
//        this.dgvList.ShowCellErrors = false;
//        this.dgvList.ShowEditingIcon = false;
//        this.dgvList.ShowRowErrors = false;
//        this.dgvList.CellDoubleClick += new JTableCellEventHandler(this.dgvList_CellDoubleClick);
        this.dgvList.addMouseListener(this.dgvList_CellMouseClick);
        new DropTarget(dgvList, DnDConstants.ACTION_COPY_OR_MOVE, dgvList_DragDrop, true);
        //
        // clmKey
        //
        //resources.ApplyResources(this.clmKey, cols.clmKey.ordinal());
//        this.clmKey.setName(cols.clmKey.ordinal());
//        this.clmKey.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmSongNo
        //
        //resources.ApplyResources(this.clmSongNo, cols.clmSongNo.ordinal());
//        this.clmSongNo.setName(cols.clmSongNo.ordinal());
        //
        // clmZipFileName
        //
        //resources.ApplyResources(this.clmZipFileName, cols.clmZipFileName.ordinal());
//        this.clmZipFileName.setName(cols.clmZipFileName.ordinal());
        //
        // clmFileName
        //
        //resources.ApplyResources(this.clmFileName, cols.clmFileName.ordinal());
//        this.clmFileName.setName(cols.clmFileName.ordinal());
//        this.clmFileName.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmPlayingNow
        //
//            this.clmPlayingNow.AutoSizeMode = JTableAutoSizeColumnMode.None;
        //resources.ApplyResources(this.clmPlayingNow, cols.clmPlayingNow.ordinal());
//        this.clmPlayingNow.setName(cols.clmPlayingNow.ordinal());
//        this.clmPlayingNow.Resizable = JTableTriState.False;
//        this.clmPlayingNow.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmEXT
        //
        //resources.ApplyResources(this.clmEXT, cols.clmEXT.ordinal());
//        this.clmEXT.setName(cols.clmEXT.ordinal());
        this.clmEXT.setEditable(false);
//        this.clmEXT.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmType
        //
        //resources.ApplyResources(this.clmType, cols.clmType.ordinal());
//        this.clmType.setName(cols.clmType.ordinal());
        this.clmType.setEditable(false);
//        this.clmType.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmTitle
        //
        //resources.ApplyResources(this.clmTitle, cols.clmTitle.ordinal());
//        this.clmTitle.setName(cols.clmTitle.ordinal());
        this.clmTitle.setEditable(false);
//        this.clmTitle.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmTitleJ
        //
        //resources.ApplyResources(this.clmTitleJ, cols.clmTitleJ.ordinal());
//        this.clmTitleJ.setName(cols.clmTitleJ.ordinal());
//        this.clmTitleJ.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmDispFileName
        //
        //resources.ApplyResources(this.clmDispFileName, cols.clmDispFileName.ordinal());
//        this.clmDispFileName.setName(cols.clmDispFileName.ordinal());
        //
        // clmGame
        //
        //resources.ApplyResources(this.clmGame, cols.clmGame.ordinal());
//        this.clmGame.setName(cols.clmGame.ordinal());
        this.clmGame.setEditable(false);
//        this.clmGame.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmGameJ
        //
        //resources.ApplyResources(this.clmGameJ, cols.clmGameJ.ordinal());
//        this.clmGameJ.setName(cols.clmGameJ.ordinal());
//        this.clmGameJ.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmComposer
        //
        //resources.ApplyResources(this.clmComposer, cols.clmComposer.ordinal());
//        this.clmComposer.setName(cols.clmComposer.ordinal());
//        this.clmComposer.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmComposerJ
        //
        //resources.ApplyResources(this.clmComposerJ, cols.clmComposerJ.ordinal());
//        this.clmComposerJ.setName(cols.clmComposerJ.ordinal());
//            this.clmComposerJ.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmVGMby
        //
        //resources.ApplyResources(this.clmVGMby, cols.clmVGMby.ordinal());
//        this.clmVGMby.setName(cols.clmVGMby.ordinal());
//            this.clmVGMby.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmConverted
        //
        //resources.ApplyResources(this.clmConverted, cols.clmConverted.ordinal());
//        this.clmConverted.setName(cols.clmConverted.ordinal());
//            this.clmConverted.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmNotes
        //
        //resources.ApplyResources(this.clmNotes, cols.clmNotes.ordinal());
//        this.clmNotes.setName(cols.clmNotes.ordinal());
//        this.clmNotes.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmDuration
        //
//        JListCellStyle2.Alignment = JTableContentAlignment.MiddleRight;
//        this.clmDuration.DefaultCellStyle = JListCellStyle2;
        //resources.ApplyResources(this.clmDuration, cols.clmDuration.ordinal());
//        this.clmDuration.setName(cols.clmDuration.ordinal());
//        this.clmDuration.SortMode = JTableColumnSortMode.NotSortable;
        //
        // clmSpacer
        //
//        this.clmSpacer.AutoSizeMode = JTableAutoSizeColumnMode.Fill;
        //resources.ApplyResources(this.clmSpacer, cols.clmSpacer.ordinal());
//        this.clmSpacer.setName(cols.clmSpacer.ordinal());
//        this.clmSpacer.setEditable(false);
//        this.clmSpacer.SortMode = JTableColumnSortMode.NotSortable;
        //
        // cmsPlayList
        //
        this.cmsPlayList.add(this.type設定ToolStripMenuItem);
        this.cmsPlayList.add(this.toolStripSeparator5);
        this.cmsPlayList.add(this.tsmiPlayThis);
        this.cmsPlayList.add(this.tsmiDelThis);
        this.cmsPlayList.add(this.toolStripSeparator3);
        this.cmsPlayList.add(this.tsmiDelAllMusic);
        this.cmsPlayList.add(this.tsmiOpenFolder);
        this.cmsPlayList.setName("cmsPlayList");
        //resources.ApplyResources(this.cmsPlayList, "cmsPlayList");
        //
        // type設定ToolStripMenuItem
        //
        this.type設定ToolStripMenuItem.add(this.tsmiA);
        this.type設定ToolStripMenuItem.add(this.tsmiB);
        this.type設定ToolStripMenuItem.add(this.tsmiC);
        this.type設定ToolStripMenuItem.add(this.tsmiD);
        this.type設定ToolStripMenuItem.add(this.tsmiE);
        this.type設定ToolStripMenuItem.add(this.tsmiF);
        this.type設定ToolStripMenuItem.add(this.tsmiG);
        this.type設定ToolStripMenuItem.add(this.tsmiH);
        this.type設定ToolStripMenuItem.add(this.tsmiI);
        this.type設定ToolStripMenuItem.add(this.tsmiJ);
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
//        this.toolStrip1.GripStyle = JToolStripGripStyle.Hidden;
        this.toolStrip1.add(this.tsbOpenPlayList);
        this.toolStrip1.add(this.tsbSavePlayList);
        this.toolStrip1.add(this.toolStripSeparator1);
        this.toolStrip1.add(this.tsbAddMusic);
        this.toolStrip1.add(this.tsbAddFolder);
        this.toolStrip1.add(this.toolStripSeparator2);
        this.toolStrip1.add(this.tsbUp);
        this.toolStrip1.add(this.tsbDown);
        this.toolStrip1.add(this.toolStripSeparator4);
        this.toolStrip1.add(this.tsbJapanese);
        this.toolStrip1.add(this.toolStripSeparator6);
        this.toolStrip1.add(this.tsbTextExt);
        this.toolStrip1.add(this.tsbMMLExt);
        this.toolStrip1.add(this.tsbImgExt);
        this.toolStrip1.setName("toolStrip1");
//        this.toolStrip1.Stretch = true;
        //
        // tsbOpenPlayList
        //
//        this.tsbOpenPlayList.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbOpenPlayList.setIcon(new ImageIcon(mdplayer.properties.Resources.getOpenPL()));
        //resources.ApplyResources(this.tsbOpenPlayList, "tsbOpenPlayList");
        this.tsbOpenPlayList.setName("tsbOpenPlayList");
        this.tsbOpenPlayList.addActionListener(this::tsbOpenPlayList_Click);
        //
        // tsbSavePlayList
        //
//        this.tsbSavePlayList.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbSavePlayList.setIcon(new ImageIcon(mdplayer.properties.Resources.getSavePL()));
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
//        this.tsbAddMusic.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbAddMusic.setIcon(new ImageIcon(mdplayer.properties.Resources.getAddPL()));
        //resources.ApplyResources(this.tsbAddMusic, "tsbAddMusic");
        this.tsbAddMusic.setName("tsbAddMusic");
        this.tsbAddMusic.addActionListener(this::tsbAddMusic_Click);
        //
        // tsbAddFolder
        //
//        this.tsbAddFolder.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbAddFolder.setIcon(new ImageIcon(mdplayer.properties.Resources.getAddFolderPL()));
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
//        this.tsbUp.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbUp.setIcon(new ImageIcon(mdplayer.properties.Resources.getUpPL()));
        //resources.ApplyResources(this.tsbUp, "tsbUp");
        this.tsbUp.setName("tsbUp");
        this.tsbUp.addActionListener(this::tsbUp_Click);
        //
        // tsbDown
        //
//        this.tsbDown.DisplayStyle = JToolStripItemDisplayStyle.Image;
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
//        this.tsbJapanese.CheckOnClick = true;
//        this.tsbJapanese.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbJapanese.setIcon(new ImageIcon(mdplayer.properties.Resources.getJapPL()));
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
//        this.tsbTextExt.DisplayStyle = JToolStripItemDisplayStyle.Image;
        //resources.ApplyResources(this.tsbTextExt, "tsbTextExt");
        this.tsbTextExt.setIcon(new ImageIcon(mdplayer.properties.Resources.getTxtPL()));
        this.tsbTextExt.setName("tsbTextExt");
        this.tsbTextExt.addActionListener(this::tsbTextExt_Click);
        //
        // tsbMMLExt
        //
//        this.tsbMMLExt.DisplayStyle = JToolStripItemDisplayStyle.Image;
        //resources.ApplyResources(this.tsbMMLExt, "tsbMMLExt");
        this.tsbMMLExt.setIcon(new ImageIcon(mdplayer.properties.Resources.getMmlPL()));
        this.tsbMMLExt.setName("tsbMMLExt");
        this.tsbMMLExt.addActionListener(this::tsbMMLExt_Click);
        //
        // tsbImgExt
        //
//        this.tsbImgExt.DisplayStyle = JToolStripItemDisplayStyle.Image;
        //resources.ApplyResources(this.tsbImgExt, "tsbImgExt");
        this.tsbImgExt.setIcon(new ImageIcon(mdplayer.properties.Resources.getImgPL()));
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
//        this.KeyPreview = true;
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
    private JPopupMenu toolStripContainer1;
    private JPopupMenu toolStrip1;
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
    private JMenu type設定ToolStripMenuItem;
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
