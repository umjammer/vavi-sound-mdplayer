package mdplayer.form.kb.chip;

import java.awt.Dimension;
import java.util.List;

import mdplayer.form.FormBase;
import mdplayer.form.View;
import mdplayer.form.kb.ViewProvider;
import mdplayer.form.sys.FormMain;


public class FormSAA1099 extends FormBase {

    public FormSAA1099() {
        initializeComponent();
    }

    private void initializeComponent() {

        //
        // frmSAA1099
        //
        this.setPreferredSize(new Dimension(800, 450));
        this.setName("frmSAA1099");
        this.setTitle("frmSAA1099");
    }

    /**
     * The SAA1099 has no working panel yet ({@link FormSAA1099} is unported); the provider exists for its
     * mixer fader slot.
     */
    public static class Provider implements ViewProvider {

        @Override public String id() { return "SAA1099"; }
        @Override public Class<? extends mdplayer.Chip> chip() { return mdplayer.chips.Saa1099Chip.class; }
        @Override public boolean hasMenuItem() { return false; }
        @Override public View create(FormMain frm, int chipId, int zoom) { return null; }

        @Override public List<MixerSlot> mixerSlots() {
            return List.of(new MixerSlot(28, mdsound.MDSound.Chip.MAIN_TAG, mdplayer.chips.Saa1099Chip.class, "saa1099", 200));
        }
    }
}
