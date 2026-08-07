package mdplayer.vst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import mdplayer.Audio;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.form.sys.FormMain;

import static java.lang.System.getLogger;


/**
 * The list of VST effects the mix is run through.
 * <p>
 * Clicking the power or editor column of a row switches that plug-in on and off, or shows and
 * hides its editor; the right button offers to take one out of the chain.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class FormVSTeffectList extends JFrame {

    private static final Logger logger = getLogger(FormVSTeffectList.class.getName());

    private static final int COLUMN_KEY = 0;
    private static final int COLUMN_FILE = 1;
    private static final int COLUMN_POWER = 2;
    private static final int COLUMN_EDITOR = 3;
    private static final int COLUMN_NAME = 4;

    private final FormMain parent;
    public boolean isClosed = false;
    public final Setting setting;
    private static final boolean isInitialOpenFolder = true;
    final Audio audio = Audio.getInstance();

    public FormVSTeffectList(FormMain parent, Setting setting) {
        this.parent = parent;
        this.setting = setting;
        initializeComponent();
        this.setVisible(false);
    }

    /**
     * The plugin that owns the chain.
     * <p>
     * Not through {@code audio.plugin}, which is null until the first song has been loaded: this
     * window can be opened straight away, and adding an effect before playing anything is a
     * perfectly ordinary thing to do.
     */
    private static VstPlugin vst() {
        return ChipRegister.shared(VstPlugin.class);
    }

    /** stops whatever is playing, if anything is */
    private void stopPlaying(boolean untilClosed) {
        if (audio.plugin == null) return;
        parent.stop();
        RealChipPlugin real = audio.plugin.chipRegister.plugin(RealChipPlugin.class);
        while (untilClosed ? !real.isThreadClosed() : !real.isThreadStopped()) {
            Thread.yield();
        }
    }

    private void tsbAddVST_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(VstFileChooser.filter());
        ofd.setDialogTitle("Select a VST plugin");
        // a plug-in is a bundle - that is, a directory - on macOS, so one has to be selectable
        ofd.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        ofd.setMultiSelectionEnabled(false);

        String defaultPath = setting.getVst().getDefaultPath();
        if (defaultPath != null && !defaultPath.isEmpty() && Files.exists(Path.of(defaultPath)) && isInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(defaultPath));
        } else {
            File known = VstFileChooser.defaultDirectory();
            if (known != null) ofd.setCurrentDirectory(known);
        }

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = ofd.getSelectedFile();
        File directory = selected.getParentFile();
        if (directory != null) setting.getVst().setDefaultPath(directory.getAbsolutePath());

        stopPlaying(false);
        // the whole path, not just the name - the chooser can be anywhere
        if (!vst().addEffect(selected.getAbsolutePath())) {
            JOptionPane.showMessageDialog(this, selected.getName() + " could not be loaded as a VST effect.",
                    "VST", JOptionPane.ERROR_MESSAGE);
        }
        dispPluginList();
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            isClosed = true;
            setting.getLocation().setPosVSTeffectList(getLocation());
            setVisible(false);
        }

        @Override
        public void windowActivated(WindowEvent e) {
            dispPluginList();
        }

        @Override
        public void windowOpened(WindowEvent e) {
            Point p = setting.getLocation().getPosVSTeffectList();
            if (p != null && (p.x != 0 || p.y != 0)) setLocation(p);
        }
    };

    private List<VstMng.VstInfo2> vstInfos = null;

    public void dispPluginList() {
        model.setRowCount(0);

        vstInfos = vst().getEffects();

        for (VstMng.VstInfo2 vi : vstInfos) {
            if (vi.isInstrument) continue;

            model.addRow(new Object[] {
                    vi.key, vi.fileName, vi.power ? "ON" : "OFF", vi.editor ? "OPENED" : "CLOSED", vi.effectName});
        }
    }

    private void tsmiDelThis_Click(ActionEvent ev) {
        int row = dgvList.getSelectedRow();
        if (row < 0) return;

        stopPlaying(false);
        vst().removeEffect((String) model.getValueAt(row, COLUMN_KEY));
        dispPluginList();
    }

    private void tsmiDelAll_Click(ActionEvent ev) {
        int res = JOptionPane.showConfirmDialog(this, "All VSTs in the VST list will be removed. Are you sure?",
                "VST", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (res != JOptionPane.YES_OPTION) return;

        stopPlaying(true);
        vst().removeEffect("");
        dispPluginList();
    }

    private final MouseAdapter dgvList_CellMouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = dgvList.rowAtPoint(e.getPoint());
            if (row < 0) return;
            dgvList.setRowSelectionInterval(row, row);

            if (e.isPopupTrigger() || e.getButton() != MouseEvent.BUTTON1) {
                tsmiDelThis.setText(dgvList.getSelectedRowCount() > 1 ? "Remove Selected VST" : "Remove this VST");
                cmsVSTEffectList.show(dgvList, e.getX(), e.getY());
                return;
            }

            if (vstInfos == null || row >= vstInfos.size()) return;
            VstMng.VstInfo2 vi = vstInfos.get(row);
            VstMng manager = vst().getManager();

            int column = dgvList.columnAtPoint(e.getPoint());
            try {
                if (column == COLUMN_POWER) {
                    manager.setPower(vi, !vi.power);
                    model.setValueAt(vi.power ? "ON" : "OFF", row, COLUMN_POWER);
                } else if (column == COLUMN_EDITOR) {
                    if (vi.editor) {
                        manager.closeEditor(vi);
                    } else {
                        manager.openEditor(vi);
                    }
                    model.setValueAt(vi.editor ? "OPENED" : "CLOSED", row, COLUMN_EDITOR);
                }
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
            }
        }
    };

    private void initializeComponent() {
        this.model = new DefaultTableModel(new Object[] {"key", "file", "power", "editor", "name"}, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.dgvList = new JTable(this.model);
        this.dgvList.setBackground(Color.black);
        this.dgvList.setForeground(Color.lightGray);
        this.dgvList.setName("dgvList");
        this.dgvList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvList.addMouseListener(this.dgvList_CellMouseClick);
        // the key is what a row is found by, not something to look at
        this.dgvList.getColumnModel().getColumn(COLUMN_KEY).setMinWidth(0);
        this.dgvList.getColumnModel().getColumn(COLUMN_KEY).setMaxWidth(0);
        this.dgvList.getColumnModel().getColumn(COLUMN_FILE).setPreferredWidth(160);
        this.dgvList.getColumnModel().getColumn(COLUMN_POWER).setPreferredWidth(50);
        this.dgvList.getColumnModel().getColumn(COLUMN_EDITOR).setPreferredWidth(70);
        this.dgvList.getColumnModel().getColumn(COLUMN_NAME).setPreferredWidth(120);

        this.toolStripContainer1 = new JPanel(new BorderLayout());
        this.toolStrip1 = new JToolBar();
        this.tsbAddVST = new JButton();
        this.cmsVSTEffectList = new JPopupMenu();
        this.tsmiDelThis = new JMenuItem();
        this.tsmiDelAll = new JMenuItem();

        this.tsbAddVST.setIcon(new ImageIcon(Common.getImage("addPL")));
        this.tsbAddVST.setText("Add VST effect");
        this.tsbAddVST.addActionListener(this::tsbAddVST_Click);
        this.toolStrip1.add(this.tsbAddVST);
        this.toolStripContainer1.add(this.toolStrip1, BorderLayout.NORTH);
        this.toolStripContainer1.add(new JScrollPane(this.dgvList), BorderLayout.CENTER);

        this.tsmiDelThis.setText("Remove this VST");
        this.tsmiDelThis.addActionListener(this::tsmiDelThis_Click);
        this.tsmiDelAll.setText("Remove all VSTs");
        this.tsmiDelAll.addActionListener(this::tsmiDelAll_Click);
        this.cmsVSTEffectList.add(this.tsmiDelThis);
        this.cmsVSTEffectList.addSeparator();
        this.cmsVSTEffectList.add(this.tsmiDelAll);

        this.getContentPane().add(this.toolStripContainer1);
        this.setIconImage(Common.getImage("Feli128"));
        this.setMinimumSize(new Dimension(400, 120));
        this.setPreferredSize(new Dimension(410, 261));
        this.setTitle("VST Effect List");
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.addWindowListener(this.windowListener);
        this.pack();
    }

    private DefaultTableModel model;
    private JTable dgvList;
    private JPanel toolStripContainer1;
    private JToolBar toolStrip1;
    private JButton tsbAddVST;
    private JPopupMenu cmsVSTEffectList;
    private JMenuItem tsmiDelThis;
    private JMenuItem tsmiDelAll;
}
