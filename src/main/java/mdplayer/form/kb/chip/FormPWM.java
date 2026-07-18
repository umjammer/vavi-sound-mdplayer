package mdplayer.form.kb.chip;

import java.awt.Dimension;

import mdplayer.form.FormBase;
import mdplayer.form.View;
import mdplayer.form.kb.ViewProvider;
import mdplayer.form.sys.FormMain;


public class FormPWM extends FormBase {

    public FormPWM() {
        initializeComponent();
    }

    private void initializeComponent() {

        //
        // frmPWM
        //
        this.setPreferredSize(new Dimension(800, 450));
        this.setName("frmPWM");
        this.setTitle("frmPWM");
    }

    /**
     * The PWM has no working panel yet ({@link FormPWM} is unported); the provider exists for its
     * mixer fader slot.
     */
    public static class Provider implements ViewProvider {

        @Override public String id() { return "PWM"; }
        @Override public Class<? extends mdplayer.Chip> chip() { return mdplayer.chips.PwmChip.class; }
        @Override public boolean hasMenuItem() { return false; }
        @Override public View create(FormMain frm, int chipId, int zoom) { return null; }

        @Override public java.util.List<MixerSlot> mixerSlots() {
            return java.util.List.of(new MixerSlot(36, mdsound.MDSound.Chip.MAIN_TAG, mdplayer.chips.PwmChip.class, "pwm", 200));
        }
    }
}
