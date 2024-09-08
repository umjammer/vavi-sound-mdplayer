package mdplayer.form.sys;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import mdplayer.Setting;
import mdplayer.Tone;
import mdplayer.TonePallet;


public class frmTPPut extends JFrame {
    private Setting setting = null;
    private TonePallet tonePallet = null;
    int DialogResult;

    public frmTPPut() {
        initializeComponent();
    }

    public int ShowDialog(Setting setting, TonePallet tonePallet) {
        this.setting = setting;
        this.tonePallet = tonePallet;

        this.setVisible(true);
        return DialogResult;
    }

    private WindowListener frmTPPut_Load = new WindowAdapter() {
        @Override
        public void windowActivated(WindowEvent e) {
            DefaultTableModel m = (DefaultTableModel) dgvTonePallet.getModel();
            m.setRowCount(0);
            if (tonePallet == null) tonePallet = new TonePallet();
            if (tonePallet.getLstTone() == null) tonePallet.setLstTone(new ArrayList<>(256));

            for (int i = 0; i < 256; i++) {
                String toneName;
                if (tonePallet.getLstTone().size() < i + 1 || tonePallet.getLstTone().get(i) == null) {
                    tonePallet.getLstTone().add(new Tone());
                }

                toneName = tonePallet.getLstTone().get(i).name;

                m.addRow(new Object[] {i, toneName});
            }
        }
    };

    private void dgvTonePallet_CellEndEdit(ListSelectionEvent e) {
        dgvTonePallet.getModel().setValueAt(e.getFirstIndex() + "*", e.getFirstIndex(), 0);
        btApply.setEnabled(true);
    }

    private void btChn_Click(ActionEvent ev) {
        int[] cc = dgvTonePallet.getSelectedColumns();
        if (cc.length != 1) return;
        if (dgvTonePallet.getSelectedRowCount() < 1) return;
        int row = dgvTonePallet.getSelectedRows()[0];

        String m = String.format("from Ch.%s", ((Button) ev.getSource()).getActionCommand());
        Object v = dgvTonePallet.getModel().getValueAt(row, cols.clmName.ordinal());
        String n = v == null ? "" : v.toString();
        dgvTonePallet.getModel().setValueAt(m.equals(n) ? "" : m, row, cols.clmName.ordinal());

        btApply.setEnabled(true);
    }

    private void btApply_Click(ActionEvent ev) {
        updateToneNames();
        updateTone();
        btApply.setEnabled(false);
    }

    private void btOK_Click(ActionEvent ev) {
        updateToneNames();
        updateTone();
        this.setVisible(false);
    }

    private void updateToneNames() {
        for (int i = 0; i < 256; i++) {
            dgvTonePallet.getModel().setValueAt(i, 0, i);
            tonePallet.getLstTone().get(i).name = dgvTonePallet.getModel().getValueAt(i, 1).toString();
        }
    }

    private void updateTone() {
        for (int i = 0; i < 256; i++) {
            Object o = dgvTonePallet.getModel().getValueAt(i, cols.clmName.ordinal());
            String n = o == null ? "" : o.toString();
            if (n.isEmpty()) continue;

            int ch = Integer.parseInt(n.replace("from Ch.", "")) - 1;

            CopySettingToneToTonePallet(ch, i);

            dgvTonePallet.getModel().setValueAt("", i, cols.clmName.ordinal());
        }
    }

    private void CopySettingToneToTonePallet(int ch, int ind) {
        for (int i = 0; i < 4; i++) {
            tonePallet.getLstTone().get(ind).ops[i].ar = setting.getMidiKbd().getTones()[ch].ops[i].ar; // AR
            tonePallet.getLstTone().get(ind).ops[i].ks = setting.getMidiKbd().getTones()[ch].ops[i].ks; // KS
            tonePallet.getLstTone().get(ind).ops[i].dr = setting.getMidiKbd().getTones()[ch].ops[i].dr; // DR
            tonePallet.getLstTone().get(ind).ops[i].am = setting.getMidiKbd().getTones()[ch].ops[i].am; // AM
            tonePallet.getLstTone().get(ind).ops[i].sr = setting.getMidiKbd().getTones()[ch].ops[i].sr; // SR
            tonePallet.getLstTone().get(ind).ops[i].rr = setting.getMidiKbd().getTones()[ch].ops[i].rr; // RR
            tonePallet.getLstTone().get(ind).ops[i].sl = setting.getMidiKbd().getTones()[ch].ops[i].sl; // SL
            tonePallet.getLstTone().get(ind).ops[i].tl = setting.getMidiKbd().getTones()[ch].ops[i].tl; // TL
            tonePallet.getLstTone().get(ind).ops[i].ml = setting.getMidiKbd().getTones()[ch].ops[i].ml; // ML
            tonePallet.getLstTone().get(ind).ops[i].dt = setting.getMidiKbd().getTones()[ch].ops[i].dt; // DT
            tonePallet.getLstTone().get(ind).ops[i].dt2 = setting.getMidiKbd().getTones()[ch].ops[i].dt2; // DT2
        }

        tonePallet.getLstTone().get(ind).al = setting.getMidiKbd().getTones()[ch].al; // AL
        tonePallet.getLstTone().get(ind).fb = setting.getMidiKbd().getTones()[ch].fb; // FB
        tonePallet.getLstTone().get(ind).ams = setting.getMidiKbd().getTones()[ch].ams;
        tonePallet.getLstTone().get(ind).pms = setting.getMidiKbd().getTones()[ch].pms;

    }

    enum cols {
        __dummy__,
        clmNo,
        clmName,
        clmSpacer
    }

    private void initializeComponent() {
        this.groupBox1 = new JPanel();
        this.btCh6 = new JButton();
        this.btCh5 = new JButton();
        this.btCh4 = new JButton();
        this.btCh3 = new JButton();
        this.btCh2 = new JButton();
        this.btCh1 = new JButton();
        this.dgvTonePallet = new JTable();
        this.clmNo = new JList();
        this.clmName = new JList();
        this.clmSpacer = new JList();
        this.label1 = new JLabel();
        this.btnCancel = new JButton();
        this.btOK = new JButton();
        this.btApply = new JButton();
//        this.groupBox1.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvTonePallet)).BeginInit();

        //
        // groupBox1
        //
        this.groupBox1.add(this.btCh6);
        this.groupBox1.add(this.btCh5);
        this.groupBox1.add(this.btCh4);
        this.groupBox1.add(this.btCh3);
        this.groupBox1.add(this.btCh2);
        this.groupBox1.add(this.btCh1);
        this.groupBox1.setLocation(new Point(12, 12));
        this.groupBox1.setName("groupBox1");
        this.groupBox1.setPreferredSize(new Dimension(264, 81));
        // this.groupBox1.TabIndex = 1
        // this.groupBox1.TabStop = false;
        this.groupBox1.setToolTipText("Ym2612Inst(From)");
        //
        // btCh6
        //
        this.btCh6.setLocation(new Point(178, 48));
        this.btCh6.setName("btCh6");
        this.btCh6.setPreferredSize(new Dimension(80, 24));
        // this.btCh6.TabIndex = 0
        this.btCh6.setActionCommand("6");
        this.btCh6.setText("Ch.6");
        // this.btCh6.UseVisualStyl.setBackground(true);
        this.btCh6.addActionListener(this::btChn_Click);
        //
        // btCh5
        //
        this.btCh5.setLocation(new Point(92, 48));
        this.btCh5.setName("btCh5");
        this.btCh5.setPreferredSize(new Dimension(80, 24));
        // this.btCh5.TabIndex = 0
        this.btCh5.setActionCommand("5");
        this.btCh5.setText("Ch.5");
        // this.btCh5.UseVisualStyl.setBackground(true);
        this.btCh5.addActionListener(this::btChn_Click);
        //
        // btCh4
        //
        this.btCh4.setLocation(new Point(6, 48));
        this.btCh4.setName("btCh4");
        this.btCh4.setPreferredSize(new Dimension(80, 24));
        // this.btCh4.TabIndex = 0
        this.btCh4.setActionCommand("4");
        this.btCh4.setText("Ch.4");
        // this.btCh4.UseVisualStyl.setBackground(true);
        this.btCh4.addActionListener(this::btChn_Click);
        //
        // btCh3
        //
        this.btCh3.setLocation(new Point(178, 18));
        this.btCh3.setName("btCh3");
        this.btCh3.setPreferredSize(new Dimension(80, 24));
        // this.btCh3.TabIndex = 0
        this.btCh3.setActionCommand("3");
        this.btCh3.setText("Ch.3");
        // this.btCh3.UseVisualStyl.setBackground(true);
        this.btCh3.addActionListener(this::btChn_Click);
        //
        // btCh2
        //
        this.btCh2.setLocation(new Point(92, 18));
        this.btCh2.setName("btCh2");
        this.btCh2.setPreferredSize(new Dimension(80, 24));
        // this.btCh2.TabIndex = 0
        this.btCh2.setActionCommand("2");
        this.btCh2.setText("Ch.2");
        // this.btCh2.UseVisualStyl.setBackground(true);
        this.btCh2.addActionListener(this::btChn_Click);
        //
        // btCh1
        //
        this.btCh1.setLocation(new Point(6, 18));
        this.btCh1.setName("btCh1");
        this.btCh1.setPreferredSize(new Dimension(80, 24));
        // this.btCh1.TabIndex = 0
        this.btCh1.setActionCommand("1");
        this.btCh1.setText("Ch.1");
        // this.btCh1.UseVisualStyl.setBackground(true);
        this.btCh1.addActionListener(this::btChn_Click);
        //
        // dgvTonePallet
        //
//        this.dgvTonePallet.AllowUserToAddRows = false;
//        this.dgvTonePallet.AllowUserToDeleteRows = false;
        this.dgvTonePallet.getTableHeader().setReorderingAllowed(true);
        this.dgvTonePallet.getTableHeader().setResizingAllowed(false);
//        this.dgvTonePallet.Anchor = ((JAnchorStyles) ((((JAnchorStyles.Top | JAnchorStyles.Bottom)
//                | JAnchorStyles.Left)
//                | JAnchorStyles.Right)));
//        this.dgvTonePallet.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        this.dgvTonePallet.setLocation(new Point(12, 121));
        this.dgvTonePallet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvTonePallet.setName("dgvTonePallet");
//        this.dgvTonePallet.RowHeadersVisible = false;
//        this.dgvTonePallet.RowTemplate.getHeight() = 21;
        this.dgvTonePallet.setPreferredSize(new Dimension(264, 81));
        // this.dgvTonePallet.TabIndex = 3
        this.dgvTonePallet.getSelectionModel().addListSelectionListener(this::dgvTonePallet_CellEndEdit);
        //
        // clmNo
        //
//        this.clmNo.Frozen = true;
//        this.clmNo.HeaderText = "No.";
//        this.clmNo.setName("clmNo");
//        this.clmNo.readOnly = true;
//        this.clmNo.Resizable = JListTriState.False;
//        this.clmNo.setWidth(60);
        //
        // clmName
        //
//        this.clmName.Frozen = true;
//        this.clmName.HeaderText = "Name";
//        this.clmName.setName("clmName");
//        this.clmName.setWidth(150);
        //
        // clmSpacer
        //
//        this.clmSpacer.AutoSizeMode = JListAutoSizeColumnMode.Fill;
//        this.clmSpacer.HeaderText = "";
//        this.clmSpacer.setName("clmSpacer");
//        this.clmSpacer.readOnly = true;
//        this.clmSpacer.Resizable = JListTriState.False;
        //
        // label1
        //
//            this.label1.AutoSize(true);
        this.label1.setLocation(new Point(16, 106));
        this.label1.setName("label1");
        this.label1.setPreferredSize(new Dimension(84, 12));
        // this.label1.TabIndex = 2
        this.label1.setText("Tone Pallet(To)");
        //
        // btnCancel
        //
        this.btnCancel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.btnCancel.setVerticalAlignment(SwingConstants.BOTTOM);
        this.btnCancel.addActionListener(e -> DialogResult = JOptionPane.NO_OPTION);
        this.btnCancel.setLocation(new Point(120, 208));
        this.btnCancel.setName("btnCancel");
        this.btnCancel.setPreferredSize(new Dimension(75, 23));
        // this.btnCancel.TabIndex = 0
        this.btnCancel.setText("キャンセル");
        // this.btnCancel.UseVisualStyl.setBackground(true);
        //
        // btOK
        //
        this.btOK.setHorizontalAlignment(SwingConstants.RIGHT);
        this.btOK.setVerticalAlignment(SwingConstants.BOTTOM);
        this.btOK.setLocation(new Point(39, 208));
        this.btOK.setName("btOK");
        this.btOK.setPreferredSize(new Dimension(75, 23));
        // this.btOK.TabIndex = 4
        this.btOK.setText("OK");
        // this.btOK.UseVisualStyl.setBackground(true);
        this.btOK.addActionListener(this::btOK_Click);
        //
        // btApply
        //
        this.btApply.setHorizontalAlignment(SwingConstants.RIGHT);
        this.btApply.setVerticalAlignment(SwingConstants.BOTTOM);
        this.btApply.setEnabled(false);
        this.btApply.setLocation(new Point(201, 208));
        this.btApply.setName("btApply");
        this.btApply.setPreferredSize(new Dimension(75, 23));
        // this.btApply.TabIndex = 5
        this.btApply.setText("適用");
        // this.btApply.UseVisualStyl.setBackground(true);
        this.btApply.addActionListener(this::btApply_Click);
        //
        // frmTPPut
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
//        this.addActionListener(e -> { dialogResult = JOptionPane.NO_OPTION; });
        this.setPreferredSize(new Dimension(288, 243));
        this.getContentPane().add(this.btApply);
        this.getContentPane().add(this.btOK);
        this.getContentPane().add(this.btnCancel);
        this.getContentPane().add(this.label1);
        this.getContentPane().add(this.dgvTonePallet);
        this.getContentPane().add(this.groupBox1);
//        this.FormBorderStyle = JFormBorderStyle.SizableToolWindow;
//        this.MinimizeBox = false;
        this.setMinimumSize(new Dimension(304, 282));
        this.setName("frmTPPut");
//        this.StartPosition = JFormStartPosition.CenterParent;
        this.setTitle("Put to Tone Pallet");
        this.addWindowListener(this.frmTPPut_Load);
        // this.groupBox1.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvTonePallet)).EndInit();
//        this.ResumeLayout(false);
//        this.PerformLayout();
    }

    private JPanel groupBox1;
    private JTable dgvTonePallet;
    private JLabel label1;
    private JButton btnCancel;
    private JButton btOK;
    private JList clmNo;
    private JList clmName;
    private JList clmSpacer;
    private JButton btApply;
    private JButton btCh1;
    private JButton btCh6;
    private JButton btCh5;
    private JButton btCh4;
    private JButton btCh3;
    private JButton btCh2;
}
