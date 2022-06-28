package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Desktop;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
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
import javax.swing.table.DefaultTableModel;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import dotnet4j.io.Directory;
import dotnet4j.io.Path;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmInstFormat;
import mdplayer.Common.EnmRealChipType;
import mdplayer.Log;
import mdplayer.Manufacturers;
import mdplayer.MidiOutInfo;
import mdplayer.Setting;
import mdplayer.Setting.ChipType2;
import mdplayer.properties.Resources;
import mdplayer.vst.VstInfo;


public class frmSetting extends JDialog {

    private boolean asioSupported = true;
    private boolean wasapiSupported = true;
    public Setting setting;
    private boolean IsInitialOpenFolder;
    JTable[] dgv;

    private int dialogResult;

    int showDialog() {
        setVisible(true);
        return dialogResult;
    }

    public frmSetting(Setting setting) {
        setModal(true);
        this.setting = setting.copy();

        initializeComponent();

        dgv = new JTable[] {
                dgvMIDIoutListA, dgvMIDIoutListB, dgvMIDIoutListC, dgvMIDIoutListD, dgvMIDIoutListE,
                dgvMIDIoutListF, dgvMIDIoutListG, dgvMIDIoutListH, dgvMIDIoutListI, dgvMIDIoutListJ
        };

        init();
    }

    public void init() {

        this.labelProductName.setText(getAssemblyProduct());
        this.labelVersion.setText(String.format("バージョン %s", getAssemblyVersion()));
        this.labelCopyright.setText(getAssemblyCopyright());
        this.labelCompanyName.setText(getAssemblyCompany());
        this.textBoxDescription.setText(Resources.getCntDescription());

        this.cmbLatency.setSelectedIndex(5);
        this.cmbWaitTime.setSelectedIndex(0);
        cbUnuseRealChip.setSelected(setting.getUnuseRealChip());

        // ASIOサポートチェック
//        if (!AsioOut.isSupported()) {
//            rbAsioOut.setEnabled(false);
//            gbAsioOut.setEnabled(false);
//            asioSupported = false;
//        }

        // wasapiサポートチェック
//        String os = System.getProperty("os.name");
//        if (os.contains("win") && os.Version.Major < 6) {
//            rbWasapiOut.setEnabled(false);
//            gbWasapiOut.setEnabled(false);
//            wasapiSupported = false;
//        }


        // Comboboxへデバイスを列挙

        for (int i = 0; i < WaveOut.DeviceCount; i++) {
            cmbWaveOutDevice.addItem(WaveOut.GetCapabilities(i).ProductName);
        }

        for (DirectSoundDeviceInfo d : DirectSoundOut.Devices) {
            cmbDirectSoundDevice.add(d.Description);
        }

        if (wasapiSupported) {
            MMDeviceEnumerator enumerator = new MMDeviceEnumerator();
            EnumerateAudioEndPoints endPoints = enumerator.EnumerateAudioEndPoints(DataFlow.Render, DeviceState.Active);
            for (var endPoint : endPoints) {
                cmbWasapiDevice.add(String.format("%s (%s)", endPoint.FriendlyName, endPoint.DeviceFriendlyName));
            }
        }

//        if (asioSupported) {
//            for (String s : AsioOut.GetDriverNames()) {
//                cmbAsioDevice.add(s);
//            }
//        }

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                ((DefaultComboBoxModel) cmbMIDIIN.getModel()).addElement(device.getDeviceInfo().getName());
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        if (MidiSystem.getMidiDeviceInfo().length > 0)
            cmbMIDIIN.setSelectedIndex(0);

        if (ucSI != null) {
            setRealCombo(EnmRealChipType.YM2612
                    , ucSI.cmbYM2612P_SCCI, ucSI.rbYM2612P_SCCI
                    , ucSI.cmbYM2612S_SCCI, ucSI.rbYM2612S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2413
                    , ucSI.cmbYM2413P_Real, ucSI.rbYM2413P_Real
                    , ucSI.cmbYM2413S_Real, ucSI.rbYM2413S_Real
            );

            setRealCombo(EnmRealChipType.AY8910
                    , ucSI.cmbAY8910P_Real, ucSI.rbAY8910P_Real
                    , ucSI.cmbAY8910S_Real, ucSI.rbAY8910S_Real
            );

            setRealCombo(EnmRealChipType.SN76489
                    , ucSI.cmbSN76489P_SCCI, ucSI.rbSN76489P_SCCI
                    , ucSI.cmbSN76489S_SCCI, ucSI.rbSN76489S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2608
                    , ucSI.cmbYM2608P_SCCI, ucSI.rbYM2608P_SCCI
                    , ucSI.cmbYM2608S_SCCI, ucSI.rbYM2608S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2610
                    , ucSI.cmbYM2610BP_SCCI, ucSI.rbYM2610BP_SCCI
                    , ucSI.cmbYM2610BS_SCCI, ucSI.rbYM2610BS_SCCI
            );

            setRealCombo(EnmRealChipType.YM2608
                    , ucSI.cmbYM2610BEP_SCCI, ucSI.rbYM2610BEP_SCCI
                    , ucSI.cmbYM2610BES_SCCI, ucSI.rbYM2610BES_SCCI
            );

            setRealCombo(EnmRealChipType.SPPCM
                    , ucSI.cmbSPPCMP_SCCI, null
                    , ucSI.cmbSPPCMS_SCCI, null
            );

            ucSI.rbYM2610BEP_SCCI.setEnabled((ucSI.cmbYM2610BEP_SCCI.isEnabled() || ucSI.cmbSPPCMP_SCCI.isEnabled()));
            ucSI.rbYM2610BES_SCCI.setEnabled((ucSI.cmbYM2610BES_SCCI.isEnabled() || ucSI.cmbSPPCMS_SCCI.isEnabled()));

            setRealCombo(EnmRealChipType.YM2151
                    , ucSI.cmbYM2151P_SCCI, ucSI.rbYM2151P_SCCI
                    , ucSI.cmbYM2151S_SCCI, ucSI.rbYM2151S_SCCI
            );

            setRealCombo(EnmRealChipType.YM2203
                    , ucSI.cmbYM2203P_SCCI, ucSI.rbYM2203P_SCCI
                    , ucSI.cmbYM2203S_SCCI, ucSI.rbYM2203S_SCCI
            );

            setRealCombo(EnmRealChipType.C140
                    , ucSI.cmbC140P_SCCI, ucSI.rbC140P_Real
                    , ucSI.cmbC140S_SCCI, ucSI.rbC140S_SCCI
            );

            setRealCombo(EnmRealChipType.SEGAPCM
                    , ucSI.cmbSEGAPCMP_SCCI, ucSI.rbSEGAPCMP_SCCI
                    , ucSI.cmbSEGAPCMS_SCCI, ucSI.rbSEGAPCMS_SCCI
            );

            setRealCombo(EnmRealChipType.YMF262
                    , ucSI.cmbYMF262P_SCCI, ucSI.rbYMF262P_SCCI
                    , ucSI.cmbYMF262S_SCCI, ucSI.rbYMF262S_SCCI
            );

            setRealCombo(EnmRealChipType.YM3526
                    , ucSI.cmbYM3526P_SCCI, ucSI.rbYM3526P_SCCI
                    , ucSI.cmbYM3526S_SCCI, ucSI.rbYM3526S_SCCI
            );

            setRealCombo(EnmRealChipType.YM3812
                    , ucSI.cmbYM3812P_SCCI, ucSI.rbYM3812P_SCCI
                    , ucSI.cmbYM3812S_SCCI, ucSI.rbYM3812S_SCCI
            );

            setRealCombo(EnmRealChipType.K051649
                    , ucSI.cmbK051649P_Real, ucSI.rbK051649P_Real
                    , ucSI.cmbK051649S_Real, ucSI.rbK051649S_Real
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

        // 設定内容をコントロールへ適用

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
            //SSPCM
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

        if (((DefaultComboBoxModel) cmbLatency.getModel()).getIndexOf(String.valueOf(setting.getOutputDevice().getLatency())) > -1) {
            cmbLatency.setSelectedItem(String.valueOf(setting.getOutputDevice().getLatency()));
        }

        if (((DefaultComboBoxModel) cmbWaitTime.getModel()).getIndexOf(String.valueOf(setting.getOutputDevice().getWaitTime())) > -1) {
            cmbWaitTime.setSelectedItem(String.valueOf(setting.getOutputDevice().getWaitTime()));
        }

        if (ucSI != null) {
            setRealParam(setting.getYM2612Type()[0]
                    , ucSI.rbYM2612P_Silent
                    , ucSI.rbYM2612P_Emu
                    , ucSI.rbYM2612P_SCCI
                    , ucSI.cmbYM2612P_SCCI
                    , null, null, null
                    , ucSI.rbYM2612P_EmuNuked
                    , ucSI.rbYM2612P_EmuMame);
            setRealParam(setting.getYM2612Type()[1]
                    , ucSI.rbYM2612S_Silent
                    , ucSI.rbYM2612S_Emu
                    , ucSI.rbYM2612S_SCCI
                    , ucSI.cmbYM2612S_SCCI
                    , null, null, null
                    , ucSI.rbYM2612S_EmuNuked
                    , ucSI.rbYM2612S_EmuMame);

            ucSI.cbSendWait.setSelected(setting.getYM2612Type()[0].getRealChipInfo()[0].getUseWait());
            ucSI.cbTwice.setSelected(setting.getYM2612Type()[0].getRealChipInfo()[0].getUseWaitBoost());
            ucSI.cbEmulationPCMOnly.setSelected(setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation());

            setRealParam(setting.getYM2610Type()[0]
                    , ucSI.rbYM2610BP_Silent
                    , ucSI.rbYM2610BP_Emu
                    , ucSI.rbYM2610BP_SCCI
                    , ucSI.cmbYM2610BP_SCCI
                    , ucSI.rbYM2610BEP_SCCI
                    , ucSI.cmbYM2610BEP_SCCI
                    , ucSI.cmbSPPCMP_SCCI
                    , null, null);
            setRealParam(setting.getYM2610Type()[1]
                    , ucSI.rbYM2610BS_Silent
                    , ucSI.rbYM2610BS_Emu
                    , ucSI.rbYM2610BS_SCCI
                    , ucSI.cmbYM2610BS_SCCI
                    , ucSI.rbYM2610BES_SCCI
                    , ucSI.cmbYM2610BES_SCCI
                    , ucSI.cmbSPPCMS_SCCI
                    , null, null);

            setRealParam(setting.getSN76489Type()[0]
                    , ucSI.rbSN76489P_Silent
                    , ucSI.rbSN76489P_Emu
                    , ucSI.rbSN76489P_SCCI
                    , ucSI.cmbSN76489P_SCCI
                    , null, null, null
                    , ucSI.rbSN76489P_Emu2, null);
            setRealParam(setting.getSN76489Type()[1]
                    , ucSI.rbSN76489S_Silent
                    , ucSI.rbSN76489S_Emu
                    , ucSI.rbSN76489S_SCCI
                    , ucSI.cmbSN76489S_SCCI
                    , null, null, null
                    , ucSI.rbSN76489S_Emu2, null);

            setRealParam(setting.getYM2608Type()[0]
                    , ucSI.rbYM2608P_Silent
                    , ucSI.rbYM2608P_Emu
                    , ucSI.rbYM2608P_SCCI
                    , ucSI.cmbYM2608P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM2608Type()[1]
                    , ucSI.rbYM2608S_Silent
                    , ucSI.rbYM2608S_Emu
                    , ucSI.rbYM2608S_SCCI
                    , ucSI.cmbYM2608S_SCCI, null, null, null, null, null);

            setRealParam(setting.getYM2151Type()[0]
                    , ucSI.rbYM2151P_Silent
                    , ucSI.rbYM2151P_Emu
                    , ucSI.rbYM2151P_SCCI
                    , ucSI.cmbYM2151P_SCCI
                    , null, null, null
                    , ucSI.rbYM2151P_EmuMame
                    , ucSI.rbYM2151P_EmuX68Sound);
            setRealParam(setting.getYM2151Type()[1]
                    , ucSI.rbYM2151S_Silent
                    , ucSI.rbYM2151S_Emu
                    , ucSI.rbYM2151S_SCCI
                    , ucSI.cmbYM2151S_SCCI
                    , null, null, null
                    , ucSI.rbYM2151S_EmuMame
                    , ucSI.rbYM2151S_EmuX68Sound);

            setRealParam(setting.getYM2203Type()[0]
                    , ucSI.rbYM2203P_Silent
                    , ucSI.rbYM2203P_Emu
                    , ucSI.rbYM2203P_SCCI
                    , ucSI.cmbYM2203P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM2203Type()[1]
                    , ucSI.rbYM2203S_Silent
                    , ucSI.rbYM2203S_Emu
                    , ucSI.rbYM2203S_SCCI
                    , ucSI.cmbYM2203S_SCCI, null, null, null, null, null);

            setRealParam(setting.getAY8910Type()[0]
                    , ucSI.rbAY8910P_Silent
                    , ucSI.rbAY8910P_Emu
                    , ucSI.rbAY8910P_Real
                    , ucSI.cmbAY8910P_Real
                    , null, null, null
                    , ucSI.rbAY8910P_Emu2, null);

            setRealParam(setting.getAY8910Type()[1]
                    , ucSI.rbAY8910S_Silent
                    , ucSI.rbAY8910S_Emu
                    , ucSI.rbAY8910S_Real
                    , ucSI.cmbAY8910S_Real
                    , null, null, null
                    , ucSI.rbAY8910S_Emu2, null);

            setRealParam(setting.getK051649Type()[0]
                    , ucSI.rbK051649P_Silent
                    , ucSI.rbK051649P_Emu
                    , ucSI.rbK051649P_Real
                    , ucSI.cmbK051649P_Real, null, null, null, null, null);
            setRealParam(setting.getK051649Type()[1]
                    , ucSI.rbK051649S_Silent
                    , ucSI.rbK051649S_Emu
                    , ucSI.rbK051649S_Real
                    , ucSI.cmbK051649S_Real, null, null, null, null, null);

            setRealParam(setting.getYM2413Type()[0]
                    , ucSI.rbYM2413P_Silent
                    , ucSI.rbYM2413P_Emu
                    , ucSI.rbYM2413P_Real
                    , ucSI.cmbYM2413P_Real, null, null, null, null, null);
            setRealParam(setting.getYM2413Type()[1]
                    , ucSI.rbYM2413S_Silent
                    , ucSI.rbYM2413S_Emu
                    , ucSI.rbYM2413S_Real
                    , ucSI.cmbYM2413S_Real, null, null, null, null, null);

            setRealParam(setting.getYM3526Type()[0]
                    , ucSI.rbYM3526P_Silent
                    , ucSI.rbYM3526P_Emu
                    , ucSI.rbYM3526P_SCCI
                    , ucSI.cmbYM3526P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM3526Type()[1]
                    , ucSI.rbYM3526S_Silent
                    , ucSI.rbYM3526S_Emu
                    , ucSI.rbYM3526S_SCCI
                    , ucSI.cmbYM3526S_SCCI, null, null, null, null, null);

            setRealParam(setting.getYM3812Type()[0]
                    , ucSI.rbYM3812P_Silent
                    , ucSI.rbYM3812P_Emu
                    , ucSI.rbYM3812P_SCCI
                    , ucSI.cmbYM3812P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYM3812Type()[1]
                    , ucSI.rbYM3812S_Silent
                    , ucSI.rbYM3812S_Emu
                    , ucSI.rbYM3812S_SCCI
                    , ucSI.cmbYM3812S_SCCI, null, null, null, null, null);

            setRealParam(setting.getYMF262Type()[0]
                    , ucSI.rbYMF262P_Silent
                    , ucSI.rbYMF262P_Emu
                    , ucSI.rbYMF262P_SCCI
                    , ucSI.cmbYMF262P_SCCI, null, null, null, null, null);
            setRealParam(setting.getYMF262Type()[1]
                    , ucSI.rbYMF262S_Silent
                    , ucSI.rbYMF262S_Emu
                    , ucSI.rbYMF262S_SCCI
                    , ucSI.cmbYMF262S_SCCI, null, null, null, null, null);

            setRealParam(setting.getC140Type()[0]
                    , ucSI.rbC140P_Silent
                    , ucSI.rbC140P_Emu
                    , ucSI.rbC140P_Real
                    , ucSI.cmbC140P_SCCI, null, null, null, null, null);
            setRealParam(setting.getC140Type()[1]
                    , ucSI.rbC140S_Silent
                    , ucSI.rbC140S_Emu
                    , ucSI.rbC140S_SCCI
                    , ucSI.cmbC140S_SCCI, null, null, null, null, null);

            setRealParam(setting.getSEGAPCMType()[0]
                    , ucSI.rbSEGAPCMP_Silent
                    , ucSI.rbSEGAPCMP_Emu
                    , ucSI.rbSEGAPCMP_SCCI
                    , ucSI.cmbSEGAPCMP_SCCI, null, null, null, null, null);
            setRealParam(setting.getSEGAPCMType()[1]
                    , ucSI.rbSEGAPCMS_Silent
                    , ucSI.rbSEGAPCMS_Emu
                    , ucSI.rbSEGAPCMS_SCCI
                    , ucSI.cmbSEGAPCMS_SCCI, null, null, null, null, null);
        }

        for (int i = 0; i < cmbSampleRate.getItemCount(); i++) {
            if (cmbSampleRate.getItemAt(i).toString().equals(String.valueOf(setting.getOutputDevice().getSampleRate()))) {
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
        tbSCCbaseAddress.setText(String.format("%04X", setting.getDebug_SCCbaseAddress()));

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


        if (setting.getMidiOut().getMidiOutInfos() != null && setting.getMidiOut().getMidiOutInfos().size() > 0) {
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
                                e.printStackTrace();
                            }
                        }

                        moi.id = found;

                        String stype = "GM";
                        switch (moi.type) {
                        case 1:
                            stype = "XG";
                            break;
                        case 2:
                            stype = "GS";
                            break;
                        case 3:
                            stype = "LA";
                            break;
                        case 4:
                            stype = "GS(SC-55_1)";
                            break;
                        case 5:
                            stype = "GS(SC-55_2)";
                            break;
                        }

                        String sbeforeSend = "None";
                        switch (moi.beforeSendType) {
                        case 1:
                            sbeforeSend = "GM Reset";
                            break;
                        case 2:
                            sbeforeSend = "XG Reset";
                            break;
                        case 3:
                            sbeforeSend = "GS Reset";
                            break;
                        case 4:
                            sbeforeSend = "Custom";
                            break;
                        }

                        m.addRow(new Object[] {
                                moi.id
                                , moi.isVST
                                , moi.fileName
                                , moi.name
                                , stype
                                , sbeforeSend
                                , moi.isVST ? moi.vendor : (moi.manufacturer != -1 ? String.valueOf(moi.manufacturer) : "Unknown")
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
                m.addRow(new Object[] { i++, device.getDeviceInfo().getName(), device.getDeviceInfo().getVendor()});
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
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

        cbUseKeyBoardHook.setSelected(setting.getKeyBoardHook().getUseKeyBoardHook());
        gbUseKeyBoardHook.setEnabled(setting.getKeyBoardHook().getUseKeyBoardHook());

        cbStopShift.setSelected(setting.getKeyBoardHook().getStop().getShift());
        cbStopCtrl.setSelected(setting.getKeyBoardHook().getStop().getCtrl());
        cbStopWin.setSelected(setting.getKeyBoardHook().getStop().getWin());
        cbStopAlt.setSelected(setting.getKeyBoardHook().getStop().getAlt());
        lblStopKey.setText(setting.getKeyBoardHook().getStop().getKey());
        btStopClr.setEnabled((!lblStopKey.getText().equals("(None)") && (lblStopKey.getText() != null && !lblStopKey.getText().isEmpty())));

        cbPauseShift.setSelected(setting.getKeyBoardHook().getPause().getShift());
        cbPauseCtrl.setSelected(setting.getKeyBoardHook().getPause().getCtrl());
        cbPauseWin.setSelected(setting.getKeyBoardHook().getPause().getWin());
        cbPauseAlt.setSelected(setting.getKeyBoardHook().getPause().getAlt());
        lblPauseKey.setText(setting.getKeyBoardHook().getPause().getKey());
        btPauseClr.setEnabled((!lblPauseKey.getText().equals("(None)") && (lblPauseKey.getText() != null && !lblPauseKey.getText().isEmpty())));

        cbFadeoutShift.setSelected(setting.getKeyBoardHook().getFadeout().getShift());
        cbFadeoutCtrl.setSelected(setting.getKeyBoardHook().getFadeout().getCtrl());
        cbFadeoutWin.setSelected(setting.getKeyBoardHook().getFadeout().getWin());
        cbFadeoutAlt.setSelected(setting.getKeyBoardHook().getFadeout().getAlt());
        lblFadeoutKey.setText(setting.getKeyBoardHook().getFadeout().getKey());
        btFadeoutClr.setEnabled((!lblFadeoutKey.getText().equals("(None)") && (lblFadeoutKey.getText() != null && !lblFadeoutKey.getText().isEmpty())));

        cbPrevShift.setSelected(setting.getKeyBoardHook().getPrev().getShift());
        cbPrevCtrl.setSelected(setting.getKeyBoardHook().getPrev().getCtrl());
        cbPrevWin.setSelected(setting.getKeyBoardHook().getPrev().getWin());
        cbPrevAlt.setSelected(setting.getKeyBoardHook().getPrev().getAlt());
        lblPrevKey.setText(setting.getKeyBoardHook().getPrev().getKey());
        btPrevClr.setEnabled((!lblPrevKey.getText().equals("(None)") && (lblPrevKey.getText() != null && !lblPrevKey.getText().isEmpty())));

        cbSlowShift.setSelected(setting.getKeyBoardHook().getSlow().getShift());
        cbSlowCtrl.setSelected(setting.getKeyBoardHook().getSlow().getCtrl());
        cbSlowWin.setSelected(setting.getKeyBoardHook().getSlow().getWin());
        cbSlowAlt.setSelected(setting.getKeyBoardHook().getSlow().getAlt());
        lblSlowKey.setText(setting.getKeyBoardHook().getSlow().getKey());
        btSlowClr.setEnabled((!lblSlowKey.getText().equals("(None)") && (lblSlowKey.getText() != null && !lblSlowKey.getText().isEmpty())));

        cbPlayShift.setSelected(setting.getKeyBoardHook().getPlay().getShift());
        cbPlayCtrl.setSelected(setting.getKeyBoardHook().getPlay().getCtrl());
        cbPlayWin.setSelected(setting.getKeyBoardHook().getPlay().getWin());
        cbPlayAlt.setSelected(setting.getKeyBoardHook().getPlay().getAlt());
        lblPlayKey.setText(setting.getKeyBoardHook().getPlay().getKey());
        btPlayClr.setEnabled((!lblPlayKey.getText().equals("(None)") && (lblPlayKey.getText() != null && !lblPlayKey.getText().isEmpty())));

        cbFastShift.setSelected(setting.getKeyBoardHook().getFast().getShift());
        cbFastCtrl.setSelected(setting.getKeyBoardHook().getFast().getCtrl());
        cbFastWin.setSelected(setting.getKeyBoardHook().getFast().getWin());
        cbFastAlt.setSelected(setting.getKeyBoardHook().getFast().getAlt());
        lblFastKey.setText(setting.getKeyBoardHook().getFast().getKey());
        btFastClr.setEnabled((!lblFastKey.getText().equals("(None)") && (lblFastKey.getText() != null && !lblFastKey.getText().isEmpty())));

        cbNextShift.setSelected(setting.getKeyBoardHook().getNext().getShift());
        cbNextCtrl.setSelected(setting.getKeyBoardHook().getNext().getCtrl());
        cbNextWin.setSelected(setting.getKeyBoardHook().getNext().getWin());
        cbNextAlt.setSelected(setting.getKeyBoardHook().getNext().getAlt());
        lblNextKey.setText(setting.getKeyBoardHook().getNext().getKey());
        btNextClr.setEnabled((!lblNextKey.getText().equals("(None)") && (lblNextKey.getText() != null && !lblNextKey.getText().isEmpty())));

        cbExALL.setSelected(setting.getOther().getExAll());
        cbNonRenderingForPause.setSelected(setting.getOther().getNonRenderingForPause());


        tbPMDCompilerArguments.setText(setting.getPmdDotNET().compilerArguments);
        rbPMDAuto.setSelected(setting.getPmdDotNET().isAuto);
        rbPMDManual.setSelected(!setting.getPmdDotNET().isAuto);
        rbPMDNrmB.setSelected(setting.getPmdDotNET().soundBoard == 0);
        rbPMDSpbB.setSelected(setting.getPmdDotNET().soundBoard == 1);
        rbPMD86B.setSelected(setting.getPmdDotNET().soundBoard == 2);
        cbPMDSetManualVolume.setSelected(setting.getPmdDotNET().setManualVolume);
        cbPMDUsePPSDRV.setSelected(setting.getPmdDotNET().usePPSDRV);
        cbPMDUsePPZ8.setSelected(setting.getPmdDotNET().usePPZ8);
        tbPMDDriverArguments.setText(setting.getPmdDotNET().driverArguments);
        rbPMDUsePPSDRVFreqDefault.setSelected(setting.getPmdDotNET().usePPSDRVUseInterfaceDefaultFreq);
        rbPMDUsePPSDRVManualFreq.setSelected(!setting.getPmdDotNET().usePPSDRVUseInterfaceDefaultFreq);
        tbPMDPPSDRVFreq.setText(String.valueOf(setting.getPmdDotNET().ppsDrvManualFreq));
        tbPMDPPSDRVManualWait.setText(String.valueOf(setting.getPmdDotNET().ppsDrvManualWait));
        tbPMDVolumeFM.setText(String.valueOf(setting.getPmdDotNET().volumeFM));
        tbPMDVolumeSSG.setText(String.valueOf(setting.getPmdDotNET().volumeSSG));
        tbPMDVolumeRhythm.setText(String.valueOf(setting.getPmdDotNET().volumeRhythm));
        tbPMDVolumeAdpcm.setText(String.valueOf(setting.getPmdDotNET().volumeAdpcm));
        tbPMDVolumeGIMICSSG.setText(String.valueOf(setting.getPmdDotNET().volumeGIMICSSG));

        rbPMDManual_CheckedChanged(null);
        cbPMDSetManualVolume_CheckedChanged(null);
        cbPMDUsePPSDRV_CheckedChanged(null);
        rbPMDUsePPSDRVManualFreq_CheckedChanged(null);
    }

    private void setRealCombo(EnmRealChipType realType, JComboBox<String> cmbP, JCheckBox rbP, JComboBox<String> cmbS, JCheckBox rbS) {

        if (rbP != null) rbP.setEnabled(false);
        cmbP.setEnabled(false);

        if (rbS != null) rbS.setEnabled(false);
        cmbS.setEnabled(false);

        List<ChipType2> lstChip = Audio.getRealChipList(realType);
        if (lstChip == null || lstChip.size() < 1) return;

        for (ChipType2 ct : lstChip) {
            if (ct == null) continue;

            cmbP.addItem(String.format("(%s:%s:%s:%s)%s"
                    , ct.getRealChipInfo()[0].getInterfaceName()
                    , ct.getRealChipInfo()[0].getSoundLocation()
                    , ct.getRealChipInfo()[0].getBusID()
                    , ct.getRealChipInfo()[0].getSoundChip()
                    , ct.getRealChipInfo()[0].getChipName()));

            cmbS.addItem(String.format("(%s:%s:%s:%s)%s"
                    , ct.getRealChipInfo()[0].getInterfaceName()
                    , ct.getRealChipInfo()[0].getSoundLocation()
                    , ct.getRealChipInfo()[0].getBusID()
                    , ct.getRealChipInfo()[0].getSoundChip()
                    , ct.getRealChipInfo()[0].getChipName()));
        }

        cmbP.setSelectedIndex(0);
        if (rbP != null) rbP.setEnabled(true);
        cmbP.setEnabled(true);

        cmbS.setSelectedIndex(0);
        if (rbS != null) rbS.setEnabled(true);
        cmbS.setEnabled(true);
    }

    private void setRealParam(ChipType2 chipType2, JCheckBox rbSilent, JCheckBox rbEmu, JCheckBox rbReal, JComboBox<String> cmbP
            , JCheckBox rbReal2 /*= null*/, JComboBox<String> cmbP2A /*= null*/, JComboBox<String> cmbP2B /*= null*/, JCheckBox rbEmu2 /*= null*/, JCheckBox rbEmu3/* = null*/) {
        String n = "";

        if (chipType2.getRealChipInfo()[0] != null) {
            n = String.format("(%s:%s:%s:%s)"
                    , chipType2.getRealChipInfo()[0].getInterfaceName()
                    , chipType2.getRealChipInfo()[0].getSoundLocation()
                    , chipType2.getRealChipInfo()[0].getBusID()
                    , chipType2.getRealChipInfo()[0].getSoundChip());
        }

        if (cmbP.getItemCount() > 0) {
            for (int i = 0; i < cmbP.getItemCount(); i++) {
                if (!cmbP.getItemAt(i).contains(n)) continue;
                cmbP.setSelectedItem(i);

                break;
            }
        }

        if (cmbP2A != null) {
            if (chipType2.getRealChipInfo()[1] != null) {
                n = String.format("(%s:%s:%s:%s)"
                        , chipType2.getRealChipInfo()[1].getInterfaceName()
                        , chipType2.getRealChipInfo()[1].getSoundLocation()
                        , chipType2.getRealChipInfo()[1].getBusID()
                        , chipType2.getRealChipInfo()[1].getSoundChip());
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
            if (chipType2.getRealChipInfo()[2] != null) {
                n = String.format("(%s:%s:%s:%s)"
                        , chipType2.getRealChipInfo()[2].getInterfaceName()
                        , chipType2.getRealChipInfo()[2].getSoundLocation()
                        , chipType2.getRealChipInfo()[2].getBusID()
                        , chipType2.getRealChipInfo()[2].getSoundChip());
            }

            if (cmbP2B.getItemCount() > 0) {
                for (int i = 0; i < cmbP2B.getItemCount(); i++) {
                    if (!cmbP2B.getItemAt(i).contains(n)) continue;
                    cmbP2B.setSelectedItem(i);

                    break;
                }
            }
        }

        if (chipType2.getUseEmu().length > 1 && chipType2.getUseEmu()[1]) {
            rbEmu2.setSelected(true);
            return;
        }

        if (chipType2.getUseEmu().length > 2 && chipType2.getUseEmu()[2]) {
            rbEmu3.setSelected(true);
            return;
        }

        if ((chipType2.getUseReal().length > 0 && !chipType2.getUseReal()[0])
                && (chipType2.getUseReal().length > 1 && !chipType2.getUseReal()[1]))// rbSCCI2==null) || (!chipType2.UseScci && rbSCCI2 != null && !chipType2.UseScci2))
        {
            if (chipType2.getUseEmu()[0])
                rbEmu.setSelected(true);
            else
                rbSilent.setSelected(true);

            return;
        }

        if (
                ((chipType2.getUseReal().length > 0 && chipType2.getUseReal()[0]) && !cmbP.isEnabled())
                        || (
                        (chipType2.getUseReal().length > 1 && chipType2.getUseReal()[1])
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

        ((DefaultTableModel) dgv.getModel()).setRowCount(0);

        for (JListColumn col : Columns) {
            dgv.Columns.add((JListColumn) col.Clone());
        }
    }

    private void btnASIOControlPanel_Click(ActionEvent ev) {
        try {
//            try (AsioOut asio = new AsioOut(cmbAsioDevice.getSelectedItem().toString())) {
//                asio.ShowControlPanel();
//            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            JOptionPane.showConfirmDialog(null, ex.getMessage());
        }
    }

    private void btnOK_Click(ActionEvent ev) {
        if (!checkSetting()) return;

        int i = 0;

        // #region 出力

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

// #endregion

// #region Sound

        setting.setUnuseRealChip(cbUnuseRealChip.isSelected());
        setting.setYM2612Type(new ChipType2[2]);
        setting.getYM2612Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2612Type()[0]
                , ucSI.rbYM2612P_SCCI
                , ucSI.cmbYM2612P_SCCI
                , ucSI.rbYM2612P_Emu
                , ucSI.rbYM2612P_EmuNuked
                , ucSI.rbYM2612P_EmuMame
                , null
                , null
                , null
        );

        setting.getYM2612Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2612Type()[1]
                , ucSI.rbYM2612S_SCCI
                , ucSI.cmbYM2612S_SCCI
                , ucSI.rbYM2612S_Emu
                , ucSI.rbYM2612S_EmuNuked
                , ucSI.rbYM2612S_EmuMame
                , null
                , null
                , null
        );
        if (setting.getYM2612Type()[0].getRealChipInfo() == null) {
            setting.getYM2612Type()[0].setrealChipInfo(new ChipType2.RealChipInfo[] {new ChipType2.RealChipInfo()});
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
                setting.getSN76489Type()[0]
                , ucSI.rbSN76489P_SCCI
                , ucSI.cmbSN76489P_SCCI
                , ucSI.rbSN76489P_Emu
                , ucSI.rbSN76489P_Emu2
                , null
                , null
                , null
                , null
        );

        setting.getSN76489Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getSN76489Type()[1]
                , ucSI.rbSN76489S_SCCI
                , ucSI.cmbSN76489S_SCCI
                , ucSI.rbSN76489S_Emu
                , ucSI.rbSN76489S_Emu2
                , null
                , null
                , null
                , null
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
                setting.getYM2608Type()[0]
                , ucSI.rbYM2608P_SCCI
                , ucSI.cmbYM2608P_SCCI
                , ucSI.rbYM2608P_Emu
                , null
                , null
                , null
                , null
                , null
        );

        setting.getYM2608Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2608Type()[1]
                , ucSI.rbYM2608S_SCCI
                , ucSI.cmbYM2608S_SCCI
                , ucSI.rbYM2608S_Emu
                , null
                , null
                , null
                , null
                , null
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
                setting.getYM2610Type()[0]
                , ucSI.rbYM2610BP_SCCI
                , ucSI.cmbYM2610BP_SCCI
                , ucSI.rbYM2610BP_Emu
                , null
                , null
                , ucSI.rbYM2610BEP_SCCI
                , ucSI.cmbYM2610BEP_SCCI
                , ucSI.cmbSPPCMP_SCCI
        );

        setting.getYM2610Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2610Type()[1]
                , ucSI.rbYM2610BS_SCCI
                , ucSI.cmbYM2610BS_SCCI
                , ucSI.rbYM2610BS_Emu
                , null
                , null
                , ucSI.rbYM2610BES_SCCI
                , ucSI.cmbYM2610BES_SCCI
                , ucSI.cmbSPPCMS_SCCI
        );

        setting.setYM2151Type(new ChipType2[2]);
        setting.getYM2151Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2151Type()[0]
                , ucSI.rbYM2151P_SCCI
                , ucSI.cmbYM2151P_SCCI
                , ucSI.rbYM2151P_Emu
                , ucSI.rbYM2151P_EmuMame
                , ucSI.rbYM2151P_EmuX68Sound
                , null
                , null
                , null
        );

        setting.getYM2151Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2151Type()[1]
                , ucSI.rbYM2151S_SCCI
                , ucSI.cmbYM2151S_SCCI
                , ucSI.rbYM2151S_Emu
                , ucSI.rbYM2151S_EmuMame
                , ucSI.rbYM2151S_EmuX68Sound
                , null
                , null
                , null
        );

        setting.setYM2203Type(new ChipType2[2]);
        setting.getYM2203Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2203Type()[0]
                , ucSI.rbYM2203P_SCCI
                , ucSI.cmbYM2203P_SCCI
                , ucSI.rbYM2203P_Emu
                , null, null, null, null, null
        );

        setting.getYM2203Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2203Type()[1]
                , ucSI.rbYM2203S_SCCI
                , ucSI.cmbYM2203S_SCCI
                , ucSI.rbYM2203S_Emu
                , null, null, null, null, null
        );

        setting.setAY8910Type(new ChipType2[2]);
        setting.getAY8910Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getAY8910Type()[0]
                , ucSI.rbAY8910P_Real
                , ucSI.cmbAY8910P_Real
                , ucSI.rbAY8910P_Emu
                , ucSI.rbAY8910P_Emu2
                , null, null, null, null
        );

        setting.getAY8910Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getAY8910Type()[1]
                , ucSI.rbAY8910S_Real
                , ucSI.cmbAY8910S_Real
                , ucSI.rbAY8910S_Emu
                , ucSI.rbAY8910S_Emu2
                , null, null, null, null
        );

        setting.setK051649Type(new ChipType2[2]);
        setting.getK051649Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getK051649Type()[0]
                , ucSI.rbK051649P_Real
                , ucSI.cmbK051649P_Real
                , ucSI.rbK051649P_Emu
                , null, null, null, null, null
        );

        setting.getK051649Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getK051649Type()[1]
                , ucSI.rbK051649S_Real
                , ucSI.cmbK051649S_Real
                , ucSI.rbK051649S_Emu
                , null, null, null, null, null
        );


        setting.setYM2413Type(new ChipType2[2]);
        setting.getYM2413Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2413Type()[0]
                , ucSI.rbYM2413P_Real
                , ucSI.cmbYM2413P_Real
                , ucSI.rbYM2413P_Emu
                , null, null, null, null, null
        );

        setting.getYM2413Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM2413Type()[1]
                , ucSI.rbYM2413S_Real
                , ucSI.cmbYM2413S_Real
                , ucSI.rbYM2413S_Emu
                , null, null, null, null, null
        );

        setting.setC140Type(new ChipType2[2]);
        setting.getC140Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getC140Type()[0]
                , ucSI.rbC140P_Real
                , ucSI.cmbC140P_SCCI
                , ucSI.rbC140P_Emu
                , null, null, null, null, null
        );

        setting.getC140Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getC140Type()[1]
                , ucSI.rbC140S_SCCI
                , ucSI.cmbC140S_SCCI
                , ucSI.rbC140S_Emu
                , null, null, null, null, null
        );


        setting.setSEGAPCMType(new ChipType2[2]);
        setting.getSEGAPCMType()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getSEGAPCMType()[0]
                , ucSI.rbSEGAPCMP_SCCI
                , ucSI.cmbSEGAPCMP_SCCI
                , ucSI.rbSEGAPCMP_Emu
                , null, null, null, null, null
        );

        setting.getSEGAPCMType()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getSEGAPCMType()[1]
                , ucSI.rbSEGAPCMS_SCCI
                , ucSI.cmbSEGAPCMS_SCCI
                , ucSI.rbSEGAPCMS_Emu
                , null, null, null, null, null
        );

        setting.setYM3526Type(new ChipType2[2]);
        setting.getYM3526Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3526Type()[0]
                , ucSI.rbYM3526P_SCCI
                , ucSI.cmbYM3526P_SCCI
                , ucSI.rbYM3526P_Emu
                , null, null, null, null, null
        );

        setting.getYM3526Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3526Type()[1]
                , ucSI.rbYM3526S_SCCI
                , ucSI.cmbYM3526S_SCCI
                , ucSI.rbYM3526S_Emu
                , null, null, null, null, null
        );

        setting.setYM3812Type(new ChipType2[2]);
        setting.getYM3812Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3812Type()[0]
                , ucSI.rbYM3812P_SCCI
                , ucSI.cmbYM3812P_SCCI
                , ucSI.rbYM3812P_Emu
                , null, null, null, null, null
        );

        setting.getYM3812Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYM3812Type()[1]
                , ucSI.rbYM3812S_SCCI
                , ucSI.cmbYM3812S_SCCI
                , ucSI.rbYM3812S_Emu
                , null, null, null, null, null
        );

        setting.setYMF262Type(new ChipType2[2]);
        setting.getYMF262Type()[0] = new ChipType2();
        setChipType2FromControls(
                setting.getYMF262Type()[0]
                , ucSI.rbYMF262P_SCCI
                , ucSI.cmbYMF262P_SCCI
                , ucSI.rbYMF262P_Emu
                , null, null, null, null, null
        );

        setting.getYMF262Type()[1] = new ChipType2();
        setChipType2FromControls(
                setting.getYMF262Type()[1]
                , ucSI.rbYMF262S_SCCI
                , ucSI.cmbYMF262S_SCCI
                , ucSI.rbYMF262S_Emu
                , null, null, null, null, null
        );

// #endregion


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

        setting.getMidiKbd().setMidiCtrl_CopySelecttingLogToClipbrd(-1);
        try {
            i = Integer.parseInt(tbCCCopyLog.getText());
            setting.getMidiKbd().setMidiCtrl_CopySelecttingLogToClipbrd(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_CopyToneFromYM2612Ch1(-1);
        }
        try {
            i = Integer.parseInt(tbCCChCopy.getText());
            setting.getMidiKbd().setMidiCtrl_CopyToneFromYM2612Ch1(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_DelOneLog(-1);
        }
        try {
            i = Integer.parseInt(tbCCDelLog.getText());
            setting.getMidiKbd().setMidiCtrl_DelOneLog(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Fadeout(-1);
        }
        try {
            i = Integer.parseInt(tbCCFadeout.getText());
            setting.getMidiKbd().setMidiCtrl_Fadeout(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Fast(-1);
        }
        try {
            i = Integer.parseInt(tbCCFast.getText());
            setting.getMidiKbd().setMidiCtrl_Fast(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Next(-1);
        }
        try {
            i = Integer.parseInt(tbCCNext.getText());
            setting.getMidiKbd().setMidiCtrl_Next(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Pause(-1);
        }
        try {
            i = Integer.parseInt(tbCCPause.getText());
            setting.getMidiKbd().setMidiCtrl_Pause(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Play(-1);
        }
        try {
            i = Integer.parseInt(tbCCPlay.getText());
            setting.getMidiKbd().setMidiCtrl_Play(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Previous(-1);
        }
        try {
            i = Integer.parseInt(tbCCPrevious.getText());
            setting.getMidiKbd().setMidiCtrl_Previous(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrlSlow(-1);
        }
        try {
            i = Integer.parseInt(tbCCSlow.getText());
            setting.getMidiKbd().setMidiCtrlSlow(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException e) {
            setting.getMidiKbd().setMidiCtrl_Stop(-1);
        }
        try {
            i = Integer.parseInt(tbCCStop.getText());
            setting.getMidiKbd().setMidiCtrl_Stop(Math.min(Math.max(i, 0), 127));
        } catch (NumberFormatException ignored) {
        }
        try {
            i = Integer.parseInt(tbLatencyEmu.getText());
            setting.setLatencyEmulation(Math.max(Math.min(i, 999), 0));
        } catch (NumberFormatException ignored) {
        }
        try {
            i = Integer.parseInt(tbLatencySCCI.getText());
            setting.setLatencySCCI(Math.max(Math.min(i, 999), 0));
        } catch (NumberFormatException ignored) {
        }

        setting.getOther().setUseLoopTimes(cbUseLoopTimes.isSelected());
        try {
            i = Integer.parseInt(tbLoopTimes.getText());
            setting.getOther().setLoopTimes(Math.max(Math.min(i, 999), 1));
        } catch (NumberFormatException ignored) {
        }

        setting.getOther().setUseGetInst(cbUseGetInst.isSelected());
        setting.getOther().setDefaultDataPath(tbDataPath.getText());
        setting.setFileSearchPathList(tbSearchPath.getText());
        setting.getOther().setInstFormat(EnmInstFormat.values()[cmbInstFormat.getSelectedIndex()]);
        try {
            i = Integer.parseInt(tbScreenFrameRate.getText());
            setting.getOther().setScreenFrameRate(Math.max(Math.min(i, 120), 10));
        } catch (NumberFormatException ignored) {
        }
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
        try {
            setting.setDebug_SCCbaseAddress(Integer.parseInt(tbSCCbaseAddress.getText(), 16));
        } catch (NumberFormatException e) {
            setting.setDebug_SCCbaseAddress(0x9800);
        }

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
                    //GM / XG / GS / LA / GS(SC - 55_1) / GS(SC - 55_2)
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
                        moi.manufacturer = mn == null || mn.equals("Unknown") ? -1 : Manufacturers.byManufacture(mn).id;
                    }

                    lstMoi.add(moi);
                }
                setting.getMidiOut().getMidiOutInfos().add(lstMoi.toArray(new MidiOutInfo[0]));
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
            setting.getNsf().setFDSLpf(Math.min(Math.max(i, 0), 99999));
        } catch (NumberFormatException ignored) {}
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
            setting.getSid().outputBufferSize = Math.min(Math.max(Integer.parseInt(tbSIDOutputBufferSize.getText()), 100), 999999);
        } catch (Exception e) {
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


        setting.getPmdDotNET().compilerArguments = tbPMDCompilerArguments.getText();
        setting.getPmdDotNET().isAuto = rbPMDAuto.isSelected();
        setting.getPmdDotNET().soundBoard = rbPMDNrmB.isSelected() ? 0 : (rbPMDSpbB.isSelected() ? 1 : 2);
        setting.getPmdDotNET().setManualVolume = cbPMDSetManualVolume.isSelected();
        setting.getPmdDotNET().usePPSDRV = cbPMDUsePPSDRV.isSelected();
        setting.getPmdDotNET().usePPZ8 = cbPMDUsePPZ8.isSelected();
        setting.getPmdDotNET().driverArguments = tbPMDDriverArguments.getText();
        setting.getPmdDotNET().usePPSDRVUseInterfaceDefaultFreq = rbPMDUsePPSDRVFreqDefault.isSelected();
        int nn;
        try {
            nn = Integer.parseInt(tbPMDPPSDRVFreq.getText());
        } catch (NumberFormatException e) {
            nn = 2000;
        }
        setting.getPmdDotNET().ppsDrvManualFreq = nn;
        try {
            nn = Integer.parseInt(tbPMDPPSDRVManualWait.getText());
        } catch (NumberFormatException e) {
            nn = 1;
        }
        nn = Math.min(Math.max(nn, 0), 100);
        setting.getPmdDotNET().ppsDrvManualWait = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeFM.getText());
        } catch (NumberFormatException e) {
            nn = 0;
        }
        nn = Math.min(Math.max(nn, -191), 20);
        setting.getPmdDotNET().volumeFM = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeSSG.getText());
        } catch (NumberFormatException e) {
            nn = 0;
        }
        nn = Math.min(Math.max(nn, -191), 20);
        setting.getPmdDotNET().volumeSSG = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeRhythm.getText());
        } catch (NumberFormatException e) {
            nn = 0;
        }
        nn = Math.min(Math.max(nn, -191), 20);
        setting.getPmdDotNET().volumeRhythm = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeAdpcm.getText());
        } catch (NumberFormatException e) {
            nn = 0;
        }
        nn = Math.min(Math.max(nn, -191), 20);
        setting.getPmdDotNET().volumeAdpcm = nn;
        try {
            nn = Integer.parseInt(tbPMDVolumeGIMICSSG.getText());
        } catch (NumberFormatException e) {
            nn = 31;
        }
        nn = Math.min(Math.max(nn, 0), 127);
        setting.getPmdDotNET().volumeGIMICSSG = nn;


        setting.getKeyBoardHook().setUseKeyBoardHook(cbUseKeyBoardHook.isSelected());

        setting.getKeyBoardHook().getStop().setShift(cbStopShift.isSelected());
        setting.getKeyBoardHook().getStop().setCtrl(cbStopCtrl.isSelected());
        setting.getKeyBoardHook().getStop().setWin(cbStopWin.isSelected());
        setting.getKeyBoardHook().getStop().setAlt(cbStopAlt.isSelected());
        setting.getKeyBoardHook().getStop().setKey(lblStopKey.getText() == null || lblStopKey.getText().isEmpty() ? "(None)" : lblStopKey.getText());

        setting.getKeyBoardHook().getPause().setShift(cbPauseShift.isSelected());
        setting.getKeyBoardHook().getPause().setCtrl(cbPauseCtrl.isSelected());
        setting.getKeyBoardHook().getPause().setWin(cbPauseWin.isSelected());
        setting.getKeyBoardHook().getPause().setAlt(cbPauseAlt.isSelected());
        setting.getKeyBoardHook().getPause().setKey(lblPauseKey.getText() == null || lblPauseKey.getText().isEmpty() ? "(None)" : lblPauseKey.getText());

        setting.getKeyBoardHook().getFadeout().setShift(cbFadeoutShift.isSelected());
        setting.getKeyBoardHook().getFadeout().setCtrl(cbFadeoutCtrl.isSelected());
        setting.getKeyBoardHook().getFadeout().setWin(cbFadeoutWin.isSelected());
        setting.getKeyBoardHook().getFadeout().setAlt(cbFadeoutAlt.isSelected());
        setting.getKeyBoardHook().getFadeout().setKey(lblFadeoutKey.getText() == null || lblFadeoutKey.getText().isEmpty() ? "(None)" : lblFadeoutKey.getText());

        setting.getKeyBoardHook().getPrev().setShift(cbPrevShift.isSelected());
        setting.getKeyBoardHook().getPrev().setCtrl(cbPrevCtrl.isSelected());
        setting.getKeyBoardHook().getPrev().setWin(cbPrevWin.isSelected());
        setting.getKeyBoardHook().getPrev().setAlt(cbPrevAlt.isSelected());
        setting.getKeyBoardHook().getPrev().setKey(lblPrevKey.getText() == null || lblPrevKey.getText().isEmpty() ? "(None)" : lblPrevKey.getText());

        setting.getKeyBoardHook().getSlow().setShift(cbSlowShift.isSelected());
        setting.getKeyBoardHook().getSlow().setCtrl(cbSlowCtrl.isSelected());
        setting.getKeyBoardHook().getSlow().setWin(cbSlowWin.isSelected());
        setting.getKeyBoardHook().getSlow().setAlt(cbSlowAlt.isSelected());
        setting.getKeyBoardHook().getSlow().setKey(lblSlowKey.getText() == null || lblSlowKey.getText().isEmpty() ? "(None)" : lblSlowKey.getText());

        setting.getKeyBoardHook().getPlay().setShift(cbPlayShift.isSelected());
        setting.getKeyBoardHook().getPlay().setCtrl(cbPlayCtrl.isSelected());
        setting.getKeyBoardHook().getPlay().setWin(cbPlayWin.isSelected());
        setting.getKeyBoardHook().getPlay().setAlt(cbPlayAlt.isSelected());
        setting.getKeyBoardHook().getPlay().setKey(lblPlayKey.getText() == null || lblPlayKey.getText().isEmpty() ? "(None)" : lblPlayKey.getText());

        setting.getKeyBoardHook().getFast().setShift(cbFastShift.isSelected());
        setting.getKeyBoardHook().getFast().setCtrl(cbFastCtrl.isSelected());
        setting.getKeyBoardHook().getFast().setWin(cbFastWin.isSelected());
        setting.getKeyBoardHook().getFast().setAlt(cbFastAlt.isSelected());
        setting.getKeyBoardHook().getFast().setKey(lblFastKey.getText() == null || lblFastKey.getText().isEmpty() ? "(None)" : lblFastKey.getText());

        setting.getKeyBoardHook().getNext().setShift(cbNextShift.isSelected());
        setting.getKeyBoardHook().getNext().setCtrl(cbNextCtrl.isSelected());
        setting.getKeyBoardHook().getNext().setWin(cbNextWin.isSelected());
        setting.getKeyBoardHook().getNext().setAlt(cbNextAlt.isSelected());
        setting.getKeyBoardHook().getNext().setKey(lblNextKey.getText() == null || lblNextKey.getText().isEmpty() ? "(None)" : lblNextKey.getText());


        this.dialogResult = JFileChooser.APPROVE_OPTION;
        this.setVisible(false);
    }

    private static void setChipType2FromControls(
            ChipType2 ct
            , JCheckBox rb_SCCI
            , JComboBox cmb_SCCI
            , JCheckBox rb_EMU0
            , JCheckBox rb_EMU1
            , JCheckBox rb_EMU2
            , JCheckBox rb_SCCI_E
            , JComboBox cmb_SCCI_E1
            , JComboBox cmb_SCCI_E2
    ) {
        ct.setUseReal(new boolean[rb_SCCI_E == null ? 1 : 3]);
        ct.getUseReal()[0] = rb_SCCI.isSelected();
        ct.setrealChipInfo(new ChipType2.RealChipInfo[rb_SCCI_E == null ? 1 : 3]);
        for (int i = 0; i < ct.getRealChipInfo().length; i++) ct.getRealChipInfo()[i] = new ChipType2.RealChipInfo();
        int v;
        ChipType2.RealChipInfo rci;
        if (rb_SCCI.isSelected()) {
            if (cmb_SCCI.getSelectedItem() != null) {
                String n = cmb_SCCI.getSelectedItem().toString();
                n = n.substring(0, n.indexOf(")")).substring(1);
                String[] ns = n.split(":");
                rci = ct.getRealChipInfo()[0];
                rci.setInterfaceName(String.join(":", Arrays.copyOfRange(ns, 0, ns.length - 3)));
                try { v = Integer.parseInt(ns[ns.length - 3]); } catch (NumberFormatException e) { v = 0; }
                rci.setSoundLocation(v);
                try { v = Integer.parseInt(ns[ns.length - 2]); } catch (NumberFormatException e) { v = 0; }
                rci.setBusID(v);
                try { v = Integer.parseInt(ns[ns.length - 1]); } catch (NumberFormatException e) { v = 0; }
                rci.setSoundChip(v);
            }
        }

        ct.setUseEmu(null);
        if (rb_EMU2 != null) ct.setUseEmu(new boolean[3]);
        else if (rb_EMU1 != null) ct.setUseEmu(new boolean[2]);
        else if (rb_EMU0 != null) ct.setUseEmu(new boolean[1]);
        if (rb_EMU0 != null) ct.getUseEmu()[0] = rb_EMU0.isSelected();// (rb_EMU0.isSelected() || rb_SCCI.isSelected());
        if (rb_EMU1 != null) ct.getUseEmu()[1] = rb_EMU1.isSelected();
        if (rb_EMU2 != null) ct.getUseEmu()[2] = rb_EMU2.isSelected();

        if (rb_SCCI_E != null && rb_SCCI_E.isSelected()) {
            if (cmb_SCCI_E1.getSelectedItem() != null) {
                String n = cmb_SCCI_E1.getSelectedItem().toString();
                n = n.substring(0, n.indexOf(")")).substring(1);
                String[] ns = n.split(":");
                rci = ct.getRealChipInfo()[1];
                rci.setInterfaceName(String.join(":", Arrays.copyOfRange(ns, 0, ns.length - 3)));
                try { v = Integer.parseInt(ns[ns.length - 3]); } catch (NumberFormatException e) { v = 0; }
                rci.setSoundLocation(v);
                try { v = Integer.parseInt(ns[ns.length - 2]); } catch (NumberFormatException e) { v = 0; }
                rci.setBusID(v);
                try { v = Integer.parseInt(ns[ns.length - 1]); } catch (NumberFormatException e) { v = 0; }
                rci.setSoundChip(v);
            }
            if (cmb_SCCI_E2.getSelectedItem() != null) {
                String n = cmb_SCCI_E2.getSelectedItem().toString();
                n = n.substring(0, n.indexOf(")")).substring(1);
                String[] ns = n.split(":");
                rci = ct.getRealChipInfo()[2];
                rci.setInterfaceName(String.join(":", Arrays.copyOfRange(ns, 0, ns.length - 3)));
                try { v = Integer.parseInt(ns[ns.length - 3]); } catch (NumberFormatException e) { v = 0; }
                rci.setSoundLocation(v);
                try { v = Integer.parseInt(ns[ns.length - 2]); } catch (NumberFormatException e) { v = 0; }
                rci.setBusID(v);
                try { v = Integer.parseInt(ns[ns.length - 1]); } catch (NumberFormatException e) { v = 0; }
                rci.setSoundChip(v);
            }
        }
    }

    /**
     * 入力値チェック
     */
    private boolean checkSetting() {
        HashSet<String> hsSCCIs = new HashSet<>();
        boolean ret = false;

        // SCCI重複設定チェック

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
                    "SCCI/GIMICのデバイスが重複して設定されています。強行しますか"
                    , "警告"
                    , JOptionPane.YES_NO_OPTION
                    , JOptionPane.ERROR_MESSAGE
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

    private void btnOpenSettingFolder_Click(ActionEvent ev) {
        try {
            String fullPath = Common.settingFilePath;
            new ProcessBuilder(fullPath).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void btnDataPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("フォルダーを指定してください。");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbDataPath.setText(fbd.getSelectedFile().getPath());
    }

    private void btnSearchPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("フォルダーを指定してください。");

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
        fbd.setDialogTitle("フォルダーを指定してください。");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbDumpPath.setText(fbd.getSelectedFile().getPath());
    }

    private void btnResetPosition_Click(ActionEvent ev) {
        int res = JOptionPane.showConfirmDialog(null,
                "表示位置を全てリセットします。よろしいですか。(現在開いているウィンドウの位置はリセットできません。)",
                "確認", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.NO_OPTION) return;

        setting.setlocation(new Setting.Location());
    }

    private void cbUseMIDIExport_CheckedChanged(ChangeEvent ev) {
        gbMIDIExport.setEnabled(cbUseMIDIExport.isSelected());
    }

    private void btnMIDIOutputPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("フォルダーを指定してください。");

        if (fbd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        tbMIDIOutputPath.setText(fbd.getSelectedFile().getPath());
    }

    private void btnWavPath_Click(ActionEvent ev) {
        JFileChooser fbd = new JFileChooser();
        fbd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fbd.setDialogTitle("フォルダーを指定してください。");

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
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".dll"); }
            @Override public String getDescription() { return "VST Pluginファイル(*.dll)"; }
        });
        ofd.setDialogTitle("ファイルを選択してください");
        ofd.setFileFilter(ofd.getChoosableFileFilters()[setting.getOther().getFilterIndex()]);

        if (!setting.getOther().getDefaultDataPath().isEmpty() && Directory.exists(setting.getOther().getDefaultDataPath()) && IsInitialOpenFolder) {
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
            @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".dll"); }
            @Override public String getDescription() { return "VST Pluginファイル(*.dll)"; }
        });
        ofd.setDialogTitle("ファイルを選択してください");
        ofd.setFileFilter(ofd.getChoosableFileFilters()[setting.getOther().getFilterIndex()]);

        if (!setting.getVst().getDefaultPath().isEmpty() && Directory.exists(setting.getVst().getDefaultPath()) && IsInitialOpenFolder) {
            ofd.setCurrentDirectory(new File(setting.getVst().getDefaultPath()));
//        } else {
//            ofd.RestoreDirectory = true;
        }
//        ofd.CheckPathExists = true;
        ofd.setMultiSelectionEnabled(false);

        if (ofd.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        VstInfo s = Audio.getVSTInfo(ofd.getSelectedFile().getName());
        if (s == null) return;

        setting.getVst().setDefaultPath(Path.getDirectoryName(ofd.getSelectedFile().getName()));

        int p = tbcMIDIoutList.getSelectedIndex();
        ((DefaultTableModel) dgv[p].getModel()).addRow(new Object[] {
                -999
                , true
                , s.fileName
                , s.effectName
                , "GM"
                , "None"
                , s.vendorName
        });
    }

    private void btnSIDKernal_Click(ActionEvent ev) {
        JFileChooser ofd = new JFileChooser();
        ofd.setFileFilter(ofd.getAcceptAllFileFilter());
        ofd.setDialogTitle("ファイルを選択してください");
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
        ofd.setDialogTitle("ファイルを選択してください");
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
        ofd.setDialogTitle("ファイルを選択してください");
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

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (frmMain.keyHookMeth != null /* this::keyHookMeth */) { // TODO
                frmMain.keyHookMeth = null;
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
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btPauseSet_Click(ActionEvent ev) {
        lblKey = lblPauseKey;
        btSet = btPauseSet;
        btOK = btnOK;
        btClr = btPauseClr;
        btPauseSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btFadeoutSet_Click(ActionEvent ev) {
        lblKey = lblFadeoutKey;
        btSet = btFadeoutSet;
        btClr = btFadeoutClr;
        btOK = btnOK;
        btFadeoutSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btPrevSet_Click(ActionEvent ev) {
        lblKey = lblPrevKey;
        btSet = btPrevSet;
        btClr = btPrevClr;
        btOK = btnOK;
        btPrevSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btSlowSet_Click(ActionEvent ev) {
        lblKey = lblSlowKey;
        btSet = btSlowSet;
        btClr = btSlowClr;
        btOK = btnOK;
        btSlowSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btPlaySet_Click(ActionEvent ev) {
        lblKey = lblPlayKey;
        btSet = btPlaySet;
        btClr = btPlayClr;
        btOK = btnOK;
        btPlaySet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btFastSet_Click(ActionEvent ev) {
        lblKey = lblFastKey;
        btSet = btFastSet;
        btClr = btFastClr;
        btOK = btnOK;
        btFastSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
    }

    private void btNextSet_Click(ActionEvent ev) {
        lblKey = lblNextKey;
        btSet = btNextSet;
        btClr = btNextClr;
        btOK = btnOK;
        btNextSet.setEnabled(false);
        btnOK.setEnabled(false);
        lblKey.setText("入力待ち");
        lblKey.setForeground(Color.red);

        lblNotice = lblKeyBoardHookNotice;
        lblKeyBoardHookNotice.setVisible(true);

        frmMain.keyHookMeth = frmSetting::keyHookMeth;
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

        frmMain.keyHookMeth = null;
        btSet.setEnabled(true);
        btOK.setEnabled(true);
        btClr.setEnabled(true);
    }

    private MouseListener llOpenGithub_LinkClicked = new MouseAdapter() {
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

    private FocusListener tbPMDPPSDRVFreq_Click = new FocusAdapter() {
        public void focusGained(FocusEvent e) {
            rbPMDUsePPSDRVManualFreq_CheckedChanged(null);
        }
    };

    private MouseListener tbPMDPPSDRVFreq_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            rbPMDUsePPSDRVManualFreq_CheckedChanged(null);
        }
    };

    private FocusListener groupBox20_Enter = new FocusAdapter() {
        public void focusGained(FocusEvent e) {
        }
    };

    private void initializeComponent() {
        this.btnOK = new JButton();
        this.btnCancel = new JButton();
        this.gbWaveOut = new JPanel();
        this.cmbWaveOutDevice = new JComboBox();
        this.rbWaveOut = new JCheckBox();
        this.rbAsioOut = new JCheckBox();
        this.rbWasapiOut = new JCheckBox();
        this.gbAsioOut = new JPanel();
        this.btnASIOControlPanel = new JButton();
        this.cmbAsioDevice = new JComboBox();
        this.rbDirectSoundOut = new JCheckBox();
        this.gbWasapiOut = new JPanel();
        this.rbExclusive = new JCheckBox();
        this.rbShare = new JCheckBox();
        this.cmbWasapiDevice = new JComboBox();
        this.gbDirectSound = new JPanel();
        this.cmbDirectSoundDevice = new JComboBox();
        this.tcSetting = new JTabbedPane();
        this.tpOutput = new JTabbedPane();
        this.rbNullDevice = new JCheckBox();
        this.label36 = new JLabel();
        this.lblWaitTime = new JLabel();
        this.label66 = new JLabel();
        this.lblLatencyUnit = new JLabel();
        this.label28 = new JLabel();
        this.label65 = new JLabel();
        this.lblLatency = new JLabel();
        this.cmbWaitTime = new JComboBox();
        this.cmbSampleRate = new JComboBox();
        this.cmbLatency = new JComboBox();
        this.rbSPPCM = new JCheckBox();
        this.groupBox16 = new JPanel();
        this.cmbSPPCMDevice = new JComboBox();
        this.tpModule = new JTabbedPane();
        this.groupBox1 = new JPanel();
        this.cbUnuseRealChip = new JCheckBox();
        this.ucSI = new ucSettingInstruments();
        this.groupBox3 = new JPanel();
        this.cbHiyorimiMode = new JCheckBox();
        this.label13 = new JLabel();
        this.label12 = new JLabel();
        this.label11 = new JLabel();
        this.tbLatencyEmu = new JTextArea();
        this.tbLatencySCCI = new JTextArea();
        this.label10 = new JLabel();
        this.tpNuked = new JTabbedPane();
        this.groupBox29 = new JPanel();
        this.cbGensSSGEG = new JCheckBox();
        this.cbGensDACHPF = new JCheckBox();
        this.groupBox26 = new JPanel();
        this.rbNukedOPN2OptionYM2612u = new JCheckBox();
        this.rbNukedOPN2OptionYM2612 = new JCheckBox();
        this.rbNukedOPN2OptionDiscrete = new JCheckBox();
        this.rbNukedOPN2OptionASIClp = new JCheckBox();
        this.rbNukedOPN2OptionASIC = new JCheckBox();
        this.tpNSF = new JTabbedPane();
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
        this.tpSID = new JTabbedPane();
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
        this.tpPMDDotNET = new JTabbedPane();
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
        this.tpMIDIOut = new JTabbedPane();
        this.btnAddVST = new JButton();
        this.tbcMIDIoutList = new JTabbedPane();
        this.tabPage1 = new JTabbedPane();
        this.dgvMIDIoutListA = new JTable();
        this.JListTextBoxColumn1 = new JTextField();
        this.clmIsVST = new JCheckBox();
        this.clmFileName = new JTextArea();
        this.JListTextBoxColumn2 = new JTextArea();
        this.clmType = new JComboBox();
        this.ClmBeforeSend = new JComboBox();
        this.JListTextBoxColumn3 = new JTextArea();
        this.JListTextBoxColumn4 = new JTextArea();
        this.btnUP_A = new JButton();
        this.btnDOWN_A = new JButton();
        this.tabPage2 = new JTabbedPane();
        this.dgvMIDIoutListB = new JTable();
        this.btnUP_B = new JButton();
        this.btnDOWN_B = new JButton();
        this.tabPage3 = new JTabbedPane();
        this.dgvMIDIoutListC = new JTable();
        this.btnUP_C = new JButton();
        this.btnDOWN_C = new JButton();
        this.tabPage4 = new JTabbedPane();
        this.dgvMIDIoutListD = new JTable();
        this.btnUP_D = new JButton();
        this.btnDOWN_D = new JButton();
        this.tabPage5 = new JTabbedPane();
        this.dgvMIDIoutListE = new JTable();
        this.btnUP_E = new JButton();
        this.btnDOWN_E = new JButton();
        this.tabPage6 = new JTabbedPane();
        this.dgvMIDIoutListF = new JTable();
        this.btnUP_F = new JButton();
        this.btnDOWN_F = new JButton();
        this.tabPage7 = new JTabbedPane();
        this.dgvMIDIoutListG = new JTable();
        this.btnUP_G = new JButton();
        this.btnDOWN_G = new JButton();
        this.tabPage8 = new JTabbedPane();
        this.dgvMIDIoutListH = new JTable();
        this.btnUP_H = new JButton();
        this.btnDOWN_H = new JButton();
        this.tabPage9 = new JTabbedPane();
        this.dgvMIDIoutListI = new JTable();
        this.btnUP_I = new JButton();
        this.btnDOWN_I = new JButton();
        this.tabPage10 = new JTabbedPane();
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
        this.tpMIDIOut2 = new JTabbedPane();
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
        this.tabMIDIExp = new JTabbedPane();
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
        this.tpMIDIKBD = new JTabbedPane();
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
        this.cmbMIDIIN = new JComboBox();
        this.label5 = new JLabel();
        this.tpKeyBoard = new JTabbedPane();
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
        this.tpBalance = new JTabbedPane();
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
        this.tpPlayList = new JTabbedPane();
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
        this.tpOther = new JTabbedPane();
        this.btnSearchPath = new JButton();
        this.tbSearchPath = new JTextArea();
        this.label68 = new JLabel();
        this.cbNonRenderingForPause = new JCheckBox();
        this.cbWavSwitch = new JCheckBox();
        this.cbUseGetInst = new JCheckBox();
        this.groupBox4 = new JPanel();
        this.cmbInstFormat = new JComboBox();
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
        this.tpOmake = new JTabbedPane();
        this.label67 = new JLabel();
        this.label14 = new JLabel();
        this.btVST = new JButton();
        this.tbSCCbaseAddress = new JTextArea();
        this.tbVST = new JTextArea();
        this.groupBox5 = new JPanel();
        this.cbDispFrameCounter = new JCheckBox();
        this.tpAbout = new JTabbedPane();
        this.tableLayoutPanel = new JTable();
        this.logoBufferedImage = new JLabel();
        this.labelProductName = new JLabel();
        this.labelVersion = new JLabel();
        this.labelCopyright = new JLabel();
        this.labelCompanyName = new JLabel();
        this.textBoxDescription = new JTextArea();
        this.llOpenGithub = new JLabel();
//            this.gbWaveOut.SuspendLayout();
//            this.gbAsioOut.SuspendLayout();
//            this.gbWasapiOut.SuspendLayout();
//            this.gbDirectSound.SuspendLayout();
//            this.tcSetting.SuspendLayout();
//            this.tpOutput.SuspendLayout();
//            this.groupBox16.SuspendLayout();
//            this.tpModule.SuspendLayout();
//            this.groupBox1.SuspendLayout();
//            this.groupBox3.SuspendLayout();
//            this.tpNuked.SuspendLayout();
//            this.groupBox29.SuspendLayout();
//            this.groupBox26.SuspendLayout();
//            this.tpNSF.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.trkbNSFLPF)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.trkbNSFHPF)).BeginInit();
//            this.groupBox10.SuspendLayout();
//            this.groupBox12.SuspendLayout();
//            this.groupBox11.SuspendLayout();
//            this.groupBox9.SuspendLayout();
//            this.groupBox8.SuspendLayout();
//            this.tpSID.SuspendLayout();
//            this.groupBox28.SuspendLayout();
//            this.groupBox27.SuspendLayout();
//            this.groupBox14.SuspendLayout();
//            this.groupBox13.SuspendLayout();
//            this.tpPMDDotNET.SuspendLayout();
//            this.gbPMDManual.SuspendLayout();
//            this.groupBox32.SuspendLayout();
//            this.gbPPSDRV.SuspendLayout();
//            this.groupBox33.SuspendLayout();
//            this.gbPMDSetManualVolume.SuspendLayout();
//            this.tpMIDIOut.SuspendLayout();
//            this.tbcMIDIoutList.SuspendLayout();
//            this.tabPage1.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListA)).BeginInit();
//            this.tabPage2.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListB)).BeginInit();
//            this.tabPage3.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListC)).BeginInit();
//            this.tabPage4.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListD)).BeginInit();
//            this.tabPage5.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListE)).BeginInit();
//            this.tabPage6.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListF)).BeginInit();
//            this.tabPage7.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListG)).BeginInit();
//            this.tabPage8.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListH)).BeginInit();
//            this.tabPage9.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListI)).BeginInit();
//            this.tabPage10.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListJ)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutPallet)).BeginInit();
//            this.tpMIDIOut2.SuspendLayout();
//            this.groupBox15.SuspendLayout();
//            this.tabMIDIExp.SuspendLayout();
//            this.gbMIDIExport.SuspendLayout();
//            this.groupBox6.SuspendLayout();
//            this.tpMIDIKBD.SuspendLayout();
//            this.gbMIDIKeyboard.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox8)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox7)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox6)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox5)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox4)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox3)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
//            this.gbUseChannel.SuspendLayout();
//            this.groupBox7.SuspendLayout();
//            this.groupBox2.SuspendLayout();
//            this.tpKeyBoard.SuspendLayout();
//            this.gbUseKeyBoardHook.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox14)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox17)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox16)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox15)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox13)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox12)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox11)).BeginInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox10)).BeginInit();
//            this.tpBalance.SuspendLayout();
//            this.groupBox25.SuspendLayout();
//            this.groupBox18.SuspendLayout();
//            this.groupBox24.SuspendLayout();
//            this.groupBox21.SuspendLayout();
//            this.groupBox22.SuspendLayout();
//            this.groupBox23.SuspendLayout();
//            this.groupBox19.SuspendLayout();
//            this.groupBox20.SuspendLayout();
//            this.tpPlayList.SuspendLayout();
//            this.groupBox17.SuspendLayout();
//            this.tpOther.SuspendLayout();
//            this.groupBox4.SuspendLayout();
//            this.gbWav.SuspendLayout();
//            this.gbDump.SuspendLayout();
//            this.tpOmake.SuspendLayout();
//            this.groupBox5.SuspendLayout();
//            this.tpAbout.SuspendLayout();
//            this.tableLayoutPanel.SuspendLayout();
        //((System.ComponentModel.ISupportInitialize)(this.logoBufferedImage)).BeginInit();

        //
        // btnOK
        //
        //resources.ApplyResources(this.btnOK, "btnOK");
        this.btnOK.setName("btnOK");
        // this.btnOK.UseVisualStyl.setBackground(true);
        this.btnOK.addActionListener(this::btnOK_Click);
        //
        // btnCancel
        //
        //resources.ApplyResources(this.btnCancel, "btnCancel");
        this.btnCancel.addActionListener(e -> dialogResult = JOptionPane.NO_OPTION);
        this.btnCancel.setName("btnCancel");
        // this.btnCancel.UseVisualStyl.setBackground(true);
        //
        // gbWaveOut
        //
        //resources.ApplyResources(this.gbWaveOut, "gbWaveOut");
        this.gbWaveOut.add(this.cmbWaveOutDevice);
        this.gbWaveOut.setName("gbWaveOut");
        // this.gbWaveOut.TabStop = false;
        //
        // cmbWaveOutDevice
        //
        //resources.ApplyResources(this.cmbWaveOutDevice, "cmbWaveOutDevice");
//        this.cmbWaveOutDevice.DropDownStyle = JComboBoxStyle.DropDownList;
        //this.cmbWaveOutDevice.FormattingEnabled = true;
        this.cmbWaveOutDevice.setName("cmbWaveOutDevice");
        //
        // rbWaveOut
        //
        //resources.ApplyResources(this.rbWaveOut, "rbWaveOut");
        this.rbWaveOut.setSelected(true);
        this.rbWaveOut.setName("rbWaveOut");
        // this.rbWaveOut.TabStop = true;
        // this.rbWaveOut.UseVisualStyl.setBackground(true);
        this.rbWaveOut.addChangeListener(this::rbWaveOut_CheckedChanged);
        //
        // rbAsioOut
        //
        //resources.ApplyResources(this.rbAsioOut, "rbAsioOut");
        this.rbAsioOut.setName("rbAsioOut");
        // this.rbAsioOut.UseVisualStyl.setBackground(true);
        this.rbAsioOut.addChangeListener(this::rbAsioOut_CheckedChanged);
        //
        // rbWasapiOut
        //
        //resources.ApplyResources(this.rbWasapiOut, "rbWasapiOut");
        this.rbWasapiOut.setName("rbWasapiOut");
        // this.rbWasapiOut.UseVisualStyl.setBackground(true);
        this.rbWasapiOut.addChangeListener(this::rbWasapiOut_CheckedChanged);
        //
        // gbAsioOut
        //
        //resources.ApplyResources(this.gbAsioOut, "gbAsioOut");
        this.gbAsioOut.add(this.btnASIOControlPanel);
        this.gbAsioOut.add(this.cmbAsioDevice);
        this.gbAsioOut.setName("gbAsioOut");
        // this.gbAsioOut.TabStop = false;
        //
        // btnASIOControlPanel
        //
        //resources.ApplyResources(this.btnASIOControlPanel, "btnASIOControlPanel");
        this.btnASIOControlPanel.setName("btnASIOControlPanel");
        // this.btnASIOControlPanel.UseVisualStyl.setBackground(true);
        this.btnASIOControlPanel.addActionListener(this::btnASIOControlPanel_Click);
        //
        // cmbAsioDevice
        //
        //resources.ApplyResources(this.cmbAsioDevice, "cmbAsioDevice");
//        this.cmbAsioDevice.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbAsioDevice.FormattingEnabled = true;
        this.cmbAsioDevice.setName("cmbAsioDevice");
        //
        // rbDirectSoundOut
        //
        //resources.ApplyResources(this.rbDirectSoundOut, "rbDirectSoundOut");
        this.rbDirectSoundOut.setName("rbDirectSoundOut");
        // this.rbDirectSoundOut.UseVisualStyl.setBackground(true);
        this.rbDirectSoundOut.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // gbWasapiOut
        //
        //resources.ApplyResources(this.gbWasapiOut, "gbWasapiOut");
        this.gbWasapiOut.add(this.rbExclusive);
        this.gbWasapiOut.add(this.rbShare);
        this.gbWasapiOut.add(this.cmbWasapiDevice);
        this.gbWasapiOut.setName("gbWasapiOut");
        // this.gbWasapiOut.TabStop = false;
        //
        // rbExclusive
        //
        //resources.ApplyResources(this.rbExclusive, "rbExclusive");
        this.rbExclusive.setName("rbExclusive");
        // this.rbExclusive.TabStop = true;
        // this.rbExclusive.UseVisualStyl.setBackground(true);
        //
        // rbShare
        //
        //resources.ApplyResources(this.rbShare, "rbShare");
        this.rbShare.setName("rbShare");
        // this.rbShare.TabStop = true;
        // this.rbShare.UseVisualStyl.setBackground(true);
        //
        // cmbWasapiDevice
        //
        //resources.ApplyResources(this.cmbWasapiDevice, "cmbWasapiDevice");
//        this.cmbWasapiDevice.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbWasapiDevice.FormattingEnabled = true;
        this.cmbWasapiDevice.setName("cmbWasapiDevice");
        //
        // gbDirectSound
        //
        //resources.ApplyResources(this.gbDirectSound, "gbDirectSound");
        this.gbDirectSound.add(this.cmbDirectSoundDevice);
        this.gbDirectSound.setName("gbDirectSound");
        // this.gbDirectSound.TabStop = false;
        //
        // cmbDirectSoundDevice
        //
        //resources.ApplyResources(this.cmbDirectSoundDevice, "cmbDirectSoundDevice");
//        this.cmbDirectSoundDevice.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbDirectSoundDevice.FormattingEnabled = true;
        this.cmbDirectSoundDevice.setName("cmbDirectSoundDevice");
        //
        // tcSetting
        //
        //resources.ApplyResources(this.tcSetting, "tcSetting");
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
//        this.tcSetting.HotTrack = true;
//        this.tcSetting.Multiline = true;
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
        //resources.ApplyResources(this.tpOutput, "tpOutput");
        this.tpOutput.setName("tpOutput");
        // this.tpOutput.UseVisualStyl.setBackground(true);
        //
        // rbNullDevice
        //
        //resources.ApplyResources(this.rbNullDevice, "rbNullDevice");
        this.rbNullDevice.setName("rbNullDevice");
        // this.rbNullDevice.UseVisualStyl.setBackground(true);
        this.rbNullDevice.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // label36
        //
        //resources.ApplyResources(this.label36, "label36");
        this.label36.setName("label36");
        //
        // lblWaitTime
        //
        //resources.ApplyResources(this.lblWaitTime, "lblWaitTime");
        this.lblWaitTime.setName("lblWaitTime");
        //
        // label66
        //
        //resources.ApplyResources(this.label66, "label66");
        this.label66.setName("label66");
        //
        // lblLatencyUnit
        //
        //resources.ApplyResources(this.lblLatencyUnit, "lblLatencyUnit");
        this.lblLatencyUnit.setName("lblLatencyUnit");
        //
        // label28
        //
        //resources.ApplyResources(this.label28, "label28");
        this.label28.setName("label28");
        //
        // label65
        //
        //resources.ApplyResources(this.label65, "label65");
        this.label65.setName("label65");
        //
        // lblLatency
        //
        //resources.ApplyResources(this.lblLatency, "lblLatency");
        this.lblLatency.setName("lblLatency");
        //
        // cmbWaitTime
        //
//        this.cmbWaitTime.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbWaitTime.FormattingEnabled = true;
        DefaultComboBoxModel m = (DefaultComboBoxModel) this.cmbWaitTime.getModel();
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items1"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items2"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items3"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items4"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items5"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items6"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items7"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items8"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items9"));
        m.addElement(Resources.getResourceManager().getString("cmbWaitTime.Items10"));
        //resources.ApplyResources(this.cmbWaitTime, "cmbWaitTime");
        this.cmbWaitTime.setName("cmbWaitTime");
        //
        // cmbSampleRate
        //
        //resources.ApplyResources(this.cmbSampleRate, "cmbSampleRate");
//        this.cmbSampleRate.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbSampleRate.FormattingEnabled = true;
        m = (DefaultComboBoxModel) this.cmbSampleRate.getModel();
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items"));
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items1"));
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items2"));
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items3"));
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items4"));
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items5"));
        m.addElement(Resources.getResourceManager().getString("cmbSampleRate.Items6"));
        this.cmbSampleRate.setName("cmbSampleRate");
        //
        // cmbLatency
        //
//        this.cmbLatency.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbLatency.FormattingEnabled = true;
        m = (DefaultComboBoxModel) this.cmbLatency.getModel();
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items1"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items2"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items3"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items4"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items5"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items6"));
        m.addElement(Resources.getResourceManager().getString("cmbLatency.Items7"));
        //resources.ApplyResources(this.cmbLatency, "cmbLatency");
        this.cmbLatency.setName("cmbLatency");
        //
        // rbSPPCM
        //
        //resources.ApplyResources(this.rbSPPCM, "rbSPPCM");
        this.rbSPPCM.setName("rbSPPCM");
        // this.rbSPPCM.UseVisualStyl.setBackground(true);
        this.rbSPPCM.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // groupBox16
        //
        //resources.ApplyResources(this.groupBox16, "groupBox16");
        this.groupBox16.add(this.cmbSPPCMDevice);
        this.groupBox16.setName("groupBox16");
        // this.groupBox16.TabStop = false;
        //
        // cmbSPPCMDevice
        //
        //resources.ApplyResources(this.cmbSPPCMDevice, "cmbSPPCMDevice");
//        this.cmbSPPCMDevice.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbSPPCMDevice.FormattingEnabled = true;
        this.cmbSPPCMDevice.setName("cmbSPPCMDevice");
        //
        // tpModule
        //
        this.tpModule.add(this.groupBox1);
        this.tpModule.add(this.groupBox3);
        //resources.ApplyResources(this.tpModule, "tpModule");
        this.tpModule.setName("tpModule");
        // this.tpModule.UseVisualStyl.setBackground(true);
        //
        // groupBox1
        //
        //resources.ApplyResources(this.groupBox1, "groupBox1");
        this.groupBox1.add(this.cbUnuseRealChip);
        this.groupBox1.add(this.ucSI);
        this.groupBox1.setName("groupBox1");
        // this.groupBox1.TabStop = false;
        //
        // cbUnuseRealChip
        //
        //resources.ApplyResources(this.cbUnuseRealChip, "cbUnuseRealChip");
        this.cbUnuseRealChip.setName("cbUnuseRealChip");
        // this.cbUnuseRealChip.UseVisualStyl.setBackground(true);
        //
        // ucSI
        //
        //resources.ApplyResources(this.ucSI, "ucSI");
        this.ucSI.setName("ucSI");
        //
        // groupBox3
        //
        //resources.ApplyResources(this.groupBox3, "groupBox3");
        this.groupBox3.add(this.cbHiyorimiMode);
        this.groupBox3.add(this.label13);
        this.groupBox3.add(this.label12);
        this.groupBox3.add(this.label11);
        this.groupBox3.add(this.tbLatencyEmu);
        this.groupBox3.add(this.tbLatencySCCI);
        this.groupBox3.add(this.label10);
        this.groupBox3.setName("groupBox3");
        // this.groupBox3.TabStop = false;
        //
        // cbHiyorimiMode
        //
        //resources.ApplyResources(this.cbHiyorimiMode, "cbHiyorimiMode");
        this.cbHiyorimiMode.setName("cbHiyorimiMode");
        // this.cbHiyorimiMode.UseVisualStyl.setBackground(true);
        //
        // label13
        //
        //resources.ApplyResources(this.label13, "label13");
        this.label13.setName("label13");
        //
        // label12
        //
        //resources.ApplyResources(this.label12, "label12");
        this.label12.setName("label12");
        //
        // label11
        //
        //resources.ApplyResources(this.label11, "label11");
        this.label11.setName("label11");
        //
        // tbLatencyEmu
        //
        //resources.ApplyResources(this.tbLatencyEmu, "tbLatencyEmu");
        this.tbLatencyEmu.setName("tbLatencyEmu");
        //
        // tbLatencySCCI
        //
        //resources.ApplyResources(this.tbLatencySCCI, "tbLatencySCCI");
        this.tbLatencySCCI.setName("tbLatencySCCI");
        //
        // label10
        //
        //resources.ApplyResources(this.label10, "label10");
        this.label10.setName("label10");
        //
        // tpNuked
        //
        this.tpNuked.add(this.groupBox29);
        this.tpNuked.add(this.groupBox26);
        //resources.ApplyResources(this.tpNuked, "tpNuked");
        this.tpNuked.setName("tpNuked");
        // this.tpNuked.UseVisualStyl.setBackground(true);
        //
        // groupBox29
        //
        this.groupBox29.add(this.cbGensSSGEG);
        this.groupBox29.add(this.cbGensDACHPF);
        //resources.ApplyResources(this.groupBox29, "groupBox29");
        this.groupBox29.setName("groupBox29");
        // this.groupBox29.TabStop = false;
        //
        // cbGensSSGEG
        //
        //resources.ApplyResources(this.cbGensSSGEG, "cbGensSSGEG");
        this.cbGensSSGEG.setName("cbGensSSGEG");
        // this.cbGensSSGEG.UseVisualStyl.setBackground(true);
        //
        // cbGensDACHPF
        //
        //resources.ApplyResources(this.cbGensDACHPF, "cbGensDACHPF");
        this.cbGensDACHPF.setName("cbGensDACHPF");
        // this.cbGensDACHPF.UseVisualStyl.setBackground(true);
        //
        // groupBox26
        //
        this.groupBox26.add(this.rbNukedOPN2OptionYM2612u);
        this.groupBox26.add(this.rbNukedOPN2OptionYM2612);
        this.groupBox26.add(this.rbNukedOPN2OptionDiscrete);
        this.groupBox26.add(this.rbNukedOPN2OptionASIClp);
        this.groupBox26.add(this.rbNukedOPN2OptionASIC);
        //resources.ApplyResources(this.groupBox26, "groupBox26");
        this.groupBox26.setName("groupBox26");
        // this.groupBox26.TabStop = false;
        //
        // rbNukedOPN2OptionYM2612u
        //
        //resources.ApplyResources(this.rbNukedOPN2OptionYM2612u, "rbNukedOPN2OptionYM2612u");
        this.rbNukedOPN2OptionYM2612u.setName("rbNukedOPN2OptionYM2612u");
        // this.rbNukedOPN2OptionYM2612u.TabStop = true;
        // this.rbNukedOPN2OptionYM2612u.UseVisualStyl.setBackground(true);
        //
        // rbNukedOPN2OptionYM2612
        //
        //resources.ApplyResources(this.rbNukedOPN2OptionYM2612, "rbNukedOPN2OptionYM2612");
        this.rbNukedOPN2OptionYM2612.setName("rbNukedOPN2OptionYM2612");
        // this.rbNukedOPN2OptionYM2612.TabStop = true;
        // this.rbNukedOPN2OptionYM2612.UseVisualStyl.setBackground(true);
        //
        // rbNukedOPN2OptionDiscrete
        //
        //resources.ApplyResources(this.rbNukedOPN2OptionDiscrete, "rbNukedOPN2OptionDiscrete");
        this.rbNukedOPN2OptionDiscrete.setName("rbNukedOPN2OptionDiscrete");
        // this.rbNukedOPN2OptionDiscrete.TabStop = true;
        // this.rbNukedOPN2OptionDiscrete.UseVisualStyl.setBackground(true);
        //
        // rbNukedOPN2OptionASIClp
        //
        //resources.ApplyResources(this.rbNukedOPN2OptionASIClp, "rbNukedOPN2OptionASIClp");
        this.rbNukedOPN2OptionASIClp.setName("rbNukedOPN2OptionASIClp");
        // this.rbNukedOPN2OptionASIClp.TabStop = true;
        // this.rbNukedOPN2OptionASIClp.UseVisualStyl.setBackground(true);
        //
        // rbNukedOPN2OptionASIC
        //
        //resources.ApplyResources(this.rbNukedOPN2OptionASIC, "rbNukedOPN2OptionASIC");
        this.rbNukedOPN2OptionASIC.setName("rbNukedOPN2OptionASIC");
        // this.rbNukedOPN2OptionASIC.TabStop = true;
        // this.rbNukedOPN2OptionASIC.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.tpNSF, "tpNSF");
        this.tpNSF.setName("tpNSF");
        // this.tpNSF.UseVisualStyl.setBackground(true);
        //
        // trkbNSFLPF
        //
        //resources.ApplyResources(this.trkbNSFLPF, "trkbNSFLPF");
        this.trkbNSFLPF.setMaximum(400);
        this.trkbNSFLPF.setName("trkbNSFLPF");
//        this.trkbNSFLPF.TickFrequency = 10;
        //
        // label53
        //
        //resources.ApplyResources(this.label53, "label53");
        this.label53.setName("label53");
        //
        // label52
        //
        //resources.ApplyResources(this.label52, "label52");
        this.label52.setName("label52");
        //
        // trkbNSFHPF
        //
        //resources.ApplyResources(this.trkbNSFHPF, "trkbNSFHPF");
        this.trkbNSFHPF.setMaximum(256);
        this.trkbNSFHPF.setName("trkbNSFHPF");
//        this.trkbNSFHPF.TickFrequency = 10;
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
        //resources.ApplyResources(this.groupBox10, "groupBox10");
        this.groupBox10.setName("groupBox10");
        // this.groupBox10.TabStop = false;
        //
        // cbNSFDmc_DPCMReverse
        //
        //resources.ApplyResources(this.cbNSFDmc_DPCMReverse, "cbNSFDmc_DPCMReverse");
        this.cbNSFDmc_DPCMReverse.setName("cbNSFDmc_DPCMReverse");
        // this.cbNSFDmc_DPCMReverse.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_RandomizeTri
        //
        //resources.ApplyResources(this.cbNSFDmc_RandomizeTri, "cbNSFDmc_RandomizeTri");
        this.cbNSFDmc_RandomizeTri.setName("cbNSFDmc_RandomizeTri");
        // this.cbNSFDmc_RandomizeTri.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_TriMute
        //
        //resources.ApplyResources(this.cbNSFDmc_TriMute, "cbNSFDmc_TriMute");
        this.cbNSFDmc_TriMute.setName("cbNSFDmc_TriMute");
        // this.cbNSFDmc_TriMute.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_RandomizeNoise
        //
        //resources.ApplyResources(this.cbNSFDmc_RandomizeNoise, "cbNSFDmc_RandomizeNoise");
        this.cbNSFDmc_RandomizeNoise.setName("cbNSFDmc_RandomizeNoise");
        // this.cbNSFDmc_RandomizeNoise.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_DPCMAntiClick
        //
        //resources.ApplyResources(this.cbNSFDmc_DPCMAntiClick, "cbNSFDmc_DPCMAntiClick");
        this.cbNSFDmc_DPCMAntiClick.setName("cbNSFDmc_DPCMAntiClick");
        // this.cbNSFDmc_DPCMAntiClick.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_EnablePNoise
        //
        //resources.ApplyResources(this.cbNSFDmc_EnablePNoise, "cbNSFDmc_EnablePNoise");
        this.cbNSFDmc_EnablePNoise.setName("cbNSFDmc_EnablePNoise");
        // this.cbNSFDmc_EnablePNoise.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_Enable4011
        //
        //resources.ApplyResources(this.cbNSFDmc_Enable4011, "cbNSFDmc_Enable4011");
        this.cbNSFDmc_Enable4011.setName("cbNSFDmc_Enable4011");
        // this.cbNSFDmc_Enable4011.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_NonLinearMixer
        //
        //resources.ApplyResources(this.cbNSFDmc_NonLinearMixer, "cbNSFDmc_NonLinearMixer");
        this.cbNSFDmc_NonLinearMixer.setName("cbNSFDmc_NonLinearMixer");
        // this.cbNSFDmc_NonLinearMixer.UseVisualStyl.setBackground(true);
        //
        // cbNSFDmc_UnmuteOnReset
        //
        //resources.ApplyResources(this.cbNSFDmc_UnmuteOnReset, "cbNSFDmc_UnmuteOnReset");
        this.cbNSFDmc_UnmuteOnReset.setName("cbNSFDmc_UnmuteOnReset");
        // this.cbNSFDmc_UnmuteOnReset.UseVisualStyl.setBackground(true);
        //
        // groupBox12
        //
        this.groupBox12.add(this.cbNSFN160_Serial);
        //resources.ApplyResources(this.groupBox12, "groupBox12");
        this.groupBox12.setName("groupBox12");
        // this.groupBox12.TabStop = false;
        //
        // cbNSFN160_Serial
        //
        //resources.ApplyResources(this.cbNSFN160_Serial, "cbNSFN160_Serial");
        this.cbNSFN160_Serial.setName("cbNSFN160_Serial");
        // this.cbNSFN160_Serial.UseVisualStyl.setBackground(true);
        //
        // groupBox11
        //
        this.groupBox11.add(this.cbNSFMmc5_PhaseRefresh);
        this.groupBox11.add(this.cbNSFMmc5_NonLinearMixer);
        //resources.ApplyResources(this.groupBox11, "groupBox11");
        this.groupBox11.setName("groupBox11");
        // this.groupBox11.TabStop = false;
        //
        // cbNSFMmc5_PhaseRefresh
        //
        //resources.ApplyResources(this.cbNSFMmc5_PhaseRefresh, "cbNSFMmc5_PhaseRefresh");
        this.cbNSFMmc5_PhaseRefresh.setName("cbNSFMmc5_PhaseRefresh");
        // this.cbNSFMmc5_PhaseRefresh.UseVisualStyl.setBackground(true);
        //
        // cbNSFMmc5_NonLinearMixer
        //
        //resources.ApplyResources(this.cbNSFMmc5_NonLinearMixer, "cbNSFMmc5_NonLinearMixer");
        this.cbNSFMmc5_NonLinearMixer.setName("cbNSFMmc5_NonLinearMixer");
        // this.cbNSFMmc5_NonLinearMixer.UseVisualStyl.setBackground(true);
        //
        // groupBox9
        //
        this.groupBox9.add(this.cbNFSNes_DutySwap);
        this.groupBox9.add(this.cbNFSNes_PhaseRefresh);
        this.groupBox9.add(this.cbNFSNes_NonLinearMixer);
        this.groupBox9.add(this.cbNFSNes_UnmuteOnReset);
        //resources.ApplyResources(this.groupBox9, "groupBox9");
        this.groupBox9.setName("groupBox9");
        // this.groupBox9.TabStop = false;
        //
        // cbNFSNes_DutySwap
        //
        //resources.ApplyResources(this.cbNFSNes_DutySwap, "cbNFSNes_DutySwap");
        this.cbNFSNes_DutySwap.setName("cbNFSNes_DutySwap");
        // this.cbNFSNes_DutySwap.UseVisualStyl.setBackground(true);
        //
        // cbNFSNes_PhaseRefresh
        //
        //resources.ApplyResources(this.cbNFSNes_PhaseRefresh, "cbNFSNes_PhaseRefresh");
        this.cbNFSNes_PhaseRefresh.setName("cbNFSNes_PhaseRefresh");
        // this.cbNFSNes_PhaseRefresh.UseVisualStyl.setBackground(true);
        //
        // cbNFSNes_NonLinearMixer
        //
        //resources.ApplyResources(this.cbNFSNes_NonLinearMixer, "cbNFSNes_NonLinearMixer");
        this.cbNFSNes_NonLinearMixer.setName("cbNFSNes_NonLinearMixer");
        // this.cbNFSNes_NonLinearMixer.UseVisualStyl.setBackground(true);
        //
        // cbNFSNes_UnmuteOnReset
        //
        //resources.ApplyResources(this.cbNFSNes_UnmuteOnReset, "cbNFSNes_UnmuteOnReset");
        this.cbNFSNes_UnmuteOnReset.setName("cbNFSNes_UnmuteOnReset");
        // this.cbNFSNes_UnmuteOnReset.UseVisualStyl.setBackground(true);
        //
        // groupBox8
        //
        this.groupBox8.add(this.label21);
        this.groupBox8.add(this.label20);
        this.groupBox8.add(this.tbNSFFds_LPF);
        this.groupBox8.add(this.cbNFSFds_4085Reset);
        this.groupBox8.add(this.cbNSFFDSWriteDisable8000);
        //resources.ApplyResources(this.groupBox8, "groupBox8");
        this.groupBox8.setName("groupBox8");
        // this.groupBox8.TabStop = false;
        //
        // label21
        //
        //resources.ApplyResources(this.label21, "label21");
        this.label21.setName("label21");
        //
        // label20
        //
        //resources.ApplyResources(this.label20, "label20");
        this.label20.setName("label20");
        //
        // tbNSFFds_LPF
        //
        //resources.ApplyResources(this.tbNSFFds_LPF, "tbNSFFds_LPF");
        this.tbNSFFds_LPF.setName("tbNSFFds_LPF");
        //
        // cbNFSFds_4085Reset
        //
        //resources.ApplyResources(this.cbNFSFds_4085Reset, "cbNFSFds_4085Reset");
        this.cbNFSFds_4085Reset.setName("cbNFSFds_4085Reset");
        // this.cbNFSFds_4085Reset.UseVisualStyl.setBackground(true);
        //
        // cbNSFFDSWriteDisable8000
        //
        //resources.ApplyResources(this.cbNSFFDSWriteDisable8000, "cbNSFFDSWriteDisable8000");
        this.cbNSFFDSWriteDisable8000.setName("cbNSFFDSWriteDisable8000");
        // this.cbNSFFDSWriteDisable8000.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.tpSID, "tpSID");
        this.tpSID.setName("tpSID");
        // this.tpSID.UseVisualStyl.setBackground(true);
        //
        // groupBox28
        //
        this.groupBox28.add(this.cbSIDModel_Force);
        this.groupBox28.add(this.rbSIDModel_8580);
        this.groupBox28.add(this.rbSIDModel_6581);
        //resources.ApplyResources(this.groupBox28, "groupBox28");
        this.groupBox28.setName("groupBox28");
        // this.groupBox28.TabStop = false;
        //
        // cbSIDModel_Force
        //
        //resources.ApplyResources(this.cbSIDModel_Force, "cbSIDModel_Force");
        this.cbSIDModel_Force.setName("cbSIDModel_Force");
        // this.cbSIDModel_Force.UseVisualStyl.setBackground(true);
        //
        // rbSIDModel_8580
        //
        //resources.ApplyResources(this.rbSIDModel_8580, "rbSIDModel_8580");
        this.rbSIDModel_8580.setName("rbSIDModel_8580");
        // this.rbSIDModel_8580.UseVisualStyl.setBackground(true);
        //
        // rbSIDModel_6581
        //
        //resources.ApplyResources(this.rbSIDModel_6581, "rbSIDModel_6581");
        this.rbSIDModel_6581.setSelected(true);
        this.rbSIDModel_6581.setName("rbSIDModel_6581");
        // this.rbSIDModel_6581.TabStop = true;
        // this.rbSIDModel_6581.UseVisualStyl.setBackground(true);
        //
        // groupBox27
        //
        this.groupBox27.add(this.cbSIDC64Model_Force);
        this.groupBox27.add(this.rbSIDC64Model_DREAN);
        this.groupBox27.add(this.rbSIDC64Model_OLDNTSC);
        this.groupBox27.add(this.rbSIDC64Model_NTSC);
        this.groupBox27.add(this.rbSIDC64Model_PAL);
        //resources.ApplyResources(this.groupBox27, "groupBox27");
        this.groupBox27.setName("groupBox27");
        // this.groupBox27.TabStop = false;
        //
        // cbSIDC64Model_Force
        //
        //resources.ApplyResources(this.cbSIDC64Model_Force, "cbSIDC64Model_Force");
        this.cbSIDC64Model_Force.setName("cbSIDC64Model_Force");
        // this.cbSIDC64Model_Force.UseVisualStyl.setBackground(true);
        //
        // rbSIDC64Model_DREAN
        //
        //resources.ApplyResources(this.rbSIDC64Model_DREAN, "rbSIDC64Model_DREAN");
        this.rbSIDC64Model_DREAN.setName("rbSIDC64Model_DREAN");
        // this.rbSIDC64Model_DREAN.UseVisualStyl.setBackground(true);
        //
        // rbSIDC64Model_OLDNTSC
        //
        //resources.ApplyResources(this.rbSIDC64Model_OLDNTSC, "rbSIDC64Model_OLDNTSC");
        this.rbSIDC64Model_OLDNTSC.setName("rbSIDC64Model_OLDNTSC");
        // this.rbSIDC64Model_OLDNTSC.UseVisualStyl.setBackground(true);
        //
        // rbSIDC64Model_NTSC
        //
        //resources.ApplyResources(this.rbSIDC64Model_NTSC, "rbSIDC64Model_NTSC");
        this.rbSIDC64Model_NTSC.setName("rbSIDC64Model_NTSC");
        // this.rbSIDC64Model_NTSC.UseVisualStyl.setBackground(true);
        //
        // rbSIDC64Model_PAL
        //
        //resources.ApplyResources(this.rbSIDC64Model_PAL, "rbSIDC64Model_PAL");
        this.rbSIDC64Model_PAL.setSelected(true);
        this.rbSIDC64Model_PAL.setName("rbSIDC64Model_PAL");
        // this.rbSIDC64Model_PAL.TabStop = true;
        // this.rbSIDC64Model_PAL.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.groupBox14, "groupBox14");
        this.groupBox14.setName("groupBox14");
        // this.groupBox14.TabStop = false;
        //
        // label27
        //
        //resources.ApplyResources(this.label27, "label27");
        this.label27.setName("label27");
        //
        // label26
        //
        //resources.ApplyResources(this.label26, "label26");
        this.label26.setName("label26");
        //
        // label25
        //
        //resources.ApplyResources(this.label25, "label25");
        this.label25.setName("label25");
        //
        // rdSIDQ1
        //
        //resources.ApplyResources(this.rdSIDQ1, "rdSIDQ1");
        this.rdSIDQ1.setSelected(true);
        this.rdSIDQ1.setName("rdSIDQ1");
        // this.rdSIDQ1.TabStop = true;
        // this.rdSIDQ1.UseVisualStyl.setBackground(true);
        //
        // rdSIDQ3
        //
        //resources.ApplyResources(this.rdSIDQ3, "rdSIDQ3");
        this.rdSIDQ3.setName("rdSIDQ3");
        // this.rdSIDQ3.UseVisualStyl.setBackground(true);
        //
        // rdSIDQ2
        //
        //resources.ApplyResources(this.rdSIDQ2, "rdSIDQ2");
        this.rdSIDQ2.setName("rdSIDQ2");
        // this.rdSIDQ2.UseVisualStyl.setBackground(true);
        //
        // rdSIDQ4
        //
        //resources.ApplyResources(this.rdSIDQ4, "rdSIDQ4");
        this.rdSIDQ4.setName("rdSIDQ4");
        // this.rdSIDQ4.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.groupBox13, "groupBox13");
        this.groupBox13.setName("groupBox13");
        // this.groupBox13.TabStop = false;
        //
        // btnSIDBasic
        //
        //resources.ApplyResources(this.btnSIDBasic, "btnSIDBasic");
        this.btnSIDBasic.setName("btnSIDBasic");
        // this.btnSIDBasic.UseVisualStyl.setBackground(true);
        this.btnSIDBasic.addActionListener(this::btnSIDBasic_Click);
        //
        // btnSIDCharacter
        //
        //resources.ApplyResources(this.btnSIDCharacter, "btnSIDCharacter");
        this.btnSIDCharacter.setName("btnSIDCharacter");
        // this.btnSIDCharacter.UseVisualStyl.setBackground(true);
        this.btnSIDCharacter.addActionListener(this::btnSIDCharacter_Click);
        //
        // btnSIDKernal
        //
        //resources.ApplyResources(this.btnSIDKernal, "btnSIDKernal");
        this.btnSIDKernal.setName("btnSIDKernal");
        // this.btnSIDKernal.UseVisualStyl.setBackground(true);
        this.btnSIDKernal.addActionListener(this::btnSIDKernal_Click);
        //
        // tbSIDCharacter
        //
        //resources.ApplyResources(this.tbSIDCharacter, "tbSIDCharacter");
        this.tbSIDCharacter.setName("tbSIDCharacter");
        //
        // tbSIDBasic
        //
        //resources.ApplyResources(this.tbSIDBasic, "tbSIDBasic");
        this.tbSIDBasic.setName("tbSIDBasic");
        //
        // tbSIDKernal
        //
        //resources.ApplyResources(this.tbSIDKernal, "tbSIDKernal");
        this.tbSIDKernal.setName("tbSIDKernal");
        //
        // label24
        //
        //resources.ApplyResources(this.label24, "label24");
        this.label24.setName("label24");
        //
        // label23
        //
        //resources.ApplyResources(this.label23, "label23");
        this.label23.setName("label23");
        //
        // label22
        //
        //resources.ApplyResources(this.label22, "label22");
        this.label22.setName("label22");
        //
        // tbSIDOutputBufferSize
        //
        //resources.ApplyResources(this.tbSIDOutputBufferSize, "tbSIDOutputBufferSize");
        this.tbSIDOutputBufferSize.setName("tbSIDOutputBufferSize");
        //
        // label51
        //
        //resources.ApplyResources(this.label51, "label51");
        this.label51.setName("label51");
        //
        // label49
        //
        //resources.ApplyResources(this.label49, "label49");
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
        //resources.ApplyResources(this.tpPMDDotNET, "tpPMDDotNET");
        this.tpPMDDotNET.setName("tpPMDDotNET");
        // this.tpPMDDotNET.UseVisualStyl.setBackground(true);
        //
        // rbPMDManual
        //
        //resources.ApplyResources(this.rbPMDManual, "rbPMDManual");
        this.rbPMDManual.setName("rbPMDManual");
        // this.rbPMDManual.TabStop = true;
        // this.rbPMDManual.UseVisualStyl.setBackground(true);
        this.rbPMDManual.addChangeListener(this::rbPMDManual_CheckedChanged);
        //
        // rbPMDAuto
        //
        //resources.ApplyResources(this.rbPMDAuto, "rbPMDAuto");
        this.rbPMDAuto.setName("rbPMDAuto");
        // this.rbPMDAuto.TabStop = true;
        // this.rbPMDAuto.UseVisualStyl.setBackground(true);
        //
        // btnPMDResetDriverArguments
        //
        //resources.ApplyResources(this.btnPMDResetDriverArguments, "btnPMDResetDriverArguments");
        this.btnPMDResetDriverArguments.setName("btnPMDResetDriverArguments");
        // this.btnPMDResetDriverArguments.UseVisualStyl.setBackground(true);
        this.btnPMDResetDriverArguments.addActionListener(this::btnPMDResetDriverArguments_Click);
        //
        // label54
        //
        //resources.ApplyResources(this.label54, "label54");
        this.label54.setName("label54");
        //
        // btnPMDResetCompilerArhguments
        //
        //resources.ApplyResources(this.btnPMDResetCompilerArhguments, "btnPMDResetCompilerArhguments");
        this.btnPMDResetCompilerArhguments.setName("btnPMDResetCompilerArhguments");
        // this.btnPMDResetCompilerArhguments.UseVisualStyl.setBackground(true);
        this.btnPMDResetCompilerArhguments.addActionListener(this::btnPMDResetCompilerArhguments_Click);
        //
        // tbPMDDriverArguments
        //
        //resources.ApplyResources(this.tbPMDDriverArguments, "tbPMDDriverArguments");
        this.tbPMDDriverArguments.setName("tbPMDDriverArguments");
        //
        // label55
        //
        //resources.ApplyResources(this.label55, "label55");
        this.label55.setName("label55");
        //
        // tbPMDCompilerArguments
        //
        //resources.ApplyResources(this.tbPMDCompilerArguments, "tbPMDCompilerArguments");
        this.tbPMDCompilerArguments.setName("tbPMDCompilerArguments");
        //
        // gbPMDManual
        //
        //resources.ApplyResources(this.gbPMDManual, "gbPMDManual");
        this.gbPMDManual.add(this.cbPMDSetManualVolume);
        this.gbPMDManual.add(this.cbPMDUsePPZ8);
        this.gbPMDManual.add(this.groupBox32);
        this.gbPMDManual.add(this.cbPMDUsePPSDRV);
        this.gbPMDManual.add(this.gbPPSDRV);
        this.gbPMDManual.add(this.gbPMDSetManualVolume);
        this.gbPMDManual.setName("gbPMDManual");
        // this.gbPMDManual.TabStop = false;
        //
        // cbPMDSetManualVolume
        //
        //resources.ApplyResources(this.cbPMDSetManualVolume, "cbPMDSetManualVolume");
        this.cbPMDSetManualVolume.setName("cbPMDSetManualVolume");
        // this.cbPMDSetManualVolume.UseVisualStyl.setBackground(true);
        this.cbPMDSetManualVolume.addChangeListener(this::cbPMDSetManualVolume_CheckedChanged);
        //
        // cbPMDUsePPZ8
        //
        //resources.ApplyResources(this.cbPMDUsePPZ8, "cbPMDUsePPZ8");
        this.cbPMDUsePPZ8.setName("cbPMDUsePPZ8");
        // this.cbPMDUsePPZ8.UseVisualStyl.setBackground(true);
        //
        // groupBox32
        //
        this.groupBox32.add(this.rbPMD86B);
        this.groupBox32.add(this.rbPMDSpbB);
        this.groupBox32.add(this.rbPMDNrmB);
        //resources.ApplyResources(this.groupBox32, "groupBox32");
        this.groupBox32.setName("groupBox32");
        // this.groupBox32.TabStop = false;
        //
        // rbPMD86B
        //
        //resources.ApplyResources(this.rbPMD86B, "rbPMD86B");
        this.rbPMD86B.setName("rbPMD86B");
        // this.rbPMD86B.TabStop = true;
        // this.rbPMD86B.UseVisualStyl.setBackground(true);
        //
        // rbPMDSpbB
        //
        //resources.ApplyResources(this.rbPMDSpbB, "rbPMDSpbB");
        this.rbPMDSpbB.setName("rbPMDSpbB");
        // this.rbPMDSpbB.TabStop = true;
        // this.rbPMDSpbB.UseVisualStyl.setBackground(true);
        //
        // rbPMDNrmB
        //
        //resources.ApplyResources(this.rbPMDNrmB, "rbPMDNrmB");
        this.rbPMDNrmB.setName("rbPMDNrmB");
        // this.rbPMDNrmB.TabStop = true;
        // this.rbPMDNrmB.UseVisualStyl.setBackground(true);
        //
        // cbPMDUsePPSDRV
        //
        //resources.ApplyResources(this.cbPMDUsePPSDRV, "cbPMDUsePPSDRV");
        this.cbPMDUsePPSDRV.setName("cbPMDUsePPSDRV");
        // this.cbPMDUsePPSDRV.UseVisualStyl.setBackground(true);
        this.cbPMDUsePPSDRV.addChangeListener(this::cbPMDUsePPSDRV_CheckedChanged);
        //
        // gbPPSDRV
        //
        //resources.ApplyResources(this.gbPPSDRV, "gbPPSDRV");
        this.gbPPSDRV.add(this.groupBox33);
        this.gbPPSDRV.setName("gbPPSDRV");
        // this.gbPPSDRV.TabStop = false;
        //
        // groupBox33
        //
        //resources.ApplyResources(this.groupBox33, "groupBox33");
        this.groupBox33.add(this.rbPMDUsePPSDRVManualFreq);
        this.groupBox33.add(this.label56);
        this.groupBox33.add(this.rbPMDUsePPSDRVFreqDefault);
        this.groupBox33.add(this.btnPMDPPSDRVManualWait);
        this.groupBox33.add(this.label57);
        this.groupBox33.add(this.tbPMDPPSDRVFreq);
        this.groupBox33.add(this.label58);
        this.groupBox33.add(this.tbPMDPPSDRVManualWait);
        this.groupBox33.setName("groupBox33");
        // this.groupBox33.TabStop = false;
        //
        // rbPMDUsePPSDRVManualFreq
        //
        //resources.ApplyResources(this.rbPMDUsePPSDRVManualFreq, "rbPMDUsePPSDRVManualFreq");
        this.rbPMDUsePPSDRVManualFreq.setName("rbPMDUsePPSDRVManualFreq");
        // this.rbPMDUsePPSDRVManualFreq.TabStop = true;
        // this.rbPMDUsePPSDRVManualFreq.UseVisualStyl.setBackground(true);
        this.rbPMDUsePPSDRVManualFreq.addChangeListener(this::rbPMDUsePPSDRVManualFreq_CheckedChanged);
        //
        // label56
        //
        //resources.ApplyResources(this.label56, "label56");
        this.label56.setName("label56");
        //
        // rbPMDUsePPSDRVFreqDefault
        //
        //resources.ApplyResources(this.rbPMDUsePPSDRVFreqDefault, "rbPMDUsePPSDRVFreqDefault");
        this.rbPMDUsePPSDRVFreqDefault.setName("rbPMDUsePPSDRVFreqDefault");
        // this.rbPMDUsePPSDRVFreqDefault.TabStop = true;
        // this.rbPMDUsePPSDRVFreqDefault.UseVisualStyl.setBackground(true);
        //
        // btnPMDPPSDRVManualWait
        //
        //resources.ApplyResources(this.btnPMDPPSDRVManualWait, "btnPMDPPSDRVManualWait");
        this.btnPMDPPSDRVManualWait.setName("btnPMDPPSDRVManualWait");
        // this.btnPMDPPSDRVManualWait.UseVisualStyl.setBackground(true);
        this.btnPMDPPSDRVManualWait.addActionListener(this::btnPMDPPSDRVManualWait_Click);
        //
        // label57
        //
        //resources.ApplyResources(this.label57, "label57");
        this.label57.setName("label57");
        //
        // tbPMDPPSDRVFreq
        //
        //resources.ApplyResources(this.tbPMDPPSDRVFreq, "tbPMDPPSDRVFreq");
        this.tbPMDPPSDRVFreq.setName("tbPMDPPSDRVFreq");
        this.tbPMDPPSDRVFreq.addFocusListener(this.tbPMDPPSDRVFreq_Click);
        this.tbPMDPPSDRVFreq.addMouseListener(this.tbPMDPPSDRVFreq_MouseClick);
        //
        // label58
        //
        //resources.ApplyResources(this.label58, "label58");
        this.label58.setName("label58");
        //
        // tbPMDPPSDRVManualWait
        //
        //resources.ApplyResources(this.tbPMDPPSDRVManualWait, "tbPMDPPSDRVManualWait");
        this.tbPMDPPSDRVManualWait.setName("tbPMDPPSDRVManualWait");
        //
        // gbPMDSetManualVolume
        //
        //resources.ApplyResources(this.gbPMDSetManualVolume, "gbPMDSetManualVolume");
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
        // this.gbPMDSetManualVolume.TabStop = false;
        //
        // label59
        //
        //resources.ApplyResources(this.label59, "label59");
        this.label59.setName("label59");
        //
        // label60
        //
        //resources.ApplyResources(this.label60, "label60");
        this.label60.setName("label60");
        //
        // tbPMDVolumeAdpcm
        //
        //resources.ApplyResources(this.tbPMDVolumeAdpcm, "tbPMDVolumeAdpcm");
        this.tbPMDVolumeAdpcm.setName("tbPMDVolumeAdpcm");
        //
        // label61
        //
        //resources.ApplyResources(this.label61, "label61");
        this.label61.setName("label61");
        //
        // tbPMDVolumeRhythm
        //
        //resources.ApplyResources(this.tbPMDVolumeRhythm, "tbPMDVolumeRhythm");
        this.tbPMDVolumeRhythm.setName("tbPMDVolumeRhythm");
        //
        // label62
        //
        //resources.ApplyResources(this.label62, "label62");
        this.label62.setName("label62");
        //
        // tbPMDVolumeSSG
        //
        //resources.ApplyResources(this.tbPMDVolumeSSG, "tbPMDVolumeSSG");
        this.tbPMDVolumeSSG.setName("tbPMDVolumeSSG");
        //
        // label63
        //
        //resources.ApplyResources(this.label63, "label63");
        this.label63.setName("label63");
        //
        // tbPMDVolumeGIMICSSG
        //
        //resources.ApplyResources(this.tbPMDVolumeGIMICSSG, "tbPMDVolumeGIMICSSG");
        this.tbPMDVolumeGIMICSSG.setName("tbPMDVolumeGIMICSSG");
        //
        // label64
        //
        //resources.ApplyResources(this.label64, "label64");
        this.label64.setName("label64");
        //
        // tbPMDVolumeFM
        //
        //resources.ApplyResources(this.tbPMDVolumeFM, "tbPMDVolumeFM");
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
        //resources.ApplyResources(this.tpMIDIOut, "tpMIDIOut");
        this.tpMIDIOut.setName("tpMIDIOut");
        // this.tpMIDIOut.UseVisualStyl.setBackground(true);
        //
        // btnAddVST
        //
        //resources.ApplyResources(this.btnAddVST, "btnAddVST");
        this.btnAddVST.setName("btnAddVST");
        // this.btnAddVST.UseVisualStyl.setBackground(true);
        this.btnAddVST.addActionListener(this::btnAddVST_Click);
        //
        // tbcMIDIoutList
        //
        //resources.ApplyResources(this.tbcMIDIoutList, "tbcMIDIoutList");
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
        //resources.ApplyResources(this.tabPage1, "tabPage1");
        this.tabPage1.setName("tabPage1");
//        this.tabPage1.setActionCommand("0");
        // this.tabPage1.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListA
        //
//        this.dgvMIDIoutListA.AllowUserToAddRows = false;
//        this.dgvMIDIoutListA.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListA.AllowUserToResizeRows = false;
        //resources.ApplyResources(this.dgvMIDIoutListA, "dgvMIDIoutListA");
//        this.dgvMIDIoutListA.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
//        this.dgvMIDIoutListA.Columns.AddRange(new JListColumn[] {
//                this.JListTextBoxColumn1,
//                this.clmIsVST,
//                this.clmFileName,
//                this.JListTextBoxColumn2,
//                this.clmType,
//                this.ClmBeforeSend,
//                this.JListTextBoxColumn3,
//                this.JListTextBoxColumn4});
        this.dgvMIDIoutListA.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListA.setName("dgvMIDIoutListA");
//        this.dgvMIDIoutListA.RowHeadersVisible = false;
//        this.dgvMIDIoutListA.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListA.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // JListTextBoxColumn1
        //
        this.JListTextBoxColumn1.setEditable(false);
        //resources.ApplyResources(this.JListTextBoxColumn1, "JListTextBoxColumn1");
        this.JListTextBoxColumn1.setName("JListTextBoxColumn1");
//        this.JListTextBoxColumn1.setEditable(true);
//        this.JListTextBoxColumn1.SortMode = JListColumnSortMode.NotSortable;
        //
        // clmIsVST
        //
        //resources.ApplyResources(this.clmIsVST, "clmIsVST");
        this.clmIsVST.setName("clmIsVST");
        //
        // clmFileName
        //
        //resources.ApplyResources(this.clmFileName, "clmFileName");
        this.clmFileName.setName("clmFileName");
//        this.clmFileName.Resizable = JListTriState.True;
//        this.clmFileName.SortMode = JListColumnSortMode.NotSortable;
        //
        // JListTextBoxColumn2
        //
        //resources.ApplyResources(this.JListTextBoxColumn2, "JListTextBoxColumn2");
        this.JListTextBoxColumn2.setName("JListTextBoxColumn2");
        this.JListTextBoxColumn2.setEditable(true);
//        this.JListTextBoxColumn2.SortMode = JListColumnSortMode.NotSortable;
        //
        // clmType
        //
        //resources.ApplyResources(this.clmType, "clmType");
        this.clmType.setModel(new DefaultComboBoxModel<>(new String[] {
                "GM",
                "XG",
                "GS",
                "LA",
                "GS(SC-55_1)",
                "GS(SC-55_2)"}));
        this.clmType.setName("clmType");
//        this.clmType.Resizable = ListTriState.True;
        //
        // ClmBeforeSend
        //
        //resources.ApplyResources(this.ClmBeforeSend, "ClmBeforeSend");
        this.ClmBeforeSend.setModel(new DefaultComboBoxModel<>(new String[] {
                "None",
                "GM Reset",
                "XG Reset",
                "GS Reset",
                "Custom"}));
        this.ClmBeforeSend.setName("ClmBeforeSend");
//        this.ClmBeforeSend.Resizable = JListTriState.True;
//        this.ClmBeforeSend.SortMode = JListColumnSortMode.Automatic;
        //
        // JListTextBoxColumn3
        //
        //resources.ApplyResources(this.JListTextBoxColumn3, "JListTextBoxColumn3");
        this.JListTextBoxColumn3.setName("JListTextBoxColumn3");
        this.JListTextBoxColumn3.setEditable(true);
//        this.JListTextBoxColumn3.SortMode = JListColumnSortMode.NotSortable;
        //
        // JListTextBoxColumn4
        //
//        this.JListTextBoxColumn4.AutoSizeMode = JListAutoSizeColumnMode.Fill;
        //resources.ApplyResources(this.JListTextBoxColumn4, "JListTextBoxColumn4");
        this.JListTextBoxColumn4.setName("JListTextBoxColumn4");
        this.JListTextBoxColumn4.setEditable(true);
//        this.JListTextBoxColumn4.SortMode = JListColumnSortMode.NotSortable;
        //
        // btnUP_A
        //
        //resources.ApplyResources(this.btnUP_A, "btnUP_A");
        this.btnUP_A.setName("btnUP_A");
        // this.btnUP_A.UseVisualStyl.setBackground(true);
        this.btnUP_A.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_A
        //
        //resources.ApplyResources(this.btnDOWN_A, "btnDOWN_A");
        this.btnDOWN_A.setName("btnDOWN_A");
        // this.btnDOWN_A.UseVisualStyl.setBackground(true);
        this.btnDOWN_A.addActionListener(this::btnDOWN_Click);
        //
        // tabPage2
        //
        this.tabPage2.add(this.dgvMIDIoutListB);
        this.tabPage2.add(this.btnUP_B);
        this.tabPage2.add(this.btnDOWN_B);
        //resources.ApplyResources(this.tabPage2, "tabPage2");
        this.tabPage2.setName("tabPage2");
//        this.tabPage2.setActionCommand("1");
        // this.tabPage2.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListB
        //
//        this.dgvMIDIoutListB.AllowUserToAddRows = false;
//        this.dgvMIDIoutListB.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListB.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListB.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListB, "dgvMIDIoutListB");
        this.dgvMIDIoutListB.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListB.setName("dgvMIDIoutListB");
//        this.dgvMIDIoutListB.RowHeadersVisible = false;
//        this.dgvMIDIoutListB.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListB.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_B
        //
        //resources.ApplyResources(this.btnUP_B, "btnUP_B");
        this.btnUP_B.setName("btnUP_B");
        // this.btnUP_B.UseVisualStyl.setBackground(true);
        this.btnUP_B.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_B
        //
        //resources.ApplyResources(this.btnDOWN_B, "btnDOWN_B");
        this.btnDOWN_B.setName("btnDOWN_B");
        // this.btnDOWN_B.UseVisualStyl.setBackground(true);
        this.btnDOWN_B.addActionListener(this::btnDOWN_Click);
        //
        // tabPage3
        //
        this.tabPage3.add(this.dgvMIDIoutListC);
        this.tabPage3.add(this.btnUP_C);
        this.tabPage3.add(this.btnDOWN_C);
        //resources.ApplyResources(this.tabPage3, "tabPage3");
        this.tabPage3.setName("tabPage3");
//        this.tabPage3.setActionCommand("2");
        // this.tabPage3.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListC
        //
//        this.dgvMIDIoutListC.AllowUserToAddRows = false;
//        this.dgvMIDIoutListC.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListC.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListC.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListC, "dgvMIDIoutListC");
        this.dgvMIDIoutListC.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListC.setName("dgvMIDIoutListC");
//        this.dgvMIDIoutListC.RowHeadersVisible = false;
//        this.dgvMIDIoutListC.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListC.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_C
        //
        //resources.ApplyResources(this.btnUP_C, "btnUP_C");
        this.btnUP_C.setName("btnUP_C");
        // this.btnUP_C.UseVisualStyl.setBackground(true);
        this.btnUP_C.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_C
        //
        //resources.ApplyResources(this.btnDOWN_C, "btnDOWN_C");
        this.btnDOWN_C.setName("btnDOWN_C");
        // this.btnDOWN_C.UseVisualStyl.setBackground(true);
        this.btnDOWN_C.addActionListener(this::btnDOWN_Click);
        //
        // tabPage4
        //
        this.tabPage4.add(this.dgvMIDIoutListD);
        this.tabPage4.add(this.btnUP_D);
        this.tabPage4.add(this.btnDOWN_D);
        //resources.ApplyResources(this.tabPage4, "tabPage4");
        this.tabPage4.setName("tabPage4");
//        this.tabPage4.setActionCommand("3");
        // this.tabPage4.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListD
        //
//        this.dgvMIDIoutListD.AllowUserToAddRows = false;
//        this.dgvMIDIoutListD.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListD.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListD.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListD, "dgvMIDIoutListD");
        this.dgvMIDIoutListD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListD.setName("dgvMIDIoutListD");
//        this.dgvMIDIoutListD.RowHeadersVisible = false;
//        this.dgvMIDIoutListD.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListD.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_D
        //
        //resources.ApplyResources(this.btnUP_D, "btnUP_D");
        this.btnUP_D.setName("btnUP_D");
        // this.btnUP_D.UseVisualStyl.setBackground(true);
        this.btnUP_D.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_D
        //
        //resources.ApplyResources(this.btnDOWN_D, "btnDOWN_D");
        this.btnDOWN_D.setName("btnDOWN_D");
        // this.btnDOWN_D.UseVisualStyl.setBackground(true);
        this.btnDOWN_D.addActionListener(this::btnDOWN_Click);
        //
        // tabPage5
        //
        this.tabPage5.add(this.dgvMIDIoutListE);
        this.tabPage5.add(this.btnUP_E);
        this.tabPage5.add(this.btnDOWN_E);
        //resources.ApplyResources(this.tabPage5, "tabPage5");
        this.tabPage5.setName("tabPage5");
//        this.tabPage5.setActionCommand("4");
        // this.tabPage5.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListE
        //
//        this.dgvMIDIoutListE.AllowUserToAddRows = false;
//        this.dgvMIDIoutListE.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListE.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListE.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListE, "dgvMIDIoutListE");
        this.dgvMIDIoutListE.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListE.setName("dgvMIDIoutListE");
//        this.dgvMIDIoutListE.RowHeadersVisible = false;
//        this.dgvMIDIoutListE.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListE.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_E
        //
        //resources.ApplyResources(this.btnUP_E, "btnUP_E");
        this.btnUP_E.setName("btnUP_E");
        // this.btnUP_E.UseVisualStyl.setBackground(true);
        this.btnUP_E.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_E
        //
        //resources.ApplyResources(this.btnDOWN_E, "btnDOWN_E");
        this.btnDOWN_E.setName("btnDOWN_E");
        // this.btnDOWN_E.UseVisualStyl.setBackground(true);
        this.btnDOWN_E.addActionListener(this::btnDOWN_Click);
        //
        // tabPage6
        //
        this.tabPage6.add(this.dgvMIDIoutListF);
        this.tabPage6.add(this.btnUP_F);
        this.tabPage6.add(this.btnDOWN_F);
        //resources.ApplyResources(this.tabPage6, "tabPage6");
        this.tabPage6.setName("tabPage6");
//        this.tabPage6.setActionCommand("5");
        // this.tabPage6.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListF
        //
//        this.dgvMIDIoutListF.AllowUserToAddRows = false;
//        this.dgvMIDIoutListF.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListF.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListF.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListF, "dgvMIDIoutListF");
        this.dgvMIDIoutListF.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListF.setName("dgvMIDIoutListF");
//        this.dgvMIDIoutListF.RowHeadersVisible = false;
//        this.dgvMIDIoutListF.RowTemplate.setHeight(21);
//        this.dgvMIDIoutListF.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_F
        //
        //resources.ApplyResources(this.btnUP_F, "btnUP_F");
        this.btnUP_F.setName("btnUP_F");
        // this.btnUP_F.UseVisualStyl.setBackground(true);
        this.btnUP_F.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_F
        //
        //resources.ApplyResources(this.btnDOWN_F, "btnDOWN_F");
        this.btnDOWN_F.setName("btnDOWN_F");
        // this.btnDOWN_F.UseVisualStyl.setBackground(true);
        this.btnDOWN_F.addActionListener(this::btnDOWN_Click);
        //
        // tabPage7
        //
        this.tabPage7.add(this.dgvMIDIoutListG);
        this.tabPage7.add(this.btnUP_G);
        this.tabPage7.add(this.btnDOWN_G);
        //resources.ApplyResources(this.tabPage7, "tabPage7");
        this.tabPage7.setName("tabPage7");
//        this.tabPage7.setActionCommand("6");
        // this.tabPage7.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListG
        //
//        this.dgvMIDIoutListG.AllowUserToAddRows = false;
//        this.dgvMIDIoutListG.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListG.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListG.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListG, "dgvMIDIoutListG");
        this.dgvMIDIoutListG.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListG.setName("dgvMIDIoutListG");
//        this.dgvMIDIoutListG.RowHeadersVisible = false;
//        this.dgvMIDIoutListG.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListG.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_G
        //
        //resources.ApplyResources(this.btnUP_G, "btnUP_G");
        this.btnUP_G.setName("btnUP_G");
        // this.btnUP_G.UseVisualStyl.setBackground(true);
        this.btnUP_G.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_G
        //
        //resources.ApplyResources(this.btnDOWN_G, "btnDOWN_G");
        this.btnDOWN_G.setName("btnDOWN_G");
        // this.btnDOWN_G.UseVisualStyl.setBackground(true);
        this.btnDOWN_G.addActionListener(this::btnDOWN_Click);
        //
        // tabPage8
        //
        this.tabPage8.add(this.dgvMIDIoutListH);
        this.tabPage8.add(this.btnUP_H);
        this.tabPage8.add(this.btnDOWN_H);
        //resources.ApplyResources(this.tabPage8, "tabPage8");
        this.tabPage8.setName("tabPage8");
//        this.tabPage8.setActionCommand("7");
        // this.tabPage8.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListH
        //
//        this.dgvMIDIoutListH.AllowUserToAddRows = false;
//        this.dgvMIDIoutListH.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListH.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListH.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListH, "dgvMIDIoutListH");
        this.dgvMIDIoutListH.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListH.setName("dgvMIDIoutListH");
//        this.dgvMIDIoutListH.RowHeadersVisible = false;
//        this.dgvMIDIoutListH.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListH.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_H
        //
        //resources.ApplyResources(this.btnUP_H, "btnUP_H");
        this.btnUP_H.setName("btnUP_H");
        // this.btnUP_H.UseVisualStyl.setBackground(true);
        this.btnUP_H.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_H
        //
        //resources.ApplyResources(this.btnDOWN_H, "btnDOWN_H");
        this.btnDOWN_H.setName("btnDOWN_H");
        // this.btnDOWN_H.UseVisualStyl.setBackground(true);
        this.btnDOWN_H.addActionListener(this::btnDOWN_Click);
        //
        // tabPage9
        //
        this.tabPage9.add(this.dgvMIDIoutListI);
        this.tabPage9.add(this.btnUP_I);
        this.tabPage9.add(this.btnDOWN_I);
        //resources.ApplyResources(this.tabPage9, "tabPage9");
        this.tabPage9.setName("tabPage9");
//        this.tabPage9.setActionCommand("8");
        // this.tabPage9.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListI
        //
//        this.dgvMIDIoutListI.AllowUserToAddRows = false;
//        this.dgvMIDIoutListI.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListI.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListI.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListI, "dgvMIDIoutListI");
        this.dgvMIDIoutListI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListI.setName("dgvMIDIoutListI");
//        this.dgvMIDIoutListI.RowHeadersVisible = false;
//        this.dgvMIDIoutListI.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListI.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // btnUP_I
        //
        //resources.ApplyResources(this.btnUP_I, "btnUP_I");
        this.btnUP_I.setName("btnUP_I");
        // this.btnUP_I.UseVisualStyl.setBackground(true);
        this.btnUP_I.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_I
        //
        //resources.ApplyResources(this.btnDOWN_I, "btnDOWN_I");
        this.btnDOWN_I.setName("btnDOWN_I");
        // this.btnDOWN_I.UseVisualStyl.setBackground(true);
        this.btnDOWN_I.addActionListener(this::btnDOWN_Click);
        //
        // tabPage10
        //
        this.tabPage10.add(this.dgvMIDIoutListJ);
        this.tabPage10.add(this.button17);
        this.tabPage10.add(this.btnDOWN_J);
        //resources.ApplyResources(this.tabPage10, "tabPage10");
        this.tabPage10.setName("tabPage10");
//        this.tabPage10.setActionCommand("9");
        // this.tabPage10.UseVisualStyl.setBackground(true);
        //
        // dgvMIDIoutListJ
        //
//        this.dgvMIDIoutListJ.AllowUserToAddRows = false;
//        this.dgvMIDIoutListJ.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutListJ.AllowUserToResizeRows = false;
//        this.dgvMIDIoutListJ.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
        //resources.ApplyResources(this.dgvMIDIoutListJ, "dgvMIDIoutListJ");
        this.dgvMIDIoutListJ.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutListJ.setName("dgvMIDIoutListJ");
//        this.dgvMIDIoutListJ.RowHeadersVisible = false;
//        this.dgvMIDIoutListJ.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutListJ.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // button17
        //
        //resources.ApplyResources(this.button17, "button17");
        this.button17.setName("button17");
        // this.button17.UseVisualStyl.setBackground(true);
        this.button17.addActionListener(this::btnUP_Click);
        //
        // btnDOWN_J
        //
        //resources.ApplyResources(this.btnDOWN_J, "btnDOWN_J");
        this.btnDOWN_J.setName("btnDOWN_J");
        // this.btnDOWN_J.UseVisualStyl.setBackground(true);
        this.btnDOWN_J.addActionListener(this::btnDOWN_Click);
        //
        // btnSubMIDIout
        //
        //resources.ApplyResources(this.btnSubMIDIout, "btnSubMIDIout");
        this.btnSubMIDIout.setName("btnSubMIDIout");
        // this.btnSubMIDIout.UseVisualStyl.setBackground(true);
        this.btnSubMIDIout.addActionListener(this::btnSubMIDIout_Click);
        //
        // btnAddMIDIout
        //
        //resources.ApplyResources(this.btnAddMIDIout, "btnAddMIDIout");
        this.btnAddMIDIout.setName("btnAddMIDIout");
        // this.btnAddMIDIout.UseVisualStyl.setBackground(true);
        this.btnAddMIDIout.addActionListener(this::btnAddMIDIout_Click);
        //
        // label18
        //
        //resources.ApplyResources(this.label18, "label18");
        this.label18.setName("label18");
        //
        // dgvMIDIoutPallet
        //
//        this.dgvMIDIoutPallet.AllowUserToAddRows = false;
//        this.dgvMIDIoutPallet.AllowUserToDeleteRows = false;
//        this.dgvMIDIoutPallet.AllowUserToResizeRows = false;
        //resources.ApplyResources(this.dgvMIDIoutPallet, "dgvMIDIoutPallet");
//        this.dgvMIDIoutPallet.ColumnHeadersHeightSizeMode = JListColumnHeadersHeightSizeMode.AutoSize;
//        this.dgvMIDIoutPallet.Columns.AddRange(new JListColumn[] {
//                this.clmID,
//                this.clmDeviceName,
//                this.clmManufacturer,
//                this.clmSpacer});
        this.dgvMIDIoutPallet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dgvMIDIoutPallet.setName("dgvMIDIoutPallet");
//        this.dgvMIDIoutPallet.RowHeadersVisible = false;
//        this.dgvMIDIoutPallet.RowTemplate.getHeight() = 21;
//        this.dgvMIDIoutPallet.SelectionMode = JListSelectionMode.FullRowSelect;
        //
        // clmID
        //
//        this.clmID.Frozen = true;
        //resources.ApplyResources(this.clmID, "clmID");
        this.clmID.setName("clmID");
        this.clmID.setEditable(true);
        //
        // clmDeviceName
        //
//        this.clmDeviceName.Frozen = true;
        //resources.ApplyResources(this.clmDeviceName, "clmDeviceName");
        this.clmDeviceName.setName("clmDeviceName");
        this.clmDeviceName.setEditable(true);
//        this.clmDeviceName.SortMode = JListColumnSortMode.NotSortable;
        //
        // clmManufacturer
        //
//        this.clmManufacturer.Frozen = true;
        //resources.ApplyResources(this.clmManufacturer, "clmManufacturer");
        this.clmManufacturer.setName("clmManufacturer");
        this.clmManufacturer.setEditable(true);
//        this.clmManufacturer.SortMode = JListColumnSortMode.NotSortable;
        //
        // clmSpacer
        //
//        this.clmSpacer.AutoSizeMode = JListAutoSizeColumnMode.Fill;
        //resources.ApplyResources(this.clmSpacer, "clmSpacer");
        this.clmSpacer.setName("clmSpacer");
        this.clmSpacer.setEditable(true);
//        this.clmSpacer.SortMode = JListColumnSortMode.NotSortable;
        //
        // label16
        //
        //resources.ApplyResources(this.label16, "label16");
        this.label16.setName("label16");
        //
        // tpMIDIOut2
        //
        this.tpMIDIOut2.add(this.groupBox15);
        //resources.ApplyResources(this.tpMIDIOut2, "tpMIDIOut2");
        this.tpMIDIOut2.setName("tpMIDIOut2");
        // this.tpMIDIOut2.UseVisualStyl.setBackground(true);
        //
        // groupBox15
        //
        //resources.ApplyResources(this.groupBox15, "groupBox15");
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
        // this.groupBox15.TabStop = false;
        //
        // btnBeforeSend_Default
        //
        //resources.ApplyResources(this.btnBeforeSend_Default, "btnBeforeSend_Default");
        this.btnBeforeSend_Default.setName("btnBeforeSend_Default");
        // this.btnBeforeSend_Default.UseVisualStyl.setBackground(true);
        this.btnBeforeSend_Default.addActionListener(this::btnBeforeSend_Default_Click);
        //
        // tbBeforeSend_Custom
        //
        //resources.ApplyResources(this.tbBeforeSend_Custom, "tbBeforeSend_Custom");
        this.tbBeforeSend_Custom.setName("tbBeforeSend_Custom");
        //
        // tbBeforeSend_XGReset
        //
        //resources.ApplyResources(this.tbBeforeSend_XGReset, "tbBeforeSend_XGReset");
        this.tbBeforeSend_XGReset.setName("tbBeforeSend_XGReset");
        //
        // label35
        //
        //resources.ApplyResources(this.label35, "label35");
        this.label35.setName("label35");
        //
        // label34
        //
        //resources.ApplyResources(this.label34, "label34");
        this.label34.setName("label34");
        //
        // label32
        //
        //resources.ApplyResources(this.label32, "label32");
        this.label32.setName("label32");
        //
        // tbBeforeSend_GSReset
        //
        //resources.ApplyResources(this.tbBeforeSend_GSReset, "tbBeforeSend_GSReset");
        this.tbBeforeSend_GSReset.setName("tbBeforeSend_GSReset");
        //
        // label33
        //
        //resources.ApplyResources(this.label33, "label33");
        this.label33.setName("label33");
        //
        // tbBeforeSend_GMReset
        //
        //resources.ApplyResources(this.tbBeforeSend_GMReset, "tbBeforeSend_GMReset");
        this.tbBeforeSend_GMReset.setName("tbBeforeSend_GMReset");
        //
        // label31
        //
        //resources.ApplyResources(this.label31, "label31");
        this.label31.setName("label31");
        //
        // tabMIDIExp
        //
        this.tabMIDIExp.add(this.cbUseMIDIExport);
        this.tabMIDIExp.add(this.gbMIDIExport);
        //resources.ApplyResources(this.tabMIDIExp, "tabMIDIExp");
        this.tabMIDIExp.setName("tabMIDIExp");
        // this.tabMIDIExp.UseVisualStyl.setBackground(true);
        //
        // cbUseMIDIExport
        //
        //resources.ApplyResources(this.cbUseMIDIExport, "cbUseMIDIExport");
        this.cbUseMIDIExport.setName("cbUseMIDIExport");
        // this.cbUseMIDIExport.UseVisualStyl.setBackground(true);
        this.cbUseMIDIExport.addChangeListener(this::cbUseMIDIExport_CheckedChanged);
        //
        // gbMIDIExport
        //
        //resources.ApplyResources(this.gbMIDIExport, "gbMIDIExport");
        this.gbMIDIExport.add(this.cbMIDIKeyOnFnum);
        this.gbMIDIExport.add(this.cbMIDIUseVOPM);
        this.gbMIDIExport.add(this.groupBox6);
        this.gbMIDIExport.add(this.cbMIDIPlayless);
        this.gbMIDIExport.add(this.btnMIDIOutputPath);
        this.gbMIDIExport.add(this.lblOutputPath);
        this.gbMIDIExport.add(this.tbMIDIOutputPath);
        this.gbMIDIExport.setName("gbMIDIExport");
        // this.gbMIDIExport.TabStop = false;
        //
        // cbMIDIKeyOnFnum
        //
        //resources.ApplyResources(this.cbMIDIKeyOnFnum, "cbMIDIKeyOnFnum");
        this.cbMIDIKeyOnFnum.setName("cbMIDIKeyOnFnum");
        // this.cbMIDIKeyOnFnum.UseVisualStyl.setBackground(true);
        //
        // cbMIDIUseVOPM
        //
        //resources.ApplyResources(this.cbMIDIUseVOPM, "cbMIDIUseVOPM");
        this.cbMIDIUseVOPM.setName("cbMIDIUseVOPM");
        // this.cbMIDIUseVOPM.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.groupBox6, "groupBox6");
        this.groupBox6.setName("groupBox6");
        // this.groupBox6.TabStop = false;
        //
        // cbMIDIYM2612
        //
        //resources.ApplyResources(this.cbMIDIYM2612, "cbMIDIYM2612");
        this.cbMIDIYM2612.setSelected(true);
//        this.cbMIDIYM2612.CheckState = JCheckState.isSelected();
        this.cbMIDIYM2612.setName("cbMIDIYM2612");
        // this.cbMIDIYM2612.UseVisualStyl.setBackground(true);
        //
        // cbMIDISN76489Sec
        //
        //resources.ApplyResources(this.cbMIDISN76489Sec, "cbMIDISN76489Sec");
        this.cbMIDISN76489Sec.setName("cbMIDISN76489Sec");
        // this.cbMIDISN76489Sec.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2612Sec
        //
        //resources.ApplyResources(this.cbMIDIYM2612Sec, "cbMIDIYM2612Sec");
        this.cbMIDIYM2612Sec.setName("cbMIDIYM2612Sec");
        // this.cbMIDIYM2612Sec.UseVisualStyl.setBackground(true);
        //
        // cbMIDISN76489
        //
        //resources.ApplyResources(this.cbMIDISN76489, "cbMIDISN76489");
        this.cbMIDISN76489.setName("cbMIDISN76489");
        // this.cbMIDISN76489.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2151
        //
        //resources.ApplyResources(this.cbMIDIYM2151, "cbMIDIYM2151");
        this.cbMIDIYM2151.setName("cbMIDIYM2151");
        // this.cbMIDIYM2151.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2610BSec
        //
        //resources.ApplyResources(this.cbMIDIYM2610BSec, "cbMIDIYM2610BSec");
        this.cbMIDIYM2610BSec.setName("cbMIDIYM2610BSec");
        // this.cbMIDIYM2610BSec.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2151Sec
        //
        //resources.ApplyResources(this.cbMIDIYM2151Sec, "cbMIDIYM2151Sec");
        this.cbMIDIYM2151Sec.setName("cbMIDIYM2151Sec");
        // this.cbMIDIYM2151Sec.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2610B
        //
        //resources.ApplyResources(this.cbMIDIYM2610B, "cbMIDIYM2610B");
        this.cbMIDIYM2610B.setName("cbMIDIYM2610B");
        // this.cbMIDIYM2610B.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2203
        //
        //resources.ApplyResources(this.cbMIDIYM2203, "cbMIDIYM2203");
        this.cbMIDIYM2203.setName("cbMIDIYM2203");
        // this.cbMIDIYM2203.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2608Sec
        //
        //resources.ApplyResources(this.cbMIDIYM2608Sec, "cbMIDIYM2608Sec");
        this.cbMIDIYM2608Sec.setName("cbMIDIYM2608Sec");
        // this.cbMIDIYM2608Sec.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2203Sec
        //
        //resources.ApplyResources(this.cbMIDIYM2203Sec, "cbMIDIYM2203Sec");
        this.cbMIDIYM2203Sec.setName("cbMIDIYM2203Sec");
        // this.cbMIDIYM2203Sec.UseVisualStyl.setBackground(true);
        //
        // cbMIDIYM2608
        //
        //resources.ApplyResources(this.cbMIDIYM2608, "cbMIDIYM2608");
        this.cbMIDIYM2608.setName("cbMIDIYM2608");
        // this.cbMIDIYM2608.UseVisualStyl.setBackground(true);
        //
        // cbMIDIPlayless
        //
        //resources.ApplyResources(this.cbMIDIPlayless, "cbMIDIPlayless");
        this.cbMIDIPlayless.setName("cbMIDIPlayless");
        // this.cbMIDIPlayless.UseVisualStyl.setBackground(true);
        //
        // btnMIDIOutputPath
        //
        //resources.ApplyResources(this.btnMIDIOutputPath, "btnMIDIOutputPath");
        this.btnMIDIOutputPath.setName("btnMIDIOutputPath");
        // this.btnMIDIOutputPath.UseVisualStyl.setBackground(true);
        this.btnMIDIOutputPath.addActionListener(this::btnMIDIOutputPath_Click);
        //
        // lblOutputPath
        //
        //resources.ApplyResources(this.lblOutputPath, "lblOutputPath");
        this.lblOutputPath.setName("lblOutputPath");
        //
        // tbMIDIOutputPath
        //
        //resources.ApplyResources(this.tbMIDIOutputPath, "tbMIDIOutputPath");
        this.tbMIDIOutputPath.setName("tbMIDIOutputPath");
        //
        // tpMIDIKBD
        //
        this.tpMIDIKBD.add(this.cbUseMIDIKeyboard);
        this.tpMIDIKBD.add(this.gbMIDIKeyboard);
        //resources.ApplyResources(this.tpMIDIKBD, "tpMIDIKBD");
        this.tpMIDIKBD.setName("tpMIDIKBD");
        // this.tpMIDIKBD.UseVisualStyl.setBackground(true);
        //
        // cbUseMIDIKeyboard
        //
        //resources.ApplyResources(this.cbUseMIDIKeyboard, "cbUseMIDIKeyboard");
        this.cbUseMIDIKeyboard.setName("cbUseMIDIKeyboard");
        // this.cbUseMIDIKeyboard.UseVisualStyl.setBackground(true);
        this.cbUseMIDIKeyboard.addChangeListener(this::cbUseMIDIKeyboard_CheckedChanged);
        //
        // gbMIDIKeyboard
        //
        //resources.ApplyResources(this.gbMIDIKeyboard, "gbMIDIKeyboard");
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
        // this.gbMIDIKeyboard.TabStop = false;
        //
        // pictureBox8
        //
        this.pictureBox8.setIcon(new ImageIcon(mdplayer.properties.Resources.getccNext()));
        //resources.ApplyResources(this.pictureBox8, "pictureBox8");
        this.pictureBox8.setName("pictureBox8");
        // this.pictureBox8.TabStop = false;
        //
        // pictureBox7
        //
        this.pictureBox7.setIcon(new ImageIcon(mdplayer.properties.Resources.getccFast()));
        //resources.ApplyResources(this.pictureBox7, "pictureBox7");
        this.pictureBox7.setName("pictureBox7");
        // this.pictureBox7.TabStop = false;
        //
        // pictureBox6
        //
        this.pictureBox6.setIcon(new ImageIcon(mdplayer.properties.Resources.getccPlay()));
        //resources.ApplyResources(this.pictureBox6, "pictureBox6");
        this.pictureBox6.setName("pictureBox6");
        // this.pictureBox6.TabStop = false;
        //
        // pictureBox5
        //
        this.pictureBox5.setIcon(new ImageIcon(mdplayer.properties.Resources.getccSlow()));
        //resources.ApplyResources(this.pictureBox5, "pictureBox5");
        this.pictureBox5.setName("pictureBox5");
        // this.pictureBox5.TabStop = false;
        //
        // pictureBox4
        //
        this.pictureBox4.setIcon(new ImageIcon(mdplayer.properties.Resources.getccStop()));
        //resources.ApplyResources(this.pictureBox4, "pictureBox4");
        this.pictureBox4.setName("pictureBox4");
        // this.pictureBox4.TabStop = false;
        //
        // pictureBox3
        //
        this.pictureBox3.setIcon(new ImageIcon(mdplayer.properties.Resources.getccPause()));
        //resources.ApplyResources(this.pictureBox3, "pictureBox3");
        this.pictureBox3.setName("pictureBox3");
        // this.pictureBox3.TabStop = false;
        //
        // pictureBox2
        //
        this.pictureBox2.setIcon(new ImageIcon(mdplayer.properties.Resources.getccPrevious()));
        //resources.ApplyResources(this.pictureBox2, "pictureBox2");
        this.pictureBox2.setName("pictureBox2");
        // this.pictureBox2.TabStop = false;
        //
        // pictureBox1
        //
        this.pictureBox1.setIcon(new ImageIcon(mdplayer.properties.Resources.getccFadeout()));
        //resources.ApplyResources(this.pictureBox1, "pictureBox1");
        this.pictureBox1.setName("pictureBox1");
        // this.pictureBox1.TabStop = false;
        //
        // tbCCFadeout
        //
        //resources.ApplyResources(this.tbCCFadeout, "tbCCFadeout");
        this.tbCCFadeout.setName("tbCCFadeout");
        //
        // tbCCPause
        //
        //resources.ApplyResources(this.tbCCPause, "tbCCPause");
        this.tbCCPause.setName("tbCCPause");
        //
        // tbCCSlow
        //
        //resources.ApplyResources(this.tbCCSlow, "tbCCSlow");
        this.tbCCSlow.setName("tbCCSlow");
        //
        // tbCCPrevious
        //
        //resources.ApplyResources(this.tbCCPrevious, "tbCCPrevious");
        this.tbCCPrevious.setName("tbCCPrevious");
        //
        // tbCCNext
        //
        //resources.ApplyResources(this.tbCCNext, "tbCCNext");
        this.tbCCNext.setName("tbCCNext");
        //
        // tbCCFast
        //
        //resources.ApplyResources(this.tbCCFast, "tbCCFast");
        this.tbCCFast.setName("tbCCFast");
        //
        // tbCCStop
        //
        //resources.ApplyResources(this.tbCCStop, "tbCCStop");
        this.tbCCStop.setName("tbCCStop");
        //
        // tbCCPlay
        //
        //resources.ApplyResources(this.tbCCPlay, "tbCCPlay");
        this.tbCCPlay.setName("tbCCPlay");
        //
        // tbCCCopyLog
        //
        //resources.ApplyResources(this.tbCCCopyLog, "tbCCCopyLog");
        this.tbCCCopyLog.setName("tbCCCopyLog");
        //
        // label17
        //
        //resources.ApplyResources(this.label17, "label17");
        this.label17.setName("label17");
        //
        // tbCCDelLog
        //
        //resources.ApplyResources(this.tbCCDelLog, "tbCCDelLog");
        this.tbCCDelLog.setName("tbCCDelLog");
        //
        // label15
        //
        //resources.ApplyResources(this.label15, "label15");
        this.label15.setName("label15");
        //
        // tbCCChCopy
        //
        //resources.ApplyResources(this.tbCCChCopy, "tbCCChCopy");
        this.tbCCChCopy.setName("tbCCChCopy");
        //
        // label8
        //
        //resources.ApplyResources(this.label8, "label8");
        this.label8.setName("label8");
        //
        // label9
        //
        //resources.ApplyResources(this.label9, "label9");
        this.label9.setName("label9");
        //
        // gbUseChannel
        //
        this.gbUseChannel.add(this.rbMONO);
        this.gbUseChannel.add(this.rbPOLY);
        this.gbUseChannel.add(this.groupBox7);
        this.gbUseChannel.add(this.groupBox2);
        //resources.ApplyResources(this.gbUseChannel, "gbUseChannel");
        this.gbUseChannel.setName("gbUseChannel");
        // this.gbUseChannel.TabStop = false;
        //
        // rbMONO
        //
        //resources.ApplyResources(this.rbMONO, "rbMONO");
        this.rbMONO.setSelected(true);
        this.rbMONO.setName("rbMONO");
        // this.rbMONO.TabStop = true;
        // this.rbMONO.UseVisualStyl.setBackground(true);
        //
        // rbPOLY
        //
        //resources.ApplyResources(this.rbPOLY, "rbPOLY");
        this.rbPOLY.setName("rbPOLY");
        // this.rbPOLY.UseVisualStyl.setBackground(true);
        //
        // groupBox7
        //
        this.groupBox7.add(this.rbFM6);
        this.groupBox7.add(this.rbFM3);
        this.groupBox7.add(this.rbFM5);
        this.groupBox7.add(this.rbFM2);
        this.groupBox7.add(this.rbFM4);
        this.groupBox7.add(this.rbFM1);
        //resources.ApplyResources(this.groupBox7, "groupBox7");
        this.groupBox7.setName("groupBox7");
        // this.groupBox7.TabStop = false;
        //
        // rbFM6
        //
        //resources.ApplyResources(this.rbFM6, "rbFM6");
        this.rbFM6.setName("rbFM6");
        // this.rbFM6.UseVisualStyl.setBackground(true);
        //
        // rbFM3
        //
        //resources.ApplyResources(this.rbFM3, "rbFM3");
        this.rbFM3.setName("rbFM3");
        // this.rbFM3.UseVisualStyl.setBackground(true);
        //
        // rbFM5
        //
        //resources.ApplyResources(this.rbFM5, "rbFM5");
        this.rbFM5.setName("rbFM5");
        // this.rbFM5.UseVisualStyl.setBackground(true);
        //
        // rbFM2
        //
        //resources.ApplyResources(this.rbFM2, "rbFM2");
        this.rbFM2.setName("rbFM2");
        // this.rbFM2.UseVisualStyl.setBackground(true);
        //
        // rbFM4
        //
        //resources.ApplyResources(this.rbFM4, "rbFM4");
        this.rbFM4.setName("rbFM4");
        // this.rbFM4.UseVisualStyl.setBackground(true);
        //
        // rbFM1
        //
        //resources.ApplyResources(this.rbFM1, "rbFM1");
        this.rbFM1.setSelected(true);
        this.rbFM1.setName("rbFM1");
        // this.rbFM1.TabStop = true;
        // this.rbFM1.UseVisualStyl.setBackground(true);
        //
        // groupBox2
        //
        this.groupBox2.add(this.cbFM1);
        this.groupBox2.add(this.cbFM6);
        this.groupBox2.add(this.cbFM2);
        this.groupBox2.add(this.cbFM5);
        this.groupBox2.add(this.cbFM3);
        this.groupBox2.add(this.cbFM4);
        //resources.ApplyResources(this.groupBox2, "groupBox2");
        this.groupBox2.setName("groupBox2");
        // this.groupBox2.TabStop = false;
        //
        // cbFM1
        //
        //resources.ApplyResources(this.cbFM1, "cbFM1");
        this.cbFM1.setSelected(true);
//        this.cbFM1.CheckState = JCheckState.isSelected();
        this.cbFM1.setName("cbFM1");
        // this.cbFM1.UseVisualStyl.setBackground(true);
        //
        // cbFM6
        //
        //resources.ApplyResources(this.cbFM6, "cbFM6");
        this.cbFM6.setSelected(true);
//        this.cbFM6.CheckState = JCheckState.isSelected();
        this.cbFM6.setName("cbFM6");
        // this.cbFM6.UseVisualStyl.setBackground(true);
        //
        // cbFM2
        //
        //resources.ApplyResources(this.cbFM2, "cbFM2");
        this.cbFM2.setSelected(true);
//        this.cbFM2.CheckState = JCheckState.isSelected();
        this.cbFM2.setName("cbFM2");
        // this.cbFM2.UseVisualStyl.setBackground(true);
        //
        // cbFM5
        //
        //resources.ApplyResources(this.cbFM5, "cbFM5");
        this.cbFM5.setSelected(true);
//        this.cbFM5.CheckState = JCheckState.isSelected();
        this.cbFM5.setName("cbFM5");
        // this.cbFM5.UseVisualStyl.setBackground(true);
        //
        // cbFM3
        //
        //resources.ApplyResources(this.cbFM3, "cbFM3");
        this.cbFM3.setSelected(true);
//        this.cbFM3.CheckState = JCheckState.isSelected();
        this.cbFM3.setName("cbFM3");
        // this.cbFM3.UseVisualStyl.setBackground(true);
        //
        // cbFM4
        //
        //resources.ApplyResources(this.cbFM4, "cbFM4");
        this.cbFM4.setSelected(true);
//        this.cbFM4.CheckState = JCheckState.isSelected();
        this.cbFM4.setName("cbFM4");
        // this.cbFM4.UseVisualStyl.setBackground(true);
        //
        // cmbMIDIIN
        //
//        this.cmbMIDIIN.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbMIDIIN.FormattingEnabled = true;
        //resources.ApplyResources(this.cmbMIDIIN, "cmbMIDIIN");
        this.cmbMIDIIN.setName("cmbMIDIIN");
        //
        // label5
        //
        //resources.ApplyResources(this.label5, "label5");
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
        //resources.ApplyResources(this.tpKeyBoard, "tpKeyBoard");
        this.tpKeyBoard.setName("tpKeyBoard");
        // this.tpKeyBoard.UseVisualStyl.setBackground(true);
        //
        // cbUseKeyBoardHook
        //
        //resources.ApplyResources(this.cbUseKeyBoardHook, "cbUseKeyBoardHook");
        this.cbUseKeyBoardHook.setName("cbUseKeyBoardHook");
        // this.cbUseKeyBoardHook.UseVisualStyl.setBackground(true);
        this.cbUseKeyBoardHook.addChangeListener(this::cbUseKeyBoardHook_CheckedChanged);
        //
        // gbUseKeyBoardHook
        //
        //resources.ApplyResources(this.gbUseKeyBoardHook, "gbUseKeyBoardHook");
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
        // this.gbUseKeyBoardHook.TabStop = false;
        //
        // lblKeyBoardHookNotice
        //
        //resources.ApplyResources(this.lblKeyBoardHookNotice, "lblKeyBoardHookNotice");
        this.lblKeyBoardHookNotice.setForeground(Color.red);
        this.lblKeyBoardHookNotice.setName("lblKeyBoardHookNotice");
        //
        // btNextClr
        //
        //resources.ApplyResources(this.btNextClr, "btNextClr");
        this.btNextClr.setName("btNextClr");
        // this.btNextClr.UseVisualStyl.setBackground(true);
        this.btNextClr.addActionListener(this::btNextClr_Click);
        //
        // btPrevClr
        //
        //resources.ApplyResources(this.btPrevClr, "btPrevClr");
        this.btPrevClr.setName("btPrevClr");
        // this.btPrevClr.UseVisualStyl.setBackground(true);
        this.btPrevClr.addActionListener(this::btPrevClr_Click);
        //
        // btPlayClr
        //
        //resources.ApplyResources(this.btPlayClr, "btPlayClr");
        this.btPlayClr.setName("btPlayClr");
        // this.btPlayClr.UseVisualStyl.setBackground(true);
        this.btPlayClr.addActionListener(this::btPlayClr_Click);
        //
        // btPauseClr
        //
        //resources.ApplyResources(this.btPauseClr, "btPauseClr");
        this.btPauseClr.setName("btPauseClr");
        // this.btPauseClr.UseVisualStyl.setBackground(true);
        this.btPauseClr.addActionListener(this::btPauseClr_Click);
        //
        // btFastClr
        //
        //resources.ApplyResources(this.btFastClr, "btFastClr");
        this.btFastClr.setName("btFastClr");
        // this.btFastClr.UseVisualStyl.setBackground(true);
        this.btFastClr.addActionListener(this::btFastClr_Click);
        //
        // btFadeoutClr
        //
        //resources.ApplyResources(this.btFadeoutClr, "btFadeoutClr");
        this.btFadeoutClr.setName("btFadeoutClr");
        // this.btFadeoutClr.UseVisualStyl.setBackground(true);
        this.btFadeoutClr.addActionListener(this::btFadeoutClr_Click);
        //
        // btSlowClr
        //
        //resources.ApplyResources(this.btSlowClr, "btSlowClr");
        this.btSlowClr.setName("btSlowClr");
        // this.btSlowClr.UseVisualStyl.setBackground(true);
        this.btSlowClr.addActionListener(this::btSlowClr_Click);
        //
        // btStopClr
        //
        //resources.ApplyResources(this.btStopClr, "btStopClr");
        this.btStopClr.setName("btStopClr");
        // this.btStopClr.UseVisualStyl.setBackground(true);
        this.btStopClr.addActionListener(this::btStopClr_Click);
        //
        // btNextSet
        //
        //resources.ApplyResources(this.btNextSet, "btNextSet");
        this.btNextSet.setName("btNextSet");
        // this.btNextSet.UseVisualStyl.setBackground(true);
        this.btNextSet.addActionListener(this::btNextSet_Click);
        //
        // btPrevSet
        //
        //resources.ApplyResources(this.btPrevSet, "btPrevSet");
        this.btPrevSet.setName("btPrevSet");
        // this.btPrevSet.UseVisualStyl.setBackground(true);
        this.btPrevSet.addActionListener(this::btPrevSet_Click);
        //
        // btPlaySet
        //
        //resources.ApplyResources(this.btPlaySet, "btPlaySet");
        this.btPlaySet.setName("btPlaySet");
        // this.btPlaySet.UseVisualStyl.setBackground(true);
        this.btPlaySet.addActionListener(this::btPlaySet_Click);
        //
        // btPauseSet
        //
        //resources.ApplyResources(this.btPauseSet, "btPauseSet");
        this.btPauseSet.setName("btPauseSet");
        // this.btPauseSet.UseVisualStyl.setBackground(true);
        this.btPauseSet.addActionListener(this::btPauseSet_Click);
        //
        // btFastSet
        //
        //resources.ApplyResources(this.btFastSet, "btFastSet");
        this.btFastSet.setName("btFastSet");
        // this.btFastSet.UseVisualStyl.setBackground(true);
        this.btFastSet.addActionListener(this::btFastSet_Click);
        //
        // btFadeoutSet
        //
        //resources.ApplyResources(this.btFadeoutSet, "btFadeoutSet");
        this.btFadeoutSet.setName("btFadeoutSet");
        // this.btFadeoutSet.UseVisualStyl.setBackground(true);
        this.btFadeoutSet.addActionListener(this::btFadeoutSet_Click);
        //
        // btSlowSet
        //
        //resources.ApplyResources(this.btSlowSet, "btSlowSet");
        this.btSlowSet.setName("btSlowSet");
        // this.btSlowSet.UseVisualStyl.setBackground(true);
        this.btSlowSet.addActionListener(this::btSlowSet_Click);
        //
        // btStopSet
        //
        //resources.ApplyResources(this.btStopSet, "btStopSet");
        this.btStopSet.setName("btStopSet");
        // this.btStopSet.UseVisualStyl.setBackground(true);
        this.btStopSet.addActionListener(this::btStopSet_Click);
        //
        // label50
        //
        //resources.ApplyResources(this.label50, "label50");
        this.label50.setName("label50");
        //
        // lblNextKey
        //
        //resources.ApplyResources(this.lblNextKey, "lblNextKey");
        this.lblNextKey.setName("lblNextKey");
        //
        // lblFastKey
        //
        //resources.ApplyResources(this.lblFastKey, "lblFastKey");
        this.lblFastKey.setName("lblFastKey");
        //
        // lblPlayKey
        //
        //resources.ApplyResources(this.lblPlayKey, "lblPlayKey");
        this.lblPlayKey.setName("lblPlayKey");
        //
        // lblSlowKey
        //
        //resources.ApplyResources(this.lblSlowKey, "lblSlowKey");
        this.lblSlowKey.setName("lblSlowKey");
        //
        // lblPrevKey
        //
        //resources.ApplyResources(this.lblPrevKey, "lblPrevKey");
        this.lblPrevKey.setName("lblPrevKey");
        //
        // lblFadeoutKey
        //
        //resources.ApplyResources(this.lblFadeoutKey, "lblFadeoutKey");
        this.lblFadeoutKey.setName("lblFadeoutKey");
        //
        // lblPauseKey
        //
        //resources.ApplyResources(this.lblPauseKey, "lblPauseKey");
        this.lblPauseKey.setName("lblPauseKey");
        //
        // lblStopKey
        //
        //resources.ApplyResources(this.lblStopKey, "lblStopKey");
        this.lblStopKey.setName("lblStopKey");
        //
        // pictureBox14
        //
        this.pictureBox14.setIcon(new ImageIcon(mdplayer.properties.Resources.getccStop()));
        //resources.ApplyResources(this.pictureBox14, "pictureBox14");
        this.pictureBox14.setName("pictureBox14");
        // this.pictureBox14.TabStop = false;
        //
        // pictureBox17
        //
        this.pictureBox17.setIcon(new ImageIcon(mdplayer.properties.Resources.getccFadeout()));
        //resources.ApplyResources(this.pictureBox17, "pictureBox17");
        this.pictureBox17.setName("pictureBox17");
        // this.pictureBox17.TabStop = false;
        //
        // cbNextAlt
        //
        //resources.ApplyResources(this.cbNextAlt, "cbNextAlt");
        this.cbNextAlt.setName("cbNextAlt");
        // this.cbNextAlt.UseVisualStyl.setBackground(true);
        //
        // pictureBox16
        //
        this.pictureBox16.setIcon(new ImageIcon(mdplayer.properties.Resources.getccPrevious()));
        //resources.ApplyResources(this.pictureBox16, "pictureBox16");
        this.pictureBox16.setName("pictureBox16");
        // this.pictureBox16.TabStop = false;
        //
        // cbFastAlt
        //
        //resources.ApplyResources(this.cbFastAlt, "cbFastAlt");
        this.cbFastAlt.setName("cbFastAlt");
        // this.cbFastAlt.UseVisualStyl.setBackground(true);
        //
        // pictureBox15
        //
        this.pictureBox15.setIcon(new ImageIcon(mdplayer.properties.Resources.getccPause()));
        //resources.ApplyResources(this.pictureBox15, "pictureBox15");
        this.pictureBox15.setName("pictureBox15");
        // this.pictureBox15.TabStop = false;
        //
        // cbPlayAlt
        //
        //resources.ApplyResources(this.cbPlayAlt, "cbPlayAlt");
        this.cbPlayAlt.setName("cbPlayAlt");
        // this.cbPlayAlt.UseVisualStyl.setBackground(true);
        //
        // pictureBox13
        //
        this.pictureBox13.setIcon(new ImageIcon(mdplayer.properties.Resources.getccSlow()));
        //resources.ApplyResources(this.pictureBox13, "pictureBox13");
        this.pictureBox13.setName("pictureBox13");
        // this.pictureBox13.TabStop = false;
        //
        // cbSlowAlt
        //
        //resources.ApplyResources(this.cbSlowAlt, "cbSlowAlt");
        this.cbSlowAlt.setName("cbSlowAlt");
        // this.cbSlowAlt.UseVisualStyl.setBackground(true);
        //
        // pictureBox12
        //
        this.pictureBox12.setIcon(new ImageIcon(mdplayer.properties.Resources.getccPlay()));
        //resources.ApplyResources(this.pictureBox12, "pictureBox12");
        this.pictureBox12.setName("pictureBox12");
        // this.pictureBox12.TabStop = false;
        //
        // cbPrevAlt
        //
        //resources.ApplyResources(this.cbPrevAlt, "cbPrevAlt");
        this.cbPrevAlt.setName("cbPrevAlt");
        // this.cbPrevAlt.UseVisualStyl.setBackground(true);
        //
        // pictureBox11
        //
        this.pictureBox11.setIcon(new ImageIcon(mdplayer.properties.Resources.getccFast()));
        //resources.ApplyResources(this.pictureBox11, "pictureBox11");
        this.pictureBox11.setName("pictureBox11");
        // this.pictureBox11.TabStop = false;
        //
        // cbFadeoutAlt
        //
        //resources.ApplyResources(this.cbFadeoutAlt, "cbFadeoutAlt");
        this.cbFadeoutAlt.setName("cbFadeoutAlt");
        // this.cbFadeoutAlt.UseVisualStyl.setBackground(true);
        //
        // pictureBox10
        //
        this.pictureBox10.setIcon(new ImageIcon(mdplayer.properties.Resources.getccNext()));
        //resources.ApplyResources(this.pictureBox10, "pictureBox10");
        this.pictureBox10.setName("pictureBox10");
        // this.pictureBox10.TabStop = false;
        //
        // cbPauseAlt
        //
        //resources.ApplyResources(this.cbPauseAlt, "cbPauseAlt");
        this.cbPauseAlt.setName("cbPauseAlt");
        // this.cbPauseAlt.UseVisualStyl.setBackground(true);
        //
        // label37
        //
        //resources.ApplyResources(this.label37, "label37");
        this.label37.setName("label37");
        //
        // cbStopAlt
        //
        //resources.ApplyResources(this.cbStopAlt, "cbStopAlt");
        this.cbStopAlt.setName("cbStopAlt");
        // this.cbStopAlt.UseVisualStyl.setBackground(true);
        //
        // label45
        //
        //resources.ApplyResources(this.label45, "label45");
        this.label45.setName("label45");
        //
        // label46
        //
        //resources.ApplyResources(this.label46, "label46");
        this.label46.setName("label46");
        //
        // label48
        //
        //resources.ApplyResources(this.label48, "label48");
        this.label48.setName("label48");
        //
        // label38
        //
        //resources.ApplyResources(this.label38, "label38");
        this.label38.setName("label38");
        //
        // label39
        //
        //resources.ApplyResources(this.label39, "label39");
        this.label39.setName("label39");
        //
        // label40
        //
        //resources.ApplyResources(this.label40, "label40");
        this.label40.setName("label40");
        //
        // label41
        //
        //resources.ApplyResources(this.label41, "label41");
        this.label41.setName("label41");
        //
        // label42
        //
        //resources.ApplyResources(this.label42, "label42");
        this.label42.setName("label42");
        //
        // cbNextCtrl
        //
        //resources.ApplyResources(this.cbNextCtrl, "cbNextCtrl");
        this.cbNextCtrl.setName("cbNextCtrl");
        // this.cbNextCtrl.UseVisualStyl.setBackground(true);
        //
        // label43
        //
        //resources.ApplyResources(this.label43, "label43");
        this.label43.setName("label43");
        //
        // cbFastCtrl
        //
        //resources.ApplyResources(this.cbFastCtrl, "cbFastCtrl");
        this.cbFastCtrl.setName("cbFastCtrl");
        // this.cbFastCtrl.UseVisualStyl.setBackground(true);
        //
        // label44
        //
        //resources.ApplyResources(this.label44, "label44");
        this.label44.setName("label44");
        //
        // cbPlayCtrl
        //
        //resources.ApplyResources(this.cbPlayCtrl, "cbPlayCtrl");
        this.cbPlayCtrl.setName("cbPlayCtrl");
        // this.cbPlayCtrl.UseVisualStyl.setBackground(true);
        //
        // cbStopShift
        //
        //resources.ApplyResources(this.cbStopShift, "cbStopShift");
        this.cbStopShift.setName("cbStopShift");
        // this.cbStopShift.UseVisualStyl.setBackground(true);
        //
        // cbSlowCtrl
        //
        //resources.ApplyResources(this.cbSlowCtrl, "cbSlowCtrl");
        this.cbSlowCtrl.setName("cbSlowCtrl");
        // this.cbSlowCtrl.UseVisualStyl.setBackground(true);
        //
        // cbPauseShift
        //
        //resources.ApplyResources(this.cbPauseShift, "cbPauseShift");
        this.cbPauseShift.setName("cbPauseShift");
        // this.cbPauseShift.UseVisualStyl.setBackground(true);
        //
        // cbPrevCtrl
        //
        //resources.ApplyResources(this.cbPrevCtrl, "cbPrevCtrl");
        this.cbPrevCtrl.setName("cbPrevCtrl");
        // this.cbPrevCtrl.UseVisualStyl.setBackground(true);
        //
        // cbFadeoutShift
        //
        //resources.ApplyResources(this.cbFadeoutShift, "cbFadeoutShift");
        this.cbFadeoutShift.setName("cbFadeoutShift");
        // this.cbFadeoutShift.UseVisualStyl.setBackground(true);
        //
        // cbFadeoutCtrl
        //
        //resources.ApplyResources(this.cbFadeoutCtrl, "cbFadeoutCtrl");
        this.cbFadeoutCtrl.setName("cbFadeoutCtrl");
        // this.cbFadeoutCtrl.UseVisualStyl.setBackground(true);
        //
        // cbPrevShift
        //
        //resources.ApplyResources(this.cbPrevShift, "cbPrevShift");
        this.cbPrevShift.setName("cbPrevShift");
        // this.cbPrevShift.UseVisualStyl.setBackground(true);
        //
        // cbPauseCtrl
        //
        //resources.ApplyResources(this.cbPauseCtrl, "cbPauseCtrl");
        this.cbPauseCtrl.setName("cbPauseCtrl");
        // this.cbPauseCtrl.UseVisualStyl.setBackground(true);
        //
        // cbSlowShift
        //
        //resources.ApplyResources(this.cbSlowShift, "cbSlowShift");
        this.cbSlowShift.setName("cbSlowShift");
        // this.cbSlowShift.UseVisualStyl.setBackground(true);
        //
        // cbStopCtrl
        //
        //resources.ApplyResources(this.cbStopCtrl, "cbStopCtrl");
        this.cbStopCtrl.setName("cbStopCtrl");
        // this.cbStopCtrl.UseVisualStyl.setBackground(true);
        //
        // cbPlayShift
        //
        //resources.ApplyResources(this.cbPlayShift, "cbPlayShift");
        this.cbPlayShift.setName("cbPlayShift");
        // this.cbPlayShift.UseVisualStyl.setBackground(true);
        //
        // cbNextShift
        //
        //resources.ApplyResources(this.cbNextShift, "cbNextShift");
        this.cbNextShift.setName("cbNextShift");
        // this.cbNextShift.UseVisualStyl.setBackground(true);
        //
        // cbFastShift
        //
        //resources.ApplyResources(this.cbFastShift, "cbFastShift");
        this.cbFastShift.setName("cbFastShift");
        // this.cbFastShift.UseVisualStyl.setBackground(true);
        //
        // label47
        //
        //resources.ApplyResources(this.label47, "label47");
        this.label47.setName("label47");
        //
        // cbStopWin
        //
        //resources.ApplyResources(this.cbStopWin, "cbStopWin");
        this.cbStopWin.setName("cbStopWin");
        // this.cbStopWin.UseVisualStyl.setBackground(true);
        //
        // cbPauseWin
        //
        //resources.ApplyResources(this.cbPauseWin, "cbPauseWin");
        this.cbPauseWin.setName("cbPauseWin");
        // this.cbPauseWin.UseVisualStyl.setBackground(true);
        //
        // cbFadeoutWin
        //
        //resources.ApplyResources(this.cbFadeoutWin, "cbFadeoutWin");
        this.cbFadeoutWin.setName("cbFadeoutWin");
        // this.cbFadeoutWin.UseVisualStyl.setBackground(true);
        //
        // cbPrevWin
        //
        //resources.ApplyResources(this.cbPrevWin, "cbPrevWin");
        this.cbPrevWin.setName("cbPrevWin");
        // this.cbPrevWin.UseVisualStyl.setBackground(true);
        //
        // cbSlowWin
        //
        //resources.ApplyResources(this.cbSlowWin, "cbSlowWin");
        this.cbSlowWin.setName("cbSlowWin");
        // this.cbSlowWin.UseVisualStyl.setBackground(true);
        //
        // cbPlayWin
        //
        //resources.ApplyResources(this.cbPlayWin, "cbPlayWin");
        this.cbPlayWin.setName("cbPlayWin");
        // this.cbPlayWin.UseVisualStyl.setBackground(true);
        //
        // cbFastWin
        //
        //resources.ApplyResources(this.cbFastWin, "cbFastWin");
        this.cbFastWin.setName("cbFastWin");
        // this.cbFastWin.UseVisualStyl.setBackground(true);
        //
        // cbNextWin
        //
        //resources.ApplyResources(this.cbNextWin, "cbNextWin");
        this.cbNextWin.setName("cbNextWin");
        // this.cbNextWin.UseVisualStyl.setBackground(true);
        //
        // tpBalance
        //
        this.tpBalance.add(this.groupBox25);
        this.tpBalance.add(this.cbAutoBalanceUseThis);
        this.tpBalance.add(this.groupBox18);
        //resources.ApplyResources(this.tpBalance, "tpBalance");
        this.tpBalance.setName("tpBalance");
        // this.tpBalance.UseVisualStyl.setBackground(true);
        //
        // groupBox25
        //
        //resources.ApplyResources(this.groupBox25, "groupBox25");
        this.groupBox25.add(this.rbAutoBalanceNotSamePositionAsSongData);
        this.groupBox25.add(this.rbAutoBalanceSamePositionAsSongData);
        this.groupBox25.setName("groupBox25");
        // this.groupBox25.TabStop = false;
        //
        // rbAutoBalanceNotSamePositionAsSongData
        //
        //resources.ApplyResources(this.rbAutoBalanceNotSamePositionAsSongData, "rbAutoBalanceNotSamePositionAsSongData");
        this.rbAutoBalanceNotSamePositionAsSongData.setSelected(true);
        this.rbAutoBalanceNotSamePositionAsSongData.setName("rbAutoBalanceNotSamePositionAsSongData");
        // this.rbAutoBalanceNotSamePositionAsSongData.TabStop = true;
        // this.rbAutoBalanceNotSamePositionAsSongData.UseVisualStyl.setBackground(true);
        //
        // rbAutoBalanceSamePositionAsSongData
        //
        //resources.ApplyResources(this.rbAutoBalanceSamePositionAsSongData, "rbAutoBalanceSamePositionAsSongData");
        this.rbAutoBalanceSamePositionAsSongData.setName("rbAutoBalanceSamePositionAsSongData");
        // this.rbAutoBalanceSamePositionAsSongData.UseVisualStyl.setBackground(true);
        //
        // cbAutoBalanceUseThis
        //
        //resources.ApplyResources(this.cbAutoBalanceUseThis, "cbAutoBalanceUseThis");
        this.cbAutoBalanceUseThis.setSelected(true);
//        this.cbAutoBalanceUseThis.CheckState = JCheckState.isSelected();
        this.cbAutoBalanceUseThis.setName("cbAutoBalanceUseThis");
        // this.cbAutoBalanceUseThis.UseVisualStyl.setBackground(true);
        //
        // groupBox18
        //
        //resources.ApplyResources(this.groupBox18, "groupBox18");
        this.groupBox18.add(this.groupBox24);
        this.groupBox18.add(this.groupBox23);
        this.groupBox18.setName("groupBox18");
        // this.groupBox18.TabStop = false;
        //
        // groupBox24
        //
        this.groupBox24.add(this.groupBox21);
        this.groupBox24.add(this.groupBox22);
        //resources.ApplyResources(this.groupBox24, "groupBox24");
        this.groupBox24.setName("groupBox24");
        // this.groupBox24.TabStop = false;
        //
        // groupBox21
        //
        this.groupBox21.add(this.rbAutoBalanceNotSaveSongBalance);
        this.groupBox21.add(this.rbAutoBalanceSaveSongBalance);
        //resources.ApplyResources(this.groupBox21, "groupBox21");
        this.groupBox21.setName("groupBox21");
        // this.groupBox21.TabStop = false;
        //
        // rbAutoBalanceNotSaveSongBalance
        //
        //resources.ApplyResources(this.rbAutoBalanceNotSaveSongBalance, "rbAutoBalanceNotSaveSongBalance");
        this.rbAutoBalanceNotSaveSongBalance.setSelected(true);
        this.rbAutoBalanceNotSaveSongBalance.setName("rbAutoBalanceNotSaveSongBalance");
        // this.rbAutoBalanceNotSaveSongBalance.TabStop = true;
        // this.rbAutoBalanceNotSaveSongBalance.UseVisualStyl.setBackground(true);
        //
        // rbAutoBalanceSaveSongBalance
        //
        //resources.ApplyResources(this.rbAutoBalanceSaveSongBalance, "rbAutoBalanceSaveSongBalance");
        this.rbAutoBalanceSaveSongBalance.setName("rbAutoBalanceSaveSongBalance");
        // this.rbAutoBalanceSaveSongBalance.UseVisualStyl.setBackground(true);
        //
        // groupBox22
        //
        this.groupBox22.add(this.label4);
        //resources.ApplyResources(this.groupBox22, "groupBox22");
        this.groupBox22.setName("groupBox22");
        // this.groupBox22.TabStop = false;
        //
        // label4
        //
        //resources.ApplyResources(this.label4, "label4");
        this.label4.setName("label4");
        //
        // groupBox23
        //
        //resources.ApplyResources(this.groupBox23, "groupBox23");
        this.groupBox23.add(this.groupBox19);
        this.groupBox23.add(this.groupBox20);
        this.groupBox23.setName("groupBox23");
        // this.groupBox23.TabStop = false;
        //
        // groupBox19
        //
        this.groupBox19.add(this.rbAutoBalanceNotLoadSongBalance);
        this.groupBox19.add(this.rbAutoBalanceLoadSongBalance);
        //resources.ApplyResources(this.groupBox19, "groupBox19");
        this.groupBox19.setName("groupBox19");
        // this.groupBox19.TabStop = false;
        //
        // rbAutoBalanceNotLoadSongBalance
        //
        //resources.ApplyResources(this.rbAutoBalanceNotLoadSongBalance, "rbAutoBalanceNotLoadSongBalance");
        this.rbAutoBalanceNotLoadSongBalance.setSelected(true);
        this.rbAutoBalanceNotLoadSongBalance.setName("rbAutoBalanceNotLoadSongBalance");
        // this.rbAutoBalanceNotLoadSongBalance.TabStop = true;
        // this.rbAutoBalanceNotLoadSongBalance.UseVisualStyl.setBackground(true);
        //
        // rbAutoBalanceLoadSongBalance
        //
        //resources.ApplyResources(this.rbAutoBalanceLoadSongBalance, "rbAutoBalanceLoadSongBalance");
        this.rbAutoBalanceLoadSongBalance.setName("rbAutoBalanceLoadSongBalance");
        // this.rbAutoBalanceLoadSongBalance.UseVisualStyl.setBackground(true);
        //
        // groupBox20
        //
        //resources.ApplyResources(this.groupBox20, "groupBox20");
        this.groupBox20.add(this.rbAutoBalanceNotLoadDriverBalance);
        this.groupBox20.add(this.rbAutoBalanceLoadDriverBalance);
        this.groupBox20.setName("groupBox20");
        // this.groupBox20.TabStop = false;
        this.groupBox20.addFocusListener(this.groupBox20_Enter);
        //
        // rbAutoBalanceNotLoadDriverBalance
        //
        //resources.ApplyResources(this.rbAutoBalanceNotLoadDriverBalance, "rbAutoBalanceNotLoadDriverBalance");
        this.rbAutoBalanceNotLoadDriverBalance.setName("rbAutoBalanceNotLoadDriverBalance");
        // this.rbAutoBalanceNotLoadDriverBalance.UseVisualStyl.setBackground(true);
        //
        // rbAutoBalanceLoadDriverBalance
        //
        //resources.ApplyResources(this.rbAutoBalanceLoadDriverBalance, "rbAutoBalanceLoadDriverBalance");
        this.rbAutoBalanceLoadDriverBalance.setSelected(true);
        this.rbAutoBalanceLoadDriverBalance.setName("rbAutoBalanceLoadDriverBalance");
        // this.rbAutoBalanceLoadDriverBalance.TabStop = true;
        // this.rbAutoBalanceLoadDriverBalance.UseVisualStyl.setBackground(true);
        //
        // tpPlayList
        //
        this.tpPlayList.add(this.groupBox17);
        this.tpPlayList.add(this.cbEmptyPlayList);
        //resources.ApplyResources(this.tpPlayList, "tpPlayList");
        this.tpPlayList.setName("tpPlayList");
        // this.tpPlayList.UseVisualStyl.setBackground(true);
        //
        // groupBox17
        //
        //resources.ApplyResources(this.groupBox17, "groupBox17");
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
        // this.groupBox17.TabStop = false;
        //
        // cbAutoOpenImg
        //
        //resources.ApplyResources(this.cbAutoOpenImg, "cbAutoOpenImg");
        this.cbAutoOpenImg.setName("cbAutoOpenImg");
        // this.cbAutoOpenImg.UseVisualStyl.setBackground(true);
        this.cbAutoOpenImg.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // tbImageExt
        //
        //resources.ApplyResources(this.tbImageExt, "tbImageExt");
        this.tbImageExt.setName("tbImageExt");
        //
        // cbAutoOpenMML
        //
        //resources.ApplyResources(this.cbAutoOpenMML, "cbAutoOpenMML");
        this.cbAutoOpenMML.setName("cbAutoOpenMML");
        // this.cbAutoOpenMML.UseVisualStyl.setBackground(true);
        this.cbAutoOpenMML.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // tbMMLExt
        //
        //resources.ApplyResources(this.tbMMLExt, "tbMMLExt");
        this.tbMMLExt.setName("tbMMLExt");
        //
        // tbTextExt
        //
        //resources.ApplyResources(this.tbTextExt, "tbTextExt");
        this.tbTextExt.setName("tbTextExt");
        //
        // cbAutoOpenText
        //
        //resources.ApplyResources(this.cbAutoOpenText, "cbAutoOpenText");
        this.cbAutoOpenText.setName("cbAutoOpenText");
        // this.cbAutoOpenText.UseVisualStyl.setBackground(true);
        this.cbAutoOpenText.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // label1
        //
        //resources.ApplyResources(this.label1, "label1");
        this.label1.setName("label1");
        //
        // label3
        //
        //resources.ApplyResources(this.label3, "label3");
        this.label3.setName("label3");
        //
        // label2
        //
        //resources.ApplyResources(this.label2, "label2");
        this.label2.setName("label2");
        //
        // cbEmptyPlayList
        //
        //resources.ApplyResources(this.cbEmptyPlayList, "cbEmptyPlayList");
        this.cbEmptyPlayList.setName("cbEmptyPlayList");
        // this.cbEmptyPlayList.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.tpOther, "tpOther");
        this.tpOther.setName("tpOther");
        // this.tpOther.UseVisualStyl.setBackground(true);
        //
        // btnSearchPath
        //
        //resources.ApplyResources(this.btnSearchPath, "btnSearchPath");
        this.btnSearchPath.setName("btnSearchPath");
        // this.btnSearchPath.UseVisualStyl.setBackground(true);
        this.btnSearchPath.addActionListener(this::btnSearchPath_Click);
        //
        // tbSearchPath
        //
        //resources.ApplyResources(this.tbSearchPath, "tbSearchPath");
        this.tbSearchPath.setName("tbSearchPath");
        //
        // label68
        //
        //resources.ApplyResources(this.label68, "label68");
        this.label68.setName("label68");
        //
        // cbNonRenderingForPause
        //
        //resources.ApplyResources(this.cbNonRenderingForPause, "cbNonRenderingForPause");
        this.cbNonRenderingForPause.setName("cbNonRenderingForPause");
        // this.cbNonRenderingForPause.UseVisualStyl.setBackground(true);
        //
        // cbWavSwitch
        //
        //resources.ApplyResources(this.cbWavSwitch, "cbWavSwitch");
        this.cbWavSwitch.setName("cbWavSwitch");
        // this.cbWavSwitch.UseVisualStyl.setBackground(true);
        this.cbWavSwitch.addChangeListener(this::cbWavSwitch_CheckedChanged);
        //
        // cbUseGetInst
        //
        //resources.ApplyResources(this.cbUseGetInst, "cbUseGetInst");
        this.cbUseGetInst.setName("cbUseGetInst");
        // this.cbUseGetInst.UseVisualStyl.setBackground(true);
        this.cbUseGetInst.addChangeListener(this::cbUseGetInst_CheckedChanged);
        //
        // groupBox4
        //
        this.groupBox4.add(this.cmbInstFormat);
        this.groupBox4.add(this.lblInstFormat);
        //resources.ApplyResources(this.groupBox4, "groupBox4");
        this.groupBox4.setName("groupBox4");
        // this.groupBox4.TabStop = false;
        //
        // cmbInstFormat
        //
//        this.cmbInstFormat.DropDownStyle = JComboBoxStyle.DropDownList;
//        this.cmbInstFormat.FormattingEnabled = true;
        m = ((DefaultComboBoxModel) this.cmbInstFormat.getModel());
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items1"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items2"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items3"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items4"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items5"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items6"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items7"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items8"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items9"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items10"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items11"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items12"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items13"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items14"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items15"));
        m.addElement(Resources.getResourceManager().getString("cmbInstFormat.Items16"));
        //resources.ApplyResources(this.cmbInstFormat, "cmbInstFormat");
        this.cmbInstFormat.setName("cmbInstFormat");
        //
        // lblInstFormat
        //
        //resources.ApplyResources(this.lblInstFormat, "lblInstFormat");
        this.lblInstFormat.setName("lblInstFormat");
        //
        // cbDumpSwitch
        //
        //resources.ApplyResources(this.cbDumpSwitch, "cbDumpSwitch");
        this.cbDumpSwitch.setName("cbDumpSwitch");
        // this.cbDumpSwitch.UseVisualStyl.setBackground(true);
        this.cbDumpSwitch.addChangeListener(this::cbDumpSwitch_CheckedChanged);
        //
        // gbWav
        //
        this.gbWav.add(this.btnWavPath);
        this.gbWav.add(this.label7);
        this.gbWav.add(this.tbWavPath);
        //resources.ApplyResources(this.gbWav, "gbWav");
        this.gbWav.setName("gbWav");
        // this.gbWav.TabStop = false;
        //
        // btnWavPath
        //
        //resources.ApplyResources(this.btnWavPath, "btnWavPath");
        this.btnWavPath.setName("btnWavPath");
        // this.btnWavPath.UseVisualStyl.setBackground(true);
        this.btnWavPath.addActionListener(this::btnWavPath_Click);
        //
        // label7
        //
        //resources.ApplyResources(this.label7, "label7");
        this.label7.setName("label7");
        //
        // tbWavPath
        //
        //resources.ApplyResources(this.tbWavPath, "tbWavPath");
        this.tbWavPath.setName("tbWavPath");
        //
        // gbDump
        //
        this.gbDump.add(this.btnDumpPath);
        this.gbDump.add(this.label6);
        this.gbDump.add(this.tbDumpPath);
        //resources.ApplyResources(this.gbDump, "gbDump");
        this.gbDump.setName("gbDump");
        // this.gbDump.TabStop = false;
        //
        // btnDumpPath
        //
        //resources.ApplyResources(this.btnDumpPath, "btnDumpPath");
        this.btnDumpPath.setName("btnDumpPath");
        // this.btnDumpPath.UseVisualStyl.setBackground(true);
        this.btnDumpPath.addActionListener(this::btnDumpPath_Click);
        //
        // label6
        //
        //resources.ApplyResources(this.label6, "label6");
        this.label6.setName("label6");
        //
        // tbDumpPath
        //
        //resources.ApplyResources(this.tbDumpPath, "tbDumpPath");
        this.tbDumpPath.setName("tbDumpPath");
        //
        // label30
        //
        //resources.ApplyResources(this.label30, "label30");
        this.label30.setName("label30");
        //
        // tbScreenFrameRate
        //
        //resources.ApplyResources(this.tbScreenFrameRate, "tbScreenFrameRate");
        this.tbScreenFrameRate.setName("tbScreenFrameRate");
        //
        // label29
        //
        //resources.ApplyResources(this.label29, "label29");
        this.label29.setName("label29");
        //
        // lblLoopTimes
        //
        //resources.ApplyResources(this.lblLoopTimes, "lblLoopTimes");
        this.lblLoopTimes.setName("lblLoopTimes");
        //
        // btnDataPath
        //
        //resources.ApplyResources(this.btnDataPath, "btnDataPath");
        this.btnDataPath.setName("btnDataPath");
        // this.btnDataPath.UseVisualStyl.setBackground(true);
        this.btnDataPath.addActionListener(this::btnDataPath_Click);
        //
        // tbLoopTimes
        //
        //resources.ApplyResources(this.tbLoopTimes, "tbLoopTimes");
        this.tbLoopTimes.setName("tbLoopTimes");
        //
        // tbDataPath
        //
        //resources.ApplyResources(this.tbDataPath, "tbDataPath");
        this.tbDataPath.setName("tbDataPath");
        //
        // label19
        //
        //resources.ApplyResources(this.label19, "label19");
        this.label19.setName("label19");
        //
        // btnResetPosition
        //
        //resources.ApplyResources(this.btnResetPosition, "btnResetPosition");
        this.btnResetPosition.setName("btnResetPosition");
        // this.btnResetPosition.UseVisualStyl.setBackground(true);
        this.btnResetPosition.addActionListener(this::btnResetPosition_Click);
        //
        // btnOpenSettingFolder
        //
        //resources.ApplyResources(this.btnOpenSettingFolder, "btnOpenSettingFolder");
        this.btnOpenSettingFolder.setName("btnOpenSettingFolder");
        // this.btnOpenSettingFolder.UseVisualStyl.setBackground(true);
        this.btnOpenSettingFolder.addActionListener(this::btnOpenSettingFolder_Click);
        //
        // cbExALL
        //
        //resources.ApplyResources(this.cbExALL, "cbExALL");
        this.cbExALL.setName("cbExALL");
        // this.cbExALL.UseVisualStyl.setBackground(true);
        this.cbExALL.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // cbInitAlways
        //
        //resources.ApplyResources(this.cbInitAlways, "cbInitAlways");
        this.cbInitAlways.setName("cbInitAlways");
        // this.cbInitAlways.UseVisualStyl.setBackground(true);
        this.cbInitAlways.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // cbAutoOpen
        //
        //resources.ApplyResources(this.cbAutoOpen, "cbAutoOpen");
        this.cbAutoOpen.setName("cbAutoOpen");
        // this.cbAutoOpen.UseVisualStyl.setBackground(true);
        this.cbAutoOpen.addChangeListener(this::cbUseLoopTimes_CheckedChanged);
        //
        // cbUseLoopTimes
        //
        //resources.ApplyResources(this.cbUseLoopTimes, "cbUseLoopTimes");
        this.cbUseLoopTimes.setName("cbUseLoopTimes");
        // this.cbUseLoopTimes.UseVisualStyl.setBackground(true);
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
        //resources.ApplyResources(this.tpOmake, "tpOmake");
        this.tpOmake.setName("tpOmake");
        // this.tpOmake.UseVisualStyl.setBackground(true);
        //
        // label67
        //
        //resources.ApplyResources(this.label67, "label67");
        this.label67.setName("label67");
        //
        // label14
        //
        //resources.ApplyResources(this.label14, "label14");
        this.label14.setName("label14");
        //
        // btVST
        //
        //resources.ApplyResources(this.btVST, "btVST");
        this.btVST.setName("btVST");
        // this.btVST.UseVisualStyl.setBackground(true);
        this.btVST.addActionListener(this::btVST_Click);
        //
        // tbSCCbaseAddress
        //
        //resources.ApplyResources(this.tbSCCbaseAddress, "tbSCCbaseAddress");
        this.tbSCCbaseAddress.setName("tbSCCbaseAddress");
        //
        // tbVST
        //
        //resources.ApplyResources(this.tbVST, "tbVST");
        this.tbVST.setName("tbVST");
        //
        // groupBox5
        //
        this.groupBox5.add(this.cbDispFrameCounter);
        //resources.ApplyResources(this.groupBox5, "groupBox5");
        this.groupBox5.setName("groupBox5");
        // this.groupBox5.TabStop = false;
        //
        // cbDispFrameCounter
        //
        //resources.ApplyResources(this.cbDispFrameCounter, "cbDispFrameCounter");
        this.cbDispFrameCounter.setName("cbDispFrameCounter");
        // this.cbDispFrameCounter.UseVisualStyl.setBackground(true);
        //
        // tpAbout
        //
        this.tpAbout.add(this.tableLayoutPanel);
        //resources.ApplyResources(this.tpAbout, "tpAbout");
        this.tpAbout.setName("tpAbout");
        // this.tpAbout.UseVisualStyl.setBackground(true);
        //
        // tableLayoutPanel
        //
        //resources.ApplyResources(this.tableLayoutPanel, "tableLayoutPanel");
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
        //resources.ApplyResources(this.logoBufferedImage, "logoBufferedImage");
        this.logoBufferedImage.setIcon(new ImageIcon(mdplayer.properties.Resources.getFeliAndMD2()));
        this.logoBufferedImage.setName("logoBufferedImage");
//        this.tableLayoutPanel.SetRowSpan(this.logoBufferedImage, 6);
        // this.logoBufferedImage.TabStop = false;
        //
        // labelProductName
        //
        //resources.ApplyResources(this.labelProductName, "labelProductName");
        this.labelProductName.setName("labelProductName");
        //
        // labelVersion
        //
        //resources.ApplyResources(this.labelVersion, "labelVersion");
        this.labelVersion.setName("labelVersion");
        //
        // labelCopyright
        //
        //resources.ApplyResources(this.labelCopyright, "labelCopyright");
        this.labelCopyright.setName("labelCopyright");
        //
        // labelCompanyName
        //
        //resources.ApplyResources(this.labelCompanyName, "labelCompanyName");
        this.labelCompanyName.setName("labelCompanyName");
        //
        // textBoxDescription
        //
        //resources.ApplyResources(this.textBoxDescription, "textBoxDescription");
        this.textBoxDescription.setName("textBoxDescription");
        this.textBoxDescription.setEditable(true);
        // this.textBoxDescription.TabStop = false;
        //
        // llOpenGithub
        //
        //resources.ApplyResources(this.llOpenGithub, "llOpenGithub");
        this.llOpenGithub.setName("llOpenGithub");
        // this.llOpenGithub.TabStop = true;
        this.llOpenGithub.addMouseListener(this.llOpenGithub_LinkClicked);
        //
        // frmSetting
        //
        //resources.ApplyResources(this, "$this");
//            this.AutoScaleMode = JAutoScaleMode.Font;
//        this.CancelButton = this.btnCancel;
        this.getContentPane().add(this.tcSetting);
        this.getContentPane().add(this.btnCancel);
        this.getContentPane().add(this.btnOK);
//        this.MaximizeBox = false;
//        this.MinimizeBox = false;
        this.setTitle("frmSetting");
        this.addWindowListener(this.windowListener);
        // this.gbWaveOut.ResumeLayout(false);
        // this.gbAsioOut.ResumeLayout(false);
        // this.gbWasapiOut.ResumeLayout(false);
        // this.gbWasapiOut.PerformLayout();
        // this.gbDirectSound.ResumeLayout(false);
        // this.tcSetting.ResumeLayout(false);
        // this.tpOutput.ResumeLayout(false);
        // this.tpOutput.PerformLayout();
        // this.groupBox16.ResumeLayout(false);
        // this.tpModule.ResumeLayout(false);
        // this.groupBox1.ResumeLayout(false);
        // this.groupBox1.PerformLayout();
        // this.groupBox3.ResumeLayout(false);
        // this.groupBox3.PerformLayout();
        // this.tpNuked.ResumeLayout(false);
        // this.groupBox29.ResumeLayout(false);
        // this.groupBox29.PerformLayout();
        // this.groupBox26.ResumeLayout(false);
        // this.groupBox26.PerformLayout();
        // this.tpNSF.ResumeLayout(false);
        // this.tpNSF.PerformLayout();
        //((System.ComponentModel.ISupportInitialize)(this.trkbNSFLPF)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.trkbNSFHPF)).EndInit();
        // this.groupBox10.ResumeLayout(false);
        // this.groupBox10.PerformLayout();
        // this.groupBox12.ResumeLayout(false);
        // this.groupBox12.PerformLayout();
        // this.groupBox11.ResumeLayout(false);
        // this.groupBox11.PerformLayout();
        // this.groupBox9.ResumeLayout(false);
        // this.groupBox9.PerformLayout();
        // this.groupBox8.ResumeLayout(false);
        // this.groupBox8.PerformLayout();
        // this.tpSID.ResumeLayout(false);
        // this.tpSID.PerformLayout();
        // this.groupBox28.ResumeLayout(false);
        // this.groupBox28.PerformLayout();
        // this.groupBox27.ResumeLayout(false);
        // this.groupBox27.PerformLayout();
        // this.groupBox14.ResumeLayout(false);
        // this.groupBox14.PerformLayout();
        // this.groupBox13.ResumeLayout(false);
        // this.groupBox13.PerformLayout();
        // this.tpPMDDotNET.ResumeLayout(false);
        // this.tpPMDDotNET.PerformLayout();
        // this.gbPMDManual.ResumeLayout(false);
        // this.gbPMDManual.PerformLayout();
        // this.groupBox32.ResumeLayout(false);
        // this.groupBox32.PerformLayout();
        // this.gbPPSDRV.ResumeLayout(false);
        // this.groupBox33.ResumeLayout(false);
        // this.groupBox33.PerformLayout();
        // this.gbPMDSetManualVolume.ResumeLayout(false);
        // this.gbPMDSetManualVolume.PerformLayout();
        // this.tpMIDIOut.ResumeLayout(false);
        // this.tpMIDIOut.PerformLayout();
        // this.tbcMIDIoutList.ResumeLayout(false);
        // this.tabPage1.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListA)).EndInit();
        // this.tabPage2.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListB)).EndInit();
        // this.tabPage3.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListC)).EndInit();
        // this.tabPage4.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListD)).EndInit();
        // this.tabPage5.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListE)).EndInit();
        // this.tabPage6.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListF)).EndInit();
        // this.tabPage7.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListG)).EndInit();
        // this.tabPage8.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListH)).EndInit();
        // this.tabPage9.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListI)).EndInit();
        // this.tabPage10.ResumeLayout(false);
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutListJ)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.dgvMIDIoutPallet)).EndInit();
        // this.tpMIDIOut2.ResumeLayout(false);
        // this.groupBox15.ResumeLayout(false);
        // this.groupBox15.PerformLayout();
        // this.tabMIDIExp.ResumeLayout(false);
        // this.tabMIDIExp.PerformLayout();
        // this.gbMIDIExport.ResumeLayout(false);
        // this.gbMIDIExport.PerformLayout();
        // this.groupBox6.ResumeLayout(false);
        // this.groupBox6.PerformLayout();
        // this.tpMIDIKBD.ResumeLayout(false);
        // this.tpMIDIKBD.PerformLayout();
        // this.gbMIDIKeyboard.ResumeLayout(false);
        // this.gbMIDIKeyboard.PerformLayout();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox8)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox7)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox6)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox5)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox4)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox3)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
        // this.gbUseChannel.ResumeLayout(false);
        // this.gbUseChannel.PerformLayout();
        // this.groupBox7.ResumeLayout(false);
        // this.groupBox7.PerformLayout();
        // this.groupBox2.ResumeLayout(false);
        // this.groupBox2.PerformLayout();
        // this.tpKeyBoard.ResumeLayout(false);
        // this.tpKeyBoard.PerformLayout();
        // this.gbUseKeyBoardHook.ResumeLayout(false);
        // this.gbUseKeyBoardHook.PerformLayout();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox14)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox17)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox16)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox15)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox13)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox12)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox11)).EndInit();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox10)).EndInit();
        // this.tpBalance.ResumeLayout(false);
        // this.tpBalance.PerformLayout();
        // this.groupBox25.ResumeLayout(false);
        // this.groupBox25.PerformLayout();
        // this.groupBox18.ResumeLayout(false);
        // this.groupBox24.ResumeLayout(false);
        // this.groupBox21.ResumeLayout(false);
        // this.groupBox21.PerformLayout();
        // this.groupBox22.ResumeLayout(false);
        // this.groupBox22.PerformLayout();
        // this.groupBox23.ResumeLayout(false);
        // this.groupBox19.ResumeLayout(false);
        // this.groupBox19.PerformLayout();
        // this.groupBox20.ResumeLayout(false);
        // this.groupBox20.PerformLayout();
        // this.tpPlayList.ResumeLayout(false);
        // this.tpPlayList.PerformLayout();
        // this.groupBox17.ResumeLayout(false);
        // this.groupBox17.PerformLayout();
        // this.tpOther.ResumeLayout(false);
        // this.tpOther.PerformLayout();
        // this.groupBox4.ResumeLayout(false);
        // this.groupBox4.PerformLayout();
        // this.gbWav.ResumeLayout(false);
        // this.gbWav.PerformLayout();
        // this.gbDump.ResumeLayout(false);
        // this.gbDump.PerformLayout();
        // this.tpOmake.ResumeLayout(false);
        // this.tpOmake.PerformLayout();
        // this.groupBox5.ResumeLayout(false);
        // this.groupBox5.PerformLayout();
        // this.tpAbout.ResumeLayout(false);
        // this.tableLayoutPanel.ResumeLayout(false);
        // this.tableLayoutPanel.PerformLayout();
        //((System.ComponentModel.ISupportInitialize)(this.logoBufferedImage)).EndInit();
//        this.ResumeLayout(false);
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
    private JComboBox cmbWaveOutDevice;
    private JButton btnASIOControlPanel;
    private JComboBox cmbAsioDevice;
    private JComboBox cmbWasapiDevice;
    private JComboBox cmbDirectSoundDevice;
    private JTabbedPane tcSetting;
    private JTabbedPane tpOutput;
    private JTabbedPane tpAbout;
    private JTable tableLayoutPanel;
    private JLabel logoBufferedImage;
    private JLabel labelProductName;
    private JLabel labelVersion;
    private JLabel labelCopyright;
    private JLabel labelCompanyName;
    private JTextArea textBoxDescription;
    private JTabbedPane tpOther;
    private JPanel gbMIDIKeyboard;
    private JPanel gbUseChannel;
    private JCheckBox cbFM1;
    private JCheckBox cbFM2;
    private JCheckBox cbFM3;
    private JCheckBox cbUseMIDIKeyboard;
    private JCheckBox cbFM4;
    private JCheckBox cbFM5;
    private JCheckBox cbFM6;
    private JComboBox cmbMIDIIN;
    private JLabel label5;
    private JCheckBox rbExclusive;
    private JCheckBox rbShare;
    private JLabel lblLatencyUnit;
    private JLabel lblLatency;
    private JComboBox cmbLatency;
    private JTabbedPane tpModule;
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
    private JTabbedPane tpMIDIKBD;
    private JComboBox cmbInstFormat;
    private JLabel lblInstFormat;
    private JLabel label30;
    private JTextArea tbScreenFrameRate;
    private JLabel label29;
    private JCheckBox cbAutoOpen;
    private ucSettingInstruments ucSI;
    private JPanel groupBox1;
    private JPanel groupBox4;
    private JCheckBox cbDumpSwitch;
    private JPanel gbDump;
    private JButton btnDumpPath;
    private JLabel label6;
    private JTextArea tbDumpPath;
    private JButton btnResetPosition;
    private JTabbedPane tabMIDIExp;
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
    private JTabbedPane tpOmake;
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
    private JTabbedPane tpMIDIOut;
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
    private JTabbedPane tabPage1;
    private JTabbedPane tabPage2;
    private JTabbedPane tabPage3;
    private JTabbedPane tabPage4;
    private JButton btnUP_B;
    private JButton btnDOWN_B;
    private JButton btnUP_C;
    private JButton btnDOWN_C;
    private JButton btnUP_D;
    private JButton btnDOWN_D;
    private JTabbedPane tabPage5;
    private JButton btnUP_E;
    private JButton btnDOWN_E;
    private JTabbedPane tabPage6;
    private JButton btnUP_F;
    private JButton btnDOWN_F;
    private JTabbedPane tabPage7;
    private JButton btnUP_G;
    private JButton btnDOWN_G;
    private JTabbedPane tabPage8;
    private JButton btnUP_H;
    private JButton btnDOWN_H;
    private JTabbedPane tabPage9;
    private JButton btnUP_I;
    private JButton btnDOWN_I;
    private JTabbedPane tabPage10;
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
    private JTabbedPane tpNSF;
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
    private JTabbedPane tpSID;
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
    private JTabbedPane tpMIDIOut2;
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
    private JTabbedPane tpBalance;
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
    private JTabbedPane tpKeyBoard;
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
    private JTabbedPane tpNuked;
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
    private JTabbedPane tpPMDDotNET;
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
    private JTabbedPane tpPlayList;
    private JCheckBox cbAutoOpenImg;
    private JCheckBox cbAutoOpenMML;
    private JCheckBox cbAutoOpenText;
    private JLabel label66;
    private JLabel label65;
    private JComboBox cmbSampleRate;
    private JLabel label67;
    private JTextArea tbSCCbaseAddress;
    private JButton btnSearchPath;
    private JTextArea tbSearchPath;
    private JLabel label68;
    private JCheckBox cbNSFDmc_DPCMReverse;
    private JCheckBox cbUnuseRealChip;
}


class BindData implements PropertyChangeListener {
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


