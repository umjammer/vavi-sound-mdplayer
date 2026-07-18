package mdplayer.form.sys.setting;

import java.lang.System.Logger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import mdplayer.Setting;
import mdplayer.form.SettingTab;
import static java.lang.System.getLogger;

/** the "PlayList" page of the settings dialog, split out of the original FormSetting */
public class SettingPlayListPanel extends SettingTab {

    @Override
    public int order() {
        return 130;
    }

    private static final Logger logger = getLogger(SettingPlayListPanel.class.getName());

    private JPanel groupBox17;
    private JTextArea tbImageExt;
    private JTextArea tbMMLExt;
    private JTextArea tbTextExt;
    private JLabel label1;
    private JLabel label3;
    private JLabel label2;
    private JCheckBox cbEmptyPlayList;
    private JCheckBox cbAutoOpenImg;
    private JCheckBox cbAutoOpenMML;
    private JCheckBox cbAutoOpenText;

    public SettingPlayListPanel() {
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

        //
        // cbAutoOpenImg
        //
        this.cbAutoOpenImg.setName("cbAutoOpenImg");
        //
        // cbAutoOpenMML
        //
        this.cbAutoOpenMML.setName("cbAutoOpenMML");
        //
        // cbAutoOpenText
        //
        this.cbAutoOpenText.setName("cbAutoOpenText");
        //
        // cbEmptyPlayList
        //
        this.cbEmptyPlayList.setName("cbEmptyPlayList");
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
        // label1
        //
        this.label1.setName("label1");
        //
        // label2
        //
        this.label2.setName("label2");
        //
        // label3
        //
        this.label3.setName("label3");
        //
        // tbImageExt
        //
        this.tbImageExt.setName("tbImageExt");
        //
        // tbMMLExt
        //
        this.tbMMLExt.setName("tbMMLExt");
        //
        // tbTextExt
        //
        this.tbTextExt.setName("tbTextExt");
        //
        // tpPlayList
        //
        this.add(this.groupBox17);
        this.add(this.cbEmptyPlayList);
        this.setName("tpPlayList");
    }

    @Override
    public void load(Setting setting) {
        tbTextExt.setText(setting.getOther().getTextExt());
        tbMMLExt.setText(setting.getOther().getMMLExt());
        cbAutoOpenText.setSelected(setting.getOther().getAutoOpenText());
        cbAutoOpenMML.setSelected(setting.getOther().getAutoOpenMML());
        cbAutoOpenImg.setSelected(setting.getOther().getAutoOpenImg());
        tbImageExt.setText(setting.getOther().getImageExt());
        cbEmptyPlayList.setSelected(setting.getOther().getEmptyPlayList());
    }

    @Override
    public void apply(Setting setting) {
        setting.getOther().setTextExt(tbTextExt.getText());
        setting.getOther().setMMLExt(tbMMLExt.getText());
        setting.getOther().setImageExt(tbImageExt.getText());
        setting.getOther().setAutoOpenText(cbAutoOpenText.isSelected());
        setting.getOther().setAutoOpenMML(cbAutoOpenMML.isSelected());
        setting.getOther().setAutoOpenImg(cbAutoOpenImg.isSelected());
        setting.getOther().setEmptyPlayList(cbEmptyPlayList.isSelected());
    }
}
