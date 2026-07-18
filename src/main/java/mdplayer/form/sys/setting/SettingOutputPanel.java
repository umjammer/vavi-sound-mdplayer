package mdplayer.form.sys.setting;

import java.lang.System.Logger;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.form.SettingTab;
import mdplayer.form.sys.FormSetting;

import static java.lang.System.getLogger;

/** the "Output" page of the settings dialog, split out of the original FormSetting */
public class SettingOutputPanel extends SettingTab {

    @Override
    public int order() {
        return 10;
    }

    private static final Logger logger = getLogger(SettingOutputPanel.class.getName());

    /** the designer's geometry and captions, converted from frmSetting.resx */
    private static final ResourceBundle resources = ResourceBundle.getBundle("mdplayer/form/sys/frmSetting", Locale.getDefault());

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
    private JCheckBox rbExclusive;
    private JCheckBox rbShare;
    private JLabel lblLatencyUnit;
    private JLabel lblLatency;
    private JComboBox<String> cmbLatency;
    private JLabel lblWaitTime;
    private JLabel label28;
    private JComboBox<String> cmbWaitTime;
    private JLabel label36;
    private JCheckBox rbSPPCM;
    private JPanel groupBox16;
    private JComboBox<String> cmbSPPCMDevice;
    private JCheckBox rbNullDevice;
    private JLabel label66;
    private JLabel label65;
    private JComboBox<String> cmbSampleRate;

    public SettingOutputPanel() {
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
        // cmbDirectSoundDevice
        //
        this.cmbDirectSoundDevice.setName("cmbDirectSoundDevice");
        //
        // cmbLatency
        //
        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) this.cmbLatency.getModel();
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
        // cmbSPPCMDevice
        //
        this.cmbSPPCMDevice.setName("cmbSPPCMDevice");
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
        // cmbWaitTime
        //
        m = (DefaultComboBoxModel<String>) this.cmbWaitTime.getModel();
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
        // cmbWasapiDevice
        //
        this.cmbWasapiDevice.setName("cmbWasapiDevice");
        //
        // cmbWaveOutDevice
        //
        this.cmbWaveOutDevice.setName("cmbWaveOutDevice");
        //
        // gbAsioOut
        //
        this.gbAsioOut.add(this.btnASIOControlPanel);
        this.gbAsioOut.add(this.cmbAsioDevice);
        this.gbAsioOut.setName("gbAsioOut");
        //
        // gbDirectSound
        //
        this.gbDirectSound.add(this.cmbDirectSoundDevice);
        this.gbDirectSound.setName("gbDirectSound");
        //
        // gbWasapiOut
        //
        this.gbWasapiOut.add(this.rbExclusive);
        this.gbWasapiOut.add(this.rbShare);
        this.gbWasapiOut.add(this.cmbWasapiDevice);
        this.gbWasapiOut.setName("gbWasapiOut");
        //
        // gbWaveOut
        //
        this.gbWaveOut.add(this.cmbWaveOutDevice);
        this.gbWaveOut.setName("gbWaveOut");
        //
        // groupBox16
        //
        this.groupBox16.add(this.cmbSPPCMDevice);
        this.groupBox16.setName("groupBox16");
        //
        // label28
        //
        this.label28.setName("label28");
        //
        // label36
        //
        this.label36.setName("label36");
        //
        // label65
        //
        this.label65.setName("label65");
        //
        // label66
        //
        this.label66.setName("label66");
        //
        // lblLatency
        //
        this.lblLatency.setName("lblLatency");
        //
        // lblLatencyUnit
        //
        this.lblLatencyUnit.setName("lblLatencyUnit");
        //
        // lblWaitTime
        //
        this.lblWaitTime.setName("lblWaitTime");
        //
        // rbAsioOut
        //
        this.rbAsioOut.setName("rbAsioOut");
        this.rbAsioOut.addChangeListener(this::rbAsioOut_CheckedChanged);
        //
        // rbDirectSoundOut
        //
        this.rbDirectSoundOut.setName("rbDirectSoundOut");
        this.rbDirectSoundOut.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // rbExclusive
        //
        this.rbExclusive.setName("rbExclusive");
        //
        // rbNullDevice
        //
        this.rbNullDevice.setName("rbNullDevice");
        this.rbNullDevice.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // rbSPPCM
        //
        this.rbSPPCM.setName("rbSPPCM");
        this.rbSPPCM.addChangeListener(this::rbDirectSoundOut_CheckedChanged);
        //
        // rbShare
        //
        this.rbShare.setName("rbShare");
        //
        // rbWasapiOut
        //
        this.rbWasapiOut.setName("rbWasapiOut");
        this.rbWasapiOut.addChangeListener(this::rbWasapiOut_CheckedChanged);
        //
        // rbWaveOut
        //
        this.rbWaveOut.setSelected(true);
        this.rbWaveOut.setName("rbWaveOut");
        this.rbWaveOut.addChangeListener(this::rbWaveOut_CheckedChanged);
        //
        // tpOutput
        //
        this.add(this.rbNullDevice);
        this.add(this.label36);
        this.add(this.lblWaitTime);
        this.add(this.label66);
        this.add(this.lblLatencyUnit);
        this.add(this.label28);
        this.add(this.label65);
        this.add(this.lblLatency);
        this.add(this.cmbWaitTime);
        this.add(this.cmbSampleRate);
        this.add(this.cmbLatency);
        this.add(this.rbSPPCM);
        this.add(this.rbDirectSoundOut);
        this.add(this.rbWaveOut);
        this.add(this.rbAsioOut);
        this.add(this.gbWaveOut);
        this.add(this.rbWasapiOut);
        this.add(this.groupBox16);
        this.add(this.gbAsioOut);
        this.add(this.gbDirectSound);
        this.add(this.gbWasapiOut);
        this.setName("tpOutput");
    }

    @Override
    public void load(Setting setting) {
        this.cmbLatency.setSelectedIndex(5);
        this.cmbWaitTime.setSelectedIndex(0);
        Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixersInfo) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
            for (Line.Info info : sourceLineInfo) {
                if (info instanceof DataLine.Info dataLineInfo)
                    cmbDirectSoundDevice.addItem(dataLineInfo.toString());
            }
        }
        switch (setting.getOutputDevice().getDeviceType()) {
            case 0:
            default:
                rbWaveOut.setSelected(true);
                break;
            case 1:
                rbDirectSoundOut.setSelected(true);
                break;
            case 2:
                if (FormSetting.wasapiSupported) rbWasapiOut.setSelected(true);
                else rbWaveOut.setSelected(true);
                break;
            case 3:
                if (FormSetting.asioSupported) rbAsioOut.setSelected(true);
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
        for (int i = 0; i < cmbSampleRate.getItemCount(); i++) {
            if (cmbSampleRate.getItemAt(i).equals(String.valueOf(setting.getOutputDevice().getSampleRate()))) {
                cmbSampleRate.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    public void apply(Setting setting) {
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
    }

    private void rbAsioOut_CheckedChanged(ChangeEvent ev) {
        lblLatency.setEnabled(false);
        lblLatencyUnit.setEnabled(false);
        cmbLatency.setEnabled(false);
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

    private void rbWaveOut_CheckedChanged(ChangeEvent ev) {
        lblLatency.setEnabled(true);
        lblLatencyUnit.setEnabled(true);
        cmbLatency.setEnabled(true);
    }
}
