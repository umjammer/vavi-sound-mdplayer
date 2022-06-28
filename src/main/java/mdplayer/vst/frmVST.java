package mdplayer.vst;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.Timer;

import mdplayer.properties.Resources;
import org.urish.jnavst.VstPlugin;


public class frmVST extends JDialog {

    int dialogResult;

    Rectangle wndRect = new Rectangle();

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
     * Shows the custom plugin editor UI.
     */
    public int ShowDialog() {

        this.setTitle(PluginCommandStub.GetEffectName());

        if (PluginCommandStub.EditorGetRect(wndRect)) {
            this.setPreferredSize(this.SizeFromClientSize(new Dimension(wndRect.width, wndRect.height)));
            PluginCommandStub.EditorOpen(this.Handle);
        }

        super.setVisible(true);
        return dialogResult;
    }

    public void Show(VstMng.VstInfo2 vi) {

        this.setText(PluginCommandStub.GetEffectName());

        if (PluginCommandStub.EditorGetRect(wndRect)) {
            this.setPreferredSize(this.SizeFromClientSize(new Dimension(wndRect.width, wndRect.height)));
            PluginCommandStub.EditorOpen(this.Handle);
        }
        this.setLocation(new Point(vi.location.x, vi.location.y));
        super.setVisible(true);
    }

//    @Override
    protected void OnClosing(System.ComponentModel.CancelEventArgs ev) {
        super.OnClosing(e);

        if (e.Cancel == false) {
            PluginCommandStub.EditorClose();
        }
    }

    private void timer1_Tick(ActionEvent ev) {
        try {
            PluginCommandStub.EditorIdle();
            if (PluginCommandStub.EditorGetRect(wndRect)) {
                this.setPreferredSize(this.SizeFromClientSize(new Dimension(wndRect.width, wndRect.height)));
            }
        } catch (Exception ignored) {
        }
    }

    private void initializeComponent() {
//            this.components = new System.ComponentModel.Container();
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmVST));
        this.timer1 = new Timer(20, this::timer1_Tick);

        //
        // timer1
        //
//        this.timer1.setEnabled(true);
//        this.timer1.Interval = 20;
//        this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
        this.timer1.start();
        //
        // frmVST
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(284, 261));
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmVST");
//        this.ShowIcon = false;
        this.setTitle("frmVST");
//            this.ResumeLayout(false);
    }

    public Timer timer1;
}
