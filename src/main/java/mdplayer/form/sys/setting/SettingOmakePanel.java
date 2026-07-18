package mdplayer.form.sys.setting;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.form.SettingTab;
import mdplayer.form.sys.FormSetting;

import static java.lang.System.getLogger;

/** the "Omake" page of the settings dialog, split out of the original FormSetting */
public class SettingOmakePanel extends SettingTab {

    @Override
    public int order() {
        return 150;
    }

    private static final Logger logger = getLogger(SettingOmakePanel.class.getName());

    private boolean IsInitialOpenFolder;

    private JPanel groupBox5;
    private JCheckBox cbDispFrameCounter;
    private JLabel label14;
    private JButton btVST;
    private JTextArea tbVST;
    private JLabel label67;
    private JTextArea tbSCCbaseAddress;

    public SettingOmakePanel() {
        this.label67 = new JLabel();
        this.label14 = new JLabel();
        this.btVST = new JButton();
        this.tbSCCbaseAddress = new JTextArea();
        this.tbVST = new JTextArea();
        this.groupBox5 = new JPanel();
        this.cbDispFrameCounter = new JCheckBox();

        //
        // btVST
        //
        this.btVST.setName("btVST");
        this.btVST.addActionListener(this::btVST_Click);
        //
        // cbDispFrameCounter
        //
        this.cbDispFrameCounter.setName("cbDispFrameCounter");
        //
        // groupBox5
        //
        this.groupBox5.add(this.cbDispFrameCounter);
        this.groupBox5.setName("groupBox5");
        //
        // label14
        //
        this.label14.setName("label14");
        //
        // label67
        //
        this.label67.setName("label67");
        //
        // tbSCCbaseAddress
        //
        this.tbSCCbaseAddress.setName("tbSCCbaseAddress");
        //
        // tbVST
        //
        this.tbVST.setName("tbVST");
        //
        // tpOmake
        //
        this.add(this.label67);
        this.add(this.label14);
        this.add(this.btVST);
        this.add(this.tbSCCbaseAddress);
        this.add(this.tbVST);
        this.add(this.groupBox5);
        this.setName("tpOmake");
    }

    @Override
    public void load(Setting setting) {
        cbDispFrameCounter.setSelected(setting.getDebug_DispFrameCounter());
        tbSCCbaseAddress.setText("%04X".formatted(setting.getDebug_SCCbaseAddress()));
    }

    @Override
    public void apply(Setting setting) {
        setting.setDebug_DispFrameCounter(cbDispFrameCounter.isSelected());
        setting.setDebug_SCCbaseAddress(FormSetting.parseHexSafe(tbSCCbaseAddress.getText(), setting.getDebug_SCCbaseAddress()));
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
}
