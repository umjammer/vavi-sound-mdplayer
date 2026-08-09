package mdplayer.form;

import java.awt.Dimension;
import javax.swing.JFrame;

import mdplayer.Audio;
import mdplayer.form.sys.FormMain;


public class FormBase extends JFrame {

    protected FormMain parent = null;
    protected final Audio audio = Audio.getInstance();

    private void initializeComponent() {
        //
        // frmBase
        //
        this.setPreferredSize(new Dimension(323, 303));
        this.setName("frmBase");
        this.setTitle("frmBase");
    }

    protected FormBase() {
        initializeComponent();
    }

    public FormBase(FormMain frm) {
        parent = frm;
        initializeComponent();
    }
}
