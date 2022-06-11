package mdplayer.form.kb.pcm;

import java.awt.Dimension;

import mdplayer.form.frmBase;


public class frmPWM extends frmBase {
    public frmPWM() {
        initializeComponent();
    }

    private void initializeComponent() {

        //
        // frmPWM
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(800, 450));
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
//        this.MaximizeBox = false;
        this.setName("frmPWM");
        this.setTitle("frmPWM");
    }
}

