package mdplayer.vst;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.JDialog;
import javax.swing.Timer;

import mdplayer.properties.Resources;
import org.urish.jnavst.ERect;
import org.urish.jnavst.VstPlugin;

import static java.lang.System.getLogger;


public class frmVST extends JDialog {

    private static final Logger logger = getLogger(frmVST.class.getName());

    int dialogResult;

    ERect wndRect;

    public frmVST(Frame owner) {
        super(owner, true);
        initializeComponent();
    }

    private VstPlugin PluginCommandStub;
    /**
     * Gets or sets the Plugin Command Stub.
     */
    public VstPlugin getPluginCommandStub() {
        return PluginCommandStub;
    }

    public void setPluginCommandStub(VstPlugin value) {
        PluginCommandStub = value;
    }

    /**
     * Shows the custom chips editor UI.
     */
    public int ShowDialog() {

        this.setTitle(PluginCommandStub.getName());

        if ((wndRect = PluginCommandStub.getEditRect()) != null) {
            this.setPreferredSize(new Dimension(wndRect.right - wndRect.left, wndRect.bottom - wndRect.top));
            PluginCommandStub.editOpen("");
        }

        super.setVisible(true);
        return dialogResult;
    }

    public void Show(VstMng.VstInfo2 vi) {

        this.setTitle(PluginCommandStub.getName());

        if ((wndRect = PluginCommandStub.getEditRect()) != null) {
            this.setPreferredSize(new Dimension(wndRect.right - wndRect.left, wndRect.bottom - wndRect.top));
            PluginCommandStub.editOpen("");
        }
        this.setLocation(new Point(vi.location.x, vi.location.y));
        super.setVisible(true);
    }

//    @Override
    protected void OnClosing(WindowEvent ev) {
        PluginCommandStub.editClose();
    }

    private void timer1_Tick(ActionEvent ev) {
        try {
            PluginCommandStub.editIdle();
            if ((wndRect = PluginCommandStub.getEditRect()) != null) {
                this.setPreferredSize(new Dimension(wndRect.right - wndRect.left, wndRect.bottom - wndRect.top));
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private void initializeComponent() {
        this.timer1 = new Timer(20, this::timer1_Tick);

        //
        // timer1
        //
        this.timer1.start();
        //
        // frmVST
        //
        this.setPreferredSize(new Dimension(284, 261));
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
        this.setName("frmVST");
        this.setTitle("frmVST");
        this.pack();
    }

    public Timer timer1;
}
