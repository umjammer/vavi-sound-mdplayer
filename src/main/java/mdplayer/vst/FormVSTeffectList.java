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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.form.sys.FormMain;


public class FormVSTeffectList extends JFrame {

    private final FormMain parent;
    public boolean isClosed = false;
    public final Setting setting;
    private static final boolean isInitialOpenFolder = true;
    final Audio audio = Audio.getInstance();

    static final Preferences prefs = Preferences.userNodeForPackage(FormVSTeffectList.class);

    public FormVSTeffectList(FormMain parent, Setting setting) {
        initializeComponent();
        this.setVisible(false);
        this.parent = parent;
        this.setting = setting;
    }

    private void tsbAddVST_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".dll");
            }

            @Override
            public String getDescription() {
                return "VST Plugin file (*.dll)";
            }
        });
        ofd.setDialogTitle("Select a file");
        int filterIndex = setting.getOther().getFilterIndex();
        FileFilter[] filters = ofd.getChoosableFileFilters();
        if (filterIndex >= 0 && filterIndex < filters.length) {
            ofd.setFileFilter(filters[filterIndex]);
        }

        if (!setting.getVst().getDefaultPath().isEmpty() && Files.exists(Path.of(setting.getVst().getDefaultPath())) && isInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(setting.getVst().getDefaultPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        setting.getVst().setDefaultPath(Path.of(ofd.getSelectedFile().getName()).getParent().toString());
        parent.stop();
        while (!audio.plugin.chipRegister.plugin(RealChipPlugin.class).isThreadStopped()) {
            Thread.yield();
        }
        audio.plugin.chipRegister.plugin(VstPlugin.class).addVSTeffect(ofd.getSelectedFile().getName());
        dispPluginList();

    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            isClosed = true;
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().setPosVSTeffectList(getLocation());
            } else {
                parent.setting.getLocation().setPosVSTeffectList(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
            }
            //setting.location.PPlayListWH = new Point(this.getWidth(), this.getHeight());
            setVisible(false);
//            e.Cancel = true;
        }

        @Override
        public void windowActivated(WindowEvent e) {
            dispPluginList();
        }

        @Override
        public void windowOpened(WindowEvent e) {
            setLocation(new Point((int) setting.getLocation().getPosVSTeffectList().getX(), (int) setting.getLocation().getPosVSTeffectList().getY()));
        }
    };

    private List<VstMng.VstInfo2> vstInfos = null;

    public void dispPluginList() {
        model.setRowCount(0);

        vstInfos = audio.plugin.chipRegister.plugin(VstPlugin.class).getVSTInfos();

        int i = 0;
        for (VstMng.VstInfo2 vi : vstInfos) {
            if (vi.isInstrument) continue;

            model.insertRow(i++, new Object[] {vi.key, vi.fileName, vi.power ? "ON" : "OFF", vi.editor ? "OPENED" : "CLOSED", vi.effectName});
        }
    }

    private void tsmiDelThis_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 0) return;

        parent.stop();
        while (!audio.plugin.chipRegister.plugin(RealChipPlugin.class).isThreadStopped()) {
            Thread.yield();
        }
        int row = dgvList.getSelectionModel().getSelectedIndices()[0];
        audio.plugin.chipRegister.plugin(VstPlugin.class).delVSTeffect((String) model.getValueAt(row, 1 /* clmKey */));
        dispPluginList();
    }

    private void tsmiDelAll_Click(ActionEvent ev) {
        int res = JOptionPane.showConfirmDialog(null, "All VSTs in the VST list will be removed. Are you sure?", "PlayList", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (res != JFileChooser.APPROVE_OPTION) return;

        parent.stop();
        //while (!Audio.trdStopped) { Thread.sleep(1); }
        while (!audio.plugin.chipRegister.plugin(RealChipPlugin.class).isThreadClosed()) {
            Thread.yield();
        }
        audio.plugin.chipRegister.plugin(VstPlugin.class).delVSTeffect("");
        dispPluginList();
    }

    private final MouseAdapter dgvList_CellMouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = dgvList.rowAtPoint(e.getPoint());
            if (row < 0) return;
            dgvList.getSelectionModel().setLeadSelectionIndex(row);

            if (e.getButton() == MouseEvent.BUTTON2) {
                if (dgvList.getSelectedRowCount() > 1) {
                    tsmiDelThis.setText("Remove Selected VST");
                } else {
                    tsmiDelThis.setText("Remove this VST");
                }
                cmsVSTEffectList.setVisible(true);
                cmsVSTEffectList.setLocation(e.getX(), e.getY());
            } else {
                if (vstInfos == null) return;

                int column = dgvList.columnAtPoint(e.getPoint());
                if (column == 2) {
                    vstInfos.get(row).power = !vstInfos.get(row).power;
                    if (vstInfos.get(row).power)
                        vstInfos.get(row).vstPlugins.open();
                    else
                        vstInfos.get(row).vstPlugins.close();
                    model.setValueAt(vstInfos.get(row).power ? "ON" : "OFF", row, 2);
                }

                if (column == 3) {
                    vstInfos.get(row).editor = !vstInfos.get(row).editor;
                    if (!vstInfos.get(row).editor) {
                        vstInfos.get(row).vstPluginsForm.timer1.stop();
                        vstInfos.get(row).location = vstInfos.get(row).vstPluginsForm.getLocation();
                        vstInfos.get(row).vstPluginsForm.setVisible(false);
                    } else {
                        FormVST dlg = new FormVST(FormVSTeffectList.this);
                        dlg.setPluginCommandStub(vstInfos.get(row).vstPlugins);
                        dlg.Show(vstInfos.get(row));
                        vstInfos.get(row).vstPluginsForm = dlg;
                    }
                    model.setValueAt(vstInfos.get(row).editor ? "OPENED" : "CLOSED", row, 3);
                }
            }
        }
    };

    private void initializeComponent() {
        this.model = new DefaultTableModel();

        JList<String> JListCellStyle1 = new JList<>();
        JList<String> JListCellStyle3 = new JList<>();
        JList<String> JListCellStyle4 = new JList<>();
        JList<String> JListCellStyle2 = new JList<>();
        this.dgvList = new JTable();
        this.clmKey = new JTableHeader();
        this.clmFileName = new JTableHeader();
        this.clmPow = new JTableHeader();
        this.clmEdit = new JTableHeader();
        this.clmName = new JTableHeader();
        this.clmSpacer = new JTableHeader();
        this.toolStripContainer1 = new JPanel();
        this.toolStrip1 = new JToolBar();
        this.tsbAddVST = new JButton();
        this.toolStripSeparator1 = new JSeparator();
        this.tsbUp = new JButton();
        this.tsbDown = new JButton();
        this.cmsVSTEffectList = new JPopupMenu();
        this.tsmiDelThis = new JMenuItem();
        this.toolStripSeparator2 = new JSeparator();
        this.tsmiDelAll = new JMenuItem();

        //
        // dgvList
        //
        this.dgvList.setBackground(Color.black);
        JListCellStyle1.setBackground(Color.black);
        this.dgvList.setLocation(new Point(0, 0));
        this.dgvList.setName("dgvList");
        JListCellStyle3.setBackground(Color.black);
        JListCellStyle4.setBackground(Color.black);
        this.dgvList.setPreferredSize(new Dimension(410, 236));
        this.dgvList.addMouseListener(this.dgvList_CellMouseClick);
        //
        // clmKey
        //
        this.clmKey.setName("clmKey");
        this.clmKey.setVisible(false);
        //
        // clmFileName
        //
        this.clmFileName.setVisible(false);
        //
        // clmPow
        //
//        this.clmPow.setName("clmPow");
        //
        // clmEdit
        //
//        this.clmEdit.setName("clmEdit");
        //
        // clmName
        //
//        this.clmName.setName("clmName");
        //
        // clmSpacer
        //
//        this.clmSpacer.setName("clmSpacer");
        //
        // toolStripContainer1
        //
        //
        // toolStripContainer1.ContentPanel
        //
        this.toolStripContainer1.setLayout(new BorderLayout());
        //
        // toolStripContainer1.TopToolStripPanel
        //
        //
        // toolStrip1
        //
        this.toolStrip1.add(this.tsbAddVST);
        this.toolStrip1.add(this.toolStripSeparator1);
        this.toolStrip1.add(this.tsbUp);
        this.toolStrip1.add(this.tsbDown);
        this.toolStripContainer1.add(this.toolStrip1, BorderLayout.NORTH);
        //
        // tsbAddVST
        //
        this.tsbAddVST.setIcon(new ImageIcon(Common.getImage("addPL")));
        this.tsbAddVST.setText("Add VST effect.");
        this.tsbAddVST.addActionListener(this::tsbAddVST_Click);
        //
        // toolStripSeparator1
        //
        this.toolStripSeparator1.setName("toolStripSeparator1");
        this.toolStripSeparator1.setPreferredSize(new Dimension(6, 25));
        //
        // tsbUp
        //
        this.tsbUp.setEnabled(false);
        this.tsbUp.setIcon(new ImageIcon(Common.getImage("upPL")));
        this.tsbUp.setText("Up VST effect.");
        //
        // tsbDown
        //
        this.tsbDown.setEnabled(false);
        this.tsbDown.setIcon(new ImageIcon(Common.getImage("downPL")));
        this.tsbDown.setText("Down VST effect.");
        //
        // cmsVSTEffectList
        //
        this.cmsVSTEffectList.add(this.tsmiDelThis);
        this.cmsVSTEffectList.add(this.toolStripSeparator2);
        this.cmsVSTEffectList.add(this.tsmiDelAll);
        //
        // tsmiDelThis
        //
        this.tsmiDelThis.setText("Remove this VST");
        this.tsmiDelThis.addActionListener(this::tsmiDelThis_Click);
        //
        // toolStripSeparator2
        //
        //
        // tsmiDelAll
        //
        this.tsmiDelAll.setText("Remove all VSTs");
        this.tsmiDelAll.addActionListener(this::tsmiDelAll_Click);
        //
        // frmVSTeffectList
        //
        this.setPreferredSize(new Dimension(410, 261));
        this.getContentPane().add(this.toolStripContainer1);
        this.setIconImage(Common.getImage("Feli128"));
        this.setMinimumSize(new Dimension(400, 120));
        this.setOpacity(0f);
        this.setTitle("VST Effect List");
        this.addWindowListener(this.windowListener);
    }

    private DefaultTableModel model;
    private JTable dgvList;
    private JPanel toolStripContainer1;
    private JToolBar toolStrip1;
    private JButton tsbAddVST;
    private JSeparator toolStripSeparator1;
    private JButton tsbUp;
    private JButton tsbDown;
    private JTableHeader clmKey;
    private JTableHeader clmFileName;
    private JTableHeader clmPow;
    private JTableHeader clmEdit;
    private JTableHeader clmName;
    private JTableHeader clmSpacer;
    private JPopupMenu cmsVSTEffectList;
    private JMenuItem tsmiDelThis;
    private JSeparator toolStripSeparator2;
    private JMenuItem tsmiDelAll;
}
