package mdplayer.vst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;
import javax.sound.sampled.Control;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.JTableHeader;

import dotnet4j.io.Directory;
import dotnet4j.io.Path;
import mdplayer.Audio;
import mdplayer.Setting;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmVSTeffectList extends JFrame {
    private frmMain parent;
    public boolean isClosed = false;
    public Setting setting;
    private boolean isInitialOpenFolder = true;

    static Preferences prefs = Preferences.userNodeForPackage(frmVSTeffectList.class);

    public frmVSTeffectList(frmMain parent, Setting setting) {
        initializeComponent();
        this.setVisible(false);
        this.parent = parent;
        this.setting = setting;
    }

    private void tsbAddVST_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(new FileFilter() {
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".dll"); }
            @Override public String getDescription() { return "VST Pluginファイル(*.dll)"; }
        });
        ofd.setDialogTitle("ファイルを選択してください");
        ofd.setFileFilter(ofd.getChoosableFileFilters()[setting.getOther().getFilterIndex()]);

        if (!setting.getVst().getDefaultPath().isEmpty() && Directory.exists(setting.getVst().getDefaultPath()) && isInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(setting.getVst().getDefaultPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        setting.getVst().setDefaultPath(Path.getDirectoryName(ofd.getSelectedFile().getName()));
        parent.stop();
        while (!Audio.getTrdStopped()) {
            try { Thread.sleep(1); } catch (InterruptedException e) {}
        }
        Audio.addVSTeffect(ofd.getSelectedFile().getName());
        dispPluginList();

    }

    private WindowListener windowListener = new WindowAdapter() {
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
            e.Cancel = true;
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
        dgvList.Rows.clear();

        vstInfos = Audio.getVSTInfos();

        for (VstMng.VstInfo2 vi : vstInfos) {
            if (vi.isInstrument) continue;

            dgvList.Rows.add(vi.key, vi.fileName, vi.power ? "ON" : "OFF", vi.editor ? "OPENED" : "CLOSED", vi.effectName);
        }
    }

    private void tsmiDelThis_Click(ActionEvent ev) {
        if (dgvList.getSelectedRowCount() < 0) return;

        parent.stop();
        while (!Audio.getTrdStopped()) {
            try { Thread.sleep(1); } catch (InterruptedException e) {}
        }
        Audio.delVSTeffect((String) dgvList.Rows[dgvList.SelectedRows[0].Index].Cells["clmKey"].Value);
        dispPluginList();
    }

    private void tsmiDelAll_Click(ActionEvent ev) {
        int res = JOptionPane.showConfirmDialog(null, "VSTリストの全てのVSTが除去されます。よろしいですか。", "PlayList", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res != JFileChooser.APPROVE_OPTION) return;

        parent.stop();
        //while (!Audio.trdStopped) { Thread.sleep(1); }
        while (!Audio.trdClosed) {
            try { Thread.sleep(1); } catch (InterruptedException e) {}
        }
        Audio.delVSTeffect("");
        dispPluginList();
    }

    private void dgvList_CellMouseClick(MouseEvent e) {
        if (e.RowIndex < 0) return;
        dgvList.Rows[e.RowIndex].Selected = true;

        if (e.Button == MouseEvent.BUTTON2) {
            if (dgvList.getSelectedRowCount() > 1) {
                tsmiDelThis.setText("選択したVSTを除去");
            } else {
                tsmiDelThis.setText("このVSTを除去");
            }
            cmsVSTEffectList.setVisible(true);
            Point p = Control.MousePosition;
            cmsVSTEffectList.Top = p.Y;
            cmsVSTEffectList.Left = p.X;
        } else {
            if (vstInfos == null) return;

            if (e.ColumnIndex == 2) {
                vstInfos[e.RowIndex].power = !vstInfos[e.RowIndex].power;
                vstInfos[e.RowIndex].vstPlugins.PluginCommandStub.SetBypass(!vstInfos[e.RowIndex].power);
                dgvList.Rows[e.RowIndex].Cells[2].Value = vstInfos[e.RowIndex].power ? "ON" : "OFF";
            }

            if (e.ColumnIndex == 3) {
                vstInfos[e.RowIndex].editor = !vstInfos[e.RowIndex].editor;
                if (!vstInfos[e.RowIndex].editor) {
                    vstInfos[e.RowIndex].vstPluginsForm.timer1.setEnabled(false);
                    vstInfos[e.RowIndex].location = vstInfos[e.RowIndex].vstPluginsForm.Location;
                    vstInfos[e.RowIndex].vstPluginsForm.Close();
                } else {
                    frmVST dlg = new frmVST(this);
                    dlg.PluginCommandStub = vstInfos[e.RowIndex].vstPlugins.PluginCommandStub;
                    dlg.Show(vstInfos[e.RowIndex]);
                    vstInfos[e.RowIndex].vstPluginsForm = dlg;
                }
                dgvList.Rows[e.RowIndex].Cells[3].Value = vstInfos[e.RowIndex].editor ? "OPENED" : "CLOSED";
            }
        }
    }

    private void initializeComponent() {
//        this.components = new System.ComponentModel.Container();
        JList JListCellStyle1 = new JList();
        JList JListCellStyle3 = new JList();
        JList JListCellStyle4 = new JList();
        JList JListCellStyle2 = new JList();
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmVSTeffectList));
        this.dgvList = new JTable();
        this.clmKey = new JTableHeader();
        this.clmFileName = new JTableHeader();
        this.clmPow = new JTableHeader();
        this.clmEdit = new JTableHeader();
        this.clmName = new JTableHeader();
        this.clmSpacer = new JTableHeader();
        this.toolStripContainer1 = new JToolBar();
        this.toolStrip1 = new JToolBar();
        this.tsbAddVST = new JButton();
        this.toolStripSeparator1 = new JSeparator();
        this.tsbUp = new JButton();
        this.tsbDown = new JButton();
        this.cmsVSTEffectList = new JPopupMenu();
        this.tsmiDelThis = new JMenuItem();
        this.toolStripSeparator2 = new JSeparator();
        this.tsmiDelAll = new JMenuItem();
        //((System.ComponentModel.ISupportInitialize)(this.dgvList)).BeginInit();
//        this.toolStripContainer1.ContentPanel.SuspendLayout();
//        this.toolStripContainer1.TopToolStripPanel.SuspendLayout();
//        this.toolStripContainer1.SuspendLayout();
//        this.toolStrip1.SuspendLayout();
//        this.cmsVSTEffectList.SuspendLayout();

        //
        // dgvList
        //
        this.dgvList.AllowUserToAddRows = false;
        this.dgvList.AllowUserToDeleteRows = false;
        this.dgvList.AllowUserToResizeRows = false;
        this.dgvList.setBackground(Color.black);
//        this.dgvList.BorderStyle = JBorderStyle.None;
//        this.dgvList.CellBorderStyle = JListCellBorderStyle.None;
        JListCellStyle1.Alignment = JListContentAlignment.MiddleLeft;
        JListCellStyle1.setBackground(Color.black);
//        JListCellStyle1.setFont(new Font("メイリオ", 8.25F, Font.PLAIN, ((byte) (128))));
//        JListCellStyle1.ForeColor = Color.MenuHighlight;
//        JListCellStyle1.SelectionColor.setBackground(Color.Highlight);
//        JListCellStyle1.SelectionForeColor = Color.HighlightText;
        JListCellStyle1.WrapMode = JListTriState.False;
        this.dgvList.ColumnHeadersDefaultCellStyle = JListCellStyle1;
        this.dgvList.ColumnHeadersHeight = 20;
        this.dgvList.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.DisableResizing;
        this.dgvList.Columns.AddRange(new JListColumn[] {
                this.clmKey,
                this.clmFileName,
                this.clmPow,
                this.clmEdit,
                this.clmName,
                this.clmSpacer});
        this.dgvList.Dock = JDockStyle.Fill;
        this.dgvList.EditMode = JListEditMode.EditProgrammatically;
        this.dgvList.setLocation(new Point(0, 0));
        this.dgvList.MultiSelect = false;
        this.dgvList.setName("dgvList");
        this.dgvList.RowHeadersBorderStyle = JListHeaderBorderStyle.None;
        JListCellStyle3.Alignment = JListContentAlignment.MiddleLeft;
        JListCellStyle3.setBackground(Color.black);
//        JListCellStyle3.setFont(new Font("メイリオ", Font.BOLD, 8.25F));
//        JListCellStyle3.ForeColor = Color.Window;
//        JListCellStyle3.SelectionColor(Color.Highlight);
//        JListCellStyle3.SelectionForeColor = Color.HighlightText;
        JListCellStyle3.WrapMode = JListTriState.True;
        this.dgvList.RowHeadersDefaultCellStyle = JListCellStyle3;
        this.dgvList.RowHeadersVisible = false;
        JListCellStyle4.setBackground(Color.black);
//        JListCellStyle4.setFont(new Font("メイリオ", 8.25F, Font.BOLD, ((byte) (128))));
        JListCellStyle4.ForeColor = new Color(((byte) (192)), ((byte) (192)), ((byte) (255)));
        this.dgvList.RowsDefaultCellStyle = JListCellStyle4;
        this.dgvList.RowTemplate.DefaultCellStyle.Alignment = JListContentAlignment.MiddleLeft;
        this.dgvList.RowTemplate.getHeight() = 20;
        this.dgvList.RowTemplate.readOnly = true;
        this.dgvList.SelectionMode = JListSelectionMode.FullRowSelect;
        this.dgvList.ShowCellErrors = false;
        this.dgvList.ShowCellToolTips = false;
        this.dgvList.ShowEditingIcon = false;
        this.dgvList.ShowRowErrors = false;
        this.dgvList.setPreferredSize(new Dimension(410, 236));
        // this.dgvList.TabIndex = 1
        this.dgvList.addMouseListener(this.dgvList_CellMouseClick);
        //
        // clmKey
        //
        this.clmKey.HeaderText = "Key";
        this.clmKey.setName("clmKey");
        this.clmKey.SortMode = JListColumnSortMode.NotSortable;
        this.clmKey.setVisible(false);
        //
        // clmFileName
        //
        this.clmFileName.HeaderText = "FileName";
        this.clmFileName.setName("clmFileName");
        this.clmFileName.SortMode = JListColumnSortMode.NotSortable;
        this.clmFileName.setVisible(false);
        //
        // clmPow
        //
        this.clmPow.AutoSizeMode = JListAutoSizeColumnMode.None;
        JListCellStyle2.Alignment = JListContentAlignment.MiddleCenter;
        this.clmPow.DefaultCellStyle = JListCellStyle2;
        this.clmPow.HeaderText = "Pow";
        this.clmPow.setName("clmPow");
        this.clmPow.Resizable = JListTriState.False;
        this.clmPow.SortMode = JListColumnSortMode.NotSortable;
        this.clmPow.ToolTipText = "Power";
        this.clmPow.setWidth(50);
        //
        // clmEdit
        //
        this.clmEdit.HeaderText = "Editor";
        this.clmEdit.setName("clmEdit");
        this.clmEdit.Resizable = JListTriState.False;
        this.clmEdit.SortMode = JListColumnSortMode.NotSortable;
        this.clmEdit.setWidth(60);
        //
        // clmName
        //
        this.clmName.HeaderText = "Name";
        this.clmName.setName("clmName");
        this.clmName.readOnly = true;
        this.clmName.SortMode = JListColumnSortMode.NotSortable;
        this.clmName.setWidth(300);
        //
        // clmSpacer
        //
        this.clmSpacer.AutoSizeMode = JListAutoSizeColumnMode.Fill;
        this.clmSpacer.HeaderText = "";
        this.clmSpacer.setName("clmSpacer");
        this.clmSpacer.readOnly = true;
        this.clmSpacer.SortMode = JListColumnSortMode.NotSortable;
        //
        // toolStripContainer1
        //
        //
        // toolStripContainer1.ContentPanel
        //
        this.toolStripContainer1.ContentPanel.add(this.dgvList);
        this.toolStripContainer1.ContentPanel.setPreferredSize(new Dimension(410, 236));
        this.toolStripContainer1.Dock = JDockStyle.Fill;
        this.toolStripContainer1.setLocation(new Point(0, 0));
        this.toolStripContainer1.setName("toolStripContainer1");
        this.toolStripContainer1.setPreferredSize(new Dimension(410, 261));
        // this.toolStripContainer1.TabIndex = 2
        this.toolStripContainer1.setToolTipText("toolStripContainer1");
        //
        // toolStripContainer1.TopToolStripPanel
        //
        this.toolStripContainer1.TopToolStripPanel.add(this.toolStrip1);
        //
        // toolStrip1
        //
        this.toolStrip1.Dock = JDockStyle.None;
        this.toolStrip1.GripStyle = JToolStripGripStyle.Hidden;
        this.toolStrip1.add(this.tsbAddVST);
        this.toolStrip1.add(this.toolStripSeparator1);
        this.toolStrip1.add(this.tsbUp);
        this.toolStrip1.add(this.tsbDown);
        this.toolStrip1.setLocation(new Point(0, 0));
        this.toolStrip1.setName("toolStrip1");
        this.toolStrip1.setPreferredSize(new Dimension(410, 25));
        this.toolStrip1.Stretch = true;
        // this.toolStrip1.TabIndex = 0
        //
        // tsbAddVST
        //
//        this.tsbAddVST.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbAddVST.setIcon(new ImageIcon(mdplayer.properties.Resources.getaddPL()));
        this.tsbAddVST.ImageTransparentColor = Color.black;
        this.tsbAddVST.setName("tsbAddVST");
        this.tsbAddVST.setPreferredSize(new Dimension(23, 22));
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
//        this.tsbUp.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbUp.setEnabled(false);
        this.tsbUp.setIcon(new ImageIcon(mdplayer.properties.Resources.getupPL()));
        this.tsbUp.ImageTransparentColor = Color.black;
        this.tsbUp.setName("tsbUp");
        this.tsbUp.setPreferredSize(new Dimension(23, 22));
        this.tsbUp.setText("Up VST effect.");
        //
        // tsbDown
        //
//        this.tsbDown.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbDown.setEnabled(false);
        this.tsbDown.setIcon(new ImageIcon(mdplayer.properties.Resources.getDownPL()));
        this.tsbDown.ImageTransparentColor = Color.black;
        this.tsbDown.setName("tsbDown");
        this.tsbDown.setPreferredSize(new Dimension(23, 22));
        this.tsbDown.setText("Down VST effect.");
        //
        // cmsVSTEffectList
        //
        this.cmsVSTEffectList.add(this.tsmiDelThis);
        this.cmsVSTEffectList.add(this.toolStripSeparator2);
        this.cmsVSTEffectList.add(this.tsmiDelAll);
        this.cmsVSTEffectList.setName("cmsVSTEffectList");
        this.cmsVSTEffectList.setPreferredSize(new Dimension(158, 54));
        //
        // tsmiDelThis
        //
        this.tsmiDelThis.setName("tsmiDelThis");
        this.tsmiDelThis.setPreferredSize(new Dimension(157, 22));
        this.tsmiDelThis.setText("このVSTを除去");
        this.tsmiDelThis.addActionListener(this::tsmiDelThis_Click);
        //
        // toolStripSeparator2
        //
        this.toolStripSeparator2.setName("toolStripSeparator2");
        this.toolStripSeparator2.setPreferredSize(new Dimension(154, 6));
        //
        // tsmiDelAll
        //
        this.tsmiDelAll.setName("tsmiDelAll");
        this.tsmiDelAll.setPreferredSize(new Dimension(157, 22));
        this.tsmiDelAll.setText("全てのVSTを除去");
        this.tsmiDelAll.addActionListener(this::tsmiDelAll_Click);
        //
        // frmVSTeffectList
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(410, 261));
        this.getContentPane().add(this.toolStripContainer1);
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.KeyPreview = true;
        this.setMinimumSize(new Dimension(400, 120));
        this.setName("frmVSTeffectList");
        this.setOpacity(0f);
        this.setTitle("VST Effect List");
        this.addWindowListener(this.windowListener);
        //((System.ComponentModel.ISupportInitialize)(this.dgvList)).EndInit();
        // this.toolStripContainer1.ContentPanel.ResumeLayout(false);
        // this.toolStripContainer1.TopToolStripPanel.ResumeLayout(false);
        // this.toolStripContainer1.TopToolStripPanel.PerformLayout();
        // this.toolStripContainer1.ResumeLayout(false);
        // this.toolStripContainer1.PerformLayout();
        // this.toolStrip1.ResumeLayout(false);
        // this.toolStrip1.PerformLayout();
        // this.cmsVSTEffectList.ResumeLayout(false);
//        this.ResumeLayout(false);
    }

    private JTable dgvList;
    private JToolBar toolStripContainer1;
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
