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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import mdplayer.Setting;
import mdplayer.Tone;
import mdplayer.TonePallet;
import mdplayer.form.Layouts;


public class FormTPGet extends JDialog {

    int DialogResult;

    private void initializeComponent() {
        this.groupBox1 = new JPanel();
        this.btCh6 = new JButton();
        this.btCh3 = new JButton();
        this.btCh5 = new JButton();
        this.btCh2 = new JButton();
        this.btCh4 = new JButton();
        this.btCh1 = new JButton();
        this.dgvTonePallet = new JTable();
        this.clmNo = new JList<>();
        this.clmName = new JList<>();
        this.clmSpacer = new JList<>();
        this.label1 = new JLabel();
        this.btnCancel = new JButton();
        this.btOK = new JButton();
        this.btApply = new JButton();

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
        this.groupBox1.setToolTipText("Ym2612Inst(To)");
        //
        // btCh6
        //
        this.btCh6.setLocation(new Point(178, 48));
        this.btCh6.setName("btCh6");
        this.btCh6.setPreferredSize(new Dimension(80, 24));
        this.btCh6.setActionCommand("6");
        this.btCh6.setText("Ch.6");
        this.btCh6.addActionListener(this::btChn_Click);
        //
        // btCh3
        //
        this.btCh3.setLocation(new Point(178, 18));
        this.btCh3.setName("btCh3");
        this.btCh3.setPreferredSize(new Dimension(80, 24));
        this.btCh3.setActionCommand("3");
        this.btCh3.setText("Ch.3");
        this.btCh3.addActionListener(this::btChn_Click);
        //
        // btCh5
        //
        this.btCh5.setLocation(new Point(92, 48));
        this.btCh5.setName("btCh5");
        this.btCh5.setPreferredSize(new Dimension(80, 24));
        this.btCh5.setActionCommand("5");
        this.btCh5.setText("Ch.5");
        this.btCh5.addActionListener(this::btChn_Click);
        //
        // btCh2
        //
        this.btCh2.setLocation(new Point(92, 18));
        this.btCh2.setName("btCh2");
        this.btCh2.setPreferredSize(new Dimension(80, 24));
        this.btCh2.setActionCommand("2");
        this.btCh2.setText("Ch.2");
        this.btCh2.addActionListener(this::btChn_Click);
        //
        // btCh4
        //
        this.btCh4.setLocation(new Point(6, 48));
        this.btCh4.setName("btCh4");
        this.btCh4.setPreferredSize(new Dimension(80, 24));
        this.btCh4.setActionCommand("4");
        this.btCh4.setText("Ch.4");
        this.btCh4.addActionListener(this::btChn_Click);
        //
        // btCh1
        //
        this.btCh1.setLocation(new Point(6, 18));
        this.btCh1.setName("btCh1");
        this.btCh1.setPreferredSize(new Dimension(80, 24));
        this.btCh1.setActionCommand("1");
        this.btCh1.setText("Ch.1");
        this.btCh1.addActionListener(this::btChn_Click);
        //
        // dgvTonePallet
        //
        this.dgvTonePallet.setLocation(new Point(12, 24));
        this.dgvTonePallet.setName("dgvTonePallet");
        this.dgvTonePallet.setPreferredSize(new Dimension(264, 81));
        //
        // clmNo
        //
        this.clmNo.setName("clmNo");
        //
        // clmName
        //
        this.clmName.setName("clmName");
        //
        // clmSpacer
        //
        this.clmSpacer.setName("clmSpacer");
        //
        // label1
        //
        this.label1.setLocation(new Point(16, 9));
        this.label1.setName("label1");
        this.label1.setPreferredSize(new Dimension(97, 12));
        this.label1.setText("Tone Pallet(From)");
        //
        // btnCancel
        //
        this.btnCancel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.btnCancel.setVerticalAlignment(SwingConstants.BOTTOM);
        this.btnCancel.addActionListener(e -> DialogResult = JOptionPane.NO_OPTION);
        this.btnCancel.setLocation(new Point(120, 208));
        this.btnCancel.setName("btnCancel");
        this.btnCancel.setPreferredSize(new Dimension(75, 23));
        this.btnCancel.setText("Cancel");
        //
        // btOK
        //
        this.btOK.setHorizontalAlignment(SwingConstants.RIGHT);
        this.btOK.setVerticalAlignment(SwingConstants.BOTTOM);
        this.btOK.setLocation(new Point(39, 208));
        this.btOK.setName("btOK");
        this.btOK.setPreferredSize(new Dimension(75, 23));
        this.btOK.setText("OK");
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
        this.btApply.setText("Apply");
        this.btApply.addActionListener(this::btApply_Click);
        //
        // frmTPGet
        //
        this.setPreferredSize(new Dimension(288, 243));
        this.getContentPane().add(this.btApply);
        this.getContentPane().add(this.btOK);
        this.getContentPane().add(this.btnCancel);
        this.getContentPane().add(this.label1);
        this.getContentPane().add(this.dgvTonePallet);
        this.getContentPane().add(this.groupBox1);
        this.setMinimumSize(new Dimension(304, 282));
        this.setName("frmTPGet");
        this.setTitle("Get from Tone Pallet");
        this.addWindowListener(this.frmTPGet_Load);
        Layouts.absolute(this.getContentPane());
        this.setSize(this.getMinimumSize());
    }

    private JPanel groupBox1;
    private JTable dgvTonePallet;
    private JLabel label1;
    private JButton btnCancel;
    private JButton btOK;
    private JList<String> clmNo;
    private JList<String> clmName;
    private JList<String> clmSpacer;
    private JButton btCh6;
    private JButton btCh3;
    private JButton btCh5;
    private JButton btCh2;
    private JButton btCh4;
    private JButton btCh1;
    private JButton btApply;

    private Setting setting = null;
    private TonePallet tonePallet = null;

    public FormTPGet() {
        initializeComponent();
    }

    public int ShowDialog(Setting setting, TonePallet tonePallet) {
        this.setting = setting;
        this.tonePallet = tonePallet;

        this.setVisible(true);
        return DialogResult;
    }

    private final WindowListener frmTPGet_Load = new WindowAdapter() {
        @Override
        public void windowActivated(WindowEvent e) {
            DefaultTableModel m = new DefaultTableModel();
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

    private void btChn_Click(ActionEvent ev) {
        int[] cc = dgvTonePallet.getSelectedRows();
        if (cc == null || cc.length != 1) return;
        int row = cc[0];

        String m = "to Ch.%s".formatted(((Button) ev.getSource()).getActionCommand());
        String n = dgvTonePallet.getValueAt(row, 2) == null ? "" : dgvTonePallet.getValueAt(row, 2).toString();
        dgvTonePallet.setValueAt(m.equals(n) ? "" : m, row, 2);

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
            Object o = dgvTonePallet.getValueAt(i, 2);
            String n = o == null ? "" : o.toString();
            if (n.isEmpty()) continue;

            int ch = Integer.parseInt(n.replace("to Ch.", "")) - 1;

            CopyTonePalletToSettingTone(i, ch);

            dgvTonePallet.setValueAt("", i, 2);
        }
    }

    private void CopyTonePalletToSettingTone(int ind, int ch) {
        for (int i = 0; i < 4; i++) {
            setting.getMidiKbd().getTones()[ch].ops[i].ar = tonePallet.getLstTone().get(ind).ops[i].ar; // AR
            setting.getMidiKbd().getTones()[ch].ops[i].ks = tonePallet.getLstTone().get(ind).ops[i].ks; // KS
            setting.getMidiKbd().getTones()[ch].ops[i].dr = tonePallet.getLstTone().get(ind).ops[i].dr; // DR
            setting.getMidiKbd().getTones()[ch].ops[i].am = tonePallet.getLstTone().get(ind).ops[i].am; // AM
            setting.getMidiKbd().getTones()[ch].ops[i].sr = tonePallet.getLstTone().get(ind).ops[i].sr; // SR
            setting.getMidiKbd().getTones()[ch].ops[i].rr = tonePallet.getLstTone().get(ind).ops[i].rr; // RR
            setting.getMidiKbd().getTones()[ch].ops[i].sl = tonePallet.getLstTone().get(ind).ops[i].sl; // SL
            setting.getMidiKbd().getTones()[ch].ops[i].tl = tonePallet.getLstTone().get(ind).ops[i].tl; // TL
            setting.getMidiKbd().getTones()[ch].ops[i].ml = tonePallet.getLstTone().get(ind).ops[i].ml; // ML
            setting.getMidiKbd().getTones()[ch].ops[i].dt = tonePallet.getLstTone().get(ind).ops[i].dt; // DT
            setting.getMidiKbd().getTones()[ch].ops[i].dt2 = tonePallet.getLstTone().get(ind).ops[i].dt2; // DT2
        }

        setting.getMidiKbd().getTones()[ch].al = tonePallet.getLstTone().get(ind).al; // AL
        setting.getMidiKbd().getTones()[ch].fb = tonePallet.getLstTone().get(ind).fb; // FB
        setting.getMidiKbd().getTones()[ch].ams = tonePallet.getLstTone().get(ind).ams; // AMS
        setting.getMidiKbd().getTones()[ch].pms = tonePallet.getLstTone().get(ind).pms; // PMS
    }
}
