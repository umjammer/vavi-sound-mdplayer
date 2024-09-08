package mdplayer.form.sys;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;


public class ucSettingInstruments extends JPanel {
    public ucSettingInstruments() {
        initializeComponent();
    }

    private void cbSendWait_CheckedChanged(ChangeEvent ev) {
        cbTwice.setEnabled(cbSendWait.isSelected());
    }

    private void initializeComponent() {
        this.cmbYM2151P_SCCI = new JComboBox<>();
        this.rbYM2151P_SCCI = new JCheckBox();
        this.rbYM2151P_Silent = new JCheckBox();
        this.rbYM2151P_Emu = new JCheckBox();
        this.groupBox1 = new JPanel();
        this.rbYM2151P_EmuX68Sound = new JCheckBox();
        this.rbYM2151P_EmuMame = new JCheckBox();
        this.groupBox2 = new JPanel();
        this.cmbYM2151S_SCCI = new JComboBox<>();
        this.rbYM2151S_SCCI = new JCheckBox();
        this.rbYM2151S_Silent = new JCheckBox();
        this.rbYM2151S_EmuX68Sound = new JCheckBox();
        this.rbYM2151S_EmuMame = new JCheckBox();
        this.rbYM2151S_Emu = new JCheckBox();
        this.cmbYM2203P_SCCI = new JComboBox<>();
        this.rbYM2203P_SCCI = new JCheckBox();
        this.rbYM2203P_Emu = new JCheckBox();
        this.groupBox3 = new JPanel();
        this.rbYM2203P_Silent = new JCheckBox();
        this.groupBox4 = new JPanel();
        this.cmbYM2203S_SCCI = new JComboBox<>();
        this.rbYM2203S_SCCI = new JCheckBox();
        this.rbYM2203S_Silent = new JCheckBox();
        this.rbYM2203S_Emu = new JCheckBox();
        this.cmbYM2608P_SCCI = new JComboBox<>();
        this.rbYM2608P_SCCI = new JCheckBox();
        this.rbYM2608P_Emu = new JCheckBox();
        this.groupBox5 = new JPanel();
        this.rbYM2608P_Silent = new JCheckBox();
        this.groupBox6 = new JPanel();
        this.cmbYM2608S_SCCI = new JComboBox<>();
        this.rbYM2608S_SCCI = new JCheckBox();
        this.rbYM2608S_Silent = new JCheckBox();
        this.rbYM2608S_Emu = new JCheckBox();
        this.cmbYM2610BP_SCCI = new JComboBox<>();
        this.rbYM2610BP_SCCI = new JCheckBox();
        this.rbYM2610BP_Emu = new JCheckBox();
        this.groupBox7 = new JPanel();
        this.label1 = new JLabel();
        this.cmbYM2610BEP_SCCI = new JComboBox<>();
        this.cmbSPPCMP_SCCI = new JComboBox<>();
        this.rbYM2610BEP_SCCI = new JCheckBox();
        this.rbYM2610BP_Silent = new JCheckBox();
        this.groupBox8 = new JPanel();
        this.label2 = new JLabel();
        this.cmbYM2610BS_SCCI = new JComboBox<>();
        this.cmbYM2610BES_SCCI = new JComboBox<>();
        this.rbYM2610BS_SCCI = new JCheckBox();
        this.cmbSPPCMS_SCCI = new JComboBox<>();
        this.rbYM2610BS_Silent = new JCheckBox();
        this.rbYM2610BS_Emu = new JCheckBox();
        this.rbYM2610BES_SCCI = new JCheckBox();
        this.cbEmulationPCMOnly = new JCheckBox();
        this.cbTwice = new JCheckBox();
        this.cbSendWait = new JCheckBox();
        this.cmbYM2612P_SCCI = new JComboBox<>();
        this.rbYM2612P_SCCI = new JCheckBox();
        this.rbYM2612P_Emu = new JCheckBox();
        this.groupBox9 = new JPanel();
        this.rbYM2612P_Silent = new JCheckBox();
        this.rbYM2612P_EmuNuked = new JCheckBox();
        this.rbYM2612P_EmuMame = new JCheckBox();
        this.groupBox10 = new JPanel();
        this.rbYM2612S_Silent = new JCheckBox();
        this.cmbYM2612S_SCCI = new JComboBox<>();
        this.rbYM2612S_SCCI = new JCheckBox();
        this.rbYM2612S_EmuNuked = new JCheckBox();
        this.rbYM2612S_EmuMame = new JCheckBox();
        this.rbYM2612S_Emu = new JCheckBox();
        this.cmbSN76489P_SCCI = new JComboBox<>();
        this.rbSN76489P_SCCI = new JCheckBox();
        this.rbSN76489P_Emu = new JCheckBox();
        this.groupBox11 = new JPanel();
        this.rbSN76489P_Silent = new JCheckBox();
        this.rbSN76489P_Emu2 = new JCheckBox();
        this.groupBox12 = new JPanel();
        this.cmbSN76489S_SCCI = new JComboBox<>();
        this.rbSN76489S_SCCI = new JCheckBox();
        this.rbSN76489S_Silent = new JCheckBox();
        this.rbSN76489S_Emu2 = new JCheckBox();
        this.rbSN76489S_Emu = new JCheckBox();
        this.groupBox13 = new JPanel();
        this.groupBox14 = new JPanel();
        this.cmbC140S_SCCI = new JComboBox<>();
        this.rbC140S_SCCI = new JCheckBox();
        this.rbC140S_Silent = new JCheckBox();
        this.rbC140S_Emu = new JCheckBox();
        this.groupBox15 = new JPanel();
        this.cmbC140P_SCCI = new JComboBox<>();
        this.rbC140P_Real = new JCheckBox();
        this.rbC140P_Silent = new JCheckBox();
        this.rbC140P_Emu = new JCheckBox();
        this.groupBox16 = new JPanel();
        this.cmbSEGAPCMS_SCCI = new JComboBox<>();
        this.rbSEGAPCMS_SCCI = new JCheckBox();
        this.rbSEGAPCMS_Silent = new JCheckBox();
        this.rbSEGAPCMS_Emu = new JCheckBox();
        this.groupBox17 = new JPanel();
        this.cmbSEGAPCMP_SCCI = new JComboBox<>();
        this.rbSEGAPCMP_SCCI = new JCheckBox();
        this.rbSEGAPCMP_Silent = new JCheckBox();
        this.rbSEGAPCMP_Emu = new JCheckBox();
        this.groupBox18 = new JPanel();
        this.cmbYMF262P_SCCI = new JComboBox<>();
        this.rbYMF262P_SCCI = new JCheckBox();
        this.rbYMF262P_Silent = new JCheckBox();
        this.rbYMF262P_Emu = new JCheckBox();
        this.groupBox19 = new JPanel();
        this.cmbYMF262S_SCCI = new JComboBox<>();
        this.rbYMF262S_SCCI = new JCheckBox();
        this.rbYMF262S_Silent = new JCheckBox();
        this.rbYMF262S_Emu = new JCheckBox();
        this.groupBox20 = new JPanel();
        this.cmbYM3812P_SCCI = new JComboBox<>();
        this.rbYM3812P_SCCI = new JCheckBox();
        this.rbYM3812P_Silent = new JCheckBox();
        this.rbYM3812P_Emu = new JCheckBox();
        this.groupBox21 = new JPanel();
        this.cmbYM3812S_SCCI = new JComboBox<>();
        this.rbYM3812S_SCCI = new JCheckBox();
        this.rbYM3812S_Silent = new JCheckBox();
        this.rbYM3812S_Emu = new JCheckBox();
        this.groupBox22 = new JPanel();
        this.cmbYM3526S_SCCI = new JComboBox<>();
        this.rbYM3526S_SCCI = new JCheckBox();
        this.rbYM3526S_Silent = new JCheckBox();
        this.rbYM3526S_Emu = new JCheckBox();
        this.groupBox23 = new JPanel();
        this.cmbYM3526P_SCCI = new JComboBox<>();
        this.rbYM3526P_SCCI = new JCheckBox();
        this.rbYM3526P_Silent = new JCheckBox();
        this.rbYM3526P_Emu = new JCheckBox();
        this.groupBox24 = new JPanel();
        this.cmbYM2413S_Real = new JComboBox<>();
        this.rbYM2413S_Real = new JCheckBox();
        this.rbYM2413S_Silent = new JCheckBox();
        this.rbYM2413S_Emu = new JCheckBox();
        this.groupBox25 = new JPanel();
        this.cmbYM2413P_Real = new JComboBox<>();
        this.rbYM2413P_Real = new JCheckBox();
        this.rbYM2413P_Silent = new JCheckBox();
        this.rbYM2413P_Emu = new JCheckBox();
        this.groupBox26 = new JPanel();
        this.cmbAY8910P_Real = new JComboBox<>();
        this.rbAY8910P_Real = new JCheckBox();
        this.rbAY8910P_Silent = new JCheckBox();
        this.rbAY8910P_Emu = new JCheckBox();
        this.groupBox27 = new JPanel();
        this.cmbAY8910S_Real = new JComboBox<>();
        this.rbAY8910S_Real = new JCheckBox();
        this.rbAY8910S_Silent = new JCheckBox();
        this.rbAY8910S_Emu = new JCheckBox();
        this.groupBox28 = new JPanel();
        this.cmbK051649S_Real = new JComboBox<>();
        this.rbK051649S_Real = new JCheckBox();
        this.rbK051649S_Silent = new JCheckBox();
        this.rbK051649S_Emu = new JCheckBox();
        this.groupBox29 = new JPanel();
        this.cmbK051649P_Real = new JComboBox<>();
        this.rbK051649P_Real = new JCheckBox();
        this.rbK051649P_Silent = new JCheckBox();
        this.rbK051649P_Emu = new JCheckBox();
        this.rbAY8910P_Emu2 = new JCheckBox();
        this.rbAY8910S_Emu2 = new JCheckBox();

        //
        // cmbYM2151P_SCCI
        //
//            this.cmbYM2151P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2151P_SCCI.FormattingEnabled = true;
        this.cmbYM2151P_SCCI.setLocation(new Point(220, 34));
        this.cmbYM2151P_SCCI.setName("cmbYM2151P_SCCI");
        this.cmbYM2151P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2151P_SCCI.TabIndex = 2
        //
        // rbYM2151P_SCCI
        //
//            this.rbYM2151P_SCCI.AutoSize(true);
        this.rbYM2151P_SCCI.setLocation(new Point(165, 35));
        this.rbYM2151P_SCCI.setName("rbYM2151P_SCCI");
        this.rbYM2151P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2151P_SCCI.TabIndex = 1
        this.rbYM2151P_SCCI.setText("Real");
        // this.rbYM2151P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2151P_Silent
        //
//            this.rbYM2151P_Silent.AutoSize(true);
        this.rbYM2151P_Silent.setLocation(new Point(4, 13));
        this.rbYM2151P_Silent.setName("rbYM2151P_Silent");
        this.rbYM2151P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2151P_Silent.TabIndex = 0
        this.rbYM2151P_Silent.setText("Silent");
        // this.rbYM2151P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2151P_Emu
        //
//            this.rbYM2151P_Emu.AutoSize(true);
        this.rbYM2151P_Emu.setSelected(true);
        this.rbYM2151P_Emu.setLocation(new Point(62, 13));
        this.rbYM2151P_Emu.setName("rbYM2151P_Emu");
        this.rbYM2151P_Emu.setPreferredSize(new Dimension(116, 16));
        // this.rbYM2151P_Emu.TabIndex = 0
        // this.rbYM2151P_Emu.TabStop = true;
        this.rbYM2151P_Emu.setText("Emulation (fmgen)");
        // this.rbYM2151P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox1
        //
//            this.groupBox1.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox1.add(this.cmbYM2151P_SCCI);
        this.groupBox1.add(this.rbYM2151P_SCCI);
        this.groupBox1.add(this.rbYM2151P_Silent);
        this.groupBox1.add(this.rbYM2151P_EmuX68Sound);
        this.groupBox1.add(this.rbYM2151P_EmuMame);
        this.groupBox1.add(this.rbYM2151P_Emu);
        this.groupBox1.setLocation(new Point(3, 3));
        this.groupBox1.setName("groupBox1");
        this.groupBox1.setPreferredSize(new Dimension(411, 60));
        // this.groupBox1.TabIndex = 0
        // this.groupBox1.TabStop = false;
        this.groupBox1.setToolTipText("YM2151(Primary)");
        //
        // rbYM2151P_EmuX68Sound
        //
//            this.rbYM2151P_EmuX68Sound.AutoSize(true);
        this.rbYM2151P_EmuX68Sound.setLocation(new Point(4, 35));
        this.rbYM2151P_EmuX68Sound.setName("rbYM2151P_EmuX68Sound");
        this.rbYM2151P_EmuX68Sound.setPreferredSize(new Dimension(135, 16));
        // this.rbYM2151P_EmuX68Sound.TabIndex = 0
        this.rbYM2151P_EmuX68Sound.setText("Emulation (X68Sound)");
        // this.rbYM2151P_EmuX68Sound.UseVisualStyl.setBackground(true);
        //
        // rbYM2151P_EmuMame
        //
//            this.rbYM2151P_EmuMame.AutoSize(true);
        this.rbYM2151P_EmuMame.setLocation(new Point(222, 13));
        this.rbYM2151P_EmuMame.setName("rbYM2151P_EmuMame");
        this.rbYM2151P_EmuMame.setPreferredSize(new Dimension(115, 16));
        // this.rbYM2151P_EmuMame.TabIndex = 0
        this.rbYM2151P_EmuMame.setText("Emulation (mame)");
        // this.rbYM2151P_EmuMame.UseVisualStyl.setBackground(true);
        //
        // groupBox2
        //
//            this.groupBox2.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox2.add(this.cmbYM2151S_SCCI);
        this.groupBox2.add(this.rbYM2151S_SCCI);
        this.groupBox2.add(this.rbYM2151S_Silent);
        this.groupBox2.add(this.rbYM2151S_EmuX68Sound);
        this.groupBox2.add(this.rbYM2151S_EmuMame);
        this.groupBox2.add(this.rbYM2151S_Emu);
        this.groupBox2.setLocation(new Point(3, 69));
        this.groupBox2.setName("groupBox2");
        this.groupBox2.setPreferredSize(new Dimension(411, 60));
        // this.groupBox2.TabIndex = 0
        // this.groupBox2.TabStop = false;
        this.groupBox2.setToolTipText("YM2151(Secondary)");
        //
        // cmbYM2151S_SCCI
        //
//            this.cmbYM2151S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2151S_SCCI.FormattingEnabled = true;
        this.cmbYM2151S_SCCI.setLocation(new Point(220, 34));
        this.cmbYM2151S_SCCI.setName("cmbYM2151S_SCCI");
        this.cmbYM2151S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2151S_SCCI.TabIndex = 2
        //
        // rbYM2151S_SCCI
        //
//            this.rbYM2151S_SCCI.AutoSize(true);
        this.rbYM2151S_SCCI.setLocation(new Point(165, 35));
        this.rbYM2151S_SCCI.setName("rbYM2151S_SCCI");
        this.rbYM2151S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2151S_SCCI.TabIndex = 1
        this.rbYM2151S_SCCI.setText("Real");
        // this.rbYM2151S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2151S_Silent
        //
//            this.rbYM2151S_Silent.AutoSize(true);
        this.rbYM2151S_Silent.setLocation(new Point(4, 13));
        this.rbYM2151S_Silent.setName("rbYM2151S_Silent");
        this.rbYM2151S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2151S_Silent.TabIndex = 0
        this.rbYM2151S_Silent.setText("Silent");
        // this.rbYM2151S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2151S_EmuX68Sound
        //
//            this.rbYM2151S_EmuX68Sound.AutoSize(true);
        this.rbYM2151S_EmuX68Sound.setLocation(new Point(4, 35));
        this.rbYM2151S_EmuX68Sound.setName("rbYM2151S_EmuX68Sound");
        this.rbYM2151S_EmuX68Sound.setPreferredSize(new Dimension(135, 16));
        // this.rbYM2151S_EmuX68Sound.TabIndex = 0
        this.rbYM2151S_EmuX68Sound.setText("Emulation (X68Sound)");
        // this.rbYM2151S_EmuX68Sound.UseVisualStyl.setBackground(true);
        //
        // rbYM2151S_EmuMame
        //
//            this.rbYM2151S_EmuMame.AutoSize(true);
        this.rbYM2151S_EmuMame.setLocation(new Point(222, 13));
        this.rbYM2151S_EmuMame.setName("rbYM2151S_EmuMame");
        this.rbYM2151S_EmuMame.setPreferredSize(new Dimension(115, 16));
        // this.rbYM2151S_EmuMame.TabIndex = 0
        this.rbYM2151S_EmuMame.setText("Emulation (mame)");
        // this.rbYM2151S_EmuMame.UseVisualStyl.setBackground(true);
        //
        // rbYM2151S_Emu
        //
//            this.rbYM2151S_Emu.AutoSize(true);
        this.rbYM2151S_Emu.setSelected(true);
        this.rbYM2151S_Emu.setLocation(new Point(62, 13));
        this.rbYM2151S_Emu.setName("rbYM2151S_Emu");
        this.rbYM2151S_Emu.setPreferredSize(new Dimension(116, 16));
        // this.rbYM2151S_Emu.TabIndex = 0
        // this.rbYM2151S_Emu.TabStop = true;
        this.rbYM2151S_Emu.setText("Emulation (fmgen)");
        // this.rbYM2151S_Emu.UseVisualStyl.setBackground(true);
        //
        // cmbYM2203P_SCCI
        //
//            this.cmbYM2203P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2203P_SCCI.FormattingEnabled = true;
        this.cmbYM2203P_SCCI.setLocation(new Point(220, 12));
        this.cmbYM2203P_SCCI.setName("cmbYM2203P_SCCI");
        this.cmbYM2203P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2203P_SCCI.TabIndex = 2
        //
        // rbYM2203P_SCCI
        //
//            this.rbYM2203P_SCCI.AutoSize(true);
        this.rbYM2203P_SCCI.setLocation(new Point(165, 13));
        this.rbYM2203P_SCCI.setName("rbYM2203P_SCCI");
        this.rbYM2203P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2203P_SCCI.TabIndex = 1
        this.rbYM2203P_SCCI.setText("Real");
        // this.rbYM2203P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2203P_Emu
        //
//            this.rbYM2203P_Emu.AutoSize(true);
        this.rbYM2203P_Emu.setSelected(true);
        this.rbYM2203P_Emu.setLocation(new Point(62, 13));
        this.rbYM2203P_Emu.setName("rbYM2203P_Emu");
        this.rbYM2203P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2203P_Emu.TabIndex = 0
        // this.rbYM2203P_Emu.TabStop = true;
        this.rbYM2203P_Emu.setText("Emulation");
        // this.rbYM2203P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox3
        //
//            this.groupBox3.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox3.add(this.cmbYM2203P_SCCI);
        this.groupBox3.add(this.rbYM2203P_SCCI);
        this.groupBox3.add(this.rbYM2203P_Silent);
        this.groupBox3.add(this.rbYM2203P_Emu);
        this.groupBox3.setLocation(new Point(3, 135));
        this.groupBox3.setName("groupBox3");
        this.groupBox3.setPreferredSize(new Dimension(411, 38));
        // this.groupBox3.TabIndex = 1
        // this.groupBox3.TabStop = false;
        this.groupBox3.setToolTipText("YM2203(Primary)");
        //
        // rbYM2203P_Silent
        //
//            this.rbYM2203P_Silent.AutoSize(true);
        this.rbYM2203P_Silent.setLocation(new Point(4, 13));
        this.rbYM2203P_Silent.setName("rbYM2203P_Silent");
        this.rbYM2203P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2203P_Silent.TabIndex = 0
        this.rbYM2203P_Silent.setText("Silent");
        // this.rbYM2203P_Silent.UseVisualStyl.setBackground(true);
        //
        // groupBox4
        //
//            this.groupBox4.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox4.add(this.cmbYM2203S_SCCI);
        this.groupBox4.add(this.rbYM2203S_SCCI);
        this.groupBox4.add(this.rbYM2203S_Silent);
        this.groupBox4.add(this.rbYM2203S_Emu);
        this.groupBox4.setLocation(new Point(3, 179));
        this.groupBox4.setName("groupBox4");
        this.groupBox4.setPreferredSize(new Dimension(411, 38));
        // this.groupBox4.TabIndex = 1
        // this.groupBox4.TabStop = false;
        this.groupBox4.setToolTipText("YM2203(Secondary)");
        //
        // cmbYM2203S_SCCI
        //
//            this.cmbYM2203S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2203S_SCCI.FormattingEnabled = true;
        this.cmbYM2203S_SCCI.setLocation(new Point(220, 12));
        this.cmbYM2203S_SCCI.setName("cmbYM2203S_SCCI");
        this.cmbYM2203S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2203S_SCCI.TabIndex = 2
        //
        // rbYM2203S_SCCI
        //
//            this.rbYM2203S_SCCI.AutoSize(true);
        this.rbYM2203S_SCCI.setLocation(new Point(165, 13));
        this.rbYM2203S_SCCI.setName("rbYM2203S_SCCI");
        this.rbYM2203S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2203S_SCCI.TabIndex = 1
        this.rbYM2203S_SCCI.setText("Real");
        // this.rbYM2203S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2203S_Silent
        //
//            this.rbYM2203S_Silent.AutoSize(true);
        this.rbYM2203S_Silent.setLocation(new Point(4, 13));
        this.rbYM2203S_Silent.setName("rbYM2203S_Silent");
        this.rbYM2203S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2203S_Silent.TabIndex = 0
        this.rbYM2203S_Silent.setText("Silent");
        // this.rbYM2203S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2203S_Emu
        //
//            this.rbYM2203S_Emu.AutoSize(true);
        this.rbYM2203S_Emu.setSelected(true);
        this.rbYM2203S_Emu.setLocation(new Point(62, 13));
        this.rbYM2203S_Emu.setName("rbYM2203S_Emu");
        this.rbYM2203S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2203S_Emu.TabIndex = 0
        // this.rbYM2203S_Emu.TabStop = true;
        this.rbYM2203S_Emu.setText("Emulation");
        // this.rbYM2203S_Emu.UseVisualStyl.setBackground(true);
        //
        // cmbYM2608P_SCCI
        //
//            this.cmbYM2608P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2608P_SCCI.FormattingEnabled = true;
        this.cmbYM2608P_SCCI.setLocation(new Point(220, 12));
        this.cmbYM2608P_SCCI.setName("cmbYM2608P_SCCI");
        this.cmbYM2608P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2608P_SCCI.TabIndex = 2
        //
        // rbYM2608P_SCCI
        //
//            this.rbYM2608P_SCCI.AutoSize(true);
        this.rbYM2608P_SCCI.setLocation(new Point(165, 13));
        this.rbYM2608P_SCCI.setName("rbYM2608P_SCCI");
        this.rbYM2608P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2608P_SCCI.TabIndex = 1
        this.rbYM2608P_SCCI.setText("Real");
        // this.rbYM2608P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2608P_Emu
        //
//            this.rbYM2608P_Emu.AutoSize(true);
        this.rbYM2608P_Emu.setSelected(true);
        this.rbYM2608P_Emu.setLocation(new Point(62, 13));
        this.rbYM2608P_Emu.setName("rbYM2608P_Emu");
        this.rbYM2608P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2608P_Emu.TabIndex = 0
        // this.rbYM2608P_Emu.TabStop = true;
        this.rbYM2608P_Emu.setText("Emulation");
        // this.rbYM2608P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox5
        //
//            this.groupBox5.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox5.add(this.cmbYM2608P_SCCI);
        this.groupBox5.add(this.rbYM2608P_SCCI);
        this.groupBox5.add(this.rbYM2608P_Silent);
        this.groupBox5.add(this.rbYM2608P_Emu);
        this.groupBox5.setLocation(new Point(3, 311));
        this.groupBox5.setName("groupBox5");
        this.groupBox5.setPreferredSize(new Dimension(411, 38));
        // this.groupBox5.TabIndex = 2
        // this.groupBox5.TabStop = false;
        this.groupBox5.setToolTipText("YM2608(Primary)");
        //
        // rbYM2608P_Silent
        //
//            this.rbYM2608P_Silent.AutoSize(true);
        this.rbYM2608P_Silent.setLocation(new Point(4, 13));
        this.rbYM2608P_Silent.setName("rbYM2608P_Silent");
        this.rbYM2608P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2608P_Silent.TabIndex = 0
        this.rbYM2608P_Silent.setText("Silent");
        // this.rbYM2608P_Silent.UseVisualStyl.setBackground(true);
        //
        // groupBox6
        //
//            this.groupBox6.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox6.add(this.cmbYM2608S_SCCI);
        this.groupBox6.add(this.rbYM2608S_SCCI);
        this.groupBox6.add(this.rbYM2608S_Silent);
        this.groupBox6.add(this.rbYM2608S_Emu);
        this.groupBox6.setLocation(new Point(3, 355));
        this.groupBox6.setName("groupBox6");
        this.groupBox6.setPreferredSize(new Dimension(411, 38));
        // this.groupBox6.TabIndex = 2
        // this.groupBox6.TabStop = false;
        this.groupBox6.setToolTipText("YM2608(Secondary)");
        //
        // cmbYM2608S_SCCI
        //
//            this.cmbYM2608S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2608S_SCCI.FormattingEnabled = true;
        this.cmbYM2608S_SCCI.setLocation(new Point(220, 12));
        this.cmbYM2608S_SCCI.setName("cmbYM2608S_SCCI");
        this.cmbYM2608S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2608S_SCCI.TabIndex = 2
        //
        // rbYM2608S_SCCI
        //
//            this.rbYM2608S_SCCI.AutoSize(true);
        this.rbYM2608S_SCCI.setLocation(new Point(165, 13));
        this.rbYM2608S_SCCI.setName("rbYM2608S_SCCI");
        this.rbYM2608S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2608S_SCCI.TabIndex = 1
        this.rbYM2608S_SCCI.setText("Real");
        // this.rbYM2608S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2608S_Silent
        //
//            this.rbYM2608S_Silent.AutoSize(true);
        this.rbYM2608S_Silent.setLocation(new Point(4, 13));
        this.rbYM2608S_Silent.setName("rbYM2608S_Silent");
        this.rbYM2608S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2608S_Silent.TabIndex = 0
        this.rbYM2608S_Silent.setText("Silent");
        // this.rbYM2608S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2608S_Emu
        //
//            this.rbYM2608S_Emu.AutoSize(true);
        this.rbYM2608S_Emu.setSelected(true);
        this.rbYM2608S_Emu.setLocation(new Point(62, 13));
        this.rbYM2608S_Emu.setName("rbYM2608S_Emu");
        this.rbYM2608S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2608S_Emu.TabIndex = 0
        // this.rbYM2608S_Emu.TabStop = true;
        this.rbYM2608S_Emu.setText("Emulation");
        // this.rbYM2608S_Emu.UseVisualStyl.setBackground(true);
        //
        // cmbYM2610BP_SCCI
        //
//            this.cmbYM2610BP_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2610BP_SCCI.FormattingEnabled = true;
        this.cmbYM2610BP_SCCI.setLocation(new Point(220, 12));
        this.cmbYM2610BP_SCCI.setName("cmbYM2610BP_SCCI");
        this.cmbYM2610BP_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2610BP_SCCI.TabIndex = 2
        //
        // rbYM2610BP_SCCI
        //
//            this.rbYM2610BP_SCCI.AutoSize(true);
        this.rbYM2610BP_SCCI.setLocation(new Point(165, 13));
        this.rbYM2610BP_SCCI.setName("rbYM2610BP_SCCI");
        this.rbYM2610BP_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2610BP_SCCI.TabIndex = 1
        this.rbYM2610BP_SCCI.setText("Real");
        // this.rbYM2610BP_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2610BP_Emu
        //
//            this.rbYM2610BP_Emu.AutoSize(true);
        this.rbYM2610BP_Emu.setSelected(true);
        this.rbYM2610BP_Emu.setLocation(new Point(62, 13));
        this.rbYM2610BP_Emu.setName("rbYM2610BP_Emu");
        this.rbYM2610BP_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2610BP_Emu.TabIndex = 0
        // this.rbYM2610BP_Emu.TabStop = true;
        this.rbYM2610BP_Emu.setText("Emulation");
        // this.rbYM2610BP_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox7
        //
//            this.groupBox7.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox7.add(this.label1);
        this.groupBox7.add(this.cmbYM2610BEP_SCCI);
        this.groupBox7.add(this.cmbSPPCMP_SCCI);
        this.groupBox7.add(this.cmbYM2610BP_SCCI);
        this.groupBox7.add(this.rbYM2610BEP_SCCI);
        this.groupBox7.add(this.rbYM2610BP_SCCI);
        this.groupBox7.add(this.rbYM2610BP_Silent);
        this.groupBox7.add(this.rbYM2610BP_Emu);
        this.groupBox7.setLocation(new Point(3, 399));
        this.groupBox7.setName("groupBox7");
        this.groupBox7.setPreferredSize(new Dimension(411, 64));
        // this.groupBox7.TabIndex = 3
        // this.groupBox7.TabStop = false;
        this.groupBox7.setToolTipText("YM2610/B(Primary)");
        //
        // label1
        //
//            this.label1.AutoSize(true);
        this.label1.setLocation(new Point(230, 41));
        this.label1.setName("label1");
        this.label1.setPreferredSize(new Dimension(11, 12));
        // this.label1.TabIndex = 3
        this.label1.setText("+");
        //
        // cmbYM2610BEP_SCCI
        //
//            this.cmbYM2610BEP_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2610BEP_SCCI.FormattingEnabled = true;
        this.cmbYM2610BEP_SCCI.setLocation(new Point(69, 38));
        this.cmbYM2610BEP_SCCI.setName("cmbYM2610BEP_SCCI");
        this.cmbYM2610BEP_SCCI.setPreferredSize(new Dimension(155, 20));
        // this.cmbYM2610BEP_SCCI.TabIndex = 2
        //
        // cmbSPPCMP_SCCI
        //
//            this.cmbSPPCMP_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbSPPCMP_SCCI.FormattingEnabled = true;
        this.cmbSPPCMP_SCCI.setLocation(new Point(249, 38));
        this.cmbSPPCMP_SCCI.setName("cmbSPPCMP_SCCI");
        this.cmbSPPCMP_SCCI.setPreferredSize(new Dimension(156, 20));
        // this.cmbSPPCMP_SCCI.TabIndex = 2
        //
        // rbYM2610BEP_SCCI
        //
//            this.rbYM2610BEP_SCCI.AutoSize(true);
        this.rbYM2610BEP_SCCI.setLocation(new Point(4, 39));
        this.rbYM2610BEP_SCCI.setName("rbYM2610BEP_SCCI");
        this.rbYM2610BEP_SCCI.setPreferredSize(new Dimension(56, 16));
        // this.rbYM2610BEP_SCCI.TabIndex = 1
        this.rbYM2610BEP_SCCI.setText("Real_2");
        // this.rbYM2610BEP_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2610BP_Silent
        //
//            this.rbYM2610BP_Silent.AutoSize(true);
        this.rbYM2610BP_Silent.setLocation(new Point(4, 13));
        this.rbYM2610BP_Silent.setName("rbYM2610BP_Silent");
        this.rbYM2610BP_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2610BP_Silent.TabIndex = 0
        this.rbYM2610BP_Silent.setText("Silent");
        // this.rbYM2610BP_Silent.UseVisualStyl.setBackground(true);
        //
        // groupBox8
        //
//            this.groupBox8.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox8.add(this.label2);
        this.groupBox8.add(this.cmbYM2610BS_SCCI);
        this.groupBox8.add(this.cmbYM2610BES_SCCI);
        this.groupBox8.add(this.rbYM2610BS_SCCI);
        this.groupBox8.add(this.cmbSPPCMS_SCCI);
        this.groupBox8.add(this.rbYM2610BS_Silent);
        this.groupBox8.add(this.rbYM2610BS_Emu);
        this.groupBox8.add(this.rbYM2610BES_SCCI);
        this.groupBox8.setLocation(new Point(3, 469));
        this.groupBox8.setName("groupBox8");
        this.groupBox8.setPreferredSize(new Dimension(411, 64));
        // this.groupBox8.TabIndex = 3
        // this.groupBox8.TabStop = false;
        this.groupBox8.setToolTipText("YM2610/B(Secondary)");
        //
        // label2
        //
//            this.label2.AutoSize(true);
        this.label2.setLocation(new Point(230, 41));
        this.label2.setName("label2");
        this.label2.setPreferredSize(new Dimension(11, 12));
        // this.label2.TabIndex = 3
        this.label2.setText("+");
        //
        // cmbYM2610BS_SCCI
        //
//            this.cmbYM2610BS_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2610BS_SCCI.FormattingEnabled = true;
        this.cmbYM2610BS_SCCI.setLocation(new Point(220, 12));
        this.cmbYM2610BS_SCCI.setName("cmbYM2610BS_SCCI");
        this.cmbYM2610BS_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2610BS_SCCI.TabIndex = 2
        //
        // cmbYM2610BES_SCCI
        //
//            this.cmbYM2610BES_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2610BES_SCCI.FormattingEnabled = true;
        this.cmbYM2610BES_SCCI.setLocation(new Point(68, 38));
        this.cmbYM2610BES_SCCI.setName("cmbYM2610BES_SCCI");
        this.cmbYM2610BES_SCCI.setPreferredSize(new Dimension(156, 20));
        // this.cmbYM2610BES_SCCI.TabIndex = 2
        //
        // rbYM2610BS_SCCI
        //
//            this.rbYM2610BS_SCCI.AutoSize(true);
        this.rbYM2610BS_SCCI.setLocation(new Point(165, 13));
        this.rbYM2610BS_SCCI.setName("rbYM2610BS_SCCI");
        this.rbYM2610BS_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2610BS_SCCI.TabIndex = 1
        this.rbYM2610BS_SCCI.setText("Real");
        // this.rbYM2610BS_SCCI.UseVisualStyl.setBackground(true);
        //
        // cmbSPPCMS_SCCI
        //
//            this.cmbSPPCMS_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbSPPCMS_SCCI.FormattingEnabled = true;
        this.cmbSPPCMS_SCCI.setLocation(new Point(249, 38));
        this.cmbSPPCMS_SCCI.setName("cmbSPPCMS_SCCI");
        this.cmbSPPCMS_SCCI.setPreferredSize(new Dimension(156, 20));
        // this.cmbSPPCMS_SCCI.TabIndex = 2
        //
        // rbYM2610BS_Silent
        //
//            this.rbYM2610BS_Silent.AutoSize(true);
        this.rbYM2610BS_Silent.setLocation(new Point(4, 13));
        this.rbYM2610BS_Silent.setName("rbYM2610BS_Silent");
        this.rbYM2610BS_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2610BS_Silent.TabIndex = 0
        this.rbYM2610BS_Silent.setText("Silent");
        // this.rbYM2610BS_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2610BS_Emu
        //
//            this.rbYM2610BS_Emu.AutoSize(true);
        this.rbYM2610BS_Emu.setSelected(true);
        this.rbYM2610BS_Emu.setLocation(new Point(62, 13));
        this.rbYM2610BS_Emu.setName("rbYM2610BS_Emu");
        this.rbYM2610BS_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2610BS_Emu.TabIndex = 0
        // this.rbYM2610BS_Emu.TabStop = true;
        this.rbYM2610BS_Emu.setText("Emulation");
        // this.rbYM2610BS_Emu.UseVisualStyl.setBackground(true);
        //
        // rbYM2610BES_SCCI
        //
//            this.rbYM2610BES_SCCI.AutoSize(true);
        this.rbYM2610BES_SCCI.setLocation(new Point(4, 39));
        this.rbYM2610BES_SCCI.setName("rbYM2610BES_SCCI");
        this.rbYM2610BES_SCCI.setPreferredSize(new Dimension(56, 16));
        // this.rbYM2610BES_SCCI.TabIndex = 1
        this.rbYM2610BES_SCCI.setText("Real_2");
        // this.rbYM2610BES_SCCI.UseVisualStyl.setBackground(true);
        //
        // cbEmulationPCMOnly
        //
//            this.cbEmulationPCMOnly.AutoSize(true);
        this.cbEmulationPCMOnly.setLocation(new Point(159, 18));
        this.cbEmulationPCMOnly.setName("cbEmulationPCMOnly");
        this.cbEmulationPCMOnly.setPreferredSize(new Dimension(118, 16));
        // this.cbEmulationPCMOnly.TabIndex = 5
        this.cbEmulationPCMOnly.setText("Emulate PCM only");
        // this.cbEmulationPCMOnly.UseVisualStyl.setBackground(true);
        //
        // cbTwice
        //
//            this.cbTwice.AutoSize(true);
        this.cbTwice.setLocation(new Point(22, 39));
        this.cbTwice.setName("cbTwice");
        this.cbTwice.setPreferredSize(new Dimension(84, 16));
        // this.cbTwice.TabIndex = 4
        this.cbTwice.setText("Double wait");
        // this.cbTwice.UseVisualStyl.setBackground(true);
        //
        // cbSendWait
        //
//            this.cbSendWait.AutoSize(true);
        this.cbSendWait.setLocation(new Point(6, 18));
        this.cbSendWait.setName("cbSendWait");
        this.cbSendWait.setPreferredSize(new Dimension(110, 16));
        // this.cbSendWait.TabIndex = 3
        this.cbSendWait.setText("Send Wait Signal");
        // this.cbSendWait.UseVisualStyl.setBackground(true);
        this.cbSendWait.addChangeListener(this::cbSendWait_CheckedChanged);
        //
        // cmbYM2612P_SCCI
        //
//            this.cmbYM2612P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2612P_SCCI.FormattingEnabled = true;
        this.cmbYM2612P_SCCI.setLocation(new Point(220, 36));
        this.cmbYM2612P_SCCI.setName("cmbYM2612P_SCCI");
        this.cmbYM2612P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2612P_SCCI.TabIndex = 2
        //
        // rbYM2612P_SCCI
        //
//            this.rbYM2612P_SCCI.AutoSize(true);
        this.rbYM2612P_SCCI.setLocation(new Point(165, 37));
        this.rbYM2612P_SCCI.setName("rbYM2612P_SCCI");
        this.rbYM2612P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2612P_SCCI.TabIndex = 1
        this.rbYM2612P_SCCI.setText("Real");
        // this.rbYM2612P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2612P_Emu
        //
//            this.rbYM2612P_Emu.AutoSize(true);
        this.rbYM2612P_Emu.setSelected(true);
        this.rbYM2612P_Emu.setLocation(new Point(234, 15));
        this.rbYM2612P_Emu.setName("rbYM2612P_Emu");
        this.rbYM2612P_Emu.setPreferredSize(new Dimension(111, 16));
        // this.rbYM2612P_Emu.TabIndex = 0
        // this.rbYM2612P_Emu.TabStop = true;
        this.rbYM2612P_Emu.setText("Emulation (Gens)");
        // this.rbYM2612P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox9
        //
//            this.groupBox9.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox9.add(this.rbYM2612P_Silent);
        this.groupBox9.add(this.cmbYM2612P_SCCI);
        this.groupBox9.add(this.rbYM2612P_SCCI);
        this.groupBox9.add(this.rbYM2612P_EmuNuked);
        this.groupBox9.add(this.rbYM2612P_EmuMame);
        this.groupBox9.add(this.rbYM2612P_Emu);
        this.groupBox9.setLocation(new Point(3, 539));
        this.groupBox9.setName("groupBox9");
        this.groupBox9.setPreferredSize(new Dimension(411, 60));
        // this.groupBox9.TabIndex = 4
        // this.groupBox9.TabStop = false;
        this.groupBox9.setToolTipText("Ym2612Inst(Primary)");
        //
        // rbYM2612P_Silent
        //
//            this.rbYM2612P_Silent.AutoSize(true);
        this.rbYM2612P_Silent.setLocation(new Point(4, 15));
        this.rbYM2612P_Silent.setName("rbYM2612P_Silent");
        this.rbYM2612P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2612P_Silent.TabIndex = 0
        this.rbYM2612P_Silent.setText("Silent");
        // this.rbYM2612P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2612P_EmuNuked
        //
//            this.rbYM2612P_EmuNuked.AutoSize(true);
        this.rbYM2612P_EmuNuked.setLocation(new Point(62, 15));
        this.rbYM2612P_EmuNuked.setName("rbYM2612P_EmuNuked");
        this.rbYM2612P_EmuNuked.setPreferredSize(new Dimension(152, 16));
        // this.rbYM2612P_EmuNuked.TabIndex = 0
        this.rbYM2612P_EmuNuked.setText("Emulation (Nuked-OPN2)");
        // this.rbYM2612P_EmuNuked.UseVisualStyl.setBackground(true);
        //
        // rbYM2612P_EmuMame
        //
//            this.rbYM2612P_EmuMame.AutoSize(true);
        this.rbYM2612P_EmuMame.setLocation(new Point(4, 37));
        this.rbYM2612P_EmuMame.setName("rbYM2612P_EmuMame");
        this.rbYM2612P_EmuMame.setPreferredSize(new Dimension(115, 16));
        // this.rbYM2612P_EmuMame.TabIndex = 0
        this.rbYM2612P_EmuMame.setText("Emulation (mame)");
        // this.rbYM2612P_EmuMame.UseVisualStyl.setBackground(true);
        //
        // groupBox10
        //
//            this.groupBox10.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox10.add(this.rbYM2612S_Silent);
        this.groupBox10.add(this.cmbYM2612S_SCCI);
        this.groupBox10.add(this.rbYM2612S_SCCI);
        this.groupBox10.add(this.rbYM2612S_EmuNuked);
        this.groupBox10.add(this.rbYM2612S_EmuMame);
        this.groupBox10.add(this.rbYM2612S_Emu);
        this.groupBox10.setLocation(new Point(3, 605));
        this.groupBox10.setName("groupBox10");
        this.groupBox10.setPreferredSize(new Dimension(411, 60));
        // this.groupBox10.TabIndex = 4
        // this.groupBox10.TabStop = false;
        this.groupBox10.setToolTipText("Ym2612Inst(Secondary)");
        //
        // rbYM2612S_Silent
        //
//            this.rbYM2612S_Silent.AutoSize(true);
        this.rbYM2612S_Silent.setLocation(new Point(4, 15));
        this.rbYM2612S_Silent.setName("rbYM2612S_Silent");
        this.rbYM2612S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2612S_Silent.TabIndex = 0
        this.rbYM2612S_Silent.setText("Silent");
        // this.rbYM2612S_Silent.UseVisualStyl.setBackground(true);
        //
        // cmbYM2612S_SCCI
        //
//            this.cmbYM2612S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2612S_SCCI.FormattingEnabled = true;
        this.cmbYM2612S_SCCI.setLocation(new Point(220, 36));
        this.cmbYM2612S_SCCI.setName("cmbYM2612S_SCCI");
        this.cmbYM2612S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2612S_SCCI.TabIndex = 2
        //
        // rbYM2612S_SCCI
        //
//            this.rbYM2612S_SCCI.AutoSize(true);
        this.rbYM2612S_SCCI.setLocation(new Point(165, 37));
        this.rbYM2612S_SCCI.setName("rbYM2612S_SCCI");
        this.rbYM2612S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2612S_SCCI.TabIndex = 1
        this.rbYM2612S_SCCI.setText("Real");
        // this.rbYM2612S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM2612S_EmuNuked
        //
//            this.rbYM2612S_EmuNuked.AutoSize(true);
        this.rbYM2612S_EmuNuked.setLocation(new Point(62, 15));
        this.rbYM2612S_EmuNuked.setName("rbYM2612S_EmuNuked");
        this.rbYM2612S_EmuNuked.setPreferredSize(new Dimension(152, 16));
        // this.rbYM2612S_EmuNuked.TabIndex = 0
        this.rbYM2612S_EmuNuked.setText("Emulation (Nuked-OPN2)");
        // this.rbYM2612S_EmuNuked.UseVisualStyl.setBackground(true);
        //
        // rbYM2612S_EmuMame
        //
//            this.rbYM2612S_EmuMame.AutoSize(true);
        this.rbYM2612S_EmuMame.setLocation(new Point(4, 37));
        this.rbYM2612S_EmuMame.setName("rbYM2612S_EmuMame");
        this.rbYM2612S_EmuMame.setPreferredSize(new Dimension(115, 16));
        // this.rbYM2612S_EmuMame.TabIndex = 0
        this.rbYM2612S_EmuMame.setText("Emulation (mame)");
        // this.rbYM2612S_EmuMame.UseVisualStyl.setBackground(true);
        //
        // rbYM2612S_Emu
        //
//            this.rbYM2612S_Emu.AutoSize(true);
        this.rbYM2612S_Emu.setSelected(true);
        this.rbYM2612S_Emu.setLocation(new Point(234, 15));
        this.rbYM2612S_Emu.setName("rbYM2612S_Emu");
        this.rbYM2612S_Emu.setPreferredSize(new Dimension(111, 16));
        // this.rbYM2612S_Emu.TabIndex = 0
        // this.rbYM2612S_Emu.TabStop = true;
        this.rbYM2612S_Emu.setText("Emulation (Gens)");
        // this.rbYM2612S_Emu.UseVisualStyl.setBackground(true);
        //
        // cmbSN76489P_SCCI
        //
//            this.cmbSN76489P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbSN76489P_SCCI.FormattingEnabled = true;
        this.cmbSN76489P_SCCI.setLocation(new Point(220, 38));
        this.cmbSN76489P_SCCI.setName("cmbSN76489P_SCCI");
        this.cmbSN76489P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbSN76489P_SCCI.TabIndex = 2
        //
        // rbSN76489P_SCCI
        //
//            this.rbSN76489P_SCCI.AutoSize(true);
        this.rbSN76489P_SCCI.setLocation(new Point(165, 39));
        this.rbSN76489P_SCCI.setName("rbSN76489P_SCCI");
        this.rbSN76489P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbSN76489P_SCCI.TabIndex = 1
        this.rbSN76489P_SCCI.setText("Real");
        // this.rbSN76489P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbSN76489P_Emu
        //
//            this.rbSN76489P_Emu.AutoSize(true);
        this.rbSN76489P_Emu.setSelected(true);
        this.rbSN76489P_Emu.setLocation(new Point(62, 15));
        this.rbSN76489P_Emu.setName("rbSN76489P_Emu");
        this.rbSN76489P_Emu.setPreferredSize(new Dimension(118, 16));
        // this.rbSN76489P_Emu.TabIndex = 0
        // this.rbSN76489P_Emu.TabStop = true;
        this.rbSN76489P_Emu.setText("Emulation (maxim)");
        // this.rbSN76489P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox11
        //
//            this.groupBox11.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox11.add(this.cmbSN76489P_SCCI);
        this.groupBox11.add(this.rbSN76489P_SCCI);
        this.groupBox11.add(this.rbSN76489P_Silent);
        this.groupBox11.add(this.rbSN76489P_Emu2);
        this.groupBox11.add(this.rbSN76489P_Emu);
        this.groupBox11.setLocation(new Point(3, 1000));
        this.groupBox11.setName("groupBox11");
        this.groupBox11.setPreferredSize(new Dimension(411, 64));
        // this.groupBox11.TabIndex = 5
        // this.groupBox11.TabStop = false;
        this.groupBox11.setToolTipText("SN76489(Primary)");
        //
        // rbSN76489P_Silent
        //
//            this.rbSN76489P_Silent.AutoSize(true);
        this.rbSN76489P_Silent.setLocation(new Point(4, 15));
        this.rbSN76489P_Silent.setName("rbSN76489P_Silent");
        this.rbSN76489P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbSN76489P_Silent.TabIndex = 0
        this.rbSN76489P_Silent.setText("Silent");
        // this.rbSN76489P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbSN76489P_Emu2
        //
//            this.rbSN76489P_Emu2.AutoSize(true);
        this.rbSN76489P_Emu2.setLocation(new Point(200, 15));
        this.rbSN76489P_Emu2.setName("rbSN76489P_Emu2");
        this.rbSN76489P_Emu2.setPreferredSize(new Dimension(115, 16));
        // this.rbSN76489P_Emu2.TabIndex = 0
        this.rbSN76489P_Emu2.setText("Emulation (mame)");
        // this.rbSN76489P_Emu2.UseVisualStyl.setBackground(true);
        //
        // groupBox12
        //
//            this.groupBox12.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox12.add(this.cmbSN76489S_SCCI);
        this.groupBox12.add(this.rbSN76489S_SCCI);
        this.groupBox12.add(this.rbSN76489S_Silent);
        this.groupBox12.add(this.rbSN76489S_Emu2);
        this.groupBox12.add(this.rbSN76489S_Emu);
        this.groupBox12.setLocation(new Point(3, 1070));
        this.groupBox12.setName("groupBox12");
        this.groupBox12.setPreferredSize(new Dimension(411, 64));
        // this.groupBox12.TabIndex = 5
        // this.groupBox12.TabStop = false;
        this.groupBox12.setToolTipText("SN76489(Secondary)");
        //
        // cmbSN76489S_SCCI
        //
//            this.cmbSN76489S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbSN76489S_SCCI.FormattingEnabled = true;
        this.cmbSN76489S_SCCI.setLocation(new Point(220, 38));
        this.cmbSN76489S_SCCI.setName("cmbSN76489S_SCCI");
        this.cmbSN76489S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbSN76489S_SCCI.TabIndex = 2
        //
        // rbSN76489S_SCCI
        //
//            this.rbSN76489S_SCCI.AutoSize(true);
        this.rbSN76489S_SCCI.setLocation(new Point(165, 39));
        this.rbSN76489S_SCCI.setName("rbSN76489S_SCCI");
        this.rbSN76489S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbSN76489S_SCCI.TabIndex = 1
        this.rbSN76489S_SCCI.setText("Real");
        // this.rbSN76489S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbSN76489S_Silent
        //
//            this.rbSN76489S_Silent.AutoSize(true);
        this.rbSN76489S_Silent.setLocation(new Point(4, 15));
        this.rbSN76489S_Silent.setName("rbSN76489S_Silent");
        this.rbSN76489S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbSN76489S_Silent.TabIndex = 0
        this.rbSN76489S_Silent.setText("Silent");
        // this.rbSN76489S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbSN76489S_Emu2
        //
//            this.rbSN76489S_Emu2.AutoSize(true);
        this.rbSN76489S_Emu2.setLocation(new Point(200, 15));
        this.rbSN76489S_Emu2.setName("rbSN76489S_Emu2");
        this.rbSN76489S_Emu2.setPreferredSize(new Dimension(115, 16));
        // this.rbSN76489S_Emu2.TabIndex = 0
        this.rbSN76489S_Emu2.setText("Emulation (mame)");
        // this.rbSN76489S_Emu2.UseVisualStyl.setBackground(true);
        //
        // rbSN76489S_Emu
        //
//            this.rbSN76489S_Emu.AutoSize(true);
        this.rbSN76489S_Emu.setSelected(true);
        this.rbSN76489S_Emu.setLocation(new Point(62, 15));
        this.rbSN76489S_Emu.setName("rbSN76489S_Emu");
        this.rbSN76489S_Emu.setPreferredSize(new Dimension(118, 16));
        // this.rbSN76489S_Emu.TabIndex = 0
        // this.rbSN76489S_Emu.TabStop = true;
        this.rbSN76489S_Emu.setText("Emulation (maxim)");
        // this.rbSN76489S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox13
        //
//            this.groupBox13.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox13.add(this.cbSendWait);
        this.groupBox13.add(this.cbEmulationPCMOnly);
        this.groupBox13.add(this.cbTwice);
        this.groupBox13.setLocation(new Point(3, 671));
        this.groupBox13.setName("groupBox13");
        this.groupBox13.setPreferredSize(new Dimension(411, 59));
        // this.groupBox13.TabIndex = 6
        // this.groupBox13.TabStop = false;
        this.groupBox13.setToolTipText("Ym2612Inst(Use SCCI module Only!)");
        //
        // groupBox14
        //
//            this.groupBox14.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox14.add(this.cmbC140S_SCCI);
        this.groupBox14.add(this.rbC140S_SCCI);
        this.groupBox14.add(this.rbC140S_Silent);
        this.groupBox14.add(this.rbC140S_Emu);
        this.groupBox14.setLocation(new Point(3, 1186));
        this.groupBox14.setName("groupBox14");
        this.groupBox14.setPreferredSize(new Dimension(411, 40));
        // this.groupBox14.TabIndex = 6
        // this.groupBox14.TabStop = false;
        this.groupBox14.setToolTipText("C140Inst(Secondary)");
        //
        // cmbC140S_SCCI
        //
//            this.cmbC140S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbC140S_SCCI.FormattingEnabled = true;
        this.cmbC140S_SCCI.setLocation(new Point(220, 14));
        this.cmbC140S_SCCI.setName("cmbC140S_SCCI");
        this.cmbC140S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbC140S_SCCI.TabIndex = 2
        //
        // rbC140S_SCCI
        //
//            this.rbC140S_SCCI.AutoSize(true);
        this.rbC140S_SCCI.setLocation(new Point(165, 15));
        this.rbC140S_SCCI.setName("rbC140S_SCCI");
        this.rbC140S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbC140S_SCCI.TabIndex = 1
        this.rbC140S_SCCI.setText("Real");
        // this.rbC140S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbC140S_Silent
        //
//            this.rbC140S_Silent.AutoSize(true);
        this.rbC140S_Silent.setLocation(new Point(4, 15));
        this.rbC140S_Silent.setName("rbC140S_Silent");
        this.rbC140S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbC140S_Silent.TabIndex = 0
        this.rbC140S_Silent.setText("Silent");
        // this.rbC140S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbC140S_Emu
        //
//            this.rbC140S_Emu.AutoSize(true);
        this.rbC140S_Emu.setSelected(true);
        this.rbC140S_Emu.setLocation(new Point(62, 15));
        this.rbC140S_Emu.setName("rbC140S_Emu");
        this.rbC140S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbC140S_Emu.TabIndex = 0
        // this.rbC140S_Emu.TabStop = true;
        this.rbC140S_Emu.setText("Emulation");
        // this.rbC140S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox15
        //
//            this.groupBox15.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox15.add(this.cmbC140P_SCCI);
        this.groupBox15.add(this.rbC140P_Real);
        this.groupBox15.add(this.rbC140P_Silent);
        this.groupBox15.add(this.rbC140P_Emu);
        this.groupBox15.setLocation(new Point(3, 1140));
        this.groupBox15.setName("groupBox15");
        this.groupBox15.setPreferredSize(new Dimension(411, 40));
        // this.groupBox15.TabIndex = 7
        // this.groupBox15.TabStop = false;
        this.groupBox15.setToolTipText("C140Inst(Primary)");
        //
        // cmbC140P_SCCI
        //
//            this.cmbC140P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbC140P_SCCI.FormattingEnabled = true;
        this.cmbC140P_SCCI.setLocation(new Point(220, 14));
        this.cmbC140P_SCCI.setName("cmbC140P_SCCI");
        this.cmbC140P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbC140P_SCCI.TabIndex = 2
        //
        // rbC140P_Real
        //
//            this.rbC140P_Real.AutoSize(true);
        this.rbC140P_Real.setLocation(new Point(165, 15));
        this.rbC140P_Real.setName("rbC140P_Real");
        this.rbC140P_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbC140P_Real.TabIndex = 1
        this.rbC140P_Real.setText("Real");
        // this.rbC140P_Real.UseVisualStyl.setBackground(true);
        //
        // rbC140P_Silent
        //
//            this.rbC140P_Silent.AutoSize(true);
        this.rbC140P_Silent.setLocation(new Point(4, 15));
        this.rbC140P_Silent.setName("rbC140P_Silent");
        this.rbC140P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbC140P_Silent.TabIndex = 0
        this.rbC140P_Silent.setText("Silent");
        // this.rbC140P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbC140P_Emu
        //
//            this.rbC140P_Emu.AutoSize(true);
        this.rbC140P_Emu.setSelected(true);
        this.rbC140P_Emu.setLocation(new Point(62, 15));
        this.rbC140P_Emu.setName("rbC140P_Emu");
        this.rbC140P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbC140P_Emu.TabIndex = 0
        // this.rbC140P_Emu.TabStop = true;
        this.rbC140P_Emu.setText("Emulation");
        // this.rbC140P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox16
        //
//            this.groupBox16.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox16.add(this.cmbSEGAPCMS_SCCI);
        this.groupBox16.add(this.rbSEGAPCMS_SCCI);
        this.groupBox16.add(this.rbSEGAPCMS_Silent);
        this.groupBox16.add(this.rbSEGAPCMS_Emu);
        this.groupBox16.setLocation(new Point(3, 1278));
        this.groupBox16.setName("groupBox16");
        this.groupBox16.setPreferredSize(new Dimension(411, 40));
        // this.groupBox16.TabIndex = 8
        // this.groupBox16.TabStop = false;
        this.groupBox16.setToolTipText("SEGAPCM(Secondary)");
        //
        // cmbSEGAPCMS_SCCI
        //
//            this.cmbSEGAPCMS_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbSEGAPCMS_SCCI.FormattingEnabled = true;
        this.cmbSEGAPCMS_SCCI.setLocation(new Point(277, 14));
        this.cmbSEGAPCMS_SCCI.setName("cmbSEGAPCMS_SCCI");
        this.cmbSEGAPCMS_SCCI.setPreferredSize(new Dimension(128, 20));
        // this.cmbSEGAPCMS_SCCI.TabIndex = 2
        //
        // rbSEGAPCMS_SCCI
        //
//            this.rbSEGAPCMS_SCCI.AutoSize(true);
        this.rbSEGAPCMS_SCCI.setLocation(new Point(222, 15));
        this.rbSEGAPCMS_SCCI.setName("rbSEGAPCMS_SCCI");
        this.rbSEGAPCMS_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbSEGAPCMS_SCCI.TabIndex = 1
        this.rbSEGAPCMS_SCCI.setText("Real");
        // this.rbSEGAPCMS_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbSEGAPCMS_Silent
        //
//            this.rbSEGAPCMS_Silent.AutoSize(true);
        this.rbSEGAPCMS_Silent.setLocation(new Point(4, 15));
        this.rbSEGAPCMS_Silent.setName("rbSEGAPCMS_Silent");
        this.rbSEGAPCMS_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbSEGAPCMS_Silent.TabIndex = 0
        this.rbSEGAPCMS_Silent.setText("Silent");
        // this.rbSEGAPCMS_Silent.UseVisualStyl.setBackground(true);
        //
        // rbSEGAPCMS_Emu
        //
//            this.rbSEGAPCMS_Emu.AutoSize(true);
        this.rbSEGAPCMS_Emu.setSelected(true);
        this.rbSEGAPCMS_Emu.setLocation(new Point(62, 15));
        this.rbSEGAPCMS_Emu.setName("rbSEGAPCMS_Emu");
        this.rbSEGAPCMS_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbSEGAPCMS_Emu.TabIndex = 0
        // this.rbSEGAPCMS_Emu.TabStop = true;
        this.rbSEGAPCMS_Emu.setText("Emulation");
        // this.rbSEGAPCMS_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox17
        //
//            this.groupBox17.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox17.add(this.cmbSEGAPCMP_SCCI);
        this.groupBox17.add(this.rbSEGAPCMP_SCCI);
        this.groupBox17.add(this.rbSEGAPCMP_Silent);
        this.groupBox17.add(this.rbSEGAPCMP_Emu);
        this.groupBox17.setLocation(new Point(3, 1232));
        this.groupBox17.setName("groupBox17");
        this.groupBox17.setPreferredSize(new Dimension(411, 40));
        // this.groupBox17.TabIndex = 9
        // this.groupBox17.TabStop = false;
        this.groupBox17.setToolTipText("SEGAPCM(Primary)");
        //
        // cmbSEGAPCMP_SCCI
        //
//            this.cmbSEGAPCMP_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbSEGAPCMP_SCCI.FormattingEnabled = true;
        this.cmbSEGAPCMP_SCCI.setLocation(new Point(277, 14));
        this.cmbSEGAPCMP_SCCI.setName("cmbSEGAPCMP_SCCI");
        this.cmbSEGAPCMP_SCCI.setPreferredSize(new Dimension(128, 20));
        // this.cmbSEGAPCMP_SCCI.TabIndex = 2
        //
        // rbSEGAPCMP_SCCI
        //
//            this.rbSEGAPCMP_SCCI.AutoSize(true);
        this.rbSEGAPCMP_SCCI.setLocation(new Point(222, 15));
        this.rbSEGAPCMP_SCCI.setName("rbSEGAPCMP_SCCI");
        this.rbSEGAPCMP_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbSEGAPCMP_SCCI.TabIndex = 1
        this.rbSEGAPCMP_SCCI.setText("Real");
        // this.rbSEGAPCMP_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbSEGAPCMP_Silent
        //
//            this.rbSEGAPCMP_Silent.AutoSize(true);
        this.rbSEGAPCMP_Silent.setLocation(new Point(4, 15));
        this.rbSEGAPCMP_Silent.setName("rbSEGAPCMP_Silent");
        this.rbSEGAPCMP_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbSEGAPCMP_Silent.TabIndex = 0
        this.rbSEGAPCMP_Silent.setText("Silent");
        // this.rbSEGAPCMP_Silent.UseVisualStyl.setBackground(true);
        //
        // rbSEGAPCMP_Emu
        //
//            this.rbSEGAPCMP_Emu.AutoSize(true);
        this.rbSEGAPCMP_Emu.setSelected(true);
        this.rbSEGAPCMP_Emu.setLocation(new Point(62, 15));
        this.rbSEGAPCMP_Emu.setName("rbSEGAPCMP_Emu");
        this.rbSEGAPCMP_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbSEGAPCMP_Emu.TabIndex = 0
        // this.rbSEGAPCMP_Emu.TabStop = true;
        this.rbSEGAPCMP_Emu.setText("Emulation");
        // this.rbSEGAPCMP_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox18
        //
//            this.groupBox18.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox18.add(this.cmbYMF262P_SCCI);
        this.groupBox18.add(this.rbYMF262P_SCCI);
        this.groupBox18.add(this.rbYMF262P_Silent);
        this.groupBox18.add(this.rbYMF262P_Emu);
        this.groupBox18.setLocation(new Point(3, 912));
        this.groupBox18.setName("groupBox18");
        this.groupBox18.setPreferredSize(new Dimension(411, 38));
        // this.groupBox18.TabIndex = 3
        // this.groupBox18.TabStop = false;
        this.groupBox18.setToolTipText("YMF262(Primary)");
        //
        // cmbYMF262P_SCCI
        //
//            this.cmbYMF262P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYMF262P_SCCI.FormattingEnabled = true;
        this.cmbYMF262P_SCCI.setLocation(new Point(220, 12));
        this.cmbYMF262P_SCCI.setName("cmbYMF262P_SCCI");
        this.cmbYMF262P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYMF262P_SCCI.TabIndex = 2
        //
        // rbYMF262P_SCCI
        //
//            this.rbYMF262P_SCCI.AutoSize(true);
        this.rbYMF262P_SCCI.setLocation(new Point(165, 13));
        this.rbYMF262P_SCCI.setName("rbYMF262P_SCCI");
        this.rbYMF262P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYMF262P_SCCI.TabIndex = 1
        this.rbYMF262P_SCCI.setText("Real");
        // this.rbYMF262P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYMF262P_Silent
        //
//            this.rbYMF262P_Silent.AutoSize(true);
        this.rbYMF262P_Silent.setLocation(new Point(4, 13));
        this.rbYMF262P_Silent.setName("rbYMF262P_Silent");
        this.rbYMF262P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYMF262P_Silent.TabIndex = 0
        this.rbYMF262P_Silent.setText("Silent");
        // this.rbYMF262P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYMF262P_Emu
        //
//            this.rbYMF262P_Emu.AutoSize(true);
        this.rbYMF262P_Emu.setSelected(true);
        this.rbYMF262P_Emu.setLocation(new Point(62, 13));
        this.rbYMF262P_Emu.setName("rbYMF262P_Emu");
        this.rbYMF262P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYMF262P_Emu.TabIndex = 0
        // this.rbYMF262P_Emu.TabStop = true;
        this.rbYMF262P_Emu.setText("Emulation");
        // this.rbYMF262P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox19
        //
//            this.groupBox19.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox19.add(this.cmbYMF262S_SCCI);
        this.groupBox19.add(this.rbYMF262S_SCCI);
        this.groupBox19.add(this.rbYMF262S_Silent);
        this.groupBox19.add(this.rbYMF262S_Emu);
        this.groupBox19.setLocation(new Point(3, 956));
        this.groupBox19.setName("groupBox19");
        this.groupBox19.setPreferredSize(new Dimension(411, 38));
        // this.groupBox19.TabIndex = 4
        // this.groupBox19.TabStop = false;
        this.groupBox19.setToolTipText("YMF262(Secondary)");
        //
        // cmbYMF262S_SCCI
        //
//            this.cmbYMF262S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYMF262S_SCCI.FormattingEnabled = true;
        this.cmbYMF262S_SCCI.setLocation(new Point(220, 12));
        this.cmbYMF262S_SCCI.setName("cmbYMF262S_SCCI");
        this.cmbYMF262S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYMF262S_SCCI.TabIndex = 2
        //
        // rbYMF262S_SCCI
        //
//            this.rbYMF262S_SCCI.AutoSize(true);
        this.rbYMF262S_SCCI.setLocation(new Point(165, 13));
        this.rbYMF262S_SCCI.setName("rbYMF262S_SCCI");
        this.rbYMF262S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYMF262S_SCCI.TabIndex = 1
        this.rbYMF262S_SCCI.setText("Real");
        // this.rbYMF262S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYMF262S_Silent
        //
//            this.rbYMF262S_Silent.AutoSize(true);
        this.rbYMF262S_Silent.setLocation(new Point(4, 13));
        this.rbYMF262S_Silent.setName("rbYMF262S_Silent");
        this.rbYMF262S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYMF262S_Silent.TabIndex = 0
        this.rbYMF262S_Silent.setText("Silent");
        // this.rbYMF262S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYMF262S_Emu
        //
//            this.rbYMF262S_Emu.AutoSize(true);
        this.rbYMF262S_Emu.setSelected(true);
        this.rbYMF262S_Emu.setLocation(new Point(62, 13));
        this.rbYMF262S_Emu.setName("rbYMF262S_Emu");
        this.rbYMF262S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYMF262S_Emu.TabIndex = 0
        // this.rbYMF262S_Emu.TabStop = true;
        this.rbYMF262S_Emu.setText("Emulation");
        // this.rbYMF262S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox20
        //
//            this.groupBox20.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox20.add(this.cmbYM3812P_SCCI);
        this.groupBox20.add(this.rbYM3812P_SCCI);
        this.groupBox20.add(this.rbYM3812P_Silent);
        this.groupBox20.add(this.rbYM3812P_Emu);
        this.groupBox20.setLocation(new Point(3, 824));
        this.groupBox20.setName("groupBox20");
        this.groupBox20.setPreferredSize(new Dimension(411, 38));
        // this.groupBox20.TabIndex = 3
        // this.groupBox20.TabStop = false;
        this.groupBox20.setToolTipText("YM3812(Primary)");
        //
        // cmbYM3812P_SCCI
        //
//            this.cmbYM3812P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM3812P_SCCI.FormattingEnabled = true;
        this.cmbYM3812P_SCCI.setLocation(new Point(220, 12));
        this.cmbYM3812P_SCCI.setName("cmbYM3812P_SCCI");
        this.cmbYM3812P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM3812P_SCCI.TabIndex = 2
        //
        // rbYM3812P_SCCI
        //
//            this.rbYM3812P_SCCI.AutoSize(true);
        this.rbYM3812P_SCCI.setLocation(new Point(165, 13));
        this.rbYM3812P_SCCI.setName("rbYM3812P_SCCI");
        this.rbYM3812P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM3812P_SCCI.TabIndex = 1
        this.rbYM3812P_SCCI.setText("Real");
        // this.rbYM3812P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM3812P_Silent
        //
//            this.rbYM3812P_Silent.AutoSize(true);
        this.rbYM3812P_Silent.setLocation(new Point(4, 13));
        this.rbYM3812P_Silent.setName("rbYM3812P_Silent");
        this.rbYM3812P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM3812P_Silent.TabIndex = 0
        this.rbYM3812P_Silent.setText("Silent");
        // this.rbYM3812P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM3812P_Emu
        //
//            this.rbYM3812P_Emu.AutoSize(true);
        this.rbYM3812P_Emu.setSelected(true);
        this.rbYM3812P_Emu.setLocation(new Point(62, 13));
        this.rbYM3812P_Emu.setName("rbYM3812P_Emu");
        this.rbYM3812P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM3812P_Emu.TabIndex = 0
        // this.rbYM3812P_Emu.TabStop = true;
        this.rbYM3812P_Emu.setText("Emulation");
        // this.rbYM3812P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox21
        //
//            this.groupBox21.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox21.add(this.cmbYM3812S_SCCI);
        this.groupBox21.add(this.rbYM3812S_SCCI);
        this.groupBox21.add(this.rbYM3812S_Silent);
        this.groupBox21.add(this.rbYM3812S_Emu);
        this.groupBox21.setLocation(new Point(3, 868));
        this.groupBox21.setName("groupBox21");
        this.groupBox21.setPreferredSize(new Dimension(411, 38));
        // this.groupBox21.TabIndex = 4
        // this.groupBox21.TabStop = false;
        this.groupBox21.setToolTipText("YM3812(Secondary)");
        //
        // cmbYM3812S_SCCI
        //
//            this.cmbYM3812S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM3812S_SCCI.FormattingEnabled = true;
        this.cmbYM3812S_SCCI.setLocation(new Point(220, 12));
        this.cmbYM3812S_SCCI.setName("cmbYM3812S_SCCI");
        this.cmbYM3812S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM3812S_SCCI.TabIndex = 2
        //
        // rbYM3812S_SCCI
        //
//            this.rbYM3812S_SCCI.AutoSize(true);
        this.rbYM3812S_SCCI.setLocation(new Point(165, 13));
        this.rbYM3812S_SCCI.setName("rbYM3812S_SCCI");
        this.rbYM3812S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM3812S_SCCI.TabIndex = 1
        this.rbYM3812S_SCCI.setText("Real");
        // this.rbYM3812S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM3812S_Silent
        //
//            this.rbYM3812S_Silent.AutoSize(true);
        this.rbYM3812S_Silent.setLocation(new Point(4, 13));
        this.rbYM3812S_Silent.setName("rbYM3812S_Silent");
        this.rbYM3812S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM3812S_Silent.TabIndex = 0
        this.rbYM3812S_Silent.setText("Silent");
        // this.rbYM3812S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM3812S_Emu
        //
//            this.rbYM3812S_Emu.AutoSize(true);
        this.rbYM3812S_Emu.setSelected(true);
        this.rbYM3812S_Emu.setLocation(new Point(62, 13));
        this.rbYM3812S_Emu.setName("rbYM3812S_Emu");
        this.rbYM3812S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM3812S_Emu.TabIndex = 0
        // this.rbYM3812S_Emu.TabStop = true;
        this.rbYM3812S_Emu.setText("Emulation");
        // this.rbYM3812S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox22
        //
//            this.groupBox22.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox22.add(this.cmbYM3526S_SCCI);
        this.groupBox22.add(this.rbYM3526S_SCCI);
        this.groupBox22.add(this.rbYM3526S_Silent);
        this.groupBox22.add(this.rbYM3526S_Emu);
        this.groupBox22.setLocation(new Point(3, 780));
        this.groupBox22.setName("groupBox22");
        this.groupBox22.setPreferredSize(new Dimension(411, 38));
        // this.groupBox22.TabIndex = 6
        // this.groupBox22.TabStop = false;
        this.groupBox22.setToolTipText("YM3526(Secondary)");
        //
        // cmbYM3526S_SCCI
        //
//            this.cmbYM3526S_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM3526S_SCCI.FormattingEnabled = true;
        this.cmbYM3526S_SCCI.setLocation(new Point(220, 12));
        this.cmbYM3526S_SCCI.setName("cmbYM3526S_SCCI");
        this.cmbYM3526S_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM3526S_SCCI.TabIndex = 2
        //
        // rbYM3526S_SCCI
        //
//            this.rbYM3526S_SCCI.AutoSize(true);
        this.rbYM3526S_SCCI.setLocation(new Point(165, 13));
        this.rbYM3526S_SCCI.setName("rbYM3526S_SCCI");
        this.rbYM3526S_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM3526S_SCCI.TabIndex = 1
        this.rbYM3526S_SCCI.setText("Real");
        // this.rbYM3526S_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM3526S_Silent
        //
//            this.rbYM3526S_Silent.AutoSize(true);
        this.rbYM3526S_Silent.setLocation(new Point(4, 13));
        this.rbYM3526S_Silent.setName("rbYM3526S_Silent");
        this.rbYM3526S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM3526S_Silent.TabIndex = 0
        this.rbYM3526S_Silent.setText("Silent");
        // this.rbYM3526S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM3526S_Emu
        //
//            this.rbYM3526S_Emu.AutoSize(true);
        this.rbYM3526S_Emu.setSelected(true);
        this.rbYM3526S_Emu.setLocation(new Point(62, 13));
        this.rbYM3526S_Emu.setName("rbYM3526S_Emu");
        this.rbYM3526S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM3526S_Emu.TabIndex = 0
        // this.rbYM3526S_Emu.TabStop = true;
        this.rbYM3526S_Emu.setText("Emulation");
        // this.rbYM3526S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox23
        //
//            this.groupBox23.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox23.add(this.cmbYM3526P_SCCI);
        this.groupBox23.add(this.rbYM3526P_SCCI);
        this.groupBox23.add(this.rbYM3526P_Silent);
        this.groupBox23.add(this.rbYM3526P_Emu);
        this.groupBox23.setLocation(new Point(3, 736));
        this.groupBox23.setName("groupBox23");
        this.groupBox23.setPreferredSize(new Dimension(411, 38));
        // this.groupBox23.TabIndex = 5
        // this.groupBox23.TabStop = false;
        this.groupBox23.setToolTipText("YM3526(Primary)");
        //
        // cmbYM3526P_SCCI
        //
//            this.cmbYM3526P_SCCI.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM3526P_SCCI.FormattingEnabled = true;
        this.cmbYM3526P_SCCI.setLocation(new Point(220, 12));
        this.cmbYM3526P_SCCI.setName("cmbYM3526P_SCCI");
        this.cmbYM3526P_SCCI.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM3526P_SCCI.TabIndex = 2
        //
        // rbYM3526P_SCCI
        //
//            this.rbYM3526P_SCCI.AutoSize(true);
        this.rbYM3526P_SCCI.setLocation(new Point(165, 13));
        this.rbYM3526P_SCCI.setName("rbYM3526P_SCCI");
        this.rbYM3526P_SCCI.setPreferredSize(new Dimension(46, 16));
        // this.rbYM3526P_SCCI.TabIndex = 1
        this.rbYM3526P_SCCI.setText("Real");
        // this.rbYM3526P_SCCI.UseVisualStyl.setBackground(true);
        //
        // rbYM3526P_Silent
        //
//            this.rbYM3526P_Silent.AutoSize(true);
        this.rbYM3526P_Silent.setLocation(new Point(4, 13));
        this.rbYM3526P_Silent.setName("rbYM3526P_Silent");
        this.rbYM3526P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM3526P_Silent.TabIndex = 0
        this.rbYM3526P_Silent.setText("Silent");
        // this.rbYM3526P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM3526P_Emu
        //
//            this.rbYM3526P_Emu.AutoSize(true);
        this.rbYM3526P_Emu.setSelected(true);
        this.rbYM3526P_Emu.setLocation(new Point(62, 13));
        this.rbYM3526P_Emu.setName("rbYM3526P_Emu");
        this.rbYM3526P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM3526P_Emu.TabIndex = 0
        // this.rbYM3526P_Emu.TabStop = true;
        this.rbYM3526P_Emu.setText("Emulation");
        // this.rbYM3526P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox24
        //
//            this.groupBox24.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox24.add(this.cmbYM2413S_Real);
        this.groupBox24.add(this.rbYM2413S_Real);
        this.groupBox24.add(this.rbYM2413S_Silent);
        this.groupBox24.add(this.rbYM2413S_Emu);
        this.groupBox24.setLocation(new Point(3, 267));
        this.groupBox24.setName("groupBox24");
        this.groupBox24.setPreferredSize(new Dimension(411, 38));
        // this.groupBox24.TabIndex = 3
        // this.groupBox24.TabStop = false;
        this.groupBox24.setToolTipText("YM2413(Secondary)");
        //
        // cmbYM2413S_Real
        //
//            this.cmbYM2413S_Real.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2413S_Real.FormattingEnabled = true;
        this.cmbYM2413S_Real.setLocation(new Point(220, 12));
        this.cmbYM2413S_Real.setName("cmbYM2413S_Real");
        this.cmbYM2413S_Real.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2413S_Real.TabIndex = 2
        //
        // rbYM2413S_Real
        //
//            this.rbYM2413S_Real.AutoSize(true);
        this.rbYM2413S_Real.setLocation(new Point(165, 13));
        this.rbYM2413S_Real.setName("rbYM2413S_Real");
        this.rbYM2413S_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2413S_Real.TabIndex = 1
        this.rbYM2413S_Real.setText("Real");
        // this.rbYM2413S_Real.UseVisualStyl.setBackground(true);
        //
        // rbYM2413S_Silent
        //
//            this.rbYM2413S_Silent.AutoSize(true);
        this.rbYM2413S_Silent.setLocation(new Point(4, 13));
        this.rbYM2413S_Silent.setName("rbYM2413S_Silent");
        this.rbYM2413S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2413S_Silent.TabIndex = 0
        this.rbYM2413S_Silent.setText("Silent");
        // this.rbYM2413S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2413S_Emu
        //
//            this.rbYM2413S_Emu.AutoSize(true);
        this.rbYM2413S_Emu.setSelected(true);
        this.rbYM2413S_Emu.setLocation(new Point(62, 13));
        this.rbYM2413S_Emu.setName("rbYM2413S_Emu");
        this.rbYM2413S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2413S_Emu.TabIndex = 0
        // this.rbYM2413S_Emu.TabStop = true;
        this.rbYM2413S_Emu.setText("Emulation");
        // this.rbYM2413S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox25
        //
//            this.groupBox25.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox25.add(this.cmbYM2413P_Real);
        this.groupBox25.add(this.rbYM2413P_Real);
        this.groupBox25.add(this.rbYM2413P_Silent);
        this.groupBox25.add(this.rbYM2413P_Emu);
        this.groupBox25.setLocation(new Point(3, 223));
        this.groupBox25.setName("groupBox25");
        this.groupBox25.setPreferredSize(new Dimension(411, 38));
        // this.groupBox25.TabIndex = 4
        // this.groupBox25.TabStop = false;
        this.groupBox25.setToolTipText("YM2413(Primary)");
        //
        // cmbYM2413P_Real
        //
//            this.cmbYM2413P_Real.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbYM2413P_Real.FormattingEnabled = true;
        this.cmbYM2413P_Real.setLocation(new Point(220, 12));
        this.cmbYM2413P_Real.setName("cmbYM2413P_Real");
        this.cmbYM2413P_Real.setPreferredSize(new Dimension(185, 20));
        // this.cmbYM2413P_Real.TabIndex = 2
        //
        // rbYM2413P_Real
        //
//            this.rbYM2413P_Real.AutoSize(true);
        this.rbYM2413P_Real.setLocation(new Point(165, 13));
        this.rbYM2413P_Real.setName("rbYM2413P_Real");
        this.rbYM2413P_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbYM2413P_Real.TabIndex = 1
        this.rbYM2413P_Real.setText("Real");
        // this.rbYM2413P_Real.UseVisualStyl.setBackground(true);
        //
        // rbYM2413P_Silent
        //
//            this.rbYM2413P_Silent.AutoSize(true);
        this.rbYM2413P_Silent.setLocation(new Point(4, 13));
        this.rbYM2413P_Silent.setName("rbYM2413P_Silent");
        this.rbYM2413P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbYM2413P_Silent.TabIndex = 0
        this.rbYM2413P_Silent.setText("Silent");
        // this.rbYM2413P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbYM2413P_Emu
        //
//            this.rbYM2413P_Emu.AutoSize(true);
        this.rbYM2413P_Emu.setSelected(true);
        this.rbYM2413P_Emu.setLocation(new Point(62, 13));
        this.rbYM2413P_Emu.setName("rbYM2413P_Emu");
        this.rbYM2413P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbYM2413P_Emu.TabIndex = 0
        // this.rbYM2413P_Emu.TabStop = true;
        this.rbYM2413P_Emu.setText("Emulation");
        // this.rbYM2413P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox26
        //
//            this.groupBox26.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox26.add(this.cmbAY8910P_Real);
        this.groupBox26.add(this.rbAY8910P_Real);
        this.groupBox26.add(this.rbAY8910P_Silent);
        this.groupBox26.add(this.rbAY8910P_Emu2);
        this.groupBox26.add(this.rbAY8910P_Emu);
        this.groupBox26.setLocation(new Point(3, 1324));
        this.groupBox26.setName("groupBox26");
        this.groupBox26.setPreferredSize(new Dimension(411, 64));
        // this.groupBox26.TabIndex = 6
        // this.groupBox26.TabStop = false;
        this.groupBox26.setToolTipText("AY-3-8910(Primary)");
        //
        // cmbAY8910P_Real
        //
//            this.cmbAY8910P_Real.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbAY8910P_Real.FormattingEnabled = true;
        this.cmbAY8910P_Real.setLocation(new Point(220, 38));
        this.cmbAY8910P_Real.setName("cmbAY8910P_Real");
        this.cmbAY8910P_Real.setPreferredSize(new Dimension(185, 20));
        // this.cmbAY8910P_Real.TabIndex = 2
        //
        // rbAY8910P_Real
        //
//            this.rbAY8910P_Real.AutoSize(true);
        this.rbAY8910P_Real.setLocation(new Point(165, 39));
        this.rbAY8910P_Real.setName("rbAY8910P_Real");
        this.rbAY8910P_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbAY8910P_Real.TabIndex = 1
        this.rbAY8910P_Real.setText("Real");
        // this.rbAY8910P_Real.UseVisualStyl.setBackground(true);
        //
        // rbAY8910P_Silent
        //
//            this.rbAY8910P_Silent.AutoSize(true);
        this.rbAY8910P_Silent.setLocation(new Point(4, 15));
        this.rbAY8910P_Silent.setName("rbAY8910P_Silent");
        this.rbAY8910P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbAY8910P_Silent.TabIndex = 0
        this.rbAY8910P_Silent.setText("Silent");
        // this.rbAY8910P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbAY8910P_Emu
        //
//            this.rbAY8910P_Emu.AutoSize(true);
        this.rbAY8910P_Emu.setSelected(true);
        this.rbAY8910P_Emu.setLocation(new Point(62, 15));
        this.rbAY8910P_Emu.setName("rbAY8910P_Emu");
        this.rbAY8910P_Emu.setPreferredSize(new Dimension(112, 16));
        // this.rbAY8910P_Emu.TabIndex = 0
        this.rbAY8910P_Emu.setText("Emulation(fmgen)");
        // this.rbAY8910P_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox27
        //
//            this.groupBox27.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox27.add(this.cmbAY8910S_Real);
        this.groupBox27.add(this.rbAY8910S_Real);
        this.groupBox27.add(this.rbAY8910S_Silent);
        this.groupBox27.add(this.rbAY8910S_Emu2);
        this.groupBox27.add(this.rbAY8910S_Emu);
        this.groupBox27.setLocation(new Point(3, 1394));
        this.groupBox27.setName("groupBox27");
        this.groupBox27.setPreferredSize(new Dimension(411, 64));
        // this.groupBox27.TabIndex = 7
        // this.groupBox27.TabStop = false;
        this.groupBox27.setToolTipText("AY-3-8910(Secondary)");
        //
        // cmbAY8910S_Real
        //
//            this.cmbAY8910S_Real.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbAY8910S_Real.FormattingEnabled = true;
        this.cmbAY8910S_Real.setLocation(new Point(220, 38));
        this.cmbAY8910S_Real.setName("cmbAY8910S_Real");
        this.cmbAY8910S_Real.setPreferredSize(new Dimension(185, 20));
        // this.cmbAY8910S_Real.TabIndex = 2
        //
        // rbAY8910S_Real
        //
//            this.rbAY8910S_Real.AutoSize(true);
        this.rbAY8910S_Real.setLocation(new Point(165, 39));
        this.rbAY8910S_Real.setName("rbAY8910S_Real");
        this.rbAY8910S_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbAY8910S_Real.TabIndex = 1
        this.rbAY8910S_Real.setText("Real");
        // this.rbAY8910S_Real.UseVisualStyl.setBackground(true);
        //
        // rbAY8910S_Silent
        //
//            this.rbAY8910S_Silent.AutoSize(true);
        this.rbAY8910S_Silent.setLocation(new Point(4, 15));
        this.rbAY8910S_Silent.setName("rbAY8910S_Silent");
        this.rbAY8910S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbAY8910S_Silent.TabIndex = 0
        this.rbAY8910S_Silent.setText("Silent");
        // this.rbAY8910S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbAY8910S_Emu
        //
//            this.rbAY8910S_Emu.AutoSize(true);
        this.rbAY8910S_Emu.setSelected(true);
        this.rbAY8910S_Emu.setLocation(new Point(62, 15));
        this.rbAY8910S_Emu.setName("rbAY8910S_Emu");
        this.rbAY8910S_Emu.setPreferredSize(new Dimension(112, 16));
        // this.rbAY8910S_Emu.TabIndex = 0
        this.rbAY8910S_Emu.setText("Emulation(fmgen)");
        // this.rbAY8910S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox28
        //
//            this.groupBox28.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox28.add(this.cmbK051649S_Real);
        this.groupBox28.add(this.rbK051649S_Real);
        this.groupBox28.add(this.rbK051649S_Silent);
        this.groupBox28.add(this.rbK051649S_Emu);
        this.groupBox28.setLocation(new Point(3, 1510));
        this.groupBox28.setName("groupBox28");
        this.groupBox28.setPreferredSize(new Dimension(411, 40));
        // this.groupBox28.TabIndex = 9
        // this.groupBox28.TabStop = false;
        this.groupBox28.setToolTipText("K051649Inst(Secondary)");
        //
        // cmbK051649S_Real
        //
//            this.cmbK051649S_Real.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbK051649S_Real.FormattingEnabled = true;
        this.cmbK051649S_Real.setLocation(new Point(220, 14));
        this.cmbK051649S_Real.setName("cmbK051649S_Real");
        this.cmbK051649S_Real.setPreferredSize(new Dimension(185, 20));
        // this.cmbK051649S_Real.TabIndex = 2
        //
        // rbK051649S_Real
        //
//            this.rbK051649S_Real.AutoSize(true);
        this.rbK051649S_Real.setLocation(new Point(165, 15));
        this.rbK051649S_Real.setName("rbK051649S_Real");
        this.rbK051649S_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbK051649S_Real.TabIndex = 1
        this.rbK051649S_Real.setText("Real");
        // this.rbK051649S_Real.UseVisualStyl.setBackground(true);
        //
        // rbK051649S_Silent
        //
//            this.rbK051649S_Silent.AutoSize(true);
        this.rbK051649S_Silent.setLocation(new Point(4, 15));
        this.rbK051649S_Silent.setName("rbK051649S_Silent");
        this.rbK051649S_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbK051649S_Silent.TabIndex = 0
        this.rbK051649S_Silent.setText("Silent");
        // this.rbK051649S_Silent.UseVisualStyl.setBackground(true);
        //
        // rbK051649S_Emu
        //
//            this.rbK051649S_Emu.AutoSize(true);
        this.rbK051649S_Emu.setSelected(true);
        this.rbK051649S_Emu.setLocation(new Point(62, 15));
        this.rbK051649S_Emu.setName("rbK051649S_Emu");
        this.rbK051649S_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbK051649S_Emu.TabIndex = 0
        // this.rbK051649S_Emu.TabStop = true;
        this.rbK051649S_Emu.setText("Emulation");
        // this.rbK051649S_Emu.UseVisualStyl.setBackground(true);
        //
        // groupBox29
        //
//            this.groupBox29.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.groupBox29.add(this.cmbK051649P_Real);
        this.groupBox29.add(this.rbK051649P_Real);
        this.groupBox29.add(this.rbK051649P_Silent);
        this.groupBox29.add(this.rbK051649P_Emu);
        this.groupBox29.setLocation(new Point(3, 1464));
        this.groupBox29.setName("groupBox29");
        this.groupBox29.setPreferredSize(new Dimension(411, 40));
        // this.groupBox29.TabIndex = 8
        // this.groupBox29.TabStop = false;
        this.groupBox29.setToolTipText("K051649Inst(Primary)");
        //
        // cmbK051649P_Real
        //
//            this.cmbK051649P_Real.DropDownStyle = JComboBoxStyle.DropDownList;
//            this.cmbK051649P_Real.FormattingEnabled = true;
        this.cmbK051649P_Real.setLocation(new Point(220, 14));
        this.cmbK051649P_Real.setName("cmbK051649P_Real");
        this.cmbK051649P_Real.setPreferredSize(new Dimension(185, 20));
        // this.cmbK051649P_Real.TabIndex = 2
        //
        // rbK051649P_Real
        //
//            this.rbK051649P_Real.AutoSize(true);
        this.rbK051649P_Real.setLocation(new Point(165, 15));
        this.rbK051649P_Real.setName("rbK051649P_Real");
        this.rbK051649P_Real.setPreferredSize(new Dimension(46, 16));
        // this.rbK051649P_Real.TabIndex = 1
        this.rbK051649P_Real.setText("Real");
        // this.rbK051649P_Real.UseVisualStyl.setBackground(true);
        //
        // rbK051649P_Silent
        //
//            this.rbK051649P_Silent.AutoSize(true);
        this.rbK051649P_Silent.setLocation(new Point(4, 15));
        this.rbK051649P_Silent.setName("rbK051649P_Silent");
        this.rbK051649P_Silent.setPreferredSize(new Dimension(52, 16));
        // this.rbK051649P_Silent.TabIndex = 0
        this.rbK051649P_Silent.setText("Silent");
        // this.rbK051649P_Silent.UseVisualStyl.setBackground(true);
        //
        // rbK051649P_Emu
        //
//            this.rbK051649P_Emu.AutoSize(true);
        this.rbK051649P_Emu.setSelected(true);
        this.rbK051649P_Emu.setLocation(new Point(62, 15));
        this.rbK051649P_Emu.setName("rbK051649P_Emu");
        this.rbK051649P_Emu.setPreferredSize(new Dimension(73, 16));
        // this.rbK051649P_Emu.TabIndex = 0
        // this.rbK051649P_Emu.TabStop = true;
        this.rbK051649P_Emu.setText("Emulation");
        // this.rbK051649P_Emu.UseVisualStyl.setBackground(true);
        //
        // rbAY8910P_Emu2
        //
//            this.rbAY8910P_Emu2.AutoSize(true);
        this.rbAY8910P_Emu2.setLocation(new Point(200, 15));
        this.rbAY8910P_Emu2.setName("rbAY8910P_Emu2");
        this.rbAY8910P_Emu2.setPreferredSize(new Dimension(111, 16));
        // this.rbAY8910P_Emu2.TabIndex = 0
        this.rbAY8910P_Emu2.setText("Emulation(mame)");
        // this.rbAY8910P_Emu2.UseVisualStyl.setBackground(true);
        //
        // rbAY8910S_Emu2
        //
//            this.rbAY8910S_Emu2.AutoSize(true);
        this.rbAY8910S_Emu2.setLocation(new Point(200, 15));
        this.rbAY8910S_Emu2.setName("rbAY8910S_Emu2");
        this.rbAY8910S_Emu2.setPreferredSize(new Dimension(111, 16));
        // this.rbAY8910S_Emu2.TabIndex = 0
        this.rbAY8910S_Emu2.setText("Emulation(mame)");
        // this.rbAY8910S_Emu2.UseVisualStyl.setBackground(true);
        //
        // ucSettingInstruments
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
//            this.AutoScroll = true;
        this.add(this.groupBox28);
        this.add(this.groupBox27);
        this.add(this.groupBox29);
        this.add(this.groupBox26);
        this.add(this.groupBox24);
        this.add(this.groupBox22);
        this.add(this.groupBox25);
        this.add(this.groupBox21);
        this.add(this.groupBox23);
        this.add(this.groupBox19);
        this.add(this.groupBox16);
        this.add(this.groupBox14);
        this.add(this.groupBox17);
        this.add(this.groupBox13);
        this.add(this.groupBox15);
        this.add(this.groupBox12);
        this.add(this.groupBox11);
        this.add(this.groupBox2);
        this.add(this.groupBox10);
        this.add(this.groupBox9);
        this.add(this.groupBox1);
        this.add(this.groupBox8);
        this.add(this.groupBox20);
        this.add(this.groupBox18);
        this.add(this.groupBox7);
        this.add(this.groupBox4);
        this.add(this.groupBox6);
        this.add(this.groupBox5);
        this.add(this.groupBox3);
        this.setName("ucSettingInstruments");
        this.setPreferredSize(new Dimension(417, 1558));
        // this.groupBox1.ResumeLayout(false);
        // this.groupBox1.PerformLayout();
        // this.groupBox2.ResumeLayout(false);
        // this.groupBox2.PerformLayout();
        // this.groupBox3.ResumeLayout(false);
        // this.groupBox3.PerformLayout();
        // this.groupBox4.ResumeLayout(false);
        // this.groupBox4.PerformLayout();
        // this.groupBox5.ResumeLayout(false);
        // this.groupBox5.PerformLayout();
        // this.groupBox6.ResumeLayout(false);
        // this.groupBox6.PerformLayout();
        // this.groupBox7.ResumeLayout(false);
        // this.groupBox7.PerformLayout();
        // this.groupBox8.ResumeLayout(false);
        // this.groupBox8.PerformLayout();
        // this.groupBox9.ResumeLayout(false);
        // this.groupBox9.PerformLayout();
        // this.groupBox10.ResumeLayout(false);
        // this.groupBox10.PerformLayout();
        // this.groupBox11.ResumeLayout(false);
        // this.groupBox11.PerformLayout();
        // this.groupBox12.ResumeLayout(false);
        // this.groupBox12.PerformLayout();
        // this.groupBox13.ResumeLayout(false);
        // this.groupBox13.PerformLayout();
        // this.groupBox14.ResumeLayout(false);
        // this.groupBox14.PerformLayout();
        // this.groupBox15.ResumeLayout(false);
        // this.groupBox15.PerformLayout();
        // this.groupBox16.ResumeLayout(false);
        // this.groupBox16.PerformLayout();
        // this.groupBox17.ResumeLayout(false);
        // this.groupBox17.PerformLayout();
        // this.groupBox18.ResumeLayout(false);
        // this.groupBox18.PerformLayout();
        // this.groupBox19.ResumeLayout(false);
        // this.groupBox19.PerformLayout();
        // this.groupBox20.ResumeLayout(false);
        // this.groupBox20.PerformLayout();
        // this.groupBox21.ResumeLayout(false);
        // this.groupBox21.PerformLayout();
        // this.groupBox22.ResumeLayout(false);
        // this.groupBox22.PerformLayout();
        // this.groupBox23.ResumeLayout(false);
        // this.groupBox23.PerformLayout();
        // this.groupBox24.ResumeLayout(false);
        // this.groupBox24.PerformLayout();
        // this.groupBox25.ResumeLayout(false);
        // this.groupBox25.PerformLayout();
        // this.groupBox26.ResumeLayout(false);
        // this.groupBox26.PerformLayout();
        // this.groupBox27.ResumeLayout(false);
        // this.groupBox27.PerformLayout();
        // this.groupBox28.ResumeLayout(false);
        // this.groupBox28.PerformLayout();
        // this.groupBox29.ResumeLayout(false);
        // this.groupBox29.PerformLayout();
//            this.ResumeLayout(false);

    }

    // //#endregion
    private JPanel groupBox1;
    private JPanel groupBox2;
    private JPanel groupBox3;
    private JPanel groupBox4;
    private JPanel groupBox5;
    private JPanel groupBox6;
    private JPanel groupBox7;
    private JPanel groupBox8;
    private JPanel groupBox9;
    private JPanel groupBox10;
    private JPanel groupBox11;
    private JPanel groupBox12;
    public JComboBox<String> cmbYM2151P_SCCI;
    public JCheckBox rbYM2151P_SCCI;
    public JCheckBox rbYM2151P_Silent;
    public JCheckBox rbYM2151P_Emu;
    public JComboBox<String> cmbYM2151S_SCCI;
    public JCheckBox rbYM2151S_SCCI;
    public JCheckBox rbYM2151S_Silent;
    public JCheckBox rbYM2151S_Emu;
    public JComboBox<String> cmbYM2203P_SCCI;
    public JCheckBox rbYM2203P_SCCI;
    public JCheckBox rbYM2203P_Emu;
    public JCheckBox rbYM2203P_Silent;
    public JComboBox<String> cmbYM2203S_SCCI;
    public JCheckBox rbYM2203S_SCCI;
    public JCheckBox rbYM2203S_Silent;
    public JCheckBox rbYM2203S_Emu;
    public JComboBox<String> cmbYM2608P_SCCI;
    public JCheckBox rbYM2608P_SCCI;
    public JCheckBox rbYM2608P_Emu;
    public JCheckBox rbYM2608P_Silent;
    public JComboBox<String> cmbYM2608S_SCCI;
    public JCheckBox rbYM2608S_SCCI;
    public JCheckBox rbYM2608S_Silent;
    public JCheckBox rbYM2608S_Emu;
    public JComboBox<String> cmbYM2610BP_SCCI;
    public JCheckBox rbYM2610BP_SCCI;
    public JCheckBox rbYM2610BP_Emu;
    public JCheckBox rbYM2610BP_Silent;
    public JComboBox<String> cmbYM2610BS_SCCI;
    public JCheckBox rbYM2610BS_SCCI;
    public JCheckBox rbYM2610BS_Silent;
    public JCheckBox rbYM2610BS_Emu;
    public JCheckBox cbEmulationPCMOnly;
    public JCheckBox cbTwice;
    public JCheckBox cbSendWait;
    public JComboBox<String> cmbYM2612P_SCCI;
    public JCheckBox rbYM2612P_SCCI;
    public JCheckBox rbYM2612P_Emu;
    public JCheckBox rbYM2612P_Silent;
    public JCheckBox rbYM2612S_Silent;
    public JComboBox<String> cmbYM2612S_SCCI;
    public JCheckBox rbYM2612S_SCCI;
    public JCheckBox rbYM2612S_Emu;
    public JComboBox<String> cmbSN76489P_SCCI;
    public JCheckBox rbSN76489P_SCCI;
    public JCheckBox rbSN76489P_Emu;
    public JCheckBox rbSN76489P_Silent;
    public JComboBox<String> cmbSN76489S_SCCI;
    public JCheckBox rbSN76489S_SCCI;
    public JCheckBox rbSN76489S_Silent;
    public JCheckBox rbSN76489S_Emu;
    private JPanel groupBox13;
    private JPanel groupBox14;
    public JComboBox<String> cmbC140S_SCCI;
    public JCheckBox rbC140S_SCCI;
    public JCheckBox rbC140S_Silent;
    public JCheckBox rbC140S_Emu;
    private JPanel groupBox15;
    public JComboBox<String> cmbC140P_SCCI;
    public JCheckBox rbC140P_Real;
    public JCheckBox rbC140P_Silent;
    public JCheckBox rbC140P_Emu;
    private JPanel groupBox16;
    public JComboBox<String> cmbSEGAPCMS_SCCI;
    public JCheckBox rbSEGAPCMS_SCCI;
    public JCheckBox rbSEGAPCMS_Silent;
    public JCheckBox rbSEGAPCMS_Emu;
    private JPanel groupBox17;
    public JComboBox<String> cmbSEGAPCMP_SCCI;
    public JCheckBox rbSEGAPCMP_SCCI;
    public JCheckBox rbSEGAPCMP_Silent;
    public JCheckBox rbSEGAPCMP_Emu;
    private JLabel label1;
    public JComboBox<String> cmbYM2610BEP_SCCI;
    public JComboBox<String> cmbSPPCMP_SCCI;
    public JCheckBox rbYM2610BEP_SCCI;
    private JLabel label2;
    public JComboBox<String> cmbYM2610BES_SCCI;
    public JComboBox<String> cmbSPPCMS_SCCI;
    public JCheckBox rbYM2610BES_SCCI;
    public JCheckBox rbYM2151P_EmuMame;
    public JCheckBox rbYM2151S_EmuMame;
    public JCheckBox rbYM2151P_EmuX68Sound;
    public JCheckBox rbYM2151S_EmuX68Sound;
    public JCheckBox rbYM2612P_EmuNuked;
    public JCheckBox rbYM2612S_EmuNuked;
    private JPanel groupBox18;
    public JComboBox<String> cmbYMF262P_SCCI;
    public JCheckBox rbYMF262P_SCCI;
    public JCheckBox rbYMF262P_Silent;
    public JCheckBox rbYMF262P_Emu;
    private JPanel groupBox19;
    public JComboBox<String> cmbYMF262S_SCCI;
    public JCheckBox rbYMF262S_SCCI;
    public JCheckBox rbYMF262S_Silent;
    public JCheckBox rbYMF262S_Emu;
    private JPanel groupBox20;
    public JComboBox<String> cmbYM3812P_SCCI;
    public JCheckBox rbYM3812P_SCCI;
    public JCheckBox rbYM3812P_Silent;
    public JCheckBox rbYM3812P_Emu;
    private JPanel groupBox21;
    public JComboBox<String> cmbYM3812S_SCCI;
    public JCheckBox rbYM3812S_SCCI;
    public JCheckBox rbYM3812S_Silent;
    public JCheckBox rbYM3812S_Emu;
    private JPanel groupBox22;
    public JComboBox<String> cmbYM3526S_SCCI;
    public JCheckBox rbYM3526S_SCCI;
    public JCheckBox rbYM3526S_Silent;
    public JCheckBox rbYM3526S_Emu;
    private JPanel groupBox23;
    public JComboBox<String> cmbYM3526P_SCCI;
    public JCheckBox rbYM3526P_SCCI;
    public JCheckBox rbYM3526P_Silent;
    public JCheckBox rbYM3526P_Emu;
    public JCheckBox rbYM2612P_EmuMame;
    public JCheckBox rbYM2612S_EmuMame;
    private JPanel groupBox24;
    public JComboBox<String> cmbYM2413S_Real;
    public JCheckBox rbYM2413S_Real;
    public JCheckBox rbYM2413S_Silent;
    public JCheckBox rbYM2413S_Emu;
    private JPanel groupBox25;
    public JComboBox<String> cmbYM2413P_Real;
    public JCheckBox rbYM2413P_Real;
    public JCheckBox rbYM2413P_Silent;
    public JCheckBox rbYM2413P_Emu;
    public JCheckBox rbSN76489P_Emu2;
    public JCheckBox rbSN76489S_Emu2;
    private JPanel groupBox26;
    public JComboBox<String> cmbAY8910P_Real;
    public JCheckBox rbAY8910P_Real;
    public JCheckBox rbAY8910P_Silent;
    public JCheckBox rbAY8910P_Emu;
    private JPanel groupBox27;
    public JComboBox<String> cmbAY8910S_Real;
    public JCheckBox rbAY8910S_Real;
    public JCheckBox rbAY8910S_Silent;
    public JCheckBox rbAY8910S_Emu;
    private JPanel groupBox28;
    public JComboBox<String> cmbK051649S_Real;
    public JCheckBox rbK051649S_Real;
    public JCheckBox rbK051649S_Silent;
    public JCheckBox rbK051649S_Emu;
    private JPanel groupBox29;
    public JComboBox<String> cmbK051649P_Real;
    public JCheckBox rbK051649P_Real;
    public JCheckBox rbK051649P_Silent;
    public JCheckBox rbK051649P_Emu;
    public JCheckBox rbAY8910P_Emu2;
    public JCheckBox rbAY8910S_Emu2;
}
