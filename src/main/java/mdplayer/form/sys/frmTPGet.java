package mdplayer.form.sys;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import mdplayer.Setting;
import mdplayer.Tone;
import mdplayer.TonePallet;


public class frmTPGet extends JDialog {

    private void initializeComponent() {
        this.groupBox1 = new JPanel();
        this.btCh6 = new JButton();
        this.btCh3 = new JButton();
        this.btCh5 = new JButton();
        this.btCh2 = new JButton();
        this.btCh4 = new JButton();
        this.btCh1 = new JButton();
        this.dgvTonePallet = new JList();
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
        this.btnCancel.setHorizontalAlignment(SwingConstants.LEFT);
        this.btnCancel.setVerticalAlignment(SwingConstants.BOTTOM);
        this.groupBox1.add(this.btCh6);
        this.groupBox1.add(this.btCh3);
        this.groupBox1.add(this.btCh5);
        this.groupBox1.add(this.btCh2);
        this.groupBox1.add(this.btCh4);
        this.groupBox1.add(this.btCh1);
        this.groupBox1.setLocation(new Point(12, 121));
        this.groupBox1.setName("groupBox1");
        this.groupBox1.setPreferredSize(new Dimension(264, 81));
        // this.groupBox1.TabIndex = 3
        // this.groupBox1.TabStop = false;
        this.groupBox1.setToolTipText("YM2612(To)");
        //
        // btCh6
        //
        this.btCh6.setLocation(new Point(178, 48));
        this.btCh6.setName("btCh6");
        this.btCh6.setPreferredSize(new Dimension(80, 24));
        // this.btCh6.TabIndex = 1
        this.btCh6.setActionCommand("6");
        this.btCh6.setText("Ch.6");
        // this.btCh6.UseVisualStyl.setBackground(true);
        this.btCh6.addActionListener(this::btChn_Click);
        //
        // btCh3
        //
        this.btCh3.setLocation(new Point(178, 18));
        this.btCh3.setName("btCh3");
        this.btCh3.setPreferredSize(new Dimension(80, 24));
        // this.btCh3.TabIndex = 1
        this.btCh3.setActionCommand("3");
        this.btCh3.setText("Ch.3");
        // this.btCh3.UseVisualStyl.setBackground(true);
        this.btCh3.addActionListener(this::btChn_Click);
        //
        // btCh5
        //
        this.btCh5.setLocation(new Point(92, 48));
        this.btCh5.setName("btCh5");
        this.btCh5.setPreferredSize(new Dimension(80, 24));
        // this.btCh5.TabIndex = 1
        this.btCh5.setActionCommand("5");
        this.btCh5.setText("Ch.5");
        // this.btCh5.UseVisualStyl.setBackground(true);
        this.btCh5.addActionListener(this::btChn_Click);
        //
        // btCh2
        //
        this.btCh2.setLocation(new Point(92, 18));
        this.btCh2.setName("btCh2");
        this.btCh2.setPreferredSize(new Dimension(80, 24));
        // this.btCh2.TabIndex = 1
        this.btCh2.setActionCommand("2");
        this.btCh2.setText("Ch.2");
        // this.btCh2.UseVisualStyl.setBackground(true);
        this.btCh2.addActionListener(this::btChn_Click);
        //
        // btCh4
        //
        this.btCh4.setLocation(new Point(6, 48));
        this.btCh4.setName("btCh4");
        this.btCh4.setPreferredSize(new Dimension(80, 24));
        // this.btCh4.TabIndex = 1
        this.btCh4.setActionCommand("4");
        this.btCh4.setText("Ch.4");
        // this.btCh4.UseVisualStyl.setBackground(true);
        this.btCh4.addActionListener(this::btChn_Click);
        //
        // btCh1
        //
        this.btCh1.setLocation(new Point(6, 18));
        this.btCh1.setName("btCh1");
        this.btCh1.setPreferredSize(new Dimension(80, 24));
        // this.btCh1.TabIndex = 1
        this.btCh1.setActionCommand("1");
        this.btCh1.setText("Ch.1");
        // this.btCh1.UseVisualStyl.setBackground(true);
        this.btCh1.addActionListener(this::btChn_Click);
        //
        // dgvTonePallet
        //
        this.dgvTonePallet.AllowUserToAddRows = false;
        this.dgvTonePallet.AllowUserToDeleteRows = false;
        this.dgvTonePallet.AllowUserToOrderColumns = true;
        this.dgvTonePallet.AllowUserToResizeRows = false;
//        this.dgvTonePallet.Anchor = ((JAnchorStyles) ((((JAnchorStyles.Top | JAnchorStyles.Bottom)
//                | JAnchorStyles.Left)
//                | JAnchorStyles.Right)));
        this.dgvTonePallet.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        this.dgvTonePallet.Columns.AddRange(new JListColumn[] {
                this.clmNo,
                this.clmName,
                this.clmSpacer});
        this.dgvTonePallet.setLocation(new Point(12, 24));
        this.dgvTonePallet.setName("dgvTonePallet");
        this.dgvTonePallet.readOnly = true;
        this.dgvTonePallet.RowHeadersVisible = false;
        this.dgvTonePallet.RowTemplate.getHeight() = 21;
        this.dgvTonePallet.setPreferredSize(new Dimension(264, 81));
        // this.dgvTonePallet.TabIndex = 2
        //
        // clmNo
        //
        this.clmNo.Frozen = true;
        this.clmNo.HeaderText = "No.";
        this.clmNo.setName("clmNo");
        this.clmNo.readOnly = true;
        this.clmNo.Resizable = JListTriState.False;
        this.clmNo.setWidth(60);
        //
        // clmName
        //
        this.clmName.Frozen = true;
        this.clmName.HeaderText = "Name";
        this.clmName.setName("clmName");
        this.clmName.readOnly = true;
        this.clmName.setWidth(150);
        //
        // clmSpacer
        //
        this.clmSpacer.AutoSizeMode = JListAutoSizeColumnMode.Fill;
        this.clmSpacer.HeaderText = "";
        this.clmSpacer.setName("clmSpacer");
        this.clmSpacer.readOnly = true;
        this.clmSpacer.Resizable = JListTriState.False;
        //
        // label1
        //
//            this.label1.AutoSize = true;
        this.label1.setLocation(new Point(16, 9));
        this.label1.setName("label1");
        this.label1.setPreferredSize(new Dimension(97, 12));
        // this.label1.TabIndex = 1
        this.label1.setText("Tone Pallet(From)");
        //
        // btnCancel
        //
        this.btnCancel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.btnCancel.setVerticalAlignment(SwingConstants.BOTTOM);
        this.btnCancel.DialogResult = JDialogResult.Cancel;
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
        // this.btApply.TabIndex = 4
        this.btApply.setText("適用");
        // this.btApply.UseVisualStyl.setBackground(true);
        this.btApply.addActionListener(this::btApply_Click);
        //
        // frmTPGet
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.CancelButton = this.btnCancel;
        this.setPreferredSize(new Dimension(288, 243));
        this.getContentPane().add(this.btApply);
        this.getContentPane().add(this.btOK);
        this.getContentPane().add(this.btnCancel);
        this.getContentPane().add(this.label1);
        this.getContentPane().add(this.dgvTonePallet);
        this.getContentPane().add(this.groupBox1);
        this.FormBorderStyle = JFormBorderStyle.SizableToolWindow;
        this.MinimizeBox = false;
        this.setMinimumSize(new Dimension(304, 282));
        this.setName("frmTPGet");
        this.StartPosition = JFormStartPosition.CenterParent;
        this.setTitle("Get from Tone Pallet");
        this.addComponentListener(this.componentListener);
        // this.groupBox1.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvTonePallet)).EndInit();
//        this.ResumeLayout(false);
//        this.PerformLayout();
    }

    // // #endregion
    private JPanel groupBox1;
    private JList dgvTonePallet;
    private JLabel label1;
    private JButton btnCancel;
    private JButton btOK;
    private JList clmNo;
    private JList clmName;
    private JList clmSpacer;
    private JButton btCh6;
    private JButton btCh3;
    private JButton btCh5;
    private JButton btCh2;
    private JButton btCh4;
    private JButton btCh1;
    private JButton btApply;

    private Setting setting = null;
    private TonePallet tonePallet = null;

    public frmTPGet() {
        initializeComponent();
    }

    public int ShowDialog(Setting setting, TonePallet tonePallet) {
        this.setting = setting;
        this.tonePallet = tonePallet;

        return this.setVisible(true);
    }

    private void frmTPGet_Load(ActionEvent ev) {
        dgvTonePallet.Rows.clear();
        if (tonePallet == null) tonePallet = new TonePallet();
        if (tonePallet.getLstTone() == null) tonePallet.setLstTone(new ArrayList<Tone>(256));

        for (int i = 0; i < 256; i++) {
            String toneName = "";
            if (tonePallet.getLstTone().size() < i + 1 || tonePallet.getLstTone().get(i) == null) {
                tonePallet.getLstTone().add(new Tone());
            }

            toneName = tonePallet.getLstTone().get(i).name;

            dgvTonePallet.Rows.add();
            dgvTonePallet.Rows[i].Cells["clmNo"].Value = i;
            dgvTonePallet.Rows[i].Cells["clmName"].Value = toneName;
        }
    }

    private void btChn_Click(ActionEvent ev) {
        List cc = dgvTonePallet.getSelectedValuesList();
        if (cc == null || cc.size() != 1) return;
        JListRow row = cc.get(0).OwningRow;
        if (row == null) return;

        String m = String.format("to Ch.%s", ((Button) ev.getSource()).getActionCommand());
        String n = row.Cells["clmSpacer"].Value == null ? "" : row.Cells["clmSpacer"].Value.toString();
        row.Cells["clmSpacer"].Value = (m.equals(n)) ? "" : m;

        btApply.setEnabled(true);
    }

    private void btApply_Click(ActionEvent ev) {
        updateTone();
        btApply.setEnabled(false);
    }

    private void btOK_Click(ActionEvent ev) {
        updateTone();
        this.setVisible(false);
    }

    private void updateTone() {
        for (int i = 0; i < 256; i++) {
            Object o = dgvTonePallet.Rows[i].Cells["clmSpacer"].Value;
            String n = o == null ? "" : o.toString();
            if (n == "") continue;

            int ch = Integer.parseInt(n.replace("to Ch.", "")) - 1;

            CopyTonePalletToSettingTone(i, ch);

            dgvTonePallet.Rows[i].Cells["clmSpacer"].Value = "";
        }
    }

    private void CopyTonePalletToSettingTone(int ind, int ch) {
        for (int i = 0; i < 4; i++) {
            setting.getMidiKbd().getTones()[ch].ops[i].ar = tonePallet.getLstTone().get(ind).ops[i].ar;//AR
            setting.getMidiKbd().getTones()[ch].ops[i].ks = tonePallet.getLstTone().get(ind).ops[i].ks;//KS
            setting.getMidiKbd().getTones()[ch].ops[i].dr = tonePallet.getLstTone().get(ind).ops[i].dr;//DR
            setting.getMidiKbd().getTones()[ch].ops[i].am = tonePallet.getLstTone().get(ind).ops[i].am;//AM
            setting.getMidiKbd().getTones()[ch].ops[i].sr = tonePallet.getLstTone().get(ind).ops[i].sr;//SR
            setting.getMidiKbd().getTones()[ch].ops[i].rr = tonePallet.getLstTone().get(ind).ops[i].rr;//RR
            setting.getMidiKbd().getTones()[ch].ops[i].sl = tonePallet.getLstTone().get(ind).ops[i].sl;//SL
            setting.getMidiKbd().getTones()[ch].ops[i].tl = tonePallet.getLstTone().get(ind).ops[i].tl;//TL
            setting.getMidiKbd().getTones()[ch].ops[i].ml = tonePallet.getLstTone().get(ind).ops[i].ml;//ML
            setting.getMidiKbd().getTones()[ch].ops[i].dt = tonePallet.getLstTone().get(ind).ops[i].dt;//DT
            setting.getMidiKbd().getTones()[ch].ops[i].dt2 = tonePallet.getLstTone().get(ind).ops[i].dt2;//DT2
        }

        setting.getMidiKbd().getTones()[ch].al = tonePallet.getLstTone().get(ind).al;//AL
        setting.getMidiKbd().getTones()[ch].fb = tonePallet.getLstTone().get(ind).fb;//FB
        setting.getMidiKbd().getTones()[ch].ams = tonePallet.getLstTone().get(ind).ams;//AMS
        setting.getMidiKbd().getTones()[ch].pms = tonePallet.getLstTone().get(ind).pms;//PMS
    }
}
