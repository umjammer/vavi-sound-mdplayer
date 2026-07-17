package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmInstFormat;
import mdplayer.MidiOutInfo;
import mdplayer.RealChip.EnmRealChipType;
import mdplayer.Setting;
import mdplayer.Setting.ChipType2;
import mdplayer.chips.RealChipPlugin;
import mdplayer.form.Layouts;

import static java.lang.System.getLogger;


public class FormSetting extends JDialog {

    private static final Logger logger = getLogger(FormSetting.class.getName());

    /** the designer's geometry and captions, converted from frmSetting.resx */
    private static final ResourceBundle resources = ResourceBundle.getBundle("mdplayer/form/sys/frmSetting", Locale.getDefault());
    private static final ResourceBundle rb2 = ResourceBundle.getBundle("mdplayer/properties/resources");

    private static final boolean asioSupported = true;
    private static final boolean wasapiSupported = true;
    public final Setting setting;
    private boolean IsInitialOpenFolder;
    final JTable[] dgv;

    private int dialogResult;

    int showDialog() {
        setVisible(true);
        return dialogResult;
    }

    public FormSetting(Setting setting) {
        setModal(true);
        this.setting = setting.clone();

        initializeComponent();

        dgv = new JTable[] {
                dgvMIDIoutListA, dgvMIDIoutListB, dgvMIDIoutListC, dgvMIDIoutListD, dgvMIDIoutListE,
                dgvMIDIoutListF, dgvMIDIoutListG, dgvMIDIoutListH, dgvMIDIoutListI, dgvMIDIoutListJ
        };

        init();
    }

    public void init() {

        this.labelProductName.setText(getAssemblyProduct());
        this.labelVersion.setText("version %s".formatted(getAssemblyVersion()));
        this.labelCopyright.setText(getAssemblyCopyright());
        this.labelCompanyName.setText(getAssemblyCompany());
        this.textBoxDescription.setText(rb2.getString("cntDescription"));

        this.cmbLatency.setSelectedIndex(5);
        this.cmbWaitTime.setSelectedIndex(0);
        cbUnuseRealChip.setSelected(setting.getUnuseRealChip());

        // Enumerate devices in the Combobox
        Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixersInfo) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
            for (Line.Info info : sourceLineInfo) {
                if (info instanceof DataLine.Info dataLineInfo)
                    cmbDirectSoundDevice.addItem(dataLineInfo.toString());
            }
        }

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                ((DefaultComboBoxModel<String>) cmbMIDIIN.getModel()).addElement(device.getDeviceInfo().getName());
            } catch (MidiUnavailableException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
        if (cmbMIDIIN.getItemCount() > 0)
            cmbMIDIIN.setSelectedIndex(0);

        if (ucSI != null) {
            setRealCombo(EnmRealChipType.YM2612,
                    ucSI.cmbYM2612P_SCCI, ucSI.rbYM2612P_SCCI,
                    ucSI.cmbYM2612S_SCCI, ucSI.rbYM2612S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2413,
                    ucSI.cmbYM2413P_Real, ucSI.rbYM2413P_Real,
                    ucSI.cmbYM2413S_Real, ucSI.rbYM2413S_Real
            );

            setRealCombo(EnmRealChipType.AY8910,
                    ucSI.cmbAY8910P_Real, ucSI.rbAY8910P_Real,
                    ucSI.cmbAY8910S_Real, ucSI.rbAY8910S_Real
            );

            setRealCombo(EnmRealChipType.SN76489,
                    ucSI.cmbSN76489P_SCCI, ucSI.rbSN76489P_SCCI,
                    ucSI.cmbSN76489S_SCCI, ucSI.rbSN76489S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2608,
                    ucSI.cmbYM2608P_SCCI, ucSI.rbYM2608P_SCCI,
                    ucSI.cmbYM2608S_SCCI, ucSI.rbYM2608S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2610,
                    ucSI.cmbYM2610BP_SCCI, ucSI.rbYM2610BP_SCCI,
                    ucSI.cmbYM2610BS_SCCI, ucSI.rbYM2610BS_SCCI
            );

            setRealCombo(EnmRealChipType.YM2608,
                    ucSI.cmbYM2610BEP_SCCI, ucSI.rbYM2610BEP_SCCI,
                    ucSI.cmbYM2610BES_SCCI, ucSI.rbYM2610BES_SCCI
            );

            setRealCombo(EnmRealChipType.SPPCM,
                    ucSI.cmbSPPCMP_SCCI, null,
                    ucSI.cmbSPPCMS_SCCI, null
            );

            ucSI.rbYM2610BEP_SCCI.setEnabled((ucSI.cmbYM2610BEP_SCCI.isEnabled() || ucSI.cmbSPPCMP_SCCI.isEnabled()));
            ucSI.rbYM2610BES_SCCI.setEnabled((ucSI.cmbYM2610BES_SCCI.isEnabled() || ucSI.cmbSPPCMS_SCCI.isEnabled()));

            setRealCombo(EnmRealChipType.YM2151,
                    ucSI.cmbYM2151P_SCCI, ucSI.rbYM2151P_SCCI,
                    ucSI.cmbYM2151S_SCCI, ucSI.rbYM2151S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2203,
                    ucSI.cmbYM2203P_SCCI, ucSI.rbYM2203P_SCCI,
                    ucSI.cmbYM2203S_SCCI, ucSI.rbYM2203S_SCCI
            );

            setRealCombo(EnmRealChipType.C140,
                    ucSI.cmbC140P_SCCI, ucSI.rbC140P_Real,
                    ucSI.cmbC140S_SCCI, ucSI.rbC140S_SCCI
            );

            setRealCombo(EnmRealChipType.SEGAPCM,
                    ucSI.cmbSEGAPCMP_SCCI, ucSI.rbSEGAPCMP_SCCI,
                    ucSI.cmbSEGAPCMS_SCCI, ucSI.rbSEGAPCMS_SCCI
            );

            setRealCombo(EnmRealChipType.YMF262,
                    ucSI.cmbYMF262P_SCCI, ucSI.rbYMF262P_SCCI,
                    ucSI.cmbYMF262S_SCCI, ucSI.rbYMF262S_SCCI
            );

            setRealCombo(EnmRealChipType.YM3526,
                    ucSI.cmbYM3526P_SCCI, ucSI.rbYM3526P_SCCI,
                    ucSI.cmbYM3526S_SCCI, ucSI.rbYM3526S_SCCI
            );

            setRealCombo(EnmRealChipType.YM3812,
                    ucSI.cmbYM3812P_SCCI, ucSI.rbYM3812P_SCCI,
                    ucSI.cmbYM3812S_SCCI, ucSI.rbYM3812S_SCCI
            );

            setRealCombo(EnmRealChipType.K051649,
                    ucSI.cmbK051649P_Real, ucSI.rbK051649P_Real,
                    ucSI.cmbK051649S_Real, ucSI.rbK051649S_Real
            );
        }

        copyFromMIDIoutListA(dgvMIDIoutListB);
        copyFromMIDIoutListA(dgvMIDIoutListC);
        copyFromMIDIoutListA(dgvMIDIoutListD);
        copyFromMIDIoutListA(dgvMIDIoutListE);
        copyFromMIDIoutListA(dgvMIDIoutListF);
        copyFromMIDIoutListA(dgvMIDIoutListG);
        copyFromMIDIoutListA(dgvMIDIoutListH);
        copyFromMIDIoutListA(dgvMIDIoutListI);
        copyFromMIDIoutListA(dgvMIDIoutListJ);

        // Applying settings to controls

        switch (setting.getOutputDevice().getDeviceType()) {
            case 0:
            default:
                rbWaveOut.setSelected(true);
                break;
            case 1:
                rbDirectSoundOut.setSelected(true);
                break;
            case 2:
                if (wasapiSupported) rbWasapiOut.setSelected(true);
                else rbWaveOut.setSelected(true);
                break;
            case 3:
                if (asioSupported) rbAsioOut.setSelected(true);
                else rbWaveOut.setSelected(true);
                break;
            case 4:
                // SSPCM
                rbWaveOut.setSelected(true);
                break;
            case 5:
                rbNullDevice.setSelected(true);
                break;
        }

        if (cmbWaveOutDevice.getItemCount() > 0) {
            cmbWaveOutDevice.setSelectedIndex(0);
            for (int i = 0; i < cmbWaveOutDevice.getItemCount(); i++) {
                if (cmbWaveOutDevice.getItemAt(i).equals(setting.getOutputDevice().getWaveOutDeviceName())) {
                    cmbWaveOutDevice.setSelectedIndex(i);
                }
            }
        }

        if (cmbDirectSoundDevice.getItemCount() > 0) {
            cmbDirectSoundDevice.setSelectedIndex(0);
            for (int i = 0; i < cmbDirectSoundDevice.getItemCount(); i++) {
                if (cmbDirectSoundDevice.getItemAt(i).equals(setting.getOutputDevice().getDirectSoundDeviceName())) {
                    cmbDirectSoundDevice.setSelectedIndex(i);
                }
            }
        }

        if (cmbWasapiDevice.getItemCount() > 0) {
            cmbWasapiDevice.setSelectedIndex(0);
            for (int i = 0; i < cmbWasapiDevice.getItemCount(); i++) {
                if (cmbWasapiDevice.getItemAt(i).equals(setting.getOutputDevice().getWasapiDeviceName())) {
                    cmbWasapiDevice.setSelectedIndex(i);
                }
            }
        }

        if (cmbAsioDevice.getItemCount() > 0) {
            cmbAsioDevice.setSelectedIndex(0);
            for (int i = 0; i < cmbAsioDevice.getItemCount(); i++) {
                if (cmbAsioDevice.getItemAt(i).equals(setting.getOutputDevice().getAsioDeviceName())) {
                    cmbAsioDevice.setSelectedIndex(i);
                }
            }
        }

        if (cmbMIDIIN.getItemCount() > 0) {
            cmbMIDIIN.setSelectedIndex(0);
            for (int i = 0; i < cmbMIDIIN.getItemCount(); i++) {
                if (cmbMIDIIN.getItemAt(i).equals(setting.getMidiKbd().getMidiInDeviceName())) {
                    cmbMIDIIN.setSelectedIndex(i);
                }
            }
        }

        rbShare.setSelected(setting.getOutputDevice().getWasapiShareMode());
        rbExclusive.setSelected(!setting.getOutputDevice().getWasapiShareMode());

        lblLatency.setEnabled(!rbAsioOut.isSelected());
        lblLatencyUnit.setEnabled(!rbAsioOut.isSelected());
        cmbLatency.setEnabled(!rbAsioOut.isSelected());

        if (((DefaultComboBoxModel<?>) cmbLatency.getModel()).getIndexOf(String.valueOf(setting.getOutputDevice().getLatency())) > -1) {
            cmbLatency.setSelectedItem(String.valueOf(setting.getOutputDevice().getLatency()));
        }

        if (((DefaultComboBoxModel<?>) cmbWaitTime.getModel()).getIndexOf(String.valueOf(setting.getOutputDevice().getWaitTime())) > -1) {
            cmbWaitTime.setSelectedItem(String.valueOf(setting.getOutputDevice().getWaitTime()));
        }

        if (ucSI != null) {
            setRealParam(setting.getYM2612Type()[0],
                    ucSI.rbYM2612P_Silent,
                    ucSI.rbYM2612P_Emu,
                    ucSI.rbYM2612P_SCCI,
                    ucSI.cmbYM2612P_SCCI,
                    null, null, null,
                    ucSI.rbYM2612P_EmuNuked,
                    ucSI.rbYM2612P_EmuMame);
            setRealParam(setting.getYM2612Type()[1],
                    ucSI.rbYM2612S_Silent,
                    ucSI.rbYM2612S_Emu,
                    ucSI.rbYM2612S_SCCI,
                    ucSI.cmbYM2612S_SCCI,
                    null, null, null,
                    ucSI.rbYM2612S_EmuNuked,
                    ucSI.rbYM2612S_EmuMame);

            if (setting.getYM2612Type() != null && setting.getYM2612Type().length > 0
                    && setting.getYM2612Type()[0].getRealChipInfo() != null
                    && setting.getYM2612Type()[0].getRealChipInfo().length > 0
                    && setting.getYM2612Type()[0].getRealChipInfo()[0] != null) {
                ucSI.cbSendWait.setSelected(setting.getYM2612Type()[0].getRealChipInfo()[0].getUseWait());
                ucSI.cbTwice.setSelected(setting.getYM2612Type()[0].getRealChipInfo()[0].getUseWaitBoost());
                ucSI.cbEmulationPCMOnly.setSelected(setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation());
            }

            setRealParam(setting.getYM2610Type()[0],
                    ucSI.rbYM2610BP_Silent,
                    ucSI.rbYM2610BP_Emu,
                    ucSI.rbYM2610BP_SCCI,
                    ucSI.cmbYM2610BP_SCCI,
                    ucSI.rbYM2610BEP_SCCI,
                    ucSI.cmbYM2610BEP_SCCI,
                    ucSI.cmbSPPCMP_SCCI,
                    null, null);
            setRealParam(setting.getYM2610Type()[1],
                    ucSI.rbYM2610BS_Silent,
                    ucSI.rbYM2610BS_Emu,
                    ucSI.rbYM2610BS_SCCI,
                    ucSI.cmbYM2610BS_SCCI,
                    ucSI.rbYM2610BES_SCCI,
                    ucSI.cmbYM2610BES_SCCI,
                    ucSI.cmbSPPCMS_SCCI,
                    null, null);

            setRealParam(setting.getSN76489Type()[0],
                    ucSI.rbSN76489P_Silent,
                    ucSI.rbSN76489P_Emu,
                    ucSI.rbSN76489P_SCCI,
                    ucSI.cmbSN76489P_SCCI,
                    null, null, null,
                    ucSI.rbSN76489P_Emu2, null);
            setRealParam(setting.getSN76489Type()[1],
                    ucSI.rbSN76489S_Silent,
                    ucSI.rbSN76489S_Emu,
                    ucSI.rbSN76489S_SCCI,
                    ucSI.cmbSN76489S_SCCI,
                    null, null, null,
                    ucSI.rbSN76489S_Emu2, null);

            setRealParam(setting.getYM2608Type()[0],
                    ucSI.rbYM2608P_Silent,
                    ucSI.rbYM2608P_Emu,
                    ucSI.rbYM2608P_SCCI,
                    ucSI.cmbYM2608P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM2608Type()[1],
                    ucSI.rbYM2608S_Silent,
                    ucSI.rbYM2608S_Emu,
                    ucSI.rbYM2608S_SCCI,
                    ucSI.cmbYM2608S_SCCI, null, null, null, null, null);

            setRealParam(setting.getYM2151Type()[0],
                    ucSI.rbYM2151P_Silent,
                    ucSI.rbYM2151P_Emu,
                    ucSI.rbYM2151P_SCCI,
                    ucSI.cmbYM2151P_SCCI,
                    null, null, null,
                    ucSI.rbYM2151P_EmuMame,
                    ucSI.rbYM2151P_EmuX68Sound);
            setRealParam(setting.getYM2151Type()[1],
                    ucSI.rbYM2151S_Silent,
                    ucSI.rbYM2151S_Emu,
                    ucSI.rbYM2151S_SCCI,
                    ucSI.cmbYM2151S_SCCI,
                    null, null, null,
                    ucSI.rbYM2151S_EmuMame,
                    ucSI.rbYM2151S_EmuX68Sound);

            setRealParam(setting.getYM2203Type()[0],
                    ucSI.rbYM2203P_Silent,
                    ucSI.rbYM2203P_Emu,
                    ucSI.rbYM2203P_SCCI,
                    ucSI.cmbYM2203P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM2203Type()[1],
                    ucSI.rbYM2203S_Silent,
                    ucSI.rbYM2203S_Emu,
                    ucSI.rbYM2203S_SCCI,
                    ucSI.cmbYM2203S_SCCI, null, null, null, null, null);

            setRealParam(setting.getAY8910Type()[0],
                    ucSI.rbAY8910P_Silent,
                    ucSI.rbAY8910P_Emu,
                    ucSI.rbAY8910P_Real,
                    ucSI.cmbAY8910P_Real,
                    null, null, null,
                    ucSI.rbAY8910P_Emu2, null);

            setRealParam(setting.getAY8910Type()[1],
                    ucSI.rbAY8910S_Silent,
                    ucSI.rbAY8910S_Emu,
                    ucSI.rbAY8910S_Real,
                    ucSI.cmbAY8910S_Real,
                    null, null, null,
                    ucSI.rbAY8910S_Emu2, null);

            setRealParam(setting.getK051649Type()[0],
                    ucSI.rbK051649P_Silent,
                    ucSI.rbK051649P_Emu,
                    ucSI.rbK051649P_Real,
                    ucSI.cmbK051649P_Real, null, null, null, null, null);
            setRealParam(setting.getK051649Type()[1],
                    ucSI.rbK051649S_Silent,
                    ucSI.rbK051649S_Emu,
                    ucSI.rbK051649S_Real,
                    ucSI.cmbK051649S_Real, null, null, null, null, null);

            setRealParam(setting.getYM2413Type()[0],
                    ucSI.rbYM2413P_Silent,
                    ucSI.rbYM2413P_Emu,
                    ucSI.rbYM2413P_Real,
                    ucSI.cmbYM2413P_Real, null, null, null, null, null);
            setRealParam(setting.getYM2413Type()[1],
                    ucSI.rbYM2413S_Silent,
                    ucSI.rbYM2413S_Emu,
                    ucSI.rbYM2413S_Real,
                    ucSI.cmbYM2413S_Real, null, null, null, null, null);

            setRealParam(setting.getYM3526Type()[0],
                    ucSI.rbYM3526P_Silent,
                    ucSI.rbYM3526P_Emu,
                    ucSI.rbYM3526P_SCCI,
                    ucSI.cmbYM3526P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM3526Type()[1],
                    ucSI.rbYM3526S_Silent,
                    ucSI.rbYM3526S_Emu,
                    ucSI.rbYM3526S_SCCI,
                    ucSI.cmbYM3526S_SCCI, null, null, null, null, null);

            setRealParam(setting.getYM3812Type()[0],
                    ucSI.rbYM3812P_Silent,
                    ucSI.rbYM3812P_Emu,
                    ucSI.rbYM3812P_SCCI,
                    ucSI.cmbYM3812P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM3812Type()[1],
                    ucSI.rbYM3812S_Silent,
                    ucSI.rbYM3812S_Emu,
                    ucSI.rbYM3812S_SCCI,
                    ucSI.cmbYM3812S_SCCI, null, null, null, null, null);

            setRealParam(setting.getYMF262Type()[0],
                    ucSI.rbYMF262P_Silent,
                    ucSI.rbYMF262P_Emu,
                    ucSI.rbYMF262P_SCCI,
                    ucSI.cmbYMF262P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYMF262Type()[1],
                    ucSI.rbYMF262S_Silent,
                    ucSI.rbYMF262S_Emu,
                    ucSI.rbYMF262S_SCCI,
                    ucSI.cmbYMF262S_SCCI, null, null, null, null, null);

            setRealParam(setting.getC140Type()[0],
                    ucSI.rbC140P_Silent,
                    ucSI.rbC140P_Emu,
                    ucSI.rbC140P_Real,
                    ucSI.cmbC140P_SCCI, null, null, null, null, null);
            setRealParam(setting.getC140Type()[1],
                    ucSI.rbC140S_Silent,
                    ucSI.rbC140S_Emu,
                    ucSI.rbC140S_SCCI,
                    ucSI.cmbC140S_SCCI, null, null, null, null, null);

            setRealParam(setting.getSEGAPCMType()[0],
                    ucSI.rbSEGAPCMP_Silent,
                    ucSI.rbSEGAPCMP_Emu,
                    ucSI.rbSEGAPCMP_SCCI,
                    ucSI.cmbSEGAPCMP_SCCI, null, null, null, null, null);
            setRealParam(setting.getSEGAPCMType()[1],
                    ucSI.rbSEGAPCMS_Silent,
                    ucSI.rbSEGAPCMS_Emu,
                    ucSI.rbSEGAPCMS_SCCI,
                    ucSI.cmbSEGAPCMS_SCCI, null, null, null, null, null);
        }

        for (int i = 0; i < cmbSampleRate.getItemCount(); i++) {
            if (cmbSampleRate.getItemAt(i).equals(String.valueOf(setting.getOutputDevice().getSampleRate()))) {
                cmbSampleRate.setSelectedIndex(i);
                break;
            }
        }

        cbUseMIDIKeyboard.setSelected(setting.getMidiKbd().getUseMIDIKeyboard());

        cbFM1.setSelected(setting.getMidiKbd().getUseChannel()[0]);
        cbFM2.setSelected(setting.getMidiKbd().getUseChannel()[1]);
        cbFM3.setSelected(setting.getMidiKbd().getUseChannel()[2]);
        cbFM4.setSelected(setting.getMidiKbd().getUseChannel()[3]);
        cbFM5.setSelected(setting.getMidiKbd().getUseChannel()[4]);
        cbFM6.setSelected(setting.getMidiKbd().getUseChannel()[5]);

        rbMONO.setSelected(setting.getMidiKbd().isMono());
        rbPOLY.setSelected(!setting.getMidiKbd().isMono());

        rbFM1.setSelected(setting.getMidiKbd().getUseMonoChannel() == 0);
        rbFM2.setSelected(setting.getMidiKbd().getUseMonoChannel() == 1);
        rbFM3.setSelected(setting.getMidiKbd().getUseMonoChannel() == 2);
        rbFM4.setSelected(setting.getMidiKbd().getUseMonoChannel() == 3);
        rbFM5.setSelected(setting.getMidiKbd().getUseMonoChannel() == 4);
        rbFM6.setSelected(setting.getMidiKbd().getUseMonoChannel() == 5);

        tbLatencyEmu.setText(String.valueOf(setting.getLatencyEmulation()));
        tbLatencySCCI.setText(String.valueOf(setting.getLatencySCCI()));

        cbDispFrameCounter.setSelected(setting.getDebug_DispFrameCounter());
        cbHiyorimiMode.setSelected(setting.getHiyorimiMode());
        tbSCCbaseAddress.setText("%04X".formatted(setting.getDebug_SCCbaseAddress()));

        cbUseLoopTimes.setSelected(setting.getOther().getUseLoopTimes());
        tbLoopTimes.setEnabled(cbUseLoopTimes.isSelected());
        lblLoopTimes.setEnabled(cbUseLoopTimes.isSelected());
        tbLoopTimes.setText(String.valueOf(setting.getOther().getLoopTimes()));
        cbUseGetInst.setSelected(setting.getOther().getUseGetInst());
        cbUseGetInst_CheckedChanged(null);
        tbDataPath.setText(setting.getOther().getDefaultDataPath());
        tbSearchPath.setText(setting.getFileSearchPathList());
        cmbInstFormat.setSelectedIndex(setting.getOther().getInstFormat().ordinal());
        tbScreenFrameRate.setText(String.valueOf(setting.getOther().getScreenFrameRate()));
        cbAutoOpen.setSelected(setting.getOther().getAutoOpen());
        cbDumpSwitch.setSelected(setting.getOther().getDumpSwitch());
        gbDump.setEnabled(cbDumpSwitch.isSelected());
        tbDumpPath.setText(setting.getOther().getDumpPath());
        cbWavSwitch.setSelected(setting.getOther().getWavSwitch());
        gbWav.setEnabled(cbWavSwitch.isSelected());
        tbWavPath.setText(setting.getOther().getWavPath());
        tbTextExt.setText(setting.getOther().getTextExt());
        tbMMLExt.setText(setting.getOther().getMMLExt());
        cbAutoOpenText.setSelected(setting.getOther().getAutoOpenText());
        cbAutoOpenMML.setSelected(setting.getOther().getAutoOpenMML());
        cbAutoOpenImg.setSelected(setting.getOther().getAutoOpenImg());
        tbImageExt.setText(setting.getOther().getImageExt());
        cbInitAlways.setSelected(setting.getOther().getInitAlways());
        cbEmptyPlayList.setSelected(setting.getOther().getEmptyPlayList());

        cbUseMIDIExport.setSelected(setting.getMidiExport().getUseMIDIExport());
        gbMIDIExport.setEnabled(cbUseMIDIExport.isSelected());
        tbMIDIOutputPath.setText(setting.getMidiExport().getExportPath());
        cbMIDIUseVOPM.setSelected(setting.getMidiExport().getUseVOPMex());
        cbMIDIKeyOnFnum.setSelected(setting.getMidiExport().getKeyOnFnum());
        cbMIDIYM2151.setSelected(setting.getMidiExport().getUseYM2151Export());
        cbMIDIYM2612.setSelected(setting.getMidiExport().getUseYM2612Export());

        tbCCChCopy.setText(setting.getMidiKbd().getMidiCtrl_CopyToneFromYM2612Ch1() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_CopyToneFromYM2612Ch1()));
        tbCCCopyLog.setText(setting.getMidiKbd().getMidiCtrl_CopySelecttingLogToClipbrd() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_CopySelecttingLogToClipbrd()));
        tbCCDelLog.setText(setting.getMidiKbd().getMidiCtrl_DelOneLog() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_DelOneLog()));
        tbCCFadeout.setText(setting.getMidiKbd().getMidiCtrl_Fadeout() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Fadeout()));
        tbCCFast.setText(setting.getMidiKbd().getMidiCtrl_Fast() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Fast()));
        tbCCNext.setText(setting.getMidiKbd().getMidiCtrl_Next() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Next()));
        tbCCPause.setText(setting.getMidiKbd().getMidiCtrl_Pause() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Pause()));
        tbCCPlay.setText(setting.getMidiKbd().getMidiCtrl_Play() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Play()));
        tbCCPrevious.setText(setting.getMidiKbd().getMidiCtrl_Previous() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Previous()));
        tbCCSlow.setText(setting.getMidiKbd().getMidiCtrlSlow() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrlSlow()));
        tbCCStop.setText(setting.getMidiKbd().getMidiCtrl_Stop() == -1 ? "" : String.valueOf(setting.getMidiKbd().getMidiCtrl_Stop()));


        if (setting.getMidiOut().getMidiOutInfos() != null && !setting.getMidiOut().getMidiOutInfos().isEmpty()) {
            for (int i = 0; i < setting.getMidiOut().getMidiOutInfos().size(); i++) {
                DefaultTableModel m = (DefaultTableModel) dgv[i].getModel();
                m.setRowCount(0);
                Set<Integer> midioutNotFound = new HashSet<>();
                if (setting.getMidiOut().getMidiOutInfos().get(i) != null && setting.getMidiOut().getMidiOutInfos().get(i).length > 0) {
                    for (int j = 0; j < setting.getMidiOut().getMidiOutInfos().get(i).length; j++) {
                        MidiOutInfo moi = setting.getMidiOut().getMidiOutInfos().get(i)[j];
                        int found = -999;
                        int k = 0;
                        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                            try {
                                MidiDevice device = MidiSystem.getMidiDevice(info);
                                if (device.getMaxReceivers() == 0) {
                                    continue;
                                }
                                if (moi.name.equals(device.getDeviceInfo().getName())) {
                                    midioutNotFound.add(k);
                                    found = k++;
                                    break;
                                }
                            } catch (MidiUnavailableException e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }

                        moi.id = found;

                        String stype = switch (moi.type) {
                            case 1 -> "XG";
                            case 2 -> "GS";
                            case 3 -> "LA";
                            case 4 -> "GS(SC-55_1)";
                            case 5 -> "GS(SC-55_2)";
                            default -> "GM";
                        };

                        String sbeforeSend = switch (moi.beforeSendType) {
                            case 1 -> "GM Reset";
                            case 2 -> "XG Reset";
                            case 3 -> "GS Reset";
                            case 4 -> "Custom";
                            default -> "None";
                        };

                        m.addRow(new Object[] {
                                moi.id,
                                moi.isVST,
                                moi.fileName,
                                moi.name,
                                stype,
                                sbeforeSend,
                                moi.isVST ? moi.vendor : (moi.manufacturer != -1 ? String.valueOf(moi.manufacturer) : "Unknown")
                        });
                    }
                }
            }
        }

        tbBeforeSend_GMReset.setText(setting.getMidiOut().getGMReset());
        tbBeforeSend_XGReset.setText(setting.getMidiOut().getXGReset());
        tbBeforeSend_GSReset.setText(setting.getMidiOut().getGSReset());
        tbBeforeSend_Custom.setText(setting.getMidiOut().getCustom());

        DefaultTableModel m = (DefaultTableModel) dgvMIDIoutPallet.getModel();
        int i = 0;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxReceivers() == 0) {
                    continue;
                }
                m.addRow(new Object[] {i++, device.getDeviceInfo().getName(), device.getDeviceInfo().getVendor()});
            } catch (MidiUnavailableException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }

        trkbNSFHPF.setValue(setting.getNsf().getHPF());
        trkbNSFLPF.setValue(setting.getNsf().getLPF());
        cbNFSNes_UnmuteOnReset.setSelected(setting.getNsf().getNESUnmuteOnReset());
        cbNFSNes_NonLinearMixer.setSelected(setting.getNsf().getNESNonLinearMixer());
        cbNFSNes_PhaseRefresh.setSelected(setting.getNsf().getNESPhaseRefresh());
        cbNFSNes_DutySwap.setSelected(setting.getNsf().getNESDutySwap());

        tbNSFFds_LPF.setText(String.valueOf(setting.getNsf().getFDSLpf()));
        cbNFSFds_4085Reset.setSelected(setting.getNsf().getFDS4085Reset());
        cbNSFFDSWriteDisable8000.setSelected(setting.getNsf().getFDSWriteDisable8000());

        cbNSFDmc_UnmuteOnReset.setSelected(setting.getNsf().getDMCUnmuteOnReset());
        cbNSFDmc_NonLinearMixer.setSelected(setting.getNsf().getDMCNonLinearMixer());
        cbNSFDmc_Enable4011.setSelected(setting.getNsf().getDMCEnable4011());
        cbNSFDmc_EnablePNoise.setSelected(setting.getNsf().getDMCEnablePnoise());
        cbNSFDmc_DPCMAntiClick.setSelected(setting.getNsf().getDMCDPCMAntiClick());
        cbNSFDmc_RandomizeNoise.setSelected(setting.getNsf().getDMCRandomizeNoise());
        cbNSFDmc_TriMute.setSelected(setting.getNsf().getDMCTRImute());
        cbNSFDmc_RandomizeTri.setSelected(setting.getNsf().getDMCRandomizeTRI());
        cbNSFDmc_DPCMReverse.setSelected(setting.getNsf().getDMCDPCMReverse());

        cbNSFMmc5_NonLinearMixer.setSelected(setting.getNsf().getMMC5NonLinearMixer());
        cbNSFMmc5_PhaseRefresh.setSelected(setting.getNsf().getMMC5PhaseRefresh());

        cbNSFN160_Serial.setSelected(setting.getNsf().getN160Serial());

        tbSIDKernal.setText(setting.getSid().romKernalPath);
        tbSIDBasic.setText(setting.getSid().romBasicPath);
        tbSIDCharacter.setText(setting.getSid().romCharacterPath);
        switch (setting.getSid().quality) {
        case 0:
            rdSIDQ1.setSelected(true);
            break;
        case 1:
            rdSIDQ2.setSelected(true);
            break;
        case 2:
            rdSIDQ3.setSelected(true);
            break;
        case 3:
            rdSIDQ4.setSelected(true);
            break;
        }
        tbSIDOutputBufferSize.setText(String.valueOf(setting.getSid().outputBufferSize));

        rbSIDC64Model_PAL.setSelected((setting.getSid().c64model == 0));
        rbSIDC64Model_NTSC.setSelected((setting.getSid().c64model == 1));
        rbSIDC64Model_OLDNTSC.setSelected((setting.getSid().c64model == 2));
        rbSIDC64Model_DREAN.setSelected((setting.getSid().c64model == 3));
        cbSIDC64Model_Force.setSelected(setting.getSid().c64modelForce);

        rbSIDModel_6581.setSelected((setting.getSid().sidModel == 0));
        rbSIDModel_8580.setSelected((setting.getSid().sidModel == 1));
        cbSIDModel_Force.setSelected(setting.getSid().sidmodelForce);

        switch (setting.getNukedOPN2().emuType) {
        case 0:
            rbNukedOPN2OptionDiscrete.setSelected(true);
            break;
        case 1:
            rbNukedOPN2OptionASIC.setSelected(true);
            break;
        case 2:
            rbNukedOPN2OptionYM2612.setSelected(true);
            break;
        case 3:
            rbNukedOPN2OptionYM2612u.setSelected(true);
            break;
        case 4:
            rbNukedOPN2OptionASIClp.setSelected(true);
            break;
        }

        cbGensDACHPF.setSelected(setting.getNukedOPN2().gensDACHPF);
        cbGensSSGEG.setSelected(setting.getNukedOPN2().gensSSGEG);

        cbAutoBalanceUseThis.setSelected(setting.getAutoBalance().getUseThis());
        rbAutoBalanceLoadSongBalance.setSelected(setting.getAutoBalance().getLoadSongBalance());
        rbAutoBalanceNotLoadSongBalance.setSelected(!setting.getAutoBalance().getLoadSongBalance());
        rbAutoBalanceLoadDriverBalance.setSelected(setting.getAutoBalance().getLoadDriverBalance());
        rbAutoBalanceNotLoadDriverBalance.setSelected(!setting.getAutoBalance().getLoadDriverBalance());
        rbAutoBalanceSaveSongBalance.setSelected(setting.getAutoBalance().getSaveSongBalance());
        rbAutoBalanceNotSaveSongBalance.setSelected(!setting.getAutoBalance().getSaveSongBalance());
        rbAutoBalanceSamePositionAsSongData.setSelected(setting.getAutoBalance().getSamePositionAsSongData());
        rbAutoBalanceNotSamePositionAsSongData.setSelected(!setting.getAutoBalance().getSamePositionAsSongData());

        cbUseKeyBoardHook.setSelected(setting.getKeyboardHook().getUseKeyBoardHook());
        gbUseKeyBoardHook.setEnabled(setting.getKeyboardHook().getUseKeyBoardHook());

        cbStopShift.setSelected(setting.getKeyboardHook().getStop().getShift());
        cbStopCtrl.setSelected(setting.getKeyboardHook().getStop().getCtrl());
        cbStopWin.setSelected(setting.getKeyboardHook().getStop().getWin());
        cbStopAlt.setSelected(setting.getKeyboardHook().getStop().getAlt());
        lblStopKey.setText(setting.getKeyboardHook().getStop().getKey());
        btStopClr.setEnabled((!lblStopKey.getText().equals("(None)") && (lblStopKey.getText() != null && !lblStopKey.getText().isEmpty())));

        cbPauseShift.setSelected(setting.getKeyboardHook().getPause().getShift());
        cbPauseCtrl.setSelected(setting.getKeyboardHook().getPause().getCtrl());
        cbPauseWin.setSelected(setting.getKeyboardHook().getPause().getWin());
        cbPauseAlt.setSelected(setting.getKeyboardHook().getPause().getAlt());
        lblPauseKey.setText(setting.getKeyboardHook().getPause().getKey());
        btPauseClr.setEnabled((!lblPauseKey.getText().equals("(None)") && (lblPauseKey.getText() != null && !lblPauseKey.getText().isEmpty())));

        cbFadeoutShift.setSelected(setting.getKeyboardHook().getFadeout().getShift());
        cbFadeoutCtrl.setSelected(setting.getKeyboardHook().getFadeout().getCtrl());
        cbFadeoutWin.setSelected(setting.getKeyboardHook().getFadeout().getWin());
        cbFadeoutAlt.setSelected(setting.getKeyboardHook().getFadeout().getAlt());
        lblFadeoutKey.setText(setting.getKeyboardHook().getFadeout().getKey());
        btFadeoutClr.setEnabled((!lblFadeoutKey.getText().equals("(None)") && (lblFadeoutKey.getText() != null && !lblFadeoutKey.getText().isEmpty())));

        cbPrevShift.setSelected(setting.getKeyboardHook().getPrev().getShift());
        cbPrevCtrl.setSelected(setting.getKeyboardHook().getPrev().getCtrl());
        cbPrevWin.setSelected(setting.getKeyboardHook().getPrev().getWin());
        cbPrevAlt.setSelected(setting.getKeyboardHook().getPrev().getAlt());
        lblPrevKey.setText(setting.getKeyboardHook().getPrev().getKey());
        btPrevClr.setEnabled((!lblPrevKey.getText().equals("(None)") && (lblPrevKey.getText() != null && !lblPrevKey.getText().isEmpty())));

        cbSlowShift.setSelected(setting.getKeyboardHook().getSlow().getShift());
        cbSlowCtrl.setSelected(setting.getKeyboardHook().getSlow().getCtrl());
        cbSlowWin.setSelected(setting.getKeyboardHook().getSlow().getWin());
        cbSlowAlt.setSelected(setting.getKeyboardHook().getSlow().getAlt());
        lblSlowKey.setText(setting.getKeyboardHook().getSlow().getKey());
        btSlowClr.setEnabled((!lblSlowKey.getText().equals("(None)") && (lblSlowKey.getText() != null && !lblSlowKey.getText().isEmpty())));

        cbPlayShift.setSelected(setting.getKeyboardHook().getPlay().getShift());
        cbPlayCtrl.setSelected(setting.getKeyboardHook().getPlay().getCtrl());
        cbPlayWin.setSelected(setting.getKeyboardHook().getPlay().getWin());
        cbPlayAlt.setSelected(setting.getKeyboardHook().getPlay().getAlt());
        lblPlayKey.setText(setting.getKeyboardHook().getPlay().getKey());
        btPlayClr.setEnabled((!lblPlayKey.getText().equals("(None)") && (lblPlayKey.getText() != null && !lblPlayKey.getText().isEmpty())));

        cbFastShift.setSelected(setting.getKeyboardHook().getFast().getShift());
        cbFastCtrl.setSelected(setting.getKeyboardHook().getFast().getCtrl());
        cbFastWin.setSelected(setting.getKeyboardHook().getFast().getWin());
        cbFastAlt.setSelected(setting.getKeyboardHook().getFast().getAlt());
        lblFastKey.setText(setting.getKeyboardHook().getFast().getKey());
        btFastClr.setEnabled((!lblFastKey.getText().equals("(None)") && (lblFastKey.getText() != null && !lblFastKey.getText().isEmpty())));

        cbNextShift.setSelected(setting.getKeyboardHook().getNext().getShift());
        cbNextCtrl.setSelected(setting.getKeyboardHook().getNext().getCtrl());
        cbNextWin.setSelected(setting.getKeyboardHook().getNext().getWin());
        cbNextAlt.setSelected(setting.getKeyboardHook().getNext().getAlt());
        lblNextKey.setText(setting.getKeyboardHook().getNext().getKey());
        btNextClr.setEnabled((!lblNextKey.getText().equals("(None)") && (lblNextKey.getText() != null && !lblNextKey.getText().isEmpty())));

        cbExALL.setSelected(setting.getOther().getExAll());
        cbNonRenderingForPause.setSelected(setting.getOther().getNonRenderingForPause());


        tbPMDCompilerArguments.setText(setting.getPmd().compilerArguments);
        rbPMDAuto.setSelected(setting.getPmd().isAuto);
        rbPMDManual.setSelected(!setting.getPmd().isAuto);
        rbPMDNrmB.setSelected(setting.getPmd().soundBoard == 0);
        rbPMDSpbB.setSelected(setting.getPmd().soundBoard == 1);
        rbPMD86B.setSelected(setting.getPmd().soundBoard == 2);
        cbPMDSetManualVolume.setSelected(setting.getPmd().setManualVolume);
        cbPMDUsePPSDRV.setSelected(setting.getPmd().usePPSDRV);
        cbPMDUsePPZ8.setSelected(setting.getPmd().usePPZ8);
        tbPMDDriverArguments.setText(setting.getPmd().driverArguments);
        rbPMDUsePPSDRVFreqDefault.setSelected(setting.getPmd().usePPSDRVUseInterfaceDefaultFreq);
        rbPMDUsePPSDRVManualFreq.setSelected(!setting.getPmd().usePPSDRVUseInterfaceDefaultFreq);
        tbPMDPPSDRVFreq.setText(String.valueOf(setting.getPmd().ppsDrvManualFreq));
        tbPMDPPSDRVManualWait.setText(String.valueOf(setting.getPmd().ppsDrvManualWait));
        tbPMDVolumeFM.setText(String.valueOf(setting.getPmd().volumeFM));
        tbPMDVolumeSSG.setText(String.valueOf(setting.getPmd().volumeSSG));
        tbPMDVolumeRhythm.setText(String.valueOf(setting.getPmd().volumeRhythm));
        tbPMDVolumeAdpcm.setText(String.valueOf(setting.getPmd().volumeAdpcm));
        tbPMDVolumeGIMICSSG.setText(String.valueOf(setting.getPmd().volumeGIMICSSG));

        rbPMDManual_CheckedChanged(null);
        cbPMDSetManualVolume_CheckedChanged(null);
        cbPMDUsePPSDRV_CheckedChanged(null);
        rbPMDUsePPSDRVManualFreq_CheckedChanged(null);
    }

    private static void setRealCombo(EnmRealChipType realType, JComboBox<String> cmbP, JCheckBox rbP, JComboBox<String> cmbS, JCheckBox rbS) {

        if (rbP != null) rbP.setEnabled(false);
        cmbP.setEnabled(false);

        if (rbS != null) rbS.setEnabled(false);
        cmbS.setEnabled(false);

        // the real chip list hangs off the plugin, and there is none until a song is loaded, so
        // settings opened before that offers no real chip — same as having none attached
        if (Audio.getInstance().plugin == null) return;

        List<ChipType2> lstChip = Audio.getInstance().plugin.chipRegister.plugin(RealChipPlugin.class).getRealChipList(realType);
        if (lstChip == null || lstChip.isEmpty()) return;

        for (ChipType2 ct : lstChip) {
            if (ct == null || ct.getRealChipInfo() == null || ct.getRealChipInfo().length == 0 || ct.getRealChipInfo()[0] == null)
                continue;

            cmbP.addItem("(%s:%s:%s:%s)%s".formatted(
                    ct.getRealChipInfo()[0].getInterfaceName(),
                    ct.getRealChipInfo()[0].getSoundLocation(),
                    ct.getRealChipInfo()[0].getBusID(),
                    ct.getRealChipInfo()[0].getSoundChip(),
                    ct.getRealChipInfo()[0].getChipName()));

            cmbS.addItem("(%s:%s:%s:%s)%s".formatted(
                    ct.getRealChipInfo()[0].getInterfaceName(),
                    ct.getRealChipInfo()[0].getSoundLocation(),
                    ct.getRealChipInfo()[0].getBusID(),
                    ct.getRealChipInfo()[0].getSoundChip(),
                    ct.getRealChipInfo()[0].getChipName()));
        }

        if (cmbP.getItemCount() > 0) {
            cmbP.setSelectedIndex(0);
            if (rbP != null) rbP.setEnabled(true);
            cmbP.setEnabled(true);
        }

        if (cmbS.getItemCount() > 0) {
            cmbS.setSelectedIndex(0);
            if (rbS != null) rbS.setEnabled(true);
            cmbS.setEnabled(true);
        }
    }

    private static void setRealParam(ChipType2 chipType2,
                                     JCheckBox rbSilent,
                                     JCheckBox rbEmu,
                                     JCheckBox rbReal,
                                     JComboBox<String> cmbP,
                                     JCheckBox rbReal2 /* = null */,
                                     JComboBox<String> cmbP2A /* = null */,
                                     JComboBox<String> cmbP2B /* = null */,
                                     JCheckBox rbEmu2 /* = null */,
                                     JCheckBox rbEmu3/* = null */) {
        String n = "";

        if (chipType2.getRealChipInfo() != null && chipType2.getRealChipInfo().length > 0 && chipType2.getRealChipInfo()[0] != null) {
            n = "(%s:%s:%s:%s)".formatted(
                    chipType2.getRealChipInfo()[0].getInterfaceName(),
                    chipType2.getRealChipInfo()[0].getSoundLocation(),
                    chipType2.getRealChipInfo()[0].getBusID(),
                    chipType2.getRealChipInfo()[0].getSoundChip());
        }

        if (cmbP.getItemCount() > 0) {
            for (int i = 0; i < cmbP.getItemCount(); i++) {
                if (!cmbP.getItemAt(i).contains(n)) continue;
                cmbP.setSelectedItem(i);

                break;
            }
        }

        if (cmbP2A != null) {
            if (chipType2.getRealChipInfo() != null && chipType2.getRealChipInfo().length > 1 && chipType2.getRealChipInfo()[1] != null) {
                n = "(%s:%s:%s:%s)".formatted(
                        chipType2.getRealChipInfo()[1].getInterfaceName(),
                        chipType2.getRealChipInfo()[1].getSoundLocation(),
                        chipType2.getRealChipInfo()[1].getBusID(),
                        chipType2.getRealChipInfo()[1].getSoundChip());
            }

            if (cmbP2A.getItemCount() > 0) {
                for (int i = 0; i < cmbP2A.getItemCount(); i++) {
                    if (!cmbP2A.getItemAt(i).contains(n)) continue;
                    cmbP2A.setSelectedItem(i);

                    break;
                }
            }
        }

        if (cmbP2B != null) {
            if (chipType2.getRealChipInfo() != null && chipType2.getRealChipInfo().length > 2 && chipType2.getRealChipInfo()[2] != null) {
                n = "(%s:%s:%s:%s)".formatted(
                        chipType2.getRealChipInfo()[2].getInterfaceName(),
                        chipType2.getRealChipInfo()[2].getSoundLocation(),
                        chipType2.getRealChipInfo()[2].getBusID(),
                        chipType2.getRealChipInfo()[2].getSoundChip());
            }

            if (cmbP2B.getItemCount() > 0) {
                for (int i = 0; i < cmbP2B.getItemCount(); i++) {
                    if (!cmbP2B.getItemAt(i).contains(n)) continue;
                    cmbP2B.setSelectedItem(i);

                    break;
                }
            }
        }

        if (rbEmu2 != null && chipType2.getUseEmu().length > 1 && chipType2.getUseEmu()[1]) {
            rbEmu2.setSelected(true);
            return;
        }

        if (rbEmu3 != null && chipType2.getUseEmu().length > 2 && chipType2.getUseEmu()[2]) {
            rbEmu3.setSelected(true);
            return;
        }

        if ((chipType2.getUseReal().length > 0 && !chipType2.getUseReal()[0])
                && (chipType2.getUseReal().length > 1 && !chipType2.getUseReal()[1]))
        // rbSCCI2==null) || (!chipType2.UseScci && rbSCCI2 != null && !chipType2.UseScci2))
        {
            if (chipType2.getUseEmu()[0])
                rbEmu.setSelected(true);
            else
                rbSilent.setSelected(true);

            return;
        }

        if (((chipType2.getUseReal().length > 0 && chipType2.getUseReal()[0]) && !cmbP.isEnabled())
                || ((chipType2.getUseReal().length > 1 && chipType2.getUseReal()[1])
                && !cmbP2A.isEnabled()
                && !cmbP2B.isEnabled())
        ) {
            rbEmu.setSelected(true);

            return;
        }

        if (chipType2.getUseReal().length > 0 && chipType2.getUseReal()[0]) {
            rbReal.setSelected(true);
            return;
        }

        if (rbReal2 != null) rbReal2.setSelected(true);
    }

    private void copyFromMIDIoutListA(JTable dgv) {

        dgv.setColumnModel(new DefaultTableColumnModel());

        Enumeration<TableColumn> columns = dgvMIDIoutListA.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            dgv.getColumnModel().addColumn(columns.nextElement());
        }
    }

    private static void btnASIOControlPanel_Click(ActionEvent ev) {
        try {
//            try (AsioOut asio = new AsioOut(cmbAsioDevice.getSelectedItem().toString())) {
//                asio.ShowControlPanel();
//            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private void btnOK_Click(ActionEvent ev) {
        if (!checkSetting()) return;

        int i;

//#region Output

        setting.getOutputDevice().setDeviceType(Common.DEV_WaveOut);
        if (rbWaveOut.isSelected()) setting.getOutputDevice().setDeviceType(Common.DEV_WaveOut);
        if (rbDirectSoundOut.isSelected()) setting.getOutputDevice().setDeviceType(Common.DEV_DirectSound);
        if (rbWasapiOut.isSelected()) setting.getOutputDevice().setDeviceType(Common.DEV_WasapiOut);
        if (rbAsioOut.isSelected()) setting.getOutputDevice().setDeviceType(Common.DEV_AsioOut);
        if (rbSPPCM.isSelected()) setting.getOutputDevice().setDeviceType(Common.DEV_SPPCM);
        if (rbNullDevice.isSelected()) setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        setting.getOutputDevice().setWaveOutDeviceName(cmbWaveOutDevice.getSelectedItem() != null ? cmbWaveOutDevice.getSelectedItem().toString() : "");
        setting.getOutputDevice().setDirectSoundDeviceName(cmbDirectSoundDevice.getSelectedItem() != null ? cmbDirectSoundDevice.getSelectedItem().toString() : "");
        setting.getOutputDevice().setWasapiDeviceName(cmbWasapiDevice.getSelectedItem() != null ? cmbWasapiDevice.getSelectedItem().toString() : "");
        setting.getOutputDevice().setAsioDeviceName(cmbAsioDevice.getSelectedItem() != null ? cmbAsioDevice.getSelectedItem().toString() : "");

        setting.getOutputDevice().setWasapiShareMode(rbShare.isSelected());
        setting.getOutputDevice().setLatency(Integer.parseInt(cmbLatency.getSelectedItem().toString()));
        setting.getOutputDevice().setWaitTime(Integer.parseInt(cmbWaitTime.getSelectedItem().toString()));
        setting.getOutputDevice().setSampleRate(Integer.parseInt(cmbSampleRate.getSelectedItem().toString()));

//#endregion

//#region Sound

        setting.setUnuseRealChip(cbUnuseRealChip.isSelected());
        setting.getYM2612Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2612Type()[0],
                ucSI.rbYM2612P_SCCI,
                ucSI.cmbYM2612P_SCCI,
                ucSI.rbYM2612P_Emu,
                ucSI.rbYM2612P_EmuNuked,
                ucSI.rbYM2612P_EmuMame,
                null,
                null,
                null
        );

        setting.getYM2612Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2612Type()[1],
                ucSI.rbYM2612S_SCCI,
                ucSI.cmbYM2612S_SCCI,
                ucSI.rbYM2612S_Emu,
                ucSI.rbYM2612S_EmuNuked,
                ucSI.rbYM2612S_EmuMame,
                null,
                null,
                null
        );
        if (setting.getYM2612Type()[0].getRealChipInfo() == null) {
            setting.getYM2612Type()[0].setRealChipInfo(new ChipType2.RealChipInfo[] {new ChipType2.RealChipInfo()});
        }
        setting.getYM2612Type()[0].getRealChipInfo()[0].setUseWait(ucSI.cbSendWait.isSelected());
        setting.getYM2612Type()[0].getRealChipInfo()[0].setUseWaitBoost(ucSI.cbTwice.isSelected());
        setting.getYM2612Type()[0].getRealChipInfo()[0].setOnlyPCMEmulation(ucSI.cbEmulationPCMOnly.isSelected());

        //setting.YM2612Type.LatencyForEmulation = 0;
        //if (Integer.parseInt(tbYM2612EmuDelay.getText(),  i)) {
        //    setting.YM2612Type.LatencyForEmulation = Math.max(Math.min(i, 999), 0);
        //}
        //setting.YM2612Type.LatencyForScci = 0;
        //if (Integer.parseInt(tbYM2612ScciDelay.getText(),  i)) {
        //    setting.YM2612Type.LatencyForScci = Math.max(Math.min(i, 999), 0);
        //}

        setting.setSN76489Type(new ChipType2[2]);
        setting.getSN76489Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getSN76489Type()[0],
                ucSI.rbSN76489P_SCCI,
                ucSI.cmbSN76489P_SCCI,
                ucSI.rbSN76489P_Emu,
                ucSI.rbSN76489P_Emu2,
                null,
                null,
                null,
                null
        );

        setting.getSN76489Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getSN76489Type()[1],
                ucSI.rbSN76489S_SCCI,
                ucSI.cmbSN76489S_SCCI,
                ucSI.rbSN76489S_Emu,
                ucSI.rbSN76489S_Emu2,
                null,
                null,
                null,
                null
        );

        //setting.SN76489Type.LatencyForEmulation = 0;
        //if (Integer.parseInt(tbSN76489EmuDelay.getText(),  i)) {
        //    setting.SN76489Type.LatencyForEmulation = Math.max(Math.min(i, 999), 0);
        //}
        //setting.SN76489Type.LatencyForScci = 0;
        //if (Integer.parseInt(tbSN76489ScciDelay.getText(),  i)) {
        //    setting.SN76489Type.LatencyForScci = Math.max(Math.min(i, 999), 0);
        //}

        setting.setYM2608Type(new ChipType2[2]);
        setting.getYM2608Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2608Type()[0],
                ucSI.rbYM2608P_SCCI,
                ucSI.cmbYM2608P_SCCI,
                ucSI.rbYM2608P_Emu,
                null,
                null,
                null,
                null,
                null
        );

        setting.getYM2608Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2608Type()[1],
                ucSI.rbYM2608S_SCCI,
                ucSI.cmbYM2608S_SCCI,
                ucSI.rbYM2608S_Emu,
                null,
                null,
                null,
                null,
                null
        );

        //setting.YM2608Type.UseWaitBoost = cbYM2608UseWaitBoost.isSelected();
        //setting.YM2608Type.OnlyPCMEmulation = cbOnlyPCMEmulation.isSelected();
        //setting.YM2608Type.LatencyForEmulation = 0;
        //if (Integer.parseInt(tbYM2608EmuDelay.getText(),  i)) {
        //    setting.YM2608Type.LatencyForEmulation = Math.max(Math.min(i, 999), 0);
        //}
        //setting.YM2608Type.LatencyForScci = 0;
        //if (Integer.parseInt(tbYM2608ScciDelay.getText(),  i)) {
        //    setting.YM2608Type.LatencyForScci = Math.max(Math.min(i, 999), 0);
        //}

        setting.setYM2610Type(new ChipType2[2]);
        setting.getYM2610Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2610Type()[0],
                ucSI.rbYM2610BP_SCCI,
                ucSI.cmbYM2610BP_SCCI,
                ucSI.rbYM2610BP_Emu,
                null,
                null,
                ucSI.rbYM2610BEP_SCCI,
                ucSI.cmbYM2610BEP_SCCI,
                ucSI.cmbSPPCMP_SCCI
        );

        setting.getYM2610Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2610Type()[1],
                ucSI.rbYM2610BS_SCCI,
                ucSI.cmbYM2610BS_SCCI,
                ucSI.rbYM2610BS_Emu,
                null,
                null,
                ucSI.rbYM2610BES_SCCI,
                ucSI.cmbYM2610BES_SCCI,
                ucSI.cmbSPPCMS_SCCI
        );

        setting.setYM2151Type(new ChipType2[2]);
        setting.getYM2151Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2151Type()[0],
                ucSI.rbYM2151P_SCCI,
                ucSI.cmbYM2151P_SCCI,
                ucSI.rbYM2151P_Emu,
                ucSI.rbYM2151P_EmuMame,
                ucSI.rbYM2151P_EmuX68Sound,
                null,
                null,
                null
        );

        setting.getYM2151Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2151Type()[1],
                ucSI.rbYM2151S_SCCI,
                ucSI.cmbYM2151S_SCCI,
                ucSI.rbYM2151S_Emu,
                ucSI.rbYM2151S_EmuMame,
                ucSI.rbYM2151S_EmuX68Sound,
                null,
                null,
                null
        );

        setting.setYM2203Type(new ChipType2[2]);
        setting.getYM2203Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2203Type()[0],
                ucSI.rbYM2203P_SCCI,
                ucSI.cmbYM2203P_SCCI,
                ucSI.rbYM2203P_Emu,
                null, null, null, null, null
        );

        setting.getYM2203Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2203Type()[1],
                ucSI.rbYM2203S_SCCI,
                ucSI.cmbYM2203S_SCCI,
                ucSI.rbYM2203S_Emu,
                null, null, null, null, null
        );

        setting.setAY8910Type(new ChipType2[2]);
        setting.getAY8910Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getAY8910Type()[0],
                ucSI.rbAY8910P_Real,
                ucSI.cmbAY8910P_Real,
                ucSI.rbAY8910P_Emu,
                ucSI.rbAY8910P_Emu2,
                null, null, null, null
        );

        setting.getAY8910Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getAY8910Type()[1],
                ucSI.rbAY8910S_Real,
                ucSI.cmbAY8910S_Real,
                ucSI.rbAY8910S_Emu,
                ucSI.rbAY8910S_Emu2,
                null, null, null, null
        );

        setting.setK051649Type(new ChipType2[2]);
        setting.getK051649Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getK051649Type()[0],
                ucSI.rbK051649P_Real,
                ucSI.cmbK051649P_Real,
                ucSI.rbK051649P_Emu,
                null, null, null, null, null
        );

        setting.getK051649Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getK051649Type()[1],
                ucSI.rbK051649S_Real,
                ucSI.cmbK051649S_Real,
                ucSI.rbK051649S_Emu,
                null, null, null, null, null
        );


        setting.setYM2413Type(new ChipType2[2]);
        setting.getYM2413Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2413Type()[0],
                ucSI.rbYM2413P_Real,
                ucSI.cmbYM2413P_Real,
                ucSI.rbYM2413P_Emu,
                null, null, null, null, null
        );

        setting.getYM2413Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2413Type()[1],
                ucSI.rbYM2413S_Real,
                ucSI.cmbYM2413S_Real,
                ucSI.rbYM2413S_Emu,
                null, null, null, null, null
        );

        setting.setC140Type(new ChipType2[2]);
        setting.getC140Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getC140Type()[0],
                ucSI.rbC140P_Real,
                ucSI.cmbC140P_SCCI,
                ucSI.rbC140P_Emu,
                null, null, null, null, null
        );

        setting.getC140Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getC140Type()[1],
                ucSI.rbC140S_SCCI,
                ucSI.cmbC140S_SCCI,
                ucSI.rbC140S_Emu,
                null, null, null, null, null
        );


        setting.setSEGAPCMType(new ChipType2[2]);
        setting.getSEGAPCMType()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getSEGAPCMType()[0],
                ucSI.rbSEGAPCMP_SCCI,
                ucSI.cmbSEGAPCMP_SCCI,
                ucSI.rbSEGAPCMP_Emu,
                null, null, null, null, null
        );

        setting.getSEGAPCMType()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getSEGAPCMType()[1],
                ucSI.rbSEGAPCMS_SCCI,
                ucSI.cmbSEGAPCMS_SCCI,
                ucSI.rbSEGAPCMS_Emu,
                null, null, null, null, null
        );

        setting.setYM3526Type(new ChipType2[2]);
        setting.getYM3526Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3526Type()[0],
                ucSI.rbYM3526P_SCCI,
                ucSI.cmbYM3526P_SCCI,
                ucSI.rbYM3526P_Emu,
                null, null, null, null, null
        );

        setting.getYM3526Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3526Type()[1],
                ucSI.rbYM3526S_SCCI,
                ucSI.cmbYM3526S_SCCI,
                ucSI.rbYM3526S_Emu,
                null, null, null, null, null
        );

        setting.setYM3812Type(new ChipType2[2]);
        setting.getYM3812Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3812Type()[0],
                ucSI.rbYM3812P_SCCI,
                ucSI.cmbYM3812P_SCCI,
                ucSI.rbYM3812P_Emu,
                null, null, null, null, null
        );

        setting.getYM3812Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3812Type()[1],
                ucSI.rbYM3812S_SCCI,
                ucSI.cmbYM3812S_SCCI,
                ucSI.rbYM3812S_Emu,
                null, null, null, null, null
        );

        setting.setYMF262Type(new ChipType2[2]);
        setting.getYMF262Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYMF262Type()[0],
                ucSI.rbYMF262P_SCCI,
                ucSI.cmbYMF262P_SCCI,
                ucSI.rbYMF262P_Emu,
                null, null, null, null, null
        );

        setting.getYMF262Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYMF262Type()[1],
                ucSI.rbYMF262S_SCCI,
                ucSI.cmbYMF262S_SCCI,
                ucSI.rbYMF262S_Emu,
                null, null, null, null, null
        );

//#endregion

        setting.getMidiKbd().setMidiInDeviceName(cmbMIDIIN.getSelectedItem() != null ? cmbMIDIIN.getSelectedItem().toString() : "");
        setting.getMidiKbd().getUseChannel()[0] = cbFM1.isSelected();
        setting.getMidiKbd().getUseChannel()[1] = cbFM2.isSelected();
        setting.getMidiKbd().getUseChannel()[2] = cbFM3.isSelected();
        setting.getMidiKbd().getUseChannel()[3] = cbFM4.isSelected();
        setting.getMidiKbd().getUseChannel()[4] = cbFM5.isSelected();
        setting.getMidiKbd().getUseChannel()[5] = cbFM6.isSelected();

        setting.getMidiKbd().setUseMIDIKeyboard(cbUseMIDIKeyboard.isSelected());

        setting.getMidiKbd().setMono(rbMONO.isSelected());
        setting.getMidiKbd().setUseMonoChannel(rbFM1.isSelected() ? 0 : (rbFM2.isSelected() ? 1 : (rbFM3.isSelected() ? 2 : (rbFM4.isSelected() ? 3 : (rbFM5.isSelected() ? 4 : (rbFM6.isSelected() ? 5 : -1))))));

        setting.getMidiKbd().setMidiCtrl_CopySelecttingLogToClipbrd(parseMidiCtrl(tbCCCopyLog.getText()));
        setting.getMidiKbd().setMidiCtrl_CopyToneFromYM2612Ch1(parseMidiCtrl(tbCCChCopy.getText()));
        setting.getMidiKbd().setMidiCtrl_DelOneLog(parseMidiCtrl(tbCCDelLog.getText()));
        setting.getMidiKbd().setMidiCtrl_Fadeout(parseMidiCtrl(tbCCFadeout.getText()));
        setting.getMidiKbd().setMidiCtrl_Fast(parseMidiCtrl(tbCCFast.getText()));
        setting.getMidiKbd().setMidiCtrl_Next(parseMidiCtrl(tbCCNext.getText()));
        setting.getMidiKbd().setMidiCtrl_Pause(parseMidiCtrl(tbCCPause.getText()));
        setting.getMidiKbd().setMidiCtrl_Play(parseMidiCtrl(tbCCPlay.getText()));
        setting.getMidiKbd().setMidiCtrl_Previous(parseMidiCtrl(tbCCPrevious.getText()));
        setting.getMidiKbd().setMidiCtrlSlow(parseMidiCtrl(tbCCSlow.getText()));
        setting.getMidiKbd().setMidiCtrl_Stop(parseMidiCtrl(tbCCStop.getText()));

        setting.setLatencyEmulation(parseIntSafe(tbLatencyEmu.getText(), 0, 999, setting.getLatencyEmulation()));
        setting.setLatencySCCI(parseIntSafe(tbLatencySCCI.getText(), 0, 999, setting.getLatencySCCI()));

        setting.getOther().setUseLoopTimes(cbUseLoopTimes.isSelected());
        setting.getOther().setLoopTimes(parseIntSafe(tbLoopTimes.getText(), 1, 999, setting.getOther().getLoopTimes()));

        setting.getOther().setUseGetInst(cbUseGetInst.isSelected());
        setting.getOther().setDefaultDataPath(tbDataPath.getText());
        setting.setFileSearchPathList(tbSearchPath.getText());
        setting.getOther().setInstFormat(EnmInstFormat.values()[cmbInstFormat.getSelectedIndex()]);
        setting.getOther().setScreenFrameRate(parseIntSafe(tbScreenFrameRate.getText(), 10, 120, setting.getOther().getScreenFrameRate()));
        setting.getOther().setAutoOpen(cbAutoOpen.isSelected());
        setting.getOther().setDumpSwitch(cbDumpSwitch.isSelected());
        setting.getOther().setDumpPath(tbDumpPath.getText());
        setting.getOther().setWavSwitch(cbWavSwitch.isSelected());
        setting.getOther().setWavPath(tbWavPath.getText());
        setting.getOther().setTextExt(tbTextExt.getText());
        setting.getOther().setMMLExt(tbMMLExt.getText());
        setting.getOther().setImageExt(tbImageExt.getText());
        setting.getOther().setAutoOpenText(cbAutoOpenText.isSelected());
        setting.getOther().setAutoOpenMML(cbAutoOpenMML.isSelected());
        setting.getOther().setAutoOpenImg(cbAutoOpenImg.isSelected());
        setting.getOther().setInitAlways(cbInitAlways.isSelected());
        setting.getOther().setEmptyPlayList(cbEmptyPlayList.isSelected());
        setting.getOther().setExAll(cbExALL.isSelected());
        setting.getOther().setNonRenderingForPause(cbNonRenderingForPause.isSelected());
        setting.setDebug_DispFrameCounter(cbDispFrameCounter.isSelected());
        setting.setDebug_SCCbaseAddress(parseHexSafe(tbSCCbaseAddress.getText(), setting.getDebug_SCCbaseAddress()));

        setting.setHiyorimiMode(cbHiyorimiMode.isSelected());

        setting.getMidiExport().setUseMIDIExport(cbUseMIDIExport.isSelected());
        setting.getMidiExport().setExportPath(tbMIDIOutputPath.getText());
        setting.getMidiExport().setUseVOPMex(cbMIDIUseVOPM.isSelected());
        setting.getMidiExport().setKeyOnFnum(cbMIDIKeyOnFnum.isSelected());
        setting.getMidiExport().setUseYM2151Export(cbMIDIYM2151.isSelected());
        setting.getMidiExport().setUseYM2612Export(cbMIDIYM2612.isSelected());

        setting.getMidiOut().setMidiOutInfos(new ArrayList<>());

        for (JTable d : dgv) {
            if (d.getRowCount() > 0) {
                List<MidiOutInfo> lstMoi = new ArrayList<>();
                for (i = 0; i < d.getRowCount(); i++) {
                    MidiOutInfo moi = new MidiOutInfo();
                    moi.id = (int) d.getModel().getValueAt(i, 0);
                    moi.isVST = (boolean) d.getModel().getValueAt(i, 1);
                    moi.fileName = (String) d.getModel().getValueAt(i, 2);
                    moi.name = (String) d.getModel().getValueAt(i, 3);
                    String stype = (String) d.getModel().getValueAt(i, 4);
                    // GM / XG / GS / LA / GS(SC - 55_1) / GS(SC - 55_2)
                    moi.type = 0;
                    if (stype.equals("XG")) moi.type = 1;
                    if (stype.equals("GS")) moi.type = 2;
                    if (stype.equals("LA")) moi.type = 3;
                    if (stype.equals("GS(SC - 55_1)")) moi.type = 4;
                    if (stype.equals("GS(SC - 55_2)")) moi.type = 5;
                    String sbeforeSend = (String) d.getModel().getValueAt(i, 5);
                    moi.beforeSendType = 0;
                    if (sbeforeSend.equals("GM Reset")) moi.beforeSendType = 1;
                    if (sbeforeSend.equals("XG Reset")) moi.beforeSendType = 2;
                    if (sbeforeSend.equals("GS Reset")) moi.beforeSendType = 3;
                    if (sbeforeSend.equals("Custom")) moi.beforeSendType = 4;

                    String mn = (String) d.getModel().getValueAt(i, 6);
                    if (moi.isVST) {
                        moi.vendor = mn;
                        moi.manufacturer = -1;
                    } else {
                        moi.vendor = "";
                        moi.manufacturer = mn == null || mn.equals("Unknown") ? -1 : Manufacturers.byManufacture(mn);
                    }

                    lstMoi.add(moi);
                }
                setting.getMidiOut().getMidiOutInfos().add(lstMoi.toArray(MidiOutInfo[]::new));
            } else {
                setting.getMidiOut().getMidiOutInfos().add(null);
            }
        }

        setting.getMidiOut().setGMReset(tbBeforeSend_GMReset.getText());
        setting.getMidiOut().setXGReset(tbBeforeSend_XGReset.getText());
        setting.getMidiOut().setGSReset(tbBeforeSend_GSReset.getText());
        setting.getMidiOut().setCustom(tbBeforeSend_Custom.getText());

        setting.getNsf().setNESUnmuteOnReset(cbNFSNes_UnmuteOnReset.isSelected());
        setting.getNsf().setNESNonLinearMixer(cbNFSNes_NonLinearMixer.isSelected());
        setting.getNsf().setNESPhaseRefresh(cbNFSNes_PhaseRefresh.isSelected());
        setting.getNsf().setNESDutySwap(cbNFSNes_DutySwap.isSelected());

        try {
            i = Integer.parseInt(tbNSFFds_LPF.getText());
            setting.getNsf().setFDSLpf(Math.clamp(i, 0, 99999));
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
        }
        setting.getNsf().setFDS4085Reset(cbNFSFds_4085Reset.isSelected());
        setting.getNsf().setFDSWriteDisable8000(cbNSFFDSWriteDisable8000.isSelected());

        setting.getNsf().setDMCUnmuteOnReset(cbNSFDmc_UnmuteOnReset.isSelected());
        setting.getNsf().setDMCNonLinearMixer(cbNSFDmc_NonLinearMixer.isSelected());
        setting.getNsf().setDMCEnable4011(cbNSFDmc_Enable4011.isSelected());
        setting.getNsf().setDMCEnablePnoise(cbNSFDmc_EnablePNoise.isSelected());
        setting.getNsf().setDMCDPCMAntiClick(cbNSFDmc_DPCMAntiClick.isSelected());
        setting.getNsf().setDMCRandomizeNoise(cbNSFDmc_RandomizeNoise.isSelected());
        setting.getNsf().setDMCTRImute(cbNSFDmc_TriMute.isSelected());
        setting.getNsf().setDMCRandomizeTRI(cbNSFDmc_RandomizeTri.isSelected());
        setting.getNsf().setDMCDPCMReverse(cbNSFDmc_DPCMReverse.isSelected());

        setting.getNsf().setMMC5NonLinearMixer(cbNSFMmc5_NonLinearMixer.isSelected());
        setting.getNsf().setMMC5PhaseRefresh(cbNSFMmc5_PhaseRefresh.isSelected());

        setting.getNsf().setN160Serial(cbNSFN160_Serial.isSelected());
        setting.getNsf().setHPF(trkbNSFHPF.getValue());
        setting.getNsf().setLPF(trkbNSFLPF.getValue());

        setting.setSid(new Setting.SID());
        setting.getSid().romKernalPath = tbSIDKernal.getText();
        setting.getSid().romBasicPath = tbSIDBasic.getText();
        setting.getSid().romCharacterPath = tbSIDCharacter.getText();
        if (rdSIDQ1.isSelected()) setting.getSid().quality = 0;
        if (rdSIDQ2.isSelected()) setting.getSid().quality = 1;
        if (rdSIDQ3.isSelected()) setting.getSid().quality = 2;
        if (rdSIDQ4.isSelected()) setting.getSid().quality = 3;
        try {
            setting.getSid().outputBufferSize = Math.clamp(Integer.parseInt(tbSIDOutputBufferSize.getText()), 100, 999999);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            setting.getSid().outputBufferSize = 5000;
        }

        setting.getSid().c64model = rbSIDC64Model_PAL.isSelected() ? 0 : (
                rbSIDC64Model_NTSC.isSelected() ? 1 : (
                        rbSIDC64Model_OLDNTSC.isSelected() ? 2 : (
                                rbSIDC64Model_DREAN.isSelected() ? 3 : 0)));
        setting.getSid().c64modelForce = cbSIDC64Model_Force.isSelected();

        setting.getSid().sidModel = rbSIDModel_6581.isSelected() ? 0 : (
                rbSIDModel_8580.isSelected() ? 1 : 0);
        setting.getSid().sidmodelForce = cbSIDModel_Force.isSelected();


        setting.setNukedOPN2(new Setting.NukedOPN2());
        if (rbNukedOPN2OptionYM2612.isSelected()) setting.getNukedOPN2().emuType = 2;
        if (rbNukedOPN2OptionASIC.isSelected()) setting.getNukedOPN2().emuType = 1;
        if (rbNukedOPN2OptionDiscrete.isSelected()) setting.getNukedOPN2().emuType = 0;
        if (rbNukedOPN2OptionYM2612u.isSelected()) setting.getNukedOPN2().emuType = 3;
        if (rbNukedOPN2OptionASIClp.isSelected()) setting.getNukedOPN2().emuType = 4;
        setting.getNukedOPN2().gensDACHPF = cbGensDACHPF.isSelected();
        setting.getNukedOPN2().gensSSGEG = cbGensSSGEG.isSelected();

        setting.setAutoBalance(new Setting.AutoBalance());
        setting.getAutoBalance().setUseThis(cbAutoBalanceUseThis.isSelected());
        setting.getAutoBalance().setLoadSongBalance(rbAutoBalanceLoadSongBalance.isSelected());
        setting.getAutoBalance().setLoadDriverBalance(rbAutoBalanceLoadDriverBalance.isSelected());
        setting.getAutoBalance().setSaveSongBalance(rbAutoBalanceSaveSongBalance.isSelected());
        setting.getAutoBalance().setSamePositionAsSongData(rbAutoBalanceSamePositionAsSongData.isSelected());


        setting.getPmd().compilerArguments = tbPMDCompilerArguments.getText();
        setting.getPmd().isAuto = rbPMDAuto.isSelected();
        setting.getPmd().soundBoard = rbPMDNrmB.isSelected() ? 0 : (rbPMDSpbB.isSelected() ? 1 : 2);
        setting.getPmd().setManualVolume = cbPMDSetManualVolume.isSelected();
        setting.getPmd().usePPSDRV = cbPMDUsePPSDRV.isSelected();
        setting.getPmd().usePPZ8 = cbPMDUsePPZ8.isSelected();
        setting.getPmd().driverArguments = tbPMDDriverArguments.getText();
        setting.getPmd().usePPSDRVUseInterfaceDefaultFreq = rbPMDUsePPSDRVFreqDefault.isSelected();
        int nn;
        try {
            nn = Integer.parseInt(tbPMDPPSDRVFreq.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 2000;
        }
        setting.getPmd().ppsDrvManualFreq = nn;
        try {
            nn = Integer.parseInt(tbPMDPPSDRVManualWait.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 1;
        }
        nn = Math.clamp(nn, 0, 100);
        setting.getPmd().ppsDrvManualWait = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeFM.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 0;
        }
        nn = Math.clamp(nn, -191, 20);
        setting.getPmd().volumeFM = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeSSG.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 0;
        }
        nn = Math.clamp(nn, -191, 20);
        setting.getPmd().volumeSSG = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeRhythm.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 0;
        }
        nn = Math.clamp(nn, -191, 20);
        setting.getPmd().volumeRhythm = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeAdpcm.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 0;
        }
        nn = Math.clamp(nn, -191, 20);
        setting.getPmd().volumeAdpcm = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeGIMICSSG.getText());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e);
            nn = 31;
        }
        nn = Math.clamp(nn, 0, 127);
        setting.getPmd().volumeGIMICSSG = nn;

        setting.getKeyboardHook().setUseKeyBoardHook(cbUseKeyBoardHook.isSelected());

        setting.getKeyboardHook().getStop().setShift(cbStopShift.isSelected());
        setting.getKeyboardHook().getStop().setCtrl(cbStopCtrl.isSelected());
        setting.getKeyboardHook().getStop().setWin(cbStopWin.isSelected());
        setting.getKeyboardHook().getStop().setAlt(cbStopAlt.isSelected());
        setting.getKeyboardHook().getStop().setKey(lblStopKey.getText() == null || lblStopKey.getText().isEmpty() ? "(None)" : lblStopKey.getText());

        setting.getKeyboardHook().getPause().setShift(cbPauseShift.isSelected());
        setting.getKeyboardHook().getPause().setCtrl(cbPauseCtrl.isSelected());
        setting.getKeyboardHook().getPause().setWin(cbPauseWin.isSelected());
        setting.getKeyboardHook().getPause().setAlt(cbPauseAlt.isSelected());
        setting.getKeyboardHook().getPause().setKey(lblPauseKey.getText() == null || lblPauseKey.getText().isEmpty() ? "(None)" : lblPauseKey.getText());

        setting.getKeyboardHook().getFadeout().setShift(cbFadeoutShift.isSelected());
        setting.getKeyboardHook().getFadeout().setCtrl(cbFadeoutCtrl.isSelected());
        setting.getKeyboardHook().getFadeout().setWin(cbFadeoutWin.isSelected());
        setting.getKeyboardHook().getFadeout().setAlt(cbFadeoutAlt.isSelected());
        setting.getKeyboardHook().getFadeout().setKey(lblFadeoutKey.getText() == null || lblFadeoutKey.getText().isEmpty() ? "(None)" : lblFadeoutKey.getText());

        setting.getKeyboardHook().getPrev().setShift(cbPrevShift.isSelected());
        setting.getKeyboardHook().getPrev().setCtrl(cbPrevCtrl.isSelected());
        setting.getKeyboardHook().getPrev().setWin(cbPrevWin.isSelected());
        setting.getKeyboardHook().getPrev().setAlt(cbPrevAlt.isSelected());
        setting.getKeyboardHook().getPrev().setKey(lblPrevKey.getText() == null || lblPrevKey.getText().isEmpty() ? "(None)" : lblPrevKey.getText());

        setting.getKeyboardHook().getSlow().setShift(cbSlowShift.isSelected());
        setting.getKeyboardHook().getSlow().setCtrl(cbSlowCtrl.isSelected());
        setting.getKeyboardHook().getSlow().setWin(cbSlowWin.isSelected());
        setting.getKeyboardHook().getSlow().setAlt(cbSlowAlt.isSelected());
        setting.getKeyboardHook().getSlow().setKey(lblSlowKey.getText() == null || lblSlowKey.getText().isEmpty() ? "(None)" : lblSlowKey.getText());

        setting.getKeyboardHook().getPlay().setShift(cbPlayShift.isSelected());
        setting.getKeyboardHook().getPlay().setCtrl(cbPlayCtrl.isSelected());
        setting.getKeyboardHook().getPlay().setWin(cbPlayWin.isSelected());
        setting.getKeyboardHook().getPlay().setAlt(cbPlayAlt.isSelected());
        setting.getKeyboardHook().getPlay().setKey(lblPlayKey.getText() == null || lblPlayKey.getText().isEmpty() ? "(None)" : lblPlayKey.getText());

        setting.getKeyboardHook().getFast().setShift(cbFastShift.isSelected());
        setting.getKeyboardHook().getFast().setCtrl(cbFastCtrl.isSelected());
        setting.getKeyboardHook().getFast().setWin(cbFastWin.isSelected());
        setting.getKeyboardHook().getFast().setAlt(cbFastAlt.isSelected());
        setting.getKeyboardHook().getFast().setKey(lblFastKey.getText() == null || lblFastKey.getText().isEmpty() ? "(None)" : lblFastKey.getText());

        setting.getKeyboardHook().getNext().setShift(cbNextShift.isSelected());
        setting.getKeyboardHook().getNext().setCtrl(cbNextCtrl.isSelected());
        setting.getKeyboardHook().getNext().setWin(cbNextWin.isSelected());
        setting.getKeyboardHook().getNext().setAlt(cbNextAlt.isSelected());
        setting.getKeyboardHook().getNext().setKey(lblNextKey.getText() == null || lblNextKey.getText().isEmpty() ? "(None)" : lblNextKey.getText());

        this.dialogResult = JFileChooser.APPROVE_OPTION;
        this.setVisible(false);
    }

    private static void setChipType2FromControls(
            ChipType2 ct,
            JCheckBox rb_SCCI,
            JComboBox<String> cmb_SCCI,
            JCheckBox rb_EMU0,
            JCheckBox rb_EMU1,
            JCheckBox rb_EMU2,
            JCheckBox rb_SCCI_E,
            JComboBox<String> cmb_SCCI_E1,
            JComboBox<String> cmb_SCCI_E2
    ) {
        ct.setUseReal(new boolean[rb_SCCI_E == null ? 1 : 3]);
        ct.getUseReal()[0] = rb_SCCI.isSelected();
        ct.setRealChipInfo(new ChipType2.RealChipInfo[rb_SCCI_E == null ? 1 : 3]);
        for (int i = 0; i < ct.getRealChipInfo().length; i++) ct.getRealChipInfo()[i] = new ChipType2.RealChipInfo();
        int v;
        ChipType2.RealChipInfo rci;
        if (rb_SCCI.isSelected()) {
            if (cmb_SCCI.getSelectedItem() != null) {
                String n = cmb_SCCI.getSelectedItem().toString();
                int idx = n.indexOf(")");
                if (idx != -1 && n.startsWith("(")) {
                    n = n.substring(1, idx);
                    String[] ns = n.split(":");
                    if (ns.length >= 3) {
                        rci = ct.getRealChipInfo()[0];
                        rci.setInterfaceName(String.join(":", Arrays.copyOfRange(ns, 0, ns.length - 3)));
                        try {
                            v = Integer.parseInt(ns[ns.length - 3]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setSoundLocation(v);
                        try {
                            v = Integer.parseInt(ns[ns.length - 2]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setBusID(v);
                        try {
                            v = Integer.parseInt(ns[ns.length - 1]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setSoundChip(v);
                    }
                }
            }
        }

        ct.setUseEmu(null);
        if (rb_EMU2 != null) ct.setUseEmu(new boolean[3]);
        else if (rb_EMU1 != null) ct.setUseEmu(new boolean[2]);
        else if (rb_EMU0 != null) ct.setUseEmu(new boolean[1]);
        if (rb_EMU0 != null)
            ct.getUseEmu()[0] = rb_EMU0.isSelected(); // (rb_EMU0.isSelected() || rb_SCCI.isSelected());
        if (rb_EMU1 != null) ct.getUseEmu()[1] = rb_EMU1.isSelected();
        if (rb_EMU2 != null) ct.getUseEmu()[2] = rb_EMU2.isSelected();

        if (rb_SCCI_E != null && rb_SCCI_E.isSelected()) {
            if (cmb_SCCI_E1.getSelectedItem() != null) {
                String n = cmb_SCCI_E1.getSelectedItem().toString();
                int idx = n.indexOf(")");
                if (idx != -1 && n.startsWith("(")) {
                    n = n.substring(1, idx);
                    String[] ns = n.split(":");
                    if (ns.length >= 3) {
                        rci = ct.getRealChipInfo()[1];
                        rci.setInterfaceName(String.join(":", Arrays.copyOfRange(ns, 0, ns.length - 3)));
                        try {
                            v = Integer.parseInt(ns[ns.length - 3]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setSoundLocation(v);
                        try {
                            v = Integer.parseInt(ns[ns.length - 2]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setBusID(v);
                        try {
                            v = Integer.parseInt(ns[ns.length - 1]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setSoundChip(v);
                    }
                }
            }
            if (cmb_SCCI_E2.getSelectedItem() != null) {
                String n = cmb_SCCI_E2.getSelectedItem().toString();
                int idx = n.indexOf(")");
                if (idx != -1 && n.startsWith("(")) {
                    n = n.substring(1, idx);
                    String[] ns = n.split(":");
                    if (ns.length >= 3) {
                        rci = ct.getRealChipInfo()[2];
                        rci.setInterfaceName(String.join(":", Arrays.copyOfRange(ns, 0, ns.length - 3)));
                        try {
                            v = Integer.parseInt(ns[ns.length - 3]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setSoundLocation(v);
                        try {
                            v = Integer.parseInt(ns[ns.length - 2]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setBusID(v);
                        try {
                            v = Integer.parseInt(ns[ns.length - 1]);
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, e);
                            v = 0;
                        }
                        rci.setSoundChip(v);
                    }
                }
            }
        }
    }

    private static int parseMidiCtrl(String text) {
        if (text == null || text.trim().isEmpty()) return -1;
        try {
            return Math.clamp(Integer.parseInt(text.trim()), 0, 127);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int parseIntSafe(String text, int min, int max, int defaultValue) {
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Math.clamp(Integer.parseInt(text.trim()), min, max);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int parseHexSafe(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(text.trim(), 16);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Input value check
     */
    private boolean checkSetting() {
        HashSet<String> hsSCCIs = new HashSet<>();
        boolean ret = false;

        // SCCI duplicate setting check

        if (ucSI.rbYM2612P_SCCI.isSelected())
            if (ucSI.cmbYM2612P_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2612P_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2612P_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2612S_SCCI.isSelected())
            if (ucSI.cmbYM2612S_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2612S_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2612S_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbSN76489P_SCCI.isSelected())
            if (ucSI.cmbSN76489P_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbSN76489P_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbSN76489P_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbSN76489S_SCCI.isSelected())
            if (ucSI.cmbSN76489S_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbSN76489S_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbSN76489S_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2608P_SCCI.isSelected())
            if (ucSI.cmbYM2608P_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2608P_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2608P_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2608S_SCCI.isSelected())
            if (ucSI.cmbYM2608S_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2608S_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2608S_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2151P_SCCI.isSelected())
            if (ucSI.cmbYM2151P_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2151P_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2151P_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2151S_SCCI.isSelected())
            if (ucSI.cmbYM2151S_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2151S_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2151S_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2203P_SCCI.isSelected())
            if (ucSI.cmbYM2203P_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2203P_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2203P_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2203S_SCCI.isSelected())
            if (ucSI.cmbYM2203S_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2203S_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2203S_SCCI.getSelectedItem().toString());
                else ret = true;


        if (ucSI.rbYM2413P_Real.isSelected())
            if (ucSI.cmbYM2413P_Real.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2413P_Real.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2413P_Real.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2413S_Real.isSelected())
            if (ucSI.cmbYM2413S_Real.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2413S_Real.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2413S_Real.getSelectedItem().toString());
                else ret = true;


        if (ucSI.rbYM2610BP_SCCI.isSelected())
            if (ucSI.cmbYM2610BP_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2610BP_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2610BP_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2610BS_SCCI.isSelected())
            if (ucSI.cmbYM2610BS_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2610BS_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2610BS_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2610BEP_SCCI.isSelected())
            if (ucSI.cmbYM2610BEP_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2610BEP_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2610BEP_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2610BES_SCCI.isSelected())
            if (ucSI.cmbYM2610BES_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbYM2610BES_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbYM2610BES_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2610BEP_SCCI.isSelected())
            if (ucSI.cmbSPPCMP_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbSPPCMP_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbSPPCMP_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbYM2610BES_SCCI.isSelected())
            if (ucSI.cmbSPPCMS_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbSPPCMS_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbSPPCMS_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbC140P_Real.isSelected())
            if (ucSI.cmbC140P_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbC140P_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbC140P_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbC140S_SCCI.isSelected())
            if (ucSI.cmbC140S_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbC140S_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbC140S_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbSEGAPCMP_SCCI.isSelected())
            if (ucSI.cmbSEGAPCMP_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbSEGAPCMP_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbSEGAPCMP_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ucSI.rbSEGAPCMS_SCCI.isSelected())
            if (ucSI.cmbSEGAPCMS_SCCI.getSelectedItem() != null)
                if (!hsSCCIs.contains(ucSI.cmbSEGAPCMS_SCCI.getSelectedItem().toString()))
                    hsSCCIs.add(ucSI.cmbSEGAPCMS_SCCI.getSelectedItem().toString());
                else ret = true;

        if (ret) {
            return JOptionPane.showConfirmDialog(null,
                    "Duplicate SCCI/GIMIC devices are configured. Do you want to continue?",
                    "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
            ) == JOptionPane.YES_OPTION;
        }

        return true;
    }

    public String getAssemblyTitle() {
//        Object[] attributes = Assembly.GetExecutingAssembly().GetCustomAttributes(typeof(AssemblyTitleAttribute), false);
//        if (attributes.length > 0) {
//            AssemblyTitleAttribute titleAttribute = (AssemblyTitleAttribute) attributes[0];
//            if (titleAttribute.Title != "") {
//                return titleAttribute.Title;
//            }
//        }
//        return Path.GetFileNameWithoutExtension(Assembly.GetExecutingAssembly().CodeBase);
        return null;
    }

    public String getAssemblyVersion() {
//            return Assembly.GetExecutingAssembly().GetName().Version.toString();
        return null;
    }

    public String getAssemblyDescription() {
//        Object[] attributes = Assembly.GetExecutingAssembly().GetCustomAttributes(typeof(AssemblyDescriptionAttribute), false);
//        if (attributes.length == 0) {
//            return "";
//        }
//        return ((AssemblyDescriptionAttribute) attributes[0]).Description;
        return null;
    }

    public String getAssemblyProduct() {
//        Object[] attributes = Assembly.GetExecutingAssembly().GetCustomAttributes(typeof(AssemblyProductAttribute), false);
//        if (attributes.length == 0) {
//            return "";
//        }
//        return ((AssemblyProductAttribute) attributes[0]).Product;
        return null;
    }

    public String getAssemblyCopyright() {
//        Object[] attributes = Assembly.GetExecutingAssembly().GetCustomAttributes(typeof(AssemblyCopyrightAttribute), false);
//        if (attributes.length == 0) {
//            return "";
//        }
//        return ((AssemblyCopyrightAttribute) attributes[0]).Copyright;
        return null;
    }

    public String getAssemblyCompany() {
//        Object[] attributes = Assembly.GetExecutingAssembly().GetCustomAttributes(typeof(AssemblyCompanyAttribute), false);
//        if (attributes.length == 0) {
//            return "";
//        }
//        return ((AssemblyCompanyAttribute) attributes[0]).Company;
        return null;
    }

    private void cbUseMIDIKeyboard_CheckedChanged(ChangeEvent ev) {
        gbMIDIKeyboard.setEnabled(cbUseMIDIKeyboard.isSelected());
    }

    private void rbWaveOut_CheckedChanged(ChangeEvent ev) {
        lblLatency.setEnabled(true);
        lblLatencyUnit.setEnabled(true);
        cmbLatency.setEnabled(true);
    }

    private void rbDirectSoundOut_CheckedChanged(ChangeEvent ev) {
        lblLatency.setEnabled(true);
        lblLatencyUnit.setEnabled(true);
        cmbLatency.setEnabled(true);
    }

    private void rbWasapiOut_CheckedChanged(ChangeEvent ev) {
        lblLatency.setEnabled(true);
        lblLatencyUnit.setEnabled(true);
        cmbLatency.setEnabled(true);
    }

    private void rbAsioOut_CheckedChanged(ChangeEvent ev) {
        lblLatency.setEnabled(false);
        lblLatencyUnit.setEnabled(false);
        cmbLatency.setEnabled(false);
    }

    private void cbUseLoopTimes_CheckedChanged(ChangeEvent ev) {
        tbLoopTimes.setEnabled(cbUseLoopTimes.isSelected());
        lblLoopTimes.setEnabled(cbUseLoopTimes.isSelected());
    }

    private static void btnOpenSettingFolder_Click(ActionEvent ev) {
        try {
            Path fullPath = Common.settingFilePath;
            new ProcessBuilder(fullPath.toString()).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void btnDataPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("Please specify a folder.");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbDataPath.setText(fbd.getSelectedFile().getPath());
    }

    private void btnSearchPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("Please specify a folder.");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbSearchPath.setText(fbd.getSelectedFile().getPath());
    }

    private void cbUseGetInst_CheckedChanged(ChangeEvent ev) {
        lblInstFormat.setEnabled(cbUseGetInst.isSelected());
        cmbInstFormat.setEnabled(cbUseGetInst.isSelected());
    }

    private void cbDumpSwitch_CheckedChanged(ChangeEvent ev) {
        gbDump.setEnabled(cbDumpSwitch.isSelected());
    }

    private void btnDumpPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("Please specify a folder.");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbDumpPath.setText(fbd.getSelectedFile().getPath());
    }

    private void btnResetPosition_Click(ActionEvent ev) {
        int res = JOptionPane.showConfirmDialog(null,
                "Reset all display positions. Are you sure? (The position of currently open windows cannot be reset.)",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.NO_OPTION) return;

        setting.setLocation(new Setting.Location());
    }

    private void cbUseMIDIExport_CheckedChanged(ChangeEvent ev) {
        gbMIDIExport.setEnabled(cbUseMIDIExport.isSelected());
    }

    private void btnMIDIOutputPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("Please specify a folder.");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbMIDIOutputPath.setText(fbd.getSelectedFile().getPath());
    }

    private void btnWavPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("Please specify a folder.");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbWavPath.setText(fbd.getSelectedFile().getPath());
    }

    private void cbWavSwitch_CheckedChanged(ChangeEvent ev) {
        gbWav.setEnabled(cbWavSwitch.isSelected());
    }

    private void btVST_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".dll");
            }

            @Override
            public String getDescription() {
                return "VST Plugin file(*.dll)";
            }
        });
        ofd.setDialogTitle("Select a file");
        int filterIndex = setting.getOther().getFilterIndex();
        FileFilter[] filters = ofd.getChoosableFileFilters();
        if (filterIndex >= 0 && filterIndex < filters.length) {
            ofd.setFileFilter(filters[filterIndex]);
        }

        if (!setting.getOther().getDefaultDataPath().isEmpty() && Files.exists(Path.of(setting.getOther().getDefaultDataPath())) && IsInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(setting.getOther().getDefaultDataPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        IsInitialOpenFolder = false;
        setting.getOther().setFilterIndex(Common.getFilterIndex(ofd));

        tbVST.setText(ofd.getSelectedFile().getName());
    }

    private void btnAddMIDIout_Click(ActionEvent ev) {
        if (dgvMIDIoutPallet.getSelectedRowCount() < 1) return;

        int p = tbcMIDIoutList.getSelectedIndex();

        for (int row : dgvMIDIoutPallet.getSelectedRows()) {
            boolean found = false;
            for (int r = 0; r < dgv[p].getRowCount(); r++) {
                if (dgv[p].getModel().getValueAt(r, 1).equals(dgvMIDIoutPallet.getModel().getValueAt(row, 1))) {
                    found = true;
                    break;
                }
            }

            if (!found)
                ((DefaultTableModel) dgv[p].getModel()).addRow(new Object[] {
                        dgvMIDIoutPallet.getModel().getValueAt(row, 0),
                        false,
                        "",
                        dgvMIDIoutPallet.getModel().getValueAt(row, 1),
                        "GM",
                        "None",
                        dgvMIDIoutPallet.getModel().getValueAt(row, 2)
                });
        }
    }

    private void btnSubMIDIout_Click(ActionEvent ev) {
        int p = tbcMIDIoutList.getSelectedIndex();

        if (dgv[p].getSelectedRowCount() < 1) return;

        for (int row : dgv[p].getSelectedRows()) {
            ((DefaultTableModel) dgv[p].getModel()).removeRow(row);
        }
    }

    private void btnUP_Click(ActionEvent ev) {
        int p = tbcMIDIoutList.getSelectedIndex();

        if (dgv[p].getSelectedRowCount() < 1) return;

        for (int row : dgv[p].getSelectedRows()) {
            if (row < 1) continue;

            int i = row - 1;
            DefaultTableModel m = (DefaultTableModel) dgv[p].getModel();
            m.insertRow(i, new Object[] {
                    m.getValueAt(row, 0),
                    m.getValueAt(row, 1),
                    m.getValueAt(row, 2),
                    m.getValueAt(row, 3),
                    m.getValueAt(row, 4),
                    m.getValueAt(row, 5),
            });
            m.removeRow(row);
            dgv[p].setRowSelectionInterval(i, i);
        }
    }

    private void btnDOWN_Click(ActionEvent ev) {
        int p = tbcMIDIoutList.getSelectedIndex();

        if (dgv[p].getSelectedRowCount() < 1) return;

        for (int row : dgv[p].getSelectedRows()) {
            if (row > dgv[p].getRowCount() - 2) continue;

            int i = row + 1;
            DefaultTableModel m = (DefaultTableModel) dgv[p].getModel();
            m.insertRow(i + 2, new Object[] {
                    m.getValueAt(row, 0),
                    m.getValueAt(row, 1),
                    m.getValueAt(row, 2),
                    m.getValueAt(row, 3),
                    m.getValueAt(row, 4),
                    m.getValueAt(row, 5),
            });
            m.removeRow(row);
            dgv[p].setRowSelectionInterval(i, i);
        }
    }

    private void btnAddVST_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".dll");
            }

            @Override
            public String getDescription() {
                return "VST Plugin file(*.dll)";
            }
        });
        ofd.setDialogTitle("Select a file");
        int filterIndex = setting.getOther().getFilterIndex();
        FileFilter[] filters = ofd.getChoosableFileFilters();
        if (filterIndex >= 0 && filterIndex < filters.length) {
            ofd.setFileFilter(filters[filterIndex]);
        }

//        if (!setting.getVst().getDefaultPath().isEmpty() && Directory.exists(setting.getVst().getDefaultPath()) && IsInitialOpenFolder) {
//            ofd.setCurrentDirectory(new File(setting.getVst().getDefaultPath()));
////        } else {
////            ofd.RestoreDirectory = true;
//        }
////        ofd.CheckPathExists = true;
//        ofd.setMultiSelectionEnabled(false);
//
//        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
//            return;
//        }
//
//        VstInfo s = audio.getVSTInfo(ofd.getSelectedFile().getName());
//        if (s == null) return;
//
//        setting.getVst().setDefaultPath(Path.getDirectoryName(ofd.getSelectedFile().getName()));
//
//        int p = tbcMIDIoutList.getSelectedIndex();
//        ((DefaultTableModel) dgv[p].getModel()).addRow(new Object[] {
//                -999
//                , true
//                , s.fileName
//                , s.effectName
//                , "GM"
//                , "None"
//                , s.vendorName
//        });
    }

    private void btnSIDKernal_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(ofd.getAcceptAllFileFilter());
        ofd.setDialogTitle("Select a file");
//        ofd.RestoreDirectory = true;
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        tbSIDKernal.setText(ofd.getSelectedFile().getName());
    }

    private void btnSIDBasic_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(ofd.getAcceptAllFileFilter());
        ofd.setDialogTitle("Select a file");
//        ofd.RestoreDirectory = true;
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        tbSIDBasic.setText(ofd.getSelectedFile().getName());
    }

    private void btnSIDCharacter_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(ofd.getAcceptAllFileFilter());
        ofd.setDialogTitle("Select a file");
//        ofd.RestoreDirectory = true;
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        tbSIDCharacter.setText(ofd.getSelectedFile().getName());
    }

    private void btnBeforeSend_Default_Click(ActionEvent ev) {
        Setting.MidiOut mo = new Setting.MidiOut();
        tbBeforeSend_GMReset.setText(mo.getGMReset());
        tbBeforeSend_XGReset.setText(mo.getXGReset());
        tbBeforeSend_GSReset.setText(mo.getGSReset());
        tbBeforeSend_Custom.setText(mo.getCustom());
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (FormMain.keyHookMeth != null /* this::keyHookMeth */) { // TODO
                FormMain.keyHookMeth = null;
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }
    };

    private void cbUseKeyBoardHook_CheckedChanged(ChangeEvent ev) {
        gbUseKeyBoardHook.setEnabled(cbUseKeyBoardHook.isSelected());
    }

    private void btStopClr_Click(ActionEvent ev) {
        lblStopKey.setText("(None)");
        btStopClr.setEnabled(false);
    }

    private void btPauseClr_Click(ActionEvent ev) {
        lblPauseKey.setText("(None)");
        btPauseClr.setEnabled(false);
    }

    private void btFadeoutClr_Click(ActionEvent ev) {
        lblFadeoutKey.setText("(None)");
        btFadeoutClr.setEnabled(false);
    }

    private void btPrevClr_Click(ActionEvent ev) {
        lblPrevKey.setText("(None)");
        btPrevClr.setEnabled(false);
    }

    private void btSlowClr_Click(ActionEvent ev) {
        lblSlowKey.setText("(None)");
        btSlowClr.setEnabled(false);
    }

    private void btPlayClr_Click(ActionEvent ev) {
        lblPlayKey.setText("(None)");
        btPlayClr.setEnabled(false);
    }

    private void btFastClr_Click(ActionEvent ev) {
        lblFastKey.setText("(None)");
        btFastClr.setEnabled(false);
    }

    private void btNextClr_Click(ActionEvent ev) {
        lblNextKey.setText("(None)");
        btNextClr.setEnabled(false);
    }

    private void btStopSet_Click(ActionEvent ev) {

        lblKey = lblStopKey;
        btSet = btStopSet;
        btClr = btStopClr;
        btOK = btnOK;
        btStopSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btPauseSet_Click(ActionEvent ev) {
        lblKey = lblPauseKey;
        btSet = btPauseSet;
        btOK = btnOK;
        btClr = btPauseClr;
        btPauseSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btFadeoutSet_Click(ActionEvent ev) {
        lblKey = lblFadeoutKey;
        btSet = btFadeoutSet;
        btClr = btFadeoutClr;
        btOK = btnOK;
        btFadeoutSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btPrevSet_Click(ActionEvent ev) {
        lblKey = lblPrevKey;
        btSet = btPrevSet;
        btClr = btPrevClr;
        btOK = btnOK;
        btPrevSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btSlowSet_Click(ActionEvent ev) {
        lblKey = lblSlowKey;
        btSet = btSlowSet;
        btClr = btSlowClr;
        btOK = btnOK;
        btSlowSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btPlaySet_Click(ActionEvent ev) {
        lblKey = lblPlayKey;
        btSet = btPlaySet;
        btClr = btPlayClr;
        btOK = btnOK;
        btPlaySet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btFastSet_Click(ActionEvent ev) {
        lblKey = lblFastKey;
        btSet = btFastSet;
        btClr = btFastClr;
        btOK = btnOK;
        btFastSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    private void btNextSet_Click(ActionEvent ev) {
        lblKey = lblNextKey;
        btSet = btNextSet;
        btClr = btNextClr;
        btOK = btnOK;
        btNextSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("Waiting for input");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        FormMain.keyHookMeth = FormSetting::keyHookMeth;
    }

    public static JLabel lblKey = null;
    public static JLabel lblNotice = null;
    public static JButton btSet = null;
    public static JButton btClr = null;
    public static JButton btOK = null;

    public static void keyHookMeth(NativeKeyEvent e) {
//        if (e.UpDown != HongliangSoft.Utilities.Gui.KeyboardUpDown.Up) return;

//        lblKey.setForeground(Color.ControlText);
        lblKey.setText(String.valueOf(e.getKeyCode()));
        lblNotice.setVisible(false);

        FormMain.keyHookMeth = null;
        btSet.setEnabled(true);
        btOK.setEnabled(true);
        btClr.setEnabled(true);
    }

    private final MouseListener llOpenGithub_LinkClicked = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
//            llOpenGithub.LinkVisited = true; // TODO
                Desktop.getDesktop().browse(URI.create("https://github.com/kuma4649/MDPlayer/releases/latest"));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    };

    private void rbPMDManual_CheckedChanged(ChangeEvent ev) {
        gbPMDManual.setEnabled(rbPMDManual.isSelected());
    }

    private void btnPMDResetCompilerArhguments_Click(ActionEvent ev) {
        tbPMDCompilerArguments.setText("/v /C");
    }

    private void btnPMDResetDriverArguments_Click(ActionEvent ev) {
        tbPMDDriverArguments.setText("");
    }

    private void cbPMDUsePPSDRV_CheckedChanged(ChangeEvent ev) {
        gbPPSDRV.setEnabled(cbPMDUsePPSDRV.isSelected());
    }

    private void cbPMDSetManualVolume_CheckedChanged(ChangeEvent ev) {
        gbPMDSetManualVolume.setEnabled(cbPMDSetManualVolume.isSelected());
    }

    private void rbPMDUsePPSDRVManualFreq_CheckedChanged(ChangeEvent ev) {
        tbPMDPPSDRVFreq.setEnabled(rbPMDUsePPSDRVManualFreq.isSelected());
    }

    private void btnPMDPPSDRVManualWait_Click(ActionEvent ev) {
        tbPMDPPSDRVManualWait.setText("1");
    }

    private final FocusListener tbPMDPPSDRVFreq_Click = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            rbPMDUsePPSDRVManualFreq_CheckedChanged(null);
        }
    };

    private final MouseListener tbPMDPPSDRVFreq_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            rbPMDUsePPSDRVManualFreq_CheckedChanged(null);
        }
    };

    private final FocusListener groupBox20_Enter = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
        }
    };

    private void initializeComponent() {
        this.btnOK = new JButton();
        this.btnCancel = new JButton();
        this.gbWaveOut = new JPanel();
        this.cmbWaveOutDevice = new JComboBox<>();
        this.rbWaveOut = new JCheckBox();
        this.rbAsioOut = new JCheckBox();
        this.rbWasapiOut = new JCheckBox();
        this.gbAsioOut = new JPanel();
        this.btnASIOControlPanel = new JButton();
        this.cmbAsioDevice = new JComboBox<>();
        this.rbDirectSoundOut = new JCheckBox();
        this.gbWasapiOut = new JPanel();
        this.rbExclusive = new JCheckBox();
        this.rbShare = new JCheckBox();
        this.cmbWasapiDevice = new JComboBox<>();
        this.gbDirectSound = new JPanel();
        this.cmbDirectSoundDevice = new JComboBox<>();
        this.tcSetting = new JTabbedPane();
        this.tpOutput = new JPanel();
        this.rbNullDevice = new JCheckBox();
        this.label36 = new JLabel();
        this.lblWaitTime = new JLabel();
        this.label66 = new JLabel();
        this.lblLatencyUnit = new JLabel();
        this.label28 = new JLabel();
        this.label65 = new JLabel();
        this.lblLatency = new JLabel();
        this.cmbWaitTime = new JComboBox<>();
        this.cmbSampleRate = new JComboBox<>();
        this.cmbLatency = new JComboBox<>();
        this.rbSPPCM = new JCheckBox();
        this.groupBox16 = new JPanel();
        this.cmbSPPCMDevice = new JComboBox<>();
        this.tpModule = new JPanel();
        this.groupBox1 = new JPanel();
        this.cbUnuseRealChip = new JCheckBox();
        this.ucSI = new SettingInstrumentsPanel();
        this.groupBox3 = new JPanel();
        this.cbHiyorimiMode = new JCheckBox();
        this.label13 = new JLabel();
        this.label12 = new JLabel();
        this.label11 = new JLabel();
        this.tbLatencyEmu = new JTextArea();
        this.tbLatencySCCI = new JTextArea();
        this.label10 = new JLabel();
        this.tpNuked = new JPanel();
        this.groupBox29 = new JPanel();
        this.cbGensSSGEG = new JCheckBox();
        this.cbGensDACHPF = new JCheckBox();
        this.groupBox26 = new JPanel();
        this.rbNukedOPN2OptionYM2612u = new JCheckBox();
        this.rbNukedOPN2OptionYM2612 = new JCheckBox();
        this.rbNukedOPN2OptionDiscrete = new JCheckBox();
        this.rbNukedOPN2OptionASIClp = new JCheckBox();
        this.rbNukedOPN2OptionASIC = new JCheckBox();
        this.tpNSF = new JPanel();
        this.trkbNSFLPF = new JProgressBar();
        this.label53 = new JLabel();
        this.label52 = new JLabel();
        this.trkbNSFHPF = new JProgressBar();
        this.groupBox10 = new JPanel();
        this.cbNSFDmc_DPCMReverse = new JCheckBox();
        this.cbNSFDmc_RandomizeTri = new JCheckBox();
        this.cbNSFDmc_TriMute = new JCheckBox();
        this.cbNSFDmc_RandomizeNoise = new JCheckBox();
        this.cbNSFDmc_DPCMAntiClick = new JCheckBox();
        this.cbNSFDmc_EnablePNoise = new JCheckBox();
        this.cbNSFDmc_Enable4011 = new JCheckBox();
        this.cbNSFDmc_NonLinearMixer = new JCheckBox();
        this.cbNSFDmc_UnmuteOnReset = new JCheckBox();
        this.groupBox12 = new JPanel();
        this.cbNSFN160_Serial = new JCheckBox();
        this.groupBox11 = new JPanel();
        this.cbNSFMmc5_PhaseRefresh = new JCheckBox();
        this.cbNSFMmc5_NonLinearMixer = new JCheckBox();
        this.groupBox9 = new JPanel();
        this.cbNFSNes_DutySwap = new JCheckBox();
        this.cbNFSNes_PhaseRefresh = new JCheckBox();
        this.cbNFSNes_NonLinearMixer = new JCheckBox();
        this.cbNFSNes_UnmuteOnReset = new JCheckBox();
        this.groupBox8 = new JPanel();
        this.label21 = new JLabel();
        this.label20 = new JLabel();
        this.tbNSFFds_LPF = new JTextArea();
        this.cbNFSFds_4085Reset = new JCheckBox();
        this.cbNSFFDSWriteDisable8000 = new JCheckBox();
        this.tpSID = new JPanel();
        this.groupBox28 = new JPanel();
        this.cbSIDModel_Force = new JCheckBox();
        this.rbSIDModel_8580 = new JCheckBox();
        this.rbSIDModel_6581 = new JCheckBox();
        this.groupBox27 = new JPanel();
        this.cbSIDC64Model_Force = new JCheckBox();
        this.rbSIDC64Model_DREAN = new JCheckBox();
        this.rbSIDC64Model_OLDNTSC = new JCheckBox();
        this.rbSIDC64Model_NTSC = new JCheckBox();
        this.rbSIDC64Model_PAL = new JCheckBox();
        this.groupBox14 = new JPanel();
        this.label27 = new JLabel();
        this.label26 = new JLabel();
        this.label25 = new JLabel();
        this.rdSIDQ1 = new JCheckBox();
        this.rdSIDQ3 = new JCheckBox();
        this.rdSIDQ2 = new JCheckBox();
        this.rdSIDQ4 = new JCheckBox();
        this.groupBox13 = new JPanel();
        this.btnSIDBasic = new JButton();
        this.btnSIDCharacter = new JButton();
        this.btnSIDKernal = new JButton();
        this.tbSIDCharacter = new JTextArea();
        this.tbSIDBasic = new JTextArea();
        this.tbSIDKernal = new JTextArea();
        this.label24 = new JLabel();
        this.label23 = new JLabel();
        this.label22 = new JLabel();
        this.tbSIDOutputBufferSize = new JTextArea();
        this.label51 = new JLabel();
        this.label49 = new JLabel();
        this.tpPMDDotNET = new JPanel();
        this.rbPMDManual = new JCheckBox();
        this.rbPMDAuto = new JCheckBox();
        this.btnPMDResetDriverArguments = new JButton();
        this.label54 = new JLabel();
        this.btnPMDResetCompilerArhguments = new JButton();
        this.tbPMDDriverArguments = new JTextArea();
        this.label55 = new JLabel();
        this.tbPMDCompilerArguments = new JTextArea();
        this.gbPMDManual = new JPanel();
        this.cbPMDSetManualVolume = new JCheckBox();
        this.cbPMDUsePPZ8 = new JCheckBox();
        this.groupBox32 = new JPanel();
        this.rbPMD86B = new JCheckBox();
        this.rbPMDSpbB = new JCheckBox();
        this.rbPMDNrmB = new JCheckBox();
        this.cbPMDUsePPSDRV = new JCheckBox();
        this.gbPPSDRV = new JPanel();
        this.groupBox33 = new JPanel();
        this.rbPMDUsePPSDRVManualFreq = new JCheckBox();
        this.label56 = new JLabel();
        this.rbPMDUsePPSDRVFreqDefault = new JCheckBox();
        this.btnPMDPPSDRVManualWait = new JButton();
        this.label57 = new JLabel();
        this.tbPMDPPSDRVFreq = new JTextArea();
        this.label58 = new JLabel();
        this.tbPMDPPSDRVManualWait = new JTextArea();
        this.gbPMDSetManualVolume = new JPanel();
        this.label59 = new JLabel();
        this.label60 = new JLabel();
        this.tbPMDVolumeAdpcm = new JTextArea();
        this.label61 = new JLabel();
        this.tbPMDVolumeRhythm = new JTextArea();
        this.label62 = new JLabel();
        this.tbPMDVolumeSSG = new JTextArea();
        this.label63 = new JLabel();
        this.tbPMDVolumeGIMICSSG = new JTextArea();
        this.label64 = new JLabel();
        this.tbPMDVolumeFM = new JTextArea();
        this.tpMIDIOut = new JPanel();
        this.btnAddVST = new JButton();
        this.tbcMIDIoutList = new JTabbedPane();
        this.tabPage1 = new JPanel();
        this.dgvMIDIoutListA = new JTable();
        this.JListTextBoxColumn1 = new JTextField();
        this.clmIsVST = new JCheckBox();
        this.clmFileName = new JTextArea();
        this.JListTextBoxColumn2 = new JTextArea();
        this.clmType = new JComboBox<>();
        this.ClmBeforeSend = new JComboBox<>();
        this.JListTextBoxColumn3 = new JTextArea();
        this.JListTextBoxColumn4 = new JTextArea();
        this.btnUP_A = new JButton();
        this.btnDOWN_A = new JButton();
        this.tabPage2 = new JPanel();
        this.dgvMIDIoutListB = new JTable();
        this.btnUP_B = new JButton();
        this.btnDOWN_B = new JButton();
        this.tabPage3 = new JPanel();
        this.dgvMIDIoutListC = new JTable();
        this.btnUP_C = new JButton();
        this.btnDOWN_C = new JButton();
        this.tabPage4 = new JPanel();
        this.dgvMIDIoutListD = new JTable();
        this.btnUP_D = new JButton();
        this.btnDOWN_D = new JButton();
        this.tabPage5 = new JPanel();
        this.dgvMIDIoutListE = new JTable();
        this.btnUP_E = new JButton();
        this.btnDOWN_E = new JButton();
        this.tabPage6 = new JPanel();
        this.dgvMIDIoutListF = new JTable();
        this.btnUP_F = new JButton();
        this.btnDOWN_F = new JButton();
        this.tabPage7 = new JPanel();
        this.dgvMIDIoutListG = new JTable();
        this.btnUP_G = new JButton();
        this.btnDOWN_G = new JButton();
        this.tabPage8 = new JPanel();
        this.dgvMIDIoutListH = new JTable();
        this.btnUP_H = new JButton();
        this.btnDOWN_H = new JButton();
        this.tabPage9 = new JPanel();
        this.dgvMIDIoutListI = new JTable();
        this.btnUP_I = new JButton();
        this.btnDOWN_I = new JButton();
        this.tabPage10 = new JPanel();
        this.dgvMIDIoutListJ = new JTable();
        this.button17 = new JButton();
        this.btnDOWN_J = new JButton();
        this.btnSubMIDIout = new JButton();
        this.btnAddMIDIout = new JButton();
        this.label18 = new JLabel();
        this.dgvMIDIoutPallet = new JTable();
        this.clmID = new JTextArea();
        this.clmDeviceName = new JTextArea();
        this.clmManufacturer = new JTextArea();
        this.clmSpacer = new JTextArea();
        this.label16 = new JLabel();
        this.tpMIDIOut2 = new JPanel();
        this.groupBox15 = new JPanel();
        this.btnBeforeSend_Default = new JButton();
        this.tbBeforeSend_Custom = new JTextArea();
        this.tbBeforeSend_XGReset = new JTextArea();
        this.label35 = new JLabel();
        this.label34 = new JLabel();
        this.label32 = new JLabel();
        this.tbBeforeSend_GSReset = new JTextArea();
        this.label33 = new JLabel();
        this.tbBeforeSend_GMReset = new JTextArea();
        this.label31 = new JLabel();
        this.tabMIDIExp = new JPanel();
        this.cbUseMIDIExport = new JCheckBox();
        this.gbMIDIExport = new JPanel();
        this.cbMIDIKeyOnFnum = new JCheckBox();
        this.cbMIDIUseVOPM = new JCheckBox();
        this.groupBox6 = new JPanel();
        this.cbMIDIYM2612 = new JCheckBox();
        this.cbMIDISN76489Sec = new JCheckBox();
        this.cbMIDIYM2612Sec = new JCheckBox();
        this.cbMIDISN76489 = new JCheckBox();
        this.cbMIDIYM2151 = new JCheckBox();
        this.cbMIDIYM2610BSec = new JCheckBox();
        this.cbMIDIYM2151Sec = new JCheckBox();
        this.cbMIDIYM2610B = new JCheckBox();
        this.cbMIDIYM2203 = new JCheckBox();
        this.cbMIDIYM2608Sec = new JCheckBox();
        this.cbMIDIYM2203Sec = new JCheckBox();
        this.cbMIDIYM2608 = new JCheckBox();
        this.cbMIDIPlayless = new JCheckBox();
        this.btnMIDIOutputPath = new JButton();
        this.lblOutputPath = new JLabel();
        this.tbMIDIOutputPath = new JTextArea();
        this.tpMIDIKBD = new JPanel();
        this.cbUseMIDIKeyboard = new JCheckBox();
        this.gbMIDIKeyboard = new JPanel();
        this.pictureBox8 = new JLabel();
        this.pictureBox7 = new JLabel();
        this.pictureBox6 = new JLabel();
        this.pictureBox5 = new JLabel();
        this.pictureBox4 = new JLabel();
        this.pictureBox3 = new JLabel();
        this.pictureBox2 = new JLabel();
        this.pictureBox1 = new JLabel();
        this.tbCCFadeout = new JTextArea();
        this.tbCCPause = new JTextArea();
        this.tbCCSlow = new JTextArea();
        this.tbCCPrevious = new JTextArea();
        this.tbCCNext = new JTextArea();
        this.tbCCFast = new JTextArea();
        this.tbCCStop = new JTextArea();
        this.tbCCPlay = new JTextArea();
        this.tbCCCopyLog = new JTextArea();
        this.label17 = new JLabel();
        this.tbCCDelLog = new JTextArea();
        this.label15 = new JLabel();
        this.tbCCChCopy = new JTextArea();
        this.label8 = new JLabel();
        this.label9 = new JLabel();
        this.gbUseChannel = new JPanel();
        this.rbMONO = new JCheckBox();
        this.rbPOLY = new JCheckBox();
        this.groupBox7 = new JPanel();
        this.rbFM6 = new JCheckBox();
        this.rbFM3 = new JCheckBox();
        this.rbFM5 = new JCheckBox();
        this.rbFM2 = new JCheckBox();
        this.rbFM4 = new JCheckBox();
        this.rbFM1 = new JCheckBox();
        this.groupBox2 = new JPanel();
        this.cbFM1 = new JCheckBox();
        this.cbFM6 = new JCheckBox();
        this.cbFM2 = new JCheckBox();
        this.cbFM5 = new JCheckBox();
        this.cbFM3 = new JCheckBox();
        this.cbFM4 = new JCheckBox();
        this.cmbMIDIIN = new JComboBox<>();
        this.label5 = new JLabel();
        this.tpKeyBoard = new JPanel();
        this.cbUseKeyBoardHook = new JCheckBox();
        this.gbUseKeyBoardHook = new JPanel();
        this.lblKeyBoardHookNotice = new JLabel();
        this.btNextClr = new JButton();
        this.btPrevClr = new JButton();
        this.btPlayClr = new JButton();
        this.btPauseClr = new JButton();
        this.btFastClr = new JButton();
        this.btFadeoutClr = new JButton();
        this.btSlowClr = new JButton();
        this.btStopClr = new JButton();
        this.btNextSet = new JButton();
        this.btPrevSet = new JButton();
        this.btPlaySet = new JButton();
        this.btPauseSet = new JButton();
        this.btFastSet = new JButton();
        this.btFadeoutSet = new JButton();
        this.btSlowSet = new JButton();
        this.btStopSet = new JButton();
        this.label50 = new JLabel();
        this.lblNextKey = new JLabel();
        this.lblFastKey = new JLabel();
        this.lblPlayKey = new JLabel();
        this.lblSlowKey = new JLabel();
        this.lblPrevKey = new JLabel();
        this.lblFadeoutKey = new JLabel();
        this.lblPauseKey = new JLabel();
        this.lblStopKey = new JLabel();
        this.pictureBox14 = new JLabel();
        this.pictureBox17 = new JLabel();
        this.cbNextAlt = new JCheckBox();
        this.pictureBox16 = new JLabel();
        this.cbFastAlt = new JCheckBox();
        this.pictureBox15 = new JLabel();
        this.cbPlayAlt = new JCheckBox();
        this.pictureBox13 = new JLabel();
        this.cbSlowAlt = new JCheckBox();
        this.pictureBox12 = new JLabel();
        this.cbPrevAlt = new JCheckBox();
        this.pictureBox11 = new JLabel();
        this.cbFadeoutAlt = new JCheckBox();
        this.pictureBox10 = new JLabel();
        this.cbPauseAlt = new JCheckBox();
        this.label37 = new JLabel();
        this.cbStopAlt = new JCheckBox();
        this.label45 = new JLabel();
        this.label46 = new JLabel();
        this.label48 = new JLabel();
        this.label38 = new JLabel();
        this.label39 = new JLabel();
        this.label40 = new JLabel();
        this.label41 = new JLabel();
        this.label42 = new JLabel();
        this.cbNextCtrl = new JCheckBox();
        this.label43 = new JLabel();
        this.cbFastCtrl = new JCheckBox();
        this.label44 = new JLabel();
        this.cbPlayCtrl = new JCheckBox();
        this.cbStopShift = new JCheckBox();
        this.cbSlowCtrl = new JCheckBox();
        this.cbPauseShift = new JCheckBox();
        this.cbPrevCtrl = new JCheckBox();
        this.cbFadeoutShift = new JCheckBox();
        this.cbFadeoutCtrl = new JCheckBox();
        this.cbPrevShift = new JCheckBox();
        this.cbPauseCtrl = new JCheckBox();
        this.cbSlowShift = new JCheckBox();
        this.cbStopCtrl = new JCheckBox();
        this.cbPlayShift = new JCheckBox();
        this.cbNextShift = new JCheckBox();
        this.cbFastShift = new JCheckBox();
        this.label47 = new JLabel();
        this.cbStopWin = new JCheckBox();
        this.cbPauseWin = new JCheckBox();
        this.cbFadeoutWin = new JCheckBox();
        this.cbPrevWin = new JCheckBox();
        this.cbSlowWin = new JCheckBox();
        this.cbPlayWin = new JCheckBox();
        this.cbFastWin = new JCheckBox();
        this.cbNextWin = new JCheckBox();
        this.tpBalance = new JPanel();
        this.groupBox25 = new JPanel();
        this.rbAutoBalanceNotSamePositionAsSongData = new JCheckBox();
        this.rbAutoBalanceSamePositionAsSongData = new JCheckBox();
        this.cbAutoBalanceUseThis = new JCheckBox();
        this.groupBox18 = new JPanel();
        this.groupBox24 = new JPanel();
        this.groupBox21 = new JPanel();
        this.rbAutoBalanceNotSaveSongBalance = new JCheckBox();
        this.rbAutoBalanceSaveSongBalance = new JCheckBox();
        this.groupBox22 = new JPanel();
        this.label4 = new JLabel();
        this.groupBox23 = new JPanel();
        this.groupBox19 = new JPanel();
        this.rbAutoBalanceNotLoadSongBalance = new JCheckBox();
        this.rbAutoBalanceLoadSongBalance = new JCheckBox();
        this.groupBox20 = new JPanel();
        this.rbAutoBalanceNotLoadDriverBalance = new JCheckBox();
        this.rbAutoBalanceLoadDriverBalance = new JCheckBox();
        this.tpPlayList = new JPanel();
        this.groupBox17 = new JPanel();
        this.cbAutoOpenImg = new JCheckBox();
        this.tbImageExt = new JTextArea();
        this.cbAutoOpenMML = new JCheckBox();
        this.tbMMLExt = new JTextArea();
        this.tbTextExt = new JTextArea();
        this.cbAutoOpenText = new JCheckBox();
        this.label1 = new JLabel();
        this.label3 = new JLabel();
        this.label2 = new JLabel();
        this.cbEmptyPlayList = new JCheckBox();
        this.tpOther = new JPanel();
        this.btnSearchPath = new JButton();
        this.tbSearchPath = new JTextArea();
        this.label68 = new JLabel();
        this.cbNonRenderingForPause = new JCheckBox();
        this.cbWavSwitch = new JCheckBox();
        this.cbUseGetInst = new JCheckBox();
        this.groupBox4 = new JPanel();
        this.cmbInstFormat = new JComboBox<>();
        this.lblInstFormat = new JLabel();
        this.cbDumpSwitch = new JCheckBox();
        this.gbWav = new JPanel();
        this.btnWavPath = new JButton();
        this.label7 = new JLabel();
        this.tbWavPath = new JTextArea();
        this.gbDump = new JPanel();
        this.btnDumpPath = new JButton();
        this.label6 = new JLabel();
        this.tbDumpPath = new JTextArea();
        this.label30 = new JLabel();
        this.tbScreenFrameRate = new JTextArea();
        this.label29 = new JLabel();
        this.lblLoopTimes = new JLabel();
        this.btnDataPath = new JButton();
        this.tbLoopTimes = new JTextArea();
        this.tbDataPath = new JTextArea();
        this.label19 = new JLabel();
        this.btnResetPosition = new JButton();
        this.btnOpenSettingFolder = new JButton();
        this.cbExALL = new JCheckBox();
        this.cbInitAlways = new JCheckBox();
        this.cbAutoOpen = new JCheckBox();
        this.cbUseLoopTimes = new JCheckBox();
        this.tpOmake = new JPanel();
        this.label67 = new JLabel();
        this.label14 = new JLabel();
        this.btVST = new JButton();
        this.tbSCCbaseAddress = new JTextArea();
        this.tbVST = new JTextArea();
        this.groupBox5 = new JPanel();
        this.cbDispFrameCounter = new JCheckBox();
        this.tpAbout = new JPanel();
        this.tableLayoutPanel = new JTable();
        this.logoBufferedImage = new JLabel();
        this.labelProductName = new JLabel();
        this.labelVersion = new JLabel();
        this.labelCopyright = new JLabel();
        this.labelCompanyName = new JLabel();
        this.textBoxDescription = new JTextArea();
        this.llOpenGithub = new JLabel();

        //
        // btnOK
        //
        this.btnOK.setName("btnOK");
        this.btnOK.addActionListener(this::btnOK_Click);
        //
        // btnCancel
        //
        this.btnCancel.addActionListener(e -> {
            dialogResult = JFileChooser.CANCEL_OPTION;
            setVisible(false);
        });
        this.btnCancel.setName("btnCancel");
        //
        // gbWaveOut
        //
        this.gbWaveOut.add(this.cmbWaveOutDevice);
        this.gbWaveOut.setName("gbWaveOut");
        //
        // cmbWaveOutDevice
        //
        this.cmbWaveOutDevice.setName("cmbWaveOutDevice");
        //
        // rbWaveOut
        //
        this.rbWaveOut.setSelected(true);
        this.rbWaveOut.setName("rbWaveOut");
        this.rbWaveOut.addChangeListener(this::rbWaveOut_CheckedChanged);
        //
        // rbAsioOut
        //
        this.rbAsioOut.setName("rbAsioOut");
        this.rbAsioOut.addChangeListener(this::rbAsioOut_CheckedChanged);
        //
        // rbWasapiOut
        //
        this.rbWasapiOut.setName("rbWasapiOut");
        this.rbWasapiOut.addChangeListener(this::rbWasapiOut_CheckedChanged);
        //
        // gbAsioOut
        //
        this.gbAsioOut.add(this.btnASIOControlPanel);
        this.gbAsioOut.add(this.cmbAsioDevice);
        this.gbAsioOut.setName("gbAsioOut");
        //
        // btnASIOControlPanel
        //
        this.btnASIOControlPanel.setName("btnASIOControlPanel");
        this.btnASIOControlPanel.addActionListener(FormSetting::btnASIOControlPanel_Click);
        //
        // cmbAsioDevice
        //
        this.cmbAsioDevice.setName("cmbAsioDevice");
        //
        // rbDirectSoundOut
        //
        this.rbDirectSoundOut.setName("rbDirectSoundOut");
        this.rbDirectSoundOut.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // gbWasapiOut
        //
        this.gbWasapiOut.add(this.rbExclusive);
        this.gbWasapiOut.add(this.rbShare);
        this.gbWasapiOut.add(this.cmbWasapiDevice);
        this.gbWasapiOut.setName("gbWasapiOut");
        //
        // rbExclusive
        //
        this.rbExclusive.setName("rbExclusive");
        //
        // rbShare
        //
        this.rbShare.setName("rbShare");
        //
        // cmbWasapiDevice
        //
        this.cmbWasapiDevice.setName("cmbWasapiDevice");
        //
        // gbDirectSound
        //
        this.gbDirectSound.add(this.cmbDirectSoundDevice);
        this.gbDirectSound.setName("gbDirectSound");
        //
        // cmbDirectSoundDevice
        //
        this.cmbDirectSoundDevice.setName("cmbDirectSoundDevice");
        //
        // tcSetting
        //
        this.tcSetting.add(this.tpOutput);
        this.tcSetting.add(this.tpModule);
        this.tcSetting.add(this.tpNuked);
        this.tcSetting.add(this.tpNSF);
        this.tcSetting.add(this.tpSID);
        this.tcSetting.add(this.tpPMDDotNET);
        this.tcSetting.add(this.tpMIDIOut);
        this.tcSetting.add(this.tpMIDIOut2);
        this.tcSetting.add(this.tabMIDIExp);
        this.tcSetting.add(this.tpMIDIKBD);
        this.tcSetting.add(this.tpKeyBoard);
        this.tcSetting.add(this.tpBalance);
        this.tcSetting.add(this.tpPlayList);
        this.tcSetting.add(this.tpOther);
        this.tcSetting.add(this.tpOmake);
        this.tcSetting.add(this.tpAbout);
        this.tcSetting.setName("tcSetting");
        this.tcSetting.setSelectedIndex(0);
        //
        // tpOutput
        //
        this.tpOutput.add(this.rbNullDevice);
        this.tpOutput.add(this.label36);
        this.tpOutput.add(this.lblWaitTime);
        this.tpOutput.add(this.label66);
        this.tpOutput.add(this.lblLatencyUnit);
        this.tpOutput.add(this.label28);
        this.tpOutput.add(this.label65);
        this.tpOutput.add(this.lblLatency);
        this.tpOutput.add(this.cmbWaitTime);
        this.tpOutput.add(this.cmbSampleRate);
        this.tpOutput.add(this.cmbLatency);
        this.tpOutput.add(this.rbSPPCM);
        this.tpOutput.add(this.rbDirectSoundOut);
        this.tpOutput.add(this.rbWaveOut);
        this.tpOutput.add(this.rbAsioOut);
        this.tpOutput.add(this.gbWaveOut);
        this.tpOutput.add(this.rbWasapiOut);
        this.tpOutput.add(this.groupBox16);
        this.tpOutput.add(this.gbAsioOut);
        this.tpOutput.add(this.gbDirectSound);
        this.tpOutput.add(this.gbWasapiOut);
        this.tpOutput.setName("tpOutput");
        //
        // rbNullDevice
        //
        this.rbNullDevice.setName("rbNullDevice");
        this.rbNullDevice.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // label36
        //
        this.label36.setName("label36");
        //
        // lblWaitTime
        //
        this.lblWaitTime.setName("lblWaitTime");
        //
        // label66
        //
        this.label66.setName("label66");
        //
        // lblLatencyUnit
        //
        this.lblLatencyUnit.setName("lblLatencyUnit");
        //
        // label28
        //
        this.label28.setName("label28");
        //
        // label65
        //
        this.label65.setName("label65");
        //
        // lblLatency
        //
        this.lblLatency.setName("lblLatency");
        //
        // cmbWaitTime
        //
        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) this.cmbWaitTime.getModel();
        m.addElement(resources.getString("cmbWaitTime.Items"));
        m.addElement(resources.getString("cmbWaitTime.Items1"));
        m.addElement(resources.getString("cmbWaitTime.Items2"));
        m.addElement(resources.getString("cmbWaitTime.Items3"));
        m.addElement(resources.getString("cmbWaitTime.Items4"));
        m.addElement(resources.getString("cmbWaitTime.Items5"));
        m.addElement(resources.getString("cmbWaitTime.Items6"));
        m.addElement(resources.getString("cmbWaitTime.Items7"));
        m.addElement(resources.getString("cmbWaitTime.Items8"));
        m.addElement(resources.getString("cmbWaitTime.Items9"));
        m.addElement(resources.getString("cmbWaitTime.Items10"));
        this.cmbWaitTime.setName("cmbWaitTime");
        //
        // cmbSampleRate
        //
        m = (DefaultComboBoxModel<String>) this.cmbSampleRate.getModel();
        m.addElement(resources.getString("cmbSampleRate.Items"));
        m.addElement(resources.getString("cmbSampleRate.Items1"));
        m.addElement(resources.getString("cmbSampleRate.Items2"));
        m.addElement(resources.getString("cmbSampleRate.Items3"));
        m.addElement(resources.getString("cmbSampleRate.Items4"));
        m.addElement(resources.getString("cmbSampleRate.Items5"));
        m.addElement(resources.getString("cmbSampleRate.Items6"));
        this.cmbSampleRate.setName("cmbSampleRate");
        //
        // cmbLatency
        //
        m = (DefaultComboBoxModel<String>) this.cmbLatency.getModel();
        m.addElement(resources.getString("cmbLatency.Items"));
        m.addElement(resources.getString("cmbLatency.Items1"));
        m.addElement(resources.getString("cmbLatency.Items2"));
        m.addElement(resources.getString("cmbLatency.Items3"));
        m.addElement(resources.getString("cmbLatency.Items4"));
        m.addElement(resources.getString("cmbLatency.Items5"));
        m.addElement(resources.getString("cmbLatency.Items6"));
        m.addElement(resources.getString("cmbLatency.Items7"));
        this.cmbLatency.setName("cmbLatency");
        //
        // rbSPPCM
        //
        this.rbSPPCM.setName("rbSPPCM");
        this.rbSPPCM.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // groupBox16
        //
        this.groupBox16.add(this.cmbSPPCMDevice);
        this.groupBox16.setName("groupBox16");
        //
        // cmbSPPCMDevice
        //
        this.cmbSPPCMDevice.setName("cmbSPPCMDevice");
        //
        // tpModule
        //
        this.tpModule.add(this.groupBox1);
        this.tpModule.add(this.groupBox3);
        this.tpModule.setName("tpModule");
        //
        // groupBox1
        //
        this.groupBox1.add(this.cbUnuseRealChip);
        this.groupBox1.add(this.ucSI);
        this.groupBox1.setName("groupBox1");
        //
        // cbUnuseRealChip
        //
        this.cbUnuseRealChip.setName("cbUnuseRealChip");
        //
        // ucSI
        //
        this.ucSI.setName("ucSI");
        //
        // groupBox3
        //
        this.groupBox3.add(this.cbHiyorimiMode);
        this.groupBox3.add(this.label13);
        this.groupBox3.add(this.label12);
        this.groupBox3.add(this.label11);
        this.groupBox3.add(this.tbLatencyEmu);
        this.groupBox3.add(this.tbLatencySCCI);
        this.groupBox3.add(this.label10);
        this.groupBox3.setName("groupBox3");
        //
        // cbHiyorimiMode
        //
        this.cbHiyorimiMode.setName("cbHiyorimiMode");
        //
        // label13
        //
        this.label13.setName("label13");
        //
        // label12
        //
        this.label12.setName("label12");
        //
        // label11
        //
        this.label11.setName("label11");
        //
        // tbLatencyEmu
        //
        this.tbLatencyEmu.setName("tbLatencyEmu");
        //
        // tbLatencySCCI
        //
        this.tbLatencySCCI.setName("tbLatencySCCI");
        //
        // label10
        //
        this.label10.setName("label10");
        //
        // tpNuked
        //
        this.tpNuked.add(this.groupBox29);
        this.tpNuked.add(this.groupBox26);
        this.tpNuked.setName("tpNuked");
        //
        // groupBox29
        //
        this.groupBox29.add(this.cbGensSSGEG);
        this.groupBox29.add(this.cbGensDACHPF);
        this.groupBox29.setName("groupBox29");
        //
        // cbGensSSGEG
        //
        this.cbGensSSGEG.setName("cbGensSSGEG");
        //
        // cbGensDACHPF
        //
        this.cbGensDACHPF.setName("cbGensDACHPF");
        //
        // groupBox26
        //
        this.groupBox26.add(this.rbNukedOPN2OptionYM2612u);
        this.groupBox26.add(this.rbNukedOPN2OptionYM2612);
        this.groupBox26.add(this.rbNukedOPN2OptionDiscrete);
        this.groupBox26.add(this.rbNukedOPN2OptionASIClp);
        this.groupBox26.add(this.rbNukedOPN2OptionASIC);
        this.groupBox26.setName("groupBox26");
        //
        // rbNukedOPN2OptionYM2612u
        //
        this.rbNukedOPN2OptionYM2612u.setName("rbNukedOPN2OptionYM2612u");
        //
        // rbNukedOPN2OptionYM2612
        //
        this.rbNukedOPN2OptionYM2612.setName("rbNukedOPN2OptionYM2612");
        //
        // rbNukedOPN2OptionDiscrete
        //
        this.rbNukedOPN2OptionDiscrete.setName("rbNukedOPN2OptionDiscrete");
        //
        // rbNukedOPN2OptionASIClp
        //
        this.rbNukedOPN2OptionASIClp.setName("rbNukedOPN2OptionASIClp");
        //
        // rbNukedOPN2OptionASIC
        //
        this.rbNukedOPN2OptionASIC.setName("rbNukedOPN2OptionASIC");
        //
        // tpNSF
        //
        this.tpNSF.add(this.trkbNSFLPF);
        this.tpNSF.add(this.label53);
        this.tpNSF.add(this.label52);
        this.tpNSF.add(this.trkbNSFHPF);
        this.tpNSF.add(this.groupBox10);
        this.tpNSF.add(this.groupBox12);
        this.tpNSF.add(this.groupBox11);
        this.tpNSF.add(this.groupBox9);
        this.tpNSF.add(this.groupBox8);
        this.tpNSF.setName("tpNSF");
        //
        // trkbNSFLPF
        //
        this.trkbNSFLPF.setMaximum(400);
        this.trkbNSFLPF.setName("trkbNSFLPF");
        //
        // label53
        //
        this.label53.setName("label53");
        //
        // label52
        //
        this.label52.setName("label52");
        //
        // trkbNSFHPF
        //
        this.trkbNSFHPF.setMaximum(256);
        this.trkbNSFHPF.setName("trkbNSFHPF");
        //
        // groupBox10
        //
        this.groupBox10.add(this.cbNSFDmc_DPCMReverse);
        this.groupBox10.add(this.cbNSFDmc_RandomizeTri);
        this.groupBox10.add(this.cbNSFDmc_TriMute);
        this.groupBox10.add(this.cbNSFDmc_RandomizeNoise);
        this.groupBox10.add(this.cbNSFDmc_DPCMAntiClick);
        this.groupBox10.add(this.cbNSFDmc_EnablePNoise);
        this.groupBox10.add(this.cbNSFDmc_Enable4011);
        this.groupBox10.add(this.cbNSFDmc_NonLinearMixer);
        this.groupBox10.add(this.cbNSFDmc_UnmuteOnReset);
        this.groupBox10.setName("groupBox10");
        //
        // cbNSFDmc_DPCMReverse
        //
        this.cbNSFDmc_DPCMReverse.setName("cbNSFDmc_DPCMReverse");
        //
        // cbNSFDmc_RandomizeTri
        //
        this.cbNSFDmc_RandomizeTri.setName("cbNSFDmc_RandomizeTri");
        //
        // cbNSFDmc_TriMute
        //
        this.cbNSFDmc_TriMute.setName("cbNSFDmc_TriMute");
        //
        // cbNSFDmc_RandomizeNoise
        //
        this.cbNSFDmc_RandomizeNoise.setName("cbNSFDmc_RandomizeNoise");
        //
        // cbNSFDmc_DPCMAntiClick
        //
        this.cbNSFDmc_DPCMAntiClick.setName("cbNSFDmc_DPCMAntiClick");
        //
        // cbNSFDmc_EnablePNoise
        //
        this.cbNSFDmc_EnablePNoise.setName("cbNSFDmc_EnablePNoise");
        //
        // cbNSFDmc_Enable4011
        //
        this.cbNSFDmc_Enable4011.setName("cbNSFDmc_Enable4011");
        //
        // cbNSFDmc_NonLinearMixer
        //
        this.cbNSFDmc_NonLinearMixer.setName("cbNSFDmc_NonLinearMixer");
        //
        // cbNSFDmc_UnmuteOnReset
        //
        this.cbNSFDmc_UnmuteOnReset.setName("cbNSFDmc_UnmuteOnReset");
        //
        // groupBox12
        //
        this.groupBox12.add(this.cbNSFN160_Serial);
        this.groupBox12.setName("groupBox12");
        //
        // cbNSFN160_Serial
        //
        this.cbNSFN160_Serial.setName("cbNSFN160_Serial");
        //
        // groupBox11
        //
        this.groupBox11.add(this.cbNSFMmc5_PhaseRefresh);
        this.groupBox11.add(this.cbNSFMmc5_NonLinearMixer);
        this.groupBox11.setName("groupBox11");
        //
        // cbNSFMmc5_PhaseRefresh
        //
        this.cbNSFMmc5_PhaseRefresh.setName("cbNSFMmc5_PhaseRefresh");
        //
        // cbNSFMmc5_NonLinearMixer
        //
        this.cbNSFMmc5_NonLinearMixer.setName("cbNSFMmc5_NonLinearMixer");
        //
        // groupBox9
        //
        this.groupBox9.add(this.cbNFSNes_DutySwap);
        this.groupBox9.add(this.cbNFSNes_PhaseRefresh);
        this.groupBox9.add(this.cbNFSNes_NonLinearMixer);
        this.groupBox9.add(this.cbNFSNes_UnmuteOnReset);
        this.groupBox9.setName("groupBox9");
        //
        // cbNFSNes_DutySwap
        //
        this.cbNFSNes_DutySwap.setName("cbNFSNes_DutySwap");
        //
        // cbNFSNes_PhaseRefresh
        //
        this.cbNFSNes_PhaseRefresh.setName("cbNFSNes_PhaseRefresh");
        //
        // cbNFSNes_NonLinearMixer
        //
        this.cbNFSNes_NonLinearMixer.setName("cbNFSNes_NonLinearMixer");
        //
        // cbNFSNes_UnmuteOnReset
        //
        this.cbNFSNes_UnmuteOnReset.setName("cbNFSNes_UnmuteOnReset");
        //
        // groupBox8
        //
        this.groupBox8.add(this.label21);
        this.groupBox8.add(this.label20);
        this.groupBox8.add(this.tbNSFFds_LPF);
        this.groupBox8.add(this.cbNFSFds_4085Reset);
        this.groupBox8.add(this.cbNSFFDSWriteDisable8000);
        this.groupBox8.setName("groupBox8");
        //
        // label21
        //
        this.label21.setName("label21");
        //
        // label20
        //
        this.label20.setName("label20");
        //
        // tbNSFFds_LPF
        //
        this.tbNSFFds_LPF.setName("tbNSFFds_LPF");
        //
        // cbNFSFds_4085Reset
        //
        this.cbNFSFds_4085Reset.setName("cbNFSFds_4085Reset");
        //
        // cbNSFFDSWriteDisable8000
        //
        this.cbNSFFDSWriteDisable8000.setName("cbNSFFDSWriteDisable8000");
        //
        // tpSID
        //
        this.tpSID.add(this.groupBox28);
        this.tpSID.add(this.groupBox27);
        this.tpSID.add(this.groupBox14);
        this.tpSID.add(this.groupBox13);
        this.tpSID.add(this.tbSIDOutputBufferSize);
        this.tpSID.add(this.label51);
        this.tpSID.add(this.label49);
        this.tpSID.setName("tpSID");
        //
        // groupBox28
        //
        this.groupBox28.add(this.cbSIDModel_Force);
        this.groupBox28.add(this.rbSIDModel_8580);
        this.groupBox28.add(this.rbSIDModel_6581);
        this.groupBox28.setName("groupBox28");
        //
        // cbSIDModel_Force
        //
        this.cbSIDModel_Force.setName("cbSIDModel_Force");
        //
        // rbSIDModel_8580
        //
        this.rbSIDModel_8580.setName("rbSIDModel_8580");
        //
        // rbSIDModel_6581
        //
        this.rbSIDModel_6581.setSelected(true);
        this.rbSIDModel_6581.setName("rbSIDModel_6581");
        //
        // groupBox27
        //
        this.groupBox27.add(this.cbSIDC64Model_Force);
        this.groupBox27.add(this.rbSIDC64Model_DREAN);
        this.groupBox27.add(this.rbSIDC64Model_OLDNTSC);
        this.groupBox27.add(this.rbSIDC64Model_NTSC);
        this.groupBox27.add(this.rbSIDC64Model_PAL);
        this.groupBox27.setName("groupBox27");
        //
        // cbSIDC64Model_Force
        //
        this.cbSIDC64Model_Force.setName("cbSIDC64Model_Force");
        //
        // rbSIDC64Model_DREAN
        //
        this.rbSIDC64Model_DREAN.setName("rbSIDC64Model_DREAN");
        //
        // rbSIDC64Model_OLDNTSC
        //
        this.rbSIDC64Model_OLDNTSC.setName("rbSIDC64Model_OLDNTSC");
        //
        // rbSIDC64Model_NTSC
        //
        this.rbSIDC64Model_NTSC.setName("rbSIDC64Model_NTSC");
        //
        // rbSIDC64Model_PAL
        //
        this.rbSIDC64Model_PAL.setSelected(true);
        this.rbSIDC64Model_PAL.setName("rbSIDC64Model_PAL");
        //
        // groupBox14
        //
        this.groupBox14.add(this.label27);
        this.groupBox14.add(this.label26);
        this.groupBox14.add(this.label25);
        this.groupBox14.add(this.rdSIDQ1);
        this.groupBox14.add(this.rdSIDQ3);
        this.groupBox14.add(this.rdSIDQ2);
        this.groupBox14.add(this.rdSIDQ4);
        this.groupBox14.setName("groupBox14");
        //
        // label27
        //
        this.label27.setName("label27");
        //
        // label26
        //
        this.label26.setName("label26");
        //
        // label25
        //
        this.label25.setName("label25");
        //
        // rdSIDQ1
        //
        this.rdSIDQ1.setSelected(true);
        this.rdSIDQ1.setName("rdSIDQ1");
        //
        // rdSIDQ3
        //
        this.rdSIDQ3.setName("rdSIDQ3");
        //
        // rdSIDQ2
        //
        this.rdSIDQ2.setName("rdSIDQ2");
        //
        // rdSIDQ4
        //
        this.rdSIDQ4.setName("rdSIDQ4");
        //
        // groupBox13
        //
        this.groupBox13.add(this.btnSIDBasic);
        this.groupBox13.add(this.btnSIDCharacter);
        this.groupBox13.add(this.btnSIDKernal);
        this.groupBox13.add(this.tbSIDCharacter);
        this.groupBox13.add(this.tbSIDBasic);
        this.groupBox13.add(this.tbSIDKernal);
        this.groupBox13.add(this.label24);
        this.groupBox13.add(this.label23);
        this.groupBox13.add(this.label22);
        this.groupBox13.setName("groupBox13");
        //
        // btnSIDBasic
        //
        this.btnSIDBasic.setName("btnSIDBasic");
        this.btnSIDBasic.addActionListener(this::btnSIDBasic_Click);
        //
        // btnSIDCharacter
        //
        this.btnSIDCharacter.setName("btnSIDCharacter");
        this.btnSIDCharacter.addActionListener(this::btnSIDCharacter_Click);
        //
        // btnSIDKernal
        //
        this.btnSIDKernal.setName("btnSIDKernal");
        this.btnSIDKernal.addActionListener(this::btnSIDKernal_Click);
        //
        // tbSIDCharacter
        //
        this.tbSIDCharacter.setName("tbSIDCharacter");
        //
        // tbSIDBasic
        //
        this.tbSIDBasic.setName("tbSIDBasic");
        //
        // tbSIDKernal
        //
        this.tbSIDKernal.setName("tbSIDKernal");
        //
        // label24
        //
        this.label24.setName("label24");
        //
        // label23
        //
        this.label23.setName("label23");
        //
        // label22
        //
        this.label22.setName("label22");
        //
        // tbSIDOutputBufferSize
        //
        this.tbSIDOutputBufferSize.setName("tbSIDOutputBufferSize");
        //
        // label51
        //
        this.label51.setName("label51");
        //
        // label49
        //
        this.label49.setName("label49");
        //
        // tpPMDDotNET
        //
        this.tpPMDDotNET.add(this.rbPMDManual);
        this.tpPMDDotNET.add(this.rbPMDAuto);
        this.tpPMDDotNET.add(this.btnPMDResetDriverArguments);
        this.tpPMDDotNET.add(this.label54);
        this.tpPMDDotNET.add(this.btnPMDResetCompilerArhguments);
        this.tpPMDDotNET.add(this.tbPMDDriverArguments);
        this.tpPMDDotNET.add(this.label55);
        this.tpPMDDotNET.add(this.tbPMDCompilerArguments);
        this.tpPMDDotNET.add(this.gbPMDManual);
        this.tpPMDDotNET.setName("tpPMDDotNET");
        //
        // rbPMDManual
        //
        this.rbPMDManual.setName("rbPMDManual");
        this.rbPMDManual.addChangeListener(this::rbPMDManual_CheckedChanged);
        //
        // rbPMDAuto
        //
        this.rbPMDAuto.setName("rbPMDAuto");
        //
        // btnPMDResetDriverArguments
        //
        this.btnPMDResetDriverArguments.setName("btnPMDResetDriverArguments");
        this.btnPMDResetDriverArguments.addActionListener(this::btnPMDResetDriverArguments_Click);
        //
        // label54
        //
        this.label54.setName("label54");
        //
        // btnPMDResetCompilerArhguments
        //
        this.btnPMDResetCompilerArhguments.setName("btnPMDResetCompilerArhguments");
        this.btnPMDResetCompilerArhguments.addActionListener(this::btnPMDResetCompilerArhguments_Click);
        //
        // tbPMDDriverArguments
        //
        this.tbPMDDriverArguments.setName("tbPMDDriverArguments");
        //
        // label55
        //
        this.label55.setName("label55");
        //
        // tbPMDCompilerArguments
        //
        this.tbPMDCompilerArguments.setName("tbPMDCompilerArguments");
        //
        // gbPMDManual
        //
        this.gbPMDManual.add(this.cbPMDSetManualVolume);
        this.gbPMDManual.add(this.cbPMDUsePPZ8);
        this.gbPMDManual.add(this.groupBox32);
        this.gbPMDManual.add(this.cbPMDUsePPSDRV);
        this.gbPMDManual.add(this.gbPPSDRV);
        this.gbPMDManual.add(this.gbPMDSetManualVolume);
        this.gbPMDManual.setName("gbPMDManual");
        //
        // cbPMDSetManualVolume
        //
        this.cbPMDSetManualVolume.setName("cbPMDSetManualVolume");
        this.cbPMDSetManualVolume.addChangeListener(this::cbPMDSetManualVolume_CheckedChanged);
        //
        // cbPMDUsePPZ8
        //
        this.cbPMDUsePPZ8.setName("cbPMDUsePPZ8");
        //
        // groupBox32
        //
        this.groupBox32.add(this.rbPMD86B);
        this.groupBox32.add(this.rbPMDSpbB);
        this.groupBox32.add(this.rbPMDNrmB);
        this.groupBox32.setName("groupBox32");
        //
        // rbPMD86B
        //
        this.rbPMD86B.setName("rbPMD86B");
        //
        // rbPMDSpbB
        //
        this.rbPMDSpbB.setName("rbPMDSpbB");
        //
        // rbPMDNrmB
        //
        this.rbPMDNrmB.setName("rbPMDNrmB");
        //
        // cbPMDUsePPSDRV
        //
        this.cbPMDUsePPSDRV.setName("cbPMDUsePPSDRV");
        this.cbPMDUsePPSDRV.addChangeListener(this::cbPMDUsePPSDRV_CheckedChanged);
        //
        // gbPPSDRV
        //
        this.gbPPSDRV.add(this.groupBox33);
        this.gbPPSDRV.setName("gbPPSDRV");
        //
        // groupBox33
        //
        this.groupBox33.add(this.rbPMDUsePPSDRVManualFreq);
        this.groupBox33.add(this.label56);
        this.groupBox33.add(this.rbPMDUsePPSDRVFreqDefault);
        this.groupBox33.add(this.btnPMDPPSDRVManualWait);
        this.groupBox33.add(this.label57);
        this.groupBox33.add(this.tbPMDPPSDRVFreq);
        this.groupBox33.add(this.label58);
        this.groupBox33.add(this.tbPMDPPSDRVManualWait);
        this.groupBox33.setName("groupBox33");
        //
        // rbPMDUsePPSDRVManualFreq
        //
        this.rbPMDUsePPSDRVManualFreq.setName("rbPMDUsePPSDRVManualFreq");
        this.rbPMDUsePPSDRVManualFreq.addChangeListener(this::rbPMDUsePPSDRVManualFreq_CheckedChanged);
        //
        // label56
        //
        this.label56.setName("label56");
        //
        // rbPMDUsePPSDRVFreqDefault
        //
        this.rbPMDUsePPSDRVFreqDefault.setName("rbPMDUsePPSDRVFreqDefault");
        //
        // btnPMDPPSDRVManualWait
        //
        this.btnPMDPPSDRVManualWait.setName("btnPMDPPSDRVManualWait");
        this.btnPMDPPSDRVManualWait.addActionListener(this::btnPMDPPSDRVManualWait_Click);
        //
        // label57
        //
        this.label57.setName("label57");
        //
        // tbPMDPPSDRVFreq
        //
        this.tbPMDPPSDRVFreq.setName("tbPMDPPSDRVFreq");
        this.tbPMDPPSDRVFreq.addFocusListener(this.tbPMDPPSDRVFreq_Click);
        this.tbPMDPPSDRVFreq.addMouseListener(this.tbPMDPPSDRVFreq_MouseClick);
        //
        // label58
        //
        this.label58.setName("label58");
        //
        // tbPMDPPSDRVManualWait
        //
        this.tbPMDPPSDRVManualWait.setName("tbPMDPPSDRVManualWait");
        //
        // gbPMDSetManualVolume
        //
        this.gbPMDSetManualVolume.add(this.label59);
        this.gbPMDSetManualVolume.add(this.label60);
        this.gbPMDSetManualVolume.add(this.tbPMDVolumeAdpcm);
        this.gbPMDSetManualVolume.add(this.label61);
        this.gbPMDSetManualVolume.add(this.tbPMDVolumeRhythm);
        this.gbPMDSetManualVolume.add(this.label62);
        this.gbPMDSetManualVolume.add(this.tbPMDVolumeSSG);
        this.gbPMDSetManualVolume.add(this.label63);
        this.gbPMDSetManualVolume.add(this.tbPMDVolumeGIMICSSG);
        this.gbPMDSetManualVolume.add(this.label64);
        this.gbPMDSetManualVolume.add(this.tbPMDVolumeFM);
        this.gbPMDSetManualVolume.setName("gbPMDSetManualVolume");
        //
        // label59
        //
        this.label59.setName("label59");
        //
        // label60
        //
        this.label60.setName("label60");
        //
        // tbPMDVolumeAdpcm
        //
        this.tbPMDVolumeAdpcm.setName("tbPMDVolumeAdpcm");
        //
        // label61
        //
        this.label61.setName("label61");
        //
        // tbPMDVolumeRhythm
        //
        this.tbPMDVolumeRhythm.setName("tbPMDVolumeRhythm");
        //
        // label62
        //
        this.label62.setName("label62");
        //
        // tbPMDVolumeSSG
        //
        this.tbPMDVolumeSSG.setName("tbPMDVolumeSSG");
        //
        // label63
        //
        this.label63.setName("label63");
        //
        // tbPMDVolumeGIMICSSG
        //
        this.tbPMDVolumeGIMICSSG.setName("tbPMDVolumeGIMICSSG");
        //
        // label64
        //
        this.label64.setName("label64");
        //
        // tbPMDVolumeFM
        //
        this.tbPMDVolumeFM.setName("tbPMDVolumeFM");
        //
        // tpMIDIOut
        //
        this.tpMIDIOut.add(this.btnAddVST);
        this.tpMIDIOut.add(this.tbcMIDIoutList);
        this.tpMIDIOut.add(this.btnSubMIDIout);
        this.tpMIDIOut.add(this.btnAddMIDIout);
        this.tpMIDIOut.add(this.label18);
        this.tpMIDIOut.add(this.dgvMIDIoutPallet);
        this.tpMIDIOut.add(this.label16);
        this.tpMIDIOut.setName("tpMIDIOut");
        //
        // btnAddVST
        //
        this.btnAddVST.setName("btnAddVST");
        this.btnAddVST.addActionListener(this::btnAddVST_Click);
        //
        // tbcMIDIoutList
        //
        this.tbcMIDIoutList.add(this.tabPage1);
        this.tbcMIDIoutList.add(this.tabPage2);
        this.tbcMIDIoutList.add(this.tabPage3);
        this.tbcMIDIoutList.add(this.tabPage4);
        this.tbcMIDIoutList.add(this.tabPage5);
        this.tbcMIDIoutList.add(this.tabPage6);
        this.tbcMIDIoutList.add(this.tabPage7);
        this.tbcMIDIoutList.add(this.tabPage8);
        this.tbcMIDIoutList.add(this.tabPage9);
        this.tbcMIDIoutList.add(this.tabPage10);
        this.tbcMIDIoutList.setName("tbcMIDIoutList");
        this.tbcMIDIoutList.setSelectedIndex(0);
        //
        // tabPage1
        //
        this.tabPage1.add(this.dgvMIDIoutListA);
        this.tabPage1.add(this.btnUP_A);
        this.tabPage1.add(this.btnDOWN_A);
        this.tabPage1.setName("tabPage1");
        //
        // dgvMIDIoutListA
        //
        this.dgvMIDIoutListA.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListA.setName("dgvMIDIoutListA");
        //
        // JListTextBoxColumn1
        //
        this.JListTextBoxColumn1.setEditable(false);
        this.JListTextBoxColumn1.setName("JListTextBoxColumn1");
        //
        // clmIsVST
        //
        this.clmIsVST.setName("clmIsVST");
        //
        // clmFileName
        //
        this.clmFileName.setName("clmFileName");
        //
        // JListTextBoxColumn2
        //
        this.JListTextBoxColumn2.setName("JListTextBoxColumn2");
        this.JListTextBoxColumn2.setEditable(true);
        //
        // clmType
        //
        this.clmType.setModel(new DefaultComboBoxModel<>(new String[] {
                "GM",
                "XG",
                "GS",
                "LA",
                "GS(SC-55_1)",
                "GS(SC-55_2)"}));
        this.clmType.setName("clmType");
        //
        // ClmBeforeSend
        //
        this.ClmBeforeSend.setModel(new DefaultComboBoxModel<>(new String[] {
                "None",
                "GM Reset",
                "XG Reset",
                "GS Reset",
                "Custom"}));
        this.ClmBeforeSend.setName("ClmBeforeSend");
        //
        // JListTextBoxColumn3
        //
        this.JListTextBoxColumn3.setName("JListTextBoxColumn3");
        this.JListTextBoxColumn3.setEditable(true);
        //
        // JListTextBoxColumn4
        //
        this.JListTextBoxColumn4.setName("JListTextBoxColumn4");
        this.JListTextBoxColumn4.setEditable(true);
        //
        // btnUP_A
        //
        this.btnUP_A.setName("btnUP_A");
        this.btnUP_A.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_A
        //
        this.btnDOWN_A.setName("btnDOWN_A");
        this.btnDOWN_A.addActionListener(this::btnDOWN_Click);
        //
        // tabPage2
        //
        this.tabPage2.add(this.dgvMIDIoutListB);
        this.tabPage2.add(this.btnUP_B);
        this.tabPage2.add(this.btnDOWN_B);
        this.tabPage2.setName("tabPage2");
        //
        // dgvMIDIoutListB
        //
        this.dgvMIDIoutListB.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListB.setName("dgvMIDIoutListB");
        //
        // btnUP_B
        //
        this.btnUP_B.setName("btnUP_B");
        this.btnUP_B.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_B
        //
        this.btnDOWN_B.setName("btnDOWN_B");
        this.btnDOWN_B.addActionListener(this::btnDOWN_Click);
        //
        // tabPage3
        //
        this.tabPage3.add(this.dgvMIDIoutListC);
        this.tabPage3.add(this.btnUP_C);
        this.tabPage3.add(this.btnDOWN_C);
        this.tabPage3.setName("tabPage3");
        //
        // dgvMIDIoutListC
        //
        this.dgvMIDIoutListC.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListC.setName("dgvMIDIoutListC");
        //
        // btnUP_C
        //
        this.btnUP_C.setName("btnUP_C");
        this.btnUP_C.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_C
        //
        this.btnDOWN_C.setName("btnDOWN_C");
        this.btnDOWN_C.addActionListener(this::btnDOWN_Click);
        //
        // tabPage4
        //
        this.tabPage4.add(this.dgvMIDIoutListD);
        this.tabPage4.add(this.btnUP_D);
        this.tabPage4.add(this.btnDOWN_D);
        this.tabPage4.setName("tabPage4");
        //
        // dgvMIDIoutListD
        //
        this.dgvMIDIoutListD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListD.setName("dgvMIDIoutListD");
        //
        // btnUP_D
        //
        this.btnUP_D.setName("btnUP_D");
        this.btnUP_D.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_D
        //
        this.btnDOWN_D.setName("btnDOWN_D");
        this.btnDOWN_D.addActionListener(this::btnDOWN_Click);
        //
        // tabPage5
        //
        this.tabPage5.add(this.dgvMIDIoutListE);
        this.tabPage5.add(this.btnUP_E);
        this.tabPage5.add(this.btnDOWN_E);
        this.tabPage5.setName("tabPage5");
        //
        // dgvMIDIoutListE
        //
        this.dgvMIDIoutListE.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListE.setName("dgvMIDIoutListE");
        //
        // btnUP_E
        //
        this.btnUP_E.setName("btnUP_E");
        this.btnUP_E.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_E
        //
        this.btnDOWN_E.setName("btnDOWN_E");
        this.btnDOWN_E.addActionListener(this::btnDOWN_Click);
        //
        // tabPage6
        //
        this.tabPage6.add(this.dgvMIDIoutListF);
        this.tabPage6.add(this.btnUP_F);
        this.tabPage6.add(this.btnDOWN_F);
        this.tabPage6.setName("tabPage6");
        //
        // dgvMIDIoutListF
        //
        this.dgvMIDIoutListF.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListF.setName("dgvMIDIoutListF");
        //
        // btnUP_F
        //
        this.btnUP_F.setName("btnUP_F");
        this.btnUP_F.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_F
        //
        this.btnDOWN_F.setName("btnDOWN_F");
        this.btnDOWN_F.addActionListener(this::btnDOWN_Click);
        //
        // tabPage7
        //
        this.tabPage7.add(this.dgvMIDIoutListG);
        this.tabPage7.add(this.btnUP_G);
        this.tabPage7.add(this.btnDOWN_G);
        this.tabPage7.setName("tabPage7");
        //
        // dgvMIDIoutListG
        //
        this.dgvMIDIoutListG.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListG.setName("dgvMIDIoutListG");
        //
        // btnUP_G
        //
        this.btnUP_G.setName("btnUP_G");
        this.btnUP_G.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_G
        //
        this.btnDOWN_G.setName("btnDOWN_G");
        this.btnDOWN_G.addActionListener(this::btnDOWN_Click);
        //
        // tabPage8
        //
        this.tabPage8.add(this.dgvMIDIoutListH);
        this.tabPage8.add(this.btnUP_H);
        this.tabPage8.add(this.btnDOWN_H);
        this.tabPage8.setName("tabPage8");
        //
        // dgvMIDIoutListH
        //
        this.dgvMIDIoutListH.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListH.setName("dgvMIDIoutListH");
        //
        // btnUP_H
        //
        this.btnUP_H.setName("btnUP_H");
        this.btnUP_H.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_H
        //
        this.btnDOWN_H.setName("btnDOWN_H");
        this.btnDOWN_H.addActionListener(this::btnDOWN_Click);
        //
        // tabPage9
        //
        this.tabPage9.add(this.dgvMIDIoutListI);
        this.tabPage9.add(this.btnUP_I);
        this.tabPage9.add(this.btnDOWN_I);
        this.tabPage9.setName("tabPage9");
        //
        // dgvMIDIoutListI
        //
        this.dgvMIDIoutListI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListI.setName("dgvMIDIoutListI");
        //
        // btnUP_I
        //
        this.btnUP_I.setName("btnUP_I");
        this.btnUP_I.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_I
        //
        this.btnDOWN_I.setName("btnDOWN_I");
        this.btnDOWN_I.addActionListener(this::btnDOWN_Click);
        //
        // tabPage10
        //
        this.tabPage10.add(this.dgvMIDIoutListJ);
        this.tabPage10.add(this.button17);
        this.tabPage10.add(this.btnDOWN_J);
        this.tabPage10.setName("tabPage10");
        //
        // dgvMIDIoutListJ
        //
        this.dgvMIDIoutListJ.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListJ.setName("dgvMIDIoutListJ");
        //
        // button17
        //
        this.button17.setName("button17");
        this.button17.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_J
        //
        this.btnDOWN_J.setName("btnDOWN_J");
        this.btnDOWN_J.addActionListener(this::btnDOWN_Click);
        //
        // btnSubMIDIout
        //
        this.btnSubMIDIout.setName("btnSubMIDIout");
        this.btnSubMIDIout.addActionListener(this::btnSubMIDIout_Click);
        //
        // btnAddMIDIout
        //
        this.btnAddMIDIout.setName("btnAddMIDIout");
        this.btnAddMIDIout.addActionListener(this::btnAddMIDIout_Click);
        //
        // label18
        //
        this.label18.setName("label18");
        //
        // dgvMIDIoutPallet
        //
        this.dgvMIDIoutPallet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutPallet.setName("dgvMIDIoutPallet");
        //
        // clmID
        //
        this.clmID.setName("clmID");
        this.clmID.setEditable(true);
        //
        // clmDeviceName
        //
        this.clmDeviceName.setName("clmDeviceName");
        this.clmDeviceName.setEditable(true);
        //
        // clmManufacturer
        //
        this.clmManufacturer.setName("clmManufacturer");
        this.clmManufacturer.setEditable(true);
        //
        // clmSpacer
        //
        this.clmSpacer.setName("clmSpacer");
        this.clmSpacer.setEditable(true);
        //
        // label16
        //
        this.label16.setName("label16");
        //
        // tpMIDIOut2
        //
        this.tpMIDIOut2.add(this.groupBox15);
        this.tpMIDIOut2.setName("tpMIDIOut2");
        //
        // groupBox15
        //
        this.groupBox15.add(this.btnBeforeSend_Default);
        this.groupBox15.add(this.tbBeforeSend_Custom);
        this.groupBox15.add(this.tbBeforeSend_XGReset);
        this.groupBox15.add(this.label35);
        this.groupBox15.add(this.label34);
        this.groupBox15.add(this.label32);
        this.groupBox15.add(this.tbBeforeSend_GSReset);
        this.groupBox15.add(this.label33);
        this.groupBox15.add(this.tbBeforeSend_GMReset);
        this.groupBox15.add(this.label31);
        this.groupBox15.setName("groupBox15");
        //
        // btnBeforeSend_Default
        //
        this.btnBeforeSend_Default.setName("btnBeforeSend_Default");
        this.btnBeforeSend_Default.addActionListener(this::btnBeforeSend_Default_Click);
        //
        // tbBeforeSend_Custom
        //
        this.tbBeforeSend_Custom.setName("tbBeforeSend_Custom");
        //
        // tbBeforeSend_XGReset
        //
        this.tbBeforeSend_XGReset.setName("tbBeforeSend_XGReset");
        //
        // label35
        //
        this.label35.setName("label35");
        //
        // label34
        //
        this.label34.setName("label34");
        //
        // label32
        //
        this.label32.setName("label32");
        //
        // tbBeforeSend_GSReset
        //
        this.tbBeforeSend_GSReset.setName("tbBeforeSend_GSReset");
        //
        // label33
        //
        this.label33.setName("label33");
        //
        // tbBeforeSend_GMReset
        //
        this.tbBeforeSend_GMReset.setName("tbBeforeSend_GMReset");
        //
        // label31
        //
        this.label31.setName("label31");
        //
        // tabMIDIExp
        //
        this.tabMIDIExp.add(this.cbUseMIDIExport);
        this.tabMIDIExp.add(this.gbMIDIExport);
        this.tabMIDIExp.setName("tabMIDIExp");
        //
        // cbUseMIDIExport
        //
        this.cbUseMIDIExport.setName("cbUseMIDIExport");
        this.cbUseMIDIExport.addChangeListener(this::cbUseMIDIExport_CheckedChanged);
        //
        // gbMIDIExport
        //
        this.gbMIDIExport.add(this.cbMIDIKeyOnFnum);
        this.gbMIDIExport.add(this.cbMIDIUseVOPM);
        this.gbMIDIExport.add(this.groupBox6);
        this.gbMIDIExport.add(this.cbMIDIPlayless);
        this.gbMIDIExport.add(this.btnMIDIOutputPath);
        this.gbMIDIExport.add(this.lblOutputPath);
        this.gbMIDIExport.add(this.tbMIDIOutputPath);
        this.gbMIDIExport.setName("gbMIDIExport");
        //
        // cbMIDIKeyOnFnum
        //
        this.cbMIDIKeyOnFnum.setName("cbMIDIKeyOnFnum");
        //
        // cbMIDIUseVOPM
        //
        this.cbMIDIUseVOPM.setName("cbMIDIUseVOPM");
        //
        // groupBox6
        //
        this.groupBox6.add(this.cbMIDIYM2612);
        this.groupBox6.add(this.cbMIDISN76489Sec);
        this.groupBox6.add(this.cbMIDIYM2612Sec);
        this.groupBox6.add(this.cbMIDISN76489);
        this.groupBox6.add(this.cbMIDIYM2151);
        this.groupBox6.add(this.cbMIDIYM2610BSec);
        this.groupBox6.add(this.cbMIDIYM2151Sec);
        this.groupBox6.add(this.cbMIDIYM2610B);
        this.groupBox6.add(this.cbMIDIYM2203);
        this.groupBox6.add(this.cbMIDIYM2608Sec);
        this.groupBox6.add(this.cbMIDIYM2203Sec);
        this.groupBox6.add(this.cbMIDIYM2608);
        this.groupBox6.setName("groupBox6");
        //
        // cbMIDIYM2612
        //
        this.cbMIDIYM2612.setSelected(true);
        this.cbMIDIYM2612.setName("cbMIDIYM2612");
        //
        // cbMIDISN76489Sec
        //
        this.cbMIDISN76489Sec.setName("cbMIDISN76489Sec");
        //
        // cbMIDIYM2612Sec
        //
        this.cbMIDIYM2612Sec.setName("cbMIDIYM2612Sec");
        //
        // cbMIDISN76489
        //
        this.cbMIDISN76489.setName("cbMIDISN76489");
        //
        // cbMIDIYM2151
        //
        this.cbMIDIYM2151.setName("cbMIDIYM2151");
        //
        // cbMIDIYM2610BSec
        //
        this.cbMIDIYM2610BSec.setName("cbMIDIYM2610BSec");
        //
        // cbMIDIYM2151Sec
        //
        this.cbMIDIYM2151Sec.setName("cbMIDIYM2151Sec");
        //
        // cbMIDIYM2610B
        //
        this.cbMIDIYM2610B.setName("cbMIDIYM2610B");
        //
        // cbMIDIYM2203
        //
        this.cbMIDIYM2203.setName("cbMIDIYM2203");
        //
        // cbMIDIYM2608Sec
        //
        this.cbMIDIYM2608Sec.setName("cbMIDIYM2608Sec");
        //
        // cbMIDIYM2203Sec
        //
        this.cbMIDIYM2203Sec.setName("cbMIDIYM2203Sec");
        //
        // cbMIDIYM2608
        //
        this.cbMIDIYM2608.setName("cbMIDIYM2608");
        //
        // cbMIDIPlayless
        //
        this.cbMIDIPlayless.setName("cbMIDIPlayless");
        //
        // btnMIDIOutputPath
        //
        this.btnMIDIOutputPath.setName("btnMIDIOutputPath");
        this.btnMIDIOutputPath.addActionListener(this::btnMIDIOutputPath_Click);
        //
        // lblOutputPath
        //
        this.lblOutputPath.setName("lblOutputPath");
        //
        // tbMIDIOutputPath
        //
        this.tbMIDIOutputPath.setName("tbMIDIOutputPath");
        //
        // tpMIDIKBD
        //
        this.tpMIDIKBD.add(this.cbUseMIDIKeyboard);
        this.tpMIDIKBD.add(this.gbMIDIKeyboard);
        this.tpMIDIKBD.setName("tpMIDIKBD");
        //
        // cbUseMIDIKeyboard
        //
        this.cbUseMIDIKeyboard.setName("cbUseMIDIKeyboard");
        this.cbUseMIDIKeyboard.addChangeListener(this::cbUseMIDIKeyboard_CheckedChanged);
        //
        // gbMIDIKeyboard
        //
        this.gbMIDIKeyboard.add(this.pictureBox8);
        this.gbMIDIKeyboard.add(this.pictureBox7);
        this.gbMIDIKeyboard.add(this.pictureBox6);
        this.gbMIDIKeyboard.add(this.pictureBox5);
        this.gbMIDIKeyboard.add(this.pictureBox4);
        this.gbMIDIKeyboard.add(this.pictureBox3);
        this.gbMIDIKeyboard.add(this.pictureBox2);
        this.gbMIDIKeyboard.add(this.pictureBox1);
        this.gbMIDIKeyboard.add(this.tbCCFadeout);
        this.gbMIDIKeyboard.add(this.tbCCPause);
        this.gbMIDIKeyboard.add(this.tbCCSlow);
        this.gbMIDIKeyboard.add(this.tbCCPrevious);
        this.gbMIDIKeyboard.add(this.tbCCNext);
        this.gbMIDIKeyboard.add(this.tbCCFast);
        this.gbMIDIKeyboard.add(this.tbCCStop);
        this.gbMIDIKeyboard.add(this.tbCCPlay);
        this.gbMIDIKeyboard.add(this.tbCCCopyLog);
        this.gbMIDIKeyboard.add(this.label17);
        this.gbMIDIKeyboard.add(this.tbCCDelLog);
        this.gbMIDIKeyboard.add(this.label15);
        this.gbMIDIKeyboard.add(this.tbCCChCopy);
        this.gbMIDIKeyboard.add(this.label8);
        this.gbMIDIKeyboard.add(this.label9);
        this.gbMIDIKeyboard.add(this.gbUseChannel);
        this.gbMIDIKeyboard.add(this.cmbMIDIIN);
        this.gbMIDIKeyboard.add(this.label5);
        this.gbMIDIKeyboard.setName("gbMIDIKeyboard");
        //
        // pictureBox8
        //
        this.pictureBox8.setIcon(new ImageIcon(Common.getImage("ccNext")));
        this.pictureBox8.setName("pictureBox8");
        //
        // pictureBox7
        //
        this.pictureBox7.setIcon(new ImageIcon(Common.getImage("ccFast")));
        this.pictureBox7.setName("pictureBox7");
        //
        // pictureBox6
        //
        this.pictureBox6.setIcon(new ImageIcon(Common.getImage("ccPlay")));
        this.pictureBox6.setName("pictureBox6");
        //
        // pictureBox5
        //
        this.pictureBox5.setIcon(new ImageIcon(Common.getImage("ccSlow")));
        this.pictureBox5.setName("pictureBox5");
        //
        // pictureBox4
        //
        this.pictureBox4.setIcon(new ImageIcon(Common.getImage("ccStop")));
        this.pictureBox4.setName("pictureBox4");
        //
        // pictureBox3
        //
        this.pictureBox3.setIcon(new ImageIcon(Common.getImage("ccPause")));
        this.pictureBox3.setName("pictureBox3");
        //
        // pictureBox2
        //
        this.pictureBox2.setIcon(new ImageIcon(Common.getImage("ccPrevious")));
        this.pictureBox2.setName("pictureBox2");
        //
        // pictureBox1
        //
        this.pictureBox1.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.pictureBox1.setName("pictureBox1");
        //
        // tbCCFadeout
        //
        this.tbCCFadeout.setName("tbCCFadeout");
        //
        // tbCCPause
        //
        this.tbCCPause.setName("tbCCPause");
        //
        // tbCCSlow
        //
        this.tbCCSlow.setName("tbCCSlow");
        //
        // tbCCPrevious
        //
        this.tbCCPrevious.setName("tbCCPrevious");
        //
        // tbCCNext
        //
        this.tbCCNext.setName("tbCCNext");
        //
        // tbCCFast
        //
        this.tbCCFast.setName("tbCCFast");
        //
        // tbCCStop
        //
        this.tbCCStop.setName("tbCCStop");
        //
        // tbCCPlay
        //
        this.tbCCPlay.setName("tbCCPlay");
        //
        // tbCCCopyLog
        //
        this.tbCCCopyLog.setName("tbCCCopyLog");
        //
        // label17
        //
        this.label17.setName("label17");
        //
        // tbCCDelLog
        //
        this.tbCCDelLog.setName("tbCCDelLog");
        //
        // label15
        //
        this.label15.setName("label15");
        //
        // tbCCChCopy
        //
        this.tbCCChCopy.setName("tbCCChCopy");
        //
        // label8
        //
        this.label8.setName("label8");
        //
        // label9
        //
        this.label9.setName("label9");
        //
        // gbUseChannel
        //
        this.gbUseChannel.add(this.rbMONO);
        this.gbUseChannel.add(this.rbPOLY);
        this.gbUseChannel.add(this.groupBox7);
        this.gbUseChannel.add(this.groupBox2);
        this.gbUseChannel.setName("gbUseChannel");
        //
        // rbMONO
        //
        this.rbMONO.setSelected(true);
        this.rbMONO.setName("rbMONO");
        //
        // rbPOLY
        //
        this.rbPOLY.setName("rbPOLY");
        //
        // groupBox7
        //
        this.groupBox7.add(this.rbFM6);
        this.groupBox7.add(this.rbFM3);
        this.groupBox7.add(this.rbFM5);
        this.groupBox7.add(this.rbFM2);
        this.groupBox7.add(this.rbFM4);
        this.groupBox7.add(this.rbFM1);
        this.groupBox7.setName("groupBox7");
        //
        // rbFM6
        //
        this.rbFM6.setName("rbFM6");
        //
        // rbFM3
        //
        this.rbFM3.setName("rbFM3");
        //
        // rbFM5
        //
        this.rbFM5.setName("rbFM5");
        //
        // rbFM2
        //
        this.rbFM2.setName("rbFM2");
        //
        // rbFM4
        //
        this.rbFM4.setName("rbFM4");
        //
        // rbFM1
        //
        this.rbFM1.setSelected(true);
        this.rbFM1.setName("rbFM1");
        //
        // groupBox2
        //
        this.groupBox2.add(this.cbFM1);
        this.groupBox2.add(this.cbFM6);
        this.groupBox2.add(this.cbFM2);
        this.groupBox2.add(this.cbFM5);
        this.groupBox2.add(this.cbFM3);
        this.groupBox2.add(this.cbFM4);
        this.groupBox2.setName("groupBox2");
        //
        // cbFM1
        //
        this.cbFM1.setSelected(true);
        this.cbFM1.setName("cbFM1");
        //
        // cbFM6
        //
        this.cbFM6.setSelected(true);
        this.cbFM6.setName("cbFM6");
        //
        // cbFM2
        //
        this.cbFM2.setSelected(true);
        this.cbFM2.setName("cbFM2");
        //
        // cbFM5
        //
        this.cbFM5.setSelected(true);
        this.cbFM5.setName("cbFM5");
        //
        // cbFM3
        //
        this.cbFM3.setSelected(true);
        this.cbFM3.setName("cbFM3");
        //
        // cbFM4
        //
        this.cbFM4.setSelected(true);
        this.cbFM4.setName("cbFM4");
        //
        // cmbMIDIIN
        //
        this.cmbMIDIIN.setName("cmbMIDIIN");
        //
        // label5
        //
        this.label5.setName("label5");
        //
        // tpKeyBoard
        //
        this.tpKeyBoard.add(this.cbUseKeyBoardHook);
        this.tpKeyBoard.add(this.gbUseKeyBoardHook);
        this.tpKeyBoard.add(this.label47);
        this.tpKeyBoard.add(this.cbStopWin);
        this.tpKeyBoard.add(this.cbPauseWin);
        this.tpKeyBoard.add(this.cbFadeoutWin);
        this.tpKeyBoard.add(this.cbPrevWin);
        this.tpKeyBoard.add(this.cbSlowWin);
        this.tpKeyBoard.add(this.cbPlayWin);
        this.tpKeyBoard.add(this.cbFastWin);
        this.tpKeyBoard.add(this.cbNextWin);
        this.tpKeyBoard.setName("tpKeyBoard");
        //
        // cbUseKeyBoardHook
        //
        this.cbUseKeyBoardHook.setName("cbUseKeyBoardHook");
        this.cbUseKeyBoardHook.addChangeListener(this::cbUseKeyBoardHook_CheckedChanged);
        //
        // gbUseKeyBoardHook
        //
        this.gbUseKeyBoardHook.add(this.lblKeyBoardHookNotice);
        this.gbUseKeyBoardHook.add(this.btNextClr);
        this.gbUseKeyBoardHook.add(this.btPrevClr);
        this.gbUseKeyBoardHook.add(this.btPlayClr);
        this.gbUseKeyBoardHook.add(this.btPauseClr);
        this.gbUseKeyBoardHook.add(this.btFastClr);
        this.gbUseKeyBoardHook.add(this.btFadeoutClr);
        this.gbUseKeyBoardHook.add(this.btSlowClr);
        this.gbUseKeyBoardHook.add(this.btStopClr);
        this.gbUseKeyBoardHook.add(this.btNextSet);
        this.gbUseKeyBoardHook.add(this.btPrevSet);
        this.gbUseKeyBoardHook.add(this.btPlaySet);
        this.gbUseKeyBoardHook.add(this.btPauseSet);
        this.gbUseKeyBoardHook.add(this.btFastSet);
        this.gbUseKeyBoardHook.add(this.btFadeoutSet);
        this.gbUseKeyBoardHook.add(this.btSlowSet);
        this.gbUseKeyBoardHook.add(this.btStopSet);
        this.gbUseKeyBoardHook.add(this.label50);
        this.gbUseKeyBoardHook.add(this.lblNextKey);
        this.gbUseKeyBoardHook.add(this.lblFastKey);
        this.gbUseKeyBoardHook.add(this.lblPlayKey);
        this.gbUseKeyBoardHook.add(this.lblSlowKey);
        this.gbUseKeyBoardHook.add(this.lblPrevKey);
        this.gbUseKeyBoardHook.add(this.lblFadeoutKey);
        this.gbUseKeyBoardHook.add(this.lblPauseKey);
        this.gbUseKeyBoardHook.add(this.lblStopKey);
        this.gbUseKeyBoardHook.add(this.pictureBox14);
        this.gbUseKeyBoardHook.add(this.pictureBox17);
        this.gbUseKeyBoardHook.add(this.cbNextAlt);
        this.gbUseKeyBoardHook.add(this.pictureBox16);
        this.gbUseKeyBoardHook.add(this.cbFastAlt);
        this.gbUseKeyBoardHook.add(this.pictureBox15);
        this.gbUseKeyBoardHook.add(this.cbPlayAlt);
        this.gbUseKeyBoardHook.add(this.pictureBox13);
        this.gbUseKeyBoardHook.add(this.cbSlowAlt);
        this.gbUseKeyBoardHook.add(this.pictureBox12);
        this.gbUseKeyBoardHook.add(this.cbPrevAlt);
        this.gbUseKeyBoardHook.add(this.pictureBox11);
        this.gbUseKeyBoardHook.add(this.cbFadeoutAlt);
        this.gbUseKeyBoardHook.add(this.pictureBox10);
        this.gbUseKeyBoardHook.add(this.cbPauseAlt);
        this.gbUseKeyBoardHook.add(this.label37);
        this.gbUseKeyBoardHook.add(this.cbStopAlt);
        this.gbUseKeyBoardHook.add(this.label45);
        this.gbUseKeyBoardHook.add(this.label46);
        this.gbUseKeyBoardHook.add(this.label48);
        this.gbUseKeyBoardHook.add(this.label38);
        this.gbUseKeyBoardHook.add(this.label39);
        this.gbUseKeyBoardHook.add(this.label40);
        this.gbUseKeyBoardHook.add(this.label41);
        this.gbUseKeyBoardHook.add(this.label42);
        this.gbUseKeyBoardHook.add(this.cbNextCtrl);
        this.gbUseKeyBoardHook.add(this.label43);
        this.gbUseKeyBoardHook.add(this.cbFastCtrl);
        this.gbUseKeyBoardHook.add(this.label44);
        this.gbUseKeyBoardHook.add(this.cbPlayCtrl);
        this.gbUseKeyBoardHook.add(this.cbStopShift);
        this.gbUseKeyBoardHook.add(this.cbSlowCtrl);
        this.gbUseKeyBoardHook.add(this.cbPauseShift);
        this.gbUseKeyBoardHook.add(this.cbPrevCtrl);
        this.gbUseKeyBoardHook.add(this.cbFadeoutShift);
        this.gbUseKeyBoardHook.add(this.cbFadeoutCtrl);
        this.gbUseKeyBoardHook.add(this.cbPrevShift);
        this.gbUseKeyBoardHook.add(this.cbPauseCtrl);
        this.gbUseKeyBoardHook.add(this.cbSlowShift);
        this.gbUseKeyBoardHook.add(this.cbStopCtrl);
        this.gbUseKeyBoardHook.add(this.cbPlayShift);
        this.gbUseKeyBoardHook.add(this.cbNextShift);
        this.gbUseKeyBoardHook.add(this.cbFastShift);
        this.gbUseKeyBoardHook.setName("gbUseKeyBoardHook");
        //
        // lblKeyBoardHookNotice
        //
        this.lblKeyBoardHookNotice.setForeground(Color.red);
        this.lblKeyBoardHookNotice.setName("lblKeyBoardHookNotice");
        //
        // btNextClr
        //
        this.btNextClr.setName("btNextClr");
        this.btNextClr.addActionListener(this::btNextClr_Click);
        //
        // btPrevClr
        //
        this.btPrevClr.setName("btPrevClr");
        this.btPrevClr.addActionListener(this::btPrevClr_Click);
        //
        // btPlayClr
        //
        this.btPlayClr.setName("btPlayClr");
        this.btPlayClr.addActionListener(this::btPlayClr_Click);
        //
        // btPauseClr
        //
        this.btPauseClr.setName("btPauseClr");
        this.btPauseClr.addActionListener(this::btPauseClr_Click);
        //
        // btFastClr
        //
        this.btFastClr.setName("btFastClr");
        this.btFastClr.addActionListener(this::btFastClr_Click);
        //
        // btFadeoutClr
        //
        this.btFadeoutClr.setName("btFadeoutClr");
        this.btFadeoutClr.addActionListener(this::btFadeoutClr_Click);
        //
        // btSlowClr
        //
        this.btSlowClr.setName("btSlowClr");
        this.btSlowClr.addActionListener(this::btSlowClr_Click);
        //
        // btStopClr
        //
        this.btStopClr.setName("btStopClr");
        this.btStopClr.addActionListener(this::btStopClr_Click);
        //
        // btNextSet
        //
        this.btNextSet.setName("btNextSet");
        this.btNextSet.addActionListener(this::btNextSet_Click);
        //
        // btPrevSet
        //
        this.btPrevSet.setName("btPrevSet");
        this.btPrevSet.addActionListener(this::btPrevSet_Click);
        //
        // btPlaySet
        //
        this.btPlaySet.setName("btPlaySet");
        this.btPlaySet.addActionListener(this::btPlaySet_Click);
        //
        // btPauseSet
        //
        this.btPauseSet.setName("btPauseSet");
        this.btPauseSet.addActionListener(this::btPauseSet_Click);
        //
        // btFastSet
        //
        this.btFastSet.setName("btFastSet");
        this.btFastSet.addActionListener(this::btFastSet_Click);
        //
        // btFadeoutSet
        //
        this.btFadeoutSet.setName("btFadeoutSet");
        this.btFadeoutSet.addActionListener(this::btFadeoutSet_Click);
        //
        // btSlowSet
        //
        this.btSlowSet.setName("btSlowSet");
        this.btSlowSet.addActionListener(this::btSlowSet_Click);
        //
        // btStopSet
        //
        this.btStopSet.setName("btStopSet");
        this.btStopSet.addActionListener(this::btStopSet_Click);
        //
        // label50
        //
        this.label50.setName("label50");
        //
        // lblNextKey
        //
        this.lblNextKey.setName("lblNextKey");
        //
        // lblFastKey
        //
        this.lblFastKey.setName("lblFastKey");
        //
        // lblPlayKey
        //
        this.lblPlayKey.setName("lblPlayKey");
        //
        // lblSlowKey
        //
        this.lblSlowKey.setName("lblSlowKey");
        //
        // lblPrevKey
        //
        this.lblPrevKey.setName("lblPrevKey");
        //
        // lblFadeoutKey
        //
        this.lblFadeoutKey.setName("lblFadeoutKey");
        //
        // lblPauseKey
        //
        this.lblPauseKey.setName("lblPauseKey");
        //
        // lblStopKey
        //
        this.lblStopKey.setName("lblStopKey");
        //
        // pictureBox14
        //
        this.pictureBox14.setIcon(new ImageIcon(Common.getImage("ccStop")));
        this.pictureBox14.setName("pictureBox14");
        //
        // pictureBox17
        //
        this.pictureBox17.setIcon(new ImageIcon(Common.getImage("ccFadeout")));
        this.pictureBox17.setName("pictureBox17");
        //
        // cbNextAlt
        //
        this.cbNextAlt.setName("cbNextAlt");
        //
        // pictureBox16
        //
        this.pictureBox16.setIcon(new ImageIcon(Common.getImage("ccPrevious")));
        this.pictureBox16.setName("pictureBox16");
        //
        // cbFastAlt
        //
        this.cbFastAlt.setName("cbFastAlt");
        //
        // pictureBox15
        //
        this.pictureBox15.setIcon(new ImageIcon(Common.getImage("ccPause")));
        this.pictureBox15.setName("pictureBox15");
        //
        // cbPlayAlt
        //
        this.cbPlayAlt.setName("cbPlayAlt");
        //
        // pictureBox13
        //
        this.pictureBox13.setIcon(new ImageIcon(Common.getImage("ccSlow")));
        this.pictureBox13.setName("pictureBox13");
        //
        // cbSlowAlt
        //
        this.cbSlowAlt.setName("cbSlowAlt");
        //
        // pictureBox12
        //
        this.pictureBox12.setIcon(new ImageIcon(Common.getImage("ccPlay")));
        this.pictureBox12.setName("pictureBox12");
        //
        // cbPrevAlt
        //
        this.cbPrevAlt.setName("cbPrevAlt");
        //
        // pictureBox11
        //
        this.pictureBox11.setIcon(new ImageIcon(Common.getImage("ccFast")));
        this.pictureBox11.setName("pictureBox11");
        //
        // cbFadeoutAlt
        //
        this.cbFadeoutAlt.setName("cbFadeoutAlt");
        //
        // pictureBox10
        //
        this.pictureBox10.setIcon(new ImageIcon(Common.getImage("ccNext")));
        this.pictureBox10.setName("pictureBox10");
        //
        // cbPauseAlt
        //
        this.cbPauseAlt.setName("cbPauseAlt");
        //
        // label37
        //
        this.label37.setName("label37");
        //
        // cbStopAlt
        //
        this.cbStopAlt.setName("cbStopAlt");
        //
        // label45
        //
        this.label45.setName("label45");
        //
        // label46
        //
        this.label46.setName("label46");
        //
        // label48
        //
        this.label48.setName("label48");
        //
        // label38
        //
        this.label38.setName("label38");
        //
        // label39
        //
        this.label39.setName("label39");
        //
        // label40
        //
        this.label40.setName("label40");
        //
        // label41
        //
        this.label41.setName("label41");
        //
        // label42
        //
        this.label42.setName("label42");
        //
        // cbNextCtrl
        //
        this.cbNextCtrl.setName("cbNextCtrl");
        //
        // label43
        //
        this.label43.setName("label43");
        //
        // cbFastCtrl
        //
        this.cbFastCtrl.setName("cbFastCtrl");
        //
        // label44
        //
        this.label44.setName("label44");
        //
        // cbPlayCtrl
        //
        this.cbPlayCtrl.setName("cbPlayCtrl");
        //
        // cbStopShift
        //
        this.cbStopShift.setName("cbStopShift");
        //
        // cbSlowCtrl
        //
        this.cbSlowCtrl.setName("cbSlowCtrl");
        //
        // cbPauseShift
        //
        this.cbPauseShift.setName("cbPauseShift");
        //
        // cbPrevCtrl
        //
        this.cbPrevCtrl.setName("cbPrevCtrl");
        //
        // cbFadeoutShift
        //
        this.cbFadeoutShift.setName("cbFadeoutShift");
        //
        // cbFadeoutCtrl
        //
        this.cbFadeoutCtrl.setName("cbFadeoutCtrl");
        //
        // cbPrevShift
        //
        this.cbPrevShift.setName("cbPrevShift");
        //
        // cbPauseCtrl
        //
        this.cbPauseCtrl.setName("cbPauseCtrl");
        //
        // cbSlowShift
        //
        this.cbSlowShift.setName("cbSlowShift");
        //
        // cbStopCtrl
        //
        this.cbStopCtrl.setName("cbStopCtrl");
        //
        // cbPlayShift
        //
        this.cbPlayShift.setName("cbPlayShift");
        //
        // cbNextShift
        //
        this.cbNextShift.setName("cbNextShift");
        //
        // cbFastShift
        //
        this.cbFastShift.setName("cbFastShift");
        //
        // label47
        //
        this.label47.setName("label47");
        //
        // cbStopWin
        //
        this.cbStopWin.setName("cbStopWin");
        //
        // cbPauseWin
        //
        this.cbPauseWin.setName("cbPauseWin");
        //
        // cbFadeoutWin
        //
        this.cbFadeoutWin.setName("cbFadeoutWin");
        //
        // cbPrevWin
        //
        this.cbPrevWin.setName("cbPrevWin");
        //
        // cbSlowWin
        //
        this.cbSlowWin.setName("cbSlowWin");
        //
        // cbPlayWin
        //
        this.cbPlayWin.setName("cbPlayWin");
        //
        // cbFastWin
        //
        this.cbFastWin.setName("cbFastWin");
        //
        // cbNextWin
        //
        this.cbNextWin.setName("cbNextWin");
        //
        // tpBalance
        //
        this.tpBalance.add(this.groupBox25);
        this.tpBalance.add(this.cbAutoBalanceUseThis);
        this.tpBalance.add(this.groupBox18);
        this.tpBalance.setName("tpBalance");
        //
        // groupBox25
        //
        this.groupBox25.add(this.rbAutoBalanceNotSamePositionAsSongData);
        this.groupBox25.add(this.rbAutoBalanceSamePositionAsSongData);
        this.groupBox25.setName("groupBox25");
        //
        // rbAutoBalanceNotSamePositionAsSongData
        //
        this.rbAutoBalanceNotSamePositionAsSongData.setSelected(true);
        this.rbAutoBalanceNotSamePositionAsSongData.setName("rbAutoBalanceNotSamePositionAsSongData");
        //
        // rbAutoBalanceSamePositionAsSongData
        //
        this.rbAutoBalanceSamePositionAsSongData.setName("rbAutoBalanceSamePositionAsSongData");
        //
        // cbAutoBalanceUseThis
        //
        this.cbAutoBalanceUseThis.setSelected(true);
        this.cbAutoBalanceUseThis.setName("cbAutoBalanceUseThis");
        //
        // groupBox18
        //
        this.groupBox18.add(this.groupBox24);
        this.groupBox18.add(this.groupBox23);
        this.groupBox18.setName("groupBox18");
        //
        // groupBox24
        //
        this.groupBox24.add(this.groupBox21);
        this.groupBox24.add(this.groupBox22);
        this.groupBox24.setName("groupBox24");
        //
        // groupBox21
        //
        this.groupBox21.add(this.rbAutoBalanceNotSaveSongBalance);
        this.groupBox21.add(this.rbAutoBalanceSaveSongBalance);
        this.groupBox21.setName("groupBox21");
        //
        // rbAutoBalanceNotSaveSongBalance
        //
        this.rbAutoBalanceNotSaveSongBalance.setSelected(true);
        this.rbAutoBalanceNotSaveSongBalance.setName("rbAutoBalanceNotSaveSongBalance");
        //
        // rbAutoBalanceSaveSongBalance
        //
        this.rbAutoBalanceSaveSongBalance.setName("rbAutoBalanceSaveSongBalance");
        //
        // groupBox22
        //
        this.groupBox22.add(this.label4);
        this.groupBox22.setName("groupBox22");
        //
        // label4
        //
        this.label4.setName("label4");
        //
        // groupBox23
        //
        this.groupBox23.add(this.groupBox19);
        this.groupBox23.add(this.groupBox20);
        this.groupBox23.setName("groupBox23");
        //
        // groupBox19
        //
        this.groupBox19.add(this.rbAutoBalanceNotLoadSongBalance);
        this.groupBox19.add(this.rbAutoBalanceLoadSongBalance);
        this.groupBox19.setName("groupBox19");
        //
        // rbAutoBalanceNotLoadSongBalance
        //
        this.rbAutoBalanceNotLoadSongBalance.setSelected(true);
        this.rbAutoBalanceNotLoadSongBalance.setName("rbAutoBalanceNotLoadSongBalance");
        //
        // rbAutoBalanceLoadSongBalance
        //
        this.rbAutoBalanceLoadSongBalance.setName("rbAutoBalanceLoadSongBalance");
        //
        // groupBox20
        //
        this.groupBox20.add(this.rbAutoBalanceNotLoadDriverBalance);
        this.groupBox20.add(this.rbAutoBalanceLoadDriverBalance);
        this.groupBox20.setName("groupBox20");
        this.groupBox20.addFocusListener(this.groupBox20_Enter);
        //
        // rbAutoBalanceNotLoadDriverBalance
        //
        this.rbAutoBalanceNotLoadDriverBalance.setName("rbAutoBalanceNotLoadDriverBalance");
        //
        // rbAutoBalanceLoadDriverBalance
        //
        this.rbAutoBalanceLoadDriverBalance.setSelected(true);
        this.rbAutoBalanceLoadDriverBalance.setName("rbAutoBalanceLoadDriverBalance");
        //
        // tpPlayList
        //
        this.tpPlayList.add(this.groupBox17);
        this.tpPlayList.add(this.cbEmptyPlayList);
        this.tpPlayList.setName("tpPlayList");
        //
        // groupBox17
        //
        this.groupBox17.add(this.cbAutoOpenImg);
        this.groupBox17.add(this.tbImageExt);
        this.groupBox17.add(this.cbAutoOpenMML);
        this.groupBox17.add(this.tbMMLExt);
        this.groupBox17.add(this.tbTextExt);
        this.groupBox17.add(this.cbAutoOpenText);
        this.groupBox17.add(this.label1);
        this.groupBox17.add(this.label3);
        this.groupBox17.add(this.label2);
        this.groupBox17.setName("groupBox17");
        //
        // cbAutoOpenImg
        //
        this.cbAutoOpenImg.setName("cbAutoOpenImg");
        this.cbAutoOpenImg.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // tbImageExt
        //
        this.tbImageExt.setName("tbImageExt");
        //
        // cbAutoOpenMML
        //
        this.cbAutoOpenMML.setName("cbAutoOpenMML");
        this.cbAutoOpenMML.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // tbMMLExt
        //
        this.tbMMLExt.setName("tbMMLExt");
        //
        // tbTextExt
        //
        this.tbTextExt.setName("tbTextExt");
        //
        // cbAutoOpenText
        //
        this.cbAutoOpenText.setName("cbAutoOpenText");
        this.cbAutoOpenText.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // label1
        //
        this.label1.setName("label1");
        //
        // label3
        //
        this.label3.setName("label3");
        //
        // label2
        //
        this.label2.setName("label2");
        //
        // cbEmptyPlayList
        //
        this.cbEmptyPlayList.setName("cbEmptyPlayList");
        this.cbEmptyPlayList.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // tpOther
        //
        this.tpOther.add(this.btnSearchPath);
        this.tpOther.add(this.tbSearchPath);
        this.tpOther.add(this.label68);
        this.tpOther.add(this.cbNonRenderingForPause);
        this.tpOther.add(this.cbWavSwitch);
        this.tpOther.add(this.cbUseGetInst);
        this.tpOther.add(this.groupBox4);
        this.tpOther.add(this.cbDumpSwitch);
        this.tpOther.add(this.gbWav);
        this.tpOther.add(this.gbDump);
        this.tpOther.add(this.label30);
        this.tpOther.add(this.tbScreenFrameRate);
        this.tpOther.add(this.label29);
        this.tpOther.add(this.lblLoopTimes);
        this.tpOther.add(this.btnDataPath);
        this.tpOther.add(this.tbLoopTimes);
        this.tpOther.add(this.tbDataPath);
        this.tpOther.add(this.label19);
        this.tpOther.add(this.btnResetPosition);
        this.tpOther.add(this.btnOpenSettingFolder);
        this.tpOther.add(this.cbExALL);
        this.tpOther.add(this.cbInitAlways);
        this.tpOther.add(this.cbAutoOpen);
        this.tpOther.add(this.cbUseLoopTimes);
        this.tpOther.setName("tpOther");
        //
        // btnSearchPath
        //
        this.btnSearchPath.setName("btnSearchPath");
        this.btnSearchPath.addActionListener(this::btnSearchPath_Click);
        //
        // tbSearchPath
        //
        this.tbSearchPath.setName("tbSearchPath");
        //
        // label68
        //
        this.label68.setName("label68");
        //
        // cbNonRenderingForPause
        //
        this.cbNonRenderingForPause.setName("cbNonRenderingForPause");
        //
        // cbWavSwitch
        //
        this.cbWavSwitch.setName("cbWavSwitch");
        this.cbWavSwitch.addChangeListener(this::cbWavSwitch_CheckedChanged);
        //
        // cbUseGetInst
        //
        this.cbUseGetInst.setName("cbUseGetInst");
        this.cbUseGetInst.addChangeListener(this::cbUseGetInst_CheckedChanged);
        //
        // groupBox4
        //
        this.groupBox4.add(this.cmbInstFormat);
        this.groupBox4.add(this.lblInstFormat);
        this.groupBox4.setName("groupBox4");
        //
        // cmbInstFormat
        //
        m = ((DefaultComboBoxModel<String>) this.cmbInstFormat.getModel());
        m.addElement(resources.getString("cmbInstFormat.Items"));
        m.addElement(resources.getString("cmbInstFormat.Items1"));
        m.addElement(resources.getString("cmbInstFormat.Items2"));
        m.addElement(resources.getString("cmbInstFormat.Items3"));
        m.addElement(resources.getString("cmbInstFormat.Items4"));
        m.addElement(resources.getString("cmbInstFormat.Items5"));
        m.addElement(resources.getString("cmbInstFormat.Items6"));
        m.addElement(resources.getString("cmbInstFormat.Items7"));
        m.addElement(resources.getString("cmbInstFormat.Items8"));
        m.addElement(resources.getString("cmbInstFormat.Items9"));
        m.addElement(resources.getString("cmbInstFormat.Items10"));
        m.addElement(resources.getString("cmbInstFormat.Items11"));
        m.addElement(resources.getString("cmbInstFormat.Items12"));
        m.addElement(resources.getString("cmbInstFormat.Items13"));
        m.addElement(resources.getString("cmbInstFormat.Items14"));
        m.addElement(resources.getString("cmbInstFormat.Items15"));
        m.addElement(resources.getString("cmbInstFormat.Items16"));
        this.cmbInstFormat.setName("cmbInstFormat");
        //
        // lblInstFormat
        //
        this.lblInstFormat.setName("lblInstFormat");
        //
        // cbDumpSwitch
        //
        this.cbDumpSwitch.setName("cbDumpSwitch");
        this.cbDumpSwitch.addChangeListener(this::cbDumpSwitch_CheckedChanged);
        //
        // gbWav
        //
        this.gbWav.add(this.btnWavPath);
        this.gbWav.add(this.label7);
        this.gbWav.add(this.tbWavPath);
        this.gbWav.setName("gbWav");
        //
        // btnWavPath
        //
        this.btnWavPath.setName("btnWavPath");
        this.btnWavPath.addActionListener(this::btnWavPath_Click);
        //
        // label7
        //
        this.label7.setName("label7");
        //
        // tbWavPath
        //
        this.tbWavPath.setName("tbWavPath");
        //
        // gbDump
        //
        this.gbDump.add(this.btnDumpPath);
        this.gbDump.add(this.label6);
        this.gbDump.add(this.tbDumpPath);
        this.gbDump.setName("gbDump");
        //
        // btnDumpPath
        //
        this.btnDumpPath.setName("btnDumpPath");
        this.btnDumpPath.addActionListener(this::btnDumpPath_Click);
        //
        // label6
        //
        this.label6.setName("label6");
        //
        // tbDumpPath
        //
        this.tbDumpPath.setName("tbDumpPath");
        //
        // label30
        //
        this.label30.setName("label30");
        //
        // tbScreenFrameRate
        //
        this.tbScreenFrameRate.setName("tbScreenFrameRate");
        //
        // label29
        //
        this.label29.setName("label29");
        //
        // lblLoopTimes
        //
        this.lblLoopTimes.setName("lblLoopTimes");
        //
        // btnDataPath
        //
        this.btnDataPath.setName("btnDataPath");
        this.btnDataPath.addActionListener(this::btnDataPath_Click);
        //
        // tbLoopTimes
        //
        this.tbLoopTimes.setName("tbLoopTimes");
        //
        // tbDataPath
        //
        this.tbDataPath.setName("tbDataPath");
        //
        // label19
        //
        this.label19.setName("label19");
        //
        // btnResetPosition
        //
        this.btnResetPosition.setName("btnResetPosition");
        this.btnResetPosition.addActionListener(this::btnResetPosition_Click);
        //
        // btnOpenSettingFolder
        //
        this.btnOpenSettingFolder.setName("btnOpenSettingFolder");
        this.btnOpenSettingFolder.addActionListener(FormSetting::btnOpenSettingFolder_Click);
        //
        // cbExALL
        //
        this.cbExALL.setName("cbExALL");
        this.cbExALL.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // cbInitAlways
        //
        this.cbInitAlways.setName("cbInitAlways");
        this.cbInitAlways.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // cbAutoOpen
        //
        this.cbAutoOpen.setName("cbAutoOpen");
        this.cbAutoOpen.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // cbUseLoopTimes
        //
        this.cbUseLoopTimes.setName("cbUseLoopTimes");
        this.cbUseLoopTimes.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // tpOmake
        //
        this.tpOmake.add(this.label67);
        this.tpOmake.add(this.label14);
        this.tpOmake.add(this.btVST);
        this.tpOmake.add(this.tbSCCbaseAddress);
        this.tpOmake.add(this.tbVST);
        this.tpOmake.add(this.groupBox5);
        this.tpOmake.setName("tpOmake");
        //
        // label67
        //
        this.label67.setName("label67");
        //
        // label14
        //
        this.label14.setName("label14");
        //
        // btVST
        //
        this.btVST.setName("btVST");
        this.btVST.addActionListener(this::btVST_Click);
        //
        // tbSCCbaseAddress
        //
        this.tbSCCbaseAddress.setName("tbSCCbaseAddress");
        //
        // tbVST
        //
        this.tbVST.setName("tbVST");
        //
        // groupBox5
        //
        this.groupBox5.add(this.cbDispFrameCounter);
        this.groupBox5.setName("groupBox5");
        //
        // cbDispFrameCounter
        //
        this.cbDispFrameCounter.setName("cbDispFrameCounter");
        //
        // tpAbout
        //
        this.tpAbout.add(this.tableLayoutPanel);
        this.tpAbout.setName("tpAbout");
        //
        // tableLayoutPanel
        //
        this.tableLayoutPanel.add(this.logoBufferedImage, 0, 0);
        this.tableLayoutPanel.add(this.labelProductName, 1, 0);
        this.tableLayoutPanel.add(this.labelVersion, 1, 1);
        this.tableLayoutPanel.add(this.labelCopyright, 1, 2);
        this.tableLayoutPanel.add(this.labelCompanyName, 1, 3);
        this.tableLayoutPanel.add(this.textBoxDescription, 1, 4);
        this.tableLayoutPanel.add(this.llOpenGithub, 1, 5);
        this.tableLayoutPanel.setName("tableLayoutPanel");
        //
        // logoBufferedImage
        //
        this.logoBufferedImage.setIcon(new ImageIcon(Common.getImage("FeliAndMD2")));
        this.logoBufferedImage.setName("logoBufferedImage");
        //
        // labelProductName
        //
        this.labelProductName.setName("labelProductName");
        //
        // labelVersion
        //
        this.labelVersion.setName("labelVersion");
        //
        // labelCopyright
        //
        this.labelCopyright.setName("labelCopyright");
        //
        // labelCompanyName
        //
        this.labelCompanyName.setName("labelCompanyName");
        //
        // textBoxDescription
        //
        this.textBoxDescription.setName("textBoxDescription");
        this.textBoxDescription.setEditable(true);
        //
        // llOpenGithub
        //
        this.llOpenGithub.setName("llOpenGithub");
        this.llOpenGithub.addMouseListener(this.llOpenGithub_LinkClicked);
        //
        // frmSetting
        //
        this.getContentPane().add(this.tcSetting);
        this.getContentPane().add(this.btnCancel);
        this.getContentPane().add(this.btnOK);
        this.setTitle("frmSetting");
        this.addWindowListener(this.windowListener);
        // this form's geometry never lived in code: it was all resources.ApplyResources()
        Layouts.absolute(this.getContentPane(), resources);
        this.getContentPane().setPreferredSize(Layouts.clientSize(resources, new Dimension(504, 481)));
        this.pack();
    }

    private JButton btnOK;
    private JButton btnCancel;
    private JPanel gbWaveOut;
    private JCheckBox rbWaveOut;
    private JCheckBox rbAsioOut;
    private JCheckBox rbWasapiOut;
    private JPanel gbAsioOut;
    private JCheckBox rbDirectSoundOut;
    private JPanel gbWasapiOut;
    private JPanel gbDirectSound;
    private JComboBox<String> cmbWaveOutDevice;
    private JButton btnASIOControlPanel;
    private JComboBox<String> cmbAsioDevice;
    private JComboBox<String> cmbWasapiDevice;
    private JComboBox<String> cmbDirectSoundDevice;
    private JTabbedPane tcSetting;
    private JPanel tpOutput;
    private JPanel tpAbout;
    private JTable tableLayoutPanel;
    private JLabel logoBufferedImage;
    private JLabel labelProductName;
    private JLabel labelVersion;
    private JLabel labelCopyright;
    private JLabel labelCompanyName;
    private JTextArea textBoxDescription;
    private JPanel tpOther;
    private JPanel gbMIDIKeyboard;
    private JPanel gbUseChannel;
    private JCheckBox cbFM1;
    private JCheckBox cbFM2;
    private JCheckBox cbFM3;
    private JCheckBox cbUseMIDIKeyboard;
    private JCheckBox cbFM4;
    private JCheckBox cbFM5;
    private JCheckBox cbFM6;
    private JComboBox<String> cmbMIDIIN;
    private JLabel label5;
    private JCheckBox rbExclusive;
    private JCheckBox rbShare;
    private JLabel lblLatencyUnit;
    private JLabel lblLatency;
    private JComboBox<String> cmbLatency;
    private JPanel tpModule;
    private JPanel groupBox3;
    private JLabel label13;
    private JLabel label12;
    private JLabel label11;
    private JTextArea tbLatencyEmu;
    private JTextArea tbLatencySCCI;
    private JLabel label10;
    private JPanel groupBox5;
    private JCheckBox cbDispFrameCounter;
    private JCheckBox cbHiyorimiMode;
    private JCheckBox cbUseLoopTimes;
    private JLabel lblLoopTimes;
    private JTextArea tbLoopTimes;
    private JButton btnOpenSettingFolder;
    private JCheckBox cbUseGetInst;
    private JButton btnDataPath;
    private JTextArea tbDataPath;
    private JLabel label19;
    private JPanel tpMIDIKBD;
    private JComboBox<String> cmbInstFormat;
    private JLabel lblInstFormat;
    private JLabel label30;
    private JTextArea tbScreenFrameRate;
    private JLabel label29;
    private JCheckBox cbAutoOpen;
    private SettingInstrumentsPanel ucSI;
    private JPanel groupBox1;
    private JPanel groupBox4;
    private JCheckBox cbDumpSwitch;
    private JPanel gbDump;
    private JButton btnDumpPath;
    private JLabel label6;
    private JTextArea tbDumpPath;
    private JButton btnResetPosition;
    private JPanel tabMIDIExp;
    private JCheckBox cbUseMIDIExport;
    private JPanel gbMIDIExport;
    private JCheckBox cbMIDIUseVOPM;
    private JPanel groupBox6;
    private JCheckBox cbMIDIYM2612;
    private JCheckBox cbMIDISN76489Sec;
    private JCheckBox cbMIDIYM2612Sec;
    private JCheckBox cbMIDISN76489;
    private JCheckBox cbMIDIYM2151;
    private JCheckBox cbMIDIYM2610BSec;
    private JCheckBox cbMIDIYM2151Sec;
    private JCheckBox cbMIDIYM2610B;
    private JCheckBox cbMIDIYM2203;
    private JCheckBox cbMIDIYM2608Sec;
    private JCheckBox cbMIDIYM2203Sec;
    private JCheckBox cbMIDIYM2608;
    private JCheckBox cbMIDIPlayless;
    private JButton btnMIDIOutputPath;
    private JLabel lblOutputPath;
    private JTextArea tbMIDIOutputPath;
    private JCheckBox cbWavSwitch;
    private JPanel gbWav;
    private JButton btnWavPath;
    private JLabel label7;
    private JTextArea tbWavPath;
    private JCheckBox rbMONO;
    private JCheckBox rbPOLY;
    private JPanel groupBox7;
    private JCheckBox rbFM6;
    private JCheckBox rbFM3;
    private JCheckBox rbFM5;
    private JCheckBox rbFM2;
    private JCheckBox rbFM4;
    private JCheckBox rbFM1;
    private JPanel groupBox2;
    private JPanel tpOmake;
    private JTextArea tbCCFadeout;
    private JTextArea tbCCPause;
    private JTextArea tbCCSlow;
    private JTextArea tbCCPrevious;
    private JTextArea tbCCNext;
    private JTextArea tbCCFast;
    private JTextArea tbCCStop;
    private JTextArea tbCCPlay;
    private JTextArea tbCCCopyLog;
    private JLabel label17;
    private JTextArea tbCCDelLog;
    private JLabel label15;
    private JTextArea tbCCChCopy;
    private JLabel label9;
    private JLabel label8;
    private JLabel pictureBox1;
    private JLabel pictureBox4;
    private JLabel pictureBox3;
    private JLabel pictureBox2;
    private JLabel pictureBox8;
    private JLabel pictureBox7;
    private JLabel pictureBox6;
    private JLabel pictureBox5;
    private JLabel label14;
    private JButton btVST;
    private JTextArea tbVST;
    private JPanel tpMIDIOut;
    private JButton btnUP_A;
    private JButton btnSubMIDIout;
    private JButton btnDOWN_A;
    private JButton btnAddMIDIout;
    private JLabel label18;
    private JTable dgvMIDIoutListA;
    private JTable dgvMIDIoutPallet;
    private JLabel label16;
    private JTextArea clmID;
    private JTextArea clmDeviceName;
    private JTextArea clmManufacturer;
    private JTextArea clmSpacer;
    private JTabbedPane tbcMIDIoutList;
    private JPanel tabPage1;
    private JPanel tabPage2;
    private JPanel tabPage3;
    private JPanel tabPage4;
    private JButton btnUP_B;
    private JButton btnDOWN_B;
    private JButton btnUP_C;
    private JButton btnDOWN_C;
    private JButton btnUP_D;
    private JButton btnDOWN_D;
    private JPanel tabPage5;
    private JButton btnUP_E;
    private JButton btnDOWN_E;
    private JPanel tabPage6;
    private JButton btnUP_F;
    private JButton btnDOWN_F;
    private JPanel tabPage7;
    private JButton btnUP_G;
    private JButton btnDOWN_G;
    private JPanel tabPage8;
    private JButton btnUP_H;
    private JButton btnDOWN_H;
    private JPanel tabPage9;
    private JButton btnUP_I;
    private JButton btnDOWN_I;
    private JPanel tabPage10;
    private JButton button17;
    private JButton btnDOWN_J;
    private JButton btnAddVST;
    private JTable dgvMIDIoutListB;
    private JTable dgvMIDIoutListC;
    private JTable dgvMIDIoutListD;
    private JTable dgvMIDIoutListE;
    private JTable dgvMIDIoutListF;
    private JTable dgvMIDIoutListG;
    private JTable dgvMIDIoutListH;
    private JTable dgvMIDIoutListI;
    private JTable dgvMIDIoutListJ;
    private JPanel tpNSF;
    private JPanel groupBox8;
    private JCheckBox cbNSFFDSWriteDisable8000;
    private JPanel groupBox10;
    private JCheckBox cbNSFDmc_RandomizeTri;
    private JCheckBox cbNSFDmc_TriMute;
    private JCheckBox cbNSFDmc_RandomizeNoise;
    private JCheckBox cbNSFDmc_DPCMAntiClick;
    private JCheckBox cbNSFDmc_EnablePNoise;
    private JCheckBox cbNSFDmc_Enable4011;
    private JCheckBox cbNSFDmc_NonLinearMixer;
    private JCheckBox cbNSFDmc_UnmuteOnReset;
    private JPanel groupBox12;
    private JCheckBox cbNSFN160_Serial;
    private JPanel groupBox11;
    private JCheckBox cbNSFMmc5_PhaseRefresh;
    private JCheckBox cbNSFMmc5_NonLinearMixer;
    private JPanel groupBox9;
    private JCheckBox cbNFSNes_DutySwap;
    private JCheckBox cbNFSNes_PhaseRefresh;
    private JCheckBox cbNFSNes_NonLinearMixer;
    private JCheckBox cbNFSNes_UnmuteOnReset;
    private JLabel label21;
    private JLabel label20;
    private JTextArea tbNSFFds_LPF;
    private JCheckBox cbNFSFds_4085Reset;
    private JPanel tpSID;
    private JPanel groupBox13;
    private JLabel label22;
    private JButton btnSIDCharacter;
    private JButton btnSIDBasic;
    private JButton btnSIDKernal;
    private JTextArea tbSIDCharacter;
    private JTextArea tbSIDBasic;
    private JTextArea tbSIDKernal;
    private JLabel label24;
    private JLabel label23;
    private JPanel groupBox14;
    private JLabel label27;
    private JLabel label26;
    private JLabel label25;
    private JCheckBox rdSIDQ1;
    private JCheckBox rdSIDQ3;
    private JCheckBox rdSIDQ2;
    private JCheckBox rdSIDQ4;
    private JLabel lblWaitTime;
    private JLabel label28;
    private JComboBox<String> cmbWaitTime;
    private JPanel tpMIDIOut2;
    private JPanel groupBox15;
    private JButton btnBeforeSend_Default;
    private JTextArea tbBeforeSend_Custom;
    private JTextArea tbBeforeSend_XGReset;
    private JLabel label34;
    private JLabel label32;
    private JTextArea tbBeforeSend_GSReset;
    private JLabel label33;
    private JTextArea tbBeforeSend_GMReset;
    private JLabel label31;
    private JTextField JListTextBoxColumn1;
    private JCheckBox clmIsVST;
    private JTextArea clmFileName;
    private JTextArea JListTextBoxColumn2;
    private JComboBox<String> clmType;
    private JComboBox<String> ClmBeforeSend;
    private JTextArea JListTextBoxColumn3;
    private JTextArea JListTextBoxColumn4;
    private JLabel label35;
    private JLabel label36;
    private JCheckBox rbSPPCM;
    private JPanel groupBox16;
    private JComboBox<String> cmbSPPCMDevice;
    private JPanel groupBox17;
    private JTextArea tbImageExt;
    private JTextArea tbMMLExt;
    private JTextArea tbTextExt;
    private JLabel label1;
    private JLabel label3;
    private JLabel label2;
    private JCheckBox cbInitAlways;
    private JPanel tpBalance;
    private JCheckBox cbAutoBalanceUseThis;
    private JPanel groupBox18;
    private JPanel groupBox24;
    private JPanel groupBox21;
    private JCheckBox rbAutoBalanceNotSaveSongBalance;
    private JCheckBox rbAutoBalanceSamePositionAsSongData;
    private JCheckBox rbAutoBalanceSaveSongBalance;
    private JPanel groupBox22;
    private JLabel label4;
    private JPanel groupBox23;
    private JPanel groupBox19;
    private JCheckBox rbAutoBalanceNotLoadSongBalance;
    private JCheckBox rbAutoBalanceLoadSongBalance;
    private JPanel groupBox20;
    private JCheckBox rbAutoBalanceNotLoadDriverBalance;
    private JCheckBox rbAutoBalanceLoadDriverBalance;
    private JPanel groupBox25;
    private JCheckBox rbAutoBalanceNotSamePositionAsSongData;
    private JPanel tpKeyBoard;
    private JLabel pictureBox10;
    private JLabel pictureBox11;
    private JLabel pictureBox12;
    private JLabel pictureBox13;
    private JLabel pictureBox14;
    private JLabel pictureBox15;
    private JLabel pictureBox16;
    private JLabel pictureBox17;
    private JCheckBox cbUseKeyBoardHook;
    private JPanel gbUseKeyBoardHook;
    private JButton btPrevClr;
    private JButton btPauseClr;
    private JButton btFadeoutClr;
    private JButton btStopClr;
    private JButton btNextSet;
    private JButton btPrevSet;
    private JButton btPlaySet;
    private JButton btPauseSet;
    private JButton btFastSet;
    private JButton btFadeoutSet;
    private JButton btSlowSet;
    private JButton btStopSet;
    private JLabel label50;
    private JLabel lblNextKey;
    private JLabel lblFastKey;
    private JLabel lblPlayKey;
    private JLabel lblSlowKey;
    private JLabel lblPrevKey;
    private JLabel lblFadeoutKey;
    private JLabel lblPauseKey;
    private JLabel lblStopKey;
    private JCheckBox cbNextAlt;
    private JCheckBox cbFastAlt;
    private JCheckBox cbPlayAlt;
    private JCheckBox cbSlowAlt;
    private JCheckBox cbPrevAlt;
    private JCheckBox cbFadeoutAlt;
    private JCheckBox cbPauseAlt;
    private JLabel label37;
    private JCheckBox cbStopAlt;
    private JLabel label45;
    private JCheckBox cbNextWin;
    private JLabel label46;
    private JCheckBox cbFastWin;
    private JLabel label47;
    private JCheckBox cbPlayWin;
    private JLabel label48;
    private JCheckBox cbSlowWin;
    private JLabel label38;
    private JCheckBox cbPrevWin;
    private JLabel label39;
    private JCheckBox cbFadeoutWin;
    private JLabel label40;
    private JCheckBox cbPauseWin;
    private JLabel label41;
    private JCheckBox cbStopWin;
    private JLabel label42;
    private JCheckBox cbNextCtrl;
    private JLabel label43;
    private JCheckBox cbFastCtrl;
    private JLabel label44;
    private JCheckBox cbPlayCtrl;
    private JCheckBox cbStopShift;
    private JCheckBox cbSlowCtrl;
    private JCheckBox cbPauseShift;
    private JCheckBox cbPrevCtrl;
    private JCheckBox cbFadeoutShift;
    private JCheckBox cbFadeoutCtrl;
    private JCheckBox cbPrevShift;
    private JCheckBox cbPauseCtrl;
    private JCheckBox cbSlowShift;
    private JCheckBox cbStopCtrl;
    private JCheckBox cbPlayShift;
    private JCheckBox cbNextShift;
    private JCheckBox cbFastShift;
    private JButton btNextClr;
    private JButton btPlayClr;
    private JButton btFastClr;
    private JButton btSlowClr;
    //private ucSettingInstruments ucSettingInstruments1;
    private JLabel lblKeyBoardHookNotice;
    private JCheckBox rbNullDevice;
    private JTextArea tbSIDOutputBufferSize;
    private JLabel label49;
    private JLabel label51;
    private JPanel tpNuked;
    private JPanel groupBox26;
    private JCheckBox rbNukedOPN2OptionYM2612u;
    private JCheckBox rbNukedOPN2OptionYM2612;
    private JCheckBox rbNukedOPN2OptionDiscrete;
    private JCheckBox rbNukedOPN2OptionASIC;
    private JCheckBox rbNukedOPN2OptionASIClp;
    private JCheckBox cbEmptyPlayList;
    private JCheckBox cbMIDIKeyOnFnum;
    private JCheckBox cbExALL;
    private JCheckBox cbNonRenderingForPause;
    private JLabel llOpenGithub;
    private JProgressBar trkbNSFLPF;
    private JLabel label53;
    private JLabel label52;
    private JProgressBar trkbNSFHPF;
    private JPanel tpPMDDotNET;
    private JCheckBox rbPMDManual;
    private JCheckBox rbPMDAuto;
    private JButton btnPMDResetDriverArguments;
    private JLabel label54;
    private JButton btnPMDResetCompilerArhguments;
    private JTextArea tbPMDDriverArguments;
    private JLabel label55;
    private JTextArea tbPMDCompilerArguments;
    private JPanel gbPMDManual;
    private JCheckBox cbPMDSetManualVolume;
    private JCheckBox cbPMDUsePPZ8;
    private JPanel groupBox32;
    private JCheckBox rbPMD86B;
    private JCheckBox rbPMDSpbB;
    private JCheckBox rbPMDNrmB;
    private JCheckBox cbPMDUsePPSDRV;
    private JPanel gbPPSDRV;
    private JPanel groupBox33;
    private JCheckBox rbPMDUsePPSDRVManualFreq;
    private JLabel label56;
    private JCheckBox rbPMDUsePPSDRVFreqDefault;
    private JButton btnPMDPPSDRVManualWait;
    private JLabel label57;
    private JTextArea tbPMDPPSDRVFreq;
    private JLabel label58;
    private JTextArea tbPMDPPSDRVManualWait;
    private JPanel gbPMDSetManualVolume;
    private JLabel label59;
    private JLabel label60;
    private JTextArea tbPMDVolumeAdpcm;
    private JLabel label61;
    private JTextArea tbPMDVolumeRhythm;
    private JLabel label62;
    private JTextArea tbPMDVolumeSSG;
    private JLabel label63;
    private JTextArea tbPMDVolumeGIMICSSG;
    private JLabel label64;
    private JTextArea tbPMDVolumeFM;
    private JPanel groupBox28;
    private JPanel groupBox27;
    private JCheckBox rbSIDC64Model_PAL;
    private JCheckBox rbSIDC64Model_DREAN;
    private JCheckBox rbSIDC64Model_OLDNTSC;
    private JCheckBox rbSIDC64Model_NTSC;
    private JCheckBox rbSIDModel_8580;
    private JCheckBox rbSIDModel_6581;
    private JCheckBox cbSIDC64Model_Force;
    private JCheckBox cbSIDModel_Force;
    private JPanel groupBox29;
    private JCheckBox cbGensSSGEG;
    private JCheckBox cbGensDACHPF;
    private JPanel tpPlayList;
    private JCheckBox cbAutoOpenImg;
    private JCheckBox cbAutoOpenMML;
    private JCheckBox cbAutoOpenText;
    private JLabel label66;
    private JLabel label65;
    private JComboBox<String> cmbSampleRate;
    private JLabel label67;
    private JTextArea tbSCCbaseAddress;
    private JButton btnSearchPath;
    private JTextArea tbSearchPath;
    private JLabel label68;
    private JCheckBox cbNSFDmc_DPCMReverse;
    private JCheckBox cbUnuseRealChip;

    static class BindData implements PropertyChangeListener {

        public PropertyChangeListener propertyChanged;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (propertyChanged != null) {
                propertyChanged.propertyChange(new PropertyChangeEvent(this, "Value", null, null));
            }
        }

        int _value;

        public int getValue() {
            return _value;
        }

        public void setValue(int value) {
            if (value != _value) {
                _value = value;
                propertyChange(new PropertyChangeEvent(this, "Value", _value, value));
            }
        }
    }

    static class Manufacturers {

        public static final Properties props = new Properties();

        static {
            try {
                props.load(Manufacturers.class.getResourceAsStream("manufacturers.properties"));
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public static String byId(int id) {
            return props.keySet().stream().filter(k -> k.equals(String.valueOf(id))).map(props::get).findFirst().orElseThrow().toString();
        }

        public static int byManufacture(String manufacture) {
            return props.entrySet().stream().filter(e -> e.getValue().equals(manufacture)).map(e -> Integer.decode((String) props.get(e.getKey()))).findFirst().orElseThrow();
        }
    }
}
