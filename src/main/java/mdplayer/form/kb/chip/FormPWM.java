package mdplayer.form.kb.chip;

import java.awt.Dimension;
import java.util.List;

import mdplayer.chips.PwmChip;
import mdplayer.form.FormBase;
import mdplayer.form.View;
import mdplayer.form.kb.ViewProvider;
import mdplayer.form.sys.FormMain;
import mdsound.MDSound;


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
        @Override public Class<? extends mdplayer.Chip> chip() { return PwmChip.class; }
        @Override public boolean hasMenuItem() { return false; }
        @Override public View create(FormMain frm, int chipId, int zoom) { return null; }

        @Override public List<MixerSlot> mixerSlots() {
            return List.of(new MixerSlot(36, MDSound.Chip.MAIN_TAG, PwmChip.class, "pwm", 200));
        }
    }
}
