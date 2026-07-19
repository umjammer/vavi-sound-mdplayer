package mdplayer.form.kb.chip;

import java.util.List;

import mdplayer.Chip;
import mdplayer.chips.SidChip;
import mdplayer.form.SettingTab;
import mdplayer.form.View;
import mdplayer.form.kb.ViewProvider;
import mdplayer.form.sys.FormMain;
import mdplayer.form.sys.setting.SettingSIDPanel;


/** The SID has no panel of its own yet; it is registered only for the register-dump menu. */
public class SidViewProvider implements ViewProvider {

    @Override public String id() { return "SID"; }
    @Override public Class<? extends Chip> chip() { return SidChip.class; }
    @Override public boolean perChip() { return false; }
    @Override public boolean hasMenuItem() { return false; }
    @Override public boolean hasRegisterDump() { return true; }
    @Override public View create(FormMain frm, int chipId, int zoom) { return null; }
    @Override public List<SettingTab> settingTabs() { return List.of(new SettingSIDPanel()); }
}
