package mdplayer.form.kb;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmChipBase extends frmBase {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    protected int frameSizeW = 0;
    protected int frameSizeH = 0;
    protected int chipID = 0;
    protected int zoom = 1;

    protected MDChipParams.AY8910 newParam = null;
    protected MDChipParams.AY8910 oldParam = null;

    protected FrameBuffer frameBuffer = new FrameBuffer();

    private void initializeComponent() {
        //
        // frmChipBase
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(329, 57));
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmChipBase");
        this.setTitle("frmChipBase");
        this.addWindowListener(this.windowListener);
        this.addMouseListener(this.pbScreen_MouseClick);
//            this.ResumeLayout(false);
    }

    public frmChipBase() {
        initializeComponent();
    }

    public frmChipBase(frmMain frm, int chipID, int zoom, MDChipParams.AY8910 newParam) {
        super(frm);
        parent = frm;
        this.chipID = chipID;
        this.zoom = zoom;
        this.newParam = newParam;

        initializeComponent();
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }
    };

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
        }
    };

    public void update() {
        frameBuffer.Refresh(null);
    }

    //@Override
    protected Boolean getShowWithoutActivation() {
        return true;
    }


    public void screenChangeParams() {
    }

    public void screenDrawParams() {
    }

    public void screenInit() {
    }
}
