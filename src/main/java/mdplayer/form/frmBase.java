
package mdplayer.form;

import java.awt.Dimension;
import javax.swing.JFrame;

import mdplayer.form.sys.frmMain;


public class frmBase extends JFrame {

    public frmMain parent = null;

    /**
     //Required method for Designer support - do not modify
     //the contents of this method with the code editor.
     */
    private void initializeComponent()    {
//        this.SuspendLayout();
        //
        // frmBase
        //
//        this.AutoScaleDimensions = new DimensionF(6F, 12F);
//        this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(323, 303));
        this.setName("frmBase");
        this.setTitle("frmBase");
//        this.ResumeLayout(false);
    }

    public frmBase() {
        initializeComponent();
    }

    public frmBase(frmMain frm) {
        parent = frm;
        initializeComponent();
    }

//    @Override
//    protected void WndProc(Message m) {
//        if (parent != null) {
//            parent.windowsMessage(m);
//        }
//
//        try {
//
//            int WM_NCLBUTTONDBLCLK = 0xA3;
//            if (m.Msg == WM_NCLBUTTONDBLCLK) {
//                TopMost = !TopMost;
//                if (TopMost)
//                    this.Icon = Resources.FeliTop;
//                else
//                    this.Icon = Resources.Feli128;
//            }
//            super.WndProc(m);
//        } catch (Exception ex) {
//            Log.ForcedWrite(ex);
//        }
//    }
}
