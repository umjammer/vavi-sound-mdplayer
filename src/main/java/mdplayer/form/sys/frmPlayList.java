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
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
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

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmArcType;
import mdplayer.MDChipParams;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.format.FileFormat;
import mdplayer.properties.Resources;
import vavi.awt.dnd.BasicDTListener;
import vavi.util.compat.Tuple;
import vavi.util.compat.Tuple4;

import static java.lang.System.getLogger;
import static vavi.util.compat.Util.getExtension;
import static vavi.util.compat.Util.getFileNameWithoutExtension;


public class frmPlayList extends JFrame {

    private static final Logger logger = getLogger(frmPlayList.class.getName());

    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public Setting setting;

    public String playFilename = "";
    public String playArcFilename = "";
    public FileFormat playFormat = FileFormat.unknown;
    public EnmArcType playArcType = EnmArcType.unknown;
    public int playSongNum = -1;

    private PlayList playList;
    private final frmMain frmMain;

    private boolean playing = false;

    /** the kind of list last opened or saved, so that saving offers the same kind back */
    private boolean m3u = false;
    private int playIndex;
    private int oldPlayIndex;

    private final Random rand = new Random();
    private boolean IsInitialOpenFolder = true;

    static final Preferences prefs = Preferences.userNodeForPackage(frmPlayList.class);

    private static final String[] sext = ".vgm;.vgz;.zip;.lzh;.nrd;.xgm;.zgm;.s98;.nsf;.hes;.sid;.mnd;.mgs;.mdr;.mdx;.mub;.muc;.m;.m2;.mz;.mml;.mid;.rcp;.wav;.mp3;.aiff;.m3u".split(";");

    public frmPlayList(frmMain frm) {
        frmMain = frm;
        setting = frm.setting;
        initializeComponent();

        playList = PlayList.load(null);
        playList.addRow = row -> ((DefaultTableModel) dgvList.getModel()).addRow(row);
        playList.setRow = (index, row) -> ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
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

    public final List<Tuple<String, String>> randomStack = new ArrayList<>();

//    @Override
//    protected void WndProc(Message m) {
//        if (frmMain != null) {
//            frmMain.windowsMessage(m);
//        }
//
//        super.WndProc(m);
//    }

    private final WindowListener windowListener = new WindowAdapter() {
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
        logger.log(Level.INFO, "updatePlayingIndex: newPlayingIndex=" + newPlayingIndex +
            ", dgvList.getRowCount()=" + dgvList.getRowCount() +
            ", playList.getMusics().size()=" + playList.getMusics().size());

        for (int i = 0; i < playList.getMusics().size(); i++) {
            PlayList.Music m = playList.getMusics().get(i);
            logger.log(Level.INFO, "  music " + i + ": fileName=" + m.fileName + ", title=" + m.title + ", game=" + m.game);
        }

        if (dgvList.getRowCount() < 1 && playList.getMusics().size() > 0) {
            logger.log(Level.INFO, "dgvList is empty but playList.getMusics() is not! Refreshing...");
            refresh();
            logger.log(Level.INFO, "After refresh: dgvList.getRowCount()=" + dgvList.getRowCount());
        }

        if (dgvList.getRowCount() < 1) {
            // an empty list has no row to mark, and -1/-2 would resolve to one that is not there
            playIndex = -1;
            oldPlayIndex = -1;
            return;
        }

        if (oldPlayIndex != -1 && oldPlayIndex < dgvList.getRowCount()) {
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

    private static final Color clrLightBlue = new Color(255, 192, 192, 255);

    private void ResetColor(int rowIndex) {
        dgvList.setValueAt(" ", rowIndex, cols.clmPlayingNow.ordinal());
        for (int i = 0; i < dgvList.getColumnCount(); i++) {
            Component c = dgvList.getCellRenderer(rowIndex, i).getTableCellRendererComponent(dgvList, dgvList.getValueAt(rowIndex, i), true, false, rowIndex, i);
            c.setForeground(clrLightBlue);
//            dgvList.Rows[rowIndex].Cells[i].Style.SelectionForeColor = Color.white;
        }
    }

    private final MouseListener dgvList_CellMouseClick = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            showPlayListPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPlayListPopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int row = dgvList.rowAtPoint(e.getPoint());
            if (row < 0) return;
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                playRow(row);
            }
        }
    };

    private void showPlayListPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return;

        int row = dgvList.rowAtPoint(e.getPoint());
        if (row < 0) return;
        if (!dgvList.isRowSelected(row)) {
            dgvList.setRowSelectionInterval(row, row);
        }

        if (dgvList.getSelectedRowCount() > 1) {
            tsmiDelThis.setText("Remove the selected song.");
        } else {
            tsmiDelThis.setText("Remove this song");
        }
        cmsPlayList.show(dgvList, e.getX(), e.getY());
    }

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
        int songNo;
        try {
            songNo = (int) dgvList.getValueAt(pi, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            songNo = 0;
        }
        if (dgvList.getValueAt(pi, cols.clmType.ordinal()) != null && !dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().equals("-")) {
            m = dgvList.getValueAt(pi, cols.clmType.ordinal()).toString().charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }

        try {
            frmMain.loadAndPlay(m, songNo, fn, zfn);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
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
        case 0: // normal
            if (dgvList.getRowCount() <= playIndex + 1) return;
            pi++;
            break;
        case 1: // random

            if (pi != -1) {
                // Updating playback history
                fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
                zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());

                randomStack.add(new Tuple<>(fn, zfn));
                while (randomStack.size() > 1000)
                    randomStack.removeFirst();
            }

            pi = rand.nextInt(dgvList.getRowCount());
            break;
        case 2: // All songs loop
            pi++;
            if (pi >= dgvList.getRowCount()) {
                pi = 0;
            }
            break;
        case 3: // One song loop
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
        int songNo;
        try {
            songNo = (int) dgvList.getValueAt(pi, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
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
            if (!randomStack.isEmpty()) {
                while (true) {
                    String hfn = randomStack.getLast().getItem1();
                    String hzfn = randomStack.getLast().getItem2();
                    randomStack.removeLast();

                    for (; pi < dgvList.getRowCount(); pi++) {
                        fn = (String) dgvList.getValueAt(pi, cols.clmFileName.ordinal());
                        zfn = (String) dgvList.getValueAt(pi, cols.clmZipFileName.ordinal());
                        if (hfn.equals(fn) && hzfn.equals(zfn)) {
                            break loopEx;
                        }
                    }

                    if (randomStack.isEmpty()) break;
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
        int songNo;
        try {
            songNo = (int) dgvList.getValueAt(pi, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            songNo = 0;
        }

        frmMain.loadAndPlay(m, songNo, fn, zfn);
        updatePlayingIndex(pi);
        playing = true;
    }

    private void tsmiPlayThis_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 1) return;
        playRow(dgvList.getSelectedRows()[0]);
    }

    private void playRow(int row) {
        if (row < 0 || row >= dgvList.getRowCount()) return;
        playing = false;

        String fn = (String) dgvList.getValueAt(row, cols.clmFileName.ordinal());
        String zfn = (String) dgvList.getValueAt(row, cols.clmZipFileName.ordinal());
        int m = 0;
        if (dgvList.getValueAt(row, cols.clmType.ordinal()) != null && !dgvList.getValueAt(row, cols.clmType.ordinal()).toString().equals("-")) {
            m = dgvList.getValueAt(row, cols.clmType.ordinal()).toString().charAt(0) - 'A';
            if (m < 0 || m > 9) m = 0;
        }
        int songNo;
        try {
            songNo = (int) dgvList.getValueAt(row, cols.clmSongNo.ordinal());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            songNo = 0;
        }

        if (frmMain.loadAndPlay(m, songNo, fn, zfn)) {
            updatePlayingIndex(row);
            playing = true;
        }
    }

    private void tsmiDelAllMusic_Click(ActionEvent ev) {

        int res = JOptionPane.showConfirmDialog(null, "All songs in the playlist will be removed. Is that okay?", "PlayList",
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
            @Override public String getDescription() { return "XML file(*.xml)"; }
        });
        ofd.addChoosableFileFilter(new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".m3u"); }
            @Override public String getDescription() { return "M3U file(*.m3u)"; }
        });
        ofd.setDialogTitle("Select a playlist file");
        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Files.exists(Path.of(frmMain.setting.getOther().getDefaultDataPath())) && IsInitialOpenFolder) {
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
            PlayList pl;
            String filename = ofd.getSelectedFile().getPath();

            m3u = filename.toLowerCase().endsWith(".m3u");

            if (!m3u) {
                pl = PlayList.load(filename);
                playing = false;
                playList = pl;
                playList.addRow = row -> ((DefaultTableModel) dgvList.getModel()).addRow(row);
                playList.setRow = (index, row) -> ((DefaultTableModel) dgvList.getModel()).insertRow(index, row);
            } else {
                pl = PlayList.loadM3U(filename);
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "File loading failed.");
        }
    }

    private void tsbSavePlayList_Click(ActionEvent ev) {

        JFileChooser sfd = new JFileChooser();
        FileFilter xmlFilter = new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".xml"); }
            @Override public String getDescription() { return "XML file(*.xml)"; }
        };
        FileFilter m3uFilter = new FileFilter() {
            @Override public boolean accept(java.io.File f) { return f.getName().toLowerCase().endsWith(".m3u"); }
            @Override public String getDescription() { return "M3U file(*.m3u)"; }
        };
        sfd.addChoosableFileFilter(xmlFilter);
        sfd.addChoosableFileFilter(m3uFilter);
        // upstream STBL546: a list opened as an m3u is offered back as an m3u
        sfd.setFileFilter(m3u ? m3uFilter : xmlFilter);
        sfd.setDialogTitle("Save the playlist file");
        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Files.exists(Path.of(frmMain.setting.getOther().getDefaultDataPath())) && IsInitialOpenFolder) {
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
            if (getExtension(filename).isEmpty()) {
                filename = Path.of(filename + ".m3u").toString();
            }
            break;
        case 0:
            if (getExtension(filename).isEmpty()) {
                filename = Path.of(filename + ".xml").toString();
            }
            break;
        }

        try {
            m3u = filename.toLowerCase().endsWith(".m3u");

            if (!m3u)
                playList.save(filename);
            else
                playList.saveM3U(filename);

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "File saving failed.");
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
        ofd.setDialogTitle("Select a file");
        int filterIndex = setting.getOther().getFilterIndex();
        FileFilter[] filters = ofd.getChoosableFileFilters();
        if (filterIndex >= 0 && filterIndex < filters.length) {
            ofd.setFileFilter(filters[filterIndex]);
        }

        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Files.exists(Path.of(frmMain.setting.getOther().getDefaultDataPath())) && IsInitialOpenFolder) {
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
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
        fbd.setDialogTitle("Please specify the folder.");
        if (!frmMain.setting.getOther().getDefaultDataPath().isEmpty() && Files.exists(Path.of(frmMain.setting.getOther().getDefaultDataPath()))) {
            fbd.setSelectedFile(new java.io.File(frmMain.setting.getOther().getDefaultDataPath()));
        }

        if (fbd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        stop();

        try (var s = Files.list(Paths.get(fbd.getSelectedFile().getPath()))) {
            s.forEach(p -> {
                String ext = getExtension(p.getFileName().toString()).toUpperCase();
                if (Arrays.asList(_exts).contains(ext)) {
                    playList.addFile(p.toFile().getAbsolutePath());
                }
            });
        } catch (IOException ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
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

    private final KeyListener frmPlayList_KeyDown = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            //logger.log(Level.TRACE, "keycode%d %d %d".formatted(e.KeyCode, e.KeyData, e.KeyValue));

            switch (e.getKeyCode()) {
            case 32: // Space
            case 13: // Enter
                if (dgvList.getSelectedRowCount() == 0) {
                    return;
                }

//                e.Handled = true;
                playRow(dgvList.getSelectedRows()[0]);
                break;
            case 46: // Delete
//                e.Handled = true;
                tsmiDelThis_Click(null);
                break;
            }
        }
    };

    private final BasicDTListener dgvList_DragDrop = new BasicDTListener() {
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

            try {
                if (!(data instanceof List<?> dropped)) return false;

                List<String> files = dropped.stream()
                        .filter(File.class::isInstance)
                        .map(File.class::cast)
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList());
                List<String> filenames = new ArrayList<>();
                getTrueFileNameList(filenames, files);
                filenames = filenames.stream().distinct().collect(Collectors.toList());
                if (filenames.isEmpty()) return false;

                int row = dgvList.rowAtPoint(e.getLocation());
                int[] insertIndex = {row >= 0 && row < dgvList.getRowCount() ? row : playList.getMusics().size()};

                stop();
                frmMain.stop();

                int before = insertIndex[0];
                playList.insertFile(insertIndex, filenames.toArray(String[]::new));
                if (before <= oldPlayIndex) {
                    oldPlayIndex += insertIndex[0] - before;
                }
                if (before <= playIndex) {
                    playIndex += insertIndex[0] - before;
                }

                return true;
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "File loading failed.");
                return false;
            } finally {
                synchronized (relock) {
                    reent = false;
                }
            }
        }
    };

    private static void getTrueFileNameList(List<String> res, List<String> files) {
        for (String f : files) {
            if (Files.isDirectory(Path.of(f))) {
                try (var s = Files.list(Paths.get(f))) {
                    List<String> fs = s.map(java.nio.file.Path::toString).collect(Collectors.toList());
                    getTrueFileNameList(res, fs);
                } catch (IOException ev) {
                    throw new UncheckedIOException(ev);
                }
            } else if (Files.exists(Path.of(f))) {
                if (!res.contains(f)) {
                    String ext = getExtension(f).toLowerCase();
                    if (Arrays.asList(sext).contains(ext)) res.add(f);
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
    final String[][] exts = new String[3][];
    String text = "";
    String mml = "";
    String img = "";

    private void timer1_Tick(ActionEvent ev) {
        if (!playing) return;
        if (setting == null) return;

        ofn = Audio.getInstance().plugin.playingFileName;
        oafn = Audio.getInstance().plugin.playingArcFileName;

        exts[0] = setting.getOther().getTextExt().split(";");
        exts[1] = setting.getOther().getMMLExt().split(";");
        exts[2] = setting.getOther().getImageExt().split(";");

        String bfn = Path.of(ofn).getParent().resolve(getFileNameWithoutExtension(ofn)).toString();
        String bfnFld = Path.of(ofn).getParent().resolve(Path.of(ofn).getParent().getFileName()).toString();

        text = "";
        for (String ext : exts[0]) {
            if (Files.exists(Path.of(bfn + "." + ext))) {
                text = bfn + "." + ext;
                break;
            }
            if (Files.exists(Path.of(bfnFld + "." + ext))) {
                text = bfnFld + "." + ext;
                break;
            }
        }
        mml = "";
        for (String ext : exts[1]) {
            if (Files.exists(Path.of(bfn + "." + ext))) {
                mml = bfn + "." + ext;
                break;
            }
            if (Files.exists(Path.of(bfnFld + "." + ext))) {
                mml = bfnFld + "." + ext;
                break;
            }
        }
        img = "";
        for (String ext : exts[2]) {
            if (Files.exists(Path.of(bfn + "." + ext))) {
                img = bfn + "." + ext;
                break;
            }
            if (Files.exists(Path.of(bfnFld + "." + ext))) {
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
        if (mml.isEmpty()) return;
        try {
            new ProcessBuilder(mml).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void tsbImgExt_Click(ActionEvent ev) {
        if (img.isEmpty()) return;
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
            path = Path.of(path).getParent().toString();
            new ProcessBuilder(path).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * The columns of {@link #dgvList}, in the order {@link PlayList#makeRow} builds a row: an
     * ordinal is the column index, so the two must be kept in step.
     */
    enum cols {
        clmPlayingNow,
        clmKey,
        clmFileName,
        clmZipFileName,
        clmDispFileName,
        clmEXT,
        clmType,
        clmTitle,
        clmTitleJ,
        clmGame,
        clmGameJ,
        clmComposer,
        clmComposerJ,
        clmConverted,
        clmNotes,
        clmDuration,
        clmVGMby,
        clmSongNo
    }

    /** the designer's captions and widths, converted from frmPlayList.resx */
    private static final ResourceBundle resources = ResourceBundle.getBundle("mdplayer/form/sys/frmPlayList", Locale.getDefault());

    /** The caption the designer gave a column, or its name if it has none. */
    private static String header(String column) {
        try {
            return resources.getString(column + ".HeaderText");
        } catch (MissingResourceException e) {
            return column;
        }
    }

    /** Sizes a column the way the designer did. */
    private void width(cols column) {
        try {
            dgvList.getColumnModel().getColumn(column.ordinal())
                    .setPreferredWidth(Integer.parseInt(resources.getString(column.name() + ".Width").trim()));
        } catch (MissingResourceException | NumberFormatException e) {
            logger.log(Level.DEBUG, "no width for " + column);
        }
    }

    private void initializeComponent() {
//        this.components = new System.ComponentModel.Container();
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmPlayList));
//        JTableCellStyle JListCellStyle1 = new JTableCellStyle();
//        JTableCellStyle JListCellStyle3 = new JTableCellStyle();
//        JTableCellStyle JListCellStyle4 = new JTableCellStyle();
//        JTableCellStyle JListCellStyle2 = new JTableCellStyle();
        this.toolStripContainer1 = new JPanel();
        this.dgvList = new JTable();
        // the WinForms columns did not survive the port, so the model has to declare them; their
        // captions and widths are the designer's, out of frmPlayList.resx
        this.dgvList.setModel(new DefaultTableModel(
                Arrays.stream(cols.values()).map(c -> header(c.name())).toArray(), 0));
        this.dgvList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (cols column : cols.values()) {
            width(column);
        }
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
        this.cmsPlayList = new JPopupMenu();
        this.typeSettingsToolStripMenuItem = new JMenu();
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
        // a ToolStripContainer is a panel with the tool strip along its top and the content filling
        // the rest, so that is what it is here — the table needs a scroll pane of its own, or its
        // header does not show and a long list cannot be reached
        this.toolStripContainer1.setName("toolStripContainer1");
        this.toolStripContainer1.setLayout(new BorderLayout());
        this.toolStripContainer1.add(this.toolStrip1, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(this.dgvList);
        scrollPane.getViewport().setBackground(Color.BLACK);
        this.dgvList.setBackground(Color.BLACK);
        this.dgvList.setForeground(new Color(192, 192, 255));
        this.dgvList.setSelectionBackground(Color.DARK_GRAY);
        this.dgvList.setSelectionForeground(Color.WHITE);
        this.toolStripContainer1.add(scrollPane, BorderLayout.CENTER);
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
//        JListCellStyle1.setFont(new Font("Meyryo", 8.25F, FontStyle.Regular, GraphicsUnit.Point, ((byte) (128))));
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
//        JListCellStyle3.setFont(new Font("Meyryo", 8.25F, Font.BOLD, GraphicsUnit.Point, ((byte) (128))));
//        JListCellStyle3.ForeColor = Color.Window;
//        JListCellStyle3.Selectio.setBackground(Color.Highlight);
//        JListCellStyle3.SelectionForeColor = Color.HighlightText;
//        JListCellStyle3.WrapMode = JTableTriState.True;
//        this.dgvList.RowHeadersDefaultCellStyle = JListCellStyle3;
//        this.dgvList.RowHeadersVisible = false;
//        JListCellStyle4.setBackground(Color.black);
//        JListCellStyle4.setFont(new Font("Meyryo", 8.25F, Font.BOLD, GraphicsUnit.Point, ((byte) (128))));
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
        this.cmsPlayList.add(this.typeSettingsToolStripMenuItem);
        this.cmsPlayList.add(this.toolStripSeparator5);
        this.cmsPlayList.add(this.tsmiPlayThis);
        this.cmsPlayList.add(this.tsmiDelThis);
        this.cmsPlayList.add(this.toolStripSeparator3);
        this.cmsPlayList.add(this.tsmiDelAllMusic);
        this.cmsPlayList.add(this.tsmiOpenFolder);
        this.cmsPlayList.setName("cmsPlayList");
        //resources.ApplyResources(this.cmsPlayList, "cmsPlayList");
        //
        // typeSettingsToolStripMenuItem
        //
        this.typeSettingsToolStripMenuItem.add(this.tsmiA);
        this.typeSettingsToolStripMenuItem.add(this.tsmiB);
        this.typeSettingsToolStripMenuItem.add(this.tsmiC);
        this.typeSettingsToolStripMenuItem.add(this.tsmiD);
        this.typeSettingsToolStripMenuItem.add(this.tsmiE);
        this.typeSettingsToolStripMenuItem.add(this.tsmiF);
        this.typeSettingsToolStripMenuItem.add(this.tsmiG);
        this.typeSettingsToolStripMenuItem.add(this.tsmiH);
        this.typeSettingsToolStripMenuItem.add(this.tsmiI);
        this.typeSettingsToolStripMenuItem.add(this.tsmiJ);
        this.typeSettingsToolStripMenuItem.setName("typeSettingsToolStripMenuItem");
        //resources.ApplyResources(this.typeSettingsToolStripMenuItem, "typeSettingsToolStripMenuItem");
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
        // the container fills the window, so BorderLayout is what is wanted here
        this.getContentPane().add(this.toolStripContainer1, BorderLayout.CENTER);
//        this.KeyPreview = true;
        this.setName("frmPlayList");
//        this.setOpacity(0);
        this.setTitle("play list");
        this.setSize(585, 270);
        this.setMinimumSize(new Dimension(400, 120));
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
    private JPopupMenu cmsPlayList;
    private JMenuItem tsmiPlayThis;
    private JMenuItem tsmiDelThis;
    private JPanel toolStripContainer1;
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
    private JMenu typeSettingsToolStripMenuItem;
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
